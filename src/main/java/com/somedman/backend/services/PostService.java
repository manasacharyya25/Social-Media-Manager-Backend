package com.somedman.backend.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somedman.backend.entities.Post;
import com.somedman.backend.entities.UserAccessTokens;
import com.somedman.backend.entities.UserSetting;
import com.somedman.backend.entities.WebResponse;
import com.somedman.backend.repository.PostsRepository;
import com.somedman.backend.repository.UserAccessTokenRepository;
import com.somedman.backend.repository.UserSettingsRepository;
import com.somedman.backend.utills.ApplicationConstants;
import com.somedman.backend.utills.CustomUtils;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class PostService
{
  @Autowired
  private PostsRepository postsRepository;
  @Autowired
  private UserSettingsRepository userSettingsRepository;
  @Autowired
  private UserAccessTokenRepository uatRepo;

  public WebResponse publishPosts(Post post)
      throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, URISyntaxException
  {
    try
    {
      StringBuilder response = new StringBuilder();
      int userId = post.getUserId();


      //    post.setImageData(CustomUtils.compressString(post.getImage()));
      //    Post savedPost = this.postsRepository.save(post);
      //      return savedPost.getPostId();

      UserSetting userSetting = userSettingsRepository.findByUserId(userId);

      if (userSetting != null)
      {
        if (userSetting.isTumblrIntegrated())
        {
          response.append(PostToTumblr(post));
        }
        if (userSetting.isTwitterIntegrated())
        {
          response.append(PostToTwitter(post));
        }
        if (userSetting.isFacebookIntegrated())
        {
          response.append(PostToFacebook(post));
        }
      }
      return WebResponse.builder()
          .responseCode(ApplicationConstants.SUCCESS_RESPONSE)
          .responseMessage(response.toString())
          .responseDetails(ApplicationConstants.POST_REQUEST)
          .build();
    } catch (Exception ex) {
      return WebResponse.builder()
          .responseCode(ApplicationConstants.FAILURE_RESPONSE)
          .responseMessage(ex.getMessage())
          .responseDetails(ApplicationConstants.POST_REQUEST)
          .build();
    }

  }

  private String PostToFacebook(Post post) throws IOException
  {
    try {
      UserAccessTokens uat = this.uatRepo.findByUserId(post.getUserId());
      String pageLLAT = uat.getFacebookAccessToken();
      String pageId = uat.getFacebookPageId();

      CloseableHttpClient httpclient = HttpClients.createDefault();
      HttpPost httpPost = new HttpPost(String.format("https://graph.facebook.com/v9.0/%s/photos", pageId));

      java.io.File outputfile = getFileFromBase64(post);

      String postText = String.format("%s\n"
          + ".\n"
          + ".\n"
          + ".\n"
          + ".\n"
          + ".\n"
          + "%s\n"
          + "%s",post.getCaption(), getEmptyStringIfNull(post.getContent()), getTags(post.getTags()));

      HttpEntity entity = MultipartEntityBuilder
          .create()
          .addBinaryBody("source", outputfile)
          .addTextBody("caption", postText)
          .addTextBody("access_token", pageLLAT)
          .build();

      httpPost.setEntity(entity);
      httpclient.execute(httpPost);

      return ApplicationConstants.FACEBOOK+" ";
    } catch (Exception ex) { return ""; }
  }

  private String PostToTwitter(Post post)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException, URISyntaxException
  {
    try {
      OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
          "gmV1QfUPuoy5g9bwmlfj4muur",
          "hAvPcBqijFKMqbWaaQGeypyOEWQkcrLsgCdubJNE2zwCY2hoju"
      );

      UserAccessTokens uat = this.uatRepo.findByUserId(post.getUserId());

      consumer.setTokenWithSecret(uat.getTwitterAccessToken(), uat.getTwitterAccessTokenSecret());

      CloseableHttpClient httpclient = HttpClients.createDefault();

      HttpPost httpPost = new HttpPost("https://upload.twitter.com/1.1/media/upload.json");

      String imageBase64EncodedString =  post.getImage().split(",")[1];

      HttpEntity entity = MultipartEntityBuilder
          .create()
          .addTextBody("media_category", "tweet_image")
          .addTextBody("media_data", imageBase64EncodedString)
          .build();

      httpPost.setEntity(entity);
      consumer.sign(httpPost);

      CloseableHttpResponse response = httpclient.execute(httpPost);

      String responseJson =  EntityUtils.toString(response.getEntity());

      JsonNode blogsNode = new ObjectMapper(new JsonFactory()).readTree(responseJson).get("media_id");

      String postText = String.format("%s\n"
              + ".\n"
              + ".\n"
              + ".\n"
              + ".\n"
              + ".\n"
              + "%s\n"
              + "%s", post.getCaption(), getEmptyStringIfNull(post.getContent()), getTags(post.getTags()));

      URIBuilder builder = new URIBuilder("https://api.twitter.com/1.1/statuses/update.json");
      builder.setParameter("media_ids", blogsNode.asText()).setParameter("status", postText);

      HttpPost httpPost2 = new HttpPost(builder.build());



      consumer.sign(httpPost2);

      httpclient.execute(httpPost2);

      return ApplicationConstants.TWITTER+" ";
    } catch (Exception ex) { return ""; }
  }

  private String PostToTumblr(Post post) throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException
  {
    try {
      OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
          "olqhlNchIoMOxelBmNcIgcTjTJ4kdh7VVCNmBzMxi7HREothkJ",
          "crF13d1aP4uc1YQSg04QmpawGazHMhEF9RMDwva0NaKWxU1IHX"
      );

      UserAccessTokens uat = this.uatRepo.findByUserId(post.getUserId());

      String blogUuid = uat.getTumblrPageId();

      consumer.setTokenWithSecret(uat.getTumblrAccessToken(), uat.getTumblrAccessTokenSecret());

      CloseableHttpClient httpclient = HttpClients.createDefault();

      HttpPost httpPost = new HttpPost(String.format("https://api.tumblr.com/v2/blog/%s/post", blogUuid));

      String imageBase64EncodedString =  post.getImage().split(",")[1];

      String postText = String.format("<h1>%s</h1>"
          + "<br />"
          + "<i>%s</i>",post.getCaption(), getEmptyStringIfNull(post.getContent()));

      HttpEntity entity = MultipartEntityBuilder
          .create()
          .addTextBody("type", "photo")
          .addTextBody("caption", postText)
          .addTextBody("data64", imageBase64EncodedString)
          .addTextBody("tags", getTumblrTags(post.getTags()))
          .build();

      httpPost.setEntity(entity);
      consumer.sign(httpPost);

      httpclient.execute(httpPost);

      return ApplicationConstants.TUMBLR+" ";

    } catch(Exception ex) { return ""; }
  }

  private String getTumblrTags(String tags)
  {
    if (tags == null)
      return "";

    return tags.replace("#","").trim().replace(" ",",");
  }

  public List<Post> getPostsByUser(int userId) throws IOException
  {
  List<Post> retrievedPosts = this.postsRepository.findByUserId(userId);
    for (Post post : retrievedPosts)
    {
      post.setImage(CustomUtils.uncompressString(post.getImageData()));
    }
    return retrievedPosts;
  }

  private java.io.File getFileFromBase64(Post post) throws IOException
  {
    String imageBase64EncodedString =  post.getImage().split(",")[1];

    BASE64Decoder decoder = new BASE64Decoder();
    byte[] imageByte = decoder.decodeBuffer(imageBase64EncodedString);
    ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
    BufferedImage image = ImageIO.read(bis);
    bis.close();

    // write the image to a file
    java.io.File outputfile = new java.io.File("imageFile");

    ImageIO.write(image, "png", outputfile);
    return outputfile;
  }

  private String getEmptyStringIfNull(String content)
  {
    return content == null ? "" : content;
  }

  private String getTags(String tags) {
    if (tags==null)
      return "";

    return "#".concat(tags.replace("#", "").trim().replace(" ", " #"));
  }
}

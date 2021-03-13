package com.somedman.backend.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somedman.backend.entities.Post;
import com.somedman.backend.entities.UserAccessTokens;
import com.somedman.backend.entities.UserSetting;
import com.somedman.backend.repository.PostsRepository;
import com.somedman.backend.repository.UserAccessTokenRepository;
import com.somedman.backend.repository.UserSettingsRepository;
import com.somedman.backend.utills.CustomUtils;
import lombok.var;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.jni.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

  public int publishPosts(Post post)
      throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, URISyntaxException
  {
    int userId = post.getUserId();

    post.setImageData(CustomUtils.compressString(post.getImage()));
    Post savedPost = this.postsRepository.save(post);

    UserSetting userSetting = userSettingsRepository.findByUserId(userId);

    if (userSetting != null) {
      if (userSetting.isTumblrIntegrated()) {
        PostToTumblr(post, userSetting.getTumblrBlogUuid());
      }
      if (userSetting.isTwitterIntegrated()) {
        PostToTwitter(post);
      }
      if (userSetting.isFacebookIntegrated()) {
        PostToFacebook(post, userSetting.getFacebookPageId());
      }
    }

    return savedPost.getPostId();
  }

  private void PostToFacebook(Post post, String pageId) throws IOException
  {
    String pageLLAT = this.uatRepo.findByUserId(post.getUserId()).getFacebookAccessToken();

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(String.format("https://graph.facebook.com/v9.0/%s/photos", pageId));

    java.io.File outputfile = getFileFromBase64(post);

    HttpEntity entity = MultipartEntityBuilder
        .create()
        .addBinaryBody("source", outputfile)
        .addTextBody("caption", post.getCaption())
        .addTextBody("access_token", pageLLAT)
        .build();

    httpPost.setEntity(entity);
    CloseableHttpResponse response = httpclient.execute(httpPost);
  }

  private void PostToTwitter(Post post)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException, URISyntaxException
  {
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

    URIBuilder builder = new URIBuilder("https://api.twitter.com/1.1/statuses/update.json");
    builder.setParameter("media_ids", blogsNode.asText()).setParameter("status", post.getCaption());

    HttpPost httpPost2 = new HttpPost(builder.build());

    HttpEntity entity2 = MultipartEntityBuilder
        .create()
        .addTextBody("media_ids", blogsNode.asText())
        .addTextBody("status", post.getCaption())
        .build();

    httpPost2.setEntity(entity2);
    consumer.sign(httpPost2);

    response = httpclient.execute(httpPost2);

  }

  private void PostToTumblr(Post post, String blogUuid) throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException
  {
    OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
        "olqhlNchIoMOxelBmNcIgcTjTJ4kdh7VVCNmBzMxi7HREothkJ",
        "crF13d1aP4uc1YQSg04QmpawGazHMhEF9RMDwva0NaKWxU1IHX"
    );

    UserAccessTokens uat = this.uatRepo.findByUserId(post.getUserId());

    consumer.setTokenWithSecret(uat.getTumblrAccessToken(), uat.getTumblrAccessTokenSecret());

    CloseableHttpClient httpclient = HttpClients.createDefault();

    HttpPost httpPost = new HttpPost(String.format("https://api.tumblr.com/v2/blog/%s/post", blogUuid));

    String imageBase64EncodedString =  post.getImage().split(",")[1];

    HttpEntity entity = MultipartEntityBuilder
        .create()
        .addTextBody("type", "photo")
        .addTextBody("caption", post.getCaption())
        .addTextBody("data64", imageBase64EncodedString)
        .addTextBody("tags", post.getTags())
        .build();

    httpPost.setEntity(entity);
    consumer.sign(httpPost);


    CloseableHttpResponse response = httpclient.execute(httpPost);
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
}

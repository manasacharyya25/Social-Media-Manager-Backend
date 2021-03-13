package com.somedman.backend.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somedman.backend.entities.*;
import com.somedman.backend.repository.UserAccessTokenRepository;
import com.somedman.backend.repository.UserSettingsRepository;
import com.somedman.backend.utills.ApplicationConstants;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Service
@Scope("request")
public class SocialService
{
  @Autowired
  private UserAccessTokenRepository uatRepo;

  @Autowired
  private UserSettingsRepository userSettingsRepo;

  @Value("${host-url}")
  private String hostUrl;

  // TODO: Change to Redis Cache
  @Autowired
  private InMemoryCache cache;

  public ResponseObject integrateTumblr(int userId)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
        "olqhlNchIoMOxelBmNcIgcTjTJ4kdh7VVCNmBzMxi7HREothkJ",
        "crF13d1aP4uc1YQSg04QmpawGazHMhEF9RMDwva0NaKWxU1IHX"
    );

    OAuthProvider provider =  new CommonsHttpOAuthProvider(
        "https://www.tumblr.com/oauth/request_token",
        "https://www.tumblr.com/oauth/access_token",
        "https://www.tumblr.com/oauth/authorize"
    );

    String authUrl = provider.retrieveRequestToken(consumer,
        String.format("%s/social/tumblr/authorize/%d", hostUrl, userId));

    String oauthToken = authUrl.split("=")[1];

    cache.oauthTokenToConsumerMap.put(oauthToken, consumer);
    cache.oauthTokenToProviderMap.put(oauthToken, provider);

    return new ResponseObject(authUrl);
  }

  public void handleAuthorizeCallback(String platform, int userId, String oauthToken, String oauthVerifier)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException
  {
    OAuthConsumer consumer = cache.oauthTokenToConsumerMap.get(oauthToken);
    OAuthProvider provider = cache.oauthTokenToProviderMap.get(oauthToken);

    provider.retrieveAccessToken(consumer, oauthVerifier);

    StoreAccessTokens(platform, userId, consumer.getToken(), consumer.getTokenSecret());
  }

  private void StoreAccessTokens(String platform, int userId, String accessToken, String accessTokenSecret)
  {
    //TODO: Create UAT Object for each new user signed in
    UserAccessTokens uat = this.uatRepo.findByUserId(userId);

    switch(platform) {
      case "TUMBLR":
        uat.setTumblrAccessToken(accessToken);
        uat.setTumblrAccessTokenSecret(accessTokenSecret);
        break;
      case "TWITTER":
        uat.setTwitterAccessToken(accessToken);
        uat.setTwitterAccessTokenSecret(accessTokenSecret);
        break;
      case "FACEBOOK":
          uat.setFacebookAccessToken(accessToken);
      default:
        break;
    }

    this.uatRepo.save(uat);
  }

  public ResponseObject getTumblrBlogsByUserId(int userId)
      throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException
  {
    OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
        "olqhlNchIoMOxelBmNcIgcTjTJ4kdh7VVCNmBzMxi7HREothkJ",
        "crF13d1aP4uc1YQSg04QmpawGazHMhEF9RMDwva0NaKWxU1IHX"
    );

    UserAccessTokens uat = this.uatRepo.findByUserId(userId);

    consumer.setTokenWithSecret(uat.getTumblrAccessToken(), uat.getTumblrAccessTokenSecret());

    CloseableHttpClient httpclient = HttpClients.createDefault();

    //Creating a HttpGet object
    HttpGet httpget = new HttpGet("https://api.tumblr.com/v2/user/info");

    consumer.sign(httpget);

    CloseableHttpResponse response = httpclient.execute(httpget);

    String responseJson =  EntityUtils.toString(response.getEntity());

    JsonNode blogsNode = new ObjectMapper(new JsonFactory()).readTree(responseJson).get("response").get("user").get("blogs");

    ArrayList<Blog> blogs = new ArrayList<Blog>();


    for( JsonNode blog : blogsNode) {
      if (blog.get("admin").booleanValue()) {
        String blogId = blog.get("uuid").textValue();

        //TODO: Remove Later
        StoreAssociatedPageIds(ApplicationConstants.TUMBLR, userId, blogId);

        String blogUrl = blog.get("url").textValue();
        String blogTitle = blog.get("title").textValue();
        String blogAvatar = blog.get("avatar").get(3).get("url").textValue();

        blogs.add(Blog.builder().BlogId(blogId).BlogUrl(blogUrl).BlogName(blogTitle).BlogImageUrl(blogAvatar).build());
      }
    }
    return new ResponseObject(blogs);
  }

  public ResponseObject integrateTwitter(int userId)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException
  {
    OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
        "gmV1QfUPuoy5g9bwmlfj4muur",
        "hAvPcBqijFKMqbWaaQGeypyOEWQkcrLsgCdubJNE2zwCY2hoju"
    );

    OAuthProvider provider =  new CommonsHttpOAuthProvider(
        "https://api.twitter.com/oauth/request_token",
        "https://api.twitter.com/oauth/access_token",
        "https://api.twitter.com/oauth/authorize"
    );

//    String authUrl = provider.retrieveRequestToken(consumer,
//        String.format("http://example.com", ApplicationConstants.HOST_URL, userId));

    String authUrl = provider.retrieveRequestToken(consumer,
        String.format("%s/social/twitter/authorize?userId=%d", hostUrl, userId));

    String oauthToken = authUrl.split("=")[1];

    cache.oauthTokenToConsumerMap.put(oauthToken, consumer);
    cache.oauthTokenToProviderMap.put(oauthToken, provider);

    return new ResponseObject(authUrl);
  }

  public void integrateFacebook(FbShortLivedUAT sluat) throws IOException
  {
    CloseableHttpClient httpclient = HttpClients.createDefault();

    //Creating a HttpGet object
    HttpGet getLongLivedUserAccessToken = new HttpGet(String.format("https://graph.facebook.com/v9.0/oauth/access_token?"
        + "grant_type=fb_exchange_token&client_id=836109743840031&"
        + "client_secret=98821c425374c1d6248b7715d4cf70c5&"
        + "fb_exchange_token=%s", sluat.uat));

    CloseableHttpResponse response = httpclient.execute(getLongLivedUserAccessToken);

    String responseJson =  EntityUtils.toString(response.getEntity());

    String longLivedUserAccessToken = new ObjectMapper(new JsonFactory()).readTree(responseJson).get("access_token").textValue();

    HttpGet getLongLivedPageAccessToken = new HttpGet(String.format("https://graph.facebook.com/v9.0/%s/accounts?"
        + "access_token=%s",sluat.fbUserId, longLivedUserAccessToken));

    response = httpclient.execute(getLongLivedPageAccessToken);

    responseJson =  EntityUtils.toString(response.getEntity());

    JsonNode pageAccessTokenNode = new ObjectMapper(new JsonFactory()).readTree(responseJson).get("data").get(0);

    String pageLLAT = pageAccessTokenNode.get("access_token").asText();
    String pageId = pageAccessTokenNode.get("id").asText();

    StoreAccessTokens(ApplicationConstants.FACEBOOK, sluat.userId, pageLLAT, null);
    StoreAssociatedPageIds(ApplicationConstants.FACEBOOK, sluat.userId, pageId);
  }

  private void StoreAssociatedPageIds(String platform, int userId, String pageId)
  {
    UserSetting userSetting = userSettingsRepo.findByUserId(userId);

    switch(platform) {
      case "FACEBOOK":
        userSetting.setFacebookPageId(pageId);
        break;
      case "TUMBLR":
        userSetting.setTumblrBlogUuid(pageId);
        break;
      default:
        break;
    }

    userSettingsRepo.save(userSetting);
  }
}

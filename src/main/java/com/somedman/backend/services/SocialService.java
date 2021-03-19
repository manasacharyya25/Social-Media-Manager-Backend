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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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

  public WebResponse integrateTumblr(int userId)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    try {
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

      return WebResponse.builder()
          .responseCode(ApplicationConstants.SUCCESS_RESPONSE)
          .responseMessage(authUrl)
          .responseDetails(ApplicationConstants.CALLBACK_URL).build();
    }
    catch (Exception ex) {
      return WebResponse.builder()
          .responseCode(ApplicationConstants.FAILURE_RESPONSE)
          .responseMessage(ex.getMessage())
          .responseDetails(ApplicationConstants.CALLBACK_URL)
          .build();
    }

  }

  public WebResponse integrateTwitter(int userId)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException
  {
    try
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

      String authUrl = provider.retrieveRequestToken(consumer,
          String.format("%s/social/twitter/authorize?userId=%d", hostUrl, userId));

      String oauthToken = authUrl.split("=")[1];

      cache.oauthTokenToConsumerMap.put(oauthToken, consumer);
      cache.oauthTokenToProviderMap.put(oauthToken, provider);

      return WebResponse.builder()
          .responseCode(ApplicationConstants.SUCCESS_RESPONSE)
          .responseMessage(authUrl)
          .responseDetails(ApplicationConstants.CALLBACK_URL).build();
    }
    catch (Exception ex) {
      return WebResponse.builder()
          .responseCode(ApplicationConstants.FAILURE_RESPONSE)
          .responseMessage(ex.getMessage())
          .responseDetails(ApplicationConstants.CALLBACK_URL)
          .build();
    }
  }

  public WebResponse integrateFacebook(FbShortLivedUAT sluat)
      throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException
  {
    try {
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

      //NOTE: Access Token Secret Doesnot Exist for Facebook. Parameter is used ot pass pageId
      StoreAccessTokens(ApplicationConstants.FACEBOOK, sluat.userId, pageLLAT, pageId);

      return WebResponse.builder()
          .responseCode(ApplicationConstants.SUCCESS_RESPONSE)
          .build();
    }
    catch (Exception ex) {
      return WebResponse.builder()
          .responseCode(ApplicationConstants.FAILURE_RESPONSE)
          .responseMessage(ex.getMessage())
          .build();
    }
  }

  public String handleAuthorizeCallback(String platform, int userId, String oauthToken, String oauthVerifier)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    try {
      OAuthConsumer consumer = cache.oauthTokenToConsumerMap.get(oauthToken);
      OAuthProvider provider = cache.oauthTokenToProviderMap.get(oauthToken);

      provider.retrieveAccessToken(consumer, oauthVerifier);

      StoreAccessTokens(platform, userId, consumer.getToken(), consumer.getTokenSecret());

      return String.format(ApplicationConstants.AUTHORIZE_HTML_RESPONSE, platform, ApplicationConstants.SUCCESS_RESPONSE);
    }
    catch (Exception ex)
    {
      return String.format(ApplicationConstants.AUTHORIZE_HTML_RESPONSE, platform, ApplicationConstants.FAILURE_RESPONSE);
    }

  }

  private void StoreAccessTokens(String platform, int userId, String accessToken, String accessTokenSecret)
      throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException
  {
    //TODO: Create UAT Object for each new user signed in
    UserAccessTokens uat = this.uatRepo.findByUserId(userId);

    switch(platform) {
      case "TUMBLR":
        uat.setTumblrAccessToken(accessToken);
        uat.setTumblrAccessTokenSecret(accessTokenSecret);
        uat.setTumblrPageId(getTumblrBlogsByUserId(userId, accessToken, accessTokenSecret));
        break;
      case "TWITTER":
        uat.setTwitterAccessToken(accessToken);
        uat.setTwitterAccessTokenSecret(accessTokenSecret);
        break;
      case "FACEBOOK":
          uat.setFacebookAccessToken(accessToken);
          uat.setFacebookPageId(accessTokenSecret);
      default:
        break;
    }

    this.uatRepo.save(uat);
  }

  private String getTumblrBlogsByUserId(int userId, String accessToken, String accessTokenSecret)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException
  {
    OAuthConsumer consumer =
        new CommonsHttpOAuthConsumer("olqhlNchIoMOxelBmNcIgcTjTJ4kdh7VVCNmBzMxi7HREothkJ", "crF13d1aP4uc1YQSg04QmpawGazHMhEF9RMDwva0NaKWxU1IHX");

    consumer.setTokenWithSecret(accessToken, accessTokenSecret);

    CloseableHttpClient httpclient = HttpClients.createDefault();

    //Creating a HttpGet object
    HttpGet httpget = new HttpGet("https://api.tumblr.com/v2/user/info");

    consumer.sign(httpget);

    CloseableHttpResponse response = httpclient.execute(httpget);

    String responseJson = EntityUtils.toString(response.getEntity());

    JsonNode blogsNode = new ObjectMapper(new JsonFactory()).readTree(responseJson).get("response").get("user").get("blogs");

    ArrayList<Blog> blogs = new ArrayList<Blog>();

    for (JsonNode blog : blogsNode)
    {
      if (blog.get("admin").booleanValue())
      {
        String blogId = blog.get("uuid").textValue();
        return blogId;
      }
    }
    return null;
  }
}

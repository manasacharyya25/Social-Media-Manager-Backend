package com.somedman.backend.services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somedman.backend.entities.Blog;
import com.somedman.backend.entities.ResponseObject;
import com.somedman.backend.entities.UserAccessTokens;
import com.somedman.backend.repository.UserAccessTokenRepository;
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

  // TODO: Change to Redis Cache
  @Autowired
  private InMemoryCache cache;

  public ResponseObject integrateTumblr(int userId)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException
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
        String.format("%s/social/tumblr/authorize/%d", ApplicationConstants.HOST_URL, userId));

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
        String.format("%s/social/twitter/authorize?userId=%d", ApplicationConstants.HOST_URL, userId));

    String oauthToken = authUrl.split("=")[1];

    cache.oauthTokenToConsumerMap.put(oauthToken, consumer);
    cache.oauthTokenToProviderMap.put(oauthToken, provider);

    return new ResponseObject(authUrl);
  }
}

package com.somedman.backend.services;

import com.somedman.backend.entities.UrlObject;
import com.somedman.backend.entities.UserAccessTokens;
import com.somedman.backend.repository.UserAccessTokenRepository;
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

@Service
@Scope("request")
public class SocialService
{
  @Autowired
  private UserAccessTokenRepository uatRepo;

  // TODO: Change to Redis Cache
  @Autowired
  private InMemoryCache cache;

  SocialService() {
    OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
        "olqhlNchIoMOxelBmNcIgcTjTJ4kdh7VVCNmBzMxi7HREothkJ",
        "crF13d1aP4uc1YQSg04QmpawGazHMhEF9RMDwva0NaKWxU1IHX"
    );

    OAuthProvider provider =  new CommonsHttpOAuthProvider(
        "https://www.tumblr.com/oauth/request_token",
        "https://www.tumblr.com/oauth/access_token",
        "https://www.tumblr.com/oauth/authorize"
    );
  }

  public UrlObject integrateTumblr(int userId)
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
        String.format("http://localhost:8080/social/tumblr/authorize/%d", userId));

    String oauthToken = authUrl.split("=")[1];

    cache.oauthTokenToConsumerMap.put(oauthToken, consumer);
    cache.oauthTokenToProviderMap.put(oauthToken, provider);

    return new UrlObject(authUrl);
  }

  public void handleAuthorizeCallback(int userId, String oauthToken, String oauthVerifier)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException
  {
    OAuthConsumer consumer = cache.oauthTokenToConsumerMap.get(oauthToken);
    OAuthProvider provider = cache.oauthTokenToProviderMap.get(oauthToken);

    provider.retrieveAccessToken(consumer, oauthVerifier);

    String accessToken = consumer.getToken();
    String accessTokenSecret = consumer.getTokenSecret();

//    this.uatRepo.findByUserId(userId);

    UserAccessTokens uat = new UserAccessTokens();
    uat.setUserId(userId);
    uat.setTumblrAccessToken(accessToken);
    uat.setTumblrAccessTokenSecret(accessTokenSecret);

    this.uatRepo.save(uat);
  }

  public String getTumblrBlogsByUserId(int userId)
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

    return EntityUtils.toString(response.getEntity());
  }
}

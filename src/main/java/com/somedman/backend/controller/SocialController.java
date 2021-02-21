package com.somedman.backend.controller;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.OAuthProviderListener;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpResponse;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

@RestController
@RequestMapping("social")
@CrossOrigin(origins = "*")
public class SocialController
{
  private static HashMap<String, OAuthConsumer> cache;
  private static OAuthConsumer consumer;
  private static OAuthProvider provider;

  SocialController() {
    cache = new HashMap<String, OAuthConsumer>();

    consumer = new CommonsHttpOAuthConsumer(
        "olqhlNchIoMOxelBmNcIgcTjTJ4kdh7VVCNmBzMxi7HREothkJ",
        "crF13d1aP4uc1YQSg04QmpawGazHMhEF9RMDwva0NaKWxU1IHX"
    );

    provider =  new CommonsHttpOAuthProvider(
        "https://www.tumblr.com/oauth/request_token",
        "https://www.tumblr.com/oauth/access_token",
        "https://www.tumblr.com/oauth/authorize"
    );
  }

  @GetMapping("/tumblr/initialize")
  public String integrateTumblr() throws
      OAuthCommunicationException,
      OAuthExpectationFailedException,
      OAuthNotAuthorizedException,
      OAuthMessageSignerException
  {
    String authUrl = provider.retrieveRequestToken(consumer, "http://localhost:8080/social/tumblr/authorize");

    String oauthToken = authUrl.split("=")[1];

    cache.put(oauthToken, consumer);

    return authUrl;
  }

  @GetMapping("/tumblr/authorize")
  public String AuthorizeTumblr(@RequestParam(name = "oauth_token") String oauthToken,
      @RequestParam (name = "oauth_verifier") String oauthVerifier)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    OAuthConsumer consumer = cache.get(oauthToken);

    provider.retrieveAccessToken(consumer, oauthVerifier);

    String accessToken = consumer.getToken();
    String accessTokenSecret = consumer.getTokenSecret();

    // store consumer.getToken() and consumer.getTokenSecret(),
    // for the current user, e.g. in a relational database
    // or a flat file
    // ...

    /****************************************************
     * The following steps are performed everytime you
     * send a request accessing a resource on Twitter
     ***************************************************/

    // if not yet done, load the token and token secret for
    // the current user and set them
    consumer.setTokenWithSecret(accessToken, accessTokenSecret);

    // create a request that requires authentication

    //Creating a HttpClient object
    CloseableHttpClient httpclient = HttpClients.createDefault();

    //Creating a HttpGet object
    HttpGet httpget = new HttpGet("https://api.tumblr.com/v2/user/info");

    consumer.sign(httpget);

    CloseableHttpResponse response = httpclient.execute(httpget);

    return EntityUtils.toString(response.getEntity());
  }
}

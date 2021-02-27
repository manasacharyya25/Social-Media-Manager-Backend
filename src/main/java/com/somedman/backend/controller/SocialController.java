package com.somedman.backend.controller;

import com.somedman.backend.entities.UrlObject;
import com.somedman.backend.services.InMemoryCache;
import com.somedman.backend.services.SocialService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

@RestController
@RequestMapping("social")
@CrossOrigin(origins = "*")
@Scope("request")
public class SocialController
{
  @Autowired
  private SocialService socialService;

  @GetMapping("/tumblr/initialize/{userId}")
  public UrlObject integrateTumblr(@PathVariable("userId") int userId) throws
      OAuthCommunicationException,
      OAuthExpectationFailedException,
      OAuthNotAuthorizedException,
      OAuthMessageSignerException
  {
    return this.socialService.integrateTumblr(userId);
  }

  @GetMapping("/tumblr/authorize/{userId}")
  public void AuthorizeTumblr(@PathVariable("userId") int userId, @RequestParam(name = "oauth_token") String oauthToken,
      @RequestParam (name = "oauth_verifier") String oauthVerifier)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException
  {
    this.socialService.handleAuthorizeCallback(userId, oauthToken, oauthVerifier);
  }

  @GetMapping("/tumblr/BlogsList/{userId}")
  public String GetAllBlogsByUserId(@PathVariable("userId") int userId)
      throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException
  {
    return this.socialService.getTumblrBlogsByUserId(userId);
  }
}

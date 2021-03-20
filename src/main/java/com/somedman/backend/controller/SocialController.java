package com.somedman.backend.controller;

import com.somedman.backend.entities.Blog;
import com.somedman.backend.entities.FbShortLivedUAT;
import com.somedman.backend.entities.ResponseObject;
import com.somedman.backend.entities.WebResponse;
import com.somedman.backend.services.SocialService;
import com.somedman.backend.utills.ApplicationConstants;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Controller
@RequestMapping("social")
@CrossOrigin(origins = "*")
@Scope("request")
public class SocialController
{
  @Autowired
  private SocialService socialService;

  /** TUMBLR APIs **/
  @GetMapping("/tumblr/initialize/{userId}")
  public WebResponse integrateTumblr(@PathVariable("userId") int userId)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    return this.socialService.integrateTumblr(userId);
  }

  @GetMapping(value = "/tumblr/authorize/{userId}", produces = MediaType.TEXT_HTML_VALUE, params ="oauth_token")
  @ResponseBody
  public String AuthorizeTumblr(@PathVariable("userId") int userId, @RequestParam(name = "oauth_token") String oauthToken,
      @RequestParam (name = "oauth_verifier") String oauthVerifier)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    return this.socialService.handleAuthorizeCallback(ApplicationConstants.TUMBLR, userId, oauthToken, oauthVerifier);
  }

  @GetMapping(value = "/tumblr/authorize/{userId}", produces = MediaType.TEXT_HTML_VALUE)
  @ResponseBody
  public String DonotAuthorizeTumblr(@PathVariable("userId") int userId)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    return String.format(ApplicationConstants.DONOT_AUTHORIZE_HTML_RESPONSE, ApplicationConstants.TUMBLR, ApplicationConstants.FAILURE_RESPONSE);
  }

  /** Twitter APIs  **/
  @GetMapping("/twitter/initialize/{userId}")
  public WebResponse integrateTwitter(@PathVariable("userId") int userId) throws
      OAuthCommunicationException,
      OAuthExpectationFailedException,
      OAuthNotAuthorizedException,
      OAuthMessageSignerException
  {
    return this.socialService.integrateTwitter(userId);
  }

  @GetMapping(value = "/twitter/authorize", produces = MediaType.TEXT_HTML_VALUE, params = "oauth_token")
  @ResponseBody
  public String AuthorizeTwitter(@RequestParam(name = "userId") int userId, @RequestParam(name = "oauth_token") String oauthToken,
      @RequestParam (name = "oauth_verifier") String oauthVerifier)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    return this.socialService.handleAuthorizeCallback(ApplicationConstants.TWITTER, userId, oauthToken, oauthVerifier);
  }

  @GetMapping(value = "/twitter/authorize", produces = MediaType.TEXT_HTML_VALUE, params = "denied")
  @ResponseBody
  public String DonotAuthorizeTwitter(@RequestParam("userId") int userId, @RequestParam("denied") String deniedId)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    return String.format(ApplicationConstants.DONOT_AUTHORIZE_HTML_RESPONSE, ApplicationConstants.TWITTER, ApplicationConstants.FAILURE_RESPONSE);
  }

  /** Facebook APIs **/
  @PostMapping("/facebook/integrate")
  public WebResponse IntegrateFcebook(@RequestBody FbShortLivedUAT sluat)
      throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException
  {
    return this.socialService.integrateFacebook(sluat);
  }
}

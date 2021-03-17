package com.somedman.backend.controller;

import com.somedman.backend.entities.Blog;
import com.somedman.backend.entities.FbShortLivedUAT;
import com.somedman.backend.entities.ResponseObject;
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
import java.util.*;

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
  public ResponseObject integrateTumblr(@PathVariable("userId") int userId)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    return this.socialService.integrateTumblr(userId);
  }

  @GetMapping(value = "/tumblr/authorize/{userId}", produces = MediaType.TEXT_HTML_VALUE)
  @ResponseBody
  public String AuthorizeTumblr(@PathVariable("userId") int userId, @RequestParam(name = "oauth_token") String oauthToken,
      @RequestParam (name = "oauth_verifier") String oauthVerifier)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    this.socialService.handleAuthorizeCallback(ApplicationConstants.TUMBLR, userId, oauthToken, oauthVerifier);
    return "<html>\n" +
              "<head>\n" +
              "    <script>\n" +
              "        window.close();\n" +
              "    </script>\n" +
              "</head>\n" +
              "<body>\n" +
              "</body>\n" +
           "</html>";
  }

//  @GetMapping("/tumblr/BlogsList/{userId}")
//  public ResponseObject GetAllBlogsByUserId(@PathVariable("userId") int userId)
//      throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException
//  {
//    return this.socialService.getTumblrBlogsByUserId(userId);
//  }

  /** Twitter APIs  **/
  @GetMapping("/twitter/initialize/{userId}")
  public ResponseObject integrateTwitter(@PathVariable("userId") int userId) throws
      OAuthCommunicationException,
      OAuthExpectationFailedException,
      OAuthNotAuthorizedException,
      OAuthMessageSignerException
  {
    return this.socialService.integrateTwitter(userId);
  }

  @GetMapping("/twitter/authorize")
  public void AuthorizeTwitter(@RequestParam(name = "userId") int userId, @RequestParam(name = "oauth_token") String oauthToken,
      @RequestParam (name = "oauth_verifier") String oauthVerifier)
      throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException
  {
    this.socialService.handleAuthorizeCallback(ApplicationConstants.TWITTER, userId, oauthToken, oauthVerifier);
  }

  /** Facebook APIs **/
  @PostMapping("/facebook/integrate")
  public void IntegrateFcebook(@RequestBody FbShortLivedUAT sluat)
      throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException
  {
    this.socialService.integrateFacebook(sluat);
  }
}

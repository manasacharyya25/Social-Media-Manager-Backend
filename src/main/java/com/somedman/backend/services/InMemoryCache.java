package com.somedman.backend.services;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.OAuthProviderListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class InMemoryCache
{
  public static HashMap<String, OAuthConsumer> oauthTokenToConsumerMap;

  public static HashMap<String, OAuthProvider> oauthTokenToProviderMap;

  InMemoryCache() {
    oauthTokenToConsumerMap  = new HashMap<String, OAuthConsumer>();
    oauthTokenToProviderMap  = new HashMap<String, OAuthProvider>();
  }
}

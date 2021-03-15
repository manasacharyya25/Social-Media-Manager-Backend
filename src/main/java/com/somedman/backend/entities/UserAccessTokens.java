package com.somedman.backend.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user_access_tokens")
@Data
public class UserAccessTokens
{
  @Id
  private int userId;
  private String facebookAccessToken;
  private String facebookPageId;
  private String instagramAccessToken;
  private String tumblrAccessToken;
  private String tumblrAccessTokenSecret;
  private String tumblrPageId;
  private String twitterAccessToken;
  private String twitterAccessTokenSecret;
  private String linkedinAccessToken;
  private String linkedinAccessTokenSecret;
  private String redditAccessToken;
  private String redditAccessTokenSecret;
}

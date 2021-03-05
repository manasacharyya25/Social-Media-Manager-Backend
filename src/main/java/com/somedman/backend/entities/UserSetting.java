package com.somedman.backend.entities;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user_settings")
@Data
public class UserSetting
{
  @Id
  private int userId;
  private boolean facebookIntegrated;
  private boolean instagramIntegrated;
  private boolean tumblrIntegrated;
  private String tumblrBlogUuid;
  private boolean twitterIntegrated;
  private boolean redditIntegrated;
  private boolean linkedinIntegrated;
}

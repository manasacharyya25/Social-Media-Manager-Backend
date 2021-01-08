package com.somedman.backend.entities;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user_settings")
@Data
public class UserSettings
{
  @Id
  private int userId;
  @Getter
  private boolean facebookIntegrated;
  private boolean instagramIntegrated;
  private boolean tumblrIntegrated;
  private boolean twitterIntegrated;
  private boolean redditIntegrated;
  private boolean linkedinIntegrated;

}

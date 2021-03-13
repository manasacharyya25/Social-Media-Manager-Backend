package com.somedman.backend.services;

import com.somedman.backend.entities.User;
import com.somedman.backend.entities.UserAccessTokens;
import com.somedman.backend.entities.UserSetting;
import com.somedman.backend.repository.UserAccessTokenRepository;
import com.somedman.backend.repository.UserRepository;
import com.somedman.backend.repository.UserSettingsRepository;
import com.somedman.backend.utills.ApplicationConstants;
import com.somedman.backend.utills.CustomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService
{
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserSettingsRepository userSettingsRepository;

  @Autowired
  private UserAccessTokenRepository uatRepository;

  public int loginUser(User newUser) {
    int userId = CustomUtils.getHashId(newUser.getEmail(), newUser.getProvider());

    if(!this.userRepository.findById(userId).isPresent()) {
      //1. User Table
      newUser.setId(userId);
      this.userRepository.save(newUser);

      //2. User Settings Table
      UserSetting userSettings = new UserSetting();
      userSettings.setUserId(userId);
      this.userSettingsRepository.save(userSettings);

      //3. User Access Tokens Table
      UserAccessTokens uat = new UserAccessTokens();
      uat.setUserId(userId);
      if(newUser.getProvider() == ApplicationConstants.FACEBOOK) {
        //TODO: Facebook Long Lived Page Access Token
        //uat.setFacebookAccessToken(SocialService.GetFacebookLongLivedAccessToken(newUser.getAccessToken()));
      }
      this.uatRepository.save(uat);
    }
    return userId;
  }

  public Optional<UserSetting> getUserSettings(int userId)
  {
    return this.userSettingsRepository.findById(userId);
  }

  public UserSetting saveUserSettings(UserSetting userSettings)
  {
    UserSetting updateUserSettings = this.userSettingsRepository.save(userSettings);
    return updateUserSettings;
  }
}

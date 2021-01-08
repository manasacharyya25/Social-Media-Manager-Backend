package com.somedman.backend.services;

import com.somedman.backend.entities.User;
import com.somedman.backend.entities.UserSettings;
import com.somedman.backend.repository.UserRepository;
import com.somedman.backend.repository.UserSettingsRepository;
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

  public int loginUser(User newUser) {
    int userId = CustomUtils.getHashId(newUser.getEmail());


    if(!this.userRepository.findById(userId).isPresent()) {
      newUser.setId(userId);
      this.userRepository.save(newUser);

      UserSettings userSettings = new UserSettings();
      userSettings.setUserId(userId);
      this.userSettingsRepository.save(userSettings);
    }
    return userId;
  }

  public Optional<UserSettings> getUserSettings(int userId)
  {
    return this.userSettingsRepository.findById(userId);
  }

  public UserSettings saveUserSettings(UserSettings userSettings)
  {
    UserSettings updateUserSettings = this.userSettingsRepository.save(userSettings);
    return updateUserSettings;
  }
}

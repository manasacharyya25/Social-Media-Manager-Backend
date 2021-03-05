package com.somedman.backend.repository;

import com.somedman.backend.entities.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface UserSettingsRepository extends JpaRepository<UserSetting, Integer>
{
  @Query(value = "Select * from user_settings us where us.user_id= ?1", nativeQuery = true)
  UserSetting findByUserId(int userId);
}

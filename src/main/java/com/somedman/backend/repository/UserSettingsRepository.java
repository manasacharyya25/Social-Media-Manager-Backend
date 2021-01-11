package com.somedman.backend.repository;

import com.somedman.backend.entities.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSetting, Integer>
{
}

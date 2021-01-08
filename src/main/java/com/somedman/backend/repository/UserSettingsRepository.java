package com.somedman.backend.repository;

import com.somedman.backend.entities.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Integer>
{
}

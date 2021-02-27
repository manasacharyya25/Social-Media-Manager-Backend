package com.somedman.backend.repository;

import com.somedman.backend.entities.UserAccessTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface UserAccessTokenRepository extends JpaRepository<UserAccessTokens, Integer>
{
  @Query(value = "Select * from user_access_tokens uat where uat.user_id= ?1", nativeQuery = true)
  UserAccessTokens findByUserId(int userId);
}

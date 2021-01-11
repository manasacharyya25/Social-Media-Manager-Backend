package com.somedman.backend.repository;

import com.somedman.backend.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostsRepository extends JpaRepository<Post, Integer>
{
  @Query(value = "Select * from posts p where p.user_id= ?1", nativeQuery = true)
  List<Post> findByUserId(int userId);
}

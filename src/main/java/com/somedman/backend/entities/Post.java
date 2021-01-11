package com.somedman.backend.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name="posts")
@Data
public class Post
{
  @Id
  @GeneratedValue
  private int postId;
  @Transient
  private String image;
  private String caption;
  private String content;
  private String tags;
  private byte[] imageData;
  private int userId;
}

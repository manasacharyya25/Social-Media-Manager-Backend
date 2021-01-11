package com.somedman.backend.controller;

import com.somedman.backend.entities.Post;
import com.somedman.backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("posts")
public class PostController
{
  @Autowired
  private PostService postsService;

  @PostMapping("/publish")
  @CrossOrigin(origins = "*")
  public int PublishPost(@RequestBody Post newPost) throws IOException
  {
    return this.postsService.publishPosts(newPost);
  }

  @GetMapping("/{userId}")
  @CrossOrigin(origins="*")
  public List<Post> GetPostsByUser(@PathVariable int userId) throws IOException
  {
    return this.postsService.getPostsByUser(userId);
  }
}


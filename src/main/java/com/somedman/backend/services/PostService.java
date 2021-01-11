package com.somedman.backend.services;

import com.somedman.backend.entities.Post;
import com.somedman.backend.repository.PostsRepository;
import com.somedman.backend.utills.CustomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PostService
{
  @Autowired
  private PostsRepository postsRepository;

  public int publishPosts(Post post) throws IOException
  {
    post.setImageData(CustomUtils.compressString(post.getImage()));
    Post savedPost = this.postsRepository.save(post);

    return savedPost.getPostId();
  }

  public List<Post> getPostsByUser(int userId) throws IOException
  {
  List<Post> retrievedPosts = this.postsRepository.findByUserId(userId);
    for (Post post : retrievedPosts)
    {
      post.setImage(CustomUtils.uncompressString(post.getImageData()));
    }
    return retrievedPosts;
  }
}

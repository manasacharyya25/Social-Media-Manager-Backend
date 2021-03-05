package com.somedman.backend.entities;

import lombok.Builder;
import lombok.Data;

@Builder
public class Blog
{
  public String BlogId;
  public String BlogName;
  public String BlogImageUrl;
  public String BlogUrl;
}

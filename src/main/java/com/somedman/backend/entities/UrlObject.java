package com.somedman.backend.entities;

import lombok.Data;

@Data
public class UrlObject
{
  public String url;

  public UrlObject(String url)
  {
    this.url = url;
  }
}

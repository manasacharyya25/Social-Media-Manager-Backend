package com.somedman.backend.entities;

import lombok.Data;

@Data
public class ResponseObject
{
  public Object response;

  public ResponseObject(Object response)
  {
    this.response = response;
  }
}

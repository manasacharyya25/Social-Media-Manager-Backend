package com.somedman.backend.entities;

import lombok.Builder;

@Builder
public class WebResponse
{
  public String responseCode;
  public String responseMessage;
  public String responseDetails;
}

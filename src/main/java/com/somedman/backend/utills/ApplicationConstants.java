package com.somedman.backend.utills;

public class ApplicationConstants
{
  // TODO:  Pull from app_properties file
  public static String HOST_URL = "http://localhost:8080";
  public static String TWITTER = "TWITTER";
  public static String TUMBLR = "TUMBLR";
  public static String FACEBOOK = "FACEBOOK";
  public static String AUTHORIZE_HTML_RESPONSE = "<html>\n" +
                                                    "<head>\n" +
                                                      "    <script>\n" +
                                                      "        window.opener.postMessage(\"%s Integration %s\",\"*\");\n" +
                                                      "        window.close();\n" +
                                                      "    </script>\n" +
                                                    "</head>\n" +
                                                    "<body>\n" +
                                                    "</body>\n" +
                                                  "</html>";
  public static String DONOT_AUTHORIZE_HTML_RESPONSE = "<html>\n" +
                                                        "<head>\n" +
                                                        "    <script>\n" +
                                                        "        window.opener.postMessage(\"%s Integration %s\",\"*\");\n" +
                                                        "        window.close();\n" +
                                                        "    </script>\n" +
                                                        "</head>\n" +
                                                        "<body>\n" +
                                                        "</body>\n" +
                                                        "</html>";
  public static String SUCCESS_RESPONSE = "SUCCESS";
  public static String FAILURE_RESPONSE = "FAILURE";
  public static String CALLBACK_URL = "CALLBACK_URL";
  public static String POST_REQUEST = "POST_REQUEST";
}

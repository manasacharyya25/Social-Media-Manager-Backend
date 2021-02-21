package com.somedman.backend.utills;

import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CustomUtils
{
  public static int getHashId(String text1, String text2)
  {
    int hashCode = 0;
    String text = text1.concat(text2);

    for( int i = 0; i < text.length(); i++) {
      hashCode += Character.getNumericValue(text.charAt(i))*(i+1);
    }
    return hashCode;
  }

  public static byte[] compressString(String srcTxt)
      throws IOException
  {
    ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
    GZIPOutputStream zos = new GZIPOutputStream(rstBao);
    zos.write(srcTxt.getBytes());
    IOUtils.closeQuietly(zos);

    return rstBao.toByteArray();
  }

  public static String uncompressString(byte[] zippedByteArray)
      throws IOException {
    final int BUFFER_SIZE = 32;
    ByteArrayInputStream is = new ByteArrayInputStream(zippedByteArray);
    GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
    StringBuilder string = new StringBuilder();
    byte[] data = new byte[BUFFER_SIZE];
    int bytesRead;
    while ((bytesRead = gis.read(data)) != -1) {
      string.append(new String(data, 0, bytesRead));
    }
    gis.close();
    is.close();
    return string.toString();
  }
}

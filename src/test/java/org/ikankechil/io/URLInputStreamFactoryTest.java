/**
 * URLInputStreamFactoryTest.java  v0.4  7 April 2015 10:06:58 AM
 *
 * Copyright © 2015-2018 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.junit.Test;

/**
 * JUnit test for <code>URLInputStreamFactory</code>.
 * <p>
 *
 * @author Daniel Kuan
 * @version 0.4
 */
public class URLInputStreamFactoryTest {

  private static boolean         isTransparentEncoding;

  private static final String    URL             = "http://httpbin.org/";
  private static final String    GZIP_URL        = "http://httpbin.org/gzip";
  private static final String    DEFLATE_URL     = "http://httpbin.org/deflate";
  private static final String    BAD_URL         = "http://bad";

  private static final String    OK_URL_FACTORY  = "com.squareup.okhttp.OkUrlFactory";
  private static final String    OK_URL_FACTORY3 = "okhttp3.OkUrlFactory";

  private static final String    COOKIE          = "Cookie";

  static {
    try {
      final Field field = URLInputStreamFactory.class.getDeclaredField("isTransparentEncoding");
      field.setAccessible(true);
      isTransparentEncoding = field.getBoolean(URLInputStreamFactory.class);
    }
    catch (NoSuchFieldException |
           SecurityException |
           IllegalArgumentException |
           IllegalAccessException e) {
      isTransparentEncoding = false;
    }
  }

  @Test(expected=NullPointerException.class)
  public void cannotCreateInputStreamWithNullURL() throws IOException {
    URLInputStreamFactory.newInputStream(null);
  }

  @Test
  public void newGzipEncodedInputStream() throws IOException {
    try (final InputStream actual = URLInputStreamFactory.newInputStream(new URL(GZIP_URL))) {
      if (isTransparentEncoding) {
        assertFalse(actual instanceof GZIPInputStream);
      }
      else {
        assertTrue(actual instanceof GZIPInputStream);
      }
      assertNotNull(actual);
    }
  }

  @Test
  public void newDeflateEncodedInputStream() throws IOException {
    try (final InputStream actual = URLInputStreamFactory.newInputStream(new URL(DEFLATE_URL))) {
      assertTrue(actual instanceof InflaterInputStream);
    }
  }

  @Test
  public void newUnencodedInputStream() throws IOException {
    try (final InputStream actual = URLInputStreamFactory.newInputStream(new URL(URL))) {
      assertFalse(actual instanceof GZIPInputStream);
      assertFalse(actual instanceof InflaterInputStream);
      assertNotNull(actual);
    }
  }

  @Test
  public void transparentEncodingWhenUsingOkHttp() {
    try {
      Class.forName(OK_URL_FACTORY3);
      assertTrue(isTransparentEncoding);
    }
    catch (final ClassNotFoundException cnfE) {
      try {
        Class.forName(OK_URL_FACTORY);
        assertTrue(isTransparentEncoding);
      }
      catch (final ClassNotFoundException cnfE2) {
        assertFalse(isTransparentEncoding);
      }
    }
  }

  @Test(expected=IOException.class)
  public void retryOnceOnFailure() throws IOException {
    URLInputStreamFactory.newInputStream(new URL(BAD_URL));

//    final URL url = mock(URL.class);
//    when(url.openConnection()).thenThrow(IOException.class)
//                              .thenReturn(new URL(URL).openConnection());
//    try (final InputStream actual = URLInputStreamFactory.newInputStream(url)) {
//      assertNotNull(actual);
//    }
  }

  @Test
  public void setCookie() throws IOException {
    try (final InputStream actual = URLInputStreamFactory.newInputStream(new URL(URL), COOKIE)) {
      assertNotNull(actual);
    }
  }

}

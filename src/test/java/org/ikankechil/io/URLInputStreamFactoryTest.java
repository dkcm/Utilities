/**
 * URLInputStreamFactoryTest.java  v0.3  7 April 2015 10:06:58 AM
 *
 * Copyright © 2015-2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for <code>URLInputStreamFactory</code>.
 * <p>
 *
 * @author Daniel Kuan
 * @version 0.3
 */
public class URLInputStreamFactoryTest {

  private static boolean         isTransparentEncoding;

  private static final String    URL             = "http://httpbin.org/";
  private static final String    GZIP_URL        = "http://httpbin.org/gzip";
  private static final String    DEFLATE_URL     = "http://httpbin.org/deflate";

  private static final String    OK_URL_FACTORY  = "com.squareup.okhttp.OkUrlFactory";
  private static final String    OK_URL_FACTORY3 = "okhttp3.OkUrlFactory";

//  private static final String    COOKIE          = "Cookie";

  @Rule
  public final ExpectedException thrown          = ExpectedException.none();

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

  @Test
  public void cannotCreateInputStreamWithNullURL() throws IOException {
    thrown.expect(NullPointerException.class);
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

  @Ignore
  @Test
  public void nullCookiesNotSet() throws IOException {
    final URL url = new URL(URL);
    try (final InputStream actual = URLInputStreamFactory.newInputStream(url, null)) {
//      final String expected = url.openConnection().getHeaderField(COOKIE);
    }
  }

}

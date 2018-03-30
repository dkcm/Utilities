/**
 * URLInputStreamFactory.java  v0.5  4 November 2014 5:16:25 PM
 *
 * Copyright © 2014-2018 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An <code>InputStream</code> factory that reads from <code>URL</code>s.
 *
 *
 * @author Daniel Kuan
 * @version 0.5
 */
public final class URLInputStreamFactory {
  // TODO
  // 1. evaluate cronet, jersey
  // 2. support Brotli ("br"), SDCH ("sdch")?
  // 3. [COMPLETED] v0.4 support cookies

  private static boolean        isTransparentEncoding;

  private static final String[] OK_HTTP_CLIENTS      = { "okhttp3.OkHttpClient",
                                                         "com.squareup.okhttp.OkHttpClient" };
  private static final String[] OK_URL_FACTORIES     = { "okhttp3.OkUrlFactory",
                                                         "com.squareup.okhttp.OkUrlFactory" };

  private static final String   FACTORY              = "factory";
  private static final String   HEADERS              = "headers";

  // HTTP encoding schemes
  private static final String   GZIP                 = "gzip";
  private static final String   DEFLATE              = "deflate";
  private static final String   SCHEMES              = GZIP + ',' + DEFLATE;
  private static final String   CHUNKED              = "chunked";

  // HTTP headers
  private static final String   ACCEPT_ENCODING      = "Accept-Encoding";
  private static final String   CONTENT_ENCODING     = "Content-Encoding";
  private static final String   TRANSFER_ENCODING    = "Transfer-Encoding";
  private static final String   CONTENT_RANGE        = "Content-Range";
  private static final String   COOKIE               = "Cookie";

  private static final int      BUFFER_SIZE          = 65536;
  private static final String   TRANSPARENT_ENCODING = "Transparent Encoding: {}";
  private static final String   URL_CONNECTION       = "URLConnection {}: {}";

  private static final Logger   logger               = LoggerFactory.getLogger(URLInputStreamFactory.class);

  private URLInputStreamFactory() { /* disallow instantiation */ }

  static { // load OkHttp client if available and possible, else default to JRE
    for (int i = 0; i < OK_HTTP_CLIENTS.length; ++i) {
      final String okHttpClient = OK_HTTP_CLIENTS[i];
      final String okUrlFactory = OK_URL_FACTORIES[i];
      try {
        logger.info("Looking for: {} and {}", okHttpClient, okUrlFactory);
        final Class<?> okhc = Class.forName(okHttpClient);
        final Class<?> okuf = Class.forName(okUrlFactory);
        final Constructor<?> okufCtor = okuf.getConstructor(okhc);

        try {
          URL.setURLStreamHandlerFactory((URLStreamHandlerFactory) okufCtor.newInstance(okhc.newInstance()));

          isTransparentEncoding = true;
          logger.info("Using OkHttp");
          break;
        }
        catch (final Error e) {
          final Field field = URL.class.getDeclaredField(FACTORY);
          field.setAccessible(true);
          final String factory = field.get(null).getClass().getName();
          logger.error("URLStreamHandlerFactory already defined: {}", factory);
          break;
        }
      }
      catch (final ReflectiveOperationException | // super class of ClassNotFoundException, NoSuchMethodException, etc.
                   SecurityException |
                   IllegalArgumentException e) {
        logger.debug("{} and {} unavailable", okHttpClient, okUrlFactory);
        if (i == OK_HTTP_CLIENTS.length - 1) {
          logger.warn("OkHttp unavailable.  Defaulting to JRE");
        }
      }
    }
  }

  /**
   * Creates an input stream that reads from a <code>URL</code>.
   *
   * @param source the source <code>URL</code>
   * @return an input stream that reads from a <code>URL</code>
   * @throws IOException
   *           if an I/O error occurs while creating the input stream
   */
  public static final InputStream newInputStream(final URL source) throws IOException {
    return newInputStream(source, null);
  }

  @SuppressWarnings("resource")
  public static final InputStream newInputStream(final URL source, final String cookie) throws IOException {
    InputStream is;
    try {
      is = createInputStream(source, cookie);
    }
    catch (final IOException ioE) { // retry once on failure
      logger.warn("I/O error encountered, retrying URL: {}", source);
      is = createInputStream(source, cookie);
    }
    return is;
  }

  private static InputStream createInputStream(final URL source, final String cookie) throws IOException {
    final URLConnection connection = source.openConnection();
    if (!isTransparentEncoding) {
      // does not work well with OkHttp
      connection.setRequestProperty(ACCEPT_ENCODING, SCHEMES);
      connection.setRequestProperty(TRANSFER_ENCODING, CHUNKED);
    }
    logger.debug(TRANSPARENT_ENCODING, isTransparentEncoding);

    // set cookie
    if (cookie != null) {
      connection.setRequestProperty(COOKIE, cookie);
    }

    // decode by content encoding
    final String contentEncoding = connection.getHeaderField(CONTENT_ENCODING);
    logger.debug(URL_CONNECTION, CONTENT_ENCODING, contentEncoding);
    logger.debug(URL_CONNECTION, TRANSFER_ENCODING, connection.getHeaderField(TRANSFER_ENCODING));
    logger.debug(URL_CONNECTION, CONTENT_RANGE, connection.getHeaderField(CONTENT_RANGE));
    logger.trace(URL_CONNECTION, HEADERS, connection.getHeaderFields());

    return GZIP.equals(contentEncoding)    ? new GZIPInputStream(connection.getInputStream(), BUFFER_SIZE) :
           DEFLATE.equals(contentEncoding) ? new InflaterInputStream(connection.getInputStream(), new Inflater(), BUFFER_SIZE) :
                                             connection.getInputStream();
  }

}

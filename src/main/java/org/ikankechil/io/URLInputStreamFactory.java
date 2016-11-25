/**
 * URLInputStreamFactory.java  v0.2  4 November 2014 5:16:25 PM
 *
 * Copyright © 2014-2015 Daniel Kuan.  All rights reserved.
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
 * <p>
 *
 * @author Daniel Kuan
 * @version 0.2
 */
public final class URLInputStreamFactory {

  private static boolean      isTransparentEncoding;

  private static final String OK_HTTP_CLIENT    = "com.squareup.okhttp.OkHttpClient";
  private static final String OK_URL_FACTORY    = "com.squareup.okhttp.OkUrlFactory";

  private static final String FACTORY           = "factory";
  private static final String HEADERS           = "headers";

  // HTTP-related
  private static final String GZIP              = "gzip";
  private static final String DEFLATE           = "deflate";
  private static final String SCHEMES           = GZIP + ',' + DEFLATE;
  private static final String CHUNKED           = "chunked";
  private static final String ACCEPT_ENCODING   = "Accept-Encoding";
  private static final String CONTENT_ENCODING  = "Content-Encoding";
  private static final String TRANSFER_ENCODING = "Transfer-Encoding";
  private static final String CONTENT_RANGE     = "Content-Range";

  private static final int    BUFFER_SIZE       = 65536;
  private static final String URL_CONNECTION    = "URLConnection {}: {}";

  private static final Logger logger            = LoggerFactory.getLogger(URLInputStreamFactory.class);

  private URLInputStreamFactory() { /* disallow instantiation */ }

  static { // load OkHttp client if available and possible, else default to JRE
    try {
      logger.info("Looking for: {} and {}", OK_HTTP_CLIENT, OK_URL_FACTORY);
      final Class<?> okhc = Class.forName(OK_HTTP_CLIENT);
      final Class<?> okuf = Class.forName(OK_URL_FACTORY);
      final Constructor<?> okufCtor = okuf.getConstructor(okhc);

      try {
        URL.setURLStreamHandlerFactory((URLStreamHandlerFactory) okufCtor.newInstance(okhc.newInstance()));

        isTransparentEncoding = true;
        logger.info("Using OkHttp");
      }
      catch (final Error e) {
        final Field field = URL.class.getDeclaredField(FACTORY);
        field.setAccessible(true);
        final String factory = field.get(null).getClass().getName();
        logger.error("URLStreamHandlerFactory already defined: {}", factory);
      }
    }
    catch (final ReflectiveOperationException |
                 SecurityException |
                 IllegalArgumentException e) {
      logger.warn("OkHttp unavailable: {}", e.getMessage(), e);
      logger.info("Defaulting to JRE");
    }
  }

  /**
   * Creates an input stream that reads from <code>source</code>.
   *
   * @param source
   * @return an input stream that reads from <code>source</code>
   * @throws IOException
   *           if an I/O error occurs while creating the input stream
   */
  public static final InputStream newInputStream(final URL source) throws IOException {
    final URLConnection connection = source.openConnection();
    if (!isTransparentEncoding) {
      // does not work well with OkHttp
      connection.setRequestProperty(ACCEPT_ENCODING, SCHEMES);
      connection.setRequestProperty(TRANSFER_ENCODING, CHUNKED);
    }
    logger.debug("Transparent Encoding: {}", isTransparentEncoding);

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

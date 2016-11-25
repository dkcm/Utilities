/**
 * TextReader.java  v0.3  14 December 2013 11:28:38 PM
 *
 * Copyright © 2013-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads plain text (decoded) from an <code>URL</code>, a <code>File</code> or
 * an <code>InputStream</code>.
 *
 * @author Daniel Kuan
 * @version 0.3
 */
public class TextReader { // TODO v0.5 read n lines only

  private static final String UTF_8  = "UTF-8";

  // default sizes
  private static final int    LINES  = 1024;

  private static final Logger logger = LoggerFactory.getLogger(TextReader.class);

  /**
   * Reads a series of lines from a URL as <code>String</code>s.
   *
   * @param source
   * @return a <code>List</code> of <code>String</code>s read from
   *         <code>source</code>
   * @throws IOException if an I/O error occurs
   */
  public List<String> read(final URL source) throws IOException {
    logger.info("Reading from URL: {}", source);

    final List<String> lines = read(URLInputStreamFactory.newInputStream(source));

    logger.info("URL read: {}", source);
    return lines;
  }

  static final URI toURI(final String url) throws UnsupportedEncodingException {
    URI uri;
    try {
      uri = new URI(url);
    }
    catch (final URISyntaxException urisE) {
      logger.warn("Invalid URI: {}", url, urisE);
      final StringBuilder urlBuilder = new StringBuilder(url);
      final int i = urisE.getIndex();
      urlBuilder.replace(i,
                         i + 1,
                         URLEncoder.encode(String.valueOf(urlBuilder.charAt(i)), UTF_8));
      uri = URI.create(urlBuilder.toString());
      logger.info("Corrected URI: {}", uri);
    }
    return uri;
  }

  /**
   * Reads a series of lines from a file as <code>String</code>s.
   *
   * @param source the <code>File</code> to read from
   * @return a <code>List</code> of <code>String</code>s read from the source
   * @throws FileNotFoundException if the file does not exist, is a directory
   *           rather than a regular file, or for some other reason cannot be
   *           opened for reading
   * @throws IOException if an I/O error occurs
   */
  public List<String> read(final File source) throws FileNotFoundException, IOException {
    logger.info("Reading from file: {}", source);

    final List<String> lines;
    try (final FileInputStream fis = new FileInputStream(source)) {
      lines = read(fis);
    }

    logger.info("File read: {}", source);
    return lines;
  }

  /**
   * Subclasses should override this method if they wish to provide a decoding
   * scheme.
   *
   * @param source
   * @return
   * @throws IOException
   */
  public List<String> read(final InputStream source) throws IOException {
    return read(new InputStreamReader(source));
  }

  private static final List<String> read(final InputStreamReader input) throws IOException {
    final List<String> lines = new ArrayList<>(LINES);

    // read all lines from input
    try (final BufferedReader reader = new BufferedReader(input)) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    }

    return lines;
  }

}

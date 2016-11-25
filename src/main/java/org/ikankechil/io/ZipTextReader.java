/**
 * ZipTextReader.java  v0.1  14 November 2014 1:41:05 PM
 *
 * Copyright © 2014-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads text encoded in the ZIP format.
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public class ZipTextReader extends TextReader {

  private static final Logger logger = LoggerFactory.getLogger(ZipTextReader.class);

  @Override
  public List<String> read(final InputStream source) throws IOException {
    final ZipInputStream zip = new ZipInputStream(source);
    // assume only one Zip entry
    logger.info("ZIP file entry: {}", zip.getNextEntry().getName());
    return super.read(zip);
  }

}

/**
 * TextWriter.java  v0.2  17 December 2013 10:34:04 PM
 *
 * Copyright © 2013-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type description goes here.
 *
 * @author Daniel Kuan
 * @version 0.2
 */
public class TextWriter {

  private static final Logger logger = LoggerFactory.getLogger(TextWriter.class);

  /**
   * Writes a series of lines to a file as <code>String</code>s.
   *
   * @param lines the <code>List</code> of <code>String</code>s to write
   * @param destination the destination <code>File</code>
   * @throws IOException if an I/O error occurs
   */
  public void write(final Collection<? extends String> lines, final File destination)
      throws IOException {
    logger.info("Writing file: {}", destination);

    try (FileOutputStream fos = new FileOutputStream(destination)) {
      write(lines, fos);
    }

    logger.info("File written: {}", destination);
  }

  /**
   * Subclasses should override this method if they wish to provide an encoding
   * scheme.
   *
   * @param lines
   * @param destination
   * @throws IOException
   */
  public void write(final Collection<? extends String> lines, final OutputStream destination)
      throws IOException {
    write(lines, new OutputStreamWriter(destination));
  }

  private static final void write(final Collection<? extends String> lines, final OutputStreamWriter output)
      throws IOException {
    // write all lines to output
    try (final BufferedWriter writer = new BufferedWriter(output)) {
      for (final String line : lines) {
        writer.write(line);
        writer.newLine();
      }
    }
    logger.debug("Lines written: {}", lines.size());
  }

}

/**
 * ZipTextReaderTest.java v0.3 7 April 2015 10:13:13 AM
 *
 * Copyright © 2015-2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for <code>ZipTextReader</code>.
 * <p>
 *
 * @author Daniel Kuan
 * @version 0.3
 */
public class ZipTextReaderTest extends TextReaderTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    sourceURL = new URL("http://www.fxhistoricaldata.com/download/EURUSD_day.zip");

    TextIOTest.setUpBeforeClass(ZipTextReaderTest.class);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    sourceURL = null;

    TextIOTest.tearDownAfterClass();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    reader = new ZipTextReader();
  }

  @Override
  @Test
  public void readFile() throws Exception {
    // prepare zipped input
    final File zippedSourceFile = new File(ZipTextReaderTest.class.getSimpleName());
    final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zippedSourceFile));
    try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zos))) {
      zos.putNextEntry(new ZipEntry(zippedSourceFile.getName()));
      for (final String line : EXPECTEDS) {
        writer.write(line);
        writer.newLine();
      }
    }

    try {
      actuals = reader.read(zippedSourceFile);
      compare();
    }
    finally {
      assertTrue(zippedSourceFile.delete());
    }
  }

  @Override
  @Test
  public void readInputStream() throws Exception {
    actuals = reader.read(sourceURL.openStream());
    compare();
  }

}

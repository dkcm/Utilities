/**
 * TextReaderTest.java  v0.2  1 January 2014 10:57:07 PM
 *
 * Copyright © 2014-2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit test for <code>TextReader</code>.
 *
 * @author Daniel Kuan
 * @version 0.2
 */
public class TextReaderTest extends TextIOTest {

  TextReader          reader;

  static URL          sourceURL;

  static final String BASE   = "http://ichart.finance.yahoo.com/table.csv?s=";
  static final String PERIOD = "&a=0&b=7&c=2013&d=0&e=11&f=2013&g=d";

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // 5 days' worth of price data from 7 - 11 Jan 2013 for Agilent on NYSE
    sourceURL = new URL(BASE + "A" + PERIOD);

    TextIOTest.setUpBeforeClass(TextReaderTest.class);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    sourceURL = null;

    TextIOTest.tearDownAfterClass();
  }

  @Before
  public void setUp() throws Exception {
    reader = new TextReader();
  }

  @Test
  public void cannotReadNullSources() throws Exception {
    final URL url = null;
    thrown.expect(NullPointerException.class);
    reader.read(url);

    final File file = null;
    thrown.expect(NullPointerException.class);
    reader.read(file);
  }

  @Test
  public void readURL() throws Exception {
    // read from URL
    actuals = reader.read(sourceURL);

    // compare actual results with expected results line-by-line
    compare();
  }

  @Test
  public void stringToURI() throws Exception {
    final String expected = sourceURL.toString();
    final String actual = TextReader.toURI(expected).toString();

    assertEquals(expected, actual);
  }

  @Test
  public void correctInvalidURI() throws Exception {
    final String gspc = "^GSPC" + PERIOD;
    final String expected = BASE + gspc.replace("^", "%5E");
    final String actual = TextReader.toURI(BASE + gspc).toString();

    assertEquals(expected, actual);
  }

  @Test
  public void readFile() throws Exception {
    actuals = reader.read(SOURCE_FILE);
    compare();
  }

  @Test
  public void readInputStream() throws Exception {
    try (FileInputStream fis = new FileInputStream(SOURCE_FILE)) {
      actuals = reader.read(fis);
      compare();
    }
  }

  @Ignore@Test
  public void readFileSpeed() throws Exception {
    for (int i = 0; i < 1024; ++i) {
      reader.read(new File("Test.log"));
    }
  }

  @Ignore@Test
  public void readURLSpeed() throws Exception {
    final TextReader r = new TextReader();
    long start = System.currentTimeMillis();
    for (int i = 0; i < 5; ++i) {
      r.read(new URL(BASE + "IBM"));
      final long end = System.currentTimeMillis();
      System.out.println(end - start + "ms");
      start = end;
    }
  }

}

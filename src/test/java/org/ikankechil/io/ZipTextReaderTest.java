/**
 * ZipTextReaderTest.java v0.1 7 April 2015 10:13:13 AM
 *
 * Copyright © 2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import java.net.URL;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for <code>ZipTextReader</code>.
 * <p>
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public class ZipTextReaderTest extends TextReaderTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    sourceURL = new URL("http://www.fxhistoricaldata.com/download/EURUSD?t=day");

    TextIOTest.setUpBeforeClass();
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
  public void readInputStream() throws Exception {
    actuals = reader.read(sourceURL.openStream());
    compare();
  }

}

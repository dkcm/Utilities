/**
 * TextWriterTest.java	v0.1	3 January 2014 1:12:20 AM
 *
 * Copyright © 2014-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for <code>TextWriter</code>.
 *
 * @author Daniel Kuan
 * @version
 */
public class TextWriterTest extends TextIOTest {

  private final TextWriter writer = new TextWriter();

  private File             destination;

  @Before
  public void setUp() throws Exception {
    String fileName = SOURCE_FILE.getName();
    // insert "_actual" before file suffix
    fileName = new StringBuilder(fileName).insert(fileName.length() - 4, "_actual")
                                          .toString();
    destination = new File(SOURCE_FILE.getParent(), fileName);
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    if (destination != null) {
      destination.delete();
      destination = null;
    }
  }

  @Test
  public void writeFile() throws Exception {
    writer.write(EXPECTEDS, destination);

    actuals = Files.readAllLines(destination.toPath(), Charset.defaultCharset());

    compare();
  }

}

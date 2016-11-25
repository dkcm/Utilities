/**
 * TextIOTest.java  v0.1  6 January 2014 2:00:35 AM
 *
 * Copyright © 2014-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class TextIOTest {

  @Rule
  public final ExpectedException thrown      = ExpectedException.none();

  static final File              SOURCE_FILE = new File(".//./src/test/resources/" + TextIOTest.class.getSimpleName(), "A_20130107-20130111.csv");

  static final List<String>      EXPECTEDS   = new ArrayList<>();
  List<String>                   actuals;

  static final char              SLASH       = '/';

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // read from file to reduce external dependencies
    EXPECTEDS.addAll(Files.readAllLines(SOURCE_FILE.toPath(), Charset.defaultCharset()));
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    EXPECTEDS.clear();
  }

  @After
  public void tearDown() throws Exception {
    if (actuals != null) {
      actuals.clear();
      actuals = null;
    }
  }

  final void compare() {
    assertNotNull(actuals);
    assertNotSame(EXPECTEDS, actuals);

    int i = 0;
    for (final String expected : EXPECTEDS) {
      final String actual = actuals.get(i++);

      assertNotNull(actual);
      assertEquals(expected, actual);
      assertNotSame(expected, actual);
    }

    assertEquals(EXPECTEDS.size(), actuals.size());
    assertTrue(EXPECTEDS.equals(actuals));
  }

}

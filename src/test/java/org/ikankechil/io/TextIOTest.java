/**
 * TextIOTest.java  v0.2  6 January 2014 2:00:35 AM
 *
 * Copyright © 2014-2016 Daniel Kuan.  All rights reserved.
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
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * Abstract superclass for all TextIO JUnit tests.
 *
 * @author Daniel Kuan
 * @version 0.2
 */
public abstract class TextIOTest {

  @Rule
  public final ExpectedException thrown    = ExpectedException.none();

  static File                    SOURCE_FILE;
  private static final String    DIRECTORY = ".//./src/test/resources/";

  static final List<String>      EXPECTEDS = new ArrayList<>();
  List<String>                   actuals;

  static final char              SLASH     = '/';
  private static final char      DOT       = '.';

  private static final String    CSV       = ".csv";

  public static void setUpBeforeClass(final Class<?> testClass) throws Exception {
//    final String testDataFile = DIRECTORY + testClass.getName().replace(DOT, SLASH) + CSV;
//    SOURCE_FILE = new File(TextIOTest.class.getClassLoader().getResource(testDataFile).toURI());
    final String testDataFile = testClass.getSimpleName() + CSV;
    SOURCE_FILE = new File(DIRECTORY, testDataFile);

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

/**
 * TextTransformerTest.java	v0.1	5 January 2014 11:32:15 PM
 *
 * Copyright © 2014-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit test for <code>TextTransformer</code>.
 *
 * @author Daniel Kuan
 * @version
 */
public class TextTransformerTest extends TextIOTest {

  private final TextTransformer          transformer = new TextTransformer(TRANSFORM);

  private static final TestTextTransform TRANSFORM   = new TestTextTransform();
  private static final Source            SOURCE      = new Source();

  static final Logger                    logger      = LoggerFactory.getLogger(TextTransformerTest.class);

  static final class Source {

    private static List<String> sourceLines;

    void readLines(final File file) throws IOException {
      sourceLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    }

    List<String> newLines() {
      final ArrayList<String> newLines = new ArrayList<>(sourceLines.size());
      for (final String sourceLine : sourceLines) {
        newLines.add(new String(sourceLine));
      }
      return newLines;
    }

  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    SOURCE.readLines(SOURCE_FILE);
    final List<String> sourceLines = SOURCE.newLines();

    for (final String line : sourceLines) {
      EXPECTEDS.add(TRANSFORM.transform(line));
    }
  }

  @Test
  public void cannotInstantiateWithNullTransform() {
    thrown.expect(NullPointerException.class);
    @SuppressWarnings("unused")
    final TextTransformer nullTransformer = new TextTransformer(null);
  }

  @Test
  public void cannotTransformNullSource() throws Exception {
    assertNotNull(transformer);
    thrown.expect(NullPointerException.class);
    transformer.transform(null);
  }

  @Test
  public void emptySource() throws Exception {
    final List<String> emptySource = new ArrayList<>();
    actuals = transformer.transform(emptySource);

    assertNotNull(actuals);
    assertSame(emptySource, actuals); // transformation is in-place
    assertEquals(emptySource.size(), actuals.size());
    assertTrue(emptySource.isEmpty());
    assertTrue(actuals.isEmpty());

    assertTrue(emptySource.equals(actuals));
  }

  @Test
  public void transform() throws Exception {
    final List<String> sourceLines = SOURCE.newLines();
    actuals = transformer.transform(sourceLines);
    compare();
  }

  @Test
  public void transformReverseOrder() throws Exception {
    Collections.reverse(EXPECTEDS);
    final TextTransformer rot = new TextTransformer(TRANSFORM, true);
    final List<String> sourceLines = SOURCE.newLines();
    try {
      actuals = rot.transform(sourceLines);
      compare();
    }
    finally {
      Collections.reverse(EXPECTEDS);
    }
  }

  public static class TestTextTransform implements TextTransform {

    @Override
    public String transform(final String line) {
      // Yahoo! Finance CSV format
      // YYYY-MM-DD, Open, High, Low, Close, Volume, Adj Close
      logger.debug(line);
      final StringBuilder builder = new StringBuilder(line);

      final char[] ddmm = new char[4];
      builder.getChars(8, 10, ddmm, 0);
      builder.getChars(5, 7, ddmm, 2);

      builder.delete(4, 10)
             .insert(0, ddmm, 0, 2)
             .insert(2, SLASH)
             .insert(3, ddmm, 2, 2)
             .insert(5, SLASH);
      // DD/MM/YYYY, Open, High, Low, Close, Volume, Adj Close
      final String result = builder.toString();
      logger.debug(result);

      return result;
    }

  }

}

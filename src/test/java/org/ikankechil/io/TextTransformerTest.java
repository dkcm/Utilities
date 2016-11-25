/**
 * TextTransformerTest.java  v0.3  5 January 2014 11:32:15 PM
 *
 * Copyright © 2014-2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import static org.junit.Assert.*;

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
 * @version 0.3
 */
public class TextTransformerTest extends TextIOTest {

  private final TextTransformer          transformer = new TextTransformer(TRANSFORM);

  private static final TestTextTransform TRANSFORM   = new TestTextTransform();

  static final Logger                    logger      = LoggerFactory.getLogger(TextTransformerTest.class);

  static final class Source {

    private static List<String> sourceLines;

    static List<String> newLines() {
      final ArrayList<String> newLines = new ArrayList<>(sourceLines.size());
      for (final String sourceLine : sourceLines) {
        newLines.add(new String(sourceLine));
      }
      return newLines;
    }

    static void loadLines(final List<String> froms) {
      sourceLines = new ArrayList<>(froms);
    }

  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    TextIOTest.setUpBeforeClass(TextTransformerTest.class);
    Source.loadLines(EXPECTEDS);

    EXPECTEDS.clear();
    for (final String line : Source.newLines()) {
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
  public void negativeSkippedRowsImmaterial() throws Exception {
    final List<String> sourceLines = Source.newLines();
    actuals = new TextTransformer(TRANSFORM, -1, false).transform(sourceLines);
    compare();
    assertEquals(sourceLines, actuals);

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
    final List<String> sourceLines = Source.newLines();
    actuals = transformer.transform(sourceLines);
    compare();
    assertEquals(sourceLines, actuals);
  }

  @Test
  public void transformReverseOrder() throws Exception {
    Collections.reverse(EXPECTEDS);
    final List<String> sourceLines = Source.newLines();
    final TextTransformer rot = new TextTransformer(TRANSFORM, 0, true);
    try {
      actuals = rot.transform(sourceLines);
      compare();
      assertEquals(sourceLines, actuals);
    }
    finally {
      Collections.reverse(EXPECTEDS);
    }
  }

  @Test
  public void transformSkipRows() throws Exception {
    final int skippedRows = 3;
    final List<String> expectedSkippedRows = new ArrayList<>(skippedRows);
    for (int i = 0; i < skippedRows; ++i) {
      expectedSkippedRows.add(EXPECTEDS.remove(0));
    }

    final List<String> sourceLines = Source.newLines();
    final TextTransformer srt = new TextTransformer(TRANSFORM, skippedRows, false);
    try {
      actuals = srt.transform(sourceLines);
      compare();
      assertEquals(sourceLines, actuals);
    }
    finally {
      assertTrue(EXPECTEDS.addAll(0, expectedSkippedRows));
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

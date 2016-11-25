/**
 * StringUtilityTest.java  v0.1  1 January 2009 2:20:52 AM
 *
 * Copyright © 2009-2010 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.ikankechil.util.StringUtility.CharacterComparator;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for <code>StringUtility</code>.
 *
 * @author Daniel Kuan
 * @version
 */
public final class StringUtilityTest {

  @Rule
  public final ExpectedException thrown                = ExpectedException.none();

  private static final Random    rng                   = new Random();
  private static final char[]    UPPERCASE_CHARACTERS  = new char[26];

  private static final int       MAXIMUM_STRING_LENGTH = 30;
  private static final String    EMPTY                 = "";
  private static final String    SPACE                 = " ";
  private static final char      COMMA                 = ',';

  private static final int       NONE                  = -1;

  @BeforeClass
  public static void setUpBeforeClass() {
    // initialise upper case letters
    char c = 'A';
    UPPERCASE_CHARACTERS[0] = c;
    for (int i = 1; i < UPPERCASE_CHARACTERS.length; ++i) {
      UPPERCASE_CHARACTERS[i] = ++c;
    }
  }

  @Test
  public void toUpperAndLowerCase() {
    // first character not forcibly set to lower case
    final StringBuilder builder = new StringBuilder(newRandomUpperCase(false));
    builder.append(SPACE).append(newRandomUpperCase(false))
           .append(SPACE).append(newRandomUpperCase(false));
    String operand = builder.toString();

    final String expected = newUpperAndLowerCase(operand);
    String actual = StringUtility.toUpperAndLowerCase(operand);

    assertNotNull(expected);
    assertFalse(expected.isEmpty());
    assertNotNull(actual);
    assertFalse(actual.isEmpty());
    assertEquals(expected, actual);
    assertNotSame(expected, actual);

    // first character forcibly set to lower case
    builder.setCharAt(0, Character.toLowerCase(builder.charAt(0)));
    operand = builder.toString();
    actual = StringUtility.toUpperAndLowerCase(operand);

    assertNotNull(actual);
    assertFalse(actual.isEmpty());
    assertEquals(expected, actual);
    assertNotSame(expected, actual);
  }

  // TODO newUpperAndLowerCase == toTitleCase?
  private static final String newUpperAndLowerCase(final String operand) {
    final char[] upperAndLowerCase = operand.toLowerCase().toCharArray();
    upperAndLowerCase[0] = Character.toUpperCase(upperAndLowerCase[0]);
    for (int i = 1; i < (upperAndLowerCase.length - 1); ++i) {
      if (Character.isWhitespace(upperAndLowerCase[i])) {
        upperAndLowerCase[++i] = Character.toUpperCase(upperAndLowerCase[i]);
      }
    }
    return new String(upperAndLowerCase);
  }

  @Test
  public void containsLowerCase() {
    //
    boolean containsLowerCase = false;
    String random = newRandomUpperCase(containsLowerCase);

    assertNotNull(random);
    assertFalse(random.isEmpty());
    assertEquals(random, containsLowerCase, StringUtility.containsLowerCase(random));

    //
    containsLowerCase = true;
    random = newRandomUpperCase(containsLowerCase);

    assertNotNull(random);
    assertFalse(random.isEmpty());
    assertEquals(random, containsLowerCase, StringUtility.containsLowerCase(random));
  }

  @Test
  public void indexOfAlphabetic() {
    final StringBuilder builder = new StringBuilder(Integer.toString(rng.nextInt(Integer.MAX_VALUE)));
    final String suffix = newRandomUpperCase(true);
    final char alphabet = 'a';
    int indexOfAlphabetic;

    // lower case alphabet
    builder.insert(indexOfAlphabetic = rng.nextInt(builder.length()),
                   Character.toLowerCase(alphabet))
           .append(suffix);

    assertEquals(indexOfAlphabetic,
                 StringUtility.indexOfAlphabetic(builder.toString()));

    builder.deleteCharAt(indexOfAlphabetic)
           .delete(builder.lastIndexOf(suffix), builder.length());

    // upper case alphabet
    builder.insert(indexOfAlphabetic = rng.nextInt(builder.length()),
                   Character.toUpperCase(alphabet))
           .append(suffix);

    assertEquals(indexOfAlphabetic,
                 StringUtility.indexOfAlphabetic(builder.toString()));

    builder.deleteCharAt(indexOfAlphabetic)
           .delete(builder.lastIndexOf(suffix), builder.length());
  }

  @Test
  public void lastIndexOfAlphabetic() {
    final StringBuilder builder = new StringBuilder(Integer.toString(rng.nextInt(Integer.MAX_VALUE)));
    final String prefix = newRandomUpperCase(true);
    final char alphabet = 'a';
    int lastIndexOfAlphabetic;

    // lower case alphabet
    builder.insert(lastIndexOfAlphabetic = rng.nextInt(builder.length()),
                   Character.toLowerCase(alphabet))
           .insert(0, prefix);

    assertEquals(lastIndexOfAlphabetic += prefix.length(),
                 StringUtility.lastIndexOfAlphabetic(builder.toString()));

    builder.deleteCharAt(lastIndexOfAlphabetic)
           .delete(0, prefix.length());

    // upper case alphabet
    builder.insert(lastIndexOfAlphabetic = rng.nextInt(builder.length()),
                   Character.toUpperCase(alphabet))
           .insert(0, prefix);

    assertEquals(lastIndexOfAlphabetic += prefix.length(),
                 StringUtility.lastIndexOfAlphabetic(builder.toString()));

    builder.deleteCharAt(lastIndexOfAlphabetic)
           .delete(0, prefix.length());
  }

  @Test
  public void indexOfDigit() {
    final StringBuilder builder = new StringBuilder(newRandomUpperCase(true));
    final int indexOfDigit = rng.nextInt(builder.length());
    builder.insert(indexOfDigit, rng.nextInt(Integer.MAX_VALUE));

    // at least one digit present
    assertEquals(indexOfDigit,
                 StringUtility.indexOfDigit(builder.toString()));

    // digits absent
    assertEquals(-1,
                 StringUtility.indexOfDigit(newRandomUpperCase(true)));
  }

  @Test
  public void lastIndexOfDigit() {
    final StringBuilder builder = new StringBuilder(newRandomUpperCase(true));
    int lastIndexOfDigit = rng.nextInt(builder.length());
    final int insertedInteger = rng.nextInt(Integer.MAX_VALUE);
    builder.insert(lastIndexOfDigit, insertedInteger);

    // at least one digit present
    lastIndexOfDigit += Integer.toString(insertedInteger).length() - 1;

    assertEquals(lastIndexOfDigit,
                 StringUtility.lastIndexOfDigit(builder.toString()));

    // digits absent
    assertEquals(-1,
                 StringUtility.indexOfDigit(newRandomUpperCase(true)));
  }

  @Test
  public void indexOfUpperCase() {
    final String random = newRandomUpperCase(true);
    int indexOfUpperCase = 0;
    for (final char letter : random.toCharArray()) {
      if (Character.isUpperCase(letter)) {
        break;
      }
      ++indexOfUpperCase;
    }

    // mix of upper and lower case
    final String lowerCase = newRandomUpperCase(false).toLowerCase();

    assertEquals(lowerCase + random,
                 indexOfUpperCase + lowerCase.length(),
                 StringUtility.indexOfUpperCase(lowerCase + random));

    // solely lower case
    assertEquals(lowerCase,
                 -1,
                 StringUtility.indexOfUpperCase(lowerCase));
  }

  @Test
  public void lastIndexOfUpperCase() {
    String random = newRandomUpperCase(true);
    int lastIndexOfUpperCase = random.length() - 1;
    for (; lastIndexOfUpperCase >= 0; --lastIndexOfUpperCase) {
      if (Character.isUpperCase(random.charAt(lastIndexOfUpperCase))) {
        break;
      }
    }

    // mix of upper and lower case
    random += newRandomUpperCase(false).toLowerCase();

    assertEquals(lastIndexOfUpperCase,
                 StringUtility.lastIndexOfUpperCase(random));

    // solely lower case
    random = newRandomUpperCase(false).toLowerCase();

    assertEquals(-1,
                 StringUtility.lastIndexOfUpperCase(random));
  }

  @Test
  public void redundantSpaceRemoval() {
    // empty string
    String randomWithSpaces = insertRandomSpaces(SPACE);
    String expected = EMPTY;
    String actual = StringUtility.removeRedundantSpaces(randomWithSpaces);

    assertNotNull(actual);
    assertTrue(actual.isEmpty());
    assertNotSame(randomWithSpaces, actual);
    assertNotSame(expected, actual);
    assertEquals('\"' + randomWithSpaces + '\"', expected, actual);

    // arbitrary string (with leading and trailing spaces)
    randomWithSpaces = insertRandomSpaces(newRandomUpperCase(true));

    String[] expecteds = randomWithSpaces.split(SPACE);
    StringBuilder builder = new StringBuilder();
    for (final String e : expecteds) {
      if (e.length() > 0) {
        builder.append(e).append(SPACE);
      }
    }
    expected = builder.deleteCharAt(builder.length() - 1).toString();
    actual = StringUtility.removeRedundantSpaces(randomWithSpaces);

    assertNotNull(actual);
    assertFalse(actual.isEmpty());
    assertNotSame(randomWithSpaces, actual);
    assertNotSame(expected, actual);
    assertEquals('\"' + randomWithSpaces + '\"', expected, actual);

    // arbitrary string without leading and trailing spaces
    randomWithSpaces = randomWithSpaces.trim();
    actual = StringUtility.removeRedundantSpaces(randomWithSpaces);

    assertNotNull(actual);
    assertFalse(actual.isEmpty());
    assertNotSame(randomWithSpaces, actual);
    assertNotSame(expected, actual);
    assertEquals('\"' + randomWithSpaces + '\"', expected, actual);

    // long arbitrary string
    randomWithSpaces = "  S ldS rtjrtdkk rgergjul  vnVK K xZg   d  sZdWo PDlhkUr tMXpTg k qNu bAXJWb Y  vT C weglkxg rtjdtyk trrtu   ";
    expecteds = randomWithSpaces.split(SPACE);
    builder = new StringBuilder();
    for (final String e : expecteds) {
      if (e.length() > 0) {
        builder.append(e).append(SPACE);
      }
    }
    expected = builder.deleteCharAt(builder.length() - 1).toString();
    actual = StringUtility.removeRedundantSpaces(randomWithSpaces);

    assertNotNull(actual);
    assertFalse(actual.isEmpty());
    assertNotSame(randomWithSpaces, actual);
    assertNotSame(expected, actual);
    assertEquals('\"' + randomWithSpaces + '\"', expected, actual);
  }

  @Test
  public void concatenate() {
    final String[] randoms = new String[5];
    final StringBuilder expected = new StringBuilder(MAXIMUM_STRING_LENGTH * randoms.length);
    for (int i = 0; i < randoms.length; ++i) {
      final String random = newRandomUpperCase(true);
      randoms[i] = random;
      expected.append(random);
    }

    assertEquals(expected.toString(), StringUtility.concatenate(randoms));
  }

  @Test
  public void split() {
    final String text = "18459,2010/08/28,21:58:20";
    final String[] expected = text.split(String.valueOf(COMMA));
    final List<String> actual = StringUtility.split(text, COMMA);

    assertNotNull(actual);
    assertFalse(actual.isEmpty());
    assertArrayEquals(expected, actual.toArray(new String[actual.size()]));
  }

  @Test
  public void isNumber() throws Exception {
    final String number = Integer.toString(rng.nextInt(Integer.MAX_VALUE));
    assertTrue(StringUtility.isNumber(number));
    assertNotNull(number);

    final String nonNumber = newRandomUpperCase(true);

    assertFalse(StringUtility.isNumber(nonNumber));
    assertNotNull(nonNumber);

    assertFalse(StringUtility.isNumber(EMPTY));

    assertFalse(StringUtility.isNumber(number + nonNumber));

    thrown.expect(NullPointerException.class);
    StringUtility.isNumber(null);
  }

  @Test
  public void reverseSort() throws Exception {
    final List<String> expecteds = new ArrayList<>();
    for (int i = 0; i < 5; ++i) {
      expecteds.add(newRandomUpperCase(true));
    }
    final List<String> actuals = new ArrayList<>(expecteds);

    Collections.sort(expecteds, new Comparator<String>() {
      @Override
      public int compare(final String o1, final String o2) {
        return o2.compareTo(o1);
      }
    });
    StringUtility.reverseSort(actuals);

    assertEquals(expecteds, actuals);
  }

  @Test
  public void characterComparator() throws Exception {
    final char digit = '0';
    final char upperCaseCharacter = UPPERCASE_CHARACTERS[rng.nextInt(UPPERCASE_CHARACTERS.length)];
    final char lowerCaseCharacter = Character.toLowerCase(UPPERCASE_CHARACTERS[rng.nextInt(UPPERCASE_CHARACTERS.length)]);

    for (final CharacterComparator comparator : CharacterComparator.values()) {
      switch (comparator) {
        case ALPHABETIC:
          assertFalse(comparator.compare(digit));
          assertTrue(comparator.compare(upperCaseCharacter));
          assertTrue(comparator.compare(lowerCaseCharacter));
          break;

        case DIGIT:
          assertTrue(comparator.compare(digit));
          assertFalse(comparator.compare(upperCaseCharacter));
          assertFalse(comparator.compare(lowerCaseCharacter));
          break;

        case UPPERCASE:
          assertFalse(comparator.compare(digit));
          assertTrue(comparator.compare(upperCaseCharacter));
          assertFalse(comparator.compare(lowerCaseCharacter));
          break;

        default:
          fail("undefined comparator");
          break;
      }
    }

    thrown.expect(IllegalArgumentException.class);
    CharacterComparator.valueOf(newRandomUpperCase(true));
  }

  @Test
  public void findNthLastComma() throws Exception {
    assertEquals(NONE, StringUtility.findNthLast(COMMA, EMPTY, 1));
    assertEquals(0, StringUtility.findNthLast(COMMA, ",", 1));
    assertEquals(NONE, StringUtility.findNthLast(COMMA, " ", 1));

    assertEquals(NONE, StringUtility.findNthLast(COMMA, ",", 2));
  }

  @Test
  public void findNthComma() throws Exception {
    assertEquals(NONE, StringUtility.findNth(COMMA, EMPTY, 1, 0));

    assertEquals(0, StringUtility.findNth(COMMA, ",", 1, 0));
    assertEquals(NONE, StringUtility.findNth(COMMA, ",", 1, 1));
    assertEquals(NONE, StringUtility.findNth(COMMA, ",", 1, 2));

    assertEquals(NONE, StringUtility.findNth(COMMA, " ", 1, 0));
    assertEquals(NONE, StringUtility.findNth(COMMA, " ", 2, 0));
  }

  @Test
  public void findNthCommas() throws Exception {
    final String line = "a,b,c,d,e";

    assertEquals(7, StringUtility.findNth(COMMA, line, 4, 0));
    assertEquals(7, StringUtility.findNthLast(COMMA, line, 1));
    assertEquals(StringUtility.findNth(COMMA, line, 4, 0), StringUtility.findNthLast(COMMA, line, 1));

    assertEquals(StringUtility.findNth(COMMA, line, 3, 0), StringUtility.findNthLast(COMMA, line, 2));

    assertEquals(StringUtility.findNth(COMMA, line, 1, 2), StringUtility.findNthLast(COMMA, line, 3));
  }

  private static final String insertRandomSpaces(final String operand) {
    // bracket with leading and trailing spaces
    final StringBuilder operandWithSpaces = new StringBuilder(SPACE).append(operand).append(SPACE);

    int randomSpaceIndex = operandWithSpaces.length();
    while (randomSpaceIndex > 1) {
      randomSpaceIndex = rng.nextInt(randomSpaceIndex);
      operandWithSpaces.insert(randomSpaceIndex, SPACE);
    }

    return operandWithSpaces.toString();
  }

  public static final String newRandomUpperCase(final boolean containsLowerCase) {
    // generate a random string of upper case characters
    final int length = rng.nextInt(MAXIMUM_STRING_LENGTH) + 2;
    final StringBuilder randomUpperCase = new StringBuilder(length);
    for (int i = 0; i < length; ++i) {
      final int randomIndex = rng.nextInt(UPPERCASE_CHARACTERS.length);
      final char randomCharacter = UPPERCASE_CHARACTERS[randomIndex];
      randomUpperCase.append(randomCharacter);
    }

    // modify string according to method parameter
    if (containsLowerCase) {
      int numberOfLowerCase = rng.nextInt(length - 1);
      if (numberOfLowerCase == 0) {
        ++numberOfLowerCase;
      }
      for (int i = 0; i < numberOfLowerCase; ++i) {
        final int randomIndex = rng.nextInt(length);
        final char randomCharacter = randomUpperCase.charAt(randomIndex);
        randomUpperCase.setCharAt(randomIndex,
                                  Character.toLowerCase(randomCharacter));
      }
    }

    return randomUpperCase.toString();
  }

}

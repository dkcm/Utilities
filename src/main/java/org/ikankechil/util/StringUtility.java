/**
 * StringUtility.java  v0.1  17 October 2008 7:15:48 PM
 *
 * Copyright © 2008-2012 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for string manipulation.
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public final class StringUtility {

  private static final int               ZERO          = 0;
  private static final int               ONE           = 1;

  public static final Comparator<String> REVERSE_ORDER = new Comparator<String>() {
    @Override
    public int compare(final String o1, final String o2) {
      return o2.compareTo(o1);
    }
  };

  // Functor
  enum CharacterComparator {
    ALPHABETIC {
      @Override
      public boolean compare(final char character) {
        return Character.isAlphabetic(character);
      }
    },
    DIGIT {
      @Override
      public boolean compare(final char character) {
        return Character.isDigit(character);
      }
    },
    UPPERCASE {
      @Override
      public boolean compare(final char character) {
        return Character.isUpperCase(character);
      }
    };

    public abstract boolean compare(final char character);

  }

  private StringUtility() { /* disallow instantiation */ }

  /**
   * Converts <code>text</code> into upper and lower case.
   *
   * @param text
   * @return
   */
  public static final String toUpperAndLowerCase(final String text) {
    final char[] characters = text.trim().toCharArray();
    boolean wordStartFound = false;
    for (int i = 0; i < characters.length; ++i) {
      final char character = characters[i];
      // upper case
      if (Character.isUpperCase(character)) {
        if (wordStartFound) {
          characters[i] = Character.toLowerCase(character);
        }
        else {
          wordStartFound = true;
        }
      }
      // lower case
      else if (Character.isLowerCase(character)) {
        if (!wordStartFound) {
          wordStartFound = true;
          characters[i] = Character.toUpperCase(character);
        }
      }
      // space
      else if (Character.isWhitespace(character)) {
        wordStartFound = false;
      }
    }
    return new String(characters);
  }

  /**
   * Checks whether the <code>text</code> contains any lower case characters.
   *
   * @param text the <code>String</code> to be checked.
   * @return <code>true</code> if at least one character is lower case.
   */
  public static final boolean containsLowerCase(final String text) {
    boolean containsLowerCase = false;
    for (final char letter : text.toCharArray()) {
      if (Character.isLowerCase(letter)) {
        containsLowerCase = true;
        break;
      }
    }
    return containsLowerCase;
  }

  public static final int indexOfAlphabetic(final String text) {
    return indexOf(text, CharacterComparator.ALPHABETIC);
  }

  public static final int lastIndexOfAlphabetic(final String text) {
    return lastIndexOf(text, CharacterComparator.ALPHABETIC);
  }

  /**
   * Returns the index of the first occurrence of a digit.
   *
   * @param text
   * @return
   */
  public static final int indexOfDigit(final String text) {
    return indexOf(text, CharacterComparator.DIGIT);
  }

  /**
   * Returns the index of the last occurrence of a digit.
   *
   * @param text
   * @return
   */
  public static final int lastIndexOfDigit(final String text) {
    return lastIndexOf(text, CharacterComparator.DIGIT);
  }

  /**
   * Returns the index of the first occurrence of an upper case character.
   *
   * @param text
   * @return
   */
  public static final int indexOfUpperCase(final String text) {
    return indexOf(text, CharacterComparator.UPPERCASE);
  }

  /**
   * Returns the index of the last occurrence of an upper case character.
   *
   * @param text
   * @return
   */
  public static final int lastIndexOfUpperCase(final String text) {
    return lastIndexOf(text, CharacterComparator.UPPERCASE);
  }

  private static final int indexOf(final String text, final CharacterComparator comparator) {
    int index = 0;
    for (final char letter : text.toCharArray()) {
      if (comparator.compare(letter)) {
        break;
      }
      ++index;
    }
    return (index >= text.length()) ? -1 : index;
  }

  private static final int lastIndexOf(final String text, final CharacterComparator comparator) {
    final char[] characters = text.toCharArray();
    int index = characters.length - 1;
    for (; index >= 0; --index) {
      if (comparator.compare(characters[index])) {
        break;
      }
    }
    return index;
  }

  /**
   * Removes redundant spaces, leaving only at most one whitespace between
   * words.
   *
   * @param text
   * @return
   */
  public static final String removeRedundantSpaces(final String text) {
    final char[] oldCharacters = text.toCharArray();
    final char[] newCharacters = new char[oldCharacters.length];

    boolean isPreviousCharacterWhitespace = true;
    int length = 0, start = -1, end = -1;
    for (int c = 0; c < oldCharacters.length; ++c) {
      final char current = oldCharacters[c];
      if (Character.isWhitespace(current)) {
        if (isPreviousCharacterWhitespace) {
          if ((end > start) && (start > -1)) {
            System.arraycopy(oldCharacters, start, newCharacters, length, (end - start) + 1);
            length += (end - start) + 1;
            start = -1;
            end = -1;
          }
        }
        else {
          end = c;
          isPreviousCharacterWhitespace = true;
        }
      }
      // current character is not whitespace
      else if (isPreviousCharacterWhitespace) {
        if (start < 0) {
          start = c;
        }
        isPreviousCharacterWhitespace = false;
      }
    }

    if (start >= length) {
      if (!isPreviousCharacterWhitespace) {
        end = oldCharacters.length;
      }
      System.arraycopy(oldCharacters, start, newCharacters, length, end - start);
      length += end - start;
    }
    // get rid of trailing whitespace
    else if (isPreviousCharacterWhitespace && (length > 0)) {
      --length;
    }

    return new String(newCharacters, 0, length);
  }

  /**
   * Concatenates strings.
   *
   * @param text text fragments to be appended one after another
   * @return
   */
  public static final String concatenate(final String... text) {
    int textLength = 0;
    for (final String word : text) {
      textLength += word.length();
    }
    final char[] characters = new char[textLength];
    int start = 0;
    for (final String word : text) {
      word.getChars(0, word.length(), characters, start);
      start += word.length();
    }

    return new String(characters);
  }

  /**
   * Splits <code>text</code> around <code>separator</code>.
   *
   * @param text
   * @param separator
   * @return
   */
  public static final List<String> split(final String text, final char separator) {
    final List<String> strings = new ArrayList<>();

    final int length = text.length();
    final char[] characters = new char[length + 1];
    characters[length] = separator;
    text.getChars(0, length, characters, 0);
    int start = 0, end = 0;
    for (final char letter : characters) {
      if (letter == separator) {
        strings.add(new String(characters, start, end - start));
        start = ++end;
      }
      else {
        ++end;
      }
    }

    return strings;
  }

  public static final boolean isNumber(final String text) {
    boolean isNumber = !text.isEmpty();
    for (int i = 0; isNumber && (i < text.length()); ++i) {
      isNumber &= Character.isDigit(text.charAt(i));
    }
    return isNumber;
  }

  public static final void reverseSort(final List<String> lines) {
    Collections.sort(lines, REVERSE_ORDER);
  }

  /**
   * Finds the <code>n</code>th character in <code>line</code> starting from
   * <code>start</code>.
   *
   * @param character
   * @param line
   * @param n
   * @param start character search starting point
   *
   * @return index of the <code>n</code>th character
   */
  public static final int findNth(final char character, final String line, final int n, final int start) {
    int location = -ONE;
    for (int i = start, characters = ZERO; i < line.length(); ++i) {
      if ((line.charAt(i) == character) &&
          (++characters == n)) {
        location = i;
        break;
      }
    }
    return location; // -1 if no matching characters
  }

  /**
   * Finds the <code>n</code>th last character in <code>line</code>.
   *
   * @param character
   * @param line
   * @param n
   * @return index of the <code>n</code>th last character
   */
  public static final int findNthLast(final char character, final String line, final int n) {
    int location = line.length() - ONE;
    for (int characters = n; location >= ZERO; --location) {
      if ((line.charAt(location) == character) &&
          (--characters == ZERO)) {
        break;
      }
    }
    return location; // -1 if no matching characters
  }

}

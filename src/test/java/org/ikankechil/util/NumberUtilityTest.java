/**
 * NumberUtilityTest.java  v0.1  5 December 2014 1:45:56 PM
 *
 * Copyright © 2014-2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit test for <code>NumberUtility</code>.
 *
 * @author Daniel Kuan
 * @version
 */
public class NumberUtilityTest {

  @Test
  public void doubleToStringNaN() {
    assertEquals("NaN", NumberUtility.toString(Double.NaN));
  }

  @Test
  public void doubleToStringNegativeNaN() {
    assertEquals("NaN", NumberUtility.toString(-Double.NaN));
  }

  @Test
  public void doubleToStringInfinity() {
    assertEquals(Double.toString(Double.POSITIVE_INFINITY),
                 NumberUtility.toString(Double.POSITIVE_INFINITY));
  }

  @Test
  public void doubleToStringNegativeInfinity() {
    assertEquals(Double.toString(Double.NEGATIVE_INFINITY),
                 NumberUtility.toString(Double.NEGATIVE_INFINITY));
  }

  @Test
  public void doubleToStringZero() {
    assertEquals("0.000000000000000000", NumberUtility.toString(0.0d));
  }

  @Test
  public void doubleToStringSmallNegative() {
    assertEquals("-0.000001113856002816", NumberUtility.toString(-0.000001113856002816));
  }

  @Test
  public void doubleToStringMinusOne() {
    assertEquals("-1.000000000000000000", NumberUtility.toString(-1.000000000000000000));
  }

  @Test
  public void doubleToStringLessThanMinusOne() {
    assertEquals("-14.000001397565240448", NumberUtility.toString(-14.000001397565240448));
  }

  @Test
  public void doubleToStringSmallPositive() {
    assertEquals("0.000001113856002816", NumberUtility.toString(0.000001113856002816));
  }

  @Test
  public void doubleToStringOne() {
    assertEquals("1.000000000000000000", NumberUtility.toString(1.000000000000000000));
  }

  @Test
  public void doubleToStringMoreThanOne() {
    assertEquals("14.000001397565240448", NumberUtility.toString(14.000001397565240448));
  }

}

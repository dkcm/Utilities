/**
 * IRRTest.java  v0.1  23 February 2016 11:04:13 pm
 *
 * Copyright © 2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.math;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for <code>IRR</code>.
 *
 *
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public class IRRTest {

  private static final double    INTERNAL_RATE_OF_RETURN = 0.02720;
  private static final double[]  CASHFLOWS               = new double[21];
  private static final double    ESTIMATE                = 0.03250;
  private static final double    DELTA                   = 5E-6;

  @Rule
  public final ExpectedException thrown                  = ExpectedException.none();

  @BeforeClass
  public static void setUpBeforeClass() {
    final double annualPremium = -1081.8;
    int i = -1;
    CASHFLOWS[++i] = -811.35;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = 518.2;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = 2176.77;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = 691.2;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = annualPremium;
    CASHFLOWS[++i] = 20045.43;
  }

  @Test
  public void irr() {
    assertEquals(INTERNAL_RATE_OF_RETURN, IRR.irr(CASHFLOWS), DELTA);
  }

  @Test
  public void irrWithEstimate() {
    assertEquals(INTERNAL_RATE_OF_RETURN, IRR.irr(CASHFLOWS, ESTIMATE), DELTA);
    assertEquals(INTERNAL_RATE_OF_RETURN, IRR.irr(CASHFLOWS, ESTIMATE, CASHFLOWS.length), DELTA);
  }

  @Test
  public void irrTruncatedDuration() {
    for (final int duration : new int[] { 1, CASHFLOWS.length / 2, CASHFLOWS.length - 1}) {
      assertEquals(Double.NaN, IRR.irr(CASHFLOWS, ESTIMATE, duration), DELTA);
    }
  }

  @Test
  public void irrTruncatedDurationGoodEstimate() {
    assertEquals(-0.31150, IRR.irr(CASHFLOWS, -0.5, 12), DELTA);
  }

  @Test
  public void irrTruncatedDurationGoodEstimateSufficientIterations() {
    assertEquals(-0.31150, IRR.irr(CASHFLOWS, -0.25, 12, 1000, DELTA), DELTA);
  }

  @Test
  public void irrTruncatedDurationBadEstimate() {
    assertEquals(Double.NaN, IRR.irr(CASHFLOWS, ESTIMATE, 12), DELTA);
  }

  @Test
  public void negativeDelta() {
    // -ve delta
    thrown.expect(IllegalArgumentException.class);
    IRR.irr(CASHFLOWS, ESTIMATE, CASHFLOWS.length, 0, -DELTA);
  }

  @Test
  public void negativeIterations() {
    // -ve iterations
    thrown.expect(IllegalArgumentException.class);
    IRR.irr(CASHFLOWS, ESTIMATE, CASHFLOWS.length, -1, DELTA);
  }

  @Test
  public void durationLongerThanCashflows() {
    // duration > CASHFLOWS.length
    thrown.expect(IllegalArgumentException.class);
    IRR.irr(CASHFLOWS, ESTIMATE, CASHFLOWS.length + 1);
  }

  @Test
  public void durationLongerThanCashflows2() {
    thrown.expect(IllegalArgumentException.class);
    IRR.irr(CASHFLOWS, ESTIMATE, CASHFLOWS.length + 1, 0, DELTA);
  }

}

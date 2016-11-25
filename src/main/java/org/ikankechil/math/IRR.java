/**
 * IRR.java  v0.1  19 February 2016 7:26:19 PM
 *
 * Copyright © 2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.math;

/**
 * Internal rate of return
 *
 * <p>https://en.wikipedia.org/wiki/Internal_rate_of_return#Numerical_solution
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public class IRR {

  private static final double ESTIMATE   = 0.1;
  private static final int    ITERATIONS = 20;
  private static final double DELTA      = 1e-7;

  private IRR() { /* disallow instantiation */ }

  public static final double irr(final double[] cashflows) {
    return irr(cashflows, ESTIMATE);
  }

  public static final double irr(final double[] cashflows, final double estimate) {
    return irr(cashflows, estimate, cashflows.length);
  }

  public static double irr(final double[] cashflows,
                           final double estimate,
                           final int duration) {
    return irr(cashflows, estimate, duration, ITERATIONS, DELTA);
  }

  public static double irr(final double[] cashflows,
                           final double estimate,
                           final int duration,
                           final int iterations,
                           final double delta) {
    throwExceptionIfWrong(cashflows.length, duration, delta, iterations);

    // NPV(n) = Sum(C(n) / (1 + r)^n) = 0
    // r(n+1) = r(n) - NPV(n) * (r(n) - r(n-1)) / (NPV(n) - NPV(n-1))
    double irr = Double.NaN;

    double r0 = estimate;
    double r1 = 0;

    int i = 0;
    while (++i <= iterations) {
      int c = 0;
      double npv = cashflows[c];
      double npv1 = 0;
      final double factor = 1.0 + r0;
      for (double denominator = factor; ++c < duration; ) {
        final double cashflow = cashflows[c];
        npv += cashflow / denominator;
        denominator *= factor;
        npv1 -= c * cashflow / denominator; // first-order derivative of NPV
      }

      // Newton-Raphson Method: x1 = x0 - f(x0) / f'(x0)
      r1 = r0 - npv / npv1;

      if (Math.abs(r1 - r0) <= delta) {
        irr = r1;
        break;
      }

      r0 = r1;
    }

    return irr;
  }

  private static final void throwExceptionIfWrong(final int cashflowsLength,
                                                  final int duration,
                                                  final double delta,
                                                  final int iterations) {
    if (duration > cashflowsLength) {
      throw new IllegalArgumentException(String.format("Duration longer than cashflows: %s > %s",
                                                       duration,
                                                       cashflowsLength));
    }
    else if (delta < 0d) {
      throw new IllegalArgumentException(String.format("Negative delta: %d",
                                                       delta));
    }
    else if (iterations < 0) {
      throw new IllegalArgumentException(String.format("Negative iterations: %s",
                                                       iterations));
    }
  }

}

/**
 * NumberUtility.java  v0.2  5 December 2014 11:33:18 AM
 *
 * Copyright © 2014-2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.util;

/**
 *
 *
 * @author Daniel Kuan
 * @version 0.2
 */
public final class NumberUtility {

  private static final char   DOT        = '.';
  private static final char   MINUS      = '-';

  private static final char   INFINITY[] = { 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y' };
  private static final char   NAN[]      = { 'N', 'a', 'N' };

  private static final double PRECISION  = 1e18;

  private NumberUtility() { /* disallow instantiation */ }

  public static final String toString(final double d) {
    final StringBuilder sb = new StringBuilder();
    append(d, sb);
    return sb.toString();
  }

  /**
   * Appends a <code>double</code> to a <code>StringBuilder</code>. Supports 18
   * decimal places.
   *
   * @param d
   * @param sb
   */
  public static final void append(final double d, final StringBuilder sb) {
    if (Double.isNaN(d)) {
      sb.append(NAN);
    }
    else if (d == Double.POSITIVE_INFINITY) {
      sb.append(INFINITY);
    }
    else if (d == Double.NEGATIVE_INFINITY) {
      sb.append(MINUS).append(INFINITY);
    }
    else {
      // split double along '.' then append
      final long i = (long) d;
      double f = d > 0.0d ? d - i : i - d;      // f > 0
      final long f2 = (long) (++f * PRECISION); // increment fractional by 1 to preserve zeroes

      if ((-1.0d < d) && (d < 0.0d)) {          // '-' not preserved when casting double to long
        sb.append(MINUS);                       // when -1.0 < d < 0
      }
      final int length = sb.append(i).append(DOT).length();
      sb.append(f2).deleteCharAt(length);       // remove 1
    }
  }

  public static final void interpolate(final int x1,
                                       final double y1,
                                       final int x2,
                                       final double y2,
                                       final double... f) {
    // General equation of a straight line: y = mx + c
    final double m = gradient(x1, y1, x2, y2); // m = (y2 - y1) / (x2 - x1)
    final double c = y1;

    double y = c;
    for (int x = x1 + 1; x < x2; ++x) {
      f[x] = (y += m); // multiplication by repeated addition
    }
  }

  public static final double gradient(final double x1,
                                      final double y1,
                                      final double x2,
                                      final double y2) {
    return (y2 - y1) / (x2 - x1);
  }

  public static final double intercept(final double x,
                                       final double y,
                                       final double gradient) {
    // General equation of a straight line: y = mx + c
    return y - (gradient * x);
  }

  public static final double f(final double x,
                               final double gradient,
                               final double intercept) { // TODO rename method
    return (gradient * x) + intercept; // y = f(x) = mx + c
  }

}

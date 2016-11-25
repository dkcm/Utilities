/**
 * TextTransform.java  v0.1  16 December 2013 11:46:14 PM
 *
 * Copyright © 2013-2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

/**
 * Type description goes here.
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public interface TextTransform {

  /**
   * Transforms a <code>String</code> into another.
   *
   * @param line the <code>String</code> to be transformed
   * @return the transformed <code>String</code>
   */
  public String transform(final String line);

}

/**
 * TextTransformer.java  v0.2  4 January 2014 7:20:35 PM
 *
 * Copyright © 2013-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Type description goes here.
 *
 * @author Daniel Kuan
 * @version 0.2
 */
public class TextTransformer {

  private final TextTransform transform;
  private final boolean       reverseOrder;

  private static final String EMPTY  = "";

  private static final Logger logger = LoggerFactory.getLogger(TextTransformer.class);

  public TextTransformer(final TextTransform transform) {
    // source data order is kept "as-is" by default
    this(transform, false);
  }

  /**
   * @param transform
   * @param reverseOrder if true, reverse the order of incoming source data
   */
  public TextTransformer(final TextTransform transform, final boolean reverseOrder) {
    if (transform == null) {
      throw new NullPointerException("Null transform");
    }
    this.transform = transform;
    this.reverseOrder = reverseOrder;
  }

  /**
   * Transforms in-place a series of lines into another. The default
   * implementation transforms one line at a time.
   *
   * @param lines
   * @return the transformed <code>List</code> of <code>String</code>s
   */
  public List<String> transform(final List<String> lines) {
    // TextTransformer figures out which lines to transform and/or how many
    // lines to take in as input during each iteration, whereas TextTransform is
    // concerned with transforming each individual line

    if (reverseOrder) {
      Collections.reverse(lines); // TODO optimize
      logger.debug("Reversed lines' order");
    }
    lines.add(EMPTY);
    // setting an element retrieves current value
    String line = lines.set(0, EMPTY);
    for (int i = 1; i < lines.size(); ++i) {
      line = lines.set(i, transform.transform(line));
    }
    // remove redundant line
    lines.remove(0);
    logger.info("Transformation complete");

    return lines;
  }

}

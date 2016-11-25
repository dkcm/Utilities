/**
 * FileUtilityTest.java  v0.1  23 May 2014 12:46:06 AM
 *
 * Copyright © 2014-2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for <code>FileUtility</code>.
 *
 * @author Daniel Kuan
 * @version
 */
public class FileUtilityTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public final void cannotDeleteNullStartPath() throws Exception {
    thrown.expect(NullPointerException.class);
    FileUtility.deleteFileTree(null);
  }

  @Test
  public final void cannotCopyNullSourceTarget() throws Exception {
    thrown.expect(NullPointerException.class);
    FileUtility.copy(null, null);
  }

}

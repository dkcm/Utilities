/**
 * URLInputStreamFactoryTest.java v0.1 7 April 2015 10:06:58 AM
 *
 * Copyright © 2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for <code>URLInputStreamFactory</code>.
 * <p>
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public class URLInputStreamFactoryTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void cannotCreateInputStreamWithNullURL() throws Exception {
    thrown.expect(NullPointerException.class);
    URLInputStreamFactory.newInputStream(null);
  }

}

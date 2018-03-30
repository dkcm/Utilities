/**
 * AbstractCommandLineInterface.java  v0.1  25 February 2016 11:27:19 pm
 *
 * Copyright © 2016-2018 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *  A superclass for command-line applications.
 *
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public abstract class AbstractCommandLineInterface<K, V> {

  protected final OptionParser  parser;

  protected static final Logger logger = LoggerFactory.getLogger(AbstractCommandLineInterface.class);

  public AbstractCommandLineInterface(final String nonOptionsDescription, final Class<K> nonOptionsClass) {
    parser = new OptionParser();

    // Configuring command-line options
    parser.acceptsAll(Arrays.asList("h", "?"), "Show help").forHelp();

    // operands
    parser.nonOptions(nonOptionsDescription)
          .ofType(nonOptionsClass);
  }

  public V execute(final String... arguments) throws Exception {
    logger.info("Command-line option(s): {}", (Object) arguments);
    start();
    try {
      final OptionSet options = parser.parse(arguments);
      @SuppressWarnings("unchecked")
      final List<K> nonOptionArguments = (List<K>) options.nonOptionArguments();

      workOn(options, nonOptionArguments);
    }
    catch (final OptionException |
                 IllegalArgumentException |
                 NullPointerException |
                 InterruptedException |
                 IOException e) {
      System.out.println("Command-line option(s): " + Arrays.asList(arguments));
      System.out.println("Error: " + e.getMessage());
      parser.printHelpOn(System.out);
      logger.error(e.getMessage(), e);
    }
    finally {
      stop();
    }
    return result();
  }

  protected abstract void start();

  protected abstract void workOn(final OptionSet options, final List<K> nonOptionArguments)
      throws OptionException, InterruptedException, IOException;

  protected abstract void stop();

  protected abstract V result();

  protected static final void checkIllegalOptions(final OptionSet options, final OptionSpec<?>... illegalOptions) {
    for (final OptionSpec<?> illegalOption : illegalOptions) {
      if (options.has(illegalOption)) {
        throw new IllegalArgumentException("Illegal option: " + illegalOption);
      }
    }
  }

}

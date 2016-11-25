/**
 * CompletionServiceFileVisitorTest.java v0.1 7 April 2015 11:44:48 AM
 *
 * Copyright © 2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.regex.PatternSyntaxException;

import org.ikankechil.synchronous.TaskHelper;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for <code>CompletionServiceFileVisitor</code>.
 * <p>
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public class CompletionServiceFileVisitorTest {

  private final TaskHelper<Path, String>             taskHelper      = new FileName();
  private final CompletionServiceFileVisitor<String> fileVisitor     = new CompletionServiceFileVisitor<>(ONLY_CSV, taskHelper, EXECUTOR);

  @Rule
  public final ExpectedException                     thrown          = ExpectedException.none();

  private static final ExecutorService               EXECUTOR        = Executors.newSingleThreadExecutor();

  private static final String                        EMPTY           = "";
  private static final Path                          START_PATH      = Paths.get(".//./tst/");
  private static final String                        ONLY_CSV        = "glob:*.csv";
  private static final String                        INVALID_PATTERN = "regex:{";
  private static final PathMatcher                   MATCHER         = FileSystems.getDefault().getPathMatcher(ONLY_CSV);

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    EXECUTOR.shutdown();
  }

  @SuppressWarnings("unused")
  @Test
  public void cannotInstantiateWithNullSyntaxAndPattern() {
    thrown.expect(NullPointerException.class);
    new CompletionServiceFileVisitor<>(null, taskHelper, EXECUTOR);
  }

  @SuppressWarnings("unused")
  @Test
  public void cannotInstantiateWithEmptySyntaxAndPattern() {
    thrown.expect(IllegalArgumentException.class);
    new CompletionServiceFileVisitor<>(EMPTY, taskHelper, EXECUTOR);
  }

  @SuppressWarnings("unused")
  @Test
  public void cannotInstantiateWithInvalidSyntaxAndPattern() {
    thrown.expect(PatternSyntaxException.class);
    new CompletionServiceFileVisitor<>(INVALID_PATTERN, taskHelper, EXECUTOR);
  }

  @SuppressWarnings("unused")
  @Test
  public void cannotInstantiateWithInvalidSyntaxAndPattern2() {
    thrown.expect(UnsupportedOperationException.class);
    new CompletionServiceFileVisitor<>(START_PATH + ONLY_CSV, taskHelper, EXECUTOR);
  }

  @SuppressWarnings("unused")
  @Test
  public void cannotInstantiateWithNullTaskHelper() {
    thrown.expect(NullPointerException.class);
    new CompletionServiceFileVisitor<>(ONLY_CSV, null, EXECUTOR);
  }

  @SuppressWarnings("unused")
  @Test
  public void cannotInstantiateWithNullExecutor() {
    thrown.expect(NullPointerException.class);
    new CompletionServiceFileVisitor<>(ONLY_CSV, taskHelper, null);
  }

  @Test
  public void storeFirstDirectoryVisited() throws IOException {
    final Path expected = START_PATH;
    Files.walkFileTree(expected, fileVisitor);

    assertEquals(expected, fileVisitor.startDirectory());
  }

  @Test
  public void executionFailures() throws IOException {
    final CompletionServiceFileVisitor<String> fv =
            new CompletionServiceFileVisitor<>(ONLY_CSV,
                                               new FileNameExecutionFailure(),
                                               EXECUTOR);
    Files.walkFileTree(START_PATH, fv);

    final int expected = fileCount(START_PATH);
    assertEquals(expected, fv.failures().size());
    assertTrue(fv.results().isEmpty());
  }

  @Test
  public void cancelledTasks() throws IOException {
    final CompletionServiceFileVisitor<String> fv =
            new CompletionServiceFileVisitor<String>(ONLY_CSV,
                                                     taskHelper,
                                                     EXECUTOR) {
      @Override
      protected void submitTasks(final Path file) throws IOException {
        // cancel task immediately after submitting it
        completionService.submit(taskHelper.newTask(file)).cancel(true);
        ++numberOfTasks;
      }
    };
    Files.walkFileTree(START_PATH, fv);

    final int expected = fileCount(START_PATH);
    assertEquals(expected, fv.failures().size());
    assertTrue(fv.results().isEmpty());
  }

  @Test
  public void results() throws IOException {
    Files.walkFileTree(START_PATH, fileVisitor);
    final List<String> actuals = fileVisitor.results();

    assertTrue(actuals.contains(TextIOTest.SOURCE_FILE.getName()));

    final int expected = fileCount(START_PATH);
    assertEquals(expected, actuals.size());
    assertTrue(fileVisitor.failures().isEmpty());
  }

  private static final int fileCount(final Path path) {
    int files = 0;
    if (Files.isDirectory(path)) {
      for (final File file : path.toFile().listFiles()) {
        if (file.isDirectory()) {
          files += fileCount(file.toPath()); // recurse
        }
        else if (MATCHER.matches(file.toPath().getFileName())) {
          ++files;
        }
      }
    }
    else if (MATCHER.matches(path.getFileName())) {
      ++files;
    }
    return files;
  }

  class FileName implements TaskHelper<Path, String> {

    @Override
    public Callable<String> newTask(final Path operand) {
      return new Callable<String>() {
        @Override
        public String call() throws Exception {
          return filename(operand);
        }
      };
    }

    @Override
    public String handleExecutionFailure(final ExecutionException eE, final Path operand) {
      return filename(operand);
    }

    @Override
    public String handleTaskCancellation(final CancellationException cE, final Path operand) {
      return filename(operand);
    }

    @Override
    public String handleTimeout(final TimeoutException tE, final Path operand) {
      return filename(operand);
    }

    String filename(final Path operand) {
      return operand.getFileName().toString();
    }

  }

  class FileNameExecutionFailure extends FileName {

    @Override
    public Callable<String> newTask(final Path operand) {
      return new Callable<String>() {
        @Override
        public String call() throws Exception {
          throw new ExecutionException(null);
        }
      };
    }

  }

}

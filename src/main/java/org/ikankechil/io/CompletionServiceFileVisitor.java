/**
 * CompletionServiceFileVisitor.java  v0.1  28 October 2014 1:42:58 PM
 *
 * Copyright © 2014-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.io;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.ikankechil.synchronous.TaskHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file visitor that submits a task (in the form of a <code>Callable</code>
 * via a given <code>TaskHelper</code>) to a <code>CompletionService</code> for
 * every file (but not directory) that matches a given name pattern. Name
 * pattern matching supports "glob" and "regex" syntaxes.
 *
 * @author Daniel Kuan
 * @version 0.1
 * @param <V> the result type of <code>TaskHelper</code>
 */
public class CompletionServiceFileVisitor<V> extends SimpleFileVisitor<Path> {

  private final PathMatcher            matcher;
  private Path                         startDirectory;
  protected int                        numberOfTasks;
  private final List<V>                results;
  private final List<Throwable>        failures;
  private final Map<Future<V>, Path>   futures;

  protected final TaskHelper<Path, V>  taskHelper;
  protected final CompletionService<V> completionService;

  private int                          filesVisited = 0;

  private static final Logger          logger       = LoggerFactory.getLogger(CompletionServiceFileVisitor.class);

  /**
   * @param syntaxAndPattern
   *          file name syntax and pattern that supports both "glob" and "regex"
   *          syntaxes
   * @param taskHelper
   * @param executor
   *          the executor that the <code>CompletionService</code> leverages
   * @throws IllegalArgumentException
   *           If <code>syntaxAndPattern</code> does not take the form:
   *           <code>syntax:pattern</code>
   * @throws java.util.regex.PatternSyntaxException
   *           If the pattern in <code>syntaxAndPattern</code> is invalid
   * @throws UnsupportedOperationException
   *           If the pattern syntax in <code>syntaxAndPattern</code> is not
   *           known to the implementation
   * @throws NullPointerException
   *           If any of <code>syntaxAndPattern</code>, <code>taskHelper</code>
   *           or <code>executor</code> are null
   *
   * @see java.nio.file.FileSystem#getPathMatcher(String)
   */
  public CompletionServiceFileVisitor(final String syntaxAndPattern, final TaskHelper<Path, V> taskHelper, final Executor executor) {
    matcher = FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
    if (taskHelper == null) {
      throw new NullPointerException("Null task helper");
    }
    completionService = new ExecutorCompletionService<>(executor);
    logger.debug("ExecutorService used: {}", executor);
    startDirectory = null;
    numberOfTasks = 0;
    failures = new ArrayList<>();
    results = new ArrayList<>();
    futures = new HashMap<>();

    this.taskHelper = taskHelper;
  }

  @Override
  public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
      throws IOException {
    // store the first (and also last) directory visited
    if (startDirectory == null) {
      startDirectory = dir;
      logger.debug("Starting in: {}", startDirectory);
      results.clear();
      failures.clear();
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
      throws IOException {
    if (matcher.matches(file.getFileName())) { // match file name only
      submitTasks(file);
      logger.debug("File match: {}", file);
    }
    else {
      logger.debug("File non-match: {}", file);
    }
    ++filesVisited;
    return FileVisitResult.CONTINUE;
  }

  /**
   * @throws IOException
   */
  protected void submitTasks(final Path file) throws IOException {
    // submit one task per file
    final Future<V> future = completionService.submit(taskHelper.newTask(file));
    futures.put(future, file);
    ++numberOfTasks;
  }

  @Override
  public FileVisitResult visitFileFailed(final Path file, final IOException exc)
      throws IOException {
    logger.warn("File visit failed: {}", file, exc);
    return super.visitFileFailed(file, exc);
  }

  @Override
  public FileVisitResult postVisitDirectory(final Path dir, final IOException exc)
      throws IOException {
    if (Files.isSameFile(startDirectory, dir)) {
      logger.info("Files visited: {}", filesVisited);
      logger.info("Tasks submitted: {}", numberOfTasks);

      // retrieve results
      try {
        for (int t = 0; t < numberOfTasks; ++t) {
          final Future<V> future = completionService.take();
          try {
            results.add(future.get());
          }
          catch (final ExecutionException eE) {
            taskHelper.handleExecutionFailure(eE, dir);
            failures.add(eE);
            logger.warn("Task failed for: {}.  Cause: {}",
                        futures.get(future),
                        eE.getCause(),
                        eE);
          }
          catch (final CancellationException cE) {
            taskHelper.handleTaskCancellation(cE, dir);
            failures.add(cE);
            logger.info("Task cancelled for: {}.  Cause: {}",
                        futures.get(future),
                        cE.getCause(),
                        cE);
          }
          finally {
            future.cancel(true);
          }
        }
        logger.info("Results retrieved: {}", numberOfTasks);
      }
      catch (final InterruptedException iE) {
        logger.warn("Interrupted: {}", iE.getCause(), iE);
      }
    }
    return FileVisitResult.CONTINUE;
  }

  public List<V> results() {
    return results;
  }

  public List<Throwable> failures() {
    return failures;
  }

  public Path startDirectory() {
    return startDirectory;
  }

}

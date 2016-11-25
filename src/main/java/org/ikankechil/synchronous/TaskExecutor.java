/**
 * TaskExecutor.java  v0.1  22 November 2008 1:03:17 AM
 *
 * Copyright © 2008-2016 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.synchronous;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for running tasks concurrently.
 *
 * @author Daniel Kuan
 * @version
 */
public class TaskExecutor {

  private final ExecutorService                executor;
  private final List<Object>                   failures;
  private final List<Object>                   cancellations;

  // Constants
  private static final int                     TIME_OUT        = Short.MAX_VALUE;
  private static final TimeUnit                TIME_OUT_UNIT   = TimeUnit.MILLISECONDS;

  private static final int                     PROCESSORS      = Runtime.getRuntime().availableProcessors();
  private static final int                     LOAD_MULTIPLIER = 25;

  static final Logger                          logger          = LoggerFactory.getLogger(TaskExecutor.class);

  public TaskExecutor() {
    this(null);
  }

  public TaskExecutor(final ExecutorService executor) {
    this.executor = executor != null ? executor
                                     : Executors.newFixedThreadPool(PROCESSORS * LOAD_MULTIPLIER);

    failures = newList(LOAD_MULTIPLIER);
    cancellations = newList(LOAD_MULTIPLIER);
  }

  /**
   * Creates a <code>List</code> of <code>Callable</code>s from the
   * <code>operands</code> with the help of <code>taskHelper</code>.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param operands the collection of operands
   * @param taskHelper helper class used in creating tasks and handling
   *          exceptions
   * @return a <code>List</code> of <code>Callable</code>s
   * @throws IllegalArgumentException if <code>operands</code> is empty
   * @throws NullPointerException if <code>operands</code>,
   *           <code>taskHelper</code> or any of the tasks created by
   *           <code>taskHelper</code> are null
   */
  public final <K, V> List<Callable<V>> newTasks(final Collection<? extends K> operands,
                                                 final TaskHelper<K, V> taskHelper) {
    throwExceptionIfEmpty(operands);

    final List<Callable<V>> tasks = newList(operands.size());
    for (final K operand : operands) {
      tasks.add(taskHelper.newTask(operand));
    }
    logger.info("{} tasks created.", operands.size());

    return tasks;
  }

  /**
   * Executes tasks created from the given <code>operands</code> with the aid of
   * <code>taskHelper</code>, returning the results mapped to their
   * corresponding operands when all complete.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param operands the collection of operands
   * @param taskHelper helper class used in creating tasks and handling
   *          exceptions
   * @return a <code>Map</code> of results associated to their corresponding
   *         operands
   * @throws IllegalArgumentException if <code>operands</code> is empty
   * @throws InterruptedException if interrupted while waiting, in which case
   *           unfinished tasks are cancelled
   * @throws NullPointerException if <code>operands</code>,
   *           <code>taskHelper</code> or any of the tasks created by
   *           <code>taskHelper</code> are null
   * @throws RejectedExecutionException if any task cannot be scheduled for
   *           execution
   */
  public <K, V> Map<K, V> executeAll(final Collection<? extends K> operands,
                                     final TaskHelper<K, V> taskHelper)
      throws InterruptedException {
    return executeAll(operands,
                      taskHelper,
                      TIME_OUT * operands.size(),
                      TIME_OUT_UNIT);
  }

  /**
   * Executes tasks created from the given <code>operands</code> with the aid of
   * <code>taskHelper</code>, returning the results mapped to their
   * corresponding operands when all complete or the <code>timeout</code>
   * expires, whichever happens first.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param operands the collection of operands
   * @param taskHelper helper class used in creating tasks and handling
   *          exceptions
   * @param timeout the maximum time to wait
   * @param unit the time unit of the timeout argument
   * @return a <code>Map</code> of results associated to their corresponding
   *         operands
   * @throws IllegalArgumentException if <code>operands</code> is empty
   * @throws InterruptedException if interrupted while waiting, in which case
   *           unfinished tasks are cancelled
   * @throws NullPointerException if <code>operands</code>, <code>unit</code>,
   *           <code>taskHelper</code> or any of the tasks created by
   *           <code>taskHelper</code> are null
   * @throws RejectedExecutionException if any task cannot be scheduled for
   *           execution
   */
  public <K, V> Map<K, V> executeAll(final Collection<? extends K> operands,
                                     final TaskHelper<K, V> taskHelper,
                                     final long timeout,
                                     final TimeUnit unit)
      throws InterruptedException {
    // create tasks, one for each operand
    final List<Callable<V>> tasks = newTasks(operands, taskHelper);

    // complete tasks
    // TODO how to implement and verify throttling?
    final List<Future<V>> futures = executor.invokeAll(tasks, timeout, unit);
    final int numberOfTasks = tasks.size();
    logger.info("All {} tasks executed.", numberOfTasks);

    // retrieve results
    final Map<K, V> results = new LinkedHashMap<>(numberOfTasks);
    final Iterator<? extends K> iterator = operands.iterator();

    clearExceptions();
    for (final Future<V> future : futures) {
      final K operand = iterator.next();
      V result;
      try {
        result = future.get();
      }
      catch (final ExecutionException eE) {
        // do not retry task
        result = taskHelper.handleExecutionFailure(eE, operand);
        failures.add(operand);
        logger.warn("Task failed: {}.  Cause: {}", operand, eE.getCause(), eE);
      }
      catch (final CancellationException cE) {
        result = taskHelper.handleTaskCancellation(cE, operand);
        cancellations.add(operand);
        logger.info("Task cancelled: {}.  Cause: {}", operand, cE.getCause(), cE);
      }
      finally {
        future.cancel(true);
      }

      results.put(operand, result);
    }
    logger.info("All {} results retrieved.  Successful: {}, Failed: {}, Cancelled: {}",
                numberOfTasks,
                (numberOfTasks - failures.size() - cancellations.size()),
                failures.size(),
                cancellations.size());

    return results;
  }

  /**
   * Executes tasks created from the given <code>operands</code> with the aid of
   * <code>taskHelper</code>, returning the result of one that has completed
   * successfully.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param operands the collection of operands
   * @param taskHelper helper class used in creating tasks and handling
   *          exceptions
   * @return the result returned by one of the tasks
   * @throws IllegalArgumentException if <code>operands</code> is empty
   * @throws InterruptedException if interrupted while waiting
   * @throws NullPointerException if <code>operands</code>,
   *           <code>taskHelper</code> or any of the tasks created by
   *           <code>taskHelper</code> are null
   * @throws RejectedExecutionException if tasks cannot be scheduled for
   *           execution
   */
  public <K, V> V executeAny(final Collection<? extends K> operands,
                             final TaskHelper<K, V> taskHelper)
      throws InterruptedException {
    return executeAny(operands,
                      taskHelper,
                      TIME_OUT * operands.size(),
                      TIME_OUT_UNIT);
  }

  /**
   * Executes tasks created from the given <code>operands</code> with the aid of
   * <code>taskHelper</code>, returning the result of one that has completed
   * successfully or the <code>timeout</code> expires, whichever happens first.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param operands the collection of operands
   * @param taskHelper helper class used in creating tasks and handling
   *          exceptions
   * @param timeout the maximum time to wait
   * @param unit the time unit of the timeout argument
   * @return the result returned by one of the tasks
   * @throws IllegalArgumentException if <code>operands</code> is empty
   * @throws InterruptedException if interrupted while waiting
   * @throws NullPointerException if <code>operands</code>, <code>unit</code>,
   *           <code>taskHelper</code> or any of the tasks created by
   *           <code>taskHelper</code> are null
   * @throws RejectedExecutionException if tasks cannot be scheduled for
   *           execution
   */
  public <K, V> V executeAny(final Collection<? extends K> operands,
                             final TaskHelper<K, V> taskHelper,
                             final long timeout,
                             final TimeUnit unit)
      throws InterruptedException {
    // create tasks, one for each operand
    final List<Callable<V>> tasks = newTasks(operands, taskHelper);

    clearExceptions();
    // complete tasks and retrieve result
    V result;
    try {
      result = executor.invokeAny(tasks, timeout, unit);
    }
    catch (final ExecutionException eE) {
      result = taskHelper.handleExecutionFailure(eE, null);
      logger.warn("Task failed.  Cause: {}", eE.getCause(), eE);
    }
    catch (final TimeoutException tE) {
      result = taskHelper.handleTimeout(tE, null);
      logger.warn("Task timed out: {}", tE.toString(), tE);
    }
    logger.info("Result retrieved.");

    return result;
  }

  /**
   * Submits tasks created from the given <code>operands</code> with the aid of
   * <code>taskHelper</code>.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param operands the collection of operands
   * @param taskHelper helper class used in creating tasks and handling
   *          exceptions
   * @return the <code>TaskCompletionService</code> through which the tasks have
   *         been submitted
   * @throws IllegalArgumentException if <code>operands</code> is empty
   * @throws NullPointerException if <code>operands</code>,
   *           <code>taskHelper</code> or any of the tasks created by
   *           <code>taskHelper</code> are null
   * @throws RejectedExecutionException if any of the tasks cannot be scheduled
   *           for execution
   */
  public <K, V> TaskCompletionService<K, V> submitAll(final Collection<? extends K> operands,
                                                      final TaskHelper<K, V> taskHelper) {
    throwExceptionIfEmpty(operands);

    // create tasks, one for each operand, and submit them
    final TaskCompletionService<K, V> service = new TaskCompletionService<>(executor, taskHelper);
    for (final K operand : operands) {
      service.submit(taskHelper.newTask(operand), operand);
    }
    logger.info("All {} tasks submitted.", operands.size());

    return service;
  }

  /**
   * Submits tasks created from the given <code>operands</code> with the aid of
   * <code>taskHelper</code>.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param operands the collection of operands
   * @param taskHelper helper class used in creating tasks and handling
   *          exceptions
   * @param futures the collection of <code>Future</code>s representing the
   *          pending results
   * @return a <code>CompletionService</code> through which the tasks have been
   *         submitted
   * @throws IllegalArgumentException if <code>operands</code> is empty
   * @throws NullPointerException if <code>operands</code>, <code>futures</code>
   *           , <code>taskHelper</code> or any of the tasks created by
   *           <code>taskHelper</code> are null
   * @throws RejectedExecutionException if any of the tasks cannot be scheduled
   *           for execution
   */
  @Deprecated
  public <K, V> CompletionService<V> submitAll(final Collection<? extends K> operands,
                                               final TaskHelper<K, V> taskHelper,
                                               final Map<Future<V>, K> futures) {
    throwExceptionIfEmpty(operands);

    // create tasks, one for each operand, and submit them
    final CompletionService<V> service = new ExecutorCompletionService<>(executor);
    for (final K operand : operands) {
      final Future<V> future = service.submit(taskHelper.newTask(operand));
      futures.put(future, operand);
    }
    logger.info("All {} tasks submitted.", operands.size());

    return service;
  }

  /**
   * Retrieves the results of completed tasks.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param service
   * @param operands the collection of operands
   * @param taskHelper helper class used in creating tasks and handling
   *          exceptions
   * @return a <code>List</code> of results
   * @throws IllegalArgumentException if <code>operands</code> is empty
   * @throws InterruptedException if interrupted while waiting
   * @throws NullPointerException if <code>service</code>, <code>operands</code>,
   *           or <code>taskHelper</code> are null
   */
  @Deprecated
  public <K, V> List<V> takeAll(final CompletionService<? extends V> service,
                                final Map<Future<V>, K> futures,
                                final TaskHelper<K, V> taskHelper)
      throws InterruptedException {
    return takeAll(service,
                   futures,
                   taskHelper,
                   TIME_OUT,
                   TIME_OUT_UNIT);
  }

  /**
   * Retrieves the results of completed tasks.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param service
   * @param operands the collection of operands
   * @param taskHelper helper class used in creating tasks and handling
   *          exceptions
   * @param timeout the maximum time to wait
   * @param unit the time unit of the timeout argument
   * @return a <code>List</code> of results
   * @throws IllegalArgumentException if <code>operands</code> is empty
   * @throws InterruptedException if interrupted while waiting
   * @throws NullPointerException if <code>service</code>, <code>operands</code>,
   *           <code>taskHelper</code> or <code>unit</code> are null
   */
  @Deprecated
  public <K, V> List<V> takeAll(final CompletionService<? extends V> service,
                                final Map<Future<V>, K> futures,
                                final TaskHelper<K, V> taskHelper,
                                final long timeout,
                                final TimeUnit unit)
      throws InterruptedException {
    throwExceptionIfEmpty(futures.keySet());
    final int numberOfTasks = futures.size();
    final List<V> results = newList(numberOfTasks);

    for (int t = 0; t < numberOfTasks; ++t) {
      final Future<? extends V> future = service.poll(timeout, unit);
      V result = null;
      try {
        result = future.get();
      }
      catch (final ExecutionException eE) {
        // map future with its operand
        result = taskHelper.handleExecutionFailure(eE, futures.get(future));
        logger.warn("Task failed.  Cause: {}", eE.getCause());
      }
      catch (final CancellationException cE) {
        result = taskHelper.handleTaskCancellation(cE, futures.get(future));
        logger.info("Task cancelled.  Cause: {}", cE.getCause());
      }
      finally {
        future.cancel(true);
      }
      results.add(result);
    }
    logger.info("All {} results retrieved.", numberOfTasks);
    // TODO report passes / failures

    return results;
  }

  /**
   * Retrieves the results of completed tasks.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param service
   * @return a <code>List</code> of results
   * @throws InterruptedException if interrupted while waiting
   * @throws NullPointerException if <code>service</code> is null
   */
  public <K, V> List<V> takeAll(final TaskCompletionService<K, V> service)
      throws InterruptedException {
    return takeAll(service,
                   TIME_OUT * service.getNumberOfTasks(),
                   TIME_OUT_UNIT);
  }

  /**
   * Retrieves the results of completed tasks.
   *
   * @param <K> operand type
   * @param <V> result type
   * @param service
   * @param timeout the maximum time to wait
   * @param unit the time unit of the timeout argument
   * @return a <code>List</code> of results
   * @throws InterruptedException if interrupted while waiting
   * @throws NullPointerException if <code>service</code> or <code>unit</code>
   *           are null
   */
  public <K, V> List<V> takeAll(final TaskCompletionService<K, V> service,
                                final long timeout,
                                final TimeUnit unit)
      throws InterruptedException {
    final int numberOfTasks = service.getNumberOfTasks();
    final List<V> results = newList(numberOfTasks);
    final TaskHelper<K, V> taskHelper = service.getTaskHelper();

    clearExceptions();
    for (int t = 0; t < numberOfTasks; ++t) {
      final Future<? extends V> future = service.poll(timeout, unit);
      V result = null;
      try {
        result = future.get();
      }
      catch (final ExecutionException eE) {
        // map future with its operand
        final K operand = service.get(future);
        result = taskHelper.handleExecutionFailure(eE, operand);
        failures.add(operand);
        logger.warn("Task failed: {}.  Cause: {}", operand, eE.getCause(), eE);
      }
      catch (final CancellationException cE) {
        final K operand = service.get(future);
        result = taskHelper.handleTaskCancellation(cE, operand);
        cancellations.add(operand);
        logger.info("Task cancelled: {}.  Cause: {}", operand, cE.getCause(), cE);
      }
      finally {
        future.cancel(true);
      }
      results.add(result);
    }
    logger.info("All {} results retrieved.  Successful: {}, Failed: {}, Cancelled: {}",
                numberOfTasks,
                (numberOfTasks - failures.size() - cancellations.size()),
                failures.size(),
                cancellations.size());

    return results;
  }

  /**
   * Attempts to cancel tasks submitted for execution and whose results have not
   * been retrieved.
   *
   * @param <V> result type
   * @param futures the collection of <code>Future</code>s whose tasks are to be
   *          cancelled
   * @throws IllegalArgumentException if <code>futures</code> is empty
   * @throws NullPointerException if <code>futures</code> or any of the
   *           <code>Future</code>s contained herein are null
   * @see {@link TaskExecutor#submitAll(Collection, TaskHelper, Collection)}
   */
  public <V> void cancel(final Collection<Future<V>> futures) {
    throwExceptionIfEmpty(futures);
    // only applies to submit-take use model
    for (final Future<V> future : futures) {
      future.cancel(true);
    }
    logger.info("Cancel request submitted.");
  }

  /**
   * Attempts to cancel tasks submitted for execution and whose results have not
   * been retrieved.
   *
   * @param <V> result type
   * @throws IllegalArgumentException if <code>futures</code> is empty
   * @throws NullPointerException if <code>futures</code> or any of the
   *           <code>Future</code>s contained herein are null
   * @see {@link TaskExecutor#submitAll(Collection, TaskHelper)}
   */
  public <K, V> void cancel(final TaskCompletionService<K, V> service) {
    cancel(service.getFutures());
  }

  private static final void throwExceptionIfEmpty(final Collection<?> collection) {
    if (collection.isEmpty()) {
      throw new IllegalArgumentException("collection cannot be empty");
    }
  }

  private static final <E> List<E> newList(final int size) {
    return new ArrayList<>(size);
  }

  /**
   * Stops <code>TaskExecutor</code> in a gradual and orderly manner by
   * completing existing tasks and forbidding any new tasks. Repeated calls have
   * no effect.
   *
   * @return <code>true</code> if this executor terminated and
   *         <code>false</code> if the timeout elapsed before termination
   * @throws InterruptedException if interrupted while waiting
   */
  public boolean stop() throws InterruptedException {
    clearExceptions();
    logger.info("Executor shutdown requested.");
    executor.shutdown();
    return executor.awaitTermination(TIME_OUT, TIME_OUT_UNIT);
  }

  private void clearExceptions() {
    failures.clear();
    cancellations.clear();
    logger.debug("Exceptions cleared.");
  }

  public List<Object> getFailures() {
    return failures;
  }

  public List<Object> getCancellations() {
    return cancellations;
  }

}

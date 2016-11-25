/**
 * TaskCompletionService.java  v0.1  27 January 2012 10:13:20 PM
 *
 * Copyright © 2012 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.synchronous;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Type description goes here.
 *
 * @author Daniel Kuan
 * @version 0.1
 */
public class TaskCompletionService<K, V> implements CompletionService<V> {

  private final CompletionService<V> completionService;
  private final TaskHelper<K, V>     taskHelper;
  final Map<Future<V>, K>            futures;
  private int                        numberOfTasks;

  public TaskCompletionService(final Executor executor,
                               final TaskHelper<K, V> taskHelper) {
    completionService = new ExecutorCompletionService<>(executor);
    this.taskHelper = taskHelper;
    futures = new HashMap<>();
    numberOfTasks = 0;
  }

  public Future<V> submit(final Callable<V> task, final K operand) {
    Future<V> future = completionService.submit(task);
    futures.put(future, operand);
    ++numberOfTasks;
    return future;
  }

  @Override
  public Future<V> submit(final Callable<V> task) {
    Future<V> future = completionService.submit(task);
    ++numberOfTasks;
    return future;
  }

  @Override
  public Future<V> submit(final Runnable task, final V result) {
    ++numberOfTasks;
    return completionService.submit(task, result);
  }

  @Override
  public Future<V> take() throws InterruptedException {
    --numberOfTasks;
    return completionService.take();
  }

  @Override
  public Future<V> poll() {
    --numberOfTasks;
    return completionService.poll();
  }

  @Override
  public Future<V> poll(final long timeout, final TimeUnit unit)
      throws InterruptedException {
    --numberOfTasks;
    return completionService.poll(timeout, unit);
  }

  public TaskHelper<K, V> getTaskHelper() {
    return taskHelper;
  }

  public int getNumberOfTasks() {
    return numberOfTasks;
  }

  public K get(final Future<? extends V> future) {
    return futures.get(future);
  }

  public K put(final Future<V> future, final K operand) {
    return futures.put(future, operand);
  }

  public Collection<Future<V>> getFutures() {
    return futures.keySet();
  }

}

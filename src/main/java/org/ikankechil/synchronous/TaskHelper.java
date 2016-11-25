/**
 * TaskHelper.java  v0.1  2 December 2008 12:06:03 PM
 *
 * Copyright © 2008-2015 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.synchronous;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * A helper interface that creates tasks and handles task execution exceptions.
 *
 * @author Daniel Kuan
 * @version
 * @param <K> the operand type for which <code>Callable</code>s are to be
 *          created
 * @param <V> the result type of <code>Callable</code>'s <code>call</code>
 *          method
 */
public interface TaskHelper<K, V> {
  // TODO Can TaskHelper take over ticketing role of TaskCompletionService?

  /**
   * Creates a task based on the given <code>operand</code>.
   *
   * @param operand
   * @return a <code>Callable</code> which is never null
   */
  public Callable<V> newTask(final K operand);

  /**
   * Handles execution failures that arise from exceptions.
   *
   * @param eE the <code>ExecutionException</code> to be handled
   * @param operand the operand associated with the failed task
   * @return an input-dependent or default result
   */
  public V handleExecutionFailure(final ExecutionException eE, final K operand);

  /**
   * Handles task cancellations.
   *
   * @param cE the <code>CancellationException</code> to be handled
   * @param operand the operand associated with the cancelled task
   * @return
   */
  public V handleTaskCancellation(final CancellationException cE, final K operand);

  /**
   * Handles timeouts.
   *
   * @param tE the <code>TimeoutException</code> to be handled
   * @param operand
   * @return a value that represents timeout
   */
  public V handleTimeout(final TimeoutException tE, final K operand);

}

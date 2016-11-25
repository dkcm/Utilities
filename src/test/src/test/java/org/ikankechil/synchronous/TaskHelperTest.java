/**
 * TaskHelperTest.java  v0.1  1 May 2009 12:25:54 AM
 *
 * Copyright © 2009-2010 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.synchronous;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.ikankechil.synchronous.TaskHelper;
import org.junit.Test;

/**
 * JUnit test for <code>TaskHelper</code>.
 *
 * @author Daniel Kuan
 * @version
 */
public abstract class TaskHelperTest<K, V> {

  protected final TaskHelper<K, V> taskHelper = newTaskHelper();

  protected abstract TaskHelper<K, V> newTaskHelper();

  @Test
  public void testTaskCreation() {
    K operand = newOperand();

    assertNotNull(operand);

    Callable<V> expected = newTask(operand);
    Callable<V> actual = taskHelper.newTask(operand);

    assertNotNull(actual);
    assertNotNull(expected);
    assertNotSame(expected, actual);

    // check that task is of the correct Class
    assertTrue(actual.getClass().isInstance(expected));
  }

  protected abstract K newOperand();

  protected abstract Callable<V> newTask(final K operand);

  @Test
  public void testExecutionFailureHandling() {
    K operand = newOperand();
    ExecutionException eE = newExecutionException(operand);

    assertNotNull(eE);

    V expected = newFailureResult(operand);
    V actual = taskHelper.handleExecutionFailure(eE, operand);

    assertNotNull(actual);
    assertNotNull(expected);
    assertEquals(expected, actual);
    assertNotSame(expected, actual);
  }

  protected abstract V newFailureResult(final K operand);

  protected abstract ExecutionException newExecutionException(final K operand);

  @Test
  public void testTaskCancellationHandling() {
    K operand = newOperand();
    CancellationException cE = newCancellationException(operand);

    assertNotNull(cE);

    V expected = newCancellationResult(operand);
    V actual = taskHelper.handleTaskCancellation(cE, operand);

    assertNotNull(actual);
    assertNotNull(expected);
    assertEquals(expected, actual);
    assertNotSame(expected, actual);
  }

  protected abstract V newCancellationResult(final K operand);

  protected abstract CancellationException newCancellationException(final K operand);

  @Test
  public void testTimeoutHandling() {
    K operand = newOperand();
    TimeoutException tE = newTimeoutException(operand);

    assertNotNull(tE);

    V expected = newTimeoutResult(operand);
    V actual = taskHelper.handleTimeout(tE, operand);

    assertNotNull(actual);
    assertNotNull(expected);
    assertEquals(expected, actual);
    assertNotSame(expected, actual);
  }

  protected abstract V newTimeoutResult(final K operand);

  protected abstract TimeoutException newTimeoutException(final K operand);

}

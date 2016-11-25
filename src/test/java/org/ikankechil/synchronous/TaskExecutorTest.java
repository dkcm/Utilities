/**
 * TaskExecutorTest.java  v0.1  9 April 2009 8:00:24 PM
 *
 * Copyright © 2009-2010 Daniel Kuan.  All rights reserved.
 */
package org.ikankechil.synchronous;

import static org.ikankechil.synchronous.TaskExecutorTest.Behaviours.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit test for <code>TaskExecutor</code>.
 *
 * @author Daniel Kuan
 * @version
 */
public class TaskExecutorTest {

  private final TaskExecutor                         taskExecutor         = new TaskExecutor();
  private final List<Double>                         operands             = new ArrayList<>(NUMBER_OF_OPERANDS);
  private final Map<Future<Double>, Number>          futures              = new HashMap<>(NUMBER_OF_OPERANDS);
  private final NaturalLogarithmTaskHelper           taskHelper           = new NaturalLogarithmTaskHelper(NORMAL);
  protected final Map<Number, CancellationException> cancellations        = new HashMap<>();
  protected final Map<Number, ExecutionException>    failures             = new HashMap<>();
  protected final List<Double>                       expecteds            = new ArrayList<>(NUMBER_OF_OPERANDS);
  private final TaskHelperTest<Number, Double>       taskHelperTest       = new NaturalLogarithmTaskHelperTest();

  private static final int                           NUMBER_OF_OPERANDS   = (int) 1E3;
  private static final long                          TIME_OUT             = 1;
  private static final TimeUnit                      TIME_OUT_UNIT        = TimeUnit.NANOSECONDS;
  private static final double                        ZERO                 = 0.0;
  private static final double                        CANCELLED_PROPORTION = 1.0;

  @Before
  public void setUp() {
    for (int i = 0; i < NUMBER_OF_OPERANDS; ++i) {
      double operand = Math.random() * NUMBER_OF_OPERANDS;
      operands.add(operand);
      expecteds.add(Math.log(operand));
    }
  }

  @After
  public void tearDown() {
    operands.clear();
    futures.clear();
    cancellations.clear();
    failures.clear();
  }

  @Test
  public void testNullCollectionDisallowed() throws Exception {
    // null collection
    try {
      taskExecutor.newTasks(null, taskHelper);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.executeAll(null, taskHelper);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.executeAll(null, taskHelper, TIME_OUT, TIME_OUT_UNIT);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.executeAny(null, taskHelper);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.executeAny(null, taskHelper, TIME_OUT, TIME_OUT_UNIT);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.submitAll(null, taskHelper, futures);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.submitAll(operands, taskHelper, null);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.takeAll(null, null, taskHelper);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.takeAll(null, null, taskHelper, TIME_OUT, TIME_OUT_UNIT);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.takeAll(new ExecutorCompletionService<Double>(Executors.newSingleThreadExecutor()),
                           null,
                           taskHelper,
                           TIME_OUT,
                           null);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }

    try {
      Collection<Future<Double>> collection = null;
      taskExecutor.cancel(collection);
      fail("null collection");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("collection cannot be null", npE.getMessage());
    }
  }

  @Test
  public void testEmptyCollectionDisallowed() throws Exception {
    // empty collection
    Collection<Double> emptyOperands = Collections.emptyList();
    try {
      taskExecutor.newTasks(emptyOperands, taskHelper);
      fail("empty collection");
    }
    catch (IllegalArgumentException iaE) {
      assertEquals("collection cannot be empty", iaE.getMessage());
      assertTrue(emptyOperands.isEmpty());
    }

    try {
      taskExecutor.executeAll(emptyOperands, taskHelper);
      fail("empty collection");
    }
    catch (IllegalArgumentException iaE) {
      assertEquals("collection cannot be empty", iaE.getMessage());
      assertTrue(emptyOperands.isEmpty());
    }

    try {
      taskExecutor.executeAll(emptyOperands, taskHelper, TIME_OUT, TIME_OUT_UNIT);
      fail("empty collection");
    }
    catch (IllegalArgumentException iaE) {
      assertEquals("collection cannot be empty", iaE.getMessage());
      assertTrue(emptyOperands.isEmpty());
    }

    try {
      taskExecutor.executeAny(emptyOperands, taskHelper);
      fail("empty collection");
    }
    catch (IllegalArgumentException iaE) {
      assertEquals("collection cannot be empty", iaE.getMessage());
      assertTrue(emptyOperands.isEmpty());
    }

    try {
      taskExecutor.executeAny(emptyOperands, taskHelper, TIME_OUT, TIME_OUT_UNIT);
      fail("empty collection");
    }
    catch (IllegalArgumentException iaE) {
      assertEquals("collection cannot be empty", iaE.getMessage());
      assertTrue(emptyOperands.isEmpty());
    }

    try {
      taskExecutor.submitAll(emptyOperands, taskHelper, futures);
      fail("empty collection");
    }
    catch (IllegalArgumentException iaE) {
      assertEquals("collection cannot be empty", iaE.getMessage());
      assertTrue(emptyOperands.isEmpty());
    }
  }

  @Test
  public void testNullTaskHelperDisallowed() throws Exception {
    try {
      taskExecutor.newTasks(operands, null);
      fail("null task helper");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("task helper cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.executeAll(operands, null);
      fail("null task helper");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("task helper cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.executeAll(operands, null, TIME_OUT, TIME_OUT_UNIT);
      fail("null task helper");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("task helper cannot be null", npE.getMessage());
    }

    try {
      taskExecutor.submitAll(operands, null, futures);
      fail("null task helper");
    }
    catch (NullPointerException npE) {
      assertNull(npE.getMessage());
//      assertEquals("task helper cannot be null", npE.getMessage());
    }
  }

  @Test
  public void testTaskCreation() {
    List<Callable<Double>> tasks = taskExecutor.newTasks(operands, taskHelper);

    assertNotNull(tasks);
    assertFalse(tasks.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, tasks.size());
    assertNotNull(operands);
    assertFalse(operands.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, operands.size());
    assertNotNull(taskHelper);

    // check that tasks are of the correct class
    Iterator<Double> iterator = operands.iterator();
    for (Callable<Double> task : tasks) {
      assertTrue(task instanceof ComputeNaturalLogarithm);
      assertEquals(iterator.next(), ((ComputeNaturalLogarithm) task).getOperand(), ZERO);
    }
  }

  @Test
  public void testTaskCreationByTaskHelper() {
    taskHelperTest.testTaskCreation();
  }

  @Test
  public void testTaskExecutionAll() throws Exception {
    Map<Number, Double> results = taskExecutor.executeAll(operands, taskHelper);

    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, results.size());
    assertNotNull(operands);
    assertFalse(operands.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, operands.size());
    assertNotNull(taskHelper);

    // check that the order and values of the results are correct
    Iterator<Double> iterator = operands.iterator();
    for (Entry<Number, Double> result : results.entrySet()) {
      assertEquals(iterator.next(), result.getKey());
      assertEquals(Math.log(result.getKey().doubleValue()), result.getValue(), ZERO);
    }
  }

  @Test
  public void testTaskExecutionAny() throws Exception {
    Double result = taskExecutor.executeAny(operands, taskHelper);

    assertNotNull(result);
    assertNotNull(operands);
    assertFalse(operands.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, operands.size());
    assertNotNull(taskHelper);

    assertTrue(result >= Math.log(Collections.min(operands)));
    assertTrue(result <= Math.log(Collections.max(operands)));
  }

  @Test
  public void testTaskExecutionException() throws Exception {
    //
    Map<Number, Double> naughtyResults = taskExecutor.executeAll(operands,
                                                                 new NaturalLogarithmTaskHelper(THROW_EXCEPTION));

    assertNotNull(naughtyResults);
    assertFalse(naughtyResults.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, naughtyResults.size());

    for (Entry<Number, Double> naughtyResult : naughtyResults.entrySet()) {
      assertEquals(Double.NaN, naughtyResult.getValue(), ZERO);
      assertTrue(failures.containsKey(naughtyResult.getKey()));
    }

    //
    Double naughtyResult = taskExecutor.executeAny(operands,
                                                   new NaturalLogarithmTaskHelper(THROW_EXCEPTION));

    assertNotNull(naughtyResult);
    assertEquals(Double.NaN, naughtyResult, ZERO);
  }

  @Test
  public void testTaskExecutionCancellation() throws Exception {
    // tasks cancelled because time ran out
    Map<Number, Double> cancelledResults = taskExecutor.executeAll(operands,
                                                                   taskHelper,
                                                                   TIME_OUT,
                                                                   TIME_OUT_UNIT);

    assertNotNull(cancelledResults);
    assertFalse(cancelledResults.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, cancelledResults.size());

    // not all tasks were necessarily cancelled; some might have been completed
    // before time ran out
    for (Entry<Number, Double> cancelledResult : cancelledResults.entrySet()) {
      Double cancelledValue = cancelledResult.getValue();
      if (cancellations.containsKey(cancelledResult.getKey())) {
        assertTrue(String.valueOf(cancelledValue), Double.isNaN(cancelledValue));
      }
      else {
        assertFalse(String.valueOf(cancelledValue), Double.isNaN(cancelledValue));
        assertFalse(String.valueOf(cancelledValue), Double.isInfinite(cancelledValue));
      }
    }
  }

  @Test
  public void testTaskExecutionTimeout() throws Exception {
    Double timeoutResult = taskExecutor.executeAny(operands,
                                                   new NaturalLogarithmTaskHelper(SLOW),
                                                   TIME_OUT,
                                                   TIME_OUT_UNIT);

    assertNotNull(String.valueOf(timeoutResult), timeoutResult);
    assertTrue(String.valueOf(timeoutResult), Double.isNaN(timeoutResult));
  }

  @Ignore@Test
  public void testTaskExecutionDefaultTimeout() throws Exception {
    Double timeoutResult = taskExecutor.executeAny(operands,
                                                   new NaturalLogarithmTaskHelper(DEFAULT_TIMEOUT));

    assertNull(String.valueOf(timeoutResult), timeoutResult);
  }

  @Test
  public void testTaskSubmission() throws Exception {
    CompletionService<Double> service = taskExecutor.submitAll(operands, taskHelper, futures);

    assertNotNull(service);
    assertNotNull(futures);
    assertFalse(futures.isEmpty());

    List<Double> actuals = new ArrayList<>(operands.size());
    for (int i = 0; i < operands.size(); ++i) {
      Future<Double> future = service.take();
      assertNotNull(future);

      Double result = future.get();
      assertNotNull(result);
      assertFalse(result.isNaN());
      assertFalse(result.isInfinite());

      actuals.add(result);
    }

    assertNull(service.poll());

    // test accuracy
    Collections.sort(expecteds); // TODO do not sort!
    Collections.sort(actuals);
    assertArrayEquals(expecteds.toArray(), actuals.toArray());
  }

  @Test
  public void testMultipleTaskSubmissions() throws Exception {
    List<Double> firstHalf = operands.subList(0, NUMBER_OF_OPERANDS / 2);
    List<Double> secondHalf = operands.subList(NUMBER_OF_OPERANDS / 2, NUMBER_OF_OPERANDS);
    Map<Future<Double>, Number> futures1 = new HashMap<>(firstHalf.size());
    Map<Future<Double>, Number> futures2 = new HashMap<>(secondHalf.size());

    CompletionService<Double> service1 = taskExecutor.submitAll(firstHalf, taskHelper, futures1);
    CompletionService<Double> service2 = taskExecutor.submitAll(secondHalf, taskHelper, futures2);

    List<Double> results2 = taskExecutor.takeAll(service2, futures2, taskHelper);
    List<Double> results1 = taskExecutor.takeAll(service1, futures1, taskHelper);
  }

  @Test
  public void testResultRetrieval() throws Exception {
    CompletionService<Double> service = taskExecutor.submitAll(operands, taskHelper, futures);
    List<Double> results = taskExecutor.takeAll(service, futures, taskHelper);

    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, results.size());

    // test accuracy
    Collections.sort(expecteds);
    Collections.sort(results);
    assertArrayEquals(expecteds.toArray(), results.toArray());
  }

  @Test
  public void testResultRetrievalException() throws Exception {
    CompletionService<Double> service = taskExecutor.submitAll(operands,
                                                               new NaturalLogarithmTaskHelper(THROW_EXCEPTION),
                                                               futures);
    List<Double> naughtyResults = taskExecutor.takeAll(service, futures, taskHelper);

    assertNotNull(naughtyResults);
    assertFalse(naughtyResults.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, naughtyResults.size());

    for (double naughtyResult : naughtyResults) {
      assertEquals(Double.NaN, naughtyResult, ZERO);
    }
  }

  @Test
  public void testResultRetrievalCancellation() throws Exception {
    // overload single thread executor in order to force task cancellations
    CompletionService<Double> service =
      new ExecutorCompletionService<>(Executors.newSingleThreadExecutor());

    // cancel the tasks the moment they are created
    TaskHelper<Number,Double> slowTaskHelper = new NaturalLogarithmTaskHelper(SLOW);
    for (Double operand : operands) {
      Future<Double> future = service.submit(slowTaskHelper.newTask(operand));
      future.cancel(true);
      futures.put(future, operand);
    }

    List<Double> cancelledResults = taskExecutor.takeAll(service,
                                                         futures,
                                                         taskHelper,
                                                         TIME_OUT,
                                                         TIME_OUT_UNIT);

    assertNotNull(cancelledResults);
    assertFalse(cancelledResults.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, cancelledResults.size());

    for (double cancelledResult : cancelledResults) {
      assertEquals(Double.NaN, cancelledResult, ZERO);
    }

    assertEquals(NUMBER_OF_OPERANDS, cancellations.size(), ZERO);
  }

  @Test
  public void testCancelAfterSubmission() throws Exception {
    CompletionService<Double> service = taskExecutor.submitAll(operands,
                                                               new NaturalLogarithmTaskHelper(SLOW),
                                                               futures);

    taskExecutor.cancel(futures.keySet());

    List<Double> cancelledResults = taskExecutor.takeAll(service,
                                                         futures,
                                                         taskHelper,
                                                         TIME_OUT,
                                                         TIME_OUT_UNIT);

    assertNotNull(cancelledResults);
    assertFalse(cancelledResults.isEmpty());
    assertEquals(NUMBER_OF_OPERANDS, cancelledResults.size());

    int numberOfCancelledResults = 0;
    for (double cancelledResult : cancelledResults) {
      if (Double.isNaN(cancelledResult)) {
        ++numberOfCancelledResults;
      }
    }


    assertTrue(new StringBuilder("Cancelled: ").append(numberOfCancelledResults).toString(),
               numberOfCancelledResults >= (NUMBER_OF_OPERANDS * CANCELLED_PROPORTION));
  }

  @Test
  public void testExecutorStoppage() throws Exception {
    assertTrue(taskExecutor.stop());

    // executor should longer accept tasks after being stopped
    try {
      taskExecutor.executeAll(operands, taskHelper);
      fail("executor should no longer accept tasks after being stopped");
    }
    catch (RejectedExecutionException reE) {
      assertNotNull(reE.getMessage());
      assertNull(reE.getCause());
    }

    try {
      taskExecutor.submitAll(operands, taskHelper, futures);
      fail("executor should no longer accept tasks after being stopped");
    }
    catch (RejectedExecutionException reE) {
      assertNotNull(reE.getMessage());
      assertNull(reE.getCause());
    }

    // other task executors should not have problems accepting tasks
    Map<Number, Double> results = new TaskExecutor(Executors.newCachedThreadPool()).executeAll(operands, taskHelper);
    assertNotNull(results);
    assertFalse(results.isEmpty());

    // executor can still create tasks
    testTaskCreation();

    // repeated calls to stop have no effect.
    assertTrue(taskExecutor.stop());
  }

  @Test
  public void testExecutionFailureHandling() {
    taskHelperTest.testExecutionFailureHandling();
  }

  @Test
  public void testTaskCancellationHandling() {
    taskHelperTest.testTaskCancellationHandling();
  }

  @Test
  public void testTimeoutHandling() {
    taskHelperTest.testTimeoutHandling();
  }

  /**
   * Strategy enum.
   *
   * @author Daniel Kuan
   * @version
   */
  public enum Behaviours {
    NORMAL {
      @Override
      public Callable<Double> newTask(final double operand) {
        return new ComputeNaturalLogarithm(operand);
      }
    },
    THROW_EXCEPTION {
      @Override
      public Callable<Double> newTask(final double operand) {
        return new Callable<Double>() {
          @Override
          public Double call() throws Exception {
            throw new Exception(Double.toString(operand));
          }
        };
      }
    },
    DEFAULT_TIMEOUT {
      @Override
      public Callable<Double> newTask(final double operand) {
        return new ComputeNaturalLogarithm(operand) {
          @Override
          public Double call() {
            try {
              TimeUnit.MICROSECONDS.sleep(Integer.MAX_VALUE);
            }
            catch (InterruptedException iE) { /* do nothing */ }

            return super.call();
          }
        };
      }
    },
    SLOW {
      @Override
      public Callable<Double> newTask(final double operand) {
        return new ComputeNaturalLogarithm(operand) {
          @Override
          public Double call() {
            try {
              TimeUnit.SECONDS.sleep(TIME_OUT);
            }
            catch (InterruptedException iE) { /* do nothing */ }

            return super.call();
          }
        };
      }
    };

    public abstract Callable<Double> newTask(final double operand);

  }

  public class NaturalLogarithmTaskHelper implements TaskHelper<Number, Double> {

    private final Behaviours behaviour;

    public NaturalLogarithmTaskHelper(final Behaviours behaviour) {
      this.behaviour = behaviour;
    }

    @Override
    public Callable<Double> newTask(final Number operand) {
      return behaviour.newTask(operand.doubleValue());
    }

    @Override
    public Double handleExecutionFailure(final ExecutionException eE,
                                         final Number operand) {
      failures.put(operand, eE);
      return Double.NaN;
    }

    @Override
    public Double handleTaskCancellation(final CancellationException cE,
                                         final Number operand) {
      cancellations.put(operand, cE);
      return Double.NaN;
    }

    @Override
    public Double handleTimeout(final TimeoutException tE,
                                final Number operand) {
      return Double.NaN;
    }

  }

  public class NaturalLogarithmTaskHelperTest extends TaskHelperTest<Number, Double> {

    @Override
    protected TaskHelper<Number, Double> newTaskHelper() {
      return new NaturalLogarithmTaskHelper(TaskExecutorTest.Behaviours.NORMAL);
    }

    @Override
    protected Number newOperand() {
      return Math.random();
    }

    @Override
    protected Callable<Double> newTask(final Number operand) {
      return new ComputeNaturalLogarithm(operand.doubleValue());
    }

    @Override
    protected Double newFailureResult(final Number operand) {
      return Double.NaN;
    }

    @Override
    protected ExecutionException newExecutionException(final Number operand) {
      return new ExecutionException(new Throwable());
    }

    @Override
    protected Double newCancellationResult(final Number operand) {
      return Double.NaN;
    }

    @Override
    protected CancellationException newCancellationException(final Number operand) {
      return new CancellationException();
    }

    @Override
    protected Double newTimeoutResult(final Number operand) {
      return Double.NaN;
    }

    @Override
    protected TimeoutException newTimeoutException(final Number operand) {
      return new TimeoutException();
    }

  }

  public static class ComputeNaturalLogarithm implements Callable<Double> {

    private final double operand;

    public ComputeNaturalLogarithm(final double operand) {
      this.operand = operand;
    }

    @Override
    public Double call() {
      return Math.log(operand);
    }

    public final double getOperand() {
      return operand;
    }

  }

}

 // Copyright (C) 2001, 2002, 2003 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.console.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.NumberFormat;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import net.grinder.common.GrinderException;
 import net.grinder.common.Test;
 import net.grinder.console.common.ConsoleException;
 import net.grinder.statistics.CommonStatisticsViews;
 import net.grinder.statistics.ExpressionView;
 import net.grinder.statistics.PeakStatisticExpression;
 import net.grinder.statistics.StatisticExpression;
 import net.grinder.statistics.StatisticExpressionFactory;
 import net.grinder.statistics.StatisticsIndexMap;
 import net.grinder.statistics.StatisticsView;
 import net.grinder.statistics.TestStatistics;
 import net.grinder.statistics.TestStatisticsMap;
 import net.grinder.util.SignificantFigureFormat;
 
 
 /**
  * The console model.
  *
  * <p>This class uses synchronisation sparingly. In particular, when
  * notifying listeners of changes to the number of tests it sends
  * copies of the new index arrays. This helps because most listeners
  * are Swing dispatched and so can't gurantee the model is in a
  * reasonable state when they call back.</p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class Model {
 
   /** 
    * Constant that represents the model state of <em>waiting for a
    * trigger</em>.
    * @see #getState
    */
   public static final int STATE_WAITING_FOR_TRIGGER = 0;
 
   /** 
    * Constant that represents the model state of <em>stopped statisics
    * capture</em>.
    * @see #getState
    */
   public static final int STATE_STOPPED = 1;
 
   /**
    * Constant that represents the model state of <em>statisics capture
    * in progress</em>.
    * @see #getState
    */
   public static final int STATE_CAPTURING = 2;
 
   /** Time statistics capture was last started. */
   private long m_startTime;
 
   /** Time statistics capture was last stopped. */
   private long m_stopTime;
 
   /**
      * The current test set. A TreeSet is used to maintain the test
      * order. Should synchronise on <code>m_test</code> before
      * accessing it.
      **/
   private final Set m_tests = new TreeSet();
 
   /** 
    * A {@link SampleAccumulator} for each test.
    **/
   private final Map m_accumulators =
     Collections.synchronizedMap(new HashMap());
 
   private final SampleAccumulator m_totalSampleAccumulator;
 
   private final ConsoleProperties m_properties;
   private int m_sampleInterval;
   private NumberFormat m_numberFormat;
 
   private int m_state = 0;
   private long m_sampleCount = 0;
   private boolean m_receivedReport = false;
   private boolean m_receivedReportInLastInterval = false;
 
   /** Synchronise on m_modelListeners before accessing. */
   private final List m_modelListeners = new LinkedList();
 
   private final StatisticsIndexMap.LongIndex m_periodIndex;
   private final StatisticExpression m_tpsExpression;
   private final ExpressionView m_tpsExpressionView;
   private final PeakStatisticExpression m_peakTPSExpression;
   private final ExpressionView m_peakTPSExpressionView;
   private StatisticsView m_intervalStatisticsView;
   private StatisticsView m_cumulativeStatisticsView;
 
   private final ProcessStatusSet m_processStatusSet = new ProcessStatusSet();
 
   /**
    * System.currentTimeMillis() is expensive. This is accurate to one
    * sample interval.
    **/
   private long m_currentTime;
 
   /**
    * Creates a new <code>Model</code> instance.
    *
    * @param properties The console properties.
    * @exception GrinderException if an error occurs
    */
   public Model(ConsoleProperties properties) throws GrinderException {
 
     m_properties = properties;
 
     m_sampleInterval = m_properties.getSampleInterval();
     m_numberFormat =
       new SignificantFigureFormat(m_properties.getSignificantFigures());
 
     final StatisticsIndexMap indexMap = StatisticsIndexMap.getInstance();
 
     m_periodIndex = indexMap.getIndexForLong("period");
 
     final StatisticExpressionFactory statisticExpressionFactory =
       StatisticExpressionFactory.getInstance();
 
     m_tpsExpression =
       statisticExpressionFactory.createExpression(
 	"(* 1000 (/ (+ untimedTransactions timedTransactions) period))");
 	
     m_tpsExpressionView =
       new ExpressionView("TPS", "statistic.tps", m_tpsExpression);
 
     m_peakTPSExpression =
       statisticExpressionFactory.createPeak(
 	indexMap.getIndexForDouble("peakTPS"), m_tpsExpression);
 	
     m_peakTPSExpressionView =
       new ExpressionView("Peak TPS", "statistic.peakTPS", m_peakTPSExpression);
 
     m_totalSampleAccumulator =
       new SampleAccumulator(m_peakTPSExpression, m_periodIndex);
 
     createStatisticsViews();
 
     setState(STATE_WAITING_FOR_TRIGGER);
 
     new Thread(new Sampler()).start();
 
     m_properties.addPropertyChangeListener(
       ConsoleProperties.SIG_FIG_PROPERTY,
       new PropertyChangeListener() {
 	public void propertyChange(PropertyChangeEvent event) {
 	  m_numberFormat = 
 	    new SignificantFigureFormat(
 	      ((Integer)event.getNewValue()).intValue());
 	}
       });
 
     m_properties.addPropertyChangeListener(
       ConsoleProperties.SAMPLE_INTERVAL_PROPERTY,
       new PropertyChangeListener() {
 	public void propertyChange(PropertyChangeEvent event) {
 	  m_sampleInterval = ((Integer)event.getNewValue()).intValue();
 	}
       });
   }
 
   private final void createStatisticsViews() {
 
     final StatisticsView summaryStatisticsView =
       CommonStatisticsViews.getSummaryStatisticsView();
 
     m_intervalStatisticsView = new StatisticsView();
     m_intervalStatisticsView.add(summaryStatisticsView);
     m_intervalStatisticsView.add(m_tpsExpressionView);
 
     m_cumulativeStatisticsView = new StatisticsView();
     m_cumulativeStatisticsView.add(summaryStatisticsView);
     m_cumulativeStatisticsView.add(m_tpsExpressionView);
     m_cumulativeStatisticsView.add(m_peakTPSExpressionView);
   }
 
   /**
    * Get the ProcessStatusSet for this model.
    *
    * @return The <code>ProcessStatusSet</code>.
    */
   public final ProcessStatusSet getProcessStatusSet() {
     return m_processStatusSet;
   }
 
   /**
    * Get the expression view for TPS.
    *
    * @return The TPS expression view for this model.
    */
   public final ExpressionView getTPSExpressionView() {
     return m_tpsExpressionView;
   }
 
   /**
    * Get the expression for TPS.
    *
    * @return The TPS expression for this model.
    */
   public final StatisticExpression getTPSExpression() {
     return m_tpsExpression;
   }
 
   /**
    * Get the expression view for peak TPS.
    *
    * @return The peak TPS expression view for this model.
    */
   public final ExpressionView getPeakTPSExpressionView() {
     return m_peakTPSExpressionView;
   }
 
   /**
    * Get the expression for peak TPS.
    *
    * @return The peak TPS expression for this model.
    */
   public final StatisticExpression getPeakTPSExpression() {
     return m_peakTPSExpression;
   }
 
   /**
    * Register new tests.
    *
    * @param tests The new tests.
    */
   public final void registerTests(Collection tests) {
     // Need to copy collection, might be immutable.
     final HashSet newTests = new HashSet(tests);
 
     final Test[] testArray;
 
     synchronized (m_tests) {
       newTests.removeAll(m_tests);
 
       if (newTests.size() == 0) {
 	// No new tests.
 	return;
       }
 
       m_tests.addAll(newTests);
 
       // Create a index of m_tests sorted by test number.
       testArray = (Test[])m_tests.toArray(new Test[0]);
     }
 
     final SampleAccumulator[] accumulatorArray =
       new SampleAccumulator[testArray.length];
 
     final Iterator newTestIterator = newTests.iterator();
       
     synchronized (m_accumulators) {
       while (newTestIterator.hasNext()) {
 	m_accumulators.put((Test)newTestIterator.next(),
 			   new SampleAccumulator(m_peakTPSExpression,
 						 m_periodIndex));
       }
 
       for (int i=0; i<accumulatorArray.length; i++) {
 	accumulatorArray[i] =
 	  (SampleAccumulator)m_accumulators.get(testArray[i]);
       }
     }
 
     fireModelNewTests(newTests,
 		      new ModelTestIndex(testArray, accumulatorArray));
   }
 
   /**
    * Register new statisics views.
    *
    * @param intervalStatisticsView a <code>StatisticsView</code> value
    * @param cumulativeStatisticsView a <code>StatisticsView</code> value
    */
   public final void registerStatisticsViews(
     StatisticsView intervalStatisticsView,
     StatisticsView cumulativeStatisticsView) {
 
     // The StatisticsView objects are responsible for synchronisation.
     m_intervalStatisticsView.add(intervalStatisticsView);
     m_cumulativeStatisticsView.add(cumulativeStatisticsView);
 
     fireModelNewViews(intervalStatisticsView, cumulativeStatisticsView);
   }
 
   /**
    * Get the cumulative statistics for this model.
    *
    * @return The cumulative statistics.
    */
   public TestStatistics getTotalCumulativeStatistics() {
     return m_totalSampleAccumulator.getCumulativeStatistics();
   }
 
   /**
    * Get the cumulative statistics view for this model.
    *
    * @return The cumulative statistics view.
    */
   public final StatisticsView getCumulativeStatisticsView() {
     return m_cumulativeStatisticsView;
   }
 
   /**
    * Get the interval statistics view for this model.
    *
    * @return The interval statistics view.
    */
   public final StatisticsView getIntervalStatisticsView() {
     return m_intervalStatisticsView;
   }
 
   /**
    * Add a new model listener.
    *
    * @param listener The listener.
    */
   public final void addModelListener(ModelListener listener) {
     synchronized (m_modelListeners) {
       m_modelListeners.add(listener);
     }
   }
 
   /**
    * Add a new sample listener for the specific test.
    *
    * @param test The test to add the sample listener for.
    * @param listener The sample listener.
    */
   public final void addSampleListener(Test test, SampleListener listener) {
     ((SampleAccumulator)m_accumulators.get(test)).addSampleListener(listener);
   }
 
   /**
    * Add a new total sample listener.
    *
    * @param listener The sample listener.
    */
   public final void addTotalSampleListener(SampleListener listener) {
     m_totalSampleAccumulator.addSampleListener(listener);
   }
 
   private final void fireModelUpdate() {
     synchronized (m_modelListeners) {
       final Iterator iterator = m_modelListeners.iterator();
 
       while (iterator.hasNext()) {
 	final ModelListener listener = (ModelListener)iterator.next();
 	listener.update();
       }
     }
   }
 
   private final void fireModelNewTests(Set newTests,
 				       ModelTestIndex modelTestIndex) {
 
     synchronized (m_modelListeners) {
       final Iterator iterator = m_modelListeners.iterator();
 
       while (iterator.hasNext()) {
 	final ModelListener listener = (ModelListener)iterator.next();
 	listener.newTests(newTests, modelTestIndex);
       }
     }
   }
 
   private final void fireModelNewViews(
     StatisticsView intervalStatisticsView,
     StatisticsView cumulativeStatisticsView) {
 
     synchronized (m_modelListeners) {
       final Iterator iterator = m_modelListeners.iterator();
 
       while (iterator.hasNext()) {
 	final ModelListener listener = (ModelListener)iterator.next();
 	listener.newStatisticsViews(intervalStatisticsView,
 				    cumulativeStatisticsView);
       }
     }
   }
 
   private final void fireModelResetTestsAndStatisticsViews() {
 
     synchronized (m_modelListeners) {
       final Iterator iterator = m_modelListeners.iterator();
 
       while (iterator.hasNext()) {
 	final ModelListener listener = (ModelListener)iterator.next();
 	listener.resetTestsAndStatisticsViews();
       }
     }
   }
 
   /**
    * Reset the model.
    */
   public final void reset() {
 
     synchronized(m_tests) {
       m_tests.clear();
     }
 
     m_accumulators.clear();
     m_totalSampleAccumulator.zero();
 
     createStatisticsViews();
 
     fireModelResetTestsAndStatisticsViews();
   }
 
   /**
    * Start  the model.
    */
   public final void start() {
     setState(STATE_WAITING_FOR_TRIGGER);
     fireModelUpdate();
   }
 
   /**
    * Stop the model.
    */
   public final void stop() {
     setState(STATE_STOPPED);
     fireModelUpdate();
   }
 
   /**
    * Add a new test report.
    * @param testStatisticsMap The new test statistics.
    * @exception ConsoleException If an error occurs.
    */
   public final void addTestReport(TestStatisticsMap testStatisticsMap)
     throws ConsoleException {
 
     m_receivedReport = true;
 
     if (getState() == STATE_CAPTURING) {
       // TestStatisticsMap doesn't change within the console so we
       // don't need to synchronise.
       final TestStatisticsMap.Iterator iterator = 
 	testStatisticsMap.new Iterator();
 
       while (iterator.hasNext()) {
 	final TestStatisticsMap.Pair pair = iterator.next();
 
 	final TestStatistics statistics = pair.getStatistics();
 
 	final SampleAccumulator sampleAccumulator =
 	  (SampleAccumulator)m_accumulators.get(pair.getTest());
 
 	if (sampleAccumulator == null) {
 	  System.err.println("Ignoring unknown test: " + pair.getTest());
 	}
 	else {
 	  sampleAccumulator.add(statistics);
 	  m_totalSampleAccumulator.add(statistics);
 	}
       }
     }
   }
 
   /**
    * I've thought a couple of times about replacing this with a
    * java.util.TimerTask, and giving Model a Timer thread which
    * things like ProcessStatusSet could share. Its not as nice as it
    * first seems though because you have to deal with cancelling and
    * rescheduling the TimerTask when the sample period is changed.
    **/
   private final class Sampler implements Runnable {
     public void run() {
       while (true) {
 	m_currentTime = System.currentTimeMillis();
 
 	final long sampleInterval = m_sampleInterval;
 		
 	final long wakeUpTime = m_currentTime + sampleInterval;
 
 	while (m_currentTime < wakeUpTime) {
 	  try {
 	    Thread.sleep(wakeUpTime - m_currentTime);
 	    m_currentTime = wakeUpTime;
 	  }
 	  catch(InterruptedException e) {
 	    m_currentTime = System.currentTimeMillis();
 	  }
 	}
 
 	final int state = getState();
 
 	final long period =
 	  (state == STATE_STOPPED ? m_stopTime : m_currentTime) - m_startTime;
 
 	synchronized (m_accumulators) {
 	  final Iterator iterator = m_accumulators.values().iterator();
 
 	  while (iterator.hasNext()) {
 	    final SampleAccumulator sampleAccumulator = 
 	      ((SampleAccumulator)iterator.next());
 	    sampleAccumulator.fireSample(sampleInterval, period);
 	  }
 	}
 
 	m_totalSampleAccumulator.fireSample(sampleInterval, period);
 
 	if (m_receivedReport) {
 	  ++m_sampleCount;
 	  m_receivedReportInLastInterval = true;
 	}
 	else {
 	  m_receivedReportInLastInterval = false;
 	}
 		
 	if (state == STATE_CAPTURING) {
 	  if (m_receivedReport) {
 	    final int collectSampleCount =
 	      m_properties.getCollectSampleCount();
 
 	    if (collectSampleCount != 0 &&
 		m_sampleCount >= collectSampleCount) {
 	      setState(STATE_STOPPED);
 	    }
 	  }
 	}
 	else if (state == STATE_WAITING_FOR_TRIGGER) {
 	  if (m_sampleCount >= m_properties.getIgnoreSampleCount()) {
 	    setState(STATE_CAPTURING);
 	  }
 	}
 
 	fireModelUpdate();
 
 	m_receivedReport = false;
       }
     }
   }
 
   /**
    * Return the console properties for this model.
    *
    * @return The properties.
    */
   public final ConsoleProperties getProperties() {
     return m_properties;
   }
 
   /**
    * Get the number format for this model.
    *
    * @return The number format.
    */
   public final NumberFormat getNumberFormat() {
     return m_numberFormat;
   }
 
   /**
    * Get the current sample count.
    *
    * @return The sample count.
    */
   public final long getSampleCount() {
     return m_sampleCount;
   }
 
   /**
    * Whether or not a report was received in the last interval.
    * @return <code>true</code> => yes there was a report.
    */
   public final boolean getReceivedReport() {
     return m_receivedReportInLastInterval;
   }
 
   /**
    * Get the current model state.
    *
    * @return The model state.
    */
   public final int getState() {
     return m_state;
   }
 
   private final void zero() {
 
     synchronized (m_accumulators) {
       final Iterator iterator = m_accumulators.values().iterator();
       
       while (iterator.hasNext()) {
 	final SampleAccumulator sampleAccumulator =
 	  ((SampleAccumulator)iterator.next());
 	sampleAccumulator.zero();
       }
     }
 
     m_totalSampleAccumulator.zero();
 
     m_startTime = m_currentTime;
 
     fireModelUpdate();
   }
 
   private void setState(int i) {
     if (i != STATE_WAITING_FOR_TRIGGER &&
 	i != STATE_STOPPED &&
 	i != STATE_CAPTURING) {
       throw new IllegalArgumentException("Unknown state: " + i);
     }
 
     if (i == STATE_WAITING_FOR_TRIGGER) {
       // Zero everything because it looks pretty.
       zero();
     }
 
     if (i == STATE_CAPTURING) {
       zero();
     }
 
     if (m_state != STATE_STOPPED && i == STATE_STOPPED) {
       m_stopTime = m_currentTime;
     }
 
     m_sampleCount = 0;
     m_state = i;
   }
 }

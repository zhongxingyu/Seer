 // The Grinder
 // Copyright (C) 2000, 2001 Paco Gomez
 // Copyright (C) 2000, 2001 Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.console.model;
 
 import java.text.NumberFormat;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import net.grinder.common.GrinderException;
 import net.grinder.common.Test;
 import net.grinder.console.ConsoleException;
 import net.grinder.statistics.CumulativeStatistics;
 import net.grinder.statistics.IntervalStatistics;
 import net.grinder.statistics.StatisticsImplementation;
 import net.grinder.statistics.TestStatisticsMap;
 import net.grinder.util.SignificantFigureFormat;
 
 
 /**
  * The console model.
  *
  * <p>This class uses synchronisation sparingly, in particular it is
  * not used to protect accessor methods across a model structure
  * change. Instead clients should implement {@link ModelListener}, and
  * should not call any of the following methods in between receiving a
  * {@link ModelListener#reset} and a {@link ModelListener#update}.
  * <ul>
  * <li>{@link #getCumulativeStatistics}</li>
  * <li>{@link #getLastSampleStatistics}</li>
  * <li>{@link #getNumberOfTests}</li>
  * <li>{@link #getTest}</li>
  * </ul>
  * These methods will throw a {@link IllegalStateException} if called between a 
  * @link ModelListener#reset} and a {@link ModelListener#update}.
  * </p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class Model
 {
     public final static int STATE_WAITING_FOR_TRIGGER = 0;
     public final static int STATE_STOPPED = 1;
     public final static int STATE_CAPTURING = 2;
 
     private long m_startTime;
     private long m_stopTime;
 
     /**
      * The current test set. A TreeSet is used to maintain the test
      * order.
      **/
     private final Set m_tests = new TreeSet();
 
     /** A {@link SampleAccumulator} for each test. **/
     private final Map m_accumulators = new HashMap();
 
    /** Index into m_tests by test number. **/
     private Test[] m_testArray;
 
    /** Index into m_accumulators by test number. **/
     private SampleAccumulator[] m_accumulatorArray;
 
     /** true => m_testArray and m_accumulatorArray are valid. **/
     private boolean m_indicesValid = false;
 
     private final SampleAccumulator m_totalSampleAccumulator =
 	new SampleAccumulator();
 
     private ConsoleProperties m_properties;
     private int m_ignoreSampleCount;
     private int m_sampleInterval;
     private int m_significantFigures;
     private NumberFormat m_numberFormat;
 
     private boolean m_stopSampler = false;
     private int m_state = 0;
     private long m_sampleCount = 0;
     private boolean m_receivedReport = false;
     private final List m_modelListeners = new LinkedList();
 
     /**
      * System.currentTimeMillis is expensive. This is acurate to one
      * sample period.
      **/
     private long m_currentTime;
 
     public Model() throws GrinderException
     {
 	m_properties = new ConsoleProperties();
 	m_ignoreSampleCount = m_properties.getIgnoreSampleCount();
 	m_sampleInterval = m_properties.getSampleInterval();
 	m_significantFigures = m_properties.getSignificantFigures();
 	m_numberFormat = new SignificantFigureFormat(m_significantFigures);
 
 	m_testArray = new Test[0];
 	m_accumulatorArray = new SampleAccumulator[0];
 	m_indicesValid = true;
 
 	setInitialState();
 
 	new Thread(new Sampler()).start();
     }
 
     public synchronized void registerTests(Set newTests)
     {
 	newTests.removeAll(m_tests);
 
 	if (newTests.size() > 0) {
 	    m_tests.addAll(newTests);
 
 	    final Iterator newTestIterator = newTests.iterator();
 
 	    while (newTestIterator.hasNext()) {
 		m_accumulators.put((Test)newTestIterator.next(),
 				   new SampleAccumulator());
 	    }	
 
 	    fireModelReset(Collections.unmodifiableSet(newTests));
 
 	    m_indicesValid = false;
 
 	    m_testArray = (Test[])m_tests.toArray(new Test[0]);
 	    m_accumulatorArray = new SampleAccumulator[m_testArray.length];
 
 	    for (int i=0; i<m_accumulatorArray.length; i++) {
 		m_accumulatorArray[i] =
 		    (SampleAccumulator)m_accumulators.get(m_testArray[i]);
 	    }
 
 	    m_indicesValid = true;
 	}
     }
 
     /**
      * See note on sychronisation in {@link Model} class
      * description. 
      * @throws IllegalStateException if called when model structure is changing.
      **/
     public Test getTest(int testIndex)
     {
 	assertIndiciesValid();
 	return m_testArray[testIndex];
     }
 
     /**
      * See note on sychronisation in {@link Model} class
      * description. 
      * @throws IllegalStateException if called when model structure is changing.
      **/
     public int getNumberOfTests()
     {
 	assertIndiciesValid();
 	return m_testArray.length;
     }
 
     /**
      * See note on sychronisation in {@link Model} class
      * description. 
      * @throws IllegalStateException if called when model structure is changing.
      **/
     public CumulativeStatistics getCumulativeStatistics(int testIndex)
     {
 	assertIndiciesValid();
 	return m_accumulatorArray[testIndex];
     }
 
     public CumulativeStatistics getTotalCumulativeStatistics()
     {
 	return m_totalSampleAccumulator;
     }
 
     /**
      * See note on sychronisation in {@link Model} class
      * description. 
      * @throws IllegalStateException if called when model structure is changing.
      **/
     public IntervalStatistics getLastSampleStatistics(int testIndex)
     {
 	assertIndiciesValid();
 	return m_accumulatorArray[testIndex].getLastSampleStatistics();
     }
 
     public synchronized void addModelListener(ModelListener listener)
     {
 	m_modelListeners.add(listener);
     }
 
     public void addSampleListener(Test test, SampleListener listener)
     {
 	((SampleAccumulator)m_accumulators.get(test))
 	    .addSampleListener(listener);
     }
 
     public void addTotalSampleListener(SampleListener listener)
     {
 	m_totalSampleAccumulator.addSampleListener(listener);
     }
 
     private void assertIndiciesValid()
     {
 	if (!m_indicesValid) {
 	    throw new IllegalStateException("Invalid model state");
 	}
     }
 
     private synchronized void fireModelReset(Set newTests)
     {
 	final Iterator iterator = m_modelListeners.iterator();
 
 	while (iterator.hasNext()) {
 	    final ModelListener listener = (ModelListener)iterator.next();
 	    listener.reset(newTests);
 	}
     }
 
     private synchronized void fireModelUpdate()
     {
 	final Iterator iterator = m_modelListeners.iterator();
 
 	while (iterator.hasNext()) {
 	    final ModelListener listener = (ModelListener)iterator.next();
 	    listener.update();
 	}
     }
 
     private void setInitialState()
     {
 	if (m_ignoreSampleCount != 0) {
 	    setState(STATE_WAITING_FOR_TRIGGER);
 	}
 	else {
 	    setState(STATE_CAPTURING);
 	}
     }
 
     public void start()
     {
 	setInitialState();
 	fireModelUpdate();
     }
 
     public void stop()
     {
 	setState(STATE_STOPPED);
 	fireModelUpdate();
     }
 
     public void add(TestStatisticsMap testStatisticsMap)
 	throws ConsoleException
     {
 	m_receivedReport = true;
 
 	if (getState() == STATE_CAPTURING) {
 	    final TestStatisticsMap.Iterator iterator =
 		testStatisticsMap.new Iterator();
 
 	    while (iterator.hasNext()) {
 		final TestStatisticsMap.Pair pair = iterator.next();
 
 		final StatisticsImplementation statistics =
 		    pair.getStatistics();
 
 		final SampleAccumulator sampleAccumulator =
 		    (SampleAccumulator)m_accumulators.get(pair.getTest());
 
 		if (sampleAccumulator == null) {
 		    System.err.println("Ignoring unknown test: " +
 				       pair.getTest());
 		}
 		else {
 		    sampleAccumulator.add(statistics);
 		    m_totalSampleAccumulator.add(statistics);
 		}
 	    }
 	}
     }
 
     private class IntervalStatisticsImplementation
 	extends StatisticsImplementation implements IntervalStatistics
     {
 	public synchronized double getTPS()
 	{
 	    return 1000d*getTransactions()/(double)m_sampleInterval;
 	}
     }
 
     private class SampleAccumulator implements CumulativeStatistics
     {
 	private final List m_listeners = new LinkedList();
 	private IntervalStatisticsImplementation m_intervalStatistics =
 	    new IntervalStatisticsImplementation();
 	private IntervalStatistics m_lastSampleStatistics =
 	    new IntervalStatisticsImplementation();
 	private StatisticsImplementation m_total;
 	private double m_tps;
 	private double m_peakTPS;
 	
 	{
 	    reset();
 	}
 
 	private synchronized void addSampleListener(SampleListener listener)
 	{
 	    m_listeners.add(listener);
 	}
 
 	private void add(StatisticsImplementation report)
 	{
 	    m_intervalStatistics.add(report);
 	    m_total.add(report);
 	}
 
 	private synchronized void fireSample()
 	{
 	    final double tps = m_intervalStatistics.getTPS();
 
 	    if (tps > m_peakTPS) {
 		m_peakTPS = tps;
 	    }
 
 	    final double totalTime =
 		(getState() == STATE_STOPPED ? m_stopTime : m_currentTime) -
 		m_startTime;
 
 	    m_tps = 1000d * m_total.getTransactions()/totalTime;
 
 	    final Iterator iterator = m_listeners.iterator();
 
 	    while (iterator.hasNext()) {
 		final SampleListener listener =
 		    (SampleListener)iterator.next();
 		listener.update(m_intervalStatistics, this);
 	    }
 
 	    m_lastSampleStatistics = m_intervalStatistics;
 	    m_intervalStatistics = new IntervalStatisticsImplementation();
 	}
 
 	private void reset()
 	{
 	    m_intervalStatistics = new IntervalStatisticsImplementation();
 	    m_lastSampleStatistics = new IntervalStatisticsImplementation();
 	    m_tps = 0;
 	    m_peakTPS = 0;
 	    m_total = new StatisticsImplementation();
 	}
 
 	public double getAverageTransactionTime()
 	{
 	    return m_total.getAverageTransactionTime();
 	}
 
 	public long getTransactions()
 	{
 	    return m_total.getTransactions();
 	}
 
 	public long getErrors()
 	{
 	    return m_total.getErrors();
 	}
 
 	public double getTPS()
 	{
 	    return m_tps;
 	}
 
 	public double getPeakTPS()
 	{
 	    return m_peakTPS;
 	}
 
 	public IntervalStatistics getLastSampleStatistics()
 	{
 	    return m_lastSampleStatistics;
 	}
     }
 
     private class Sampler implements Runnable
     {
 	public void run()
 	{
 	    while (!m_stopSampler) {
 		m_currentTime = System.currentTimeMillis();
 		
 		final long wakeUpTime = m_currentTime + m_sampleInterval;
 
 		while (m_currentTime < wakeUpTime) {
 		    try {
 			Thread.sleep(wakeUpTime - m_currentTime);
 			m_currentTime = wakeUpTime;
 		    }
 		    catch(InterruptedException e) {
 			m_currentTime = System.currentTimeMillis();
 		    }
 		}
 
 		for (int i=0; i<m_accumulatorArray.length; i++) {
 		    m_accumulatorArray[i].fireSample();
 		}
 
 		m_totalSampleAccumulator.fireSample();
 
 		final int state = getState();
 
 		if (m_receivedReport) {
 		    ++m_sampleCount;
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
 
     public ConsoleProperties getProperties()
     {
 	return m_properties;
     }
 
     public void setProperties(ConsoleProperties properties)
     {
 	m_properties = properties;
 
 	final int significantFigures = m_properties.getSignificantFigures();
 
 	if (m_significantFigures != significantFigures) {
 	    m_numberFormat = new SignificantFigureFormat(significantFigures);
 	    m_significantFigures = significantFigures;
 	}
 
 	final int ignoreSampleCount = m_properties.getIgnoreSampleCount();
 
 	if (m_ignoreSampleCount != ignoreSampleCount) {
 	    if (getState() == STATE_WAITING_FOR_TRIGGER) {
 		setInitialState();
 	    }
 	    
 	    m_ignoreSampleCount = ignoreSampleCount;
 	}
 
 	// Should really wait until the next sample boundary before
 	// changing sample interval.
 	m_sampleInterval = m_properties.getSampleInterval();
 
 	fireModelUpdate();
     }
 
     public NumberFormat getNumberFormat()
     {
 	return m_numberFormat;
     }
 
     public long getSampleCount()
     {
 	return m_sampleCount;
     }
 
     /** Whether or not a report was received in the last period. */
     public boolean getReceivedReport()
     {
 	return m_receivedReport;
     }
 
     public int getState()
     {
 	return m_state;
     }
 
     private void reset()
     {
 	for (int i=0; i<m_accumulatorArray.length; i++) {
 	    m_accumulatorArray[i].reset();
 	}
 
 	m_totalSampleAccumulator.reset();
 
 	m_startTime = m_currentTime;
 
 	fireModelUpdate();
     }
 
     private void setState(int i)
     {
 	if (i != STATE_WAITING_FOR_TRIGGER &&
 	    i != STATE_STOPPED &&
 	    i != STATE_CAPTURING) {
 	    throw new IllegalArgumentException("Unknown state: " + i);
 	}
 
 	if (i == STATE_WAITING_FOR_TRIGGER) {
 	    reset();
 	}
 
 	if (i == STATE_CAPTURING) {
 	    reset();
 	}
 
 	if (m_state != STATE_STOPPED && i == STATE_STOPPED) {
 	    m_stopTime = m_currentTime;
 	}
 
 	m_state = i;
 	m_sampleCount = 0;
     }
 }

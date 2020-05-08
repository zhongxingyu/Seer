// Copyright (C) 2003, 2004, 2005 Philip Aston
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
 
 package net.grinder.engine.process;
 
 import java.io.PrintWriter;
 
 import net.grinder.common.GrinderException;
 import net.grinder.common.ThreadLifeCycleListener;
 import net.grinder.script.InvalidContextException;
 import net.grinder.script.Statistics;
 import net.grinder.script.StatisticsAlreadyReportedException;
 import net.grinder.statistics.CommonStatisticsViews;
 import net.grinder.statistics.ExpressionView;
 import net.grinder.statistics.StatisticExpression;
 import net.grinder.statistics.StatisticsIndexMap;
 import net.grinder.statistics.TestStatistics;
 import net.grinder.statistics.TestStatisticsFactory;
 
 
 
 /**
  * Implement the script statistics interface.
  *
  * <p>Package scope.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class ScriptStatisticsImplementation
   implements Statistics, ThreadLifeCycleListener {
 
   private static final StatisticsIndexMap.LongIndex s_errorsIndex;
   private static final StatisticsIndexMap.LongIndex s_timedTestsIndex;
   private static final StatisticsIndexMap.LongIndex s_untimedTestsIndex;
   private static final StatisticsIndexMap.LongIndex s_testTimeIndex;
 
   static {
     final StatisticsIndexMap indexMap = StatisticsIndexMap.getInstance();
 
     try {
       s_errorsIndex = indexMap.getIndexForLong("errors");
       s_timedTestsIndex =
         indexMap.getIndexForLong("timedTests");
       s_untimedTestsIndex =
         indexMap.getIndexForLong("untimedTests");
       s_testTimeIndex =
         indexMap.getIndexForLong("timedTestTime");
     }
     catch (GrinderException e) {
       throw new ExceptionInInitializerError(
         "Assertion failure, " +
         "ScriptStatisticsImplementation could not initialise: " +
         e.getMessage());
     }
   }
 
   private final ThreadContextLocator m_threadContextLocator;
   private final PrintWriter m_dataWriter;
   private final StringBuffer m_buffer = new StringBuffer();
   private final int m_bufferAfterThreadIDIndex;
   private final boolean m_recordTime;
   private final ExpressionView[] m_detailExpressionViews =
     CommonStatisticsViews.getDetailStatisticsView().getExpressionViews();
 
   private final TestStatistics m_testStatistics =
     TestStatisticsFactory.getInstance().create();
 
   private TestData m_currentTestData = null;
   private long m_currentTestStartTime = -1;
   private boolean m_noTests = true;
   private boolean m_delayReports = false;
   private int m_runNumber = -1;
   private int m_lastRunNumber = -1;
   private int m_bufferAfterRunNumberIndex = -1;
 
   public ScriptStatisticsImplementation(
     ThreadContextLocator threadContextLocator,
     PrintWriter dataWriter,
     int threadID,
     boolean recordTime) {
 
     m_threadContextLocator = threadContextLocator;
     m_dataWriter = dataWriter;
     m_recordTime = recordTime;
 
     m_buffer.append(threadID);
     m_buffer.append(", ");
     m_bufferAfterThreadIDIndex = m_buffer.length();
   }
 
   public void setDelayReports(boolean b) {
     if (!b) {
       reportInternal();
     }
 
     m_delayReports = b;
   }
 
   public void report() throws InvalidContextException {
     checkCallContext();
     reportInternal();
   }
 
   private void checkCallContext() throws InvalidContextException {
     final ThreadContext threadContext = m_threadContextLocator.get();
 
     if (threadContext == null) {
       throw new InvalidContextException(
         "Statistics interface is only supported for worker threads.");
     }
 
     if (threadContext.getScriptStatistics() != this) {
       throw new InvalidContextException(
         "Statistics objects must be used from the worker thread from" +
         "which they are acquired.");
     }
 
     if (m_noTests) {
       throw new InvalidContextException(
         "This worker thread has not yet performed any tests.");
     }
   }
 
   private void checkNotAlreadyReported()
     throws StatisticsAlreadyReportedException {
 
     if (m_currentTestData == null) {
       throw new StatisticsAlreadyReportedException(
         "The statistics for the last test performed by this thread have " +
         "already been reported. Perhaps you should have called " +
         "setDelayReports(true)?");
     }
   }
 
   public boolean availableForUpdate() {
     final ThreadContext threadContext = m_threadContextLocator.get();
 
     return
       threadContext != null &&
       threadContext.getScriptStatistics() == this &&
       m_currentTestData != null;
   }
 
   public void setValue(StatisticsIndexMap.LongIndex index, long value)
     throws InvalidContextException, StatisticsAlreadyReportedException {
 
     checkCallContext();
     checkNotAlreadyReported();
     m_testStatistics.setValue(index, value);
   }
 
   public void setValue(StatisticsIndexMap.DoubleIndex index, double value)
     throws InvalidContextException, StatisticsAlreadyReportedException {
 
     checkCallContext();
     checkNotAlreadyReported();
     m_testStatistics.setValue(index, value);
   }
 
   public void addValue(StatisticsIndexMap.LongIndex index, long value)
     throws InvalidContextException, StatisticsAlreadyReportedException {
 
     checkCallContext();
     checkNotAlreadyReported();
     m_testStatistics.addValue(index, value);
   }
 
   public void addValue(StatisticsIndexMap.DoubleIndex index, double value)
     throws InvalidContextException, StatisticsAlreadyReportedException {
 
     checkCallContext();
     checkNotAlreadyReported();
     m_testStatistics.addValue(index, value);
   }
 
   public long getValue(StatisticsIndexMap.LongIndex index) {
 
     return m_testStatistics.getValue(index);
   }
 
   public double getValue(StatisticsIndexMap.DoubleIndex index) {
 
     return m_testStatistics.getValue(index);
   }
 
   public void setSuccess(boolean success)
     throws InvalidContextException, StatisticsAlreadyReportedException {
 
     checkCallContext();
     checkNotAlreadyReported();
 
     if (success) {
       setSuccessNoChecks();
     }
     else {
       setErrorNoChecks();
     }
   }
 
   public boolean getSuccess() {
     return m_testStatistics.getErrors() == 0;
   }
 
   public long getTime() {
     return getValue(s_testTimeIndex);
   }
 
   void setSuccessNoChecks() {
 
     if (m_recordTime) {
       m_testStatistics.setValue(s_timedTestsIndex, 1);
       m_testStatistics.setValue(s_untimedTestsIndex, 0);
     }
     else {
       m_testStatistics.setValue(s_timedTestsIndex, 0);
       m_testStatistics.setValue(s_untimedTestsIndex, 1);
     }
 
     m_testStatistics.setValue(s_errorsIndex, 0);
   }
 
   void setErrorNoChecks() {
 
     m_testStatistics.setValue(s_untimedTestsIndex, 0);
     m_testStatistics.setValue(s_timedTestsIndex, 0);
     m_testStatistics.setValue(s_testTimeIndex, 0);
     m_testStatistics.setValue(s_errorsIndex, 1);
   }
 
   void setTimeNoChecks(long time) {
 
     if (m_recordTime) {
       m_testStatistics.setValue(s_testTimeIndex, time);
     }
   }
 
   void beginTest(TestData testData, int runNumber) {
 
     // Flush any pending report.
     reportInternal();
 
     m_currentTestData = testData;
     m_runNumber = runNumber;
     m_testStatistics.reset();
     m_noTests = false;
   }
 
   void endTest(long startTime) {
     m_currentTestStartTime = startTime;
 
     if (!m_delayReports) {
       reportInternal();
     }
   }
 
   public void beginRun() {
   }
 
   public void endRun() {
     reportInternal();
   }
 
   private void reportInternal() {
 
     if (m_currentTestData != null) {
       if (m_dataWriter != null) {
        if (m_runNumber == m_lastRunNumber &&
            m_lastRunNumber != -1) {
           m_buffer.setLength(m_bufferAfterRunNumberIndex);
         }
         else {
           m_lastRunNumber = m_runNumber;
 
           m_buffer.setLength(m_bufferAfterThreadIDIndex);
           m_buffer.append(m_runNumber);
           m_buffer.append(", ");
           m_bufferAfterRunNumberIndex = m_buffer.length();
         }
 
         m_buffer.append(m_currentTestData.getTest().getNumber());
 
         m_buffer.append(", ");
         m_buffer.append(m_currentTestStartTime);
 
         for (int i = 0; i < m_detailExpressionViews.length; ++i) {
           m_buffer.append(", ");
 
           final StatisticExpression expression =
             m_detailExpressionViews[i].getExpression();
 
           if (expression.isDouble()) {
             m_buffer.append(expression.getDoubleValue(m_testStatistics));
           }
           else {
             m_buffer.append(expression.getLongValue(m_testStatistics));
           }
         }
 
         m_dataWriter.println(m_buffer);
       }
 
       m_currentTestData.getStatistics().add(m_testStatistics);
       m_currentTestData = null;
     }
   }
 }
 

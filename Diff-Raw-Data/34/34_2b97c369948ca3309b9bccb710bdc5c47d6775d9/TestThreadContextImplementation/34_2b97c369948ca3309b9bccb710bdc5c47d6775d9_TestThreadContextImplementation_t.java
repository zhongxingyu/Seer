 // Copyright (C) 2006 Philip Aston
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
 import java.io.StringWriter;
 
 import net.grinder.common.FilenameFactory;
 import net.grinder.common.GrinderProperties;
 import net.grinder.common.SSLContextFactory;
 import net.grinder.common.StubTest;
 import net.grinder.common.Test;
 import net.grinder.script.Statistics.StatisticsForTest;
 import net.grinder.statistics.StatisticsServices;
 import net.grinder.statistics.StatisticsServicesImplementation;
 import net.grinder.statistics.StatisticsSet;
 import net.grinder.testutility.AssertUtilities;
 import net.grinder.testutility.RandomStubFactory;
 import junit.framework.TestCase;
 
 
 public class TestThreadContextImplementation extends TestCase {
   private final StatisticsServices m_statisticsServices =
     StatisticsServicesImplementation.getInstance();
 
   private final RandomStubFactory m_threadLoggerStubFactory =
     new RandomStubFactory(ThreadLogger.class);
   private final ThreadLogger m_threadLogger =
     (ThreadLogger) m_threadLoggerStubFactory.getStub();
 
   private final RandomStubFactory m_filenameFactoryStubFactory =
     new RandomStubFactory(FilenameFactory.class);
   private final FilenameFactory m_filenameFactory =
     (FilenameFactory)m_filenameFactoryStubFactory.getStub();
 
   private final ProcessContextStubFactory m_processContextStubFactory =
     new ProcessContextStubFactory();
   private final ProcessContext m_processContext =
     m_processContextStubFactory.getProcessContext();
 
   private final RandomStubFactory m_sslContextFactoryStubFactory =
     new RandomStubFactory(SSLContextFactory.class);
   private final SSLContextFactory m_sslContextFactory =
     (SSLContextFactory)m_sslContextFactoryStubFactory.getStub();
 
   private final RandomStubFactory m_dispatchContextStubFactory =
     new RandomStubFactory(DispatchContext.class);
   private final DispatchContext m_dispatchContext =
     (DispatchContext)m_dispatchContextStubFactory.getStub();
 
   public void setUp() {
     m_processContextStubFactory.setResult(
       "getStatisticsServices", m_statisticsServices);
   }
 
   public void testBasics() throws Exception {
     m_threadLoggerStubFactory.setResult("getThreadID", new Integer(13));
     m_threadLoggerStubFactory.setResult("getCurrentRunNumber", new Integer(2));
 
     final ThreadContext threadContext =
       new ThreadContextImplementation(
         m_processContext, m_threadLogger, m_filenameFactory, null);
 
     assertSame(m_threadLogger, threadContext.getThreadLogger());
     assertSame(m_filenameFactory, threadContext.getFilenameFactory());
     assertEquals(13, threadContext.getThreadID());
     assertEquals(2, threadContext.getRunNumber());
 
     assertNull(threadContext.getThreadSSLContextFactory());
     threadContext.setThreadSSLContextFactory(m_sslContextFactory);
     assertSame(m_sslContextFactory, threadContext.getThreadSSLContextFactory());
   }
 
   public void testDispatchResultReporter() throws Exception {
 
     final StringWriter dataStringWriter = new StringWriter();
 
     final ThreadContext threadContext =
       new ThreadContextImplementation(
         m_processContext, m_threadLogger, m_filenameFactory,
         new PrintWriter(dataStringWriter, true));
 
     final DispatchResultReporter dispatchResultReporter =
       threadContext.getDispatchResultReporter();
 
     final Test test = new StubTest(22, "test");
 
     final StatisticsSet statistics =
       m_statisticsServices.getStatisticsSetFactory().create();
 
     m_threadLoggerStubFactory.resetCallHistory();
 
     dispatchResultReporter.report(test, 123456, statistics);
 
     final String output = dataStringWriter.toString();
     AssertUtilities.assertContains(output, "22");
   }
 
   public void testNullDispatchResultReporter() throws Exception {
 
     final StringWriter dataStringWriter = new StringWriter();
 
     m_processContext.getProperties().setBoolean("grinder.logData", false);
 
     final ThreadContext threadContext =
       new ThreadContextImplementation(
         m_processContext, m_threadLogger, m_filenameFactory,
         new PrintWriter(dataStringWriter, true));
 
     final DispatchResultReporter dispatchResultReporter =
       threadContext.getDispatchResultReporter();
 
     final Test test = new StubTest(22, "test");
 
     final StatisticsSet statistics =
       m_statisticsServices.getStatisticsSetFactory().create();
 
     dispatchResultReporter.report(test, 123456, statistics);
 
     m_threadLoggerStubFactory.assertNoMoreCalls();
     assertEquals("", dataStringWriter.toString());
   }
 
   public void testDispatchContext() throws Exception {
     final ThreadContext threadContext =
       new ThreadContextImplementation(
         m_processContext, m_threadLogger, m_filenameFactory, null);
 
     assertNull(threadContext.getStatisticsForCurrentTest());
     assertNull(threadContext.getStatisticsForLastTest());
 
     try {
       threadContext.popDispatchContext();
       fail("Expected AssertionError");
     }
     catch (AssertionError e) {
     }
 
     final Test test = new StubTest(14, "test");
     m_dispatchContextStubFactory.setResult("getTest", test);
 
     m_threadLoggerStubFactory.resetCallHistory();
 
     final RandomStubFactory statisticsForTestStubFactory =
       new RandomStubFactory(StatisticsForTest.class);
     final StatisticsForTest statisticsForTest =
       (StatisticsForTest)statisticsForTestStubFactory.getStub();
     m_dispatchContextStubFactory.setResult(
       "getStatisticsForTest", statisticsForTest);
 
     threadContext.pushDispatchContext(m_dispatchContext);
 
     m_dispatchContextStubFactory.assertSuccess("getTest");
     m_dispatchContextStubFactory.assertNoMoreCalls();
 
     assertSame(statisticsForTest, threadContext.getStatisticsForCurrentTest());
     assertNull(threadContext.getStatisticsForLastTest());
 
     m_threadLoggerStubFactory.assertSuccess(
       "setCurrentTestNumber", new Integer(14));
 
     m_dispatchContextStubFactory.assertSuccess("getStatisticsForTest");
     m_dispatchContextStubFactory.assertNoMoreCalls();
 
     threadContext.popDispatchContext();
 
     m_dispatchContextStubFactory.assertSuccess("getStatisticsForTest");
     m_dispatchContextStubFactory.assertSuccess("report");
     m_dispatchContextStubFactory.assertNoMoreCalls();
     assertSame(statisticsForTest, threadContext.getStatisticsForLastTest());
     assertNull(threadContext.getStatisticsForCurrentTest());
 
     final RandomStubFactory anotherDispatchContextStubFactory =
       new RandomStubFactory(DispatchContext.class);
     final DispatchContext anotherDispatchContext =
       (DispatchContext)anotherDispatchContextStubFactory.getStub();
     final Test test2 = new StubTest(3, "another test");
     anotherDispatchContextStubFactory.setResult("getTest", test2);
 
     final RandomStubFactory stopWatchStubFactory =
       new RandomStubFactory(StopWatch.class);
     final StopWatch stopWatch =
       (StopWatch)stopWatchStubFactory.getStub();
     anotherDispatchContextStubFactory.setResult("getPauseTimer", stopWatch);
 
     threadContext.pushDispatchContext(anotherDispatchContext);
     threadContext.pushDispatchContext(m_dispatchContext);
     m_dispatchContextStubFactory.resetCallHistory();
     anotherDispatchContextStubFactory.resetCallHistory();
 
     threadContext.popDispatchContext();
     m_dispatchContextStubFactory.assertSuccess("getPauseTimer");
     m_dispatchContextStubFactory.assertSuccess("getStatisticsForTest");
     m_dispatchContextStubFactory.assertSuccess("report");
     m_dispatchContextStubFactory.assertNoMoreCalls();
     anotherDispatchContextStubFactory.assertSuccess("getPauseTimer");
     anotherDispatchContextStubFactory.assertNoMoreCalls();
     stopWatchStubFactory.assertSuccess("add", StopWatch.class);
     stopWatchStubFactory.assertNoMoreCalls();
 
     threadContext.pauseClock();
     anotherDispatchContextStubFactory.assertSuccess("getPauseTimer");
     stopWatchStubFactory.assertSuccess("start");
 
     threadContext.resumeClock();
     anotherDispatchContextStubFactory.assertSuccess("getPauseTimer");
     stopWatchStubFactory.assertSuccess("stop");
 
     threadContext.popDispatchContext();
     m_dispatchContextStubFactory.assertNoMoreCalls();
     anotherDispatchContextStubFactory.assertSuccess("getStatisticsForTest");
     anotherDispatchContextStubFactory.assertSuccess("report");
     anotherDispatchContextStubFactory.assertNoMoreCalls();
 
     threadContext.pauseClock();
     threadContext.resumeClock();
     anotherDispatchContextStubFactory.assertNoMoreCalls();
 
     threadContext.beginRunEvent();
     anotherDispatchContextStubFactory.assertNoMoreCalls();
     threadContext.endRunEvent();
     anotherDispatchContextStubFactory.assertNoMoreCalls();
 
     try {
       threadContext.popDispatchContext();
       fail("Expected AssertionError");
     }
     catch (AssertionError e) {
     }
   }
 
   public void testDelayReports() throws Exception {
     final ThreadContext threadContext =
       new ThreadContextImplementation(
         m_processContext, m_threadLogger, m_filenameFactory, null);
 
     final Test test = new StubTest(14, "test");
     m_dispatchContextStubFactory.setResult("getTest", test);
 
     threadContext.pushDispatchContext(m_dispatchContext);
     m_dispatchContextStubFactory.assertSuccess("getTest");
 
     threadContext.setDelayReports(false);
     m_dispatchContextStubFactory.assertNoMoreCalls();
 
     threadContext.setDelayReports(true);
     m_dispatchContextStubFactory.assertNoMoreCalls();
 
    assertNull(threadContext.getStatisticsForLastTest());

    assertNotNull(threadContext.getStatisticsForCurrentTest());
    m_dispatchContextStubFactory.assertSuccess("getStatisticsForTest");
    m_dispatchContextStubFactory.assertNoMoreCalls();

     threadContext.popDispatchContext();
    m_dispatchContextStubFactory.assertSuccess("getStatisticsForTest");
     m_dispatchContextStubFactory.assertNoMoreCalls();
 
    assertNotNull(threadContext.getStatisticsForLastTest());
    assertNull(threadContext.getStatisticsForCurrentTest());

     // Now have a pending context.
 
     threadContext.reportPendingDispatchContext();
     m_dispatchContextStubFactory.assertSuccess("report");
     threadContext.reportPendingDispatchContext();
     m_dispatchContextStubFactory.assertNoMoreCalls();
 
     // Test flushed at end of run.
     threadContext.pushDispatchContext(m_dispatchContext);
     m_dispatchContextStubFactory.assertSuccess("getTest");
     threadContext.popDispatchContext();
    m_dispatchContextStubFactory.assertSuccess("getStatisticsForTest");
     threadContext.beginRunEvent();
     m_dispatchContextStubFactory.assertNoMoreCalls();
     threadContext.endRunEvent();
     m_dispatchContextStubFactory.assertSuccess("report");
     m_dispatchContextStubFactory.assertNoMoreCalls();
 
     // Test flushed if delay reports is turned off.
     threadContext.pushDispatchContext(m_dispatchContext);
     m_dispatchContextStubFactory.assertSuccess("getTest");
     threadContext.popDispatchContext();
    m_dispatchContextStubFactory.assertSuccess("getStatisticsForTest");
     m_dispatchContextStubFactory.assertNoMoreCalls();
     threadContext.setDelayReports(false);
     m_dispatchContextStubFactory.assertSuccess("report");
     m_dispatchContextStubFactory.assertNoMoreCalls();
   }
 
   public static final class ProcessContextStubFactory
     extends RandomStubFactory {
 
     private GrinderProperties m_properties = new GrinderProperties();
 
     public ProcessContextStubFactory() {
       super(ProcessContext.class);
     }
 
     public ProcessContext getProcessContext() {
       return (ProcessContext)getStub();
     }
 
     public GrinderProperties override_getProperties(Object proxy) {
       return m_properties;
     }
   }
 }

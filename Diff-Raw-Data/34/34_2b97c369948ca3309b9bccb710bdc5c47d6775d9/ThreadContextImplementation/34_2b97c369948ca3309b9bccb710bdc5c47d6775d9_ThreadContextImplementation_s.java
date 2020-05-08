 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000 - 2006 Philip Aston
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
 import java.util.ArrayList;
 import java.util.List;
 
 import net.grinder.common.FilenameFactory;
 import net.grinder.common.SSLContextFactory;
 import net.grinder.common.Test;
 import net.grinder.common.ThreadLifeCycleListener;
 import net.grinder.engine.common.EngineException;
 import net.grinder.engine.process.DispatchContext.DispatchStateException;
 import net.grinder.plugininterface.PluginThreadContext;
 import net.grinder.script.Statistics.StatisticsForTest;
 import net.grinder.statistics.StatisticsSet;
 import net.grinder.util.ListenerSupport;
 import net.grinder.util.ListenerSupport.Informer;
 
 
 /**
  * Package scope.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class ThreadContextImplementation
   implements ThreadContext, PluginThreadContext {
 
   private final ListenerSupport m_threadLifeCycleListeners =
     new ListenerSupport();
 
   private final DispatchContextStack m_dispatchContextStack =
     new DispatchContextStack();
 
   private final ProcessContext m_processContext;
   private final ThreadLogger m_threadLogger;
   private final FilenameFactory m_filenameFactory;
   private final DispatchResultReporter m_dispatchResultReporter;
 
   private SSLContextFactory m_sslContextFactory;
 
   private boolean m_delayReports;
 
   private DispatchContext m_pendingDispatchContext;
 
   private StatisticsForTest m_statisticsForLastTest;
 
   public ThreadContextImplementation(ProcessContext processContext,
                                      ThreadLogger threadLogger,
                                      FilenameFactory filenameFactory,
                                      PrintWriter dataWriter)
     throws EngineException {
 
     m_processContext = processContext;
     m_threadLogger = threadLogger;
     m_filenameFactory = filenameFactory;
 
     // Undocumented property. Added so Tom Barnes can investigate overhead
     // of data logging.
     if (processContext.getProperties().getBoolean("grinder.logData", true)) {
       final ThreadDataWriter threadDataWriter =
         new ThreadDataWriter(
           dataWriter,
           processContext.getStatisticsServices()
           .getDetailStatisticsView().getExpressionViews(),
           m_threadLogger.getThreadID());
 
       m_dispatchResultReporter = new DispatchResultReporter() {
         public void report(Test test,
                            long startTime,
                            StatisticsSet statistics) {
           threadDataWriter.report(
             getRunNumber(),
             test,
             startTime - m_processContext.getExecutionStartTime(),
             statistics);
         }
       };
     }
     else {
       m_dispatchResultReporter = new DispatchResultReporter() {
         public void report(Test test,
                            long startTime,
                            StatisticsSet statistics) {
           // Null reporter.
         }
       };
     }
 
 
     registerThreadLifeCycleListener(
       new ThreadLifeCycleListener() {
         public void beginRun() { }
         public void endRun() { reportPendingDispatchContext(); }
       });
   }
 
   public FilenameFactory getFilenameFactory() {
     return m_filenameFactory;
   }
 
   public int getThreadID() {
     return m_threadLogger.getThreadID();
   }
 
   public int getRunNumber() {
     return m_threadLogger.getCurrentRunNumber();
   }
 
   public ThreadLogger getThreadLogger() {
     return m_threadLogger;
   }
 
   public SSLContextFactory getThreadSSLContextFactory() {
     return m_sslContextFactory;
   }
 
   public void setThreadSSLContextFactory(SSLContextFactory sslContextFactory) {
     m_sslContextFactory = sslContextFactory;
   }
 
   public DispatchResultReporter getDispatchResultReporter() {
     return m_dispatchResultReporter;
   }
 
   public void registerThreadLifeCycleListener(
     ThreadLifeCycleListener listener) {
     m_threadLifeCycleListeners.add(listener);
   }
 
   public void beginRunEvent() {
     m_threadLifeCycleListeners.apply(new Informer() {
       public void inform(Object listener) {
         ((ThreadLifeCycleListener)listener).beginRun();
       } }
     );
   }
 
   public void endRunEvent() {
     m_threadLifeCycleListeners.apply(new Informer() {
       public void inform(Object listener) {
         ((ThreadLifeCycleListener)listener).endRun();
       } }
     );
   }
 
   public void pushDispatchContext(DispatchContext dispatchContext)
     throws ShutdownException {
 
     m_processContext.checkIfShutdown();
 
     getThreadLogger().setCurrentTestNumber(
       dispatchContext.getTest().getNumber());
 
     m_dispatchContextStack.push(dispatchContext);
   }
 
   public void popDispatchContext() {
     final DispatchContext dispatchContext = m_dispatchContextStack.pop();
 
     if (dispatchContext == null) {
       throw new AssertionError("DispatchContext stack unexpectedly empty");
     }
 
     final DispatchContext parentDispatchContext =
       m_dispatchContextStack.peekTop();
 
     if (parentDispatchContext != null) {
       parentDispatchContext.getPauseTimer().add(
         dispatchContext.getPauseTimer());
     }
 
     if (m_delayReports) {
       reportPendingDispatchContext();
       m_pendingDispatchContext = dispatchContext;
     }
     else {
       try {
        m_statisticsForLastTest = dispatchContext.getStatisticsForTest();
         dispatchContext.report();
       }
       catch (DispatchStateException e) {
         throw new AssertionError(e);
       }
     }
   }
 
   public StatisticsForTest getStatisticsForCurrentTest() {
      final DispatchContext dispatchContext = m_dispatchContextStack.peekTop();
 
      if (dispatchContext == null) {
        return null;
      }
 
      return dispatchContext.getStatisticsForTest();
   }
 
   public StatisticsForTest getStatisticsForLastTest() {
     return m_statisticsForLastTest;
   }
 
   public void setDelayReports(boolean b) {
     if (!b) {
       reportPendingDispatchContext();
     }
 
     m_delayReports = b;
   }
 
   public void reportPendingDispatchContext() {
     if (m_pendingDispatchContext != null) {
       try {
        m_statisticsForLastTest =
          m_pendingDispatchContext.getStatisticsForTest();
         m_pendingDispatchContext.report();
       }
       catch (DispatchStateException e) {
         throw new AssertionError(e);
       }
 
       m_pendingDispatchContext = null;
     }
   }
 
   public void pauseClock() {
     final DispatchContext dispatchContext = m_dispatchContextStack.peekTop();
 
     if (dispatchContext != null) {
       dispatchContext.getPauseTimer().start();
     }
   }
 
   public void resumeClock() {
     final DispatchContext dispatchContext = m_dispatchContextStack.peekTop();
 
     if (dispatchContext != null) {
       dispatchContext.getPauseTimer().stop();
     }
   }
 
   private static final class DispatchContextStack {
     private List m_stack = new ArrayList();
 
     public void push(DispatchContext dispatchContext) {
       m_stack.add(dispatchContext);
     }
 
     public DispatchContext pop() {
       final int size = m_stack.size();
 
       if (size == 0) {
         return null;
       }
 
       return (DispatchContext)m_stack.remove(size - 1);
     }
 
     public DispatchContext peekTop() {
       final int size = m_stack.size();
 
       if (size == 0) {
         return null;
       }
 
       return (DispatchContext)m_stack.get(size - 1);
     }
   }
 }

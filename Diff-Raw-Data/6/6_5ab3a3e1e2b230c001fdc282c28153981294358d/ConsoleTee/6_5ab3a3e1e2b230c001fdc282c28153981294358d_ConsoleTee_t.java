 package no.ebakke.studycaster.util.stream;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Logger;
 
 public class ConsoleTee {
   private final OutputStream out;
   private final PrintStream oldStdOut, oldStdErr;
   private final TeePrintStream teeStdOut, teeStdErr;
   private final ConsoleHandler oldLogHandler, newLogHandler;
   private final AtomicBoolean closed = new AtomicBoolean(false);
 
   public ConsoleTee(OutputStream out, Formatter logFormatter) {
     this.out = out;
     oldStdOut = System.out;
     oldStdErr = System.err;
     Logger globalLogger = Logger.getLogger("");
     ConsoleHandler oldLogHandlerFound = null;
     for (Handler h : globalLogger.getHandlers()) {
       if (h instanceof ConsoleHandler) {
         oldLogHandlerFound = (ConsoleHandler) h;
         globalLogger.removeHandler(oldLogHandlerFound);
         break;
       }
     }
     oldLogHandler = oldLogHandlerFound;
     teeStdOut = new TeePrintStream(out, System.out);
     teeStdErr = new TeePrintStream(out, System.err);
     System.setOut(teeStdOut);
     System.setErr(teeStdErr);
     newLogHandler = new ConsoleHandler();
     newLogHandler.setFormatter(logFormatter);
     globalLogger.addHandler(newLogHandler);
   }
 
   public void close() throws IOException {
     if (closed.getAndSet(true))
       return;
     Logger globalLogger = Logger.getLogger("");
     globalLogger.removeHandler(newLogHandler);
     System.setOut(oldStdOut);
     System.setErr(oldStdErr);
     if (oldLogHandler != null)
       globalLogger.addHandler(oldLogHandler);
    /* A rare bug was once seen where a logger would flush the underlying stream after it had been
    closed; the flushes below attempt to avoid this condition, and should probably be there in any
    case. */
    teeStdOut.flush();
    teeStdErr.flush();
    newLogHandler.flush();
     out.close();
   }
 }

 package org.rubyforge.debugcommons;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 import org.rubyforge.debugcommons.model.ExceptionSuspensionPoint;
 import org.rubyforge.debugcommons.model.IRubyBreakpoint;
 import org.rubyforge.debugcommons.model.IRubyExceptionBreakpoint;
 import org.rubyforge.debugcommons.model.IRubyLineBreakpoint;
 import org.rubyforge.debugcommons.model.SuspensionPoint;
 import org.rubyforge.debugcommons.model.RubyThreadInfo;
 import org.rubyforge.debugcommons.model.RubyDebugTarget;
 import org.rubyforge.debugcommons.model.RubyFrame;
 import org.rubyforge.debugcommons.model.RubyFrameInfo;
 import org.rubyforge.debugcommons.model.RubyThread;
 import org.rubyforge.debugcommons.model.RubyVariable;
 import org.rubyforge.debugcommons.model.RubyVariableInfo;
 
 public final class RubyDebuggerProxy {
 
     public static enum DebuggerType { CLASSIC_DEBUGGER, RUBY_DEBUG }
     
     public static final DebuggerType CLASSIC_DEBUGGER = DebuggerType.CLASSIC_DEBUGGER;
     public static final DebuggerType RUBY_DEBUG = DebuggerType.RUBY_DEBUG;
     
     public static final List<RubyDebuggerProxy> PROXIES = new CopyOnWriteArrayList<RubyDebuggerProxy>();
     
     private final List<RubyDebugEventListener> listeners;
     private final Map<Integer, IRubyLineBreakpoint> breakpointsIDs;
     private final int timeout;
     
     private final DebuggerType debuggerType;
     private RubyDebugTarget debugTarged;
     private Socket commandSocket;
     private boolean connected;
     private boolean finished;
     
     private PrintWriter commandWriter;
     private RubyLoop rubyLoop;
     private ICommandFactory commandFactory;
     private ReadersSupport readersSupport;
     
     private boolean supportsCondition;
     
     // catchpoint removing is not supported by backend yet, handle it in the
     // debug-commons-java until the support is added
     // http://rubyforge.org/tracker/index.php?func=detail&aid=20237&group_id=1900&atid=7436
     private Set<String> removedCatchpoints;
     
     public RubyDebuggerProxy(final DebuggerType debuggerType) {
         this(debuggerType, 10); // default reading timeout 10s
     }
     
     public RubyDebuggerProxy(final DebuggerType debuggerType, final int timeout) {
         this.debuggerType = debuggerType;
         this.listeners = new CopyOnWriteArrayList<RubyDebugEventListener>();
         this.breakpointsIDs = new HashMap<Integer, IRubyLineBreakpoint>();
         this.removedCatchpoints = new HashSet<String>();
         this.timeout = timeout;
     }
     
     public void connect(RubyDebugTarget debugTarged) throws IOException, RubyDebuggerException {
         this.debugTarged = debugTarged;
         this.readersSupport = new ReadersSupport(timeout);
     }
     
     public RubyDebugTarget getDebugTarged() {
         return debugTarged;
     }
     
     /** <b>Package-private</b> for unit tests only. */
     ReadersSupport getReadersSupport() {
         return readersSupport;
     }
     
     /**
      * Set initial breakpoints and start the debugging process stopping (and
      * firing event to the {@link #addRubyDebugEventListener}) on the first
      * breakpoint.
      *
      * @param initialBreakpoints initial set of breakpoints to be set before
      *        triggering the debugging
      */
     public void startDebugging(final IRubyBreakpoint[] initialBreakpoints) throws RubyDebuggerException {
         try {
             switch(debuggerType) {
             case CLASSIC_DEBUGGER:
                 startClassicDebugger(initialBreakpoints);
                 break;
             case RUBY_DEBUG:
                 startRubyDebug(initialBreakpoints);
                 break;
             default:
                 throw new IllegalStateException("Unhandled debugger type: " + debuggerType);
             }
         } catch (RubyDebuggerException e) {
             PROXIES.remove(this);
             throw e;
         }
         startRubyLoop();
     }
     
     public synchronized boolean checkConnection() {
         return !finished && connected;
     }
 
     private void startClassicDebugger(final IRubyBreakpoint[] initialBreakpoints) throws RubyDebuggerException {
         try {
             commandFactory = new ClassicDebuggerCommandFactory();
             readersSupport.startCommandLoop(getCommandSocket().getInputStream());
             setBreakpoints(initialBreakpoints);
             sendCommand("cont");
         } catch (IOException ex) {
             throw new RubyDebuggerException(ex);
         }
     }
 
     private void startRubyDebug(final IRubyBreakpoint[] initialBreakpoints) throws RubyDebuggerException {
         try {
             commandFactory = new RubyDebugCommandFactory();
             readersSupport.startCommandLoop(getCommandSocket().getInputStream());
             setBreakpoints(initialBreakpoints);
             sendCommand("start");
         } catch (IOException ex) {
             throw new RubyDebuggerException(ex);
         }
     }
     
     public void fireDebugEvent(final RubyDebugEvent e) {
         for (RubyDebugEventListener listener : listeners) {
             listener.onDebugEvent(e);
         }
     }
     
     public void addRubyDebugEventListener(final RubyDebugEventListener listener) {
         listeners.add(listener);
     }
     
     public void removeRubyDebugEventListener(final RubyDebugEventListener listener) {
         listeners.remove(listener);
     }
     
     private synchronized PrintWriter getCommandWriter() throws RubyDebuggerException {
         if (commandWriter == null) {
             try {
                 commandWriter = new PrintWriter(getCommandSocket().getOutputStream(), true);
                 connected = true;
             } catch (IOException e) {
                 throw new RubyDebuggerException(e);
             }
         }
         return commandWriter;
     }
     
     protected void setBreakpoints(final IRubyBreakpoint[] breakpoints) throws RubyDebuggerException {
         for (IRubyBreakpoint breakpoint: breakpoints) {
             addBreakpoint(breakpoint);
         }
     }
     
     public void addBreakpoint(final IRubyBreakpoint breakpoint) throws RubyDebuggerException {
         if (breakpoint.isEnabled()) {
             if (breakpoint instanceof IRubyLineBreakpoint) {
                 IRubyLineBreakpoint lineBreakpoint = (IRubyLineBreakpoint) breakpoint;
                 String command = commandFactory.createAddBreakpoint(
                         lineBreakpoint.getFilePath(), lineBreakpoint.getLineNumber());
                 sendCommand(command);
                 Integer id = getReadersSupport().readAddedBreakpointNo();
                 String condition = lineBreakpoint.getCondition();
                 if (condition != null && supportsCondition) {
                     command = commandFactory.createSetCondition(id, condition);
                     if (command != null) {
                         sendCommand(command);
                         getReadersSupport().readConditionSet(); // read response
                     } else {
                         Util.info("conditional breakpoints are not supported by backend");
                     }
                 }
                 breakpointsIDs.put(id, lineBreakpoint);
             } else if (breakpoint instanceof IRubyExceptionBreakpoint) {
                 IRubyExceptionBreakpoint excBreakpoint = (IRubyExceptionBreakpoint) breakpoint;
                 // just 're-enable' if contained in removedCatchpoints
                 if (!removedCatchpoints.remove(excBreakpoint.getException())) {
                     String command = commandFactory.createCatchOn(excBreakpoint);
                     sendCommand(command);
                     // TODO: Read response. Now the protocol sends back just a
                     // message. Will be changed to the confirmation. Then read
                     // it here.
                 }
             } else {
                 throw new IllegalArgumentException("Unknown breakpoint type: " + breakpoint);
             }
         }
     }
     
     public void removeBreakpoint(final IRubyBreakpoint breakpoint) {
         removeBreakpoint(breakpoint, false);
     }
     
     /**
      * Remove the given breakpoint from this debugging session.
      *
      * @param breakpoint breakpoint to be removed
      * @param silent whether info message should be omitted if the breakpoint
      *        has not been set in this session
      */
     public void removeBreakpoint(final IRubyBreakpoint breakpoint, boolean silent) {
         if (breakpoint instanceof IRubyLineBreakpoint) {
             IRubyLineBreakpoint lineBreakpoint = (IRubyLineBreakpoint) breakpoint;
             Integer id = findBreakpointId(lineBreakpoint);
             if (id != null) {
                 String command = commandFactory.createRemoveBreakpoint(id);
                 try {
                     sendCommand(command);
                     getReadersSupport().waitForRemovedBreakpoint(id);
                     breakpointsIDs.remove(id);
                 } catch (RubyDebuggerException e) {
                     Util.severe("Exception during removing breakpoint.", e);
                 }
             } else if (!silent) {
                 Util.fine("Breakpoint [" + breakpoint + "] cannot be removed since " +
                         "its ID cannot be found. Might have been alread removed.");
             }
         } else if (breakpoint instanceof IRubyExceptionBreakpoint) {
             // catchpoint removing is not supported by backend yet, handle in
             // the debug-commons-java until the support is added
             // http://rubyforge.org/tracker/index.php?func=detail&aid=20237&group_id=1900&atid=7436
             IRubyExceptionBreakpoint catchpoint = (IRubyExceptionBreakpoint) breakpoint;
             removedCatchpoints.add(catchpoint.getException());
         } else {
             throw new IllegalArgumentException("Unknown breakpoint type: " + breakpoint);
         }
     }
     
     /**
      * Update the given breakpoint. Use when <em>enabled</em> property has
      * changed.
      */
     public void updateBreakpoint(IRubyBreakpoint breakpoint) throws RubyDebuggerException {
         removeBreakpoint(breakpoint, true);
         addBreakpoint(breakpoint);
     }
     
     /**
      * Find ID under which the given breakpoint is known in the current
      * debugging session.
      *
      * @return found ID; might be <tt>null</tt> if none is found
      */
     private Integer findBreakpointId(final IRubyLineBreakpoint wantedBP) {
         for (Iterator<Map.Entry<Integer, IRubyLineBreakpoint>> it = breakpointsIDs.entrySet().iterator(); it.hasNext();) {
             Map.Entry<Integer, IRubyLineBreakpoint> breakpointID = it.next();
             IRubyLineBreakpoint bp = breakpointID.getValue();
             int id = breakpointID.getKey();
             if (wantedBP.getFilePath().equals(bp.getFilePath()) &&
                     wantedBP.getLineNumber() == bp.getLineNumber()) {
                 return id;
             }
         }
         return null;
     }
     
     private void startRubyLoop() {
         rubyLoop = new RubyLoop();
         rubyLoop.start();
     }
     
     public Socket getCommandSocket() throws RubyDebuggerException {
         if (commandSocket == null) {
             commandSocket = attach();
         }
         return commandSocket;
     }
     
     public void resume(final RubyThread thread) {
         try {
             sendCommand(commandFactory.createResume(thread));
         } catch (RubyDebuggerException e) {
             Util.severe("resuming of " + thread.getId() + " failed", e);
         }
     }
     
     private void sendCommand(final String s) throws RubyDebuggerException {
         Util.fine("Sending command debugger: " + s);
         if (!debugTarged.isRunning()) {
             throw new RubyDebuggerException("Trying to send a command [" + s + "] to terminated process");
         }
         getCommandWriter().println(s);
     }
     
     
     public void sendStepOver(RubyFrame frame, boolean forceNewLine) {
         try {
             if (forceNewLine) {
                 sendCommand(commandFactory.createForcedStepOver(frame));
             } else {
                 sendCommand(commandFactory.createStepOver(frame));
             }
         } catch (RubyDebuggerException e) {
             Util.severe("Stepping failed", e);
         }
     }
     
     public void sendStepReturnEnd(RubyFrame frame) {
         try {
             sendCommand(commandFactory.createStepReturn(frame));
         } catch (RubyDebuggerException e) {
             Util.severe("Stepping failed", e);
         }
     }
     
     public void sendStepIntoEnd(RubyFrame frame, boolean forceNewLine) {
         try {
             if (forceNewLine) {
                 sendCommand(commandFactory.createForcedStepInto(frame));
             } else {
                 sendCommand(commandFactory.createStepInto(frame));
             }
         } catch (RubyDebuggerException e) {
             Util.severe("Stepping failed", e);
         }
     }
     
     public RubyThreadInfo[] readThreadInfo() throws RubyDebuggerException {
         sendCommand(commandFactory.createReadThreads());
         return getReadersSupport().readThreads();
     }
     
     public RubyFrame[] readFrames(RubyThread thread) throws RubyDebuggerException {
         RubyFrameInfo[] infos;
         try {
             sendCommand(commandFactory.createReadFrames(thread));
             infos = getReadersSupport().readFrames();
         } catch (RubyDebuggerException e) {
             if (checkConnection()) {
                 throw e;
             }
             infos = new RubyFrameInfo[0];
         }
         RubyFrame[] frames = new RubyFrame[infos.length];
         for (int i = 0; i < infos.length; i++) {
             RubyFrameInfo info = infos[i];
             frames[i] = new RubyFrame(thread, info);
         }
         return frames;
     }
     
     public RubyVariable[] readVariables(RubyFrame frame) throws RubyDebuggerException {
         sendCommand(commandFactory.createReadLocalVariables(frame));
         RubyVariableInfo[] infos = getReadersSupport().readVariables();
         RubyVariable[] variables= new RubyVariable[infos.length];
         for (int i = 0; i < infos.length; i++) {
             RubyVariableInfo info = infos[i];
             variables[i] = new RubyVariable(info, frame);
         }
         return variables;
     }
     
     public RubyVariable[] readInstanceVariables(final RubyVariable variable) throws RubyDebuggerException {
         sendCommand(commandFactory.createReadInstanceVariable(variable));
         RubyVariableInfo[] infos = getReadersSupport().readVariables();
         RubyVariable[] variables= new RubyVariable[infos.length];
         for (int i = 0; i < infos.length; i++) {
             RubyVariableInfo info = infos[i];
             variables[i] = new RubyVariable(info, variable);
         }
         return variables;
     }
     
     public RubyVariable[] readGlobalVariables() throws RubyDebuggerException {
         sendCommand(commandFactory.createReadGlobalVariables());
         RubyVariableInfo[] infos = getReadersSupport().readVariables();
         RubyVariable[] variables= new RubyVariable[infos.length];
         for (int i = 0; i < infos.length; i++) {
             RubyVariableInfo info = infos[i];
             variables[i] = new RubyVariable(this, info);
         }
         return variables;
     }
     
     public RubyVariable inspectExpression(RubyFrame frame, String expression) throws RubyDebuggerException {
         expression = expression.replaceAll("\n", "\\\\n");
         sendCommand(commandFactory.createInspect(frame, expression));
         RubyVariableInfo[] infos = getReadersSupport().readVariables();
         return infos.length == 0 ? null : new RubyVariable(infos[0], frame);
     }
     
     public void finish(final boolean forced) {
         synchronized(this) {
             if (finished) {
                 // possible if client call this explicitly and then second time from RubyLoop
                 Util.fine("Trying to finish the same proxy more than once: " + this);
                 return;
             }
             finished = true;
             PROXIES.remove(RubyDebuggerProxy.this);
             if (forced) {
                 sendExit();
                 try {
                     // Needed to let the IO readers to read the last pieces of input and
                     // output streams.
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                     Util.LOGGER.log(Level.INFO, "Interrupted during IO readers waiting", e);
                 }
                 getDebugTarged().getProcess().destroy();
             }
         }
         fireDebugEvent(RubyDebugEvent.createTerminateEvent());
     }
     
     private synchronized void sendExit() {
         if (commandSocket != null && debugTarged.isRunning()) {
             try {
                 sendCommand("exit");
             } catch (RubyDebuggerException ex) {
                 Util.fine("'exit' command failed. Process died? " + debugTarged.isRunning());
             }
         }
     }
     
     /**
      * Tries to attach to the <code>targed</code>'s process and gives up in
      * <code>timeout</code> seconds.
      */
     private Socket attach() throws RubyDebuggerException {
         int port = debugTarged.getPort();
         Socket socket = null;
         for (int tryCount = (timeout*2), i = 0; i < tryCount && socket == null; i++) {
             try {
                socket = new Socket(InetAddress.getLocalHost(), port);
             } catch (ConnectException e) {
                 synchronized (this) {
                     if (finished) { // terminated by frontend before process started
                         throw new RubyDebuggerException("Process was terminated before debugger connection was established.");
                     }
                     if (i == tryCount - 1) {
                         failWithInfo(e);
                     }
                 }
                 try {
                     if (debugTarged.isRunning()) {
                         Util.finest("Cannot connect to localhost:" + port + ". Trying again...(" + (tryCount - i - 1) + ')');
                         Thread.sleep(500);
                     } else {
                         failWithInfo(e);
                     }
                 } catch (InterruptedException e1) {
                     Util.severe("Interrupted during attaching.", e1);
                     Thread.currentThread().interrupt();
                 }
             } catch (IOException e) {
                 throw new RubyDebuggerException(e);
             }
         }
         return socket;
     }
 
     private void failWithInfo(ConnectException e) throws RubyDebuggerException {
         String info = dumpProcess(debugTarged.getProcess());
         throw new RubyDebuggerException("Cannot connect to the debugged process in " + timeout + "s:\n\n" + info, e);
     }
 
     private static String dumpProcess(final Process process) {
         final StringBuilder info = new StringBuilder();
         boolean running = Util.isRunning(process);
         if (running) {
             info.append("But server process is running. You might try to increase the timeout. Killing...\n\n");
         }
         info.append(dumpStream(process.getInputStream(), Level.INFO, "Standard Output: ", running));
         info.append(dumpStream(process.getErrorStream(), Level.SEVERE, "Error Output: ", running));
         if (running) {
             process.destroy();
         }
         return info.toString();
     }
 
     private static String dumpStream(final InputStream stream, final Level level, final String msgPrefix, final boolean asynch) {
         final StringBuilder output = new StringBuilder();
         if (asynch) {
             Thread collector = new Thread(new Runnable() {
                 public void run() {
                     collect(stream, output);
                 }
             });
             collector.start();
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {
                 Util.severe(ex);
             }
             collector.interrupt();
         } else {
             collect(stream, output);
         }
         if (output.length() > 0) {
             Util.LOGGER.log(level, msgPrefix);
             String outputS = output.toString();
             Util.LOGGER.log(level, outputS);
             return msgPrefix + '\n' + outputS;
         } else {
             return "";
         }
     }
     
     private static void collect(final InputStream stream, final StringBuilder output) {
         try {
             int c;
             while ((c = stream.read()) != -1) {
                 output.append((char) c);
             }
         } catch (IOException e) {
             Util.LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
         }
     }
 
     /**
      * Tells the proxy whether condition on breakpoint is supported. Older
      * engines version do not support it.
      */
     void setConditionSupport(final boolean supportsCondition) {
         this.supportsCondition = supportsCondition;
     }
 
     private class RubyLoop extends Thread {
         
         RubyLoop() {
             this.setName("RubyDebuggerLoop [" + System.currentTimeMillis() + ']');
         }
         
         public void suspensionOccurred(final SuspensionPoint hit) {
             new Thread() {
                 public @Override void run() {
                     debugTarged.suspensionOccurred(hit);
                 }
             }.start();
         }
         
         public @Override void run() {
             Util.finest("Waiting for breakpoints.");
             while (true) {
                 SuspensionPoint sp = getReadersSupport().readSuspension();
                 if (sp == SuspensionPoint.END) {
                     break;
                 }
                 Util.finest(sp.toString());
 
                 // see removedCatchpoints's JavaDoc
                 if (sp.isException()) {
                     ExceptionSuspensionPoint exceptionSP = (ExceptionSuspensionPoint) sp;
                     if (removedCatchpoints.contains(exceptionSP.getExceptionType())) {
                         RubyThread thread = getDebugTarged().getThreadById(sp.getThreadId());
                         if (thread != null) {
                             RubyDebuggerProxy.this.resume(thread);
                             continue;
                         }
                     }
                 }
 
                 RubyLoop.this.suspensionOccurred(sp);
             }
             finish(getReadersSupport().isUnexpectedFail());
             Util.finest("Socket reader loop finished.");
         }
     }
     
 }

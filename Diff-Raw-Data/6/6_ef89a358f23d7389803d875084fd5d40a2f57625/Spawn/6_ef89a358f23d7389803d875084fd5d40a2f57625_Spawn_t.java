 package com.redhat.rhevm.api.powershell.expectj;
 
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.Pipe;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * This class is used for talking to processes / ports. This will also interact
  * with the process to read and write to it.
  *
  * @author	Sachin Shekar Shetty
  */
 public class Spawn {
     /**
      * Log messages go here.
      */
     private final static Log LOG = LogFactory.getLog(ProcessSpawn.class);
 
     /** Default time out for expect commands */
     private long m_lDefaultTimeOutSeconds = -1;
 
     /**
      * Buffered wrapper stream for slave's stdin.
      */
     private BufferedWriter toStdin = null;
 
     /**
      * This is what we're actually talking to.
      */
     private SpawnableHelper slave = null;
 
     /**
      * Turns false on timeout.
      */
     private volatile boolean continueReading = true;
 
     /**
      * Pumps data from stdin to the spawn's stdin.
      */
     private StreamPiper interactIn = null;
 
     /**
      * Pumps data from the spawn's stdout to stdout.
      */
     private StreamPiper interactOut = null;
 
     /**
      * Pumps data from the spawn's stderr to stderr.
      */
     private StreamPiper interactErr = null;
 
     /**
      * Wait for data from spawn's stdout.
      */
     private Selector stdoutSelector;
 
     /**
      * Wait for data from spawn's stderr.
      */
     private Selector stderrSelector;
 
     /**
      * This object will be notified on timer timeout or when the spawn we're
      * waiting for closes.
      */
     private final Object doneWaitingForClose = new Object();
 
     /**
      * Constructor
      *
      * @param spawn This is what we'll control.
      * @param lDefaultTimeOutSeconds Default timeout for expect commands
      * @throws IOException on trouble launching the spawn
      */
     Spawn(Spawnable spawn, long lDefaultTimeOutSeconds) throws IOException {
         if (lDefaultTimeOutSeconds < -1) {
             throw new IllegalArgumentException("Timeout must be >= -1, was "
                                                + lDefaultTimeOutSeconds);
         }
         m_lDefaultTimeOutSeconds = lDefaultTimeOutSeconds;
 
         slave = new SpawnableHelper(spawn, lDefaultTimeOutSeconds);
         slave.start();
         LOG.debug("Spawned Process: " + spawn);
 
         if (slave.getStdin() != null) {
             toStdin =
                 new BufferedWriter(new OutputStreamWriter(slave.getStdin()));
         }
 
         stdoutSelector = Selector.open();
         slave.getStdoutChannel().register(stdoutSelector, SelectionKey.OP_READ);
         if (slave.getStderrChannel() != null) {
             stderrSelector = Selector.open();
             slave.getStderrChannel().register(stderrSelector, SelectionKey.OP_READ);
         }
     }
 
     /**
      * This method is invoked by our {@link Timer} when the time-out occurs.
      */
     private synchronized void timerTimedOut() {
         continueReading = false;
         stdoutSelector.wakeup();
         if (stderrSelector != null) {
             stderrSelector.wakeup();
         }
         synchronized (doneWaitingForClose) {
             doneWaitingForClose.notify();
         }
     }
 
     /**
      * This method is invoked by our {@link Timer} when the timer thread
      * receives an interrupted exception
      * @param reason The reason for the interrupt
      */
     private void timerInterrupted(InterruptedException reason) {
         timerTimedOut();
     }
 
     /**
      * Wait for a pattern to appear on standard out.
      * @param pattern The case-insensitive substring to match against.
      * @param timeOutSeconds The timeout in seconds before the match fails.
      * @throws IOException on IO trouble waiting for pattern
      * @throws TimeoutException on timeout waiting for pattern
      */
     public void expect(String pattern, long timeOutSeconds)
     throws IOException, TimeoutException
     {
         expect(pattern, timeOutSeconds, stdoutSelector);
     }
 
     /**
      * Wait for the spawned process to finish.
      * @param timeOutSeconds The number of seconds to wait before giving up, or
      * -1 to wait forever.
      * @throws ExpectJException if we're interrupted while waiting for the spawn
      * to finish.
      * @throws TimeoutException if the spawn didn't finish inside of the
      * timeout.
      * @see #expectClose()
      */
     public void expectClose(long timeOutSeconds)
     throws TimeoutException, ExpectJException
     {
         if (timeOutSeconds < -1) {
             throw new IllegalArgumentException("Timeout must be >= -1, was "
                                                + timeOutSeconds);
         }
 
         LOG.debug("Waiting for spawn to close connection...");
         Timer tm = null;
         slave.setCloseListener(new Spawnable.CloseListener() {
             public void onClose() {
                 synchronized (doneWaitingForClose) {
                     doneWaitingForClose.notify();
                 }
             }
         });
         if (timeOutSeconds != -1 ) {
             tm = new Timer(timeOutSeconds, new TimerEventListener() {
                 public void timerTimedOut() {
                     Spawn.this.timerTimedOut();
                 }
 
                 public void timerInterrupted(InterruptedException reason) {
                     Spawn.this.timerInterrupted(reason);
                 }
             });
             tm.startTimer();
         }
         continueReading = true;
         boolean closed = false;
         synchronized (doneWaitingForClose) {
             while(continueReading) {
                 // Sleep if process is still running
                 if (slave.isClosed()) {
                     closed = true;
                     break;
                 } else {
                     try {
                         doneWaitingForClose.wait(500);
                     } catch (InterruptedException e) {
                         throw new ExpectJException("Interrupted waiting for spawn to finish",
                                                    e);
                     }
                 }
             }
         }
         if (tm != null) {
             tm.close();
         }
         if (closed) {
             LOG.debug("Connection to spawn closed, continueReading="
                       + continueReading);
         } else {
             LOG.debug("Timed out waiting for spawn to close, continueReading="
                       + continueReading);
         }
         if (tm != null) {
             LOG.debug("Timer Status:" + tm.getStatus());
         }
         if (!continueReading) {
             throw new TimeoutException("Timeout waiting for spawn to finish");
         }
 
         freeResources();
     }
 
     /**
      * Free up system resources.
      */
     private void freeResources() {
         try {
             slave.close();
             if (interactIn != null) {
                 interactIn.stopProcessing();
             }
             if (interactOut != null) {
                 interactOut.stopProcessing();
             }
             if (interactErr != null) {
                 interactErr.stopProcessing();
             }
             if (stderrSelector != null) {
                 stderrSelector.close();
             }
             if (stdoutSelector != null) {
                 stdoutSelector.close();
             }
             if (toStdin != null) {
                 toStdin.close();
             }
         } catch (IOException e) {
             // Cleaning up is a best effort operation, failures are
             // logged but otherwise accepted.
             LOG.warn("Failed cleaning up after spawn done", e);
         }
     }
 
     /**
      * Wait the default timeout for the spawned process to finish.
      * @throws ExpectJException If something fails.
      * @throws TimeoutException if the spawn didn't finish inside of the default
      * timeout.
      * @see #expectClose(long)
      * @see ExpectJ#ExpectJ(long)
      */
     public void expectClose()
     throws ExpectJException, TimeoutException
     {
         expectClose(m_lDefaultTimeOutSeconds);
     }
 
     /**
      * Workhorse of the expect() and expectErr() methods.
      * @see #expect(String, long)
      * @param pattern What to look for
      * @param lTimeOutSeconds How long to look before giving up
      * @param selector A selector covering only the channel we should read from
      * @throws IOException on IO trouble waiting for pattern
      * @throws TimeoutException on timeout waiting for pattern
      */
     private void expect(String pattern, long lTimeOutSeconds, Selector selector)
     throws IOException, TimeoutException
     {
         if (lTimeOutSeconds < -1) {
             throw new IllegalArgumentException("Timeout must be >= -1, was "
                                                + lTimeOutSeconds);
         }
 
         if (selector.keys().size() != 1) {
             throw new IllegalArgumentException("Selector key set size must be 1, was "
                                                + selector.keys().size());
         }
         // If this cast fails somebody gave us the wrong selector.
         Pipe.SourceChannel readMe =
             (Pipe.SourceChannel)((SelectionKey)(selector.keys().iterator().next())).channel();
 
         LOG.debug("Expecting '" + pattern + "'");
         continueReading = true;
         boolean found = false;
         StringBuilder line = new StringBuilder();
         Date runUntil = null;
         if (lTimeOutSeconds > 0) {
             runUntil = new Date(new Date().getTime() + lTimeOutSeconds * 1000);
         }
         ByteBuffer buffer = ByteBuffer.allocate(1024);
         while(continueReading) {
             if (runUntil == null) {
                 selector.select();
             } else {
                 long msLeft = runUntil.getTime() - new Date().getTime();
                 if (msLeft > 0) {
                     selector.select(msLeft);
                 } else {
                     continueReading = false;
                     break;
                 }
             }
             if (selector.selectedKeys().size() == 0) {
                 // Woke up with nothing selected, try again
                 continue;
             }
 
             buffer.rewind();
            int nbytes = readMe.read(buffer);
            if (nbytes == -1) {
                 // End of stream
                 throw new IOException("End of stream reached, no match found");
             }
             buffer.rewind();
            for (int i = 0; i < nbytes; i++) {
                 line.append((char)buffer.get(i));
             }
             if (line.toString().trim().toUpperCase().indexOf(pattern.toUpperCase()) != -1) {
                 LOG.debug("Found match for " + pattern + ":" + line);
                 found = true;
                 break;
             }
             while (line.indexOf("\n") != -1) {
                 line.delete(0, line.indexOf("\n") + 1);
             }
         }
         if (found) {
             LOG.debug("Match found, continueReading=" + continueReading);
         } else {
             LOG.debug("Timed out waiting for match, continueReading="
                       + continueReading);
         }
         if (!continueReading) {
             throw new TimeoutException("Timeout trying to match \"" + pattern + "\"");
         }
     }
 
     /**
      * Wait for a pattern to appear on standard error.
      * @see #expect(String, long)
      * @param pattern The case-insensitive substring to match against.
      * @param timeOutSeconds The timeout in seconds before the match fails.
      * @throws TimeoutException on timeout waiting for pattern
      * @throws IOException on IO trouble waiting for pattern
      */
     public void expectErr(String pattern, long timeOutSeconds)
     throws IOException, TimeoutException
     {
         expect(pattern, timeOutSeconds, stderrSelector);
     }
 
     /**
      * Wait for a pattern to appear on standard out.
      * @param pattern The case-insensitive substring to match against.
      * @throws TimeoutException on timeout waiting for pattern
      * @throws IOException on IO trouble waiting for pattern
      */
     public void expect(String pattern)
     throws IOException, TimeoutException
     {
         expect(pattern, m_lDefaultTimeOutSeconds);
     }
 
     /**
      * Wait for a pattern to appear on standard error.
      * @param pattern The case-insensitive substring to match against.
      * @throws TimeoutException on timeout waiting for pattern
      * @throws IOException on IO trouble waiting for pattern
      * @see #expect(String)
      */
     public void expectErr(String pattern)
     throws IOException, TimeoutException
     {
         expectErr(pattern, m_lDefaultTimeOutSeconds);
     }
 
     /**
      * This method can be use use to check the target process status
      * before invoking {@link #send(String)}
      * @return true if the process has already exited.
      */
     public boolean isClosed() {
         return slave.isClosed();
     }
 
     /**
      * Retrieve the exit code of a finished process.
      * @return the exit code of the process if the process has
      * already exited.
      * @throws ExpectJException if the spawn is still running.
      */
     public int getExitValue()
     throws ExpectJException
     {
         return slave.getExitValue();
     }
 
     /**
      * Writes a string to the standard input of the spawned process.
      *
      * @param string The string to send.  Don't forget to terminate it with \n
      * if you want it linefed.
      * @throws IOException on IO trouble talking to spawn
      */
     public void send(String string)
     throws IOException {
         LOG.debug("Sending '" + string + "'");
         toStdin.write(string);
         toStdin.flush();
     }
 
     /**
      * Allows the user to interact with the spawned process.
      */
     public void interact() {
         // FIXME: User input is echoed twice on the screen
         interactIn = new StreamPiper(null,
                                      System.in, slave.getStdin());
         interactIn.start();
         interactOut = new StreamPiper(null,
                                       Channels.newInputStream(slave.getStdoutChannel()),
                                       System.out);
         interactOut.start();
         interactErr = new StreamPiper(null,
                                       Channels.newInputStream(slave.getStderrChannel()),
                                       System.err);
         interactErr.start();
         slave.stopPipingToStandardOut();
     }
 
     /**
      * This method kills the process represented by SpawnedProcess object.
      */
     public void stop() {
         slave.stop();
 
         freeResources();
     }
 
     /**
      * Returns everything that has been received on the spawn's stdout during
      * this session.
      *
      * @return the available contents of Standard Out
      */
     public String getCurrentStandardOutContents() {
         return slave.getCurrentStandardOutContents();
     }
 
     /**
      * Returns everything that has been received on the spawn's stderr during
      * this session.
      *
      * @return the available contents of Standard Err
      */
     public String getCurrentStandardErrContents() {
         return slave.getCurrentStandardErrContents();
     }
 }

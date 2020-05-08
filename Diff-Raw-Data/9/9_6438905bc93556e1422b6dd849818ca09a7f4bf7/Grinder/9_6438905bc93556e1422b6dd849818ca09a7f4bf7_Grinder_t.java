 // Copyright (C) 2001 - 2008 Philip Aston
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
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.script;
 
 import net.grinder.common.FilenameFactory;
 import net.grinder.common.GrinderException;
 import net.grinder.common.GrinderProperties;
 import net.grinder.common.Logger;
 
 
 /**
  * Name space for <code>grinder</code> script context object.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class Grinder {
 
   /**
    * Object that provides context services to scripts.
    */
   public static InternalScriptContext grinder;
 
   /**
    * Scripts can get contextual information through a global
    * <code>net.grinder.script.Grinder.grinder</code> object that
    * supports this interface.
    *
    * @author Philip Aston
    * @version $Revision$
    */
   public static interface ScriptContext {
     /**
      * Return the agent number. The console allocates a unique number to that
      * each connected agent, and the agent passes this on to the worker process.
      *
      * <p>
      * The lowest possible number is allocated. When an agent disconnects, its
      * number will be reused. Script authors can assume that the agent number
      * lies between <code>0</code> and the number of currently connected
      * agents.
      * </p>
      *
      * @return The agent number, or <code>-1</code> if not launched from the
      *         console.
      * @see #getProcessNumber()
      * @see #getThreadNumber()
      */
     int getAgentNumber();
 
 
     /**
      * Return the process number. The agent allocates a unique number to that
      * each worker process it starts.
      *
      * <p>
      * The lowest possible number is allocated. When an worker process stops,
      * its number will be reused. Script authors can assume that the worker
      * number lies between <code>0</code> and the number of currently
      * connected agents.
      * </p>
      *
      * @return The process number.
      * @see #getAgentNumber()
      * @see #getThreadNumber()
      */
     int getProcessNumber();
 
     /**
      * Get a unique name for this worker process.
      *
      * @return The process name.
      * @see #getProcessNumber()
      */
     String getProcessName();
 
     /**
      * Return the thread number, or <code>-1</code> if not called from a
      * worker thread.
      *
      * @return The thread number.
      * @see #getAgentNumber()
      * @see #getProcessNumber()
      */
     int getThreadNumber();
 
     /**
      * Return the current run number, or <code>-1</code> if not called from a
      * worker thread.
      *
      * @return An <code>int</code> value.
      */
     int getRunNumber();
 
     /**
      * Get a {@link net.grinder.common.Logger}.
      *
      * @return A <code>Logger</code>.
      */
     Logger getLogger();
 
     /**
      * Sleep for a time based on the meanTime parameter. The actual
      * time may be greater or less than meanTime, and is distributed
      * according to a pseudo normal distribution.
      *
      * @param meanTime Mean time in milliseconds.
      * @exception GrinderException If the sleep failed.
      */
     void sleep(long meanTime) throws GrinderException;
 
     /**
      * Sleep for a time based on the meanTime parameter. The actual
      * time may be greater or less than meanTime, and is distributed
      * according to a pseudo normal distribution.
      *
      * @param meanTime Mean time in milliseconds.
      * @param sigma The standard deviation, in milliseconds.
      * @exception GrinderException If the sleep failed.
      */
     void sleep(long meanTime, long sigma) throws GrinderException;
 
     /**
      * Start a new worker thread.
      *
      * @return The thread number of the new worker thread.
      * @throws InvalidContextException If the main thread has not yet
      *  initialised the script engine, or all other threads have shut down.
      *  Typically, you should only call <code>startWorkerThread()</code> from
      *  another worker thread.
      * @throws GrinderException If the new worker thread could not be started.
      */
    int startWorkerThread() throws GrinderException;
 
     /**
      * Get a {@link net.grinder.common.FilenameFactory} that can be
      * used to create unique filenames. The filenames depend upon the
      * process name and the thread used to call the
      * <code>FilenameFactory</code>.
      *
      * @return A <code>FilenameFactory</code>.
      */
     FilenameFactory getFilenameFactory();
 
     /**
      * Get the global properties for this agent/worker process set.
      *
      * @return The properties.
      */
     GrinderProperties getProperties();
 
     /**
      * Get a {@link Statistics} object that allows statistics to be queried
      * and updated.
      *
      * @return The statistics.
      */
     Statistics getStatistics();
 
 
     /**
      * Get an {@link SSLControl}. This can be used to create secure
      * sockets or to set the certificates that a worker thread should
      * use.
      *
      * @return The SSL control.
      */
     SSLControl getSSLControl();
   }
 }

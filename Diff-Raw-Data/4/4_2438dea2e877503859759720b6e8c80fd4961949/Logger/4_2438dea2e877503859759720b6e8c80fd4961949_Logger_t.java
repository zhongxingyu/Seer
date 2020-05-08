 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002 Philip Aston
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
 
 package net.grinder.common;
 
 import java.io.PrintWriter;
 
 
 /**
  * Interface to The Grinder logging system.
  *
  * <p>Output can be sent to either the terminal used to launch the
 * worker process, or to the process log files. The destination is
 * specified by a mask of constant values.</p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public interface Logger
 {
     /** Destination constant that represents the log files. **/
     public int LOG = 1 << 0;
 
     /** Destination constant that represents the terminal. **/
     public int TERMINAL = 1 << 1;
 
     /**
      * Log a message to the output log with context information.
      * <p>Equivalent to <code>output(message, Logger.LOG)</code>.</p>
      * @param message The message
      */
     public void output(String message);
 
     /**
      * Log a message with context information.
      * @param message The message
      * @param where Destination mask
      */
     public void output(String message, int where);
 
     /**
      * Log an error to the error log  with context information.
      * <p>Equivalent to <code>error(message, Logger.LOG)</code>.</p>
      * @param message The message
      */
     public void error(String message);
 
     /**
      * Log an error with context information.
      * @param message The message
      * @param where Destination mask
      */
     public void error(String message, int where);
 
     /**
      * Get a <code>PrintWriter</code> that can be used to write to the output log file.
      * @return a <code>PrintWriter</code>
      */
     public PrintWriter getOutputLogWriter();
 
     /**
      * Get a <code>PrintWriter</code> that can be used to write to the error log file.
      * @return a <code>PrintWriter</code>
      */
     public PrintWriter getErrorLogWriter();
 }

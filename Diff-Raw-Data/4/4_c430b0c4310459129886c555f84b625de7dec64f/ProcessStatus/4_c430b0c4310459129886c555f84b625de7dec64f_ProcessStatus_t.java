 // Copyright (C) 2001, 2002 Philip Aston
 // Copyright (C) 2001, 2002 Dirk Feufel
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
 
 
 /**
  * @author Philip Aston
  **/
 public interface ProcessStatus
 {
     public static final short STATE_STARTED = 1;
     public static final short STATE_RUNNING = 2;
     public static final short STATE_FINISHED = 3;
 
     /**
      * Return the process name.
      *
      * @return The process name.
      **/
     String getName();
 
     /**
      * Return the process status.
      *
     * @return One of {@link #STATE_STARTED}, {@link #STATE_RUNNING},
     * {@link #STATE_FINISHED}.
      **/
     short getState();
 
     /**
      * Get the number of running threads
      *
      * @return The number of threads that are stull running.
      **/
     short getNumberOfRunningThreads();
 
     /**
      * Get the total number of threads
      *
      * @return The total number of threads.
      **/
     short getTotalNumberOfThreads();
 }
 

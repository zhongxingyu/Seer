 package gnu.cajo.utils.extra;
 
 import java.io.*;
 import gnu.cajo.invoke.Remote;
 
 /*
  * General purpose cooperative task scheduler
  * Copyright (c) 1999 John Catherino
  *
  * For issues or suggestions mailto:cajo@dev.java.net
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, at version 2.1 of the license, or any
  * later version.  The license differs from the GNU General Public License
  * (GPL) to allow this library to be used in proprietary applications. The
  * standard GPL would forbid this.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * To receive a copy of the GNU Lesser General Public License visit their
  * website at http://www.fsf.org/licenses/lgpl.html or via snail mail at Free
  * Software Foundation Inc., 59 Temple Place Suite 330, Boston MA 02111-1307
  * USA
  */
 
 /**
  * This class is for scheduling the non-preemptive execution of tasks.  The
  * class creates a single thread, and will independently manange and run its
  * loaded objects.  The Scheduler accepts objects for scheduling, at any time
  * during its existence. The scheduled object must implement no-arg, void
  * slice() method, to be used as a time slice, in which to perform some fixed
  * piece of functionality. The scheduler will provide the calling mechanism.
  * An objectwith this type of method implementation will be considered a task
  * for the purposes of this class.
  * <p>
  * The scheduler's purpose is to provide an exclusive thread of execution
  * amongst the scheduled tasks.  It will assure that only one of the tasks is
  * running at any given time, therefore shared memory between the tasks cannot
  * be corrupted by the scheduler due to synchronization problems.
  * <p>
  * All scheduler methods are properly synchronized, to allow task loading,
  * unloading, and management, from other threads, as well as by the scheduled
  * tasks themselves.
  * <p>
  * Up to 32 tasks can be loaded, at any given time, for scheduling.  Once
  * loaded, tasks can be scheduled to run in any, or all, of three ways;
  * Synchronous, Triggered, or Asynchronous. The class implements three methods
  * to flag a loaded task as such.  The following is a description of the
  * scheduling algorithm:
  * <p>
  * When a task is flagged as asynchronous, it will be run only when no
  * tasks are flagged as synchronous, or triggered, at the start of its
  * slice.  Its asynchronous flag is not cleared when it is run, meaning until
  * stopped, it will be run again automatically, as scheduling permits.
  * Scheduling among multiple asynchronous flags is round-robbin, to ensure
  * each an opportunity to run.  This provides a method for scheduling
  * low-priority tasks.  Typically an asynchronous task will break its
  * functionality into distinct pieces, and use a switch() type mechanism to
  * execute only one piece per timeslice.
  * <p>
  * When a task is flagged as triggered, will run before any tasks flagged
  * asynchronous, but only when no tasks are flagged as synchronous, at the
  * start of its slice.  The trigger flag is cleared when the task is run,
  * meaning that unless re-flagged as triggered, it will not run again
  * through the trigger scheduling algorithm. This provides an event-driven
  * mode of execution, but at a lower priority than the synchronous mode.
  * <p>
  * When a task is flagged as synchronous, it will run before any triggered,
  * or asynchronous flagged tasks are allowed to run.  Its synchronous flag
  * is also cleared when the task is run. Since the scheduling is not
  * preemptive, any currently running task is allowed to complete. This flag
  * provides the highest responsiveness to a synchronizing event.
  * <p>
  * The stop method is used to prevent the execution of any flagged task.
  * General notes:<ul>
  * <li>
  * Since scheduling is not preemptive, agressively minimize asynchronous task's
  * run time requirements, to decrease the latency of event responsiveness.  This
  * becomes especially important when tasks are elevated in priority.
  * <li>
  * Non-preemptive scheduling eliminates all synchronization concerns for
  * objects shared between scheduled tasks.
  * <li>
  * As a general rule: try to use asynchronous scheduling initially.  Then, as
  * the design runtime load increases, selected tasks can be boosted in
  * proiority to achieve the desired responsiveness.
  * </ul><p>
  * <i>Note:</i>  This class supports serialization.  It will restart the
  * scheduling task automatically on deserialization.  However, in order for
  * serialization to succeed, all of the loaded tasks must also be serializable.
  *
  * @version 1.0, 01-Nov-99 Initial release
  * @author John Catherino
  *
  */
 
 public final class Scheduler implements Serializable {
    private transient Thread thread;
    private int syncFlags, soonFlags, wakeFlags;
    private Object list[] = new Object[32];
    private void readObject(ObjectInputStream in)
       throws IOException, ClassNotFoundException {
       in.defaultReadObject();
       setEnabled(true);
    }
    private final Runnable kernel = new Runnable() {
       public void run() {
          try {
             int next = 0;
            while (!thread.isInterrupted()) {
                int slot = 0;
                synchronized(Scheduler.this) {
                   if (syncFlags != 0) {
                      int mask = 1;
                      while((syncFlags & mask) == 0) {
                         slot++;
                         mask <<= 1;
                      }
                      syncFlags ^= mask;
                   } else if (soonFlags != 0) {
                      int mask = 1;
                      while((soonFlags & mask) == 0) {
                         slot++;
                         mask <<= 1;
                      }
                      soonFlags ^= mask;
                   } else if (wakeFlags != 0) {
                      for (int i = 0; i < 32; i++) {
                         if (++next == 32) next = 0;
                         if ((1 << next & wakeFlags) != 0) {
                            slot = next;
                            break;
                         }
                      }
                   } else {
                      Scheduler.this.wait();
                      continue;
                   }
                }
                try { Remote.invoke(list[slot], "slice", null); }
                catch(Exception x) { drop(new Integer(slot)); }
            }
          } catch(InterruptedException x) {}
       }
    };
    /**
     * Nothing is performed in the constructor, since no tasks can be
     * scheduled for execution until they have been loaded.
     */
    public Scheduler() {}
    /**
     * This method will start, or suspend, the scheduler.  The scheduler will
     * be started automatically when the first task is flagged for execution.
     * Enabling an already enabled scheduler will cause no effect, just as
     * disabling a currently disabled scheduler. <i>Note:</i> the scheduler
     * cannot be stopped remotely.
     * @param enabled The flag to indicate if this is a startup, or suspend
     * operation.
     * @return true if successfully started or stopped, false if not for
     * logical reasons, i.e. already stopped/started, no tasks running.
     */
    public synchronized boolean setEnabled(boolean enabled) {
       if (enabled) {
          if (syncFlags != 0 | soonFlags != 0 | wakeFlags != 0) {
             if (thread == null) {
                thread = new Thread(kernel);
                thread.start();
                return true;
             } else notify();
          }
       } else {
          if (thread != null) {
             thread.interrupt();
             thread = null;
             return true;
          }
       }
       return false;
    }
    /**
     * This method accepts a task to be scheduled.  If the task is accepted, it
     * is placed in the queue, but will not be executed, until it is
     * flagged for operation. If the execution of the task slice results in
     * an exception, the task will be automatically dropped from the queue.
     * @param task The task to attempt to schedule, it may be either local,
     * remote, or even a proxy, when enabled.
     * @return the handle of the task in the queue.  The task handle is used
     * in the scheduling methods.
     * @throws IllegalArgumentException If the task queue is full.
     */
    public synchronized Integer load(Object task) {
       for (int slot = 0; slot < 32; slot++) {
 	     if (list[slot] == null) {
 	        list[slot] = task;
             return new Integer(slot);
          }
       }
       throw new IllegalArgumentException("task table currently full");
    }
    /**
     * This method sets the synchronous execution flag for a task. The flag
     * will be cleared automatically, when the scheduler calls this task.
     * @param task The handle of the task in the queue
     * @return false if the task was already flagged, or not in the queue,
     * else true if successfully flagged.
     * @throws IllegalArgumentException If the task handle is invalid.
     */
    public synchronized Boolean sync(Integer task) {
       if (task.intValue() < 32) {
          int mask = 1 << task.intValue();
          if ((syncFlags & mask) == 0) {
             syncFlags |= mask;
             setEnabled(true);
             return Boolean.TRUE;
          } else return Boolean.FALSE;
       } else throw new IllegalArgumentException("task table index too large");
    }
    /**
     * This method sets the triggered execution flag for a task.  It will be
     * cleared just before passing execution on to the task.
     * @param  task The handle of the task in the queue
     * @return true if successfully flagged, false if already flagged
     * @throws IllegalArgumentException If the task handle is invalid.
     */
    public synchronized Boolean soon(Integer task) {
       if (task.intValue() < 32) {
          int mask = 1 << task.intValue();
          if ((soonFlags & mask) == 0) {
             soonFlags |= mask;
             setEnabled(true);
             return Boolean.TRUE;
          } else return Boolean.FALSE;
       } else throw new IllegalArgumentException("task table index too large");
    }
    /**
     * This method sets the asynchronous execution flag for a task. It will
     * <u>not</u> be cleared when passing execution on to the task. The flag
     * will retain its state, until changed through the scheduler interface.
     * @param  task The handle of the task to be scheduled for continuous
     * asynchronous execution.
     * @return true if successfully flagged, false if already flagged.
     * @throws IllegalArgumentException If the task handle is invalid.
     */
    public synchronized Boolean wake(Integer task) {
       if (task.intValue() < 32) {
          int mask = 1 << task.intValue();
          if ((wakeFlags & mask) == 0) {
             wakeFlags |= mask;
             setEnabled(true);
             return Boolean.TRUE;
          } else return Boolean.FALSE;
       } else throw new IllegalArgumentException("task table index too large");
    }
    /**
     * This method clears all scheduling flags for indicated task.  The task
     * will reamain in the queue however. <i>Note:</i> to remove a task, use
     * the drop method instead, it automatically calls stop, before removing
     * the task from the queue.
     * @param task The handle of the task in the queue
     * @throws IllegalArgumentException If the task handle is invalid.
     */
    public synchronized void stop(Integer task) {
       if (task.intValue() < 32) {
          int mask = ~(1 << task.intValue());
          syncFlags &= mask;
          soonFlags &= mask;
          wakeFlags &= mask;
       } else throw new IllegalArgumentException("task table index too large");
    }
    /**
     * This method clears all scheduling flags for the indicated task, and
     * also removes it from the queue.
     * @param task The handle of the task to stop and remove from the queue
     * @throws IllegalArgumentException If the task handle is invalid.
     */
    public synchronized void drop(Integer task) {
       if (task.intValue() < 32) {
          stop(task);
          list[task.intValue()] = null;
       } else throw new IllegalArgumentException("task table index too large");
    }
    /**
     * The purpose of this function is to reduce event-driven task latency by
     * allowing asynchronous tasks to voluntarily exit prematurely.  To improve
     * responsiveness, asynchronous tasks could check this method, before going
     * on to another functionally distinct section of its task execution. It is
     * normally checked when the async task has some periodic extra work, if
     * the method returns false, the extra work could be processed in its
     * current slice.
     * @return true if synchronous or triggered tasks have been flagged for
     * execution, false if it is OK for the task to continue a little longer.
     */
    public Boolean pending() {
       return (syncFlags != 0 | soonFlags != 0) ? Boolean.TRUE : Boolean.FALSE;
    }
 }

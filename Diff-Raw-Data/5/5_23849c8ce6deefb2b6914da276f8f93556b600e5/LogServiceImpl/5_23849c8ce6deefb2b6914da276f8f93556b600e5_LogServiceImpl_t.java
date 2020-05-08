 /* Copyright (c) 2006 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Institute for Pervasive Computing, ETH Zurich.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of ETH Zurich nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ch.ethz.iks.concierge.framework;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.Vector;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogListener;
 import org.osgi.service.log.LogReaderService;
 import org.osgi.service.log.LogService;
 import org.osgi.service.log.LogEntry;
 
 /**
  * A lightweight log service implementation. Since this is part of the
  * framework, the framework itself can be configured to use this log for debug
  * messages. This makes it easier to debug bundles on embedded and headless
  * devices.
  * 
  * @author Jan S. Rellermeyer, IKS, ETH Zurich
  */
 final class LogServiceImpl implements LogService, LogReaderService {
 	/**
 	 * the log buffer. Works like a ring buffer. The size can be configured by a
 	 * property.
 	 */
 	private final Vector logBuffer;
 
 	/**
 	 * the list of subscribed listeners.
 	 */
 	private final List logListeners = new ArrayList(0);
 
 	/**
 	 * the size.
 	 */
 	private final int LOG_BUFFER_SIZE;
 
 	/**
 	 * the log level.
 	 */
 	private final int LOG_LEVEL;
 
 	/**
 	 * do not log to screen ?
 	 */
 	private final boolean QUIET;
 
 	/**
 	 * the constants for the log levels.
 	 */
 	private static final String[] LEVELS = { "NULL", "ERROR", "WARNING",
 			"INFO", "DEBUG" };
 
 	public LogServiceImpl(final int buffersize, final int loglevel,
 			final boolean quiet) {
 		setDateFormat("MMM dd, yyyy HH:mm:ss:SSS");
 		LOG_BUFFER_SIZE = buffersize;
 		if (loglevel < 0) {
 			LOG_LEVEL = 0;
 		} else if (loglevel > 4) {
 			LOG_LEVEL = 4;
 		} else {
 			LOG_LEVEL = loglevel;
 		}
 		QUIET = quiet;
 		logBuffer = new Vector(LOG_BUFFER_SIZE);
 		if (!QUIET) {
 			System.out.println("Logger initialized, loglevel is "
 					+ LEVELS[LOG_LEVEL]);
 		}
 	}
 
 	private static DateFormat df;
 	private static Date myDate = new Date();
 
 	private void setDateFormat(String dfmt) {
 		if ((dfmt != null) && (dfmt.length() != 0)) {
 			try {
 				df = new SimpleDateFormat(dfmt);
 			} catch (RuntimeException e) {
 				df = DateFormat.getDateTimeInstance();
 			}
 		} else
 			df = DateFormat.getDateTimeInstance();
 
 		df.setTimeZone(TimeZone.getTimeZone("UTC"));
 	}
 	/**
 	 * log an entry.
 	 * 
 	 * @param entry
 	 *            the entry.
 	 */
 	private void log(final int level, final String message,
 			final Throwable throwable, final ServiceReference sref) {
 //		System.out.println("Log: "+level+" "+LOG_LEVEL+" -> "+(level <= LOG_LEVEL?"yo":"nö"));
 //		new Error("Trace log call").printStackTrace();
 		if (level <= LOG_LEVEL) {
 			final LogEntryImpl entry = LogEntryImpl.getEntry(level, message,
 					throwable, sref);
 			logBuffer.add(entry);
 			if (logBuffer.size() > LOG_BUFFER_SIZE) {
 				LogEntryImpl.releaseEntry((LogEntryImpl) logBuffer.remove(0));
 			}
 			for (Iterator listeners = logListeners.iterator(); listeners
 					.hasNext();) {
 				((LogListener) listeners.next()).logged(entry);
 			}
 			if (!QUIET) {
 				System.out.println(entry);
 			}
 		}
 	}
 
 	/**
 	 * Log a message.
 	 * 
 	 * @param level
 	 *            the level.
 	 * @param message
 	 *            the message.
 	 * @see org.osgi.service.log.LogService#log(int, java.lang.String)
 	 */
 	public void log(int level, String message) {
 		log(level, message, null, null);
 	}
 
 	/**
 	 * Log a message.
 	 * 
 	 * @param level
 	 *            the level.
 	 * @param message
 	 *            the message.
 	 * @param exception
 	 *            an exception.
 	 * 
 	 * @see org.osgi.service.log.LogService#log(int, java.lang.String,
 	 *      java.lang.Throwable)
 	 */
 	public void log(int level, String message, Throwable exception) {
 		log(level, message, exception, null);
 	}
 
 	/**
 	 * Log a message.
 	 * 
 	 * @param sr
 	 *            the service reference.
 	 * @param level
 	 *            the level.
 	 * @param message
 	 *            the message.
 	 * 
 	 * @see org.osgi.service.log.LogService#log(org.osgi.framework.ServiceReference,
 	 *      int, java.lang.String)
 	 */
 	public void log(ServiceReference sr, int level, String message) {
 		log(level, message, null, sr);
 	}
 
 	/**
 	 * Log a message.
 	 * 
 	 * @param sr
 	 *            the service reference.
 	 * @param level
 	 *            the level.
 	 * @param message
 	 *            the message.
 	 * 
 	 * @see org.osgi.service.log.LogService#log(org.osgi.framework.ServiceReference,
 	 *      int, java.lang.String, java.lang.Throwable)
 	 */
 	public void log(ServiceReference sr, int level, String message,
 			Throwable exception) {
 		log(level, message, exception, sr);
 
 	}
 
 	/**
 	 * Add a log listener.
 	 * 
 	 * @param listener
 	 *            the new listener.
 	 * 
 	 * @see org.osgi.service.log.LogReaderService#addLogListener(org.osgi.service.log.LogListener)
 	 */
 	public void addLogListener(LogListener listener) {
 		logListeners.add(listener);
 	}
 
 	/**
 	 * remove a log listener.
 	 * 
 	 * @param listener
 	 *            the listener.
 	 * 
 	 * @see org.osgi.service.log.LogReaderService#removeLogListener(org.osgi.service.log.LogListener)
 	 */
 	public void removeLogListener(LogListener listener) {
 		logListeners.remove(listener);
 	}
 
 	/**
 	 * get the buffered log messages.
 	 * 
 	 * @return an <code>Enumeration</code> over the buffered log messages.
 	 * 
 	 * @see org.osgi.service.log.LogReaderService#getLog()
 	 */
 	public Enumeration getLog() {
 		return logBuffer.elements();
 	}
 
 	/**
 	 * A log entry.
 	 * 
 	 * @author Jan S. Rellermeyer, IKS, ETH Zurich.
 	 * 
 	 */
 	final static class LogEntryImpl implements LogEntry {
 		private int level;
 
 		private String message;
 
 		private ServiceReference sref;
 
 		private Throwable exception;
 
 		private long time;
 
 		private final static List entryRecyclingList = new ArrayList(5);
 
 		private final static int THRESHOLD = 5;
 
 		private static LogEntryImpl getEntry(final int level,
 				final String message, final Throwable throwable,
 				final ServiceReference sref) {
 			synchronized (entryRecyclingList) {
 				LogEntryImpl entry = entryRecyclingList.isEmpty() ? new LogEntryImpl()
 						: (LogEntryImpl) entryRecyclingList.remove(0);
 				entry.log(level, message, throwable, sref);
 				return entry;
 			}
 		}
 
 		private static void releaseEntry(final LogEntryImpl entry) {
 			synchronized (entryRecyclingList) {
 				if (entryRecyclingList.size() < THRESHOLD) {
 					entry.log(0, null, null, null);
 					entryRecyclingList.add(entry);
 				}
 			}
 		}
 
 		/**
 		 * 
 		 */
 		private LogEntryImpl() {
 		}
 
 		/**
 		 * @param sref
 		 * @param level
 		 * @param message
 		 * @param exception
 		 */
 		private void log(final int level, final String message,
 				final Throwable exception, final ServiceReference sref) {
 			this.level = level;
 			this.message = message;
 			this.exception = exception;
 			this.sref = sref;
 			this.time = System.currentTimeMillis();
 		}
 
 		/**
 		 * @see org.osgi.service.log.LogEntry#getBundle()
 		 */
 		public Bundle getBundle() {
 			return sref == null ? null : sref.getBundle();
 		}
 
 		/**
 		 * @see org.osgi.service.log.LogEntry#getServiceReference()
 		 */
 		public ServiceReference getServiceReference() {
 			return sref;
 		}
 
 		/**
 		 * @see org.osgi.service.log.LogEntry#getLevel()
 		 */
 		public int getLevel() {
 			return level;
 		}
 
 		/**
 		 * @see org.osgi.service.log.LogEntry#getMessage()
 		 */
 		public String getMessage() {
 			return message;
 		}
 
 		/**
 		 * @see org.osgi.service.log.LogEntry#getException()
 		 */
 		public Throwable getException() {
 			return exception;
 		}
 
 		/**
 		 * @see org.osgi.service.log.LogEntry#getTime()
 		 */
 		public long getTime() {
 			return time;
 		}
 
 		/**
 		 * 
 		 * @see java.lang.Object#toString()
 		 */
 		public String toString() {
 			StringBuffer buffer = new StringBuffer("[").append(formatDate(time))
 					.append("] [").append(LEVELS[level]).append("] ");
 			if (sref != null) {
 				buffer.append("Bundle: ");
 				buffer.append(sref.getBundle());
 				buffer.append(" ");
 				buffer.append("ServiceReference: ");
 				buffer.append(sref);
 				buffer.append(" ");
 			}
 			buffer.append(message);
 			if (exception != null) {
 				Throwable e = exception;
				buffer.append('\n');
 				// Write stacktrace if available
 				for(int j=0;j<20 && e != null;j++) {
 					buffer.append(e.toString());
 					
 					StackTraceElement[] trace = e.getStackTrace();
 					
 					if(trace == null)
 						buffer.append("(null)\n");
 					else if(trace.length == 0)
 						buffer.append("(no stack trace)\n");
 					else {
 						buffer.append('\n');
 						for(int i=0;i<trace.length;i++) {
 							buffer.append("\tat ");
 							buffer.append(trace[i].toString());
							if (i<(trace.length-1))
								buffer.append('\n');
 						}
 					}
 					
 					Throwable cause = e.getCause();
 					if(cause != e) e = cause;
 					else break;
 				}
 			}
 			return buffer.toString();
 		}
 	}
 
 	static synchronized String formatDate(long time) {
 		myDate.setTime(time);
 		return df.format(myDate);
 	}
 }

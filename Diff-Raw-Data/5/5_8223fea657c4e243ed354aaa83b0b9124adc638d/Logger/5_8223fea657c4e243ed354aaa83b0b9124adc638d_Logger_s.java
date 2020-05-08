 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.log;
 
 import us.exultant.ahs.util.*;
 import java.io.*;
 
 /**
  * Logging level is variable and can be set at runtime. In a single threaded application,
  * putting one of these in a centrally available static field might be advisable;
  * otherwise, in multithreaded contexts, setting up a SyncFreeProvider is advisable.
  */
 public class Logger {
 	public Logger() {
 		this(LEVEL_INFO);
 	}
 	public Logger(int $level) {
 		this($level, System.err);
 	}
 	public Logger(int $level, PrintStream $ps) {
 		this($level, new StandardWriter($ps));
 	}
 	public Logger(int $level, Writer $writer) {
 		set($level);
 		this.$writer = $writer;
 	}
 	/* TODO:AHS:REFACTOR: these convenience methods can no longer exist here because of the modularization requirements.
 	public static Logger logPlainToFile(int $level, File $file) {
 		return new Logger($level, new VapidWriter(IOForge.makePrintStreamNoGuff($file)));
 	}
 	
 	
 	public static SyncFreeProvider<Logger> makeProvider() {
 		return makeProvider(LEVEL_INFO);
 	}
 	public static SyncFreeProvider<Logger> makeProvider(final int $level) {
 		return makeProvider($level, System.err);
 	}
 	public static SyncFreeProvider<Logger> makeProvider(final int $level, final PrintStream $ps) {
 		return makeProvider($level, new StandardWriter($ps));
 	}
 	public static SyncFreeProvider<Logger> makeProvider(final int $level, final Writer $writer) {
 		return new SyncFreeProvider<Logger>(new Factory<Logger>() {
 			public Logger make() {
 				return new Logger($level, $writer);
 			}
 		});
 	}
 	*/
 	
 	
 	
 	
 	/**
 	 * This is used as a guess as to when the program itself started, so for sane
 	 * output hopefully this class is touched by the classloader very early on.
 	 */
 	private static final long	start		= System.currentTimeMillis();
 	
 	/** No logging at all. */
 	public static final int		LEVEL_NONE	= 6;
 	/** Critical errors. The application may no longer work correctly. */
 	public static final int		LEVEL_ERROR	= 5;
 	/** Important warnings. The application will continue to work correctly. */
 	public static final int		LEVEL_WARN	= 4;
 	/** Informative $messages. Typically used for deployment. */
 	public static final int		LEVEL_INFO	= 3;
 	/** Debug $messages. This level is useful during development. */
 	public static final int		LEVEL_DEBUG	= 2;
 	/**
 	 * Trace $messages. A lot of information is logged, so this level is usually only
 	 * needed when debugging a problem.
 	 */
 	public static final int		LEVEL_TRACE	= 1;
 	
 	/**
 	 * The level of $messages that will be logged. Compiling this and the booleans
 	 * below as "final" will cause the compiler to remove all "if (Log.info) ..." type
 	 * statements below the set level.
 	 */
 	private int			$level;
 	private Writer			$writer;
 	
 	/** True when the ERROR level will be logged. */
 	public boolean			ERROR		= $level <= LEVEL_ERROR;
 	/** True when the WARN level will be logged. */
 	public boolean			WARN		= $level <= LEVEL_WARN;
 	/** True when the INFO level will be logged. */
 	public boolean			INFO		= $level <= LEVEL_INFO;
 	/** True when the DEBUG level will be logged. */
 	public boolean			DEBUG		= $level <= LEVEL_DEBUG;
 	/** True when the TRACE level will be logged. */
 	public boolean			TRACE		= $level <= LEVEL_TRACE;
 	
 	public void set(int level) {
 		this.$level = level;
 		ERROR = level <= LEVEL_ERROR;
 		WARN  = level <= LEVEL_WARN;
 		INFO  = level <= LEVEL_INFO;
 		DEBUG = level <= LEVEL_DEBUG;
 		TRACE = level <= LEVEL_TRACE;
 	}
 	
 	public void NONE() {
 		set(LEVEL_NONE);
 	}
 	
 	public void ERROR() {
 		set(LEVEL_ERROR);
 	}
 	
 	public void WARN() {
 		set(LEVEL_WARN);
 	}
 	
 	public void INFO() {
 		set(LEVEL_INFO);
 	}
 	
 	public void DEBUG() {
 		set(LEVEL_DEBUG);
 	}
 	
 	public void TRACE() {
 		set(LEVEL_TRACE);
 	}
 	
 	/**
 	 * Sets the logger that will write the log $messages.
 	 */
 	public void setLogger(Writer logger) {
 		this.$writer = logger;
 	}
 	
 	public void error(Class<?> $category, Throwable $ex) {
 		if (ERROR) $writer.log(LEVEL_ERROR, $category.getCanonicalName(), null, $ex);
 	}
 	
 	public void error(String $message, Throwable $ex) {
 		if (ERROR) $writer.log(LEVEL_ERROR, null, $message, $ex);
 	}
 	
 	public void error(String $category, String $message, Throwable $ex) {
 		if (ERROR) $writer.log(LEVEL_ERROR, $category, $message, $ex);
 	}
 	
 	public void error(String $message) {
 		if (ERROR) $writer.log(LEVEL_ERROR, null, $message, null);
 	}
 	
 	public void error(String $category, String $message) {
 		if (ERROR) $writer.log(LEVEL_ERROR, $category, $message, null);
 	}
 	
 	public void error(Class<?> $category, String $message, Throwable $ex) {
 		if (ERROR) $writer.log(LEVEL_ERROR, $category.getCanonicalName(), $message, $ex);
 	}
 	
 	public void error(Class<?> $category, String $message) {
 		if (ERROR) $writer.log(LEVEL_ERROR, $category.getCanonicalName(), $message, null);
 	}
 	
 	public void error(Object $category, String $message, Throwable $ex) {
 		if (ERROR) $writer.log(LEVEL_ERROR, $category.getClass().getCanonicalName(), $message, $ex);
 	}
 	
 	public void error(Object $category, String $message) {
 		if (ERROR) $writer.log(LEVEL_ERROR, $category.getClass().getCanonicalName(), $message, null);
 	}
 	
 	public void warn(Class<?> $category, Throwable $ex) {
 		if (WARN) $writer.log(LEVEL_WARN, $category.getCanonicalName(), null, $ex);
 	}
 	
 	public void warn(String $message, Throwable $ex) {
 		if (WARN) $writer.log(LEVEL_WARN, null, $message, $ex);
 	}
 	
 	public void warn(String $category, String $message, Throwable $ex) {
 		if (WARN) $writer.log(LEVEL_WARN, $category, $message, $ex);
 	}
 	
 	public void warn(String $message) {
 		if (WARN) $writer.log(LEVEL_WARN, null, $message, null);
 	}
 	
 	public void warn(String $category, String $message) {
 		if (WARN) $writer.log(LEVEL_WARN, $category, $message, null);
 	}
 	
 	public void warn(Class<?> $category, String $message, Throwable $ex) {
 		if (WARN) $writer.log(LEVEL_WARN, $category.getCanonicalName(), $message, $ex);
 	}
 	
 	public void warn(Class<?> $category, String $message) {
 		if (WARN) $writer.log(LEVEL_WARN, $category.getCanonicalName(), $message, null);
 	}
 	
 	public void warn(Object $category, String $message, Throwable $ex) {
 		if (WARN) $writer.log(LEVEL_WARN, $category.getClass().getCanonicalName(), $message, $ex);
 	}
 	
 	public void warn(Object $category, String $message) {
 		if (WARN) $writer.log(LEVEL_WARN, $category.getClass().getCanonicalName(), $message, null);
 	}
 	
 	public void info(Class<?> $category, Throwable $ex) {
 		if (INFO) $writer.log(LEVEL_INFO, $category.getCanonicalName(), null, $ex);
 	}
 	
 	public void info(String $message, Throwable $ex) {
 		if (INFO) $writer.log(LEVEL_INFO, null, $message, $ex);
 	}
 	
 	public void info(String $category, String $message, Throwable $ex) {
 		if (INFO) $writer.log(LEVEL_INFO, $category, $message, $ex);
 	}
 	
 	public void info(String $message) {
 		if (INFO) $writer.log(LEVEL_INFO, null, $message, null);
 	}
 	
 	public void info(String $category, String $message) {
 		if (INFO) $writer.log(LEVEL_INFO, $category, $message, null);
 	}
 	
 	public void info(Class<?> $category, String $message, Throwable $ex) {
 		if (INFO) $writer.log(LEVEL_INFO, $category.getCanonicalName(), $message, $ex);
 	}
 	
 	public void info(Class<?> $category, String $message) {
 		if (INFO) $writer.log(LEVEL_INFO, $category.getCanonicalName(), $message, null);
 	}
 	
 	public void info(Object $category, String $message, Throwable $ex) {
 		if (INFO) $writer.log(LEVEL_INFO, $category.getClass().getCanonicalName(), $message, $ex);
 	}
 	
 	public void info(Object $category, String $message) {
 		if (INFO) $writer.log(LEVEL_INFO, $category.getClass().getCanonicalName(), $message, null);
 	}
 	
 	public void debug(Class<?> $category, Throwable $ex) {
 		if (DEBUG) $writer.log(LEVEL_DEBUG, $category.getCanonicalName(), null, $ex);
 	}
 	
 	public void debug(String $message, Throwable $ex) {
 		if (DEBUG) $writer.log(LEVEL_DEBUG, null, $message, $ex);
 	}
 	
 	public void debug(String $category, String $message, Throwable $ex) {
 		if (DEBUG) $writer.log(LEVEL_DEBUG, $category, $message, $ex);
 	}
 	
 	public void debug(String $message) {
 		if (DEBUG) $writer.log(LEVEL_DEBUG, null, $message, null);
 	}
 	
 	public void debug(String $category, String $message) {
 		if (DEBUG) $writer.log(LEVEL_DEBUG, $category, $message, null);
 	}
 	
 	public void debug(Class<?> $category, String $message, Throwable $ex) {
 		if (DEBUG) $writer.log(LEVEL_DEBUG, $category.getCanonicalName(), $message, $ex);
 	}
 	
 	public void debug(Class<?> $category, String $message) {
 		if (DEBUG) $writer.log(LEVEL_DEBUG, $category.getCanonicalName(), $message, null);
 	}
 	
 	public void debug(Object $category, String $message, Throwable $ex) {
 		if (DEBUG) $writer.log(LEVEL_DEBUG, $category.getClass().getCanonicalName(), $message, $ex);
 	}
 	
 	public void debug(Object $category, String $message) {
 		if (DEBUG) $writer.log(LEVEL_DEBUG, $category.getClass().getCanonicalName(), $message, null);
 	}
 	
 	public void trace(Class<?> $category, Throwable $ex) {
 		if (TRACE) $writer.log(LEVEL_TRACE, $category.getCanonicalName(), null, $ex);
 	}
 	
 	public void trace(String $message, Throwable $ex) {
 		if (TRACE) $writer.log(LEVEL_TRACE, null, $message, $ex);
 	}
 	
 	public void trace(String $category, String $message, Throwable $ex) {
 		if (TRACE) $writer.log(LEVEL_TRACE, $category, $message, $ex);
 	}
 	
 	public void trace(String $message) {
 		if (TRACE) $writer.log(LEVEL_TRACE, null, $message, null);
 	}
 	
 	public void trace(String $category, String $message) {
 		if (TRACE) $writer.log(LEVEL_TRACE, $category, $message, null);
 	}
 	
 	public void trace(Class<?> $category, String $message, Throwable $ex) {
 		if (TRACE) $writer.log(LEVEL_TRACE, $category.getCanonicalName(), $message, $ex);
 	}
 	
 	public void trace(Class<?> $category, String $message) {
 		if (TRACE) $writer.log(LEVEL_TRACE, $category.getCanonicalName(), $message, null);
 	}
 	
 	public void trace(Object $category, String $message, Throwable $ex) {
 		if (TRACE) $writer.log(LEVEL_TRACE, $category.getClass().getCanonicalName(), $message, $ex);
 	}
 	
 	public void trace(Object $category, String $message) {
 		if (TRACE) $writer.log(LEVEL_TRACE, $category.getClass().getCanonicalName(), $message, null);
 	}
 	
 	
 	
 	
 	public static interface Writer {
 		public void log(int $level, String $category, String $message, Throwable $e);
 	}	
 	
 	/**
 	 * Good for human reading; pretty mediocre for machine parsing. Prints a time in
 	 * minute and seconds since logger start, then the log level as a string, then the
 	 * thread id, then the category in brackets, then the message, and the stack trace
 	 * of an exception on the following line.
 	 */
 	public static class StandardWriter implements Writer {
 		public StandardWriter(PrintStream $ps) {
 			this.$ps = $ps;
 		}
 		
 		private PrintStream $ps;
 		
 		public void log(int $level, String $category, String $message, Throwable $e) {
 			StringBuilder $sb = new StringBuilder(256);	// i'd love to allocate this per class instead of per call, but I think i'd have to strengthen the admonishments against multithreaded use of loggers quite a bit if i were to do that.  as it stands now, this is actually reentrant, and that's probably a good thing for the default.
 			long $time = System.currentTimeMillis() - start;
 			long $seconds = $time / 1000;
 			long $milliseconds = $time - $seconds * 1000;
			$sb.append(Strings.padLeftToWidth($seconds+"", "0", 3));
 			$sb.append('.');
			$sb.append(Strings.padRightToWidth($milliseconds+"", "0", 3));
 			
 			switch ($level) {
 				case LEVEL_ERROR:
 					$sb.append(" ERROR: ");
 					break;
 				case LEVEL_WARN:
 					$sb.append("  WARN: ");
 					break;
 				case LEVEL_INFO:
 					$sb.append("  INFO: ");
 					break;
 				case LEVEL_DEBUG:
 					$sb.append(" DEBUG: ");
 					break;
 				case LEVEL_TRACE:
 					$sb.append(" TRACE: ");
 					break;
 			}
 			
 			$sb.append("thid").append(Thread.currentThread().getId()).append(' ');
 			
 			if ($category != null) {
 				$sb.append('[');
 				$sb.append($category);
 				$sb.append("] ");
 			}
 			
 			if ($message != null) {
 				$sb.append($message).append('\n');
 			}
 			
 			if ($e != null) {
 				$sb.append(X.toString($e));
 			}
 			
 			$ps.print($sb.toString());
 		}
 	}
 	
 	
 	//XXX:AHS:LOG: make a writer that outputs json.  it'd be my fav -- one json object per line is an epicly awesome combination of grep'able and machine parsable and still readable.
 	
 
 	/**
 	 * Writes nothing but the message (and/or exception, since those are bedfellows)
 	 * -- no category, timestamp, level, thread-id, nothing. For when you want
 	 * something that fits the Logger interface but is dead-simple.
 	 */
 	public static class VapidWriter implements Writer {
 		public VapidWriter(PrintStream $ps) {
 			this.$ps = $ps;
 		}
 		
 		private PrintStream $ps;
 		
 		public void log(int $level, String $category, String $message, Throwable $e) {
 			StringBuilder $sb = new StringBuilder(256);
 			
 			if ($message != null) {
 				$sb.append($message).append('\n');
 			}
 			
 			if ($e != null) {
 				$sb.append(X.toString($e));
 			}
 			
 			$ps.print($sb.toString());
 		}
 	}
 }

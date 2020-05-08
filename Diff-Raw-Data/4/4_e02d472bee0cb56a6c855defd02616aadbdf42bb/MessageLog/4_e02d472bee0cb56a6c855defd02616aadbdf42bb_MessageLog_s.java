 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 
 import com.nginious.http.server.LogOutputConsumer;
 
 /**
  * A message log for writing informational messages to a log file. A log message has a log level
  * identified by {@link MessageLevel}. Log entries are queued when added. A separate thread takes
  * log entries from the queue and writes them to the log file.
  * 
  * <p>
  * The maximum queue size is 1000 entries. When this limit is reached callers block until a slot in the 
  * queue is available.
  * </p>
  * 
  * <p>
  * A message log has a log level where all messages with the same numeric log level or lower are written
  * to the log file. Any message with a higher log level are filtered.
  * </p>
  * 
  * <p>
  * A log entry in the message log has the following format
  * 
  * <pre>
  * yyyy-MM-dd HH:mm:ss.S [level] [module] message
  * </pre>
  * </p>
  * 
  * <p>
  * The log file is rotated at 12:00 am where the old log file is renamed to "server-yyyy-MM-dd.log" and
  * a new file with the name "server.log" is created for writing. A history of 5 log files is kept.
  * </p>
  *
  * @author Bojan Pisler, NetDigital Sweden AB
  *
  */
 public class MessageLog {
 	
 	private static MessageLog log = null;
 	
 	private LogOutputConsumer consumer;
 	
 	private MessageLevel level;
 	
 	/**
 	 * Constructs a new message log with the specified log level.
 	 * 
 	 * @param level the log level
 	 */
 	private MessageLog(MessageLevel level) {
 		super();
 		this.level = level;
 	}
 	
 	/**
 	 * Sets this message logs output consumer to the specified consumer.
 	 * 
 	 * @param consumer the consumer
 	 */
 	void setConsumer(LogOutputConsumer consumer) {
 		this.consumer = consumer;
 	}
 	
 	/**
 	 * Returns the only message log instance. The message log is created with a default message level
 	 * of {@link MessageLevel#EVENT} on first call.
 	 * 
 	 * @return the message log
 	 */
 	public static MessageLog getInstance() {
 		if(log == null) {
 			log = new MessageLog(MessageLevel.EVENT);
 		}
 		
 		return log;
 	}
 	
 	/**
 	 * Sets message log level to the specified level. Any added log entries with a higher numeric message
 	 * log level are filtered and not written to the log file.
 	 * 
 	 * @param level the message log level
 	 */
 	void setLevel(MessageLevel level) {
 		this.level = level;
 	}
 	
 	/**
 	 * Opens the message log for writing.
 	 * 
 	 * @throws IOException if unable to open message log
 	 */
 	void open() throws IOException {
 		if(this.consumer == null) {
 			this.consumer = new FileLogConsumer("logs/server"); 
 		}
 		
 		consumer.start();
 	}
 	
 	/**
 	 * Closes the message log. Waits for any log entries to be written before closing.
 	 * 
 	 * @throws IOException if unable to close message log
 	 */
 	void close() throws IOException {
		consumer.stop();
 	}
 	
 	/**
 	 * Adds the specified message from the specified module with message level error for writing
 	 * to the message log.
 	 * 
 	 * @param module the module
 	 * @param message the message to write
 	 */
 	public void error(String module, String message) {
 		log(module, MessageLevel.ERROR, message);
 	}
 	
 	/**
 	 * Adds the specified exception from the specified module at message level error for writing
 	 * to the message log.
 	 * 
 	 * @param module the module
 	 * @param t the exception to write stack trace for in log
 	 */
 	public void error(String module, Throwable t) {
 		StringWriter writer = new StringWriter();
 		PrintWriter printer = new PrintWriter(writer);
 		printer.println(t.getMessage());
 		t.printStackTrace(printer);
 		log(module, MessageLevel.ERROR, writer.toString());
 	}
 	
 	/**
 	 * Adds the specified message from the specified module at message level warning for writing
 	 * to the message log.
 	 * 
 	 * @param module the module
 	 * @param message the message to write
 	 */
 	public void warn(String module, String message) {
 		log(module, MessageLevel.WARN, message);
 	}
 	
 	/**
 	 * Adds the specified exception from the specified module at message level warning for writing
 	 * to the message log.
 	 * 
 	 * @param module the module
 	 * @param t the exception to write stack trace for in log
 	 */
 	public void warn(String module, Throwable t) {
 		StringWriter writer = new StringWriter();
 		PrintWriter printer = new PrintWriter(writer);
 		printer.println(t.getMessage());
 		t.printStackTrace(printer);
 		log(module, MessageLevel.WARN, writer.toString());
 	}
 	
 	/**
 	 * Adds the specified message from the specified module at message level info for writing
 	 * to the message log.
 	 * 
 	 * @param module the module
 	 * @param message the message to write
 	 */
 	public void info(String module, String message) {
 		log(module, MessageLevel.INFO, message);
 	}
 	
 	/**
 	 * Adds the specified message from the specified module at message level event for writing
 	 * to the message log.
 	 * 
 	 * @param module the module
 	 * @param message the message to write
 	 */
 	public void event(String module, String message) {
 		log(module, MessageLevel.EVENT, message);
 	}
 	
 	/**
 	 * Adds the specified message from the specified module at message level trace for writing
 	 * to the message log.
 	 * 
 	 * @param module the module
 	 * @param message the message to write
 	 */
 	public void trace(String module, String message) {
 		log(module, MessageLevel.TRACE, message);
 	}
 	
 	/**
 	 * Adds the specified message from the specified module at message level debug for writing
 	 * to the message log.
 	 * 
 	 * @param module the module
 	 * @param message the message to write
 	 */
 	public void debug(String module, String message) {
 		log(module, MessageLevel.DEBUG, message);
 	}
 	
 	/**
 	 * Adds a log message from the specified module at the specified message level with the specified message.
 	 * 
 	 * @param module the module
 	 * @param level the message level
 	 * @param message the message to write
 	 */
 	public void log(String module, MessageLevel level, String message) {
 		if(level.getLevel() > this.level.getLevel()){
 			return;
 		}
 		
 		StringBuffer line = new StringBuffer();
 
 		// datetime yyyy-MM-dd HH:mm:ss.S
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
 		line.append(format.format(System.currentTimeMillis()));
 		
 		// level
 		line.append(" [");
 		line.append(level);
 		
 		// module
 		line.append("] [");
 		line.append(module);
 		line.append("]");
 		
 		// message
 		line.append(" ");
 		line.append(message);
 		
 		line.append("\n");
 		
 		byte[] outLine = line.toString().getBytes();
 		consumer.consume(outLine);
 	}
 }

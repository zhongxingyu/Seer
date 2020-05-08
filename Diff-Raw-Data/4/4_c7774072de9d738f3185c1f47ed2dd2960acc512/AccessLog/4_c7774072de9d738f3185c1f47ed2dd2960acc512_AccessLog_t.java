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
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 
 import com.nginious.http.HttpStatus;
 import com.nginious.http.server.LogOutputConsumer;
 
 /**
  * A HTTP access log which logs HTTP requests in combined log format. Log entries are queued when added.
  * A separate thread takes log entries from the queue and writes them to the log file.
  * 
  * <p>
  * The maximum queue size is 1000 entries. When this limit is reached callers block until a slot in the 
  * queue is available.
  * </p>
  * 
  * <p>
  * The log file is rotated at 12:00 am where the old log file is renamed to "access-yyyy-MM-dd.log" and
  * a new file with the name "access.log" is created for writing. A history of 5 log files is kept.
  * </p>
  * 
  * @author Bojan Pisler, NetDigital Sweden AB
  *
  */
 class AccessLog {
 	
 	private static MessageLog log = MessageLog.getInstance();
 	
 	private LogOutputConsumer consumer;
 	
 	/**
 	 * Constructs a new access log
 	 */
 	AccessLog() {
 		super();
 	}
 	
 	/**
 	 * Sets this access logs output consumer to the specified consumer.
 	 * 
 	 * @param consumer the consumer
 	 */
 	void setConsumer(LogOutputConsumer consumer) {
 		this.consumer = consumer;
 	}
 	
 	/**
 	 * Opens the access log for writing.
 	 * 
 	 * @throws IOException if unable to open access log
 	 */
 	void open() throws IOException {
 		if(this.consumer == null) {
 			this.consumer = new FileLogConsumer("logs/access");
 		}
 		
 		consumer.start();
 	}
 	
 	/**
 	 * Closes the access log. Waits for any log entries to be written before closing.
 	 * 
 	 * @throws IOException if unable to close access log
 	 */
 	void close() throws IOException {
		if(consumer != null) {
			consumer.stop();
		}
 	}
 	
 	/**
 	 * Queues a combined log entry with the specified remote IP, request time, request URI, status, response bytes, referer and agent
 	 * for writing.
 	 * 
 	 * @param remoteIp remote IP of the HTTP request
 	 * @param requestTimeMillis request time for the HTTP request
 	 * @param request request URI
 	 * @param status status code for request
 	 * @param responseBytes number of bytes returned in response for request
 	 * @param referer request referer
 	 * @param agent request agent
 	 */
 	void write(String remoteIp, long requestTimeMillis, String request, HttpStatus status, int responseBytes, String referer, String agent) {
 		StringBuffer line = new StringBuffer();
 		line.append(remoteIp); // Remote IP
 		line.append(" - - ["); // RFC 931 username
 		
 		// datetime dd/MMM/yyyy:HH:mm:ss Z
 		SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
 		line.append(format.format(requestTimeMillis));
 
 		line.append("] \"");
 		line.append(request); // Request
 		line.append("\" ");
 		line.append(status.getStatusCode()); // Status
 		line.append(" ");
 		line.append(responseBytes); // Response bytes
 		
 		if(referer != null) { // Referer
 			line.append(" \"");
 			line.append(referer);
 			line.append("\" ");
 		} else {
 			line.append(" \"-\"");
 		}
 		
 		if(agent != null) { // Agent
 			line.append(" \"");
 			line.append(agent);
 			line.append("\"");
 		} else {
 			line.append(" \"-\"");
 		}
 		
 		line.append(" \"-\""); // Cookie
 		line.append("\n");
 		
 		
 		try {
 			byte[] outLine = line.toString().getBytes("iso-8859-1");
 			consumer.consume(outLine);
 		} catch(UnsupportedEncodingException e) {
 			log.warn("Http", e);
 		}
 	
 	}	
 }

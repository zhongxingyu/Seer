 /*
  * Copyright (c) 2012, someone All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1.Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2.Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3.Neither the name of the Happyelements Ltd. nor the
  * names of its contributors may be used to endorse or promote products derived
  * from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.happyelements.hive.web;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.RollingFileAppender;
 
 /**
  * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
  *
  */
 public class Starter {
 
 	private static final Log LOGGER = LogFactory.getLog(Starter.class);
 
 	public static void initializeLogSystem(String log) throws IOException {
 		checkAndCreate(log);
 		Logger logger = Logger.getRootLogger();
 		logger.setLevel(Level.INFO);
 		logger.removeAllAppenders();
 		if (log != null) {
 			RollingFileAppender appender = new RollingFileAppender(
					new PatternLayout("%d [%t] %-5p %c [%x] - %m%n"), log);
			appender.setFile(new File(log, "log.log").getPath());
 			appender.setImmediateFlush(false);
 			appender.setMaxBackupIndex(10);
 			appender.setMaxFileSize("100MB");
 			logger.addAppender(appender);
 		} else {
 			logger.addAppender(new ConsoleAppender(new PatternLayout(
 					"%d [%t] %-5p %c [%x] - %m%n")));
 		}
 	}
 
 	public static void checkAndCreate(String path) throws IOException {
 		File file = new File(path);
 		if (file.exists()) {
 			if (!file.isDirectory()) {
 				throw new IOException(path + " is not directory");
 			}
 		} else if (!file.mkdirs()) {
 			throw new IOException("fail to create path:" + path);
 		}
 	}
 
 	public static void main(String[] args) {
 		try {
 			if (args.length != 3) {
 				System.out
 						.println("Usage ${hadoop} jar ${jar} ${static_root} ${log_root} ${port}");
 				return;
 			}
 
 			initializeLogSystem(args[0]);
 			LOGGER.info("initialize log system done");
 			LOGGER.info("starting http server at port:" + args[2]
 					+ " staticfiles:" + args[2] + " log_root:" + args[0]);
 			new HTTPServer(args[1], Integer.parseInt(args[2], 10)).start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }

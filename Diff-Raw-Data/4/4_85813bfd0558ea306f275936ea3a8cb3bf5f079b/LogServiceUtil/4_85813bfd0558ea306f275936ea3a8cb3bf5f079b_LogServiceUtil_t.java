 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
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
  *******************************************************************************/
 package com.buglabs.util;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogReaderService;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.osgi.log.LogServiceImpl;
 
 /**
  * Utility functions relating to OSGi log service.
  * 
  * @author kgilmer
  * 
  */
 public class LogServiceUtil {
 
 	/**
 	 * @param context
 	 *            if context is null, it will return a log service that uses
 	 *            stdout.
 	 * @return Either the first LogService available in the runtime if available
 	 *         or a SysoutLogService.
 	 */
 	public static LogService getLogService(BundleContext context) {
 		final LogService logService;
 		// See if LogService is available.
 		// also protect against context being null
 		ServiceReference sr = null;
 		if (context != null)
 			sr = context.getServiceReference(LogService.class.getName());
 
 		if (sr != null) {
 			logService = (LogService) context.getService(sr);
 		} else {
 			LogServiceImpl logServiceImpl = new LogServiceImpl(context);
 			logService = (LogService) logServiceImpl;
 
 			//We will register a log service and just let it dangle out there for the lifetime of the OSGi framework instance.
 			context.registerService(LogService.class.getName(), logServiceImpl, getLogServiceProperties(logServiceImpl));
 			context.registerService(LogReaderService.class.getName(), logServiceImpl, getLogServiceProperties(logServiceImpl));
 		}
 
 		return logService;
 	}
 
 	
 	/**
 	 * @return Properties associated with this service implementation.
 	 */
 	private static Dictionary getLogServiceProperties(LogServiceImpl logServiceImpl) {
 		Hashtable dict = new Hashtable();
 		dict.put("Implementation", LogServiceImpl.class.getName());
 		dict.put("Log Level", "" + logServiceImpl.logLevel);
 		dict.put("Quiet", "" + logServiceImpl.quiet);
		dict.put("Output Stream", LogServiceImpl.out.getClass().getName());
		dict.put("Error Stream", LogServiceImpl.err.getClass().getName());
 		dict.put("Buffer Size", "" + logServiceImpl.bufferSize);
 		dict.put(Constants.SERVICE_RANKING, -4096);
 		return dict;
 	}
 
 	/**
 	 * Log a bundle exception and print nested exception if it exists.
 	 * 
 	 * @param logService
 	 * @param message
 	 * @param exception
 	 */
 	public static void logBundleException(LogService logService, String message, BundleException exception) {
 		// Add error handling to be specific about what exactly happened.
 		logService.log(LogService.LOG_ERROR, message + ": " + exception.getMessage() + "\n" + stackTraceToString(exception));
 		stackTraceToString(exception);
 
 		if (exception.getNestedException() != null) {
 			logService.log(LogService.LOG_ERROR, "Nested Exception: " + exception.getNestedException().getMessage() + "\n" + stackTraceToString(exception.getNestedException()));
 		}
 	}
 
 	/**
 	 * Log an exception and print nested exception if it exists.
 	 * 
 	 * @param logService
 	 * @param message
 	 * @param exception
 	 */
 	public static void logBundleException(LogService logService, String message, Exception exception) {
 		// Add error handling to be specific about what exactly happened.
 		logService.log(LogService.LOG_ERROR, message + ": " + exception.getMessage() + "\n" + stackTraceToString(exception));
 	}
 
 	/**
 	 * @param t
 	 * @return A stack trace as a string.
 	 */
 	private static String stackTraceToString(Throwable t) {
 		StringWriter sw = new StringWriter();
 		t.printStackTrace(new PrintWriter(sw));
 		return sw.getBuffer().toString();
 	}
 }

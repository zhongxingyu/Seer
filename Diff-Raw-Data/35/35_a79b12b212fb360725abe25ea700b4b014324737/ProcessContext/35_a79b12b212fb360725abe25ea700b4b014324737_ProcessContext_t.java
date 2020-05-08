 // The Grinder
// Copyright (C) 2000, 2001  Paco Gomez
// Copyright (C) 2000, 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.util;
 
 import java.io.BufferedOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.util.Date;
 
 import net.grinder.plugininterface.Logger;
 import net.grinder.plugininterface.PluginProcessContext;
 import net.grinder.util.FilenameFactory;
 import net.grinder.util.GrinderProperties;
 
 
 /**
 * Currently each thread owns its own instance of
 * ProcessContextImplementation (or derived class), so we don't need
 * to worry about thread safety.
 *
  * @author Philip Aston
  * @version $Revision$
  */
 public class ProcessContextImplementation implements PluginProcessContext
 {
    private final StringBuffer m_buffer = new StringBuffer();
    private final DateFormat m_dateFormat =
 	DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
 
     private final GrinderProperties m_pluginParameters;
     private final String m_hostIDString;
     private final String m_processIDString;
     private final boolean m_logProcessStreams;
     private final PrintWriter m_outputWriter;
     private final PrintWriter m_errorWriter;
     private final boolean m_appendToLog;
 
     private final FilenameFactory m_filenameFactory;
 
     {
 	final GrinderProperties properties =
 	    GrinderProperties.getProperties();	
 
 	m_logProcessStreams =
 	    properties.getBoolean("grinder.logProcessStreams", true);
 
 	m_appendToLog = properties.getBoolean("grinder.appendLog", false);
     }
     
     protected ProcessContextImplementation(PluginProcessContext processContext,
 					   String threadID)
     {
 	m_pluginParameters = processContext.getPluginParameters();
 	m_hostIDString = processContext.getHostIDString();
 	m_processIDString = processContext.getProcessIDString();
 	m_outputWriter = processContext.getOutputLogWriter();
 	m_errorWriter = processContext.getErrorLogWriter();
 
 	m_filenameFactory = new FilenameFactory(m_hostIDString,
 						m_processIDString, threadID);
     }
 
     public ProcessContextImplementation(String hostIDString,
 					String processIDString)
 	throws GrinderException
     {
 	m_hostIDString = hostIDString;
 	m_processIDString = processIDString;
 
 	final GrinderProperties properties = GrinderProperties.getProperties();
 
 	m_pluginParameters =
 	    properties.getPropertySubset("grinder.plugin.parameter.");
 
 	m_filenameFactory = new FilenameFactory(m_hostIDString,
 						m_processIDString, null);
 
 	try {
 	    m_outputWriter =
 		new PrintWriter(
 		    new BufferedOutputStream(
 			new FileOutputStream(
 			    m_filenameFactory.createFilename("out"),
 			    m_appendToLog)),
 		    true);
 
 	    m_errorWriter =
 		new PrintWriter(
 		    new BufferedOutputStream(
 			new FileOutputStream(
 			    m_filenameFactory.createFilename("error"),
 			    m_appendToLog)),
 		    true);
 	}
 	catch (FileNotFoundException e) {
 	    throw new GrinderException("Could not create output streams", e);
 	}
     }
 
     public String getHostIDString()
     {
 	return m_hostIDString;
     }
     
     public String getProcessIDString()
     {
 	return m_processIDString;
     }
 
     public FilenameFactory getFilenameFactory()
     {
 	return m_filenameFactory;
     }
 
     public GrinderProperties getPluginParameters()
     {
 	return m_pluginParameters;
     }
 
     public boolean getAppendToLog()
     {
 	return m_appendToLog;
     }
 
     public void logMessage(String message)
     {
 	logMessage(message, Logger.LOG);
     }
 
     public void logMessage(String message, int where)
     {
 	if (!m_logProcessStreams) {
 	    where &= ~Logger.LOG;
 	}
 
 	if (where != 0) {
 	    final String s = formatMessage(message);
 
 	    if ((where & Logger.LOG) != 0) {
 		m_outputWriter.println(s);
 	    }
 
 	    if ((where & Logger.TERMINAL) != 0) {
 		System.out.println(s);
 	    }
 	}
     }
 
     public void logError(String message)
     {
 	logError(message, Logger.LOG);
     }
     
     public void logError(String message, int where) 
     {
 	if (!m_logProcessStreams) {
 	    where &= ~Logger.LOG;
 	}
 
 	if (where != 0) {
 	    final String s = formatMessage(message);
 
 	    if ((where & Logger.LOG) != 0) {
 		m_errorWriter.println(s);
 	    }
 
 	    if ((where & Logger.TERMINAL) != 0) {
 		System.err.println(s);
 	    }
 
 	    final int summaryLength = 20;
 
 	    final String summary = 
 		message.length() > summaryLength ?
 		message.substring(0, summaryLength) + "..." : message;
 
 	    logMessage("ERROR (\"" + summary +
 		       "\"), see error log for details",
 		       where);
 	}
     }
 
     public PrintWriter getOutputLogWriter()
     {
 	return m_outputWriter;
     }
 
     public PrintWriter getErrorLogWriter()
     {
 	return m_errorWriter;
     }
 
     private String formatMessage(String message)
     {
	// Each ProcessContextImplementation is used by at most one
	// thread, so we can safely reuse our StringBuffer and
	// DateFormat.
	m_buffer.delete(0, m_buffer.length());
 
	m_buffer.append(m_dateFormat.format(new Date()));
	m_buffer.append(": ");
 
	appendMessageContext(m_buffer);
 
	m_buffer.append(message);
 
	return m_buffer.toString();
     }
 
     protected void appendMessageContext(StringBuffer buffer)
     {
 	buffer.append("Grinder Process (Host ");
 	buffer.append(getHostIDString());
 	buffer.append(" JVM ");
 	buffer.append(getProcessIDString());
 	buffer.append(") ");
     }
 }

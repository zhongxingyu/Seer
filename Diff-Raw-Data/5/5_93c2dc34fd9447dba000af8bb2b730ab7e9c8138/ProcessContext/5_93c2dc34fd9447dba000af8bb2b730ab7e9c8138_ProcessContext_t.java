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
 
 package net.grinder.engine.process;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.util.Date;
 
 import net.grinder.common.FilenameFactory;
 import net.grinder.common.GrinderException;
 import net.grinder.common.GrinderProperties;
 import net.grinder.common.Logger;
 import net.grinder.plugininterface.PluginProcessContext;
 
 
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
     private static final PrintWriter s_stdoutWriter;
     private static final PrintWriter s_stderrWriter;
     private static final String s_lineSeparator =
 	System.getProperty("line.separator");
     private static final int s_lineSeparatorLength = s_lineSeparator.length();
     private static final DateFormat s_dateFormat =
 	DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
     private static long s_nextTime = System.currentTimeMillis();
     private static String s_dateString;
 
     static
     {
 	s_stdoutWriter = new PrintWriter(System.out);
 	s_stderrWriter = new PrintWriter(System.err);
     }
 
     private synchronized static String getDateString()
     {
 	final long now = System.currentTimeMillis();
 	
 	if (now > s_nextTime) {
 	    s_nextTime = now + 1000;
 	    s_dateString = s_dateFormat.format(new Date());
 	}
 
 	return s_dateString;
     }
 
     // Each ProcessContextImplementation is used by at most one
     // thread, so we can reuse the following objects.
     private final StringBuffer m_buffer = new StringBuffer();
     private final char[] m_outputLine = new char[512];
 
     private final String m_grinderID;
     private final GrinderProperties m_properties;
     private final boolean m_logProcessStreams;
     private final boolean m_appendToLog;
     private final GrinderProperties m_pluginParameters;
     private final PrintWriter m_outputWriter;
     private final PrintWriter m_errorWriter;
 
     private final FilenameFactoryImplementation m_filenameFactory;
 
     protected ProcessContextImplementation(
 	ProcessContextImplementation processContext, String contextSuffix)
     {
 	m_grinderID = processContext.m_grinderID;
 	m_properties = processContext.m_properties;
 	m_logProcessStreams = processContext.m_logProcessStreams;
 	m_appendToLog = processContext.m_appendToLog;
 	m_pluginParameters = processContext.m_pluginParameters;
 	m_outputWriter = processContext.m_outputWriter;
 	m_errorWriter = processContext.m_errorWriter;
 
 	m_filenameFactory =
 	    new FilenameFactoryImplementation(
 		processContext.m_filenameFactory.getLogDirectory(),
 		contextSuffix);
     }
 
     public ProcessContextImplementation(String grinderID,
 					GrinderProperties properties)
 	throws GrinderException
     {
 	m_grinderID = grinderID;
 	m_properties = properties;
 
 	m_logProcessStreams =
 	    properties.getBoolean("grinder.logProcessStreams", true);
 
 	m_appendToLog = properties.getBoolean("grinder.appendLog", false);
 
 	m_pluginParameters =
 	    properties.getPropertySubset("grinder.plugin.parameter.");
 	    
 	final File logDirectory =
 	    new File(properties.getProperty("grinder.logDirectory", "."), "");
 
 	try {
 	    logDirectory.mkdirs();
 	}
 	catch (Exception e) {
 	    throw new GrinderException(e.getMessage(), e);
 	}
 
 	if (!logDirectory.canWrite()) {
 	    throw new GrinderException("Cannot write to log directory '" +
 				       logDirectory.getPath() + "'");
 	}
 
 	m_filenameFactory = new FilenameFactoryImplementation(logDirectory);
 
 	m_outputWriter = createStream("out");
 	m_errorWriter = createStream("error");
     }
 
     private PrintWriter createStream(String prefix)
 	throws GrinderException
     {
 	final File file = new File(m_filenameFactory.createFilename(prefix));
 
 	// Check we can write to the file and moan now. We won't see
 	// the problem later because PrintWriters eat exceptions. If
 	// the file doesn't exist, we're pretty sure we can create it
 	// because we checked we can write to the log directory.
 	if (file.exists() && !file.canWrite()) {
 	    throw new GrinderException("Cannot write to '" + file.getPath() +
 				       "'");
 	}
 
 	// Although we manage the flushing ourselves and don't call
 	// printn, we set auto flush on our PrintWriters because
 	// clients can get direct access to them.
 	return
 	    new PrintWriter(new DelayedCreationFileOutputStream(file,
 								m_appendToLog),
 			    true);
     }
 
     public String getGrinderID()
     {
 	return m_grinderID;
     }
 
     public FilenameFactory getFilenameFactory()
     {
 	return m_filenameFactory;
     }
 
     public GrinderProperties getProperties()
     {
 	return m_properties;
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
 	    final int lineLength = formatMessage(message);
 
 	    if ((where & Logger.LOG) != 0) {
 		m_outputWriter.write(m_outputLine, 0, lineLength);
 		m_outputWriter.flush();
 	    }
 
 	    if ((where & Logger.TERMINAL) != 0) {
 		s_stdoutWriter.write(m_outputLine, 0, lineLength);
 		s_stdoutWriter.flush();
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
 	    final int lineLength = formatMessage(message);
 
 	    if ((where & Logger.LOG) != 0) {
 		m_errorWriter.write(m_outputLine, 0, lineLength);
 		m_errorWriter.flush();
 	    }
 
 	    if ((where & Logger.TERMINAL) != 0) {
 		s_stderrWriter.write(m_outputLine, 0, lineLength);
 		s_stderrWriter.flush();
 	    }
 
 	    final int summaryLength = 20;
 
 	    final String summary = 
 		message.length() > summaryLength ?
 		message.substring(0, summaryLength) + "..." : message;
 
 	    logMessage("ERROR (\"" + summary +
 		       "\"), see error log for details",
 		       Logger.LOG);
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
 
     private int formatMessage(String message)
     {
 	m_buffer.setLength(0);
 
 	m_buffer.append(getDateString());
 	m_buffer.append(": ");
 
 	appendMessageContext(m_buffer);
 
 	m_buffer.append(message);
 
 	// Sadly this is the most efficient way to get something we
 	// can println from the StringBuffer. getString() creates an
 	// extra string, getValue() is package scope.
	final int bufferLength = m_buffer.length();
 	final int outputLineSpace =
 	    m_outputLine.length - s_lineSeparatorLength;
 	
 	final int lineLength =
	    bufferLength > outputLineSpace ? outputLineSpace : bufferLength;
 
 	m_buffer.getChars(0, lineLength, m_outputLine, 0);
 	s_lineSeparator.getChars(0, s_lineSeparatorLength, m_outputLine,
 				 lineLength);
 
 	return lineLength + s_lineSeparatorLength;
     }
 
     protected void appendMessageContext(StringBuffer buffer)
     {
 	buffer.append("Grinder Process (");
 	buffer.append(m_grinderID);
 	buffer.append(") ");
     }
 
     private final class FilenameFactoryImplementation
 	implements FilenameFactory
     {
 	private final File m_logDirectory;
 	private final String m_contextString;
 
 	public FilenameFactoryImplementation(File logDirectory)
 	{
 	    this(logDirectory, null);
 	}
 
 	public FilenameFactoryImplementation(File logDirectory,
 					     String subContext)
 	{
 	    m_logDirectory = logDirectory;
 
 	    m_contextString =
 		"_" + m_grinderID +
 		(subContext != null ? "_" + subContext : "");
 	}
 
 	public final File getLogDirectory()
 	{
 	    return m_logDirectory;
 	}
 
 	public final String createFilename(String prefix, String suffix)
 	{
 	    final StringBuffer result = new StringBuffer();
 
 	    result.append(m_logDirectory);
 	    result.append(File.separator);
 	    result.append(prefix);
 	    result.append(m_contextString);
 	    result.append(suffix);
 
 	    return result.toString();
 	}
 
 	public final String createFilename(String prefix)
 	{
 	    return createFilename(prefix, ".log");
 	}
     }
 }

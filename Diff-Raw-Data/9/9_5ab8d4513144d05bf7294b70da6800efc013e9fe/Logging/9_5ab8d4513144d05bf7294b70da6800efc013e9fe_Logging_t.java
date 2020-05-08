 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2008 Brett Wilson.
  * All rights reserved.
  */
 
 package org.wwscc.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.MessageFormat;
 import java.util.Date;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileSystemView;
 
 /**
  */
 public class Logging
 {
 	public static void logSetup(String name) throws IOException
 	{
 		LogManager lm = LogManager.getLogManager();
 		Logger root = lm.getLogger("");
 		Logger wwscc = Logger.getLogger("org.wwscc");
 		SingleLineFormatter formatter = new SingleLineFormatter();
 
 		ConsoleHandler ch = new ConsoleHandler();
 		ch.setFormatter(formatter);
 		ch.setLevel(Level.ALL);
 
 		File[] roots = FileSystemView.getFileSystemView().getRoots();
 		String prefix;
 
 		if (roots.length > 0)
 		{
 			prefix = roots[0].getPath();
 			if (prefix.equals("/"))
 				prefix = "%h";
 		}
 		else
 			prefix = "%h";
 
 
 		FileHandler fh = new FileHandler(prefix+"/"+name+".%g.log", 1000000, 10, true);
 		fh.setFormatter(formatter);
 		fh.setLevel(Level.ALL);
 
 		AlertHandler ah = new AlertHandler();
 		ah.setLevel(Level.WARNING);
 
 		root.addHandler(ah);
 		root.addHandler(fh);
 		root.addHandler(ch);
 
 		root.setLevel(Level.WARNING);
 		wwscc.setLevel(Level.FINER);
 	}
 
 	public static class AlertHandler extends Handler
 	{
 		public AlertHandler()
 		{
 			LogManager manager = LogManager.getLogManager();
 			Level l;
 			try
 			{
 				String val = manager.getProperty(getClass().getName() + ".level");
 				l = Level.parse(val.trim());
 			}
 			catch (Exception e)
 			{
 				l = Level.SEVERE;
 			}
 			setLevel(l);
 			setFormatter(new SimpleFormatter());
 		}
 
 		public void publish(LogRecord logRecord)
 		{
 			if (isLoggable(logRecord))
 			{
 				int type;
 				String title;
 
 				int val = logRecord.getLevel().intValue();
 				if (val >= Level.SEVERE.intValue())
 				{
 					title = "Error";
 					type = JOptionPane.ERROR_MESSAGE;
 				}
 				else if (val >= Level.WARNING.intValue())
 				{
 					title = "Warning";
 					type = JOptionPane.WARNING_MESSAGE;
 				}
 				else
 				{
 					title = "Note";
 					type = JOptionPane.INFORMATION_MESSAGE;
 				}
 
 				String record = getFormatter().formatMessage(logRecord);
 				if (record.contains("\n"))
					record = "<HTML>" + record.replace("\n", "<br>") + "</HTML>";
 				JOptionPane.showMessageDialog(null, record, title, type);
 			}
 		}
 
 		public void flush() {}
 		public void close() {}
 
 	}
 
 	public static class SingleLineFormatter extends Formatter
 	{
 		Date dat = new Date();
 		private final static String format = "{0,time}";
 		private MessageFormat formatter;
 
 		private Object args[] = new Object[1];
 
 		/**
 		 * Format the given LogRecord.
 		 * @param record the log record to be formatted.
 		 * @return a formatted log record
 		 */
 		@Override
 		public synchronized String format(LogRecord record)
 		{
 			StringBuffer sb = new StringBuffer();
 
 			// Minimize memory allocations here.
 			dat.setTime(record.getMillis());
 			args[0] = dat;
 			StringBuffer text = new StringBuffer();
 			if (formatter == null) {
 				formatter = new MessageFormat(format);
 			}
 			formatter.format(args, text, null);
 			sb.append(text + " ");
 
 			String className = record.getSourceClassName();
 			if ((className == null) || !className.equals("Storage.DebugPrepared"))
 			{
 				if (className != null)
 					sb.append(className);
 				else
 					sb.append(record.getLoggerName());
 
 				if (record.getSourceMethodName() != null)
 					sb.append(" " + record.getSourceMethodName() + " ");
 
 				sb.append(record.getLevel().getLocalizedName());
 				sb.append(": ");
 			}
 
 			sb.append(formatMessage(record));
 			sb.append("\n");
 
 			if (record.getThrown() != null)
 			{
 				try
 				{
 					StringWriter sw = new StringWriter();
 					PrintWriter pw = new PrintWriter(sw);
 					record.getThrown().printStackTrace(pw);
 					pw.close();
 					sb.append(sw.toString());
 				}
 				catch (Exception ex)
 				{
 				}
 			}
 
 			return sb.toString();
 		}
 	}
 
 
 
 }

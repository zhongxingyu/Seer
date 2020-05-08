 package com.sleaker.logparser;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Queue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class LogThread extends Thread {
 	private static final String baseLogName = "-serverlog.csv";
 	Queue<String> logQueue;
 	PrintWriter logWriter;
 	boolean run = true;
 
 	public LogThread(Queue<String> logQueue) {
 		this.logQueue = logQueue;
 	}
 
 	@Override
 	public void run() {
 		//initial wait
 		while (run) {
 			try {
 				System.out.println("Running wait in logger thread.");
				synchronized (this) {
					this.wait(60000);
				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			//Skip writing the queue if it's empty.
 			if (logQueue.isEmpty())
 				continue;
 
 			System.out.println("Write queue has items, starting writer!");
 			writeQueue();
 		}
 	}
 
 	protected void writeQueue() {
 		File logFile = new File(LogParser.logDir + getDate() + baseLogName);
 		try {
 			if (!logFile.exists()) {
 				logFile.createNewFile();
 			}
 			//Create a FileWriter wrapped in a PrintWriter.
 			logWriter = new PrintWriter(new FileWriter(logFile, true));
 			Iterator<String> iter = logQueue.iterator();
 			while (iter.hasNext()) {
 				String s = iter.next();
 				String message = parseString(s);
 				if (message != null) {
 					logWriter.println(message);
 				}
 				
 				iter.remove();
 			}
 			logWriter.close();
 		} catch (IOException e) {
 			return;
 		}
 	}
 
 	private String getDate() {
 		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yy");
 		return dateFormat.format(new Date());
 	}
 
 	private String parseString(String s) {
 		String[] v = s.split("\\|");
 		
         Pattern pattern = Pattern.compile("\\]\\s*(\\w*\\d*)(\\(\\*\\)\\s*)?:(\\.*)$");
         Matcher matcher = pattern.matcher(v[1].trim());
         if (matcher.find()) {
         	return format(LogType.CHAT, v[0], matcher.group(1), matcher.group(3));
         } 
         
         pattern = Pattern.compile("(\\w*)\\s*\\[\\/(.*):.*logged in");
         matcher = pattern.matcher(v[1].trim());
         if (matcher.find()) {
         	return format(LogType.CONNECT, v[0], matcher.group(1), matcher.group(2));
         }
         
         pattern = Pattern.compile("(\\.*) used command \"(.*)\"$");
         matcher = pattern.matcher(v[1].trim());
         if (matcher.find()) {
         	return format(LogType.COMMAND, v[0], matcher.group(1), matcher.group(2));
         }
         
         pattern = Pattern.compile("(\\w+\\d*) moved wrongly!$");
         matcher = pattern.matcher(v[1].trim());
         if (matcher.find()) {
         	return format(LogType.MOVEMENT_ERROR, v[0], matcher.group(1), "moved wrongly");
         }
         
         pattern = Pattern.compile("(\\w+\\d*) lost connection: (.*)$");
         matcher = pattern.matcher(v[1].trim());
         if (matcher.find()) {
         	return format(LogType.DISCONNECT, v[0], matcher.group(1), matcher.group(2));
         }
         
         if (s.contains("Expected ") || s.contains("Got position ")) {
         	return format(LogType.MOVEMENT_ERROR, v[0], "server", v[1].replace("Expected ", "").replace("Got position ", "").replace(",", ""));
         }
 		
 		return null;
 	}
 	
 	/**
 	 * Formats a string for writing to file
 	 * @return
 	 */
 	private String format(LogType logType, String time, String issuer, String data) {
 		return time + ", " + logType.getName() + ", " + issuer + ", " + data.replace(",", " ");
 	}
 
 	public enum LogType {
 
 		CHAT("CHATX"),
 		COMMAND("COMMD"),
 		CONNECT("CNNCT"),
 		DISCONNECT("DISCO"),
 		MOVEMENT_ERROR("MVERR"),
 		SERVER_ERROR("SVERR");
 		
 		private final String displayName;
 		
 		LogType(String name) {
 			this.displayName = name;
 		}
 		
 		public String getName() {
 			return this.displayName;
 		}
 	}
 }

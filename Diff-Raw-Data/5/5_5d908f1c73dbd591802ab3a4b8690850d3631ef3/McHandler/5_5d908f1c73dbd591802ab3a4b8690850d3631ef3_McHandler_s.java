 package ch.romibi.minecraft.toIrc;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class McHandler extends Thread{
 	
 	private static Process mcProcess;
 	private BufferedWriter mcWriter;
 
 	public McHandler() {
 		startMc();
 	}
 	
 	public void run () {
 		BufferedReader mcReader = new BufferedReader(new InputStreamReader(mcProcess.getErrorStream()));
 		String mcOutputCache = "";
 	
 		mcWriter = new BufferedWriter(new OutputStreamWriter(mcProcess.getOutputStream()));
 		boolean exit = false;
 		
 		while (!exit) {
 			
 			try {
 				mcOutputCache = mcReader.readLine();
 				if(mcOutputCache != null) {
 					parseOutputFromMc(mcOutputCache.trim());
 				}
 				try {
 					if(mcProcess.exitValue() == 0) {
 						exit = true;
 					}
 				} catch (IllegalThreadStateException e) {
 					
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			
 			
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	private static void startMc() {
 		ProcessBuilder pb = null;
 		if (McToIrc.configFile.getProperty("nogui").equals("true")) {
			pb = new ProcessBuilder("java", "-Xmx"+McToIrc.configFile.getProperty("xmx")+"M", "-Xms"+McToIrc.configFile.getProperty("xms"), "-jar", McToIrc.configFile.getProperty("serverFile"), "nogui");
 		} else {
			pb = new ProcessBuilder("java", "-Xmx"+McToIrc.configFile.getProperty("xmx")+"M", "-Xms"+McToIrc.configFile.getProperty("xms"), "-jar", McToIrc.configFile.getProperty("serverFile"));
 		}
 		
 		try {
 			mcProcess = pb.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void parseOutputFromMc(String cache) {
 		Pattern userMsgPattern = Pattern.compile(McToIrc.configFile.getProperty("messageRegex"));
 		Matcher userMsg = userMsgPattern.matcher(cache);
 		if(userMsg.matches()) {
 			String user = userMsg.group(1);
 			String msg = userMsg.group(2);
 			McToIrc.userMessage(user, msg);
 		}
 		Pattern userLoggedInPattern = Pattern.compile(McToIrc.configFile.getProperty("loginRegex"));
 		Matcher userLoggedIn = userLoggedInPattern.matcher(cache);		
 		if(userLoggedIn.matches()){
 			String user = userLoggedIn.group(1);
 			McToIrc.userLoggedIn(user);
 		}
 		
 		Pattern userLoggedOutPattern = Pattern.compile(McToIrc.configFile.getProperty("logoutRegex"));
 		Matcher userLoggedOut = userLoggedOutPattern.matcher(cache);
 		if(userLoggedOut.matches()){
 			String user = userLoggedOut.group(1);
 			McToIrc.userLoggedOut(user);
 		}
 		
 //		Pattern userMeActionPattern = Pattern.compile(McToIrc.configFile.getProperty("meActionRegex"));
 //		Matcher userMeAction = userMeActionPattern.matcher(cache);
 //		if(userMeAction.matches()){
 //			String user = userMeAction.group(1);
 //			String msg = userMeAction.group(2);
 //			McToIrc.userMeAction(user, msg);
 //		}
 		
 		System.out.println(cache);
 	}
 	
 	public void sendToMc(String string) {
 		try {
 			mcWriter.append(string);
 			mcWriter.newLine();
 			mcWriter.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }

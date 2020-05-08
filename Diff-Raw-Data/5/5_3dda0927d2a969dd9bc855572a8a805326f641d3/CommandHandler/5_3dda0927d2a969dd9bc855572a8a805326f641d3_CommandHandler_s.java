 package main;
 
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.google.gson.Gson;
 
 public class CommandHandler {
 	
 	private String[] shutdownMessages = {"He- oh.. Goodbye world ;~;", "Nobody loves me D:", "Hey, I didn't hurt you!", "It's okay.", "AAAAAAHHHHHHH!!!!", "I don't blame you...", "No hard feelings.", "Shutting down.", "Nap time.", "Nononono I can fix this I ca-", "NNNNNOOOOOOOOOOOOOOOOOOOO~"};
 	private boolean gfy = false;
 	
 	public void processCommand(String channel, String sender, String login, String hostname, String message){
 		String command = message.substring(1);
 		String[] params = command.split(" ");
 		System.out.println("COMMAND: " + command);
 		if(SimpleBot.instance.cadburyMode){
 			if(command.startsWith("rem ")){
 				addRem(channel, sender, command);
 			}else if(command.startsWith("forget ")){
 				SimpleBot.instance.removeRemIfExists(command.substring("forget ".length()));
 				SimpleBot.instance.sendMessage(channel, "I have forgotten " + command.substring("forget ".length()) + ".");
 			}else if(command.startsWith("g ")){
 				processGoogleSearch(channel, sender, command);
 			}else if(command.startsWith("gis ")){
 				//processGoogleImageSearch(channel,sender,command);
 			}
 		}
 		if(command.startsWith("ed") && authorize(channel,login,hostname)){
 			if(SimpleBot.instance.unreadExceptions.size() > 0){
 				SimpleBot.instance.sendMessage(channel, "Last exception details: " + SimpleBot.instance.unreadExceptions.get(0));
 				SimpleBot.instance.unreadExceptions.remove(0);
 			}else{
 				SimpleBot.instance.sendMessage(channel, "No unread exceptions left.");
 			}
 		
 		}else if(command.equals("shutdown") && authorize(channel, login, hostname)){
 			SimpleBot.instance.sendMessage(channel, shutdownMessages[new Random().nextInt(shutdownMessages.length)]);
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			SimpleBot.instance.shutdown();
 		
 		}else if(command.startsWith("query ")){
 			processSqlCommand(channel, sender, login, hostname, command);
 			
 		
 		}else if(command.startsWith("help ")){
 			if(params[1].equals("quote")){
 				sendMessage(channel, sender + ", For each line sent to the channel, if it contains more than 6 words, it has a 5% chance of becoming that user's random quote.");
 			}else if(params[1].equals("topics")){
 				sendMessage(channel, sender + ", Use -help <topic> to get help about a specific topic. Available topics are: login, quote, commands");
 			}else if(params[1].equals("login")){
 				sendMessage(channel, sender + ", Currently, people are identified by their login. This means that statistics are shared if you use webchat or if you haven't set up your login. Refer to your IRC client's documentation to find out how to set it up.");
 			}else if(params[1].equals("commands")){
 				sendMessage(channel, "The following commands are available for everyone to use: info/help/version, get, ping. Other commands are: ed, shutdown, query, tcm, set, del, mem, c*tricpuns++, snag.");
 				sendMessage(channel, "For help on a specific command, use -help <command>");
 			}else if(params[1].equals("get")){
 				if(params.length == 2)
 				sendMessage(channel, "Returns statistics data. Available parameters: quote, lines, glines. for more information, use -help get <parameter>");
 				else if(params[2].equals("quote"))
 				sendMessage(channel, "Returns the random quote for that user. Usage: -help get quote <username>");
 				else if(params[2].equals("lines"))
 				sendMessage(channel, "Returns the line count for that user. Usage: -help get lines <username>");
 				else if(params[2].equals("glines"))
 				sendMessage(channel, "Returns the global line count. No other parameters required. Usage: -help get glines");
 			}
 		
 		}else if(command.startsWith("info") || command.startsWith("help") || command.startsWith("version")){
 			SimpleBot.instance.sendMessage(channel, "StatsBot " + SimpleBot.version +" - made by baggerboot. Stats page: http://jgeluk.net/stats/ - To see a list of help topics, use the '-help topics' command.");
 		
 		
 		}else if(command.equals("tcm") && authorize(channel, login, hostname)){
 			SimpleBot.instance.cadburyMode = !SimpleBot.instance.cadburyMode;
 			if(SimpleBot.instance.cadburyMode){
 				SimpleBot.instance.sendMessage(channel, "'$g' and rems enabled.");
 			}else{
 				SimpleBot.instance.sendMessage(channel, "'$g' and rems disabled.");
 			}
 		
 		}else if(command.startsWith("set ") && authorize(channel, login, hostname)){
 			if(params.length == 5 && params[1].equals("primary") && params[3].equals("for")){
 				String _login = params[4];
 				String newPrimary = params[2];
 				String result = SqlConnector.getInstance().sendQuery("UPDATE alts SET `primary` = '" + newPrimary + "' WHERE `login` = '" + _login + "'");
 				SimpleBot.instance.sendMessage(channel, sender + ", " + result);
 			}
 			
 		}else if(command.startsWith("get ")){
 			if(params.length == 2){
 				if(params[1].equals("glines")){
 					sendMessage(channel, sender + ", the global line count is " + SqlConnector.getInstance().sendSelectQuery("SELECT `value` FROM `varia` WHERE `key` = 'global_line_count'"));
 				}
 			}else if(params.length == 3){
 				if(params[1].equals("lines")){
					sendMessage(channel, sender + ", " + params[2] + " has sent " + SqlConnector.getInstance().sendSelectQuery("SELECT `lines` FROM `users` LEFT JOIN alts ON `user`.`nick` = `alts`.login WHERE (`primary` = '" + params[2] + "` OR `additional` LIKE '%" + params[2] + "%')"));
 				}else if(params[1].equals("quote")){
					sendMessage(channel, sender + ", " + params[2] + "'s random quote: " + SqlConnector.getInstance().sendSelectQuery("SELECT `random_quote` FROM `users` LEFT JOIN alts ON `user`.`nick` = `alts`.login WHERE (`primary` = '" + params[2] + "` OR `additional` LIKE '%" + params[2] + "%')"));
 				}
 			}
 			
 		}else if(command.startsWith("del ") && authorize(channel, login, hostname)){
 			if(command.substring("del ".length()).startsWith("word")){
 				String word = command.substring("del word ".length());
 				SqlConnector.getInstance().sendQuery("DELETE FROM words WHERE word = '" + word + "'");
 				SimpleBot.instance.sendMessage(channel, sender + ", I have removed " + word + " from the words list.");
 			}
 			
 		}else if(command.startsWith("mem")){
 			Runtime runtime = Runtime.getRuntime();
 
 		    NumberFormat format = NumberFormat.getInstance();
 
 		    StringBuilder sb = new StringBuilder();
 		    long maxMemory = runtime.maxMemory();
 		    long allocatedMemory = runtime.totalMemory();
 		    long freeMemory = runtime.freeMemory();
 
 		    sb.append("free memory: " + format.format(freeMemory / 1024) + "<br/>");
 		    sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "<br/>");
 		    sb.append("max memory: " + format.format(maxMemory / 1024) + "<br/>");
 		    sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "<br/>");
 		    System.out.println(sb.toString());
 		
 		
 		}else if(command.equals("citricpuns++") && (login.equals("~baggerboo") || login.equals("~citricsqu"))){
 			SqlConnector.getInstance().tryIncrementVaria("citricpuns");
 			SimpleBot.instance.sendMessage(channel, sender + ", done.");
 		
 		
 		}else if(SimpleBot.instance.cadburyMode){
 			processRem(channel, sender, login, hostname, command);
 		
 		
 		}else if(command.equals("calc ")){
 			String formattedStatement = command.substring("calc ".length()).replaceAll(" ", "");
 			
 		
 		
 		}else if(command.equals("ping")){
 			SimpleBot.instance.sendMessage(channel, "Pong!");
 		
 		}else if(command.startsWith("queery")){
 			
 			sendMessage(channel, "Dohohohoho~");
 			
 		}else if(command.equals("snag") && authorize(channel, login, hostname)){
 			sendMessage(channel, "Snagging next line.");
 			StatsHandler.getInstance().snagNextLine("*");
 			
 		}else if(command.equals("snag ") && params.length > 1 && authorize(channel, login, hostname)){
 			sendMessage(channel, "Snagging next line by " + params[1]);
 			StatsHandler.getInstance().snagNextLine(params[1]);
 			
 		}else{
 			System.out.println("Ignoring invalid command.");
 		}
 	}
 	private void sendMessage(String channel, String message) {
 		SimpleBot.instance.sendMessage(channel, message);
 		
 	}
 	private boolean authorize(String channel, String login, String hostname){
 		if(login.equals("~baggerboo") && hostname.equals("199.115.228.30")){
 			return true;
 		}else{
 			
 			return false;
 		}
 	}
 	
 	private void processSqlCommand(String channel, String sender, String login, String hostname, String command) {
 		if(!(login.equals("~baggerboo") && hostname.equals("199.115.228.30"))){
 			if(command.contains("dick") || command.contains("fuck")){
 				sendMessage(channel, sender + ", how about you go " + command.substring(6));
 			}else if(!command.startsWith("SELECT") && !command.startsWith("UPDATE") && !command.startsWith("ALTER")){
 				sendMessage(channel, "Are you trying to screw with me now");
 			}else{
 				sendMessage(channel, sender + ": Only my master may do that ;~;");
 			}
 			return;
 		}
 		command = command.substring(6);
 		List<String> lines = new ArrayList<String>();
 		
 		if(command.toLowerCase().startsWith("select")){
 			if(command.toLowerCase().startsWith("go fuck yourself") || command.startsWith("fuck yourself")){
 				sendMessage(channel, "No, how about you go fuck yourself. That seems like a better plan.");
 				gfy = true;
 				return;
 			}
 			try {
 				String[] results = SqlConnector.getInstance().sendSelectQueryArr(command);
 				for(int i = 0; i < results.length; i++){
 					String nextLine = "";
 					while(nextLine.length() < 414){
 						if(nextLine.equals("")){
 							nextLine = results[i];
 							if(i < results.length-1) i++;
 							else break;
 						}else{
 							nextLine = nextLine.concat(", " + results[i]);
 							if(i < results.length-1) i++;
 							else break;
 						}
 					}
 					lines.add(nextLine);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				lines.add("ERROR: " + e.getMessage());
 			}
 		}else{
 			try {
 				lines.add(SqlConnector.getInstance().sendQuery(command));
 			} catch (Exception e) {
 				lines.add("ERROR: " + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		for(int i = 0; i < lines.size(); i++){
 			if(i == 0){
 				SimpleBot.instance.sendMessage(channel, sender + ": " + lines.get(i));
 			}else{
 				SimpleBot.instance.sendMessage(channel, lines.get(i));
 			}
 			
 		}
 	}
 
 	private void addRem(String channel, String sender, String command) {
 		String[] args = command.split(" ");
 		if(SimpleBot.instance.remExists(args[1])){
 			SimpleBot.instance.sendMessage(channel, "I already have something saved for " + args[1]);
 		}else{
 			SimpleBot.instance.addRem(args[1], command.substring(4 + args[1].length()));
 			SimpleBot.instance.sendMessage(channel, "Added $" + args[1] + " to remlist.");
 		}
 	}
 	private void processRem(String channel, String sender, String login, String hostname, String command){
 		if(SimpleBot.instance.remExists(command)){
 			String definition = SimpleBot.instance.getRem(command);
 			SimpleBot.instance.sendMessage(channel, definition);
 		}
 	}	
 	/*private void processGoogleImageSearch(String channel, String sender, String command) {
 		String search = command.substring(2);
 	    String google = "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=";
 	    String charset = "UTF-8";
 
 	    URL url;
 	    Reader reader = null;
 		try {
 			url = new URL(google + URLEncoder.encode(search, charset));
 			reader = new InputStreamReader(url.openStream(), charset);
 		} catch (Exception e) {
 			SimpleBot.instance.sendMessage(channel, sender + ", something bad happened ;~;");
 			SimpleBot.instance.unreadExceptions.add(e.getMessage());
 			e.printStackTrace();
 		}
 	    GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);
 
 	    // Show title and URL of 1st result.
 	    String title = results.getResponseData().getResults().get(0).getTitle();
 	    title = title.replace("<b>", "");
 	    title = title.replace("</b>", "");
 	    String URL = results.getResponseData().getResults().get(0).getUrl();
 	    SimpleBot.instance.sendMessage(channel, sender + ", " + title + ": " + URL);
 	}*/
 	private void processGoogleSearch(String channel, String sender, String command){
 		String search = command.substring(2);
 	    String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
 	    String charset = "UTF-8";
 
 	    URL url;
 	    Reader reader = null;
 		try {
 			url = new URL(google + URLEncoder.encode(search, charset));
 			reader = new InputStreamReader(url.openStream(), charset);
 		} catch (Exception e) {
 			SimpleBot.instance.sendMessage(channel, sender + ", something bad happened ;~;");
 			SimpleBot.instance.unreadExceptions.add(e.getMessage());
 			e.printStackTrace();
 		}
 	    GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);
 
 	    // Show title and URL of 1st result.
 	    String title = results.getResponseData().getResults().get(0).getTitle();
 	    title = title.replace("<b>", "");
 	    title = title.replace("</b>", "");
 	    String URL = results.getResponseData().getResults().get(0).getUrl();
 	    SimpleBot.instance.sendMessage(channel, sender + ", " + title + ": " + URL);
 	}
 }

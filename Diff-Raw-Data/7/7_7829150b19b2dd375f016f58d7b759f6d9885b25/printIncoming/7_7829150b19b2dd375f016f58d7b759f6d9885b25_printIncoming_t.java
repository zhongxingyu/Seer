 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.lang.reflect.Array;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 public class printIncoming extends Thread{
 	//The socket connected to the server
 	Socket socket;
 
 	//Files that will be read from and written to
 	File logfile = new File("./log.txt");
 	File linkfile = new File("./links.txt");
 	File loginfile = new File("./logins.txt");
 	File spotifyfile = new File("./spotify.txt");
 
 	//All the filewriters
 	FileWriter logw = null;
 	FileWriter linkw = null;
 	FileWriter spotifyw = null;
 
 	//The lists contaning links, spotify-links and logins
 	LinkedList<String> linklist = new LinkedList<String>();
 	LinkedList<String> loginlist = new LinkedList<String>();
 	LinkedList<String> spotifylist = new LinkedList<String>();
 
 	//The string that will contain the most current
 	//line of server response in raw, unprocessed form
 	String in = "";
 
 	//A string that will contain the current date so that the logger
 	//can log date changes
 	String logdate = "";
 
 	//An array that will contain the actual message word by word
 	String[] command;
 
 	//Will contain per message info later
 	String message;
 	String channel;
 	String hostname;
 	String nick;
 
 	//The reader and writer that will send and recieve data from/to the server
 	BufferedReader fromRemote = null;
 	PrintWriter toRemote = null;
 
 	//    Custom constructor so that we can pass on a connected socket to this class
 	//    public printIncoming(Socket socket){
 	//        this.socket = socket;
 	//    }
 
 	//Custom constructor so we will be able to
 	//pass on just host and portnumber
 	public printIncoming(String host, int port){
 		try{
 			this.socket = new Socket(host, port);
 		}catch(Exception e){
 			System.out.println("fail");
 		}
 	}
 
 	public printIncoming(BufferedReader r, PrintWriter w){
 		try {
 			this.fromRemote = r;
 			this.toRemote = w;
 		}catch(Exception e){
 			System.out.println("failtrain");
 		}
 	}
 
 	@Override
 	//The thread object that will process everything
 	public void run(){
 
 		//Load logins from file
 		loginlist = loadLogins(loginfile);
 
 		// for(String s: loginlist) System.out.println(s);
 		// Prints all logins for debugging purposes
 
 		//Set all the object needed for correct operation
 		//to appropriate values and catch all exceptions
 		try{
 			if(fromRemote == null || toRemote == null){
 				fromRemote = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 				toRemote = new PrintWriter(socket.getOutputStream());
 			}
 
 			linkw = new FileWriter(linkfile, true);
 			spotifyw = new FileWriter(spotifyfile, true);
 			logw = new FileWriter(logfile, true);
 			linklist = loadList(linkfile);
 			spotifylist = loadList(spotifyfile);
 			logw.write("\nStartup:\n"+getDate()+"\n");
 			logdate = getDate();
 		}catch(Exception e){
 			System.out.println("Error in printthread: "+e);
 			System.exit(1);
 		}
 
 		//Read read all incoming data, let the magic begin :)
 		while(true){
 			try {        
 				//Process the incoming data and stor it to "in"
 				in = fromRemote.readLine();
 
 				//If nothing can be read, exit
 				if(in == null) System.exit(1);
 
 				//Process incoming data to minimize method overhead
 				message = getMsg(in);
 				channel = getChannel(in);
 				hostname = getHostname(in);
 				nick = getNick(in).trim();
 				command = message.split(" ");
 
 
 				//Catch CTCP :)
 				if(in.contains(""+'\001')) {
 					in = in.replace(""+'\001', "");
 					in = in.replace("ACTION", nick);
 					System.out.println(" * "+message);
 				}
 
 				//If the raw message is a ping request, send a reply
 				else if(in.startsWith("PING")){
 					in = in.replace("PING", "PONG");
 					sendRaw(in);
 
 				}
 				//Catch all the joins
 				else if ((in.contains("JOIN") && !(message.contains("JOIN")))) {
 					System.out.println(nick+" has joined.");
 				}
 				//Catch all channel parts/quits and log those users out
 				else if(in.contains("PART") && !(message.contains("PART"))){
 					System.out.println(nick+" has left "+channel);
 					logout(nick);
 				}
 				else if((in.contains("QUIT") && !(message.contains("QUIT")))){
 					System.out.println(nick+" has quit.");
 					logout(nick);                	
 				}
 				//Catch all nick changes to see if someone changes their nick to
 				//or from an adminregistered nick, and if so log the admin out
 				else if((in.contains("NICK"))){
 					try {
 						String[] tokens = message.split(" ");
 						if(isLoggedIn((tokens[2]))) logout(tokens[2]);
 						if(isLoggedIn(nick)) logout(nick);
 					}catch (ArrayIndexOutOfBoundsException e){}
 				}
 				//Handle the "!date" command
 				else if(command[0].equalsIgnoreCase("!date")){
 					sendNotice(nick, getDate());
 
 				}
 				//Handle the "!login" command
 				else if(command[0].equalsIgnoreCase("!login") && Array.getLength(command) > 1){
 					try {
 						login(nick, command[1]);
 					}catch (Exception e){}
 				}
 				//Handle the "!logout" command
 				else if(command[0].equalsIgnoreCase("!logout")){
 					logout(nick);
 				}
 				//Handle the "!thetime" command
 				else  if(command[0].equalsIgnoreCase("!thetime")){
 					sendNotice(nick, getTime());
 
 				}else if(command[0].equalsIgnoreCase("!deop")){
 					System.out.println(nick+" deopped him/herself.");
 					sendRaw("MODE "+channel+" -o "+nick);
 				}else if(command[0].equalsIgnoreCase("!op")){
 					if(isLoggedIn(nick) && isAdmin(nick)){
 						System.out.println(nick+" was granted super cow powers.");
 						sendNotice(nick, "OP-request granted, opping you in "+channel+ " right away.");
 						sendRaw("MODE "+channel+" +o "+nick);
 					}else{
 						sendNotice(nick, "You can get Super cow powers if you log in, please do and try again.");
 						System.out.println(nick+" tried to gain OP powers but wasn't logged in.");
 					}
 				}else if(command[0].equalsIgnoreCase("!help")){
 					sendNotice(nick, "!date - Show the current date and time (This is the time in Sweden, where the bot is run).");
 					sendNotice(nick, "!thetime - Like !date but just the time.");
 					sendNotice(nick, "!login <password> - Log in as a botadmin.");
 					sendNotice(nick, "!logout - The opposite, log out.");
 					sendNotice(nick, "!spotify - Want some music, well, here you go.");
 					sendNotice(nick, "!bored - Supplies the bored commandgiver a randomized link.");
 					sendNotice(nick, "WARNING! The links that the bot supplies aren't in any way filtered by content and can be NSFW. Neiter the bot nor it's maker (DOSn3rd) takes any responsibility of the contents of the supplied links. Use common sense and be cautious.");
 				}else if(command[0].equalsIgnoreCase("!addadmin") && isLoggedIn(nick)){
 					try{
 						loginlist.add(command[1]+"#"+command[2]+"#0");
 						sendNotice(nick, "Admin \""+command[1]+"\" added.");
 					}catch(ArrayIndexOutOfBoundsException s){
 						sendNotice(nick, "Syntax error, correct syntax is: !addadmin <nick> <password>");
 					}
 
 					saveLogins(loginlist, loginfile);
 				}else if(command[0].equalsIgnoreCase("!admincommands") && isLoggedIn(nick)){
 					sendNotice(nick, "Supercommands: (The bot has to be OP)");
 					sendNotice(nick, "!op - Request OP.");
 					sendNotice(nick, "!deop - Resign your OP capabilities.");
 					sendNotice(nick, "!op <nick> - Give someone OP.");
 					sendNotice(nick, "!chpass <oldpass> <newpass> - Change your password.");
 					sendNotice(nick, "!deop <nick> - Revoke OP from someone.");
 					sendNotice(nick, "----------------------------------------------------------");
 					sendNotice(nick, "Admincommands:");
 					sendNotice(nick, "!lsadmin - List all botadmins.");
 					sendNotice(nick, "!addadmin <nick> <password> - Add an admin -=!Be careful, botadmin means lots of power!=-");
 					sendNotice(nick, "!join <channel> - Makes the bot join a channel.");
 					sendNotice(nick, "!part (<channel>) - Makes the bot part the specified channel, if the channel name is left out the bot will part the channel that the message came from.");
 					sendNotice(nick, "!rmadmin <nick> - Revoke botadmin status for a nick.");
 					sendNotice(nick, "!quit (<quitmessage>) - Makes the bot quit and shut down, with eventual quitmessage.");
 
 				}else if(command[0].equalsIgnoreCase("!rmadmin") && isLoggedIn(nick)){
 					try{
 						delAdmin(command[1]);
 						sendNotice(nick, "Admin \""+command[1]+"\" removed.");
 					}catch(ArrayIndexOutOfBoundsException s){
 						sendNotice(nick, "Fail ;D");
 					}
 
 				}else if(command[0].equalsIgnoreCase("!reloadlinks") && isLoggedIn(nick)){
 					linklist = loadList(linkfile);
 				}else if(command[0].equalsIgnoreCase("!bored")){
 					sendRandomLink(channel, linklist);
 				}else if(command[0].equalsIgnoreCase("!spotify")){
 					sendRandomLink(channel, spotifylist);
 				}else if(command[0].equalsIgnoreCase("!chpass")){
 					changePw(nick, command[1], command[2]);
 					sendNotice(nick, " :Pass changed to "+command[2]);
 				}else if(command[0].equalsIgnoreCase("!lsadmin") && isLoggedIn(nick)){
 					int i = 1;
 					String[] info = null;
 					for(String s : loginlist){
 						info = s.split("#");
 						sendNotice(nick, i+": "+info[0]);
 						i++;
 					}
 				}else if(command[0].equalsIgnoreCase("!join") && isLoggedIn(nick)){
 					try{
 						sendRaw("JOIN "+command[1].toLowerCase());
 					}catch(ArrayIndexOutOfBoundsException e){}
 
 				}else if(command[0].equalsIgnoreCase("!quit") && isLoggedIn(nick)){
 					try{
 						sendRaw("QUIT :"+message.substring(6));
 					}catch(StringIndexOutOfBoundsException s){
 						sendRaw("QUIT");
 					}
 
 				}else if(command[0].equalsIgnoreCase("!part") && isLoggedIn(nick)){
 					try{
 						sendRaw("PART "+channel.trim());
 					}catch(StringIndexOutOfBoundsException s){
 						sendRaw("PART");
 					}
 
 				}else{
 					System.out.println(nick+": "+message);
 				}
 
 				if(message.toLowerCase().contains("://")){
 					String[] tokens = message.split(" ");
 					for(int i = 0; i < tokens.length; i++){
 						if(tokens[i].toLowerCase().startsWith("http")){
 							sendMsg(channel, getHtmlTag("title", getHtmlCode(tokens[i])));
 
 							addlink(tokens[i]);
 						}
 					}
 				}
 
 				if(in.toLowerCase().contains("privmsg #")){
 					if(!logdate.equals(getDate())) {
 						logw.write("\n"+getDate()+"\n");
 						logdate = getDate();
 						logw.flush();
 					}
 					logw.write(getTime()+" < "+nick+"> "+message+"\n");
 					logw.flush();
 				}
 			}catch(Exception e){
 				System.out.println("thread2blaj: "+e);
 				System.exit(1);
 
 			}
 		}
 	}
 	private String getNick(String message){
 		String nick = "";
 		message = message.replace(":", "");
 		if(message.indexOf("!") == -1){
 			nick = "-!-";
 		}else{
 			Scanner nickscanner = new Scanner(message);
 			while(nickscanner.hasNext()){
 				if((nick = nickscanner.next()).contains("!")) {
 					nick = nick.substring(0, nick.indexOf("!"));
 					break;
 				}
 			}
 			nickscanner.close();
 		}
 		return nick;
 	}
 	private String getMsg(String message) {
 		message = message.substring(1);
 		message = message.substring(message.indexOf(":")+1);
 		return message;
 	}
 	private String getHostname(String message){
 		String hostname = "";
 		String[] args = message.split(" ");
 		for(int i = 0; i < args.length; i++){
 			if (args[i].contains("!") && args[i].contains("@") && args[i].contains(".")){
 				hostname = args[i];
 				break;
 			}
 		}
 		return hostname;
 	}
 	private String getChannel(String message){
 		String token = "";
 		message = message.replace(":", "");
 		Scanner sc = new Scanner(message);
 		while(sc.hasNext()){
 			token = sc.next();
 			if(token.startsWith("#")){
 				message = token;
 				break;
 			}
 		}
 		sc.close();
 		return message;
 	}
 	private String getDate(){
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 		Date date = new Date();
 		String Sdate = df.format(date); 
 		return Sdate;
 	}
 	private String getTime(){
 		DateFormat df = new SimpleDateFormat("HH:mm");
 		Date date = new Date();
 		String time = df.format(date); 
 		return time;
 	}
 
 	private boolean isAdmin(String nick) {
 		boolean admin = false;
 		for(String text : loginlist){
 			if(text.startsWith(nick+"#")){
 				admin = true;
 			}
 		}
 		return admin;
 	}
 	private boolean isLoggedIn(String nick){
 		boolean isLoggedIn = false;
 		for(String text : loginlist){
 			System.out.print("Testing: "+text);
 			if(text.startsWith(nick+"#") && text.endsWith("#1")){
 				System.out.println("accepted");
 				isLoggedIn = true;
 			}else System.out.println();
 		}
 		return isLoggedIn;
 	}
 	private void login(String nick, String pw){
 		String[] info = {"", "", ""};
 		int i = 0;
 		for(String text : loginlist){
 			info = text.split("#");
 			if(info[0].equals(nick) && info[1].equals(pw) && info[2].equals("0") && info[0] != null && info[1] != null && info[2] != null){
 				//Check for empty content to fix the bug suggested by Enari
 				info[2] = "1";
 				text = info[0]+"#"+info[1]+"#"+info[2];
 				loginlist.set(i, text);
 				System.out.println("User \""+info[0]+"\" logged in.");
 				//Send message comfirming correct login credentials
 				sendNotice(nick, "Password correct, you are now logged in.");
 			}
 			i++;
 			System.out.println(text);
 		}
 	}
 	private void logout(String nick){
 		String[] info = null;
 		int i = 0;
 		for(String text : loginlist){
 			info = text.split("#");
 			if(info[0].equals(nick) && info[2].equals("1")){
 				info[2] = "0";
 				text = info[0]+"#"+info[1]+"#"+info[2];
 				loginlist.set(i, text);
 				System.out.println("User \""+info[0]+"\" logged out.");
 				sendNotice(nick, "You have logged out.");
 			}
 			i++;
 		}
 	}
 	private void delAdmin(String nick){
 		if(isLoggedIn(nick)){
 			int i = 0;
 			for(String text : loginlist){
 				if(text.contains(nick+"#")){
 					loginlist.remove(i);
 					break;
 				}
 				i++;
 			}
 			saveLogins(loginlist, loginfile);
 		}
 	}
 	private void flushToFile(LinkedList<String> list, File file){
 		try{
 			FileWriter filew = new FileWriter(file, false);
 			for(String listline : list){
 				filew.write(listline+"\n");
 			}
 			filew.flush();
 			filew.close();
 		}catch(Exception e){}
 	}
 	private LinkedList<String> loadList(File file){
 		LinkedList<String> list = null;
 		try{
 			list = new LinkedList<String>();
 			Scanner filescanner = new Scanner(file);
 			String line = "";
 			while(filescanner.hasNextLine()){
 				line = filescanner.nextLine();
 				if(list.contains(line)){}else{
 					list.add(line);
 				}
 			}
 			filescanner.close();
 		}catch(Exception e){}
 		return list;
 	}
 	private void addlink(String link) {
 		link = link.trim();
 		try{
 			Scanner linksc = null;
 			boolean exists = false;
 			if(link.startsWith("http://open.spotify")){
 				linksc = new Scanner(spotifyfile);
 
 				for(String linkinlist : spotifylist){
 					if(linkinlist.equals(link)) exists = true;
 				}
 				while(linksc.hasNextLine()){
 					if(linksc.nextLine().equals(link)) exists = true;
 				}
 
 				if(exists){}else{
 					spotifyw.write(link+"\n");
 					spotifyw.flush();
 					spotifylist.add(link);
 					System.out.println("Spotifylink \""+link+"\" added...");
 				}
 			}else{
 				linksc = new Scanner(linkfile);
 				for(String linkinlist : linklist){
 					if(linkinlist.toLowerCase().contains(link.toLowerCase())) exists = true;
 				}
 				while(linksc.hasNextLine()){
 					if(linksc.nextLine().toLowerCase().contains(link.toLowerCase())) exists = true;
 				}
 				if(!exists){
 					linkw.write(link+"\n");
 					linkw.flush();
 					linklist.add(link);
 					System.out.println("Link \""+link+"\" added...");
 				}
 			}
 		}catch(Exception e){}
 	}
 	private void dellink(String link){
 		boolean deleted = false;
 		link = link.trim();
 		link = link.replace("\n", "");
 		System.out.println(link);
 		int i = 0;
 		for(String text : linklist){
 			if(text.toLowerCase().contains(link.toLowerCase())){
 				linklist.remove(i);
 				deleted = true;
 			}
 			i++;
 		}linklist = loadList(linkfile);
 		if(deleted){
 			sendNotice(nick, "The link \""+link+"\" has been deleted.");
 			flushToFile(linklist, linkfile);
 		}else{
 			sendNotice(nick, "Couldn't find \""+link+"\" in the database. Nothing deleted.");
 		}
 	}
 	private void changePw(String nick, String oldPw, String newPw) {
 		try {
 			int i = 0;
 			String[] info = null;
 			for(String text : loginlist){
 				info = text.split("#");
 				if(nick.equals(info[0]) && oldPw.equals(info[1])){
 					info[1] = newPw; //Set the old password to the new one....
 					loginlist.set(i, info[0]+"#"+info[1]+"#"+info[2]);
 				}
 				i++;
 			}
 			saveLogins(loginlist, loginfile); //....and save it all.
 		}catch(ArrayIndexOutOfBoundsException e){
 
 		}
 	}
 	private LinkedList<String> loadLogins(File file){
 		LinkedList<String> temp = loadList(loginfile);
 		LinkedList<String> returned = new LinkedList<String>();
 		for(String text : temp){
 			text = text+"#0";
 			returned.add(text);
 		}
 		return returned;
 	}
 	private void saveLogins(LinkedList<String> temp, File file){
 		LinkedList<String> done = new LinkedList<String>();
 		for(String text : temp){
 			text = text.replace("#0", "");
 			text = text.replace("#1", "");
 			done.add(text);
 		}
 		flushToFile(done, loginfile);
 	}
 	private void sendRandomLink(String channel, LinkedList<String> list) {
 		long randpos = Math.round(Math.random()*(list.size()-1));
 		sendMsg(channel, list.get((int) randpos));
 	}
 	private String getHtmlCode(String link) {
 		URL url;
 		StringBuilder builder;
 		String htmlCode = "";
 
 		try {
 			url = new URL(link);
 			URLConnection conn = url.openConnection();
 			conn.setConnectTimeout(10000);
 			conn.setReadTimeout(10000);
 			InputStreamReader inputStream = new InputStreamReader(conn.getInputStream());
 			BufferedReader reader = new BufferedReader(inputStream);
 			builder = new StringBuilder();
 			htmlCode = "";
 			String line;
 
 			while ((line = reader.readLine()) != null) {
 				builder.append(line+" ");
 			}
 			reader.close();
 
 			htmlCode = builder.toString();
 		} catch (MalformedURLException e) {
 			System.out.println(link+" is a bad url link");
 			return "";
 		} catch (IllegalArgumentException e) {
 			System.out.println(link+" is a bad url link");
 			return "";
 		} catch (IOException e) {
 			System.out.println("Could not open link: "+link);
 			return "";
 		}
 
 		return htmlCode;
 
 	}
 	private String getHtmlTag(String tag, String source) {
 		tag = tag.toLowerCase().trim();
 		String tagContent = "";
 		if(source.toLowerCase().contains("<"+tag.toLowerCase()+">")){
 			tagContent += source.substring(source.toLowerCase().indexOf(tag+">")+tag.length()+1, source.toLowerCase().indexOf("</"+tag+">"));
 		}
 		/*
 	Patten p = Pattern.compile("&#\\d\\d\\d;");
 	Matcher m = p.matcher(tagContent);
 	boolean result = m.find();
         while(result) {
             m.appendReplacement(sb, "dog");
             result = m.find();
         }
 		 */
 
 		return tagContent.replace("\n", "").replace("\t","").replace("  ", "");
 	}
 	public void sendNotice(String user, String message){
 		if(message != null && !message.equals("")) {
 			toRemote.println("NOTICE "+user+" :"+message);
 			toRemote.flush();
 		}
 	}
 	public void sendMsg(String channel, String message){
 		if(message != null && !message.equals("")) {
 			toRemote.println("PRIVMSG "+channel+" :"+message);
 			toRemote.flush();
 		}
 	}
 	public void sendRaw(String message){
 		if(message != null && !message.equals("")) {
 			toRemote.println(message);
 			toRemote.flush();
 		}
 	}
 	private void log(String logentry){
 		
 	}
 }

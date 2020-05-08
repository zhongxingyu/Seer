 import org.jibble.pircbot.*;
 import net.zifnab.google.*;
 import java.util.*;
 import java.net.*;
 import java.io.*;
 public class Bot extends PircBot{
 	private ArrayList<String> channels;
 	private ArrayList<String> vendItems = new ArrayList<String>();
 	private String nspass;
 	public Bot(String nick, ArrayList<String> chans, String nspass_in) {
 		this.setName(nick);
 		this.setLogin(nick);
 		channels = chans;
 		nspass = nspass_in;
 	}
 	public void onConnect(){
 		identify(nspass);
 
 		for (String channel : channels){
 			joinChannel(channel);
 		}
 
 	}
 
 	public void onMessage(String channel, String sender, String login, String hostname, String message) {
 		if (message.charAt(0) != '!') return;
 		User[] names = getUsers(channel);
 		User nick;
 		String command = message.split(" ")[0].toLowerCase();
 		switch (command){
 			case "!save":
 				if(sender.toLowerCase().equals("zifnab")){
 					if(doSave()){
 						sendMessage(channel, "Save Successful");
 					} else {
 						sendMessage(channel, "Save Failed");
 					}
 				}
 			break;
 			case "!google":
 				sendMessage(channel, google.search(message.toLowerCase().split("!google")[1])[1]);
 			break;
 			case "!join":
 				joinChannel(message.split(" ")[1]);
 				channels.add(message.split(" ")[1]);
 				doSave();
 			break;
 			case "!leave":
 				nick = findUser(sender, names);
 				if (nick.isOp()){
 					partChannel(channel);
 					channels.remove(channel);
 					doSave();
 				} else {
 					sendMessage(channel, "You must be an operator to ask me to leave.");
 				}
 			break;
 			case "!lmgtfy":
 				StringBuilder sb = new StringBuilder();
 				for (int i=1; i<message.split(" ").length; i++){
 					sb.append(message.split(" ")[i]);
 					sb.append("+");
 				}
 				sb.deleteCharAt(sb.length()-1);
 			sendMessage(channel, "http://www.lmgtfy.com/?q=" + sb.toString());
 			break;
 			case "!vend":
 				if(vendItems.isEmpty()){
 					try{
 						URL vend = new URL("http://itvends.com/vend.php?action=vend&format=text&count=10");
 						URLConnection vendC = vend.openConnection();
 						Scanner in = new Scanner(vendC.getInputStream());
 						while(in.hasNextLine()){
 							vendItems.add(in.nextLine());
 						}
 						in.close();
 					} catch(Exception e){sendMessage(channel, e.toString());}
 				}
 				sendAction(channel, "vends " + vendItems.get(0));
 				vendItems.remove(0);
 			break;
 			case "!lotion":
 				sendAction(channel, "gives " + message.split(" ")[1] + " some burn lotion");
 			break;
 			case "!nick":
 				if(sender.toLowerCase().equals("zifnab")){
 					if(message.split(" ").length == 2){
 						changeNick(message.split(" ")[1]);
 					}
 				}
 			break;
 			case "!isup":
 				boolean isReachable = false;
 				int timeout = 1000;
 				String address = message.split(" ")[1];
 
 				try {
 					isReachable = InetAddress.getByName(address).isReachable(timeout);
 					sendMessage(channel, address + " is online.");
 				} catch(Exception e){
 					sendMessage(channel, address+ " is offline.");
 				}
 			break;
 			case "!learn":
                                 nick = findUser(sender, names);
                                 if (nick.isOp()){
 					try{
 						sendMessage(channel, learnWord(message));
 					}catch (Exception e){sendMessage(channel, e.toString());}
 				}
 			break;
 			case "!update":
                                 nick = findUser(sender, names);
                                 if (nick.isOp()){
 					try {
 						sendMessage(channel, updateWord(message));
 					}catch (Exception e){sendMessage(channel, e.toString());}
 				}
 			break;
 			case "!learned":
 				try {
 					File dir = new File("words/");
 					File[] list = dir.listFiles();
					Arrays.sort(list);
 					StringBuilder files = new StringBuilder();
 					for (File x : list){
						if(x.toString().contains(" ")){
							files.append("\'" + x.toString().substring(6) + "\'");
						} else {
							files.append(x.toString().substring(6));
						}
						files.append(" ");
 					}
 					sendMessage(channel, files.toString());
 				} catch (Exception e){sendMessage(channel, e.toString());}
 
 			break;
 			default:
 				try{
 					sendMessage(channel, lookupWord(message));
 				} catch (Exception e){}
 
 			break;
 		}
 
 	}
 	public void onPrivateMessage(String sender, String login, String hostname, String message){
 		if (sender.toLowerCase().equals("zifnab")){
 			if(message.split(" ")[0].toLowerCase().equals("sm")){
 				sendMessage(message.split(" ")[1], message.split(message.split(" ")[0] + " " + message.split(" ")[1] + " ")[1]);
 			}
 		}
 	}
 	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
 		if (recipientNick.equalsIgnoreCase(getNick())) {
 			joinChannel(channel);
 		}
 	}
 	public void onDisconnect(){
 		try{
 			Thread.sleep(10000);
 			reconnect();
 		} catch (Exception e){
 			e.printStackTrace();
 			onDisconnect();
 		}
 	}
 	public void onServerResponse(int code, String response){
 		System.out.print("");
 	}
 	public void onJoin(String channel, String sender, String login, String hostname){
 		if(channel.toLowerCase().equals("#droid-dna")){
 			if(login.toLowerCase().equals("webchat")){
 				sendNotice(sender, Colors.RED + Colors.BOLD +
 				"Welcome to #Droid-DNA! If you are attempting to unlock your device, please read this tutorial IN FULL: " +
 				"http://forum.xda-developers.com/showthread.php?p=36976137#post36976137 We cannot help you unless you attempt to help yourself. ");
 				sendNotice(sender, Colors.RED + Colors.BOLD + 
 				"Please ask your question, and wait. We're human too, and this isn't our entire life, and none of us are paid to be here. " +
 				"Google is also your friend! Use it!");
 				sendNotice(sender, Colors.RED + Colors.BOLD + 
 				"If you accepted the OTA, you are SOL for root unless you unlocked your bootloader. A new exploit has NOT been discovered. " + 
 				"Asking for OTAs = BAN");
 			}
 		}
 	}
 	public User findUser(String nick, User[] names) {
 		for (User user : names){
 			if (user.equals(nick)) return user;
 		}
 		return null;
 	}
 	public boolean doSave(){
 		try {
 			File settings = new File("settings");
 			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(settings)));
 			out.println("nick=" + this.getName());
 			out.println("server=" + this.getServer());
 			out.println("nspass=" + nspass);
 			out.print("channels=");
 			for(int i=0; i<channels.size(); i++){
 				out.print(channels.get(i) + " ");
 			}
 			out.flush();
 			out.close();
 		} catch (Exception e){e.printStackTrace(); return false;}
 		return true;
 	}
 	public String lookupWord(String word) throws Exception{
 		String prefix = "words/";
 		word = word.substring(1).toLowerCase();
 		if (word.contains("..")) return "Nice Try";
 		File input = new File(prefix + word);
 		FileReader fr = new FileReader(input);
 		Scanner sc = new Scanner(fr);
 		StringBuilder sb = new StringBuilder();
 		while(sc.hasNextLine()){
 			sb.append(sc.nextLine());
 		}
 		sc.close();
 		fr.close();
 		return sb.toString();
 
 	}
 	public String learnWord(String word) throws Exception{
                 if (word.contains("..")) return "Nice Try";
 		word = word.substring(7);
 		String [] split = word.split(" as ");
 		split[0] = split[0].toLowerCase();
 		File output = new File("words/" + split[0].trim());
 		if(output.exists()) return "This file exists! Use \"!update command with text\" instead.";
 		FileWriter fw = new FileWriter(output);
 		BufferedWriter out = new BufferedWriter(fw);
 		out.write(split[1]);
 		out.newLine();
 		out.close();
 		return "Learned " + split[0] + " as " + split[1];
 	}
 	public String updateWord(String word) throws Exception{
                 if (word.contains("..")) return "Nice Try";
                 word = word.substring(8);
                 String [] split = word.split(" as ");
                 split[0] = split[0].toLowerCase();
                 File output = new File("words/" + split[0].trim());
                 if(!output.exists()) return "This file doesn't exist! Use \"!learn command as text\" instead.";
                 FileWriter fw = new FileWriter(output);
                 BufferedWriter out = new BufferedWriter(fw);
                 out.write(split[1]);
                 out.newLine();
                 out.close();
                 return "Learned " + split[0] + " as " + split[1];
 	}
 }

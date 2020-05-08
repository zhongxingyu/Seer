 import org.jibble.pircbot.*;
 import java.io.*;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.net.*;
 
 public class baseBot extends PircBot {
 	private String owner = null;
 	private String botName = null;
 	private String ircNet = null;
 	private ArrayList<String> channels = new ArrayList<String>();
 
 	public baseBot(){
 		try{
 
 			File settings = new File("settings.txt");
 			if(!settings.exists())
 			{
 				firstRun();
 			}
 
 			FileReader fr = new FileReader(settings);
 			BufferedReader br = new BufferedReader(fr);
 
 			owner = br.readLine();
 			botName = br.readLine();
 			ircNet = br.readLine();
 			String channel = br.readLine();
 			while(channel != null) {
 				channels.add(channel);
 				log(channel, "", "====Bot restarted====");
 				channel = br.readLine();
 			}
 		}
 		catch(Exception e){System.out.println(e.getMessage());}
 			
 
 		setName(botName);
 		log("","","=====Bot started=====");
 	}
 	
 	public static void main(String[] args){
 		baseBot bot = new baseBot();
 		bot.setVerbose(true);
 		try{
 			bot.connect(bot.ircNet);
 		}
 		catch (Exception e){
 			System.out.println("Can't connect: "+e);
 		}
 		bot.setVerbose(false);
 		for(String c : bot.channels) {
 			bot.joinChannel(c);
 		}
 	}
 
 	public void firstRun()
 	{
 		Scanner scan = new Scanner(System.in);
 		System.out.println("It appears that you haven't set up your bots settings. Please do that now.");
 		System.out.println("First, enter you IRC nick. This will set the owner of the bot, used for some commands.");
 		String owner = scan.nextLine();
 		System.out.println("Now, enter a name for your bot. It needs to be unique on the IRC network you are using.");
 		String botName = scan.nextLine();
 		System.out.println("Thirdly, enter the network URL of the IRC network you are using (eg irc.freenode.net).");
 		String ircNet = scan.nextLine();
 		System.out.println("now enter the names of the chatrooms you want to include, with all leading characters, seperated by spaces.");
 		String[] channel = scan.nextLine().split(" ");
 
 		try{
 			File f = new File("settings.txt");
 			FileWriter fw = new FileWriter(f, true);
 			fw.write(owner +"\n");
 			fw.write(botName+"\n");
 			fw.write(ircNet+"\n");
 			for(int i = 0; i < channel.length; i++)
 			{
 				fw.write(channel[i] + "\n");
 			}
 			fw.close();
 		}
 		catch(Exception e){System.out.println(e.getMessage());}
 	}
 		 
 
 	public void onMessage(String channel, String sender, String login, String hostname, String message)
 	{
 		DateFormat df = DateFormat.getInstance();
 
 		String time = df.format(new java.util.Date());
 		System.out.println(channel + ", " + time + " | " + sender + ": " + message);
 	
 		log(channel, sender, message);
 		if(message.equalsIgnoreCase(botName+": time"))
 		{
 			sendMessage(channel, sender + ": " + time);
 		}
 		else if(message.equalsIgnoreCase(botName+": about"))
 		{
 			sendMessage(channel, sender+": I am a bot written in java, operated by "+ owner);
 		}
 		else if(message.equalsIgnoreCase(botName+": quit") && sender.equalsIgnoreCase(owner))
 		{
 			sendMessage(channel, "Goodbye");
 			disconnect();
 		}
 		else if(message.indexOf(botName+": say") > -1 && sender.equalsIgnoreCase(owner))
 		{
 			String[] parts = message.split(" ");
 			String chan = parts[2];
 			String toSend = "";
 			int i = 3;
 			String temp = "";
 			while((temp = parts[i]) != null)
 			{
 				toSend += " "+temp;
 				i++;
 			}
 			sendMessage(chan, toSend);
 		}
 
 		else if(message.indexOf(botName+": join") > -1 && sender.equalsIgnoreCase(owner))
 		{
 			String newChan = message.split("join ")[1];
 			joinChannel(newChan);
 		}
 		else if(message.indexOf(botName) < 1 && message.indexOf(botName) > -1)
 		{
 			String toAI = message.split(botName+": ")[1];
 			sendMessage(channel, sender + ai(toAI));
 		}
 	}
 	public void onPart(String channel, String sender, String login, String hostname)
 	{
 		System.out.println(channel+": ====="+sender+" has left====");
 		log(channel, sender, "--Has quit--");
 	}
 
 	public void onJoin(String channel, String sender, String login, String hostname)
 	{
 		System.out.println(channel+": ===="+sender+" has joined====");
 		log(channel, sender, "--Has joined--");
 	}
 
 	
 	public void onDisconnect()
 	{
 
 		System.exit(0);
 	}
 	
 	public void onKick(String channel, String kicker, String kickerLogin, String kickerHost,String nick, String reason)
 	{
 		if(nick.equalsIgnoreCase(botName))
 		{
 			System.out.println("======Bot kicked by "+kicker+" because "+reason+"=====");
 			System.out.println("======Rejoining=======");
 			joinChannel(channel);
 		}
 	}
 
 	public void log(String channel, String sender, String message)
 	{
 		DateFormat df = DateFormat.getInstance();
 
 		String time = df.format(new java.util.Date());
 		try{
 			boolean s = (new File("logs").mkdir());
 			File f = new File("logs/"+channel+".txt");
 			FileWriter fw = new FileWriter(f, true);
 			fw.write(time + "| " + sender + ": " + message + "\n");
 			fw.close();
 		}
 		catch(Exception e){System.out.println(e.getMessage());}
 	}
 
 
 	public String ai(String message)
 	{
 		String result =  "";
 		try{ 
 			URL url = new URL("http://kato.botdom.com");
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setDoOutput(true);
 			conn.setRequestMethod("POST");
 			conn.setInstanceFollowRedirects(true);	
 			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 			wr.write("m="+message);
 			wr.flush();
 			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;	
 			while ((line = rd.readLine()) != null) 
 			{
 				result += line + "\r\n";
 			}
 			
 			rd.close();
 			wr.close();
 		}
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage());
 		}
 
		String splitResult = result.split("<div id=\"originalmsg\">&#8220;"+message+"&#8221;</div>")[1];
 		String aiMessage = splitResult.split("</h2>")[0];
		aiMessage = aiMessage.replace("<h2>", "");
 
 		return aiMessage;
 	}		
 }
 

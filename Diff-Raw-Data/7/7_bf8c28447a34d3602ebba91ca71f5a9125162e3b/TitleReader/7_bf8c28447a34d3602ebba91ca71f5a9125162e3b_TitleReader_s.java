 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import AndrewCassidy.PluggableBot.PluggableBot;
 import AndrewCassidy.PluggableBot.Plugin;
 
 public class TitleReader implements Plugin {
 
 	private static final Pattern p = Pattern
 			.compile("\\<title\\>(.*)\\<\\/title\\>");
 
 	@Override
 	public String getHelp() {
 		return "Gets the title of pages from mentioned urls";
 	}
 
 	@Override
 	public void onAction(String sender, String login, String hostname,
 			String target, String action) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onJoin(String channel, String sender, String login,
 			String hostname) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onKick(String channel, String kickerNick, String kickerLogin,
 			String kickerHostname, String recipientNick, String reason) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		// separete input by spaces ( URLs don't have spaces )
 		String[] parts = message.split("\\s");
 
 		URLConnection connection = null;
 		// Attempt to convert each item into an URL.
 		for (String item : parts)
 			try {
 				URL url = new URL(item);
 				System.out.println("'" + url.getProtocol() + "'");
 
 				if (url.getProtocol().equals("http")
 						|| url.getProtocol().equals("http")) {
 					connection = url.openConnection();
 
 					if (connection.getContentType().startsWith("text/html")) {
 						System.out.println(connection.getContentType());
 
 						BufferedReader in = new BufferedReader(
 								new InputStreamReader(url.openStream()));
 
 						String inputLine;
 						StringBuilder sb = new StringBuilder();
 
 						while ((inputLine = in.readLine()) != null)
 							sb.append(inputLine);
 
 						Matcher m = p.matcher(sb);
 						if (m.find()) {
 							PluggableBot.Message(channel, url.toString() + ": " +  m.group(1));
 							System.out.println(url.toString() + ": " +  m.group(1));
 						}
 					}
 
 				}
 
 			} catch (MalformedURLException e) {
 			} catch (IOException e) {
 			} finally {
 			}
 
 	}
 
 	@Override
 	public void onPart(String channel, String sender, String login,
 			String hostname) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onPrivateMessage(String sender, String login, String hostname,
 			String message) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onQuit(String sourceNick, String sourceLogin,
 			String sourceHostname, String reason) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void unload() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public static void main(String[] args) {
 		TitleReader t = new TitleReader();
 		t.onMessage(null, null, null, null, "hello http://google.com fish");
 		System.out.println();
 		t.onMessage(null, null, null, null, "hello google.com fish");
 
 	}
 }

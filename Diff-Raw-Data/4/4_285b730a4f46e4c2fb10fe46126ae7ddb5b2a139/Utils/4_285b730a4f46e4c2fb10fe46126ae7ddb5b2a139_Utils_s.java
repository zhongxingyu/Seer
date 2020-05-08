 package net.esper.tacos.ircbot.utils;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.Hashtable;
 import java.util.Random;
 import javax.naming.NamingException;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.InitialDirContext;
 
 import net.esper.tacos.ircbot.utils.mcping.MinecraftPing;
 import net.esper.tacos.ircbot.utils.mcping.MinecraftPingReply;
 
 import org.pircbotx.Colors;
 
 public class Utils {
 	
 	private static DatagramSocket udpSocket;
 	
 	public static String compileMessage(int startIndex, String[] args) {
 		String msg = "";
 		for (int i = startIndex; i < args.length; i++) {
 			msg += args[i] + " ";
 		}
 		
 		return msg;
 	}
 	
 	
 	public static String getFormattedPing(String arg1, int arg2) {
 		try {
 			System.out.println("Attempting to ping " + arg1 + " with port " + arg2);
 			MinecraftPingReply mpr = MinecraftPing.getPing(arg1, (arg2));
 			return processColorCodes(mpr.getMotd()) + " - " + mpr.getProtocolVersion() + " - " + mpr.getOnlinePlayers() + "/" + mpr.getMaxPlayers();
 		} catch (IOException e) {
 			return null;
 		}
 	}
 	
 	public static String processColorCodes(String message) {
 		char mccode = '\u00a7';
 		String repl = message;
 		repl = repl.replace(mccode + "4", Colors.RED);
 		repl = repl.replace(mccode + "c", Colors.RED);
 		repl = repl.replace(mccode + "6", Colors.BROWN);
 		repl = repl.replace(mccode + "e", Colors.YELLOW);
 		repl = repl.replace(mccode + "2", Colors.DARK_GREEN);
 		repl = repl.replace(mccode + "a", Colors.GREEN);
 		repl = repl.replace(mccode + "b", Colors.CYAN);
 		repl = repl.replace(mccode + "3", Colors.BLUE);
 		repl = repl.replace(mccode + "1", Colors.DARK_BLUE);
 		repl = repl.replace(mccode + "9", Colors.BLUE);
 		repl = repl.replace(mccode + "d", Colors.MAGENTA);
 		repl = repl.replace(mccode + "5", Colors.PURPLE);
 		repl = repl.replace(mccode + "f", Colors.NORMAL);
 		repl = repl.replace(mccode + "7", Colors.LIGHT_GRAY);
 		repl = repl.replace(mccode + "8", Colors.DARK_GRAY);
 		repl = repl.replace(mccode + "0", Colors.BLACK);
 		repl = repl.replace(mccode + "k", Colors.REVERSE);
 		repl = repl.replace(mccode + "l", Colors.BOLD);
 		repl = repl.replace(mccode + "m", Colors.UNDERLINE);
 		repl = repl.replace(mccode + "n", Colors.UNDERLINE);
 		repl = repl.replace(mccode + "o", Colors.WHITE);
 		repl = repl.replace(mccode + "r", Colors.NORMAL);
 		return repl + Colors.NORMAL;
 	}
 	
 	// Fields
 	static private final Random rand = new Random(System.currentTimeMillis());
 	
 	// Methods
 	/**
 	 * Get a random number between 0 and the max number specified.
 	 * 
 	 * @param max the maximum number of the random.
 	 * @return a random number.
 	 */
 	static public int getRandom(int max) {
 		return rand.nextInt(max);
 	}
 }

 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
//TODO Fix line splitting

 /**
  * QDB
  *
  * @author Michael Mrozek
  *         Created Jul 12, 2010.
  */
 public class QDB extends NoiseModule {
 	private static final String COLOR_ERROR = RED;
 	private static final String COLOR_QUOTE = CYAN;
 	private static final int PERIOD = 30; // seconds
 
 	private final Timer timer = new Timer();
 	private int curID = 0;
 	
 	@Override public void init(NoiseBot bot) {
 		super.init(bot);
 		this.timer.scheduleAtFixedRate(new TimerTask() {
 			@Override public void run() {
 				QDB.this.checkForNewQuotes();
 			}
		}, 0, PERIOD);
 	}
 
 	@Override public void unload() {
 		super.unload();
 		this.timer.cancel();
 	}
 
 	@Command("\\.(?:qdb|quote) ([0-9]+)")
 	public void show(Message message, int id) {
 		try {
 			boolean skippedLineMarker = false;
 			for(String line : getQuote(id)) {
 				if(!skippedLineMarker) {
 					skippedLineMarker = true;
 					continue;
 				}
 				
 				this.bot.sendMessage(COLOR_QUOTE + line);
 			}
 		} catch(IOException e) {
 			this.bot.reply(message, COLOR_ERROR + "Unable to connect to QDB");
 			e.printStackTrace();
 		}
 	}
 
 	@Command("\\.(?:qdb|quote)")
 	public void showRandom(Message message) {
 		show(message, 0);
 	}
 
 	private void checkForNewQuotes() {
 		int maxID;
 		try {
 			String[] lines = getQuote("max");
 			maxID = Integer.parseInt(lines[0]);
 		} catch(IOException e) {return;}
 		
 		if(this.curID == 0) {
 			this.curID = maxID;
 			return;
 		}
 		
 		if(maxID <= this.curID)
 			return;
 		
 		for(; this.curID < maxID; this.curID++) {
 			boolean skippedLineMarker = false;
 			try {
 				for(String line : getQuote(this.curID)) {
 					if(!skippedLineMarker) {
 						skippedLineMarker = true;
 						continue;
 					}
 					
 					this.bot.sendMessage(COLOR_QUOTE + line);
 				}
 				
 				this.bot.sendMessage(" -- http://lug.rose-hulman.edu/qdb/" + this.curID);
 			} catch(IOException e) {}
 		}
 	}
 	
 	private static String[] getQuote(int id) throws IOException {return getQuote("" + id);}
 	private static String[] getQuote(String id) throws IOException {
 		String url = "http://mrozekma.com/qdb.php";
 		if(id.equals("max") || Integer.parseInt(id) > 0)
 			url += "?id=" + id;
 		final URLConnection c = new URL(url).openConnection();
 		final Scanner s = new Scanner(c.getInputStream());
 		final Vector<String>  lines = new Vector<String>();
 		while(s.hasNextLine()) {
 			final String line = s.nextLine();
 			lines.add(line);
 		}
 		return lines.toArray(new String[0]);
 	}
 	
 	@Override public String getFriendlyName() {return "QDB";}
 	@Override public String getDescription() {return "Displays quotes from the RHLUG Quote Database at http://lug.rose-hulman.edu/qdb/";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".qdb -- Shows a random short quote",
 				".qdb _id_ -- Shows quote _id_",
 				".quote _id_ -- Same as .qdb"
 		};
 	}
 	@Override public String getOwner() {return "Morasique";}
 }

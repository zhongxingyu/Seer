 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import debugging.Log;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 import static panacea.Panacea.*;
 
 /**
  * QDB
  *
  * @author Michael Mrozek
  *         Created Jul 12, 2010.
  */
 public class QDB extends NoiseModule {
 	private static class Quote implements Iterable<String> {
 		public final int id;
 		public final String[] lines;
 		public final int upvotes;
 		public final int downvotes;
 		
 		public Quote(int id, String[] lines, int upvotes, int downvotes) {
 			this.id = id;
 			this.lines = lines;
 			this.upvotes = upvotes;
 			this.downvotes = downvotes;
 		}
 		
 		private String getVoteString() {
 			return (this.upvotes >= 0 && this.downvotes >= 0) ? "(+" + this.upvotes + "/-" + this.downvotes + ")" : "(?/?)";
 		}
 		
 		public String[] render() {
 			final String[] rtn = new String[this.lines.length];
 			if(this.lines.length == 0) return rtn;
 			rtn[0] = this.getVoteString() + " " + this.lines[0];
 			System.arraycopy(this.lines, 1, rtn, 1, this.lines.length - 1);
 			return rtn;
 		}
 
 		@Override public Iterator<String> iterator() {
 			return Arrays.asList(this.render()).iterator();
 		}
 	}
 	
 	private static class ParseException extends Exception {
 		public ParseException(String message) {super(message);}
 	}
 	
 	private static final String COLOR_ERROR = RED;
 	private static final String COLOR_QUOTE = CYAN;
 	private static final int MAX_LINES = 2; // maximum lines in a random quote
 	private static final int PERIOD = 30; // seconds
 	private static final int TIMEOUT = 5; // seconds
 
 	private final Timer timer = new Timer();
 	private int curID = 0;
 	
 	@Override public void init(NoiseBot bot) {
 		super.init(bot);
 		this.timer.scheduleAtFixedRate(new TimerTask() {
 			@Override public void run() {
 				QDB.this.checkForNewQuotes();
 			}
 		}, 0, PERIOD * 1000);
 	}
 
 	@Override public void unload() {
 		super.unload();
 		this.timer.cancel();
 	}
 
 	@Command("\\.(?:qdb|quote) ([0-9]+)")
 	public void show(Message message, int id) {
 		try {
 			for(String line : getQuote(id))
 				this.bot.sendMessage(COLOR_QUOTE + line);
 		} catch(ParseException e) {
 			this.bot.reply(message, COLOR_ERROR + e.getMessage());
 			Log.e(e);
 		} catch(IOException e) {
 			this.bot.reply(message, COLOR_ERROR + "Unable to connect to QDB");
 			Log.e(e);
 		}
 	}
 
 	@Command("\\.(?:qdb|quote)")
 	public void showRandom(Message message) {
 		try {
 			final int maxId = getMaxID();
 
 			Quote quote = null;
 			do {
 				try {
 					quote = getQuote(getRandomInt(1, maxId));
 				} catch(ParseException e) {} // Probably the particular quy
 			} while(quote == null || quote.lines.length > MAX_LINES);
 			
 			for(String line : quote)
 				this.bot.sendMessage(COLOR_QUOTE + line);
 		} catch(ParseException e) {
 			this.bot.reply(message, COLOR_ERROR + e.getMessage());
 			Log.e(e);
 		} catch(IOException e) {
 			this.bot.reply(message, COLOR_ERROR + "Unable to connect to QDB");
 			Log.e(e);
 		}
 	}
 
 	private void checkForNewQuotes() {
 		final int maxID;
 		try {
 			maxID = getMaxID();
 		} catch(IOException e) {
 			Log.w(e);
 			return;
 		} catch(ParseException e) {
 			Log.e(e);
 			return;
 		}
 		
 		if(this.curID == 0) {
 			this.curID = maxID;
 			Log.i("Initial QDB poll; set current ID to " + this.curID);
 			return;
 		}
 
 		if(maxID == this.curID) {
 			return;
 		} else if(maxID < this.curID) {
 			Log.e("QDB ID mismatch: " + maxID + " < " + this.curID);
 		}
 	
		Log.i("QDB poll; old ID was " + this.curID + ", new ID is " + maxID);
 		for(this.curID++; this.curID <= maxID; this.curID++) {
 			// try {
 				// for(String line : getQuote(this.curID))
 					// this.bot.sendMessage(COLOR_QUOTE + line);
 				this.bot.sendMessage("http://lug.rose-hulman.edu/qdb/" + this.curID);
 			// } catch(IOException e) {
 			// } catch(ParseException e) {}
 		}
 	}
 	
 	private static Quote getQuote(int id) throws IOException, ParseException {
 		final Document doc = Jsoup.connect("http://lug.rose-hulman.edu/qdb/" + id).timeout(TIMEOUT * 1000).get();
 
 		int upvotes = -1, downvotes = -1;
 		try {
 			final Elements scoreSpan = doc.select("span.quote-rating");
 			if(!scoreSpan.isEmpty()) {
 				final int score = Integer.parseInt(scoreSpan.first().text().replace((char)8722, '-').replaceFirst("^\\+", ""));
 				final Elements totalSpan = doc.select("span.quote-vote-count");
 				if(!totalSpan.isEmpty() && totalSpan.first().text().charAt(0) == '/') {
 					final int total = Integer.parseInt(totalSpan.first().text().substring(1));
 					
 					// Andy depresses me
 					/*
 					int upvotes = 0, downvotes = 0;
 					if(score >= 0)
 						upvotes = score;
 					else
 						downvotes = Math.abs(score);
 					final int diff = total - Math.abs(score);
 					assert(diff % 2 == 0);
 					upvotes += diff/2;
 					downvotes += diff/2;
 					*/
 					upvotes = (total+score) / 2;
 					downvotes = (total-score) / 2;
 				}
 			}
 		} catch(NumberFormatException e) {Log.e(e);}
 
 		Elements e = doc.select("p");
 		if(e.isEmpty())
 			throw new ParseException("Unable to find quote block");
 		String[] lines = e.first().html().replace("&nbsp;", " ").split("<br />");
 		String[] rtn = new String[lines.length];
 		for(int i = 0; i < lines.length; i++)
 			rtn[i] = Jsoup.parse(lines[i]).text();
 		return new Quote(id, rtn, upvotes, downvotes);
 	}
 	
 	private static int getMaxID() throws IOException, ParseException {
 		final Document doc = Jsoup.connect("http://lug.rose-hulman.edu/qdb/browse").timeout(TIMEOUT * 1000).get();
 		Elements e = doc.select("ul.quote-list > li");
 		if(e.isEmpty())
 			throw new ParseException("Unable to find top quote on browse page");
 		final String id = e.first().id();
 		if(!id.startsWith("quote-"))
 			throw new ParseException("Unexpected ID on top quote on browse page");
 		return Integer.parseInt(id.substring("quote-".length()));
 	}
 	
 	// Old PHP method
 	/*
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
 	*/
 	
 	@Override public String getFriendlyName() {return "QDB";}
 	@Override public String getDescription() {return "Displays quotes from the RHLUG Quote Database at http://lug.rose-hulman.edu/qdb/";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".qdb -- Shows a random short quote",
 				".qdb _id_ -- Shows quote _id_",
 				".quote _id_ -- Same as .qdb"
 		};
 	}
 }

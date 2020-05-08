 import org.pircbotx.PircBotX;
 import org.pircbotx.exception.NickAlreadyInUseException;
 
 import database.Database;
 
 import achievements.*;
 import commands.*;
 
 
 public class IRCBot {
 
 	/**
 	 * IRCBot takes the NickServ password as an argument via CLI.
 	 */
 	public static void main(String[] args) {
 		if(args.length < 1 || args.length > 1) {
 			System.err.println("This program requires that the NickServ pass be the only argument.");
 			System.exit(1);
 		}
 		PircBotX bot = new PircBotX();
 		Database db = null;
 		try {
 			db = Database.getInstance();
 		} catch (Exception e) {
 			System.err.println("Unable to load JDBC driver, so unable to continue execution of program. Sorry :(");
 			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		}
 		setUpBot(bot);
 		addModules(bot, db);
 		try {
 			bot.connect("irc.us.ponychat.net");
 			bot.joinChannel("#tulpa");
 		} catch (NickAlreadyInUseException e) {
 			System.err.println("Our nick is already in use! :(");
 			System.exit(1);
 		} catch (Exception e) {
 			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		}
 		bot.sendMessage("NickServ", "IDENTIFY " + args[0]);
 		bot.sendMessage("HostServ", "ON"); // attempt to turn on our VHOST
		while(true) { // run until forcibly terminated
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) { } // ignore
		}
 	}
 	
 	/**
 	 * Configures various settings such as the bot's name, version info, and more.
 	 * @param bot the bot to configure settings for
 	 */
 	private static void setUpBot(PircBotX bot) {
 		bot.setName("AchievementBot");
 		bot.setLogin("Two");
 		bot.setVersion("AchievementBot v. 1.01");
 		bot.setFinger("Hey! Save that for the second date <3");
 		bot.setAutoReconnect(true);
 		bot.setAutoReconnectChannels(true);
 		bot.setVerbose(true);
 		bot.setCapEnabled(true);
 	}
 	
 	/**
 	 * Adds various event handlers to the bot, which drive the logic behind the desired behaviors.
 	 * @param bot the bot to add handlers to
 	 */
 	private static void addModules(PircBotX bot, Database db) {
 		/* achievements */
 		bot.getListenerManager().addListener(new CruiseControl(db));
 		bot.getListenerManager().addListener(new Lollerskating(db));
 		bot.getListenerManager().addListener(new Roflcopter(db));
 		bot.getListenerManager().addListener(new OMFGWTFNukes(db));
 		bot.getListenerManager().addListener(new TheAdvertiser(db));
 		bot.getListenerManager().addListener(new TheSpammer(db));
 		bot.getListenerManager().addListener(new DoYouEvenContent(db));
 		bot.getListenerManager().addListener(new PopularityContest(db));
 		bot.getListenerManager().addListener(new AttentionWhore(db));
 		bot.getListenerManager().addListener(new ForeverAlone(db));
 		bot.getListenerManager().addListener(new Actor(db));
 		bot.getListenerManager().addListener(new TheManyMoods(db));
 		
 		/* commands */
 		bot.getListenerManager().addListener(new AchievementsList(db));
 		bot.getListenerManager().addListener(new Help(db));
 		bot.getListenerManager().addListener(new Say(db));
 		bot.getListenerManager().addListener(new Score(db));
 	}
 	
 }

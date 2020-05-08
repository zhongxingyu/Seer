 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 import static panacea.Panacea.*;
 
 public class Christmas extends NoiseModule {
 	private static String[] lyrics = {
		"Deck the halls with bought of holly,~Fa la la la la la, la la la la.~Tis the season to by jolly,~Fa la la la la la, la la la la.",
 		"Frosty the snowman was a jolly happy soul~With a corncob pipe and a button nose~and two eyes made out of coal~Frosty the snowman is a fairy tale they say~He was made of snow but the children~know how he came to life one day",
 		"Have yourself a merry little Christmas.~Let your heart be light,~From now on our troubles~Will be out of sight.",
 		"Jingle bells~jingle bells~jingle all the way!~O what fun it is to ride~In a one-horse open sleigh",
		"Rudolph the Red-Nosed Reindeer~Had a very shiny nose,~And if you ever saw it,~You could even say it glows."
 	};
 
 
 	@Override public void init(NoiseBot bot) {
 		super.init(bot);
 		talked(null);
 	}
 
 	@Command("[^\\.].*")
 	public void talked(Message message) {
 		if(getRandomInt(0, 10) == 0)
 			this.bot.sendMessage(GREEN + "Ho " + RED + "Ho " + GREEN + "Ho!");
 	}
 
 	@Command("\\.jingle")
 	public void jingle(Message message) {
 		String song = getRandom(lyrics);
 		for(String line : song.split("~")) {
 			this.bot.sendMessage(BLUE + line);
 		}
 	}
 
 	@Override public String getFriendlyName() {return "Christmas";}
 	@Override public String getDescription() {return "Says `Ho Ho Ho' a lot, and maybe other stuff";}
 	@Override public String[] getExamples() {
 		return new String[] {
 		};
 	}
 	@Override public String getOwner() {return "Morasique";}
 }

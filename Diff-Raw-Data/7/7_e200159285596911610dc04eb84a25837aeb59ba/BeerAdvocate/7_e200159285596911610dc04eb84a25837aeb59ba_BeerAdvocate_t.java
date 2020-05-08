 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.IOException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import main.Message;
 import main.NoiseModule;
 import static main.Utilities.*;
 
 /**
  * Beer Advocate module
  *
  * @author Michael Auchter
  *         Created Sweetmorn, the 22nd day of Bureaucracy in the YOLD 3178
  */
 public class BeerAdvocate extends NoiseModule {
   private static final String COLOR_ERROR = RED + REVERSE;
 
   // Yeah, you wanna fight?
   public static String extract(Document page, String selector)
   {
     Element node = page.select(selector).first();
     if (node == null)
       return "";
     return node.text();
   }
 
   @Command(".*(http://beeradvocate.com/beer/profile/[0-9]+/[0-9]+).*")
   public void beer(Message message, String beerUrl)
   {
     Document page = null;
     try {
       page = Jsoup.connect(beerUrl).timeout(10000).get();
     } catch (IOException e) {
       e.printStackTrace();
       this.bot.sendMessage(COLOR_ERROR + "Error retrieving BA page...");
     }
 
    String name = extract(page, ".titleBar");
     String score = extract(page, ".BAscore_big");
     String style = extract(page, "[href^=/beer/style/]");
 
    this.bot.sendMessage(name + " - " + style + " - " + score);
   }
 
   @Override
   public String getFriendlyName() {
     return "BeerAdvocate";
   }
 
   @Override
   public String getDescription() {
     return "Returns information about a beer when a BeerAdvocate link appears...";
   }
 
   @Override
   public String[] getExamples() {
     return new String[] { 
       "http://beeradvocate.com/beer/profile/192/83920/"
     };
   }
 }

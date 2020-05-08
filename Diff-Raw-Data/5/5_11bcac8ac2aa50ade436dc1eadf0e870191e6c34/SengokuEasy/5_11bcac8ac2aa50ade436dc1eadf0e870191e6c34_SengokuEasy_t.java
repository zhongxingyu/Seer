 package nexi.sengoku.easy;
 
 import java.io.File;
 import java.io.FileReader;
 import java.util.Properties;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 public class SengokuEasy {
 
 	private static final Logger logger = Logger.getLogger(SengokuEasy.class);
 
 	public static void main (String... args) throws Exception {
		
 		BasicConfigurator.configure();
 		Logger.getRootLogger().setLevel(Level.INFO);
 		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.ERROR);
 		Logger.getLogger("org.apache.http").setLevel(Level.ERROR);
 		Logger.getLogger("nexi.sengoku.easy.Auth").setLevel(Level.WARN);
 
 		Properties properties = new Properties();
 		properties.load(new FileReader(new File("sengoku.properties")));
 		
 		logger.info("logging in to yahoo");
 		HtmlPage page = new Auth(properties).loginToYahooWithRetry();
 		logger.info("logged in to yahoo");
 
 		logger.info("logging in to world server");
		HtmlPage worldsPage = page.getAnchorByText("ゲームスタート").click();
 		logger.info("logged in to world server");
 		
 		World world9 = new World(9, worldsPage.getUrl().toString());
 		world9.load();
 		
 		Thread.sleep(30000L);
 	}
 }

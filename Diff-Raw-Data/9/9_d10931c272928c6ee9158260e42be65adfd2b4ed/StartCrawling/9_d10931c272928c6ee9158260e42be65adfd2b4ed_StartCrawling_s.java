 package novajoy.crawler;
 
 import novajoy.util.config.IniWorker;
 import novajoy.util.db.JdbcManager;
 
 public class StartCrawling {
 	public static void main(String[] args) throws Exception {
 		IniWorker config = new IniWorker(
				"/home/ubuntu/NovaJoy/config/config.ini");
 		JdbcManager dbman = new JdbcManager(config.getDBaddress(),
 				config.getDBbasename(), config.getDBuser(),
 				config.getDBpassword());
 		new Crawler("Crawler_1", config.getCrawlerSleepTime(), dbman).start();
 	}
 }

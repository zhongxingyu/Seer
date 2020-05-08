 package info.balajeerc.spellspider;
 
 
 import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
 import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
 import edu.uci.ics.crawler4j.crawler.CrawlConfig;
 import edu.uci.ics.crawler4j.crawler.CrawlController;
 import edu.uci.ics.crawler4j.fetcher.PageFetcher;
 
 public class Main {
     public static void main(String[] args){
     	String baseUrl = args[0];
     	String baseDirPath = args[1];
     	
    	System.out.println("Base url at: "+baseUrl);
    	System.out.println("Base directory at: "+baseDirPath);
     	        
     	String crawlStorageFolder = baseDirPath;
         int numberOfCrawlers = 15;
 
         CrawlConfig config = new CrawlConfig();
         config.setCrawlStorageFolder(crawlStorageFolder);
         config.setMaxDepthOfCrawling(1000);
         config.setFollowRedirects(true);
 
         /*
          * Instantiate the controller for this crawl.
          */
         PageFetcher pageFetcher = new PageFetcher(config);
         RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
         RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
         CrawlController controller = null;
         try{
         	controller = new CrawlController(config, pageFetcher, robotstxtServer);
         }catch(Exception e){
         	System.out.println(e.getMessage());
         }
         /*
          * For each crawl, you need to add some seed urls. These are the first
          * URLs that are fetched and then the crawler starts following links
          * which are found in these pages
          */
         OutputQueue.CreateInstance(baseDirPath);
         PageChecker.CreateInstance(OutputQueue.GetInstance());
         SpellSpider.SetBaseUrl(baseUrl);
         controller.addSeed(baseUrl);
 
         /*
          * Start the crawl. This is a blocking operation, meaning that your code
          * will reach the line after this only when crawling is finished.
          */
         controller.start(SpellSpider.class, numberOfCrawlers);        
     }
 
 }

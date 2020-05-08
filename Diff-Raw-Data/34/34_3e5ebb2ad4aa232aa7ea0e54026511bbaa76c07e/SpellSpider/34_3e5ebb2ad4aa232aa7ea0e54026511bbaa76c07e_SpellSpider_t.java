 package info.balajeerc.spellspider;
 
 import java.util.regex.Pattern;
 
 import edu.uci.ics.crawler4j.crawler.WebCrawler;
 import edu.uci.ics.crawler4j.crawler.Page;
 import edu.uci.ics.crawler4j.url.WebURL;
 import edu.uci.ics.crawler4j.parser.HtmlParseData;
 
 
 public class SpellSpider extends WebCrawler{
 	private static String BaseUrl;
 	private PageChecker checker;
 	private OutputQueue outputQueue;
 	
     private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
                                                       + "|png|tiff?|mid|mp2|mp3|mp4"
                                                       + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
                                                       + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
     public SpellSpider(){
     	checker = PageChecker.GetInstance();
     	outputQueue = OutputQueue.GetInstance();
     }
     
     /**
      * This function to specifies whether
      * the given url should be crawled or not (based on your
      * crawling logic).
      */
     @Override
     public boolean shouldVisit(WebURL url) {
             String href = url.getDomain();           
             boolean patternMatch = BaseUrl.contains(href);
             boolean result = !FILTERS.matcher(href).matches() && patternMatch;
//            System.out.println("Checking if url:"+href+"must be crawled: "+String.valueOf(result));
             return result;
     }
 
     /**
      * This function is called when a page is fetched and ready 
      * to be processed by your program.
      */
     @Override
     public void visit(Page page) {          
             String url = page.getWebURL().getURL();
             System.out.println("URL: " + url);
            
             if (page.getParseData() instanceof HtmlParseData) {
                     HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                     String text = htmlParseData.getText();
                     outputQueue.addText(text);
                     //Send the page checker the same text
                     //to check for errors
                     checker.checkText(text, url);
             }
     }
     
     public static void SetBaseUrl(String baseUrl){
     	BaseUrl = baseUrl;
     }
 }

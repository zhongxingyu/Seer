 package com.tidings.backend;
 
 import com.tidings.backend.repository.NewsItemsRepository;
 import messagepassing.pipeline.Message;
 import messagepassing.pipeline.Pipeline;
 import org.jetlang.channels.MemoryChannel;
 import org.jetlang.fibers.ThreadFiber;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class PipelineTest {
 
     @Test
     public void shouldExecuteAllStagesOfThePipeline() {
         Pipeline pipeline = new Pipeline();
         final MemoryChannel<Message> crawlInbox = new MemoryChannel<Message>();
         MemoryChannel<Message> transformInbox = new MemoryChannel<Message>();
         MemoryChannel<Message> loaderInbox = new MemoryChannel<Message>();
         ThreadFiber crawlWorker = new ThreadFiber();
         ThreadFiber transformWorker = new ThreadFiber();
         ThreadFiber loadingWorker = new ThreadFiber();
 
         FeedCrawlStage crawlStage = new FeedCrawlStage(crawlInbox, transformInbox, crawlWorker);
         TransformStage trasformStage = new TransformStage(transformInbox, loaderInbox, transformWorker);
         LoadingStage loadingStage = new LoadingStage(loaderInbox, null, loadingWorker, new NewsItemsRepository());
         pipeline.addStage(crawlStage);
         pipeline.addStage(trasformStage);
         pipeline.addStage(loadingStage);
         pipeline.start();
         crawlInbox.publish(new Message(bigDataFeeds()));
 //        final Message crawlTrigger = new Message(new CrawlableSources());
 
 //        ThreadFiber scheduler = new ThreadFiber();
 //        Disposable scheduleWorker = scheduler.scheduleAtFixedRate(new Runnable() {
 //            @Override
 //            public void run() {
 //                System.out.println("******************************************");
 //                System.out.println("Crawl Triggered at " + new Date());
 //                System.out.println("******************************************");
 //                crawlInbox.publish(crawlTrigger);
 //            }
 //        }, 0, 120, TimeUnit.MINUTES);
 //        scheduler.start();
         try {
             crawlWorker.join();
             transformWorker.join();
             loadingWorker.join();
 
             crawlWorker.dispose();
             transformWorker.dispose();
             loadingWorker.dispose();
 //            scheduleWorker.dispose();
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     }
 
     private List<String> bigDataFeeds() {
         String[] feeds = {"http://allthingsd.com/tag/big-data/feed/",
                 "http://feeds.feedburner.com/TdwiChannel-BigDataAnalytics",
                 "http://blogs.teradata.com/rss/",
                 "http://hortonworks.com/feed/",
                 "http://www.zdnet.com/blog/big-data/rss.xml",
                 "http://feeds.feedburner.com/QlikviewMarketingIntelligenceBlog",
                 "http://www.thebigdatainsightgroup.com/site/rss/articles/all",
                 "http://www.smartercomputingblog.com/category/big-data/feed/",
         };
         return Arrays.asList(feeds);
     }
 
    private List<String> feeds() {
        String[] feeds = {"http://feeds.feedburner.com/TheShillongTimes?format=xml",
                "http://www.thehindu.com/?service=rss",
                "http://www.thehindu.com/news/?service=rss",
                "http://www.thehindu.com/opinion/?service=rss",
                "http://www.thehindu.com/business/?service=rss",
                "http://www.thehindu.com/sport/?service=rss",
                "http://www.thehindu.com/arts/?service=rss",
                "http://www.thehindu.com/life-and-style/?service=rss",
                "http://www.thehindu.com/health/?service=rss",
                "http://www.thehindu.com/education/?service=rss",
                "http://www.thehindu.com/sci-tech/?service=rss",
                "http://timesofindia.indiatimes.com/rssfeedstopstories.cms",
                "http://timesofindia.indiatimes.com/rssfeeds/1221656.cms",
                "http://timesofindia.indiatimes.com/rssfeeds/-2128936835.cms",
                "http://syndication.indianexpress.com/rss/latest-news.xml",
                "http://syndication.indianexpress.com/rss/33/front-page.xml",
                "http://syndication.indianexpress.com/rss/698/science-&-technology.xml",
                "http://syndication.indianexpress.com/rss/798/latest-news.xml",
                "http://www.deccanchronicle.com/rss/nation/rss.xml",
                "http://www.deccanchronicle.com/rss/cities/rss.xml",
                "http://www.deccanchronicle.com/rss/business/rss.xml",
                "http://www.deccanchronicle.com/rss/Showbiz/rss.xml",
                "http://www.deccanherald.com/rss/news.rss",
                "http://www.deccanherald.com/rss/business.rss",
                "http://www.deccanherald.com/rss/national.rss",
                "http://economictimes.indiatimes.com/rssfeedsdefault.cms",
                "http://economictimes.indiatimes.com/rssfeedstopstories.cms",
                "http://economictimes.indiatimes.com/News/rssfeeds/1715249553.cms",
        };
        return Arrays.asList(feeds);
    }

     public static void main(String[] args) {
         new PipelineTest().shouldExecuteAllStagesOfThePipeline();
     }
 }
 

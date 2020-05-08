 package com.tidings.backend.pipelines.classification;
 
 
import com.tidings.backend.domain.Link;
 import com.tidings.backend.domain.NewsFeed;
 import com.tidings.backend.domain.NewsFeedBuilder;
 import com.tidings.backend.repository.TrainingRepository;
 import messagepassing.pipeline.Message;
 import messagepassing.pipeline.Stage;
 import org.jetlang.channels.Channel;
 import org.jetlang.fibers.Fiber;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class FeedCrawlStage extends Stage {
 
     public FeedCrawlStage(Channel<Message> inbox, Channel<Message> outbox, Fiber threadFiber) {
         super(inbox, outbox, threadFiber);
     }
 
     public void onMessage(Message message) {
        List<Link> feedList = (List<Link>) message.payload();
         ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (final Link link : feedList) {
            executorService.execute(pullFeedAndPublish(link.value()));
         }
     }
 
     private Runnable pullFeedAndPublish(final String url) {
         return new Runnable() {
             public void run() {
                 System.out.println("Pulling feed from: " + url);
                 NewsFeed feed = new NewsFeedBuilder(new TrainingRepository()).pullNewContents(url).extractFullText().instance();
                 if (feed != null)
                     publish(new Message(feed));
             }
         };
     }
 
 }

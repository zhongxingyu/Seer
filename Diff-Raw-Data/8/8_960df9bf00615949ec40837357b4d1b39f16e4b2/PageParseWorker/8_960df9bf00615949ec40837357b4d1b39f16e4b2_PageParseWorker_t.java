 package com.zibea.parser.core.workers;
 
 import com.zibea.parser.core.task.Task;
 import com.zibea.parser.core.utils.ZapOfferListingParser;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * @author: Mikhail Bragin
  */
 public class PageParseWorker implements Worker {
 
     private ExecutorService pageParsePool;
 
     private OfferParseWorker offerParseWorker;
 
     public PageParseWorker(OfferParseWorker offerParseWorker) {
         this.pageParsePool = Executors.newFixedThreadPool(1, new CustomThreadFactory("page-parse-worker"));
         this.offerParseWorker = offerParseWorker;
     }
 
     private AtomicLong tasksProduced = new AtomicLong();
 
     @Override
     public void addTask(Task task) {
 
         pageParsePool.submit(new ParseWorker(task) {
 
             @Override
             public void processTask() throws InterruptedException {
                 int attempt = 0;
                 boolean parsed = false;
                 while (!parsed) {
                     try {
                         attempt++;
 
                         //testUrl(this.currentTask.getUrl()); //already tested before in another thread
                         Document doc = Jsoup.connect(task.getUrl()).get(); //parseWithProxy();//
                         ZapOfferListingParser parser = new ZapOfferListingParser(doc, task, new HashSet<Long>()); //todo add ids
                         List<Task> preparedTasks = parser.parse();
 
                         for (Task task : preparedTasks) {
                             offerParseWorker.addTask(task);
                             tasksProduced.incrementAndGet();
                         }
                         parsed = true;
 
                     } catch (IOException e) {
 
                         if (attempt > 5) {
                             e.printStackTrace();
                             return;
                         }
 
                         Thread.sleep(1000);
                     }
                 }
             }
         });
     }
 
     public long getTasksProduced() {
         return tasksProduced.get();
     }
 }

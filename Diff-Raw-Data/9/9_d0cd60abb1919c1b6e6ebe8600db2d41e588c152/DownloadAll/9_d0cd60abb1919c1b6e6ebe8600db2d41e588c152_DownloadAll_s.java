 package at.yawk.fimfiction.api.downloadall;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import at.yawk.fimfiction.api.AccountInternetAccess;
 import at.yawk.fimfiction.api.DownloadType;
 import at.yawk.fimfiction.api.JSONStoryMeta;
 import at.yawk.fimfiction.api.SearchRequest;
 import at.yawk.fimfiction.api.StoryAccess;
import at.yawk.fimfiction.api.StoryMeta;
 import at.yawk.fimfiction.api.actions.Downloader;
 import at.yawk.fimfiction.api.factories.SearchRequestFactory;
 import at.yawk.fimfiction.api.parsers.CompatibilitySearchIterable;
 
 public class DownloadAll {
     private static final int MODES = 3;
     private static final int CONSOLE_WIDTH = 80;
     private static final int BORDER = 1;
     private static final int MODE_WIDTH = (CONSOLE_WIDTH - BORDER * (MODES - 1)) / MODES;
     private static final boolean COMPATIBILITY_MODE = true;
     
     private static String currentMessage;
     private static File output;
     private static ExecutorService epubDownloader;
     private static AccountInternetAccess ac;
     
     static {
         currentMessage = "";
         for (int i = 0; i < MODES; i++) {
             if (i > 0) {
                 currentMessage += '|';
             }
             currentMessage += repeat(' ', MODE_WIDTH);
         }
     }
     
     public static void main(String[] args) throws InterruptedException {
         if (args.length != 2) {
             System.err.println("Usage: <java> <username> <password>");
             return;
         }
         {
             ac = new AccountInternetAccess();
             if (!ac.login(args[0], args[1])) {
                 System.err.println("Invalid username or password!");
                 return;
             }
             ac.setMature(true);
         }
         output = new File("output");
         epubDownloader = Executors.newFixedThreadPool(5);
         final Runnable[] tasks = new Runnable[MODES];
         tasks[0] = new Task("unread", 0, new SearchRequestFactory().setFavorite(true).setUnread(true));
         tasks[1] = new Task("read later", 1, new SearchRequestFactory().setReadLater(true));
         tasks[2] = new Task("favorites", 2, new SearchRequestFactory().setFavorite(true));
         
         final Thread[] threads = new Thread[MODES];
         for (int i = 0; i < MODES; i++) {
             threads[i] = new Thread(tasks[i]);
             threads[i].start();
         }
         for (int i = 0; i < MODES; i++) {
             threads[i].join();
         }
         epubDownloader.shutdown();
     }
     
     private static synchronized void setMessage(String message, int taskId) {
         if (message.length() > MODE_WIDTH) {
             message = message.substring(0, MODE_WIDTH);
         }
         final char[] onto = currentMessage.toCharArray();
         final char[] overlay = message.toCharArray();
         final int offset = taskId * MODE_WIDTH + BORDER * taskId;
         for (int i = 0; i < MODE_WIDTH; i++) {
             onto[i + offset] = i < overlay.length ? overlay[i] : ' ';
         }
         currentMessage = new String(onto);
         System.out.print("\r" + currentMessage);
     }
     
     private static String repeat(char c, int amount) {
         final char[] ac = new char[amount];
         Arrays.fill(ac, c);
         return new String(ac);
     }
     
     private static String progressBar(int done, int total) {
         final char[] chars = repeat(' ', MODE_WIDTH).toCharArray();
        for (int i = 0; i < MODE_WIDTH * done / total; i++) {
             chars[i] = '=';
         }
         final char[] label = (done + "/" + total).toCharArray();
         int offset = (MODE_WIDTH - label.length) / 2;
         for (int i = 0; i < label.length; i++) {
             chars[i + offset] = label[i];
         }
         return new String(chars);
     }
     
     private static String escapeFilename(String s) {
         final StringBuilder result = new StringBuilder();
         for (char c : s.toCharArray()) {
             if (Character.isAlphabetic(c) || Character.isDigit(c) || c == ' ' || c == '!') {
                 result.append(c);
             }
         }
         return result.toString();
     }
     
     private static class Task implements Runnable {
         private final String name;
         private final int taskId;
         private final SearchRequest searchRequest;
         
         public Task(String name, int taskId, SearchRequest searchRequest) {
             this.name = name;
             this.taskId = taskId;
             this.searchRequest = searchRequest;
         }
         
         @Override
         public void run() {
             setMessage("Preparing", taskId);
             final File taskOutput = new File(output, name);
             taskOutput.mkdirs();
             setMessage("Searching stories: 0", taskId);
             final Collection<StoryAccess<?>> stories = new ArrayList<StoryAccess<?>>();
             final CompatibilitySearchIterable itrbl = new CompatibilitySearchIterable(searchRequest, ac);
             itrbl.setUseIdSearch(COMPATIBILITY_MODE);
             @SuppressWarnings("unchecked")
             final Iterator<StoryAccess<?>> itrt = itrbl.iterator();
             int i = 0;
             for (int retr = 0; retr < 3; retr++) {
                 try {
                     if (itrt.hasNext()) {
                         final StoryAccess<?> a = itrt.next();
                         setMessage("Searching stories: " + ++i, taskId);
                         stories.add(a);
                         retr = 0;
                     } else {
                         break;
                     }
                 } catch (Error e) {}
             }
             final int total = stories.size();
             setMessage(progressBar(0, total), taskId);
             final AtomicInteger done = new AtomicInteger(0);
             for (final StoryAccess<?> story : stories) {
                 epubDownloader.execute(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             final File outputFile = new File(taskOutput, escapeFilename(story.getMeta().getTitle()) + ".epub");
                             for (int i = 0; i < 3; i++) {
                                 try {
                                     Downloader.downloadStory(story, outputFile, DownloadType.EPUB, ac);
                                    StoryMeta mta = story.getMeta();
                                     if (mta instanceof JSONStoryMeta && ((JSONStoryMeta) mta).getModificationDate().getTime() > 0) {
                                         outputFile.setLastModified(((JSONStoryMeta) mta).getModificationDate().getTime());
                                     }
                                     break;
                                 } catch (IOException e) {}
                             }
                         } finally {
                             setMessage(progressBar(done.incrementAndGet(), total), taskId);
                         }
                     }
                 });
             }
         }
     }
 }

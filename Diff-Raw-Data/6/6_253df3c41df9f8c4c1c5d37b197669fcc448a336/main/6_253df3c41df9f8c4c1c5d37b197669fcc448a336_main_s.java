 //main - http://collabedit.com/uwmpf
 //Crawl - http://collabedit.com/836y2
 //Pair - http://collabedit.com/tscyv
 
 //
 //Hao: I've commented out your tCrawl for now. It's at the bottom.
 //
 package mainPackage;
 
 import java.util.*;
 import java.util.concurrent.locks.ReentrantLock;
 
 import java.io.*;
 
 public class main {
     
     public static Hashtable<String,Integer> ht = new Hashtable<String,Integer>();
     
     public static LinkedList<Pair<String,Integer>> linksToProcess = new LinkedList<Pair<String,Integer>>(); //Queue to process links
     private static final ReentrantLock queueLock = new ReentrantLock();
     private static final ReentrantLock htLock 	= new ReentrantLock();
     private static final ReentrantLock errorLock = new ReentrantLock();
     private static int maxDepth = 0;
     private static int maxPages = 1000;
     private static String dirName = "hnguy067";
 	private static int counter = -1;
 	private static int numThreads = 49;
 
     public static void main(String[] args){
     	if( args.length != 4 ){
     		System.out.println("Usage: <seed-file> <num-pages> <hops-away> <output-dir>");
     		System.exit(1);
     	}
 
     	maxPages = Integer.parseInt(args[1]);
     	maxDepth = Integer.parseInt(args[2]);
     	dirName = args[3];
     	String line;
     	ArrayList<String> urlsFromFile = new ArrayList<String>();
     	
     	try{
     		BufferedReader input = new BufferedReader( new FileReader(args[0]));
     		while( (line = input.readLine()) != null )
     			urlsFromFile.add(line);
     		input.close();
     	}
     	catch(Exception e){
     		System.err.println("Error: " + e.getMessage());
     	}
     	
     	for( String s : urlsFromFile ){
     		linksToProcess.add( new Pair<String, Integer>(s,0));
     	}
             
 
         //Make directory to save all html files into.
         File dir = new File(dirName);
         dir.mkdir();
         
         System.out.println("Initializing threads...");
        Thread threadArray[] = new Thread[100];
         for( int i = 0; i < numThreads; ++i ){
         	threadArray[i] = new Thread(new threadCrawl());
         }
         
         threadCrawl mainThread = new threadCrawl();
         
         long startTime = System.currentTimeMillis();
 
         for( int i = 0; i < numThreads; ++i ){
         	threadArray[i].start();
         }
 
         mainThread.run();
         
         try{
         	for( int i = 0 ; i < numThreads; ++i ){
         		threadArray[i].join();
         	}
         }
         catch( Exception e ){
         	System.out.println("Error in joining: " + e.getMessage());
         }
         
         long endTime = System.currentTimeMillis();
         long totalTime = endTime - startTime;
     }
     
     private static class threadCrawl implements Runnable{
     	LinkedList<Pair<String,Integer>> threadLinksToProcess = new LinkedList<Pair<String,Integer>>();
     	public void run(){
     		if( linksToProcess.isEmpty()){
     			try{
     				Thread.sleep(800);
     			}
     			catch( Exception e ){
     				System.err.println("Error in sleeping: " + e.getMessage());
     			}
     		}
     		while( !linksToProcess.isEmpty() ){
     			queueLock.lock();
     			Pair<String,Integer> p = linksToProcess.remove();
     			queueLock.unlock();
     			
 			if(counter > maxPages ){
 				return;
 			}
            		if(!ht.containsKey(p.getFirst())){
            			htLock.lock();
            			ht.put(p.getFirst(), 1);
            			htLock.unlock();
            			processLink( p.getFirst(), p.getSecond() );
            		}
     		}
     	}
     	
         public void processLink(String url, int currDepth){
             Crawl crawler = new Crawl();
             if( !crawler.connect(url) ){
             	return;
             }
 			errorLock.lock();
 			counter++;
 			if(counter >= maxPages){
 				errorLock.unlock();
 				return;
 			}
             errorLock.unlock();
 	    threadMessage("Connecting to: " + url);
 			
             
             ArrayList<String> linksToQueueUp = crawler.getLinks();
             
             String fileName = url.replaceAll("/",",");
             fileName = fileName.replaceAll(":", "[");
             fileName = fileName.replaceAll("[?]", "]");
             
             
             crawler.saveAsFile(dirName + "/" + fileName + ".html", crawler.getHTML());
             
             if( currDepth < maxDepth ){
                 for( String s : linksToQueueUp ){
                 	if( !ht.containsKey(s) ){
                 		threadLinksToProcess.add(new Pair<String,Integer>(s,currDepth+1));
                 		//threadMessage(" adding to local: " + s + " " + (currDepth+1) );
                 	}
                 }
             }
 		else
 			return;
             queueLock.lock();
             while( !threadLinksToProcess.isEmpty() )
             	linksToProcess.add( threadLinksToProcess.remove() );
             queueLock.unlock();
         }
     }
     
 	static void threadMessage(String s){
 		String threadName = Thread.currentThread().getName();
 		System.out.println(threadName + ": " + s);
 	}
 
 }

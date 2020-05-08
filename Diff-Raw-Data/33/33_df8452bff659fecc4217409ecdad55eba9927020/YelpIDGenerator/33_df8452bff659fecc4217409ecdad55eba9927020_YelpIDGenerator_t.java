 package com.where.hadoop;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * 
  * @author fliuzzi
  * 
  * 
  * YelpIDGenerator
  * 	Usage:  adds ids to yelp zip buckets
  */
 public class YelpIDGenerator {
 	
 	BufferedReader reader;
 	ExecutorService thePool;
 	String path;
 	File[] files;
 	AtomicInteger count;
 	
 	
 	public YelpIDGenerator(File[] files, String dirPath, int numThreads)
 	{
 		count = new AtomicInteger(2000000);
 		this.files = files;
 		this.path = dirPath;
 		thePool = Executors.newFixedThreadPool(numThreads);
 	}
 
 	public void start() throws IOException
 	{
 		
 		for(int i = 0; i < files.length; i++)
 		{
 			final int lcv = i;
 			
 			
 			@SuppressWarnings("unused")
 			Future<?> fut = thePool.submit(
                     new Runnable(){ public void run(){
 			
 				try{
 					
 					readAndWriteFile(new FileReader(files[lcv]),files[lcv].getName());
 					
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 				}
             }});
 		}
 	}
 	
 	
 	public void readAndWriteFile(FileReader stream,String name) throws IOException, JSONException
 	{
 		BufferedReader reader = new BufferedReader(stream);
 		BufferedWriter writer = new BufferedWriter(new FileWriter(path+"/"+name));
 		
 		String line = null;
 		
 		
 		while((line = reader.readLine()) != null) {
 			
 			stripAndWrite(new JSONObject(line), writer);
 			
 		}
 		reader.close();
 		writer.close();
 	}
 	
 	public void stripAndWrite(JSONObject json, BufferedWriter writer) throws JSONException, IOException
 	{
 		if(json != null)
 		{
 				//yelp
 				json.put("pid", String.valueOf(count.incrementAndGet()));
 		}
 		
 		write(json, writer);
 	}
 	
 	public void write(JSONObject json,BufferedWriter writer) throws IOException
 	{
 		writer.write(json.toString());
 		writer.newLine();
 	}
 	
 	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
 		if(args.length != 3) return;
 		
 		File inDir = new File(args[0]);
 		File[] files = inDir.listFiles();
 		
 		
 		
 		
 		YelpIDGenerator lshis = new YelpIDGenerator(files,args[1],Integer.parseInt(args[2]));
 		lshis.start();
		lshis.await();
 		System.out.println("DONE.");
 
 	}
	
	public void await() throws InterruptedException
	{

		thePool.shutdown();
        thePool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
	}
 
 }

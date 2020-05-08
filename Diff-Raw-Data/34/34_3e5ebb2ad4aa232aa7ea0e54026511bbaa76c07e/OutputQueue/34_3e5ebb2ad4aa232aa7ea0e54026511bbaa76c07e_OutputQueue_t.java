 package info.balajeerc.spellspider;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.concurrent.locks.ReentrantLock;
 
import org.apache.commons.io.FilenameUtils;

 public class OutputQueue {
 	private static OutputQueue Instance;
 	private String outputDirectory;
 	private String consolidatedErrorOutputFilePath;
 	private String consolidatedTextOutputFilepath;	
 	private ArrayList<String> consolidatedTextQueue;
 	private ArrayList<String> consolidatedErrorQueue;
 	private final ReentrantLock lock = new ReentrantLock();
 	
 	private OutputQueue(String outputDirectory){
 		consolidatedTextQueue = new ArrayList<String>();
 		consolidatedErrorQueue = new ArrayList<String>();
 		this.outputDirectory = outputDirectory;
		this.consolidatedErrorOutputFilePath = FilenameUtils.concat(outputDirectory,"errors.csv");
		this.consolidatedTextOutputFilepath = FilenameUtils.concat(outputDirectory,"consolidated.txt");
 		//Create new text and error output files
 		try{
 	    	FileWriter outFile = new FileWriter(consolidatedErrorOutputFilePath);
 	    	outFile.close();
 	    	outFile = new FileWriter(consolidatedTextOutputFilepath);
 	    	outFile.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void CreateInstance(String outputDirectory){
 		Instance = new OutputQueue(outputDirectory);
 	}
 	
 	public static OutputQueue GetInstance(){
 		return Instance;
 	}
 	
 	public String getOutputDirectory(){
 		return outputDirectory;
 	}
 	
 	public void addText(String text){
 		lock.lock();
 		consolidatedTextQueue.add(text);
 		if(consolidatedTextQueue.size()>20)
 			flushConsolidatedTextQueue();
 		lock.unlock();
 	}
 	
 	public void addError(String text){
 		lock.lock();
 		consolidatedErrorQueue.add(text);
 		if(consolidatedErrorQueue.size()>20)
 			flushConsolidatedErrorQueue();
 		lock.unlock();
 	}
 	
 	private void flushConsolidatedTextQueue(){
     	//Open the error output file
 		try{
 	    	FileWriter outFile = new FileWriter(consolidatedTextOutputFilepath,true);
 	    	for(int i=0;i<consolidatedTextQueue.size();i++){
 	    		outFile.write(consolidatedTextQueue.get(i));
 	    	}
 	    	consolidatedTextQueue.clear();
 	    	outFile.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	private void flushConsolidatedErrorQueue(){
     	//Open the error output file
 		try{
 	    	FileWriter outFile = new FileWriter(consolidatedErrorOutputFilePath,true);
 	    	for(int i=0;i<consolidatedErrorQueue.size();i++){
 	    		outFile.write(consolidatedErrorQueue.get(i));
	    	}
     	consolidatedErrorQueue.clear();
     	outFile.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 }

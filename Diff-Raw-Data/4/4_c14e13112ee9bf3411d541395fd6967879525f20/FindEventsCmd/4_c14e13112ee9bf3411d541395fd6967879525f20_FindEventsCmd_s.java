 package com.matrobot.gha.archive.cmd;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 
 import com.matrobot.gha.Configuration;
 import com.matrobot.gha.ICommand;
 import com.matrobot.gha.archive.event.FilteredEventReader;
 import com.matrobot.gha.archive.event.EventReader;
 import com.matrobot.gha.archive.event.EventRecord;
 import com.matrobot.gha.archive.repo.RepositoryRecord;
 
 /**
  * Find all events for given repository or user
  * 
  * @author Krzysztof Langner
  */
 public class FindEventsCmd implements ICommand{
 
 	private FilteredEventReader eventReader;
 	HashMap<String, RepositoryRecord> repos = new HashMap<String, RepositoryRecord>();
 	private PrintStream outputStream;
 	
 	
 	@Override
 	public void run(Configuration params) throws IOException {
 
 		outputStream = params.getOutputStream();
 		eventReader = new FilteredEventReader(new EventReader(params.getMonthFolders()));
		eventReader.setRepoName(params.getRepositoryName());
 		if(params.getActor() != null){
 			eventReader.setActor(params.getActor());
 		}
 
 		outputStream.print(EventRecord.getCSVHeaders());
 		saveAsCSV();
 	}
 
 
 	private void saveAsCSV() throws IOException{
 		
 		EventRecord	record;
 		while((record = eventReader.next()) != null){
 			outputStream.print(record.toCSV());
 		}
 	}
 
 	
 	/**
 	 * for local testing
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 
 		Configuration params = new Configuration("configs/events.yaml");
 		
 		FindEventsCmd app = new FindEventsCmd();
 		app.run(params);
 	}
 	
 }

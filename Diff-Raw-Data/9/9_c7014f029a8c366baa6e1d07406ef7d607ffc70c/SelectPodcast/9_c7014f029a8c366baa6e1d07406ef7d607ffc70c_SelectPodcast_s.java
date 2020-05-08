 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options.podcast;
 
 import java.util.HashMap;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnCall;
 
 /**
  * @author sbell
  *
  */
 public class SelectPodcast extends CLIOption {
 
 	public SelectPodcast(DataStorage newData) {
 		super(newData);
 		options = new HashMap<String, CLIOption>();
 		options.put("", new ShowSelectedMenu(newData));
 		options.put("1", new ListEpisodes(newData));
 		options.put("2", new UpdatePodcast(newData));
 		options.put("3", new DeletePodcast(newData));
 		options.put("4", new ChangeDownloadDirectory(newData));
 		options.put("5", new AutoQueueEpisodes(newData));
 		options.put("showSelectedMenu", new ShowSelectedMenu(newData));
 	}
 
 	@Override
 	public ReturnCall execute(String command) {
		if (command.length()<=2)
 			returnObject = options.get("").execute(command);
 		else {
 			returnObject = options.get(command.split(" ")[1]).execute(command);
 		}
 		
 		return returnObject;
 	}
 
 }

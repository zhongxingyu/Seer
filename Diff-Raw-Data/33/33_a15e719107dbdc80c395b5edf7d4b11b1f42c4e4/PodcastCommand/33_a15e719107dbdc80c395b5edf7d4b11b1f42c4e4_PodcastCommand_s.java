 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 import com.mimpidev.podsalinan.cli.options.podcast.*;
 import com.mimpidev.podsalinan.data.Podcast;
 
 /**
  * @author sbell
  *
  */
 public class PodcastCommand extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public PodcastCommand(DataStorage newData) {
 		super(newData);
 		// I will declare CLIOptions first that are referenced more than once in the Map
 		ShowMenu showMenu = new ShowMenu(newData);
 		
 		options.put("<aaaaaaaa>", new SelectPodcast(newData));
 		options.put("showmenu", showMenu);
 		options.put("", showMenu);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.mimpidev.podsalinan.cli.CLIOption#execute(java.lang.String)
 	 */
 	@Override
 	public ReturnObject execute(String command) {
 		debug=true;
 		if (debug) Podsalinan.debugLog.logInfo(this,"Line: 38, command: "+command);
 
 		returnObject.methodCall="podcast";
 
 		if (options.containsKey(command.toLowerCase()))
 			options.get(command).execute(command);
 		else{
 			try {
 				// Check if the value is a number and act accordingly
				String[] commandSplit = command.split(" ", 2);
				if (debug)
					for (String commandItem: commandSplit)
						Podsalinan.debugLog.logInfo(this, commandItem);
 				Integer.parseInt(command);
 				if (command.equals("9")){
 					globalSelection.clear();
 					returnObject.methodCall="";
 					returnObject.methodParameters="";
 				} else {
 					// If the user has entered 8 characters find the right podcast in the list, and the hash happens to
 					// be completely numerical
 					for (int count=0; count<data.getPodcasts().getList().size(); count++){
 						Podcast currentPodcast = data.getPodcasts().getList().get(count);
 						if (currentPodcast.getDatafile().equals(command))
 							returnObject = options.get("<aaaaaaaa>").execute(command);
 					}
 				}
 			} catch (NumberFormatException e) {
 				// 8 characters is the unique identifier we are using for the podcast. So if it is less than 8 numbers
 				// it will be the number in the array
 				if (command.length()<8){
 					if (command.split(" ").length==1){
 						if (debug) Podsalinan.debugLog.logInfo(this,"Command Value: "+command);
 						if (convertCharToNumber(command)<data.getPodcasts().getList().size()){
 							Podcast currentPodcast = data.getPodcasts().getList().get(convertCharToNumber(command));
 							command = currentPodcast.getDatafile();
 							if (debug) Podsalinan.debugLog.logInfo("Found podcast: "+command);
 						}
 					}
 					returnObject = options.get("<aaaaaaaa>").execute(command);
 				} else {
 					if (debug) Podsalinan.debugLog.logInfo(this, "Podcast Value: "+command.split(" ",2)[0]);
 					// If the user has entered 8 characters find the right podcast in the list
                     int count=0;
                     boolean found=false;
                     while ((!found)&&(count<data.getPodcasts().getList().size())){
 						Podcast currentPodcast = data.getPodcasts().getList().get(count);
 						if (currentPodcast.getDatafile().equals(command.split(" ")[0])){
 							returnObject = options.get("<aaaaaaaa>").execute(command);
 							found=true;
 						}
 						count++;
 					}
 				}
 			}
 		}
 		return returnObject;
 	}
 }

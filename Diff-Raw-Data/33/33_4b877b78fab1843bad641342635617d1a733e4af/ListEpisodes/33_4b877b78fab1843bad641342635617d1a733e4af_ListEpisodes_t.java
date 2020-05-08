 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options.podcast;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnCall;
 import com.mimpidev.podsalinan.data.Episode;
 
 /**
  * @author sbell
  *
  */
 public class ListEpisodes extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public ListEpisodes(DataStorage newData) {
 		super(newData);
 	}
 
 	@Override
 	public ReturnCall execute(String command) {
		if (debug) Podsalinan.debugLog.logInfo("ListEpisodes Class called");
		if (debug) Podsalinan.debugLog.logInfo("Command Value: "+command);
 		
 		System.out.println ();
 		int epCount=1;
 /*		synchronized (selectedPodcast.getEpisodes()){
 			for (Episode episode : selectedPodcast.getEpisodes()){
 				System.out.println (getEncodingFromNumber(epCount)+" - " +
 						episode.getTitle()+" : "+episode.getDate());
 				epCount++;
 				if ((epCount%20)==0){
 					System.out.println("-- Press any key to continue, q to quit --");
 					char charInput=input.getSingleCharInput();
 					if (charInput=='q')
 						break;
 				}
 			}
 		}*/
 		
 		return null;
 	}
 
 }

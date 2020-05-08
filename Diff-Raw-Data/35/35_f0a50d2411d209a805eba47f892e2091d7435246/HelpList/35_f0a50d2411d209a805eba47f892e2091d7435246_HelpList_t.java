 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options.help;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 
 /**
  * @author sbell
  *
  */
 public class HelpList extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public HelpList(DataStorage newData) {
 		super(newData);
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		// If user enters "help list"
 	    // Show sub commands for list command
 		System.out.println("list can have the following parameters");
 		System.out.println("");
 		System.out.println("   list podcast           show list of podcasts to the screen");
 		System.out.println("   list episodes          show list of episodes to the screen, if a podcast has been selected");
 		System.out.println("   list downloads         show list of queued downloads to the screen");
 		System.out.println("   list preferences       show list of preferences to the screen");
 		System.out.println("   list select            show list of current selection made to the screen");
 		System.out.println("   list details           show details about currently selected item to the screen");
		returnObject.execute=false;
 		return returnObject;
 	}
 }

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
 public class Help extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public Help(DataStorage newData) {
 		super(newData);
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		// Main help text
 		System.out.println("A url is accepted at almost any time to add it to the download queue.");
 		System.out.println("It will have to start with (http/https/ftp):// for the system to download it.");
 		System.out.println("");
 		System.out.println("root commands");
 		System.out.println("");
 		System.out.println("   help <command>                 will show this screen");
 		System.out.println("   select                         used to select podcast/episode/queued download");
 		System.out.println("   set <preference name> <value>  used for changing system preferences");
 		System.out.println("   list                           used for listing podcasts/episodes/queued downloads/preferences");
 		System.out.println("   hide menu                      used to disable the menu");
 		System.out.println("   show menu                      used to enable the menu");
 		System.out.println("   show details                   used to show details of podcast/episode/queued download");
 		System.out.println("");
 		System.out.println("podcast specific commands");
 		System.out.println("   update                         used to force an update");
 		System.out.println("   download <episode number>      used to download an episode");
 		System.out.println("   set podcast directory <path>   used to change download directory");
 		System.out.println("");
 		System.out.println("download specific commands");
 		System.out.println("   increase <download number>     used to move download up in the list");
 		System.out.println("   decrease <download number>     used to move download down in the list");
 		System.out.println("   remove <download number>       used to cancel download");
 		System.out.println("   restart <download number>      used to restart download");
 		System.out.println("   stop <download number>         used to stop download");
 		System.out.println("   set destination <path>         used to change download directory");
 		System.out.println("");
 		System.out.println("Commands to exit the program");
 		System.out.println("   quit");
 		System.out.println("   exit");
		return null;
 	}
 }

 /*******************************************************************************
  * Copyright (c) 2013 Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  * 
  */
 package podsalinan;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Vector;
 
 /**
  * @author bugman
  *
  */
 public class CLInterface implements Runnable{
 	private boolean finished=false;
 	private Vector<Podcast> podcasts;
 	private URLDownloadList urlDownloads;
 	private ProgSettings settings;
 	private Object waitObject = new Object();
 	private CLInput input;
 	private ProgSettings menuList;
 	private CLMainMenu mainMenu;
 
 	public CLInterface(Vector<Podcast> podcasts, URLDownloadList urlDownloads, ProgSettings settings){
 		this.podcasts=podcasts;
 		this.urlDownloads=urlDownloads;
 		this.settings=settings;
 		menuList = new ProgSettings();
 		input = new CLInput();
 		initializeMenus();
 	}
 
 	private void initializeMenus() {
 		// Main menu requires podcasts and urlDownloads so it can display the number of podcasts and downloads queued.
 		mainMenu = new CLMainMenu(menuList,podcasts,urlDownloads);
 		// When creating the Podcast Menus, we need settings to grab the default directory to do a manual update,
 		// and urlDownloads so we can queue episodes up for downloading manually.
 		mainMenu.addSubmenu(new CLPodcastMenu(menuList,podcasts,urlDownloads));
 		mainMenu.addSubmenu(new CLPreferencesMenu(menuList,settings,waitObject));
 		mainMenu.addSubmenu(new CLDownloadMenu(menuList,urlDownloads));
 	}
 
 
 	/** Brain Storming how to deal with user input, as it works on 2 different levels.
 	 *  
 	 *  Process
 	 *  ===================
 	 *  accept user_input
 	 *  if user_input is a number then
 	 *  	if ((menuList.size = 0) && (user_input==4))
 	 * 			quit
 	 * 		else
 	 * 			mainMenu.process(user_input);
 	 *  else
 	 *  	process user command
 	 */
 	
 	public void userInput(){
 		System.out.print("->");
 		String menuInput=input.getStringInput();
 		if (menuInput.length()>0){
 			try {
 				int inputInt = Integer.parseInt(menuInput);
 				// process number input
 				if ((menuList.size()==0)&&(inputInt==4))
 					finished=true;
 				else if ((settings.findSetting("menuVisible")==null)||
 						 (settings.findSetting("menuVisible").value.equalsIgnoreCase("true")))
 					mainMenu.process(inputInt);
 			} catch (NumberFormatException e){
 				// If the input is not a number This area will sort out that code
 				if ((menuInput.equalsIgnoreCase("quit"))||
 					(menuInput.equalsIgnoreCase("exit"))){
 					finished=true;
 				} else if ((menuInput.toUpperCase().startsWith("HTTP"))||
 						   (menuInput.toUpperCase().startsWith("FTP"))){
 					// User has entered a url to download.
 					urlDownloads.addDownload(menuInput,settings.getSettingValue("defaultDirectory"),"-1",false);
 				} else if (menuInput.toUpperCase().startsWith("HELP")){
 					helpList(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("SELECT")){
 					selectCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("SET")){
 					setCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("LIST")){
 					listCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("SHOW")){
 					showCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("HIDE")){
 					hideCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("DOWNLOAD")){
 					downloadCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("RESTART")){
 					restartCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("STOP")){
 					stopCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("REMOVE")){
 					removeCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("CLEAR")){
 					clearCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("INCREASE")){
 					increaseCommand(menuInput);
 				} else if (menuInput.toUpperCase().startsWith("DECREASE")){
 					decreaseCommand(menuInput);
 				} else if ((settings.findSetting("menuVisible")==null)||
 						   (settings.findSetting("menuVisible").value.equalsIgnoreCase("true")))
 					mainMenu.process(menuInput);
 			}
 		}
 	}
 	
 	/** This method will mostly be used to move a download down the download queue
 	 * @param menuInput
 	 */
 	private void decreaseCommand(String menuInput) {
 		// TODO Drop the priority of the download in the queue
 		menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 
 		if (menuInput.equalsIgnoreCase("decrease")){
 			if ((menuList.size()>0)&&
 				(menuList.getArray().lastElement().name.equalsIgnoreCase("selectedDownload"))){
 					URLDownload download = ((CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu")).getDownload();
 					urlDownloads.decreasePriority(urlDownloads.findDownload(download.getURL()));
 				}
		} else if ((menuInput.toLowerCase().startsWith("download"))||
				   ((menuInput.length()>0)&&(menuInput.length()<3))){
 			menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 			
 			if ((menuInput.length()>0)&&(menuInput.length()<3)){
 				int select = mainMenu.convertCharToNumber(menuInput);
 				if ((select>=0)&&(select<urlDownloads.size())){
 					if (urlDownloads.decreasePriority(select))
 					   System.out.println("Decreased Priority: "+urlDownloads.getDownloads().get(select+1).getURL().toString());
 					else
 						System.out.println("Error: Download already at the bottom of the list.");
 				}
 			}
 		} else 
 			System.out.println("Error: Invalid user input.");
 	}
 
 	/** This method will mostly be used to move a download higher in the download queue
 	 * @param menuInput
 	 */
 	private void increaseCommand(String menuInput) {
 		menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 
 		if (menuInput.equalsIgnoreCase("increase")){
 			if ((menuList.size()>0)&&
 				(menuList.getArray().lastElement().name.equalsIgnoreCase("selectedDownload"))){
 				URLDownload download = ((CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu")).getDownload();
 				urlDownloads.increasePriority(urlDownloads.findDownload(download.getURL()));
 			}
		} else if (((menuInput.toLowerCase().startsWith("download")))||
				 	((menuInput.length()>0)&&(menuInput.length()<3))){
 			menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 			
 			if ((menuInput.length()>0)&&(menuInput.length()<3)){
 				int select = mainMenu.convertCharToNumber(menuInput);
 				if ((select>=0)&&(select<urlDownloads.size())){
 					if (urlDownloads.increasePriority(select))
 						System.out.println("Increased Priority: "+urlDownloads.getDownloads().get(select-1).getURL().toString());
 					else
 						System.out.println("Error: Download already highest priority.");
 				}
 			}
 		} else 
 			System.out.println("Error: Invalid user input.");
 	}
 
 	/** clearCommand - used to clear menuList selection
 	 * @param menuInput
 	 */
 	private void clearCommand(String menuInput) {
 		menuList.clear();
 		System.out.println("Selection Cleared.");
 	}
 
 	/** stopCommand - used to pause the download.
 	 * @param menuInput
 	 */
 	private void stopCommand(String menuInput){
 		URLDownload download=null;
 		// Grab the selected download and call the method below
 		menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 		if (menuInput.equalsIgnoreCase("stop")){
 			download = ((CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu")).getDownload();
 		} else if (menuInput.toLowerCase().startsWith("download")){
 			menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 			
 			if ((menuInput.length()>0)&&(menuInput.length()<3)){
 				int select = mainMenu.convertCharToNumber(menuInput);
 				if ((select>=0)&&(select<urlDownloads.size()))
 					download = urlDownloads.getDownloads().get(select);
 			} else {
 				if (menuInput.equalsIgnoreCase("download")){
 					download = ((CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu")).getDownload();
 				}
 			}
 		} else if ((menuInput.length()>0)&&(menuInput.length()<3)){
 			int select = mainMenu.convertCharToNumber(menuInput);
 			if ((select>=0)&&(select<urlDownloads.size()))
 				download = urlDownloads.getDownloads().get(select);
 		}
 		if (download!=null){
 			// Stop the download
 			urlDownloads.cancelDownload(download);
 			((CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu")).printDetails(download,true);
 		} else {
 			System.out.println("Error: Invalid user input.");
 		}
 	}
 	
 	/** removeCommand - Used to remove a download from the download list. 
 	 * @param menuInput
 	 */
 	private void removeCommand(String menuInput) {
 		menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 		
 		if (menuInput.equalsIgnoreCase("remove")){
 			if ((menuList.size()>0)&&
 			    (menuList.getArray().lastElement().name.equalsIgnoreCase("selectedDownload"))){
 				// remove the download
 				CLDownloadSelectedMenu cldsmenu = (CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu");
 				cldsmenu.printDetails(null,true);
 
 				if (confirmRemoval()){
 					urlDownloads.deleteDownload(cldsmenu.getDownload());
 					System.out.println("Download deleted.");
 				}
 			} else if (menuList.getArray().lastElement().name.equalsIgnoreCase("selectedPodcast")){
 				// remove the podcast from the system
 				CLPodcastSelectedMenu clpsmenu = (CLPodcastSelectedMenu)mainMenu.findSubmenu("podcast_selected");
 				clpsmenu.printDetails(null);
 				
 				if (confirmRemoval()){
 					clpsmenu.getSelectedPodcast().setRemove(true);
 					for (URLDownload download : urlDownloads.getDownloads()){
 						if (download.getPodcastId().equalsIgnoreCase(clpsmenu.getSelectedPodcast().getDatafile())){
 							download.setPodcastId("");
 						}
 					}
 					System.out.println("Podcast deleted.");
 				}
 			} else
 				System.out.println("Error: No Download or Podcast selected");
 		} else if (menuInput.startsWith("download")){
 			menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 			
 			if (menuInput.equalsIgnoreCase("download")){
 				if ((menuList.size()>0)&&
 					(menuList.getArray().lastElement().name.equalsIgnoreCase("selectedDownload"))){
 					CLDownloadSelectedMenu cldsmenu = (CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu");
 					cldsmenu.printDetails(null,true);
 					if (confirmRemoval()){
 						urlDownloads.deleteDownload(cldsmenu.getDownload());
 						System.out.println("Download deleted.");
 					}
 				} else 
 					System.out.println("Error: Invalid user input");
 			} else {
 				if ((menuInput.length()>0)&&(menuInput.length()<3)){
 					int select = mainMenu.convertCharToNumber(menuInput);
 					CLDownloadSelectedMenu cldsmenu = (CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu");
 					cldsmenu.printDetails(urlDownloads.getDownloads().get(select), true);
 					if (confirmRemoval()){
 						urlDownloads.deleteDownload(select);
 						System.out.println("Download deleted.");
 					}
 				} else 
 					System.out.println("Error: Invalid user input");
 			}
 		} else if (menuInput.startsWith("podcast")){
 			menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 			
 			if (menuInput.equalsIgnoreCase("podcast")){
 				if ((menuList.size()>0)&&
 				    (menuList.getArray().lastElement().name.equalsIgnoreCase("selectedPodcast"))){
 					// remove the podcast from the system
 					CLPodcastSelectedMenu clpsmenu = (CLPodcastSelectedMenu)mainMenu.findSubmenu("podcast_selected");
 					clpsmenu.printDetails(null);
 					if (confirmRemoval()){
 						clpsmenu.getSelectedPodcast().setRemove(true);
 						for (URLDownload download : urlDownloads.getDownloads()){
 							if (download.getPodcastId().equalsIgnoreCase(clpsmenu.getSelectedPodcast().getDatafile())){
 								download.setPodcastId("");
 							}
 						}
 						System.out.println("Podcast deleted.");
 					}
 				} else {
 					int podcastCount=0;
 					boolean podcastFound=false;
 					Podcast podcast=null;
 					
 		            while ((podcastCount<podcasts.size())&&(!podcastFound)){
 		            	podcast=podcasts.get(podcastCount);
 						if (((podcast.getName().equalsIgnoreCase(menuInput))||
 							(podcast.getDatafile().equalsIgnoreCase(menuInput)))&&
 							(!podcast.isRemoved())){
 							selectPodcast(podcast);
 							podcastFound=true;
 						}
 		            	podcastCount++;
 		            }
 		            
 		            if (podcastFound){
 						CLPodcastSelectedMenu clpsmenu = (CLPodcastSelectedMenu)mainMenu.findSubmenu("podcast_selected");
 						clpsmenu.printDetails(podcast);
 						if (confirmRemoval()){
 							podcast.setRemove(true);
 							for (URLDownload download : urlDownloads.getDownloads()){
 								if (download.getPodcastId().equalsIgnoreCase(podcast.getDatafile())){
 									download.setPodcastId("");
 								}
 							}
 							System.out.println("Podcast deleted.");
 		            	}
 		            } else {
 						System.out.println("Error: Invalid user input");
 		            }
 				}
 			} else 
 				System.out.println("Error: Invalid user input");
 		} else 
 			System.out.println("Error: Invalid user input");
 	}
 
 		
 	private boolean confirmRemoval(){
 		System.out.println("Are you sure you want to delete this (Y/N)?");
 		String deletePrompt = input.getStringInput();
 		if ((deletePrompt.equalsIgnoreCase("Y"))||
 			(deletePrompt.equalsIgnoreCase("Yes"))){
 			return true;
 		} else {
 			return false;
 		}
 	}
 		
 	/** restartCommand - if it is used on an episode, it will remove the datafile for the episode.
 	 * If it is used on a download, it will remove the data file for the download.
 	 * from the drive.
 	 * @param menuInput
 	 */
 	private void restartCommand(String menuInput) {
 		menuInput = menuInput.replaceFirst(menuInput.split(" ")[0]+" ", "");
 		if (((menuInput.equalsIgnoreCase("delete"))||(menuInput.equalsIgnoreCase("episode")))&&
 			(menuList.getArray().lastElement().name.equalsIgnoreCase("selectedEpisode"))){
 			CLEpisodeMenu episodeMenu = (CLEpisodeMenu)mainMenu.findSubmenu("episode_selected");
 			episodeMenu.deleteEpisodeFromDrive();
 			System.out.println("Deleting file for episode: "+episodeMenu.getEpisode().getTitle());
 		} else if (menuList.getArray().lastElement().name.equalsIgnoreCase("downloads")){
 			if (menuList.getArray().lastElement().name.equalsIgnoreCase("selectedDownload")){
 				URLDownload download = ((CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu")).getDownload();
 				urlDownloads.deleteDownload(urlDownloads.findDownload(download.getURL()));
 				System.out.println("Deleting file for download: "+download.getURL().toString());
 			}
 		}
 	}
 
 	/** downloadCommand is used to 1 - download an episode, 2 - download a url
 	 * @param menuInput - is the user input in the system
 	 */
 	private void downloadCommand(String menuInput) {
 		boolean downloading=false;
 		menuInput= menuInput.replaceFirst(menuInput.split(" ")[0]+" ","");
 		if ((menuInput.toLowerCase().contentEquals("download"))||
 			(menuInput.toLowerCase().equalsIgnoreCase("episode"))){
 			if (menuList.getArray().lastElement().name.equalsIgnoreCase("selectedEpisode")){
 				// If user enters "download" or "download episode" and user has selected episode, download the episode
 				CLEpisodeMenu episodeMenu = (CLEpisodeMenu)mainMenu.findSubmenu("episode_selected");
 				episodeMenu.downloadEpisode();
 				System.out.println("Downloading Episode: "+episodeMenu.getEpisode().getTitle()+" - "+episodeMenu.getEpisode().getDate());
 				downloading=true;
 			}
 		} else if (menuInput.length()>6){
 			try {
 				// newURL is only used to confirm that the user input is a url
 				URL newURL = new URL(menuInput);
 				urlDownloads.addDownload(menuInput, settings.getSettingValue("defaultDirectory"),"-1",false);
 				System.out.println("Downloading URL: "+menuInput);
 				downloading=true;
 			} catch (MalformedURLException e) {
 				// menuInput is not a url
 				System.out.println("Error: Invalid Input");
 			}
 		}
 		
 		if (!downloading){
 			System.out.println("Error: Invalid user Input");
 		}
 	}
 
 	/** turn off the command line menu
 	 * @param menuInput
 	 */
 	private void hideCommand(String menuInput) {
 		if (menuInput.equalsIgnoreCase("hide menu"))
 			if (!settings.addSetting("menuVisible", "false"))
 				settings.updateSetting("menuVisible", "false");
 	}
 
 	/** turn on the command line menu
 	 * @param menuInput
 	 */
 	private void showCommand(String menuInput) {
 		if (menuInput.equalsIgnoreCase("show menu")){
 			if (!settings.addSetting("menuVisible", "true"))
 				settings.updateSetting("menuVisible", "true");
 			mainMenu.process(99);
 		} else if (menuInput.toLowerCase().startsWith("show")){
 			menuInput= menuInput.replaceFirst(menuInput.split(" ")[0]+" ","");
 			if (menuInput.toLowerCase().equalsIgnoreCase("details")){
 				mainMenu.process(98);
 			} else {
 				System.out.println("Error: Invalid User Input");
 			}
 		}
 	}
 
 	@Override
 	public void run() {
 		System.out.println("Welcome to podsalinan.");
 		System.out.println("----------------------");
 		while (!finished){
 			if ((menuList.size()==0)&&
 				((settings.findSetting("menuVisible")==null)||
 				 (settings.findSetting("menuVisible").value.equalsIgnoreCase("true"))))
 				mainMenu.printMainMenu();
 			if (!finished)
 				userInput();
 		}
 		System.out.println("Please Standby for system Shutdown.");
 		synchronized (waitObject){
 			waitObject.notify();
 		}
 	}
 	
     /** listCommand(String) - the command starting with list, handling all
      *  parameters
      * @param menuInput
      */
 	private void listCommand(String menuInput) {
 		menuInput = menuInput.replaceAll("(?i)list ", "");
 		if (menuInput.toLowerCase().startsWith("podcast")){
 			menuList.clear();
 			menuList.addSetting("mainMenu", "podcast");
 			((CLPodcastMenu)(mainMenu.findSubmenu("podcast"))).listPodcasts();
 		} else if (menuInput.toLowerCase().startsWith("episode")){
 			if ((menuList.isValidSetting("mainMenu"))&&
 				((menuList.findSetting("mainMenu").value.equalsIgnoreCase("podcast"))&&
 				 (menuList.findSetting("selectedPodcast")!=null))){
 				((CLPodcastSelectedMenu)(mainMenu.findSubmenu("podcast_selected"))).printEpisodeList();
 			} else {
 				System.out.println("Error: No podcast selected.");
 			}
 		} else if (menuInput.toLowerCase().startsWith("select")){
 			Podcast selectedPodcast=null;
 			System.out.println("Current selection");
 			for (Setting currentItem : menuList.getArray()){
 				if (currentItem.name.equalsIgnoreCase("selectedPodcast")){
 					for (Podcast podcast : podcasts)
 						if (podcast.getDatafile().equalsIgnoreCase(currentItem.value)){
 							selectedPodcast=podcast;
 							System.out.println(currentItem.name+": "+podcast.getName());
 						}
 				} else if (currentItem.name.equalsIgnoreCase("selectedEpisode")){
 					if (selectedPodcast!=null){
 						Episode currentEpisode = selectedPodcast.getEpisodes().get(Integer.parseInt(currentItem.value));
 						System.out.println(currentItem.name+": "+currentEpisode.getTitle()+" : "+currentEpisode.getDate());
 					}
 			    }else
 					System.out.println(currentItem.name+": "+currentItem.value);
 			}
 		} else if (menuInput.toLowerCase().startsWith("details")){
 			mainMenu.process(98);
 		} else if (menuInput.toLowerCase().startsWith("downloads")){
 			((CLDownloadMenu)mainMenu.findSubmenu("downloads")).listDownloads();
 		}
 	}
 
 	/** setCommand(String) - used to set preferences
 	 * @param menuInput
 	 */
 	private void setCommand(String menuInput) {
 		//TODO: Flesh out this command for preferences
 	}
 
 	/** cliSelection(String) - used for processing select commands. for everything
 	 * @param menuInput
 	 */
 	private void selectCommand(String menuInput) {
 		menuInput = menuInput.replaceAll("(?i)select ", "");
 		if (menuInput.toLowerCase().startsWith("podcast")){
 			// remove podcast text at the start (and the space)
 			menuInput= menuInput.replaceFirst(menuInput.split(" ")[0]+" ","");
 			boolean podcastFound=false;
             int podcastCount=0;
             Podcast podcast=null;
             
             // First while loop for exact matches
             while ((podcastCount<podcasts.size())&&(!podcastFound)){
             	podcast=podcasts.get(podcastCount);
 				if (((podcast.getName().equalsIgnoreCase(menuInput))||
 					(podcast.getDatafile().equalsIgnoreCase(menuInput)))&&
 					(!podcast.isRemoved())){
 					selectPodcast(podcast);
 					podcastFound=true;
 				}
             	podcastCount++;
             }
 			if (!podcastFound){
 				// If the user only entered part of the name we need to give suggestions to the user
 				Vector<Podcast> foundPodcasts = new Vector<Podcast>();
 				for (Podcast podcastSearch : podcasts)
 					if ((podcastSearch.getName().toLowerCase().contains(menuInput.toLowerCase()))&&
 						(!podcastSearch.isRemoved()))
 						foundPodcasts.add(podcastSearch);
 				if (foundPodcasts.size()==1){
 					// if only 1 matching podcast found
 					selectPodcast(foundPodcasts.firstElement());
 					podcastFound=true;
 				} else if (foundPodcasts.size()>1){
 		            podcastCount=1;
 					// If too many podcasts with text found
 					System.out.println ("Matches Found: "+foundPodcasts.size());
 					for (Podcast foundPodcast : foundPodcasts){
 						System.out.println(getEncodingFromNumber(podcastCount)+". "+foundPodcast.getName());
 					    podcastCount++;
 					}
 					System.out.print("Please select a podcast: ");
 					// Ask user to select podcast
 					String userInput = input.getStringInput();
 					if ((userInput.length()>0)&&(userInput!=null)){
 						int selection = mainMenu.convertCharToNumber(userInput);
 						if ((selection>=0)&&(selection<foundPodcasts.size())){
 							selectPodcast(foundPodcasts.get(selection));
 							podcastFound=true;
 						} else
 							System.out.println("Error: Invalid user input");
 					} else 
 						System.out.println("Error: Invalid user input");
 				} else if (foundPodcasts.size()==0){
 					System.out.println("Error: No podcast found matching text("+menuInput+")");
 				}
 			}
 			if ((podcastFound)&&
 				(settings.isValidSetting("menuVisible"))&&
 				(settings.findSetting("menuVisible").value.equalsIgnoreCase("true"))){
 				// not going to go through the menuList array, because we had just set it in selectPodcast(Podcast)
 				
 				((CLPodcastSelectedMenu)mainMenu.findSubmenu("podcast_selected")).printMainMenu();
 			}
 		} else if (menuInput.toLowerCase().startsWith("episode")){
 			if ((menuList.isValidSetting("mainMenu"))&&
 				((menuList.findSetting("mainMenu").value.equalsIgnoreCase("podcast"))&&
 				 (menuList.findSetting("selectedPodcast")!=null))){
 				menuInput= menuInput.replaceFirst(menuInput.split(" ")[0]+" ","");
 				// Working here next. handling user input to select an episode. my thoughts are,
 				// user input will either be the letter assigned in list episodes, or the date
 				
 				Podcast selectedPodcast = ((CLPodcastSelectedMenu)mainMenu.findSubmenu("podcast_selected")).getSelectedPodcast();
 				boolean episodeSelected=false;
 				if ((menuInput.length()>0) &&(menuInput!=null)){
 					int select = mainMenu.convertCharToNumber(menuInput);
 					if ((select>=0)&&(select<selectedPodcast.getEpisodes().size())){
 						menuList.addSetting("selectedEpisode", Integer.toString(select));
 						((CLEpisodeMenu)mainMenu.findSubmenu("episode_selected")).setEpisode(selectedPodcast.getEpisodes().get(select),selectedPodcast);
 						episodeSelected = true;
 					}
 					if (!episodeSelected){
 						/* Create a vector of matching dates on episodes. and then let the user select from the list
 						 * similar to how selecting a podcast works.
 						 */
 						Date menuInputDate = convertDate(menuInput);
 						//System.out.println("Date: "+menuInputDate.toString());
 						Vector<Episode> matchingEpisodes = selectedPodcast.getEpisodesByDate(menuInputDate);
 						if (matchingEpisodes.size()==1){
 							// Working below. need to find the episode number to add it to selectedEpisode
 							//menuList.addSetting("selectedEpisode", Integer.toString(select));
 							((CLEpisodeMenu)mainMenu.findSubmenu("episode_selected")).setEpisode(matchingEpisodes.firstElement(),selectedPodcast);
 							episodeSelected = true;
 						} else
 							// Need to deal with more than 1 episode being found, and no episodes found.
 							// 14-1-12 = 8 in hak5
 							if (matchingEpisodes.size()>1){
 					            int episodeCount=1;
 								// If too many podcasts with text found
 								System.out.println ("Matches Found: "+matchingEpisodes.size());
 								for (Episode currentEpisode : matchingEpisodes){
 									System.out.println(getEncodingFromNumber(episodeCount)+". "+currentEpisode.getTitle());
 								    episodeCount++;
 								}
 								System.out.print("Please select an episode: ");
 								// Ask user to select the episode
 								String userInput = input.getStringInput();
 								if ((userInput.length()>0)&&(userInput!=null)){
 									int selection = mainMenu.convertCharToNumber(userInput);
 									if ((selection>=0)&&(selection<matchingEpisodes.size())){
 										menuList.addSetting("selectedEpisode", Integer.toString(selectedPodcast.getEpisodeId(matchingEpisodes.get(selection))));
 										((CLEpisodeMenu)mainMenu.findSubmenu("episode_selected")).setEpisode(matchingEpisodes.get(selection),selectedPodcast);
 										episodeSelected=true;
 									} else
 										System.out.println("Error: Invalid user input");
 								} else 
 									System.out.println("Error: Invalid user input");
 							} else {
 								System.out.println("No match found.");
 							}
 					}
 				}
 				if ((episodeSelected)&&
 						(settings.isValidSetting("menuVisible"))&&
 						(settings.findSetting("menuVisible").value.equalsIgnoreCase("true"))){
 						// not going to go through the menuList array, because we had just set it in selectPodcast(Podcast)
 						CLEpisodeMenu episodeMenu = (CLEpisodeMenu)mainMenu.findSubmenu("episode_selected");
 						episodeMenu.printMainMenu();
 					}
 			}
 		} else if (menuInput.toLowerCase().startsWith("download")){
 			menuInput= menuInput.replaceFirst(menuInput.split(" ")[0]+" ","");
 			// Select download
 			int select = mainMenu.convertCharToNumber(menuInput);
 			if ((select>=0) && (select<urlDownloads.getDownloads().size())){
 				menuList.addSetting("mainMenu", "downloads");
 				menuList.addSetting("selectedDownload", urlDownloads.getDownloads().get(select).getURL().toString());
 				CLDownloadSelectedMenu dsMenu = (CLDownloadSelectedMenu)mainMenu.findSubmenu("downloadSelected_menu"); 
 				dsMenu.setDownload(urlDownloads.getDownloads().get(select));
 				if ((settings.isValidSetting("menuVisible"))&&
 				    (settings.findSetting("menuVisible").value.equalsIgnoreCase("true"))){
 					dsMenu.printMainMenu();
 				}
 			}
 		} else {
 			System.out.println("Error: Invalid User entry.");
 		}
 	}
 	
     /** This method is used to try to find a date from the user entry
      * @param menuInput
      * @return
      */
 	private Date convertDate(String menuInput) {
 		Date date=null;
 		String[] dateFormat = {"dd-MMM-yy",
 				               "dd-MM-yy",
 				               "dd-MMM-yyy",
 				               "dd-MM-yyy"};
 		int dateCounter=0;
 		
 		while ((date==null)&&(dateCounter<dateFormat.length)){
 			try {
 				date = new SimpleDateFormat(dateFormat[dateCounter]).parse(menuInput);
 			} catch (ParseException e) {
 				// date not found
 			}
 			dateCounter++;
 		}
 		
 		return date;
 	}
 
 	private void selectPodcast(Podcast podcast){
 		// Add information to menuList
 		menuList.clear();
 		menuList.addSetting("mainMenu", "podcast");
 		menuList.addSetting("selectedPodcast", podcast.getDatafile());
 			
 		// Set selected podcast
 		((CLPodcastSelectedMenu)(mainMenu.findSubmenu("podcast_selected"))).setSelectedPodcast(podcast);
 
 		if ((settings.isValidSetting("menuVisible"))&&
 			(settings.findSetting("menuVisible").value.equalsIgnoreCase("false")))
   		    System.out.println("Selected Podcast: "+podcast.getName());
 	}
 
 	private void helpList(String menuInput) {
 		System.out.println("");
 		if (menuInput.equalsIgnoreCase("help")){
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
 			System.out.println("");
 			System.out.println("download specific commands");
 			System.out.println("   increase <download number>     used to move download up in the list");
 			System.out.println("   decrease <download number>     used to move download down in the list");
 			System.out.println("   remove <download number>       used to cancel download");
 			System.out.println("   restart <download number>      used to restart download");
 			System.out.println("   stop <download number>         used to stop download");
 			System.out.println("");
 			System.out.println("Commands to exit the program");
 			System.out.println("   quit");
 			System.out.println("   exit");
 		} else if (menuInput.toLowerCase().contains("select")){
 			// If user enters "help select"
 			System.out.println("select is used to traverse around the system, when not using the menu");
 			System.out.println("");
 			System.out.println("   select podcast <podcast name>       this will select the podcast");
 			System.out.println("            If the podcast name is not exact, the system will try to guess the podcast.");
 			System.out.println("   select podcast <podcast number>     this will select the podcast");
 			System.out.println("   select episode <episode number>     this will select the episode");
 			System.out.println("            If the podcast is not selected this will tell you to select a podcast first.");
 			System.out.println("   select download <download number>   this will select the download");
 		} else if (menuInput.toLowerCase().contains("list")){
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
 		} else if (menuInput.toLowerCase().contains("set")){
 			// If the user enters "help set"
 			System.out.println("set is used to change settings");
 			System.out.println("");
 			System.out.println("   set <preference name> <value>  this will update the preference in the system");
 		}
 		System.out.println("");
 		System.out.println("");
 	}
 
 	public boolean isFinished(){
 		return finished;
 	}
 	
 	public void setFinished(boolean isFinished){
 		finished = isFinished;
 	}
 	
 	public String getCharForNumber(int i){
 		return i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : null;
 	}
 	
 	public String getEncodingFromNumber(int number){
 		String charOutput="";
 		if (number<27)
 			charOutput = getCharForNumber(number);
 		else {
 			if (number%26!=0){
 				charOutput+=getCharForNumber(number/26);
 				charOutput+=getCharForNumber(number%26);
 			} else {
 				charOutput=getCharForNumber((number/26)-1)+"Z";
 			}
 		}
 		
 		return charOutput;
 	}
 
 	/**
 	 * @return the waitObject
 	 */
 	public Object getWaitObject() {
 		return waitObject;
 	}
 
 	/**
 	 * @param waitObject the waitObject to set
 	 */
 	public void setWaitObject(Object waitObject) {
 		this.waitObject = waitObject;
 	}
 }

 /**
  * 
  */
 package podsalinan;
 
 /**
  * @author bugman
  *
  */
 public class CLDownloadMenu extends CLMenu{
 	private URLDownloadList urlDownloads;
 
 	public CLDownloadMenu(ProgSettings newMenuList, URLDownloadList downloadList) {
 		super(newMenuList,"downloads");
 		String[] mainMenuList = {"(A-ZZ) Enter Download letter to select Download.",
 								 "To add a new download to the queue just enter the the url to be downloaded.",
 								 "",
 								 "9. Return to Main Menu"
 
 		};
 		setMainMenuList(mainMenuList);
 		setUrlDownloads(downloadList);
 		addSubmenu(new CLDownloadSelectedMenu(menuList));
 	}
 
 	public void printMainMenu() {
 		int downloadCount=1;
 		for (URLDownload download : urlDownloads.getDownloads()){
 			if (!download.isRemoved()){
 				System.out.println(getEncodingFromNumber(downloadCount)+". "+download.getURL().toString());
				downloadCount++;
 			}
 		}
 		
 		super.printMainMenu();
 	}
 
 	public void process(int userInputInt) {
 		switch (userInputInt){
 			case 9:
 				menuList.clear();
 				break;
 		}
 		super.process(userInputInt);
 	}
 	
 	public void process(String userInput){
 		if (menuList.size()==1){
 			if (userInput.length()<3){
 				int downloadNumber = convertCharToNumber(userInput);
 				
 				if ((downloadNumber>urlDownloads.size())&&
 					(downloadNumber<0))
 					System.out.println("Error: Invalid Download Selected");
 				else {
 					synchronized (urlDownloads.getDownloads()){
 						menuList.addSetting("selectedDownload", urlDownloads.getDownloads().get(downloadNumber).getURL().toString());
 						((CLDownloadSelectedMenu)findSubmenu("downloadSelected_menu")).setDownload(urlDownloads.getDownloads().get(downloadNumber));
 					}
 				}
 				userInput=null;
 			}
 		}
 		if (menuList.size()>1){
 			if (menuList.findSetting("downloadSelected_menu")!=null)
 				((CLDownloadSelectedMenu)findSubmenu("downloadSelected_menu")).process(userInput);
 		} else
 			super.process(userInput);
 	}
 
 	public URLDownloadList getUrlDownloads() {
 		return urlDownloads;
 	}
 
 	public void setUrlDownloads(URLDownloadList urlDownloads) {
 		this.urlDownloads = urlDownloads;
 	}
 }

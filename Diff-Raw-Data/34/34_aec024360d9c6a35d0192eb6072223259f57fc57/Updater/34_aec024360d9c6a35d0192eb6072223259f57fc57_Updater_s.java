 package main;
 
 import java.util.ArrayList;
 
 import cleaner.*;
 import menusystem2.*;
 
 //very static class to hold the current progress of the scan
 public class Updater
 {
 	public static ArrayList<MatchList> allLists = new ArrayList<MatchList>();
 	
 	public static ArrayList<Match> cacheMatches = new ArrayList<Match>();
 	public static ArrayList<Match> historyMatches = new ArrayList<Match>();
 	
 	public static int totalFiles = 0;
 	public static int scannedFiles = 0;
 	
 	public static void startUp()
 	{
 		Lists.loadLists();
 	}
 	
 	public static void setTotalFiles(int total)
 	{
 		totalFiles = total;
 		MakeMenu.loadingbar.setMax(total);
 	}
 	
 	public static void incrementScannedFiles()
 	{
 		totalFiles++;
 		MakeMenu.loadingbar.setCurrent(totalFiles);
 	}
 	
 	public static void clean()
 	{
 		System.out.println("Cleaning...");
 //		Lists.deleteMatchedFiles = true;
 		runScan();
 	}
 	
 	public static void scan()
 	{
 		System.out.println("Scanning...");
 		runScan();
 	}
 	
 	private static void runScan()
 	{
 //		Lists.loadLists();
 		historyMatches = Lists.getMatches(Lists.historyFolders, true);
 		cacheMatches = Lists.getMatches(Lists.cacheFolders, true);
 
 		Updater.finishScan();
 		
 //		for(Match m : historyMatches)
 //		{
 //			System.out.println("~"+m.matchedTerm);
 //			m.toMenu().printMenus();
 //		}
 	}
 	
 	public static void finishScan()
 	{
 		ArrayList<Menu> matchMenus = new ArrayList<Menu>();
 		
 		Button historyMenu = new Button(10, 1, true, "History Matches");
 		historyMenu.changeColorWhenClicked = false;
 		matchMenus.add(historyMenu);
 		
 		for(int i = 0; i < historyMatches.size(); i++)
 			matchMenus.add(historyMatches.get(i).toMenu());
 		
 		Button cacheMenu = new Button(10, 1, true, "Cache Matches");
 		cacheMenu.changeColorWhenClicked = false;
 		matchMenus.add(cacheMenu);
 		
 		for(int i = 0; i < cacheMatches.size(); i++)
 			matchMenus.add(cacheMatches.get(i).toMenu());
 		
 		MakeMenu.matchScroller.addMenus(matchMenus);
 	}
 	
 	public static void removeLastNMinutes(int n)
 	{
 		
 	}
 
 	public static void addLists()
 	{
 		ArrayList<Menu> allListMenus = new ArrayList<Menu>();
 		for(int i = 0; i < allLists.size(); i++)
 		{
 			ArrayList<Menu> matchListMenus = allLists.get(i).getMenus();
 			for(int j = 0; j < matchListMenus.size(); j++)
 			{
 				allListMenus.add(matchListMenus.get(j));
 			}
 		}
 		
 		MakeMenu.listsScroller.addMenus(allListMenus);
 	}
 }

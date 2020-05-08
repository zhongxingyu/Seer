 package com.ladinc.showrss;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.util.Log;
 
 import com.ladinc.showrss.domain.Show;
 import com.ladinc.showrss.utilities.Utilities;
 
 public class YourShows {
 
 	public static List<Show> shows;
 	public static List<Show> availableShows;
 	private static String url = Utilities.URL + "?cs=shows";
 	private static String addShowUrl = Utilities.URL + "?cs=ajax&m=shows&func=add&show=";
 	private static String deleteShowUrl = Utilities.URL + "?cs=ajax&m=shows&func=delete&show=";
 	private static String optsShowUrl = Utilities.URL + "?cs=ajax&m=shows&func=opts&show=";
 	private static String showSettingsUrl = Utilities.URL + "?cs=ajax&m=opts&show=";
 
 	public static List<Show> getShows() throws Exception {
 		String htmlCode = "";
 		try {
 			htmlCode = HtmlCode.getHtmlCode(url, true);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		extractYourShows(htmlCode);
 		extractAvailableShows(htmlCode);
 
 		return shows;
 	}
 
 	private static void extractAvailableShows(String htmlCode) {
 		Pattern p = Pattern.compile("<option value=\"([0-9]*)\">([^<]*)</option>");
 		Matcher m = p.matcher(htmlCode);
 
 		availableShows = new ArrayList<Show>();
 
 		while (m.find()) {
 			Show show = new Show(m.group(2), // Show Name,
 					Integer.parseInt(m.group(1)) // showID
 			);
 
 			availableShows.add(show);
 		}
 
 	}
 
 	private static void extractYourShows(String htmlCode) {
 		Pattern p = Pattern.compile("onclick=\"rundelete\\(([0-9]+)\\);");
 		Matcher m = p.matcher(htmlCode);
 
 		shows = new ArrayList<Show>();
 
 		while (m.find()) {
 
 			Show show = new Show(AllShows.allshows.get(m.group(1)), Integer.parseInt(m.group(1)) // Show
 																									// Name
 			);
 
 			shows.add(show);
 
 		}
 
 	}
 	
 	public static boolean hasShows() {
 		
 		if (shows != null)
 		{
 			if(!shows.isEmpty())
 				return true;
 		}
 
 		return false;
 	}
 	
 	public static String[] showsAsArray() {
 		
 		if (YourShows.hasShows())
 		{
 			int i;
 			String[] showName = new String[shows.size()];
 			for (i = 0; i < shows.size(); i++) {
 				showName[i] = shows.get(i).getShowName();
 			}
 			return showName;
 		}
 		else
 		{
 			return new String[] {"You are not subscribed to any shows"};
 		}
 	}
 
 	public static String[] availableShowsAsArray() {
 		int i;
 		String[] showName = new String[availableShows.size()];
 		for (i = 0; i < availableShows.size(); i++) {
 			showName[i] = availableShows.get(i).getShowName();
 		}
 
 		return showName;
 	}
 
 	public static void addShow(String showName) throws Exception {
 		String showId = AllShows.showNameAsKey.get(showName);
 		try {
 			HtmlCode.getHtmlCode(addShowUrl + showId, false);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// Deletes the passes show from your list.
 	public static void deleteShow(String showName) throws Exception {
 		String showId = AllShows.showNameAsKey.get(showName);
 		try {
 			HtmlCode.getHtmlCode(deleteShowUrl + showId, false);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// Deletes the passes show from your list.
 	public static boolean setShowSettings(String showName, boolean sd, boolean hd, boolean repack) throws Exception {
 		String showId = AllShows.showNameAsKey.get(showName);
 		int hasHd = 0;
 		int hasProper = 0;
 
 		// if neither hd or sd or true
 		if (!(hd || sd))
 			return false;
 
 		// Set hasHD
 		// 0 = Only SD
 		// 1 = Only Hd
 		// 2 = both
 
 		if (hd)
 			hasHd = 1;
 
 		// if hd was true, doubling hasHd leaves hasHd at 2, while if hd was
 		// false it will be 0. Which is what we want here.
 		if (sd)
 			hasHd = hasHd * 2;
 
 		// Set hasProper
 		// 0 = Do not include repacks and proper
 		// 1 = Include proper and repacks
 
 		if (repack)
 			hasProper = 1;
 
 		String url = optsShowUrl + showId + "&hashd=" + hasHd + "&hasproper=" + hasProper;
 
 		try {
 			HtmlCode.getHtmlCode(url, false);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	public static Show getShowSettings(String showName) throws Exception {
 		Show show = new Show(showName);
 
 		String settingsUrl = showSettingsUrl + show.getShowId();
 
 		String htmlCode = "";
 		try {
 			htmlCode = HtmlCode.getHtmlCode(settingsUrl, false);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return show;
 		}
 
 		// Get Has HD Value
 <<<<<<< HEAD
<<<<<<< HEAD
 		Pattern p = Pattern.compile("hashd.*value=\"([0-9]{1})\" selected.*hasproper");
 =======
 		Pattern p = Pattern.compile("Torrent quality.*value=\\\"([0-9]{1})\\\" selected.*Torrent types");
 >>>>>>> 073dd85aabd26d4759fbe79465fd9e2b75a33147
=======
		Pattern p = Pattern.compile("Torrent quality.*value=\\\"([0-9]{1})\\\" selected.*Torrent types");
>>>>>>> 073dd85aabd26d4759fbe79465fd9e2b75a33147
 		Matcher m = p.matcher(htmlCode);
 
 		if (m.find())
 		{
 			Log.d("Utilities", "Found HD Value: " +  m.group(1));
 			show.setHasHd(Integer.parseInt(m.group(1)));
 		}
 		
 		// Get has proper value
 <<<<<<< HEAD
<<<<<<< HEAD
 		p = Pattern.compile("hasproper.*value=\"([0-9]{1})\" selected");
 		m = p.matcher(htmlCode);
 
 		if (m.find())
 		{
 			Log.d("Utilities", "Found Repack Value: " +  m.group(1));
 			show.setHasProper(Integer.parseInt(m.group(1)));
 		}
 =======
 		p = Pattern.compile("Torrent types.*value=\\\"([0-9]{1})\\\" selected");
 		m = p.matcher(htmlCode);
 
 		if (m.find())
=======
		p = Pattern.compile("Torrent types.*value=\\\"([0-9]{1})\\\" selected");
		m = p.matcher(htmlCode);

		if (m.find())
>>>>>>> 073dd85aabd26d4759fbe79465fd9e2b75a33147
 			show.setHasProper(Integer.parseInt(m.group(1)));
 
 >>>>>>> 073dd85aabd26d4759fbe79465fd9e2b75a33147
 		return show;
 
 	}
 
 }

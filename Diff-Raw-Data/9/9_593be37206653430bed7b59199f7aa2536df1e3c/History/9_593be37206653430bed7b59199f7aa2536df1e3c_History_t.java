 package com.ppp.wordplayadvlib.model;
 
 import java.io.BufferedReader;
 import java.io.StringReader;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 
 import com.ppp.wordplayadvlib.Constants;
 import com.ppp.wordplayadvlib.WordPlayApp;
 import com.ppp.wordplayadvlib.dialogs.AppErrDialog;
 import com.ppp.wordplayadvlib.utils.Debug;
 
 public class History {
 
 	private static History instance;
 
 	// Search History
 	private LinkedList<HistoryObject> history = new LinkedList<HistoryObject>();
 
 	private History() {}
 
 	public static History getInstance()
 	{
 		if (instance == null)
 			instance = new History();
 		return instance;
 	}
 
 	public LinkedList<HistoryObject> getHistory() { return history; }
 
 	public void clearHistory(Activity activity)
 	{
 		Debug.d("clearHistory: history cleared");
 		history.clear();
 		saveHistory(activity);
 	}
 
 	public void saveHistory(Activity activity)
 	{
 
 		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = prefs.edit();
 		StringBuilder historyBuf = new StringBuilder();
 
 		ListIterator<HistoryObject> iterator = getHistory().listIterator(history.size());
 		while (iterator.hasPrevious())  {
 			HistoryObject elem = iterator.previous();
 			historyBuf.append(elem.getSearchString()).append(":");
 			historyBuf.append(elem.getBoardString()).append(":");
 			historyBuf.append(elem.getSearchType()).append(":");
 			historyBuf.append(elem.getDictionary().toString()).append(":");
 			historyBuf.append(elem.getScoreState().ordinal()).append(":");
 			historyBuf.append(elem.getSortState().ordinal());
 			historyBuf.append("\n");
 		}
 
 		editor.putString("savedHistory", historyBuf.toString());
 		Debug.v("SAVE HISTORY = '" + historyBuf.toString() + "'");
 
 		editor.commit();
 
 	}
 
 	public void loadHistory(Activity activity)
 	{
 
 		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
 		String historyStr = prefs.getString("savedHistory", "");
 
 		Debug.v("LOAD HISTORY = '" + historyStr + "'");
 
		history.clear();
 
 		BufferedReader historyBuf = new BufferedReader(new StringReader(historyStr), Constants.BufSize);
 		try {
 			String input;
 			while ((input = historyBuf.readLine()) != null)  {
 				HistoryObject history = new HistoryObject(input);
 				addHistory(history);
 			}
 		}
 		catch (Exception e) {
 			new AppErrDialog(WordPlayApp.getInstance()).showMessage("Problem loading search history");
 			return;
 		}
 
 	}
 
 	public void addHistory(HistoryObject newHistory)
 	{
 
 		// Don't add one we already have
 		for (HistoryObject elem : history)
 			if (elem.equalTo(newHistory))  {
 				Debug.v("addHistory: duplicate '" + newHistory + "'");
 				return;
 			}
 
 		Debug.v("addHistory: " + newHistory);
 
 		// If the history stack size is at its maximum, get rid of
 		// the oldest item first before adding this new item
 		if (history.size() >= Constants.MaxHistory)  {
 			Debug.v("addHistory: exceeds " + Constants.MaxHistory + " elements...removing last");
 			history.removeLast();
 		}
 		history.addFirst(newHistory);
 
 		Debug.v("addHistory: size " + history.size());
 
 	}
 
 	public void addHistory(String searchString,
 							String boardString,
 							SearchType seaerchType,
 							DictionaryType dictionary,
 							WordScoreState wordScore,
 							WordSortState wordSort)
 	{
 		HistoryObject newHistory =
 			new HistoryObject(searchString, boardString, seaerchType, dictionary, wordScore, wordSort);
 		addHistory(newHistory);
 	}
 
 	public void addHistory(Bundle bundle)
 	{
 		if (bundle != null)  {
 			HistoryObject newHistory = new HistoryObject(bundle);
 			addHistory(newHistory);
 		}
 	}
 
 	public HistoryObject getHistory(int position)
 	{
 		return history.get(position);
 	}
 
 }

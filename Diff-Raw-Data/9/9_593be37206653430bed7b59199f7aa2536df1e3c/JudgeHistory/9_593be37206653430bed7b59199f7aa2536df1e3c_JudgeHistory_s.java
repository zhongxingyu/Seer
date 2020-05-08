 package com.ppp.wordplayadvlib.model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import com.ppp.wordplayadvlib.Constants;
 import com.ppp.wordplayadvlib.WordPlayApp;
 import com.ppp.wordplayadvlib.dialogs.AppErrDialog;
 import com.ppp.wordplayadvlib.utils.Debug;
 
 public class JudgeHistory {
 
 	private static JudgeHistory instance;
 
 	// Word Judge History
 	private static LinkedList<JudgeHistoryObject> judge_history = new LinkedList<JudgeHistoryObject>();
 
 	private JudgeHistory() {}
 
 	public static JudgeHistory getInstance()
 	{
 		if (instance == null)
 			instance = new JudgeHistory();
 		return instance;
 	}
 
 	public LinkedList<JudgeHistoryObject> getJudgeHistory() { return judge_history; }
 
 	public void clearJudgeHistory(Activity activity)
 	{
 		Debug.d("clearJudgeHistory: history cleared");
 		judge_history.clear();
 		saveJudgeHistory(activity);
 	}
 
 	public void saveJudgeHistory(Activity activity)
 	{
 
 		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = prefs.edit();
 
 		StringBuilder judgeBuf = new StringBuilder();
 		ListIterator<JudgeHistoryObject> iterator = getJudgeHistory().listIterator(judge_history.size());
 		while (iterator.hasPrevious())  {
 			JudgeHistoryObject elem = iterator.previous();
 			judgeBuf.append(elem.getWord()).append(":");
 			judgeBuf.append(elem.getState());
 			judgeBuf.append("\n");
 		}
 
 		editor.putString("wordjudgeHistory", judgeBuf.toString());
 		Debug.v("SAVE JUDGE_HISTORY = '" + judgeBuf.toString() + "'");
 
 		editor.commit();
 
 	}
 
 	public void loadJudgeHistory(Activity activity)
 	{
 
 		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
 		String judgeHistoryStr = prefs.getString("wordjudgeHistory", "");
 
 		Debug.v("LOAD JUDGE_HISTORY = '" + judgeHistoryStr + "'");
 
		clearJudgeHistory(activity);
 
 		BufferedReader judgeBuf = new BufferedReader(new StringReader(judgeHistoryStr), Constants.BufSize);
 		try {
 			String input;
 			while ((input = judgeBuf.readLine()) != null)  {
 				JudgeHistoryObject history = new JudgeHistoryObject(input);
 				addJudgeHistory(history);
 			}
 		}
 		catch (IOException e) {
 			new AppErrDialog(WordPlayApp.getInstance()).showMessage("Problem loading word judge history");
 			return;
 		}
 
 	}
 
 	public void addJudgeHistory(JudgeHistoryObject newHistory)
 	{
 		if (getJudgeHistory().size() >= Constants.MaxJudgeHistory)
 			getJudgeHistory().removeLast();
 		getJudgeHistory().addFirst(newHistory);
 	}
 
 	public void addJudgeHistory(String word, boolean state)
 	{	
 		JudgeHistoryObject elem = new JudgeHistoryObject(word, state);
 		addJudgeHistory(elem);
 	}
 
 }

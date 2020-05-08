 package sl.tasks;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import sl.items.Furi;
 import sl.items.Word;
 import sl.libs.json.YahooFurigana;
 import sl.libs.xml.XmlHandler;
 import sl.libs.xml.domsample.DomSample;
 import sl.utils.CONS;
 import sl.utils.DBUtils;
 import sl.utils.Methods;
 import sl.utils.Methods_sl;
 import android.R;
 import android.app.Activity;
 import android.app.Dialog;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Task_GetYomi extends AsyncTask<String, Integer, Integer> {
 
 	static Activity actv;
 	
 	Dialog dlg;
 	
 	public Task_GetYomi(Activity actv) {
 		// TODO Auto-generated constructor stub
 		this.actv = actv;
 	}
 
 	public Task_GetYomi(Activity actv, Dialog dlg) {
 		// TODO Auto-generated constructor stub
 		this.actv = actv;
 		
 		this.dlg = dlg;
 		
 	}
 
 	@Override
 	protected Integer doInBackground(String... arg0) {
 		
 		// TODO Auto-generated method stub
 //		int res = Task_GetYomi.doInBackground__1();
 //		Task_GetYomi.doInBackground__1();
 //		Task_GetYomi.doInBackground__2();
 		
 //		int res = Methods_sl.getYomi(actv, dlg);
 		
 		/*********************************
 		 * XmlHandler
 		 *********************************/
 //		Task_GetYomi.doInBackground__2__1__XmlHandler();
 		
 //		Task_GetYomi.doInBackground_B18_v_2_0_d();
 		
 //		Task_GetYomi.doInBackground_B18_v_2_0_d_t_2();
 //		Task_GetYomi.doInBackground_B18_v_2_0_d_t_3();
 //		Task_GetYomi.doInBackground_B18_v_2_0_d_t_4();
 //		Task_GetYomi.doInBackground_B18_v_2_0_d_t_5();
 //		Task_GetYomi.doInBackground_B18_v_2_0_d_t_6();
 //		Task_GetYomi.doInBackground_B18_v_2_0_d_t_7();
 //		Task_GetYomi.doInBackground_B18_v_3_0();
 //		Integer res = Task_GetYomi.doInBackground_B18_v_4_0();
 //		return Task_GetYomi.doInBackground_B18_v_4_0();
 //		return Task_GetYomi.doInBackground_B18_v_4_1();
 //		return Task_GetYomi.doInBackground_B18_v_5_0();
 //		return Task_GetYomi.doInBackground_B18_v_5_0_e_1_t_1();
 //		return Task_GetYomi.doInBackground_B18_v_5_1();
 //		return Task_GetYomi.doInBackground_B18_v_5_1a();
 //		return Task_GetYomi.doInBackground_B18_v_5_2();
 //		return Task_GetYomi.doInBackground_B18_v_5_3();
 //		return Task_GetYomi.doInBackground_B18_v_5_4();
 		return Task_GetYomi.doInBackground_B18_v_6_0();
 		
 //		v-5.0-e1-t1
 //		return CONS.GETYOMI_FAILED;
 //		return res;
 		
 //		return null;
 	}
 
 	private static Integer doInBackground_B18_v_6_0() {
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Time=" + Methods.get_TimeLabel(Methods.getMillSeconds_now()));
 		
 		/***************************************
 		 * Word list
 		 ***************************************/
 		List<Word> wordList = doInBackground_B18_v_6_0__1_getWordList();
 
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "wordList.size()=" + wordList.size());
 
 		/***************************************
 		 * If no more entries to process, quit the task
 		 ***************************************/
 		if (wordList.size() < 1) {
 			
 			return CONS.GETYOMI_NO_ENTRY;
 			
 		}//if (wordList.size() < 1)
 		
 		/*********************************
 		 * Get combo from API
 		 *********************************/
 		wordList = doInBackground_B18_v_6_0__2_getCombo(wordList);
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "wordList.size()=" + wordList.size());
 		
 		/***************************************
 		 * Convert combo into yomi (i.e. all-hiragana)
 		 ***************************************/
 		doInBackground_B18_v_6_0__3_convertCombo2Yomi(wordList);
 		
 		/***************************************
 		 * Update table
 		 ***************************************/
 		doInBackground_B18_v_6_0__4_updateTable(wordList);
 		
 		/***************************************
 		 * Debug: Combo values
 		 ***************************************/
 		for (int i = 0; i < wordList.size(); i++) {
 			
 			Word word = wordList.get(i);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]",
 					"name=" + word.getName()
 					+ "/" + "combo=" + word.getCombo()
 					+ "/" + "yomi=" + word.getYomi()
 					);
 
 			if (word.getCombo() == null) {
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]", "combo => null");
 				
 			} else {//if (word.getCombo() == null)
 
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]", "combo => Not null");
 				
 			}//if (word.getCombo() == null)
 			
 		}//for (int i = 0; i < wordList.size(); i++)
 		
 		/***************************************
 		 * Return
 		 ***************************************/
 		return CONS.GETYOMI_SUCCESSFUL;
 		
 	}//private static void doInBackground_B18_v_6_0()
 
 	/***************************************
 	 * Created at: 20130223_131422<br>
 	 * 1. The entry in the db gets extracted into the list if:<br>
 	 * 		1. The entry has "name" value<br>
 	 * 		2. The "yomi" value being either null or ""(blank)<br>
 	 * 
 	 ***************************************/
 	private static List<Word> doInBackground_B18_v_6_0__1_getWordList() {
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*----------------------------
 		 * 0. Table exists?
 			----------------------------*/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "tableName=" + CONS.tableName);
 		
 		boolean res = dbu.tableExists(rdb, CONS.tableName);
 		
 		if (res == false) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "getAllData() => Table doesn't exist: " + CONS.tableName);
 			
 			rdb.close();
 			
 			return null;
 			
 		}//if (res == false)
 		
 		/*----------------------------
 		 * 2. Get data
 		 * 		2.1. Get cursor
 		 * 		2.2. Add to list
 			----------------------------*/
 		//
 		String sql = "SELECT * FROM " + CONS.tableName;
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount()=" + c.getCount());
 			
 		} catch (Exception e) {
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return null;
 		}
 	
 		/*********************************
 		 * Get names
 		 *********************************/
 //		List<String> itemNames = new ArrayList<String>();
 //		
 //		List<Long> itemIds = new ArrayList<Long>();
 		
 		List<Word> wordList = new ArrayList<Word>();
 		
 		c.moveToFirst();
 		
 		int numOfSamples = 10;
 		
 		/***************************************
 		 * Counter: Count 1 each time when a new entry 
 		 * 				is made into wordList
 		 ***************************************/
 		int counter = 0;
 //		int numOfSamples = c.getCount();
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "numOfSamples=" + numOfSamples);
 		
 //		for (int i = 0; i < 10; i++) {
 //		for (int i = 0; i < numOfSamples; i++) {
 		for (int i = 0; i < c.getCount(); i++) {
 			
 			String name = c.getString(CONS.colAddUp + Methods.getArrayIndex(CONS.columns, "name"));
 			
 			String yomi = c.getString(CONS.colAddUp + Methods.getArrayIndex(CONS.columns, "yomi"));
 		
 			long itemId = c.getLong(0);
 
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "name=" + name + "/" + "yomi=" + yomi);
 			
 //			if (name != null) {
 //			if (name != null && (yomi == null || yomi == "null")) {
//			if (name != null && (yomi == null || yomi.equals("null"))) {
			if (name != null 
					&& (yomi == null || yomi.equals("null") || yomi.equals(""))) {
 				
 //				// Log
 //				Log.d("Task_GetYomi.java"
 //						+ "["
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getMethodName() + "]",
 //						"name != null && (yomi == null || yomi.equals(\"null\"))");
 				
 				wordList.add(new Word(itemId, name, yomi));
 				
 				counter += 1;
 
 			} else {//if (name != null)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"!(name != null && (yomi == null || yomi.equals(\"null\")))");
 				
 //				wordList.add(new Word(itemId, name, yomi));
 //				
 //				counter += 1;
 
 			}//if (name != null)
 			
 			/***************************************
 			 * Check
 			 ***************************************/
 			if (counter > numOfSamples) {
 				
 				break;
 				
 			}//if (counter == numOfSamples)
 			
 			/*********************************
 			 * Next row in the cursor
 			 *********************************/
 			c.moveToNext();
 			
 		}//for (int i = 0; i < 10; i++)
 		
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "wordList.size()=" + wordList.size());
 		
 		rdb.close();
 		
 		/***************************************
 		 * Return
 		 ***************************************/
 		return wordList;
 		
 	}//private static List<Word> doInBackground_B18_v_6_0__1_getFuriganaList()
 
 	/***************************************
 	 * Conduct YahooFurigana.getFurigana(keyWord, true)<br>
 	 * 1. If the Word instance has "yomi" value (i.e. not null),
 	 * 		then skip the entry
 	 ***************************************/
 	private static List<Word> doInBackground_B18_v_6_0__2_getCombo(
 			List<Word> wordList) {
 		
 		YahooFurigana yf = YahooFurigana.getInstance();
 		
 		int isNull = 0;
 		int isNotNull = 0;
 		
 		int count = 0;
 		int numOfSamples = 10;
 		
 //		for (int i = 0; i < itemNames.size(); i++) {
 		for (int i = 0; i < wordList.size(); i++) {
 //		for (int i = 0; (i < wordList.size()) && count < numOfSamples; i++) {
 			
 			Word word = wordList.get(i);
 			
 			String keyWord = word.getName();
 				
 			String furi = yf.getFurigana(keyWord, true);
 	
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "furi=" + furi);
 	
 			word.setCombo(furi);
 				
 		}//for (int i = 0; i < itemNames.size(); i++)
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Get furigana => Done");
 		
 		/***************************************
 		 * Return
 		 ***************************************/
 		return wordList;
 
 	}//private static List<Word> doInBackground_B18_v_6_0__2_getFurigana
 
 	/***************************************
 	 * Created at: 20130223_141154
 	 * 1. Extract "combo" value
 	 * 2. Convert the "combo" into "yomi"
 	 * 2-2. If the coversion fails, i.e. the yomi value gets
 	 * 			null, then insert null into the "yomi" field of
 	 * 			the word instance
 	 * 3. If the "combo" value is null, set null instead
 	 ***************************************/
 	private static List<Word> doInBackground_B18_v_6_0__3_convertCombo2Yomi(
 			List<Word> wordList) {
 		
 		for (int i = 0; i < wordList.size(); i++) {
 
 			Word word = wordList.get(i);
 			
 			String combo = word.getCombo();
 			
 			if (combo != null) {
 				
 //				String gana = Methods.convert_Kana2Gana(word.getFuri());
 				String yomi = Methods.convert_Kana2Gana(combo);
 				
 				if (yomi != null) {
 					
 //					wordList.get(i).setGana(gana);
 					word.setYomi(yomi);
 					
 				} else {//if (gana != null)
 					
 //					wordList.get(i).setGana(null);
 					word.setYomi(null);
 					
 					// Log
 					Log.d("Task_GetYomi.java"
 							+ "["
 							+ Thread.currentThread().getStackTrace()[2]
 									.getLineNumber()
 							+ ":"
 							+ Thread.currentThread().getStackTrace()[2]
 									.getMethodName() + "]",
 							"yomi == null"
 							+ "(id=" + wordList.get(i).getId() + ")");
 					
 					continue;
 					
 				}//if (gana != null)
 				
 				
 			} else {//if (word.getFuri() != null)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]", "word.getFuri() == null");
 				
 				word.setYomi(null);
 				
 			}//if (word.getFuri() != null)
 			
 		}//for (int i = 0; i < wordList.size(); i++)
 		
 		/***************************************
 		 * Return
 		 ***************************************/
 		return wordList;
 		
 	}//private static List<Word> doInBackground_B18_v_6_0__3_convertCombo2Yomi
 
 	/***************************************
 	 * Created at: 20130223_141154
 	 * 
 	 ***************************************/
 	private static void doInBackground_B18_v_6_0__4_updateTable(
 			List<Word> wordList) {
 		/***************************************
 		 * Setup
 		 ***************************************/
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 		
 		String sql = null;
 		
 		// Variables for debugging
 		int numOfTargets = wordList.size();
 		int numOfSuccess = 0;
 		int numOfFail = 0;
 		
 		/***************************************
 		 * Update
 		 ***************************************/
 		for (int i = 0; i < wordList.size(); i++) {
 			
 			Word word = wordList.get(i);
 			
 //			long dbId = wordList.get(i).getId();
 			long dbId = word.getId();
 			
 			String colYomi = CONS.columns[Methods.getArrayIndex(CONS.columns, "yomi")];
 			
 			// Get "gana" value: "gana" value in a Word instance 
 			//	corresponds to "yomi" yomi in db
 //			String yomi = word.getName();
 			String yomi = word.getYomi();
 			
 			int res = dbu.updateData_shoppingItem(actv, wdb, CONS.tableName, dbId, colYomi, yomi);
 
 			if (res == CONS.DB_UPDATE_SUCCESSFUL) {
 				
 				numOfSuccess += 1;
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"Data updated: name=" + word.getName()
 						+ "/"
 						+ "yomi=" + yomi);
 				
 			} else {//if (res == CONS.DB_UPDATE_SUCCESSFUL)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"Update failed => id=" + dbId
 						+ "/"
 						+ "name=" + word.getName());
 				
 			}//if (res == CONS.DB_UPDATE_SUCCESSFUL)
 			
 			
 			
 		}//for (int i = 0; i < wordList.size(); i++)
 		
 		
 		/***************************************
 		 * Close db
 		 ***************************************/
 		wdb.close();
 		
 		/***************************************
 		 * Result
 		 ***************************************/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"numOfTargets=" + numOfTargets
 				+ "/"
 				+ "numOfSuccess=" + numOfSuccess
 				+ "/"
 				+ "numOfFail=" + numOfFail);
 		
 	}//private static void doInBackground_B18_v_6_0__4_updateTable
 
 	private static Integer doInBackground_B18_v_5_4() {
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Time=" + Methods.get_TimeLabel(Methods.getMillSeconds_now()));
 		
 		/***************************************
 		 * Furigana list
 		 ***************************************/
 		List<Furi> furiganaList = doInBackground_B18_v_5_4__1_getFuriganaList();
 
 		/*********************************
 		 * Get furigana
 		 *********************************/
 		furiganaList = doInBackground_B18_v_5_4__2_getFurigana(furiganaList);
 		
 ////		List<String> furiganaList = new ArrayList<String>();
 ////		List<Furi> furiganaList = new ArrayList<Furi>();
 //		
 ////		YahooFurigana yf = YahooFurigana.getInstance();
 ////		
 //////		for (int i = 0; i < itemNames.size(); i++) {
 ////		for (int i = 0; i < furiganaList.size(); i++) {
 ////			
 ////			Furi objFuri = furiganaList.get(i);
 ////			
 ////			if (objFuri.getGana() != null) {
 ////				
 ////				
 ////				
 ////			} else {//if (objFuri.getGana() != null)
 ////				line2
 ////			}//if (objFuri.getGana() != null)
 ////			
 ////				String keyWord = furiganaList.get(i).getName();
 ////				
 ////	//			YahooFurigana yf = YahooFurigana.getInstance();
 ////				
 ////				String furi = yf.getFurigana(keyWord, true);
 ////	
 ////				// Log
 ////				Log.d("Task_GetYomi.java" + "["
 ////						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 ////						+ ":"
 ////						+ Thread.currentThread().getStackTrace()[2].getMethodName()
 ////						+ "]", "furi=" + furi);
 ////	
 ////	//			furiganaList.add(furi);
 ////				furiganaList.get(i).setFuri(furi);
 ////			
 ////		}//for (int i = 0; i < itemNames.size(); i++)
 ////		
 ////		//
 //
 		/***************************************
 		 * Get: Gana ("Gana" => all-hiragana string, as opposed to
 		 * 		"furi", which is katakana-including
 		 * 1. If "furi" value of the Furi instance is null
 		 * 		=> Set "gana" value to null, also 
 		 ***************************************/
 		furiganaList = doInBackground_B18_v_5_4__3_getGana(furiganaList);
 		
 //		for (int i = 0; i < furiganaList.size(); i++) {
 //
 //			Furi f = furiganaList.get(i);
 //			
 //			if (f.getFuri() != null) {
 //				
 //				String gana = Methods.convert_Kana2Gana(f.getFuri());
 //				
 //				if (gana != null) {
 //					
 //					furiganaList.get(i).setGana(gana);
 //					
 //				} else {//if (gana != null)
 //					
 //					furiganaList.get(i).setGana(null);
 //					
 //					// Log
 //					Log.d("Task_GetYomi.java"
 //							+ "["
 //							+ Thread.currentThread().getStackTrace()[2]
 //									.getLineNumber()
 //							+ ":"
 //							+ Thread.currentThread().getStackTrace()[2]
 //									.getMethodName() + "]",
 //							"gana == null"
 //							+ "(id=" + furiganaList.get(i).getId() + ")");
 //					
 //					continue;
 //					
 //				}//if (gana != null)
 //				
 //				
 //			} else {//if (f.getFuri() != null)
 //				
 //				// Log
 //				Log.d("Task_GetYomi.java"
 //						+ "["
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getMethodName() + "]", "f.getFuri() == null");
 //				
 //				furiganaList.get(i).setGana(null);
 //				
 //			}//if (f.getFuri() != null)
 //			
 //		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		/***************************************
 		 * Update table
 		 ***************************************/
 		doInBackground_B18_v_5_3__2_updateTable(furiganaList);
 		
 		/***************************************
 		 * Debug
 		 ***************************************/
 		int numOfYes = 0;
 		int numOfNo = 0;
 		
 		for (int i = 0; i < furiganaList.size(); i++) {
 		
 			Furi f = furiganaList.get(i);
 			
 			if (f.getGana() != null) {
 				
 				numOfYes += 1;
 				
 			} else {//if (f.getGana() != null)
 				
 				numOfNo += 1;
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"Gana => null(id=" + f.getId() + "/" + f.getName() + ")");
 				
 			}//if (f.getGana() != null)
 			
 
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"furiganaList.size()=" + furiganaList.size()
 				+ "/"
 				+ "numOfYes=" + numOfYes
 				+ "/"
 				+ "numOfNo=" + numOfNo);
 	
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Time=" + Methods.get_TimeLabel(Methods.getMillSeconds_now()));
 
 		/***************************************
 		 * Return
 		 ***************************************/
 		return CONS.GETYOMI_SUCCESSFUL;
 		
 	}//private static void doInBackground_B18_v_5_4()
 
 	private static List<Furi> doInBackground_B18_v_5_4__3_getGana(
 			List<Furi> furiganaList) {
 		for (int i = 0; i < furiganaList.size(); i++) {
 
 			Furi f = furiganaList.get(i);
 			
 			if (f.getFuri() != null) {
 				
 				String gana = Methods.convert_Kana2Gana(f.getFuri());
 				
 				if (gana != null) {
 					
 					furiganaList.get(i).setGana(gana);
 					
 				} else {//if (gana != null)
 					
 					furiganaList.get(i).setGana(null);
 					
 					// Log
 					Log.d("Task_GetYomi.java"
 							+ "["
 							+ Thread.currentThread().getStackTrace()[2]
 									.getLineNumber()
 							+ ":"
 							+ Thread.currentThread().getStackTrace()[2]
 									.getMethodName() + "]",
 							"gana == null"
 							+ "(id=" + furiganaList.get(i).getId() + ")");
 					
 					continue;
 					
 				}//if (gana != null)
 				
 				
 			} else {//if (f.getFuri() != null)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]", "f.getFuri() == null");
 				
 				furiganaList.get(i).setGana(null);
 				
 			}//if (f.getFuri() != null)
 			
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		/***************************************
 		 * Return
 		 ***************************************/
 		return furiganaList;
 		
 	}//private static void doInBackground_B18_v_5_4__3_getGana
 
 	/***************************************
 	 * Conduct YahooFurigana.getFurigana(keyWord, true)
 	 ***************************************/
 	private static List<Furi> doInBackground_B18_v_5_4__2_getFurigana(
 			List<Furi> furiganaList) {
 		
 		YahooFurigana yf = YahooFurigana.getInstance();
 		
 		int isNull = 0;
 		int isNotNull = 0;
 		
 		int count = 0;
 		int numOfSamples = 10;
 		
 //		for (int i = 0; i < itemNames.size(); i++) {
 //		for (int i = 0; i < furiganaList.size(); i++) {
 		for (int i = 0; (i < furiganaList.size()) && count < numOfSamples; i++) {
 			
 			Furi objFuri = furiganaList.get(i);
 			
 			if (objFuri.getGana() != null) {
 				
 				isNotNull += 1;
 				
 			} else {//if (objFuri.getGana() != null)
 				
 //				isNull += 1;
 				String keyWord = furiganaList.get(i).getName();
 				
 	//			YahooFurigana yf = YahooFurigana.getInstance();
 				
 				String furi = yf.getFurigana(keyWord, true);
 	
 				// Log
 				Log.d("Task_GetYomi.java" + "["
 						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2].getMethodName()
 						+ "]", "furi=" + furi);
 	
 	//			furiganaList.add(furi);
 				furiganaList.get(i).setFuri(furi);
 				
 				// Increment
 				count += 1;
 				
 			}//if (objFuri.getGana() != null)
 			
 //				String keyWord = furiganaList.get(i).getName();
 //				
 //	//			YahooFurigana yf = YahooFurigana.getInstance();
 //				
 //				String furi = yf.getFurigana(keyWord, true);
 //	
 //				// Log
 //				Log.d("Task_GetYomi.java" + "["
 //						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //						+ "]", "furi=" + furi);
 //	
 //	//			furiganaList.add(furi);
 //				furiganaList.get(i).setFuri(furi);
 			
 		}//for (int i = 0; i < itemNames.size(); i++)
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Get furigana => Done");
 		
 		/***************************************
 		 * Return
 		 ***************************************/
 		return furiganaList;
 		
 		//
 
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]",
 //				"isNull=" + isNull
 //				+ "/"
 //				+ "isNotNull=" + isNotNull);
 		
 	}//private static void doInBackground_B18_v_5_4__2_getFurigana
 
 	private static List<Furi> doInBackground_B18_v_5_4__1_getFuriganaList() {
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*----------------------------
 		 * 0. Table exists?
 			----------------------------*/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "tableName=" + CONS.tableName);
 		
 		boolean res = dbu.tableExists(rdb, CONS.tableName);
 		
 		if (res == false) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "getAllData() => Table doesn't exist: " + CONS.tableName);
 			
 			rdb.close();
 			
 			return null;
 			
 		}//if (res == false)
 		
 		/*----------------------------
 		 * 2. Get data
 		 * 		2.1. Get cursor
 		 * 		2.2. Add to list
 			----------------------------*/
 		//
 		String sql = "SELECT * FROM " + CONS.tableName;
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount()=" + c.getCount());
 			
 		} catch (Exception e) {
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return null;
 		}
 	
 		/*********************************
 		 * Get names
 		 *********************************/
 //		List<String> itemNames = new ArrayList<String>();
 //		
 //		List<Long> itemIds = new ArrayList<Long>();
 		
 		List<Furi> furiganaList = new ArrayList<Furi>();
 		
 		c.moveToFirst();
 		
 //		int numOfSamples = 10;
 		int numOfSamples = c.getCount();
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "numOfSamples=" + numOfSamples);
 		
 //		for (int i = 0; i < 10; i++) {
 		for (int i = 0; i < numOfSamples; i++) {
 			
 			String name = c.getString(CONS.colAddUp + Methods.getArrayIndex(CONS.columns, "name"));
 			
 			String yomi = c.getString(CONS.colAddUp + Methods.getArrayIndex(CONS.columns, "yomi"));
 			
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //											CONS.columns,
 ////											String.valueOf(android.provider.BaseColumns._ID)));
 //											android.provider.BaseColumns._ID));
 
 			long itemId = c.getLong(0);
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //					CONS.columns,
 //					);
 			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "android.provider.BaseColumns._ID=" + android.provider.BaseColumns._ID);
 			
 			if (name != null) {
 				
 				furiganaList.add(new Furi(itemId, name, yomi));
 				
 //				itemNames.add(name);
 //				
 //				itemIds.add(itemId);
 				
 			}//if (name != null)
 			
 			/*********************************
 			 * Next entry
 			 *********************************/
 			c.moveToNext();
 			
 		}//for (int i = 0; i < 10; i++)
 		
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "furiganaList.size()=" + furiganaList.size());
 		
 		rdb.close();
 		
 		/***************************************
 		 * Return
 		 ***************************************/
 		return furiganaList;
 		
 	}//private static List<Furi> doInBackground_B18_v_5_4__1_getFuriganaList()
 
 	private static void doInBackground_B18_v_5_4__2_updateTable(
 			List<Furi> furiganaList) {
 		/***************************************
 		 * Setup
 		 ***************************************/
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 		
 		String sql = null;
 		
 		// Variables for debugging
 		int numOfTargets = furiganaList.size();
 		int numOfSuccess = 0;
 		int numOfFail = 0;
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]",
 //				"colName=" + CONS.columns[Methods.getArrayIndex(CONS.columns, "yomi")]);
 
 		/***************************************
 		 * Update
 		 ***************************************/
 		for (int i = 0; i < furiganaList.size(); i++) {
 			
 			Furi furi = furiganaList.get(i);
 			
 //			long dbId = furiganaList.get(i).getId();
 			long dbId = furi.getId();
 			
 			String colName = CONS.columns[Methods.getArrayIndex(CONS.columns, "yomi")];
 			
 //			String value = furi.getName();
 			String value = furi.getGana();
 			
 			int res = dbu.updateData_shoppingItem(actv, wdb, CONS.tableName, dbId, colName, value);
 
 			if (res == CONS.DB_UPDATE_SUCCESSFUL) {
 				
 				numOfSuccess += 1;
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"Data updated: name=" + furi.getName()
 						+ "/"
 						+ "gana=" + value);
 				
 			} else {//if (res == CONS.DB_UPDATE_SUCCESSFUL)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"Update failed => id=" + dbId
 						+ "/"
 						+ "name=" + value);
 				
 			}//if (res == CONS.DB_UPDATE_SUCCESSFUL)
 			
 			
 			
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		
 		/***************************************
 		 * Close db
 		 ***************************************/
 		wdb.close();
 		
 		/***************************************
 		 * Result
 		 ***************************************/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"numOfTargets=" + numOfTargets
 				+ "/"
 				+ "numOfSuccess=" + numOfSuccess
 				+ "/"
 				+ "numOfFail=" + numOfFail);
 		
 	}//private static void doInBackground_B18_v_5_4__2_updateTable
 
 	private static Integer doInBackground_B18_v_5_3() {
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Time=" + Methods.get_TimeLabel(Methods.getMillSeconds_now()));
 		
 		List<Furi> furiganaList = doInBackground_B18_v_5_3__1_getFuriganaList();
 
 		/*********************************
 		 * Get furigana
 		 *********************************/
 //		List<String> furiganaList = new ArrayList<String>();
 //		List<Furi> furiganaList = new ArrayList<Furi>();
 		
 		YahooFurigana yf = YahooFurigana.getInstance();
 		
 //		for (int i = 0; i < itemNames.size(); i++) {
 		for (int i = 0; i < furiganaList.size(); i++) {
 			
 			String keyWord = furiganaList.get(i).getName();
 			
 //			YahooFurigana yf = YahooFurigana.getInstance();
 			
 			String furi = yf.getFurigana(keyWord, true);
 
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "furi=" + furi);
 
 //			furiganaList.add(furi);
 			furiganaList.get(i).setFuri(furi);
 			
 		}//for (int i = 0; i < itemNames.size(); i++)
 		
 		//
 
 		/***************************************
 		 * Get: Gana
 		 * 1. If "furi" value of the Furi instance is null
 		 * 		=> Set "gana" value to null, also 
 		 ***************************************/
 		for (int i = 0; i < furiganaList.size(); i++) {
 
 			Furi f = furiganaList.get(i);
 			
 			if (f.getFuri() != null) {
 				
 				String gana = Methods.convert_Kana2Gana(f.getFuri());
 				
 				if (gana != null) {
 					
 					furiganaList.get(i).setGana(gana);
 					
 				} else {//if (gana != null)
 					
 					furiganaList.get(i).setGana(null);
 					
 					// Log
 					Log.d("Task_GetYomi.java"
 							+ "["
 							+ Thread.currentThread().getStackTrace()[2]
 									.getLineNumber()
 							+ ":"
 							+ Thread.currentThread().getStackTrace()[2]
 									.getMethodName() + "]",
 							"gana == null"
 							+ "(id=" + furiganaList.get(i).getId() + ")");
 					
 					continue;
 					
 				}//if (gana != null)
 				
 				
 			} else {//if (f.getFuri() != null)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]", "f.getFuri() == null");
 				
 				furiganaList.get(i).setGana(null);
 				
 			}//if (f.getFuri() != null)
 			
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		/***************************************
 		 * Update table
 		 ***************************************/
 		doInBackground_B18_v_5_3__2_updateTable(furiganaList);
 		
 		/***************************************
 		 * Debug
 		 ***************************************/
 		int numOfYes = 0;
 		int numOfNo = 0;
 		
 		for (int i = 0; i < furiganaList.size(); i++) {
 		
 			Furi f = furiganaList.get(i);
 			
 			if (f.getGana() != null) {
 				
 				numOfYes += 1;
 				
 			} else {//if (f.getGana() != null)
 				
 				numOfNo += 1;
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"Gana => null(id=" + f.getId() + "/" + f.getName() + ")");
 				
 			}//if (f.getGana() != null)
 			
 
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"furiganaList.size()=" + furiganaList.size()
 				+ "/"
 				+ "numOfYes=" + numOfYes
 				+ "/"
 				+ "numOfNo=" + numOfNo);
 	
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Time=" + Methods.get_TimeLabel(Methods.getMillSeconds_now()));
 
 		/***************************************
 		 * Return
 		 ***************************************/
 		return CONS.GETYOMI_SUCCESSFUL;
 		
 	}//private static void doInBackground_B18_v_5_3()
 
 	private static void doInBackground_B18_v_5_3__2_updateTable(
 			List<Furi> furiganaList) {
 		/***************************************
 		 * Setup
 		 ***************************************/
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 		
 		String sql = null;
 		
 		// Variables for debugging
 		int numOfTargets = furiganaList.size();
 		int numOfSuccess = 0;
 		int numOfFail = 0;
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]",
 //				"colName=" + CONS.columns[Methods.getArrayIndex(CONS.columns, "yomi")]);
 
 		/***************************************
 		 * Update
 		 ***************************************/
 		for (int i = 0; i < furiganaList.size(); i++) {
 			
 			Furi furi = furiganaList.get(i);
 			
 //			long dbId = furiganaList.get(i).getId();
 			long dbId = furi.getId();
 			
 			String colName = CONS.columns[Methods.getArrayIndex(CONS.columns, "yomi")];
 			
 			// Get "gana" value: "gana" value in a Furi instance 
 			//	corresponds to "yomi" value in db
 //			String value = furi.getName();
 			String value = furi.getGana();
 			
 			int res = dbu.updateData_shoppingItem(actv, wdb, CONS.tableName, dbId, colName, value);
 
 			if (res == CONS.DB_UPDATE_SUCCESSFUL) {
 				
 				numOfSuccess += 1;
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"Data updated: name=" + furi.getName()
 						+ "/"
 						+ "gana=" + value);
 				
 			} else {//if (res == CONS.DB_UPDATE_SUCCESSFUL)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"Update failed => id=" + dbId
 						+ "/"
 						+ "name=" + value);
 				
 			}//if (res == CONS.DB_UPDATE_SUCCESSFUL)
 			
 			
 			
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		
 		/***************************************
 		 * Close db
 		 ***************************************/
 		wdb.close();
 		
 		/***************************************
 		 * Result
 		 ***************************************/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"numOfTargets=" + numOfTargets
 				+ "/"
 				+ "numOfSuccess=" + numOfSuccess
 				+ "/"
 				+ "numOfFail=" + numOfFail);
 		
 	}//private static void doInBackground_B18_v_5_3__2_updateTable
 
 	private static List<Furi> doInBackground_B18_v_5_3__1_getFuriganaList() {
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*----------------------------
 		 * 0. Table exists?
 			----------------------------*/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "tableName=" + CONS.tableName);
 		
 		boolean res = dbu.tableExists(rdb, CONS.tableName);
 		
 		if (res == false) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "getAllData() => Table doesn't exist: " + CONS.tableName);
 			
 			rdb.close();
 			
 			return null;
 			
 		}//if (res == false)
 		
 		/*----------------------------
 		 * 2. Get data
 		 * 		2.1. Get cursor
 		 * 		2.2. Add to list
 			----------------------------*/
 		//
 		String sql = "SELECT * FROM " + CONS.tableName;
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount()=" + c.getCount());
 			
 		} catch (Exception e) {
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return null;
 		}
 	
 		/*********************************
 		 * Get names
 		 *********************************/
 //		List<String> itemNames = new ArrayList<String>();
 //		
 //		List<Long> itemIds = new ArrayList<Long>();
 		
 		List<Furi> furiganaList = new ArrayList<Furi>();
 		
 		c.moveToFirst();
 		
 		int numOfSamples = 20;
 //		int numOfSamples = c.getCount();
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "numOfSamples=" + numOfSamples);
 		
 //		for (int i = 0; i < 10; i++) {
 		for (int i = 0; i < numOfSamples; i++) {
 			
 			String name = c.getString(CONS.colAddUp + Methods.getArrayIndex(CONS.columns, "name"));
 			
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //											CONS.columns,
 ////											String.valueOf(android.provider.BaseColumns._ID)));
 //											android.provider.BaseColumns._ID));
 
 			long itemId = c.getLong(0);
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //					CONS.columns,
 //					);
 			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "android.provider.BaseColumns._ID=" + android.provider.BaseColumns._ID);
 			
 			if (name != null) {
 				
 				furiganaList.add(new Furi(itemId, name));
 				
 //				itemNames.add(name);
 //				
 //				itemIds.add(itemId);
 				
 			}//if (name != null)
 			
 			/*********************************
 			 * Next entry
 			 *********************************/
 			c.moveToNext();
 			
 		}//for (int i = 0; i < 10; i++)
 		
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "furiganaList.size()=" + furiganaList.size());
 		
 		rdb.close();
 		
 		/***************************************
 		 * Return
 		 ***************************************/
 		return furiganaList;
 		
 	}//private static List<Furi> doInBackground_B18_v_5_3__1_getFuriganaList()
 
 	private static Integer doInBackground_B18_v_5_2() {
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Time=" + Methods.get_TimeLabel(Methods.getMillSeconds_now()));
 		
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*----------------------------
 		 * 0. Table exists?
 			----------------------------*/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "tableName=" + CONS.tableName);
 		
 		boolean res = dbu.tableExists(rdb, CONS.tableName);
 		
 		if (res == false) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "getAllData() => Table doesn't exist: " + CONS.tableName);
 			
 			rdb.close();
 			
 			return CONS.GETYOMI_FAILED;
 			
 		}//if (res == false)
 		
 		/*----------------------------
 		 * 2. Get data
 		 * 		2.1. Get cursor
 		 * 		2.2. Add to list
 			----------------------------*/
 		//
 		String sql = "SELECT * FROM " + CONS.tableName;
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount()=" + c.getCount());
 			
 		} catch (Exception e) {
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return CONS.GETYOMI_FAILED;
 		}
 	
 		/*********************************
 		 * Get names
 		 *********************************/
 //		List<String> itemNames = new ArrayList<String>();
 //		
 //		List<Long> itemIds = new ArrayList<Long>();
 		
 		List<Furi> furiganaList = new ArrayList<Furi>();
 		
 		c.moveToFirst();
 		
 //		int numOfSamples = 20;
 		int numOfSamples = c.getCount();
 		
 //		for (int i = 0; i < 10; i++) {
 		for (int i = 0; i < numOfSamples; i++) {
 			
 			String name = c.getString(CONS.colAddUp + Methods.getArrayIndex(CONS.columns, "name"));
 			
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //											CONS.columns,
 ////											String.valueOf(android.provider.BaseColumns._ID)));
 //											android.provider.BaseColumns._ID));
 
 			long itemId = c.getLong(0);
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //					CONS.columns,
 //					);
 			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "android.provider.BaseColumns._ID=" + android.provider.BaseColumns._ID);
 			
 			if (name != null) {
 				
 				furiganaList.add(new Furi(itemId, name));
 				
 //				itemNames.add(name);
 //				
 //				itemIds.add(itemId);
 				
 			}//if (name != null)
 			
 			/*********************************
 			 * Next entry
 			 *********************************/
 			c.moveToNext();
 			
 		}//for (int i = 0; i < 10; i++)
 		
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "furiganaList.size()=" + furiganaList.size());
 		
 		rdb.close();
 		
 		/*********************************
 		 * Get furigana
 		 *********************************/
 //		List<String> furiganaList = new ArrayList<String>();
 //		List<Furi> furiganaList = new ArrayList<Furi>();
 		
 		YahooFurigana yf = YahooFurigana.getInstance();
 		
 //		for (int i = 0; i < itemNames.size(); i++) {
 		for (int i = 0; i < furiganaList.size(); i++) {
 			
 			String keyWord = furiganaList.get(i).getName();
 			
 //			YahooFurigana yf = YahooFurigana.getInstance();
 			
 			String furi = yf.getFurigana(keyWord, true);
 
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "furi=" + furi);
 
 //			furiganaList.add(furi);
 			furiganaList.get(i).setFuri(furi);
 			
 		}//for (int i = 0; i < itemNames.size(); i++)
 		
 		//
 
 		/***************************************
 		 * Get: Gana
 		 ***************************************/
 		for (int i = 0; i < furiganaList.size(); i++) {
 
 			Furi f = furiganaList.get(i);
 			
 			if (f.getFuri() != null) {
 				
 				String gana = Methods.convert_Kana2Gana(f.getFuri());
 				
 				if (gana != null) {
 					
 					furiganaList.get(i).setGana(gana);
 					
 				} else {//if (gana != null)
 					
 					furiganaList.get(i).setGana(null);
 					
 					// Log
 					Log.d("Task_GetYomi.java"
 							+ "["
 							+ Thread.currentThread().getStackTrace()[2]
 									.getLineNumber()
 							+ ":"
 							+ Thread.currentThread().getStackTrace()[2]
 									.getMethodName() + "]",
 							"gana == null"
 							+ "(id=" + furiganaList.get(i).getId() + ")");
 					
 					continue;
 					
 				}//if (gana != null)
 				
 				
 			} else {//if (f.getFuri() != null)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]", "f.getFuri() == null");
 				
 			}//if (f.getFuri() != null)
 			
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		/***************************************
 		 * Debug
 		 ***************************************/
 		int numOfYes = 0;
 		int numOfNo = 0;
 		
 		for (int i = 0; i < furiganaList.size(); i++) {
 		
 			Furi f = furiganaList.get(i);
 			
 			if (f.getGana() != null) {
 				
 				numOfYes += 1;
 				
 			} else {//if (f.getGana() != null)
 				
 				numOfNo += 1;
 				
 			}//if (f.getGana() != null)
 			
 
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"furiganaList.size()=" + furiganaList.size()
 				+ "/"
 				+ "numOfYes=" + numOfYes
 				+ "/"
 				+ "numOfNo=" + numOfNo);
 	
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Time=" + Methods.get_TimeLabel(Methods.getMillSeconds_now()));
 
 		/***************************************
 		 * Return
 		 ***************************************/
 		return CONS.GETYOMI_SUCCESSFUL;
 		
 	}//private static void doInBackground_B18_v_5_2()
 
 	private static Integer doInBackground_B18_v_5_0_e_1_t_1() {
 		String keyWord = "洗濯網（中）目玉クリップ";
 //		String keyWord = "かりんとう";
 //		String keyWord = "のど飴";
 		
 //		YahooFurigana yf = YahooFurigana.getInstanceWithKeyWord(keyWord);
 //		
 //		String furi = yf.getFurigana(true);
 		
 		YahooFurigana yf = YahooFurigana.getInstanceWithKeyWord(keyWord);
 		
 		String furi = yf.getFurigana(keyWord, true);
 //		String furi = yf.getFurigana_B18_v_5_0_e_1_t_1(keyWord, true);
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"keyWord=" + keyWord + "/" + "furi=" + furi);
 
 		if (furi != null) {
 			
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "furi(Hiragana)=" + Methods.convert_Kana2Gana(furi));
 
 			return CONS.GETYOMI_SUCCESSFUL;
 			
 		} else {//if (furi != null)
 			
 			return CONS.GETYOMI_FAILED;
 			
 		}//if (furi != null)
 
 	}//private static Integer doInBackground_B18_v_5_0_e_1_t_1()
 
 	private static Integer doInBackground_B18_v_5_1a() {
 		// TODO Auto-generated method stub
 //		String keyWord = "洗濯網（中）";
 //		String keyWord = "目玉クリップ";
 		
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*----------------------------
 		 * 0. Table exists?
 			----------------------------*/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "tableName=" + CONS.tableName);
 		
 		boolean res = dbu.tableExists(rdb, CONS.tableName);
 		
 		if (res == false) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "getAllData() => Table doesn't exist: " + CONS.tableName);
 			
 			rdb.close();
 			
 			return CONS.GETYOMI_FAILED;
 			
 		}//if (res == false)
 		
 		/*----------------------------
 		 * 2. Get data
 		 * 		2.1. Get cursor
 		 * 		2.2. Add to list
 			----------------------------*/
 		//
 		String sql = "SELECT * FROM " + CONS.tableName;
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount()=" + c.getCount());
 			
 		} catch (Exception e) {
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return CONS.GETYOMI_FAILED;
 		}
 	
 		/*********************************
 		 * Get names
 		 *********************************/
 //		List<String> itemNames = new ArrayList<String>();
 //		
 //		List<Long> itemIds = new ArrayList<Long>();
 		
 		List<Furi> furiganaList = new ArrayList<Furi>();
 		
 		c.moveToFirst();
 		
 		int numOfSamples = 20;
 		
 //		for (int i = 0; i < 10; i++) {
 		for (int i = 0; i < numOfSamples; i++) {
 			
 			String name = c.getString(CONS.colAddUp + Methods.getArrayIndex(CONS.columns, "name"));
 			
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //											CONS.columns,
 ////											String.valueOf(android.provider.BaseColumns._ID)));
 //											android.provider.BaseColumns._ID));
 
 			long itemId = c.getLong(0);
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //					CONS.columns,
 //					);
 			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "android.provider.BaseColumns._ID=" + android.provider.BaseColumns._ID);
 			
 			if (name != null) {
 				
 				furiganaList.add(new Furi(itemId, name));
 				
 //				itemNames.add(name);
 //				
 //				itemIds.add(itemId);
 				
 			}//if (name != null)
 			
 			/*********************************
 			 * Next entry
 			 *********************************/
 			c.moveToNext();
 			
 		}//for (int i = 0; i < 10; i++)
 		
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "furiganaList.size()=" + furiganaList.size());
 		
 		rdb.close();
 		
 		/*********************************
 		 * Get furigana
 		 *********************************/
 //		List<String> furiganaList = new ArrayList<String>();
 //		List<Furi> furiganaList = new ArrayList<Furi>();
 		
 		YahooFurigana yf = YahooFurigana.getInstance();
 		
 //		for (int i = 0; i < itemNames.size(); i++) {
 		for (int i = 0; i < furiganaList.size(); i++) {
 			
 			String keyWord = furiganaList.get(i).getName();
 			
 //			YahooFurigana yf = YahooFurigana.getInstance();
 			
 			String furi = yf.getFurigana(keyWord, true);
 
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "furi=" + furi);
 
 //			furiganaList.add(furi);
 			furiganaList.get(i).setFuri(furi);
 			
 //			if (furi != null) {
 //				
 //				Log.d("Task_GetYomi.java" + "["
 //						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //						+ "]", "furi(Hiragana)=" + Methods.convert_Kana2Gana(furi));
 //
 ////				return CONS.GETYOMI_SUCCESSFUL;
 //				
 //				furiganaList.add(furi);
 //				
 //			} else {//if (furi != null)
 //				
 //				
 //				
 //				furiganaList.add(null);
 //				
 //			}//if (furi != null)
 
 			//			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]",
 //					"id=" + itemIds.get(i)
 //					+ "/"
 //					+ "name=" + itemNames.get(i));
 			
 		}//for (int i = 0; i < itemNames.size(); i++)
 		
 		//
 
 		/***************************************
 		 * Get: Gana
 		 ***************************************/
 		for (int i = 0; i < furiganaList.size(); i++) {
 
 			Furi f = furiganaList.get(i);
 			
 			if (f.getFuri() != null) {
 				
 				String gana = Methods.convert_Kana2Gana(f.getFuri());
 				
 				if (gana != null) {
 					
 					furiganaList.get(i).setGana(gana);
 					
 				} else {//if (gana != null)
 					
 					furiganaList.get(i).setGana(null);
 					
 					// Log
 					Log.d("Task_GetYomi.java"
 							+ "["
 							+ Thread.currentThread().getStackTrace()[2]
 									.getLineNumber()
 							+ ":"
 							+ Thread.currentThread().getStackTrace()[2]
 									.getMethodName() + "]", "gana == null");
 					
 					continue;
 					
 				}//if (gana != null)
 				
 				
 			} else {//if (f.getFuri() != null)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]", "f.getFuri() == null");
 				
 			}//if (f.getFuri() != null)
 			
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		/***************************************
 		 * Debug
 		 ***************************************/
 		for (int i = 0; i < furiganaList.size(); i++) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]",
 					"id=" + furiganaList.get(i).getId()
 					+ "/"
 					+ "name=" + furiganaList.get(i).getName()
 					+ "/"
 					+ "furi=" + furiganaList.get(i).getFuri()
 					+ "/"
 					+ "gana=" + furiganaList.get(i).getGana());
 			
 //					"id=" + itemIds.get(i)
 //					+ "/"
 //					+ "name=" + itemNames.get(i)
 //					+ "/"
 //					+ "furi=" + furiganaList.get(i));
 			
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		/***************************************
 		 * Return
 		 ***************************************/
 		return CONS.GETYOMI_SUCCESSFUL;
 		
 	}//private static void doInBackground_B18_v_5_1a()
 
 	private static Integer doInBackground_B18_v_5_1() {
 		// TODO Auto-generated method stub
 //		String keyWord = "洗濯網（中）";
 //		String keyWord = "目玉クリップ";
 		
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*----------------------------
 		 * 0. Table exists?
 			----------------------------*/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "tableName=" + CONS.tableName);
 		
 		boolean res = dbu.tableExists(rdb, CONS.tableName);
 		
 		if (res == false) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "getAllData() => Table doesn't exist: " + CONS.tableName);
 			
 			rdb.close();
 			
 			return CONS.GETYOMI_FAILED;
 			
 		}//if (res == false)
 		
 		/*----------------------------
 		 * 2. Get data
 		 * 		2.1. Get cursor
 		 * 		2.2. Add to list
 			----------------------------*/
 		//
 		String sql = "SELECT * FROM " + CONS.tableName;
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount()=" + c.getCount());
 			
 		} catch (Exception e) {
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return CONS.GETYOMI_FAILED;
 		}
 	
 		/*********************************
 		 * Get names
 		 *********************************/
 		List<String> itemNames = new ArrayList<String>();
 		
 		List<Long> itemIds = new ArrayList<Long>();
 		
 		c.moveToFirst();
 		
 		for (int i = 0; i < 10; i++) {
 			
 			String name = c.getString(CONS.colAddUp + Methods.getArrayIndex(CONS.columns, "name"));
 			
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //											CONS.columns,
 ////											String.valueOf(android.provider.BaseColumns._ID)));
 //											android.provider.BaseColumns._ID));
 
 			long itemId = c.getLong(0);
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //					CONS.columns,
 //					);
 			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "android.provider.BaseColumns._ID=" + android.provider.BaseColumns._ID);
 			
 			if (name != null) {
 				
 				itemNames.add(name);
 				
 				itemIds.add(itemId);
 				
 			}//if (name != null)
 			
 			/*********************************
 			 * Next entry
 			 *********************************/
 			c.moveToNext();
 			
 		}//for (int i = 0; i < 10; i++)
 		
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "itemNames.size()=" + itemNames.size());
 		
 		rdb.close();
 		
 		/*********************************
 		 * Get furigana
 		 *********************************/
 		List<String> furiganaList = new ArrayList<String>();
 		
 		YahooFurigana yf = YahooFurigana.getInstance();
 		
 		for (int i = 0; i < itemNames.size(); i++) {
 			
 			String keyWord = itemNames.get(i);
 			
 //			YahooFurigana yf = YahooFurigana.getInstance();
 			
 			String furi = yf.getFurigana(keyWord, true);
 
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "furi=" + furi);
 
 			furiganaList.add(furi);
 			
 //			if (furi != null) {
 //				
 //				Log.d("Task_GetYomi.java" + "["
 //						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //						+ "]", "furi(Hiragana)=" + Methods.convert_Kana2Gana(furi));
 //
 ////				return CONS.GETYOMI_SUCCESSFUL;
 //				
 //				furiganaList.add(furi);
 //				
 //			} else {//if (furi != null)
 //				
 //				
 //				
 //				furiganaList.add(null);
 //				
 //			}//if (furi != null)
 
 			//			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]",
 //					"id=" + itemIds.get(i)
 //					+ "/"
 //					+ "name=" + itemNames.get(i));
 			
 		}//for (int i = 0; i < itemNames.size(); i++)
 		
 		//
 		
 		/***************************************
 		 * Debug
 		 ***************************************/
 		for (int i = 0; i < furiganaList.size(); i++) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]",
 					"id=" + itemIds.get(i)
 					+ "/"
 					+ "name=" + itemNames.get(i)
 					+ "/"
 					+ "furi=" + furiganaList.get(i));
 			
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		
 		return CONS.GETYOMI_SUCCESSFUL;
 		
 	}//private static void doInBackground_B18_v_5_1()
 
 	
 	private static Integer doInBackground_B18_v_5_0() {
 		// TODO Auto-generated method stub
 //		String keyWord = "洗濯網（中）";
 //		String keyWord = "目玉クリップ";
 		
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase rdb = dbu.getReadableDatabase();
 		
 		/*----------------------------
 		 * 0. Table exists?
 			----------------------------*/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ "]", "tableName=" + CONS.tableName);
 		
 		boolean res = dbu.tableExists(rdb, CONS.tableName);
 		
 		if (res == false) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "getAllData() => Table doesn't exist: " + CONS.tableName);
 			
 			rdb.close();
 			
 			return CONS.GETYOMI_FAILED;
 			
 		}//if (res == false)
 		
 		/*----------------------------
 		 * 2. Get data
 		 * 		2.1. Get cursor
 		 * 		2.2. Add to list
 			----------------------------*/
 		//
 		String sql = "SELECT * FROM " + CONS.tableName;
 		
 		Cursor c = null;
 		
 		try {
 			
 			c = rdb.rawQuery(sql, null);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "c.getCount()=" + c.getCount());
 			
 		} catch (Exception e) {
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "Exception => " + e.toString());
 			
 			rdb.close();
 			
 			return CONS.GETYOMI_FAILED;
 		}
 	
 		/*********************************
 		 * Get names
 		 *********************************/
 		List<String> itemNames = new ArrayList<String>();
 		
 		List<Long> itemIds = new ArrayList<Long>();
 		
 		c.moveToFirst();
 		
 		for (int i = 0; i < 10; i++) {
 			
 			String name = c.getString(CONS.colAddUp + Methods.getArrayIndex(CONS.columns, "name"));
 			
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //											CONS.columns,
 ////											String.valueOf(android.provider.BaseColumns._ID)));
 //											android.provider.BaseColumns._ID));
 
 			long itemId = c.getLong(0);
 //			long itemId = c.getLong(Methods.getArrayIndex(
 //					CONS.columns,
 //					);
 			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "android.provider.BaseColumns._ID=" + android.provider.BaseColumns._ID);
 			
 			if (name != null) {
 				
 				itemNames.add(name);
 				
 				itemIds.add(itemId);
 				
 			}//if (name != null)
 			
 			/*********************************
 			 * Next entry
 			 *********************************/
 			c.moveToNext();
 			
 		}//for (int i = 0; i < 10; i++)
 		
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "itemNames.size()=" + itemNames.size());
 		
 		rdb.close();
 		
 		/*********************************
 		 * Get furigana
 		 *********************************/
 		List<String> furiganaList = new ArrayList<String>();
 		
 		YahooFurigana yf = YahooFurigana.getInstance();
 		
 		for (int i = 0; i < itemNames.size(); i++) {
 			
 			String keyWord = itemNames.get(i);
 			
 //			YahooFurigana yf = YahooFurigana.getInstance();
 			
 			String furi = yf.getFurigana(keyWord, true);
 
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "furi=" + furi);
 
 			furiganaList.add(furi);
 			
 //			if (furi != null) {
 //				
 //				Log.d("Task_GetYomi.java" + "["
 //						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //						+ "]", "furi(Hiragana)=" + Methods.convert_Kana2Gana(furi));
 //
 ////				return CONS.GETYOMI_SUCCESSFUL;
 //				
 //				furiganaList.add(furi);
 //				
 //			} else {//if (furi != null)
 //				
 //				
 //				
 //				furiganaList.add(null);
 //				
 //			}//if (furi != null)
 
 			//			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]",
 //					"id=" + itemIds.get(i)
 //					+ "/"
 //					+ "name=" + itemNames.get(i));
 			
 		}//for (int i = 0; i < itemNames.size(); i++)
 		
 		//
 		
 		/***************************************
 		 * Debug
 		 ***************************************/
 		for (int i = 0; i < furiganaList.size(); i++) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]",
 					"id=" + itemIds.get(i)
 					+ "/"
 					+ "name=" + itemNames.get(i)
 					+ "/"
 					+ "furi=" + furiganaList.get(i));
 			
 		}//for (int i = 0; i < furiganaList.size(); i++)
 		
 		
 		return CONS.GETYOMI_SUCCESSFUL;
 		
 		
 //		String keyWord = "洗濯網（中）目玉クリップ";
 //		
 //		YahooFurigana yf = YahooFurigana.getInstance();
 //		
 //		String furi = yf.getFurigana(keyWord, true);
 //
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "furi=" + furi);
 //
 //		if (furi != null) {
 //			
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "furi(Hiragana)=" + Methods.convert_Kana2Gana(furi));
 //
 //			return CONS.GETYOMI_SUCCESSFUL;
 //			
 //		} else {//if (furi != null)
 //			
 //			return CONS.GETYOMI_FAILED;
 //			
 //		}//if (furi != null)
 		
 		
 	}//private static void doInBackground_B18_v_5_0()
 
 	private static Integer doInBackground_B18_v_4_1() {
 		// TODO Auto-generated method stub
 //		String keyWord = "洗濯網（中）";
 //		String keyWord = "目玉クリップ";
 		String keyWord = "洗濯網（中）目玉クリップ";
 		
 //		YahooFurigana yf = YahooFurigana.getInstanceWithKeyWord(keyWord);
 //		
 //		String furi = yf.getFurigana(true);
 		
 		YahooFurigana yf = YahooFurigana.getInstanceWithKeyWord(keyWord);
 		
 		String furi = yf.getFurigana(keyWord, true);
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "furi=" + furi);
 
 		if (furi != null) {
 			
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "furi(Hiragana)=" + Methods.convert_Kana2Gana(furi));
 
 			return CONS.GETYOMI_SUCCESSFUL;
 			
 		} else {//if (furi != null)
 			
 			return CONS.GETYOMI_FAILED;
 			
 		}//if (furi != null)
 		
 		
 	}//private static void doInBackground_B18_v_4_1()
 
 	private static Integer doInBackground_B18_v_4_0() {
 		// TODO Auto-generated method stub
 		String keyWord = "洗濯網（中）";
 		
 //		YahooFurigana yf = YahooFurigana.getInstanceWithKeyWord(keyWord);
 //		
 //		String furi = yf.getFurigana(true);
 		
 		YahooFurigana yf = YahooFurigana.getInstanceWithKeyWord(keyWord);
 		
 		String furi = yf.getFurigana(keyWord, true);
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "furi=" + furi);
 		
 		if (furi != null) {
 			
 			return CONS.GETYOMI_SUCCESSFUL;
 			
 		} else {//if (furi != null)
 			
 			return CONS.GETYOMI_FAILED;
 			
 		}//if (furi != null)
 		
 		
 	}//private static void doInBackground_B18_v_4_0()
 
 	private static void doInBackground_B18_v_3_0() {
 		// TODO Auto-generated method stub
 		String kw = "洗濯網（中）";
 		
 //		String url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana" +
 //				"?appid=dj0zaiZpPTZjQWNRNExhd0thayZkPVlXazlhR2gwTTJGUE56SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9Mjc-&grade=1" +
 //				"&sentence="
 //				+ kw;
 
 //		String url = "http://localhost/Learn_php/01/01_get_furigana.php?kw=%E5%88%B6%E5%BE%A1%E6%BC%94%E7%AE%97%E5%AD%90%E3%81%8C%E3%81%82%E3%81%A3%E3%81%A6%E3%82%82" +
 //				"&kw="
 //				+ kw;
 ////		http://localhost/Learn_php/01/01_get_furigana.php?kw=%E5%88%B6%E5%BE%A1%E6%BC%94%E7%AE%97%E5%AD%90%E3%81%8C%E3%81%82%E3%81%A3%E3%81%A6%E3%82%82
 		
 		String url = "http://benfranklin.chips.jp/Learn_php/01/01_get_furigana.php?kw="
 				+ kw;
 //		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "url=" + url);
 		
 		HttpPost httpPost = new HttpPost(url);
 		
 		httpPost.setHeader("Content-type", "application/json");
 		
 //		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
 //		
 //		paramList.add(new BasicNameValuePair("file_name", "1234.wav"));
 		
 		HttpUriRequest postRequest = httpPost;
 		
 		DefaultHttpClient dhc = new DefaultHttpClient();
 		
 		HttpResponse hr = null;
 		
 		try {
 			
 			hr = dhc.execute(postRequest);
 			
 		} catch (ClientProtocolException e) {
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", e.toString());
 		} catch (IOException e) {
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", e.toString());
 		}
 		
 
 		/*----------------------------
 		 * 6. Response null?
 			----------------------------*/
 		if (hr == null) {
 			
 //			// debug
 //			Toast.makeText(actv, "hr == null", 2000).show();
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ "]", "hr == null");
 			
 			return;
 			
 		} else {//if (hr == null)
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "hr => Not null");
 			
 		}//if (hr == null)
 
 		/*********************************
 		 * Status code
 		 *********************************/
 		int status = hr.getStatusLine().getStatusCode();
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "status=" + status);
 		
 		/*********************************
 		 * Entity
 		 *********************************/
 		HttpEntity entity = hr.getEntity();
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "entity.toString()=" + entity.toString());
 		
 		/*********************************
 		 * JSONObject
 		 *********************************/
 		JSONObject json = null;
 		
 		try {
 			
 			json = new JSONObject(EntityUtils.toString(entity));
 			
 		} catch (ParseException e) {
 			
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 			return;
 			
 		} catch (JSONException e) {
 
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 			return;
 
 		} catch (IOException e) {
 
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 			return;
 			
 		}//try
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "JSONObject => Created");
 		
 		/*********************************
 		 * Parse JSONObject
 		 *********************************/
 		JSONObject jsonResult = null;
 		
 		String tagName = "Result";
 //		String tagName = "abc";
 //		String tagName = "Word";
 		
 		try {
 			
 			jsonResult = json.getJSONObject(tagName);
 
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "JSONObject => Created (Tag=" + tagName + ")");
 
 		} catch (JSONException e) {
 			
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 			return;
 			
 		}//try
 		
 		/*********************************
 		 * Parse: WordList
 		 *********************************/
 //		JSONArray jsArray = null;
 		
 		JSONObject joWordList = null;
 
 		tagName = "WordList";
 //		String tagName = "abc";
 //		String tagName = "Word";
 		
 		try {
 			
 //			jsArray = jsonResult.getJSONArray(tagName);
 			joWordList = jsonResult.getJSONObject(tagName);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "JSONObject => Created (Tag=" + tagName + ")");
 
 		} catch (JSONException e) {
 			
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 			return;
 			
 		}//try
 
 		/*********************************
 		 * Parse: Word
 		 *********************************/
 		JSONArray jaWord = null;
 		
 		tagName = "Word";
 		
 		try {
 			
 			jaWord = joWordList.getJSONArray(tagName);
 
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "JSONArray => Created (Tag=" + tagName + ")");
 
 		} catch (JSONException e) {
 			
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 			return;
 			
 		}//try
 		
 		/*********************************
 		 * Parse: Surface
 		 *********************************/
 		JSONObject joSurface = null;
 
 //		tagName = "WordList";
 		int index = 0;		// Surface
 		
 		try {
 			
 //			jsArray = jsonResult.getJSONArray(tagName);
 			joSurface = jaWord.getJSONObject(0);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "JSONObject => Created (Tag=" + tagName + ")");
 
 		} catch (JSONException e) {
 			
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 			return;
 			
 		}//try
 
 		/*********************************
 		 * Get: Value of Surface
 		 *********************************/
 		String valSurface = null;
 		
 		try {
 			
 			valSurface = joSurface.getString("Surface");
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "valSurface=" + valSurface);
 			
 		} catch (JSONException e) {
 
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 			return;
 
 		}
 		
 		/*********************************
 		 * Get value
 		 *********************************/
 		String s = null;
 		
 		try {
 			
 			s = joWordList.getString("Surface");
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "s=" + s);
 			
 		} catch (JSONException e) {
 
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 			return;
 
 		}
 		
 	}//private static void doInBackground_B18_v_3_0()
 
 	private static void doInBackground_B18_v_2_0_d_t_2() {
 		// TODO Auto-generated method stub
 		String kw = "����ԁi���j";
 		
 		String url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana" +
 				"?appid=dj0zaiZpPTZjQWNRNExhd0thayZkPVlXazlhR2gwTTJGUE56SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9Mjc-" +
 				"&grade=1" +
 				"&sentence=" + kw;
 
 		XmlHandler xh = new XmlHandler();
 		
 		Document doc = xh.getDoc(url);
 		
 		String tagName = "Word";
 		
 		NodeList nlWord = doc.getElementsByTagName(tagName);
 
 		/*********************************
 		 * Word node
 		 *********************************/
 		Node nWord1 = nlWord.item(0);
 		
 		if (nWord1.getNodeType() == Node.ELEMENT_NODE) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "nWord1 => Element node");
 		} else {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "nNode => Not an element node");
 			
 			return;
 		}
 
 			//=> [98:doInBackground_B18_v_2_0_d_t_2](23709): nWord1 => Element node
 		
 		/*********************************
 		 * Node list of nWord1
 		 *********************************/
 		NodeList nlWord1Surface = ((Element) nWord1).getElementsByTagName("Surface");
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"nlWord1Surface.getLength()=" + nlWord1Surface.getLength());
 
 			//=> [123:doInBackground_B18_v_2_0_d_t_2](23876): nlWord1Surface.getLength()=1
 
 		/*********************************
 		 * Surface
 		 *********************************/
 		Node nWord1Surface = nlWord1Surface.item(0);
 		
 		/*********************************
 		 * Children of Surface
 		 *********************************/
 		NodeList nlChildrenOfSurface = nWord1Surface.getChildNodes();
 		
 		for (int i = 0; i < nlChildrenOfSurface.getLength(); i++) {
 			
 			Node n = nlChildrenOfSurface.item(i);
 			
 			if (n.getNodeType() == Node.TEXT_NODE) {
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"name=" + n.getNodeName()
 						+ "/"
 						+ "value=" + n.getNodeValue());
 
 			} else {//if (n.getNodeType() == Node.TEXT_NODE)
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]",
 						"name=" + n.getNodeName());
 				
 			}//if (n.getNodeType() == Node.TEXT_NODE)
 			
 			
 		}//for (int i = 0; i < nlChildrenOfSurface.getLength(); i++)
 		
 			//=> [150:doInBackground_B18_v_2_0_d_t_2](24081): name=#text/value=?
 
 
 		/*********************************
 		 * Try again
 		 *********************************/
 		Node nChildrenOfSurface = nlChildrenOfSurface.item(0);
 		
 		for (int i = 0; i < nlChildrenOfSurface.getLength(); i++) {
 			
 			Node n = nlChildrenOfSurface.item(i);
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "nlChildrenOfSurface.item(" + i + ")");
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]",
 					(i+1) + ": Name=" + n.getNodeName()
 					+ "/"
 					+ "Type=" + n.getNodeType()
 					+ "/"
 					+ "Value=" + n.getNodeValue()
 					);
 			
 				//=> [197:doInBackground_B18_v_2_0_d_t_2](24410): 1: Name=#text/Type=3/Value=?
 
 			
 			NodeList nl = n.getChildNodes();
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "n.getChildNodes().getLength()" + nl.getLength());
 			
 			Element elem = (Element) n;
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "elem...");
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]",
 					(i+1) + ": Name=" + elem.getNodeName()
 					+ "/"
 					+ "Type=" + elem.getNodeType()
 					+ "/"
 					+ "Value=" + elem.getNodeValue()
 					);
 			
 				//=> [197:doInBackground_B18_v_2_0_d_t_2](24410): 1: Name=#text/Type=3/Value=?
 			
 		}//for (int i = 0; i < nlChildrenOfSurface.getLength(); i++)
 		
 		
 //		NodeList nlChildrenOfSurface2 = nChildrenOfSurface.getChildNodes();
 		
 		
 		
 	}//private static void doInBackground_B18_v_2_0_d_t_2()
 
 	private static void doInBackground_B18_v_2_0_d_t_3() {
 		// TODO Auto-generated method stub
 		String kw = "����ԁi���j";
 		
 		String url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana" +
 				"?appid=dj0zaiZpPTZjQWNRNExhd0thayZkPVlXazlhR2gwTTJGUE56SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9Mjc-" +
 				"&grade=1" +
 				"&sentence=" + kw;
 
 		XmlHandler xh = new XmlHandler();
 		
 		Document doc = xh.getDoc(url);
 		
 		String tagName = "Word";
 		
 		NodeList nlWord = doc.getElementsByTagName(tagName);
 
 		/*********************************
 		 * Word node
 		 *********************************/
 		Node nWord1 = nlWord.item(0);
 		
 //		if (nWord1.getNodeType() == Node.ELEMENT_NODE) {
 //			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "nWord1 => Element node");
 //		} else {
 //			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "nNode => Not an element node");
 //			
 //			return;
 //		}
 //
 //			//=> [98:doInBackground_B18_v_2_0_d_t_2](23709): nWord1 => Element node
 		
 		/*********************************
 		 * Node list of nWord1
 		 *********************************/
 //		NodeList nlWord1Surface = ((Element) nWord1).getElementsByTagName("Surface");
 //		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]",
 //				"nlWord1Surface.getLength()=" + nlWord1Surface.getLength());
 //
 //			//=> [123:doInBackground_B18_v_2_0_d_t_2](23876): nlWord1Surface.getLength()=1
 
 		/*********************************
 		 * Surface
 		 *********************************/
 //		Node nWord1Surface = nlWord1Surface.item(0);
 		
 //		/*********************************
 //		 * Children of Surface
 //		 *********************************/
 //		NodeList nlChildrenOfSurface = nWord1Surface.getChildNodes();
 //		
 //		for (int i = 0; i < nlChildrenOfSurface.getLength(); i++) {
 //			
 //			Node n = nlChildrenOfSurface.item(i);
 //			
 //			if (n.getNodeType() == Node.TEXT_NODE) {
 //				
 //				// Log
 //				Log.d("Task_GetYomi.java"
 //						+ "["
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getMethodName() + "]",
 //						"name=" + n.getNodeName()
 //						+ "/"
 //						+ "value=" + n.getNodeValue());
 //
 //			} else {//if (n.getNodeType() == Node.TEXT_NODE)
 //				
 //				// Log
 //				Log.d("Task_GetYomi.java"
 //						+ "["
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getMethodName() + "]",
 //						"name=" + n.getNodeName());
 //				
 //			}//if (n.getNodeType() == Node.TEXT_NODE)
 //			
 //			
 //		}//for (int i = 0; i < nlChildrenOfSurface.getLength(); i++)
 //		
 //			//=> [150:doInBackground_B18_v_2_0_d_t_2](24081): name=#text/value=?
 
 
 //		/*********************************
 //		 * Try again
 //		 *********************************/
 //		Node nChildrenOfSurface = nlChildrenOfSurface.item(0);
 //		
 //		for (int i = 0; i < nlChildrenOfSurface.getLength(); i++) {
 //			
 //			Node n = nlChildrenOfSurface.item(i);
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "n=" + n.getClass().getName());
 //			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "nlChildrenOfSurface.item(" + i + ")");
 //			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]",
 //					(i+1) + ": Name=" + n.getNodeName()
 //					+ "/"
 //					+ "Type=" + n.getNodeType()
 //					+ "/"
 //					+ "Value=" + n.getNodeValue()
 //					);
 //			
 //				//=> [197:doInBackground_B18_v_2_0_d_t_2](24410): 1: Name=#text/Type=3/Value=?
 //
 //			
 //			NodeList nl = n.getChildNodes();
 //			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]", "n.getChildNodes().getLength()" + nl.getLength());
 //			
 //			if (n.getNodeType() == Node.ELEMENT_NODE) {
 //				
 //				Element elem = (Element) n;
 //				
 //				// Log
 //				Log.d("Task_GetYomi.java" + "["
 //						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //						+ "]",
 //						(i+1) + ": Name=" + n.getNodeName()
 //						+ "/"
 //						+ "Type=" + n.getNodeType()
 //						+ "/"
 //						+ "Value=" + n.getNodeValue()
 //						);
 //				
 //				
 //			} else {//if (n.getNodeType() == Node.ELEMENT_NODE)
 //
 //				// Log
 //				Log.d("Task_GetYomi.java"
 //						+ "["
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getMethodName() + "]",
 //						"Not an element node: " + n.getNodeType());
 //				
 //			}//if (n.getNodeType() == Node.ELEMENT_NODE)
 //			
 //				//=> [430:doInBackground_B18_v_2_0_d_t_3](1117): Not an element node: 3
 //			
 //
 //			
 ////			Element elem = (Element) n;
 ////			
 ////				//=> java.lang.ClassCastException: org.apache.harmony.xml.dom.TextImpl
 ////
 ////			
 ////			// Log
 ////			Log.d("Task_GetYomi.java" + "["
 ////					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 ////					+ ":"
 ////					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 ////					+ "]", "elem...");
 ////			// Log
 ////			Log.d("Task_GetYomi.java" + "["
 ////					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 ////					+ ":"
 ////					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 ////					+ "]",
 ////					(i+1) + ": Name=" + elem.getNodeName()
 ////					+ "/"
 ////					+ "Type=" + elem.getNodeType()
 ////					+ "/"
 ////					+ "Value=" + elem.getNodeValue()
 ////					);
 //			
 //				//=> [197:doInBackground_B18_v_2_0_d_t_2](24410): 1: Name=#text/Type=3/Value=?
 //			
 //		}//for (int i = 0; i < nlChildrenOfSurface.getLength(); i++)
 		
 		
 //		NodeList nlChildrenOfSurface2 = nChildrenOfSurface.getChildNodes();
 
 //		/*********************************
 //		 * Surface node
 //		 *********************************/
 //		if (nWord1Surface.getNodeType() == Node.ELEMENT_NODE) {
 //			
 //			// Log
 //			Log.d("Task_GetYomi.java"
 //					+ "["
 //					+ Thread.currentThread().getStackTrace()[2]
 //							.getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2]
 //							.getMethodName() + "]",
 //					"nWord1Surface => Element node");
 //			
 //		} else {//if (nWord1Surface.getNodeType() == Node.ELEMENT_NODE)
 //			
 //			// Log
 //			Log.d("Task_GetYomi.java"
 //					+ "["
 //					+ Thread.currentThread().getStackTrace()[2]
 //							.getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2]
 //							.getMethodName() + "]",
 //					"nWord1Surface => Not an element node");
 //			
 //		}//if (nWord1Surface.getNodeType() == Node.ELEMENT_NODE)
 //		
 //			//=> [449:doInBackground_B18_v_2_0_d_t_3](1634): nWord1Surface => Element node
 //		
 //		Element eSurface = (Element) nWord1Surface;
 //		
 //		NodeList nlChildrenOfSurface = eSurface.getChildNodes();
 //
 //		for (int i = 0; i < nlChildrenOfSurface.getLength(); i++) {
 //			
 //			Node n = nlChildrenOfSurface.item(0);
 //			
 //			// Log
 //			Log.d("Task_GetYomi.java" + "["
 //					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //					+ ":"
 //					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //					+ "]",
 //					(i+1) + ": Name=" + n.getNodeName()
 //					+ "/"
 //					+ "Type=" + n.getNodeType()
 //					+ "/"
 //					+ "Value=" + n.getNodeValue()
 //					);
 //			
 //		}//for (int i = 0; i < nlChildrenOfSurface.getLength(); i++)
 //
 //			//=> [515:doInBackground_B18_v_2_0_d_t_3](2417): 1: Name=#text/Type=3/Value=?
 
 		/*********************************
 		 * Node.getFirstChild()�@���g����@
 		 *********************************/
 		Node n1 = nWord1.getFirstChild();
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Node n1 = nWord1.getFirstChild()");
 
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				": Name=" + nWord1.getNodeName()
 				+ "/"
 				+ "Type=" + nWord1.getNodeType()
 				+ "/"
 				+ "Value=" + nWord1.getNodeValue());
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				": Name=" + n1.getNodeName()
 				+ "/"
 				+ "Type=" + n1.getNodeType()
 				+ "/"
 				+ "Value=" + n1.getNodeValue());
 		
 //			[537:doInBackground_B18_v_2_0_d_t_3](3414): Node n1 = nWord1.getFirstChild()
 //			[544:doInBackground_B18_v_2_0_d_t_3](3414): : Name=Word/Type=1/Value=null
 //			[556:doInBackground_B18_v_2_0_d_t_3](3414): : Name=#text/Type=3/Value=
 //			[556:doInBackground_B18_v_2_0_d_t_3](3414):         
 
 		
 		
 	}//private static void doInBackground_B18_v_2_0_d_t_3()
 
 	private static void doInBackground_B18_v_2_0_d_t_4() {
 		// TODO Auto-generated method stub
 		String kw = "����ԁi���j";
 		
 		String url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana" +
 				"?appid=dj0zaiZpPTZjQWNRNExhd0thayZkPVlXazlhR2gwTTJGUE56SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9Mjc-" +
 				"&grade=1" +
 				"&sentence=";
 
 //		DomSample ds = new DomSample(url, kw);
 		DomSample ds = DomSample.getDomSampleFromUri(url, kw);
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Starting... => walk");
 		
 		ds.walkThrough();
 		
 	}//private static void doInBackground_B18_v_2_0_d_t_3()
 
 	private static void doInBackground_B18_v_2_0_d_t_5() {
 		// TODO Auto-generated method stub
 		String kw = "����ԁi���j";
 		
 		String url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana" +
 				"?appid=dj0zaiZpPTZjQWNRNExhd0thayZkPVlXazlhR2gwTTJGUE56SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9Mjc-" +
 				"&grade=1" +
 				"&sentence=";
 
 //		DomSample ds = new DomSample(url, kw);
 		DomSample ds = DomSample.getDomSampleFromUri(url, kw);
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "Starting... => walk");
 		
 		ds.walkThrough();
 		
 	}//private static void doInBackground_B18_v_2_0_d_t_3()
 
 	private static void doInBackground_B18_v_2_0_d_t_6() {
 		// TODO Auto-generated method stub
 		String kw = "����ԁi���j";
 		
 		String url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana" +
 				"?appid=dj0zaiZpPTZjQWNRNExhd0thayZkPVlXazlhR2gwTTJGUE56SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9Mjc-" +
 				"&grade=1" +
 				"&sentence=" + kw;
 
 		XmlHandler xh = new XmlHandler();
 		
 		Document doc = xh.getDoc(url);
 		
 		Element root = doc.getDocumentElement();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "root.getNodeType()=" + root.getNodeType());
 		
 		/*********************************
 		 * Surfaces
 		 *********************************/
 		NodeList surfaces = root.getElementsByTagName("Surface");
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "surfaces.getLength()=" + surfaces.getLength());
 		
 //			[643:doInBackground_B18_v_2_0_d_t_6](17459): root.getNodeType()=1
 //			[652:doInBackground_B18_v_2_0_d_t_6](17459): surfaces.getLength()=6
 
 		Element el_Surface = (Element) surfaces.item(0);
 		
 		String s = el_Surface.getChildNodes().item(0).getNodeValue();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "s=" + s);
 
 		String s2 = el_Surface.getFirstChild().getNodeValue();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "s2=" + s2);
 		
 //			[669:doInBackground_B18_v_2_0_d_t_6](17706): s=?
 //			[678:doInBackground_B18_v_2_0_d_t_6](17706): s2=?
 
 		/*********************************
 		 * TRY: http://www.anddev.org/parse_xml_with_dom_-_getnodevalue_always_null-t3082.html
 		 * 	=> by ExxKA �� Sun Nov 08, 2009 5:05 pm
 		 *********************************/
 		Node childOfSurface = el_Surface.getFirstChild();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "childOfSurface.getNodeType()=" + childOfSurface.getNodeType());
 		
 //			=> [694:doInBackground_B18_v_2_0_d_t_6](17995): childOfSurface.getNodeType()=3
 		
 		String val = childOfSurface.getNodeValue();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "childOfSurface.getNodeValue()=" + val);
 		
 //			=> [705:doInBackground_B18_v_2_0_d_t_6](18118): childOfSurface.getNodeValue()=?
 
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "val.equals(\"\")=" + (val.equals(""))
 //				+ "/"
 //				+ "(val == null)=" + (val == null));
 		
 //			=> [714:doInBackground_B18_v_2_0_d_t_6](18244): val.equals("")=false/(val == null)=false
 
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "val.getClass().getName()=" + val.getClass().getName());
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "val.length()=" + val.length());
 
 	}//private static void doInBackground_B18_v_2_0_d_t_3()
 
 	private static void doInBackground_B18_v_2_0_d_t_7() {
 		// TODO Auto-generated method stub
 		String kw = "����ԁi���j";
 		
 		String url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana" +
 				"?appid=dj0zaiZpPTZjQWNRNExhd0thayZkPVlXazlhR2gwTTJGUE56SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9Mjc-" +
 				"&grade=1" +
 				"&sentence=" + kw;
 
 		XmlHandler xh = new XmlHandler();
 		
 		Document doc = xh.getDoc(url);
 		
 		Element root = doc.getDocumentElement();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "root.getNodeType()=" + root.getNodeType());
 		
 		/*********************************
 		 * Surfaces
 		 *********************************/
 		NodeList surfaces = root.getElementsByTagName("Surface");
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "surfaces.getLength()=" + surfaces.getLength());
 		
 //			[643:doInBackground_B18_v_2_0_d_t_6](17459): root.getNodeType()=1
 //			[652:doInBackground_B18_v_2_0_d_t_6](17459): surfaces.getLength()=6
 
 		Element el_Surface = (Element) surfaces.item(0);
 		
 		String s = el_Surface.getChildNodes().item(0).getNodeValue();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "s=" + s);
 
 		String s2 = el_Surface.getFirstChild().getNodeValue();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "s2=" + s2);
 		
 //			[669:doInBackground_B18_v_2_0_d_t_6](17706): s=?
 //			[678:doInBackground_B18_v_2_0_d_t_6](17706): s2=?
 
 		/*********************************
 		 * TRY: http://www.anddev.org/parse_xml_with_dom_-_getnodevalue_always_null-t3082.html
 		 * 	=> by ExxKA �� Sun Nov 08, 2009 5:05 pm
 		 *********************************/
 		Node childOfSurface = el_Surface.getFirstChild();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "childOfSurface.getNodeType()=" + childOfSurface.getNodeType());
 		
 //			=> [694:doInBackground_B18_v_2_0_d_t_6](17995): childOfSurface.getNodeType()=3
 		
 		String val = childOfSurface.getNodeValue();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "childOfSurface.getNodeValue()=" + val);
 		
 //			=> [705:doInBackground_B18_v_2_0_d_t_6](18118): childOfSurface.getNodeValue()=?
 
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "val.equals(\"\")=" + (val.equals(""))
 //				+ "/"
 //				+ "(val == null)=" + (val == null));
 		
 //			=> [714:doInBackground_B18_v_2_0_d_t_6](18244): val.equals("")=false/(val == null)=false
 
 		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "val.getClass().getName()=" + val.getClass().getName());
 //		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "val.length()=" + val.length());
 //
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]",
 //				"childOfSurface.getClass().getName()=" + childOfSurface.getClass().getName());
 		
 //			[846:doInBackground_B18_v_2_0_d_t_7](18504): val.length()=1
 //			[853:doInBackground_B18_v_2_0_d_t_7](18504):
 //					childOfSurface.getClass().getName()=org.apache.harmony.xml.dom.TextImpl
 
 		/*********************************
 		 * CharacterDataImpl
 		 *********************************/
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]",
 //				"childOfSurface.getClass().getSuperclass().getName()="
 //						+ childOfSurface.getClass().getSuperclass().getName());
 
 //				[868:doInBackground_B18_v_2_0_d_t_7](18770):
 //					childOfSurface.getClass().getSuperclass().getName()=
 //					org.apache.harmony.xml.dom.CharacterDataImpl
 
 		/*********************************
 		 * el_Surface
 		 *********************************/
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]",
 //				"el_Surface.getClass().getName()=" + el_Surface.getClass().getName());
 		
 //			=> [884:doInBackground_B18_v_2_0_d_t_7](19039):
 //					el_Surface.getClass().getName()=org.apache.harmony.xml.dom.ElementImpl
 
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]",
 //				"el_Surface.getClass().getSuperclass().getName()="
 //						+ el_Surface.getClass().getSuperclass().getName());
 		
 //			=> [895:doInBackground_B18_v_2_0_d_t_7](19166):
 //					el_Surface.getClass().getSuperclass().getName()=
 //						org.apache.harmony.xml.dom.InnerNodeImpl
 
 		
 		/*********************************
 		 * Root
 		 *********************************/
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "root.getClass().getName()=" + root.getClass().getName());
 		
 //			=> [912:doInBackground_B18_v_2_0_d_t_7](19500):
 //						root.getClass().getName()=
 //								org.apache.harmony.xml.dom.ElementImpl
 
 		/*********************************
 		 * InnerNodeImpl
 		 *********************************/
 //		Class c1 = root.getClass().getSuperclass();
 //		
 //		Class c2 = c1.getClass().getSuperclass();
 //		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "c2.getClass().getName()=" + c2.getClass().getName());
 		
 		s = root.getClass().getSuperclass().getSuperclass().getName();
 		
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]", "root.getClass().getSuperclass().getSuperclass().getName()=" + s);
 		
 //			=> [939:doInBackground_B18_v_2_0_d_t_7](19782):
 //					root.getClass().getSuperclass().getSuperclass().getName()=
 //						org.apache.harmony.xml.dom.LeafNodeImpl
 		
 		s = root.getClass()
 				.getSuperclass()
 				.getSuperclass()
 				.getSuperclass()
 				.getName();
 
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"root.getClass().getSuperclass().getSuperclass().getSuperclass().getName()" + s);
 
 //			=> [952:doInBackground_B18_v_2_0_d_t_7](19953):
 //					root.getClass().getSuperclass().getSuperclass().getSuperclass().getName()
 //						org.apache.harmony.xml.dom.NodeImpl
 
 //		s = root.getClass()
 //				.getSuperclass()
 //				.getSuperclass()
 //				.getSuperclass()
 //				.getSuperclass()
 //				.getSuperclass()
 //				.getName();
 //
 //		// Log
 //		Log.d("Task_GetYomi.java" + "["
 //				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 //				+ ":"
 //				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 //				+ "]",
 //				"root.getClass()" +
 //						".getSuperclass()" +
 //						".getSuperclass()" +
 //						".getSuperclass()" +
 //						".getSuperclass()" +
 //						".getSuperclass()" +
 //						".getName()=" + s);
 
 //			Caused by: java.lang.NullPointerException
 //			at sl.tasks.Task_GetYomi.doInBackground_B18_v_2_0_d_t_7(Task_GetYomi.java:972)
 
 		
 	}//private static void doInBackground_B18_v_2_0_d_t_3()
 
 	private static void doInBackground_B18_v_2_0_d() {
 		// TODO Auto-generated method stub
 		String kw = "����ԁi���j";
 		
 		String url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana" +
 				"?appid=dj0zaiZpPTZjQWNRNExhd0thayZkPVlXazlhR2gwTTJGUE56SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9Mjc-" +
 				"&grade=1" +
 				"&sentence=" + kw;
 
 		XmlHandler xh = new XmlHandler();
 		
 		Document doc = xh.getDoc(url);
 		
 		String tagName = "Word";
 		
 		NodeList listWord = doc.getElementsByTagName(tagName);
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "listWord.getLength()=" + listWord.getLength());
 		
 		Node nNode = listWord.item(0);
 		
 		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "nNode => Element node");
 		} else {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "nNode => Not an element node");
 		}
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "nNode.getNodeName()=" + nNode.getNodeName());
 		
 		NodeList nListWordChilds = nNode.getChildNodes();
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]", "nListWordChilds=" + nListWordChilds.getLength());
 		
 		for (int i = 0; i < nListWordChilds.getLength(); i++) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]",
 					"Child " + (i + 1) + "=" + nListWordChilds.item(i).getNodeName());
 			
 		}//for (int i = 0; i < nListWordChilds.getLength(); i++)
 		
 		// Log
 		Log.d("Task_GetYomi.java" + "["
 				+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 				+ ":"
 				+ Thread.currentThread().getStackTrace()[2].getMethodName()
 				+ "]",
 				"item(1)=" + nListWordChilds.item(1).getTextContent());
 		
 		
 //		for (int i = 0; i < listWord.getLength(); i++) {
 //
 //			Node nNode = listWord.item(i);
 //			
 //			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 //				
 //				Element elm = (Element) nNode;
 //				
 //				String strSurface = elm.getElementsByTagName("Surface").item(0).getTextContent();
 //				
 //				// Log
 //				Log.d("Task_GetYomi.java"
 //						+ "["
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getLineNumber()
 //						+ ":"
 //						+ Thread.currentThread().getStackTrace()[2]
 //								.getMethodName() + "]",
 //						"Surface no." + 1 + "=" + strSurface);
 //				
 ////				Node nChild = elm.getElementsByTagName("Surface").item(0);
 //				
 ////				if (nChild.getNodeType() == Node.ELEMENT_NODE) {
 ////					
 //////					Element elmSurface = (Element) nChild;
 //////					
 //////					String strSurface = elmSurface.getTextContent();
 ////					
 ////					String strSurface = nChild.getTextContent();
 ////					
 ////					// Log
 ////					Log.d("Task_GetYomi.java"
 ////							+ "["
 ////							+ Thread.currentThread().getStackTrace()[2]
 ////									.getLineNumber()
 ////							+ ":"
 ////							+ Thread.currentThread().getStackTrace()[2]
 ////									.getMethodName() + "]",
 ////							"Surface no." + 1 + "=" + strSurface);
 ////					
 ////				}//if (elmChild.getNodeType() == Node.ELEMENT_NODE)
 //				
 //			}//if (variable == condition)
 //			
 //		}//for (int i = 0; i < listWord.getLength(); i++)
 		
 	}//private static void doInBackground_B18_v_2_0_d()
 
 	private static int doInBackground__1() {
 		
 		DBUtils dbu = new DBUtils(actv, CONS.dbName);
 		
 		SQLiteDatabase wdb = dbu.getWritableDatabase();
 		
 		String tableName = "shopping_item";
 		
 		String sql = "SELECT * FROM " + tableName;
 
 		Cursor c = null;
 
 		try {
 			
 			c = wdb.rawQuery(sql, null);
 			
 			/*********************************
 			 * Cursor => null?
 			 *********************************/
 			if (null == c) {
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]", "Cursor => null");
 				
 				wdb.close();
 				
 				return CONS.GETYOMI_FAILED;
 				
 			}//if (null == c)
 			
 			/*********************************
 			 * Num of entries in the cursor => Less than 1?
 			 *********************************/
 			if (c.getCount() < 1) {
 				
 				// Log
 				Log.d("Task_GetYomi.java"
 						+ "["
 						+ Thread.currentThread().getStackTrace()[2]
 								.getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2]
 								.getMethodName() + "]", "Cursor => No entry");
 				
 				wdb.close();
 				
 				return CONS.GETYOMI_FAILED;
 				
 			}//if (null == c)
 			
 			/*********************************
 			 * Start
 			 *********************************/
 			c.moveToFirst();
 			
 			for (int i = 0; i < c.getCount(); i++) {
 
 				String yomi =
 						c.getString(1 + Methods.getArrayIndex(
 											CONS.columns,
 											"yomi"));
 	
 				String name =
 						c.getString(1 + Methods.getArrayIndex(
 											CONS.columns,
 											"name"));
 				
 				// Log
 				Log.d("Task_GetYomi.java" + "["
 						+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 						+ ":"
 						+ Thread.currentThread().getStackTrace()[2].getMethodName()
 						+ "]",
 						"name=" + name
 						+ "/"
 						+ "yomi=" + yomi);
 				
 				/*********************************
 				 * If "yomi" value is null,
 				 * 	=> Get yomi using the method: Methods.getYomi_full(String. String)
 				 *********************************/
 				if (yomi == null || yomi.equals("")) {
 					
 					yomi = Methods.getYomi_full(name, "utf-8")[2];
 					
 					// Log
 					Log.d("Task_GetYomi.java"
 							+ "["
 							+ Thread.currentThread().getStackTrace()[2]
 									.getLineNumber()
 							+ ":"
 							+ Thread.currentThread().getStackTrace()[2]
 									.getMethodName() + "]", "yomi=" + yomi);
 					
 				}//if (yomi == null || yomi.equals(""))
 				
 				/*********************************
 				 * Next
 				 *********************************/
 				c.moveToNext();
 
 			}//for (int i = 0; i < c.getCount(); i++)
 			
 		} catch (Exception e) {
 			
 			// Log
 			Log.d("Task_GetYomi.java"
 					+ "["
 					+ Thread.currentThread().getStackTrace()[2]
 							.getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2]
 							.getMethodName() + "]", e.toString());
 			
 			wdb.close();
 			
 			return CONS.GETYOMI_FAILED;
 			
 		}//try
 		
 		wdb.close();
 		
 		return CONS.GETYOMI_FAILED;
 		
 	}//private static int doInBackground__1()
 
 	private static int doInBackground__2() {
 		/*********************************
 		 * API-related processes
 		 *********************************/
 		String name = "����ԁi���j";
 
 		String yomi = Methods_sl.getYomi_full(name, "utf-8")[2];
 		
 		
 		
 		if (yomi != null) {
 			
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "yomi=" + yomi);
 			
 			return CONS.GETYOMI_SUCCESSFUL;
 			
 		} else {//if (yomi != null)
 		
 			// Log
 			Log.d("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "yomi == null (" + name + ")");
 			
 			return CONS.GETYOMI_FAILED;
 			
 		}//if (yomi != null)
 		
 	}//private static int doInBackground__1()
 
 
 	private static void doInBackground__2__1__XmlHandler() {
 		// TODO Auto-generated method stub
 		String kw = "����ԁi���j";
 		
 		String url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana" +
 				"?appid=dj0zaiZpPTZjQWNRNExhd0thayZkPVlXazlhR2gwTTJGUE56SW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9Mjc-" +
 				"&grade=1" +
 				"&sentence=" + kw;
 
 		HttpEntity entity = Methods.getYomi_getHttpEntity(url);
 		
 		if (entity == null) {
 			
 			// Log
 			Log.d("Methods_sl.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "entity == null");
 			
 			return;
 			
 		}//if (entity == null)
 
 		/*********************************
 		 * Get: XMLPullParser
 		 *********************************/
 		String xmlString =
 					Methods.convert_HttpEntity2XmlString(entity);
 		
 		if (xmlString == null) {
 			
 			// Log
 			Log.d("Methods_sl.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", "xmlString == null");
 			
 			return;
 			
 		}//if (entity == null)
 
 		/*********************************
 		 * XmlHandler
 		 *********************************/
 		XmlHandler xh = new XmlHandler();
 		
 		try {
 			
 //			xh.showXml(xmlString);
 			xh.showXml(url);
 			
 		} catch (SAXException e) {
 
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 		} catch (IOException e) {
 
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 		} catch (ParserConfigurationException e) {
 
 			// Log
 			Log.e("Task_GetYomi.java" + "["
 					+ Thread.currentThread().getStackTrace()[2].getLineNumber()
 					+ ":"
 					+ Thread.currentThread().getStackTrace()[2].getMethodName()
 					+ "]", e.toString());
 			
 		}//try
 
 	}//private static void doInBackground__2__1__XmlHandler()
 	
 
 	@Override
 	protected void onCancelled() {
 		// TODO Auto-generated method stub
 		super.onCancelled();
 	}
 
 	@Override
 	protected void onPostExecute(Integer res) {
 		// TODO Auto-generated method stub
 		super.onPostExecute(res);
 		
 		switch (res) {
 		
 		case CONS.GETYOMI_SUCCESSFUL:
 			
 			// debug
 			Toast.makeText(actv,
 					"Get yomi => Done", Toast.LENGTH_LONG).show();
 			
 			dlg.dismiss();
 			
 			break;
 			
 		case CONS.GETYOMI_NO_ENTRY:
 			
 			// debug
 			Toast.makeText(actv,
 					"Get yomi => No entry to process", Toast.LENGTH_LONG).show();
 			
 			dlg.dismiss();
 			
 			break;
 			
 		case CONS.GETYOMI_FAILED:
 
 			// debug
 			Toast.makeText(actv,
 					"Get yomi => Failed", Toast.LENGTH_LONG).show();
 
 			break;
 			
 		default:
 			break;
 		}//switch (res)
 		
 	}//protected void onPostExecute(Integer res)
 
 	@Override
 	protected void onPreExecute() {
 		// TODO Auto-generated method stub
 		super.onPreExecute();
 	}
 
 }//public class Task_GetYomi extends AsyncTask<String, Integer, Integer>

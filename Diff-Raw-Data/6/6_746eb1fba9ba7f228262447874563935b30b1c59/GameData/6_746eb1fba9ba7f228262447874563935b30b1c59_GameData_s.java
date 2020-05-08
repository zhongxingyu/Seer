 package client.data;
 
 
 import java.util.Vector;
 
 import net.rim.device.api.io.FileNotFoundException;
 import net.rim.device.api.util.Arrays;

import common.Utils;

 import data.Contestant;
import data.InvalidFieldException;
import data.me.json.JSONArray;
 import data.me.json.JSONException;
 import data.me.json.JSONObject;
 
 public class GameData extends data.GameData {
 
 	private Vector allList; // lits of
 	// all/remaining
 	// contestants
 	
 	public GameData(int numContestants) {
 		super(numContestants);
 		
 		allList = allContestants;
 	}
 	
 	private void updateSortAllContestants(int compFactID) {
 		allList = allContestants;
 		Vector t = noNullList(allList);
 		Object[] ar = new Object[t.size()];
 		t.copyInto(ar);
 		Arrays.sort(ar,ComparatorFactory.getComparator(compFactID));
 		
 		// t holds the sorted array, replace all the values with their
 		// new index. When the entry is null, it means we are done.
 		for (int i = 0; i <t.size();i++) {
 			allContestants.setElementAt((Contestant) ar[i],i);;
 		}
 
 		allList = allContestants;
 	}
 
 	private Vector noNullList(Vector c){
 		Vector retList=new Vector();
 		for(int i =0;i<c.size();i++){
 			if(c.elementAt(i)!=null)
 				retList.addElement(c.elementAt(i));
 		}
 		return retList;
 	}
 	
 	// extends the method in super class to sort it.
 	public void addContestant(Contestant c) {
 		super.addContestant(c);
 		updateSortAllContestants(ComparatorFactory.CONTNT_ID);
 	}
 
 	public void removeContestant(Contestant target) {
 		//if contestant was there, sort the array
 		if(allList.removeElement(target)){
 			updateSortAllContestants(ComparatorFactory.CONTNT_ID);
 		}
 	}
 	
 	/**
 	 * initGameData reads in a data file and builds a GameData object out
 	 * of it, returning it to the user.
 	 * 
 	 * @param inputFile   file to be read in
 	 * @return GameData object made out of file or null if season not created
 	 * 
 	 */
 	public static boolean initGameData() {
 		System.out.println("initGameData started");
 		JSONObject json;
 		try {
 			json = JSONUtils.readFile(JSONUtils.seasonFile);
 			if(json==null){
 				return false;
 			}
 		} catch (FileNotFoundException e) {
 			System.out.println("Failed to read file.");	
 			return false;
 		}
 		System.out.println("json read fine");
 		
 		try {
 			if (!json.getBoolean(KEY_SEASON_STARTED)){
 				ErrorText.displayErrorMsg("Season has not started.");
 				return false;
 			}
 			currentGame = new GameData(json.getInt(KEY_NUM_CONTEST));
 		} catch (JSONException e) {
 			System.out.println("json exception");
 			return false;
 		}
 		
 		System.out.println("creating gamedata from json");
 		
 		try {
 			GameData.getCurrentGame().fromJSONObject(json);
 		} catch (JSONException e) {
 			ErrorText.displayErrorMsg("Corrupted file.");
 			System.out.println("json exception");
 			return false;
 		}
 		
 		System.out.println("json read");
 		System.out.println("initGameData finished");
 		return true;
 	}
 
 	public void fromJSONObject(JSONObject obj) throws JSONException {
 		super.fromJSONObject(obj);
 		allList = allContestants;
 		
 	}
 	
 	// TODO: Implement:
 	
 	public String toString() {
 		return super.toString();
 	}
 	
 	/**
 	 * Used by SeasonCreate to create a new season.
 	 * @param num
 	 */
 	public static void initSeason(int num){
 		currentGame = new GameData(num);
 	}
 	
 	public void writeData() {
 		// TODO figure out a way to ouput data on BB
 		
 	}
 
 }

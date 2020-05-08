 package admin.data;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import admin.ComparatorFactory;
 import admin.Utils;
 import data.me.json.*;
 
 //import data.Contestant;
 import data.InvalidFieldException;
 
 public class GameData extends data.GameData {
 
 	private List<Contestant> allList; // lits of
 	// all/remaining
 	// contestants
 	
 	public GameData(int numContestants) {
 		super(numContestants);
 		
 		allList = Arrays.asList(allContestants);
 	}
 	
 	private void updateSortAllContestants(int compFactID) {
 		allList = Arrays.asList(allContestants);
 		List<Contestant> t = Utils.noNullList(allList);
 		Collections.sort(t, ComparatorFactory.getComparator(compFactID));
 		// t holds the sorted array, replace all the values with their
 		// new index. When the entry is null, it means we are done.
 		for (int i = 0; i < numContestants && allContestants[i] != null; i++) {
 			allContestants[i] = t.get(i);
 		}
 
 		allList = Arrays.asList(allContestants);
 	}
 
 	// extends the method in super class to sort it.
 	@Override
 	public void addContestant(Contestant c) {
 		super.addContestant(c);
 		
 		updateSortAllContestants(ComparatorFactory.CONTNT_ID);
 	}
 
 	// overridden to use binary search for speeeeed.
 	@Override
 	public void removeContestant(Contestant target) {
 		// is the contestant there?
 		int i = Utils.BinSearchSafe(allList, (Contestant)target,
 				ComparatorFactory.getComparator(ComparatorFactory.CONTNT_ID));
 		
 		if (i < 0) {
 			// i < 0 implies not found.
 			return;
 		}
 		
 		allList.remove(i);
 		updateSortAllContestants(ComparatorFactory.CONTNT_ID);
 	}
 	
 	@Override
 	protected int getContestantIndexID(String id) {
 		Contestant t = new Contestant();
 		try { 
 			t.setID(id);
 		} catch (InvalidFieldException e) 
 		{ 
 			System.out.println("getContestantIndexID:\t" + e.getMessage());
 			return -1;
 		}
 	
 		return Utils.BinSearchSafe(allList, t,
 				ComparatorFactory.getComparator(ComparatorFactory.CONTNT_ID));
 	}
 	
 	/**
 	 * intGameData reads in a data file and builds a GameData object out
 	 * of it, returning it to the user.
 	 * 
 	 * @param inputFile   file to be read in
 	 * @return GameData object made out of file or null if season not created
 	 * 
 	 */
 	public static GameData initGameData() {
 		JSONObject json;
 		try {
 			json = JSONUtils.readFile(JSONUtils.seasonFile);
 		} catch (FileNotFoundException e) {
 			return (GameData) currentGame; 
 		}
 		
 		try {
 			currentGame = new GameData(((Number)json.get(KEY_NUM_CONTEST)).intValue());
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			GameData.getCurrentGame().fromJSONObject(json);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return (GameData)currentGame;
 	}
 
 	
 	
 	// should be OK on bb:
 	@Override
 	public JSONObject toJSONObject() throws JSONException {
 		JSONObject obj = new JSONObject();
 		
 		obj.put(KEY_NUM_CONTEST, new Integer(numContestants));
 		JSONArray cons = new JSONArray();
 		for (Contestant c: allContestants) {
 			if (c != null)
 				cons.put(c.toJSONObject());
 		}
 		
 		JSONArray ts = new JSONArray();
 		// TODO: only two tribes?
 		ts.put(tribeNames[0]);
 		ts.put(tribeNames[1]);
 		
 		
 		obj.put(KEY_CONTESTANTS, cons);
 		obj.put(KEY_TRIBES, ts);
 		obj.put(KEY_WEEKS_REMAIN, new Integer(weeksRem));
 		obj.put(KEY_WEEKS_PASSED, new Integer(weeksPassed));
 		
 		return obj;
 	}
 	
 	@Override
 	public void fromJSONObject(JSONObject obj) throws JSONException {
 		numContestants = ((Number)obj.get(KEY_NUM_CONTEST)).intValue();
 				
 		// tribes
 		JSONArray ts = (JSONArray)obj.get(KEY_TRIBES);
 		this.setTribeNames((String)ts.get(0),  (String)ts.get(1) );
 		// week info:
 		weeksRem = ((Number)obj.get(KEY_WEEKS_REMAIN)).intValue();
 		weeksPassed = ((Number)obj.get(KEY_WEEKS_PASSED)).intValue();
 		
 		//Contestants must be loaded last!
 		allContestants = new Contestant[numContestants];
 		
 		// if we can move this back into the subclass, put this after a super.fromJSONObject() call.
 		allList = Arrays.asList(allContestants);
 		
 		// load the contestant array. 
 		JSONArray cons = (JSONArray)obj.get(KEY_CONTESTANTS);
 		for (int i =0;i<cons.length();i++) {
 			Contestant c = new Contestant();
 			c.fromJSONObject(cons.getJSONObject(i));
 			addContestant(c); // this would be valid in a subclass..
 		}
 	}
 	
 	/**
 	 * Used by SeasonCreate to create a new season.
 	 * @param num
 	 */
 	public static void initSeason(int num){
 		currentGame = new GameData(num);
 	}
 	
 	public static void main(String[] args) {
 		GameData g = new GameData(6);
 		
 		String[] tribes = new String[] {"banana", "apple"};
 		
 		g.setTribeNames(tribes[0], tribes[1]);
 		
 		Contestant c1 = null, c2 = null;
 		try {
			c1 = new Contestant("asd2", "Al", "Sd", tribes[1]);
 			c2 = new Contestant("as", "John", "Silver", tribes[0]);
 		} catch (InvalidFieldException e) {
 			// wont happen.
 		}
 		
 		g.addContestant(c1);
 		g.addContestant(c2);
 		
 		try {
 			System.out.println(g.toJSONString());
 		} catch (JSONException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		GameData g2 = new GameData(6);
 		
 			try {
 				g2.fromJSONString(g.toJSONString());
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 		
 	}
 	
 }

 package il.ac.huji.chores.dal;
 
 import android.util.Log;
 import com.google.common.collect.Collections2;
 import com.parse.*;
 
 import il.ac.huji.chores.ApartmentChore;
 import il.ac.huji.chores.Chore;
 import il.ac.huji.chores.Chore.CHORE_STATUS;
 import il.ac.huji.chores.ChoreInfo;
 import il.ac.huji.chores.ChoreInfo.CHORE_INFO_PERIOD;
 import il.ac.huji.chores.ChoreInfoInstance;
 import il.ac.huji.chores.Constants;
 import il.ac.huji.chores.exceptions.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class ChoreDAL {
 
 	public static String addChoreInfo(ChoreInfo choreInfo)
 			throws UserNotLoggedInException, ParseException {
 		String apartmentID = RoommateDAL.getApartmentID();
 		ParseObject chores = new ParseObject("ChoresInfo");
 		ParseACL permissions = new ParseACL();
 		permissions.setPublicWriteAccess(true);
 		permissions.setPublicReadAccess(true);
 		chores.setACL(permissions);
 		chores.put("apartment", apartmentID);
 		chores.put("choreName", choreInfo.getName());
 		chores.put("frequency", choreInfo.getHowManyInPeriod());
 		chores.put("coins", choreInfo.getCoinsNum());
 		chores.put("period", choreInfo.getPriod().toString());
 		chores.put("isEveryone", choreInfo.isEveryone());
 		chores.save();
 		return chores.getObjectId();
 	}
 
 	public static void updateAssignedTo(String choreID, String newOwner)
 			throws UserNotLoggedInException, ParseException {
 		String apartmentID = RoommateDAL.getApartmentID();
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Chores");
 		ParseObject choreObj;
 		choreObj = query.get(choreID);
 		choreObj.put("assignedTo", newOwner);
 		choreObj.save();
 
 	}
 
 	// updateAssignedTo + updateChoreStatus to done
 	public static void stealChore(String choreID, String newOwner)
 			throws UserNotLoggedInException, ParseException {
 
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Chores");
 		ParseObject choreObj;
 		choreObj = query.get(choreID);
 		choreObj.put("assignedTo", newOwner);
 		choreObj.put("status", CHORE_STATUS.STATUS_DONE.toString());
 		choreObj.save();
 	}
 
 	public static void updateChoreInfo(ChoreInfo choreInfo, String choreInfoId)
 			throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("ChoresInfo");
 		ParseObject updated = query.get(choreInfoId);
 		updated.put("coins", choreInfo.getCoinsNum());
 		updated.put("frequency", choreInfo.getHowManyInPeriod());
 		updated.put("period", choreInfo.getPriod().toString());
 		updated.put("choreName", choreInfo.getName());
 		updated.put("isEveryone", choreInfo.isEveryone());
 		updated.save();
 	}
 
 	public static void updateChoreInfoName(String choreID, String choreName)
 			throws UserNotLoggedInException, DataNotFoundException {
 		String apartmentID = RoommateDAL.getApartmentID();
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("ChoresInfo");
 		ParseObject choreInfoObj;
 		try {
 			choreInfoObj = query.get(choreID);
 
 			choreInfoObj.put("choreName", choreName);
 			choreInfoObj.save();
 		} catch (ParseException e) {
 			throw new DataNotFoundException("Chore info with id " + choreID
 					+ " wasn't found");
 		}
 	}
 
 	public static void updateFrequency(String choreID, int frequency)
 			throws UserNotLoggedInException, ParseException {
 		String apartmentID = RoommateDAL.getApartmentID();
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("ChoresInfo");
 		ParseObject choreInfoObj;
 		choreInfoObj = query.get(choreID);
 		choreInfoObj.put("frequency", frequency);
 		choreInfoObj.save();
 	}
 
 	public static void updatePeriod(String choreID, CHORE_INFO_PERIOD period)
 			throws UserNotLoggedInException, ParseException {
 		String apartmentID = RoommateDAL.getApartmentID();
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("ChoresInfo");
 		ParseObject choreInfoObj;
 		choreInfoObj = query.get(choreID);
 		choreInfoObj.put("period", period.toString());
 		choreInfoObj.save();
 
 	}
 
 	public static void updateCoins(String choreID, int coins)
 			throws UserNotLoggedInException, DataNotFoundException {
 		String apartmentID = RoommateDAL.getApartmentID();
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Chores");
 		ParseObject choreInfoObj;
 		try {
 			choreInfoObj = query.get(choreID);
 			choreInfoObj.put("coins", coins);
 			choreInfoObj.save();
 		} catch (ParseException e) {
 			throw new DataNotFoundException("Chore info with id " + choreID
 					+ " wasn't found");
 		}
 	}
 
 	public static ChoreInfo getChoreInfo(String choreInfoId)
 			throws UserNotLoggedInException, ParseException {
 
 		ParseObject choreInfoObj;
 		choreInfoObj = getChoreInfoObj(choreInfoId);
 		ChoreInfo choreInfo = convertParseObjectToChoreInfo(choreInfoObj);
 		return choreInfo;
 	}
 
 	public static ParseObject getChoreInfoObj(String choreInfoId)
 			throws UserNotLoggedInException, ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("ChoresInfo");
 		ParseObject choreInfoObj;
 		choreInfoObj = query.get(choreInfoId);
 		return choreInfoObj;
 
 	}
 
 	public static void updateChoreStatus(String choreId, CHORE_STATUS status) throws ParseException {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Chores");
 		ParseObject chore;
 		chore = query.get(choreId);
 		chore.put("status", status.toString());
 		chore.save();
 	}
 
 	public static Chore convertParseObjectToChore(ParseObject obj) {
 		Chore chore = new ApartmentChore();
 		chore.setId(obj.getObjectId());
 		chore.setAssignedTo(obj.getString("assignedTo"));
 		chore.setCoinsNum(obj.getInt("coins"));
 		chore.setDeadline(new Date(obj.getLong("deadline")));
 		chore.setName(obj.getString("name"));
 		chore.setStartsFrom(new Date(obj.getLong("startsFrom")));
 		String choreStatus = obj.getString("status");
 		chore.setStatus(CHORE_STATUS.valueOf(choreStatus));
 		String funFact = obj.getString("funFact");
 		chore.setFunFact(funFact);
 		chore.setApartment(obj.getString("apartment"));
 		return chore;
 	}
 
 	public static String addChore(Chore chore) throws ParseException,
 			UserNotLoggedInException {
 		String apartment = RoommateDAL.getApartmentID();
 		ParseObject choreObj = new ParseObject("Chores");
 		choreObj.put("assignedTo", chore.getAssignedTo());
 		choreObj.put("apartment", apartment);
 		choreObj.put("coins", chore.getCoinsNum());
 		choreObj.put("name", chore.getName());
 		choreObj.put("startsFrom", chore.getStartsFrom().getTime());
 		choreObj.put("deadline", chore.getDeadline().getTime());
 		choreObj.put("status", chore.getStatus().toString());
 		choreObj.put("choreInfoId", chore.getChoreInfoId());
 		choreObj.save();
 		return choreObj.getObjectId();
 	}
 
 	public static List<Chore> getRoommatesChores()
 			throws UserNotLoggedInException {
 		List<Chore> chores = new ArrayList<Chore>();
 		List<ParseObject> parseChores;
 		try {
 			String username = ParseUser.getCurrentUser().getUsername();
 			ParseQuery<ParseObject> query = ParseQuery.getQuery("Chores");
 			query.whereEqualTo("assignedTo", username);
 			parseChores = query.find();
 
 			for (ParseObject chore : parseChores) {
 				chores.add(convertParseObjectToChore(chore));
 			}
 
 			chores.addAll(getAllRoommatesChores(RoommateDAL.getApartmentID()));
 
 		} catch (ParseException e) {
 			throw new UserNotLoggedInException("User not logged in");
 		}
 
 		return chores;
 	}
 
 	private static List<Chore> getAllRoommatesChores(String apartmentId)
 			throws ParseException {
 		List<Chore> chores;
 		List<ParseObject> parseChores;
 		if (apartmentId == null) {
 			return Collections.emptyList();
 		}
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Chores");
 		query.whereEqualTo("assignedTo", Constants.CHORE_ASSIGNED_TO_EVERYONE);
 		query.whereEqualTo("apartment", apartmentId);
 		chores = new ArrayList<Chore>();
 		parseChores = query.find();
 
 		for (ParseObject chore : parseChores) {
 			chores.add(convertParseObjectToChore(chore));
 		}
 		return chores;
 
 	}
 
 	public static List<ChoreInfo> getAllChoreInfo() throws ParseException,
 			UserNotLoggedInException {
 		String apartmentID = RoommateDAL.getApartmentID();
 		if (apartmentID == null) {
 			return Collections.emptyList();
 		}
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("ChoresInfo");
 		query.whereEqualTo("apartment", apartmentID);
 		List<ParseObject> results = query.find();
 		List<ChoreInfo> choresInfoList = new ArrayList<ChoreInfo>();
 		ChoreInfo choreInfo;
 		for (ParseObject parseChore : results) {
 			choreInfo = convertParseObjectToChoreInfo(parseChore);
 			choresInfoList.add(choreInfo);
 		}
 		return choresInfoList;
 	}
 
 	public static Chore getChore(String choreID) {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Chores");
 		ParseObject chore;
 		try {
 			chore = query.get(choreID);
 		} catch (ParseException e) {
 			return null;
 		}
 		return convertParseObjectToChore(chore);
 
 	}
 
 	public static List<Chore> getUserAllOldChores()
 			throws UserNotLoggedInException, ParseException {
 		String roommateID = RoommateDAL.getUserID();
 		ParseQuery<ParseObject> queryDone = ParseQuery.getQuery("Chores");
 		// .whereEqualTo("assignedTo", roommateID)
 		queryDone.whereEqualTo("assignedTo", roommateID).whereEqualTo("status",
 				CHORE_STATUS.STATUS_DONE.toString());
 		ParseQuery<ParseObject> queryMissed = ParseQuery.getQuery("Chores");
 		queryMissed.whereEqualTo("assignedTo", roommateID).whereEqualTo(
 				"status", CHORE_STATUS.STATUS_MISS.toString());
 		List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
 		queries.add(queryDone);
 		queries.add(queryMissed);
 		ParseQuery<ParseObject> query = ParseQuery.or(queries);
 		List<ParseObject> results;
 		results = query.find();
 		List<Chore> choresList = new ArrayList<Chore>();
 		Chore chore;
 		for (ParseObject parseChore : results) {
 			chore = convertParseObjectToChore(parseChore);
 			choresList.add(chore);
 		}
 		return choresList;
 	}
 
 	/**
 	 * gets older chores. oldestChoreDisplayed is the parse id. if
 	 * oldestChoreDisplayed is null, there are no chores displayed at the moment
 	 * - return the first amount
 	 * 
 	 * @throws UserNotLoggedInException
 	 * @throws ParseException
 	 * @throws FailedToRetrieveOldChoresException
 	 */
 	public static List<Chore> getUserOldChores(String oldestChoreDisplayed,
 			int amount) throws UserNotLoggedInException, ParseException {
 		if (oldestChoreDisplayed == null)
			return null;
 		String roommateID = RoommateDAL.getUserID();
 		ParseQuery<ParseObject> queryDone = ParseQuery.getQuery("Chores");
 		queryDone.whereEqualTo("assignedTo", roommateID)
 				.whereEqualTo("name", oldestChoreDisplayed)
 				.whereEqualTo("status", CHORE_STATUS.STATUS_DONE.toString());
 		ParseQuery<ParseObject> queryMissed = ParseQuery.getQuery("Chores");
 		queryMissed.whereEqualTo("assignedTo", roommateID)
 				.whereEqualTo("name", oldestChoreDisplayed)
 				.whereEqualTo("status", CHORE_STATUS.STATUS_MISS.toString());
 		List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
 		queries.add(queryDone);
 		queries.add(queryMissed);
 		ParseQuery<ParseObject> query = ParseQuery.or(queries)
 				.orderByAscending("startsFrom");
 		List<ParseObject> results;
 		results = query.find();
		List<Chore> choresList = new ArrayList<Chore>();
 		Chore chore;
 		for (int i = 0; i < Math.min(results.size(), amount); i++) {
 			chore = convertParseObjectToChore(results.get(i));
 			choresList.add(chore);
 		}
 		return choresList;
 	}
 
 	/*
 	 * return all assigned (not done, deadline has not passed) chores. If there
 	 * are no chores, return an empty ArrayList.
 	 */
 	public static List<Chore> getAllChores() throws UserNotLoggedInException,
 			ParseException {
 		String apartmentID = RoommateDAL.getApartmentID();
 		if (apartmentID == null) {
 			Log.w("ChoreDAL.getAllChores",
 					"all chores was called with null apartment");
 			return Collections.emptyList();
 		}
 		ParseQuery<ParseObject> queryfuture = ParseQuery.getQuery("Chores");
 		queryfuture.whereEqualTo("apartment", apartmentID).whereEqualTo(
 				"status", CHORE_STATUS.STATUS_FUTURE.toString());
 
 		List<ParseObject> results;
 		results = queryfuture.find();
 		List<Chore> choresList = new ArrayList<Chore>();
 		Chore chore;
 		for (ParseObject parseChore : results) {
 			chore = convertParseObjectToChore(parseChore);
 			if (chore.getAssignedTo() == null) {
 				Log.w("ChoreDAL.getAllChores",
 						"found a chore with a null owner in apartment "
 								+ apartmentID);
 				continue;
 			}
 			choresList.add(chore);
 		}
 		return choresList;
 	}
 
 	private static ChoreInfo convertParseObjectToChoreInfo(
 			ParseObject choreInfoObj) {
 		ChoreInfo choreInfo = new ChoreInfoInstance();
 		choreInfo.setChoreInfoID(choreInfoObj.getObjectId());
 		choreInfo.setChoreInfoName(choreInfoObj.getString("choreName"));
 		choreInfo.setCoins(choreInfoObj.getInt("coins"));
 		choreInfo.setHowMany(choreInfoObj.getInt("frequency"));
 		choreInfo.setPeriod(choreInfoObj.getString("period"));
 		choreInfo.setIsEveryone(choreInfoObj.getBoolean("isEveryone"));
 		return choreInfo;
 
 	}
 
 	/**
 	 * Gets all chores created after createTime. createTime in milis.
 	 * 
 	 * @throws UserNotLoggedInException
 	 * @throws ParseException
 	 **/
 	public static List<Chore> getAllChoresCreatedAfter(long createTime)
 			throws UserNotLoggedInException, ParseException {
 		String username = RoommateDAL.getRoomateUsername();
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Chores");
 		query.whereEqualTo("assignedTo", username).whereGreaterThan(
 				"startsFrom", createTime);
 		List<Chore> chores = new ArrayList<Chore>();
 		List<ParseObject> results = query.find();
 		for (ParseObject result : results) {
 			Chore chore = convertParseObjectToChore(result);
 			chores.add(chore);
 		}
 		return chores;
 	}
 
 }

 package cpsc310.server;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import cpsc310.client.HouseDataService;
 import cpsc310.client.HouseData;
 
 /**
  * Server side methods to fetch house data
  */
 public class HouseDataServiceImpl extends RemoteServiceServlet implements
 		HouseDataService {
 
 	private DataStore store;
 	private List<String> workingIDStore;
 
 	/**
 	 * Constructor
 	 */
 	public HouseDataServiceImpl() {
 		store = new DataStore();
 		refreshIDStore();
 	}
 
 	/**
 	 * Refreshes the working set of IDs
 	 */
 	public void refreshIDStore() {
 		workingIDStore = store.getAllKeys();
 	}
 
 	/**
 	 * Get house data for initial drawing of table. Returning list must be
 	 * ArrayList because of Google RPC's Serialization policy.
 	 * 
 	 * @param start
 	 * @param range
 	 * @return list of HouseData within specified range
 	 */
 	@Override
 	public List<HouseData> getHouses(int start, int range) {
 		// retrieve house data points
 		List<HouseDataPoint> tempList = store.getHouses(workingIDStore, start,
 				range);
 
 		// Convert HouseDataPoint into HouseData
 		if (tempList == null) {
 			tempList = new ArrayList<HouseDataPoint>();
 		}
 		List<HouseData> grab = convertToListHouseData(tempList);
 
 		// Change below to return grab
 		return grab;
 	}
 
 	@Override
 	public void searchHouses(String[] userSearchInput, int isSelling) {
 		// [0]"civicNumber"
 		// [1]"StreetName",
 		// Value "Current Land Value" - [2]min, [3]max
 		// Value "Price" - [4]min, [5]max
 		// [6]"Realtor"
 		// [7]"Postal Code"
 		// Value "Current Improvement Value" - [8]min, [9]max
 		// Year "Assessment Year" - [10]min, [11]max
 		// Value "Previous Land Value" - [12]min, [13]max
 		// Value "Previous Improvement Value" - [14]min, [15]max
 		// Year "Year Built" - [16]min, [17]max
 		// Year "Big Improvement Year" - [18]min, [19]max
 
 		// reduction factors (avg case) = houseID > Street > Civic Number >
 		// Postal Code > Realtor > Price > Is Selling > Values > Years > Not
 		// Selling
 		
 		System.out.println("Normal Search");
 
 		Set<String> results = store.getAllKeysSet();
 
 		// First pass checks in order of reduction factors
 
 		results = firstPassSearch(userSearchInput, results);
 
 		if (isSelling == 1) {
 			results.retainAll(store.getForSaleHomes());
 		} else if (isSelling == 0) {
 			results.removeAll(store.getForSaleHomes());
 		}
 		// TODO: Start performing single value evaluation increase speed?
 
 		// Second Pass, requires tree traversal
 		results = secondPassSearch(userSearchInput, results);
 
 		// convert results
 		ArrayList<String> convertedResults = new ArrayList<String>();
 		convertedResults.addAll(results);
 		workingIDStore = convertedResults;
 	}
 
 	/**
 	 * Helper to table drawing to figure out how many rows need to exist.
 	 * 
 	 * @return size of database
 	 */
 	@Override
 	public int getHouseDatabaseLength() {
 		int databaseLength = workingIDStore.size();
 		return databaseLength;
 	}
 
 	/**
 	 * Helper to convert HouseDataPoint into HouseDataPoint (data transfer
 	 * object).
 	 * 
 	 * @param houseList
 	 *            - HouseDataPoint to convert into HouseData
 	 * @return the converted HouseDataPoint (returned as a HouseData object)
 	 */
 	private List<HouseData> convertToListHouseData(
 			List<HouseDataPoint> houseList) {
 		List<HouseData> converted = new ArrayList<HouseData>();
 		Iterator<HouseDataPoint> tempItr = houseList.iterator();
 
 		// Convert HouseDataPoint into HouseData
 		while (tempItr.hasNext()) {
 			converted.add(convertToHouseData(tempItr.next()));
 		}
 		return converted;
 	}
 
 	/**
 	 * Helper to convert HouseDataPoint into HouseDataPoint (data transfer
 	 * object).
 	 * 
 	 * @param house
 	 *            - HouseDataPoint to convert into HouseData
 	 * @return the converted HouseDataPoint (returned as a HouseData object)
 	 */
 	private HouseData convertToHouseData(HouseDataPoint house) {
 		HouseData converted = new HouseData();
 
 		converted.setHouseID(house.getHouseID());
 		converted.setCivicNumber(house.getCivicNumber());
 		converted.setStreetName(house.getStreetName());
 		converted.setPostalCode(house.getPostalCode());
 		converted.setCurrentLandValue(house.getCurrentLandValue());
 		converted
 				.setCurrentImprovementValue(house.getCurrentImprovementValue());
 		converted.setAssessmentYear(house.getAssessmentYear());
 		converted.setPreviousLandValue(house.getPreviousLandValue());
 		converted.setPreviousImporvementValue(house
 				.getPreviousImprovementValue());
 		converted.setYearBuilt(house.getYearBuilt());
 		converted.setBigImprovementYear(house.getBigImprovementYear());
 		converted.setOwner(house.getOwner());
 		converted.setIsSelling(house.getIsSelling());
 		converted.setPrice(house.getPrice());
 
 		return converted;
 	}
 
 	@Override
 	public List<HouseData> getHouses(List<String> list, int start, int range) {
 		// retrieve house data points
 		List<HouseDataPoint> tempList = store.getHouses(list, start, range);
 
 		// Convert HouseDataPoint into HouseData
 		List<HouseData> grab = convertToListHouseData(tempList);
 
 		// Change below to return grab
 		return grab;
 	}
 
 	@Override
 	public void sortByAddress(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByHouseID(workingIDStore);
 		} else {
 			workingIDStore = store.sortByHouseIDDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByPostalCode(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByPostalCode(workingIDStore);
 		} else {
 			workingIDStore = store.sortByPostalCodeDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByOwner(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByOwner(workingIDStore);
 		} else {
 			workingIDStore = store.sortByOwnerDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByForSale(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByForSale(workingIDStore);
 		} else {
 			workingIDStore = store.sortByForSaleDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByCurrentLandValue(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByCurrentLandValue(workingIDStore);
 		} else {
 			workingIDStore = store.sortByCurrentLandValueDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByCurrentImprovementValue(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store
 					.sortByCurrentImprovementValue(workingIDStore);
 		} else {
 			workingIDStore = store
 					.sortByCurrentImprovementValueDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByAssessmentYear(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByAssessmentYear(workingIDStore);
 		} else {
 			workingIDStore = store.sortByAssessmentYearDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByPreviousLandValue(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByPreviousLandValue(workingIDStore);
 		} else {
 			workingIDStore = store.sortByPreviousLandValueDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByPreviousImprovementValue(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store
 					.sortByPreviousImprovementValue(workingIDStore);
 		} else {
 			workingIDStore = store
 					.sortByPreviousImprovementValueDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByYearBuilt(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByYearBuilt(workingIDStore);
 		} else {
 			workingIDStore = store.sortByYearBuiltDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByBigImprovementYear(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByBigImprovementYear(workingIDStore);
 		} else {
 			workingIDStore = store.sortByBigImprovementYearDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void sortByPrice(boolean isSortAscending) {
 		if (isSortAscending) {
 			workingIDStore = store.sortByPrice(workingIDStore);
 		} else {
 			workingIDStore = store.sortByPriceDes(workingIDStore);
 		}
 	}
 
 	@Override
 	public void updateHouse(String Owner, int price, boolean isSelling,
 			String houseID, double latitude, double longitude, String postalCode) {
 		store.updateHouse(Owner, price, isSelling, houseID, longitude,
 				latitude, postalCode);
 	}
 
 	@Override
 	public List<String> getStreetNames() {
 		return store.getStreets();
 	}
 
 	@Override
 	public void resetHouse(String houseID) {
 		store.resetHouse(houseID);
 	}
 
 	@Override
 	public HouseData retrieveSingleHouse(int civicNumber, String street) {
 		workingIDStore = new ArrayList<String>();
 		workingIDStore.addAll(store.searchByAddress(civicNumber, street));
 		HouseDataPoint currentHDP = store.getHouses(workingIDStore, 0, 1)
 				.get(0);
 		return convertToHouseData(currentHDP);
 	}
 
 	@Override
 	public void searchHousesForSalePolygon(String[] userSearchInput,
 			double[] latitude, double[] longitude) {
 		System.out.println("Polygon Search");
 		
 		Set<String> results = store.searchForSaleInPolygon(latitude, longitude);
		firstPassSearch(userSearchInput, results);
		secondPassSearch(userSearchInput, results);
 		
 		// convert to array
 		ArrayList<String> convertedResults = new ArrayList<String>();
 		convertedResults.addAll(results);
 		workingIDStore = convertedResults;
 	}
 
 	/**
 	 * Intermediate search using all O(1) search functions
 	 * 
 	 * @param results
 	 * @return results - pruned partial results
 	 */
 	private Set<String> firstPassSearch(String[] userSearchInput,
 			Set<String> results) {
 		if (!userSearchInput[0].equals("") && !userSearchInput[1].equals("")) {
 			results = store.searchByAddress(
 					Integer.parseInt(userSearchInput[0]), userSearchInput[1]);
 		} else if (!userSearchInput[1].equals("")) {
 			results = store.searchByStreet(userSearchInput[1]);
 		} else if (!userSearchInput[0].equals("")) {
 			results = store.searchByCivicNumber(Integer
 					.parseInt(userSearchInput[0]));
 		}
 		if (!userSearchInput[7].equals("")) {
 			results.retainAll(store.searchByPostalCode(userSearchInput[7]));
 		}
 		if (!userSearchInput[6].equals("")) {
 			results.retainAll(store.searchByOwner(userSearchInput[6]));
 		}
 		return results;
 	}
 
 	/**
 	 * Intermediate search using all O(log(n)) search functions
 	 * 
 	 * @param results
 	 * @return results - pruned partial results
 	 */
 	private Set<String> secondPassSearch(String[] userSearchInput,
 			Set<String> results) {
 		if (!userSearchInput[4].equals("") && !userSearchInput[5].equals("")) {
 			results.retainAll(store.searchByPrice(
 					Integer.parseInt(userSearchInput[4]),
 					Integer.parseInt(userSearchInput[5])));
 		}
 		if (!userSearchInput[2].equals("") && !userSearchInput[3].equals("")) {
 			results.retainAll(store.searchByCurrentLandValue(
 					Integer.parseInt(userSearchInput[2]),
 					Integer.parseInt(userSearchInput[3])));
 		}
 		if (!userSearchInput[8].equals("") && !userSearchInput[9].equals("")) {
 			results.retainAll(store.searchByCurrentImprovementValue(
 					Integer.parseInt(userSearchInput[8]),
 					Integer.parseInt(userSearchInput[9])));
 		}
 		if (!userSearchInput[12].equals("") && !userSearchInput[13].equals("")) {
 			results.retainAll(store.searchByPreviousLandValue(
 					Integer.parseInt(userSearchInput[12]),
 					Integer.parseInt(userSearchInput[13])));
 		}
 		if (!userSearchInput[14].equals("") && !userSearchInput[15].equals("")) {
 			results.retainAll(store.searchByPreviousImprovementValue(
 					Integer.parseInt(userSearchInput[14]),
 					Integer.parseInt(userSearchInput[15])));
 		}
 		return results;
 	}
 
 	@Override
 	public void getHomesByUser(String email) {
 		workingIDStore = new ArrayList<String>();
 		workingIDStore.addAll(store.searchByOwner(email));
 	}
 }

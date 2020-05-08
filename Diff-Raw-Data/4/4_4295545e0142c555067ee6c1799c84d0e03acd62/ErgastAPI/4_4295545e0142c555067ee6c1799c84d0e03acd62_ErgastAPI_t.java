 /*
  The MIT License (MIT)
 
 Copyright (c) 2013 Felipe Herranz<felhr85@gmail.com>
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
  */
 package com.felipeDev.ErgastApi;
 
 import com.felipeDev.ErgastObjects.*;
 import com.felipeDev.simpleJson.*;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This facade-style class provides easy methods to get information of Ergast Developer API
  * More information about Ergast API <a href="http://ergast.com/mrd/"> here </a> 
  * @author Felipe Herranz (felhr85@gmail.com)
  *
  */
 public class ErgastAPI 
 {
 	/** Limit of results returned in array fields (SeasonTable.Seasons, DriverTable.Drivers...) */
 	private int limit;
 	private int offset;
 	/** Maximum number of elements available */
 	private int total;
 	
 	//----TOKENS
 	public final static String CIRCUITS_TOKEN = "circuits";
 	public final static String CONSTRUCTORS_TOKEN = "constructors";
 	public final static String DRIVERS_TOKEN = "drivers";
 	public final static String GRID_TOKEN = "grid";
 	public final static String RESULTS_TOKEN = "results";
 	public final static String FASTESTS_TOKEN = "fastests";
 	public final static String STATUS_TOKEN = "status";
 	public final static String CONSTRUCTOR_STANDINGS_TOKEN = "constructorStandings";
 	public final static String DRIVERS_STANDINGS_TOKEN = "driversStandings";
 	public final static String PITSTOPS_TOKEN = "pitstops";
 	public final static String CURRENT_TOKEN = "current";
 	public final static String LAST_TOKEN = "last";
 	public final static String YEAR = "year";
 	public final static String ROUND = "round";
 	public final static String LAPS = "laps";
 
 	private QueryValues queryValues;
 	
 	public ErgastAPI()
 	{
 		this.limit = 30;
 		this.offset = 0;
 		this.queryValues = new QueryValues();
 	}
 	// Getters and setters
 	
 	public int getLimit() 
 	{
 		return limit;
 	}
 
 	public void setLimit(int limit) 
 	{
 		this.limit = limit;
 	}
 
 	public int getOffset() 
 	{
 		return offset;
 	}
 
 	public void setOffset(int offset) 
 	{
 		this.offset = offset;
 	}
 
 	public int getTotal() 
 	{
 		return total;
 	}
 	
 	public void setTotal(int total)
 	{
 		this.total = total;
 	}
 	// Methods to set query parameters
 	public void putValue(String key, String value)
 	{
 		queryValues.putString(key, value);
 	}
 	
 	public String getValue(String key)
 	{
 		return queryValues.getString(key);
 	}
 	
 	public void resetQuery()
 	{
 		queryValues.resetQuery();
 	}
 	
 	/**
 	 * 
 	 * @return A list of selected seasons
 	 */
 	public List<Season> getSeasons()
 	{
 		String terminationFile = "seasons.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<Season> seasons = JsonHandler.getSeasons(jsonResponse);
 			return seasons;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	public List<Race> getQualifyingOfRace()
 	{
 		String terminationFile = "qualifying.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<Race> races = JsonHandler.getQualifyingResults(jsonResponse);
 			return races;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	public List<Qualifying> getQualifyingOfRace2()
 	{
 		List<Race> races = getQualifyingOfRace();
 		if(races != null)
 		{
 			int sizeRaces = races.size();
 			List<Qualifying> qualy = new ArrayList<Qualifying>(sizeRaces);
 			for(int i=0;i <= sizeRaces -1;i++)
 			{
 				List<Position> positions = races.get(i).getQualifyingResults();
 				qualy.add(new Qualifying(positions));
 			}
 			return qualy;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public List<Constructor> getConstructorsInfo()
 	{
 		String terminationFile = "constructors.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<Constructor> constructors = JsonHandler.getConstructorInformation(jsonResponse);
 			return constructors;
 			
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public Race getLapTimes()
 	{
 		String terminationFile = "laps.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			Race race = JsonHandler.getLaps(jsonResponse);
 			return race;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public List<Lap> getLapTimes2()
 	{
 		Race race = getLapTimes();
 		if(race != null)
 		{
 			return race.getLaps();
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public List<StandingList> getDriverStandings()
 	{
 		String terminationFile = "driverStandings.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<StandingList> standings = JsonHandler.getStandings(jsonResponse);
 			return standings;
 		}else
 		{
 			return null;
 		}
 		
 	}
 	
 	
 	public List<StandingList> getDriverStandings(int rank)
 	{
 		String terminationFile =  "driverStandings/" + String.valueOf(rank) + ".json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<StandingList> standings = JsonHandler.getStandings(jsonResponse);
 			return standings;
 		}else
 		{
 			return null;
 		}
 		
 	}
 	
 	
 	public List<StandingList> getConstructorStandings()
 	{
 		String terminationFile = "constructorStandings.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<StandingList> standings = JsonHandler.getStandings(jsonResponse);
 			return standings;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public List<StandingList> getConstructorStandings(int rank)
 	{
 		String terminationFile = "constructorStandings/" + String.valueOf(rank) + ".json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<StandingList> standings = JsonHandler.getStandings(jsonResponse);
 			return standings;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public List<Circuit> getCircuitInfo()
 	{
 		String terminationFile = "circuits.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<Circuit> circuits = JsonHandler.getCircuits(jsonResponse);
 			return circuits;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	public Race getPitStopsInfo()
 	{
 		String terminationFile = "pitstops.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			Race race = JsonHandler.getPitStops(jsonResponse);
 			return race;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public Race getPitStopsInfo(int pitStopNumber)
 	{
 		String terminationFile = "pitstops/" + String.valueOf(pitStopNumber) + ".json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			Race race = JsonHandler.getPitStops(jsonResponse);
 			return race;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public List<PitStop> getPitStopsInfo2()
 	{
 		Race race = getPitStopsInfo();
 		if(race != null)
 		{
 			return race.getPitStops();
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public List<PitStop> getPitStopsInfo2(int pitStopNumber)
 	{
 		Race race = getPitStopsInfo(pitStopNumber);
 		if(race != null)
 		{
 			return race.getPitStops();
 		}else
 		{
 			return null;
 		}
 	}
 	
 	public List<Race> getResults()
 	{
 		String terminationFile = "results.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<Race> races = JsonHandler.getResults(jsonResponse);
 			return races;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	public List<Driver> getDriversInfo()
 	{
 		String terminationFile = "drivers.json";
 		String jsonResponse = getResponseFromAPI(terminationFile);
 		if(jsonResponse != null)
 		{
 			List<Driver> drivers = JsonHandler.getDriverInformation(jsonResponse);
 			return drivers;
 		}else
 		{
 			return null;
 		}
 	}
 	
 	
 	// ===========================================================
 	// Private methods
 	// ===========================================================
 	
 	private String getQueryString()
 	{
 		List<String> keys = new ArrayList<String>();
 		List<String> values = new ArrayList<String>();
 		String query = "";
 		String year = getValue(YEAR);
 		String round = getValue(ROUND);
 		// If year or round is set. Special treatment is needed.
 		if(year != null)
 		{
 			query = query + year + "/";
 		}
 		
 		if(round != null)
 		{
 			query = query + round + "/";
 		}
 		Iterator<String> e,t;
 		queryValues.getParameters(keys, values);
 		e = keys.iterator();
 		t = values.iterator();
 		
 		while(e.hasNext())
 		{
 			String key = e.next();
 			String value = t.next();
 			if(key.equals(YEAR) == false && key.equals(ROUND) == false)
 			{
 				query = query + key + "/" + value + "/";
 			}
 		}
 		return query;
 	}
 	
 	private String getResponseFromAPI(String terminationFile)
 	{
 		String limitString = "?limit=" + String.valueOf(limit);
 		String offsetString = "&offset=" + String.valueOf(offset);
 		
 		if(queryValues.isQuery()) // There is at least one parameter
 		{
 			String query = getQueryString();
 			String finalString = query + terminationFile + limitString + offsetString;
 			try 
 			{
 				String jsonResponse = APIConnection.getResponse(finalString);
 				return jsonResponse;
 			} catch (Exception e1) 
 			{
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 				return null;
 			}
 		}else // There is no parameters. So It performs the basic query. It lists all values
 		{
 			try 
 			{
 				String finalString = terminationFile + limitString + offsetString;
 				String jsonResponse = APIConnection.getResponse(finalString);
 				return jsonResponse;
 			} catch (Exception e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 		}
 	}
 
 	/**
 	 * A class to handle connection with Ergast API. This nested class provides upper class with
 	 * a response in json format
 	 * @author Felipe Herranz (felhr85@gmail.com)
 	 *
 	 */
 	private static class APIConnection
 	{
 		private final static String ERGAST_URI="http://ergast.com/api/f1/";
 		
 		private APIConnection()
 		{
 			
 		}
 		
 		/**
 		 * 
 		 * @param A coherent String with the API guidelines, for example:
 		 * "2010/status" or "current/constructorStandings"
 		 * @return A string with the response, in json format
 		 * @throws Exception, specifically IOException,MalformedURLException or a generic
 		 * exception due to a HTTP Code != 200
 		 */
 		public static String getResponse(String query) throws Exception
 		{
 			StringBuilder totalString = new StringBuilder();
 			String uriUrl = ERGAST_URI + query;
 			URL url = new URL(uriUrl);
 			URLConnection newConnection = url.openConnection();
 			HttpURLConnection httpConnection = (HttpURLConnection) newConnection;
 			int responseCode = httpConnection.getResponseCode();
 			if(responseCode == HttpURLConnection.HTTP_OK)
 			{
 				InputStream dataFlow = httpConnection.getInputStream();
 				BufferedReader br = new BufferedReader(new InputStreamReader(dataFlow,"UTF-8"));
 				String line;
 				while((line = br.readLine()) != null)
 				{
 					totalString.append(line);
 				}	
 			}else
 			{
 				Exception e = new Exception("HTTP Code Error: " + String.valueOf(responseCode) + " URL: " + uriUrl);
 				e.printStackTrace();
 				throw e;
 			}
 			return totalString.toString();
 		}
 		
 		
 		
 	}
 	
 	/**
 	 * A class to deserialize json data to objects of the package com.felipeDev.ErgastObjects
 	 * @author Felipe Herranz (felhr85@gmail.com)
 	 *
 	 */
 	private static class JsonHandler
 	{
 		private final static String MR_DATA = "MRData";
 		private final static String TOTAL = "total";
 		private final static String SEASON_TABLE = "SeasonTable";
 		private final static String SEASONS = "Seasons";
 		private final static String SEASON = "season";
 		private final static String URL = "url";
 		private final static String RACE_TABLE = "RaceTable";
 		private final static String RACES = "Races";
 		private final static String ROUND = "round";
 		private final static String RACE_NAME = "raceName";
 		private final static String CIRCUIT = "Circuit";
 		private final static String CIRCUIT_ID = "circuitId";
 		private final static String CIRCUIT_NAME = "circuitName";
 		private final static String LOCATION = "Location";
 		private final static String LAT = "lat";
 		private final static String LONG = "long";
 		private final static String LOCALITY = "locality";
 		private final static String COUNTRY = "country";
 		private final static String DATE = "date";
 		private final static String TIME = "time";
 		private final static String QUALIFYING_RESULTS = "QualifyingResults";
 		private final static String NUMBER = "number";
 		private final static String POSITION = "position";
 		private final static String DRIVER = "Driver";
 		private final static String CONSTRUCTOR = "Constructor";
 		private final static String Q1 = "Q1";
 		private final static String Q2 = "Q2";
 		private final static String Q3 = "Q3";
 		private final static String DRIVER_ID = "driverId";
 		private final static String GIVEN_NAME = "givenName";
 		private final static String FAMILY_NAME = "familyName";
 		private final static String DATE_OF_BIRTH = "dateOfBirth";
 		private final static String NATIONALITY = "nationality";
 		private final static String CONSTRUCTOR_ID = "constructorId";
 		private final static String NAME = "name";
 		private final static String CONSTRUCTOR_TABLE = "ConstructorTable";
 		private final static String CONSTRUCTORS = "Constructors";
 		private final static String TIMINGS = "Timings";
 		private final static String LAPS = "Laps";
 		private final static String STANDINGS_TABLE = "StandingsTable";
 		private final static String STANDINGS_LIST = "StandingsLists";
 		private final static String DRIVER_STANDINGS = "DriverStandings";
 		private final static String CONSTRUCTOR_STANDINGS = "ConstructorStandings";
 		private final static String POINTS = "points";
 		private final static String WINS = "wins";
 		private final static String CIRCUIT_TABLE = "CircuitTable";
 		private final static String CIRCUITS = "Circuits";
 		private final static String LAP = "lap";
 		private final static String STOP = "stop";
 		private final static String DURATION = "duration";
 		private final static String PIT_STOPS = "PitStops";
 		private final static String MILLIS = "millis";
 		private final static String RANK = "rank";
 		private final static String UNITS = "units";
 		private final static String SPEED = "speed";
 		private final static String POSITION_TEXT = "positionText";
 		private final static String GRID = "grid";
 		private final static String STATUS = "status";
 		private final static String FASTEST_LAP = "FastestLap";
 		private final static String AVERAGE_SPEED = "AverageSpeed";
 		private final static String TIME_CAPS = "Time";
 		private final static String RESULTS = "Results";
 		private final static String DRIVER_TABLE = "DriverTable";
 		private final static String DRIVERS = "Drivers";
 		
 		
 		private JsonHandler()
 		{
 			
 		}
 		
 		// ===========================================================
 		// Season methods
 		// ===========================================================
 		/**
 		 * A json response from API to a List of Season objects
 		 * @param A string with a json response from API
 		 * @return A list of Season objects
 		 */
 		public static List<Season> getSeasons(String jsonResponse)
 		{
 			List<Season> seasonList;
 			JSONObject o = getJsonObject(jsonResponse);
 			JSONObject mrData = getJsonObject(o,MR_DATA);
 			JSONObject seasonTable = getJsonObject(mrData,SEASON_TABLE);
 			JSONArray seasons = getJsonArray(seasonTable,SEASONS);
 			int lengthSize = seasons.size();
 			if(lengthSize > 0) // if It is a well constructed query but There is not a valid result. Returns null;
 			{
 				seasonList = new ArrayList<Season>(lengthSize);
 				for(int i = 0;i <= lengthSize -1;i++)
 				{
 					JSONObject element = getJsonObject(seasons,i);
 					int year = Integer.parseInt((String) element.get(SEASON));
 					String url = (String) element.get(URL);
 					seasonList.add(new Season(year,url));
 				}
 				return seasonList;
 			}else
 			{
 				return seasonList = new ArrayList<Season>();
 			}
 		}
 		
 		/**
 		 * APi returns data of qualifying results embedded in a Race object, in a
 		 * field called "qualifyingResults". This method returns the Race object. If you
 		 * want only Results. Use getQualifyingResults2
 		 * @param jsonResponse
 		 * @return Race object
 		 */
 		public static List<Race> getQualifyingResults(String jsonResponse)
 		{
 			JSONObject o = getJsonObject(jsonResponse);
 			JSONObject mrData = getJsonObject(o,MR_DATA);
 			JSONObject raceTable = getJsonObject(mrData,RACE_TABLE);
 			JSONArray races = getJsonArray(raceTable,RACES);
 			int sizeRaces = races.size();
 			List<Race> raceList = new ArrayList<Race>(sizeRaces);
 			if(sizeRaces > 0)
 			{
 				for(int i = 0;i<= sizeRaces -1;i++)
 				{
 					JSONObject item = getJsonObject(races,i);
 					Race race = getRaceObject(item);
 					JSONArray qualifying = getJsonArray(item,QUALIFYING_RESULTS);
 					List<Position> qResults = getQualifyingResultsList(qualifying);
 					race.setQualifyingResults(qResults);
 					raceList.add(race);
 				}
 				return raceList;
 			}else
 			{
 				return new ArrayList<Race>();
 			}
 			
 		}
 		
 		/**
 		 * This methods returns information about constructors.
 		 * @param jsonResponse
 		 * @return List of constructor objects
 		 */
 		public static List<Constructor> getConstructorInformation(String jsonResponse)
 		{
 			JSONObject o = getJsonObject(jsonResponse);
 			JSONObject mrData = getJsonObject(o,MR_DATA);
 			JSONObject constructorTable = getJsonObject(mrData,CONSTRUCTOR_TABLE);
 			JSONArray constructorsArray = getJsonArray(constructorTable,CONSTRUCTORS);
 			if(constructorsArray.size() > 0)
 			{
 				int length = constructorsArray.size();
 				List<Constructor> list = new ArrayList<Constructor>(length);
 				for(int i=0;i<= length -1;i++)
 				{
 					list.add(getConstructorObject(getJsonObject(constructorsArray,i)));
 				}
 				return list;
 			}else
 			{
 				List<Constructor> list = new ArrayList<Constructor>();
 				return list;
 			}
 			
 			
 		}
 		/**
 		 * This method returns information about laps of a single race. Lap information
 		 * appears embedded in a Race object.
 		 * @param jsonResponse
 		 * @return
 		 */
 		public static Race getLaps(String jsonResponse)
 		{
 			JSONObject o = getJsonObject(jsonResponse);
 			JSONObject mrData = getJsonObject(o,MR_DATA);
 			JSONObject raceTable = getJsonObject(mrData,RACE_TABLE);
 			JSONArray races = getJsonArray(raceTable,RACES);
 			if(races.size() > 0)
 			{
 				JSONObject singleRace = getJsonObject(races,0);
 				Race race = getRaceObject(singleRace);
 				JSONArray arrayLaps = getJsonArray(singleRace,LAPS);
 				race.setLaps(getLapList(arrayLaps));
 				return race;
 			}else
 			{
 				return null;
 			}
 			
 		}
 		
 		public static List<StandingList> getStandings(String jsonResponse)
 		{
 			List<StandingList> list;
 			JSONObject o = getJsonObject(jsonResponse);
 			JSONObject mrData = getJsonObject(o,MR_DATA);
 			JSONObject standingsTable = getJsonObject(mrData,STANDINGS_TABLE);
 			JSONArray standingsList = getJsonArray(standingsTable,STANDINGS_LIST);
 			int sizeStandings = standingsList.size();
 			list = new ArrayList<StandingList>(sizeStandings);
 			if(sizeStandings > 0)
 			{
 				for(int i = 0;i<= sizeStandings-1;i++)
 				{
 					JSONObject item = getJsonObject(standingsList,i);
 					list.add(getStandingListObject(item));
 				}
 				return list;
 			}else
 			{
 				list = new ArrayList<StandingList>();
 				return list;
 			}
 		}
 		
 		public static List<Circuit> getCircuits(String jsonResponse)
 		{
 			List<Circuit> list;
 			JSONObject o = getJsonObject(jsonResponse);
 			JSONObject mrData = getJsonObject(o,MR_DATA);
 			JSONObject circuitTable = getJsonObject(mrData,CIRCUIT_TABLE);
 			JSONArray circuits = getJsonArray(circuitTable,CIRCUITS);
 			int lengthCircuits = circuits.size();
 			list = new ArrayList<Circuit>(lengthCircuits);
 			if(lengthCircuits > 0)
 			{
 				for(int i = 0;i <= lengthCircuits -1;i++)
 				{
 					JSONObject item = getJsonObject(circuits,i);
 					list.add(getCircuitObject(item));
 				}
 				return list;
 			}else
 			{
 				return list = new ArrayList<Circuit>();
 			}
 		}
 		
 		
 		public static Race getPitStops(String jsonResponse)
 		{
 			List<PitStop> list;
 			JSONObject o = getJsonObject(jsonResponse);
 			JSONObject mrData = getJsonObject(o,MR_DATA);
 			JSONObject raceTable = getJsonObject(mrData,RACE_TABLE);
 			JSONArray raceArray = getJsonArray(raceTable,RACES);
 			JSONObject raceElement = getJsonObject(raceArray,0); // It will be always an array with a single object. I swear it!!!
 			Race race = getRaceObject(raceElement);
 			JSONArray pitStopsArray = getJsonArray(raceElement,PIT_STOPS);
 			int lengthPitStops = pitStopsArray.size();
 			list = new ArrayList<PitStop>(lengthPitStops);
 			if(lengthPitStops > 0)
 			{
 				for(int i = 0;i <= lengthPitStops -1;i++)
 				{
 					JSONObject item = getJsonObject(pitStopsArray,i);
 					list.add(getPitStopObject(item));
 				}
 				race.setPitStops(list);
 				return race;
 			}else
 			{
 				list = new ArrayList<PitStop>();
 				race.setPitStops(list);
 				return race;
 			}
 		}
 		
 		
 		public static List<Race> getResults(String jsonResponse)
 		{
 			List<Race> list;
 			JSONObject o = getJsonObject(jsonResponse);
 			JSONObject mrData = getJsonObject(o,MR_DATA);
 			JSONObject raceTable = getJsonObject(mrData,RACE_TABLE);
 			JSONArray races = getJsonArray(raceTable,RACES);
 			int lengthRaces = races.size();
 			list = new ArrayList<Race>(lengthRaces);
 			if(lengthRaces > 0)
 			{
 				for(int i = 0;i <= lengthRaces -1;i++)
 				{
 					JSONObject item = getJsonObject(races,i);
 					Race race = getRaceObject(item);
 					JSONArray results = getJsonArray(item,RESULTS);
 					List<Result> resultsList = getResults(results);
 					race.setResults(resultsList);
 					list.add(race);
 				}
 				return list;
 			}else
 			{
 				list = new ArrayList<Race>();
 				return list;
 			}
 		}
 		
 		
 		public static List<Driver> getDriverInformation(String jsonResponse)
 		{
 			JSONObject o = getJsonObject(jsonResponse);
 			JSONObject mrData = getJsonObject(o,MR_DATA);
 			JSONObject driverTable = getJsonObject(mrData,DRIVER_TABLE);
 			JSONArray driversArray = getJsonArray(driverTable,DRIVERS);
 			if(driversArray.size() > 0)
 			{
 				int length = driversArray.size();
 				List<Driver> list = new ArrayList<Driver>(length);
 				for(int i=0;i<= length -1;i++)
 				{
 					list.add(getDriverObject(getJsonObject(driversArray,i)));
 				}
 				return list;
 			}else
 			{
 				List<Driver> list = new ArrayList<Driver>();
 				return list;
 			}
 		}
 		
 		// ===========================================================
 		// Private methods
 		// ===========================================================
 		
 		private static JSONObject getJsonObject(String jsonResponse)
 		{
 			JSONObject o = (JSONObject) JSONValue.parse(jsonResponse);
 			return o;
 		}
 		
 		private static JSONObject getJsonObject(JSONObject o,String field)
 		{
 			JSONObject obj = (JSONObject) o.get(field);
 			return obj;
 		}
 		
 		private static JSONObject getJsonObject(JSONArray array,int index)
 		{
 			JSONObject obj = (JSONObject) array.get(index);
 			return obj;
 		}
 		
 		private static JSONArray getJsonArray(JSONObject o,String field)
 		{
 			JSONArray array = (JSONArray) o.get(field);
 			return array;
 		}
 		
 		private static Race getRaceObject(JSONObject o)
 		{
 				
 			int season = Integer.parseInt((String) o.get(SEASON));
 			int round = Integer.parseInt((String) o.get(ROUND));
 			String url = (String) o.get(URL);
 			String raceName = (String) o.get(RACE_NAME);
 			String date = (String) o.get(DATE);
 			String time = (String) o.get(TIME);
 				
 			JSONObject circuitObj = getJsonObject(o,CIRCUIT);
 			Circuit circ = getCircuitObject(circuitObj);
 			Race raceObj = new Race(season,round,url,raceName,circ,date,time);
 			return raceObj;
 		}
 		
 		private static List<Position> getQualifyingResultsList(JSONArray o)
 		{
 			int lengthArray = o.size();
 			List<Position> qualifyingR = new ArrayList<Position>(lengthArray);
 			for(int i = 0;i<= lengthArray -1;i++)
 			{
 				JSONObject positionObj = getJsonObject(o,i);
 				int number = Integer.parseInt((String) positionObj.get(NUMBER));
 				int pos = Integer.parseInt((String) positionObj.get(POSITION));
 				Driver driver = getDriverObject((JSONObject) positionObj.get(DRIVER));
 				Constructor constructor = getConstructorObject((JSONObject) positionObj.get(CONSTRUCTOR));
 				String q1 = (String) positionObj.get(Q1);
 				String q2 = (String) positionObj.get(Q2);
 				String q3 = (String) positionObj.get(Q3);
 				qualifyingR.add(new Position(number,pos,driver,constructor,q1,q2,q3));
 			}
 			return qualifyingR;
 		}
 		
 		private static Driver getDriverObject(JSONObject driverObj)
 		{
 			String driverId = (String) driverObj.get(DRIVER_ID);
 			String url = (String) driverObj.get(URL);
 			String givenName = (String) driverObj.get(GIVEN_NAME);
 			String familyName = (String) driverObj.get(FAMILY_NAME);
 			String dateOfBirth = (String) driverObj.get(DATE_OF_BIRTH);
 			String nationality = (String) driverObj.get(NATIONALITY);
 			return new Driver(driverId,url,givenName,familyName,dateOfBirth,nationality);
 		}
 		
 		private static Constructor getConstructorObject(JSONObject constructorObj)
 		{
 			String constructorId = (String) constructorObj.get(CONSTRUCTOR_ID);
 			String url = (String) constructorObj.get(URL);
 			String name = (String) constructorObj.get(NAME);
 			String nationality = (String) constructorObj.get(NATIONALITY);
 			return new Constructor(constructorId,url,name,nationality);
 		}
 		
 		private static List<Lap> getLapList(JSONArray arrayLap)
 		{
 			int lengthArray = arrayLap.size();
 			List<Lap> laps = new ArrayList<Lap>(lengthArray);
 			List<Timing> timingsList;
 			for(int i = 0;i<=lengthArray -1;i++)
 			{
 				JSONObject lap = getJsonObject(arrayLap,i);
 				int number = Integer.parseInt((String) lap.get(NUMBER));
 				JSONArray timings = getJsonArray(lap,TIMINGS);
 				int lengthTimings = timings.size();
 				timingsList = new ArrayList<Timing>(lengthTimings);
 				for(int j = 0;j<=lengthTimings -1;j++)
 				{
 					timingsList.add(getTimingObject(getJsonObject(timings,j)));
 				}
 				laps.add(new Lap(number,timingsList));
 			}
 			return laps;
 		}
 		
 		
 		private static Timing getTimingObject(JSONObject timings)
 		{
 			String driverId = (String) timings.get(DRIVER_ID);
 			int position = Integer.parseInt((String) timings.get(POSITION));
 			String time = (String) timings.get(TIME);
 			return new Timing(driverId,position,time);
 		}
 		
 		
 		private static StandingList getStandingListObject(JSONObject standingList)
 		{
 			List<Standing> list;
 			int season = Integer.parseInt((String) standingList.get(SEASON));
 			int round = Integer.parseInt((String) standingList.get(ROUND));
 			if(getJsonArray(standingList,DRIVER_STANDINGS) != null)
 			{
 				JSONArray standings = getJsonArray(standingList,DRIVER_STANDINGS);
 				int standingsSize = standings.size();
 				list = new ArrayList<Standing>(standingsSize);
 				for(int i = 0;i <= standingsSize - 1;i++)
 				{
 					JSONObject item = getJsonObject(standings, i);
 					Standing standing = getStandingObject(item);
 					list.add(standing);
 				}
 				
 			}else
 			{
 				JSONArray standings = getJsonArray(standingList,CONSTRUCTOR_STANDINGS);
 				int standingsSize = standings.size();
 				list = new ArrayList<Standing>(standingsSize);
 				for(int i = 0;i <= standingsSize - 1;i++)
 				{
 					JSONObject item = getJsonObject(standings, i);
 					Standing standing = getStandingObject(item);
 					list.add(standing);
 				}
 				
 			}
 			return new StandingList(season,round,list);
 			
 		}
 		
 		
 		
 		private static Standing getStandingObject(JSONObject standing)
 		{
 			int position = Integer.parseInt((String) standing.get(POSITION));
 			int points = Integer.parseInt((String) standing.get(POINTS));
 			int wins = Integer.parseInt((String) standing.get(WINS));
 			if(getJsonObject(standing,DRIVER) != null)
 			{
 				Driver driver = getDriverObject(getJsonObject(standing,DRIVER));
 				Constructor constructor = getConstructorObject(getJsonObject(standing,CONSTRUCTOR));
 				Standing driverStanding = new DriverStanding(position,points,wins,driver,constructor);
 				return driverStanding;
 			}else
 			{
 				Constructor constructor = getConstructorObject(getJsonObject(standing,CONSTRUCTOR));
 				Standing constructorStanding = new ConstructorStanding(position,points,wins,constructor);
 				return constructorStanding;
 			}
 			
 		}
 		
 		
 		private static Circuit getCircuitObject(JSONObject circuitObj)
 		{
 			String circuitId = (String) circuitObj.get(CIRCUIT_ID);
 			String url = (String) circuitObj.get(URL);
 			String circuitName = (String) circuitObj.get(CIRCUIT_NAME);
 			JSONObject locObj = getJsonObject(circuitObj,LOCATION);
 			Location loc = getLocationObject(locObj);
 			return new Circuit(circuitId,url,circuitName,loc);
 		}
 		
 		
 		private static Location getLocationObject(JSONObject locationObj)
 		{
 			float lat = Float.valueOf((String) locationObj.get(LAT));
 			float longitude = Float.valueOf((String) locationObj.get(LONG));
 			String locality = (String) locationObj.get(LOCALITY);
 			String country = (String) locationObj.get(COUNTRY);
 			Location loc = new Location(lat,longitude,locality,country);
 			return loc;
 		}
 		
 		
 		private static PitStop getPitStopObject(JSONObject pitStopObj)
 		{
 			String driverId = (String) pitStopObj.get(DRIVER_ID);
			int lap = Integer.parseInt((String) pitStopObj.get(LAP));
			int stop = Integer.parseInt((String) pitStopObj.get(STOP));
 			String time = (String) pitStopObj.get(TIME);
 			String duration = (String) pitStopObj.get(DURATION);
 			return new PitStop(driverId,lap,stop,time,duration);
 		}
 		
 		
 		private static Time getTimeObject(JSONObject timeObj)
 		{
 			float millis = Float.parseFloat((String) timeObj.get(MILLIS));
 			String time = (String) timeObj.get(TIME);
 			return new Time(millis,time);
 		}
 		
 		
 		private static FastestLap getFastestLapObject(JSONObject fastestObj)
 		{
 			int rank = Integer.parseInt((String) fastestObj.get(RANK));
 			int lap = Integer.parseInt((String) fastestObj.get(LAP));
 			Time time = getTimeObject(getJsonObject(fastestObj,TIME));
 			return new FastestLap(rank,lap,time);
 		}
 		
 		
 		private static AverageSpeed getAverageSpeedObject(JSONObject averageObj)
 		{
 			String units = (String) averageObj.get(UNITS);
 			float speed = Float.parseFloat((String) averageObj.get(SPEED));
 			return new AverageSpeed(units,speed);
 		}
 		
 		private static Result getResultObject(JSONObject resultObj)
 		{
 			int number = Integer.parseInt((String) resultObj.get(NUMBER));
 			int position = Integer.parseInt((String) resultObj.get(POSITION));
 			String positionText = (String) resultObj.get(POSITION_TEXT);
 			int points = Integer.parseInt((String) resultObj.get(POINTS));
 			Driver driver = getDriverObject((JSONObject) resultObj.get(DRIVER));
 			Constructor constructor = getConstructorObject((JSONObject) resultObj.get(CONSTRUCTOR));
 			int grid = Integer.parseInt((String) resultObj.get(GRID));
 			int lap = Integer.parseInt((String) resultObj.get(LAP));
 			String status = (String) resultObj.get(STATUS);
 			Time time = getTimeObject((JSONObject) resultObj.get(TIME_CAPS));
 			FastestLap fastestLap = getFastestLapObject((JSONObject) resultObj.get(FASTEST_LAP));
 			AverageSpeed averageSpeed = getAverageSpeedObject((JSONObject) resultObj.get(AVERAGE_SPEED));
 			return new Result(number,position,positionText,points,driver,constructor,grid,
 					lap,status,time,fastestLap,averageSpeed);
 		}
 		
 		
 		private static List<Result> getResults(JSONArray results)
 		{
 			int lengthResults = results.size();
 			List<Result> list = new ArrayList<Result>(lengthResults);
 			if(lengthResults > 0)
 			{
 				for(int i = 0;i <= lengthResults -1;i++)
 				{
 					JSONObject item = getJsonObject(results,i);
 					list.add(getResultObject(item));
 				}
 				return list;
 			}else
 			{
 				return list;
 			}
 		}
 	}
 	/**
 	 * This class handles all query values
 	 * @author Felipe Herranz (felhr85@gmail.com)
 	 *
 	 */
 	private class QueryValues
 	{
 		private HashMap<String,String> queryValues;
 		private int numberOfParameters = 9;
 		private boolean isQuery;
 		
 		public QueryValues()
 		{
 			queryValues = new HashMap<String,String>(numberOfParameters);
 			putValuesToNull();
 			isQuery = false;
 		}
 
 		public boolean isQuery() 
 		{
 			return isQuery;
 		}
 		
 		public void putString(String key,String value)
 		{
 			queryValues.put(key, value);
 			if(isQuery == false)
 			{
 				isQuery = true;
 			}
 			
 		}
 		
 		public String getString(String key)
 		{
 			return queryValues.get(key);
 		}
 		
 		public void resetQuery()
 		{
 			putValuesToNull();
 			isQuery = false;
 		}
 		
 		public void getParameters(List<String> keys, List<String> values)
 		{
 			for(Map.Entry<String, String> entry : queryValues.entrySet() )
 			{
 				String key = entry.getKey();
 				String value = entry.getValue();
 				if(value != null)
 				{
 					keys.add(key);
 					values.add(value);
 				}
 			}
 			resetQuery();
 		}
 		
 		private void putValuesToNull()
 		{
 			queryValues.put(CIRCUITS_TOKEN, null);
 			queryValues.put(CONSTRUCTORS_TOKEN, null);
 			queryValues.put(DRIVERS_TOKEN, null);
 			queryValues.put(GRID_TOKEN, null);
 			queryValues.put(RESULTS_TOKEN, null);
 			queryValues.put(FASTESTS_TOKEN, null);
 			queryValues.put(STATUS_TOKEN, null);
 			queryValues.put(YEAR, null);
 			queryValues.put(ROUND, null);
 			queryValues.put(LAPS, null);
 		}
 		
 			
 		
 	}
 
 }

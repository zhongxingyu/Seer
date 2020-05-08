 /**
  * @contributor(s): Christian Skjetne (NTNU), Jacqueline Floch (SINTEF), Rune Sætre (NTNU)
  * @version: 		0.1
  * @date:			23 May 2011
  * @revised:
  *
  * Copyright (C) 2011 UbiCompForAll Consortium (SINTEF, NTNU)
  * for the UbiCompForAll project
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  * 
  */
 
 /**
  * @description:
  *
  * 
  */
 
 package org.ubicompforall.CityExplorer.data;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import android.content.Context;
 import android.widget.Toast;
 
 public class DatabaseUpdater
 {
 
 	/** The url address. */
 	private String urlAddress = "http://godz.serveftp.org/";//"http://idi.ntnu.no/~chriskje/";
 	
 	/** The update server url. */
 	private URL updateServerURL;
 	
 	/** The context. */
 	private Context context;
 	
 	/**
 	 * Instantiates a new database updater.
 	 *
 	 * @param c the c
 	 */
 	public DatabaseUpdater(Context c)
 	{
 		context = c;
 	}
 	
 	/**
 	 * Downloads, adds and updates pois from a repository.
 	 * 
 	 * @return int[0] number of pois added. int[1] number of pois updated.
 	 */
 	public int[] doInternetUpdatePois()
 	{
 		int[] res = new int[]{0,0};
 		
 		String[] poidata;
 		
		try
		{
 			updateServerURL = new URL(urlAddress+"pois.ce");
 			
			InputStream is = updateServerURL.openStream();
 			BufferedReader in = new BufferedReader(new InputStreamReader(is));
 			
 			String line;
 			
 			while ((line = in.readLine()) != null)
 			{
 				if(line.startsWith("global_id"))continue;
 				line.replace("%EOL", "\n");
 				line = line+"%EOL";//hack to make the split behave correctly
 				poidata = line.split(";");
 				poidata[12] = poidata[12].replace("%EOL", "");
 				res[handlePoiData(poidata)]++;
 			}
 			in.close();
 		} 
 		catch (MalformedURLException e) {Toast.makeText(context, "Error updating MalformedURLException", Toast.LENGTH_LONG).show();
 		} 
 		catch (IOException e) {Toast.makeText(context, "Error updating IOException "+e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 		
 		return res;
 	}
 	
 	public ArrayList<Poi> getInternetPois()
 	{
 		ArrayList<Poi> res = new ArrayList<Poi>();
 		
 		String[] poidata;
 		
 		try
 		{
 			updateServerURL = new URL(urlAddress+"pois.ce");
 			
 			InputStream is = updateServerURL.openStream();
 			BufferedReader in = new BufferedReader(new InputStreamReader(is));
 			
 			String line;
 			
 			while ((line = in.readLine()) != null)
 			{
 				if(line.startsWith("global_id"))continue;
 				line.replace("%EOL", "\n");
 				line = line+"%EOL";//hack to make the split behave correctly
 				poidata = line.split(";");
 				poidata[12] = poidata[12].replace("%EOL", "");
 				res.add(getPoiFromPoiData(poidata));
 			}
 			in.close();
 		} 
 		catch (MalformedURLException e) {Toast.makeText(context, "Error updating MalformedURLException", Toast.LENGTH_LONG).show();
 		} 
 		catch (IOException e) {Toast.makeText(context, "Error updating IOException "+e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * adds and updates pois from a text.
 	 *
 	 * @param filetext the filetext
 	 * @return int[0] number of pois added. int[1] number of pois updated.
 	 */
 	public int[] doFileUpdatePois(String filetext)
 	{
 		int[] res = new int[]{0,0};
 		
 		String[] poidata = new String[/*13*/ 12]; // ZIP code removed => 12
 
 		String[] lines = filetext.split("\n");
 
 		for (String line : lines)
 		{
 			
 			if(line.startsWith("global_id"))continue;
 			line.replace("%EOL", "\n");
 			line = line+"%EOL";//hack to make the split behave correctly
 			poidata = line.split(";");
 			poidata[/*12*/ 11] = poidata[/*12*/ 11].replace("%EOL", "");
 			res[handlePoiData(poidata)]++;
 		}
 
 		return res;
 	}
 	
 	private Poi getPoiFromPoiData(String[] poidata)
 	{
 		/*
 		 * 0  global_id;
 		 * 1  title;
 		 * 2  description;
 		 * 3  street_name;
 // ZIP code removed
 //		 * x4  zipcode;
 		 * 4  city;
 		 * 5  lat;
 		 * 6  lon;
 		 * 
 		 * 7  category_title;
 		 * 8 web_page;
 		 * 9 openingHours;
 		 * 10 telephone;
 		 * 11 image_url
 		*/
 		
 		int privateId = DBFactory.getInstance(context).getPoiPrivateIdFromGlobalId(Integer.parseInt(poidata[0]));
 		
 		Poi poi = new Poi.Builder(poidata[1],new PoiAddress.Builder(poidata[/*5*/ 4]) 
 // ZIP code removed
 //		.zipCode(Integer.parseInt(poidata[4])) 
 		.street(poidata[3])
 		.longitude(Double.parseDouble(poidata[/*7*/ 6])).latitude(Double.parseDouble(poidata[/*6*/ 5]))
 		.build()
 		).description(poidata[2])
 		.category(poidata[/*8*/ 7])
 		.favourite(false)
 		.telephone(poidata[/*11*/ 10])
 		.imageURL(poidata[/*12*/ 11])
 		.idPrivate(privateId)
 		.idGlobal(Integer.parseInt(poidata[0])).build();
 		
 		return poi;
 	}
 	
 	
 	/**
 	 * adds and updates pois.
 	 *
 	 * @param poidata the poidata
 	 * @return int[0] number of pois added. int[1] number of pois updated.
 	 */
 	private int handlePoiData(String[] poidata)
 	{
 		
 		Poi poi = getPoiFromPoiData(poidata);
 		
 		if(poi.getIdPrivate() == -1 )//new poi. add to DB
 		{
 			DBFactory.getInstance(context).newPoi(poi);
 			return 0;
 		}
 		else //old poi. update
 		{
 			DBFactory.getInstance(context).editPoi(poi);
 			return 1;
 		}
 	}
 	/**
 	 * commits pois to the local database
 	 * 
 	 * @param pois
 	 * @return int[0] number of pois added. int[1] number of pois updated.
 	 */
 	public int[] storePois(ArrayList<Poi> pois)
 	{
 		int[] res = new int[]{0,0};
 		
 		for (Poi poi : pois)
 		{
 			if(poi.getIdPrivate() == -1 )//new poi. add to DB
 			{
 				DBFactory.getInstance(context).newPoi(poi);
 				res[0]++;
 			}
 			else //old poi. update
 			{
 				DBFactory.getInstance(context).editPoi(poi);
 				res[1]++;
 			}
 		}	
 		return res;
 	}
 	
 	/**
 	 * Downloads, adds and updates trips from a repository.
 	 * 
 	 * @return int[0] number of trips added. int[1] number of trips updated.
 	 */
 	public int[] doUpdateTrips()
 	{
 		doInternetUpdatePois();
 		
 		int[] res = new int[]{0,0};
 		
 		String[] tripdata;
 		
 		try
 		{
 			updateServerURL = new URL(urlAddress+"trips.ce");
 			
 			InputStream is = updateServerURL.openStream();
 			BufferedReader in = new BufferedReader(new InputStreamReader(is));
 			
 			String line;
 			
 			while ((line = in.readLine()) != null)
 			{
 				if(line.startsWith("global_id"))continue;
 				tripdata = line.split(";");
 				res[handleTripData(tripdata)]++;
 			}
 			in.close();
 		} 
 		catch (MalformedURLException e) {Toast.makeText(context, "Error updating MalformedURLException", Toast.LENGTH_LONG).show();
 		} 
 		catch (IOException e) {Toast.makeText(context, "Error updating IOException "+e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 		
 		return res;
 	}
 	
 	public ArrayList<Trip> getInternetTrips()
 	{
 		ArrayList<Trip> trips = new ArrayList<Trip>();
 		
 		String[] tripdata;
 		
 		try
 		{
 			updateServerURL = new URL(urlAddress+"trips.ce");
 			
 			InputStream is = updateServerURL.openStream();
 			BufferedReader in = new BufferedReader(new InputStreamReader(is));
 			
 			String line;
 			
 			while ((line = in.readLine()) != null)
 			{
 				if(line.startsWith("global_id"))continue;
 				tripdata = line.split(";");
 				trips.add(getTripFromTripData(tripdata));
 			}
 			in.close();
 		} 
 		catch (MalformedURLException e) {Toast.makeText(context, "Error updating MalformedURLException", Toast.LENGTH_LONG).show();
 		} 
 		catch (IOException e) {Toast.makeText(context, "Error updating IOException "+e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 		
 		
 		return trips;
 		
 	}
 	
 	private Trip getTripFromTripData(String[] tripdata)
 	{
 		DatabaseInterface db = DBFactory.getInstance(context);
 		
 		ArrayList<Poi> allInternetPois = getInternetPois();
 		
 		
 		/*
 		0*global_id;
 		1*title;
 		2*description;
 		3*free_trip;
 		*
 		4*poi_global_id;
 		5*hour;
 		6*minute;
 		*
 		7*poi_global_id;
 		8*hour;
 		9*minute;
 		*/
 		
 		
 		ArrayList<Poi> downloadedPois = new ArrayList<Poi>();
 		HashMap<Poi, Time> times = new HashMap<Poi, Time>();
 		for (int i = 4; i < tripdata.length; i = i+3)
 		{
 			//get the pois contained in this downloaded trip.
 			for (int j = 0; j < allInternetPois.size(); j++)
 			{
 				if(allInternetPois.get(j).getIdGlobal() == Integer.parseInt(tripdata[i]))
 				{
 					Poi poi = allInternetPois.get(j);
 					
 					int privateID = db.getPoiPrivateIdFromGlobalId(Integer.parseInt(tripdata[i]));
 					if(privateID != -1)
 						poi = db.getPoi(privateID);
 					
 					downloadedPois.add(poi);
 					times.put(poi, new Time(Integer.parseInt(tripdata[i+1]), Integer.parseInt(tripdata[i+2])));
 				}
 			}
 		}
 		
 		int privateId = db.getTripPrivateIdFromGlobalId(Integer.parseInt(tripdata[0]));
 		
 		Trip trip;
 		
 		if(privateId == -1)//Trip does not exist.. create it.
 		{
 			trip = new Trip.Builder(tripdata[1])
 			.idGlobal(Integer.parseInt(tripdata[0]))
 			.description(tripdata[2])
 			.freeTrip((tripdata[3].equals("1")) ? true:false)
 			.build();
 			
 			for (Poi poi : downloadedPois)
 			{	
 				trip.addPoi(poi);
 				trip.setTime(poi, times.get(poi));
 				System.out.println("added poi to new trip obj: "+poi.getLabel());
 			}
 			
 			
 		}
 		else//trip exists.. update it
 		{
 			trip = db.getTrip(privateId);
 			ArrayList<Poi> oldPois = trip.getPois();
 			for (Poi poi : downloadedPois)
 			{
 				trip.setTime(poi, times.get(poi));
 				if( !oldPois.contains(poi))	//if the local trip does not contain the poi...
 				{
 					System.out.println("New poi not in list: "+poi.getLabel()+" adding it to trip obj");
 					trip.addPoi(poi);		//add it.
 					
 				}
 			}
 			
 			
 			
 		}
 		
 		System.out.println("ok.. trip sixe="+trip.getPois().size());
 		
 		return trip;
 		
 		/*db.newTrip(trip);
 		db.addPoiToTrip(trip, poi);
 		db.addTimesToTrip(trip);*/
 		
 	}
 	
 	public int[] storeTrips(ArrayList<Trip> trips)
 	{
 		DatabaseInterface db = DBFactory.getInstance(context);
 		
 		int[] res = new int[]{0,0};
 		
 		
 		for (Trip trip : trips)
 		{
 			System.out.println("ok.. trip size="+trip.getPois().size());
 			
 			HashMap<Poi, Time> poitimes = trip.getFixedTimes();
 			HashMap<Integer, Time> gidtimes = new HashMap<Integer, Time>();
 			//add new pois to the database
 			ArrayList<Integer> globalIDs = new ArrayList<Integer>();
 			for (Poi p : trip.getPois()) 
 			{
 				globalIDs.add(p.getIdGlobal());
 				gidtimes.put(p.getIdGlobal(), poitimes.get(p));
 				int privateID = db.getPoiPrivateIdFromGlobalId(p.getIdGlobal());
 				if(privateID == -1)//not added to db
 				{
 					db.newPoi(p);
 					System.out.println("poi "+p.getLabel()+" added to db");
 				}
 				else//in db, update
 				{
 					db.editPoi(p);
 					System.out.println("poi "+p.getLabel()+" updated in db");
 				}
 			}
 			//update trip with new poi object (containing correct private IDs)
 			trip.getPois().clear();
 			poitimes.clear();
 			for (Integer gid : globalIDs) 
 			{
 				Poi p = db.getPoi(db.getPoiPrivateIdFromGlobalId(gid));
 				trip.getPois().add(p);
 				poitimes.put(p, gidtimes.get(gid));
 			}
 			
 			int privateId = db.getTripPrivateIdFromGlobalId(trip.getIdGlobal());
 			
 			if(privateId == -1)//Trip does not exist.. create it.
 			{
 				
 				db.newTrip(trip);
 				ArrayList<Poi> newpois = trip.getPois();
 				
 				trip = db.getTrip(db.getTripPrivateIdFromGlobalId(trip.getIdGlobal()));
 				for (Poi poi : newpois)
 				{
 					db.addPoiToTrip(trip, poi);
 					trip.addPoi(poi);
 					System.out.println("added poi to new trip: "+poi.getLabel());
 				}
 				System.out.println("create new trip..");
 				trip.setTime(poitimes);
 				db.addTimesToTrip(trip);
 				
 				res[0]++;
 			}
 			else//trip exists.. update it
 			{
 				System.out.println("update old trip.."+trip.getPois().size());
 				ArrayList<Poi> dbPois = db.getTrip(privateId).getPois();//trip.getPois();
 				for (Poi poi : trip.getPois())
 				{
 					if( !dbPois.contains(poi))	//if the db trip does not contain the poi...
 					{
 						System.out.println("New poi not in list: "+poi.getLabel()+" adding it");
 						db.addPoiToTrip(trip, poi);
 						trip.getFixedTimes().remove(poi);
 					}
 				}
 				trip.setTime(poitimes);
 				db.addTimesToTrip(trip);
 				
 				res[1]++;
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Handle trip data.
 	 *
 	 * @param tripdata the tripdata
 	 * @return the int
 	 */
 	private int handleTripData(String[] tripdata)
 	{
 		DatabaseInterface db = DBFactory.getInstance(context);
 		
 		/*
 		0*global_id;
 		1*title;
 		2*description;
 		3*free_trip;
 		*
 		4*poi_global_id;
 		5*hour;
 		6*minute;
 		*
 		7*poi_global_id;
 		8*hour;
 		9*minute;
 		*/
 		
 		
 		ArrayList<Poi> downloadedPois = new ArrayList<Poi>();
 		HashMap<Poi, Time> times = new HashMap<Poi, Time>();
 		for (int i = 4; i < tripdata.length; i = i+3)
 		{
 			//get the pois contained in this downloaded trip.
 			Poi poi = db.getPoi(db.getPoiPrivateIdFromGlobalId(Integer.parseInt(tripdata[i])));
 			downloadedPois.add(poi);
 			times.put(poi, new Time(Integer.parseInt(tripdata[i+1]), Integer.parseInt(tripdata[i+2])));
 		}
 		
 		int privateId = db.getTripPrivateIdFromGlobalId(Integer.parseInt(tripdata[0]));
 		
 		if(privateId == -1)//Trip does not exist.. create it.
 		{
 			Trip trip = new Trip.Builder(tripdata[1])
 			.idGlobal(Integer.parseInt(tripdata[0]))
 			.description(tripdata[2])
 			.freeTrip((tripdata[3].equals("1")) ? true:false)
 			.build();
 			
 			db.newTrip(trip);
 			
 			trip = db.getTrip(db.getTripPrivateIdFromGlobalId(Integer.parseInt(tripdata[0])));
 			
 			for (Poi poi : downloadedPois)
 			{	
 				trip.addPoi(poi);
 				trip.setTime(poi, times.get(poi));
 				db.addPoiToTrip(trip, poi);
 				//System.out.println("added poi to new trip: "+poi.getLabel());
 			}
 			
 			db.addTimesToTrip(trip);
 			
 			return 0;
 		}
 		else//trip exists.. update it
 		{
 			Trip trip = db.getTrip(privateId);
 			ArrayList<Poi> oldPois = trip.getPois();
 			for (Poi poi : downloadedPois)
 			{
 				trip.setTime(poi, times.get(poi));
 				if( !oldPois.contains(poi))	//if the local trip does not contain the poi...
 				{
 					System.out.println("New poi not in list: "+poi.getLabel()+" adding it");
 					trip.addPoi(poi);		//add it.
 					db.addPoiToTrip(trip, poi);
 				}
 			}
 			
 			db.addTimesToTrip(trip);
 			
 			return 1;
 		}
 	}
 	
 	
 	
 	
 }

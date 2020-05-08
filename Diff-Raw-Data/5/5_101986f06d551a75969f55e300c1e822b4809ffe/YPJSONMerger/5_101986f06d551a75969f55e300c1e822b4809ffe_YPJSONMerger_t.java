 package com.where.atlas.feed.yellowpages.YPdedupe;
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.tika.io.IOUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 public class YPJSONMerger {
 
 	
 	public static HashMap<String,JSONObject> reviews;
 	public static HashMap<String,JSONObject> details;
 	public static HashSet<JSONObject> listings;
 	public static JSONArray dump;
 	public static BufferedWriter writer;
 	
 	private static void setupWriter(String writepath) throws IOException
 	{
 		writer = new BufferedWriter(new FileWriter(writepath));
         
 	}
 	
 	private static void closeWriter() throws IOException
 	{
         writer.close();
 	}
 	
 	
 	public static void main(String[] args) {
 		if(args.length != 2){
 			System.err.println("Usage:\narg0 - folder with YPdetails,YPlistings,and YPreviews.json"+
 									"\narg1 - file to write final deduped json.");
 			return;
 		}
 		
 		
 		try {
 			setupWriter(args[1]);
 			
 			
 			loadPIDSfromJSONArray(args[0]+"/YPreviews.json",reviews);
 			loadPIDSfromJSONArray(args[0]+"/YPdetails.json",details);
 			parseListings(args[0]+"/YPlistings.json");
 			
 			closeWriter();
 		} catch (IOException e) {
 			System.err.println("IO ERROR:"+e.getMessage());
 		} catch (JSONException e) {
 			System.err.println("JSON ERROR:"+e.getMessage());
 		}
 	} 
 	
 	public static void parseListings(String path) throws IOException, JSONException
 	{
 		BufferedReader br = new BufferedReader(new FileReader(path));
 		listings = new HashSet<JSONObject>();
 		int count =0 ;
 		 String line = null;
         while((line = br.readLine()) != null) {
         	count++;
 			listings.add(new JSONObject(line.replace("amp;", "&")));
 		}
 		
         System.out.println("Added "+count+" unique listings to memory...writing");
 		//now we have no duplicates
 		Iterator<JSONObject> it = listings.iterator();
 		while(it.hasNext())
 		{
 			JSONObject listing = it.next();
			if(!(listing.optString("pid").equals("")) && reviews.containsKey(listing.optString("pid")))
 			{
 				listing = updateReview(listing);
 				//remove from map
 				reviews.remove(listing.optString("pid"));
 			}
			if(!(listing.optString("pid").equals("")) && details.containsKey(listing.optString("pid")))
 			{
 				listing = updateDetail(listing);
 				//remove from map
 				details.remove(listing.optString("pid"));
 			}
 			//aaannddddd write
 			collect(listing);
 		}
 		
 		System.out.print("done. dumping leftovers...");
 		
 		//add remaining in maps
 		if(reviews.size() > 0)
 		{
 			collectFromMap(reviews);
 		}
 		if(details.size() > 0)
 		{
 			collectFromMap(details);
 		}
 		System.out.println("Done.");
 	}
 	
 	private static void collectFromMap(HashMap<String,JSONObject> map)
 	{
 		Set<String> keyset = map.keySet();
 		Iterator<String> it = keyset.iterator();
 		
 		while(it.hasNext())
 		{
 			collect(map.get(it.next()));
 		}
 	}
 	
 	private static boolean isUniqueReview(JSONArray listing,JSONObject review) throws JSONException
 	{
 		for(int i=0;i<listing.length();i++)
 		{
 			if(listing.get(i).equals(review))
 				return false;
 		}
 		return true;
 	}
 	
 	//updates the json since its already in the reviews map
 	private static JSONObject updateReview(JSONObject listing) throws JSONException
 	{
 		JSONObject reviewpoi = reviews.get(listing.optString("pid"));
 		
 		
 		if(reviewpoi.optJSONArray("reviews") != null)
 		{
 			JSONArray listingreviews = listing.optJSONArray("reviews");
 			JSONArray reviewReviews = reviewpoi.optJSONArray("reviews");
 			
 			for(int i=0;i<reviewReviews.length();i++)
 			{
 				if(isUniqueReview(listingreviews,(JSONObject)reviewReviews.get(i)))
 				{
 					listing.accumulate("reviews", reviewReviews.get(i));
 				}
 			}
 		}
 		
 		return listing;
 	}
 	
 	//updates the json since its already in the details map
 	private static JSONObject updateDetail(JSONObject listing) throws JSONException
 	{
 		JSONObject detailpoi = details.get(listing.optString("pid"));
 		
 		if(detailpoi.optString("accreditations") != null)
 			listing.put("accreditations", detailpoi.optString("accreditations"));
 		if(detailpoi.optString("brands") != null)
 			listing.put("brands", detailpoi.optString("brands"));
 		if(detailpoi.optString("payment") != null)
 			listing.put("payment", detailpoi.optString("payment"));
 		if(detailpoi.optString("in_biz_since") != null)
 			listing.put("in_biz_since", detailpoi.optString("in_biz_since"));
 		if(detailpoi.optString("aka") != null)
 			listing.put("aka", detailpoi.optString("aka"));
 		if(detailpoi.optString("avg_rating") != null)
 			listing.put("avg_rating", detailpoi.optString("avg_rating"));
 		if(detailpoi.optString("languages") != null)
 			listing.put("languages", detailpoi.optString("languages"));
 		
 		
 		return listing;
 	}
 	
 	public static void collect(JSONObject json) {
 		try {
 			
 			writer.write(json.toString());
 			writer.newLine();
 			
 		} catch (IOException e) {
 			System.err.println("error writing:"+e.getMessage());
 		}
 	}
 	
 	public static void loadPIDSfromJSONArray(String path,HashMap<String,JSONObject> map) throws IOException, JSONException
 	{
 		map = new HashMap<String,JSONObject>();
 		InputStream is = new FileInputStream(path);
 		JSONArray jarray = new JSONArray(IOUtils.toString( is ));
 		JSONObject jobj = new JSONObject();
 		String pid = null;
 		
 		for(int i = 0;  i < jarray.length(); i++)
 		{
 			jobj = jarray.getJSONObject(i);
 			pid = jobj.optString("pid");
 			if(pid != null)
 				map.put(pid,jobj);
 		}
 		System.out.println("Loaded " + path + " to memory.");
 	}
 	
 }

 package se.chalmers.krogkollen.pub;
 
 import java.util.LinkedList;
 import java.util.List;
 
import android.util.Log;
 import se.chalmers.krogkollen.utils.StringConverter;
 import android.content.res.Resources;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 /*
  * This file is part of Krogkollen.
  *
  * Krogkollen is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Krogkollen is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Krogkollen.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 /**
  *
  * A singleton holding a list of all the pubs, and is used to load data from the server.
  *
  * For now this class only contains hard coded values, since we don't have support for a server.
  *
  * @author Albin Garpetun
  * Created 2013-09-22
  */
 public class PubUtilities {
 
 	private List<IPub> pubList = new LinkedList<IPub>();
 	private static PubUtilities instance = null;
 
 	private PubUtilities() {
 		// Exists only to defeat instantiation.
 	}
 
 	/**
 	 * Creates an instance of this object if there is none, otherwise it simply returns the old one.
 	 *
 	 * @return The instance of the object.
 	 */
 	public static PubUtilities getInstance() {
 		if(instance == null) {
 			instance = new PubUtilities();
 		}
 		return instance;
 	}
 
 	/**
 	 * Loads the pubs from the server and puts them in the list of pubs.
 	 *
 	 * For now this method only adds hardcoded pubs into the list.
 	 */
 	public void loadPubList() {
 
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Pub");
 		List <ParseObject> tempList;
 		try {
 			tempList = query.find();
 			for(ParseObject object : tempList){
 				int hourFourDigit = StringConverter.convertCombinedStringto(object.getString("openingHours"), 5);
 				pubList.add(new Pub(object.getString("name"),
 						object.getString("description"),
 						object.getDouble("latitude"),
 						object.getDouble("longitude"),
 						object.getInt("ageRestriction"),
 						object.getInt("entranceFee"),
 						(hourFourDigit/100),
 						(hourFourDigit%100),
 						object.getInt("posRate"),
 						object.getInt("negRate"),
 						object.getInt("queueTime"),
						object.getObjectId()));
 
            }
 		} catch (com.parse.ParseException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		
 		// TODO Check code below, can it be removed?
 		/*query.findInBackground(new FindCallback<ParseObject>() {
 			@Override
 			public void done(List<ParseObject> parsePubList, com.parse.ParseException e){
 				if(e == null){
 					Log.d("pub", "Retrieved " + parsePubList.size() + " pubs");
 					for(ParseObject object : parsePubList){
 						pubList.add(new Pub(object.getString("name"),
 								object.getString("description"),
 								object.getString("openingHours"),
 								object.getInt("ageRestriction"),
 								object.getInt("queueTime"),
 								object.getDouble("latitude"),
 								object.getDouble("longitude"),
 								object.getString("objectId")));
 
 					}
 				}
 				else{
 					Log.d("pub", "Error: " + e.getMessage());
 				}
 			}
 
 		});
 		Log.d("STR�NG", "PUBAR I LISTAN : " + pubList.size());
 		 */
 	}
 
 	/**
 	 * Returns the list of pubs.
 	 *
 	 * @return The list of pubs.
 	 */
 	public List<IPub> getPubList() {
 		return pubList;
 	}
 	
 	// TODO Check this method, id is a STring now
 	// TODO write Javadoc
     public IPub getPub(String id){
        for(IPub pub: pubList) {
             if(pub.getName().equals(id)){
                 return pub;
             }
         }
         throw  new Resources.NotFoundException("The ID does not match with any pub");
     }
 }

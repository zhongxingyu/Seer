 package lastfm;
 
 import java.io.ByteArrayInputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.ws.rs.core.Response;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 
 public class LastfmObjects {
 	private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
 	Client client = JerseyClient.getClient();
 	private static int NOOFEVENTS = 1000; 
 
 	public String getTopArtistsByCountry(String key, String country) {
 		try{
 			String url = BASE_URL+"method=geo.gettopartists&country=spain&api_key="+key+"&limit=2000";
 			WebResource webResource = client.resource(url);
 			ClientResponse cr =  webResource.get(ClientResponse.class);;
 			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
 				String resString = cr.getEntity(String.class);
 				if(resString != null){
 					ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes());
 					Element docEle = LastfmObjectUtil.parseXml(bs);
 					NodeList nl = docEle.getElementsByTagName("artist");
 					if(nl != null && nl.getLength() > 0) {
 						for(int i = 0 ; i < nl.getLength();i++) {
 							Element el = (Element)nl.item(i);
 							System.out.println(LastfmObjectUtil.getTextValue(el, "name"));
 						}
 					}
 				}
 			}	
 			else{
 				System.err.println("error in fetching us top artists");
 				return null;
 			}
 		}catch(Exception e){
 			System.err.println("exception caught ");
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public ArrayList<String>  getEventsByLocation(String key, String location){
 		try{
 			String url = BASE_URL+"method=geo.getevents&location="+location+"&api_key="+key+"&limit="+NOOFEVENTS;
 			System.out.println(url);
 			ArrayList<String> events = new ArrayList<String>();
 			WebResource webResource = client.resource(url);
 			ClientResponse cr =  webResource.get(ClientResponse.class);;
 			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
 				String resString = cr.getEntity(String.class);
 				//String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"eventsAt"+location);
 				if(resString != null){
 					ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes());
 					Element docEle = LastfmObjectUtil.parseXml(bs);
 					NodeList nl = docEle.getElementsByTagName("event");
 					if(nl != null && nl.getLength() > 0) {
 						for(int i = 0 ; i < nl.getLength();i++) {
 							Element el = (Element)nl.item(i);
 							events.add(LastfmObjectUtil.getTextValue(el, "id"));
 
 						}
 					}
 				}
 				return events;
 			}
 			else{
 				System.err.println("error in fetching us top artists");
 				JSONObject response = (JSONObject) new JSONObject(cr.getEntity(String.class)).get("response");
 				System.err.println(response.toString());
 				return null;
 			}
 		}catch(Exception e){
 			System.err.println("exception caught ");
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public ArrayList<String> getAttendeesByEvents(String key, String eventId) {
 		try{
 			String url = BASE_URL+"method=event.getattendees&event="+eventId+"&api_key="+key+"&limit=5000";
 			System.out.println(url);
 			ArrayList<String> attendees = new ArrayList<String>();
 			WebResource webResource = client.resource(url);
 			ClientResponse cr =  webResource.get(ClientResponse.class);;
 			//System.out.println(cr.toString());
 			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
 				String resString = cr.getEntity(String.class);
 				//String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"AttendeesForEventId"+eventId);
 				if(resString != null){
 					ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes());
 					Element docEle = LastfmObjectUtil.parseXml(bs);
 					NodeList nl = docEle.getElementsByTagName("user");
 					if(nl != null && nl.getLength() > 0) {
 						for(int i = 0 ; i < nl.getLength();i++) {
 							Element el = (Element)nl.item(i);
 							//System.out.println(LastfmObjectUtil.getTextValue(el, "name"));
 							attendees.add(LastfmObjectUtil.getTextValue(el, "name"));
 						}
 					}
 				}
 				return attendees;
 			}
 			else{
 				System.err.println("error in fetching us top artists");
 				return null;
 			}
 		}catch(Exception e){
 			System.err.println("exception caught ");
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public HashSet<String> getUserFriends(String key, String u) {
 		try{
 			// http://ws.audioscrobbler.com/2.0/?method=user.getfriends&user=rj&api_key=b25b959554ed76058ac220.
			if(LastfmMain.hmFriends.containsKey(u)){
				return LastfmMain.hmFriends.get(u);
			}
 			String subStr = "method=user.getfriends&user="+URLEncoder.encode(u, "UTF-8")+"&api_key="+key+"&limit=1000";
 			//String url = BASE_URL+URLEncoder.encode(subStr, "UTF-8");
 			String url = BASE_URL+subStr;
 			System.out.println(url);
 			HashSet<String> friends = new HashSet<String>();
 			WebResource webResource = client.resource(url);
 			ClientResponse cr =  webResource.get(ClientResponse.class);;
 			//System.out.println(cr.toString());
 			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
 				String resString = cr.getEntity(String.class);
 				//String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"FriendListOf-"+u);
 				if(resString != null){
 					ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes());
 					Element docEle = LastfmObjectUtil.parseXml(bs);
 					NodeList nl = docEle.getElementsByTagName("user");
 					if(nl != null && nl.getLength() > 0) {
 						for(int i = 0 ; i < nl.getLength();i++) {
 							Element el = (Element)nl.item(i);
 							friends.add(LastfmObjectUtil.getTextValue(el, "name"));
 						}
 					}
 				}
 				return friends;
 			}
 			else{
 				System.err.println("error in fetching friends");
 				JSONObject response = (JSONObject) new JSONObject(cr.getEntity(String.class)).get("response");
 				System.err.println(response.getString("error"));
 				return null;
 			}
 		}catch(Exception e){
 			System.err.println("exception caught ");
 			e.printStackTrace();
 			return null;
 		}	}
 
 	public Set<Track> getUserTracks(String key, String u) {
 		try{
 			// http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=rj&api_key=b25b959554ed76058
			if(LastfmMain.hmUser.containsKey(u)){
				return LastfmMain.hmUser.get(u).getHsTracks();
			}
 			String url = BASE_URL+"method=user.getrecenttracks&user="+u+"&api_key="+key+"&limit=200";
 			//System.out.println(url);
 			HashMap<Track, String> tracks = new HashMap<Track, String>();
 			WebResource webResource = client.resource(url);
 			ClientResponse cr =  webResource.get(ClientResponse.class);;
 			//System.out.println(cr.toString());
 			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
 				String resString = cr.getEntity(String.class);
 				//String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"tracksOf-"+u);
 				if(resString != null){
 					ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes());
 					Element docEle = LastfmObjectUtil.parseXml(bs);
 					NodeList nl = docEle.getElementsByTagName("track");
 					if(nl != null && nl.getLength() > 0) {
 						for(int i = 0 ; i < nl.getLength();i++) {
 							Element el = (Element)nl.item(i);
 							Track track = new Track();
 							track.setTimeofPlay(LastfmObjectUtil.getTextValue(el, "date"));
 							String trackName = LastfmObjectUtil.getTextValue(el, "name");
 							track.setName(trackName);
 							String artist = LastfmObjectUtil.getTextValue(el, "artist");
 							String tagName = getTrackTagName(trackName, artist, key);
 							track.setTagName(tagName);
 							String album = LastfmObjectUtil.getTextValue(el, "album");
 							track.setArtist(getArtistInfo(key, artist));
 							track.setAlbum(getAlbumInfo(key, album));
 							//Steystem.out.println("track date "+LastfmObjectUtil.getDateValue(el, "date"));
 							if(tracks.containsKey(track)){
 								String existingTimeOfPlay = tracks.get(track);
 								String newTimeOfPlay = track.getTimeofPlay();
 								if(newTimeOfPlay != null && existingTimeOfPlay != null){
 									Date aDate = LastfmObjectUtil.parseDate(newTimeOfPlay);
 									Date bDate = LastfmObjectUtil.parseDate(existingTimeOfPlay);
 									if(aDate != null && bDate != null){
 										if(aDate.before(bDate)){
 											tracks.put(track, newTimeOfPlay);
 										}
 									}
 								}
 							}else{
 								tracks.put(track, track.getTimeofPlay());	
 							}
 						}
 					}
 
 				}
 				return tracks.keySet();
 			}
 			else{
 				System.err.println("error in fetching user tracks...ignoring");
 				JSONObject response = (JSONObject) new JSONObject(cr.getEntity(String.class)).get("response");
 				//System.err.println(response.getString("error"));
 				return null;
 			}
 		}catch(Exception e){
 			System.err.println("exception caught ");
 			e.printStackTrace();
 			return null;
 		}	}
 
 
 	private String getTrackTagName(String trackName, String artist, String key) throws UnsupportedEncodingException, ClientHandlerException, UniformInterfaceException, JSONException {
 		if(LastfmMain.hmTrackTags.containsKey(trackName)){
 			return LastfmMain.hmTrackTags.get(trackName);
 		}
 		String subStr = "track="+trackName+"&artist="+artist;
 		String url = BASE_URL+"method=track.getinfo&api_key="+key+"&track="+URLEncoder.encode(trackName, "UTF-8")+"&artist="+URLEncoder.encode(artist, "UTF-8");
 		StringBuffer trackTags = new StringBuffer();
 		WebResource webResource = client.resource(url);
 		ClientResponse cr =  webResource.get(ClientResponse.class);;
 		if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
 			String resString = cr.getEntity(String.class);
			//String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"tracksOf-tag-"+trackName);
 			if(resString != null){
 				ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes());
 				Element docEle = LastfmObjectUtil.parseXml(bs);
 				NodeList nl = docEle.getElementsByTagName("track");
 				if(nl != null && nl.getLength() > 0) {
 					for(int i = 0 ; i < nl.getLength();i++) {
 						Element el = (Element) nl.item(i);
 						NodeList tagName  =  el.getElementsByTagName("tag");
 						for(int j = 0; j < tagName.getLength(); j++){
 							Element tag = (Element)tagName.item(j);
 							String trkTag =  LastfmObjectUtil.getTextValue(tag, "name");
 							if(trkTag != null){
 								trackTags.append(trkTag+";");
 							}
 						}
 					}
 				}
 			}
 			LastfmMain.hmTrackTags.put(trackName, trackTags.toString());
 			return trackTags.toString();
 		}else{
 			System.err.println("error in fetching user tracks...ignoring");
 			JSONObject response = (JSONObject) new JSONObject(cr.getEntity(String.class)).get("response");
 			System.err.println(response.getString("error"));
 		}
 		cr.close();
 		return null;		//<toptags>
 	}
 
 	public Artist getArtistInfo(String key, String artist){
 		Artist ar = new Artist();
 		ar.setName(artist);
 		return ar;
 	}
 
 	public Album getAlbumInfo(String key, String album){
 		Album ab = new Album();
 		ab.setName(album);
 		return ab;
 	}
 
 	public User getUserInfo(String key, String u) {
 		try{
 			String url = BASE_URL+"method=user.getinfo&user="+u+"&api_key="+key;
 			Collection<Track> tracks = getUserTracks(key, u); 
 			User userInfo = new User();
 			WebResource webResource = client.resource(url);
 			ClientResponse cr =  webResource.get(ClientResponse.class);;
 			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
 				String resString = cr.getEntity(String.class);
 				//String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"InfoOfUser-"+u);
 				if(resString != null){
 					ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes());
 					Element docEle = LastfmObjectUtil.parseXml(bs);
 					NodeList nl = docEle.getElementsByTagName("user");
 					if(nl != null && nl.getLength() > 0) {
 						for(int i = 0 ; i < nl.getLength();i++) {
 							Element el = (Element)nl.item(i);
 							userInfo.setUserID(LastfmObjectUtil.getTextValue(el, "id"));
 							userInfo.setName(LastfmObjectUtil.getTextValue(el, "name"));
 							userInfo.setRealname(LastfmObjectUtil.getTextValue(el, "realname"));
 							userInfo.setCountry(LastfmObjectUtil.getTextValue(el, "country"));
 							userInfo.setGender(LastfmObjectUtil.getTextValue(el, "gender"));
 							userInfo.setAge(LastfmObjectUtil.getIntValue(el, "age"));
 							userInfo.setPlayCount(LastfmObjectUtil.getIntValue(el, "playcount"));
 							userInfo.setPlayLists(LastfmObjectUtil.getIntValue(el, "playlists"));
 							userInfo.setRegistrationDate(LastfmObjectUtil.getDateValue(el, "registered"));
 							if(tracks != null)
 								userInfo.setHsTracks(new HashSet(tracks));
 						}
 					}
 				}
 				return userInfo;
 			}
 			else{
 				System.err.println("error in fetching user info");
 				return null;
 			}
 		}catch(Exception e){
 			System.err.println("exception caught ");
 			e.printStackTrace();
 			return null;
 		}
 	}
 }

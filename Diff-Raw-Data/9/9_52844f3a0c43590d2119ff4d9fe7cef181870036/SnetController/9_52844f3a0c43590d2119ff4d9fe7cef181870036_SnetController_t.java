 package edu.uw.cs.cse461.sp12.OS;
 
 import java.util.Iterator;
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import edu.uw.cs.cse461.sp12.DB461.DB461.DB461Exception;
 import edu.uw.cs.cse461.sp12.DB461.DB461.Record;
 import edu.uw.cs.cse461.sp12.DB461.DB461.RecordSet;
 import edu.uw.cs.cse461.sp12.OS.RPCCallable.RPCCallableMethod;
 import edu.uw.cs.cse461.sp12.OS.SNetDB461.CommunityRecord;
 import edu.uw.cs.cse461.sp12.OS.SNetDB461.Photo;
 import edu.uw.cs.cse461.sp12.OS.SNetDB461.PhotoRecord;
 import edu.uw.cs.cse461.sp12.util.Base64;
 
 
 public class SnetController extends RPCCallable {
 
 	//TODO friends
 	private SNetDB461 db;
 	private File photoDir;
 	
 	private RPCCallableMethod<SnetController> fetchUpdates;
 	private RPCCallableMethod<SnetController> fetchPhoto;
 	
 	public SnetController() throws Exception {
 		fetchUpdates = new RPCCallableMethod<SnetController>(this, "_fetchUpdates");
 		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "fetchUpdates", fetchUpdates);
 		fetchPhoto = new RPCCallableMethod<SnetController>(this, "_fetchPhoto");
 		((RPCService)OS.getService("rpc")).registerHandler(servicename(), "fetchPhoto", fetchPhoto);
 		db = new SNetDB461();
 		if(!db.dbExists()) db.openOrCreateDatabase();
		CommunityRecord host = db.COMMUNITYTABLE.readOne(OS.config().getProperty("host.name"));
		fetchUpdates(".");
		if(host == null) {
 			storeInfo(db.COMMUNITYTABLE.createRecord(), OS.config().getProperty("host.name"), Integer.MIN_VALUE, 0, 0);
 			storeInfo(db.COMMUNITYTABLE.createRecord(), OS.config().getProperty("ddns.rootserver"), Integer.MIN_VALUE, 0, 0);
 		}
 		
 		photoDir = null;
		db.discard(); //The db must be closed so that other threads may access it. Only one thread can access at a time;
 	}
 		
 	
 	private void storeInfo(CommunityRecord cr, String name, int myHash, int chosenHash, int generation) throws DB461Exception {
 		cr = db.COMMUNITYTABLE.createRecord();
 		cr.generation = 0;
 		cr.name = OS.config().getProperty("host.name");
 		try {
 			db.openOrCreateDatabase();
 			db.COMMUNITYTABLE.write(cr);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			db.discard();
 		}
 		
 	}
 	
 	@Override
 	public String servicename() {
 		return "snet";
 	}
 
 	@Override
 	public void shutdown() {
 		// TODO Auto-generated method stub
 		
 	}
 	public JSONObject _fetchUpdates(JSONObject args){
 		try {
 			db = new SNetDB461();
 			db.openOrCreateDatabase();
 			JSONObject community;
 			JSONArray needPhotos;
 			needPhotos = args.getJSONArray("needphotos");
 			community = args.getJSONObject("community");
 			JSONObject out = new JSONObject();
 			RecordSet<CommunityRecord> oRecVec;
 			oRecVec = toRecordSet(community);
 
 			/** Process Community Updates **/
 			JSONObject communityUpdates = new JSONObject();
 			for(CommunityRecord oR: oRecVec){
 				CommunityRecord mR = db.COMMUNITYTABLE.readOne(oR.name);
 				if(mR != null){
 					if(mR.generation > oR.generation){ // Checks to see if my data is more up to date
 						JSONObject newRec = toJSONResponse(mR);
 						communityUpdates.put(oR.name, newRec);
 					} else if(mR.generation < oR.generation){
 						//oR.isFriend = mR.isFriend;
 						//db.COMMUNITYTABLE.write(oR); //Updates own table with more up to date data;
 						//TODO: Manage ref counts in the photo table
 					}
 				}
 			}
 			
 			/**Process Photo Updates **/
 			JSONArray photoUpdates = new JSONArray();
 			for(int i = 0; i < needPhotos.length(); i++){
 				PhotoRecord pr = db.PHOTOTABLE.readOne(needPhotos.getInt(i));
 				if(pr != null) photoUpdates.put(pr.hash);
 			}
 			out.put("communityupdates", communityUpdates);
 			out.put("photoupdates", photoUpdates);
 			return out;
 		} catch (DB461Exception e1) {
 			e1.printStackTrace();
 		} catch (JSONException je) {
 			je.printStackTrace();
 		} catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			   if ( db != null ) db.discard();
 		}
 		return new JSONObject();
 
 	}
 	public JSONObject _fetchPhoto(JSONObject args){
 		try {
 			db.openOrCreateDatabase();
 			int photoHash = args.getInt("photohash");
 			PhotoRecord pr = db.PHOTOTABLE.readOne(args.getInt("photohash"));
 			if(pr != null){
 				JSONObject out = new JSONObject();
 				out.put("photohash", photoHash);
 				out.put("photodata", Base64.encodeFromFile(pr.file.getAbsolutePath()));
 				return out;
 			} else {
 				//TODO: Return Exception
 				return null;
 			}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 
 		} catch (DB461Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 
 		} finally {
 			if(db != null) db.discard();
 		}
 		
 	}
 	
 	private RecordSet<CommunityRecord> toRecordSet(JSONObject o) throws JSONException{
 		Iterator<String> names = o.keys();
 		RecordSet<CommunityRecord> out = new RecordSet<CommunityRecord>();
 		while(names.hasNext()){
 			String name = names.next();
 			JSONObject rec = o.getJSONObject(name);
 			CommunityRecord r = db.createCommunityRecord();
 			r.name = name;           
 			r.generation = rec.getInt("generation");     
 			r.myPhotoHash = rec.getInt("myphotohash");    
 			r.chosenPhotoHash = rec.getInt("chosenphotohash");
 			out.add(r);
 		}
 		return out;
 	}
 	private JSONObject toJSONResponse(CommunityRecord r) throws JSONException{
 		JSONObject newRec = new JSONObject();
 		newRec.put("generation", r.generation);
 		newRec.put("chosenphotohash", r.chosenPhotoHash);
 		newRec.put("myphotohash", r.myPhotoHash);
 		return newRec;
 	}
 	
 	/**
 	 * Client Side Fetch updates
 	 * @param name
 	 * @return
 	 */
 	public JSONArray fetchUpdates(String name) {
 		//TODO Make neededphotos
 		
 		try {
 			DDNSRRecord rr = ((DDNSResolverService)OS.getService("ddnsresolver")).resolve(name);
 			RPCCallerSocket sock = new RPCCallerSocket(rr.name, rr.host, rr.port);
 			JSONObject request = new JSONObject();
 			request.put("community", communityToJSON());
 			request.put("needphotos", neededPhotos());
 			JSONObject response = sock.invoke("snet", "fetchUpdates", request);
 			JSONObject commUpdates = response.getJSONObject("communityupdates");
 			db.openOrCreateDatabase();
 			for(Iterator<String> it = commUpdates.keys(); it.hasNext();) {
 				String key = it.next();
 				JSONObject memberUpdate = commUpdates.getJSONObject(key);
 				CommunityRecord cr = db.COMMUNITYTABLE.readOne(key);
 				if(cr == null) cr = db.createCommunityRecord();
 				if(cr.generation < memberUpdate.getInt("generation")) {
 					changeRef(cr.myPhotoHash, -1);
 					changeRef(cr.chosenPhotoHash, -1);
 					int myHash = memberUpdate.getInt("myphotohash");
 					int chosenHash = memberUpdate.getInt("chosenphotohash");
 					cr.generation = memberUpdate.getInt("generation");
 					cr.myPhotoHash = myHash;
 					cr.myPhotoHash = chosenHash;
 					newPhoto(myHash);
 					newPhoto(chosenHash);
 					db.COMMUNITYTABLE.write(cr);
 				}
 			}
 			
 			return response.getJSONArray("photoupdates");
 		} catch (DDNSException e) {
 			// TODO failed ddns for whatever reason
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DB461Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch(Exception e){
 			e.printStackTrace();
 		} finally {
 			db.discard();
 		}
 		return null;
 	}
 	
 	private JSONObject communityToJSON() throws DB461Exception, JSONException {
 		JSONObject result = new JSONObject();
 		try {
 			db.openOrCreateDatabase();
 			for(CommunityRecord cr : db.COMMUNITYTABLE.readAll()) {
 				JSONObject member = new JSONObject();
 				member.put("generation", cr.generation);
 				member.put("myphotohash", cr.myPhotoHash);
 				member.put("chosenphotohash", cr.chosenPhotoHash);
 				result.put(cr.name, member);
 			}
 			return result;
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} finally {
 			db.discard();
 		}
 	}
 	
 	private JSONArray neededPhotos() {
 		JSONArray result = new JSONArray();
 		
 		return result;
 	}
 	
 	private void changeRef(int hash, int dif) throws DB461Exception {
 		PhotoRecord pr = db.PHOTOTABLE.readOne(hash);
 		pr.refCount += dif;
 		db.PHOTOTABLE.write(pr);
 	}
 	
 	private void newPhoto(int hash) throws DB461Exception {
 		PhotoRecord pr = db.PHOTOTABLE.readOne(hash);
 		if(pr == null) {
 			pr = db.PHOTOTABLE.createRecord();
 			pr.hash = hash;
 			pr.refCount = 1;
 			pr.file = null;
 		} else {
 			changeRef(hash, 1);
 		}
 	}
 	
 	public boolean addFriend(String name) {
 		//TODO write this
 		return false;
 	}
 	
 	public boolean removeFriend(String name) {
 		//TODO write this
 		return false;
 	}
 	
 	public List<File> getUnusedPhotos(){
 		try{
 			List<File> result = new LinkedList<File>();
 			for(PhotoRecord pr : db.PHOTOTABLE.readAll()) {
 				if(pr.refCount == 0) {
 					result.add(pr.file);
 				}
 				db.PHOTOTABLE.delete(pr.hash);
 			}
 			return result;
 		} catch (DB461Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public void fetchPhoto(String name, int photoHash) {
 		if(photoDir == null)
 			throw new IllegalStateException("Set Photo Directory First!");
 		
 		DDNSRRecord rr;
 		try {
 			rr = ((DDNSResolverService)OS.getService("ddnsresolver")).resolve(name);
 			RPCCallerSocket sock = new RPCCallerSocket(rr.host, rr.host, rr.port);
 			JSONObject request = new JSONObject();
 			request.put("photohash", photoHash);
 			JSONObject response = sock.invoke("snet", "fetchUpdates", request);
 			byte[] bitmap = Base64.decode(response.getString("photodata"));
 			
 		} catch (DDNSException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void setPhotoDirectory(File location) {
 		photoDir = location;
 	}
 	
 //	public List<String> getOnlineUsers() {
 //		List<String> names = new LinkedList<String>();
 //		
 //		return names;
 //	}
 	
 }

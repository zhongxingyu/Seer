 package edu.uw.cs.cse461.SNet;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.DB461.DB461.DB461Exception;
 import edu.uw.cs.cse461.DB461.DB461.RecordSet;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.Net.DDNS.DDNSFullName;
 import edu.uw.cs.cse461.Net.DDNS.DDNSFullNameInterface;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.ARecord;
 import edu.uw.cs.cse461.Net.DDNS.DDNSResolverService;
 import edu.uw.cs.cse461.Net.RPC.RPCCall;
 import edu.uw.cs.cse461.Net.RPC.RPCCallableMethod;
 import edu.uw.cs.cse461.Net.RPC.RPCService;
 import edu.uw.cs.cse461.SNet.SNetDB461.CommunityRecord;
 import edu.uw.cs.cse461.SNet.SNetDB461.Photo;
 import edu.uw.cs.cse461.SNet.SNetDB461.PhotoRecord;
 import edu.uw.cs.cse461.util.Base64;
 import edu.uw.cs.cse461.util.Log;
 
 
 /**
  * Handles UI-independent operations on SNetDB
  * 
  * @author zahorjan
  *
  */
 public class SNetController extends NetLoadableService {
 	private static final String TAG="SNetController";
 	private static final int MAX_LENGTH_PHOTO_FETCH = 16*1024;
 	private static final int MAX_PHOTO_XFER_RETRY = 8;
 	/**
 	 * A full path name to the sqlite database.
 	 */
 	private String mDBName;
 
 	private RPCCallableMethod fetchupdates;
 	private RPCCallableMethod fetchphoto;
 	
 	/**
 	 * IMPLEMENTED: Returns the full path name of the DB file.
 	 */
 	public String DBName() {
 		return mDBName;
 	}
 
 	/**
 	 * IMPLEMENTED: Ensures that the root member and the member whose name is the argument
 	 * (usually the host name of this node) both have entries in the community table.
 	 * @param user A user name
 	 * @throws DB461Exception Some unanticipated exception occurred.
 	 */
 	public void registerBaseUsers(DDNSFullName user) throws DB461Exception {
 		SNetDB461 db = null;
 		try {
 			db = new SNetDB461(mDBName);
 			db.registerMember( user );
 			db.registerMember( new DDNSFullName("") );  // and the root
 		} catch (Exception e) {
 			Log.e(TAG, "registerBaseUsers caught exception: " + e.getMessage());
 			throw new DB461Exception("registerUser caught exception: " + e.getMessage());
 		}
 		finally {
 			if ( db != null ) db.discard();
 		}
 	}
 
 	/**
 	 * IMPLEMENTED: Helper function that returns a string representing the community table record in the database
 	 * for the user whose name is the node's hostname.
 	 * @return The community table row, as a String.
 	 */
 	synchronized public String getStatusMsg() {
 		SNetDB461 db = null;
 		StringBuilder sb = new StringBuilder();
 		try {
 			sb.append("Host: ").append(NetBase.theNetBase().hostname()).append("\n");
 			RPCService rpcService = (RPCService)NetBase.theNetBase().getService("rpc");
 			sb.append("Location: ").append(rpcService.localIP()).append(":").append(rpcService.localPort()).append("\n");
 			db = new SNetDB461(mDBName);
 			String memberName = new DDNSFullName(NetBase.theNetBase().hostname()).toString();
 			CommunityRecord member = db.COMMUNITYTABLE.readOne(memberName);
 			if ( member != null ) sb.append(member.toString());
 			else sb.append("No data for member '" + memberName + "'");
 		} catch (Exception e) {
 			Log.e(TAG, "getStatusMsg: caught exception: " + e.getMessage());
 		}
 		finally {
 			if ( db != null ) db.discard();
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Implemented: Checks db for consistency violations and tries to fix  them.
 	 * Consistency requirements:
 	 * &lt;ul&gt;
 	 * &lt;li&gt; Every photo hash in community table should have a photo table entry
 	 * &lt;li&gt; Ref counts should be correct
 	 * &lt;li&gt; If a photo table entry has a file name, the file should exist, and the file's hash should correspond to the photo record key
 	 * &lt;/ul&gt;
 	 * @param galleryDir A File object representing the gallery directory
 	 */
 	synchronized public void fixDB(File galleryDir) throws DB461Exception {
 		SNetDB461 db = null;
 		try {
 			db = new SNetDB461(mDBName);
 			db.checkAndFixDB(galleryDir);
 		} catch (Exception e) {
 			throw new DB461Exception("fixDB caught exception: "+ e.getMessage());
 		}
 		finally {
 			if ( db != null ) db.discard();
 		}
 	}
 
 
 	//////////////////////////////////////////////////////////////////////////////////////
 	// Below here the methods may need implementation, completion, or customization.
 	//////////////////////////////////////////////////////////////////////////////////////
 
 	public SNetController(String dbDirName) throws Exception {
 		super("snet", true);
 		mDBName = dbDirName + "/" + new DDNSFullName(NetBase.theNetBase().hostname()) + "snet.db";
 		
 		// register rpc interface
 		fetchupdates = new RPCCallableMethod(this, "_rpcFetchUpdates");
 		fetchphoto = new RPCCallableMethod(this, "_rpcFetchPhoto");
 
 		RPCService rpcService = (RPCService)NetBase.theNetBase().getService("rpc");
 		if ( rpcService == null) throw new Exception("The SNet requires that the RPC resolver service be loaded");
		rpcService.registerHandler(loadablename(), "fetchUpdates", fetchupdates );
		rpcService.registerHandler(loadablename(), "fetchPhoto", fetchphoto );
 	}
 
 	/**
 	 * OPTIONAL: Provides a web interface displaying status of SNet database.
 	 * @param uriVec The parsed URI argument.  (See HTTPProviderInterface.java.)
 	 * @return Text to return to the browser.
 	 */
 	public String httpServe(String[] uriVec) throws DB461Exception {
 		return "Not implemented";
 	}
 
 	/** IMPLEMENTED:
 	 * Assign a photo that exists as a local file as a member's chosen photo.
 	 * Decrement reference count on any existing chosen photo and delete underyling file if appropriate.
 	 * @param memberName Name of member
 	 * @param filename Full path name to new chosen photo file
 	 * @param galleryDir File object representing the gallery directory
 	 * @throws DB461Exception
 	 */
 	synchronized public void setChosenPhoto(DDNSFullNameInterface memberName, String filename, File galleryDir) throws DB461Exception {
 		setPhoto(memberName, filename, galleryDir, "chosen");
 	}
 
 	/** IMPLEMENTED:
 	 * This method supports the manual setting of a user's generation number.  It is probably not useful
 	 * in building the SNet application per se, but can be useful in debugging tools you might write.
 	 * @param memberName Name of the member whose generation number you want to set
 	 * @param gen The value the generation number should be set to.
 	 * @return The old value of the member's generation number
 	 * @throws DB461Exception  Member doesn't exist in community db, or some unanticipated exception occurred.
 	 */
 	synchronized public int setGenerationNumber(DDNSFullNameInterface memberName, int gen) throws DB461Exception {
 		SNetDB461 db = null;
 		try {
 			db = new SNetDB461(this.DBName());
 			CommunityRecord comRec = db.COMMUNITYTABLE.readOne(memberName.toString());
 			if(comRec == null) {
 				throw new DB461Exception("Member is not in the database: " + memberName.toString());
 			}
 			int oldGen = comRec.generation;
 			comRec.generation = gen;
 			db.COMMUNITYTABLE.write(comRec);
 			return oldGen;
 		} finally {
 			if(db != null)
 				db.discard();
 		}
 	}
 
 	/**IMPLEMENTED:
 	 * Sets friend status of a member.  The member must already be in the DB.
 	 * @param friend  Name of member whose friend status should be set.
 	 * @param isfriend true to make a friend; false to unfriend.
 	 * @throws DB461Exception The member is not in the community table in the DB, or some unanticipated exception occurs.
 	 */
 	synchronized public void setFriend(DDNSFullNameInterface friend, boolean isfriend) throws DB461Exception {
 		SNetDB461 db = null;
 		try {
 			db = new SNetDB461(this.DBName());
 			CommunityRecord friendRec = db.COMMUNITYTABLE.readOne(friend.toString());
 			if(friendRec == null) {
 				throw new DB461Exception("Member is not in the database: " + friend.toString());
 			}
 			friendRec.isFriend = isfriend;
 			db.COMMUNITYTABLE.write(friendRec);
 		} finally {
 			if(db != null)
 				db.discard();
 		}
 	}
 
 	/** IMPLEMENTED:
 	 * Registers a photo as the "my" photo for a given member.
 	 * Decrements the reference count of any existing my photo for that member, and deletes the underyling file for it
 	 * as appropriate.
 	 * @param memberName Member name
 	 * @param filename  Full path name to new my photo file
 	 * @param galleryDir File object describing directory in which gallery photos live
 	 * @throws DB461Exception  Can't find member in community table, or some unanticipated exception occurs.
 	 */
 	synchronized public void newMyPhoto(DDNSFullNameInterface memberName, String filename, File galleryDir) throws DB461Exception {
 		setPhoto(memberName, filename, galleryDir, "my");
 	}
 
 	/**IMPLEMENTED:
 	 * Registers a photo as the "my" or "chosen" photo for a given member.
 	 * Decrements the reference count of any existing my photo for that member, and deletes the underyling file for it
 	 * as appropriate.
 	 * @param memberName Member name
 	 * @param filename  Full path name to new my photo file
 	 * @param galleryDir File object describing directory in which gallery photos live
 	 * @param photoType String indicating either "my" or "chosen" as what is to be updated
 	 * @throws DB461Exception  Can't find member in community table, or some unanticipated exception occurs.
 	 */
 	private void setPhoto(DDNSFullNameInterface memberName, String filename, File galleryDir, String photoType) throws DB461Exception {
 		try {
 			if(!photoType.equals("chosen") && !photoType.equals("my")) {
 				throw new IllegalArgumentException("photo type must be either chosen or my");
 			}
 			
 			File photoFile = new File(filename);
 			Photo photo = new Photo(photoFile);
 			int newPhotoHash = photo.hash();
 			if(newPhotoHash == 0)
 				throw new IOException("File cannot have a hash of 0.");
 			photo = null; //that way a null exception will occur if i try to use any of its methods illegally
 			
 			//if file isn't in the gallery directory, or doesn't have its hash as its name, make a copy of it
 			if(!photoFile.getParentFile().equals(galleryDir) || !photoFile.getName().equals(newPhotoHash)) {
 				photoFile = copyToGallery(photoFile, newPhotoHash+"", galleryDir);
 			}
 			
 			SNetDB461 db = null;
 			int gen;
 			try {
 				db = new SNetDB461(this.DBName());
 				CommunityRecord memRec = db.COMMUNITYTABLE.readOne(memberName.toString());
 				if(memRec == null) {
 					throw new DB461Exception("Member is not in the database: " + memberName.toString());
 				}
 				gen = getGenNum(memRec.generation);
 			} finally {
 				if(db!= null)
 					db.discard();
 			}
 			updatePhotoRecord(memberName, photoFile, newPhotoHash, gen, photoType);
 			/*SNetDB461 db = null;
 			try {
 				db = new SNetDB461(this.DBName());
 				CommunityRecord memRec = db.COMMUNITYTABLE.readOne(memberName.toString());
 				if(memRec == null) {
 					throw new DB461Exception("Member is not in the database: " + memberName.toString());
 				}
 	
 				//OLD PHOTO HANDLING: Decrement reference count on any existing chosen photo and
 				//delete underyling file if appropriate.
 				int oldPhotoHash;
 				if(photoType.equals("chosen"))
 					oldPhotoHash = memRec.chosenPhotoHash;
 				else //my photo
 					oldPhotoHash = memRec.myPhotoHash;
 	
 				if(oldPhotoHash != 0) { //if old photo exists
 					PhotoRecord oldPhotoRec = db.PHOTOTABLE.readOne(oldPhotoHash);
 					if(oldPhotoRec == null) {
 						throw new DB461Exception("Old photo is not in the database. Hash: " + oldPhotoHash);
 					}
 	
 					oldPhotoRec.refCount--;
 					if(oldPhotoRec.refCount <= 0) {
 						if(oldPhotoRec.file != null && oldPhotoRec.file.isFile())
 							oldPhotoRec.file.delete();
 						db.PHOTOTABLE.delete(oldPhotoHash);
 					} else {
 						db.PHOTOTABLE.write(oldPhotoRec);
 					}
 				}
 	
 				//NEW PHOTO HANDLING: if record, doesn't exit, create one; otherwise increment refcount
 				PhotoRecord newPhotoRec = db.PHOTOTABLE.readOne(newPhotoHash);
 				if(newPhotoRec == null) {
 					newPhotoRec = db.createPhotoRecord();
 					newPhotoRec.file = photoFile;
 					newPhotoRec.hash = newPhotoHash;
 					newPhotoRec.refCount = 1;
 				} else {
 					newPhotoRec.refCount++;
 				}
 				db.PHOTOTABLE.write(newPhotoRec);
 	
 				if(photoType.equals("chosen"))
 					memRec.chosenPhotoHash= newPhotoHash;
 				else //my photo
 					memRec.myPhotoHash = newPhotoHash;
 				memRec.generation = getGenNum(memRec.generation);
 				db.COMMUNITYTABLE.write(memRec);
 			} finally {
 				if(db != null)
 					db.discard();
 			}*/
 		} catch(IOException e) {
 			throw new DB461Exception(e.getMessage());
 		}
 	}
 	
 	//helper method for updating the DB table with photo info
 	private void updatePhotoRecord(DDNSFullNameInterface memberName, File newPhotoFile, int newHash, int gen, String photoType) throws DB461Exception {
 		if(!photoType.equals("chosen") && !photoType.equals("my")) {
 			throw new IllegalArgumentException("photo type must be either chosen or my");
 		}
 		
 		SNetDB461 db = null;
 		try {
 			db = new SNetDB461(this.DBName());
 			CommunityRecord memRec = db.COMMUNITYTABLE.readOne(memberName.toString());
 			if(memRec == null) {
 				throw new DB461Exception("Member is not in the database: " + memberName.toString());
 			}
 
 			//OLD PHOTO HANDLING: Decrement reference count on any existing chosen photo and
 			//delete underyling file if appropriate.
 			int oldPhotoHash;
 			if(photoType.equals("chosen"))
 				oldPhotoHash = memRec.chosenPhotoHash;
 			else //my photo
 				oldPhotoHash = memRec.myPhotoHash;
 
 			if(oldPhotoHash != 0) { //if old photo exists
 				PhotoRecord oldPhotoRec = db.PHOTOTABLE.readOne(oldPhotoHash);
 				if(oldPhotoRec == null) {
 					throw new DB461Exception("Old photo is not in the database. Hash: " + oldPhotoHash);
 				}
 
 				oldPhotoRec.refCount--;
 				if(oldPhotoRec.refCount <= 0) {
 					if(oldPhotoRec.file != null && oldPhotoRec.file.isFile())
 						oldPhotoRec.file.delete();
 					db.PHOTOTABLE.delete(oldPhotoHash);
 				} else {
 					db.PHOTOTABLE.write(oldPhotoRec);
 				}
 			}
 
 			//NEW PHOTO HANDLING: if record, doesn't exit, create one; otherwise increment refcount
 			PhotoRecord newPhotoRec = db.PHOTOTABLE.readOne(newHash);
 			if(newPhotoRec == null) {
 				newPhotoRec = db.createPhotoRecord();
 				newPhotoRec.file = newPhotoFile;
 				newPhotoRec.hash = newHash;
 				newPhotoRec.refCount = 1;
 			} else {
 				newPhotoRec.refCount++;
 			}
 			db.PHOTOTABLE.write(newPhotoRec);
 
 			if(photoType.equals("chosen"))
 				memRec.chosenPhotoHash= newHash;
 			else //my photo
 				memRec.myPhotoHash = newHash;
 			memRec.generation = gen;
 			db.COMMUNITYTABLE.write(memRec);
 		} finally {
 			if(db != null)
 				db.discard();
 		}
 	}
 
 	//copies src to the dstDir with the file name newName in the dstDir
 	private static File copyToGallery(File src, String newName, File dstDir) throws FileNotFoundException, IOException {
 		File result = new File(dstDir, newName);
 		if(result.exists() && !result.delete())
 			throw new IOException("Cannot delete already existent file: " + result.getAbsolutePath());
 		result.createNewFile();
 		
 		InputStream in = null;
 		OutputStream out = null;
     	try{
     		in = new FileInputStream(src);
     		out = new FileOutputStream(result);
     		
     		byte[] buffer = new byte[1024];
     		int length;
     	    //copy the file content in bytes 
     	    while ((length = in.read(buffer)) > 0){
     	    	out.write(buffer, 0, length); 
     	    }
     	    return result;
     	} finally {
     		if(in != null)
     			in.close();
     		if(out != null)
     			out.close();
     	}
 	}
 	
 	/**
 	 * The caller side of fetchUpdates.
 	 * @param friend The friend to be contacted
 	 * @param galleryDir The path name to the gallery directory
 	 * @throws DB461Exception Something went wrong...
 	 */
 	synchronized public void fetchUpdatesCaller( String mem, File galleryDir) throws DB461Exception {
 		try {
 			/****************************
 			 * FETCH UPDATES
 			 ****************************/
 			SNetDB461 db = null;
 			try {
 				mem = (new DDNSFullName(mem)).toString();
 				db = new SNetDB461(this.DBName());
 				CommunityRecord memRec = db.COMMUNITYTABLE.readOne(mem);
 				if(memRec == null) {
 					throw new DB461Exception("Member is not in the database: " + mem);
 				}
 			} finally {
 				if (db != null)
 					db.discard();
 			}
 			DDNSResolverService resolver = (DDNSResolverService)NetBase.theNetBase().getService("ddnsresolver");
 			DDNSFullNameInterface myHostname = new DDNSFullName(NetBase.theNetBase().config().getProperty("net.hostname"));
 			String portString = NetBase.theNetBase().config().getProperty("ddns.rootport");
 			if (portString == null) {
 				throw new Exception("No ddns.rootport entry in config file.");
 			}
 			int myPort = Integer.parseInt(portString);
 			resolver.register(myHostname, myPort);
 			ARecord memConnectInfo = resolver.resolve(mem);
 			
 			JSONObject results = RPCCall.invoke(memConnectInfo.ip(), memConnectInfo.port(), "snet", "_rpcFetchUpdates", createFetchUpdatesArgs());
 			
 			/****************************
 			 * PERFORM COMMUNITY UPDATES
 			 ****************************/
 			
 			JSONObject communityUpdates = results.getJSONObject("communityupdates");
 			JSONArray photoUpdates = results.getJSONArray("photoupdates");
 			
 			Iterator comUpdateKeys = communityUpdates.keys();
 			while(comUpdateKeys.hasNext()) {
 				String person = (String) comUpdateKeys.next();
 				JSONObject personInfo = communityUpdates.getJSONObject(person);
 				DDNSFullNameInterface personFullName = new DDNSFullName(person);
 				
 				//get all arguments first just in case something is wrong, an error will be thrown
 				int chosenPhotoHash =  personInfo.getInt("chosenphotohash");
 				int myPhotoHash = personInfo.getInt("myphotohash");
 				int gen =  personInfo.getInt("generation");
 				
 				PhotoRecord myPhoto = null;
 				PhotoRecord chosenPhoto = null;
 				db = null;
 				try {
 					db = new SNetDB461(this.DBName());
 					//if they aren't in the database, put them in it
 					db.registerMember(new DDNSFullName(person));
 					
 					myPhoto = db.PHOTOTABLE.readOne(myPhotoHash);
 					chosenPhoto = db.PHOTOTABLE.readOne(chosenPhotoHash);
 				} finally {
 					if(db != null)
 						db.discard();
 				}
 				
 				updatePhotoRecord(personFullName, myPhoto == null ? null : myPhoto.file, myPhotoHash, gen, "my");
 				updatePhotoRecord(personFullName, chosenPhoto == null ? null : chosenPhoto.file, chosenPhotoHash, gen, "chosen");
 			}
 			
 			/****************************
 			 * PERFORM PHOTO UPDATES
 			 ****************************/
 			
 			for(int i = 0; i < photoUpdates.length(); i++) {
 				int photoHash = photoUpdates.getInt(i);
 				db = null;
 				try {
 					db = new SNetDB461(this.DBName());
 					PhotoRecord photoRec = db.PHOTOTABLE.readOne(photoHash);
 					//if we have the file or don't need the file, then don't do anything for this hash
 					if(photoRec == null || (photoRec.file != null && photoRec.file.isFile()) ) {
 						continue;
 					}
 				} finally {
 					if(db != null)
 						db.discard();
 				}
 				
 				JSONObject fetchPhotoArgs = new JSONObject();
 				fetchPhotoArgs.put("photohash", photoHash);
 				fetchPhotoArgs.put("maxlength", MAX_LENGTH_PHOTO_FETCH);
 				int numBytesRecieved = -1;
 				int offset = 0;
 				File newPhoto = new File(galleryDir, photoHash+"");
 				OutputStream fstream = null;
 				db = null;
 				try {
 					newPhoto.createNewFile();
 					int attempts = 0;
 					fstream = new FileOutputStream(newPhoto);
 					
 					do {
 						try{
 							fetchPhotoArgs.put("offset", offset);
 							JSONObject dataReturned = null;
 							try {
 								dataReturned = RPCCall.invoke(memConnectInfo.ip(), memConnectInfo.port(), "snet", "_rpcFetchUpdates", createFetchUpdatesArgs());
 							} catch(Exception e) {
 								throw new JSONException(e.getMessage());
 							}
 							numBytesRecieved = dataReturned.getInt("length");
 							int offsetRecieved = dataReturned.getInt("offset"); 
 							if(numBytesRecieved < 0 || numBytesRecieved > MAX_LENGTH_PHOTO_FETCH) {
 								throw new JSONException("invalid data size.  bytes recieved must be negative and less than " + MAX_LENGTH_PHOTO_FETCH);
 							} else if(offset != offsetRecieved) {
 								throw new JSONException("invalid offset.  offset must match recieved offset");
 							} else if(numBytesRecieved > 0) {
 								byte[] bytesRecieved = Base64.decode(dataReturned.getString("photodata"));
 								bytesRecieved = Arrays.copyOf(bytesRecieved, numBytesRecieved);
 								fstream.write(bytesRecieved);
 							}
 							attempts = 0;
 						} catch(JSONException e) {
 							if(++attempts > MAX_PHOTO_XFER_RETRY)
 								throw new DB461Exception("could not complete photo transfer");
 						}
 					} while(numBytesRecieved > 0);
 					//if fstream fail, delete file
 					
 					db = new SNetDB461(this.DBName());
 					PhotoRecord photoRec = db.PHOTOTABLE.readOne(photoHash);
 					if(photoRec == null) {
 						throw new DB461Exception("couldn't find the photo record associated with " + photoHash);
 					} else {
 						photoRec.file = newPhoto;
 					}
 					db.PHOTOTABLE.write(photoRec);
 				} catch(Exception e) {
 					if(fstream != null) {
 						try {
 							fstream.close();
 						} catch (IOException e1) {
 						}
 					}
 					if(newPhoto != null)
 						newPhoto.delete();
 					throw e;
 				} finally {
 					if(fstream != null) {
 						try {
 							fstream.close();
 						} catch (IOException e1) {
 						}
 					}
 					if(db != null)
 						db.discard();
 				}
 			}
 		} catch(Exception e) {
 			throw new DB461Exception(e.getMessage());
 		}
 	}
 	
 	private JSONObject createFetchUpdatesArgs() throws DB461Exception {
 		SNetDB461 db = null;
 		try {
 			db = new SNetDB461(this.DBName());
 			JSONObject fetchUpdatesArgs = new JSONObject();
 			JSONObject communityArg = new JSONObject();
 			Set<Integer> needPhotosSet = new HashSet<Integer>();
 			RecordSet<CommunityRecord> community = db.COMMUNITYTABLE.readAll();
 			for(CommunityRecord person : community) {
 				JSONObject personInfo = new JSONObject();
 				try {
 					personInfo.put("generation", person.generation);
 					personInfo.put("myphotohash", person.myPhotoHash);
 					personInfo.put("chosenphotohash", person.chosenPhotoHash);
 					communityArg.put(person.name.toString(), personInfo);
 				} catch(JSONException e) {
 					//shouldn't happen
 				}
 				
 				if(person.isFriend) {
 					String[] photoTypes = {"chosen", "my"};
 					for(String photoType : photoTypes) {
 						int hash;
 						if(photoType.equals("chosen")) {
 							hash = person.chosenPhotoHash;
 						} else { //my
 							hash = person.myPhotoHash;
 						}
 						
 						if(!needPhotosSet.contains(hash)) {
 							PhotoRecord photoRec = db.PHOTOTABLE.readOne(hash);
 							if(photoRec == null)
 								throw new DB461Exception("photorec reference is null, which shouldn't be the case");
 							if(photoRec.file != null && !photoRec.file.exists()) {
 								photoRec.file = null;
 								db.PHOTOTABLE.write(photoRec);
 							}
 							
 							if(photoRec.file == null) {
 								needPhotosSet.add(hash);
 							}
 						}
 					}
 				}
 			}
 			
 			JSONArray needPhotosArg = new JSONArray();
 			for(int photoHash : needPhotosSet) {
 				needPhotosArg.put(photoHash);
 			}
 			
 			try {
 				fetchUpdatesArgs.put("community", communityArg);
 				fetchUpdatesArgs.put("needphotos", needPhotosArg);
 			} catch(JSONException e) {
 				//shouldn't happen
 			}
 			return fetchUpdatesArgs;
 		} finally {
 			if(db != null)
 				db.discard();
 		}
 	}
 
 	/**IMPLEMENTED:
 	 * The callee side of fetchUpdates - invoked via RPC.
 	 * @param args JSONObject containing commmunity JSONObject and needphotos JSONArray (described in assignment)
 	 * @return JSONObject containing communityupdates JSONObject and photoupdates JSONArray (described in assignment)
 	 */
 	synchronized public JSONObject fetchUpdatesCallee(JSONObject args) throws Exception {
 		SNetDB461 db = null;
 		try {
 			JSONObject result = new JSONObject();
 			JSONObject community = args.getJSONObject("community");
 			JSONArray needPhotos = args.getJSONArray("needphotos");
 			db = new SNetDB461(this.DBName());
 
 			//community updates returns all member info that i know about that 
 			//is more up to date than what they know about
 			JSONObject communityUpdates = new JSONObject();
 			if(community != null) {
 				Iterator iter = community.keys();
 				while(iter.hasNext()) {
 					String memName = (String) iter.next();
 					CommunityRecord memInfoOurs = db.COMMUNITYTABLE.readOne((new DDNSFullName(memName)).toString());
 
 					if(memInfoOurs != null) {
 						JSONObject memInfoTheirs = community.getJSONObject(memName);
 						int memGenNumTheirs = memInfoTheirs.getInt("generation");
 
 						if(memInfoOurs.generation > memGenNumTheirs) {
 							JSONObject memInfoPut = new JSONObject();
 							memInfoPut.put("generation", memInfoOurs.generation);
 							memInfoPut.put("myphotohash", memInfoOurs.myPhotoHash);
 							memInfoPut.put("chosenphotohash", memInfoOurs.chosenPhotoHash);
 							communityUpdates.put(memName, memInfoPut);
 						}
 					}
 				}
 			}
 
 			//return all photos that i have that are in the need photo argument
 			JSONArray photoUpdates = new JSONArray();
 			if(needPhotos != null) {
 				for(int i = 0; i < needPhotos.length(); i++) {
 					int photoHash = needPhotos.getInt(i);
 					PhotoRecord photoOurs = db.PHOTOTABLE.readOne(photoHash);
 
 					if(photoOurs != null && photoOurs.file != null && photoOurs.file.isFile()) {
 						photoUpdates.put(photoHash);
 					}
 				}
 			}
 
 			result.put("communityupdates", communityUpdates);
 			result.put("photoupdates", photoUpdates);
 			return result;
 		} finally {
 			if(db != null)
 				db.discard();
 		}
 	}	
 
 	/**IMPLEMENTED:
 	 * Callee side of fetchPhoto (fetch one photo).  To fetch an image file, call this
 	 * method repeatedly, starting at offset 0 and incrementing by the returned length each
 	 * subsequent call.  Repeat until a length of 0 comes back.
 	 * @param args {photoHash: int, maxlength: int, offset: int}
 	 * @return {photoHash: int, photoData: String (base64 encoded byte[]), length: int, offset: int}.  The photoData field may
 	 *     not exist if the length is 0.
 	 * @throws Exception
 	 */
 	synchronized public JSONObject fetchPhotoCallee(JSONObject args) throws Exception { 
 		SNetDB461 db = null;
 		FileInputStream fstream = null;
 		try {
 			JSONObject result = new JSONObject();
 			int photoHash = args.getInt("photohash");
 			int maxLength = args.getInt("maxlength");
 			int offset = args.getInt("offset");
 			db = new SNetDB461(this.DBName());
 			PhotoRecord pr = db.PHOTOTABLE.readOne(photoHash);
 			if(pr == null) {
 				throw new DB461Exception("Photo not found in database.  PhotoHash: " + photoHash);
 			}
 
 			File photoFile = pr.file;
 			fstream = new FileInputStream(photoFile); //possibly make more efficient
 			byte[] photoData = new byte[maxLength];
 			int bytesRead = fstream.read(photoData, offset, maxLength);
 			if(bytesRead == -1) {
 				result.put("photodata", (String) null);
 				result.put("photohash",  pr.hash);
 				result.put("length", 0);
 				result.put("offset", offset);
 			} else {
 				result.put("photodata", Base64.encodeBytes(photoData, 0, bytesRead));
 				result.put("photohash", pr.hash);
 				result.put("length", bytesRead);
 				result.put("offset", offset);
 			}
 			return result;
 		} finally {
 			if(db != null) {
 				db.discard();
 			}
 
 			if(fstream != null) {
 				try {
 					fstream.close();
 				} catch (IOException e) {
 					//won't happen
 				}
 			}
 		}
 	}
 
 	private static int getGenNum(int oldGenNum) {
 		return Math.max(oldGenNum+1, (int) NetBase.theNetBase().now());
 	}
 
 	@Override
 	public String dumpState() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }

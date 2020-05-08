 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal.service;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.Header;
 import org.apache.http.message.BasicHeader;
 
 import android.content.ContentQueryMap;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.util.Log;
 
 import com.morphoss.acal.Constants;
 import com.morphoss.acal.DatabaseChangedEvent;
 import com.morphoss.acal.HashCodeUtil;
 import com.morphoss.acal.acaltime.AcalDateTime;
 import com.morphoss.acal.database.AcalDBHelper;
 import com.morphoss.acal.providers.DavCollections;
 import com.morphoss.acal.providers.DavResources;
 import com.morphoss.acal.providers.Servers;
 import com.morphoss.acal.service.SynchronisationJobs.WriteActions;
 import com.morphoss.acal.service.connector.AcalRequestor;
 import com.morphoss.acal.xml.DavNode;
 
 public class InitialCollectionSync extends ServiceJob {
 
 	private int collectionId = -1;
 	private int serverId = -1;
 	private String collectionPath = null;
 	private boolean isCollectionIdAssigned = false;
 	private boolean collectionNeedsSync = false;
 
 	private AcalRequestor 		requestor;
 	
 	private static final String TAG = "aCal InitialCollectionSync";
 	private ContentResolver cr;
 	public static final int MAX_RESULTS = 100;
 
 	private Header[] syncHeaders = new Header[] {
 			new BasicHeader("Depth","1"),
 			new BasicHeader("Content-Type","text/xml; encoding=UTF-8")
 	};
 	
 	private final String syncData = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
 										"<sync-collection xmlns=\"DAV:\">"+
 											"<sync-token/>"+
 											"<prop>"+
 												"<getetag/>"+
 											"</prop>"+
 /**											"<limit>"+
 												"<nresults>"+MAX_RESULTS+"</nresults>"+
 											"</limit>"+
 */
 										"</sync-collection>";
 	private aCalService	context;
 												
 
 
 	public InitialCollectionSync (int collectionId ) {
 		this.collectionId = collectionId;
 		this.isCollectionIdAssigned = true;
 		collectionPath = null;
 	}
 	
 	public InitialCollectionSync (int collectionId, int serverId, String collectionPath) {
 		this.collectionId = collectionId;
 		this.serverId = serverId;
 		this.collectionPath = collectionPath;
 		this.isCollectionIdAssigned = true;
 	}
 
 	public InitialCollectionSync ( int serverId, String collectionPath) {
 		this.serverId = serverId;
 		this.collectionPath = collectionPath;
 	}
 	
 	
 	@Override
 	public void run(aCalService context) {
 		this.context = context;
 		this.cr = context.getContentResolver();
 		
 		if ( !getCollectionId() ) return;
 		
 		collectionNeedsSync = false;
 
 		if (Constants.LOG_DEBUG) Log.d(TAG, "Starting initial sync process for server " + serverId + ", Collection: " + collectionPath);
 		ContentValues serverData;
 		try {
 			// get serverData
			serverData = SynchronisationJobs.getServerData(serverId, cr);
 			if (serverData == null) throw new Exception("No record for ID " + serverId);
 			requestor = AcalRequestor.fromServerValues(serverData);
 			requestor.setPath(collectionPath);
 		}
 		catch (Exception e) {
 			// Error getting data
			Log.e(TAG, "Error getting server data from DB: " + e.getMessage());
 			return;
 		}
 		
 		if ( null == serverData.getAsInteger(Servers.HAS_SYNC) || 0 == serverData.getAsInteger(Servers.HAS_SYNC) ) {
 			Log.i(TAG, "Skipping initial sync process since server does not support WebDAV synchronisation");
 			collectionNeedsSync = true;
 		}
 		else {
 	
 			DavNode root = requestor.doXmlRequest("REPORT", collectionPath, syncHeaders, syncData);
 			if (requestor.getStatusCode() == 404) {
 				Log.i(TAG, "Sync REPORT got 404 on " + collectionPath + " so a HomeSetsUpdate is being scheduled.");
 				ServiceJob sj = new HomeSetsUpdate(serverId);
 				context.addWorkerJob(sj);
 				return;
 			}
 			if ( root == null ) {
 				Log.w(TAG, "Unable to do intial sync - no XML data from server.");
 				collectionNeedsSync = true;
 			}
 			else {
 				processSyncToDatabase(root);
 			}
 		}
 
 		// Now schedule a sync contents on this.
 		context.addWorkerJob(new SyncCollectionContents(collectionId,collectionNeedsSync));
 	}
 	
 
 	private boolean getCollectionId() {
 		Cursor cursor = null;
 		try {
 			if ( collectionPath == null ) {
 				cursor = cr.query(Uri.withAppendedPath(DavCollections.CONTENT_URI,Integer.toString(collectionId)),
 							new String[] { DavCollections.SERVER_ID, DavCollections.COLLECTION_PATH }, 
 							null, null, null);
 				if ( cursor.moveToFirst() ) {
 					isCollectionIdAssigned = true;
 					serverId = cursor.getInt(cursor.getColumnIndex(DavCollections.SERVER_ID));
 					collectionPath = cursor.getString(cursor.getColumnIndex(DavCollections.COLLECTION_PATH));
 				}
 				else {
 					Log.e(TAG,"Cannot find collection ID "+collectionId+" which I should sync!");
 				}
 			}
 			else if ( serverId > 0 && collectionId < 0 ) {
 				cursor = cr.query(DavCollections.CONTENT_URI, new String[] { DavCollections._ID }, 
 							DavCollections.SERVER_ID + "=? AND " + DavCollections.COLLECTION_PATH + "=?",
 							new String[] { "" + serverId, collectionPath }, null);
 				if ( cursor.moveToFirst() ) {
 					isCollectionIdAssigned = true;
 					collectionId = cursor.getInt(cursor.getColumnIndex(DavCollections._ID));
 				}
 				else {
 					Log.e(TAG,"Cannot find collection "+collectionPath+" which I should sync!");
 				}
 			}
 			else if ( collectionId < 0 ) {
 				Log.e(TAG,"Cannot find collection which I should sync!");
 				throw new IllegalStateException();
 			}
 		}
 		catch ( Exception e ) {
 			Log.e(TAG,"Error finding "+collectionPath+" in database: " + e.getMessage());
 			Log.e(TAG,Log.getStackTraceString(e));
 		}
 		finally {
 			if (cursor != null) cursor.close();
 		}
 		return ( isCollectionIdAssigned && collectionPath != null );
 	}
 
 	
 	public void processSyncToDatabase( DavNode root ) {
 		AcalDBHelper dbHelper = new AcalDBHelper(this.context);
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		Cursor resourceCursor;
 		try {
 		
 			// begin transaction
 			db.beginTransaction();
 			if (Constants.LOG_VERBOSE) Log.v(TAG, "DavResources DB Transaction started.");
 
 			// Get a map of all existing records where Name is the key.
 			resourceCursor = db.query(DavResources.DATABASE_TABLE,
 						new String[] { DavResources.RESOURCE_NAME, DavResources._ID, DavResources.ETAG },
 						DavResources.COLLECTION_ID+"=?", new String[] { Integer.toString(collectionId) },
 						null, null, null);
 			resourceCursor.moveToFirst();
 			ContentQueryMap cqm = new ContentQueryMap(resourceCursor, DavResources.RESOURCE_NAME, false, null);
 			cqm.requery();
 			Map<String, ContentValues> databaseList = cqm.getRows();
 			cqm.close();			
 			resourceCursor.close();
 
 			Map<String, ContentValues> serverList = new HashMap<String,ContentValues>();
 			List<DavNode> responseList = root.getNodesFromPath("multistatus/response");
 
 			if ( Constants.LOG_VERBOSE ) {
 				Log.v(TAG,"Database list has "+databaseList.size()+" rows.");
 				Log.v(TAG,"Response list has "+responseList.size()+" rows.");
 			}
 			
 			// This could potentially be a really big list of small response sections, so
 			// we allocate variables here and re-use inside loop for performance.
 			List<DavNode> propstats;
 			String name;
 			String etag;
 
 			//iterate through each response and add to serverList
 			for (DavNode response : responseList) {
 				//get each prop
 				propstats = response.getNodesFromPath("propstat");
 				for (DavNode propstat : propstats) {
 					if ( !propstat.getFirstNodeText("status").equalsIgnoreCase("HTTP/1.1 200 OK") ) continue;
 					
 					name = response.segmentFromFirstHref("href");
 					etag = propstat.getFirstNodeText("prop/getetag");
 
 					ContentValues cv = new ContentValues();
 					if ( name != null ) cv.put(DavResources.RESOURCE_NAME, name);
 					if ( etag != null ) cv.put(DavResources.ETAG, etag);
 					serverList.put(name, cv);
 				}
 				//Remove subtree to free up memory
 				root.removeSubTree(response);
 			}
 			//Remove all duplicates
 			removeDuplicates(databaseList, serverList);
 			
 			//Delete those that have been removed
 			int deleted = deleteRecords(db, databaseList);
 			int updated = updateRecords(db, serverList);
 			if (Constants.LOG_VERBOSE) Log.v(TAG, deleted + " records deleted, " + updated + " updated");			
 			
 			if ( root != null ) {
 				//Update sync token
 				ContentValues cv = new ContentValues();
 				String syncToken = root.getFirstNodeText("multistatus/sync-token");
 				cv.put(DavCollections.SYNC_TOKEN, syncToken);
 				db.update(DavCollections.DATABASE_TABLE, cv, DavCollections._ID+"=?", new String[] {Integer.toString(collectionId)});
 			}
 				
 			//We can now approve the transaction
 			db.setTransactionSuccessful();
 			db.endTransaction();
 			db.close();
 		}
 		catch (Exception e) {
 			Log.w(TAG, "Initial Sync transaction failed. Data not will not be saved.");
 			Log.e(TAG,Log.getStackTraceString(e));
 			db.endTransaction();
 			db.close();
 		}
 
 		//lastly, create new regular sync
 		context.addWorkerJob(new SyncCollectionContents(this.collectionId)); 
 	}
 	
 	public boolean equals(Object that) {
 		if ( this == that ) return true;
 	    if ( !(that instanceof InitialCollectionSync) ) return false;
 	    InitialCollectionSync thatCis = (InitialCollectionSync)that;
 	    return (
 	    	this.collectionPath.equals(thatCis.collectionPath) &&
 	    	this.serverId == thatCis.serverId
 	    	);
 		
 	}
 	
 	public int hashCode() {
 		int result = HashCodeUtil.SEED;
 		result = HashCodeUtil.hash( result, this.serverId );
 	    result = HashCodeUtil.hash( result, this.collectionPath );
 	    return result;
 	}
 	
 	//Create a sync job for ALL active collections
 	public static void initialiseAll(WorkerClass worker, Context c) {
 		ContentResolver cr = c.getContentResolver();
 		String lastSyncString;
 		AcalDateTime lastSync;
 		int collectionId;
 		long timeOfFirst = System.currentTimeMillis();
 		Cursor mCursor = cr.query(	DavCollections.CONTENT_URI,
 									new String[] { 
 											DavCollections._ID,
 											DavCollections.SERVER_ID, 
 											DavCollections.COLLECTION_PATH,
 											DavCollections.ACTIVE_ADDRESSBOOK,
 											DavCollections.ACTIVE_EVENTS,
 											DavCollections.ACTIVE_JOURNAL,
 											DavCollections.ACTIVE_TASKS,
 											DavCollections.LAST_SYNCHRONISED },
 									null, null, null);
 		mCursor.moveToFirst();
 		while (!mCursor.isAfterLast()) {
 			//Only add if there is at least ONE active componant
 			if (	mCursor.getInt(mCursor.getColumnIndex(DavCollections.ACTIVE_ADDRESSBOOK)) == 1 ||
 					mCursor.getInt(mCursor.getColumnIndex(DavCollections.ACTIVE_EVENTS)) == 1 ||
 					mCursor.getInt(mCursor.getColumnIndex(DavCollections.ACTIVE_JOURNAL)) == 1 ||
 					mCursor.getInt(mCursor.getColumnIndex(DavCollections.ACTIVE_TASKS)) == 1) {
 				collectionId = mCursor.getInt(mCursor.getColumnIndex(DavCollections._ID));
 				lastSyncString = mCursor.getString(mCursor.getColumnIndex(DavCollections.LAST_SYNCHRONISED));
 				if ( lastSyncString != null ) {
 					lastSync = AcalDateTime.fromString(lastSyncString);
 					if ( AcalDateTime.addDays(lastSync, 14).getMillis() > System.currentTimeMillis() ) {
 						// In this case we will schedule a normal sync on the collection
 						// which will hopefully be *much* lighter weight.
 						SyncCollectionContents job = new SyncCollectionContents(collectionId);
 						job.TIME_TO_EXECUTE = timeOfFirst;
 						worker.addJobAndWake(job);
 						timeOfFirst += 500;
 						mCursor.moveToNext();
 						continue;
 					}
 				}
 				InitialCollectionSync job = new InitialCollectionSync(
 						mCursor.getInt(mCursor.getColumnIndex(DavCollections._ID)),
 						mCursor.getInt(mCursor.getColumnIndex(DavCollections.SERVER_ID)),
 						mCursor.getString(mCursor.getColumnIndex(DavCollections.COLLECTION_PATH)));
 				job.TIME_TO_EXECUTE = timeOfFirst;
 				worker.addJobAndWake(job);
 				timeOfFirst += 500;
 			}
 			mCursor.moveToNext();
 		}
 		mCursor.close();
 		
 	}
 	
 	private void removeDuplicates(Map<String,ContentValues> db, Map<String,ContentValues> server) {
 		String[] names = new String[server.size()];
 		server.keySet().toArray(names);
 		for (String name : names) {
 			if (!db.containsKey(name)) continue;	//New value from server
 			if (db.get(name).getAsString(DavResources.ETAG).equals(server.get(name).getAsString(DavResources.ETAG)))  { //records match, remove from both
 				server.remove(name);
 			} else {
 				server.get(name).put(DavResources._ID, db.get(name).getAsString(DavResources._ID));	//record to be updated. Insert ID
 			}
 			db.remove(name);
 		}
 	}
 	
 	public int deleteRecords(SQLiteDatabase db, Map<String,ContentValues> toDelete) {
 		//database list now contains all the records we need to delete
 		String delIds = "";
 		boolean sep = false;
 		String names[] = new String[toDelete.size()];
 		toDelete.keySet().toArray(names);
 		for (String name : names) {
 			if (sep)delIds+=",";
 			else sep=true;
 			delIds+=toDelete.get(name).getAsInteger(DavResources._ID);
 
 			aCalService.databaseDispatcher.dispatchEvent(new DatabaseChangedEvent(
 						DatabaseChangedEvent.DATABASE_RECORD_DELETED, DavResources.class, toDelete.get(name)));
 		}
 		
 		if (delIds == null || delIds.equals("")) return 0;
 		
 		//remove from db
 		return db.delete( DavResources.DATABASE_TABLE, DavResources._ID+" IN ("+delIds+")", new String[] {  });
 	}
 	
 	public int updateRecords(SQLiteDatabase db, Map<String, ContentValues> toUpdate) {
 		String names[] = new String[toUpdate.size()];
 		toUpdate.keySet().toArray(names);
 		try {
 			for (String name : names) {
 				ContentValues cv = toUpdate.get(name);
 				cv.put(DavResources.NEEDS_SYNC, 1);
 				cv.put(DavResources.COLLECTION_ID, this.collectionId);
 				// is this an update or insert?
 				String id = cv.getAsString(DavResources._ID);
 				WriteActions action;
 				if (id == null || id.equals("")) {
 					action = WriteActions.INSERT;
 				}
 				else {
 					action = WriteActions.UPDATE;
 				}
 				SynchronisationJobs.writeResource(db,action,cv);
 				collectionNeedsSync = true;
 			}
 		}
 		catch ( Exception e ) { }
 		return names.length;
 	}
 
 	@Override
 	public String getDescription() {
 		return "Initial collection sync on "
 					+(serverId>-1?"server "+serverId:"collection "+collectionId)
 					+(collectionPath==null?"":", path "+collectionPath);
 	}
 
 }

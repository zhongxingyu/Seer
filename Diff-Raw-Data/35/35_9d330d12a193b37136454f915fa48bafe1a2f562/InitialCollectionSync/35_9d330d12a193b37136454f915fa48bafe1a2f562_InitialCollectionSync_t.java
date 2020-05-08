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
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 
 import com.morphoss.acal.HashCodeUtil;
 import com.morphoss.acal.database.resourcesmanager.ResourceManager;
 import com.morphoss.acal.database.resourcesmanager.requests.RRInitialCollectionSync;
 
 public class InitialCollectionSync extends ServiceJob {
 
 	private int collectionId = -2;
 	private int serverId = -2;
 	private String collectionPath = null;
 	ContentValues collectionValues = null;
 	private boolean isCollectionIdAssigned = false;
 	private boolean collectionNeedsSync = false;
 
 	
 	
 	private static final String TAG = "aCal InitialCollectionSync";
 	private ContentResolver cr;
 	public static final int MAX_RESULTS = 100;
 
 	
 
 	
 	private RRInitialCollectionSync request;
 
 	public InitialCollectionSync (int collectionId ) {
 		request = new RRInitialCollectionSync(collectionId);
 		
 	}
 	
 	public InitialCollectionSync (int collectionId, int serverId, String collectionPath) {
 		request = new RRInitialCollectionSync(collectionId, serverId, collectionPath);
 	}
 
 	public InitialCollectionSync ( int serverId, String collectionPath) {
 		request = new RRInitialCollectionSync(serverId, collectionPath);
 	}
 	
 	
 	@Override
 	public void run(aCalService context) {
 		request.setService(context);
 		ResourceManager rm = ResourceManager.getInstance(context);
 		//send request
 		rm.sendRequest(request);
 		//block until response completed
		while (!request.isProcessed()) {
 			Thread.yield();
 			try { Thread.sleep(100); } catch (Exception e) {}
 		}
 	}
 	
 	public boolean equals(Object that) {
 		if ( this == that ) return true;
 	    if ( !(that instanceof InitialCollectionSync) ) return false;
 	    InitialCollectionSync thatCis = (InitialCollectionSync)that;
 	    return (this.request == thatCis.request);
 	}
 	
 	public int hashCode() {
 		int result = HashCodeUtil.SEED;
 		result = HashCodeUtil.hash( result, this.serverId );
 	    result = HashCodeUtil.hash( result, this.collectionPath );
 	    return result;
 	}
 	
 	
 	/**
 	private int deleteRecords(SQLiteDatabase db, Map<String,ContentValues> toDelete) {
 		//database list now contains all the records we need to delete
 		String delIds = "";
 		boolean sep = false;
 		String names[] = new String[toDelete.size()];
 		toDelete.keySet().toArray(names);
 		for (String name : names) {
 			if (sep)delIds+=",";
 			else sep=true;
 			delIds+=toDelete.get(name).getAsInteger(OldDavResources._ID);
 
 			aCalService.databaseDispatcher.dispatchEvent(new DatabaseChangedEvent(
 						DatabaseChangedEvent.DATABASE_RECORD_DELETED, OldDavResources.class, toDelete.get(name)));
 		}
 		
 		if (delIds == null || delIds.equals("")) return 0;
 		
 		//remove from db
 		return db.delete( OldDavResources.DATABASE_TABLE, OldDavResources._ID+" IN ("+delIds+")", new String[] {  });
 	}
 */
 	@Override
 	public String getDescription() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 /*
 	public int updateRecords(SQLiteDatabase db, Map<String, ContentValues> toUpdate) {
 		String names[] = new String[toUpdate.size()];
 		toUpdate.keySet().toArray(names);
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
 			db.yieldIfContendedSafely();
 		}
 		return names.length;
 	}
 */
 	
 
 
 }

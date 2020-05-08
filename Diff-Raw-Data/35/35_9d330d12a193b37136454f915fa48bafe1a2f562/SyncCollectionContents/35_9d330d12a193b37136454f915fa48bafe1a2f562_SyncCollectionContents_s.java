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
 
 import com.morphoss.acal.HashCodeUtil;
 import com.morphoss.acal.database.resourcesmanager.ResourceManager;
 import com.morphoss.acal.database.resourcesmanager.requests.RRSyncCollectionContents;
 
 public class SyncCollectionContents extends ServiceJob {
 
 	public static final String	TAG					= "aCal SyncCollectionContents";
 	private RRSyncCollectionContents request;
 	private long collectionId;
 	
 	/**
 	 * <p>
 	 * Constructor
 	 * </p>
 	 * 
 	 * @param collectionId2
 	 *            <p>
 	 *            The ID of the collection to be synced
 	 *            </p>
 	 * @param context
 	 *            <p>
 	 *            The context to use for all those things contexts are used for.
 	 *            </p>
 	 */
 	public SyncCollectionContents(long collectionId) {
 		this.collectionId = collectionId;
 		request = new RRSyncCollectionContents(collectionId);
 		this.TIME_TO_EXECUTE = 0;
 	}
 
 
 	/**
 	 * <p>
 	 * Schedule a sync of the contents of a collection, potentially forcing it to happen now even
 	 * if this would otherwise be considered too early according to the normal schedule.
 	 * </p>  
 	 * @param collectionId
 	 * @param forceSync
 	 */
 	public SyncCollectionContents(int collectionId, boolean forceSync ) {
 		this.collectionId = collectionId;
 		request = new RRSyncCollectionContents(collectionId, forceSync);
 		this.TIME_TO_EXECUTE = 0;
 	}
 
 	
 	@Override
 	public void run(aCalService context) {
 		request.setService(context);
 		ResourceManager rm = ResourceManager.getInstance(context);
 		//send request
 		rm.sendRequest(request);
 		//block until response completed
		while (request.isRunning()) {
 			Thread.yield();
 			try { Thread.sleep(100); } catch (Exception e) {}
 		}
 		if (request.doScheduleNextInstance()) {
 			this.TIME_TO_EXECUTE = request.getTimeToExecute();
 			context.addWorkerJob(this);
 		}
 	}
 
 
 	public boolean equals(Object that) {
 		if (this == that) return true;
 		if (!(that instanceof SyncCollectionContents)) return false;
 		SyncCollectionContents thatCis = (SyncCollectionContents) that;
 		return this.collectionId == thatCis.collectionId;
 	}
 
 	public int hashCode() {
 		int result = HashCodeUtil.SEED;
 		result = HashCodeUtil.hash(result, this.collectionId);
 		return result;
 	}
 
 	@Override
 	public String getDescription() {
 		return "Syncing collection contents of collection " + collectionId;
 	}
 }

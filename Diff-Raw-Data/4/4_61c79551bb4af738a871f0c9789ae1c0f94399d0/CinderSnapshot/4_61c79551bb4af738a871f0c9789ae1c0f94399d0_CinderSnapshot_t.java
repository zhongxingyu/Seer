 /**
  * Copyright (C) 2009-2013 Dell, Inc.
  * See annotations for authorship information
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 package org.dasein.cloud.openstack.nova.os.compute;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudErrorType;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.compute.AbstractSnapshotSupport;
 import org.dasein.cloud.compute.Snapshot;
 import org.dasein.cloud.compute.SnapshotCreateOptions;
 import org.dasein.cloud.compute.SnapshotFilterOptions;
 import org.dasein.cloud.compute.SnapshotState;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.openstack.nova.os.NovaMethod;
 import org.dasein.cloud.openstack.nova.os.NovaOpenStack;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.util.CalendarWrapper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 
 /**
  * Implements support for snapshots from the OpenStack Cinder API.
  * <p>Created by George Reese: 10/25/12 9:27 AM</p>
  * @author George Reese
  * @version 2012.09.1 copied from the HP block storage support
  * @version 2013.02 implement Dasein Cloud 2013.02 model changes
  * @version 2013.04 implemented new heirarchy for snapshot support
  * @since 2012.09.1
  */
 public class CinderSnapshot extends AbstractSnapshotSupport {
     static private final Logger logger = NovaOpenStack.getLogger(CinderSnapshot.class, "std");
 
     static public final String SERVICE  = "volume";
 
     public CinderSnapshot(NovaOpenStack provider) {
         super(provider);
     }
 
     private @Nonnull String getTenantId() throws CloudException, InternalException {
         return ((NovaOpenStack)getProvider()).getAuthenticationContext().getTenantId();
     }
 
     private @Nonnull String getResource() {
        // hp seems to be used the regular grizzly resource
        // return (((NovaOpenStack)getProvider()).isHP() ? "/os-snapshots" : "/snapshots");
        return "/snapshots";
     }
 
     @Override
     public @Nonnull String createSnapshot(@Nonnull SnapshotCreateOptions options) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Snapshot.createSnapshot");
         try {
             String volumeId = options.getVolumeId();
 
             if( volumeId == null ) {
                 throw new OperationNotSupportedException("Snapshot copying is not supported in " + getProvider().getCloudName());
             }
 
             HashMap<String,Object> wrapper = new HashMap<String,Object>();
             HashMap<String,Object> json = new HashMap<String,Object>();
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             json.put("volume_id", volumeId);
             json.put("display_name", options.getName());
             json.put("display_description", options.getDescription());
             json.put("force", "True");
             wrapper.put("snapshot", json);
             JSONObject result = method.postString(SERVICE, getResource(), null, new JSONObject(wrapper), true);
 
             if( result != null && result.has("snapshot") ) {
                 try {
                     Snapshot snapshot = toSnapshot(result.getJSONObject("snapshot"));
 
                     if( snapshot != null ) {
                         return snapshot.getProviderSnapshotId();
                     }
                 }
                 catch( JSONException e ) {
                     logger.error("create(): Unable to understand create response: " + e.getMessage());
                     if( logger.isTraceEnabled() ) {
                         e.printStackTrace();
                     }
                     throw new CloudException(e);
                 }
             }
             logger.error("snapshot(): No snapshot was created by the create attempt, and no error was returned");
             throw new CloudException("No snapshot was created");
 
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForSnapshot(@Nonnull Locale locale) {
         return "snapshot";
     }
 
     @Override
     public @Nullable Snapshot getSnapshot(@Nonnull String snapshotId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Snapshot.getSnapshot");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             JSONObject ob = method.getResource(SERVICE, getResource(), snapshotId, true);
 
             if( ob == null ) {
                 return null;
             }
             try {
                 if( ob.has("snapshot") ) {
                     return toSnapshot(ob.getJSONObject("snapshot"));
                 }
             }
             catch( JSONException e ) {
                 logger.error("getSnapshot(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for snapshot");
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Requirement identifyAttachmentRequirement() throws InternalException, CloudException {
         return Requirement.OPTIONAL;
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listSnapshotStatus() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Snapshot.listSnapshotStatus");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             ArrayList<ResourceStatus> snapshots = new ArrayList<ResourceStatus>();
 
             JSONObject json = method.getResource(SERVICE, getResource(), null, false);
 
             if( json != null && json.has("snapshots") ) {
                 try {
                     JSONArray list = json.getJSONArray("snapshots");
 
                     for( int i=0; i<list.length(); i++ ) {
                         ResourceStatus snapshot = toStatus(list.getJSONObject(i));
 
                         if( snapshot != null ) {
                             snapshots.add(snapshot);
                         }
                     }
                 }
                 catch( JSONException e ) {
                     throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for snapshots in " + json.toString());
                 }
             }
             return snapshots;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean isPublic(@Nonnull String snapshotId) throws InternalException, CloudException {
         return false;
     }
 
     @Override
     public boolean isSubscribed() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Snapshot.isSubscribed");
         try {
             return (((NovaOpenStack)getProvider()).getAuthenticationContext().getServiceUrl(SERVICE) != null);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<Snapshot> listSnapshots() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Snapshot.listSnapshots");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();
 
             JSONObject json = method.getResource(SERVICE, getResource(), null, false);
 
             if( json != null && json.has("snapshots") ) {
                 try {
                     JSONArray list = json.getJSONArray("snapshots");
 
                     for( int i=0; i<list.length(); i++ ) {
                         Snapshot snapshot = toSnapshot(list.getJSONObject(i));
 
                         if( snapshot != null ) {
                             snapshots.add(snapshot);
                         }
                     }
                 }
                 catch( JSONException e ) {
                     logger.error("listSnapshots(): Unable to identify expected values in JSON: " + e.getMessage());
                     e.printStackTrace();
                     throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for snapshots in " + json.toString());
                 }
             }
             return snapshots;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void remove(@Nonnull String snapshotId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Snapshot.remove");
         try {
             long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 15L);
 
             while( System.currentTimeMillis() < timeout ) {
                 try {
                     Snapshot s = getSnapshot(snapshotId);
 
                     if( s == null || s.getCurrentState().equals(SnapshotState.DELETED) ) {
                         return;
                     }
                     if( s.getCurrentState().equals(SnapshotState.AVAILABLE) ) {
                         break;
                     }
                 }
                 catch( Throwable ignore ) {
                     // ignore
                 }
                 try { Thread.sleep(15000L); }
                 catch( InterruptedException ignore ) { }
             }
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
 
             method.deleteResource(SERVICE, getResource(), snapshotId, null);
             timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 5L);
             while( System.currentTimeMillis() < timeout ) {
                 try {
                     Snapshot s = getSnapshot(snapshotId);
 
                     if( s == null || s.getCurrentState().equals(SnapshotState.DELETED) ) {
                         return;
                     }
                 }
                 catch( Throwable ignore ) {
                     // ignore
                 }
                 try { Thread.sleep(15000L); }
                 catch( InterruptedException ignore ) { }
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<Snapshot> searchSnapshots(@Nonnull SnapshotFilterOptions options) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Snapshot.searchSnapshots");
         try {
             NovaMethod method = new NovaMethod((NovaOpenStack)getProvider());
             ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();
 
             JSONObject json = method.getResource(SERVICE, getResource(), null, false);
 
             if( json != null && json.has("snapshots") ) {
                 try {
                     JSONArray list = json.getJSONArray("snapshots");
 
                     for( int i=0; i<list.length(); i++ ) {
                         Snapshot snapshot = toSnapshot(list.getJSONObject(i));
 
                         if( snapshot != null && options.matches(snapshot, null) ) {
                             snapshots.add(snapshot);
                         }
                     }
                 }
                 catch( JSONException e ) {
                     logger.error("searchSnapshots(): Unable to identify expected values in JSON: " + e.getMessage());
                     e.printStackTrace();
                     throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for snapshots in " + json.toString());
                 }
             }
             return snapshots;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean supportsSnapshotCreation() throws CloudException, InternalException {
         return true;
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
     private @Nullable Snapshot toSnapshot(@Nullable JSONObject json) throws CloudException, InternalException {
         if( json == null ) {
             return null;
         }
 
         try {
             String snapshotId = (json.has("id") ? json.getString("id") : null);
 
             if( snapshotId == null ) {
                 return null;
             }
 
             String regionId = getContext().getRegionId();
             String name = (json.has("displayName") ? json.getString("displayName") : null);
 
             if( name == null ) {
                 name = (json.has("display_name") ? json.getString("display_name") : null);
                 if( name == null ) {
                     name = snapshotId;
                 }
             }
 
             String description = (json.has("displayDescription") ? json.getString("displayDescription") : null);
 
             if( description == null ) {
                 description = (json.has("display_description") ? json.getString("display_description") : null);
                 if( description == null ) {
                     description = name;
                 }
             }
 
             String volumeId = (json.has("volumeId") ? json.getString("volumeId") : null);
 
 
             if( volumeId == null ) {
                 volumeId = (json.has("volume_id") ? json.getString("volume_id") : null);
             }
             SnapshotState currentState = SnapshotState.PENDING;
             String status = (json.has("status") ? json.getString("status") : null);
 
             if( status != null ) {
                 if( status.equalsIgnoreCase("deleted") ) {
                     currentState = SnapshotState.DELETED;
                 }
                 else if( status.equalsIgnoreCase("available") ) {
                     currentState = SnapshotState.AVAILABLE;
                 }
                 else if( status.equalsIgnoreCase("creating") ) {
                     currentState = SnapshotState.PENDING;
                 }
                 else {
                     logger.warn("DEBUG: Unknown OpenStack snapshot state: " + status);
                 }
             }
             long created = (json.has("createdAt") ? ((NovaOpenStack)getProvider()).parseTimestamp(json.getString("createdAt")) : -1L);
 
             if( created < 1L ) {
                 created = (json.has("created_at") ? ((NovaOpenStack)getProvider()).parseTimestamp(json.getString("created_at")) : -1L);
             }
             int size = (json.has("size") ? json.getInt("size") : 0);
 
             Snapshot snapshot = new Snapshot();
 
             snapshot.setCurrentState(currentState);
             snapshot.setDescription(description);
             snapshot.setName(name);
             snapshot.setOwner(getTenantId());
             snapshot.setProviderSnapshotId(snapshotId);
             snapshot.setRegionId(regionId);
             snapshot.setSizeInGb(size);
             snapshot.setSnapshotTimestamp(created);
             snapshot.setVolumeId(volumeId);
             return snapshot;
         }
         catch( JSONException e ) {
             throw new CloudException(e);
         }
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable JSONObject json) throws CloudException, InternalException {
         if( json == null ) {
             return null;
         }
 
         try {
             String snapshotId = (json.has("id") ? json.getString("id") : null);
 
             if( snapshotId == null ) {
                 return null;
             }
             SnapshotState state = SnapshotState.PENDING;
 
             String status = (json.has("status") ? json.getString("status") : null);
 
             if( status != null ) {
                 if( status.equalsIgnoreCase("deleted") ) {
                     state = SnapshotState.DELETED;
                 }
                 else if( status.equalsIgnoreCase("available") ) {
                     state = SnapshotState.AVAILABLE;
                 }
                 else if( status.equalsIgnoreCase("creating") ) {
                     state = SnapshotState.PENDING;
                 }
                 else {
                     logger.warn("DEBUG: Unknown OpenStack snapshot state: " + status);
                 }
             }
             return new ResourceStatus(snapshotId, state);
         }
         catch( JSONException e ) {
             throw new CloudException(e);
         }
     }
 }

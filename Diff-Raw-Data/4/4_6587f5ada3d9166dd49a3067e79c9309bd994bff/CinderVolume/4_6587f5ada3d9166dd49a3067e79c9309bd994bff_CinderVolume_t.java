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
 import org.dasein.cloud.compute.AbstractVolumeSupport;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.compute.Volume;
 import org.dasein.cloud.compute.VolumeCreateOptions;
 import org.dasein.cloud.compute.VolumeFormat;
 import org.dasein.cloud.compute.VolumeProduct;
 import org.dasein.cloud.compute.VolumeState;
 import org.dasein.cloud.compute.VolumeType;
 import org.dasein.cloud.openstack.nova.os.NovaMethod;
 import org.dasein.cloud.openstack.nova.os.NovaOpenStack;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.cloud.util.Cache;
 import org.dasein.cloud.util.CacheLevel;
 import org.dasein.util.CalendarWrapper;
 import org.dasein.util.uom.storage.Gigabyte;
 import org.dasein.util.uom.storage.Storage;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * Support for the Cinder volumes API in Dasein Cloud.
  * <p>Created by George Reese: 10/25/12 9:22 AM</p>
  * @author George Reese
  * @version 2012.09.1 copied over from volume extension for HP
  * @since 2012.09.1
  */
 public class CinderVolume extends AbstractVolumeSupport {
     static private final Logger logger = NovaOpenStack.getLogger(CinderVolume.class, "std");
 
     static public final String SERVICE  = "volume";
 
     public CinderVolume(@Nonnull NovaOpenStack provider) {
         super(provider);
     }
 
     private @Nonnull String getAttachmentsResource() {
         return "os-volume_attachments";
     }
 
     private @Nonnull String getResource() {
        // 20130930 dmayne: seems like hp may have upgraded and uses the same resource
        return "/volumes";
        // return (((NovaOpenStack)getProvider()).isHP() ? "/os-volumes" : "/volumes");
     }
 
     private @Nonnull String getTypesResource() {
         return "/types";
     }
 
     @Override
     public void attach(@Nonnull String volumeId, @Nonnull String toServer, @Nonnull String device) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Volume.attach");
         try {
             HashMap<String,Object> attachment = new HashMap<String, Object>();
             HashMap<String,Object> wrapper = new HashMap<String, Object>();
             NovaMethod method = new NovaMethod(((NovaOpenStack)getProvider()));
 
             attachment.put("volumeId", volumeId);
             attachment.put("device", device);
             wrapper.put("volumeAttachment", attachment);
 
             if( method.postString(NovaServer.SERVICE, "/servers", toServer, getAttachmentsResource(), new JSONObject(wrapper)) == null ) {
                 throw new CloudException("No response from the cloud");
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String createVolume(@Nonnull VolumeCreateOptions options) throws InternalException, CloudException {
         if( options.getVlanId() != null ) {
             throw new OperationNotSupportedException("Creating NFS volumes is not supported in " + getProvider().getCloudName());
         }
         APITrace.begin(getProvider(), "Volume.createVolume");
         try {
             HashMap<String,Object> wrapper = new HashMap<String,Object>();
             HashMap<String,Object> json = new HashMap<String,Object>();
             NovaMethod method = new NovaMethod(((NovaOpenStack)getProvider()));
 
             json.put("display_name", options.getName());
             json.put("display_description", options.getDescription());
 
             Storage<Gigabyte> size = options.getVolumeSize();
 
             if( size == null || (size.intValue() < getMinimumVolumeSize().intValue()) ) {
                 size = getMinimumVolumeSize();
             }
             else if( getMaximumVolumeSize() != null && size.intValue() > getMaximumVolumeSize().intValue() ) {
                 size = getMaximumVolumeSize();
             }
             json.put("size", size.intValue());
             if( options.getSnapshotId() != null ) {
                 json.put("snapshot_id", options.getSnapshotId());
             }
             Map<String,Object> md = options.getMetaData();
 
             if( md != null && !md.isEmpty() ) {
                 json.put("metadata", md);
             }
             if( options.getVolumeProductId() != null ) {
                 // TODO: cinder is broken and expects the name; should be fixed in Grizzly
                 VolumeProduct product = null;
 
                 for( VolumeProduct p : listVolumeProducts() ) {
                     if( p.getProviderProductId().equals(options.getVolumeProductId()) ) {
                         product = p;
                         break;
                     }
                 }
                 if( product != null ) {
                     json.put("volume_type", product.getName());
                 }
             }
             wrapper.put("volume", json);
             JSONObject result = method.postString(SERVICE, getResource(), null, new JSONObject(wrapper), true);
 
             if( result != null && result.has("volume") ) {
                 try {
                     Volume volume = toVolume(result.getJSONObject("volume"), listVolumeProducts());
 
                     if( volume != null ) {
                         return volume.getProviderVolumeId();
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
             logger.error("create(): No volume was created by the create attempt, and no error was returned");
             throw new CloudException("No volume was created");
 
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void detach(@Nonnull String volumeId, boolean force) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Volume.detach");
         try {
             Volume volume = getVolume(volumeId);
 
             if( volume == null ) {
                 throw new CloudException("No such volume: " + volumeId);
             }
             if( volume.getProviderVirtualMachineId() == null ) {
                 throw new CloudException("Volume " + volumeId + " is not attached");
             }
             NovaMethod method = new NovaMethod(((NovaOpenStack)getProvider()));
 
             method.deleteResource(NovaServer.SERVICE, "/servers", volume.getProviderVirtualMachineId(), getAttachmentsResource() + "/" + volumeId);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public int getMaximumVolumeCount() throws InternalException, CloudException {
         return -2;
     }
 
     @Override
     public Storage<Gigabyte> getMaximumVolumeSize() throws InternalException, CloudException {
         return new Storage<Gigabyte>(1024, Storage.GIGABYTE);
     }
 
     @Override
     public @Nonnull Storage<Gigabyte> getMinimumVolumeSize() throws InternalException, CloudException {
         if( ((NovaOpenStack)getProvider()).isRackspace() ) {
             return new Storage<Gigabyte>(100, Storage.GIGABYTE);
         }
         return new Storage<Gigabyte>(1, Storage.GIGABYTE);
     }
 
     @Override
     public @Nonnull String getProviderTermForVolume(@Nonnull Locale locale) {
         return "volume";
     }
 
     @Override
     public @Nullable Volume getVolume(@Nonnull String volumeId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Volume.getVolume");
         try {
             NovaMethod method = new NovaMethod(((NovaOpenStack)getProvider()));
             JSONObject ob = method.getResource(SERVICE, getResource(), volumeId, true);
 
             if( ob == null ) {
                 return null;
             }
             try {
                 if( ob.has("volume") ) {
                     return toVolume(ob.getJSONObject("volume"), listVolumeProducts());
                 }
             }
             catch( JSONException e ) {
                 logger.error("getVolume(): Unable to identify expected values in JSON: " + e.getMessage());
                 throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for volume");
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Requirement getVolumeProductRequirement() throws InternalException, CloudException {
         return (((NovaOpenStack)getProvider()).isHP() ? Requirement.NONE : Requirement.OPTIONAL);
     }
 
     @Override
     public boolean isVolumeSizeDeterminedByProduct() throws InternalException, CloudException {
         return false;
     }
 
     @Override
     public @Nonnull Iterable<String> listPossibleDeviceIds(@Nonnull Platform platform) throws InternalException, CloudException {
         ArrayList<String> list = new ArrayList<String>();
 
         if( platform.isWindows() ) {
             list.add("xvdf");
             list.add("xvdg");
             list.add("xvdh");
             list.add("xvdi");
             list.add("xvdj");
         }
         else {
             list.add("/dev/sdf");
             list.add("/dev/sdg");
             list.add("/dev/sdh");
             list.add("/dev/sdi");
             list.add("/dev/sdj");
         }
         return list;
     }
 
     @Override
     public @Nonnull Iterable<VolumeFormat> listSupportedFormats() throws InternalException, CloudException {
         return Collections.singletonList(VolumeFormat.BLOCK);
     }
 
     @Override
     public @Nonnull Iterable<VolumeProduct> listVolumeProducts() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Volume.listVolumeProducts");
         try {
             Cache<VolumeProduct> cache = Cache.getInstance(getProvider(), "volumeProducts", VolumeProduct.class, CacheLevel.REGION_ACCOUNT);
             Iterable<VolumeProduct> current = cache.get(getContext());
 
             if( current != null ) {
                 return current;
             }
             NovaMethod method = new NovaMethod(((NovaOpenStack)getProvider()));
             ArrayList<VolumeProduct> products = new ArrayList<VolumeProduct>();
 
             JSONObject json = method.getResource(SERVICE, getTypesResource(), null, false);
 
             if( json != null && json.has("volume_types") ) {
                 try {
                     JSONArray list = json.getJSONArray("volume_types");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject t = list.getJSONObject(i);
                         String name = (t.has("name") ? t.getString("name") : null);
                         String id = (t.has("id") ? t.getString("id") : null);
                         JSONObject specs = (t.has("extra_specs") ? t.getJSONObject("extra_specs") : null);
 
                         if( name == null || id == null ) {
                             continue;
                         }
                         // this is a huge ass guess
                         VolumeType type = (name.toLowerCase().contains("ssd") ? VolumeType.SSD : VolumeType.HDD);
 
                         if( specs != null ) {
                             String[] names = JSONObject.getNames(specs);
 
                             if( names != null && names.length > 0 ) {
                                 for( String field : names ) {
                                     if( specs.has(field) && specs.get(field) instanceof String ) {
                                         String value = specs.getString(field);
 
                                         if( value != null && value.toLowerCase().contains("ssd") ) {
                                             type = VolumeType.SSD;
                                             break;
                                         }
                                     }
                                 }
                             }
                         }
                         products.add(VolumeProduct.getInstance(id, name, name, type));
                     }
                 }
                 catch( JSONException e ) {
                     logger.error("listVolumes(): Unable to identify expected values in JSON: " + e.getMessage());
                     e.printStackTrace();
                     throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for volumes in " + json.toString());
                 }
             }
             cache.put(getContext(), Collections.unmodifiableList(products));
             return products;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listVolumeStatus() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Volume.listVolumeStatus");
         try {
             NovaMethod method = new NovaMethod(((NovaOpenStack)getProvider()));
             ArrayList<ResourceStatus> volumes = new ArrayList<ResourceStatus>();
 
             JSONObject json = method.getResource(SERVICE, getResource(), null, false);
 
             if( json != null && json.has("volumes") ) {
                 try {
                     JSONArray list = json.getJSONArray("volumes");
 
                     for( int i=0; i<list.length(); i++ ) {
                         ResourceStatus volume = toStatus(list.getJSONObject(i));
 
                         if( volume != null ) {
                             volumes.add(volume);
                         }
                     }
                 }
                 catch( JSONException e ) {
                     logger.error("listVolumes(): Unable to identify expected values in JSON: " + e.getMessage());
                     e.printStackTrace();
                     throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for volumes in " + json.toString());
                 }
             }
             return volumes;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<Volume> listVolumes() throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Volume.listVolumes");
         try {
             Iterable<VolumeProduct> products = listVolumeProducts();
             NovaMethod method = new NovaMethod(((NovaOpenStack)getProvider()));
             ArrayList<Volume> volumes = new ArrayList<Volume>();
 
             JSONObject json = method.getResource(SERVICE, getResource(), null, false);
 
             if( json != null && json.has("volumes") ) {
                 try {
                     JSONArray list = json.getJSONArray("volumes");
 
                     for( int i=0; i<list.length(); i++ ) {
                         JSONObject v = list.getJSONObject(i);
                         Volume volume = toVolume(v, products);
 
                         if( volume != null ) {
                             volumes.add(volume);
                         }
                     }
                 }
                 catch( JSONException e ) {
                     logger.error("listVolumes(): Unable to identify expected values in JSON: " + e.getMessage());
                     e.printStackTrace();
                     throw new CloudException(CloudErrorType.COMMUNICATION, 200, "invalidJson", "Missing JSON element for volumes in " + json.toString());
                 }
             }
             return volumes;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Volume.isSubscribed");
         try {
             return (((NovaOpenStack)getProvider()).getAuthenticationContext().getServiceUrl(SERVICE) != null);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void remove(@Nonnull String volumeId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Volume.remove");
         try {
             long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 10L);
             Volume v = getVolume(volumeId);
 
             while( timeout > System.currentTimeMillis() ) {
                 if( v == null ) {
                     return;
                 }
                 if( !VolumeState.PENDING.equals(v.getCurrentState()) ) {
                     break;
                 }
                 try { Thread.sleep(15000L); }
                 catch( InterruptedException ignore ) { }
                 try {
                     v = getVolume(volumeId);
                 }
                 catch( Throwable ignore ) {
                     // ignore
                 }
             }
             NovaMethod method = new NovaMethod(((NovaOpenStack)getProvider()));
 
             method.deleteResource(SERVICE, getResource(), volumeId, null);
 
             v = getVolume(volumeId);
             timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 10L);
             while( timeout > System.currentTimeMillis() ) {
                 if( v == null || v.getCurrentState().equals(VolumeState.DELETED)) {
                     return;
                 }
                 try { Thread.sleep(15000L); }
                 catch( InterruptedException ignore ) { }
                 try {
                     v = getVolume(volumeId);
                 }
                 catch( Throwable ignore ) {
                     // ignore
                 }
             }
             logger.warn("Volume remove op accepted but still available: current state - "+v.getCurrentState());
         }
         finally {
             APITrace.end();
         }
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable JSONObject json) throws CloudException, InternalException {
         if( json == null ) {
             return null;
         }
         try {
             String volumeId = null;
 
             if( json.has("id") ) {
                 volumeId = json.getString("id");
             }
             if( volumeId == null ) {
                 return null;
             }
 
             VolumeState state = VolumeState.PENDING;
 
             if( json.has("status") ) {
                 String status = json.getString("status");
 
                 if( status.equalsIgnoreCase("available") ) {
                     state = VolumeState.AVAILABLE;
                 }
                 else if( status.equalsIgnoreCase("creating") ) {
                     state = VolumeState.PENDING;
                 }
                 else if( status.equalsIgnoreCase("error") ) {
                     state = VolumeState.PENDING;
                 }
                 else if( status.equals("in-use") ) {
                     state = VolumeState.AVAILABLE;
                 }
                 else if( status.equals("attaching") ) {
                     state = VolumeState.PENDING;
                 }
                 else {
                     logger.warn("DEBUG: Unknown OpenStack Cinder volume state: " + status);
                 }
             }
             return new ResourceStatus(volumeId, state);
         }
         catch( JSONException e ) {
             throw new CloudException(e);
         }
     }
 
     private @Nullable Volume toVolume(@Nullable JSONObject json, @Nonnull Iterable<VolumeProduct> types) throws CloudException, InternalException {
         if( json == null ) {
             return null;
         }
         try {
             String region = getContext().getRegionId();
             String dataCenter = region + "-a";
 
             String volumeId = null;
 
             if( json.has("id") ) {
                 volumeId = json.getString("id");
             }
             if( volumeId == null ) {
                 return null;
             }
             String name = (json.has("displayName") ? json.getString("displayName") : null);
 
             if( name == null ) {
                 name = (json.has("display_name") ? json.getString("display_name") : null);
                 if( name == null ) {
                     name = volumeId;
                 }
             }
 
             String description = (json.has("displayDescription") ? json.getString("displayDescription") : null);
 
             if( description == null ) {
                 description = (json.has("display_description") ? json.getString("display_description") : null);
                 if( description == null ) {
                     description = name;
                 }
             }
 
             long created = (json.has("createdAt") ? ((NovaOpenStack)getProvider()).parseTimestamp(json.getString("createdAt")) : -1L);
 
             if( created < 1L ) {
                 created = (json.has("created_at") ? ((NovaOpenStack)getProvider()).parseTimestamp(json.getString("created_at")) : -1L);
             }
 
             int size = 0;
 
             if( json.has("size") ) {
                 size = json.getInt("size");
             }
             String productId = null;
 
             if( json.has("volume_type") ) {
                 productId = json.getString("volume_type");
             }
             else if( json.has("volumeType") ) {
                 productId = json.getString("volumeType");
             }
             String vmId = null, deviceId = null;
 
             if( json.has("attachments") ) {
                 JSONArray servers = json.getJSONArray("attachments");
 
                 for( int i=0; i<servers.length(); i++ ) {
                     JSONObject ob = servers.getJSONObject(i);
 
                     if( ob.has("serverId") ) {
                         vmId = ob.getString("serverId");
                     }
                     else if( ob.has("server_id") ) {
                         vmId = ob.getString("server_id");
                     }
                     if( ob.has("device") ) {
                         deviceId = ob.getString("device");
                     }
                     if( vmId != null ) {
                         break;
                     }
                 }
             }
             String snapshotId = (json.has("snapshotId") ? json.getString("snapshotId") : null);
 
             if( snapshotId == null ) {
                 snapshotId = (json.has("snapshot_id") ? json.getString("snapshot_id") : null);
             }
 
             VolumeState currentState = VolumeState.PENDING;
 
             if( json.has("status") ) {
                 String status = json.getString("status");
 
                 if( status.equalsIgnoreCase("available") ) {
                     currentState = VolumeState.AVAILABLE;
                 }
                 else if( status.equalsIgnoreCase("creating") ) {
                     currentState = VolumeState.PENDING;
                 }
                 else if( status.equalsIgnoreCase("error") ) {
                     currentState = VolumeState.PENDING;
                 }
                 else if( status.equals("in-use") ) {
                     currentState = VolumeState.AVAILABLE;
                 }
                 else if( status.equals("attaching") ) {
                     currentState = VolumeState.PENDING;
                 }
                 else {
                     logger.warn("DEBUG: Unknown OpenStack Cinder volume state: " + status);
                 }
             }
             Volume volume = new Volume();
 
             volume.setCreationTimestamp(created);
             volume.setCurrentState(currentState);
             volume.setDeviceId(deviceId);
             volume.setName(name);
             volume.setDescription(description);
             volume.setProviderDataCenterId(dataCenter);
             volume.setProviderRegionId(region);
             volume.setProviderSnapshotId(snapshotId);
             volume.setProviderVirtualMachineId(vmId);
             volume.setProviderVolumeId(volumeId);
             volume.setSize(new Storage<Gigabyte>(size, Storage.GIGABYTE));
             if( productId != null ) {
                 VolumeProduct match = null;
 
                 for( VolumeProduct prd : types ) {
                     if( productId.equals(prd.getProviderProductId()) ) {
                         match = prd;
                         break;
                     }
                 }
                 if( match == null ) { // TODO: stupid Folsom bug
                     for( VolumeProduct prd : types ) {
                         if( productId.equals(prd.getName()) ) {
                             match = prd;
                             break;
                         }
                     }
                 }
                 if( match != null ) {
                     volume.setProviderProductId(match.getProviderProductId());
                     volume.setType(match.getType());
                 }
             }
             if( volume.getProviderProductId() == null ) {
                 volume.setProviderProductId(productId);
             }
             if( volume.getType() == null ) {
                 volume.setType(VolumeType.HDD);
             }
             return volume;
         }
         catch( JSONException e ) {
             throw new CloudException(e);
         }
     }
 }

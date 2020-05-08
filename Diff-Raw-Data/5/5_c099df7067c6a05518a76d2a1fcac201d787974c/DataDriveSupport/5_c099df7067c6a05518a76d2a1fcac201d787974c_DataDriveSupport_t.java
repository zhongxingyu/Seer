 /**
  * Copyright (C) 2012-2013 Dell, Inc.
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
 
 package org.dasein.cloud.cloudsigma.compute.block;
 
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.Tag;
 import org.dasein.util.CalendarWrapper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.cloudsigma.CloudSigma;
 import org.dasein.cloud.cloudsigma.CloudSigmaConfigurationException;
 import org.dasein.cloud.cloudsigma.CloudSigmaMethod;
 import org.dasein.cloud.cloudsigma.NoContextException;
 import org.dasein.cloud.compute.*;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.util.uom.storage.*;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.*;
 
 /**
  * Implements support for block storage devices that may be attached to virtual machines in CloudSigma.
  * <p>Created by Danielle Mayne: 02/19/13 8:04 am</p>
  * @author George Reese
  * @author Danielle Mayne
  * @version 2013.02 initial version
  * @since 2013.02
  */
 public class DataDriveSupport extends AbstractVolumeSupport {
     static private final Logger logger = CloudSigma.getLogger(DataDriveSupport.class);
 
     private CloudSigma provider;
 
     public DataDriveSupport(@Nonnull CloudSigma provider) {
         super(provider);
         this.provider = provider;
     }
 
     @Override
     public void attach(@Nonnull String volumeId, @Nonnull String toServer, @Nonnull String deviceId) throws InternalException, CloudException {
         Volume v = getVolume(volumeId);
 
         if (v == null) {
             throw new CloudException("No such volume: " + volumeId);
         }
         provider.getComputeServices().getVirtualMachineSupport().attach(v, toServer, deviceId);
     }
 
     @Override
     public @Nonnull String createVolume(@Nonnull VolumeCreateOptions options) throws InternalException, CloudException {
         if (options.getSnapshotId() != null) {
             throw new OperationNotSupportedException("CloudSigma does not support snapshots");
         }
         if( !VolumeFormat.BLOCK.equals(options.getFormat()) ) {
             throw new OperationNotSupportedException("Only block volumes are supported");
         }
         try {
             logger.debug("Creating volume: "+options.getName()+", ("+options.getDescription()+")");
             JSONObject newDrive = new JSONObject();
             newDrive.put("name", options.getName().replaceAll("\n", " "));
             newDrive.put("size", String.valueOf(options.getVolumeSize().convertTo(Storage.BYTE).longValue()));
             newDrive.put("encryption", "aes-xts-plain");    //todo can this be assumed?
             newDrive.put("media", "disk");                  //todo can this be assumed?
             if (options.getVolumeProductId() != null && "ssd".equals(options.getVolumeProductId())) {
                 JSONArray affinities = new JSONArray();
                 affinities.put("ssd");
                 newDrive.put("affinities", affinities);
             }
 
             //dmayne 20130218: add JSON parsing
             CloudSigmaMethod method = new CloudSigmaMethod(provider);
             Volume volume = null;
             JSONObject obj = new JSONObject(method.postString("/drives/", newDrive.toString()));
             if (obj != null) {
                 JSONObject json = (JSONObject) obj;
                 JSONArray objects = json.getJSONArray("objects");
                 volume = toVolume(objects.getJSONObject(0));
             }
 
             if (volume == null) {
                 throw new CloudException("Volume created but no volume information was provided");
             }
             return volume.getProviderVolumeId();
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     @Override
     public void detach(@Nonnull String volumeId, boolean force) throws InternalException, CloudException {
         Volume v = getVolume(volumeId);
 
         if (v == null) {
             throw new CloudException("No such volume: " + volumeId);
         }
         provider.getComputeServices().getVirtualMachineSupport().detach(v);
     }
 
     public @Nullable String getDrive(String driveId) throws CloudException, InternalException {
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         return method.getString(toDriveURL(driveId, ""));
     }
 
     @Override
     public int getMaximumVolumeCount() throws InternalException, CloudException {
         return -2;
     }
 
     @Override
     public Storage<Gigabyte> getMaximumVolumeSize() throws InternalException, CloudException {
         return null;
     }
 
     @Override
     public @Nonnull Storage<Gigabyte> getMinimumVolumeSize() throws InternalException, CloudException {
         return new Storage<Gigabyte>(1, Storage.GIGABYTE);
     }
 
     @Override
     public @Nonnull String getProviderTermForVolume(@Nonnull Locale locale) {
         return "drive";
     }
 
     @Override
     public Volume getVolume(@Nonnull String volumeId) throws InternalException, CloudException {
         //dmayne 20130218: JSON Parsing
         try {
             String jDrive = (getDrive(volumeId));
             if (jDrive != null){
                 return toVolume(new JSONObject(jDrive));
             }
             return null;
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     @Override
     public @Nonnull Requirement getVolumeProductRequirement() throws InternalException, CloudException {
         return Requirement.OPTIONAL;
     }
 
     @Override
     public boolean isVolumeSizeDeterminedByProduct() throws InternalException, CloudException {
         return false;
     }
 
     static private Collection<String> deviceIds;
 
     @Override
     public @Nonnull Iterable<String> listPossibleDeviceIds(@Nonnull Platform platform) throws InternalException, CloudException {
         if (deviceIds == null) {
             ArrayList<String> ids = new ArrayList<String>();
 
             for (int i = 0; i <= 9; i++) {
                 for (int j = 0; j <= 3; j++) {
                    if (i == 0 && j == 0){} //0:0 is always the boot drive so unavailable for attaching volumes
                    else {
                        ids.add(String.valueOf(i).concat(":").concat(String.valueOf(j)));
                    }
                 }
             }
             deviceIds = Collections.unmodifiableList(ids);
         }
         return deviceIds;
     }
 
     @Override
     public @Nonnull Iterable<VolumeFormat> listSupportedFormats() throws InternalException, CloudException {
         return Collections.singletonList(VolumeFormat.BLOCK);
     }
 
     @Override
     public @Nonnull Iterable<VolumeProduct> listVolumeProducts() throws InternalException, CloudException {
         ArrayList<VolumeProduct> products = new ArrayList<VolumeProduct>();
 
         products.add(VolumeProduct.getInstance("hdd", "HDD", "HDD Affinity", VolumeType.HDD));
         products.add(VolumeProduct.getInstance("ssd", "SSD", "SSD Affinity", VolumeType.SSD));
         return products;
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listVolumeStatus() throws InternalException, CloudException {
         //dmayne 20130218: use JSON parsing
         ArrayList<ResourceStatus> list = new ArrayList<ResourceStatus>();
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         boolean moreData = true;
         String baseTarget = "/drives";
         String target = "/?limit=0&fields=uuid,meta,name,status";
 
       //  while(moreData)  {      used for paging which cloudsigma no longer seems to support
             //dmayne 20130218: JSON Parsing
             target = baseTarget+target;
 
             try {
                 JSONObject json = method.list(target);
 
                 if (json == null) {
                     throw new CloudException("Could not identify drive endpoint for CloudSigma");
                 }
                 if (json.has("objects")) {
                     JSONArray objects = json.getJSONArray("objects");
                     for (int i = 0; i < objects.length(); i++) {
                         JSONObject jVolume = objects.getJSONObject(i);
                         //dmayne 20130522: check that we are looking at a volume
                         //(will not have an image_type attribute)
                         JSONObject metadata = jVolume.getJSONObject("meta");
                         String name = jVolume.getString("name");
                         if (!metadata.has("image_type") && !name.startsWith("esimg-")) {
                             ResourceStatus volume = toStatus(jVolume);
 
                             if (volume != null) {
                                 list.add(volume);
                             }
                          }
                     }
                 }
 
                 /*//dmayne 20130314: check if there are more pages  - commented out as it seems paging is no longer supported
                 but who knows when the api will change back again
                 if (json.has("meta")) {
                     JSONObject meta = json.getJSONObject("meta");
 
                     if (meta.has("next") && !(meta.isNull("next")) && !meta.getString("next").equals("")) {
                         target = meta.getString("next");
                         target = target.substring(target.indexOf("?"));
                         moreData = true;
                     }
                     else  {
                         moreData = false;
                     }
                 }*/
             }
             catch (JSONException e) {
                 throw new InternalException(e);
             }
        // }
 
         return list;
     }
 
     @Override
     public @Nonnull Iterable<Volume> listVolumes() throws InternalException, CloudException {
         //dmayne 20130218: use JSON parsing
         ArrayList<Volume> list = new ArrayList<Volume>();
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         boolean moreData = true;
         String baseTarget = "/drives/detail/?limit=0";
         String target = "";
 
         //while(moreData)  {    - commented out as it seems paging is no longer supported
         //but who knows when the api will change back again
             //dmayne 20130218: JSON Parsing
             target = baseTarget+target;
 
             try {
                 JSONObject json = method.list(target);
 
                 if (json == null) {
                     throw new CloudException("Could not identify drive endpoint for CloudSigma");
                 }
                 if (json.has("objects")) {
                     JSONArray objects = json.getJSONArray("objects");
                     for (int i = 0; i < objects.length(); i++) {
                         JSONObject jVolume = objects.getJSONObject(i);
                         //dmayne 20130522: check that we are looking at a volume
                         //(will not have an image_type attribute)
                         JSONObject metadata = jVolume.getJSONObject("meta");
                         String name = jVolume.getString("name");
                         if (!metadata.has("image_type") && !name.startsWith("esimg-")) {
                             Volume volume = toVolume(jVolume);
 
                             if (volume != null) {
                                 list.add(volume);
                             }
                          }
                     }
                 }
 
                 /*//dmayne 20130314: check if there are more pages     - commented out as it seems paging is no longer supported
                 but who knows when the api will change back again
                 if (json.has("meta")) {
                     JSONObject meta = json.getJSONObject("meta");
 
                     if (meta.has("next") && !(meta.isNull("next")) && !meta.getString("next").equals("")) {
                         target = meta.getString("next");
                         target = target.substring(target.indexOf("?"));
                         moreData = true;
                     }
                     else  {
                         moreData = false;
                     }
                 } */
             }
             catch (JSONException e) {
                 throw new InternalException(e);
             }
        // }
 
 
 
         return list;
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         return provider.getComputeServices().getVirtualMachineSupport().isSubscribed();
     }
 
     @Override
     public void remove(@Nonnull String volumeId) throws InternalException, CloudException {
         Volume v = getVolume(volumeId);
 
         long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 5L);
 
         while( timeout > System.currentTimeMillis() ) {
             if( v == null ) {
                 return;
             }
             if( !VolumeState.PENDING.equals(v.getCurrentState()) ) {
                 break;
             }
             try { Thread.sleep(15000L); }
             catch( InterruptedException ignore ) { }
             try { v = getVolume(volumeId); }
             catch( Throwable ignore ) { }
         }
         CloudSigmaMethod method = new CloudSigmaMethod(provider);
 
         if (method.deleteString(toDriveURL(volumeId, ""), "") == null) {
             throw new CloudException("Unable to identify drives endpoint for removal");
         }
 
         timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 5L);
         v = getVolume(volumeId);
         while( timeout > System.currentTimeMillis() ) {
             if( v == null || VolumeState.DELETED.equals(v.getCurrentState())) {
                 return;
             }
             try { Thread.sleep(15000L); }
             catch( InterruptedException ignore ) { }
             try { v = getVolume(volumeId); }
             catch( Throwable ignore ) { }
         }
     }
 
     private @Nullable Volume toVolume(@Nullable JSONObject drive) throws CloudException, InternalException {
         if (drive == null) {
             return null;
         }
         ProviderContext ctx = provider.getContext();
 
         if (ctx == null) {
             throw new NoContextException();
         }
         String regionId = ctx.getRegionId();
 
         if (regionId == null) {
             throw new CloudSigmaConfigurationException("No region was specified for this request");
         }
 
         Volume volume = new Volume();
 
         volume.setProviderDataCenterId(regionId + "-a");
         volume.setProviderRegionId(regionId);
 
         try {
             String id = drive.getString("uuid");
             if (id != null && !id.equals("")) {
                 volume.setProviderVolumeId(id);
             }
 
             boolean found = false;
             if (drive.has("mounted_on")) {
                 JSONArray servers = drive.getJSONArray("mounted_on");
                 for (int i = 0; i < servers.length(); i++) {
                     JSONObject server = servers.getJSONObject(i);
                     String host = server.getString("uuid");
                     if (host != null && !host.equals("")) {
                         VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(host);
                         String deviceId = null;
                         if (vm != null && !found) {
                             //dmayne 20130314: set server to first found in case none of them are running
                             found = true;
                             volume.setProviderVirtualMachineId(host);
                             deviceId = provider.getComputeServices().getVirtualMachineSupport().getDeviceId(vm, id);
                             if (deviceId != null) {
                                 volume.setDeviceId(deviceId);
                             }
                         }
                         if (vm != null && vm.getCurrentState().equals(VmState.RUNNING)) {
                             volume.setProviderVirtualMachineId(host);
                             deviceId = provider.getComputeServices().getVirtualMachineSupport().getDeviceId(vm, id);
                             if (deviceId != null) {
                                 volume.setDeviceId(deviceId);
                             }
                             break;
                         }
                     }
                 }
             }
 
             String name = drive.getString("name");
             if (name != null && name.length() > 0) {
                 volume.setName(name);
             }
 
             String size = drive.getString("size");
             if (size != null && size.length() > 0) {
                 try {
                     volume.setSize(new Storage<org.dasein.util.uom.storage.Byte>(Long.parseLong(size), Storage.BYTE));
                 } catch (NumberFormatException e) {
                     logger.warn("Invalid drive size: " + size);
                 }
             }
 
             if (volume.getSize() == null) {
                 volume.setSize(new Storage<Gigabyte>(1, Storage.GIGABYTE));
             }
 
             String s = drive.getString("status");
             if (s != null) {
                 if (s.equalsIgnoreCase("mounted") || s.equalsIgnoreCase("unmounted")) {
                     volume.setCurrentState(VolumeState.AVAILABLE);
                 } else if (s.equalsIgnoreCase("unavailable")) {
                     volume.setCurrentState(VolumeState.DELETED);
                 } else if (s.startsWith("copying")) {
                     volume.setCurrentState(VolumeState.PENDING);
                 } else {
                     logger.warn("DEBUG: Unknown drive state for CloudSigma: " + s);
                 }
             }
             if (VolumeState.AVAILABLE.equals(volume.getCurrentState())) {
                 //todo: dmayne 20130305: not implemented by cloudsigma yet
                 if (drive.has("imaging")) {
                     s = drive.getString("imaging");
                     if (s != null) {
                         volume.setCurrentState(VolumeState.PENDING);
                     }
                 }
             }
 
             //dmayne 20130225: default volume type is HDD
             volume.setType(VolumeType.HDD);
             volume.setProviderProductId("hdd");
 
             //dmayne 20130225: if affinities set, update volume type
             if (drive.has("affinities")) {
                 JSONArray affinities = drive.getJSONArray("affinities");
                 //dmayne 20130218: if affinities exists it must be ssd as this is only option in enum
                 if (affinities != null && affinities.length() > 0) {
                     volume.setType(VolumeType.SSD);
                     volume.setProviderProductId("ssd");
                 }
             }
 
             if (volume.getProviderVolumeId() == null) {
                 return null;
             }
             if (volume.getName() == null) {
                 volume.setName(volume.getProviderVolumeId());
             }
             if (volume.getDescription() == null) {
                 volume.setDescription(volume.getName());
             }
 
             //dmayne 20130306: check if meta object has an os attribute
             String description = null;
             String install_notes = null;
             String os = null;
             if (drive.has("meta")){
                 JSONObject meta = drive.getJSONObject("meta");
                 //dmayne 20130220: look for description tag and if not available check install_notes
                 if (meta != null) {
                     if (meta.has("os")) {
                         os = meta.getString("os");
                     }
                     if (meta.has("description")) {
                         description = meta.getString("description");
                     }
                     if (meta.has("install_notes")) {
                         install_notes = meta.getString("install_notes");
                     }
                 }
 
                 if (description != null && !description.equals("")) {
                     volume.setDescription(description);
                 }
                 else if (install_notes != null && !install_notes.equals("")) {
                     volume.setDescription(install_notes);
                 }
             }
 
             Platform platform = Platform.UNKNOWN;
             if (os != null && !os.equals("")) {
                 platform = Platform.guess(os);
             }
 
             if (platform.equals(Platform.UNKNOWN)) {
                 platform = Platform.guess(volume.getName());
                 if (platform.equals(Platform.UNKNOWN)) {
                     //check description followed by install notes
                     platform = Platform.guess(volume.getDescription());
                     if (platform.equals(Platform.UNKNOWN)){
                         platform = Platform.guess(install_notes);
                     }
                 }
             } else if (platform.equals(Platform.UNIX)) {
                 Platform p = Platform.guess(volume.getName());
 
                 if (!p.equals(Platform.UNKNOWN)) {
                     platform = p;
                 }
             }
             volume.setGuestOperatingSystem(platform);
             volume.setRootVolume(true);
 
             return volume;
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable JSONObject drive) throws CloudException, InternalException {
         if (drive == null) {
             return null;
         }
         ProviderContext ctx = provider.getContext();
 
         if (ctx == null) {
             throw new NoContextException();
         }
 
         try {
             String id = drive.getString("uuid");
             if (id == null || id.equals("")) {
                 return null;
             }
 
             VolumeState state = VolumeState.PENDING;
             String s = drive.getString("status");
             if (s != null) {
                 if (s.equalsIgnoreCase("mounted") || s.equalsIgnoreCase("unmounted")) {
                     state = VolumeState.AVAILABLE;
                 } else if (s.equalsIgnoreCase("unavailable")) {
                     state = VolumeState.DELETED;
                 } else if (s.startsWith("copying")) {
                     state = VolumeState.PENDING;
                 } else {
                     logger.warn("DEBUG: Unknown drive state for CloudSigma: " + s);
                 }
             }
             return new ResourceStatus(id, state);
         }
         catch (JSONException e) {
             throw new InternalException(e);
         }
     }
 
     private @Nonnull String toDriveURL(@Nonnull String vmId, @Nonnull String action) throws InternalException {
         try {
             return ("/drives/" + URLEncoder.encode(vmId, "utf-8") + "/" + action);
         } catch (UnsupportedEncodingException e) {
             logger.error("UTF-8 not supported: " + e.getMessage());
             throw new InternalException(e);
         }
     }
 }

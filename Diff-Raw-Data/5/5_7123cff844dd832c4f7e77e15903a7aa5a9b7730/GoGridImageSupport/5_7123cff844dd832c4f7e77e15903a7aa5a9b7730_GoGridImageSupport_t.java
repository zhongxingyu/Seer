 /**
  * Copyright (C) 2012 enStratus Networks Inc
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
 
 package org.dasein.cloud.gogrid.compute.image;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AsynchronousTask;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.Tag;
 import org.dasein.cloud.compute.Architecture;
 import org.dasein.cloud.compute.ImageClass;
 import org.dasein.cloud.compute.ImageCreateOptions;
 import org.dasein.cloud.compute.MachineImage;
 import org.dasein.cloud.compute.MachineImageFormat;
 import org.dasein.cloud.compute.MachineImageState;
 import org.dasein.cloud.compute.MachineImageSupport;
 import org.dasein.cloud.compute.MachineImageType;
 import org.dasein.cloud.compute.Platform;
 
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.compute.VmState;
 import org.dasein.cloud.gogrid.GoGrid;
 import org.dasein.cloud.gogrid.GoGridMethod;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.util.CalendarWrapper;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Locale;
 
 /**
  * Support for GoGrid machine images.
  * <p>Created by George Reese: 10/14/12 8:35 PM</p>
  * @author George Reese
  * @version 2012.09 initial version
  * @since 2012.09
  */
 public class GoGridImageSupport implements MachineImageSupport {
     static private final Logger logger = GoGrid.getLogger(GoGridImageSupport.class);
 
     private GoGrid provider;
 
     public GoGridImageSupport(GoGrid provider) { this.provider = provider; }
 
     private @Nonnull ProviderContext getContext() throws CloudException {
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new CloudException("No context was provided for this request");
         }
         return ctx;
     }
 
     @Override
     public void addImageShare(@Nonnull String providerImageId, @Nonnull String accountNumber) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Image sharing is not supported");
     }
 
     @Override
     public void addPublicShare(@Nonnull String providerImageId) throws CloudException, InternalException {
         GoGridMethod method = new GoGridMethod(provider);
 
         method.get(GoGridMethod.IMAGE_EDIT, new GoGridMethod.Param("id", providerImageId), new GoGridMethod.Param("isPublic", "true"));
     }
 
     @Override
     public @Nonnull String bundleVirtualMachine(@Nonnull String virtualMachineId, @Nonnull MachineImageFormat format, @Nonnull String bucket, @Nonnull String name) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Image bundling is not supported");
     }
 
     @Override
     public void bundleVirtualMachineAsync(@Nonnull String virtualMachineId, @Nonnull MachineImageFormat format, @Nonnull String bucket, @Nonnull String name, @Nonnull AsynchronousTask<String> trackingTask) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Image bundling is not supported");
     }
 
     @Override
     public @Nonnull MachineImage captureImage(@Nonnull ImageCreateOptions options) throws CloudException, InternalException {
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new CloudException("No context was set for this request");
         }
         return captureImage(options, null);
     }
 
     @Override
     public void captureImageAsync(final @Nonnull ImageCreateOptions options, final @Nonnull AsynchronousTask<MachineImage> taskTracker) throws CloudException, InternalException {
         final ProviderContext ctx = provider.getContext();
         VirtualMachine vm = null;
 
         long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 30L);
 
         while( timeout > System.currentTimeMillis() ) {
             try {
                 //noinspection ConstantConditions
                 vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(options.getVirtualMachineId());
                 if( vm == null ) {
                     break;
                 }
                 if( !vm.isPersistent() ) {
                     throw new OperationNotSupportedException("You cannot capture instance-backed virtual machines");
                 }
                 if( VmState.RUNNING.equals(vm.getCurrentState()) || VmState.STOPPED.equals(vm.getCurrentState()) ) {
                     break;
                 }
             }
             catch( Throwable ignore ) {
                 // ignore
             }
             try { Thread.sleep(15000L); }
             catch( InterruptedException ignore ) { }
         }
         if( vm == null ) {
             throw new CloudException("No such virtual machine: " + options.getVirtualMachineId());
         }
 
         if( ctx == null ) {
             throw new CloudException("No context was set for this request");
         }
         Thread t = new Thread() {
             public void run() {
                 try {
                     taskTracker.completeWithResult(captureImage(options, taskTracker));
                 }
                 catch( Throwable t ) {
                     taskTracker.complete(t);
                 }
                 finally {
                     provider.release();
                 }
             }
         };
 
         provider.hold();
         t.setName("Imaging " + options.getVirtualMachineId() + " as " + options.getName());
         t.setDaemon(true);
         t.start();
     }
 
     private @Nonnull MachineImage captureImage(@Nonnull ImageCreateOptions options, @Nullable AsynchronousTask<MachineImage> task) throws CloudException, InternalException {
         if( task != null ) {
             task.setPercentComplete(1);
         }
 
         GoGridMethod method = new GoGridMethod(provider);
         String vmId = options.getVirtualMachineId();
 
         if( vmId == null ) {
             throw new CloudException("No VM ID was specified for capture");
         }
         JSONArray list = method.get(GoGridMethod.IMAGE_SAVE, new GoGridMethod.Param("server", vmId), new GoGridMethod.Param("friendlyName", options.getName()), new GoGridMethod.Param("description", options.getDescription()));
 
         if( list == null ) {
             throw new CloudException("Attempting to image virtual machine but nothing was returned without comment");
         }
 
         for( int i=0; i<list.length(); i++ ) {
             try {
                 MachineImage img = toImage(list.getJSONObject(i));
 
                 if( img != null ) {
                     return img;
                 }
             }
             catch( JSONException e ) {
                 logger.error("Failed to parse JSON: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(e);
             }
         }
         throw new CloudException("Image was captured but no image was returned");
     }
 
     @Override
     public MachineImage getImage(@Nonnull String providerImageId) throws CloudException, InternalException {
         GoGridMethod method = new GoGridMethod(provider);
 
         JSONArray list = method.get(GoGridMethod.IMAGE_GET, new GoGridMethod.Param("image", providerImageId));
 
         if( list == null ) {
             return null;
         }
 
         for( int i=0; i<list.length(); i++ ) {
             try {
                 MachineImage img = toImage(list.getJSONObject(i));
 
                 if( img != null && img.getProviderMachineImageId().equals(providerImageId) ) {
                     return img;
                 }
             }
             catch( JSONException e ) {
                 logger.error("Failed to parse JSON: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(e);
             }
         }
         return null;
     }
 
     @Override
     @Deprecated
     public MachineImage getMachineImage(@Nonnull String machineImageId) throws CloudException, InternalException {
         return getImage(machineImageId);
     }
 
     @Override
     @Deprecated
     public @Nonnull String getProviderTermForImage(@Nonnull Locale locale) {
         return getProviderTermForImage(locale, ImageClass.MACHINE);
     }
 
     @Override
     public @Nonnull String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
         switch( cls ) {
             case KERNEL: return "kernel image";
             case RAMDISK: return "ramdisk image";
         }
         return "server image";
     }
 
     private @Nonnull String getRegionId(@Nonnull ProviderContext ctx) throws CloudException {
         String regionId = ctx.getRegionId();
 
         if( regionId == null ) {
             throw new CloudException("No region was provided for this request");
         }
         return regionId;
     }
 
     @Override
     public boolean hasPublicLibrary() {
         return true;
     }
 
     @Override
     public @Nonnull Requirement identifyLocalBundlingRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     public @Nonnull AsynchronousTask<String> imageVirtualMachine(final String vmId, String name, String description) throws CloudException, InternalException {
         VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(vmId);
 
         if( vm == null ) {
             throw new CloudException("No such virtual machine: " + vmId);
         }
         if( name == null ) {
             name = "Image of " + vm.getProviderVirtualMachineId();
         }
         if( description == null ) {
             description = name;
         }
         final ImageCreateOptions options = ImageCreateOptions.getInstance(vm, name, description);
         final AsynchronousTask<String> task = new AsynchronousTask<String>();
 
         Thread t = new Thread() {
             public void run() {
                 try {
                     task.completeWithResult(captureImage(options, null).getProviderMachineImageId());
                 }
                 catch( Throwable t ) {
                     task.complete(t);
                 }
             }
         };
 
         t.setName("Imaging " + vmId + " as " + name);
         t.setDaemon(true);
         t.start();
         return task;
     }
 
     @Override
     public boolean isImageSharedWithPublic(@Nonnull String machineImageId) throws CloudException, InternalException {
         GoGridMethod method = new GoGridMethod(provider);
 
         JSONArray list = method.get(GoGridMethod.IMAGE_GET, new GoGridMethod.Param("image", machineImageId));
 
         if( list == null ) {
             return false;
         }
 
         for( int i=0; i<list.length(); i++ ) {
             try {
                 JSONObject json = list.getJSONObject(i);
 
                 if( json.has("isPublic") && json.has("id") && json.getString("id").equals(machineImageId) ) {
                     return json.getBoolean("isPublic");
                 }
             }
             catch( JSONException e ) {
                 logger.error("Failed to parse JSON: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(e);
             }
         }
         return false;
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         GoGridMethod method = new GoGridMethod(provider);
         String regionId = getRegionId(getContext());
 
         JSONArray regionList = method.get(GoGridMethod.LOOKUP_LIST, new GoGridMethod.Param("lookup", "server.datacenter"));
 
         if( regionList == null ) {
             return false;
         }
         for( int i=0; i<regionList.length(); i++ ) {
             try {
                 JSONObject r = regionList.getJSONObject(i);
 
                 if( r.has("id") && regionId.equals(r.getString("id")) ) {
                     return true;
                 }
             }
             catch( JSONException e ) {
                 logger.error("Unable to load data centers from GoGrid: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(e);
             }
         }
         return false;
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listImageStatus(@Nonnull ImageClass cls) throws CloudException, InternalException {
         ProviderContext ctx = getContext();
         String regionId = getRegionId(ctx);
 
         GoGridMethod method = new GoGridMethod(provider);
 
         JSONArray list = method.get(GoGridMethod.IMAGE_LIST, new GoGridMethod.Param("datacenter", regionId));
 
         if( list == null ) {
             return Collections.emptyList();
         }
         ArrayList<ResourceStatus> images = new ArrayList<ResourceStatus>();
 
         for( int i=0; i<list.length(); i++ ) {
             try {
                 ResourceStatus img = toStatus(list.getJSONObject(i), false);
 
                 if( img != null ) {
                     images.add(img);
                 }
             }
             catch( JSONException e ) {
                 logger.error("Failed to parse JSON: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(e);
             }
         }
         return images;
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> listImages(@Nonnull ImageClass cls) throws CloudException, InternalException {
         ProviderContext ctx = getContext();
         String regionId = getRegionId(ctx);
 
         GoGridMethod method = new GoGridMethod(provider);
 
         JSONArray list = method.get(GoGridMethod.IMAGE_LIST, new GoGridMethod.Param("datacenter", regionId));
 
         if( list == null ) {
             return Collections.emptyList();
         }
         ArrayList<MachineImage> images = new ArrayList<MachineImage>();
 
         for( int i=0; i<list.length(); i++ ) {
             try {
                 MachineImage img = toImage(list.getJSONObject(i));
 
                 if( img != null && !img.getProviderOwnerId().equals("--gogrid--") ) {
                     images.add(img);
                 }
             }
             catch( JSONException e ) {
                 logger.error("Failed to parse JSON: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(e);
             }
         }
         return images;
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> listImages(@Nonnull ImageClass cls, @Nonnull String ownedBy) throws CloudException, InternalException {
         if( !cls.equals(ImageClass.MACHINE) ) {
             return Collections.emptyList();
         }
         ProviderContext ctx = getContext();
         String regionId = getRegionId(ctx);
 
         GoGridMethod method = new GoGridMethod(provider);
 
         JSONArray list = method.get(GoGridMethod.IMAGE_LIST, new GoGridMethod.Param("datacenter", regionId));
 
         if( list == null ) {
             return Collections.emptyList();
         }
         ArrayList<MachineImage> images = new ArrayList<MachineImage>();
 
         for( int i=0; i<list.length(); i++ ) {
             try {
                 MachineImage img = toImage(list.getJSONObject(i));
 
                 if( img != null && ownedBy.equals(img.getProviderOwnerId()) ) {
                     images.add(img);
                 }
             }
             catch( JSONException e ) {
                 logger.error("Failed to parse JSON: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(e);
             }
         }
         return images;
     }
 
     @Override
     @Deprecated
     public @Nonnull Iterable<MachineImage> listMachineImages() throws CloudException, InternalException {
         return listImages(ImageClass.MACHINE);
     }
 
     @Override
     @Deprecated
     public @Nonnull Iterable<MachineImage> listMachineImagesOwnedBy(String accountId) throws CloudException, InternalException {
         if( accountId == null ) {
             return listImages(ImageClass.MACHINE);
         }
         else {
             return listImages(ImageClass.MACHINE, accountId);
         }
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormatsForBundling() throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     @Override
     public @Nonnull Iterable<String> listShares(@Nonnull String forMachineImageId) throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     @Override
     public @Nonnull Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
         return Collections.singletonList(ImageClass.MACHINE);
     }
 
     @Override
     public @Nonnull Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
        return Collections.singletonList(MachineImageType.STORAGE);
     }
 
     @Override
     public @Nonnull MachineImage registerImageBundle(@Nonnull ImageCreateOptions options) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Bundle registration is not supported");
     }
 
     @Override
     public void remove(@Nonnull String machineImageId) throws CloudException, InternalException {
         GoGridMethod method = new GoGridMethod(provider);
 
         method.get(GoGridMethod.IMAGE_DELETE, new GoGridMethod.Param("id", machineImageId));
     }
 
     @Override
     public void removeAllImageShares(@Nonnull String providerImageId) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Image sharing is not supported");
     }
 
     @Override
     public void removeImageShare(@Nonnull String providerImageId, @Nonnull String accountNumber) throws CloudException, InternalException {
         throw new OperationNotSupportedException("Image sharing is not supported");
     }
 
     @Override
     public void removePublicShare(@Nonnull String providerImageId) throws CloudException, InternalException {
         GoGridMethod method = new GoGridMethod(provider);
 
         method.get(GoGridMethod.IMAGE_EDIT, new GoGridMethod.Param("id", providerImageId), new GoGridMethod.Param("isPublic", "false"));
     }
 
     @Override
     @Deprecated
     public @Nonnull Iterable<MachineImage> searchMachineImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture) throws CloudException, InternalException {
         return searchPublicImages(keyword, platform, architecture);
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> searchImages(@Nullable String accountNumber, @Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
         ProviderContext ctx = getContext();
         String regionId = getRegionId(ctx);
 
         GoGridMethod method = new GoGridMethod(provider);
 
         JSONArray list = method.get(GoGridMethod.IMAGE_LIST, new GoGridMethod.Param("datacenter", regionId));
 
         if( list == null ) {
             return Collections.emptyList();
         }
         ArrayList<MachineImage> images = new ArrayList<MachineImage>();
 
         for( int i=0; i<list.length(); i++ ) {
             try {
                 MachineImage img = toImage(list.getJSONObject(i));
 
                 if( img != null ) {
                     String ownerId = img.getProviderOwnerId();
 
                     if( (accountNumber == null && !ownerId.equals("--gogrid--")) || (accountNumber != null && accountNumber.equals(ownerId)) ) {
                         if( keyword != null ) {
                             if( !img.getName().toLowerCase().contains(keyword) && !img.getDescription().toLowerCase().contains(keyword) ) {
                                 continue;
                             }
                         }
                         if( platform != null && !platform.equals(Platform.UNKNOWN) ) {
                             Platform mine = img.getPlatform();
 
                             if( platform.isWindows() && !mine.isWindows() ) {
                                 continue;
                             }
                             if( platform.isUnix() && !mine.isUnix() ) {
                                 continue;
                             }
                             if( platform.isBsd() && !mine.isBsd() ) {
                                 continue;
                             }
                             if( platform.isLinux() && !mine.isLinux() ) {
                                 continue;
                             }
                             if( platform.equals(Platform.UNIX) ) {
                                 if( !mine.isUnix() ) {
                                     continue;
                                 }
                             }
                             else if( !platform.equals(mine) ) {
                                 continue;
                             }
                         }
                         if( architecture != null && !img.getArchitecture().equals(architecture) ) {
                             continue;
                         }
                         images.add(img);
                     }
                 }
             }
             catch( JSONException e ) {
                 logger.error("Failed to parse JSON: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(e);
             }
         }
         return images;
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> searchPublicImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
         ProviderContext ctx = getContext();
         String regionId = getRegionId(ctx);
 
         GoGridMethod method = new GoGridMethod(provider);
 
         JSONArray list = method.get(GoGridMethod.IMAGE_LIST, new GoGridMethod.Param("datacenter", regionId));
 
         if( list == null ) {
             return Collections.emptyList();
         }
         ArrayList<MachineImage> images = new ArrayList<MachineImage>();
 
         for( int i=0; i<list.length(); i++ ) {
             try {
                 MachineImage img = toImage(list.getJSONObject(i));
 
                 if( img != null ) {
                     if( keyword != null ) {
                         if( !img.getName().toLowerCase().contains(keyword) && !img.getDescription().toLowerCase().contains(keyword) ) {
                             continue;
                         }
                     }
                     if( platform != null && !platform.equals(Platform.UNKNOWN) ) {
                         Platform mine = img.getPlatform();
 
                         if( platform.isWindows() && !mine.isWindows() ) {
                             continue;
                         }
                         if( platform.isUnix() && !mine.isUnix() ) {
                             continue;
                         }
                         if( platform.isBsd() && !mine.isBsd() ) {
                             continue;
                         }
                         if( platform.isLinux() && !mine.isLinux() ) {
                             continue;
                         }
                         if( platform.equals(Platform.UNIX) ) {
                             if( !mine.isUnix() ) {
                                 continue;
                             }
                         }
                         else if( !platform.equals(mine) ) {
                             continue;
                         }
                     }
                     if( architecture != null && !img.getArchitecture().equals(architecture) ) {
                         continue;
                     }
                     images.add(img);
 
                 }
             }
             catch( JSONException e ) {
                 logger.error("Failed to parse JSON: " + e.getMessage());
                 e.printStackTrace();
                 throw new CloudException(e);
             }
         }
         return images;
     }
 
     @Override
     @Deprecated
     public void shareMachineImage(@Nonnull String machineImageId, @Nullable String withAccountId, boolean allow) throws CloudException, InternalException {
         if( withAccountId == null ) {
             if( allow ) {
                 addPublicShare(machineImageId);
             }
             else {
                 removePublicShare(machineImageId);
             }
         }
         else if( allow ) {
             addImageShare(machineImageId, withAccountId);
         }
         else {
             removeImageShare(machineImageId, withAccountId);
         }
     }
 
     @Override
     public boolean supportsCustomImages() {
         return true;
     }
 
     @Override
     public boolean supportsDirectImageUpload() throws CloudException, InternalException {
         return false;
     }
 
     @Override
     public boolean supportsImageCapture(@Nonnull MachineImageType type) throws CloudException, InternalException {
         return true;
     }
 
     @Override
     public boolean supportsImageSharing() {
         return false;
     }
 
     @Override
     public boolean supportsImageSharingWithPublic() {
         return true;
     }
 
     @Override
     public boolean supportsPublicLibrary(@Nonnull ImageClass cls) throws CloudException, InternalException {
         return true;
     }
 
     @Override
     public void updateTags(@Nonnull String imageId, @Nonnull Tag... tags) throws CloudException, InternalException {
         // NO-OP
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
 
     private @Nullable MachineImage toImage(@Nullable JSONObject json) throws CloudException, InternalException {
         if( json == null ) {
             return null;
         }
         MachineImage img = new MachineImage();
         String regionId = getRegionId(getContext());
 
         img.setPlatform(Platform.UNKNOWN);
        img.setType(MachineImageType.STORAGE);
         img.setCurrentState(MachineImageState.PENDING);
         img.setSoftware("");
         img.setProviderRegionId(regionId);
         img.setImageClass(ImageClass.MACHINE);
         try {
             if( json.has("id") ) {
                 img.setProviderMachineImageId(json.getString("id"));
             }
             if( json.has("owner") ) {
                 JSONObject owner = json.getJSONObject("owner");
 
                 if( owner != null && owner.has("id") ) {
                     long id = owner.getLong("id");
 
                     if( id > 0L ) {
                         img.setProviderOwnerId(String.valueOf(id));
                     }
                 }
             }
             /*
             if( json.has("createdTime") ) {
                 long ts = json.getLong("createdTime") * 1000L;
                 // TODO: implement this when Dasein supports it
             }
             */
             if( json.has("os") ) {
                 JSONObject os = json.getJSONObject("os");
                 StringBuilder str = new StringBuilder();
 
                 if( os.has("name") ) {
                     str.append(os.getString("name"));
                 }
                 if( os.has("description") ) {
                     str.append(" ").append(os.getString("description"));
                 }
                 img.setPlatform(Platform.guess(str.toString()));
                 img.setArchitecture(provider.toArchitecture(str.toString()));
             }
             if( json.has("state") ) {
                 JSONObject state = json.getJSONObject("state");
 
                 if( state.has("id") ) {
                     int id = state.getInt("id");
 
                     /*
                    [{"id":1,"description":"Image is being saved","name":"Saving","object":"option"},
                    {"id":2,"description":"Image is available for adds","name":"Available","object":"option"},
                    {"id":3,"description":"Image has been deleted","name":"Deleted","object":"option"},
                    {"id":4,"description":"Image is marked for deletion","name":"Trash","object":"option"},
                    {"id":7,"description":"Image is being migrated from another data center","name":"Migrating","object":"option"}]
                     */
                     switch( id ) {
                         case 1: case 7: img.setCurrentState(MachineImageState.PENDING); break;
                         case 2: img.setCurrentState(MachineImageState.ACTIVE); break;
                         case 3: case 4: img.setCurrentState(MachineImageState.DELETED); break;
                     }
                 }
             }
             if( json.has("architecture") ) {
                 JSONObject architecture = json.getJSONObject("architecture");
 
                 if( architecture.has("id") && architecture.getInt("id") == 1 ) {
                     img.setArchitecture(Architecture.I32);
                 }
                 else if( architecture.has("id") && architecture.getInt("id") == 2 ) {
                     img.setArchitecture(Architecture.I64);
                 }
             }
             if( json.has("datacenterlist") ) {
                 JSONArray list = json.getJSONArray("datacenterlist");
                 boolean matches = false;
 
                 for( int i=0; i<list.length(); i++ ) {
                     JSONObject item = list.getJSONObject(i);
 
                     if( item.has("datacenter") ) {
                         item = item.getJSONObject("datacenter");
                         if( item.has("id") && item.getString("id").equals(regionId) ) {
                             matches = true;
                             break;
                         }
                     }
                 }
                 if( !matches ) {
                     return null;
                 }
             }
         }
         catch( JSONException e ) {
             logger.error("Failed to process image JSON: " + e.getMessage());
             e.printStackTrace();
             throw new CloudException(e);
         }
         if( img.getProviderMachineImageId() == null ) {
             return null;
         }
         if( img.getProviderOwnerId() == null ) {
             img.setProviderOwnerId("--gogrid--");
         }
         if( img.getName() == null ) {
             img.setName(img.getProviderMachineImageId());
         }
         if( img.getDescription() == null ) {
             img.setDescription(img.getName());
         }
         return img;
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable JSONObject json, boolean includeGoGrid) throws CloudException, InternalException {
         if( json == null ) {
             return null;
         }
         try {
             String id;
 
             if( json.has("id") ) {
                 id = json.getString("id");
             }
             else {
                 return null;
             }
             if( json.has("owner") && !includeGoGrid ) {
                 JSONObject owner = json.getJSONObject("owner");
 
                 if( owner != null && owner.has("id") ) {
                     long ownerId = owner.getLong("id");
 
                     if( ownerId < 1L ) {
                         return null;
                     }
                 }
             }
             else if( !includeGoGrid ) {
                 return null;
             }
             MachineImageState is = MachineImageState.PENDING;
 
             if( json.has("state") ) {
                 JSONObject state = json.getJSONObject("state");
 
                 if( state.has("id") ) {
                     switch( state.getInt("id") ) {
                         case 1: case 7: is = MachineImageState.PENDING; break;
                         case 2: is = MachineImageState.ACTIVE; break;
                         case 3: case 4: is = MachineImageState.DELETED; break;
                     }
                 }
             }
             return new ResourceStatus(id, is);
         }
         catch( JSONException e ) {
             logger.error("Failed to process image JSON: " + e.getMessage());
             e.printStackTrace();
             throw new CloudException(e);
         }
     }
 }

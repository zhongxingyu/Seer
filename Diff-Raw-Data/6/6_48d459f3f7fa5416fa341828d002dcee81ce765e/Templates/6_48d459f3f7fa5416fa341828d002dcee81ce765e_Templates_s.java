 /**
  * Copyright (C) 2009-2013 enstratius, Inc.
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
 
 package org.dasein.cloud.cloudstack.compute;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.TreeSet;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.servlet.http.HttpServletResponse;
 
 import org.dasein.cloud.AsynchronousTask;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.OperationNotSupportedException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.Requirement;
 import org.dasein.cloud.ResourceStatus;
 import org.dasein.cloud.cloudstack.CSCloud;
 import org.dasein.cloud.cloudstack.CSException;
 import org.dasein.cloud.cloudstack.CSMethod;
 import org.dasein.cloud.cloudstack.CSServiceProvider;
 import org.dasein.cloud.cloudstack.CSTopology;
 import org.dasein.cloud.cloudstack.CSVersion;
 import org.dasein.cloud.cloudstack.Param;
 import org.dasein.cloud.compute.AbstractImageSupport;
 import org.dasein.cloud.compute.Architecture;
 import org.dasein.cloud.compute.ImageClass;
 import org.dasein.cloud.compute.ImageCreateOptions;
 import org.dasein.cloud.compute.ImageFilterOptions;
 import org.dasein.cloud.compute.MachineImage;
 import org.dasein.cloud.compute.MachineImageFormat;
 import org.dasein.cloud.compute.MachineImageState;
 import org.dasein.cloud.compute.MachineImageType;
 import org.dasein.cloud.compute.Platform;
 import org.dasein.cloud.compute.Snapshot;
 import org.dasein.cloud.compute.SnapshotCreateOptions;
 import org.dasein.cloud.compute.SnapshotState;
 import org.dasein.cloud.compute.VirtualMachine;
 import org.dasein.cloud.util.APITrace;
 import org.dasein.util.CalendarWrapper;
 import org.dasein.util.Jiterator;
 import org.dasein.util.JiteratorPopulator;
 import org.dasein.util.PopulatorThread;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class Templates extends AbstractImageSupport {
     static private final String CREATE_TEMPLATE             = "createTemplate";
     static private final String DELETE_TEMPLATE             = "deleteTemplate";
     static private final String LIST_OS_TYPES               = "listOsTypes";
     static private final String LIST_TEMPLATE_PERMISSIONS   = "listTemplatePermissions";
     static private final String LIST_TEMPLATES              = "listTemplates";
     static private final String REGISTER_TEMPLATE           = "registerTemplate";
     static private final String UPDATE_TEMPLATE_PERMISSIONS = "updateTemplatePermissions";
     
     private CSCloud provider;
     
     public Templates(CSCloud provider) {
         super(provider);
         this.provider = provider;
     }
 
     @Override
     public void addImageShare(@Nonnull String providerImageId, @Nonnull String accountNumber) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.addImageShare");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             MachineImage img = getImage(providerImageId);
 
             if( img == null ) {
                 return;
             }
             if( !ctx.getAccountNumber().equals(img.getProviderOwnerId())
                     && !provider.getParentAccount(ctx.getAccountNumber()).equalsIgnoreCase(img.getProviderOwnerId())) {
                 return;
             }
             Param[] params = new Param[] { new Param("id", providerImageId), new Param("accounts", accountNumber), new Param("op", "add") };
 
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(UPDATE_TEMPLATE_PERMISSIONS, params), UPDATE_TEMPLATE_PERMISSIONS);
 
             provider.waitForJob(doc, "Share Template");
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void addPublicShare(@Nonnull String providerImageId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.addPublicShare");
         try {
             MachineImage img = getImage(providerImageId);
 
             if( img == null ) {
                 return;
             }
 
             if( !getContext().getAccountNumber().equals(img.getProviderOwnerId())
                     && !provider.getParentAccount(getContext().getAccountNumber()).equalsIgnoreCase(img.getProviderOwnerId())) {
                 return;
             }
             Param[] params = new Param[] { new Param("id", providerImageId), new Param("isPublic", "true") };
 
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(UPDATE_TEMPLATE_PERMISSIONS, params), UPDATE_TEMPLATE_PERMISSIONS);
 
             provider.waitForJob(doc, "Share Template");
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public MachineImage getImage(@Nonnull String providerImageId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.getImage");
         try {
             CSMethod method = new CSMethod(provider);
             String url = method.buildUrl(LIST_TEMPLATES, new Param("id", providerImageId), new Param("templateFilter", "executable"), new Param("zoneId", getContext().getRegionId()));
             Document doc;
             try {
                 doc = method.get(url, LIST_TEMPLATES);
             }
             catch( CSException e ) {
                 if( e.getHttpCode() == 431 ) {
                     return null;
                 }
                 if( e.getMessage() != null && (e.getMessage().contains("specify a valid template ID") || e.getMessage().contains("does not have permission")) ) {
                     return null;
                 }
                 throw e;
             }
             NodeList matches = doc.getElementsByTagName("template");
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);
 
                 MachineImage image = toImage(node, false);
 
                 if( image != null ) {
                     return image;
                 }
             }
             return null;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
         switch( cls ) {
             case KERNEL: return "kernel template";
             case RAMDISK: return "ramdisk template";
         }
         return "template";
     }
 
     private @Nullable String getRootVolume(@Nonnull String serverId) throws InternalException, CloudException {
         return provider.getComputeServices().getVolumeSupport().getRootVolumeId(serverId);
     }
     
     private Architecture guess(String desc) {
         Architecture arch = Architecture.I64;
         
         if( desc.contains("x64") ) {
             arch = Architecture.I64;
         }
         else if( desc.contains("x32") ) {
             arch = Architecture.I32;
         }
         else if( desc.contains("64 bit") ) {
             arch = Architecture.I64;
         }
         else if( desc.contains("32 bit") ) {
             arch = Architecture.I32;
         }
         else if( desc.contains("i386") ) {
             arch = Architecture.I32;
         }
         else if( desc.contains("64") ) {
             arch = Architecture.I64;
         }
         else if( desc.contains("32") ) {
             arch = Architecture.I32;
         }
         return arch;
     }
     
     private void guessSoftware(@Nonnull MachineImage image) {
         String[] components = ((image.getName() + " " + image.getDescription()).toLowerCase()).split(",");
         StringBuilder software = new StringBuilder();
         boolean comma = false;
 
         if( components == null || components.length < 0 ) {
             components = new String[] { (image.getName() + " " + image.getDescription()).toLowerCase() };
         }
         for( String str : components ) {
             if( str.contains("sql server") ) {
                 if( comma ) {
                     software.append(",");
                 }
                 if( str.contains("sql server 2008") ) {
                     software.append("SQL Server 2008");
                 }
                 else if( str.contains("sql server 2005") ) {
                     software.append("SQL Server 2005");
                 }
                 else {
                     software.append("SQL Server 2008");
                 }
                 comma = true;
             }
         }
         image.setSoftware(software.toString());
     }
 
     @Override
     public @Nonnull Requirement identifyLocalBundlingRequirement() throws CloudException, InternalException {
         return Requirement.NONE;
     }
 
     @Override
     protected @Nonnull MachineImage capture(@Nonnull ImageCreateOptions options, @Nullable AsynchronousTask<MachineImage> task) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.capture");
         try {
             String vmId = options.getVirtualMachineId();
             if( vmId == null ) {
                 throw new OperationNotSupportedException("Only options based off of servers are supported");
             }
             VirtualMachine server = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(vmId);
 
             if( server == null ) {
                 throw new CloudException("No such server: " + vmId);
             }
             CSMethod method = new CSMethod(provider);
             Document doc;
 
             String rootVolumeId = getRootVolume(vmId);
 
             if( rootVolumeId == null ) {
                 throw new CloudException("No root volume is attached to the target server.");
             }
 
             String baseSnapshotId = provider.getComputeServices().getSnapshotSupport().createSnapshot(SnapshotCreateOptions.getInstanceForCreate(rootVolumeId, "Template Builder " + (new Date()), "Template Builder from " + server.getProviderVirtualMachineId()));
             Snapshot baseSnapshot;
 
             if( baseSnapshotId == null ) {
                 throw new CloudException("Unable to make a new snapshot for machine image");
             }
             baseSnapshot = provider.getComputeServices().getSnapshotSupport().getSnapshot(baseSnapshotId);
             if( baseSnapshot == null ) {
                 throw new CloudException("Snapshot " + baseSnapshot + " went away.");
             }
             long timeout = System.currentTimeMillis() + (CalendarWrapper.MINUTE * 30L);
 
             while( !SnapshotState.AVAILABLE.equals(baseSnapshot.getCurrentState()) ) {
                 if( System.currentTimeMillis() > timeout ) {
                     throw new CloudException("Timeout in snapshotting root volume.");
                 }
                 try { Thread.sleep(15000L); }
                 catch( InterruptedException ignore ) { }
                 baseSnapshot = provider.getComputeServices().getSnapshotSupport().getSnapshot(baseSnapshot.getProviderSnapshotId());
                 if( baseSnapshot == null ) {
                     throw new CloudException("Snapshot disappeared while waiting for it to become available");
                 }
             }
             MachineImage img = getImage(server.getProviderMachineImageId());
             String osId = (img == null ? null : (String)img.getTag("cloud.com.os.typeId"));
             String name = validateName(options.getName());
             Param[] params = new Param[8];
 
             params[0] = new Param("name", name);
             params[1] = new Param("displayText", name);
             params[2] = new Param("osTypeId", osId == null ? toOs(server.getPlatform(),server.getArchitecture()) : osId);
             params[3] = new Param("zoneId", getContext().getRegionId());
             params[4] = new Param("isPublic", "false");
             params[5] = new Param("isFeatured", "false");
             params[6] = new Param("snapshotId", baseSnapshot.getProviderSnapshotId());
             params[7] = new Param("passwordEnabled", String.valueOf(isPasswordEnabled(server.getProviderMachineImageId())));
             doc = method.get(method.buildUrl(CREATE_TEMPLATE, params), CREATE_TEMPLATE);
 
             NodeList matches = doc.getElementsByTagName("templateid"); // v2.1
             String templateId = null;
 
             if( matches.getLength() > 0 ) {
                 templateId = matches.item(0).getFirstChild().getNodeValue();
             }
             if( templateId == null ) {
                 matches = doc.getElementsByTagName("id"); // v2.2
                 if( matches.getLength() > 0 ) {
                     templateId = matches.item(0).getFirstChild().getNodeValue();
                 }
             }
             if( templateId == null ) {
                 matches = doc.getElementsByTagName("jobid"); // v4.1
                 if( matches.getLength() > 0 ) {
                     templateId = matches.item(0).getFirstChild().getNodeValue();
                 }
             }
             if( templateId == null ) {
                 throw new CloudException("Failed to provide a template ID.");
             }
             Document responseDoc = provider.waitForJob(doc, "Create Template");
             if (responseDoc != null){
                 NodeList nodeList = responseDoc.getElementsByTagName("template");
                 if (nodeList.getLength() > 0) {
                     Node template = nodeList.item(0);
                     NodeList attributes = template.getChildNodes();
                     for (int i = 0; i<attributes.getLength(); i++) {
                         Node attribute = attributes.item(i);
                         String tmpname = attribute.getNodeName().toLowerCase();
                         String value;
 
                         if( attribute.getChildNodes().getLength() > 0 ) {
                             value = attribute.getFirstChild().getNodeValue();
                         }
                         else {
                             value = null;
                         }
                         if (tmpname.equalsIgnoreCase("id")) {
                             templateId = value;
                             break;
                         }
                     }
                 }
             }
             img = getImage(templateId);
             if( img == null ) {
                 throw new CloudException("Machine image job completed successfully, but no image " + templateId + " exists.");
             }
             if( task != null ) {
                 task.completeWithResult(img);
             }
             return img;
         }
         finally {
             APITrace.end();
         }
     }
     
     @Override
     public boolean isImageSharedWithPublic(@Nonnull String templateId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.isImageSharedWithPublic");
         try {
             CSMethod method = new CSMethod(provider);
             String url = method.buildUrl(LIST_TEMPLATES, new Param("templateFilter", "executable"));
             Document doc = method.get(url, LIST_TEMPLATES);
             NodeList matches = doc.getElementsByTagName("template");
 
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);
 
                 MachineImage image = toImage(node, true);
 
                 if( image != null && image.getProviderMachineImageId().equals(templateId) ) {
                     return true;
                 }
             }
             return false;
         }
         finally {
             APITrace.end();
         }
     }
     
     private boolean isPasswordEnabled(@Nonnull String templateId) throws InternalException, CloudException {
         APITrace.begin(getProvider(), "Image.isPasswordEnabled");
         try {
             CSMethod method = new CSMethod(provider);
             String url = method.buildUrl(LIST_TEMPLATES, new Param("templateFilter", "executable"));
             Document doc = method.get(url, LIST_TEMPLATES);
             NodeList matches = doc.getElementsByTagName("template");
 
             if( matches.getLength() > 0 ) {
                 Node node = matches.item(0);
 
                 Boolean val = isPasswordEnabled(templateId, node);
 
                 return (val != null && val);
             }
             return false;
         }
         finally {
             APITrace.end();
         }
     }
     
     private @Nullable Boolean isPasswordEnabled(@Nonnull String templateId, @Nullable Node node) {
         if( node == null ) {
             return null;
         }
         NodeList attributes = node.getChildNodes();
         boolean enabled = false;
         String id = null;
         
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             String name = attribute.getNodeName().toLowerCase();
             String value;
             
             if( attribute.getChildNodes().getLength() > 0 ) {
                 value = attribute.getFirstChild().getNodeValue();
             }
             else {
                 value = null;
             }
             if( name.equalsIgnoreCase("id") ) {
                 id = value;
             }
             else if( name.equalsIgnoreCase("passwordenabled") ) {
                 enabled = (value != null && value.equalsIgnoreCase("true"));
             }
             if( id != null && enabled ) {
                 break;
             }
         } 
         if( id == null || !id.equals(templateId) ) {
             return null;
         }
         return enabled;
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.isSubscribed");
         try {
             CSMethod method = new CSMethod(provider);
 
             try {
                 method.get(method.buildUrl(CSTopology.LIST_ZONES, new Param("available", "true")), CSTopology.LIST_ZONES);
                 return true;
             }
             catch( CSException e ) {
                 int code = e.getHttpCode();
 
                 if( code == HttpServletResponse.SC_FORBIDDEN || code == 401 || code == 531 ) {
                     return false;
                 }
                 throw e;
             }
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<ResourceStatus> listImageStatus(@Nonnull ImageClass cls) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.listImageStatus");
         try {
             if( !cls.equals(ImageClass.MACHINE) ) {
                 return Collections.emptyList();
             }
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(LIST_TEMPLATES, new Param("templateFilter", "self"), new Param("zoneId", ctx.getRegionId())), LIST_TEMPLATES);
             ArrayList<ResourceStatus> templates = new ArrayList<ResourceStatus>();
             NodeList matches = doc.getElementsByTagName("template");
 
             for( int i=0; i<matches.getLength(); i++ ) {
                 ResourceStatus status = toStatus(matches.item(i), false);
 
                 if( status != null ) {
                     templates.add(status);
                 }
             }
             return templates;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> listImages(@Nullable ImageFilterOptions options) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.listImages");
         try {
             CSMethod method = new CSMethod(provider);
             String accountNumber = (options == null ? null : options.getAccountNumber());
             Param[] params;
 
             if( accountNumber == null || provider.getServiceProvider().equals(CSServiceProvider.DATAPIPE) ) {
                 params = new Param[] { new Param("templateFilter", "self"),  new Param("zoneId", getContext().getRegionId()) };
             }
             else {
                 String domainId = provider.getDomainId(accountNumber);
                 String parentAccount = provider.getParentAccount(accountNumber);
                 params = new Param[] { new Param("templateFilter", "executable"),  new Param("zoneId", getContext().getRegionId()), new Param("account", parentAccount), new Param("domainId", domainId) };
             }
 
             Document doc = method.get(method.buildUrl(LIST_TEMPLATES, params), LIST_TEMPLATES);
 
             ArrayList<MachineImage> templates = new ArrayList<MachineImage>();
             NodeList matches = doc.getElementsByTagName("template");
 
             for( int i=0; i<matches.getLength(); i++ ) {
                 MachineImage image = toImage(matches.item(i), false);
 
                 if( image != null && (options == null || options.matches(image)) ) {
                     templates.add(image);
                 }
             }
             return templates;
         }
         finally {
             APITrace.end();
         }
     }
     
     @Override
     public @Nonnull Iterable<String> listShares(@Nonnull String templateId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.listShares");
         try {
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(LIST_TEMPLATE_PERMISSIONS, new Param("id", templateId)), LIST_TEMPLATES);
             TreeSet<String> accounts = new TreeSet<String>();
             NodeList matches = doc.getElementsByTagName("account");
 
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);
 
                 accounts.add(node.getFirstChild().getNodeValue());
             }
             return accounts;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
         return Collections.singletonList(ImageClass.MACHINE);
     }
 
     @Override
     public @Nonnull Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
         return Collections.singletonList(MachineImageType.VOLUME);
     }
 
     @Override
     public @Nonnull MachineImage registerImageBundle(@Nonnull ImageCreateOptions options) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.registerImageBundle");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             String atStorageLocation = options.getBundleLocation();
 
             if( atStorageLocation == null ) {
                 throw new OperationNotSupportedException("Cannot register a machine image without a location");
             }
             String name = validateName(options.getName());
 
             Platform platform = Platform.guess(name);
             Architecture architecture = guess(name);
             Param[] params = new Param[8];
 
             params[0] = new Param("name", name);
             params[1] = new Param("displayText", name);
             params[2] = new Param("url", atStorageLocation);
             MachineImageFormat fmt = options.getBundleFormat();
 
             if( fmt == null ) {
                 throw new CloudException("You must specify the bundle format for the new bundle");
             }
             if( MachineImageFormat.VHD.equals(options.getBundleFormat()) ) {
                 params[3] = new Param("format", "VHD");
             }
             else if( MachineImageFormat.RAW.equals(fmt) ) {
                 params[3] = new Param("format", "RAW");
             }
             else if( MachineImageFormat.QCOW2.equals(fmt) ) {
                 params[3] = new Param("format", "QCOW2");
             }
             else {
                 throw new OperationNotSupportedException("Unsupported bundle format: " + options.getBundleFormat());
             }
             params[4] = new Param("osTypeId", toOs(platform, architecture));
             params[5] = new Param("zoneId", ctx.getRegionId());
             params[6] = new Param("isPublic", "false");
             params[7] = new Param("isFeatured", "false");
 
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(REGISTER_TEMPLATE, params), REGISTER_TEMPLATE);
             NodeList matches = doc.getElementsByTagName("templateid");
             String templateId = null;
 
             if( matches.getLength() > 0 ) {
                 templateId = matches.item(0).getFirstChild().getNodeValue();
             }
             if( templateId == null ) {
                 throw new CloudException("No error was encountered during registration, but no templateId was returned");
             }
             provider.waitForJob(doc, "Create Template");
             MachineImage img = getImage(templateId);
 
             if( img == null ) {
                 throw new CloudException("Machine image " + templateId + " was created, but it does not exist");
             }
             return img;
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
         ArrayList<MachineImageFormat> formats = new ArrayList<MachineImageFormat>();
 
         formats.add(MachineImageFormat.QCOW2);
         formats.add(MachineImageFormat.VHD);
         formats.add(MachineImageFormat.RAW);
         return formats;
     }
 
     @Override
     public @Nonnull Iterable<MachineImageFormat> listSupportedFormatsForBundling() throws CloudException, InternalException {
         return Collections.emptyList();
     }
 
     @Override
     public void remove(@Nonnull String providerImageId, boolean checkState) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.remove");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for the request");
             }
             String accountNumber = ctx.getAccountNumber();
             MachineImage img = getImage(providerImageId);
 
             if( img == null ) {
                 throw new CloudException("No such machine image: " + providerImageId);
             }
             if( !accountNumber.equals(img.getProviderOwnerId())
                     && !provider.getParentAccount(accountNumber).equalsIgnoreCase(img.getProviderOwnerId())) {
                 throw new CloudException(accountNumber + " cannot remove images belonging to " + img.getProviderOwnerId());
             }
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(DELETE_TEMPLATE, new Param("id", providerImageId)), DELETE_TEMPLATE);
 
             provider.waitForJob(doc, "Delete Template");
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void removeAllImageShares(@Nonnull String providerImageId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.removeAllImageShares");
         try {
             for( String account : listShares(providerImageId) ) {
                 removeImageShare(providerImageId, account);
             }
             removePublicShare(providerImageId);
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void removeImageShare(@Nonnull String providerImageId, @Nonnull String accountNumber) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.removeImageShare");
         try {
             MachineImage img = getImage(providerImageId);
 
             if( img == null ) {
                 return;
             }
             if( !getContext().getAccountNumber().equals(img.getProviderOwnerId())
                     && !provider.getParentAccount(getContext().getAccountNumber()).equalsIgnoreCase(img.getProviderOwnerId())) {
                 return;
             }
             Param[] params = new Param[] { new Param("id", providerImageId), new Param("accounts", accountNumber), new Param("op", "remove") };
 
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(UPDATE_TEMPLATE_PERMISSIONS, params), UPDATE_TEMPLATE_PERMISSIONS);
 
             provider.waitForJob(doc, "Share Template");
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public void removePublicShare(@Nonnull String providerImageId) throws CloudException, InternalException {
         APITrace.begin(getProvider(), "Image.removePublicShare");
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             MachineImage img = getImage(providerImageId);
 
             if( img == null ) {
                 return;
             }
             if( !ctx.getAccountNumber().equals(img.getProviderOwnerId())
                     && !provider.getParentAccount(ctx.getAccountNumber()).equalsIgnoreCase(img.getProviderOwnerId())) {
                 return;
             }
             Param[] params = new Param[] { new Param("id", providerImageId), new Param("isPublic", "false") };
 
             CSMethod method = new CSMethod(provider);
             Document doc = method.get(method.buildUrl(UPDATE_TEMPLATE_PERMISSIONS, params), UPDATE_TEMPLATE_PERMISSIONS);
 
             provider.waitForJob(doc, "Share Template");
         }
         finally {
             APITrace.end();
         }
     }
 
     @Override
     public @Nonnull Iterable<MachineImage> searchPublicImages(final @Nonnull ImageFilterOptions options) throws CloudException, InternalException {
         final Param[] params;
 
         params = new Param[] { new Param("templateFilter", "executable"),  new Param("zoneId", getContext().getRegionId()) };
 
         final CSMethod method = new CSMethod(provider);
 
         provider.hold();
         PopulatorThread<MachineImage> populator = new PopulatorThread<MachineImage>(new JiteratorPopulator<MachineImage>() {
             @Override
             public void populate(@Nonnull Jiterator<MachineImage> iterator) throws Exception {
                 try {
                     APITrace.begin(getProvider(), "Image.searchPublicImages.populate");
                     try {
                         Document doc = method.get(method.buildUrl(LIST_TEMPLATES, params), LIST_TEMPLATES);
                         NodeList matches = doc.getElementsByTagName("template");
 
                         for( int i=0; i<matches.getLength(); i++ ) {
                             MachineImage img = toImage(matches.item(i), true);
 
                             if( img != null && options.matches(img) ) {
                                 iterator.push(img);
                             }
                         }
                     }
                     finally {
                         APITrace.end();
                     }
                 }
                 finally {
                     provider.release();
                 }
             }
         });
 
         populator.populate();
         return populator.getResult();
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
         return true;
     }
 
     @Override
     public boolean supportsImageSharingWithPublic() {
         return true;
     }
 
     @Override
     public boolean supportsPublicLibrary(@Nonnull ImageClass cls) throws CloudException, InternalException {
         return true;
     }
 
     private @Nullable MachineImage toImage(@Nullable Node node, boolean onlyIfPublic) throws CloudException, InternalException {
         if( node == null ) {
             return null;
         }
         Architecture bestArchitectureGuess = Architecture.I64;
         HashMap<String,String> properties = new HashMap<String,String>();
         NodeList attributes = node.getChildNodes();
         MachineImage image = new MachineImage();
         boolean isPublic = false;
         
         image.setProviderOwnerId(getContext().getAccountNumber());
         image.setType(MachineImageType.VOLUME);
         image.setCurrentState(MachineImageState.PENDING);
         image.setProviderRegionId(getContext().getRegionId());
         image.setTags(properties);
         image.setImageClass(ImageClass.MACHINE);
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             String name = attribute.getNodeName().toLowerCase();
             String value;
             
             if( attribute.hasChildNodes() && attribute.getChildNodes().getLength() > 0 ) {
                 value = attribute.getFirstChild().getNodeValue();
             }
             else {
                 value = null;
             }
             if( name.equals("id") ) {
                 image.setProviderMachineImageId(value);
             }
             else if( name.equals("zoneid") ) {
                 if( value == null || !value.equals(getContext().getRegionId()) ) {
                     return null;
                 }
             }
             else if( name.equalsIgnoreCase("account") ) {
                 if( value != null && value.startsWith("Customer [") ) {
                     value = value.substring("Customer [".length());
                     value = value.substring(0, value.length()-1);
                 }
                 image.setProviderOwnerId(value);
             }
             else if( name.equals("name") ) {
                 image.setName(value);
                 if( value != null && value.contains("x64") ) {
                     bestArchitectureGuess = Architecture.I64;
                 }
                 else if( value != null && value.contains("x32") ) {
                     bestArchitectureGuess = Architecture.I32;
                 }
             }
             else if( name.equals("displaytext") ) {
                 image.setDescription(value);
                 if( value != null && value.contains("x64") ) {
                     bestArchitectureGuess = Architecture.I64;
                 }
                 else if( value != null && value.contains("x32") ) {
                     bestArchitectureGuess = Architecture.I32;
                 }
             }
             else if( name.equals("ispublic") ) {
                 isPublic = (value != null && value.equalsIgnoreCase("true"));
             }
             else if( name.equals("ostypename") ) {
                 if( value != null && value.contains("64") ) {
                     bestArchitectureGuess = Architecture.I64;
                 }
                 else if( value != null && value.contains("32") ) {
                     bestArchitectureGuess = Architecture.I32;
                 }
                 if( value != null ) {
                     image.setPlatform(Platform.guess(value));
                 }
             }
             else if( name.equals("ostypeid") && value != null ) {
                 image.getTags().put("cloud.com.os.typeId", value);
             }
             else if( name.equals("bits") ) {
                 if( value == null || value.equals("64") ) {
                     image.setArchitecture(Architecture.I64);
                 }
                 else {
                     image.setArchitecture(Architecture.I32);
                 }
             }
             else if( name.equals("created") ) {
                 // 2010-06-29T20:49:28+1000
                 if( value != null ) {
                     image.setCreationTimestamp(provider.parseTime(value));
                 }
             }
             else if( name.equals("isready") ) {
                 if( value != null && value.equalsIgnoreCase("true") ) {
                     image.setCurrentState(MachineImageState.ACTIVE);
                 }
             }
             else if( name.equals("status") ) {
                 if( value == null || !value.equalsIgnoreCase("Download Complete") ) {
                     System.out.println("Template status=" + value);
                 }
             }
         }
         if( image.getPlatform() == null && image.getName() != null ) {
             image.setPlatform(Platform.guess(image.getName()));
         }
         if (image.getPlatform() == null) {
             image.setPlatform(Platform.UNKNOWN);
         }
         
         if( image.getArchitecture() == null ) {
             image.setArchitecture(bestArchitectureGuess);
         }
         if( !onlyIfPublic || isPublic ) {
             guessSoftware(image);
             return image;
         }
         return null;
     }
 
     private @Nullable ResourceStatus toStatus(@Nullable Node node, boolean onlyIfPublic) throws CloudException, InternalException {
         if( node == null ) {
             return null;
         }
         NodeList attributes = node.getChildNodes();
         MachineImageState imageState = null;
         String imageId = null;
         Boolean isPublic = null;
 
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             String name = attribute.getNodeName().toLowerCase();
             String value;
 
             if( attribute.hasChildNodes() && attribute.getChildNodes().getLength() > 0 ) {
                 value = attribute.getFirstChild().getNodeValue();
             }
             else {
                 value = null;
             }
             if( name.equals("id") ) {
                 imageId = value;
             }
             else if( name.equals("ispublic") ) {
                 isPublic = (value != null && value.equalsIgnoreCase("true"));
             }
             else if( name.equals("isready") ) {
                 if( value != null && value.equalsIgnoreCase("true") ) {
                     imageState = MachineImageState.ACTIVE;
                 }
             }
         }
         if( isPublic == null ) {
             isPublic = false;
         }
         if( imageId != null && (!onlyIfPublic || isPublic) ) {
             if( imageState == null ) {
                 imageState = MachineImageState.PENDING;
             }
             return new ResourceStatus(imageId, imageState);
         }
         return null;
     }
 
     private String toOs(Platform platform, Architecture architecture) throws InternalException, CloudException {
         CSMethod method = new CSMethod(provider);
         Document doc = method.get(method.buildUrl(LIST_OS_TYPES), LIST_OS_TYPES);
         NodeList matches = doc.getElementsByTagName("ostype");
         
         for( int i=0; i<matches.getLength(); i++ ) {
             NodeList attrs = matches.item(i).getChildNodes();
             Architecture arch = Architecture.I64;
             Platform pf = null;
             String id = null;
             
             for( int j=0; j<attrs.getLength(); j++ ) {
                 Node attr = attrs.item(j);
 
                 if( attr.getNodeName().equals("id") ) {
                     id = attr.getFirstChild().getNodeValue();
                 }
                 else if( attr.getNodeName().equals("description") ) {
                     String desc = attr.getFirstChild().getNodeValue();
                     
                     pf = Platform.guess(desc);
                     arch = guess(desc);
                 }
             }
             if( platform.equals(pf) && architecture.equals(arch) ) {
                 return id;
             }
         }
         return null;
     }
     
     private String validateName(String name) throws InternalException, CloudException {
         if( name.length() < 32 ) {
             return name;
         }
         name = name.substring(0,32);
         boolean found;
         int i = 0;
         
         do {
             found = false;
             for( MachineImage vm : listImages(ImageClass.MACHINE) ) {
                 if( vm.getName().equals(name) ) {
                     found = true;
                     break;
                 }
             }
             if( found ) {
                 i++;
                 if( i < 10 ) {
                     name = name.substring(0,31) + i;
                 }
                 else if( i < 100 ) {
                     name = name.substring(0, 30) + i;
                 }
                 else {
                     name = name.substring(0, 29) + i;
                 }
             }
         } while( found );
         return name;
     }
 }

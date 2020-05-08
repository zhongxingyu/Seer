 /**
  * Copyright (C) 2011-2012 enStratus Networks Inc
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
 
 package org.dasein.cloud.opsource.compute;
 
import java.io.InputStream;
import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.TreeSet;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 import org.apache.log4j.Logger;
 import org.dasein.cloud.*;
 import org.dasein.cloud.compute.*;
 import org.dasein.cloud.identity.ServiceAction;
 import org.dasein.cloud.opsource.OpSource;
 import org.dasein.cloud.opsource.OpSourceMethod;
 import org.dasein.cloud.opsource.Param;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class ServerImage implements MachineImageSupport {
     static private final Logger logger = OpSource.getLogger(ServerImage.class);
 
 	static private final String DEPLOYED_PATH = "deployed";
 	
 	static private final String PENDING_DEPLOY_PATH = "pendingDeploy";
 	
 	//Node tag name
 	static private final String OpSource_IMAGE_TAG = "ServerImage";
 	static private final String DEPLOYOED_IMAGE_TAG = "DeployedImage";
 	
 	static private final String DELETE_IMAGE = "delete";
 	
 	static private final String CREATE_IMAGE = "clone";
     
     private OpSource provider;
     
     public ServerImage(OpSource provider) {
         this.provider = provider;
     }
     
     public MachineImage getOpSourceImage(String imageId) throws InternalException, CloudException{
     	
     	ArrayList<MachineImage> images = (ArrayList<MachineImage>) listCustomerMachineImages();
 
         for( MachineImage img: images) {
 
             if(img.getProviderMachineImageId().equals(imageId)){
                 return img;
             }
         }
 
 
         images = (ArrayList<MachineImage>) listOpSourceMachineImages();
      
         for( MachineImage img: images) {       	
             
         	if(img.getProviderMachineImageId().equals(imageId)){
     			return img;
     		}    	
         }
         return null;    	
     }
 
     @Override
     public void addImageShare(@Nonnull String s, @Nonnull String s2) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
     @Override
     public void addPublicShare(@Nonnull String s) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
     @Nonnull
     @Override
     public String bundleVirtualMachine(@Nonnull String s, @Nonnull MachineImageFormat machineImageFormat, @Nonnull String s2, @Nonnull String s3) throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Override
     public void bundleVirtualMachineAsync(@Nonnull String s, @Nonnull MachineImageFormat machineImageFormat, @Nonnull String s2, @Nonnull String s3, @Nonnull AsynchronousTask<String> stringAsynchronousTask) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
     @Nonnull
     @Override
     public MachineImage captureImage(@Nonnull ImageCreateOptions imageCreateOptions) throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Override
     public void captureImageAsync(@Nonnull ImageCreateOptions imageCreateOptions, @Nonnull AsynchronousTask<MachineImage> machineImageAsynchronousTask) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
     @Nullable
     @Override
     public MachineImage getImage(@Nonnull String imageId) throws CloudException, InternalException {
         return getMachineImage(imageId);
     }
 
     @Override
     @Deprecated
     public MachineImage getMachineImage(String imageId) throws InternalException, CloudException {
     	//First check the pending images, because it is mostly being checked by customers
     	ArrayList<MachineImage> list = (ArrayList<MachineImage>) listCustomerMachinePendingImages();
     	for(MachineImage image : list){
     		if(image.getProviderMachineImageId().equals(imageId)){
     			return image;
     		}    		
     	}
     	
     	list = (ArrayList<MachineImage>) this.listCustomerMachineDeployedImages();
     	for(MachineImage image : list){
     		if(image.getProviderMachineImageId().equals(imageId)){
     			return image;
     		}    		
     	}
     	
     	list = (ArrayList<MachineImage>) listOpSourceMachineImages();
     	for(MachineImage image : list){
     		if(image.getProviderMachineImageId().equals(imageId)){
     			return image;
     		}    		
     	}    	
 
         return null;
     }
     
     @Override
     @Deprecated
     public @Nonnull String getProviderTermForImage(@Nonnull Locale locale) {
         return "OS Image";
     }
 
     @Nonnull
     @Override
     public String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass imageClass) {
         return "OS Image";
     }
 
     @Override
     public String getProviderTermForCustomImage(@Nonnull Locale locale, @Nonnull ImageClass cls){
         return "Customer Image";
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
     
     private void guessSoftware(MachineImage image) {
         String str = (image.getName() + " " + image.getDescription()).toLowerCase();
         StringBuilder software = new StringBuilder();
         boolean comma = false;
         
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
         image.setSoftware(software.toString());
     }
     
     @Override
     @Deprecated
     public boolean hasPublicLibrary() {
         return true;
     }
 
     @Nonnull
     @Override
     public Requirement identifyLocalBundlingRequirement() throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Override
     @Deprecated
     public AsynchronousTask<String> imageVirtualMachine(String vmId, String name, String description) throws CloudException, InternalException {
       
         final AsynchronousTask<String> task = new AsynchronousTask<String>();
         final String fname = name;
         final String fdesc = description;
         final String fvmId = vmId;
         
         Thread t = new Thread() {
             public void run() {
                 try {
                     MachineImage image = imageVirtualMachine(fvmId, fname, fdesc, task);
                 
                     task.completeWithResult(image.getProviderMachineImageId());
                 }
                 catch( Throwable t ) {
                     task.complete(t);
                 }
             }
         };
 
         t.start();
         return task;
     }
     
     private MachineImage imageVirtualMachine(String vmId, String name, String description, AsynchronousTask<String> task) throws CloudException, InternalException {
 
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.SERVER_BASE_PATH, null);
     	parameters.put(0, param);
     	   	
         param = new Param(vmId, null);
     	parameters.put(1, param);
    	
        param = new Param(CREATE_IMAGE, name);
     	parameters.put(2, param);
     	
     	// Can not use space in the url
         param = new Param("desc", description.replace(" ", "_"));
       	parameters.put(3, param);
     
     	OpSourceMethod method = new OpSourceMethod(provider,    			
     			provider.buildUrl(null,true, parameters),
     			provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
     	
    	if(method.parseRequestResult("Imaging", method.invoke(), "result", "resultDetail")){
     	   	//First check the pending images, because it is mostly being checked by customers
         	ArrayList<MachineImage> list = (ArrayList<MachineImage>) listCustomerMachinePendingImages();
         	for(MachineImage image : list){
         		if(image.getName().equals(name)){
         			return image;
         		}    		
         	}
         	//Check deployed Image
        	list = (ArrayList<MachineImage>) this.listCustomerMachineDeployedImages();
         	for(MachineImage image : list){
         		if(image.getName().equals(name)){
         			return image;
         		}    		
         	}    		
     	}
     	return null; 
     }
     
     @Override
     public boolean isImageSharedWithPublic(String templateId) throws CloudException, InternalException {
 
         return false;
     }
 
     @Override
     public boolean isSubscribed() throws CloudException, InternalException {
     	return true;
     }
 
     @Nonnull
     @Override
     public Iterable<ResourceStatus> listImageStatus(@Nonnull ImageClass imageClass) throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> listImages(@Nonnull ImageClass imageClass) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER: " + ServerImage.class.getName() + ".listOpSourceMachineImages()");
         }
 
         ArrayList<MachineImage> list = new ArrayList<MachineImage>();
 
         /** Get OpSource public Image */
         HashMap<Integer, Param> parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.IMAGE_BASE_PATH, null);
         parameters.put(0, param);
 
         param = new Param(provider.getDefaultRegionId(), null);
         parameters.put(1, param);
 
         OpSourceMethod method = new OpSourceMethod(provider,
                 provider.buildUrl(null, false, parameters),
                 provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
 
         Document doc = method.invoke();
 
         NodeList matches = doc.getElementsByTagName(OpSource_IMAGE_TAG);
         for( int i=0; i<matches.getLength(); i++ ) {
             Node node = matches.item(i);
 
             MachineImage image = toImage(node,false,false, "");
 
             if( image != null ) {
                 list.add(image);
             }
         }
 
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER: " + ServerImage.class.getName() + ".listOpSourceMachineImages()");
         }
 
         return list;
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> listImages(@Nonnull ImageClass imageClass, @Nonnull String ownedBy) throws CloudException, InternalException {
        //listCustomerMachineImages
        return null;  //TODO: Implement for 2013.01
     }
 
     @Override
     @Deprecated
     public Iterable<MachineImage> listMachineImages() throws InternalException, CloudException {
     	ArrayList<MachineImage> allList = new ArrayList<MachineImage>();
  
     	ArrayList<MachineImage> list = (ArrayList<MachineImage>) listCustomerMachineImages();
     	if(list != null){
     		allList.addAll(list);
     	}
     	/** Only list the private image */
     	
     	/**    	
  		list = (ArrayList<MachineImage>) listOpSourceMachineImages();
     	if(list != null){
     		allList.addAll(list);
     	}*/
     	
         return allList;
     }
 
     private Iterable<MachineImage> listCustomerMachineImages() throws InternalException, CloudException {
     	ArrayList<MachineImage> allList = new ArrayList<MachineImage>();
     	
     	ArrayList<MachineImage> list = (ArrayList<MachineImage>) listCustomerMachineDeployedImages();
     	if(list != null){
     		allList.addAll(list);
     	}
     	
     	list = (ArrayList<MachineImage>) this.listCustomerMachinePendingImages();
     	if(list != null){
     		allList.addAll(list);
     	}
         return allList;   
     }
     
     /**
      * https://<Cloud API URL>/oec/0.9/{orgid}/
 	 *	image/deployedWithSoftwareLabels/{location-id}
      */
     
     private Iterable<MachineImage> listCustomerMachineDeployedImages() throws InternalException, CloudException {
         if( logger.isTraceEnabled() ) {
         	logger.trace("ENTER: " + ServerImage.class.getName() + ".listCustomerMachineDeployedImages()");
         }
        try{        	
   
	    	
 	    	ArrayList<MachineImage> list = new ArrayList<MachineImage>();
 	    	
 	    	/** Get deployed Image */
 	        HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
 	        Param param = new Param(OpSource.IMAGE_BASE_PATH, null);
 	    	parameters.put(0, param);
 	    	param = new Param(DEPLOYED_PATH, null);
 	    	parameters.put(1, param);
 	    	
 	    	param = new Param(provider.getDefaultRegionId(), null);
 	    	parameters.put(2, param);   	
 	    
 	    	OpSourceMethod method = new OpSourceMethod(provider, provider.buildUrl(null, true, parameters),provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET", null));
 	    	Document doc = method.invoke();
 	        NodeList matches = doc.getElementsByTagName(DEPLOYOED_IMAGE_TAG);
 	        if(matches != null){
 	            for( int i=0; i<matches.getLength(); i++ ) {
 	                Node node = matches.item(i);            
	                MachineImage image = this.toImage(node, true, false, "");
 	                
 	                if( image != null ) {
 	                	list.add(image);
 	                }
 	            }
 	        }
 	        return list;
         }finally{        	
 	        if( logger.isTraceEnabled() ) {
 	        	logger.trace("Exit: " + ServerImage.class.getName() + ".listCustomerMachineDeployedImages()");
 	        }
         }
     }
     
     private Iterable<MachineImage> listCustomerMachinePendingImages() throws InternalException, CloudException {
         if( logger.isTraceEnabled() ) {
         	logger.trace("ENTER: " + ServerImage.class.getName() + ".listCustomerMachinePendingImages()");
         }
     	
     	ArrayList<MachineImage> list = new ArrayList<MachineImage>();
          
     	/** Get pending deployed Image */
         HashMap<Integer, Param> parameters = new HashMap<Integer, Param>();
     	Param param = new Param(OpSource.IMAGE_BASE_PATH, null);
     	parameters.put(0, param);
     	
     	param = new Param(PENDING_DEPLOY_PATH, null);
     	parameters.put(1, param);
     	
     	param = new Param(provider.getDefaultRegionId(), null);
     	parameters.put(2, param);
     
     	OpSourceMethod method = new OpSourceMethod(provider,
     								provider.buildUrl(null, true, parameters),
     								provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
     	Document doc = method.invoke();
         NodeList matches = doc.getElementsByTagName("PendingDeployImage");
         if(matches != null){
             for( int i=0; i<matches.getLength(); i++ ) {
                 Node node = matches.item(i);            
                 MachineImage image = toImage(node, true, true, "");
                 
                 if( image != null ) {
                 	list.add(image);
                 }
             }
         }
         if( logger.isTraceEnabled() ) {
         	logger.trace("EXIT: " + ServerImage.class.getName() + ".listCustomerMachinePendingImages()");
         }
     	
         return list;
     }
     
     
     public Iterable<MachineImage> listOpSourceMachineImages() throws InternalException, CloudException {
         if( logger.isTraceEnabled() ) {
         	logger.trace("ENTER: " + ServerImage.class.getName() + ".listOpSourceMachineImages()");
         }
     	
     	ArrayList<MachineImage> list = new ArrayList<MachineImage>();
     	
     	/** Get OpSource public Image */
     	HashMap<Integer, Param> parameters = new HashMap<Integer, Param>();
     	Param param = new Param(OpSource.IMAGE_BASE_PATH, null);
     	parameters.put(0, param);
     	
     	param = new Param(provider.getDefaultRegionId(), null);
     	parameters.put(1, param);
     
     	OpSourceMethod method = new OpSourceMethod(provider,
     						provider.buildUrl(null, false, parameters),
     						provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
         
     	Document doc = method.invoke();
     	
         NodeList matches = doc.getElementsByTagName(OpSource_IMAGE_TAG);
         for( int i=0; i<matches.getLength(); i++ ) {
             Node node = matches.item(i);
             
             MachineImage image = toImage(node,false,false, "");
             
             if( image != null ) {
             	list.add(image);
             }
         }        
 
         if( logger.isTraceEnabled() ) {
         	logger.trace("ENTER: " + ServerImage.class.getName() + ".listOpSourceMachineImages()");
         }    	
   
         return list;
     }
     
     @Override
     @Deprecated
     public Iterable<MachineImage> listMachineImagesOwnedBy(String accountId) throws CloudException, InternalException {
     	/** Only two types of owner OpSource, or customer itself */
         /** If no account specified, return all images */
         if (provider.getContext().getAccountNumber().equals(accountId)){
         	return listCustomerMachineImages();
         }else{
         	return listOpSourceMachineImages();
         }
     }
     
     @Override
     public Iterable<String> listShares(String templateId) throws CloudException, InternalException {
     	return new TreeSet<String>();     
     }
 
     @Nonnull
     @Override
     public Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Nonnull
     @Override
     public MachineImage registerImageBundle(@Nonnull ImageCreateOptions imageCreateOptions) throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Override
     public Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
         ArrayList<MachineImageFormat> list = new  ArrayList<MachineImageFormat>();
         list.add(MachineImageFormat.OVF);
         list.add(MachineImageFormat.VMDK);
         //TODO 
         //list.add(MachineImageFormat.valueOf("MF"));
         return list;
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImageFormat> listSupportedFormatsForBundling() throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Override
     public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
         return new String[0];
     }
     
     @Override
     public void remove(String imageId) throws InternalException, CloudException {
     	
         HashMap<Integer, Param>  parameters = new HashMap<Integer, Param>();
         Param param = new Param(OpSource.IMAGE_BASE_PATH, null);
     	parameters.put(0, param);
     	param = new Param(imageId, null);
     	parameters.put(1, param);    
     	OpSourceMethod method = new OpSourceMethod(provider, provider.buildUrl(DELETE_IMAGE,true, parameters),provider.getBasicRequestParameters(OpSource.Content_Type_Value_Single_Para, "GET",null));
     	method.requestResult("Removing image",method.invoke());
     }
 
     @Override
     public void remove(@Nonnull String providerImageId, boolean checkState) throws CloudException, InternalException{
         //TODO: Implement for 2013.02
     }
 
     @Override
     public void removeAllImageShares(@Nonnull String s) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
     @Override
     public void removeImageShare(@Nonnull String s, @Nonnull String s2) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
     @Override
     public void removePublicShare(@Nonnull String s) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
     public MachineImage searchImage(Platform platform, Architecture architecture, int cpuCount, int memoryInMb) throws InternalException, CloudException{
     
     	ArrayList<MachineImage> images = (ArrayList<MachineImage>) listOpSourceMachineImages();
      
         for( MachineImage img: images) {        	
             
             if( img != null ) {
                 if(architecture == null || (architecture != null && !architecture.equals(img.getArchitecture())) ) {
                     continue;
                 }
                 if((platform == null) || (platform != null && !platform.equals(Platform.UNKNOWN)) ) {
                    continue;
                 }
                 if(img.getTag("cpuCount") == null || img.getTag("memory") == null){
                 	continue;
                 }
                 
             	int currentCPU = Integer.valueOf((String) img.getTag("cpuCount"));
         		int currentMemory = Integer.valueOf((String) img.getTag("memory"));
         	
                 if(currentCPU == cpuCount && currentMemory == memoryInMb){
                 	return img;                	
                 } 
             }
         }
         return null;    	
     }
     
     @Override
     @Deprecated
     public Iterable<MachineImage> searchMachineImages(String keyword, Platform platform, Architecture architecture) throws InternalException, CloudException {
     	ArrayList<MachineImage> list = new ArrayList<MachineImage>();
     	
     	/**Search only OpSource public image, not include owner's images */
     	ArrayList<MachineImage> images = (ArrayList<MachineImage>) listOpSourceMachineImages();
      
         for( MachineImage img: images) {        	
             
             if( img != null ) {
                 if( architecture != null && !architecture.equals(img.getArchitecture()) ) {
                     continue;
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
                 if( keyword != null && !keyword.equals("") ) {
                     keyword = keyword.toLowerCase();
                     if( !img.getProviderMachineImageId().toLowerCase().contains(keyword) ) {
                         if( !img.getName().toLowerCase().contains(keyword) ) {
                             if( !img.getDescription().toLowerCase().contains(keyword) ) {
                                 continue;
                             }
                         }
                     }
                 }               
                 list.add(img);
             }
         }
         return list;
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> searchImages(@Nullable String s, @Nullable String s2, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Nonnull
     @Override
     public Iterable<MachineImage> searchPublicImages(@Nullable String s, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
         return null;  //TODO: Implement for 2013.01
     }
 
     @Override
     @Deprecated
     public void shareMachineImage(String templateId, String withAccountId, boolean allow) throws CloudException, InternalException {
     	  throw new OperationNotSupportedException("OpSource does not support share image.");
     }
 
     @Override
     @Deprecated
     public boolean supportsCustomImages() {
         return true;
     }
 
     @Override
     public boolean supportsDirectImageUpload() throws CloudException, InternalException {
         return false;  //TODO: Implement for 2013.01
     }
 
     @Override
     public boolean supportsImageCapture(@Nonnull MachineImageType machineImageType) throws CloudException, InternalException {
         return false;  //TODO: Implement for 2013.01
     }
 
     @Override
     public boolean supportsImageSharing() {
         return false;
     }
 
     @Override
     public boolean supportsImageSharingWithPublic() {
         return false;
     }
 
     @Override
     public boolean supportsPublicLibrary(@Nonnull ImageClass imageClass) throws CloudException, InternalException {
         return false;  //TODO: Implement for 2013.01
     }
 
     @Override
     public void updateTags(@Nonnull String s, @Nonnull Tag... tags) throws CloudException, InternalException {
         //TODO: Implement for 2013.01
     }
 
 
     private MachineImage toImage(Node node, boolean isCustomerDeployed, boolean isPending, String nameSpace) throws CloudException, InternalException {
         Architecture bestArchitectureGuess = Architecture.I64;
         MachineImage image = new MachineImage();
         
         HashMap<String,String> properties = new HashMap<String,String>();
         image.setTags(properties);
         
         NodeList attributes = node.getChildNodes();
               
         if(isCustomerDeployed){
         	
         	image.setProviderOwnerId(provider.getContext().getAccountNumber());
         	
         }else{
         	/** Default owner is opsource */        	  
             image.setProviderOwnerId(provider.getCloudName());        	
         }
        
         image.setType(MachineImageType.STORAGE);
         if(isPending){
         	image.setCurrentState(MachineImageState.PENDING);
         }else{
         	image.setCurrentState(MachineImageState.ACTIVE);
         }
         
         for( int i=0; i<attributes.getLength(); i++ ) {
             Node attribute = attributes.item(i);
             
             if(attribute.getNodeType() == Node.TEXT_NODE) continue;
             
             String name = attribute.getNodeName();
             String value;
             
             if( attribute.getChildNodes().getLength() > 0 ) {
                 value = attribute.getFirstChild().getNodeValue();
             }
             else {
                 continue;
             }
 
             String nameSpaceString = "";
             if(!nameSpace.equals("")) nameSpaceString = nameSpace + ":";
 
             if( name.equals(nameSpaceString + "id") ) {
                 image.setProviderMachineImageId(value);
             }else if(name.equals(nameSpaceString + "resourcePath") && value != null ){
             	image.getTags().put("resourcePath", value);
             }            
             else if( name.equals(nameSpaceString + "name") ) {
                 image.setName(value);
                 if(  value.contains("x64") ||  value.contains("64-bit") ||  value.contains("64 bit") ) {
                     bestArchitectureGuess = Architecture.I64;
                 }
                 else if(value.contains("x32") ) {
                     bestArchitectureGuess = Architecture.I32;
                 }
             }
             else if( name.equals(nameSpaceString + "description") ) {
                 image.setDescription(value);
                 if( value.contains("x64") ||  value.contains("64-bit") ||  value.contains("64 bit") ) {
                     bestArchitectureGuess = Architecture.I64;
                 }
                 else if( value.contains("x32") ||  value.contains("32-bit") ||  value.contains("32 bit")) {
                     bestArchitectureGuess = Architecture.I32;
                 }
             }
             else if( name.equals(nameSpaceString + "machineSpecification") ) {
             	NodeList machineAttributes  = attribute.getChildNodes();
             	for(int j=0;j<machineAttributes.getLength();j++ ){
             		Node machine = machineAttributes.item(j);
 	            	if( machine.getNodeName().equals(nameSpaceString + "operatingSystem") ){
 		            	 NodeList osAttributes  = machine.getChildNodes();
 		            	 for(int k=0;k<osAttributes.getLength();k++ ){
 		            		 Node os = osAttributes.item(k);
 		            		 
 		            		 if(os.getNodeType() == Node.TEXT_NODE) continue;
 		            		 
 		            		 String osName = os.getNodeName();
 		                     
 		            		 String osValue = null ;
 		            		 
 		                     if( osName.equals(nameSpaceString + "displayName") && os.getChildNodes().getLength() > 0 ) {
 		                    	 osValue = os.getFirstChild().getNodeValue();
 		                     }
 		                     else if( osName.equals(nameSpaceString + "type") && os.getChildNodes().getLength() > 0) {
 			                     image.setPlatform(Platform.guess(os.getFirstChild().getNodeValue()));			                       
 		                     }
 		                     else if( osName.equalsIgnoreCase(nameSpaceString + "cpuCount") && os.getFirstChild().getNodeValue() != null ) {
 		                    	 
 		                    	 image.getTags().put("cpuCount", os.getFirstChild().getNodeValue());
 		                     }  
 		                     else if( osName.equalsIgnoreCase(nameSpaceString + "memory") && os.getFirstChild().getNodeValue() != null ) {
 		                    	 image.getTags().put("memory", os.getFirstChild().getNodeValue());
 		                     }
 		                     
 		                     if( osValue != null ) {
 		                    	 bestArchitectureGuess = guess(osValue);
 		            		 }		            		           		     		 
 		            	 }
 	            	}
             	}             
             }
             else if( name.equals(nameSpaceString + "operatingSystem") ) {
             	
            	 	NodeList osAttributes  = attribute.getChildNodes();
            	 	
            	 	for(int j=0;j<osAttributes.getLength();j++ ){
            	 		
            	 		Node os = osAttributes.item(j);
            	 		         
            	 		if(os.getNodeType() == Node.TEXT_NODE) continue;
         		 
 	        		 String osName = os.getNodeName();              
 	                 
 	        		 String osValue = null ;
 	        		 
 	                 if( osName.equals(nameSpaceString + "displayName") && os.getChildNodes().getLength() > 0 ) {
 	                	 osValue = os.getFirstChild().getNodeValue();
 	                 }
 	                 else if( osName.equals(nameSpaceString + "type") && os.getChildNodes().getLength() > 0) {
 	                     image.setPlatform(Platform.guess(os.getFirstChild().getNodeValue()));			                       
 	                 }
 	                 else if( osName.equalsIgnoreCase(nameSpaceString + "cpuCount") && os.getFirstChild().getNodeValue() != null ) {
 	                	 
 	                	 image.getTags().put("cpuCount", os.getFirstChild().getNodeValue());
 	                 }  
 	                 else if( osName.equalsIgnoreCase(nameSpaceString + "memory") && os.getFirstChild().getNodeValue() != null ) {
 	                   
 	                	 image.getTags().put("memory", os.getFirstChild().getNodeValue());
 	                 }
 	                 	                           	
 	           		 if( osValue != null  ) {
 	                      bestArchitectureGuess = guess(osValue);
 	           		 }
 	           		 
 	           		 if( osValue != null ) {	           			 
 	           			 image.setPlatform(Platform.guess(osValue));	                    
 	           		 }           		 
            	 	}            	
             
            }
           else if( name.equals(nameSpaceString + "location") && value != null) {
        	   if(! provider.getDefaultRegionId().equals(value)){
         		  return null;  
         	   }
         	   image.setProviderRegionId(value);
         	   
            }
            else if( name.equals(nameSpaceString + "cpuCount") && value != null ) {
         	
         	   image.getTags().put("cpuCount", value);
  
            }  
            else if( name.equals(nameSpaceString + "memory") && value != null ) {
         	   image.getTags().put("memory", value);
            } 
            else if( name.equals("created") ) {
                // 2010-06-29T20:49:28+1000
                // TODO: implement when dasein cloud supports template creation timestamps
            	
            }
            else if( name.equals(nameSpaceString + "osStorage") ) {
               // TODO
            }
            else if( name.equals(nameSpaceString + "location") ) {
             	image.setProviderRegionId(value);
            }           
            else if( name.equals(nameSpaceString + "deployedTime") ) {
                 // 2010-06-29T20:49:28+1000
                 // TODO: implement when dasein cloud supports template creation timestamps
            }else if( name.equals(nameSpaceString + "sourceServerId") ) {
             	//TODO
            }else if( name.equals(nameSpaceString + "softwareLabel") ) {
             	image.setSoftware(value);
            }
             
         }
         if(image.getDescription() == null || image.getDescription().equals("")){
             image.setDescription(image.getName());
         }
         if( image.getPlatform() == null && image.getName() != null ) {
             image.setPlatform(Platform.guess(image.getName()));            
         }
         if( image.getPlatform().equals(Platform.UNKNOWN) && image.getDescription()!= null ) {
             image.setPlatform(Platform.guess(image.getDescription()));            
         }
         if(image.getPlatform().equals(Platform.UNKNOWN)){
         	if(image.getName().contains("Win2008") || image.getName().contains("Win2003")){
         		image.setPlatform(Platform.WINDOWS);        		
         	}        	
         }
         if( image.getArchitecture() == null ) {
             image.setArchitecture(bestArchitectureGuess);
         }
         if( image.getSoftware() == null ) {
         	guessSoftware(image);        	
         }
         return image;       
     }
 }

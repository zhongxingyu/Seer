 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.resourcemanager;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.Level;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.Configuration;
 import com.delcyon.capo.CapoApplication.Location;
 import com.delcyon.capo.Configuration.PREFERENCE;
 import com.delcyon.capo.annotations.DefaultDocumentProvider;
 import com.delcyon.capo.annotations.DirectoyProvider;
 import com.delcyon.capo.controller.ControlElement;
 import com.delcyon.capo.controller.elements.ResourceElement;
 import com.delcyon.capo.preferences.Preference;
 import com.delcyon.capo.preferences.PreferenceInfo;
 import com.delcyon.capo.preferences.PreferenceInfoHelper;
 import com.delcyon.capo.preferences.PreferenceProvider;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
 import com.delcyon.capo.resourcemanager.remote.RemoteResourceType;
 import com.delcyon.capo.resourcemanager.types.ContentMetaData;
 import com.delcyon.capo.resourcemanager.types.FileResourceType;
 
 /**
  * @author jeremiah
  *
  */
 @PreferenceProvider(preferences=ResourceManager.Preferences.class)
 @DirectoyProvider(preferenceName="RESOURCE_DIR",preferences=Configuration.PREFERENCE.class,location=Location.SERVER)
 public class ResourceManager extends CapoDataManager
 {
 	
 	public enum Preferences implements Preference
 	{
 		
 		@PreferenceInfo(arguments={"type"}, defaultValue="file", description="URI scheme name to use if no scheme is present. ", longOption="DEFAULT_RESOURCE_TYPE", option="DEFAULT_RESOURCE_TYPE")
 		DEFAULT_RESOURCE_TYPE;
 		
 		@Override
 		public String[] getArguments()
 		{
 			return PreferenceInfoHelper.getInfo(this).arguments();
 		}
 
 		@Override
 		public String getDefaultValue()
 		{
 			return PreferenceInfoHelper.getInfo(this).defaultValue();
 		}
 
 		@Override
 		public String getDescription()
 		{
 			return PreferenceInfoHelper.getInfo(this).description();
 		}
 
 		@Override
 		public String getLongOption()
 		{
 			return PreferenceInfoHelper.getInfo(this).longOption();
 		}
 
 		@Override
 		public String getOption()
 		{
 		
 			return PreferenceInfoHelper.getInfo(this).option();
 		}
 	
 		@Override
 		public Location getLocation() 
 		{
 			return PreferenceInfoHelper.getInfo(this).location();
 		}
 	}
 	
 	private ResourceDescriptor dataDir;
 
 	
 	
 	private transient Transformer transformer;	
 	private HashMap<String, ResourceDescriptor> directoryHashMap = new HashMap<String, ResourceDescriptor>();	
 	private HashMap<String,ResourceType> schemeResourceTypeHashMap = new HashMap<String, ResourceType>();
 	
 	
 	public ResourceManager() throws Exception
 	{
 		loadResourceTypes();
 		
 		//setup xml output
 		TransformerFactory tFactory = TransformerFactory.newInstance();
 		transformer = tFactory.newTransformer();
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 		
 		//Build and load directories		
 		//no place should use append resource URI's like this except here where the resource manager hasn't been loaded yet, so auto resolution can't happen.
 		dataDir =  getResourceDescriptor(null,CapoApplication.getConfiguration().getValue(PREFERENCE.CAPO_DIR));
 		ContentMetaData dataDirContentMetaData = dataDir.getContentMetaData(null);
 		if (dataDirContentMetaData.exists() == false)
         {
 		    
 			//don't create this directory if auto sync is disabled.  
 			if(CapoApplication.getConfiguration().hasOption(PREFERENCE.DISABLE_CONFIG_AUTOSYNC) == false)
 			{
 				dataDir.performAction(null, Action.CREATE,new ResourceParameter(ResourceDescriptor.DefaultParameters.CONTAINER, "true"));
 				dataDir.close(null);
 				dataDir.open(null);
 			}			
         }
 		directoryHashMap.put(PREFERENCE.CAPO_DIR.toString(), dataDir);
 		
 		
 	}
 	
 	@Override
 	public void init() throws Exception
 	{
 	    
 	  //build dynamic directories
         Preference[] directoryPreferences = CapoApplication.getConfiguration().getDirectoryPreferences();
         for (Preference preference : directoryPreferences)
         {
             
             ResourceDescriptor dynamicDir = dataDir.getChildResourceDescriptor(null,CapoApplication.getConfiguration().getValue(preference));
             
             ContentMetaData dynamicDirContentMetaData = dynamicDir.getContentMetaData(null);
             if (dynamicDirContentMetaData.exists() == false)
             {            	
                 dynamicDir.performAction(null, Action.CREATE,new ResourceParameter(ResourceDescriptor.DefaultParameters.CONTAINER, "true"));
                 dynamicDir.close(null);
                 dynamicDir.open(null);
             }           
             directoryHashMap.put(preference.toString(), dynamicDir);
         }
         
         //verify and create default documents
         DefaultDocumentProvider[] defaultDocumentProviders = CapoApplication.getConfiguration().getDefaultDocumentProviders();
         for (DefaultDocumentProvider defaultDocumentProvider : defaultDocumentProviders)
         {
             if (defaultDocumentProvider.location() != Location.BOTH)
             {
                 if (defaultDocumentProvider.location() == Location.CLIENT && CapoApplication.isServer() == true)
                 {
                     continue;
                 }
                 if (defaultDocumentProvider.location() == Location.SERVER && CapoApplication.isServer() == false)
                 {
                     continue;
                 }
             }
         	
             Preference preference = CapoApplication.getConfiguration().getPreference(defaultDocumentProvider.directoryPreferenceName());
             String[] fileNames = defaultDocumentProvider.name().split(",");
             for (String fileName : fileNames)
 			{
             	ResourceDescriptor dynamicFile = directoryHashMap.get(preference.toString()).getChildResourceDescriptor(null,fileName);
                 if (dynamicFile.getContentMetaData(null).exists() == false)
                 {
                     Document document = CapoApplication.getDefaultDocument(fileName);
                     OutputStream outputStream = dynamicFile.getOutputStream(null);
                     transformer.transform(new DOMSource(document), new StreamResult(outputStream));             
                     outputStream.close();
                 }
 			}
             
         }
         
 			
 	}
 	
 	private void loadResourceTypes() throws Exception
 	{
 		Set<String> resourceTypeProvidersSet = CapoApplication.getAnnotationMap().get(ResourceTypeProvider.class.getCanonicalName());
 		if (resourceTypeProvidersSet != null)
 		{
 			for (String className : resourceTypeProvidersSet)
 			{
 				try
 				{
 					String[] schemes = Class.forName(className).getAnnotation(ResourceTypeProvider.class).schemes();
 					ResourceType resourceType = (ResourceType) Class.forName(className).newInstance();
 					for (String scheme : schemes)
 					{
 						CapoApplication.logger.log(Level.CONFIG, "loaded "+resourceType.getClass().getName()+" as '"+scheme+":' resourceType");
 						schemeResourceTypeHashMap.put(scheme.toLowerCase(), resourceType);
 					}
 				} catch (ClassNotFoundException classNotFoundException)
 				{
 					CapoApplication.logger.log(Level.WARNING, "Error getting resource type providers",classNotFoundException);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public ResourceDescriptor getResourceDirectory(String resourceDirectoryPreferenceName)
 	{
 		return directoryHashMap.get(resourceDirectoryPreferenceName);
 	}
 		
 	/**
 	 * Called from anyplace that needs a resource
 	 */
 	@Override
 	public ResourceDescriptor getResourceDescriptor(ControlElement callingControlElement,String resourceURI) throws Exception
 	{
 		
 		String scheme = getScheme(resourceURI);
 		
 		
 		//if there is no scheme, and it didn't come from client request, assume it's a default resource type. Which unless overridden is a file:.
 		if (scheme == null) 
 		{
 			scheme = CapoApplication.getConfiguration().getValue(Preferences.DEFAULT_RESOURCE_TYPE);
 		}
 		
 		
 		//we get here when called from a place that uses a parsable uri
 		ResourceType resourceType = getResourceType(scheme);
 		if (resourceType != null)
 		{
 			if (resourceType instanceof RemoteResourceType)
 			{
 				RemoteResourceType remoteResourceType = (RemoteResourceType) resourceType;
 				return remoteResourceType.getResourceDescriptor(callingControlElement.getControllerClientRequestProcessor(),resourceURI);
 			}
 			else
 			{
 				return resourceType.getResourceDescriptor(resourceURI);
 			}
 		}
 		else
 		{
 			
 			throw new Exception("No matching ResourceType for: "+resourceURI);			
 		}		
 	}
 	@Override
 	public ResourceType getResourceType(String scheme)
 	{
 		return schemeResourceTypeHashMap.get(scheme.toLowerCase());
 	}
 	
 	/**
 	 * This gets called from ResourceElement when we encounter a resource element declaration
 	 */
 	
 	public ResourceDescriptor createResourceDescriptor(ResourceElement resourceElement, ResourceParameter[] resourceParameters) throws Exception
 	{
 
 	    //TODO verify our special brand of file URLs
 	    String resourceURI = null;
 	    if (resourceElement != null)
 	    {
 	        resourceURI = resourceElement.getURIValue();
 	    }
 
 	    //figure out resource type
 		String scheme = getScheme(resourceURI);
 		//if we don't know what this assume it's a default resource type. Which unless overridden is a file.
 		if (resourceURI != null && scheme == null && resourceElement == null)
 		{
 			scheme = CapoApplication.getConfiguration().getValue(Preferences.DEFAULT_RESOURCE_TYPE);
 		}
 		
 		ResourceType resourceType = schemeResourceTypeHashMap.get(scheme.toLowerCase());
 		if (resourceType != null)
 		{
 			ResourceDescriptor resourceDescriptor = resourceType.getResourceDescriptor(resourceURI);
 			//TODO Application level resourceDescriptor caching
 			//declaredResourceDecriptorHashMap.put(resourceElement.getName()+"@"+resourceElement.getURIValue(), resourceDescriptor);
 			return resourceDescriptor;
 		}
 		else
 		{
 			CapoApplication.logger.log(Level.WARNING, "No matching ResourceType for: "+resourceURI);
 			throw new Exception("No matching ResourceType for: "+resourceURI);
 		}	
 		
 	}	
 	//DOCUMENT PROCESSING
 	@Override
 	public ResourceDescriptor findDocumentResourceDescriptor(String documentName, String clientID, Preference directoryPreference) throws Exception
 	{
 	  //remove extension from documentName
         documentName = documentName.replaceAll("(.*)\\.[Xx][MmSs][Ll]", "$1");
         
         //check the client file system (if we have a client id)
         ResourceDescriptor foundResourceDescriptor = null;
         if (clientID != null)
         {
             ResourceDescriptor clientResourceDescriptor = getResourceDescriptor(null, "clients:"+clientID);
             //see if we have a child filesystem
             if (clientResourceDescriptor.getContentMetaData(null).exists() == true)
             {
                 ResourceDescriptor relaventChildResourceDescriptor = clientResourceDescriptor.getChildResourceDescriptor(null,CapoApplication.getConfiguration().getValue(directoryPreference));
                 if (relaventChildResourceDescriptor != null && relaventChildResourceDescriptor.getContentMetaData(null).exists() == true)
                 {
                     //search the client filesystem
                     foundResourceDescriptor = getDocumentResourceDescriptor(relaventChildResourceDescriptor, documentName);
                 }
             }
            
         }
         
         //check server file system   if wo don't have a document RD yet
         if (foundResourceDescriptor == null)
         {
          
             ResourceDescriptor relaventChildResourceDescriptor = getResourceDirectory(directoryPreference.toString());
             if (relaventChildResourceDescriptor != null && relaventChildResourceDescriptor.getContentMetaData(null).exists() == true)
             {
                 //search the relevant file system
                 foundResourceDescriptor = getDocumentResourceDescriptor(relaventChildResourceDescriptor, documentName);
             }
         }
         //if not null return parsed document
         if (foundResourceDescriptor != null && foundResourceDescriptor.getContentMetaData(null).exists() == true)
         {
             return foundResourceDescriptor;
         }
         
         
         //both should call getDocument with an appropriate directory container
         //so this method should check for the existence of a container, then try them in order of priority, child, then server
         return foundResourceDescriptor;
 	}
 	
 	@Override
 	public Document findDocument(String documentName, String clientID, Preference directoryPreference) throws Exception
 	{
 	    ResourceDescriptor documentResourceDescriptor = findDocumentResourceDescriptor(documentName, clientID, directoryPreference);
 	    if (documentResourceDescriptor != null)
 	    {
 	    	Document document = CapoApplication.getDocumentBuilder().parse(documentResourceDescriptor.getInputStream(null));
 	    	document.setDocumentURI(documentResourceDescriptor.getResourceURI());
 	        return document;
 	    }
 	    else
         {
             //if were STILL null, try class path
             //try class loaders
 	    	if (documentName.matches(".*\\.[Xx][MmSs][Ll]") == false)
 	    	{
 	    		documentName = documentName+".xml";
 	    	}
             URL moduleURL = ClassLoader.getSystemResource(CapoApplication.getConfiguration().getValue(directoryPreference)+"/"+documentName);
             if (moduleURL != null)
             {
             	Document document = CapoApplication.getDocumentBuilder().parse(moduleURL.openStream());
             	document.setDocumentURI(moduleURL.toString());
                 return document;
             }
         }
 	    
 	    return null;
 	}
 	
 	private ResourceDescriptor getDocumentResourceDescriptor(ResourceDescriptor parentDirectory, String documentName) throws Exception
     {
 		
         //bak here
         ContentMetaData contentMetaData =  parentDirectory.getContentMetaData(null);
         
         List<ContentMetaData> childResourceList = contentMetaData.getContainedResources();
         for (ContentMetaData childContentMetaData : childResourceList)
         {
            if (childContentMetaData.isContainer() == false && childContentMetaData.getResourceURI().matches(".*"+documentName+"\\.[Xx][MmSs][Ll]"))
             {
                 return  CapoApplication.getDataManager().getResourceDescriptor(null, childContentMetaData.getResourceURI());
                 
             }
            else if (childContentMetaData.isContainer() && childContentMetaData.getResourceURI().endsWith(documentName))
             {
                 ResourceDescriptor moduleResourceDescriptor =  CapoApplication.getDataManager().getResourceDescriptor(null, childContentMetaData.getResourceURI());
                 return getDocumentResourceDescriptor(moduleResourceDescriptor, documentName);
             }
         }
         
         return null;
     }
 	
 	@Override
 	public List<ResourceDescriptor> findDocuments(ResourceDescriptor parentDirectory) throws Exception
 	{
 	    Vector<ResourceDescriptor> resourceDescriptorVector = new Vector<ResourceDescriptor>();
 	    ContentMetaData contentMetaData =  parentDirectory.getContentMetaData(null);
         
         List<ContentMetaData> childResourceList = contentMetaData.getContainedResources();
         for (ContentMetaData childContentMetaData : childResourceList)
         {
             if (childContentMetaData.isContainer() == false && childContentMetaData.getResourceURI().matches(".*\\.[Xx][MmSs][Ll]"))
             {
                 resourceDescriptorVector.add(CapoApplication.getDataManager().getResourceDescriptor(null, childContentMetaData.getResourceURI()));
                 
             }
             else if (childContentMetaData.isContainer())
             {
                 ResourceDescriptor moduleResourceDescriptor =  CapoApplication.getDataManager().getResourceDescriptor(null, childContentMetaData.getResourceURI());
                 resourceDescriptorVector.addAll(findDocuments(moduleResourceDescriptor));
             }
         }
         return resourceDescriptorVector;
 	}
 	
 	//URI PROCESSIG
 	
 	public static String getScheme(String capoURIString)
 	{
 		String scheme = null;
 		
 		int schemeDeliminatorIndex = capoURIString.indexOf(":");
 		if(schemeDeliminatorIndex > 0)
 		{
 			scheme = capoURIString.substring(0, schemeDeliminatorIndex);			
 		}
 		return scheme;
 	}
 	
 	public static String getSchemeSpecificPart(String capoURIString)
 	{
 		String scheme = null;
 		String uriRemainder = null;
 		int schemeDeliminatorIndex = capoURIString.indexOf(":");
 		if(schemeDeliminatorIndex > 0)
 		{
 			scheme = capoURIString.substring(0, schemeDeliminatorIndex);
 			uriRemainder = capoURIString.substring(schemeDeliminatorIndex+1);
 		}
 		if (scheme == null)
 		{
 			return capoURIString;
 		}
 		else
 		{
 			return uriRemainder;
 		}
 	}
 	
 	public static boolean isOpaque(String resourceURI)
 	{
 		return getSchemeSpecificPart(resourceURI).startsWith("/") == false;
 	}
 
 	public static String getAuthroity(String resourceURI)
 	{
 		if (resourceURI.matches(".+//.+/.+"))
 		{
 			return resourceURI.replaceFirst(".+//(.+)/.+", "$1");
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	public static String removeURN(String uriString)
 	{
 		while(getScheme(uriString) != null)
 		{
 			uriString = getSchemeSpecificPart(uriString); 
 		}
 		return uriString;
 	}
 
 	//SEQUENCE PROCESSING
 	
 	@Override
 	public synchronized long nextValue(String sequenceName) throws Exception
 	{
 		ResourceDescriptor sequenceFile = getResourceDescriptor(null,sequenceName+".seq");
 		sequenceFile.addResourceParameters(null,new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONFIG_DIR));
 		if (sequenceFile.getContentMetaData(null).exists() == false)
 		{
 			//initialize sequence file
 		    sequenceFile.performAction(null, Action.CREATE);
 		    sequenceFile.close(null);
 		    sequenceFile.open(null);
             
             OutputStream sequenceFileOutputStream = sequenceFile.getOutputStream(null);
             sequenceFileOutputStream.write("0".getBytes());
             sequenceFileOutputStream.close();
 		}
 		sequenceFile.open(null);
 		InputStream sequenceFileInputStream = sequenceFile.getInputStream(null);
 		byte[] buffer = new byte[4096];
 		sequenceFileInputStream.read(buffer);
 		sequenceFileInputStream.close();
 		long nextValue = Long.parseLong(new String(buffer).trim());
 		nextValue++;
 		OutputStream sequenceFileOutputStream = sequenceFile.getOutputStream(null);
 		sequenceFileOutputStream.write((nextValue+"").getBytes());
 		sequenceFileOutputStream.close();
 		sequenceFile.close(null);
 		return nextValue;
 	}
 	
 }
 

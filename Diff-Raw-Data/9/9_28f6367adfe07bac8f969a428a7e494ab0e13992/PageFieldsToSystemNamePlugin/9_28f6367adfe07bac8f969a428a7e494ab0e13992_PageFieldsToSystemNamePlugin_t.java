 package com.hannonhill.cascade.plugin;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.cms.assetfactory.PluginException;
 import com.cms.assetfactory.FatalPluginException;
 import com.hannonhill.cascade.api.asset.admin.AssetFactory;
 import com.hannonhill.cascade.api.asset.home.FolderContainedAsset;
 import com.hannonhill.cascade.api.asset.home.Page;
 import com.hannonhill.cascade.api.asset.common.StructuredDataNode;
 import com.hannonhill.cascade.api.asset.common.DynamicMetadataField;
 
 /**
  * Plug-in which accepts a comma-delimited list of Page Field identifiers and constructs a 
  * URL-safe system name based on the provided Page Field values.  Expands the StructuredDataFieldsToSystemNamePlugin
  * functionality to allow for inclusion of Wired or Dynamic Metadata fields in addition to Structured Data fields.<br/><br/>
  * 
  * The expected format of the field identifier Strings is as follows:<br/><br/>
  * 
  * Wired Metadata fields:  [field-name]  e.g. title,display-name,author<br/><br/>
  * 
  * Dynamic Metadata fields:  [dynamic-metadata/field-name]  e.g. dynamic-metadata/my-custom-field1,dynamic-metadata/my-custom-field2<br/><br/>
  * 
  * Structured Data (Data Definition) fields:  [system-data-structure/{group-name}/field-name]  e.g. system-data-structure/my-group/my-field,system-data-structure/my-ungrouped-field
  * 
  * @author Brent Arrington
  */
 public final class PageFieldsToSystemNamePlugin extends StructuredDataPagePlugin
 {
     /** The resource bundle key for the name of the plugin */
     private static final String NAME_KEY = "plugin.assetfactory.pagefieldstosystemname.name";
     /** The resource bundle key for the description of the plugin */
     private static final String DESC_KEY = "plugin.assetfactory.pagefieldstosystemname.description";
     /** The resource bundle key for the name of the Field IDs parameter */
     private static final String FIELDIDS_PARAM_NAME_KEY = "plugin.assetfactory.pagefieldstosystemname.parameter.fieldids.name";
     /** The resource bundle key for the description of the Field IDs parameter */
     private static final String FIELDIDS_PARAM_DESC_KEY = "plugin.assetfactory.pagefieldstosystemname.parameter.fieldids.description";
     /** The resource bundle key for the name of the Space Token parameter */
     private static final String SPACETOKEN_PARAM_NAME_KEY = "plugin.assetfactory.pagefieldstosystemname.parameter.spacetoken.name";
     /** The resource bundle key for the description of the Space Token parameter */
     private static final String SPACETOKEN_PARAM_DESC_KEY = "plugin.assetfactory.pagefieldstosystemname.parameter.spacetoken.description";
     /** The resource bundle key for the name of the Concatenation Token parameter **/
     private static final String CONCATTOKEN_PARAM_NAME_KEY = "plugin.assetfactory.pagefieldstosystemname.parameter.concattoken.name";
     /** The resource bundle key for the description of the Space Token parameter */
     private static final String CONCATTOKEN_PARAM_DESC_KEY = "plugin.assetfactory.pagefieldstosystemname.parameter.concattoken.description";
 
     /**
      * @see com.cms.assetfactory.BaseAssetFactoryPlugin#doPluginActionPost(com.hannonhill.cascade.api.asset.admin.AssetFactory, com.hannonhill.cascade.api.asset.home.FolderContainedAsset)
      */
     @Override
     public void doPluginActionPost(AssetFactory factory, FolderContainedAsset asset) throws PluginException
     {
         //code in this method will be executed after the users submits the creation.
         //This could be used for data validation or post-population/property transfer.
     	
     	// check for valid asset type for this plugin
     	// throw exception & forbid asset creation for invalid types
     	if (!this.isValidType(asset))
     	{
     		this.setAllowCreation(false, "PageFieldsToSystemNamePlugin: This plug-in may only be applied to Page asset factories.");
     		throw new FatalPluginException("PageFieldsToSystemNamePlugin: This plug-in may only be applied to Page asset factories.");
     	}
     	
     	Page page = (Page)asset;
     	
     	String stIdentifiers = getParameter(FIELDIDS_PARAM_NAME_KEY);
     	// if no fields are specified for auto-naming values, throw exception & forbid asset creation
     	if (stIdentifiers == null || stIdentifiers.trim().equals(""))
     	{
     		this.setAllowCreation(false, "PageFieldsToSystemNamePlugin: Field IDs are required for this plugin.");
     		throw new FatalPluginException("PageFieldsToSystemNamePlugin: Field IDs are required for this plugin.");
     	}
     	
     	String stSpaceToken = getParameter(SPACETOKEN_PARAM_NAME_KEY);
     	// if no space token is explicitly provided, default to dash ("-")
     	if (stSpaceToken == null || stSpaceToken.trim().equals(""))
     	{
     		stSpaceToken = "-";
     	}
     	
        	String stConcatToken = getParameter(CONCATTOKEN_PARAM_NAME_KEY);
        	// if no concatenation token is explicitly provided, default to dash ("-")
     	if (stConcatToken == null || stConcatToken.trim().equals(""))
     	{
     		stConcatToken = "-";
     	}
     	
     	StringBuilder newName = new StringBuilder();
     	String[] arIdentifiers = stIdentifiers.split(",");
     	
     	// iterate through specified fields & use derived values to build name string
     	// if any of the specified fields contain null or empty values, throw exception & forbid asset creation
     	for (String stIdentifier : arIdentifiers)
     	{
     		String stNodeVal = "";
     		// determine what type of field we are dealing with
     		if (stIdentifier.contains(CUSTOM_METADATA_TOKEN))
     		{
     			// dynamic metadata fields
     			DynamicMetadataField[] dynamicMetadata = page.getMetadata().getDynamicFields();
     			if (dynamicMetadata == null)
     			{
     				this.setAllowCreation(false, "PageFieldsToSystemNamePlugin: This page contains no Dynamic Metadata Fiels, therefore '" + stIdentifier + "' is an invalid field identifier.");
     				throw new FatalPluginException("PageFieldsToSystemNamePlugin: This page contains no Dynamic Metadata Fiels, therefore '" + stIdentifier + "' is an invalid field identifier.");
     			}
     			stNodeVal = this.searchDynamicMetadata(dynamicMetadata, stIdentifier);
     			if (stNodeVal == null || stNodeVal.trim().equals(""))
     			{
     				this.setAllowCreation(false, "PageFieldsToSystemNamePlugin: The dynamic metadata field '" + stIdentifier + "' either does not exist or contains no value in the asset being created.");
     				throw new FatalPluginException("PageFieldsToSystemNamePlugin: The dynamic metadata field '" + stIdentifier + "' either does not exist or contains no value in the asset being created.");
     			}
     			
     		}
     		else if (stIdentifier.contains(STRUCTURED_DATA_TOKEN))
     		{
     			// structured data (data definition) fields
     			StructuredDataNode[] structuredData = page.getStructuredData();
     			if (structuredData == null)
     			{
     				this.setAllowCreation(false, "PageFieldsToSystemNamePlugin: This Page contains no Structured Data, therefore '" + stIdentifier + "' is an invalid field identifier.");
     				throw new FatalPluginException("PageFieldsToSystemNamePlugin: This Page contains no Structured Data, therefore '" + stIdentifier + "' is an invalid field identifier.");
     			}
     			stNodeVal = this.searchStructuredData(structuredData, stIdentifier);
     			if (stNodeVal == null || stNodeVal.trim().equals(""))
     			{
     				this.setAllowCreation(false, "PageFieldsToSystemNamePlugin: The structured data field '" + stIdentifier + "' either does not exist or contains no value in the asset being created.");
     				throw new FatalPluginException("PageFieldsToSystemNamePlugin: The structured data field '" + stIdentifier + "' either does not exist or contains no value in the asset being created.");
     			}
     			
     		}
     		else
     		{
     			// wired metadata fields
     			stNodeVal = this.searchWiredMetadata(page.getMetadata(), stIdentifier);
     			if (stNodeVal == null || stNodeVal.trim().equals(""))
     			{
     				this.setAllowCreation(false, "PageFieldsToSystemNamePlugin: The wired metadata field '" + stIdentifier + "' either does not exist or contains no value in the asset being created.");
     				throw new FatalPluginException("PageFieldsToSystemNamePlugin: The wired metadata field '" + stIdentifier + "' either does not exist or contains no value in the asset being created.");
     			}
     			
     		}
     		
     		// normalize for URL-safe system name
    		stNodeVal = this.utilityProvider.getFilenameNormalizer().normalize(stNodeVal, new ArrayList<Character>());
     		
     		// replace spaces with space token
 			stNodeVal = stNodeVal.trim().replace(" ", stSpaceToken).toLowerCase();
     		
     		newName.append(stNodeVal);
     		
     		// append concatenation token
     		newName.append(stConcatToken);
     	}
     	
     	// strip trailing concatenation token
     	if (newName.length() > stConcatToken.length())
     	{
     		newName.delete(newName.length() - stConcatToken.length(), newName.length());
     	}
     	
     	String stNewName = newName.toString();
     	
     	if (stNewName == null || stNewName.trim().equals(""))
     	{
     		this.setAllowCreation(false, "PageFieldsToSystemNamePlugin:  None of the following fields are populated: " + stIdentifiers);
     		throw new FatalPluginException("PageFieldsToSystemNamePlugin:  None of the following fields are populated: " + stIdentifiers);
     	}
     	
     	// if all is well, update the asset's system name & allow creation of the asset
     	page.setName(stNewName);
     	this.setAllowCreation(true, "");
     }
 
     /**
      * @see com.cms.assetfactory.BaseAssetFactoryPlugin#doPluginActionPre(com.hannonhill.cascade.api.asset.admin.AssetFactory, com.hannonhill.cascade.api.asset.home.FolderContainedAsset)
      */
     @Override
     public void doPluginActionPre(AssetFactory factory, FolderContainedAsset asset) throws PluginException
     {
         //code in this method will be executed before the user is presented with the
         //initial edit screen. This could be used for pre-population, etc.
     	
     	// suppress the system name field in the page creation UI, since it will be auto-generated
     	asset.setHideSystemName(true);
     	if (asset.getName() == null || asset.getName().trim().equals(""))
     	{
     	  asset.setName("hidden");
     	}
     }
     
     private boolean isValidType(FolderContainedAsset asset)
     {
     	if (asset instanceof Page)
     	{
     		return true;
     	}
     	return false;
     }
 
     /**
      * @see com.cms.assetfactory.AssetFactoryPlugin#getAvailableParameterDescriptions()
      */
     public Map<String, String> getAvailableParameterDescriptions()
     {
         //build a map where the keys are the names of the parameters
         //and the values are the descriptions of the parameters
         Map<String, String> paramDescriptionMap = new HashMap<String, String>();
         paramDescriptionMap.put(FIELDIDS_PARAM_NAME_KEY, FIELDIDS_PARAM_DESC_KEY);
         paramDescriptionMap.put(SPACETOKEN_PARAM_NAME_KEY, SPACETOKEN_PARAM_DESC_KEY);
         paramDescriptionMap.put(CONCATTOKEN_PARAM_NAME_KEY, CONCATTOKEN_PARAM_DESC_KEY);
         return paramDescriptionMap;
     }
 
     /**
      * @see com.cms.assetfactory.AssetFactoryPlugin#getAvailableParameterNames()
      */
     public String[] getAvailableParameterNames()
     {
         //return a string array with all the name keys of
         //the parameters for the plugin
         return new String[] { FIELDIDS_PARAM_NAME_KEY,SPACETOKEN_PARAM_NAME_KEY,CONCATTOKEN_PARAM_NAME_KEY };
     }
 
     /**
      * @see com.cms.assetfactory.AssetFactoryPlugin#getDescription()
      */
     public String getDescription()
     {
         //return the resource bundle key of this plugin's
         //description
         return DESC_KEY;
     }
 
     /**
      * @see com.cms.assetfactory.AssetFactoryPlugin#getName()
      */
     public String getName()
     {
         //return the resource bundle key of this plugin's
         //name
         return NAME_KEY;
     }
 }

 package com.hannonhill.cascade.plugin;
 
 import com.hannonhill.cascade.api.asset.common.Metadata;
 import com.hannonhill.cascade.api.asset.common.DynamicMetadataField;
 import com.hannonhill.cascade.api.asset.common.StructuredDataNode;
 import com.cms.assetfactory.BaseAssetFactoryPlugin;
 
 import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Formatter;
 
 /**
  * Abstract Plug-in class useful in searching out Page Field values based on provided String field
  * identifiers.  Extends the StructuredDataPlugin functionality to allow for retrieval of Wired and
  * Dynamic Metadata in addition to Structured Data (Data Definition) fields.<br/><br/>
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
 public abstract class StructuredDataPagePlugin extends BaseAssetFactoryPlugin
 {
 	protected final static String CUSTOM_METADATA_TOKEN = "dynamic-metadata";
 	protected final static String STRUCTURED_DATA_TOKEN = "system-data-structure";
 	protected final static String METADATA_TITLE = "title";
 	protected final static String METADATA_DISPLAY_NAME = "display-name";
 	protected final static String METADATA_DESCRIPTION = "description";
 	protected final static String METADATA_AUTHOR = "author";
 	protected final static String METADATA_KEYWORDS = "keywords";
 	protected final static String METADATA_SUMMARY = "summary";
 	protected final static String METADATA_TEASER = "teaser";
 	protected final static String METADATA_START_DATE = "start-date";
 	protected final static String METADATA_END_DATE = "end-date";
 	protected final static String METADATA_REVIEW_DATE = "review-date";
 	protected final static String METADATA_EXPIRATION_FOLDER = "expiration-folder";
 	/**
 	 * Searches the provided Metadata for the given wired metadata field name (stIdentifier) and
 	 * returns its value (if any).
 	 * @param metadata Metadata object to be searched
 	 * @param stIdentifier String indicating the specific metadata field name to search for
 	 * @return String containing the value of the specified wired metadata field
 	 */
 	protected String searchWiredMetadata(Metadata metadata, String stIdentifier)
 	{
 		if (stIdentifier.contains(METADATA_TITLE))
 		{
 			return metadata.getTitle().trim();
 		}
 		else if (stIdentifier.contains(METADATA_DISPLAY_NAME))
 		{
 			return metadata.getDisplayName().trim();
 		}
 		else if (stIdentifier.contains(METADATA_DESCRIPTION))
 		{
 			return metadata.getDescription().trim();
 		}
 		else if (stIdentifier.contains(METADATA_AUTHOR))
 		{
 			return metadata.getAuthor().trim();
 		}
 		else if (stIdentifier.contains(METADATA_KEYWORDS))
 		{
 			return metadata.getKeywords().trim();
 		}
 		else if (stIdentifier.contains(METADATA_SUMMARY))
 		{
 			return metadata.getSummary().trim();
 		}
 		else if (stIdentifier.contains(METADATA_TEASER))
 		{
 			return metadata.getTeaser().trim();
 		}
 		else if (stIdentifier.contains(METADATA_START_DATE))
 		{
 			// use date format of: yyyy-mm-dd
 			Date startDate = metadata.getStartDate();
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(startDate);
 			Formatter format = new Formatter();
 			String stDate = format.format("%tF", cal).toString();
 			return stDate;
 		}
 		else if (stIdentifier.contains(METADATA_END_DATE))
 		{
 			// use date format of yyyy-mm-dd
 			Date endDate = metadata.getEndDate();
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(endDate);
 			Formatter format = new Formatter();
 			String stDate = format.format("%tF", cal).toString();
 			return stDate;
 		}
 		else if (stIdentifier.contains(METADATA_REVIEW_DATE))
 		{
 			// use date format of yyyy-mm-dd
 			Date reviewDate = metadata.getReviewDate();
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(reviewDate);
 			Formatter format = new Formatter();
 			String stDate = format.format("%tF", cal).toString();
 			return stDate;
 		}
 		else if (stIdentifier.contains(METADATA_EXPIRATION_FOLDER))
 		{
 			// not a valid field for auto-name generation
 			return null;
 		}
 		return null;
 	}
 	
 	/**
 	 * Searches the provided array of dynamic metadata fields for the provided custom-field name (stIdentifier)
 	 * and returns the value of said field.
 	 * 
 	 * @param dynamicFields DynamicMetadataFields[] array containin all custom fields to be searched
 	 * @param stIdentifier String indicating the specific custom field name to search for
 	 * @return String containing the value of the specified custom field
 	 */
 	protected String searchDynamicMetadata(DynamicMetadataField[] dynamicFields, String stIdentifier)
 	{
 		int startIndex = stIdentifier.indexOf(CUSTOM_METADATA_TOKEN) + CUSTOM_METADATA_TOKEN.length();
 		String stNodeName = stIdentifier.substring(startIndex);
 		
 		if (stNodeName.startsWith("/"))
 		{
 			stNodeName = stNodeName.substring(1);
 		}
 		
 		for (int i = 0; i < dynamicFields.length; i++)
 		{
 			if (dynamicFields[i].getName().equals(stNodeName))
 			{
 				
 				StringBuffer multiVal = new StringBuffer();
             	for (int j = 0; j < dynamicFields[i].getValues().length; j++)
             	{
             		if (dynamicFields[i].getValues()[j] != null && dynamicFields[i].getValues()[j].trim() != "")
             		{
             			multiVal.append(dynamicFields[i].getValues()[j].trim());
             			if (j < (dynamicFields[i].getValues().length - 1))
             			{
             				multiVal.append(" ");
             			}
             		}
             		
             	}
             	
             	if (multiVal.length() > 0)
             	{
             		return multiVal.toString();
             	}
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Checks for the presence of <code>"system-data-strucure"</code> token in <code>sdIdentifier</code> & strips this out prior
 	 * to passing along to <code>super.searchStructuredData(structuredData,sdIdentifier)</code>
 	 * @see StructuredDataPlugin
 	 */
     protected String searchStructuredData(StructuredDataNode[] structuredData, String sdIdentifier)
     {
 		if (sdIdentifier.contains(STRUCTURED_DATA_TOKEN))
 		{
 			int startIndex = sdIdentifier.indexOf(STRUCTURED_DATA_TOKEN) + STRUCTURED_DATA_TOKEN.length();
 			sdIdentifier = sdIdentifier.substring(startIndex);
 		}
     	
         if (sdIdentifier.startsWith("/"))
             sdIdentifier = sdIdentifier.substring(1);
 
         if (sdIdentifier.contains("/"))
         {
             String[] nodePath = sdIdentifier.split("/");
             String curNode = nodePath[0];
             String subNodes = "";
             for (int i = 1; i < nodePath.length; i++)
             {
                 subNodes += nodePath[i] + (i != nodePath.length - 1 ? "/" : "");
             }
 
             for (StructuredDataNode node : structuredData)
             {
                 if (node.isGroup() && curNode.equals(node.getIdentifier()))
                 {
                     String value = searchStructuredData(node.getGroup(), subNodes);
                     if (value != null)
                     {
                         return value;
                     }
                 }
             }
         }
         else
         {
             for (StructuredDataNode node : structuredData)
             {
                 if (node.isGroup())
                 {
                     String value = searchStructuredData(node.getGroup(), sdIdentifier);
                     if (value != null)
                     {
                         return value;
                     }
                 }
                 else if (sdIdentifier.equals(node.getIdentifier()) && node.isText())
                 {
                     String[] nodeValues = node.getTextValues();
                     String nodeValue = null;
                     if (nodeValues.length > 0 && nodeValues[0] != null && nodeValues[0].trim() != "")
                     {
                         nodeValue = nodeValues[0];
                         
                         // for date/time & calendar, return formatted date string, i.e. yyyy-mm-dd
                         if (node.getTextNodeOptions().isDatetime())
                         {
                         	Date date = new Date(Long.valueOf(nodeValue).longValue());
                 			Calendar cal = Calendar.getInstance();
                 			cal.setTime(date);
                 			Formatter format = new Formatter();
                 			String stDate = format.format("%tF", cal).toString();
                 			return stDate;                  	
                         }
                         else if (node.getTextNodeOptions().isCalendar())
                         {
                         	try
                         	{
                         		String[] dateParts = nodeValue.split("-");
                         		int month = Integer.parseInt(dateParts[0]) - 1; // because month is zero-based
                         		int day = Integer.parseInt(dateParts[1]);
                         		int year = Integer.parseInt(dateParts[2]);
                         		
                     			Calendar cal = Calendar.getInstance();
                     			cal.set(year, month, day);
                     			Formatter format = new Formatter();
                     			String stDate = format.format("%tF", cal).toString();
                     			return stDate;  
                         	}
                         	catch (Exception e)
                         	{
                         		return null;
                         	}
                         	
                         }
                         // for check-box & multi-select (where multiple values are allowed), concatenate all selected values
                         else if (node.getTextNodeOptions().isCheckbox() || node.getTextNodeOptions().isMultiselect())
                         {
                         	StringBuffer multiVal = new StringBuffer();
                         	for (int i = 0; i < nodeValues.length; i++)
                         	{
                         		if (nodeValues[i] != null && nodeValues[i].trim() != "")
                         		{
                         			multiVal.append(nodeValues[i].trim());
                         			if (i < (nodeValues.length - 1))
                         			{
                         				multiVal.append(" ");
                         			}
                         		}
                         		
                         	}
                         	
                         	if (multiVal.length() > 0)
                         	{
                         		return multiVal.toString();
                         	}
                         }
                         else if (!(node.getTextNodeOptions().isWysiwyg()))
                         {
                         	return nodeValue;
                         }
                     }
                     
 
 
                 }
 
             }
         }
         return null;
     }
 
 }

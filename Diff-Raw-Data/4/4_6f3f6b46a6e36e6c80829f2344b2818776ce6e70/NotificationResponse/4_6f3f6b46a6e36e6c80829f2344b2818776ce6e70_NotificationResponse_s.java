 package org.jasig.portlet.notice.response;
 
 import java.io.Serializable;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import net.sf.json.JSON;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 /**
  * This class contains all the categories and errors
  * retrieved by an INotificationService. It is also
  * used to aggregate all the NotificationResponses from
  * various services into a single NotificationResponse.
  * The data from the overall NotificationResponse instance
  * is returned to the portlet to be rendered.
  */
 public class NotificationResponse implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private List<NotificationCategory> categories = new ArrayList<NotificationCategory>();
 	private List<NotificationError> errors = new ArrayList<NotificationError>();
 
 	public NotificationResponse(){}
 
 	public NotificationResponse(
 			List<NotificationCategory> categories,
 			List<NotificationError> errors){
 		this.categories = categories;
 		this.errors = errors;
 	}
 	
 	/**
 	 * Set the source of the data. This method will iterate through the
 	 * data and set the source value for the entries and error (if any).
 	 * @param source is the source of the data.
 	 */
 	public void setSource(String source)
 	{
 		for(NotificationCategory category : categories)
 			category.setSource(source);
 		for(NotificationError error : errors)
 			error.setSource(source);
 	}
 
 	/**
 	 * Write the instance data to a JSON data String.
 	 *
 	 * @return String, null if the data is invalid.
 	 */
 	public String toJson()
 	{
 		return toJson(this);
 	}
 
 	/**
 	 * Write the instance data to a JSON data String.
 	 *
 	 * @return String, null if the data is invalid.
 	 */
 	public static String toJson(NotificationResponse request)
 	{
 		try
 		{
 			JSON json = JSONSerializer.toJSON(request.toMap());
 			return json.toString(1);
 		}
 		catch(JSONException je)
 		{
 			je.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Write the instance data to a JSON data file.
 	 *
 	 * @param data the JSON data string.
 	 * @return NotificationRequest, null if the JSON data is invalid.
 	 */
 	public static NotificationResponse fromJson(String data)
 	{
 		NotificationResponse request = null;
 		try
 		{
 			//create a map that is used to convert the JSON data back into a class object
 			Map<String, Object> convertMap = new HashMap<String, Object>();
 			convertMap.put("errors", NotificationError.class);
 			convertMap.put("categories", NotificationCategory.class);
 			convertMap.put("entries", NotificationEntry.class);
 
 			JSONObject json = JSONObject.fromObject(data);
 			request = (NotificationResponse)JSONObject.toBean(json, NotificationResponse.class, convertMap);
 		}
 		catch(JSONException je)
 		{
 			je.printStackTrace();
 		}
 
 		return request;
 	}
 	
 	public Map<String, Object> toMap()
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 
 		for(int i = 0; i < categories.size(); i++)
 			map.put("categories", categories);
 
 		for(int j = 0; j < errors.size(); j++)
 			map.put("errors", errors);
 
 		return map;
 	}
 
 	/**
 	 * Extract the category and error data from the given response and
 	 * add it to this instance's data.
 	 * @param response the source of data
 	 */
 	public void addResponseData(NotificationResponse response)
 	{
     	addCategories(response.getCategories());
     	addErrors(response.getErrors());
 
 	}
 	public List<NotificationCategory> getCategories() {
 		return categories;
 	}
 
 	public void setCategories(List<NotificationCategory> categories) {
 		this.categories = categories;
 	}
 
 	/** Insert the given categories and their entries into the any existing
 	 * categories of the same title. If a category doesn't match an existing
 	 * one, add it to the list.
 	 * @param newCategories collection of new categories and their entries.
 	 */
 	public void addCategories(List<NotificationCategory> newCategories) {
 		
 		//check if an existing category (by the same title) already exists
 		//if so, add the new categories entries to the existing category
 		for(NotificationCategory newCategory : newCategories) {
 			boolean found = false;
 
 			for(NotificationCategory myCategory : categories) {
 				if(myCategory.getTitle().toLowerCase().equals(newCategory.getTitle().toLowerCase())){
 					found = true;
 					myCategory.addEntries(newCategory.getEntries());
 				}
 			}
 			
 			if(!found)
 				categories.add(newCategory);
 		}
 	}
 
 	public void clearCategories() {
 		categories.clear();
 	}
 
 	public List<NotificationError> getErrors() {
 		return errors;
 	}
 
 	public void setErrors(List<NotificationError> errors) {
 		this.errors = errors;
 	}
 
 	public void addErrors(List<NotificationError> newErrors) {
 		for(NotificationError error : newErrors)
 			errors.add(error);
 	}
 
 	public void filterErrors(Set<Integer> filterErrors) {
	    Set iSetClone = new HashSet(filterErrors);
        for(NotificationError error : errors)
         {
             if(filterErrors.contains(error.getKey()))
             {
                 errors.remove(error);
             }   
         }
 	}
 
 	public void clearErrors() {
 		errors.clear();
 	}
 
 	public String toString() {
 		StringBuffer buffer = new StringBuffer(
 				"org.jasig.portlet.notice.serverresponse.NotificationRequest\n");
 
 		for(NotificationCategory category : categories)
 			buffer.append(category.toString());
 
 		for(NotificationError error : errors)
 			buffer.append(error.toString());
 
 		return buffer.toString();
 	}
 }

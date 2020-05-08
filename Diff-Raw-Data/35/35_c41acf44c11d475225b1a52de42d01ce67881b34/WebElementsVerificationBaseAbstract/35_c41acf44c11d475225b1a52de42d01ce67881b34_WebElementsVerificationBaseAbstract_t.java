 package npg.webadmin.acceptance.test.util;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import npg.webadmin.acceptance.test.Constants;
  
 
 public class WebElementsVerificationBaseAbstract {
 	  
 	private List<SearchItem> searchItemList = null;  
 	private List<SearchItem> getSearchItemList() { return searchItemList; }
     
 	private List<FieldItem> fieldItemList = null;  
 	private List<FieldItem> getFieldItemList() { return fieldItemList; }
   
 	private List<License> licenseList = null;  
 	private List<License> getLicenseList() { return licenseList; }
 	
 	private Map<String, String> attributes = null;
 	
 	public class SearchItem {
 	   	public String field;
 	   	public String operation;
 	   	public String fieldvalue;
	   	public String result;
 	   	public String remark;
 	}    
 	
 	public class License {
 	   	public String licenseId;
 	   	public String productCode;
 	   	public String startDate;
 	   	public String endDate;
 	   	public String remark;
 	}   
 	
 	public class FieldItem {
 	   	public String fieldname;
 	   	public String value;
 	   	public String type;
 	   	public String present;
 	   	public String remark;
 	}
 	
 	public String getAttributes(String key) {
 		if (attributes == null) {
 			attributes = new HashMap<String, String>();
 		    attributes.put(Constants.WITH_ATTRIBUTE_ID, Constants.ATTRIBUTE_ID);
 		    attributes.put(Constants.WITH_ATTRIBUTE_CLASS, Constants.ATTRIBUTE_CLASS);
 		    attributes.put(Constants.WITH_ATTRIBUTE_TITLE, Constants.ATTRIBUTE_TITLE);
 		    attributes.put(Constants.WITH_ATTRIBUTE_TYPE, Constants.ATTRIBUTE_TYPE);
 		    attributes.put(Constants.WITH_ATTRIBUTE_NAME, Constants.ATTRIBUTE_NAME);
 		    attributes.put(Constants.WITH_ATTRIBUTE_VALUE, Constants.ATTRIBUTE_VALUE);
 		    attributes.put(Constants.WITH_ATTRIBUTE_VALUE_CHECKED, Constants.ATTRIBUTE_VALUE);
 		    attributes.put(Constants.WITH_ATTRIBUTE_LINK, Constants.ATTRIBUTE_HREF);
 		    attributes.put(Constants.WITH_ATTRIBUTE_ALT, Constants.ATTRIBUTE_ALT);
 		    attributes.put(Constants.WITH_ATTRIBUTE_NAME_AND_VALUE, Constants.ATTRIBUTE_NAME);
 		    attributes.put(Constants.WITH_ATTRIBUTE_SOURCE, Constants.ATTRIBUTE_SRC);
 		    attributes.put(Constants.PRECEDING_SIBLING_TEXT, Constants.XPATH_PRECEDING_SIBLING);
 		    attributes.put(Constants.FOLLOWING_SIBLING_TEXT, Constants.XPATH_FOLLOWING_SIBLING);
 		    
 		}		
 		return (String)attributes.get(key);
 	}
 }

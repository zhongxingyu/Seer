 package npg.webadmin.acceptance.test.service;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.support.ui.Select;
 import npg.webadmin.acceptance.test.WebDriverWrapper;
 
 public class SearchService {
 
 	public SearchService() {
 		super();
 	}
 
 	 
     /*
      *  This search the fields in Personal Account search based 
      *   
      *  operationField      :      example 'user_id_op', 'email_op'  
      *  operationCondition  :      example '=', 'Begins With', 'Ends With', 'Contains'
      *  inputField          :      example 'user_id','email', 'firstname'
      *  inputFieldValue     :      example resource name like 'account.search.user.id'  
      *  elementValueToSearch:      element value in the page to be searched
      *  stringSearch        :      xpath formatted search string ex. //input[@value=' Search ' and @type=\"SUBMIT\" and @id=\"search1\"]
      */
      public void inputAndSubmitField(
     		  WebDriverWrapper webDriver,
     		  String operationField, 
     		  String operationCondition,
     		  String inputField,
     		  String inputFieldValue,
     		  final String elementValueToSearch,
     		  final String stringSearch) {
     	
     	 this.fillAndSubmitInputField(
     			webDriver,
    				operationField,
    				operationCondition,
    	    	    inputField,
    	    	    inputFieldValue,
    	    	    stringSearch
    	     );       		 
   
     }
     
     public void inputAndSubmitFieldForSiteAdmin(
     	  WebDriverWrapper webDriver,	 
    		  String operationField, 
    		  String operationCondition,
    		  String inputField,
    		  String inputFieldValue,
    		  final String elementValueToSearch) {
    	
    	      this.fillAndSubmitInputField(
    	    		webDriver,
   				operationField,
   				operationCondition,
   	    	    inputField,
   	    	    inputFieldValue,
   	    	    "//input[@value=' Search ' and @type=\"SUBMIT\" and @id=\"search2\"]"  
   	     );    
     }
      
     public String mapOperationFieldSiteLicense(String field) {
     	    
      	if (field.toLowerCase().trim().equals("site id"))             {	return "site_id_op";
     		} else if (field.toLowerCase().trim().equals("user id"))      {	return "user_id_op";        	  
     		} else if (field.toLowerCase().trim().equals("organization")) {	return "company_op"; 
     		} else if (field.toLowerCase().trim().equals("consortia"))    {	return "consortium_op"; 
     		} else if (field.toLowerCase().trim().equals("ip address"))   {	return "ip_address_op"; 
     		} else if (field.toLowerCase().trim().equals("email"))        {	return "email_op"; 
     		} else if (field.toLowerCase().trim().equals("last name"))    {	return "lname_op"; 
     		} else if (field.toLowerCase().trim().equals("first name"))   {	return "fname_op"; 
     		} else if (field.toLowerCase().trim().equals("address1"))   {	return "address1"; 
     		} else {return "";}    	
      }
      
      public String mapOperationConditionSiteLicense(String condition) {
      	if (condition.toLowerCase().trim().equals("begins with"))      { return "swth";   			
     		} else if (condition.toLowerCase().trim().equals("ends with")) { return "ewth";   				
    		} else if (condition.toLowerCase().trim().equals("contains"))  { return "cont";
    		} else if (condition.toLowerCase().trim().equals("="))         { return "iseq";	
    		} else if (condition.toLowerCase().trim().equals(">="))        { return "gteq";
    		} else if (condition.toLowerCase().trim().equals("<="))        { return "lteq";
    		//} else if (condition.toLowerCase().trim().equals(":INPUT:"))        { return ":INPUT:";
     		} else { return  "iseq";}
      }
      
      public String mapInputFieldSiteLicense(String field) {
      	if (field.toLowerCase().trim().equals("site id"))             {	return "site_id";
     		} else if (field.toLowerCase().trim().equals("user id"))      { return "user_id";  
     	    } else if (field.toLowerCase().trim().equals("organization")) {	return "company"; 
     		} else if (field.toLowerCase().trim().equals("consortia"))    {	return "consortium";
     		} else if (field.toLowerCase().trim().equals("ip address"))   {	return "ip_address";
     		} else if (field.toLowerCase().trim().equals("email"))        {	return "email"; 
     		} else if (field.toLowerCase().trim().equals("last name"))    {	return "lname"; 
     		} else if (field.toLowerCase().trim().equals("first name"))   {	return "fname";     		
     		} else { return field.toLowerCase().trim(); }
      }
      
     
     public String mapOperationFieldPersonalAccount(String field) { 
     	if (field.toLowerCase().trim().equals("user id"))           { return "user_id_op";
    		} else if (field.toLowerCase().trim().equals("email"))      { return "email_op";        	  
    		} else if (field.toLowerCase().trim().equals("first name")) { return "fname_op"; 
    		} else if (field.toLowerCase().trim().equals("last name"))  { return "lname_op"; 
   		} else if (field.toLowerCase().trim().equals("eclipse id")) { return "account_user_eclipse_id_op";   		
    		} else { return ""; } 
     	
     }
     
     public String mapOperationConditionPersonalAccount(String condition) {
   		if (condition.toLowerCase().trim().equals("begins with"))      { return "swth";  			
    		} else if (condition.toLowerCase().trim().equals("ends with")) { return "ewth";    				
   		} else if (condition.toLowerCase().trim().equals("contains"))  { return "cont";
   		} else if (condition.toLowerCase().trim().equals("="))         { return "iseq";	
   		} else if (condition.toLowerCase().trim().equals(">="))        { return "gteq";
   		} else if (condition.toLowerCase().trim().equals("<="))        { return "lteq";
   		} else { return "iseq"; }
     }
     
     public String mapInputFieldPersonalAccount(String field) {
     	if (field.toLowerCase().trim().equals("user id"))           { return "user_id";
    		} else if (field.toLowerCase().trim().equals("email"))      { return "email";  
    	    } else if (field.toLowerCase().trim().equals("first name")) { return "fname"; 
    		} else if (field.toLowerCase().trim().equals("last name"))  { return "lname";
    		} else if (field.toLowerCase().trim().equals("country"))    { return "country"; 
    		} else if (field.toLowerCase().trim().equals("eclipse id")) { return "account_user_eclipse_id";
    		} else if (field.toLowerCase().trim().equals("orcid id"))   { return "orcid_id"; 
    		} else { return ""; }
     }
     
     
 	public void fillAndSubmitInputField(WebDriverWrapper driver,
     		String operationField, 
     		String operationCondition,
     		String inputField,
     		String inputFieldValue, 
     		String searchString) { 
	
	  System.out.println(" ===== fillAndSubmitInputField()  Input Field: " + inputField
			  + "    Input Field Value: " +  inputFieldValue
			  + "    Operation field: " +  operationField
			  + "    Operation condition: " +  operationCondition);
 		
     	// field has no operation e.g. 'Begins With', 'Ends With' 
       //if (operationField !=null && !operationField.isEmpty()) {
       if (operationCondition !=null && !operationCondition.isEmpty() && !operationCondition.equals("iseq")) {	  
            // change the selector (option) in the select tag  
     	   (new Select(driver.findElement(By.name(operationField)))).selectByValue(operationCondition);
        }
        
        //System.out.println(" ===== Input Field: " + inputField + "    Input Field Value: " +  inputFieldValue);
        
        // key input on User ID field
        driver.findElement(By.name(inputField)).sendKeys(inputFieldValue);        
    			
        // submit for <form name='acc' ...
        driver.findElement(By.xpath(searchString)).click();       
    }
 	
 }

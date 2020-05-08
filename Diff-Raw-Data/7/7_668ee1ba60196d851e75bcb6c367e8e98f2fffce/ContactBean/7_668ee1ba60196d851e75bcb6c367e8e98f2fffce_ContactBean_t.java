 package webapp.help.beans;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.UserServiceFactory;
 
 
 public class ContactBean{
 	public static String kind = "Contact";
 	public static String PROPERTY_PARENT = "parent";
 	public static String PROPERTY_NAME = "name";
 	public static String PROPERTY_PHONE = "phone";
 	public static String PROPERTY_EMAIL = "email";
 	public static String PROPERTY_MSG = "msg";
 	public static String PROPERTY_CAT = "category";
 	
 	private Key parent;
 	private Key key;
 	
 	private String name;
 	private String phone;
 	private String email;
 	private String message;
 	private String category;
 	
 	private Entity entity;
 	
 	public ContactBean( String keyStr, String name, String phone, String email, String category, String message){
 		this.parent = KeyFactory.createKey("User",  UserServiceFactory.getUserService().getCurrentUser().getEmail());
 		
 		this.name = name;
 		this.phone = phone;
 		this.email = email;
 		this.message = message;
 		this.category = category;
 		
 		this.entity = getEntity();
 		if(keyStr==null)
 			this.key = entity.getKey();
 		else
 			this.key = KeyFactory.stringToKey(keyStr);
 		
 	}
 	
 	public ContactBean(Entity entity){
 		parent = entity.getParent();
 		key = entity.getKey();
 		name = (String) entity.getProperty(PROPERTY_NAME);
 		phone = (String) entity.getProperty(PROPERTY_PHONE);
 		email = (String) entity.getProperty(PROPERTY_EMAIL);
 		message = (String) entity.getProperty(PROPERTY_MSG);
 		category = (String) entity.getProperty(PROPERTY_CAT);
 		this.entity = entity;
 	}
 	
 	public Entity getEntity(){
 		if(entity == null){
 			entity = new Entity(kind,parent);
 			
 			entity.setProperty(PROPERTY_PARENT,parent);
 			
 			entity.setProperty(PROPERTY_NAME,name);
 			entity.setProperty(PROPERTY_PHONE,phone);
 			entity.setProperty(PROPERTY_EMAIL,email);
 			entity.setProperty(PROPERTY_MSG,message);  
 			entity.setProperty(PROPERTY_CAT,category);
 		}
 
 		return entity;
 
 	}
 	
 	public Key getParent(){return parent;}
 	public Key getKey() {return entity.getKey(); };
 	
 	public String getName(){return name;}
 	public String getPhone(){return phone;}
 	public String getEmail(){return email;}
 	public String getMessage(){return message;}
 	public String getCategory(){return category;}
 	public String getKeyStr() {return KeyFactory.keyToString(this.key);}
 	
 	
 	
 	public List<String> getValidationErrors() {
 		List<String> errors = new ArrayList<String>();
 
 		if (name == null || name.length() == 0) {
 			errors.add("Name is required");
 		}
 
 		if(!verifyEmail(email)){
 			errors.add("Email is in wrong format");
 		}
 		
 		if(!verifyPhone(phone)){
 			errors.add("phone number is in wrong format");
 		}
 		
 		return errors;
 	}
 	
 	public static ContactBean createBean(HttpServletRequest req){
 		String name = req.getParameter(PROPERTY_NAME);
 		String phone = req.getParameter(PROPERTY_PHONE);
 		String email = req.getParameter(PROPERTY_EMAIL);
 		String message = req.getParameter(PROPERTY_MSG);
 		String category = req.getParameter(PROPERTY_CAT); 
 		String keyStr = req.getParameter("keyStr");
 		
 		return new ContactBean(keyStr,name, phone, email, category, message);
 	}
	 private boolean verifyEmail(String rawEmail){
	    	String email=rawEmail.trim();
 	    	int identifierPos=-1;
 	    	int periodPos=-1;
 
          if(email==null||email.length()==0)
          	return false;
          
 	    	//check if the email has @
 	    	for(int i=0;i<email.length();i++){
 	    		if(email.charAt(i)=='@')
 	    			identifierPos=i;
	    
 	    		if(email.charAt(i)==' ')
 	    			return false;
 	    		/*
 	    		if(!(email.charAt(i)>32 && email.charAt(i)<128) )
 	    			printable=false;
 	    		*/
 	    	}
 	    	for(int i=0;i<email.length();i++){
 	    		if(email.charAt(i)=='.')
 	    			periodPos=i;
 	    		/*
 	    		if(!(email.charAt(i)>32 && email.charAt(i)<128) )
 	    			printable=false;
 	    		*/
 	    	}
 	    	
 	    	if(identifierPos==-1||identifierPos==0||identifierPos==(email.length()-1))
 		    	   return false;
 	    	
 	    	if(periodPos==-1||periodPos==0||periodPos==(email.length()-1))
 		    	   return false;
 	    	
 	    	if(periodPos-identifierPos==1||periodPos-identifierPos==-1)
 	    		   return false;
 	    	/*	
 	    	if(!printable)
 	    		return false;
 	    	*/
 	    	return true;
 	    }
 	 private boolean verifyPhone(String phone){
 	    	
          if(phone==null||phone.length()==0)
          	return true;
          
 	    	//check if the phone has @
 	    	for(int i=0;i<phone.length();i++){
 	    		if ((phone.charAt(i) >= '0' && phone.charAt(i) <= '9') ){ 
 	    		
 	    		  return true;
 	    		}
 	    		/*
 	    		if(!(phone.charAt(i)>32 && phone.charAt(i)<128) )
 	    			printable=false;
 	    		*/
 	    	}
 	    	
 	    	return false;
 	    }
 }

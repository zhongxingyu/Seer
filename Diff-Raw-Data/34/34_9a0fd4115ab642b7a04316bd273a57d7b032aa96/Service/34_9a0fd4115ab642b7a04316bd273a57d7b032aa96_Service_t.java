 package com.alagad.ant4cf;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 import com.alagad.ant4cf.util.ConfigReader;
 import com.alagad.ant4cf.util.Util; 
 
 public class Service extends Task {
 
 	private String component;
 	private String method;
 	private String property; 
 	private ArrayList<Argument> arguments = new ArrayList<Argument>();
 	private ConfigReader config;
 	
 	// The method executing the task
     public void execute() throws BuildException {
     	config = new ConfigReader(getProject());
     	
     	String result;
     	try{
 			result = runService();
			
			
 		} catch(Exception e){
 			throw new BuildException(e.getMessage());
 		}
     	
 		// set the property
		if(this.property != null){
			getProject().setProperty(this.property, result);
		}  
		
     }	
     
     private String runService() throws IOException, Exception{
     	HttpClient client = new HttpClient();
     	
     	String url = getComponentUrl();
     	PostMethod post = new PostMethod(url);

         NameValuePair[] data = getArguments();
         
         if(config.getDebug()){
        	System.out.println("Posting to Service URL: " + url);
        	System.out.println("Service Arguments...");
        	
         	for(int x = 0 ; x < data.length ; x++){
         		System.out.println(data[x].getName() + ": " + data[x].getValue());
         	}
         }
         
         post.setRequestBody(data);
         
         int status = client.executeMethod(post);
         
         String result;
         if(status == 200){
        	if(config.getDebug()){
            	System.out.println("Status: 200");
        	}
        	//post.getResponseContentLength();
        	
             InputStream in = post.getResponseBodyAsStream();
                        
 	        // dump the response.
             result = Util.read(new InputStreamReader(in));
            
         } else {
        	if(config.getDebug()){
            	System.out.println("Status: " + status);
        	}
        	
         	throw new Exception("HTTP Error: " + status + ", Detail: " + Util.read(new InputStreamReader(post.getResponseBodyAsStream())));
         }
 
     	// disconnect
         post.releaseConnection();
       
         return result;
     }
     
     private NameValuePair[] getArguments(){
     	ArrayList<NameValuePair> args = new ArrayList<NameValuePair>();
 
     	args.add(new NameValuePair("adminPassword", this.config.getAdminPassword()));
     	if(this.config.getAdminUserId() != null){
     		args.add(new NameValuePair("adminUserId", this.config.getAdminUserId()));
     	}
     	args.add(new NameValuePair("method", this.method));
     	
     	for(int x = 0 ; x < this.arguments.size() ; x++){
     		Argument argument = (Argument) this.arguments.get(x);
     		args.add(new NameValuePair(argument.getName(), argument.getValue()));
     	}
     	
     	NameValuePair[] result = new NameValuePair[args.size()];
     	args.toArray(result);
     	
     	return result;
     }
     
     private String getComponentUrl(){
     	String url = this.config.getAnt4cfUrl() + "/services/" + this.component.replace(".", "/") + ".cfc";
     	
     	return url;
     }
         
     public Argument createArgument() {
     	Argument argument = new Argument();
     	this.arguments.add(argument);
         return argument;
     }
 
     public void setComponent(String component){
     	this.component = component;
     }
     
     public void setMethod(String method){
     	this.method = method;
     }
     
     public void setProperty(String property){
     	this.property = property;
     }
     
     public class Argument extends Task {
 
     	private String name;
     	private String value;
     		
     	public void setName(String name){
     		this.name = name;
     	}	
     	
     	public String getName(){
     		return this.name;
     	}
     	
     	public void setValue(String value){
     		this.value = value;
     	}
     	
     	public String getValue(){
     		return this.value;
     	}
     	
     }
 }

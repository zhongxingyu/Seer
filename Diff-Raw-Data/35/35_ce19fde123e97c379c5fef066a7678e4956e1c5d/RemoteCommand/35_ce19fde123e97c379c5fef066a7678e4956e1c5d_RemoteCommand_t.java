 package net.fobel.android.remoteduino;
 
 import java.io.Serializable;
 import java.util.regex.*;
 import java.util.*;
 import java.io.IOException;
 import net.fobel.android.HttpHelper;
 
 
 public class RemoteCommand implements Serializable {
 	private static final long serialVersionUID = -4352237220714175048L;
 	public String label;
 	public String protocol;
 	public String code;
 	
 	public RemoteCommand(String label, String protocol, String code) {
 		super();
 		this.label = label;
 		this.protocol = protocol;
 		this.code = code;
 	}
 	
 	
 	public RemoteCommand(String label, String response) throws IOException {
 		super();
 		this.label = label;
 		String patternstr__code = "<h2>\\s*(.*?)\\s*</h2>";
 		Pattern pattern__code = Pattern.compile(patternstr__code, Pattern.DOTALL | Pattern.MULTILINE);
 		String patternstr__protocol = "Received\\s+(NEC|SONY|RC6|RC5)\\[(\\d+)\\]:";
 		Pattern pattern__protocol = Pattern.compile(patternstr__protocol, Pattern.DOTALL | Pattern.MULTILINE);
 		
 		Matcher matcher = pattern__code.matcher(response);
 		boolean matchFound = matcher.find();
 
 		if(matchFound) {
	        this.code = "0x" + matcher.group(1);
 		} else {
 			throw new IOException("No code found!");
 		}
 		
 		matcher = pattern__protocol.matcher(response);
 		matchFound = matcher.find();
 
 		if(matchFound) {
 	        this.protocol = matcher.group(2);
 		} else {
 			throw new IOException("No protocol found!");
 		}
 	}
 	
 	
     public static RemoteCommand learn_command(String label) throws Exception {
         String url = "http://192.168.1.182/learn";
         
         String result = HttpHelper.process_get_request(url);
         RemoteCommand cmd;
 		try {
 			cmd = new RemoteCommand(label, result);
 		} catch(Exception e) {
 			//Toast.makeText(this, "Error parsing response:\n" + result, Toast.LENGTH_SHORT).show();
 			throw e;
 		}
 		return cmd;
 	}
     
     public String send() {
         Map<String, String> query_params = new HashMap<String, String>();
         query_params.put("c", code);
         query_params.put("p", protocol);
         
         String url = "http://192.168.1.182/send";
         String result = HttpHelper.process_get_request(url, query_params);
         return result;
     }
 }

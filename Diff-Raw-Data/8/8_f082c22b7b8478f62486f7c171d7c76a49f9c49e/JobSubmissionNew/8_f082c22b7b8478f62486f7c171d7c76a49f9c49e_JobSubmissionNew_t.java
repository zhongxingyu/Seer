 package org.vpac.grisu.client.control.example;
 
 import org.vpac.grisu.client.control.login.LoginException;
 import org.vpac.grisu.client.control.login.LoginParams;
 import org.vpac.grisu.client.control.login.ServiceInterfaceFactory;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.js.model.JobSubmissionObjectImpl;
 
 public class JobSubmissionNew {
 	
 	public static void main(String[] args) throws Exception {
 		
 		String username = args[0];
 
 		char[] password = args[1].toCharArray();
 
 		LoginParams loginParams = new LoginParams(
 		// "http://localhost:8080/grisu-ws/services/grisu",
 				// "https://ngportaldev.vpac.org/grisu-ws/services/grisu",
 				"Local", username, password, "myproxy2.arcs.org.au", "7512");
 
 		ServiceInterface si = null;
 		try {
 			si = ServiceInterfaceFactory.createInterface(loginParams);
 			si.login(username, password);
 		} catch (Exception e) {
 			throw new LoginException(e.getLocalizedMessage());
 		}
 		
 		JobSubmissionObjectImpl jso = new JobSubmissionObjectImpl();
 		
 		jso.setApplication("java");
		jso.setCommandline("java -version");  
 		
//		String jobname = si.createJob(jso.getStringJobPropertyMap(), "/APAC/VPAC", "force-name");
 		
//		si.submitJob(jobname);
 		
 		
 	}
 
 }

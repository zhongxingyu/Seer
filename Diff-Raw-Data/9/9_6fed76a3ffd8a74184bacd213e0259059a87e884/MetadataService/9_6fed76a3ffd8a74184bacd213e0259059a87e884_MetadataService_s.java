 package edu.illinois.gitsvn.infra.filters;
 
 /**
  * This is a service for filters to get their required info. You
  * push an information here, and then the filters pull it a the
  * required time.
  * 
  * @author caius
  *
  */
 public class MetadataService {
 	
 	private static MetadataService service = null;
 	
 	private MetadataService() {
 	}
 	
 	public static MetadataService getService() {
 		if (service == null)
 			service = new MetadataService();
 		
 		return service;
 	}

 }

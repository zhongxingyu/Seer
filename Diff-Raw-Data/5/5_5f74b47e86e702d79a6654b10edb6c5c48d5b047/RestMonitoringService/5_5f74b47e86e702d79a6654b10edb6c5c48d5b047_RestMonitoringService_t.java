package org.bitrepository.monitoringservice.webservice;
 
 import org.bitrepository.monitoringservice.MonitoringService;
 import org.bitrepository.monitoringservice.MonitoringServiceFactory;
 
 public class RestMonitoringService {
 
 	private MonitoringService service;
 	
 	public RestMonitoringService() {
 		service = MonitoringServiceFactory.getMonitoringService();
 	}
 	
 	public String getMonitoringServiceConfiguration() {
 		return "";
 	}
 	
	public String getComponentStatuses() {
 		return "";
 	}
 }

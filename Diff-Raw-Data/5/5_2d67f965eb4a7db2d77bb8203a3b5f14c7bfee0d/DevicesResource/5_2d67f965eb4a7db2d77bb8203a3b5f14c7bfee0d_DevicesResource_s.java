 package com.ledomatic.server;
 
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import org.restlet.resource.Put;
 import org.restlet.resource.ServerResource;
 
 import com.google.inject.Inject;
 
 public class DevicesResource extends ServerResource {
 
 	private RestRequest restRequest;
 	private DeviceService deviceService;
 
 	@Inject
 	public DevicesResource(DeviceService deviceService) {
 		this.deviceService = deviceService;
 	}
 
     @Put
     public void login() {
     	restRequest = parseParams();
     	Device device = new Device();
     	device.setId(restRequest.getDeviceid());
     	device.setStatus(true);
     	deviceService.saveDevice(device);
     }
 
     @Delete
     public void logout() {
     	restRequest = parseParams();
     	Device device = deviceService.getDevice(restRequest.getDeviceid());
     	device.setStatus(false);
     	deviceService.saveDevice(device);
     }
 
     @Get
     public String status() {
     	restRequest = parseParams();
     	Device device = deviceService.getDevice(restRequest.getDeviceid());
     	if (device.isStatus()) {
    		return "On";
     	}
    	return "Off";
     }
 
     private RestRequest parseParams() {
     	RestRequest restRequest = new RestRequest();
     	restRequest.setDeviceid((String) getRequest().getAttributes().get("deviceid"));
     	if (getRequest().getAttributes().containsKey("inputtype")) {
     		restRequest.setInputtype((String) getRequest().getAttributes().get("inputtype"));
     	}
     	if (getRequest().getAttributes().containsKey("id")) {
     		restRequest.setId((String) getRequest().getAttributes().get("id"));
     	}
     	return restRequest;
     }
 
 	private class RestRequest {
 		private String deviceid;
 		private String inputtype;
 		private String id;
 
 		public String getDeviceid() {
 			return deviceid;
 		}
 
 		public void setDeviceid(String deviceid) {
 			this.deviceid = deviceid;
 		}
 
 		public String getInputtype() {
 			return inputtype;
 		}
 
 		public void setInputtype(String inputtype) {
 			this.inputtype = inputtype;
 		}
 
 		public String getId() {
 			return id;
 		}
 
 		public void setId(String id) {
 			this.id = id;
 		}
 	}
 
 }

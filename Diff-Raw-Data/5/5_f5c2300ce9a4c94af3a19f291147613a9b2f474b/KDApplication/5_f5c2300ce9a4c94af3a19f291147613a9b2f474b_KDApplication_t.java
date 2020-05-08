 package com.kdcloud.server.rest.application;
 
 import org.restlet.Application;
 import org.restlet.Restlet;
 import org.restlet.routing.Router;
 
 import com.kdcloud.server.rest.api.DatasetResource;
import com.kdcloud.server.rest.api.DeviceResource;
 import com.kdcloud.server.rest.api.UserDataResource;
 import com.kdcloud.server.rest.api.ReportResource;
 import com.kdcloud.server.rest.api.SchedulerResource;
 import com.kdcloud.server.rest.resource.DatasetServerResource;
import com.kdcloud.server.rest.resource.DeviceServerResource;
 import com.kdcloud.server.rest.resource.UserDataServerResource;
 import com.kdcloud.server.rest.resource.ReportServerResource;
 import com.kdcloud.server.rest.resource.SchedulerServerResource;
 
 public class KDApplication extends Application {
 
 
 	/**
 	 * Creates a root Restlet that will receive all incoming calls.
 	 */
 	@Override
 	public Restlet createInboundRoot() {
 		
 		Router router = new Router(getContext());
 
 		router.attach(UserDataResource.URI, UserDataServerResource.class);
 		router.attach(DatasetResource.URI, DatasetServerResource.class);
 		router.attach(SchedulerResource.URI, SchedulerServerResource.class);
 		router.attach(ReportResource.URI, ReportServerResource.class);
		router.attach(DeviceResource.URI, DeviceServerResource.class);
 		
 		return router;
 	}
 }

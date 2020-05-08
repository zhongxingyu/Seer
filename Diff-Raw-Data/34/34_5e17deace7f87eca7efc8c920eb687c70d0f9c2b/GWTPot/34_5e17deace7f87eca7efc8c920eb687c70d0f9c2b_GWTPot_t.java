 package com.fountainhead.client;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.fountainhead.client.gin.ClientGinjector;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.gwtplatform.mvp.client.DelayedBindRegistry;
 
 public class GWTPot implements EntryPoint {
 
 	private final ClientGinjector ginjector = GWT.create(ClientGinjector.class);
 
 	@Override
 	public void onModuleLoad() {
 		/*
 		 * Install an UncaughtExceptionHandler which will produce
 		 * <code>FATAL</code> log messages
 		 */
 		Log.setUncaughtExceptionHandler();
 
		try {
			// This is required for Gwt-Platform proxy's generator
			DelayedBindRegistry.bind(ginjector);
 
			ginjector.getPlaceManager().revealCurrentPlace();
		} catch (Exception e) {
			Log.debug(e.toString());
		}
 	}
 }

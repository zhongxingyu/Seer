 package com.v2t.client.module;
 
 import com.google.gwt.inject.client.AbstractGinModule;
 import com.google.inject.Singleton;
import com.v2t.client.ServerDispatchAsync;
 
 public class ClientModule extends AbstractGinModule{
 
 	@Override
 	protected void configure() {
		bind( ServerDispatchAsync.class ).in( Singleton.class );
 	}
 	
 }

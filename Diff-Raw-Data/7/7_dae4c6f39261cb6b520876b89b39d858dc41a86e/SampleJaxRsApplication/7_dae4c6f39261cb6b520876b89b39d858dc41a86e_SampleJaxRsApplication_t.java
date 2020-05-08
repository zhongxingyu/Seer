 package com.wordnik.swagger.sample.jersey;
 
 import com.sun.jersey.api.core.PackagesResourceConfig;
import com.wordnik.swagger.jaxrs.config.BeanConfig;
 
 public final class SampleJaxRsApplication extends PackagesResourceConfig {
 
 	public SampleJaxRsApplication() {
 		super("com.wordnik.swagger.sample.resource",
 				"com.wordnik.swagger.jaxrs.listing");
 	}
 
 	public static void main(String[] args) throws Exception {
 		final int port = 8002;
 
		BeanConfig config = new BeanConfig();
		config.setResourcePackage("com.wordnik.swagger.sample.resource");
		config.setVersion("1.0.0");
		config.setBasePath("http://localhost:8002/");

 		SampleJaxRsWebserver server = new SampleJaxRsWebserver(port);
 		server.start();
 		System.in.read();
 		server.stop();
 	}
 }

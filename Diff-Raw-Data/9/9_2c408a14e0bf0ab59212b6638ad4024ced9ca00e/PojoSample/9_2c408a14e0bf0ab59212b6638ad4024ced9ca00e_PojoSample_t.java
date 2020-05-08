 package com.github.marschall.sars.pojo;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 public class PojoSample implements PojoSampleMBean {
 
 	private static final String NAME = "java:global/env/foo";
 	private static final String VALUE = "FOO";
 
 	private static final Logger LOG = Logger.getLogger("sar-pojo");
 
 	public PojoSample() {
 		super();
 	}
 
 	public void start() {
 		try {
 			InitialContext ic = new InitialContext();
 			ic.rebind(NAME, VALUE);
 			LOG.info("started");
 		} catch (NamingException e) {
 			LOG.log(Level.SEVERE, "could not bind value", e);
 		}
 	}
 
 	public void stop() {
 		try {
 			InitialContext ic = new InitialContext();
 			ic.unbind(NAME);
			LOG.info("stopped");
 		} catch (NamingException e) {
 			LOG.log(Level.SEVERE, "could not bind value", e);
 		}
 	}
 	
 	@Override
 	public String getMessage() {
 		return "message";
 	}
 
 }

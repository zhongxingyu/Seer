 package com.github.marschall.sars.legacy;
 
 import java.util.logging.Logger;
 
 import javax.naming.InitialContext;
 
 import org.jboss.system.ServiceMBeanSupport;
 
 public class LegacySample extends ServiceMBeanSupport implements LegacySampleMBean {
 
 	private static final String NAME = "java:global/env/foo/legacy";
 	private static final String VALUE = "BAR";
 
	private static final Logger LOG = Logger.getLogger("sar-legacy");
 
 	public LegacySample() {
 		super();
 	}
 
 	@Override
 	protected void startService() throws Exception {
 		InitialContext ic = new InitialContext();
 		ic.rebind(NAME, VALUE);
 		LOG.info("started");
 	}
 
 	@Override
 	protected void stopService() throws Exception {
 		InitialContext ic = new InitialContext();
 		ic.unbind(NAME);
 		LOG.info("stopped");
 	}
 
 	@Override
 	public String getMessage() {
 		return "legacy";
 	}
 
 }

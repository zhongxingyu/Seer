 package com.buglabs.bug.module.gps;
 
 import java.io.InputStream;
 
 import com.buglabs.bug.module.gps.pub.INMEARawFeed;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.StreamMultiplexer;
 
 public class NMEARawFeed extends StreamMultiplexer implements INMEARawFeed {
 
	public NMEARawFeed(InputStream is) {
		super(is);
 		setName("NMEARawFeed");
 		setLogService(LogServiceUtil.getLogService(Activator.getInstance().getBundleContext()));
 	}
 
 }

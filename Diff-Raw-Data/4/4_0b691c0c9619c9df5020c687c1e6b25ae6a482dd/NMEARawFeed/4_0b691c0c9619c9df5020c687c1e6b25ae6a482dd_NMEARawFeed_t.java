 package com.buglabs.bug.module.gps;
 
 import java.io.InputStream;
 
 import com.buglabs.bug.module.gps.pub.INMEARawFeed;
 import com.buglabs.util.LogServiceUtil;
 import com.buglabs.util.StreamMultiplexer;
 
 public class NMEARawFeed extends StreamMultiplexer implements INMEARawFeed {
 
	public NMEARawFeed(InputStream is, long read_delay) {
		super(is, 1, 0, read_delay);
 		setName("NMEARawFeed");
 		setLogService(LogServiceUtil.getLogService(Activator.getInstance().getBundleContext()));
 	}
 
 }

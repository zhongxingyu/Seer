 package com.justcloud.osgifier.service.impl;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Collection;
 
import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.FrameworkUtil;
 
 import com.justcloud.osgifier.annotation.REST;
 import com.justcloud.osgifier.annotation.REST.RESTMethod;
 import com.justcloud.osgifier.annotation.RESTParam;
 import com.justcloud.osgifier.dto.BundleAdapter;
 import com.justcloud.osgifier.service.BundleService;
 import com.justcloud.osgifier.util.BundleUtil;
 
 public class BundleServiceImpl implements BundleService {
 
 	private BundleContext context;
 
 	@Override
 	@REST(url = "/osgi/bundle/list", method = RESTMethod.GET)
 	public Collection<BundleAdapter> getBundles() {
 		return BundleUtil.adaptBundles(getContext().getBundles());
 	}
 
 	@Override
 	@REST(url = "/osgi/bundle/start", method = RESTMethod.POST)
 	public void startBundle(@RESTParam("id") Long id) throws BundleException {
 		getContext().getBundle(id).start();
 	}
 
 	@Override
 	@REST(url = "/osgi/bundle/stop", method = RESTMethod.POST)
 	public void stopBundle(@RESTParam("id") Long id) throws BundleException {
 		getContext().getBundle(id).stop();
 	}
 
 	@Override
 	@REST(url = "/osgi/bundle/uninstall", method = RESTMethod.POST)
 	public void uninstallBundle(@RESTParam("id") Long id) throws BundleException {
 		getContext().getBundle(id).uninstall();
 	}
 
 	@Override
 	@REST(url = "/osgi/bundle/install", method = RESTMethod.POST)
 	public void installBundle(@RESTParam("location") String location) throws BundleException {
 		getContext().installBundle(location);
 	}
 
 	@Override
 	public void installBundle(String location, InputStream is)
 			throws BundleException {
 		getContext().installBundle(location, is);
 	}
 
 	@Override
 	@REST(url = "/osgi/bundle/restart", method = RESTMethod.POST)
 	public void restartBundle(@RESTParam("id") Long id) throws BundleException {
 		stopBundle(id);
 		startBundle(id);
 	}
 
 	@Override
 	@REST(url = "/osgi/bundle/update", method = RESTMethod.POST)
 	public void updateBundle(@RESTParam("id") Long id) throws BundleException {
 		getContext().getBundle(id).update();
 	}
 
 	@Override
 	public void updateBundle(Long id, InputStream is) throws BundleException {
 		getContext().getBundle(id).update(is);
 	}
 
 	@Override
 	@REST(url = "/osgi/package/install", method = RESTMethod.POST)
 	public void installPackage(@RESTParam("package") String pack) throws BundleException {
 		InputStream is = null;
 		BufferedReader reader = null;
 		try {
 			is = getContext().getBundle().getResource("/packages/" + pack).openStream();
 			reader = new BufferedReader(new InputStreamReader(is));
 			String line;
 			while((line = reader.readLine()) != null) {
				boolean start = line.endsWith("!start");
				line = line.replaceAll("!start", "");
				Bundle bundle = getContext().installBundle(line);
				if(start) {
					bundle.start();
				}
 			}
 		} catch (IOException e) {
 			throw new BundleException("Package " + pack + " is invalid");
 		} finally {
 			if(is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	private BundleContext getContext() {
 		if (context == null) {
 			context = FrameworkUtil.getBundle(BundleServiceImpl.class)
 					.getBundleContext();
 		}
 		return context;
 	}
 
 }

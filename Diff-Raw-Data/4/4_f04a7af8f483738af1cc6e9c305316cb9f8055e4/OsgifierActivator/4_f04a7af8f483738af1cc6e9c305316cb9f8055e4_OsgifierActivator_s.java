 /*******************************************************************************
  * Copyright 2012 Just-Cloud
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.justcloud.osgifier;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.FrameworkUtil;
 
 import com.justcloud.osgifier.dto.User;
 import com.justcloud.osgifier.service.LogbackService;
 import com.justcloud.osgifier.service.SpringService;
 import com.justcloud.osgifier.service.UserService;
 import com.justcloud.osgifier.service.impl.LogbackServiceImpl;
 import com.justcloud.osgifier.service.impl.SpringServiceImpl;
 import com.justcloud.osgifier.service.impl.UserServiceImpl;
 
 public class OsgifierActivator implements BundleActivator {
 
	private List<String> restartOnStart = Arrays.asList("org.apache.aries.jpa.container"); 
	private List<String> stopAndStart   = Arrays.asList("org.apache.jackrabbit.jackrabbit-bundle"); 
 	
 	@Override
 	public void start(BundleContext context) throws Exception {
 		UserService userService = new UserServiceImpl();
 
 		startBundles(context);
 		restartBundles(context);
 		
 		if (userService.getUsers().size() == 0) {
 			createAdminUser(userService);
 		}
 
 		if (isSpringInstalled()) {
 			SpringService service = new SpringServiceImpl();
 			service.start();
 		}
 
 		if (isLogbackInstalled()) {
 			LogbackService logbackService = new LogbackServiceImpl();
 			try {
 				logbackService.reloadConfiguration();
 			} catch (Exception ex) {
 				Logger.getLogger(OsgifierActivator.class.getName()).severe(
 						ex.getMessage());
 			}
 		}
 	}
 
 	private void stopBundles(BundleContext context) throws BundleException {
 		
 		for(String bundleName : stopAndStart) {
 			for(Bundle bundle : context.getBundles()) {
 				if(bundleName.equals(bundle.getSymbolicName())) {
 					try {
 						bundle.stop();
 					} catch(Exception ex){
 						ex.printStackTrace();
 					}
 				}
 			}
 		}
 		
 	}
 
 	private void startBundles(BundleContext context) throws BundleException {
 
 		for(String bundleName : stopAndStart) {
 			for(Bundle bundle : context.getBundles()) {
 				if(bundleName.equals(bundle.getSymbolicName())) {
 					try {
 						bundle.start();
 					} catch(Exception ex){
 						ex.printStackTrace();
 					}
 				}
 			}
 		}
 		
 	}
 
 	private void restartBundles(BundleContext context) throws BundleException {
 
 		for(String bundleName : restartOnStart) {
 			for(Bundle bundle : context.getBundles()) {
 				if(bundleName.equals(bundle.getSymbolicName())) {
 					try {
 						bundle.stop();
 						bundle.start();
 					} catch(Exception ex){
 						ex.printStackTrace();
 					}
 				}
 			}
 		}
 		
 	}
 
 	private void createAdminUser(UserService userService) {
 		User user = new User();
 		user.setUsername("admin");
 		user.setEmail("admin@just-cloud.com");
 		user.setPassword("admin");
 		userService.createUser(user);
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		stopBundles(context);
 		if (isSpringInstalled()) {
 			SpringService service = new SpringServiceImpl();
 			service.stop();
 		}
 	}
 
 	private boolean isSpringInstalled() {
 		try {
 			FrameworkUtil
 					.getBundle(LogbackServiceImpl.class)
 					.loadClass(
 							"org.springframework.context.support.GenericApplicationContext");
 			return true;
 		} catch (ClassNotFoundException ex) {
 			return false;
 		}
 	}
 
 	private boolean isLogbackInstalled() {
 
 		try {
 			FrameworkUtil.getBundle(LogbackServiceImpl.class).loadClass(
 					"ch.qos.logback.classic.joran.JoranConfigurator");
 			return true;
 		} catch (ClassNotFoundException ex) {
 			return false;
 		}
 	}
 
 }

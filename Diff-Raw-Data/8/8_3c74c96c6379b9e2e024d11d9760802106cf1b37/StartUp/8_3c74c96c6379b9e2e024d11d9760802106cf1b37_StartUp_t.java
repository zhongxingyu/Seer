 /**
  * Copyright (c) 2013 SAP
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the SAP nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.sopeco.webui.server;
 
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Enumeration;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.sopeco.config.Configuration;
 import org.sopeco.config.IConfiguration;
 import org.sopeco.config.exception.ConfigurationException;
 import org.sopeco.engine.measurementenvironment.socket.SocketAcception;
 import org.sopeco.persistence.exceptions.DataNotFoundException;
 import org.sopeco.webui.server.persistence.UiPersistence;
 
 /**
  * 
  * @author Marius Oehler
  * 
  */
 public final class StartUp implements ServletContextListener {
 
 	private final String configurationFile = "sopeco-gui.conf";
 
 	public StartUp() {
 	}
 
 	private void loadConfiguration() throws ConfigurationException {
 		Configuration.getSessionSingleton(Configuration.getGlobalSessionId()).loadConfiguration(
 				StartUp.class.getClassLoader(), configurationFile);
 
 		IConfiguration cc = Configuration.getSessionSingleton(Configuration.getGlobalSessionId());
 	}
 
 	@Override
 	public void contextDestroyed(ServletContextEvent arg0) {
 		System.out.println(">> Destroying webapp..");
 		
 		Enumeration<Driver> drivers = DriverManager.getDrivers();
 	    while (drivers.hasMoreElements()) {
 	        Driver driver = drivers.nextElement();
 	        ClassLoader driverclassLoader = driver.getClass().getClassLoader();
 	        ClassLoader thisClassLoader = this.getClass().getClassLoader();
 	        if (driverclassLoader != null && thisClassLoader != null &&  driverclassLoader.equals(thisClassLoader)) {
 	            try {
 	            	System.out.println("Deregistering: " + driver);
 	                DriverManager.deregisterDriver(driver);
 	            } catch (SQLException e) {
 	                e.printStackTrace();
 	            }
 	        }
 	    }
 		
 	}
 
 	@Override
 	public void contextInitialized(ServletContextEvent arg0) {
 		System.out.println(">> Starting webapp..");
 		try {
 			loadConfiguration();
 
 			Scheduler.startScheduler();
 
 			int port = Configuration.getSessionSingleton(Configuration.getGlobalSessionId()).getPropertyAsInteger(
 					UiConfiguration.SOPECO_CONFIG_MEC_LISTENER_PORT, 11300);
 			if (port > 0) {
 				SocketAcception.open(port);
 			}
 		} catch (ConfigurationException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	
 }

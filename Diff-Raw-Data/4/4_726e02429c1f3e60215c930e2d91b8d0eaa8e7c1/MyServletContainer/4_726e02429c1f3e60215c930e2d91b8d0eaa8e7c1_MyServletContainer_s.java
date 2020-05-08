 /*******************************************************************************
  * Copyright (c) 2013, SAP AG
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions 
  * are met:
  *  
  *     - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *     - Redistributions in binary form must reproduce the above copyright 
  *      notice, this list of conditions and the following disclaimer in the 
  *      documentation and/or other materials provided with the distribution.
  *     - Neither the name of the SAP AG nor the names of its contributors may
  *      be used to endorse or promote products derived from this software 
  *      without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
  * THE POSSIBILITY OF SUCH DAMAGE.
  ******************************************************************************/
 package com.sap.research.primelife.rest;
 
 import java.io.File;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.sap.research.primelife.dc.initializer.Initializer;
 import com.sap.research.primelife.dc.timebasedtrigger.TimeBasedTriggerHandler;
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 
 public class MyServletContainer extends ServletContainer{
 	
 	private static final String INIT_PARAM_LOJ4G_FILE_LOC = "log4j-properties-location";
 	private static final String INIT_PARAM_SYSTEM_FILE_LOC = "system-properties-location";
 	private static final String SYS_PROPERTY_ENTITY_NAME = "EntityName";
 	private static final String SYS_PROPERTY_CONF_PATH = "systemConfigFilePath";
 	private static String servletRealPath;
 	
 	
 	@Override
 	public void init() throws ServletException {
 		super.init();
 		
 		ServletContext sc = getServletContext();
 		servletRealPath = sc.getRealPath(File.separator);
 		
 		initLog4j();
 		configurePPL();
 		
 		Initializer.getInstance();
 		TimeBasedTriggerHandler.getInstance().start();
 	}
 	
 	private void configurePPL() throws ServletException{
 		String systemConfLocation = getInitParameter(INIT_PARAM_SYSTEM_FILE_LOC);
 		if(systemConfLocation == null){
 			throw new ServletException("Fail to load " + INIT_PARAM_SYSTEM_FILE_LOC + " param from web.xml");
 		}
 		
 		ServletContext sc = getServletContext();
 		systemConfLocation = sc.getRealPath(File.separator) + getInitParameter(INIT_PARAM_SYSTEM_FILE_LOC);
 		
 		File fSystemConf = new File(systemConfLocation);
 		if(!fSystemConf.exists()){
 			throw new ServletException("Can't find " + systemConfLocation);
 		}
 		
 		if(!fSystemConf.canRead()){
 			throw new ServletException("Can't read " + systemConfLocation);
 		}
 		
 		System.setProperty(SYS_PROPERTY_ENTITY_NAME, "DC");
 		System.setProperty(SYS_PROPERTY_CONF_PATH, systemConfLocation);
 	}
 	
 	private void initLog4j(){
 		System.out.println("Log4JInitServlet is initializing log4j");
 		String log4jLocation = getInitParameter(INIT_PARAM_LOJ4G_FILE_LOC);
 
 		ServletContext sc = getServletContext();
 		
 		if (log4jLocation == null) {
 			System.err.println("*** No log4j-properties-location init param, so initializing log4j with BasicConfigurator");
 			BasicConfigurator.configure();
 		} else {
 			String webAppPath = sc.getRealPath(File.separator);
 			String log4jProp = webAppPath + log4jLocation;
			File yoMamaYesThisSaysYoMama = new File(log4jProp);
			if (yoMamaYesThisSaysYoMama.exists()) {
 				System.out.println("Initializing log4j with: " + log4jProp);
 				PropertyConfigurator.configure(log4jProp);
 			} else {
 				System.err.println("*** " + log4jProp + " file not found, so initializing log4j with BasicConfigurator");
 				BasicConfigurator.configure();
 			}
 		}
 	}
 	
 	public static String getServletRealPath(){
 		return servletRealPath;
 	}
 }

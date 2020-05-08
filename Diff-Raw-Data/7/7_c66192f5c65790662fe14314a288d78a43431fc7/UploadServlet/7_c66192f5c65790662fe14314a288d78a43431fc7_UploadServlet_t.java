 /*******************************************************************************
  * Copyright (c) 2008, 2011 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 package org.eclipse.virgo.management.console;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.management.ManagementFactory;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.openmbean.CompositeDataSupport;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  *
  */
 public class UploadServlet extends HttpServlet {
 
 	private static final String[] DEPLOYMENT_IDENTITY_FIELDS = new String[]{"type", "symbolicName", "version"};
 
 	private static final int HTTP_RESPONSE_INTERNAL_SERVER_ERROR = 500;
 
     private static final String ORG_ECLIPSE_VIRGO_KERNEL_HOME = "org.eclipse.virgo.kernel.home";
     
     private static final String DEPLOYER_MBEAN_NAME = "org.eclipse.virgo.kernel:category=Control,type=Deployer";
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static final String STAGING_DIR = "/work/org.eclipse.virgo.apps.admin.web.UploadServlet";
 	
 	private static final Logger log = LoggerFactory.getLogger(UploadServlet.class);
 
 	private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
 	
 	private String serverHome = null;
 	
 	private BundleContext bundleContext = null;
 
 	public UploadServlet() {
 	}
 	
 	public UploadServlet(BundleContext bundleContext) {
 		this.bundleContext = bundleContext;
 	}
 
 	/**
 	 * Do not use this method with the HTTPService unless the BundleContext has already been set.
 	 */
     public void init(ServletConfig config) throws ServletException {
     	super.init(config);
     	if(bundleContext == null){
     		this.bundleContext = (BundleContext) config.getServletContext().getAttribute("osgi-bundlecontext");
     	}
 		this.serverHome = this.bundleContext.getProperty(ORG_ECLIPSE_VIRGO_KERNEL_HOME);
     }
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		try {
 		    File stagingDir = createStagingDirectory();
 			FileItemFactory factory = new DiskFileItemFactory();
 			ServletFileUpload upload = new ServletFileUpload(factory);
 		    response.setContentType("text/html");
 			@SuppressWarnings("unchecked")
 			List<FileItem> items = (List<FileItem>) upload.parseRequest(request);
 			List<File> uploadedFiles = new ArrayList<File>();
 			for (FileItem fileItem : items) {
 				File uploadedFile = this.doUpload(fileItem, stagingDir);
 				if(uploadedFile != null){
 					uploadedFiles.add(uploadedFile);
 				}
 			}
 			doDeployment(uploadedFiles, response);
 		} catch (IllegalArgumentException ea){
 		    PrintWriter writer = response.getWriter();
	        writer.append("<ol id=\"uploadResults\"><li>File name contains '/' or '\\', this is not allowed.</ol>");
 			writer.close();
 		} catch (Exception e) {
 		    log.error(e.toString());
 			response.sendError(HTTP_RESPONSE_INTERNAL_SERVER_ERROR, e.toString());
 		}
 	}
 	
 	File doUpload(FileItem fileItem, File stagingDir) throws Exception{
 		if (!fileItem.isFormField()) {
 			String name = fileItem.getName();
 			if(name != null && name.length() > 0){
				if(name.contains("\\") || name.contains("/")){
					throw new IllegalArgumentException("Security violation, file name contains '/' or '\\'");
 				}
 				File uploadedFile = new File(stagingDir, name);
 				fileItem.write(uploadedFile);
 				log.info(String.format("Uploaded artifact of size (%db) to %s", fileItem.getSize(), uploadedFile.getPath()));
 				return uploadedFile;
 			}
 		}
 		return null;
 	}
 	
 	private void doDeployment(List<File> uploadedFiles, HttpServletResponse response) throws MalformedObjectNameException, NullPointerException, IOException{
 		ObjectName objectName = new ObjectName(DEPLOYER_MBEAN_NAME);
 	    PrintWriter writer = response.getWriter();
         writer.append("<ol id=\"uploadResults\">");
         for (File file : uploadedFiles) {
 			URI uri = file.toURI();
 			try {
 				Object invoke = this.mBeanServer.invoke(objectName, "deploy", new Object[]{uri.toString()}, new String[]{String.class.getName()});
 				writer.append("<li>" + file.getName() + " deployed as " + getDeploymentIdentity(invoke) + "</li>");
 			} catch (Exception e) {
 				writer.append("<li>" + file.getName() + " failed to deploy '" + e.getMessage() + "'</li>");
 			}
 			writer.append("<li />");
 		}
         writer.append("</ol>");
 		writer.close();
 	}
 	
 	private String getDeploymentIdentity(Object deploymentIdentity) {
 		StringBuilder builder = new StringBuilder();
 		if(deploymentIdentity instanceof CompositeDataSupport){
 			CompositeDataSupport deploymentIdentityInstance = (CompositeDataSupport) deploymentIdentity;
 			Object[] all = deploymentIdentityInstance.getAll(DEPLOYMENT_IDENTITY_FIELDS);
 			builder.append(all[0]);
 			builder.append(" - ").append(all[1]);
 			builder.append(": ").append(all[2]);
 		}
 		return builder.toString();
 	}
 
     private File createStagingDirectory() throws IOException {
         File pathReference = new File(String.format("%s%s", this.serverHome, STAGING_DIR));
         if (!pathReference.exists()) {
         	if (!pathReference.mkdirs()) {
         		throw new IOException("Unable to create directory " + pathReference.getAbsolutePath());
         	}
         }
         return pathReference.getAbsoluteFile();
     }
 
 }

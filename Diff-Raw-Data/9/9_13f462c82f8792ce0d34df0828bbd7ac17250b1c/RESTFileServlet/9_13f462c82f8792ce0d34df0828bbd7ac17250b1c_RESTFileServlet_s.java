 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  *     Jan Philipp Fiedler
  ******************************************************************************/
 package org.sociotech.communitymashup.interfaceprovider.restinterface.html;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.emf.common.util.EList;
 import org.sociotech.communitymashup.data.Attachment;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.security.facade.SecurityServiceFacade;
 
 /**
  * A Servlet providing the binary files referenced by the CommunityMashup.
  * 
  * @author Jan Philipp Fiedler (Jan.Fiedler@unbw.de)
  */
 public class RESTFileServlet extends HttpServlet {
 	
 	private static final long serialVersionUID = -6286487399809459879L;
 	DataSet dataSet;
 	String serverName;
 	int serverPort;
 
 	/**
 	 * Flag to show if a security service is needed based on the configuration. If true and no
 	 * security service is available then no data will be served.
 	 */
 	private boolean securityServiceNeeded = false;
 	
 	/**
 	 * Local reference to the security service 
 	 */
 	private SecurityServiceFacade securityService;
 	
 	/**
 	 * The default Constructor initializing the fileMap.
 	 * 
 	 * @param dataSet The DataSet to be used.
 	 * @param securityNeeded 
 	 */
 	public RESTFileServlet(DataSet dataSet, boolean securityNeeded) {
 		this.dataSet = dataSet;
 		this.securityServiceNeeded = securityNeeded;
 	}
 	
 	/**
 	 * Returns the file path for a given attachment.
 	 * 
 	 * @param attachment Attachment to get the file path for
 	 * @return The file path for the attachment or null in error case
 	 */
 	private String getFilePathForAttachment(Attachment attachment) {
 		
 		if(attachment == null)
 		{
 			return null;
 		}
 		
 		String url = attachment.getCachedFileUrl();
 		
 		if (url == null) {
 			return null;
 		}
 	
 		// convert the url to a proper filename
 		if (url.startsWith("file:/"))
 		{
 			url = url.substring(6);
 		}
 		return url.replaceAll("//", "/");
 	}
 	
 	/* (non-Javadoc)
 	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		
 		// security check
 		
 		if ((securityServiceNeeded && securityService == null) || 
 		    (securityServiceNeeded && !securityService.isRequestAlowed(req)))
 		{
 				// send error
 			resp.setContentType("text/plain");
 			resp.sendError(403, "Forbidden: Authentication failed.");
 			return;
 		}
 		
 		//gather information about the server
 		serverName = req.getServerName();
 		serverPort = req.getServerPort();
 		String fileName = req.getPathInfo();
 		// remove leading slash
 		fileName = fileName.substring(1);
 		
 		if(securityService != null)
 		{
 			fileName = securityService.cleanUpRequestURL(fileName);
 		}
 		
 		// find matching attachments (synchronized to avoid parallel access)
 		EList<Attachment> attachments = findAttachment(fileName);
 		
 		if (attachments == null || attachments.isEmpty()) {
 			// security: trying to access a file which is no attachment
 			resp.sendError(403, "Access denied.");
 			return;
 		}
 		
 		resp.setContentType("");
 		try {
 			
 			// use first attachment (all attachments lead to the same file)
 			String filePath = getFilePathForAttachment(attachments.get(0));
 			
 			//TODO: Mac+Linux workaround
			if(filePath.startsWith("var/") || filePath.startsWith("tmp/"))
 			{
 				filePath = "/"+filePath;
 			}
 			
 			//System.out.println("Accessing file " + filePath);
 			
 			int read = 0;
 			byte[] bytes = new byte[1024];
 			FileInputStream fis = null;
 			OutputStream os = null;
 			fis = new FileInputStream(new File(filePath));
 			os = resp.getOutputStream();
 			while((read = fis.read(bytes)) != -1){
 				os.write(bytes,0,read);
 			}
 			os.flush();
 			os.close();
 			// close input stream
 			fis.close();
 		} catch (IOException e) {
 				resp.sendError(404, "File not found.");
 		}
 	}
 
 	/**
 	 * Synchronized access to the data set attachments
 	 * @param fileName filename of the needed attachments
 	 * @return All attachments with this filename
 	 */
 	private synchronized EList<Attachment> findAttachment(String fileName) {
 		return dataSet.getAttachmentsWithCachedFileName(fileName);
 	}
 	
 	/**
 	 * Sets the security service for this rest servlet.
 	 * 
 	 * @param securityService Security service.
 	 */
 	public void setSecurityService(SecurityServiceFacade securityService)
 	{
 		this.securityService = securityService;
 	}	
 }

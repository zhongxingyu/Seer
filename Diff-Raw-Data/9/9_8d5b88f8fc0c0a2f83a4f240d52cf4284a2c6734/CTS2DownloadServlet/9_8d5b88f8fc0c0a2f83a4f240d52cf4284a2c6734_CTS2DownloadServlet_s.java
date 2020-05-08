 package edu.mayo.cts2Viewer.server;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.mayo.bsi.cts.cts2connector.cts2search.CTS2Config;
 import edu.mayo.bsi.cts.cts2connector.cts2search.ConvenienceMethods;
 import edu.mayo.bsi.cts.cts2connector.cts2search.aux.CTS2Utils;
 import edu.mayo.bsi.cts.cts2connector.cts2search.aux.ServiceResultFormat;
 import edu.mayo.cts2Viewer.server.properties.PropertiesHelper;
 
 public class CTS2DownloadServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 3457906406134591880L;
 	private static Logger logger = Logger.getLogger(CTS2DownloadServlet.class.getName());
 
 	private ConvenienceMethods cm = null;
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) {
 		doPost(request, response);
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) {
 		handleRequest(request, response);
 	}
 
 	private void handleRequest(HttpServletRequest request, HttpServletResponse response) {
 		processDownloadAlgorithm(request, response);
 	}
 
 	private void processDownloadAlgorithm(HttpServletRequest request, HttpServletResponse response) {
 		String zipFileName = request.getParameter("ZipFileName");
 
 		if (CTS2Utils.isNull(zipFileName)) {
 			zipFileName = "CTS2Download";
 		}
 
 		String valueSetNames = request.getParameter("valueSetNames");
 		if (CTS2Utils.isNull(valueSetNames)) {
 			logger.severe("No Value Name received for download operation");
 			return;
 		}
 
 		String[] valueSets = valueSetNames.split(",");
 
 		String DT_XLS = "csv";
 		String DT_XML = "xml";
 		String DT_SVS = "svs";
 		String DT_JSON = "json";
 		String DT_ALL = "all";
 
 		String downloadType = request.getParameter("downloadType");
 		if (CTS2Utils.isNull(downloadType)) {
 			downloadType = DT_XLS;
 		}
 
 		String XLS_EXTN = ".xls";
 		String XML_EXTN = "_CTS2.xml";
 		String JSON_EXTN = "_CTS2.json";
 		String SVS_EXTN = "_SVS.xml";
 
 		String[] exts = null;
 		String[] extTypes = null;
 		String contentTypeForSingleFileDownload = "text/plain";
 		
 		if (DT_ALL.equalsIgnoreCase(downloadType)) {
 			exts = new String[4];
 			extTypes = new String[4];
 
 			exts[0] = XLS_EXTN;
 			extTypes[0] = DT_XLS;
 			exts[1] = XML_EXTN;
 			extTypes[1] = DT_XML;
 			exts[2] = JSON_EXTN;
 			extTypes[2] = DT_JSON;
 			exts[3] = SVS_EXTN;
 			extTypes[3] = DT_SVS;
 
 			zipFileName += "_ALL.zip";
 		} else if (DT_JSON.equalsIgnoreCase(downloadType)) {
 			exts = new String[1];
 			extTypes = new String[1];
 
 			exts[0] = JSON_EXTN;
 			extTypes[0] = DT_JSON;
 
 			zipFileName += "_JSON.zip";
 			contentTypeForSingleFileDownload = "application/json";
 		} else if (DT_XML.equalsIgnoreCase(downloadType)) {
 			exts = new String[1];
 			extTypes = new String[1];
 
 			exts[0] = XML_EXTN;
 			extTypes[0] = DT_XML;
 
 			zipFileName += "_XML.zip";
 			contentTypeForSingleFileDownload = "application/xml";
 		} else if (DT_SVS.equalsIgnoreCase(downloadType)) {
 			exts = new String[1];
 			extTypes = new String[1];
 
 			exts[0] = SVS_EXTN;
 			extTypes[0] = DT_SVS;
 
 			zipFileName += "_SVS.zip";
 			contentTypeForSingleFileDownload = "application/xml";
 		} else {
 			exts = new String[1];
 			extTypes = new String[1];
 
 			exts[0] = XLS_EXTN;
 			extTypes[0] = DT_XLS;
 			zipFileName += "_CSV.zip";
 			contentTypeForSingleFileDownload = "application/octet-stream";
 		}
 		//logger.info("Download Type=" + downloadType);
 		//logger.info("Download requested: " + zipFileName);
 		
 
 		if ((valueSets.length == 1)&&(!(DT_ALL.equalsIgnoreCase(downloadType))))
 		{
 			//logger.info("Single file download Content Type: " + contentTypeForSingleFileDownload);
 			
 			try
 			{
 				initCM(request.getParameter("serviceName"));
 				response.setContentType(contentTypeForSingleFileDownload);
 				String fileText = createValueSetContent(extTypes[0], valueSets[0]);
 				
 				String singleFileName = valueSets[0] + exts[0];
 				
 				response.setHeader("Content-Length", String.valueOf(fileText.getBytes().length));
 				
 				response.setHeader("Content-Disposition", "attachment; filename=\"" + singleFileName + "\"");
 				
 				ServletOutputStream out = response.getOutputStream();
 				out.write(fileText.getBytes());
 				out.close();
 				
 			} catch (IOException ioe1) {
 				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				logger.log(Level.SEVERE, "Unable to write the file to the response output stream.", ioe1);
 			}
 		}
 		else
 		{
 			// logger.info("Sending file " + algorithmFile.getAbsolutePath());
 			response.setContentType("application/zip");
 			// response.setContentLength((int) algorithmFile.length());
 			response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
 	
 			initCM(request.getParameter("serviceName"));
 	
 			try {
 				ServletOutputStream out = response.getOutputStream();
 				ZipOutputStream zipout = new ZipOutputStream(out);
 	
 				for (int m = 0; m < exts.length; m++) {
 					for (String vs : valueSets) {
 						zipout.putNextEntry(new ZipEntry(vs + exts[m]));
 						zipout.write(createValueSetContent(extTypes[m], vs).getBytes());
 						zipout.closeEntry();
 					}
 				}
 	
 				zipout.flush();
 				zipout.close();
 				out.close();
 			} catch (IOException ioe2) {
 				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				logger.log(Level.SEVERE, "Unable to write the file to the response output stream.", ioe2);
 			}
 		}
 
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 	private String createValueSetContent(String type, String valueSetName) 
 	{
 		String msg = null;
 		
 		String existingValue = cm.getCurrentContext().getUserParameterValue(CTS2Config.REQUIRES_CREDENTIALS);
 		
 		try 
 		{
 			ServiceResultFormat requestedFormat = getFormatFromString(type);
 			
 			if ((requestedFormat == ServiceResultFormat.CSV)||(requestedFormat == ServiceResultFormat.SVS))
 			{
 				// These transforms use xslt server for transforms.
 				// XSLT server does not need authorization details. so set the flag false.
 				// if you send user name/password when it does not need, it fails.
 				cm.getCurrentContext().setUserParameter(CTS2Config.REQUIRES_CREDENTIALS, "false");
 			}
 
 			String valueSetContent = cm.getValueSetMembers(valueSetName, requestedFormat);
 			cm.getCurrentContext().setUserParameter(CTS2Config.REQUIRES_CREDENTIALS, existingValue);
			
 			return ((CTS2Utils.isNull(valueSetContent))?"":valueSetContent);
 			
 		} 
 		catch (Exception e) 
 		{
 			msg = e.getMessage();
 			logger.log(Level.SEVERE, "createValueSetContent failed: " + msg);
 		}
 
 		cm.getCurrentContext().setUserParameter(CTS2Config.REQUIRES_CREDENTIALS, existingValue);
 		return "Failed to get content for value set '" + valueSetName + "'\n" + msg;
 	}
 
 	private ServiceResultFormat getFormatFromString(String formatType) {
 		if (CTS2Utils.isNull(formatType)) {
 			return ServiceResultFormat.XML;
 		}
 
 		if (formatType.toLowerCase().indexOf("json") != -1) {
 			return ServiceResultFormat.JSON;
 		}
 
 		if (formatType.toLowerCase().indexOf("csv") != -1) {
 			return ServiceResultFormat.CSV;
 		}
 
 		if (formatType.toLowerCase().indexOf("svs") != -1) {
 			return ServiceResultFormat.SVS;
 		}
 
 		return ServiceResultFormat.XML;
 	}
 
 	private void initCM(String serviceName) 
 	{
 		try 
 		{
 			if (this.cm == null) 
 			{
 				this.cm = ConvenienceMethods.instance(PropertiesHelper.getInstance().getPropertiesDirectory());
 			}
 
 			if (CTS2Utils.isNull(serviceName))
 			{
 				logger.log(Level.WARNING, "(CTS2 Download Service):Requested CTS2 Service Name is either initializing, null or undefined! REST Context unchanged!");
 				return;
 			}
 					
 			if ((CTS2Utils.isNull(cm.getCurrentProfileName()))||
 				(!cm.getCurrentProfileName().equals(serviceName))) 
 				cm.setCurrentProfileName(serviceName);
 		} 
 		catch (Exception ex) 
 		{
 			logger.log(Level.SEVERE, ex.getMessage(), ex);
 		}
 	}
 }

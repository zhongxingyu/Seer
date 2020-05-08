 /**********************************************************************************
  *
  * $Header:
  *
  ***********************************************************************************
  *
  * Copyright (c) 2004, 2005, 2006, 2007 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  **********************************************************************************/
 package org.sakaiproject.tool.melete;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.sakaiproject.util.ResourceLoader;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.sakaiproject.api.app.melete.MeleteExportService;
 import org.sakaiproject.api.app.melete.MeleteImportService;
 import org.sakaiproject.api.app.melete.ModuleService;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.sakaiproject.api.app.melete.util.XMLHelper;
 import org.sakaiproject.component.app.melete.MeleteScormExportServiceImpl;
 import org.sakaiproject.component.app.melete.ModuleDateBean;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.util.Validator;
 import org.sakaiproject.component.app.melete.Module;
 import javax.faces.model.SelectItem;
 
 /**
  * <p>
  * Functionality for import and export modules
  * </p>
  *
  * @author Foothill College Mallika - 4/17/06 - Adding meleteDocsDir references
  *         Rashmi - 10/24/06 - clean up comments and change logger.info to debug
  *         Mallika - 2/22/07 - took out homedirpath
  */
 public class ExportMeleteModules {
 
 	/** Dependency: The logging service. */
 	protected Log logger = LogFactory.getLog(ExportMeleteModules.class);
 
 	protected MeleteSiteAndUserInfo meleteSiteAndUserInfo;
 
 	/** Dependency: The melete import export service. */
 	protected MeleteExportService meleteExportService;
 	protected MeleteExportService meleteExportScormService;
 	protected MeleteImportService meleteImportService;
 
 	/** Dependency: The module service. */
 	ModuleService moduleService;
 	private int uploadmax;
 	private List<Module> modList;
 	private List<SelectItem> availableModules;
 	private List<String> selectedModules;
 	private boolean noFlag;
 	private String selectFormat;
 
 	final static int BUFFER = 2048;
 	final static String IMS_MANIFEST_FILENAME = "imsmanifest.xml";
 	final static String SCORM2004_BASE_FILENAME = "SCORM2004base.zip";
 	/**
 	 * constructor
 	 */
 	public ExportMeleteModules() {
 	}
 
 	public void resetValues()
 	{
 		modList = null;
 		availableModules = null;
 		selectedModules=null;
 		noFlag = false;
 		selectFormat = "IMS";
 		selectedModules = new ArrayList<String>(0);
 		selectedModules.add("all");
 	}
 
 
 	public List getAvailableModules()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader(
 				"org.sakaiproject.tool.melete.bundle.Messages");
 
 		if(availableModules == null)
 		{
 		String courseId = getMeleteSiteAndUserInfo().getCurrentSiteId();
 		modList = getModuleService().getModules(courseId);
 		availableModules = new ArrayList<SelectItem>(0);
 		if(modList == null || modList.size() == 0)
 		{
 			String nomsg = bundle.getString("no_course_modules_available");
 			noFlag = true;
 			availableModules.add(new SelectItem("no",nomsg));
 			return availableModules;
 		}
 
 		String allmsg = bundle.getString("importexportmodules_export_one_more_select");
 		availableModules.add(new SelectItem("all",allmsg));
 
 		for(Module mod:modList)
 			availableModules.add(new SelectItem(mod.getModuleId().toString(),mod.getTitle()));
 
 		}
 		return availableModules;
 	}
 
 	private List<Module> createSelectedList()
 	{
 		if(selectedModules == null || selectedModules.size() == 0)return null;
 		if(selectedModules.size() == 1 && selectedModules.get(0).equals("all"))
 			return modList;
 
 		List<Module> returnList = new ArrayList<Module>(0);
 		for(String sel:selectedModules)
 		{
 			if(sel.equals("all"))continue;
 
 			for(Module m :modList){
 				if(m.getModuleId().toString().equals(sel))
 				{
 					returnList.add(m);
 					break;
 				}
 			}
 		}
 		return returnList;
 	}
 
 	public String exportModules()
 	{
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader(
 		"org.sakaiproject.tool.melete.bundle.Messages");
 
 		try {
 			List<Module>selectList = createSelectedList();
 
 			if (selectList != null && selectList.size() > 0) {
 				if(selectFormat.startsWith("IMS")) exportIMSModules(selectList);
 				else exportScormModules(selectList);
 
 			}
 			else {
 				// add message for no modules
 				String moModMsg = bundle
 				.getString("no_course_modules_available");
 				FacesMessage msg = new FacesMessage(null, moModMsg);
 				msg.setSeverity(FacesMessage.SEVERITY_INFO);
 				context.addMessage(null, msg);
 			}
 		} catch (Exception e) {
 			if (logger.isErrorEnabled())
 				logger.error(this.getClass().getName() + " : " + e.toString());
 
 			e.printStackTrace();
 			String errMsg = bundle.getString("error_exporting");
 			if (e instanceof MeleteException) {
 				errMsg = errMsg + e.getMessage() + ". "
 				+ bundle.getString("error_contact_admin");
 			}
 			FacesMessage msg = new FacesMessage(null, errMsg);
 			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 			context.addMessage(null, msg);
 		}
 
 
 		return "importexportmodules";
 	}
 	/**
 	 * exports the modules according to imsglobal content packaging specs 1.1.4
 	 *
 	 * @return navigation page
 	 */
 	public void exportIMSModules(List<Module> selectList) throws Exception{
 		if (logger.isDebugEnabled())
 			logger.debug("Starting export IMS Modules....");
 
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		String packagingdirpath = context.getExternalContext()
 		.getInitParameter("packagingdir");
 		String instr_id = getMeleteSiteAndUserInfo().getCurrentUser().getId();
 		instr_id = instr_id.trim();
 		String courseId = getMeleteSiteAndUserInfo().getCurrentSiteId();
 		courseId = courseId.trim();
 		
 		File packagedir = null;
 		try {
 			File basePackDir = new File(packagingdirpath);
 			if (!basePackDir.exists())
 				basePackDir.mkdirs();
 
 			String exisXmlFile = basePackDir.getAbsolutePath()
 			+ File.separator + IMS_MANIFEST_FILENAME;
 			File manifestFile = new File(exisXmlFile);
 
 			meleteExportService.initValues();
 			Element manifest = createManifestMetadata(manifestFile,
 					meleteExportService);
 
 			String title = getMeleteSiteAndUserInfo().getCourseTitle();
 			title =title.trim();
 			packagedir = new File(basePackDir.getAbsolutePath()
 					+ File.separator + courseId + "_" + instr_id
 					+ File.separator + title.replace(' ', '_'));
 			// delete exisiting files
 			meleteExportService.deleteFiles(packagedir);
 
 			if (!packagedir.exists())
 				packagedir.mkdirs();
 
 			// copy the schema files
 			File schemaFilesDir = basePackDir;
 			String imscp_v1p1 = schemaFilesDir.getAbsolutePath()
 			+ File.separator + "imscp_v1p1.xsd";
 			File imscp_v1p1_File = new File(imscp_v1p1);
 			meleteExportService.createFile(imscp_v1p1_File
 					.getAbsolutePath(), packagedir.getAbsolutePath()
 					+ File.separator + "imscp_v1p1.xsd");
 
 			String imsmd_v1p2 = schemaFilesDir.getAbsolutePath()
 			+ File.separator + "imsmd_v1p2.xsd";
 			File imsmd_v1p2_File = new File(imsmd_v1p2);
 			meleteExportService.createFile(imsmd_v1p2_File
 					.getAbsolutePath(), packagedir.getAbsolutePath()
 					+ File.separator + "imsmd_v1p2.xsd");
 
 			String imsxml = schemaFilesDir.getAbsolutePath()
 			+ File.separator + "xml.xsd";
 			File xml_File = new File(imsxml);
 			meleteExportService.createFile(xml_File.getAbsolutePath(),
 					packagedir.getAbsolutePath() + File.separator
 					+ "xml.xsd");
 
 			List orgResElements = meleteExportService
 			.generateOrganizationResourceItems(selectList,
 					packagedir, title);
 
 			if (orgResElements != null && orgResElements.size() > 0) {
 				manifest.add((Element) orgResElements.get(0));
 				manifest.add((Element) orgResElements.get(1));
 			}
 
 			// create xml document and add element
 			Document document = XMLHelper.createXMLDocument(manifest);
 
 			String newXmlFile = packagedir.getAbsolutePath()
 			+ File.separator + IMS_MANIFEST_FILENAME;
 			// generate xml file
 			XMLHelper.generateXMLFile(newXmlFile, document);
 
 			// validate new manifest xml
 			XMLHelper.parseFile(new File(newXmlFile));
 
 			title = Validator.escapeResourceName(title);
 			
 			String outputfilename = null;
 			if(modList.equals(selectList))
 			{
 			outputfilename = packagedir.getParentFile()
 			.getAbsolutePath()
 			+ File.separator + title.replace(' ', '_') + "_allModules.zip";
 			}
 			else {
 				outputfilename = packagedir.getParentFile()
 				.getAbsolutePath()
 				+ File.separator + title.replace(' ', '_') + "_fewModules.zip";
 			}
 			File zipfile = new File(outputfilename);
 			// create zip
 			createZip(packagedir, zipfile);
 
 			// download zip
 			download(new File(zipfile.getAbsolutePath()));
 
 			FacesContext facesContext = FacesContext
 			.getCurrentInstance();
 			facesContext.responseComplete();
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			// delete the files - Directory courseid_instructorid and
 			// it's child
 			if (packagedir != null && packagedir.exists())
 				meleteExportService.deleteFiles(packagedir
 						.getParentFile());
 
 		}
 
 		if (logger.isDebugEnabled())
 			logger.debug("Exiting exportModules....");
 
 	}
 
 	/**
 	 * exports the modules according to ADL SCORM 2004 3rd Edition
 	 *
 	 * @return navigation page
 	 */
 	public void exportScormModules(List<Module> selectList) throws Exception
 	{
 		if (logger.isDebugEnabled()) logger.debug("Starting exportModules....");
 		FacesContext context = FacesContext.getCurrentInstance();
 		String instr_id = getMeleteSiteAndUserInfo().getCurrentUser().getId();
 		String courseId = getMeleteSiteAndUserInfo().getCurrentSiteId();
 
 		String packagingdirpath = context.getExternalContext().getInitParameter("packagingdir") + File.separator + "packagefilesscorm";
 		File packagedir = null;
 		try
 		{
 			File basePackDir = new File(packagingdirpath);
 			if (!basePackDir.exists()) basePackDir.mkdirs();
 
 			String title = getMeleteSiteAndUserInfo().getCourseTitle();
 			packagedir = new File(basePackDir.getAbsolutePath() + File.separator + courseId + "_" + instr_id + File.separator
 					+ title.replace(' ', '_') + "scorm");
 			// delete exisiting files
 			meleteExportScormService.deleteFiles(packagedir);
 
 			if (!packagedir.exists()) packagedir.mkdirs();
 
 			// 1- unzip the SCORM2004_BASE_FILENAME in the packagedir
 			// Directory
 			File scorm2004BaseFile = new File(basePackDir.getAbsolutePath() + File.separator + SCORM2004_BASE_FILENAME);
 			unZipFile(scorm2004BaseFile, packagedir.getAbsolutePath());
 
 			// 2- Get the manifestBaseFile
 			String exisXmlFile = packagedir.getAbsolutePath() + File.separator + IMS_MANIFEST_FILENAME;
 			File manifestFile = new File(exisXmlFile);
 
 			meleteExportScormService.initValues();
 			Element manifest = createManifestMetadata(manifestFile, meleteExportScormService);
 
 			// copy the schema files
 			File schemaFilesDir = basePackDir;
 
 			List orgResElements = meleteExportScormService.generateOrganizationResourceItems(selectList, packagedir, title);
 
 			if (orgResElements != null && orgResElements.size() > 0)
 			{
 				manifest.add((Element) orgResElements.get(0));
 				manifest.add((Element) orgResElements.get(1));
 			}
 
 			// create xml document and add element
 			Document document = XMLHelper.createXMLDocument(manifest);
 
 			String newXmlFile = packagedir.getAbsolutePath() + File.separator + IMS_MANIFEST_FILENAME;
 			// generate xml file
 			XMLHelper.generateXMLFile(newXmlFile, document);
 
 			// validate new manifest xml
 			XMLHelper.parseFile(new File(newXmlFile));
 
 			title = Validator.escapeResourceName(title);
 			String outputfilename = packagedir.getParentFile().getAbsolutePath() + File.separator + title.replace(' ', '_') + "_scorm.zip";
 
 			File zipfile = new File(outputfilename);
 			// create zip
 			createZip(packagedir, zipfile);
 
 			// download zip
 			download(new File(zipfile.getAbsolutePath()));
 
 			FacesContext facesContext = FacesContext.getCurrentInstance();
 			facesContext.responseComplete();
 
 		}
 		catch (Exception e)
 		{
 			throw e;
 		}
 		finally
 		{
 			// delete the files - Directory courseid_instructorid and
 			// it's child
 			if (packagedir != null && packagedir.exists()) meleteExportScormService.deleteFiles(packagedir.getParentFile());
 
 		}
 		if (logger.isDebugEnabled()) logger.debug("Exiting exportModules....");
 
 	}
 
 	/**
 	 * imports the modules according to imsglobal content packaging specs 1.1.4
 	 *
 	 * @return navigation page
 	 */
 	public String importModules() {
 
 		if (logger.isDebugEnabled())
 			logger.debug("Entering importModules....");
 
 		FacesContext context = FacesContext.getCurrentInstance();
 		ResourceLoader bundle = new ResourceLoader(
 				"org.sakaiproject.tool.melete.bundle.Messages");
 		FileItem fileItem = (FileItem) context.getExternalContext()
 				.getRequestMap().get("impfile");
 
 		if (fileItem != null) {
 			String uploadedFileName = fileItem.getName();
 			if (uploadedFileName != null
 					&& uploadedFileName.trim().length() > 0) {
 				// check for zip file
 				// if (uploadedFileName.endsWith(".zip")) {
 				if (uploadedFileName.lastIndexOf('.') != -1
 						&& uploadedFileName.substring(
 								uploadedFileName.lastIndexOf('.') + 1)
 								.equalsIgnoreCase("zip")) {
 					File unpackagedir = null;
 					try {
 						String packagingdirpath = context.getExternalContext()
 								.getInitParameter("packagingdir");
 						String instr_id = getMeleteSiteAndUserInfo()
 								.getCurrentUser().getId();
 						String courseId = getMeleteSiteAndUserInfo()
 								.getCurrentSiteId();
 
 						File basePackDir = new File(packagingdirpath);
 						if (!basePackDir.exists())
 							basePackDir.mkdirs();
 						String title = getMeleteSiteAndUserInfo()
 								.getCourseTitle();
 
 						unpackagedir = new File(basePackDir.getAbsolutePath()
 								+ File.separator + "import" + File.separator
 								+ courseId + "_" + instr_id + File.separator
 								+ title.replace(' ', '_'));
 						// delete exisiting files
 						meleteImportService.deleteFiles(unpackagedir);
 
 						if (!unpackagedir.exists())
 							unpackagedir.mkdirs();
 						String actFileName;
 						// write the uploaded zip file to disk
 						if (uploadedFileName.indexOf('\\') != -1)
 							actFileName = uploadedFileName
 									.substring(uploadedFileName
 											.lastIndexOf('\\') + 1);
 						else
 							actFileName = uploadedFileName
 									.substring(uploadedFileName
 											.lastIndexOf('/') + 1);
 						// write file to disk
 						File outputFile = new File(unpackagedir
 								+ File.separator + actFileName);
 						writeFileToDisk(fileItem, outputFile);
 						// unzipping dir
 						File unzippeddir = new File(unpackagedir
 								.getAbsolutePath()
 								+ File.separator
 								+ actFileName.substring(0, actFileName
 										.lastIndexOf('.') + 1));
 						if (!unzippeddir.exists())
 							unzippeddir.mkdirs();
 						String unzippedDirpath = unzippeddir.getAbsolutePath();
 						// unzip files
 						unZipFile(outputFile, unzippedDirpath);
 
 						File imsmanifest = new File(unzippedDirpath
 								+ File.separator + IMS_MANIFEST_FILENAME);
 						validatemanifest(imsmanifest);
 
 						Document document = XMLHelper.parseFile(imsmanifest);
 						// Mallika - line below changes
 						meleteImportService.parseAndBuildModules(document,
 								unzippedDirpath);
 
 						// message as import is success
 						String infoMsg = bundle.getString("import_success");
 						FacesMessage msg = new FacesMessage(null, infoMsg);
 						msg.setSeverity(FacesMessage.SEVERITY_INFO);
 						context.addMessage(null, msg);
 
 					} catch (Exception e) {
 						if (logger.isErrorEnabled())
 							logger.error(this.getClass().getName() + " : "
 									+ e.toString());
 
 						String infoMsg = bundle.getString("error_importing");
 						FacesMessage msg = new FacesMessage(null, infoMsg);
 						msg.setSeverity(FacesMessage.SEVERITY_ERROR);
 						context.addMessage(null, msg);
 						e.printStackTrace();
 					} finally {
 						// delete the files - Directory courseid_instructorid
 						// and
 						// it's child
 						if (unpackagedir != null && unpackagedir.exists())
 							meleteImportService.deleteFiles(unpackagedir
 									.getParentFile());
 					}
 				} else {
 					// add message as file is not a zip file
 					String infoMsg = bundle.getString("import_file_not_zip");
 					FacesMessage msg = new FacesMessage(null, infoMsg);
 					msg.setSeverity(FacesMessage.SEVERITY_INFO);
 					context.addMessage(null, msg);
 				}
 			} else {
 				String infoMsg = bundle
 						.getString("import_content_file_not_selected");
 				FacesMessage msg = new FacesMessage(null, infoMsg);
 				msg.setSeverity(FacesMessage.SEVERITY_INFO);
 				context.addMessage(null, msg);
 			}
 
 		}
 
 		if (logger.isDebugEnabled())
 			logger.debug("Exiting importModules....");
 
 		return "importexportmodules";
 	}
 
 	/**
 	 * validate manifest with imscp spec version 1.1.4
 	 *
 	 * @param imsmanifest
 	 */
 	private void validatemanifest(File imsmanifest) {
 	}
 
 	/**
 	 * writes the file to disk
 	 *
 	 * @param fileItem
 	 *            Fileitem
 	 * @param outputFile
 	 *            out put file
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void writeFileToDisk(FileItem fileItem, File outputFile)
 			throws FileNotFoundException, IOException {
 		FileInputStream in = null;
 		FileOutputStream out = null;
 		try {
 			in = (FileInputStream) fileItem.getInputStream();
 			out = new FileOutputStream(outputFile);
 
 			int len;
 			byte buf[] = new byte[102400];
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 		} catch (FileNotFoundException e) {
 			throw e;
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			try {
 				if (in != null)
 					in.close();
 			} catch (IOException e1) {
 			}
 			try {
 				if (out != null)
 					out.close();
 			} catch (IOException e2) {
 			}
 		}
 	}
 
 	/**
 	 * unzip the file and write to disk
 	 *
 	 * @param zipfile
 	 * @param dirpath
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void unZipFile(File zipfile, String dirpath) throws Exception {
 		FileInputStream fis = new FileInputStream(zipfile);
 		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
 		ZipEntry entry;
 		while ((entry = zis.getNextEntry()) != null) {
 			if (entry.isDirectory()) {
 
 			} else if (entry.getName().lastIndexOf('\\') != -1) {
 				String filenameincpath = entry.getName();
 
 				String actFileNameIncPath = dirpath;
 
 				while (filenameincpath.indexOf('\\') != -1) {
 					String subFolName = filenameincpath.substring(0,
 							filenameincpath.indexOf('\\'));
 
 					File subfol = new File(actFileNameIncPath + File.separator
 							+ subFolName);
 					if (!subfol.exists())
 						subfol.mkdirs();
 
 					actFileNameIncPath = actFileNameIncPath + File.separator
 							+ subFolName;
 
 					filenameincpath = filenameincpath.substring(filenameincpath
 							.indexOf('\\') + 1);
 				}
 
 				String filename = entry.getName().substring(
 						entry.getName().lastIndexOf('\\') + 1);
 				unzip(zis, actFileNameIncPath + File.separator + filename);
 			} else if (entry.getName().lastIndexOf('/') != -1) {
 				String filenameincpath = entry.getName();
 
 				String actFileNameIncPath = dirpath;
 
 				while (filenameincpath.indexOf('/') != -1) {
 					String subFolName = filenameincpath.substring(0,
 							filenameincpath.indexOf('/'));
 					File subfol = new File(actFileNameIncPath + File.separator
 							+ subFolName);
 					if (!subfol.exists())
 						subfol.mkdirs();
 
 					actFileNameIncPath = actFileNameIncPath + File.separator
 							+ subFolName;
 
 					filenameincpath = filenameincpath.substring(filenameincpath
 							.indexOf('/') + 1);
 				}
 
 				String filename = entry.getName().substring(
 						entry.getName().lastIndexOf('/') + 1);
 				unzip(zis, actFileNameIncPath + File.separator + filename);
 			} else
 				unzip(zis, dirpath + File.separator + entry.getName());
 		}
 		zis.close();
 	}
 
 	/**
 	 * write zip file to disk
 	 *
 	 * @param zis
 	 * @param name
 	 * @throws IOException
 	 */
 	private void unzip(ZipInputStream zis, String name) throws Exception {
 		BufferedOutputStream dest = null;
 		int count;
 		byte data[] = new byte[BUFFER];
 		try {
 			// write the files to the disk
 			FileOutputStream fos = new FileOutputStream(name);
 			dest = new BufferedOutputStream(fos, BUFFER);
 			while ((count = zis.read(data, 0, BUFFER)) != -1) {
 				dest.write(data, 0, count);
 			}
 			dest.flush();
 		} catch (FileNotFoundException e) {
 			throw e;
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			try {
 				if (dest != null)
 					dest.close();
 			} catch (IOException e1) {
 			}
 		}
 	}
 
 	/**
 	 * creates zip file
 	 *
 	 * @param inputFile -
 	 *            input directory that is to be zipped
 	 * @param outputfile -
 	 *            out zip file
 	 * @throws Exception
 	 */
 	private void createZip(File inputFile, File outputfile) throws Exception {
 		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(
 				outputfile));
 		writeToZip(inputFile, zout, inputFile.getName());
 		zout.close();
 	}
 
 	/**
 	 * writes the zip file to browser
 	 *
 	 * @param file -
 	 *            zip file to download
 	 * @throws Exception
 	 */
 	private void download(File file) throws Exception {
 		FileInputStream fis = null;
 		ServletOutputStream out = null;
 		try {
 			String disposition = "attachment; filename=\"" + file.getName()
 					+ "\"";
 			fis = new FileInputStream(file);
 
 			FacesContext cxt = FacesContext.getCurrentInstance();
 			ExternalContext context = cxt.getExternalContext();
 			HttpServletResponse response = (HttpServletResponse) context
 					.getResponse();
 			response.setContentType("application/zip"); // application/zip
 			response.addHeader("Content-Disposition", disposition);
 			// Contributed by Diego for ME-233
 			response.setHeader("Pragma", "public");
 			response.setHeader("Cache-Control",
 					"public, post-check=0, must-revalidate, pre-check=0");
 
 			out = response.getOutputStream();
 
 			int len;
 			byte buf[] = new byte[102400];
 			while ((len = fis.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 
 			out.flush();
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			try {
 				if (out != null)
 					out.close();
 			} catch (IOException e1) {
 			}
 
 			try {
 				if (fis != null)
 					fis.close();
 			} catch (IOException e2) {
 			}
 		}
 	}
 
 	/**
 	 * @param inputFile -
 	 *            file to zip
 	 * @param zout -
 	 *            zip output stream
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	private void writeToZip(File inputFile, ZipOutputStream zout,
 			String baseFileName) throws FileNotFoundException, IOException {
 		File files[] = inputFile.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].isFile()) {
 				FileInputStream in = new FileInputStream(files[i]);
 
 				String name = null;
 				String filepath = files[i].getAbsolutePath();
				name = filepath.substring(filepath.indexOf(baseFileName)
						+ baseFileName.length() + 1);
 				zout.putNextEntry(new ZipEntry(name));
 
 				// Transfer bytes from the file to the ZIP file
 				int len;
 				byte buf[] = new byte[102400];
 				while ((len = in.read(buf)) > 0) {
 					zout.write(buf, 0, len);
 				}
 				zout.closeEntry();
 				in.close();
 			} else {
 				writeToZip(files[i], zout, baseFileName);
 			}
 		}
 	}
 
 	/**
 	 * get MeleteSiteAndUserInfo
 	 *
 	 * @return
 	 */
 	private MeleteSiteAndUserInfo getMeleteSiteAndUserInfo() {
 		if (meleteSiteAndUserInfo == null) {
 			FacesContext context = FacesContext.getCurrentInstance();
 			ValueBinding binding = Util.getBinding("#{meleteSiteAndUserInfo}");
 			meleteSiteAndUserInfo = (MeleteSiteAndUserInfo) binding
 					.getValue(context);
 
 			return meleteSiteAndUserInfo;
 		} else
 			return meleteSiteAndUserInfo;
 	}
 
 	/**
 	 * creates manifest metadata - course title and description
 	 *
 	 * @param manifestFile -
 	 *            manifest file
 	 * @return - the manifest element
 	 * @throws Exception
 	 */
 	private Element createManifestMetadata(File manifestFile,
 			MeleteExportService meleteExportService) throws Exception {
 		try {
 
 			Element manifest = meleteExportService.getManifest(manifestFile);
 
 			// create manifest metadata
 			Element manifestMetadata = meleteExportService
 					.createManifestMetadata();
 
 			// imsmd:lom
 			Element imsmdlom = meleteExportService.createLOMElement(
 					"imsmd:lom", "lom");
 			// imsmd:general
 			Element imsmdgeneral = meleteExportService.createLOMElement(
 					"imsmd:general", "general");
 
 			// title
 			String title = getMeleteSiteAndUserInfo().getCourseTitle();
 			if (title != null) {
 				Element metadataTitle = meleteExportService
 						.createMetadataTitle(title);
 				imsmdgeneral.add(metadataTitle);
 			}
 
 			// description
 			String description = getMeleteSiteAndUserInfo()
 					.getCourseDescription();
 			if (description != null) {
 				Element metadataDesc = meleteExportService
 						.createMetadataDescription(description);
 				imsmdgeneral.add(metadataDesc);
 			}
 
 			imsmdlom.add(imsmdgeneral);
 			manifestMetadata.add(imsmdlom);
 
 			manifest.add(manifestMetadata);
 
 			return manifest;
 
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 	/**
 	 * @return Returns the moduleService.
 	 */
 	public ModuleService getModuleService() {
 		return moduleService;
 	}
 
 	/**
 	 * @param moduleService
 	 *            The moduleService to set.
 	 */
 	public void setModuleService(ModuleService moduleService) {
 		this.moduleService = moduleService;
 	}
 
 	/**
 	 * @param logger
 	 *            The logger to set.
 	 */
 	public void setLogger(Log logger) {
 		this.logger = logger;
 	}
 
 	/**
 	 * @return Returns the uploadmax.
 	 */
 	public String getUploadmax() {
 		int uploadmax = ServerConfigurationService.getInt(
 				"content.upload.ceiling", 50);
 		return new Integer(uploadmax).toString();
 	}
 	/**
 	 * @param uploadmax
 	 *            The uploadmax to set.
 	 */
 	public void setUploadmax(int uploadmax) {
 		this.uploadmax = uploadmax;
 	}
 
 	/**
 	 * @param meleteExportService
 	 *            The meleteExportService to set.
 	 */
 	public void setMeleteExportService(MeleteExportService meleteExportService) {
 		this.meleteExportService = meleteExportService;
 	}
 
 	public void setMeleteExportScormService(
 			MeleteExportService meleteExportScormService) {
 		this.meleteExportScormService = meleteExportScormService;
 	}
 
 	/**
 	 * @param meleteImportService
 	 *            The meleteImportService to set.
 	 */
 	public void setMeleteImportService(MeleteImportService meleteImportService) {
 		this.meleteImportService = meleteImportService;
 	}
 
 
 	/**
 	 * @return the selectedModules
 	 */
 	public List getSelectedModules()
 	{
 		return this.selectedModules;
 	}
 
 
 	/**
 	 * @param selectedModules the selectedModules to set
 	 */
 	public void setSelectedModules(List selectedModules)
 	{
 		this.selectedModules = selectedModules;
 		logger.debug("set list is" + selectedModules.toString());
 	}
 
 	/**
 	 * @return the noFlag
 	 */
 	public boolean isNoFlag()
 	{
 		return this.noFlag;
 	}
 
 	/**
 	 * @return the selectFormat
 	 */
 	public String getSelectFormat()
 	{
 		return this.selectFormat;
 	}
 
 	/**
 	 * @param selectFormat the selectFormat to set
 	 */
 	public void setSelectFormat(String selectFormat)
 	{
 		this.selectFormat = selectFormat;
 	}
 }

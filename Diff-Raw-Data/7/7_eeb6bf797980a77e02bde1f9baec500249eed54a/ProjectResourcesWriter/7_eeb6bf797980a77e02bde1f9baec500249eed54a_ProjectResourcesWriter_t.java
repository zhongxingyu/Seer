 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.internal.project;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.PropertyException;
 
 import org.zend.sdklib.application.ZendProject.TemplateApplications;
 import org.zend.sdklib.descriptor.pkg.Package;
 import org.zend.sdklib.internal.project.ScriptsWriter.DeploymentScriptTypes;
 import org.zend.sdklib.internal.utils.JaxbHelper;
 
 /**
  * Project creation and update handling including descriptor, scripts and
  * application resources
  */
 public class ProjectResourcesWriter {
 
 	public static final String DESCRIPTOR = "deployment.xml";
 
 	// properties of the subject project
 	private final String name;
 
 	/**
 	 * @param name
 	 *            of the application
 	 * @param path
 	 *            project root
 	 * @param withScripts2
 	 *            true if scripts are added to the project
 	 * @param WelcomePgae
 	 * @param isZend
 	 */
 	public ProjectResourcesWriter(String name) {
 		this.name = name;
 	}
 
 	public ProjectResourcesWriter(File projectPath) {
 		this(getProjectName(new File(projectPath, DESCRIPTOR)));
 	}
 
 	/**
 	 * Writing descriptor file to the root project
 	 * 
 	 * @param name
 	 *            - name of the project
 	 * @param withContent
 	 *            - whether to write other contents than scripts and descriptor
 	 * @param withScripts
 	 *            - whether to write scripts
 	 * @param destination
 	 *            - destination directory
 	 * @throws IOException
 	 * @throws JAXBException
 	 * @throws PropertyException
 	 */
 	public File writeDescriptor(File destination) throws IOException,
 			PropertyException, JAXBException {
 		File descrFile = new File(destination, DESCRIPTOR);
 
 		if (!descrFile.exists()) {
 			final boolean n = descrFile.createNewFile();
 			if (!n) {
 				throw new IOException("Error creating file "
 						+ destination.getAbsolutePath());
 			}
 			writeDescriptor(new FileOutputStream(descrFile));
 		}
 		return descrFile;
 	}
 
 	/**
 	 * Writing project descriptor to a given output stream
 	 * 
 	 * @param outputStream
 	 * @throws IOException
 	 * @throws PropertyException
 	 * @throws JAXBException
 	 */
 	public void writeDescriptor(OutputStream outputStream) throws IOException,
 			PropertyException, JAXBException {
 		if (name == null) {
 			throw new IllegalArgumentException(
 					"Failed to create deployment descriptor. Project name is missing");
 		}
 
 		DescriptorWriter w = new DescriptorWriter(xmlEscape(name), "data",
 				"1.0");
 		w.write(outputStream);
 		outputStream.close();
 	}
 
 	/**
 	 * Writes scripts under destination with a given list of scripts (all or
 	 * nothing are
 	 * 
 	 * @param path
 	 * @param withScripts
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	public void writeScriptsByName(File descriptor, String withScripts)
 			throws IOException, JAXBException {
 		if (withScripts == null) {
 			return;
 		}
 
 		File destination = getScriptsDirectory(descriptor);
 		if (destination != null && !destination.isDirectory()) {
 			destination.mkdirs();
 		}
 
 		final ScriptsWriter w = new ScriptsWriter();
 		if ("all".equals(withScripts)) {
 			w.writeAllScripts(destination);
 			return;
 		}
 
 		final DeploymentScriptTypes n = DeploymentScriptTypes
 				.byName(withScripts);
 		if (n != null) {
 			w.writeSpecificScript(destination, n);
 		} else {
 			throw new IllegalArgumentException(MessageFormat.format(
 					"script with name {0} cannot be found", withScripts));
 		}
 	}
 
 	// TODO: change the way scripts is resolved, should be resolved by
 	// descriptor.properties
 	private File getScriptsDirectory(File descriptor) throws IOException,
 			JAXBException, FileNotFoundException {
 		final Package pkg = JaxbHelper.unmarshalPackage(new FileInputStream(
 				descriptor));
 
 		String scriptsdir = pkg.getScriptsdir();
 		if (scriptsdir == null) {
 			scriptsdir = "scripts";
 		}
 		File destination = new File(descriptor.getParentFile(), scriptsdir);
 		return destination;
 	}
 
 	private static String getProjectName(File descriptor) {
 
 		Package pkg;
 		try {
 			pkg = JaxbHelper.unmarshalPackage(new FileInputStream(descriptor));
 		} catch (FileNotFoundException e) {
 			// no descriptor file - choose project name as direcory name
 			final File parentFile = descriptor.getParentFile();
 			return parentFile.getName();
 		} catch (IOException e) {
 			throw new IllegalArgumentException("Error reading descriptor file "
 					+ descriptor.getAbsolutePath());
 		} catch (JAXBException e) {
 			throw new IllegalArgumentException("Error reading descriptor file "
 					+ descriptor.getAbsolutePath());
 		}
 
 		if (pkg == null) {
 			throw new IllegalArgumentException("Error reading descriptor file "
 					+ descriptor.getAbsolutePath());
 		}
 
 		return pkg.getName();
 	}
 
 	/**
 	 * Writes an application to a given destination directory
 	 * 
 	 * @param destination
 	 * @param app
 	 * @throws IOException
 	 */
 	public void writeApplication(File destination, TemplateApplications app)
 			throws IOException {
 
 		final List<String> allResources = getAllResources(app);
 
 		for (String path : allResources) {
 
 			// file handling
 			if (!path.endsWith("/")) {
 				copyFile(destination, path, app);
 			} else {
				createFolder(destination, path, app);
 			}
 
 		}
 	}
 
	private boolean createFolder(File destination, String path, TemplateApplications app) {
		final File file = new File(destination, relativeToApp(path, app));
 		return file.mkdirs();
 	}
 
 	private void copyFile(File destination, String path, TemplateApplications app) throws IOException,
 			FileNotFoundException {
 		if (path.length() == 0) {
 			return;
 		}
 		final InputStream is = this.getClass().getResourceAsStream(path);
 		if (is == null) {
 			throw new IOException(path + " path is not found");
 		}
 		
 		File outputFile = new File(destination, relativeToApp(path, app));
 
 		// create canonical structure
 		outputFile.getParentFile().mkdirs();
 
 		if (!outputFile.createNewFile()) {
 			throw new IOException("Cannot create file "
 					+ outputFile.getAbsolutePath());
 		}
 
 		InputOutputResource ior = new InputOutputResource(is,
 				new FileOutputStream(outputFile));
 		ior.copy();
 	}
 
 	private String relativeToApp(String path, TemplateApplications app) {
 		final int length = app.getBasePath().length();
 		return path.substring(length);
 	}
 
 	private List<String> getAllResources(TemplateApplications app)
 			throws IOException {
 		final InputStream iStream = this.getClass().getResourceAsStream(
 				app.getMap());
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				iStream));
 
 		List<String> paths = new ArrayList<String>();
 		String readLine = reader.readLine();
 		while (readLine != null) {
 			paths.add(readLine.trim());
 			readLine = reader.readLine();
 		}
 		return paths;
 	}
 
 	private String xmlEscape(String name) {
 		for (int i = 0; i < name.length(); i++) {
 			char c = name.charAt(i);
 			if (c == '&' || c == '<' || c == '>') {
 				return "<![CDATA[" + name.replaceAll("]]>", "]]>]]><![CDATA[")
 						+ "]]>";
 			}
 		}
 
 		return name;
 	}
 
 }

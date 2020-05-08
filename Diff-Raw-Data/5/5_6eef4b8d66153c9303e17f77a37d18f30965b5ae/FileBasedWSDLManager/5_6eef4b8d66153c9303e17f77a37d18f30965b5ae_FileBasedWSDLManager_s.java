 /*******************************************************************************
 * Copyright (c) 2008, 2009 SOPERA GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SOPERA GmbH - initial API and implementation
 *******************************************************************************/
 package org.eclipse.swordfish.registry;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.Writer;
 
 import javax.wsdl.WSDLException;
 
 import org.eclipse.swordfish.registry.domain.Definition;
 import org.eclipse.swordfish.registry.domain.DefinitionImpl;
 import org.eclipse.swordfish.registry.domain.WSDLCreator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class FileBasedWSDLManager implements WSDLResourceReader {
 
 	private static final Log LOGGER = LogFactory.getLog(FileBasedWSDLManager.class);
 
 	private static final String LOCATION_PROPERTY = "org.eclipse.swordfish.registry.fileLocation";
 
 	private File wsdlDirectory; 
 	
 	private InMemoryRepository repos;
 
 	private WSDLCreator wsdlCreator;
 	
 	FileBasedWSDLManager() {
 	}
 	
 	public void setRepository(InMemoryRepository repository) {
 		repos = repository;
 	}
 
 	public void setDirectory(String directoryName)  throws RegistryException {
 		setDirectory(new File(directoryName));
 	}
 
 	public void setWsdlCreator(WSDLCreator creator) {
 		wsdlCreator = creator;
 	}
 
 	public void setDirectory(File directory) throws RegistryException {
 		if (!(directory.exists() && directory.isDirectory())) {
 			throw new RegistryException("The directory "  + directory.getAbsolutePath() + " specified to contain the registry WSDL's does either not exist or is not a directory.");			
 		}
 		wsdlDirectory = directory;
 	}
 
 	public void fill() throws RegistryException {
 		ensureDirectoryDefined();
 		
 		File[] files = wsdlDirectory.listFiles();
 		
 		for (File file : files) {
 			try {
 				add(file.getName(), new FileReader(file));
 			} catch (InvalidFormatException e) {
				LOGGER.error("File " + file.getAbsolutePath() + "is not a valid WSDL.", e);
 			} catch (IOException e) {
				LOGGER.error("Unable to load file " + file.getAbsolutePath(), e);
 			}
 		}
 	}
 	
 	public Resource getResource(String id) {
 		File location = new File(wsdlDirectory,id);
 		return new WSDLResource(new FileData(location));
 	}
 
 	public void add(String id, Reader reader) throws InvalidFormatException, IOException {
 		javax.wsdl.Definition wsdl4jDef = null;
 		try {
 			wsdl4jDef = wsdlCreator.definition(reader);
 		} catch(WSDLException e ) {
 			throw new InvalidFormatException(e);
 		}
 		Definition definition = new DefinitionImpl(id, wsdl4jDef);
 		
 		delete(id);
 		definition.register(repos);
 	}
 	
 	public void delete(String id) {
 		Definition wsdlDefinition = repos.getWSDLDefinitionById(id);
 		if (wsdlDefinition != null) {
 			wsdlDefinition.deregister(repos);
 		}
 	}
 
 	private void ensureDirectoryDefined() throws RegistryException {
 		if (wsdlDirectory == null) {
 			String fileLocation = System.getProperty(LOCATION_PROPERTY);
 		
 			if (fileLocation == null) {
 				throw new RegistryException("The system property " + LOCATION_PROPERTY + " is not defined.");
 			}
 			setDirectory(fileLocation);
 		}
 	}
 	
 	class FileData implements PersistentData {
 		private File file;
 
 		FileData(File file) {
 			this.file = file;
 		}
 		
 		public boolean isExisting() {
 			return file.exists();
 		}
 		
 		public String getId() {
 			return file.getName();	
 		}
 		
 		public void read(Writer writer) throws IOException {
 			swap(new FileReader(file), writer);
 		}
 		
 		public InputStream read() throws IOException {
 			return new FileInputStream(file);
 		}
 		
 		public void write(Reader reader) throws InvalidFormatException, IOException {
 			Writer writer = new FileWriter(file); 
 			try {
 				swap(reader, writer);
 			} finally {
 				writer.flush();
 			}
 				
 			try {
 				add(file.getName(), new FileReader(file));
 			} catch (InvalidFormatException e) {
 				file.delete();
 				throw e;
 			} catch(IOException e) {
 				file.delete();
 				throw e;
 			}
 		}
 
 		public OutputStream write() throws IOException{
 			return new FileOutputStream(file); 
 		}
 		
 		public void delete() {
 			FileBasedWSDLManager.this.delete(file.getName());
 			file.delete();
 		}
 	}
 
 	private static void swap(Reader reader, Writer writer) throws IOException {
 		char[] buffer = new char[4000];
 		int length = 0;
 		do {
 			length = reader.read(buffer);
 			if (length > 0) {
 				writer.write(buffer, 0, length);
 			}
 		} while (length >= 0);
 	}
 }

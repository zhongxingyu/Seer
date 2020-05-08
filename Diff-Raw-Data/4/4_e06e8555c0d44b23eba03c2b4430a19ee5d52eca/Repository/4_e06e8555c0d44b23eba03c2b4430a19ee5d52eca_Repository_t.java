 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.imirsel.nema.flowmetadataservice.impl;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 
 import org.imirsel.nema.client.beans.converters.IBeanConverter;
 import org.imirsel.nema.client.beans.converters.MeandreConverter;
 import org.imirsel.nema.client.beans.repository.WBExecutableComponentDescription;
 import org.imirsel.nema.client.beans.repository.WBFlowDescription;
 import org.imirsel.nema.flowservice.MeandreServerException;
 import org.imirsel.nema.flowservice.MeandreServerProxy;
 import org.meandre.core.repository.FlowDescription;
 import org.meandre.core.repository.QueryableRepository;
 import org.meandre.core.repository.RepositoryImpl;
 import org.springframework.core.io.DefaultResourceLoader;
 import org.springframework.core.io.Resource;
 
 import com.hp.hpl.jena.rdf.model.Model;
 
 /**
  * Encapsulates the Flow Repository for a {@link MeandreServerProxy}
  * 
  * @author kumaramit01
  * @since 0.5.0
  *
  */
 public class Repository {
 
 	private final Logger logger = Logger.getLogger(this.getClass().getName());
 	public MeandreServerProxy meandreServerProxy;
 	private String repositoryLocation;
 	
 	@PostConstruct
 	public void init() throws MeandreServerException {
 		DefaultResourceLoader drl = new DefaultResourceLoader();
 		Resource resource=drl.getResource("flowrepository.properties");
 		if(resource!=null){
 			Properties properties = new Properties();
 			try {
 				properties.load(resource.getInputStream());
 				setRepositoryLocation(properties.getProperty("flowresource.location","repository"));
 			} catch (IOException e) {
 				logger.warning("flowrepository.properties file not found -using the default properties");
 				setRepositoryLocation("repository");
 			}
 		}else{
 			logger.warning("fowrepository.properties file not found -using the default properties");
 			setRepositoryLocation("repository");
 		}
 		File file = new File(this.getRepositoryLocation());
 		if(!file.exists()){
 			boolean success=file.mkdirs();
 			if(!success){
 				throw new MeandreServerException("Could not create flow repository location " + file.getAbsolutePath());
 			}
 		}
 	}
 
 	
 
 	public void setRepositoryLocation(String repositoryLocation) {
 		this.repositoryLocation = repositoryLocation;
 	}
 
 	public String getRepositoryLocation() {
 		return repositoryLocation;
 	}
 
 
 
 	// Converts URI to string
 	private static final IBeanConverter<URI, String> UriStringConverter =
 		new IBeanConverter<URI, String>() {
 		public String convert(URI url) {
 			return url.toString();
 		}
 	};
 
 
 	public MeandreServerProxy getMeandreServerProxy() {
 		return meandreServerProxy;
 	}
 
 	public void setMeandreServerProxy(MeandreServerProxy meandreServerProxy) {
 		this.meandreServerProxy = meandreServerProxy;
 	}
 
 
 
 
 
 	public Set<String> retrieveComponentUrls()
 	throws  MeandreServerException {
 		Set<URI> componentUrls = getMeandreServerProxy().retrieveComponentUris();
 		return MeandreConverter.convert(componentUrls, UriStringConverter);
 	}
 
 	public WBExecutableComponentDescription retrieveComponentDescriptor(String componentURL)
 	throws  MeandreServerException {
 		return MeandreConverter.ExecutableComponentDescriptionConverter.convert(
 				getMeandreServerProxy().retrieveComponentDescriptor(componentURL));
 	}
 
 	public Set<WBExecutableComponentDescription> retrieveComponentDescriptors()
 	throws  MeandreServerException {
 		QueryableRepository repository = this.getMeandreServerProxy().getRepository();
 		return MeandreConverter.convert(
 				repository.getAvailableExecutableComponentDescriptions(),
 				MeandreConverter.ExecutableComponentDescriptionConverter);
 	}
 
 	public Set<String> retrieveFlowUrls()
 	throws  MeandreServerException {
 		return MeandreConverter.convert(getMeandreServerProxy().retrieveFlowUris(), UriStringConverter);
 	}
 
 	public WBFlowDescription retrieveFlowDescriptor(String flowURL)
 	throws  MeandreServerException {
 		return MeandreConverter.FlowDescriptionConverter.convert(
 				getMeandreServerProxy().retrieveFlowDescriptor(flowURL));
 	}
 
 	public Set<WBFlowDescription> retrieveFlowDescriptors()
 	throws  MeandreServerException {
 		QueryableRepository repository = this.getMeandreServerProxy().getRepository();
 		return MeandreConverter.convert(
 				repository.getAvailableFlowDescriptions(),
 				MeandreConverter.FlowDescriptionConverter);
 	}
 
 	/**
 	 * Returns the file path to the new flow
 	 * 
 	 * @param wbFlow
 	 * @param userId 
 	 * @return File Path to the new flow
 	 * @throws MeandreServerException
 	 * @throws CorruptedFlowException
 	 */
 	public String saveFlow(WBFlowDescription wbFlow, long userId)
 	throws  MeandreServerException {
 		FlowDescription flow = MeandreConverter.WBFlowDescriptionConverter.convert(wbFlow);
 		String flowURI = flow.getFlowComponent().getURI();
 		logger.info("Saving flow " + flowURI);
 		
 		String execStepMsg = "";
 		String fileName=null;
 		FileOutputStream ttlStream=null,ntStream=null,rdfStream=null;
 		try {
 			Model flowModel = flow.getModel();
 
 			String fName = flowURI.replaceAll(":|/", "_");
 			String tempFolder = this.getRepositoryLocation()+ File.separator+"x"+userId;//System.getProperty("java.io.tmpdir");
 			File file = new File(tempFolder);
 			if(!file.exists()){
 			boolean success=file.mkdir();
 				if(!success){
 					throw new RuntimeException("Error could not create user directory " + file.getAbsolutePath());
 				}
 			}
 			
			//if (!(tempFolder.endsWith("/") || tempFolder.endsWith("\\")))
			//	tempFolder += System.getProperty("file.separator");
 			
 			fileName = tempFolder +  System.getProperty("file.separator")+ fName + ".nt";
 			logger.info(fileName);
 			ntStream = new FileOutputStream(tempFolder + fName + ".nt");
 			flowModel.write(ntStream, "N-TRIPLE");
 			ttlStream = new FileOutputStream(tempFolder + fName + ".ttl");
 			flowModel.write(ttlStream, "TTL");
 			rdfStream = new FileOutputStream(tempFolder + fName + ".rdf");
 			flowModel.write(rdfStream, "RDF/XML-ABBREV");
 			
 
 			execStepMsg = "STEP1: Creating RepositoryImpl from flow model";
 			RepositoryImpl repository = new RepositoryImpl(flowModel);
 			execStepMsg = "STEP2: Retrieving available flows";
 			Set<FlowDescription> flows = repository.getAvailableFlowDescriptions();
 			execStepMsg = "STEP3: Getting flow";
 			flow = flows.iterator().next();
 			if (flow == null){
 				throw new MeandreServerException("The flow obtained is null!");
 			}
 		}catch (Exception e) {
 			MeandreServerException mException = (execStepMsg != null) ?
 					new MeandreServerException(execStepMsg, e) : (MeandreServerException) e;
 			throw mException;
 		}finally{
 			
 				try {
 					if(ttlStream!=null)
 						ttlStream.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				try {
 					if(ntStream!=null)
 						ntStream.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				try {
 					if(rdfStream!=null)
 						rdfStream.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		}
 		return fileName;
 	}
 
 
 	/**Removes the Flow
 	 * 
 	 * @param resourceURL
 	 * @return success true or false
 	 * @throws MeandreServerException
 	 */
 	public boolean removeResource(String resourceURL)
 	throws  MeandreServerException {
 		return getMeandreServerProxy().removeResource(resourceURL);
 	}
 
 
 }

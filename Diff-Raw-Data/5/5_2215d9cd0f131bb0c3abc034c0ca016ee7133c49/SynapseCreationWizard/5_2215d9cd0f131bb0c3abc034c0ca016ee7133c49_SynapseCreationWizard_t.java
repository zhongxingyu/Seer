 /*
  * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.wso2.developerstudio.eclipse.artifact.synapse.ui.wizard;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.FactoryConfigurationError;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.model.PluginExecution;
 import org.apache.maven.model.Repository;
 import org.apache.maven.model.RepositoryPolicy;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.wso2.developerstudio.eclipse.artifact.synapse.Activator;
 import org.wso2.developerstudio.eclipse.artifact.synapse.model.SynapseModel;
 import org.wso2.developerstudio.eclipse.artifact.synapse.template.SynapseClassTemplate;
 import org.wso2.developerstudio.eclipse.artifact.synapse.utils.SynapseImageUtils;
 import org.wso2.developerstudio.eclipse.capp.maven.utils.MavenConstants;
 import org.wso2.developerstudio.eclipse.esb.project.artifact.ESBArtifact;
 import org.wso2.developerstudio.eclipse.esb.project.artifact.ESBProjectArtifact;
 import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
 import org.wso2.developerstudio.eclipse.logging.core.Logger;
 import org.wso2.developerstudio.eclipse.maven.util.MavenUtils;
 import org.wso2.developerstudio.eclipse.platform.ui.wizard.AbstractWSO2ProjectCreationWizard;
 import org.wso2.developerstudio.eclipse.utils.file.FileUtils;
 
 public class SynapseCreationWizard extends AbstractWSO2ProjectCreationWizard {
 	
 	private static IDeveloperStudioLog log=Logger.getLog(Activator.PLUGIN_ID);
 
 	private SynapseModel synapseModel;
 	private IProject esbProject;
 	private IFile synapseConfig;
 	private File pomfile;
 	private IContainer saveLocation;
 	private List<File> fileList = new ArrayList<File>();
 
 
 	public SynapseCreationWizard() {
 		setSynapseModel(new SynapseModel());
 		setModel(synapseModel);
 		setWindowTitle("Create a Synapse Configuration");
 		setDefaultPageImageDescriptor(SynapseImageUtils.getInstance().getImageDescriptor("synapse-wizard.png"));
 	}
 
 	protected boolean isRequireProjectLocationSection() {
 		return false;
 	}
 
 
 	protected boolean isRequiredWorkingSet() {
 	    return false;
 	}
 	
 	public boolean performFinish() {
 		try {
 			esbProject = synapseModel.getEsbProject().getProject();
 			saveLocation = esbProject.getFolder("src" + File.separator + "main" +
                                  File.separator +
                                  "synapse-config");
 			pomfile = esbProject.getFile("pom.xml").getLocation().toFile();
 			getModel().getMavenInfo().setPackageName("synapse/configuration");
 			if (!pomfile.exists()) {
 				createPOM(pomfile);
 			}
 			esbProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 			String groupId = getMavenGroupId(pomfile);
 			if (synapseModel.getSelectedOption().equals("new.synapse")) {
 				this.createNewSynapseConfig(SynapseClassTemplate.getSimpleTemplete(),groupId);
 			} else if (synapseModel.getSelectedOption().equals("new.synapse.tp")) {
 				this.createNewSynapseConfig(SynapseClassTemplate.getRichTemplate(),groupId);
 			} else if (synapseModel.getSelectedOption().equals("import.synapse.config")) {
 				if (synapseModel.isESBartifactsCreate()) {
 					List<OMElement> esbArtiList = synapseModel.getSelectedArtifacts();
 					createESBArtifacts(esbArtiList,groupId);
 				} else {
 					File synConfig = new File(saveLocation.getLocation().toFile(),
 							synapseModel.getImportFile().getName());
 					FileUtils.copy(synapseModel.getImportFile(), synConfig);
 					MavenProject mavenProject = MavenUtils.getMavenProject(pomfile);
 					addPluginEntry(mavenProject,"org.wso2.maven","wso2-esb-synapse-plugin", MavenConstants.WSO2_ESB_SYNAPSE_VERSION,"synapse");
 					MavenUtils.saveMavenProject(mavenProject, pomfile);
 					createArtifactMetaDataEntry(synConfig.getName().substring(0,synConfig.getName().lastIndexOf(".")), "synapse/configuration",
					                            saveLocation.getLocation().toFile(),groupId + ".configuration");
 					fileList.add(synConfig);
 				}
 			}
 			updatePom();
 			esbProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 			
 			if (!fileList.isEmpty()) {
 	            if (MessageDialog.openQuestion(getShell(), "Open file(s) in the Editor",
 	                                           "Do you like to open the file(s) in Developer Studio?")) {
 		            for (File file : fileList) {
 			            openEditor(file);
 		            }
 	            }
             }
 		} catch (Exception e) {
 			log.error(e);
 		}
 		return true;
 	}
 	
 	public void updatePom() throws Exception {
 
 		File mavenProjectPomLocation = esbProject.getFile("pom.xml").getLocation().toFile();
 		MavenProject mavenProject = MavenUtils.getMavenProject(mavenProjectPomLocation);
 
 		addPluginEntry(mavenProject, "org.wso2.maven","wso2-esb-synapse-plugin", MavenConstants.WSO2_ESB_SYNAPSE_VERSION,"synapse");
 		
 		MavenUtils.saveMavenProject(mavenProject, mavenProjectPomLocation);
 	}
 
 	private void addPluginEntry(MavenProject mavenProject, String groupId, String artifactId, String version, String Id) {
 	    List<Plugin> plugins = mavenProject.getBuild().getPlugins();
 		for (Plugin plg : plugins) {
 			if (plg.getGroupId().equalsIgnoreCase(groupId) && plg.getArtifactId().equalsIgnoreCase(artifactId) && plg.getVersion().equalsIgnoreCase(version) ) {
 				return;
 			}
 		}
 		
 		Plugin plugin = MavenUtils.createPluginEntry(mavenProject, groupId, artifactId, version, true);
 		
 		PluginExecution pluginExecution = new PluginExecution();
 		pluginExecution.addGoal("pom-gen");
 		pluginExecution.setPhase("process-resources");
 		pluginExecution.setId(Id);
 		
 		Xpp3Dom configurationNode = MavenUtils.createMainConfigurationNode();
 		Xpp3Dom artifactLocationNode = MavenUtils.createXpp3Node(configurationNode, "artifactLocation");
 		artifactLocationNode.setValue(".");
 		Xpp3Dom typeListNode = MavenUtils.createXpp3Node(configurationNode, "typeList");
 		typeListNode.setValue("${artifact.types}");
 		pluginExecution.setConfiguration(configurationNode);
 		
 		plugin.addExecution(pluginExecution);
 		Repository repo = new Repository();
 		repo.setUrl("http://maven.wso2.org/nexus/content/groups/wso2-public/");
 		repo.setId("wso2-nexus");
 		
 		RepositoryPolicy releasePolicy=new RepositoryPolicy();
 		releasePolicy.setEnabled(true);
 		releasePolicy.setUpdatePolicy("daily");
 		releasePolicy.setChecksumPolicy("ignore");
 		
 		repo.setReleases(releasePolicy);
 		
 		if (!mavenProject.getRepositories().contains(repo)) {
 	        mavenProject.getModel().addRepository(repo);
 	        mavenProject.getModel().addPluginRepository(repo);
         }
     }
 
 	
 
 	// synapse/configuration
 	public void createNewSynapseConfig(String template,String groupId) throws Exception {
 		synapseConfig =synapseModel.getEsbProject().getFile(new Path(synapseModel.getName().replaceAll(".xml$", "")+
 		                                                              ".xml"));
 		File synapseConfigFile =new File(saveLocation.getLocation().toFile(),
 		                                  synapseModel.getName().replaceAll(".xml$", "") + ".xml");
 		FileUtils.writeContent(synapseConfigFile, template);
 		MavenProject mavenProject = MavenUtils.getMavenProject(pomfile);
 		addPluginEntry(mavenProject,"org.wso2.maven","wso2-esb-synapse-plugin", MavenConstants.WSO2_ESB_SYNAPSE_VERSION,"synapse");
 		MavenUtils.saveMavenProject(mavenProject, pomfile);
 		createArtifactMetaDataEntry(synapseModel.getName(), "synapse/configuration",
		                            saveLocation.getLocation().toFile(),groupId + ".configuration");
 		fileList.add(synapseConfigFile);
 	}
 
 //	public boolean addPluginEntry(String groupId, String artifactId, String version) throws Exception {
 //
 //		MavenProject mavenProject = MavenUtils.getMavenProject(pomfile);
 //		List<Plugin> plugins = mavenProject.getBuild().getPlugins();
 //		for (Plugin plg : plugins) {
 //			if (plg.getGroupId().equalsIgnoreCase(groupId) && plg.getArtifactId().equalsIgnoreCase(artifactId) && plg.getVersion().equalsIgnoreCase(version) ) {
 //				return true;
 //			}
 //		}
 //		MavenUtils.createPluginEntry(mavenProject, "org.wso2.maven", artifactId, version, true);
 //		return false;
 //	}
 
 	public void createArtifactMetaDataEntry(String name, String type,File baseDir,String groupId)
 	                                                                 throws FactoryConfigurationError,
 	                                                                 Exception {
 		ESBProjectArtifact esbProjectArtifact = new ESBProjectArtifact();
 		esbProjectArtifact.fromFile(esbProject.getFile("artifact.xml").getLocation().toFile());
 		ESBArtifact artifact = new ESBArtifact();
 		artifact.setName(name);
 		artifact.setVersion("1.0.0");
 		artifact.setType(type);
 		artifact.setServerRole("EnterpriseServiceBus");
 		artifact.setGroupId(groupId);
 		artifact.setFile(FileUtils.getRelativePath(esbProject.getLocation().toFile(),
 			                                           new File(baseDir,
 			                                                    name + ".xml")));	
 		esbProjectArtifact.addESBArtifact(artifact);
 		esbProjectArtifact.toFile();
 	}
 
 	public void setSynapseModel(SynapseModel customMediatorModel) {
 		this.synapseModel = customMediatorModel;
 	}
 
 	public SynapseModel geSynapseModel() {
 		return synapseModel;
 	}
 
 	public IResource getCreatedResource() {
 		return synapseConfig;
 	}
 
 
 	public void copyImportFile(IContainer importLocation) throws IOException {
 		File importFile = getModel().getImportFile();
 		File destFile = new File(importLocation.getLocation().toFile(), importFile.getName());
 		FileUtils.copy(importFile, destFile);
 		fileList.add(destFile);
 	}
 
 	public void createESBArtifacts(List<OMElement> selectedElementsList,String groupId)
 	                                                                    throws FactoryConfigurationError,
 	                                                                    Exception {
 
 		if (selectedElementsList != null) {
 
 			for (OMElement element : selectedElementsList) {
 
 				String localName = element.getLocalName();
 				String qName = element.getAttributeValue(new QName("name"));
 				if (("".equals(qName)) || (qName == null)) {
 					qName = element.getAttributeValue(new QName("key"));
 					if(("".equals(qName)) || (qName == null)){
 						continue;
 					}
 				}
 				synapseModel.setName(qName);
 				
 				String commonESBPath = "src" + File.separator + "main" +
                                                          File.separator +
                                                          "synapse-config" +
                                                          File.separator;
 				if (localName.equalsIgnoreCase("sequence")) {
 					File baseDir = esbProject.getFolder(commonESBPath + "sequences").getLocation().toFile();
 					File destFile = new File(baseDir, qName + ".xml");
 					FileUtils.createFile(destFile, element.toString());
 					MavenProject mavenProject = MavenUtils.getMavenProject(pomfile);
 					addPluginEntry(mavenProject,"org.wso2.maven","wso2-esb-sequence-plugin", MavenConstants.WSO2_ESB_SEQUENCE_VERSION,"sequence");
 					MavenUtils.saveMavenProject(mavenProject, pomfile);
 					fileList.add(destFile);
 					createArtifactMetaDataEntry(qName, "synapse/sequence", baseDir,groupId + ".sequence");
 				} else if (localName.equalsIgnoreCase("endpoint")) {
 					File baseDir = esbProject.getFolder(commonESBPath + "endpoints").getLocation().toFile();
 					File destFile = new File(baseDir, qName + ".xml");
 					FileUtils.createFile(destFile, element.toString());
 					MavenProject mavenProject = MavenUtils.getMavenProject(pomfile);
 					addPluginEntry(mavenProject,"org.wso2.maven","wso2-esb-endpoint-plugin", MavenConstants.WSO2_ESB_ENDPOINT_VERSION,"endpoint");
 					MavenUtils.saveMavenProject(mavenProject, pomfile);
 					fileList.add(destFile);
 					createArtifactMetaDataEntry(qName, "synapse/endpoint", baseDir,groupId + ".endpoint");
 				}else if (localName.equalsIgnoreCase("proxy")) {
 					File baseDir = esbProject.getFolder(commonESBPath + "proxy-services").getLocation().toFile();
 					File destFile = new File(baseDir, qName + ".xml");
 					FileUtils.createFile(destFile, element.toString());
 					MavenProject mavenProject = MavenUtils.getMavenProject(pomfile);
 					addPluginEntry(mavenProject,"org.wso2.maven","wso2-esb-proxy-plugin", MavenConstants.WSO2_ESB_PROXY_VERSION,"proxy");
 					MavenUtils.saveMavenProject(mavenProject, pomfile);
 					fileList.add(destFile);
 					createArtifactMetaDataEntry(qName, "synapse/proxy-service", baseDir,groupId + ".proxy-service");
 				}else if (localName.equalsIgnoreCase("localEntry")) {
 					File baseDir = esbProject.getFolder(commonESBPath + "local-entries").getLocation().toFile();
 					File destFile = new File(baseDir, qName + ".xml");
 					FileUtils.createFile(destFile, element.toString());
 					MavenProject mavenProject = MavenUtils.getMavenProject(pomfile);
 					addPluginEntry(mavenProject,"org.wso2.maven","wso2-esb-localentry-plugin", MavenConstants.WSO2_ESB_LOCAL_ENTRY_VERSION,"localentry");
 					MavenUtils.saveMavenProject(mavenProject, pomfile);
 					fileList.add(destFile);
 					createArtifactMetaDataEntry(qName, "synapse/local-entry", baseDir,groupId + ".local-entry");
 				}	
 			}
 		}
 	}
 	
 	public void openEditor(File file){
 		try {
 			IFile dbsFile  = ResourcesPlugin
 			.getWorkspace()
 			.getRoot()
 			.getFileForLocation(
 					Path.fromOSString(file.getAbsolutePath()));
 			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),dbsFile);
 		} catch (Exception e) { 
 			log.error("cannot open editor",e);
 		}
 	}
 
 }

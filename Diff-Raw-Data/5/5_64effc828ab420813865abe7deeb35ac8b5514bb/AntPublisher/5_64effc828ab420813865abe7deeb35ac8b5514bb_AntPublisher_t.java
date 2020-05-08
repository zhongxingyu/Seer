 /***************************************************************************************************
  * Copyright (c) 2005 Eteration A.S. and Gorkem Ercan. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Gorkem Ercan - initial API and implementation
  *               
  **************************************************************************************************/
 package org.eclipse.jst.server.generic.core.internal.publishers;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import org.eclipse.ant.internal.ui.IAntUIConstants;
 import org.eclipse.ant.internal.ui.launchConfigurations.IAntLaunchConfigurationConstants;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.jst.server.generic.core.internal.CorePlugin;
 import org.eclipse.jst.server.generic.core.internal.GenericPublisher;
 import org.eclipse.jst.server.generic.core.internal.GenericServerCoreMessages;
 import org.eclipse.jst.server.generic.internal.core.util.FileUtil;
 import org.eclipse.jst.server.generic.servertype.definition.Module;
 import org.eclipse.jst.server.generic.servertype.definition.PublisherData;
 import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleArtifact;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.osgi.framework.Bundle;
 /**
  * Ant based publisher.
  * All the properties defined in the server definition file are
  * passed into the ANT build file as properties.
  * In addition to the properties defined in the server definition
  * <I>module.dir</I>, <I>module.name,</I> and <I>server.publish.dir</I> are computed and passed to the 
  * definition file.
  * <ul>
  * <li>module.dir: includes the root of the module project file</li>
  * <li>module.name: the name of the module</li>
  * <li>server.publish.dir: the directory to put the deployment units</li>
  * <li>project.working.dir: the working dir of the project that deployed module is in</li>
  * </ul>
  *
  * @author Gorkem Ercan
  */
 
 public class AntPublisher extends GenericPublisher{
 	private static final String JAR_PROTOCOL_PREFIX = "jar"; //$NON-NLS-1$
 
 	/**
 	 * publisher id for ANT publisher.
 	 */
 	public static final String PUBLISHER_ID="org.eclipse.jst.server.generic.antpublisher"; //$NON-NLS-1$
     
 	private static final String PROP_SERVER_PUBLISH_DIR = "server.publish.dir";//$NON-NLS-1$
 	private static final String PROP_PROJECT_WORKING_DIR= "project.working.dir";//$NON-NLS-1$
 	private static final String PROP_MODULE_DIR = "module.dir";//$NON-NLS-1$
 	private static final String PROP_MODULE_NAME = "module.name";//$NON-NLS-1$
 	private static final String PROP_PROJECT_NAME = "project.name";//$NON-NLS-1$
 	private static final String MODULE_PUBLISH_TARGET_PREFIX = "target.publish."; //$NON-NLS-1$
 	private static final String MODULE_UNPUBLISH_TARGET_PREFIX = "target.unpublish.";//$NON-NLS-1$
 	private static final String DATA_NAME_BUILD_FILE="build.file";//$NON-NLS-1$
 	
 
     /* (non-Javadoc)
 	 * @see org.eclipse.wtp.server.core.model.IPublisher#publish(org.eclipse.wtp.server.core.resources.IModuleResource[], org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public IStatus[] publish(IModuleArtifact[] resource, IProgressMonitor monitor){
 		if(getModule().length>1)// only respond to root module calls. 
 			return null;
 		try{
 			assembleModule(monitor);
         	File file = computeBuildFile();
         	runAnt(file.toString(),getPublishTargetsForModule(),getPublishProperties(),monitor);
         }catch(CoreException e){
             IStatus s = new Status(IStatus.ERROR,CorePlugin.PLUGIN_ID,0,GenericServerCoreMessages.errorPublishAntpublisher,e);
             CorePlugin.getDefault().getLog().log(s);
             return new IStatus[] {s};
         }
 		return null;
 	}
 
 	private void assembleModule(IProgressMonitor monitor)throws CoreException{
 		AbstractModuleAssembler assembler= AbstractModuleAssembler.Factory.getModuleAssembler(getModule()[0], getServer());
 		assembler.assemble(monitor);
 	}
 
     /**
      * 
      * @return
      * @throws CoreException
      */
     private File computeBuildFile() throws CoreException {
         Bundle bundle = Platform.getBundle(getServerRuntime().getServerTypeDefinition().getConfigurationElementNamespace());
         URL bundleUrl =bundle.getEntry(getBuildFile());
         URL fileURL = FileUtil.resolveURL(bundleUrl);
         if(fileURL.getProtocol().equals(JAR_PROTOCOL_PREFIX)){
         	OutputStream os=null;
         	InputStream is=null; 
         	try{
         		String filename =fileURL.getPath();
         		String jarname= fileURL.getFile().substring(0,filename.indexOf('!'));
         		
         		File jarFile = new File(new URL(jarname).getFile());
         		JarFile jar = new JarFile(jarFile);
         		File tmpFile = FileUtil.createTempFile(getBuildFile(),CorePlugin.getDefault().getStateLocation().toOSString());
         		os = new FileOutputStream(tmpFile);
         		String entryname= getBuildFile();
         		if (entryname.startsWith("/"))//$NON-NLS-1$
         			entryname= entryname.substring(1);
         		JarEntry entry = jar.getJarEntry(entryname);
         		is =jar.getInputStream(entry);
         		FileUtil.copy(is,os);
         		return tmpFile;
          	}
         	catch (IOException e) {
         		IStatus s = new Status(IStatus.ERROR,CorePlugin.PLUGIN_ID,0,"error creating temporary build file",e);//$NON-NLS-1$
                 CorePlugin.getDefault().getLog().log(s);
 				throw new CoreException(s);
 			}
         	finally{
         		try {
         			if(is!=null)
         				is.close();
 					if(os!=null)
 						os.close();
 				} catch (IOException e) {
 					//ignore
 				}
         	}
         }
         else{
         	return FileUtil.resolveFile(fileURL);
         } 	
     }
    
     /**
      * @return
      */
     private String getPublishTargetsForModule() {
     	return doGetTargets(MODULE_PUBLISH_TARGET_PREFIX+getModuleTypeId());
     }
 
     /**
      * @return
      */
     private String getUnpublishTargetsForModule() {
         return doGetTargets(MODULE_UNPUBLISH_TARGET_PREFIX+getModuleTypeId());
     }
     
     /**
      * @param dataname
      * @return
      */
     private String doGetTargets(String dataname) {
     	StringBuffer buffer = new StringBuffer();
     	Iterator iterator = getServerRuntime().getServerTypeDefinition().getPublisher(PUBLISHER_ID).getPublisherdata().iterator();
         while(iterator.hasNext()){
             PublisherData data = (PublisherData)iterator.next();
             if(dataname.equals(data.getDataname())) {
                 if(buffer.length()>0)
                 	buffer.append(",");//$NON-NLS-1$
             	buffer.append(data.getDatavalue());
             }   
         }
         return buffer.toString();
     }
 
     private String getModuleTypeId(){
         return getModule()[0].getModuleType().getId();
     }
     
 	private String getBuildFile()
     {
         Iterator iterator = getServerRuntime().getServerTypeDefinition().getPublisher(PUBLISHER_ID).getPublisherdata().iterator();
         while(iterator.hasNext())
         {
             PublisherData data = (PublisherData)iterator.next();
             if(DATA_NAME_BUILD_FILE.equals(data.getDataname()))
                 return getServerRuntime().getServerTypeDefinition().getResolver().resolveProperties(data.getDatavalue());
         }
         return null;
     }
 	
 	private Map getPublishProperties()
 	{
         Map props = new HashMap();
         // pass all properties to build file.
         Map serverProperties = getServer().getServerInstanceProperties();
         Map properties = getServerRuntime().getServerInstanceProperties();
         properties.putAll(serverProperties);
         Iterator propertyIterator = properties.keySet().iterator();
         while(propertyIterator.hasNext())
         {
             String property = (String)propertyIterator.next();
             String value = (String)properties.get(property);
             if(value!=null && value.trim().length()>0)
             	props.put(property,properties.get(property));
         }
         Module module =  getServerRuntime().getServerTypeDefinition().getModule(getModuleTypeId());
 		String modDir = module.getPublishDir();
 		modDir = getServerRuntime().getServerTypeDefinition().getResolver().resolveProperties(modDir);
 		IModule webModule = getModule()[0];
 		
         String moduleName=guessModuleName(webModule);
         props.put(PROP_PROJECT_WORKING_DIR,getProjectWorkingLocation().toString());
 		props.put(PROP_MODULE_NAME,moduleName);
 		props.put(PROP_MODULE_DIR,getModuleWorkingDir().toString());
 		props.put(PROP_PROJECT_NAME,webModule.getProject().getName());
 
 		props.put(PROP_SERVER_PUBLISH_DIR,modDir);
 		return props;
 	}
 	
	private IPath getModuleWorkingDir(){
		return getProjectWorkingLocation().append(getModule()[0].getProject().getName());
 	}
 
 	private IPath getProjectWorkingLocation(){
 		return ServerPlugin.getInstance().getTempDirectory(getServer().getServer().getId());
 	}
 	
 	private String guessModuleName(IModule module) {
 		String moduleName = module.getName(); 
 		if("jst.web".equals(getModuleTypeId())){ //$NON-NLS-1$
 			IWebModule webModule = (IWebModule)getModule()[0].loadAdapter(IWebModule.class,null);
 			String contextRoot = webModule.getURI(module);
 			//TODO we should really pass the full uri including the file extension in the future
 			moduleName = contextRoot.substring(0,contextRoot.lastIndexOf('.'));
 			}
 		return moduleName;
 		}
 
 
 
 	private void runAnt(String buildFile,String targets,Map properties ,IProgressMonitor monitor)throws CoreException{
 		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
 		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(IAntLaunchConfigurationConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE);
 		if(type==null){
 			IStatus s = new Status(IStatus.ERROR,CorePlugin.PLUGIN_ID,0,GenericServerCoreMessages.antLauncherMissing,null);
 			throw new CoreException(s);
 		}
 		ILaunchConfigurationWorkingCopy wc= type.newInstance(null,properties.get(PROP_MODULE_NAME)+" module publisher"); //$NON-NLS-1$
 		wc.setContainer(null);
 		wc.setAttribute(IExternalToolConstants.ATTR_LOCATION, buildFile);
 		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER,"org.eclipse.ant.ui.AntClasspathProvider"); //$NON-NLS-1$
 		wc.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_TARGETS,targets);
 		wc.setAttribute(IAntLaunchConfigurationConstants.ATTR_ANT_PROPERTIES,properties);
 		wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND,false);
 		wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE,true);
 		wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE,true);
 		
 		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, "org.eclipse.ant.ui.AntClasspathProvider");  //$NON-NLS-1$
 		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME,getServerRuntime().getVMInstall().getName());
 		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE,getServerRuntime().getVMInstall().getVMInstallType().getId());
 		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "org.eclipse.ant.internal.ui.antsupport.InternalAntRunner"); //$NON-NLS-1$
 		wc.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, IAntUIConstants.REMOTE_ANT_PROCESS_FACTORY_ID);
 		
 		setupAntLaunchConfiguration(wc);
 		
 		
 		ILaunchConfiguration launchConfig = wc.doSave();
         launchConfig.launch("run",monitor); //$NON-NLS-1$
 	}
 
 
 
   /**
    * Hook method for subclasses.
    * 
    * @param wc
    */
     protected void setupAntLaunchConfiguration(ILaunchConfigurationWorkingCopy wc) {		
 	}
 
 	/* (non-Javadoc)
      * @see org.eclipse.jst.server.generic.internal.core.GenericPublisher#unpublish(org.eclipse.wst.server.core.IModule, org.eclipse.core.runtime.IProgressMonitor)
      */
     public IStatus[] unpublish(IProgressMonitor monitor) {
 
     	if(getModule().length>1)// only respond to root module calls. 
 			return null;
     	try {
         	 File file = computeBuildFile();
             runAnt(file.toString(),getUnpublishTargetsForModule(),getPublishProperties(),monitor);
         } catch (CoreException e) {
             IStatus s = new Status(IStatus.ERROR,CorePlugin.PLUGIN_ID,0,GenericServerCoreMessages.errorRemoveModuleAntpublisher,e);
             return new IStatus[] {s};
         }
         return null;
     }
 }

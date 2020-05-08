 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.operation;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.jboss.tools.common.meta.action.XActionInvoker;
 import org.jboss.tools.common.model.ServiceDialog;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelConstants;
 import org.jboss.tools.common.model.XModelFactory;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.filesystems.FileSystemsHelper;
 import org.jboss.tools.common.model.filesystems.impl.FileSystemImpl;
 import org.jboss.tools.common.model.options.PreferenceModelUtilities;
 import org.jboss.tools.common.model.project.IModelNature;
 import org.jboss.tools.common.model.project.ProjectHome;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.jst.web.context.RegisterServerContext;
 import org.jboss.tools.jst.web.model.helpers.WebAppHelper;
 import org.jboss.tools.jst.web.project.helpers.IWebProjectTemplate;
 import org.jboss.tools.jst.web.project.helpers.NewWebProjectContext;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 
 public abstract class WebProjectCreationOperation extends WebNatureOperation {
 	protected IWebProjectTemplate template = createTemplate();
 	protected XModel templateModel = null;
 
 	public WebProjectCreationOperation(IProject project, IPath projectLocation, RegisterServerContext registry, Properties properties)	{
 		super(project, projectLocation, registry, properties);
 	}
 
 	public WebProjectCreationOperation(NewWebProjectContext context) {
 		this(context.getProject(), context.getLocationPath(), context.getRegisterServerContext(), context.getActionProperties());
 		setProperty(WebNatureOperation.PROJECT_NAME_ID, context.getActionProperties().getProperty(NewWebProjectContext.ATTR_NAME));
 		setProperty(WebNatureOperation.PROJECT_LOCATION_ID, context.getActionProperties().getProperty(NewWebProjectContext.ATTR_LOCATION));
 		setProperty(WebNatureOperation.USE_DEFAULT_LOCATION_ID, context.getActionProperties().getProperty(NewWebProjectContext.ATTR_USE_DEFAULT_LOCATION));
 		setProperty(WebNatureOperation.TEMPLATE_ID, context.getActionProperties().getProperty(NewWebProjectContext.ATTR_TEMPLATE));
 		setProperty(WebNatureOperation.TEMPLATE_VERSION_ID, context.getActionProperties().getProperty(NewWebProjectContext.ATTR_VERSION));
 		setProperty(WebNatureOperation.SERVLET_VERSION_ID, context.getActionProperties().getProperty(NewWebProjectContext.ATTR_SERVLET_VERSION));
 		setProperty(WebNatureOperation.REGISTER_WEB_CONTEXT_ID, context.getActionProperties().getProperty(NewWebProjectContext.ATTR_REGISTER_WEB_CONTEXT));
 		setProperty(WebNatureOperation.RUNTIME_NAME, context.getRegisterServerContext().getRuntimeName());
 		setProperty(WebNatureOperation.JAVA_SOURCES_LOCATION_ID, getJavaSources());
		String contextRoot = context.getRegisterServerContext().getApplicationName();
		if(contextRoot != null && contextRoot.length() > 0) {
			setProperty("WebNatureOperation.CONTEXT_ROOT", contextRoot);
		}
 	}
 	
 	protected abstract IWebProjectTemplate createTemplate();
 	protected abstract void copyTemplate() throws Exception;
 
 	protected void preCreateWebNature() throws CoreException {
 		Properties properties = this.getWizardPropertiesAsIs();
 		properties.setProperty(NewWebProjectContext.ATTR_LOCATION, getProject().getLocation().toString());
 		try	{
 			createTemplateModel();
 			copyTemplate();
 		} catch (Exception e) {
 			WebUiPlugin.getPluginLog().logError(e);
 			String message = e.getMessage();
 			if(message == null || message.length() == 0) message = e.getClass().getName(); 
 			throw new CoreException(new Status(IStatus.ERROR, ModelUIPlugin.PLUGIN_ID, 1, message, e));
 		}
 		copyProjectFile(properties);
 	}
 
 	protected void createWebNature() throws CoreException {
 		Properties properties = this.getWizardPropertiesAsIs();
 		properties.setProperty(NewWebProjectContext.ATTR_LOCATION, getProject().getLocation().toString());
 //		try	{
 //			createTemplateModel();
 //			copyTemplate();
 //		} catch (Exception e) {
 //			WebUiPlugin.getPluginLog().logError(e);
 //			String message = e.getMessage();
 //			if(message == null || message.length() == 0) message = e.getClass().getName(); 
 //			throw new CoreException(new Status(IStatus.ERROR, ModelUIPlugin.PLUGIN_ID, 1, message, e));
 //		}
 //		copyProjectFile(properties);
 		EclipseResourceUtil.addNatureToProject(getProject(), getNatureID());
 		IModelNature strutsProject = (IModelNature)getProject().getNature(getNatureID());
 		model = strutsProject.getModel();
 		XModelObject fso = FileSystemsHelper.getFileSystems(model);
 		properties.setProperty("skipWizard", "yes");  //$NON-NLS-1$//$NON-NLS-2$
 		properties.setProperty("name", getProject().getName()); //$NON-NLS-1$
 		XActionInvoker.invoke("CreateStrutsProject", fso, properties); //$NON-NLS-1$
 		
 		XModelObject web = model.getByPath("Web"); //$NON-NLS-1$
 		if (web != null && properties.containsKey(NewWebProjectContext.ATTR_SERVLET_VERSION))
 			model.changeObjectAttribute(web, NewWebProjectContext.ATTR_SERVLET_VERSION, properties.getProperty("servlet version")); //$NON-NLS-1$
 		XModelObject webxml = WebAppHelper.getWebApp(model);
 		if(webxml != null) {
 			model.changeObjectAttribute(webxml, "display-name", "" + getProject().getName()); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 	
 	protected void postCreateWebNature() {
 		
 	}
 	
 	protected File projectFile = null;
 	
 	protected void copyProjectFile(Properties p) {
 		String templateFolder = template.getProjectTemplatesLocation(
 				getProperty(TEMPLATE_VERSION_ID)) + "/" +  //$NON-NLS-1$
 				getProperty(TEMPLATE_ID) + "/"; //$NON-NLS-1$
 		File sf =  new File(templateFolder + IModelNature.PROJECT_FILE);
 		String tf = p.getProperty(NewWebProjectContext.ATTR_LOCATION) + "/" + IModelNature.PROJECT_FILE; //$NON-NLS-1$
 		if(sf.exists())	{
 			FileUtil.copyFile(sf, new File(tf), true);
 			projectFile = new File(tf);
 		} else {
 			throw new RuntimeException("Project template must have model configuration file"); //$NON-NLS-1$
 		}
 	}
 
 	protected void createTemplateModel() {
 		if(templateModel != null) return;
 		String templateLocation = getTemplateLocation();
 		Properties p = new Properties();
 		p.putAll(System.getProperties());
 		String workspace = new ProjectHome().getLocation(templateLocation);
 		p.setProperty(XModelConstants.WORKSPACE, workspace);
 		p.setProperty(XModelConstants.WORKSPACE_OLD, workspace);
 		p.setProperty(IModelNature.ECLIPSE_PROJECT, templateLocation);
 		templateModel = XModelFactory.getModel(p);
 	}
 	
 	protected String getTemplateLocation() {
 		String fileName = template.getProjectTemplatesLocation(
 				getProperty(TEMPLATE_VERSION_ID)) + "/" + //$NON-NLS-1$
 				getProperty(TEMPLATE_ID) + "/"; //$NON-NLS-1$
 		try {
 			return new File(fileName).getCanonicalPath();
 		} catch (IOException e) {
 			WebUiPlugin.getPluginLog().logError("Cannot find folder '" + fileName + "'", null); //$NON-NLS-1$ //$NON-NLS-2$
 			return fileName;
 		}
 	}
 
 	/**
 	 * @deprecated use bundle via Messages.getString()
 	 */
 	public static final String WARNING_MESSAGE = "COD_MESSAGE"; //$NON-NLS-1$
 	/**
 	 * @deprecated use bundle via Messages.getString()
 	 */
 	public static final String WARNING_TITLE   = "COD_TITLE"; //$NON-NLS-1$
 	/**
 	 * @deprecated use bundle via Messages.getString()
 	 */
 	public static final String BTN_CANCEL      = "BTN_CANCEL"; //$NON-NLS-1$
 	/**
 	 * @deprecated use bundle via Messages.getString()
 	 */
 	public static final String BTN_OK          = "BTN_OK"; //$NON-NLS-1$
 	
 	protected boolean checkOverwrite() {
 		String location = getProperty(PROJECT_LOCATION_ID);
 		
 		if(location == null) return true;
 		
 		File targetFile = new File(location);
 		File[] cs = (targetFile.exists()) ? targetFile.listFiles() : null;
 		
 		if(cs != null && cs.length > 0) {
 			ServiceDialog dlg = PreferenceModelUtilities.getPreferenceModel().getService();
 			
 			
 			String message = MessageFormat.format(
 			        Messages.COD_MESSAGE,new Object[]{location} 
 				);			
 			
 			int selAction = dlg.showDialog(
 				Messages.COD_TITLE,  
 				message, 
 				new String[]{Messages.BTN_OK,Messages.BTN_CANCEL},   
 				null, 
 				ServiceDialog.WARNING
 			);
 
 			if(selAction != 0) return false; 
 		}
 		
 		return true;
 	}
 	
 	private String[] getJavaSources() {
 		try {
 			createTemplateModel();
 		} catch (Exception e) {
 			WebUiPlugin.getPluginLog().logError(e);
 		}
 		if(templateModel != null) {
 			XModelObject o = FileSystemsHelper.getFileSystem(templateModel, "src"); //$NON-NLS-1$
 			if(o instanceof FileSystemImpl) {
 				String s = ((FileSystemImpl)o).getAbsoluteLocation();
 				File f = new File(s);
 				if(f.exists()) {
 					return new String[]{f.getName()};
 				}
 			}
 		}
 		return new String[0];
 	}
 
 }

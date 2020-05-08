 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.tomcat.core.internal.command;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jst.server.tomcat.core.internal.*;
 import org.eclipse.osgi.util.NLS;
 
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.core.TaskModel;
 import org.eclipse.wst.server.core.model.PublishOperation;
 /**
  * Task to fix a context root on a web module.
  */
 public class FixModuleContextRootTask extends PublishOperation {
 	protected int index;
 	protected WebModule module;
 	protected IModule webModule;
 	protected String contextRoot;
 
 	/**
 	 * FixModuleContextRootTask constructor.
 	 * 
 	 * @param webModule
 	 * @param index
 	 * @param contextRoot
 	 */
 	public FixModuleContextRootTask(IModule webModule, int index, String contextRoot) {
 		super(NLS.bind(Messages.fixModuleContextRoot, webModule.getName()), Messages.fixModuleContextRootDescription);
 		this.webModule = webModule;
 		this.index = index;
 		this.contextRoot = contextRoot;
 	}
 
 	/**
 	 * Execute the command.
 	 * 
 	 * @param monitor a progress monitor
 	 * @param info
 	 * @throws CoreException
 	 */
 	public void execute(IProgressMonitor monitor, IAdaptable info) throws CoreException {
 		IServerWorkingCopy wc = (IServerWorkingCopy) getTaskModel().getObject(TaskModel.TASK_SERVER);
 		TomcatServer server = (TomcatServer) wc.loadAdapter(TomcatServer.class, monitor);
 		TomcatConfiguration configuration = server.getTomcatConfiguration();
 		if (configuration.getWebModules().size() <= index)
 			return;
 		module = (WebModule) configuration.getWebModules().get(index);
		if (contextRoot != null && !contextRoot.startsWith("/") && contextRoot.length() > 0)
 			contextRoot = "/" + contextRoot;
 		configuration.modifyWebModule(index, module.getDocumentBase(), contextRoot, module.isReloadable());
 		wc.save(true, monitor);
 	}
 
 	public int getKind() {
 		return PREFERRED;
 	}
 
 	public int getOrder() {
 		return 0;
 	}
 }

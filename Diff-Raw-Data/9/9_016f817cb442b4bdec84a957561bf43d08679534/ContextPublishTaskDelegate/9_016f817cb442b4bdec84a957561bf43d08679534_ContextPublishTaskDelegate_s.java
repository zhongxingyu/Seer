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
 package org.eclipse.jst.server.tomcat.core.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.jst.server.tomcat.core.internal.command.FixModuleContextRootTask;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.model.PublishOperation;
 import org.eclipse.wst.server.core.model.PublishTaskDelegate;
 
 public class ContextPublishTaskDelegate extends PublishTaskDelegate {
 	public PublishOperation[] getTasks(IServer server, List modules) {
 		if (modules == null)
 			return null;
 	
 		TomcatServer tomcatServer = (TomcatServer) server.loadAdapter(TomcatServer.class, null);
 		TomcatConfiguration configuration = null;
 		try {
 			configuration = tomcatServer.getTomcatConfiguration();
 		} catch (Exception e) {
 			return null;
 		}
 		
 		List tasks = new ArrayList();
 		int size = modules.size();
 		for (int i = 0; i < size; i++) {
 			IModule[] module = (IModule[]) modules.get(i);
 			IModule m = module[module.length - 1];
 			IWebModule webModule = (IWebModule) m.loadAdapter(IWebModule.class, null);
 			WebModule webModule2 = configuration.getWebModule(m);
 			if (webModule != null && webModule2 != null) {
 				String contextRoot = webModule.getContextRoot();
				if (contextRoot != null && !contextRoot.startsWith("/"))
 					contextRoot = "/" + contextRoot;
 				if (!contextRoot.equals(webModule2.getPath())) {
 					int index = configuration.getWebModules().indexOf(webModule2);
 					FixModuleContextRootTask task = new FixModuleContextRootTask(m, index, webModule.getContextRoot());
 					tasks.add(task);
 				}
 			}
 		}
 		
 		return (PublishOperation[]) tasks.toArray(new PublishOperation[tasks.size()]);
 	}
 }

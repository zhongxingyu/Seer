 /*******************************************************************************
  * Copyright (c) May 24, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.commands;
 
 import java.io.InputStream;
 import java.text.MessageFormat;
 
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.sdklib.repository.site.Application;
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 
 /**
  * Deploys package to specified target.
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class DeployApplicationCommand extends AbstractDeploymentCommand {
 
 	private static final String BASE_PATH = "b";
 	private static final String VHOST = "h";
 	private static final String NAME = "n";
 
 	@Option(opt = BASE_PATH, required = false, description = "Base path of this application (relative to hostname), if not provided project name is considered", argName = "base-path")
 	public String getBasePath() {
 		return getValue(BASE_PATH);
 	}
 
 	@Option(opt = NAME, required = false, description = "Application name", argName = "name")
 	public String getName() {
 		return getValue(NAME);
 	}
 
 	@Option(opt = VHOST, required = false, description = "Specify the virtual host which should be used. If a virtual host with the specified name does not exist, it will be created. By default if virtual host is not specified then the default one will be used (marked as <default-server> in the application url)", argName = "vHost")
 	public String getVhost() {
 		return getValue(VHOST);
 	}
 
 	public boolean isVhost() {
 		return getVhost() != null;
 	}
 
 	@Override
 	protected ApplicationInfo doRepositoryOperation(InputStream stream,
 			Application app) {
 		return getApplication().deploy(stream, app, getBasePath(),
 				getTargetId(), getParams(), getName(), isIgnoreFailures(),
 				getVhost(), !isVhost());
 	}
 
 	@Override
 	protected ApplicationInfo doGitOperation() {
 		return getApplication().deploy(getPath(), getBasePath(), getTargetId(),
 				getParams(), getName(), isIgnoreFailures(), getVhost(),
 				!isVhost());
 	}
 
 	@Override
 	protected ApplicationInfo doOperation() {
 		return getApplication().deploy(getPath(), getBasePath(), getTargetId(),
 				getParams(), getName(), isIgnoreFailures(), getVhost(),
 				!isVhost());
 	}
 
 	@Override
 	protected boolean resolveResult(ApplicationInfo info) {
 		if (info != null) {
 			getLogger()
					.info(MessageFormat.format(
							"Application {0} (id {1}) is deployed to {2}",
							info.getAppName(), info.getId(), info.getBaseUrl()));
 			return true;
 		}
 		return false;
 	}
 
 }

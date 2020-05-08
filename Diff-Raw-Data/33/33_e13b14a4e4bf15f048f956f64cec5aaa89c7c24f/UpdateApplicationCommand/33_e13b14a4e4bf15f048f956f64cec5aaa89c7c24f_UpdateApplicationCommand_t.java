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
 public class UpdateApplicationCommand extends AbstractDeploymentCommand {
 
 	private static final String APPID = "a";
 
 	@Option(opt = APPID, required = true, description = "The application id", argName="app-id")
 	public String getApplicationId() {
 		return getValue(APPID);
 	}
 
 	@Override
 	protected ApplicationInfo doGitOperation() {
 		return getApplication().update(getPath(), getTargetId(),
 				getApplicationId(), getParams(), isIgnoreFailures());
 	}
 
 	@Override
 	protected ApplicationInfo doOperation() {
 		return getApplication().update(getPath(), getTargetId(),
 				getApplicationId(), getParams(), isIgnoreFailures());
 	}
 
 	@Override
 	protected ApplicationInfo doRepositoryOperation(InputStream stream,
 			Application app) {
 		return getApplication().update(stream, app, getTargetId(),
 				getApplicationId(), getParams(), isIgnoreFailures());
 	}
 
 	@Override
 	protected boolean resolveResult(ApplicationInfo info) {
 		if (info != null) {
 			getLogger()
					.info(MessageFormat
							.format("Application {0} (id {1}) is updated to {2} (target id {3})",
									info.getAppName(), info.getId(),
									info.getBaseUrl(), getTargetId()));
 			return true;
 		}
 		return false;
 	}
 
 }

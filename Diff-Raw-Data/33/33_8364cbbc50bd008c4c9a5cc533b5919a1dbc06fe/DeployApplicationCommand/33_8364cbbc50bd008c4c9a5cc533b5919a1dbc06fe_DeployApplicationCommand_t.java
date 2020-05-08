 /*******************************************************************************
  * Copyright (c) May 24, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.commands;
 
 import org.zend.sdkcli.internal.options.Option;
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 
 /**
  * Deploys package to specified target.
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class DeployApplicationCommand extends ApplicationAwareCommand {
 
 	private static final String PATH = "p";
 	private static final String BASE_URL = "b";
 	private static final String TARGET = "t";
 	private static final String PARAMS = "m";
 	private static final String NAME = "n";
 	private static final String IGNORE_FAILURES = "f";
 	private static final String CREATE_VHOST = "v";
 	private static final String DEFAULT_SERVER = "d";
 
 	@Option(opt = PATH, required = true, description = "The path to the project or application package", argName = "path")
 	public String getPath() {
 		return getValue(PATH);
 	}
 
 	@Option(opt = BASE_URL, required = true, description = "The base URL of the application", argName = "url")
 	public String getBaseUrl() {
 		return getValue(BASE_URL);
 	}
 
 	@Option(opt = TARGET, required = true, description = "The target id", argName = "id")
 	public String getTargetId() {
 		return getValue(TARGET);
 	}
 
 	@Option(opt = PARAMS, required = false, description = "The path to parameters properties file", argName = "parameters")
 	public String getParams() {
 		return getValue(PARAMS);
 	}
 
 	@Option(opt = NAME, required = false, description = "The application name", argName = "name")
 	public String getName() {
 		return getValue(NAME);
 	}
 
 	@Option(opt = IGNORE_FAILURES, required = false, description = "Ignore failures")
 	public boolean isIgnoreFailures() {
 		return hasOption(IGNORE_FAILURES);
 	}
 
 	@Option(opt = CREATE_VHOST, required = false, description = "Create vhost")
 	public boolean isCreateVhost() {
 		return hasOption(CREATE_VHOST);
 	}
 
 	@Option(opt = DEFAULT_SERVER, required = false, description = "Use default server")
 	public boolean isDefaultServer() {
 		return hasOption(DEFAULT_SERVER);
 	}
 
 	@Override
 	public boolean doExecute() {
		ApplicationInfo info = getApplication().deploy(getPath(), getBaseUrl(),
				getTargetId(), getParams(), getName(), isIgnoreFailures(),
				isCreateVhost(), isDefaultServer());
 		if (info == null) {
 			return false;
 		}
 		return true;
 	}
 
 }

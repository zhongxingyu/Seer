 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.launching;
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.environment.IDeployment;
 import org.eclipse.dltk.internal.launching.AbstractInterpreterInstallType;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.launching.RubyLaunchingPlugin;
 
 public class RubyGenericInstallType extends AbstractInterpreterInstallType {
 
 	private static final String INSTALL_TYPE_NAME = "Generic Ruby"; //$NON-NLS-1$
 
	private static final String[] INTERPRETER_NAMES = {
			"ruby", "rubyw", "ruby18", "ruby19" }; //$NON-NLS-1$ //$NON-NLS-2$
 
 	public String getNatureId() {
 		return RubyNature.NATURE_ID;
 	}
 
 	public String getName() {
 		return INSTALL_TYPE_NAME;
 	}
 
 	protected String getPluginId() {
 		return RubyLaunchingPlugin.PLUGIN_ID;
 	}
 
 	protected String[] getPossibleInterpreterNames() {
 		return INTERPRETER_NAMES;
 	}
 
 	protected IInterpreterInstall doCreateInterpreterInstall(String id) {
 		return new RubyGenericInstall(this, id);
 	}
 
 	protected IPath createPathFile(IDeployment deployment) throws IOException {
		return deployment.add(RubyLaunchingPlugin.getDefault().getBundle(),
				"scripts/path.rb"); //$NON-NLS-1$
 	}
 
 	protected String getBuildPathDelimeter() {
 		return ";:"; //$NON-NLS-1$
 	}
 
 	protected ILog getLog() {
 		return RubyLaunchingPlugin.getDefault().getLog();
 	}
 }

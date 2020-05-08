 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.launching;
 
 import java.io.IOException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.dltk.core.environment.IDeployment;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.launching.AbstractScriptLaunchConfigurationDelegate;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.ruby.core.RubyNature;
 
 public class RubyLaunchConfigurationDelegate extends
 		AbstractScriptLaunchConfigurationDelegate {
 
 	public String getLanguageId() {
 		return RubyNature.NATURE_ID;
 	}
 	protected InterpreterConfig createInterpreterConfig(
 			ILaunchConfiguration configuration, ILaunch launch)
 	throws CoreException {
 		
 		final InterpreterConfig config = super.createInterpreterConfig(
 				configuration, launch);
 		if (config != null) {
 			addEncodingInterpreterArg(config, configuration);
 			addIncludePathInterpreterArg(config, configuration);
			addStreamSync(config, configuration);
 		}
 		
 		return config;
 	}
 	protected String getCharset(ILaunchConfiguration configuration)
 			throws CoreException {
 		IProject project = getScriptProject(configuration).getProject();
 		IResource resource = project
 				.findMember(getMainScriptName(configuration));
 
 		if (resource instanceof IFile) {
 			IFile file = (IFile) resource;
 			return file.getCharset();
 		}
 
 		return null;
 	}
 
 	protected String getCharsetInterpreterFlag(String charset) {
 		if (charset.equals("UTF-8")) { //$NON-NLS-1$
 			return "-KU"; //$NON-NLS-1$
 		} else if (charset.equals("EUC")) { //$NON-NLS-1$
 			return "-KE"; //$NON-NLS-1$
 		} else if (charset.equals("SJIS")) { //$NON-NLS-1$
 			return "-KS"; //$NON-NLS-1$
 		}
 
 		return "-KA"; //$NON-NLS-1$
 	}
 
 	protected void addEncodingInterpreterArg(InterpreterConfig config,
 			ILaunchConfiguration configuration) throws CoreException {
 		if (!config.hasMatchedInterpreterArg("-K.*")) { //$NON-NLS-1$
 			String charset = getCharset(configuration);
 			if (charset != null) {
 				config.addInterpreterArg(getCharsetInterpreterFlag(charset));
 			}
 		}
 	}
 
 	protected void addIncludePathInterpreterArg(InterpreterConfig config,
 			ILaunchConfiguration configuration) throws CoreException {
 		IPath[] paths = createBuildPath(configuration);
 
 		IEnvironment env = config.getEnvironment();
 		char separator = env.getPathsSeparatorChar();
 
 		final StringBuffer sb = new StringBuffer();
 		if (paths.length > 0) {
 			sb.append("-I"); //$NON-NLS-1$
 			sb.append(env.convertPathToString(paths[0]));
 			for (int i = 1; i < paths.length; ++i) {
 				sb.append(separator);
 				sb.append(env.convertPathToString(paths[i]));
 			}
 		}
 
 		config.addInterpreterArg(sb.toString());
 	}
 
 	protected void addStreamSync(InterpreterConfig config,
			ILaunchConfiguration configuration) {
 		try {
 			IDeployment deployment = config.getExecutionEnvironment().createDeployment();
 			final IPath path = deployment.add(RubyLaunchingPlugin
 					.getDefault().getBundle(), "scripts/sync.rb"); //$NON-NLS-1$
 			config.addInterpreterArg("-r"); //$NON-NLS-1$
 			config.addInterpreterArg(deployment.getFile(path).toString());
 		} catch (IOException e) {
 			RubyLaunchingPlugin.log(e);
 		}
 	}
 
 
 }

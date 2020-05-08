 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.launching;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.dltk.launching.AbstractInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.IInterpreterRunner;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.ScriptLaunchUtil;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.launching.RubyLaunchingPlugin;
 import org.eclipse.dltk.utils.DeployHelper;
 
 public class RubyGenericInstall extends AbstractInterpreterInstall {
 
 	public class BuiltinsHelper {
 		private static final String PREFIX = "#### DLTK RUBY BUILTINS ####";
 
 		private Map sources;
 
 		private String[] generateLines() throws IOException, CoreException {
 			final File builder = DeployHelper.deploy(
 					RubyLaunchingPlugin.getDefault(), "scripts/builtin.rb")
 					.toFile();
 
 			final List lines = new ArrayList();
 
 			Process process = ScriptLaunchUtil.runScriptWithInterpreter(
 					RubyGenericInstall.this.getInstallLocation()
 							.getAbsolutePath(), new InterpreterConfig(builder));
 
 			BufferedReader input = null;
 			try {
 				input = new BufferedReader(new InputStreamReader(process
 						.getInputStream()));
 
 				String line = null;
 				while ((line = input.readLine()) != null) {
 					lines.add(line);
 				}
 
 				return (String[]) lines.toArray(new String[lines.size()]);
 			} finally {
 				if (input != null) {
 					input.close();
 				}
 			}
 		}
 
 		private void parseLines(String[] lines) {
 			String fileName = null;
 			StringBuffer sb = new StringBuffer();
 			for (int i = 0; i < lines.length; ++i) {
 				String line = lines[i];
 
 				int index = line.indexOf(PREFIX);
 				if (index != -1) {
 					if (fileName != null) {
						sources.put(fileName, sb.toString());
 						sb.setLength(0);
 					}
 
 					fileName = line.substring(index + PREFIX.length());
 
 				} else {
 					sb.append(line);
 					sb.append("\n");
 				}
 			}
 		}
 
 		public Map getSources() {
 			if (sources == null) {
 				sources = new HashMap();
 
 				try {
 					String[] lines = generateLines();
 					if (lines != null) {
 						parseLines(lines);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 			}
 
 			return sources;
 		}
 	}
 
 	private BuiltinsHelper helper = new BuiltinsHelper();
 
 	public RubyGenericInstall(IInterpreterInstallType type, String id) {
 		super(type, id);
 	}
 
 	public IInterpreterRunner getInterpreterRunner(String mode) {
 		final IInterpreterRunner runner = super.getInterpreterRunner(mode);
 
 		if (runner != null) {
 			return runner;
 		}
 
 		if (mode.equals(ILaunchManager.RUN_MODE)) {
 			return new RubyInterpreterRunner(this);
 		}
 
 		return null;
 	}
 
 	public String getNatureId() {
 		return RubyNature.NATURE_ID;
 	}
 
 	// Builtins
 	public String getBuiltinModuleContent(String name) {
 		final Map sources = helper.getSources();
 		return (String) sources.get(name);
 	}
 
 	public String[] getBuiltinModules() {
 		final Map sources = helper.getSources();
 		return (String[]) sources.keySet().toArray(new String[sources.size()]);
 	}
 }

 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.launching;
 
 import org.eclipse.dltk.launching.AbstractInterpreterRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.ruby.launching.RubyLaunchConfigurationConstants;
 
 public class RubyInterpreterRunner extends AbstractInterpreterRunner {
	public RubyInterpreterRunner(IInterpreterInstall install) {
		super(install);
 	}
 
 	protected String getProcessType() {
 		return RubyLaunchConfigurationConstants.ID_RUBY_PROCESS_TYPE;
 	}
 }

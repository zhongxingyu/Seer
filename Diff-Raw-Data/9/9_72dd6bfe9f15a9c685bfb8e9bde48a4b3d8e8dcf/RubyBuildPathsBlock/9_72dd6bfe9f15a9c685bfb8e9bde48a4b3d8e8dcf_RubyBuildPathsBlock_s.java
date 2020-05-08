 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.preferences;
 
 import org.eclipse.dltk.ruby.internal.ui.RubyUI;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 import org.eclipse.dltk.ui.wizards.BuildpathsBlock;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 
 public class RubyBuildPathsBlock extends BuildpathsBlock {
 	public RubyBuildPathsBlock(IRunnableContext runnableContext,
 			IStatusChangeListener context, int pageToShow, boolean useNewPage,
 			IWorkbenchPreferenceContainer pageContainer) {
 		super(runnableContext, context, pageToShow, useNewPage, pageContainer);
 	}
 
 	protected IPreferenceStore getPreferenceStore() {
 		return RubyUI.getDefault().getPreferenceStore();
 	}
 
 	protected boolean supportZips() {
		return true;
 	}
 }

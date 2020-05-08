 /***********************************************************************
 * Copyright (c) 2011 by SAP AG, Walldorf. 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SAP AG - initial API and implementation
  ***********************************************************************/
 package org.eclipse.jst.jee.ui.internal.navigator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jst.j2ee.navigator.internal.LoadingDDUIJob;
 
 public class LoadingJeeDDJob extends Job {
	
	private Viewer viewer;
	private JEE5ContentProvider rootProvider;
	private IProject project;
	private LoadingGroupProvider provider;
 
 	public LoadingJeeDDJob(Viewer viewer2, LoadingGroupProvider provider, IProject project, JEE5ContentProvider rootProvider) {
 		super(provider.getText());
 		this.viewer = viewer2;
 		this.provider = provider;
 		this.project = project;
 		this.rootProvider = rootProvider;
 	}
 
 	@Override
 	protected IStatus run(IProgressMonitor arg0) {
 		LoadingDDUIJob updateUIJob = new LoadingDDUIJob((StructuredViewer) viewer, provider.getPlaceHolder());
 		updateUIJob.schedule();
 
 		List children = new ArrayList();
 		AbstractGroupProvider rootObject = null;
		
 		try {
 			rootObject = (rootProvider != null) ? rootProvider.getNewContentProviderInstance(project) : null;
 			rootProvider.registerProvider(project, rootObject);
 			if (rootObject != null) {
 					children.add(rootObject);
			}		
		} finally { 
 			/* dispose of the place holder, causes the termination of the animation job */
			provider.dispose(); 
 			new ClearJeePlaceHolderJob((AbstractTreeViewer) viewer, provider, project, children.toArray()).schedule();
 		}
		
		 
 		return Status.OK_STATUS;
 	}
 
 }

 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.ui.project.facet.appclient;
 
 import java.net.URL;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jst.j2ee.applicationclient.internal.creation.AppClientFacetProjectCreationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProjectTemplate;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.web.ui.internal.wizards.NewProjectDataModelFacetWizard;
 import org.osgi.framework.Bundle;
 
 public class AppClientProjectWizard extends NewProjectDataModelFacetWizard {
 
 	public AppClientProjectWizard(IDataModel model) {
 		super(model);
 		setWindowTitle(J2EEUIMessages.getResourceString(J2EEUIMessages.APP_CLIENT_PROJECT_WIZ_TITLE));
 	}
 
 	public AppClientProjectWizard() {
 		super();
 		setWindowTitle(J2EEUIMessages.getResourceString(J2EEUIMessages.APP_CLIENT_PROJECT_WIZ_TITLE));
 	}
 
 	protected IDataModel createDataModel() {
 		return DataModelFactory.createDataModel(new AppClientFacetProjectCreationDataModelProvider());
 	}
 
 	protected ImageDescriptor getDefaultPageImageDescriptor() {
 		final Bundle bundle = Platform.getBundle(J2EEUIPlugin.PLUGIN_ID);
		final URL url = bundle.getEntry("icons/full/wizban/appclient_wiz.gif"); //$NON-NLS-1$
 		return ImageDescriptor.createFromURL(url);
 	}
 
 	protected IFacetedProjectTemplate getTemplate() {
 		return ProjectFacetsManager.getTemplate("template.jst.appclient"); //$NON-NLS-1$
 	}
 
 	protected IWizardPage createFirstPage() {
 		return new AppClientProjectFirstPage(model, "first.page"); //$NON-NLS-1$
 	}
 
 }

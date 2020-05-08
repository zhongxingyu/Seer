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
 package org.eclipse.jst.j2ee.jca.ui.internal.wizard;
 
 import java.net.URL;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.jst.j2ee.jca.project.facet.ConnectorFacetProjectCreationDataModelProvider;
 import org.eclipse.jst.j2ee.jca.ui.internal.util.JCAUIMessages;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProjectTemplate;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.web.ui.internal.wizards.NewProjectDataModelFacetWizard;
 import org.osgi.framework.Bundle;
 
 public class ConnectorProjectWizard extends NewProjectDataModelFacetWizard {
 
 	public ConnectorProjectWizard(IDataModel model) {
 		super(model);
 		setWindowTitle(JCAUIMessages.JCA_MODULE_WIZ_TITLE);
 	}
 	
 	public ConnectorProjectWizard() {
 		super();
 		setWindowTitle(JCAUIMessages.JCA_MODULE_WIZ_TITLE);
 	}
 
 	protected IDataModel createDataModel() {
 		return DataModelFactory.createDataModel(new ConnectorFacetProjectCreationDataModelProvider());
 	}
 
 	protected ImageDescriptor getDefaultPageImageDescriptor() {
 		final Bundle bundle = Platform.getBundle(J2EEUIPlugin.PLUGIN_ID);
		final URL url = bundle.getEntry("icons/full/wizban/connector_wiz.gif"); //$NON-NLS-1$
 		return ImageDescriptor.createFromURL(url);
 	}
 
 	protected IFacetedProjectTemplate getTemplate() {
 		return ProjectFacetsManager.getTemplate("template.jst.connector"); //$NON-NLS-1$
 	}
 
 	protected IWizardPage createFirstPage() {
 		return new ConnectorProjectFirstPage(model, "first.page"); //$NON-NLS-1$
 	}
 }

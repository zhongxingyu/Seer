 /*
   GRANITE DATA SERVICES
   Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.
 
   This file is part of Granite Data Services.
 
   Granite Data Services is free software; you can redistribute it and/or modify
   it under the terms of the GNU Library General Public License as published by
   the Free Software Foundation; either version 2 of the License, or (at your
   option) any later version.
 
   Granite Data Services is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
   for more details.
 
   You should have received a copy of the GNU Library General Public License
   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.granite.wizard;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.LocationEvent;
 import org.eclipse.swt.browser.LocationListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.browser.IWebBrowser;
 import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
 import org.granite.generator.ScriptingEngine;
 import org.granite.generator.ScriptingEngineFactory;
 import org.granite.wizard.controllers.AbstractTemplateController;
 import org.granite.wizard.util.PropertiesUtil;
 
 /**
  * @author Franck WOLFF
  */
 public class DynamicProjectWizardPageOne extends WizardPage {
 
 	private static final String TEMPLATES = "resources/templates";
 	private static final String TEMPLATES_PROPERTIES = "templates.properties";
 	private static final String GLOBAL_GROOVY = "global.groovy";
 	
 	private IConfigurationElement configurationElement = null;
 	
 	private ScriptingEngine engine = null;
 	private List<ProjectTemplate> templates = null;	
 	private Properties properties = null;
 	
 	private AbstractTemplateController controller = null;
 	
 	public DynamicProjectWizardPageOne(IConfigurationElement configurationElement) {
 		super("GraniteDS Project Templates", "Select a GraniteDS Project Template", null);
 
 		this.configurationElement = configurationElement;
 		
 		initialize();
 	}
 
 	public IConfigurationElement getConfigurationElement() {
 		return configurationElement;
 	}
 
 	@Override
 	public boolean canFlipToNextPage() {
 		return controller != null;
 	}
 
 	@Override
 	public IWizardPage getNextPage() {
 		if (controller == null)
 			return null;
 		return controller.getNextPage();
 	}
 
 	public Properties getProperties() {
 		return properties;
 	}
 
 	public ScriptingEngine getEngine() {
 		return engine;
 	}
 
 	private void initialize() {
 
 		// Find templates directory.
 		File templatesDirectory = null;
 		try {
 			URL url = getClass().getClassLoader().getResource(TEMPLATES);
 			url = FileLocator.resolve(url);
			templatesDirectory = new File(url.toURI());
 		} catch (Exception e) {
 			throw new WizardException("Cannot resolve directory: " + TEMPLATES, e);
 		}
 		if (!templatesDirectory.isDirectory())
 			throw new WizardException("Not a directory: " + TEMPLATES, null);
 		
 		// Load templates.properties file.
 		try {
 			properties = PropertiesUtil.loadProperties(templatesDirectory, TEMPLATES_PROPERTIES);
 		}
 		catch (IOException e) {
 			throw new WizardException("Could not load general template properties: ", e);
 		}
 		
 		// Create a scripting engine and load prolog.groovy.
 		engine = ScriptingEngineFactory.createInstance();
 		try {
 			engine.load(new File(templatesDirectory, GLOBAL_GROOVY), null);
 		}
 		catch (IOException e) {
 			throw new WizardException("Could not load prolog script: ", e);
 		}
 		
 		templates = new ArrayList<ProjectTemplate>();
 		File[] templatesSubDirectories = templatesDirectory.listFiles();
 		for (File template : templatesSubDirectories) {
 			if (template.isDirectory() && !template.isHidden()) {
 				try {
 					templates.add(new ProjectTemplate(template, properties, engine));
 				}
 				catch (Exception e) {
 					Activator.log("Template loading error", e);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 		initializeDialogUnits(parent);
 		
 		Composite composite= new Composite(parent, SWT.NONE);
 		composite.setFont(parent.getFont());
         GridLayout layout = new GridLayout();
         layout.numColumns = 2;
         layout.makeColumnsEqualWidth = false;
         layout.horizontalSpacing = 10;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		final org.eclipse.swt.widgets.List templatesList = new org.eclipse.swt.widgets.List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
 		GridData listGridData = new GridData(GridData.FILL_BOTH);
 		listGridData.widthHint = 220;
 		templatesList.setLayoutData(listGridData);
 		if (templates.size() > 0) {
 			for (ProjectTemplate template : templates)
 				templatesList.add(template.getName());
 		}
 		
 		final Browser browser = new Browser(composite, SWT.READ_ONLY | SWT.BORDER);
 		final String blankLocation = browser.getUrl();
 		browser.addLocationListener(new LocationListener() {
 			@Override
 			public void changing(LocationEvent event) {
 				String url = event.location;
 				if (url != null && !url.equals(blankLocation)) {
 					event.doit = false;
 					try {
 						final IWebBrowser externalBrowser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
 							IWorkbenchBrowserSupport.AS_EXTERNAL,
 							getClass().getName(),
 							"",
 							""
 						);
 						externalBrowser.openURL(new URL(url));
 					}
 					catch (Exception e) {
 						// ignore...
 					}
 				}
 			}
 
 			@Override
 			public void changed(LocationEvent event) {
 			}
 		});
 		GridData textGridData = new GridData(GridData.FILL_BOTH);
 		textGridData.widthHint = 180;
 		browser.setLayoutData(textGridData);
 
 		templatesList.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				try {
 					setErrorMessage(null);
 					int index = templatesList.getSelectionIndex();
 					if (index == -1) {
 						controller = null;
 						setPageComplete(false);
 					}
 					else {
 						ProjectTemplate template = templates.get(index);
 						browser.setText("<div style='font-size: 14px'>" + template.getDescription() + "</div>", true);
 						controller = template.getController().newInstance();
 						controller.initialize((DynamicProjectWizard)getWizard(), template);
 						setPageComplete(true);
 					}
 				}
 				catch (Exception e) {
 					controller = null;
 					setErrorMessage(e.getMessage());
 					Activator.log("Template controller initialization error", e);
 					setPageComplete(false);
 				}
 			}
 		});
 				
 		setControl(composite);
 	}
 	
 	public boolean canFinish() {
 		return controller != null && controller.canFinish();
 	}
 	
 	public boolean performFinish() {
 		boolean accept = true;
 		
 		if (controller != null) {
 			try {
 				accept = controller.performFinish();
 			} catch (CoreException e) {
 				Activator.log("Template execution error", e);
 				accept = false;
 			} catch (InterruptedException e) {
 			}
 			if (accept)
 				controller = null;
 		}
 		
 		return accept;
 	}
 	
 	public boolean performCancel() {
 		boolean accept = true;
 		
 		if (controller != null) {
 			accept = controller.performCancel();
 			if (accept)
 				controller = null;
 		}
 		
 		return accept;
 	}
 }

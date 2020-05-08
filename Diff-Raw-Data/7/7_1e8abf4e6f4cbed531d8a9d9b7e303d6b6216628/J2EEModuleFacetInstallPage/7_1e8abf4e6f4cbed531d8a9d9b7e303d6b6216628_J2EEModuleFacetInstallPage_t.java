 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.wizard;
 
 
 import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.ui.project.facet.EarSelectionPanel;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelListener;
 import org.eclipse.wst.web.ui.internal.wizards.DataModelFacetInstallPage;
 
 public abstract class J2EEModuleFacetInstallPage extends DataModelFacetInstallPage implements IJ2EEModuleFacetInstallDataModelProperties {
 
     private IDataModelListener facetVersionListener = null;
     protected Button addDD;
     
 	public J2EEModuleFacetInstallPage(String pageName) {
 		super(pageName);
 	}
 
 	protected EarSelectionPanel earPanel;
 
 	public void dispose() {
 		if (null != earPanel) {
 			earPanel.dispose();
 		}
 		this.model.removeListener( this.facetVersionListener );
 		super.dispose();
 	}
 
 	protected void setupEarControl(final Composite parent) {
 		Composite c = new Composite(parent, SWT.NONE);
 		c.setLayoutData(gdhfill());
 		final GridLayout layout = new GridLayout(3, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		c.setLayout(layout);
 		this.earPanel = new EarSelectionPanel(model, c);
 	}
 	
 	protected void createGenerateDescriptorControl( final Composite parent )
 	{
         this.addDD = new Button(parent, SWT.CHECK);
         this.addDD.setText(Resources.generateDeploymentDescriptor);
         //synchHelper.synchCheckbox(addDD, GENERATE_DD, null); bug 215284 - see enter()
         GridData gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = 2;
         this.addDD.setLayoutData(gd);
 	}
 	
 	protected void registerFacetVersionChangeListener()
 	{
         this.facetVersionListener = new IDataModelListener()
         {
             public void propertyChanged( final DataModelEvent event )
             {
                 if( event.getFlag() == DataModelEvent.VALUE_CHG &&
                     event.getPropertyName().equals( FACET_VERSION ) )
                 {
                     final Runnable runnable = new Runnable()
                     {
                         public void run()
                         {
                             handleFacetVersionChangedEvent();
                         }
                     };
                    if(Thread.currentThread() == Display.getDefault().getThread()){
                    	runnable.run();
                    } else {
                    	Display.getDefault().asyncExec( runnable );
                    }
                 }
             }
         };
         
         this.model.addListener( facetVersionListener );
         handleFacetVersionChangedEvent();
 	}
 	
     protected void handleFacetVersionChangedEvent()
     {
         // The default implementation doesn't do anything. The subclass should override
         // to handle this event.
     }
 
     private static final class Resources extends NLS {
         public static String generateDeploymentDescriptor;
 
 
         static {
             initializeMessages(J2EEModuleFacetInstallPage.class.getName(), Resources.class);
         }
     }
 
     protected void enter() {
     	if (isFirstTimeToPage() && addDD != null)
     	{
     		synchHelper.synchCheckbox(addDD, GENERATE_DD, null);
     	}
     	super.enter();
     }
 
 }

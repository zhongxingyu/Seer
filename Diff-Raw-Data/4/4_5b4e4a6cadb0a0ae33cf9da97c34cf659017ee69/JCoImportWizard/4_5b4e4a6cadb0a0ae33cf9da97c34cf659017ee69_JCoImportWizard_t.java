 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.jcoimport;
 
import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 
 /**
  * A wizard to import the SAP Java Connector archives and create the necessary plug-ins.
  * @author vwegert
  *
  */
 public class JCoImportWizard extends Wizard implements IImportWizard {
 	
 	private ProjectGeneratorSettings generatorSettings;
 	private DataBindingContext context;
 	private DownloadPage downloadPage;
 	private ArchiveFilesPage archiveFilesPage;
 	private SummaryPage summaryPage;
 
 	/**
 	 * Default constructor.
 	 */
 	public JCoImportWizard() {
 		super();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		setWindowTitle(Messages.JCoImportWizard_WindowTitle);
 		setNeedsProgressMonitor(true);
 		
 		generatorSettings = new ProjectGeneratorSettings();
 		if (Platform.getInstallLocation() != null) {
 			generatorSettings.setBundleExportSelected(true);
 			// TODO use some API to locate the dropins folder
			generatorSettings.setExportPath(Platform.getInstallLocation().getURL().getFile() + "dropins" + File.pathSeparator + "sapjco"); //$NON-NLS-1$ $NON-NLS-2$
 		}
 		
 		context = new DataBindingContext();
 		downloadPage = new DownloadPage();
 		archiveFilesPage = new ArchiveFilesPage(context, generatorSettings);
 		summaryPage = new SummaryPage(context, generatorSettings);
 	}
 	
 	/* (non-Javadoc)
      * @see org.eclipse.jface.wizard.IWizard#addPages()
      */
     @Override
 	public void addPages() {
         super.addPages(); 
         addPage(downloadPage);
         addPage(archiveFilesPage);
         addPage(summaryPage);        
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.jface.wizard.Wizard#canFinish()
      */
     @Override
     public boolean canFinish() {
     	return super.canFinish() && getContainer().getCurrentPage() == summaryPage;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
 	 */
 	@Override
 	public boolean performFinish() {
 		context.updateModels();
 		try {
 			getContainer().run(true, true, new ProjectGenerator(generatorSettings));
 			return true;
 		} catch (InvocationTargetException e) {
 			ErrorDialog.openError(getShell(), Messages.JCoImportWizard_DialogTitle, e.getLocalizedMessage(), 
 					new Status(IStatus.ERROR, "net.sf.rcer.jcoimport", Messages.JCoImportWizard_ErrorMessage, e)); //$NON-NLS-1$
 			return false;
 		} catch (InterruptedException e) {
 			MessageDialog.openWarning(getShell(), Messages.JCoImportWizard_DialogTitle, Messages.JCoImportWizard_ImportCancelledMessage);
 			return false;
 		}
 	}
 
 }

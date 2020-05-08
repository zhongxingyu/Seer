 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jubula.client.core.errorhandling.IDatabaseVersionErrorHandler;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.businessprocess.ImportFileBP;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.dialogs.NagDialog;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.wizards.pages.DatabaseMigrationAssistantIntroPage;
 import org.eclipse.jubula.client.ui.wizards.pages.ImportProjectsWizardPage;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.JBFatalException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Wizard that assists in migrating a Jubula database so that it is 
  * compatible with a newer version of Jubula.
  *
  * @author BREDEX GmbH
  * @created May 25, 2010
  */
 public class DatabaseMigrationAssistant extends Wizard 
         implements IDatabaseVersionErrorHandler {
 
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(DatabaseMigrationAssistant.class);
     
     /** ID for the "Intro" page */
     private static final String INTRO_PAGE_ID =
         "DatabaseMigrationAssistant.IntroPage"; //$NON-NLS-1$
 
     /** ID for the "Import Projects" page */
     private static final String IMPORT_PROJECTS_PAGE_ID =
         "DatabaseMigrationAssistant.ImportProjectsPage"; //$NON-NLS-1$
 
     /** flag indicating whether the migration was successful */
     private boolean m_wasMigrationSuccessful = false;
 
     /** the page containing information about projects to import */
     private ImportProjectsWizardPage m_importProjectsPage;
 
     /**
      * Constructor
      */
     public DatabaseMigrationAssistant() {
         setNeedsProgressMonitor(true);
         setWindowTitle("Database Migration Assistant"); //$NON-NLS-1$
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public boolean handleDatabaseError() {
         Plugin.getDisplay().syncExec(new Runnable() {
             
             public void run() {
                 WizardDialog dialog = new WizardDialog(Plugin.getShell(), 
                         DatabaseMigrationAssistant.this);
                 dialog.setHelpAvailable(true);
                 dialog.open();
             }
         });
         
         return m_wasMigrationSuccessful;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addPages() {
         super.addPages();
         addPage(new DatabaseMigrationAssistantIntroPage(INTRO_PAGE_ID));
         m_importProjectsPage = 
             new ImportProjectsWizardPage(IMPORT_PROJECTS_PAGE_ID, false);
         addPage(m_importProjectsPage);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean canFinish() {
         return true;
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean performFinish() {
         try {
             getContainer().run(true, false, new IRunnableWithProgress() {
                 @SuppressWarnings("synthetic-access")
                 public void run(IProgressMonitor monitor) 
                     throws InterruptedException {
 
                     monitor.beginTask("Migrating...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                     try {
                         Hibernator.migrateDatabaseStructure();
                         m_wasMigrationSuccessful = true;
                         ImportFileBP.getInstance().importProjects(
                                 m_importProjectsPage, monitor);
                         Plugin.getDisplay().asyncExec(new Runnable() {
                             public void run() {
                                 NagDialog.runNagDialog(null,
                                            Messages
                                           .DatabaseMigrationAssistantFinalInfo,
                                            ContextHelpIds.
                                            DATABASE_MIGRATION_ASSISTANT);
                             }
                         });
                     } catch (JBFatalException e) {
                         LOG.error(Messages.AnErrorOccurredDuringMigration 
                                 + StringConstants.DOT, e);
                     } catch (JBException e) {
                         LOG.error(Messages.AnErrorOccurredDuringMigration 
                                 + StringConstants.DOT, e);
                     } finally {
                         monitor.done();
                     }
                 }
             });
         } catch (InvocationTargetException e) {
             LOG.error(Messages.AnErrorOccurredDuringMigration 
                     + StringConstants.DOT, e);
         } catch (InterruptedException e) {
             LOG.error(Messages.AnErrorOccurredDuringMigration 
                     + StringConstants.DOT, e);
         }
 
         return true;
     }
 
 }

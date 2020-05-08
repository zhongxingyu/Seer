 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.wizards.search.refactor;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.MultipleNodePM;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.persistence.locking.LockManager;
 import org.eclipse.jubula.client.ui.rcp.handlers.project.RefreshProjectHandler.RefreshProjectOperation;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.wizards.refactor.pages.ChooseTestCasePage;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.messagehandling.MessageInfo;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * This wizard is used for replacing Test Cases from items in a search result
  * 
  * @author BREDEX GmbH
  * 
  */
 public class SearchReplaceTCRWizard extends Wizard {
     /** ID for the "Choose" page */
     private static final String CHOOSE_PAGE_ID = "ReplaceTCRWizard.ChoosePageId"; //$NON-NLS-1$
     
     /**
      * Operation for replacing the test cases
      * 
      * @author BREDEX GmbH
      */
     private class ReplaceTestCaseOperation 
         implements IRunnableWithProgress {
                 
         /**
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) {
             monitor.beginTask(Messages.ReplaceTestCasesActionDialog,
                     IProgressMonitor.UNKNOWN);
             EntityManager session = GeneralStorage.getInstance()
                     .getMasterSession();
             try {
                 Persistor.instance().lockPOSet(session, m_setOfExecsToReplace);
                 
                 List<MultipleNodePM.AbstractCmdHandle> commands = 
                         new ArrayList<MultipleNodePM.AbstractCmdHandle>();
                 for (Iterator iterator = m_setOfExecsToReplace.iterator(); 
                         iterator.hasNext();) {
                     IExecTestCasePO exec = (IExecTestCasePO) iterator.next();
                     commands.add(new MultipleNodePM.UpdateTestCaseRefHandle(
                             exec, m_newSpec));
                 }
                 MessageInfo errorMessageInfo = 
                         MultipleNodePM.getInstance().executeCommands(
                                 commands, session);
                 if ((errorMessageInfo != null)) {
                     ErrorHandlingUtil.createMessageDialog(
                             errorMessageInfo.getMessageId(), 
                             errorMessageInfo.getParams(), 
                             null);
                 }
             } catch (JBException e) {
                 ErrorHandlingUtil.createMessageDialog(e, null, null);
             } finally {
                 LockManager.instance().unlockPOs(session);
             }
             monitor.done();
         }
         
     }
     
     /**
      * <code>m_setOfExecsToReplace</code>
      */
     private final Set<IExecTestCasePO> m_setOfExecsToReplace;
     
     /**
      * <code>m_choosePage</code>
      */
     private ChooseTestCasePage m_choosePage;
 
     /**
      * <code>m_newSpec</code>
      */
     private ISpecTestCasePO m_newSpec;
     
     /**
      * Constructor for the wizard page
      * 
      * @param execsToReplace
      *            set of ExecTC in which the SpecTC and other information should
      *            be changed
      */
     public SearchReplaceTCRWizard(Set<IExecTestCasePO> execsToReplace) {
         m_setOfExecsToReplace = execsToReplace;
     }
     
     /** {@inheritDoc}    */
     public boolean performFinish() {
         //This is needed if Finish was pressed on the first page
         m_newSpec = m_choosePage.getChoosenTestCase();
         EntityManager session = GeneralStorage.getInstance()
                 .getMasterSession();
         try {           
             Persistor.instance().lockPOSet(session, m_setOfExecsToReplace);
             PlatformUI.getWorkbench().getProgressService()
                 .run(true, false, new ReplaceTestCaseOperation());
             PlatformUI.getWorkbench().getProgressService().run(true, false,
                     new RefreshProjectOperation());
         } catch (InvocationTargetException e) {
             //Already handled;
         } catch (InterruptedException e) {
             //Already handled
         } catch (JBException e) {
             ErrorHandlingUtil.createMessageDialog(e, null, null);
         }
         return true;
     }
     /** {@inheritDoc}    */
     public boolean performCancel() {
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addPages() {
         super.addPages();
         Set<INodePO> specSet = new HashSet<INodePO>();
         for (Iterator iterator = m_setOfExecsToReplace.iterator(); 
                 iterator.hasNext();) {
             IExecTestCasePO exec = (IExecTestCasePO) iterator.next();
            if (ISpecTestCasePO.class.isAssignableFrom(
                    exec.getParentNode().getClass())) {
                specSet.add(exec.getParentNode());
            }
         }
         m_choosePage = new ChooseTestCasePage(specSet, CHOOSE_PAGE_ID);
         addPage(m_choosePage);
 
     }
     
     /**
      * {@inheritDoc}
      */
     public IWizardPage getNextPage(IWizardPage page) {
         if (page instanceof ChooseTestCasePage) {
             m_newSpec = m_choosePage.getChoosenTestCase();
         }
         return super.getNextPage(page);
     }
 }

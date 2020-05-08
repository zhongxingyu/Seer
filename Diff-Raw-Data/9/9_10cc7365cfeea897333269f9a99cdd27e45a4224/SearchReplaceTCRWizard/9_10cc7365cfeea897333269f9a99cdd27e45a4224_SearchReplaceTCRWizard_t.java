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
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jubula.client.core.businessprocess.CompNamesBP;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IDataSetPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITDManager;
 import org.eclipse.jubula.client.core.model.ITestDataPO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.MultipleNodePM;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.persistence.locking.LockManager;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.wizards.refactor.pages.ChooseTestCasePage;
 import org.eclipse.jubula.client.ui.rcp.wizards.search.refactor.pages.ComponentNameMappingWizardPage;
 import org.eclipse.jubula.client.ui.rcp.wizards.search.refactor.pages.ParameterNamesMatchingWizardPage;
 import org.eclipse.jubula.client.ui.rcp.wizards.search.refactor.pages.ReplaceExecTestCaseData;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.JBRuntimeException;
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
     /** ID for the "Component Mapping" page */
     private static final String COMPONENT_MAPPING_PAGE_ID = "ReplaceTCRWizard.ComponentMappingPageId"; //$NON-NLS-1$
     /** ID for the "Parameter Matching" page */
     private static final String PARAMETER_MATCHING_PAGE_ID = "ReplaceTCRWizard.ParameterMatchingPageId"; //$NON-NLS-1$
 
     /**
      * Operation for replacing the test cases
      * 
      * @author BREDEX GmbH
      */
     private class ReplaceTestCaseOperation implements IRunnableWithProgress {
 
         /** CompNamesBP to get AllCompNamePairs from an execTC*/
         private CompNamesBP m_compNamesBP = new CompNamesBP();
         
         /**
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) {
             monitor.beginTask(Messages.ReplaceTestCasesActionDialog,
                     m_replaceExecTestCaseData.getOldExecTestCases().size());
             EntityManager session = GeneralStorage.getInstance()
                     .getMasterSession();
             try {
                 Persistor.instance().lockPOSet(session,
                         m_replaceExecTestCaseData.getOldExecTestCases());
                 Persistor.instance().lockPO(session,
                         m_replaceExecTestCaseData.getNewSpecTestCase());
                 List<MultipleNodePM.AbstractCmdHandle> commands = 
                         new ArrayList<MultipleNodePM.AbstractCmdHandle>();
                 for (IExecTestCasePO exec: m_replaceExecTestCaseData
                         .getOldExecTestCases()) {
                     INodePO parent = exec.getParentNode();
                     int index = parent.indexOf(exec);
                     IExecTestCasePO newExec;
                     if (IEventExecTestCasePO.class.isAssignableFrom(
                             exec.getClass())) {
                         IEventExecTestCasePO newEventExec = NodeMaker
                             .createEventExecTestCasePO(
                                 m_replaceExecTestCaseData.getNewSpecTestCase(),
                                 parent);
                         IEventExecTestCasePO oldEventExec = 
                                 (IEventExecTestCasePO) exec;
                         newEventExec.setEventType(oldEventExec.getEventType());
                         newEventExec.setReentryProp(oldEventExec
                                 .getReentryProp());
                         newEventExec
                                 .setMaxRetries(oldEventExec.getMaxRetries());
                         newExec = newEventExec;
                     } else {
                         newExec = NodeMaker.createExecTestCasePO(
                                 m_replaceExecTestCaseData.getNewSpecTestCase());
                     }
                     // 1. copy primitive members
                     copyPrimitiveMembers(exec, newExec);
                     // 2. add data manager with new columns
                     addParametersFromOldToNewTC(exec, newExec);
                     // 3. copy referenced data cube
                     if (exec.getReferencedDataCube() != null
                             && !exec.getReferencedDataCube().equals(exec
                                     .getSpecTestCase()
                                     .getReferencedDataCube())) {
                         newExec.setReferencedDataCube(
                                 exec.getReferencedDataCube());
                     }
                     // 4. add pairs of component names
                     addNewCompNamePairs(exec, newExec);
                     commands.add(new MultipleNodePM.DeleteExecTCHandle(exec));
                     commands.add(new MultipleNodePM.AddExecTCHandle(parent,
                             newExec, index));
                     monitor.worked(1);
                 }
                 MessageInfo errorMessageInfo = MultipleNodePM.getInstance()
                         .executeCommands(commands, session);
                 // Since a lot of changes are done fire the project is "reloaded"
                 DataEventDispatcher.getInstance().fireProjectLoadedListener(
                         monitor);
                 if (errorMessageInfo != null) {
                     ErrorHandlingUtil.createMessageDialog(
                             errorMessageInfo.getMessageId(),
                             errorMessageInfo.getParams(), null);
                 }
             } catch (JBException e) {
                 ErrorHandlingUtil.createMessageDialog(e, null, null);
             } catch (JBRuntimeException e) {
                 ErrorHandlingUtil.createMessageDialog(e);
             } catch (RuntimeException e) {
                 e.printStackTrace();
             } finally {
                 LockManager.instance().unlockPOs(session);
             }
             monitor.done();
         }
 
         /**
          * Copy primitive members from old to new execution Test Case.
          * @param oldExec The old execution Test Case.
          * @param newExec The new execution Test Case.
          */
         private void copyPrimitiveMembers(IExecTestCasePO oldExec,
                 IExecTestCasePO newExec) {
             newExec.setActive(oldExec.isActive());
             newExec.setComment(oldExec.getComment());
             newExec.setDataFile(oldExec.getDataFile());
             newExec.setGenerated(oldExec.isGenerated());
             // copy execution test case name
             if (oldExec.getSpecTestCase().getName() != oldExec.getName()) {
                 newExec.setName(oldExec.getName());
             }
             newExec.setParentProjectId(oldExec.getParentProjectId());
             newExec.setToolkitLevel(oldExec.getToolkitLevel());
             newExec.setHasReferencedTD(
                     oldExec.getDataManager().equals(
                     oldExec.getSpecTestCase().getDataManager()));
         }
 
         /**
          * Add the parameters to the new execution Test Case by using the
          * map between new to old parameter descriptions.
          * @param oldExec The old execution Test Case.
          * @param newExec The new execution Test Case.
          */
         private void addParametersFromOldToNewTC(
                 IExecTestCasePO oldExec,
                 IExecTestCasePO newExec) {
             if (oldExec.getHasReferencedTD()
                     || oldExec.getReferencedDataCube() != null
                     || m_replaceExecTestCaseData.hasNoMatching()) {
                 // test data referenced to specification Test Case
                 // or a data cube exists or every new parameters are unmatched
                 return; // add no local test data
             }
             // get the parameter map
             Map<IParamDescriptionPO, IParamDescriptionPO> newOldParamMap =
                     m_replaceExecTestCaseData.getNewOldParamMap();
             // add the new parameter description IDs to data manager
             ITDManager tdManager = newExec.getDataManager();
             for (IParamDescriptionPO newParam: newOldParamMap.keySet()) {
                 tdManager.addUniqueId(newParam.getUniqueId());
             }
             // iterate over all data sets (rows)
             for (IDataSetPO oldDataSet: oldExec
                     .getDataManager().getDataSets()) {
                 List<ITestDataPO> newRow = new ArrayList<ITestDataPO>(
                         newOldParamMap.size());
                 // iterate over all new parameter descriptions
                 for (IParamDescriptionPO newParam: newOldParamMap.keySet()) {
                     IParamDescriptionPO oldParam = newOldParamMap.get(newParam);
                     if (oldParam != null) {
                         // copy old test data for new Test Case
                         int column = oldExec.getDataManager()
                                 .findColumnForParam(oldParam.getUniqueId());
                         // get column from old test data
                         ITestDataPO oldTestData = oldDataSet.getColumn(column);
                         newRow.add(oldTestData.deepCopy());
                     } else {
                         // create empty test data for new Test Case
                         newRow.add(PoMaker.createTestDataPO());
                     }
                 }
                 // add the new row to data set for new Test Case
                 tdManager.insertDataSet(
                         PoMaker.createListWrapperPO(newRow),
                         tdManager.getDataSetCount());
             }
         }
 
         /**
          * Looks in the<code>m_matchedCompNameGuidMap</code> if there is a
          * matching for the new ExecTestCase and add new CompNamePairs if
          * necessary
          * 
          * @param oldExec
          *            the old ExecTestCase
          * @param newExec
          *            the newly created ExecTestCase
          * @return the <code>newExec</code>
          */
         private IExecTestCasePO addNewCompNamePairs(IExecTestCasePO oldExec,
                 IExecTestCasePO newExec) {
             // Get all new compNamePairs to get the component names
             Collection<ICompNamesPairPO> compNamePairs = 
                     m_compNamesBP.getAllCompNamesPairs(newExec);
             for (ICompNamesPairPO newPseudoPairs : compNamePairs) {
                 if (m_matchedCompNameGuidMap.containsKey(newPseudoPairs
                         .getFirstName())) {
                     String oldCompName = m_matchedCompNameGuidMap
                             .get(newPseudoPairs.getFirstName());
                     Collection<ICompNamesPairPO> oldCompNamePairs = 
                             m_compNamesBP.getAllCompNamesPairs(oldExec);
                     
                     // Get the corresponding new Name of the old CompNamePair
                     for (Iterator iterator = oldCompNamePairs.iterator();
                             iterator.hasNext();) {
                         ICompNamesPairPO oldPair = (ICompNamesPairPO) iterator
                                 .next();
                         if (oldPair.getFirstName().equals(oldCompName)) {
                             ICompNamesPairPO newPair = PoMaker
                                     .createCompNamesPairPO(
                                             newPseudoPairs.getFirstName(),
                                             oldPair.getSecondName(),
                                             oldPair.getType());
                             newPair.setPropagated(oldPair.isPropagated());
                             newExec.addCompNamesPair(newPair);
                             break;
                         }
 
                     }
                 }
             }
             return newExec;
         }
 
     }
 
     /**
      * <code>m_setOfExecsToReplace</code>
      */
     private final ReplaceExecTestCaseData m_replaceExecTestCaseData;
 
     /**
      * Map for matching guid's for component names 
      */
     private Map<String, String> m_matchedCompNameGuidMap;
     
     /**
      * <code>m_choosePage</code>
      */
     private ChooseTestCasePage m_choosePage;
 
     /**
      * Component Names matching page ID
      */
     private ComponentNameMappingWizardPage m_componentNamesPage;
 
     /**
      * Constructor for the wizard page
      * 
      * @param execsToReplace
      *            set of ExecTC in which the SpecTC and other information should
      *            be changed
      */
     public SearchReplaceTCRWizard(Set<IExecTestCasePO> execsToReplace) {
         m_replaceExecTestCaseData = new ReplaceExecTestCaseData(execsToReplace);
         setWindowTitle(Messages.ReplaceTCRWizardTitle);
     }
 
     /** {@inheritDoc} */
     public boolean performFinish() {
         // This is needed if Finish was pressed on the first page
         m_matchedCompNameGuidMap = m_componentNamesPage.getCompMatching();
         try {
             PlatformUI.getWorkbench().getProgressService()
                     .run(true, false, new ReplaceTestCaseOperation());
         } catch (InvocationTargetException e) {
             // Already handled;
         } catch (InterruptedException e) {
             // Already handled
         }
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addPages() {
         super.addPages();
         Set<INodePO> specSet = new HashSet<INodePO>();
         for (IExecTestCasePO exec : m_replaceExecTestCaseData
                 .getOldExecTestCases()) {
             if (ISpecTestCasePO.class.isAssignableFrom(exec.getParentNode()
                     .getClass())) {
                 specSet.add(exec.getParentNode());
             }
         }    
         m_choosePage = new ChooseTestCasePage(specSet, CHOOSE_PAGE_ID);
         m_choosePage.setDescription(
                Messages.ReplaceTCRWizard_choosePage_multi_description);
         m_choosePage.setContextHelpId(ContextHelpIds
                 .SEARCH_REFACTOR_REPLACE_EXECUTION_TEST_CASE_WIZARD);
         m_componentNamesPage = new ComponentNameMappingWizardPage(
                 COMPONENT_MAPPING_PAGE_ID,
                 m_replaceExecTestCaseData.getOldExecTestCases());
         m_componentNamesPage.setDescription(Messages
                 .ReplaceTCRWizard_matchComponentNames_multi_description);
         addPage(m_choosePage);
         addPage(m_componentNamesPage);
         addPage(new ParameterNamesMatchingWizardPage(
                 PARAMETER_MATCHING_PAGE_ID, m_replaceExecTestCaseData));
     }
 
     /**
      * {@inheritDoc}
      */
     public IWizardPage getNextPage(IWizardPage page) {
         if (page instanceof ChooseTestCasePage) {
             m_replaceExecTestCaseData.setNewSpecTestCase(
                     m_choosePage.getChoosenTestCase());
             // FIXME RB: wizard pages should update data it self
             m_componentNamesPage.setNewSpec(
                     m_replaceExecTestCaseData.getNewSpecTestCase());
         }
         IWizardPage nextPage = super.getNextPage(page);
         return nextPage;
     }
 
 }

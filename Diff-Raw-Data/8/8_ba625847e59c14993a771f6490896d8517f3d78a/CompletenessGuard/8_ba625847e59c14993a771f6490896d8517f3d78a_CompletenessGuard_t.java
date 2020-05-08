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
 package org.eclipse.jubula.client.core.businessprocess;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.client.core.model.LogicComponentNotManagedException;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IObjectMappingPO;
 import org.eclipse.jubula.client.core.model.IParamNodePO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.utils.ITreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.jubula.tools.xml.businessmodell.Component;
 import org.eclipse.jubula.tools.xml.businessmodell.ConcreteComponent;
 
 
 /**
  * @author BREDEX GmbH
  * @created 10.05.2005
  */
 @SuppressWarnings("synthetic-access")
 public final class CompletenessGuard {
     
     
     /**
      * Operation to set the CompleteSpecTc flag for a .
      */
     private static class CompleteSpecTcOperation 
             implements ITreeNodeOperation<INodePO> {
 
         /**
          * {@inheritDoc}
          */
         public boolean operate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             
             if (!node.isActive()) {
                 node.setSumSpecTcFlag(true);
                 if (node instanceof IExecTestCasePO) {
                     ((IExecTestCasePO)node).setCompleteSpecTcFlag(true);
                 }
                 return false;
             }
             if (node instanceof IExecTestCasePO) {
                 IExecTestCasePO execTc = (IExecTestCasePO)node;
                 boolean isMissingSpecTc = execTc.getSpecTestCase() == null;
                 execTc.setSumSpecTcFlag(!isMissingSpecTc);
                 execTc.setCompleteSpecTcFlag(!isMissingSpecTc);
             } else {
                 node.setSumSpecTcFlag(true);
             }
             return !alreadyVisited;
         }
 
         /**
          * {@inheritDoc}
          */
         public void postOperate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
 
             if (parent != null && parent.getSumSpecTcFlag()) {
                 parent.setSumSpecTcFlag(node.getSumSpecTcFlag());
             }            
         }
         
     }
 
     /**
      * Updates the flags for the object mapping (complete or not?)
      * in all nodes down to the test step.
      */
     private static class OMFlagsOperation 
             implements ITreeNodeOperation<INodePO> {
         /**
          * The business process that performs component name operations.
          */
         private final CompNamesBP m_compNamesBP = new CompNamesBP();
         /**
          * The AUT
          */
         private IAUTMainPO m_aut;
         /**
          * The flag of complete object mapping
          */
         private boolean m_sumOMFlag = true;
         
         /**
          * Operates on the test step by setting its object mapping flag. The
          * node that is responsible for a component name is also updated.
          * 
          * {@inheritDoc}
          * 
          * @param ctx
          *            The tree traverser context
          * @param cap
          *            The test step
          * @return <code>true</code> if the component name of the test step is
          *         mapped, <code>false</code> otherwise
          */
         private boolean handleCap(ITreeTraverserContext<INodePO> ctx, 
                 ICapPO cap) {
             if (!cap.isActive()) {
                 cap.setCompleteOMFlag(m_aut, true);
                 return true;
             }
             IObjectMappingPO objMap = m_aut.getObjMap();
             String compName = cap.getComponentName();
             IComponentNamePO compNamePo = 
                 ComponentNamesBP.getInstance().getCompNamePo(compName);
             if (compNamePo != null) {
                 compName = compNamePo.getGuid();
             }
             
             final Component metaComponentType = cap.getMetaComponentType();
             if (metaComponentType instanceof ConcreteComponent
                     && ((ConcreteComponent)metaComponentType)
                         .hasDefaultMapping()) {
                 
                 cap.setCompleteOMFlag(m_aut, true);
                 return true;
             }
             
             if (compName != null && objMap != null) {
                 IComponentIdentifier id = null;
                 CompNameResult result = m_compNamesBP.findCompName(
                     ctx.getCurrentTreePath(), cap, cap.getComponentName(),
                     ComponentNamesBP.getInstance());
                 try {
                     id = objMap.getTechnicalName(result.getCompName());
                 } catch (LogicComponentNotManagedException e) { // NOPMD by al on 3/19/07 1:21 PM
                     // ok
                 }
                 if (id == null) {
                     result.getResponsibleNode().setSumOMFlag(m_aut, false);
                     if (result.getResponsibleNode() == cap) {
                         cap.setCompleteOMFlag(m_aut, false);
                     }
                 } else {
                     cap.setCompleteOMFlag(m_aut, true);
                 }
             }
             
             return cap.getCompleteOMFlag(m_aut);
         }
         /**
          * Sets the object mapping flag of the passed node to <code>true</code>
          * 
          * {@inheritDoc}
          * {@inheritDoc}
          *      org.eclipse.jubula.client.core.model.INodePO,
          *      org.eclipse.jubula.client.core.model.INodePO)
          * 
          * @param ctx
          *            The context
          * @param parent
          *            The parent node
          * @param node
          *            The node
          */
         public boolean operate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             if (node instanceof ITestSuitePO) {
                 ITestSuitePO ts = (ITestSuitePO)node;
                 m_aut = ts.getAut();
             }
             if (m_aut != null) {
                 if (!(node instanceof ICapPO)) { 
                     node.setSumOMFlag(m_aut, true);
                     if (!node.isActive()) {
                         return false;
                     }
                 }
             }
             return true;
         }
         /**
          * Updates the object mapping flags of the passed node and its parent.
          * 
          * {@inheritDoc}
          *      org.eclipse.jubula.client.core.model.INodePO,
          *      org.eclipse.jubula.client.core.model.INodePO)
          */
         public void postOperate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             
             if (m_aut != null) {
                 if (node instanceof ICapPO) {
                     m_sumOMFlag = handleCap(ctx, (ICapPO)node);
                     if (parent != null && !m_sumOMFlag) {
                         parent.setSumOMFlag(m_aut, m_sumOMFlag);
                     }
                // Only set the OM flag if it's possible that the node could 
                // actually *have* incomplete mappings (i.e. if a node is not a 
                // Test Step and has no children, then it can't have mappings 
                // and therefore can't have incomplete mappings). Without this 
                // check, an empty Test Case could be marked as OM Incomplete if
                // a "nearby" Test Case is OM Incomplete.
                } else if (node.getNodeListSize() > 0) {  
                     m_sumOMFlag = node.getSumOMFlag(m_aut) && m_sumOMFlag;
                     node.setSumOMFlag(m_aut, m_sumOMFlag);
                     if (parent != null && !m_sumOMFlag) {
                         parent.setSumOMFlag(m_aut, m_sumOMFlag);
                     }
                 }
             }
         }
     }
     
     /**
      * Operation to set the CompleteTestData flag at TDManager.
      * @author BREDEX GmbH
      * @created 10.10.2005
      */
     private static class CompleteTdDataOperation 
             implements ITreeNodeOperation<INodePO> {
 
         /** The Locale to check */
         private Locale m_locale;
         
         /**
          * 
          * @param loc The Locale to check
          */
         public CompleteTdDataOperation(Locale loc) {
             m_locale = loc;
         }
         
         /**
          * Sets the CompleteTDFlag in all ParamNodePOs.
          * {@inheritDoc}
          * @param ctx
          * @param parent
          * @param node
          */
         public boolean operate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             if (node instanceof IParamNodePO) {
                 IParamNodePO paramNode = (IParamNodePO)node;
                 if (node instanceof IExecTestCasePO) {
                     IExecTestCasePO execTc = (IExecTestCasePO)node;
                     if (execTc.getHasReferencedTD()) {
                         paramNode = execTc.getSpecTestCase();
                     }
                 }
                 
                 if (paramNode != null) { 
                     // FIXME Andreas : If entering a new Parameter into a step, a parameter is
                     // created at parent TC. But there is no Row created. Guard should
                     // check if there is missing data or Row(0) have to be created
 
                     boolean isComplete = paramNode.isTestDataComplete(m_locale);
                     if (!node.isActive() || !paramNode.isActive()) {
                         isComplete = true;
                     }
                     CompletenessGuard.setCompFlagForTD(paramNode, m_locale, 
                             isComplete);
                     if (!node.isActive() || !paramNode.isActive()) {
                         return false;
                     }
                 }
             }
             return !alreadyVisited;
         }
 
         /**
          * {@inheritDoc}
          * @param ctx
          * @param parent
          * @param node
          */
         public void postOperate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             // no op
         }
         
     }
     
     /**
      * private constructor
      */
     private CompletenessGuard() {
     // nothing
     }
 
     /**
      * method to label the testdata completeness for a node
      * 
      * @param node
      *            node, for which the complete flag is to set
      * @param loc
      *            language, for which the complete flag is valid
      * @param hasCompleteTD
      *            logical value for completeness of testdata
      */
     public static void setCompFlagForTD(IParamNodePO node, Locale loc,
         boolean hasCompleteTD) {
         updateTdMap(loc, hasCompleteTD, node);
     }
 
     /**
      * @param loc
      *            language, for which the complete flag is modified
      * @param hasCompleteTD
      *            state of completeFlag for node
      * @param node
      *            node, which has the modified completeTDFlag
      */
     private static void updateTdMap(Locale loc, boolean hasCompleteTD,
         IParamNodePO node) {
         node.setCompleteTdFlag(loc, hasCompleteTD);
         if (!hasCompleteTD) {
             node.setSumTdFlag(loc, hasCompleteTD);
         }
     }
 
     /**
      * @param node node, for which to summarizes the tdFlag for itself and 
      * its children to the sumTdFlag
      * @param locale The Locale to check
      * @return state of sumTdFlag
      */
     private static boolean checkTdFlags(INodePO node, Locale locale) {
         
         boolean sumTdFlag = true;
         if (node instanceof ICapPO) {
             sumTdFlag = ((IParamNodePO)node).getCompleteTdFlag(locale) 
                 && sumTdFlag;
             // testsuite or execTestCase
         } else {
             if (node instanceof IExecTestCasePO) {
                 final IExecTestCasePO execTC = (IExecTestCasePO)node;
                 if (execTC.getSpecTestCase() != null) {
                     for (Object obj : execTC.getSpecTestCase()
                             .getAllEventEventExecTC()) {
                         
                         IEventExecTestCasePO ev = (IEventExecTestCasePO)obj;
                         sumTdFlag = 
                             checkTdFlags(ev, locale) && sumTdFlag;
                     }
                     if (StringUtils.isEmpty(execTC.getDataFile())) {
                         if (execTC.getHasReferencedTD()) {
                             sumTdFlag = execTC.getSpecTestCase()
                                 .getCompleteTdFlag(locale) && sumTdFlag;
                         } else {
                             sumTdFlag = 
                                 execTC.getCompleteTdFlag(locale) && sumTdFlag;
                         }
                     }
                 }
                 // if ExecTC under a TestSuite has references as value
                 if (execTC.getParentNode() instanceof ITestSuitePO 
                         && execTC.getParamReferencesIterator(locale)
                             .hasNext()) {
                     
                     execTC.setCompleteTdFlag(locale, false);
                     execTC.setSumTdFlag(locale, false);
                     sumTdFlag = false;
                 }
             }
             final Iterator it = node.getNodeListIterator();
             while (it.hasNext()) {
                 final INodePO child = (INodePO)it.next();
                 sumTdFlag = checkTdFlags(child, locale) 
                     && sumTdFlag;
             }
             if (!node.isActive()) {
                 sumTdFlag = true;
             }
             node.setSumTdFlag(locale, sumTdFlag);
         }
         return sumTdFlag;
     }
     
     /**
      * checks OM and TD Completness of all TS
      * @param loc the Locale to check
      * @param root INodePO
      */
     public static void checkAll(Locale loc, INodePO root) {
         // Iterate Execution tree
         final TreeTraverser traverser = new TreeTraverser(root);
         traverser.addOperation(new OMFlagsOperation());
         traverser.addOperation(new CompleteTdDataOperation(loc));
         traverser.addOperation(new CompleteSpecTcOperation());
         traverser.traverse(true);
         if (root instanceof ITestSuitePO) {
             checkTdFlags(root, loc);
         } else {
             final List<ITestSuitePO> tsList = GeneralStorage.getInstance()
                 .getProject().getTestSuiteCont().getTestSuiteList();
             for (ITestSuitePO ts : tsList) {
                 // calculate final td flags
                 checkTdFlags(ts, loc);
             }
         }
     }
 
     /**
      * checks TD Completness of all TS
      * @param loc the Locale to check
      * @param root
      *      INodePO
      */
     public static void checkTD(Locale loc, INodePO root) {
         // Iterate Execution tree
         final ITreeNodeOperation<INodePO> tdOp = 
             new CompleteTdDataOperation(loc);
         final TreeTraverser traverser = new TreeTraverser(root, tdOp);
         traverser.traverse(true);
 
         if (root instanceof ITestSuitePO) {
             checkTdFlags(root, loc);
         } else {
             final List<ITestSuitePO> tsList = GeneralStorage.getInstance()
                 .getProject().getTestSuiteCont().getTestSuiteList();
             for (ITestSuitePO ts : tsList) {
                 // calculate final td flags
                 checkTdFlags(ts, loc);
             }
         }
     }
 
     /**
      * checks OM Completness of all TS
      * @param root
      *      INodePO
      */
     public static void checkOM(INodePO root) {
         // Iterate Execution tree
         final ITreeNodeOperation<INodePO> omOp = new OMFlagsOperation();
         final TreeTraverser traverser = new TreeTraverser(root, omOp);
         traverser.traverse(true);
     }
 
 }

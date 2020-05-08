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
 package org.eclipse.jubula.client.ui.rcp.search.query;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jubula.client.core.businessprocess.db.TestSuiteBP;
 import org.eclipse.jubula.client.core.businessprocess.treeoperations.FindResponsibleNodesForComponentNameOp;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.osgi.util.NLS;
 
 
 /**
  * @author BREDEX GmbH
  * @created Aug 24, 2010
  */
 public class ShowResponsibleNodeForComponentName 
     extends ShowWhereUsedComponentNameQuery {
     /**
      * <code>m_aut</code>
      */
     private IAUTMainPO m_aut;
     
     /**
      * @param compName the comp name to search
      * @param aut the aut to search the comp name usage for
      */
     public ShowResponsibleNodeForComponentName(IComponentNamePO compName,
         IAUTMainPO aut) {
         super(compName);
         m_aut = aut;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getLabel() {
         StringBuilder sb = new StringBuilder();
         sb.append(getTimestamp());
         sb.append(StringConstants.COLON);
         sb.append(StringConstants.SPACE);
         sb.append(Messages.UIJobSearchingResponsibleCompNames);
         sb.append(StringConstants.SPACE);
         sb.append(StringConstants.QUOTE);
         sb.append(getCompName().getName());
         sb.append(StringConstants.QUOTE);
         return sb.toString();
     }
     
     /**
      * {@inheritDoc}
      */
     public IStatus run(IProgressMonitor monitor) {
         calculateUseOfLogicalName(getCompName().getGuid(), monitor);
         finished();
         return Status.OK_STATUS;
     }
 
     /**
      * shows all places, where a specified component name is used spcific for
      * it's occurence in this object mapping
      * 
      * @param logicalName
      *            the guid of the logical name to search for
      * @param monitor
      *            the progress monitor
      */
     protected void calculateUseOfLogicalName(String logicalName, 
         IProgressMonitor monitor) {
         monitor.beginTask(NLS.bind(Messages.ShowResponsibleNodeOperation,
                 getCompName().getName()),
                 IProgressMonitor.UNKNOWN);
         calculateListOfComponentNamesUsingNodes(logicalName, m_aut);
     }
 
     /**
      * @param logicalNameGUID
      *            the guid of the comp name to search for
      * @param searchAUT
      *            the aut to search for
      */
     private void calculateListOfComponentNamesUsingNodes(
             String logicalNameGUID, IAUTMainPO searchAUT) {
         IProjectPO currentProject = GeneralStorage.getInstance().getProject();
         for (ITestSuitePO ts : TestSuiteBP
                 .getListOfTestSuites(currentProject)) {
             IAUTMainPO aut = ts.getAut();
             if (aut != null
                     && ObjectUtils.equals(aut.getGuid(), searchAUT.getGuid())) {
                 FindResponsibleNodesForComponentNameOp op = 
                     new FindResponsibleNodesForComponentNameOp(logicalNameGUID);
                 TreeTraverser traverser = new TreeTraverser(ts, op);
                 traverser.traverse(true);
                 addAll(op.getNodes());
             }
         }
     }
 
 }

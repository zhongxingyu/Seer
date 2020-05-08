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
 package org.eclipse.jubula.client.ui.provider.contentprovider;
 import java.util.Locale;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestJobContPO;
 import org.eclipse.jubula.client.core.model.ITestSuiteContPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.ui.businessprocess.WorkingLanguageBP;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created 15.10.2004
  */
 public class TestSuiteBrowserContentProvider 
     extends AbstractTreeViewContentProvider {
 
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(TestSuiteBrowserContentProvider.class);
     
     /** {@inheritDoc} */
     public Object[] getElements(Object inputElement) {
         if (inputElement instanceof IProjectPO[]) {
             return (IProjectPO[])inputElement;
         }
 
         LOG.error("Wrong type for input element: " + inputElement); //$NON-NLS-1$
         return ArrayUtils.EMPTY_OBJECT_ARRAY;
     }
 
     /**
      * @param parentElement Object
      * @return object array
      */
     public Object[] getChildren(Object parentElement) {
         if (parentElement instanceof IProjectPO) {
             IProjectPO project = (IProjectPO)parentElement;
             return new Object[] {
                 project.getTestJobCont(),
                 project.getTestSuiteCont(),
             };
         }
 
         if (parentElement instanceof IExecTestCasePO) {
             ISpecTestCasePO referencedTestCase = 
                 ((IExecTestCasePO)parentElement).getSpecTestCase();
             if (referencedTestCase != null) {
                return referencedTestCase.getUnmodifiableNodeList().toArray();
             }
             
             return ArrayUtils.EMPTY_OBJECT_ARRAY;
         }
         
         if (parentElement instanceof ITestSuitePO) {
             ITestSuitePO testSuite = (ITestSuitePO)parentElement;
             Locale workLang = WorkingLanguageBP.getInstance()
                     .getWorkingLanguage();
             if (testSuite.getAut() != null
                     && !WorkingLanguageBP.getInstance()
                             .isTestSuiteLanguage(workLang, testSuite)) {
                 return new Object[0];
             }
         
             // fall through
         }
         
         if (parentElement instanceof INodePO) {
             return ((INodePO)parentElement).getUnmodifiableNodeList().toArray();
         }
         
         if (parentElement instanceof ITestSuiteContPO) {
             return ((ITestSuiteContPO)parentElement)
                 .getTestSuiteList().toArray();
         }
 
         if (parentElement instanceof ITestJobContPO) {
             return ((ITestJobContPO)parentElement).getTestJobList().toArray();
         }
 
 
         LOG.error("Wrong type for parent element: " + parentElement); //$NON-NLS-1$
         return ArrayUtils.EMPTY_OBJECT_ARRAY;
     }
 }

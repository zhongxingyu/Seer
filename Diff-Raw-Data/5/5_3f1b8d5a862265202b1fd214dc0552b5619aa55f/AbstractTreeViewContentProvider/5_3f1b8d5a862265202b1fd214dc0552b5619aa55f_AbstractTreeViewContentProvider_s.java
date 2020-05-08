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
 
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.ITestDataCubeContPO;
 import org.eclipse.jubula.client.core.model.ITestDataCubePO;
 import org.eclipse.jubula.client.core.model.ITestJobContPO;
 import org.eclipse.jubula.client.core.model.ITestSuiteContPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.search.result.BasicSearchResult.SearchResultElement;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 
 
 /**
  * @author BREDEX GmbH
  * @created 13.09.2005
  */
 public abstract class AbstractTreeViewContentProvider 
         implements ITreeContentProvider {
     
     /** {@inheritDoc} */
     public Object[] getElements(Object inputElement) {
         return getChildren(inputElement);
     }
     
     /** {@inheritDoc} */
     public boolean hasChildren(Object element) {
         return getChildren(element).length > 0;
     }
     
     /** {@inheritDoc} */
     public void dispose() {
         // do nothing
     }
 
     /** {@inheritDoc} */
     public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         // do nothing yet
     }
 
     /** {@inheritDoc} */
     public Object getParent(Object element) {
         if (element instanceof INodePO) {
             return ((INodePO)element).getParentNode();
         }
         if (element instanceof ITestSuiteContPO
                || element instanceof ITestJobContPO) {
             return GeneralStorage.getInstance().getProject();
         }
         if (element instanceof ITestDataCubeContPO
                 || element instanceof ITestDataCubePO
                 || element instanceof SearchResultElement) {
             return null;
         }
         Assert.notReached(Messages.WrongTypeOfElement 
                 + StringConstants.EXCLAMATION_MARK);
         return null;
     }
 
 }

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
 package org.eclipse.jubula.client.core.businessprocess.treeoperations;
 
 import org.eclipse.jubula.client.core.businessprocess.CompNameResult;
 import org.eclipse.jubula.client.core.businessprocess.CompNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 
 /**
  * Operation for finding all nodes that are responsible for a specific Component
  * Name in the OME.
  * 
  * @author BREDEX GmbH
  * @created Aug 30, 2010
  */
 public class FindResponsibleNodesForComponentNameOp 
     extends FindNodesForComponentNameOp {
     /**
      * <code>m_compNameBP</code>
      */
     private CompNamesBP m_compNameBP = null;
     
     /**
      * Constructor
      * 
      * @param compNameGuid The GUID of the Component Name to use for this 
      *                     operation.
      */
     public FindResponsibleNodesForComponentNameOp(String compNameGuid) {
         super(compNameGuid);
         m_compNameBP = new CompNamesBP();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean operate(ITreeTraverserContext<INodePO> ctx, INodePO parent, 
             INodePO node, boolean alreadyVisited) {
         if (Hibernator.isPoSubclass(node, ICapPO.class)) {
             final ICapPO cap = (ICapPO)node;
             CompNameResult result = 
                 m_compNameBP.findCompName(ctx.getCurrentTreePath(), 
                         cap, cap.getComponentName(),
                         ComponentNamesBP.getInstance());
             if (getCompNameGuid().equals(result.getCompName())) {
                INodePO responsibleNode = result.getResponsibleNode();
                if (Hibernator.isPoSubclass(responsibleNode, ICapPO.class)) {
                    getNodes().add(parent);
                }
                getNodes().add(responsibleNode);
             }
         }
         return true;
     }
 }

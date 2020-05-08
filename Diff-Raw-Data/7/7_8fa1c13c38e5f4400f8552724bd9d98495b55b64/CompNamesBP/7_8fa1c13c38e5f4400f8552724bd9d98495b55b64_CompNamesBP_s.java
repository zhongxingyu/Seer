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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP.CompNameCreationContext;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.persistence.IncompatibleTypeException;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.xml.businessmodell.Component;
 import org.eclipse.jubula.tools.xml.businessmodell.ConcreteComponent;
 
 
 /**
  * This business process performs the override and propagate operations on
  * component names on the test execution node level.
  * 
  * @author BREDEX GmbH
  * @created 08.09.2005
  */
 public class CompNamesBP {
     /**
      * Interface for updates of overriden component names or propagations.
      */
     private interface IUpdater {
         /**
          * Updates the parent test execution node.
          * 
          * @param parentExec
          *            The parent test execution node
          * @param pair
          *            The component name pair
          */
         public void updateParentExecTestCase(IExecTestCasePO parentExec,
             ICompNamesPairPO pair);
 
         /**
          * Updates the component name pair
          * 
          * @param pair
          *            The component name pair
          * @return <code>true</code> if the pair has been updated,
          *         <code>false</code> otherwise
          */
         public boolean updateCompNamesPair(ICompNamesPairPO pair);
     }
 
     /**
      * Updates the propagated property of a component name pair.
      * 
      * {@inheritDoc}
      */
     private static class PropagatedUpdater implements IUpdater {
         /**
          * The propagated property
          */
         private boolean m_propagated;
 
         /**
          * Constructor
          * 
          * @param propagated
          *            The propagated property to update
          */
         public PropagatedUpdater(boolean propagated) {
             m_propagated = propagated;
         }
 
         /**
          * Updates the The propagated property and returns <code>true</code>
          * if the new property differs from the property of <code>pair</code>.
          * 
          * {@inheritDoc}
          */
         public boolean updateCompNamesPair(ICompNamesPairPO pair) {
             boolean oldValue = pair.isPropagated();
             pair.setPropagated(m_propagated);
 
             return oldValue != m_propagated;
         }
 
         /**
          * Removes the component name pair from the parent if it is not
          * propagated.
          * 
          * {@inheritDoc}
          *      org.eclipse.jubula.client.core.model.CompNamesPairPO)
          */
         public void updateParentExecTestCase(IExecTestCasePO parentExec,
             ICompNamesPairPO pair) {
 
             String name = pair.getSecondName();
             if (!m_propagated) {
                 parentExec.removeCompNamesPair(name);
             }
         }
     }
 
     /**
      * Updates the second name (that means, the overriding name) of a component
      * name pair.
      */
     private static class SecondNameUpdater implements IUpdater {
         /**
          * The second name GUID.
          */
         private String m_secondNameGuid;
 
         /**
          * Constructor
          * 
          * @param secondName
          *            The second name
          */
         public SecondNameUpdater(String secondName) {
             m_secondNameGuid = secondName;
         }
 
         /**
          * Sets the second name into the <code>pair</code>. Returns
          * <code>true</code>.
          * 
          * {@inheritDoc}
          */
         public boolean updateCompNamesPair(ICompNamesPairPO pair) {
             pair.setSecondName(m_secondNameGuid);
             return true;
         }
 
         /**
          * Updates the parent node if it contains a component name pair that
          * overrides the second name of the passed <code>pair</code>.
          * 
          * {@inheritDoc}
          *      org.eclipse.jubula.client.core.model.CompNamesPairPO)
          */
         public void updateParentExecTestCase(IExecTestCasePO parentExec,
             ICompNamesPairPO pair) {
 
             Validate.noNullElements(new Object[]{parentExec, pair});
             ICompNamesPairPO parentPair = parentExec.getCompNamesPair(pair
                 .getSecondName());
             if (parentPair != null) {
                 parentExec.removeCompNamesPair(parentPair.getFirstName());
                 final String type = parentExec.getCompNamesPair(
                     pair.getFirstName()).getType();
                 parentExec.addCompNamesPair(PoMaker.createCompNamesPairPO(
                     m_secondNameGuid, parentPair.getSecondName(), type));
             }
         }
     }
 
     /**
      * Adds all propagated component name pairs of the test execution node to
      * the map <code>pairs</code>.
      * 
      * @param pairs
      *            The map
      * @param execNode
      *            The test execution node
      */
     private void addPropagatedPairs(Map<String, ICompNamesPairPO> pairs,
             IExecTestCasePO execNode) {
         for (ICompNamesPairPO pair : execNode.getCompNamesPairs()) {
             if (pair.isPropagated()) {
                 String name = pair.getSecondName();
                 if (!pairs.containsKey(name)) {
                     String type = (pair.getType() == null || pair.getType()
                             .equals(StringConstants.EMPTY)) 
                                 ? StringConstants.EMPTY : pair.getType();
                     // ------------------------------------------------
                     if (pair.getType() == null) {
                         for (Object o : execNode.getSpecTestCase()
                                 .getUnmodifiableNodeList()) {
 
                             INodePO node = (INodePO)o;
                             if (node instanceof ICapPO) {
                                 ICapPO cap = (ICapPO)node;
                                 if (cap.getComponentName().equals(
                                         pair.getName())) {
 
                                     type = cap.getComponentType();
                                     pair.setType(type);
                                     break;
                                 }
                             }
                         }
                     }
                     pairs.put(name, PoMaker.createCompNamesPairPO(name, type));
                 }
             }
         }
     }
 
     /**
      * Adds the component name of the passed test step to the map if the
      * component is not mapped by default.
      * 
      * @param pairs
      *            The map
      * @param capNode
      *            The test step
      */
     private void addCapComponentName(Map<String, ICompNamesPairPO> pairs,
         ICapPO capNode) {
         
         final Component component = capNode.getMetaComponentType();
         if (component instanceof ConcreteComponent
             && ((ConcreteComponent)component).hasDefaultMapping()) {
             return;
         }
         final String name = capNode.getComponentName();
         final String type = capNode.getComponentType();
         if (!pairs.containsKey(name)) {
             ICompNamesPairPO pair = PoMaker.createCompNamesPairPO(name, type);
             pairs.put(name, pair);
         }
     }
 
     /**
      * Gets all component name pairs of the passed test execution node. The list
      * contains all pairs of the passed node itself, and the propagated pairs of
      * the child test execution nodes, and the component names of the child test
      * steps.
      * 
      * @param execNode
      *            The test execution node
      * @return The list with all component name pairs that (directly or
      *         indirectly) belong to the passed node. The list is ordered by the
      *         first names in ascending order.
      */
     public List<ICompNamesPairPO> getAllCompNamesPairs(
             IExecTestCasePO execNode) {
         
         Map<String, ICompNamesPairPO> pairs = 
             new HashMap<String, ICompNamesPairPO>();
 
         for (ICompNamesPairPO pair : execNode.getCompNamesPairs()) {
             pairs.put(pair.getFirstName(), pair);
         }
 
         ISpecTestCasePO specNode = execNode.getSpecTestCase();
 
         if (specNode != null) {
             for (Iterator it = specNode.getNodeListIterator(); it.hasNext();) {
                 INodePO child = (INodePO)it.next();
                 if (child instanceof IExecTestCasePO) {
                     addPropagatedPairs(pairs, (IExecTestCasePO)child);
                 } else if (child instanceof ICapPO) {
                     addCapComponentName(pairs, (ICapPO)child);
                 }
             }
         }
 
         List<ICompNamesPairPO> pairList =
             new ArrayList<ICompNamesPairPO>(pairs.values());
         // Sort the list by default
         Collections.sort(pairList, new Comparator<ICompNamesPairPO>() {
             public int compare(ICompNamesPairPO o1, ICompNamesPairPO o2) {
                 return o1.getFirstName().compareTo(o2.getFirstName());
             }
         });
         
         return pairList;
     }
 
     /**
      * Finds all test execution nodes of the passed test specification node with
      * the following condition: The test execution node matches if one of its
      * children is the same as the passed execution node <code>execNode</code>.
      * 
      * @param specNode
      *            The specification node
      * @param execNode
      *            The execution node
      * @return The list of execution nodes
      */
     private List<IExecTestCasePO> findExecNodes(ISpecTestCasePO specNode,
         IExecTestCasePO execNode) {
         
         List<IExecTestCasePO> nodes = new ArrayList<IExecTestCasePO>();
         List <IExecTestCasePO> execTestCases = NodePM
             .getInternalExecTestCases(specNode.getGuid(), 
                 specNode.getParentProjectId());
         for (IExecTestCasePO execTc : execTestCases) {
             for (Iterator itNodes = execTc.getNodeListIterator(); itNodes
                 .hasNext();) {
                 
                 INodePO child = (INodePO)itNodes.next();
                 if (child == execNode) {
                     nodes.add(execTc);
                 }
             }
         }        
         return nodes;
     }
 
     /**
      * Updates the passed execution node and the passed component name pair
      * using the updater.
      * 
      * @param execNode
      *            The execution node to update
      * @param pair
      *            The component name pair to update
      * @param updater
      *            The updater
      * @return The result of
      *         {@link IUpdater#updateCompNamesPair(CompNamesPairPO)}
      */
     private boolean updateCompNamesPair(IExecTestCasePO execNode,
         ICompNamesPairPO pair, IUpdater updater) {
 
         final INodePO execParent = execNode.getParentNode();
         if (execParent instanceof ISpecTestCasePO) {
             ISpecTestCasePO parent = (ISpecTestCasePO)execParent;
             for (IExecTestCasePO parentExec : findExecNodes(parent, execNode)) {
 
                 updater.updateParentExecTestCase(parentExec, pair);
             }
         }
 
         boolean update = updater.updateCompNamesPair(pair);
         if (update && execNode.getCompNamesPair(pair.getFirstName()) == null) {
             execNode.addCompNamesPair(pair);
         }
         return update;
     }
 
     /**
      * Updates the passed component name pair by setting the propagated
      * property. The method also updates the passed test execution node (by
      * adding the pair if required) and its parent execution nodes.
      * 
      * @param execNode
      *            The test execution node
      * @param pair
      *            The component name pair
      * @param propagated
      *            The property to update
      * @return <code>true</code> if the propagated property has been updated,
      *         <code>false</code> otherwise
      */
     public boolean updateCompNamesPair(IExecTestCasePO execNode,
         ICompNamesPairPO pair, boolean propagated) {
         return updateCompNamesPair(execNode, pair, new PropagatedUpdater(
             propagated));
     }
 
     /**
      * Updates the passed component name pair by setting the second name
      * property (that means, the overriding component name. The method also
      * updates the passed test execution node (by adding the pair if required)
      * and its parent execution nodes.
      * 
      * @param execNode The test execution node
      * @param pair The component name pair
      * @param secondCompName The second component name
      * @param compMapper business process for componentNames.    
      * @return <code>true</code> if the name property has been updated,
      *         <code>false</code> otherwise
      */
     public boolean updateCompNamesPair(IExecTestCasePO execNode,
         ICompNamesPairPO pair, String secondCompName, 
         IWritableComponentNameMapper compMapper) 
         throws IncompatibleTypeException, PMException {
 
         if (secondCompName == null 
                 || StringConstants.EMPTY.equals(secondCompName)) {
             return false;
         }
         String secondName = 
             compMapper.getCompNameCache().getGuidForName(secondCompName);
         if (StringUtils.equals(secondName, pair.getSecondName())) {
             return false;
         }
 
         
         
         if (secondName == null) {
             final IComponentNamePO newComponentNamePO = 
                 compMapper.getCompNameCache().createComponentNamePO(
                         secondCompName, pair.getType(), 
                         CompNameCreationContext.OVERRIDDEN_NAME);
             newComponentNamePO.setParentProjectId(
                     execNode.getParentProjectId());
             secondName = newComponentNamePO.getGuid();
         }
 
         compMapper.changeReuse(pair, pair.getSecondName(), secondName);
 
         return updateCompNamesPair(execNode, pair, new SecondNameUpdater(
                 secondName));
     }
 
     /**
      * @param pair the current compNamesPairPO
      * @param node the node to search comp type in
      * @return true, if comp type was found
      */
     public static boolean searchCompType(
             final ICompNamesPairPO pair, Object node) {
         
         if (node instanceof IExecTestCasePO 
                 && ((IExecTestCasePO)node).getSpecTestCase() != null) {
             for (Object childNode : ((IExecTestCasePO)node).getSpecTestCase()
                 .getUnmodifiableNodeList()) {
 
                 if (childNode instanceof IExecTestCasePO) {
                     IExecTestCasePO exec = (IExecTestCasePO)childNode;
                     for (ICompNamesPairPO cnp : exec.getCompNamesPairs()) {
                         if (cnp.getSecondName().equals(pair.getFirstName())
                             && cnp.isPropagated()) {
 
                             pair.setType(cnp.getType());
                             if (pair.getType() != null && !StringConstants.EMPTY
                                     .equals(pair.getType())) {
                                 return true;
                             } 
                             boolean retVal = searchCompType(cnp, exec);
                             pair.setType(cnp.getType());
                             return retVal;
                         }                    
                     }                    
                 } else if (childNode instanceof ICapPO) {
                     ICapPO cap = (ICapPO)childNode;
                     if (cap.getComponentName().equals(pair.getFirstName())) {
                         pair.setType(cap.getComponentType());
                         return true;
                     }
                 }
             }
         }
 
         return false;
     }
     
     /**
      * Finds the component name of the passed test step. The method searches for
      * the name in the passed tree path, which is exepected to be a top-down
      * path to the <code>capNode</code>. The <code>capNode</code> itself
      * may or may not be included as the last element of the list. Usually, the
      * tree path is determined by calling
      * {@link org.eclipse.jubula.client.core.utils.ITreeTraverserContext#getCurrentTreePath()}.
      * The result contains the component name and the node that is responsible
      * for defining the component name. This is the test step itself or the
      * execution node with the latest overriding. If a name is propagated
      * (overriden or not), the parent node is always responsible.
      * 
      * @param treePath
      *            The tree path
      * @param compNameDefiner
      *            The node that is using the component name
      * @param compNameCache
      *            The cache to use in order to resolve Component Name 
      *            references.
      * @param compNameGuid
      *            The GUID of the component name.
      * @return The result containing the component name and the responsible node
      */
     public CompNameResult findCompName(List<INodePO> treePath, 
             INodePO compNameDefiner, String compNameGuid, 
             IComponentNameCache compNameCache) {
         String currentName = compNameGuid;
         IComponentNamePO currentNamePo = 
             compNameCache.getCompNamePo(currentName);
         if (currentNamePo != null) {
             currentName = currentNamePo.getGuid();
         }
         return findCompName(treePath, compNameCache, currentName,
                 compNameDefiner);
     }
 
     /**
      * @param treePath
      *            The tree path
      * @param compNameCache
      *            The cache to use in order to resolve Component Name 
      *            references.
      * @param originalName
      *            The GUID of the component name.
      * @param originalCompNameDefiner
      *            The node that is using the component name
      * @return The result containing the component name and the responsible node
      */
     private CompNameResult findCompName(List<INodePO> treePath,
             IComponentNameCache compNameCache, String originalName,
             INodePO originalCompNameDefiner) {
         
         String currentName = originalName;
         INodePO compNameDefiner = originalCompNameDefiner;
         IComponentNamePO currentNamePo;
         ListIterator<INodePO> it = treePath.listIterator(treePath.size());
         while (it.hasPrevious()) {
             INodePO node = it.previous();
             if (node instanceof IExecTestCasePO) {
                 IExecTestCasePO execNode = (IExecTestCasePO)node;
                 ICompNamesPairPO pair = execNode.getCompNamesPair(currentName);
                 if (pair != null) {
                     currentName = pair.getSecondName();
                     currentNamePo = 
                         compNameCache.getCompNamePo(currentName);
                     if (currentNamePo != null) {
                         currentName = currentNamePo.getGuid();
                     }
                     if (pair.isPropagated()) {
                         int index = it.previousIndex();
                         if (index > -1) {
                             compNameDefiner = treePath.get(index);
                         }
                     } else {
                         compNameDefiner = execNode;
                         break;
                     }
                 }
             }
         }
 
         return new CompNameResult(currentName, compNameDefiner);
     }
 
     /**
      * Removes incorrect CompNamePairs from children of the given node.
      * 
      * @param node CompNamePairs for children of this node will be analyzed.
      */
     public static void removeIncorrectCompNamePairs(INodePO node) {
         for (Object o : node.getUnmodifiableNodeList()) {
             if (o instanceof IExecTestCasePO) {
                 IExecTestCasePO exec = (IExecTestCasePO)o;
                for (ICompNamesPairPO pair : exec.getCompNamesPairs()) {
                     searchCompType(pair, exec);
                     if (pair.getType().length() == 0) {
                         exec.removeCompNamesPair(pair.getFirstName());
                     }
                 }
             }
         }
     }
 
 }

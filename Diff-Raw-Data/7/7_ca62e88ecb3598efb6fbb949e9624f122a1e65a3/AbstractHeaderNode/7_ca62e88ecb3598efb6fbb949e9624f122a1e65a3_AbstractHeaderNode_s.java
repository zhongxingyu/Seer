 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.core.database.nodes;
 
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser.Order;
 
 /**
  * Abstract Class for Header Cell of Table
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 public abstract class AbstractHeaderNode extends CellNode {
     
     /**
      * Name of 'Last Cell ID' property
      */
     private static final String LAST_CELL_ID = "Last Cell ID";
     
     /**
      * Neo Service
      */
     private NeoService neoService = NeoServiceProvider.getProvider().getService();
 
     /**
      * Creates a Header node from Neo Node
      * 
      * @param node node 
      */
     public AbstractHeaderNode(Node node) {
         super(node);
     }
     
     /**
      * Creates a Header node from Neo Node and sets an index
      * 
      * @param node node 
      * @param index index of this Header
      */
     public AbstractHeaderNode(Node node, int index) {
         super(node);
         setIndex(index);
     }
     
     /**
      * Sets an index of this Header
      *
      * @param index index of Header
      */
     public abstract void setIndex(int index);
     
     /**
      * Sets last Cell id in this Header
      *
      * @param lastCellId id of Last Cell in this line
      */
     public void setLastCellId(long lastCellId) {
         setParameter(LAST_CELL_ID, lastCellId);
     }
     
     /**
      * Returns last Cell id
      *
      * @return id of Last Cell in this line
      */
     public Long getLastCellId() {
         return (Long)getParameter(LAST_CELL_ID);
     }
     
     /**
      * Returns type of Relationships between Cells for this Header
      *
      * @return type of Relationships between Cells
      */
     protected abstract RelationshipType getRelationshipType();
     
     /**
      * Returns Property of Cell to count
      *
      * @return property of Cell
      */
     protected abstract String getIndexProperty();
     
     /**
      * Returns the Index of this Header
      *
      * @return index of Header
      */
     protected abstract int getIndex();
     
     /**
      * Compute an Index of Cell for this Header
      *
      * @param node Cell's node
      * @return index of Cell
      */
     private int getCellIndex(Node node) {
         return (Integer)node.getProperty(getIndexProperty());
     }
     
     /**
      * Adds a new Cell to this index
      *
      * @param newCellNode cell to add
      */
     public void addNextCell(CellNode newCellNode) {
         //compute a previous node for this cell
         Node previousCellNode = getPreviousNode(newCellNode.getUnderlyingNode());
         
         if (previousCellNode != null) {
             //if previous node exists than insert a new node 
             Relationship relationship = previousCellNode.getSingleRelationship(getRelationshipType(), Direction.INCOMING);
             
             relationship.getStartNode().createRelationshipTo(newCellNode.getUnderlyingNode(), getRelationshipType());
             newCellNode.getUnderlyingNode().createRelationshipTo(relationship.getEndNode(), getRelationshipType());
             relationship.delete();
         }
         else {
             //if previous node didn't exist than add this cell to the end of Header's Cells
             Long lastCellId = getLastCellId();
             if (lastCellId == null) {
                 //if there are no last cells, than last cell is current header cell
                 previousCellNode = getUnderlyingNode();
             }
             else {
                 previousCellNode = neoService.getNodeById(lastCellId);
             }
             
             previousCellNode.createRelationshipTo(newCellNode.getUnderlyingNode(), getRelationshipType());
             setLastCellId(newCellNode.getUnderlyingNode().getId());
         }
     }
     
     /**
      * Computes Previous Cell node
      *
      * @param newCellNode node of new cell
      * @return node of previous cell
      */
     private Node getPreviousNode(Node newCellNode) {
         NextNodeIterator nodes = new NextNodeIterator(getCellIndex(newCellNode));
         
         if (nodes.hasNext()) {
             return nodes.next().getUnderlyingNode();           
         }
         else {
             return null;
         }
     }
     
     /**
      * Iterator to search previous Cell
      * 
      * @author Lagutko_N
      * @since 1.0.0
      */
     private class NextNodeIterator extends AbstractIterator<CellNode> {
         
         /*
          * Is current node should be returned 
          */
         private boolean isReturnableNode = false;
         
         /**
          * Constructor for this Iterator
          * 
          * @param newCellIndex index of New cell
          */
         public NextNodeIterator(final int newCellIndex) {
             this.iterator = node.traverse(Order.DEPTH_FIRST, 
                                           new StopEvaluator(){
                                             
                                             @Override
                                             public boolean isStopNode(TraversalPosition currentPos) {
                                                 isReturnableNode =  getCellIndex(currentPos.currentNode()) > newCellIndex;
                                                 return isReturnableNode;
                                             }
                                         }, 
                                         new ReturnableEvaluator(){
                                             
                                             @Override
                                             public boolean isReturnableNode(TraversalPosition currentPos) {
                                                 return isReturnableNode;
                                             }
                                         }, 
                                         getRelationshipType(),
                                         Direction.OUTGOING).iterator();
         }
 
         @Override
         protected CellNode wrapNode(Node node) {
             return CellNode.fromNode(node);
         }
         
     }
 }

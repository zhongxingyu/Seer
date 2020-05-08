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
 
 package org.amanzi.awe.views.reuse.views;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 import org.amanzi.awe.views.reuse.Distribute;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 
 /**
  * <p>
  * Information about column
  * </p>.
  *
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class Column implements Comparable<Column> {
 
     /** The min value. */
     private Double minValue;
     
     /** The range. */
     private Double range;
     
     /** The node. */
     private Node node;
     
     /** The distribute. */
     private Distribute distribute;
     
     /** The property value. */
     private Object propertyValue;
 
     /**
      * Constructor.
      *
      * @param curValue - minimum number which enters into a column
      * @param range - range of column
      */
     public Column(double curValue, double range) {
         minValue = curValue;
         this.range = range;
         node = null;
     }
 
     /**
      * Merge.
      *
      * @param prevCol the prev col
      */
     public void merge(Column prevCol,GraphDatabaseService service) {
         minValue = prevCol.minValue;
         range += prevCol.range;
         node.setProperty(INeoConstants.PROPERTY_NAME_MIN_VALUE, minValue);
         node.setProperty(INeoConstants.PROPERTY_NAME_MAX_VALUE, minValue + range);
         Node prevNode = prevCol.getNode();
         for (Relationship relation : prevNode.getRelationships(NetworkRelationshipTypes.AGGREGATE, Direction.OUTGOING)) {
             node.createRelationshipTo(relation.getOtherNode(node), NetworkRelationshipTypes.AGGREGATE);
         }
         GeoNeoRelationshipTypes linkType = GeoNeoRelationshipTypes.CHILD;
         Relationship parentLink = prevNode.getSingleRelationship(linkType, Direction.INCOMING);
         if(parentLink==null){
             linkType = GeoNeoRelationshipTypes.NEXT;
             parentLink = prevNode.getSingleRelationship(linkType, Direction.INCOMING);
         }
         Node parentMain = parentLink.getOtherNode(prevNode);
         NeoUtils.deleteSingleNode(prevNode,service);
         node.setProperty(INeoConstants.PROPERTY_NAME_NAME, getColumnName());
         prevCol.setNode(null);
         parentMain.createRelationshipTo(node, linkType);
         setSpacer(true);
     }
 
     /**
      * Sets the node.
      *
      * @param node the new node
      */
     private void setNode(Node node) {
         this.node = node;
     }
 
     /**
      * Sets the value.
      *
      * @param countNode the new value
      */
     public void setValue(int countNode) {
         if (node != null) {
             node.setProperty(INeoConstants.PROPERTY_VALUE_NAME, countNode);
         }
     }
 
     /**
      * Gets the node.
      *
      * @return the node
      */
     public Node getNode() {
         return node;
     }
 
     /**
      * Instantiates a new column.
      *
      * @param aggrNode the aggr node
      * @param lastNode the last column node
      * @param curValue the cur value
      * @param range the range
      * @param distribute the distribute
      * @param propertyValue the property value
      */
     public Column(Node aggrNode, Node lastNode, double curValue, double range, Distribute distribute, Object propertyValue,GraphDatabaseService service) {
         this(curValue, range);
         this.distribute = distribute;
         this.propertyValue = propertyValue;
         node = service.createNode();
         node.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.COUNT.getId());
         node.setProperty(INeoConstants.PROPERTY_NAME_NAME, getColumnName());
         node.setProperty(INeoConstants.PROPERTY_NAME_MIN_VALUE, minValue);
         node.setProperty(INeoConstants.PROPERTY_NAME_MAX_VALUE, minValue + range);
         node.setProperty(INeoConstants.PROPERTY_VALUE_NAME, 0);
         node.setProperty(INeoConstants.PROPERTY_AGGR_PARENT_ID, aggrNode.getId());
         NeoUtils.addChild(aggrNode, node, aggrNode.equals(lastNode)?null:lastNode, service);
 
     }
 
     /**
      * Gets the column name.
      *
      * @return the column name
      */
     private String getColumnName() {
         String nameCol;
 
         BigDecimal minValue = new BigDecimal(this.minValue);
         BigDecimal maxValue = new BigDecimal(this.minValue + this.range);
         if (propertyValue instanceof String) {
             return propertyValue.toString();
         }
         if (distribute == Distribute.INTEGERS) {
             nameCol = (minValue.add(new BigDecimal(0.5))).setScale(0, RoundingMode.HALF_UP).toString();
         } else if (propertyValue instanceof Integer) {
            minValue = minValue.setScale(0, RoundingMode.CEILING);
            maxValue = maxValue.setScale(0, RoundingMode.FLOOR);
            if (maxValue.subtract(minValue).compareTo(BigDecimal.ONE) < 0) {
                 nameCol = minValue.toString();
             } else {
                 nameCol = minValue.toString() + "-" + maxValue.toString();
             }
 
         } else {
             // TODO calculate scale depending on key.getRange()
             minValue = minValue.setScale(3, RoundingMode.HALF_UP);
             maxValue = maxValue.setScale(3, RoundingMode.HALF_UP);
             if (range == 0) {
                 nameCol = minValue.toString();
             } else {
                 nameCol = minValue.toString() + "-" + maxValue.toString();
             }
         }
         return nameCol;
     }
 
     /**
      * Set this column to be a chart spacer (no data).
      *
      * @param value the new spacer
      */
     public void setSpacer(boolean value) {
         node.setProperty("spacer", value);
     }
 
     /**
      * Returns true if value in [minValue,minValue+range);.
      *
      * @param value the value
      * @return true, if successful
      */
     public boolean containsValue(double value) {
         return value >= minValue && (range == 0 || value < minValue + range);
     }
 
     /**
      * Compare to.
      *
      * @param o the o
      * @return the int
      */
     @Override
     public int compareTo(Column o) {
         return minValue.compareTo(o.minValue);
     }
 
     /**
      * Hash code.
      *
      * @return the int
      */
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((minValue == null) ? 0 : minValue.hashCode());
         return result;
     }
 
     /**
      * Equals.
      *
      * @param obj the obj
      * @return true, if successful
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Column other = (Column)obj;
         if (minValue == null) {
             if (other.minValue != null)
                 return false;
         } else if (!minValue.equals(other.minValue))
             return false;
         return true;
     }
 
     /**
      * Gets the min value.
      *
      * @return the min value
      */
     public Double getMinValue() {
         return minValue;
     }
 
     /**
      * Sets the min value.
      *
      * @param minValue the new min value
      */
     public void setMinValue(Double minValue) {
         this.minValue = minValue;
     }
 
     /**
      * Gets the range.
      *
      * @return the range
      */
     public Double getRange() {
         return range;
     }
 
     /**
      * Sets the range.
      *
      * @param range the new range
      */
     public void setRange(Double range) {
         this.range = range;
     }
 
     /**
      * Gets the distribute.
      *
      * @return the distribute
      */
     public Distribute getDistribute() {
         return distribute;
     }
 
     /**
      * Sets the distribute.
      *
      * @param distribute the new distribute
      */
     public void setDistribute(Distribute distribute) {
         this.distribute = distribute;
     }
 
     /**
      * Gets the property value.
      *
      * @return the property value
      */
     public Object getPropertyValue() {
         return propertyValue;
     }
 
     /**
      * Sets the property value.
      *
      * @param propertyValue the new property value
      */
     public void setPropertyValue(Object propertyValue) {
         this.propertyValue = propertyValue;
     }
 
 }
 

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
 
 package org.amanzi.neo.services.correlation;
 
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Set;
 
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 
 /**
  * <p>
  * Data keeper for correlation
  * </p>
  * .
  * 
  * @author Saelenchits_N
  * @since 1.0.0
  */
 public class CorrelationModel {
 
     /** The network. */
     private final Node network;
 
     /** The datasets. */
     private Set<Node> datasets;
     private HashMap<Node, Relationship> relationshipsMap;
     /**
      * Instantiates a new correlation model.
      * 
      * @param networkNode the network node
      * @param datasets the datasets
      */
     public CorrelationModel(Node networkNode, Set<Node> datasets){
         network = networkNode;
         this.datasets = datasets;
     }
 
     public CorrelationModel(Node networkNode, HashMap<Node, Relationship> relationshipsMap) {
         network = networkNode;
         this.relationshipsMap = relationshipsMap;
        this.datasets = new HashSet<Node>();
        this.datasets.addAll(relationshipsMap.keySet());
     }
 
     /**
      * Gets the network.
      * 
      * @return Returns the network.
      */
     public Node getNetwork() {
         return network;
     }
 
     /**
      * Gets the datasets.
      * 
      * @return Returns the datasets.
      */
     public Set<Node> getDatasets() {
         return datasets;
     }
 
     /**
      * Sets the datasets.
      * 
      * @param datasets The datasets to set.
      */
     public void setDatasets(Set<Node> datasets) {
         this.datasets = datasets;
     }
 
     /**
      * @return Returns the relationshipsMap.
      */
     public HashMap<Node, Relationship> getRelationshipsMap() {
         return relationshipsMap;
     }
 
     /**
      * @param relationshipsMap The relationshipsMap to set.
      */
     public void setRelationshipsMap(HashMap<Node, Relationship> relationshipsMap) {
         this.relationshipsMap = relationshipsMap;
     }
 
 }

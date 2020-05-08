 package org.amanzi.awe.views.drive.views;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.amanzi.awe.views.network.proxy.NeoNode;
 import org.amanzi.awe.views.network.proxy.Root;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.SplashRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 
 /**
  * Proxy class that provides access for Neo-database
  * 
  * @since 1.1.0
  */
 public class DriveRoot extends Root {
 
     /**
      * Constructor
      * 
      * @param serviceProvider service Provider
      */
     public DriveRoot(NeoServiceProvider serviceProvider) {
         super(serviceProvider);
     }
 
     /**
      * Returns all DRIVE Nodes of database
      */
 
     public NeoNode[] getChildren() {
         ArrayList<NeoNode> driveNodes = new ArrayList<NeoNode>();
 
         NeoService service = serviceProvider.getService();
 
         Transaction transaction = service.beginTx();
         try {
             Node reference = service.getReferenceNode();
             Traverser rootDriveTraverse = reference.traverse(Order.DEPTH_FIRST, new StopEvaluator() {
 
                 @Override
                 public boolean isStopNode(TraversalPosition currentPos) {
                     return currentPos.depth() >= 2;
                 }
             }, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Relationship lastRelationshipTraversed = currentPos.lastRelationshipTraversed();
                     return currentPos.depth() > 1
                             && lastRelationshipTraversed.getStartNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(
                                     INeoConstants.AWE_PROJECT_NODE_TYPE)
                             && (lastRelationshipTraversed.getEndNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(
                                     INeoConstants.DATASET_TYPE_NAME) || (lastRelationshipTraversed.getEndNode().getProperty(
                                    INeoConstants.PROPERTY_TIME_NAME, "").equals(INeoConstants.FILE_TYPE_NAME)));
                 }
            }, SplashRelationshipTypes.AWE_PROJECT, Direction.OUTGOING, NetworkRelationshipTypes.CHILD, Direction.OUTGOING,
                    SplashRelationshipTypes.AWE_PROJECT, Direction.OUTGOING, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
             for (Node node : rootDriveTraverse) {
                 driveNodes.add(new DriveNeoNode(node));
             }
 
             transaction.success();
         } finally {
             transaction.finish();
         }
 
         if (driveNodes.isEmpty()) {
             return NO_NODES;
         } else {
             Collections.sort(driveNodes, new NeoNodeComparator());
             return driveNodes.toArray(NO_NODES);
         }
     }
 
 }

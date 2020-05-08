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
 
 package org.amanzi.neo.services;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.amanzi.neo.db.manager.DatabaseManager;
 import org.amanzi.neo.db.manager.IDatabaseChangeListener;
 import org.amanzi.neo.db.manager.INeoDbService;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.traversal.PruneEvaluator;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.graphdb.traversal.Uniqueness;
 import org.neo4j.helpers.Predicate;
 import org.neo4j.index.IndexService;
 import org.neo4j.kernel.Traversal;
 
 /**
  * TODO Purpose of 
  * <p>
  *
  * </p>
  * @author Lagutko_N
  * @since 1.0.0
  */
 public abstract class AbstractService implements IDatabaseChangeListener {
 
     protected GraphDatabaseService databaseService;
     
    private boolean updateService = true;
    
     public AbstractService() {
         DatabaseManager.getInstance().addDatabaseChangeListener(this);
         this.databaseService = DatabaseManager.getInstance().getCurrentDatabaseService();
     }
     
     public AbstractService(GraphDatabaseService databaseService) {
         this.databaseService = databaseService;
        updateService = false;
     }
     
     public void onDatabaseAccessChange() {
        if (updateService) {
            this.databaseService = DatabaseManager.getInstance().getCurrentDatabaseService();
        }
     }
     
     public IndexService getIndexService() {
         return DatabaseManager.getInstance().getIndexService();
     }
 
     public void deleteTree(INeoDbService service, final Node root) {
         TraversalDescription deleteDescription = Traversal.description().uniqueness(Uniqueness.NONE).prune(new PruneEvaluator() {
 
             @Override
             public boolean pruneAfter(Path paramPath) {
                 return paramPath.lastRelationship().getEndNode().equals(paramPath.startNode());
             }
         }).depthFirst().filter(new Predicate<Path>() {
 
             @Override
             public boolean accept(Path paramT) {
                 return !paramT.endNode().hasRelationship(Direction.OUTGOING);
             }
         });
         Set<Node> cacheDelete = new HashSet<Node>();
         int size;
         do {
             Iterator<Node> iterator = deleteDescription.traverse(root).nodes().iterator();
             while (iterator.hasNext()) {
                 Node nodeToDelete = iterator.next();
                 cacheDelete.add(nodeToDelete);
                 if (nodeToDelete.equals(root)){
                     deleteCache(service, cacheDelete);
                     return;
                 }
                 if (cacheDelete.size() > 100) {
                     deleteCache(service, cacheDelete);
                     iterator = deleteDescription.traverse(root).nodes().iterator();
                 }
             }
             size = cacheDelete.size();
             deleteCache(service, cacheDelete);
         } while (size!=0);
     }
     /**
      *
      * @param cacheDelete
      */
     private void deleteCache(INeoDbService service,Set<Node> cacheDelete) {
         Transaction tx = service.beginTx();
         try{
             Iterator<Node> iterator = cacheDelete.iterator();
             while(iterator.hasNext()){
                 Node node=iterator.next();
                 deleteNode(service, node);
                 iterator.remove();
             }
             tx.success();
         }finally{
             tx.finish();
         }
         
     }
 
     public void deleteNode(INeoDbService service,Node node){
         for (Relationship relation:node.getRelationships()){
             service.delete(relation);
         }
         service.delete(node);
     }
 }

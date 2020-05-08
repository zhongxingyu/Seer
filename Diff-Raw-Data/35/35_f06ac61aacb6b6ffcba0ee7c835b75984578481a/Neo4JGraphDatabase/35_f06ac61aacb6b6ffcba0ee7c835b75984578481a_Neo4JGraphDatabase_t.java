 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 package org.geogit.storage;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.geogit.api.ObjectId;
 import org.geogit.api.Platform;
 import org.geogit.api.plumbing.ResolveGeogitDir;
 import org.neo4j.graphalgo.GraphAlgoFactory;
 import org.neo4j.graphalgo.PathFinder;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.factory.GraphDatabaseFactory;
 import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.UniqueFactory;
 import org.neo4j.graphdb.traversal.Evaluation;
 import org.neo4j.graphdb.traversal.Evaluator;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.graphdb.traversal.Traverser;
 import org.neo4j.kernel.Traversal;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.google.inject.Inject;
 
 /**
  * Provides an implementation of a GeoGit Graph Database using Neo4J.
  */
 public class Neo4JGraphDatabase extends AbstractGraphDatabase {
 
     protected GraphDatabaseService graphDB = null;
 
     protected String dbPath;
 
     protected static Map<String, ServiceContainer> databaseServices = new ConcurrentHashMap<String, ServiceContainer>();
 
     private final Platform platform;
 
     /**
      * Container class for the database service to keep track of reference counts.
      */
     protected class ServiceContainer {
         private GraphDatabaseService dbService;
 
         private int refCount;
 
         public ServiceContainer(GraphDatabaseService dbService) {
             this.dbService = dbService;
             this.refCount = 0;
         }
 
         public void removeRef() {
             this.refCount--;
         }
 
         public void addRef() {
             this.refCount++;
         }
 
         public int getRefCount() {
             return this.refCount;
         }
 
         public GraphDatabaseService getService() {
             return this.dbService;
         }
     }
 
     private enum CommitRelationshipTypes implements RelationshipType {
         TOROOT, PARENT, MAPPED_TO
     }
 
     static {
         // Registers a shutdown hook for the Neo4j instance so that it
         // shuts down nicely when the VM exits (even if you "Ctrl-C" the
         // running example before it's completed)
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 for (Entry<String, ServiceContainer> entry : databaseServices.entrySet()) {
                     File graphPath = new File(entry.getKey());
                     if (graphPath.exists()) {
                         entry.getValue().getService().shutdown();
                     }
                 }
                 databaseServices.clear();
             }
         });
     }
 
     /**
      * Constructs a new {@code Neo4JGraphDatabase} using the given platform.
      * 
      * @param platform the platform to use.
      */
     @Inject
     public Neo4JGraphDatabase(final Platform platform) {
         this.platform = platform;
     }
 
     /**
      * Opens the Neo4J graph database.
      */
     @Override
     public void open() {
         if (isOpen()) {
             return;
         }
         URL envHome = new ResolveGeogitDir(platform).call();
         if (envHome == null) {
             throw new IllegalStateException("Not inside a geogit directory");
         }
         if (!"file".equals(envHome.getProtocol())) {
             throw new UnsupportedOperationException(
                     "This Graph Database works only against file system repositories. "
                             + "Repository location: " + envHome.toExternalForm());
         }
         File repoDir;
         try {
             repoDir = new File(envHome.toURI());
         } catch (URISyntaxException e) {
             throw Throwables.propagate(e);
         }
         File graph = new File(repoDir, "graph");
         if (!graph.exists() && !graph.mkdir()) {
             throw new IllegalStateException("Cannot create graph directory '"
                     + graph.getAbsolutePath() + "'");
         }
 
         dbPath = graph.getAbsolutePath() + "/graphDB.db";
 
         if (databaseServices.containsKey(dbPath)) {
             ServiceContainer serviceContainer = databaseServices.get(dbPath);
             serviceContainer.addRef();
             graphDB = serviceContainer.getService();
         } else {
             graphDB = getGraphDatabase();
             ServiceContainer newContainer = new ServiceContainer(graphDB);
             newContainer.addRef();
             databaseServices.put(dbPath, newContainer);
         }
 
     }
 
     /**
      * Constructs the graph database service.
      * 
      * @return the new {@link GraphDatabaseService}
      */
     protected GraphDatabaseService getGraphDatabase() {
         return new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
     }
 
     /**
      * Destroy the graph database service. This will only happen when the ref count for the database
      * service is 0.
      */
     protected void destroyGraphDatabase() {
         File graphPath = new File(dbPath);
         if (graphPath.exists()) {
             graphDB.shutdown();
         }
         databaseServices.remove(dbPath);
     }
 
     /**
      * @return true if the database is open, false otherwise
      */
     @Override
     public boolean isOpen() {
         return graphDB != null;
     }
 
     /**
      * Closes the database.
      */
     @Override
     public void close() {
         if (isOpen()) {
             ServiceContainer container = databaseServices.get(dbPath);
             container.removeRef();
             if (container.getRefCount() <= 0) {
                 destroyGraphDatabase();
             }
             graphDB = null;
         }
 
     }
 
     /**
      * Determines if the given commit exists in the graph database.
      * 
      * @param commitId the commit id to search for
      * @return true if the commit exists, false otherwise
      */
     @Override
     public boolean exists(ObjectId commitId) {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
         Node node = idIndex.get("id", commitId.toString()).getSingle();
         return node != null;
     }
 
     /**
      * Retrieves all of the parents for the given commit.
      * 
      * @param commitid the commit whose parents should be returned
      * @return a list of the parents of the provided commit
      * @throws IllegalArgumentException
      */
     @Override
     public ImmutableList<ObjectId> getParents(ObjectId commitId) throws IllegalArgumentException {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
         Node node = idIndex.get("id", commitId.toString()).getSingle();
 
         Builder<ObjectId> listBuilder = new ImmutableList.Builder<ObjectId>();
 
         if (node != null) {
             for (Relationship parent : node.getRelationships(Direction.OUTGOING,
                     CommitRelationshipTypes.PARENT)) {
                 Node parentNode = parent.getOtherNode(node);
                 listBuilder.add(ObjectId.valueOf((String) parentNode.getProperty("id")));
             }
         }
         return listBuilder.build();
     }
 
     private ImmutableList<Node> getParentNodes(final Node commitNode) {
         Builder<Node> listBuilder = new ImmutableList.Builder<Node>();
 
         if (commitNode != null) {
             for (Relationship parent : commitNode.getRelationships(Direction.OUTGOING,
                     CommitRelationshipTypes.PARENT)) {
                 listBuilder.add(parent.getOtherNode(commitNode));
             }
         }
         return listBuilder.build();
     }
 
     /**
      * Retrieves all of the children for the given commit.
      * 
      * @param commitid the commit whose children should be returned
      * @return a list of the children of the provided commit
      * @throws IllegalArgumentException
      */
     @Override
     public ImmutableList<ObjectId> getChildren(ObjectId commitId) throws IllegalArgumentException {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
         Node node = idIndex.get("id", commitId.toString()).getSingle();
 
         Builder<ObjectId> listBuilder = new ImmutableList.Builder<ObjectId>();
 
         for (Relationship child : node.getRelationships(Direction.INCOMING,
                 CommitRelationshipTypes.PARENT)) {
             Node childNode = child.getOtherNode(node);
             listBuilder.add(ObjectId.valueOf((String) childNode.getProperty("id")));
         }
         return listBuilder.build();
     }
 
     /**
      * Adds a commit to the database with the given parents. If a commit with the same id already
      * exists, it will not be inserted.
      * 
      * @param commitId the commit id to insert
      * @param parentIds the commit ids of the commit's parents
      * @return true if the commit id was inserted, false otherwise
      */
     @Override
     public boolean put(ObjectId commitId, ImmutableList<ObjectId> parentIds) {
         Transaction tx = graphDB.beginTx();
 
         Node commitNode = null;
         try {
             // See if it already exists
             commitNode = getOrAddNode(commitId);
 
             if (parentIds.isEmpty()) {
                 if (!commitNode
                         .getRelationships(Direction.OUTGOING, CommitRelationshipTypes.TOROOT)
                         .iterator().hasNext()) {
                     // Attach this node to the root node
                     commitNode.createRelationshipTo(graphDB.getNodeById(0),
                             CommitRelationshipTypes.TOROOT);
                 }
             }
 
             if (!commitNode.getRelationships(Direction.OUTGOING, CommitRelationshipTypes.PARENT)
                     .iterator().hasNext()) {
                 // Don't make relationships if they have been created already
                 for (ObjectId parent : parentIds) {
                     Node parentNode = getOrAddNode(parent);
                     commitNode.createRelationshipTo(parentNode, CommitRelationshipTypes.PARENT);
                 }
             }
 
             tx.success();
         } catch (Exception e) {
             tx.failure();
             throw Throwables.propagate(e);
         } finally {
             tx.finish();
         }
 
         return true;
     }
 
     /**
      * Maps a commit to another original commit. This is used in sparse repositories.
      * 
      * @param mapped the id of the mapped commit
      * @param original the commit to map to
      */
     @Override
     public void map(final ObjectId mapped, final ObjectId original) {
         Transaction tx = graphDB.beginTx();
 
         Node commitNode = null;
         try {
             // See if it already exists
             commitNode = getOrAddNode(mapped);
 
             if (commitNode.getRelationships(Direction.OUTGOING, CommitRelationshipTypes.MAPPED_TO)
                     .iterator().hasNext()) {
                 // Remove old mapping
                 commitNode.getRelationships(Direction.OUTGOING, CommitRelationshipTypes.MAPPED_TO)
                         .iterator().next().delete();
             }
 
             // Don't make relationships if they have been created already
             Node originalNode = getOrAddNode(original);
             commitNode.createRelationshipTo(originalNode, CommitRelationshipTypes.MAPPED_TO);
 
             tx.success();
         } catch (Exception e) {
             tx.failure();
             throw Throwables.propagate(e);
         } finally {
             tx.finish();
         }
     }
 
     /**
      * Gets the id of the commit that this commit is mapped to.
      * 
      * @param commitId the commit to find the mapping of
      * @return the mapped commit id
      */
     public ObjectId getMapping(final ObjectId commitId) {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
         Node node = idIndex.get("id", commitId.toString()).getSingle();
 
         ObjectId mapped = ObjectId.NULL;
         Node mappedNode = getMappedNode(node);
         if (mappedNode != null) {
             mapped = ObjectId.valueOf((String) mappedNode.getProperty("id"));
         }
         return mapped;
     }
 
     private Node getMappedNode(final Node commitNode) {
         if (commitNode != null) {
             Iterator<Relationship> mappings = commitNode.getRelationships(Direction.OUTGOING,
                     CommitRelationshipTypes.MAPPED_TO).iterator();
             if (mappings.hasNext()) {
                 return mappings.next().getOtherNode(commitNode);
             }
         }
         return null;
     }
 
     /**
      * Gets a node or adds it if it doesn't exist. Note, this must be called within a
      * {@link Transaction}.
      * 
      * @param commitId
      * @return
      */
     private Node getOrAddNode(ObjectId commitId) {
         final String commitIdStr = commitId.toString();
        UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(graphDB, "identifiers") {
            @Override
            protected void initialize(Node created, Map<String, Object> properties) {
                created.setProperty("id", properties.get("id"));
            }
        };

        return factory.getOrCreate("id", commitIdStr);
     }
 
     /**
      * Gets the number of ancestors of the commit until it reaches one with no parents, for example
      * the root or an orphaned commit.
      * 
      * @param commitId the commit id to start from
      * @return the depth of the commit
      */
     @Override
     public int getDepth(final ObjectId commitId) {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
         Node commitNode = idIndex.get("id", commitId.toString()).getSingle();
         TraversalDescription traversalDescription = Traversal.description().breadthFirst()
                 .evaluator(new Evaluator() {
                     @Override
                     public Evaluation evaluate(Path path) {
                         if (!path.endNode().hasRelationship(Direction.OUTGOING)
                                 || path.endNode().hasRelationship(CommitRelationshipTypes.TOROOT)) {
 
                             return Evaluation.INCLUDE_AND_PRUNE;
                         }
                         return Evaluation.EXCLUDE_AND_CONTINUE;
                     }
 
                 }).relationships(CommitRelationshipTypes.PARENT, Direction.OUTGOING);
         Traverser traverser = traversalDescription.traverse(commitNode);
 
         int min = Integer.MAX_VALUE;
         for (Path path : traverser) {
             int length = path.length();
             if (length < min) {
                 min = length;
             }
         }
 
         return min;
     }
 
     /**
      * Determines if there are any sparse commits between the start commit and the end commit, not
      * including the end commit.
      * 
      * @param start the start commit
      * @param end the end commit
      * @return true if there are any sparse commits between start and end
      */
     public boolean isSparsePath(ObjectId start, ObjectId end) {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
         Node startNode = idIndex.get("id", start.toString()).getSingle();
         Node endNode = idIndex.get("id", end.toString()).getSingle();
         PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
                 Traversal.expanderForTypes(CommitRelationshipTypes.PARENT, Direction.OUTGOING),
                 Integer.MAX_VALUE);
 
         Iterable<Path> paths = finder.findAllPaths(startNode, endNode);
 
         for (Path path : paths) {
             Node lastNode = path.lastRelationship().getOtherNode(path.endNode());
             Node mappedNode = getMappedNode(lastNode);
             if (mappedNode != null) {
                 ImmutableList<Node> parentNodes = getParentNodes(mappedNode);
                 for (Node parentNode : parentNodes) {
                     // If these nodes aren't represented, they were excluded and the path is sparse
                     Node mappedParentNode = getMappedNode(parentNode);
                     if (getMappedNode(mappedParentNode).getId() != parentNode.getId()) {
                         return true;
                     }
                 }
             }
             for (Node node : path.nodes()) {
                 if (!node.equals(endNode) && node.hasProperty(SPARSE_FLAG)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Set a property on the provided commit node.
      * 
      * @param commitId the id of the commit
      */
     public void setProperty(ObjectId commitId, String propertyName, String propertyValue) {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
 
         Transaction tx = graphDB.beginTx();
         try {
             Node commitNode = idIndex.get("id", commitId.toString()).getSingle();
             commitNode.setProperty(propertyName, propertyValue);
 
             tx.success();
         } catch (Exception e) {
             tx.failure();
             throw Throwables.propagate(e);
         } finally {
             tx.finish();
         }
 
     }
 
     /**
      * Finds the lowest common ancestor of two commits.
      * 
      * @param leftId the commit id of the left commit
      * @param rightId the commit id of the right commit
      * @return An {@link Optional} of the lowest common ancestor of the two commits, or
      *         {@link Optional#absent()} if a common ancestor could not be found.
      */
     @Override
     public Optional<ObjectId> findLowestCommonAncestor(ObjectId leftId, ObjectId rightId) {
         Index<Node> idIndex = graphDB.index().forNodes("identifiers");
 
         Set<Node> leftSet = new HashSet<Node>();
         Set<Node> rightSet = new HashSet<Node>();
 
         Queue<Node> leftQueue = new LinkedList<Node>();
         Node leftNode = idIndex.get("id", leftId.toString()).getSingle();
         if (!leftNode.hasRelationship(Direction.OUTGOING)) {
             return Optional.absent();
         }
         leftQueue.add(leftNode);
         Queue<Node> rightQueue = new LinkedList<Node>();
         Node rightNode = idIndex.get("id", rightId.toString()).getSingle();
         if (!rightNode.hasRelationship(Direction.OUTGOING)) {
             return Optional.absent();
         }
         rightQueue.add(rightNode);
         List<Node> potentialCommonAncestors = new LinkedList<Node>();
         while (!leftQueue.isEmpty() || !rightQueue.isEmpty()) {
             if (!leftQueue.isEmpty()) {
                 Node commit = leftQueue.poll();
                 if (processCommit(commit, leftQueue, leftSet, rightQueue, rightSet)) {
                     potentialCommonAncestors.add(commit);
                 }
             }
             if (!rightQueue.isEmpty()) {
                 Node commit = rightQueue.poll();
                 if (processCommit(commit, rightQueue, rightSet, leftQueue, leftSet)) {
                     potentialCommonAncestors.add(commit);
                 }
             }
         }
         verifyAncestors(potentialCommonAncestors, leftSet, rightSet);
 
         Optional<ObjectId> ancestor = Optional.absent();
         if (potentialCommonAncestors.size() > 0) {
             ancestor = Optional.of(ObjectId.valueOf((String) potentialCommonAncestors.get(0)
                     .getProperty("id")));
         }
         return ancestor;
     }
 
     /**
      * Helper method for {@link #findLowestCommonAncestor(ObjectId, ObjectId)}.
      */
     private boolean processCommit(Node commit, Queue<Node> myQueue, Set<Node> mySet,
             Queue<Node> theirQueue, Set<Node> theirSet) {
         if (!mySet.contains(commit)) {
             mySet.add(commit);
             if (theirSet.contains(commit)) {
                 // found a common ancestor
                 stopAncestryPath(commit, theirQueue, theirSet);
 
                 return true;
             }
             for (Relationship parent : commit.getRelationships(Direction.OUTGOING,
                     CommitRelationshipTypes.PARENT)) {
                 if (parent.getEndNode().hasRelationship(Direction.OUTGOING)) {
                     myQueue.add(parent.getEndNode());
                 }
             }
         }
         return false;
     }
 
     /**
      * Helper method for {@link #findLowestCommonAncestor(ObjectId, ObjectId)}.
      */
     private void stopAncestryPath(Node commit, Queue<Node> theirQueue, Set<Node> theirSet) {
         Queue<Node> ancestorQueue = new LinkedList<Node>();
         ancestorQueue.add(commit);
        List<Node> processed = new LinkedList<Node>();
         while (!ancestorQueue.isEmpty()) {
             Node ancestor = ancestorQueue.poll();
             for (Relationship parent : ancestor.getRelationships(CommitRelationshipTypes.PARENT)) {
                 Node parentNode = parent.getEndNode();
                 if (parentNode.getId() != ancestor.getId()) {
                     if (theirSet.contains(parentNode)) {
                        if (!processed.contains(parentNode)) {
                            ancestorQueue.add(parentNode);
                            processed.add(parentNode);
                        }
                     } else if (theirQueue.contains(parentNode)) {
                         theirQueue.remove(parentNode);
                     }
                 }
             }
         }
     }
 
     /**
      * Helper method for {@link #findLowestCommonAncestor(ObjectId, ObjectId)}.
      */
     private void verifyAncestors(List<Node> potentialCommonAncestors, Set<Node> leftSet,
             Set<Node> rightSet) {
         Queue<Node> ancestorQueue = new LinkedList<Node>();
         List<Node> falseAncestors = new LinkedList<Node>();
        List<Node> processed = new LinkedList<Node>();
         for (Node n : potentialCommonAncestors) {
             if (falseAncestors.contains(n)) {
                 continue;
             }
             ancestorQueue.add(n);
             while (!ancestorQueue.isEmpty()) {
                 Node ancestor = ancestorQueue.poll();
                 for (Relationship parent : ancestor
                         .getRelationships(CommitRelationshipTypes.PARENT)) {
                     Node parentNode = parent.getEndNode();
                     if (parentNode.getId() != ancestor.getId()) {
                         if (leftSet.contains(parentNode) || rightSet.contains(parentNode)) {
                            if (!processed.contains(parentNode)) {
                                ancestorQueue.add(parentNode);
                                processed.add(parentNode);
                            }
                             if (potentialCommonAncestors.contains(parentNode)) {
                                 falseAncestors.add(parentNode);
                             }
                         }
                     }
                 }
             }
         }
         potentialCommonAncestors.removeAll(falseAncestors);
     }
 }

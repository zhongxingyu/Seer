 /*
  * (C) Copyright 2006-2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu, jcarsique, slacoin
  */
 package org.nuxeo.build.maven.graph;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.Stack;
 import java.util.TreeMap;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
 import org.apache.maven.artifact.versioning.VersionRange;
 import org.apache.maven.project.MavenProject;
 import org.apache.tools.ant.BuildException;
 import org.nuxeo.build.maven.ArtifactDescriptor;
 import org.nuxeo.build.maven.Logger;
 import org.nuxeo.build.maven.MavenClient;
 import org.nuxeo.build.maven.MavenClientFactory;
 import org.nuxeo.build.maven.filter.Filter;
 import org.nuxeo.build.maven.filter.VersionManagement;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class Graph {
 
     protected MavenClient maven;
 
     public final TreeMap<String, Node> nodes = new TreeMap<String, Node>();
 
     public final LinkedList<Node> roots = new LinkedList<Node>();
 
     protected Resolver resolver = new Resolver(this);
 
     protected Map<String, Artifact> file2artifacts = new HashMap<String, Artifact>();
 
     // manage versions from dependency management -> lazy initialized when
     // required. (by calling artifact:resolveFile without a version)
     protected VersionManagement vmgr;
 
     protected boolean shouldLoadDependencyManagement = false;
 
     public Graph(MavenClient maven) {
         this.maven = maven;
         this.vmgr = new VersionManagement();
     }
 
     public VersionManagement getVersionManagement() {
         return vmgr;
     }
 
     public void setShouldLoadDependencyManagement(
             boolean shouldLoadDependencyManagement) {
         this.shouldLoadDependencyManagement = shouldLoadDependencyManagement;
     }
 
     public boolean shouldLoadDependencyManagement() {
         return shouldLoadDependencyManagement;
     }
 
     public MavenClient getMaven() {
         return maven;
     }
 
     public List<Node> getRoots() {
         return roots;
     }
 
     public Collection<Node> getNodes() {
         return nodes.values();
     }
 
     public Artifact getArtifactByFile(String fileName) {
         return file2artifacts.get(fileName);
     }
 
     public void collectNodes(Collection<Node> nodesToCollect) {
         for (Node node : roots) {
             node.collectNodes(nodesToCollect);
         }
     }
 
     public void collectNodes(Collection<Node> nodesToCollect, Filter filter) {
         for (Node node : roots) {
             node.collectNodes(nodesToCollect, filter);
         }
     }
 
     public Node[] getNodesArray() {
         return nodes.values().toArray(new Node[nodes.size()]);
     }
 
     public Node findFirst(String pattern) {
         return findFirst(pattern, false);
     }
 
     public Node findFirst(String pattern, boolean stopIfNotUnique) {
         SortedMap<String, Node> map = nodes.subMap(pattern + ':', pattern
                 + ((char) (':' + 1)));
         int size = map.size();
         if (size == 0) {
             return null;
         }
         if (stopIfNotUnique && size > 1) {
             throw new BuildException(
                     "Pattern '"
                             + pattern
                             + "' cannot be resolved to a unique node. Matching nodes are: "
                             + map.values());
         }
         return map.get(map.firstKey());
     }
 
     public Collection<Node> find(String pattern) {
         SortedMap<String, Node> map = nodes.subMap(pattern + ':', pattern
                 + ((char) (':' + 1)));
         return map.values();
     }
 
     /**
      * Add a root node given an artifact pom. This can be used by the embedder
      * maven mojo to initialize the graph with the current pom.
      */
     public Node addRootNode(MavenProject pom) {
         return getRootNode(pom, pom.getArtifact());
     }
 
     public Node addRootNode(String key) {
         ArtifactDescriptor ad = new ArtifactDescriptor(key);
         Artifact artifact = ad.getBuildArtifact();
         return getRootNode(artifact);
     }
 
     public Node getRootNode(Artifact artifact) {
         MavenProject pom = resolver.load(artifact);
         return getRootNode(pom, artifact);
     }
 
     /**
      * @since 1.10.2
      */
     public Node getRootNode(MavenProject pom, Artifact artifact) {
         Node node = nodes.get(Node.createNodeId(artifact));
         if (node == null) {
             node = new Node(Graph.this, artifact, pom);
             nodes.put(node.getId(), node);
             nodesByArtifact.put(artifact, node);
             roots.add(node);
         }
         return node;
     }
 
     public Resolver getResolver() {
         return resolver;
     }
 
     public Node lookup(String id) {
         return nodes.get(id);
     }
 
     public Node lookup(Artifact artifact) {
         return lookup(Node.createNodeId(artifact));
     }
 
     @SuppressWarnings("unchecked")
     public Node findNode(ArtifactDescriptor ad) {
         String key = ad.getNodeKeyPattern();
         Collection<Node> nodesToParse = null;
         if (key == null) {
             nodesToParse = getNodes();
         } else {
             nodesToParse = find(key);
         }
         Node returnNode = null;
         for (Node node : nodesToParse) {
             Artifact artifact = node.getArtifact();
             if (ad.artifactId != null
                     && !ad.artifactId.equals(artifact.getArtifactId())) {
                 continue;
             }
             if (ad.groupId != null && !ad.groupId.equals(artifact.getGroupId())) {
                 continue;
             }
             if (ad.version != null && !ad.version.equals(artifact.getVersion())) {
                 continue;
             }
             if (ad.type != null && !ad.type.equals(artifact.getType())) {
                 continue;
             }
             try {
                 if (returnNode != null
                         && artifact.getSelectedVersion().compareTo(
                                 returnNode.getArtifact().getSelectedVersion()) < 0) {
                     continue;
                 }
             } catch (OverConstrainedVersionException e) {
                 MavenClientFactory.getLog().error(
                         "Versions comparison failed on " + artifact, e);
             }
             returnNode = node;
         }
         return returnNode;
     }
 
     public MavenProject loadPom(Artifact artifact) {
         if ("system".equals(artifact.getScope()))
             return null;
         try {
             return maven.getProjectBuilder().buildFromRepository(
                     // this create another Artifact instance whose type is 'pom'
                     maven.getArtifactFactory().createProjectArtifact(
                             artifact.getGroupId(), artifact.getArtifactId(),
                             artifact.getVersion()),
                     maven.getRemoteRepositories(), maven.getLocalRepository());
         } catch (Exception e) {
             MavenClientFactory.getLog().error(e.getMessage(), e);
             return null;
         }
     }
 
     public final IdentityHashMap<Artifact, Node> nodesByArtifact = new IdentityHashMap<Artifact, Node>();
 
     protected class NodesInjector implements
             org.apache.maven.artifact.resolver.ResolutionListener {
 
         protected final HashSet<Node> filteredNodes = new HashSet<Node>();
 
         protected final Stack<Node> parentNodes = new Stack<Node>();
 
         protected final Node rootNode;
 
         protected final Filter filter;
 
         protected final int maxDepth;
 
         protected Node currentNode;
 
         protected NodesInjector(Node node, Filter filter, int maxDepth) {
             this.currentNode = node;
             this.rootNode = node;
             this.filter = filter;
             this.maxDepth = maxDepth;
             this.rootNode.state = Node.INCLUDED;
         }
 
         @Override
         public void testArtifact(Artifact node) {
             debug("testArtifact: artifact=" + node);
         }
 
         @Override
         public void startProcessChildren(Artifact artifact) {
             debug("startProcessChildren: artifact=" + artifact);
 
             if (!currentNode.getArtifact().equals(artifact)) {
                 throw new IllegalStateException("Artifact was expected to be "
                         + currentNode.getArtifact() + " but was " + artifact);
             }
 
             parentNodes.push(currentNode);
         }
 
         @Override
         public void endProcessChildren(Artifact artifact) {
             Node node = parentNodes.pop();
 
             debug("endProcessChildren: artifact=" + artifact);
 
             if (node == null) {
                 throw new IllegalStateException(
                         "Parent dependency node was null");
             }
 
             if (!node.getArtifact().equals(artifact)) {
                 throw new IllegalStateException(
                         "Parent dependency node artifact was expected to be "
                                 + node.getArtifact() + " but was " + artifact);
             }
         }
 
         @Override
         public void includeArtifact(Artifact artifact) {
             debug("includeArtifact: artifact=" + artifact);
 
             Node node = nodesByArtifact.get(artifact);
 
             if (node != null) {
                 debug("already included, returning : artifact=" + artifact);
                 return;
             }
 
             if (!isCurrentNodeIncluded()) {
                 debug("not included, returning : artifact=" + currentNode);
                 return;
             }
 
             addNode(artifact);
 
         }
 
         @Override
         public void omitForNearer(Artifact omitted, Artifact kept) {
             debug("omitForNearer: omitted=" + omitted + "( "
                     + System.identityHashCode(omitted) + ") kept=" + kept + "("
                     + System.identityHashCode(kept) + ")");
 
             if (!omitted.getDependencyConflictId().equals(
                     kept.getDependencyConflictId())) {
                 throw new IllegalArgumentException(
                         "Omitted artifact dependency conflict id "
                                 + omitted.getDependencyConflictId()
                                 + " differs from kept artifact dependency conflict id "
                                 + kept.getDependencyConflictId());
             }
 
             if (!isCurrentNodeIncluded()) {
                 debug("not included, returning : artifact=" + currentNode);
                 return;
             }
 
             // Node omittedNode = nodesByArtifact.get(omitted);
             //
             // if (omittedNode != null) {
             // removeNode(omitted);
             // validateDependencyTree();
             // omittedNode.state = Node.OMITTED;
             // }
 
             Node keptNode = nodesByArtifact.get(kept);
 
             if (keptNode == null) {
                 keptNode = addNode(kept);
             } else {
                 addEdges(keptNode);
                 currentNode = keptNode;
             }
 
         }
 
         @Override
         public void updateScope(Artifact artifact, String scope) {
             debug("updateScope: artifact=" + artifact + ", scope=" + scope);
         }
 
         @Override
         public void manageArtifact(Artifact artifact, Artifact replacement) {
             debug("manageArtifact: artifact=" + artifact + ", replacement="
                     + replacement);
         }
 
         @Override
         public void omitForCycle(Artifact artifact) {
             warn("omitForCycle: artifact=" + artifact);
         }
 
         @Override
         public void updateScopeCurrentPom(Artifact artifact, String ignoredScope) {
             debug("updateScopeCurrentPom: artifact=" + artifact
                     + ", scopeIgnored=" + ignoredScope);
         }
 
         @Override
         public void selectVersionFromRange(Artifact artifact) {
             warn("selectVersionFromRange: artifact=" + artifact);
         }
 
         @Override
         public void restrictRange(Artifact artifact, Artifact replacement,
                 VersionRange newRange) {
             warn("restrictRange: artifact=" + artifact + ", replacement="
                     + replacement + ", versionRange=" + newRange);
         }
 
         /**
          * The log to write debug messages to.
          */
         protected final Logger logger = MavenClientFactory.getLog();
 
         /**
          * Writes the specified message to the log at debug level with
          * indentation for the current node's depth.
          *
          * @param message the message to write to the log
          */
         protected void debug(String message) {
 
             if (logger.isDebugEnabled() == false) {
                 return;
             }
 
             int depth = parentNodes.size();
 
             StringBuffer buffer = new StringBuffer();
 
             for (int i = 0; i < depth; i++) {
                 buffer.append("  ");
             }
 
             buffer.append(message);
 
             logger.debug(buffer.toString());
         }
 
         protected void warn(String message) {
 
             int depth = parentNodes.size();
 
             StringBuffer buffer = new StringBuffer();
 
             for (int i = 0; i < depth; i++) {
                 buffer.append("  ");
             }
 
             buffer.append(message);
 
             logger.info(buffer.toString());
         }
 
         protected boolean isCurrentNodeIncluded() {
 
             for (Iterator<Node> iterator = parentNodes.iterator(); iterator.hasNext();) {
                 Node node = iterator.next();
 
                 if (node.state != Node.INCLUDED && node.state != Node.FILTERED) {
                     return false;
                 }
             }
 
             return true;
         }
 
         protected Node addNode(Artifact artifact) {
             Node node = createNode(artifact);
             Node previousNode = nodesByArtifact.put(artifact, node);
             nodes.put(node.id, node);
 
             if (previousNode != null) {
                 throw new IllegalStateException(
                         "Duplicate node registered for artifact: "
                                 + node.getArtifact());
             }
             currentNode = node;
 
             debug("indexed artifact=" + artifact + ",identity="
                     + System.identityHashCode(artifact));
 
             return node;
         }
 
         protected void removeNode(Artifact artifact) {
             Node node = nodesByArtifact.remove(artifact);
             if (node == null) {
                 warn("removing not indexed "
                         + System.identityHashCode(artifact) + " : artifact="
                         + artifact);
                 node = nodes.get(Node.createNodeId(artifact));
             }
             nodes.remove(node.id);
             if (filteredNodes.remove(node)) {
                 debug("Reset filtering : " + node);
             }
             for (Edge e : node.edgesOut) {
                 e.out.edgesIn.remove(e);
             }
             for (Edge e : node.edgesIn) {
                 e.in.edgesOut.remove(e);
             }
             if (!artifact.equals(node.getArtifact())) {
                 throw new IllegalStateException(
                         "Removed dependency node artifact was expected to be "
                                 + artifact + " but was " + node.getArtifact());
             }
 
             debug("unindexed   artifact=" + artifact + ",identity= "
                     + System.identityHashCode(artifact));
         }
 
         protected Node createNode(Artifact artifact) {
             MavenProject pom = resolver.load(artifact);
             Node node = new Node(Graph.this, artifact, pom);
 
             addEdges(node);
 
             return node;
         }
 
         protected void addEdges(Node out) {
 
             if (parentNodes.isEmpty()) {
                 return;
             }
 
             Node in = parentNodes.peek();
             Edge edge = new Edge(in, out);
 
             switch (out.state) {
             case Node.UNKNOWN: // node injection
                 if (!accept(edge)) {
                     filteredNodes.add(out);
                     out.state = Node.FILTERED;
                     return;
                 }
                 out.state = Node.INCLUDED;
                 break;
             case Node.FILTERED:
                 if (accept(edge)) {
                     filteredNodes.remove(out);
                     warn("unfiltering : artifact=" + out.artifact);
                     out.state = Node.INCLUDED;
                 }
                 break;
             case Node.INCLUDED: // edges injection
                 break;
             default:
                 throw new IllegalStateException("Cannot add edge : artifact="
                         + out.artifact);
             }
 
             out.edgesIn.add(edge);
             in.edgesOut.add(edge);
 
         }
 
         protected boolean accept(Edge edge) {
 
             if (edge.in.state == Node.FILTERED) {
                 debug("filtering edge (inherited from parent) : artifact="
                         + edge.out);
                 return false;
             }
 
            if (parentNodes.size() >= maxDepth) {
                 debug("filtering edge (max depth) : artifact=" + edge.out);
                 return false;
             }
 
             if (!filter.accept(edge)) {
                 debug("filtering edge (filter) : artifact=" + edge.out.artifact);
                 return false;
             }
 
             return true;
         }
 
         protected void removeFiltered() {
             Iterator<Node> it = filteredNodes.iterator();
             while (it.hasNext()) {
                 Node n = it.next();
                 it.remove();
                 removeNode(n.artifact);
             }
         }
 
     }
 
     protected class UnreferencedNodesValidator extends AbstractGraphVisitor {
 
         protected HashSet<Node> unreferencedNodes = new HashSet<Node>();
 
         @Override
         public boolean visitNode(Node node) {
             if (nodes.get(node.id) == null && !roots.contains(node)) {
                 unreferencedNodes.add(node);
                 // re-inject unreferenced node
                 nodes.put(node.id, node);
             }
             return true;
         }
 
         @Override
         public boolean visitEdge(Edge edge) {
             return true;
         }
     }
 
     protected void validateDependencyTree() {
         UnreferencedNodesValidator validator = new UnreferencedNodesValidator();
         validator.process(this);
         if (validator.unreferencedNodes.size() > 0) {
             MavenClientFactory.getLog().warn(
                     "Fixed unreferenced nodes : " + validator.unreferencedNodes);
         }
     }
 
     public void resolveDependencyTree(Node node, Filter filter, int depth) {
         MavenClientFactory.getLog().info("Resolving dependencies for " + node);
         final NodesInjector injector = new NodesInjector(node, filter, depth);
         try {
             maven.resolveDependencyTree(node.artifact, new ArtifactFilter() {
 
                 public boolean include(Artifact artifact) {
                     return false;
                 }
             }, injector);
         } catch (Exception cause) {
             throw new Error("Cannot resolve dependency tree for " + node, cause);
         }
         validateDependencyTree();
 
         // remove filtered artifacts
         MavenClientFactory.getLog().info("Filtering dependency tree");
         injector.removeFiltered();
 
         validateDependencyTree();
 
     }
 
 }

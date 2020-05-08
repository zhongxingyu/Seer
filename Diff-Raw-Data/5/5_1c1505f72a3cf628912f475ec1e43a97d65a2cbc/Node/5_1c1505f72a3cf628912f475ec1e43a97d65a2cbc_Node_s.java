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
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.project.MavenProject;
 import org.nuxeo.build.maven.MavenClientFactory;
 import org.nuxeo.build.maven.filter.Filter;
 
 /**
  *
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class Node {
 
     protected final Graph graph;
 
     protected final String id;
 
     protected final Artifact artifact;
 
     protected final List<Edge> edgesIn = new ArrayList<Edge>();
 
     protected final List<Edge> edgesOut = new ArrayList<Edge>();
 
     /**
      * Point to an artifact pom. When embedded in maven and using the current
      * project pom as the root this will be set by the maven loader Mojo to
      * point to the current pom
      */
     protected final MavenProject pom;
 
     private List<char[]> acceptedCategories;
 
     public List<char[]> getAcceptedCategories() {
         if (acceptedCategories == null) {
             acceptedCategories = new ArrayList<char[]>();
         }
         return acceptedCategories;
     }
 
     public static String createNodeId(Artifact artifact) {
         StringBuilder sb = new StringBuilder().append(artifact.getGroupId()).append(
                 ':').append(artifact.getArtifactId()).append(':').append(
                artifact.getVersion()).append(':').append(artifact.getType());
         if (artifact.getClassifier() != null) {
            sb.append(':').append(artifact.getClassifier());
         }
         sb.append(':').append(artifact.getScope());
         return sb.toString();
     }
 
     public Node(Node node) {
         this.id = node.id;
         this.graph = node.graph;
         this.artifact = node.artifact;
         this.edgesIn.addAll(node.edgesIn);
         this.edgesOut.addAll(node.edgesOut);
         this.pom = node.pom;
     }
 
     protected Node(Graph graph, Artifact artifact, MavenProject pom) {
         this.id = createNodeId(artifact);
         this.graph = graph;
         this.artifact = artifact;
         this.pom = pom;
     }
 
     protected static final int UNKNOWN = 0;
 
     protected static final int INCLUDED = 1;
 
     protected static final int OMITTED = 2;
 
     protected static final int FILTERED = 3;
 
     protected int state = UNKNOWN;
 
     /**
      * Default format with GAV: group:artifact:version:type:classifier
      *
      * @since 1.10.2
      */
     public static final int FORMAT_GAV = 0;
 
     /**
      * Key-value format: FILENAME=GAV
      *
      * @since 1.10.2
      */
     public static final int FORMAT_KV_F_GAV = 1;
 
     public Artifact getArtifact() {
         return artifact;
     }
 
     public File getFile() {
         if (!artifact.isResolved()) {
             graph.getResolver().resolve(artifact);
         }
         File file = artifact.getFile();
         if (file != null) {
             graph.file2artifacts.put(file.getName(), artifact);
         }
         return file;
     }
 
     public File getFile(String classifier) {
         Artifact ca = graph.maven.getArtifactFactory().createArtifactWithClassifier(
                 artifact.getGroupId(), artifact.getArtifactId(),
                 artifact.getVersion(), artifact.getType(), classifier);
         try {
             graph.maven.resolve(ca);
             File file = ca.getFile();
             if (file != null) {
                 graph.file2artifacts.put(file.getAbsolutePath(), ca);
             }
             return file;
         } catch (Throwable t) {
             t.printStackTrace();
             return null;
         }
     }
 
     public boolean isRoot() {
         return edgesIn.isEmpty();
     }
 
     public String getId() {
         return id;
     }
 
     public Collection<Edge> getEdgesOut() {
         return edgesOut;
     }
 
     public Collection<Edge> getEdgesIn() {
         return edgesIn;
     }
 
     protected void addEdgeIn(Edge edge) {
         edgesIn.add(edge);
     }
 
     protected void addEdgeOut(Edge edge) {
         edgesOut.add(edge);
     }
 
     public MavenProject getPom() {
         return pom;
     }
 
     public MavenProject getPomIfAlreadyLoaded() {
         return pom;
     }
 
     public List<Node> getTrail() {
         if (edgesIn.isEmpty()) {
             ArrayList<Node> result = new ArrayList<Node>();
             result.add(this);
             return result;
         }
         Edge edge = edgesIn.get(0);
         List<Node> path = edge.in.getTrail();
         path.add(this);
         return path;
     }
 
     public void collectNodes(Collection<Node> nodes, Filter filter) {
         for (Edge edge : edgesOut) {
             if (filter.accept(edge)) {
                 nodes.add(edge.out);
             }
         }
     }
 
     public void collectNodes(Collection<Node> nodes) {
         for (Edge edge : edgesOut) {
             nodes.add(edge.out);
         }
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
         }
         if (obj instanceof Node) {
             return ((Node) obj).id.equals(this);
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return id.hashCode();
     }
 
     @Override
     public String toString() {
         return artifact.toString();
     }
 
     /**
      * @param pattern
      */
     public void setAcceptedCategory(char[] pattern) {
         getAcceptedCategories().add(pattern);
     }
 
     /**
      * @param patterns
      * @return true if at least one pattern has been accepted
      */
     public boolean isAcceptedCategory(List<char[]> patterns) {
         for (char[] pattern : patterns) {
             if (getAcceptedCategories().contains(pattern)) {
                 return true;
             }
         }
         return false;
     }
 
     public void expand(Filter filter, int depth) {
         graph.resolveDependencyTree(this, filter, depth);
     }
 
     /**
      * @param format output format
      * @return String representation depending on format
      * @see #FORMAT_GAV
      * @see #FORMAT_KV_F_GAV
      * @since 1.10.2
      */
     public String toString(int format) {
         switch (format) {
         case FORMAT_GAV:
             return toString();
 
         case FORMAT_KV_F_GAV:
             String toString;
             try {
                 if (artifact.getFile() == null) {
                     MavenClientFactory.getInstance().resolve(artifact);
                 }
                 toString = artifact.getFile().getName();
             } catch (ArtifactNotFoundException e) {
                 toString = "ArtifactNotFound";
             }
             toString += "=" + id;
             return toString;
 
         default:
             return "Unknown format: " + format + "!";
         }
     }
 }

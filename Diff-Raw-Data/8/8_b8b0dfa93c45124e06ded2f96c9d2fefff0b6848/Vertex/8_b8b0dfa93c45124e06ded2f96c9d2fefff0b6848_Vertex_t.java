 package org.triple_brain.module.model.graph;
 
 import org.triple_brain.module.model.Suggestion;
 
 import java.util.List;
 import java.util.Set;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public abstract class Vertex implements GraphElement{
     public static final String EMPTY_LABEL = "a concept";
 
     public abstract boolean hasEdge(Edge edge);
     public abstract void addOutgoingEdge(Edge edge);
     public abstract void removeOutgoingEdge(Edge edge);
     public abstract Edge edgeThatLinksToDestinationVertex(Vertex destinationVertex);
     public abstract boolean hasDestinationVertex(Vertex destinationVertex);
     public abstract void addNeighbor(Vertex neighbor);
     public abstract Edge addVertexAndRelation();
     public abstract Edge addRelationToVertex(Vertex destinationVertex);
     public abstract void remove();
     public abstract void removeNeighbor(Vertex neighbor);
     public abstract Set<Edge> outGoingEdges();
     public abstract Set<Edge> connectedEdges();
     public abstract List<String> hiddenConnectedEdgesLabel();
     public abstract void hiddenConnectedEdgesLabel(List<String> hiddenEdgeLabel);
     public abstract boolean hasMinNumberOfEdgesFromCenterVertex();
     public abstract void suggestions(Set<Suggestion> suggestions);
     public abstract Set<Suggestion> suggestions();
     public abstract void addSemanticType(String typeUri);
     public abstract void setSameAsUsingUri(String uri);
     @Override
     public boolean equals(Object vertexToCompareAsObject) {
         Vertex vertexToCompare = (Vertex) vertexToCompareAsObject;
         return id().equals(vertexToCompare.id());
     }
 
     @Override
     public int hashCode() {
         return id().hashCode();
     }
 }
 

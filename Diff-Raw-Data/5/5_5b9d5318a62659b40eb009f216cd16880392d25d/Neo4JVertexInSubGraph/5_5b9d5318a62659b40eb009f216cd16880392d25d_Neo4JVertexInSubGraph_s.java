 package org.triple_brain.module.neo4j_graph_manipulator.graph;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 import org.joda.time.DateTime;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.triple_brain.module.common_utils.Uris;
 import org.triple_brain.module.model.FriendlyResource;
 import org.triple_brain.module.model.Image;
 import org.triple_brain.module.model.TripleBrainUris;
 import org.triple_brain.module.model.UserUris;
 import org.triple_brain.module.model.graph.Edge;
 import org.triple_brain.module.model.graph.Vertex;
 import org.triple_brain.module.model.graph.VertexInSubGraph;
 import org.triple_brain.module.model.suggestion.Suggestion;
 
 import javax.inject.Inject;
 import java.net.URI;
 import java.util.HashSet;
 import java.util.Set;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class Neo4JVertexInSubGraph implements VertexInSubGraph {
 
     public static final String NUMBER_OF_CONNECTED_EDGES_PROPERTY_NAME = "number_of_connected_edges_property_name";
 
     private Integer depthInSubGraph = -1;
     protected Node node;
     protected Neo4JGraphElement graphElement;
     protected Neo4JVertexFactory vertexFactory;
 
     protected FriendlyResource friendlyResource;
 
     protected Neo4JEdgeFactory edgeFactory;
 
     protected Neo4JUtils utils;
 
     protected Neo4JSuggestionFactory suggestionFactory;
 
     @Inject
     Neo4JFriendlyResourceFactory friendlyResourceFactory;
 
     @AssistedInject
     protected Neo4JVertexInSubGraph(
             Neo4JVertexFactory vertexFactory,
             Neo4JEdgeFactory edgeFactory,
             Neo4JUtils utils,
             Neo4JGraphElementFactory neo4JGraphElementFactory,
             Neo4JFriendlyResourceFactory neo4JFriendlyResourceFactory,
             Neo4JSuggestionFactory suggestionFactory,
             @Assisted Node node
     ) {
         this.vertexFactory = vertexFactory;
         this.edgeFactory = edgeFactory;
         this.suggestionFactory = suggestionFactory;
         this.utils = utils;
         this.friendlyResourceFactory = neo4JFriendlyResourceFactory;
         this.node = node;
         graphElement = neo4JGraphElementFactory.withNode(
                 node
         );
         this.friendlyResource = neo4JFriendlyResourceFactory.createOrLoadFromNode(
                 node
         );
         if(!node.hasProperty(IS_PUBLIC_PROPERTY_NAME)){
             makePrivate();
         }
         if(!isInitialized()){
             initialize();
         }
     }
 
     @AssistedInject
     protected Neo4JVertexInSubGraph(
             Neo4JVertexFactory vertexFactory,
             Neo4JEdgeFactory edgeFactory,
             Neo4JUtils utils,
             Neo4JGraphElementFactory neo4JGraphElementFactory,
             Neo4JFriendlyResourceFactory friendlyResourceFactory,
             Neo4JSuggestionFactory suggestionFactory,
             @Assisted URI uri
     ){
         this(
                 vertexFactory,
                 edgeFactory,
                 utils,
                 neo4JGraphElementFactory,
                 friendlyResourceFactory,
                 suggestionFactory,
                 utils.getOrCreate(uri)
         );
     }
 
     @AssistedInject
     protected Neo4JVertexInSubGraph(
             Neo4JVertexFactory vertexFactory,
             Neo4JEdgeFactory edgeFactory,
             Neo4JUtils utils,
             Neo4JGraphElementFactory neo4JGraphElementFactory,
             Neo4JFriendlyResourceFactory friendlyResourceFactory,
             Neo4JSuggestionFactory suggestionFactory,
             @Assisted String ownerUserName
     ){
         this(
                 vertexFactory,
                 edgeFactory,
                 utils,
                 neo4JGraphElementFactory,
                 friendlyResourceFactory,
                 suggestionFactory,
                 new UserUris(ownerUserName).generateVertexUri()
         );
     }
 
     @AssistedInject
     protected Neo4JVertexInSubGraph(
             Neo4JVertexFactory vertexFactory,
             Neo4JEdgeFactory edgeFactory,
             Neo4JUtils utils,
             Neo4JGraphElementFactory neo4JGraphElementFactory,
             Neo4JFriendlyResourceFactory friendlyResourceFactory,
             Neo4JSuggestionFactory suggestionFactory,
             @Assisted Set<Vertex> includedVertices
     )throws IllegalArgumentException{
         this(
                 vertexFactory,
                 edgeFactory,
                 utils,
                 neo4JGraphElementFactory,
                 friendlyResourceFactory,
                 suggestionFactory,
                 new UserUris(
                         includedVertices.iterator().next().ownerUsername()
                 ).generateVertexUri()
         );
         if(includedVertices.size() <= 1){
             throw new IllegalArgumentException(
                     "A minimum number of 2 vertices is required to create a vertex from included vertices"
             );
         }
         setIncludedVertices(
                 includedVertices
         );
     }
 
     @Override
     public boolean hasEdge(Edge edge) {
         for (Relationship relationship : relationshipsToEdges()) {
             Edge edgeToCompare = edgeFactory.createOrLoadWithNode(
                     relationship.getStartNode()
             );
             if (edge.equals(edgeToCompare)) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public void addOutgoingEdge(Edge edge) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void removeOutgoingEdge(Edge edge) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Edge edgeThatLinksToDestinationVertex(Vertex destinationVertex) {
         for (Relationship relationship : relationshipsToEdges()) {
             Edge edge = edgeFactory.createOrLoadWithNode(
                     relationship.getStartNode()
             );
             if (edge.hasVertex(destinationVertex)) {
                 return edge;
             }
         }
         throw new RuntimeException(
                 "Edge between vertex with " + uri() +
                         " and vertex with uri " + destinationVertex.uri() +
                         " was not found"
         );
     }
 
     @Override
     public Boolean hasDestinationVertex(Vertex destinationVertex) {
         Iterable<Relationship> relationshipsIt = node.getRelationships(
                 Relationships.SOURCE_VERTEX
         );
         for (Relationship relationship : relationshipsIt){
             Edge edge = edgeFactory.createOrLoadWithNode(
                     relationship.getStartNode()
             );
             if (edge.destinationVertex().equals(destinationVertex)){
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public void addNeighbor(Vertex neighbor) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Edge addVertexAndRelation() {
         Neo4JVertexInSubGraph newVertex = vertexFactory.createForOwnerUsername(
                 ownerUsername()
         );
        newVertex.incrementNumberOfConnectedEdges();
        incrementNumberOfConnectedEdges();
         return addRelationToVertex(newVertex);
     }
 
     @Override
     public Edge addRelationToVertex(Vertex destinationVertex) {
         return edgeFactory.createForSourceAndDestinationVertex(
                 this,
                 (Neo4JVertexInSubGraph) destinationVertex
         );
     }
 
     @Override
     public void remove() {
         for (Edge edge : connectedEdges()) {
             Neo4JVertexInSubGraph otherVertex = (Neo4JVertexInSubGraph) edge.otherVertex(this);
             otherVertex.decrementNumberOfConnectedEdges();
             edge.remove();
         }
         graphElement.remove();
     }
 
     @Override
     public String ownerUsername() {
         return graphElement.ownerUsername();
     }
 
     @Override
     public void removeNeighbor(Vertex neighbor) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Set<Edge> outGoingEdges() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Set<Edge> connectedEdges() {
         Set<Edge> edges = new HashSet<Edge>();
         for (Relationship relationship : relationshipsToEdges()) {
             edges.add(
                     edgeFactory.createOrLoadWithNode(
                             relationship.getStartNode()
                     )
             );
         }
         return edges;
     }
 
     @Override
     public Integer getNumberOfConnectedEdges() {
         return Integer.valueOf(node.getProperty(
                 NUMBER_OF_CONNECTED_EDGES_PROPERTY_NAME
         ).toString());
     }
 
     @Override
     public void setNumberOfConnectedEdges(Integer numberOfConnectedEdges) {
         node.setProperty(
                 NUMBER_OF_CONNECTED_EDGES_PROPERTY_NAME,
                 numberOfConnectedEdges
         );
     }
 
     protected void incrementNumberOfConnectedEdges(){
         node.setProperty(
                 NUMBER_OF_CONNECTED_EDGES_PROPERTY_NAME,
                 getNumberOfConnectedEdges() + 1
         );
     }
 
     protected void decrementNumberOfConnectedEdges() {
         node.setProperty(
                 NUMBER_OF_CONNECTED_EDGES_PROPERTY_NAME,
                 getNumberOfConnectedEdges() - 1
         );
     }
 
     private Iterable<Relationship> relationshipsToEdges() {
         return node.getRelationships(
                 Relationships.SOURCE_VERTEX,
                 Relationships.DESTINATION_VERTEX
         );
     }
 
     @Override
     public void addSuggestions(Suggestion... suggestions) {
         for (Suggestion suggestion : suggestions) {
             Neo4JSuggestion neo4JSuggestion = (Neo4JSuggestion) suggestion;
             node.createRelationshipTo(
                     neo4JSuggestion.getNode(),
                     Relationships.SUGGESTION
             );
         }
         graphElement.updateLastModificationDate();
     }
 
     private void removePropertiesWithRelationShipType(RelationshipType relationshipType) {
         for (Relationship relationship : node.getRelationships(Direction.OUTGOING, relationshipType)) {
             utils.removeAllProperties(relationship);
             relationship.delete();
         }
     }
 
     @Override
     public Set<Suggestion> suggestions() {
         Set<Suggestion> suggestions = new HashSet<>();
         for (Relationship relationship : node.getRelationships(Relationships.SUGGESTION)) {
             Suggestion suggestion = suggestionFactory.getFromNode(
                     relationship.getEndNode()
             );
             suggestions.add(suggestion);
         }
         return suggestions;
     }
 
     @Override
     public void addType(FriendlyResource type) {
         graphElement.addType(type);
     }
 
     @Override
     public void removeIdentification(FriendlyResource friendlyResource) {
         graphElement.removeIdentification(
                 friendlyResource
         );
         removeSuggestionsHavingExternalResourceAsOrigin(
                 friendlyResource
         );
     }
 
 
     private void removeSuggestionsHavingExternalResourceAsOrigin(FriendlyResource resource) {
         for (Suggestion suggestion : suggestions()) {
             suggestion.removeOriginsThatDependOnResource(
                     resource
             );
             if (suggestion.origins().isEmpty()) {
                 suggestion.remove();
             }
         }
     }
 
     @Override
     public Set<FriendlyResource> getAdditionalTypes() {
         return graphElement.getAdditionalTypes();
     }
 
     @Override
     public Set<FriendlyResource> getIdentifications() {
         return graphElement.getIdentifications();
     }
 
     @Override
     public void addSameAs(FriendlyResource friendlyResourceImpl) {
         graphElement.addSameAs(friendlyResourceImpl);
     }
 
     @Override
     public Set<FriendlyResource> getSameAs() {
         return graphElement.getSameAs();
     }
 
     private final String IS_PUBLIC_PROPERTY_NAME = "is_public";
 
     @Override
     public Boolean isPublic() {
         return (Boolean) node.getProperty(
                 IS_PUBLIC_PROPERTY_NAME
         );
     }
 
     @Override
     public void makePublic() {
         node.setProperty(
                 IS_PUBLIC_PROPERTY_NAME,
                 true
         );
         graphElement.updateLastModificationDate();
     }
 
     @Override
     public void makePrivate() {
         node.setProperty(
                 IS_PUBLIC_PROPERTY_NAME,
                 false
         );
         graphElement.updateLastModificationDate();
     }
 
     public void setIncludedVertices(Set<Vertex> includedVertices){
         for(Vertex vertex : includedVertices){
             Neo4JVertexInSubGraph neo4JVertex = (Neo4JVertexInSubGraph) vertex;
             node.createRelationshipTo(
                     neo4JVertex.node,
                     Relationships.HAS_INCLUDED_VERTEX
             );
         }
     }
 
     @Override
     public Set<Vertex> getIncludedVertices() {
         Set<Vertex> includedVertices = new HashSet<>();
         for(Relationship relationship : node.getRelationships(Relationships.HAS_INCLUDED_VERTEX, Direction.OUTGOING)){
             includedVertices.add(
                     vertexFactory.createOrLoadUsingNode(
                             relationship.getEndNode()
                     )
             );
         }
         return includedVertices;
     }
 
     @Override
     public DateTime creationDate() {
         return graphElement.creationDate();
     }
 
     @Override
     public DateTime lastModificationDate() {
         return graphElement.lastModificationDate();
     }
 
     @Override
     public URI uri() {
         return graphElement.uri();
     }
 
     @Override
     public String label() {
         return graphElement.label();
     }
 
     @Override
     public Set<Image> images() {
         return friendlyResource.images();
     }
 
     @Override
     public Boolean gotTheImages() {
         return friendlyResource.gotTheImages();
     }
 
     @Override
     public String comment() {
         return friendlyResource.comment();
     }
 
     @Override
     public void comment(String comment) {
         friendlyResource.comment(
                 comment
         );
     }
 
     @Override
     public Boolean gotComments() {
         return friendlyResource.gotComments();
     }
 
     @Override
     public void addImages(Set<Image> images) {
         friendlyResource.addImages(
                 images
         );
     }
 
     @Override
     public void label(String label) {
         graphElement.label(label);
     }
 
     @Override
     public boolean hasLabel() {
         return graphElement.hasLabel();
     }
 
     @Override
     public void addGenericIdentification(FriendlyResource friendlyResource) {
         graphElement.addGenericIdentification(friendlyResource);
     }
 
     @Override
     public Set<FriendlyResource> getGenericIdentifications() {
         return graphElement.getGenericIdentifications();
     }
 
     @Override
     public boolean equals(Object vertexToCompareAsObject) {
         return graphElement.equals(vertexToCompareAsObject);
     }
 
     @Override
     public int hashCode() {
         return graphElement.hashCode();
     }
 
     @Override
     public Integer minDistanceFromCenterVertex() {
         return depthInSubGraph;
     }
 
     @Override
     public VertexInSubGraph setMinDistanceFromCenterVertex(Integer minDistanceFromCenterVertex) {
         this.depthInSubGraph = minDistanceFromCenterVertex;
         return this;
     }
 
     private void addVertexType(){
         addType(friendlyResourceFactory.createOrLoadUsingUriAndLabel(
                 Uris.get(TripleBrainUris.TRIPLE_BRAIN_VERTEX),
                 ""
         ));
     }
 
     private boolean isInitialized(){
         return hasVertexType();
     }
 
     private void initialize(){
         addVertexType();
         initNumberOfConnectedEdges();
     }
 
     private void initNumberOfConnectedEdges(){
         node.setProperty(
                 NUMBER_OF_CONNECTED_EDGES_PROPERTY_NAME,
                 0
         );
     }
 
     private boolean hasVertexType(){
         for (Relationship relationship : node.getRelationships(Relationships.TYPE)) {
             FriendlyResource type = friendlyResourceFactory.createOrLoadFromNode(
                     relationship.getEndNode()
             );
             if (type.uri().toString().equals(TripleBrainUris.TRIPLE_BRAIN_VERTEX)) {
                 return true;
             }
         }
         return false;
     }
 }

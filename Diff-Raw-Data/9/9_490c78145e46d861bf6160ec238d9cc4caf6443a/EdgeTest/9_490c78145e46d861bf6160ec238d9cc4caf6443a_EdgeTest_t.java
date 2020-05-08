 package org.triple_brain.module.model.graph;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.triple_brain.module.model.FriendlyResource;
 
 import java.net.URI;
 
 import static junit.framework.Assert.assertTrue;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNot.not;
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class EdgeTest extends AdaptableGraphComponentTest {
 
     @Test
     public void can_add_relation() {
         Vertex vertexD = vertexA.addVertexAndRelation().destinationVertex();
         Vertex vertexE = vertexD.addVertexAndRelation().destinationVertex();
 
         Integer numberOfEdgesAndVertices = wholeGraphAroundDefaultCenterVertex().numberOfEdgesAndVertices();
         Edge newEdge = vertexE.addRelationToVertex(vertexA);
 
         assertThat(newEdge.sourceVertex(), is(vertexE));
         assertThat(newEdge.destinationVertex(), is(vertexA));
         assertTrue(userGraph.haveElementWithId(newEdge.uri()));
         assertThat(newEdge.label(), is(""));
         assertThat(numberOfEdgesAndVertices(), is(numberOfEdgesAndVertices + 1));
     }
 
     @Test
     public void can_remove_an_edge() {
         Integer numberOfEdgesAndVertices = numberOfEdgesAndVertices();
         Edge edge = vertexA.edgeThatLinksToDestinationVertex(vertexB);
         URI edgeId = edge.uri();
         assertTrue(userGraph.haveElementWithId(edgeId));
         edge.remove();
         assertFalse(userGraph.haveElementWithId(edgeId));
         assertFalse(vertexA.hasDestinationVertex(vertexB));
 
         Integer updatedNumberOfEdgesAndVertices = numberOfEdgesAndVertices();
         assertThat(updatedNumberOfEdgesAndVertices, is(numberOfEdgesAndVertices - 1));
     }
 
     @Test
     public void can_update_label() {
         Edge edge = vertexA.addVertexAndRelation();
         edge.label("likes");
         assertThat(edge.label(), is("likes"));
     }
 
     @Test
     public void there_is_a_creation_date(){
         Edge edge = vertexA.addVertexAndRelation();
         assertThat(
                 edge.creationDate(),
                 is(not(nullValue()))
         );
     }
 
     @Test
     public void there_is_a_last_modification_date(){
         Edge edge = vertexA.addVertexAndRelation();
         assertThat(
                 edge.lastModificationDate(),
                 is(not(nullValue()))
         );
     }
 
     @Test
     public void can_add_same_as(){
         Edge newEdge = vertexA.addVertexAndRelation();
         Assert.assertTrue(newEdge.getSameAs().isEmpty());
         newEdge.addSameAs(modelTestScenarios.creatorPredicate());
         assertFalse(newEdge.getSameAs().isEmpty());
     }
 
     @Test
     public void can_check_equality(){
         Edge anEdge = vertexA.addVertexAndRelation();
         assertTrue(anEdge.equals(anEdge));
         Edge anotherEdge = vertexA.addVertexAndRelation();
         assertFalse(anEdge.equals(anotherEdge));
     }
 
     @Test
     public void can_compare_to_friendly_resource(){
         Edge anEdge = vertexA.addVertexAndRelation();
         FriendlyResource anEdgeAsFriendlyResource = (FriendlyResource) anEdge;
         assertTrue(anEdge.equals(anEdgeAsFriendlyResource));
     }
 
     @Test
     public void can_inverse(){
         Edge betweenAAndB = vertexA.edgeThatLinksToDestinationVertex(vertexB);
         assertThat(betweenAAndB.sourceVertex(), is(vertexA));
         assertThat(betweenAAndB.destinationVertex(), is(vertexB));
         betweenAAndB.inverse();
         assertThat(betweenAAndB.sourceVertex(), is(vertexB));
         assertThat(betweenAAndB.destinationVertex(), is(vertexA));
     }

    @Test
    public void deleting_a_relation_decrements_number_of_connected_edges_to_vertices(){
        assertThat(vertexA.getNumberOfConnectedEdges(), is(1));
        assertThat(vertexB.getNumberOfConnectedEdges(), is(2));
        vertexA.edgeThatLinksToDestinationVertex(vertexB).remove();
        assertThat(vertexA.getNumberOfConnectedEdges(), is(0));
        assertThat(vertexB.getNumberOfConnectedEdges(), is(1));
    }
 }

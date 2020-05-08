 package org.triple_brain.module.search;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONObject;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
import static org.triple_brain.module.model.json.graph.VertexJsonFields.LABEL;
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class GraphSearchTest extends SearchRelatedTest {
 
     @Test
     public void can_search_vertices_for_auto_completion() throws Exception{
         indexVertexABAndC();
         indexVertex(pineApple);
         GraphSearch graphSearch = GraphSearch.withCoreContainer(coreContainer);
         JSONArray vertices;
         vertices = graphSearch.searchVerticesForAutoCompletionByLabelAndUser("vert", user);
         assertThat(vertices.length(), is(3));
         vertices = graphSearch.searchVerticesForAutoCompletionByLabelAndUser("vertex Cad", user);
         assertThat(vertices.length(), is(1));
         JSONObject firstVertex = vertices.getJSONObject(0);
         assertThat(firstVertex.getString(LABEL), is("vertex Cadeau"));
         vertices = graphSearch.searchVerticesForAutoCompletionByLabelAndUser("pine A", user);
         assertThat(vertices.length(), is(1));
     }
 
 }

 package org.triple_brain.module.model.json.graph;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.triple_brain.module.model.Suggestion;
 import org.triple_brain.module.model.graph.Vertex;
 import org.triple_brain.module.model.json.SuggestionJSONFields;
 
 import java.util.List;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class VertexJSONFields {
     public static final String ID = "id";
     public static final String LABEL = "label";
     public static final String IS_FRONTIER_VERTEX_WITH_HIDDEN_VERTICES = "is_frontier_vertex_with_hidden_vertices";
     public static final String NUMBER_OF_HIDDEN_CONNECTED_VERTICES = "number_of_hidden_connected_vertices";
     public static final String NAME_OF_HIDDEN_PROPERTIES = "name_of_hidden_properties";
     public static final String MIN_NUMBER_OF_EDGES_FROM_CENTER_VERTEX = "min_number_of_edges_from_center_vertex";
     public static final String SUGGESTIONS = "suggestions";
 
     public static JSONObject vertexToJson(Vertex vertex) {
         try {
             JSONArray suggestions = new JSONArray();
             for(Suggestion suggestion : vertex.suggestions()){
                 suggestions.put(
                         SuggestionJSONFields.suggestionToJson(
                                 suggestion
                         )
                 );
             }
             JSONObject jsonVertex = new JSONObject()
             .put(ID, vertex.id())
             .put(LABEL, vertex.label().trim().isEmpty() ? Vertex.EMPTY_LABEL : vertex.label())
             .put(SUGGESTIONS, suggestions);
             List<String> hiddenConnectedEdgesLabel = vertex.hiddenConnectedEdgesLabel();
             if(!hiddenConnectedEdgesLabel.isEmpty()){
                 jsonVertex.put(VertexJSONFields.IS_FRONTIER_VERTEX_WITH_HIDDEN_VERTICES, true);
                 Integer numberOfHiddenConnectedVertices = hiddenConnectedEdgesLabel.size();
                 jsonVertex.put(VertexJSONFields.NUMBER_OF_HIDDEN_CONNECTED_VERTICES, numberOfHiddenConnectedVertices);
                 jsonVertex.put(NAME_OF_HIDDEN_PROPERTIES, new JSONArray(hiddenConnectedEdgesLabel));
             }
             return jsonVertex;
         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
     }
 }

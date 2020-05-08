 /*
  * Copyright 2012 Jon Ivmark
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.graphit.examples.tinkerpop;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.graphit.graph.blueprints.BlueprintsGraph;
 import org.graphit.graph.node.domain.NodeId;
 import org.graphit.graph.service.PropertyGraph;
 import org.graphit.graph.service.PropertyGraphImpl;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 
 import com.tinkerpop.blueprints.Graph;
 import com.tinkerpop.blueprints.Vertex;
 import com.tinkerpop.gremlin.java.GremlinPipeline;
 
 import static org.graphit.examples.ExampleConstants.*;
 
 /**
  * Some examples using Gremlin pipes.
 *
  * @author jon
 *
  */
 public class GremlinPipesExamples {
 
     private static Graph importGraph() throws IOException {
         PropertyGraph graph = new PropertyGraphImpl();
         Resource resource = new ClassPathResource("/examples/musicstore.json");
         graph.importJson(resource.getFile());
         return new BlueprintsGraph(graph);
     }
 
     private List<Object> getJohnsPurchases() throws IOException {
         Graph graph = importGraph();
         GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>();
         NodeId john = new NodeId(USER, "john.doe");
         return pipe.start(graph.getVertex(john))
             .out(BOUGHT.name())
             .property("Title").toList();
     }
 
     private List<Object> getUsersThatListendedToTracksJohnBought() throws IOException {
         Graph graph = importGraph();
         GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>();
         NodeId john = new NodeId(USER, "john.doe");
         return pipe.start(graph.getVertex(john))
             .out(BOUGHT.name())
             .in(LISTENED_TO.name())
             .dedup()
            .property("Name")
            .toList();
     }
 
     /**
      * Main method that runs through the examples.
      */
     public static void main(String[] args) throws IOException {
         GremlinPipesExamples examples = new GremlinPipesExamples();
         System.out.println("John's purhcases: " + examples.getJohnsPurchases());
         System.out.println("Other users: " + examples.getUsersThatListendedToTracksJohnBought());
     }
 }

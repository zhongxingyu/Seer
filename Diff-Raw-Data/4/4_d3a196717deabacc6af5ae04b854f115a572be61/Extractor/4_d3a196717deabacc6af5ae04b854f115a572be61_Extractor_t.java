 /*
  * Copyright 2012 University of Helsinki.
  *
  * This file is part of BMVis².
  *
  * BMVis² is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * BMVis² is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with BMVis².  If not, see
  * <http://www.gnu.org/licenses/>.
  */
 
 package biomine.bmvis2.subgraph;
 
 import biomine.bmvis2.VisualEdge;
 import biomine.bmvis2.VisualGraph;
 import biomine.bmvis2.VisualNode;
 import biomine.bmvis2.pipeline.GraphOperation;
 import biomine.bmvis2.pipeline.SettingsChangeCallback;
 import biomine.bmvis2.pipeline.StructuralOperation;
 import org.json.simple.JSONObject;
 
 import javax.swing.*;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
 * Abstract superclass for subgraph extractors. 
  * Given a graph and knowledge of whether certain nodes are of interest
  * (scored between [-1,1] where -1 means the presence of a given node is not
  * wished for and 1 meaning the node should most probably be in the
  * visualization, this class should provide some kind of end result.
  * <p/>
  * Non-desired nodes should be hidden from the result.  The input graph
  * should be as complete as possible: it should be expanded around the
  * interesting points before this class is run to contain as much of the
  * structure of the graph.
  */
 public abstract class Extractor extends StructuralOperation implements GraphOperation {
     private double score = 0.0;
     private int nodeBudget = 30;
     private int edgeBudget = 110;
     private Map<VisualNode, Double> interestMap;
 
     public String getTitle() {
         return "Visible subgraph extractor";
     }
 
     public String getToolTip() {
         return "Score: " + this.score;
     }
 
     protected void setScore(double newScore) {
         this.score = newScore;
     }
 
     public void setNodeBudget(int newBudget) {
         this.nodeBudget = newBudget;
     }
 
     public int getNodeBudget() {
         return this.nodeBudget;
     }
 
     public int getEdgeBudget() {
         return edgeBudget;
     }
 
     public void setEdgeBudget(int edgeBudget) {
         this.edgeBudget = edgeBudget;
     }
 
     public void setInterestMap(Map<VisualNode, Double> newMap) {
         this.interestMap = newMap;
     }
 
     public Map<VisualNode, Double> getInterestMap() {
         return this.interestMap;
     }
 
     public static void hideAllExceptNodes(VisualGraph g, Set<VisualNode> keepers) {
         Set<VisualNode> parents = new HashSet<VisualNode>();
 
         for (VisualNode node : keepers) {
             VisualNode parent = node.getParent();
             while (parent != null) {
                 parents.add(parent);
                 parent = parent.getParent();
             }
         }
 
         Set<VisualNode> newHidden = new HashSet<VisualNode>();
         for (VisualNode node : g.getAllNodes())
             if (!keepers.contains(node) && !parents.contains(node))
                 newHidden.add(node);
 
         g.setHiddenNodes(newHidden);
     }
 
     public static void removeAllExceptNodes(VisualGraph g, Set<VisualNode> keepers) {
         Set<VisualNode> parents = new HashSet<VisualNode>();
 
         for (VisualNode node : keepers) {
             VisualNode parent = node.getParent();
             while (parent != null) {
                 parents.add(parent);
                 parent = parent.getParent();
             }
         }
 
         Set<VisualNode> newHidden = new HashSet<VisualNode>();
         for (VisualNode node : g.getAllNodes())
             if (!keepers.contains(node) && !parents.contains(node))
                 newHidden.add(node);
 
         for (VisualNode node : newHidden) {
             g.deleteNode(node);
         }
     }
 
     public static void removeAllExceptEdges(VisualGraph g, Set<VisualEdge> keepers) {
         Set<VisualEdge> newHidden = new HashSet<VisualEdge>();
         for (VisualEdge edge: g.getAllEdges())
             if (!keepers.contains(edge))
                 newHidden.add(edge);
 
         for (VisualEdge edge : newHidden)
             g.deleteEdge(edge);
     }
 
     public void fromJSON(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
         this.nodeBudget = ((Integer) o.get("nodeBudget")).intValue();
     }
 
     public JSONObject toJSON() {
         JSONObject ret = new JSONObject();
         ret.put("nodeBudget", this.nodeBudget);
         return ret;
     }
 
     public abstract JComponent getSettingsComponent(SettingsChangeCallback v, VisualGraph graph);
 
     public abstract void doOperation(VisualGraph g) throws GraphOperationException;
 
 }

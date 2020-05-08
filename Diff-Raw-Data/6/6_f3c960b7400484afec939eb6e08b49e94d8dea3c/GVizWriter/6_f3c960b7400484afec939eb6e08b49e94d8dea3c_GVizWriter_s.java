 package uk.ac.kcl.informatics.opmbuild.format.gviz;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import uk.ac.kcl.informatics.opmbuild.Agent;
 import uk.ac.kcl.informatics.opmbuild.Artifact;
 import uk.ac.kcl.informatics.opmbuild.Account;
 import uk.ac.kcl.informatics.opmbuild.Edge;
 import uk.ac.kcl.informatics.opmbuild.Graph;
 import uk.ac.kcl.informatics.opmbuild.GraphElement;
 import uk.ac.kcl.informatics.opmbuild.Node;
 import uk.ac.kcl.informatics.opmbuild.OPMAnnotations;
 import uk.ac.kcl.informatics.opmbuild.Process;
 import uk.ac.kcl.informatics.opmbuild.Retrieve;
 import uk.ac.kcl.informatics.opmbuild.RoleAnnotatedEdge;
 import uk.ac.kcl.informatics.opmbuild.Used;
 import uk.ac.kcl.informatics.opmbuild.WasControlledBy;
 import uk.ac.kcl.informatics.opmbuild.WasDerivedFrom;
 import uk.ac.kcl.informatics.opmbuild.WasGeneratedBy;
 import uk.ac.kcl.informatics.opmbuild.WasTriggeredBy;
 
 public class GVizWriter {
     private PrintWriter _out;
     private boolean _annotations;
     private boolean _standardTypes;
     private boolean _numberEntities;
 
     public GVizWriter (File output) throws IOException {
         _out = new PrintWriter (new FileWriter (output));
         _annotations = false;
         _standardTypes = false;
         _numberEntities = false;
     }
 
     public void close () {
         _out.close ();
     }
 
     public void setGraphAnnotations (boolean flag) {
         _annotations = flag;
     }
 
     public void setNumberEntities (boolean flag) {
         _numberEntities = flag;
     }
 
     public void setShowStandardEdgeTypes (boolean flag) {
         _standardTypes = flag;
     }
 
     private List<GraphElement> sort (Collection<GraphElement> elements) {
         List<GraphElement> sorted = new LinkedList<GraphElement> ();
         Map<GraphElement, Integer> layers = new HashMap<GraphElement, Integer> ();
         GraphElement effect, cause;
         int layer, other;
         boolean moved;
 
         do {
             moved = false;
             for (GraphElement element : elements) {
                 layer = getLayer (layers, element);
                 if (element instanceof Edge) {
                     cause = ((Edge) element).getCause ();
                     other = getLayer (layers, cause);
                     if (layer <= other) {
                         layer = setLayer (layers, element, other + 1);
                         moved = true;
                     }
                     effect = ((Edge) element).getEffect ();
                     other = getLayer (layers, effect);
                     if (layer >= other) {
                         other = setLayer (layers, effect, layer + 1);
                         moved = true;
                     }
                 }
             }
         } while (moved);
 
         layer = 0;
         while (!elements.isEmpty ()) {
             for (GraphElement element : elements) {
                 if (getLayer (layers, element) == layer) {
                     sorted.add (element);
                 }
             }
             elements.removeAll (sorted);
             layer += 1;
         }
 
         return sorted;
     }
 
     private static int getLayer (Map<GraphElement, Integer> layers, GraphElement element) {
         if (layers.containsKey (element)) {
             return layers.get (element);
         } else {
             return setLayer (layers, element, 0);
         }
     }
 
     private static int setLayer (Map<GraphElement, Integer> layers, GraphElement element, int layer) {
         layers.put (element, layer);
         return layer;
     }
 
     public List<GraphElement> write (Graph graph) {
         List<GraphElement> indexOrder = sort (graph.getElements ());
 
         _out.println ("digraph OPMGraph { rankdir=\"BT\"; ");
         for (Node node : Retrieve.getNodes (indexOrder)) {
             writeNode (node, indexOrder.indexOf (node));
         }
         for (Edge edge : Retrieve.getEdges (indexOrder)) {
             writeEdge (edge, indexOrder);
         }
         _out.println ("}");
 
         return indexOrder;
     }
 
     private void writeEdge (Edge edge, List<GraphElement> nodeOrder) {
         String effect = "n" + nodeOrder.indexOf (edge.getEffect ());
         String cause = "n" + nodeOrder.indexOf (edge.getCause ());
         Object type = Retrieve.getValueByKey (edge, OPMAnnotations.TYPE);
         String label = "";
 
         if (_numberEntities) {
             label = nodeOrder.indexOf (edge) + ": ";
         }
         if (type == null) {
             if (_standardTypes && edge instanceof Used) {
                 label += "used";
             }
             if (_standardTypes && edge instanceof WasControlledBy) {
                 label += "wasControlledBy";
             }
             if (_standardTypes && edge instanceof WasDerivedFrom) {
                 label += "wasDerivedFrom";
             }
             if (_standardTypes && edge instanceof WasGeneratedBy) {
                 label += "wasGeneratedBy";
             }
             if (_standardTypes && edge instanceof WasTriggeredBy) {
                 label += "wasTriggeredBy";
             }
         } else {
             label += type.toString ();
         }
         if (edge instanceof RoleAnnotatedEdge) {
             if (((RoleAnnotatedEdge) edge).getRole () != null) {
                 label += "(" + ((RoleAnnotatedEdge) edge).getRole ().toString () + ")";
             }
         }
         _out.println (effect + " -> " + cause + " [color=\"black\",style=\"filled\","
                 + "labelfontsize=\"8\",label=\"" + label + "\",fontcolor=\"black\"]");
     }
 
     private void writeNode (Node node, int name) {
 		String label;
 		if(Retrieve.getValueByKey (node, OPMAnnotations.LABEL) == null)
 		{
			System.out.println("Node with null label = " + node);
 			label = "";
 		}
 		else
 		{
         	label = Retrieve.getValueByKey (node, OPMAnnotations.LABEL).toString ();
			System.out.println(node);
			System.out.println("Label retrieved " + label);
 		}
 
         if (_numberEntities) {
             label = name + ": " + label;
         }
         if (node instanceof Agent) {
             _out.println ("  n" + name + "[shape=\"polygon\",label=\"" + label + "\",sides=\"8\"]");
         }
         if (node instanceof Artifact) {
             _out.println ("  n" + name + "[label=\"" + label + "\"]");
         }
         if (node instanceof Process) {
             _out.println ("  n" + name + "[shape=\"polygon\",label=\"" + label + "\",sides=\"4\"]");
         }
     }
 }

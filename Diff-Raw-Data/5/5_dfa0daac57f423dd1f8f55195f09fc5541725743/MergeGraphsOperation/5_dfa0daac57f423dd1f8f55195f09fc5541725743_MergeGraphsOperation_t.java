 
 package it.wolfed.operation;
 
 import com.mxgraph.model.mxCell;
 import it.wolfed.model.ArcEdge;
 import it.wolfed.model.InterfaceVertex;
 import it.wolfed.model.PetriNetGraph;
 import it.wolfed.model.PlaceVertex;
 import it.wolfed.model.TransitionVertex;
 import it.wolfed.model.Vertex;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  */
 public class MergeGraphsOperation extends Operation
 {
     protected List<PetriNetGraph> inputGraphs;
     
     /**
      * Clone all the cells from inputGraphs into operationGraph.
      * 
      * @param operationGraph
      * @param inputGraphs
      * @throws Exception  
      */
     public MergeGraphsOperation(PetriNetGraph operationGraph, PetriNetGraph... inputGraphs) throws Exception
     {
         super(operationGraph);
         this.inputGraphs = Arrays.asList(inputGraphs);
         execute();
     }
     
     @Override
     void process()
     {
         mxCell clone = null;
         Object parent = operationGraph.getDefaultParent();
         
         for (int i = 0; i < inputGraphs.size(); i++)
         {
             PetriNetGraph net = inputGraphs.get(i);
 
             for (Object cellObj : net.getChildCells(net.getDefaultParent()))
             {
                 mxCell cell = (mxCell) cellObj;
 
                 if(cell instanceof PlaceVertex)
                 {
                    PlaceVertex place = (PlaceVertex) cell;
                     clone = new PlaceVertex(parent, getPrefix(i + 1) + cell.getId(), cell.getValue(), 0, 0);
                    ((PlaceVertex)clone).setTokens(place.getTokens());
                }
                 else if(cell instanceof TransitionVertex)
                 {
                     clone = new TransitionVertex(parent, getPrefix(i + 1) + cell.getId(), cell.getValue(), 0, 0);
                 }
                 else if(cell instanceof InterfaceVertex)
                 {
                     clone = new InterfaceVertex(parent, getPrefix(i + 1) + cell.getId(), cell.getValue());
                 }
                 // @todo check instanceof Arc when the mouserelease creation of arcs will be type-zed
                 // ArcEdge e mxCell.isEdge()
                 else if(cell.isEdge())
                 {
                     // Get
                     Vertex source = operationGraph.getVertexById(getPrefix(i + 1) + cell.getSource().getId());
                     Vertex target = operationGraph.getVertexById(getPrefix(i + 1) + cell.getTarget().getId());
 
                     // Clone
                     clone = new ArcEdge(parent, getPrefix(i + 1) + cell.getId(), cell.getValue(), source, target);
                 }
 
                 operationGraph.addCell(clone);
             }
         }
     }
 }

 package visibilityGraph;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class GraphNode {
     private Float[] dimensions;
     private int numOfDims;
     
     public GraphNode(int dimNum, Float[] dims) throws InvalidNumberOfDimensionsException{
         numOfDims = dimNum;
         if(dimNum != dims.length)
             throw new InvalidNumberOfDimensionsException();
         dimensions = dims;
     }
     
     public int getNumOfDims(){
         return this.numOfDims;
     }
     
     public Float getDimension(int dim,int numOfDims) throws InvalidDimensionRequestException{
         if(dim >= numOfDims || dim < 0){
             throw new InvalidDimensionRequestException();
         }
         return dimensions[dim];
     }
     
     public Float[] getCoords(){
        return this.dimensions;
     }
 }

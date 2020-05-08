package xtc.oop.helper; //UPDATE PACKAGE
import java.util.ArrayList;
import xtc.tree.GNode;

 public class Field{
     /*
      * For holding info about parameters and data fields
      */
 
     String name;
     String type;
     boolean isArray;
     int arrayDims;
     boolean hasAssignment;
     GNode assignmentNode; //for the situations where a
     ArrayList<Integer> concreteDims = new ArrayList<Integer>();
     ArrayList<String> modifiers = new ArrayList<String>();
 
     public Field(){};
 
     public Field(String name, String type, String modifier){
         this.name           = name;
         this.type           = type;
         this.isArray        = false;
         this.arrayDims      = 0;
         this.hasAssignment  = false;
         this.assignmentNode = null;
         modifiers.add(modifier);
     }
 
     public Field(String name, String type, ArrayList<String> modifiers){
         this.name           = name;
         this.type           = type;
         this.isArray        = false;
         this.arrayDims      = 0;
         this.hasAssignment  = false;
         this.assignmentNode = null;
         modifiers.addAll(modifiers);
     }
 
     public void setName(String name){
         this.name = name;
     }
     public void setType(String type){
         this.type = type;
     }
     public void setIsArray(boolean isArray){
         this.isArray = isArray;
     }
     public void setArrayDims(int arrayDims){
         this.arrayDims = arrayDims;
     }
     public void addModifier(String modifier){
         modifiers.add(modifier);
     }
     public void addConcreteDimension(int dim){
         concreteDims.add(dim);
     }
     public void setHasAssignment(boolean hasAssignment){
         this.hasAssignment = hasAssignment;
     }
     public void setAssignment(GNode assignmentNode){
         this.assignmentNode = assignmentNode;
     }
 }

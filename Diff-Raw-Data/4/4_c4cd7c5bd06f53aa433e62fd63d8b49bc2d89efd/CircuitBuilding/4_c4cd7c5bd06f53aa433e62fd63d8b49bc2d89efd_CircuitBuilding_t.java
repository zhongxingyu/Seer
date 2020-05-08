 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.circuit;
 
 import com.mahn42.framework.Building;
 import java.util.ArrayList;
 
 /**
  *
  * @author andre
  */
 public class CircuitBuilding extends Building{
 
    public String circuitType = "";
    public String pinValues = ""; // Comma separated true,false,false,true
     
     @Override
     protected void toCSVInternal(ArrayList aCols) {
         super.toCSVInternal(aCols);
         aCols.add(circuitType);
         aCols.add(pinValues);
     }
 
     @Override
     protected void fromCSVInternal(DBRecordCSVArray aCols) {
         super.fromCSVInternal(aCols);
         circuitType = aCols.pop();
         pinValues = aCols.pop();
     }
 }

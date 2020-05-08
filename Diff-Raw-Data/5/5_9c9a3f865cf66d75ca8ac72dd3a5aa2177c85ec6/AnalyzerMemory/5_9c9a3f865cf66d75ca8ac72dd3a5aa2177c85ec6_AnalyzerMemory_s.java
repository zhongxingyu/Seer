 package com.google.javascript.jscomp;
 
 import java.util.ArrayList;
 
 /**
  * @author arcadoss
  */
 public class AnalyzerMemory {
   private final ArrayList<AbsObject> memory;
   private final int memorySize = 5000;
   private int lastUsed;
 
   private AnalyzerState.Label labelNull;
   private AnalyzerState.Label labelThis;
 
   public AnalyzerMemory() {
     this.memory = new ArrayList<AbsObject>(memorySize);
     this.lastUsed = 0;
     this.labelNull = createObject();
     this.labelThis = createObject();
   }
 
   public AnalyzerState.Label createObject() {
    memory.add(lastUsed++, new AbsObject());
    return new AnalyzerState.Label(lastUsed);
   }
 
   public AbsObject getObject(AnalyzerState.Label label) {
     return memory.get(label.get());
   }
 
   public AnalyzerState.Label getThis() {
     return labelThis;
   }
 
   public AnalyzerState.Label getNull() {
     return labelNull;
   }
 }

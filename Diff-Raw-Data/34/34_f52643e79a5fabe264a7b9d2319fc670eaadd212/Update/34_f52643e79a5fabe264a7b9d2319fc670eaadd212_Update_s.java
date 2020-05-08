 package fr.norad.core.updater;
 
 import lombok.Data;
 
 @Data
 public abstract class Update {
 
     private final String name;
 
     public Update(String name) {
         this.name = name;
     }
 
     public abstract void runUpdate();
 
 }

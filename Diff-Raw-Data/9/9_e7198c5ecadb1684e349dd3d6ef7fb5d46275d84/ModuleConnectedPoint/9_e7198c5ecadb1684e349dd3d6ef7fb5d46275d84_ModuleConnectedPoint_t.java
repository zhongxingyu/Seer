 package jDistsim.core.simulation.modules;
 
 import jDistsim.core.simulation.exception.ModuleConnectedPointFullCapacityException;
 import jDistsim.utils.pattern.observer.Observable;
 
 import java.util.ArrayList;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 4.12.12
  * Time: 21:48
  */
 public class ModuleConnectedPoint extends Observable {
 
     private int capacity;
     private ArrayList<Module> dependencies;
 
     public ModuleConnectedPoint(int capacity) {
         this.capacity = capacity;
         this.dependencies = new ArrayList<>();
     }
 
     public int getCapacity() {
         return capacity;
     }
 
     public void addDependency(Module dependencyModule) throws Exception {
         for (Module module : dependencies) {
             if (module.getIdentifier().equals(dependencyModule.getIdentifier())) {
                 throw new Exception("Dependency not created! -> is exist!!");
             }
         }
 
         if (isFull())
             throw new ModuleConnectedPointFullCapacityException("Capacity is full");
 
         dependencies.add(dependencyModule);
         setChanged();
     }
 
     public void removeDependency(Module module) {
         dependencies.remove(module);
        setChanged();
     }
 
     public void removeDependency(String identifier) {
         for (Module module : dependencies) {
             if (module.equals(identifier)) {
                 dependencies.remove(module);
                 return;
             }
         }
         setChanged();
     }
 
     public boolean isFull() {
         return dependencies.size() == capacity;
     }
 
     public boolean canBeConnected() {
         return !isFull();
     }
 
     public ArrayList<Module> getDependencies() {
         return dependencies;
     }
 
     public void removeAllDependencies() {
         dependencies.clear();
         setChanged();
     }
 }

 package jDistsim.application.designer.common;
 
import jDistsim.core.simulation.distributed.DistributedModelDefinition;
 import jDistsim.utils.collection.observable.ObservableHashMap;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 10.2.13
  * Time: 15:23
  */
 public class Application {
 
     private static Application instance;
     private static Object lock = new Object();
 
     private String modelName = "model1.jdsim";
    private ObservableHashMap<String, DistributedModelDefinition> distributedModels;
 
     private Application() {
         distributedModels = new ObservableHashMap<>();
     }
 
     public static Application global() {
         if (instance == null) {
             synchronized (lock) {
                 if (instance == null) {
                     instance = new Application();
                 }
             }
         }
         return instance;
     }
 
     public String getModelName() {
         return modelName;
     }
 
     public void setModelName(String modelName) {
         this.modelName = modelName;
     }
 
    public ObservableHashMap<String, DistributedModelDefinition> getDistributedModels() {
         return distributedModels;
     }
 }

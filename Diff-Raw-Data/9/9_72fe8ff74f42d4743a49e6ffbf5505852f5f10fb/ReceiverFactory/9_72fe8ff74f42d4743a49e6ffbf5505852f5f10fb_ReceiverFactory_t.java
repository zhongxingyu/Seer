 package jDistsim.core.simulation.modules.lib.receiver;
 
 import jDistsim.core.simulation.modules.Module;
 import jDistsim.core.simulation.modules.ModuleConnectedPoint;
 import jDistsim.core.simulation.modules.lib.BaseModuleFactory;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 5.3.13
  * Time: 17:11
  */
 public class ReceiverFactory extends BaseModuleFactory {
 
     @Override
     public Module create() {
         Module module = new Receiver(new ReceiverView(moduleConfiguration.getColorScheme()), moduleConfiguration);
        module.addOutputPoint(new ModuleConnectedPoint(1));
         return module;
     }
 }
 

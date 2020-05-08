 package jDistsim.core.simulation.modules.lib.sender;
 
 import jDistsim.core.simulation.modules.Module;
 import jDistsim.core.simulation.modules.ModuleConnectedPoint;
 import jDistsim.core.simulation.modules.lib.BaseModuleFactory;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 1.3.13
  * Time: 20:27
  */
 public class SenderFactory extends BaseModuleFactory {
 
     @Override
     public Module create() {
         Module module = new Sender(new SenderView(moduleConfiguration.getColorScheme()), moduleConfiguration);
        module.addOutputPoint(new ModuleConnectedPoint(1));
         return module;
     }
 }

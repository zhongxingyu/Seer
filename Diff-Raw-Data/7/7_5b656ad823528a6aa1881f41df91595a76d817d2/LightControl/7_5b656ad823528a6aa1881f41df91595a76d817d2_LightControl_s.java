 package org.morden.lightcontrol;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.tal.redstonechips.circuit.CircuitLibrary;
 import org.tal.redstonechips.RedstoneChips;
 import org.bukkit.event.Listener;
 
 
 /**
  *
  * @author Dennis Flanagan
  */
 public class LightControl extends CircuitLibrary implements Listener {
     private static List<RSLight> rsTorchLightCircuits = new ArrayList<RSLight>();
     public static RedstoneChips redstonechips;
     
    @Override
    public Class[] getCircuitClasses() {
         return new Class[] { glasslight.class, pumpkinlight.class, rstorchlight.class, torchlight.class, netherlight.class };
     }
     
     @Override
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockPhysics(BlockPhysicsEvent event) {
         if (!event.isCancelled()) {
             for (RSLight circuit : rsTorchLightCircuits)
                 circuit.onBlockPhysics(event);
         }
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockBreak(BlockBreakEvent event) {
         if (!event.isCancelled()) {
             for (RSLight circuit : rsTorchLightCircuits)
                 circuit.onBlockBreak(event);
         }
     }
     static void registerRSTorchLightCircuit(RSLight circuit) {
         if (!rsTorchLightCircuits.contains(circuit))
             rsTorchLightCircuits.add(circuit);
     }
 
     static boolean deregisterRSTorchLightCircuit(RSLight circuit) {
         return rsTorchLightCircuits.remove(circuit);
     }
     
     @Override
     public void onRedstoneChipsEnable(RedstoneChips instance) {
         instance.getPrefs().registerCircuitPreference(glasslight.class, "dropGlowstone", false);
         instance.getPrefs().registerCircuitPreference(LightControl.class, "lampPostBlocks", "WOOD,LOG,IRON_FENCE,FENCE,NETHER_BRICK,NETHER_FENCE");
         redstonechips = instance;
     }
 }

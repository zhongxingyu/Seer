<<<<<<< HEAD:src/main/java/com/thedevteam/theicport/materials/ICMaterials.java

package com.thedevteam.theicport.materials;
=======
 package com.thedevteam.theindustrialmod.materials;
>>>>>>> 0c9e94b013de1ded0179ac3fd4f14bf1a1a0e09a:src/main/java/com/thedevteam/theindustrialmod/materials/ICMaterials.java
 
 //Equivilent to Vanilla's VanillaMaterials - All Blocks should be registered here
 
 import org.spout.api.material.Material;
 import org.spout.vanilla.material.block.Ore;
 import org.spout.vanilla.material.generic.GenericItem;
 
 public final class ICMaterials{
 
 	private static boolean initialized = false;
     // Blocks
     public static final Ore URANIUM_ORE = Material.register(new Ore("Uranium Ore",1));
     
     // Items
     public static final GenericItem URANIUM = Material.register(new GenericItem("Uranium", 2));
     
     public static void initialize(){
         if(initialized) return;
         URANIUM_ORE.setDrop(URANIUM);
         initialized = true;
     }
 }

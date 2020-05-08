 package com.thedevteam.theindustrialmod.materials;
 
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

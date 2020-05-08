 package com.tehbeard.map.entities.vehicles;
 
 import com.tehbeard.map.factory.DataType;
 import com.tehbeard.mojang.nbt.CompoundTag;
 
 @DataType("MinecartFurnace")
 public class MinecartFurnace extends Minecart{
 
 	private double pushX;
 	private double pushZ;
 	private short fuel;
 	
	@SuppressWarnings("unchecked")
 	public MinecartFurnace(CompoundTag tag) {
 		super(tag);
 		fuel = tag.getShort("Fuel");
 		pushX = tag.getDouble("PushX");
 		pushZ = tag.getDouble("PushZ");
 		
 	}
 
 	public double getPushX() {
 		return pushX;
 	}
 
 	public double getPushZ() {
 		return pushZ;
 	}
 
 	public short getFuel() {
 		return fuel;
 	}
 
 	
 }

 package com.grover.mingebag.ic;
 
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseIC;
 import com.bukkit.gemo.FalseBook.IC.ICs.Lever;
 import com.bukkit.gemo.utils.ICUtils;
 import com.bukkit.gemo.utils.SignUtils;
 
 import org.bukkit.Location;
 import org.bukkit.block.Sign;
 
 
 
 public class BaseDataChip extends BaseIC
 {
    
 	public void outputData(BaseData data, final Sign signBlock, final int distance) {
 		outputData(data, signBlock, distance, Lever.BACK);
 	}
 	
 	public void outputDataLeft(BaseData data, DataTypes type, final Sign signBlock, final int distance) {
 		outputData(data, signBlock, distance, Lever.LEFT);
 	}
 	
 	public void outputDataRight(BaseData data, DataTypes type, final Sign signBlock, final int distance) {
 		outputData(data, signBlock, distance, Lever.RIGHT);
 	}
 	
 	private void outputData(BaseData data, final Sign signBlock, final int distance, final Lever lever) {
 		
 		// send datatype
 		DataTypeManager manager = this.core.getFactory().getDataTypeManager();
 		manager.addDataType(ICUtils.getLeverPos(signBlock, distance), new DataType(data));
 		switchLever(lever, signBlock, true, distance);
 		
 		// pulse
 		Integer pulse = 2;
 		if(signBlock.getLine(2).length() > 0) {
 			try {
 				pulse = Integer.parseInt(signBlock.getLine(2));
 			} catch (Exception e) {
 			}
 		}
 		
 		if(pulse > 600) {
 			pulse = 2;
 		}
 		
 		if(pulse > 0) {
 			this.core.getServer().getScheduler().scheduleSyncDelayedTask(this.core, new Runnable() {
 			    public void run() {
 			        switchLever(lever, signBlock, false, distance);
 			    }
 			}, pulse);
 		}
 		
 		// end datatype at lever
 		if(lever == Lever.BACK)
 			manager.endDataType(ICUtils.getLeverPos(signBlock, distance));
 			
 		if(lever == Lever.LEFT)
 			manager.endDataType(ICUtils.getLeverPosLeft(signBlock, distance));
 		
 		if(lever == Lever.RIGHT)
 			manager.endDataType(ICUtils.getLeverPosRight(signBlock, distance));
 	}
 	
 	
 	
 	public BaseData getData(Sign signBlock) {
 		int direction = SignUtils.getDirection(signBlock);
 		DataType type = null;
 		Location loc = signBlock.getLocation().clone();
 		if(direction == 1) {
 			loc.setZ(loc.getZ()+1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(direction == 2) {
 			loc.setX(loc.getX()+1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(direction == 3) {
 			loc.setZ(loc.getZ()-1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(direction == 4) {
 			loc.setX(loc.getX()-1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(type == null) {
 			return null;
 		}
 		return type.getData();
 	}
 	
	public BaseData getDataRight(Sign signBlock) {
 		int direction = SignUtils.getDirection(signBlock);
 		DataType type = null;
 		Location loc = signBlock.getLocation().clone();
 		if(direction == 1) {
 			loc.setX(loc.getX()+1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(direction == 2) {
 			loc.setZ(loc.getZ()+1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(direction == 3) {
 			loc.setX(loc.getX()-1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(direction == 4) {
 			loc.setX(loc.getZ()-1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(type == null) {
 			return null;
 		}
 		return type.getData();
 	}
 	
	public BaseData getDataLeft(Sign signBlock) {
 		int direction = SignUtils.getDirection(signBlock);
 		DataType type = null;
 		Location loc = signBlock.getLocation().clone();
 		if(direction == 1) {
 			loc.setX(loc.getX()-1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(direction == 2) {
 			loc.setZ(loc.getZ()-1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(direction == 3) {
 			loc.setX(loc.getX()+1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(direction == 4) {
 			loc.setX(loc.getZ()+1d);
 			type = core.getFactory().getDataTypeManager().getDataType(loc);
 		}
 		if(type == null) {
 			return null;
 		}
 		return type.getData();
 	}
 	
 }

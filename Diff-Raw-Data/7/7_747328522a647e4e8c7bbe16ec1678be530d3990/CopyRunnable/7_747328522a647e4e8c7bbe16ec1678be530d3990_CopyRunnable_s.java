 package fr.richie.Regen3000;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.InventoryHolder;
 
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.blocks.BlockType;
 
 public class CopyRunnable implements Runnable{
 
 	private RMAction a;
 	
	int toCopy = 1;
 	int nbCopied = 0;
 	int lastPercent = -100;
 	private long startTime = 0;
 
 	public int speed = 20000;
 	
 	int currentListId = 0;
 		
 	private List<List<BlockVector>> truncList = new ArrayList<List<BlockVector>>();
 
 	public CopyRunnable(RMAction a){
 		this.a = a;
 		
		toCopy = a.weRegion.getArea();
 		
 		
 		int nbCurrent = 0;
 		List<BlockVector> tmpList = new ArrayList<BlockVector>();
 		
 		for(BlockVector bv : a.weRegion){
 			
 			tmpList.add(bv);
 			nbCurrent++;
 			
 			if(nbCurrent>speed){
 				truncList.add(tmpList);
 				tmpList = new ArrayList<BlockVector>();
 				nbCurrent = 0;
 			}
 			
 			//this.remainingList.add(bv);
 		}
 		if(tmpList.size()>0){
 			truncList.add(tmpList);
 		}
 		
 	}
 	
 	@Override
 	public void run() {
 		
 		if(startTime == 0){
 			startTime = System.currentTimeMillis();
 		}
 		
 		Block from = null;
 		Block to = null;
 				
 		List<BlockVector> thisList = this.truncList.get(this.currentListId);
 		
 		for(BlockVector bv : thisList){
 			
 			from = a.wsource.getBlockAt(bv.getBlockX(), bv.getBlockY()-a.ydiff, bv.getBlockZ());
 			to = a.wdest.getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
 			
 			
 			
 			int x = to.getX();
 			int z = to.getZ();
 			
 			if(x==a.minPoint.getBlockX() || x == a.maxPoint.getBlockX()
 					|| z==a.minPoint.getBlockZ() || z == a.maxPoint.getBlockZ()){
 				
 				if(a.wallBlockMaterial.getId() != 0){
 					checkContainer(to);
 					to.setTypeIdAndData(a.wallBlockMaterial.getId(), (byte)0, false);
 				}
 				
 			}else{
 				int fromTypeID = from.getTypeId();
 				checkContainer(to);
 				
 				if(a.nolava && (fromTypeID == 10 || fromTypeID == 11)){
 					// if no-lava is set to true => stone instead of lava
 					to.setTypeIdAndData(1, (byte) 0, false);
 				}else{
 					to.setTypeIdAndData(fromTypeID, from.getData(), false);
 				}
 			}
 			
 			
 			nbCopied++;
 			
 			if((lastPercent+4)<((int)((((double)nbCopied)/((double)toCopy))*100.0/1.0))){
 				lastPercent = (int) ((((double)nbCopied)/((double)toCopy))*100.0/1.0);
 				
 				a.sendMessage(ChatColor.GRAY + "Copie des blocs : "+lastPercent+"% ("+nbCopied+"/"+toCopy+")");
 			}
 			
 		}
 		this.currentListId++;
 		
 		if(a.stopped){
 			a.notifStop();
 			return;
 		}
 		
 		if(this.nbCopied < this.toCopy){
 			a.runningTask = Bukkit.getScheduler().scheduleSyncDelayedTask(a.plugin, this, 2);
 		}else{
 			//Bukkit.broadcastMessage();
 			a.sendMessage(ChatColor.GREEN +""+ nbCopied +" blocs copis ! (en "+((System.currentTimeMillis()-startTime)/1000)+" secondes)");
 			//a.makeBedWalls();
 			
 			a.sendMessage(ChatColor.GREEN + "Regen achev en "+((System.currentTimeMillis()-a.startTime)/1000) + " secondes !");
 			
 			a.finish();
 			
 		}
 	}
 
 	private void checkContainer(Block to) {
 		
 		if(BlockType.isContainerBlock(to.getTypeId())){
 			if(to.getState() instanceof InventoryHolder){
 				((InventoryHolder)to.getState()).getInventory().clear();
 			}
 			
 			to.breakNaturally();
 		}
 		
 	}
 	
 	
 	
 }

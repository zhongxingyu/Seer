 package me.cvenomz.MetalDetector;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class MetalDetectorPlayerListener extends PlayerListener {
 
 	public MetalDetectorPlayerListener()
 	{
 		
 	}
 	
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent e)
 	{
 		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)
 		{
			if (e.getItem().getTypeId() == 278)  //278 = Diamond Pickaxe
 			{
 				doMetalDetect(e);
 			}
 		}
 	}
 	
 	private void doMetalDetect(PlayerInteractEvent e)
 	{
 		int range = 8;
 		if (e.getPlayer().getInventory().contains(57))
 			range = 16;
 		int x,y,z;
 		Location l = e.getPlayer().getLocation();
 		x = (int) l.getX();
 		y = (int) l.getY();
 		z = (int) l.getZ();
 		World w = l.getWorld();
 		ArrayList<ArrayList<Block>> data2 = new ArrayList<ArrayList<Block>>();
 		Block b;
 		for (int i = x-range;i < x+range; i++)
 			for (int j = y-range; j < y+range; j++)
 				for (int k = z-range; k < z+range; k++)
 				{
 					b = w.getBlockAt(i, j, k);
 					if (b.getTypeId() == 15 || b.getTypeId() == 14 || b.getTypeId() == 56)  //15=iron,14=gold
 						getCluster(b, data2);
 				}
 		//got data
 		printData(e, data2);
 	}
 	
 	private void getCluster(Block b, ArrayList<ArrayList<Block>> data)
 	{
 		if (containsBlock(b,data))
 			return;
 		ArrayList<Block> arr = new ArrayList<Block>();
 
 		clusterHelper(b, b.getTypeId(), arr);
 		data.add(arr);
 		
 	}
 	
 	private void clusterHelper(Block b, int id, ArrayList<Block> arr)
 	{
 		if (b.getTypeId() == id)
 		{
 			if (arr.contains(b))
 				return;
 			else
 			{
 				arr.add(b);
 				clusterHelper(b.getFace(BlockFace.DOWN), id, arr);
 				clusterHelper(b.getFace(BlockFace.UP), id, arr);
 				clusterHelper(b.getFace(BlockFace.EAST), id, arr);
 				clusterHelper(b.getFace(BlockFace.WEST), id, arr);
 				clusterHelper(b.getFace(BlockFace.NORTH), id, arr);
 				clusterHelper(b.getFace(BlockFace.SOUTH), id, arr);
 			}
 		}
 	}
 	
 	private boolean containsBlock(Block b, ArrayList<ArrayList<Block>> data)
 	{
 		Iterator<ArrayList<Block>> it = data.iterator();
 		//boolean ret = false;
 		while (it.hasNext())
 		{
 			if (it.next().contains(b))
 				return true;
 		}
 		return false;
 	}
 	
 	private void printData(PlayerInteractEvent e, ArrayList<ArrayList<Block>> data)
 	{
 		e.getPlayer().sendMessage(ChatColor.GREEN + "-----START-----");
 		Block b;
 		Iterator<ArrayList<Block>> it = data.iterator();
 		ArrayList<Block> arr;
 		while (it.hasNext())
 		{
 			arr = it.next();
 			b = findClosest(e.getPlayer(), arr);
 			e.getPlayer().sendMessage(ChatColor.BLUE + b.getType().toString() + " : " + arr.size() + " - " + getRelativeCoordinates(e.getPlayer(), b));
 		}
 		e.getPlayer().sendMessage(ChatColor.GREEN + "---END OF REPORT---");
 
 	}
 	
 	private Block findClosest(Player p, ArrayList<Block> arr)
 	{
 		double min = 100;
 		Block bmin = null;
 		int px = (int) p.getLocation().getX(), py = (int) p.getLocation().getY(), pz = (int) p.getLocation().getZ();
 		double distance;
 		for (Block b : arr)
 		{
 			distance = Math.sqrt(Math.pow(px-b.getX(),2) + Math.pow(py-b.getY(),2) + Math.pow(pz-b.getZ(), 2));
 			if (distance < min)
 			{
 				min = distance;
 				bmin = b;
 			}
 		}
 		
 		return bmin;
 	}
 	
 	private String getRelativeCoordinates(Player p, Block b)
 	{
 		int dir = (int) ((p.getLocation().getYaw() + 30) / 90);
 		dir %= 4;
 		int x,y,z;
 		x=(b.getX()-p.getLocation().getBlockX());
 		y=(b.getY()-p.getLocation().getBlockY());
 		z=(b.getZ()-p.getLocation().getBlockZ());
 		if (dir == 1)
 			return "("+(0-x)+","+y+","+(0-z)+")";
 		if (dir == 2)
 			return "("+(0-z)+","+y+","+x+")";
 		if (dir == 3)
 			return "("+x+","+y+","+z+")";
 		if (dir == 0)
 			return "("+z+","+y+","+(0-x)+")";
 		
 		//return "("+(b.getX()-p.getLocation().getBlockX())+","+(b.getY()-p.getLocation().getBlockY())+","+(b.getZ()-p.getLocation().getBlockZ())+")";
 		return ""+p.getLocation().getYaw();
 	}
 	//90=-x,180=-z
 	
 }

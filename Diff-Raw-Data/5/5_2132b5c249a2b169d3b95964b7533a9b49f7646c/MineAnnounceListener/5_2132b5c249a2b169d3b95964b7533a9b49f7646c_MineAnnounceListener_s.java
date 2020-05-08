 import java.io.File;
 import java.util.ArrayList;
 
 
 public class MineAnnounceListener extends PluginListener{
 		File file = new File("plugins/config");
 		File file2 = new File ("plugins/config/MineAnnounce");
 	PropertiesFile props1 = new PropertiesFile("plugins/config/MineAnnounce/configuration.properties");
 	PropertiesFile props2 = new PropertiesFile("plugins/config/MineAnnounce/announcements.properties");
 	PropertiesFile props3 = new PropertiesFile("plugins/config/MineAnnounce/Radiusses.properties");
 	PropertiesFile props4 = new PropertiesFile("plugins/config/MineAnnounce/timer.properties");
 	boolean coal;
 	boolean iron;
 	boolean gold;
 	boolean diamond;
 	boolean lapis;
 	boolean redstone;
 	boolean spawner;
 	boolean mossycobble;
 
 	String coals;
 	String irons;
 	String golds;
 	String diamonds;
 	String lapiss;
 	String redstones;
 	String spawners;
 	String mossycobbles;
 
 	int coalr;
 	int ironr;
 	int goldr;
 	int diamondr;
 	int lapisr;
 	int redstoner;
 	int spawnerr;
 	int mossycobbler;
 	
 	int coalt;
 	int iront;
 	int goldt;
 	int diamondt;
 	int lapist;
 	int redstonet;
 	int spawnert;
 	int mossycobblet;
 
 	public void load1(){
 	coal = props1.getBoolean("Announce-Coal",true);
 	iron = props1.getBoolean("Announce-Iron",true);
 	gold = props1.getBoolean("Announce-Gold",true);
 	diamond = props1.getBoolean("Announce-Diamond",true);
 	lapis = props1.getBoolean("Announce-Lapis",true);
 	redstone = props1.getBoolean("Announce-Redstone",true);
 	spawner = props1.getBoolean("Announce-Spawner",true);
 	mossycobble = props1.getBoolean("Announce-MossyCobble",true);
 	}
 	public void load2(){
 		coals = props2.getString("Coal-Text","&1/p &2found &3/a &4coal!");
 		irons = props2.getString("Iron-Text","&1/p &2found &3/a &4Iron!");
 		golds = props2.getString("Gold-Text","&1/p &2found &3/a &4Gold!");
 		diamonds = props2.getString("Diamond-Text","&1/p &2found &3/a &4Diamond!");
 		lapiss = props2.getString("Lapis-Text","&1/p &2found &3/a &4Lapis!");
 		redstones = props2.getString("Redstone-Text","&1/p &2found &3/a &4Redstone!");
 		spawners = props2.getString("Spawner-Text","&1/p &2found &3/a &4Spawner!");
 		mossycobbles = props2.getString("MossyCobble-Text","&1/p &2found &3/a &4MossyCobbleStone!");
 		
 	}
 	public void load3(){
 		coalr = props3.getInt("Coal-Radius",4);
 		ironr = props3.getInt("Iron-Radius",3);
 		goldr = props3.getInt("Gold-Radius",3);
 		diamondr = props3.getInt("Diamond-Radius",3);
 		lapisr = props3.getInt("Lapis-Radius",4);
 		redstoner = props3.getInt("Redstone-Radius",4);
 		spawnerr= props3.getInt("Spawner-Radius",0);
 		mossycobbler = props3.getInt("MossyCobble-Radius",10);
 	}
 	public void load4(){
 		coalt = props4.getInt("Coal-Timer", 30);
		ironr = props4.getInt("Iron-Timer", 30);
 		goldt = props4.getInt("Gold-Timer",30);
 		diamondt = props4.getInt("Diamond-Timer",30);
 		lapist = props4.getInt("Lapis-Timer",30);
 		redstonet = props4.getInt("Redstone-Timer",30);
 		spawnert= props4.getInt("Spawner-Timer",30);
 		mossycobblet = props4.getInt("MossyCobble-Timer",30);
 	}
 	ArrayList<String> listc = new ArrayList<String>();
 	ArrayList<String> listi = new ArrayList<String>();
 	ArrayList<String> listg = new ArrayList<String>();
 	ArrayList<String> listd = new ArrayList<String>();
 	ArrayList<String> listl = new ArrayList<String>();
 	ArrayList<String> listr = new ArrayList<String>();
 	ArrayList<String> lists = new ArrayList<String>();
 	ArrayList<String> listm = new ArrayList<String>();
 	
 	int cam =1;
 	public boolean onBlockBreak(Player player,Block block){
 		if (player.canUseCommand("/mineannounce")){
 		if (block.getType() == 16){
 			if (coal == true){
 				if (!listc.contains(player.getName())){
 				amount(player.getName(), coals, block, coalr);
 				listc.add(player.getName()); timer(listc,coalt*1000, player.getName());
 				return false;
 			}
 			}
 			}else if (block.getType() == 15){
 			if (iron == true){
 				if (!listi.contains(player.getName())){
 					amount(player.getName(), irons, block, ironr);
					listi.add(player.getName());timer(listi,coalt*1000, player.getName());
 					return false;
 				}
 				}
 			}else if (block.getType() == 14){
 					if (gold == true){
 						if (!listg.contains(player.getName())){
 							amount(player.getName(), golds, block, goldr);
 							listg.add(player.getName());timer(listg,goldt*1000, player.getName());
 							return false;
 						}
 					}
 				}else if (block.getType() == 56){
 						if (diamond == true){
 							if (!listd.contains(player.getName())){
 								amount(player.getName(), diamonds, block, diamondr);
 								listd.add(player.getName());timer(listd,diamondt*1000, player.getName());
 								return false;
 						}
 					}
 				}else if (block.getType() == 21){
 						if (lapis == true){
 							if (!listl.contains(player.getName())){
 								amount(player.getName(), lapiss, block, lapisr);
 								listl.add(player.getName());timer(listl,lapist*1000, player.getName());
 								return false;
 						}
 						}
 				}else if (block.getType() == 52){
 					if (spawner == true){
 						if (!lists.contains(player.getName())){
 							amount(player.getName(), spawners, block, spawnerr);
 							lists.add(player.getName());timer(lists,spawnert*1000, player.getName());
 							return false;
 						}
 					}
 				}else if (block.getType() == 48){
 					if (mossycobble == true){
 						if (!listm.contains(player.getName())){
 							amount(player.getName(), mossycobbles, block, mossycobbler);
 							listm.add(player.getName());timer(listm,mossycobblet*1000, player.getName());
 							return false;
 					}
 				}
 				}else if (block.getType() == 74 || block.getType() == 73){
 					if(redstone == true){
 					if (!listr.contains(player.getName())){
 						redstone(player.getName(), redstones, block, redstoner);
 						listr.add(player.getName());timer(listr,redstonet*1000, player.getName());
 						return false;
 					}
 					}
 				}
 		}
 		return false;
 				}
 	
 	public void redstone(String p, String s, Block b, int radius){
 			int xmin = (int)b.getX()-radius;
 			int xmax = (int)b.getX()+radius;
 			int ymin = (int)b.getY()-radius;
 			int ymax = (int)b.getY()+radius;
 			int zmin = (int)b.getZ()-radius;
 			int zmax = (int)b.getZ()+radius;
 			
 			for (int x = xmin; x <= xmax; x++) {
 				for (int y = ymin; y <= ymax; y++) {
 					for (int z = zmin; z <= zmax; z++) {
 						
 						if (b.getWorld().getBlockAt(x, y, z).getType() == 74 ||b.getWorld().getBlockAt(x, y, z).getType() == 73){cam++;}
 					}
 				}
 			}
 			s = s.replaceAll("&", "");
 			s = s.replaceAll("/p", p);
 			s = s.replaceAll("/a", cam+"");
 			etc.getServer().messageAll("3[MA]f "+s);
 			cam = 0;
 	}
 	public void amount(String p, String s, Block b, int radius){
 		int xmin = (int)b.getX()-radius;
 		int xmax = (int)b.getX()+radius;
 		int ymin = (int)b.getY()-radius;
 		int ymax = (int)b.getY()+radius;
 		int zmin = (int)b.getZ()-radius;
 		int zmax = (int)b.getZ()+radius;
 		
 		for (int x = xmin; x <= xmax; x++) {
 			for (int y = ymin; y <= ymax; y++) {
 				for (int z = zmin; z <= zmax; z++) {
 					
 					if (b.getWorld().getBlockAt(x, y, z).getType() == b.getType()){cam++;}
 				}
 			}
 		}
 		s = s.replaceAll("&", "");
 		s = s.replaceAll("/p", p);
 		s = s.replaceAll("/a", cam+"");
 		etc.getServer().messageAll("3[MA]f "+s);
 		cam = 0;
 	  }
 	  
 	  public void timer(final ArrayList<String> list, final int t, final String s){
 		     new Thread() {
 			     public void run() {
 			          try{
 			                Thread.sleep(t);
 			                list.remove(s);
 			          }catch(InterruptedException e) {e.printStackTrace();}
 			     }
 			}.start();
 	  }
 }

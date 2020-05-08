 package com.github.peter200lx.yadp;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Button;
 import org.bukkit.material.Cake;
 import org.bukkit.material.Coal;
 import org.bukkit.material.Crops;
 import org.bukkit.material.DetectorRail;
 import org.bukkit.material.Diode;
 import org.bukkit.material.Directional;
 import org.bukkit.material.Door;
 import org.bukkit.material.Dye;
 import org.bukkit.material.Ladder;
 import org.bukkit.material.Lever;
 import org.bukkit.material.LongGrass;
 import org.bukkit.material.MaterialData;
 import org.bukkit.material.PistonBaseMaterial;
 import org.bukkit.material.PoweredRail;
 import org.bukkit.material.PressurePlate;
 import org.bukkit.material.Pumpkin;
 import org.bukkit.material.Rails;
 import org.bukkit.material.RedstoneTorch;
 import org.bukkit.material.Sign;
 import org.bukkit.material.Stairs;
 import org.bukkit.material.Step;
 import org.bukkit.material.Torch;
 import org.bukkit.material.TrapDoor;
 import org.bukkit.material.Tree;
 import org.bukkit.material.Wool;
 
 public class ToolListener implements Listener {
 	protected static final Logger log = Logger.getLogger("Minecraft");
 
 	@EventHandler
 	public void catchinteract(PlayerInteractEvent event){
 		Player subject = event.getPlayer();
 		Material tool = subject.getItemInHand().getType();
 		if(YADP.tools.containsValue(tool)) {
 			if((YADP.tools.get("dupe")==tool)&&(YADP.hasPerm(subject,"yadp.tool.dupe")))
 				this.dupeTool(event);
 			else if((YADP.tools.get("scroll")==tool)&&(YADP.hasPerm(subject,"yadp.tool.scroll")))
 				this.scrollTool(event);
 		}
 	}
 
 	@SuppressWarnings("deprecation")	//TODO Investigate replacement .updateInventory()
 	private void dupeTool(PlayerInteractEvent event){
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			Player subject = event.getPlayer();
 			Block clicked = event.getClickedBlock();
 			Material type = clicked.getType();
 
 			if(YADP.debug) log.info("[yadp][dupeTool] "+subject.getName()+
 					" clicked "+clicked.getState().getData());
 
 			Material toUse = YADP.dupeMap.get(type);
 			if(toUse == null)
 				toUse = type;
 			if(toUse == Material.AIR) {
 				subject.sendMessage(ChatColor.GREEN + "Duplicating " + ChatColor.GOLD +
 						type.toString()+ ChatColor.GREEN + "is disabled");
 				return;
 			}
 
 			if((clicked.getData() != 0)&&(YADP.keepData.contains(toUse))&& (
 					type.equals(toUse) ||
 					type.equals(Material.WOOL)&&toUse.equals(Material.INK_SACK)   ||
 					type.equals(Material.STEP)&&toUse.equals(Material.DOUBLE_STEP)||
 					type.equals(Material.DOUBLE_STEP)&&toUse.equals(Material.STEP)||
 					type.equals(Material.LOG)&&toUse.equals(Material.LEAVES) ||
 					type.equals(Material.LOG)&&toUse.equals(Material.SAPLING)||
 					type.equals(Material.LEAVES)&&toUse.equals(Material.LOG) ||
 					type.equals(Material.LEAVES)&&toUse.equals(Material.SAPLING)	)	) {
 				subject.getInventory().addItem(new ItemStack(toUse, 64, (short) 0, clicked.getData()));
 			} else {
 				subject.getInventory().addItem(new ItemStack(toUse, 64));
 			}
 			subject.updateInventory();
 			if(YADP.keepData.contains(toUse))
 			{
 				subject.sendMessage(ChatColor.GREEN + "Enjoy your " + ChatColor.GOLD +
 						toUse.toString() + ChatColor.WHITE + ":" +
 						ChatColor.BLUE + data2Str(clicked.getState().getData()));
 			} else {
 				subject.sendMessage(ChatColor.GREEN + "Enjoy your " + ChatColor.GOLD +
 							toUse.toString());
 			}
 		}
 	}
 	
 	private void scrollTool(PlayerInteractEvent event) {
 		Action act = event.getAction();
 		if(act.equals(Action.LEFT_CLICK_BLOCK)||(act.equals(Action.RIGHT_CLICK_BLOCK))) {
 			if(YADP.dataMap.containsKey(event.getClickedBlock().getType())) {
 				Block clicked = event.getClickedBlock();
 				if(YADP.debug) log.info("[yadp][scrollTool] "+event.getPlayer().getName()+
 						" clicked "+clicked.getState().getData());
 				if(event.getPlayer().getGameMode().equals(GameMode.CREATIVE) &&
 										act.equals(Action.LEFT_CLICK_BLOCK)     ) {
 						event.getPlayer().sendMessage("You are in Creative and just " +
 								"destroyed the block "+ clicked.getType());
 						return;
 				}
 
 				int max = YADP.dataMap.get(clicked.getType());
 				byte data = clicked.getData();
 
 				if(max != 0) {
 					data = simpScroll(event, data, max);
 				} else {
 					//TODO Add special case if statements here for complex scrolls
 					MaterialData b = clicked.getState().getData();
 					//if(clicked.getType().equals(Material.NOTE_BLOCK)) {
 					// } else
 					if(clicked.getType().equals(Material.POWERED_RAIL)) {
 						data = simpScroll(event, (byte)(data&0x07), 6);
 						if(((PoweredRail)b).isPowered())
 							data |= 0x08;
 					} else if(clicked.getType().equals(Material.DETECTOR_RAIL)) {
 						data = simpScroll(event, (byte)(data&0x07), 6);
 						if(((DetectorRail)b).isPressed())
 							data |= 0x08;
 					} else {
 						event.getPlayer().sendMessage(clicked.getType()+" is not yet scrollable");
 						return;
 					}
 				}
 
 				clicked.setData(data, false);
 
 				event.getPlayer().sendMessage(ChatColor.GREEN + "Block is now " + ChatColor.GOLD +
 						clicked.getType() + ChatColor.WHITE + ":" +
 						ChatColor.BLUE + data2Str(clicked.getState().getData()));
 			}
 		}
 	}
 
 	private byte simpScroll(PlayerInteractEvent event, byte data, int max) {
 		if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
			if ((data - 1) < 0)
				data = (byte) (max - 1);
			else
				data = (byte) ((data - 1) % max);
 			event.getPlayer().sendMessage("Data value scrolled, you might "+
 					"not see the change");
 		} else if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 			data = (byte) ((data + 1) % max);
 		}
 		return data;
 	}
 
 	private String data2Str(MaterialData b) {
 		Material type = b.getItemType();
 		byte data = b.getData();
 		if(YADP.debug) log.info("[yadp][data2str] Block "+b.toString());
 		if(Material.LOG == type) {
 			if(((Tree)b).getSpecies() != null)
 				return ((Tree)b).getSpecies().toString();
 			else
 				return ""+data;
 		} else if((Material.LEAVES == type)||(Material.SAPLING == type)) {
 			if(((Tree)b).getSpecies() != null)	//More research into LEAVES (not working for high #s
 				return ((Tree)b).getSpecies().toString();
 			else
 				return ""+data;
 		} else if(Material.JUKEBOX == type) {
 			//TODO Find record name list
 			return ""+data;
 		} else if(Material.CROPS == type) {
 			return ((Crops)b).getState().toString();
 		} else if(Material.WOOL == type) {
 				return ((Wool)b).getColor().toString();
 			} else if(Material.INK_SACK == type) {
 				return ((Dye)b).toString();
 		} else if(Material.TORCH == type) {
 			return ((Torch)b).getAttachedFace().toString();
 		} else if((Material.REDSTONE_TORCH_OFF == type)||(Material.REDSTONE_TORCH_ON == type)) {
 			return ((RedstoneTorch)b).getAttachedFace().toString();
 		} else if(Material.RAILS==type) {
 			return ((Rails)b).getDirection() +
 				(	((Rails)b).isCurve() ? " on a curve" : (
 					((Rails)b).isOnSlope() ? " on a slope" : ""	)	);
 		} else if(Material.POWERED_RAIL==type) {
 			return ((PoweredRail)b).getDirection().toString();
 		} else if(Material.DETECTOR_RAIL==type) {
 			return ((DetectorRail)b).getDirection().toString();
 		} else if((Material.WOOD_STAIRS==type)||(Material.COBBLESTONE_STAIRS==type)) {
 			return ((Stairs)b).getFacing().toString();
 		} else if((Material.NETHER_BRICK_STAIRS==type)||
 				(Material.BRICK_STAIRS==type)||(Material.SMOOTH_STAIRS==type)) {
 			if((data&0x3) == 0x0) {
 				return "NORTH";
 			} else if((data&0x3) == 0x1) {
 				return "SOUTH";
 			} else if((data&0x3) == 0x2) {
 				return "EAST";
 			} else if((data&0x3) == 0x3) {
 				return "WEST";
 			}
 			return "" + data;
 		} else if(Material.LEVER == type) {
 			return ((Lever)b).getAttachedFace().toString();
 		} else if((Material.WOODEN_DOOR == type)||(Material.IRON_DOOR_BLOCK == type)) {
 			return ((Door)b).getHingeCorner().toString() + " is " +
 					(((Door)b).isOpen()?"OPEN":"CLOSED");
 		} else if(Material.STONE_BUTTON == type) {
 			return ((Button)b).getAttachedFace().toString();
 		} else if(Material.SIGN_POST == type) {
 			return ((Sign)b).getFacing().toString();
 		} else if(Material.LADDER == type) {
 			return ((Ladder)b).getAttachedFace().toString();
 		} else if(Material.WALL_SIGN == type) {
 			return ((Sign)b).getAttachedFace().toString();
 		} else if(Material.FURNACE == type) {
 			return ((Directional)b).getFacing().toString();
 		} else if(Material.DISPENSER == type) {
 			return ((Directional)b).getFacing().toString();
 		} else if((Material.PUMPKIN==type)||(Material.JACK_O_LANTERN==type)) {
 			return ((Pumpkin)b).getFacing().toString();
 		} else if((Material.STONE_PLATE==type)||(Material.WOOD_PLATE==type)) {
 			return ((PressurePlate)b).isPressed()?" is PRESSED":" is not PRESSED";
 		} else if(Material.COAL == type) {
 			return ((Coal)b).getType().toString();
 		} else if((Material.STEP == type)||((Material.DOUBLE_STEP == type))) {
 			return ((Step)b).getMaterial().toString();
 		} else if(Material.SNOW == type) {
 			if(data == 0x0)			return "1/8 HEIGHT";
 			else if(data == 0x1)	return "2/8 HEIGHT";
 			else if(data == 0x3)	return "3/8 HEIGHT (STEP)";
 			else if(data == 0x4)	return "4/8 HEIGHT (STEP)";
 			else if(data == 0x5)	return "5/8 HEIGHT (STEP)";
 			else if(data == 0x6)	return "6/8 HEIGHT (STEP)";
 			else if(data == 0x7)	return "7/8 HEIGHT (STEP)";
 			else if(data == 0x8)	return "FULL HEIGHT (STEP)";
 			else					return ""+data;
 		} else if(Material.CAKE_BLOCK == type) {
 			return ""+((Cake)b).getSlicesRemaining()+"/6 REMAINING";
 		} else if((Material.DIODE_BLOCK_OFF==type)||(Material.DIODE_BLOCK_ON==type)) {
 			return ((Diode)b).getFacing().toString()+" with DELAY of "+((Diode)b).getDelay();
 		} else if(Material.LONG_GRASS == type) {
 			return ((LongGrass)b).getSpecies().toString();
 		} else if(Material.TRAP_DOOR == type) {
 			return ((TrapDoor)b).getAttachedFace().toString() + " is " +
 									(((TrapDoor)b).isOpen()?"OPEN":"CLOSED");
 		} else if((Material.PISTON_BASE==type)||(Material.PISTON_STICKY_BASE==type)) {
 			return ((PistonBaseMaterial)b).getFacing().toString();
 		} else if(Material.SMOOTH_BRICK == type) {
 			if(data == 0x0)			return "NORMAL";
 			else if(data == 0x1)	return "MOSSY";
 			else if(data == 0x2)	return "CRACKED";
 			else					return ""+data;
 		} else if((Material.HUGE_MUSHROOM_1 == type)||(Material.HUGE_MUSHROOM_2 == type)) {
 			if(data == 0x0)			return "FLESHY PIECE";
 			else if(data == 0x1)	return "CAP ON TOP & W & N";
 			else if(data == 0x2)	return "CAP ON TOP & N";
 			else if(data == 0x3)	return "CAP ON TOP & N & E";
 			else if(data == 0x4)	return "CAP ON TOP & W";
 			else if(data == 0x5)	return "CAP ON TOP";
 			else if(data == 0x6)	return "CAP ON TOP & E";
 			else if(data == 0x7)	return "CAP ON TOP & S & W";
 			else if(data == 0x8)	return "CAP ON TOP & S";
 			else if(data == 0x9)	return "CAP ON TOP & E & S";
 			else if(data == 0x10)	return "STEM";
 			else					return ""+data;
 		} else if(Material.VINE == type) {
 			String ret = "";
 			if((data&0x1) == 0x1) {
 				if(ret.length() == 0)	ret += "SOUTH";
 				else					ret += " & SOUTH";	}
 			if((data&0x2) == 0x2) {
 				if(ret.length() == 0)	ret += "WEST";
 				else					ret += " & WEST";	}
 			if((data&0x4) == 0x4) {
 				if(ret.length() == 0)	ret += "NORTH";
 				else					ret += " & NORTH";	}
 			if((data&0x8) == 0x8) {
 				if(ret.length() == 0)	ret += "EAST";
 				else					ret += " & EAST";	}
 			if(ret.length() == 0)
 				ret += "TOP";
 			return ret;
 		} else if(Material.FENCE_GATE == type) {
 			String append = " is Closed";
 			if((data&0x4) == 0x4)
 				append = " is OPEN";
 			if((data&0x3) == 0x0) {
 				return "SOUTH"+append;
 			} else if((data&0x3) == 0x1) {
 				return "WEST"+append;
 			} else if((data&0x3) == 0x2) {
 				return "NORTH"+append;
 			} else if((data&0x3) == 0x3) {
 				return "EAST"+append;
 			}
 			return ""+data;
 		} else if(Material.MONSTER_EGGS == type) {
 			if(data == 0x0)			return Material.STONE.toString();
 			else if(data == 0x1)	return Material.COBBLESTONE.toString();
 			else if(data == 0x2)	return Material.SMOOTH_BRICK.toString();
 			else					return ""+data;
 		} else if(Material.BREWING_STAND == type) {
 			String ret = "Bottle in ";
 			if((data&0x1) == 0x1) {
 			if(ret.length() == 10)	ret += "EAST Slot";
 			else					ret += " & EAST Slot";	}
 			if((data&0x2) == 0x2) {
 			if(ret.length() == 10)	ret += "SOUTH_WEST Slot";
 			else					ret += " & SOUTH_WEST Slot";	}
 			if((data&0x4) == 0x4) {
 				if(ret.length() == 10)	ret += "NORTH_WEST Slot";
 				else					ret += " & NORTH_WEST Slot";	}
 			if(ret.length() == 10)
 				ret = "Empty";
 			return ret;
 		} else if(Material.CAULDRON == type) {
 			if(data == 0x0)			return "EMPTY";
 			else if(data == 0x1)	return "1/3 FILLED";
 			else if(data == 0x2)	return "2/3 FILLED";
 			else if(data == 0x3)	return "FULL";
 			else					return ""+data;
 		} else if(Material.ENDER_PORTAL_FRAME == type) {
 			//TODO Add intelligence here
 			return "" + data;
 		} else if(Material.EGG == type) {
 			//TODO Is there anywhere we can get a mapping of entity id to name?
 			return "" + data;
 		} else {
 			return "" + data;
 		}
 	}
 }

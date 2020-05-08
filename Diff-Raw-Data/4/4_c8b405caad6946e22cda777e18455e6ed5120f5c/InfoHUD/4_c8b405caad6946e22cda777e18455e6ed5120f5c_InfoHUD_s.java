 package com.qzx.au.hud;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.client.renderer.WorldRenderer;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItemFrame;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.FoodStats;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.EnumSkyBlock;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.Chunk;
 
 #if !defined MC147 && !defined MC152
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.attributes.AttributeModifier;
 import net.minecraft.item.ItemSword;
 import net.minecraft.item.ItemTool;
 #endif
 
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.IShearable;
 
 #ifdef MC147
 import ic2.api.IWrenchable;
 #else
 import ic2.api.tile.IWrenchable;
 #endif
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.lwjgl.opengl.GL11;
 
 import com.qzx.au.util.UI;
 
 @SideOnly(Side.CLIENT)
 public class InfoHUD {
 	private UI ui = new UI();
 
 	public InfoHUD(){}
 
 	//////////
 
 	private static final String[] toolLevels = { "Wooden", "Stone", "Iron", "Diamond", "Unknown" };
 	private static final int[] toolColors = { 0x866526, 0x9a9a9a, 0xffffff, 0x33ebcb, 0x000000 };
 	private void showTool(String tool, int level){
 		if(level > 3 || level < 0) level = 4;
 		this.ui.drawString("(", 0xaaaaaa);
 		this.ui.drawString(this.toolLevels[level] + " " + tool, this.toolColors[level]);
 		this.ui.drawString(") ", 0xaaaaaa);
 	}
 	private void showTool(String tool){
 		this.ui.drawString("(", 0xaaaaaa);
 		this.ui.drawString(tool, 0xffffff); // use iron color
 		this.ui.drawString(") ", 0xaaaaaa);
 	}
 
 	//////////
 
 	private void showItemName(String name, ItemStack item){
 		if(item == null) return;
 
 		try {
 			this.ui.drawString("   " + name + ": ", 0xaaaaaa);
 			this.ui.drawString(item.getDisplayName(), 0xffffff);
 			this.ui.lineBreak();
 		} catch(Exception e){
 			Failure.log("showItemName() entity inspector");
 		}
 	}
 
 	//////////
 
 	private int invItemCX = 0;
 	private int invItemCount = 0;
 	private long invItemTime = 0;
 
 	//////////
 
 	private long ticks;
 	private long time = 0;
 	private static int TPS_QUEUE = 10;
 	private static int TPS_TAIL = TPS_QUEUE-1;
 	private float[] tps_queue;
 	private int tps_tail;
 	private float tps_interval; // seconds
 	private static float MAX_TPS_INTERVAL = 6.0F; // seconds
 
 	private void initTPS(){
 		this.ticks = 0;
 		this.time = System.currentTimeMillis();
 		this.tps_queue = new float[TPS_QUEUE];
 		this.tps_tail = -1;
 		this.tps_interval = 1.0F;
 	}
 
 	//////////
 
 	private int lastBlockID = 0;
 	private int lastBlockMetadata = 0;
 	private class LastDrop {
 		public int id;
 		public int metadata;
 		public String name;
 		public LastDrop(int droppedID, int droppedMetadata, String droppedName){
 			this.id = droppedID;
 			this.metadata = droppedMetadata;
 			this.name = droppedName;
 		}
 		@Override
 		public boolean equals(Object obj){
 			if(obj instanceof LastDrop){
 				LastDrop drop = (LastDrop)obj;
 				if(drop.id == this.id && drop.metadata == this.metadata) return true;
 			}
 			return false;
 		}
 	}
 	private ArrayList<LastDrop> lastDrops = new ArrayList<LastDrop>();
 
 	//////////
 
 	public void draw(Minecraft mc, ScaledResolution screen, EntityPlayer player){
 		GL11.glPushMatrix();
 
 		int pos_x = MathHelper.floor_double(player.posX);
 		int pos_y = MathHelper.floor_double(player.posY);
 		int pos_z = MathHelper.floor_double(player.posZ);
 		int feet_y = MathHelper.floor_double(player.boundingBox.minY);
 
 		World world = mc.isIntegratedServerRunning() ? mc.getIntegratedServer().worldServerForDimension(player.dimension) : mc.theWorld;
 		if(world == null) return;
 
 		Chunk chunk = world.getChunkFromBlockCoords(pos_x, pos_z);
 		if(chunk == null) return;
 
 		this.ui.setCursor(Cfg.info_hud_x, Cfg.info_hud_y);
 
 		// world
 		// biome
 		try {
 			if(Cfg.show_world || Cfg.show_biome){
 				if(Cfg.show_world)
 					this.ui.drawString(world.provider.getDimensionName(), 0xffffff);
 				if(Cfg.show_biome){
 					BiomeGenBase biome = chunk.getBiomeGenForWorldCoords(pos_x & 15, pos_z & 15, world.getWorldChunkManager());
 					if(Cfg.show_world) this.ui.drawString(" (", 0xaaaaaa);
 					this.ui.drawString(biome.biomeName, 0x22aa22);
 					if(Cfg.show_world) this.ui.drawString(")", 0xaaaaaa);
 				}
 				this.ui.lineBreak();
 			}
 		} catch(Exception e){
 			Failure.log("world/biome info element");
 		}
 
 		// player position
 		try {
 			if(Cfg.show_position){
 				this.ui.drawString(String.format("%+.1f", player.posX), 0xffffff);
 				this.ui.drawString(", ", 0xaaaaaa);
 				this.ui.drawString(String.format("%+.1f", player.posZ), 0xffffff);
 				this.ui.drawString(" f: ", 0xaaaaaa);
 				this.ui.drawString(String.format("%.1f", player.boundingBox.minY), 0xffffff);
 				if(Cfg.show_position_eyes){
 					this.ui.drawString(" e: ", 0xaaaaaa);
 					this.ui.drawString(String.format("%.1f", player.posY), 0xffffff);
 				}
 				this.ui.lineBreak();
 			}
 		} catch(Exception e){
 			Failure.log("player position info element");
 		}
 
 		// light levels -- 0-6 red, 7-8 yellow, 9-15 green
 		// time
 		// raining
 		try {
 			if(Cfg.show_light || Cfg.show_time || Cfg.show_weather){
 				if(Cfg.show_light){
 					int light = chunk.getSavedLightValue(EnumSkyBlock.Block, pos_x & 15, feet_y, pos_z & 15);
 					this.ui.drawString("light ", 0xaaaaaa);
 					this.ui.drawString(String.format("%d ", light), (light < 7 ? 0xff6666 : (light < 9 ? 0xffff66 : 0x66ff66)));
 				}
 
 				if(Cfg.show_time){
 					this.ui.drawString("time ", 0xaaaaaa);
 					long time = world.getWorldTime();
 					int hours = (int) (((time / 1000L) + 6) % 24L);
 					int minutes = (int) (time % 1000L / 1000.0D * 60.0D);
 					this.ui.drawString((hours < 10 ? "0" : "") + String.valueOf(hours) + ":" + (minutes < 10 ? "0" : "") + String.valueOf(minutes) + " ",
 						(world.calculateSkylightSubtracted(1.0F) < 4 ? 0xffff66 : 0x666666));
 				}
 				boolean is_raining = false;
 				if(Cfg.show_weather){
 					is_raining = world.isRaining();
 					if(is_raining){
 						if(Cfg.show_light || Cfg.show_time) this.ui.drawString("(", 0xaaaaaa);
 						this.ui.drawString("raining/snowing", 0x6666ff);
 						if(Cfg.show_light || Cfg.show_time) this.ui.drawString(")", 0xaaaaaa);
 					}
 				}
 				if(Cfg.show_light || Cfg.show_time || (Cfg.show_weather && is_raining))
 					this.ui.lineBreak();
 			}
 		} catch(Exception e){
 			Failure.log("light/time/weather info element");
 		}
 
 		// player inventory
 		try {
 			if(Cfg.show_used_inventory){
 				String invText = "used inventory slots: ";
 				ItemStack[] mainInventory = player.inventory.mainInventory;
 				int nr_items = 0;
 				for(int i = 0; i < 36; i++) if(mainInventory[i] != null) nr_items++;
 
 				long time = System.currentTimeMillis();
 				if(nr_items == 36 && Cfg.animate_used_inventory){
 					if(this.invItemCount != 36){
 						this.invItemCX = screen.getScaledWidth()/2 - mc.fontRenderer.getStringWidth(invText+"100%")/2;
 						this.invItemTime = time;
 					}
 				} else
 					this.invItemCX = this.ui.base_x;
 				if(this.invItemCX > this.ui.base_x){
 					this.ui.x = this.invItemCX - (int)((float)this.invItemCX * ((float)(time - this.invItemTime) / 4000.0F));
 					if(this.ui.x < this.ui.base_x) this.ui.x = this.ui.base_x;
 				} else
 					this.ui.x = this.ui.base_x;
 				this.invItemCount = nr_items;
 
 				this.ui.drawString(invText, 0xaaaaaa);
 				this.ui.drawString(String.format("%d", nr_items*100/36), (nr_items == 36 ? 0xff6666 : (nr_items >= 27 ? 0xffff66 : 0x66ff66)));
 				this.ui.drawString("%", 0xaaaaaa);
 				this.ui.lineBreak();
 			}
 		} catch(Exception e){
 			Failure.log("inventory info element");
 		}
 
 		// fps and chunk updates
 		try {
 			if(Cfg.show_fps || Cfg.show_chunk_updates){
 				if(Cfg.show_fps){
 					this.ui.drawString(mc.debug.substring(0, mc.debug.indexOf(" fps")), 0xffffff);
 					this.ui.drawString(" FPS ", 0xaaaaaa);
 				}
 				if(Cfg.show_chunk_updates){
 					String chunk_updates = mc.debug.substring(mc.debug.indexOf(" fps, ")+6, mc.debug.indexOf(" chunk"));
 					int length = chunk_updates.length();
 					this.ui.drawString(chunk_updates, (length < 3 ? 0xffffff : chunk_updates.charAt(0) == '1' && length == 3 ? 0xffff66 : 0xff6666));
 					this.ui.drawString(" chunk updates", 0xaaaaaa);
 				}
 				this.ui.lineBreak();
 			}
 		} catch(Exception e){
 			Failure.log("fps/chunk info element");
 		}
 
 		// rendered entities, total entities, particles
 		try {
 			if(Cfg.show_entities || Cfg.show_particles){
 				if(Cfg.show_entities){
 					String entities = mc.getEntityDebug();
 					this.ui.drawString(entities.substring(entities.indexOf(' ') + 1, entities.indexOf('/')), 0xffffff);
 					this.ui.drawString("/", 0xaaaaaa);
 					this.ui.drawString(entities.substring(entities.indexOf('/') + 1, entities.indexOf('.')), 0xffffff);
 					this.ui.drawString(" entities ", 0xaaaaaa);
 				}
 				if(Cfg.show_particles){
 					this.ui.drawString(mc.effectRenderer.getStatistics(), 0xffffff);
 					this.ui.drawString(" particles", 0xaaaaaa);
 				}
 				this.ui.lineBreak();
 			}
 		} catch(Exception e){
 			Failure.log("entities/particles info element");
 		}
 
 		// TPS
 		try {
 			if(Cfg.show_tps && !Hacks.isGamePaused(mc)){
 				long ticks = world.getWorldInfo().getWorldTotalTime();
 				long time = System.currentTimeMillis();
 				if(time >= this.time + 60000 || (this.ticks != 0 && Math.abs(ticks - this.ticks) > 1200)) this.initTPS(); // re-initialize TPS if last update was more than 1 minute ago
 				if(time >= this.time + (int)(this.tps_interval*1000.0F)){
 					if(this.ticks > 0){
 						if(this.tps_tail == TPS_TAIL)
 							for(int i = 0; i < TPS_TAIL; i++) this.tps_queue[i] = this.tps_queue[i+1];
 						else this.tps_tail++;
 						float tps = (float)(ticks - this.ticks) / this.tps_interval / ((float)(time - this.time) / (this.tps_interval*1000.0F));
 						this.tps_queue[this.tps_tail] = (tps > 20.0F ? 20.0F : tps);
 						if(this.tps_interval < MAX_TPS_INTERVAL) this.tps_interval++;
 					}
 					this.ticks = ticks;
 					this.time = time;
 				}
 
 				if(this.ticks > 0 && this.tps_tail > -1){
 					float tps = 0.0F; for(int i = 0; i <= this.tps_tail; i++) tps += tps_queue[i]; tps = Math.round(tps/(float)(this.tps_tail+1));
 					int[] colors = {0x66ff66, 0xb2ff66, 0xffff66, 0xff6666};
 					if(tps > 20.0F) tps = 20.0F;
 					if(tps < 0.0F) tps = 0.0F;
 					int lag = (int)Math.floor((20.0F - (int)tps) / 5.0F);
 					if(lag > 3) lag = 3;
 					this.ui.drawString(String.format("%d", (int)tps), colors[lag]);
 					this.ui.drawString(" tps (", 0xaaaaaa);
 					this.ui.drawString(String.format("%d%%", (int)tps * 5), colors[lag]);
 					this.ui.drawString(" = ", 0xaaaaaa);
 					this.ui.drawString(String.format("%.1fx", 20.0F / (tps > 0.01F ? tps : 0.01F)), colors[lag]);
 					this.ui.drawString(")", 0xaaaaaa);
 					if(this.tps_tail < TPS_TAIL){
 //						this.ui.drawString("   averaging over "+String.format("%d", (int)this.tps_interval*10)+" seconds, at "
 //											+String.format("%.1f", this.tps_interval)+" second intervals", 0xaa0000);
 						this.ui.drawString(" ", 0xaaaaaa);
 						for(int i = this.tps_tail; i < TPS_TAIL; i++) this.ui.drawString(".", 0xff6666);
 					}
 				} else
 					this.ui.drawString("preparing TPS...", 0xaa0000);
 				this.ui.lineBreak();
 
 //				this.ui.drawString("TPS Queue:", 0xaaaaaa);
 //				for(int i = 0; i <= TPS_TAIL; i++) this.ui.drawString(String.format("  %.2f", this.tps_queue[i]), 0xcccccc);
 //				this.ui.lineBreak();
 			}
 		} catch(Exception e){
 			Failure.log("tps info element");
 		}
 
 		// block at cursor
 		if(Cfg.show_block_name || Cfg.show_inspector){
 			if(mc.objectMouseOver != null){
 				if(mc.objectMouseOver.typeOfHit == EnumMovingObjectType.ENTITY){														// ENTITY
 					// name and ID of entity
 					try {
 						if(mc.objectMouseOver.entityHit instanceof EntityItemFrame){
 							ItemStack stackItemFrame = new ItemStack(Item.itemFrame);
 							this.ui.drawString(stackItemFrame.getDisplayName(), 0xffffff);
 						} else
 							this.ui.drawString(mc.objectMouseOver.entityHit.getEntityName(), 0xffffff);
 						if(Cfg.show_inspector){
 							this.ui.drawString(" (", 0xaaaaaa);
 							if(mc.objectMouseOver.entityHit instanceof EntityItemFrame)
 								this.ui.drawString("e:", 0xaaaaaa);
 							this.ui.drawString(String.format("%d", mc.objectMouseOver.entityHit.entityId), 0xffffff);
 							if(mc.objectMouseOver.entityHit instanceof EntityItemFrame){
 								this.ui.drawString(" i:", 0xaaaaaa);
 								this.ui.drawString(String.format("%d", Item.itemFrame.itemID), 0xffffff);
 							}
 							this.ui.drawString(")", 0xaaaaaa);
 						}
 						this.ui.lineBreak();
 					} catch(Exception e){
 						Failure.log("entity name/ID info element");
 					}
 
 					// name and ID of item in item frame
 					try {
 						if(mc.objectMouseOver.entityHit instanceof EntityItemFrame){
 							ItemStack itemstack = ((EntityItemFrame)mc.objectMouseOver.entityHit).getDisplayedItem();
 							if(itemstack != null){
 								this.ui.drawString("   ", 0xaaaaaa);
 								this.ui.drawString(itemstack.getDisplayName(), 0xffffff);
 								if(Cfg.show_inspector){
 									this.ui.drawString(" (", 0xaaaaaa);
 									if(itemstack.isItemStackDamageable()){
 										int max_durability = itemstack.getMaxDamage();
 										int durability = max_durability - itemstack.getItemDamage();
 										this.ui.drawString(String.format("%d  %d/%d", itemstack.getItem().itemID, durability, max_durability), 0xffffff);
 									} else
 										this.ui.drawString(String.format("%d:%d", itemstack.getItem().itemID, itemstack.getItemDamage()), 0xffffff);
 									this.ui.drawString(")", 0xaaaaaa);
 								}
 								this.ui.lineBreak();
 							}
 						}
 					} catch(Exception e){
 						Failure.log("entity name/ID info element, item frame contents name/ID");
 					}
 
 					if(Cfg.show_inspector && mc.objectMouseOver.entityHit instanceof EntityLiving){
 						EntityLiving entity = (EntityLiving)mc.objectMouseOver.entityHit;
 
 						// health, armor and xp
 						try {
 							#if defined MC147 || defined MC152
 							this.ui.drawString("   max health: ", 0xaaaaaa);
 							this.ui.drawString(String.format("%d", (int)entity.getMaxHealth()), 0xffffff);
 							#else
 							this.ui.drawString("   health: ", 0xaaaaaa);
 							this.ui.drawString(String.format("%d/%d", (int)entity.getHealth(), (int)entity.getMaxHealth()), 0xffffff);
 							#endif
 							int armorValue = entity.getTotalArmorValue();
 							if(armorValue > 0){
 								this.ui.drawString(" armor: ", 0xaaaaaa);
 								this.ui.drawString(String.format("%d", armorValue), 0xffffff);
 							}
 							if(entity.experienceValue > 0){
 								this.ui.drawString(" xp: ", 0xaaaaaa);
 								this.ui.drawString(String.format("%d", entity.experienceValue), 0xffffff);
 							}
 							this.ui.lineBreak();
 						} catch(Exception e){
 							Failure.log("entity inspector, max_health/armor_value/xp");
 						}
 
 						// invulnerable
 						try {
 							if(entity.isEntityInvulnerable()){
 								this.ui.drawString("   is invulnerable", 0xb25bfd);
 								this.ui.lineBreak();
 							}
 						} catch(Exception e){
 							Failure.log("entity inspector, invulnerable");
 						}
 
 						// name of armor and item (5 lines)
 						try {
 							showItemName("helmet", entity.getCurrentItemOrArmor(4));
 							showItemName("chest", entity.getCurrentItemOrArmor(3));
 							showItemName("pants", entity.getCurrentItemOrArmor(2));
 							showItemName("boots", entity.getCurrentItemOrArmor(1));
 							showItemName("hand", entity.getHeldItem());
 						} catch(Exception e){
 							Failure.log("entity inspector, armor/hand");
 						}
 					}
 				} else if(mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE){
 					int blockID = world.getBlockId(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
 					if(blockID > 0){
 						Block block = Block.blocksList[blockID];
 						int inspectX = mc.objectMouseOver.blockX;
 						int inspectY = mc.objectMouseOver.blockY;
 						int inspectZ = mc.objectMouseOver.blockZ;
 						if(block != null){
 							int blockMetadata = world.getBlockMetadata(inspectX, inspectY, inspectZ);
 
 							if(blockID != lastBlockID || blockMetadata != lastBlockMetadata){
 								// reset drops
 								lastBlockID = blockID;
 								lastBlockMetadata = blockMetadata;
 								lastDrops.clear();
 							}
 
 							// name and ID if block is picked
 							try {
 								ItemStack stackBlock = new ItemStack(blockID, 1, blockMetadata);
 								String blockName = ItemUtils.getDisplayName(stackBlock);
 
 								int pickedID = block.idPicked(world, inspectX, inspectY, inspectZ), pickedMetadata = 0;
 								boolean pickedSameAsBlock = false;
 								if(pickedID > 0){
 									pickedMetadata = block.getDamageValue(world, inspectX, inspectY, inspectZ);
 									ItemStack stackPicked = new ItemStack(pickedID, 1, pickedMetadata);
 									String pickedName = ItemUtils.getDisplayName(stackPicked);
 
 									if(blockName != null && pickedName != null)
 										if(blockName.equals(pickedName))
 											pickedSameAsBlock = true;
 									this.ui.drawString(pickedName, "<Unknown Block>", 0xffffff);
 
 									if(Cfg.show_inspector){
 										if(blockID == pickedID && blockMetadata == pickedMetadata){
 											// ID of placed block
 											this.ui.drawString(" (", 0xaaaaaa);
 											this.ui.drawString(String.format("%d:%d", blockID, blockMetadata), 0xffffff);
 											this.ui.drawString(")", 0xaaaaaa);
 										} else {
 											// ID of picked block
 											this.ui.drawString(" (i: ", 0xaaaaaa);
 											this.ui.drawString(String.format("%d:%d", pickedID, pickedMetadata), 0xffffff);
 											this.ui.drawString(")", 0xaaaaaa);
 											// ID of placed block
 											this.ui.drawString(" (b: ", 0xaaaaaa);
 											this.ui.drawString(String.format("%d:%d", blockID, blockMetadata), 0xffffff);
 											this.ui.drawString(")", 0xaaaaaa);
 										}
 									}
 								} else if(Cfg.show_inspector){
 									this.ui.drawString("<Unknown Block>", 0xffffff);
 
 									// ID of placed block
 									this.ui.drawString(" (b: ", 0xaaaaaa);
 									this.ui.drawString(String.format("%d:%d", blockID, blockMetadata), 0xffffff);
 									this.ui.drawString(")", 0xaaaaaa);
 								}
 								this.ui.lineBreak();
 								if(!pickedSameAsBlock && blockName != null && (blockID != pickedID || blockMetadata != pickedMetadata))
 									if(!blockName.equals("")){
 										this.ui.drawString("   ", 0xaaaaaa);
 										this.ui.drawString(blockName, 0xffffff);
 										this.ui.lineBreak();
 									}
 							} catch(Exception e){
 								Failure.log("block name/ID info element");
 							}
 
 							if(Cfg.show_inspector){
 
 								boolean hasTileEntity = block.hasTileEntity(blockMetadata);
 								TileEntity tileEntity = (hasTileEntity ? world.getBlockTileEntity(inspectX, inspectY, inspectZ) : null);
 
 								// creative tab name
 								try {
 									CreativeTabs tab = block.getCreativeTabToDisplayOn();
 									String tabName = "";
 									if(tab != null)
 										tabName = tab.getTranslatedTabLabel();
 									if(!tabName.equals("")){
 										this.ui.drawString("   tab ", 0xaaaaaa);
 										this.ui.drawString(tabName, 0xffffff);
 										this.ui.lineBreak();
 									}
 								} catch(Exception e){
 									Failure.log("block inspector, creative tab");
 								}
 
 								// required tools and levels
 								try {
 									int swordLevel = MinecraftForge.getBlockHarvestLevel(block, blockMetadata, "sword");
 									int axeLevel = MinecraftForge.getBlockHarvestLevel(block, blockMetadata, "axe");
 									int pickaxeLevel = MinecraftForge.getBlockHarvestLevel(block, blockMetadata, "pickaxe");
 									int shovelLevel = MinecraftForge.getBlockHarvestLevel(block, blockMetadata, "shovel");
 									int scoopLevel = MinecraftForge.getBlockHarvestLevel(block, blockMetadata, "scoop"); // TC
 // TODO: thaumcraft grafter
 									boolean shearable = block instanceof IShearable;
 									boolean ic2_wrenchable = false;
 									if(AUHud.supportIC2)
 										if(tileEntity instanceof IWrenchable)
 											ic2_wrenchable = true;
 									if(swordLevel != -1 || axeLevel != -1 || pickaxeLevel != -1 || shovelLevel != -1 || scoopLevel != -1 || shearable || ic2_wrenchable){
 										this.ui.drawString("   use ", 0xaaaaaa);
 										if(swordLevel != -1) this.showTool("Sword", swordLevel);
 										if(axeLevel != -1) this.showTool("Axe", axeLevel);
 										if(pickaxeLevel != -1) this.showTool("Pickaxe", pickaxeLevel);
 										if(shovelLevel != -1) this.showTool("Shovel", shovelLevel);
 										if(scoopLevel != -1) this.showTool("Scoop");
 										if(shearable) this.showTool("Shears");
 										if(ic2_wrenchable) this.showTool("IC2 Wrench");
 										this.ui.lineBreak();
 									}
 								} catch(Exception e){
 									Failure.log("block inspector, required tool");
 								}
 
 								// item dropped when broken
 								try {
 									int droppedID = block.idDropped(blockMetadata, new Random(), 0);
 									if(droppedID > 0){
 										int droppedMetadata = block.damageDropped(blockMetadata);
 										ItemStack stackDropped = new ItemStack(droppedID, 1, droppedMetadata);
 										String droppedName = ItemUtils.getDisplayName(stackDropped);
 
 										if(droppedID == 0 || droppedName != null){
 											// add to drops list, if not already
 											LastDrop thisDrop = new LastDrop(droppedID, droppedMetadata, droppedName);
 											if(!lastDrops.contains(thisDrop))
 												lastDrops.add(thisDrop);
 										}
 										int nr_drops = lastDrops.size();
 										boolean dropSelf = true;
 										for(int i = 0; i < nr_drops; i++){
 											LastDrop drop = lastDrops.get(i);
 											if(drop.id == 0){
 												this.ui.drawString("   no drop", 0xaaaaaa);
 												dropSelf = false;
 											} else {
 												if(drop.id == blockID && drop.metadata == blockMetadata){
 													// drops itself
 													this.ui.drawString("   drops as is", 0xaaaaaa);
 												} else {
 													// drops something other than self
 													this.ui.drawString("   drops ", 0xaaaaaa);
 													this.ui.drawString((drop.name == null ? "<Unknown>" : drop.name), 0xffffff);
 													this.ui.drawString(" (", 0xaaaaaa);
 													this.ui.drawString(String.format("%d:%d", drop.id, drop.metadata), 0xffffff);
 													this.ui.drawString(")", 0xaaaaaa);
 													dropSelf = false;
 												}
 											}
 											this.ui.lineBreak();
 										}
 
 										// silkable
 										boolean silkable = block.canSilkHarvest(world, player, inspectX, inspectY, inspectZ, blockMetadata);
 										if(silkable && !dropSelf){
 											this.ui.drawString("   silkable", 0xb25bfd);
 											this.ui.lineBreak();
 										}
 									}
 								} catch(Exception e){
 									Failure.log("block inspector, drops as");
 								}
 
 								// redstone
 								try {
 									int isProvidingSide = (mc.objectMouseOver.sideHit == 0 ? 1 : (mc.objectMouseOver.sideHit == 1 ? 0 : mc.objectMouseOver.sideHit));
 									#ifdef MC147
 									int isProvidingWeakPower = (block.isProvidingWeakPower(world, inspectX, inspectY, inspectZ, isProvidingSide) ? -1 : 0);
 									int isProvidingStrongPower = (block.isProvidingStrongPower(world, inspectX, inspectY, inspectZ, isProvidingSide) ? -1 : 0);
 									int blockPowerInput = (world.isBlockGettingPowered(inspectX, inspectY, inspectZ) ? -1 : 0);
 									#else
 									int isProvidingWeakPower = block.isProvidingWeakPower(world, inspectX, inspectY, inspectZ, isProvidingSide);
 									int isProvidingStrongPower = block.isProvidingStrongPower(world, inspectX, inspectY, inspectZ, isProvidingSide);
 									int blockPowerInput = world.getBlockPowerInput(inspectX, inspectY, inspectZ);
 									#endif
 									this.ui.drawString("   in: ", 0xaaaaaa);
 									this.ui.drawString((blockPowerInput > 0 ? String.format("%d", blockPowerInput) : (blockPowerInput == -1 ? "x" : "_")), 0xffffff);
 									this.ui.drawString(" out: ", 0xaaaaaa);
 									this.ui.drawString((isProvidingWeakPower > 0 ? String.format("%d", isProvidingWeakPower) : (isProvidingWeakPower == -1 ? "x" : "_")), 0xffffff);
 									this.ui.drawString(" / ", 0xaaaaaa);
 									this.ui.drawString((isProvidingStrongPower > 0 ? String.format("%d", isProvidingStrongPower) : (isProvidingStrongPower == -1 ? "x" : "_")), 0xffffff);
 									this.ui.lineBreak();
 								} catch(Exception e){
 									Failure.log("block inspector, redstone");
 								}
 
 								if(Cfg.enable_advanced_inspector){
 
 									// brightness, hardness, resistance
 									try {
 										int brightness = block.getLightValue(world, inspectX, inspectY, inspectZ);
 										float hardness = block.getBlockHardness(world, inspectX, inspectY, inspectZ);
 										float resistance = block.getExplosionResistance(null);
 										this.ui.drawString("   b: ", 0xaaaaaa);
 										this.ui.drawString((brightness > 0 ? String.format("%d", brightness) : "_"), 0xffffff);
 										this.ui.drawString(" h: ", 0xaaaaaa);
 										this.ui.drawString((hardness == -1 ? "unbreakable" : String.format("%.1f", hardness)), 0xffffff);
 										this.ui.drawString(" r: ", 0xaaaaaa);
 										this.ui.drawString(String.format("%.1f", resistance), 0xffffff);
 										this.ui.lineBreak();
 									} catch(Exception e){
 										Failure.log("advanced block inspector, b.h.r.");
 									}
 
 									// has tile entity, has random ticks, has ticking tile entity, tile entities in block, total tile entities around player
 									try {
 										boolean tickRandomly = block.getTickRandomly();
 										boolean tickingEntity = false;
 										if(tileEntity != null)
 											if(tileEntity.canUpdate())
 												tickingEntity = true;
 										this.ui.drawString("   e: ", 0xaaaaaa);
 										this.ui.drawString((hasTileEntity ? "x" : "_"), 0xffffff);
 										this.ui.drawString(" r: ", 0xaaaaaa);
 										this.ui.drawString((tickRandomly ? "x" : "_"), 0xffffff);
 										this.ui.drawString(" t: ", 0xaaaaaa);
 										this.ui.drawString((tickingEntity ? "x" : "_"), 0xffffff);
 
 										ArrayList tileEntityList = (ArrayList)world.loadedTileEntityList;
 										int nr_tileEntities = tileEntityList.size();
 										int nr_tileEntitiesAtPos = 0;
 										int nr_tileEntitiesTicking = 0;
 										for(int t = 0; t < nr_tileEntities; t++){
 											TileEntity te = (TileEntity)tileEntityList.get(t);
 											if(te.xCoord == inspectX && te.yCoord == inspectY && te.zCoord == inspectZ)
 												nr_tileEntitiesAtPos++;
 											if(te.canUpdate())
 												nr_tileEntitiesTicking++;
 										}
 										if(nr_tileEntitiesAtPos == 0 && hasTileEntity)
 											nr_tileEntitiesAtPos = world.getBlockTileEntity(inspectX, inspectY, inspectZ) == null ? 0 : 1;
 										this.ui.drawString(" b: ", 0xaaaaaa);
 										this.ui.drawString((nr_tileEntitiesAtPos > 0 ? String.format("%d", nr_tileEntitiesAtPos) : "_"), 0xffffff);
 										this.ui.drawString(" a: ", 0xaaaaaa);
 										this.ui.drawString((nr_tileEntities > 0 ? String.format("%d", nr_tileEntities) : "_"), 0xffffff);
 										this.ui.drawString(" (", 0xaaaaaa);
 										this.ui.drawString((nr_tileEntitiesTicking > 0 ? String.format("%d", nr_tileEntitiesTicking) : "_"), 0xffffff);
 										this.ui.drawString(")", 0xaaaaaa);
 										this.ui.lineBreak();
 									} catch(Exception e){
 										Failure.log("advanced block inspector, e.r.t.b.a.");
 									}
 
 									// normal, opaque, solid
 									try {
 										boolean solid = block.isBlockSolid(world, inspectX, inspectY, inspectZ, mc.objectMouseOver.sideHit);
 										this.ui.drawString("   n: ", 0xaaaaaa);
 										this.ui.drawString((block.isNormalCube(blockID) ? "x" : "_"), 0xffffff);
 										this.ui.drawString(" o: ", 0xaaaaaa);
 										this.ui.drawString((block.isOpaqueCube() ? "x" : "_"), 0xffffff);
 										this.ui.drawString(" s: ", 0xaaaaaa);
 										this.ui.drawString((solid ? "x" : "_"), 0xffffff);
 										this.ui.lineBreak();
 									} catch(Exception e){
 										Failure.log("advanced block inspector, n.o.s.");
 									}
 
 								} // END Cfg.enable_advanced_inspector
 
 							} // END Cfg.show_inspector
 						}
 					}
 				}
 			}
 		}
 
 		// SELF: armor value inspector
 		try {
 			if(Cfg.show_inspector && !player.capabilities.isCreativeMode){
 				int armorValue = player.getTotalArmorValue();
 				if(armorValue > 0){
 					this.ui.setCursor(screen.getScaledWidth()/2 - 95, screen.getScaledHeight() - 70);
 					this.ui.drawString(" armor: ", 0xaaaaaa);
 					this.ui.drawString(String.format("%d", armorValue), 0xffffff);
 				}
 			}
 		} catch(Exception e){
 			Failure.log("armor value inspector");
 		}
 
 		// SELF: damage inspector
 		try {
 			if(Cfg.show_inspector && !player.capabilities.isCreativeMode){
 				Entity entity = null;
 				Block block = null;
 				float damageEntity = 1.0F;
 				float damageBlock = 1.0F;
 
 				ItemStack hand = player.getHeldItem();
 				Item item = (hand != null ? Item.itemsList[hand.itemID] : null);
 				if(item != null){
 					if(mc.objectMouseOver != null){
 						if(mc.objectMouseOver.typeOfHit == EnumMovingObjectType.ENTITY){
 							entity = mc.objectMouseOver.entityHit;
 						} else if(mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE){
 							int blockID = world.getBlockId(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
 							if(blockID > 0)
 								block = Block.blocksList[blockID];
 						}
 					}
 
 					try {
						#if defined MC147 || defined MC152
 						damageEntity = (float)item.getDamageVsEntity(entity, hand);
 						#else
 						if(item instanceof ItemTool){
 							entity = null;
 							damageEntity = ((ItemTool)item).damageVsEntity;
 						} else if(item instanceof ItemSword){
 							entity = null;
 //							damageEntity = (float)((AttributeModifier)item.getItemAttributeModifiers().get(SharedMonsterAttributes.attackDamage)).getAmount();
 						} else
 							damageEntity = item.getDamageVsEntity(entity, hand);
 // TODO: getDamageVsEntity is deprecated
 						#endif
 					} catch(Exception e){
 						// possible? item.getDamageVsEntity
 					}
 					if(block != null)
 						try {
 							damageBlock = item.getStrVsBlock(hand, block);
 						} catch(Exception e){
 							// possible? item.getStrVsBlock
 						}
 				}
 				this.ui.setCursor(screen.getScaledWidth()/2 + 10, screen.getScaledHeight() - 70);
 				this.ui.drawString("damage: ", 0xaaaaaa);
 				this.ui.drawString(String.format("%s%.1f", (entity == null ? "*" : ""), damageEntity), 0xffffff);
 				this.ui.drawString("e ", 0xaaaaaa);
 				this.ui.drawString(String.format("%s%.1f", (block == null ? "*" : ""), damageBlock), 0xffffff);
 				this.ui.drawString("b", 0xaaaaaa);
 			}
 		} catch(Exception e){
 			Failure.log("damage inspector");
 		}
 
 		// SELF: food inspector
 		try {
 			if(Cfg.show_inspector && !player.capabilities.isCreativeMode){
 				FoodStats food = player.getFoodStats();
 				this.ui.setCursor(screen.getScaledWidth()/2 + 10, screen.getScaledHeight() - 50);
 				this.ui.drawString("f: ", 0xaaaaaa);
 				this.ui.drawString(String.format("%d%%", 100*food.getFoodLevel()/20), 0xffffff);
 				this.ui.drawString("  s: ", 0xaaaaaa);
 				this.ui.drawString(String.format("%d%%", (int)Math.round(100*food.getSaturationLevel()/20)), 0xffffff);
 			}
 		} catch(Exception e){
 			Failure.log("food inspector");
 		}
 
 // TODO: memory
 //	long maxMemory = Runtime.getRuntime().maxMemory();
 //	long totalMemory = Runtime.getRuntime().totalMemory();
 //	long freeMemory = Runtime.getRuntime().freeMemory();
 
 		GL11.glPopMatrix();
 	}
 }

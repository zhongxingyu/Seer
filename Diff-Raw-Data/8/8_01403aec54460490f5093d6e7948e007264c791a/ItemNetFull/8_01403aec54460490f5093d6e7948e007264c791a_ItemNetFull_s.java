 package craiggowing.mod.netmod;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.passive.EntityPig;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemDye;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityMobSpawner;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 
 public class ItemNetFull extends ItemNet
 {
     public ItemNetFull(int par1)
     {
         super(par1);
         this.setHasSubtypes(true);
         this.setMaxDamage(0);
         this.maxStackSize = 1;
         this.setIconIndex(16);
     }
 
     @SideOnly(Side.CLIENT)
     public int getIconFromDamage(int par1)
     {
         int var2 = MathHelper.clamp_int(par1, 0, mod_NetMod.mobTotal-1);
         return this.iconIndex + var2;
     }
 
     public String getItemNetName(ItemStack par1ItemStack)
     {
         int var2 = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, mod_NetMod.mobTotal-1);
         return StatCollector.translateToLocal(mod_NetMod.itemNames[var2][0]);
     }
 
     public String getMobName(ItemStack par1ItemStack)
     {
         int var2 = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, mod_NetMod.mobTotal-1);
         return mod_NetMod.itemNames[var2][1];
     }
 
     protected EntityLiving getEntityToSpawn(ItemStack par1ItemStack, World par2World)
     {
         int var2 = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, mod_NetMod.mobTotal-1);
         try
         {
             return (EntityLiving)mod_NetMod.itemClasses[var2].getConstructor(new Class[] {World.class}).newInstance(new Object[] {par2World});
         }
         catch (Exception e)
         {
             return new EntityPig(par2World);
         }
     }
 
     public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
     {
         super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
         par3List.add(this.getItemNetName(par1ItemStack));
         NBTTagCompound tag = null;
         if (par1ItemStack.getTagCompound() != null && par1ItemStack.getTagCompound().hasKey("Creature"))
         {
             NBTTagList tl = par1ItemStack.getTagCompound().getTagList("Creature");
             tag = (NBTTagCompound)tl.tagAt(0);
         }
         if (tag != null)
         {
             if (tag.hasKey("Health"))
             {
                 par3List.add(StatCollector.translateToLocal("netmod.health") + ": " + tag.getShort("Health"));
             }
             if (tag.hasKey("Age"))
             {
                 par3List.add(StatCollector.translateToLocal("netmod.age") + ": " + tag.getInteger("Age"));
             }
             if (tag.hasKey("InLove"))
             {
                 par3List.add(StatCollector.translateToLocal("netmod.inlove") + ": " + tag.getInteger("InLove"));
             }
             if (tag.hasKey("Saddle"))
             {
                 par3List.add(StatCollector.translateToLocal("netmod.hassaddle") + ": " + (tag.getBoolean("Saddle") ? "Yes" : "No"));
             }
             if (tag.hasKey("Sheared"))
             {
                 par3List.add(StatCollector.translateToLocal("netmod.sheared") + ": " + (tag.getBoolean("Sheared") ? "Yes" : "No"));
             }
             if (tag.hasKey("Color"))
             {
                 par3List.add(StatCollector.translateToLocal("netmod.fleece") + ": " + StatCollector.translateToLocal("item.fireworksCharge." + ItemDye.dyeColorNames[15 - (tag.getByte("Color") & 15)]));
             }
             if (tag.hasKey("Size"))
             {
                 par3List.add(StatCollector.translateToLocal("netmod.size") + ": " + tag.getInteger("Size") + 1);
             }
             if (tag.hasKey("Equipment"))
             {
                 NBTTagList eqtl = tag.getTagList("Equipment");
                 if (eqtl.tagCount() > 0)
                 {
                     NBTTagCompound itemNBT = null;
                     Item item = null;
                     byte stackSize = 0;
                     short itemDamage = 0;
                     ArrayList Entries = new ArrayList();
                     for (int var3 = 0; var3 < eqtl.tagCount(); ++var3)
                     {
                         itemNBT = (NBTTagCompound)eqtl.tagAt(var3);
                         item = Item.itemsList[itemNBT.getShort("id")];
                         if (item != null)
                         {
                             stackSize = itemNBT.getByte("Count");
                             itemDamage = itemNBT.getShort("Damage");
                             Entries.add("* " + StatCollector.translateToLocal(item.getItemName()) + " (#" + stackSize + ", " + itemDamage + ")");
                         }
                     }
                     if (!Entries.isEmpty())
                     {
                         par3List.add(StatCollector.translateToLocal("netmod.equipment") + ": " + Entries.size());
                         for (int var3 = 0; var3 < Entries.size(); ++var3)
                         {
                             par3List.add(Entries.get(var3));
                         }
                     }
                 }
             }
         }
         else
         {
             par3List.add("Random");
         }
     }
 
     public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
     {
         Random random = new Random();
         int var2 = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, mod_NetMod.mobTotal-1);
 
         if (!par3EntityPlayer.capabilities.isCreativeMode)
         {
             --par1ItemStack.stackSize;
         }
 
         /* Are we clicking on a diamond block? */
         MovingObjectPosition var12 = this.getMovingObjectPositionFromPlayer(par2World, par3EntityPlayer, true);
         if (var12 != null && var12.typeOfHit == EnumMovingObjectType.TILE)
         {
             int var13 = var12.blockX;
             int var14 = var12.blockY;
             int var15 = var12.blockZ;
 
             if (par2World.canMineBlock(par3EntityPlayer, var13, var14, var15) && par3EntityPlayer.canPlayerEdit(var13, var14, var15, var12.sideHit, par1ItemStack))
             {
                 if (par2World.getBlockId(var13, var14, var15) == Block.blockDiamond.blockID || par2World.getBlockId(var13, var14, var15) == Block.blockEmerald.blockID)
                 {
                     if (!par2World.isRemote)
                     {
                         par2World.setBlockAndMetadataWithNotify(var13, var14, var15, Block.mobSpawner.blockID, 1);
                         TileEntity te = par2World.getBlockTileEntity(var13, var14, var15);
                         if (te != null && te instanceof TileEntityMobSpawner)
                         {
                             TileEntityMobSpawner tems = (TileEntityMobSpawner)te;
                             tems.setMobID(this.getMobName(par1ItemStack));
                         }
                    }
                    if (random.nextFloat() >= mod_NetMod.itemProbs[var2][1])
                    {
                        par3EntityPlayer.dropItem(mod_NetMod.itemNet.itemID, 1);
                     }
                     par2World.playSoundAtEntity(par3EntityPlayer, "mob.endermen.stare", 3.0F, 1.0F);
                     return par1ItemStack;
                 }
             }
         }
 
         /* Throw creature if not */
 
         if (!par2World.isRemote)
         {
             EntityLiving spawnEntity = this.getEntityToSpawn(par1ItemStack, par2World);
             this.setEntityAttributes(par1ItemStack, spawnEntity, par2World, par3EntityPlayer);
             par2World.spawnEntityInWorld(spawnEntity);
             if (random.nextFloat() >= mod_NetMod.itemProbs[var2][1])
             {
                 par3EntityPlayer.dropItem(mod_NetMod.itemNet.itemID, 1);
             }
             spawnEntity.playLivingSound();
         }
 
         return par1ItemStack;
     }
 
     private void setEntityAttributes(ItemStack par1ItemStack, EntityLiving spawnEntity, World par2World, EntityPlayer par3EntityPlayer)
     {
         spawnEntity.setLocationAndAngles(par3EntityPlayer.posX, par3EntityPlayer.posY + (double)par3EntityPlayer.getEyeHeight(), par3EntityPlayer.posZ, par3EntityPlayer.rotationYaw, par3EntityPlayer.rotationPitch);
         spawnEntity.posX -= (double)(MathHelper.cos(spawnEntity.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
         spawnEntity.posY -= 0.10000000149011612D;
         spawnEntity.posZ -= (double)(MathHelper.sin(spawnEntity.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
         spawnEntity.setPosition(spawnEntity.posX, spawnEntity.posY, spawnEntity.posZ);
         spawnEntity.yOffset = 0.0F;
         float var3 = 0.4F;
         spawnEntity.motionX = (double)(-MathHelper.sin(spawnEntity.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(spawnEntity.rotationPitch / 180.0F * (float)Math.PI) * var3) * 2.0;
         spawnEntity.motionZ = (double)(MathHelper.cos(spawnEntity.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(spawnEntity.rotationPitch / 180.0F * (float)Math.PI) * var3) * 2.0;
         spawnEntity.motionY = (double)(-MathHelper.sin((spawnEntity.rotationPitch) / 180.0F * (float)Math.PI) * var3) * 2.0;
         if (par1ItemStack.getTagCompound() != null && par1ItemStack.getTagCompound().hasKey("Creature"))
         {
             NBTTagList tl = par1ItemStack.getTagCompound().getTagList("Creature");
             spawnEntity.readEntityFromNBT((NBTTagCompound)tl.tagAt(0));
         }
         else
         {
             spawnEntity.initCreature();
         }
     }
 
     @SideOnly(Side.CLIENT)
     public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
     {
         for (int var4 = 0; var4 < mod_NetMod.mobTotal; ++var4)
         {
             par3List.add(new ItemStack(par1, 1, var4));
         }
     }
 }

 package nuclearcontrol;
 
 import forge.ISidedInventory;
 import ic2.api.INetworkClientTileEntityEventListener;
 import ic2.api.INetworkDataProvider;
 import ic2.api.INetworkUpdateListener;
 import ic2.api.IWrenchable;
 import ic2.api.NetworkHelper;
 import java.util.ArrayList;
 import java.util.List;
 import net.minecraft.server.EntityHuman;
 import net.minecraft.server.Facing;
 import net.minecraft.server.Item;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.TileEntity;
 import net.minecraft.server.mod_IC2NuclearControl;
 import nuclearcontrol.panel.IPanelDataSource;
 import org.bukkit.craftbukkit.entity.CraftHumanEntity;
 import java.util.List;
 import java.util.ArrayList;
 
 public class TileEntityInfoPanel extends TileEntity implements ISlotItemFilter, INetworkDataProvider, INetworkUpdateListener, INetworkClientTileEntityEventListener, IWrenchable, IRedstoneConsumer, ITextureHelper, IScreenPart, ISidedInventory, IRotation
 {
     private static final int CARD_TYPE_MAX = 3;
     public static final int BORDER_NONE = 0;
     public static final int BORDER_LEFT = 1;
     public static final int BORDER_RIGHT = 2;
     public static final int BORDER_TOP = 4;
     public static final int BORDER_BOTTOM = 8;
     public static final int DISPLAY_DEFAULT = Integer.MAX_VALUE;
     public static final int SLOT_CARD = 0;
     public static final int SLOT_UPGRADE = 1;
     private static final int LOCATION_RANGE = 8;
     protected int updateTicker;
     protected int tickRate;
     protected boolean init = false;
     private ItemStack[] inventory = new ItemStack[2];
     private Screen screen = null;
     private ItemStack card = null;
     private boolean prevPowered;
     public boolean powered;
     public int[] displaySettings;
     private int prevRotation;
     public int rotation;
     private short prevFacing;
     public short facing;
     public List transaction = new ArrayList();
 
     public void onOpen(CraftHumanEntity crafthumanentity)
     {
         transaction.add(crafthumanentity);
     }
 
     public void onClose(CraftHumanEntity crafthumanentity)
     {
         transaction.remove(crafthumanentity);
     }
 
     public List getViewers()
     {
         return transaction;
     }
 
     public void setMaxStackSize(int i)
     {
     }
 
     public ItemStack[] getContents()
     {
         return inventory;
     }
 
     public short getFacing()
     {
         return (short)Facing.OPPOSITE_FACING[this.facing];
     }
 
     public void setFacing(short var1)
     {
         this.setSide((short)Facing.OPPOSITE_FACING[var1]);
     }
 
     private void setCard(ItemStack var1)
     {
         this.card = var1;
         NetworkHelper.updateTileEntityField(this, "card");
     }
 
     private void setSide(short var1)
     {
         this.facing = var1;
 
         if (this.prevFacing != var1)
         {
             NetworkHelper.updateTileEntityField(this, "facing");
         }
 
         this.prevFacing = var1;
     }
 
     public void setPowered(boolean var1)
     {
         this.powered = var1;
 
         if (this.prevPowered != var1)
         {
             NetworkHelper.updateTileEntityField(this, "powered");
         }
 
         this.prevPowered = this.powered;
     }
 
     public boolean getPowered()
     {
         return this.powered;
     }
 
     public void setDisplaySettings(int var1)
     {
         int var2 = 0;
 
         if (this.inventory[0] != null && this.inventory[0].getItem() instanceof IPanelDataSource)
         {
             var2 = ((IPanelDataSource)this.inventory[0].getItem()).getCardType();
         }
 
         if (var2 > 3)
         {
             var2 = 0;
         }
 
         boolean var3 = this.displaySettings[var2] != var1;
         this.displaySettings[var2] = var1;
 
         if (var3)
         {
             NetworkHelper.updateTileEntityField(this, "displaySettings");
         }
     }
 
     public void onNetworkUpdate(String var1)
     {
         if (var1.equals("facing") && this.prevFacing != this.facing)
         {
             if (mod_IC2NuclearControl.isClient())
             {
                 mod_IC2NuclearControl.screenManager.unregisterScreenPart(this);
                 mod_IC2NuclearControl.screenManager.registerInfoPanel(this);
             }
 
             this.world.notify(this.x, this.y, this.z);
             this.prevFacing = this.facing;
         }
 
         if (var1.equals("card"))
         {
             this.inventory[0] = this.card;
         }
 
         if (var1.equals("powered") && this.prevPowered != this.powered)
         {
             if (this.screen != null)
             {
                 this.screen.turnPower(this.powered);
             }
             else
             {
                 this.world.notify(this.x, this.y, this.z);
                 this.world.v(this.x, this.y, this.z);
             }
 
             this.prevPowered = this.powered;
         }
 
         if (var1.equals("rotation") && this.prevRotation != this.rotation)
         {
             this.world.notify(this.x, this.y, this.z);
             this.prevRotation = this.rotation;
         }
     }
 
     public void onNetworkEvent(EntityHuman var1, int var2)
     {
         this.setDisplaySettings(var2);
     }
 
     public TileEntityInfoPanel()
     {
         this.tickRate = IC2NuclearControl.screenRefreshPeriod;
         this.updateTicker = this.tickRate;
         this.displaySettings = new int[4];
 
         for (int var1 = 0; var1 <= 3; ++var1)
         {
             this.displaySettings[var1] = Integer.MAX_VALUE;
         }
 
         this.powered = false;
         this.prevPowered = false;
         this.facing = 0;
         this.prevFacing = 0;
         this.prevRotation = 0;
         this.rotation = 0;
     }
 
     public List getNetworkedFields()
     {
         ArrayList var1 = new ArrayList(5);
         var1.add("powered");
         var1.add("displaySettings");
         var1.add("facing");
         var1.add("rotation");
         var1.add("card");
         return var1;
     }
 
     protected void initData()
     {
         if (this.world.isStatic)
         {
             NetworkHelper.requestInitialData(this);
         }
         else
         {
             RedstoneHelper.checkPowered(this.world, this);
         }
 
         if (mod_IC2NuclearControl.isClient())
         {
             mod_IC2NuclearControl.screenManager.registerInfoPanel(this);
         }
 
         this.init = true;
     }
 
     /**
      * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
      * ticks and creates a new spawn inside its implementation.
      */
     public void q_()
     {
         if (!this.init)
         {
             this.initData();
         }
 
         if (!this.world.isStatic)
         {
             if (this.updateTicker-- > 0)
             {
                 return;
             }
 
             this.updateTicker = this.tickRate;
             this.update();
         }
 
         super.q_();
     }
 
     /**
      * Reads a tile entity from NBT.
      */
     public void a(NBTTagCompound var1)
     {
         super.a(var1);
 
         if (var1.hasKey("rotation"))
         {
             this.prevRotation = this.rotation = var1.getInt("rotation");
         }
 
         this.prevFacing = this.facing = var1.getShort("facing");
         int var3;
 
         if (var1.hasKey("dSets"))
         {
            int[] var2 = var1.func_48445_l("dSets");
 
             if (var2.length == this.displaySettings.length)
             {
                 this.displaySettings = var2;
             }
             else
             {
                 for (var3 = 0; var3 < var2.length; ++var3)
                 {
                     this.displaySettings[var3] = var2[var3];
                 }
             }
         }
         else
         {
             this.displaySettings[0] = var1.getInt("displaySettings");
         }
 
         NBTTagList var6 = var1.getList("Items");
         this.inventory = new ItemStack[this.getSize()];
 
         for (var3 = 0; var3 < var6.size(); ++var3)
         {
             NBTTagCompound var4 = (NBTTagCompound)var6.get(var3);
             byte var5 = var4.getByte("Slot");
 
             if (var5 >= 0 && var5 < this.inventory.length)
             {
                 this.inventory[var5] = ItemStack.a(var4);
 
                 if (var5 == 0)
                 {
                     this.card = this.inventory[var5];
                 }
             }
         }
 
         this.update();
     }
 
     /**
      * invalidates a tile entity
      */
     public void j()
     {
         super.j();
 
         if (mod_IC2NuclearControl.isClient())
         {
             mod_IC2NuclearControl.screenManager.unregisterScreenPart(this);
         }
     }
 
     /**
      * Writes a tile entity to NBT.
      */
     public void b(NBTTagCompound var1)
     {
         super.b(var1);
         var1.setShort("facing", this.facing);
        var1.func_48446_a("dSets", this.displaySettings);
         var1.setInt("rotation", this.rotation);
         NBTTagList var2 = new NBTTagList();
 
         for (int var3 = 0; var3 < this.inventory.length; ++var3)
         {
             if (this.inventory[var3] != null)
             {
                 NBTTagCompound var4 = new NBTTagCompound();
                 var4.setByte("Slot", (byte)var3);
                 this.inventory[var3].save(var4);
                 var2.add(var4);
             }
         }
 
         var1.set("Items", var2);
     }
 
     /**
      * Returns the number of slots in the inventory.
      */
     public int getSize()
     {
         return this.inventory.length;
     }
 
     /**
      * Returns the stack in slot i
      */
     public ItemStack getItem(int var1)
     {
         return this.inventory[var1];
     }
 
     /**
      * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
      * stack.
      */
     public ItemStack splitStack(int var1, int var2)
     {
         if (this.inventory[var1] != null)
         {
             ItemStack var3;
 
             if (this.inventory[var1].count <= var2)
             {
                 var3 = this.inventory[var1];
                 this.inventory[var1] = null;
 
                 if (var1 == 0)
                 {
                     this.setCard((ItemStack)null);
                 }
 
                 return var3;
             }
             else
             {
                 var3 = this.inventory[var1].a(var2);
 
                 if (this.inventory[var1].count == 0)
                 {
                     this.inventory[var1] = null;
 
                     if (var1 == 0)
                     {
                         this.setCard((ItemStack)null);
                     }
                 }
 
                 return var3;
             }
         }
         else
         {
             return null;
         }
     }
 
     /**
      * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
      * like when you close a workbench GUI.
      */
     public ItemStack splitWithoutUpdate(int var1)
     {
         return null;
     }
 
     /**
      * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
      */
     public void setItem(int var1, ItemStack var2)
     {
         this.inventory[var1] = var2;
 
         if (var1 == 0)
         {
             this.setCard(var2);
         }
 
         if (var2 != null && var2.count > this.getMaxStackSize())
         {
             var2.count = this.getMaxStackSize();
         }
     }
 
     /**
      * Returns the name of the inventory.
      */
     public String getName()
     {
         return "block.StatusDisplay";
     }
 
     /**
      * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended. *Isn't
      * this more of a set than a get?*
      */
     public int getMaxStackSize()
     {
         return 64;
     }
 
     /**
      * Do not make give this method the name canInteractWith because it clashes with Container
      */
     public boolean a(EntityHuman var1)
     {
         return this.world.getTileEntity(this.x, this.y, this.z) == this && var1.e((double)this.x + 0.5D, (double)this.y + 0.5D, (double)this.z + 0.5D) <= 64.0D;
     }
 
     public void f() {}
 
     public void g() {}
 
     /**
      * Called when an the contents of an Inventory change, usually
      */
     public void update()
     {
         super.update();
 
         if (this.world != null && !this.world.isStatic)
         {
             int var1 = 0;
             ItemStack var2 = this.inventory[1];
 
             if (var2 != null && var2.getItem() instanceof ItemRangeUpgrade)
             {
                 var1 = var2.count;
             }
 
             if (this.inventory[0] != null)
             {
                 Item var3 = this.inventory[0].getItem();
 
                 if (var3 instanceof IPanelDataSource)
                 {
                     if (var1 > 7)
                     {
                         var1 = 7;
                     }
 
                     int var4 = 8 * (int)Math.pow(2.0D, (double)var1);
                     ((IPanelDataSource)var3).update(this, this.inventory[0], var4);
                 }
             }
         }
     }
 
     public boolean isItemValid(int var1, ItemStack var2)
     {
         switch (var1)
         {
             case 0:
                 return var2.getItem() instanceof IPanelDataSource;
 
             default:
                 return var2.getItem() instanceof ItemRangeUpgrade;
         }
     }
 
     public boolean wrenchCanSetFacing(EntityHuman var1, int var2)
     {
         return !var1.isSneaking() && this.getFacing() != var2;
     }
 
     public float getWrenchDropRate()
     {
         return 1.0F;
     }
 
     public boolean wrenchCanRemove(EntityHuman var1)
     {
         return !var1.isSneaking();
     }
 
     public int modifyTextureIndex(int var1, int var2, int var3, int var4)
     {
         if (var1 != 80)
         {
             return var1;
         }
         else
         {
             if (this.screen != null)
             {
                 boolean var5 = false;
                 boolean var6 = false;
                 boolean var7 = false;
                 boolean var8 = false;
                 boolean var9 = false;
 
                 switch (this.facing)
                 {
                     case 0:
                         if (var2 == this.screen.minX)
                         {
                             var5 = true;
                         }
 
                         if (var2 == this.screen.maxX)
                         {
                             var6 = true;
                         }
 
                         if (var4 == this.screen.minZ)
                         {
                             var7 = true;
                         }
 
                         if (var4 == this.screen.maxZ)
                         {
                             var8 = true;
                         }
 
                         break;
 
                     case 1:
                         if (var2 == this.screen.minX)
                         {
                             var5 = true;
                         }
 
                         if (var2 == this.screen.maxX)
                         {
                             var6 = true;
                         }
 
                         if (var4 == this.screen.minZ)
                         {
                             var7 = true;
                         }
 
                         if (var4 == this.screen.maxZ)
                         {
                             var8 = true;
                         }
 
                         break;
 
                     case 2:
                         if (var2 == this.screen.minX)
                         {
                             var5 = true;
                         }
 
                         if (var2 == this.screen.maxX)
                         {
                             var6 = true;
                         }
 
                         if (var3 == this.screen.maxY)
                         {
                             var7 = true;
                         }
 
                         if (var3 == this.screen.minY)
                         {
                             var8 = true;
                         }
 
                         break;
 
                     case 3:
                         if (var2 == this.screen.minX)
                         {
                             var6 = true;
                         }
 
                         if (var2 == this.screen.maxX)
                         {
                             var5 = true;
                         }
 
                         if (var3 == this.screen.maxY)
                         {
                             var7 = true;
                         }
 
                         if (var3 == this.screen.minY)
                         {
                             var8 = true;
                         }
 
                         var9 = true;
                         break;
 
                     case 4:
                         if (var4 == this.screen.minZ)
                         {
                             var6 = true;
                         }
 
                         if (var4 == this.screen.maxZ)
                         {
                             var5 = true;
                         }
 
                         if (var3 == this.screen.maxY)
                         {
                             var7 = true;
                         }
 
                         if (var3 == this.screen.minY)
                         {
                             var8 = true;
                         }
 
                         var9 = true;
                         break;
 
                     case 5:
                         if (var4 == this.screen.minZ)
                         {
                             var5 = true;
                         }
 
                         if (var4 == this.screen.maxZ)
                         {
                             var6 = true;
                         }
 
                         if (var3 == this.screen.maxY)
                         {
                             var7 = true;
                         }
 
                         if (var3 == this.screen.minY)
                         {
                             var8 = true;
                         }
                 }
 
                 if (this.rotation == 0)
                 {
                     if (var5)
                     {
                         ++var1;
                     }
 
                     if (var6)
                     {
                         var1 += 2;
                     }
 
                     if (var7)
                     {
                         var1 += 4;
                     }
 
                     if (var8)
                     {
                         var1 += 8;
                     }
                 }
                 else if (!var9 && this.rotation == 1)
                 {
                     if (this.facing == 1)
                     {
                         if (var5)
                         {
                             var1 += 4;
                         }
 
                         if (var6)
                         {
                             var1 += 8;
                         }
 
                         if (var7)
                         {
                             var1 += 2;
                         }
 
                         if (var8)
                         {
                             ++var1;
                         }
                     }
                     else
                     {
                         if (var5)
                         {
                             var1 += 8;
                         }
 
                         if (var6)
                         {
                             var1 += 4;
                         }
 
                         if (var7)
                         {
                             ++var1;
                         }
 
                         if (var8)
                         {
                             var1 += 2;
                         }
                     }
                 }
                 else if (var9 && this.rotation == 1)
                 {
                     if (var5)
                     {
                         var1 += 8;
                     }
 
                     if (var6)
                     {
                         var1 += 4;
                     }
 
                     if (var7)
                     {
                         var1 += 2;
                     }
 
                     if (var8)
                     {
                         ++var1;
                     }
                 }
                 else if (this.rotation == 3)
                 {
                     if (var5)
                     {
                         var1 += 2;
                     }
 
                     if (var6)
                     {
                         ++var1;
                     }
 
                     if (var7)
                     {
                         var1 += 8;
                     }
 
                     if (var8)
                     {
                         var1 += 4;
                     }
                 }
                 else if (!var9 && this.rotation == 2)
                 {
                     if (this.facing == 1)
                     {
                         if (var5)
                         {
                             var1 += 8;
                         }
 
                         if (var6)
                         {
                             var1 += 4;
                         }
 
                         if (var7)
                         {
                             ++var1;
                         }
 
                         if (var8)
                         {
                             var1 += 2;
                         }
                     }
                     else
                     {
                         if (var5)
                         {
                             var1 += 4;
                         }
 
                         if (var6)
                         {
                             var1 += 8;
                         }
 
                         if (var7)
                         {
                             var1 += 2;
                         }
 
                         if (var8)
                         {
                             ++var1;
                         }
                     }
                 }
                 else if (var9 && this.rotation == 2)
                 {
                     if (var5)
                     {
                         var1 += 4;
                     }
 
                     if (var6)
                     {
                         var1 += 8;
                     }
 
                     if (var7)
                     {
                         ++var1;
                     }
 
                     if (var8)
                     {
                         var1 += 2;
                     }
                 }
             }
             else
             {
                 var1 += 15;
             }
 
             if (this.powered)
             {
                 var1 += 16;
             }
 
             return var1;
         }
     }
 
     public int modifyTextureIndex(int var1)
     {
         return this.modifyTextureIndex(var1, this.x, this.y, this.z);
     }
 
     public void setScreen(Screen var1)
     {
         this.screen = var1;
     }
 
     public Screen getScreen()
     {
         return this.screen;
     }
 
     public int hashCode()
     {
         boolean var1 = true;
         byte var2 = 1;
         int var3 = 31 * var2 + this.x;
         var3 = 31 * var3 + this.y;
         var3 = 31 * var3 + this.z;
         return var3;
     }
 
     public boolean equals(Object var1)
     {
         if (this == var1)
         {
             return true;
         }
         else if (var1 == null)
         {
             return false;
         }
         else if (this.getClass() != var1.getClass())
         {
             return false;
         }
         else
         {
             TileEntityInfoPanel var2 = (TileEntityInfoPanel)var1;
             return this.x != var2.x ? false : (this.y != var2.y ? false : (this.z != var2.z ? false : this.world == var2.world));
         }
     }
 
     public int getStartInventorySide(int var1)
     {
         return var1 == 0 ? 1 : 0;
     }
 
     public int getSizeInventorySide(int var1)
     {
         return var1 != 0 && var1 != 1 ? this.inventory.length : 1;
     }
 
     public void rotate()
     {
         byte var1;
 
         switch (this.rotation)
         {
             case 0:
                 var1 = 1;
                 break;
 
             case 1:
                 var1 = 3;
                 break;
 
             case 2:
                 var1 = 0;
                 break;
 
             case 3:
                 var1 = 2;
                 break;
 
             default:
                 var1 = 0;
         }
 
         this.setRotation(var1);
     }
 
     public int getRotation()
     {
         return this.rotation;
     }
 
     public void setRotation(int var1)
     {
         this.rotation = var1;
 
         if (this.rotation != this.prevRotation)
         {
             NetworkHelper.updateTileEntityField(this, "rotation");
         }
 
         this.prevRotation = this.rotation;
     }
 
     public int getDisplaySettings()
     {
         if (this.inventory[0] == null)
         {
             return 0;
         }
         else
         {
             int var1 = 0;
 
             if (this.inventory[0] != null && this.inventory[0].getItem() instanceof IPanelDataSource)
             {
                 var1 = ((IPanelDataSource)this.inventory[0].getItem()).getCardType();
             }
 
             if (var1 > 3)
             {
                 var1 = 0;
             }
 
             return this.displaySettings[var1];
         }
     }
 }

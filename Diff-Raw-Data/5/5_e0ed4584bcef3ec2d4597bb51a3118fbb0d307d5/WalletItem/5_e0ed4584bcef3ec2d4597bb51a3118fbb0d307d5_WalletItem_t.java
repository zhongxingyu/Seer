 /**
  * CCM Modding, ModJam
  */
 package ccm.trade_stuffs.items;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import ccm.trade_stuffs.TradeStuffs;
 import ccm.trade_stuffs.api.CoinTypes;
 import ccm.trade_stuffs.inventory.WalletInventory;
 import ccm.trade_stuffs.utils.helper.InventoryHelper;
 import ccm.trade_stuffs.utils.helper.NBTHelper;
 import ccm.trade_stuffs.utils.lib.Archive;
 import ccm.trade_stuffs.utils.lib.Guis;
 
 /**
  * WalletItem
  * <p>
  * 
  * @author Captain_Shadows
  */
 public class WalletItem extends BaseItem
 {
     public static final String openedWallet = "CCM.WALLET.OPEN";
     public static final String fullWallet   = "CCM.WALLET.FULL";
     String[]                   types        = new String[] { "wallet", "wallet_empty", "wallet_full" };
     Icon[]                     icons        = new Icon[types.length];
 
     /**
      * @param id
      */
     public WalletItem(final int id)
     {
         super(id);
         setMaxDamage(0);
         setMaxStackSize(1);
         setNoRepair();
     }
 
     @Override
     public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player)
     {
         if (!world.isRemote)
         {
             NBTHelper.setBoolean(stack, openedWallet, true);
             player.openGui(TradeStuffs.instance,
                            Guis.GUI_WALLET,
                            player.worldObj,
                            (int) player.posX,
                            (int) player.posY,
                            (int) player.posZ);
         }
 
         return stack;
     }
 
     @Override
     public Icon getIcon(final ItemStack stack, final int pass)
     {
         if (NBTHelper.getBoolean(stack, openedWallet))
         {
             if (NBTHelper.getBoolean(stack, fullWallet))
             {
                 setDamage(stack, 2);
                 return icons[2];
             }
             else
             {
                 setDamage(stack, 1);
                 return icons[1];
             }
         }
         else
         {
             setDamage(stack, 0);
             return icons[0];
         }
     }
 
     @Override
     public boolean getShareTag()
     {
         return true;
     }
 
     @Override
     public String getUnlocalizedName(final ItemStack item)
     {
         return Archive.MOD_ID_ITEM + types[item.getItemDamage()];
     }
 
     @Override
     public void registerIcons(final IconRegister register)
     {
         for (int i = 0; i < types.length; i++)
         {
             icons[i] = register.registerIcon(Archive.MOD_ID + ":" + types[i]);
         }
     }
 
     /**
      * Gets an icon index based on an item's damage value
      */
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIconFromDamage(final int meta)
     {
         return icons[meta];
     }
 
     @Override
     public void addInformation(final ItemStack item,
                                final EntityPlayer palyer,
                                final List list,
                                final boolean color)
     {
        // TODO Fix this
         final WalletInventory wallet = new WalletInventory(item);
         final ItemStack[] inv = InventoryHelper.readInventoryFromNBT(item.getTagCompound()
                                                                          .getTagList(WalletInventory.INVENTORY_WALLET),
                                                                      wallet.getSizeInventory());
         final StringBuilder sb = new StringBuilder();
         sb.append("You have a total of ");
         int value = 0;
         for (final ItemStack stack : inv)
         {
             if (stack != null)
             {
                 value += CoinTypes.getTypes().get(item.getItemDamage()).getValueStack(item);
             }
         }
         sb.append(value + " coin");
         if (value != 1)
         {
             sb.append("s");
         }
         sb.append(" in this ");
        sb.append(list.get(0));
        list.add(sb.toString());
     }
 
 }

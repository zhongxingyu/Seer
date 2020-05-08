 package blfngl.fallout.item;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import blfngl.fallout.FalloutMain;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockJukeBox;
import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumRarity;
 import net.minecraft.item.ItemRecord;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemFalloutRecord extends ItemRecord
 {
 	private static final Map records = new HashMap();
 	public final String recordName;
 
 	public ItemFalloutRecord(int var1, String var2)
 	{
 		super(var1, var2);
 		this.recordName = var2;
 		this.maxStackSize = 1;
 		this.setCreativeTab(FalloutMain.TabFalloutMusic);
 		records.put(var2, this);
 	}
 
 	/**
 	 * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
 	 * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
 	 */
 	public boolean onItemUse(ItemStack var1, EntityPlayer var2, World var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10)
 	{
 		if (var3.getBlockId(var4, var5, var6) == Block.jukebox.blockID && var3.getBlockMetadata(var4, var5, var6) == 0)
 		{
 			if (var3.isRemote)
 			{
 				return true;
 			}
 			else
 			{
 				((BlockJukeBox)Block.jukebox).insertRecord(var3, var4, var5, var6, var1);
 				var3.playAuxSFXAtEntity((EntityPlayer)null, 1005, var4, var5, var6, this.itemID);
 				--var1.stackSize;
 				return true;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	@SideOnly(Side.CLIENT)
 
 	/**
 	 * allows items to add custom lines of information to the mouseover description
 	 */
 	public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4)
 	{
 		if (this.recordName == "Blue Moon")
 		{
 			var3.add("Frank Sinatra - " + this.recordName);
 		}
 	}

 	@SideOnly(Side.CLIENT)
 
 	/**
 	 * Return the title for this record.
 	 */
 	public String getRecordTitle()
 	{
		return this.recordName;
 	}
 
 	@SideOnly(Side.CLIENT)
 
 	/**
 	 * Return an item rarity from EnumRarity
 	 */
 	public EnumRarity getRarity(ItemStack var1)
 	{
 		return EnumRarity.rare;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public static ItemFalloutRecord getRecord(String var0)
 	{
 		return (ItemFalloutRecord)records.get(var0);
 	}

	public void func_94581_a(IconRegister iconRegister)
	{
		itemIcon = iconRegister.registerIcon("blfngl" + ":" + this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1));
	}
 }

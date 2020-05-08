 package blfngl.fallout.item;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import blfngl.fallout.FalloutMain;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockJukeBox;
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
		return this.recordName; // == "Dont Fear The Reaper" ? "Blue Oyster Cult - " + this.recordName : (this.recordName == "Always" ? "Erasure - " + this.recordName : (this.recordName == "Blue" ? "Eiffel65 - " + this.recordName : (this.recordName == "Ghosts N Stuff" ? "Deadmau5 - " + this.recordName : (this.recordName == "Somebody That I Use To Know" ? "Gotye - " + this.recordName : (this.recordName == "Whistle" ? "Flo Rida - " + this.recordName : (this.recordName == "Take On Me" ? "Ah-Ha - " + this.recordName : (this.recordName == "Sail" ? "Awolnation - " + this.recordName : (this.recordName == "Killer Queen" ? "Queen - " + this.recordName : (this.recordName == "Call Me Maybe" ? "Carly Rae Jepsen - " + this.recordName : (this.recordName == "Viva La Vida" ? "Coldplay - " + this.recordName : (this.recordName == "Lights" ? "Ellie Goulding - " + this.recordName : (this.recordName == "Barbra Striesand" ? "Duck Sauce - " + this.recordName : (this.recordName == "Enter Sandman" ? "Metallica - " + this.recordName : (this.recordName == "Moves Like Jagger" ? "Maroon5 - " + this.recordName : (this.recordName == "Say It Aint So" ? "Weezer - " + this.recordName : (this.recordName == "Payphone" ? "Maroon5 - " + this.recordName : (this.recordName == "Carry On My Wayward Son" ? "Kansas - " + this.recordName : (this.recordName == "Dragostea Din Tei" ? "Ozone - " + this.recordName : (this.recordName == "Now You\'re Gone" ? "Basshunter - " + this.recordName : (this.recordName == "All The Small Things" ? "Blink182 - " + this.recordName : (this.recordName == "Twenty One Guns" ? "Green Day - " + this.recordName : (this.recordName == "Beat It" ? "Michael Jackson - " + this.recordName : (this.recordName == "Don\'t You Worry Bout A Thing Child" ? "Sweedish House Mafia - " + this.recordName : (this.recordName == "Land Of Confusion" ? "Genesis - " + this.recordName : (this.recordName == "Little Talks" ? "Of Monsters And Men - " + this.recordName : (this.recordName == "Sweet Dreams" ? "Eurythmics - " + this.recordName : this.recordName))))))))))))))))))))))))));
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
 }

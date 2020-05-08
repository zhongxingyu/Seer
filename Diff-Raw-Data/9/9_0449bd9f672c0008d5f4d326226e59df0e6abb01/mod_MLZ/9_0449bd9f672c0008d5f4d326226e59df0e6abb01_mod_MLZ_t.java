 package net.minecraft.src;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import net.minecraft.client.Minecraft;
 
 public class mod_MLZ extends BaseMod
 {
 	public static ItemMCH mch = (ItemMCH)(new ItemMCH(128).setItemName("mch"));
 	public static Item defenseBeacon = mch; /* Testing */
 	public static Item miningBeacon = mch; /* Testing */
 	
 	public void load()
 	{
 		this.addRecipes();
 		ModLoader.addName(mch, "Mind-Control Helmet");
		mch.iconIndex = ModLoader.addOverride("/gui/items.png", "mchelmet.png");
 		
 		ModLoader.registerEntityID(EntityZombieMC.class, "EntityZombieMC", 123);
 	}
 	
 	public void addRecipes()
 	{
 		ModLoader.addRecipe(new ItemStack(mch, 1), new Object[] { "R", "H", 'R', Block.torchRedstoneActive, 'H', Item.helmetSteel});
 		ModLoader.addRecipe(new ItemStack(mch, 64), new Object[] { "D", 'D', Block.dirt } ); /* For testing */
 	}
 	
 	public String getVersion()
 	{
 		return "1.0";
 	}
 }

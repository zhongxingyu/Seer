 package dev.mCraft.Coinz.Blocks;
 
 import java.util.HashMap;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.inventory.InventoryBuilder;
 import org.getspout.spoutapi.material.MaterialData;
 import org.getspout.spoutapi.material.block.GenericCuboidCustomBlock;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import dev.mCraft.Coinz.Coinz;
 import dev.mCraft.Coinz.GUI.VaultInv.KeypadPopup;
 import dev.mCraft.Coinz.Serializer.PersistVault;
 
 public class Vault extends GenericCuboidCustomBlock {
 	
 	private static Coinz plugin = Coinz.instance;
 	public static Vault hook = null;
 	private PersistVault persist = null;
 	
 	public static int[] textID = {16, 17, 16, 16, 16, 16};
 	
 	private InventoryBuilder builder = null;
 	public HashMap<Location, ItemStack[]> vaultInv = new HashMap<Location, ItemStack[]>();
 	private SpoutPlayer player;
 	
 	public Vault() {
 		super(plugin, "Vault", true, new Design(textID));
		setHardness(MaterialData.furnace.getHardness());
 		hook = this;
 	}
 	
 	@Override
 	public boolean onBlockInteract(World world, int x, int y, int z, SpoutPlayer player) {
 		return player.getMainScreen().attachPopupScreen(new KeypadPopup());
 	}
 	
 	@Override
 	public void onBlockPlace(World world, int x, int y, int z, LivingEntity entity) {
 		persist = PersistVault.hook;
 		Location loc = new Location(world, x, y, z);
 		builder = SpoutManager.getInventoryBuilder();
 		player = (SpoutPlayer)entity;
 		
 		ItemStack[] stack = new ItemStack[9];
 		stack[0] = null;
 		stack[1] = null;
 		stack[2] = null;
 		stack[3] = null;
 		stack[4] = null;
 		stack[5] = null;
 		stack[6] = null;
 		stack[7] = null;
 		stack[8] = null;
 		
 		Inventory tempInv = builder.construct(stack, "Vault");
 		
 		try {
 			persist.save(loc, tempInv);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		player.getMainScreen().attachPopupScreen(new KeypadPopup());
 	}
 }

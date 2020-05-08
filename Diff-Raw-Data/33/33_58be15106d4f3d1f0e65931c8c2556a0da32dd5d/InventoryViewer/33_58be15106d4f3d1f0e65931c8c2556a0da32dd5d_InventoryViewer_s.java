 package no.runsafe.runsafeinventories;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.minecraft.RunsafeServer;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.runsafeinventories.repositories.InventoryRepository;
 
 public class InventoryViewer implements IConfigurationChanged
 {
 	public InventoryViewer(InventoryRepository repository, UniverseHandler universeHandler)
 	{
 		this.repository = repository;
 		this.universeHandler = universeHandler;
 	}
 
 	public boolean viewUniverseInventory(RunsafePlayer viewer, RunsafePlayer owner)
 	{
 		return this.viewUniverseInventory(viewer, owner, this.defaultUniverse);
 	}
 
 	public boolean viewUniverseInventory(RunsafePlayer viewer, RunsafePlayer owner, String universeName)
 	{
 		// Is the player online and in the same world? If so, get their current inventory from memory.
 		if (owner.isOnline())
 		{
			if (owner.getWorld().IsUniverse(universeName))
 			{
 				viewer.openInventory(owner.getInventory());
 				return true;
 			}
 		}
 
 		// Player is not online, pull inventory data from database.
 		PlayerInventory inventoryData = this.repository.getInventory(owner, universeName);
 		if (inventoryData == null)
 			return false;
 
 		RunsafeInventory inventory = RunsafeServer.Instance.createInventory(null, RunsafeInventoryType.PLAYER.getDefaultSize(), String.format("%s's Inventory", owner.getName()));
 		inventory.unserialize(inventoryData.getInventoryString());
 		viewer.openInventory(inventory);
 
 		return true;
 	}
 
 	public boolean hasDefaultUniverse()
 	{
 		return (this.defaultUniverse != null);
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.defaultUniverse = configuration.getConfigValueAsString("defaultOpenInventoryUniverse");
 	}
 
 	private InventoryRepository repository;
 	private String defaultUniverse;
 	private UniverseHandler universeHandler;
 }

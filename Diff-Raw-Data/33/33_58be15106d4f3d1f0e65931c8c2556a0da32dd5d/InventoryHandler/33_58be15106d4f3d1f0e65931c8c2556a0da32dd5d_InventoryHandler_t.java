 package no.runsafe.runsafeinventories;
 
 import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.runsafeinventories.repositories.InventoryRepository;
 import no.runsafe.runsafeinventories.repositories.TemplateRepository;
 
 public class InventoryHandler
 {
	public InventoryHandler(InventoryRepository inventoryRepository, TemplateRepository templateRepository, IOutput output)
 	{
 		this.inventoryRepository = inventoryRepository;
 		this.templateRepository = templateRepository;
 		this.output = output;
 	}
 
 	public void saveInventory(RunsafePlayer player)
 	{
		String universe = player.getWorld().getUniverse().getName();
 		this.output.fine("Running force save for %s in %s", player.getName(), universe);
 		this.inventoryRepository.saveInventory(new PlayerInventory(player, universe));
 	}
 
 	public void handlePreWorldChange(RunsafePlayer player)
 	{
 		this.saveInventory(player); // Save inventory
 		this.wipeInventory(player);
 	}
 
 	public void wipeInventory(RunsafePlayer player)
 	{
 		this.output.fine("Wiping inventory for " + player.getName());
 		player.getInventory().clear(); // Clear inventory
 		player.setXP(0); // Remove all XP
 		player.setLevel(0); // Remove all levels
 	}
 
 	public void handlePostWorldChange(RunsafePlayer player)
 	{
		String universeName = player.getWorld().getUniverse().getName();
 		PlayerInventory inventory = this.inventoryRepository.getInventory(player, universeName); // Get inventory
 
 		// If we are null, the player had no stored inventory.
 		if (inventory != null)
 		{
 			this.output.fine(String.format("Settings inventory for %s to %s", player.getName(), inventory.getInventoryName()));
 			player.getInventory().unserialize(inventory.getInventoryString()); // Restore inventory
 			player.setLevel(inventory.getLevel()); // Restore level
 			player.setXP(inventory.getExperience()); // Restore experience
 			player.updateInventory();
 		}
 		else
 		{
 			// Lets check if we can give them a template.
 			this.templateRepository.setToTemplate(universeName, player.getInventory());
 		}
 	}
 
 	private InventoryRepository inventoryRepository;
 	private TemplateRepository templateRepository;
 
 	private IOutput output;
 }

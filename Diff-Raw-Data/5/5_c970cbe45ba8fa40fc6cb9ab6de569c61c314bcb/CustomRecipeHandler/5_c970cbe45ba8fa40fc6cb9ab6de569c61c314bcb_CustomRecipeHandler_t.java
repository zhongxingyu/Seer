 package no.runsafe.ItemControl;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.api.IServer;
 import no.runsafe.framework.api.event.IServerReady;
 import no.runsafe.framework.api.event.inventory.IPrepareCraftItem;
 import no.runsafe.framework.api.item.ICustomRecipe;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.event.inventory.RunsafePrepareItemCraftEvent;
 import no.runsafe.framework.minecraft.inventory.RunsafeCraftingInventory;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import org.bukkit.inventory.ShapedRecipe;
 
 import java.util.*;
 
 public class CustomRecipeHandler implements IServerReady, IPrepareCraftItem
 {
 	public CustomRecipeHandler(IConsole console, IServer server)
 	{
 		this.console = console;
 		this.server = server;
 
 		Character[] chars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
 		keys = Arrays.asList(chars);
 	}
 
 	@Override
 	public void OnServerReady()
 	{
 		recipes.addAll(RunsafePlugin.getPluginAPI(ICustomRecipe.class));
 
 		for (ICustomRecipe recipe : recipes)
 		{
 			ShapedRecipe bukkitRecipe = new ShapedRecipe(Item.Unavailable.Cauldron.getItem().getRaw());
 			HashMap<Item, Character> itemKey = new HashMap<Item, Character>(0);
 			Iterator<Character> keyList = keys.iterator();
 			Map<Integer, RunsafeMeta> recipeMap = recipe.getRecipe();
 			StringBuilder recipeString = new StringBuilder();
 
 			for (int slotID = 1; slotID < 10; slotID++)
 			{
 				Item type = Item.Unavailable.Air;
 				if (recipeMap.containsKey(slotID))
 					type = recipeMap.get(slotID).getItemType();
 
 				if (!itemKey.containsKey(type))
 					itemKey.put(type, keyList.next());
 
 				recipeString.append(itemKey.get(type));
 			}
 
 			console.logInformation("Shape: " + recipeString.toString());
			bukkitRecipe = bukkitRecipe.shape(recipeString.toString().split("(?<=\\G...)"));
 
 			for (String test : recipeString.toString().split("(?<=\\G...)"))
 				console.logInformation(test);
 
 			for (Map.Entry<Item, Character> node : itemKey.entrySet())
 			{
				bukkitRecipe = bukkitRecipe.setIngredient(node.getValue(), node.getKey().getType());
 				console.logInformation("Setting ing: %s -> %s", node.getValue(), node.getKey().getName());
 			}
 
 			server.addRecipe(bukkitRecipe);
 		}
 
 		console.logInformation("Loaded " + recipes.size() + " custom recipes");
 	}
 
 	@Override
 	public void OnPrepareCraftItem(RunsafePrepareItemCraftEvent event)
 	{
 		RunsafeCraftingInventory inventory = event.getInventory();
 		if (inventory.getType() == RunsafeInventoryType.WORKBENCH)
 		{
 			List<RunsafeMeta> items = inventory.getMatrix();
 			for (ICustomRecipe recipe : recipes)
 			{
 				boolean failed = false;
 				Map<Integer, RunsafeMeta> recipeDesign = recipe.getRecipe();
 				int slot = 1;
 				for (RunsafeMeta item : items)
 				{
 					if ((item == null || item.is(Item.Unavailable.Air)) && !recipeDesign.containsKey(slot))
 						continue;
 
 					if (!recipeDesign.containsKey(slot) || !matches(recipeDesign.get(slot), item))
 					{
 						failed = true;
 						break;
 					}
 					slot++;
 				}
 
 				if (!failed)
 					inventory.setResult(recipe.getResult());
 			}
 		}
 	}
 
 	private boolean matches(RunsafeMeta item, RunsafeMeta check)
 	{
 		if (item.getItemType().equals(check.getItemType()))
 		{
 			console.logInformation("Mis-matched item?");
 			return false;
 		}
 
 		String displayName = item.getDisplayName();
 		String checkName = check.getDisplayName();
 
 		if (displayName == null)
 		{
 			if (checkName != null)
 			{
 				console.logInformation("Name mis-match");
 				return false;
 			}
 		}
 		else
 		{
 			if (checkName == null || !displayName.equals(checkName))
 			{
 				console.logInformation("Name mis-match 2");
 				return false;
 			}
 		}
 
 		List<String> lore = item.getLore();
 		List<String> checkLore = check.getLore();
 
 		if (lore == null || lore.isEmpty())
 		{
 			if (checkLore != null && !checkLore.isEmpty())
 			{
 				console.logInformation("Lore mis-match.");
 				return false;
 			}
 		}
 		else
 		{
 			if (checkLore == null || checkLore.isEmpty())
 			{
 				console.logInformation("Lore mis-match 2");
 				return false;
 			}
 
 			if (checkLore.size() != lore.size())
 			{
 				console.logInformation("Lore size mis-match");
 				return false;
 			}
 
 			int index = 0;
 			for (String loreString : lore)
 			{
 				if (!checkLore.get(index).equals(loreString))
 				{
 					console.logInformation("Lore mis-match: %s / %s", loreString, checkLore.get(index));
 					return false;
 				}
 
 				index++;
 			}
 		}
 		return true;
 	}
 
 	private final IConsole console;
 	private final IServer server;
 	private final List<Character> keys;
 	private final List<ICustomRecipe> recipes = new ArrayList<ICustomRecipe>(0);
 }

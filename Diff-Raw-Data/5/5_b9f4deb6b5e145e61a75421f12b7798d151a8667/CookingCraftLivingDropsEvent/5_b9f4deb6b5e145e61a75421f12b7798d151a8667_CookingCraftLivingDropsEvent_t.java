 package mods.cc.rock.event;
 
 import mods.cc.rock.core.helpers.ItemHelper;
 import mods.cc.rock.lib.ItemIDs;
 
 import net.minecraft.entity.monster.EntityZombie;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingDropsEvent;
 
 public class CookingCraftLivingDropsEvent
 {
 
 	@ForgeSubscribe
 	public void onEntityDrop(LivingDropsEvent event)
 	{
		if(event.entityLiving instanceof EntityZombie && event.source.getDamageType().equals("player"))
 		{
			ItemHelper.dropItem(event.entityLiving, ItemIDs.ID_HAMMER, 50);
 		}
 	}
 
 }

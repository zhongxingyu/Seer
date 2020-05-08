 package hunternif.mc.dota2items.item;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.Sound;
 import hunternif.mc.dota2items.core.EntityStats;
 
 import java.util.List;
 
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 
 public class ArcaneBoots extends CooldownItem {
 	public static final double radius = 6;
 	public static final float manaRestored = 135;
 	
 	public ArcaneBoots(int id) {
 		super(id);
 		setCooldown(55);
 		setManaCost(25);
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
 		if (!tryUse(stack, player)) {
 			return stack;
 		}
 		startCooldown(stack, player);
 		Dota2Items.mechanics.getOrCreateEntityStats(player).removeMana(getManaCost());
 		world.playSoundAtEntity(player, Sound.ARCANE_BOOTS.getName(), 0.7f, 1);
 		// Restore mana in the area:
 		double x = Math.floor(player.posX);
 		double y = Math.floor(player.posY);
 		double z = Math.floor(player.posZ);
 		AxisAlignedBB area = AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1);
		area = area.expand(radius, radius, radius);
 		List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, area);
 		if (list != null && !list.isEmpty()) {
 			for (EntityLivingBase entity : list) {
 				EntityStats stats = Dota2Items.mechanics.getEntityStats(entity);
 				if (stats != null) {
 					stats.addMana(manaRestored);
					if (stats.entity instanceof EntityPlayer) {
						stats.sendSyncPacketToClient((EntityPlayer)stats.entity);
					}
 				}
 			}
 		}
 		return stack;
 	}
 }

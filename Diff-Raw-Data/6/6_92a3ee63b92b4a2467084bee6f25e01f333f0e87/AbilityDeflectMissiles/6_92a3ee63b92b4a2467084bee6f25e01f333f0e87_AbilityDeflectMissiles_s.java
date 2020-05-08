 package org.monk.MineQuest.Ability;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 import org.monk.MineQuest.Quester.Quester;
 import org.monk.MineQuest.Quester.SkillClass.SkillClass;
 import org.monk.MineQuest.Quester.SkillClass.Combat.Archer;
 
 public class AbilityDeflectMissiles extends Ability implements DefendingAbility, PassiveAbility {
 
 	@Override
 	public void castAbility(Quester quester, Location location,
 			LivingEntity entity) {
 		// Passive
 	}
 
 	@Override
 	public SkillClass getClassType() {
 		return new Archer();
 	}
 
 	@Override
 	public List<ItemStack> getManaCost() {
 		List<ItemStack> cost = new ArrayList<ItemStack>();
 		int i;
 		
 		for (i = 0; i < 10; i++) {
 			cost.add(new ItemStack(Material.LEATHER, 1));
 		}
 		
 		return cost;
 	}
 
 	@Override
 	public String getName() {
 		return "Deflect Missiles";
 	}
 
 	@Override
 	public int getReqLevel() {
 		return 15;
 	}
 
 	@Override
 	public int parseDefend(Quester quester, LivingEntity mob, int amount) {
		if (myclass.getGenerator().nextDouble() < .6) {
 			Location location = quester.getPlayer().getLocation();
 			World world = location.getWorld();
 			Random generator = myclass.getGenerator();
 			location.setY(location.getY() + 1.5);
 			Vector vector = new Vector(generator.nextDouble(), .2, generator.nextDouble());
 			
 			world.spawnArrow(location, vector, .6f, 12);
 			
 			return amount;
 		}
 		
 		return 0;
 	}
 
 }

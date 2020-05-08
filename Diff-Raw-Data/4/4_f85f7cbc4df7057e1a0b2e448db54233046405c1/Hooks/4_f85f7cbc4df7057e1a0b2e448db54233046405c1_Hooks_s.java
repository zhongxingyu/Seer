 package gmod.testing;
 
 import gmod.Entities;
 import gmod.events.NPCKilledEvent;
 import gmod.objects.Entity;
 import gmod.objects.Vector;
 
 import com.google.common.eventbus.Subscribe;
 
 public class Hooks {
 
 	@Subscribe
	public void onInitialize() {
 		System.out.println("Gamemode has initialized!");
 	}
 
 	@Subscribe
 	public void onNPCKilled(NPCKilledEvent e) {
 		System.out.println("NPCKilled!");
 		System.out.println(e.getKiller().getName() + " killed " + e.getVictim().getClassName() + " with a " + e.getWeapon().getClassName());
 		
 		Entity ent = e.getVictim();
 		Entity killer = e.getKiller();
 		
 		Entity newEnt = Entities.create(ent.getClassName());
 		Vector newPos = new Vector(killer.getPos());
 		newPos.add(new Vector(0, 0, 100));
 		newEnt.setPos(newPos);
 	}
 
 }

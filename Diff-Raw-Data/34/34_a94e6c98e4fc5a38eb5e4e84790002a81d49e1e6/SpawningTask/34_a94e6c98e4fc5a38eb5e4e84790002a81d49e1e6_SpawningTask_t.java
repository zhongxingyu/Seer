 package com.archmageinc.RandomEncounters.Tasks;
 
 import com.archmageinc.RandomEncounters.Mobs.Mob;
 import com.archmageinc.RandomEncounters.Encounters.PlacedEncounter;
 import com.archmageinc.RandomEncounters.Mobs.PlacedMob;
 import com.archmageinc.RandomEncounters.RandomEncounters;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
import org.bukkit.Location;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  *
  * @author ArchmageInc
  */
 public class SpawningTask extends BukkitRunnable   {
     private final PlacedEncounter placedEncounter;
     private final List<Mob> placements;
     private final Iterator<Mob> itr;
    private Location location;
     
     public SpawningTask(PlacedEncounter placedEncounter){
         this.placedEncounter    =   placedEncounter;
         this.placements         =   placedEncounter.getEncounter().getMobPlacements();
         itr                     =   placements.iterator();
     }
     
     public SpawningTask(Mob mob,PlacedEncounter placedEncounter){
         this.placedEncounter    =   placedEncounter;
         this.placements         =   mob.getPlacements();
         itr                     =   placements.iterator();
     }
     
    public SpawningTask(Mob mob,PlacedEncounter placedEncounter,Location location){
        this.placedEncounter    =   placedEncounter;
        this.placements         =   mob.getPlacements();
        this.location           =   location;
        itr                     =   placements.iterator();
    }
    
     @Override
     public void run() {
         Calendar timeLimit  =   (Calendar) Calendar.getInstance().clone();
         timeLimit.add(Calendar.MILLISECOND, RandomEncounters.getInstance().lockTime());
         while(itr.hasNext()){
             Mob mob =   itr.next();
            placedEncounter.addMob(PlacedMob.create(mob, placedEncounter,location));
             if(Calendar.getInstance().after(timeLimit))
                 break;
         }
         if(itr.hasNext()){
             if(RandomEncounters.getInstance().getLogLevel()>8){
                 RandomEncounters.getInstance().logMessage("More time needed for "+placedEncounter.getName()+" spawning.");
             }
         }else{
             if(RandomEncounters.getInstance().getLogLevel()>8){
                 RandomEncounters.getInstance().logMessage("Spawning Completed for "+placedEncounter.getName()+".");
             }
             cancel();
         }
     }
     
 }

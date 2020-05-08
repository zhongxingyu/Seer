 package to.joe.j2mc.survival.runnable;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.survival.J2MC_Survival;
 
 public class CompassUpdater implements Runnable {
 
     J2MC_Survival plugin;
 
     public CompassUpdater(J2MC_Survival survival) {
         plugin = survival;
     }
 
     @Override
     public void run() {
         ArrayList<String> participants = plugin.getGame().getParticipants();
        if (participants.size() < 2)
            return;
         for (String s1 : participants) {
             Player p1 = plugin.getServer().getPlayer(s1);
             ArrayList<String> participantsMinusPlayer = new ArrayList<String>(participants);
             participantsMinusPlayer.remove(s1);
             Player closestPlayer = plugin.getServer().getPlayer(participantsMinusPlayer.get(0));
             for (String s2 : participants) {
                 Player p2 = plugin.getServer().getPlayer(s2);
                 if (p1.equals(p2))
                     continue;
                 if (p1.getLocation().distanceSquared(p2.getLocation()) < p1.getLocation().distanceSquared(closestPlayer.getLocation())) {
                     closestPlayer = p2;
                 }
             }
             p1.setCompassTarget(closestPlayer.getLocation());
         }
     }
 
 }

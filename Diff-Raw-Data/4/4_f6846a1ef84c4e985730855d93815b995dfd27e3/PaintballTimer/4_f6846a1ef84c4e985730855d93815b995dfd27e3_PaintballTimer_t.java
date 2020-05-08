 package me.naithantu.ArenaPVP.Gamemodes.Gamemodes.Paintball;
 
 import me.naithantu.ArenaPVP.Arena.Arena;
 import me.naithantu.ArenaPVP.Arena.ArenaExtras.ArenaPlayerState;
 import me.naithantu.ArenaPVP.Arena.ArenaPlayer;
 import me.naithantu.ArenaPVP.Arena.ArenaTeam;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.util.List;
 
 public class PaintballTimer extends BukkitRunnable {
     private List<ArenaTeam> teams;
     private ItemStack snowBall;
 
     public PaintballTimer(List<ArenaTeam> teams){
         this.teams = teams;
         snowBall = new ItemStack(Material.SNOW_BALL, 1);
     }
 
     @Override
     public void run() {
         for(ArenaTeam team: teams){
             for(ArenaPlayer arenaPlayer: team.getPlayers()){
                 if(arenaPlayer.getPlayerState() == ArenaPlayerState.PLAYING){
                     Player player = Bukkit.getPlayerExact(arenaPlayer.getPlayerName());
                    if(!player.isDead()){
                        player.getInventory().addItem(snowBall);
                    }
                 }
             }
         }
     }
 }

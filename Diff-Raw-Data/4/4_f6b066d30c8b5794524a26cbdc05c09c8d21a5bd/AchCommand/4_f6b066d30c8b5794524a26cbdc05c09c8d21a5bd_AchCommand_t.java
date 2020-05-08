 package me.tehbeard.BeardAch.commands;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import me.tehbeard.BeardAch.BeardAch;
 import me.tehbeard.BeardAch.achievement.Achievement;
 import me.tehbeard.BeardAch.achievement.AchievementPlayerLink;
 import me.tehbeard.utils.commands.ArgumentPack;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class AchCommand implements CommandExecutor{
 
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
         String player = (sender instanceof Player) ? ((Player)sender).getName() : null;
         
         ArgumentPack pack = new ArgumentPack(new String[0],new String[]{"p","a","n"}, args);
         
         int page = pack.getOption("n")==null? 1 : Integer.parseInt(pack.getOption("n")); 
         //select other player if provided
         if(pack.getOption("p")!=null){player = pack.getOption("p");}
 
 
         //Better pagination
         ArrayList<AchievementPlayerLink> list = new ArrayList<AchievementPlayerLink>(BeardAch.self.getAchievementManager().getAchievements(player));
 
         //sort list based on time
         Collections.sort(list, new Comparator<AchievementPlayerLink>() {
 
             public int compare(AchievementPlayerLink o1,
                     AchievementPlayerLink o2) {
                 return o2.getDate().compareTo(o1.getDate());
 
             }
         });
 
         
         if(pack.getOption("a")==null){
             
             int size = (int) Math.ceil(((double)list.size())/9.0);
            int s = Math.min(9,list.size());
             sender.sendMessage(ChatColor.AQUA + "Unlocked Achievements - page " + page + " of " + size + " :");
             int start = ((page-1)*9);
            for(int i=start;i<start+s;i++){
                 AchievementPlayerLink a = list.get(i);
                 if(a==null){break;}
                 sender.sendMessage(ChatColor.WHITE + "#" + a.getAch().getId() + " "+ ChatColor.GOLD + a.getAch().getName() + " - " + ChatColor.WHITE + a.getDate().toString());
             }
             String msg = BeardAch.self.getConfig().getString("ach.msg.ach", null);
 
             if(msg!=null){
                 sender.sendMessage(msg);
             }
         }else{
 
             Achievement a = BeardAch.self.getAchievementManager().getAchievement(Integer.parseInt(pack.getOption("a")));
             if(a!=null){
                 sender.sendMessage(ChatColor.GOLD + a.getName());
                 sender.sendMessage(ChatColor.BLUE + a.getDescrip());
 
                 //if they have unlocked it, tell them when they did
                 for( AchievementPlayerLink aLink:BeardAch.self.getAchievementManager().getAchievements(player)){
                     if(aLink.getSlug().equals(a.getSlug())){
                         sender.sendMessage(ChatColor.WHITE  + "Unlocked on: " + aLink.getDate().toString());
                     }
                 }
 
             }
         }
 
 
 
         return true;
     }
 
 }

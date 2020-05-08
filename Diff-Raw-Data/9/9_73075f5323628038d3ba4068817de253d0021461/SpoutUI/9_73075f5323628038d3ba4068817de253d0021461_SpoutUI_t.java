 package com.herocraftonline.dev.heroes.spout;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import org.bukkit.Material;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.Color;
 
 import java.awt.*;
 
 public class SpoutUI {
 
     public enum Cloaks{
         Mojang {
             public String toString(){
                 return "http://www.minecraftwiki.net/images/b/be/Mojang.png";
             }
         },
         Million {
             public String toString(){
                 return "http://www.minecraftwiki.net/images/f/f7/1MCape.png";
             }
         },
         dannyBstyle{
             public String toString(){
                 return "http://www.minecraftwiki.net/images/4/40/DBCape.png";
             }
         },
         JulianClark{
             public String toString(){
                 return "http://www.minecraftwiki.net/images/7/79/JulianClark.png";
             }
         },
         //MineCon{
         //    public String toString(){
         //        return "http://www.minecraftwiki.net/images/b/be/Mojang.png";
         //    }
         //},
         Christmas {
             public String toString(){
                 return "http://www.minecraftwiki.net/images/3/33/Xmas.png";
             }
         },
         NewYears{
             public String toString(){
                 return "http://www.minecraftwiki.net/images/b/b4/2011.png";
             }
         },
         Bacon{
             public String toString(){
                 return "http://www.minecraftwiki.net/images/6/63/BaconCape.png";
             }
         }
     }
 
 
     public static void sendPlayerNotification(Player player, String title, String Body, Material material) {
         if (!Heroes.useSpout)
             return;
         if (SpoutManager.getPlayer(player).isSpoutCraftEnabled()) {
             SpoutManager.getPlayer(player).sendNotification(title, Body, material);
         }
     }
 
    public static void screenText(Player player, String text, Color color, Point point, Heroes plugin) {
         if(!Heroes.useSpout)
             return;
         if(SpoutManager.getPlayer(player).isSpoutCraftEnabled()){
             GenericTextField textField = new GenericTextField();
             textField.setText(text);
             textField.setColor(color);
             textField.setY(point.y);
             textField.setX(point.x);
            SpoutManager.getPlayer(player).getMainScreen().attachWidget(plugin, textField);
         }
     }
 
     public static void cloakPlayer(Player player, String cloakName){
         if(!Heroes.useSpout)
             return;
         if(SpoutManager.getPlayer(player).isSpoutCraftEnabled()){
             SpoutManager.getAppearanceManager().setGlobalCloak((HumanEntity) player, Cloaks.valueOf(cloakName).toString());
         }
     }
 }

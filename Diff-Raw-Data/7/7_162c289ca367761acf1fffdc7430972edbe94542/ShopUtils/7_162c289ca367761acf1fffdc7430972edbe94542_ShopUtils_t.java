 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mewin.util;
 
 import com.mewin.perkShop.DatabaseConnector;
 import com.mewin.perkShop.PerkShopPlugin;
 import com.mewin.perkShop.shop.Order;
 import com.mewin.perkShop.shop.Perk;
 import java.text.SimpleDateFormat;
 import java.util.HashSet;
 import java.util.Set;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class ShopUtils {
     private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy, HH:mm");
     public static void checkExpiredPerks(Player player, DatabaseConnector db, PerkShopPlugin plugin)
     {
         Set<Order> playerOrders = db.getOrdersForMember(player.getName());
         
         for (Order order : playerOrders)
         {
             if (order.activated() && !order.expired())
             {
                 order.checkExpiredPerks(player, db, plugin);
             }
         }
     }
     
     
     
     public static Set<Perk> getActivePerks(String member, DatabaseConnector db)
     {
         Set<Perk> perks = new HashSet<>();
         Set<Order> orders = db.getOrdersForMember(member);
         
         for (Order order : orders)
         {
             for (Perk perk : order.perks)
             {
                 if (perk.activated && perk.canExpire())
                 {
                     perks.add(perk);
                 }
             }
         }
         
         return perks;
     }
     
     public static String getPerkExpireString(Perk perk)
     {
         if (!perk.hasOptions)
         {
             return null;
         }
         else if (!perk.canExpire())
         {
             return "Never";
         }
        else if(perk.expireDate != null)
         {
             return sdf.format(perk.expireDate);
         }
        else
        {
            return "Never";
        }
     }
 }

 package com.codisimus.plugins.pvpreward;
 
 /**
  * Holds messages that are displayed to users of this plugin
  *
  * @author Codisimus
  */
 public class PvPRewardMessages {
     private static String deaded;
     private static String killer;
     private static String deadedNotEnoughMoney;
     private static String killerNotEnoughMoney;
     private static String outlawBroadcast;
     private static String noLongerOutlawBroadcast;
     private static String karmaDecreased;
     private static String karmaIncreased;
     private static String karmaNoChange;
     private static String deathToll;
     private static String denyTele;
     private static String graveRob;
     private static String combatLoggerBroadcast;
     
     public static void setDeadedMsg(String msg) {
         deaded = format(msg);
     }
     
     public static void setKillerMsg(String msg) {
         killer = format(msg);
     }
     
     public static void setDeadedNotEnoughMoneyMsg(String msg) {
         deadedNotEnoughMoney = format(msg);
     }
     
     public static void setKillerNotEnoughMoneyMsg(String msg) {
         killerNotEnoughMoney = format(msg);
     }
     
     public static void setOutLawBroadcast(String msg) {
         outlawBroadcast = format(msg);
     }
     
     public static void setNoLongerOutLawBroadcast(String msg) {
         noLongerOutlawBroadcast = format(msg);
     }
     
     public static void setKarmaDecreasedMsg(String msg) {
         karmaDecreased = format(msg);
     }
     
     public static void setKarmaIncreasedMsg(String msg) {
         karmaIncreased = format(msg);
     }
     
     public static void setKarmaNoChangeMsg(String msg) {
         karmaNoChange = format(msg);
     }
     
     public static void setDeathTollMsg(String msg) {
         deathToll = format(msg);
     }
     
     public static void setDenyTeleMsg(String msg) {
         denyTele = format(msg);
     }
     
     public static void setGraveRobMsg(String msg) {
         graveRob = format(msg);
     }
     
     public static void setCombatLoggerBroadcast(String msg) {
         combatLoggerBroadcast = format(msg);
     }
     
     /**
      * Replaces specific values in the requested message
      * 
      * @param amount The amount of money
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getDeadedMsg(double amount, String killed, String killer, String karma) {
         String msg = deaded.replace("<killed>", killed).replace("<killer>", killer);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
             msg = msg.replace("<karma>", "-"+karma);
         else
             msg = msg.replace("<karma>", karma);
         
         return msg.replace("<amount>", Econ.format(amount));
     }
     
     /**
      * Replaces specific values in the requested message
      * 
      * @param amount The amount of money
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getKillerMsg(double amount, String killed, String killerName, String karma) {
         String msg = killer.replace("<killed>", killed).replace("<killer>", killerName);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
             msg = msg.replace("<karma>", "-"+karma);
         else
             msg = msg.replace("<karma>", karma);
         
         return msg.replace("<amount>", Econ.format(amount));
     }
     
     /**
      * Replaces specific values in the requested message
      * 
      * @param amount The amount of money
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getDeadedNotEnoughMoneyMsg(double amount, String killed, String killer, String karma) {
         String msg = deadedNotEnoughMoney.replace("<killed>", killed).replace("<killer>", killer);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
             msg = msg.replace("<karma>", "-"+karma);
         else
             msg = msg.replace("<karma>", karma);
         
         return msg.replace("<amount>", Econ.format(amount));
     }
     
     /**
      * Replaces specific values in the requested message
      * 
      * @param amount The amount of money
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getKillerNotEnoughMoneyMsg(double amount, String killed, String killer, String karma) {
         String msg = killerNotEnoughMoney.replace("<killed>", killed).replace("<killer>", killer);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
             msg = msg.replace("<karma>", "-"+karma);
         else
             msg = msg.replace("<karma>", karma);
         
         return msg.replace("<amount>", Econ.format(amount));
     }
     
     /**
      * Replaces specific values in the requested message
      * 
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getOutlawBroadcast(String killed, String killer, String karma) {
         String msg = outlawBroadcast.replace("<killed>", killed).replace("<killer>", killer);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
             msg = msg.replace("<karma>", "-"+karma);
         else
             msg = msg.replace("<karma>", karma);
         
         return msg;
     }
     
     /**
      * Replaces specific values in the requested message
      * 
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getNoLongerOutlawBroadcast(String killed, String killer, String karma) {
         String msg = noLongerOutlawBroadcast.replace("<killed>", killed).replace("<killer>", killer);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
             msg = msg.replace("<karma>", "-"+karma);
         else
             msg = msg.replace("<karma>", karma);
         
         return msg;
     }
     
     /**
      * Replaces specific values in the requested message
      * 
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getKarmaDecreasedMsg(String killed, String killer, String karma) {
         String msg = karmaDecreased.replace("<killed>", killed).replace("<killer>", killer);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
             msg = msg.replace("<karma>", "-"+karma);
         else
             msg = msg.replace("<karma>", karma);
         
         return msg;
     }
     
     /**
      * Replaces specific values in the requested message
      * 
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getKarmaIncreasedMsg(String killed, String killer, String karma) {
         String msg = karmaIncreased.replace("<killed>", killed).replace("<killer>", killer);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
             msg = msg.replace("<karma>", "-"+karma);
         else
             msg = msg.replace("<karma>", karma);
         
         return msg;
     }
     
     /**
      * Replaces specific values in the requested message
      * 
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getKarmaNoChangeMsg(String killed, String killer, String karma) {
         String msg = karmaNoChange.replace("<killed>", killed).replace("<killer>", killer);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
             msg = msg.replace("<karma>", "-"+karma);
         else
             msg = msg.replace("<karma>", karma);
         
         return msg;
     }
     
     public static String getDeathTollMsg(double amount) {
         return deathToll.replace("<amount>", Econ.format(amount));
     }
     
     public static String getDenyTeleMsg() {
         return denyTele;
     }
     
     public static String getGraveRobMsg() {
         return graveRob;
     }
     
     public static String getCombatLoggerBroadcast(double amount, String player) {
        return combatLoggerBroadcast.replace("<player>", player).replace("<amount>", Econ.format(amount));
     }
     
     /**
      * Adds various Unicode characters and colors to a string
      * 
      * @param string The string being formated
      * @return The formatted String
      */
     static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
     }
 }

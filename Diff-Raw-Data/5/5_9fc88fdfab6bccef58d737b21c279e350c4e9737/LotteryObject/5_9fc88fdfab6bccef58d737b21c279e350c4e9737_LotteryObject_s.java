 package com.imdeity.objects;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.iConomy.iConomy;
 import com.imdeity.lottery.Lottery;
 import com.imdeity.util.ChatTools;
 import com.imdeity.util.Settings;
 
 public class LotteryObject {
 
     public static int getPot() {
         String sql = "SELECT * FROM " + Settings.getMySQLPlayersTable()
                 + " ORDER BY `id` DESC LIMIT 1";
 
         try {
             return ((Integer.parseInt(Lottery.database.Read(sql).get(1).get(0))) * (Settings
                     .getTicketPrice())) + (Settings.getExtraPot());
         } catch (Exception ex) {
             return 0;
         }
     }
 
     public static String drawWinner() {
         String output = "";
         String sql = "";
         int winnings = 0;
         String winner = "";
 
         try {
             winnings = getPot();
             sql = "SELECT `username` FROM " + Settings.getMySQLPlayersTable()
                     + " ORDER BY RAND() LIMIT 1";
            winner = Lottery.database.Read(sql).get(1).get(1);
             
             if (winner.isEmpty() || winner == null) {
                winner = Lottery.database.Read(sql).get(1).get(1); 
             }
         } catch (Exception ex) { 
             ex.printStackTrace();
         }
 
         output = "<option><white>" + winner +  "<gray> won <yellow>"
                + winnings + ".00 Dei!";
 
         sql = "INSERT INTO  " + Settings.getMySQLWinnersTable() + " ("
                 + "`username`, `winnings`, `time`)" + "VALUES (" + "'"
                 + winner + "', '" + winnings + "', NOW());";
         if (!winner.isEmpty() && winner != null) {
             double money = winnings;
             iConomy.getAccount(winner).getHoldings().add(money);
             Lottery.database.Write(sql);
 
             clear();
             return output;
         } else {
             System.out.println("[Lottery] Winner field was null");
             return "";
         }
     }
 
     public static ArrayList<String> getWinners() {
         ArrayList<String> out = new ArrayList<String>();
 
         String sql = "";
         sql = "SELECT * FROM " + Settings.getMySQLWinnersTable()
                 + " ORDER BY `id` DESC LIMIT 10";
 
         HashMap<Integer, ArrayList<String>> winners = Lottery.database
                 .Read(sql);
         for (int i = 1; i <= winners.size(); i++) {
             out.add(ChatTools.formatUserList(winners.get(i).get(1), winners
                     .get(i).get(2)));
         }
         return out;
     }
 
     public static boolean isWinner(String name) {
 
         String sql = "";
         sql = "SELECT * FROM " + Settings.getMySQLWinnersTable()
                 + " ORDER BY `id` DESC LIMIT 10";
 
         HashMap<Integer, ArrayList<String>> winners = Lottery.database
                 .Read(sql);
         for (int i = 1; i <= winners.size(); i++) {
             if (winners.get(i).get(1).equalsIgnoreCase(name)) {
                 return true;
             }
         }
         return false;
     }
 
     public static int getNumTickets(String name) {
         String sql = "SELECT * FROM " + Settings.getMySQLPlayersTable()
                 + " WHERE `username` = '" + name + "';";
         try {
             return Lottery.database.Read(sql).size();
         } catch (Exception ex) {
             return 0;
         }
     }
 
     public static void clear() {
         String sql = "";
 
         sql = "TRUNCATE " + Settings.getMySQLPlayersTable() + ";";
         Lottery.database.Write(sql);
     }
 }

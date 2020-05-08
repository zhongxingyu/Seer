 package edu.gatech.cs2340.group29.spacemerchant.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import edu.gatech.cs2340.group29.spacemerchant.model.*;
 
 public class PlayerDataSource
 {
     private static String[] ALL_COLUMNS = { "name", "money",
             "pilotSkillPoints", "fighterSkillPoints", "traderSkillPoints",
             "engineerSkillPoints" };
 
     private SQLiteDatabase database;
     private DatabaseHelper databaseHelper;
 
     public PlayerDataSource(Context context)
     {
         databaseHelper = new DatabaseHelper(context);
     }
 
     public void open() throws SQLiteException
     {
         database = databaseHelper.getWritableDatabase();
     }
 
     public void close()
     {
         databaseHelper.close();
     }
 
     public void createPlayer(Player player)
     {
         ContentValues values = new ContentValues();
 
         String name = player.getName();
         int money = player.getMoney();
         int[] stats = player.getStats();
         int head = player.getHead();
         int body = player.getBody();
         int legs = player.getLegs();
         int feet = player.getLegs();
         
         Ship ship = player.getShip();
         
         long shipID = ship.getID();
 
         values.put("name", name);
         values.put("money", money);
         values.put("pilotSkillPoints", stats[0]);
         values.put("fighterSkillPoints", stats[1]);
         values.put("traderSkillPoints", stats[2]);
         values.put("engineerSkillPoints", stats[3]);
         values.put("head", head);
         values.put("body", body);
         values.put("legs", legs);
         values.put("feet", feet);
         values.put("ship", shipID);
 
         long playerID = database.insert("tb_player", null, values);
 
         player.setID(playerID);
         return;
     }
 
     public void deletePlayer(Player player)
     {
         long playerID = player.getID();
 
         database.delete("tb_player", "player=" + playerID, null);
     }
 
     public List<Player> getPlayerList()
     {
         List<Player> players = new ArrayList<Player>();
 
         Cursor cursor = database.query("tb_player", ALL_COLUMNS, null, null,
                 null, null, null);
 
         cursor.moveToFirst();
         
         while( !cursor.isAfterLast() )
         {
             Player player = cursorToPlayer(cursor);
             
             players.add(player);
             
             cursor.moveToNext();
             
         }
        
         cursor.close();
         return players;
     }
 
     public Player getPlayerByID(long playerID)
     {
 
         Cursor cursor = database.query("tb_player", ALL_COLUMNS, 
                 "player=" + playerID, null, null, null, null);
 
         cursor.moveToFirst();
 
        Player player = cursorToPlayer(cursor);
 
         cursor.close();
         return player;
     }
 
     public Player cursorToPlayer(Cursor cursor)
     {
 
         Player player = new Player();
 
         int[] stats = { cursor.getInt(3), cursor.getInt(4), cursor.getInt(5),
                 cursor.getInt(6) };
 
         player.setID(cursor.getLong(0));
         player.setName(cursor.getString(1));
         player.setMoney(cursor.getInt(2));
         player.setStats(stats);
 
         //long shipID = cursor.getLong(7);
 
         // change this to pull actual ship based on ID
         Ship ship = new Ship();
 
         player.setShip(ship);
 
         return player;
     }
 }

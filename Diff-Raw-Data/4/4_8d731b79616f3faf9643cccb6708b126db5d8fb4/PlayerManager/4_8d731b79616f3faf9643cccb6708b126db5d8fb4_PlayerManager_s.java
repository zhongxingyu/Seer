 package com.minecraftdimensions.bungeesuite.managers;
 
 import com.minecraftdimensions.bungeesuite.BungeeSuite;
 import com.minecraftdimensions.bungeesuite.Utilities;
 import com.minecraftdimensions.bungeesuite.configs.ChatConfig;
 import com.minecraftdimensions.bungeesuite.configs.MainConfig;
 import com.minecraftdimensions.bungeesuite.configs.SpawnConfig;
 import com.minecraftdimensions.bungeesuite.objects.BSPlayer;
 import com.minecraftdimensions.bungeesuite.objects.Messages;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.connection.Server;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.concurrent.TimeUnit;
 
 public class PlayerManager {
 
     public static HashMap<String, BSPlayer> onlinePlayers = new HashMap<>();
     static ProxyServer proxy = ProxyServer.getInstance();
     static BungeeSuite plugin = BungeeSuite.instance;
     public static ArrayList<ProxiedPlayer> kickedPlayers = new ArrayList<ProxiedPlayer>();
 
     public static boolean playerExists( String player ) {
         if ( getSimilarPlayer( player ) != null ) {
             return true;
         }
         return SQLManager.existanceQuery( "SELECT playername FROM BungeePlayers WHERE playername = '" + player + "'" );
     }
 
     public static void loadPlayer( ProxiedPlayer player ) throws SQLException {
         String nickname = null;
         String channel = null;
         boolean muted = false;
         boolean chatspying = false;
         boolean dnd = false;
         boolean tps = true;
         if ( playerExists( player.getName() ) ) {
             ResultSet res = SQLManager.sqlQuery( "SELECT playername,nickname,channel,muted,chat_spying,dnd,tps FROM BungeePlayers WHERE playername = '" + player + "'" );
             while ( res.next() ) {
                 nickname = res.getString( "nickname" );
                 if ( nickname != null ) {
                     nickname = Utilities.colorize( nickname );
                 }
                 channel = res.getString( "channel" );
                 muted = res.getBoolean( "muted" );
                 chatspying = res.getBoolean( "chat_spying" );
                 dnd = res.getBoolean( "dnd" );
                 tps = res.getBoolean( "tps" );
             }
             res.close();
             BSPlayer bsplayer = new BSPlayer( player.getName(), nickname, channel, muted, chatspying, dnd, tps );
             addPlayer( bsplayer );
             IgnoresManager.LoadPlayersIgnores( bsplayer );
             HomesManager.loadPlayersHomes( bsplayer );
         } else {
             createNewPlayer( player );
         }
     }
 
     private static void createNewPlayer( final ProxiedPlayer player ) throws SQLException {
         String ip = player.getAddress().getAddress().toString();
         SQLManager.standardQuery( "INSERT INTO BungeePlayers (playername,lastonline,ipaddress,channel) VALUES ('" + player.getName() + "', NOW(), '" + ip.substring( 1, ip.length() ) + "','" + ChatConfig.defaultChannel + "')" );
         final BSPlayer bsplayer = new BSPlayer( player.getName(), null, ChatConfig.defaultChannel, false, false, false, true );
         if ( MainConfig.newPlayerBroadcast ) {
             sendBroadcast( Messages.NEW_PLAYER_BROADCAST.replace( "{player}", player.getName() ) );
         }
         addPlayer( bsplayer );
 
         if ( SpawnConfig.newspawn && SpawnManager.NewPlayerSpawn != null ) {
             SpawnManager.newPlayers.add( player );
             ProxyServer.getInstance().getScheduler().schedule( BungeeSuite.instance, new Runnable() {
 
                 @Override
                 public void run() {
                     SpawnManager.sendPlayerToNewPlayerSpawn( bsplayer, true );
                     SpawnManager.newPlayers.remove( player );
                 }
 
             }, 300, TimeUnit.MILLISECONDS );
 
         }
     }
 
     private static void addPlayer( BSPlayer player ) {
         onlinePlayers.put( player.getName(), player );
         LoggingManager.log( Messages.PLAYER_LOAD.replace( "{player}", player.getName() ) );
     }
 
     public static void unloadPlayer( String player ) {
         if ( onlinePlayers.containsKey( player ) ) {
             onlinePlayers.remove( player );
             LoggingManager.log( Messages.PLAYER_UNLOAD.replace( "{player}", player ) );
         }
     }
 
     public static BSPlayer getPlayer( String player ) {
         return onlinePlayers.get( player );
     }
 
     public static BSPlayer getSimilarPlayer( String player ) {
         if ( onlinePlayers.containsKey( player ) ) {
             return onlinePlayers.get( player );
         }
         for ( String p : onlinePlayers.keySet() ) {
             if ( p.toLowerCase().contains( player.toLowerCase() ) ) {
                 return onlinePlayers.get( p );
             }
         }
         return null;
     }
 
     public static void sendPrivateMessageToPlayer( BSPlayer from, String receiver, String message ) {
         BSPlayer rec = getSimilarPlayer( receiver );
         if ( rec == null ) {
             from.sendMessage( Messages.PLAYER_NOT_ONLINE );
             return;
         }
         if ( rec.isIgnoring( from.getName() ) ) {
             from.sendMessage( Messages.PLAYER_IGNORING.replace( "{player}", rec.getName() ) );
             return;
         }
         from.sendMessage( Messages.PRIVATE_MESSAGE_OTHER_PLAYER.replace( "{player}", rec.getName() ).replace( "{message}", message ) );
         rec.sendMessage( Messages.PRIVATE_MESSAGE_RECEIVE.replace( "{player}", from.getName() ).replace( "{message}", message ) );
         rec.setReplyPlayer( from.getName() );
         sendPrivateMessageToSpies( from, rec, message );
     }
 
     public static void sendMessageToPlayer( String player, String message ) {
         if ( player.equals( "CONSOLE" ) ) {
             ProxyServer.getInstance().getConsole().sendMessage( message );
         } else {
             for ( String line : message.split( "\n" ) ) {
                 getPlayer( player ).sendMessage( line );
             }
         }
     }
 
     public static String getPlayersIP( String player ) throws SQLException {
         BSPlayer p = getSimilarPlayer( player );
         String ip = null;
         if ( p == null ) {
             ResultSet res = SQLManager.sqlQuery( "SELECT ipaddress FROM BungeePlayers WHERE playername = '" + player + "'" );
             while ( res.next() ) {
                 ip = res.getString( "ipaddress" );
             }
             res.close();
         } else {
             ip = p.getProxiedPlayer().getAddress().getAddress().toString();
         }
        return ip.substring( 1, ip.length() );
     }
 
     public static void sendBroadcast( String message ) {
         for ( ProxiedPlayer p : proxy.getPlayers() ) {
             for ( String line : message.split( "\n" ) ) {
                 p.sendMessage( line );
             }
         }
         LoggingManager.log( message );
     }
 
     public static boolean isPlayerOnline( String player ) {
         return onlinePlayers.containsKey( player );
     }
 
     public static boolean isSimilarPlayerOnline( String player ) {
         return getSimilarPlayer( player ) != null;
     }
 
     public static ArrayList<String> getPlayersAltAccounts( String player ) throws SQLException {
         ArrayList<String> accounts = new ArrayList<>();
         ResultSet res = SQLManager.sqlQuery( "SELECT playername from BungeePlayers WHERE ipaddress = (SELECT ipaddress FROM BungeePlayers WHERE playername = '" + player + "')" );
         while ( res.next() ) {
             accounts.add( res.getString( "playername" ) );
         }
         return accounts;
     }
 
     public static ArrayList<String> getPlayersAltAccountsByIP( String ip ) throws SQLException {
         ArrayList<String> accounts = new ArrayList<>();
         ResultSet res = SQLManager.sqlQuery( "SELECT playername from BungeePlayers WHERE ipaddress = '" + ip + "'" );
         while ( res.next() ) {
             accounts.add( res.getString( "playername" ) );
         }
         res.close();
         return accounts;
     }
 
     public static BSPlayer getPlayer( CommandSender sender ) {
         return onlinePlayers.get( sender.getName() );
     }
 
     public static void setPlayerAFK( String player, boolean sendGlobal, boolean hasDisplayPerm ) {
         BSPlayer p = getPlayer( player );
         if ( !p.isAFK() ) {
             p.setAFK( true );
             if ( sendGlobal ) {
                 sendBroadcast( Messages.PLAYER_AFK.replace( "{player}", p.getDisplayingName() ) );
             } else {
                 sendServerMessage( p.getServer(), Messages.PLAYER_AFK.replace( "{player}", p.getDisplayingName() ) );
             }
             if ( hasDisplayPerm ) {
                 p.setTempName( Messages.AFK_DISPLAY + p.getDisplayingName() );
             }
         } else {
             p.setAFK( false );
             if ( hasDisplayPerm ) {
                 p.revertName();
             }
             if ( sendGlobal ) {
                 sendBroadcast( Messages.PLAYER_NOT_AFK.replace( "{player}", p.getDisplayingName() ) );
             } else {
                 sendServerMessage( p.getServer(), Messages.PLAYER_NOT_AFK.replace( "{player}", p.getDisplayingName() ) );
             }
         }
 
     }
 
     private static void sendServerMessage( Server server, String message ) {
         for ( ProxiedPlayer p : server.getInfo().getPlayers() ) {
             for ( String line : message.split( "\n" ) ) {
                 p.sendMessage( line );
             }
         }
     }
 
     public static ArrayList<BSPlayer> getChatSpies() {
         ArrayList<BSPlayer> spies = new ArrayList<>();
         for ( BSPlayer p : onlinePlayers.values() ) {
             if ( p.isChatSpying() ) {
                 spies.add( p );
             }
         }
         return spies;
     }
 
     public static void sendPrivateMessageToSpies( BSPlayer sender, BSPlayer receiver, String message ) {
         for ( BSPlayer p : getChatSpies() ) {
             if ( !( p.equals( sender ) || p.equals( receiver ) ) ) {
                 p.sendMessage( Messages.PRIVATE_MESSAGE_SPY.replace( "{sender}", sender.getName() ).replace( "{player}", receiver.getName() ).replace( "{message}", message ) );
             }
         }
     }
 
     public static void sendMessageToSpies( Server server, String message ) {
         for ( BSPlayer p : getChatSpies() ) {
             if ( !p.getServer().getInfo().getName().equals( server.getInfo().getName() ) ) {
                 p.sendMessage( message );
             }
         }
     }
 
     public static void setPlayerChatSpy( BSPlayer p ) throws SQLException {
         if ( p.isChatSpying() ) {
             p.setChatSpying( false );
             p.sendMessage( Messages.CHATSPY_DISABLED );
         } else {
             p.setChatSpying( true );
             p.sendMessage( Messages.CHATSPY_ENABLED );
         }
         SQLManager.standardQuery( "UPDATE BungeePlayers SET chat_spying =" + p.isChatSpying() + " WHERE playername = '" + p.getName() + "'" );
 
     }
 
     public static boolean nickNameExists( String nick ) {
         return SQLManager.existanceQuery( "SELECT nickname FROM BungeePlayers WHERE nickname ='" + nick + "'" );
     }
 
     public static void setPlayersNickname( String p, String nick ) throws SQLException {
         if ( isPlayerOnline( p ) ) {
             getPlayer( p ).setNickname( nick );
             getPlayer( p ).updateDisplayName();
             getPlayer( p ).updatePlayer();
         }
         if ( nick == null ) {
             SQLManager.standardQuery( "UPDATE BungeePlayers SET nickname =NULL WHERE playername ='" + p + "'" );
         } else {
             SQLManager.standardQuery( "UPDATE BungeePlayers SET nickname ='" + nick + "' WHERE playername ='" + p + "'" );
         }
     }
 
     public static boolean isPlayerMuted( String target ) {
         if ( getSimilarPlayer( target ) != null ) {
             return getPlayer( target ).isMuted();
         } else {
             return SQLManager.existanceQuery( "SELECT muted FROM BungeePlayers WHERE playername ='" + target + "' AND muted = 1" );
         }
 
     }
 
     public static void mutePlayer( String target ) throws SQLException {
         BSPlayer p = getSimilarPlayer( target );
         boolean isMuted = isPlayerMuted( target );
         if ( p != null ) {
             if ( isMuted ) {
                 p.setMute( false );
                 p.sendMessage( Messages.UNMUTED );
             } else {
                 p.setMute( true );
                 p.sendMessage( Messages.MUTED );
             }
         }
         SQLManager.standardQuery( "UPDATE BungeePlayers SET muted = " + !isMuted + " WHERE playername ='" + target + "'" );
 
     }
 
     public static void tempMutePlayer( final BSPlayer t, int minutes ) throws SQLException {
         mutePlayer( t.getName() );
         BungeeSuite.proxy.getScheduler().schedule( plugin, new Runnable() {
             @Override
             public void run() {
                 if ( t.isMuted() ) {
                     try {
                         mutePlayer( t.getName() );
                     } catch ( SQLException e ) {
                         e.printStackTrace();
                     }
                 }
 
             }
         }, minutes, TimeUnit.MINUTES );
     }
 
     public static void getPlayerInformation( CommandSender arg0, String string ) {
 
     }
 
     public static boolean playerUsingNickname( String string ) {
         return SQLManager.existanceQuery( "SELECT playername FROM BungeePlayers WHERE nickname LIKE '%" + string + "%'" );
     }
 
     public static void removeNickname( String target ) throws SQLException {
         setPlayersNickname( target, null );
     }
 
     public static Collection<BSPlayer> getPlayers() {
         return onlinePlayers.values();
     }
 }

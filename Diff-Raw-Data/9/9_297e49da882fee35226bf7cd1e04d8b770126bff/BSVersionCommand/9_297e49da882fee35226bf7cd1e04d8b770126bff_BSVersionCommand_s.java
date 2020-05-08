 package com.minecraftdimensions.bungeesuite.commands;
 
 import com.minecraftdimensions.bungeesuite.BungeeSuite;
 import com.minecraftdimensions.bungeesuite.managers.*;
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 /**
  * User: Bloodsplat
  * Date: 13/10/13
  */
 public class BSVersionCommand extends Command {
 
     public BSVersionCommand() {
         super( "bsversion" );
     }
 
     @Override
     public void execute( CommandSender sender, String[] args ) {
        if ( !sender.hasPermission( "bungeesuite.version" ) || !sender.hasPermission( "bungeesuite.admin" ) ) {
             ProxiedPlayer p = ( ProxiedPlayer ) sender;
             p.chat( "/bsversion" );
         } else {
 
             sender.sendMessage( ChatColor.RED + "BungeeSuite version - " + ChatColor.GOLD + BungeeSuite.instance.getDescription().getVersion() );
             if ( sender instanceof ProxiedPlayer ) {
                 ProxiedPlayer p = ( ProxiedPlayer ) sender;
                 ServerInfo s = p.getServer().getInfo();
                 ByteArrayOutputStream b = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream( b );
                 try {
                     out.writeUTF( "GetVersion" );
                     out.writeUTF( p.getName() );
 
                 } catch ( IOException e ) {
                     e.printStackTrace();
                 }
                 ChatManager.sendPluginMessageTaskChat( s, b );
                 BansManager.sendPluginMessageTaskBans( s, b );
                 HomesManager.sendPluginMessageTaskHomes( s, b );
                 PortalManager.sendPluginMessageTaskPortals( s, b );
                 SpawnManager.sendPluginMessageTaskSpawns( s, b );
                 TeleportManager.sendPluginMessageTaskTP( s, b );
                 WarpsManager.sendPluginMessageTaskTP( s, b );
             }
         }
 
     }
 }

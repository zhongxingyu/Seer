 package com.minecraftdimensions.bungeesuite.commands;
 
 import com.minecraftdimensions.bungeesuite.objects.Messages;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 
 /**
  * User: Bloodsplat
  * Date: 13/10/13
  */
 public class MOTDCommand extends Command {
 
     public MOTDCommand() {
         super( "motd" );
     }
 
     @Override
     public void execute( CommandSender sender, String[] args ) {
        if ( !sender.hasPermission( "bungeesuite.motd" ) || !sender.hasPermission( "bungeesuite.admin" ) ) {
             ProxiedPlayer p = ( ProxiedPlayer ) sender;
             p.chat( "/motd" );
         } else {
             for ( String split : Messages.MOTD.split( "\n" ) ) {
                 sender.sendMessage( split );
             }
 
         }
     }
 
 }
 
 

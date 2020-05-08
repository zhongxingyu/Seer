 package com.ikkerens.worldedit.commands;
 
 import com.ikkerens.worldedit.WorldEditPlugin;
 import com.ikkerens.worldedit.handlers.ActionCommand;
 import com.ikkerens.worldedit.model.Clipboard;
 
 import com.mbserver.api.game.Player;
 
 public class SaveCommand extends ActionCommand< WorldEditPlugin > {
 
     public SaveCommand( final WorldEditPlugin plugin ) {
         super( plugin );
     }
 
     @Override
     protected void execute( final String label, final Player player, final String[] args ) {
         if ( args.length != 1 ) {
             player.sendMessage( "Usage: /" + label + " <filename>" );
             return;
         }
 
         final Clipboard clb = this.getSession( player ).getClipboard();
         if ( clb == null ) {
             player.sendMessage( "Your clipboard is empty!" );
             return;
         }
 
         if ( clb.save( args[ 0 ] ) )
            player.sendMessage( "Clipboard saved to \"plugins/MBWorldEdit/" + args[ 0 ] + ".mbschem\"" );
         else
             player.sendMessage( "Saving clipboard failed!" );
     }
 
 }

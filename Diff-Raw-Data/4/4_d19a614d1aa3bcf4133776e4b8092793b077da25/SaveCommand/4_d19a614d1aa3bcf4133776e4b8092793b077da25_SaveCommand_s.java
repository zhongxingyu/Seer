 package com.ikkerens.worldedit.commands;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 import com.ikkerens.worldedit.WorldEditPlugin;
 import com.ikkerens.worldedit.handlers.ActionCommand;
 import com.ikkerens.worldedit.model.events.ClipboardActionEvent;
 
 import com.mbserver.api.game.MBSchematic;
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
 
         this.getPlugin();
         final MBSchematic clb = WorldEditPlugin.getSession( player ).getClipboard();
         if ( clb == null ) {
             player.sendMessage( "Your clipboard is empty!" );
             return;
         }
 
         final ClipboardSaveEvent event = new ClipboardSaveEvent( player, args[ 0 ] );
         this.getPlugin().getPluginManager().triggerEvent( event );
 
         if ( !event.isCancelled() )
             try {
                clb.saveTo( String.format( "plugins/MBWorldEdit/%s.mbschem", args[ 0 ] ) );
                player.sendMessage( String.format( "Clipboard saved to \"plugins/MBWorldEdit/%s.mbschem\".", args[ 0 ] ) );
             } catch ( final IOException e ) {
                 this.getPlugin().getLogger().log( Level.SEVERE, "Could not save clipboard", e );
                 player.sendMessage( "Saving clipboard failed!" );
             }
     }
 
     public static class ClipboardSaveEvent extends ClipboardActionEvent {
 
         public ClipboardSaveEvent( final Player player, final String filename ) {
             super( player, filename );
         }
 
     }
 }

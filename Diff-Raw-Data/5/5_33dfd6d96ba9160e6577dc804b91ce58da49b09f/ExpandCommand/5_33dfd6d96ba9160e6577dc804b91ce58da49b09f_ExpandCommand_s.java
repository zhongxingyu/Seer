 package com.ikkerens.worldedit.commands;
 
 import com.ikkerens.worldedit.Util;
 import com.ikkerens.worldedit.WorldEditPlugin;
 import com.ikkerens.worldedit.handlers.AbstractCommand;
 import com.ikkerens.worldedit.model.Selection;
 import com.ikkerens.worldedit.model.events.SelectionCommandEvent;
 import com.ikkerens.worldedit.model.wand.Direction;
 
 import com.mbserver.api.game.Location;
 import com.mbserver.api.game.Player;
 
 public class ExpandCommand extends AbstractCommand< WorldEditPlugin > {
 
     public ExpandCommand( final WorldEditPlugin plugin ) {
         super( plugin );
     }
 
     @Override
     protected void execute( final String label, final Player player, final String[] args ) {
         if ( args.length != 2 ) {
             player.sendMessage( "Usage: /" + label + " <amount> <direction>" );
             return;
         }
 
         this.getPlugin();
         final Selection sel = WorldEditPlugin.getSession( player ).getSelection();
         if ( sel.isValid() ) {
             Location lowest, highest;
            if ( args[ 0 ].equalsIgnoreCase( "vert" ) ) {
                 final VerticalExpansionSelectionEvent event = new VerticalExpansionSelectionEvent( player );
                 this.getPlugin().getPluginManager().triggerEvent( event );
 
                 if ( !event.isCancelled() ) {
                     lowest = sel.getMinimumPosition();
                     highest = sel.getMaximumPosition();
 
                     sel.setPositions( Util.newLocation( sel.getWorld(), lowest.getX(), 0, highest.getZ() ), Util.newLocation( sel.getWorld(), highest.getX(), 127, highest.getZ() ) );
                     sel.inform();
                 } else
                     return;
             } else {
                 int amount;
                 try {
                     amount = Integer.parseInt( args[ 0 ] );
                 } catch ( final NumberFormatException e ) {
                     player.sendMessage( "That amount is invalid." );
                     return;
                 }
 
                if ( label.equalsIgnoreCase( "shrink" ) )
                     amount *= -1;
 
                 Direction dir;
                 try {
                     dir = Direction.valueOf( args[ 1 ].toUpperCase() );
                 } catch ( final IllegalArgumentException e ) {
                     player.sendMessage( "That direction is invalid." );
                     return;
                 }
 
                 final ExpandSelectionCommandEvent event = new ExpandSelectionCommandEvent( player, dir, amount );
                 this.getPlugin().getPluginManager().triggerEvent( event );
 
                 if ( !event.isCancelled() ) {
                     lowest = sel.getMinimumPosition();
                     highest = sel.getMaximumPosition();
 
                     switch ( dir ) {
                         case SOUTH:
                         case UP:
                         case EAST:
                             highest = dir.addToLocation( highest, amount );
                             break;
 
                         case NORTH:
                         case DOWN:
                         case WEST:
                             lowest = dir.addToLocation( lowest, amount );
                             break;
                     }
                 } else
                     return;
             }
 
             sel.setPositions( lowest, highest );
             sel.inform();
         } else
             player.sendMessage( NEED_SELECTION );
     }
 
     public static class ExpandSelectionCommandEvent extends SelectionCommandEvent {
         private final Direction direction;
         private final int       amount;
 
         public ExpandSelectionCommandEvent( final Player player, final Direction direction, final int amount ) {
             super( player );
             this.direction = direction;
             this.amount = amount;
         }
 
         public Direction getDirection() {
             return this.direction;
         }
 
         public int getAmount() {
             return this.amount;
         }
     }
 
     public static class VerticalExpansionSelectionEvent extends SelectionCommandEvent {
 
         public VerticalExpansionSelectionEvent( final Player player ) {
             super( player );
         }
 
     }
 
 }

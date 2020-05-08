 package com.ikkerens.worldedit.commands;
 
 import java.util.ArrayList;
 
 import com.ikkerens.worldedit.WorldEditPlugin;
 import com.ikkerens.worldedit.handlers.AbstractCommand;
 import com.ikkerens.worldedit.model.Selection;
 import com.ikkerens.worldedit.model.Session;
 import com.ikkerens.worldedit.model.events.WorldEditActionEvent;
 
 import com.mbserver.api.Constructors;
 import com.mbserver.api.game.Chunk;
 import com.mbserver.api.game.Location;
 import com.mbserver.api.game.Player;
 import com.mbserver.api.game.World;
 
 public class FixLightCommand extends AbstractCommand< WorldEditPlugin > {
 
     public FixLightCommand( final WorldEditPlugin plugin ) {
         super( plugin );
     }
 
     @Override
     protected void execute( final String label, final Player player, final String[] args ) {
         final Session session = WorldEditPlugin.getSession( player );
         final Selection sel = session.getSelection();
         if ( sel.isValid() ) {
             final Location lowest = sel.getMinimumPosition();
             final Location highest = sel.getMaximumPosition();
             final World world = lowest.getWorld();
 
             final FixLightEvent event = new FixLightEvent( player );
             this.getPlugin().getPluginManager().triggerEvent( event );
 
             if ( !event.isCancelled() ) {
                 final long start = System.currentTimeMillis();
 
                 final ArrayList< Chunk > chunks = new ArrayList< Chunk >();
                for ( int y = highest.getBlockY(); y >= lowest.getBlockY(); y += 16 ) {
                    if ( y < 0 || y > 127 )
                        continue;

                     for ( int x = lowest.getBlockX(); x <= highest.getBlockX(); x += 16 )
                         for ( int z = lowest.getBlockZ(); z <= highest.getBlockZ(); z += 16 ) {
                             final Chunk chunk = Constructors.newLocation( world, x, y, z ).getChunk();
                             if ( !chunks.contains( chunk ) )
                                 chunks.add( chunk );
                         }
                }
 
                 for ( final Chunk chunk : chunks )
                     chunk.recalculateLight();
 
                 player.sendMessage( String.format( "Action completed in %s seconds.", ( System.currentTimeMillis() - start ) / 1000f ) );
             }
         } else
             player.sendMessage( NEED_SELECTION );
     }
 
     public static class FixLightEvent extends WorldEditActionEvent {
 
         public FixLightEvent( final Player player ) {
             super( player, null );
         }
     }
 
 }

 package com.ikkerens.worldguard.listeners;
 
 import com.ikkerens.worldedit.handlers.AbstractListener;
 import com.ikkerens.worldguard.WorldGuardPlugin;
 import com.ikkerens.worldguard.model.Flags;
 import com.ikkerens.worldguard.model.MatchedRegion;
 import com.ikkerens.worldguard.model.flagtypes.GroupStateFlag.GroupState;
 
 import com.mbserver.api.events.EventHandler;
 import com.mbserver.api.events.PlayerMoveEvent;
 import com.mbserver.api.game.Player;
 
 public class MoveListener extends AbstractListener< WorldGuardPlugin > {
     private static final String ENTRY_KEY = "region_capture";
 
     private static final String NO_ENTRY  = "You are not allowed to go here.";
     private static final String NO_LEAVE  = "You are not allowed to leave this place.";
 
     public MoveListener( final WorldGuardPlugin plugin ) {
         super( plugin );
     }
 
     @EventHandler
     public void onMove( final PlayerMoveEvent event ) {
         final Player player = event.getPlayer();
 
         if ( player.hasPermission( "ikkerens.worldguard.admin" ) )
             return;
 
         final MatchedRegion rg = new MatchedRegion( this.getPlugin().getStorage(), event.getTargetLocation() );
         final GroupState entry = rg.getFlagValue( Flags.ENTRY );
         final GroupState leave = rg.getFlagValue( Flags.LEAVE );
        final int membership = rg.getMembership( player.getLoginName() );
 
         if ( membership < entry.ordinal() ) {
             event.getPlayer().sendMessage( NO_ENTRY );
             event.setCancelled( true );
             return;
         }
 
         if ( membership < leave.ordinal() )
             player.setMetaData( ENTRY_KEY, true );
         else
             player.setMetaData( ENTRY_KEY, false );
 
         if ( player.getMetaData( ENTRY_KEY, false ) )
             if ( leave != GroupState.ALLOW ) {
                 player.sendMessage( NO_LEAVE );
                 event.setCancelled( true );
             }
     }
 }

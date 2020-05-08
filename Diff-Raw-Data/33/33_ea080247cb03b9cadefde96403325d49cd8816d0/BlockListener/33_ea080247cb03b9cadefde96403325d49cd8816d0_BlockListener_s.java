 package com.ikkerens.worldguard.listeners;
 
 import com.ikkerens.worldedit.handlers.AbstractListener;
 import com.ikkerens.worldguard.WorldGuardPlugin;
 import com.ikkerens.worldguard.model.Flags;
 import com.ikkerens.worldguard.model.MatchedRegion;
 import com.ikkerens.worldguard.model.flagtypes.GroupStateFlag.GroupState;
 import com.ikkerens.worldguard.model.flagtypes.StateFlag.State;
 
 import com.mbserver.api.events.BlockEvent;
 import com.mbserver.api.events.BlockInteractEvent;
 import com.mbserver.api.events.ChestOpenEvent;
 import com.mbserver.api.events.EventHandler;
 import com.mbserver.api.events.TNTExplosionEvent;
 
 public class BlockListener extends AbstractListener< WorldGuardPlugin > {
 
     public BlockListener( final WorldGuardPlugin plugin ) {
         super( plugin );
     }
 
     @EventHandler
     public void onBlockEvent( final BlockEvent event ) {
         if ( event.getPlayer().hasPermission( "ikkerens.worldguard.admin" ) )
             return;
 
         final MatchedRegion rg = new MatchedRegion( this.getPlugin().getStorage(), event.getLocation() );
         final GroupState build = rg.getFlagValue( Flags.BUILD );
 
        if ( rg.getMembership( event.getPlayer().getName() ) < build.ordinal() )
             event.setCancelled( true );
     }
 
     @EventHandler
     public void onExplosion( final TNTExplosionEvent event ) {
         final MatchedRegion rg = new MatchedRegion( this.getPlugin().getStorage(), event.getLocation() );
         final State tnt = rg.getFlagValue( Flags.EXPLOSIONS );
 
         if ( tnt != State.DENY )
             event.setCancelled( true );
     }
 
     @EventHandler
     public void onInteract( final BlockInteractEvent event ) {
         if ( event.getPlayer().hasPermission( "ikkerens.worldguard.admin" ) )
             return;
 
         final MatchedRegion rg = new MatchedRegion( this.getPlugin().getStorage(), event.getLocation() );
        final int membership = rg.getMembership( event.getPlayer().getName() );
 
         if ( ( membership < rg.getFlagValue( Flags.INTERACT ).ordinal() ) || ( ( event instanceof ChestOpenEvent ) && ( membership < rg.getFlagValue( Flags.CHEST_ACCESS ).ordinal() ) ) )
             event.setCancelled( true );
     }
 }

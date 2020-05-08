 package com.ikkerens.worldguard.listeners;
 
 import com.ikkerens.worldedit.handlers.AbstractListener;
 import com.ikkerens.worldguard.WorldGuardPlugin;
 import com.ikkerens.worldguard.model.Flags;
 import com.ikkerens.worldguard.model.MatchedRegion;
 
 import com.mbserver.api.events.EventHandler;
 import com.mbserver.api.events.PlayerChatEvent;
 
 public class ChatListener extends AbstractListener< WorldGuardPlugin > {
 
     public ChatListener( final WorldGuardPlugin plugin ) {
         super( plugin );
     }
 
     @EventHandler
     public void onChat( final PlayerChatEvent event ) {
         if ( event.getPlayer().hasPermission( "ikkerens.worldguard.admin" ) )
             return;
 
         final MatchedRegion rg = new MatchedRegion( this.getPlugin().getStorage(), event.getPlayer().getLocation() );
 
        if ( rg.getMembership( event.getPlayer().getName() ) < rg.getFlagValue( Flags.CHAT ).ordinal() )
             event.setCancelled( true );
     }
 
 }

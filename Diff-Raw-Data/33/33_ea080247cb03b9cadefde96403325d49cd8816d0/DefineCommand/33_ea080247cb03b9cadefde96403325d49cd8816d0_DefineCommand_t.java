 package com.ikkerens.worldguard.commands;
 
 import com.ikkerens.worldedit.handlers.AbstractCommand;
 import com.ikkerens.worldedit.model.Selection;
 import com.ikkerens.worldguard.WorldGuardPlugin;
 import com.ikkerens.worldguard.model.Region;
 
 import com.mbserver.api.game.Player;
 
 public class DefineCommand extends AbstractCommand< WorldGuardPlugin > {
 
     public DefineCommand( final WorldGuardPlugin plugin ) {
         super( plugin );
     }
 
     @Override
     protected void execute( final String label, final Player player, final String[] args ) {
         if ( args.length != 1 )
             player.sendMessage( "Usage: /" + label + " <name>" );
 
         final Selection sel = this.getSession( player ).getSelection();
         if ( sel.isValid() ) {
             final Region region = new Region( args[ 0 ], sel.getMinimumPosition(), sel.getMaximumPosition() );
             this.getPlugin().getStorage().saveRegion( region );
 
             if ( label.equalsIgnoreCase( "/claim" ) )
                region.addOwner( player.getLoginName() );
 
             player.sendMessage( String.format( "The region named %s has been created%s",
                 region.getName(),
                 label.equalsIgnoreCase( "/claim" ) ? " and you have been added as its owner." : "." ) );
         } else
             player.sendMessage( NEED_SELECTION );
     }
 
 }

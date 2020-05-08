 package com.ikkerens.worldedit.commands;
 
 import com.ikkerens.worldedit.WorldEditPlugin;
 import com.ikkerens.worldedit.exceptions.BlockLimitException;
 import com.ikkerens.worldedit.exceptions.BlockNotFoundException;
 import com.ikkerens.worldedit.handlers.ActionCommand;
 import com.ikkerens.worldedit.model.Selection;
 import com.ikkerens.worldedit.model.Session;
 import com.ikkerens.worldedit.model.SetBlockType;
 import com.ikkerens.worldedit.model.WEAction;
 
 import com.mbserver.api.game.Location;
 import com.mbserver.api.game.Player;
 
 public class PyramidCommand extends ActionCommand< WorldEditPlugin > {
 
     public PyramidCommand( final WorldEditPlugin plugin ) {
         super( plugin );
     }
 
     @Override
     protected void execute( final String label, final Player player, final String[] args ) {
         if ( args.length != 2 ) {
             player.sendMessage( "Usage: /" + label + " <block> <height>" );
             return;
         }
 
         SetBlockType type;
         try {
             type = new SetBlockType( args[ 0 ] );
         } catch ( final BlockNotFoundException e ) {
             player.sendMessage( e.getMessage() );
             return;
         }
 
         int height;
         try {
             height = Integer.parseInt( args[ 1 ] );
         } catch ( final NumberFormatException e ) {
             player.sendMessage( "Invalid number: " + args[ 1 ] );
             return;
         }
 
         final Session session = this.getSession( player );
         final Selection sel = session.getSelection();
 
         final Location center = sel.getPosition1() != null ? sel.getPosition1() : ( sel.getPosition2() != null ? sel.getPosition2() : null );
         if ( center == null ) {
            player.sendMessage( "You need to mark a center with position 1 to create a sphere." );
             return;
         }
 
         final Selection cSel = new Selection( null );
         cSel.setPositions( center, center.add( height, height, height, true ) );
 
         final long start = System.currentTimeMillis();
         final WEAction wea = session.newAction( center.getWorld(), cSel.getCount() );
 
         try {
             this.generatePyramid( wea, center, type, height, label.equalsIgnoreCase( "/pyramid" ) );
             wea.finish();
         } catch ( final BlockLimitException e ) {
             player.sendMessage( String.format( FINISHED_LIMIT, wea.getAffected(), ( System.currentTimeMillis() - start ) / 1000f ) );
             return;
         }
 
         player.sendMessage( String.format( FINISHED_DONE, wea.getAffected(), ( System.currentTimeMillis() - start ) / 1000f ) );
     }
 
     private void generatePyramid( final WEAction wea, final Location center, final SetBlockType type, int size, final boolean filled ) throws BlockLimitException {
         final int height = size;
 
         final int cX = center.getBlockX();
         final int cY = center.getBlockY();
         final int cZ = center.getBlockZ();
 
         for ( int y = 0; y <= height; ++y ) {
             size--;
             for ( int x = 0; x <= size; ++x )
                 for ( int z = 0; z <= size; ++z )
                     if ( ( filled && ( z <= size ) && ( x <= size ) ) || ( z == size ) || ( x == size ) ) {
                         wea.setBlock( cX + x, cY + y, cZ + z, type );
                         wea.setBlock( cX - x, cY + y, cZ + z, type );
                         wea.setBlock( cX + x, cY + y, cZ - z, type );
                         wea.setBlock( cX - x, cY + y, cZ - z, type );
                     }
         }
     }
 }

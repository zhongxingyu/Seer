 package com.chewielouie.tinyrpg;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
import android.util.Log;
 import android.view.View;
 import com.chewielouie.tinyrpg.terrain.TerrainMap;
 import com.chewielouie.tinyrpg.terrain.Grass;
 import com.chewielouie.tinyrpg.terrain.TerrainPiece;
 
 public class WorldView extends View {
     private Context context;
     private TerrainMap terrainMap;
     private Canvas canvas;
 
     public WorldView( Context context, AttributeSet attrs ) {
         super( context, attrs );
         this.context = context;
     }
 
     public void setTerrain( TerrainMap map ) {
         this.terrainMap = map;
         invalidate();
     }
 
     protected void onDraw( Canvas c ) {
         this.canvas = c;
        Log.v( "WorldView", "onDraw canvas size is " + c.getWidth() + " x " + c.getHeight() );
         drawTerrain();
     }
 
     private void drawTerrain() {
         for( TerrainPiece p : terrainMap ) {
             if( p.terrain() instanceof Grass ) {
                 drawGrass( p.coordinate() );
             }
         }
     }
 
     private void drawGrass( Coordinate coord ) {
         CoordinateConverter c = new CoordinateConverter();
        c.setViewSize( canvas.getWidth(), canvas.getHeight() );
         c.setViewUnitSize( tileWidth(), tileHeight() );
         Coordinate viewCoord = c.convertWorldToView( coord );
        Log.v( "WorldView", "Drawing tile at " + viewCoord.x() + "," + viewCoord.y() );
         fillTile( viewCoord, Color.GREEN );
         highlightTile( viewCoord );
     }
 
     private int tileWidth() {
         return 60;
     }
 
     private int tileHeight() {
         return 60;
     }
 
     private void fillTile( Coordinate coord, int color ) {
         Paint paint = new Paint();
         paint.setStyle( Paint.Style.FILL );
         paint.setColor( color );
         drawTile( coord, paint );
     }
 
     private void drawTile( Coordinate coord, Paint paint ) {
         canvas.drawRect( coord.x(), coord.y(),
                 coord.x() + tileWidth(), coord.y() + tileHeight(), paint );
     }
 
     private void highlightTile( Coordinate coord ) {
         Paint paint = new Paint();
         paint.setStyle( Paint.Style.STROKE );
         paint.setColor( Color.RED );
         drawTile( coord, paint );
     }
 }
 

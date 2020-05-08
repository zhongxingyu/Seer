 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.common.data.chapter.elements;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
import java.util.Random;
 
 import es.eucm.eadventure.common.data.chapter.InfluenceArea;
 import es.eucm.eadventure.common.data.chapter.Rectangle;
 import es.eucm.eadventure.common.data.chapter.conditions.Conditions;
 
 /**
  * This class holds the data of an active area in eAdventure
  */
 public class ActiveArea extends Item implements Rectangle {
 
     /**
      * X position of the upper left corner of the exit
      */
     private int x;
 
     /**
      * Y position of the upper left corner of the exit
      */
     private int y;
 
     /**
      * Width of the exit
      */
     private int width;
 
     /**
      * Height of the exit
      */
     private int height;
 
     /**
      * True if the active area is rectangular
      */
     private boolean rectangular;
 
     /**
      * List of the points in the active area
      */
     private List<Point> points;
 
     /**
      * Conditions of the active area
      */
     private Conditions conditions;
 
     private InfluenceArea influenceArea;
 
     /**
      * Creates a new Exit
      * 
      * @param rectangular
      * 
      * @param x
      *            The horizontal coordinate of the upper left corner of the exit
      * @param y
      *            The vertical coordinate of the upper left corner of the exit
      * @param width
      *            The width of the exit
      * @param height
      *            The height of the exit
      */
     public ActiveArea( String id, boolean rectangular, int x, int y, int width, int height ) {
 
         super( id );
         this.rectangular = rectangular;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         points = new ArrayList<Point>( );
         conditions = new Conditions( );
         influenceArea = new InfluenceArea( );
     }
 
     /**
      * Returns the horizontal coordinate of the upper left corner of the exit
      * 
      * @return the horizontal coordinate of the upper left corner of the exit
      */
     public int getX( ) {
 
         if( rectangular )
             return x;
         else {
             int minX = Integer.MAX_VALUE;
             for( Point point : points ) {
                 if( point.x < minX )
                     minX = point.x;
             }
             return minX;
         }
     }
 
     /**
      * Returns the horizontal coordinate of the bottom right of the exit
      * 
      * @return the horizontal coordinate of the bottom right of the exit
      */
     public int getY( ) {
 
         if( rectangular )
             return y;
         else {
             int minY = Integer.MAX_VALUE;
             for( Point point : points ) {
                 if( point.y < minY )
                     minY = point.y;
             }
             return minY;
         }
     }
 
     /**
      * Returns the width of the exit
      * 
      * @return Width of the exit
      */
     public int getWidth( ) {
 
         if( rectangular )
             return width;
         else {
             int maxX = Integer.MIN_VALUE;
             int minX = Integer.MAX_VALUE;
             for( Point point : points ) {
                 if( point.x > maxX )
                     maxX = point.x;
                 if( point.x < minX )
                     minX = point.x;
             }
             return maxX - minX;
 
         }
     }
 
     /**
      * Returns the height of the exit
      * 
      * @return Height of the exit
      */
     public int getHeight( ) {
 
         if( rectangular )
             return height;
         else {
             int maxY = Integer.MIN_VALUE;
             int minY = Integer.MAX_VALUE;
             for( Point point : points ) {
                 if( point.y > maxY )
                     maxY = point.y;
                 if( point.y < minY )
                     minY = point.y;
             }
             return maxY - minY;
         }
 
     }
 
     public boolean isRectangular( ) {
 
         return rectangular;
     }
 
     public List<Point> getPoints( ) {
 
         return points;
     }
 
     public void addPoint( Point point ) {
 
         points.add( point );
     }
 
     /**
      * Set the values of the exit.
      * 
      * @param x
      *            X coordinate of the upper left point
      * @param y
      *            Y coordinate of the upper left point
      * @param width
      *            Width of the exit area
      * @param height
      *            Height of the exit area
      */
     public void setValues( int x, int y, int width, int height ) {
 
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
     }
 
     /**
      * @return the conditions
      */
     public Conditions getConditions( ) {
 
         return conditions;
     }
 
     /**
      * @param conditions
      *            the conditions to set
      */
     public void setConditions( Conditions conditions ) {
 
         this.conditions = conditions;
     }
 
     @Override
     public Object clone( ) throws CloneNotSupportedException {
 
         ActiveArea aa = (ActiveArea) super.clone( );
        
        //can not be two identical id
        String id = aa.getId( ) + "-" + ( new Random( ) ).nextInt( 1000 );
        aa.setId( id );
         aa.conditions = ( conditions != null ? (Conditions) conditions.clone( ) : null );
         aa.height = height;
         aa.width = width;
         aa.x = x;
         aa.y = y;
         aa.influenceArea = ( influenceArea != null ? (InfluenceArea) influenceArea.clone( ) : null );
         aa.rectangular = rectangular;
         aa.points = ( points != null ? new ArrayList<Point>( ) : null );
         for( Point p : points )
             aa.points.add( (Point) p.clone( ) );
         return aa;
     }
 
     public void setRectangular( boolean rectangular ) {
 
         this.rectangular = rectangular;
     }
 
     public InfluenceArea getInfluenceArea( ) {
 
         return influenceArea;
     }
 
     public void setInfluenceArea( InfluenceArea influeceArea ) {
 
         this.influenceArea = influeceArea;
     }
 }

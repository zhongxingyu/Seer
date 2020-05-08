 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.editor.gui.elementpanels.conversation.representation.graphicnode;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 import es.eucm.eadventure.common.data.chapter.conversation.node.ConversationNodeView;
 
 /**
  * This class is the graphic representation of a given conversational node.
  * Basically, it holds a link to the node, its position in the canvas, and links
  * to other GraphicNodes, so it can draw lines attaching nodes
  */
 public class GraphicNode {
 
     /**
      * Radius of the nodes.
      */
     public static final int NODE_RADIUS = 40;
 
     /**
      * Diameter for the nodes.
      */
     public static final int NODE_DIAMETER = 80;
 
     /**
      * Radius to paint the border on the nodes.
      */
     public static final int NODE_SELECTED_RADIUS = 45;
 
     /**
      * Diameter to paint the border on the nodes.
      */
     public static final int NODE_SELECTED_DIAMETER = 90;
 
     /**
      * Link to the conversational attached node
      */
     protected ConversationNodeView node;
 
     /**
      * Position of the node in the canvas (usually a JPanel)
      */
     protected Point position;
 
     /**
      * Link to other GraphicNodes, to draw connections
      */
     private List<GraphicNode> children;
 
     protected boolean selected;
 
     protected boolean selectedChild;
 
     /**
      * Constructor
      * 
      * @param node
      *            Link to the conversational node
      * @param position
      *            Position of the node
      */
     public GraphicNode( ConversationNodeView node, Point position ) {
 
         this.node = node;
         this.position = position;
         children = new ArrayList<GraphicNode>( );
         selected = false;
     }
 
     /**
      * Adds a new child to the node
      * 
      * @param graphicNode
      *            New child
      */
     public void addChild( GraphicNode graphicNode ) {
 
         children.add( graphicNode );
     }
 
     /**
      * Removes all the children of the graphic node
      */
     public void removeAllChildren( ) {
 
         while( !children.isEmpty( ) )
             children.remove( 0 );
     }
 
     /**
      * Returns the conversational node linked to the GraphicNode.
      * 
      * @return The contained node
      */
     public ConversationNodeView getNode( ) {
 
         return node;
     }
 
     /**
      * Returns the number of GraphicNode children.
      * 
      * @return The children count of the node
      */
     public int getChildCount( ) {
 
         return children.size( );
     }
 
     /**
      * Returns the position of the selected GraphicNode child.
      * 
      * @param scale
      * 
      * @param index
      *            Index of child
      * @return The position of the given child node
      */
     public Point getChildPosition( float scale, int index ) {
 
         return children.get( index ).getPosition( scale );
     }
 
     public GraphicNode getChildNode( int index ) {
 
         return children.get( index );
     }
 
     /**
      * Returns the position of the node in the canvas.
      * 
      * @param scale
      * 
      * @return The position of the node
      */
     public Point getPosition( float scale ) {
 
         return new Point( (int) ( position.x * scale ), (int) ( position.y * scale ) );
     }
 
     /**
      * Moves the node in the given increment.
      * 
      * @param increment
      *            Increment of position
      */
     public void moveNode( Point increment ) {
 
         position.x += increment.x;
         position.y += increment.y;
        if (position.x < 0)
            position.x = 0;
        if (position.y < 0)
            position.y = 0;
     }
 
     /**
      * Returns if the given point is inside the node (for selecting nodes with
      * mouse clicks).
      * 
      * @param scale
      * 
      * @param point
      *            Point we want to know if it is inside the node
      * @return True if the point is near enough of the node, false otherwise
      */
     public boolean isInside( float scale, Point point ) {
 
         // True if the distance is less than the radius of the node
         Point position = getPosition( scale );
         double difX = point.getX( ) - position.getX( );
         double difY = point.getY( ) - position.getY( );
         return Math.sqrt( difX * difX + difY * difY ) <= NODE_RADIUS * scale;
     }
 
     /**
      * Draws the current node in the stored position.
      * 
      * @param scale
      * 
      * @param g
      *            Graphics for drawing
      */
     public void drawNode( float scale, Graphics g ) {
 
         if( selected ) {
             int x = (int) ( ( position.getX( ) - NODE_SELECTED_RADIUS ) * scale );
             int y = (int) ( ( position.getY( ) - NODE_SELECTED_RADIUS ) * scale );
             int d = (int) ( NODE_SELECTED_DIAMETER * scale );
             g.setColor( Color.RED );
             g.fillOval( x, y, d, d );
         }
 
         if( selectedChild ) {
             int x = (int) ( ( position.getX( ) - NODE_SELECTED_RADIUS ) * scale );
             int y = (int) ( ( position.getY( ) - NODE_SELECTED_RADIUS ) * scale );
             int d = (int) ( NODE_SELECTED_DIAMETER * scale );
             g.setColor( Color.BLUE );
             g.fillOval( x, y, d, d );
         }
 
         // Draws a black circle
         g.setColor( Color.BLACK );
         int x = (int) ( ( position.getX( ) - NODE_RADIUS ) * scale );
         int y = (int) ( ( position.getY( ) - NODE_RADIUS ) * scale );
         int d = (int) ( NODE_DIAMETER * scale );
         g.fillOval( x, y, d, d );
     }
 
     public void setSelected( boolean selected ) {
 
         this.selected = selected;
     }
 
     public void setSelectedChild( boolean selectedChild ) {
 
         this.selectedChild = selectedChild;
     }
 }

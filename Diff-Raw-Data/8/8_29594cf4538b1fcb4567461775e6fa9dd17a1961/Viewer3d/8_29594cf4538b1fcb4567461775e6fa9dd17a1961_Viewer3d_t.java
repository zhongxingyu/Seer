 package com.ringlord.xs3d;
 
 import java.awt.Rectangle;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Color;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.BasicStroke;
 import java.awt.event.ActionEvent;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 import javax.swing.AbstractAction;
 
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 
 
 // This file is part of XS3D.
 //
 // XS3D is free software: you can redistribute it and/or modify it
 // under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // XS3D is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with XS3D. If not, see <http://www.gnu.org/licenses/>.
 
 /**
  * <p>
  * The 3D Viewer/Renderer for the elements defined by the {@link Mesh}. The
  * component's preferred (initial) size is 480x300 pixels.
  * </p>
  * 
  * <p>
  * To rotate the view, move mouse button while holding down the left button. To
  * zoom in/out, use the mouse wheel.
  * </p>
  * 
  * @author K. Udo Schuermann
  **/
 class Viewer3d
   extends JComponent
   implements ChangeListener
 {
   private static final long serialVersionUID = -2883317371127403464L;
 
   /**
    * Indicates whether to render a numeric count (starting at 1) near points,
    * edges, and faces as they are being rendered from the rear-most to the
    * front, to help identify the drawing order of {@link Mesh} elements.
    **/
   public static final boolean RENDER_DRAWING_DEPTH = false;
 
   /**
    * Indicates whether to use anti-aliased rendering, which creates the illusion
    * of smoother lines at the cost of filling pixels with a blend of color where
    * the displayed element would only occupy a part of a pixel. Anti-aliased
    * rendering is generally slower than non-AA rendering, but on modern 3D
    * hardware the difference may be small enough that the gain in smoothness is
    * worth it.
    **/
   public static final boolean RENDER_ANTI_ALIASED = true;
 
   /**
    * Indicates whether points are drawn as little spheres (true) or not at all
    * (false). Setting this value to true makes for a less "realistic" visual
    * representation but certainly has its uses.
    **/
   public static final boolean RENDER_POINTS = true;
 
 
   /**
    * <p>
    * Construct a Viewer3d Component whose initial viewing angle is
    * (-1,&nbsp;3.35,&nbsp;&pi;) and initial screen position is
    * (0,&nbsp;0,&nbsp;50) to provide an oblique view at the scene.
    * </p>
    * 
    * <p>
    * The Viewer3d is non-interactive by default. A convenient
    * {@link MouseHandler} is available which implements MouseListener,
    * MouseMotionListener, and MouseWheelListener interfaces to provide
    * rudimentary view manipulation.
    * </p>
    * 
    * <p>
    * Example #1: Convenient pre-built MouseHandler
    * </p>
    * 
    * <pre>
    *   Viewer3d v = new Viewer3d();
    * 
    *   // remembers the Viewer3d and adds itself to the Viewer3d as a
    *   MouseListener, MouseMotionListener, and MouseWheelListener:
    *   new MouseHandler( v );
    * </pre>
    * 
    * <p>
    * Example #2: Your own custom handlers
    * </p>
    * 
    * <pre>
    *   // 'final' ensures that the anonymous inner classes can
    *   // reference 'v' (the Viewer3d instance):
    *   final Viewer3d v = new Viewer3d();
    * 
    *   v.addMouseListener( new MouseListener()
    *     {
    *       &hellip;
    *     } );
    *   v.addMouseMotionListener( new MouseMotionListener()
    *     {
    *       &hellip;
    *     } );
    *   v.addMouseWheelListener( new MouseWheelListener()
    *     {
    *       &hellip;
    *     } );
    *   v.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW )
    *    .put( KeyStroke.getKeyStroke("ctrl L"),
    *          "reset" );
    * </pre>
    * 
    * <p>
    * NOTE: Supported actions are (see also example #2 above):
    * </p>
    * 
    * <dl>
    * <dt>"reset"
    * <dd>Resets the view to the initial position, rotation, and scale.
    * </dl>
    **/
   Viewer3d()
   {
     super();
     setOpaque( true );
     reset();
 
     // Define the "reset" action.
     getActionMap().put( "reset",
 	                new AbstractAction()
 	                {
 	                  private static final long serialVersionUID = -5310593910601826531L;
 
 
 	                  public void actionPerformed( final ActionEvent e )
 	                  {
 	                    reset();
 	                  }
 	                } );
   }
 
 
   public void reset()
   {
     // a fudge factor to control distortion
     this.modelScale = 1000;
 
     // do not call setWorldCenterXYZ as that one repaints
     worldCenterX = 0.0d;
     worldCenterY = 0.0d;
     worldCenterZ = 0.0d;
 
     // do not call setScreenPosition as that one repaints
     screenPositionX = 0.0d;
     screenPositionY = 0.0d;
     screenPositionZ = 50.d;
 
     // this method call repaints, which we want LAST!
     setViewAngle( new Vector3d( Math.toRadians( 192.5d ), // View toward 192½°
 	                        Math.toRadians( 30.0d ), // Positioned 30° above
 	                                                 // the "equator"
 	                        Math.PI ) ); // Z-axis perfectly straight
   }
 
 
   /**
    * Add a {@link Mesh} to the scene. The scene's display will be updated at
    * once. The Viewer3d will register itself as a {@link ChangeListener} on the
    * Mesh so that the Viewer3d will be notified when the Mesh changes, and can
    * show changes to the Mesh as they happen.
    * 
    * @param mesh
    *          The Mesh to add. It must not be null.
    **/
   public void add( final Mesh mesh )
   {
     meshes.add( mesh );
     meshArray = null;
     mesh.addChangeListener( this );
     repaint();
   }
 
 
   /**
    * Remove a previously added {@link Mesh} from the scene. The scene's display
    * will be updated at once. The Viewer3d will de-register iself from the Mesh.
    * 
    * @param mesh
    *          The Mesh to remove. It must not be null. Removing a Mesh that was
    *          not previously added (or removing it multiple times will not cause
    *          a problem).
    **/
   public void remove( final Mesh mesh )
   {
     // we COULD null zbuf to force zbuf to shrink
     mesh.removeChangeListener( this );
     meshes.remove( mesh );
     meshArray = null;
     repaint();
   }
 
 
   public Mesh[] meshes()
   {
     if( meshArray == null )
       {
 	meshArray = new Mesh[meshes.size()];
 	meshes.toArray( meshArray );
       }
     return meshArray;
   }
 
 
   /**
    * Describe to the Swing framework how large our preferred initial display
    * should be.
    * 
    * @return Currently hard-coded to 480&nbsp;x&nbsp;300 pixels.
    **/
   public Dimension getPreferredSize()
   {
     return new Dimension( 480,
 	                  300 );
   }
 
 
   public FocusInfo getFocusedMesh( final int focusX,
 	                           final int focusY )
   {
     if( zbuf == null )
       {
 	return null;
       }
     // Move backwards through the zbuffer, checking front-most items
     // before checking ones in the back (if we actually culled items
     // that are totally obscured, we could do this quicker)
     for( int i = zcount - 1; i >= 0; i-- )
       {
 	final ZRef z = zbuf[i];
 	final FocusInfo n = z.getAt( focusX,
 	                             focusY );
 	if( n != null )
 	  {
 	    // We have a mesh that would be focused, but if this mesh
 	    // is not focusable we will return null, as anything else
 	    // would be obscured by this one and should not be
 	    // selected until it would be above the current one.
 	    return (n.getMesh().isFocusable()
 		? n
 		: null);
 	  }
       }
     return null;
   }
 
 
   /**
    * Set the view angle which controls rotation around the vertical. It is the
    * primary means of affecting the view, and is updated when the mouse is
    * dragged with the left button held down.
    * 
    * @param viewAngle
    *          The view angle, see {@link #setViewAngle(double,double,double)}
    *          for details.
    * 
    * @see #setViewAngle(double,double,double)
    **/
   public void setViewAngle( final Vector3d viewAngle )
   {
     setViewAngle( viewAngle.x,
 	          viewAngle.y,
 	          viewAngle.z );
   }
 
 
   /**
    * Set the view angle which controls rotation around the vertical. It is the
    * primary means of affecting the view, and is updated when the mouse is
    * dragged with the left button held down.
    * 
    * @param x
    *          The x-component of the view angle (in radians): which direction
    *          along the "equator" are we looking. It defaults to 347½°
    * @param y
    *          The y-component of the view angle (in radians): How far above or
    *          below the "equator" the camera is located. -180° is looking
    *          straight up from the south pole, 0° is looking straight along the
    *          flat of the equator, 180° is looking straight down from the north
    *          pole. It defaults to 30° (above) the equator.
    * @param z
    *          The z-component of the view angle: How far is the Z-axis tilted.
    *          It defaults to 180°, pointing the Z-Axis is perfectly straight up.
    * 
    * @see #setViewAngle(Vector3d)
    **/
   public void setViewAngle( final double x,
 	                    final double y,
 	                    final double z )
   {
     // precalculate the values needed by the 'project' method
     this.cosTheta = Math.cos( x );
     this.sinTheta = Math.sin( x );
     this.cosPhi = Math.cos( y );
     this.sinPhi = Math.sin( y );
 
     this.sinThetaSinPhi = sinTheta * sinPhi;
     this.cosThetaSinPhi = cosTheta * sinPhi;
     this.sinThetaCosPhi = sinTheta * cosPhi;
     this.cosThetaCosPhi = cosTheta * cosPhi;
 
     this.viewAngleX = x;
     this.viewAngleY = y;
     this.viewAngleZ = z;
 
     repaint();
   }
 
 
   /**
    * Obtains the angle of rotation around the Z-axis.
    * 
    * @return The x-component of the view angle (in radians): which direction
    *         along the "equator" are we looking.
    */
   public double getViewAngleX()
   {
     return viewAngleX;
   }
 
 
   /**
    * Obtains the angle of rotation above or below the XY-plane
    * 
    * @return The y-component view angle (in radians): How far above or below the
    *         "equator" the camera is located. -180° is looking straight up from
    *         the south pole, 0° is looking straight along the flat of the
    *         equator, 180° is looking straight down from the north pole.
    */
   public double getViewAngleY()
   {
     return viewAngleY;
   }
 
 
   /**
    * Obtains the angle of rotation/tilt of the Z-axis.
    * 
    * @return The z-component of the view angle: How far is the Z-axis tilted. A
    *         value of 180° points the Z axis straight up.
    */
   public double getViewAngleZ()
   {
     return viewAngleZ;
   }
 
 
   public void setWorldCenterXYZ( final double x,
 	                         final double y,
 	                         final double z )
   {
     this.worldCenterX = x;
     this.worldCenterY = y;
     this.worldCenterZ = z;
 
     repaint();
   }
 
 
   /**
    * Sets the position of the screen that is mapped to the display, onto which
    * the 3D scene is projected. Changing the z-coordinate controls the zoom
    * level. The x and y coordinates control panning.
    * 
    * @param screenPosition
    *          Where the viewer / screen is positioned with respect to the scene.
    *          The x and y coordinate should be centered at (0,&nbsp;0), whereas
    *          the z-coordinate should be a positive value (it defaults to 50).
    **/
   public void setScreenPosition( final Vector3d screenPosition )
   {
     this.screenPositionX = screenPosition.x;
     this.screenPositionY = screenPosition.y;
     this.screenPositionZ = screenPosition.z;
 
     repaint();
   }
 
 
   public double getScreenPositionZ()
   {
     return screenPositionZ;
   }
 
 
   public void setScreenPositionZ( final double screenPositionZ )
   {
     this.screenPositionZ = screenPositionZ;
     repaint();
   }
 
 
   /**
    * Project the given point in 3D space to a 2D coordinate on the screen. This
    * is affected by the given screen center coordinate, the
    * {@link #setViewAngle(Vector3d)} and the
    * {@link #setScreenPosition(Vector3d)}.
    * 
    * @param xScreenCenter
    *          The horizontal center offset of the physical screen, which should
    *          be the horizontal bounds divided by 2.
    * 
    * @param yScreenCenter
    *          The vertical center offset of the physical screen, which should be
    *          the vertical bounds divided by 2.
    * 
    * @param point
    *          The point in 3D space to project.
    * 
    * @param p2d
    *          A point in 2D space, representing the (x,y) location of the 3D
    *          coordinate on the current display (it may be off-screen, though)
    *          and its depth (distance from the viewer, where a positive depth
    *          means that "in front of" and a negative depth means "behind the
    *          viewer". This depth value is used to determine visibility as well
    *          as drawing order.
    **/
   private void project( final double xScreenCenter,
 	                final double yScreenCenter,
 	                final Mesh.Point3d point,
 	                final Point2d p2d )
   {
     final double px = point.getX() - worldCenterX;
     final double py = point.getY() - worldCenterY;
     final double pz = point.getZ() - worldCenterZ;
 
     final double x = (screenPositionX + (px * cosTheta) - (py * sinTheta));
     final double y = (screenPositionY + (px * sinThetaSinPhi) + (py * cosThetaSinPhi) + (pz * cosPhi));
     final double z = ((screenPositionZ + (px * sinThetaCosPhi) + (py * cosThetaCosPhi) - (pz * sinPhi)));
     final double temp = modelScale * (viewAngleZ / z);
 
     // z is the distance from the viewer
     p2d.set( (int)(xScreenCenter + (temp * x)),
 	     (int)(yScreenCenter - (temp * y)),
 	     z );
   }
 
 
   // ======================================================================
   // ChangeListener
   // ======================================================================
   /**
    * Implementation of the {@link ChangeListener}, called by the Mesh when
    * something (a point, edge, or face) is added or removed from the Mesh. This
    * method causes the scene to be repainted.
    * 
    * @param e
    *          The ChangeEvent describing the change. Generally the source is one
    *          of the {@link Mesh}es added to this object.
    **/
   public void stateChanged( final ChangeEvent e )
   {
     repaint();
   }
 
 
   // ----------------------------------------------------------------------
 
   /**
    * This method is invoked by Swing whenever a repaint event is handled.
    **/
   @Override
   public void paintComponent( final Graphics g )
   {
     final long startTime = System.nanoTime();
 
     final Graphics2D g2 = (Graphics2D)g;
 
     final Rectangle bounds = getBounds();
 
     g2.setColor( Color.black );
     g2.fillRect( 0,
 	         0,
 	         bounds.width,
 	         bounds.height );
    
     // use anti-aliased drawing? (tends to be slower)
     if( RENDER_ANTI_ALIASED )
       {
 	g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
 	                     RenderingHints.VALUE_ANTIALIAS_ON );
       }
 
    final Rectangle dim = getBounds();
    final double xScreenCenter = dim.width / 2.0d;
    final double yScreenCenter = dim.height / 2.0d;
 
     // Collect ZRef objects which we can sort to ensure drawing from
     // back to front, and therefore effect proper depth perception,
     // especially when it comes to faces. It doesn't matter in
     // which order we process the points, edges, and faces of a
     // Mesh (in the loop below) but we'll do it in a "natural" order,
     // points first, edges next, and faces last.
     final List<ZRef> zref = new ArrayList<ZRef>();
     for( Mesh mesh : meshes() )
       {
 	if( mesh.isVisible() )
 	  {
 
 	    // Points
 	    for( Mesh.Point3d p : mesh.points() )
 	      {
 		final Point2d p2d = new Point2d();
 		project( xScreenCenter,
 		         yScreenCenter,
 		         p,
 		         p2d );
 		if( p2d.depth > 0 )
 		  {
 		    // The ZRef will take the Point2d's z-coordinate to
 		    // determine the distance from the viewer.
 		    zref.add( new ZRef( mesh,
 			                p,
 			                p2d ) );
 		  }
 	      }
 
 	    // Edges
 	    for( Mesh.Edge e : mesh.edges() )
 	      {
 		final Point2d p2d1 = new Point2d();
 		project( xScreenCenter,
 		         yScreenCenter,
 		         e.getHead(),
 		         p2d1 );
 		final Point2d p2d2 = new Point2d();
 		project( xScreenCenter,
 		         yScreenCenter,
 		         e.getTail(),
 		         p2d2 );
 		if( p2d1.depth > 0 )
 		  {
 		    if( p2d2.depth > 0 )
 		      {
 			// The line is fully in front of the viewer; the
 			// average distance of each point's z-coordinate
 			// will determine how far this line is from the
 			// viewer.
 			zref.add( new ZRef( mesh,
 			                    e,
 			                    p2d1,
 			                    p2d2 ) );
 		      }
 		    else
 		      {
 			// The start of the line is in FRONT of the
 			// viewer, but the end of it is BEHIND
 
 			// @@@ find intersection in x,y space, construct a
 			// new Point2d and store that as the END point,
 			// instead (i.e. compute a clipped line)
 		      }
 		  }
 		else if( p2d2.depth > 0 )
 		  {
 		    // The start of the line is BEHIND the viewer, the end
 		    // of it is in FRONT
 
 		    // @@@ find intersection in x,y space, construct a new
 		    // Point2d and store that as the START point, instead
 		    // (i.e. compute a clipped line)
 		  }
 	      }
 
 	    // Faces
 	    nextFace: for( Mesh.Face f : mesh.faces() )
 	      {
 		// start with an empty list of points
 		points.clear();
 		for( Mesh.Edge e : f.edges() )
 		  {
 		    final Point2d p2d1 = new Point2d();
 		    project( xScreenCenter,
 			     yScreenCenter,
 			     e.getHead(),
 			     p2d1 );
 		    final Point2d p2d2 = new Point2d();
 		    project( xScreenCenter,
 			     yScreenCenter,
 			     e.getTail(),
 			     p2d2 );
 		    if( (p2d1.depth < 0) || (p2d2.depth < 0) )
 		      {
 			// One or both points of this edge lies behind the
 			// viewer, so let's not render any part of the
 			// face because it gets really complicated
 			// trying to determine intersection points, and
 			// render only subsections of the face.
 			continue nextFace;
 		      }
 		    // As our edges should be defining a CLOSED series of
 		    // points, we simply capture the first point of each
 		    // edge
 		    points.add( p2d1 );
 		  }
 		// If we got here then we didn't do a 'continue nextMesh'
 		// in the loop above, meaning that we have a full set of
 		// at least 3 points now to enclose the face.
 		final Point2d[] pointList = new Point2d[points.size()];
 		points.toArray( pointList );
 		zref.add( new ZRef( mesh,
 		                    f,
 		                    pointList ) );
 	      }
 	  }
       }
 
     // Time to sort the ZRef: The Comparator for the ZRef causes the
     // elements farthest away to be ordered first in the list. We
     // convert the list to an array and loop through that as it is
     // generally much faster to process an array than a List (it has
     // been observed to be about 3× faster, actually, but that may
     // vary).
     zcount = zref.size();
     zbuf = zref.toArray( zbuf );
     Arrays.sort( zbuf,
 	         0,
 	         zcount );
 
     if( RENDER_DRAWING_DEPTH )
       {
 	_counter = 0;
       }
 
     // Render each of the elements in the ZRef structure. The number
     // of points referenced determines whether it's a point (1), an
     // edge (2), or a face (3+).
     for( int i = 0; i < zcount; i++ )
       {
 	final ZRef z = zbuf[i];
 	final Point2d[] pointList = z.get();
 	if( pointList.length == 1 )
 	  {
 	    // We have a single point
 	    if( RENDER_POINTS )
 	      {
 		paintPoint( g2,
 		            z.getPoint(),
 		            pointList[0] );
 	      }
 	  }
 	else if( pointList.length == 2 )
 	  {
 	    // We have an edge
 	    paintEdge( g2,
 		       z.getEdge(),
 		       pointList[0],
 		       pointList[1] );
 	  }
 	else
 	  {
 	    // We have 3+ so it's a face
 	    paintFace( g2,
 		       z.getFace(),
 		       pointList );
 	  }
       }
 
     long nanos = (System.nanoTime() - startTime);
     System.err.print( String.format( "\r%1.2f FPS",
 	                             1000000000.0d / nanos ) );
   }
 
 
   /**
    * Paints a point at the indicated Point2d (x,y) coordinate. Points are
    * rendered to appear like small spheres using concentric rings of color from
    * dark on the outer edge to white in the center.
    * 
    * @param g2
    *          The graphics object into which to render
    * @param p
    *          Where to render the point
    **/
   private void paintPoint( final Graphics2D g2,
 	                   final Mesh.Point3d point,
 	                   final Point2d p )
   {
     final Color[] colors;
     if( point.isSelected() )
       {
 	colors = SELECTED;
       }
     else if( point.isFocused() )
       {
 	colors = FOCUSED;
       }
     else
       {
 	colors = NORMAL;
       }
 
     g2.setColor( colors[0] );
     g2.fillOval( p.x - 3,
 	         p.y - 3,
 	         7,
 	         7 );
 
     g2.setColor( colors[1] );
     g2.fillOval( p.x - 2,
 	         p.y - 2,
 	         5,
 	         5 );
 
     g2.setColor( colors[2] );
     g2.fillOval( p.x - 1,
 	         p.y - 1,
 	         3,
 	         3 );
 
     if( RENDER_DRAWING_DEPTH )
       {
 	g2.drawString( String.valueOf( ++_counter ),
 	               p.x + 5,
 	               p.y );
       }
   }
 
 
   /**
    * Paints a colored edge (a line between two coordinates).
    * 
    * @param g2
    *          The graphics object into which to render
    * @param c
    *          The color for the line
    * @param head
    *          The starting point of the edge
    * @param tail
    *          The ending point of the edge
    **/
   private void paintEdge( final Graphics2D g2,
 	                  final Mesh.Edge edge,
 	                  final Point2d head,
 	                  final Point2d tail )
   {
     final Mesh.Coloring coloring = edge.getColoring();
     if( coloring != null )
       {
 	final Color color;
 	if( edge.isSelected() )
 	  {
 	    color = coloring.selected();
 	  }
 	else if( edge.isFocused() )
 	  {
 	    color = coloring.focused();
 	  }
 	else
 	  {
 	    color = coloring.normal();
 	  }
 
 	// no color, no rendering
 	if( color != null )
 	  {
 	    g2.setColor( color );
 	    if( originalStroke == null )
 	      {
 		originalStroke = g2.getStroke();
 	      }
 	    g2.setStroke( edge.isSelected()
 		? selectedStroke
 		: originalStroke );
 	    g2.drawLine( head.x,
 		         head.y,
 		         tail.x,
 		         tail.y );
 	    g2.setStroke( originalStroke );
 	    if( RENDER_DRAWING_DEPTH )
 	      {
 		g2.drawString( String.valueOf( ++_counter ),
 		               (head.x + tail.x) / 2 + 5,
 		               (head.y + tail.y) / 2 + 5 );
 	      }
 	  }
       }
   }
 
 
   /**
    * Paints a color-filled face using three or more points in 3D space.
    * 
    * @param g2
    *          The graphics object into which to render
    * @param c
    *          The color for the face
    * @param pN
    *          Three or more points to define the face corners.
    **/
   private void paintFace( final Graphics2D g2,
 	                  final Mesh.Face face,
 	                  final Point2d[] pN )
   {
     final Mesh.Coloring coloring = face.getColoring();
     if( coloring != null )
       {
 	final Color color;
 
 	if( face.isSelected() )
 	  {
 	    color = coloring.selected();
 	  }
 	else if( face.isFocused() )
 	  {
 	    color = coloring.focused();
 	  }
 	else
 	  {
 	    color = coloring.normal();
 	  }
 
 	if( color != null )
 	  {
 	    g2.setColor( color );
 
 	    final int size = pN.length;
 	    final int[] x = new int[size];
 	    final int[] y = new int[size];
 
 	    int n = 0;
 	    for( int i = 0; i < pN.length; i++ )
 	      {
 		x[n] = pN[i].x;
 		y[n] = pN[i].y;
 		n++;
 	      }
 
 	    g2.fillPolygon( x,
 		            y,
 		            size );
 
 	    if( RENDER_DRAWING_DEPTH )
 	      {
 		// find the center of the face, drawString ++_counter there
 		// (as in paintPoint and paintEdge above)
 	      }
 	  }
       }
   }
 
 
   /**
    * A vector in 3D space, structurally the same as a 3D coordinate.
    **/
   static class Vector3d
   {
     Vector3d( final double x,
 	      final double y,
 	      final double z )
     {
       this.x = x;
       this.y = y;
       this.z = z;
     }
     private final double x, y, z;
   }
 
 
   /**
    * A point in 2D space (on the screen) with associated distance from the
    * viewer that allows for depth sorting.
    **/
   static class Point2d
   {
     void set( final int x,
 	      final int y,
 	      final double depth )
     {
       this.x = x;
       this.y = y;
       this.depth = depth;
     }
 
 
     int getX()
     {
       return x;
     }
 
 
     int getY()
     {
       return y;
     }
 
 
     double getDepth()
     {
       return depth;
     }
     private int x, y;
     private double depth;
   }
 
 
   /**
    * A ZRef references one or more coordinates (and associated color where
    * applicable) for points, edges, and faces. The important feature here is
    * that we've already calculated where on the screen these points are to be
    * displayed, so we don't need to recalculate that information. If our Mesh
    * elements (Point, Edge, Face) had more attributes, then we might want to
    * reference them directly here, but for now capturing their color is all we
    * need here.
    **/
   static class ZRef
     implements Comparable<ZRef>
   {
     ZRef( final Mesh mesh,
 	  final Mesh.Point3d point,
 	  final Point2d ref )
     {
       this.refs = new Point2d[] {ref};
       this.avgDepth = ref.depth;
       //
       this.mesh = mesh;
       this.point = point;
     }
 
 
     ZRef( final Mesh mesh,
 	  final Mesh.Edge edge,
 	  final Point2d refHead,
 	  final Point2d refTail )
     {
       this.refs = new Point2d[] {refHead, refTail};
       this.avgDepth = (refHead.depth + refTail.depth) / 2.0d;
       //
       this.mesh = mesh;
       this.edge = edge;
     }
 
 
     ZRef( final Mesh mesh,
 	  final Mesh.Face face,
 	  final Point2d[] refs )
     {
       this.refs = refs;
       fixDepth();
       //
       this.mesh = mesh;
       this.face = face;
     }
 
 
     Point2d[] get()
     {
       return Arrays.copyOf( refs,
 	                    refs.length );
     }
 
 
     public Mesh getMesh()
     {
       return mesh;
     }
 
 
     public Mesh.Face getFace()
     {
       return face;
     }
 
 
     public Mesh.Edge getEdge()
     {
       return edge;
     }
 
 
     public Mesh.Point3d getPoint()
     {
       return point;
     }
 
 
     public FocusInfo getAt( final int focusX,
 	                    final int focusY )
     {
       if( face != null )
 	{
 	  boolean isInside = false;
 	  // last/previous one
 	  Point2d p = refs[refs.length - 1];
 	  for( Point2d v : refs )
 	    {
 	      final int x0 = p.getX();
 	      final int y0 = p.getY();
 
 	      final int x1 = v.getX();
 	      final int y1 = v.getY();
 
 	      if( (((y0 <= focusY) && (focusY < y1)) || ((y1 <= focusY) && (focusY < y0))) &&
 		  (focusX < (((x0 - x1) * (focusY - y1)) / (y0 - y1)) + x1) )
 		{
 		  isInside = !isInside;
 		}
 	      p = v;
 	    }
 	  if( isInside )
 	    {
 	      return new FocusInfo( mesh,
 		                    face );
 	    }
 	}
       else if( edge != null )
 	{
 	  Point2d p1 = refs[0];
 	  Point2d p2 = refs[1];
 
 	  final int x1 = p1.getX();
 	  final int y1 = p1.getY();
 
 	  final int x2 = p2.getX();
 	  final int y2 = p2.getY();
 
 	  final int dX = x2 - x1;
 	  final int dY = y2 - y1;
 	  final float du = (dX * dX + dY * dY);
 	  if( du != 0.0f )
 	    {
 	      final float u = (((dX * (focusX - x1)) + (dY * (focusY - y1))) / du);
 
 	      // Check whether we have no perpendicular line through
 	      // the segment, i.e. we have something like the left
 	      // side vertical line that misses the segment ("whoops")
 	      // rather than crossing it ("yay")
 	      //
 	      // p + p +
 	      // | |
 	      // | p1 +-------+-----------+ p2
 	      // | |
 	      // : whoops |
 	      // + yay
 	      //
 	      // For what it's worth, the following holds:
 	      //
 	      // u < 0 : it misses on the x1/y1 side of the segment
 	      // u > 1 : it misses on the x2/y2 side of the segment
 	      //
 	      // But we don't care about that; all we really need to
 	      // know here is that we do NOT intercept the line
 	      // segment:
 	      if( (u >= 0.0f) && (u <= 1.0f) )
 		{
 		  // The perpendicular line intercepts the line segment
 		  // somewhere,
 		  // so let's find out the distance. if it's less than our
 		  // fuzziness
 		  // value, then the point is close enough to the line to be
 		  // considered "on" it:
 		  final float x3 = x1 + u * dX;
 		  final float y3 = y1 + u * dY;
 
 		  final float dist = (float)Math.hypot( (x3 - focusX),
 			                                (y3 - focusY) );
 		  if( dist <= 5.0f )
 		    {
 		      return new FocusInfo( mesh,
 			                    edge );
 		    }
 		}
 	    }
 	}
       else if( point != null )
 	{
 	  Point2d p = refs[0];
 
 	  if( (Math.abs( p.getX() - focusX ) < 6) && (Math.abs( p.getY() - focusY ) < 6) )
 	    {
 	      return new FocusInfo( mesh,
 		                    point );
 	    }
 	}
       return null;
     }
 
 
     public int compareTo( final ZRef other )
     {
       if( avgDepth > other.avgDepth )
 	{
 	  // we're farther away, so put us before the other ZRef
 	  return -1;
 	}
       if( avgDepth < other.avgDepth )
 	{
 	  // we're nearer, so put us after the other ZRef
 	  return 1;
 	}
       // same distance
       return 0;
     }
 
 
     /**
      * Convenience method for calculating the average depth of the referenced
      * points. We call this only when we have 2 or more, as it's easy enough
      * with 2 points to calculate the average directly.
      **/
     private void fixDepth()
     {
       double d = 0.0d;
       for( Point2d p : refs )
 	{
 	  d += p.depth;
 	}
       avgDepth = d / refs.length;
     }
     private final Point2d[] refs;
     private double avgDepth;
     //
     private Mesh mesh;
     private Mesh.Face face;
     private Mesh.Edge edge;
     private Mesh.Point3d point;
   }
 
   // values controlling the 3D projection
   private double screenPositionX, screenPositionY, screenPositionZ;
   private double viewAngleX, viewAngleY, viewAngleZ;
   private double worldCenterX, worldCenterY, worldCenterZ;
   private int modelScale;
   // values that are changed only when the viewangle is altered, and
   // are therefore pre-computed and cached for optimal performance
   private double cosTheta, sinTheta, cosPhi, sinPhi;
   private double sinThetaSinPhi, cosThetaSinPhi, sinThetaCosPhi, cosThetaCosPhi;
   // when RENDER_DRAWING_DEPTH is set to true, this counter is reset
   // during each drawing cycle, incremented for each Mesh element that
   // is drawn, and its value painted next to that element to provide
   // visual feedback for the drawing order
   private int _counter;
   //
   /**
    * Reused structure for collecting the edge points of a face.
    **/
   private final List<Point2d> points = new ArrayList<Point2d>();
   /**
    * The {@link Mesh}es to be rendered.
    **/
   private Mesh[] meshArray;
   private int zcount; // how many in zbuf are actually used
   private ZRef[] zbuf = new ZRef[0]; // quicker than a List<ZBuf>, never shrinks
   private final List<Mesh> meshes = new ArrayList<Mesh>();
   //
   // colors for drawing the little spheres to represent points
   private static final Color GRAY = new Color( 127,
 	                                       127,
 	                                       127 );
   private static final Color LGRAY = new Color( 191,
 	                                        191,
 	                                        191 );
   private static final Color WHITE = new Color( 255,
 	                                        255,
 	                                        255 );
   //
   private static final Color BROWN = new Color( 127,
 	                                        127,
 	                                        0 );
   private static final Color YELLOW = new Color( 191,
 	                                         191,
 	                                         0 );
   private static final Color BRIGHT_YELLOW = new Color( 255,
 	                                                255,
 	                                                0 );
   //
   private static final Color DARK_RED = new Color( 127,
 	                                           0,
 	                                           0 );
   private static final Color RED = new Color( 191,
 	                                      0,
 	                                      0 );
   private static final Color BRIGHT_RED = new Color( 255,
 	                                             0,
 	                                             0 );
   //
   private static final Color[] NORMAL = new Color[] {GRAY, LGRAY, WHITE};
   private static final Color[] FOCUSED = new Color[] {BROWN, YELLOW, BRIGHT_YELLOW};
   private static final Color[] SELECTED = new Color[] {DARK_RED, RED, BRIGHT_RED};
   private static Stroke originalStroke;
   private static final Stroke selectedStroke = new BasicStroke( 3,
 	                                                        BasicStroke.CAP_BUTT,
 	                                                        BasicStroke.JOIN_ROUND );
 }

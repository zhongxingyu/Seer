 package com.phyloa.dlib.dui;
 
 import javax.vecmath.Point2i;
 
 import com.phyloa.dlib.renderer.Renderer2D;
 
 public class DScrollPane extends DUIElement
 {
 	DPanel innerPane;
 	int scrollx;
 	int scrolly;
 	
 	public DScrollPane( int x, int y, int width, int height )
 	{
 		super( x, y, width, height );
 	}
 	
 	public void setInnerPane( DPanel innerPane )
 	{
 		this.innerPane = innerPane;
 	}
 
 	public void keyPressed( DKeyEvent dke )
 	{
 		
 	}
 
 	public void keyReleased( DKeyEvent dke )
 	{
 		
 	}
 
 	public void mouseEntered( DMouseEvent e )
 	{
 		
 	}
 
 	public void mouseExited( DMouseEvent e )
 	{
 		
 	}
 
 	public void mousePressed( DMouseEvent e )
 	{
 		
 	}
 
 	public void mouseReleased( DMouseEvent e )
 	{
 		
 	}
 
 	public void mouseMoved( DMouseEvent e )
 	{
 		
 	}
 
 	public void mouseDragged( DMouseEvent e )
 	{
 		
 	}
 
 	public void render( Renderer2D r )
 	{
 		
 	}
 
 	public void update( DUI ui )
 	{
 		
 	}
 	
 	@Override
 	public void renderChildren( Renderer2D r )
 	{
 		if( visible )
 		{
 			Point2i pos = getScreenLocation();
 			r.setClip( pos.x, pos.y, width, height );
 			r.pushMatrix();
 			r.translate( x, y );
 			r.translate( -scrollx, -scrolly );
 			for( int i = 0; i < children.size(); i++ )
 			{
 				children.get( i ).render( r );
 				children.get( i ).renderChildren( r );
 			}
 			r.popMatrix();
 			r.clearClip();
 		}
 	}
 	
 	public void handleChildrenMouseMoved( DMouseEvent e )
 	{
 		if( visible )
 		{
 			e.x = e.x - this.x + scrollx;
 			e.y = e.y - this.y + scrolly;
 			for( int i = 0; i < children.size(); i++ )
 			{
 				DUIElement el = children.get( i );
 				boolean inside = el.isInside( e.x, e.y );
 				if( inside || el.isInside )
 				{
 					el.isInside = inside;
 					el.mouseMoved( e );
 					el.handleChildrenMouseMoved( e );
 				}
 			}
 		}
 	}
 	
 	public void handleChildrenMousePressed( DMouseEvent e )
 	{
 		if( visible )
 		{
 			ui.setFocus( this );
			e.x = e.x - this.x + scrollx;
			e.y = e.y - this.y + scrolly;
 			for( int i = 0; i < children.size(); i++ )
 			{
 				DUIElement el = children.get( i );
 				if( el.isInside( e.x, e.y ) )
 				{
 					el.mousePressed( e );
 					el.handleChildrenMousePressed( e );
 				}
 			}
 		}
 	}
 	
 	public void handleChildrenMouseReleased( DMouseEvent e )
 	{
 		if( visible )
 		{
			e.x = e.x - this.x + scrollx;
			e.y = e.y - this.y + scrolly;
 			for( int i = 0; i < children.size(); i++ )
 			{
 				DUIElement el = children.get( i );
 				if( el.isInside( e.x, e.y ) )
 				{
 					el.mouseReleased( e );
 					el.handleChildrenMouseReleased( e );
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseWheel( DMouseEvent dme )
 	{
 		scrolly -= dme.wheel * .5f;
 	}
 }

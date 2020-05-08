 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.java2d;
 
 import ca.couchware.wezzle2d.graphics.AbstractEntity;
 import ca.couchware.wezzle2d.graphics.IPositionable.Alignment;
 import ca.couchware.wezzle2d.ui.ILabel;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.font.TextLayout;
 import java.awt.geom.Rectangle2D;
 import java.awt.AlphaComposite;
 import java.awt.Composite;
 import java.awt.font.FontRenderContext;
 import java.util.EnumSet;
 
 /**
  * This class provides a Java2D implementation of the Label interface.
  * The font is hard coded as BubbleBoy2 but may be be changed in the future.
  * 
  * @author Kevin, Cameron
  *
  */
 
 public class Java2DLabel extends AbstractEntity implements ILabel
 {           
     
     /**
      * The text in the baseline text layout.
      */
     final private static String baselineText = "A";
     
     /** 
      * The game window to which this text is going to be drawn 
      */
 	final private Java2DGameWindow window;	       
     
     /**
      * The color of the label.
      */
     final private Color color;
     
     /**
      * The size of the label.
      */
     final private float size;
     
     /**
      * The text of the label.
      */
     final private String text;
     
 	/** 
      * The font. 
      */
 	final private Font font;         
     
     /**
      * The baseline text layout.
      */
     final private TextLayout baselineLayout;
     
     /**
      * The text layout instance.
      */
 	final private TextLayout textLayout;		
     
     /**
      * Should the text layout be cached?
      */
     final private boolean cached;
     
 	/**
 	 * Create a new Java2D label.	 
 	 */
 	public Java2DLabel(Java2DGameWindow window,
             int x, int y,
             EnumSet<Alignment> alignment,
             Color color,
             int opacity,
             float size,
             String text,
             boolean visible,
             boolean cached)
 	{                		
         // Setup the values.
 		this.window = window;
         this.x  = x;
         this.x_ = x;
         this.y  = y;
         this.y_ = y;        
         this.alignment = alignment;
         this.color = color;
         this.opacity = limitOpacity(opacity);
         this.size = size;
         this.text = text;
         this.visible = visible;
         this.cached = cached;
            
         // If the string is empty, then don't do anything.
         if (text.length() != 0)
         {
             // Get the font.
             this.font = Java2DFontStore.get().getFont((int) size);
 
             // Create the text layout object.
             this.baselineLayout = Java2DTextLayoutStore.get()
                     .getTextLayout(window.getDrawGraphics(), baselineText, font, true);
             this.textLayout = Java2DTextLayoutStore.get()
                     .getTextLayout(window.getDrawGraphics(), text, font, cached);            
 
             // Setup the alignment.  This should be replaced, alignment should
             // be immutable.                   
             this.offsetX = determineLabelOffsetX(alignment);
             this.offsetY = determineLabelOffsetY(alignment);
         }     
         else
         {
             this.font = null;
             this.baselineLayout = null;
             this.textLayout = null;            
         }
         
         // Set dirty so it will be drawn.        
         dirty = true;
 	}		           			           
         
     protected int determineLabelOffsetX(EnumSet<Alignment> alignment)
     {        
         Rectangle2D bounds = textLayout.getBounds();
         
      	if (alignment.contains(Alignment.LEFT))
 		{
 			return (int) -bounds.getMinX();
 		}
 		else if (alignment.contains(Alignment.CENTER))
 		{
 			return (int) -(bounds.getMinX() + bounds.getWidth() / 2);			
 		}
 		else if (alignment.contains(Alignment.RIGHT))
 		{
 			return (int) -bounds.getMaxX();
 		}
 		else
 		{
 			throw new IllegalStateException("No X alignment set!");
 		}        
     }   
         
     protected int determineLabelOffsetY(EnumSet<Alignment> alignment)
     {      
         Rectangle2D bounds = baselineLayout.getBounds();
         
 		if (alignment.contains(Alignment.BOTTOM))
 		{            
 			return 0;
 		}        
 		else if (alignment.contains(Alignment.MIDDLE))
 		{			
             return (int) (-bounds.getY() / 2f + 0.5);                        
 		}
 		else if (alignment.contains(Alignment.TOP))
 		{
 			return (int) (-bounds.getY() + 0.5);                                              
 		}
 		else
 		{
 			throw new IllegalStateException("No Y alignment set!");
 		}
     }	    
     
     private int getLetterHeight()
     {        
         // Return the height.
         return (int) -baselineLayout.getBounds().getY();            
     }
     
 	/**
 	 * Draw the text onto the graphics context provided.	   
 	 */
 	public boolean draw()
 	{
         // Do nothing if the string is empty.
         if (text.length() == 0) return false;            
         
         x_ = x + offsetX;
         y_ = y + offsetY;
         
         width_ = getWidth();
         height_ = getHeight();
         
 		// Return if the label is invisible.
 		if (isVisible() == false)
 			return false;                					
             
         // Get the graphics context.
         Graphics2D g = window.getDrawGraphics();
         
         // Deal with opacity.
         Composite c = null;        
         if (opacity != 100)
         {
             c = g.getComposite();
             g.setComposite(
                     AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                     ((float) opacity) / 100.0f));
         }
 
         g.setColor(Color.BLACK);
 
         textLayout.draw(g, x + offsetX + 1, y + offsetY + 1);            
 
         g.setColor(this.color);		
 
         textLayout.draw(g, x + offsetX, y + offsetY);            
 
         // Opacity.
         if (opacity != 100)        
             g.setComposite(c);  
         
         return true;
 	}        
 
     /**
      * Returns the width of the label.
      * 
      * @return The width of the label.
      */    
     @Override
     public int getWidth()
     {   
         if (text.length() == 0) return 0;
         else return (int) textLayout.getBounds().getMaxX();         
     }
 
     /**
      * This method is not supported.  Do not use.
      * 
      * @param width
      */    
     @Override
     public void setWidth(int width)
     {
         // This is not supported.        
         throw new UnsupportedOperationException("Not supported.");
     }
 
     /**
      * Returns the height of the label.
      * 
      * @return The height of the label.
      */    
     @Override
     public int getHeight()
     {    
         if (text.length() == 0) return 0;
         else return (int) baselineLayout.getBounds().getHeight();
     }
 
     /**
      * This method is not supported.  Do not use.
      * 
      * @param height
      */    
     @Override
     public void setHeight(int height)
     {
         // This is not supported.        
         throw new UnsupportedOperationException(
                 "Height should be changed via the size and text attribute.");
     }
           
     @Override
     public Rectangle getDrawRect()
     {
         // Return an empty (null) draw-rect if the string is length 0.
         if (text.length() == 0)
             return null;
         
         // If the draw rect is null, generate it.
         if (drawRect == null)
         {            
             // Draw a rectangle that can fully cover the text.
             Rectangle rect = new Rectangle(x, y, getWidth() + 2, getHeight() + 2);      
             
             // Move it so it covers the text.
             rect.translate(offsetX, offsetY);
             
             // Add the old rectangle.
             rect.add(new Rectangle(x_, y_, width_ + 2, height_ + 2));                             
                                      
             // Move the point up to the top left corner.
             rect.translate(0, -(getLetterHeight() + 1));
         
             drawRect = rect;
         }
                
         return drawRect;
     }
        
     @Override
     public void resetDrawRect()
     {
         // Do nothing if the text is length 0.
         if (text.length() == 0)
             return;
         
         x_ = x;
         y_ = y;
         
         width_ = getWidth();
         height_ = getHeight();
     }
 
     public Color getColor()
     {
         return color;
     }
 
     public float getSize()
     {
         return size;
     }
 
     public String getText()
     {
         return text;
     }          
 
     @Override
     public void setRotation(double theta)
     {
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public double getRotation()
     {
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean isCached()
     {
         return cached;
     }        
     
 }

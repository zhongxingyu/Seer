 package com.calumgilchrist.ld23.tinyworld.core;
 
 import static playn.core.PlayN.graphics;
 
 import org.jbox2d.common.Vec2;
 
 import playn.core.CanvasImage;
 import playn.core.Color;
 import playn.core.Font;
 import playn.core.ImageLayer;
 import playn.core.TextFormat;
 import playn.core.TextFormat.Alignment;
 import playn.core.TextLayout;
 
 /**
  * Handle Text. Allow for creating layers and drawing new text 
  * per frame.
  * 
  * @author homelinen
  *
  */
 public class TextHandler {
 
 	private TextFormat textFormat;
 	private CanvasImage canv;
 	private ImageLayer iLayer;
 	private TextLayout textLayout;
 	private String message;
 	private Font textFont;
 	
 	private Vec2 pos;
 	
 	/**
 	 * Create a pre-formatted, white text in Courier 
 	 * message at 12 points
 	 * 
 	 * @param message
 	 * @param pos
 	 */
 	public TextHandler(String message, Vec2 pos) {
 		setText(message);
 		
 		textFont = graphics().createFont("Courier", Font.Style.PLAIN, 12);
 		textFormat = new TextFormat(textFont, 100, Alignment.LEFT, Color.rgb(255, 255,255), new TextFormat().effect);
 		textLayout = graphics().layoutText("" + message, textFormat);
 		
 		this.pos = pos;
 		
		canv = graphics().createImage((int) textLayout.width() + 50, (int) textLayout.height() + 50);
 		this.iLayer = graphics().createImageLayer(canv);
 	}
 	
 	/**
 	 * Create text with message, font and color
 	 * 
 	 * @param message
 	 * @param pos
 	 * @param textFont
 	 * @param color
 	 */
 	public TextHandler(String message, Vec2 pos, Font textFont, int color) {
 		setText(message);
 		
 		this.pos = pos;
 		
 		textFormat = new TextFormat(textFont, 100, Alignment.LEFT, color, new TextFormat().effect);
 		textLayout = graphics().layoutText("" + message, textFormat);
 		
		canv = graphics().createImage((int) textLayout.width() + 50, (int) textLayout.width() + 50);
 		this.iLayer = graphics().createImageLayer(canv);
 	}
 	
 	public void update() {
 		canv.canvas().clear();
 		textLayout = graphics().layoutText("" + message, textFormat);
 		
 		canv.canvas().drawText(textLayout, pos.x, pos.y);
 	}
 	
 	public void setText(String text) {
 		this.message = text;
 	}
 	
 	public Vec2 getPos() {
 		return pos;
 	}
 	
 	public void setPos(Vec2 pos) {
 		this.pos = pos;
 	}
 	
 	public ImageLayer getTextLayer() {
 		return iLayer;
 	}
 }

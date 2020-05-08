 package com.karolmajta.stp.views;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 
 import com.karolmajta.stp.views.View;
 
 public class FancyTextView extends View<String> {
 	public static final int SHOW = 0;
 	public static final int STAY = 1;
 	
 	private static final float  VALPHA = 0.5f;
 	
 	private Integer lastTick = null;
 	private Integer deltaTime = null;
 	
 	private int mode;
 	private float x;
 	private float y;
 	private PFont font;
 	private int color;
 	private float alpha = 0;
 	
 	public FancyTextView(float x, float y, PFont font, int color,  int mode) {
 		this.mode = mode;
 		this.x = x;
 		this.y = y;
 		this.font = font;
 		this.color = color;
 	}
 	
 	@Override
 	protected void onDraw(PApplet p) {
 		int newTick = p.millis();
 		if(lastTick == null){
 			lastTick = newTick;
 		}
 		deltaTime = newTick - lastTick;
 		lastTick = newTick;
 		
 		switch(mode){
 			case SHOW:
 				updateAlpha();
 				show(p);
 				break;
 			case STAY:
 				show(p);
 				break;
 		}
 			
 	}
 
 	private void updateAlpha() {
 		alpha += VALPHA*deltaTime;
 	}
 
 	private void show(PApplet p) {
 		int prevFill = p.g.fillColor;
 		int prevAlign = p.g.textAlign;
 		
 		p.fill(p.red(color), p.green(color), p.blue(color), alpha);
 		p.textAlign(p.CENTER);
 		p.text(model, x, y);
 		
 		p.fill(prevFill);
		p.g.textAlign(prevAlign);
 	}
 
 }

 package com.discretesoftworks.framework;
 
 
 
 public class TextDisplay extends GameObject implements TextHolder{
 
 	private String text;
 	private RenderModel[] displayText;
 	private GameFont myFont;
 	private boolean hudElement;
 	private float size;
 	private int length;
 	private int hAlign;
 	
 	public TextDisplay(float x, float y, float z, GameFont myFont, int length, float size){
 		super(x,y,z,2,2,null);
 		this.myFont = myFont;
 		this.length = length;
 		this.size = size;
 		this.text = "";
 		this.hAlign = FA_LEFT;
 		hudElement = false;
 		displayText = new RenderModel[this.length];
 		for (int i = 0; i < this.length; i++){
 			displayText[i]  = GameRenderer.s_instance.getNewModel(getX(),getY(),1,1,null);
 			displayText[i].setZ(z);
 			displayText[i].setVisible(false);
 		}
 	}
 	
 	public void setLength(int length){
 		this.length = length;
 		displayText = new RenderModel[this.length];
 		for (int i = 0; i < this.length; i++){
 			displayText[i]  = GameRenderer.s_instance.getNewModel(getX(),getY(),1,1,null);
 			displayText[i].setVisible(false);
 		}
 		System.gc();
 	}
 	
 	public void setSize(int size){
 		this.size = size;
 		setText(text);
 	}
 	
 	public void setHAlign(int hAlign){
 		if (hAlign >0 && hAlign <= 3){
 			this.hAlign = hAlign;
 			setText(text);
 		}
 	}
 	
 	public void setHudElement(boolean h){
 		hudElement = h;
 		setText(text);
 	}
 	
 	@Override
 	public void setColor(float r, float g, float b, float a){
 		for (int i = 0; i < displayText.length; i++){
 			displayText[i].setColor(r,g,b,a);
 		}
 	}
 	
 	public int getTextWidth(){
 		int width = 0;
 		for (int i = 0; i < this.text.length(); i++){
 			if (text.charAt(i) == '\n'){
 			}
 			else {
 				displayText[i].setSprite(myFont.getLetter(text.charAt(i)));
 				float w = displayText[i].getSprite().getWidth();
 				float h = displayText[i].getSprite().getLength();
 				float scale = (float)size/h;
 				displayText[i].setWidth((int)(w*scale));
 				displayText[i].setLength((int)(h*scale));
 				width += displayText[i].getSprite().getMaskWidth()*scale;
 			}
 		}
 		return width;
 	}
 	
 	public void setText(String text){
 		if (text.length() > this.length){
 			text = text.substring(0,this.length);
 		}
 		this.text = text;
 		
 		float x = getX();
 		if (hAlign == FA_CENTER) {
 			x -= getTextWidth()/2;
 		} else if (hAlign == FA_RIGHT) {
 			x -= getTextWidth();
 		}
 		float y = getY();
 		for (int i = 0; i < displayText.length; i++){
 			displayText[i].setVisible(false);
 			displayText[i].setHudElement(hudElement);
 		}
 		for (int i = 0; i < this.text.length(); i++){
 			if (text.charAt(i) == '\n'){
 				y += size + 2;
 				x = getX();
 			}
 			else  {
 				displayText[i].setSprite(myFont.getLetter(text.charAt(i)));
 				float w = displayText[i].getSprite().getWidth();
 				float h = displayText[i].getSprite().getLength();
 				float scale = (float)size/h;
 				displayText[i].setWidth(w*scale);
 				displayText[i].setLength(h*scale);
 				displayText[i].createSquare(w*scale, h*scale);
 				displayText[i].setLeft(x);
 				displayText[i].setTop(y);
 				displayText[i].setZ(getZ());
 				displayText[i].setVisible(true);
 				x += displayText[i].getSprite().getMaskWidth()*scale;
 			}
 		}
 	}
 	
 	@Override
 	public void changeX(float dx){
 		for (int i = 0; i < displayText.length; i++){
 			displayText[i].changeX(dx);
 		}
 		super.changeX(dx);
 	}
 	
 	@Override
 	public void changeY(float dy){
 		for (int i = 0; i < displayText.length; i++){
 			displayText[i].changeY(dy);
 		}
 		super.changeY(dy);
 	}
 	
 	@Override
 	public void setX(float x){
 		changeX(x-getX());
 	}
 	
 	@Override
 	public void setY(float y){
 		changeY(y-getY());
 	}
 	
 	@Override
 	public void update(float deltaTime){
 		for (int i = 0; i < displayText.length; i++){
 			displayText[i].remakeModelMatrix();
 		}
 	}
 	
 	@Override
 	public String getText(){
 		return text;
 	}
 	
 	public void setDir(int xyz, float dir){
 		for (int i = 0; i < text.length(); i++){
			displayText[i].setFDir(xyz,dir);
 		}
 	}
 	
 	public void setVisible(boolean visible){
 		for (int i = 0; i < text.length(); i++){
 			displayText[i].setVisible(visible);
 		}
 		super.setVisible(visible);
 	}
 	
 	@Override
 	public void draw(float[] vpMatrix){
 		for (int i = 0; i < displayText.length; i++){
 			displayText[i].draw(vpMatrix);
 		}
 	}
 	
 }

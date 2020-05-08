 package prototype;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.util.ArrayList;
 
 import processing.core.PApplet;
import processing.core.PConstants;
 import processing.core.PImage;
 import processing.core.PMatrix3D;
 
 abstract public class BasicPart implements Part {
 	private Blueprint blueprint;
 	public Part parent;
 	ArrayList<Part> parts;
 
 	private SmartInt width;
 	private SmartInt height;
 	private SmartInt x;
 	private SmartInt y;
 	private SmartFloat relX;
 	private SmartFloat relY;
 	private SmartFloat scaleX;
 	private SmartFloat scaleY;
 	private SmartFloat pivotX;
 	private SmartFloat pivotY;
 	private SmartFloat rotation;
 	private SmartFloat alpha;
 
 	private float left;
 	private float top;
 	private float right;
 	private float bottom;
 	private int pivotModelX;
 	private int pivotModelY;
 
 	private boolean visible;
 	private boolean enabled;
 	private boolean showPivot;
 	private boolean interactble;
 
 	private float[] localMouse;
 	protected PMatrix3D localModel;
 
 	public BasicPart (Blueprint blueprint) {
 		this.initPart(blueprint);
 	}
 
 	public BasicPart (Blueprint blueprint, float x, float y) {
 		this.initPart(blueprint);
 		this.setX(x);
 		this.setY(y);
 	}	
 
 	abstract protected void initPart (Blueprint blueprint);
 
 	protected void basicSetup(Blueprint blueprint) {
 
 		if(blueprint.width == 0 || blueprint.height == 0) {
 			throw new RuntimeException("A Part's height or width cannnot be 0. Set the 'width' and 'height' in your your class constructor to fix this.");
 		}
 
 		this.localMouse = new float[] {0, 0, 0, 0};
 		this.localModel = new PMatrix3D();
 		this.parts = new ArrayList<Part>();
 		this.setBlueprint(blueprint);
 
 		this.width = new SmartInt(blueprint.width);
 		this.height = new SmartInt(blueprint.height);
 		this.x = new SmartInt(blueprint.x);
 		this.y = new SmartInt(blueprint.y);
 		this.relX = new SmartFloat(blueprint.relX, 0, 1);
 		this.relY = new SmartFloat(blueprint.relY, 0, 1);
 		this.scaleX = new SmartFloat(blueprint.scaleX);
 		this.scaleY = new SmartFloat(blueprint.scaleY);
 		this.pivotX = new SmartFloat(blueprint.pivotX, 0, 1);
 		this.pivotY = new SmartFloat(blueprint.pivotY, 0, 1);
 		this.rotation = new SmartFloat(blueprint.rotation);
 		this.alpha = new SmartFloat(blueprint.alpha, 0, 1);
 
 		this.visible(blueprint.visible);
 		this.enabled(blueprint.enabled);
 		this.showPivot(blueprint.showPivot);
 		this.interactble(blueprint.intractable);
 	}
 
 	protected void calcBox() {
 		left = -getPivotX() * getWidth();
 		top = -getPivotY() * getHeight();
 		right =  left + getWidth();
 		bottom = top + getHeight();
 	}
 
 	public void draw() {
 		if(visible) {
 			float translateX = (parent == null) ? getX() : (int)(getX() + (parent.getWidth() * getRelX() +parent.left()));
 			float translateY = (parent == null) ? getY() : (int)(getY() + (parent.getHeight() * getRelY() +parent.top()));
 
 			Prototype.stage.pushStyle();
 			Prototype.stage.pushMatrix();
 			Prototype.stage.translate(translateX, translateY);
 			if(getRotation() != 0) {
 				Prototype.stage.rotate(PApplet.radians(getRotation()));
 			}
 			if(getScaleX() != 1 || getScaleY() != 1) {
 				Prototype.stage.scale(getScaleX(), getScaleY());
 			}
 			this.localModel = Utils.getCurrentModel().get();
 			drawPart();
 			
 			//TODO: Take this out of here.
 			if(showPivot()) {
 				pivotModelX = PApplet.round(Prototype.stage.modelX(left + getPivotX() * getWidth(), top + getPivotY() * getHeight(), 0));
 				pivotModelY = PApplet.round(Prototype.stage.modelY(left + getPivotX() * getWidth(), top + getPivotY() * getHeight(), 0));
 			}
 			
 			drawChildren();
 			Prototype.stage.popMatrix();
 			Prototype.stage.popStyle();
 		}
 	}
 
 	abstract protected void drawPart();
 
 	void drawChildren() {
 		for(int p=0; p < parts.size(); p++) {
 			Part part = parts.get(p);
 			part.draw();
 		}
 	}
 
 	public void drawPivot() {
 		if(visible()) {
 			if(showPivot()  && pivotModelX >= 0 && pivotModelX <= Prototype.stage.width &&
 			pivotModelY >= 0 && pivotModelY <= Prototype.stage.height) {
 				//fillPivot(Utils.getComplementar(Prototype.stage.get(pivotModelX+1, pivotModelY)));
 				Prototype.stage.image(Prototype.pivot, pivotModelX-5, pivotModelY-5);
 			}		
 			for(int p=0; p < parts.size(); p++) {
 				Part part = parts.get(p);
 				part.drawPivot();
 			}
 		}
 	}
 	
 	void fillPivot(int color) {
 		Prototype.pivot.loadPixels();
 		if(Prototype.pivot.pixels[61] != color) {
 			for(int p = 0; p < Prototype.pivotPixels.length; p++) {
 				Prototype.pivot.pixels[Prototype.pivotPixels[p]] = color;
 			}
 		}
 		Prototype.pivot.updatePixels();
 	}
 
 	public void pre() {
 		calcBox();
 		updateParts();
 		updateLocalMouse();
 	}
 
 	void updateLocalMouse() {
 		localMouse = Utils.localMouse(localModel);
 	}
 
 	void updateParts() {
 		for(int p=0; p<parts.size(); p++) {
 			Part part = parts.get(p);
 			part.pre();
 		}
 	}
 
 	abstract public boolean mouseInside();
 
 	public boolean boxCollide(int x, int y) {
 		calcBox();
 		return (
 				x > left &&
 				x < right &&
 				y > top &&
 				y < bottom
 		)
 		? true : false;
 	}
 
 	public boolean circleCollide(int x, int y) {
 		calcBox();
		float center_x = left + (right-left)/2;
		float center_y = top + (bottom-top)/2;
		float radius = (getWidth() > getHeight()) ? getWidth()/2 : getHeight()/2;		
 		return (x-center_x)*(x-center_x) + (y - center_y)*(y - center_y) < radius*radius;
 	}
 
 	public boolean pixelCollide(int x, int y, PImage pickerBuffer) {
 		if (boxCollide(x, y)) {
 			int offSetMouseX = x + Math.round(getWidth() * getPivotX());
 			int offSetMouseY = y + Math.round(getHeight() * getPivotY());
 			PImage buffer = pickerBuffer.get();
 			buffer.resize(getWidth() * (int)getScaleX(), getHeight() * (int)getScaleY());
 			buffer.loadPixels();
 			if (buffer.pixels[(int) PApplet.constrain( offSetMouseX + offSetMouseY * getWidth(), 0, buffer.pixels.length-1)] == 0x00000000) {
 				buffer.updatePixels();
 				return false;
 			}
 			buffer.updatePixels();
 			return true;    
 		}
 		return false;
 	}
 
 	public Part part(Blueprint blueprint) {
 		return addPart(blueprint, 0, 0);
 	}
 
 	public Part part(Blueprint blueprint, float x, float y) {
 		return addPart(blueprint, x, y);
 	}
 
 	protected Part addPart(Blueprint blueprint, float x, float y) {
 		Part newPart;
 		switch(blueprint.type) {
 		case Part.IMAGE:
 			newPart = new ImagePart(blueprint, x, y);
 			newPart.setParent(this);
 			break;
 		case Part.SHAPE:
 			newPart = new ShapePart(blueprint, x, y);
 			newPart.setParent(this);
 			break;
 		default:
 			throw new RuntimeException("The declared part type is not valid.");
 		}
 		parts.add(newPart);
 		return newPart;
 	}
 
 	public void mouseEvent(MouseEvent event) {
 		if(enabled && visible) {
 			for(int p=parts.size()-1; p >= 0; p--) {
 				Part part = parts.get(p);		
 				part.mouseEvent(event);		
 			}
 			if(interactble()) {
 				blueprint.partEvent(new PartEvent(this, event));
 			}
 		}
 	}
 
 	public void mouseEvent(MouseWheelEvent event) {
 		if(enabled && visible) {
 			for(int p=parts.size()-1; p >= 0; p--) {
 				Part part = parts.get(p);		
 				part.mouseEvent(event);		
 			}
 			if(interactble()) {
 				blueprint.partEvent(new PartEvent(this, event));
 			}
 		}
 	}
 
 	public boolean partEvent(MouseEvent event, float shiftX, float shiftY) {
 		if(enabled && visible) {
 			boolean found = false;
 			for(int p=parts.size()-1; p >= 0 ; p--) {
 				Part part = parts.get(p);	
 				found = part.partEvent(event, left+shiftX, top+shiftY);
 				if(found) {
 					break;
 				}
 			}
 			if(!found) {
 				if(mouseInside() && interactble()) {
 					updateLocalMouse();
 					blueprint.partEvent(new PartEvent(this, event, event.getID()-500));
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean partEvent(MouseWheelEvent event, float shiftX, float shiftY) {
 		if(enabled && visible) {
 			for(int p=parts.size()-1; p >= 0 ; p--) {
 				Part part = parts.get(p);		
 				if(part.partEvent(event, left+shiftX, top+shiftY)) {
 					part.getBlueprint().partEvent(new PartEvent(part, event, event.getID()-500));
 					break;
 				}
 			}
 			if(mouseInside() && interactble()) {
 				updateLocalMouse();
 				blueprint.partEvent(new PartEvent(this, event, event.getID()-500));
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public float widthToScale() {
 		return (float)this.getWidth() / (float)this.getBlueprint().width;
 	}
 	public float heightToScale() {
 		return (float)this.getHeight() / (float)this.getBlueprint().height;
 	}
 
 	public Blueprint getBlueprint() { return blueprint; }
 	public void setBlueprint(Blueprint blueprint) { this.blueprint = blueprint; }
 
 	public SmartInt width() { return width; }
 	public int getWidth() { return this.width.value(); }
 	public void setWidth(float value) { this.width.value(value); }
 
 	public SmartInt height() { return height; }
 	public int getHeight() { return this.height.value(); }
 	public void setHeight(float value) { this.height.value(value); }
 
 	public SmartInt x() { return x; }
 	public int getX() { return this.x.value(); }
 	public void setX(float value) { this.x.value(value); }
 
 	public SmartInt y() { return y; }
 	public int getY() { return this.y.value(); }
 	public void setY(float value) { this.y.value(value); }
 
 	public SmartFloat relX() { return relX; }
 	public float getRelX() { return this.relX.value(); }
 	public void setRelX(float value) { this.relX.value(value); }
 
 	public SmartFloat relY() { return relY; }
 	public float getRelY() { return this.relY.value(); }
 	public void setRelY(float value) { this.relY.value(value); }
 
 	public SmartFloat scaleX() { return scaleX; }
 	public float getScaleX() { return this.scaleX.value(); }
 	public void setScaleX(float value) { this.scaleX.value(value); }
 
 	public SmartFloat scaleY() { return scaleY; }
 	public float getScaleY() { return this.scaleY.value(); }
 	public void setScaleY(float value) { this.scaleY.value(value); }
 
 	public SmartFloat pivotX() { return pivotX; }
 	public float getPivotX() { return this.pivotX.value(); }
 	public void setPivotX(float value) { this.pivotX.value(value); }
 
 	public SmartFloat pivotY() { return pivotY; }
 	public float getPivotY() { return this.pivotY.value(); }
 	public void setPivotY(float value) { this.pivotY.value(value); }
 
 	public SmartFloat rotation() { return rotation; }
 	public float getRotation() { return this.rotation.value(); }
 	public void setRotation(float value) { this.rotation.value(value); }
 
 	public SmartFloat alpha() { return alpha; }
 	public float getAlpha() { return this.alpha.value(); }
 	public void setAlpha(float value) { this.alpha.value(value); }
 
 	public int localMouseX() { return (int)(localMouse[0]); }
 	public int localMouseY() { return (int)(localMouse[1]);	}
 
 	public int plocalMouseX() { return (int)(localMouse[2]);}
 	public int plocalMouseY() { return (int)(localMouse[3]);}
 
 	public boolean visible() { return this.visible; }
 	public void visible(boolean state) { this.visible = state; }
 
 	public boolean enabled() { return this.enabled; }
 	public void enabled(boolean state) { this.enabled = state; }
 
 	public boolean showPivot() { return this.showPivot; }
 	public void showPivot(boolean state) { this.showPivot = state; }
 	
 	public boolean interactble() { return this.interactble; }
 	public void interactble(boolean state) { this.interactble = state; }
 
 	public float left() { calcBox(); return left; }
 	public float top() { calcBox(); return top; }
 	public float right() { calcBox(); return right; }
 	public float bottom() { calcBox(); return bottom; }
 
 	public Part getParent() { return this.parent; }
 	public void setParent(Part parent) { this.parent = parent; }
 }

 package prototype;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import processing.core.PApplet;
 import processing.core.PConstants;
 import processing.core.PFont;
 import processing.core.PImage;
 import processing.core.PMatrix3D;
 import processing.core.PShape;
 
 public class Part implements PrototypeConstants, PartListener {	
 	private Part parent;
 	ArrayList<Part> parts;
 	Map<String, Behavior> behaviors;
 	ArrayList<PartListener> listeners;
 	private final int type;
 
 	private int initialWidth;
 	private int initialHeight;
 
 	//TODO (Change to protected)
 	public SmartInt width;
 	public SmartInt height;
 	public SmartInt x;
 	public SmartInt y;
 	public SmartFloat relX;
 	public SmartFloat relY;
 	public SmartFloat scaleX;
 	public SmartFloat scaleY;
 	public SmartFloat pivotX;
 	public SmartFloat pivotY;
 	public SmartFloat rotation;
 	public SmartFloat alpha;
 	float multipliedAlpha;
 
 	private Box boundingBox;
 	private Box boundingBoxWorld;
 	private Box scaleGrid;
 	private float pivotModelX;
 	private float pivotModelY;
 	private int collisionMethod;
 
 	private boolean visible;
 	private boolean enabled;
 	private boolean showPivot;
 
 	private float[] localMouse;
 	protected PMatrix3D localModel;
 
 	//Belongs to ImagePart only
 	public PImage[] texture;
 	private PShape partModel;
 	private ShapeRender[] dynamicTexture;
 
 	int gridStyle;
 	private float[][][] gridVertexs;
 
 	//TODO
 	//boolean passThrough;
 	private boolean alphaChanged;
 	private boolean updated;
 	private boolean scaled;
 	private boolean stateChanged;
 	
 	//Belongs to TextPart only
 	int color;
 	
 	//Part values REDO
 	private PartValue value;
 	private int state;
 	
 
 	//Properties, used for listeners. Change here if you want to extend Part.
 	public enum Field
 	{
 		WIDTH, HEIGHT, X, Y, RELX, RELY, SCALEX,
 		SCALEY,PIVOTX, PIVOTY, ROTATION, ALPHA,
 		VISIBLE, ENABLED, SHOWPIVOT, VALUE, STATE, NOVALUE;  
 	}
 
 	//Image Part
 	public Part (String texturePath, Behavior... behaviors) {
 		this.type = IMAGE;
 		this.texture = new PImage[1];
 		this.texture[0] = Prototype.loadTexture(texturePath);
 		this.initWithDefaultValues();
 		this.width(this.texture[0].width);
 		this.height(this.texture[0].height);
 		this.initialWidth = this.width();
 		this.initialHeight = this.height();
 		for(Behavior b : behaviors) {
 			if(!this.behaviors.containsKey(Utils.className(b))) {
 				b.initBehavior(this);
 				this.behaviors.put(Utils.className(b), b);
 			} else {
 				System.err.println("The behavior "+ Utils.className(b) +" was already added to this part.");
 			}
 
 		}
 	}
 
 	//Shape Part
 	public Part (int width, int height, ShapeRender imageRecipe, Behavior... behaviors) {
 		this.type = SHAPE;
 		this.dynamicTexture = new ShapeRender[1];
 		this.dynamicTexture[0] = imageRecipe;
 		this.dynamicTexture[0].parent = this;
 		this.initWithDefaultValues();
 		this.width(width);
 		this.height(height);
 		this.initialWidth = this.width();
 		this.initialHeight = this.height();
 		for(Behavior b : behaviors) {
 			if(!this.behaviors.containsKey(Utils.className(b))) {
 				b.initBehavior(this);
 				this.behaviors.put(Utils.className(b), b);
 			} else {
 				System.err.println("The behavior "+ Utils.className(b) +" was already added to this part.");
 			}
 
 		}
 	}
 	
 	//Text Shape Part
 	public Part(String text, int textColor, PFont textFont, Behavior... behaviors)  {
 		this.type = TEXT;
 		initWithDefaultValues();
 		this.color = textColor;
 		TextRender textRender = new TextRender(text, textFont);
 		textRender.parent = this;
 		this.width(textRender.width);
 		this.height(textRender.height);
 		this.initialWidth = this.width();
 		this.initialHeight = this.height();
 		this.dynamicTexture = new ShapeRender[1];
 		this.dynamicTexture[0] = textRender;
 		for(Behavior b : behaviors) {
 			if(!this.behaviors.containsKey(Utils.className(b))) {
 				b.initBehavior(this);
 				this.behaviors.put(Utils.className(b), b);
 			} else {
 				System.err.println("The behavior "+ Utils.className(b) +" was already added to this part.");
 			}
 
 		}
 	}
 	
 	public Part(String text, int textColor, PFont textFont, int width, Behavior... behaviors)  {
 		this.type = TEXT;
 		initWithDefaultValues();
 		this.color = textColor;
 		TextRender textRender = new TextRender(text, textFont, width);
 		textRender.parent = this;
 		this.width(textRender.width);
 		this.height(textRender.height);
 		this.initialWidth = this.width();
 		this.initialHeight = this.height();
 		this.dynamicTexture = new ShapeRender[1];
 		this.dynamicTexture[0] = textRender;
 		for(Behavior b : behaviors) {
 			if(!this.behaviors.containsKey(Utils.className(b))) {
 				b.initBehavior(this);
 				this.behaviors.put(Utils.className(b), b);
 			} else {
 				System.err.println("The behavior "+ Utils.className(b) +" was already added to this part.");
 			}
 
 		}
 	}
 
 	public Part(PartBuilder builder) {
 		this.type = builder.type();
 		initWithBuilderValues(builder);
 		//Error checking & individual part setup
 		switch (this.type) {
 		case SHAPE:
 			if(builder.width() == 0 || builder.height() == 0 || builder.dynamicStates() == null || builder.dynamicStates().length == 0) {
 				throw new RuntimeException( "Invalid shape part.");
 			}
			this.dynamicTexture = builder.dynamicStates();
 			break;
 		case TEXT:
 			if(builder.text() == "" || builder.font() == null) {
 				throw new RuntimeException( "Invalid text part.");
 			}
 			this.color = builder.color();
 			TextRender textRender;
 			if(builder.width() > 0) {
 				textRender = new TextRender(builder.text(), builder.font(), builder.width());
 			} else {
 				textRender = new TextRender(builder.text(), builder.font());
 			}
 			this.width(textRender.width);
 			this.height(textRender.height);
 			this.initialWidth = this.width();
 			this.initialHeight = this.height();
 			this.dynamicTexture = new ShapeRender[1];
 			this.dynamicTexture[0] = textRender;
 			break;
 		case IMAGE:
 			if(builder.states().length == 0 || builder.states() == null) {
 				throw new RuntimeException( "Invalid shape part.");
 			}
 			if(builder.scaleGrid() != null) {
 				this.scaleGrid = builder.scaleGrid();
 				//this.faces = new float[3][3][8];
 			}
 			this.texture = new PImage[builder.states().length];
 			for(int s = 0; s < builder.states().length; s++) {	
 				this.texture[s] = Prototype.loadTexture(builder.states()[s]);
 			}
 			this.width(this.texture[0].width);
 			this.height(this.texture[0].height);
 			this.initialWidth = this.width();
 			this.initialHeight = this.height();
 			break;
 		}
 	}
 
 	//Image child part
 	public Part part(String texturePath, Behavior... behaviors) {
 		Part newPart = new Part(texturePath, behaviors);
 		newPart.parent(this);
 		this.parts.add(newPart);
 		return newPart;
 	}
 
 	//Shape child part
 	public Part part(int width, int height, ShapeRender imageRecipe, Behavior... behaviors) {
 		Part newPart = new Part(width, height, imageRecipe, behaviors);
 		newPart.parent(this);
 		this.parts.add(newPart);
 		return newPart;
 	}
 
 	//Shape child part
 	public Part part(PartBuilder builder) {
 		Part newPart = new Part(builder);
 		newPart.parent(this);
 		this.parts.add(newPart);
 		return newPart;
 	}
 
 	//Text childPart
 	public Part part(String text, int textColor, PFont textFont, Behavior... behaviors)  {
 		Part newPart = new Part(text, textColor, textFont, behaviors);
 		newPart.parent(this);
 		parts.add(newPart);
 		return newPart;
 	}
 	public Part part(String text, int textColor, PFont textFont, int width, Behavior... behaviors)  {
 		Part newPart = new Part(text, textColor, textFont, width, behaviors);
 		newPart.parent(this);
 		parts.add(newPart);
 		return newPart;
 	}
 	
 	//Pre-Built child Part
 	public void part(Part newPart) {
 		newPart.parent(this);
 		parts.add(newPart);
 	}
 
 	public void initWithBuilderValues(PartBuilder builder) {
 		this.localMouse = new float[] {0, 0, 0, 0};
 		this.localModel = new PMatrix3D();
 		this.parts = new ArrayList<Part>();
 		listeners = new ArrayList<PartListener>();
 		this.boundingBox = new Box();
 		this.boundingBoxWorld = new Box();
 
 		this.behaviors = builder.behaviors();
 		initBuilderBehaviors();
 
 		this.width = new SmartInt(builder.width());
 		this.height = new SmartInt(builder.height());
 		this.initialWidth = this.width();
 		this.initialHeight = this.height();
 
 		this.x = new SmartInt(builder.x());
 		this.y = new SmartInt(builder.y());
 		this.relX = new SmartFloat(builder.relX(), 0, 1);
 		this.relY = new SmartFloat(builder.relY(), 0, 1);
 		this.scaleX = new SmartFloat(builder.scaleX());
 		this.scaleY = new SmartFloat(builder.scaleY());
 		this.pivotX = new SmartFloat(builder.pivotX(), 0, 1);
 		this.pivotY = new SmartFloat(builder.pivotY(), 0, 1);
 		this.rotation = new SmartFloat(builder.rotation());
 		this.alpha = new SmartFloat(builder.alpha(), 0, 1);
 		this.multipliedAlpha = alpha();
 
 		this.visible(builder.visible());
 		this.enabled(builder.enabled());
 		this.showPivot(builder.showPivot());
 
 		this.collisionMethod = builder.collisionMethod();	
 
 		this.behaviors = builder.behaviors();
 		updated = true;
 		scaled = true;
 		alphaChanged = true;
 		stateChanged = true;
 		state = 0;
 		
 		value = new PartValue();
 	}
 
 	private void initBuilderBehaviors() {
 		for (Behavior behavior : behaviors.values()) {
 			behavior.initBehavior(this);
 		}
 	}
 
 	protected void initWithDefaultValues() {
 		this.localMouse = new float[] {0, 0, 0, 0};
 		this.localModel = new PMatrix3D();
 		this.parts = new ArrayList<Part>();
 		listeners = new ArrayList<PartListener>();
 		this.behaviors = new HashMap<String, Behavior>();
 		this.boundingBox = new Box();
 		this.boundingBoxWorld = new Box();
 
 		this.width = new SmartInt();
 		this.height = new SmartInt();
 
 		this.x = new SmartInt();
 		this.y = new SmartInt();
 		this.relX = new SmartFloat(0, 0, 1);
 		this.relY = new SmartFloat(0, 0, 1);
 		this.scaleX = new SmartFloat(1);
 		this.scaleY = new SmartFloat(1);
 		this.pivotX = new SmartFloat(0, 0, 1);
 		this.pivotY = new SmartFloat(0, 0, 1);
 		this.rotation = new SmartFloat();
 		this.alpha = new SmartFloat(1, 0, 1);
 		this.multipliedAlpha = 1;
 
 		this.visible(true);
 		this.enabled(true);
 		this.showPivot(false);
 
 		this.collisionMethod = BOX;
 		updated = true;
 		scaled = true;
 		alphaChanged = true;
 		stateChanged = true;
 		state = 0;
 		
 		value = new PartValue();
 	}
 
 	private void calcBox() {
 		this.boundingBox.left = 0 - this.pivotX() * this.width();
 		this.boundingBox.top = 0 - this.pivotY() * this.height();
 		this.boundingBox.right =  this.boundingBox.left + this.width();
 		this.boundingBox.bottom = this.boundingBox.top + this.height();
 	}
 
 	private void calcBoxWorld() {
 		this.boundingBoxWorld.left = Prototype.stage.modelX(this.boundingBox.left, this.boundingBox.top, 0);
 		this.boundingBoxWorld.top = Prototype.stage.modelY(this.boundingBox.left, this.boundingBox.top, 0);
 		this.boundingBoxWorld.right = this.boundingBox.left + this.width();
 		this.boundingBoxWorld.bottom = this.boundingBox.top + this.height();
 	}
 
 	private void worldToLocal() {
 		float translateX = (this.parent == null) ? this.x() : (int)(this.x() + (this.parent.width() * this.relX() + this.parent.left()));
 		float translateY = (this.parent == null) ? this.y() : (int)(this.y() + (this.parent.height() * this.relY() + this.parent.top()));
 		Prototype.stage.pushMatrix();
 		Prototype.stage.translate(translateX, translateY);
 		if(this.rotation() != 0) {
 			Prototype.stage.rotate(PApplet.radians(this.rotation()));
 		}
 		if(this.scaleX() != 1 || this.scaleY() != 1) {
 			Prototype.stage.scale(this.scaleX(), this.scaleY());
 		}
 	}
 
 	private void localToWorld() {
 		Prototype.stage.popMatrix();
 	}
 
 	private void updateLocalModel() {
 		this.localModel = Utils.getCurrentModel().get();
 		if(this.showPivot()) {
 			pivotModelX = Prototype.stage.modelX(boundingBox.left + pivotX() * width(), boundingBox.top + pivotY() * height(), 0);
 			pivotModelY = Prototype.stage.modelY(boundingBox.left + pivotX() * width(), boundingBox.top + pivotY() * height(), 0);
 		}
 		calcBoxWorld();
 	}
 	
 	private void updateAlphaStack() {
 		//Alpha multiplication
 		if(parent != null) {
 			if(parent.parent != null) {
 				multipliedAlpha = this.alpha()*parent.multipliedAlpha;
 			} else {
 				multipliedAlpha = this.alpha()*parent.alpha();
 			}
 		}
 	}
 
 	public boolean onScreen() {
 		//TODO REFACTORY
 		return (
 			boundingBoxWorld.left <= Prototype.stage.width ||
 			boundingBoxWorld.top <= Prototype.stage.height ||
 			boundingBoxWorld.right >= 0 ||
 			boundingBoxWorld.bottom >= 0
 		);
 	}
 
 	public void draw(boolean parentUpdated, boolean parentScaled, boolean parentChangedAlpha) {
 		if(visible && onScreen()) {
 			if(parentScaled == true) { this.scaled = true; }
 			if(parentChangedAlpha == true) { this.alphaChanged = true; }
 			this.worldToLocal();
 			if(updated || parentUpdated) {
 				this.updateLocalModel();
 			}
 			if(updated || parentUpdated || alphaChanged) {
 				this.updateAlphaStack();
 			}
 			switch(this.type) {
 			case IMAGE:
 				drawImage();
 				break;
 			case SHAPE:
 				drawShape();
 				break;
 			case TEXT:
 				drawText();
 				break;
 			}
 			this.drawChildren();
 			this.localToWorld();
 			updated = false;
 			scaled = false;
 			alphaChanged = false;
 			stateChanged = false;
 		}
 	}
 
 	private void drawImage() {
 		Prototype.stage.tint(255, 255*alphaStack());
 		if(this.scaleGrid != null) {
 			scale9Grid(this.texture[safeState()], this.scaleGrid);
 		} else {
 			drawPlane(this.initialWidth, this.initialHeight, this.pivotX(), this.pivotY(), this.texture[safeState()]);
 		}
 	}
 
 	private void drawShape() {
 		Prototype.stage.pushMatrix();
 		Prototype.stage.translate(-this.width()*this.pivotX(), -this.height()*this.pivotY());
 		if(this.widthToScale() != 1 || this.heightToScale() != 1) {
 			Prototype.stage.scale(this.widthToScale(), this.heightToScale());
 		}
 		dynamicTexture[safeState()].draw();
 		Prototype.stage.popMatrix();
 	}
 	
 	private void drawText() {
 		Prototype.stage.pushMatrix();
 		Prototype.stage.translate(-this.width()*this.pivotX(), -this.height()*this.pivotY());
 		if(this.widthToScale() != 1 || this.heightToScale() != 1) {
 			Prototype.stage.scale(this.widthToScale(), this.heightToScale());
 		}
 		Prototype.stage.fill(color, 255*alphaStack());
 		dynamicTexture[safeState()].draw();
 		Prototype.stage.popMatrix();
 	}
 	
 	//Scale Grid functions using Vertex Buffer. Only on Processing 2.0
 	/*
 	void drawPlane(float width, float height, float pivotX, float pivotY, PImage texture) {
 		Prototype.stage.pushMatrix();
 		if(widthToScale() != 1 || heightToScale() != 1) {
 			Prototype.stage.scale(widthToScale(), heightToScale());
 		}
 		if(partModel == null || alphaChanged || stateChanged) {
 			Prototype.stage.mergeShapes(true);
 			partModel = Prototype.stage.beginRecord();
 			Prototype.stage.noStroke();
 			Prototype.stage.beginShape(PConstants.QUADS);
 			Prototype.stage.texture(texture);
 			Prototype.stage.vertex(-width*pivotX, -height*pivotY, 0, 0);
 			Prototype.stage.vertex(width*(1-pivotX), -height*pivotY, texture.width, 0);
 			Prototype.stage.vertex(width*(1-pivotX), height*(1-pivotY), texture.width, texture.height);
 			Prototype.stage.vertex(-width*pivotX, height*(1-pivotY), 0, texture.height);
 			Prototype.stage.endShape();
 			Prototype.stage.endRecord();
 		}
 		Prototype.stage.shape(partModel);
 		Prototype.stage.popMatrix();
 	}
 
 	void scale9Grid(PImage texture, Box grid) {
 		
 		//Creates models based on the scale grid and sends to the GPU (Happens only once);
 		if(scaled || alphaChanged) {
 			Prototype.stage.textureMode(PConstants.IMAGE);
 			if(grid.left == 0) {
 				
 				gridStyle = 0; // 1x3 grid;
 				gridVertexs = new float[][][] {
 					{{0, 0, 0, 0}, {width(), 0, texture.width, 0}},
 					{{0, grid.top, 0, grid.top}, {width(), grid.top, texture.width, grid.top}},
 					{{0, height()-grid.bottom, 0, texture.height-grid.bottom}, {width(), height()-grid.bottom, texture.width, texture.height-grid.bottom}},
 					{{0, height(), 0, texture.height}, {width(), height(), texture.width, texture.height}}
 				};
 				
 				Prototype.stage.mergeShapes(true);
 				partModel = Prototype.stage.beginRecord();
 				Prototype.stage.noStroke();
 				Prototype.stage.beginShape(PConstants.QUADS);
 				Prototype.stage.texture(texture);
 				int rows = gridVertexs.length-1;
 				int cols = gridVertexs[0].length-1;
 				for(int y = 0; y < rows; y++) {
 					for(int x = 0; x < cols; x++)  {
 						Prototype.stage.vertex(gridVertexs[y][x][0], gridVertexs[y][x][1], 0, gridVertexs[y][x][2], gridVertexs[y][x][3]);
 						Prototype.stage.vertex(gridVertexs[y][x+1][0], gridVertexs[y][x+1][1], 0, gridVertexs[y][x+1][2], gridVertexs[y][x+1][3]);
 						Prototype.stage.vertex(gridVertexs[y+1][x+1][0], gridVertexs[y+1][x+1][1], 0, gridVertexs[y+1][x+1][2], gridVertexs[y+1][x+1][3]);
 						Prototype.stage.vertex(gridVertexs[y+1][x][0], gridVertexs[y+1][x][1], 0, gridVertexs[y+1][x][2], gridVertexs[y+1][x][3]);
 					}
 				}
 				Prototype.stage.endShape();
 				Prototype.stage.endRecord();
 				
 
 			}
 			else if(grid.top == 0) {
 				gridStyle = 1;
 				gridVertexs = new float[][][] {
 					{{0, 0, 0, 0}, {grid.left, 0, grid.left, 0}, {width()-grid.right, 0, texture.width-grid.right, 0}, {width(), 0, texture.width, 0}},
 					{{0, height(), 0, texture.height}, {grid.left, height(), grid.left, texture.height}, {width()-grid.right, height(), texture.width-grid.right, texture.height}, {width(), height(), texture.width, texture.height}},			
 				};
 					
 				Prototype.stage.mergeShapes(true);
 				partModel = Prototype.stage.beginRecord();
 				Prototype.stage.noStroke();
 				Prototype.stage.beginShape(PConstants.QUADS);
 				Prototype.stage.texture(texture);
 				int rows = gridVertexs.length-1;
 				int cols = gridVertexs[0].length-1;
 				for(int y = 0; y < rows; y++) {
 					for(int x = 0; x < cols; x++)  {
 						Prototype.stage.vertex(gridVertexs[y][x][0], gridVertexs[y][x][1], 0, gridVertexs[y][x][2], gridVertexs[y][x][3]);
 						Prototype.stage.vertex(gridVertexs[y][x+1][0], gridVertexs[y][x+1][1], 0, gridVertexs[y][x+1][2], gridVertexs[y][x+1][3]);
 						Prototype.stage.vertex(gridVertexs[y+1][x+1][0], gridVertexs[y+1][x+1][1], 0, gridVertexs[y+1][x+1][2], gridVertexs[y+1][x+1][3]);
 						Prototype.stage.vertex(gridVertexs[y+1][x][0], gridVertexs[y+1][x][1], 0, gridVertexs[y+1][x][2], gridVertexs[y+1][x][3]);
 					}
 				}
 				Prototype.stage.endShape();
 				Prototype.stage.endRecord();
 			}
 			else {
 				gridStyle = 2; //3x3 grid;
 				gridVertexs = new float[][][] {
 					{{0, 0, 0, 0}, {grid.left, 0, grid.left, 0}, {width()-grid.right, 0, texture.width-grid.right, 0}, {width(), 0, texture.width, 0}},
 					{{0, grid.top, 0, grid.top}, {grid.left, grid.top, grid.left, grid.top}, {width()-grid.right, grid.top, texture.width-grid.right, grid.top}, {width(), grid.top, texture.width, grid.top}},
 					{{0, height()-grid.bottom, 0, texture.height-grid.bottom}, {grid.left, height()-grid.bottom, grid.left, texture.height-grid.bottom}, {width()-grid.right, height()-grid.bottom, texture.width-grid.right, texture.height-grid.bottom}, {width(), height()-grid.bottom, texture.width, texture.height-grid.bottom}},
 					{{0, height(), 0, texture.height}, {grid.left, height(), grid.left, texture.height}, {width()-grid.right, height(), texture.width-grid.right, texture.height}, {width(), height(), texture.width, texture.height}},			
 				};
 					
 				Prototype.stage.mergeShapes(true);
 				partModel = Prototype.stage.beginRecord();
 				Prototype.stage.noStroke();
 				Prototype.stage.beginShape(PConstants.QUADS);
 				Prototype.stage.texture(texture);
 				int rows = gridVertexs.length-1;
 				int cols = gridVertexs[0].length-1;
 				for(int y = 0; y < rows; y++) {
 					for(int x = 0; x < cols; x++)  {
 						Prototype.stage.vertex(gridVertexs[y][x][0], gridVertexs[y][x][1], 0, gridVertexs[y][x][2], gridVertexs[y][x][3]);
 						Prototype.stage.vertex(gridVertexs[y][x+1][0], gridVertexs[y][x+1][1], 0, gridVertexs[y][x+1][2], gridVertexs[y][x+1][3]);
 						Prototype.stage.vertex(gridVertexs[y+1][x+1][0], gridVertexs[y+1][x+1][1], 0, gridVertexs[y+1][x+1][2], gridVertexs[y+1][x+1][3]);
 						Prototype.stage.vertex(gridVertexs[y+1][x][0], gridVertexs[y+1][x][1], 0, gridVertexs[y+1][x][2], gridVertexs[y+1][x][3]);
 					}
 				}
 				Prototype.stage.endShape();
 				Prototype.stage.endRecord();
 			}
 			
 		}
 		//End of setting up the shapes;
 		Prototype.stage.pushMatrix();
 		Prototype.stage.translate(-width()*pivotX(), -height()*pivotY());
 		Prototype.stage.shape(partModel);
 		Prototype.stage.popMatrix();
 	}
 	*/
 	
 	
 	//Scale Grid functions using Immediate Mode. Works on any version of Processing.
 	//This functions ins more CPU intensive.
 	
 	void drawPlane(float width, float height, float pivotX, float pivotY, PImage texture) {
 		Prototype.stage.pushMatrix();
 		if(widthToScale() != 1 || heightToScale() != 1) {
 			Prototype.stage.scale(widthToScale(), heightToScale());
 		}
 		Prototype.stage.noStroke();
 		Prototype.stage.beginShape(PConstants.QUADS);
 		Prototype.stage.texture(texture);
 		Prototype.stage.vertex(-width*pivotX, -height*pivotY, 0, 0);
 		Prototype.stage.vertex(width*(1-pivotX), -height*pivotY, texture.width, 0);
 		Prototype.stage.vertex(width*(1-pivotX), height*(1-pivotY), texture.width, texture.height);
 		Prototype.stage.vertex(-width*pivotX, height*(1-pivotY), 0, texture.height);
 		Prototype.stage.endShape();
 		Prototype.stage.popMatrix();
 	}
 	
 	void scale9Grid(PImage texture, Box grid) {
 		
 		//Creates models based on the scale grid and sends to the GPU (Happens only once);
 		if(scaled) {
 			Prototype.stage.textureMode(PConstants.IMAGE);
 			if(grid.left == 0) {			
 				gridStyle = 0; // 1x3 grid;
 				gridVertexs = new float[][][] {
 					{{0, 0, 0, 0}, {width(), 0, texture.width, 0}},
 					{{0, grid.top, 0, grid.top}, {width(), grid.top, texture.width, grid.top}},
 					{{0, height()-grid.bottom, 0, texture.height-grid.bottom}, {width(), height()-grid.bottom, texture.width, texture.height-grid.bottom}},
 					{{0, height(), 0, texture.height}, {width(), height(), texture.width, texture.height}}
 				};
 			}
 			else if(grid.top == 0) {
 				gridStyle = 1;
 				gridVertexs = new float[][][] {
 					{{0, 0, 0, 0}, {grid.left, 0, grid.left, 0}, {width()-grid.right, 0, texture.width-grid.right, 0}, {width(), 0, texture.width, 0}},
 					{{0, height(), 0, texture.height}, {grid.left, height(), grid.left, texture.height}, {width()-grid.right, height(), texture.width-grid.right, texture.height}, {width(), height(), texture.width, texture.height}},			
 				};
 			}
 			else {
 				gridStyle = 2; //3x3 grid;
 				gridVertexs = new float[][][] {
 					{{0, 0, 0, 0}, {grid.left, 0, grid.left, 0}, {width()-grid.right, 0, texture.width-grid.right, 0}, {width(), 0, texture.width, 0}},
 					{{0, grid.top, 0, grid.top}, {grid.left, grid.top, grid.left, grid.top}, {width()-grid.right, grid.top, texture.width-grid.right, grid.top}, {width(), grid.top, texture.width, grid.top}},
 					{{0, height()-grid.bottom, 0, texture.height-grid.bottom}, {grid.left, height()-grid.bottom, grid.left, texture.height-grid.bottom}, {width()-grid.right, height()-grid.bottom, texture.width-grid.right, texture.height-grid.bottom}, {width(), height()-grid.bottom, texture.width, texture.height-grid.bottom}},
 					{{0, height(), 0, texture.height}, {grid.left, height(), grid.left, texture.height}, {width()-grid.right, height(), texture.width-grid.right, texture.height}, {width(), height(), texture.width, texture.height}},			
 				};
 			}
 			
 		}
 		//End of setting up the shapes;
 		Prototype.stage.pushMatrix();
 		Prototype.stage.translate(-width()*pivotX(), -height()*pivotY());
 		
 		Prototype.stage.noStroke();
 		Prototype.stage.beginShape(PConstants.QUADS);
 		Prototype.stage.texture(texture);
 		int rows = gridVertexs.length-1;
 		int cols = gridVertexs[0].length-1;
 		for(int y = 0; y < rows; y++) {
 			for(int x = 0; x < cols; x++)  {
 				Prototype.stage.vertex(gridVertexs[y][x][0], gridVertexs[y][x][1], 0, gridVertexs[y][x][2], gridVertexs[y][x][3]);
 				Prototype.stage.vertex(gridVertexs[y][x+1][0], gridVertexs[y][x+1][1], 0, gridVertexs[y][x+1][2], gridVertexs[y][x+1][3]);
 				Prototype.stage.vertex(gridVertexs[y+1][x+1][0], gridVertexs[y+1][x+1][1], 0, gridVertexs[y+1][x+1][2], gridVertexs[y+1][x+1][3]);
 				Prototype.stage.vertex(gridVertexs[y+1][x][0], gridVertexs[y+1][x][1], 0, gridVertexs[y+1][x][2], gridVertexs[y+1][x][3]);
 			}
 		}
 		Prototype.stage.endShape();
 		
 		Prototype.stage.popMatrix();
 	}
 	
 	//End
 	
 	
 	void drawChildren() {
 		for(int p=0; p < parts.size(); p++) {
 			Part part = parts.get(p);
 			part.draw(updated, scaled, alphaChanged);
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
 
 	public void pre(boolean parentUpdated) {
 		if(updated || parentUpdated) {
 			calcBox();
 		}
 		updateParts();
 		//updateLocalMouse();
 	}
 
 	void updateLocalMouse() {
 		localMouse = Utils.localMouse(localModel);
 	}
 
 	void updateParts() {
 		for(int p=0; p<parts.size(); p++) {
 			Part part = parts.get(p);
 			part.pre(updated);
 		}
 	}
 
 	public boolean mouseInside() {
 		switch(this.collisionMethod) {
 		case BOX:
 			return boxCollide(this.localMouseX(), this.localMouseY());
 		case CIRCLE:
 			return circleCollide(this.localMouseX(), this.localMouseY());
 		case PIXEL:
 			return pixelCollide(this.localMouseX(), this.localMouseY());
 		default:
 			System.err.println("Invalid Collision Method. Switching to Box Collision");
 			return boxCollide(localMouseX(), localMouseY());
 		}
 	}
 
 	public boolean boxCollide(int x, int y) {
 		calcBox();
 		return (
 				x > this.boundingBox.left &&
 				x < this.boundingBox.right &&
 				y > this.boundingBox.top &&
 				y < this.boundingBox.bottom
 		)
 		? true : false;
 	}
 
 	public boolean circleCollide(int x, int y) {
 		calcBox();
 		float center_x = this.boundingBox.left + boundingBox.width()/2;
 		float center_y = this.boundingBox.top + boundingBox.height()/2;
 		float radius = (this.width() > this.height()) ? this.width()/2 : this.height()/2;		
 		return (x-center_x)*(x-center_x) + (y - center_y)*(y - center_y) < radius*radius;
 	}
 
 	public boolean pixelCollide(int x, int y) {
 		if (boxCollide(x, y)) {
 			int offSetMouseX = x + Math.round(this.width() * this.pivotX());
 			int offSetMouseY = y + Math.round(this.height() * this.pivotY());
 			//this.diffuseMap.loadPixels();
 			if (this.texture[safeState()].pixels[PApplet.constrain( offSetMouseX + offSetMouseY * this.width(), 0, this.texture[safeState()].pixels.length-1)] == 0x00000000) {
 				this.texture[safeState()].updatePixels();
 				return false;
 			}
 			//this.diffuseMap.updatePixels();
 			return true;    
 		}
 		return false;
 	}
 
 	public Behavior addBehavior(Behavior behavior) {
 		if(!this.behaviors.containsKey(Utils.className(behavior))) {
 			behavior.initBehavior(this);
 			this.behaviors.put(Utils.className(behavior), behavior);
 			return behavior;
 		} else {
 			System.err.println("The behavior "+ Utils.className(behavior) +" was already added to this part.");
 			return this.behaviors.get(Utils.className(behavior));
 		}
 	}
 
 	public void partUpdated(PartUpdateEvent event) {
 		switch(event.field) {
 		case WIDTH:
 			break;
 		case HEIGHT:
 			break;
 		case X:
 			break;
 		case Y:
 			break;
 		case RELX:
 			break;
 		case RELY:
 			break;
 		case SCALEX:
 			break;
 		case SCALEY:
 			break;
 		case PIVOTX:
 			break;
 		case PIVOTY:
 			break;
 		case ROTATION:
 			break;
 		case ALPHA:
 			break;
 		case VISIBLE:
 			break;
 		case ENABLED:
 			break;
 		case SHOWPIVOT:
 			break;
 		}
 	}
 
 	public void addListener(PartListener listener) {
 		listeners.add(listener);
 	}
 
 	protected void propagatePartUpdate(PartUpdateEvent event) {
 		//TODO Self listener?
 		this.partUpdated(event);
 		for(PartListener listener : listeners) {
 			listener.partUpdated(event);
 		}
 	}
 
 	public float widthToScale() {
 		return (float)this.width() / this.initialWidth;
 	}
 	public float heightToScale() {
 		return (float)this.height() / this.initialHeight;
 	}
 
 	public int width() { return this.width.value(); }
 	public void width(float value) {
 		scaled = true;
 		updated = true;
 		this.width.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.WIDTH) );
 	}
 
 	public int height() { return this.height.value(); }
 	public void height(float value) {
 		scaled = true;
 		updated = true;
 		this.height.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.HEIGHT) );
 	}
 
 	public int x() { return this.x.value(); }
 	public void x(float value) {
 		updated = true;
 		this.x.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.X) );
 	}
 
 	public int y() { return this.y.value(); }
 	public void y(float value) {
 		updated = true;
 		this.y.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.Y) );
 	}
 
 	public float relX() { return this.relX.value(); }
 	public void relX(float value) {
 		updated = true;
 		this.relX.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.RELX) );
 	}
 
 	public float relY() { return this.relY.value(); }
 	public void relY(float value) {
 		updated = true;
 		this.relY.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.RELY) );
 	}
 
 	public float scaleX() { return this.scaleX.value(); }
 	public void scaleX(float value) {
 		scaled = true;
 		updated = true;
 		this.scaleX.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.SCALEX) );
 	}
 
 	public float scaleY() { return this.scaleY.value(); }
 	public void scaleY(float value) {
 		scaled = true;
 		updated = true;
 		this.scaleY.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.SCALEY) );
 	}
 
 	public float pivotX() { return this.pivotX.value(); }
 	public void pivotX(float value) {
 		updated = true;
 		this.pivotX.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.PIVOTX) );
 	}
 
 	public float pivotY() { return this.pivotY.value(); }
 	public void pivotY(float value) {
 		updated = true;
 		this.pivotY.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.PIVOTY) );
 	}
 
 	public float rotation() { return this.rotation.value(); }
 	public void rotation(float value) {
 		updated = true;
 		this.rotation.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.ROTATION) );
 	}
 	
 	public float alphaStack() {
 		if(parent != null) {
 			return multipliedAlpha;
 		} else {
 			return alpha();
 		}
 	}
 	public float alpha() { return this.alpha.value(); }
 	public void alpha(float value) {
 		alphaChanged = true;
 		this.alpha.value(value);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.ALPHA) );
 	}
 	public void alpha(boolean value) {
 		alphaChanged = true;
 		this.alpha.value((value) ? 1: 0);
 		propagatePartUpdate( new PartUpdateEvent(this, Field.ALPHA) );
 	}
 
 	//REFACTORY
 	public int localMouseX() { updateLocalMouse(); return (int)(localMouse[0]); }
 	public int localMouseY() { updateLocalMouse(); return (int)(localMouse[1]);	}
 
 	public int plocalMouseX() { return (int)(localMouse[2]);}
 	public int plocalMouseY() { return (int)(localMouse[3]);}
 
 	public boolean visible() { return this.visible; }
 	public void visible(boolean state) {
 		this.visible = state;
 		propagatePartUpdate( new PartUpdateEvent(this, Field.VISIBLE) );
 	}
 
 	public boolean enabled() { return this.enabled; }
 	public void enabled(boolean state) {
 		this.enabled = state;
 		propagatePartUpdate( new PartUpdateEvent(this, Field.ENABLED) );
 	}
 
 	public boolean showPivot() { return this.showPivot; }
 	public void showPivot(boolean state) {
 		this.showPivot = state;
 		propagatePartUpdate( new PartUpdateEvent(this, Field.SHOWPIVOT) );
 	}
 	
 	public int safeState() { 
 		switch (this.type) {
 		case SHAPE:
 			return state % dynamicTexture.length;
 		case TEXT:
 			return state % dynamicTexture.length;
 		case IMAGE:
 			return state % texture.length;
 		}
 		throw new RuntimeException( "Invalid part type.");
 	}
 	public void state(int value) {
 		state = value;
 		stateChanged = true;
 		propagatePartUpdate( new PartUpdateEvent(this, Field.STATE) );
 	}
 	public int state() { return this.state; }
 
 	public float left() { calcBox(); return boundingBox.left; }
 	public float top() { calcBox(); return boundingBox.top; }
 	public float right() { calcBox(); return boundingBox.right; }
 	public float bottom() { calcBox(); return boundingBox.bottom; }
 
 	public Part parent() { return this.parent; }
 	protected void parent(Part parent) { this.parent = parent; }
 	
 	final public PartValue value() { return this.value; }
 	final public void value(boolean asBol) { this.value.value(asBol); propagatePartUpdate( new PartUpdateEvent(this, Field.VALUE) ); }
 	final public void value(float asFloat) { this.value.value(asFloat); propagatePartUpdate( new PartUpdateEvent(this, Field.VALUE) ); }
 	final public void value(String asString) { this.value.value(asString); propagatePartUpdate( new PartUpdateEvent(this, Field.VALUE) ); }
 }

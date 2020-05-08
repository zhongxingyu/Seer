 package org.anddev.andengine.memopuzzle.game;
 
 import java.util.LinkedList;
 
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.anddev.andengine.memopuzzle.MemoPuzzle;
 import org.anddev.andengine.memopuzzle.utils.Enviroment;
 import org.anddev.andengine.memopuzzle.utils.GameScene;
 import org.anddev.andengine.memopuzzle.utils.MyChangeableText;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import android.hardware.SensorManager;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public class SumBox extends GameScene {
 	private static final int SUP = 9;
 	private static final int INF = 1;
 	
 	private PhysicsWorld mPhysicsWorld;
 	private LinkedList<Integer> mListValue;
 	private LinkedList<Integer> mTempListValue;
 	private Integer sum;
 	private Texture tex;
	private TextureRegion texReg;
 	private Rectangle mBorder;
 	
 	public SumBox() {
 		super();
 		setBackground(new ColorBackground(1f, 1f, 1f));
 		
 		// dati
 		this.mListValue = new LinkedList<Integer>();
 		this.mTempListValue = new LinkedList<Integer>();
 		
     	// fisica
     	this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH + 30), false);
     	
         // base
     	final Rectangle ground = new Rectangle(0, MemoPuzzle.CAMERA_HEIGHT - 2, MemoPuzzle.CAMERA_WIDTH, 2);  		
     	final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f);
     	PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
     	getGameLayer().attachChild(ground);
         
        	// border for collisione, non aggiunto alla scena
        	this.mBorder = new Rectangle(-200, 100, 2, MemoPuzzle.CAMERA_HEIGHT);
        	
         // texture shared
     	this.tex  = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
    	this.texReg = TextureRegionFactory.createFromAsset(tex, this.mGame, "gfx/1.png", 0, 0);
     	
     	this.mGame.getEngine().getTextureManager().loadTexture(this.tex); // prende + text con la ,
 		
     	int num = 0;
     	switch (Enviroment.instance().getDifficult()) {
     		case 0: num = 4; break;
     		case 1: num = 5; break;
     		case 2: num = 6; break;
     	}
     	
         for (int pos = 1; pos <= num; pos++) {
         	int value = Enviroment.random(INF, SUP);
         	addBox(value, pos); 	
         }
     	
         // calc solution
     	int to_remove = Enviroment.random(1, num - 3);
     	for (int i = 1; i <= to_remove; i++) {
     		int index = Enviroment.random(0, this.mTempListValue.size() - 1);         	
     		this.mTempListValue.remove(index);
     	}
     	this.sum = new Integer(0);
     	for (int i = 0; i < this.mTempListValue.size(); i++) {
     		this.sum += this.mTempListValue.get(i);
     	}
     	
     	// label
     	final Font font = Enviroment.instance().getFont(2);
     	final Text sumText = new Text(90, 60, font, "Sum: " + this.sum.toString());		
     	getGameLayer().attachChild(sumText);
 		
         // phisic
         registerUpdateHandler(this.mPhysicsWorld);
         // touch listner
         setOnAreaTouchListener(this.mGame);
 	}
 	
 	private void addBox(int value, int pos) {
     	this.mListValue.add(new Integer(value));
     	this.mTempListValue.add(new Integer(value));
     	
		final Sprite box = new Sprite(185, - pos * 150, this.texReg);
 		final Font font = Enviroment.instance().getFont(1);
     	final MyChangeableText label = new MyChangeableText(32, 19, font, Integer.toString(value));
     	
     	// color
     	switch (value%3) {
     	case 0: 
     		box.setColor(1.0f, (float)value/SUP, (float)value/SUP);
     		break;
     	case 1: 
     		box.setColor((float)value/SUP, 1.0f, (float)value/SUP);
     		break;
     	case 2: 
     		box.setColor((float)value/SUP, (float)value/SUP, 1.0f);
     		break;
     	}
     	
     	final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f);
     	final Body bodyBox = PhysicsFactory.createBoxBody(this.mPhysicsWorld, box, BodyType.DynamicBody, objectFixtureDef); 	
     	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(box, bodyBox, true, false)); // false updata la rotazione
     	
     	registerTouchArea(box); // reg touch
     	
     	box.attachChild(label);
     	getGameLayer().attachChild(box);
     	
     	/* The actual collision-checking. */
 		registerUpdateHandler(new IUpdateHandler() {
 			public void reset() {}
 			
 			public void onUpdate(final float pSecondsElapsed) {
 				if (mBorder.collidesWith(box)) {
 					checkFinish();
 				}
 			}
 		});
 	}
 	
 	private void checkFinish() {
 		Integer temp_sum = new Integer(0);
     	for (int i = 0; i < this.mListValue.size(); i++) {
     		temp_sum += this.mListValue.get(i);
     	}   	
     	if (temp_sum < this.sum) {
     		this.mGame.nextScene();
     	} else if (temp_sum == this.sum) {
     		getScoreLayer().increaseStep(1);
     		this.mGame.nextScene();
     	}
 	}
 	
 	public void manageTouch(ITouchArea pTouchArea) {
 		final Sprite box = (Sprite)pTouchArea;
 		final Body bodyBox = this.mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(box);
 		
 		final Vector2 velocity = Vector2Pool.obtain(-50, 0);
 		bodyBox.setLinearVelocity(velocity);
 		Vector2Pool.recycle(velocity);
 		
 		Integer value = Integer.parseInt(((MyChangeableText)box.getFirstChild()).getText());
 		this.mListValue.remove(value);
 	}
 	
 }

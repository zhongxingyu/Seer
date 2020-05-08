 package org.anddev.andengine.memopuzzle.utils;
 
 import org.anddev.andengine.entity.layer.Layer;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 
 public class ScoreLayer extends Layer {
 	private String mStepLabel = "Step: ";
 	private int mStep;
 	private MyChangeableText mStepText;
 	private Texture tex;
 	private TextureRegion texReg;
 	
 	public ScoreLayer() {
 		final Font font = Enviroment.instance().getFont(2);
 		
 		this.mStep = 0;
		this.mStepText = new MyChangeableText(20, 20, font, this.mStepLabel + "0");
 		
 		attachChild(this.mStepText);
 		
 		this.tex  = new Texture(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	this.texReg = TextureRegionFactory.createFromAsset(this.tex, Enviroment.instance().getGame(), "gfx/back.png", 0, 0);
     	
     	Enviroment.instance().getGame().getEngine().getTextureManager().loadTexture(this.tex);
     	
     	final Sprite back = new Sprite(0, 0, this.texReg);
     	
     	attachChild(back);
 	}
 	
 	public void increaseStep(int value) {
 		this.mStep += value;
 		this.mStepText.setText(this.mStepLabel + Integer.toString(this.mStep));
 	}
 	
 }

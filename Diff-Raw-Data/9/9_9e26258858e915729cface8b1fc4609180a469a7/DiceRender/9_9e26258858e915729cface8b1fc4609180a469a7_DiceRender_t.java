 package com.plovergames.diceindark;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.util.Log;
 
 import com.plovergames.framework.gl.Animation;
 import com.plovergames.framework.gl.Camera2D;
 import com.plovergames.framework.gl.SpriteBatcher;
 import com.plovergames.framework.gl.TextureRegion;
 import com.plovergames.framework.impl.GLGraphics;
 
 public class DiceRender {
 
 	static final float FRUSTUM_WIDTH = 10;
 	static final float FRUSTUM_HEIGHT = 15;
 	GLGraphics glGraphics;
 	DiceScreen dicescreen;
 	Camera2D cam;
 	SpriteBatcher batcher;
 	List<List<TextureRegion>> diceTexture;
 	List<Animation> diceAnimation;
 
 	public DiceRender(GLGraphics glGraphics, SpriteBatcher batcher, DiceScreen ds){
 		this.glGraphics= glGraphics;
 		this.dicescreen=ds;
 		this.cam= new Camera2D(glGraphics, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
 		this.batcher=batcher;
 		diceTexture = new ArrayList<List<TextureRegion>>();
 		diceTexture.add(Assets.D4);
 		diceTexture.add(Assets.D6);
 		diceTexture.add(Assets.D8);
 		diceTexture.add(Assets.D10);
 		diceTexture.add(Assets.D12);
 		diceTexture.add(Assets.D20);
 
 		diceAnimation = new ArrayList<Animation>();
 		diceAnimation.add(Assets.D4_anim);
 		diceAnimation.add(Assets.D6_anim);
 		diceAnimation.add(Assets.D8_anim);
 		diceAnimation.add(Assets.D10_anim);
 		diceAnimation.add(Assets.D12_anim);
 		diceAnimation.add(Assets.D20_anim);
 
 
 	}
 
 	public void render(){
 		cam.setViewportAndMatrices();
 
 		renderDice();
 	}
 
 
 
 	public void renderDice() {
 
 		GL10 gl = glGraphics.getGL();
 		gl.glEnable(GL10.GL_BLEND);
 		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 
 		batcher.beginBatch(Assets.items);
 
 		Die die = dicescreen.dice.get(dicescreen.currentDie); 
 		int number = die.numberOfDice;
 
 		int scaler = (int) Math.ceil(Math.sqrt(number));
 		TextureRegion keyFrame;
 		int W=0;
 		if(dicescreen.currentDie<dicescreen.dice.size()-1)
 			switch(dicescreen.state){
 			case DiceScreen.DICE_READY:
 				//				 		   if(die.hasResult){
 				// 			   switch(number){
 				// 			   case 1: 
 				//
 
 				if(die.hasResult){
 					int [] result = new int[die.numberOfDice];
 					int j=0;
 					for(int i =0; i<die.sides;i++){
 
 
 						int k =die.result[i];
						while(k>0 & j<die.numberOfDice){
 							result[j]=i;
 							j++;
 							k--;
 
 						}
 
 					}
 					for(int i = 1; i<=number; i++){
 
 						if(number ==1) batcher.drawSprite(die.position.x+4, die.position.y+5, FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, diceTexture.get(dicescreen.currentDie).get(result[i-1]));
 						else if (number<= 4) batcher.drawSprite(die.position.x+2+W, die.position.y+2+(i%scaler)*FRUSTUM_HEIGHT/scaler, FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, diceTexture.get(dicescreen.currentDie).get(result[i-1]));
 						else batcher.drawSprite(die.position.x+W, die.position.y+(i%scaler)*FRUSTUM_HEIGHT/scaler, FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, diceTexture.get(dicescreen.currentDie).get(result[i-1]));
 
 						if(i%scaler ==0) W+=FRUSTUM_WIDTH/scaler;
 //						Log.v("Die result", ""+result[i-1]);
 					}
 
 				}
 				else{
 					for(int i = 1; i<=number; i++){
 
 						if(number ==1) batcher.drawSprite(die.position.x+4, die.position.y+5, FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, diceTexture.get(dicescreen.currentDie).get(0));
 						else if (number<= 4) batcher.drawSprite(die.position.x+2+W, die.position.y+2+(i%scaler)*FRUSTUM_HEIGHT/scaler, FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, diceTexture.get(dicescreen.currentDie).get(0));
 						else batcher.drawSprite(die.position.x+W, die.position.y+(i%scaler)*FRUSTUM_HEIGHT/scaler, FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, diceTexture.get(dicescreen.currentDie).get(0));
 
 						if(i%scaler ==0) W+=FRUSTUM_WIDTH/scaler;
 
 					}
 				}
 				break;
 
 			case DiceScreen.DICE_SHAKING:
 				for(int i = 1; i<=number; i++){
 					keyFrame = diceAnimation.get(dicescreen.currentDie).getKeyFrames(die.rand.nextFloat(), Animation.ANIMATION_LOOPING);		 
 					batcher.drawSprite(die.position.x+FRUSTUM_WIDTH*die.rand.nextFloat(), die.position.y+FRUSTUM_HEIGHT/scaler*die.rand.nextFloat(), FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, keyFrame);
 					//					batcher.drawSprite(die.position.x+4, die.position.y+FRUSTUM_HEIGHT*die.rand.nextFloat(),FRUSTUM_WIDTH,  FRUSTUM_HEIGHT,keyFrame);
 
 				}
 				break;
 			default:
 				for(int i = 1; i<=number; i++){
 					keyFrame = diceAnimation.get(dicescreen.currentDie).getKeyFrames(die.rand.nextFloat(), Animation.ANIMATION_LOOPING);
 					if(number ==1) batcher.drawSprite(die.position.x+4, die.position.y+5, FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, keyFrame);
 					else if (number<= 4) batcher.drawSprite(die.position.x+2+W, die.position.y+2+(i%scaler)*FRUSTUM_HEIGHT/scaler, FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, keyFrame);
 					else batcher.drawSprite(die.position.x+W, die.position.y+(i%scaler)*FRUSTUM_HEIGHT/scaler, FRUSTUM_WIDTH/scaler, FRUSTUM_HEIGHT/scaler, keyFrame);
 
 					if(i%scaler ==0) W+=FRUSTUM_WIDTH/scaler;
 //					Log.v("Die result", ""+die.result);
 				}
 				//				batcher.drawSprite(die.position.x+4, die.position.y+6,FRUSTUM_WIDTH,  FRUSTUM_HEIGHT,keyFrame);
 				break;
 
 			}
 		else{
 			TextureRegion keyFrame2 ;
 			switch(dicescreen.state){
 			case DiceScreen.DICE_READY:
 				int result =0;
 				if(die.hasResult){
 					int j=0;
 					while(result == 0){
 						if(die.result[j]>0) result=j;
 						j++;
 					}
 				}
 				if(die.hasResult && result<100){
 					batcher.drawSprite(die.position.x+4, die.position.y+9, FRUSTUM_WIDTH/2, FRUSTUM_HEIGHT/2, 
 							Assets.D100.get((int)Math.floor(result/10)));
 					if(result%10>0)
 						batcher.drawSprite(die.position.x+4, die.position.y+3, FRUSTUM_WIDTH/2, FRUSTUM_HEIGHT/2, 
 								Assets.D10.get((result)%10));
 					else
 						batcher.drawSprite(die.position.x+4, die.position.y+3, FRUSTUM_WIDTH/2, FRUSTUM_HEIGHT/2, 
 								Assets.D10.get(9));
 				}
 				else{
 					batcher.drawSprite(die.position.x+4, die.position.y+9,FRUSTUM_WIDTH/2,  FRUSTUM_HEIGHT/2, Assets.D100.get(0));
 					batcher.drawSprite(die.position.x+4, die.position.y+3,FRUSTUM_WIDTH/2,  FRUSTUM_HEIGHT/2, Assets.D10.get(0));
 				}
 				break;
 			case DiceScreen.DICE_SHAKING:
 				keyFrame2 = Assets.D10_anim.getKeyFrames(die.rand.nextFloat(), Animation.ANIMATION_LOOPING); 
 				keyFrame = Assets.D100_anim.getKeyFrames(die.rand.nextFloat(), Animation.ANIMATION_LOOPING);		 
 				batcher.drawSprite(die.position.x+4, die.position.y+FRUSTUM_HEIGHT*die.rand.nextFloat(),FRUSTUM_WIDTH/2,  FRUSTUM_HEIGHT/2,keyFrame);
 				batcher.drawSprite(die.position.x+4, die.position.y+FRUSTUM_HEIGHT*die.rand.nextFloat(),FRUSTUM_WIDTH/2,  FRUSTUM_HEIGHT/2,keyFrame2);
 				break;
 			default:
 				keyFrame2 = Assets.D10_anim.getKeyFrames(die.rand.nextFloat(), Animation.ANIMATION_LOOPING); 
 				keyFrame = Assets.D100_anim.getKeyFrames(die.rand.nextFloat(), Animation.ANIMATION_LOOPING);
 				batcher.drawSprite(die.position.x+4, die.position.y+9,FRUSTUM_WIDTH/2,  FRUSTUM_HEIGHT/2,keyFrame);
 				batcher.drawSprite(die.position.x+4, die.position.y+3,FRUSTUM_WIDTH/2,  FRUSTUM_HEIGHT/2,keyFrame2);
 
 				break;
 			}
 		}
 
 
 
 		batcher.endBatch();
 		gl.glDisable(GL10.GL_BLEND);
 	}
 
 
 
 }

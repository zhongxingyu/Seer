 package com.example.diceindark;
 
 import java.util.Random;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.util.Log;
 
 import com.example.framework.gl.Animation;
 import com.example.framework.gl.Camera2D;
 import com.example.framework.gl.SpriteBatcher;
 import com.example.framework.gl.TextureRegion;
 import com.example.framework.impl.GLGraphics;
 
 public class DiceRender {
 	
 	static final float FRUSTUM_WIDTH = 10;
 	static final float FRUSTUM_HEIGHT = 15;
 	GLGraphics glGraphics;
 	DiceScreen dice;
 	Camera2D cam;
 	SpriteBatcher batcher;
 	
 	int direction=0;
 	public DiceRender(GLGraphics glGraphics, SpriteBatcher batcher, DiceScreen dice){
 		this.glGraphics= glGraphics;
 		this.dice=dice;
 		this.cam= new Camera2D(glGraphics, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
 		this.batcher=batcher;
 
 	}
 
 	public void render(){
 		cam.setViewportAndMatrices();
 		
 		renderDice();
 	}
 	
 
 
     public void renderDice() {
 		 TextureRegion keyFrame;
         GL10 gl = glGraphics.getGL();
         gl.glEnable(GL10.GL_BLEND);
         gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
         
         batcher.beginBatch(Assets.items);
         Die die = dice.dice.get(dice.currentDie);
         switch(dice.state){
         case DiceScreen.DICE_READY:
         	direction=0;
		   if(die.hasResult && die.sides==4)
 			   batcher.drawSprite(die.position.x+4, die.position.y+7, FRUSTUM_WIDTH, FRUSTUM_HEIGHT, Assets.D4.get(die.result-1));
 		   else
 			   batcher.drawSprite(die.position.x+4, die.position.y+7,FRUSTUM_WIDTH,  FRUSTUM_HEIGHT, Assets.D4.get(0));
 		   break;
         case DiceScreen.DICE_SHAKING:
 			 keyFrame = Assets.D4_anim.getKeyFrames(dice.stateTime, Animation.ANIMATION_LOOPING);		 
 			 batcher.drawSprite(die.position.x+4, die.position.y+FRUSTUM_HEIGHT*die.rand.nextFloat(),FRUSTUM_WIDTH,  FRUSTUM_HEIGHT,keyFrame);
 			 break;
 		 default:
 			 keyFrame = Assets.D4_anim.getKeyFrames(dice.stateTime, Animation.ANIMATION_LOOPING);
 			 batcher.drawSprite(die.position.x+4, die.position.y+7,FRUSTUM_WIDTH,  FRUSTUM_HEIGHT,keyFrame);
 			 break;
 			   
         }
         /*switch(dice.currentDie){
         case 0:
         	renderD4();
         	break;
         case 1:
         	renderD6();
         	break;
         default: 		
         	break;
         	
         }*/
 
 
         batcher.endBatch();
         gl.glDisable(GL10.GL_BLEND);
     }
 
 
    private void renderD4(){
 	   switch(dice.state){
 	   case DiceScreen.DICE_READY:
 		   Die D4 = dice.dice.get(dice.currentDie);
 		   if(D4.hasResult){
 			   batcher.drawSprite(D4.position.x, D4.position.y, FRUSTUM_HEIGHT,FRUSTUM_WIDTH, Assets.D4.get(D4.result-1));
 		   }
 		   else
 			   batcher.drawSprite(D4.position.x, D4.position.y, Die.DIE_HEIGHT, Die.DIE_WIDTH, Assets.D4.get(0));
 	   }
    }
    
    private void renderD6(){
 	   
    }
 
 }

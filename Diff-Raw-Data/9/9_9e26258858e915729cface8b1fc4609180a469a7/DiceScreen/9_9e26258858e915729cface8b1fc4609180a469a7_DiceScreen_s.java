 /*    Dice in the dark. D & D app for the blind and seeing impaired,
  *    Copyright (C) <2013r>  <Lovisa Irpa Helgadottir>
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.plovergames.diceindark;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 
 import com.plovergames.framework.Game;
 import com.plovergames.framework.Input.GestureEvent;
 import com.plovergames.framework.gl.Camera2D;
 import com.plovergames.framework.gl.SpriteBatcher;
 import com.plovergames.framework.impl.GLScreen;
 
 
 public class DiceScreen extends GLScreen {
 
 	static final int DICE_READY =0;
 	static final int DICE_SHAKING=1;
 	static final int DICE_THROW =2;
 
 	Camera2D guiCam;
 	SpriteBatcher batcher;
 	DiceRender renderer;
 	List<Die> dice;
 	int state;
 	int currentDie =0;
 	float stateTime=0;
 	private boolean initializing = true;
 	public DiceScreen( Game game) {
 		super(game);
 		guiCam = new Camera2D(glGraphics, 320, 480);
 		batcher = new SpriteBatcher(glGraphics, 100);
 		this.state= DICE_READY;
 		dice = new ArrayList<Die>();
 		dice.add(new Die(4));
 		dice.add(new Die(6));
 		dice.add(new Die(8));
 		dice.add(new Die(10));
 		dice.add(new Die(12)); 
 		dice.add(new Die(20));
 		dice.add(new Die(100));
 		// TODO Auto-generated constructor stub
 		renderer = new DiceRender(glGraphics, batcher, this);
 	}
 
 	@Override
 	public void update(float deltaTime) {
 
 		if(initializing){
 			initializing = false;
 			game.getAudio().speakOut(""+dice.get(currentDie).name);
 		}
 
 		stateTime+=deltaTime;
 		float accelX = game.getInput().getAccelX();
 		float accelY = game.getInput().getAccelY();
 
 
 		switch(state){
 		case DICE_READY:
 			if(Math.abs(accelX)>10 ||Math.abs(accelY)>30){
 				dice.get(currentDie).shake();
 				state=DICE_SHAKING;
 				Assets.playSound(Assets.shakeDie);
 				return;
 			}
 			List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
 			int len = gestureEvents.size();
 			for(int i = 0; i<len; i++){
 				GestureEvent event = gestureEvents.get(i);  
 				if(event.type == GestureEvent.FLING_RIGHT){
 					dice.get(currentDie).numberOfDice=1;
 					currentDie--;
 					if(currentDie<0)
 						currentDie=dice.size()-1;
 					game.getAudio().speakOut(dice.get(currentDie).name);
 				}
 				if(event.type == GestureEvent.FLING_LEFT){
 					dice.get(currentDie).numberOfDice=1;
 					currentDie++;
 					if(currentDie>dice.size()-1)
 						currentDie=0;
 
 					game.getAudio().speakOut(dice.get(currentDie).name);
 
 				}
 				if(event.type == GestureEvent.DOUBLE_TAP){
 					game.setScreen(new MainMenuScreen(game));
 					return;
 				}
 				if(event.type == GestureEvent.SINGLE_TAP){
 					game.getAudio().speakOut(dice.get(currentDie).name);
 					if(dice.get(currentDie).numberOfDice>1)
 						game.getAudio().speakOut(dice.get(currentDie).name+"Number of Dice, "+dice.get(currentDie).numberOfDice);
 					if(dice.get(currentDie).hasResult)
 						speakResult();
 				
 				}
 				if(event.type == GestureEvent.SCROLL_UP){
 					if(dice.get(currentDie).sides!=100){
 						dice.get(currentDie).hasResult= false;
 						dice.get(currentDie).numberOfDice++;
 						game.getAudio().speakOut(""+dice.get(currentDie).numberOfDice+", "+dice.get(currentDie).name);
 					}
 				}
 				if(event.type == GestureEvent.SCROLL_DOWN){
 					if(dice.get(currentDie).sides!=100){
 						dice.get(currentDie).hasResult= false;
 						dice.get(currentDie).numberOfDice=(dice.get(currentDie).numberOfDice==1)?1:dice.get(currentDie).numberOfDice-1;
 						game.getAudio().speakOut(""+dice.get(currentDie).numberOfDice+", "+dice.get(currentDie).name);
 					}
 				}
 			}
 			return;
 
 		case DICE_SHAKING:
 			game.getInput().getGestureEvents();
 			if(Math.abs(accelX)>10 ||Math.abs(accelY)>10){
 				dice.get(currentDie).shake();
 				stateTime=0;
 
 			}
 
 			if(stateTime>.5f){
 				Assets.playSound(Assets.rollDie);
 				state = DICE_THROW;
 			}			
 			return;
 
 		case DICE_THROW:
 			game.getInput().getGestureEvents();
 			if (stateTime >2f){
 				dice.get(currentDie).thrown();
 				speakResult();
 				//game.getAudio().speakOut("Result, "+dice.get(currentDie).result);
 				state = DICE_READY;
 			}
 			return;
 		}
 
 	}
 
 	private void speakResult(){
 		String speak = "Result, ";
 		for(int i =0; i<dice.get(currentDie).sides;i++){
 			if(dice.get(currentDie).result[i]!=0)
 				if(dice.get(currentDie).numberOfDice>1) speak+=dice.get(currentDie).result[i]+" of "+(i+1)+", ";
 				else speak +=(i+1);
 
 		}
 		game.getAudio().speakOut(speak);
 	}
 
 	@Override
 	public void present(float deltaTime) {
 		GL10 gl = glGraphics.getGL();
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		gl.glEnable(GL10.GL_TEXTURE_2D);
 		renderer.render();
 
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 
 }

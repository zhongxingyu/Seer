 /*    Dice in the dark. D & D app for the blind and seeing impaired,
 *    Copyright (C) 2013  Lovisa Irpa Helgadottir
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
 
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 
 
 import com.plovergames.framework.Game;
 import com.plovergames.framework.Input.GestureEvent;
 import com.plovergames.framework.gl.Camera2D;
 import com.plovergames.framework.gl.SpriteBatcher;
 import com.plovergames.framework.impl.GLScreen;
 
 
 
 public class MainMenuScreen extends GLScreen {
 	static final int GAME_STARTING  =0;
 	static final int GAME_RUNNING =1;
 	static final int GAME_HELP =2;
 	static final int GAME_LEVEL_END =3;
 	
 	Camera2D guiCam;
 	SpriteBatcher batcher;
 	int state; 
 	public MainMenuScreen(Game game) {
 		super(game);
 		guiCam = new Camera2D(glGraphics, 320, 480);
 		batcher = new SpriteBatcher(glGraphics, 100);
 		state = GAME_STARTING;
 	}
 
 	@Override
 	public void update(float deltaTime) {
 
       try{  
         switch(state) {
         case GAME_STARTING:
         	welcomeMessage();
         	break;      	
         case GAME_RUNNING:
             updateGesture();
             break;
         case GAME_HELP:
             updateHelp();
             break;
 
 
         }
         }catch (Exception e) {
         	e.printStackTrace();
         }
 	}
 	
 	private void welcomeMessage(){
 		game.getAudio().speakOut("Welcome To, Dice In Dark. Double Tap for help");
 		state = GAME_RUNNING;
 	}
 	private void updateGesture(){
 		
 	List<GestureEvent> gestureEvents = game.getInput().getGestureEvents();
       game.getInput().getKeyEvents();
       int len = gestureEvents.size();
       for(int i = 0; i<len; i++){
     	  GestureEvent event = gestureEvents.get(i);  	
     	  if(event.type == GestureEvent.FLING_RIGHT || event.type == GestureEvent.FLING_LEFT)
     		  game.setScreen(new DiceScreen(game));
     	  if(event.type == GestureEvent.SINGLE_TAP){
     		  state = GAME_STARTING;
 	    	  return;
     	  }
     	  if(event.type == GestureEvent.DOUBLE_TAP){
     		  state =  GAME_HELP;   
     		  return;
     	 }
     		 	  
     	 }
 
     	  
         }
 
     private void updateHelp(){
   	  
      game.getAudio().speakOut("Fling right, or left to select die. Skrol up, or down to increase, or decrease number of dice. Shake to juggle die. " +
       		"Stop Shaking to roll die. Tap screen for repetition of result. Double tap for main menu ");
   	  state = GAME_RUNNING;
     }
     
 	@Override
 	public void present(float deltaTime) {
 		 	GL10 gl = glGraphics.getGL();        
 	        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	        guiCam.setViewportAndMatrices();
 	        
 	        gl.glEnable(GL10.GL_TEXTURE_2D);
 	        
 	        batcher.beginBatch(Assets.mainMenuScreen);
 	        batcher.drawSprite(160, 240, 320, 480, Assets.mainMenuScreenRegion);
 	        batcher.endBatch();
 	        
 	        
 	        gl.glDisable(GL10.GL_BLEND);
 	}
 
 	@Override
 	public void pause() {
 
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

 /**
  * Copyright (C) 2012 Emil Edholm, Emil Johansson, Johan Andersson, Johan Gustafsson
  *
  * This file is part of dat255-bearded-octo-lama
  *
  *  dat255-bearded-octo-lama is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  dat255-bearded-octo-lama is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with dat255-bearded-octo-lama.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package it.chalmers.dat255_bearded_octo_lama.games;
 
 import it.chalmers.dat255_bearded_octo_lama.R;
 import it.chalmers.dat255_bearded_octo_lama.games.anno.Game;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 
 @Game(name = "WhacAMole")
 public class WhacAMoleGame extends AbstractGameView {
 	
 	//Set all background color attributes.
 	private static final int ALPHA = 255;
 	private static final int RED   = 61;
 	private static final int GREEN = 245;
 	private static final int BLUE  = 0;
 	
 	private static final int NUMBER_OF_BUTTONS = 3;
 	
 	private List<Integer>  btnsToHit;
 	
 	public WhacAMoleGame(Context context) {
 		super(context);
 		
 		initUI();
 		initGame();
 	}
 
 	private void initGame() {		
 		//Decide what buttons that needs to be pressed to complete the game.
 		Random numberGen = new Random();
 		btnsToHit = new ArrayList<Integer>();
 		
 		for(int i = 0; i < NUMBER_OF_BUTTONS; i++) {
 			//Add 1 button on each row of the game.
 			btnsToHit.add(numberGen.nextInt(NUMBER_OF_BUTTONS) + (NUMBER_OF_BUTTONS * i) + 1);
 		}
 		
 		getPainter().setColor(getResources().getColor(R.color.red));
 		
 		//TESTCODE: This will only be used when testing the game to remove random elements.
 		btnsToHit.clear();
 		btnsToHit.add(1);
 		btnsToHit.add(2);
 		btnsToHit.add(3);
 	}
 	
 	
 	/**
 	 * Since the method setBackground for buttons is API Level 16 we are using
 	 * the older deprecated setBackgroundDrawable to make the buttons transparent.
 	 */
 	@SuppressWarnings("deprecation")
 	private void initUI() {
 		
 		//Initiate ui components and add them to the view.
 		LinearLayout uiLayout = new LinearLayout(getContext());
 		
 		uiLayout.setOrientation(LinearLayout.VERTICAL);
 		uiLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		uiLayout.setWeightSum(NUMBER_OF_BUTTONS);
 		
 		//Using nested LinearLayout's instead of a GridLayout to make it work properly on low API Levels.
 		LinearLayout horizontalLayout1 = new LinearLayout(getContext());
 		horizontalLayout1.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT
 				, android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1));
 		horizontalLayout1.setWeightSum(NUMBER_OF_BUTTONS);
 		LinearLayout horizontalLayout2 = new LinearLayout(getContext());
 		horizontalLayout2.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT
 				, android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1));
 		horizontalLayout2.setWeightSum(NUMBER_OF_BUTTONS);
 		LinearLayout horizontalLayout3 = new LinearLayout(getContext());
 		horizontalLayout3.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT
 				, android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1));
 		horizontalLayout3.setWeightSum(NUMBER_OF_BUTTONS);
 		
 		uiLayout.addView(horizontalLayout1);
 		uiLayout.addView(horizontalLayout2);
 		uiLayout.addView(horizontalLayout3);
 		
 		OnClickListener btnListener = new OnClickListener() {
 			
 			public void onClick(View v) {
 				onItemClick(v);
 			}
 		};
 		
 		//Here we add all the buttons that represent each colored square on the gameboard.
 		int numberOfRowsAndCols = 3;
 		int count = 1;
 		for(int y = 1; y <= numberOfRowsAndCols; y++) {
 			for(int x = 1; x <= numberOfRowsAndCols; x++) {
 				Button btn = new Button(getContext());
 				btn.setId(count);
 				btn.setOnClickListener(btnListener);
 				btn.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT
 						, android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1));
 				
 				//Using deprecated method instead of it's new equivalent since the new one requires API level 16.
 				btn.setBackgroundDrawable(null);
 				
 				//Check what row we are to place the button in.
 				switch (y) {
 				case 1:
 					horizontalLayout1.addView(btn);
 					break;
 				case 2:
 					horizontalLayout2.addView(btn);
 					break;
 				case 3:
 					horizontalLayout3.addView(btn);
 					break;
 				}
 				count++;
 			}
 		}
 		
 		getUiList().add(uiLayout);
 	}
 	
 	/**
 	 * Method for handling all button clicks in this game.
 	 * @param v The button which called the method.
 	 */
 	public void onItemClick(View v) {
 		btnsToHit.remove((Object)v.getId());
 	}
 
 	@Override
 	protected void updateGame() {
 		if(btnsToHit.isEmpty()) {
 			endGame();
 		}
 	}
 
 	@Override
	protected void updateGraphics(Canvas c) {
		List<Integer> redButtons = new ArrayList<Integer>(btnsToHit);
		
 		//Draw button graphics for the game.
 		
 		int currentWidth = getWidth()/NUMBER_OF_BUTTONS;
 		int currentHeight = getHeight()/NUMBER_OF_BUTTONS;
 		
 		//Paint background color
 		c.drawARGB(ALPHA, RED, GREEN, BLUE);
 		
		for(Integer i : redButtons) {
 			//Calculate row and column placement based on the button value.
 			int x = (i-1)%NUMBER_OF_BUTTONS;
 			int y = (i-1)/NUMBER_OF_BUTTONS;
 			c.drawRect(new Rect(currentWidth*x, currentHeight*(y), currentWidth*(x+1), currentHeight*(y+1)), getPainter());
 		}
 	}
 	
 }

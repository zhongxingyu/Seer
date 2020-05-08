 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.challenge;
 
import se.chalmers.dat255.sleepfighter.android_component.button.FontFitButton;
 import android.content.Context;
 import android.util.AttributeSet;
import android.widget.Button;
 
 /**
  * Represents a memory card graphically
  *
  * @author Eric Arneb�ck
  * @version 1.0
  * @since Sep 28, 2013
  */

 public class MemoryCardView extends FontFitButton {
 
 	// what to show when the memory card is flipped over and hidden.
   	private static String HIDDEN =  "";
   	// what to show when the memory card s flipped over and shown.
   	private String SHOWN;
   	
   	private int position;
   	
   	private String currentImage;
     
   	
     public MemoryCardView(Context context) {
         super(context);
     }
 
     public MemoryCardView(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 	
 	public void hide() {
 		currentImage = HIDDEN;
 		super.setText(currentImage);
 	}
 	
 	public void show() {
 		currentImage = SHOWN;
 		super.setText(currentImage);
 	}
 	
 	public boolean isHidden() {
 		return this.currentImage.equals(HIDDEN);
 	}
 	
 	public boolean isShown() {
 		return !isHidden();
 	}
 	
 	public void flip() {
 		if(isHidden()) {
 			this.show();
 		} else
 			this.hide();
 	}
 	
 	public void setImage(String image) {
 		//super.setBackgroundResource(android.R.color.white);
 
 		SHOWN = image;
 		
 		// all cards are hidden by default. 
 		hide();
 	}
 
 	public void setPosition(int position) {
 		this.position = position;
 	}
 	
 	public int getPosition() {
 		return this.position;
 	}
 }

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
 package com.plovergames.framework.impl;
 
 import java.util.List;
 
 import android.content.Context;
import android.os.Build.VERSION;
 import android.view.View;
 
 import com.plovergames.framework.Input;
 
 public class AndroidInput implements Input {    
     AccelerometerHandler accelHandler;
     TouchHandler touchHandler;
 
     public AndroidInput(Context context, View view, float scaleX, float scaleY) {
         accelHandler = new AccelerometerHandler(context);
           
         touchHandler = new GestureHandler(context, view);        
     }
 
     public boolean isKeyPressed(int keyCode) {
         return false;
     }
 
     public boolean isTouchDown(int pointer) {                         
         return touchHandler.isTouchDown(pointer);
     }
 
     public int getTouchX(int pointer) {
         return touchHandler.getTouchX(pointer);
     }
 
     public int getTouchY(int pointer) {
         return touchHandler.getTouchY(pointer);
     }
 
     public float getAccelX() {
         return accelHandler.getAccelX();
     }
 
     public float getAccelY() {
         return accelHandler.getAccelY();
     }
 
     public float getAccelZ() {
         return accelHandler.getAccelZ();
     }
 
     public List<TouchEvent> getTouchEvents() {
         return touchHandler.getTouchEvents();
     }
     
     public List<KeyEvent> getKeyEvents() {
         return null;
     }
 
 	public List<GestureEvent> getGestureEvents() {
 		// TODO Auto-generated method stub
 		return touchHandler.getGestureEvents();
 	}
 
 
 }

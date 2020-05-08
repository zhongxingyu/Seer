 /*
  *  BlinzEngine - A library for large 2D world simultions and games.
  *  Copyright (C) 2009  Blinz <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.input;
 
 import org.blinz.util.User;
 
 /**
  * An InputEvent generated with a mouse operation.
  * @author Blinz
  */
 public class MouseEvent extends InputEvent {
 
     private int buttonID;
     private int x, y;
 
     /**
      *
      * @param user the User associated with this InputEvent
      * @param buttonID the button operated
      * @param cursorX the x coordinate of the cursor at the time of the event
      * @param cursorY the y coordinate of the cursor at the time of the event
      */
     public MouseEvent(User user, int buttonID, int cursorX, int cursorY) {
         super(user);
         this.buttonID = buttonID;
     }
 
     /**
      * Returns the numerical identification of the mouse button.
      * @return the button associated with this event.
      */
     public final int getButton() {
         return buttonID;
     }
 
     /**
     *
      * @return the x coordinate of the cursor at the time of this event
      */
     public final int cursorX() {
         return x;
     }
 
     /**
     *
      * @return the y coordinate of the cursor at the time of this event
      */
     public final int cursorY() {
         return y;
     }
 }

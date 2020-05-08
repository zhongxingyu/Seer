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
  * An InputEvent generated with use of the mouse wheel.
  * @author Blinz
  */
 public class MouseWheelEvent extends InputEvent {
 
     private int ticks, cursorX, cursorY;
 
     /**
      *
      * @param user the user that generated this event
      * @param cursorX the x coordinate of the cursor at the time of this event
      * @param cursorY the y coordinate of the cursor at the time of this event
      * @param scrollTicks the number of ticks behind this event
      */
     public MouseWheelEvent(User user, int cursorX, int cursorY, int scrollTicks) {
         super(user);
         ticks = scrollTicks;
         this.cursorX = cursorX;
         this.cursorY = cursorY;
     }
 
     /**
     * Replace the number of scroll ticks in this event.
      * @return the number of scroll ticks in this event.
      */
     public final int getScrollTicks() {
         return ticks;
     }
 
     /**
     * Returns the x coordinate of the cursor at the time of this event
      * @return the x coordinate of the cursor at the time of this event
      */
     public final int cursorX() {
         return cursorX;
     }
 
     /**
     * Returns the y coordinate of the cursor at the time of this event
      * @return the y coordinate of the cursor at the time of this event
      */
     public final int cursorY() {
         return cursorY;
     }
 }

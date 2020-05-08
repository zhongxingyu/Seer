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
  * An InputEvent generated with a mouse click.
  * @author Blinz
  */
 public class ClickEvent extends MouseEvent {
 
     private int clickCount;
 
     /**
      * @param user the User associated with this InputEvent
      * @param buttonID the button operated
      * @param cursorX the x coordinate of the cursor at the time of the event
      * @param cursorY the y coordinate of the cursor at the time of the event
      */
     public ClickEvent(User user, int buttonID, int cursorX, int cursorY, int clickCount) {
         super(user, buttonID, cursorX, cursorY);
         this.clickCount = clickCount;
     }
 
     /**
     * Returns the number of clicks in this event.
      * @return the number of clicks behind this event.
      */
     public final int clickCount() {
         return clickCount;
     }
 }

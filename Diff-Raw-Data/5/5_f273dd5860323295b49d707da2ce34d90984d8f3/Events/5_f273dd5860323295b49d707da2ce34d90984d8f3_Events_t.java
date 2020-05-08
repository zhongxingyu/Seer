 /*******************************************************************************
  * Copyright (C) 2012 Steven Le Rouzic
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package com.stevenlr.minesweeper;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.LinkedList;
 import java.util.Queue;
 
 public class Events implements MouseListener, MouseMotionListener {
 	
 	public int x = 0;
 	public int y = 0;
 	
 	private Queue<MouseEvent> mouseEvents;
 	
 	public Events() {
 		mouseEvents = new LinkedList<MouseEvent>();
 	}
 	
 	public void mouseDragged(MouseEvent arg0) {
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		x = e.getX();
 		y = e.getY();
 	}
 
 	public void mouseClicked(MouseEvent e) {
 	}
 	
 	
 	public MouseEvent dequeueMouseEvent() {
 		return mouseEvents.poll();
 	}
 
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	public void mouseExited(MouseEvent arg0) {
 	}
 
	public void mousePressed(MouseEvent e) {
		mouseEvents.add(e);
 	}
 
 	public void mouseReleased(MouseEvent arg0) {
 	}
 }

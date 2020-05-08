 package de.codeforum.wedabecha.system.draw;
 /****************************************************************************
  *   Copyright (C) 2004 by BTU SWP GROUP 04/6.1                             *
  *                                                                          *
  *   This program is free software; you can redistribute it and/or modify   *
  *   it under the terms of the GNU General Public License as published by   *
  *   the Free Software Foundation; either version 2 of the License, or	    *
  *   (at your option) any later version                                     *
  *                                                                          *
  *   This program is distributed in the hope that it will be useful,        *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of         *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
  *   GNU General Public License for more details                            *
  *                                                                          *
  *   You should have received a copy of the GNU General Public License      *
  *   along with this program; if not, write to the                          *
  *   Free Software Foundation, Inc.,                                        *
  *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.              *
  ***************************************************************************/
 /**
 	@author
 		Matthias Tylkowski (micron at users.berlios.de)
 */
 
 import java.awt.*;
 import javax.swing.*;
 
 public class Grid extends JComponent {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private int breite;
 	private int hoehe;
 
	public void setGroesse(int breite, int hoehe){
 		this.breite = breite;
 		this.hoehe = hoehe;
 		this.setSize(breite, hoehe);
 	} // setGroesse()
 
	public void setVisibility(boolean sichtbar){
 		this.setVisible(sichtbar);
 	}// setVisibility();
 
 	public Grid(int breite, int hoehe){
 		this.hoehe = hoehe;
 		this.breite = breite;
 		this.setSize(breite, hoehe);
 		this.setVisible(false);
 	} // zeichneRaster()
 
 	public void paintComponent(Graphics raster){
 		// bestimmt den Abstand zwischen den Linien
 		int abstand = 12;
 		// zeichnet die senkrechten Linien im Abstand von 25 nach rechts
 		for(int i=0; i<this.breite;i+=abstand){
 		    // zeichnet die horizontalen Linien im Abstand von 25 nach oben
 		    for(int j=this.hoehe; j>0; j-=abstand){
 				raster.setColor(Color.LIGHT_GRAY);
 				// zeichnet eine Linie von unten nach oben
 				raster.drawLine(i, this.hoehe, i, 0);
 				// zeichnet eine Linie von links nach rechts
 				raster.drawLine(0, j, this.breite , j);
 		    } // for(j)
 		} // for(i)
 	} // paintComponent(Graphics raster)
 
 } // zeichneRaster

 package de.codeforum.wedabecha;
 /***************************************************************************
  *   Copyright (C) 2004 by BTU SWP GROUP 04/6.1                            *
  *                                                                         *
  *   This program is free software; you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation; either version 2 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  *   This program is distributed in the hope that it will be useful,       *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
  *   GNU General Public License for more details.                          *
  *                                                                         *
  *   You should have received a copy of the GNU General Public License     *
  *   along with this program; if not, write to the                         *
  *   Free Software Foundation, Inc.,                                       *
  *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
  ***************************************************************************/
 
 import java.util.Arrays;
 
import de.codeforum.wedabecha.ui.MainWindow;

 /**
  * 	@author Dominic Hopf (dmaphy at users.berlios.de)
  *  TODO: curve zu einer dynamisch anpassbaren Liste (Array) machen.
 *  
 *  Hauptklasse die als Programm aufgerufen wird.
 *  Das Programm soll fünf Kurven zeichnen können.
  */
 public class wedabecha {
	private static Curve curve[];
 	
 	/**
 	 * @param number Die Nummer der abzurufenden Kurve
 	 * @return Die Kurve
 	 */
	public static Curve getCurve(int number){
		return curve[number - 1];
 	} // getKurve()
 	
 	/**
 	 * @return Die ID der Kurve
 	 */
	public static int addCurve() {
		curve[curve.length] = new Curve(curve.length);
		return curve.length;
 	} // addCurve
 	
 	/**
 	 * TODO: Methode mit Funktion hinterlegen
 	 * @param id die ID der Kurve, die gelöscht werden soll.
 	 */
 	protected void removeCurve(int id) {
		//true;
 	}
 
 	// Konstruktor
 	/**
 	 * Konstruktor, erwartet keine weiteren Parameter
 	 */
 	public wedabecha(){
		new MainWindow(700, 500);
 	} // wedabecha()
 	
 	/**
 	 * Einstiegspunkt zum Starten des Programms.
 	 * @param args
 	 */
 	public static void main(String args[]){
 		// Einstiegspunkt für das Programm
 		new wedabecha();
 	} // main(String args[])
 } // wedabecha

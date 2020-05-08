 package de.codeforum.wedabecha.debug;
 /****************************************************************************
  *   Copyright (C) 2005 by BTU SWP GROUP 04/6.1                             *
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
 		Dominic Hopf (dmaphy at users.berlios.de),
 		Robert Exner (ashrak at users.berlios.de)
 
 	Debug
 	Testklasse zum auslesen der Daten,
 	kann evtl. in zeichneKurve übernommen und bearbeitet werden.
 */
 
 
 import java.util.ArrayList;
 
 import de.codeforum.wedabecha.wedabecha;
 
 
 class DReadData {
 	private ArrayList werte;
 	private ArrayList daten;
 
 	public DReadData(){
		this.werte = wedabecha.getKurve(1).getWerte();
		this.daten = wedabecha.getKurve(1).getDaten();
 		this.printWerte();
 		this.printDaten();
 	} // DReadData()
 
 	private void printWerte(){
 		// gibt die Werte aus
 
 		double statArray[];
 		String debug;
 		for (int i = 0; i < this.werte.size(); i++){
 			// zeile für zeile
 			statArray = (double[])this.werte.get(i);
 			debug = "";
 			for (int j = 0; j < statArray.length; j++){
 				// element für element einer zeile...
 				debug += statArray[j];
 			} // for(j)
 			System.out.println(debug);
 		} // for(i)
 
 	} // printWerte()
 
 	private void printDaten(){
 		String statArray[];
 		String debug;
 		for (int i = 0; i < this.daten.size(); i++){
 			statArray = (String[])this.daten.get(i);
 // 			System.out.println(statArray);
 			debug = "";
 			for (int j = 0; j < statArray.length; j++){
 				debug += statArray[j] + "-";
 			} // for(j)
 			System.out.println(debug);
 		} // for(i)
 	} // printDaten()
 } // DReadData

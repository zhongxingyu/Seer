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
 
 package de.codeforum.wedabecha.system;
 
 // Import von Dateien aus Java-Libs
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 // programminterne Imports
 import de.codeforum.wedabecha.wedabecha;
 import de.codeforum.wedabecha.ui.MainWindow;
 import de.codeforum.wedabecha.ui.dialogs.DataImportUI;
 import de.codeforum.wedabecha.system.DataImport;
 
 /**
  * @author
  * Martin Müller (mrtnmueller at users.berlios.de)
  * 
  * Diese Klasse dient zum Öffnen der .weda-Dateien
  */
 public class WedaFile {
 	
 	/**
 	 * Konstruktor, erwartet zur Zeit keine weiteren Parameter,
 	 * hat selber auch noch keine speziellen Aufgaben
 	 *
 	 */
 	public WedaFile () { /** Der Konstruktor hat noch keine speziellen Aufgaben. */
 
 	} // WedaFile ()
 	
 	
 	/**
 	 * Die Funktion zeigt öffnet .weda-Dateien
 	 * @param filename Der vollständige Dateiname (also inklusive Pfad) der .weda-Datei.
 	 * @return Eine zweidimensionale ArrayList mit den Daten.
 	 */
 	public void openFile (String filename) {
 
 		// Tabellennummer abfragen
 		String input = JOptionPane.showInputDialog("Bitte die darzustellende Kurvennummer angeben:" );
 
 		try{
 			//wandelt den Eingabestring in eine int-Zahl um
 			int kurvennummer = Integer.parseInt( input );
 
 			//Abfrage, ob eine gültige Tabellennummer eingegeben wurde
 			if (kurvennummer > 0 & kurvennummer < 6) {
 
 					JFileChooser auswahlDialog = new JFileChooser();
 
 					auswahlDialog.setFileFilter( new FileFilter() {
 						/** akzeptiert nur Dateien mit .weda als Endung */
 						@Override
 						public boolean accept( File f ) {
 							return 	f.isDirectory() ||
 									f.getName().toLowerCase().endsWith(".weda");
 						} // accept()
 
 						/** Beschreibung des Dateityps im Speichern-Dialog */
 						@Override
 						public String getDescription() {
 							return "WeDaBeCha Tabellendatei (*.weda)";
 						} // getDescription()
 
 					} ); //setFileFilter()
 
 			    	int returnVal = auswahlDialog.showOpenDialog(auswahlDialog);
 			    	if(returnVal == JFileChooser.APPROVE_OPTION){
 
						DataImport.setImportPfad(auswahlDialog.getSelectedFile().getPath());
						DataImport.setTrennzeichenIndex(0); //das Trennzeichen ist immer Semikolon
 						DataImport.setDatumsFormatIndex(0);//das Datumsformat ist immer YYYY-MM-DD
 						DataImport.setDatumsPosFirstColumn(true);//das Datum steht immer an erster Stelle
 
 						//Daten importieren
 						ArrayList resAL = DataImport.getWerte();
 						ArrayList ergebnis = DataImport.getDaten();
 
 						//Werte auf Eingelesene setzen
 						wedabecha.getCurve(kurvennummer).setValues(resAL);
 
 						//Daten auf Eingelesene setzen
 						wedabecha.getCurve(kurvennummer).setValues(ergebnis);
 
 						//Kurve auf "existent" setzten
 						wedabecha.getCurve(kurvennummer).setExists(true);
 
 						// Button zur jeweiligen eingelesenen Kurve anzeigen
 						MainWindow.getToolBar().kurveWaehlen(kurvennummer, true);
 						MainWindow.getMainMenuBar().setCurveEditable(kurvennummer, true);
 
 						//im Importdialog soll noch der Pfad erscheinen
 						DataImportUI.setPath(auswahlDialog.getSelectedFile().getPath(),kurvennummer);
 
 						int datenLaengen[] = new int[5];
 
 						for(int i = 1; i <= 5; i++){
 						    // alle importierten Tabellen als Kurve zeichnen
 							if(wedabecha.getCurve(i).isset()){
 								wedabecha.getCurve(i).draw();
 
 								datenLaengen[i] = wedabecha.getCurve(i).getDates().size();
 							} // if
 
 							java.util.Arrays.sort(datenLaengen);
 							MainWindow.setMaxDate(datenLaengen[4]);
 
 						} // for
 
 						// Koordinatensystem zeichnen
 						MainWindow.getCoords().draw();
 
 			    	}//if [JFileChooser.APPROVE_OPTION() ist nicht null]
 
 			} else {
 			   	JOptionPane.showMessageDialog( null, "Die Tabellennummer war falsch !" );
 			} // if
 
 
 		} catch(NumberFormatException error){
 			JOptionPane.showMessageDialog
 			(null,"Auswahl abgebrochen !","Fehler",JOptionPane.ERROR_MESSAGE );
 		} // try
 	} // openFile(String filename)
 	
 	public static void writeFile(String filename, int id) {
 		ArrayList werteAL;
 		ArrayList datenAL;
 
 		String zeile = new String("");
 		String subZeile = new String("");
 		double werteA[];
 		String datenA[];
 
 		//holt die Werte und Daten aus der übergebenen tabellennummer
 		//und speichert sie in die jeweilige ArrayList
 		werteAL = wedabecha.getCurve(id).getValues();
 		datenAL = wedabecha.getCurve(id).getDates();
 
 
 		//erzeugt eine leere Datei mit dem übergebenen Dateinamen
 		try {
 			FileWriter fw = new FileWriter( filename );
 			fw.write("");
 			fw.close();
 		}
 			//falls die Datei nicht geschrieben werden kann
 			//(z.b. auf CD speichern, schreibgeschützte Datei...)
 			catch (IOException except){
 			showWriteError(filename);
 		} // try
 
 		// zeilenweises Anhängen der Zeilen an die Datei
 		try {
 			// erzeugt neuen FileWriter fa; true bedeutet "anhängen=ja"
 			FileWriter fa = new FileWriter(filename, true);
 
 			// geht die ArrayList mit Daten solange durch, bis das Ende erreicht wurde
 			for (int i = 0; i < werteAL.size(); i++){
 
 				werteA = (double[])werteAL.get(i);
 				datenA = (String[])datenAL.get(i);
 
 				// gehe alle Werte einer Zeile durch
 				for (int j = 0; j < werteA.length; j++){
 						subZeile += werteA[j]; //subZeile den aktuellen Wert adden
 
 						// wenn das Ende noch nicht erreicht wurde setze ein Semikolon
 						if (j != werteA.length - 1 ) subZeile += ";";
 				} // for(j)
 
 				// fügt "zeile" das Datum dieser Tabellenzeile hinzu
  				zeile += datenA[0] + "-" + datenA[1] + "-" + datenA[2] + ";" + subZeile;
 
  				// wenn das Ende erreicht ist füge einen Zeilenumbruch ein
  				if (i != werteAL.size() - 1) zeile += "\n";
 
 				fa.write(zeile); // schreibt die Zeile
 				zeile = ""; // leeren von "zeile"
 				subZeile = ""; // leeren von "subZeile"
 			} // for(i)
 
 			fa.close(); //schließt die Datei
 
 		} // falls beim Anhängen etwas fehlschlägt
 			catch (IOException except){
 			showAppendError(filename);
 		} // try
 	} // writeFile(ArrayList data, String filename)
 	
 	protected static void showWriteError(String fileName){
 		JOptionPane.showMessageDialog(null,
 		"Datei" + fileName + "konnte nicht geschrieben werden.","Dateifehler",
 		JOptionPane.ERROR_MESSAGE );
 	} // showWriteError()
 
 
 	protected static void showAppendError(String fileName){
 		JOptionPane.showMessageDialog(null,
 		"Datei " + fileName + "konnte nicht verändert werden","Dateifehler",
 		JOptionPane.ERROR_MESSAGE );
 	} // showAppendError()
 
 
 	protected static void showFNFE(String fileName){
 		JOptionPane.showMessageDialog(null,
 		"Datei" + fileName + "konnte nicht gefunden werden.","Dateifehler",
 		JOptionPane.ERROR_MESSAGE );
 	} // showFNFE()
 } // class WedaFile
 
 class FileWriter extends OutputStreamWriter {
 	public FileWriter(String fileName) throws FileNotFoundException {
 			super(new FileOutputStream(fileName));
 	} // fileWriter
 
 	public FileWriter(String fileName, boolean append) throws FileNotFoundException {
 			super(new FileOutputStream(fileName, append));
 	}
 } // fileWriter

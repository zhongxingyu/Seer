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
 
 package de.codeforum.wedabecha.ui;
 
 
 // in dieser Klasse brauchen wir nur Swing, da sie
 // von der hauptFensterUI aus weiterverwendet wird
 
 import javax.swing.*;
 
 import de.codeforum.wedabecha.wedabecha;
 import de.codeforum.wedabecha.system.WedaFile;
 import de.codeforum.wedabecha.ui.dialogs.*;
 
 import java.awt.event.*;
 
 /**
  * @author
  * Dominic Hopf (dmaphy at users.berlios.de),
  * Robert Exner (ashrak at users.berlios.de)
  * 
  */
 public class MenuBar extends JMenuBar {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private boolean curveEditable[];
 	
 	/**
 	 * Konstruktor, erwartet keine weiteren Parameter
 	 */
 	public MenuBar(){
 		this.pack();
 	} // hauptMenuUI
 	
 	
 	/**
 	 * setzt die Menuleiste zusammen. Wird vom Konstruktor aufgerufen.
 	 */
 	public void pack(){
 		// begin fileMenu
 		
 		final JMenu fileMenu = new JMenu("Datei");
 		
 		// Items for fileMenu
 		final JMenuItem I_openFile = new JMenuItem("\u00D6ffnen");
 		I_openFile.addActionListener(new ActionListener() {
 			// für den MenuPunkt [Datei]->Öffnen
 			public void actionPerformed(ActionEvent event){
 				new WedaFile();
 			} // actionPerformed(ActionEvent event)
 		} ); // oeffnenListener
 		
 		final JMenuItem I_DataImport = new JMenuItem("Daten importieren");
 		I_DataImport.addActionListener(new ActionListener() {
 			// für den MenuPunkt [Datei]->[Tabelle importieren]
 			public void actionPerformed(ActionEvent event){
				new DataImportUI();
 			} // actionPerformed(ActionEvent event)
 		} ); // importiereTabelleListener
 		
 		final JMenuItem I_MergeFiles = new JMenuItem("Daten verkn\u00fcpfen");
 		I_MergeFiles.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				wedabecha.notImplementedError();
 			}
 		});
 		I_MergeFiles.setEnabled(false);
 		
 		final JMenuItem I_DataExport = new JMenuItem("Daten exportieren");
 		I_DataExport.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				new DataExport();
 			}
 		});
 		
 		final JMenuItem I_GraphicExport = new JMenuItem("Grafik exportieren");
 		I_GraphicExport.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
				new GraphicExportUI();
 			}
 		});
 		
 		final JMenuItem I_Print = new JMenuItem("Drucken");
 		I_Print.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				wedabecha.notImplementedError();
 			}
 		});
 		I_Print.setEnabled(false);
 		
 		final JMenuItem I_QuitProgram = new JMenuItem("Programm beenden");
 		I_QuitProgram.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				wedabecha.QuitProgram();
 			}
 		});
 		
 		fileMenu.add(I_openFile);
 		fileMenu.addSeparator();
 		fileMenu.add(I_DataImport);
 		fileMenu.add(I_MergeFiles);
 		fileMenu.add(I_DataExport);
 		fileMenu.addSeparator();
 		fileMenu.add(I_GraphicExport);
 		fileMenu.add(I_Print);
 		fileMenu.addSeparator();
 		fileMenu.add(I_QuitProgram);
 		this.add(fileMenu);
 		// fileMenu END
 
 		// curveMenu
 		final JMenu curveMenu = new JMenu("Kurve");
 		
 		for (int i = 1; i < this.curveEditable.length; i++) {
 			final int currentCurve = i;
 			
 			JMenu I_curveMenu = new JMenu("Kurve " + currentCurve);
 			
 			JMenuItem I_openCurve = new JMenuItem("Oeffnen");
 			I_openCurve.setEnabled(false);
 			I_curveMenu.add(I_openCurve);
 			
 			JMenuItem I_saveCurve = new JMenuItem("Speichern");
 			I_saveCurve.setEnabled(false);
 			I_curveMenu.add(I_saveCurve);
 			
 			JMenuItem I_curveGraph = new JMenuItem("Darstellung");
 			I_curveGraph.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					new CurveType(currentCurve);
 				}
 			});
 			I_curveMenu.add(I_curveGraph);
 			
 			JMenuItem I_curveRedraw = new JMenuItem("Neu Zeichnen");
 			I_curveRedraw.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent event){
 					// alle importierten Tabellen als Kurve zeichnen
 					if (wedabecha.getCurve(currentCurve).isset()) {
 						try {
 							MainWindow.layeredPane.remove(
 									MainWindow.layeredPane.getIndexOf (
 											wedabecha.getCurve(currentCurve).zeichneAktienKurve
 									)
 							);
 						} catch (ArrayIndexOutOfBoundsException except){
 							try {
 								MainWindow.layeredPane.remove(
 										MainWindow.layeredPane.getIndexOf (
 												wedabecha.getCurve(currentCurve).zeichneLinienKurve
 										)
 								);
 							} catch (ArrayIndexOutOfBoundsException exception) {
 								// mache von mir aus nix
 							} // try
 						} // try
 	
 						MainWindow.layeredPane.repaint();
 						
 						wedabecha.getCurve(currentCurve).zeichneKurve();
 						MainWindow.toolBar.setKurve1Button();
 					} // if
 
 					/*
 					java.util.Arrays.sort(datenLaengen);
 					MainWindow.maxDate = datenLaengen[4];
 					*/
 					
 					MainWindow.layeredPane.repaint();
 				} // actionPerformed()
 			} // ActionListener()
 			);
 
 			I_curveMenu.add(I_curveRedraw);
 				
 			curveMenu.add(I_curveMenu);			
 		} // for
 		// curveMenu END
 		
 		// annotationMenu
 		final JMenu annotationMenu = new JMenu("Annotation");
 		
 		final JMenuItem I_curveAnnotationArrow = new JMenuItem("Pfeil zeichnen");
 		I_curveAnnotationArrow.setEnabled(false);
 		annotationMenu.add(I_curveAnnotationArrow);
 		
 		final JMenuItem I_curveAnnotationLine = new JMenuItem("Linie zeichnen");
 		I_curveAnnotationLine.setEnabled(false);
 		annotationMenu.add(I_curveAnnotationLine);
 		
 		final JMenuItem I_curveAnnotationText = new JMenuItem("Text einf\u00fcgen");
 		I_curveAnnotationText.setEnabled(false);
 		annotationMenu.add(I_curveAnnotationText);
 		
 		this.add(annotationMenu);
 		// annotationMenu END
 	
 		// editMenu
 		final JMenu editMenu = new JMenu("Bearbeiten");
 		this.add(editMenu);
 		// editMenu END
 		
 		// viewMenu
 		final JMenu viewMenu = new JMenu("Ansicht");
 		
 		final JCheckBoxMenuItem I_showGrid = new JCheckBoxMenuItem("Gitter anzeigen");
 		I_showGrid.setEnabled(false);
 		viewMenu.add(I_showGrid);
 		
 		final JCheckBoxMenuItem I_showToolbar = new JCheckBoxMenuItem("Werkzeugleiste anzeigen");
 		I_showToolbar.setEnabled(false);
 		viewMenu.add(I_showToolbar);
 		// viewMenu END
 
 		final JMenu helpMenu = new JMenu("Hilfe");
 		final JMenuItem I_shortDoc = new JMenuItem("Kurzanleitung");
 		I_shortDoc.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				new Documentation("Kurzanleitung");
 			}
 		});
 		helpMenu.add(I_shortDoc);
 		final JMenuItem I_Doc = new JMenuItem("Dokumentation");
 		I_Doc.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				new Documentation("Dokumentation");
 			}
 		});
 		helpMenu.add(I_Doc);
 		helpMenu.addSeparator();
 		
 		final JMenuItem I_about = new JMenuItem("\u00dcber");
 		I_about.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event){
 				new About();
 			}
 		});
 
 	} // pack()
 	
 	/**
 	 * set a Curve in mode editable or not editable
 	 * @param id Die ID der Kurve, deren Status gesetzt werden soll.
 	 * @param flag true oder false für "bearbeitbar" oder "nicht bearbeitbar"
 	 */
 	public void setCurveEditable(int id, boolean flag){
 		/**
 			setKurveEditable setzt die einzelnen Menueinträge für eine Kurve
 			aktiv oder deaktiv... wird z.b. vom import bei klick auf ok
 			des subImportDialoges true gesetzt bzw. beim klick auf abbrechen false
 		*/
 		this.curveEditable[id] = flag;
 	}
 } // public class MenuBar

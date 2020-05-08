 package de.codeforum.wedabecha.ui.dialogs;
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
 		Dominic Hopf (dmaphy at users.berlios.de),
 		Robert Exner (ashrak at users.berlios.de),
 		Matthias Tylkowski (micron at users.berlios.de)
 
 	verändert die Darstellungseigenschaften der Kurve
 */
 
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.*;
 
 class CurveType extends JDialog {
 	final static long serialVersionUID = 1L;
 
 	// Die nummer der Tabelle bzw. Kurve
 	private int id;
 	
 	// Farbe, welche im JColorChooser ausgewählt wurde
 	private static Color color;
 	
     protected static Color getColor() {
         return color;
     }
   
     protected static void setColor(Color col) {
         color = col;
     }
     
     protected int getID() {
     	return this.id;
     }
     
     protected void setID(int number) {
     	this.id = number;
     }
     
 
    	// Konstruktor
     /**
      * @param number Tabellennummer für welche der Darstellungstype festgelegt werden soll.
      */
 	public CurveType(int number){
 		// erzeugen eines neuen Dialogs speziell für jede kurve...
 		this.setID(number);
 		System.out.println(number); // debug
 		this.pack();
 	}
 
 	/**
 	 * Setzt die Elemente des Dialogs zum Ganzen zusammen
 	 */
 	@Override
     public void pack(){
 		final JPanel topPanel = new JPanel( new GridLayout(2,2) );		
 		final JPanel topLeftPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT) );
 		final JPanel topRightPanel = new JPanel( new FlowLayout(FlowLayout.LEFT) );
 		
 		final JPanel bottomPanel = new JPanel( new FlowLayout() );
 		final JPanel bottomLeftPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT) );
 		final JPanel bottomRightPanel = new JPanel( new FlowLayout(FlowLayout.LEFT) );
 		
 		final JLabel styleLabel = new JLabel("Kurvenstil:"); //$NON-NLS-1$
 		final JComboBox styleComboBox = new JComboBox(Curve.getKurvenStile());
 
 		final JLabel colorLabel = new JLabel(Messages.getString("darstellungsTypUI.0")); //$NON-NLS-1$
 		final JButton colorButton = new JButton(Messages.getString("darstellungsTypUI.1")); //$NON-NLS-1$
 		colorButton.setBackground(getColor());
 		colorButton.addActionListener( new ActionListener(){
 			public void actionPerformed(ActionEvent event){
 				Color farbe = JColorChooser.showDialog (
 					/*
 						Farbe auswählen, und den Hintergrund des Buttons verändern
 					*/
 					null, Messages.getString("darstellungsTypUI.3"), colorButton.getBackground() //$NON-NLS-1$
                 ); 
 				colorButton.setBackground(farbe);
 				CurveType.setColor(farbe);
 			} // actionPerformed (ActionEvent event)
 		} );
 
 
 		final JButton okayButton = new JButton(Messages.getString("darstellungsTypUI.2")); //$NON-NLS-1$
 		okayButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event){
 				if (wedabecha.getCurve(getID()).isset()){
 					wedabecha.getCurve(getID()).setKurvenStilIndex(
 						styleComboBox.getSelectedIndex()
 					);
 
						wedabecha.getKurve(getTableNumber()).setFarbe(getColor());
 
 					setVisible(false);
 				} // if
 			} // actionPerformed()
 		});
 		
 		bottomPanel.add(okayButton);
 
 		topLeftPanel.add(styleLabel);
 		topRightPanel.add(styleComboBox);
 		
 		bottomLeftPanel.add(colorLabel);
 		bottomRightPanel.add(colorButton);
 		
 		topPanel.add(topLeftPanel);
 		topPanel.add(topRightPanel);
 		topPanel.add(bottomLeftPanel);
 		topPanel.add(bottomRightPanel);
 				
 		// zusammenpacken der ganzen Dialogbestandteile
 		this.getContentPane().setLayout(new FlowLayout());
 		this.getContentPane().add(topPanel);
 		this.getContentPane().add(bottomPanel);
 						
 		/*
 			standard zum erzeugen und positionieren des dialogs
 		*/
 		int bildSchirmBreite = getToolkit().getScreenSize().width;
 		int bildSchirmHoehe = getToolkit().getScreenSize().height;
 		int Xposition = (bildSchirmBreite - 500) / 2;
 		int Yposition = (bildSchirmHoehe - 150) / 2;
 		setSize(500,150);
 		setLocation(Xposition,Yposition);
 		setResizable(false);
 		setModal(true);
 		setTitle(Messages.getString("darstellungsTypUI.4"));  //$NON-NLS-1$
 		setVisible(true);
 
 	} // pack()
 
 } // darstellungsTypUI

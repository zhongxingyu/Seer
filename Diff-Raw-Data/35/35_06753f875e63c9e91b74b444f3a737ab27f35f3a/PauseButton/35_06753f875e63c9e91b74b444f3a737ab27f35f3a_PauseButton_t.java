 /*  	CASi Context Awareness Simulation Software
 *   Copyright (C) 2011 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.ui.simplegui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.logging.Logger;
 
 import javax.swing.JButton;
 
 import de.uniluebeck.imis.casi.simulation.engine.SimulationClock;
 
 /**
  * This class extends JButton and represents the pause button of the simulation
  * clock. It handles the ActionEvent and pause or resume the simulation depending
  * on the state of the simulation clock.
  * 
  * @author Moritz Buerger
  *
  */
 @SuppressWarnings("serial")
 public class PauseButton extends JButton implements ActionListener {
 	
 	private static final Logger log = Logger.getLogger(
 			PauseButton.class.getName());
 	
 	/**
 	 * Constructor of PauseButton sets preferred size and adds itself as ActionListener.
 	 */
 	public PauseButton() {
 		
 		this.setPreferredSize(new Dimension(30,30));
 		this.addActionListener(this);
 	}
 	
 	/**
 	 * Paints the pause button depending on the state od the simulation clock.
 	 */
 	public void paintComponent(Graphics g) {
 		
 		super.paintComponent(g);
 		g.setColor(Color.GRAY);
 		
 		/** Check, if clock is paused */
 		if(SimulationClock.getInstance().isPaused()) {
 			
 			/** Paint triangle (play)*/
 			int[] x = {6,6,26};
 			int[] y = {8,22,15};
 			g.fillPolygon(x,y,3);
 			g.setColor(Color.BLACK);
 			g.drawPolygon(x,y,3);
 			
 		} else {
 			
 			/** Paint two rectangles (pause)*/
 			g.fillRect(8, 5, 5, 20);
 			g.fillRect(16, 5, 5, 20);
 			g.setColor(Color.BLACK);
 			g.drawRect(8, 5, 5, 20);
 			g.drawRect(16, 5, 5, 20);
 		}
 		
 		
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 		/** If simulation is running */
 		if(!SimulationClock.getInstance().isPaused()) {
 			
 			log.info("Pause simulation");
 			
 			/** Set simulation clock paused */
 			SimulationClock.getInstance().setPaused(true);
 			
 			/** If simulation is paused */
 		} else if(SimulationClock.getInstance().isPaused()) {
 			
 			log.info("Resume simulation");
 			
 			/** Set simulation clock resumed */
 			SimulationClock.getInstance().setPaused(false);
 		
 		}			
 		
 		this.repaint();
 		
 		
 	}
 
 }

 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2011 2012  Moritz B�rger, Marvin Frick, Tobias Mende
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
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.util.logging.Logger;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.uniluebeck.imis.casi.simulation.engine.ISimulationClockListener;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationClock;
 import de.uniluebeck.imis.casi.simulation.model.SimulationTime;
 
 /**
  * This class extends JPanel and shows the simulation time in the simple GUI.
  * 
  * @author Moritz Buerger
  * 
  */
 @SuppressWarnings("serial")
 public class ClockViewPanel extends JPanel implements ISimulationClockListener,
 		ChangeListener {
 
 	private static final Logger log = Logger.getLogger(ClockViewPanel.class
 			.getName());
 
 	private JLabel timeLabel;
 	private JSlider slider;
 
 	public ClockViewPanel() {
 
 		/** Add the clock panel as listener on the simulation clock */
 		SimulationClock.getInstance().addListener(this);
 
 		/** Set layout to FlowLayout */
 		this.setLayout(new GridLayout(1, 0));
 
 		/** Set the components */
 		this.setComponents();
 
 		/** Set preferred size */
 		this.setPreferredSize(new Dimension(0, 65));
 	}
 
 	/**
 	 * This method sets the components of the ClockViewPanel (JLabel showing the
 	 * date and time, JSlider scaling the simulation time).
 	 */
 	private void setComponents() {
 
 		/** JLabel showing the simulation time */
 		this.timeLabel = new JLabel();
 		this.timeLabel.setFont(new Font("sans", Font.BOLD, 16));
 		this.timeLabel
 				.setBorder(BorderFactory.createTitledBorder("Date/Time:"));
 		this.add(this.timeLabel);
 
 		/** JSlider to set the speed */
 		this.slider = new JSlider(JSlider.HORIZONTAL,
 				SimulationClock.MINIMUM_SCALE_FACTOR,
 				SimulationClock.MAXIMUM_SCALE_FACTOR,
 				this.calculateScaledValue(SimulationClock.DEFAULT_SCALE_FACTOR));
 		this.slider.setMajorTickSpacing(200);
 		this.slider.setMinorTickSpacing(100);
 		this.slider.setSnapToTicks(true);
 		this.slider.setPaintTicks(true);
 		this.slider.addChangeListener(this);
 		this.slider.setBorder(BorderFactory.createTitledBorder("Time scaler:"));
 		this.add(this.slider);
 
 		/** Add control panel */
 		ControlPanel controlPanel = new ControlPanel();
 		this.add(controlPanel);
 	}
 
 	/**
 	 * This method gets a value between maximum and minimum and computes a
 	 * squared and scaled value between minimum and maximum.
 	 * 
 	 * @param value
 	 *            - value between maximum and minimum
 	 * @return squared value between minimum and maximum
 	 */
 	private int calculateScaledValue(int value) {
 
 		/** Save maximum and minimum in doubles */
 		double max = SimulationClock.MAXIMUM_SCALE_FACTOR;
 		double min = SimulationClock.MINIMUM_SCALE_FACTOR;
 
 		/** Compute values of the quadratic function */
 		double a = (max - min) / (Math.pow(max, 2) - Math.pow(min, 2));
 		double c = max - a * Math.pow(max, 2);
 
 		/** Invert the value limits */
 		double number = -value + max + min;
 
 		/** Square and scale the new value */
 		number = a * Math.pow(number, 2) + c;
 
 		/** Check, if the new values are in range */
 		if ((int) number < SimulationClock.MINIMUM_SCALE_FACTOR) {
 
 			/** Else return minimum */
 			return SimulationClock.MINIMUM_SCALE_FACTOR;
 		}
 		if ((int) number > SimulationClock.MAXIMUM_SCALE_FACTOR) {
 
 			/** Else return maximum */
 			return SimulationClock.MAXIMUM_SCALE_FACTOR;
 		}
 
 		/** Return the new value */
 		return (int) (number);
 	}
 
 	@Override
 	public void timeChanged(final SimulationTime newTime) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				/** Calculate the new scale factor */
 				double newScaleFactor = ClockViewPanel.this
 						.calculateScaledValue(ClockViewPanel.this.slider
 								.getValue());
 
 				double scaleFactor = 1000 / newScaleFactor;
 				scaleFactor = Math.floor(scaleFactor * 100) / 100;
 
 				ClockViewPanel.this.timeLabel.setText(newTime
 						.getLocalizedDate()
 						+ " - "
 						+ newTime.getLocalizedTime()
 						+ " ("
 						+ scaleFactor
 						+ "x)");
 
 			}
 		});
 
 	}
 
 	@Override
 	public void simulationPaused(final boolean pause) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				// Handle here!!!
 
 			}
 		});
 
 	}
 
 	@Override
 	public void simulationStopped() {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				// Handle here!!!
 
 			}
 		});
 
 	}
 
 	@Override
 	public void simulationStarted() {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				// Handle here!!!
 
 			}
 		});
 
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent arg0) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				/** Calculate the new scale factor */
 				int newScaleFactor = ClockViewPanel.this.slider.getValue();
 				newScaleFactor = ClockViewPanel.this
 						.calculateScaledValue(newScaleFactor);
 
 				/** Only change if the value has changed */
 				if (SimulationClock.getInstance().getScaleFactor() != newScaleFactor) {
 
 					/** Set new value to SimulationClock */
 					SimulationClock.getInstance()
 							.setScaleFactor(newScaleFactor);
 
 				}
 
 				ClockViewPanel.this.timeChanged(SimulationClock.getInstance()
 						.getCurrentTime());
 
 			}
 		});
 	}
 
 }

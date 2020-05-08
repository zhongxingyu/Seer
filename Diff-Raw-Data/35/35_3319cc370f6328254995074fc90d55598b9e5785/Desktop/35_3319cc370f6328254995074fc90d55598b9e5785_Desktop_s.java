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
 package de.uniluebeck.imis.casi.simulation.model.mateComponents;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.HashMap;
 
 import de.uniluebeck.imis.casi.communication.mack.MACKProtocolFactory;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationEngine;
 import de.uniluebeck.imis.casi.simulation.model.AbstractInteractionComponent;
 import de.uniluebeck.imis.casi.simulation.model.Agent;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.AbstractAction;
 
 /**
  * This class represents the desktop activity analyzer which is a sensor that is used in the MATe
  * implementation to get information about the agents state.
  * 
  * @author Tobias Mende
  * 
  */
 public class Desktop extends AbstractInteractionComponent {
 	/** Program classes that can be in foreground on this desktop */
 	public enum Program {
 		text, browser, unknown;
 	}
 	
 	/** The possible frequencies */
 	public enum Frequency {
 		active, inactive, very_active;
 	}
 	
 	private static int idCounter;
 	
 	private Frequency currentFrequency = Frequency.inactive;
 	private Program currentProgram = Program.unknown;
 	
 	/**
 	 * Constructor
 	 * @param coordinates
 	 *            the exact position of this daa.
 	 */
 	public Desktop(Point2D coordinates, Agent agent) {
 		super("daa-"+idCounter, coordinates);
 		type = Type.SENSOR;
 		this.agent = agent;
 		
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8750391465421352206L;
 
 
 	@Override
 	protected boolean handleInternal(AbstractAction action, Agent agent) {
		agent.removeVetoableListener(this);
 		return false;
 	}
 
 	@Override
 	public String getHumanReadableValue() {
 		return identifier+"[P: "+currentProgram+", F: "+currentFrequency+"]";
 	}
 	
 	/**
 	 * Starts working with the provided program and frequency
 	 * @param program
 	 * @param frequency
 	 */
 	public void work(Program program, Frequency frequency) {
 		this.currentFrequency = frequency;
 		this.currentProgram = program;
 		send();
 	}
 	
 	/**
 	 * Finally sends a message to the communication handler.
 	 */
 	private void send() {
 		HashMap<String, String> values = new HashMap<String, String>();
 		values.put("frequency", currentFrequency.toString());
 		values.put("program", currentProgram.toString());
 		String message = MACKProtocolFactory.generatePushMessage(agent, "daa", values);
 		SimulationEngine.getInstance().getCommunicationHandler().send(this, message);
 	}
 
 }

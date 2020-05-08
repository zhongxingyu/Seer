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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.uniluebeck.imis.casi.communication.mack.MACKProtocolFactory;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationEngine;
 import de.uniluebeck.imis.casi.simulation.model.AbstractInteractionComponent;
 import de.uniluebeck.imis.casi.simulation.model.Agent;
 import de.uniluebeck.imis.casi.simulation.model.Door;
 import de.uniluebeck.imis.casi.simulation.model.Door.State;
 import de.uniluebeck.imis.casi.simulation.model.IDoorListener;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.AbstractAction;
 
 /**
  * This sensor senses the state of a door. It's an implementation of the MATe component "DoorSensor".
  * @author Tobias Mende
  *
  */
 public class DoorSensor extends AbstractInteractionComponent implements
 		IDoorListener {
 	private static final long serialVersionUID = -7466985585534462471L;
 	private Door door;
 	/**
 	 * Creates a new door sensor for a provided door
 	 * @param door the door to attach the sensor to
 	 * @param agent the agent  which should be object of the push messages
 	 */
 	public DoorSensor(Door door, Agent agent) {
 		super(door.getCentralPoint());
 		setShapeRepresentation(door.getShapeRepresentation());
 		this.door = door;
 		this.agent = agent;
 		type = Type.SENSOR;
 		door.addListener(this);
 	}
 
 	@Override
 	public void stateChanged(State oldState, State newState) {
 		int value;
 		switch(newState) {
 		case OPEN:
 			value = 1;
 			break;
 		default:
 			value = 0;
 		}
 		// TODO check if doorsensor is correct as subject
 		Map<String,String> values = new HashMap<String, String>();
 		values.put("doorstate", Integer.toString(value));
 		String message = MACKProtocolFactory.generatePushMessage(agent, "doorsensor", values);
 		SimulationEngine.getInstance().getCommunicationHandler().send(this, message);
 	}
 
 
 	@Override
 	protected boolean handleInternal(AbstractAction action, Agent agent) {
		agent.removeVetoableListener(this);
		return true;
 	}
 
 	@Override
 	public String getHumanReadableValue() {
 		return door.getState().toString();
 	}
 
 }

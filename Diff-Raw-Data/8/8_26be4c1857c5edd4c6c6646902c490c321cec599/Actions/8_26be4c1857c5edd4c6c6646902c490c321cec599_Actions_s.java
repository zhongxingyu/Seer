 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.simulations.mate.generator.java;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import de.uniluebeck.imis.casi.generator.ActionCollector;
 import de.uniluebeck.imis.casi.generator.AgentCollector;
 import de.uniluebeck.imis.casi.generator.ComponentCollector;
 import de.uniluebeck.imis.casi.generator.RoomCollector;
 import de.uniluebeck.imis.casi.simulation.model.AbstractComponent;
 import de.uniluebeck.imis.casi.simulation.model.Agent;
 import de.uniluebeck.imis.casi.simulation.model.Room;
 import de.uniluebeck.imis.casi.simulation.model.SimulationTime;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.AbstractAction;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.AbstractAction.TYPE;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.ComplexAction;
 import de.uniluebeck.imis.casi.simulation.model.actions.CreateAMeeting;
 import de.uniluebeck.imis.casi.simulation.model.actions.GoAndSpeakTo;
 import de.uniluebeck.imis.casi.simulation.model.actions.GoAndStayThere;
 import de.uniluebeck.imis.casi.simulation.model.actions.Move;
 import de.uniluebeck.imis.casi.simulation.model.actions.StayHere;
 import de.uniluebeck.imis.casi.simulation.model.mackActions.GoAndWorkOnDesktop;
 import de.uniluebeck.imis.casi.simulation.model.mackActions.WorkOnDesktop;
 import de.uniluebeck.imis.casi.simulation.model.mackComponents.Desktop;
 
 /**
  * Action generator file with static methods that generate all the actions for
  * the MATe simulation environment World.
  * 
  * Put all your actions in here!
  * 
  * @author Marvin Frick
  * 
  */
 public final class Actions {
 
 	private static final Logger log = Logger.getLogger(WorldGenerator.class
 			.getName());
 
 	/**
 	 * Fills the ActionGenerator singleton object with all the actions.
 	 * 
 	 * Put all your actions here!
 	 */
 	public static void generateActions(SimulationTime simulationStartTime) {
 		RoomCollector roomC = RoomCollector.getInstance();
 		AgentCollector agentC = AgentCollector.getInstance();
 		ActionCollector actionC = ActionCollector.getInstance();
 		ComponentCollector componentC = ComponentCollector.getInstance();
 
 		// 09:00 - Agents are working on their desktops
 		for (Agent a : agentC.getAll()) {
 			AbstractComponent ac;
 			String ident = "Desktop-" + a.getIdentifier();
 
 			ac = componentC.findComponentByIdentifier(ident);
 			Desktop.Program randomProgramm = Desktop.Program.values()[(int) (Math
 					.random() * Desktop.Program.values().length)];
 			Desktop.Frequency randomFrequency = Desktop.Frequency.values()[(int) (Math
 					.random() * Desktop.Frequency.values().length)];
 			AbstractAction workOnDesktop = new GoAndWorkOnDesktop((Desktop) ac,
 					randomProgramm, randomFrequency, 5);
 
 			// the actionCollector key has to start with the Agents identifier!
 			// Otherwise is would not be added to his todoList automatically!
 			actionC.newAction(a.getIdentifier() + "action_" + ident + "_1",
 					workOnDesktop);
 
 			// also add this action to the agents actionPool
 			actionC.newAction(a.getIdentifier() + "action_" + ident + "_pool",
 					workOnDesktop.clone());
 		}
 
 		// 10:00 - Felix goes to the kitchen to drink a coffee
 		Agent felix = agentC.findAgentByName("Felix Freudentanz");
 		ComplexAction goToDrinkACoffee = new ComplexAction();
 		goToDrinkACoffee.setDeadline(simulationStartTime
 				.plus(60 * 60 + 10 * 60));
 		goToDrinkACoffee
 				.setEarliestStartTime(simulationStartTime.plus(60 * 60));
 		goToDrinkACoffee.addSubAction(new Move(roomC
 				.findRoomByIdentifier("kitchen")));
 		goToDrinkACoffee.addSubAction(new StayHere(5, 3));
 		goToDrinkACoffee.addSubAction(new Move(roomC
 				.findRoomByIdentifier("officeFelix")));
 		actionC.newAction(felix.getIdentifier()
 				+ "_goesToTheKitchenToDrinkCoffee", goToDrinkACoffee);
 
 		// 10:30 - Hermann creates a meeting for 12:00
 		// Meeting: takes 2 hours, every employee attends, is in Hermanns room
 		Agent hermann = agentC.findAgentByName("Hermann Matsumbishi");
 		AbstractAction createMeeting = new CreateAMeeting(
 				simulationStartTime.plus(60 * 60), hermann, AgentCollector
 						.getInstance().getAll(),
 				roomC.findRoomByIdentifier("officeHermann"),
 				simulationStartTime.plus(60 * 60 * 3), 120, 9);
 		actionC.newAction(hermann.getIdentifier()
 				+ "_createsThe12oclockMeeting", createMeeting);
 
 		// between 15:00 and 15:45: Felix visits Hermann for half an hour
 		AbstractAction goToHermann = new GoAndSpeakTo(hermann, 30);
 		// using real timestamps to parse here. SimulationsStartTime is:
 		// 04/23/2012 09:00:42
 		try {
 			goToHermann.setEarliestStartTime(new SimulationTime(
 					"04/23/2012 15:00:00"));
 			goToHermann.setEarliestStartTime(new SimulationTime(
 					"04/23/2012 15:45:00"));
 		} catch (ParseException e) {
 			log.warning("Could not parse timeString for simulationClock!");
 		}
 		actionC.newAction(
 				felix.getIdentifier() + "_goesToHermannForHalfAnHour",
 				goToHermann);
 
 		// till 15:30: Dagobert has to work exactly 42 very concentrated minutes
 		// in his text
 		// application
 		Agent dagobert = agentC.findAgentByName("Dagobert Dreieck");
 		AbstractAction dagobertHasToWork = new GoAndWorkOnDesktop(
 				(Desktop) componentC.findComponentByIdentifier("Desktop-"
 						+ dagobert.getIdentifier()), Desktop.Program.text,
 				Desktop.Frequency.very_active, 42);
 		try {
 			dagobertHasToWork.setDeadline(new SimulationTime(
 					"04/23/2012 15:30:23"));
 		} catch (ParseException e) {
 			log.warning("Could not parse timeString for simulationClock!");
 		}
 		actionC.newAction(dagobert.getIdentifier() + "_hasToWork",
 				dagobertHasToWork);
 
 		// 16:00 Felix geht zu Zwotah ins Büro, sie reden über Belangloses. Das
 		// Mike hört das. Der Würfel sollte jetzt erkennen, dass die beiden
 		// beschäftigt sind. Zwotah dreht seinen WÜrfel auf "nicht beschäftigt".
 		AbstractAction felixTalksWithZwotah = new GoAndSpeakTo(
 				agentC.findAgentByName("Zwotah Zwiebel"), 60);
 		felixTalksWithZwotah.setEarliestStartTime(simulationStartTime
 				.plus(60 * 60 * 7));
 		// they just talk about the weekend, nothing important.
 		felixTalksWithZwotah.setPriority(2);
 
 	}
 
 	/**
 	 * Generate all the action pools
 	 * 
 	 * @param startTime
 	 */
 	public static void generateActionsPools(SimulationTime startTime) {
 		// all agents have some ActionPool actions
 
 		// they need to go to the restroome sometimes...
 		String toilettActionIdentifier = "goToToilettSometimes";
 		forAllAgentsActionPoolDoThis(new GoAndStayThere(RoomCollector
 				.getInstance().findRoomByIdentifier("mensRestroom"), 1),
 				toilettActionIdentifier);
 		// but in fact Susi has to go to the womensRoom -> overwriting her
 		// Action
 		ActionCollector.getInstance().newAction(
				AgentCollector.getInstance().findAgentByName("Susi Sekretärin").getIdentifier()+"_"+
				toilettActionIdentifier+"_pool",
 				new GoAndStayThere(RoomCollector.getInstance()
 						.findRoomByIdentifier("womensRestroom"), 1));
 
 		// and need some coffee sometimes...
 		ComplexAction makeSomeCoffee = new ComplexAction();
 		makeSomeCoffee.addSubAction(new Move(RoomCollector.getInstance()
 				.findRoomByIdentifier("kitchen")));
 		makeSomeCoffee.addSubAction(new StayHere(5, 2));
 
 		forAllAgentsActionPoolDoThis(makeSomeCoffee, "makeCoffeeSometimes");
 
 	}
 
 	/**
 	 * Adds a given action to every already created agents action pool. Clones
 	 * the action so that everyone has its own.
 	 * 
 	 * @param action
 	 *            the action that everyone should do
 	 * @param identifier
	 *            TODO
 	 */
 	private static void forAllAgentsActionPoolDoThis(AbstractAction action,
 			String givenIdentifier) {
 		for (Agent a : AgentCollector.getInstance().getAll()) {
 			AbstractAction clonedAction = action.clone();
 			ActionCollector.getInstance().newAction(
 					a.getIdentifier() + "_" + givenIdentifier + "_pool",
 					clonedAction);
 		}
 	}
 }

 package pl.ug.airport.agents;
 
 import pl.ug.airport.behaviours.StaffAssignmentBehaviour;
 import jade.core.Agent;
 
 /*
  * Przydzia zaogi
  */
 public class StaffAssignmentAgent extends Agent {
 	StaffAssignmentBehaviour mainBhv;
	
 	@Override
 	protected void setup() {
		mainBhv = new StaffAssignmentBehaviour(this);
 		this.addBehaviour(mainBhv);
 	}
 }

 package smarthouse.simulation;
 
 import Data.Constants;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.gui.GuiAgent;
 import jade.gui.GuiEvent;
 
 public class SimulationAgent extends GuiAgent {
 	private SimulationFrame window;
 
 	public SimulationAgent() {
 		super();
 	}
 
 	protected void setup(){
 		window = new SimulationFrame(this);
 
 		DFAgentDescription dfd = new DFAgentDescription();
 		dfd.setName(getAID());
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType(Constants.SIMULATION);
 		sd.setName(Constants.SIMULATION_AGENT);
 		dfd.addServices(sd);
 		try {
 			DFService.register(this, dfd);
 		} catch (FIPAException fe) {
 			fe.printStackTrace();
 		}
 
 		addBehaviour(new ReceiveInformBehaviour());
 		addBehaviour(new ReceiveRequestBehaviour());
 	}
 
 	public SimulationFrame getWindow() {
 		return window;
 	}
 
 	protected void onGuiEvent(GuiEvent event) {
		Integer day = (Integer) event.getParameter(0);
		Integer hour = (Integer) event.getParameter(1);
		Integer minute = (Integer) event.getParameter(2);
 		addBehaviour(new SendTimeBehaviour(day, hour, minute));
 	}
 }

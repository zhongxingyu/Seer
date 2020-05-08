 package agent.test.mock;
 
 import java.util.List;
 
 import factory.KitConfig;
 import factory.PartType;
 import agent.data.Kit;
 import agent.data.Part;
 import agent.interfaces.Nest;
 import agent.interfaces.PartsRobot;
 
 
 /**
  * Mock partsrobot. Messages received simply add an entry to the mock agent's
  * log.
  * @author Daniel Paje
  */
 public class MockPartsRobot extends MockAgent implements PartsRobot {
 
 	public EventLog log;
 
 	public MockPartsRobot(String name) {
 		super(name, new EventLog());
 		this.log = super.getLog();
 	}
 
 	@Override
 	public void msgHereIsKitConfiguration(KitConfig config) {
 		// TODO Auto-generated method stub
 		log.add(new LoggedEvent("Recieved msgHereIsKitConfiguration"));
 	}
 
 	@Override
 	public void msgUseThisKit(Kit k) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void msgPickUpPartDone() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void msgGivePartToKitDone() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void msgHereAreGoodParts(Nest n, List<Part> goodparts) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void msgSetDropChance(float dChance) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }

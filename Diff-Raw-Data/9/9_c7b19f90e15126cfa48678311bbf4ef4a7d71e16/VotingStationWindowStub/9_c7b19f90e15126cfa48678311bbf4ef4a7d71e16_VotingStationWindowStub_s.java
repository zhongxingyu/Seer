 package unitTests;
 
import GUI.StationPanel;
 import choosingList.IChoosingList.ChoosingInterruptedException;
 import votingStation.IVotingStationWindow;
 
public class VotingStationWindowStub extends StationPanel implements IVotingStationWindow{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public VotingStationWindowStub(String name) {
		super(name);
		this.setVisible(false);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public Boolean getConfirmation(String confirmationMessage) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void printError(String errorMessage) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void printMessage(String message) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void closeWindow() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void startLoop() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public int getID() throws ChoosingInterruptedException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public String getPassword() throws ChoosingInterruptedException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void endLoop() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }

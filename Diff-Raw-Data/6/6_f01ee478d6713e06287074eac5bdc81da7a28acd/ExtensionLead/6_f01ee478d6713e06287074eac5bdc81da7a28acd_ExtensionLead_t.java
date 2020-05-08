 package sta.andswtch.extensionLead;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import sta.andswtch.gui.IAndSwtchViews;
 import sta.andswtch.network.ConnectionManager;
 
 public class ExtensionLead implements IExtensionLead {
 	
 	public static final int POWERPOINTCNT = 8;
 	
 	private List<PowerPoint> powerPoints;
 	private Config config;
 	private ConnectionManager connectionManager;
 	private ResponseProcessor responseProcessor;
 	private CommandGenerator commandGenerator;
 	private AutoRefresh autoRefresh;
 	private IAndSwtchViews currentView;
 	private String name = "";
 	
 	public ExtensionLead(IAndSwtchViews currentView) {
 		this.currentView = currentView;
 		this.init();
 	}
 	
 	private void init() {
 		this.powerPoints = new ArrayList<PowerPoint>();
 		this.config = new Config(this.currentView.getAppContext());
 		this.connectionManager = new ConnectionManager(this.config, this);
 		for (int id = 1; id <= POWERPOINTCNT; id++) {
 			this.addPowerPoint(id - 1, "No. " + id, false, false);
 		}
 		this.responseProcessor = new ResponseProcessor(this.powerPoints, this);
 		this.commandGenerator = new CommandGenerator(this.config);
 		this.startAutoRefreshRunning();
 	}
 	
 	public void setCurrentView(IAndSwtchViews v) {
 		this.currentView = v;
 	}
 
 	public void switchState(int id) {
 		boolean on = this.powerPoints.get(id - 1).isOn();
 		String command = this.commandGenerator.generateSwitchCommand(id, !on);
 		this.connectionManager.sendAndReceive(command);
 	}
 
 	public void sendState(int id, boolean on, int time) {
 		byte[] command = this.commandGenerator.generateSwitchDelayedCommand(id, on, time);
 		this.connectionManager.sendWithoutReceive(command);
 	}
 
 	public void sendStateAll(boolean on) {
 		byte[] command = this.commandGenerator.generateSwitchAllCommand(on, powerPoints);
 		this.connectionManager.sendAndReceive(command);
 	}
 
 	public void sendUpdateMessage() {
 		String command = this.commandGenerator.generateStatusUpdateCommand();
 		this.connectionManager.sendAndReceive(command);
 	}
 
 	public void updateDatastructure(String response) {
 		if (this.responseProcessor == null) {
 			this.responseProcessor = new ResponseProcessor(this.powerPoints, this);
 		}
 		this.responseProcessor.processResponse(response);
 		this.currentView.updateActivity();
 	}
 
 	public void errorOccured(String message){
 		this.currentView.showErrorMessage(message);
 	}
 
 	private void addPowerPoint(int id, String name, boolean enable, boolean on) {
 		this.powerPoints.add(new PowerPoint(id, name, enable, on));
 	}
 
 	public boolean isPowerPointOn(int id) {
 		return this.powerPoints.get(id - 1).isOn();
 	}
 
 	public boolean isPowerPointEnable(int id) {
 		return this.powerPoints.get(id - 1).isEnabled();
 	}
 
 	public void setPowerPointName(int id, String name) {
 		this.powerPoints.get(id - 1).setName(name);
 	}
 
 	public String getPowerPointName(int id) {
 		return this.powerPoints.get(id - 1).getName();
 	}
 
 	public String getHost() {
 		return this.config.getHost();
 	}
 
 	public int getPortIn() {
 		return this.config.getApplicationSenderPort();
 	}
 
 	public int getPortOut() {
 		return this.config.getApplicationReceiverPort();
 	}
 
 	public String getUser() {
 		return this.config.getUser();
 	}
 
 	public String getPassword() {
 		return this.config.getPassword();
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public int getUpdateInterval() {
 		return this.config.getUpdateInterval();
 	}
 	
 	public void stopAutoRefreshRunning() {
		if(this.autoRefresh != null) {
			this.autoRefresh.stopAutoRefresh();
			this.autoRefresh = null;
		}
 	}
 	
 	public void startAutoRefreshRunning() {
 		if(this.autoRefresh == null) {
 			this.autoRefresh = new AutoRefresh(this);
 		}
 		this.autoRefresh.startAutoRefresh();
 	}
 }

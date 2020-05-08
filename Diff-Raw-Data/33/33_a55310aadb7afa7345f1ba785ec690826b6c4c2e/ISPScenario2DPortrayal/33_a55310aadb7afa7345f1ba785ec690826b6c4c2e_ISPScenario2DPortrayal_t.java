 package es.upm.dit.gsi.shanks.shanks_isp_module.model.scenario.portrayal;
 
 import sim.field.grid.SparseGrid2D;
 import sim.portrayal.continuous.ContinuousPortrayal2D;
 import sim.portrayal.grid.SparseGridPortrayal2D;
 import sim.portrayal.network.NetworkPortrayal2D;
 import sim.util.Double2D;
 import es.upm.dit.gsi.shanks.datacenter.model.element.device.DCRouter;
 import es.upm.dit.gsi.shanks.hackerhan.model.element.device.WifiRouterADSL;
 import es.upm.dit.gsi.shanks.hackerhan.model.element.device.WirelessDevice;
 import es.upm.dit.gsi.shanks.hackerhan.model.element.device.portrayal.Smartphone2DPortrayal;
 import es.upm.dit.gsi.shanks.hackerhan.model.element.device.portrayal.WifiRouterADSL2DPortrayal;
 import es.upm.dit.gsi.shanks.model.element.NetworkElement;
 import es.upm.dit.gsi.shanks.model.element.device.Device;
 import es.upm.dit.gsi.shanks.model.element.link.Link;
 import es.upm.dit.gsi.shanks.model.failure.portrayal.Failure2DPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.ComplexScenario;
 import es.upm.dit.gsi.shanks.model.scenario.Scenario;
 import es.upm.dit.gsi.shanks.model.scenario.exception.ScenarioNotFoundException;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.ComplexScenario2DPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.ScenarioPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.ShanksMath;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.exception.DuplicatedPortrayalIDException;
 import es.upm.dit.gsi.shanks.networkattacks.util.networkelements.Computer;
import es.upm.dit.gsi.shanks.networkattacks.util.networkelements.RouterDNS;
 import es.upm.dit.gsi.shanks.networkattacks.util.networkelements.portrayals.Computer2DPortrayal;
 import es.upm.dit.gsi.shanks.networkattacks.util.networkelements.portrayals.Router2DPortrayal;
 import es.upm.dit.gsi.shanks.networkattacks.util.networkelements.portrayals.Server2DPortrayal;
 import es.upm.dit.gsi.shanks.shanks_isp_module.model.Values;
 import es.upm.dit.gsi.shanks.shanks_isp_module.model.element.device.ISPGateway;
 import es.upm.dit.gsi.shanks.shanks_isp_module.model.element.portrayal.ISPGateway2DPortrayal;
 import es.upm.dit.gsi.shanks.workerroom.model.element.device.Printer;
 import es.upm.dit.gsi.shanks.workerroom.model.element.portrayal.EthernetLink2DPortrayal;
 import es.upm.dit.gsi.shanks.workerroom.model.element.portrayal.Printer2DPortrayal;
 
 public class ISPScenario2DPortrayal extends ComplexScenario2DPortrayal {
 
 	public static final String FAILURE_DISPLAY_ID = "Current Failures";
 	public static final String FAILURE_PORTRAYAL_ID = "Failures";
 
 	public ISPScenario2DPortrayal(Scenario scenario, int width, int height)
 			throws DuplicatedPortrayalIDException, ScenarioNotFoundException {
 		super(scenario, width, height);
 	}
 
 	@Override
 	public void addPortrayals() {
 		SparseGrid2D failuresGrid = new SparseGrid2D(100, 100);
 		SparseGridPortrayal2D failuresPortrayal = new SparseGridPortrayal2D();
 		failuresPortrayal.setField(failuresGrid);
 		try {
 			this.addPortrayal(FAILURE_DISPLAY_ID, FAILURE_PORTRAYAL_ID,
 					failuresPortrayal);
 		} catch (DuplicatedPortrayalIDException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void placeElements() {
 		ComplexScenario cs = (ComplexScenario) this.getScenario();
 		this.situateDevice(
 				(Device) cs.getNetworkElement(Values.ISP_GATEWAY_ID), 150, 150);
 		for (String key : cs.getCurrentElements().keySet()) {
 			NetworkElement ne = cs.getCurrentElements().get(key);
 			if (ne instanceof Link) {
 				this.drawLink((Link) ne);
 			}
 		}
 	}
 
 	@Override
 	public void setupPortrayals() {
 		ContinuousPortrayal2D devicesPortrayal = (ContinuousPortrayal2D) this
 				.getPortrayals().get(Scenario2DPortrayal.MAIN_DISPLAY_ID)
 				.get(ScenarioPortrayal.DEVICES_PORTRAYAL);
 		NetworkPortrayal2D networkPortrayalLink = (NetworkPortrayal2D) this
 				.getPortrayals().get(Scenario2DPortrayal.MAIN_DISPLAY_ID)
 				.get(ScenarioPortrayal.LINKS_PORTRAYAL);
 		SparseGridPortrayal2D failuresPortrayal = (SparseGridPortrayal2D) this
 				.getPortrayals().get(FAILURE_DISPLAY_ID)
 				.get(FAILURE_PORTRAYAL_ID);
 
 		devicesPortrayal.setPortrayalForClass(Computer.class,
 				new Computer2DPortrayal());
		devicesPortrayal.setPortrayalForClass(RouterDNS.class,
				new Router2DPortrayal());
 		devicesPortrayal.setPortrayalForClass(DCRouter.class,
 				new Router2DPortrayal());
 		devicesPortrayal
 				.setPortrayalForClass(
 						es.upm.dit.gsi.shanks.networkattacks.util.networkelements.Server.class,
 						new Server2DPortrayal());
 		devicesPortrayal.setPortrayalForClass(Computer.class,
 				new Computer2DPortrayal());
 		devicesPortrayal.setPortrayalForClass(WifiRouterADSL.class,
 				new WifiRouterADSL2DPortrayal());
 		devicesPortrayal.setPortrayalForClass(WirelessDevice.class,
 				new Smartphone2DPortrayal());
 		devicesPortrayal
 				.setPortrayalForClass(
 						es.upm.dit.gsi.shanks.workerroom.model.element.device.Computer.class,
 						new es.upm.dit.gsi.shanks.workerroom.model.element.portrayal.Computer2DPortrayal());
 		devicesPortrayal.setPortrayalForClass(Printer.class,
 				new Printer2DPortrayal());
 		devicesPortrayal
 				.setPortrayalForClass(
 						es.upm.dit.gsi.shanks.workerroom.model.element.device.LANRouter.class,
 						new es.upm.dit.gsi.shanks.workerroom.model.element.portrayal.Router2DPortrayal());
 		devicesPortrayal.setPortrayalForClass(ISPGateway.class,
 				new ISPGateway2DPortrayal());
 
 		networkPortrayalLink.setPortrayalForAll(new EthernetLink2DPortrayal());
 		failuresPortrayal.setPortrayalForAll(new Failure2DPortrayal());
 	}
 
 	@Override
 	public void placeScenarios() throws DuplicatedPortrayalIDException,
 			ScenarioNotFoundException {
 		ComplexScenario cs = (ComplexScenario) this.getScenario();
 		int place = 0;
 		int positionX = 0;
 		int positionY = 0;
 		for (Scenario subScenario : cs.getScenarios()) {
 			if (place == 0) {
 				this.situateScenario(subScenario, new Double2D(
 						positionX, positionY), ShanksMath.ANGLE_0, 
 						ShanksMath.ANGLE_0, ShanksMath.ANGLE_0);
 				place = 1;
 				positionX = positionX+Values.deltaX;
 			} else if (place == 1) {
 				this.situateScenario(subScenario, new Double2D(
 						positionX, positionY), ShanksMath.ANGLE_0, 
 						ShanksMath.ANGLE_0, ShanksMath.ANGLE_0);
 				place = 2;
 				positionX = positionX+Values.deltaX;
 			} else if (place == 2) {
 				this.situateScenario(subScenario, new Double2D(
 						positionX, positionY), ShanksMath.ANGLE_0, 
 						ShanksMath.ANGLE_0, ShanksMath.ANGLE_0);
 				place = 3;
 				positionY = positionY+Values.deltaY;
 			} else if (place == 3) {
 				this.situateScenario(subScenario, new Double2D(
 						positionX, positionY), ShanksMath.ANGLE_0, 
 						ShanksMath.ANGLE_0, ShanksMath.ANGLE_0);
 				place = 0;
 				positionY = positionY+Values.deltaY;
 			}
 		}
 	}
 
 }

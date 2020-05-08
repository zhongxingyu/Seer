 package es.upm.dit.gsi.shanks.datacenter.model.scenario;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import es.upm.dit.gsi.shanks.datacenter.model.Values;
 import es.upm.dit.gsi.shanks.datacenter.model.failure.ServerFailure;
 import es.upm.dit.gsi.shanks.datacenter.model.scenario.portrayal.DataCenterScenario2DPortrayal;
 import es.upm.dit.gsi.shanks.datacenter.model.scenario.portrayal.DataCenterScenario3DPortrayal;
 import es.upm.dit.gsi.shanks.model.element.NetworkElement;
 import es.upm.dit.gsi.shanks.model.element.exception.TooManyConnectionException;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementStatusException;
 import es.upm.dit.gsi.shanks.model.element.link.Link;
 import es.upm.dit.gsi.shanks.model.failure.Failure;
 import es.upm.dit.gsi.shanks.model.scenario.Scenario;
 import es.upm.dit.gsi.shanks.model.scenario.exception.DuplicatedIDException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.ScenarioNotFoundException;
 import es.upm.dit.gsi.shanks.model.scenario.exception.UnsupportedScenarioStatusException;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario2DPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.Scenario3DPortrayal;
 import es.upm.dit.gsi.shanks.model.scenario.portrayal.exception.DuplicatedPortrayalIDException;
 import es.upm.dit.gsi.shanks.networkattacks.util.failures.ComputerFailure;
 import es.upm.dit.gsi.shanks.networkattacks.util.failures.RouterFailure;
 import es.upm.dit.gsi.shanks.networkattacks.util.networkelements.Computer;
 import es.upm.dit.gsi.shanks.networkattacks.util.networkelements.EthernetLink;
 import es.upm.dit.gsi.shanks.networkattacks.util.networkelements.RouterDNS;
 import es.upm.dit.gsi.shanks.networkattacks.util.networkelements.Server;
 
 public class DataCenterScenario extends Scenario {
 
 	// TODO make that the status has influence.
 	public static final String CLOUDY = "CLOUDY";
     public static final String SUNNY = "SUNNY";
 	public static final String STATUS_NORMAL = "Normal";
 	public static final String STATUS_UNDER_ATTACK = "UnderAttack";
 
 	public DataCenterScenario(String id, String initialState,
 			Properties properties)
 			throws UnsupportedNetworkElementStatusException,
 			TooManyConnectionException, UnsupportedScenarioStatusException,
 			DuplicatedIDException {
 		super(id, initialState, properties);
 	}
 
 	@Override
 	public Scenario2DPortrayal createScenario2DPortrayal()
 			throws DuplicatedPortrayalIDException, ScenarioNotFoundException {
 		return new DataCenterScenario2DPortrayal(this, 100, 150);
 	}
 
 	@Override
 	public Scenario3DPortrayal createScenario3DPortrayal()
 			throws DuplicatedPortrayalIDException, ScenarioNotFoundException {
 		return new DataCenterScenario3DPortrayal(this, 100, 100, 100);
 	}
 
 	@Override
 	public void setPossibleStates() {
 		this.addPossibleStatus(DataCenterScenario.STATUS_UNDER_ATTACK);
 		this.addPossibleStatus(DataCenterScenario.STATUS_NORMAL);
 
 	}
 
 	@Override
 	public void addNetworkElements()
 			throws UnsupportedNetworkElementStatusException,
 			TooManyConnectionException, DuplicatedIDException {
 		
 		RouterDNS intranetRouter = new RouterDNS(Values.DATA_CENTER_ROUTER_ID);
 		addNetworkElement(intranetRouter);
 		
 		RouterDNS webProxy = new RouterDNS(Values.WEB_PROXY_ID);
 		addNetworkElement(webProxy);
 		
 		Link intranetWebProxyLink = new EthernetLink(Values.INTRANET_LINK, Values.ETHERNET_LENGHT);
 		webProxy.connectToDeviceWithLink(intranetRouter, intranetWebProxyLink);
 
 		// SysAmind terminals.
 		ArrayList<EthernetLink> links = new ArrayList<EthernetLink>();
 		for (int i = 0; i < Values.NUMBER_OF_SYSADMINS; i++) {
 			Computer computer = (new Computer(Values.SA_COMPUTER_ID + i));
 			links.add(new EthernetLink(Values.ETHERNET_ID+i, Values.ETHERNET_LENGHT));
 			computer.connectToDeviceWithLink(intranetRouter, links.get(links.size()-1));
 			addNetworkElement(computer);
 		}
 
 		
 		// Servers
 		Server ldap = new Server(Values.LDAP_SERVER_ID);
 		links.add(new EthernetLink(Values.ETHERNET_ID+(links.size()), Values.ETHERNET_LENGHT));
 		ldap.connectToDeviceWithLink(intranetRouter, links.get(links.size()-1));
 		addNetworkElement(ldap);
 		
 		Server externalServices = new Server(Values.EXTERNAL_SERVICES_SERVER_ID);
 		links.add(new EthernetLink(Values.ETHERNET_ID+links.size(), Values.ETHERNET_LENGHT));
 		externalServices.connectToDeviceWithLink(webProxy, links.get(links.size()-1));
 		links.add(new EthernetLink(Values.ETHERNET_ID+links.size(), Values.ETHERNET_LENGHT));
 		externalServices.connectToDeviceWithLink(intranetRouter, links.get(links.size()-1));
 		addNetworkElement(externalServices);
 		
 		Server bbdd = new Server(Values.BBDD_SERVER_ID);
 		links.add(new EthernetLink(Values.ETHERNET_ID+links.size(), Values.ETHERNET_LENGHT));
 		bbdd.connectToDeviceWithLink(intranetRouter, links.get(links.size()-1));
 		addNetworkElement(bbdd);
 		
 		Server bbddReplica = new Server(Values.BBDD_REPLICA_ID);
 		links.add(new EthernetLink(Values.ETHERNET_ID+links.size(), Values.ETHERNET_LENGHT));
 		bbddReplica.connectToDeviceWithLink(bbdd, links.get(links.size()-1));
 		addNetworkElement(bbddReplica);
 
 		//Adding links
 		for(EthernetLink link: links){
 				this.addNetworkElement(link);
 		}
 
 	}
 
 	@Override
 	public void addPossibleFailures() {
 		
 		HashMap<String, NetworkElement> addedElements = this.getCurrentElements();
 		Set<NetworkElement> routerSet = new HashSet<NetworkElement>();
 		List<Set<NetworkElement>> computers = new ArrayList<Set<NetworkElement>>();
 		List<Set<NetworkElement>> servers = new ArrayList<Set<NetworkElement>>();
 		
 		for(String eName: addedElements.keySet()){
 			Set<NetworkElement> computerSet = new HashSet<NetworkElement>();
 			Set<NetworkElement> serverSet = new HashSet<NetworkElement>();
 			NetworkElement e = addedElements.get(eName);
 			if(!(e instanceof Link)){
 				if((e instanceof Computer)) {
 					if((e instanceof Server)){
 						serverSet.add(e);
 						servers.add(serverSet);
 					} else {
 						computerSet.add(e);
 						computers.add(computerSet);
 					}
				} else if(e instanceof RouterDNS){
					routerSet.add(e);
 				}
 			}
 		}
 		this.addPossibleFailure(RouterFailure.class, routerSet);
 		this.addPossibleFailure(ComputerFailure.class, computers);
 		this.addPossibleFailure(ServerFailure.class, servers);
 		
 	}
 
 	@Override
 	public void addPossibleEvents() {
 	}
 
 	@Override
 	public HashMap<Class<? extends Failure>, Double> getPenaltiesInStatus(
 			String status) throws UnsupportedScenarioStatusException {
 		HashMap<Class<? extends Failure>, Double> penalties = new HashMap<Class<? extends Failure>, Double>();
 	    return penalties;
 	}
 }

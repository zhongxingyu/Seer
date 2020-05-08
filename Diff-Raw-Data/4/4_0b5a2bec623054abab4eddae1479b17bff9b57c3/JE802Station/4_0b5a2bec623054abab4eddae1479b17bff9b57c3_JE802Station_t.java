 /*
  * 
  * This is Jemula.
  *
  *    Copyright (c) 2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
  *    All rights reserved. Urheberrechtlich geschuetzt.
  *    
  *    Redistribution and use in source and binary forms, with or without modification,
  *    are permitted provided that the following conditions are met:
  *    
  *      Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer. 
  *    
  *      Redistributions in binary form must reproduce the above copyright notice,
  *      this list of conditions and the following disclaimer in the documentation and/or
  *      other materials provided with the distribution. 
  *    
  *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
  *      may be used to endorse or promote products derived from this software without
  *      specific prior written permission. 
  *    
  *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  *    OF SUCH DAMAGE.
  *    
  */
 
 package station;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import kernel.JEEvent;
 import kernel.JEEventScheduler;
 import kernel.JETime;
 import kernel.JEmula;
 import layer0_medium.JEIWirelessMedium;
 import layer1_802Phy.JE802Mobility;
 import layer1_802Phy.JE802PhyMode;
 import layer2_802Mac.JE802_Mac;
 import layer2_80211Mac.JE802_11Mac;
 import layer3_network.JE802RouteManager;
 import layer4_transport.JE802TCPManager;
 import layer5_application.JE802TrafficGen;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import emulator.JE802StatEval;
 import gui.JE802Gui;
 
 public class JE802Station extends JEmula {
 
 	private List<JE802TrafficGen> trafficGenerators;
 
 	private int address;
 
 	private JE802_Mac theMac;
 
 	// TODO: Roman: Shouldn't there be only one general map for all the macs?
 	private Map<Integer, JE802_11Mac> dot11MacMap;
 
 	private final JE802StatEval statEval;
 
 	private JE802Mobility mobility;
 
 	private List<Integer> wiredAddresses;
 
 	private JE802Sme sme;
 
 	private JE802RouteManager ipLayer;
 
 	private JE802TCPManager tcp;
 
 	private final XPath xpath = XPathFactory.newInstance().newXPath();
 
 	public JE802Station(JEEventScheduler aScheduler, JEIWirelessMedium aChannel, Random aGenerator, JE802Gui aGui,
 			JE802StatEval aStatEval, Node topLevelNode, List<JE802PhyMode> phyModes, double longitude, double latitude)
 			throws XPathExpressionException {
 		Element aTopLevelNode = (Element) topLevelNode;
 		this.theUniqueEventScheduler = aScheduler;
 		this.statEval = aStatEval;
 		if (aTopLevelNode.getNodeName().equals("JE802Station")) {
 			this.message("XML definition " + aTopLevelNode.getNodeName() + " found.", 1);
 
 			// get station address
 			this.address = Integer.parseInt(aTopLevelNode.getAttribute("address"));
 			String wiredStationsString = aTopLevelNode.getAttribute("wiredTo");
 			if (wiredStationsString.isEmpty()) {
 				this.wiredAddresses = null;
 			} else {
 				String[] addresses = wiredStationsString.split(",");
 				this.wiredAddresses = new ArrayList<Integer>(addresses.length);
 				for (String wired : addresses) {
 					Integer addr = new Integer(wired);
 					this.wiredAddresses.add(addr);
 				}
 			}
 
 			this.dot11MacMap = new HashMap<Integer, JE802_11Mac>();
 			this.trafficGenerators = new ArrayList<JE802TrafficGen>();
 
 			if (aTopLevelNode.hasChildNodes()) {
 				// -- create SME (Station Management Entity):
 				// ------------------------------------------------------------------------------------------------
 				Node smeNode = (Node) xpath.evaluate("JE802SME", aTopLevelNode, XPathConstants.NODE);
 				this.message("allocating " + smeNode.getNodeName(), 10);
 				this.sme = new JE802Sme(aScheduler, aGenerator, smeNode, this);
 
 				// -- create mobility:
 				// ----------------------------------------------------------------------------------------
 				Node mobNode = (Node) xpath.evaluate("JE802Mobility", aTopLevelNode, XPathConstants.NODE);
 				this.message("allocating " + mobNode.getNodeName(), 10);
 				this.mobility = new JE802Mobility(mobNode, longitude, latitude);
 
 				// -- create MACS:
 				// ----------------------------------------------------------------------------------------
 
 				// create 802_11 Macs, if any
 
 				NodeList macList = (NodeList) xpath.evaluate("JE80211MAC", aTopLevelNode, XPathConstants.NODESET);
 				if (macList.getLength() > 0) {
 					for (int i = 0; i < macList.getLength(); i++) {
 						Node macNode = macList.item(i);
 						this.message("allocating " + macNode.getNodeName(), 10);
 						JE802_11Mac theMac = new JE802_11Mac(aScheduler, aStatEval, aGenerator, aGui, aChannel, macNode,
 								this.sme.getHandlerId());
 						this.dot11MacMap.put(theMac.getChannel(), (JE802_11Mac) theMac);
 						theMac.setMACAddress(this.address);
 						theMac.getPhy().setThePhyModeList(phyModes);
 						theMac.getMlme().getTheAlgorithm().compute();
 						theMac.getPhy().setMobility(this.mobility);
 						theMac.getPhy().send(new JEEvent("start_req", theMac.getPhy(), theUniqueEventScheduler.now()));
 						this.theMac = theMac;
 					}
 
 					// TODO: Roman: Why convert to ArrayList and then in sme
 					// build a HashMap again?
 					// Only this class uses the setMacs method...
 					sme.setMacs(new ArrayList<JE802_11Mac>(dot11MacMap.values()));
 
 					this.ipLayer = new JE802RouteManager(aScheduler, aGenerator, this.sme, statEval, this);
 					this.sme.setIpHandlerId(this.ipLayer.getHandlerId());
 				} else {
 					this.message("No JE80211Mac definition found", 10);
 				}
 
 				// create 802_15 Macs, if any
 				NodeList mac15List = (NodeList) xpath.evaluate("JE80215MAC", aTopLevelNode, XPathConstants.NODESET);
 				if (mac15List.getLength() > 0) {
 					for (int i = 0; i < mac15List.getLength(); i++) {
 						Node macNode = mac15List.item(i);
 						this.message("allocating " + macNode.getNodeName(), 10);
 						theMac.setMACAddress(this.address);
 						theMac.getPhy().setThePhyModeList(phyModes);
 						theMac.getPhy().setMobility(this.mobility);
 						theMac.getPhy().send(new JEEvent("start_req", theMac.getPhy(), theUniqueEventScheduler.now()));
 					}
 
 					this.ipLayer = new JE802RouteManager(aScheduler, aGenerator, this.sme, statEval, this);
 					this.sme.setIpHandlerId(this.ipLayer.getHandlerId());
 				} else {
 					this.message("No JE80215Mac found", 10);
 				}
 
 				// create 802_15 Mac Low-Energy, if any
 				NodeList mac15LowEnergyList = (NodeList) xpath.evaluate("JE80215MAC_LowEnergy", aTopLevelNode,
 						XPathConstants.NODESET);
 				if (mac15LowEnergyList.getLength() > 0) {
 					for (int i = 0; i < mac15LowEnergyList.getLength(); i++) {
 						Node macNode = mac15LowEnergyList.item(i);
 						this.message("allocating " + macNode.getNodeName(), 10);
 						theMac.setMACAddress(this.address);
 						theMac.getPhy().setThePhyModeList(phyModes);
 						theMac.getPhy().setMobility(this.mobility);
 						theMac.getPhy().send(new JEEvent("start_req", theMac.getPhy(), theUniqueEventScheduler.now()));
 					}
 
 					this.ipLayer = new JE802RouteManager(aScheduler, aGenerator, this.sme, statEval, this);
 					this.sme.setIpHandlerId(this.ipLayer.getHandlerId());
 				} else {
 					this.message("No JE80215Mac found", 10);
 				}
 
 				// TODO: Stefan, added LED MAC
 				// create 802 LED Macs, if any
 				NodeList macLEDList = (NodeList) xpath.evaluate("JE80215LEDMAC", aTopLevelNode, XPathConstants.NODESET);
 				if (macLEDList.getLength() > 0) {
 					for (int i = 0; i < macLEDList.getLength(); i++) {
 						Node macNode = macLEDList.item(i);
 						this.message("allocating " + macNode.getNodeName(), 10);
 						// this.dot15MacMap.put(theMac.getChannel(),
 						// (JE802_15Mac) theMac);
 						theMac.setMACAddress(this.address);
 						// theMac.getPhy().setThePhyModeList(phyModes);
 						// theMac.getPhy().setMobility(this.mobility);
 						// theMac.getPhy().send(new JEEvent("start_req",
 						// theMac.getPhy(), theUniqueEventScheduler.now()));
 					}
 
 					this.ipLayer = new JE802RouteManager(aScheduler, aGenerator, this.sme, statEval, this);
 					this.sme.setIpHandlerId(this.ipLayer.getHandlerId());
 				} else {
 					this.message("No JE80215LEDMAC found", 10);
 				}
 
 				// -- create TCP Manager:
 				// ------------------------------------------------------------------------------------------------
 				Node tcpNode = (Node) xpath.evaluate("JE802TCP", aTopLevelNode, XPathConstants.NODE);
 				this.tcp = new JE802TCPManager(aScheduler, aGenerator, tcpNode, this.ipLayer, this.statEval);
 				this.ipLayer.setTcpHandlerId(tcp.getHandlerId());
 
 				/*
 				 * for (Iterator<JE802_11Mac> iterator =
 				 * macMap.values().iterator(); iterator.hasNext();) {
 				 * JE802_11Mac currentMac = (JE802_11Mac) iterator.next(); }
 				 * mac.setTcpHandlerId(this.tcp.getHandlerId());
 				 */
 
 				// -- create traffic generators:
 				// ----------------------------------------------------------------------------------------
 
 				NodeList tgList = (NodeList) xpath.evaluate("JE802TrafficGen", aTopLevelNode, XPathConstants.NODESET);
 				for (int i = 0; i < tgList.getLength(); i++) {
 					Node tgNode = tgList.item(i);
 					this.message("allocating " + tgNode.getNodeName(), 10);
 					JE802TrafficGen aNewTrafficGen = new JE802TrafficGen(aScheduler, aGenerator, tgNode, this.address, aStatEval,
 							tcp);
 					this.trafficGenerators.add(aNewTrafficGen);
 					// stop generating traffic, usually not used:
 					aNewTrafficGen.send(new JEEvent("stop_req", aNewTrafficGen, aNewTrafficGen.getStopTime()));
 					// start generating traffic:
 					aNewTrafficGen.send(new JEEvent("start_req", aNewTrafficGen, aNewTrafficGen.getStartTime()));
 				}
 
 				if (mac15List.getLength() > 0) {
 					this.sme.send(new JEEvent("start_req", this.sme.getHandlerId(), theUniqueEventScheduler.now()));
 				}
 
 			} else {
 				this.message("XML definition " + aTopLevelNode.getNodeName() + " has no child nodes!", 10);
 			}
 		} else {
 			this.message("XML definition " + aTopLevelNode.getNodeName() + " found, but JE802Station expected!", 10);
 		}
 		
		if (aGui!=null) {
			aGui.setupStation(this.address);
		}
 		
 	}
 	
 	
 
 	public JE802_Mac getMac() {
 		return theMac;
 	}
 
 	public double getXLocation(JETime time) {
 		return this.mobility.getXLocation(time);
 	}
 
 	public double getYLocation(JETime time) {
 		return this.mobility.getYLocation(time);
 	}
 
 	public double getZLocation(JETime time) {
 		return this.mobility.getZLocation(time);
 	}
 
 	public boolean isMobile() {
 		return this.mobility.isMobile();
 	}
 
 	public List<JE802TrafficGen> getTrafficGenList() {
 		return this.trafficGenerators;
 	}
 
 	public JE802StatEval getStatEval() {
 		return statEval;
 	}
 
 	public int getMacAddress() {
 		return this.address;
 	}
 
 	public JE802Sme getSme() {
 		return this.sme;
 	}
 
 	public int getFixedChannel() {
 		for (JE802_11Mac aMac : dot11MacMap.values()) {
 			if (aMac.isFixedChannel()) {
 				return aMac.getChannel();
 			}
 		}
 		return this.dot11MacMap.values().iterator().next().getPhy().getCurrentChannelNumberTX();
 	}
 
 	public double getTransmitPowerLeveldBm() {
 		// returns power level of first mac
 		return this.dot11MacMap.values().iterator().next().getPhy().getCurrentTransmitPowerLevel_dBm();
 	}
 
 	public JE802Mobility getMobility() {
 		return mobility;
 	}
 
 	public long getLostPackets() {
 		return tcp.getLostPackets();
 	}
 
 	public void setWiredStations(List<JE802Station> wiredStations) {
 		this.sme.setWiredStations(wiredStations);
 	}
 
 	public List<Integer> getWiredAddresses() {
 		return this.wiredAddresses;
 	}
 
 	public long getTransmittedPackets() {
 		return tcp.getTransmittedPackets();
 	}
 
 	public void displayLossrate() {
 		tcp.retransmissionRate();
 	}
 
 	@Override
 	public String toString() {
 		return Integer.toString(theMac.getMacAddress());
 	}
 }

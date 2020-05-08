 package org.processmining.plugins.xpdl.converter;
 
 import java.awt.geom.Rectangle2D;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.processmining.framework.plugin.PluginContext;
 import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
 import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
 import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
 import org.processmining.models.graphbased.directed.bpmn.BPMNVendorSettings;
 import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
 import org.processmining.models.graphbased.directed.bpmn.elements.Event;
 import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventType;
 import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
 import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
 import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;
 import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;
 import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;
 import org.processmining.models.jgraph.ProMGraphModel;
 import org.processmining.models.jgraph.ProMJGraphVisualizer;
 import org.processmining.models.jgraph.elements.ProMGraphCell;
 import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
 import org.processmining.plugins.xpdl.Xpdl;
 import org.processmining.plugins.xpdl.XpdlAuthor;
 import org.processmining.plugins.xpdl.XpdlBlockActivity;
 import org.processmining.plugins.xpdl.XpdlCreated;
 import org.processmining.plugins.xpdl.XpdlEndEvent;
 import org.processmining.plugins.xpdl.XpdlEvent;
 import org.processmining.plugins.xpdl.XpdlIntermediateEvent;
 import org.processmining.plugins.xpdl.XpdlJoin;
 import org.processmining.plugins.xpdl.XpdlPackageHeader;
 import org.processmining.plugins.xpdl.XpdlParticipantType;
 import org.processmining.plugins.xpdl.XpdlProcessHeader;
 import org.processmining.plugins.xpdl.XpdlRedefinableHeader;
 import org.processmining.plugins.xpdl.XpdlRoute;
 import org.processmining.plugins.xpdl.XpdlSplit;
 import org.processmining.plugins.xpdl.XpdlStartEvent;
 import org.processmining.plugins.xpdl.XpdlTransitionRestriction;
 import org.processmining.plugins.xpdl.collections.XpdlActivities;
 import org.processmining.plugins.xpdl.collections.XpdlActivitySets;
 import org.processmining.plugins.xpdl.collections.XpdlLanes;
 import org.processmining.plugins.xpdl.collections.XpdlParticipants;
 import org.processmining.plugins.xpdl.collections.XpdlPools;
 import org.processmining.plugins.xpdl.collections.XpdlTransitionRefs;
 import org.processmining.plugins.xpdl.collections.XpdlTransitionRestrictions;
 import org.processmining.plugins.xpdl.collections.XpdlTransitions;
 import org.processmining.plugins.xpdl.collections.XpdlWorkflowProcesses;
 import org.processmining.plugins.xpdl.graphics.XpdlConnectorGraphicsInfo;
 import org.processmining.plugins.xpdl.graphics.XpdlCoordinates;
 import org.processmining.plugins.xpdl.graphics.XpdlNodeGraphicsInfo;
 import org.processmining.plugins.xpdl.graphics.collections.XpdlConnectorGraphicsInfos;
 import org.processmining.plugins.xpdl.graphics.collections.XpdlNodeGraphicsInfos;
 import org.processmining.plugins.xpdl.idname.XpdlActivity;
 import org.processmining.plugins.xpdl.idname.XpdlActivitySet;
 import org.processmining.plugins.xpdl.idname.XpdlLane;
 import org.processmining.plugins.xpdl.idname.XpdlParticipant;
 import org.processmining.plugins.xpdl.idname.XpdlPool;
 import org.processmining.plugins.xpdl.idname.XpdlTransition;
 import org.processmining.plugins.xpdl.idname.XpdlTransitionRef;
 import org.processmining.plugins.xpdl.idname.XpdlWorkflowProcess;
 import org.processmining.plugins.xpdl.text.XpdlDescription;
 import org.processmining.plugins.xpdl.text.XpdlDocumentation;
 import org.processmining.plugins.xpdl.text.XpdlPriority;
 import org.processmining.plugins.xpdl.text.XpdlText;
 import org.processmining.plugins.xpdl.text.XpdlVendor;
 import org.processmining.plugins.xpdl.text.XpdlVersion;
 import org.processmining.plugins.xpdl.text.XpdlXpdlVersion;
 
 public class BPMN2XPDLConversion {
 	
 	private BPMNDiagram bpmn;
 	private Xpdl xpdl;
 	
 	public BPMN2XPDLConversion(BPMNDiagram bpmn) {
 		this.bpmn=bpmn;
 		xpdl = fillXPDL();
 	}
 
 	/**
 	 * Convert BPMN diagram to XPDL with layout information
 	 * @return XPDL model
 	 */
 	public Xpdl convert2XPDL(PluginContext context) {
 
 		fillXpdlActivities();
 		fillXpdlActivityPositions(context);
 		fillXpdlTransitions();
 		fillSwimlanes();
 		return xpdl;
 	}
 	
 	/**
 	 * Convert BPMN diagram to XPDL without layout information
 	 * @return XPDL model
 	 */
 	public Xpdl convert2XPDL_noLayout() {
 
 		fillXpdlActivities();
 		fillXpdlTransitions();
 		fillSwimlanes();
 		return xpdl;
 	}
 
 	private void fillSwimlanes() {
 		List<XpdlActivity> activityList = xpdl.getWorkflowProcesses().getList().get(0).getActivities().getList();
 		
 		Map<String, String> map3 = new HashMap<String, String>();
 		for (BPMNNode bpmnNode : bpmn.getNodes()) {
 			if (bpmnNode.getParentSwimlane() != null) {
 				map3.put("" + bpmnNode.hashCode(), "" + bpmnNode.getParentSwimlane().hashCode());
 			}
 		}
 		if (!map3.isEmpty()) {
 			XpdlPools pools = xpdl.getPools();
 			XpdlPool xpdlPool = new XpdlPool("Pool");
 			xpdlPool.setId("Default-Pool");
 			xpdlPool.setBoundaryVisible("false");
 			xpdlPool.setProcess(xpdl.getWorkflowProcesses().getList().get(0).getId());
 			xpdlPool.setMainPool("true");
 
 			XpdlLanes lanes = new XpdlLanes("Lanes");
 
 			for (Swimlane swimlane : bpmn.getSwimlanes()) {
 				XpdlLane lane = new XpdlLane("Lane");
 				lane.setParentPool(xpdlPool.getId());
 				lane.setName(swimlane.getLabel());
 				lane.setId("" + swimlane.hashCode());
 				lanes.add2List(lane);
 			}
 			xpdlPool.setLanes(lanes);
 
 			pools.add2List(xpdlPool);
 
 			for (XpdlActivity xpdlActivity : activityList) {
 				if (xpdlActivity.getNodeGraphicsInfos() != null) {
 					List<XpdlNodeGraphicsInfo> infos = xpdlActivity.getNodeGraphicsInfos().getList();
 					for (XpdlNodeGraphicsInfo xpdlNodeGraphicsInfo : infos) {
 						xpdlNodeGraphicsInfo.setLaneId(map3.get(xpdlActivity.getId()));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param context
 	 * @param bpmn
 	 * @param xpdl
 	 */
 	private void fillXpdlActivityPositions(PluginContext context) {
 		ProMJGraphPanel graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, bpmn);
 		ProMGraphModel graphModel = graphPanel.getGraph().getModel();
 
 		@SuppressWarnings("rawtypes")
 		List proMGraphCellList = graphModel.getRoots();
 		XpdlWorkflowProcess workflowProcess = xpdl.getWorkflowProcesses().getList().get(0);
 		Map<String, XpdlActivity> map = new HashMap<String, XpdlActivity>();
 		for (XpdlActivity xpdlActivity : workflowProcess.getActivities().getList()) {
 			map.put(xpdlActivity.getId(), xpdlActivity);
 		}
 		System.out.println("");
 		for (Object object : proMGraphCellList) {
 			if (object instanceof ProMGraphCell) {
 				ProMGraphCell proMGraphCell = (ProMGraphCell) object;
 				XpdlActivity xpdlActivity = map.get("" + proMGraphCell.hashCode());
 				if (xpdlActivity != null) {
 					Rectangle2D rectangle = proMGraphCell.getView().getBounds();
 					XpdlNodeGraphicsInfos nodeGraphicsInfos = new XpdlNodeGraphicsInfos("NodeGraphicsInfos");
 
 					XpdlNodeGraphicsInfo nodeGraphicsInfo = new XpdlNodeGraphicsInfo("NodeGraphicsInfo");
 					nodeGraphicsInfo.setToolId("ProM");
 
 					XpdlCoordinates coordinates = new XpdlCoordinates("Coordinates");
 
 					nodeGraphicsInfo.setHeight("" + (int) rectangle.getHeight());
 					nodeGraphicsInfo.setWidth("" + (int) rectangle.getWidth());
 
 					coordinates.setxCoordinate("" + (int) rectangle.getX());
 					coordinates.setyCoordinate("" + (int) rectangle.getY());
 					nodeGraphicsInfo.setCoordinates(coordinates);
 
 					nodeGraphicsInfos.add2List(nodeGraphicsInfo);
 					xpdlActivity.setNodeGraphicsInfos(nodeGraphicsInfos);
 				} else {
 					@SuppressWarnings("unchecked")
 					List<Object> cells = proMGraphCell.getChildren();
 					for (Object object2 : cells) {
 						if (object2 instanceof ProMGraphCell) {
 							ProMGraphCell proMGraphCell2 = (ProMGraphCell) object2;
 							XpdlActivity xpdlActivity2 = map.get("" + proMGraphCell2.hashCode());
 							if (xpdlActivity2 != null) {
 								Rectangle2D rectangle = proMGraphCell2.getView().getBounds();
 								XpdlNodeGraphicsInfos nodeGraphicsInfos = new XpdlNodeGraphicsInfos("NodeGraphicsInfos");
 
 								XpdlNodeGraphicsInfo nodeGraphicsInfo = new XpdlNodeGraphicsInfo("NodeGraphicsInfo");
 								nodeGraphicsInfo.setToolId("ProM");
 
 								XpdlCoordinates coordinates = new XpdlCoordinates("Coordinates");
 
 								nodeGraphicsInfo.setHeight("" + (int) rectangle.getHeight());
 								nodeGraphicsInfo.setWidth("" + (int) rectangle.getWidth());
 
 								coordinates.setxCoordinate("" + (int) rectangle.getX());
 								coordinates.setyCoordinate("" + (int) rectangle.getY());
 								nodeGraphicsInfo.setCoordinates(coordinates);
 
 								nodeGraphicsInfos.add2List(nodeGraphicsInfo);
 								xpdlActivity2.setNodeGraphicsInfos(nodeGraphicsInfos);
 							}
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * @param bpmn
 	 * @param vendorSettings
 	 */
 	private void fillXpdlActivities() {
 
 		List<XpdlActivity> activityList = new ArrayList<XpdlActivity>();
 		// XpdlImplementation, XpdlDocumentation, XpdlDescription, XpdlLimit is excluded
 		fillActivities(activityList, null);
 		fillEvents(activityList, null);
 		fillGateways(activityList, null);
 
 		XpdlWorkflowProcess mainWorkflowProcess = xpdl.getWorkflowProcesses().getList().get(0);
 
 		for (SubProcess subProcess : bpmn.getSubProcesses()) {
 			// define a subprocess as a sub flow activity in the main process
 			XpdlActivity xpdlActivity = new XpdlActivity("Activity");
 			xpdlActivity.setId("" + subProcess.hashCode());
 			xpdlActivity.setName(subProcess.getLabel());
 
 			XpdlBlockActivity activityBlockActivity = new XpdlBlockActivity("BlockActivity");
 			activityBlockActivity.setActivitySetId("SubProcess-SUBWF" + subProcess.getId());
 			activityBlockActivity.setView("EXPANDED");
 
 			xpdlActivity.setBlockActivity(activityBlockActivity);
 			activityList.add(xpdlActivity);
 
 			// create workflow process for each subprocess
 			List<XpdlActivity> tempActivityList = new ArrayList<XpdlActivity>();
 			fillActivities(tempActivityList, subProcess);
 			fillEvents(tempActivityList, subProcess);
 			fillGateways(tempActivityList, subProcess);
 			XpdlActivitySet xpdlActivitySet = null;
 
 			for (XpdlActivitySet tempXpdlActivitySet : mainWorkflowProcess.getActivitySets().getList()) {
 				if (tempXpdlActivitySet.getId().equals("SubProcess-SUBWF" + subProcess.getId())) {
 					xpdlActivitySet = tempXpdlActivitySet;
 					break;
 				}
 			}
 			xpdlActivitySet.getActivities().setList(tempActivityList);
 			mainWorkflowProcess.getActivitySets().add2List(xpdlActivitySet);
 		}
 		mainWorkflowProcess.getActivities().setList(activityList);
 	}
 
 	/**
 	 * @param bpmn
 	 * @param vendorSettings
 	 * @param activityList
 	 */
 	private void fillGateways(List<XpdlActivity> activityList, SubProcess subprocess) {
 		for (Gateway gateway : bpmn.getGateways()) {
 			if (gateway.getParentSubProcess() == subprocess) {
 				XpdlActivity xpdlActivity = new XpdlActivity("Activity");
 				xpdlActivity.setId("" + gateway.hashCode());
 				xpdlActivity.setName(gateway.getLabel());
 
 				XpdlRoute route = new XpdlRoute("Route");
 				String gatewayTypeStr = getGatewayType(gateway.getGatewayType());
 				route.setGatewayType(gatewayTypeStr);
				setMarkerVendorSpecific(bpmn.getVendorSpecificSettings(), gateway, route);
 				xpdlActivity.setRoute(route);
 
 				XpdlTransitionRestrictions transitionRestrictions = new XpdlTransitionRestrictions(
 						"TransitionRestrictions");
 				XpdlTransitionRestriction transitionRestriction = new XpdlTransitionRestriction("TransitionRestriction");
 
 				XpdlTransitionRefs transitionRefs = new XpdlTransitionRefs("TransitionRefs");
 
 				if (isSplit(bpmn, gateway)) {
 
 					for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> gatewayOutEdge : bpmn.getOutEdges(gateway)) {
 						XpdlTransitionRef transitionRef = new XpdlTransitionRef("TransitionRef");
 						transitionRef.setId("" + gatewayOutEdge.hashCode());
 						transitionRef.setName("" + gatewayOutEdge.hashCode());
 						transitionRefs.add2List(transitionRef);
 					}
 
 					XpdlSplit xpdlSplit = new XpdlSplit("Split");
 					xpdlSplit.setType(gatewayTypeStr);
 					xpdlSplit.setTransitionRefs(transitionRefs);
 					transitionRestriction.setSplit(xpdlSplit);
 
 				} else {
 					XpdlJoin xpdlJoin = new XpdlJoin("Join");
 					xpdlJoin.setType(gatewayTypeStr);
 					transitionRestriction.setJoin(xpdlJoin);
 				}
 				transitionRestrictions.add2List(transitionRestriction);
 				xpdlActivity.setTransitionRestrictions(transitionRestrictions);
 				activityList.add(xpdlActivity);
 			}
 
 		}
 	}
 
 	/**
 	 * @param bpmn
 	 * @param activityList
 	 */
 	private void fillEvents(List<XpdlActivity> activityList, SubProcess subprocess) {
 		for (Event event : bpmn.getEvents()) {
 			if (event.getParentSubProcess() == subprocess) {
 				XpdlActivity xpdlActivity = new XpdlActivity("Activity");
 				xpdlActivity.setId("" + event.hashCode());
 				xpdlActivity.setName(event.getLabel());
 
 				XpdlEvent e1 = null;
 				if (event.getEventType().equals(EventType.END)) {
 					e1 = new XpdlEvent("Event");
 					XpdlEndEvent endEvent = null;
 					endEvent = new XpdlEndEvent("EndEvent");
 					e1.setEndEvent(endEvent);
 				} else if (event.getEventType().equals(EventType.START)) {
 					e1 = new XpdlEvent("Event");
 					XpdlStartEvent startEvent = null;
 					startEvent = new XpdlStartEvent("StartEvent");
 					if (event.getEventTrigger() != null) {
 						startEvent.setTrigger(event.getEventTrigger().name());
 					} else {
 						startEvent.setTrigger("None");
 					}
 					e1.setStartEvent(startEvent);
 				} else if (event.getEventType().equals(EventType.INTERMEDIATE)) {
 					e1 = new XpdlEvent("Event");
 					XpdlIntermediateEvent intermediateEvent = null;
 					intermediateEvent = new XpdlIntermediateEvent("IntermediateEvent");
 					if (event.getEventTrigger() != null) {
 						intermediateEvent.setTrigger(event.getEventTrigger().name());
 					} else {
 						intermediateEvent.setTrigger("None");
 					}
 					e1.setIntermediateEvent(intermediateEvent);
 				}
 
 				xpdlActivity.setEvent(e1);
 				activityList.add(xpdlActivity);
 			}
 
 		}
 	}
 
 	/**
 	 * @param bpmn
 	 * @param activityList
 	 */
 	private void fillActivities(List<XpdlActivity> activityList, SubProcess subprocess) {
 		for (Activity activity : bpmn.getActivities()) {
 			if (activity.getParentSubProcess() == subprocess) {
 				XpdlActivity xpdlActivity = new XpdlActivity("Activity");
 				xpdlActivity.setId("" + activity.hashCode());
 				xpdlActivity.setName(activity.getLabel());
 				activityList.add(xpdlActivity);
 			}
 		}
 	}
 
 	/**
 	 * @param bpmn
 	 * @param gateway
 	 * @return
 	 */
 	private boolean isSplit(BPMNDiagram bpmn, Gateway gateway) {
 		boolean isSplit = true;
 		isSplit = (bpmn.getOutEdges(gateway).size() > 1);
 		return isSplit;
 	}
 
 	/**
 	 * @param vendorSettings
 	 * @param gateway
 	 * @param route
 	 */
	private void setMarkerVendorSpecific(BPMNVendorSettings vendorSettings, Gateway gateway, XpdlRoute route) {
 
 		if (gateway.getGatewayType().equals(GatewayType.INCLUSIVE)) {
 			route.setMarkerVisible(""+gateway.isMarkerVisible());
 		} else if (gateway.getGatewayType().equals(GatewayType.DATABASED)) {
 			route.setMarkerVisible(""+gateway.isMarkerVisible());
 		} else {
 			route.setMarkerVisible(""+gateway.isMarkerVisible());
 		}
 	}
 
 	/**
 	 * @param activityNameStr
 	 * @return
 	 */
 	private String getGatewayType(GatewayType type) {
 		String typeStr = null;
 		if (type.equals(GatewayType.EVENTBASED) || type.equals(GatewayType.DATABASED)) {
 			typeStr = "XOR";
 		} else if (type.equals(GatewayType.PARALLEL)) {
 			typeStr = "AND";
 		} else if (type.equals(GatewayType.INCLUSIVE)) {
 			typeStr = "OR";
 		}
 		return typeStr;
 	}
 
 	/**
 	 * @param bpmn
 	 * @param xpdl
 	 */
 	private void fillXpdlTransitions() {
 		for (Flow flow : bpmn.getFlows()) {
 
 			XpdlTransition t = new XpdlTransition("Transition");
 			XpdlConnectorGraphicsInfos tConnectorGraphicsInfos = new XpdlConnectorGraphicsInfos(
 					"ConnectorGraphicsInfos");
 			XpdlConnectorGraphicsInfo tConnectorGraphicsInfo = new XpdlConnectorGraphicsInfo("ConnectorGraphicsInfo");
 
 			t.setFrom("" + flow.getSource().hashCode());
 			t.setTo("" + flow.getTarget().hashCode());
 			t.setId("" + flow.hashCode());
 
 			tConnectorGraphicsInfo.setToolId("ProM");
 			tConnectorGraphicsInfo.setBorderColor("0");
 			tConnectorGraphicsInfos.add2List(tConnectorGraphicsInfo);
 			t.setConnectorGraphicsInfos(tConnectorGraphicsInfos);
 
 			XpdlWorkflowProcess mainWorkflowProcess = xpdl.getWorkflowProcesses().getList().get(0);
 
 			if (flow.getParentSubProcess() == null) {
 				// transition is belong to main process
 				mainWorkflowProcess.getTransitions().add2List(t);
 			} else {
 				// transition is belong to subprocess's activity set
 				XpdlActivitySet activitySet = null;
 				for (XpdlActivitySet tempActivitySet : mainWorkflowProcess.getActivitySets().getList()) {
 					if (tempActivitySet.getId().equals("SubProcess-SUBWF" + flow.getParentSubProcess().getId())) {
 						activitySet = tempActivitySet;
 						break;
 					}
 				}
 				activitySet.getTransitions().add2List(t);
 
 			}
 		}
 
 	}
 
 	/**
 	 * @param xpdl
 	 * @return
 	 */
 	private Xpdl fillXPDL() {
 		// TODO No participant study is done
 		// TODO No datatype study is done
 		// TODO No extension study is done
 
 		Xpdl xpdl = new Xpdl();
 
 		// XPDL PACKAGE HEADER
 		XpdlPackageHeader packageHeader = setPackageHeader();
 
 		XpdlPools xpdlPools = new XpdlPools("Pools");
 
 		// XPDL WORKFLOW PROCESSES
 		XpdlWorkflowProcesses xpdlWorkflowProcesses = new XpdlWorkflowProcesses("WorkflowProcesses");
 
 		// XPDL MAIN WORKFLOW PROCESS
 		XpdlWorkflowProcess w1 = new XpdlWorkflowProcess("WorkflowProcess");
 		w1.setId("MainProcess-WF" + bpmn.getLabel());
 		w1.setName(bpmn.getLabel());
 		w1.setAccessLevel("PUBLIC");
 
 		// XPDL WORKFLOW PROCESS HEADER
 		XpdlProcessHeader processHeader = setProcessHeader();
 		w1.setProcessHeader(processHeader);
 
 		// XPDL WORKFLOW PROCESS REDEFINABLE HEADER
 		XpdlRedefinableHeader redefinableHeader = setRedefinableHeader();
 		w1.setRedefinableHeader(redefinableHeader);
 
 		// XPDL WORKFLOW PROCESS PARTICIPANTS
 		HashMap<String, String> participantAndType = new HashMap<String, String>();
 		participantAndType.put("currentOwner", "ROLE");
 		XpdlParticipants participants = setParticipants(participantAndType);
 		w1.setParticipants(participants);
 
 		// XPDL WORKFLOW PROCESS ACTIVITY SETS FOR SUBPROCESSES
 		XpdlActivitySets activitySets = new XpdlActivitySets("ActivitySets");
 		List<XpdlActivitySet> activitySetList = new ArrayList<XpdlActivitySet>();
 		for (SubProcess subProcess : bpmn.getSubProcesses()) {
 			XpdlActivitySet xpdlActivitySet = new XpdlActivitySet("ActivitySet");
 			xpdlActivitySet.setId("SubProcess-SUBWF " + subProcess.getId());
 			xpdlActivitySet.setName(subProcess.getLabel());
 
 			// XPDL ACTIVITIES FOR SUBPROCESS
 			XpdlActivities xpdlActivities = new XpdlActivities("Activities");
 			List<XpdlActivity> activities = new ArrayList<XpdlActivity>();
 			xpdlActivities.setList(activities);
 			xpdlActivitySet.setActivities(xpdlActivities);
 
 			// XPDL WORKFLOW PROCESS TRANSITIONS FOR SUBPROCESS
 			XpdlTransitions transitions = new XpdlTransitions("Transitions");
 			List<XpdlTransition> xpdlTransitionList = new ArrayList<XpdlTransition>();
 			transitions.setList(xpdlTransitionList);
 			xpdlActivitySet.setTransitions(transitions);
 
 			activitySetList.add(xpdlActivitySet);
 		}
 		activitySets.setList(activitySetList);
 		w1.setActivitySets(activitySets);
 
 		// XPDL ACTIVITIES
 		XpdlActivities mainXpdlActivities = new XpdlActivities("Activities");
 		List<XpdlActivity> activities = new ArrayList<XpdlActivity>();
 		mainXpdlActivities.setList(activities);
 		w1.setActivities(mainXpdlActivities);
 
 		// XPDL WORKFLOW PROCESS TRANSITIONS
 		XpdlTransitions mainXpdlTransitions = new XpdlTransitions("Transitions");
 		List<XpdlTransition> xpdlTransitionList = new ArrayList<XpdlTransition>();
 		mainXpdlTransitions.setList(xpdlTransitionList);
 		w1.setTransitions(mainXpdlTransitions);
 
 		xpdlWorkflowProcesses.add2List(w1);
 
 		// FILL XPDL SETTINGS
 		xpdl.setId("XPDL" + bpmn.getLabel());
 		xpdl.setName(bpmn.getLabel());
 		xpdl.setPackageHeader(packageHeader);
 		xpdl.setPools(xpdlPools);
 		xpdl.setWorkflowProcesses(xpdlWorkflowProcesses);
 
 		return xpdl;
 	}
 
 	private XpdlParticipants setParticipants(HashMap<String, String> participantAndType) {
 		XpdlParticipants participants = new XpdlParticipants("XpdlParticipants");
 
 		for (String tempParticipant : participantAndType.keySet()) {
 			XpdlParticipant participant = new XpdlParticipant("Participant");
 			participant.setId(tempParticipant);
 			XpdlParticipantType participantType = new XpdlParticipantType("ParticipantType");
 			participantType.setType(participantAndType.get(tempParticipant));
 			participant.setParticipantType(participantType);
 			participants.add2List(participant);
 		}
 		return participants;
 	}
 
 	private XpdlRedefinableHeader setRedefinableHeader() {
 		XpdlRedefinableHeader redefinableHeader = new XpdlRedefinableHeader("RedefinableHeader");
 
 		XpdlAuthor author = new XpdlAuthor("Author");
 		XpdlText authorXpdlText = new XpdlText("XpdlText");
 		authorXpdlText.setText("ProM");
 		author.setXpdlText(authorXpdlText);
 		redefinableHeader.setAuthor(author);
 
 		XpdlVersion version = new XpdlVersion("Version");
 		XpdlText versionXpdlText = new XpdlText("XpdlText");
 		versionXpdlText.setText("2.0");
 		version.setXpdlText(versionXpdlText);
 		redefinableHeader.setVersion(version);
 		return redefinableHeader;
 	}
 
 	private XpdlProcessHeader setProcessHeader() {
 		XpdlProcessHeader processHeader = new XpdlProcessHeader("ProcessHeader");
 
 		XpdlCreated processHeaderCreated = new XpdlCreated("Created");
 		String processHeaderCreateDate = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date());
 
 		XpdlText processHeaderCreateXpdlText = new XpdlText("XpdlText");
 		processHeaderCreateXpdlText.setText(processHeaderCreateDate);
 		processHeaderCreated.setXpdlText(processHeaderCreateXpdlText);
 
 		XpdlPriority processHeaderXpdlPriority = new XpdlPriority("Priority");
 		XpdlText processHeaderPriorityXpdlText = new XpdlText("XpdlText");
 		processHeaderPriorityXpdlText.setText("Normal");
 		processHeaderXpdlPriority.setXpdlText(processHeaderPriorityXpdlText);
 
 		processHeader.setCreated(processHeaderCreated);
 		processHeader.setPriority(processHeaderXpdlPriority);
 		return processHeader;
 	}
 
 	/**
 	 * @param packageHeaderVersion
 	 * @param packageHeaderVendor
 	 * @param packageHeaderToday
 	 * @param packageHeaderDescription
 	 * @param packageHeaderDocumentation
 	 * @return
 	 */
 	private XpdlPackageHeader setPackageHeader() {
 
 		// SET PACKAGE HEADER VARIABLES
 		String packageHeaderVersion = "2.0";
 		String packageHeaderVendor = "ProM";
 		Date packageHeaderToday = new Date();
 		String packageHeaderDescription = "ProM Mined BPMN Model";
 		String packageHeaderDocumentation = "ProM Mined BPMN Model";
 
 		XpdlPackageHeader packageHeader = new XpdlPackageHeader("PackageHeader");
 
 		XpdlXpdlVersion xpdlXpdlVersion = new XpdlXpdlVersion("XPDLVersion");
 		XpdlText versionText = new XpdlText("XpdlText");
 		versionText.setText(packageHeaderVersion);
 		xpdlXpdlVersion.setXpdlText(versionText);
 
 		XpdlVendor xpdlVendor = new XpdlVendor("Vendor");
 		XpdlText xpdlVendorXpdlText = new XpdlText("XpdlText");
 		xpdlVendorXpdlText.setText(packageHeaderVendor);
 		xpdlVendor.setXpdlText(xpdlVendorXpdlText);
 
 		XpdlCreated created = new XpdlCreated("Created");
 		String now = new SimpleDateFormat("dd/MM/yyyy hh:mm").format(packageHeaderToday);
 
 		XpdlText createdXpdlText = new XpdlText("XpdlText");
 		createdXpdlText.setText(now);
 		created.setXpdlText(createdXpdlText);
 
 		XpdlDescription xpdlDescription = new XpdlDescription("Description");
 		XpdlText descriptionXpdlText = new XpdlText("XpdlText");
 		descriptionXpdlText.setText(packageHeaderDescription);
 		xpdlDescription.setXpdlText(descriptionXpdlText);
 
 		XpdlDocumentation xpdlDocumentation = new XpdlDocumentation("Documentation");
 		XpdlText documentationXpdlText = new XpdlText("XpdlText");
 		documentationXpdlText.setText(packageHeaderDocumentation);
 		xpdlDocumentation.setXpdlText(documentationXpdlText);
 
 		packageHeader.setVersion(xpdlXpdlVersion);
 		packageHeader.setVendor(xpdlVendor);
 		packageHeader.setCreated(created);
 		packageHeader.setDescription(xpdlDescription);
 		packageHeader.setDocumentation(xpdlDocumentation);
 		return packageHeader;
 	}
 
 }

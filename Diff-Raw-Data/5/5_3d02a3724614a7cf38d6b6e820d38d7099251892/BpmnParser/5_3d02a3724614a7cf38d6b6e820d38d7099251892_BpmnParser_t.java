 /**
  * PromniCAT - Collection and Analysis of Business Process Models
  * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.uni_potsdam.hpi.bpt.promnicat.parser;
 
 import java.util.AbstractMap;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.jbpt.hypergraph.abs.Vertex;
 import org.jbpt.pm.AndGateway;
 import org.jbpt.pm.DataNode;
 import org.jbpt.pm.FlowNode;
 import org.jbpt.pm.NonFlowNode;
 import org.jbpt.pm.OrGateway;
 import org.jbpt.pm.ProcessModel;
 import org.jbpt.pm.Resource;
 import org.jbpt.pm.XorGateway;
 import org.jbpt.pm.bpmn.AlternativeGateway;
 import org.jbpt.pm.bpmn.Bpmn;
 import org.jbpt.pm.bpmn.BpmnActivity;
 import org.jbpt.pm.bpmn.BpmnControlFlow;
 import org.jbpt.pm.bpmn.BpmnEvent;
 import org.jbpt.pm.bpmn.BpmnEventTypes;
 import org.jbpt.pm.bpmn.BpmnMessageFlow;
 import org.jbpt.pm.bpmn.BpmnResource;
 import org.jbpt.pm.bpmn.CatchingEvent;
 import org.jbpt.pm.bpmn.Document;
 import org.jbpt.pm.bpmn.EndEvent;
 import org.jbpt.pm.bpmn.EventBasedXorGateway;
 import org.jbpt.pm.bpmn.StartEvent;
 import org.jbpt.pm.bpmn.Subprocess;
 import org.jbpt.pm.bpmn.Task;
 import org.jbpt.pm.bpmn.ThrowingEvent;
 
 import de.uni_potsdam.hpi.bpt.ai.diagram.Diagram;
 import de.uni_potsdam.hpi.bpt.ai.diagram.Shape;
 
 /**
  * Retrieves a Diagram instance (JSON format) of the BPMN process model and maps it
  * on jBPT. 
  * @author Cindy Fähnrich
  *
  */
 public class BpmnParser implements IParser {
 
 	private final static Logger logger = Logger.getLogger(BpmnParser.class.getName());
 	public Bpmn1_1Constants constants;
 	public Diagram diagram = null;
 	public Bpmn<BpmnControlFlow<FlowNode>, FlowNode> process = null;
 	public boolean strictness = false;
 	public boolean error = false;
 	
 	/**
 	 * Index for the different JSON ids of nodes(needed for edge construction)
 	 */
 	public HashMap<String, Entry<Object, Subprocess>> nodeIds = new HashMap<String, Entry<Object, Subprocess>>();
 	
 	/**
 	 * Index for the different JSON ids of control flows (needed for association construction)
 	 */
 	public HashMap<String, BpmnControlFlow<FlowNode>> controlflowIds = new HashMap<String, BpmnControlFlow<FlowNode>>();
 	
 	/**
 	 * Index for all attached events and the control flows they are attached to
 	 */
 	public HashMap<String, BpmnEvent> attachedEvents = new HashMap<String, BpmnEvent>();
 	/**
 	 * Index for the different JSON ids of sequence flows (needed for association construction)
 	 */
 	public HashMap<String, BpmnMessageFlow> messageflowIds = new HashMap<String, BpmnMessageFlow>();
 	
 	/**
 	 * Contains all message and sequence flow edges
 	 */
 	public Vector<Shape> flows = new Vector<Shape>();
 	
 	/**
 	 * Contains all association edges - needed because assocs are created at the end
 	 */
 	public Vector<Shape> assocs = new Vector<Shape>();
 	
 	/**
 	 * Set the constants set in the constructor
 	 * @param constants
 	 */
 	public BpmnParser(Bpmn1_1Constants constants, boolean strictness) {
 		this.strictness = strictness;
 		this.constants = constants;
 	}
 	
 	/**
 	 * Clear the instance variables, in case the parser has to parse a new diagram
 	 */
 	public void clear(){
 		this.assocs.clear();
 		this.attachedEvents.clear();
 		this.controlflowIds.clear();
 		this.diagram = null;
 		this.flows.clear();
 		this.process = new Bpmn<BpmnControlFlow<FlowNode>, FlowNode>();
 		this.messageflowIds.clear();
 		this.nodeIds.clear();
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.uni_potsdam.hpi.bpt.promnicat.parser.IParser#transformProcess(de.uni_potsdam.hpi.bpt.ai.diagram.Diagram)
 	 */
 	@Override
 	public ProcessModel transformProcess(Diagram diagram) {
 		clear();
 		this.process.setName(diagram.getProperty(constants.PROPERTY_TITLE));
 		
 		//invoke transformation of nodes
 		List<Shape> shapes = diagram.getChildShapes();
 		transformShapes(shapes);
 		
 		//create flows at last here, since the connected nodes may be parsed later than the actual relation otherwise
 		for (Shape s : flows){
 			createFlows(s);
 		}
 		
 		for (Shape s : assocs){
 			createAssociation(s);
 		}
 		
 		for (String key : attachedEvents.keySet()){
 			BpmnControlFlow<FlowNode> flow = this.controlflowIds.get(key);
 			BpmnEvent event = this.attachedEvents.get(key);
 			if (flow != null && event != null) {
 				flow.attachEvent(event);
 			}
 		}
 		if (error){
 			return null;
 		}
 		return this.process;
 	}
 	
 	/**
 	 * Create Message and Sequence Flows
 	 * @param s the flow shape
 	 */
 	public void createFlows(Shape s){
 		String id = s.getStencilId();
 		if (id.contains(constants.ENTITY_MESSAGEFLOW)){
 			createMessageFlow(s);
 		} else {//otherwise it is a sequence flow
 			createSequenceFlow(s);
 		}
 	}
 
 	/**
 	 * Recursive method for parsing all subshapes of a shape. For BPMN models, the nesting of subshapes has
 	 * a random depth.
 	 * @param sList List of Shapes to transform
 	 */
 	public void transformShapes(List<Shape> sList){
 		for (Shape subshape : sList){
 			if (subshape.getChildShapes().size() != 0){//invoke recursion if shape has childshapes
 				transformShapes(subshape.getChildShapes());
 			} 
 			Vertex node = parseIds(subshape);
 			if (node != null && node instanceof Subprocess){
 				addChildNodes(subshape, (Subprocess)node);
 			}
 		}
 	}
 	
 	/**
 	 * Adds all the nodes contained in a subprocess to it
 	 * @param s the subprocess shape
 	 * @param proc the jBPT process model
 	 */
 	public void addChildNodes(Shape s, Subprocess proc){
 		for (Shape subs : s.getChildShapes()){
 			String id = subs.getResourceId();
 			Entry<Object, Subprocess> tuple = nodeIds.get(id);
 			if (tuple != null){
 				Object node = tuple.getKey();
 				if (node instanceof FlowNode){
 					proc.addFlowNode((FlowNode) node);
 					this.process.removeFlowNode((FlowNode) node);
 				} else {
 					if (node instanceof NonFlowNode){//Resources are not mapped here, since they are only lanes/pools that can not occur in subprocesses
 						proc.addNonFlowNode((NonFlowNode) node);
 						this.process.removeNonFlowNode((NonFlowNode) node);
 					}
 				}
 				tuple.setValue(proc); //add its super-process
 			}	
 		}
 	}
 	
 	/**
 	 * Parses the given id of the shape and maps it to the corresponding Jbpt element by invoking the creation
 	 * method.
 	 * @param s shape to map
 	 */
 	public Vertex parseIds(Shape s){
 		String id = s.getStencilId();
 		
 		if (id.contains(constants.ENTITY_TASK)){
 			return createTask(s);
 			}
 		if (id.contains(constants.ENTITY_SUBPROCESS)){
 			return createSubprocess(s);
 		}
 		if (id.contains(constants.ENTITY_GATEWAY_XOR)){
 			return createXorGateway(s);
 		}
 		if (id.contains(constants.ENTITY_GATEWAY_AND)){
 			return createAndGateway(s);
 		}
 		if (id.contains(constants.ENTITY_GATEWAY_OR)){
 			return createOrGateway(s);
 		}
 		if (id.contains(constants.ENTITY_GATEWAY_ALTERNATIVE)){
 			return createAlternativeGateway(s);
 		}
 		if (id.contains(constants.ENTITY_GATEWAY_EVENTBASED)){
 			return createEventbasedGateway(s);
 		}
 		if (id.contains(constants.ENTITY_LANE) || id.contains(constants.ENTITY_POOL)){
 			return createResource(s);
 		}
 		if (id.contains(constants.ENTITY_DATA)){
 			return createDocument(s);
 		}
 		if (id.contains(constants.ENTITY_EVENT_START)){
 			return createStartEvent(s);
 		}
 		if (id.contains(constants.EVENT_END)){
 			return createEndEvent(s);
 		}
 		if (id.contains(constants.ENTITY_EVENT_THROWING)){
 			return createIntermediateThrowingEvent(s);
 		}
 		if (id.contains(constants.ENTITY_EVENT_CATCHING) || id.contains(constants.ENTITY_EVENT_INTERMEDIATE)){
 			return createIntermediateCatchingEvent(s);
 		}
 		if (id.contains(constants.ENTITY_SEQUENCEFLOW) || id.contains(constants.ENTITY_MESSAGEFLOW)){
 			flows.add(s);
 			return null;
 		}
 		if (id.contains(constants.ENTITY_ASSOCIATION)){
 			assocs.add(s);
 		}	
 		return null;
 	}
 	
 	/**
 	 * Creates a {@link Task} object
 	 * @param s the shape of the task
 	 * @return the jBPT {@link Task} object
 	 */
 	public Vertex createTask(Shape s){
 		Task f = new Task();
 		prepareNode(s, f);
 		prepareActivity(s,f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates a {@link Subprocess} object
 	 * @param s the shape of the subprocess
 	 * @return the jBPT {@link Subprocess} object
 	 */
 	public Vertex createSubprocess(Shape s){
 		Subprocess f = new Subprocess();
 		prepareNode(s, f);
 		prepareActivity(s,f);
 		if (s.getStencilId().contains(constants.ENTITY_SUBPROCESS_COLLAPSED)){
 			f.setCollapsed(true);
 		}
 		//event-subprocess - only relevant for bpmn 2.0
 		if (s.getStencilId().contains(constants.ENTITY_SUBPROCESS_EVENT)){
 			f.setEventDriven(true);
 		}
 		//check if process is adhoc
 		String prop = s.getProperty(constants.PROPERTY_ISADHOC);
 		if ((prop != null) && (prop.equals(constants.VALUE_TRUE))){ //this is an adhoc process
 			prop = s.getProperty(constants.PROPERTY_ADHOC_ORDER);
 			if (prop.equals(constants.VALUE_SEQUENTIAL)){
 				f.setSequentialAdhoc();
 			} else {
 				f.setParallelAdhoc();
 			}
 		}
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates a {@link XorGateway} object
 	 * @param s the shape of the XorGateway
 	 * @return the jBPT {@link XorGateway} object
 	 */
 	public Vertex createXorGateway(Shape s){
 		XorGateway f = new XorGateway();
 		prepareNode(s, f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates an {@link AndGateway} object
 	 * @param s the shape of the AndGateway
 	 * @return the jBPT {@link AndGateway} object
 	 */
 	public Vertex createAndGateway(Shape s){
 		AndGateway f = new AndGateway();
 		prepareNode(s, f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates an {@link OrGateway} object
 	 * @param s the shape of the OrGateway
 	 * @return the jBPT {@link OrGateway} object
 	 */
 	public Vertex createOrGateway(Shape s){
 		OrGateway f = new OrGateway();
 		prepareNode(s, f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates an {@link AlternativeGateway} object
 	 * @param s the shape of the AlternativeGateway
 	 * @return the jBPT {@link AlternativeGateway} object
 	 */
 	public Vertex createAlternativeGateway(Shape s){
 		AlternativeGateway f = new AlternativeGateway();
 		prepareNode(s, f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates an {@link EventBasedXorGateway} object
 	 * @param s the shape of the EventBasedXorGateway
 	 * @return the jBPT {@link EventBasedXorGateway} object
 	 */
 	public Vertex createEventbasedGateway(Shape s){
 		EventBasedXorGateway f = new EventBasedXorGateway();
 		prepareNode(s, f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates a {@link BpmnResource} object
 	 * @param s the shape of the BpmnResource
 	 * @return the jBPT {@link BpmnResource} object
 	 */
 	public Vertex createResource(Shape s){
 		BpmnResource f = new BpmnResource();
 		f.setType(s.getStencilId());
 
 		f.setName(s.getProperty(constants.PROPERTY_NAME));
 		f.setDescription(s.getProperty(constants.PROPERTY_DESCRIPTION));
 		
 		//add id to map
 		this.nodeIds.put(s.getResourceId(), new AbstractMap.SimpleEntry<Object, Subprocess>(f, null));
 		
 		for (Shape subs : s.getChildShapes()){
 			String id = subs.getResourceId();
 			Entry<Object, Subprocess> node = nodeIds.get(id);
 			if (node.getKey() instanceof FlowNode){
				((FlowNode) node.getKey()).addResource(f);
 			} else {
 				if (node.getKey() instanceof Resource){
					((Resource) node.getKey()).setResource(f);
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Creates a {@link Document} object
 	 * @param s the shape of the Document
 	 * @return the jBPT {@link Document} object
 	 */
 	public Vertex createDocument(Shape s){
 		Document f = new Document();
 		prepareNode(s, f);
 		this.process.addNonFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates a {@link StartEvent} object
 	 * @param s the shape of the StartEvent
 	 * @return the jBPT {@link StartEvent} object
 	 */
 	public Vertex createStartEvent(Shape s){
 		StartEvent f = new StartEvent();
 		prepareNode(s, f);
 		prepareEvent(s,f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates an {@link EndEvent} object
 	 * @param s the shape of the EndEvent
 	 * @return the jBPT {@link EndEvent} object
 	 */
 	public Vertex createEndEvent(Shape s){
 		EndEvent f = new EndEvent();
 		prepareNode(s, f);
 		prepareEvent(s,f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates a {@link CatchingEvent} object
 	 * @param s the shape of the CatchingEvent
 	 * @return the jBPT {@link CatchingEvent} object
 	 */
 	public Vertex createIntermediateCatchingEvent(Shape s){
 		CatchingEvent f = new CatchingEvent();
 		prepareNode(s, f);
 		prepareEvent(s,f);
 		checkForAttached(s,f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates a {@link ThrowingEvent} object
 	 * @param s the shape of the ThrowingEvent
 	 * @return the jBPT {@link ThrowingEvent} object
 	 */
 	public Vertex createIntermediateThrowingEvent(Shape s){
 		ThrowingEvent f = new ThrowingEvent();
 		prepareNode(s, f);
 		prepareEvent(s,f);
 		checkForAttached(s,f);
 		this.process.addFlowNode(f);
 		return f;
 	}
 	
 	/**
 	 * Creates a {@link BpmnControlFlow} object as sequence flow
 	 * @param s the shape of the BpmnControlFlow
 	 */
 	public void createSequenceFlow(Shape s){
 		//find the connected nodes
 		if (s.getIncomings().isEmpty() && !this.strictness){
 			logger.warning("Created control flow with JSON ID " + s.getResourceId() + " does not have an incoming node!");
 			return;
 		}  else if (s.getIncomings().isEmpty() && this.strictness){
 			this.error = true;
 			logger.warning("Will return null for this process model. Created control flow with JSON ID " + s.getResourceId() + " does not have an incoming node!");
 			return;
 		}
 		if (s.getOutgoings().isEmpty()&& !this.strictness){
 			logger.warning("Created control flow with JSON ID " + s.getResourceId() + " does not have an outgoing node!");
 			return;
 		}  else if (s.getOutgoings().isEmpty() && this.strictness){
 			this.error = true;
 			logger.warning("Will return null for this process model. Created control flow with JSON ID " + s.getResourceId() + " does not have an outgoing node!");
 			return;
 		}
 		
 		Shape in = s.getIncomings().get(0);
 		Shape out = s.getOutgoings().get(0);
 			
 		if (nodeIds.get(in.getResourceId()) == null){
 			logger.warning("Created control flow with JSON ID " + s.getResourceId() + " does not have an incoming node, since this was a notation element we do not map.");
 			return;
 		}
 			
 		if (nodeIds.get(out.getResourceId()) == null){
 			logger.warning("Created control flow with JSON ID " + s.getResourceId() + " does not have an outgoing node, since this was a notation element we do not map.");
 			return;
 		}
 		
 		//get the nodes for the resourceIds
 		Entry<Object, Subprocess> toNode = nodeIds.get(out.getResourceId());
 		Entry<Object, Subprocess> fromNode = nodeIds.get(in.getResourceId());
 				
 		String type = s.getProperty(constants.PROPERTY_CONDITION_TYPE);
 		boolean defaultFlow = false;
 		if (type.equals(constants.VALUE_DEFAULT)){ 
 			defaultFlow = true;
 		}
 		//check the control flow flow conditions and add the control flow
 		String expression = s.getProperty(constants.PROPERTY_CONDITION_EXPRESSION);
 		BpmnControlFlow<FlowNode> flow = null;
 		if (expression != "") {
 			if (toNode.getValue() != null && toNode.getValue() == fromNode.getValue()){
 				flow = toNode.getValue().addControlFlow((FlowNode)fromNode.getKey(), (FlowNode) toNode.getKey(), expression, defaultFlow);
 			} else {
 				if (toNode.getValue() == null && fromNode.getValue() == null){
 					flow = this.process.addControlFlow((FlowNode)fromNode.getKey(), (FlowNode) toNode.getKey(), expression, defaultFlow);
 				}
 			}
 		} else {
 			if (toNode.getValue() != null && toNode.getValue() == fromNode.getValue()){
 				flow = toNode.getValue().addControlFlow((FlowNode)fromNode.getKey(), (FlowNode) toNode.getKey(), defaultFlow);
 			} else {
 				if (toNode.getValue() == null && fromNode.getValue() == null){
 					flow = this.process.addControlFlow((FlowNode)fromNode.getKey(), (FlowNode) toNode.getKey(), defaultFlow);
 				}
 			}
 		}
 		this.controlflowIds.put(s.getResourceId(), flow);
 	}
 	
 	/**
 	 * Creates a {@link BpmnMessageFlow} object
 	 * @param s the shape of the BpmnMessageFlow
 	 */
 	public void createMessageFlow(Shape s){
 		//check whether association has no incoming or outgoing nodes
 		if (s.getIncomings().isEmpty() && !this.strictness){
 			logger.warning("Created message flow with JSON ID " + s.getResourceId() + " does not have an incoming node!");
 			return;
 		}  else if (s.getIncomings().isEmpty() && this.strictness){
 			this.error = true;
 			logger.warning("Will return null for this process model. Created message flow with JSON ID " + s.getResourceId() + " does not have an incoming node!");
 			return;
 		}
 	
 		if (s.getOutgoings().isEmpty() && !this.strictness){
 			logger.warning("Created message flow with JSON ID " + s.getResourceId() + " does not have an outgoing node!");
 			return;
 		} else if (s.getOutgoings().isEmpty() && this.strictness){
 			this.error = true;
 			logger.warning("Will return null for this process model. Created message flow with JSON ID " + s.getResourceId() + " does not have an outgoing node!");
 			
 			return;
 		}
 					
 		//get connected nodes
 		Shape in = s.getIncomings().get(0);
 		Shape out = s.getOutgoings().get(0);
 		
 		if (nodeIds.get(in.getResourceId()) == null){
 			logger.warning("Created message flow with JSON ID " + s.getResourceId() + " does not have an incoming node, since this was a notation element we do not map.");
 			return;
 		}
 			
 		if (nodeIds.get(out.getResourceId()) == null){
 			logger.warning("Created message flow with JSON ID " + s.getResourceId() + " does not have an outgoing node, since this was a notation element we do not map.");
 			return;
 		}
 		
 		Entry<Object, Subprocess> fromNode = nodeIds.get(in.getResourceId());
 		Entry<Object, Subprocess> toNode = nodeIds.get(out.getResourceId());
 		//check if nodes are contained in subprocess
 		BpmnMessageFlow flow;
 		if (toNode.getValue() != null && toNode.getValue() == fromNode.getValue()){
 			flow = toNode.getValue().addMessageFlow((Object)fromNode.getKey(), (Object) toNode.getKey());
 			this.process.addMessageFlow(flow);
 		} else {
 			if (toNode.getValue() == null && fromNode.getValue() != null){
 				flow = this.process.addMessageFlow((Object)fromNode.getKey(), (Object) toNode.getKey());
 			}
 		}
 	}
 	
 	/**
 	 * Creates an association from the given shape, by retrieving the incoming and outgoing nodes of that
 	 * association. Since it is binary, there is at most one incoming and one outgoing node.
 	 * @param s
 	 */
 	public void createAssociation(Shape s){
 		//check whether association has no incoming or outgoing nodes
 		if (s.getIncomings().isEmpty() && !this.strictness){
 			logger.warning("Created Relation with JSON ID " + s.getResourceId() + " does not have an incoming node!");
 			return;
 		} else if (s.getIncomings().isEmpty() && this.strictness){
 			this.error = true;
 			logger.warning("Will return null for this process model. Created association with JSON ID " + s.getResourceId() + " does not have an incoming node!");
 			return;
 		}
 		if (s.getOutgoings().isEmpty() && !this.strictness){
 			logger.warning("Created Relation with JSON ID " + s.getResourceId() + " does not have an outgoing node!");
 			return;
 		}  else if (s.getOutgoings().isEmpty() && this.strictness){
 			this.error = true;
 			logger.warning("Will return null for this process model. Created association with JSON ID " + s.getResourceId() + " does not have an outgoing node!");
 			return;
 		}
 			
 		//get connected nodes
 		Shape in = s.getIncomings().get(0);
 		Shape out = s.getOutgoings().get(0);
 			
 		//checks whether this is an association to an element we do not map - association is not created then
 		if (nodeIds.get(in.getResourceId()) == null){
 			logger.warning("Created Relation with JSON ID " + s.getResourceId() + " does not have an incoming node, since this was a notation element we do not map.");
 			return;
 		}
 				
 		if (nodeIds.get(out.getResourceId()) == null){
 			logger.warning("Created Relation with JSON ID " + s.getResourceId() + " does not have an outgoing node, since this was a notation element we do not map.");
 			return;
 		}
 		Object n = nodeIds.get(out.getResourceId()).getKey();
 		if (n instanceof NonFlowNode){
 			addAsAttribute(s, in, (NonFlowNode) nodeIds.get(out.getResourceId()).getKey());
 		} else {
 			if (this.strictness) {
 				error = true;
 			}
 			return;
 		}
 		
 		
 	}
 	
 	/**
 	 * Adds a NonFlowNode (BpmnResource or Document) as attribute to a FlowNode
 	 * @param s Shape that has the attribute
 	 * @param incoming Shape that s has as incoming
 	 * @param nonFlowN to be assigned
 	 */
 	public void addAsAttribute(Shape s, Shape incoming, NonFlowNode nonFlowN){
 		
 		Object n = nodeIds.get(incoming.getResourceId()).getKey();
 		if (!(n instanceof FlowNode)){
 			if (this.strictness) {
 				this.error = true;
 			}
 			return;
 		}
 		FlowNode flowN = (FlowNode)nodeIds.get(incoming.getResourceId()).getKey();
 		if (flowN != null){ //add it to the FlowNode
 			//add the data object
 			checkInformationFlow(s, flowN, (DataNode) nonFlowN);
 			
 		} else { //if the node has not been found, it potentially is a messageflow
 				if (messageflowIds.get(incoming.getResourceId()) != null){
 					//((BpmnMessageFlow<Vertex>)flow)
 					addAsMessageFlowAttribute(s, messageflowIds.get(incoming.getResourceId()), (DataNode) nonFlowN);
 				}
 				if (controlflowIds.get(incoming.getResourceId()) != null){
 					addAsControlFlowAttribute(s, controlflowIds.get(incoming.getResourceId()), (DataNode) nonFlowN);
 				}
 		}
 	}
 	
 	/**
 	 * Determines the kind of document (read/write/readwrite/unspecified)
 	 * @param s the association
 	 * @param node the flownode
 	 * @param document the document
 	 */
 	public void checkInformationFlow(Shape s, FlowNode node, DataNode document){
 		String id = s. getStencilId();
 		if (id.contains(constants.VALUE_UNDIRECTED)){
 			node.addUnspecifiedDocument(document);
 		}else if (id.contains(constants.VALUE_UNIDIRECTED)){
 			//FlowNode <-- Document is read 
 			Vertex outNode = (Vertex) nodeIds.get(s.getOutgoings().get(0).getResourceId()).getKey();
 			if (node == outNode){
 				node.addReadDocument(document);
 			} 
 			//FlowNode --> Document is write 
 			if (document == outNode){
 				node.addWriteDocument(document);
 			}
 		}else {//this is a bidirected edge
 			node.addReadWriteDocument(document);
 		}	
 	}
 		
 	/**
 	 * Determines the kind of document (read/write/readwrite/unspecified)
 	 * @param s the association
 	 * @param flow the ControlFlow
 	 * @param document the document
 	 */
 	public void addAsControlFlowAttribute(Shape s, BpmnControlFlow<FlowNode> flow, DataNode document){
 		String id = s. getResourceId();
 		if (id.contains(constants.VALUE_UNDIRECTED)){
 			flow.addUnspecifiedDocument(document);
 		}else if (id.contains(constants.VALUE_UNIDIRECTED)){
 			//FlowNode <-- Document is read 
 			if (flow == controlflowIds.get(s.getOutgoings().get(0))){
 				flow.addReadDocument(document);
 			} 
 			//FlowNode --> Document is write 
 			if (document == (Vertex) nodeIds.get(s.getOutgoings().get(0).getResourceId()).getKey()){
 				flow.addWriteDocument(document);
 			}
 		}else {//this is a bidirected edge
 			flow.addReadWriteDocument(document);
 		}	
 	}
 	
 	/**
 	 * Determines the kind of document (read/write/readwrite/unspecified)
 	 * @param s the association
 	 * @param flow the MessageFlow
 	 * @param document the document
 	 */
 	public void addAsMessageFlowAttribute(Shape s, BpmnMessageFlow flow, DataNode document){
 		String id = s. getStencilId();
 		if (id.contains(constants.VALUE_UNDIRECTED)){
 			flow.addUnspecifiedDocument(document);
 		}else if (id.contains(constants.VALUE_UNIDIRECTED)){
 			//FlowNode <-- Document is read 
 			if (flow == messageflowIds.get(s.getOutgoings().get(0))){
 				flow.addReadDocument(document);
 			} 
 			//FlowNode --> Document is write 
 			if (document == (Vertex) nodeIds.get(s.getOutgoings().get(0).getResourceId()).getKey()){
 				flow.addWriteDocument(document);
 			}
 		}else {//this is a bidirected edge
 			flow.addReadWriteDocument(document);
 		}	
 	}
 	
 	/**
 	 * Sets the loop types (simple or multiple instances) and compensation 
 	 * attributes of the activity
 	 * @param s current shape
 	 * @param activity jbpt class of shape
 	 */
 	private void prepareActivity(Shape s, BpmnActivity activity){
 
 		String prop = s.getProperty(constants.PROPERTY_ISCOMPENSATION);
 		if (prop != null && prop.equals(constants.VALUE_TRUE)){ //this is a compensation activity
 			activity.setCompensation(true);
 		}
 		
 		//check for looptype simple or multiple instance (parallel/sequential)
 		prop = s.getProperty(constants.PROPERTY_LOOPTYPE);
 		if (prop != null && prop.equals(constants.VALUE_STANDARD)){
 			activity.setStandardLoop(true);
 			return;
 		}
 		if (prop != null && prop.equals(constants.VALUE_NONE)){//do nothing more
 			return;
 		} 
 		//prop value must have been "MultiInstance"
 		if (constants instanceof Bpmn1_1Constants){
 			prop = s.getProperty(constants.PROPERTY_MI_ORDER);
 		}	
 		if ((prop != null) && (prop.equals(constants.VALUE_SEQUENTIAL))){
 			activity.setSequentialMultiple(true);
 			return;
 		} else {
 			activity.setParallelMultiple(true);
 			return;
 		}		
 	}
 	
 	/**
 	 * sets the event type
 	 * @param s
 	 * @param event
 	 */
 	private void prepareEvent(Shape s, BpmnEvent event){
 		//set type
 		String id = s.getStencilId();
 		Set<BpmnEventTypes.TYPES> typeSet = EnumSet.allOf(BpmnEventTypes.TYPES.class);  
 		for (BpmnEventTypes.TYPES type : typeSet){
 			if (id.contains(type.toString())){
 				event.setEventType(type);
 			}
 		}
 	}
 	
 	/**
 	 * checks whether the event is an attached event
 	 * @param s shape object of the event
 	 * @param event
 	 */
 	private void checkForAttached(Shape s, BpmnEvent event){
 		//check for attached
 				for (Shape in : s.getIncomings()){//find a BPMN activity as incoming to detect attached event
 					if (in.getStencilId().contains(constants.ENTITY_TASK) || in.getStencilId().contains(constants.ENTITY_SUBPROCESS)){
 						for (Shape out : s.getOutgoings()){//find the outgoing sequence flow to annotate event there
 							if (out.getStencilId().contains(constants.ENTITY_SEQUENCEFLOW)){
 								attachedEvents.put(out.getResourceId(), event);
 							}
 						}
 					}
 				}
 	}
 	
 	/**
 	 * sets the name and description of the given node and adds the node and its id to the nodeId map.
 	 * @param s
 	 * @param node
 	 */
 	private void prepareNode(Shape s, Vertex node){
 		node.setName(s.getProperty(constants.PROPERTY_NAME));
 		node.setDescription(s.getProperty(constants.PROPERTY_DESCRIPTION));
 		//add id to map		
 		this.nodeIds.put(s.getResourceId(), new AbstractMap.SimpleEntry<Object, Subprocess>(node, null));
 		
 	}
 
 }

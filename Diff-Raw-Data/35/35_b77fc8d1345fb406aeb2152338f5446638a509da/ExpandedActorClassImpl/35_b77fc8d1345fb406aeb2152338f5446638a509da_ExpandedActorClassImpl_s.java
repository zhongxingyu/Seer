 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.etrice.generator.etricegen.impl;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ContinuationTransition;
 import org.eclipse.etrice.core.room.DetailCode;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.MessageFromIf;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.StateGraphItem;
 import org.eclipse.etrice.core.room.StateGraphNode;
 import org.eclipse.etrice.core.room.Transition;
 import org.eclipse.etrice.core.room.TransitionTerminal;
 import org.eclipse.etrice.core.room.Trigger;
 import org.eclipse.etrice.core.room.impl.ActorClassImpl;
 import org.eclipse.etrice.core.room.util.RoomHelpers;
 import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
 import org.eclipse.etrice.core.naming.RoomNameProvider;
 
 import org.eclipse.etrice.core.room.BaseState;
 import org.eclipse.etrice.core.room.ChoicePoint;
 import org.eclipse.etrice.core.room.ChoicepointTerminal;
 import org.eclipse.etrice.core.room.EntryPoint;
 import org.eclipse.etrice.core.room.ExitPoint;
 import org.eclipse.etrice.core.room.ExternalPort;
 import org.eclipse.etrice.core.room.GuardedTransition;
 import org.eclipse.etrice.core.room.InitialTransition;
 import org.eclipse.etrice.core.room.Message;
 import org.eclipse.etrice.core.room.NonInitialTransition;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.RefinedState;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.RoomPackage;
 import org.eclipse.etrice.core.room.SAPRef;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.ServiceImplementation;
 import org.eclipse.etrice.core.room.StateGraph;
 import org.eclipse.etrice.core.room.StateMachine;
 import org.eclipse.etrice.core.room.StateTerminal;
 import org.eclipse.etrice.core.room.SubStateTrPointTerminal;
 import org.eclipse.etrice.core.room.TrPoint;
 import org.eclipse.etrice.core.room.TrPointTerminal;
 import org.eclipse.etrice.core.room.TransitionPoint;
 import org.eclipse.etrice.core.room.TriggeredTransition;
 import org.eclipse.etrice.core.room.TypedID;
 import org.eclipse.etrice.generator.etricegen.ActiveTrigger;
 import org.eclipse.etrice.generator.etricegen.ETriceGenFactory;
 import org.eclipse.etrice.generator.etricegen.ETriceGenPackage;
 import org.eclipse.etrice.generator.etricegen.ExpandedActorClass;
 import org.eclipse.etrice.generator.etricegen.IDiagnostician;
 import org.eclipse.etrice.generator.etricegen.TransitionChain;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Expanded Actor Class</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.etrice.generator.etricegen.impl.ExpandedActorClassImpl#getActorClass <em>Actor Class</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ExpandedActorClassImpl extends ActorClassImpl implements ExpandedActorClass {
 	
 	private class NodeData {
 		private LinkedList<Transition> inTrans = new LinkedList<Transition>();
 		private LinkedList<Transition> outTrans = new LinkedList<Transition>();
 		private LinkedList<Transition> loopTrans = null;
 
 		LinkedList<Transition> getInTrans() {
 			return inTrans;
 		}
 
 		LinkedList<Transition> getOutTrans() {
 			return outTrans;
 		}
 
 		LinkedList<Transition> getLoopTransitions() {
 			if (loopTrans==null) {
 				loopTrans = new LinkedList<Transition>();
 				for (Transition t : getOutTrans()) {
 					// outgoing transitions always are NonInitialTransitions
 					NonInitialTransition tr = (NonInitialTransition) t;
 					
 					if (tr.getFrom() instanceof StateTerminal) {
 						if (tr.getTo() instanceof StateTerminal) {
 							if (((StateTerminal)tr.getFrom()).getState() == ((StateTerminal)tr.getTo()).getState())
 								loopTrans.add(tr);
 						}
 					}
 					else if (tr.getFrom() instanceof TrPointTerminal) {
 						if (tr.getTo() instanceof TrPointTerminal) {
 							if (((TrPointTerminal)tr.getFrom()).getTrPoint() == ((TrPointTerminal)tr.getTo()).getTrPoint())
 								loopTrans.add(tr);
 						}
 					}
 				}
 			}
 			return loopTrans;
 		}
 	}
 	
 	/**
 	 * The cached value of the '{@link #getActorClass() <em>Actor Class</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActorClass()
 	 * @generated
 	 * @ordered
 	 */
 	protected ActorClass actorClass;
 
 	private static final String TRIGGER_SEP = "#";
 	
 	private IDiagnostician validator;
 
 	private boolean prepared = false;
 	private HashSet<StateGraphItem> ownObjects = null;
 	private HashMap<InterfaceItem, Integer> ifitem2localId = null;
 	private HashMap<StateGraphNode, NodeData> node2data = null;
 	private HashMap<State, LinkedList<ActiveTrigger>> state2triggers = null;
 	private HashMap<String, MessageFromIf> triggerstring2mif = null;
 	private LinkedList<TransitionChain> trchains = null;
 	private HashMap<Transition, TransitionChain> trans2chain = null;
 	private HashMap<EObject, EObject> copy2orig = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ExpandedActorClassImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ETriceGenPackage.Literals.EXPANDED_ACTOR_CLASS;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ActorClass getActorClass() {
 		if (actorClass != null && actorClass.eIsProxy()) {
 			InternalEObject oldActorClass = (InternalEObject)actorClass;
 			actorClass = (ActorClass)eResolveProxy(oldActorClass);
 			if (actorClass != oldActorClass) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ETriceGenPackage.EXPANDED_ACTOR_CLASS__ACTOR_CLASS, oldActorClass, actorClass));
 			}
 		}
 		return actorClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ActorClass basicGetActorClass() {
 		return actorClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setActorClass(ActorClass newActorClass) {
 		ActorClass oldActorClass = actorClass;
 		actorClass = newActorClass;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ETriceGenPackage.EXPANDED_ACTOR_CLASS__ACTOR_CLASS, oldActorClass, actorClass));
 	}
 
 	private void validationError(String msg, EObject obj, EStructuralFeature feature) {
 		validationError(msg, obj, feature, IDiagnostician.INSIGNIFICANT_INDEX);
 	}
 	
 	private void validationError(String msg, EObject obj, EStructuralFeature feature, int idx) {
 		validator.error(msg, copy2orig.get(obj), feature, idx);
 	}
 	
 	private void buildStateGraph() {
 		// create a list of super classes, super first, sub-classes last
 		LinkedList<StateGraph> sms = new LinkedList<StateGraph>();
 		ActorClass orig = getActorClass();
 		if (orig.getStateMachine()!=null)
 			sms.addFirst(orig.getStateMachine());
 		while (orig.getBase()!=null) {
 			orig = orig.getBase();
 			if (orig.getStateMachine()!=null)
 				sms.addFirst(orig.getStateMachine());
 		}
 		
 		// create a self contained copy of all actor classes
 		// references to interface items (ports, saps and spps) point to contents of the original actor class
 		//Collection<StateGraph> all = EcoreUtil.copyAll(sms);
 		// we use the copier directly since we need access to the map
 		Copier copier = new Copier();
 	    Collection<StateGraph> all = copier.copyAll(sms);
 	    copier.copyReferences();
 	    for (EObject o : copier.keySet()) {
 			EObject c = copier.get(o);
 			copy2orig.put(c, o);
 		}
 		
 		// remove self from this list
 		StateGraph self = null;
 		for (Iterator<StateGraph> it = all.iterator(); it.hasNext();) {
 			self = it.next();
 		}
 		all.remove(self);
 		
 		// now we move all base class state machine contents to our state machine
 		StateMachine sm = RoomFactory.eINSTANCE.createStateMachine();
 		if (orig.getStateMachine()!=null)
 			sm.setDataDriven(orig.getStateMachine().isDataDriven());
 		setStateMachine(sm);
 		for (StateGraph sml : all) {
 			sm.getChPoints().addAll(sml.getChPoints());
 			sm.getStates().addAll(sml.getStates());
 			sm.getTrPoints().addAll(sml.getTrPoints());
 			sm.getTransitions().addAll(sml.getTransitions());
 		}
 		
 		// then we relocate the refined state contents to their respective base state and remove all refined states
 		relocateRefinedStateContents(sm, true);
 		
 		if (getActorClass().getStateMachine()!=null) {
 			TreeIterator<EObject> it = self.eAllContents();
 			while (it.hasNext()) {
 				EObject obj = it.next();
 				if (obj instanceof StateGraphItem)
 				addOwnObject((StateGraphItem)obj);
 			}
 			
 			sm.getChPoints().addAll(self.getChPoints());
 			sm.getStates().addAll(self.getStates());
 			sm.getTrPoints().addAll(self.getTrPoints());
 			sm.getTransitions().addAll(self.getTransitions());
 			
 			// then we relocate the refined state contents to their respective base state while keeping all refined states
 			relocateRefinedStateContents(sm, false);
 		}
 	}
 
 	/**
 	 * remove refined states and relocate their respective contents to the
 	 * corresponding base state
 	 * @param sg - the current context (will be called recursively)
 	 * @param remove - if true the refined states are removed, if false they are moved
 	 *    to be siblings of their base states
 	 */
 	private void relocateRefinedStateContents(StateGraph sg, boolean remove) {
 		LinkedList<RefinedState> refinedstates = new LinkedList<RefinedState>();
 		for (State s : sg.getStates()) {
 			if (s instanceof RefinedState) {
 				RefinedState rs = (RefinedState) s;
 				refinedstates.add(rs);
 				BaseState bs = rs.getBase();
 				if (!remove) {
 					StateGraph parent = (StateGraph) bs.eContainer();
 					parent.getStates().add(rs);
 				}
 
 				// relocate contents
 				StateGraph fromSG = rs.getSubgraph();
 				if (fromSG!=null) {
 					StateGraph toSG = bs.getSubgraph();
 					if (toSG==null) {
 						toSG = RoomFactory.eINSTANCE.createStateGraph();
 						bs.setSubgraph(toSG);
 					}
 					toSG.getChPoints().addAll(fromSG.getChPoints());
 					toSG.getStates().addAll(fromSG.getStates());
 					toSG.getTrPoints().addAll(fromSG.getTrPoints());
 					toSG.getTransitions().addAll(fromSG.getTransitions());
 				}
 			}
 		}
 		
 		if (remove) {
 			// remove empty refined states
 			sg.getStates().removeAll(refinedstates);
 		}
 		
 		// recurse down into states
 		for (State s : sg.getStates()) {
 			if (s.getSubgraph()!=null)
 				relocateRefinedStateContents(s.getSubgraph(), remove);
 		}
 	}
 	
 	private void addOutgoingTransition(StateGraphNode node, Transition t) {
 		NodeData data = node2data.get(node);
 		if (data==null) {
 			data = new NodeData();
 			node2data.put(node, data);
 		}
 		data.getOutTrans().add(t);
 	}
 	
 	private void addIncomingTransition(StateGraphNode node, Transition t) {
 		NodeData data = node2data.get(node);
 		if (data==null) {
 			data = new NodeData();
 			node2data.put(node, data);
 		}
 		data.getInTrans().add(t);
 	}
 	
 	private void findOutgoingTransitions(StateGraph sg) {
 		// depth first: recurse into sub graphs of states
 		for (State s : sg.getStates()) {
 			if (s.getSubgraph()!=null)
 				findOutgoingTransitions(s.getSubgraph());
 		}
 
 		for (Transition t : sg.getTransitions()) {
 			addIncomingTransition(getAdjustedTargetNode(t), t);
 			if (t instanceof NonInitialTransition) {
 				addOutgoingTransition(getNode(((NonInitialTransition)t).getFrom()), t);
 			}
 		}
 	}
 
 	/**
 	 * @param sg
 	 */
 	private void checkTransitionChains(StateGraph sg) {
 		for (Transition t : sg.getTransitions()) {
 			TransitionChain chain = getChain(t);
 			if (chain==null)
 				if (!getActorClass().isAbstract()) {
 					int idx = sg.getTransitions().indexOf(t);
 					validationError("transition is not part of a transition chain (only allowed for abstract actor classes)", sg, RoomPackage.eINSTANCE.getStateGraph_Transitions(), idx);
 				}
 		}
 		
 		// recursion
 		for (State s : sg.getStates()) {
 			if (s.getSubgraph()!=null)
 				checkTransitionChains(s.getSubgraph());
 		}
 	}
 	
 	private void doChecks(StateGraph sg) {
 		
 		// check if empty
 		if (sg.getTransitions().isEmpty() && sg.getStates().isEmpty()
 				&& sg.getChPoints().isEmpty() && sg.getTrPoints().isEmpty())
 			return;
 		
 		int initCount = 0;
 		for (Transition t : sg.getTransitions()) {
 			if (t instanceof InitialTransition)
 				++initCount;
 		}
 		if (initCount==0) {
 			if (sg.eContainer() instanceof State) {
 				if (!isAbstract()) {
 					// having no initial transition in a nested state is valid only if there is no transition to history
 					// except of self transitions
 					// i.e. no incoming transition of the state itself
 					NodeData data = node2data.get((State)sg.eContainer());
 					if (data!=null && data.getLoopTransitions().size()!=data.getInTrans().size())
 						validationError(getName()+": Having no initial transition in a nested state is valid only if there is no transition to history except of self transitions!",
 								sg.eContainer(), RoomPackage.eINSTANCE.getState_Subgraph());
 				}
 			}
 			else {
 				validationError(getName()+": The TOP level has to have an initial transition!", sg, RoomPackage.eINSTANCE.getStateGraph_Transitions());
 			}
 		}
 		else {
 			if (initCount>1)
 				validationError(getName()+": There has to be exactly one initial transition!", sg, RoomPackage.eINSTANCE.getStateGraph_Transitions());
 		}
 		
 		for (ChoicePoint cp : sg.getChPoints()) {
 			NodeData data = node2data.get(cp);
 			int idx = sg.getChPoints().indexOf(cp);
 			
 			if (data==null) {
				validationError(getName()+": ChoicePoint is not connected!", cp, RoomPackage.eINSTANCE.getStateGraph_ChPoints(), idx);
 			}
 			else {
 				if (data.getInTrans().size()!=1)
					validationError(getName()+": ChoicePoint has "+data.getInTrans().size()+" incoming transitions!", cp, RoomPackage.eINSTANCE.getStateGraph_ChPoints(), idx);
 				if (data.getOutTrans().size()<2)
					validationError(getName()+": ChoicePoint should have 2 or more branches but has "+data.getOutTrans().size(), cp, RoomPackage.eINSTANCE.getStateGraph_ChPoints(), idx);
 				if (getDefaultBranch(data.getOutTrans())==null)
					validationError(getName()+": ChoicePoint has no default branch!", cp, RoomPackage.eINSTANCE.getStateGraph_ChPoints(), idx);
 				if (!data.getLoopTransitions().isEmpty())
					validationError(getName()+": ChoicePoint is connected to itself!", cp, RoomPackage.eINSTANCE.getStateGraph_ChPoints(), idx);
 			}
 		}
 		
 		for (TrPoint tp : sg.getTrPoints()) {
 			NodeData data = node2data.get(tp);
 			int idx = sg.getChPoints().indexOf(tp);
 			
 			if (data==null) {
 				if (!getActorClass(tp).isAbstract())
					validationError(getName()+": TrPoint is not connected", tp, RoomPackage.eINSTANCE.getStateGraph_TrPoints(), idx);
 			}
 			else {
 				if ((tp instanceof EntryPoint)||(tp instanceof ExitPoint)) {
 					// non-abstract classes must have incoming transitions for entry and exit points
 					if (!isAbstract() && data.getInTrans().isEmpty())
						validationError(getName()+": TrPoint has no incoming transition!", tp, RoomPackage.eINSTANCE.getStateGraph_TrPoints(), idx);
 					
 					if (getActorClass(tp).isAbstract()) {
 						// transition points inherited from abstract base classes
 						// (of from abstract classes themselves) must not have more than one outgoing transition
 						if (data.getOutTrans().size()>1)
							validationError(getName()+": TrPoint must have at most one outgoing transition!", tp, RoomPackage.eINSTANCE.getStateGraph_TrPoints(), idx);
 					}
 					else {
 						// non-abstract or non-inherited transition points must have one outgoing transition
 						if (data.getOutTrans().size()!=1)
							validationError(getName()+": TrPoint must have exactly one outgoing transition!", tp, RoomPackage.eINSTANCE.getStateGraph_TrPoints(), idx);
 					}
 					
 					if (!data.getLoopTransitions().isEmpty())
						validationError(getName()+": TrPoint must have no self transitions!", tp, RoomPackage.eINSTANCE.getStateGraph_TrPoints(), idx);
 				}
 				else if (tp instanceof TransitionPoint) {
 					if (data.getOutTrans().size()<data.getLoopTransitions().size())
						validationError(getName()+": TrPoint must have no incoming transitions!", tp, RoomPackage.eINSTANCE.getStateGraph_TrPoints(), idx);
 				}
 			}
 		}
 		
 		// recurse into sub graphs of states
 		for (State s : sg.getStates()) {
 			if (s.getSubgraph()!=null)
 				doChecks(s.getSubgraph());
 		}
 	}
 	
 	private void findTriggersOfState(State s) {
 		LinkedList<ActiveTrigger> triggers = new LinkedList<ActiveTrigger>();
 		HashMap<String, ActiveTrigger> caughtTriggers = new HashMap<String, ActiveTrigger>();
 		collectTriggersAndTransitions(s, caughtTriggers, triggers);
 		state2triggers.put(s, triggers);
 	}
 	
 	private String getTriggerString(MessageFromIf mifp) {
 		return mifp.getFrom().getName()+TRIGGER_SEP+mifp.getMessage().getName();
 	}
 
 	private void collectOutgoingTransitions(EList<Transition> sameLevelTransitions,
 			HashMap<String, ActiveTrigger> caughtTriggers,
 			LinkedList<ActiveTrigger> triggers) {
 		for (Transition t : sameLevelTransitions) {
 			if (t instanceof TriggeredTransition) {
 				TriggeredTransition tt = (TriggeredTransition) t;
 				
 				for (Trigger trig : ((TriggeredTransition)t).getTriggers()) {
 					for (MessageFromIf mifp : trig.getMsgFromIfPairs()) {
 						String tr = getTriggerString(mifp);
 						ActiveTrigger at = caughtTriggers.get(tr);
 						/*
 						 * accept new trigger if
 						 * 
 						 *  - no inner or inner with guard
 						 *  - accept several but only one without guard (count),
 						 *    insert those with guard first in the list of the _same_ level(!)
 						 */
 						if (at==null) {
 							// no inner transition with this trigger exists,
 							// so this is a new trigger (and our unique point of ActiveTrigger creation)
 							at = ETriceGenFactory.eINSTANCE.createActiveTrigger();
 							at.setMsg(mifp.getMessage());
 							at.setIfitem(mifp.getFrom());
 							at.setTrigger(tr);
 							at.getTransitions().add(tt);
 							caughtTriggers.put(tr, at);
 							triggers.add(at);
 						}
 						else {
 							// check guards of previous transitions
 							TriggeredTransition unguarded = null;
 							boolean accepted = true;
 							for (TriggeredTransition t2 : at.getTransitions()) {
 								for (Trigger trig2 : t2.getTriggers()) {
 									if (isMatching(trig2, tr)) {
 										if (!RoomHelpers.isGuarded(trig2)) {
 											unguarded = t2;
 											if (!sameLevelTransitions.contains(t2))
 												accepted = false;
 										}
 									}
 								}
 							}
 							if (accepted) {
 								if (unguarded!=null) {
 									// there already is an unguarded transition: require a quard
 									if (!RoomHelpers.isGuarded(trig)) {
 										validationError("Transitions with same trigger on same level have to be guarded!", t, RoomPackage.eINSTANCE.getTriggeredTransition_Triggers());
 									}
 									else {
 										int idx = at.getTransitions().indexOf(unguarded);
 										at.getTransitions().add(idx, tt);
 									}
 								}
 								else {
 									// just add at the end
 									at.getTransitions().add(tt);
 								}
 							}
 							// else: this trigger is already satisfied - nevertheless this is a valid situation
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void collectTriggersAndTransitions(State s,
 			HashMap<String, ActiveTrigger> caughtTriggers,
 			LinkedList<ActiveTrigger> triggers) {
 		// consider outgoing transitions of this state
 		collectOutgoingTransitions(getOutgoingTransitions(s), caughtTriggers, triggers);
 		
 		// consider TransitionPoint transitions
 		if (s.eContainer() instanceof StateGraph) {
 			StateGraph sg = (StateGraph) s.eContainer();
 			BasicEList<Transition> trpTransitions = new BasicEList<Transition>();
 			for (TrPoint tp : sg.getTrPoints()) {
 				trpTransitions.addAll(getOutgoingTransitions(tp));
 			}
 			collectOutgoingTransitions(trpTransitions, caughtTriggers, triggers);
 			
 			// go to surrounding context
 			if (sg.eContainer() instanceof State) {
 				collectTriggersAndTransitions((State) sg.eContainer(), caughtTriggers, triggers);
 			}
 		}
 		else {
 			// this should never happen
 			assert(false): "A State must always reside in a StateGraph!";
 		}
 	}
 
 	private void findLeafStateTriggers(StateGraph sg) {
 		for (State s : sg.getStates()) {
 			if (s.getSubgraph()!=null)
 				findLeafStateTriggers(s.getSubgraph());
 			else
 				findTriggersOfState(s);
 		}
 	}
 
 	private void fillTriggerStringMap() {
 		// improve performance using maps name2ifitem and name2msgs
 		HashMap<String, InterfaceItem> name2ifitem = new HashMap<String, InterfaceItem>();
 		HashMap<String, EList<Message>> name2msgs = new HashMap<String, EList<Message>>();
 		ActorClass ac = getActorClass();
 		while (ac!=null) {
 			for (Port ip : ac.getIntPorts()) {
 				mapPort(ip, name2ifitem, name2msgs);
 			}
 			for (ExternalPort ep : ac.getExtPorts()) {
 				mapPort(ep.getIfport(), name2ifitem, name2msgs);
 			}
 			for (SAPRef sap : ac.getStrSAPs()) {
 				mapSAP(sap, name2ifitem, name2msgs);
 			}
 			for (ServiceImplementation spp : ac.getServiceImplementations()) {
 				mapSPP(spp.getSpp(), name2ifitem, name2msgs);
 			}
 
 			ac = ac.getBase();
 		}
 		
 		// compute a set of all trigger strings
 		HashSet<String> triggers = new HashSet<String>();
 		for (LinkedList<ActiveTrigger> ttlist : state2triggers.values()) {
 			for (ActiveTrigger tt : ttlist) {
 				triggers.add(tt.getTrigger());
 			}
 		}
 		
 		// now fill triggerstring2mif
 		for (String trig : triggers) {
 			String[] parts = trig.split(TRIGGER_SEP);
 			
 			// this should always hold true
 			assert(parts.length==2): "By our convention triggers are composed of two parts separated by "
 				+TRIGGER_SEP+". Here we have '"+trig+"' which doesn't consist of two parts!";
 			
 			InterfaceItem ii = name2ifitem.get(parts[0]);
 			
 			// this should always hold true
 			assert(ii!=null): "The name '"+parts[0]+"' did not match an interface item (in name2ifitem)!";
 
 			EList<Message> msgs = name2msgs.get(parts[0]);
 			
 			// this should always hold true
 			assert(msgs!=null): "The name '"+parts[0]+"' did not match an interface item (in name2msgs)!";
 
 			Message msg = null;
 			for (Message m : msgs) {
 				if (m.getName().equals(parts[1]))
 					msg  = m;
 			}
 			
 			// this should always hold true
 			assert(msg!=null): "The message '"+parts[1]+"' did not match a message!";
 			
 			MessageFromIf mif = RoomFactory.eINSTANCE.createMessageFromIf();
 			mif.setFrom(ii);
 			mif.setMessage(msg);
 			triggerstring2mif.put(trig, mif);
 		}
 	}
 
 	private void mapPort(Port p, HashMap<String, InterfaceItem> name2ifitem,
 			HashMap<String, EList<Message>> name2msgs) {
 		name2ifitem.put(p.getName(), p);
 		
 		if (p.isConjugated())
 			name2msgs.put(p.getName(), p.getProtocol().getOutgoingMessages());
 		else
 			name2msgs.put(p.getName(), p.getProtocol().getIncomingMessages());
 	}
 
 	private void mapSAP(SAPRef sap, HashMap<String, InterfaceItem> name2ifitem,
 			HashMap<String, EList<Message>> name2msgs) {
 		name2ifitem.put(sap.getName(), sap);
 
 		// sap is conjugated wrt to the protocol
 		name2msgs.put(sap.getName(), sap.getProtocol().getOutgoingMessages());
 	}
 
 	private void mapSPP(SPPRef spp, HashMap<String, InterfaceItem> name2ifitem,
 			HashMap<String, EList<Message>> name2msgs) {
 		name2ifitem.put(spp.getName(), spp);
 
 		// spp is regular wrt to the protocol
 		name2msgs.put(spp.getName(), spp.getProtocol().getIncomingMessages());
 	}
 
 	private void addTransitionChain(Transition t) {
 		TransitionChain tc = ETriceGenFactory.eINSTANCE.createTransitionChain();
 		tc.setTransition(t);
 		
 		if (t instanceof TriggeredTransition) {
 			TypedID data = null;
 			boolean first = true;
 			for (Trigger tr : ((TriggeredTransition)t).getTriggers()) {
 				for (MessageFromIf mif : tr.getMsgFromIfPairs()) {
 					if (first) {
 						first = false;
 						data = mif.getMessage().getData();
 					}
 					else {
 						if (data!=null) {
 							if (mif.getMessage().getData()==null) {
 								validationError("If one MessageFromIf has data all have to have data for a given transition!", t, RoomPackage.eINSTANCE.getTriggeredTransition_Triggers());
 							}
 							else {
 								TypedID a = mif.getMessage().getData();
 								if (data.getType().getPrim()!=a.getType().getPrim())
 									validationError("The data types of all MessageFromIf have to be the same!", t, RoomPackage.eINSTANCE.getTriggeredTransition_Triggers());
 								if (data.getType().getType()!=a.getType().getType())
 									validationError("The data types of all MessageFromIf have to be the same!", t, RoomPackage.eINSTANCE.getTriggeredTransition_Triggers());
 							}
 						}
 						else {
 							if (mif.getMessage().getData()!=null)
 								validationError("If one MessageFromIf has no data all have to have no data for a given transition!", t, RoomPackage.eINSTANCE.getTriggeredTransition_Triggers());
 						}
 					}
 				}
 			}
 	
 			if (first)
 				validationError("Triggered transition has to have a message from interface!", t, RoomPackage.eINSTANCE.getTriggeredTransition_Triggers());
 		}
 		
 		collectChainTransitions(tc, t);
 		
 		trchains.add(tc);
 	}
 
 	private void collectChainTransitions(TransitionChain tc, Transition t) {
 		trans2chain.put(t, tc);
 
 		// should always hold true
 //		assert(t instanceof NonInitialTransition): "A transition chain must not contain initial transitions!";
 		
 		StateGraphNode node = getNode(t.getTo());
 		
 		// the chain ends if a state is reached
 		if (node instanceof State)
 			return;
 		
 		// the chain ends if source and destination coincide
 		if (t instanceof NonInitialTransition && node==getNode(((NonInitialTransition)t).getFrom())) {
 			if (node instanceof TransitionPoint)
 				tc.setSkipEntry(true);
 			return;
 		}
 		
 		for (Transition next : getOutgoingTransitions(node)) {
 			// from the second transition in the chain on we have:
 			if (next instanceof TriggeredTransition) {
 				int idx = ((StateGraph)next.eContainer()).getTransitions().indexOf(next);
				validationError("Segments following the triggering transition can have no triggers!\n", next, RoomPackage.eINSTANCE.getStateGraph_Transitions(), idx);
 			}
 			
 			collectChainTransitions(tc, next);
 		}
 	}
 
 	private void findTriggeredTransitionChains(StateGraph sg) {
 		for (Transition t : sg.getTransitions()) {
 			if (t instanceof TriggeredTransition) {
 				addTransitionChain(t);
 			}
 			else if (t instanceof InitialTransition) {
 				addTransitionChain(t);
 			}
 		}
 		
 		// recurse into sub graphs of states
 		for (State s : sg.getStates()) {
 			if (s.getSubgraph()!=null)
 				findTriggeredTransitionChains(s.getSubgraph());
 		}
 	}
 
 	private void findGuardedTransitionChains(StateGraph sg) {
 		for (Transition t : sg.getTransitions()) {
 			if (t instanceof GuardedTransition) {
 				addTransitionChain(t);
 			}
 			else if (t instanceof InitialTransition) {
 				addTransitionChain(t);
 			}
 		}
 		
 		// recurse into sub graphs of states
 		for (State s : sg.getStates()) {
 			if (s.getSubgraph()!=null)
 				findGuardedTransitionChains(s.getSubgraph());
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void prepare(IDiagnostician validator) {
 		if (prepared)
 			return;
 		
 		prepared = true;
 		
 		this.validator = validator;
 		
 		ifitem2localId = new HashMap<InterfaceItem, Integer>();
 		ownObjects = new HashSet<StateGraphItem>();
 		node2data = new HashMap<StateGraphNode, NodeData>();
 		state2triggers = new HashMap<State, LinkedList<ActiveTrigger>>();
 		triggerstring2mif = new HashMap<String, MessageFromIf>();
 		trchains = new LinkedList<TransitionChain>();
 		trans2chain = new HashMap<Transition, TransitionChain>();
 		copy2orig = new HashMap<EObject, EObject>();
 		
 		buildStateGraph();
 		computeInterfaceItemLocalIds(getActorClass(), 0);
 		findOutgoingTransitions(getStateMachine());
 
 		doChecks(getStateMachine());
 		if (validator.isFailed())
 			return;
 		
 		if (getStateMachine().isDataDriven()) {
 			findGuardedTransitionChains(getStateMachine());
 		}
 		else {
 			// event driven state machine
 			findLeafStateTriggers(getStateMachine());
 			fillTriggerStringMap();
 			findTriggeredTransitionChains(getStateMachine());
 			checkTransitionChains(getStateMachine());
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void release() {
 		if (!prepared)
 			return;
 		
 		prepared = false;
 		
 		// release resources
 		ifitem2localId = null;
 		ownObjects = null;
 		node2data = null;
 		state2triggers = null;
 		triggerstring2mif = null;
 		trchains = null;
 		trans2chain = null;
 		copy2orig = null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void addOwnObject(StateGraphItem obj) {
 		ownObjects.add(obj);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isOwnObject(StateGraphItem obj) {
 		return ownObjects.contains(obj);
 	}
 	
 	private int computeInterfaceItemLocalIds(ActorClass ac, int offset) {
 		if (ac.getBase()!=null)
 			// first recurse into base class
 			offset = computeInterfaceItemLocalIds(ac.getBase(), offset);
 		
 		for (ExternalPort ep : ac.getExtPorts()) {
 			ifitem2localId.put(ep.getIfport(), offset);
 			++offset;
 		}
 		for (Port ip : ac.getIntPorts()) {
 			ifitem2localId.put(ip, offset);
 			++offset;
 		}
 		
 		for (SAPRef sap : ac.getStrSAPs()) {
 			ifitem2localId.put(sap, offset);
 			++offset;
 		}
 
 		for (ServiceImplementation svc : ac.getServiceImplementations()) {
 			ifitem2localId.put(svc.getSpp(), offset);
 			++offset;
 		}
 		return offset;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public int getInterfaceItemLocalId(InterfaceItem ifitem) {
 		Integer localId = ifitem2localId.get(ifitem);
 		if (localId!=null)
 			return localId.intValue();
 		else
 			return -1;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean hasStateMachine() {
 		ActorClass ac = getActorClass();
 		while (ac!=null) {
 			if (ac.getStateMachine()!=null)
 				return true;
 			ac = ac.getBase();
 		}
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getCode(DetailCode code) {
 		if (code.getCommands().isEmpty())
 			return "";
 		
 		String result = "";
 		for (String cmd : code.getCommands()) {
 			result += cmd + "\n";
 		}
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getTriggerCodeName(MessageFromIf mif) {
 		return "TRIG_"+mif.getFrom().getName()+"__"+mif.getMessage().getName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getTriggerCodeName(String trigger) {
 		String[] parts = trigger.split(TRIGGER_SEP);
 		return "TRIG_"+parts[0]+"__"+parts[1];
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<Transition> getOutgoingTransitions(StateGraphNode node) {
 		NodeData data = node2data.get(node);
 		if (data==null)
 			return new BasicEList<Transition>();
 		else
 			return new BasicEList<Transition>(data.getOutTrans());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<Transition> getIncomingTransitions(StateGraphNode node) {
 		NodeData data = node2data.get(node);
 		if (data==null)
 			return new BasicEList<Transition>();
 		else
 			return new BasicEList<Transition>(data.getInTrans());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<ActiveTrigger> getActiveTriggers(State state) {
 		LinkedList<ActiveTrigger> triggers = state2triggers.get(state);
 		if (triggers==null)
 			return new BasicEList<ActiveTrigger>();
 		else
 			return new BasicEList<ActiveTrigger>(triggers);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<MessageFromIf> getTriggers() {
 		return new BasicEList<MessageFromIf>(triggerstring2mif.values());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<MessageFromIf> getOwnTriggers() {
 		BasicEList<MessageFromIf> result = new BasicEList<MessageFromIf>();
 		
 		HashSet<InterfaceItem> ownIfItems = new HashSet<InterfaceItem>();
 		ownIfItems.addAll(getActorClass().getIntPorts());
 		for (ExternalPort ep : getActorClass().getExtPorts()) {
 			ownIfItems.add(ep.getIfport());
 		}
 		ownIfItems.addAll(getActorClass().getStrSAPs());
 		for (ServiceImplementation svc : getActorClass().getServiceImplementations()) {
 			ownIfItems.add(svc.getSpp());
 		}
 		
 		for(MessageFromIf mif : triggerstring2mif.values()) {
 			if (ownIfItems.contains(mif.getFrom()))
 				result.add(mif);
 		}
 		
 		Collections.sort(result, new Comparator<MessageFromIf>() {
 
 			@Override
 			public int compare(MessageFromIf o1, MessageFromIf o2) {
 				return getTriggerCodeName(o1).compareTo(getTriggerCodeName(o2));
 			}
 
 			
 		});
 		
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getMessageID(MessageFromIf mif) {
 		if (mif.getFrom() instanceof Port) {
 			Port p = (Port) mif.getFrom();
 			return p.getProtocol().getName()+(p.isConjugated()?".OUT_":".IN_")+mif.getMessage().getName();
 		}
 		else if (mif.getFrom() instanceof SAPRef) {
 			SAPRef sap = (SAPRef) mif.getFrom();
 			return sap.getProtocol().getName()+".OUT_"+mif.getMessage().getName();
 		}
 		else if (mif.getFrom() instanceof SPPRef) {
 			SPPRef spp = (SPPRef) mif.getFrom();
 			return spp.getProtocol().getName()+".IN_"+mif.getMessage().getName();
 		}
 
 		return "unknown interface item";
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public TransitionChain getChain(Transition trans) {
 		return trans2chain.get(trans);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<TransitionChain> getTransitionChains() {
 		return new BasicEList<TransitionChain>(trchains);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<TransitionChain> getOwnTransitionChains() {
 		BasicEList<TransitionChain> result = new BasicEList<TransitionChain>();
 		
 		for (TransitionChain tc : trchains) {
 			if (isOwnObject(tc.getTransition()))
 				result.add(tc);
 		}
 		
 		Collections.sort(result, new Comparator<TransitionChain>() {
 
 			@Override
 			public int compare(TransitionChain o1, TransitionChain o2) {
 				return RoomNameProvider.getFullPath(o1.getTransition()).compareTo(RoomNameProvider.getFullPath(o2.getTransition()));
 			}
 			
 		});
 		
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public StateGraphNode getNode(TransitionTerminal tt) {
 		if (tt instanceof StateTerminal)
 			return ((StateTerminal)tt).getState();
 		else if (tt instanceof TrPointTerminal)
 			return ((TrPointTerminal)tt).getTrPoint();
 		else if (tt instanceof SubStateTrPointTerminal)
 			return ((SubStateTrPointTerminal)tt).getTrPoint();
 		else if (tt instanceof ChoicepointTerminal)
 			return ((ChoicepointTerminal)tt).getCp();
 		
 		return null;
 	}
 	
 	private StateGraphNode getAdjustedTargetNode(Transition t) {
 		StateGraphNode node = getNode(t.getTo());
 		if (node instanceof EntryPoint) {
 			NodeData data = node2data.get(node);
 			if (data==null || data.getOutTrans().isEmpty()) {
 				if (getActorClass(node).isAbstract()) {
 					if (node.eContainer().eContainer() instanceof State) {
 						// in this case 
 						State newTarget = (State) node.eContainer().eContainer();
 						BaseState newBaseTarget = (newTarget instanceof BaseState)? (BaseState)newTarget:((RefinedState)newTarget).getBase();
 						
 						StateTerminal st = RoomFactory.eINSTANCE.createStateTerminal();
 						st.setState(newBaseTarget);
 						t.setTo(st);
 						
 						node = newBaseTarget;
 					}
 				}
 			}
 		}
 		return node;
 	}
 	
 	private ActorClass getActorClass(EObject node) {
 		node = copy2orig.get(node);
 		while (node!=null) {
 			if (node instanceof ActorClass)
 				return (ActorClass) node;
 			node = node.eContainer();
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isMatching(Trigger trig, String trigstr) {
 		for (MessageFromIf mifp2 : trig.getMsgFromIfPairs()) {
 			String tr2 = getTriggerString(mifp2);
 			if (tr2.equals(trigstr))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public ContinuationTransition getDefaultBranch(EList<Transition> out) {
 		return getDefaultBranch((List<Transition>)out);
 	}
 	private ContinuationTransition getDefaultBranch(List<Transition> out) {
 		for (Transition t : out) {
 			if (t instanceof ContinuationTransition)
 				return (ContinuationTransition) t;
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ETriceGenPackage.EXPANDED_ACTOR_CLASS__ACTOR_CLASS:
 				if (resolve) return getActorClass();
 				return basicGetActorClass();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case ETriceGenPackage.EXPANDED_ACTOR_CLASS__ACTOR_CLASS:
 				setActorClass((ActorClass)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case ETriceGenPackage.EXPANDED_ACTOR_CLASS__ACTOR_CLASS:
 				setActorClass((ActorClass)null);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case ETriceGenPackage.EXPANDED_ACTOR_CLASS__ACTOR_CLASS:
 				return actorClass != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 } //ExpandedActorClassImpl

 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 
 package org.eclipse.etrice.core.naming;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.ActorRef;
 import org.eclipse.etrice.core.room.BaseState;
 import org.eclipse.etrice.core.room.CPBranchTransition;
 import org.eclipse.etrice.core.room.ChoicePoint;
 import org.eclipse.etrice.core.room.ChoicepointTerminal;
 import org.eclipse.etrice.core.room.ContinuationTransition;
 import org.eclipse.etrice.core.room.DetailCode;
 import org.eclipse.etrice.core.room.GuardedTransition;
 import org.eclipse.etrice.core.room.InitialTransition;
 import org.eclipse.etrice.core.room.LogicalSystem;
 import org.eclipse.etrice.core.room.MessageFromIf;
 import org.eclipse.etrice.core.room.NonInitialTransition;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.RefinedState;
 import org.eclipse.etrice.core.room.RoomClass;
 import org.eclipse.etrice.core.room.RoomModel;
 import org.eclipse.etrice.core.room.SAPRef;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.StateGraph;
 import org.eclipse.etrice.core.room.StateGraphItem;
 import org.eclipse.etrice.core.room.StateTerminal;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.room.SubStateTrPointTerminal;
 import org.eclipse.etrice.core.room.SubSystemClass;
 import org.eclipse.etrice.core.room.SubSystemRef;
 import org.eclipse.etrice.core.room.TrPoint;
 import org.eclipse.etrice.core.room.TrPointTerminal;
 import org.eclipse.etrice.core.room.Transition;
 import org.eclipse.etrice.core.room.TransitionTerminal;
 import org.eclipse.etrice.core.room.Trigger;
 import org.eclipse.etrice.core.room.TriggeredTransition;
 import org.eclipse.etrice.core.room.util.RoomHelpers;
 import org.eclipse.etrice.core.room.util.RoomSwitch;
 
 
 public class RoomNameProvider {
 	
 	private static final String TOP_STATE_NAME = "TOP";
 	private static final String PATH_SEP = "_";
 	
 	private static RoomSwitch<String> nameProvider = new RoomSwitch<String>() {
 		public String caseState(State object) { return RoomNameProvider.getStateName(object); }
 		public String caseChoicePoint(ChoicePoint object) { return object.getName(); };
 		public String caseTrPoint(TrPoint object) { return object.getName(); };
 		public String caseTransition(Transition object) { return RoomNameProvider.getTransitionName(object); };
 		public String caseActorRef(org.eclipse.etrice.core.room.ActorRef object) { return object.getName(); };
 		public String casePort(org.eclipse.etrice.core.room.Port object) { return "Port '"+object.getName()+"'"; };
 		public String caseSAPRef(org.eclipse.etrice.core.room.SAPRef object) { return "SAP '"+object.getName()+"'"; };
 		public String caseSPPRef(org.eclipse.etrice.core.room.SPPRef object) { return "SPP '"+object.getName()+"'"; };
 	};
 	
 	public static String getName(EObject item) {
 		return nameProvider.doSwitch(item);
 	}
 	
 	public static String getFullPath(StateGraphItem item) {
 		return getParentPath(item)+getName(item);
 	}
 
 	public static String getFullPath(StateGraph sg) {
 		if (sg.eContainer() instanceof State)
 			return getFullPath((State)sg.eContainer());
 		else
 			return TOP_STATE_NAME;
 	}
 
 	public static RoomClass getModelClass(EObject obj) {
 		while (obj!=null) {
 			if (obj instanceof RoomClass) {
 				return (RoomClass) obj;
 			}
 			obj = obj.eContainer();
 		}
 		return null;
 	}
 	
 	public static String getClassLocation(RoomClass cls) {
 		if (cls==null)
 			return null;
 		
 		if (cls.eContainer() instanceof RoomModel) {
 			return ((RoomModel)cls.eContainer()).getName()+"."+cls.getName();
 		}
 		return cls.getName();
 	}
 	
 	public static String getLocation(EObject source) {
 		String location = null;
 		String clsLocation = getClassLocation(getModelClass(source));
 		
 		while (source!=null) {
 			if (source instanceof StateGraph) {
 				location = RoomNameProvider.getFullPath((StateGraph)source);
 				break;
 			}
 			else if (source instanceof StateGraphItem) {
 				location = RoomNameProvider.getFullPath((StateGraphItem)source);
 				break;
 			}
 			else {
 				location = RoomNameProvider.getName(source);
 				if (location!=null)
 					break;
 			}
 			source = source.eContainer();
 		}
 		
 		if (location==null)
 			location = "";
 		
 		if (clsLocation!=null)
 			location += " ["+clsLocation+"]";
 		
 		return location;
 	}
 	
 	public static String getTransitionName(Transition t) {
 		String toName = getTerminalName(t.getTo());
 		if (t instanceof InitialTransition) {
 			return "TRANS_INITIAL_TO__"+toName;
 		}
 		else {
 			String fromName = getTerminalName(((NonInitialTransition) t).getFrom());
 			String fromTo = "TRANS_"+fromName+"_TO_"+toName;
 			if (t instanceof TriggeredTransition) {
 				if (fromName.equals(toName))
 					return fromTo + "_BY_" + getTriggerName((TriggeredTransition) t) + "_" + t.getName();
 				else
 					return fromTo + "_BY_" + getTriggerName((TriggeredTransition) t);
 			}
 			else if (t instanceof ContinuationTransition) {
 				return fromTo;
 			}
 			else if (t instanceof CPBranchTransition) {
 				return fromTo+"_COND_"+t.getName();
 			}
 			else if (t instanceof GuardedTransition) {
 				return fromTo+"_GUARD_"+t.getName();
 			}
 		}
 		
 		assert(false): "Unexpected transition class "+t.eClass().getName();
 		return null;
 	}
 	
 	private static boolean isTopLevel(StateGraphItem item) {
 		return item==null? true : !(item.eContainer().eContainer() instanceof State);
 	}
 	
 	public static State getParentState(StateGraphItem item) {
 		if (isTopLevel(item))
 			return null;
 		else
 			return (State) item.eContainer().eContainer();
 	}
 	
 	public static String getStateName(State s) {
 		if (s==null) {
 			return TOP_STATE_NAME;
 		}
 		else if (s instanceof BaseState) {
 			return ((BaseState) s).getName();
 		}
 		else if (s instanceof RefinedState) {
 			return ((RefinedState) s).getBase().getName();
 		}
 		
 		assert(false): "Unexpected State class "+s.eClass().getName();
 		return null;
 	}
 	
 	public static String getStatePathName(State s) {
 		return getParentPath(s)+getStateName(s);
 	}
 	
 	public static String getParentPath(StateGraphItem item) {
 		State parent = getParentState(item);
 		if (parent==null)
 			return "";
 		else
 			return getStatePathName(parent)+PATH_SEP;
 	}
 	
 	public static String getTriggerName(TriggeredTransition tr) {
 		String result = "";
 		for (Trigger tri : tr.getTriggers()) {
 			for (MessageFromIf mif : tri.getMsgFromIfPairs()) {
 				result += mif.getMessage().getName()+mif.getFrom().getName();
 			}
 		}
 		return result;
 	}
 
 	public static String getTerminalName(TransitionTerminal tt) {
 		if (tt instanceof StateTerminal) {
 			return getStateName(((StateTerminal) tt).getState());
 		}
 		else if (tt instanceof TrPointTerminal) {
 			return ((TrPointTerminal) tt).getTrPoint().getName();
 		}
 		else if (tt instanceof SubStateTrPointTerminal) {
 			return getStatePathName(((SubStateTrPointTerminal) tt).getState())+PATH_SEP+((SubStateTrPointTerminal) tt).getTrPoint().getName();
 		}
 		else if (tt instanceof ChoicepointTerminal) {
 			return ((ChoicepointTerminal) tt).getCp().getName();
 		}
 		
 		assert(false): "Unexpected TransitionTerminal class "+tt.eClass().getName();
 		return null;
 	}
 
 	public static String getUniqueTransitionName(StateGraph sg) {
 		Set<String> names = RoomHelpers.getAllNames(sg);
 		
 		for (int i = 0; i < 1000; i++) {
 			String name = "tr"+i;
 			if (!names.contains(name))
 				return name;
 		}
 		
 		return "not_unique";
 	}
 
 	public static String getUniqueInitialTransitionName(StateGraph sg) {
 		Set<String> names = RoomHelpers.getAllNames(sg);
 		
 		if (!names.contains("init"))
 			return "init";
 		
 		for (int i = 0; i < 1000; i++) {
 			String name = "init"+i;
 			if (!names.contains(name))
 				return name;
 		}
 		
 		return "not_unique";
 	}
 
 	public static String getUniqueChoicePointName(StateGraph sg) {
 		Set<String> names = RoomHelpers.getAllNames(sg);
 		
 		for (int i = 0; i < 1000; i++) {
 			String name = "cp"+i;
 			if (!names.contains(name))
 				return name;
 		}
 		
 		return "not_unique";
 	}
 
 	public static String getUniqueTrPointName(StateGraph sg) {
 		Set<String> names = RoomHelpers.getAllNames(sg);
 		
 		for (int i = 0; i < 1000; i++) {
 			String name = "tp"+i;
 			if (!names.contains(name))
 				return name;
 		}
 		
 		return "not_unique";
 	}
 
 	public static String getUniqueStateName(StateGraph sg) {
 		Set<String> names = RoomHelpers.getAllNames(sg);
 		
 		for (int i = 0; i < 1000; i++) {
 			String name = "state"+i;
 			if (!names.contains(name))
 				return name;
 		}
 		
 		return "not_unique";
 	}
 
 	public static String getRefLabelName(ActorContainerRef acr) {
 		String className = "<unknown>";
 		if (acr instanceof ActorRef) {
 			if (((ActorRef)acr).getType()!=null)
 				className = ((ActorRef)acr).getType().getName();
 		}
 		else if (acr instanceof SubSystemRef) {
 			if (((SubSystemRef)acr).getType()!=null)
 				className = ((SubSystemRef)acr).getType().getName();
 		}
 		return acr.getName()+"\n("+className+")";
 	}
 	
 	public static String getTransitionLabelName(Transition t) {
 		String name = null;
 		if (t instanceof InitialTransition) {
 			return "init";
 		}
 		else {
 			if (t.getName()!=null)
 				name = t.getName()+": ";
 			else
 				name ="";
 			
 			NonInitialTransition nit = (NonInitialTransition) t;
 			if (nit.getFrom() instanceof ChoicepointTerminal) {
 				if (nit instanceof ContinuationTransition)
 					return name+"[else]";
 				
 				if (nit instanceof CPBranchTransition) {
 					CPBranchTransition cpt = (CPBranchTransition) nit;
 					if (cpt.getCondition()!=null && !cpt.getCondition().getCommands().isEmpty())
 						return name+"["+cpt.getCondition().getCommands().get(0)+"]";
 				}
 
 				return name+"[?]";
 			}
 			
 			if (t instanceof TriggeredTransition) {
 				boolean first = true;
 				for (Trigger trig : ((TriggeredTransition) t).getTriggers()) {
 					if (first)
 						first = false;
 					else
 						name += "or";
 					name += getTriggerLabel(trig);
 				}
 			}
 			
 			if (t instanceof GuardedTransition) {
 				name += " guard {"+((GuardedTransition)t).getGuard().getCommands().get(0)+"}";
 			}
 		}
 		return name;
 	}
 	
 	public static String getTriggerLabel(Trigger trig) {
 		String name = "<";
 		boolean first = true;
 		for (MessageFromIf mif : trig.getMsgFromIfPairs()) {
 			if (first)
 				first = false;
 			else
 				name += "|";
 			name += getMsgFromIfLabel(mif);
 		}
 		if (trig.getGuard()!=null && !trig.getGuard().getGuard().getCommands().isEmpty())
 			name += " guard {"+trig.getGuard().getGuard().getCommands().get(0)+"}";
 		
 		return name+">";
 	}
 
 	public static String getMsgFromIfLabel(MessageFromIf mif) {
 		return mif.getMessage().getName()+":"+mif.getFrom().getName();
 	}
 
 	public static String getStateGraphLabel(StateGraph sg) {
 		if (sg.eContainer() instanceof State) {
 			State s = (State) sg.eContainer();
 			return getStatePathLabel(s);
 		}
 		else
 			return "/";
 	}
 
 	public static String getStatePathLabel(State s) {
 		if (s.eContainer().eContainer() instanceof State) {
 			return getStatePathLabel((State) s.eContainer().eContainer())+"/"+s.getName();
 		}
 		else
 			return "/"+s.getName();
 	}
 
 	/**
 	 * @param sc
 	 * @return
 	 */
 	public static String getUniqueActorContainerRefName(StructureClass sc) {
 		HashSet<String> names = new HashSet<String>();
 		if (sc instanceof ActorContainerClass) {
 			for (ActorRef ar : ((ActorContainerClass) sc).getActorRefs()) {
 				names.add(ar.getName());
 			}
 		}
 		else if (sc instanceof LogicalSystem) {
 			for (SubSystemRef ar : ((LogicalSystem) sc).getSubSystems()) {
 				names.add(ar.getName());
 			}
 		}
 		
 		for (int i = 0; i < 1000; i++) {
 			String name = "ref"+i;
 			if (!names.contains(name))
 				return name;
 		}
 		
 		return "not_unique";
 	}
 
 	/**
 	 * @param acc
 	 * @return
 	 */
 	public static String getUniqueInterfaceItemName(String prefix, ActorContainerClass acc) {
 		HashSet<String> names = new HashSet<String>();
 		if (acc instanceof ActorClass) {
 			ActorClass ac = (ActorClass) acc;
 			do {
 				for (Port p : ac.getIfPorts()) {
 					names.add(p.getName());
 				}
 				for (Port p : ac.getIntPorts()) {
 					names.add(p.getName());
 				}
 				for (SAPRef sap : ac.getStrSAPs()) {
 					names.add(sap.getName());
 				}
 				for (SPPRef spp : ac.getIfSPPs()) {
 					names.add(spp.getName());
 				}
 				
 				ac = ac.getBase();
 			}
 			while (ac!=null);
 		}
 		else if (acc instanceof SubSystemClass) {
 			for (Port p : ((SubSystemClass) acc).getRelayPorts()) {
 				names.add(p.getName());
 			}
 			for (SPPRef spp : ((SubSystemClass) acc).getIfSPPs()) {
 				names.add(spp.getName());
 			}
 		}
 		
 		for (int i = 0; i < 1000; i++) {
 			String name = prefix+i;
 			if (!names.contains(name))
 				return name;
 		}
 		
 		return "not_unique";
 	}
 	
 	public static String getDetailCodeLocation(DetailCode code) {
 		if (code.eContainer() instanceof Transition) {
 			Transition tr = (Transition) code.eContainer();
 			String where = "?";
 			if (code==tr.getAction())
 				where = "action";
			else if (tr instanceof CPBranchTransition && code==((CPBranchTransition)tr).getCondition())
 				where = "condition";
			else if (tr instanceof GuardedTransition && code==((GuardedTransition)tr).getGuard())
 				where = "guard";
 			return "transition "+tr.getName()+": "+RoomNameProvider.getTransitionName(tr)+" "+where+" code";
 		}
 		else if (code.eContainer() instanceof Trigger) {
 			Transition tr = (Transition) code.eContainer().eContainer();
 			return "transition "+tr.getName()+": "+RoomNameProvider.getTransitionName(tr)+" trigger guard";
 		}
 		else if (code.eContainer() instanceof State) {
 			State state = (State) code.eContainer();
 			String where = "?";
 			if (code==state.getEntryCode())
 				where = "entry";
			else if (code==state.getExitCode())
 				where = "exit";
 			else
 				where = "do";
 			return "state "+RoomNameProvider.getStatePathName(state)+" "+where+" code";
 		}
 		else {
 			assert(false): "unexpected detaild code location";
 			return "???";
 		}
 	}
 }

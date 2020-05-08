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
 
 package org.eclipse.etrice.core.validation;
 
 import java.util.ArrayList;
 
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.ActorRef;
 import org.eclipse.etrice.core.room.Binding;
 import org.eclipse.etrice.core.room.BindingEndPoint;
 import org.eclipse.etrice.core.room.DataClass;
 import org.eclipse.etrice.core.room.EntryPoint;
 import org.eclipse.etrice.core.room.ExitPoint;
 import org.eclipse.etrice.core.room.ExternalPort;
 import org.eclipse.etrice.core.room.InitialTransition;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.LayerConnection;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.ProtocolClass;
 import org.eclipse.etrice.core.room.RefSAPoint;
 import org.eclipse.etrice.core.room.RelaySAPoint;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.ServiceImplementation;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.StateGraph;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.room.SubStateTrPointTerminal;
 import org.eclipse.etrice.core.room.TrPoint;
 import org.eclipse.etrice.core.room.TrPointTerminal;
 import org.eclipse.etrice.core.room.Transition;
 import org.eclipse.etrice.core.room.TransitionPoint;
 import org.eclipse.etrice.core.room.TransitionTerminal;
 
 public class ValidationUtil {
 
 	/**
 	 * check whether dc1 is super type of dc2 
 	 * @param dc1
 	 * @param dc2
 	 * @return <code>true</code> if dc1 or one of its base types is identical to dc2
 	 */
 	public static boolean isKindOf(DataClass dc1, DataClass dc2) {
 		if (dc2==null)
 			return false;
 		
 		while (dc1!=null) {
 			if (dc2==dc1)
 				return true;
 			dc1 = dc1.getBase();
 		}
 		return false;
 	}
 
 	/**
 	 * check whether dc1 is base class of dc2
 	 * @param dc1
 	 * @param dc2
 	 * @return <code>true</code> if dc1 is base class of dc2
 	 */
 	public static boolean isBaseOf(DataClass dc1, DataClass dc2) {
 		return isKindOf(dc2.getBase(), dc1);
 	}
 
 	/**
 	 * check whether pc1 is super type of pc2 
 	 * @param pc1
 	 * @param pc2
 	 * @return <code>true</code> if pc1 or one of its base types is identical to pc2
 	 */
 	public static boolean isKindOf(ProtocolClass pc1, ProtocolClass pc2) {
 		if (pc2==null)
 			return false;
 		
 		while (pc1!=null) {
 			if (pc2==pc1)
 				return true;
 			pc1 = pc1.getBase();
 		}
 		return false;
 	}
 
 	/**
 	 * check whether pc1 is base class of pc2
 	 * @param pc1
 	 * @param pc2
 	 * @return <code>true</code> if pc1 is base class of pc2
 	 */
 	public static boolean isBaseOf(ProtocolClass pc1, ProtocolClass pc2) {
 		return isKindOf(pc2.getBase(), pc1);
 	}
 
 	/**
 	 * check whether ac1 is super type of ac2 
 	 * @param ac1
 	 * @param ac2
 	 * @return <code>true</code> if ac1 or one of its base types is identical to ac2
 	 */
 	public static boolean isKindOf(ActorClass ac1, ActorClass ac2) {
 		if (ac2==null)
 			return false;
 		
 		while (ac1!=null) {
 			if (ac2==ac1)
 				return true;
 			ac1 = ac1.getBase();
 		}
 		return false;
 	}
 
 	/**
 	 * check whether ac1 is base class of ac2
 	 * @param ac1
 	 * @param ac2
 	 * @return <code>true</code> if ac1 is base class of ac2
 	 */
 	public static boolean isBaseOf(ActorClass ac1, ActorClass ac2) {
 		return isKindOf(ac2.getBase(), ac1);
 	}
 	
 	/**
 	 * check if ref recursively is referencing ac
 	 * @param ref
 	 * @param ac
 	 * @return <code>true</code> if ref recursively is referencing ac
 	 */
 	public static boolean isReferencing(ActorClass ref, ActorClass ac) {
 		if (isKindOf(ref,ac))
 			return true;
 		
 		for (ActorRef ar : ref.getActorRefs()) {
 			if (isKindOf(ar.getType(), ac))
 				return true;
 			else if (isReferencing(ar.getType(), ac))
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * returns true if this is a relay port
 	 * 
 	 * @param port
 	 * @return true if relay port
 	 */
 	public static boolean isRelay(Port port) {
 		ActorContainerClass acc = (ActorContainerClass) port.eContainer();
 		if (acc instanceof ActorClass) {
 			if (((ActorClass)acc).getIfPorts().contains(port)) {
 				for (ExternalPort xp : ((ActorClass)acc).getExtPorts()) {
 					if (xp.getIfport()==port)
 						return false;
 				}
 				return true;
 			}
 			return false;
 		}
 		else
 			return true;
 	}
 	
 	/**
 	 * returns true if this port is connectable inside its parent, i.e. an internal end port or a relay port
 	 * 
 	 * @param port
 	 * @return true if connectable
 	 */
 	public static boolean isConnectable(Port port, ActorContainerRef ref, StructureClass acc) {
 		return isConnectable(port, ref, acc, null);
 	}
 	
 	public static boolean isConnectable(Port port, ActorContainerRef ref, StructureClass acc, Binding exclude) {
 		if (port.getMultiplicity()==1 && isConnected(port, ref, acc, exclude))
 			return error("port with multiplicity 1 is already connected");
 
 		if (acc instanceof ActorClass) {
 			for (ExternalPort xp : ((ActorClass)acc).getExtPorts()) {
 				if (xp.getIfport()==port)
 					return error("external end ports must not be connected");
 			}
 			return true;
 		}
 		else
 			return true;
 	}
 
 	public static boolean isValid(Binding bind) {
 		return isConnectable(bind.getEndpoint1().getPort(), bind.getEndpoint1().getActorRef(), bind.getEndpoint2().getPort(), bind.getEndpoint2().getActorRef(), (StructureClass)bind.eContainer(), bind);
 	}
 
 	public static boolean isConnectable(BindingEndPoint ep1, BindingEndPoint ep2, StructureClass sc) {
 		return isConnectable(ep1.getPort(), ep1.getActorRef(), ep2.getPort(), ep2.getActorRef(), sc);
 	}
 	
 	public static String errorMessage = "";
 	public static String getErrorMessage() {
 		return errorMessage;
 	}
 	
 	private static boolean error(String msg) {
 		errorMessage = msg;
 		return false;
 	}
 	
 	public static boolean isConnectable(Port p1, ActorContainerRef ref1, Port p2, ActorContainerRef ref2, StructureClass sc) {
 		return isConnectable(p1, ref1, p2, ref2, sc, null);
 	}
 	
 	public static boolean isConnectable(Port p1, ActorContainerRef ref1, Port p2, ActorContainerRef ref2, StructureClass sc, Binding exclude) {
 		if (p1==p2)
 			return error("no self connection allowed, ports are indentical");
 		
 		if (p1.getProtocol()!=p2.getProtocol())
 			return error("protocols don't match");
 		
 		if (ref1==null && ref2==null)
 			return error("cannot connect two local ports");
 		
 		if (ref1!=null && ref2!=null) {
 			if (ref1==ref2)
 				return error("ports of one ref must not be connected");
 
 			// both ports are on references
 			if (p1.isConjugated()==p2.isConjugated())
 				return error("connected sub component ports must be conjugated to each other");
 			
 			if (!isConnectable(p1, ref1, sc, exclude))
 				return false;
 			if (!isConnectable(p2, ref2, sc, exclude))
 				return false;
 		}
 		else {
 			// one port is an internal end port or a relay port
 			Port local = ref1==null? p1:p2;
 			Port sub = ref1!=null? p1:p2;
 			ActorContainerRef ref = ref1!=null? ref1:ref2;
 			ActorContainerClass acc = (ActorContainerClass) ref.eContainer();
 			
 			if (isRelay(local)) {
 				if (local.isConjugated()!=sub.isConjugated())
 					return error("connected relay port must have same direction");
 				
 				// both must be replicated or both must be not
 				if (local.getMultiplicity()>1 && sub.getMultiplicity()==1)
 					return error("connected relay port must match replication type of peer");
 				if (local.getMultiplicity()==1 && sub.getMultiplicity()>1)
 					return error("connected relay port must match replication type of peer");
 
 				if (!isConnectable(local, null, acc, exclude))
 					return false;
 				if (!isConnectable(sub, ref, acc, exclude))
 					return false;
 			}
 			else {
 				// local port must be an internal end port
 				
 				if (local.isConjugated()==sub.isConjugated())
 					return error("internal end port must have opposite direction");
 				
 				if (local.getMultiplicity()>1 && sub.getMultiplicity()>1)
 					return error("not both ports can be replicated");
 			}
 		}
 		
 		return true;
 	}
 
 	public static boolean isConnected(Port port, ActorContainerRef ref, StructureClass sc) {
 		return isConnected(port, ref, sc, null);
 	}
 	
 	public static boolean isConnected(Port port, ActorContainerRef ref, StructureClass sc, Binding exclude) {
 		for (Binding bind : sc.getBindings()) {
 			if (bind!=exclude) {
 				if (isEndpoint(bind.getEndpoint1(), port, ref))
 					return true;
 				if (isEndpoint(bind.getEndpoint2(), port, ref))
 					return true;
 			}
 		}
 		
 		if (sc instanceof ActorClass) {
 			if (((ActorClass)sc).getBase()!=null)
 				return isConnected(port, ref, ((ActorClass)sc).getBase(), exclude);
 		}
 		return false;
 	}
 
 	private static boolean isEndpoint(BindingEndPoint ep, Port port, ActorContainerRef ref) {
 		return ep.getActorRef()==ref && ep.getPort()==port;
 	}
 
 	public static boolean isRelay(SPPRef spp) {
 		ActorContainerClass acc = (ActorContainerClass) spp.eContainer();
 		if (acc instanceof ActorClass) {
 			ActorClass ac = (ActorClass) acc;
 			for (ServiceImplementation svc : ac.getServiceImplementations()) {
 				if (svc.getSpp()==spp)
 					return false;
 			}
 		}
 		return true;
 	}
 	
 	public static boolean isValid(LayerConnection lc) {
 		if (lc.getFrom() instanceof RelaySAPoint)
 			return isConnectable(((RelaySAPoint)lc.getFrom()).getRelay(), null, lc.getTo().getService(), lc.getTo().getRef(), (StructureClass)lc.eContainer(), lc);
 		else if (lc.getFrom() instanceof RefSAPoint)
 			return isConnectable(null, ((RefSAPoint)lc.getFrom()).getRef(), lc.getTo().getService(), lc.getTo().getRef(), (StructureClass)lc.eContainer(), lc);
 		else {
 			assert(false): "unexpected sub type";
 			return false;
 		}
 	}
 	
 	public static boolean isConnectable(SPPRef src, ActorContainerRef srcRef,
 			SPPRef tgt, ActorContainerRef tgtRef, StructureClass ac) {
 		return isConnectable(src, srcRef, tgt, tgtRef, ac, null);
 	}
 	
 	public static boolean isConnectable(SPPRef src, ActorContainerRef srcRef,
 			SPPRef dst, ActorContainerRef dstRef, StructureClass sc, LayerConnection exclude) {
 
 		if (sc==null) {
 			return false;
 		}
 		
 		if ((src==null && srcRef==null) || (src!=null && srcRef!=null))
 			return error("source can be an own SPP _or_ a ref");
 		
 		if (dst==null || dstRef==null)
 			return error("destination must be an SPP on a ref");
 
 		if (src!=null && isConnectedSrc(src, sc, exclude))
 			return error("source SPP is already connected");
 		
 		// the destination may be connected several times, so don't check this
 		//		if (isConnectedDst(dst, dstRef, sc, exclude))
 		//			return error("destination SPP is already connected");
 		
 		return true;
 	}
 
 	public static boolean isConnectableSrc(SPPRef src, ActorContainerRef ref,
 			StructureClass sc) {
 		return isConnectableSrc(src, ref, sc, null);
 	}
 	
 	public static boolean isConnectableSrc(SPPRef src, ActorContainerRef ref,
 			StructureClass sc, LayerConnection exclude) {
 		
 		if (sc==null) {
 			return false;
 		}
 		
 		if ((src==null && ref==null) || (src!=null && ref!=null))
 			return false;
 
 		// in case of ref!=null no further checks possible
 		// the connection is attached to an ActorContainerRef
 		// which can be multiply connected
 		
 		if (src!=null) {
 			if (isConnectedSrc(src, sc, exclude))
 				return false;
 		}
 		
 		return true;
 	}
 	
 	public static boolean isConnectedSrc(SPPRef src, StructureClass sc) {
 		return isConnectedSrc(src, sc, null);
 	}
 	
 	public static boolean isConnectedSrc(SPPRef src, StructureClass sc, LayerConnection exclude) {
 		for (LayerConnection lc : sc.getConnections()) {
 			if (lc!=exclude)
 				if (lc.getFrom() instanceof RelaySAPoint)
 					if (((RelaySAPoint)lc.getFrom()).getRelay()==src)
 						return true;
 		}
 		
 		if (sc instanceof ActorClass) {
 			ActorClass ac = (ActorClass)sc;
 			
 			// check for attached services
 			for (ServiceImplementation svc : ac.getServiceImplementations()) {
 				if (svc.getSpp()==src)
 					return true;
 			}
 			
 			// recurse into base classes
 			if (ac.getBase()!=null)
 				return isConnectedSrc(src, ac.getBase(), exclude);
 		}
 		return false;
 	}
 
 	public static boolean isConnectableDst(SPPRef src, ActorContainerRef ref,
 			StructureClass sc) {
 		return isConnectableDst(src, ref, sc, null);
 	}
 	
 	public static boolean isConnectableDst(SPPRef dst, ActorContainerRef ref,
 			StructureClass sc, LayerConnection exclude) {
 		
 		if (sc==null) {
 			return false;
 		}
 		
 		if (dst==null || ref==null)
 			return false;
 
 		if (dst!=null) {
 			if (isConnectedDst(dst, ref, sc, exclude))
 				return false;
 		}
 		
 		return true;
 	}
 	
 	public static boolean isConnectedDst(SPPRef src, ActorContainerRef acr, StructureClass sc) {
 		return isConnectedDst(src, acr, sc, null);
 	}
 	
 	public static boolean isConnectedDst(SPPRef src, ActorContainerRef acr, StructureClass sc, LayerConnection exclude) {
 		for (LayerConnection lc : sc.getConnections()) {
 			if (lc!=exclude)
 					if (lc.getTo().getService()==src && lc.getTo().getRef()==acr)
 						return true;
 		}
 		
 		if (sc instanceof ActorClass) {
 			if (((ActorClass)sc).getBase()!=null)
 				return isConnectedDst(src, acr, ((ActorClass)sc).getBase(), exclude);
 		}
 		return false;
 	}
 	
 	public static boolean isConnectable(TransitionTerminal src, TransitionTerminal tgt, StateGraph sg) {
 		// TODOHRR-B validation for transitions
 		
 		if (!isConnectable(src, sg))
 			return false;
 		
 		if (tgt instanceof TrPointTerminal) {
 			if (((TrPointTerminal) tgt).getTrPoint() instanceof EntryPoint)
 				return false;
 			// TransitionPoint and ExitPoint are valid
 		}
 		else if (tgt instanceof SubStateTrPointTerminal) {
 			if (((SubStateTrPointTerminal) tgt).getTrPoint() instanceof EntryPoint)
 				return false;
 			// ExitPoint is valid
 		}
 
 		return true;
 	}
 	
 	public static boolean isConnectable(TransitionTerminal src, StateGraph sg) {
 		if (src==null) {
 			for (Transition t : sg.getTransitions()) {
 				if (t instanceof InitialTransition)
 					// there already is a InitialTransition
 					return false;
 			}
 		}
 		else if (src instanceof TrPointTerminal) {
 			if (((TrPointTerminal) src).getTrPoint() instanceof ExitPoint)
 				return false;
 			// TransitionPoint and EntryPoint are valid
 		}
 		else if (src instanceof SubStateTrPointTerminal) {
 			if (((SubStateTrPointTerminal) src).getTrPoint() instanceof ExitPoint)
 				return false;
 			// EntryPoint is valid
 		}
 		
 		return true;
 	}
 
 	public static boolean isValid(TrPoint tp) {
 		if (tp instanceof TransitionPoint)
 			return true;
 		
 		if (tp.eContainer().eContainer() instanceof State)
 			return true;
 		
 		return error("entry and exit points forbidden on top level state graph");
 	}
 
 	public static boolean isUniqueName(InterfaceItem item) {
 		if (item.eContainer() instanceof ActorClass) {
 			ArrayList<InterfaceItem> all = new ArrayList<InterfaceItem>();
 			ActorClass ac = (ActorClass) item.eContainer();
 			while (ac.getBase()!=null) {
 				ac = ac.getBase();
 				all.addAll(ac.getIfPorts());
 				all.addAll(ac.getIntPorts());
 				all.addAll(ac.getIfSPPs());
 				all.addAll(ac.getStrSAPs());
 			}
 			for (InterfaceItem ii : all) {
				if (ii!=item && ii.getName().equals(item.getName())) {
					return error("name already used in base class "+((ActorClass)ii.eContainer()).getName());
				}
 			}
 		}
 		// else
 		// we don't have to check SubSystemClasses since this is done by xtext (standard namespace)
 		
 		return true;
 	}
 }

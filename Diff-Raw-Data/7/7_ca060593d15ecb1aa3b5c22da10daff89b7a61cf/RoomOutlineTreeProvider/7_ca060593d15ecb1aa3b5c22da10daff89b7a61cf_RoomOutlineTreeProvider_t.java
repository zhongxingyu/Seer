 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.core.ui.outline;
 
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorRef;
 import org.eclipse.etrice.core.room.Attribute;
import org.eclipse.etrice.core.room.ExternalPort;
 import org.eclipse.etrice.core.room.Message;
 import org.eclipse.etrice.core.room.Operation;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.ProtocolClass;
 import org.eclipse.etrice.core.room.SAPRef;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.ServiceImplementation;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
 import org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider;
 
 /**
  * customization of the default outline structure
  * 
  * @author Henrik Rentz-Reichert
  */
 public class RoomOutlineTreeProvider extends DefaultOutlineTreeProvider {
 
 	private static final String STATE_MACHINE_LABEL = "StateMachine";
 	private static final String BEHAVIOR_LABEL = "Behavior";
 	private static final String STRUCTURE_LABEL = "Structure";
 	private static final String INTERFACE_LABEL = "Interface";
 	private static final Object INCOMING_LABEL = "incoming";
 	private static final Object OUTGOING_LABEL = "outgoing";
 	
 	protected boolean _isLeaf(ActorClass ac) {
 		if (ac.getIfPorts().size()>0 || ac.getIfSPPs().size()>0) {
 			return false;
 		}
 		if (ac.getIntPorts().size()>0 || ac.getServiceImplementations().size()>0 ||
 				ac.getStrSAPs().size()>0 || ac.getAttributes().size()>0 ||
 				ac.getActorRefs().size()>0) {
 			return false;
 		}
 		if (ac.getOperations().size()>0 || ac.getStateMachine()!=null) {
 			return false;
 		}
 		return true;
 	}
 	
 	protected void _createChildren(IOutlineNode parentNode, ActorClass ac) {
 		if (ac.getIfPorts().size()>0 || ac.getIfSPPs().size()>0) {
 			new ExtraOutlineNode(ac, parentNode, INTERFACE_LABEL);
 		}
		if (ac.getIntPorts().size()>0 || ac.getExtPorts().size()>0 || ac.getServiceImplementations().size()>0 ||
 				ac.getStrSAPs().size()>0 || ac.getAttributes().size()>0 ||
 				ac.getActorRefs().size()>0) {
 			new ExtraOutlineNode(ac, parentNode, STRUCTURE_LABEL);
 		}
 		if (ac.getOperations().size()>0 || ac.getStateMachine()!=null) {
 			new ExtraOutlineNode(ac, parentNode, BEHAVIOR_LABEL);
 		}
 	}
 	
 	protected void _createChildren(ExtraOutlineNode parentNode, ActorClass ac) {
 		if (parentNode.getText().equals(INTERFACE_LABEL)) {
 			for (Port port : ac.getIfPorts())
 				createNode(parentNode, port);
 			for (SPPRef spp : ac.getIfSPPs())
 				createNode(parentNode, spp);
 		}
 		else if (parentNode.getText().equals(STRUCTURE_LABEL)) {
 			for (Port port : ac.getIntPorts())
 				createNode(parentNode, port);
			for (ExternalPort port : ac.getExtPorts())
				createNode(parentNode, port.getIfport());
 			for (ServiceImplementation svc : ac.getServiceImplementations())
 				createNode(parentNode, svc);
 			for (SAPRef sap : ac.getStrSAPs())
 				createNode(parentNode, sap);
 			for (Attribute attr : ac.getAttributes())
 				createNode(parentNode, attr);
 			for (ActorRef ar : ac.getActorRefs())
 				createNode(parentNode, ar);
 		}
 		else if (parentNode.getText().equals(BEHAVIOR_LABEL)) {
 			for (Operation op : ac.getOperations())
 				createNode(parentNode, op);
 			if (ac.getStateMachine()!=null) {
 				new ExtraOutlineNode(ac, parentNode, STATE_MACHINE_LABEL);
 			}
 		}
 		else if (parentNode.getText().equals(STATE_MACHINE_LABEL)) {
 			for (State s : ac.getStateMachine().getStates()) {
 				createNode(parentNode, s);
 			}
 		}
 	}
 
 	protected void _createChildren(IOutlineNode parentNode, State s) {
 		if (s.getSubgraph()!=null) {
 			for (State state : s.getSubgraph().getStates()) {
 				createNode(parentNode, state);
 			}
 		}
 	}
 	
 	protected boolean _isLeaf(State s) {
 		if (s.getSubgraph()!=null)
 			if (!s.getSubgraph().getStates().isEmpty())
 				return false;
 		return true;
 	}
 	
 	protected void _createChildren(IOutlineNode parentNode, ProtocolClass pc) {
 		if (pc.getIncomingMessages().size()>0) {
 			new ExtraOutlineNode(pc, parentNode, INCOMING_LABEL);
 		}
 		if (pc.getOutgoingMessages().size()>0) {
 			new ExtraOutlineNode(pc, parentNode, OUTGOING_LABEL);
 		}
 	}	
 	
 	protected void _createChildren(ExtraOutlineNode parentNode, ProtocolClass pc) {
 		if (parentNode.getText().equals(INCOMING_LABEL)) {
 			for (Message m : pc.getIncomingMessages()) {
 				createNode(parentNode, m);
 			}
 		}
 		if (parentNode.getText().equals(OUTGOING_LABEL)) {
 			for (Message m : pc.getOutgoingMessages()) {
 				createNode(parentNode, m);
 			}
 		}
 	}
 	
 	protected boolean _isLeaf(Message m) {
 		return true;
 	}
 	
 	protected boolean _isLeaf(Operation o) {
 		return true;
 	}
 }

 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.eclipse.etrice.runtime.java.modelbase;
 
 import org.eclipse.etrice.runtime.java.messaging.Address;
 import org.eclipse.etrice.runtime.java.messaging.IMessageReceiver;
 import org.eclipse.etrice.runtime.java.messaging.IRTObject;
 import org.eclipse.etrice.runtime.java.messaging.Message;
 import org.eclipse.etrice.runtime.java.messaging.MessageService;
 import org.eclipse.etrice.runtime.java.messaging.RTServices;
 import org.eclipse.etrice.runtime.java.messaging.RTSystemServicesProtocol;
 import org.eclipse.etrice.runtime.java.messaging.RTSystemServicesProtocol.*;
 
 
 /**
  * The base class for model actor classes.
  * 
  * @author Thomas Schuetz
  *
  */
 public abstract class ActorClassBase extends EventReceiver implements IMessageReceiver {
 
 	protected static final int EVT_SHIFT = 1000;	// TODOHRR: use 256 or shift operation later
 
 	protected static final int NO_STATE = 0;
 	protected static final int STATE_TOP = 1;
 
 	protected static final int NOT_CAUGHT = 0;
 	
 	private String className = "noname";
 
 	/**
 	 * the current state
 	 */
 	protected int state;
 
 	private MessageService ownMsgsvc = null;
 	private Address ownAddr = null;
 	// the System Port
 	private RTSystemServicesProtocolPort RTSystemPort = null;
 	
 	public ActorClassBase(IRTObject parent, String name, Address ownAddr, Address systemPortPeerAddr) {
 		super(parent, name);
 		
 		this.ownAddr = ownAddr;
 		ownMsgsvc = RTServices.getInstance().getMsgSvcCtrl().getMsgSvc(this.ownAddr.threadID);
 		// own ports
 		RTSystemPort = new RTSystemServicesProtocolPort(this, "RTSystemPort",
 				0, 0, ownAddr,
 				systemPortPeerAddr);
 	}
 
 	public String toString(){
 		return "ActorClass(className="+className+", instancePath="+getInstancePath()+")";
 	}
 	
 	public String getClassName() {
 		return className;
 	}
 	public void setClassName(String className) {
 		this.className = className;
 	}
 
 	@Override
 	public Address getAddress() {
 		// TODO: Actor should have its own address for services and debugging
 		return null;
 	}
 	
 	//--------------------- lifecycle functions
 	// automatically generated lifecycle functions
 	public abstract void init();
 	public abstract void start();
 	public abstract void stop();
 	public abstract void destroy();
 
 	// not automatically generated lifecycle functions
 	// are called, but with empty implementation -> can be overridden by user
 	public void initUser(){}
 	public void startUser(){}
 	public void stopUser(){}
 	public void destroyUser(){}
 
 	@Override
 	public void receive(Message msg) {
 	}
 	
 	public int getState() {
 		return state;
 	}
 
 	public MessageService getMsgsvc() {
 		return ownMsgsvc;
 	}
 	
 	protected boolean handleSystemEvent(InterfaceItemBase ifitem, int evt, Object... generic_data){
		if (ifitem.getLocalId()!=0)return false;
 		
 		switch (evt){
 		case RTSystemServicesProtocol.IN_executeInitialTransition :
 			executeInitTransition();
 			break;
 		case RTSystemServicesProtocol.IN_startDebugging :
 			break;
 		case RTSystemServicesProtocol.IN_stopDebugging :
 			break;		
 		}
		
 		return true;
 	}
 }

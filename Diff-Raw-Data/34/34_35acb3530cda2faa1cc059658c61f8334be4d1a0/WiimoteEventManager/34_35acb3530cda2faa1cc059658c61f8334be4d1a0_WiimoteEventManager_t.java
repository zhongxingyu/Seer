 package org.malai.wiimote.interaction;
 
 import java.util.List;
 
 import org.malai.interaction.BasicEventManager;
 
 import wiiusej.WiiUseApiManager;
 import wiiusej.Wiimote;
 import wiiusej.wiiusejevents.physicalevents.ExpansionEvent;
 import wiiusej.wiiusejevents.physicalevents.IREvent;
 import wiiusej.wiiusejevents.physicalevents.MotionSensingEvent;
 import wiiusej.wiiusejevents.physicalevents.WiimoteButtonsEvent;
 import wiiusej.wiiusejevents.utils.WiimoteListener;
 import wiiusej.wiiusejevents.wiiuseapievents.ClassicControllerInsertedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.ClassicControllerRemovedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.DisconnectionEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.GuitarHeroInsertedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.GuitarHeroRemovedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.NunchukInsertedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.NunchukRemovedEvent;
 import wiiusej.wiiusejevents.wiiuseapievents.StatusEvent;
 
 /**
  * An android event manager gathers Wiimote events produces by the controller and transfers them handlers.<br>
  * <br>
  * This file is part of Malai.<br>
  * Copyright (c) 2009-2013 Tom Demulier--Chevret, Juliette Gourlaouen, Maxime Lorant, Liantsoa Rasata-Manantena <br>
  * <br>
  * Malai is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later version.
  * <br>
  * Malai is distributed without any warranty; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.<br>
  * <br>
  * 2013-02-26<br>
  * @author Tom Demulier--Chevret
  * @author Juliette Gourlaouen
  * @author Maxime Lorant
  * @author Liantsoa Rasata-Manantena
  * @since 0.2
  */
 public class WiimoteEventManager extends BasicEventManager<Wiimote> implements WiimoteListener {
 
 	/** A subset of the set 'handlers' corresponding to the Wiimote Handlers. */
 	private List<WiimoteEventHandler> wiimoteHandlers;
 	
 	private Wiimote[] wiimotes;
 	
 	/**
	 * Create Event Manager with one Wiimote
	 */
	public WiimoteEventManager() {
		this(1);
	}

	/**
 	 * Creates a event manager that gathers Wiimote events and transfers them to handlers.
 	 * @since 0.2
 	 */
	public WiimoteEventManager(int nbWiimotes) {
 		super();
 		
		wiimotes = WiiUseApiManager.getWiimotes(nbWiimotes, true);
 		wiimotes[0].addWiiMoteEventListeners(this);
 		
 		wiimoteHandlers = null;
 	}
 	
 	/**
 	 * Attachs this listener to a new wiimote controller
 	 */
 	public void attachTo(final Wiimote controller) {
 		if(controller != null) {
 			controller.addWiiMoteEventListeners(this);
 		}
 	}
 
 	/**
 	 * Disconnect a wiimote
 	 */
 	public void detachForm(final Wiimote controller) {
 		if(controller != null) {
 			controller.addWiiMoteEventListeners(null);
 		}
 	}
 	
 	/**
	 * Disconnect a wiimote, via its wiimote ID
	 */
	public void detachForm(int id) {
		if(wiimotes.length < id) {
			wiimotes[id].addWiiMoteEventListeners(null);
		}
	}
	
	/**
 	 * Event receives when a button is pressed on the wiimote or a plugged controller
 	 * @param button Event object which contains info about the button pressed
 	 * @since 0.2
 	 */
 	public void onButtonsEvent(WiimoteButtonsEvent button) {
 		System.out.println("Event Button reu !");
 		if(wiimoteHandlers != null) {
 			System.out.println("Envoi aux handlers");
 			for(final WiimoteEventHandler handler : wiimoteHandlers)
 				handler.onButtonPressed(button);
 		}
 	}
 
 	public void onClassicControllerInsertedEvent(ClassicControllerInsertedEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onClassicControllerRemovedEvent(ClassicControllerRemovedEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Event received when a wiimote is disconnected (no battery left, 
 	 * connection dropped or controller has been turn off)
 	 * @since 0.2
 	 */
 	public void onDisconnectionEvent(DisconnectionEvent disconnection) {
 		if(wiimoteHandlers != null) {
 			for(final WiimoteEventHandler handler : wiimoteHandlers)
 				handler.onDisconnection(disconnection);
 		}
 	}
 
 	public void onExpansionEvent(ExpansionEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onIrEvent(IREvent ir) {
 		if(wiimoteHandlers != null) {
 			for(final WiimoteEventHandler handler : wiimoteHandlers)
 				handler.onIrEvent(ir);
 		}
 	}
 
 	public void onMotionSensingEvent(MotionSensingEvent motion) {
 		if(wiimoteHandlers != null) {
 			for(final WiimoteEventHandler handler : wiimoteHandlers)
 				handler.onMotionSensing(motion);
 		}
 	}
 
 	public void onNunchukInsertedEvent(NunchukInsertedEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onNunchukRemovedEvent(NunchukRemovedEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/** Called on a status event : when a expansion controller is (un)plugged
 	 * Return many information about wiimote setup
 	 */
 	public void onStatusEvent(StatusEvent status) {
 		//
 	}
 	
 	
 	public void onGuitarHeroInsertedEvent(GuitarHeroInsertedEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onGuitarHeroRemovedEvent(GuitarHeroRemovedEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }

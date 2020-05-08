 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical;
 
 import java.util.Observable;
 
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used in an HRMController for notifying registered GUIs about HRMController changes. 
  */
 public class HRMControllerObservable extends Observable
 {
 	/**
 	 * Stores the parent HRMController instance.
 	 */
 	private HRMController mHRMController = null;	
 	
 	public HRMControllerObservable(HRMController pHRMController)
 	{
 		mHRMController = pHRMController;
 	}
 	
 	public void notifyObservers(Object pArgument)
 	{
		Logging.log(this, "Got notification with argument " + pArgument + ", will notify " + countObservers() + " observers");
 
 		// mark the Observable object as "changed"
 		setChanged();
 		
 		// notify all registered observers (GUIs) about the change
 		super.notifyObservers(pArgument);
 	}
 	
 	public String toString()
 	{
 		return getClass().getSimpleName() + "@" + mHRMController;
 	}
 }

 /*
  * (C) Copyright 2013 Mark Browning
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 3 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-3.0.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  */
 
 
 package com.mtbs3d.minecrift.hydra;
 
 import java.io.File;
 
 import de.fruitfly.ovr.OculusRift;
 
 import com.sixense.ControllerData;
 import com.sixense.utils.ControllerManager;
 import com.sixense.utils.ManagerCallback;
 import com.sixense.utils.enums.EnumGameType;
 import com.sixense.utils.enums.EnumSetupStep;
 import com.sixense.Sixense;
 
 /**
  * Implements head tracking interface of OculusRift, using
  * left Razer Hydra controller for head orientation (and position?). 
  * 
  * @author mabrowning
  *
  */
 public class Oculus extends OculusRift implements ManagerCallback
 {
     public ControllerData[] newestData = {new ControllerData(), new ControllerData(), new ControllerData(), new ControllerData()};
     ControllerManager cm;
 
 	boolean initialized = false;
 	boolean cmSetup = false;
 	
 	float pitch, yaw, roll;
 
 	@Override
 	public boolean init( File nativeDir ) {
 		Oculus.LoadLibrary(nativeDir);
 		if( Sixense.LoadLibrary( nativeDir ) )
 		{
 			if(Sixense.init())
 			{
 				cm = ControllerManager.getInstance();
 				cm.setGameType(EnumGameType.ONE_PLAYER_TWO_CONTROLLER);
 				cm.registerSetupCallback(this);
 				Sixense.getAllNewestData(newestData);
 
 				this._initSummary = "Initializing Sixense Controller Manager...";
 				cm.update(newestData);
 				cmSetup	= true;
 			}
 			else
 			{
 				this._initSummary = "Couldn't initialize Sixense Library";
 			}
 		}
 		else
 		{
 			this._initSummary = "Couldn't load library";
 		}
 		System.out.println("Hydra: "+ _initSummary);
 		return initialized;
 	}
 
 	@Override
 	public void destroy() {
 		Sixense.exit();
 		
 	}
 
 	@Override
 	public String getInitializationStatus() {
 		return this._initSummary;
 	}
 
 	@Override
 	public float getPitchDegrees_LH() {
 		return pitch;
 	}
 
 	@Override
 	public float getYawDegrees_LH() {
 		return yaw;
 	}
 
 	@Override
 	public float getRollDegrees_LH() {
 		return roll;
 	}
 
 
 	@Override
 	public String getVersion() {
 		return "0.1";
 	}
 
 
 	@Override
 	public boolean isInitialized() {
 		if( !initialized && cmSetup )
 		{
 			Sixense.getAllNewestData(newestData);
 			System.out.println(this._initSummary);
 			cm.update(newestData);
 		}
 		return initialized;
 	}
 
 	@Override
 	public void poll() {
 		Sixense.getAllNewestData(newestData);
 		
 		/*
 		float[][] m = newestData[0].rot_mat;
 		
 		if( m[1][2] > 0.998 || m[1][2] < -0.998 )
 		{
 			//Singularity at south or north pole
 			yaw = (float)Math.toDegrees(Math.atan2(-m[2][0], m[0][0]));
 			roll = 0;
 		}
 		else
 		{
 			yaw  = (float)Math.toDegrees(Math.atan2(m[0][2], m[2][2]));
 			roll = (float)Math.toDegrees(Math.atan2(m[1][0], m[1][1]));
 		}
 		pitch = (float)-Math.toDegrees(Math.asin(m[1][2]));
 		*/
		yaw = newestData[0].yaw;
		pitch = newestData[0].pitch;
		roll = newestData[0].roll;
 	}
 
 
 	/**
 	 * Called by the Sixense Controller manager when the setup step changes
 	 */
 	@Override
 	public void onCallback(EnumSetupStep step) {
 		switch(step)
 		{
 		case P1C2_DONE:
 		case P1C2_IDLE:
 		case SETUP_COMPLETE:
 			this._initSummary = "Razer Hydra initialized";
 			initialized = true;
 			break;
 		default:
 			//Shows the "help" for the current step
 			this._initSummary = cm.getStepString();
 		}
 	}
 }

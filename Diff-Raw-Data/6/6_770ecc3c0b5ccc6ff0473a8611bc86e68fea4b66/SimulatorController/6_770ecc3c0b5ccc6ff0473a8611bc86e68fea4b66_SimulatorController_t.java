 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2013-2014  Dimitrios Desyllas (pc_magas)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *Contact with me by main at thes address: pc_magas@yahoo.gr
 */
 
 package guma.ui.simulator;
 
 import guma.simulator.*;
 import guma.ui.simulator.SimulatorUI; 
 
 public class SimulatorController
 {
 
 	/**
 	*The current simulator
 	*/
 	private AbstractSimulator simulator=null;
 	
 	/**
 	*Tells us how the Interface will look like
 	*/
 	private SimulatorUI ui=null;
 	
 	
 	
 	/**
 	*Constructor Method
 	*@param telestis1: The first operator that we need in order to simulate the operation
 	*@param telestis2: The second operator that we need in order to simulate the operation
 	*@param praxisType: What kind of operation we want to simulate
 	*@param html: configures it we need html output or not
 	*/
 	public SimulatorController(int telestis1,int telestis2,char praxisType)
 	{
 		simulator= AbstractSimulator.makeSimulator(telestis1,telestis2,praxisType);
 		ui=new SimulatorUI();
 	}
 
 	/**
 	*Does the next step on the simulator
 	*/
 	public SimulatorUI next()
 	{
 		try
 		{
 			guma.simulator.InternalStatus s=simulator.next();
 			
 				System.out.println("Message "+ s.getMessage());
				
				ui.update(s.getMessage(),s.getCarry(),s.getStatusValue(),!s.isFinal());
 		}
 		catch(NullPointerException e)
 		{
			return null;
 
 		}
 		
 		return ui.clone();
 	}
 	
 	/**
 	*Returns a copy of SimulatorUI
 	*/
 	public SimulatorUI getSimulatorUI()
 	{
 		return ui.clone();
 	}
 	
 }

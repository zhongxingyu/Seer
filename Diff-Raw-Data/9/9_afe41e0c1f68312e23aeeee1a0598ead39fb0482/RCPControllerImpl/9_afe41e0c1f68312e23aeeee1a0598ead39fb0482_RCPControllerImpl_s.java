 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.gui;
 
 import gda.observable.IObserver;
 import gda.observable.ObservableComponent;
 
 /**
  * Implementation of RCPController to run on the server.
  * 
  * To send a command to the client in a script
  * 
  * from gda.gui import RCPController
  * 
  * 	rcpController = finder.find("RCPController")
  *	if rcpController != None:
  *		rcpController.openView("gda.rcp.mx.views.AlignmentPlot")
  *
  */
 public class RCPControllerImpl implements RCPController {
 
 	ObservableComponent obs = new ObservableComponent();
 	@Override
 	public void openView(String id) {
 		notifyIObservers(this, new RCPOpenViewCommand(id));
 	}
 
 	public static String name = "RCPController";
 
 	@Override
 	public void setName(String name) {
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void addIObserver(IObserver anIObserver) {
 		obs.addIObserver(anIObserver);
 	}
 
 	@Override
 	public void deleteIObserver(IObserver anIObserver) {
 		obs.deleteIObserver(anIObserver);
 	}
 
 	@Override
 	public void deleteIObservers() {
 		obs.deleteIObservers();
 	}
 
 	public void notifyIObservers(Object theObserved, Object changeCode) {
 		obs.notifyIObservers(theObserved, changeCode);
 	}
 
 	public boolean IsBeingObserved() {
 		return obs.IsBeingObserved();
 	}
 
 	@Override
	public void openPesrpective(String id) {
 		notifyIObservers(this, new RCPOpenPerspectiveCommand(id));
 		
 	}
 
 }

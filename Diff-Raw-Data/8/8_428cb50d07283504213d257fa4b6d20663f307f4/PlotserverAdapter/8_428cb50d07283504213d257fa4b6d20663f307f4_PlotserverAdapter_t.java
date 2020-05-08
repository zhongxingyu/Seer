 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
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
 
 package uk.ac.diamond.scisoft.analysis.plotserver.corba.impl;
 
 import gda.configuration.properties.LocalProperties;
 import gda.device.DeviceException;
 import gda.device.corba.CorbaDeviceException;
 import gda.device.corba.impl.DeviceAdapter;
 import gda.factory.corba.util.NetService;
 import gda.observable.IObserver;
 
 import java.util.Arrays;
 import java.util.List;
import java.util.Vector;
 
 import org.omg.CORBA.COMM_FAILURE;
 import org.omg.CORBA.TRANSIENT;
 
 import uk.ac.diamond.scisoft.analysis.PlotServer;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.SimplePlotServer;
 import uk.ac.diamond.scisoft.analysis.plotserver.corba.CorbaPlotServer;
 import uk.ac.diamond.scisoft.analysis.plotserver.corba.CorbaPlotServerHelper;
 
 /**
  * A client side implementation of the adapter pattern for the PlotServer class
  * 
  * For a view whose name matches the regular expression given in property GDA_LOCALPLOTVIEWS_REGEX
  * this class acts as a local plotserver; for others it acts as the adapter to the server based plotserver.
  */
 public class PlotserverAdapter extends DeviceAdapter implements PlotServer {
 
 	private static final String GDA_LOCALPLOTVIEWS_REGEX = "gda.localplotviews.regex";
 	private CorbaPlotServer corbaPlotServer;
 	SimplePlotServer delegate=null;
 	private String localplotviews=null;
 
 	/**
 	 * Create client side interface to the CORBA package.
 	 * 
 	 * @param obj
 	 *            the CORBA object
 	 * @param name
 	 *            the name of the object
 	 * @param netService
 	 *            the CORBA naming service
 	 */
 	public PlotserverAdapter(org.omg.CORBA.Object obj, String name,
 			NetService netService) {
 		super(obj, name, netService);
 		corbaPlotServer = CorbaPlotServerHelper.narrow(obj);
 		this.netService = netService;
 		this.name = name;
 		localplotviews = LocalProperties.get(GDA_LOCALPLOTVIEWS_REGEX);
 		if( localplotviews != null && localplotviews.length()>0){
 			delegate = new SimplePlotServer(true);
 			delegate.addIObserver(new IObserver() {
 				
 				@Override
 				public void update(Object source, Object arg) {
 					notifyIObservers(source, arg);
 				}
 			});
 		}
 	}
 
 	private boolean viewIsLocal(String viewname){
 		return localplotviews != null && localplotviews.length()>0 && viewname.matches(localplotviews);
 	}
 	@Override
 	public GuiBean getGuiState(String guiName) throws DeviceException {
 		if(delegate != null && viewIsLocal(guiName)){
 			try {
 				return delegate.getGuiState(guiName);
 			} catch (Exception e) {
 				throw new DeviceException(e.getMessage());
 			}
 		}
 		
 		for (int i = 0; i < NetService.RETRY; i++) {
 			try {
 				org.omg.CORBA.Any any = corbaPlotServer.getGuiState(guiName);
 				return (GuiBean) any.extract_Value();
 			} catch (COMM_FAILURE cf) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService
 						.reconnect(name));
 			} catch (TRANSIENT ct) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService
 						.reconnect(name));
 			} catch (CorbaDeviceException ex) {
 				throw new DeviceException(ex.message);
 			}
 
 		}
 		throw new DeviceException("Retrieve data error in the PlotServerAdapter");
 	}
 
 	@Override
 	public DataBean getData(String guiName) throws DeviceException {
 		if(delegate != null && viewIsLocal(guiName)){
 			try {
 				return delegate.getData(guiName);
 			} catch (Exception e) {
 				throw new DeviceException(e.getMessage());
 			}
 		}
 		
 		for (int i = 0; i < NetService.RETRY; i++) {
 			try {
 				org.omg.CORBA.Any any = corbaPlotServer.getPlotData(guiName);
 				return (DataBean) any.extract_Value();
 			} catch (COMM_FAILURE cf) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService.reconnect(name));
 			} catch (TRANSIENT ct) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService.reconnect(name));
 			} catch (CorbaDeviceException ex) {
 				throw new DeviceException(ex.message);
 			}
 
 		}
 		throw new DeviceException("Retrieve data error in the PlotServerAdapter");
 	}
 
 	@Override
 	public void setData(String guiName, DataBean plotData) throws DeviceException {
 		if(delegate != null && viewIsLocal(guiName)){
 			try {
 				delegate.setData(guiName, plotData);
 				return;
 			} catch (Exception e) {
 				throw new DeviceException(e.getMessage());
 			}
 		}
 		for (int i = 0; i < NetService.RETRY; i++) {
 			try {
 				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
 				any.insert_Value(plotData);
 				corbaPlotServer.setPlotData(guiName, any);
 				return;
 			} catch (COMM_FAILURE cf) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService.reconnect(name));
 			} catch (TRANSIENT ct) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService.reconnect(name));
 			} catch (CorbaDeviceException ex) {
 				throw new DeviceException(ex.message);
 			}
 
 		}
 
 		throw new DeviceException("Retrieve data error in the PlotServerAdapter");
 	}
 
 	@Override
 	public void updateGui(String guiName, GuiBean guiData) throws DeviceException {
 		if(delegate != null && viewIsLocal(guiName)){
 			
 			try {
 				delegate.updateGui(guiName, guiData);
 				return;
 			} catch (Exception e) {
 				throw new DeviceException(e.getMessage());
 			}
 		}
 
 		for (int i = 0; i < NetService.RETRY; i++) {
 			try {
 				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
 				any.insert_Value(guiData);
 				corbaPlotServer.updateGui(guiName, any);
 				return;
 			} catch (COMM_FAILURE cf) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService.reconnect(name));
 			} catch (TRANSIENT ct) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService.reconnect(name));
 			} catch (CorbaDeviceException ex) {
 				throw new DeviceException(ex.message);
 			}
 		}
 	}
 
 	@Override
 	public void updateData(String guiName) throws DeviceException {
 		if(delegate != null && viewIsLocal(guiName)){
 			try {
 				delegate.updateData(guiName);
 			} catch (Exception e) {
 				throw new DeviceException(e.getMessage());
 			}
 			return;
 		}
 
 		for (int i = 0; i < NetService.RETRY; i++) {
 			try {
 				corbaPlotServer.updateData(guiName);
 				return;
 			} catch (COMM_FAILURE cf) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService
 						.reconnect(name));
 			} catch (TRANSIENT ct) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService
 						.reconnect(name));
 			} catch (CorbaDeviceException ex) {
 				throw new DeviceException(ex.message);
 			}
 		}
 	}
 
 	@Override
 	public boolean isServerLocal() throws DeviceException {
 		return false;
 	}
 
 	@Override
 	public String[] getGuiNames() throws Exception {
 		String[] guiNames=null;
 		if(delegate != null){
 			try {
 				guiNames = delegate.getGuiNames();
 			} catch (Exception e) {
 				throw new DeviceException(e.getMessage());
 			}
 		}
 		
 
 		for (int i = 0; i < NetService.RETRY; i++) {
 			try {
 				org.omg.CORBA.Any any = corbaPlotServer.getGuiNames();
 				String [] otherNames = (String[]) any.extract_Value();
 				if( otherNames != null && otherNames.length>0){
 					if( guiNames != null && guiNames.length>0){
						List<String> asList = new Vector<String>(Arrays.asList(guiNames));
 						asList.addAll(Arrays.asList(otherNames));
 						return asList.toArray(new String[0]);
 					}
 					return otherNames;
 				}
 				return guiNames;
 			} catch (COMM_FAILURE cf) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService
 						.reconnect(name));
 			} catch (TRANSIENT ct) {
 				corbaPlotServer = CorbaPlotServerHelper.narrow(netService
 						.reconnect(name));
 			} catch (CorbaDeviceException ex) {
 				throw new DeviceException(ex.message);
 			}
 		}
 		throw new DeviceException("Retrieve data error in the PlotServerAdapter");
 	}
 	
 	
 }

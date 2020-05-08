 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.plotserver;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import uk.ac.diamond.scisoft.analysis.PlotServer;
 
 abstract public class AbstractPlotServer implements PlotServer {
 
 	private Map<String, GuiBean> guiStore;
 	private Map<String, DataBean> dataStore;
 	private final boolean removeOnGet;
 
 	public AbstractPlotServer() {
 		this(false);
 	}
 	public AbstractPlotServer(boolean removeOnGet) {
 		super();
 		guiStore = new HashMap<String, GuiBean>();
 		dataStore = new HashMap<String, DataBean>();
 		this.removeOnGet = removeOnGet;
 		
 	}
 
 	@Override
 	public DataBean getData(String guiName) throws Exception {
 		return removeOnGet ? dataStore.remove(guiName):  dataStore.get(guiName);
 	}
 
 	@Override
 	public void setData(String guiName, DataBean data) throws Exception {
 		
		Serializable value = data.getGuiParameters().get(GuiParameters.PLOTOPERATION);
 		//if its a duplicate key and an PLOTOP_ADD we need to add the datasets to the old bean
 		if (value != null && value == GuiParameters.PLOTOP_ADD && dataStore.containsKey(guiName)) {
 			for (DataSetWithAxisInformation set : data.data) dataStore.get(guiName).data.add(set);
 		} else {
 			dataStore.put(guiName, data);
 		}	
 	}
 
 	@Override
 	public GuiBean getGuiState(String guiName) throws Exception {
 		return removeOnGet ? guiStore.remove(guiName):  guiStore.get(guiName);
 	}
 
 	@Override
 	public void updateGui(String guiName, GuiBean guiData) throws Exception {
 		guiStore.put(guiName, guiData);
 	}
 
 	@Override
 	public String[] getGuiNames() throws Exception {
 		Set<String> names = new HashSet<String>();
 		names.addAll(guiStore.keySet());
 		names.addAll(dataStore.keySet());
 		return names.toArray(new String[names.size()]);
 	}
 	
 	/**
 	 * Retrieve the Gui Store. Use with caution and understanding.
 	 * @return guiStore
 	 */
 	protected Map<String, GuiBean> getGuiStore() {
 		return guiStore;
 	}
 
 	/**
 	 * Retrieve the Data Store. Use with caution and understanding.
 	 * @return dataStore
 	 */
 	protected Map<String, DataBean> getDataStore() {
 		return dataStore;
 	}
 	
 }

 /*
  * Copyright 2010 kk-electronic a/s. 
  * 
  * This file is part of KKPortal.
  *
  * KKPortal is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * KKPortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with KKPortal.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kk_electronic.kkportal.core.tabs;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.SelectionModel;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.kk_electronic.kkportal.core.activity.LocationInfo;
 import com.kk_electronic.kkportal.core.event.LocationChangedEvent;
 import com.kk_electronic.kkportal.core.event.TabSelectedEvent;
 import com.kk_electronic.kkportal.core.rpc.jsonformat.JsonTabInfo.TabInfoDTO;
 import com.kk_electronic.kkportal.core.security.Identity;
 import com.kk_electronic.kkportal.core.security.IdentityProvider;
 import com.kk_electronic.kkportal.core.security.NewPrimaryIdentityEvent;
 import com.kk_electronic.kkportal.core.services.ModuleService;
 import com.kk_electronic.kkportal.core.services.ModuleTypeInfo;
 
 @Singleton
 public class TabsModel implements NewPrimaryIdentityEvent.Handler, LocationChangedEvent.Handler{
 	List<HasData<TabInfo>> displays = new LinkedList<HasData<TabInfo>>();
 	private final LocationInfo locationInfo;
 	
 	private final ModuleService moduleService;
 	private final SingleSelectionModel<TabInfo> selectionModel;
 	private List<TabInfo> tabInfos;
 
 	private AsyncCallback<List<TabInfo>> tabscallback = new AsyncCallback<List<TabInfo>>() {
 		
 		@Override
 		public void onFailure(Throwable caught) {
 			GWT.log("ERROR-Could not fetch tabs: " + caught);
 		}	
 		
 		@Override
 		public void onSuccess(List<TabInfo> tabInfos) {
 			setTabInfos(tabInfos);
 		}
 	};
 	private final EventBus eventBus;
 	private final IdentityProvider identityProvider;
 
 	@Inject
 	public TabsModel(IdentityProvider identityProvider, ModuleService moduleService,LocationInfo locationInfo,EventBus eventBus) {
 		this.identityProvider = identityProvider;
 		this.moduleService = moduleService;
 		this.locationInfo = locationInfo;
 		this.eventBus = eventBus;
 		eventBus.addHandler(LocationChangedEvent.TYPE, this);
 		selectionModel = new SingleSelectionModel<TabInfo>();
 		identityProvider.addNewPrimaryIdentityEventHandler(this);
 		Identity identity = identityProvider.getPrimaryIdentity();
 		if(identity != null){
 			getTabs(identity);
 		}
 	}
 
 	public void addDataDisplay(HasData<TabInfo> newDisplay){
 		if(displays != null) GWT.log("display overridden");
 
 		displays.add(newDisplay);
 		updateDisplays();
 	}
 
 	private TabInfo findSelectedTabInfo(List<TabInfo> tabInfos){
 		if(locationInfo.getSubint() == null) return null;
 		for(TabInfo tabInfo : tabInfos){
 			if(tabInfo.getId() == locationInfo.getSubint()){
 				return tabInfo;
 			}
 		}
 		return null;
 	}
 	
 	public int getMaxWidth() {
 		//TODO:iterate and find
 		return 80;
 	}
 
 	protected void getTabs(Identity user) {
 		moduleService.getTabs(user, tabscallback);
 	}
 	
 	protected void setTabInfos(List<TabInfo> tabInfos) {
 		this.tabInfos = tabInfos;
 		TabInfo selectedInfo = findSelectedTabInfo(tabInfos);
		if(selectedInfo == null && tabInfos.size()>0){
 			selectedInfo = tabInfos.get(0);
 		}
 		setSelectedWithoutCheck(selectedInfo);
 	}
 	
 	public void setSelectedTab(TabInfo tabInfo){
 		if(!tabInfos.contains(tabInfo)){
 			return;
 		}
 		setSelectedWithoutCheck(tabInfo);
 	}
 	
 	public TabInfo getSelectedTab(){
 		return selectionModel.getSelectedObject();
 	}
 	
 	private void setSelectedWithoutCheck(TabInfo tabInfo){
 		selectionModel.setSelected(tabInfo, true);
 		eventBus.fireEventFromSource(new TabSelectedEvent(tabInfo), this);
 		updateDisplays();
 	}
 	
 	
 	private void updateDisplays() {
 		if(tabInfos == null) return;
 		for(HasData<TabInfo> display:displays){
 			display.setRowData(0, tabInfos);
 		}
 	}
 
 	@Override
 	public void onNewPrimaryIdentity(NewPrimaryIdentityEvent event) {
 		if(event.getIdentity() == null){
 			setTabInfos(new ArrayList<TabInfo>());
 			return;
 		}
 		getTabs(event.getIdentity());
 	}
 
 	public HandlerRegistration addTabSelectedHandler(TabSelectedEvent.Handler handler) {
 		return eventBus.addHandlerToSource(TabSelectedEvent.TYPE, this, handler); 
 	}
 
 	public SelectionModel<? super TabInfo> getSelectionModel() {
 		return selectionModel;
 	}
 
 	public void addNewModule(final TabInfo tabInfo, ModuleTypeInfo selectedObject) {
 		if(tabInfo == null){
 			Window.alert("The blue sock found");
 		}
 		Identity identity = identityProvider.getPrimaryIdentity();
 		moduleService.createModule(identity,selectedObject.getType_id(),new AsyncCallback<Integer>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("TabsModel-Could not create module",caught);
 			}
 
 			@Override
 			public void onSuccess(Integer result) {
 				test(tabInfo,result);
 			}
 		});
 	}
 
 	protected void test(final TabInfo tabInfo, Integer result) {
 		List<Integer> minColumn = null;
 		for(List<Integer> search:tabInfo.getModuleIds()){
 			if(minColumn == null || minColumn.size()>search.size()){
 				minColumn = search;
 			}
 		}
 		minColumn.add(result);
 		setModuleIds(tabInfo, tabInfo.getModuleIds());
 	}
 
 	@Override
 	public void onLocationChanged(LocationChangedEvent event) {
 		if(tabInfos != null){
 			TabInfo tabInfo = findSelectedTabInfo(tabInfos);
 			setSelectedWithoutCheck(tabInfo);
 		}
 	}
 
 	public void setModuleIds(final TabInfo tabInfo,
 			List<List<Integer>> newIds) {
 		if(!tabInfos.contains(tabInfo)){
 			return;
 		}
 		final TabInfo newTabInfo = new TabInfoDTO(tabInfo.getId(),tabInfo.getName(),newIds);
 		tabInfos.set(tabInfos.indexOf(tabInfo), newTabInfo);
 		setSelectedWithoutCheck(newTabInfo);
 		Scheduler.get().scheduleDeferred(new Command() {
 			
 			@Override
 			public void execute() {
 				saveTabInfo(tabInfo, newTabInfo);				
 			}
 		});
 	}
 
 	private void saveTabInfo(final TabInfo tabInfo, final TabInfo newTabInfo) {
 		if(newTabInfo == null){
 			Window.alert("Missing sock found");
 		}
 		moduleService.setModulesIdsOnTab(newTabInfo.getId(),newTabInfo.getModuleIds(),new AsyncCallback<Object>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("TabsModel-Save failed",caught);
 				tabInfos.set(tabInfos.indexOf(tabInfo), tabInfo);
 				setSelectedWithoutCheck(tabInfo);
 			}
 
 			@Override
 			public void onSuccess(Object result) {
 			}
 		});
 	}
 
 	public void setModuleHeight(int moduleId, int height) {
 		moduleService.setModuleHeight(moduleId, height, new AsyncCallback<Object>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("ModuleHeight-Save failed",caught);
 			}
 
 			@Override
 			public void onSuccess(Object result) {
 			}
 		});
 		
 	}
 }

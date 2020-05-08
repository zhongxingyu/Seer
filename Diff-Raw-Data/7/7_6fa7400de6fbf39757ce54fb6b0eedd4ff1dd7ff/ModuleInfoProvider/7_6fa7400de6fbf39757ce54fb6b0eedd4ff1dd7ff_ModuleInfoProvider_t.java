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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.kk_electronic.kkportal.core.services.ModuleService;
 import com.kk_electronic.kkportal.core.services.ModuleService.ModuleInfo;
 
 @Singleton
 public class ModuleInfoProvider {
 
 	HashMap<Integer, ModuleInfo> map = new HashMap<Integer, ModuleInfo>();
 	private final ModuleService moduleService;
 	List<AsyncCallback<Map<Integer, ModuleInfo>>> asyncCallbacks;
	HashSet<Integer> missing;
 	
 	private AsyncCallback<List<ModuleInfo>> infoCallback = new AsyncCallback<List<ModuleInfo>>(){
 		@Override
 		public void onFailure(Throwable caught) {
 			for(AsyncCallback<Map<Integer, ModuleInfo>> callback:asyncCallbacks){
 				callback.onFailure(caught);
 			}
 			asyncCallbacks = null;
 		}
 
 		@Override
 		public void onSuccess(List<ModuleInfo> result) {
 			for(ModuleInfo moduleInfo:result){
 				if(missing.remove(moduleInfo.getId())){
 					map.put(moduleInfo.getId(), moduleInfo);
 				} else {
 					continue;
 				}
 			}
			if(missing.isEmpty() && asyncCallbacks != null){
 				for(AsyncCallback<Map<Integer, ModuleInfo>> callback:asyncCallbacks){
 					callback.onSuccess(map);
 				}
 				asyncCallbacks = null;
 			}
 		}
 	};
 	
 	@Inject
 	public ModuleInfoProvider(ModuleService moduleService) {
 		this.moduleService = moduleService;
 	}
 	
 	public void translate(HashSet<Integer> mustHave,
 			AsyncCallback<Map<Integer, ModuleInfo>> asyncCallback) {
 		for(int x:mustHave){
 			if(!map.containsKey(x)){
 				addMissing(x);
 			}
 		}
 		if(missing != null && !missing.isEmpty()){
 			addCallback(asyncCallback);
 			//We defer so multiple call in the same cycle gets bundled
 			Scheduler.get().scheduleDeferred(new Command() {
 				
 				@Override
 				public void execute() {
 					requestMissing();
 				}
 			});
 		} else {
 			asyncCallback.onSuccess(map);
 		}
 	}
 
 	private void addCallback(
 			AsyncCallback<Map<Integer, ModuleInfo>> asyncCallback) {
 		if(asyncCallbacks == null) asyncCallbacks = new ArrayList<AsyncCallback<Map<Integer,ModuleInfo>>>();
 		asyncCallbacks.add(asyncCallback);
 	}
 	
 	private void requestMissing() {
 		moduleService.getModuleInfo(new ArrayList<Integer>(missing), infoCallback);
 	}
 
 	private void addMissing(int x) {
 		if(missing == null)	missing = new HashSet<Integer>();
 		missing.add(x);
 	}
 }

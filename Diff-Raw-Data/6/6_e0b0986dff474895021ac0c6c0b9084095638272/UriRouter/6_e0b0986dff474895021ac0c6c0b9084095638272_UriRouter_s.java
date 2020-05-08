 /*
  * Copyright (c) 2013 goodplayer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package net.moetang.turismo_plus.pipeline.router;
 
 import java.util.HashMap;
 import java.util.Map;
 import net.moetang.turismo_plus.pipeline.actionresult.ActionResult;
 import net.moetang.turismo_plus.pipeline.actionresult.NotFoundResult;
 import net.moetang.turismo_plus.pipeline.actionresult.SuccessResult;
import net.moetang.turismo_plus.pipeline.processing.Action;
 import net.moetang.turismo_plus.pipeline.processing.IAction;
 import net.moetang.turismo_plus.pipeline.processing.Resolver;
 import net.moetang.turismo_plus.util.Env;
 
 public abstract class UriRouter implements Router {
 
 	public UriRouter() {
 		map();
 		this.resolver = new ResolverImpl();
 	}
 
 	protected abstract void map();
 
 	//===============================================
 	private String g_prefix;
 	private int prefixIdx;
 	protected void _prefix(final String prefix) {
 		if(prefix.endsWith("/")){
 			this.g_prefix = prefix;
 		}
 		else{
 			this.g_prefix = prefix+"/";
 		}
 		prefixIdx = prefix.length()-1;
 	}
 	//===============================================
 	private Map<String, Map<String, IAction>> map = new HashMap<>();
 	protected final void _custom(final String method, final String path, final IAction action) {
 		if (this.resolver == null) {
 			Map<String, IAction> methodMap = map.get(method);
 			if(methodMap == null){
 				methodMap = new HashMap<>();
 				map.put(method, methodMap);
 			}
 			methodMap.put(path, action);
 		}
     }
 	//===============================================
 	private IAction defaultAction;
 	//only once
	protected final void _default(final Action action){
 		if (this.resolver == null) {
 			this.defaultAction = action;
 		}
 	}
 	//===============================================
 
 	protected final void get(final String path, final IAction action) {
 		_custom(GET, path, action);
     }
 	protected final void post(final String path, final IAction action) {
 		_custom(POST, path, action);
     }
 	protected final void put(final String path, final IAction action) {
 		_custom(PUT, path, action);
     }
 	protected final void head(final String path, final IAction action) {
 		_custom(HEAD, path, action);
     }
 	protected final void options(final String path, final IAction action) {
 		_custom(OPTIONS, path, action);
     }
 	protected final void delete(final String path, final IAction action) {
 		_custom(DELETE, path, action);
     }
 	protected final void trace(final String path, final IAction action) {
 		_custom(TRACE, path, action);
     }
 	
 	@Override
 	public Resolver resolver() {
 		return this.resolver;
 	}
 
 	private Resolver resolver;
 	private class ResolverImpl implements Resolver{
 		private String prefix = g_prefix;
 		public ResolverImpl() {
 		}
 		@Override
 		public ActionResult resolve(String method, String uri) {
 			Env env = Env.get();
 			// 1st : check prefix
 			if((prefix != null)){
 				if(!uri.startsWith(prefix))
 					return null;
 				else{
 					uri = uri.substring(prefixIdx);
 				}
 			}
 			// 2nd : find action
 			IAction action = null;
 			Map<String, IAction> mm = map.get(method);
 			if(mm != null){
 				action = mm.get(uri);
 			}
 			// 3rd : if it can't find any action, do default
 			//       if it can find one action, then do before->action->after
 			if(action == null){
 				if(defaultAction == null){
 					return NO_MAPPING;
 				}else{
 					defaultAction.doAction(env);
 					return env.getResult();
 				}
 			}
 			else{
 				action.doAction(env);
 				// finally : success or found
 				return SUCCESS_RESULT;
 			}
 		}
 	}
 	private static final ActionResult SUCCESS_RESULT = new SuccessResult();
 	private static final ActionResult NO_MAPPING = new NotFoundResult() {
 		@Override
 		public void doResult() {
 			Env._doChain();
 		}
 	};
 
     protected static final String POST = "POST";
     protected static final String GET = "GET";
     protected static final String HEAD = "HEAD";
     protected static final String OPTIONS = "OPTIONS";
     protected static final String PUT = "PUT";
     protected static final String DELETE = "DELETE";
     protected static final String TRACE = "TRACE";
 }

 package com.threewks.thundr.action.method.bind;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.threewks.thundr.action.method.MethodAction;
 import com.threewks.thundr.action.method.bind.http.HttpPostDataMap;
 import com.threewks.thundr.action.method.bind.http.InstanceParameterBinder;
 import com.threewks.thundr.action.method.bind.http.ParameterBinderSet;
 import com.threewks.thundr.introspection.ParameterDescription;
 
 public class ActionParameterBinder {
 	public List<Object> bind(MethodAction actionMethod, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVars) {
 		List<ParameterDescription> parameterDescriptions = actionMethod.parameters();
 		HttpPostDataMap pathMap = createPathMap(req, pathVars);
 		ParameterBinderSet binders = binders(req, resp, pathVars);
 		return binders.createFor(parameterDescriptions, pathMap);
 	}
 
 	@SuppressWarnings("unchecked")
 	private HttpPostDataMap createPathMap(HttpServletRequest req, Map<String, String> pathVars) {
 		Map<String, String[]> requestParameters = new HashMap<String, String[]>(req.getParameterMap());
 		if (pathVars != null) {
 			for (Map.Entry<String, String> entry : pathVars.entrySet()) {
 				requestParameters.put(entry.getKey(), new String[] { entry.getValue() });
 			}
 		}
 		HttpPostDataMap pathMap = new HttpPostDataMap(requestParameters);
 		return pathMap;
 	}
 
 	private ParameterBinderSet binders(HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVars) {
 		ParameterBinderSet binders = new ParameterBinderSet();
 		InstanceParameterBinder requestBinder = new InstanceParameterBinder(req);
 		InstanceParameterBinder responseBinder = new InstanceParameterBinder(resp);
 		InstanceParameterBinder sessionBinder = new InstanceParameterBinder(req == null ? null : req.getSession());
 
		binders.addBinder(sessionBinder);
 		binders.addBinder(requestBinder);
 		binders.addBinder(responseBinder);
 		binders.addDefaultBinders();
 		return binders;
 	}
 }

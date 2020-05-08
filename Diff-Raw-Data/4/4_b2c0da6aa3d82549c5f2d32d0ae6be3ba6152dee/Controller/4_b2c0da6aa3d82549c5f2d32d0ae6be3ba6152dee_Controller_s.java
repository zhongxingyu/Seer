 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.app.server.controller;
 
 import static org.oobium.http.HttpSession.SESSION_ID_KEY;
 import static org.oobium.http.HttpSession.SESSION_UUID_KEY;
 import static org.oobium.http.constants.Action.showEdit;
 import static org.oobium.http.constants.Action.showNew;
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.mapParams;
 import static org.oobium.utils.StringUtils.varName;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.utils.json.JsonUtils.format;
 import static org.oobium.utils.json.JsonUtils.toJson;
 import static org.oobium.utils.json.JsonUtils.toMap;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.oobium.app.AppService;
 import org.oobium.app.server.response.ByteArrayResponse;
 import org.oobium.app.server.response.Response;
 import org.oobium.app.server.routing.AppRouter;
 import org.oobium.app.server.routing.IPathRouting;
 import org.oobium.app.server.routing.IUrlRouting;
 import org.oobium.app.server.routing.Realm;
 import org.oobium.app.server.routing.Router;
 import org.oobium.app.server.routing.handlers.AuthorizationHandler;
 import org.oobium.app.server.view.DynamicAsset;
 import org.oobium.app.server.view.ScriptFile;
 import org.oobium.app.server.view.StyleSheet;
 import org.oobium.app.server.view.View;
 import org.oobium.app.server.view.ViewRenderer;
 import org.oobium.cache.CacheService;
 import org.oobium.http.HttpCookie;
 import org.oobium.http.HttpRequest;
 import org.oobium.http.HttpSession;
 import org.oobium.http.constants.Action;
 import org.oobium.http.constants.ContentType;
 import org.oobium.http.constants.Header;
 import org.oobium.http.constants.StatusCode;
 import org.oobium.logging.Logger;
 import org.oobium.persist.Model;
 import org.oobium.utils.Base64;
 
 public class Controller implements ICache, IFlash, IParams, IPathRouting, IUrlRouting, ISessions, IHelpers {
 
 	private static final String AUTHENTICATED_AT = "authenticatedAt";
 	private static final String AUTHENTICATED_BY = "authenticatedBy";
 	private static final String AUTHENTICATED_BY_TYPE = "authenticatedByType";
 	private static final long AUTHENTICATION_INTERVAL = 1000*60*30; // 30 minutes
 	
 	public static final String FLASH_KEY = "oobium_flash";
 	public static final String FLASH_ERROR = "error";
 	public static final String FLASH_NOTICE = "notice";
 	public static final String FLASH_WARNING = "warning";
 	
 	static String createActionCacheKey(Class<? extends Controller> controller, Action action) {
 		return controller.getSimpleName() + "/" + action.name();
 	}
 	
 	static String createActionCacheKey(Class<? extends Controller> controller, Action action, ContentType type) {
 		return controller.getSimpleName() + "/" + action.name() + "." + type.getFileExt();
 	}
 	
 	static String createCacheKey(AppService handler, String key) {
 		return handler.getName() + "/" + key;
 	}
 
 	protected Logger logger;
 	private AppService handler;
 	private AppRouter appRouter;
 	private Router router;
 	private Action action;
 	protected HttpRequest request;
 	protected Response response;
 	private Map<String, Object> params;
 	private Map<String, Object> flash;
 	private Map<String, Object> flashOut;
 	private HttpSession session;
 	private boolean sessionResolved;
 	private boolean isRendered;
 	
 	public Controller() {
 		// no args constructor
 	}
 	
 //	public Controller(HttpRequest request, Map<String, Object> routeParams) {
 //		initialize(request, routeParams);
 //	}
 
 	@Override
 	public boolean accepts(ContentType type) {
 		ContentType[] requestTypes = request.getContentTypes();
 		if(!blank(requestTypes)) {
 			for(ContentType accept : requestTypes) {
 				if(accept.isWild() || accept == type) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean acceptsHtml() {
 		return accepts(ContentType.HTML);
 	}
 
 	@Override
 	public boolean acceptsImage() {
 		ContentType[] requestTypes = request.getContentTypes();
 		if(!blank(requestTypes)) {
 			for(ContentType accept : requestTypes) {
 				if(accept.isWild() || accept.isImage()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean acceptsJS() {
 		return accepts(ContentType.JS);
 	}
 	
 	@Override
 	public boolean acceptsJSON() {
 		return accepts(ContentType.JSON);
 	}
 	
 	private void addFlashError(Model model) {
 		if(model.hasErrors()) {
 			if(flash == null || !flash.containsKey(FLASH_ERROR)) {
 				setFlashError(model);
 			} else {
 				Set<String> errors = new LinkedHashSet<String>();
 				Object error = flash.get(FLASH_ERROR);
 				if(error instanceof Collection<?>) {
 					for(Object o : (Collection<?>) error) {
 						errors.add(String.valueOf(o));
 					}
 				} else {
 					errors.add(String.valueOf(error));
 				}
 				for(String e : model.getErrorsList()) {
 					errors.add(e);
 				}
 				setFlashError(errors);
 			}
 		}
 	}
 	
 	/**
 	 * Code to be executed after the actual request handler.  This code
 	 * will be called whether or not there has been a render or redirect.
 	 * <p>All filters through out the class hierarchy will be called, starting with
 	 * the top most class; there is no need for an explicit call to super.afterFilter.</p>
 	 */
 	public void afterFilter(Action action) {
 		// subclasses to implement
 	}
 	
 	public boolean authenticate() {
 		resolveSession(true);
 		if(session != null) {
 			session.put(AUTHENTICATED_AT, System.currentTimeMillis());
 			session.remove(AUTHENTICATED_BY);
 			session.remove(AUTHENTICATED_BY_TYPE);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean authenticate(Model model) {
 		if(model != null) {
 			resolveSession(true);
 			if(session != null) {
 				session.put(AUTHENTICATED_AT, System.currentTimeMillis());
 				session.put(AUTHENTICATED_BY, model.getId());
 				session.put(AUTHENTICATED_BY_TYPE, model.getClass().getName());
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * This method calls the {@link #authorize(String, String)} method, passing in the
 	 * username and password from the request. If the {@link #authorize(String, String)}
 	 * method returns false then a 401 - Not Authorized response will be rendered.  If called
 	 * from a beforeFilter (as intended) then execution will be stopped and the 401 response
 	 * will be returned.  If {@link #authorize(String, String)} returns true then this
 	 * method simply returns.  If the request does not contain at least a username the
 	 * {@link #authorize(String, String)} will not be called and this method will behave as
 	 * if it had returned false by rendering the 401 response.
 	 */
 	public void authorize() {
 		boolean authorized = false;
 		String header = request.getHeader(Header.AUTHORIZATION);
 		if(header != null && header.startsWith("Basic ")) {
 			String[] sa = new String(Base64.decode(header.substring(6).getBytes())).split(":");
 			if(sa.length == 1) {
 				authorized = authorize(sa[0], null);
 			} else if(sa.length == 2) {
 				authorized = authorize(sa[0], sa[1]);
 			}
 		}
 		if(!authorized) {
 			try {
 				rendering();
 				response = new AuthorizationHandler(router, "secure area").routeRequest(request);
 			} catch(Exception e) {
 				logger.warn(e);
 			}
 		}
 	}
 	
 	/**
 	 * Authorize a request that is being authenticated using basic authentication.
 	 * This method is called from the {@link #authorize()} method.  This method is intended to be
 	 * implemented by subclasses to add actual authorization rules.  To use this method, call
 	 * the authorize method from the {@link #beforeFilter(Action)} method.
 	 * @param username the username sent in the request; never null
 	 * @param password the password sent in the request; may be null
 	 * @return true if the username and password are authorized to continue; false otherwise
 	 */
 	public boolean authorize(String username, String password) {
 		return false;
 	}
 	
 	/**
 	 * Code to be executed before the actual request handler.  A filter that either
 	 * renders or redirects will end the execution chain and all subsequent filters
 	 * and the actual handler will not be run.
 	 * <p>All filters through out the class hierarchy will be called, starting with
 	 * the top most class; there is no need for an explicit call to super.beforeFilter.</p>
 	 */
 	public void beforeFilter(Action action) {
 		// subclasses to implement
 	}
 	
 	private void callFilters(boolean before) {
 		List<Class<?>> classes = new ArrayList<Class<?>>();
 		Class<?> clazz = getClass();
 		while(clazz != Controller.class) {
 			classes.add(clazz);
 			clazz = clazz.getSuperclass();
 		}
 		for(int i = classes.size()-1; i >= 0; i--) {
 			try {
 				Method method = classes.get(i).getDeclaredMethod(before ? "beforeFilter" : "afterFilter", Action.class);
 				method.setAccessible(true);
 				try {
 					method.invoke(this, action);
 					if(before && isRendered) {
 						return;
 					}
 				} catch (IllegalArgumentException e) {
 					logger.error(e);
 				} catch (IllegalAccessException e) {
 					logger.error(e);
 				} catch (InvocationTargetException e) {
 					logger.error(e);
 				}
 			} catch(NoSuchMethodException e) {
 				// discard
 			}
 		}
 	}
 	
 	public void clear() {
 		logger = null;
 		handler = null;
 		appRouter = null;
 		action = null;
 		request = null;
 		response = null;
 		if(params != null) params.clear();
 		params = null;
 		if(flash != null) flash.clear();
 		flash = null;
 		if(flashOut != null) flashOut.clear();
 		flashOut = null;
 		if(session != null) session.clear();
 		session = null;
 		sessionResolved = false;
 		isRendered = false;
 	}
 	
 	/**
 	 * POST url/[model]s
 	 * Implemented by subclasses, if necessary, to create a model.
 	 * @throws SQLException
 	 */
 	public void create() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 	
 	/** 
 	 * DELETE url/[model]s/1
 	 * Implemented by subclasses, if necessary, to destroy / delete a model.
 	 * @throws SQLException
 	 */
 	public void destroy() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 	
 	public void execute(Action action) throws SQLException {
 		if(logger.isLoggingDebug()) {
 			logger.debug("start controller#execute - " + getControllerName() + "#" + ((action != null) ? action : "handleRequest"));
 		}
 		if(!isRendered) {
 			this.action = action;
 			callFilters(true);
 			if(!isRendered) {
 				String cache = getCacheForAction(action);
 				if(cache != null) {
 					render(wants(), cache);
 				} else {
 					if(action == null) {
 						handleRequest();
 					} else {
 						switch(action) {
 						case create:	create();	break;
 						case update:	update();	break;
 						case destroy:	destroy();	break;
 						case show:		show();		break;
 						case showAll:	showAll();	break;
 						case showEdit:	showEdit();	break;
 						case showNew:	showNew();	break;
 						}
 					}
 					if(isRendered && response != null) {
 						setCacheForAction(action, response.getBody());
 					}
 				}
 			}
 			callFilters(false);
 			this.action = null;
 		}
 
 		if(response != null) {
 			if(flashOut != null) {
 				response.setCookie(FLASH_KEY, toJson(flashOut));
 			} else if(flash != null) {
 				response.expireCookie(FLASH_KEY);
 			}
 			
 			if(session != null) {
 				if(session.isDestroyed()) {
 					response.expireCookie(SESSION_ID_KEY);
 					response.expireCookie(SESSION_UUID_KEY);
 				} else {
 					Timestamp exp = new Timestamp(System.currentTimeMillis() + 30*60*1000);
 					session.setExpiration(exp);
 					if(session.save()) {
 						response.setCookie(SESSION_ID_KEY, Integer.toString(session.getId()), exp);
 						response.setCookie(SESSION_UUID_KEY, session.getUuid(), exp);
 					}
 				}
 			}
 		}
 
 		logger.debug("end controller execute");
 	}
 	
 	public void expireCache(String key) {
 		CacheService cache = handler.getCacheService();
 		if(cache != null) {
 			cache.expire(createCacheKey(handler, key));
 		} else {
 			logger.warn("cache service is not available");
 		}
 	}
 	
 	protected void expireCacheForAction() {
 		expireCacheForAction(action);
 	}
 	
 	protected void expireCacheForAction(Action action) {
 		if(ActionCache.isCaching(this, action)) {
 			expireCache(createActionCacheKey(getClass(), action, wants()));
 		}
 	}
 	
 	/**
 	 * Create a {@link Filter} object for the parameter with the given name.<br>
 	 * If the parameter with the given name is not a map (there is no parameter with 
 	 * the given name, or there are no parameters)
 	 * this method returns an empty {@link Filter} whose methods can be called but
 	 * will not perform any function.
 	 * @param param the name of the parameter to be filtered
 	 * @return a {@link Filter}; never null
 	 */
 	public Filter filter(String param) {
 		if(params != null) {
 			return new Filter(params.get(param));
 		}
 		return new Filter(null);
 	}
 
 	@Override
 	public String flash(String name) {
 		return getFlash(name);
 	}
 	
 	public <T> T flash(String name, java.lang.Class<T> type) {
 		if(flash != null) {
 			return coerce(flash.get(name), type);
 		}
 		return null;
 	}
 
 	public <T> T flash(String name, T defaultValue) {
 		if(flash != null) {
 			return coerce(flash.get(name), defaultValue);
 		}
 		return defaultValue;
 	}
 	
 	@Override
 	public Action getAction() {
 		return action;
 	}
 	
 	@Override
 	public String getActionName() {
 		return (action != null) ? action.name() : "handleRequest";
 	}
 	
 	public AppService getApplication() {
 		return handler;
 	}
 	
 	@Override
 	public <T extends Model> T getAuthenticated(Class<T> clazz) {
 		if(clazz != null) {
 			resolveSession(true);
 			if(clazz.getName() == session.get(AUTHENTICATED_BY_TYPE)) {
 				return coerce(session.get(AUTHENTICATED_BY), clazz);
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public String getCache(String key) {
 		CacheService cache = handler.getCacheService();
 		if(cache != null) {
 			return cache.get(createCacheKey(handler, key));
 		} else {
 			logger.warn("cache service is not available");
 			return null;
 		}
 	}
 	
 	protected String getCacheForAction() {
 		return getCacheForAction(action);
 	}
 
 	protected String getCacheForAction(Action action) {
 		if(ActionCache.isCaching(this, action)) {
 			return getCache(createActionCacheKey(getClass(), action, wants()));
 		}
 		return null;
 	}
 	
 	@Override
 	public String getControllerName() {
 		return getClass().getSimpleName();
 	}
 
 	@Override
 	public String getFlash(String name) {
 		if(flash != null) {
 			Object o = flash.get(name);
 			if(o != null) {
 				return coerce(o, String.class);
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public <T> T getFlash(String name, Class<T> type) {
 		return flash(name, type);
 	}
 	
 	@Override
 	public <T> T getFlash(String name, T defaultValue) {
 		return flash(name, defaultValue);
 	}
 	
 	@Override
 	public String getFlashError() {
 		return getFlash(FLASH_ERROR);
 	}
 	
 	@Override
 	public String getFlashNotice() {
 		return getFlash(FLASH_NOTICE);
 	}
 	
 	@Override
 	public String getFlashWarning() {
 		return getFlash(FLASH_WARNING);
 	}
 
 	public int getId() {
 		return param("id", int.class);
 	}
 	
 	public Logger getLogger() {
 		return logger;
 	}
 	
 	@Override
 	public Object getParam(String name) {
 		if(params != null) {
 			return params.get(name);
 		}
 		return null;
 	}
 	
 	@Override
 	public <T> T getParam(String name, Class<T> clazz) {
 		return coerce(getParam(name), clazz);
 	}
 	
 	@Override
 	public <T> T getParam(String name, T defaultValue) {
 		return coerce(getParam(name), defaultValue);
 	}
 	
 	@Override
 	public Map<String, Object> getParams() {
 		if(params != null) {
 			return params;
 		}
 		return new HashMap<String, Object>(0);
 	}
 	
 	public HttpRequest getRequest() {
 		return request;
 	}
 	
 	public Response getResponse() {
 		return response;
 	}
 	
 	public AppRouter getRouter() {
 		return appRouter;
 	}
 	
 	public HttpSession getSession() {
 		return getSession(true);
 	}
 
 	public HttpSession getSession(boolean create) {
 		resolveSession(create);
 		return session;
 	}
 	
 	/**
 	 * Implemented by subclasses, if necessary, to handle non-RESTful routes.
 	 * @throws SQLException
 	 * @see {@link Router#}
 	 */
 	public void handleRequest() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 	
 	@Override
 	public boolean hasFlash(String name) {
 		if(flash != null) {
 			return flash.containsKey(name);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean hasFlashError() {
 		return hasFlash(FLASH_ERROR);
 	}
 	
 	@Override
 	public boolean hasFlashNotice() {
 		return hasFlash(FLASH_NOTICE);
 	}
 
 	@Override
 	public boolean hasFlashWarning() {
 		return hasFlash(FLASH_WARNING);
 	}
 
 	@Override
 	public boolean hasParam(String name) {
 		if(params != null) {
 			return params.containsKey(name);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean hasParams() {
 		return params != null && !params.isEmpty();
 	}
 	
 	@Override
 	public boolean hasSession() {
 		resolveSession(false);
 		return session != null;
 	}
 	
 	public void initialize(Router router, HttpRequest request, Map<String, Object> routeParams) {
 		this.router = router;
 		this.request = request;
 		this.handler = (AppService) request.getHandler();
 		this.logger = handler.getLogger();
 		this.appRouter = handler.getRouter();
 		
 		Map<String, Object> rparams = request.getParameters();
 		if(rparams != null) {
 			Map<String, Object> params = new HashMap<String, Object>(rparams);
 			if(routeParams != null) {
 				params.putAll(routeParams);
 			}
 			this.params = mapParams(params);
 		} else if(routeParams != null) {
 			this.params = mapParams(routeParams);
 		}
 
 		String s = request.getCookieValue(FLASH_KEY);
 		if(s != null) {
 			flash = toMap(s);
 		}
 
 		sessionResolved = false;
 	}
 	
 	@Override
 	public boolean isAction(Action action) {
 		return getAction() == action;
 	}
 	
 	public boolean isAuthenticated() {
 		resolveSession(true);
 		long start = coerce(session.get(AUTHENTICATED_AT), long.class);
 		return (System.currentTimeMillis() - start) < AUTHENTICATION_INTERVAL;
 	}
 	
 	public boolean isAuthenticated(Model model) {
 		if(model != null) {
 			resolveSession(true);
 			if(model.getId() == coerce(session.get(AUTHENTICATED_BY), int.class) &&
 					model.getClass().getName().equals(session.get(AUTHENTICATED_BY_TYPE))) {
 				return isAuthenticated();
 			}
 		}
 		return false;
 	}
 	
 	protected boolean isAuthorized(Realm realm) {
 		return appRouter.isAuthorized(request, realm);
 	}
 	
 	protected boolean isAuthorized(String username, String password) {
 		return appRouter.isAuthorized(request, username, password);
 	}
 	
 	public boolean isRendered() {
 		return isRendered;
 	}
 	
 	@Override
 	public boolean isXhr() {
 		String header = request.getHeader(Header.X_REQUESTED_WITH);
 		return "XMLHttpRequest".equals(header);
 	}
 	
 	@Override
 	public String param(String name) {
 		return coerce(getParam(name), String.class);
 	}
 	
 	@Override
 	public <T> T param(String name, Class<T> clazz) {
 		return getParam(name, clazz);
 	}
 	
 	@Override
 	public <T> T param(String name, T defaultValue) {
 		return getParam(name, defaultValue);
 	}
 	
 	@Override
 	public Map<String, Object> params() {
 		return getParams();
 	}
 
 	public Map<String, Object> params(String...names) {
 		Map<String, Object> params = new HashMap<String, Object>();
 		for(String name : names) {
			params.put(name, getParam(name));
 		}
 		return params;
 	}
 	
 	@Override
 	public String pathTo(Class<? extends Model> modelClass) {
 		return appRouter.pathTo(router, modelClass);
 	}
 	
 	@Override
 	public String pathTo(Class<? extends Model> modelClass, Action action) {
 		return appRouter.pathTo(router, modelClass, action);
 	}
 	
 	@Override
 	public String pathTo(Model model) {
 		return appRouter.pathTo(router, model);
 	}
 	
 	@Override
 	public String pathTo(Model model, Action action) {
 		return appRouter.pathTo(router, model, action);
 	}
 
 	@Override
 	public String pathTo(Model parent, String field) {
 		return appRouter.pathTo(router, parent, field);
 	}
 	
 	@Override
 	public String pathTo(Model parent, String field, Action action) {
 		return appRouter.pathTo(router, parent, field, action);
 	}
 	
 	@Override
 	public String pathTo(String routeName) {
 		return appRouter.pathTo(router, routeName);
 	}
 	
 	@Override
 	public String pathTo(String routeName, Model model) {
 		return appRouter.pathTo(router, routeName, model);
 	}
 
 	@Override
 	public String pathTo(String routeName, Object... params) {
 		return appRouter.pathTo(router, routeName, params);
 	}
 	
 	public void redirectTo(Class<? extends Model> clazz, Action action) {
 		redirectTo(pathTo(clazz, action));
 	}
 	
 	public void redirectTo(Model model, Action action) {
 		redirectTo(model, action, null);
 	}
 	
 	public void redirectTo(Model model, Action action, String notice) {
 		if(action == showEdit) {
 			addFlashError(model);
 			setFlash(varName(model.getClass()), model);
 		} else if(action == showNew) {
 			addFlashError(model);
 			setFlash(varName(model.getClass()), model);
 		}
 		if(!blank(notice)) {
 			setFlashNotice(notice);
 		}
 		redirectTo(pathTo(model, action));
 	}
 
 	public void redirectTo(Model parent, String field, Action action) {
 		if(action == showEdit || action == showNew) {
 			Object fieldValue = parent.get(field);
 			if(fieldValue instanceof Model) {
 				addFlashError((Model) fieldValue);
 				if(action == showNew) {
 					setFlash(varName(fieldValue.getClass()), (Model) fieldValue);
 				}
 			}
 		}
 		redirectTo(pathTo(parent, field, action));
 	}
 
 	public void redirectTo(String path) {
 		rendering();
 		response = new Response(request.getType());
 		response.setStatus(StatusCode.REDIRECT);
 		response.addHeader(Header.LOCATION, path);
 	}
 	
 	public void redirectToHome() {
 		redirectTo("/");
 	}
 	
 	public void render(Collection<? extends Model> models) {
 		render(wants(), models);
 	}
 	
 	public void render(ContentType type, byte[] data) {
 		rendering();
 		response = new ByteArrayResponse(request.getType());
 		response.setStatus(StatusCode.OK);
 		response.setContentType(type);
 		((ByteArrayResponse) response).setData(data);
 	}
 
 	public void render(ContentType type, Collection<? extends Model> models) {
 		render(type, toJson(models));
 	}
 	
 	public void render(ContentType type, String body) {
 		rendering();
 		response = new Response(request.getType());
 		response.setStatus(StatusCode.OK);
 		response.setContentType(type);
 		if(blank(body) && (type == ContentType.JSON || type == ContentType.JS)) {
 			response.setBody("null");
 		} else {
 			response.setBody(body);
 		}
 	}
 	
 	public void render(DynamicAsset asset) {
 		rendering();
 		response = new Response(request.getType());
 		response.setStatus(StatusCode.OK);
 		response.setContentType(wants());
 		response.setBody(asset.getContent());
 	}
 	
 	public void render(Model model) {
 		render(wants(), model.toJson());
 	}
 	
 	/**
 	 * Convenience method for render(String.valueOf(object))
 	 * @param object
 	 */
 	public void render(Object object) {
 		render(String.valueOf(object));
 	}
 	
 	public void render(ScriptFile sf) {
 		render((DynamicAsset) sf);
 	}
 	
 	public void render(StatusCode status) {
 		render(status, status.getDescription());
 	}
 	
 	public void render(StatusCode status, String body) {
 		rendering();
 		response = new Response(request.getType());
 		response.setStatus(status);
 		response.setContentType(wants());
 		response.setBody(body);
 	}
 	
 	public void render(String body) {
 		rendering();
 		response = new Response(request.getType());
 		response.setStatus(StatusCode.OK);
 		response.setContentType(wants());
 		response.setBody(body);
 	}
 	
 	public void render(String body, Collection<?> values) {
 		if(values == null || values.isEmpty()) {
 			render(body);
 		} else {
 			render(body, values.toArray());
 		}
 	}
 	
 	public void render(String body, Map<String, Object> values) {
 		if(values == null || values.isEmpty()) {
 			render(body);
 		} else {
 			StringBuilder sb = new StringBuilder(body);
 			Pattern pattern = Pattern.compile("#\\{(\\w+)}");
 			Matcher matcher = pattern.matcher(sb);
 			for(int i = 0, start = 0; matcher.find(start); i++) {
 				String key = matcher.group(1);
 				Object val = values.containsKey(key) ? values.get(key) : ("#{" + key + ": *** UNKNOWN ***}");
 				sb.replace(matcher.start(), matcher.end(), String.valueOf(val));
 				start = matcher.end();
 				matcher = pattern.matcher(sb);
 			}
 			render(sb);
 		}
 	}
 	
 	public void render(String body, Object...values) {
 		if(values.length == 0) {
 			render(body);
 		} else {
 			StringBuilder sb = new StringBuilder(body);
 			Pattern pattern = Pattern.compile("#\\{\\?}");
 			Matcher matcher = pattern.matcher(sb);
 			for(int i = 0, start = 0; matcher.find(start); i++) {
 				sb.replace(matcher.start(), matcher.end(), String.valueOf(values[i]));
 				start = matcher.end();
 				matcher = pattern.matcher(sb);
 			}
 			render(sb);
 		}
 	}
 
 	public void render(StyleSheet ss) {
 		render((DynamicAsset) ss);
 	}
 	
 	public void render(View view) {
 		render(view, isXhr());
 	}
 	
 	public void render(View view, boolean partial) {
 		if(view == null) {
 			throw new IllegalArgumentException("view cannot be null");
 		}
 		if(logger.isLoggingDebug()) {
 			logger.debug("start render of view " + view.getClass().getCanonicalName());
 		}
 		rendering();
 		
 		try {
 			response = new Response(request.getType());
 			response.setStatus(StatusCode.OK);
 			response.setContentType(ContentType.HTML);
 			
 			ViewRenderer renderer = new ViewRenderer(this, view);
 			renderer.setPartial(partial);
 			
 			response.setBody(renderer.render());
 		} finally {
 			view.setRenderer(null);
 		}
 		logger.debug("end render of view");
 	}
 	
 	public void renderAccepted() {
 		render(StatusCode.ACCEPTED, wantsJS() ? "[]" : StatusCode.ACCEPTED.getDescription());
 	}
 	
 	public void renderCreated(int id) {
 		renderCreated((long) id);
 	}
 
 	public void renderCreated(long id) {
 		renderCreated(id, null);
 	}
 	
 	public void renderCreated(long id, String path) {
 		rendering();
 		response = new Response(request.getType());
 		response.setStatus(StatusCode.CREATED);
 		response.addHeader(Header.ID, String.valueOf(id));
 		if(!blank(path)) {
 			response.addHeader(Header.LOCATION, path);
 		}
 		if(wantsJS()) {
 			response.setContentType(wants());
 			response.setBody("null");
 		}
 	}
 
 	public void renderCreated(Model model) {
 		renderCreated((long) model.getId(), pathTo(model));
 	}
 
 	public void renderDestroyed(Model model) {
 		rendering();
 		response = new Response(request.getType());
 		response.setStatus(StatusCode.OK);
 		response.addHeader(Header.ID, String.valueOf(model.getId()));
 		if(wantsJS()) {
 			response.setContentType(wants());
 			response.setBody("null");
 		}
 	}
 
 	public void renderErrors(List<String> errors) {
 		if(errors != null) {
 			renderErrors(errors.toArray(new String[errors.size()]));
 		} else {
 			renderErrors(new String[0]);
 		}
 	}
 	
 	public void renderErrors(Model...models) {
 		rendering();
 		response = new Response(request.getType());
 		response.setStatus(StatusCode.CONFLICT);
 		response.setContentType(ContentType.JSON);
 		if(models.length == 0) {
 			response.setBody("[]");
 		} else if(models.length == 1) {
 			if(models[0] != null) {
 				response.setBody(toJson(models[0].getErrors()));
 			}
 		} else {
 			List<String> errors = new ArrayList<String>();
 			for(Model model : models) {
 				if(model != null) {
 					errors.addAll(model.getErrorsList());
 				}
 			}
 			response.setBody(toJson(errors));
 		}
 	}
 	
 	public void renderErrors(String...errors) {
 		rendering();
 		response = new Response(request.getType());
 		response.setStatus(StatusCode.CONFLICT);
 		response.setContentType(ContentType.JSON);
 		response.setBody(toJson(errors));
 	}
 
 	private void rendering() {
 		if(isRendered) {
 			throw new UnsupportedOperationException("cannot render more than once");
 		}
 		isRendered = true;
 	}
 	
 	public void renderJson(Collection<? extends Model> models, String include) {
 		if(blank(models)) {
 			render(ContentType.JSON, "[]");
 		} else {
 			String json = JsonBuilder.buildJson(models, include);
 			render(ContentType.JSON, json);
 		}
 	}
 	
 	public void renderJson(Object object) {
 		render(ContentType.JSON, format(toJson(object)));
 	}
 
 	public void renderOK() {
 		render(StatusCode.OK, wantsJS() ? "[]" : StatusCode.OK.getDescription());
 	}
 	
 	public void renderPage(String text) {
 		renderPage("", text);
 	}
 
 	public void renderPage(String title, String text) {
 		render(ContentType.PLAIN,
 				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
 				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
 				"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
 				"<head>\n" +
 			   	"<title>" + title + "</title>\n" +
 			   	"</head>\n" +
 			   	"<body>\n" +
 			   	text + "\n" +
 			   	"</body>\n" +
 				"</html>"
 		);
 	}
 
 	private void resolveSession(boolean create) {
 		if((create && session == null) || (!create && !sessionResolved)) {
 			sessionResolved = true;
 			HttpCookie cookie = request.getCookie(SESSION_ID_KEY);
 			if(cookie != null) {
 				try {
 					int id = Integer.parseInt(cookie.getValue());
 					cookie = request.getCookie(SESSION_UUID_KEY);
 					if(cookie != null) {
 						String uuid = cookie.getValue();
 						session = handler.getSession(id, uuid, create);
 					}
 				} catch(NumberFormatException e) {
 					logger.warn("invalid cookie id format: " + cookie.getValue());
 				}
 			} else if(create) {
 				session = handler.getSession(-1, null, true);
 			}
 		}
 	}
 
 	public void setCache(String key, String value) {
 		CacheService cache = handler.getCacheService();
 		if(cache != null) {
 			cache.set(createCacheKey(handler, key), value);
 		} else {
 			logger.warn("cache service is not available");
 		}
 	}
 
 	protected void setCacheForAction(Action action, String content) {
 		if(ActionCache.isCaching(this, action)) {
 			setCache(createActionCacheKey(getClass(), action, wants()), content);
 		}
 	}
 
 	protected void setCacheForAction(String content) {
 		setCacheForAction(action, content);
 	}
 
 	public void setFlash(String name, Object value) {
 		if(flash == null) {
 			flash = new HashMap<String, Object>();
 		}
 		if(flashOut == null) {
 			flashOut = new HashMap<String, Object>();
 		}
 		if(value == null) {
 			flash.remove(name);
 			flashOut.remove(name);
 		} else {
 			flash.put(name, value);
 			flashOut.put(name, value);
 		}
 	}
 
 	public void setFlashError(Model model) {
 		if(model.hasErrors()) {
 			List<String> errors = new ArrayList<String>();
 			for(String error : model.getErrorsList()) {
 				errors.add(error);
 			}
 			setFlash(FLASH_ERROR, errors);
 		}
 	}
 
 	public void setFlashError(Model... models) {
 		List<String> errors = new ArrayList<String>();
 		for(Model model : models) {
 			for(String error : model.getErrorsList()) {
 				errors.add(error);
 			}
 		}
 		if(!errors.isEmpty()) {
 			setFlash(FLASH_ERROR, errors);
 		}
 	}
 
 	public void setFlashError(Object value) {
 		setFlash(FLASH_ERROR, value);
 	}
 	
 	public void setFlashNotice(Object value) {
 		setFlash(FLASH_NOTICE, value);
 	}
 	
 	public void setFlashWarning(Object value) {
 		setFlash(FLASH_ERROR, value);
 	}
 	
 	public void setParam(String name, Object value) {
 		if(params == null) {
 			params = new HashMap<String, Object>();
 		}
 		if(value == null) {
 			params.remove(name);
 		} else {
 			params.put(name, value);
 		}
 	}
 	
 	/**
 	 * GET url/[model]s/1
 	 * Implemented by subclasses, if necessary, to show (retrieve) a model.
 	 * @throws SQLException
 	 */
 	public void show() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	/**
 	 * GET url/[model]s
 	 * Implemented by subclasses, if necessary, to show (retrieve) all models.
 	 * @throws SQLException
 	 */
 	public void showAll() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	/**
 	 * GET url/[model]s/1/edit
 	 * Implemented by subclasses, if necessary, to show the edit page for a model.
 	 * @throws SQLException
 	 */
 	public void showEdit() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	/**
 	 * GET url/[model]s/new
 	 * Implemented by subclasses, if necessary, to show the new page for a model.
 	 * @throws SQLException
 	 */
 	public void showNew() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	/**
 	 * PUT url/[model]s/1
 	 * Implemented by subclasses, if necessary, to update a model.
 	 * @throws SQLException
 	 */
 	public void update() throws SQLException {
 		// to be implemented by subclasses if needed
 	}
 
 	@Override
 	public String urlTo(Class<? extends Model> modelClass) {
 		return appRouter.urlTo(router, modelClass);
 	}
 
 	@Override
 	public String urlTo(Class<? extends Model> modelClass, Action action) {
 		return appRouter.urlTo(router, modelClass, action);
 	}
 
 	@Override
 	public String urlTo(Model model) {
 		return appRouter.urlTo(router, model);
 	}
 
 	@Override
 	public String urlTo(Model model, Action action) {
 		return appRouter.urlTo(router, model, action);
 	}
 
 	@Override
 	public String urlTo(Model parent, String field) {
 		return appRouter.urlTo(router, parent, field);
 	}
 
 	@Override
 	public String urlTo(Model parent, String field, Action action) {
 		return appRouter.urlTo(router, parent, field, action);
 	}
 
 	@Override
 	public String urlTo(String routeName) {
 		return appRouter.urlTo(router, routeName);
 	}
 
 	@Override
 	public String urlTo(String routeName, Model model) {
 		return appRouter.urlTo(router, routeName, model);
 	}
 
 	@Override
 	public String urlTo(String routeName, Object... params) {
 		return appRouter.urlTo(router, routeName, params);
 	}
 	
 	@Override
 	public ContentType wants() {
 		ContentType[] requestTypes = request.getContentTypes();
 		if(requestTypes != null && requestTypes.length > 0) {
 			return requestTypes[0];
 		}
 		return ContentType.UNKNOWN;
 	}
 
 	@Override
 	public ContentType wants(ContentType...options) {
 		ContentType[] requestTypes = request.getContentTypes();
 		if(!blank(requestTypes)) {
 			if(blank(options)) {
 				return request.getContentTypes()[0];
 			} else {
 				for(ContentType accept : requestTypes) {
 					if(accept.isWild()) {
 						return options[0];
 					} else {
 						for(ContentType produce : options) {
 							if(accept == produce) {
 								return accept;
 							}
 						}
 					}
 				}
 			}
 		}
 		if(!blank(options)) {
 			return options[options.length-1];
 		}
 		return ContentType.UNKNOWN;
 	}
 
 	@Override
 	public boolean wants(ContentType type) {
 		ContentType wants = wants();
 		return wants.isWild() || wants == type;
 	}
 
 	@Override
 	public boolean wantsHtml() {
 		return wants(ContentType.HTML);
 	}
 
 	@Override
 	public boolean wantsImage() {
 		ContentType wants = wants();
 		return wants.isWild() || wants.isImage();
 	}
 	
 	@Override
 	public boolean wantsJS() {
 		return wants(ContentType.JS) || wants(ContentType.JSON);
 	}
 	
 	@Override
 	public boolean wantsJSON() {
 		return wants(ContentType.JSON);
 	}
 	
 }

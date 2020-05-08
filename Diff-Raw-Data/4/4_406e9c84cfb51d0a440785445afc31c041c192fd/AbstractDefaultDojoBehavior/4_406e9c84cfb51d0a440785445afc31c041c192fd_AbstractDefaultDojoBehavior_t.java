 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.wicketstuff.dojo;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.MetaDataKey;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.IAjaxCallDecorator;
 import org.apache.wicket.ajax.IAjaxIndicatorAware;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.resources.CompressedResourceReference;
 import org.wicketstuff.dojo.indicator.DojoIndicatorHandlerHelper;
 import org.wicketstuff.dojo.indicator.behavior.DojoIndicatorBehavior;
 
 /**
  * Handles event requests using Dojo.
  * <p>
  * This class is mainly here to automatically add the javascript files you need.
  * As header contributions are done once per class, you can have multiple
  * instances/ subclasses without having duplicate header contributions.
  * </p>
  * <p> this class use {@link AjaxRequestTarget} to respond to XMLHttpRequest </p>
  * <p> 
  * this behavior can work with a {@link DojoIndicatorBehavior} to set up an Indicator when a request 
  * has been sent and waiting for the response. This Behavior auto manage Indicator.
  * </p>
  * 
  * <p>
  * By default this behavior will use the package dojo distributiuon included in this jar. If you want to use an other 
  * Dojo Distribution (A custom one to fit to your need), You should write the following code in your {@link Application} to
  * use a custom {@link CompressedResourceReference}
  * <pre>
  *  CompressedResourceReference myCustomDojo = new CompressedResourceReference([...]);
  * 	setMetaData(AbstractDefaultDojoBeahvior.USE_CUSTOM_DOJO_DIST, myCustomDojo);
  * </pre>
  * <b>WARNING</b> : the package dojo distribution contains some patches on dojo. If you use your own
  * distribution it can broke some component beahviors.
  * </p>
  * 
  * @see <a href="http://dojotoolkit.org/">Dojo</a>
  * @author Eelco Hillenius
  */
 public abstract class AbstractDefaultDojoBehavior extends AbstractDefaultAjaxBehavior implements IAjaxIndicatorAware
 {
 	private static final long serialVersionUID = 1L;
 	public static boolean USE_DOJO_UNCOMPRESSED = false;
 	
 	/** 
 	 * a Unique key to know if a CompressedResourceReference is set by the user 
 	 * in order to use a custom dojo distribution
 	 */
 	public static final MetaDataKey USE_CUSTOM_DOJO_DIST = new MetaDataKey(CompressedResourceReference.class){
 		private static final long serialVersionUID = 1L;
 	};
 	
 	/** reference to the dojo support javascript file. */
 	public static final ResourceReference DOJO = new CompressedResourceReference(
 			AbstractDefaultDojoBehavior.class, "dojo-0.4/dojo.js");
 	public static final ResourceReference DOJO_UNCOMPRESSED = new CompressedResourceReference(
 			AbstractDefaultDojoBehavior.class, "dojo-0.4/dojo.js.uncompressed.js");
 	public static final ResourceReference DOJO_WICKET =  new CompressedResourceReference(
 			AbstractRequireDojoBehavior.class, "dojo-wicket/dojoWicket.js");
 
 	/** A unique ID for the JavaScript Dojo debug script */
 	private static final String JAVASCRIPT_DOJO_DEBUG_ID = AbstractDefaultDojoBehavior.class.getName() + "/debug";
 
 	/** A unique ID for the JavaScript Dojo console debug */
 	private static final String JAVASCRIPT_DOJO_CONSOLE_DEBUG_ID = AbstractDefaultDojoBehavior.class.getName() + "/consoleDebug";
 
 	/** A unique ID for a piece of JavaScript that registers the wicketstuff dojo namespace */
 	private static final String DOJO_NAMESPACE_PREFIX = AbstractDefaultDojoBehavior.class.getName() + "/namespaces/";
 	
 	/** The wicketstuff dojo module */
 	private static final DojoModule WICKETSTUFF_MODULE = new DojoModuleImpl("wicketstuff", AbstractDefaultDojoBehavior.class);
 
 	/**
 	 * @see wicket.ajax.AbstractDefaultAjaxBehavior#renderHead(wicket.markup.html.IHeaderResponse)
 	 */
 	public void renderHead(IHeaderResponse response)
 	{
 		super.renderHead(response);
 
 		// enable dojo debug if our configuration type is DEVELOPMENT
 		final String configurationType = Application.get().getConfigurationType();
 		if (configurationType.equalsIgnoreCase(Application.DEVELOPMENT)) {
 			StringBuffer debugScript = new StringBuffer();
 			debugScript.append("var djConfig = {};\n");
 			debugScript.append("djConfig.isDebug = true;\n");
 			response.renderJavascript(debugScript.toString(), JAVASCRIPT_DOJO_DEBUG_ID);
 		}
 		
 		// if a CompressedResourceReference to a custom Dojo script is set as
 		// metada of the application use it instead of the default one
 		DojoLocaleManager.getInstance().renderLocale(response);
 		response.renderJavascriptReference(getDojoResourceReference());
 		response.renderJavascriptReference(DOJO_WICKET);
 		
 		// register the wicketstuff namespace
 		for (DojoModule module: getDojoModules()) {
 			registerDojoModulePath(response, module);
 		}
 		
 		// debug on firebug console if it is installed, otherwise it will just
 		// end up at the bottom of the page
 		if (configurationType.equalsIgnoreCase(Application.DEVELOPMENT)) {
 			StringBuffer consoleDebugScript = new StringBuffer();
 			consoleDebugScript.append("dojo.require(\"dojo.debug.console\");\n");
 			consoleDebugScript.append("dojo.require(\"dojo.widget.Tree\");\n");
 			response.renderJavascript(consoleDebugScript.toString(), JAVASCRIPT_DOJO_CONSOLE_DEBUG_ID);
 		}
 	}
 	
 	/**
 	 * Register a specific dojo module.
 	 * @param response
 	 * @param namespace
 	 * @param path
 	 */
 	public void registerDojoModulePath(IHeaderResponse response, DojoModule module) {
 		ResourceReference dojoReference = getDojoResourceReference();
 		String dojoUrl = RequestCycle.get().urlFor(dojoReference).toString();
 		
 		// count the depth to determine the relative path
 		String url = "";
 		int last = 0;
 		while (last > -1) {
 			last = dojoUrl.indexOf("/", last + 1);
 			if (last > -1) {
 				url += "../";
 			}
 		}
 		
 		ResourceReference moduleReference = new ResourceReference(module.getScope(), "");
 		String moduleUrl = RequestCycle.get().urlFor(moduleReference).toString();
 		url = url + moduleUrl;
		//remove / at the end if exists
		if (url.charAt(url.length()-1) == '/'){
			url = url.substring(0, url.length() -1);
		}
 				
 		response.renderJavascript(
 				"dojo.registerModulePath(\"" + module.getNamespace() + "\", \"" + url + "\");",
 				DOJO_NAMESPACE_PREFIX + module.getNamespace());
 	}
 
 	/**
 	 * Get the reference to the Dojo scripts.
 	 * @return
 	 */
 	public ResourceReference getDojoResourceReference() {
 		if (Application.get().getMetaData(USE_CUSTOM_DOJO_DIST) == null || !(Application.get().getMetaData(USE_CUSTOM_DOJO_DIST) instanceof CompressedResourceReference)){
 			if (USE_DOJO_UNCOMPRESSED){
 				return DOJO_UNCOMPRESSED;
 			}else{
 				return DOJO;
 			}
 		}else{
 			return (CompressedResourceReference)Application.get().getMetaData(USE_CUSTOM_DOJO_DIST);
 		}
 	}
 	
 	/**
 	 * return the indicator Id to show it if it is in the page
 	 * @return the indicator Id to show it if it is in the page
 	 */
 	public String getAjaxIndicatorMarkupId()
 	{
 		return new DojoIndicatorHandlerHelper(getComponent()).getAjaxIndicatorMarkupId();
 	}
 
 	/**
 	 * return the ajax call decorator to do more than hide or show an image
 	 * @return the ajax call decorator to do more than hide or show an image
 	 */
 	protected IAjaxCallDecorator getAjaxCallDecorator()
 	{
 		return new DojoIndicatorHandlerHelper(getComponent()).getAjaxCallDecorator();
 	}
 	
 	/**
 	 * Returns the collection of modules that should be registered.
 	 * @return
 	 */
 	protected final Collection<DojoModule> getDojoModules() {
 		Collection<DojoModule> modules = new ArrayList<DojoModule>();
 		setDojoModules(modules);
 		return modules;
 	}
 	
 	/**
 	 * Allow classes to override this and add their own modules.
 	 * @param modules
 	 */
 	protected void setDojoModules(Collection<DojoModule> modules) {
 		modules.add(WICKETSTUFF_MODULE);
 	}
 	
 	/**
 	 * Provides information about a dojo module.
 	 * 
 	 * @author B. Molenkamp
 	 */
 	public static interface DojoModule {
 		
 		/**
 		 * Returns the scope. This scope will be used to calculate the relative
 		 * path to this module.
 		 * 
 		 * @return the scope
 		 */
 		public Class getScope();
 		
 		/**
 		 * Returns the namespace of this module.
 		 * 
 		 * @return the module's namespace
 		 */
 		public String getNamespace();
 		
 	}
 	
 	/**
 	 * Abstract implementation of a dojo module. If the scope is not passed to
 	 * the constructor, or if it's null, it will use it's own class as a scope.
 	 * 
 	 * @author B. Molenkamp
 	 */
 	public static class DojoModuleImpl implements DojoModule {
 
 		private String moduleNamespace;
 		private Class scope;
 		
 		/**
 		 * Creates a module with the scope of the implementing class.
 		 * @param namespace
 		 */
 		public DojoModuleImpl(String namespace) {
 			this(namespace, null);
 		}
 		
 		/**
 		 * Creates a module with the given namespace at the given scope. It will
 		 * use the scope to resolve anything from the namespace.
 		 * 
 		 * @param namespace
 		 * @param scope
 		 */
 		public DojoModuleImpl(String namespace, Class scope) {
 			this.moduleNamespace = namespace;
 			this.scope = scope == null? this.getClass(): scope;
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.wicketstuff.dojo.AbstractDefaultDojoBehavior.DojoModule#getNamespace()
 		 */
 		public String getNamespace() {
 			return this.moduleNamespace;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.wicketstuff.dojo.AbstractDefaultDojoBehavior.DojoModule#getScope()
 		 */
 		public Class getScope() {
 			return this.scope;
 		}
 		
 	}
 }

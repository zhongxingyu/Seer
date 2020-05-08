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
 
 
 import org.apache.wicket.Application;
 import org.apache.wicket.MetaDataKey;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.IAjaxCallDecorator;
 import org.apache.wicket.ajax.IAjaxIndicatorAware;
 import org.wicketstuff.dojo.indicator.DojoIndicatorHandlerHelper;
 import org.wicketstuff.dojo.indicator.behavior.DojoIndicatorBehavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.resources.CompressedResourceReference;
 
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
 		
		//if a CompressedResourceReference is set as metada of the applciation use it instead of default Dojo
 		DojoLocaleManager.getInstance().renderLocale(response);
 		if (Application.get().getMetaData(USE_CUSTOM_DOJO_DIST) == null || !(Application.get().getMetaData(USE_CUSTOM_DOJO_DIST) instanceof CompressedResourceReference)){
 			if (USE_DOJO_UNCOMPRESSED){
 				response.renderJavascriptReference(DOJO_UNCOMPRESSED);
 			}else{
 				response.renderJavascriptReference(DOJO);
 			}
 		}else{
 			response.renderJavascriptReference((CompressedResourceReference)Application.get().getMetaData(USE_CUSTOM_DOJO_DIST));
 		}
 		response.renderJavascriptReference(DOJO_WICKET);
 		
 		// debug on firebug console if it is installed, otherwise it will just
		// end up at the bottom of the page.
 		if (configurationType.equalsIgnoreCase(Application.DEVELOPMENT)) {
 			StringBuffer consoleDebugScript = new StringBuffer();
 			consoleDebugScript.append("dojo.require(\"dojo.debug.console\");\n");
 			consoleDebugScript.append("dojo.require(\"dojo.widget.Tree\");\n");
 			response.renderJavascript(consoleDebugScript.toString(), JAVASCRIPT_DOJO_CONSOLE_DEBUG_ID);
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
 	
 }

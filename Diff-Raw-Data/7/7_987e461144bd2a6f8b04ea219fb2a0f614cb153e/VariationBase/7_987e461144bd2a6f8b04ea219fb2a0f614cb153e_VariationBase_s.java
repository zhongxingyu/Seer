 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package jp.dip.komusubi.lunch.wicket.page;
 
 import jp.dip.komusubi.lunch.Configuration;
 import jp.dip.komusubi.lunch.Configuration.RuntimeMode;
 import jp.dip.komusubi.lunch.wicket.WicketSession;
 import jp.dip.komusubi.lunch.wicket.page.account.Setting;
 import jp.dip.komusubi.lunch.wicket.panel.Header.HeaderBean;
 
 import org.apache.log4j.MDC;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.protocol.http.RequestUtils;
 import org.apache.wicket.protocol.http.request.WebClientInfo;
 import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
 import org.apache.wicket.request.Url.StringMode;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class VariationBase extends WebPage {
 	
 	private static final long serialVersionUID = 2756031824182370952L;
 	private static Logger logger = LoggerFactory.getLogger(VariationBase.class);
 	private WebClientInfo clientInfo = WicketSession.get().getClientInfo();
 	
 	public VariationBase(final PageParameters params) {
 		
 	}
 	public VariationBase() {
 		ServletWebRequest request = (ServletWebRequest) getRequestCycle().getRequest();
 //		MDC.put("ipaddr", clientInfo.getProperties().getRemoteAddress());
 		MDC.put("ipaddr", request.getContainerRequest().getRemoteAddr());
 		MDC.put("sessionId", request.getContainerRequest().getSession().getId());
 		logger.info("[start] lunchat {}", request.getClientUrl().toString(StringMode.FULL));
 		if (logger.isDebugEnabled()) {
 			logger.debug("user agent is {} ", clientInfo.getUserAgent());
 		}
 	}
 	
 	public boolean isJQuery() {
 		if (clientInfo.getUserAgent().contains("Android") ||
 				clientInfo.getUserAgent().contains("iPhone")) {
 			return true;
 		}
 //		return false;
 		return true;
 	}
 	
 	@Override
 	public String getVariation() {
 		String variation = null;
 		if (isJQuery()) 
 			variation = WicketSession.VARIATION_JQUERY_MOBILE;
 		
 		return variation;
 	}
 
 	private static final String analytics =
             "var _gaq = _gaq || [];"
             + "_gaq.push(['_setAccount', 'UA-26541332-2']);"
 	        + "_gaq.push(['_setDomainName', 'lunchat.jp']);"
 	        + "_gaq.push(['_trackPageview']);"
 	        + "(function() {"
 	        + "var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;"
 	        + "ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';"
 	        + "var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);"
             + "})();";
 
 	private static final String disabledAjax = "$(document).bind(\"mobileinit\", function(){"
 	        + "$.mobile.ajaxEnabled = false;});";
 	
 	@Override
 	public void renderHead(IHeaderResponse response) {
 		super.renderHead(response);
 		if (isJQuery()) {
			response.renderCSSReference("/css/jquery.mobile.min.css");
			response.renderJavaScriptReference("/js/jquery-1.6.4.min.js");
 			response.renderJavaScript(disabledAjax, null);
			response.renderJavaScriptReference("/js/jquery.mobile.min.js");
 		}
 		if (RuntimeMode.DEPLOYMENT.equals(Configuration.mode()))
 			response.renderJavaScript(analytics, null);
 	}
 	
 	protected HeaderBean getDefaultHeaderBean(String pageTitle) {
 		HeaderBean bean = new HeaderBean();
 		bean.config = Setting.class;
 		bean.pageTitle = pageTitle;
 		// FIXME navigation panel link address.
 		bean.order = null;
 		bean.history = null;
 		return bean;
 	}
 	
 	protected String getPageUrl(Class<? extends WebPage> clazz) {
 		// get this page's url
 	    // MEMO cann't get "http://localhost:8080/" it's example for "http://localhost:8080/group/.."
 	    // I don't know if bug, because it is not clear mistake.
 		String targetPath = getRequestCycle().urlFor(clazz, null).toString();
 		String ownUrl = getRequestCycle().getUrlRenderer().renderFullUrl(getRequest().getClientUrl());
 		return RequestUtils.toAbsolutePath(ownUrl, targetPath);
 	}
 	
 	protected String getPageUrl(WebPage page) {
 		return getPageUrl(page.getClass());
 	}
 	
 //	protected String getPageAbsoluteUrl(Class<? extends WebPage> clazz) {
 //	    return getPageAbsoluteUrl(clazz, null);
 //	}
 //	
 //	protected String getPageAbsoluteUrl(Class<? extends WebPage> clazz, String relatedPath) {
 //	    String related = "";
 //	    if (relatedPath != null)
 //	        related = relatedPath;
 //	    String path = getRequestCycle().urlFor(clazz, null).toString();
 //	    return RequestUtils.toAbsolutePath(path, related);
 //	}
 }

 /**
  * Copyright 2013 Simon Curd <simoncurd@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.hula.web.filter;
 
 import gumi.builders.UrlBuilder;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 import com.hula.web.WebConstants;
 import com.hula.web.model.Route;
 import com.hula.web.model.Script;
 import com.hula.web.service.routing.RouteService;
 import com.hula.web.service.script.ScriptService;
 import com.hula.web.service.script.exception.ScriptNotFoundException;
 import com.hula.web.service.script.exception.ScriptParseException;
 
 /**
  * Filter responsible for recognising Hula requests, as well as channel switching
  * between secure/non-secure URLs
  */
 @Singleton
 public class HulaWebFilter implements Filter
 {
 	private static Logger logger = LoggerFactory.getLogger(HulaWebFilter.class);
 
 	private String httpPort = null;
 	private String httpsPort = null;
 	private ScriptService scriptService = null;
 	private RouteService routeService = null;
 
 	@Inject
 	public HulaWebFilter(ScriptService scriptService, RouteService routeService, @Named("http.port") String httpPort, @Named("https.port") String httpsPort)
 	{
 		this.scriptService = scriptService;
 		this.routeService = routeService;
 		this.httpPort = httpPort;
 		this.httpsPort = httpsPort;
 	}
 
 	@Override
 	public void doFilter(ServletRequest baseRequest, ServletResponse baseResponse, FilterChain fc) throws IOException, ServletException
 	{
 		HttpServletRequest request = (HttpServletRequest) baseRequest;
 		HttpServletResponse response = (HttpServletResponse) baseResponse;
 
 		String requestURL = request.getRequestURL().toString();
 		String scriptName = request.getServletPath().substring(1);
 
 		// this filters out requests for non-hula resources
 		if (!scriptService.hasScript(scriptName))
 		{
 			
 			Route route = routeService.getRoute(request.getServletPath());
 			if (route == null)
 			{
				fc.doFilter(baseRequest, baseResponse);
 				return;
 			}
 			scriptName = route.getScript();
 		}
 		logger.info("request [{}]", requestURL);
 		logger.info("script [{}]", scriptName);
 
 		// load the script
 		Script script = null;
 		try
 		{
 			script = scriptService.getScript(scriptName);
 		}
 		catch (ScriptNotFoundException e)
 		{
 			logger.error("error loading script", e);
 			response.sendError(404);
 			return;
 		}
 		catch (ScriptParseException e)
 		{
 			throw new RuntimeException("error parsing script", e);
 		}
 
 		// check if we need to switch channel
 		String alternativeChannelURL = getAlternativeChannelURL(script, request);
 		if (alternativeChannelURL != null)
 		{
 
 			logger.info("redirecting to [" + alternativeChannelURL + "]");
 			response.sendRedirect(alternativeChannelURL);
 			return;
 		}
 
 		// forward to the servlet
 		request.setAttribute(WebConstants.ScriptName, scriptName);
 		RequestDispatcher rd = request.getRequestDispatcher("/exec");
 		rd.forward(baseRequest, baseResponse);
 	}
 
 	/**
 	 * Check if we need to switch channel, and return the redirection URL.
 	 * 
 	 * @param script The script to be executed
 	 * @param request The incoming request
 	 * @return URL to redirect to, or null if not required
 	 */
 	protected String getAlternativeChannelURL(Script script, HttpServletRequest request)
 	{
 		String url = request.getRequestURL().toString().toLowerCase();
 		UrlBuilder urlBuilder = UrlBuilder.fromString(url);
 
 		// if secure status matches, no channel switch is required
 		if (script.isSecure() == urlBuilder.scheme.equals("https"))
 		{
 			return null;
 		}
 
 		// rewrite URL
 		if (script.isSecure())
 		{
 			urlBuilder = urlBuilder.withScheme("https").withPort(new Integer(httpsPort));
 		}
 		else
 		{
 			urlBuilder = urlBuilder.withScheme("http").withPort(new Integer(httpPort));
 		}
 
 		// don't need to specify default port values
 		if (urlBuilder.port == 80 || urlBuilder.port == 443)
 		{
 			urlBuilder = urlBuilder.withPort(null);
 		}
 
 		return urlBuilder.toString();
 	}
 
 	@Override
 	public void init(FilterConfig fc) throws ServletException
 	{
 	}
 
 	@Override
 	public void destroy()
 	{
 	}
 }

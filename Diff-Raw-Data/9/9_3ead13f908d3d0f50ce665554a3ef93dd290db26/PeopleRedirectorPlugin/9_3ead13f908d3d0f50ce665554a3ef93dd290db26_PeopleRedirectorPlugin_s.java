 /*
  * The MIT License
  * 
  * Copyright (c) 2011, Cisco Systems, Inc., Max Spring
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.cisco.step.jenkins.plugins.people.redirector;
 
 import hudson.Plugin;
 import hudson.model.Descriptor.FormException;
 import hudson.util.PluginServletFilter;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.StaplerRequest;
 
 public class PeopleRedirectorPlugin extends Plugin {
 	private final static Logger LOG = Logger.getLogger(PeopleRedirectorPlugin.class.getName());
 	
 	private String redirectTarget;
 	private boolean disabled;
 	
	public void setRedirectTarget(String redirectTarget) {
		this.redirectTarget = redirectTarget;
	}
	
 	public String getRedirectTarget() {
 		return redirectTarget;
 	}
 
 	public boolean isDisabled() {
 		return disabled;
 	}
 
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

 	@Override
 	public void start() throws Exception{
 		load();
 		PluginServletFilter.addFilter(new Filter(){
 			
 			public void init(FilterConfig fc) throws ServletException{
 			}
 
 			public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain) throws IOException, ServletException{
 				String uri = ((HttpServletRequest)req).getRequestURI();
 				
 				if ( !disabled && redirectTarget != null && redirectTarget.length() > 0 && uri.startsWith("/user/")){
 					String username = uri.substring(6);
 					if (username.indexOf('/') < 0){
 						HttpServletResponse hrsp = (HttpServletResponse)rsp;
 						hrsp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
 						
 						String newUri = redirectTarget.replace("${user}",username);
 						LOG.fine("redirecting to "+newUri);
 						hrsp.addHeader("Location",newUri);
 						return;
 					}
 				}
 				
 				chain.doFilter(req, rsp);
 			}
 
 			public void destroy(){
 			}
 		});
 	}
 	
 	public String getHelpFile(){
 		return "";
 	}
 
 	@Override
 	public void configure(StaplerRequest req, JSONObject formData) throws IOException, ServletException, FormException{
 		redirectTarget = formData.getString("redirectTarget");
 		disabled = formData.getBoolean("disabled");
 		save();
 	}
 }

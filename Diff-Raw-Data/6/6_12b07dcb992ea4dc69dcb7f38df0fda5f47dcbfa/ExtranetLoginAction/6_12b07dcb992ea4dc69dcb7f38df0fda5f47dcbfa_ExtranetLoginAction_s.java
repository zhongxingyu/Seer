 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.deliver.applications.actions;
 
 import java.net.URLEncoder;
 import java.security.Principal;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
 import org.infoglue.deliver.util.HttpUtilities;
 
 /**
  * This class is meant to be the authentication central for extranet users.
  * It has methods for login-forms, authenticate-methods and much more.
  */
  
 public final class ExtranetLoginAction extends WebworkAbstractAction
 {
 	private String userName     	= null;
 	private String password     	= null;
 	private String errorMessage 	= "";
 	private String returnAddress 	= null;
 	private String referer 			= null;
 	
 	
 	public String doExecute() throws Exception 
 	{
 		return "success";
 	}	
 
 	public String doLoginForm() throws Exception 
 	{
 		return "loginForm";
 	}	
 
 	public String doNoAccess() throws Exception 
 	{
 		return "noAccess";
 	}
 	
 	public String doInvalidLogin() throws Exception 
 	{
 		return "invalidLogin";
 	}
 	
 	// To check access 
 	public String doCheckUser() throws Exception
 	{
	    Map arguments = new HashMap();
	    arguments.put("userName", userName);
	    arguments.put("password", password);
	    
 		if(ExtranetController.getController().getAuthenticatedPrincipal(arguments)!=null)
 			return "granted";
 		else
 			return "denied";
 	}
 	
 	public String doAuthenticateUser() throws Exception 
 	{
 		boolean isAuthenticated = false;
 		
 		HttpServletRequest hreq  = this.getRequest();
 		HttpServletResponse hres = this.getResponse();
 		
 		//Principal principal = (Principal)this.getHttpSession().getAttribute("infoglueExtranetPrincipal");
 		//if(principal == null) 
 		//{
 			//CmsLogger.logInfo("Authenticating username '" + userName + "'");
 			Principal principal = null;
 			try
 			{
 			    Map arguments = HttpUtilities.requestToHashtable(hreq);
 			    
 				principal = ExtranetController.getController().getAuthenticatedPrincipal(arguments);
 			}
 			catch(Exception e)
 			{
 				
 			}
 			
 			if(principal != null) 
 			{
 				isAuthenticated = true;
 			}
 		//}
 		//else
 		//{	
 			//CmsLogger.logInfo("Already authenticated '" + principal.getName() + "'");
 		//	isAuthenticated = true;
 		//}
 		
 		//CmsLogger.logInfo("isAuthenticated:" + isAuthenticated);		
 		if(isAuthenticated)
 		{
 			//CmsLogger.logInfo("Yes - we try to send the user back to:" + this.returnAddress);		
 			this.getHttpSession().setAttribute("infogluePrincipal", principal);
 			this.getResponse().sendRedirect(this.returnAddress);
 		}
 		else
 		{
 			//CmsLogger.logInfo("No - we try to send the back to the lofin screen.");		
 			errorMessage = "The logon information given was incorrect, please verify and try again.";
 			return "invalidLogin";
 		}
 		
 		return NONE;
 	}	
 	
 	public String doLogout() throws Exception
 	{
 		getHttpSession().invalidate();
 		this.getResponse().sendRedirect(this.returnAddress);
 		return NONE;
 	}
 	
 	public ExtranetLoginAction getThis()
 	{
 		return this;
 	}
 	
 	public String urlEncode(String string, String encoding)
 	{
 		String endodedString = string;
 		try
 		{
 			endodedString = URLEncoder.encode(string, encoding);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return endodedString;
 	}
 	
 	public void setUserName(String userName)
 	{
 		this.userName = userName;
 	}
 	
 	public String getUserName()
 	{
 		return this.userName;
 	}
 
 	public void setPassword(String password)
 	{
 		this.password = password;
 	}
 	
 	public String getPassword()
 	{
 		return this.password;
 	}
 	
 	public void setJ_username(String userName)
 	{
 		this.userName = userName;
 	}
 	
 	public String getJ_username()
 	{
 		return this.userName;
 	}
 
 	public void setJ_password(String password)
 	{
 		this.password = password;
 	}
 	
 	public String getJ_password()
 	{
 		return this.password;
 	}
 
 	public String getErrorMessage()
 	{
 		return this.errorMessage;
 	}
 
 	public String getReturnAddress()
 	{
 		return this.returnAddress;
 	}
 
 	public void setReturnAddress(String returnAddress)
 	{
 		this.returnAddress = returnAddress;
 	}
 
 	public String getReferer()
 	{
 		return referer;
 	}
 
 	public void setReferer(String referer)
 	{
 		this.referer = referer;
 	}
 
 }

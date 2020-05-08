 /**
  * Copyright 2012 Winflow Financial Group Corporation.
  *      http://www.winflowfinancial.com
  */
 package controllers;
 
 import play.mvc.*;
 import play.mvc.Http.*;
 
 /**
  * 	Handles authentication
  */
 public class Secured extends Security.Authenticator {
 
 	/**
	 *	Retrieves the username from the HTTP context; the default is to
	 *	read from the session cookie. Null if the user is not authenticated.
 	 */
 	@Override
 	public String getUsername(Context ctx) {
 		return ctx.session().get("email");
 	}
 
 	/**
	 *	Generates an alternative result if the user is not authenticated; 
	 *	the default a simple '401 Not Authorized' page.
 	 */
 	@Override
 	public Result onUnauthorized(Context ctx) {
 		return redirect(routes.Application.login());
 	}
 }

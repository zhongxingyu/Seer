 package com.websocketchat.actions;
 
 import com.websocketchat.bl.facades.IAccountFacade;
 import com.websocketchat.bl.model.User;
 import com.websocketchat.utils.spring.SpringContextUtil;
 
 public class LoginAction extends BaseActionSupport {
 
 	private static final long serialVersionUID = 1L;
 
 	private IAccountFacade accountFacade = (IAccountFacade) SpringContextUtil.getBean("accountFacade");
 
 	private String username;
 	private String password;
 
 	@Override
 	public String execute() throws Exception {
 		User user = accountFacade.login(username, password);
 		if (null != user) {
			// 1. Set the current user for current session
			request.getSession(true).setAttribute("currentUser", user);

			// 2. Write back with user's nickname
 			response.setContentType("application/json");
 			String serializedRespMap = "{ \"status\" : \"success\", \"name\" : \"Lucas\" }";
 			response.getWriter().write(serializedRespMap);
 			response.flushBuffer();
 		} else {
 			response.setContentType("application/json");
 			String serializedRespMap = "{ \"status\" : \"failed\" }";
 			response.getWriter().write(serializedRespMap);
 			response.flushBuffer();
 		}
 
 		return null;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 }

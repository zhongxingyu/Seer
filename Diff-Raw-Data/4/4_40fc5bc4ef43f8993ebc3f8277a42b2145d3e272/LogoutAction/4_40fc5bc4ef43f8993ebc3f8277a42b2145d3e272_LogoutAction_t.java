 package com.mcafee.bapp.action;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import com.mcafee.bapp.action.common.ActionConstants;
 
 public class LogoutAction implements Controller{
 
 	@Override
 	public ModelAndView handleRequest(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		HttpSession session = request.getSession(false);
		if(session!=null){
			session.invalidate();
		}
 		return new ModelAndView(ActionConstants.REDIRECT+ActionConstants.INDEX_JSP);
 	}
 
 }

 package edu.helsinki.sulka.interceptors;
 
 import org.slf4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.servlet.HandlerInterceptor;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.helsinki.sulka.models.User;
 
 public class AuthenticationInterceptor implements HandlerInterceptor
 
 {
 	private User user;
 	private Long timeIncrementInMinutes = 10L;
 
 	@Autowired
 	private Logger logger;
 
 	@Override
 	public boolean preHandle(HttpServletRequest request,
 			HttpServletResponse response, Object arg2) throws Exception {
 
 		logger.info("Pre-handle");
 
 		HttpSession session = request.getSession(false);
 
		if (session == null) {
			return false;
		}
		
 		user = (User) session.getAttribute("user");
		if (user != null && user.accessStatus() == 0) {
 			increaseSessionExpirationTimeInMinutes(timeIncrementInMinutes);
 			return true;
 		}
 		
 		session.removeAttribute("user");
 
 		return false;
 	}
 
 	@Override
 	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
 			Object arg2, ModelAndView arg3) throws Exception {
 
 		logger.info("Post-handle");
 
 	}
 
 	@Override
 	public void afterCompletion(HttpServletRequest arg0,
 			HttpServletResponse arg1, Object arg2, Exception arg3)
 			throws Exception {
 		logger.info("After-completion-handle");
 	}
 
 	private void increaseSessionExpirationTimeInMinutes(Long minutes) {
 		user.setExpires_at(System.currentTimeMillis() / 1000 + minutes * 60);
 	}
 
 }

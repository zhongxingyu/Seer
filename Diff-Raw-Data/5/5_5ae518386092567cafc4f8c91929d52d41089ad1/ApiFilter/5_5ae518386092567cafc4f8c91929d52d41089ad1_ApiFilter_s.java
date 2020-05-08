 package com.pms.service.controller.interceptor;
 
 import java.io.IOException;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.springframework.web.util.NestedServletException;
 
 import com.pms.service.annotation.InitBean;
 import com.pms.service.controller.AbstractController;
 import com.pms.service.exception.ApiLoginException;
 import com.pms.service.exception.ApiResponseException;
 import com.pms.service.mockbean.UserBean;
 import com.pms.service.service.IUserService;
 import com.pms.service.util.ApiThreadLocal;
 
 public class ApiFilter extends AbstractController implements Filter {
 
     private static IUserService userService;
 
     @Override
     public void destroy() {
 
     }
 
     private static Logger logger = LogManager.getLogger(ApiFilter.class);
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
         HttpServletRequest srequest = (HttpServletRequest) request;
         if (srequest.getSession().getAttribute(UserBean.USER_ID) != null) {
             ApiThreadLocal.set(UserBean.USER_ID, srequest.getSession().getAttribute(UserBean.USER_ID));
         }
         if (srequest.getSession().getAttribute(UserBean.USER_NAME) != null) {
             ApiThreadLocal.set(UserBean.USER_NAME, srequest.getSession().getAttribute(UserBean.USER_NAME));
         }
         
         try {
             loginCheck((HttpServletRequest) request);
             roleCheck((HttpServletRequest) request);
             filterChain.doFilter(request, response);
         } catch (Exception e) {
 
             if (e instanceof NestedServletException) {
                 Throwable t = e.getCause();
 
                 if (t instanceof ApiResponseException) {
                     // do nothing
                     responseServerError(t, (HttpServletRequest) request, (HttpServletResponse) response);
                 } else {
                    logger.fatal("Fatal error when user try to call API ", e);
                     responseServerError(e, (HttpServletRequest) request, (HttpServletResponse) response);
 
                 }
             } else if (e instanceof ApiLoginException) {
                 forceLogin((HttpServletRequest) request, (HttpServletResponse) response);
             } else {
                logger.fatal("Fatal error when user try to call API ", e);
                 responseServerError(e, (HttpServletRequest) request, (HttpServletResponse) response);
             }
 
         }
         ApiThreadLocal.removeAll();
 
     }
 
     @Override
     public void init(FilterConfig arg0) throws ServletException {
 
     }
 
     public static void initDao(IUserService userService) {
         ApiFilter.userService = userService;
     }
 
     private void roleCheck(HttpServletRequest request) {
         String uerId = null;
 
         if (request.getSession().getAttribute(UserBean.USER_ID) != null) {
             uerId = request.getSession().getAttribute(UserBean.USER_ID).toString();
         }
         if (InitBean.rolesValidationMap.get(request.getPathInfo()) != null) {             
             userService.checkUserRole(uerId, request.getPathInfo());
         }
 
     }
 
     private void loginCheck(HttpServletRequest request) {
         
         if(request.getPathInfo().indexOf("login") == -1){
 //        if (InitBean.loginPath.contains(request.getPathInfo())) {
             if (request.getSession().getAttribute(UserBean.USER_ID) == null) {
                 logger.debug("Login requried for path : " + request.getPathInfo());
                 throw new ApiLoginException();
             }
 //        }
         }
 
     }
 }

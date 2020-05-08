 package org.realty;
 
 import org.realty.commands.CommandFactory;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class ServletFilter implements Filter {
 
     private FilterConfig config = null;
 
     public void destroy() {
         config = null;
 
     }
 
     public void init(FilterConfig config) throws ServletException {
         this.config = config;
 
     }
 
     public void doFilter(ServletRequest request, ServletResponse response,
                          FilterChain chain) throws IOException, ServletException {
 
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;
         HttpSession session = httpRequest.getSession();
 
         UsrInfo user = (UsrInfo) session.getAttribute("userInfo");
         if (user == null)
             user = new UsrInfo();
         Roles currentUser = null;
         List<Roles> roles = null;
         String command = null;
 
         if (user.IsLogin()) {
             currentUser = Roles.LOGGED;
             if (user.IsAdmin())
                 currentUser = Roles.ADMIN;
         } else
             currentUser = Roles.ANONYMOUS;
 
 
         if (httpRequest.getQueryString() != null) {
             command = request.getParameter("command");
             roles = CommandFactory.getRolesByCommand(command);
         } else {
             command = httpRequest.getRequestURI().replace(httpRequest.getContextPath() + "/", "");
             roles = CommandFactory.getRolesByResourcePath(command);
         }
 
 
         //  List<Roles> roles = CommandFactory.getRoles(httpRequest.getRequestURI()+"?"+httpRequest.getQueryString());
 
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
         if ((roles.contains(currentUser))) {
             chain.doFilter(request, response);
         } else {
             httpResponse.sendRedirect("Authentication.jsp");
         }
     }
 }

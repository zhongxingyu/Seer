 package app.filters;
 
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
 
 import app.beans.Modulo;
 import app.beans.Perfil;
 import app.services.SecurityService;
 
 public class ValidateSessionFilter implements Filter {
 
 	public void destroy() {
 
 	}
 
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest req = (HttpServletRequest) request;
 		HttpServletResponse res = (HttpServletResponse) response;
 		HttpSession session = req.getSession(false);
 		if (session != null && session.getAttribute("user") != null) {
 			boolean pass = false;
			List<Modulo> mods = new SecurityService().
				getModules((Perfil) session.getAttribute("perfil"));
 			for (Modulo m : mods) {
				if (req.getRequestURI().substring(10, req.getRequestURI().length()).
						equals(m.getUri())) {
 					pass = true;
 					break;
 				}
 			}
 			if (pass)
 				chain.doFilter(request, response);
 			else
 				res.sendRedirect("/proyecto");
 		} else {
 			res.sendRedirect("/proyecto");
 		}
 	}
 
 	public void init(FilterConfig fConfig) throws ServletException {
 
 	}
 
 }

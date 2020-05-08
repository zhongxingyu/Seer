 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.filter;
 
 import java.io.IOException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 import lombok.val;
 import tk.c4se.halt.ih31.nimunimu.dto.Member;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.repository.MemberRepository;
 import tk.c4se.halt.ih31.nimunimu.repository.SessionRepository;
 
 /**
  * @author ne_Sachirou
  */
 public class AccessControlFilter implements java.io.Serializable,
 		javax.servlet.Filter {
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public void destroy() {
 	}
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse resp,
 			FilterChain chain) throws IOException, ServletException {
 		val loginAccountId = new SessionRepository((HttpServletRequest) req)
 				.getLoginAccountId();
 		Member member = null;
 		if (loginAccountId != null) {
 			try {
 				member = new MemberRepository().find(loginAccountId);
 			} catch (DBAccessException e) {
 				e.printStackTrace();
 			}
 		}
		req.setAttribute("loginMember", member);
 		chain.doFilter(req, resp);
 	}
 
 	@Override
 	public void init(FilterConfig config) throws ServletException {
 	}
 }

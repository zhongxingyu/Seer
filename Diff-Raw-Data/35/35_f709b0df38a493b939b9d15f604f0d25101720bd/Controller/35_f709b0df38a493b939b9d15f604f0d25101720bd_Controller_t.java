 /**
  *
  */
 package tk.c4se.halt.ih31.nimunimu.controller;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import lombok.val;
 import tk.c4se.halt.ih31.nimunimu.dto.Member;
 import tk.c4se.halt.ih31.nimunimu.dto.MemberAuthority;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.model.DoPostModel;
 import tk.c4se.halt.ih31.nimunimu.repository.SessionRepository;
 
 /**
  * @author ne_Sachirou
  */
 public abstract class Controller extends HttpServlet {
 	private static final long serialVersionUID = -8093435378978911657L;
 
 	/**
 	 * 
 	 */
 	protected List<MemberAuthority> authorities = new ArrayList<>();
 
 	/**
 	 * 
 	 */
 	protected String title = "";
 
 	/**
 	 * 
 	 */
 	protected String partial;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		if (!checkAuthorized(req, resp)) {
 			return;
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
		if (!checkAuthorized(req, resp) /* || !checkCsrf(req, resp) */) {
 			return;
 		}
 	}
 
 	/**
 	 * 
 	 * @param req
 	 * @param resp
 	 * @param title
 	 *            Page title.
 	 * @param partial
 	 *            Partial JSP path.
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void forward(HttpServletRequest req, HttpServletResponse resp,
 			String title, String partial) throws ServletException, IOException {
 		if (title == null) {
 			title = "nimunimu";
 		}
 		val session = new SessionRepository(req);
 		req.setAttribute("csrfToken", session.getCsrfToken());
 		req.setAttribute("baseUri", req.getSession().getServletContext()
 				.getContextPath());
 		req.setAttribute("title", title);
 		req.setAttribute("partial", "/view/partial" + partial);
 		req.getRequestDispatcher("/view/layout/layout.jsp").forward(req, resp);
 	}
 
 	/**
 	 * 
 	 * @param req
 	 * @param resp
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void forward(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		forward(req, resp, title, partial);
 	}
 
 	/**
 	 * Check the current Member is authorized to access the page, and auto
 	 * redirect to login page.
 	 * 
 	 * @param req
 	 * @param resp
 	 * @return
 	 * @throws IOException
 	 */
 	protected boolean checkAuthorized(HttpServletRequest req,
 			HttpServletResponse resp) throws IOException {
 		val isAuth = isAuthorized(req);
 		if (!isAuth) {
 			val url = URLEncoder.encode(req.getRequestURI(), "utf-8");
 			resp.sendRedirect("/nimunimu/login?redirect=" + url);
 		}
 		return isAuth;
 	}
 
 	/**
 	 * Check CSRF protection ,and auto return an error when the token is
 	 * invalid.
 	 * 
 	 * @param req
 	 * @param resp
 	 * @return
 	 * @throws IOException
 	 */
 	protected boolean checkCsrf(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		val storedToken = new SessionRepository(req).getCsrfToken();
 		val receivedToken = req.getParameter("csrf");
 		if (storedToken.equals(receivedToken)) {
 			return true;
 		} else {
 			resp.sendError(403, "Invalid CSRF token.");
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 * @param req
 	 * @param resp
 	 * @param model
 	 * @throws IOException
 	 */
 	protected void processDoPostRequest(HttpServletRequest req,
 			HttpServletResponse resp, DoPostModel model) throws IOException {
 		val requestType = req.getParameter("requestType");
 		try {
 			if (requestType.equals("POST")) {
 				model.postRequest(req, resp);
 			} else if (requestType.equals("PUT")) {
 				model.putRequest(req, resp);
 			} else if (requestType.equals("DELETE")) {
 				model.deleteRequest(req, resp);
 			} else {
 				throw new IOException("Unknown request type: " + requestType);
 			}
 		} catch (DBAccessException e) {
 			e.printStackTrace();
 			throw new IOException(e);
 		}
 	}
 
 	private boolean isAuthorized(HttpServletRequest req) {
		val loginMember = (Member) req.getAttribute("loginMember");
 		if (authorities.size() == 0) {
 			return true;
 		}
		if (loginMember == null) {
 			return false;
 		}
 		for (val auth : authorities) {
			if (auth.equals(loginMember.getAuthority())) {
 				return true;
 			}
 		}
 		return false;
 	}
 }

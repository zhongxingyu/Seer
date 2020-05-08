 package servlet;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import terraform.common.Constants.CookieName;
 import terraform.common.Constants.RequestAttrName;
 import terraform.common.FBApiHelper;
 import terraform.common.TokenStore;
 import terraform.common.URLHelper;
 import terraform.common.fbtypes.UserLike;
 import terraform.core.digester.FoodDigester;
 
 @WebServlet(name = "MainServlet", urlPatterns = { "/main" })
 public class MainServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
 		String id = null;
         String city = null;
 
 		Cookie[] cookies = req.getCookies();
 		for (Cookie cookie : cookies) {
 			if (cookie.getName().equalsIgnoreCase(CookieName.ID)) {
 				id = cookie.getValue();
 			}
             else if(cookie.getName().equalsIgnoreCase(CookieName.CITY)){
                 city = cookie.getValue();
             }
 		}
 
 		if (id != null) {
 			String token = TokenStore.getToken(id);
 			if (token != null) {
 				List<UserLike> userLikes = FBApiHelper.getUserLikes(token);
 
 				// TODO: What if token is expired?
 				List<String> searchTerms = FoodDigester.getInstance().getSearchTerms(userLikes);
 				req.setAttribute(RequestAttrName.SEARCH_TERM, searchTerms); 
 				req.setAttribute(RequestAttrName.LOCATION, city);
 				req.getRequestDispatcher("/suggestions").forward(req, resp);
 				return;
 			} else {
 				Cookie cookie = new Cookie(CookieName.ID, "");
 				cookie.setMaxAge(0);
 				resp.addCookie(cookie);
 
 				// resp.sendRedirect("/");
 				resp.sendRedirect(URLHelper.getRedirectURL(req, "/"));
 				return;
 			}
 		}
 		
 		resp.sendRedirect(URLHelper.getRedirectURL(req, "/"));
 	}
 }

 package web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.BSUser;
 import model.BSUser.PRIVS;
 
 import common.BSUtil;
 import common.HibernateUtil;
 
 
 /**
  * Displays the login form
  * @author stevearc
  *
  */
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = 4347939279807133754L;
 	public static final String NAME = "/login.html";
 	private void warn(PrintWriter out, String message) {
 		out.println("<span class='ui-state-error' style='padding:10px'>" + message + "</span>");
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		BSUser user = WebUtil.getUserFromCookie(request, response);
 		if (user != null) {
 			request.getSession(true).setAttribute("user", user);
 			response.sendRedirect(IndexServlet.NAME);
 			return;
 		}
 		response.setContentType("text/html");
 		response.setStatus(HttpServletResponse.SC_OK);
 		PrintWriter out = response.getWriter();
 		String name_param = request.getParameter("username");
 		name_param = (name_param == null ? "" : name_param);
 		out.println("<html><head>");
 		out.println("<title>Battlecode Tester</title>");
 		out.println("</head>");
 		out.println("<body>");	
 
 		WebUtil.writeTabs(request, response, NAME);
 
 		out.println("<div class='center' style='width:400px'>");	
 		out.println("<form id='login' method='post' style='width:200px; margin: 10px auto'>" +
 				"<table>" +
 				"<tr>" +
 				"<td style='text-align:right'>Username:</td>" +
 				"<td><input type='text' name='username' id='username' value='" + name_param + "' size='15' /></td>" +
 				"</tr>" +
 				"<tr>" + 
 				"<td style='text-align:right'>Password:</td>" +
 				"<td><input type='password' name='password' id='password' size='15' /></td>" +
 				"</tr>" +
 				"<tr>" +
 				"<td style='text-align:right'>" +
 				"<input type='submit' value='Login' name='login' />" +
 				"</td>" +
 				"<td><input type='submit' value='Register' name='register' /></td>" +
 				"</tr>" +
 				"</table>" + 
 				"</form>"
 		);
 
 		String error = (String) request.getAttribute("error");
 		if ("no_username".equals(error)) {
 			warn(out, "Must enter a username");
 		} else if ("no_password".equals(error)) {
 			warn(out, "Must enter a password");
 		} else if ("bad_auth".equals(error)) {
 			warn(out, "Bad username or password");
 		} else if ("name_taken".equals(error)) {
 			warn(out, "Username already taken");
 		} else if ("name_length".equals(error)) {
 			warn(out, "Username is too long");
 		} else if ("pending_user".equals(error)) {
 			warn(out, "You must wait for an Admin to approve your account");
 		} else if ("bad_char".equals(error)) {
 			String msg = "Username or password contains illegal characters <br/>(";
 			for (char c: WebUtil.BLACKLIST)
 				msg += c;
 			msg += ")";
 			out.println("<div class='ui-state-error' style='padding:10px'>" + msg + "</div>");
 		}
 
 		if ("success".equals(request.getAttribute("register"))) {
 			out.println("<span class='ui-state-highlight' style='padding:10px'>Registration successful!  Wait for admin to confirm your credentials</span>");
 		}
 		out.println("<script type='text/javascript'>\n" +
 				"$('#seed').attr('value',Math.random());\n" +
 				"$('#username').focus();\n" +
 		"</script>");
 
 		out.println("</div>");	
 		out.println("</body></html>");
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String username = request.getParameter("username");
 		username = (username == null ? null : username.trim());
 		String password = request.getParameter("password");
 		if (username == null || "".equals(username)) {
 			request.setAttribute("error", "no_username");
 			doGet(request, response);
 			return;
 		}
 		if (username.length() > 20) {
 			request.setAttribute("error", "name_length");
 			doGet(request, response);
 			return;
 		}
 		if (WebUtil.containsBadChar(username) || WebUtil.containsBadChar(password)) {
 			request.setAttribute("error", "bad_char");
 			doGet(request, response);
 			return;
 		}
 		if (password == null || "".equals(password.trim())) {
 			request.setAttribute("error", "no_password");
 			doGet(request, response);
 			return;
 		}
 		String seed = request.getParameter("seed");
 		seed += BSUtil.SHA1(""+Math.random());
 		String salt = BSUtil.SHA1(seed);
 		EntityManager em = HibernateUtil.getEntityManager();
 		if (request.getParameter("register") != null) {
 			String hashed_password = BSUtil.SHA1(password + salt);
 			BSUser user = new BSUser();
 			user.setUsername(username);
 			user.setHashedPassword(hashed_password);
 			user.setSalt(salt);
 			user.setPrivs(BSUser.PRIVS.PENDING);
 
 			em.getTransaction().begin();
 			try {
 				em.persist(user);
 				em.flush();
 			} catch (PersistenceException e) {
 				// Username already exists
 				request.setAttribute("error", "name_taken");
 			}
 			if (em.getTransaction().getRollbackOnly()) {
 				em.getTransaction().rollback();
 			} else {
 				em.getTransaction().commit();
 			}
 			em.close();
 			doGet(request, response);
 			return;
 		}
 		BSUser user = getMatchingUser(username, password);
		if (user == null || user.getPrivs() == PRIVS.PENDING) {
 			request.setAttribute("error", "pending_user");
 			em.close();
 			doGet(request, response);
 			return;
		} else if (request.getParameter("login") != null) {
 			user.setSession(salt);
 			em.getTransaction().begin();
 			em.merge(user);
 			em.flush();
 			em.getTransaction().commit();
 			em.close();
 			// Cookie is encoded as [userid]$[session token]
 			Cookie c = new Cookie(WebUtil.COOKIE_NAME, user.getId() + "$" + salt);
 			response.addCookie(c);
 			response.setStatus(HttpServletResponse.SC_OK);
 			response.sendRedirect(IndexServlet.NAME);
 			response.setContentType("text/html");
 			return;
 		}
 		em.close();
 		request.setAttribute("error", "bad_auth");
 		doGet(request, response);
 	}
 
 	private BSUser getMatchingUser(String username, String password) {
 		EntityManager em = HibernateUtil.getEntityManager();
 
 		try {
 			BSUser user = (BSUser) em.createQuery("from BSUser user where user.username = ?")
 			.setParameter(1, username)
 			.getSingleResult();
 			if (BSUtil.SHA1(password + user.getSalt()).equals(user.getHashedPassword())) {
 				return user;
 			}
 		} catch (NoResultException e) {
 		}
 		return null;
 	}
 
 }

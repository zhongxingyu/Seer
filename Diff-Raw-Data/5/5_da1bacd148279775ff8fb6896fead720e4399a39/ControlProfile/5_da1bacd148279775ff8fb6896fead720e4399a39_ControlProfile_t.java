 package controller;
 
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import daoimpl.MySQLLangDAO;
 import daoimpl.MySQLPublisherDAO;
 import daoimpl.MySQLRoleDAO;
 import daoimpl.MySQLUserPubDAO;
 import daoimpl.MySQLUsersLangDAO;
 import daointerfaces.DALException;
 import daointerfaces.LangIDAO;
 import daointerfaces.PublisherIDAO;
 import daointerfaces.RoleIDAO;
 import daointerfaces.UserPubIDAO;
 import daointerfaces.UsersLangIDAO;
 import dto.LangDTO;
 import dto.PublisherDTO;
 import dto.RoleDTO;
 import dto.UserPubDTO;
 import dto.UsersDTO;
 import dto.UsersLangDTO;
 import funktionalitet.*;
 
 
 
 public class ControlProfile extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private IUserLogic userLogic = null;
 	private UsersDTO user = null;
 	private LangIDAO lang = null;
 	private PublisherIDAO pub = null;
 	private UserPubIDAO userPub = null;
 	private UsersLangIDAO userLang = null;
 	private RoleIDAO userRole = null;
 
 	public ControlProfile() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		ServletContext application = request.getSession().getServletContext();
 		HttpSession session = request.getSession();
 
 		// Create logic if not already created.
 		userLogic = (UserLogic) application.getAttribute("userLogic");
 		if (userLogic == null) {
 			try {
 				userLogic = new UserLogic();
 				application.setAttribute("userLogic", userLogic);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 		lang = (LangIDAO) application.getAttribute("language");
 		if (lang == null) {
 			lang = new MySQLLangDAO();
 			application.setAttribute("language", lang);
 		}
 		userLang = (UsersLangIDAO) application.getAttribute("userlanguage");
 		if (userLang == null) {
 			userLang = new MySQLUsersLangDAO();
 			application.setAttribute("userlanguage", userLang);
 		}
 		pub = (PublisherIDAO) application.getAttribute("publisher");
 		if (pub == null) {
 			pub = new MySQLPublisherDAO();
 			application.setAttribute("publisher", pub);
 		}
 		userRole = (RoleIDAO) application.getAttribute("role");
 		if (userRole == null) {
 			userRole = new MySQLRoleDAO();
 			application.setAttribute("role", userRole);
 		}
 		userPub = (UserPubIDAO) application.getAttribute("userPub");
 		if (userPub == null) {
 			userPub = new MySQLUserPubDAO();
 			application.setAttribute("userPub", userPub);
 		}
 
 		// Create user bean if not already existing.
 		user = (UsersDTO) session.getAttribute("user");
 		if (user == null) {
 			try {
 				String email = request.getUserPrincipal().getName();
 				user = userLogic.getUser(email);
 				session.setAttribute("user", user);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 
 		// Getting the action parameter.
 		String action = null;
 		action = request.getParameter("action");
 
 		// Prepare update pages. Gets information about the user
 		if ("updateUser".equals(action)) {
 			String userEmail = user.getEmail();
 			try {
 				RoleDTO role = userRole.get(userEmail);
 				UsersDTO user1 = userLogic.getUser(userEmail);
 				UsersLangDTO userLangRow = userLang.get(userEmail);
 				List<LangDTO> langu = lang.getList();
 				request.setAttribute("role", role);
 				request.setAttribute("userLang", userLangRow);
 				request.setAttribute("langList", langu);
 				request.setAttribute("user1", user1);
 				if (role.getRole().equalsIgnoreCase("user") || role.getRole().equalsIgnoreCase("administrator") || role.getRole().equalsIgnoreCase("inactive")) {
 					request.getRequestDispatcher("../WEB-INF/profile/updateUser.jsp?").forward(request, response);
 				} else {
 					UserPubDTO comp = userPub.get(userEmail);
 					List<PublisherDTO> pubList = pub.getList();
 					request.setAttribute("userPub", comp);
 					request.setAttribute("pubList", pubList);
 					request.getRequestDispatcher("../WEB-INF/profile/updatePublisher.jsp?").forward(request, response);
 				}
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("../WEB-INF/profile/index.jsp?").forward(request, response);
 			}
 		}
 		// User updates profile
 		else if ("updateOprFilled".equals(action)) {
 			String userEmail = user.getEmail();
 			try {
 				String fName = request.getParameter("newFName");
 				String lName = request.getParameter("newLName");
 				String newEmail = request.getParameter("newUserEmail");
 				String birth = request.getParameter("newUserBirth");
 				String sex = request.getParameter("newUserSex");
 				String lang = request.getParameter("newUserLang");
 				String oldPass = request.getParameter("oldPass");
 				String pass1 = request.getParameter("updOprPass1");
 				String pass2 = request.getParameter("updOprPass2");
 				int iSex = Integer.parseInt(sex);
 				int iLang = Integer.parseInt(lang);	
 				userLogic.updateOpr(fName, lName, birth, userEmail, newEmail, iSex, iLang, oldPass, pass1, pass2);
				user = userLogic.getUser(newEmail);
				session.setAttribute("user", user);
 				request.setAttribute("message", "User with email: " + newEmail + " successfully updated.");
 				request.getRequestDispatcher("../WEB-INF/profile/index.jsp?").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("index.jsp?action=updateUser").forward(request, response);
 			}
 		}
 		// Userpub updates profile
 		else if ("updatePubFilled".equals(action)) {
 			String userEmail = user.getEmail();
 			try {
 				String fName = request.getParameter("newFName");
 				String lName = request.getParameter("newLName");
 				String newEmail = request.getParameter("newUserEmail");
 				String birth = request.getParameter("newUserBirth");
 				String sex = request.getParameter("newUserSex");
 				String lang = request.getParameter("newUserLang");
 				String oldPass = request.getParameter("oldPass");
 				String pass1 = request.getParameter("updOprPass1");
 				String pass2 = request.getParameter("updOprPass2");
 				String newPub = request.getParameter("newPub");
 				int Pid = Integer.parseInt(newPub);
 				int iSex = Integer.parseInt(sex);
 				int iLang = Integer.parseInt(lang);	
 				userLogic.updatePub(fName, lName, birth, userEmail, newEmail, iSex, iLang, oldPass, pass1, pass2, Pid);
				user = userLogic.getUser(newEmail);
				session.setAttribute("user", user);
 				request.setAttribute("message", "User with email: " + newEmail + " successfully updated.");
 				request.getRequestDispatcher("../WEB-INF/profile/index.jsp?").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("index.jsp?action=updateUser").forward(request, response);
 			}
 		}
 		// User deactivates profile
 		else if ("deactivateUser".equals(action)) {
 			String userEmail = user.getEmail();
 			try {
 				userLogic.deactivateUser(userEmail);
 				session.invalidate();
 				request.setAttribute("action", null);
 				request.logout();
 				response.sendRedirect("../index.jsp");
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("../WEB-INF/profile/index.jsp?").forward(request, response);
 			}
 		} else { 
 			request.getRequestDispatcher("../WEB-INF/profile/index.jsp?").forward(request, response);
 		}
 	}
 }

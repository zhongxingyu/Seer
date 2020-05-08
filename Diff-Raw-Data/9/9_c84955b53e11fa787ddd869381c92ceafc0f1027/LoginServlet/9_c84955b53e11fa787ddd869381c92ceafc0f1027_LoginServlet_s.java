 package webview.servlet.login;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import webview.util.AlertsUtility;
 import webview.util.WebUtility;
 import business.EJB.user.AuthBean;
 import business.EJB.user.UserBean;
 import business.EJB.util.EJBUtility;
 import business.exceptions.login.UnreachableDataBaseException;
 import business.exceptions.login.UserNotFoundException;
 
 /**
  * Servlet implementation class LoginServlet
  */
 @WebServlet("/doLogin")
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public LoginServlet() {
         super();
     }
     
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String user = request.getParameter("login");
 		String senha = request.getParameter("senha");
 		try {
 			UserBean login_result = AuthBean.validarLogin(user, senha, AuthBean.NonHashedPwd);
			if(login_result != null && (login_result.getLogType().equals(AuthBean.LoginSuccessAdmin) || login_result.getLogType().equals(AuthBean.LoginSuccessUserLevel2)))
 			{
 				Cookie c_email = new Cookie(WebUtility.cookie_email, user);
 				Cookie c_pass = new Cookie(
 						WebUtility.cookie_session, 
 						EJBUtility.getHash(login_result.getUsername() + login_result.getEmail() + login_result.getLogType(), 
 								"SHA-256")
 				);
 				Cookie c_status = new Cookie(WebUtility.cookie_status, login_result.getLogType().toString());
 				Cookie c_nome = new Cookie(WebUtility.cookie_nome, WebUtility.removeAccents(login_result.getUsername()));
 				
 				c_email.setMaxAge(WebUtility.cookie_expire);
 				c_pass.setMaxAge(WebUtility.cookie_expire);
 				c_status.setMaxAge(-1);
 				c_nome.setMaxAge(WebUtility.cookie_expire);
 				
 				response.addCookie(c_email);
 				response.addCookie(c_pass);
 				response.addCookie(c_status);
 				response.addCookie(c_nome);
 
			    if(login_result.getLogType() == AuthBean.LoginSuccessUserLevel2)	// retorna para a página de USER
 			    	response.sendRedirect("/GraoPara/protected/user/index.jsp");
 			    else if(login_result.getLogType() == AuthBean.LoginSuccessAdmin)	// retorna para a página de ADMIN
 			    	response.sendRedirect("/GraoPara/protected/admin/index.jsp");
 			}
 			else if(login_result != null && (login_result.getLogType().equals(AuthBean.LoginFailOrDefault)))
 			{
 				AlertsUtility.alertAndRedirectPage(response, 
 						"Sua conta ainda não foi aprovada pelo administrador, por favor aguarde e tente novamente mais tarde.", 
 						"/GraoPara/public/index.jsp");
 				}
 			else
 			{
 				//Exibir alerta e jogar de volta para o index, provisório
 				AlertsUtility.alertAndRedirectPage(response, 
 						"Login incorreto! Verifique seu email e senha, e tente de novo.", 
 						"/GraoPara/public/index.jsp");
 			}
 
 		} catch (UnreachableDataBaseException e) {
 			e.printStackTrace();
 		} catch (UserNotFoundException e) {
 			AlertsUtility.alertAndRedirectPage(response, 
 					"Login incorreto! Verifique seu email e senha, e tente de novo.", 
 					"/GraoPara/public/index.jsp");
 			}
 		}
 	}

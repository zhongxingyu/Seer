 package br.com.fiap.si.tp2.trabalho.servlet;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import br.com.fiap.si.tp2.trabalho.dao.LoginDAO;
 
@WebServlet("/Login")
public class Login extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     
    public Login() {
         super();
     }
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		System.out.println("Cheguei no Servlet login!!!!!!");
 		
 		response.setContentType("text/html");
 		
 		String emailLogin = request.getParameter("email");
 		String senhaLogin = request.getParameter("senha");
 		
 		String login = "";
 		
 		HttpSession session = request.getSession(true);
 		
 		LoginDAO loginDao = new LoginDAO();
 		
 		String pagina = "";
 		
 		try {
 			if(loginDao.validaLogin(emailLogin, senhaLogin)) {
 				login = "logado";
 				session.setAttribute("sessionLogin", login);
 				pagina = "Index.jsp";
 				
 			}else{
 				login = "naoLogado";
 				session.setAttribute("sessionLogin", login);
 				pagina = "Login.jsp";
 				
 			}
 			
 			RequestDispatcher dispatcher = request.getRequestDispatcher(pagina);
 			dispatcher.forward(request, response);
 			
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		}
 				
 	}
 
 }

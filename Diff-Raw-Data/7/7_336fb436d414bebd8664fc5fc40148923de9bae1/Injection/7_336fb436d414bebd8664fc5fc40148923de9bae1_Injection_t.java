 package se.magos.servlet;
 
 import java.io.IOException;
 
import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import se.magos.domain.Account;
 import se.magos.service.AccountService;
 
 public class Injection extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
	@EJB
	private AccountService accountService;
	
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		response.getWriter()
 				.write("<html><body><h1>JBoss AS7 - EclipseLink</h1><h2>Injection Case</h2></body></html>");
 
 		Account account = new Account();
 		account.setAccountName("Test Account");
 		
 		accountService.create(account);
 	}
 }

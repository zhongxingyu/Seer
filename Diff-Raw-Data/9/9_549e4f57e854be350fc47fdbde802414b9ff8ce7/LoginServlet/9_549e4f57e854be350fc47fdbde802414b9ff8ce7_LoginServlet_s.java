 package userAction;
 
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import system.MyWeddingManager;
 import system.User;
 import system.UserAction;
 
 
 /**
  * Servlet implementation class LoginServlet
  */
 @WebServlet("/Login")
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	MyWeddingManager mw = MyWeddingManager.getInstance();
 
     public LoginServlet() {
         super();
         // TODO Auto-generated constructor stub
         System.out.println("------ LoginServlet");
         
     }
 
     public static final String USER_EMAIL_PARAM = "email";
     public static final String USER_PASSWORD_PARAM = "password";
     public static final String ERROR_MWSSAGE = "errorMessage";
     public static final String USER_PARAM = "user";
     public static final String USER_NAME_PARAM = "userName";
     public static final String USER1_NAME_PARAM = "שםמשתש";
     public static final String USER_ID_NAME_PARAM = "user_id";
    
     public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException , IOException{
     
     	String email = (String)request.getParameter(USER_EMAIL_PARAM);
     	String password = (String)request.getParameter(USER_PASSWORD_PARAM);
     	System.out.println( email  +   password );
     	
     	if ( password == null || password.trim().isEmpty() || email == null || email.trim().isEmpty() ||mw.login(email, password) == null ){
  
         		request.setAttribute(ERROR_MWSSAGE, "Invalid email/password combination, try again");
         
        		this.getServletConfig().getServletContext().getRequestDispatcher("/LogIng.jsp").forward(request, response);
         	return;	
         	}
     	
     	
     	User ur = new User(email);
     	UserAction uac = new UserAction(ur);
     	ur = uac.getUser(ur.getEmail());
     	User user = new User(ur.getId(), ur.getFirstName(), ur.getLastName(), ur.getPassword(), ur.getEmail());
 
     	
     	HttpSession session =  request.getSession(true);
     	if (!session.isNew()){
     		session.invalidate();
     		session = request.getSession(true);   		
     	}
     	
     	
     	session.setAttribute("welcaomeMsg" ,"Wellcome");
     	//session.setAttribute("welcaomeMsg" ,"ברוך-הבא");
     	session.setAttribute(USER_PARAM,user);
     	session.setAttribute(USER_NAME_PARAM ,user.getFirstName());
     	session.setAttribute(USER_ID_NAME_PARAM ,user.getId());
     	session.setAttribute(USER_EMAIL_PARAM ,user.getEmail());
 	
     	System.out.println(" from login ======> the user is " + user);
     	
 	
     	this.getServletConfig().getServletContext().getRequestDispatcher("/Dashboard.jsp").forward(request, response);
     	return;
     }
 	
 
 }

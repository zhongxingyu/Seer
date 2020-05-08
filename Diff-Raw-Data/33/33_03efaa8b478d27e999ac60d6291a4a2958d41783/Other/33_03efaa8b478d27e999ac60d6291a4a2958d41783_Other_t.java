 package web_interface;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import businessLogic_layer.Functionality;
 import dao_interfaces.DALException;
 
 public class Other {
 	
 
 
 	public void handleLogIn(HttpServletRequest request, ServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException{
		HttpSession session = request.getSession(true);
 		String txtID = request.getParameter("Id");
 		String pw = request.getParameter("Password");
 		try {
 			if (funktionalitetsLaget.testId(txtID)) { // checker om ID findes i DB.
 				int id = Integer.parseInt(txtID);
 				if(funktionalitetsLaget.testPassword(id, pw)){
 					session.setAttribute("operatoerNavn", funktionalitetsLaget.getOprDAO().getOperatoer(id).getOprNavn());
 					session.setAttribute("operatoerAktoer", funktionalitetsLaget.getOprDAO().getOperatoer(id).getAktoer());
 					session.setAttribute("operatoerID", funktionalitetsLaget.getOprDAO().getOperatoer(id).getOprId());
 					if(funktionalitetsLaget.getOprDAO().getOperatoer(id).getAktoer() == 1)
 						request.getRequestDispatcher("/WEB-INF/admin.jsp").forward(request, response);
 					else if(funktionalitetsLaget.getOprDAO().getOperatoer(id).getAktoer() == 2)
 						request.getRequestDispatcher("/WEB-INF/pharmacist.jsp").forward(request, response);
 					else if(funktionalitetsLaget.getOprDAO().getOperatoer(id).getAktoer() == 3)
 						request.getRequestDispatcher("/WEB-INF/supervisor.jsp").forward(request, response);
 					else
 						request.getRequestDispatcher("/WEB-INF/operator.jsp").forward(request, response);
 				}
 				else {
 					request.setAttribute("error", "BrugerID eller password findes ikke - prøv igen.");
 					request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
 				}
 			}
 			else {
 				request.setAttribute("error", "BrugerID eller password findes ikke - prøv igen.");
 				request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
 			}
 		} catch (DALException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void handleLogOff(ServletRequest request, ServletResponse response) throws ServletException, IOException{
 		request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
 	}
 	public void handleGoToHovedmenu(HttpServletRequest request, ServletResponse response) throws ServletException, IOException{
 		HttpSession session = request.getSession(true);
 		int aktoer = Integer.parseInt(String.valueOf(session.getAttribute("operatoerAktoer")));
 		if (aktoer == 1)
 			request.getRequestDispatcher("/WEB-INF/admin.jsp").forward(request, response);
 		else if (aktoer == 2)
 			request.getRequestDispatcher("/WEB-INF/pharmacist.jsp").forward(request, response);
 		else if (aktoer == 3)
 			request.getRequestDispatcher("/WEB-INF/supervisor.jsp").forward(request, response);
 		else
 			request.getRequestDispatcher("/WEB-INF/operator.jsp").forward(request, response);
 		
 	}
 
 	public void handleChangePwSubmit(HttpServletRequest request,
			HttpServletResponse response, Functionality funktionalitetsLaget) throws DALException, ServletException, IOException {
 		HttpSession session = request.getSession(true);
 		int id = Integer.parseInt(String.valueOf(session.getAttribute("operatoerID")));
 		if(funktionalitetsLaget.askForNewPassword(id, request.getParameter("password")))
			request.setAttribute("succes", "Password er ændret!");
 		else
 			request.setAttribute("fail", "Password ikke godkendt!");
 		
		request.getRequestDispatcher("/WEB-INF/admin/changepw.jsp").forward(request, response);
		
 	}
 	
 
 }

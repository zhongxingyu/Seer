 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets;
 
 import ejb.Student;
 import ejb.StudentFacadeLocal;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import pojo.md5;
 
 /**
  *
  * @author Josh
  */
 public class EditStudentServlet extends HttpServlet {
 
 	@EJB
 	private StudentFacadeLocal studentFacade;
 
 	/** 
 	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
 	 * @param request servlet request
 	 * @param response servlet response
 	 * @throws ServletException if a servlet-specific error occurs
 	 * @throws IOException if an I/O error occurs
 	 */
 	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
 					throws ServletException, IOException {
 
		String method, surname, firstname, username, password, phone, email;
 		Integer studentId = 0;
 		Short age;
 
 		try {
 			try {
 				studentId = Integer.parseInt(request.getParameter("student_id"));
 			} catch (Exception e) {
 			}
 
 			surname = request.getParameter("surname");
 			firstname = request.getParameter("firstname");
 			username = request.getParameter("username");
 			password = request.getParameter("password");
 			age = Short.parseShort(request.getParameter("age"));
 			phone = request.getParameter("phone");
 			email = request.getParameter("email");
 		} catch (Exception e) {
 			response.setContentType("text/html;charset=UTF-8");
 			PrintWriter out = response.getWriter();
 			out.println("ERROR: some form fields are missing or invalid.<br />");
 			out.println(e.getMessage());
 			out.close();
 			return;
 		}
 
 		Student student;
 		if (studentId > 0) {
			student = studentFacade.find(age);
 		} else {
 			student = new Student();
 		}
 
 		student.setLastname(surname);
 		student.setFirstname(firstname);
 		student.setUsername(username);
 		student.setPassword(md5.md5(password));
 		student.setAge(age);
 		student.setPhone(phone);
 		student.setEmail(email);
 
 		try {
 			if (studentId > 0) {
 				studentFacade.edit(student);
 			} else {
 				studentFacade.create(student);
 			}
 		} catch (Exception e) {
 			response.setContentType("text/html;charset=UTF-8");
 			PrintWriter out = response.getWriter();
 			out.println("ERROR: Could not create student in database.<br />");
 			out.println(e.getMessage());
 			out.close();
 			return;
 		}
 
 		response.sendRedirect("studentDetails.xhtml");
 	}
 
 	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
 	/** 
 	 * Handles the HTTP <code>GET</code> method.
 	 * @param request servlet request
 	 * @param response servlet response
 	 * @throws ServletException if a servlet-specific error occurs
 	 * @throws IOException if an I/O error occurs
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response)
 					throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	/** 
 	 * Handles the HTTP <code>POST</code> method.
 	 * @param request servlet request
 	 * @param response servlet response
 	 * @throws ServletException if a servlet-specific error occurs
 	 * @throws IOException if an I/O error occurs
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
 					throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	/** 
 	 * Returns a short description of the servlet.
 	 * @return a String containing servlet description
 	 */
 	@Override
 	public String getServletInfo() {
 		return "Short description";
 	}// </editor-fold>
 }

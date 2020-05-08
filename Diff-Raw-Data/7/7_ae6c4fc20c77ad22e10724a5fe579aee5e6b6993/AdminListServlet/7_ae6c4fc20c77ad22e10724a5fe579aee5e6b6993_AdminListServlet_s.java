 package cn.edu.sicau.rs.servlet;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.hibernate.HibernateException;
 
 import cn.edu.sicau.rs.model.Model;
 
 public class AdminListServlet extends HttpServlet{
 	public void doGet (HttpServletRequest request, HttpServletResponse response)
 				throws ServletException , IOException {
 		request.setCharacterEncoding("UTF-8");
 		Model model = new Model();
 		List adminList = null;
 		try {
 			adminList = model.getAllAdmin();
 			request.setAttribute("adminList", adminList); 
 			response.sendRedirect("adminList.jsp");
 		} catch (HibernateException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void doPost(HttpServletRequest request, HttpServletResponse response) 
 					throws ServletException , IOException {
		
 		this.doGet(request,response);
 	}
 
 }

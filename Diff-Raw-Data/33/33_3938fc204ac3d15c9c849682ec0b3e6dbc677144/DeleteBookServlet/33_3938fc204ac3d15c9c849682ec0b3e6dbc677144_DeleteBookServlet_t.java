 package com.shining.ibookclubserver.servlet;
 
 import java.io.IOException;
import java.io.PrintWriter;

 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import net.sf.json.JSONObject;

 
 import com.shining.ibookclubserver.dao.BookDao;
 
 /**
  * Servlet implementation class DeleteBookServlet
  */
 public class DeleteBookServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private String isbn;
 	private String email;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public DeleteBookServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		
 		request.setCharacterEncoding("UTF-8");
 		email=request.getParameter("email");
 		
 		isbn=new String(request.getParameter("isbn"));
 		
 		BookDao dao=BookDao.getInstance();
 		dao.deleteBook(email, isbn);
		
		JSONObject jsonObject=new JSONObject();
			
		jsonObject.put("Result","Success");
		
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		System.out.println("DeleteBook:"+jsonObject.toString());
		
		out.print(jsonObject.toString());
		out.flush();
		out.close();
 	}
 
 }

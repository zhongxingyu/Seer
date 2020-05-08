 package com.shining.ibookclubserver.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.*;
 
 
 
 
 /**
  * Servlet implementation class LoginServlet
  */
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public LoginServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	//	 doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		
 		
 		 String email1=new String(request.getParameter("email"));
 		  String password1=new String(request.getParameter("password"));
 		  
		 System.out.println("LoginServlet:"+email1+password1);
 		  try {
 			Class.forName("com.mysql.jdbc.Driver");
 		
 		 Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/iBookClubDB","root","123456");
 		 Statement stmt=con.createStatement();
 		 String sql="select * from  userinfo where email='"+email1+"';";
 		 ResultSet rs=stmt.executeQuery(sql);
 		  if(rs.next())
 		  {
 			  String password=new String(rs.getString("password"));
 			  if(password.equals(password1))
 			  {
 			//  session.setAttribute("userid1",userid1);
 			//  response.sendRedirect("success.jsp");
 				 
 			//	Gson gson = new Gson();  
 			//	JSONObject object =gson.toJson(user1);
 				
 				request.getSession(true).setAttribute("email" , email1);
 				
 				String nickname=new String(rs.getString("nickname"));
 				
 				JSONObject jsonObj = new JSONObject().put("email" , email1)
 													.put("nickname", nickname)
 													.put("ActionResult", true);
 				
 			//	String result="登陆成功！";
 				PrintWriter out = response.getWriter();
 				out.print(jsonObj.toString());
 				out.flush();
 				out.close();
 			  
 			  }
 			  else{
 			  
 			  }
 		  } 
 		 }
 		  catch (Exception e) {
 				
 				e.printStackTrace();
 		  }
 		  
 	}
 
 }

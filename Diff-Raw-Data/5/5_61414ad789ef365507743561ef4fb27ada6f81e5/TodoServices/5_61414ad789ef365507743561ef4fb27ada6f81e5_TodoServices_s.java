 /* a simple change */
 package com.contoso.services;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.Gson;
 
 /**
  * Servlet implementation class TodoServices
  */
 @WebServlet("/TodoServices")
 public class TodoServices extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public TodoServices() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Gson gson = new Gson();
                 TodoList todoList = new TodoList();
                 todoList.name = "Personal";
                 todoList.items = getTestItems();
 
 		String json = gson.toJson(todoList);  
 		
 		response.setContentType("application/json");
         response.setHeader("Access-Control-Allow-Origin", "*");
 		response.getWriter().write(json);
 		response.getWriter().flush();
 	}
 	
 	private TodoItem[] getTestItems() {
 		List items = new ArrayList();
 
 		items.add(new TodoItem("Get tickets for the game"));
 		items.add(new TodoItem("Mail package"));
 		items.add(new TodoItem("Pick up milk"));
 		items.add(new TodoItem("Mow the lawn"));
 		
 		return (TodoItem[])items.toArray(new TodoItem[items.size()]);
 	}
 
 	private static class TodoList {
 
 		public String name;
 		public TodoItem[] items;
 
 	}
 	
 	private static class TodoItem {
 		
 		public TodoItem(String name) {
 			this.id = ((int)Math.random() * 12300) + "";
			this.name2 = name;
 			this.createdAt = new Date();
 		}
 		
 		public String id;
		public String name2;
 		public Date createdAt;		
 				
 	}
 
 }
 
  

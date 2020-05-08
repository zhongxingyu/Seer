 /* Todo service */
 package com.contoso.services;
 
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.*;
 
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
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Gson gson = new Gson();
                 TodoList todoList = new TodoList();
                 todoList.name = "Personal";
                 todoList.color = "cyan";
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
		items.add(new TodoItem("Buy some bread"));
		items.add(new TodoItem("Pickup the kid!"));
 		
 		
 		return (TodoItem[])items.toArray(new TodoItem[items.size()]);
 	}
 
 	private static class TodoList {
 
 		public String name;
 		public String color;
         public TodoItem[] items;
         
 	}
 	
 	private static class TodoItem {
 		
 		public TodoItem(String name) {
 			this.id = ((int)Math.random() * 12300) + "";
 			this.name = name;
 			this.createdAt = new Date();
 		}
 		
 		public String id;
 		public String name;
 		public Date createdAt;		
 				
 	}
 
 }
 
  

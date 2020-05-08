 package Servlets;
 
 import helpers.HTMLHelper;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.FillInTheBlank;
 import model.Matching;
 import model.MultipleAnswer;
 import model.MultipleChoice;
 import model.MultipleChoiceMultipleAnswer;
 import model.PictureResponse;
 import model.QuestionResponse;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 
 import Accounts.Account;
 import Accounts.AccountManager;
 
 /**
  * Servlet implementation class QuizCatalogServlet
  */
 @WebServlet("/QuizCatalogServlet")
 public class QuizCatalogServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public QuizCatalogServlet() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Connection con = (Connection) request.getServletContext().getAttribute("connect");
 		AccountManager am = (AccountManager) request.getServletContext().getAttribute("accounts");
 		Statement stmt;
 		ResultSet rs;
 		try {
 			stmt = (Statement) con.createStatement();
 			String searchQuizes = "Select quiz_id, name, category, creator_id from quiz";
 			if (request.getParameter("search") != null && request.getParameter("search").length() > 0) {
 				String searchBy = request.getParameter("type");
 				String searchFor = request.getParameter("search");
 				if (searchBy.equals("creator_id")) {
 					Account author =  am.getAccount(searchFor);
 					if (author != null) searchFor = " = " + author.getId();
 					else searchFor = " = -1";
 				} else {
 					searchFor = " like \"%"+searchFor+"%\"";
 				}
 				searchQuizes += " where "+searchBy + searchFor;
 			}
 			System.out.println(searchQuizes);
 			rs = stmt.executeQuery(searchQuizes);
 			response.setContentType("text/html");
 	    	PrintWriter out = response.getWriter();
 	    	out.println("<head>");
 	    	out.println(HTMLHelper.printCSSLink());
 	    	out.println("</head");
 	    	out.println("<body>");
 	    	out.println(HTMLHelper.printHeader((Account)request.getSession().getAttribute("account")));
 	    	
 	    	
 			if(request.getSession().getAttribute("account") != null) out.println(HTMLHelper.printNewsFeed(am.getAnnouncements()));
 	    	
 	    	out.println(HTMLHelper.contentStart());
 	  
 	    	out.println("<form action=\"QuizCatalogServlet\" method=\"get\">");
 	    	out.println("Search Quizes: <input type=\"text\" name=\"search\"/>");
 	    	//out.println("<input type=\"hidden\" name=\"type\"/ value=\"name\">");
 	    	out.println("<select name = \"type\">");
 	    	out.println("<option value = \"name\">By quiz name</option>");
 	    	out.println("<option value = \"category\">By category</option>");
 	    	out.println("<option value = \"creator_id\">By author</option>");
 	    	out.println("</select>");
 	    	out.println("<input type=\"submit\" value=\"Search\"/>");
 			out.println("</form>");
 			out.println("<ul class=boxlisting>");
 			while (rs.next()) {
 				
 				String name = rs.getString("name");
 				int id = rs.getInt("quiz_id");
 				String cat = rs.getString("category");
 				Account author = am.getAccount(rs.getInt("creator_id"));
 				out.println(HTMLHelper.printQuizListing(id, name, cat, author));
 			}
 			out.println("</ul>");
 			out.println(HTMLHelper.contentEnd());
 			out.println("</body>");
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.out.println("oops!");
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String id = request.getParameter("id");
 		Connection con = (Connection) request.getServletContext().getAttribute("connect");
		ResultSet qMap;
 		try {
 			Statement stmt = (Statement) con.createStatement();
 			for (int i = 1; i < 7; i++) {
 				stmt.executeUpdate("delete from "+getTable(i)+" where question_id in (select question_id from quiz_question_mapping where question_type = "+i+");");
 			}
 			stmt.executeUpdate("delete from matching_question where question_id in (select question_id from quiz_question_mapping where question_type = 7);");
 			stmt.executeUpdate("delete from matching_question_mapping where matching_entry_id in (select question_id from quiz_question_mapping where question_type = 7);");
 			stmt.executeUpdate("delete from quiz_question_mapping where quiz_id = "+id+";");
 			stmt.executeUpdate("delete from quiz where quiz_id = "+id+";");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("Quiz deletion not implemented");
 		doGet(request, response);
 	}
 	
 	private String getTable(int index) {
 		String table = "";
 		switch(index) {
 		case 1: 
 			table = "question_response";
 			break;
 		case 2:
 			table = "fill_in_the_blank_question";
 			break;
 		case 3:
 			table = "multiple_choice_question";
 			break;
 		case 4:
 			table = "picture_response_question";
 			break;
 		case 5:
 			table = "multiple_answer_question";
 			break;
 		case 6:
 			table = "multiple_choice_multiple_answer_question";
 			break;
 		}
 		return table;
 	}
 }

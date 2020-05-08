 package com.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.common.MySQLHelper;
 import com.google.gson.Gson;
 import com.model.GradeCollection;
 import com.model.QuizCollection;
 import com.model.StudentAnswer;
 import com.model.StudentAnswerCollection;
 
 /**
  * Servlet implementation class QuizServlet
  */
 @WebServlet("/QuizServlet")
 public class QuizServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public QuizServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
 	{
 		HttpSession session = request.getSession();
 		PrintWriter writer = response.getWriter();
 		MySQLHelper mySQLHelper = MySQLHelper.getInstance();
 		Gson gson = new Gson();
 		
 		String method = request.getParameter("method");
 		if(method.equalsIgnoreCase("get"))
 		{
 			QuizCollection quizes = new QuizCollection();
 			quizes.setQuizes(mySQLHelper.getQuizes(session.getAttribute("username").toString()));
 			writer.print(gson.toJson(quizes,QuizCollection.class));
 		}
 		else if(method.equalsIgnoreCase("submit"))
 		{
 			StudentAnswerCollection studentAnswers = gson.fromJson(request.getParameter("json"), StudentAnswerCollection.class);
 			for(StudentAnswer studentAnswer : studentAnswers.getStudentAnswers())
 			{
 				MySQLHelper.getInstance().insertStudentAnswer(studentAnswer);
 			}
 		}
 		else if(method.equals("ping"))
 		{
 			MySQLHelper.getInstance().insertPing(request.getParameter("student"), request.getParameter("teacher"));
 		}
 		else if(method.equals("getPing"))
 		{
 			writer.println(MySQLHelper.getInstance().getMyPings(request.getParameter("teacher")));
 		}
 		else if(method.equals("getStudentGrades"))
 		{
 			GradeCollection grades = new GradeCollection();
 			grades.setGrades(MySQLHelper.getInstance().getStudentGrades(request.getParameter("student"), request.getParameter("course")));
 			writer.println(gson.toJson(grades,GradeCollection.class));
 		}
		
		else if(method.equals("getTeacherGrades"))
		{
			GradeCollection grades = new GradeCollection();
			grades.setGrades(MySQLHelper.getInstance().getTeacherGrades());
			writer.println(gson.toJson(grades,GradeCollection.class));
		}
 	}
 
 }

 package com.db.training.blb.cron;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.db.training.blb.dao.ConnectionEngine;
 import com.db.training.blb.dao.QueryEngine;
 
 /**
  * Servlet implementation class CronServlet
  */
 @WebServlet("/CronServlet")
 public class CronServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private static final Timer timer=new Timer();
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CronServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see Servlet#init(ServletConfig)
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		System.out.println(new Date()+" INFO: Cron Job Started");
 		timer.schedule(new TimerTask(){
 
 			@Override
 			public void run() {
 
 				try {
 					QueryEngine queryEngine=new QueryEngine(new ConnectionEngine());
 					ResultSet rs=queryEngine.query("select id from blb.transactions where transaction_status=0 and timestampdiff(MINUTE,timestamp(transaction_date),timestamp(now()))>1");
 					while(rs.next()){
						queryEngine.getConnectionEngine().update("update blb.transactions set transaction_status=1 where id=?", rs.getString(1));
 					}
 					rs.close();
 					rs=queryEngine.query("select id from blb.transactions where transaction_status=1 and timestampdiff(MINUTE,timestamp(transaction_date),timestamp(now()))>5");
 					while(rs.next()){
						queryEngine.getConnectionEngine().update("update blb.transactions set transaction_status=2 where id=?", rs.getString(1));
 					}
 					rs.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}finally{
 					System.out.println(new Date()+" INFO: Cron Job Executed");
 				}
 				
 			}
 			
 		}, 0, 30000);
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setHeader("Content-Type", "text/plain");
 		response.getOutputStream().write("Cron job started".getBytes());
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setHeader("Content-Type", "text/plain");
 		response.getOutputStream().write("Cron job started".getBytes());
 	}
 
 	@Override
 	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		resp.setHeader("Content-Type", "text/plain");
 		resp.getOutputStream().write("Cron job started".getBytes());
 	}
 
 	
 	
 }

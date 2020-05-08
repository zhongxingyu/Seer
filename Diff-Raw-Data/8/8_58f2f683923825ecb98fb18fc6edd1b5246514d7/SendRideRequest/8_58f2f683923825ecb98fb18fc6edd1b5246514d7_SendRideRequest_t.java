 package com.saventech.kaarpool;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class SendRideRequest extends HttpServlet
 {
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException 
     {
 		System.out.println("Sending ride request");
 		PrintWriter out=response.getWriter();
 		String desiredsendrequests=request.getParameter("checkboxesclicked");
 		String ridername=request.getParameter("senderridename");
 		System.out.println(desiredsendrequests.toString()+"        KKKKKKKKKKKKKKKKKKKKKK");
 		//String sendrequest[]=desiredsendrequests.toString().trim().split("::");
 		DBInterface connect = DBInterface.getInstance();
 	  	if(connect.isConnectionOpen())
 	  	{
	  		if(desiredsendrequests.toString().trim().length()!=0)
	  		{
 				String ouput=connect.sendrequest(desiredsendrequests.toString().trim(),ridername.toString().trim());
 				
 				out.print(ouput.toString().trim());
	  		}
	  		else
	  		{
	  			out.print("Please select a ride to send request");
	  		}
 				
 	    }
 	  	else
 	  	   {
 	  		   out.print("Please_check_the_connection");
 	  	   }
 		
     }
 
 }

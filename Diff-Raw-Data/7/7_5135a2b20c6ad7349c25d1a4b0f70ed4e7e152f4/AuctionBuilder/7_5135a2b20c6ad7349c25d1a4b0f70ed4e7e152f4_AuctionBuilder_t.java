 package edu.unsw.comp9321.assign2;
 
 import java.sql.*;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class AuctionBuilder {
 	ConnectionManager cm;
 	Connection c;
 	PreparedStatement auction; // insert item
 	String auctionString;
 	
 	public AuctionBuilder() {
 		cm = new ConnectionManager();
 		c = cm.getConnection();
 		
 		auctionString = "INSERT INTO auction (username, starttime, " +
 							"auctionlength, status, title, category, " +
 							"picture, description, postagedetails, " +
 							"startingprice, reserveprice, bidincrement)" +
 						"VALUES (?, ?, ?, 'new', ?, ?, ?, ?, ?, ?, ?, ?)";
 		
 		// prepare statement
 		try {
 			auction = c.prepareStatement(auctionString);
 		}
 		catch (SQLException s) {
 			System.out.println("An error occured initialising query");
 			s.printStackTrace();
 		}
 		
 	}
 	
 	/*
 	 * Adds auction to database and sends confirmation email
 	 * calls addToItem
 	 * calls addToAuction
 	 */
 	public void createAuction(HttpServletRequest request, HttpServletResponse response) {
 		/*This version does not yet allow for pictures to be added*/
 		System.out.println("createAuction called");
 		
 		try {
 			auction.setString(1, "user1");
 			auction.setString(2, request.getParameter("start"));
 			if (request.getParameter("biddingHours").equals("")) {
				auction.setInt(3, 10); System.out.println("empty length default to 10");
 			}
			else if (Integer.parseInt(request.getParameter("biddingHours")) < 3 ||
						Integer.parseInt(request.getParameter("biddingHours")) > 60) {
 				System.out.println("auction length is invalid");
 				redirectFailedAttempt(request, response);
 			}
 			else {
 				auction.setString(3, request.getParameter("length"));System.out.println("specified length");
 			}
 			auction.setString(4, request.getParameter("title"));
 			auction.setString(5, request.getParameter("category"));
 			auction.setString(6, request.getParameter("picture"));
 			auction.setString(7, request.getParameter("description"));
 			auction.setString(8, request.getParameter("postage"));
 			auction.setString(9, request.getParameter("startingPrice"));
 			auction.setString(10, request.getParameter("reservePrice"));
 			auction.setString(11, request.getParameter("biddingIncrements"));
 			
 			int result = auction.executeUpdate();
 			if (result > 0) {
 				System.out.println("successfully added auction");
 				RequestDispatcher rd = request.getRequestDispatcher("Success.jsp");
 				try {
 					rd.forward(request, response);
 				}
 				catch (Exception e) {
 					
 				}
 				
 			}
 			else {
 				redirectFailedAttempt(request, response);
 			}
 		}
 		catch (SQLException s) {
 			System.out.println("An error occured querying the database");
 			s.printStackTrace();
 			redirectFailedAttempt(request, response);
 		}
 		
 		return;
 	}
 	
 	/*
 	 * Initiates auction
 	 * sets start time
 	 * how to track auction not yet implemented - will need to initialize tracking
 	 */
 	public void confirm() { // rename to startAuction if needed
 		
 	}
 	
 	private void redirectFailedAttempt(HttpServletRequest request, HttpServletResponse response) {
 		try {
 			RequestDispatcher rdFailed = request.getRequestDispatcher("BidRegistration.jsp");
 			rdFailed.forward(request, response);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }

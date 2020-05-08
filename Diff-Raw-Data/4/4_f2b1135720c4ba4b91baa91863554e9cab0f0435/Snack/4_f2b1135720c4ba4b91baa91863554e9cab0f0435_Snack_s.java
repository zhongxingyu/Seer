 /**
  * Copyright (C) 2010-2011 Kenneth Prugh
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  */
 
 package modules;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import database.Postgres;
 
 public class Snack {
 	private final Connection db;
 
 	/* Instance Variables */
 	private final Random rgen = new Random();
 	/* rgen.nextint(snackArray.size()); */
 
 	private final List<Integer> snackArray = new ArrayList<Integer>(4000);
 
 	private String selectStatement;
 	private PreparedStatement prepStmt;
 	private ResultSet rs;
 
 	/**
 	 * The id of the previous !snack
 	 */
 	private int prevSnack;
 
 	/**
 	 * List of users that have voted on the current snack
 	 */
 	private final Votes votes = new Votes();
 
 	public Snack(Postgres db) {
 		this.db = db.getConnection();
 		updateSnackArray();
 	}
 
 	private void updateSnackArray() {
 		try {
 			selectStatement = "SELECT id FROM snacks";
 			prepStmt = db.prepareStatement(selectStatement);
 			rs = prepStmt.executeQuery();
 
 			snackArray.clear();
 
 			while (rs.next()) {
 				snackArray.add(rs.getInt("id"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
             if (rs != null) {
                 try { 
                     rs.close();
                 } catch (SQLException e) {}
             }
         }
 	}
 
 	public String getSnack() {
 		int snackid = snackArray.get(rgen.nextInt(snackArray.size()));
 		String snackResult = null;
 		/*
 		 * Let us cache the result so we may delete it if needed
 		 */
 		prevSnack = snackid;
 
 		String selectStatement = "SELECT snack FROM snacks WHERE id = ? ";
 		try {
 			prepStmt = db.prepareStatement(selectStatement);
 			prepStmt.setInt(1, snackid);
 			rs = prepStmt.executeQuery();
 
 			if (rs.next()) {
 				// snack is present
 				snackResult = rs.getString(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			snackResult = "Database Error";
 		} finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
             if (rs != null) {
                 try { 
                     rs.close();
                 } catch (SQLException e) {}
             }
         }
 
 		votes.clearVotes(snackid);
 		updateSnackCount();
 
 		return snackResult + " (#" + snackid + ")";
 	}
 
 	public String addSnack(final String msg, final String user) {
 		final String snack = msg.substring(9).trim();
 		if (snack.length() == 0) {
 			return "Screw off wise guy";
 		}
 		selectStatement = "INSERT INTO snacks (snack, by) VALUES(?, ?)";
 		try {
 			prepStmt = db.prepareStatement(selectStatement);
 			prepStmt.setString(1, snack);
 			prepStmt.setString(2, user);
 			prepStmt.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return "Error adding snack";
 		} finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
 		updateSnackArray();
 		return "snack added to database";
 	}
 
 	public String deleteSnack() {
 		selectStatement = "DELETE FROM snacks WHERE id = ?";
 		try {
 			prepStmt = db.prepareStatement(selectStatement);
 			prepStmt.setInt(1, prevSnack);
 			prepStmt.execute();
 
 			updateSnackArray();
 
 			return "Snack Terminated";
 		} catch (SQLException e) {
 			return "Database error";
 		} finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
 	}
 
 	/**
 	 * Increase the snackCount for the current snack by 1
 	 */
 	private void updateSnackCount() {
 		selectStatement = "UPDATE snacks SET snackcount = snackcount + 1 WHERE id = ?";
 		try {
 			prepStmt = db.prepareStatement(selectStatement);
 			prepStmt.setInt(1, prevSnack);
 			prepStmt.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
             if (prepStmt != null) {
                 try { 
                     prepStmt.close();
                 } catch (SQLException e) {}
             }
         }
 	}
 
 	/**
 	 * Record vote by user for the current snack
 	 * 
 	 * @param msg
 	 * @param user
 	 * @return
 	 */
 	public String voteSnack(String msg, String user) {
 		// Parse the msg and see if its valid
 		msg = msg.substring(5).trim();
 		int id;
 		String vote;
 
 		if (msg.matches("[\\\\+-] \\d+")) {
 			/* Input was valid */
 			try { 
 				id = Integer.parseInt(msg.split(" ")[1]);
 			} catch (NumberFormatException e) {
 				return "En Garde! Have at you!";
 			}
 			vote = msg.split(" ")[0];
 
 			/* Has the user voted on this snack before? */
 			if (votes.userHasVoted(id, user)) {
 				/* user has already voted */
 				return "You have already voted on snack " + id;
 			} else {
 				/* Lets VOTE! */
 				try { 
 					castVote(id, vote);
 				} catch (SQLException e) {
 					return "Database error";
                 } finally {
                     if (prepStmt != null) {
                         try { 
                             prepStmt.close();
                         } catch (SQLException e) {}
                     }
                 }
 				votes.storeUserVote(id, user);
 				return "You have voted " + vote + " for snack " + id;
 			}
 		} else {
 			return "Invalid. Valid input is <+ or -> <id>";
 		}
 	}
 
 	/**
 	 * Casts the vote to the database
 	 */
 	private void castVote(int id, String vote) throws SQLException {
 		if (vote.equals("+")) {
 			// user voted snack up
 			selectStatement = "UPDATE snacks SET votecount = votecount + 1 WHERE id = ?";
 		} else {
 			// user voted snack down
 			selectStatement = "UPDATE snacks SET votecount = votecount - 1 WHERE id = ?";
 		}
 		prepStmt = db.prepareStatement(selectStatement);
 		prepStmt.setInt(1, id);
 		prepStmt.execute();
 	}
 
 	/* Holds the current votes for snacks */
 	static class Votes {
 		Map<Integer, ArrayList<String>> votemap;
 		public Votes() {
			votemap = new HashMap<Integer, ArrayList<String>>();			
 		}
 
 		/**
 		 * Return whether the user has voted on this snack
 		 */
 		public boolean userHasVoted(int id, String user) {
 			if (votemap.containsKey(id)) {
 				ArrayList<String> votelist = votemap.get(id);
 				/* Has the user voted for this snack? */
 				if (votelist.contains(user)) {
 					return true;
 				} else {
 					return false;
 				}
 			} else {
 				/* This snack isn't tracked yet */
 				return false;
 			}
 		}
 
 		/**
 		 * Store the user as having voted for the given snack 
 		 */
 		public void storeUserVote(int id, String user) {
 			if (votemap.containsKey(id)) {
 				/* This snack is already tracked */
 				votemap.get(id).add(user);
 			} else {
 				/* This snack needs to be created first */
 				ArrayList<String> votelist = new ArrayList<String>();
 				votelist.add(user);
 				votemap.put(id, votelist);
 			}
 		}
 
 		/**
 		 * Clear votes for the given snack
 		 */
 		public void clearVotes(int id) {
 			if (votemap.containsKey(id)) {
 				votemap.get(id).clear();
 			}
 		}
 	}
 }
 

 package Server.statisticsModule;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import Server.userModule.UserObject;
 import java.rmi.*;
 import java.rmi.server.UnicastRemoteObject;
 
 
 public class LeaderBoard extends UnicastRemoteObject implements IStatistics {
 
 	protected LeaderBoard() throws RemoteException {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	public String[][] leaderBoardDisplay() throws SQLException, RemoteException{
 		return Data.statistics.createLeaderBoard();
 	}
 	/** 
 	 * This creates a 2D array where the first layer is the usernames in ranked order, the second is the number of games won
 	 * and the third is the amount earned while playing
 	 */
 }

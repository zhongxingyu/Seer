 package Server.statisticsModule;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import Server.userModule.UserObject;
 import java.rmi.*;
 import java.rmi.server.UnicastRemoteObject;
 
<<<<<<< HEAD
/**
 * Remote statistics implementation
 * @author mouhyi
 *
 */
=======
public class LeaderBoard {
>>>>>>> b1f9c5b7d05f126f7eb82eca7696a2a9c5912f4b
 
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

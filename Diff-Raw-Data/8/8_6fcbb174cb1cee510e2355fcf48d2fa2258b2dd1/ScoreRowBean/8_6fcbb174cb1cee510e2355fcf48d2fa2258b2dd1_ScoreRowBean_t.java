 package failkidz.fkzteam.beans;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 public class ScoreRowBean implements Comparable<ScoreRowBean>{
 	
 	private String teamName;
 	private int teamId;
 	private int gamesPlayed;
 	private int gamesWon;
 	private int gamesLost;
 	private int goalsMade;
 	private int goalsAgainst;
 	private int goalDiff;
 	private int points;
 	
 	private Connection conn;
 	
 	/**
 	 * Default constructor that will create and single row in the database
 	 * with the teamId and the rest set to 0
 	 */
 	public ScoreRowBean(int teamId){
 		this.teamName = null;
 		this.teamId = teamId;
 		this.gamesPlayed = 0;
 		this.gamesWon = 0;
 		this.gamesLost =  0;
 		this.goalsMade = 0;
 		this.goalsAgainst = 0;
 		this.points = 0;	
 		//Now call the insert method to save to the database
 		//this.insert();
 	}
 	
 	public ScoreRowBean(int teamId,int gamesPlayed,int gamesWon,int gamesLost,int goalsMade,int goalsAgainst,int points){
 		this.teamName = null;
 		this.teamId = teamId;
 		this.gamesPlayed = gamesPlayed;
 		this.gamesWon = gamesWon;
 		this.gamesLost =  gamesLost;
 		this.goalsMade = goalsMade;
 		this.goalsAgainst = goalsAgainst;
 		this.points = points;
 	}
 	
 	public void deleteThisRow(){
 		//this.delete();
 	}
 	
 	/**
 	 * Does a insert of the database of the current instance of this class
 	 */
 	public void insert(){
 		this.initDatabase();
 		//INSERT INTO table_name (column1, column2, column3,...)
 		String query = "INSERT INTO score VALUES ("+this.teamId+","+this.gamesPlayed+","+this.gamesWon+","+this.gamesLost+","+this.goalsMade+","+this.goalsAgainst+","+this.points+");";
 		this.execute(query);
 	}
 	
 	/**
 	 * Delete this object in the database
 	 */
 	private void delete(){
 		this.initDatabase();
 		/*
 		 * Not used at the moment
 		 */
 		String query = "DELETE FROM score WHERE teamsid="+this.teamId+";";
 		this.execute(query);
 	}
 	
 	/**
 	 * Updates the row in the database
 	 */
 	public void update(){
 		this.initDatabase();
 		String query = "UPDATE score SET teamsid="+this.teamId+", gamesplayed="+this.gamesPlayed+", gameswon="+this.gamesWon+", gameslost="+this.gamesLost+", goalsscored="+this.goalsMade+", goalsagainst="+this.goalsAgainst+", points="+this.points+" WHERE teamsid="+ this.teamId+";";
 		System.out.println(query);
 		this.execute(query);
 	}
 	
 	public void loadBean(){
 		this.initDatabase();
 		String query = "SELECT * FROM score WHERE teamsid="+this.teamId+";";
 		ResultSet rs = executeQuery(query);
 		//Since we only want to get one row we can simply just get the column values
 		try {
 			this.teamId = rs.getInt(1);
 			this.gamesPlayed = rs.getInt(2);
 			this.gamesWon = rs.getInt(3);
 			this.gamesLost = rs.getInt(4);
 			this.goalsMade = rs.getInt(5);
 			this.goalsAgainst = rs.getInt(6);
 			this.points = rs.getInt(7);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	private void execute(String query){
 		Statement stmt = null;
 		try{
 			stmt = conn.createStatement();
 			stmt.execute(query);
 		}
 		catch(SQLException e){
 		}
 		finally{
 			try {
 				stmt.close();
 				conn.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private ResultSet executeQuery(String query){
 		ResultSet rs = null;
 		Statement stmt = null;
 		try{
 			stmt = conn.createStatement();
 			rs = stmt.executeQuery(query);
 		}
 		catch(SQLException e){
 		}
 		finally{
 			try {
 				stmt.close();
 				conn.close();
 				rs.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 			}
 		}
 		return rs;
 	}
 	
 	private void initDatabase(){
 		try{
 			Context initCtx = new InitialContext();
 			Context envCtx = (Context) initCtx.lookup("java:comp/env");
 			DataSource ds = (DataSource)envCtx.lookup("jdbc/db");
 			conn = ds.getConnection();
 		}
 		catch(SQLException e){
 			
 		}
 		catch(NamingException e){
 
 		}
 	}
 	
 	public String getHtmlRow(){
 		StringBuilder sb = new StringBuilder();
 		sb.append("<tr>\n");
 		sb.append("<td>"+this.getTeamName()+"</td>\n");
 		sb.append("<td>"+this.getGamesPlayed()+"</td>\n");
 		sb.append("<td>"+this.getGamesWon()+"</td>\n");
 		sb.append("<td>"+this.getGamesLost()+"</td>\n");
 		sb.append("<td>"+this.getGoalsMade()+"</td>\n");
 		sb.append("<td>"+this.getGoalsAgainst()+"</td>\n");
 		sb.append("<td>"+this.getGoalDiff()+"</td>\n");
 		sb.append("<td>"+this.getPoints()+"</td>\n");
 		sb.append("</tr>\n");
 		
 		return sb.toString();
 	}
 	
 	public String getJSONArray(){
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("    {\n");
 		sb.append("        \"teamname\": \""+this.getTeamName()+"\",\n");
 		sb.append("        \"gamesplayed\": \""+this.getGamesPlayed()+"\",\n");
 		sb.append("        \"gameswon\": \""+this.getGamesWon()+"\",\n");
 		sb.append("        \"gameslost\": \""+this.getGamesLost()+"\",\n");
 		sb.append("        \"goalsmade\": \""+this.getGoalsMade()+"\",\n");
 		sb.append("        \"goalsagainst\": \""+this.getGoalsAgainst()+"\",\n");
 		sb.append("        \"goalsdiff\": \""+this.getGoalDiff()+"\",\n");
 		sb.append("        \"points\": \""+this.getPoints()+"\"\n");
 		sb.append("    }");
 		
 		return sb.toString();
 	}
 		
 	/*
 	 * A lot of getters and setters...
 	 */
 		
 	public String getTeamName() {
 		System.out.println("TEAMNAME IS: " + this.teamName);
 		if(this.teamName == null){
 			try{
 				Context initCtx = new InitialContext();
 				Context envCtx = (Context) initCtx.lookup("java:comp/env");
 				DataSource ds = (DataSource)envCtx.lookup("jdbc/db");
 				conn = ds.getConnection();
 			}
 			catch(SQLException e){
 			}
 			catch(NamingException e){
 			}
 				
 			String query = "SELECT * FROM teams WHERE id="+this.teamId+";";
 			ResultSet rs = null;
 			Statement stmt = null;
 			try{
 				stmt = conn.createStatement();
 				rs = stmt.executeQuery(query);
 				StringBuilder sb = new StringBuilder();
				rs.next();
 				sb.append(rs.getString(2));
 				
 				
 				setTeamName(sb.toString());
 			}
 			catch(SQLException e){
 			}
 			finally{
 				try {
 					stmt.close();
 					conn.close();
 					rs.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					//e.printStackTrace();
 				}
 			}
 			
 			return this.teamName;
 		}
 		else{
 			return this.teamName;
 		}
 	}
 	public void setTeamName(String teamName) {
 		this.teamName = teamName;
 	}
 	public int getTeamId() {
 		return teamId;
 	}
 	public void setTeamId(int teamId) {
 		this.teamId = teamId;
 	}
 	public int getGamesPlayed() {
 		return gamesPlayed;
 	}
 	public void setGamesPlayed(int gamesPlayed) {
 		this.gamesPlayed = gamesPlayed;
 	}
 	public int getGamesWon() {
 		return gamesWon;
 	}
 	public void setGamesWon(int gamesWon) {
 		this.gamesWon = gamesWon;
 	}
 	public int getGamesLost() {
 		return gamesLost;
 	}
 	public void setGamesLost(int gamesLost) {
 		this.gamesLost = gamesLost;
 	}
 	public int getGoalsMade() {
 		return goalsMade;
 	}
 	public void setGoalsMade(int goalsMade) {
 		this.goalsMade = goalsMade;
 	}
 	public int getGoalsAgainst() {
 		return goalsAgainst;
 	}
 	public void setGoalsAgainst(int goalsAgainst) {
 		this.goalsAgainst = goalsAgainst;
 	}
 	public int getGoalDiff() {
 		return (getGoalsMade()-getGoalsAgainst());
 	}
 	public void setGoalDiff(int goalDiff) {
 		this.goalDiff = goalDiff;
 	}
 	public int getPoints() {
 		return points;
 	}
 	public void setPoints(int points) {
 		this.points = points;
 	}
 
 	
 	@Override
 	public int compareTo(ScoreRowBean other) {
 		return Double.compare(other.getPoints(),this.getPoints());
 	}
 }

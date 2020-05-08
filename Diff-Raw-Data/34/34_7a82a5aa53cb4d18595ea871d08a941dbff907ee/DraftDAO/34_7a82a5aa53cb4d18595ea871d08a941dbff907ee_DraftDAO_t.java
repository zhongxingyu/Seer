 package edu.psu.sweng.ff.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.psu.sweng.ff.common.Draft;
 import edu.psu.sweng.ff.common.League;
 import edu.psu.sweng.ff.common.Member;
 import edu.psu.sweng.ff.common.Team;
 
 public class DraftDAO extends BaseDAO {
 	
 	private final static String CHECK = "SELECT league_id FROM ff_drafts WHERE league_id = ?";
 
	private final static String CHECK2 = "SELECT DISTINCT league_id FROM ff_draft_order WHERE league_id = ?";

 	private final static String SELECT_BY_LEAGUE = "SELECT automatic, round, team_index, member_id "
 			+ "FROM ff_drafts WHERE league_id = ?";
 
 	private final static String STORE = "INSERT INTO ff_drafts (league_id, automatic, round, " +
 			"team_index, member_id) VALUES (?, ?, ?, ?, ?)";
 
 	private final static String UPDATE = "UPDATE ff_drafts SET automatic = ?, round = ?, "
 			+ "team_index = ?, member_id = ? WHERE league_id = ?";
 	
 	private final static String STORE_ORDER = "INSERT INTO ff_draft_order " +
 			"(league_id, round_order, team_id) VALUES (?, ?, ?)";
 	
 	private final static String LOAD_ORDER = "SELECT team_id FROM ff_draft_order " +
 			"WHERE league_id = ? ORDER BY round_order";
 
 	private final static String REMOVE = "DELETE FROM ff_drafts WHERE league_id = ?";
 	private final static String REMOVE_ORDER = "DELETE FROM ff_draft_order WHERE league_id = ?";
 	
 	public Draft loadByLeague(League l) {
 
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 		Draft draft = null;
 		try {
 			draft = this.loadByLeague(l, conn);
 		} finally {
 			close(conn);
 		}
 		return draft;
 	}
 	
 	public Draft loadByLeague(League l, Connection conn) {
 
 		Draft d = null;
 
 		PreparedStatement stmt1 = null;
 		PreparedStatement stmt2 = null;
 		ResultSet rs1 = null;
 		ResultSet rs2 = null;
 		
 		try {
 
 			stmt1 = conn.prepareStatement(SELECT_BY_LEAGUE);
 			stmt1.setInt(1, l.getId());
 			
 			rs1 = stmt1.executeQuery();
 			
 			if (rs1.next()) {
 				
 				d = new Draft();
 				d.setLeagueId(l.getId());
 				d.setAutomatic(rs1.getBoolean(1));
 				d.setRound(rs1.getInt(2));
 				d.setTeamIndex(rs1.getInt(3));
 				MemberDAO mdao = new MemberDAO();
 				d.setWaitingFor(mdao.loadByUserName(rs1.getString(4), conn));
 				
 				List<Team> order = new ArrayList<Team>();
 				stmt2 = conn.prepareStatement(LOAD_ORDER);
 				stmt2.setInt(1, l.getId());
 				rs2 = stmt2.executeQuery();
 				TeamDAO tdao = new TeamDAO();
 				while (rs2.next()) {
 					Team t = tdao.loadById(rs2.getInt(1), conn);
 					order.add(t);
 				}
 				d.setTeamOrder(order);
 
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			close(rs2);
 			close(rs1);
 			close(stmt1);
 			close(stmt2);
 		}
 		
 		return d;
 		
 	}
 	
 	public boolean store(Draft d) {
 
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 
 		PreparedStatement stmt1 = null;
 		PreparedStatement stmt2 = null;
 		PreparedStatement stmt3 = null;
		PreparedStatement stmt4 = null;
 		ResultSet rs = null;
		ResultSet rs2 = null;
 
 		try {
 			
 			stmt1 = conn.prepareStatement(CHECK);
 			stmt1.setInt(1, d.getLeagueId());
 			rs = stmt1.executeQuery();
 
 			if (rs.next()) {

 				// already there. update.
 				stmt2 = conn.prepareStatement(UPDATE);
 				stmt2.setBoolean(1, d.isAutomatic());
 				stmt2.setInt(2, d.getRound());
 				stmt2.setInt(3, d.getTeamIndex());
 				Member m = d.getWaitingFor();
 				if (m != null) {
 					stmt2.setString(4, m.getUserName());
 				} else {
 					stmt2.setNull(4, java.sql.Types.VARCHAR);
 				}
 				stmt2.setInt(5, d.getLeagueId());
 				stmt2.executeUpdate();
				
 			} else {
 				
 				// not there yet. insert.
 				stmt2 = conn.prepareStatement(STORE);
 				stmt2.setInt(1, d.getLeagueId());
 				stmt2.setBoolean(2, d.isAutomatic());
 				stmt2.setInt(3, d.getRound());
 				stmt2.setInt(4, d.getTeamIndex());
 				Member m = d.getWaitingFor();
 				if (m != null) {
 					stmt2.setString(5, m.getUserName());
 				} else {
 					stmt2.setNull(5, java.sql.Types.VARCHAR);
 				}
 				stmt2.executeUpdate();
 				
			}

			stmt4 = conn.prepareStatement(CHECK2);
			stmt4.setInt(1, d.getLeagueId());
			rs2 = stmt4.executeQuery();

			// store draft order if not already stored (immutable once created)
			if (!rs2.next()) {

 				if (d.getTeamOrder() != null) {
 					stmt3 = conn.prepareStatement(STORE_ORDER);
 					stmt3.setInt(1, d.getLeagueId());
 					for (int i = 0; i < d.getTeamOrder().size(); i++) {
 						Team t = d.getTeamOrder().get(i);
 						stmt3.setInt(2, i);
 						stmt3.setInt(3, t.getId());
 						stmt3.executeUpdate();
 					}
 				}

 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			close(rs);
			close(rs2);
 			close(stmt1);
 			close(stmt2);
 			close(stmt3);
			close(stmt4);
 			close(conn);
 		}
 		
 		return true;
 		
 	}
 
 	public boolean remove(Draft d) {
 
 		DatabaseConnectionManager dbcm = new DatabaseConnectionManager();
 		Connection conn = dbcm.getConnection();
 
 		PreparedStatement stmt1 = null;
 
 		try {
 			
 			stmt1 = conn.prepareStatement(REMOVE);
 			stmt1.setInt(1, d.getLeagueId());
 			stmt1.executeUpdate();
 
 			stmt1 = conn.prepareStatement(REMOVE_ORDER);
 			stmt1.setInt(1, d.getLeagueId());
 			stmt1.executeUpdate();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			close(stmt1);
 			close(conn);
 		}
 		
 		return true;
 		
 	}
 
 }

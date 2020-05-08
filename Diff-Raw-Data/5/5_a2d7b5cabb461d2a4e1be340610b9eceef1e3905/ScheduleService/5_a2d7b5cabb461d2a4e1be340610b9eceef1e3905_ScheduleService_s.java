 package com.sas.comp.service;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sas.comp.models.Schedule;
 import com.sas.comp.mysql.Database;
 
 public class ScheduleService {
 
 	public List<Schedule> getLeagueSchedule(final Integer seasonId) {
 		return getSchedules(seasonId, false);
 	}
 
 	public List<Schedule> getPlayoffSchedule(final Integer seasonId) {
 		return getSchedules(seasonId, true);
 	}
 
 	private List<Schedule> getSchedules(final Integer seasonId, final Boolean playoff) {
 		final List<Schedule> schedules = new ArrayList<Schedule>();
 
 		try {
 			final Connection conn = Database.getConnection();
 			final PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM schedule WHERE playoff = ? AND season_id = ?");
 			pstmt.setBoolean(1, playoff);
 			pstmt.setInt(2, seasonId);
 
 			final ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				final Schedule schedule = new Schedule();
 				schedule.setHome(rs.getString("home"));
 				schedule.setAway(rs.getString("away"));
 				schedule.setDate(rs.getTimestamp("date"));
 				schedule.setResult(rs.getInt("home_score") + " - " + rs.getInt("away_score"));
 				schedules.add(schedule);
 			}
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 
 		return schedules;
 	}
 
 }

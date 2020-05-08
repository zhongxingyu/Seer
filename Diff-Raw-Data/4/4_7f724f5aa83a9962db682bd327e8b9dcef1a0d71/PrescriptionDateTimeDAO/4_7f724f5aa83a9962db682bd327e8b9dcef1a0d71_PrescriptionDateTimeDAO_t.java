 package backend.DAO;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import backend.SQLDatabase;
 
 import shared.Patient;
 import shared.PrescriptionDateTime;
 
 public class PrescriptionDateTimeDAO implements
 		SQLDAO<PrescriptionDateTime, Integer> {
 
 	private static final String TABLE = "prescription_meta";
 
 	@Override
 	public PrescriptionDateTime insert(PrescriptionDateTime dao)
 			throws SQLException {
 		if (dao.forPrescription == null)
 			throw new SQLException(
 					"PrescriptionDateTime.forPrescription cannot be null");
 
 		Connection conn = SQLDatabase.getConnection();
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("INSERT INTO ");
 			sb.append(PrescriptionDateTimeDAO.TABLE);
 			sb.append(" ( `id`,   `prescription_id`, `meta_key`, `meta_value`, `day_time` ) ");
 			sb.append(" VALUES ");
 			sb.append(" ( NULL,    ?,                 NULL,       NULL,         ?         );");
 
 			PreparedStatement ps = conn.prepareStatement(sb.toString(),
 					Statement.RETURN_GENERATED_KEYS);
 
 			ps.setInt(1, dao.forPrescription);
 			ps.setTime(2, dao.timeOfDay);
 
 			// Try and add it
 			ps.executeUpdate();
 
 			ResultSet rs = ps.getGeneratedKeys();
 			if (rs.next()) {
 				// Retrieve the newly created id and pass back the Alert object
 				dao.id = rs.getInt(1);
 				return dao;
 			} else
 				throw new SQLException("Could not retrieve updated values");
 		} finally {
 			conn.close();
 		}
 	}
 
 	@Override
 	public boolean update(PrescriptionDateTime dao) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("UPDATE ");
 			sb.append(PrescriptionDateTimeDAO.TABLE);
 			sb.append(" SET ");
 			sb.append("prescription_id = ?, ");
 			sb.append("day_time = ? ");
 			sb.append(" WHERE id = ?");
 
 			PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 			ps.setInt(1, dao.forPrescription);
 			ps.setTime(2, dao.timeOfDay);
			ps.setInt(3, dao.id);
 
 			// Try and add it
 			ps.execute();
 
 			return ps.getUpdateCount() > 0;
 		} finally {
 			conn.close();
 		}
 	}
 
 	@Override
 	public boolean delete(PrescriptionDateTime dao) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("DELETE FROM ");
 			sb.append(PrescriptionDateTimeDAO.TABLE);
 			sb.append(" WHERE id = ?");
 
 			PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 			ps.setInt(1, dao.id);
 
 			// Try and add it
 			ps.execute();
 
 			return ps.getUpdateCount() > 0;
 		} finally {
 			conn.close();
 		}
 	}
 
 	@Override
 	public ArrayList<PrescriptionDateTime> findAll() throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("SELECT id FROM ");
 			sb.append(PrescriptionDateTimeDAO.TABLE);
 			PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 			// Try and add it
 			ResultSet result = ps.executeQuery();
 
 			ArrayList<PrescriptionDateTime> list = new ArrayList<PrescriptionDateTime>();
 
 			while (result.next()) {
 				list.add(findByPrimaryKey(result.getInt("id")));
 			}
 
 			return list;
 		} finally {
 			conn.close();
 		}
 	}
 
 	@Override
 	public PrescriptionDateTime findByPrimaryKey(Integer key)
 			throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("SELECT * FROM ");
 			sb.append(PrescriptionDateTimeDAO.TABLE);
 			sb.append(" WHERE id = ?");
 			PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 			ps.setInt(1, key);
 
 			// Try and add it
 			ResultSet result = ps.executeQuery();
 
 			if (result.next()) {
 				return new PrescriptionDateTime(result.getInt("id"),
 						result.getInt("prescription_id"),
 						result.getTime("day_time"));
 			} else
 				return null;
 		} finally {
 			conn.close();
 		}
 	}
 
 	public ArrayList<PrescriptionDateTime> findByPrescriptionKey(Integer key)
 			throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("SELECT id FROM ");
 			sb.append(PrescriptionDateTimeDAO.TABLE);
 			sb.append(" WHERE prescription_id = ?");
 			PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 			ps.setInt(1, key);
 
 			// Try and add it
 			ResultSet result = ps.executeQuery();
 
 			ArrayList<PrescriptionDateTime> list = new ArrayList<PrescriptionDateTime>();
 
 			while (result.next()) {
 				list.add(findByPrimaryKey(result.getInt("id")));
 			}
 
 			return list;
 		} finally {
 			conn.close();
 		}
 	}
 
 }

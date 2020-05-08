 package uk.co.brotherlogic.mdb.db.upgrade;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import uk.co.brotherlogic.mdb.Connect;
 
 public abstract class Version {
 	public abstract int getVersion();
 
 	public boolean run() throws SQLException {
 		System.out.println("Upgrading to version: " + getVersion());
 		boolean res = runLocal();
 		if (res)
 			setVersion(getVersion() + 1);
 		return res;
 	}
 
 	public abstract boolean runLocal() throws SQLException;
 
 	public void setVersion(int value) throws SQLException {
		String sql = "UPDATE db_props set version = ?";
 		PreparedStatement ps = Connect.getConnection()
 				.getPreparedStatement(sql);
 		ps.setInt(1, value);
 		ps.execute();
 		ps.close();
 	}
 }

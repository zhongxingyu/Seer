 package de.softwarekollektiv.dbs.parser.imdb;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import de.softwarekollektiv.dbs.dbcon.DbConnection;
 import de.softwarekollektiv.dbs.parser.Parser;
 
 public class LocationsParser extends AbstractImdbParser implements Parser {
 
 	private final PreparedStatement locationsStatement;
 	private final PreparedStatement shotInStatement;
 
 	public LocationsParser(DbConnection dbcon, String file) throws SQLException {
 		super(dbcon, file);
 		super.delimiter = "\t+";
 		// LOCATIONS LIST
 		super.firstStop = "==============";
 
 		locationsStatement = dbcon.getConnection().prepareStatement(
 				"INSERT INTO locations VALUES (DEFAULT, ?)",
 				Statement.RETURN_GENERATED_KEYS);
 
 		shotInStatement = dbcon.getConnection().prepareStatement(
 				"INSERT INTO shotIn VALUES (("
 						+ "		SELECT mov_id FROM movies WHERE title = ?)" +
 								",?)");
 	}
 
 	@Override
 	protected void newLine(String[] lineParts) {
 
 		String location = lineParts[1].split("|")[0];
 		String movieTitle = lineParts[0];
 
 		try {
 			locationsStatement.setString(1, location);
 			locationsStatement.execute();
 
 			ResultSet result = locationsStatement.getGeneratedKeys();
 			result.next();
 			int locId = result.getInt(1);
 
 			shotInStatement.setString(1, movieTitle);
 			shotInStatement.setInt(2, locId);
 			shotInStatement.execute();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }

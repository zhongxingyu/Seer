 package de.softwarekollektiv.dbs.parser.imdb;
 
 import java.math.BigInteger;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 
 import de.softwarekollektiv.dbs.dbcon.DbConnection;
 import de.softwarekollektiv.dbs.parser.AbstractParser;
 import de.softwarekollektiv.dbs.parser.Parser;
 
 public class ActorsParser extends AbstractParser implements Parser {
 
 	String currentActor;
 	private PreparedStatement actorsStatement;
 	private PreparedStatement featuresStatement;
 	private boolean actorIsCommited;
 
 	public ActorsParser(DbConnection dbcon, String file, boolean male)
 			throws SQLException {
 		super(dbcon, file);
 		super.delimiter = "\t+";
 		super.firstStop = "----\t\t\t------";
 		super.table = "actors";
 		super.values = 3;
 
 		actorsStatement = dbcon.getConnection().prepareStatement(
 				"INSERT INTO actors VALUES (DEFAULT, ? , " + male + ")",
 				Statement.RETURN_GENERATED_KEYS);
 		featuresStatement = dbcon.getConnection().prepareStatement(
 				"INSERT INTO features VALUES (("
 						+ "		SELECT mov_id FROM movies WHERE title = ?)"
 						+ ",?)");
 
 	}
 
 	@Override
 	public void newLine(String[] lineParts) {
 
 		/*
 		 * if newline the current actor has no more featuring movies
 		 */
 		if (lineParts[0].equals("")) {
 			currentActor = null;
 			actorIsCommited = false;
 			return;
 		}
 		
 		try {
 			/*
 			 * new Actor starting
 			 */
 			if (currentActor == null) {
 				currentActor = lineParts[0];				
 			}
 
 			if (!(lineParts[1].contains("2010") || lineParts[1].contains("2011"))){
 				return;
 			}
 			
 			if (!actorIsCommited){
 				actorsStatement.setString(1, currentActor);
 				actorsStatement.execute();
 				actorIsCommited = true;
 			}
 			
 			ResultSet result = actorsStatement.getGeneratedKeys();
 			result.next();
 			int actId = result.getInt(1);
 
 			featuresStatement.setString(1, lineParts[1].split("  ")[0]);
 			featuresStatement.setInt(2, actId);
 			featuresStatement.execute();
 
 		} catch (SQLException e) {
 			log.debug(Arrays.toString(lineParts), e);
 		}
 
 	}
 }

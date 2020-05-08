 package kea.kme.pullpit.server.persistence;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 
 import kea.kme.pullpit.client.objects.Band;
 import kea.kme.pullpit.client.objects.Contact;
 //import java.util.logging.Logger;
 
 /**
  * Creates all db-queries and sends them to DBConnector
  * 
  * @author Kasper
  * 
  */
 
 public class ObjectHandler {
 //	private static final Logger log = Logger.getLogger(ObjectHandler.class
 //			.getName());
 
 	public static Band[] getBands(int offset, int limit, String orderBy)
 			throws SQLException {
 		Connection con = DBConnector.getInstance().getConnection();
 		ArrayList<Band> results = new ArrayList<Band>();
 		Statement s = con.createStatement();
		String sql = "SELECT bands.*, contacts.* "
 				+ "FROM ((bands LEFT OUTER JOIN agents ON bands.bandID = agents.bandID) "
 				+ "LEFT OUTER JOIN contacts ON agents.contactID = contacts.contactID) "
 				+ "ORDER BY " + orderBy + " LIMIT " + offset + "," + limit;
 		ResultSet rs = s.executeQuery(sql);
 //		log.info("Handling results");
 		while (rs.next()) {
 			int bandID = rs.getInt(1);
 //			log.info("got band"+bandID);
 			String bandName = rs.getString(2);
 			String bandCountry = rs.getString(3);
 			int promoterID = rs.getInt(4);
 //			log.info("Getting date");
 			Date lastEdit = rs.getDate(5);
 //			log.info("Got date");
 //			log.info("Adding band");
 //			log.info("Handling agents");
 			if (rs.getString("contactName")==null) {
 				results.add(new Band(bandID, bandName, bandCountry, promoterID,
 					lastEdit));
 			} else {
 				results.add(new Band(bandID, bandName, bandCountry, promoterID,
 						lastEdit, new Contact(rs.getInt("contactID"), rs.getString("contactName"))));
 			}
 		}
 		rs.close();
 //		log.info("Returning results");
 		return results.toArray(new Band[results.size()]);
 	}
 
 	protected static Band getBandByID(int bandID) {
 		return null;
 	}
 
 	public static void putBand(Band band) throws SQLException {
 //		Connection dbcon = DBConnector.getInstance().getConnection();
 	}
 }

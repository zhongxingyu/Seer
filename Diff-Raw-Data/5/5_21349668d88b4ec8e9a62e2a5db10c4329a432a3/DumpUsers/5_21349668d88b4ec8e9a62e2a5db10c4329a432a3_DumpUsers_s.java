 /**
  * 
  */
 package webctdbexport.jdbc;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.hibernate.Session;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import webctdbexport.db.LearningContext;
 import webctdbexport.jdbc.model.Person;
 import webctdbexport.test.TestRepository;
 import webctdbexport.tools.DumpUtils;
 import webctdbexport.utils.DbUtils;
 //import webctdbexport.utils.MoodleRepository;
 
 /** Dump all users and their own file areas in whole database ?!
  * 
  * @author cmg
  *
  */
 public class DumpUsers {
 	private static final String DONE_FILE = "done.ts";
 	static Logger logger = Logger.getLogger(DumpUsers.class.getName());
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length<4) {
 			System.err.println("Usage: <jdbc.properties> <outputdir> <filedir> <extrapermissionsfile> [usernames...]");
 			System.err.println("Extra permissions file format: <username> lc<learningcontextid> ...");
 			System.exit(-1);
 		}
 		File outputdir = new File(args[1]);
 		if (!outputdir.exists() || !outputdir.canWrite() || !outputdir.isDirectory()) {
 			logger.log(Level.SEVERE, "Output directory does not exist or is not writable: "+outputdir);
 			System.exit(-1);
 		}
 		File filedir = new File(args[2]);
 		if (!filedir.exists() || !filedir.canWrite() || !filedir.isDirectory()) {
 			logger.log(Level.SEVERE, "File directory does not exist or is not writable: "+filedir);
 			System.exit(-1);
 		}
 		File extrapermissionsfile = new File(args[3]);
 		if (!extrapermissionsfile.exists() || !extrapermissionsfile.canRead() || !extrapermissionsfile.isFile()) {
 			logger.log(Level.SEVERE, "Cannot read extra permissions file: "+extrapermissionsfile);
 			System.exit(-1);
 		}
 		Map<String,Set<String>> extrapermissions = readExtrapermissions(extrapermissionsfile);
 		Connection conn = JdbcUtils.getConnection(args[0]);
 		try {
 			boolean dumpAll = true;
 			logger.log(Level.INFO, "output folders to "+outputdir);
 			List<BigDecimal> personIds = null;
			if (args.length<=3) {
 				logger.log(Level.INFO, "Dump all users...");
 				personIds = MoodleRepository.getPersonIds(conn);
 				logger.log(Level.INFO, "Found "+personIds.size()+" active nondemo users");
 			} else {
 				dumpAll = false;
 				personIds = new LinkedList<BigDecimal>();
				for (int ai=3; ai<args.length; ai++) {
 					Person p = MoodleRepository.getPersonByWebctId(conn, args[ai]);
 					if (p==null)
 						logger.log(Level.WARNING,"Could not find user "+args[ai]);
 					else
 						personIds.add(p.getId());
 				}
 			}
 			for (BigDecimal personId : personIds) {
 				Person p = MoodleRepository.getPerson(conn, personId);
 				if (p==null) {
 					logger.log(Level.WARNING,"Could not find Person "+personId);
 					continue;
 				}
 				String username = p.getWebctId();
 				if (username==null || username.length()<3) {
 					logger.log(Level.WARNING,"Ignoring user "+username+" (short username)");
 					continue;
 				}
 				logger.log(Level.INFO, "Dump Person "+username);
 
 				// learning contexts...
 				JSONObject listing = MoodleRepository.getListingForUser(conn, username, true, true, extrapermissions.get(username));				
 				List<JSONObject> items = new LinkedList<JSONObject>();
 				if (listing.getJSONArray("list").length()==0)
 				{
 					logger.log(Level.INFO,"Skip user "+username+" (no folder, etc.)");
 					continue;
 				}
 				String user2 = username.substring(0,2);
 				String user3 = username.substring(0,3);
 				File userdir = new File(new File(new File(new File(outputdir, "user"), user2), user3), username);
 				userdir.mkdirs();
 				
 				DumpUtils.writeResponse(listing, userdir, false);
 				//DumpUtils.addPersonItems(items, listing, "/");
 				// not mark as done?!
 				DumpUtils.addItems(items, listing, null/*"/"*/);
 
 				DumpAll.processItems(conn, items, outputdir, filedir);
 			}
 			logger.log(Level.INFO, "Done all users");
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error", e);
 		}
 		finally {
 			try { conn.close(); } catch (Throwable ignore) {}
 		}
 	}
 
 	private static Map<String, Set<String>> readExtrapermissions(
 			File extrapermissionsfile) {
 		logger.log(Level.INFO, "Read extra permissions from "+extrapermissionsfile);
 		Map<String,Set<String>> extrapermissions = new HashMap<String,Set<String>>();
 		try {
 			// warning: default encoding
 			BufferedReader br = new BufferedReader(new FileReader(extrapermissionsfile));
 			int count = 0;
 			while (true) {
 				String line = br.readLine();
 				count++;
 				if (line==null)
 					break;
 				if (line.startsWith("#") || line.startsWith("//"))
 					continue;
 				String values[] = line.trim().split("[ \\t,]");
 				if (values.length<2) 
 					throw new IOException("Extra persmission line "+count+" too short: "+line);
 				String username = values[0];
 				for (int i=1; i<values.length; i++) {
 					String lc = values[i];
 					if (!lc.startsWith("lc"))
 						throw new IOException("Extra permission line "+count+", LC "+lc+" is not valid (does not start with 'lc'): "+line);
 					Set<String> lcs = extrapermissions.get(username);
 					if (lcs==null) {
 						lcs = new TreeSet<String>();
 						extrapermissions.put(username, lcs);
 					}
 					lcs.add(lc);
 				}
 			}
 			br.close();
 		}
 		catch (Exception e) {
 			logger.log(Level.SEVERE, "Error reading extra permissions file "+extrapermissionsfile, e);
 			System.exit(-1);
 		}
 		return extrapermissions;
 	}
 	
 }

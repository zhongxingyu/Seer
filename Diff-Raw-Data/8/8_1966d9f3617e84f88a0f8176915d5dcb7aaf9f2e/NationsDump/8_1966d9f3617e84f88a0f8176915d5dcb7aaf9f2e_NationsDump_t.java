 /*
  * Copyright (c) 2013 Afforess
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.afforess.nsdump;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.lang.reflect.Field;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * Created with a stream of data that is gzipped region xml, and can be parsed into a database table
  * 
  * <p><b>regions table format</b><pre>
  * <span style="padding: 0 10px">&nbsp;</span>name                       - varchar
  * <span style="padding: 0 10px">&nbsp;</span>title                      - varchar
  * <span style="padding: 0 10px">&nbsp;</span>type                       - varchar
  * <span style="padding: 0 10px">&nbsp;</span>fullname                   - varchar
  * <span style="padding: 0 10px">&nbsp;</span>motto                      - clob
  * <span style="padding: 0 10px">&nbsp;</span>category                   - varchar
  * <span style="padding: 0 10px">&nbsp;</span>unstatus                   - int
  * <span style="padding: 0 10px">&nbsp;</span>civilrights                - varchar
  * <span style="padding: 0 10px">&nbsp;</span>economy                    - varchar
  * <span style="padding: 0 10px">&nbsp;</span>politicalfreedom           - varchar
  * <span style="padding: 0 10px">&nbsp;</span>region                     - varchar
  * <span style="padding: 0 10px">&nbsp;</span>population                 - int
  * <span style="padding: 0 10px">&nbsp;</span>tax                        - int
  * <span style="padding: 0 10px">&nbsp;</span>animal                     - varchar
  * <span style="padding: 0 10px">&nbsp;</span>currency                   - varchar
  * <span style="padding: 0 10px">&nbsp;</span>flag                       - varchar
  * <span style="padding: 0 10px">&nbsp;</span>majorindustry              - varchar
  * <span style="padding: 0 10px">&nbsp;</span>governmentpriority         - varchar
  * <span style="padding: 0 10px">&nbsp;</span>environment                - int
  * <span style="padding: 0 10px">&nbsp;</span>socialequality             - int
  * <span style="padding: 0 10px">&nbsp;</span>education                  - int
  * <span style="padding: 0 10px">&nbsp;</span>lawandorder                - int
  * <span style="padding: 0 10px">&nbsp;</span>administration             - int
  * <span style="padding: 0 10px">&nbsp;</span>welfare                    - int
  * <span style="padding: 0 10px">&nbsp;</span>spirituality               - int
  * <span style="padding: 0 10px">&nbsp;</span>defence                    - int
  * <span style="padding: 0 10px">&nbsp;</span>publictransport            - int
  * <span style="padding: 0 10px">&nbsp;</span>healthcare                 - int
  * <span style="padding: 0 10px">&nbsp;</span>commerce                   - int
  * <span style="padding: 0 10px">&nbsp;</span>founded                    - varchar
  * <span style="padding: 0 10px">&nbsp;</span>firstlogin                 - bigint
  * <span style="padding: 0 10px">&nbsp;</span>lastlogin                  - bigint
  * <span style="padding: 0 10px">&nbsp;</span>lastactivity               - varchar
  * <span style="padding: 0 10px">&nbsp;</span>influence                  - varchar
  * <span style="padding: 0 10px">&nbsp;</span>civilrightscore            - int
  * <span style="padding: 0 10px">&nbsp;</span>economyscore               - int
  * <span style="padding: 0 10px">&nbsp;</span>politicalfreedomscore      - int
  * <span style="padding: 0 10px">&nbsp;</span>publicsector               - int
  * <span style="padding: 0 10px">&nbsp;</span>leader                     - varchar
  * <span style="padding: 0 10px">&nbsp;</span>capital                    - varchar
  * <span style="padding: 0 10px">&nbsp;</span>religion                   - varchar
  * </pre><p>
  */
 public class NationsDump extends ArchiveDump {
 	private final static String NATIONS_DUMP_URL = "http://www.nationstates.net/pages/nations.xml.gz";
 	private final InputStream stream;
 	private boolean parsed;
	private final List<SAXParseException> exceptions = new ArrayList<SAXParseException>();
 	/**
 	 * Constructs a nation dump using the given file as the data source
 	 * @param file nation.xml.gz file
 	 * @throws FileNotFoundException 
 	 */
 	public NationsDump(File file) throws FileNotFoundException {
 		this(new FileInputStream(file));
 	}
 
 	/**
 	 * Opens a url connection to the ns nation dump and uses the latest dump as the data stream
 	 * @throws IOException 
 	 */
 	public NationsDump() throws IOException {
 		this(getNationsDump());
 	}
 
 	/**
 	 * Constructs a nation dump using the given inputstream as the data stream
 	 * @param stream of data in gzipped xml
 	 */
 	public NationsDump(InputStream stream) {
 		this.stream = stream;
 		this.setConnectionUrl("jdbc:h2:./ns-db");
 		this.setUsername("sa");
 		this.setPassword("sa");
 	}
 
 	/**
 	 * True if the nation data has been parsed already
 	 * 
 	 * @return parsed
 	 */
 	public boolean isParsed() {
 		return parsed;
 	}
 
 	/**
 	 * Gets any exceptions that occurred during the parsing
 	 * 
 	 * @return exceptions
 	 */
 	public List<SAXParseException> getExceptions() {
 		return Collections.unmodifiableList(exceptions);
 	}
 
 	/**
 	 * Parses the nation data into the database
 	 */
 	public void parse() {
 		Connection conn = null;
 		try {
 			conn = getDatabaseConnection();
 			
 			try {
 				conn.prepareStatement("DROP TABLE nations").execute();
 			} catch (SQLException ignore) {	}
 			conn.prepareStatement("CREATE TABLE nations (name varchar(50), title varchar(50), type varchar(255), fullname varchar(255), motto clob," +
 					"category varchar(255), unstatus varchar(15), civilrights varchar(25), economy varchar(25), politicalfreedom varchar(25), " +
 					"region varchar(50), population int, tax int, animal varchar(255), currency varchar(255), flag varchar(255), majorindustry varchar(50)," +
 					"governmentpriority varchar(50), environment int, socialequality int, education int, lawandorder int, administration int," +
 					"welfare int, spirituality int, defence int, publictransport int, healthcare int, commerce int, founded varchar(255), firstlogin bigint," +
					"lastlogin bigint, lastactivity varchar(255), influence varchar(50), civilrightscore int, economyscore int, politicalfreedomscore int," +
 					"publicsector int, leader varchar(50), capital varchar(50), religion varchar(50))").execute();
 
 			conn.prepareStatement("CREATE INDEX nation_name ON nations(name);").execute();
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser parser = factory.newSAXParser();
 			XMLReader reader = parser.getXMLReader();
 			reader.setErrorHandler(new SaxErrorHandler());
 			reader.setContentHandler(new NationsParser(conn));
 			reader.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
 			reader.parse(new InputSource(new BufferedInputStream(new GZIPInputStream(stream))));
 		} catch (SQLException e) {
 			throw new RuntimeException("SQL Exception", e);
 		} catch (SAXException e) {
 			throw new RuntimeException("Sax Parsing Exception", e);
 		} catch (IOException e) {
 			throw new RuntimeException("IOException", e);
 		} catch (ParserConfigurationException e) {
 			throw new RuntimeException("Parsing Exception", e);
 		} finally {
 			parsed = true;
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) { }
 			}
 		}
 	}
 
 	private class SaxErrorHandler implements ErrorHandler {
 		public void warning(SAXParseException exception) {
 			exceptions.add(exception);
 		}
 
 		public void error(SAXParseException exception) {
 			exceptions.add(exception);
 		}
 
 		public void fatalError(SAXParseException exception) {
 			exceptions.add(exception);
 		}
 	}
 
 	/**
 	 * Creates a new connection to the database
 	 * @return database connection
 	 * @throws SQLException
 	 */
 	public Connection getDatabaseConnection() throws SQLException {
 		return DriverManager.getConnection(this.getConnectionUrl(), this.getUsername(), this.getPassword());
 	}
 
 	private static InputStream getNationsDump() throws IOException {
 		URL url = new URL(NATIONS_DUMP_URL);
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 		conn.setRequestProperty("User-Agent", "ns-dumps api by Afforess (https://github.com/Afforess/ns-dumps)");
 		return conn.getInputStream();
 	}
 
 	private static class NationsParser extends DefaultHandler {
 		private final Connection conn;
 		//Element data
 		private final StringBuilder builder = new StringBuilder();
 		private boolean reset = true;
 
 		//Region Data
 		private String name;
 		private String type;
 		private String fullname;
 		private String motto;
 		private String category;
 		private String unstatus;
 		
 		//Freedoms
 		private String civilRights;
 		private String economy;
 		private String politicalFreedom;
 		
 		private String region;
 		private String population;
 		private String tax;
 		private String animal;
 		private String currency;
 		private String flag;
 		private String majorIndustry;
 		private String govtPriority;
 		
 		//Government
 		private String environment;
 		private String socialEquality;
 		private String education;
 		private String lawAndOrder;
 		private String administration;
 		private String welfare;
 		private String spirituality;
 		private String defence;
 		private String publicTransport;
 		private String healthCare;
 		private String commerce;
 		
 		private String founded;
 		private String firstLogin;
 		private String lastLogin;
 		private String lastActivity;
 		private String influence;
 		
 		//Freedom scores
 		private String civilRightsScore;
 		private String economyScore;
 		private String politicalFreedomScore;
 		
 		private String publicSector;
 		private String leader;
 		private String capital;
 		private String religion;
 		
 		//Whether we are parsing freedom text or freedom scores
 		private boolean freedomScores = false;
 		
 		
 		private final Field[] fields = getClass().getDeclaredFields();
 
 		public NationsParser(Connection conn) {
 			this.conn = conn;
 		}
 
 		@Override
 		public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
 			reset = true;
 			if (elementName.equalsIgnoreCase("freedomscores")) {
 				freedomScores = true;
 			}
 		}
 
 		@Override
 		public void endElement(String s, String s1, String element) throws SAXException {
 			reset = true;
 			if (element.equalsIgnoreCase("freedomscores")) {
 				freedomScores = false;
 			}
 			
 			if (element.equalsIgnoreCase("nation")) {
 				try {
 					PreparedStatement statement = conn.prepareStatement("INSERT INTO nations (name, title, type, fullname, motto," +
 					"category, unstatus, civilrights, economy, politicalfreedom, region, population, tax, animal, currency," +
 					" flag, majorindustry, governmentpriority, environment, socialequality, education, lawandorder, administration," +
 					" welfare, spirituality, defence, publictransport, healthcare, commerce, founded, firstlogin, lastlogin," +
					" lastactivity, influence, civilrightscore, economyscore, politicalfreedomscore, publicsector, leader, capital," +
 					" religion)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 					statement.setString(1, name.toLowerCase().replaceAll(" ", "_"));
 					statement.setString(2, name);
 					statement.setString(3, type);
 					statement.setString(4, fullname);
 					statement.setClob(5, new StringReader(motto));
 					statement.setString(6, category);
 					statement.setString(7, unstatus);
 					statement.setString(8, civilRights);
 					statement.setString(9, economy);
 					statement.setString(10, politicalFreedom);
 					statement.setString(11, region);
 					statement.setInt(12, Integer.parseInt(population));
 					statement.setInt(13, Integer.parseInt(tax));
 					statement.setString(14, animal);
 					statement.setString(15, currency);
 					statement.setString(16, flag);
 					statement.setString(17, majorIndustry);
 					statement.setString(18, govtPriority);
 					statement.setInt(19, Integer.parseInt(slc(environment)));
 					statement.setInt(20, Integer.parseInt(slc(socialEquality)));
 					statement.setInt(21, Integer.parseInt(slc(education)));
 					statement.setInt(22, Integer.parseInt(slc(lawAndOrder)));
 					statement.setInt(23, Integer.parseInt(slc(administration)));
 					statement.setInt(24, Integer.parseInt(slc(welfare)));
 					statement.setInt(25, Integer.parseInt(slc(spirituality)));
 					statement.setInt(26, Integer.parseInt(slc(defence)));
 					statement.setInt(27, Integer.parseInt(slc(publicTransport)));
 					statement.setInt(28, Integer.parseInt(slc(healthCare)));
 					statement.setInt(29, Integer.parseInt(slc(commerce)));
 					statement.setString(30, founded);
 					statement.setLong(31, Long.parseLong(firstLogin));
 					statement.setLong(32, Long.parseLong(lastLogin));
 					statement.setString(33, lastActivity);
 					statement.setString(34, influence);
 					statement.setInt(35, Integer.parseInt(civilRightsScore));
 					statement.setInt(36, Integer.parseInt(economyScore));
 					statement.setInt(37, Integer.parseInt(politicalFreedomScore));
 					statement.setInt(38, Integer.parseInt(slc(publicSector)));
 					statement.setString(39, leader);
 					statement.setString(40, capital);
 					statement.setString(41, religion);
 					statement.execute();
 				}  catch (NumberFormatException e) {
 					throw new RuntimeException(e);
 				} catch (SQLException e) {
 					throw new RuntimeException(e);
 				}
 			} else if (element.equalsIgnoreCase("civilrights")) {
 				if (freedomScores) {
 					civilRightsScore = builder.toString();
 				} else {
 					civilRights = builder.toString();
 				}
 			} else if (element.equalsIgnoreCase("economy")) {
 				if (freedomScores) {
 					economyScore = builder.toString();
 				} else {
 					economy = builder.toString();
 				}
 			} else if (element.equalsIgnoreCase("politicalfreedom")) {
 				if (freedomScores) {
 					politicalFreedomScore = builder.toString();
 				} else {
 					politicalFreedom = builder.toString();
 				}
 			} else {
 				for (Field f : fields) {
 					if (f.getType() == String.class) {
 						if (f.getName().equalsIgnoreCase(element)) {
 							try {
 								f.setAccessible(true);
 								f.set(this, builder.toString());
 							} catch (IllegalArgumentException e) {
 								throw new RuntimeException("Failed to set field: " + f.getName(), e);
 							} catch (IllegalAccessException e) {
 								throw new RuntimeException("Failed to access field: " + f.getName(), e);
 							}
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		@Override
 		public void characters(char[] ac, int i, int j) throws SAXException {
 			if (reset) {
 				builder.delete(0, builder.length());
 				builder.append(ac, i, j);
 				reset = false;
 			} else {
 				builder.append(ac, i, j);
 			}
 		}
 
 		/**
 		 * Strips the last character of the string (used for removing % signs).
 		 * 
 		 * slc = strip last char
 		 * 
 		 * @param str
 		 * @return string
 		 */
 		private String slc(String str) {
 			return str.substring(0, str.length() - 1);
 		}
 	}
 }

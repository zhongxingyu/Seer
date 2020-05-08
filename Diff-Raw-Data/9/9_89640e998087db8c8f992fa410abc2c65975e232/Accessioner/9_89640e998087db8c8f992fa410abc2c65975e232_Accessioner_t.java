 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.dbcp.ConnectionFactory;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDriver;
 import org.apache.commons.pool.ObjectPool;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.arrayexpress2.sampletab.validator.SampleTabValidator;
 
 public class Accessioner {
 
     private String hostname;
 
     private int port = 3306;
 
     private String database;
 
     private String username;
 
     private String password;
     
     private static boolean setup = false;
     private static ObjectPool connectionPool = new GenericObjectPool();
 
     private final SampleTabValidator validator = new SampleTabValidator();
     private final SampleTabSaferParser parser = new SampleTabSaferParser(validator);
 
     private Logger log = LoggerFactory.getLogger(getClass());
     
     private BasicDataSource ds;
 
     public Accessioner(String host, int port, String database, String username, String password)
             throws ClassNotFoundException, SQLException {
         // Setup the connection with the DB
         this.username = username;
         this.password = password;
         this.hostname = host;
         this.port = port;
         this.database = database;
         
         doSetup();
     }
     
     private String getAccession(Connection connect, String table, String userAccession, String submissionAccession) throws SQLException{
 
         String prefix;
         if (table.toLowerCase().equals("sample_assay")){
             prefix = "SAMEA";
         } else if (table.toLowerCase().equals("sample_groups")){
             prefix = "SAMEG";
         } else if (table.toLowerCase().equals("sample_reference")){
             prefix = "SAME";
         } else {
             throw new IllegalArgumentException("invalud table "+table);
         }
         
         PreparedStatement statement = null;
         ResultSet results = null;
         String accession = null;
         try {
             statement = connect.prepareStatement("SELECT accession FROM " + table
                     + " WHERE user_accession LIKE ? AND submission_accession LIKE ? LIMIT 1");
             statement.setString(1, userAccession);
             statement.setString(2, submissionAccession);
             results = statement.executeQuery();
             results.first();
             Integer accessionID = results.getInt(1);
             accession = prefix + accessionID;
         } finally {
             if (statement != null){
                 try {
                     statement.close();
                 } catch (SQLException e){
                     //do nothing
                 }
             }
             if (results != null){
                 try {
                     results.close();
                 } catch (SQLException e){
                     //do nothing
                 }
             }
         }
         
         return accession;
     }
 
     
     private void doSetup() throws ClassNotFoundException, SQLException{
         //this will only setup for the first instance
         //therefore do not try to mix and match different connections in the same VM
         if (setup){
             return;
         }
 
         String connectURI = "jdbc:mysql://" + hostname + ":" + port + "/" + database;
         connectURI = "jdbc:oracle:thin:@"+hostname+":"+port+":"+database;
         ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, username, password);
         PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
         Class.forName("com.mysql.jdbc.Driver");
         Class.forName("oracle.jdbc.driver.OracleDriver");
         Class.forName("org.apache.commons.dbcp.PoolingDriver");
         PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
         driver.registerPool("accessioner", connectionPool);
 
         //
         // Now we can just use the connect string "jdbc:apache:commons:dbcp:example"
         // to access our pool of Connections.
         //        
         
         setup = true;
     }
     public SampleData convert(String sampleTabFilename) throws IOException, ParseException, SQLException {
         return convert(new File(sampleTabFilename));
     }
 
     public SampleData convert(File sampleTabFile) throws IOException, ParseException, SQLException {
         return convert(parser.parse(sampleTabFile));
     }
 
     public SampleData convert(URL sampleTabURL) throws IOException, ParseException, SQLException {
         return convert(parser.parse(sampleTabURL));
     }
 
     public SampleData convert(InputStream dataIn) throws ParseException, SQLException {
         return convert(parser.parse(dataIn));
     }
 
     public SampleData convert(SampleData sampleIn) throws ParseException, SQLException {
         String table = null;
         String prefix = null;
         if (sampleIn.msi.submissionReferenceLayer == true) {
             prefix = "SAME";
             table = "SAMPLE_REFERENCE";
         } else if (sampleIn.msi.submissionReferenceLayer == false) {
             prefix = "SAMEA";
             table = "SAMPLE_ASSAY";
         } else {
             throw new ParseException("Must specify a Submission Reference Layer MSI attribute.");
         }
 
 
         Collection<SampleNode> samples = sampleIn.scd.getNodes(SampleNode.class);
 
         log.debug("got " + samples.size() + " samples.");
         String name;
         String submission = sampleIn.msi.submissionIdentifier;
         if (submission == null){
             throw new ParseException("Submission Identifier cannot be null");
         }
         submission = submission.trim();
         int accessionID;
         String accession;
         
         Connection connect = null;
         PreparedStatement statement = null;
         ResultSet results = null;
         
         try {
             connect = DriverManager.getConnection("jdbc:apache:commons:dbcp:accessioner");
             log.info("Starting accessioning");
             
             
             //first do one query to retrieve all that have already got accessions
             long start = System.currentTimeMillis();
             statement = connect.prepareStatement("SELECT USER_ACCESSION, ACCESSION FROM " + table
                     + " WHERE SUBMISSION_ACCESSION LIKE ? AND IS_DELETED = 0");
             statement.setString(1, submission);
             log.trace(statement.toString());
             results = statement.executeQuery();
             long end = System.currentTimeMillis();
             log.debug("Time elapsed = "+(end-start)+"ms");
             while (results.next()){
                 String samplename = results.getString(1).trim();
                 accessionID = results.getInt(2);
                 accession = prefix + accessionID;
                 SampleNode sample = sampleIn.scd.getNode(samplename, SampleNode.class);
                 log.trace(samplename+" : "+accession);
                 if (sample != null){
                     sample.setSampleAccession(accession);
                 } else {
                     log.warn("Unable to find SCD sample node "+samplename+" in submission "+submission);
                 }
             }
             //TODO try finally this
             statement.close();
             results.close();
             
 
             statement = connect.prepareStatement("SELECT USER_ACCESSION, ACCESSION FROM SAMPLE_GROUPS WHERE SUBMISSION_ACCESSION LIKE ? AND IS_DELETED = 0");
             statement.setString(1, submission);
             log.trace(statement.toString());
             results = statement.executeQuery();
             while (results.next()){
                 String groupname = results.getString(1).trim();
                 accessionID = results.getInt(2);
                 accession = prefix + accessionID;
                 GroupNode group = sampleIn.scd.getNode(groupname, GroupNode.class);
                 log.trace(groupname+" : "+accession);
                 if (group != null){
                     group.setGroupAccession(accession);
                 } else {
                     log.warn("Unable to find SCD group node "+groupname+" in submission "+submission);
                 }
             }
             //TODO try finally this
             statement.close();
             results.close();
             
             
             //now assign and retrieve accessions for samples that do not have them
             for (SampleNode sample : samples) {
                 if (sample.getSampleAccession() == null) {
                     name = sample.getNodeName().trim();
 
                     log.info("Assigning new accession for "+submission+" : "+name);
                     
                     //insert it if not exists
                     start = System.currentTimeMillis();
                     statement = connect
                             .prepareStatement("INSERT INTO "
                                     + table
                                     + " (USER_ACCESSION, SUBMISSION_ACCESSION, DATE_ASSIGNED, IS_DELETED) VALUES ( ? , ? , SYSDATE, 0 )");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     log.trace(statement.toString());
                     statement.executeUpdate();
                     statement.close();
                     end = System.currentTimeMillis();
                     log.info("Time elapsed = "+(end-start)+"ms");
 
                     statement = connect.prepareStatement("SELECT ACCESSION FROM " + table
                            + " WHERE USER_ACCESSION LIKE ? AND SUBMISSION_ACCESSION LIKE ?");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     log.trace(statement.toString());
                     results = statement.executeQuery();
                    results.next();
                     accessionID = results.getInt(1);
                     accession = prefix + accessionID;
                     statement.close();
                     results.close();
 
                     log.debug("Assigning " + accession + " to " + name);
                     sample.setSampleAccession(accession);
                 }
             }
 
             Collection<GroupNode> groups = sampleIn.scd.getNodes(GroupNode.class);
 
             log.debug("got " + groups.size() + " groups.");
             for (GroupNode group : groups) {
                 if (group.getGroupAccession() == null) {
                     name = group.getNodeName();
                     statement = connect
                             .prepareStatement("INSERT INTO SAMPLE_GROUPS ( USER_ACCESSION , SUBMISSION_ACCESSION , DATE_ASSIGNED , IS_DELETED ) VALUES ( ? ,  ? , SYSDATE, 0 )");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     log.info(name);
                     log.info(submission);
                     statement.executeUpdate();
                     statement.close();
 
                     statement = connect
                             .prepareStatement("SELECT ACCESSION FROM SAMPLE_GROUPS WHERE USER_ACCESSION = ? AND SUBMISSION_ACCESSION = ?");
                     statement.setString(1, name);
                     statement.setString(2, submission);
                     log.trace(statement.toString());
                     results = statement.executeQuery();
                    results.next();
                     accessionID = results.getInt(1);
                     accession = "SAMEG" + accessionID;
                     statement.close();
                     results.close();
 
                     log.debug("Assigning " + accession + " to " + name);
                     group.setGroupAccession(accession);
                 }
             }
         } finally {
             if (results != null){
                 try {
                     results.close();
                 } catch (Exception e) {
                     //do nothing
                 }
             }
             if (statement != null) {
                 try {
                     statement.close();
                 } catch (Exception e) {
                     //do nothing
                 }
             }
             if (connect != null) {
                 try {
                     connect.close();
                 } catch (Exception e) {
                     //do nothing
                 }
             }
         }
 
         return sampleIn;
     }
 
     public void convert(SampleData sampleIn, Writer writer) throws IOException, ParseException, SQLException {
         log.info("recieved magetab, preparing to convert");
         SampleData sampleOut = convert(sampleIn);
         log.info("sampletab converted, preparing to output");
         SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
         log.info("created SampleTabWriter");
         sampletabwriter.write(sampleOut);
         sampletabwriter.close();
 
     }
 
     public void convert(File sampletabFile, Writer writer) throws IOException, ParseException, SQLException {
         log.info("preparing to load SampleData");
         SampleTabSaferParser stparser = new SampleTabSaferParser();
         log.info("created SampleTabParser<SampleData>, beginning parse");
         SampleData st = stparser.parse(sampletabFile);
         convert(st, writer);
     }
 
     public void convert(File inputFile, String outputFilename) throws IOException, ParseException, SQLException {
         convert(inputFile, new File(outputFilename));
     }
 
     public void convert(File inputFile, File outputFile) throws IOException, ParseException, SQLException {
         convert(inputFile, new FileWriter(outputFile));
     }
 
     public void convert(String inputFilename, Writer writer) throws IOException, ParseException, SQLException {
         convert(new File(inputFilename), writer);
     }
 
     public void convert(String inputFilename, File outputFile) throws IOException, ParseException, SQLException {
         convert(inputFilename, new FileWriter(outputFile));
     }
 
     public void convert(String inputFilename, String outputFilename) throws IOException, ParseException, SQLException {
         convert(inputFilename, new File(outputFilename));
     }
 }

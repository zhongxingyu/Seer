 package org.jsc.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.jsc.client.ClientConstants;
 import org.jsc.client.LoginSession;
 import org.jsc.client.MembershipRequests;
 import org.jsc.client.MembershipResult;
 import org.jsc.client.MembershipType;
 import org.jsc.client.Person;
 import org.jsc.client.RegistrationResults;
 import org.jsc.client.RosterEntry;
 import org.jsc.client.SessionSkatingClass;
 import org.jsc.client.SkaterRegistrationService;
 import org.mindrot.BCrypt;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * An implementation of a registration service that handles the creation of 
  * new accounts as well as registration of people into classes.
  * @author Matt Jones
  */
 public class SkaterRegistrationServiceImpl extends RemoteServiceServlet
         implements SkaterRegistrationService {
 
     private static final String JDBC_URL = ServerConstants
             .getString("JDBC_URL");
     private static final String JDBC_USER = ServerConstants
             .getString("JDBC_USER");
     private static final String JDBC_PASS = ServerConstants
             .getString("JDBC_PASS");
     private static final String JDBC_DRIVER = ServerConstants
             .getString("JDBC_DRIVER");
     private static final String ROSTER_QUERY = "SELECT rosterid, classid, pid, levelPassed, paymentid, payment_amount, paypal_status, date_updated, surname, givenname, section, maxlevel FROM rosterpeople";
     private static final int MAX_SESSION_INTERVAL = 60 * 30;
     private static final int SAVE = 1;
     private static final int ADD = 2;
     private static final int DELETE = 3;
 
     private static Random random = new Random();
 
     /**
      * Create a new person entry in the backing relational database, or update
      * fields on an existing entry.  If the 'pid' field of the person is empty
      * or 0, a new entry is created.  Otherwise, the entry with the pid is
      * updated.
      * @param person the Person to be created or updated in the database
      * @return the Person that was created or updated, or null on error
      */
     public Person createAccount(LoginSession loginSession, Person person) {
 
         long pid = 0;
 
         StringBuffer sql = new StringBuffer();
         Connection con = getConnection();
         PreparedStatement pstmt = null;
         if (person.getPid() == 0) {
             // Creating a new account, so no authentication needed
 
             // Look up the pid to be used for this insert
             long newPid = 0;
             StringBuffer idsql = new StringBuffer();
             idsql.append("SELECT NEXTVAL(\'\"person_id_seq\"\')");
             System.out.println(idsql.toString());
             Statement stmt;
             try {
                 stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(idsql.toString());
                 if (rs.next()) {
                     // This is the next id in the payment id sequence
                     newPid = rs.getLong(1);
                 }
                 stmt.close();
             } catch (SQLException e) {
                 System.err.println("SQLException: " + e.getMessage());
                 return null;
             }
 
             // Create the SQL INSERT statement
             sql.append("insert into people");
             sql.append(" (pid, surname, givenname, middlename, email, birthdate, home_phone, cell_phone, work_phone,"
                     + "street1, street2, city, state, zipcode, parentfirstname, parentsurname, parentemail, username, password) ");
             sql.append("values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
             System.out.println(sql.toString());
             try {
                 pstmt = con.prepareStatement(sql.toString());
                 pstmt.setLong(1, newPid);
                 pstmt.setString(2, person.getLname());
                 pstmt.setString(3, person.getFname());
                 pstmt.setString(4, person.getMname());
                 pstmt.setString(5, person.getEmail());
                 Date bday = parseDate(person.getBday());
                 pstmt.setDate(6, new java.sql.Date(bday.getTime()));
                 pstmt.setString(7, person.getHomephone());
                 pstmt.setString(8, person.getCellphone());
                 pstmt.setString(9, person.getWorkphone());
                 pstmt.setString(10, person.getStreet1());
                 pstmt.setString(11, person.getStreet2());
                 pstmt.setString(12, person.getCity());
                 pstmt.setString(13, person.getState());
                 pstmt.setString(14, person.getZip());
                 pstmt.setString(15, person.getParentFirstname());
                 pstmt.setString(16, person.getParentLastname());
                 pstmt.setString(17, person.getParentEmail());
                 pstmt.setString(18, person.getUsername());
                 // Hash the password before insertion into the database
                 String hashed = BCrypt.hashpw(person.getNewPassword(),
                         BCrypt.gensalt());
                 pstmt.setString(19, hashed);
                 System.out.println(pstmt.toString());
                 pstmt.executeUpdate();
                 pstmt.close();
             } catch (SQLException e) {
                 System.out.println("SQLException: " + e.getMessage());
                 return null;
             }
 
         } else {
             // Verify that the session is valid before allowing an update
             boolean isAuthentic = isSessionValid(loginSession);
             if (!isAuthentic) {
                 return null;
             }
             sql.append("update people set ");
             sql.append("surname=?, givenname=?, middlename=?, email=?, birthdate=?, home_phone=?, ");
             sql.append("cell_phone=?, work_phone=?, street1=?, street2=?, ");
             sql.append("city=?, state=?, zipcode=?, parentfirstname=?, ");
             sql.append("parentsurname=?, parentemail=?, username=? ");
             if (person.getNewPassword() != null
                     && person.getNewPassword().length() > 0) {
                 sql.append(",password=? ");
             }
             sql.append(" where pid=?;");
             System.out.println(sql.toString());
 
             try {
                 pstmt = con.prepareStatement(sql.toString());
                 pstmt.setString(1, person.getLname());
                 pstmt.setString(2, person.getFname());
                 pstmt.setString(3, person.getMname());
                 pstmt.setString(4, person.getEmail());
 //                SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
 //                Date bday = df.parse(person.getBday());
                 Date bday = parseDate(person.getBday());
                 pstmt.setDate(5, new java.sql.Date(bday.getTime()));
                 pstmt.setString(6, person.getHomephone());
                 pstmt.setString(7, person.getCellphone());
                 pstmt.setString(8, person.getWorkphone());
                 pstmt.setString(9, person.getStreet1());
                 pstmt.setString(10, person.getStreet2());
                 pstmt.setString(11, person.getCity());
                 pstmt.setString(12, person.getState());
                 pstmt.setString(13, person.getZip());
                 pstmt.setString(14, person.getParentFirstname());
                 pstmt.setString(15, person.getParentLastname());
                 pstmt.setString(16, person.getParentEmail());
 
                 if (person.getNewUsername() != null
                         && person.getNewUsername().length() > 0
                         && !person.getUsername()
                                 .equals(person.getNewUsername())) {
                     pstmt.setString(17, person.getNewUsername());
                 } else {
                     pstmt.setString(17, person.getUsername());
                 }
                 int nextParamNumber = 18;
                 if (person.getNewPassword() != null
                         && person.getNewPassword().length() > 0) {
                     // Hash the password before insertion into the database
                     String hashed = BCrypt.hashpw(person.getNewPassword(),
                             BCrypt.gensalt());
                     pstmt.setString(nextParamNumber, hashed);
                     nextParamNumber++;
                 }
                 pstmt.setLong(nextParamNumber, person.getPid());
                 System.out.println(pstmt.toString());
                 pstmt.executeUpdate();
                 pstmt.close();
 
             } catch (SQLException e) {
                 System.out.println("SQLException: " + e.getMessage());
                 return null;
             }
         }
         
         try {
             if (person.getPid() == 0) {
                 // This is an INSERT, so look up the new PID for the new record
                 PreparedStatement stmt = con
                         .prepareStatement("SELECT max(pid) from people where email LIKE ?");
                 stmt.setString(1, person.getEmail());
                 ResultSet rs = stmt.executeQuery();
                 if (rs.next()) {
                     pid = rs.getInt(1);
                 } else {
                     pid = 0;
                 }
                 stmt.close();
             } else {
                 // This is an UPDATE, so use the passed in PID
                 pid = person.getPid();
             }
             con.close();
             
         } catch (SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
 
         Person newPerson = lookupPerson(pid);
         return newPerson;
     }
 
     private Date parseDate(String inputDate) {
         Date date = null;
         boolean success = false;
         String[] formats = {"MM-dd-yyyy", "MM/dd/yyyy"};
         for (String format : formats) {
             System.out.println("Trying to parse " + inputDate + " using format: " + format);
             SimpleDateFormat df = new SimpleDateFormat(format);
             try {
                 date = df.parse(inputDate);
                 // Check if we get a reasonable year value -- if not, we likely 
                 // are misinterpreting the year as a month in one of our earlier 
                 // formats, so move on to a new format to be tried
                 if ((date.getYear() > 1995) && (date.getYear() <= Calendar.getInstance().getTime().getYear())) {
                     success = true;
                     break;    
                 } else {
                     System.out.println("Got year: " + date.getYear());
                 }
             } catch (ParseException e) {
                 success = false;
                 System.out.println("Failed parsing " + inputDate + " using format: " + format);
             }    
         }
         
         return date;
     }
     
     /**
      * Check if the user is in the database, and if the given password matches
      * @param username the username of the person who is signing in
      * @param password the password of the person who is signing in
      * @return the LoginSession containing the person that was authenticated if valid, otherwise null
      */
     public LoginSession authenticate(String username, String password) {
         Person person = null;
         LoginSession loginSession = null;
 
         if (username != null && password != null) {
             HttpServletRequest request = this.getThreadLocalRequest();
             HttpSession session = request.getSession();
             session.setMaxInactiveInterval(MAX_SESSION_INTERVAL);
             System.out.println("Servlet got session with id: "
                     + session.getId());
             String sessionId = session.getId();
 
             int pid = checkPassword(username, password);
             if (pid > 0) {
                 person = lookupPerson(pid);
                 loginSession = new LoginSession();
                 loginSession.setPerson(person);
                 loginSession.setSessionId(sessionId);
                 loginSession.setAuthenticated(true);
             }
         }
         return loginSession;
     }
 
     /**
      * Invalidate the session, forcing a new authentication call from the client.
      */
     public boolean logout() {
         HttpServletRequest request = this.getThreadLocalRequest();
         HttpSession session = request.getSession();
         session.invalidate();
         return true;
     }
 
     /**
      * Given a username string, look it up in the database to find the associated
      * person and their email, change their password to a new random value, and
      * email the new password to the email account on file for them.
      */
     public boolean resetPassword(String username) {
         boolean successFlag = false;
 
         if (username != null) {
 
             // Look up the username in the DB to find the email address
             AccountInfo acctInfo = lookupAccountByUsername(username);
             if (acctInfo != null) {
                 successFlag = true;
                 System.out.println("Found email: " + acctInfo.email);
             }
 
             String newPassword = "";
             if (successFlag) {
                 // Reset the password
                 long r1 = random.nextLong();
                 newPassword = Long.toHexString(r1).substring(0, 8);
                 successFlag = updatePassword(acctInfo.pid, newPassword);
             }
 
             if (successFlag) {
                 // Email the new password to the user
                 MailManager manager = new MailManager();
                 String subject = "Password reset completed";
                 String body = "Your password has been reset. Your new password is: "
                         + newPassword;
                 String sender = ServerConstants.getString("SMTP_USER");
                 manager.sendMessage(subject, body, acctInfo.email, sender);
             }
         }
 
         return successFlag;
     }
 
     /**
      * Look up all of the account usernames associated with a given email address,
      * and then email those usernames to the email address, assuming it matches
      * what is in the database.
      */
     public boolean findUsername(String email) {
         boolean successFlag = false;
         Person person = null;
         LoginSession loginSession = null;
 
         if (email != null) {
 
             // Look up the email in the DB to find the usernames
             AccountInfo acctInfo = lookupAccountByEmail(email);
 
             // Email the list of usernames to the user's registered email
             MailManager manager = new MailManager();
             String subject = "JSC username information";
             String sender = ServerConstants.getString("SMTP_USER");
             StringBuffer body = new StringBuffer(
                     "You have JSC accounts with the following registered usernames:\n ");
             for (String username : acctInfo.usernames) {
                 body.append("    ");
                 body.append(username);
                 body.append("\n");
             }
             body.append("\nReturn to the JSC registration site to log in or reset your password if you have forgotten it.\n");
             body.append("    ");
             body.append(ClientConstants.getString("CLIENT_PAYPAL_RETURN_URL"));
             manager.sendMessage(subject, body.toString(), acctInfo.email,
                     sender);
             body.append("\n");
 
             successFlag = true;
         }
 
         return successFlag;
     }
 
     /**
      * Look up the person in the database based on their identifier. If found,
      * return the Person object.
      * @param pid the identifier of the person to look up
      * @return the Person object associated with this pid
      */
     public Person getPerson(long pid) {
         // TODO: be sure to check for proper credentials here
 
         Person person = null;
         person = lookupPerson(pid);
         return person;
     }
 
     /**
      * Look up the list of current classes that are available in the database,
      * and return them as an ArrayList of SessionSkatingClass objects.
      * @param person the person used for authentication credentials
      * @return the ArrayList of SessionSkatingClass instances
      */
     public ArrayList<SessionSkatingClass> getSessionClassList(
             LoginSession loginSession, Person person) {
         ArrayList<SessionSkatingClass> classList = new ArrayList<SessionSkatingClass>();
 
         // Check authentication credentials
         boolean isAuthentic = isSessionValid(loginSession);
         if (!isAuthentic) {
             return null;
         }
 
         // Query the database to get the list of classes
         StringBuffer sql = new StringBuffer();
         sql.append("select sid, sessionname, season, startdate, enddate, ");
         sql.append("classid, classtype, day, timeslot, instructorid, cost,");
         sql.append("otherinstructors, surname, givenname, activesession, discountDate from sessionclasses ");
         sql.append("order by season, sessionname, classid");
         System.out.println(sql.toString());
 
         try {
             Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString());
             while (rs.next()) {
                 SessionSkatingClass sc = new SessionSkatingClass();
                 sc.setSid(rs.getLong(1));
                 sc.setSessionNum(rs.getInt(2));
                 sc.setSeason(rs.getString(3));
                 sc.setStartDate(rs.getString(4));
                 sc.setEndDate(rs.getString(5));
                 sc.setClassId(rs.getLong(6));
                 sc.setClassType(rs.getString(7));
                 sc.setDay(rs.getString(8));
                 sc.setTimeslot(rs.getString(9));
                 sc.setInstructorId(rs.getLong(10));
                 sc.setCost(rs.getFloat(11));
                 sc.setOtherinstructors(rs.getString(12));
                 sc.setInstructorSurName(rs.getString(13));
                 sc.setInstructorGivenName(rs.getString(14));
                 sc.setActiveSession(rs.getBoolean(15));
                 sc.setDiscountDate(rs.getString(16));
                 classList.add(sc);
                 System.out.println("Set class cost to: " + sc.getCost());
             }
             stmt.close();
             con.close();
 
         } catch (SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
 
         return classList;
     }
 
     /**
      * Register a person for one or more classes by creating a new entry in the roster
      * table in the backing relational database for each RosterEntry that is passed in
      * the newEntryList array.  This method takes on input an array of
      * skeleton RosterEntry instances that each contains the classid and pid to be registered,
      * and then creates and returns a new RosterEntry from the database after 
      * the rosterid has been created for each one.
      * @param person to be used to authenticate the connection
      * @param newEntryList list of RosterEntry objects containing the details of the class and person to be registered
      * @param createMembership boolean, set to true if the membership for the user should be created for this season
      * @param memInfo fields describing the membership type to be created and list of members to be added
      * @return an array of the completed RosterEntry instances from the database
      */
     public RegistrationResults register(LoginSession loginSession,
             Person person, ArrayList<RosterEntry> newEntryList,
             boolean createMembership, MembershipRequests memInfo) {
 
         RegistrationResults results = new RegistrationResults();
 
         ArrayList<RosterEntry> entriesCreated = new ArrayList<RosterEntry>();
         ArrayList<Long> entriesFailed = new ArrayList<Long>();
 
         // Check credentials
         if (person != null && newEntryList != null) {
             // Verify that the user is valid before allowing an insert
             boolean isAuthentic = isSessionValid(loginSession);
             if (!isAuthentic) {
                 return null;
             }
         } else {
             return null;
         }
 
         long paymentId = createPaymentEntry();
 
         // Loop through each of the entries we've been passed, and for each one
         // insert it in the database, look up its rosterid to create a new
         // RosterEntry for it, add this to the list of created roster entries
         for (RosterEntry entry : newEntryList) {
 
             RosterEntry newEntry = null;
 
             // Create the SQL INSERT statement
             String sql = "insert into roster (classid, pid, paymentId, payment_amount) values (?, ?, ?, ?)";
             System.out.println(sql);
 
             // Execute the INSERT to create the new roster table entry
             try {
                 Connection con = getConnection();
                 PreparedStatement pstmt = con.prepareStatement(sql);
                 pstmt.setLong(1, entry.getClassid());
                 pstmt.setLong(2, entry.getPid());
                 pstmt.setLong(3, paymentId);
                 pstmt.setDouble(4, entry.getPayment_amount());
                 pstmt.executeUpdate();
                 pstmt.close();
 
                 // query the roster table to find the rosterid that was created
                 Statement stmt = con.createStatement();
                 StringBuffer rsql = new StringBuffer();
                 rsql.append(ROSTER_QUERY);
                 rsql.append(" WHERE classid = '").append(entry.getClassid())
                         .append("'");
                 rsql.append(" AND ");
                 rsql.append("pid = '").append(entry.getPid()).append("'");
                 System.out.println(rsql.toString());
 
                 ResultSet rs = stmt.executeQuery(rsql.toString());
                 if (rs.next()) {
                     newEntry = createRosterEntry(rs);
                     entriesCreated.add(newEntry);
                 } else {
                     entriesFailed.add(new Long(entry.getClassid()));
                 }
                 stmt.close();
                 con.close();
 
             } catch (SQLException ex) {
                 System.err.println("SQLException: " + ex.getMessage());
                 entriesFailed.add(new Long(entry.getClassid()));
             }
         }
 
         results.setEntriesCreated(entriesCreated);
         results.setEntriesNotCreated(entriesFailed);
 
         return results;
     }
 
     /**
      * Create one or more memberships in the membership table by iterating across
      * the requests found in the MembershipInfo parameter.
      * @param person to be used to authenticate the connection
      * @param memInfo fields describing the membership type to be created and list of members to be added
      * @return an array of the completed RosterEntry instances from the database
      */
     public ArrayList<MembershipResult> createMemberships(LoginSession loginSession,
             Person person, MembershipRequests memInfo) {
         
         // Check credentials
         if (person != null) {
             // Verify that the user is valid before allowing an insert
             boolean isAuthentic = isSessionValid(loginSession);
             if (!isAuthentic) {
                 return null;
             }
         } else {
             return null;
         }
 
         ArrayList<MembershipResult> results = new ArrayList<MembershipResult>();
         
         long paymentId = createPaymentEntry();
 
         // Check if one or more membership entries should be created, and do so
         if (memInfo.size() > 0) {
             // Create the SQL INSERT statement
             String sql = "insert into membership (pid, paymentid, season, payment_amount, membertype) VALUES (?, ?, ?, ?, ?);";
            String msql = "select mid, pid, paymentid, season, paypal_status, membertype from memberstatus where pid = ? AND season LIKE ?";
 
             // Execute INSERT to create the new membership table entries
             try {
                 String season = SessionSkatingClass.calculateSeason();
                 
                 Connection con = getConnection();
                 PreparedStatement pstmt1 = con.prepareStatement(sql);
                 PreparedStatement pstmt2 = con.prepareStatement(msql);
                 
                 for (Entry<Long, ArrayList<MembershipType>> entry : memInfo.entrySet() ) {
                     
                     ArrayList<MembershipType> mtList = entry.getValue();
                     
                     for (MembershipType mt : mtList) {   
                         MembershipResult mr = new MembershipResult();
                         mr.setMembershipAttempted(true);
                         Long pid = entry.getKey();
 
                         pstmt1.setLong(1, pid);
                         pstmt1.setLong(2, paymentId);
                         pstmt1.setString(3, season);
                         pstmt1.setDouble(4, mt.getCost());
                         pstmt1.setString(5, mt.getMembershipType());
                         pstmt1.executeUpdate();
 
                         // Now look up the membershipId that was generated
                         System.out.println(msql.toString());
                         pstmt2 = con.prepareStatement(msql);
                         pstmt2.setLong(1, person.getPid());
                         pstmt2.setString(2, season);
                         ResultSet rs = pstmt2.executeQuery();
                         while (rs.next()) {
                             // if we found a matching record, the record was created
                             mr.setMembershipId(rs.getLong(1));
                             mr.setMembershipCreated(true);
                             mr.setPaymentId(paymentId);
                             mr.setMembershipStatus(rs.getString(5));
                             mr.setMembershipType(rs.getString(6));
                             results.add(mr);
                         }
                     }
                 }
                 pstmt1.close();
                 pstmt2.close();
                 con.close();
 
             } catch (SQLException ex) {
                 System.err.println("SQLException: " + ex.getMessage());
                 ex.printStackTrace(System.err);
             }
         }
         return results;
     }
 
     /** 
      * Create an entry in the payments table to represent the transaction,
      * setting the paypal_status field to incomplete. This field is later
      * updated using the paypal IPN service when the transaction completes
      */
     private long createPaymentEntry() {
         long paymentId = 0;
 
         Connection con = getConnection();
 
         try {
             // Look up the paymentId to be used for this insert
             StringBuffer idsql = new StringBuffer();
             idsql.append("SELECT NEXTVAL(\'\"payment_id_seq\"\')");
             System.out.println(idsql.toString());
             Statement stmt = con.createStatement();
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(idsql.toString());
             if (rs.next()) {
                 // This is the next id in the payment id sequence
                 paymentId = rs.getLong(1);
             }
             stmt.close();
 
             // Execute the INSERT to create the new payment table entry
             StringBuffer psql = new StringBuffer();
             psql.append("insert into payment (paymentId, paypal_status) VALUES ("
                     + paymentId + ", 'Pending')");
             System.out.println(psql.toString());
 
             stmt = con.createStatement();
             stmt.executeUpdate(psql.toString());
             stmt.close();
             con.close();
         } catch (SQLException e) {
             System.err.println("SQLException: " + e.getMessage());
         }
         
         return paymentId;
     }
 
     /**
      * Cancel the registration entries and membership entries associated with a
      * payment invoice.  The method checks that the person removing the entry is
      * the person who submitted it (or has role of COACH or ADMIN), 
      * and that it is in Pending status.
      */
     public boolean cancelInvoice(LoginSession loginSession, long paymentid) {
         // Check authentication credentials
         if (!isSessionValid(loginSession)) {
             return false;
         }
 
         // Look up the paymentid and see if the pid matches, or if the login 
         // person is authorized because they are a coach or administrator
         boolean isAuthorized = false;
         try {
             // Now check if the person logged in matches the registrant for the record
             // and that the payment is actually pending
             boolean isPending = false;
             StringBuffer invoiceQuery = new StringBuffer();
             invoiceQuery
                     .append("select py.paymentid, py.paypal_status, r.rosterid, r.pid "
                             + "from payment py, roster r "
                             + "where py.paymentid = r.paymentid "
                             + "and py.paypal_status = 'Pending' "
                             + "and py.paymentid = ");
             invoiceQuery.append(paymentid);
             System.out.println(invoiceQuery.toString());
             Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(invoiceQuery.toString());
             if (rs.next()) {
                 isPending = true;
                 long pid = rs.getLong(4);
                 // Check if the person who is logged in owns the invoice records
                 if (loginSession.getPerson().getPid() == pid) {
                     isAuthorized = true;
                 }
             }
             stmt.close();
             System.out.println("cancelAuth: " + isPending + " " + isAuthorized);
             con.close();
 
             // if not yet authorized, but the record is pending, see if the
             // loginSession represents a coach or admin, and if so authorize
             if (!isAuthorized && isPending) {
                 long role = getRole(loginSession.getPerson().getPid());
                 if (role >= Person.COACH) {
                     isAuthorized = true;
                     System.out.println("cancelAuth role is: " + role);
                 }
             }
 
         } catch (SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
 
         if (isAuthorized) {
 
             // Delete the invoice, cascading to the membership and roster tables
             Connection con = getConnection();
             try {
                 con.setAutoCommit(false);
 
                 String tables[] = { "membership", "roster", "payment" };
                 for (String table : tables) {
                     StringBuffer sql = new StringBuffer();
                     sql.append("DELETE from ");
                     sql.append(table);
                     sql.append(" where paymentid = ");
                     sql.append(paymentid);
                     System.out.println(sql.toString());
                     Statement stmt = con.createStatement();
                     int rowcount = stmt.executeUpdate(sql.toString());
                     System.out.println("Deleted " + rowcount + " rows from "
                             + table + ".");
                     stmt.close();
                 }
                 con.commit();
                 con.close();
 
             } catch (SQLException ex) {
                 try {
                     con.rollback();
                     con.close();
                 } catch (SQLException e) {
                     System.out
                             .println("Unable to rollback during invoice deletion."
                                     + e.getMessage());
                 }
                 System.err.println("SQLException: " + ex.getMessage());
                 return false;
             }
         }
 
         // Return true if the entry was deleted successfully
         return isAuthorized;
     }
 
     /**
      * Update the roster table with new section and levelpassed information.
      * The method checks that the person removing the entry is
      * a COACH or ADMIN role.
      */
     public boolean saveRoster(LoginSession loginSession, long rosterid,
             String newLevel, String newSection, String newClassId) {
         // Check authentication credentials
         if (!isSessionValid(loginSession)) {
             return false;
         }
 
         // Check if person is authorized because they are a coach or administrator
         boolean isAuthorized = false;
         long role = getRole(loginSession.getPerson().getPid());
         if (role >= Person.COACH) {
             isAuthorized = true;
             System.out.println("cancelAuth role is: " + role);
         }
 
         if (isAuthorized) {
             Connection con = getConnection();
             try {
                 con.setAutoCommit(false);
 
                 // Save the new information to the roster table
                 StringBuffer sql = new StringBuffer();
                 sql.append("UPDATE roster ");
                 sql.append("SET");
                 sql.append(" levelpassed = '").append(newLevel).append("'");
                 sql.append(", section = '").append(newSection).append("'");
                 long classid = new Long(newClassId).longValue();
                 if (classid > 0) {
                     sql.append(", classid = ").append(classid);
                 }
                 sql.append(" where rosterid = ");
                 sql.append(rosterid);
                 System.out.println(sql.toString());
                 Statement stmt = con.createStatement();
                 int rowcount = stmt.executeUpdate(sql.toString());
                 System.out.println("Updated " + rowcount + " rows in roster.");
                 stmt.close();
 
                 // Look up the pid from the rosterid
                 sql = new StringBuffer();
                 sql.append("SELECT pid from roster WHERE rosterid = ");
                 sql.append(rosterid);
                 System.out.println(sql.toString());
                 stmt = con.createStatement();
                 long pid = -1;
                 ResultSet rs = stmt.executeQuery(sql.toString());
                 if (rs.next()) {
                     pid = rs.getLong(1);
                 }
                 stmt.close();
 
                 // Look up the highest level for this person now
                 sql = new StringBuffer();
                 sql.append("SELECT levelcode from peoplelevel WHERE pid = ");
                 sql.append(pid);
                 System.out.println(sql.toString());
                 stmt = con.createStatement();
                 String maxLevel = "0";
                 rs = stmt.executeQuery(sql.toString());
                 if (rs.next()) {
                     maxLevel = rs.getString(1);
                 }
                 stmt.close();
 
                 // Update the people table with this highest level value
                 sql = new StringBuffer();
                 sql.append("UPDATE people ");
                 sql.append("SET");
                 sql.append(" maxlevel = '").append(maxLevel).append("'");
                 sql.append(" where pid = ");
                 sql.append(pid);
                 System.out.println(sql.toString());
                 stmt = con.createStatement();
                 rowcount = stmt.executeUpdate(sql.toString());
                 System.out.println("Updated " + rowcount + " rows in people.");
                 stmt.close();
 
                 con.commit();
                 con.close();
 
             } catch (SQLException ex) {
                 try {
                     con.rollback();
                     con.close();
                 } catch (SQLException e) {
                     System.out.println("Unable to rollback during roster save."
                             + e.getMessage());
                     return false;
                 }
                 System.err.println("SQLException: " + ex.getMessage());
                 return false;
             }
         }
 
         return isAuthorized;
     }
 
     /**
      * Look up the roster of classes for which this student has registered and
      * return it as an ArrayList.
      * @param person the person for whom the roster is compiled
      * @return an ArrayList of RosterEntry objects 
      */
     public ArrayList<RosterEntry> getStudentRoster(LoginSession loginSession,
             Person person) {
         // Create the query string to find the roster membership
         StringBuffer sql = new StringBuffer();
         sql.append(ROSTER_QUERY);
         sql.append(" WHERE pid = ").append(person.getPid());
         System.out.println(sql.toString());
 
         return getRoster(loginSession, sql.toString());
     }
 
     /**
      * Look up the roster of students enrolled in a class and
      * return it as an ArrayList.
      * @param loginSession the session used to authenticate
      * @param classId the identifier for the class reqeusted
      * @return an ArrayList of RosterEntry objects 
      */
     public ArrayList<RosterEntry> getClassRoster(LoginSession loginSession,
             long classId) {
         // Create the query string to find the roster membership
         StringBuffer sql = new StringBuffer();
         sql.append(ROSTER_QUERY);
         sql.append(" WHERE classid = ").append(classId);
         System.out.println(sql.toString());
 
         return getRoster(loginSession, sql.toString());
     }
 
     /**
      * Duplicate all of the classes that are present for one session and assign
      * them to a new session, thereby allowing us to quickly create a new set of
      * classes for the whole session.
      * @param loginSession the session used to authenticate
      * @param oldSeason the season of the session from which we will copy
      * @param oldSession the sessionName of the session from which we will copy
      * @param newSeason the target season for copying classes
      * @param newSession the target session for copying classes
      */
     public boolean duplicateSessionClassList(LoginSession loginSession,
             String oldSeason, String oldSession, String newSeason,
             String newSession) {
         // Check authentication credentials
         if (!isSessionValid(loginSession)) {
             return false;
         }
 
         // Basic validation check on input
         if (oldSeason != null && oldSeason.length() > 0 && oldSession != null
                 && oldSession.length() > 0 && newSeason != null
                 && newSeason.length() > 0 && newSession != null
                 && newSession.length() > 0) {
         } else {
             return false;
         }
 
         // Check if person is authorized because they are an administrator
         boolean isAuthorized = false;
         long role = getRole(loginSession.getPerson().getPid());
         if (role >= Person.ADMIN) {
             isAuthorized = true;
         }
 
         if (isAuthorized) {
             Connection con = getConnection();
             try {
                 con.setAutoCommit(false);
 
                 // Get the sid for the new session
                 StringBuffer sql = new StringBuffer();
                 sql.append("SELECT sid FROM sessions WHERE season = '");
                 sql.append(newSeason).append("' ");
                 sql.append("and sessionname = '");
                 sql.append(newSession).append("';");
                 System.out.println(sql.toString());
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql.toString());
 
                 // Check if there is a result, and if so record the sid
                 long sid = -1;
                 if (rs.next()) {
                     // Substitute new session information and insert
                     sid = rs.getLong(1);
                 } else {
                     return false;
                 }
                 stmt.close();
 
                 // Get list of current classes for old session
                 sql = new StringBuffer();
                 sql.append("SELECT classid, classType, day, timeslot, instructorid, "
                         + "otherinstructors, cost FROM sessionclasses WHERE season = '");
                 sql.append(oldSeason).append("' ");
                 sql.append("and sessionname = '");
                 sql.append(oldSession).append("';");
                 System.out.println(sql.toString());
                 stmt = con.createStatement();
                 rs = stmt.executeQuery(sql.toString());
 
                 // Loop through each class in the old session
                 while (rs.next()) {
                     // Substitute new session information and insert
                     long classid = rs.getLong(1);
                     System.out.println("Duplicating class: " + classid);
                     String classType = rs.getString(2);
                     String day = rs.getString(3);
                     String timeslot = rs.getString(4);
                     long instructorId = rs.getLong(5);
                     String otherInstructors = rs.getString(6);
                     double cost = rs.getDouble(7);
                     System.out.println("  Class cost is: " + cost);
 
                     StringBuffer ins = new StringBuffer();
                     ins.append("INSERT INTO skatingclass (sid, classtype, day, "
                             + "timeslot, instructorid, otherinstructors, cost) VALUES (");
                     ins.append(sid).append(", ");
                     ins.append("'").append(classType).append("', ");
                     ins.append("'").append(day).append("', ");
                     ins.append("'").append(timeslot).append("', ");
                     ins.append(instructorId).append(", ");
                     ins.append("'").append(otherInstructors).append("', ");
                     ins.append(cost);
                     ins.append(")");
                     System.out.println(ins.toString());
 
                     Statement stmt2 = con.createStatement();
                     int rowcount = stmt2.executeUpdate(ins.toString());
                     System.out.println("Updated " + rowcount
                             + " rows in skatingclass.");
                 }
                 stmt.close();
                 con.commit();
                 con.close();
 
             } catch (SQLException ex) {
                 try {
                     con.rollback();
                     con.close();
                 } catch (SQLException e) {
                     System.out
                             .println("Unable to rollback during class duplication."
                                     + e.getMessage());
                     return false;
                 }
                 System.err.println("SQLException: " + ex.getMessage());
                 return false;
             }
         }
 
         return isAuthorized;
     }
 
     /**
      * Save changes to a skating class identified by the given classid if the
      * person logging in is an administrator. The array of values to be changed 
      * must consist of 6 non-null String values representing the fields 
      * {season, session, classType, day, time, instructor}.
      * 
      * @param loginSession used to authenticate the session
      * @param currentClassId the identifier of the class to be saved
      * @param newClassValues an array of values to be changed for this class
      * @param operation integer code indicating whether to save (1), add (2), or delete (3) a class
      */
     public boolean saveSkatingClass(LoginSession loginSession,
             long currentClassId, ArrayList<String> newClassValues, int operation) {
 
         // Check if person is authorized because they are a coach or administrator
         boolean isAuthorized = false;
         long role = getRole(loginSession.getPerson().getPid());
         if (role >= Person.ADMIN) {
             isAuthorized = true;
             System.out.println("SaveSkatingClassRole role is: " + role);
         }
 
         if (isAuthorized) {
             Connection con = getConnection();
             try {
                 con.setAutoCommit(false);
                 String season, session, classType, day, time, instructor, price;
                 StringBuffer sql = null;
 
                 Statement stmt;
                 if (operation == SAVE || operation == ADD) {
                     season = newClassValues.get(0);
                     session = newClassValues.get(1);
                     classType = newClassValues.get(2);
                     day = newClassValues.get(3);
                     time = newClassValues.get(4);
                     instructor = newClassValues.get(5);
                     price = newClassValues.get(6);
 
                     // Look up the new sid from the sessions table
                     sql = new StringBuffer();
                     sql.append("SELECT sid from sessions WHERE");
                     sql.append(" season = '").append(season).append("'");
                     sql.append(" AND");
                     sql.append(" sessionname = '").append(session).append("'");
                     System.out.println(sql.toString());
                     stmt = con.createStatement();
                     long sid = -1;
                     ResultSet rs = stmt.executeQuery(sql.toString());
                     if (rs.next()) {
                         sid = rs.getLong(1);
                     } else {
                         // session id was not found, so return an error
                         stmt.close();
                         con.close();
                         return false;
                     }
                     stmt.close();
 
                     // Look up the new instructorid from the people table
                     boolean foundInstructorId = false;
                     sql = new StringBuffer();
                     String[] nameArray = instructor.split(" ", 2);
                     sql.append("SELECT pid from people WHERE");
                     sql.append(" givenname = '").append(nameArray[0])
                             .append("'");
                     sql.append(" AND");
                     sql.append(" surname = '").append(nameArray[1]).append("'");
                     sql.append(" AND");
                     sql.append(" role >= ").append(Person.COACH).append("");
                     System.out.println(sql.toString());
                     stmt = con.createStatement();
                     long pid = -1;
                     rs = stmt.executeQuery(sql.toString());
                     if (rs.next()) {
                         pid = rs.getLong(1);
                         foundInstructorId = true;
                     }
                     stmt.close();
                     System.out.println("Found coach with pid: " + pid);
                     // Save the new information to the skatingclass table
                     sql = new StringBuffer();
                     if (operation == SAVE) {
                         sql.append("UPDATE skatingclass ");
                         sql.append("SET");
                         sql.append(" sid = ").append(sid);
                         sql.append(", classType = '").append(classType)
                                 .append("'");
                         sql.append(", day = '").append(day).append("'");
                         sql.append(", timeslot = '").append(time).append("'");
                         if (foundInstructorId) {
                             sql.append(", instructorid = ").append(pid);
                         }
                         sql.append(", cost = ").append(price);
                         sql.append(" where classid = ").append(currentClassId);
                     } else if (operation == ADD) {
                         sql.append("INSERT INTO skatingclass ");
                         sql.append("(sid, classType, day, timeslot, cost");
                         if (foundInstructorId) {
                             sql.append(", instructorid");
                         }
                         sql.append(") VALUES (");
                         sql.append(sid).append(", ");
                         sql.append("'").append(classType).append("', ");
                         sql.append("'").append(day).append("', ");
                         sql.append("'").append(time).append("',");
                         sql.append(price);
                         if (foundInstructorId) {
                             sql.append(", ").append(pid);
                         }
                         sql.append(")");
                     }
                 } else if (operation == DELETE) {
                     sql = new StringBuffer();
                     sql.append("DELETE from skatingclass where classid = ")
                             .append(currentClassId);
                 } else {
                     System.err.println("Invalid operation while saving class: "
                             + operation);
                 }
                 System.out.println(sql.toString());
                 stmt = con.createStatement();
                 int rowcount = stmt.executeUpdate(sql.toString());
                 System.out.println("Updated " + rowcount
                         + " rows in skatingclass.");
                 stmt.close();
 
                 con.commit();
                 con.close();
 
             } catch (SQLException ex) {
                 try {
                     System.out
                             .println("SQL Error while saving class, so rolling back changes.");
                     con.rollback();
                     con.close();
                 } catch (SQLException e) {
                     System.out.println("Unable to rollback during class save."
                             + e.getMessage());
                     return false;
                 }
                 System.err.println("SQLException: " + ex.getMessage());
                 return false;
             }
         }
 
         return isAuthorized;
     }
 
     /**
      * Download a CSV file that corresponds to the given classId.
      */
     public long downloadRoster(LoginSession loginSession, long classId) {
         
         // TODO: Check authentication credentials as Admin or Coach
         // Check authentication credentials
         if (!isSessionValid(loginSession)) {
             return 0L;
         }
 
         // Check if person is authorized because they are a coach or administrator
         boolean isAuthorized = false;
         long role = getRole(loginSession.getPerson().getPid());
         if (role >= Person.COACH) {
             isAuthorized = true;
         }
 
         String filename = "";
         long key = 0L;
 
         if (isAuthorized) {
             try {
                 File tempFile = File.createTempFile("jsc-roster-", ".csv");
                 // tempFile.deleteOnExit();
                 PrintWriter w = new PrintWriter(tempFile);
 
                 CsvStreamWriter csw = new CsvStreamWriter(w);
         
                 Connection con = SkaterRegistrationServiceImpl.getConnection();
                 StringBuffer sql = new StringBuffer();
                 sql.append("SELECT sc.season,sc.sessionname as session, ");
                 sql.append("sc.classtype, sc.day, p.surname, p.givenname, y.paypal_status ");
                 sql.append("FROM roster r, people p, payment y, sessionclasses sc ");
                 sql.append("WHERE r.pid = p.pid ");
                 sql.append("AND r.paymentid = y.paymentid ");
                 sql.append("AND r.classid = sc.classid ");
                 sql.append("AND r.classId = ? ");
                 sql.append("ORDER BY sc.season,sc.sessionname, sc.classtype, sc.day, p.surname, p.givenname");
                 PreparedStatement stmt = con.prepareStatement(sql.toString());
                 stmt.setLong(1, classId);
 
                 csw.writeToCsv(stmt);
                 filename = tempFile.getAbsolutePath();
                 
                 key = recordDownloadFilename(filename);
 
             } catch (IOException e) {
                 // TODO: throw a GWT exception here
                 e.printStackTrace();
             } catch (SQLException e) {
                 // TODO: throw a GWT exception here
                 e.printStackTrace();
             }
         }
 
         return key;
     }
 
     /**
      * Query the database to get the list of membership types that have been defined, 
      * and return them as an ArrayList of the class MembershipType.
      * @param loginSession the authenticated session
      * @return ArrayList of MembershipType
      */
     public ArrayList<MembershipType> getMembershipTypes(LoginSession loginSession) {
         ArrayList<MembershipType> mtList = new ArrayList<MembershipType>();
 
         // Check authentication credentials
         boolean isAuthentic = isSessionValid(loginSession);
         if (!isAuthentic) {
             return null;
         }
 
         // Query the database to get the list of membership types
         StringBuffer sql = new StringBuffer();
         sql.append("select typeName, membertype, description, cost from membershiptype ");
         System.out.println(sql.toString());
 
         try {
             Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString());
             while (rs.next()) {
                 MembershipType mt = new MembershipType(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4));
                 mtList.add(mt);
             }
             stmt.close();
             con.close();
 
         } catch (SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
 
         return mtList;
     }
 
     /**
      * Write a random key and a path to a downloaded file into the JSCDB database.
      * @param filename the absolute path of the file to be written to the DB
      * @return the long key value that is used to index the file path/
      * @throws SQLException
      */
     private long recordDownloadFilename(String filename) throws SQLException {
         Connection con;
         PreparedStatement stmt;
         Random random =  new Random();
         long key = random.nextLong();
         
         // Now cache the filename in the database for the download servlet to pick up later
         // Use a random key to index it, which will be included in the request URI
         StringBuffer isql = new StringBuffer();
         isql.append("INSERT INTO downloads (randomkey, filepath) VALUES (?, ?)");
         con = SkaterRegistrationServiceImpl.getConnection();
         stmt = con.prepareStatement(isql.toString());
         stmt.setLong(1, key);
         stmt.setString(2, filename);
         boolean resultIsRS = stmt.execute();
         stmt.close();
         con.close();
         return key;
     }
     
     /**
      * Look up a roster of classes.  The exact roster looked up depends on the sql
      * that is passed into the class, sometimes for a particular student, sometimes
      * for a particular class.
      * @param loginSession used to authenticate the session
      * @param rosterQuery the SQL query used to find the roster
      * @return and ArrayList of RosterEntry objects matching the query
      */
     private ArrayList<RosterEntry> getRoster(LoginSession loginSession,
             String rosterQuery) {
 
         ArrayList<RosterEntry> roster = new ArrayList<RosterEntry>();
 
         // Check authentication credentials
         boolean isAuthentic = isSessionValid(loginSession);
         if (!isAuthentic) {
             return null;
         }
 
         try {
             Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(rosterQuery.toString());
             while (rs.next()) {
                 RosterEntry newEntry = createRosterEntry(rs);
                 roster.add(newEntry);
             }
             stmt.close();
             con.close();
 
         } catch (SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
 
         return roster;
     }
 
     /**
      * Helper method to create a RosterEntry object from the results of a JDBC
      * query that contains the appropriate fields.
      * @param rs the result set containing the roster fields
      * @return an instance of RosterEntry
      * @throws SQLException if there is an error accessing the result set
      */
     private RosterEntry createRosterEntry(ResultSet rs) throws SQLException {
         RosterEntry entry = null;
         entry = new RosterEntry();
         entry.setRosterid(rs.getLong(1));
         entry.setClassid(rs.getLong(2));
         entry.setPid(rs.getLong(3));
         entry.setLevelpassed(rs.getString(4));
         entry.setPaymentid(rs.getLong(5));
         entry.setPayment_amount(rs.getDouble(6));
         entry.setPaypal_status(rs.getString(7));
         entry.setDate_updated(rs.getDate(8));
         entry.setSurname(rs.getString(9));
         entry.setGivenname(rs.getString(10));
         entry.setSection(rs.getString(11));
         entry.setMaxLevel(rs.getString(12));
         return entry;
     }
 
     /**
      * Check if the user is in the database, and if the given password matches
      * @param username the username of the person who is signing in
      * @param password the password of the person who is signing in
      * @return the id of the person if valid, otherwise 0
      */
     private Person lookupPerson(long pid) {
 
         StringBuffer sql = new StringBuffer();
         sql.append("select pid, surname, givenname, middlename, email, birthdate, home_phone, "
                 + "cell_phone, work_phone, street1, street2, city, state, zipcode, parentfirstname, "
                 + "parentsurname, parentemail, username, password, role, maxlevel from people where ");
         sql.append("pid = '").append(pid).append("'");
         System.out.println(sql.toString());
 
         Person person = null;
         try {
             Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString());
             if (rs.next()) {
                 person = new Person();
                 person.setPid(rs.getInt(1));
                 person.setLname(rs.getString(2));
                 person.setFname(rs.getString(3));
                 person.setMname(rs.getString(4));
                 person.setEmail(rs.getString(5));
                 Date bday = rs.getDate(6);
                 SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
                 person.setBday(df.format(bday));
                 person.setHomephone(rs.getString(7));
                 person.setCellphone(rs.getString(8));
                 person.setWorkphone(rs.getString(9));
                 person.setStreet1(rs.getString(10));
                 person.setStreet2(rs.getString(11));
                 person.setCity(rs.getString(12));
                 person.setState(rs.getString(13));
                 person.setZip(rs.getString(14));
                 person.setParentFirstname(rs.getString(15));
                 person.setParentLastname(rs.getString(16));
                 person.setParentEmail(rs.getString(17));
                 person.setUsername(rs.getString(18));
                 person.setRole(rs.getInt(20));
                 person.setMaxLevel(rs.getString(21));
                 person.setPassword(null);
                 person.setMember(false);
             }
             stmt.close();
 
             // Now look up if the person already paid their membership this season
             // If so, set their membership flag
             String season = SessionSkatingClass.calculateSeason();
             StringBuffer msql = new StringBuffer();
             msql.append("select mid, pid, paymentid, season, paypal_status, membertype from memberstatus where ");
             msql.append("pid = '").append(pid).append("'");
             msql.append(" AND ");
             msql.append("season LIKE '").append(season).append("'");
             System.out.println(msql.toString());
             stmt = con.createStatement();
             rs = stmt.executeQuery(msql.toString());
             if (rs.next()) {
                 // if we found a matching record, they have paid their membership
                 person.setMember(true);
                 person.setMembershipId(rs.getLong(1));
                 person.setMembershipPaymentId(rs.getLong(3));
                 person.setMembershipStatus(rs.getString(5));
                 person.setMembershipType(rs.getString(6));
             } else {
                 person.setMember(false);
                 person.setMembershipId(0);
                 person.setMembershipPaymentId(0);
                 person.setMembershipStatus("Unpaid");
                 person.setMembershipType("");
             }
             stmt.close();
 
             con.close();
 
         } catch (SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
 
         return person;
     }
 
     /**
      * Look up the role for a person, assuming the session is authorized already for this lookup
      * @param pid the identifier of the person to look up
      * @return the role for this person
      */
     private long getRole(long pid) {
         long role = Person.SKATER;
         StringBuffer roleQuery = new StringBuffer();
         roleQuery.append("select p.pid, p.role " + "from people p "
                 + "where p.pid = ");
         roleQuery.append(pid);
         System.out.println(roleQuery.toString());
         Connection con = getConnection();
         try {
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(roleQuery.toString());
             if (rs.next()) {
                 role = rs.getLong(2);
             }
             stmt.close();
             con.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return role;
     }
 
     private AccountInfo lookupAccountByUsername(String username) {
 
         // Query the database to get the list of emails for username
         StringBuffer sql = new StringBuffer();
         sql.append("select pid, username, email ");
         sql.append("from people ");
         sql.append("where username = ?;");
         System.out.println(sql.toString());
 
         AccountInfo acctInfo = null;
         try {
             Connection con = getConnection();
             PreparedStatement stmt = con
                     .prepareStatement(sql.toString(),
                             ResultSet.TYPE_SCROLL_SENSITIVE,
                             ResultSet.CONCUR_READ_ONLY);
             stmt.setString(1, username);
             System.out.println(stmt.toString());
             acctInfo = lookupAccount(stmt);
             stmt.close();
             con.close();
         } catch (SQLException e) {
             System.err.println("SQLException: " + e.getMessage());
         }
 
         return acctInfo;
     }
 
     private AccountInfo lookupAccountByEmail(String email) {
 
         // Query the database to get the list of usernames for email
         StringBuffer sql = new StringBuffer();
         sql.append("select pid, username, email ");
         sql.append("from people ");
         sql.append("where email = ?");
         sql.append(" OR parentemail = ?;");
         System.out.println(sql.toString());
 
         AccountInfo acctInfo = null;
         try {
             Connection con = getConnection();
             PreparedStatement stmt = con
                     .prepareStatement(sql.toString(),
                             ResultSet.TYPE_SCROLL_SENSITIVE,
                             ResultSet.CONCUR_READ_ONLY);
             stmt.setString(1, email);
             stmt.setString(2, email);
             System.out.println(stmt.toString());
             acctInfo = lookupAccount(stmt);
             stmt.close();
             con.close();
         } catch (SQLException e) {
             System.err.println("SQLException: " + e.getMessage());
         }
 
         return acctInfo;
     }
 
     private AccountInfo lookupAccount(PreparedStatement stmt) {
         AccountInfo acctInfo = null;
         try {
             ResultSet rs = stmt.executeQuery();
             long pid = 0;
             ArrayList<String> usernames = new ArrayList<String>();
             String email = "";
 
             if (rs.next()) {
                 pid = rs.getLong(1);
                 usernames.add(rs.getString(2));
                 email = rs.getString(3);
                 acctInfo = new AccountInfo(pid, usernames, email);
             }
 
         } catch (SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
         return acctInfo;
     }
 
     /**
      * Update the database with new encrypted password for the person identified by pid.
      */
     private boolean updatePassword(long pid, String newPassword) {
         boolean successFlag = false;
 
         StringBuffer sql = new StringBuffer();
         sql.append("update people set ");
         // Hash the password before insertion into the database
         String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
         sql.append("password='").append(hashed).append("' ");
         sql.append("where pid=").append(pid);
         System.out.println(sql.toString());
 
         try {
             Connection con = getConnection();
             Statement stmt = con.createStatement();
             stmt.executeUpdate(sql.toString());
             stmt.close();
             con.close();
             successFlag = true;
         } catch (SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
         return successFlag;
     }
 
     /**
      * Check the LoginSession to see if it is a valid session that corresponds
      * to the correct user and that it has not expired.
      * @param LoginSession containing the the sessionId to be checked
      * @return true if valid session is valid, false if otherwise
      */
     private boolean isSessionValid(LoginSession loginSession) {
         boolean isAuthenticated = false;
         HttpServletRequest request = this.getThreadLocalRequest();
         HttpSession session = request.getSession();
         if (loginSession != null
                 && session.getId().equals(loginSession.getSessionId())) {
             // TODO: Also check if session corresponds to a PID (which requires having written the sessionId to the database with a PID
             isAuthenticated = true;
         } else {
             session.invalidate();
         }
         return isAuthenticated;
     }
 
     /**
      * Check if the user is in the database, and if the given password matches
      * @param username the username of the person who is signing in
      * @param password the password of the person who is signing in
      * @return the id of the person if valid, otherwise 0
      */
     private int checkPassword(String username, String password) {
         int pid = 0;
 
         StringBuffer sql = new StringBuffer();
         sql.append("select pid,password from people where ");
         sql.append("username LIKE '").append(username).append("'");
         System.out.println(sql.toString());
 
         try {
             Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString());
             if (rs.next()) {
                 pid = rs.getInt(1);
                 String hashed = rs.getString(2);
                 // Check that an unencrypted password matches one that has
                 // previously been hashed
                 if (!BCrypt.checkpw(password, hashed)) {
                     System.out.println("It does not match");
                     pid = 0;
                 }
             }
             stmt.close();
             con.close();
 
         } catch (SQLException ex) {
             System.err.println("SQLException: " + ex.getMessage());
         }
 
         return pid;
     }
 
     /**
      * Open a JDBC database connection.
      */
     protected static Connection getConnection() {
         Connection con = null;
 
         try {
             System.out.println("getConnection: finding driver class...");
             @SuppressWarnings("rawtypes")
             Class driverClass = Class.forName(JDBC_DRIVER);
             System.out.println("getConnection: driver loaded.");
         } catch (java.lang.ClassNotFoundException e) {
             System.err.print("ClassNotFoundException: ");
             System.err.println(e.getMessage());
         }
 
         try {
             System.out.println("getConnection: opening connection...");
             con = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
             System.out.println("getConnection: connection attempt completed.");
         } catch (SQLException ex) {
             ex.printStackTrace(System.err);
             System.err
                     .println(ex.getClass().getName() + ": " + ex.getMessage());
         }
 
         if (con == null) {
             System.out.println("getConnection: Connection was null.");
             System.err.println("Created a null connection object.");
         }
 
         return con;
     }
 
     /**
      * A class that encapsulates account information to be passed to local methods.
      */
     private class AccountInfo {
         protected final long pid;
         protected final ArrayList<String> usernames;
         protected final String email;
 
         public AccountInfo(long pid, ArrayList<String> usernames, String email) {
             this.pid = pid;
             this.usernames = usernames;
             this.email = email;
         }
     }
 }

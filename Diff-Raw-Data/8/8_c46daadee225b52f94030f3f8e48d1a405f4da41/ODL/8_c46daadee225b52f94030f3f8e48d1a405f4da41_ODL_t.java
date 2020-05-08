 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class ODL {
 
     public static final Scanner input = new Scanner(System.in);
     public static MyConnection myConn = null;
 
     public static void main(String[] args) {
         try {
 
             Class.forName("oracle.jdbc.driver.OracleDriver");
 
             ResultSet rs = null;
             boolean shouldContinue = true;
             char choice;
 
             try {
                 myConn = new MyConnection();
                 while (shouldContinue) {
                     System.out.println("1. Register a Patient");
                     System.out.println("2. Login as a Patient");
                     System.out.println("3. Login as a Health Professional");
                     System.out.println("4. Exit System");
                     choice = input.nextLine().charAt(0);
 
                     switch (choice) {
                         case '1':
                             registerPatient();
                             break;
 
                         case '2':
                             loginAsPatient();
                             break;
 
                         case '3':
                             loginAsHealthProfessional();
                             break;
 
                         case '4':
                             System.out.println("Thanks for using our system.. Have a nice day..");
                             System.out.println("Bye!!");
                             shouldContinue = false;
                             break;
 
                         default:
                             System.out.println("Please Select An Option From The Allowed Values");
                     }
                 }
 
             } finally {
                 close(rs);
                 if (myConn != null) {
                     myConn.closeStatement();
                     myConn.closeConnection();
                 }
             }
         } catch (Throwable oops) {
             if (oops instanceof MyException)
                 System.out.println(((MyException) oops).message);
             oops.printStackTrace();
         }
     }
 
     private static void loginAsPatient() throws MyException {
         System.out.println("Enter patient id: ");
         String patientId = input.nextLine();
         System.out.println("Enter password: ");
         String password = input.nextLine();
 
         int pid = Integer.parseInt(patientId);
         Patient patient = Patient.getById(pid, myConn);
         if (patient != null && patient.password.equals(password)) {
             boolean logout = false;
             char choice;
 
             while (!logout) {
                 System.out.println("1. Enter Data");
                 System.out.println("2. View Data");
                 System.out.println("3. Clear Alerts");
                 System.out.println("4. Connection");
                 System.out.println("5. Logout");
                 choice = input.nextLine().charAt(0);
 
                 switch (choice) {
 
                     case '1':
                         enterData(pid);
                         break;
                     case '2':
                         viewData(pid);
                         break;
                     case '3':
                         deleteAlerts(pid);
                         break;
                     case '4':
                         connections(pid);
                         break;
                     case '5':
                         logout = true;
                         System.out.println("You have been successfully logged out");
                         break;
 
                     default:
                         System.out.println("Please Select An Option From The Allowed Values");
 
                 }
             }
         } else
             System.out.println("Invalid Patient Id/Password pair. Please make sure you enter correct credentials");
     }
 
     private static void connections(int pid) throws MyException {
         boolean shouldContinue = true;
         while (shouldContinue) {
             System.out.println("1. Find a new Health Friend");
             System.out.println("2. Find a Health Friend at Risk");
             System.out.println("3. Go back to patient's home page");
             char choice = input.nextLine().charAt(0);
             switch(choice) {
                 case '1':
                     findNewHealthFriend(pid);
                     break;
                 case '2':
                     displayHealthFriendAtRisk(pid);
                     break;
                 case '3':
                     shouldContinue = false;
                     break;
                 default:
                     System.out.println("Please Select a valid option");
             }
         }
 
     }
 
     private static void findNewHealthFriend(int pid) throws MyException {
         Patient patient = Patient.getById(pid, myConn);
         List<Patient> prospectiveFriends = patient.findHealthFriends(myConn);
         for (int i = 1; i <= prospectiveFriends.size(); i++)
             System.out.println(i + ". " + prospectiveFriends.get(i - 1).name);
         if (prospectiveFriends.size() > 0) {
             System.out.println("Enter the serial number of the person you would like to be friends with: ");
             int fid = Integer.parseInt(input.nextLine());
             HealthFriend.insert(pid, prospectiveFriends.get(fid - 1).pid, myConn);
         }
     }
 
     private static void displayHealthFriendAtRisk(int pid) throws MyException {
         List<Patient> prospectiveFriends = Patient.findHealthFriendsAtRisk(pid, myConn);
         for(int i = 1; i<= prospectiveFriends.size(); i++)
             System.out.println(i + ". " + prospectiveFriends.get(i-1).name);
     }
 
     private static void deleteAlerts(int pid) throws MyException {
         Alert.deleteViewedAlerts(pid, myConn);
     }
 
     private static void viewData(int patientId) throws MyException {
         char choice;
         boolean shouldContinue = true;
         while (shouldContinue) {
             System.out.println("1. View Observations");
             System.out.println("2. View MyAlert");
             System.out.println("3. Go to patient login homepage");
             choice = input.nextLine().charAt(0);
 
             switch (choice) {
                 case '1':
                     viewObservations(patientId);
                     break;
                 case '2':
                     viewAlerts(patientId);
                     break;
                 case '3':
                     shouldContinue = false;
                     break;
                 default:
                     System.out.println("Please Select An Option From The Allowed Values");
             }
         }
     }
 
     private static void viewAlerts(int patientId) throws MyException {
         addAlertsForHealthFriends(patientId, myConn);
         displayAlertsForPatient(patientId);
     }
 
     private static void addAlertsForHealthFriends(int patientId, MyConnection myConn) throws MyException {
         List<Integer> ids = HealthFriend.getFriendsOfPatient(patientId, myConn);
         for(int id : ids) {
             if(Alert.ignoredAlertExists(id, myConn)) {
                Patient patient = Patient.getById(id, myConn);
                 String message = "ALERT: " + patient.name + " has an observation that exceeds the threshold values";
                Alert.insert(patientId, message, "0", new Date(), myConn);
             }
         }
     }
 
     private static void displayAlertsForPatient(int patientId) throws MyException {
         List<Alert> alertList = Alert.getByPId(patientId, myConn);
         for (Alert alert : alertList){
             System.out.println(alert.text + " GENERATED AT: " + alert.timestamp);
             alert.markViewed(myConn);
         }
     }
 
     private static void enterData(int patientId) throws MyException {
         char choice;
         boolean shouldContinue = true;
         while (shouldContinue) {
             System.out.println("1. Enter new observation data");
             System.out.println("2. Add a new observation type");
             System.out.println("3. Go to patient login homepage");
             choice = input.nextLine().charAt(0);
 
             switch (choice) {
                 case '1':
                     enterObservations(patientId);
                     break;
                 case '2':
                     enterNewObservationType("General");
                     break;
                 case '3':
                     shouldContinue = false;
                     break;
                 default:
                     System.out.println("Please Select An Option From The Allowed Values");
             }
         }
     }
 
     private static void enterNewObservationType(String category) throws MyException {
         System.out.println("Enter the observation type name :");
         String name = input.nextLine();
         ObservationType.insertForCategory(name, category, myConn);
         while (true) {
             System.out.println("Enter additional information question for the new type:");
             String question = input.nextLine();
             ObservationQuestion.insertByTypeName(name, question, myConn);
             System.out.println("Would you like to get any more additional information? (y/n)");
             char choice = input.nextLine().toLowerCase().charAt(0);
 
             if (choice == 'n')
                 return;
         }
     }
 
     private static void enterObservations(int patientId) throws MyException {
         List<Integer> availableTypes = getObservationTypesForPatient(patientId);
 
         System.out.println("Please enter the observation type no that you would like to enter: ");
         for (int i = 0; i < availableTypes.size(); i++)
             System.out.println((i + 1) + ". " + ObservationType.getById(availableTypes.get(i), myConn).name);
 
         int typeNo = Integer.parseInt(input.nextLine());
         int otid = availableTypes.get(typeNo - 1);
         List<ObservationQuestion> questions = ObservationQuestion.getByObservationType(otid, myConn);
         String message = "Enter date and time of the observation (in MM/dd/yyyy hh:mm AM/PM ex: 10/04/2013 10:15 AM): ";
         String format = "MM/dd/yyyy hh:mm a";
         Date obsDate = getDateFromUser(message, format);
         for (ObservationQuestion question : questions) {
             System.out.println(question.text);
             String answer = input.nextLine();
             Date recordDate = new Date();
             Observation.insert(patientId, otid, obsDate, recordDate, question.qid, answer, myConn);
             try{
                 if(ObservationThreshold.crossesThreshold(question.qid, Integer.parseInt(answer), myConn))
                     addThresholdAlert(patientId, question.qid, myConn);
             } catch (NumberFormatException e){
                 return;
             }
         }
 
     }
 
     private static void addThresholdAlert(int patientId, int qid, MyConnection myConn) throws MyException {
         String message = "ALERT: Your values for the question: " + qid +
                 " crosses the threshold values";
         Alert.insert(patientId, message, "0", new Date(), myConn);
     }
 
     private static void viewObservations(int patientId) throws MyException {
         List<Integer> availableTypes = getObservationTypesForPatient(patientId);
 
         System.out.println("Please select the observation type no: ");
         for (int i = 0; i < availableTypes.size(); i++)
             System.out.println((i + 1) + ". " + ObservationType.getById(availableTypes.get(i), myConn).name);
         int typeNo = Integer.parseInt(input.nextLine());
         System.out.println("Please enter begin date for filter in mm/dd/yyyy format eg. 03/08/1988:");
         String beginDate = input.nextLine();
         System.out.println("Please enter end date for filter in mm/dd/yyyy format eg. 03/08/1988:");
         String endDate = input.nextLine();
         try {
             List<Observation> observations = Observation.filter(patientId, availableTypes.get(typeNo - 1),
                     getDateFromString(beginDate + " 0:0:1", "MM/dd/yyyy HH:mm:ss"),
                     getDateFromString(endDate + " 23:59:59", "MM/dd/yyyy HH:mm:ss"), myConn);
             for (Observation o : observations) {
                 System.out.println(o.pid + " " + o.otid + " " + o.obvTimestamp + " " + o.recTimestamp + " " +
                         o.qid + " " + o.answer);
             }
         } catch (ParseException e) {
             e.printStackTrace();
             System.out.println("Invalid input.");
         }
     }
 
     private static List<Integer> getObservationTypesForPatient(int patientId) throws MyException {
         Set<Integer> availableTypesSet = new HashSet<Integer>();
         List<Integer> cids = PatientClassRelationship.getClassesForPatient(patientId, myConn);
 
         for (Integer cid : cids)
             availableTypesSet.addAll(PatientClassObservationTypeMapper.getByClass(cid, myConn));
 
         return new ArrayList<Integer>(availableTypesSet);
     }
 
     private static Date getDateFromUser(String message, String format) {
         while (true) {
             try {
                 System.out.println(message);
                 String date = input.nextLine();
                 return getDateFromString(date, format);
             } catch (ParseException e) {
                 System.out.println("Please enter date and time in proper format");
             }
         }
 
     }
 
     private static void registerPatient() throws ParseException, MyException {
         System.out.println("Please enter patients name:");
         String name = input.nextLine();
         System.out.println("Please enter patients address:");
         System.out.println("Apartment number:");
         int aptNo = Integer.parseInt(input.nextLine());
         System.out.println("City:");
         String city = input.nextLine();
         System.out.println("Country:");
         String country = input.nextLine();
         System.out.println("Please enter patients date of birth in mm/dd/yyyy format eg. 03/08/1988:");
         String dob = input.nextLine();
         System.out.println("Please enter patients sex(m/f):");
         String sex = input.nextLine();
         System.out.println("Do you want your profile to be public? (y/n):");
         String publicStatus = input.nextLine();
         System.out.println("Please enter the password (minimum 6 character long):");
         String password = input.nextLine();
 
         if (checkArgumentsForPatient(dob, sex, publicStatus, password)) {
             int patientId = Patient.insert(getDateFromString(dob, "MM/dd/yyyy"), name, aptNo, city, country, sex.toLowerCase(),
                     publicStatus.toLowerCase(), password.trim(), myConn);
             if (patientId > 0) {
                 System.out.println("A patient has been created with id " + patientId + ".");
                 System.out.println("Please remember this id as this will be used to login next time");
                 PatientClassRelationship.insertAsGeneral(patientId, myConn);
 
             }
         }
     }
 
     private static Date getDateFromString(String dob, String format) throws ParseException {
         SimpleDateFormat formatter = new SimpleDateFormat(format);
         formatter.setLenient(false);
         return formatter.parse(dob);
     }
 
     private static boolean checkArgumentsForPatient(String dob, String sex, String publicStatus, String password) {
         try {
             getDateFromString(dob, "MM/dd/yyyy");
         } catch (Exception e) {
             System.out.println("Please Enter date in MM dd yyyy format example 03 08 1988");
             return false;
         }
         if (sex.length() != 1 || !(sex.toLowerCase().equals("m") || sex.toLowerCase().equals("f"))) {
             System.out.println("Please Enter Sex properly. The only available options are m or f");
             return false;
         }
         if (publicStatus.length() != 1 || !(publicStatus.toLowerCase().equals("y") || publicStatus.toLowerCase().equals("n"))) {
             System.out.println("Please Enter Public Status properly. The only available options are y or n");
             return false;
         }
         if (password.trim().length() < 6) {
             System.out.println("Please make sure that password is at-least 6 character long");
             return false;
         }
 
         return true;
     }
 
     private static void loginAsHealthProfessional() throws MyException {
         System.out.println("Enter Health Professional id: ");
         String HealthProfessionalId = input.nextLine();
         System.out.println("Enter password: ");
         String password = input.nextLine();
 
         int hpid = Integer.parseInt(HealthProfessionalId);
         HealthProfessional healthProfessional = HealthProfessional.getById(hpid, myConn);
         if (healthProfessional != null && healthProfessional.password.equals(password)) {
             boolean logout = false;
             char choice;
 
             while (!logout) {
                 System.out.println("1. Enter New Observation Type");
                 System.out.println("2. Change Category of Observation Type");
                 System.out.println("3. New Association between Observation Type and Patient Class");
                 System.out.println("4. Change Patient Class");
                 System.out.println("5. View Aggregated Report");
                 System.out.println("6. Logout");
                 choice = input.nextLine().charAt(0);
 
                 switch (choice) {
 
                     case '1':
                         ObservationCategory category = getObservationCategoryFromUser();
                         enterNewObservationType(category.name);
                         break;
 
                     case '2':
                         changeCategoryOfObservationType();
                         break;
 
                     case '3':
                         addObservationTypeToPatientClass();
                         break;
 
                     case '4':
                         changePatientClass();
                         break;
 
                     case '5':
                         aggregatedReport();
                         break;
 
                     case '6':
                         logout = true;
                         System.out.println("You have been successfully logged out");
                         break;
 
                     default:
                         System.out.println("Please Select An Option From The Allowed Values");
 
                 }
             }
         } else
             System.out.println("Invalid Health Professional Id/Password pair. Please make sure you enter correct credentials");
     }
 
     private static void aggregatedReport() throws MyException {
         PatientClass patientClass = getPatientClassFromUser("Please select a Patient Class");
         ObservationType observationType = getObservationTypeFromUser("Please select an Observation Type");
         ObservationQuestion observationQuestion = getObservationQuestionFromUser("Please select an Observation Question", observationType.otid);
 
         System.out.println("Please enter the date range for filter: ");
         String message1 = "Please enter start time in mm/dd/yyyy format eg. 03/08/1988:";
         String format = "MM/dd/yyyy";
         Date startTime = getDateFromUser(message1,format);
 
         String message2 = "Please enter end time in mm/dd/yyyy format eg. 03/08/1988:";
         Date endTime = getDateFromUser(message2,format);
         Timestamp strt = new Timestamp(startTime.getTime());
         Timestamp end = new Timestamp(endTime.getTime());
 
         String func = getAggregateFunction();
         String query = "select p.pid, "+func+"(*) from patient p, " +
                 "observation o, PatientClassRelationship pc " +
                 "where pc.pid=p.pid and p.pid=o.pid and " +
                 "o.otid = "+observationType.otid+" and o.qid="+observationQuestion.qid+" and " +
                 "o.obvTimestamp >= ? and o.obvTimestamp <= ? " +
                 "and pc.cid = (select cid from PatientClass where name = "+patientClass.name+") group by p.pid";
         try {
             PreparedStatement pstmt = myConn.conn.prepareStatement(query);
             pstmt.setTimestamp(1, strt);
             pstmt.setTimestamp(2, end);
             ResultSet resultSet = pstmt.executeQuery();
             while (resultSet.next())
                 System.out.println(resultSet.getInt(1) + "\t" + resultSet.getInt(2));
         } catch (SQLException e) {
             System.out.println("The query can not be run. This may be because the aggregation is not supported");
         }
 
 
     }
 
     private static void addObservationTypeToPatientClass() throws MyException {
         ObservationType observationType = getObservationTypeFromUser("Select Observation Type");
         PatientClass patientClass = getPatientClassFromUser("Please select a Patient Class");
         observationType.updatePatientClassMapping(patientClass.cid, myConn);
     }
 
     private static void changePatientClass() throws MyException {
         List<Patient> patients = Patient.getAllPatients(myConn);
         System.out.println("Please select a patient you want to add Class for");
         System.out.println("No.\tPatient Id\tPatient Name");
         System.out.println("_____\t__________\t____________");
         for(int i = 1; i<= patients.size(); i++)
             System.out.println(i + ". \t" + patients.get(i-1).pid + "\t" + patients.get(i-1).name);
         int pid = Integer.parseInt(input.nextLine());
 
         PatientClass patientClass = getPatientClassFromUser("Please select a Patient Class");
         patients.get(pid-1).addClass(patientClass.cid, myConn);
     }
 
     private static PatientClass getPatientClassFromUser(String message) throws MyException {
         List<PatientClass> patientClasses = PatientClass.getAllPatientClass(myConn);
         System.out.println(message);
         for(int i = 1; i<= patientClasses.size(); i++)
             System.out.println(i + "." + patientClasses.get(i-1).name);
         int cid = Integer.parseInt(input.nextLine());
         return patientClasses.get(cid - 1);
     }
 
     private static String getAggregateFunction(){
         while (true) {
             System.out.println("1. Average");
             System.out.println("2. Minimum");
             System.out.println("3. Maximum");
             System.out.println("4. Count");
             char choice = input.nextLine().charAt(0);
 
             switch (choice) {
                 case '1':
                     return "AVG";
                 case '2':
                     return "MIN";
                 case '3':
                     return "MAX";
                 case '4':
                     return "COUNT";
                 default:
                     System.out.println("Please Select An Option From The Allowed Values");
             }
         }
     }
 
     private static void changeCategoryOfObservationType() throws MyException {
         ObservationType observationType = getObservationTypeFromUser("Please select the Observation Type " +
                 "You want to change Category of");
         ObservationCategory category = getObservationCategoryFromUser();
         observationType.updateCategory(category.ocid, myConn);
     }
 
     private static ObservationType getObservationTypeFromUser(String message) throws MyException {
         List<ObservationType> types = ObservationType.getAllTypes(myConn);
         System.out.println(message);
         for(int i = 1; i<= types.size(); i++)
             System.out.println(i + ". " + types.get(i-1).name);
         int otid = Integer.parseInt(input.nextLine());
         return types.get(otid - 1);
     }
 
     private static ObservationQuestion getObservationQuestionFromUser(String message, Integer otid) throws MyException {
         List<ObservationQuestion> questions = ObservationQuestion.getAllQuestionsForType(myConn,otid);
         System.out.println(message);
         for(int i = 1; i<= questions.size(); i++)
             System.out.println(i + ". " + questions.get(i-1).text);
         int qid = Integer.parseInt(input.nextLine());
         return questions.get(qid - 1);
     }
 
     private static ObservationCategory getObservationCategoryFromUser() throws MyException {
         System.out.println("Please select an Observation Category");
         List<ObservationCategory> categories = ObservationCategory.getAllCategories(myConn);
         for(int i = 1; i<= categories.size(); i++)
             System.out.println(i + ". " + categories.get(i-1).name);
         int ocid = Integer.parseInt(input.nextLine());
         return categories.get(ocid-1);
     }
 
 
     static void close(ResultSet rs) {
         if (rs != null) {
             try {
                 rs.close();
             } catch (Throwable whatever) {
                 whatever.printStackTrace();
             }
         }
     }
 }

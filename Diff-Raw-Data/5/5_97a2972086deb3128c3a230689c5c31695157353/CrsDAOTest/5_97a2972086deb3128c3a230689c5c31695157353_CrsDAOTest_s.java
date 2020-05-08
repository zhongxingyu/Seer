 package crp;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.JUnitCore;
 import org.junit.runner.Result;
 
 import com.crs.dao.CrsDAO;
 import com.crs.model.CarPoolForm;
 import com.crs.model.CarPoolMemberForm;
 import com.crs.model.EmployeeForm;
 import com.crs.service.LoginService;
 
 
 /**
  * This class contains all the test cases to check the methods in the
 * CrsDAOTest.java
  * @author Rajeev / Mohan
  */
 
 public class CrsDAOTest {
 		
 	private CrsDAO dao;	
 	
 	public CrsDAOTest(){
 		dao = new CrsDAO();		
 	}
 	
 	/**
 	 * Check for the database connection
	 *  
 	 */
 	@BeforeClass
 	public static void setUp() {		
 		
 		JUnitCore junit = new JUnitCore();
 		Result result = junit.run(MyBatisConnectionFactoryTest.class);
 		if(result != null){
 			assertTrue("Database is connected", true);
 		}else{
 			assertTrue("Database is not connected", false);
 			//System.exit(0);
 		}
 	}
 	
 	/**
 	 * Test case for getting the login record
 	 * using employee email id.
 	 *   
 	 */
 	@Test
 	public void testGetLoginRecord(){
 		EmployeeForm employee = new EmployeeForm();
 		employee.setEmailID("mkarun2@uic.edu");
 		EmployeeForm tempEmployee = dao.getLoginRecord(employee);
 		boolean test = (tempEmployee == null );
 		if(test){
 			assertTrue("Employee record not received or employee not present in the database", false);
 		}else{
 			assertTrue("Employee record received", true);
 		}
 	}
 	/**
 	 * Test case for getting the login record
 	 * using employee Employee ID.
 	 */
 
 	@Test
 	public void testGetLoginRecordWithEmpID() {
 		EmployeeForm employee = new EmployeeForm();
 		employee.setEmployeeID(20032);
 		EmployeeForm tempEmployee = dao.getLoginRecordWithEmpID(20032);
 		boolean test = (tempEmployee == null );
 		if(test){
 			assertTrue("Employee record not received or employee not present in the database", false);
 		}else{
 			assertTrue("Employee record received", true);
 		}
 	}
 
 	@Test
 	public void testInsertEmployeeRecord() {
 		CrsDAO crs = new CrsDAO();
 		LoginService ls = new LoginService();
 		EmployeeForm em = new EmployeeForm();
 		
 		em.setEmployeeID(20056);
 		em.setAddress("835 S Laflin");
 		em.setEmailID("rvisha2@uic.edu");
 		em.setFirstName("Rajeev Reddy");
 		em.setSalt(ls.generateSalt());
 		em.setLastName("Vishaka");
 		em.setSecurityAns("answer");
 		em.setSecurityQn("Questions");
 		em.setNotifyType(0);
 		em.setPassword(ls.generateMD5HashForPasswordWithSalt("password"));
 		em.setPhoneNo("312-361-4284");
 		em.setPoints(10);
 		
 		dao.insertEmployeeRecord(em);
 		
         
         //To test whether insert was done/
         EmployeeForm tempEmployee = dao.getLoginRecord(em);
         boolean test = (tempEmployee == null );
         if(test){
                 assertTrue("Employee record was not inserted.", false);
         }else{
                 assertTrue("Employee record was inserted", true);
         }
 	}
 /*
  * getting carpool details
  */
 	@Test
 	public void testGetCarPoolGroup() {
 		//CarPoolForm carPoolForm = new CarPoolForm();
 		CarPoolForm car = dao.getCarPoolGroup();
 		boolean test = (car == null );
 		if(test){
 			assertTrue("No carpool group retireved", false);
 		}else{
 			assertTrue("Carpool group retrieved", true);
 		}
 	}
 
 	
 	/*
 	 * Getting carpool details using carpool ID
 	 */
 	@Test
 	public void testGetCarPoolGroupDetails() {
 		CarPoolForm carform = new CarPoolForm();
 		carform.setCarpoolID(1234);
 		CarPoolForm car = dao.getCarPoolGroupDetails(1234);
 		boolean test = (car == null );
 		if(test){
 			assertTrue("No carpool group retireved", false);
 		}else{
 			assertTrue("Carpool group retrieved", true);
 		}	
 	}
 
 
 /*
  * checking member fetching using email ID
  */
 	@Test
 	public void testFetchMembersEmailID() {
 		CarPoolMemberForm carform = new CarPoolMemberForm();
 		carform.setCarpoolID(5009);
 		List car = dao.fetchMembersEmailID(5009);
 		boolean test = (car == null );
 		if(test){
 			assertTrue("No carpool group retireved", false);
 		}else{
 			assertTrue("Carpool group retrieved", true);
 		}	
 	}
 /*
  * Inserting and updating into carpool and group.checking Checkout
  */
 	@Test
 	public void testCheckOut() {
 		EmployeeForm employee = new EmployeeForm();
 		CarPoolMemberForm car = new CarPoolMemberForm();
 		employee.setEmployeeID(200013);
 		car.setCarpoolID(5009);
 		dao.checkOut(1234, 200013);
 		CarPoolForm ca = dao.getCarPoolGroupDetails(5009);
 		boolean test = (ca == null );
 		if(test){
 			assertTrue("No carpool group retireved", false);
 		}else{
 			assertTrue("Carpool group retrieved", true);
 		}	
 	}
 /*
  * testing checkin
  */
 	@Test
 	public void testCheckIn() {
 		CarPoolMemberForm car = new CarPoolMemberForm();
 		car.setCarpoolID(5009);
 		dao.checkIn(5009);
 		CarPoolForm ca = dao.getCarPoolGroupDetails(5009);
 		boolean test = (ca == null );
 		if(test){
 			assertTrue("No carpool group retireved", false);
 		}else{
 			assertTrue("Carpool group retrieved", true);
 		}	
 	}
 
 /*
  * creating a new carpool group
  * depends on the car pool ID given
  */
 	@Test
 	public void testCreateNewCarPoolGroup() {
 		CarPoolForm car = new CarPoolForm();
 		car.setAtWork(0);
 		car.setCarpoolID(5009);
 		Date dateCreated= new Date(2013, 2, 12, 10, 23, 34);
 		car.setDateCreated(dateCreated);
 		dao.createNewCarPoolGroup();
 		CarPoolForm ca = dao.getCarPoolGroupDetails(5009);
 		boolean test = (ca == null );
 		if(test){
 			assertTrue("No carpool group retireved", false);
 		}else{
 			assertTrue("Carpool group retrieved", true);
 		}
 	}
 
 	/**
 	 * Retreive all free carpool groups
 	 *   
 	 */
 	@Test
 	public void testRetrieveAllFreeCarpoolGroups() {
 		List<CarPoolMemberForm> listCarpools;
 		
 		listCarpools = dao.retrieveAllFreeCarpoolGroups();
 		boolean test = (listCarpools != null);
 		if(test){
 			
 			boolean test2 = (listCarpools.size() > 0);
 			if(test2){
 				assertTrue("Free carpools groups retrieved", true);
 			}else{
 				assertTrue("There are no free carpool groups", true);
 			}
 		}else{
 			assertTrue("Free carpools groups not retrieved.", false);
 		}
 		
 	}
 
 	/**
 	 * retrieve drivers list
 	 *   
 	 */
 	@Test
 	public void testRetrieveDrivers() {
 		ArrayList<CarPoolMemberForm> listDriver;
 		listDriver = dao.retrieveDrivers();
 		boolean test = (listDriver != null);
 		if(test){
 			boolean test2 = (listDriver.size() > 0);
 			if(test2){
 				assertTrue("Drivers retrieved", true);
 			}else{
 				assertTrue("There are no drivers", true);
 			}
 		}else{
 			assertTrue("Drivers not retrieved. Exception", false);
 		}
 	}
 	
 	
 	/*
 	 * updating the curent driver
 	 *   
 	 */
 	@Test
 	public void testUpdateCurrentDriver() {
 
 		CarPoolMemberForm cm = dao.getMemberInfo(20013);		
 		if (cm != null) {
 			dao.updateCurrentDriver(20013);
 			cm = dao.getMemberInfo(20013);
 			boolean test = (cm == null );
 			if(test){
 				assertTrue("No drivers updated", false);
 			}else{
 				
 				int isDriver = cm.getIsDriver();
 				boolean test2 = (isDriver == 1);
 				if(test2){
 					assertTrue("drivers updated", true);
 				}else{
 					assertTrue("driver not updated", false);
 				}
 			}
 		}else {
 			assertTrue("member not present", false);
 		}
 	}
 
 	/**
 	 * update next driver
 	 *   
 	 */
 	@Test
 	public void testUpdateNextDriver() {
 	
 		CarPoolMemberForm cm = dao.getMemberInfo(20013);
 		if (cm != null) {
 			dao.updateNextDriver(20013);
 			cm = dao.getMemberInfo(20013);
 
 			boolean test = (cm == null);
 			if (test) {
 				assertTrue("No drivers updated", false);
 			} else {
 
 				int isDriver = cm.getIsDriver();
 				boolean test2 = (isDriver == 1);
 				if (test2) {
 					assertTrue("drivers updated", true);
 				} else {
 					assertTrue("driver not updated", false);
 				}
 			}
 		} else {
 			assertTrue("member not present", false);
 		}
 	}
 
 	/**
 	 * Update temporary driver
 	 *   
 	 */
 	@Test
 	public void testUpdateTemporaryDriver() {
 		
 		CarPoolMemberForm cm = dao.getMemberInfo(20013);
 		if (cm != null) {
 			dao.updateTemporaryDriver(20013);
 			cm = dao.getMemberInfo(20013);
 
 			boolean test = (cm == null);
 			if (test) {
 				assertTrue("No drivers updated", false);
 			} else {
 
 				int isDriver = cm.getIsDriver();
 				int isTemp = cm.getIsTemporary();
 				boolean test2 = ((isDriver == 1) && (isTemp == 1));
 				if (test2) {
 					assertTrue("temporary driver updated", true);
 				} else {
 					assertTrue("temporary driver not updated correctly", false);
 				}
 			}
 		} else {
 			assertTrue("member not present", false);
 		}
 	}
 	
 /*
  * retrieving all the passengers \
  *
  */
 	@Test
 	public void testRetrievePassengers() {
 		List<Object> tempList = new ArrayList<Object>();;
 		CarPoolMemberForm car = new CarPoolMemberForm();
 		car.setCarpoolID(5009);
 		Date dateJoined= new Date(2013, 2, 12, 10, 23, 34);
 		car.setDateJoined(dateJoined);
 		car.setEmployeeID(20015);
 		car.setIsDriver(1);
 		car.setIsPickUp(1);
 		car.setIsTemporary(1);
 		dao.retrievePassengers(5009);
 		CarPoolForm ca = dao.getCarPoolGroupDetails(5009);
 		boolean test = (ca == null );
 		if(test){
 			assertTrue("no passengers retreived", false);
 		}else{
 			assertTrue("passengers retrieved", true);
 		}
 		
 	}
 /*
  * retrieving members 
  */
 	@Test
 	public void testRetrieveMembers() {
 		List<Object> tempList = new ArrayList<Object>();;
 		CarPoolMemberForm car = new CarPoolMemberForm();
 		car.setCarpoolID(5009);
 		Date dateJoined= new Date(2013, 2, 12, 10, 23, 34);
 		car.setDateJoined(dateJoined);
 		car.setEmployeeID(20015);
 		car.setIsDriver(1);
 		car.setIsPickUp(1);
 		car.setIsTemporary(1);
 		dao.retrieveMembers(car);
 		CarPoolForm ca = dao.getCarPoolGroupDetails(5009);
 		boolean test = (ca == null );
 		if(test){
 			assertTrue("no passengers retreived", false);
 		}else{
 			assertTrue("passengers retrieved", true);
 		}
 	}
 
 	/**
 	 * get the newly created carpool group
 	 *   
 	 */
 	@Test
 	public void testGetLatestCarpoolGroup() {
 		
 		CarPoolForm cm = dao.getLatestCarpoolGroup();
 		boolean test = (cm == null);
 		if(test){
 			assertTrue("latest carpool group not retrieved", false);
 		}else{
 			assertTrue("latest carpool group retrieved", true);
 		}
 	}
 /*
  * testing cancel pickup
  */
 	@Test
 	public void testCancelCarpoolPickUp() {
 		CarPoolMemberForm car = new CarPoolMemberForm();
 		car.setCarpoolID(5009);
 		Date dateJoined= new Date(2013, 2, 12, 10, 23, 34);
 		car.setDateJoined(dateJoined);
 		car.setEmployeeID(20015);
 		car.setIsDriver(1);
 		car.setIsPickUp(1);
 		car.setIsTemporary(1);
 		dao.cancelCarpoolPickUp(car);
 		CarPoolForm ca = dao.getCarPoolGroupDetails(5009);
 		boolean test = (ca == null );
 		if(test){
 			assertTrue("no passengers retreived", false);
 		}else{
 			assertTrue("passengers retrieved", true);
 		}
 	}
 /*
  * checking opting out of CRP
  */
 	@Test
 	public void testOptOutCrp() {
 		EmployeeForm em = new EmployeeForm();
 		em.setEmployeeID(20015);
 		dao.optOutCrp(20015);
 		EmployeeForm tempEmployee = dao.getEmployeeRecord(20015);
         boolean test = (tempEmployee == null );
         if(test){
                 assertTrue("Employee record was deleted", true);
         }else{
                 assertTrue("Employee record is still present", false);
         }
 	}
 
 	/**
 	 * cancel car pool group
 	 *   
 	 */
 	@Test
 	public void testCancelCarpoolDrive() {
 		CarPoolMemberForm carPoolMember = new CarPoolMemberForm();
 		carPoolMember.setEmployeeID(20013);
 		int beforePoints = 0;
 		CarPoolMemberForm cm = dao.getMemberInfo(20013);
 		EmployeeForm em = dao.getLoginRecordWithEmpID(20013);
 		
 		if(em != null){
 			beforePoints = em.getPoints();
 		}
 		
 		if (cm != null) {
 			
 			/*cancelled the driver*/
 			dao.cancelCarpoolDrive(carPoolMember);
 			
 			/*testing if done correctly*/
 			cm = dao.getMemberInfo(20013);
 			em = dao.getLoginRecordWithEmpID(20013);
 			
 			if (cm != null && em != null) {
 				
 				int isDriver = cm.getIsDriver();
 				int isTemp = cm.getIsTemporary();
 				int points = em.getPoints();
 				boolean test = ((isDriver == 1) && (isTemp == 2) && (points == (beforePoints - 3)));
 				if(test){
 					assertTrue("cancel driver update done successfully", true);
 				}else{
 					assertTrue("cancel driver update was not correctly done", false);
 				}
 				
 			} else {
 				assertTrue("member not present", false);
 			}
 		} else {
 			assertTrue("member not present", false);
 		}
 		
 	}
 /*
  * fetching employee details with empid
  */
 	@Test
 	public void testGetEmployeeRecord() {
 		EmployeeForm em = new EmployeeForm();
 		em.setEmployeeID(20015);
 		EmployeeForm tempEmployee = dao.getEmployeeRecord(20015);
 		boolean test = (tempEmployee == null );
         if(test){
                 assertTrue("Employee record was not present", false);
         }else{
                 assertTrue("Employee record is present", true);
         }
 	}
 /*
  * testing optiong out of carpool
  */
 	@Test
 	public void testOptOutCarpool() {
 		EmployeeForm em = new EmployeeForm();
 		em.setEmployeeID(20015);
 		dao.optOutCarpool(20015);
 		
 		CarPoolForm ca = dao.getCarPoolGroupDetails(5009);
 		
 		boolean test = (ca == null );
         if(test){
                 assertTrue("Employee record was not present", false);
         }else{
                 assertTrue("Employee record is present", true);
         }
 	}
 
 	@Test
 	public void testGetMemberInfo() {
 		EmployeeForm em = new EmployeeForm();
 		em.setEmployeeID(20015);
 		dao.getMemberInfo(20015);
 		CarPoolForm ca = dao.getCarPoolGroupDetails(5009);
 		
 		boolean test = (ca == null );
         if(test){
                 assertTrue("Employee record was not present", false);
         }else{
                 assertTrue("Employee record is present", true);
         }
 	}
 /*
  * updating user details
  */
 	@Test
 	public void testUpdateUserDetails() {
 		EmployeeForm em = new EmployeeForm();
 		em.setAddress("835 S Laflin");
 		em.setEmailID("rvisha2@uic.edu");
 		em.setEmployeeID(1);
 		em.setFirstName("Rajeev Reddy");
 		em.setLastName("Vishaka");
 		em.setNotifyType(1);
 		em.setPassword("password");
 		em.setPhoneNo("312-361-4284");
 		em.setPoints(10);
 		dao.updateUserDetails(em);
 		EmployeeForm e = dao.getEmployeeRecord(1);
 		boolean test = (e == null );
         if(test){
                 assertTrue("Employee record was not present", false);
         }else{
                 assertTrue("Employee record is present", true);
         }
 		
 	}
 
 }

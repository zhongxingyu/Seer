 package org.alt60m.ministry.bean;
 
 import java.sql.*;
 import java.util.*;
 import java.util.Date;
 //import org.alt60m.ministry.*;
 import org.alt60m.util.DBConnectionFactory;
 import org.alt60m.util.ObjectHashUtil;
 import org.alt60m.ministry.model.dbio.*;
 //import org.alt60m.util.TextUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Staff Updater
  *		
  * @author Mark Petrotta
  * 
  */
 
 public class PersonUpdater {
 	private static final List<String> STAFF_POSITIONS = setStaffPositions(); 
 	
 	private static Log log = LogFactory.getLog(PersonUpdater.class);
 	
 	private static List<String> setStaffPositions() {
 		List<String> result = new ArrayList<String>();
 		result.add("Associate Staff");
 		result.add("Full Time Staff");
 		result.add("Hourly Full Time");
 		result.add("Hourly on Call");
 		result.add("Salaried Exempt");
 		result.add("Self-Supported Staff");
 		result.add("Staff on Delayed Payroll");
 		result.add("Staff on Paid Leave");
 		result.add("Staff on Unpaid Leave");
 		result.add("Staff Raising Init Supprt");
 		return result;
 	}
 
 
     public PersonUpdater() {    
     }
     
     private Hashtable<String,String>staffPersonKeysForUpdatableTest(){
     	Hashtable<String,String>result=new Hashtable<String,String>();
     	result.put("current.address1","home.address1");
 		result.put("current.address2","home.address2");
 		result.put("current.address3","home.address3");
 		result.put("current.address4","home.address4");
 		result.put("current.city","home.city");
 		result.put("current.state","home.state");
 		result.put("current.zip","home.zip");
 		result.put("current.country","home.country");
 		
 		result.put("current.cellPhone","staff.mobilePhone");
 		result.put("current.homePhone","staff.homePhone");
 		result.put("current.workPhone","staff.workPhone");
 		result.put("current.fax","staff.fax");
 		result.put("current.email","staff.email");
 		
 		result.put("permanent.address1","mailing.address1");
 		result.put("permanent.address2","mailing.address2");
 		result.put("permanent.address3","mailing.address3");
 		result.put("permanent.address4","mailing.address4");
 		result.put("permanent.city","mailing.city");
 		result.put("permanent.state","mailing.state");
 		result.put("permanent.zip","mailing.zip");
 		result.put("permanent.country","mailing.country");
 		
 		result.put("permanent.cellPhone","staff.mobilePhone");
 		result.put("permanent.homePhone","staff.homePhone");
 		result.put("permanent.workPhone","staff.workPhone");
 		result.put("permanent.fax","staff.fax");
 		result.put("permanent.email","staff.email");
 		
 		result.put("person.firstName","staff.firstName");
 		result.put("person.lastName","staff.lastName");
 		result.put("person.preferredName","staff.preferredName");
 		
 		result.put("person.maritalStatus","staff.maritalStatus");
 		result.put("person.ministry", "staff.ministry");
 		result.put("person.region","staff.region");
 		result.put("person.strategy", "staff.strategy");
 		result.put("person.isSecure","staff.isSecure");
 		
     	return result;
     }
     private String fieldsCore(String staffField, String personField){
     	 String result=" (("+staffField+" is null or "+staffField+" <=> '') and not("+personField+" is null or "+personField+" <=> '')) "+
 		" or \r\n     ( "+
 		" not("+staffField+" is null or "+staffField+" <=> '') and ("+personField+" is null or "+personField+" <=> '') ) "+
 		" or \r\n     ( "+
 		" not("+personField+"<=>"+staffField+") ) "; //same comparator structure used both in mismatch/goodmatch return fields and where clause
     	 return result;
     }
     
     private String badFields(String staffField, String personField){
     	String result=" \r\n if("+fieldsCore(staffField,personField)+", "
 		+"concat('     "+ personField+ " [',if("+personField+" is not null,"+personField+",'') ,']  does not match "+staffField+" [',if("+staffField+" is not null,"+staffField+",''),'] and will be corrected.')"
 		+ " ,'match') as "+staffField.replace(".", "_dot_")+", ";
 		
     	return result;
    }
     private String tables="\r\n  staff.accountNo as accountNo From ministry_staff staff \r\n"+
 	"inner join ministry_address home on staff.fk_primaryAddress=home.AddressID \r\n"+ //we require both staff addresses to run this
 	"inner join ministry_address mailing on staff.fk_secondaryAddress=mailing.AddressID \r\n"+
 	"inner join ministry_person person on staff.person_id=person.personID \r\n"+
 	"left join ministry_newaddress current on (current.fk_PersonID=person.personID and current.addressType='current') \r\n"+
 	"left join ministry_newaddress permanent on (permanent.fk_PersonID=person.personID and permanent.addressType='permanent') \r\n"+
 	"where \r\n"+
 	"staff.person_id is not null and staff.person_id <>0 and staff.removedFromPeopleSoft='N' and ( \r\n";
     
   public void initiateNewPersonsFromStaffTable() throws Exception {
 	 Connection conn = DBConnectionFactory.getDatabaseConn();
 	Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 	String wherePart="(ms.person_id is null or ms.person_id=0) and ms.removedFromPeopleSoft='N' and (ms.isSecure='F' or ms.isSecure is null) ";
 	Date createdAt=new Date();
 	java.sql.Date sqlDate=new java.sql.Date(createdAt.getTime());
 	String sqlDateString=sqlDate.toString();
 	String qry="INSERT INTO ministry_person  (accountNo, firstName, lastName, createdBy, dateCreated)  SELECT lonelies.accountNo,'Temp','DeleteMe','PU','"+sqlDateString+"'  FROM "
 		+" (select ms.accountNo as accountNo from ministry_staff ms left join ministry_person mp on mp.accountNo=ms.accountNo "
 		+" left join ministry_address ma1 on ms.fk_primaryAddress=ma1.AddressID left join ministry_address ma2 on ms.fk_secondaryAddress=ma2.AddressID "
 		+" where mp.personID is null and ma1.AddressID is not null and ma2.AddressID is not null and "+wherePart+" ) lonelies ;";
 	log.debug(qry);
 	stmt.execute(qry);
 	String qry2="UPDATE ministry_staff ms inner join ministry_person mp on mp.accountNo=ms.accountNo "
 		+" set ms.person_id=mp.personID where mp.firstname='Temp' and mp.lastName='DeleteMe' and "+wherePart+" ;";
 	log.debug(qry2);
 	stmt.execute(qry2);
 	
 	conn.close();
   }
   public void update() throws Exception {
 	  initiateNewPersonsFromStaffTable();
 	  Connection conn = DBConnectionFactory.getDatabaseConn();
 		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
 		String qry=tables;
 		
 		String keyFields="";
 		String updateFields="\r\n Select ";
 		Iterator iter=staffPersonKeysForUpdatableTest().keySet().iterator();
 		while(iter.hasNext()){
 			String personField=(String)iter.next();
 			String staffField=staffPersonKeysForUpdatableTest().get(personField);
 			
 			
 			
 			updateFields+=" \r\n "+badFields(staffField,personField);
 			keyFields+=" \r\n "+fieldsCore(staffField,personField)+" or ";
 				
 		}
 		
 		keyFields+=" false ) ; ";
 		
 		qry=updateFields+qry+keyFields;
 		
 		log.info("Requesting dissynchronous Person records for staff...");		
 		log.debug(qry);
 		ResultSet rs = stmt.executeQuery(qry);
 		if(rs.isBeforeFirst())log.info("Updating dissynchronous Person table records from Staff table...");	
 		else log.info("No Person records out of synch with Staff table.");
 		ResultSetMetaData md =rs.getMetaData();
 		int columns=md.getColumnCount();
 	  while(rs.next()){
 		  Staff s=new Staff(rs.getString("accountNo"));
 		  log.info("Pushing Staff info to Person table for "+s.getAccountNo()+" "+s.getPreferredName()+" "+s.getLastName()+":");
 		  for(int i=1;i<(columns-1);i++){
 			  String label=md.getColumnLabel(i);
 			  if (label.contains("_dot_")&&(!rs.getString(label).equals("match"))){
 				  log.info(rs.getString(label));
 			  }
 		  }
 		copyStaffToPerson(s);}
 	  conn.close();
 	  log.info("PersonUpdater is finished and wants a biscuit.");
   }
   public void copyStaffToPerson(Staff staff)throws Exception {
 	  if (staff.getPersonID()>0){
 		  
 		  //person record properties
 	  Person person=new Person(staff.getPersonID());
 	  person.setAccountNo(staff.getAccountNo());
 	  person.setIsSecureString(staff.getIsSecureString());
 	  person.setFirstName(staff.getFirstName());
 	  person.setPreferredName(staff.getPreferredName());
 	  person.setLastName(staff.getLastName());
 	  
 	  person.setGender(staff.getIsMale()?"1":"0");
 	  person.setMaritalStatus(staff.getMaritalStatus());
 	  Staff spouse=new Staff(staff.getSpouseAccountNo());
 	  person.setFk_spouseID(spouse.getPersonID());
 	  
 	  person.setMinistry(staff.getMinistry());
 	  if (("NC SE MA MS GP GL NW RR UM SW NE HLIC").contains(staff.getRegion())){
 		  person.setRegion(staff.getRegion());}
 	  else {
 		  person.setRegion("");
 	  }
 	  person.setStrategy(staff.getStrategy());
 //	  person.setIsStaff(staff.getRemovedFromPeopleSoft().equals("N"));
 	  person.setIsStaff(staff.getRemovedFromPeopleSoft().equals("N") && STAFF_POSITIONS.contains(staff.getJobStatus()));
 	  java.text.SimpleDateFormat bDayFormatter = new java.text.SimpleDateFormat("MM/dd/yyyy");
 	  if (staff.getBirthDate()!=null)
 	  {person.setBirthDate(bDayFormatter.format(staff.getBirthDate())); }
 	  else
 	  { person.setBirthDate("");}
 	  person.setDateChanged(new java.util.Date());
 	  person.setToolName("PU");
 	  person.persist();
 	  
 	  Address current=new Address();
 	  current.setFk_PersonID(person.getPersonID());
 	  current.setAddressType("current");
 	  current.select();
 	  OldAddress home= new OldAddress(staff.getPrimaryAddressId());
 	  pushAddress (home, current, staff);
 	  
 	  Address permanent=new Address();
 	  permanent.setFk_PersonID(person.getPersonID());
 	  permanent.setAddressType("permanent");
 	  permanent.select();
 	  OldAddress mailing= new OldAddress(staff.getSecondaryAddressId());
 	  pushAddress (mailing, permanent, staff);
 	   
 	 
 	  
 	  } 
   }
   private void pushAddress (OldAddress staffAdd, Address personAdd, Staff staff){
 	personAdd.setAddress1(staffAdd.getAddress1());
 	personAdd.setAddress2(staffAdd.getAddress2());
 	personAdd.setAddress3(staffAdd.getAddress3());
 	personAdd.setAddress4(staffAdd.getAddress4());
 	personAdd.setCity(staffAdd.getCity());
 	personAdd.setState(staffAdd.getState());
 	personAdd.setZip(staffAdd.getZip());
 	personAdd.setCountry(staffAdd.getCountry());
 	personAdd.setCity(staffAdd.getCity());
 	
 	personAdd.setHomePhone(staff.getHomePhone());
 	personAdd.setWorkPhone(staff.getWorkPhone());
 	personAdd.setCellPhone(staff.getMobilePhone());
 	personAdd.setFax(staff.getFax());
 	personAdd.setEmail(staff.getEmail());
 	personAdd.setDateChanged(new java.util.Date());
 	personAdd.setToolName("PU");
 	personAdd.persist();
 	}
 	
 }

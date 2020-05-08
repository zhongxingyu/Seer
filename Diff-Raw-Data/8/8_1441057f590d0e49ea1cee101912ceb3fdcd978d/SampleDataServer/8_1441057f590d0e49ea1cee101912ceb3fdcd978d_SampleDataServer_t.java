 package com.prioritytech.meteor;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorDataResponse;
 import org.nchelp.meteor.message.response.AddressInfo;
 import org.nchelp.meteor.message.response.Award;
 import org.nchelp.meteor.message.response.Borrower;
 import org.nchelp.meteor.message.response.ConsolLender;
 import org.nchelp.meteor.message.response.Contacts;
 import org.nchelp.meteor.message.response.DataProviderAggregateTotal;
 import org.nchelp.meteor.message.response.DataProviderData;
 import org.nchelp.meteor.message.response.Default;
 import org.nchelp.meteor.message.response.DisbursingAgent;
 import org.nchelp.meteor.message.response.Employer;
 import org.nchelp.meteor.message.response.Guarantor;
 import org.nchelp.meteor.message.response.Lender;
 import org.nchelp.meteor.message.response.MeteorDataErrors;
 import org.nchelp.meteor.message.response.MeteorDataProviderAwardDetails;
 import org.nchelp.meteor.message.response.MeteorDataProviderDetailInfo;
 import org.nchelp.meteor.message.response.MeteorDataProviderInfo;
 import org.nchelp.meteor.message.response.MeteorRsMsg;
 import org.nchelp.meteor.message.response.OrgType;
 import org.nchelp.meteor.message.response.PersonType;
 import org.nchelp.meteor.message.response.Reference;
 import org.nchelp.meteor.message.response.Repayment;
 import org.nchelp.meteor.message.response.School;
 import org.nchelp.meteor.message.response.Servicer;
 import org.nchelp.meteor.message.response.Student;
 import org.nchelp.meteor.provider.data.DataServerAbstraction;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 
 /**
 * This is a sample implementation of how to tie the Index Provider
 * Meteor code with your proprietary backend system.  All code in 
 * the classes that implement IndexServerAbstraction are excluded 
 * from the normal licensing terms of Meteor.  That means you can
 * put proprietary code here and not have to release it to anyone.
 * 
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class SampleDataServer implements DataServerAbstraction {
 
 	private final Logger log = Logger.create(this.getClass());
 
 	private static Connection conn = null;
 	/*
 	 * @see DataServerAbstraction#getData(String, Date)
 	 */
 	public MeteorDataResponse getData(SecurityToken security, String ssn) {
 		MeteorDataResponse dataResp = new MeteorDataResponse();
 		MeteorRsMsg msg = dataResp.getRsMsg();
 
 		MeteorDataProviderInfo mdpi = new MeteorDataProviderInfo();
 		msg.addMeteorDataProviderInfo(mdpi);
 		
 		MeteorDataProviderDetailInfo mdpdi = this.getMeteorDataProviderDetailInfo();
 		mdpi.setMeteorDataProviderDetailInfo(mdpdi);
 
		
 		conn = this.getConnection();
 		
 		if(conn == null){
 			// something went wrong.  
 			// So there isn't much else I can do here
 			MeteorDataErrors errs = new MeteorDataErrors();
			msg.addMeteorDataErrors(errs);
 			errs.setErrorMessage("Error Connecting to Database");
 			
 			return this.createMinimalResponse(dataResp);
 		}		
 		
 		this.addAwards(mdpi, ssn);
		return this.createMinimalResponse(dataResp);
 	}
 	
 	private Connection getConnection(){
 		if(conn != null){
 			// If there's already one there then use that
 			return conn;
 		}
 		
 		Resource res = ResourceFactory.createResource("dataprovider.properties");
 	    String jdbcClass = res.getProperty("com.prioritytech.sample.JDBCClass");
 	    String jdbcURL = res.getProperty("com.prioritytech.sample.JDBCURL");
 	    String jdbcUserID = res.getProperty("com.prioritytech.sample.JDBCUserID");
 	    String jdbcPassword = res.getProperty("com.prioritytech.sample.JDBCPassword");
 
 		Connection con = null;
 		
 		try{
 		    Class.forName(jdbcClass).newInstance();
 	    	con = DriverManager.getConnection(jdbcURL, jdbcUserID, jdbcPassword);
 		} catch(ClassNotFoundException e){
 			log.error("Class not found: '" + jdbcClass + "'", e);
 		} catch(SQLException e){
 			log.error("Error connecting to the database: '" + jdbcURL + "'", e);
 		} catch(Exception e){
 			log.error("Error creating connection object: ", e);
 		}
 
 		try{
 			con.setReadOnly(true);
 			con.setAutoCommit(true);
 		} catch(NullPointerException e){
 			// hapens if the previous code threw an exception
 		} catch(SQLException e){
 			log.error("Error setting database options", e);
 		}
 
 		return con;	
 	}	
 
 	private void addAwards(MeteorDataProviderInfo mdpi, String ssn){
 		ResultSet rs = null;
 
 		MeteorDataProviderAwardDetails mdpad = new MeteorDataProviderAwardDetails();
 		mdpi.setMeteorDataProviderAwardDetails(mdpad);
 
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select a.* from \"awarddata\" a, \"person\" p where a.borrower_person_id = p.person_id and p.ssn = '" + ssn + "'"; 
 			log.debug("Executing SQL: " + sql);
 			rs = s.executeQuery(sql);
 			conn.commit();
 			rs.next();
 			while(! rs.isAfterLast() ){
 
 				Award awd = new Award();
 				mdpad.addAward(awd);
 
 				String  strColumn = null;
 				double dblColumn = 0;
 				int    iColumn = 0;
 				
 				awd.setDataProviderType(rs.getString("data_provider_type"));
 				
 				strColumn = rs.getString("award_type");
 				if(strColumn != null){ awd.setAwardType(strColumn.trim()); }
 				
 				awd.setAwardAmt(new BigDecimal(rs.getDouble("award_amt")).setScale(2, BigDecimal.ROUND_DOWN));
 				
 				awd.setAwardBeginDt(new org.exolab.castor.types.Date(rs.getDate("award_begin_dt")));
 				awd.setAwardEndDt(new org.exolab.castor.types.Date(rs.getDate("award_end_dt")));
 				
 				if(strColumn != null) { awd.setGradeLevelInd(rs.getString("grade_level_ind")); }
 				
 				if(strColumn != null) { awd.setLoanStat(rs.getString("loan_stat")); }
 				
 				awd.setLoanStatDt(new org.exolab.castor.types.Date(rs.getDate("loan_stat_dt")));
 				
 				if(strColumn != null){ awd.setMPNInd(rs.getString("mpn_ind")); }
 				
 				strColumn = rs.getString("esign");
 				if(strColumn != null){ awd.setEsign(strColumn.equalsIgnoreCase("y") ? true : false); }
 				
 				awd.setCommonlineError(rs.getString("commonline_error"));
 				
 				awd.setGuarDt(new org.exolab.castor.types.Date(rs.getDate("guar_dt")));
 				
 				iColumn = rs.getInt("student_person_id");
 				if(iColumn != 0) awd.setStudent((Student)this.getPersonType(iColumn, new Student()));
 				
 				iColumn = rs.getInt("borrower_person_id");
 				if(iColumn != 0) awd.setBorrower((Borrower)this.getPersonType(iColumn, new Borrower()));
 				
 				iColumn = rs.getInt("reference_1_person_id");
 				if(iColumn != 0) awd.addReference((Reference)this.getPersonType(iColumn, new Reference()));
 				iColumn = rs.getInt("reference_2_person_id");
 				if(iColumn != 0) awd.addReference((Reference)this.getPersonType(iColumn, new Reference()));
 				
 				iColumn = rs.getInt("disbursing_agent_org_id");
 				if(iColumn != 0) awd.setDisbursingAgent((DisbursingAgent)this.getOrgType(iColumn, new DisbursingAgent()));
 				
 				iColumn = rs.getInt("lender_org_id");
 				if(iColumn != 0) awd.setLender((Lender)this.getOrgType(iColumn, new Lender()));
 				
 				iColumn = rs.getInt("servicer_org_id");
 				if(iColumn != 0) awd.setServicer((Servicer)this.getOrgType(iColumn, new Servicer()));
 				
 				iColumn = rs.getInt("consol_lender_org_id");
 				if(iColumn != 0) awd.setConsolLender((ConsolLender)this.getOrgType(iColumn, new ConsolLender()));
 				
 				iColumn = rs.getInt("school_org_id");
 				if(iColumn != 0) awd.setSchool((School)this.getOrgType(iColumn, new School()));
 				
 				iColumn = rs.getInt("guarantor_org_id");
 				if(iColumn != 0) awd.setGuarantor((Guarantor)this.getOrgType(iColumn, new Guarantor()));
 				
 				awd.setAwardId(rs.getString("award_id"));
 				
 				iColumn = rs.getInt("repayment_id");
 				if(iColumn != 0) awd.setRepayment(this.getRepayment(iColumn));
 				
 				iColumn = rs.getInt("default_id");
 				if(iColumn != 0) mdpad.addDefault(this.getDefault(iColumn));
 
 				rs.next();	
 			}
 			
 		} catch(SQLException e) {
 			log.warn("SQL Exception: ", e);
 		}
 		
 		
 	}
 	
 	private MeteorDataProviderDetailInfo getMeteorDataProviderDetailInfo(){
 		MeteorDataProviderDetailInfo mdpdi = new MeteorDataProviderDetailInfo();
 		
 		DataProviderData dpd = new DataProviderData();
 		mdpdi.setDataProviderData(dpd);
 
 		Contacts cont = new Contacts();
 		dpd.setContacts(cont);
 		
 
 		Resource res = ResourceFactory.createResource("dataprovider.properties");
 
 		dpd.setEntityName(res.getProperty("DataProvider.Data.Name"));
 		String id = res.getProperty("DataProvider.Data.ID");
 		if(id != null && ! id.equals("")) dpd.setEntityID(id);
 		dpd.setEntityURL(res.getProperty("DataProvider.Data.URL"));
 		dpd.setDataProviderType(res.getProperty("DataProvider.Data.Type"));
 		
 		
 		cont.addWorkPhoneNum(res.getProperty("DataProvider.Data.Contacts.PhoneNum"));
 		cont.setEmail(res.getProperty("DataProvider.Data.Contacts.Email"));
 		
 		AddressInfo addr = new AddressInfo();
 		String val = res.getProperty("DataProvider.Data.Contacts.Addr");
 		if(val != null) addr.addAddr(val);
 		
 		val = res.getProperty("DataProvider.Data.Contacts.Addr2");
 		if(val != null) addr.addAddr(val);
 		val = res.getProperty("DataProvider.Data.Contacts.Addr3");
 		if(val != null) addr.addAddr(val);
 		val = res.getProperty("DataProvider.Data.Contacts.City");
 		if(val != null) addr.setCity(val);
 		val = res.getProperty("DataProvider.Data.Contacts.StateProv");
 		if(val != null) addr.setStateProv(val);
 		val = res.getProperty("DataProvider.Data.Contacts.PostalCd");
 		if(val != null) addr.setPostalCd(val);
 
 		cont.setAddressInfo(addr);
 		DataProviderAggregateTotal dpat = new DataProviderAggregateTotal();
 		mdpdi.setDataProviderAggregateTotal(dpat);
 		
 		return mdpdi;
 	}
 	
 	private PersonType getPersonType(int personID, PersonType person){
 		ResultSet rs = null;
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select * from \"person\" where person_id = " + personID; 
 			log.debug("Executing SQL: " + sql);
 			rs = s.executeQuery(sql);
 			conn.commit();
 			rs.next();
 			while(! rs.isAfterLast() ){
 				String strColumn = null;
 				
 				strColumn = rs.getString("last_name");
 				if(strColumn != null) person.setLastName(strColumn.trim());
 				
 				strColumn = rs.getString("first_name");
 				if(strColumn != null) person.setFirstName(strColumn.trim());
 				
 				strColumn = rs.getString("middle_initial");
 				if(strColumn != null) person.setMiddleInitial(strColumn.trim());
 				
 				person.setSSNum(new BigDecimal(rs.getString("ssn")));
 				
 				person.setDriversLicense(rs.getString("drivers_license"));
 				strColumn = rs.getString("drivers_license_state");
 				if(strColumn != null) person.setDriversLicenseState(strColumn);
 				
 				java.sql.Date dtColumn = null;
 				
 				dtColumn = rs.getDate("birth_dt");
 				if(dtColumn != null) person.setDtOfBirth(new org.exolab.castor.types.Date(dtColumn));
 				dtColumn = rs.getDate("grad_dt");
 				if(dtColumn != null) person.setGradDt(new org.exolab.castor.types.Date(dtColumn));
 			
 				int iColumn = 0;
 				
 				iColumn = rs.getInt("contact_id");
 				if(iColumn != 0){ person.setContacts(this.getContacts(iColumn)); }
 				
 				iColumn = rs.getInt("employer_org_id");
 				if(iColumn != 0){ person.setEmployer((Employer)this.getOrgType(iColumn, new Employer())); }
 
 				rs.next();
 			}
 
 		}catch(SQLException e){
 			log.warn("Error retrieveing data from Person table", e);
 		}
 			
 		
 		return person;	
 	}
 	
 	private OrgType getOrgType(int orgID, OrgType org){
 		ResultSet rs = null;
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select * from \"organization\" where org_id = " + orgID; 
 			log.debug("Executing SQL: " + sql);
 			rs = s.executeQuery(sql);
 			conn.commit();
 			rs.next();
 			while(! rs.isAfterLast() ){
 		
 				org.setEntityName(rs.getString("entity_name"));
 				org.setEntityURL(rs.getString("entity_url"));
 				org.setEntityID(rs.getString("entity_id"));
 				org.setDataProviderType(rs.getString("data_provider_type").trim());
 				
 				int iColumn = rs.getInt("contact_id");
 				if(iColumn != 0) {
 					org.setContacts(this.getContacts(iColumn));
 				} else {
 					org.setContacts(new Contacts());
 				}
 				
 				rs.next();
 			}
 		}catch(SQLException e){
 			log.warn("Error retrieveing data from organization table", e);
 		}
 		
 		return org;
 	}
 	
 	private Repayment getRepayment(int repaymentID){
 		return new Repayment();
 	}
 	
 	private Default getDefault(int defaultID){
 		return new Default();
 	}
 	
 	private Contacts getContacts(int contactID){
 		Contacts contacts = new Contacts();
 		ResultSet rs = null;
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select * from \"contact\" where contact_id = " + contactID; 
 			log.debug("Executing SQL: " + sql);
 			rs = s.executeQuery(sql);
 			conn.commit();
 			rs.next();
 			while(! rs.isAfterLast() ){
 				String strColumn = null;
 				
 				strColumn = rs.getString("phone_work");
 				if(strColumn != null) contacts.addWorkPhoneNum(strColumn.trim());
 				
 				strColumn = rs.getString("phone_secondary");
 				if(strColumn != null) contacts.addSecondaryPhoneNum(strColumn.trim());
 
 				strColumn = rs.getString("phone_home");
 				if(strColumn != null) contacts.addHomePrimaryPhoneNum(strColumn.trim());
 
 				AddressInfo address = new AddressInfo();
 				contacts.setAddressInfo(address);
 				
 				strColumn = rs.getString("address_type");
 				if(strColumn != null) address.setAddressType(strColumn);
 				
 				strColumn = rs.getString("addr_valid_ind");
 				if(strColumn != null) address.setAddrValidInd(strColumn.equalsIgnoreCase("y") ? true : false);
 
 				java.sql.Date dtColumn = rs.getDate("addr_valid_dt");
 				if(dtColumn != null) address.setAddrValidDt(new org.exolab.castor.types.Date(dtColumn));
 				
 				strColumn = rs.getString("address_1");
 				if(strColumn != null) address.addAddr(strColumn.trim());
 				
 				strColumn = rs.getString("address_2");
 				if(strColumn != null) address.addAddr(strColumn.trim());
 				
 				strColumn = rs.getString("address_3");
 				if(strColumn != null) address.addAddr(strColumn.trim());
 				
 				strColumn = rs.getString("city");
 				if(strColumn != null) address.setCity(strColumn.trim());
 				
 				strColumn = rs.getString("state");
 				if(strColumn != null) address.setStateProv(strColumn);
 				
 				strColumn = rs.getString("postal_code");
 				if(strColumn != null) address.setPostalCd(strColumn.trim());
 				
 				strColumn = rs.getString("email");
 				if(strColumn != null) contacts.setEmail(strColumn);
 				
 				rs.next();
 			}
 		}catch(SQLException e){
 			log.warn("Error retrieveing data from contact table", e);
 		}
 		
 		return contacts;
 	}
 	
 	private MeteorDataResponse createMinimalResponse(MeteorDataResponse resp){
 		
 		MeteorRsMsg msg = resp.getRsMsg();
 		
 		MeteorDataProviderInfo mdpi = null;
 		if(msg.getMeteorDataProviderInfoCount() < 1){
 			mdpi = new MeteorDataProviderInfo();
 			msg.addMeteorDataProviderInfo(mdpi);
 		} else { 
 			mdpi = msg.getMeteorDataProviderInfo()[0];
 		}
 		
 		MeteorDataProviderDetailInfo mdpdi = mdpi.getMeteorDataProviderDetailInfo();
 		if(mdpdi == null){
 			mdpdi = new MeteorDataProviderDetailInfo();
 			mdpi.setMeteorDataProviderDetailInfo(mdpdi);
 		}
 		
 		DataProviderData dpd = mdpdi.getDataProviderData();
 		if(dpd == null){
 			dpd = new DataProviderData();
 			mdpdi.setDataProviderData(dpd);
 		}
 		
 		Contacts contacts = dpd.getContacts();
 		if(contacts == null){
 			contacts = new Contacts();
 			dpd.setContacts(contacts);
 		}
 		
 		DataProviderAggregateTotal dpat = mdpdi.getDataProviderAggregateTotal();
 		if(dpat == null){
 			dpat = new DataProviderAggregateTotal();
 			mdpdi.setDataProviderAggregateTotal(dpat);
 		}
 		
 		
 		return resp;	
 	}
 }
 

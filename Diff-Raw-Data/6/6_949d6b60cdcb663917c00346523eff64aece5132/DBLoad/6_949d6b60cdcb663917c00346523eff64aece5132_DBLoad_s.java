 /**
  *
  * Copyright 2002 NCHELP
  *
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  *
  *
  * This code is part of the Meteor system as defined and specified
  * by the National Council of Higher Education Loan Programs, Inc.
  * (NCHELP) and the Meteor Sponsors, and developed by Priority
  * Technologies, Inc. (PTI).
  *
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.math.BigDecimal;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import org.exolab.castor.types.Date;
 import org.nchelp.hpc.util.FileUtils;
 import org.nchelp.meteor.message.MeteorDataResponse;
 import org.nchelp.meteor.message.response.AddressInfo;
 import org.nchelp.meteor.message.response.Award;
 import org.nchelp.meteor.message.response.BorrowerType;
 import org.nchelp.meteor.message.response.Contacts;
 import org.nchelp.meteor.message.response.Default;
 import org.nchelp.meteor.message.response.Disbursement;
 import org.nchelp.meteor.message.response.MeteorDataProviderAwardDetails;
 import org.nchelp.meteor.message.response.MeteorDataProviderInfo;
 import org.nchelp.meteor.message.response.MeteorRsMsg;
 import org.nchelp.meteor.message.response.OrgType;
 import org.nchelp.meteor.message.response.PaymentType;
 import org.nchelp.meteor.message.response.PersonType;
 import org.nchelp.meteor.message.response.Phone;
 import org.nchelp.meteor.message.response.Repayment;
 import org.nchelp.meteor.message.response.StudentType;
 import org.nchelp.meteor.util.exception.ParsingException;
 
 
 public class DBLoad {
     private static String filename;
 
     private String propertiesFile = "dbload.properties";
     private Properties props;
 
     private int awardID;
     private int personID;
     private int contactID;
     private int orgID;
     private int paymentID;
     private int repaymentID;
     private int disbursementID;
     private int defaultID;
 
 	public static void main(String args[]) {
         filename = args[0];
 
         DBLoad db = new DBLoad();
 
         try {
 			db.saveXML(filename);
 		} catch(ParsingException e) {
 			e.printStackTrace();
 		}
 
 		db.saveCounters();
 	}
 
 	public DBLoad (){
 		props = new Properties();
 
 		try {
 			props.load(new FileInputStream(this.propertiesFile));
 		} catch(FileNotFoundException e) {
 			return;
 		} catch(IOException e) {
 			return;
 		}
 
  		this.awardID = Integer.parseInt(props.getProperty("awardID", "0"));
 		this.personID = Integer.parseInt(props.getProperty("personID", "0"));
 		this.contactID = Integer.parseInt(props.getProperty("contactID", "0"));;
     	this.orgID = Integer.parseInt(props.getProperty("orgID", "0"));;
     	this.paymentID = Integer.parseInt(props.getProperty("paymentID", "0"));;
     	this.repaymentID = Integer.parseInt(props.getProperty("repaymentID", "0"));;
     	this.disbursementID = Integer.parseInt(props.getProperty("disbursementID", "0"));;
     	this.defaultID = Integer.parseInt(props.getProperty("defaultID", "0"));;
 
 	}
 
 	public void saveCounters(){
  		props.setProperty("awardID", Integer.toString(this.awardID));
  		props.setProperty("personID", Integer.toString(this.personID));
  		props.setProperty("contactID", Integer.toString(this.contactID));
  		props.setProperty("orgID", Integer.toString(this.orgID));
  		props.setProperty("paymentID", Integer.toString(this.paymentID));
  		props.setProperty("repaymentID", Integer.toString(this.repaymentID));
  		props.setProperty("disbursementID", Integer.toString(this.disbursementID));
  		props.setProperty("defaultID", Integer.toString(this.defaultID));
 
 		try {
 			props.list(new PrintStream(new FileOutputStream(this.propertiesFile)));
 		} catch(FileNotFoundException e) {
 		}
 
 	}
 
 	public void saveXML(String filename) throws ParsingException{
 
 		String xml = null;
 		String dir = "./";
 
 		try{
 			xml = new String(FileUtils.readFile(dir + filename));
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 
 		try{
 			// This makes sure that it is valid XML too
 		    MeteorDataResponse resp = new MeteorDataResponse(xml);
 
 			MeteorRsMsg msg = resp.getRsMsg();
 
 			printProviderType(msg);
 
 			int count = msg.getMeteorDataProviderInfoCount();
 
 			for(int i = 0; i < count; i++){
 				MeteorDataProviderInfo mdpi = msg.getMeteorDataProviderInfo(i);
 				MeteorDataProviderAwardDetails mdpad = mdpi.getMeteorDataProviderAwardDetails();
 
 				for(int j = 0; j < mdpad.getAwardCount(); j++){
 					Award awd = mdpad.getAward(j);
 
 					this.printAward(awd);
 				}
 			}
 
 		} catch(ParsingException e){
             System.err.println("Error parsing " + filename + ", " + e.getMessage());
 			//e.printStackTrace();
 			System.exit(0);
 		}
 
 	}
 
 	private void printProviderType( MeteorRsMsg msg){
 		String type = "";
 		try{
 			type = msg.getMeteorDataProviderInfo(0).getMeteorDataProviderDetailInfo().getDataProviderType();
 		} catch(NullPointerException e){
 			type = "Unknown";
 		}
 
 		System.out.println("/* Data Provider Type: " + type + " */");
 	}
 
 	private void printAward(Award awd){
 		if(awd == null) return;
 
 		awardID++;
 
 		int borrowerID = this.printPerson(awd.getBorrower());
 		int studentID = this.printPerson(awd.getStudent());
 		int referenceID1 = 0;
 		int referenceID2 = 0;
 		int defaultID = 0;
 
 		for(int i = 0; i < awd.getReferenceCount(); i++){
 			int id = this.printPerson(awd.getReference(i));
 
 			if(i == 0) { referenceID1 = id; }
 			if(i == 1) { referenceID2 = id; }
 		}
 
		repaymentID = this.printRepayment(awd.getRepayment());
 
 		for(int k = 0; k < awd.getDisbursementCount(); k++){
 			printDisbursements(awd.getDisbursement(k), awardID);
 		}
 
 		int disbursingAgentID = printOrg(awd.getDisbursingAgent(), "");
 		int lenderID = printOrg(awd.getLender(), "");
 		int servicerID = printOrg(awd.getServicer(), "");
 		int consolLenderID = printOrg(awd.getConsolLender(), "");
 		int schoolID = printOrg(awd.getSchool(), "");
 		int guarantorID = printOrg(awd.getGuarantor(), "");
 
 		Default def = awd.getDefault();
 		if(def != null){
 			this.printDefault(def);
 			defaultID = this.defaultID;
 		}
 
 		String sql = "INSERT INTO awarddata  ( award_data_id, data_provider_type, award_type, award_amt, award_begin_dt, award_end_dt, grade_level_ind, loan_stat, loan_stat_dt, mpn_ind, esign, commonline_error, guar_dt, student_person_id, borrower_person_id, reference_1_person_id, reference_2_person_id, disbursing_agent_org_id, lender_org_id, servicer_org_id, consol_lender_org_id, school_org_id, guarantor_org_id, award_id, repayment_id, default_id) values( " +
 		             awardID + ", " +
 		             dbCol(awd.getDataProviderType()) + ", " +
 		             dbCol(awd.getAwardType()) + ", " +
 		             decCol(awd.getAwardAmt()) + ", " +
 		             dbCol(awd.getAwardBeginDt()) + ", " +
 		             dbCol(awd.getAwardEndDt()) + ", " +
 		             dbCol(awd.getGradeLevelInd()) + ", " +
 		             dbCol(awd.getLoanStat()) + ", " +
 		             dbCol(awd.getLoanStatDt()) + ", " +
 		             dbCol(awd.getMPNInd()) + ", " +
 		             dbCol((awd.getEsign()) ? "Y" : "N") + ", " +
 		             dbCol(awd.getCommonlineError()) + ",  " +
 		             dbCol(awd.getGuarDt()) + ", " +
 		             studentID + ", " +
 		             borrowerID + ", " +
 		             referenceID1 + ", " +
 		             referenceID2 + ", " +
 		             disbursingAgentID + ", " +
 		             lenderID + ", " +
 		             servicerID + ", " +
 		             consolLenderID + ", " +
 		             schoolID + ", " +
 		             guarantorID + ", " +
 		             dbCol(awd.getAwardId()) + ", " +
 		             repaymentID + ", " +
 		             defaultID + " ); ";
 		System.out.println(sql);
 
 	}
 
 	private int printPerson(PersonType p){
 		if(p == null) return 0;
 
 		personID ++;
 
         Contacts c = p.getContacts();
 		int contactID = this.printContact(c);
 
 		OrgType o = p.getEmployer();
 		int orgID = this.printOrg(o, "");
 
         Hashtable columns = new Hashtable();
 
 		if(p instanceof BorrowerType){
 			BorrowerType b = (BorrowerType)p;
 			columns.put("drivers_license", dbCol(b.getDriversLicense()));
 			columns.put("drivers_license_state", dbCol(b.getDriversLicenseState()));
 		} else {
 			columns.put("drivers_license", "null");
 			columns.put("drivers_license_state", "null");
 		}
 
 		if(p instanceof StudentType){
 			StudentType s = (StudentType)p;
 			columns.put("grad_dt", dbCol(s.getGradDt()));
 		} else {
 			columns.put("grad_dt", "null");
 		}
 
 		String ssn = p.getSSNum();
 
 		// not the fastest way to do this but it works for now
 		while(ssn.length() < 9){
 			ssn = "0".concat(ssn);
 		}
 
 
 		String sql = "INSERT INTO person (person_id, last_name, first_name, middle_initial, ssn, drivers_license, drivers_license_state, birth_dt, grad_dt, contact_id, employer_org_id) " +
 		             " values ( " + personID + ", " +
 		             dbCol(p.getLastName()) + ", " +
 		             dbCol(p.getFirstName()) + ", " +
 		             dbCol(p.getMiddleInitial()) + ", " +
 		             dbCol(ssn) + ", " +
 		             columns.get("drivers_license") + ", " +
 		             columns.get("drivers_license_state") + ", " +
 		             dbCol(p.getDtOfBirth()) + ", " +
 		             columns.get("grad_dt") + ", " +
 		             contactID + ", " +
 		             orgID + ");";
 		System.out.println(sql);
 
 		return personID;
 
 	}
 
     private int printContact( Contacts c){
     	if(c == null) return 0;
 
     	contactID++;
 
         AddressInfo a = c.getAddressInfo();
 
         Hashtable columns = new Hashtable();
 
 		columns.put("contact_id", Integer.toString(contactID));
         columns.put("phone_1", "null");
         columns.put("phone_type_1", "null");
         columns.put("phone_2", "null");
         columns.put("phone_type_2", "null");
         columns.put("phone_3", "null");
         columns.put("phone_type_3", "null");
         columns.put("phone_4", "null");
         columns.put("phone_type_4", "null");
         columns.put("address_type", dbCol(a.getAddressType()));
         columns.put("address_1", "null");
         columns.put("address_2", "null");
         columns.put("address_3", "null");
         columns.put("city", dbCol(a.getCity()));
         columns.put("state", dbCol(a.getStateProv()));
         columns.put("postal_code", dbCol(a.getPostalCd()));
         columns.put("email", dbCol(c.getEmail()));
         columns.put("addr_valid_ind", dbCol(a.getAddrValidInd()));
         columns.put("addr_valid_dt", dbCol(a.getAddrValidDt()));
 
 
         for(int i = 0; i < c.getPhoneCount(); i++){
             Phone ph = c.getPhone(i);
             columns.put("phone_" + (i + 1), dbCol(ph.getPhoneNum()));
             columns.put("phone_type_" + (i + 1), dbCol(ph.getPhoneNumType()));
         }
 
 
       	for(int i = 0; i < a.getAddrCount(); i++){
       		columns.put("address_" + (i + 1), dbCol(a.getAddr(i)));
       	}
 
         String sql = "INSERT INTO contact (contact_id, phone_1, phone_type_1, phone_2, phone_type_2, phone_3, phone_type_3, phone_4, phone_type_4, address_type, address_1, address_2, address_3, city, state, postal_code, email, addr_valid_ind, addr_valid_dt) " +
                      " values (   " +
 					   contactID + ", " +
 					   columns.get("phone_1") + ", " +
 					   columns.get("phone_type_1") + ", " +
 					   columns.get("phone_2") + ", " +
 					   columns.get("phone_type_2") + ", " +
 					   columns.get("phone_3") + ", " +
 					   columns.get("phone_type_3") + ", " +
 					   columns.get("phone_4") + ", " +
 					   columns.get("phone_type_4") + ", " +
 					   columns.get("address_type") + ", " +
 					   columns.get("address_1") + ", " +
 					   columns.get("address_2") + ", " +
 					   columns.get("address_3") + ", " +
 					   columns.get("city") + ", " +
 					   columns.get("state") + ", " +
 					   columns.get("postal_code") + ", " +
 					   columns.get("email") + ", " +
 					   columns.get("addr_valid_ind") + ", " + 
 					   columns.get("addr_valid_dt") + ");";
 
 		System.out.println(sql);
 
         return contactID;
     }
 
 	private int printOrg(OrgType o, String dataProviderType){
 		if(o == null) return 0;
 
 		orgID++;
 
 		int contID = printContact(o.getContacts());
 		String sql = "INSERT INTO organization (org_id, entity_name, entity_id, entity_url, data_provider_type, contact_id) values ( " +
 		             orgID + ", " +
 		             dbCol(o.getEntityName()) + ", " +
 		             dbCol(o.getEntityID()) + ", " +
 		             dbCol(o.getEntityURL()) + ", " +
 		             dbCol(dataProviderType) + ", " +
 		             contID + ");";
 
 		System.out.println(sql);
 		return orgID;
 	}
 
 	private int printRepayment(Repayment repay){
 		if(repay == null) return 0;
 
 		repaymentID++;
 
 		String sql = "INSERT INTO repayment ( repayment_id, next_payment_amt, next_due_dt, account_balance, account_balance_dt, payment_begin_dt, current_int_rate, repaid_principal_amt, capitalized_int_amt) values ( " +
 					repaymentID + ", " +
 					decCol(repay.getNextPmtAmt()) + ", " +
 					dbCol(repay.getNextDueDt()) + ", " +
 					decCol(repay.getAcctBal()) + ", " +
 					dbCol(repay.getAcctBalDt()) + ", " +
 					dbCol(repay.getPmtBeginDt()) + ", " +
 					decCol(repay.getCurrIntRate(), 3) + ", " +
 					decCol(repay.getRepaidPrincipalAmt()) + ", " +
 					decCol(repay.getCapitalizedIntAmt()) + ");";
 
 		System.out.println(sql);
 
 		for(int i = 0; i < repay.getLastPmtCount(); i++){
 			printPayment(repay.getLastPmt(i), repaymentID);
 		}
 		return repaymentID;
 	}
 
 	private void printPayment(PaymentType pmt, int repaymentID){
 		if(pmt == null) return;
 
 		paymentID++;
 
 		String sql = "INSERT INTO payment (payment_id, repayment_id, payment_amt, payment_dt) values ( " +
 					paymentID + ", " +
 					repaymentID + ", " +
 					decCol(pmt.getPaymentAmt()) + ", " +
 					dbCol(pmt.getPaymentDt()) + ");";
 
 		System.out.println(sql);
 		return;
 	}
 
 	private void printDisbursements(Disbursement disb, int awardID){
 		if(disb == null) return;
 
 		disbursementID++;
 
 		String sql = "INSERT INTO disbursement  ( disbursement_id, award_id, disb_seq_num, sched_disb_dt, actual_disb_dt, disb_net_amt, disb_stat_code, disb_stat_dt,	disb_hold) values ( " +
 		             disbursementID + ", " +
 		             awardID + ", " +
 		             decCol(disb.getDisbSeqNum(), 0) + ", " +
 		             dbCol(disb.getSchedDisbDt()) + ", " +
 		             dbCol(disb.getActualDisbDt()) + ", " +
 		             decCol(disb.getDisbNetAmt()) + ", " +
 		             dbCol(disb.getDisbStatCd()) + ", " +
 		             dbCol(disb.getDisbStatDt()) + ", " +
 		             dbCol(disb.getDisbHold()) + ");";
 
 		System.out.println(sql);
 
 		return;
 	}
 
 	private void printDefault(Default def){
 		if(def == null) return;
 
 		// If they aren't defaulted, then skip this
		if(! def.getDef()) return;
 
 		this.defaultID++;
 
 		System.out.print("INSERT INTO defaults ( default_id, default_ind, satis_pmt_arr, def_avert_rq, def_avert_rq_dt, def_avert_req_cure, def_avert_rq_cure_dt, claim_fil, claim_fil_dt, claim_pd, claim_pd_dt) values ( ");
 		System.out.print(this.defaultID + ", " );
 		System.out.print(dbCol(def.getDef()) + ", "  );
 		System.out.print(dbCol(def.getSatisPmtArr()) + ", "  );
 		System.out.print(dbCol(def.getDefAvertRq()) + ", "  );
 		System.out.print(dbCol(def.getDefAvertRqDt()) + ", "  );
 		System.out.print(dbCol(def.getDefAvertRqCure()) + ", "  );
 		System.out.print(dbCol(def.getDefAvertRqCureDt()) + ", "  );
 		System.out.print(dbCol(def.getClaimFil()) + ", "  );
 		System.out.print(dbCol(def.getClaimFilDt()) + ", "  );
 		System.out.print(dbCol(def.getClaimPd()) + ", "  );
 		System.out.print(dbCol(def.getClaimPdDt()) + ");");
 
 		System.out.println();
 
 		return;
 	}
 
 	private String dbCol(String val){
 		if(val == null) { return "null"; }
 
 		return "'" + val + "'";
 	}
 
 	private String dbCol(boolean val){
 		return "'" + (val ? "1" : "0") + "'";
 	}
 
 	private String dbCol(Date val){
 		if(val == null) { return "null"; }
 
 		String strValue = "'" + val.toString() + "'";
 
 		if("'0000-00-00'".equals(strValue)){ strValue = "null"; }
 		return strValue;
 	}
 
 	private String decCol(BigDecimal value, int precision){
 		if(value == null) return "null";
 
 		return value.setScale(precision).toString();
 	}
 
 	private String decCol(BigDecimal value){
 		return decCol(value, 2);
 	}
 
 	private String rawCol(String value){
 		if(value == null) return "null";
 		return value;
 	}
 }
 
 

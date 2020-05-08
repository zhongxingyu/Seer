 package com.mediware.system;
 
 import java.awt.List;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import com.mediware.arch.IO;
 import com.mediware.arch.Message;
 import com.mediware.arch.mData;
 import com.mediware.arch.Enums.partition;
 import com.mediware.arch.Enums.mType;
 import com.mediware.service.LoginService;
 import com.mediware.service.PatientHistoryService;
 import com.mediware.data.dataContainers.*;
 import com.mediware.data.*;
 
 /**
  *  Class for system controls partition. 
  * @author Cameron Keith
  */
 public class SYS{
 	
 	private IO sysIO;
 	private datadriver DB = new datadriver();
 	private Message[] sysMessages; 		
 	//private int current_mType;
 	private account current_Account;		//Used for the current account tha
 	private int mPerm;
 	private int AID;
 	private int EditAID;
 	
 	private final int[] noIntArgs = {};
 	private final String[] noStringArgs = {};
 	
 	//constructor - IO as an argument.
 	public SYS(IO theIO){
 		this.sysIO = theIO;
 		//----------
 		//the folowing block of code will add a client to the database for testing
 		//client theClient = new client("doctor", "doctor"); // UN: test PW:test
 		//System.out.println(DB.addClient(theClient)); //adds client to database
 		//----------
 	
 	}
 	
 	
 
 	/**
 	 * called by main. Checks for messages, executes functions
 	 * and sends messages using IO if necessary. 
 	 */
 	public void run()
 	{
 				
 		//first retrieve messages
 		sysMessages = sysIO.nextFrame(partition.SYS);
 		for(int i = 0; i < sysMessages.length; i++) {
 			switch(sysMessages[i].getMessageType()) {
 				case loginRequest:
 					LoginService log = new LoginService(DB);
 					System.out.println("loginRequest being processed");
 					String[] loginParams = sysMessages[i].getMessageData().getLabels();
 					// Check if name & pass are valid
 		    		if (log.authenticate(loginParams[0], loginParams[1]))
 		    		{	
 		    			//get AID
 		    		    	AID = DB.findUserPass(loginParams[0], loginParams[1]);
 		    			
 		    		    //Sets current account that's logged in.
 		    		    	current_Account = DB.getAccount(AID);
 		    		    	
 		    			System.out.println("AID: " + AID);
 		    			//determine permissions
 		    			mPerm = DB.getPermission(AID);
 		    			
 		    			System.out.println("PERMISSIONS: " + mPerm);
 		    			
 		    			if(mPerm == 0)
 		    				System.out.println("No permissions");
 		    			else if(mPerm == 1) //client
 		    			{
 			    			System.out.println("Should send message to CND to display patient menu");
 			    			mData messageData = new mData(noIntArgs, noStringArgs);
 			    			partition[] subscribers = {partition.CND};
 			    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.cndDisplayPatientMenuPanel);
 		    				
 		    			}
 		    			else if(mPerm == 2) //ofc
 		    			{
 	
 		    			}
 		    			else if(mPerm == 3) //ma
 		    			{
 			    			System.out.println("Should send message to CND to display MA main panel");
 			    			mData messageData = new mData(noIntArgs, noStringArgs);
 			    			partition[] subscribers = {partition.CND};
 			    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.cndDisplayMAMainPanel);
 		    			}
 		    			else if(mPerm == 4) //nurse
 		    			{
 			    			System.out.println("Should send message to CND to display MA main panel");
 			    			mData messageData = new mData(noIntArgs, noStringArgs);
 			    			partition[] subscribers = {partition.CND};
 			    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.cndDisplayNurseMainPanel);
 		    			}
 		    			else //doctor
 		    			{
 			    			System.out.println("Should send message to CND to display doctor panel");
 			    			mData messageData = new mData(noIntArgs, noStringArgs);
 			    			partition[] subscribers = {partition.CND};
 			    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.cndDisplayDoctorMainPanel);	
 		    			}
 		    		}
 		    		else
 		    		{	// User is invalid
 		    			String[] stringParams = {"Invalid Username or Password", "Login Error"};
 		    			mData messageData = new mData(noIntArgs, stringParams);
 		    			partition[] subscribers = {partition.CND};
 		    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.cndDisplayErrorDialog);
 		    		}
 					break;
 					
 				case sysCreateEmployee:
 					//create a new employee - insert in database
 					// Message has string array created as follows:
 					// Index = 0      1      2      3      4      5     6       7          8           9         10   11       12            13         14        15
 					//      {fname, mname, lname, street, city, state, zip, homephone, workphone, mobilephone, email, dob, employeenum, permissions, username, password};
 					
 					String[] paramE = sysMessages[i].getMessageData().getLabels();
 					
 					//first check and make sure username is available
 						if(DB.isUsernameAvail(paramE[14])) {
 			    			String[] stringParams = {"Username is already taken! Please select a new one.", "Create Employee Error"};
 			    			mData messageData = new mData(noIntArgs, stringParams);
 			    			partition[] subscribers = {partition.CND};
 			    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.cndDisplayErrorDialog);
 						}
 						
 		    			//Run another check to see if the email we're adding is valid
 						else if(DB.isEmailAvail(paramE[10])){
 			    			String[] stringParams1 = {"Email is already in use! Please select a new one.", "Create Employee Error"};
 			    			mData messageData1 = new mData(noIntArgs, stringParams1);
 			    			partition[] subscribers1 = {partition.CND};
 			    			sysIO.createMessageToSend(partition.SYS, subscribers1, messageData1, mType.cndDisplayErrorDialog);
 		    			} 
 		    			
 		    			//Run yet another check to see if the empNumber is not in use
 						else if(DB.isEmpNumAvail(paramE[12])){
 			    			String[] stringParams1 = {"That employee number is already in use! Please select a new one.", "Create Employee Error"};
 			    			mData messageData1 = new mData(noIntArgs, stringParams1);
 			    			partition[] subscribers1 = {partition.CND};
 			    			sysIO.createMessageToSend(partition.SYS, subscribers1, messageData1, mType.cndDisplayErrorDialog);
 		    			} 
 		 
 					else{						
 						//create and fill out new employee
 						employee E = new employee();
 						E.setFname(paramE[0]);
 						E.setMname(paramE[1]);
 						E.setLname(paramE[2]);
 						E.setAddress1(paramE[3]);
 						E.setCity(paramE[4]);
 						E.setState(paramE[5]);
 						E.setZip(paramE[6]);
 						E.setPhoneHome(paramE[7]);
 						E.setPhoneWork(paramE[8]);
 						E.setPhoneMobile(paramE[9]);
 						E.setEmail(paramE[10]);
 						//E.setDOB(paramE[11]);		//We don't need this for employees do we? -Shane
 						E.setEmpNum(Integer.parseInt(paramE[12]));
 						E.setUsername(paramE[14]);
 						E.setPassword(paramE[15]);
 						
 						//for permissions
 						if(paramE[13].equals("Medical Assistant"))
 							E.setPermissions(3);
 						else if(paramE[13].equals("Nurse"))
 							E.setPermissions(4);
 						else
 						{
 							E.setPermissions(5);
 						}
 												
 						//insert the newly created employee into the database
 						DB.addEmployee(E);
 						
 
 						//Send message back to SYS (self) sysGoToMenu
 		    			String[] emptyParams = {"", ""};
 		    			mData messageData = new mData(noIntArgs, emptyParams);
 		    			partition[] subscribers = {partition.SYS};
 		    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.sysGoToMenu);
 					}
 					
 					break;
 				case sysCreatePatient:
 					String[] paramC = sysMessages[i].getMessageData().getLabels();
 					
 					//Example of how to display an error message if username is already in the db
 					if(DB.isUsernameAvail(paramC[17])) {
 		    			String[] stringParams = {"This username is already taken! Please select a new one.", "Create Patient Error"};
 		    			mData messageData = new mData(noIntArgs, stringParams);
 		    			partition[] subscribers = {partition.CND};
 		    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.cndDisplayErrorDialog);
 					}
 					else if(DB.isEmailAvail(paramC[10])) {
 		    			String[] stringParams = {"This email is already registered in the database! Select a different one.", "Create Patient Error"};
 		    			mData messageData = new mData(noIntArgs, stringParams);
 		    			partition[] subscribers = {partition.CND};
 		    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.cndDisplayErrorDialog);
 					}
 					else{
 						
 						//create new patient and add to DB
 						// Message has string array created as follows:
 						// Index = 0      1      2      3      4      5     6       7          8           9         10      11         12        13      14    15      16       17        18
 						//      {fname, mname, lname, street, city, state, zip, homephone, workphone, mobilephone, email, provider, policynum, groupnum, dob, height, weight, username, password};
 						client C;
 					    	if(sysMessages[i].getMessageData().getArguments().length == 1) {
 					    	C = new client(sysMessages[i].getMessageData().getArguments()[0]); // UN: test PW:test
 					}	else {
 					    C = new client(AID);
 					}
 						C.setUsername(paramC[17]);
 						C.setPassword(paramC[18]);
 						C.setFname(paramC[0]);
 						C.setMname(paramC[1]);
 						C.setLname(paramC[2]);
 						C.setAddress1(paramC[3]);
 						C.setCity(paramC[4]);
 						C.setState(paramC[5]);
 						C.setZip(paramC[6]);
 						C.setPhoneHome(paramC[7]);
 						C.setPhoneWork(paramC[8]);
 						C.setPhoneMobile(paramC[9]);
 						C.setEmail(paramC[10]);
 						C.setProvider(paramC[11]);
 						C.setPolicy(paramC[12]);
 						C.setGroup(paramC[13]);
 						C.setDob(paramC[14]);	
 						C.setHeight(paramC[15]);
 						C.setWeight(paramC[16]);   
 						
 						//insert the newly created patient into the database
 						DB.addClient(C);
 						
 						//Send message back to SYS (self) sysGoToMenu
 		    			String[] emptyParams = {"", ""};
 		    			mData messageData = new mData(noIntArgs, emptyParams);
 		    			partition[] subscribers = {partition.SYS};
 		    			sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.sysGoToMenu);
 					
 					}
 					
 					break;
 				case sysLogoutRequest:
 					// Clear saved data
 					
 					//Unbinds current account
 					current_Account = null;
 					
 					mPerm = 0;	//no permissions
 					
 					// Send message to CND to display the login panel
 					int[] intParams = new int[0];
 					String[] stringParams = new String[0];
 					mData messageData = new mData(intParams, stringParams);
 					partition[] subscribers = {partition.CND};
 					sysIO.createMessageToSend(partition.SYS, subscribers, messageData, mType.cndDisplayLoginPanel);
 					break;
 					
 				case sysPatientEditProfileRequest:
 				// Send message to CND to display the edit profile screen with the correct parameters	//TODO update it so it works with weight, height and DOB
 					int[] intPs = sysMessages[i].getMessageData().getArguments();
					
 					//EditAID = AID;
					if(current_Account.getPermissions() == 1)	//If an account is a patient, they edit their own profile
						EditAID = AID;
					
					client oc = DB.getClient(EditAID);
					
 					String[] stringPs = {oc.getFname(), oc.getMname(), oc.getLname(), oc.getAddress1(), oc.getCity(), oc.getState(), oc.getZip(), oc.getPhoneHome(), oc.getPhoneWork(), oc.getPhoneMobile(), oc.getEmail(), oc.getProvider(), oc.getPolicy(), oc.getGroup()};
 					mData messageD = new mData(intPs, stringPs);
 					partition[] subscriber = {partition.CND};
 					sysIO.createMessageToSend(partition.SYS, subscriber, messageD, mType.cndDisplayPatientProfilePanel);					
 					break;
 					
 				case sysUpdatePatient:
 					//for updating patient info
 					System.out.println("Editing AID:" + EditAID);
 					
 					String[] paramCl = sysMessages[i].getMessageData().getLabels();
 					
 					System.out.println(paramCl[2]);
 		
 					client C = DB.getClient(EditAID);
 					
 					//TODO validate data here, empty data will explode the system
 					
 					//edits database
 					C.setFname(paramCl[0]);
 					C.setMname(paramCl[1]);
 					C.setLname(paramCl[2]);
 					C.setAddress1(paramCl[3]);
 					C.setAddress2("");
 					C.setCity(paramCl[4]);
 					C.setState(paramCl[5]);
 					C.setZip(paramCl[6]);
 					C.setPhoneHome(paramCl[7]);
 					C.setPhoneWork(paramCl[8]);
 					C.setPhoneMobile(paramCl[9]);
 					C.setEmail(paramCl[10]);
 					C.setProvider(paramCl[11]);
 					C.setPolicy(paramCl[12]);
 					C.setGroup(paramCl[13]);
 					//TODO add height weight DOB
 					
 					DB.editClient(C);	//TODO make into a condit to see if pass or fail
 					
 					//Send message back to SYS (self) sysGoToMenu
 	    			String[] emptyParams = {"", ""};
 	    			mData message_Data = new mData(noIntArgs, emptyParams);
 	    			partition[] subscribers_1 = {partition.SYS};
 	    			sysIO.createMessageToSend(partition.SYS, subscribers_1, message_Data, mType.sysGoToMenu);
 					
 					break;
 					
 				case sysGoToMenu:
 					mData emptyData = new mData(noIntArgs, noStringArgs);
 					partition[] subscriberCND = {partition.CND};
 					if(mPerm == 0) {
 						//no permissions
 					} else if(mPerm == 1) {
 						//patient
 						sysIO.createMessageToSend(partition.SYS, subscriberCND, emptyData, mType.cndDisplayPatientMenuPanel);
 					} else if(mPerm == 2) {
 						//ofc
 					} else if(mPerm == 3) {
 						//ma
 						sysIO.createMessageToSend(partition.SYS, subscriberCND, emptyData, mType.cndDisplayMAMainPanel);
 					} else if(mPerm == 4) {
 						//nurse
 						sysIO.createMessageToSend(partition.SYS, subscriberCND, emptyData, mType.cndDisplayNurseMainPanel);
 					} else {
 						//doctor
 						sysIO.createMessageToSend(partition.SYS, subscriberCND, emptyData, mType.cndDisplayDoctorMainPanel);
 					}
 					break;
 					
 				case patientHistoryRequest:
 				    //CND is requesting patient history for type of StringArg[0]
 				    
 				    String type = sysMessages[i].getMessageData().getLabels()[0];
 				    PatientHistoryService phs = new PatientHistoryService(DB);
 				    
 				    int rAID = AID;
 				    
 				    if(current_Account.getPermissions() > 1)
 				    	rAID = EditAID;
 				    
 				    int[] data = phs.process(type, rAID);
 				    String[] stringPs1 = {type};
 				    mData messageD1 = new mData(data, stringPs1);
 				    partition[] subscriber1 = {partition.CND};
 				    sysIO.createMessageToSend(partition.SYS, subscriber1, messageD1, mType.patientHistoryData);
 
 				    break;
 				case patientVitalsEntry:
 					
 					int cAID;
 					
 					cAID = AID;
 					
 					if(current_Account.getPermissions() > 1)
 						cAID = EditAID;
 					
 				    bloodpressure newBP = new bloodpressure(cAID, sysMessages[i].getMessageData().getArguments()[4] + "", sysMessages[i].getMessageData().getArguments()[0] + "", sysMessages[i].getMessageData().getArguments()[3] + "", sysMessages[i].getMessageData().getArguments()[2] + "", sysMessages[i].getMessageData().getArguments()[1] + "");
 				    
 					client fucker = DB.getClient(cAID);
 					fucker.getBP().add(newBP);
 				    DB.editClient(fucker);
 				    
 					break;
 				case doctorPatientSearchRequest:
 				    String lastName = sysMessages[i].getMessageData().getLabels()[0];
 				    userinfo searchClient = new userinfo();
 				    searchClient.setLname(lastName);
 				    
 				    Object[] clients = DB.findClientByInfo(searchClient).toArray();
 				    
 				    String[] names = new String[clients.length];
 				    int[] ids = new int[clients.length];
 				    
 				    for(int index = 0; index < clients.length; index++) {
 					names[index] = ((client)clients[index]).getFname() + " " + ((client)clients[index]).getLname();
 					ids[index] = ((client)clients[index]).getAID();
 				    }
 				    
 				    mData messageD11 = new mData(ids, names);
 				    partition[] subscriber11 = {partition.CND};
 				    sysIO.createMessageToSend(partition.SYS, subscriber11, messageD11, mType.cndPatientSearchReport);
 				    break;
 				    
 				case sysSelectPatient:
 				    int searchedClientAID = sysMessages[i].getMessageData().getArguments()[0];
 					int[] intPs1 = {searchedClientAID};
 					client oc1 = DB.getClient(searchedClientAID);
 					EditAID = searchedClientAID;
 					String[] stringPs11 = {oc1.getFname(), oc1.getMname(), oc1.getLname(), oc1.getHeight(), oc1.getWeight(), oc1.getDob()};
 					mData messageD111 = new mData(intPs1, stringPs11);
 					partition[] subscriber111 = {partition.CND};
 					sysIO.createMessageToSend(partition.SYS, subscriber111, messageD111, mType.cndPatientReport);		//TODO CHANGE TO PATIENTREPORT PANEL
 				    break;
 				    
 				    
 				case doctorEmployeeSearchRequest:
 				{
 				    String ElastName = sysMessages[i].getMessageData().getLabels()[0];
 				    userinfo EsearchEmp = new userinfo();
 				    EsearchEmp.setLname(ElastName);
 				    
 				    Object[] employees = DB.findEmpByInfo(EsearchEmp).toArray();
 				    
 				    String[] Enames = new String[employees.length];
 				    int[] Eids = new int[employees.length];
 				    
 				    for(int index = 0; index < employees.length; index++) 
 				    {
 				    	Enames[index] = ((employee)employees[index]).getFname() + " " + ((employee)employees[index]).getLname();
 				    	Eids[index] = ((employee)employees[index]).getAID();
 				    }
 				    
 				    mData messageD3 = new mData(Eids, Enames);
 				    partition[] subscriber3 = {partition.CND};
 				    sysIO.createMessageToSend(partition.SYS, subscriber3, messageD3, mType.cndEmployeeSearchReport);
 				    break;
 				}
 						    
 				case sysSelectEmployee:
 				    int searchedEmpAID = sysMessages[i].getMessageData().getArguments()[0];
 					int[] intPs2 = {searchedEmpAID};
 					employee oE1 = DB.getEmployee(searchedEmpAID);
 					EditAID = searchedEmpAID;
 					String[] stringPs5 = {oE1.getFname(), oE1.getMname(), oE1.getLname(), oE1.getAddress1(), oE1.getCity(), oE1.getState(), oE1.getZip(), oE1.getPhoneHome(), oE1.getPhoneWork(), oE1.getPhoneMobile(), oE1.getEmail(), oE1.getEmpNum() + ""};
 					
 					mData messageData2 = new mData(noIntArgs, stringPs5);
 					partition[] subscriber4 = {partition.CND};
 					sysIO.createMessageToSend(partition.SYS, subscriber4, messageData2, mType.cndDisplayEditEmployee);
 				    break;
 				    
 				case sysRequestMessages:
 					java.util.List<alert> alist = DB.getClient(AID).getAlerts();
 					String[] messages = new String[alist.size()];
 
 					for(int j = 1; j < alist.size(); j++) 
 					{
 						messages[j] = alist.get(j).getPriority() + " " + alist.get(j).getDate();
 					}
 					
 					mData messageData1 = new mData(noIntArgs, messages);
 					partition[] subscriber2 = {partition.CND};
 					sysIO.createMessageToSend(partition.SYS, subscriber2, messageData1, mType.cndDisplayMessagePanel);
 					break;
 
 					case sysRequestMessageNum:
 					String[] mes = {DB.getClient(AID).getAlerts().get(sysMessages[i].getMessageData().getArguments()[0]-1).getMsg()};
 					mData messageDataD1111 = new mData(noIntArgs, mes);
 					partition[] subscriber1111 = {partition.CND};
 					sysIO.createMessageToSend(partition.SYS, subscriber1111, messageDataD1111, mType.cndDisplayMessage);
 					break;
 
 				case sysRequestMessageDeletion:
 					int indexToDelete = sysMessages[i].getMessageData().getArguments()[0]-1;
 					client pt = DB.getClient(AID);
 					java.util.List<alert> alerts = pt.getAlerts();
 					alerts.remove(indexToDelete);
 					pt.setAlerts(alerts);
 					DB.editClient(pt);
 
 					mData messageData5 = new mData(noIntArgs, noStringArgs);
 					partition[] subscriber3 = {partition.SYS};
 					sysIO.createMessageToSend(partition.SYS, subscriber3, messageData5, mType.sysRequestMessages);
 					break;
 					
 				case sysUpdateEmployee:
 					
 					//for updating patient info
 					System.out.println("Editing AID:" + EditAID);
 					
 					String[] paramC5 = sysMessages[i].getMessageData().getLabels();
 					
 					System.out.println(paramC5[2]);
 		
 					employee C1 = DB.getEmployee(EditAID);
 					
 					//TODO validate data here, empty data will explode the system
 					
 					// Index = 0      1      2      3       4     5     6      7       8         9         10      11       12    
 					//      {fname, mname, lname, street, city, state, zip, homenum, worknum, mobilenum, email, permission  empNum}
 					
 					//edits database
 					C1.setFname(paramC5[0]);
 					C1.setMname(paramC5[1]);
 					C1.setLname(paramC5[2]);
 					C1.setAddress1(paramC5[3]);
 					C1.setAddress2("");
 					C1.setCity(paramC5[4]);
 					C1.setState(paramC5[5]);
 					C1.setZip(paramC5[6]);
 					C1.setPhoneHome(paramC5[7]);
 					C1.setPhoneWork(paramC5[8]);
 					C1.setPhoneMobile(paramC5[9]);
 					C1.setEmail(paramC5[10]);
 					//C1.setPermissions(Integer.parseInt(paramC5[11]));
 					C1.setEmpNum(Integer.parseInt(paramC5[12]));
 					//TODO add height weight DOB
 					
 					//for permissions
 					if(paramC5[11].equals("Medical Assistant"))
 						C1.setPermissions(3);
 					else if(paramC5[11].equals("Nurse"))
 						C1.setPermissions(4);
 					else
 					{
 						C1.setPermissions(5);
 					}
 					
 					DB.editEmployee(C1);	//TODO make into a condit to see if pass or fail
 					
 					
 					int[] intParams1 = new int[0];
 					String[] stringParams1 = new String[0];
 					mData messageData11 = new mData(intParams1, stringParams1);
 					partition[] subscribers1 = {partition.SYS};
 					sysIO.createMessageToSend(partition.CND, subscribers1, messageData11, mType.sysGoToMenu);
 					break;
 					
 				case sysSendMessage:
 					alert newAlert = new alert(EditAID, "4/29", sysMessages[i].getMessageData().getLabels()[0], sysMessages[i].getMessageData().getArguments()[0]);
 					client pat = DB.getClient(EditAID);
 					java.util.List<alert> alerts2 = pat.getAlerts();
 					alerts2.add(newAlert);
 					pat.setAlerts(alerts2);
 					DB.editClient(pat);
 					break;
 				case sysGoToViewedPatientMenu:
 					client c1 = DB.getClient(EditAID);
 					int[] intps2 = {EditAID};
 					String[] stringPs2 = {c1.getFname(), c1.getMname(), c1.getLname(), c1.getHeight(), c1.getWeight(), c1.getDob()};
 					mData messageD2 = new mData(intps2, stringPs2);
 					partition[] subscriber11111 = {partition.CND};
 					sysIO.createMessageToSend(partition.SYS, subscriber11111, messageD2, mType.cndPatientReport);		//TODO CHANGE TO PATIENTREPORT PANEL
 				    break;
 				default:
 					break;					
 			}
 			
 		}
 		
 	
 		
 	}
 		
 }

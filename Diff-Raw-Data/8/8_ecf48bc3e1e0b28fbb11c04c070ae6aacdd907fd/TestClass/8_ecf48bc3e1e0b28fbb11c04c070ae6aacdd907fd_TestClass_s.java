 package tests;
 import java.util.ArrayList;
 
 import XML.Backup;
 import XML.ReadXMLFile;
 import XML.WriteXMLFileUnregisteredVoters;
 
 import partiesList.IPartiesList;
 import partiesList.IParty;
 import partiesList.PartiesList;
 import partiesList.Party;
 import votersList.IVoterData;
 import votersList.IVoterData.AlreadyIdentified;
 import votersList.IVoterData.Unidentified;
 import votersList.IVotersList;
 import votersList.VoterData;
 import votersList.VotersList;
 
 import factories.*;
 
 
 public class TestClass {
 
 	
 
 
 		
 	public static void main(String[] args) {
 		//testVoter3();
 		//testVotersXML();
 		//testVotingRecords();
 		//testSerBackup();
 		//testReadWriteUnregisteredVotersXMLFile();
 		//System.out.println(Boolean.toString(true));
 		//testBackupVotersList1();
 		//testBackupPartiesList1();
 		testCombinedPartiesListAndVotersListBackup();
 		System.out.println("");
 	}
 
 	public static void testVoter1(){
 		Party p = new Party("AtudaLashilton", "AL", 0);
 		
 		
 		VoterData emil = new VoterData(1);
 		VoterData ziv = new VoterData(2);
 		VoterData yona = new VoterData(3);
 		VoterData ophir = new VoterData(4);
 		VoterData daniel = new VoterData(5);
 		
 		VotersList list1 = new VotersList();
 		list1.addVoter(emil);	
 		list1.addVoter(ziv);
 		list1.peep();
 		
 		VotersList list2 = new VotersList();
 		list2.addVoter(yona);
 		list2.addVoter(ophir);
 		list2.addVoter(daniel);
 		list2.peep();
 		
 		//list1.merge(list2);
 		list1.peep();
 
 	}
 	
 	public static void testVoter2(){
 		Party p = new Party("AtudaLashilton", "AL", 0);
 		
 		
 		VoterData emil = new VoterData(1);
 		VoterData ziv = new VoterData(2);
 		VoterData yona = new VoterData(3);
 		VoterData ophir = new VoterData(4);
 		VoterData daniel = new VoterData(5);
 		
 		VotersList list1 = new VotersList();
 		list1.addVoter(emil);	
 		list1.addVoter(ziv);
 		list1.addVoter(yona);
 		list1.peep();
 		
 		VotersList list2 = new VotersList();
 		list2.addVoter(yona);
 		list2.addVoter(ophir);
 		list2.addVoter(daniel);
 		list2.peep();
 		
 		//list1.merge(list2);
 		list1.peep();
 
 	}
 	
 	public static void testVoter3(){
 		Party p = new Party("AtudaLashilton", "AL", 0);
 		
 		
 		VoterData emil = new VoterData(1);
 		VoterData ziv = new VoterData(2);
 		VoterData yona = new VoterData(3);
 		VoterData ophir = new VoterData(4);
 		VoterData daniel = new VoterData(5);
 		
 		VotersList list1 = new VotersList();
 		list1.addVoter(emil);	
 		list1.addVoter(ziv);
 		list1.addVoter(yona);
 		list1.peep();
 		
 		VotersList list2 = new VotersList();
 		list2.addVoter(yona);
 		list2.addVoter(ophir);
 		list2.addVoter(daniel);
 		list2.peep();
 		
 		//list1.replaceWith(list2);
 		list1.peep();
 
 	}
 	
 	public static void testVotersXML(){
 		IVotersList res = new ReadXMLFile(new PartiesListFactory(),new PartyFactory(), new VotersListFactory(), new VoterDataFactory()).readSuppliedVotersListXML("voters.xml");
 		
 		
 		System.out.println("======================");
 		for (IVoterData voter : res) {
 			System.out.println(voter);
 		}
 	}
 	
 	public static void testVotingRecords(){
 		IPartiesList res = new ReadXMLFile(new PartiesListFactory(),new PartyFactory(), new VotersListFactory(), new VoterDataFactory()).readSuppliedVotingRecordsXML("votingRecords.xml");
 		
 		System.out.println("======================");
 		for (IParty p : res) {
 			System.out.println(p);
 		}
 	}
 	/*
 	public static void testSerBackup(){
 		
 		Party p = new Party("AtudaLashilton", "AL", 0);
 		
 		VoterData emil = new VoterData(1);
 		VoterData ziv = new VoterData(2);
 		VoterData yona = new VoterData(3);
 		VoterData ophir = new VoterData(4);
 		VoterData daniel = new VoterData(5);
 		
 		VotersList votersList = new VotersList();
 		votersList.addVoter(emil);	
 		votersList.addVoter(ziv);
 		votersList.addVoter(yona);
 		votersList.addVoter(ophir);
 		votersList.addVoter(daniel);
 		votersList.peep();
 
 		//Backup.backup(votersList);
 		//VotersList restored = Backup.restore();
 		
 		System.out.println("#######################");
 		//restored.peep();
 		
 	}*/
 	
 	
 	
 	public static void testReadWriteUnregisteredVotersXMLFile(){
 		ReadXMLFile readServices = new ReadXMLFile(new PartiesListFactory(), new PartyFactory(), new VotersListFactory(), new VoterDataFactory() );
 		
 		WriteXMLFileUnregisteredVoters tmp = new WriteXMLFileUnregisteredVoters("file.xml");
 		
 		tmp.createEmptyUnregisteredVotersXMLFile();
 		tmp.addVoterToXMLFile(new VoterData(123));
 		tmp.addVoterToXMLFile(new VoterData(456));
 		tmp.addVoterToXMLFile(new VoterData(789));
 		
 		
 		IVotersList res = readServices.readUnregisteredVotersXMLFile("file.xml");
 		//System.out.println(res);
 		res.peep();
 	}
 	
 	
 	public static void testBackupVotersList1(){
 		
 		/*
 		 * disengage the parties list store logic in storeState
 		 */
 		
 		
 		
		Backup b = new Backup(new PartiesListFactory(), new PartyFactory(), new VotersListFactory(), new VoterDataFactory(), "votersBackup.xml", null);
 		
 		
 		
 		VoterData v1 = new VoterData(123);
 		try {
 			v1.markIdentified();
 		} catch (AlreadyIdentified e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		VoterData v2 = new VoterData(456);
 		try {
 			v2.markIdentified();
 			v2.markVoted();
 		} catch (AlreadyIdentified e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (Unidentified e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		VoterData v3 = new VoterData(789);
 		
 		VotersList lst = new VotersList();
 		lst.addVoter(v1);
 		lst.addVoter(v2);
 		lst.addVoter(v3);
 		
 		
 		
 		
 		//BackupVotersListToXMLFile.addVoterToXMLFile(v1, "votersBackup");
 		//BackupVotersListToXMLFile.addVoterToXMLFile(v2, "votersBackup");
 		//BackupVotersListToXMLFile.addVoterToXMLFile(v3, "votersBackup");
 		
 		System.out.println("The list before backup is:");
 		/*for (VoterData voterData : lst) {
 			System.out.println(voterData);
 		}*/
 		
 		lst.peep();
 		
 		System.out.println("\n\n\n now we will put it to the xml file \n... ");
 		b.storeState(null, lst, null);
 		
 		/*for (IVoterData voter : lst) {
 			BackupVotersListToXMLFile.addVoterToXMLFile(voter, "votersBackup.xml");
 		}*/
 		
 		
 		System.out.println("now we will print the backuped list:");
 		
 		IVotersList backupedVotersList = b.restoreVoters();
 		
 		/*for (VoterData voterData : backupedVotersList) {
 			System.out.println(voterData);
 		}	*/
 		
 		backupedVotersList.peep();
 	}
 	
 	
 	public static void testBackupPartiesList1(){
 		
 		/*
 		 * disengage the voters list store logic in storeState
 		 */
 		
 		//TODO: huge problem when was hebrew
 		Party p1 = new Party("RIGHT", "", 10);
 		Party p2 = new Party("LEFT", "L", 10);
 		Party p3 = new Party("CENTER", "C", 10);
 		
 		IPartiesList lst = new PartiesList();
 		
 		lst.addParty(p1);
 		lst.addParty(p2);
 		lst.addParty(p3);
 		
 		System.out.println("The parties:");
 		/*for (Party party : lst) {
 			System.out.println(party);
 		}*/
 		
 		lst.peep();
 		
 		System.out.println("\n\n\n  now we will backup \n...");
 		
 		//BackupPartiesListToXMLFile.createEmptyPartiesListXMLFile("partiesBackup.xml");
		Backup b = new Backup(new PartiesListFactory(), new PartyFactory(), new VotersListFactory(), new VoterDataFactory(), null, "partiesBackup.xml");
 		
 		/*for (IParty party : lst) {
 			BackupPartiesListToXMLFile.addPartyToXMLFile(party, "partiesBackup.xml");
 		}*/
 		
 		b.storeState(lst, null, null);
 		
 		
 		IPartiesList backupList = b.restoreParties();
 		
 		
 		
 		System.out.println("\n\n\n\n\n\nThe list after backup is:");
 		/*for (Party party : backupList) {
 			System.out.println(party);
 		}*/
 		
 		backupList.peep();
 		
 		
 	}
 	
 	
 	public static void testCombinedPartiesListAndVotersListBackup(){
 		
		Backup b = new Backup(new PartiesListFactory(), new PartyFactory(), new VotersListFactory(), new VoterDataFactory(), "votersBackup.xml", "partiesBackup.xml");
 		
 		
 		//VotersList
 		VoterData v1 = new VoterData(123);
 		try {
 			v1.markIdentified();
 		} catch (AlreadyIdentified e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		VoterData v2 = new VoterData(456);
 		try {
 			v2.markIdentified();
 			v2.markVoted();
 		} catch (AlreadyIdentified e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (Unidentified e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		VoterData v3 = new VoterData(789);
 		
 		VotersList vlst = new VotersList();
 		vlst.addVoter(v1);
 		vlst.addVoter(v2);
 		vlst.addVoter(v3);
 		
 
 		System.out.println("The list before backup is:");
 
 		
 		vlst.peep();
 		
 		
 
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		Party p1 = new Party("RIGHT", "", 10);
 		Party p2 = new Party("LEFT", "L", 10);
 		Party p3 = new Party("CENTER", "C", 10);
 		
 		IPartiesList plst = new PartiesList();
 		
 		plst.addParty(p1);
 		plst.addParty(p2);
 		plst.addParty(p3);
 		
 		System.out.println("The parties:");
 		/*for (Party party : lst) {
 			System.out.println(party);
 		}*/
 		
 		plst.peep();
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		System.out.println("\n\n\n now we will put it to the xml file \n... ");
 		System.out.println("  now we will backup \n...");
 
 		b.storeState(plst, vlst, null);
 		
 		
 		IPartiesList backupList = b.restoreParties();
 		
 		
 		
 		System.out.println("\n\n\n\n\n\nThe P list after backup is:");
 
 		backupList.peep();
 		
 		
 		System.out.println("now we will print the backuped V list:");
 		
 		IVotersList backupedVotersList = b.restoreVoters();
 		
 		/*for (VoterData voterData : backupedVotersList) {
 			System.out.println(voterData);
 		}	*/
 		
 		backupedVotersList.peep();
 	}
 	
 		
 
 
 
 }

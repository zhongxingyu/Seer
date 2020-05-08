 package global.tests;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import mainframe.communication.IStationsControllerFactory;
 import mainframe.factories.IMainframeFactory;
 import mainframe.factories.IMainframeWindowFactory;
 import mainframe.logic.IMainframe;
 import mainframe.logic.IMainframe.IdentificationError;
 import choosingList.factories.ChoosingListFactory;
 import choosingList.factories.IChoosingListFactory;
 import choosingList.factories.IChoosingWindowFactory;
 import choosingList.logic.ChooseType;
 import fileHandler.factories.IBackupFactory;
 import fileHandler.factories.IReadSuppliedXMLFactory;
 import fileHandler.logic.Backup;
 import fileHandler.logic.IBackup;
 import fileHandler.logic.IReadSuppliedXML;
 import global.gui.StationPanel;
 import partiesList.factories.IPartiesListFactory;
 import partiesList.factories.IPartyFactory;
 import partiesList.factories.PartiesListFactory;
 import partiesList.factories.PartyFactory;
 import partiesList.model.IPartiesList;
 import partiesList.model.IParty;
 import partiesList.model.Party;
 import practiceStation.factories.IImagePanelFactory;
 import practiceStation.factories.IPracticeStationFactory;
 import practiceStation.factories.IPracticeStationWindowFactory;
 import practiceStation.factories.PracticeStationFactory;
 import practiceStation.logic.IPracticeStation;
 import votersList.factories.IVoterDataFactory;
 import votersList.factories.IVotersListFactory;
 import votersList.factories.VoterDataFactory;
 import votersList.factories.VotersListFactory;
 import votersList.model.IVoterData;
 import votersList.model.IVotersList;
 import votersList.model.VoterData;
 import votersList.model.VotersList;
 import votingStation.factories.IVotingRecordFactory;
 import votingStation.factories.IVotingStationFactory;
 import votingStation.factories.IVotingStationWindowFactory;
 import votingStation.factories.VotingStationFactory;
 import org.junit.*;
 
 public class AcceptanceTest {
 	
 	private int numVotingStations = 4;
 	private int numPracticeStations = 2;
 	private long maxVotingTimeSeconds = 1L;
 	private int backupTimeIntervalSeconds = 2;
 	private List<String> passwords;
 
 	private IMainframe mainframe;
 	private List<IPracticeStation> practiceStations;
 	private IPartiesList practiceParties;
 	
 	
 	private IPartiesList initialPartiesList;
 	private IVotersList initialVotersList;
 	
 	private IPartiesList expectedPartiesList;
 	private IVotersList expectedVotersList;
 	private IVotersList expectedUnregisteredList;
 	
 	private Map<StationPanel, ChoosingWindowStub> choosingWindowStubs;
 	private List<VotingStationWindowStub> votingWindowStubs;
 	private Map<StationPanel, ImagePanelStub> imagePanelStubs;
 	private List<PracticeStationWindowStub> practiceStationWindowStubs;
 	private MainframeWindowStub mainframeWindowStub;
 	
 	private final String backupVotersListFile = "acceptanceTest/listBackup/VotersListBackup.xml";
 	private final String backupPartiesListFile = "acceptanceTest/listBackup/PartiesListBackup.xml";
 	private final String backupUnregisteredFile = "acceptanceTest/unregisteredVoters/UnregisteredVoters.xml";
 	
 	
 	private final static int WhitePartyNum = -1;
 	
 	
 	//keep track of voters (have they identified themselves? who have they voted to?)
 	private Set<Integer> identifiedIDs;
 	private Map<Integer, IParty> votes;
 	
 	
 	private String getSymbolByPlace(int place) throws Exception{
 		if(place == WhitePartyNum) return null;
 		for(IParty party : initialPartiesList){
 			if(place == 0){
 				return party.getSymbol();
 			}
 			place--;
 		}
 		throw new AssertionError();
 	}
 	
 	private void peepParties(){
 		IPartyFactory partyFactory = new PartyFactory();
 		IPartiesListFactory partiesListFactory = new PartiesListFactory(partyFactory);
 		IVotersListFactory votersListFactory = new VotersListFactory();
 		IVoterDataFactory voterDataFactory = new VoterDataFactory();
 		
 		IBackup backup = new Backup(partiesListFactory, partyFactory, votersListFactory, voterDataFactory, backupVotersListFile, backupPartiesListFile, backupUnregisteredFile);
 		IPartiesList resultParties = backup.restoreParties();
 		resultParties.peep();
 	}
 	
 	private void peepVoters(){
 		IPartyFactory partyFactory = new PartyFactory();
 		IPartiesListFactory partiesListFactory = new PartiesListFactory(partyFactory);
 		IVotersListFactory votersListFactory = new VotersListFactory();
 		IVoterDataFactory voterDataFactory = new VoterDataFactory();
 		
 		IBackup backup = new Backup(partiesListFactory, partyFactory, votersListFactory, voterDataFactory, backupVotersListFile, backupPartiesListFile, backupUnregisteredFile);
 		IVotersList resultVoters = backup.restoreVoters();
 		resultVoters.peep();
 	}
 	
 	private void peepUnregVoters(){
 		IPartyFactory partyFactory = new PartyFactory();
 		IPartiesListFactory partiesListFactory = new PartiesListFactory(partyFactory);
 		IVotersListFactory votersListFactory = new VotersListFactory();
 		IVoterDataFactory voterDataFactory = new VoterDataFactory();
 		
 		IBackup backup = new Backup(partiesListFactory, partyFactory, votersListFactory, voterDataFactory, backupVotersListFile, backupPartiesListFile, backupUnregisteredFile);
 		IVotersList resultVoters = backup.restoreUnregisteredVoters();
 		resultVoters.peep();
 	}
 	
 	
 	
 	
 	@Before
 	public void initSys(){
 		practiceStations = new ArrayList<IPracticeStation>();
 		
 		passwords = new ArrayList<String>();
 		passwords.add("password1");
 		passwords.add("password2");
 		passwords.add("password3");
 		passwords.add("password4");
 		
 		votingWindowStubs  = new ArrayList<VotingStationWindowStub>();
 		imagePanelStubs = new HashMap<StationPanel, ImagePanelStub>();
 		practiceStationWindowStubs = new ArrayList<PracticeStationWindowStub>();
 		choosingWindowStubs = new HashMap<StationPanel, ChoosingWindowStub>();
 		mainframeWindowStub = null;
 		
 		
 		IPartyFactory partyFactory = new PartyFactory();
 		IPartiesListFactory partiesListFactory = new PartiesListFactory(partyFactory);
 		IVotersListFactory votersListFactory = new VotersListFactory();
 		IVoterDataFactory voterDataFactory = new VoterDataFactory();
 		IMainframeWindowFactory mainframeWindowFactory = new MainframeWindowStubFactory(this);
 		IReadSuppliedXMLFactory readSuppliedXMLFactory = new ReadSuppliedXMLTestConfigurationFactory(partiesListFactory, partyFactory, votersListFactory, voterDataFactory);
 		
 		IChoosingWindowFactory choosingWindowFactory = new ChoosingWindowStubFactory(this);
 		IChoosingListFactory choosingListFactory = new ChoosingListFactory(choosingWindowFactory);
 		IVotingStationWindowFactory votingStationWindowFactory = new VotingStationWindowStubFactory(this);
 		IVotingRecordFactory votingRecordFactory = new VotingRecordTestConfigurationFactory(maxVotingTimeSeconds);
 		IVotingStationFactory votingStationFactory = new VotingStationFactory(choosingListFactory, votingStationWindowFactory, votingRecordFactory);
 		IStationsControllerFactory stationsControllerFactory = new StationsControllerTestConfigurationFactory(votingStationFactory, passwords, numVotingStations);
 		
 		IBackupFactory backupFactory = new BackupTestConfigurationFactory(partiesListFactory, partyFactory, votersListFactory, voterDataFactory, backupVotersListFile, backupPartiesListFile, backupUnregisteredFile);
 		IMainframeFactory mainframeFactory = new MainframeTestConfigurationFactory(backupTimeIntervalSeconds, backupFactory, mainframeWindowFactory, readSuppliedXMLFactory, stationsControllerFactory, voterDataFactory, votersListFactory);
 		mainframe = mainframeFactory.createInstance();
 		
 		IPracticeStationWindowFactory practiceStationWindowFactory = new PracticeStationWindowStubFactory(this);
 		IImagePanelFactory imagePanelFactory = new ImagePanelStubFactory(this);
 		IPracticeStationFactory practiceStationFactory = new PracticeStationFactory(choosingListFactory, practiceStationWindowFactory, imagePanelFactory);
 
 		IPartiesList practicePartiesList = partiesListFactory.createInstance();
 		for(Integer i = 1; i <= 13; i++)
 			practicePartiesList.addParty(new Party(i.toString(), i.toString(), i));
 		
 		practiceParties = practicePartiesList;
 		for(int i = 0; i < numPracticeStations; i++)
 			practiceStations.add(practiceStationFactory.createInstance(practiceParties));
 		
 		IReadSuppliedXML reader = readSuppliedXMLFactory.createInstance();
 		initialPartiesList = reader.readPartiesList();
 		initialVotersList = reader.readVotersList();
 		
 		expectedPartiesList = initialPartiesList.copy();
 		expectedVotersList = initialVotersList.copy();
 		expectedUnregisteredList = new VotersList();
 		
 		
 		identifiedIDs = new HashSet<Integer>();
 		votes = new HashMap<Integer, IParty>();
 		
 		mainframe.initialize();
 	}
 	
 	private void checkBackUp(){
 		IPartyFactory partyFactory = new PartyFactory();
 		IPartiesListFactory partiesListFactory = new PartiesListFactory(partyFactory);
 		IVotersListFactory votersListFactory = new VotersListFactory();
 		IVoterDataFactory voterDataFactory = new VoterDataFactory();
 		
 		IBackup backup = new Backup(partiesListFactory, partyFactory, votersListFactory, voterDataFactory, backupVotersListFile, backupPartiesListFile, backupUnregisteredFile);
 		IPartiesList resultParties = backup.restoreParties();
 		IVotersList resultVoters = backup.restoreVoters();
 		IVotersList reusltUnregistered = backup.restoreUnregisteredVoters();
 		Assert.assertEquals(expectedPartiesList,resultParties);
 		Assert.assertEquals(expectedVotersList, resultVoters);
 		Assert.assertEquals(expectedUnregisteredList, reusltUnregistered);
 	}
 	
 	@After
 	public void checkBackupLists() throws Exception{
 		mainframeWindowStub.setExpectedPartiesList(expectedPartiesList);
 		mainframe.countVotes();
 		mainframe.shutDown();
 		checkBackUp();
 	}
 
 	public void addVotingWindowStub(VotingStationWindowStub stub) {
 		votingWindowStubs.add(stub);
 	}
 
 	public void addImagePanelStub(ImagePanelStub stub, StationPanel caller) {
 		imagePanelStubs.put(caller, stub);
 	}
 	
 	public void addChoosingWindowStub(ChoosingWindowStub stub,
 			StationPanel stationPanel) {
 		choosingWindowStubs.put(stationPanel, stub);
 		
 	}
 
 	public void addPracticeStationWindowStub(PracticeStationWindowStub stub) {
 		practiceStationWindowStubs.add(stub);
 	}
 
 	public void setMainframeWindowStub(MainframeWindowStub stub) {
 		mainframeWindowStub = stub;
 	}
 	
 	public static class votingData{
 		private int id;
 		private int station;
 		private int party;
 		
 		public votingData(int id, int station, int party) {
 			this.station = station;
 			this.party = party;
 			this.id = id;
 		}
 
 		public int getParty() {
 			return party;
 		}
 
 		public int getStation() {
 			return station;
 		}
 
 		public int getId() {
 			return id;
 		}
 	}
 	
 	private void identify(votingData[] voting) throws Exception{
 		for(votingData data : voting){
 			int id = data.getId();
 			try {
 				mainframe.identification(id);
 				if(!expectedVotersList.inList(id)){
 					IVoterData toAddUnreg = new VoterData(id);
 					expectedUnregisteredList.addVoter(toAddUnreg);
 					IVoterData toAdd = new VoterData(id);
 					expectedVotersList.addVoter(toAdd);
 				}
 				IVoterData voter = expectedVotersList.findVoter(id);
 				voter.markIdentified();
 			} catch (IdentificationError e) {
 			}
 		}
 	}
 	
 	private void doVotes(votingData[] voting) throws Exception{
 		for(votingData singleVoting : voting){
 			Integer id = singleVoting.getId();
 			Integer party = singleVoting.getParty();
 			Integer station = singleVoting.getStation();
 			
 			
 			VotingStationWindowStub votingWindowStub = votingWindowStubs.get(station);
 			ChoosingWindowStub choosingWindowStub = choosingWindowStubs.get(votingWindowStub);
 			String partySymbol = getSymbolByPlace(party);
 			
 			votingWindowStub.setId(id);
 			final int partyInPage = 9;
 			for(int count = party; count >= partyInPage; count-=partyInPage){
 				choosingWindowStub.setChooseType(ChooseType.Next);
 			}
 			choosingWindowStub.setParty(partySymbol);
 			votingWindowStub.makeVote();
 			expectedVotersList.findVoter(id).markVoted();
 			expectedPartiesList.getPartyBySymbol(partySymbol).increaseVoteNumber();
 		}
 	}
 	
 	private void startVoting(votingData[] voting) throws Exception{
 		identify(voting);
 		doVotes(voting);
 	}
 	
 	
 	
 	
 	
 	
 	
 	@Test
 	public void testInitialize(){
 		Assert.assertEquals(numVotingStations, votingWindowStubs.size());
 		Assert.assertNotNull(mainframeWindowStub);
 		Assert.assertEquals(numPracticeStations, practiceStationWindowStubs.size());
 		for(PracticeStationWindowStub stub: practiceStationWindowStubs){
 			Assert.assertTrue(imagePanelStubs.containsKey(stub));
 			Assert.assertNotNull(imagePanelStubs.get(stub));
 		}
 	}
 	
 	
 	@Test
 	public void testOneVote() throws Exception{
 		votingData voting[] = {new votingData(1,0,0)};
 		startVoting(voting);
 	}
 	
 	
 
 
 	@Test
 	public void testOneVoteFromUnregisteredVoter() throws Exception{
 		votingData voting[] = {new votingData(4,0,0)};
 		startVoting(voting);
 	}
 	
 	@Test
 	public void testOneIdentification() throws Exception{
 
 		int id0 = 1;
 		mainframe.identification(id0);
 		
 		expectedVotersList.findVoter(id0).markIdentified();
 	}
 	
 	@Test
 	public void testIdentificationFromUnregisteredVoter() throws Exception{
 		
 	
 		int id0 = 4;
 		
 		mainframe.identification(id0);
 		
 		
 		IVoterData toAdd = new VoterData(id0);
 		toAdd.markIdentified();
 		IVoterData toAddUnreg = new VoterData(id0);
 		
 		expectedVotersList.addVoter(toAdd);
 		expectedUnregisteredList.addVoter(toAddUnreg);
 	}
 	
 	
 	
 	@Test
 	public void testFirstVotingAllParties() throws Exception{
 		
 		int parties[] = new int[initialPartiesList.size()];
 		for (int i = 0; i < parties.length; i++) {
 			parties[i] = i;
 		}
 		int stations[] = new int[numVotingStations];
 		for (int i = 0; i < stations.length; i++) {
 			stations[i] = i;
 		}
 		votingData voting[] = new votingData[10];
 		for (int i = 0; i < voting.length; i++) {
 			voting[i] = new votingData(i, stations[i%stations.length], parties[i%parties.length]);
 		}
 		
 		startVoting(voting);
 	}
 	
 	@Test
 	public void testFirstVotingManyParties() throws Exception{
 		int parties[] = new int[initialPartiesList.size()-1];
 		for (int i = 0; i < parties.length; i++) {
 			parties[i] = i;
 		}
 		int stations[] = new int[numVotingStations];
 		for (int i = 0; i < stations.length; i++) {
 			stations[i] = i;
 		}
 		votingData voting[] = new votingData[10];
 		for (int i = 0; i < voting.length; i++) {
 			voting[i] = new votingData(i, stations[i%stations.length], parties[i%parties.length]);
 		}
 		
 		startVoting(voting);
 	}
 	
 	
 	@Test
 	public void testFirstVotingNoVoting() throws Exception{
		mainframe.initialize();
 		mainframeWindowStub.setExpectedPartiesList(expectedPartiesList);
 		mainframe.countVotes();
 		mainframe.shutDown();
 	}
 	
 	@Test
 	public void testFirstFewVotes() throws Exception{
		boolean b = true;
		while(b){}
 		int parties[] = new int[initialPartiesList.size()-1];
 		for (int i = 0; i < parties.length; i++) {
 			parties[i] = i;
 		}
 		int stations[] = new int[numVotingStations];
 		for (int i = 0; i < stations.length; i++) {
 			stations[i] = i;
 		}
 		votingData voting[] = new votingData[2];
 		for (int i = 0; i < voting.length; i++) {
 			voting[i] = new votingData(i, stations[i%stations.length], parties[i%parties.length]);
 		}
 		
 		startVoting(voting);
 	}
 	
 	@Test
 	public void testRevoteOnce() throws Exception{
 		
 		votingData voting[] = {new votingData(1, 0, 0)};
 		startVoting(voting);
 		votingData reVoting[] = {new votingData(1, 0, WhitePartyNum)};
 		
 		startVoting(reVoting);
 		expectedPartiesList = initialPartiesList.copy();
 	}
 	
 	@Test
 	public void testRevoteAfterTimeOnce() throws Exception{
 		final long waiting_time = (maxVotingTimeSeconds-1) * 1000;
 		votingData voting[] = {new votingData(1, 0, 0)};
 		startVoting(voting);
 		votingData reVoting[] = {new votingData(1, 0, WhitePartyNum)};
 		Thread.sleep(waiting_time);
 		startVoting(reVoting);
 		expectedPartiesList = initialPartiesList.copy();
 	}
 	
 	@Test
 	public void testRevoteAfterTimeOver() throws Exception{
 		final long waiting_time = (maxVotingTimeSeconds+1) * 1000;
 		votingData voting[] = {new votingData(1, 0, 0)};
 		startVoting(voting);
 		votingData reVoting[] = {new votingData(1, 0, WhitePartyNum)};
 		Thread.sleep(waiting_time);
 		startVoting(reVoting);
 	}
 	
 	@Test
 	public void testRevoteDifferentStation() throws Exception{
 		votingData voting[] = {new votingData(1, 0, 0)};
 		startVoting(voting);
 		votingData reVoting[] = {new votingData(1, 1, 1)};
 		startVoting(reVoting);
 		expectedPartiesList = initialPartiesList.copy();
 		expectedPartiesList.getPartyBySymbol(getSymbolByPlace(0)).increaseVoteNumber();
 	}
 	
 	@Test
 	public void testRevoteDifferentStationAfterLongWait() throws Exception{
 		final long waiting_time = (maxVotingTimeSeconds+1) * 1000;
 		votingData voting[] = {new votingData(1, 0, 0)};
 		startVoting(voting);
 		votingData reVoting[] = {new votingData(1, 1, 1)};
 		Thread.sleep(waiting_time);
 		startVoting(reVoting);
 		expectedPartiesList = initialPartiesList.copy();
 		expectedPartiesList.getPartyBySymbol(getSymbolByPlace(0)).increaseVoteNumber();
 	}
 	
 	@Test
 	public void testGuideExist(){
 		PracticeStationWindowStub practiceStationWindow = practiceStationWindowStubs.get(0);
 		practiceStationWindow.practiceVote();
 		Assert.assertTrue(imagePanelStubs.get(practiceStationWindow).hasShowGuide());
 	}
 	
 	@Test
 	public void testTestVote() throws Exception{
 		int id = 1;
 		int party = 0;
 		mainframe.identification(id);
 		expectedVotersList.findVoter(id).markIdentified();
 		VotingStationWindowStub votingWindowStub = votingWindowStubs.get(0);
 		ChoosingWindowStub choosingWindowStub = choosingWindowStubs.get(votingWindowStub);
 		String partySymbol = getSymbolByPlace(party);
 		
 		votingWindowStub.setId(id);
 		votingWindowStub.setPassword("password1");
 		
 		choosingWindowStub.setParty(partySymbol);
 		votingWindowStub.testVote();
 		Assert.assertFalse(votingWindowStub.isWasWrongPassword());
 	}
 	
 	@Test
 	public void testTestVoteWrongPassword() throws Exception{
 		int id = 1;
 		int party = 0;
 		mainframe.identification(id);
 		expectedVotersList.findVoter(id).markIdentified();
 		VotingStationWindowStub votingWindowStub = votingWindowStubs.get(0);
 		ChoosingWindowStub choosingWindowStub = choosingWindowStubs.get(votingWindowStub);
 		String partySymbol = getSymbolByPlace(party);
 		
 		votingWindowStub.setId(id);
 		votingWindowStub.setPassword("passwerd2");
 		
 		choosingWindowStub.setParty(partySymbol);
 		votingWindowStub.testVote();
 		Assert.assertTrue(votingWindowStub.isWasWrongPassword());
 	}
 	
 	@Test
 	public void testRevoteOnlyTwice() throws Exception{
 		final long waiting1 = maxVotingTimeSeconds * 600;
 		final long waiting2 = maxVotingTimeSeconds * 300;
 		final long waiting3 = maxVotingTimeSeconds * 100;
 		votingData voting[] = {new votingData(1, 0, 0)};
 		startVoting(voting);
 		votingData reVoting1[] = {new votingData(1, 0, 1)};
 		Thread.sleep(waiting1);
 		startVoting(reVoting1);
 		
 		votingData reVoting2[] = {new votingData(1, 0, 2)};
 		Thread.sleep(waiting2);
 		startVoting(reVoting2);
 		
 		votingData reVoting3[] = {new votingData(1, 0, WhitePartyNum)};
 		Thread.sleep(waiting3);
 		startVoting(reVoting3);
 		
 		expectedPartiesList = initialPartiesList.copy();
 		expectedPartiesList.getPartyBySymbol(getSymbolByPlace(2)).increaseVoteNumber();
 	}
 	
 	private void testBackUp(long timeBetweenVotes) throws Exception{
 		//voting time during test is 5 seconds
 		final long finisingAfter = 5 * 1000;
 		//after voting we wait for backup to kick in
 		final long waitingForBackup = backupTimeIntervalSeconds * 1000 + 500;
 
 		int id = 1;
 		
 		
 		int parties[] = new int[initialPartiesList.size()];
 		for (int i = 0; i < parties.length; i++) {
 			parties[i] = i;
 		}
 		int stations[] = new int[numVotingStations];
 		for (int i = 0; i < stations.length; i++) {
 			stations[i] = i;
 		}
 		votingData voting[] = new votingData[1];
 		long finalTime = System.currentTimeMillis() + finisingAfter;
 		while(System.currentTimeMillis() < finalTime){
 			voting[0] = new votingData(id, stations[id%stations.length], parties[id % parties.length]);
 			startVoting(voting);
 			Thread.sleep(timeBetweenVotes);
 			id++;
 		}
 		Thread.sleep(waitingForBackup);
 		
 		checkBackUp();
 	}
 	
 	@Test
 	public void testIntensiveHotBackUp() throws Exception{
 		testBackUp(50);
 	}
 	
 	@Test
 	public void testNormalHotBackUp() throws Exception{
 		testBackUp(500);
 	}
 	
 	@Test
 	public void testSlowHotBackUp() throws Exception{
 		testBackUp(5000);
 	}
 	
 }

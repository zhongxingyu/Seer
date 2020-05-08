 package edu.berkeley.gamesman.database;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.ArrayList;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.game.TierGame;
 import edu.berkeley.gamesman.game.util.TierState;
 import edu.berkeley.gamesman.util.Pair;
 
 public class TierCutDatabase extends Database {
 
 	
 	static TierGame myTierGame;
 	private static boolean deleteLastRow;
 	private static int numTiersCut;
 
 	public TierCutDatabase(Configuration conf, long firstRecordIndex, long numRecords,
 			boolean reading, boolean writing) {
 		super(conf,firstRecordIndex, numRecords,
 				reading, writing);
 		myTierGame = (TierGame) conf.getGame();
 		deleteLastRow = true;
 		numTiersCut = 1;
 	}
 
 
 	@Override
 	public long readRecord(DatabaseHandle dh, long recordIndex) throws IOException {
 		TierState curPos = new TierState();
 		curPos = myTierGame.hashToState(recordIndex);
 		myTierGame.setState(curPos);
 		int tier = myTierGame.getTier();
 		if (shouldBeInDatabase(tier)) {
 			return readRecordFromByteIndex(dh, myLogic.getByteIndex(recordIndex));
 		} else {
 			if (inLastRow(recordIndex)) {
 				TierState pos = new TierState();
 				pos = myTierGame.hashToState(recordIndex);
 				myTierGame.setState(pos);
 				Record myRecord = new Record(conf);
 				myRecord.value = myTierGame.primitiveValue();
 				myRecord.remoteness = 0;
 				long val = myTierGame.recordToLong(myRecord);
 				return val;
 			} else {
 				return missingTierSolve(dh, recordIndex);
 			}
 
 		}
 	}
 
 	private long missingTierSolve(DatabaseHandle dh, long recordIndex) throws IOException {
 		long hash = recordIndex;
 		TierState pos = new TierState();
 		pos = myTierGame.hashToState(hash);
 		ArrayList<Pair<String, TierState>> moves = (ArrayList<Pair<String, TierState>>) myTierGame
 				.validMoves(pos);
 		boolean hasTie = false;
 		boolean hasWin = false;
 		Record myRecord = new Record(conf);
 		int currentRemoteness = Integer.MAX_VALUE;
 		boolean gameHasRemoteness = false;
 		for (int i = 0; i < moves.size(); i++) {
 			Pair<String, TierState> move = moves.get(i);
 			TierState newState = myTierGame.doMove(pos, move.car);
 			long newHash = myTierGame.stateToHash(newState);
 			Record curRecord = new Record(conf);
 			myTierGame.longToRecord(new TierState(), readRecord(dh, newHash),
 					curRecord);
 			Value curValue = curRecord.value;
 			int curRemoteness = Integer.MAX_VALUE;
 			if (curValue.hasRemoteness) {
 				gameHasRemoteness = true;
 				curRemoteness = curRecord.remoteness;
 			}
 			if (curValue.equals(Value.WIN)) {
 				if (hasWin) {
 					currentRemoteness = Math.min(currentRemoteness,
 							curRemoteness);
 				} else {
 					hasWin = true;
 					currentRemoteness = curRemoteness;
 				}
 			} else if ((curValue.equals(Value.TIE))) {
 				if (hasTie) {
 					currentRemoteness = Math.min(currentRemoteness,
 							curRemoteness);
 				} else {
 					hasTie = true;
 					currentRemoteness = curRemoteness;
 				}
 			} else {
 				currentRemoteness = Math.min(currentRemoteness, curRemoteness);
 			}
 
 		}
 		if (hasWin) {
 			myRecord.value = Value.WIN;
 		} else if (hasTie) {
 			myRecord.value = Value.TIE;
 		} else {
 			myRecord.value = Value.LOSE;
 		}
 		if(gameHasRemoteness) {
 			myRecord.remoteness = currentRemoteness + 1;
 		}
 		long val = myTierGame.recordToLong(myRecord);
 		return val;
 	}
 
 	@Override
 	public void writeRecord(DatabaseHandle dh, long recordIndex, long r) throws IOException {
 		TierState curPos = new TierState();
 		curPos = myTierGame.hashToState(recordIndex);
 		myTierGame.setState(curPos);
 		int tier = myTierGame.getTier();
 		if (shouldBeInDatabase(tier)) {
 			writeRecordFromByteIndex(dh, myLogic.getByteIndex(recordIndex), recordIndex);
 		}
 	}
 
 
 	// Is this supposed to be the actual amount of records, or the lying amount
 	// of records?
 	public static long getNumRecords(long firstRecord, long numRecords,
 			TierGame game) {
 		int numOfTiers = game.numberOfTiers();
 		//long lastTierHashes = game.numHashesForTier(numOfTiers - 1);
 		// TODO Auto-generated method stub
 		int amountChange = 0;
 		for (int i = 0; i < numOfTiers; i++ ) {
 			if (!shouldBeInDatabase(i)) {
 				amountChange += game.numHashesForTier(i);
 			}
 		}
 		return numRecords + amountChange; 
 	}
 
 	private static boolean shouldBeInDatabase(int tier) {
 		// Unsure if this is right, only for last row
 		if (tier == myTierGame.numberOfTiers() - 1) {
 			return false;
 		} else {
 			boolean inDB = false;
 			int remainTiers = 0;
 			for (int i = 1; i <= tier; i++) {
 				if (remainTiers == 0) {
 					inDB = true;
 					remainTiers = numTiersCut;
 				} else {
 					remainTiers--;
 					inDB = false;
 				}
 
 			}
 			return inDB;
 		}
 
 	}
	
 	private static boolean shouldBeInDatabase(int tier, TierGame game) {
 		if (tier == game.numberOfTiers() - 1) {
 			return false;
 		} else {
 			boolean inDB = false;
 			int remainTiers = 0;
 			for (int i = 1; i <= tier; i++) {
 				if (remainTiers == 0) {
 					inDB = true;
 					remainTiers = numTiersCut;
 				} else {
 					remainTiers--;
 					inDB = false;
 				}
 
 			}
 			return inDB;
 		}
 
 	}

 	private boolean inLastRow(long curByte) {
 		return deleteLastRow
 				&& curByte < (myTierGame.numHashes() - myTierGame
 						.numHashesForTier(myTierGame.numberOfTiers() - 1));
 
 	}
 
 	@Override
 	protected int readBytes(DatabaseHandle dh, long location, byte[] array,
 			int off, int len) throws IOException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	protected int writeBytes(DatabaseHandle dh, long location, byte[] array,
 			int off, int len) throws IOException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	public static void main(String[] args) throws ClassNotFoundException, IOException {
 		String jobFile = args[0];
 		Configuration conf = new Configuration(jobFile);
 		String dbListFile = args[1];
 		String dbUri = args[2];
 		final long firstRecordIndex, numRecords;
 		long tempNumRecords;
 		//ADD ARGUEMENT TO SPECIFY NUMTIERSCUT?
 		if (args.length > 3) {
 			firstRecordIndex = Integer.parseInt(args[3]);
 			//EDIT NUMRECORDS TO BE ACCURATE
 			tempNumRecords = Integer.parseInt(args[4]);
 		} else {
 			firstRecordIndex = 0L;
 			//EDIT NUMRECORDS TO BE ACCURATE
 			
 			tempNumRecords = conf.getGame().numHashes();
 			
 		}
 		int numOfTiers = ((TierGame)conf.getGame()).numberOfTiers();
 		//long lastTierHashes = game.numHashesForTier(numOfTiers - 1);
 		// TODO Auto-generated method stub
 		int amountChange = 0;
 		for (int i = 0; i < numOfTiers; i++ ) {
			if (!shouldBeInDatabase(i, ((TierGame)conf.getGame()))) {
 				amountChange += ((TierGame)conf.getGame()).numHashesForTier(i);
 			}
 		}
 		numRecords = tempNumRecords - amountChange;
 		
 		Scanner dbScanner = new Scanner(new File(dbListFile));
 		DataOutputStream dos = new DataOutputStream(new FileOutputStream(dbUri));
 		dos.writeLong(firstRecordIndex);
 		dos.writeLong(numRecords);
 		conf.store(dos);
 		long currentRecord = firstRecordIndex;
 		while (dbScanner.hasNext()) {
 			TierState curPos = new TierState();
 			curPos = myTierGame.hashToState(currentRecord);
 			myTierGame.setState(curPos);
 			int tier = myTierGame.getTier();
 			long nextNum = 0;
 			if (shouldBeInDatabase(tier)) {
 				dos.writeUTF(dbScanner.next());
 				dos.writeUTF(dbScanner.next());
 				dos.writeLong(currentRecord);
 				nextNum = dbScanner.nextLong();
 				dos.writeLong(nextNum);
 			} else {
 				dbScanner.next();
 				dbScanner.next();
 				nextNum = dbScanner.nextLong();
 			}
 			
 			if(shouldBeInDatabase(tier)) 
 			currentRecord += nextNum;
 		}
 		if (currentRecord != firstRecordIndex + numRecords)
 			throw new Error("Database is incomplete");
 		dos.close();
 		
 
 	}
 
 }

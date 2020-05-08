 package edu.berkeley.gamesman.testing.refactor;
 
 import java.math.BigInteger;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.core.PrimitiveValue;
 import edu.berkeley.gamesman.core.WorkUnit;
 import edu.berkeley.gamesman.game.connect4.OneTierC4Board;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * Fast Connect four
  * @author DNSpies
  */
 public final class MixGHC4 extends TieredMixGameHasher {
 	private OneTierC4Board otc4b;
 	private final int piecesToWin;
 	private int tier;
 
 	/**
 	 * @param conf The configuration object
 	 */
 	public MixGHC4(Configuration conf) {
 		this(conf, 0);
 	}
 
 	/**
 	 * @param conf The configuration object
 	 * @param startTier The tier to start the board at
 	 */
 	public MixGHC4(Configuration conf, int startTier) {
 		super(conf);
 		piecesToWin = Integer
 				.parseInt(conf.getProperty("connect4.pieces", "4"));
 		tier = startTier;
 		otc4b = new OneTierC4Board(gameWidth, gameHeight, piecesToWin, tier);
 	}
 
 	@Override
 	public BigInteger getHashWithoutTier() {
 		return otc4b.getHash();
 	}
 
 	@Override
 	public void nextTier() {
 		setTier(tier + 1);
 	}
 	
 	@Override
 	public void prevTier() {
 		setTier(tier - 1);
 	}
 
 	@Override
 	public void nextPositionInTier() {
 		otc4b.next();
 	}
 
 	@Override
 	public BigInteger numHashesForTier() {
 		return otc4b.numHashesForTier();
 	}
 
 	@Override
 	public int numberOfTiers() {
 		return (int) Util.longpow((gameHeight + 1), gameWidth);
 	}
 
 	@Override
 	public Collection<Integer> moveTiers() {
		return otc4b.moveTiers();
 	}
 
 	@Override
 	public Collection<BigInteger> moveHashesWithoutTiers() {
		return otc4b.moveHashes();
 	}
 
 	@Override
 	public String describe() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String displayState() {
 		return toString();
 	}
 
 	@Override
 	public int getDefaultBoardHeight() {
 		return 6;
 	}
 
 	@Override
 	public int getDefaultBoardWidth() {
 		return 7;
 	}
 
 	@Override
 	public PrimitiveValue primitiveValue() {
 		return otc4b.primitiveValue();
 	}
 
 	@Override
 	public String toString() {
 		return otc4b.toString();
 	}
 
 	@Override
 	public void setTier(int tier) {
 		this.tier = tier;
 		makeBoard();
 	}
 
 	@Override
 	public void setStartingPosition() {
 		setTier(0);
 	}
 
 	@Override
 	public int getTier() {
 		return tier;
 	}
 	
 	private void makeBoard(){
 		otc4b = new OneTierC4Board(gameWidth,  gameHeight, piecesToWin, tier);
 	}
 	
 	/**
 	 * Tester method
 	 * @param args A file name containing the properties
 	 */
 	public static void main(String[] args) {
 		Configuration conf = new Configuration(System.getProperties());
 		conf.addProperties(args[0]);
 		String databaseName;
 
 		databaseName = conf.getProperty("gamesman.database", "FileDatabase");
 
 		try {
 			Class<? extends Database> d = Util
 					.typedForName("edu.berkeley.gamesman.database."
 							+ databaseName);
 			Database db;
 			db = d.newInstance();
 			db.initialize(conf.getProperty("gamesman.db.uri"), conf);
 			TieredMixGameHashSolver<MixGHC4> solve = new TieredMixGameHashSolver<MixGHC4>();
 			solve.initialize(db);
 			WorkUnit wu = solve.prepareSolve(conf, MixGHC4.class);
 			wu.conquer();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 	}
 }

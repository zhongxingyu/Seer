 /*
  * */
 package com.semen.predict;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Properties;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 //import org.apache.log4j.Priority;
 import org.encog.ml.MLRegression;
 import org.encog.ml.data.MLData;
 import org.encog.ml.data.MLDataPair;
 import org.encog.ml.data.MLDataSet;
 import org.encog.util.Format;
 
 //import org.encog.util.simple.EncogUtility;
 
 /**
  * Basic config info for the Predict Loto.
  * 
  * @author serdar semen
  * 
  */
 public final class ConfigLoto {
 
 	/**
 	 * The base directory that all of the data for this example is stored in.
 	 */
 	private static final String basePathStr = "d:\\sayisalAi";
 
 	/* Get actual class name to be printed on */
 	public static final Logger log = Logger.getLogger(ConfigLoto.class);// .getName());
 	private static final long serialVersionUID = -447374783L;
 	public Properties props;
 
 	public final static int SAYISALMAXSETVALUE49 = 49;
 	public final static int SAYISALMINSETVALUE1 = 1;
 	public final static double MAPMAX = 1;
 	public final static double MAPMIN = 0;
 	/*
 	 * formula is mapminmax
 	 * 
 	 * (mapmax 0.9 (or 1) - mapmin 0.1(or 0)) * (valuetobe mapped- minsetvalue
 	 * 1) ---------------------------------------------- + mapmin 0.1 (or 0)
 	 * (maxsetvalue 49 - minsetvalue 1)
 	 */
 	public final static int SIZE6 = 6;
 	public final static int SIZE5 = 5;
 	public final static int SIZE54 = 54;
 	public final static int SIZE10 = 10;
 
 	public final static int SIZE80 = 80;
 	public final static int SIZE22 = 22;
 	public final static int SIZE34 = 34;
 	public final static int SIZE14 = 14;
 
 	// 0 : Sayısal Loto
 	// 1: Super Loto
 	// 2: Sans Topu
 	// 3: 10 Numara
 
 	public static final int SAYISALLOTO = 0;
 	public static final int SUPERLOTO = 1;
 	public static final int SANSTOPU = 2;
 	public static final int ONNUMARA = 3;
 
 	public static final int DIGITAL = 0; // 010100000...
 	public static final int RAWVALUE = 1; // 3 9 22 35 42 ...
 	// ********************
 	public static int GAMETYPE = SAYISALLOTO; // default
 
 	// 0 for all inputs 01 feeded or
 	// 1 1 DIV OUT possible values feeded
 	public static int INPUTVALUETYPE = RAWVALUE; // DIGITAL or RAWVALUE
 
 	// ****************************
 	public static int INPUTSIZE;
 	public static int INPUTSIZE2; // for sanstopu second set of 1 / 14
 
 	public final static int INPUT_SIZE_SAY49 = 49;
 	public final static int IDEAL_SIZE_SAY49 = 49;
 
 	public final static int INPUT_SIZE_SAY6 = 6;
 	public final static int IDEAL_SIZE_SAY6 = 6;
 
 	public final static int INPUT_SIZE_SUPER54 = 54;
 	public final static int IDEAL_SIZE_SUPER54 = 54;
 
 	public final static int INPUT_SIZE_SUPER6 = 6;
 	public final static int IDEAL_SIZE_SUPER6 = 6;
 
 	public final static int INPUT_SIZE_10NO80 = 80;
 	public final static int IDEAL_SIZE_10NO80 = 80;
 
 	public final static int INPUT_SIZE_10NO22 = 22;
 	public final static int IDEAL_SIZE_10NO22 = 22;
 
 	public final static int INPUT_SIZE_10NO10 = 10;
 	public final static int IDEAL_SIZE_10NO10 = 10;
 
 	public static int JORDANHIDDENNEURONSIZE = 160;// 180 not success !!!
 	public static int ELMANHIDDENNEURONSIZE = 120; // 180
 	public static int FEEDFORWARDHIDDENNEURONSIZE = 120; // 180
 
 	// 0 from MSSQL 1 from .csv text file
 
 	public static int DATASOURCESQL = 0;
 	public static int DATASOURCECSV = 1;
 
 	public static int ISHYPERNEAT = 0;
 
 	public final static int INPUT_SIZE_SANSTOPUSET1 = 34;
 	public final static int IDEAL_SIZE_SANSTOPUSET1 = 34;
 
 	public final static int INPUT_SIZE_SANSTOPUSET1_5 = 5;
 	public final static int IDEAL_SIZE_SANSTOPUSET1_5 = 5;
 
 	public final static int INPUT_SIZE_SANSTOPUSET2 = 14;
 	public final static int IDEAL_SIZE_SANSTOPUSET2 = 14;
 
 	public final static int INPUT_SIZE_SANSTOPUSET2_1 = 1;
 	public final static int IDEAL_SIZE_SANSTOPUSET2_1 = 1;
 
 	public final static int LO_WEEKNO = 1; // start week for train0 or 500 ?
 	public final static int HI_WEEKNO = 847; // end week for train TRAIN_SIZE
 
 	// Neat
 	// if population size is down much faster but target err rate is so slow
 	public static int NEATPOPULATIONSIZE; // 1000:8000 ideal if decrease
 	// if increase time increase
 	// 1200
 	// epoch and error increase
 	public static double NEATPOPULATIONDENSITY; // 1.0 0.45 0.35 0.3
 												// ideal?
 	// if increase time epoch decrease
 	public static double NEATDESIREDERROR; // 15 te 12 predict pop= 7000
 											// 0.109
 	// 0.14 te 9 Predict
 	// 0.01 En çabuk 0.24 0.32
 	// olabiliyor 0.1071 0.1063 0.11 fail
 
 	// HyperNEAT
 	public static int BASE_RESOLUTION; // 7
 
 	public static double JORDANDESIREDERROR;// 0.12; not success !!!
 	public static double ELMANDESIREDERROR; // 0.1058;
 	public static double FEEDFORWARDDESIREDERROR;
 
 	public static int EPOCHSAVEINTERVAL = 1000; // 1000
 
 	// public static int MULTIPLIER = 1; // default
 	public static int PRECISION = 2;
 
 	// Neural Simulated Annealing
 	public static final double SIMANNEAL_STARTTEMP = 10.0; // The starting
 															// temperature.
 	public static final double SIMANNEAL_STOPTEMP = 2.0; // 2.0 The ending
 	// temperature.
 	public static final int SIMANNEAL_CYCLES = 150; // 100 The number of cycles
 													// in a
 	// training iteration. try 300?
 	// Backpropagation
 	public static final double BPROP_LEARNRATE = 0.00001; // The rate at which
 															// the
 	// weight matrix will be
 	// adjusted based on
 	// learning.
 	public static final double BPROP_MOMENTUM = 0.05;// 0.0 The influence that
 	// previous
 	// iteration's training deltas
 	// will have on the current
 	// iteration.
 
 	public static final String trainCSVFile = basePathStr + "\\inp49.csv";
 	public static final String testCSVFile = basePathStr + "\\test49.csv";
 
 	public static final String JORDAN_FILENAME = basePathStr
 			+ "\\JordanLoto.eg";
 
 	public static final String JORDAN_DUMPFILENAME = basePathStr
 			+ "\\JordanLoto.txt";
 	public static final String JORDANFEEDFORWARD_FILENAME = basePathStr
 			+ "\\JordanFeedForwardLoto.eg";
 
 	public static final String NEAT_FILENAME = basePathStr + "\\NeatLoto.eg";
 	public static final String NEAT_SERIALFILENAME = basePathStr
 			+ "\\NeatLoto.ser";
 	public static final String NEAT_DUMPFILENAME = basePathStr
 			+ "\\NeatDump.txt";
 
 	public static final String ELMAN_FILENAME = basePathStr + "\\ElmanLoto.eg";
 	public static final String ELMAN_DUMPFILENAME = basePathStr
 			+ "\\ElmanLoto.txt";
 	public static final String ELMANFEEDFORWARD_FILENAME = basePathStr
 			+ "\\ElmanFeedForwardLoto.eg";
 
 	public static final String MaxWeekResultSQL = "select * from lotoresults where "
 			+ " weekid=(select max(weekid) from lotoresults) ";
 
 	public static final String InnerSQL = "(select weekid from lotoresults where "
 			+ "input1" + "=1 )";
 
 	public static final String RecurringSQL = "select lotoresults.weekid from lotoresults,"
 			+ InnerSQL
 			+ " as tbl"
 			+ "where lotoresults.weekid= tbl.weekid-1"
 			+ " and " + "lotoresults.input1=1";
 
 	public final static String SELECTSAY49SQL = "SELECT  `input1`,"
 			+ " `input2`," + " `input3`," + " `input4`," + " `input5`,"
 			+ " `input6`," + " `input7`," + " `input8`," + " `input9`,"
 			+ " `input10`," + " `input11`," + " `input12`," + " `input13`,"
 			+ " `input14`," + " `input15`," + " `input16`," + " `input17`,"
 			+ " `input18`," + " `input19`," + " `input20`," + " `input21`,"
 			+ " `input22`," + " `input23`," + " `input24`," + " `input25`,"
 			+ " `input26`," + " `input27`," + " `input28`," + " `input29`,"
 			+ " `input30`," + " `input31`," + " `input32`," + " `input33`,"
 			+ " `input34`," + " `input35`," + " `input36`," + " `input37`,"
 			+ " `input38`," + " `input39`," + " `input40`," + " `input41`,"
 			+ " `input42`," + " `input43`," + " `input44`," + " `input45`,"
 			+ " `input46`," + " `input47`," + " `input48`," + " `input49`,"
 			+ " `ideal1`," + " `ideal2`," + " `ideal3`," + " `ideal4`,"
 			+ " `ideal5`," + " `ideal6`," + " `ideal7`," + " `ideal8`,"
 			+ " `ideal9`," + " `ideal10`," + " `ideal11`," + " `ideal12`,"
 			+ " `ideal13`," + " `ideal14`," + " `ideal15`," + " `ideal16`,"
 			+ " `ideal17`," + " `ideal18`," + " `ideal19`," + " `ideal20`,"
 			+ " `ideal21`," + " `ideal22`," + " `ideal23`," + " `ideal24`,"
 			+ " `ideal25`," + " `ideal26`," + " `ideal27`," + " `ideal28`,"
 			+ " `ideal29`," + " `ideal30`," + " `ideal31`," + " `ideal32`,"
 			+ " `ideal33`," + " `ideal34`," + " `ideal35`," + " `ideal36`,"
 			+ " `ideal37`," + " `ideal38`," + " `ideal39`," + " `ideal40`,"
 			+ " `ideal41`," + " `ideal42`," + " `ideal43`," + " `ideal44`,"
 			+ " `ideal45`," + " `ideal46`," + " `ideal47`," + " `ideal48`,"
 			+ " `ideal49` ";
 
 	public final static String TRAINSAY49SQL = SELECTSAY49SQL
 			+ " FROM lotoresults " + " WHERE weekid<=" + HI_WEEKNO
 			+ " AND weekid>=" + LO_WEEKNO + " ORDER BY weekid";
 
 	public final static String TESTSAY49SQL = SELECTSAY49SQL
 			+ " FROM lotoresults " + " WHERE weekid>" + HI_WEEKNO
 			+ " ORDER BY weekid";
 
 	public final static String SELECTSAY6SQL = "SELECT  `input1`,"
 			+ " `input2`," + " `input3`," + " `input4`," + " `input5`,"
 			+ " `input6`," + " `ideal1`," + " `ideal2`," + " `ideal3`,"
 			+ " `ideal4`," + " `ideal5`," + " `ideal6` ";
 
 	public final static String TRAINSAY6SQL = SELECTSAY6SQL
 			+ " FROM lotoresults6 " + " WHERE weekid<=" + HI_WEEKNO
 			+ " AND weekid>=" + LO_WEEKNO + " ORDER BY weekid";
 
 	public final static String TESTSAY6SQL = SELECTSAY6SQL
 			+ " FROM lotoresults6 " + " WHERE weekid>" + HI_WEEKNO
 			+ " ORDER BY weekid";
 
 	public final static String SQL_DRIVER = "com.mysql.jdbc.Driver";
 	public final static String SQL_URL = "jdbc:mysql://localhost:3306/loto";
 	public final static String SQL_UID = "root";
 	public final static String SQL_PWD = "serdar";
 
 	public static String TRAINSQL;
 	public static String TESTSQL;
 	public static int INPUT_SIZE;
 	public static int IDEAL_SIZE;
 
 	public static final double SAYISALOTOMAX = 49.0;
 	public static final double SUPERLOTOMAX = 54.0;
 	public static final double SANSTOPUSET1MAX = 34.0;
 	public static final double SANSTOPUSET2MAX = 14.0;
 	public static final double NUMARA10MAX = 80.0;
 
 	public static final double MINVALUE = 0.48; // 0.0009;
 	public static final double IDEALDIGITALONEVALUE = 1.0;
 
 	// range check
 	public static final double ONEDIVSAYISAL = 1 / (SAYISALMAXSETVALUE49 - SAYISALMINSETVALUE1);
 	public static final double ONEDIVSUPER = 1 / SUPERLOTOMAX;
 	public static final double ONEDIVSANSTOPUSET1 = 1 / SANSTOPUSET1MAX;
 	public static final double ONEDIVSANSTOPUSET2 = 1 / SANSTOPUSET2MAX;
 	public static final double ONEDIVNUMARA10 = 1 / NUMARA10MAX;
 
 	public static final int NUM_DIGITS = 100;
 
 	public static SortedMap<Integer, Double> PREDICTMAPRESULT = new TreeMap<Integer, Double>();
 	public static SortedMap<Integer, Double> PREDICTMAP = new TreeMap<Integer, Double>();
 	public static SortedMap<Integer, Double> PREDICTLOWMAPRESULT = new TreeMap<Integer, Double>();
 	public static SortedMap<Integer, Double> PREDICTLOWMAP = new TreeMap<Integer, Double>();
 
 	public static String str_doubleFormat = "#.##";
 
 	//
 
 	public static final String TRIAL_COUNT = "fitness.function.tmaze.trial.count";
 	public static final String REWARD_SWITCH_COUNT = "fitness.function.tmaze.reward.switch.count";
 	public static final String REWARD_SWITCH_VARIATION = "fitness.function.tmaze.reward.switch.variation";
 	public static final String REWARD_LOW = "fitness.function.tmaze.reward.low";
 	public static final String REWARD_HIGH = "fitness.function.tmaze.reward.high";
 	public static final String REWARD_LOW_COLOUR = "fitness.function.tmaze.reward.low.colour";
 	public static final String REWARD_HIGH_COLOUR = "fitness.function.tmaze.reward.high.colour";
 	public static final String REWARD_CRASH = "fitness.function.tmaze.reward.crash";
 	public static final String PASSAGE_LENGTH = "fitness.function.tmaze.passage.length";
 	public static final String DOUBLE_TMAZE = "fitness.function.tmaze.double";
 
 	/*
 	 * private boolean isDouble; private int trialCount, rewardSwitchCount,
 	 * passageLength; private double rewardSwitchVariation; private double
 	 * rewardLow, rewardHigh, rewardCrash, rewardLowColour, rewardHighColour;
 	 * private int[] rewardSwitchTrials, rewardIndexForSwitch; private byte[][]
 	 * map; // The map of the maze, consists of values from // TYPE_*, format is
 	 * [x][y]. private int startX, startY; // Initial location of agent in map.
 	 * private int[] rewardLocationsX, rewardLocationsY;
 	 */
 	public void init() {
 
 		/*
 		 * (Properties props) { isDouble = *
 		 * props.getBooleanProperty(DOUBLE_TMAZE, false); trialCount =
 		 * props.getIntProperty(TRIAL_COUNT, 200); rewardSwitchCount =
 		 * props.getIntProperty(REWARD_SWITCH_COUNT, 3); rewardSwitchVariation =
 		 * props.getDoubleProperty(REWARD_SWITCH_VARIATION, 0.2); passageLength
 		 * = props.getIntProperty(PASSAGE_LENGTH, 3); rewardLow =
 		 * props.getDoubleProperty(REWARD_LOW, 0.1); rewardHigh =
 		 * props.getDoubleProperty(REWARD_HIGH, 1); rewardCrash =
 		 * props.getDoubleProperty(REWARD_CRASH, 0); rewardLowColour =
 		 * props.getDoubleProperty(REWARD_LOW_COLOUR, 0.2); rewardHighColour =
 		 * props.getDoubleProperty(REWARD_HIGH_COLOUR, 1); }
 		 */
 		log.debug("ConfigLoto init called");
 		// 0 for all inputs 01 feeded or
 		// 1 1 DIV OUT possible values feeded
 
 		switch (GAMETYPE) {
 		case SAYISALLOTO: {
 			if (INPUTVALUETYPE == DIGITAL) {
 				INPUTSIZE = SAYISALMAXSETVALUE49;
 			} else if (INPUTVALUETYPE == RAWVALUE) {
 				INPUTSIZE = SIZE6;
 			}
 			// decide to use binary 49 or 6 number dived by 49 syntax
 			if (INPUTSIZE == SAYISALMAXSETVALUE49) {
 				TRAINSQL = TRAINSAY49SQL; // SAY49 or SAY6
 				TESTSQL = TESTSAY49SQL; // SAY49 or SAY6
 				INPUT_SIZE = INPUT_SIZE_SAY49; // SAY49 or SAY6
 				IDEAL_SIZE = IDEAL_SIZE_SAY49; // SAY49 or SAY6
 
 				NEATPOPULATIONSIZE = 1000; // 1000:8000 ideal if decrease
 				// if increase time increase
 				// 1200
 				// epoch and error increase
 				NEATPOPULATIONDENSITY = 0.3; // 1.0 0.45 0.35 0.3
 												// ideal?
 				// if increase time epoch decrease
 				NEATDESIREDERROR = 0.14; // 15 te 12 predict pop= 7000
 
 				// HyperNEAT
 				BASE_RESOLUTION = 14; // 7
 
 				JORDANDESIREDERROR = 0.12;// 0.12; not success !!!
 				ELMANDESIREDERROR = 0.1059; // 0.1058;
 				FEEDFORWARDDESIREDERROR = 0.14;
 
 				// MULTIPLIER = 1;
 				PRECISION = 2;
 
 			} else if (INPUTSIZE == SIZE6) {
 				TRAINSQL = TRAINSAY6SQL; // SAY49 or SAY6
 				TESTSQL = TESTSAY6SQL; // SAY49 or SAY6
 
 				INPUT_SIZE = INPUT_SIZE_SAY6; // SAY49 or SAY6
 				IDEAL_SIZE = IDEAL_SIZE_SAY6; // SAY49 or SAY6
 
 				NEATPOPULATIONSIZE = 1000; // 1000:8000 ideal if decrease
 				// if increase time increase
 				// 1200
 				// epoch and error increase
 				NEATPOPULATIONDENSITY = 0.3; // 1.0 0.45 0.35 0.3
 												// ideal?
 				// if increase time epoch decrease
				NEATDESIREDERROR = 0.021;// 0.024;
 
 				// HyperNEAT
 				BASE_RESOLUTION = 14; // 7
 
 				JORDANDESIREDERROR = 0.12;// 0.12; not success !!!
 				ELMANDESIREDERROR = 0.1059; // 0.1058;
 				FEEDFORWARDDESIREDERROR = 0.14;
 
 				// MULTIPLIER = SAYISALMAXSETVALUE49;
 				PRECISION = 0;
 			}
 
 			break;
 		}
 		case SUPERLOTO: {
 			if (INPUTVALUETYPE == DIGITAL) {
 				INPUTSIZE = SIZE54;
 			} else if (INPUTVALUETYPE == RAWVALUE) {
 				INPUTSIZE = SIZE6;
 			}
 
 			break;
 		}
 		case SANSTOPU: {
 			if (INPUTVALUETYPE == DIGITAL) {
 				INPUTSIZE = SIZE34;
 				INPUTSIZE2 = SIZE14;
 			} else if (INPUTVALUETYPE == RAWVALUE) {
 				INPUTSIZE = SIZE5;
 				INPUTSIZE2 = SAYISALMINSETVALUE1;
 			}
 
 			break;
 		}
 		case ONNUMARA: {
 			if (INPUTVALUETYPE == DIGITAL) {
 				INPUTSIZE = SIZE80;
 			} else if (INPUTVALUETYPE == RAWVALUE) {
 				INPUTSIZE = SIZE22;
 			}
 			break;
 		}
 		default: {
 			break;
 		}
 		}
 
 	}
 
 	private static final ConfigLoto instance = new ConfigLoto();
 
 	private ConfigLoto() {
 		// call init
 		init();
 
 	}
 
 	public static ConfigLoto getInstance() {
 		return instance;
 	}
 
 	/**
 	 * Evaluate the network and display (to the logger) the output for every
 	 * value in the training set. Displays ideal and actual.
 	 * 
 	 * @param network
 	 *            The network to evaluate.
 	 * @param training
 	 *            The training set to evaluate.
 	 */
 	public static void evaluate(final MLRegression network,
 			final MLDataSet training) {
 		for (final MLDataPair pair : training) {
 			MLData calculatedOutput = network.compute(pair.getInput());
 
 			log.debug("Input=   "
 					+ ConfigLoto.formatData(pair.getInput(), PRECISION));
 			log.debug("Predict= " // actual
 					+ ConfigLoto.formatData(calculatedOutput, PRECISION + 2));
 			log.debug("Output=  " // ideal
 					+ ConfigLoto.formatData(pair.getIdeal(), PRECISION));
 
 			log.debug("Success Report ---------");
 			ConfigLoto.calculateSuccess(calculatedOutput, pair.getIdeal());
 		}
 	}
 
 	/**
 	 * Calculate success
 	 * 
 	 * @param actual
 	 * 
 	 * @param actual
 	 *            ideal
 	 * @return The success count
 	 */
 	public static void calculateSuccess(final MLData actual, final MLData ideal) {
 
 		int counterSuccess = 0;
 		int counterLowSuccess = 0;
 		int counterTotalPredict = 0;
 		int counterLowTotalPredict = 0;
 
 		SortedMap<Integer, Double> sortMap = new TreeMap<Integer, Double>();
 
 		PREDICTMAP.clear();
 		PREDICTMAPRESULT.clear();
 		PREDICTLOWMAP.clear();
 		PREDICTLOWMAPRESULT.clear();
 
 		switch (GAMETYPE) {
 		case SAYISALLOTO: {
 			if (INPUTSIZE == SAYISALMAXSETVALUE49) {
 				for (int i = 0; i < actual.size(); i++) {
 					if (ideal.getData(i) == IDEALDIGITALONEVALUE) // 1.0
 						if (actual.getData(i) > MINVALUE) {
 							counterSuccess++;
 							PREDICTMAPRESULT.put(i + 1,
 									round2(actual.getData(i)));
 						} else {
 							counterLowSuccess++;
 							PREDICTLOWMAPRESULT.put(i + 1,
 									round2(actual.getData(i)));
 						}
 
 					if (actual.getData(i) > MINVALUE) {
 						counterTotalPredict++;
 						PREDICTMAP.put(i + 1, round2(actual.getData(i)));
 					} else {
 						counterLowTotalPredict++;
 						PREDICTLOWMAP.put(i + 1, round2(actual.getData(i)));
 					}
 					sortMap.put(i + 1, round2(actual.getData(i)));
 				}
 
				log.debug("*****   HIGH  ************");
 
 				String str = "Successfull Predict Count= " + counterSuccess;
 				if (counterSuccess > 0) {
 					str += "-*-  >0   *-*-*-*-*-*-*-*-*-*-";
 				}
 				log.debug(str);
 				log.debug("Result= " + sortHashMapByValues(PREDICTMAPRESULT)); // sortHashMapByValues(
 				log.debug("Prediction= " + sortHashMapByValues(PREDICTMAP));
 				log.debug("Total Predict Count= " + counterTotalPredict);
 				log.debug("Sorted All Predict/Actual= "
 						+ sortHashMapByValues(sortMap));
 
 				log.debug("*****   LOW  ************");
 				log.debug("Successfull Low Predict Count<  " + MINVALUE + " = "
 						+ counterLowSuccess);
 				log.debug("Low Result= "
 						+ sortHashMapByValues(PREDICTLOWMAPRESULT));
 				log.debug("Low Prediction= "
 						+ sortHashMapByValues(PREDICTLOWMAP));
 				log.debug("Total Low Predict Count= " + counterLowTotalPredict);
 
 				log.debug("*************************************");
 
 			} else if (INPUTSIZE == SIZE6) {
 				for (int i = 0; i < actual.size(); i++) {
 					for (int j = 0; j < ideal.size(); j++) {
						if (ConfigLoto.denormalizeMapminmax(ideal.getData(j)) == Math
 								.round(ConfigLoto.denormalizeMapminmax(actual
 										.getData(i)))) {
 							// int y = (int)Math.round(x);
 							/*
 							 * log.debug(ideal.getData(j)* SIZE49);
 							 * log.debug(ideal.getData(j));
 							 * log.debug(actual.getData(i));
 							 * log.debug(ideal.getData(j)-actual.getData(i));
 							 */
 
 							PREDICTMAPRESULT.put((int) Math.round(ConfigLoto
 									.denormalizeMapminmax(ideal.getData(j))),
 									round2(ConfigLoto
 											.denormalizeMapminmax(actual
 													.getData(i))));
 						}
 					}
 					int predictNum = (int) Math.round(ConfigLoto
 							.denormalizeMapminmax(actual.getData(i)));
 					PREDICTMAP.put(predictNum, round2(ConfigLoto
 							.denormalizeMapminmax(actual.getData(i))));
 				}
 				counterSuccess = PREDICTMAPRESULT.size();
 				String str = "Successfull Predict Count= " + counterSuccess;
 				if (counterSuccess > 0) {
 					str += "-*-*-*-*-*-*-*-*-*-*-*-";
 				}
 				log.debug(str);
 				log.debug("Result= " + PREDICTMAPRESULT);
 				log.debug("Prediction= " + PREDICTMAP);
 				log.debug("*************************************");
 			}
 			break;
 		}
 
 		case SUPERLOTO: {
 
 			break;
 		}
 		case SANSTOPU: {
 
 			break;
 		}
 		case ONNUMARA: {
 
 			break;
 		}
 		default: {
 
 			break;
 		}
 
 		}
 	}
 
 	/**
 	 * Format neural data as a list of numbers.
 	 * 
 	 * @param data
 	 *            The neural data to format.
 	 * @return The formatted neural data.
 	 */
 	public static String formatData(final MLData data, int precision) {
 		final StringBuilder result = new StringBuilder();
 		for (int i = 0; i < data.size(); i++) {
 			if (i != 0) {
 				result.append("; ");
 			}
 			String str = Format
 					.formatDouble(
 							ConfigLoto.denormalizeMapminmax(data.getData(i)),
 							precision);
 			result.append(str);
 		}
 		return result.toString();
 	}
 
 	public static double round(double value) {
 		int decimalPlace = 2;
 		BigDecimal bd = new BigDecimal(value);
 		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_UP);
 		return bd.doubleValue();
 	}
 
 	public static double round2(double num) {
 		double result = num * NUM_DIGITS;
 		result = Math.round(result);
 		result = result / NUM_DIGITS;
 		return result;
 	}
 
 	/*
 	 * 
 	 * Normalize values to 0..1 or 0.1...0.9 as Heaton suggests
 	 */
 	public static double normalizeMapminmax(double num) {
 		int MAXsetvaluefaktor = 1;
 		int MINsetvaluefaktor = 1;
 
 		switch (GAMETYPE) {
 		case SAYISALLOTO: {
 			if (INPUTVALUETYPE == DIGITAL) {
 				MAXsetvaluefaktor = SAYISALMINSETVALUE1;
 				MINsetvaluefaktor = 0;
 			} else if (INPUTVALUETYPE == RAWVALUE) {
 				MAXsetvaluefaktor = SAYISALMAXSETVALUE49;
 				MINsetvaluefaktor = SAYISALMINSETVALUE1;
 			}
 			break;
 		}
 		default: {
 
 		}
 		}
 
 		double result = ((MAPMAX - MAPMIN) * (num - MINsetvaluefaktor));
 		result = result / (MAXsetvaluefaktor - MINsetvaluefaktor);
 		result = result + MAPMIN;
 		return result;
 	}
 
 	/*
 	 * 
 	 * DeNormalize to orig values from 0..1 or 0.1...0.9 as Heaton suggests
 	 */
 	public static double denormalizeMapminmax(double num) {
 		int MAXsetvaluefaktor = 1;
 		int MINsetvaluefaktor = 1;
 
 		switch (GAMETYPE) {
 		case SAYISALLOTO: {
 			if (INPUTVALUETYPE == DIGITAL) {
 				MAXsetvaluefaktor = SAYISALMINSETVALUE1;
 				MINsetvaluefaktor = 0;
 			} else if (INPUTVALUETYPE == RAWVALUE) {
 				MAXsetvaluefaktor = SAYISALMAXSETVALUE49;
 				MINsetvaluefaktor = SAYISALMINSETVALUE1;
 			}
 			break;
 		}
 		default: {
 
 		}
 		}
 
 		double result = num - MAPMIN;
 		result = result * (MAXsetvaluefaktor - MINsetvaluefaktor);
 		result = result / (MAPMAX - MAPMIN);
 		result = result + MINsetvaluefaktor;
 		return result;
 	}
 
 	/*
 	 * Sort Hashmap by Value
 	 */
 	public static LinkedHashMap<Integer, Double> sortHashMapByValues(
 			SortedMap<Integer, Double> passedMap) {
 		List<Integer> mapKeys = new ArrayList<Integer>(passedMap.keySet());
 		List<Double> mapValues = new ArrayList<Double>(passedMap.values());
 		Collections.sort(mapValues, Collections.reverseOrder());
 		Collections.sort(mapKeys, Collections.reverseOrder());
 
 		LinkedHashMap<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
 
 		Iterator<Double> valueIt = mapValues.iterator();
 		while (valueIt.hasNext()) {
 			Object val = valueIt.next();
 			Iterator<Integer> keyIt = mapKeys.iterator();
 
 			while (keyIt.hasNext()) {
 				Object key = keyIt.next();
 				String comp1 = passedMap.get(key).toString();
 				String comp2 = val.toString();
 
 				if (comp1.equals(comp2)) {
 					passedMap.remove(key);
 					mapKeys.remove(key);
 					sortedMap.put((Integer) key, (Double) val);
 					break;
 				}
 			}
 		}
 		return (LinkedHashMap<Integer, Double>) sortedMap;
 	}
 
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 }

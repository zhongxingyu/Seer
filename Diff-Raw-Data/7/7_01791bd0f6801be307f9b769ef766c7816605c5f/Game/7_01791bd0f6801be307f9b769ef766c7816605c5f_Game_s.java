 /*	Game class for BaseballSim
 	by Ian Zapolsky 6/6/13 */
 
 /* 	TODO: Standardize the usage of player vs batter variable names.
 	TODO: Improve pitcher subbing logic
 	TODO: Capture pitcher stats (W,L,SAV) */
 
 
 import java.util.Random;
 import java.io.*;
 
 public class Game {
 	
 	final int WALK = 0;
     final int SINGLE = 1;
     final int DOUBLE = 2;
     final int TRIPLE = 3;
     final int HOMERUN = 4;
     final int HBP = 5;
     final int STRIKEOUT = 6;
     final int OUT = 7;
 	League league;
 	Team home, away;
 	Player[] homeOrder, awayOrder, homeField, awayField;
 	Player homeP, awayP;
 	int inning, outsBefore, hRuns, aRuns, hSpot, aSpot;
 	Random rand;
 	File f;
 	PrintWriter w;
 	
 	public Game() { }	
 
 	public void playGame(Team init_home, Team init_away, League init_league, int[] homeInts, int[] awayInts) throws IOException {
 		w = new PrintWriter(new File("gamelog.txt"));
 		league = init_league;
 		home = init_home;
 		homeField = new Player[9];
 		homeOrder = new Player[9];
 
 		away = init_away;
 		awayField = new Player[9];
 		awayOrder = new Player[9];
 
 		for (int i = 0; i < 9; i++) {
 			homeOrder[i] = home.roster.get(homeInts[i]);
 			awayOrder[i] = away.roster.get(awayInts[i]);
 		}
 		homeP = home.roster.get(homeInts[9]);
 		awayP = away.roster.get(awayInts[9]);
 		buildFields();
 
 		hRuns = aRuns = hSpot = aSpot = 0;
 		inning = 1;
 		
 		printLog("-------------------------------------------------------------------------------------------\n");
 		printLog("Welcome to SimBaseball. Today's game is between the "+home+" and the "+away+"\n");
 		printLog("Starting pitcher for the "+home+" is "+homeP+". Starting pitcher for the "+away+" is "+awayP+"\n");
 		printLog("-------------------------------------------------------------------------------------------\n\n");
 		
 		// outline of game structure
 		
 		Field diamond = new Field(w);
 
 		while (aRuns == hRuns || inning < 10) {
 			printLog("Top of "+inning+". "+home+": "+hRuns+" "+away+": "+aRuns+"\n\n");
 			checkPitcherForSub(homeP);
 			diamond.resetField(homeField);
 			while ((outsBefore = diamond.getOuts()) < 3) {
 				printLog(awayOrder[aSpot]+" is up to bat.\n");
 				aRuns += diamond.updateField(matchup(homeP, awayOrder[aSpot], calcBattingAdjustment(homeP, awayOrder[aSpot])), awayOrder[aSpot]);
 				increment_aSpot();
 				if (outsBefore < diamond.getOuts())
 					printLog(""+diamond.getOuts()+" outs!\n");
 			}
 			printLog("Bottom of "+inning+". "+home+": "+hRuns+" "+away+": "+aRuns+"\n\n");
 			checkPitcherForSub(awayP);
 			diamond.resetField(awayField);
 			while ((outsBefore = diamond.getOuts()) < 3) {
 				if (hRuns > aRuns && inning > 8)
 					break;
 				printLog(homeOrder[hSpot]+" is up to bat.\n");
 				hRuns += diamond.updateField(matchup(homeP, homeOrder[hSpot], calcBattingAdjustment(awayP, homeOrder[hSpot])), homeOrder[hSpot]);
 				increment_hSpot();
 				if (outsBefore < diamond.getOuts())
 					printLog(""+diamond.getOuts()+" outs!\n");
 			}
 			inning++;
 		}
 
 		// print winner, create line score
 
 		if (aRuns > hRuns) {
 			printLog(""+away+" win, "+aRuns+" to "+hRuns+"\n\n");
 		}
 		else {
 			printLog(""+home+" win, "+hRuns+" to "+aRuns+"\n\n");
 		}
 		w.close();
 		
 	}
 
 	private double calcBattingAdjustment(Player pitcher, Player batter) {
 		if (pitcher.LR.equals("R")) {
 			if (batter.LR.equals("R"))
 				return -.015;
 			else
 				return .030;
 		}
 		else {
 			if (batter.LR.equals("L"))	
 				return -.090;
 			else
 				return .045;
 		}
 	}
 
 	private double calcPitchingAdjustment(Player pitcher) {
 		return -.05+(pitcher.pitchCount*.001);
 	}
 
 	private int matchup(Player pitcher, Player player, double adjustment) {
 
 		// 	Batter Stats  
 		double SingleAVG = player.singleAVG;
 		double DoubleAVG = player.doubleAVG;
 		double TripleAVG = player.tripleAVG;
 		double HRAVG = player.HRAVG;
 		double BBAVG = player.BBAVG;
 		double SOAVG = player.SOAVG;
 		double HBPAVG = player.HBPAVG;
 
 		// 	Adjust batting stats with normalized adjustment
 		SingleAVG += (adjustment*(player.singles/player.Hit));
 		DoubleAVG += (adjustment*(player.doubles/player.Hit));
 		TripleAVG += (adjustment*(player.triples/player.Hit));
 		HRAVG += (adjustment*(player.HR/player.Hit));
 	
 		// 	Pitcher Stats 
 		double SingleOVA = pitcher.pSingleAVG;
 		double DoubleOVA = pitcher.pDoubleAVG;
 		double TripleOVA = pitcher.pTripleAVG;
 		double HROVA = pitcher.pHRAVG;
 		double BBOVA = pitcher.pBBAVG;
 		double SOOVA = pitcher.pSOAVG;
 		double HBPOVA = pitcher.pHBPAVG;
 				
 		// 	Adjust pitching stats according to linear equation based on simulated pitch count
 		double adj = calcPitchingAdjustment(pitcher);
 		SingleOVA += (adj*SingleOVA);
 		DoubleOVA += (adj*DoubleOVA);
 		TripleOVA += (adj*TripleOVA);
 		HROVA += (adj*HROVA);
 		BBOVA += (adj*BBOVA);
 		HBPOVA += (adj*BBOVA);
 
 		// 	League Stats 
 		double lSingleOVA = league.leagueOVASingle;
 		double lDoubleOVA = league.leagueOVADouble;
 		double lTripleOVA = league.leagueOVATriple;
 		double lHROVA = league.leagueOVAHR;
 		double lBBOVA = league.leagueOVABB;
 		double lSOOVA = league.leagueOVASO;
 		double lHBPOVA = league.leagueOVAHBP;
 
 		// 	Bill James log5 adjusted batter stats
 		double aSingleleAVG = adjustStat(SingleAVG, SingleOVA, lSingleOVA);
 		double aDoubleAVG = adjustStat(DoubleAVG, DoubleOVA, lDoubleOVA);
 		double aTripleAVG = adjustStat(TripleAVG, TripleOVA, lTripleOVA);
 		double aHRAVG = adjustStat(HRAVG, HROVA, lHROVA);
 		double aBBAVG = adjustStat(BBAVG, BBOVA, lBBOVA);
 		double aSOAVG = adjustStat(SOAVG, SOOVA, lSOOVA);
 		double aHBPAVG = adjustStat(HBPAVG, HBPOVA, lHBPOVA);
 		
 		/*	
 		System.out.println("\nadjusted statistics\n");
 		System.out.println(aSingleleAVG);
 		System.out.println(aDoubleAVG);
 		System.out.println(aTripleAVG);
 		System.out.println(aHRAVG);
 		System.out.println(aBBAVG);
 		System.out.println(aSOAVG);
 		System.out.println(aHBPAVG);
 		*/
 
 		// 	Now sim outcome 
 		//  rand = new Random(System.currentTimeMillis());
 		rand = new Random();
 		double gen = rand.nextDouble();
 		int result;
 		/*	Code system for result so we can avoid the passing of bulky strings.
 			Each number corresponds to the last variable in the row of its if statement. */
 		if (gen <= aBBAVG)
 			result = WALK;
 		else if (gen <= aBBAVG+aSingleleAVG)
 			result = SINGLE;
 		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG)
 			result = DOUBLE;
 		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG+aTripleAVG)
 			result = TRIPLE;
 		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG+aTripleAVG+aHRAVG)
 			result = HOMERUN;
 		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG+aTripleAVG+aHRAVG+aHBPAVG)
 			result = HBP;
 		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG+aTripleAVG+aHRAVG+aHBPAVG+aSOAVG)
 			result = STRIKEOUT;
 		else
 			result = OUT;
 		return result;
 	}
 
 	private void checkPitcherForSub(Player pitcher) {
 		if (pitcher.pitchCount > 100 && inning < 8) {
 			if (pitcher == homeP) {
 				printLog("Pitching Substitution: "+home.bullpen.get(6)+" for "+homeP+". "+homeP+" leaves the game with "+((int)homeP.pitchCount)+" pitches.\n");
 				homeP = home.bullpen.get(6);
 				addHomeField(homeP);
 			}
 			else {
 				printLog("Pitching Substitution: "+away.bullpen.get(6)+" for "+awayP+". "+awayP+" leaves the game with "+((int)awayP.pitchCount)+" pitches.\n");
 				awayP = away.bullpen.get(6);
 				addHomeField(homeP);
 			}
 		}
 		else if (pitcher == homeP) {
 			if (hRuns > aRuns && inning > 7 && pitcher.pitchCount > 50) {
 				printLog("Pitching Substitution: Closer "+getCloser(home)+" for "+homeP+". "+homeP+" leaves the game with "+((int)homeP.pitchCount)+" pitches.\n");
 				homeP = getCloser(home);
 				addHomeField(homeP);
 			}
 		}
 		else {
 			if (aRuns > hRuns && inning > 7) {
 				printLog("Pitching Substitution: Closer "+getCloser(away)+" for "+awayP+". "+awayP+" leaves the game with "+((int)awayP.pitchCount)+" pitches.\n");
 				awayP = getCloser(home);
 				addAwayField(awayP);
 			}	
 		}
 	}
 
 	private Player getCloser(Team team) {
 		double max = 0;
 		Player closer = team.bullpen.get(0);
 		for (Player p : team.bullpen) {
 			if (p.SAV > max) {
 				if ((team == home && p != homeP) || (team == away && p != awayP)) {
 					max = p.SAV;
 					closer = p;
 				}
 			}
 		}
 		return closer;		
 	}
 
 		
 	private void buildFields() {
 		for (int i = 0; i < 9; i++) {
 			addHomeField(homeOrder[i]);
 			addAwayField(awayOrder[i]);
 		}
 		addHomeField(homeP);
 		addAwayField(awayP);
 	}
 
 	private void addAwayField(Player p) {
 		if (p.position.equals("P"))
 			awayField[0] = p;
 		else if (p.position.equals("C")) {
 			if (awayField[1] != null)
 				insertToEmptySlot(awayField, awayField[1], 1, 8);
 			awayField[1] = p;
 		}
 		else if (p.position.equals("1B")) {
 			if (awayField[2] != null)
 				insertToEmptySlot(awayField, awayField[2], 1, 8);
 			awayField[2] = p;
 		}
 		else if (p.position.equals("2B")) {
 			if (awayField[3] != null)
 				insertToEmptySlot(awayField, awayField[3], 1, 8);
 			awayField[3] = p;
 		}
 		else if (p.position.equals("3B")) {
 			if (awayField[4] != null)
 				insertToEmptySlot(awayField, awayField[4], 1, 8);
 			awayField[4] = p;
 		}
 		else if (p.position.equals("SS")) {
 			if (awayField[5] != null)
 				insertToEmptySlot(awayField, awayField[5], 1, 8);
 			awayField[5] = p;
 		}
 		else if (p.position.equals("LF")) {
 			if (awayField[6] != null)
 				insertToEmptySlot(awayField, awayField[6], 1, 8);
 			awayField[6] = p;
 		}
 		else if (p.position.equals("CF")) {
 			if (awayField[7] != null)
 				insertToEmptySlot(awayField, awayField[7], 1, 8);
 			awayField[7] = p;
 		}
 		else if (p.position.equals("RF")) {
 			if (awayField[8] != null)
 				insertToEmptySlot(awayField, awayField[8], 1, 8);
 			awayField[8] = p;
 		}
 		else if (p.position.equals("IF"))
 			insertToEmptySlot(awayField, p, 2, 5);
 		else if (p.position.equals("MI"))
 			insertToEmptySlot(awayField, p, 3, 5);
 		else if (p.position.equals("UT"))
 			insertToEmptySlot(awayField, p, 1, 8);
 		else if (p.position.equals("OF"))
 			insertToEmptySlot(awayField, p, 6, 8);
 	}
 
 	private void insertToEmptySlot(Player[] field, Player p, int startIndex, int endIndex) {
 		for (int i = startIndex; i <= endIndex; i++) {
 			if (field[i] == null) {
 				field[i] = p;
 				break;
 			}
 		}
 	}
 
 	private void addHomeField(Player p) {
 		if (p.position.equals("P"))
 			homeField[0] = p;
 		else if (p.position.equals("C")) {
 			if (homeField[1] != null)
 				insertToEmptySlot(homeField, homeField[1], 1, 8);
 			homeField[1] = p;
 		}
 		else if (p.position.equals("1B")) {
 			if (homeField[2] != null)
 				insertToEmptySlot(homeField, homeField[2], 1, 8);
 			homeField[2] = p;
 		}
 		else if (p.position.equals("2B")) {
 			if (homeField[3] != null)
 				insertToEmptySlot(homeField, homeField[3], 1, 8);
 			homeField[3] = p;
 		}
 		else if (p.position.equals("3B")) {
 			if (homeField[4] != null)
 				insertToEmptySlot(homeField, homeField[4], 1, 8);
 			homeField[4] = p;
 		}
 		else if (p.position.equals("SS")) {
 			if (homeField[5] != null)
 				insertToEmptySlot(homeField, homeField[5], 1, 8);
 			homeField[5] = p;
 		}
 		else if (p.position.equals("LF")) {
 			if (homeField[6] != null)
 				insertToEmptySlot(homeField, homeField[6], 1, 8);
 			homeField[6] = p;
 		}
 		else if (p.position.equals("CF")) {
 			if (awayField[7] != null)
 				insertToEmptySlot(homeField, homeField[7], 1, 8);
 			homeField[7] = p;
 		}
 		else if (p.position.equals("RF")) {
 			if (homeField[8] != null)
 				insertToEmptySlot(homeField, homeField[8], 1, 8);
 			homeField[8] = p;
 		}
 		else if (p.position.equals("IF"))
 			insertToEmptySlot(homeField, p, 2, 5);
 		else if (p.position.equals("MI"))
 			insertToEmptySlot(homeField, p, 3, 5);
 		else if (p.position.equals("UT"))
 			insertToEmptySlot(homeField, p, 1, 8);
 		else if (p.position.equals("OF"))
 			insertToEmptySlot(homeField, p, 6, 8);
 	}
 	
 	private void printField(Player[] field) {
 		for (Player p : field)
 			System.out.println(p);
 	}
 
 	private void printLog(String log) {
 		//System.out.print(log);
 		w.print(log);
 	}
 
 	private void increment_aSpot() {
 		if (aSpot == 8)
 			aSpot = 0;
 		else
 			aSpot++;
 	}
 	private void increment_hSpot() {
 		if (hSpot == 8)
 			hSpot = 0;
 		else
 			hSpot++;
 	}
 
 	private boolean isOn(int result) {
 		if (result < 6)
 			return true;
 		else
 			return false;
 	}
 
 	private double adjustStat(double AVG, double OVA, double LOVA) {
 		return ((AVG*OVA)/LOVA)/((((AVG*OVA)/LOVA)+((1-AVG)*(1-OVA)/(1-LOVA))));
 	}
 
 }
 
 

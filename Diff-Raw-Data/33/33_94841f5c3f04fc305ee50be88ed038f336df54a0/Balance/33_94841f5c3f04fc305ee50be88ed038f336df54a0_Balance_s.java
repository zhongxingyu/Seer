 package com.gdxtest02;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 
 public class Balance {
 	private static final int TEST_DAMAGE = 0;
 	private static final int TEST_HEAL = 1;
 	private static final int TEST_DMGHEAL = 2;
 	Char player;
 	private int totaldmg;
 	private float bestdmg;
 	private Array<Integer> bestcombo;
 	private TestResult testresult;
 	private int numberofbests;
 	/**Array of Lists of how much damage each skill does each round
 	 * if there are 4 skills, there will be 4 lists, one for each round
 	 * each list will have how much damage that skill does on each round
 	 * if there are 10 rounds, that list will have 10 items
 	 */
 	private Array<Array<Float>> listsof_damageperskill;
 	/**A list of the difference in damage between the best damage skill
 	 * and the second best, for each round
 	 */
 	private Array<Float> listof_damagediff;
 	/**List of the ids of the skill that does the most dmg this round
 	 * 
 	 */
 	private Array<Integer> listof_bestdmgid;
 	private int maxrounds;
 	private float bestheal;
 	private float bestdmgheal;
 //	private int whattotest;
 
 	public Balance(Char c) {
 		player = c;
 	}
 
 	public float getAvgDps() {
 		float total = 0;
 		for (Action a : player.actions) {
 			total += a.getAvgDps();
 		}
 		return total;
 	}
 
 	/**Model 1: Dps vs Dps 
 	 * 
 	 * 1000 damage in 10 rounds
 	 */
 	public void testModel1() {
 		testDmgModelA(10, 1000, 0);
 		testDmgModelA(10, 1000, 1);
 	}
 
 	/** Model 2: Dps vs Healer
 	 * 
 	 * 1500 in 15 rounds
 	 */
 	public void testModel2() {
 		testDmgModelA(15, 1500, 0);
 		testDmgModelA(15, 1500, 1);
 	}
 	
 	/** Model 3: Healer vs Dps if he heals 
 	 * 
 	 * 1000 dmg in 15 rounds, heal 500 in 15 rounds
 	 */
 	public void testModel3() {
 		testDmgAndHeal(15, 1000f, 500f);
 		
 	}
 	
 	
 
 	private void testDmgAndHeal(int maxrounds, float damage, float heal) {
		testTree(maxrounds, TEST_DAMAGE);
		printTestResults();
		testTree(maxrounds, TEST_HEAL);
		printTestResults();
 		testTree(maxrounds, TEST_DMGHEAL);
 		printTestResults();
 	}
 
 	// TODO check for abilities with only 1 ability with a large cooldown
 	public void testDmgModelA(int maxrounds, float targetdamage, int type) {
 		log("testing model 1 on char " + player.getName());
 		this.maxrounds = maxrounds;
 		testresult = new TestResult();
 		if (type == 0) {
 			log("doing best combo test");
 			testresult = testBestCombo(maxrounds);
 		}
 		else if (type == 1) {
 			log("doing tree test");
 //			testresult = testTree(maxrounds);
 			testTree(maxrounds, TEST_DAMAGE);
 		}
 		else {
 			log("doing brute force test");
 			testresult = testAllCombinations(maxrounds);
 		}
 
 		printTestResults();
 		
 		int bestdmg = testresult.getBestdmg();
 		if (bestdmg > 0) {
 
 			float ratio = targetdamage/bestdmg;
			balanceDmg(player, ratio);
 
 		}
 	}
 
 	/**Prints test results on log
 	 * 
 	 * @param maxrounds
 	 * @return
 	 */
 	private void printTestResults() {
 		int bestdmg = testresult.getBestdmg();
 		Array<Integer> bestcombo = testresult.getBestcombo();
 		int numberofbests = testresult.getNumberofbests();
 
 		log("test over, best dmgheal: " + bestdmgheal + " best dmg: " + bestdmg + " best heal: " + bestheal + " combo: " + bestcombo 
 				+ " number of bests: " + numberofbests);
 		int numberofskills = player.actions.size;
 		int totalsize = (int)Math.pow(numberofskills, maxrounds);
 		float pct = 100f*totaldmg/totalsize;
 		log("total loops: " + totaldmg + " of " + totalsize + " (" + pct + "%)");
 	}
 
 	/**Build a game tree to find the best combo
 	 * @param maxrounds
 	 * @return
 	 */
 	private void testTree(int maxrounds, int whattotest) {
 		// prepare for calculations
 		int round = 1;
 //		this.whattotest = whattotest;
 		prepareTest(); 
 		Array<Integer> combo = new Array<Integer>();
 		buildListsOfDamagesPerSkill(maxrounds, whattotest);
 		
 		// Start:
 		loopToNextBranches(maxrounds, round, combo, whattotest);
 
 	}
 
 	/**Initializes and resets the variables common to all tests
 	 * 
 	 */
 	private void prepareTest() {
 		totaldmg = 0;
 		bestdmg = 0;
 		bestheal = 0;
 		bestdmgheal = 0;
 		numberofbests = 0;
 		bestcombo = new Array<Integer>();
 	}
 
 	/**Build lists of how much damage each skill does each round
 	 * and a list of the difference between the highest dmg skill
 	 * and the second, records them on listsof_damageperskill
 	 * and listof_damagediff
 	 * @param maxrounds 
 	 * @param whattotest 
 	 */
 	private void buildListsOfDamagesPerSkill(int maxrounds, int whattotest) {
 		listsof_damageperskill = new Array<Array<Float>>();
 		listof_damagediff = new Array<Float>();
 		listof_bestdmgid = new Array<Integer>();
 		int numberofskills = player.actions.size;
 		float dmg;
 		for (int id = 1; id <= numberofskills; id++) {
 			Action a = player.getAction(id);
 			Array<Float> damagelist = new Array<Float>();
 			listsof_damageperskill.add(damagelist);
 			for (int round = 1; round <= maxrounds; round++) {
 				dmg = getOutputOfAction(a, round, maxrounds, whattotest);
 				damagelist.add(dmg);
 				
 			}
 		}
 		log("lists of dmg: " + listsof_damageperskill);
 		
 		// build list of differences
 		float best; float second; float delta; int id;
 		float[] array;
 		for (int round = 1; round <= maxrounds; round++) {
 			array = getBestDmgThisRound(round);
 			best = array[0];
 			second = array[1];
 			id = (int) array[2];
 			delta = best - second;
 			listof_damagediff.add(delta);
 			listof_bestdmgid.add(id);
 		}
 		
 		log("list of delta: " + listof_damagediff);
 		log("list of best dmg ids: " + listof_bestdmgid);
 		
 	}
 
 	private float getOutputOfAction(Action a, int round, int maxrounds, int whattotest) {
 		float total = 0;
 		if (whattotest == TEST_DAMAGE || whattotest == TEST_DMGHEAL) total += a.getDmgThisRound(round, maxrounds);
 		if (whattotest == TEST_HEAL || whattotest == TEST_DMGHEAL) total += a.getHealThisRound(round, maxrounds);
 		return total;
 	}
 
 	/**Returns 3 values, first is the best dmg this round, second is the second best
 	 * third is the best dmg id
 	 * @param round
 	 * @return
 	 */
 	private float[] getBestDmgThisRound(int round) {
 		int numberofskills = player.actions.size;
 //		if (numberofskills == 1) return listsof_damageperskill.get(0).get(round);
 		float best = 0; float second = 0; float dmg; int id = 0;
 		for (int i = 0; i < numberofskills; i++) {
 			dmg = listsof_damageperskill.get(i).get(round-1); 
 			if (dmg > best) {
 				second = best;
 				best = dmg;
 				id = i + 1;
 			}
 			else if (dmg > second) {
 				second = dmg;
 			}
 		}
 		float[] array = {best, second, id};
 		return array;
 	}
 
 	/**Loop through all next branches
 	 * 	
 	 *    o
 	 * [/ | \] -> You are here
 	 * o  o  o
 	 * 
 	 * @param round 
 	 * @param maxrounds 
 	 * @param combo 
 	 * @param whattotest 
 	 */
 	private void loopToNextBranches(int maxrounds, int round, Array<Integer> combo, int whattotest) {
 //		log("looping on round: " + round + "/" + maxrounds);
 
 		int numberofskills = player.actions.size;
 
 		for (int id = 1; id < numberofskills + 1; id++) {
 			Action a = player.getAction(id);
 			nextBranch(a, maxrounds, round, combo, whattotest);
 		}
 	}
 
 	/**Calculate the next branch on the game tree
 	 * 
 	 *    o
 	 *  / |[\] -> You are here
 	 * o  o  o
 	 * 
 	 * @param a
 	 * @param round 
 	 * @param maxrounds 
 	 * @param combo 
 	 * @param whattotest 
 	 */
 	private void nextBranch(Action a, int maxrounds, int round, Array<Integer> combo, int whattotest) {
 		int id = player.getIdOfAction(a);
 
 		combo = new Array<Integer>(combo);
 		combo.add(id);
 //		log("branch: " + id + " combo so far: " + combo);
 		
 		// Pruning goes here
 		
 		// cooldown prune, stop trying this combo is the ccurrent skill is on cd
 		if (isThisSkillOnCooldown(a, round, combo)) {
 //			log("yes, skill " + id + " is on cd on combo: " + combo + " prunning");
 			return;
 		}
 //		// obvious best dmg prune
 		if (shouldIPruneForDmg(a, round, maxrounds, combo, whattotest)) {
 //			log("pruning for dmg, skill: " + id + " combo: " + combo);
 			return;
 		}
 		
 		// Pruning ends here
 		
 		round++;
 		if (round <= maxrounds) {
 			loopToNextBranches(maxrounds, round, combo, whattotest);
 		}
 		else {
 			// finish, success
 			float dmgheal = calculateDmgFromCombo(combo, whattotest);
 			float dmg = calculateDmgFromCombo(combo, TEST_DAMAGE);
 			float heal = calculateDmgFromCombo(combo, TEST_HEAL); 
 //			log("and it's all over! Total loops: " + ++total + " combo: " + combo + " dmg: " +dmg);
 			totaldmg++;
 
 			addDmgToList(combo, dmgheal, dmg, heal);
 
 			buildTestResult();
 		}
 	}
 
 	/**Build together the test results and store them on the
 	 * testresult object
 	 * 
 	 */
 	private void buildTestResult() {
 		testresult = new TestResult();
 		testresult.setBestdmg((int) bestdmg);
 		testresult.setBestcombo(bestcombo);
 		testresult.setNumberofbests(numberofbests);
 	}
 
 	/**Adds this damage and combo to the list of best ones, if it's one of the 
 	 * best ones
 	 * 
 	 * @param combo
 	 * @param dmgheal
 	 */
 	private void addDmgToList(Array<Integer> combo, float dmgheal, float dmg, float heal) {
 		if (dmgheal > bestdmgheal) {
 			bestdmgheal = dmgheal;
 			bestcombo = combo;
 			numberofbests = 0;
 			bestheal = heal;
 			bestdmg = dmg;
 		}
 		if (dmgheal == bestdmgheal) {
 			numberofbests++;
 		}
 	}
 
 	/**Tests if I should prune this branch for max dmg
 	 * 
 	 * The basic idea here is that you are only not sure if the skill that does
 	 * the most dmg is the best option, if using it later would put this in place
 	 * of another skill that does less on different rounds (example, dots). If
 	 * all skills in the foreseable future (the number of skills, so one doesn't
 	 * get in place of another) do the same damage, then it's safe to
 	 * just use the one with the most damage on this round, and you wouldn't have
 	 * to think too hard about it.
 	 * 
 	 * @param a
 	 * @param round
 	 * @param maxrounds 
 	 * @param combo
 	 * @param whattotest 
 	 * @return true if pruning should be done, false otherwise
 	 */
 	private boolean shouldIPruneForDmg(Action a, int round, int maxrounds, Array<Integer> combo, int whattotest) {
 		// am I the strongest skill?
 		int id = player.getIdOfAction(a);
 		int bestavailable = getBestAvailable(combo, round, maxrounds, whattotest);
 //		log("combo: " + combo + " round: " + round + " id: " + id + " bestavailable: " + bestavailable);
 		if (id == bestavailable) {
 			return false;
 		}
 		// now I know I'm NOT the strongest available
 		
 		// does the delta change in the near future?
 		int roundsleft = maxrounds - round + 1;
 		// how long should I look in the future?
 		// TODO take skill cooldown into account?
 		int lookupsize = player.actions.size-1;
 		lookupsize = Math.min(lookupsize, roundsleft);
 		float delta = listof_damagediff.get(round-1);
 		float newdelta = 0f;
 		for (int i = 0; i < lookupsize; i++) {
 			newdelta = listof_damagediff.get(round-1 + i);
 			if (newdelta != delta) {
 				// yes, delta does change in the near future
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	private int getBestAvailable(Array<Integer> combo, int round, int maxrounds, int whattotest) {
 		int roundsleft = maxrounds - round + 1;
 		
 		int bestid = 0;
 		Action a;
 		float bestdmg = 0;
 		float dmg = 0;
 		for (int id = 1; id <= 4; id++) {
 			a = player.getAction(id);
 			if (a == null || isThisSkillOnCooldown(a, round, combo)) continue;
 			dmg = a.getDmgAfterRounds(roundsleft);
			dmg = getOutputOfAction(a, roundsleft, maxrounds, whattotest);
 			if (dmg > bestdmg) {
 				bestdmg = dmg;
 				bestid = id;
 			}
 		}
 
 		return bestid;
 	}
 
 	/**Calculates how much dmg a combshouldIPruneForDmgo does
 	 * @param combo
 	 * @param whattotest 
 	 * @return
 	 */
 	private float calculateDmgFromCombo(Array<Integer> combo, int whattotest) {
 		int maxrounds = combo.size;
 		if (maxrounds == 0) return 0;
 		float dmg = 0;
 		int id; Action a = null; int roundsleft;
 		for (int round = 1; round <= maxrounds; round++) {
 			id = combo.get(round-1); 
 			a = player.getAction(id);
 			roundsleft = maxrounds - round + 1;
 			if (isThisSkillOnCooldown(a, round, combo)) {
 //				log("skill " + id + " is on cd on round " + round);
 			}
 			else {
 //				log("round: " + round + " left: " + roundsleft + " skill: " + id + " dmg: " + a.getDmgAfterRounds(roundsleft));
 				if (whattotest == TEST_DAMAGE || whattotest == TEST_DMGHEAL) dmg += a.getDmgAfterRounds(roundsleft);
 				if (whattotest == TEST_HEAL || whattotest == TEST_DMGHEAL) dmg += a.getHealAfterRounds(roundsleft);
 			}
 		}
 //		log("combo: " + combo + " dmg: " + dmg);
 		return dmg;
 	}
 
 	/**Will check if this skill is on cooldown (cannot be used)
 	 * on this round if this combination
 	 * is used
 	 * 
 	 * @param a
 	 * @param round
 	 * @param combo
 	 * @return
 	 */
 	private boolean isThisSkillOnCooldown(Action a, int round,
 			Array<Integer> combo) {
 //		log("checking if skill " + player.getIdOfAction(a) + " is on cd on round " + round);
 		if (round == 1) return false;
 		int cd = a.getCooldown();
 		int lastuse = lastTimeThisSkillWasUsed(a, round, combo);
 		if (lastuse == 0) return false;
 		int timesincelastuse = round - lastuse;
 		if (timesincelastuse <= cd) {
 //			log("yes");
 			return true;
 		}
 		else {
 //			log("no, cd: " + cd + " lastuse: " + lastuse + " timesince: " + timesincelastuse);
 			return false;
 		}
 	}
 
 	/**The last time this skill was used in this combination, before this 
 	 * specific round
 	 * 
 	 * @param a
 	 * @param round
 	 * @param combo
 	 * @return
 	 */
 	private int lastTimeThisSkillWasUsed(Action a, int round,
 			Array<Integer> combo) {
 		int id = player.getIdOfAction(a);
 		Array<Integer> combobefore = new Array<Integer>(combo);
 		combobefore.truncate(round - 1);
 		return combobefore.lastIndexOf(id, true) + 1;
 //		log("last use of: " + id + " combobefore: " + combobefore + " lastuse: " + lastuse);
 	}
 
 	/**Will do only 1 simulation trying to use only the best skills available at
 	 * each round, then returns the results
 	 * 
 	 * @param maxrounds
 	 * @return
 	 */
 	private TestResult testBestCombo(int maxrounds) {
 		// prepare for simulation
 		Char dummy = new Char("dummy");
 		dummy.setMaxhp(100000);
 		int inihp = dummy.getMaxhp();
 		player.setTarget(dummy);
 		Array<Integer> combo = new Array<Integer>();
 		int dmg = 0;
 		player.resetStats();
 		dummy.resetStats();
 		int id = 1;
 		int roundsleft = maxrounds;
 
 		// start simulation
 		for (int round = 1; round <= maxrounds; round++) {
 
 			id = getNextBestSkill(player, roundsleft);
 			combo.add(id);
 			player.setActiveActionId(id);
 			player.updateAll();
 			dummy.updateAll();
 			dummy.applyDmg();
 			roundsleft--;
 		}
 		// simulation ended
 		// get damage done
 		dmg = (inihp - dummy.getHp());
 
 		// record results on a TestResult object and return it
 		TestResult testresult = new TestResult();
 		testresult.setBestdmg(dmg);
 		testresult.setBestcombo(combo);
 		testresult.setNumberofbests(1);
 		return testresult;
 	}
 
 	/**Returns what is the id of the best skill Char p can use,
 	 * based on which one does the best damage until game is over
 	 * and is off cooldown
 	 * 
 	 * @param p
 	 * @param roundsleft
 	 * @return
 	 */
 	private int getNextBestSkill(Char p, int roundsleft) {
 		int bestid = 0;
 		Action a;
 		float bestdmg = 0;
 		float dmg = 0;
 		for (int id = 1; id <= 4; id++) {
 			a = p.getAction(id);
 			if (a == null || a.getCurcooldown() > 0) continue;
 			dmg = a.getDmgAfterRounds(roundsleft);
 			if (dmg > bestdmg) {
 				bestdmg = dmg;
 				bestid = id;
 			}
 		}
 
 		return bestid;
 	}
 
 	/**Build all possible combinations of combos with maxrounds duration,
 	 * then will simulate all of them, then return the results
 	 * 
 	 * @param maxrounds
 	 * @return TestResult with best combo, dmg and number of bests
 	 */
 	private TestResult testAllCombinations(int maxrounds) {
 		Array<Array<Integer>> listofcombos = new Array<Array<Integer>>();
 		listofcombos = makeCombination(maxrounds, 4);
 		TestResult testresult = bruteForceAllCombos(maxrounds, listofcombos);
 		return testresult;
 	}
 
 	/**Takes a list of combos, then do simulations of all of them and return results
 	 * 
 	 * @param maxrounds
 	 * @param listofcombos
 	 * @return
 	 */
 	private TestResult bruteForceAllCombos(int maxrounds, 
 			Array<Array<Integer>> listofcombos) {
 
 		Char dummy = new Char("dummy");
 		dummy.setMaxhp(100000);
 
 		player.resetStats();
 		player.setTarget(dummy);
 
 		Array<Integer> bestcombo = new Array<Integer>();
 		int bestdmg = 0;
 		int dmg = 0;
 		int numberofbests = 0;
 
 		TestResult testresult = new TestResult();
 		for (Array<Integer> combo : listofcombos) {
 
 			dmg = getDmgFromCombo(maxrounds, dummy, combo);
 			//			log("dmg: " + dmg + " bestdmg: " + bestdmg);
 			if (dmg > bestdmg) {
 				bestdmg = dmg;
 				bestcombo = combo;
 				numberofbests = 0;
 			}
 			if (dmg == bestdmg) {
 				numberofbests++;
 			}
 		}
 
 		testresult.setBestdmg(bestdmg);
 		testresult.setBestcombo(bestcombo);
 		testresult.setNumberofbests(numberofbests);
 		return testresult;
 	}
 
 	/**Do one simulation of a fight and return the damage done
 	 * 
 	 * @param maxrounds
 	 * @param dummy
 	 * @param combo
 	 * @return
 	 */
 	private int getDmgFromCombo(int maxrounds, Char dummy, Array<Integer> combo) {
 		Action a = null;
 		int dmg;
 		int inihp = dummy.getMaxhp();
 		//			log("testing combo: " + combo.toString());
 		int id = 0;
 		player.resetStats();
 		dummy.resetStats();
 		for (int round = 1; round <= maxrounds; round++) {
 
 			try {id = combo.get(round-1);}
 			catch (Exception e) {log("out of bounds, no action to use this round");}
 			player.setActiveActionId(id);
 			a = player.getActiveAction();
 			//				log("round: " + round);
 			//				log("hp before: " + dummy.getHp());
 			//			if (a != null) {
 			//					log("using action: " + player.getActiveAction().getName());
 			//			}
 			player.updateAll();
 			dummy.updateAll();
 			dummy.applyDmg();
 			//				else log("action " + player.getActiveActionId() + "is null");
 			//				log("hp after: " + dummy.getHp());
 		}
 		dmg = (inihp - dummy.getHp());
 		return dmg;
 	}
 
 	/**Suggests a fix to balance the player skills using ratio
 	 * 
 	 * @param player
 	 * @param ratio
 	 */
	private void balanceDmg(Char player, float ratio) {
 		int id = 1;
 		player.getAction(id);
 		Array<Float> skillsdmg = new Array<Float>(); 
 		for (id = 1; id <= 4; id++) {
 			Action a = player.getAction(id);
 			if (a != null) {
 				skillsdmg.add(a.getPower()*ratio);
 			}
 			else skillsdmg.add(0f); 
 		}
 		log("ratio: " + ratio + " new skills: " + skillsdmg.toString());		
 	}
 
 	private void addCombo2(int maxrounds, Array<Array<Integer>> listofcombos) {
 		int round;
 		Array<Integer> actioncombo2 = new Array<Integer>();
 		for (round = 1; round <= maxrounds; round++) {
 			actioncombo2.add(round%3+1);
 		}
 		listofcombos.add(actioncombo2);
 	}
 
 	private void addCombo1(int maxrounds, Array<Array<Integer>> listofcombos) {
 		int round;
 		Array<Integer> actioncombo = new Array<Integer>();
 		for (round = 1; round <= maxrounds; round++) {
 			actioncombo.add(2);
 		}
 		listofcombos.add(actioncombo);
 	}
 
 	private Array<Array<Integer>> makeCombination(int sizex, int sizey) {
 		Array<Array<Integer>> list = new Array<Array<Integer>>();
 		//		int sizex = 10;
 		//		int sizey = 4;
 		int[] loop = new int[sizex];
 		for (int i : loop) {
 			loop[i] = 0;
 		}
 		int i = 0;
 
 		recb(list, loop, sizex, sizey, i);
 
 		return list;
 		//		log("list: " + list);
 	}
 
 	private void recb(Array<Array<Integer>> list, int[] loop, int sizex, int sizey, int i) {
 		while (i<Math.pow(sizey,sizex)) {
 			Array<Integer> combo = new Array<Integer>();
 
 			for (int p = 0; p < sizex; p++) {
 				combo.add(loop[p]+1);	
 			}
 
 			//			log("combo(" + i + "): " + combo);
 			list.add(combo);
 
 			for (int d = 0; d < sizex; d++) {
 				if (loop[d] < sizey - 1) {
 					loop[d]++;
 					if (d>0) {
 						for (int a = d; a > 0; a--) {
 							loop[a-1] = 0;
 						}
 
 					}
 					break;
 				}
 			}
 			i++;
 		}
 	}
 
 	private void rec(Array<Array<Integer>> list, int[] loop, int size, int i) {
 		int l = 0;
 		for (l = 0; l < size; l++) {
 			for (loop[l] = 0; loop[l] < size; loop[l]++) {
 				Array<Integer> combo = new Array<Integer>();
 				for (int p = 0; p < size; p++) {
 					combo.add(loop[p]);
 				}
 				//			combo.add(c);
 				log("combo(" + i + "): " + combo);
 				list.add(combo);
 				i++;
 			}
 		}
 	}
 
 	private void reca(Array<Array<Integer>> list, int i, int c, int b) {
 		for (int a = 0; a < 3; a++) {
 			Array<Integer> combo = new Array<Integer>();
 			combo.add(a);
 			combo.add(b);
 			combo.add(c);
 			log("combo(" + i + "): " + combo);
 			list.add(combo);
 			i++;
 		}
 	}
 
 	/**Logs text to Gdx.app.log()
 	 * @param text
 	 */
 	private void log(String text) {
 		Gdx.app.log("gdxtest", text);
 	}
 
 }

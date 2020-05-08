 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.Queue;
 import java.util.Random;
 
 /**
  * TESTED: 
  * 		percentage -works
  * 		createRule -works
  * 		addRuleAndString -works
  * 		removeRule -works
  * 		evaluateRule1 -works
  * 		yourReward -works
  * 		andSoItGoes -works
  * 		queueMoves -works
  * 		nextMove -works
  * 		getPoints -works
  * 		evaluateRule0 -works for all values tested, but there are more combos, test ALL when graphics come in
  * 		moveEvaluate -seems to work from percentage testing
  * 
  * TO TEST: 
  *     
  *      
  * PROBLEMS: 
  * 		IF it is the first turn and you have something depending on a previous turn, it will spit out a cooperate with warning. No elegant solution yet.
  * 	
  * @author ClintFrank
  *
  */
 public class Player {
     //ID of the player - either row or column
     private String id;
     //Payoff for each combo (xCC = X cooperates and [opponent] cooperates. xCD = X cooperates and [opponent] defects)
     private int ruleCount, size, turns, curTurn, points, curReward; 
     private int[][] pointGrid; //used to hold point values and give Player its reward
     private int[] pointChangeHistory; //keeps track of points at every turn
     int[][] rules; //priority queue of rules that cannot exceed size variable
     String[] ruleString; //string list of all rules to be displayed in GUI
     private int[] pointCumChange; //keeps track of cumulative points
     private String[] whichRuleFired; 
 	private ArrayList ignoreRules;
     private boolean[] myMoves; //previous moves stored as boolean: true - cooperate / false - defect
     private boolean[] oppMoves; //previous moves stored as boolean: true - cooperate / false - defect
     private Queue<Boolean> futureMoves;	//future to be done
     private Random rand; //random shit for random shit
 	
 	
     /**
      * Initializes the Player class. Receives necessary variables for point assignment.
      */
     public Player(String id){
 	this.id = id;
 	size = 10;
 	ruleCount = 0;
 	rules = new int[size][];
 	ruleString = new String[size];
 	futureMoves = new LinkedList<Boolean>();
 	pointGrid = new int[2][2];
 	rand = new Random();
     }	
 	
 	
     /**
      * Sets the rest of the variables up after player has made choices in GUI
      * 
      * 
      * @param xCC (int) payoff for coop coop
      * @param xCD (int) payoff for coop defect
      * @param xDC (int) payoff for defect coop
      * @param xDD (int) payoff for defect defect
      * @param turns (int) number of turns that will be played in the game
      */
     public void setPayoffsAndTurns(int xCC, int xCD, int xDC, int xDD, int turns){
 	ignoreRules = new ArrayList();
 	points = 0;
 	curTurn = 0;
	this.turns = turns;
 	whichRuleFired = new String[turns];
  	pointChangeHistory = new int[turns];
 	myMoves = new boolean[turns];
 	oppMoves = new boolean[turns];
 	pointCumChange = new int[turns];
 	pointGrid[0][0]=xCC;
 	pointGrid[0][1]=xCD;
 	pointGrid[1][0]=xDC;
 	pointGrid[1][1]=xDD;
     }
 	
     /**
      * Loads in a new list of strategies
      * 
      * @param (int) strat
      */
     public void loadStrat(int strat){
 	ruleString = new String[size];
 	rules = new int[size][];
 	ruleCount = 0;
 	int[][] tempRules = RuleFactory.getStrat(strat);
 	for (int i=0;i<tempRules.length;i++){
 	    addRuleAndString(tempRules[i]);
 	}
     }
 	
     /**
      * With given move choices of both players, we find our point value, adds it to points, records it in 
      * pointChangeHistory, and records adds to a to be displayed String of point histories
      * 
      * @param me - (int) my choice
      * @param opp - (int) opponent choice
      */
     public void yourReward(int me, int opp){
 	curReward = pointGrid[me][opp];
 	points+=curReward;
 	pointChangeHistory[curTurn] = curReward;
 	pointCumChange[curTurn] = points;	
     }
 	
     /**
      * Either pulls the next move off of the moveQueue or 
      * @return
      */
     public boolean nextMove(){
 	int i=0;
 	if (futureMoves.size()==0){
 	    for (i=0;i<ruleCount;i++){
 		if (ignoreRules.contains(i)){
 			continue;
 		}	
 		else if(rules[i].length==9){ //here we get the right string to add
 		    if (evaluateRule0(rules[i])==true){
 			break;
 		    }
 		}
		else if (rules[i].length==2){
 		    evaluateRule1(rules[i]);
 		    break;
 		}
 	    }
 	    whichRuleFired[curTurn] = ruleString[i];
 	}
 	boolean move;
 	try{
 	    move = futureMoves.poll();
 	}
 	catch(Exception e){
 	    move=true;
 	    whichRuleFired[curTurn] = "None - No Rules were Applicable";
 	}
 	return move;
     }
 	
     /**
      *  updates history of moves of myself (myMoves) and opponent (oppMoves) and what turn it is (curTurn)
      *  
      * @param me
      * @param opp
      * @param curRound
      */
     public void andSoItGoes(boolean me, boolean opp, int curRound){
 	myMoves[curRound-1] = me;
 	oppMoves[curRound-1] = opp;
 	this.curTurn = curRound;
     }
 	
     //-----------VVV-----------------RULE EVALUATION/QUEUE------------------VVV-------------//	
 	
     /**
      * Evaluates rule0's results with given input.
      * 
      * Here is the sentence of logic this rule/code works from:
      * priority(0) [if/until](1) the [opponent/myself](2) does [move](3) [over||=/under](4) [number](5)% of times over the previous
      * [number](6) of moves, do [move](7) for the next [number/all](8) moves.
      * 
      * 
      * @param specs (int[]) array of specifications
      */
     public boolean evaluateRule0(int[] specs){ 
 	boolean doQueue, move3, any3, move7, percentFlag;
 	boolean[] tempMove = moveEvaluate(specs[3],false);
 	doQueue = false;
 	move3 = tempMove[0]; //3 [move]
 	any3 = tempMove[1];
 	tempMove = moveEvaluate(specs[7],true);
 	move7 = tempMove[0]; //7  
 	percentFlag = (any3 || percentage(specs[2], specs[6], move3, (specs[4]==0) ? true:false, specs[5])); //2 6 4 5
 	//if we choose if AND (any move by opponent OR conditional is met)
 	//System.out.println("REMOVE FLAG: "+removeFlag);
 	if ((specs[1]==0 && (any3 || percentFlag))){ //1
 	    doQueue = true;
 	}
 	//if we choose until and the condition HASN'T been met
 	else if (specs[1]==1 && !(any3 || percentFlag)){
 	    doQueue = true;
 	}
 	//if we choose until and the condition is met, we want to delete the UNTIL rule and enact the next one in the queue
 	else if (specs[1]==1 && (any3 || percentFlag)){
 		ignoreRules.add(specs[0]);
 	}
 	if (doQueue){
 	    queueMoves(move7, (specs[8]==-1) ? turns - curTurn : specs[8]); //7, 8 (if the input is -1, that means the rest of the game, if not, we give the actual input)
 	    return true;
 	}
 	return false;
     }
 	
     /**
      * Evaluates rule1's outcome with given input
      * 
      * Here is the sentence of logic this rule/code works from:
      * Otherwise, do [move]
      * 
      * @param specs (int[]) specifications of the rules
      * @return
      */
     public boolean evaluateRule1(int[] specs){
 	boolean[] move = moveEvaluate(specs[1],true);
 	queueMoves(move[0],1);
 	return true;
     }
 	
     /**
      * Queues up the next set of moves.
      * 
      * @param move (boolean) - the desired move to queue
      * @param count (int) - the number of times to queue that move
      */
     public void queueMoves (boolean move, int count){
 	for (int i=0;i<count;i++){
 	    futureMoves.add(move);
 	}
     }
 	
     /**
      * With given data below, this algorithm will give a boolean result answering the question: is this conditional true?
      * 
      * @param moveSetChoice (boolean[]) The moveSet we are look at (players/opponents)
      * @param numberOfMoves (int) the number of moves to look back
      * @param move (boolean) the desired move to search for
      * @param over (boolean) represents this decision: (over or equal to this percent OR under this percent)
      * @param percent (int) the percentage to be matched against
      * @return
      */
     public boolean percentage(int moveSetChoice, int numberOfMoves, boolean move, boolean over, int percent){
 	if (curTurn<numberOfMoves){ //nothing to compare at this stage
 	    return false;
 	}
 	boolean[] moveSet2 = (moveSetChoice==0) ? oppMoves:myMoves; //1 
 	int tempTurn = curTurn-1;
 	if (numberOfMoves==-1){ //If the input is -1, that means ALL previous moves. This accounts for that.
 	    numberOfMoves = curTurn;
 	}
 	int total = numberOfMoves;
 	int count = 0;
 	while(numberOfMoves>0 && tempTurn>=0){
 	    //System.out.println("moveSet2[tempTurn]: "+moveSet2[tempTurn]);
 	    if (moveSet2[tempTurn]==move){
 		count++;
 	    }			
 	    numberOfMoves--;
 	    tempTurn--;
 	}
 	//System.out.println("count: "+count);
 	//System.out.println("total: "+total);
 	if (over && ((float)count/total)>=percent*(.01)){ //if we want it to be greator than a certain percent
 	    //System.out.println("returned true");
 	    return true;
 	}
 	else if (!over && ((float)count/total)<percent*(.01)){ //if we want it to be less than a certain percent
 	    //System.out.println("returned true");
 	    return true;
 	}
 	//System.out.println("returned false");
 	return false; //if it fails both tests
     }
 	
     /**
      * Helper function that takes in a specification parameter (choice) and outputs what (boolean) move that actually represents 
      * 
      * @param choice (int) decimal representation of move (explained in comments below)
      * @param random (int) do we want a random move, or just the fact that we could give back either move
      * @return (boolean[]) (0) boolean move (1)
      */
     public boolean[] moveEvaluate(int choice, boolean random){
 	boolean move=true;
 	boolean[] temp = {true, false};
 	if (choice==0){ //0 - cooperate
 	    move=true;
 	}
 	else if (choice==1){ //1 - defect
 	    move=false; 
 	}
 	else if (choice == 6){
 	    if(random){
 		move = rand.nextBoolean(); //6 - Random move
 	    }
 	    else{
 		temp[1] = true; //6 - When this is true, it doesnt matter what move is given here, any move will do
 	    }
 	}
 	else if (curTurn>0){
 	    if (choice==2){ //2 - opponents last move
 		move = oppMoves[curTurn-1];
 	    }
 	    else if (choice==3){ //3 - opposite of opponents last move
 		move = !oppMoves[curTurn-1];
 	    }
 	    else if (choice==4){ //4 - my last move
 		move = myMoves[curTurn-1];
 	    }
 	    else if(choice==5){ //5 - opposite of my last move
 		move = !myMoves[curTurn-1];
 	    }
 	}
 	temp[0]=move;
 	return temp;
 		
     }
 
     //-----------^^^-----------------RULE EVALUATION------------------^^^-------------//
 
     //-----------VVV---------RULE CREATION/ADDITION/DELETION----------VVV-------------//	
 
     /**
      * creates rule using MoveRule class, then sends it over to addRule method to be added to the rule priority queue
      * 
      * @param ruleChoice - which rule are we creating?
      */
     public void createRule0(String rule){
 	int[] newRule = RuleFactory.getRule0(rule);
 	addRuleAndString(newRule);
     }
 	
     public void createRule1(String rule){
 	int[] newRule = RuleFactory.getRule1(rule);
 	addRuleAndString(newRule);
     }
 	
     /**
      * Adds a rule to the rule array and a string representation to the String array keeps them ordered by GUI user imputed priority
      * Holds as much as (size) variable. If a user adds another rule when the list is full, the rule of least prioirity is replaced.
      * 
      * @param newRule (int[]) list of parameters representing rule specifications
      */
     public boolean addRuleAndString(int[] newRule){
 	String newRuleString;
 	if (ruleCount==size){
 		return false;
 	}
 	//this is to make sure our priority is set correctly
 	if (newRule[0]>ruleCount){
 		newRule[0]=ruleCount;
 	}
 	if (newRule[0]<0){
 		newRule[0]=0;
 	} 
 	newRuleString=RuleFactory.getRuleString(newRule);
 	if (ruleCount==0){
 	    //System.out.println("New Rule");
 	    rules[0]=newRule;
 	    ruleString[0]=newRuleString;
 	    ruleCount++;
 		updateRuleString();
 	}
 	else{
 	    boolean assign = false;
 	    for (int i=0;i<=ruleCount;i++){
 		if (rules[i]==null){
 		    assign=true;
 		}
 		else if (newRule[0]<=rules[i][0]){
 		    for (int j=ruleCount;j>i;j--){
 			if (rules[j]!=null){
 			    rules[j][0]++;
 			}
 			rules[j]=rules[j-1];
 			ruleString[j] = ruleString[j-1];
 		    }
 		    assign=true;
 		}
 		if (assign){
 		    rules[i] = newRule;
 		    ruleString[i] = newRuleString;
 		    ruleCount++;
 		    break;
 		}
 	    }
 		updateRuleString();
 	}
 	return true;
     }
 	
     /**
      * Removes a rule from the list, maintains priority ordering and a gapless list
      * 
      * @param i (int) index of rule to be removed
      */
     public boolean removeRule(int i){
 		//System.out.println("Rule to be Deleted: "+i);
 		if (i<0 || i>=ruleCount){
 			return false;	
 		}
 		rules[i] = null;
 		ruleString[i] = null;
 		//System.out.println("ruleCount before: "+ruleCount);
 		if (ruleCount==1){
 			ruleString[0] = null;
 			rules[0]=null;	
 		}
 		else if (i==ruleCount-1){
 			ruleString[i] = null;
 			rules[i]=null;	
 		}
 		else{
 			for (int j=i;j<rules.length;j++){
 				if (rules[j+1]==null || j+1==rules.length){
 				rules[j]=null;
 				ruleString[j]=null;
 				break;
 				}
 				rules[j+1][0]--;
 				rules[j] = rules[j+1];
 				ruleString[j] = ruleString[j+1];
 			}
 		}
 		ruleCount--;
 		updateRuleString();
 		return true;
     }
 	
 	public void updateRuleString(){
 		for (int i=0;i<ruleCount;i++){
 			rules[i][0]=i;
 			ruleString[i]=RuleFactory.getRuleString(rules[i]);	
 		}
 	}
     //-----------^^^---------RULE CREATION/ADDITION/DELETION----------^^^-------------//	
 	
     //-----------VVV-----------STRING RETRUNS FOR DISPLAY-------------VVV-------------//
 
     public String getID() {
 	return id;
     }
     
     public int getPointCumChangeTurn(int i) {
 	return pointCumChange[i-1];
     }
 	
     public boolean getMyMoveTurn(int i){
 	return myMoves[i-1];
     }
 	
     public int getPointChangeHistoryTurn(int i){
 	return pointChangeHistory[i-1];
     }
 	
     public String getWhichRuleFiredTurn(int i){
		if (whichRuleFired[i-1] == null)
			return "Previous Queue";
 	return whichRuleFired[i-1];
     }
 	
     public String[] getRuleString(){
 	return ruleString;
     }
 	
     public int getPoints(){
 	return points;
     }
 	
 }
 

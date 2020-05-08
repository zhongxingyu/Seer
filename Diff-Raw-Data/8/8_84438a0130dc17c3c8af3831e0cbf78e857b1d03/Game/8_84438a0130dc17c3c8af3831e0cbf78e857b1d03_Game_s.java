 package wolves;
 
 // Inspired by http://puzzle.cisra.com.au/2008/quantumwerewolf.html
 
 import java.util.ArrayList;
 
 public class Game {
 
 	private int NumPlayers;
 	private int NumWolves;
 	private ArrayList<GameState> AllStates;
 	private double[][] Probabilities;
 
 	public Game(int inPlayers, int inWolves){
 
 		NumPlayers = inPlayers;
 		NumWolves = inWolves;
 		this.Initialise(); // AllStates now contains all possible initial gamestates.
 		this.UpdateProbabilities(); // Probabilities[][] now contains Player role probabilities:
 		// First index is the playerID, second is: 0-innocent 1-seer 2,3-dead versions, 4to(3+NumWolves) are wolves 
 		// (4+NumWolves)to(3+2*NumWolves) are dead wolves
 
 	}
 
 	private void Initialise(){
 		// Fill Vector with all possible game states for round one.
 
 		AllStates = new ArrayList<GameState>();
 
 		int[] WolfPack = new int[NumWolves];
 
 		for(int n = 1; n <= NumPlayers;n++){ // Loop for each player as Seer
 			int a = 0;
 			for(int i = 1; i <= NumWolves; i++){
 				if(i == n) a = 1;
 				WolfPack[i] = i+a;
 			}
 			boolean p = false;
 			do {
 				AllStates.add(new GameState(NumPlayers, NumWolves, n, WolfPack));
 				p = nextPack(WolfPack, n);
 			} while (p);

 		}
 	}
 
 	public void UpdateProbabilities(){
 		// Compute probabilities of each player being each role.
 		// Do this by calling AllPlayers method from GameState
 		// Compile a tally of each player being each thing against total states
 
 		int[][] RoleCount = new int[NumPlayers][4 + (2 * NumWolves)];
 		// First index is the playerID, second is: 0-innocent 1-seer 2,3-dead versions, 4to(4+NumWolves) are wolves
 		int n = 0;
 		for(GameState a : AllStates){
 			for(int i = 0; i < NumPlayers; i++){
 				int b = a.AllPlayers()[i];
 				if(b == 1){
 					RoleCount[i][0]++;
 				} else if(b == 2){
 					RoleCount[i][1]++;
 				} else if(b == -1){
 					RoleCount[i][2]++;					
 				} else if(b == -2){
 					RoleCount[i][3]++;
 				} else {
 					int m = 1;
 					if(b < 0){
 						m = m + NumWolves;
 						b = (-1) * b;
 					}
 					RoleCount[i][m+b]++;
 				}
 			}
 			n++;
 		}
 		Probabilities = new double[NumPlayers][4 + (2 * NumWolves)];
 		for(int m = 0; n < NumPlayers; n++){
 			for(int i = 0; i < (4 + (2 * NumWolves)); i++){
 				Probabilities[m][i] = RoleCount[m][i] / n;
 			}
 		}
 	}
 
 	public double[][] getProbabilities(){
 		return this.Probabilities;
 	}
 
 	public byte[] HaveVisions(int[] inTargets){ // 1 means innocent, 2 means wolf, and 0 means an input of zero, i.e. 
 		// player having vision cannot be seer.
 		byte[] output = new byte[NumPlayers];
 		for(int n = 0; n < NumPlayers; n++){
 			int TargetID = inTargets[n];
 			if(TargetID == 0){
 				output[n] = 0;
 			} else {
 				double TargetProbGood = 0;
 				for(int i = 0; i < 4; i++){
 					TargetProbGood += Probabilities[TargetID - 1][i];
 				}
 				output[n] = (byte) ((Math.random() < TargetProbGood) ? 1 : 2);
 			}
 		}
 
 		return output;
 	}
 
 	public void	LynchAllStates(int inTarget){
 		for(GameState a : AllStates){
 			if(a.Lynch(inTarget)){
 				// state is allowed
 			} else { //state was not allowed, and is removed.
 				AllStates.remove(a);
 			}
 		}
 	}
 
 	public double[] LivingProbabilities(){
 		double[] output = new double[NumPlayers];
 		for(int n = 0; n < NumPlayers; n++){
 			double ProbLiving = 0;
 			for(int i = 0; i < 4 + NumWolves; i++){
 				if((i == 2) || (i == 3)) {
 					// These are dead roles, ignore.
 				} else {
 					ProbLiving += Probabilities[n][i];
 				}
 			}
 			output[n] = ProbLiving;
 		}
 		return output;
 	}
 
 	private int CharacterCollapse(int inTarget){ // computes and returns role code of the freshly deceased.
 		double[] CumulProbs = new double[4 + (2*NumWolves)];
 		CumulProbs[0] = Probabilities[inTarget - 1][0];
 		for(int i = 1; i < (4 + (2*NumWolves)); i++){
 			CumulProbs[i] = Probabilities[inTarget - 1][i] + CumulProbs[i-1];
 		}
 		double ran = Math.random();
 		int i;
 		for(i = 0; i < (4 + (2*NumWolves)); i++){
 			if(ran <= CumulProbs[i]) break;
 		}
 		int Role = 0;
 		if(i == 0){
 			Role = 1;
 		} else if (i == 1) {
 			Role = 2;
 		} else {
 			Role = i - 1;
 		}
 		return Role;
 	}
 
 	public void AllStateCharCollapse(int inTarget){
 		int inRole = CharacterCollapse(inTarget);
 		for(GameState a : AllStates){
 			if(a.SurviveCollapse(inTarget, inRole)){
 				// do nothing, state is allowed.
 			} else {
 				AllStates.remove(a);
 			}
 		}
 	}
 
 	public void AttackAllStates(int[] inTargets){
 		for(GameState a : AllStates){
 			if(a.WolfAttack(inTargets)){
 				// state is allowed, and has been updated.
 			} else { //state was not allowed, and is removed
 				AllStates.remove(a);
 			}
 		}
 	}
 	
 	public void VisionAllStates(int[] inTargets, byte[] inVisions){
 		for(GameState a : AllStates){
 			if(a.SurviveVisions(inTargets, inVisions)){
 				// state is allowed, and has been updated.
 			} else { //state was not allowed, and is removed
 				AllStates.remove(a);
 			}
 		}
 	}
 
 	private boolean nextPack(int[] inPack, int inSeer){ // returns true if this was successful in finding the next 
 		// wolfpack.
 
 		int[] outPack = new int[NumWolves];
 		int[] possWolves = new int[NumPlayers -1]; // a list of all player IDs which can be wolves (i.e. all except seer).
 		int a = 0; // used to skip Seer in populating possWolves
 		for(int n = 0; n < NumPlayers - 1; n++){
 			if(n+1 == inSeer) a = 1;
 			possWolves[n] = n+1+a;
 		}
 
 		// have function (incrementWolf) which returns the next wolf ID, or zero if none
 		// run increment wolf on last wolf
 		// if zero, run on previous until not zero.
 		// then run on wolves skipped over to end.
 		int p = 0;
 		int i = inPack.length;
 		do {
 			i--;
 			p = incrementWolf(i, inPack, possWolves);
 			outPack[i] = p;
 		} while((p == 0) && (i != 0));
 		if(i == 0 ) return false; // This was the final wolfPack, inPack remains unchanged.
 		for(i = 0; i < inPack.length; i++){
 			if(outPack[i] == 0){
 				outPack[i] = incrementWolf(i, outPack, possWolves);
 			}
 		} // outPack now contains the next wolf pack
 
 		for(int n = 0; n < NumWolves; n++){
 			inPack[n] = outPack[n];
 		} // inPack is updated to next wolf pack. 
 
 		return true;
 
 	}
 
 	private static int incrementWolf(int inWolfIndex, int[] inPack, int[] inPossWolves){ // Does this need static ?
 		// returns the next wolf ID, or zero if none
 
 		int n = inPack[inWolfIndex];
 		boolean p = false;
		if(n == 0) n = inPossWolves[0];
 		do{			
 			if(n == 0) { // The wolf needs to 'roll over' 
 				n = inPossWolves[0];
 			} else if(n == inPossWolves[inPossWolves.length - 1]) { // There is no next wolf.
 				n = 0;
 			} else { // find the next wolf
 				for(int i = 0; i < inPossWolves.length - 1; i++){
 					if(n == inPossWolves[i]){
 						n = inPossWolves[i+1];
 					}
 				}
 			}
 			p = false;
 			for(int i = 0; i < inWolfIndex;i++){ // check for duplicates
 				if(n == inPack[i]) p = true;
 			}
 		} while(p);
 
 		return n;
 	}
 	
 	public int[] getKnownRoles(){
 		int[] output = new int[NumPlayers];
 		
 		for(int n = 0; n < NumPlayers; n++){
 			int index = -1;
 			int role = 0;
 			for(int i = 0; i < (4 + 2*NumWolves); i++){
 				if(Probabilities[n][i] == 1) index = i;
 			}
 			if(index == 0) role = 1;
 			if(index == 1) role = 2;
 			if(index == 2) role = -1;
 			if(index == 3) role = -2;
 			if((index > 3) && (index <= (3 + NumWolves))) role = index - 1;
 			if((index > (3 + NumWolves))) role = index - 1 - NumWolves;
 			output[n] = role;			
 		}		
 		return output;
 	}
 	
 	public void CollapseAllDead(){ // Collapses the roles of any dead characters
 		int[] KnownRoles = getKnownRoles();
 		double[] ProbLive = LivingProbabilities();
 		boolean repeat = true;
 		
 		while(repeat){
 			repeat = false;
 			for(int n = 0; n < NumPlayers; n++){
 				if(ProbLive[n] == 0){
 					if(KnownRoles[n] == 0){ // This player needs collapsing
 						AllStateCharCollapse(n+1); // This changes probabilities, but does not update them.
 						repeat = true;
 					}
 				}
 			}
 			UpdateProbabilities();
 			KnownRoles = getKnownRoles();
 			ProbLive = LivingProbabilities();
 		}
 		
 	}
 	
 	public byte CheckWin(){ // returns 0 for no win, 1 for innocents, or 2 for wolves.
 		byte output = 5;
 		// If there is a non-zero probability of one person from either team being alive, then return zero
 		double probLiveInnocent = 0;
 		double probLiveWolf = 0;
 		for(int n = 0; n < NumPlayers; n++){
 			probLiveInnocent += Probabilities[n][0] + Probabilities[n][1];
 			for(int i = 0; i < NumWolves; i++){
 				probLiveWolf += Probabilities[n][i+4];
 			}
 		}
 		boolean LiveInnocentsExist = (probLiveInnocent != 0);
 		boolean LiveWolvesExist = (probLiveWolf != 0);
 		if(LiveInnocentsExist && LiveWolvesExist) output = 0;
 		if(LiveInnocentsExist && !LiveWolvesExist) output = 1;
 		if(!LiveInnocentsExist && LiveWolvesExist) output = 2;
 		return output;
 	}
 
 }

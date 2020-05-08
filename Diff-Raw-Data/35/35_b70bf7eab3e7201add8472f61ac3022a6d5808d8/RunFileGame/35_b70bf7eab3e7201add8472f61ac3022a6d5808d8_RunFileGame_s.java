 package wolves;
 
 // Inspired by http://puzzle.cisra.com.au/2008/quantumwerewolf.html
 
 
 public class RunFileGame {
 	
 	private static int NumPlayers;
 	private static int NumWolves;
 	private static Game RunningGame;
 	private static WolvesUI ui;
 
 	public static void main(String[] args) {
 		
 		ui = new TextWolves(); //To be replaced with actual UI
 
 		NumPlayers = ui.getNumPlayers();
 		NumWolves = ui.getNumWolves();
 
 		RunningGame = new Game(NumPlayers,NumWolves);
 		// Game Object created, initialised, and probabilities updated.
 
 		boolean GameOver = false;
 
 		while(!GameOver){
 			// Each turn, one must:
 			// Take input of visions
 			int[] VisionTargets = InputVisionTargets();
 			byte[] Visions = RunningGame.HaveVisions(VisionTargets);
 			// Get output of visions, and inform players
 			OutputVisions(Visions);
 			// Update gamestates based on visions
 			RunningGame.VisionAllStates(VisionTargets, Visions);
 			// Take input of wolf attacks
 			int[] WolfTargets = InputWolfTargets();
 			// Update gamestates based on attacks
 			RunningGame.AttackAllStates(WolfTargets);
 			// Wake players
 			RunningGame.UpdateProbabilities();
 			RunningGame.CollapseAllDead();
 			DayTimeDisplay();
 			// Take Lynching target
 			int LynchTarget = InputLynchTarget();
 			// Update gamestates based on lynch
 			RunningGame.LynchAllStates(LynchTarget);
 			RunningGame.UpdateProbabilities();
 			// run CollapseAllDead()
 			RunningGame.CollapseAllDead();
 			DayTimeDisplay();
 			byte WinCode = RunningGame.CheckWin();
 			GameOver = (WinCode != 0);
 		}
 		// Game is now over
 		String WinningTeam = (RunningGame.CheckWin() == 1) ? "Innocents" : "Wolves";
 		System.out.println("Game Over. The " + WinningTeam + " have won.");
 			
 	}
 	
 	private static int[] InputVisionTargets(){ // return 0 if player cannot be seer.
 		return ui.inputSeerTargets();
 	}
 	
 	private static int[] InputWolfTargets(){ // return 0 if player cannot be a wolf.
 		return ui.inputWolfTargets();
 	}
 	
 	private static void OutputVisions(byte[] visions){
 		ui.displayVisions(visions);
 	}
 	
 	private static int InputLynchTarget(){ // return playerID for highest voted.
 		return ui.inputLynchTarget();
 	}
 	
 	private static void DayTimeDisplay(){ // Must display Good/Evil/Alive/Dead probabilities.
 		double[][] Probabilities = RunningGame.getProbabilities();
 		double[] LiveProbs = RunningGame.LivingProbabilities();
 		double DisplayProbs[][] = new double[NumPlayers][4];
 		for(int n = 0; n < NumPlayers; n++){
 			DisplayProbs[n][0] = Probabilities[n][0] + Probabilities[n][1] + Probabilities[n][2] + Probabilities[n][3];
 			DisplayProbs[n][0] *= 100;
 			DisplayProbs[n][1] = 100 - DisplayProbs[n][0];
			DisplayProbs[n][2] = LiveProbs[n];
			DisplayProbs[n][2] *= 100;
			DisplayProbs[n][3] = 100 - DisplayProbs[n][0];
 		}
 		int[] RolesCodes = RunningGame.getKnownRoles();
 		for (int i = 0; i < RolesCodes.length; i++) {
 			if (RolesCodes[i] > 3) {
 				RolesCodes[i] = 3;
 			} else if (RolesCodes[i] < -3) {
 				RolesCodes[i] = -3;
 			}
 		}
 		ui.displayProbabilities(DisplayProbs, RolesCodes);		
 	}
 }

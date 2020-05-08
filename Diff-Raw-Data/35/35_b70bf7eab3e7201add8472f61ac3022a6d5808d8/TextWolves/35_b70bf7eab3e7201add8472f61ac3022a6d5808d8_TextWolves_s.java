 package wolves;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 public class TextWolves implements WolvesUI {
 	
 	private int players;
 	private int wolves;
 	private int[] recentTargets;
 	
 	private int getIntFromUser(String prompt) {
 		while (true) {
 			try {
 				return Integer.parseInt(getUserInput(prompt));
 			} catch (Exception e) {
 				System.out.println("FUCK OFF THATS NOT A NUMBER");
 			}
 		}
 	}
 	
 	@Override
 	public int getNumPlayers() {
 		this.players = getIntFromUser("PLEASE CHOOSE HOW MANY OF PEOPLE");
 		return players;
 	}
 	
 	@Override
 	public int getNumWolves() {
 		this.wolves = getIntFromUser("PLEASE CHOOSE HOW MANY OF WOLVES");
 		return wolves;
 	}
 	
 	private String getUserInput(String prompt) {
 		BufferedReader c = new BufferedReader(new InputStreamReader(System.in));
 		System.out.println(prompt);
 		try {
 			return c.readLine();
 		} catch (IOException e) {
 			throw (new RuntimeException(e));
 		}
 	}
 	
 	@Override
 	public int[] inputWolfTargets() {
 		int[] targets = new int[players];
 		for (int i = 0; i < players; i++) {
 			targets[i] = getIntFromUser("PLEASE CHOOSE WHO IS WOLFED DOWN BY PLAYER " + (i+1));
 		}
 		return targets;
 	}
 	
 	@Override
 	public int[] inputSeerTargets() {
 		int[] targets = new int[players];
 		this.recentTargets = new int[players];
 		for (int i = 0; i < players; i++) {
 			targets[i] = getIntFromUser("PLEASE CHOOSE WHO IS SAW BY PLAYER " + (i+1));
 			recentTargets[i] = targets[i];
 		}
 		return targets;
 	}
 	
 	@Override
 	public void displayVisions(byte[] visions) {
 		for (int i = 0; i < players; i++) {
 			String role = null;
 			switch (visions[i]) {
 			case WolvesUI.VISION_INNO:
 				role = "VILLAGER";
 				break;
 			case WolvesUI.VISION_WOLF:
 				role = "WHEREWOLF";
 				break;
 			}
 			if (visions[i] != 0) {
 				System.out.println("PLAYER " + (i+1) + " SEES THAT PLAYER " + (recentTargets[i]) + " IS " + getAdjective() + " " + role);
 			}
 		}
 	}
 	
 	private String getAdjective() {
 		String[] words = {"A FILTHY", "A CLEAN", "A CAPRICIOUS", "A WELL-INFORMED", "AN INSANE", "A SANITARY", "A TUMULTUOUS",
 			"AN INFLAMED", "AN INFLAMMATORY", "A CALAMATOUS", "AN INCONGRUOUS", "A RADISHY", "A FERAL", "A FINGER-LICKIN'", 
 			"A PERNICIOUS", "A LOOPY", "A DIRTY", "AN ANGRY", "A FOCUSED", "AN ANTICLIMATIC", "A HORRIBLE",
 			"A SCARY", "A MONSTROUS", "A TERRIBLE", "A TERRIFYING", "A DESPICABLE", "A SHIT",
 			"AN OVERPRICED", "AN EGREGIOUS", "A GREGARIOUS", "A MISSPELLED", "A HORRIFIC", "A FUCKED-UP",
 			"AN ABOMINABLE", "A COLD-HEARTED", "A HERETICAL", "A CANTANKEROUS", "A POLISHED", "A HAPPY",
 			"A FUZZY", "A FLUFFY", "A CUTE", "A TECHNOLOGICAL", "A MEGARIFFIC", "A RURAL", "A MORBID",
 			"A GRIM", "AN ANIMALISTIC", "A FRIENDLY", "A KIND-HEARTED", "A MURDEROUS", "A DANGEROUS",
 		"A BRUTAL", "A CALM", "A PSYCHOTIC", "A PSYCOPATHIC", "AN EVIL"};
 		Random generator = new Random();
 		return words[generator.nextInt(words.length)];
 	}
 	
 	@Override
 	public int inputLynchTarget() {
 		return getIntFromUser("PLEASE CHOOSE WHO IS LUNCHED");
 	}
 	
 	@Override
 	public void displayProbabilities(double[][] probabilities, int[] knownRoles) {
 		System.out.println("SORRY THIS ARENT ALIGNED");
 		System.out.println("PLAYER GOOD EVIL ALIVE DEAD");
 		for (int i = 0; i < players; i++) {
			System.out.print(i + " ");
 			for (int j = 0; j < 4; j++) {
 				System.out.print(probabilities[i][j] + " ");
 			}
 			System.out.println();
 		}
 		System.out.println("KNOWN ROLLS");
 		for (int i = 0; i < players; i++) {
 			String role = null;
 			switch (knownRoles[i]) {
 			case ROLE_BLUE:
 				role = "VILLAGER";
 				break;
 			case ROLE_SEER:
 				role = "SEER";
 				break;
 			case ROLE_WOLF:
 				role = "WOLF";
 				break;
 			}
 			if (knownRoles[i] != 0) {
				System.out.println("PLAYER " + (i+1) + " IS " + getAdjective() + role);
 			}
 		}
 	}
 	
 }

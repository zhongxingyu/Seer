 import java.util.ArrayDeque;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Random;
 
 
 public class Monopoly 
 {
 	public static Space[] spaces = getSpaces();
 	public static HashMap<String, Space> spaceMap = getSpaceMap(spaces);
 	public static LinkedList<String> communityChest = getCommunityChestCards();
 	public static LinkedList<String> chance = getChanceCards();
 	public static ArrayDeque<Integer> pastMoves = new ArrayDeque<Integer>();
 	public static Random r = new Random();
 	public static int boardPos = 0;
 	
 	public static final int NUM_SPACES = 40;
 	public static final int NUM_MOVES = 10000000;
 	
 	
 	public static void main(String[] args)
 	{
 		for (int i = 0; i < NUM_MOVES; i++) {
 			makeMove();
 		}
 		
 		Arrays.sort(spaces);
 		String firstSpace = String.format("%02d", spaces[0].position);
 		String secondSpace = String.format("%02d", spaces[1].position);
 		String thirdSpace = String.format("%02d", spaces[2].position);
 		System.out.printf("%s%s%s\n", firstSpace, secondSpace, thirdSpace);
 	}
 	
 	
 	public static void makeMove()
 	{
 		int roll1 = r.nextInt(4) + 1;
 		int roll2 = r.nextInt(4) + 1;
 		Space resultSpace;
 		
 		if (sameRollThreeTimes(roll1, roll2)) {
 			resultSpace = spaceMap.get("JAIL");
 			boardPos = resultSpace.position;
 		}
 		else {
 			boardPos = (boardPos + roll1 + roll2) % NUM_SPACES;
 			Space currentSpace = spaces[boardPos];
 			resultSpace = processSpace(currentSpace);
 		}
 		
 		resultSpace.count++;
 	}
 	
 	public static boolean sameRollThreeTimes(int roll1, int roll2)
 	{
 		// Add new move
 		int smallerRoll = Math.min(roll1, roll2);
 		int largerRoll = Math.max(roll1,  roll2);
 		int moveHash = String.format("%d %d", smallerRoll, largerRoll).hashCode();
 		pastMoves.add(moveHash);
 		
 		if (pastMoves.size() < 3)
 			return false;
 		
 		for (int i : pastMoves) {
 			if (i != moveHash) {
 				pastMoves.poll();
 				return false;
 			}
 		}
 		
 		pastMoves.clear();
 		return true;
 	}
 	
 	public static Space processSpace(Space s)
 	{
 		if (s.name.contains("CC") || s.name.contains("CH"))
 			return processCardSpace(s);
 		
 		if (s.name.equals("G2J")) {
 			Space jailSpace = spaceMap.get("JAIL");
 			boardPos = jailSpace.position;
 			return jailSpace;
 		}
 		
 		// Didn't need to move anywhere
 		return s;
 	}
 	
 	
 	public static Space processCardSpace(Space s)
 	{
 		Space newSpace;
 		boolean moveCard = true;
 		if (s.name.contains("CC")) {
 			// Get first chance card, add to end of list
 			String currCard = communityChest.removeFirst();
 			communityChest.add(currCard);
 			
 			if (currCard.equals("")) {
 				newSpace = s;
 				moveCard = false;
 			}
 			else
 				newSpace = spaceMap.get(currCard.split(" ")[2]);	
 		}
 		else { // Chance card
 			String currCard = chance.removeFirst();
 			chance.add(currCard);
 			
 			if (currCard.equals("")) {
 				newSpace = s;
 				moveCard = false;
 			}
 			
 			else {
 				String[] instructions = currCard.split(" ");
 				if (instructions.length == 3) {
 					newSpace = spaceMap.get(instructions[2]); 
 				}
 				else if (instructions[2].equals("next")) {
 					newSpace = processNextCard(instructions[3]);
 				}
 				else { // Go back 3 squares
 					newSpace = spaces[(boardPos - 3) % NUM_SPACES];
 				}
 			}
 		}
	
        // Process the new space if it is a move card    
 		boardPos = newSpace.position;
 		if (moveCard) {
 			return processSpace(newSpace);
 		}
	
 		return newSpace;
 	}
 	
 	public static Space processNextCard(String spaceType)
 	{
 		// Find next R or U space
 		while (!spaces[boardPos].name.contains(spaceType)) {
 			boardPos = (boardPos + 1) % NUM_SPACES;
 		}
 		
 		return spaces[boardPos];
 	
 	}
 	
 	public static Space[] getSpaces()
 	{
 		Space[] spaces = new Space[NUM_SPACES];
 		String[] spaceNames = getSpaceKeys();
 		for (int i = 0; i < NUM_SPACES; i++) {
 			spaces[i] = new Space(spaceNames[i], i);
 		}
 		return spaces;
 	}
 	
 	public static HashMap<String, Space> getSpaceMap(Space[] spaces) 
 	{
 		HashMap<String, Space> spaceMap = new HashMap<String, Space>();
 		String[] spaceKeys = getSpaceKeys();
 		for (int i = 0; i < spaceKeys.length; i++) {
 			spaceMap.put(spaceKeys[i], spaces[i]);
 		}
 		return spaceMap;
 		
 	}
 	
 	public static LinkedList<String> getCommunityChestCards()
 	{
 		LinkedList<String> cards = new LinkedList<String>();
 		cards.add("Advance to GO");
 		cards.add("Go to JAIL");
 		for (int i = 0; i < 14; i++)
 			cards.add("");
 		return cards;
 	}
 	
 	public static LinkedList<String> getChanceCards()
 	{
 		LinkedList<String> cards = new LinkedList<String>();
 		for (String cardName : getChanceCardNames()) {
 			cards.add(cardName);
 		}
 		cards.add("");
 		cards.add("");
 		return cards;
 	}
 	
 	public static String[] getSpaceKeys() 
 	{
 		return new String[] {
 				"GO", "A1", "CC1", "A2", "T1", "R1", "B1", "CH1", "B2", "B3",
 				"JAIL", "C1", "U1", "C2", "C3", "R2", "D1", "CC2", "D2", "D3",
 				"FP", "E1", "CH2", "E2", "E3", "R3", "F1", "F2", "U2", "F3",
 				"G2J", "G1", "G2", "CC3", "G3", "R4", "CH3", "H1", "T2", "H2"};
 	}
 	
 	public static String[] getChanceCardNames()
 	{
 		return new String[] {
 				"Advance to GO", "Go to JAIL", "Go to C1", "Go to E3", "Go to H2",
 				"Go to R1", "Go to next R", "Go to next R", "Go to next U", 
 				"Go back 3 spaces" };
 	}
 	
 	
 	public static class Space implements Comparable<Space>
 	{
 		public String name;
 		public int position;
 		public int count;
 		
 		public Space(String name, int position) {
 			this.name = name; this.position = position; this.count = 0;
 		}
 
 		@Override
 		public int compareTo(Space other) {
 			if (count > other.count)  return -1;
 			if (count < other.count)  return 1;
 			return 0;
 		}
 	}
 }

 	/* GameBoard.java
 	 * This class will handle all logic involving the actions, and movements involved with the 
 	 * game Sorry! as well as storing all other associated objects and various statistics about 
 	 * the game thus far.  This class includes a representation of the track, deck, pawns, and 
 	 * records information about the game to be logged as statistics.
 	 */
 
 import java.util.Date; //for gameplay length
 import java.util.Random;
 
 /**
  * This class will handle all logic involving the actions, and movements
  * involved with the game Sorry! as well as storing all other associated objects and
  * various statistics about the game thus far. This class includes a representation of
  * the track, deck, pawns, and records information about the game to be logged as
  * statistics.
  *
  * @author Sam Brown, Caleb Cousins, Taylor Krammen, and Yucan Zhang
  */
 public class SRGameBoard {
 	//debug
 	private static boolean debug = true;
 	private static boolean pawnsStartHome = true;
 	
 	//constants
 	public static final int trackLength = 56;
 	public static final int safetyLength = 6;
 	public static final int[] safetyZoneIndex = {56,62};
 	public static final int[] safetyZoneEntrance = {2, 29};
 	public static final int[] startIndex = {4,32};
 	public static final int slideLength = 4;
 	public static final int[] slideIndex = {1,9, 15, 23, 29, 37, 43, 51}; 
 	
 	
 	
 	//gameplay	
 	public SRSquare[] track = new SRSquare[68];	//squares that make up the regular track, safety zones, and home squares
 	public SRSquare[] startSquares  = new SRSquare[2];	//indexes into the track representing where players may move their pawns into play
 	public SRDeck deck;	//Deck object used in this game
 	public SRPawn[] pawns  = new SRPawn[8];	//8 pawns used in the game
 	
 	
 	//statistics
 	public Date startTime;
 	public int elapsedTime; //elapsed time in seconds
 	public int numTurns; 	//turns taken
 	public int numBumps; 	//times player bumped another player
 	public int numStories;	//successful uses of Sorry! cards
 	public String cpuStyle;	//either nice or mean, how the computer was set to play
 	public int playerPawnsHome;	//number of pawns player got home
 	public int playerDistanceTraveled; //total number of squares traveled by the human player
 	public int cpuDistanceTraveled;	//"" "" "" "" computer
 	
 	public SRGameBoard(){
 		//start the track
 		for (int i=0;i<track.length;i++){
 			//make 'em
 			track[i] = new SRSquare();
 			//check if this one is slippery
 			for (int j=0; j<SRGameBoard.slideIndex.length; j++){
 				//take appropriate action if it is
 				if (i == SRGameBoard.slideIndex[j]){
 					track[i].slideLength = SRGameBoard.slideLength;
 				}
 			}
 		}
 		
 		//mark home squares
 		for (int i=0;i<SRGameBoard.safetyZoneIndex.length;i++){
 			this.track[SRGameBoard.safetyZoneIndex[i]+SRGameBoard.safetyLength-1].setIsHome(true);
 		}
 		
 		//create startSquares
 		for (int i=0;i<this.startSquares.length; i++){
 			this.startSquares[i] = new SRStartSquare();
 		}
 		
 		//shuffle the deck
 		this.deck = new SRDeck();
 		this.deck.shuffle();
 		
 		
 		//make some pawns
 		for (int i=0; i<this.pawns.length;i++){
 			if (i<4)
 				this.pawns[i] = new SRPawn(0);
 			else
 				this.pawns[i] = new SRPawn(1);
 			this.pawns[i].setID(i%4);
 		}
 		
 		this.cpuStyle = "easy";
 		this.startTime = new Date();
 		
 		
 		//testing:
 		if (SRGameBoard.debug){
 			System.out.println("GameBoard initialized.");//for testing
 		}
 		
 		
 		if (SRGameBoard.pawnsStartHome){
 			for (int i=0; i<this.pawns.length;i++){
 				//if (i!=0 && i!=5){
 				movePawnTo(this.pawns[i], SRGameBoard.safetyZoneIndex[this.pawns[i].getPlayer()]+SRGameBoard.safetyLength-3);
 				//}
 			}
 		}
 //		
 //		if (SRGameBoard.pawnsStartHome){
 //			for (int i=0; i<this.pawns.length;i++){
 //				if (i!=0 && i!=5){
 //					movePawnTo(this.pawns[i], SRGameBoard.safetyZoneIndex[this.pawns[i].getPlayer()]+SRGameBoard.safetyLength-1);
 //				}
 //			}
 //		}
 		
 	}
 	//getters
 	public SRPawn getPlayerPawn(int player, int number){
 		return this.pawns[(player*4+number)];
 	}
 	
 	public SRSquare getSquareAt(int index){
 		if (index >= 0 && index < this.track.length){
 			return this.track[index];
 		}
 		else{
 			throw new NullPointerException("There is no square at index "+index+".");
 		}
 	}
 	
 	
 	//card methods:
 	public SRCard drawCard(){
 		return deck.drawCard();
 	}
 	
 	//movement methods:
 	/**
 	 * This function uses the Pawn object is current location, as well as the Rules associated
 	 * with the Card to determine where on the board the Pawn may move to. These
 	 * locations on the board are returned as an array of integers, representing indices
 	 * into the GameBoard.track object.
 	 * 
 	 * @param pawn
 	 * @param card
 	 * @return
 	 */
 	public int[] findMoves(SRPawn pawn, SRCard card){
 		int [] finalArray = new int [0];
 		int [] nextMoves;
 		
 		for (int i=0;i<card.rules.length; i++){
 			nextMoves = findMoves(pawn, card.rules[i]);
 			finalArray = concatArrays(finalArray, nextMoves);
 		}
 		
 		return finalArray;
 	}
 	
 	public int[] findMoves(SRPawn pawn, SRRule rule){
 		int numMoves = rule.numMoves;//change when card truly has "rules"
 		int [] moveIndices;
 		boolean canSplit = rule.canSplit;
 		if (SRGameBoard.debug){
 			System.out.println("Entered findMoves with rule.  NumMoves is "+numMoves);
 		}
 		
 		//figure out the direction of the pawn beforehand so we only need one loop (handles
 		//both - and + move directions.)
 		int step = getStep(numMoves);
 		
 		//test for "special case" type moves
 		moveIndices = getSpecialMoves(pawn, rule);
 		if (moveIndices.length > 0){
 			return moveIndices;
 		}
 		
 		int currIndex = pawn.getTrackIndex();
 		if (currIndex == -1 && !rule.canStart){
 			return moveIndices;
 		}
 		//make room for indices
 		int [] regIndices = new int [numMoves*step];
 		int regIndicesCount = 0;
 		int [] safetyIndices = new int [numMoves*step];
 		int safetyIndicesCount = 0;
 		
 		//IF the pawn is on the regular track:
 		if (currIndex < SRGameBoard.trackLength){
 			regIndices = this.getNormalMoves(pawn.player, pawn.trackIndex, numMoves);
 			regIndicesCount = regIndices.length;
 			//if movement wasn't negative, maybe there was an opportunity to enter the safety zone!
 			if (step>0){
 				//System.out.println("And that is greater than zero...");
 				int numMovesLeft = 0; //number of moves left to make inside safety zone
 				boolean canEnterSafety = false;
 				for (int i=0; i<regIndices.length; i++){
 					if (regIndices[i] == SRGameBoard.safetyZoneEntrance[pawn.player]){
 						canEnterSafety = true;
 						numMovesLeft = numMoves-(i+1);
 					}
 				}
 				if (canEnterSafety){
 					//System.out.println("Can enter safety!");
 					int firstSafetyIndex = SRGameBoard.safetyZoneIndex[pawn.player];
 					safetyIndices = this.getSafetyMoves(pawn.player, firstSafetyIndex, numMovesLeft);
 					safetyIndicesCount = safetyIndices.length;
 					//determine whether the safety moves were valid
 					//set the count of safetyIndices appropriately
 					if ((numMovesLeft != safetyIndices.length) && (!canSplit)){
 						safetyIndicesCount = 0;
 					}
 				}//end if(canEnterSafety)
 			}
 		}
 		else{
 			safetyIndices = this.getSafetyMoves(pawn.player, pawn.trackIndex, numMoves);
 			safetyIndicesCount = safetyIndices.length;
 //			if (SRGameBoard.debug){
 //				for (int i=0; i<safetyIndices.length; i++){
 //					System.out.println("Safety indices: "+safetyIndices[i]);
 //				}
 //			}
 
 			if (step > 0 && safetyIndices.length < numMoves*step && !canSplit){
 				safetyIndicesCount = 0;
 			}
 			else if (step < 0){
 				int numMovesLeft = (numMoves*step - safetyIndices.length)*step;
 				regIndices = getNormalMoves(pawn.player, SRGameBoard.safetyZoneEntrance[pawn.player], numMovesLeft);
 				regIndicesCount = regIndices.length;
 			}
 		}
 		
 		if (regIndicesCount== 0){
 			regIndices = new int [0];
 		}
 		if (safetyIndicesCount == 0){
 			safetyIndices = new int [0];
 		}
 		
 		int [] finalArray = cleanUpMoveArrays(regIndices, safetyIndices, canSplit);
 		
 		if (SRGameBoard.debug){
 			System.out.println("FindMoves returning from end: ");
 			for (int i=0;i<finalArray.length;i++){
 				System.out.println("\t"+finalArray[i]);
 			}
 		}
 		return cleanUpMoveArrays(regIndices, safetyIndices, canSplit);
 	}
 	
 	/**
 	 * cleanUpMoveArrays takes the array of safety moves and the array
 	 * of regular board moves and 
 	 * @param regIndices
 	 * @param safetyIndices
 	 * @param canSplit
 	 * @return
 	 */
 	private int[] cleanUpMoveArrays(int[] regIndices, int[] safetyIndices, boolean canSplit) {
 		int regIndicesCount;
 		int safetyIndicesCount;
 		int [] moveIndices;
 		safetyIndicesCount = safetyIndices.length;
 		regIndicesCount = regIndices.length;
 		
 		//now set up array depending on if the pawn can split its moves or not
 		//can't split is only 2 long (could go safety, could not)
 		if (!canSplit){
 			moveIndices = new int [2]; //only 2 potential places to move if the card doesn't allow 
 											  //splitting
 		}
 		//if pawn can split its moves, we need way more space!
 		else{
 			moveIndices = new int [regIndices.length+safetyIndices.length]; //for now treat card number like number of moves
 								  //Worst case for length of list is numMoves*2, so this is the maximum 
 								  //length of the array.
 		}
 		
 		int indiceCount = 0;  //So count how many locations we're allowed to enter, so we can trim later.
 		//clean up arrays at the end based on how we can split our moves
 		if (canSplit){
 			for (int i = 0; i < regIndicesCount+safetyIndicesCount; i++){
 				if (i<regIndicesCount){
 					moveIndices[i] = regIndices[i];
 					indiceCount++;
 				}
 				else{
 					moveIndices[i] = safetyIndices[i-regIndicesCount];
 					indiceCount++;
 				}
 			}
 		}
 		else{
 			if (regIndicesCount > 0){
 				moveIndices[0] = regIndices[regIndices.length-1];
 				indiceCount++;
 			}
 			if (safetyIndicesCount >0 && regIndicesCount > 0 && indiceCount>0){
 				moveIndices[1] = safetyIndices[safetyIndices.length-1];
 				indiceCount++;
 			}
 			else if (safetyIndicesCount > 0){
 				moveIndices[0] = safetyIndices[safetyIndices.length-1];
 				indiceCount++;
 			}
 		}
 		
 		//output for debugging
 		if (SRGameBoard.debug){
 			for (int i=0; i<indiceCount; i++){
 				System.out.println("MoveIndices: " + moveIndices[i]);
 			}
 		}
 		return this.trimArray(moveIndices, indiceCount);
 	}
 	
 	/**
 	 * Calculate the direction of the move.
 	 * 
 	 * @param numMoves
 	 * @return
 	 */
 	private int getStep(int numMoves) {
 		int step = 1;
 		if (numMoves < 0){
 			step *= -1;
 		}
 		return step;
 	}
 	
 	//gets all "special" moves for cards (ie. sorry!, all non-generic movement)
 	private int []  getSpecialMoves(SRPawn pawn, SRRule rule) {
 		int[] moveIndices;
 		int indiceCount = 0;
 		int [] noMoves = new int[0];//just a dummy
 		int [] finalArray = new int [0];
 		
 		
 		//special cases:
 		//pawn is on start and card lets it start
 		//if pawn is on home
 		if (pawn.isOnHome()){
 			finalArray = noMoves;
 		}
 		else if (pawn.isOnStart() && rule.canStart){
 			moveIndices = new int [1];
 			moveIndices[0]  = SRGameBoard.startIndex[pawn.getPlayer()]; 
 			finalArray = moveIndices;
 		}
 		//pawn moving from start and can bump another pawn
 		//or if pawn is not on start and may switch places with another pawn
 		else if ((pawn.isOnStart() && rule.isSorry) || (!pawn.isOnStart() && rule.canSwitch)){
 			SRPawn [] otherPawns = this.getOpponentPawns(pawn.getPlayer());
 			moveIndices = new int [4];
 			for (int i=0; i<4;i++){
 				SRPawn otherPawn = otherPawns[i];
 				if (otherPawn.isOnHome() && !otherPawn.isOnStart() && otherPawn.safetyIndex == -1 ){
 					moveIndices[indiceCount] = otherPawn.trackIndex;
 				}
 			}
 			finalArray = this.trimArray(moveIndices, indiceCount);
 		}
 		//pawn is on start and card doesn't let it start
 		else if (pawn.isOnStart() && !rule.canStart){
 			finalArray = noMoves;
 		}
 
 		//debugging output of each move
 		if (SRGameBoard.debug){
 			System.out.println("getSpecialMoves returning: ");
 			for (int i=0;i<finalArray.length;i++){
 				System.out.println("\t"+finalArray[i]);
 			}
 		}
 		return finalArray;
 	}
 	
 	
 	//determines where the pawn can move on the board
 	public int [] getNormalMoves(int player, int currIndex, int numMoves){
 		int step = getStep(numMoves);
 		int [] regIndices = new int [numMoves*step];
 		int regIndicesCount = 0;
 		
 		//assume forward motion
 		int max = numMoves;//currIndex + numMoves;
 		int min = 0;
 		
 		//swap them if it isn't
		if (max < 0){
 			int temp = max;
 			max = min;
 			min = temp;
 		}
 		//otherwise adjust the range to be correct
 		else{
 			max++;
 			min++;
 		}
 		
 		//first find all possible moves on the normal track
 		for (int i=min;i < max; i++){
 			regIndices[regIndicesCount] = (currIndex+(i))%SRGameBoard.trackLength;
 			
 			//modulo of negative numbers doesn't work how we want, so do it by hand.
 			if (regIndices[regIndicesCount]<0){
 				regIndices[regIndicesCount] = SRGameBoard.trackLength+regIndices[regIndicesCount];
 			}
 			regIndicesCount += 1;
 		}
 		regIndices = this.trimArray(regIndices, regIndicesCount);//trim the array
 		
 		//if the movement was negative, the array is backwards, so straighten it out
 		if (step<0){
 			regIndices = reverseArray(regIndices);
 		}
 		
 		return trimArray(regIndices, regIndicesCount);
 	}
 	
 	//figures out where the pawn can move in the safetyZone
 	public int [] getSafetyMoves(int player, int currIndex, int numMoves){	
 		int safetyStart = SRGameBoard.safetyZoneIndex[player];
 		int safetyEnd = safetyStart + SRGameBoard.safetyLength;
 		int min = 0;
 		int max = 0 + numMoves;
 		if (numMoves < 0){
 			if (SRGameBoard.debug){
 				System.out.println("Switching direction for negative movement.");
 			}
 			int temp = min;
 			min = max;
 			max = temp;
 		}
 		int step = getStep(numMoves);
 		
 		int [] safetyIndices = new int [SRGameBoard.safetyLength];
 		int [] allIndices = new int [numMoves*step];
 		int indiceCount = 0;
 		int nextIndex;
 		
 		//first get all theoretical places the pawn can go
 		for (int i=min;i < max; i++){
 			nextIndex = (currIndex+(i+1));
 			allIndices[indiceCount] = nextIndex;
 			indiceCount++;
 			//System.out.println("Next index: "+nextIndex);
 		}
 		
 		indiceCount = 0;
 		//now trim the list down to only squares in the safety zone
 		for (int i=0; i<allIndices.length; i++){
 			//System.out.println("Checking index: "+allIndices[i]);
 			if (allIndices[i] < safetyEnd && allIndices[i] >= safetyStart){
 				//System.out.println("Passed!");
 				safetyIndices[indiceCount] = allIndices[i];
 				indiceCount++;
 			}
 		}
 		
 		return this.trimArray(safetyIndices, indiceCount); 
 	}
 	
 	//returns an array of SRPawns which belong to a given player
 	public SRPawn [] getPlayerPawns(int player){
 		SRPawn [] pawns = new SRPawn [4];
 		for (int i=4*player; i<(4*player)+4; i++){
 			pawns[i-(4*player)] = this.pawns[i]; 
 		}
 		return pawns;
 	}
 	
 	//returns an array of SRPawns which belong to a given opponent
 	public SRPawn [] getOpponentPawns(int player){
 		switch (player){
 			case 0: return this.getPlayerPawns(1);
 			case 1: return this.getPlayerPawns(0);
 		}
 		return new SRPawn [0];
 	}
 		
 	/**
 	 * This function handles the actual movement of the pawn, and the resultant bumping
 	 * and sliding that may occur. If a pawn is moved to a space with another pawn, the
 	 * second pawn will be moved back to Start. If the pawn is moved to a space with a
 	 * slide, the pawn will be moved a second time the distance of the slide.
 	 * 
 	 * Return a boolean true if move successful or false otherwise.
 	 * 
 	 * @param pawn
 	 * @param card
 	 * @return int 
 	 */
 	public int movePawnTo(SRPawn pawn, int location){
 		//System.out.print("movePawnTo breaking at ");
 		//int location = (pawn.getTrackIndex()+distance)%SRGameBoard.trackLength;
 		//check for starting pawns
 		int unalteredLocation = location;
 		if (location >= 0 && location < this.track.length){
 			location += this.track[location].getSlideLength();
 		}
 		//check for pawns going to start
 		else if (location < 0){
 			pawn.setOnStart(true);
 			//System.out.println("pawns going to start.");
 			return 1;
 		}
 		//check for pawns going out of bounds
 		else if (location > SRGameBoard.trackLength+SRGameBoard.safetyLength*2){
 			//System.out.println("pawns out of bounds.");
 			return 0;
 		}
 				
 		//check for pawns that are going home
 		if (this.track[location].isHome){
 			//System.out.println("(Player "+pawn.player+" pawn "+pawn.id+" is home!)");
 			pawn.setOnHome(true);
 		}
 		//check for pawns that are already home?  do we need to?  why not.
 		else if (pawn.isOnHome()){
 			//System.out.println("pawns already at home.");
 			return 0;
 		}
 		
 		//bump (only bump the pawns of the opposing player)
 		for (int i=pawn.player;i<this.pawns.length; i++){
 			boolean sameSquare = pawns[i].getTrackIndex() == location;
 			//bump the opponent
 			if (sameSquare && pawn.player != pawns[i].player){
 				this.bumpPawn(pawns[i]);
 				if (SRGameBoard.debug){
 					System.out.print("Bumped a piece and ");
 				}
 			}
 			//but don't move if you'll land on yourself
 			else if (sameSquare && !this.track[location].isHome){
 				//System.out.println("landing on yourself.");
 				return 0;
 			}
 		}
 
 		if (SRGameBoard.debug){
 			System.out.println("Moved player"+pawn.player+" pawn "+(location-pawn.getTrackIndex())+
 					           " squares from "+pawn.trackIndex+" to "+location+".");
 		}
 		
 		//System.out.println("the final return.");
 		
 		int currentIndex = pawn.getTrackIndex();
 		
 		System.out.println("Pawn current index is: " +currentIndex);
 		System.out.println("Pawn moving to: "+location);
 		
 		//move the pawn and slide too if we need it.
 		pawn.setTrackIndex(location);
 		int distance = getDistanceBetween(currentIndex, unalteredLocation);
 		System.out.println("Number of spaces: "+distance);
 		return distance;
 	}
 	
 	private int getDistanceBetween(int index1, int index2){
 		if (index1 < index2){
 			int temp = index1;
 			index1 = index2;
 			index2 = temp;
 		}
 		
 		int distance = 0;
 		int normalDist = 0;
 		//if they are only moving on the safety zone
 		if (index1 > this.trackLength && index2 > this.trackLength){
 			distance = index1 - index2;
 		}
 		//if they are only moving in the regular track
 		else if (0 <= index1 && index1 < this.trackLength &&
 				 0 <= index2 && index2 < this.trackLength ){
 			distance = index1 - index2;
 		}
 		//now if index1 is the safetyZone entrance
 		else {
 			//assume player 0 safetyZone
 			int zone = 0;
 			//check if it is player 1 
 			if (index1 > this.safetyZoneIndex[1]){
 				zone = 1;
 			}
 			distance =index1-this.safetyZoneIndex[zone];
 			normalDist = index2 - this.safetyZoneEntrance[zone];
 		}
 		
 		if (distance < 0){
 			distance*=-1;
 		}
 		if (normalDist < 0){
 			normalDist*=-1;
 		}
 		return distance+normalDist;
 	}
 	/**
 	 * Determines the location that the move will take the pawn to, and calls
 	 * the movePawnTo function.
 	 * 
 	 * @param pawn
 	 * @param card
 	 * @return
 	 * 
 	 */
 	
 	public void movePawn(SRPawn pawn, int distance){		
 		int location = pawn.getTrackIndex()+distance;
 		this.movePawnTo(pawn, location);// isSafety);
 	} 
 	 
 	/**
 	 * This function moves Pawn pawn back onto its start square.
 	 * 
 	 * @param pawn
 	 */
 	public void bumpPawn(SRPawn pawn){
 		System.out.println("\n(Player "+pawn.player+" pawn "+pawn.id+" was bumped.)\n");
 		pawn.bump();
 	}
 	
 	/**
 	 * This function moves Pawn pawn from its start square onto the square directly
 	 * adjacent to the start square.
 	 * 
 	 * @param pawn
 	 */
 	public void startPawn(SRPawn pawn){
 		this.movePawnTo(pawn, SRGameBoard.startIndex[pawn.player]);
 	}
 	
 	/**
 	 * This function checks to see whether a player has managed to place all 4 of his/her
 	 * pawns on his/her start square and returns True if this is the case. Otherwise it
 	 * returns False.
 	 * 
 	 * @param player
 	 * @return
 	 */
 	public boolean hasWon(int player){
 		int playerPawnIndex = player*4; //0 if player 0, 4 if player 1
 		
 		//loop through pawns, if any not on home the player hasn't won.
 		for (int i=playerPawnIndex; i<playerPawnIndex+4; i++){
 			if (this.pawns[i].onHome == false){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Performs after-game clean up and statistics prep.
 	 */
 	private void endGame(){
 		Date endTime = new Date();
 		//something like:
 		//elapsedTime = endTime - this.startTime
 	}
 	
 	//trims an array down to a predetermined length
 	private int [] trimArray(int [] toTrim, int len){
 		int [] newArray = new int [len];
 		for (int i = 0; i<len; i++){
 			newArray[i]=toTrim[i];
 		}
 		return newArray;
 	}
 	
 	//reverses an array
 	private int [] reverseArray(int [] toReverse){
 		int [] tempArray = new int [toReverse.length];
 		for (int i=0; i<toReverse.length; i++){
 			tempArray[tempArray.length-i-1] = toReverse[i];
 		}
 		return tempArray;
 	}
 	
 	//concatenates two arrays
 	private int [] concatArrays(int [] array1, int [] array2){
 		int [] finalArray = new int [array1.length + array2.length];
 		
 		System.arraycopy(array1,  0, finalArray, 0, array1.length);
 		System.arraycopy(array2,  0, finalArray, array1.length, array2.length);
 		
 		return finalArray;
 	}
 	
 	public static void main(String [] args){
 		SRGameBoard gb = new SRGameBoard();
 		
 		/*//test cases for SRGameBoard findMoves
 		//start a pawn on regular board to end on finish
 		SRPawn pawn = gb.pawns[0];
 		gb.movePawnTo(pawn,0);
 
 		//now get its moves:
 		SRCard card = new SRCard(1);
 		card.canSplitMoves = false;
 		int [] moves = gb.findMoves(pawn, card);
 		System.out.println("================");
 		
 		//start a pawn on safety to pass  finish
 		gb.movePawnTo(pawn,60);
 
 		//now get its moves:
 		card = new SRCard(14);
 		card.canSplitMoves = false;
 		moves = gb.findMoves(pawn, card);
 		System.out.println("================");
 		
 		//start a pawn on safety to hit finish
 		gb.movePawnTo(pawn,60);
 
 		//now get its moves:
 		card = new SRCard(4);
 		card.canSplitMoves = false;
 		moves = gb.findMoves(pawn, card);
 		System.out.println("================");
 		
 		//move a pawn backwards out of the safety zone
 		gb.movePawnTo(pawn,60);
 
 		//now get all its moves:
 		card = new SRCard(-14);
 		card.canSplitMoves = true;
 		moves = gb.findMoves(pawn, card);
 		System.out.println("================");
 		
 		//move a pawn onto a sliding square:
 		//move a pawn backwards out of the safety zone
 		gb.movePawnTo(pawn,0);
 		*/
 		
 		Random rand = new Random();
 		int [] moves;
 		int choice;
 		SRCard card;
 		SRPawn pawn;
 		int pawnIndex;
 		
 		//while (!gb.deck.isEmpty() && !gb.hasWon(0) && !gb.hasWon(1)){
 		for(int p=0;p<1;p++){
 			do{
 				pawnIndex = rand.nextInt(8);
 				pawnIndex = 4;
 				pawn = gb.pawns[pawnIndex];
 			}while(pawn.isOnHome());
 			
 			
 			
 			System.out.println("Moving pawn "+pawnIndex+" from "+pawn.getTrackIndex());
 			card = gb.deck.drawCard();
 			System.out.println("Trying to use card "+card.cardNum);
 			moves = gb.findMoves(pawn, card);
 			
 			for (int i =0; i<moves.length;i++){
 				System.out.println("Move ["+i+"] is "+moves[i]);
 			}
 			
 			if(moves.length>1){
 				choice  = rand.nextInt(moves.length);
 				gb.movePawnTo(pawn, moves[choice]);
 			}
 			else if (moves.length == 1){
 				gb.movePawnTo(pawn, moves[0]);
 			}
 			else{
 				System.out.println("No moves.");
 			}
 			System.out.println("= = = = = = = = = = = = ");
 			for (int i=0; i<gb.pawns.length;i++){
 				System.out.print("Player "+gb.pawns[i].player+" pawn "+gb.pawns[i].id+" is at "+gb.pawns[i].trackIndex);
 				System.out.print(" || onHome = " + gb.pawns[i].onHome + ", trackIndex = " +gb.pawns[i].trackIndex+"\n");
 			}
 			System.out.println("\n\n========================");
 			
 			if (gb.hasWon(0) || gb.hasWon(1)){
 				System.out.println("The game has been won!");
 				break;
 			}
 		}
 	}
 }

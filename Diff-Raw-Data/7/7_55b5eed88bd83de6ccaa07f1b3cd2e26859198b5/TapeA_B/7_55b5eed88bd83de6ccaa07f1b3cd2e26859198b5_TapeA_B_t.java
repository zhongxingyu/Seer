 public class TapeA_B implements Tape {
 	private Cell readhead;
 	
 	//private int repOkNb = -1; // not used
 	private boolean nullNextEncountered;
 	private boolean nullPreviousEncountered;
 	
 	private static final char B = ' '; // final variable for the Blank
 	
 	/**
 	 * @param :	init : the string that represent the tape
 	 * @post :		build a TapeA_B object, the readhead is the block 
 	 * 				after the last char of init
 	 * 				raise an exception if init is null or contains char not allowed
 	 */
 	public TapeA_B(String init) throws Exception {
 		// ensure something to transform in a tape
 		if (init == null)
 			throw new Exception();
 		
 		// to ensure the position of the read head is after the last block of init
 		String initB = init + " "; 
 		
 		// initialisation
 		readhead = null;
 		Cell tempNew = null;
 		int maxLength = initB.length ();
 		int i = 0;
 		char[] initChar = initB.toCharArray ();
 		
 		/**
 		 * building of the tape:
 		 * 	Inv: TODO
 		 * 	Var: TODO
 		 */
 		while(i < maxLength){
 			tempNew = new Cell();
 			testChar(initChar[i]); // throw exception if needed
 			tempNew.content = initChar[i];
 			tempNew.previous = readhead;
 			readhead = tempNew;
 			if (readhead.previous != null) // first cell: previous doesn't exist
 				readhead.previous.next = readhead;
 			i++;
 		}
 		
 		// finish the tape
 		readhead.next = null;
 		nullNextEncountered = true;
 		nullPreviousEncountered = (readhead.previous == null);
 	}
 	
 	/**
 	 * Test if the structure agreed the invariant of representation :
 	 * - characters are in the Gamma alphabet (from char n°32 to 126)
 	 * - no loop, linear course long the double linked list
 	 * - only one cell with a null previous and only one with a null next
 	 * - the readhead must be on the tape (not null)
 	 * - B zone (contains all the not blanc char) as little as possible
 	 * - B' (extended zone B with blanc at ends) zone as little as possible
 	 * @return :	true, if the object agreed the invariant of representation
 	 * 				false, if not
 	 * TODO : invariant et variant de boucle
 	 */
 	public boolean repOk() {
 		int test = 0;
 		
 		// ensure at least one block
 		if (readhead == null)
 			test++;
 			
 		// first search, to the left
 		Cell tempNow = readhead.next;
 		Cell tempPast = readhead;
 		while (tempNow != null && test == 0) {
 			// ensure an available char
 			try {
 				testChar(tempNow.content);
 			} catch (Exception e){
 				test++;
 			}
 			// ensure double linkage 
			if ((tempNow.previous != tempPast) || (tempNow == readhead))
 				test++;
 			tempPast = tempNow;
 			tempNow = tempPast.next;
 		}
 		// test if there is not too much blanc at the end (B' as little as possible)
 		if (test == 0 && tempPast != readhead && tempPast.content == B)
 			test++;
 			
 		// second search, to the right
 		tempNow = readhead.previous;
 		tempPast = readhead;
 		while (tempNow != null && test == 0) {
 			// ensure an available char
 			try {
 				testChar(tempNow.content);
 			} catch (Exception e) {
 				test++;
 			}
 			// ensure double linkage 
			if ((tempNow.next != tempPast) || (tempNow == readhead))
 				test++;
 			tempPast = tempNow;
 			tempNow = tempPast.previous;
 		}
 		// test if there is not too much blanc at the end (B' as little as possible)
 		if (test == 0 && tempPast != readhead && tempPast.content == B)
 			test++;
 		
 		return (test == 0);
 	}
 
 	/**
 	 * @pre: /
 	 * @post: true iff the symbol bellow the read head is 's'.
 	 *        An exception is produced if 's' is not valid
 	 */
 	public boolean isSymbol(char s) throws Exception {
 		testChar(s);
 		return readhead.content == s;
 	}
 
 	/**
 	 * @pre: /
 	 * @post: replace the symbol bellow the read hind by 's'.
 	 *        An exception is produced if 's' is not valid
 	 */
 	public void putSymbol(char s) throws Exception {
 		testChar(s); 
 		readhead.content = s; 
 	}
 
 	/**
 	 * @pre: TODO: nothing?
 	 * @post: move the read head to the cell on the left or do nothing if the
 	 *        tape only contains a blank.
 	 *        If the previous cell doesn't exist, create a new one with blank char.
 	 *        If the read head is on the last cell which is blank (and is not
 	 *         the only one), remove this cell.
 	 */
 	public void leftMove() {
 		try {
 			// case of basic tape with only one box with B inside
 			if (nullNextEncountered && nullPreviousEncountered && isSymbol(B))
 				return;
 			
 			// case of we are leaving a end of the tape and it is B 
 			// so we have to delete the last box ton maintain the invariant
 			if (nullNextEncountered && isSymbol(B))
 				readhead.previous.next = null;
 		} catch (Exception e) {
 			return; // problem with isSymbol: should not happen because the
 			        // symbol has been checked before (constructor or putSymbol)
 		}
 
 		// case of end of the tape and we have to go into the B territory
 		if (nullPreviousEncountered){
 			readhead.previous = new Cell();
 			readhead.previous.next = readhead;
 		}
 
 		// normal case
 		readhead = readhead.previous;
 		nullPreviousEncountered = (readhead.previous == null);
 		nullNextEncountered = false;
 	}
 
 	/**
 	 * @pre: TODO: nothing?
 	 * @post: move the read head to the cell on the right or do nothing if the
 	 *        tape only contains a blank.
 	 *        If the next cell doesn't exist, create a new one with blank char.
 	 *        If the read head is on the first cell which is blank (and is not
 	 *         the only one), remove this cell.
 	 */
 	public void rightMove() {
 		try {
 		// case of basic tape with only one box with B inside
 			if (nullPreviousEncountered && nullNextEncountered && isSymbol(B))
 				return;
 			// case of we are leaving a end of the tape and it is B 
 			// so we have to delete the last box ton maintain the invariant
 			if (nullPreviousEncountered && isSymbol(B)) // readhead.previous == null
 				readhead.next.previous = null;
 		} catch (Exception e) {
 			return; // problem with isSymbol: should not happen because the
 			        // symbol has been checked before (constructor or putSymbol)
 		}
 		// case of end of the tape and we have to go into the B territory
 		if (nullNextEncountered){
 			readhead.next = new Cell();
 			readhead.next.previous = readhead;
 		}
 		// normal case
 		readhead = readhead.next;
 		nullNextEncountered = (readhead.next == null);
 		nullPreviousEncountered = false;
 	}
 
 	/**
 	 * @pre: readhead valid and != null (always the case)
 	 * @post: return a String of the form alpha[s]beta with the content 
 	 * 		  of all char available on the tape.
 	 *        The char bellow the read head is surrounded by brackets (s).
 	 * 		  alpha is a suffixe of what is before the readhead
 	 * 		  beta is a prefixe of what is after the readhead
 	 */
 	public String toString() {
 		String answer = "[" + readhead.content + "]";
 		Cell temp;
 		temp = readhead.next;
 		/**
 		 * Inv: answer contains the string [s]beta' where beta' is the 
 		 * 		string representation up to the previous cell of temp.
 		 * Var: at each steps, temp take the value of temp.next so temp
 		 * 		contains the first cell after readhead with the content
 		 * 		is not already in answer
 		 */
 		while (temp != null){
 			answer = answer + temp.content;
 			temp = temp.next;
 		}
 		temp = readhead.previous;
 		/**
 		 * Inv: answer contains the string alpha'[s]beta where alpha' is the 
 		 * 		string representation up to the next cell of temp.
 		 * Var: at each steps, temp take the value of temp.previous so temp
 		 * 		contains the first cell before readhead with the content
 		 * 		is not already in answer
 		 */
 		while (temp != null){
 			answer = temp.content + answer;
 			temp = temp.previous;
 		}
 		return answer;
 	}
 
 	/**
 	 * @pre: /
 	 * @post: a new Cell is created. The char is B (blank) by default
 	 */
 	private class Cell {
 		char content = B;
 		Cell next = null;
 		Cell previous = null;
 		// int checkedFor = -1; // not used
 		
 		public Cell() {
 		}
 	}
 
 	/**
 	 * @pre: s is an ASCII char.
 	 * @post: throws an exception if the char is invalid (not between 32 and 126)
 	 */
 	private void testChar(char s) throws Exception {
 		if ((int) s > 126 || (int) s < 32) {
 			System.err.println ("Invalid char: '" + s + "'");
 			throw new Exception();
 		}
 	}
 }

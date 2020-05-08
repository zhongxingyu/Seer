 public class InputStream {
 	private String input;
 	private int pointer = 0; // index of character peekToken will see
 	
 	/* Constructors */
 	public InputStream(String input) {
 		this.input = input;
 	}
 	
 	/* Setter/Getters */
 	public String getInput() {
 		return input;
 	}
 
 	public void setInput(String input) {
 		this.input = input;
 	}
 
 	public int getPointer() {
 		return pointer;
 	}
 
 	public void setPointer(int pointer) {
 		this.pointer = pointer;
 	}
 	
 	/* Navigation */
 	// Get character at current pointer without advancing
 	Character peekToken() {
 		if (isConsumed()) return null;
 		return input.charAt(pointer);
 	}
 	
 	// Consume character at current pointer
 	// Returns if we matched
	public boolean matchToken(Character token) {
 		// Make sure the token matches
 		if (!token.equals(peekToken())) return false;
 		
 		// Advance pointer
 		advancePointer();
 		return true;
 	}
 	
 	// Move pointer forward
	public int advancePointer() {
 		if (isConsumed()) return -1;
 		return pointer++;
 	}
 	
	public void resetPointer() {
 		pointer = 0;
 	}
 	
 	/* Utility */
 	// Have we already matched all the tokens in the string?
	public boolean isConsumed() {
 		return pointer == input.length();
 	}
 }

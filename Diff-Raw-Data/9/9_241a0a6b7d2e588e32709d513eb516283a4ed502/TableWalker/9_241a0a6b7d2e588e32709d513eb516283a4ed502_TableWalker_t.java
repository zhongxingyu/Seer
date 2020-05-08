 package Source;
 
 import java.util.ArrayList;
 
 /**
  * TableWalker is initialized with a DFATable.
  * calling walkTable with an input char will cause TableWalker to update its state based on it's table
  *  and return a token auto-magically at the correct time.
  * @author Sam
  *
  */
 public class TableWalker {
 	private DFATable dfa;
 	private StringBuffer lastKnownValidToken;
 	private StringBuffer currentToken = new StringBuffer();
 	private State startState = new State();//I need a way to get this from the dfa table.
 	private State currentState = startState;
 	private ArrayList<Token> returnList = new ArrayList<Token>();
 
 	public TableWalker(DFATable dfaTable) {
 		this.dfa = dfaTable;
 	}
 
 	public DFATable getDfa() {
 		return dfa;
 	}
 	
 	/**
 	 * walkTable will take in a character c, check table position and return a token when state is sanguine
 	 * @return
 	 */
 	public ArrayList<Token> walkTable(char c) {
 		returnList = null;
 		if(currentState.isFinal){
 				lastKnownValidToken = new StringBuffer(currentToken);
 		}
 		currentToken.append(c);
 		currentState = dfa.get(new StateCharacter(currentState,c));
 		
 		if(currentState == null){
 			int numCharactersUndealtWith = currentToken.length() - lastKnownValidToken.length();
 			StringBuffer newCurrentToken = new StringBuffer();
 			for(int i=0;i<numCharactersUndealtWith;i++){
 				newCurrentToken.append(currentToken.length()-numCharactersUndealtWith+i);
 			}
 			returnList.add(new Token(null,lastKnownValidToken));//How do we know what type the token is?
 			reEvaluate(newCurrentToken);
 			return returnList;
 		}
 		else{
 			return null;
		}
 	}
 
 	private void reEvaluate(StringBuffer newCurrentToken){
 		currentState = startState;
 		lastKnownValidToken = new StringBuffer();
 		currentToken = newCurrentToken;
 		for(int i=0;i<currentToken.length();i++){
 			currentState = dfa.get(new StateCharacter(currentState,currentToken.charAt(i)));
 			if(currentState == null){
 				int numCharactersUndealtWith = currentToken.length() - lastKnownValidToken.length();
 				newCurrentToken = new StringBuffer();
 				for(int j=0;j<numCharactersUndealtWith;j++){
 					newCurrentToken.append(currentToken.length()-numCharactersUndealtWith+j);
 				}
 				returnList.add(new Token(null,lastKnownValidToken));
 				reEvaluate(newCurrentToken);
 			}
 			else{
 				lastKnownValidToken.append(currentToken.charAt(i));
 			}
 		}
 	}
 }

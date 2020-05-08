 import java.lang.*;
 import java.util.*;
 
 public class Node {
 	private String concept = "";
 	private String description = "";
 	private int step = 0;
 	private String stepName = "";
 	private String dependencies = "";
 	private String questionRef = ""; // name within DISC
 	private String actionType = "";
 	private String action = "";
 	private String answerType = "";
 	private String answerOptions = "";
 	private static final String TAB = "     ";
 	private static final int ITAB = (int)TAB.charAt(0);
 
 		/* N.B. need this obtuse way of checking for adjacent TABs from tokenizer - no other way seems to work */
 
 	public Node(int step, String tsv) {
 		String token;
 		boolean lastWasTab = true; // so that if first was tab, will recognize that a token is missing
 		int count = 0;
 		try {
 			StringTokenizer st = new StringTokenizer(tsv, "\t", false);
 			this.step = step;
 			count = st.countTokens();
 			int j = 0;
 			
 			/* If there are no adjacent tabs in a line, then this fails to work.  It is not clear why.
 				Hack for now: no longer return tabs.  Since navigation.txt no longer has adjacent tabs, this
 				is working */
 			for (int i = 0; i < count; ++i) {
 				token = st.nextToken();
 				// this is the only way to tell whether the token returned is a TAB - TAB.equals(token) doesn't work!
 				if ((int)token.charAt(0) == ITAB) {
 					if (lastWasTab || i == (count - 1)) {
 						token = null; // ensures that adjacent tabs, or conceptless nodes get set correctly
 					}
 					else {
 						lastWasTab = true;
 						continue;
 					}
 				}
 				else {
 					lastWasTab = false;
 				}
 				if (token == null)
 					token = "";
 				switch (++j) {
 					case 1:
 						concept = token;
 						break;
 					case 2:
 						description = token;
 						break;
 					case 3:
 						stepName = "_" + token;
						break; // assumes, for now, that input is nubmer without underscore
 					case 4:
 						dependencies = token;
 						break;
 					case 5:
 						questionRef = token;
 						break;
 					case 6:
 						actionType = token;
 						break;
 					case 7:
 						action = token;
 						break;
 					case 8: {
 							answerOptions = token;
 							int index = answerOptions.indexOf(";");
 							if (index != -1) {
 								answerType = answerOptions.substring(0, index);
 							}
						}
 				}
 			}
 			if (j != 8) {
 				System.out.println("Error tokenizing line " + step + " (" + j + "/8 tokens found)");
 			}
 		}
 		catch(Exception e) {
 			System.out.println("Error tokenizing line " + step + " (" + count + "/8 tokens)" + e.getMessage());
 		}
 	}
 	public String getAction() { return action; }
 	public String getActionType() { return actionType; }
 	public String getAnswerOptions() { return answerOptions; }
 	public String getAnswerType() { return answerType; }
 	public String getConcept() { return concept; }
 	public String getDependencies() { return dependencies; }
 	public String getDescription() { return description; }
 	public String getName() { return stepName; }
 	public String getQuestionRef() { return questionRef; }
 	public int getStep() { return step; }
 	
 	/**
 	 * Prints out the components of a node in the schedule.
 	 */
 	public String toString() {
 		return "Node (" + step + "): <B>" + stepName + "</B><BR>\n" + "Concept: <B>" + concept + "</B><BR>\n" +
 			"Description: <B>" + description + "</B><BR>\n" + "Dependencies: <B>" + dependencies + "</B><BR>\n" +
 			"Question Reference: <B>" + questionRef + "</B><BR>\n" + "Action Type: <B>" + actionType + "</B><BR>\n" +
 			"Action: <B>" + action + "</B><BR>\n" + "AnswerType: <B>" + answerType + "</B><BR>\n" + "AnswerOptions: <B>" +
 			answerOptions + "</B><BR>\n";
 	}
 }

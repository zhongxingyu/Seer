// $ANTLR 3.5 /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g 2013-07-02 16:45:42
 
 import org.antlr.runtime.*;
 import java.util.Stack;
 import java.util.List;
 import java.util.ArrayList;
 
 @SuppressWarnings("all")
 public class LispParser extends Parser {
 	public static final String[] tokenNames = new String[] {
 		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ASSN", "EOL", "ID", "LPAR", "MINUS", 
 		"NUM", "PLUS", "PUT", "QUOTIENT", "RPAR", "SETQ", "SPACE", "TIMES"
 	};
 	public static final int EOF=-1;
 	public static final int ASSN=4;
 	public static final int EOL=5;
 	public static final int ID=6;
 	public static final int LPAR=7;
 	public static final int MINUS=8;
 	public static final int NUM=9;
 	public static final int PLUS=10;
 	public static final int PUT=11;
 	public static final int QUOTIENT=12;
 	public static final int RPAR=13;
 	public static final int SETQ=14;
 	public static final int SPACE=15;
 	public static final int TIMES=16;
 
 	// delegates
 	public Parser[] getDelegates() {
 		return new Parser[] {};
 	}
 
 	// delegators
 
 
 	public LispParser(TokenStream input) {
 		this(input, new RecognizerSharedState());
 	}
 	public LispParser(TokenStream input, RecognizerSharedState state) {
 		super(input, state);
 	}
 
 	@Override public String[] getTokenNames() { return LispParser.tokenNames; }
 	@Override public String getGrammarFileName() { return "/Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g"; }
 
 
 	        
 		private int[] store = new int[26]; // implement hashmap
 		// ... storage for variables 'a', ..., 'z'
 
 
 
 	// $ANTLR start "prog"
 	// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:11:1: prog : ( com )* EOF ;
 	public final void prog() throws RecognitionException {
 		try {
 			// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:12:2: ( ( com )* EOF )
 			// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:12:4: ( com )* EOF
 			{
 			// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:12:4: ( com )*
 			loop1:
 			while (true) {
 				int alt1=2;
 				int LA1_0 = input.LA(1);
 				if ( (LA1_0==LPAR) ) {
 					alt1=1;
 				}
 
 				switch (alt1) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:12:4: com
 					{
 					pushFollow(FOLLOW_com_in_prog19);
 					com();
 					state._fsp--;
 
 					}
 					break;
 
 				default :
 					break loop1;
 				}
 			}
 
 			match(input,EOF,FOLLOW_EOF_in_prog22); 
 			}
 
 		}
 		catch (RecognitionException re) {
 			reportError(re);
 			recover(input,re);
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "prog"
 
 
 
 	// $ANTLR start "com"
 	// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:17:1: com : ( '(' PUT v= expr ')' EOL | '(' SETQ ID v= expr ')' EOL );
 	public final void com() throws RecognitionException {
 		Token ID1=null;
 		int v =0;
 
 		try {
 			// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:18:2: ( '(' PUT v= expr ')' EOL | '(' SETQ ID v= expr ')' EOL )
 			int alt2=2;
 			int LA2_0 = input.LA(1);
 			if ( (LA2_0==LPAR) ) {
 				int LA2_1 = input.LA(2);
 				if ( (LA2_1==PUT) ) {
 					alt2=1;
 				}
 				else if ( (LA2_1==SETQ) ) {
 					alt2=2;
 				}
 
 				else {
 					int nvaeMark = input.mark();
 					try {
 						input.consume();
 						NoViableAltException nvae =
 							new NoViableAltException("", 2, 1, input);
 						throw nvae;
 					} finally {
 						input.rewind(nvaeMark);
 					}
 				}
 
 			}
 
 			else {
 				NoViableAltException nvae =
 					new NoViableAltException("", 2, 0, input);
 				throw nvae;
 			}
 
 			switch (alt2) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:18:4: '(' PUT v= expr ')' EOL
 					{
 					match(input,LPAR,FOLLOW_LPAR_in_com38); 
 					match(input,PUT,FOLLOW_PUT_in_com40); 
 					pushFollow(FOLLOW_expr_in_com44);
 					v=expr();
 					state._fsp--;
 
 					match(input,RPAR,FOLLOW_RPAR_in_com46); 
 					match(input,EOL,FOLLOW_EOL_in_com48); 
					 LispIntRun.output.println(v); 
 					}
 					break;
 				case 2 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:19:4: '(' SETQ ID v= expr ')' EOL
 					{
 					match(input,LPAR,FOLLOW_LPAR_in_com61); 
 					match(input,SETQ,FOLLOW_SETQ_in_com63); 
 					ID1=(Token)match(input,ID,FOLLOW_ID_in_com65); 
 					pushFollow(FOLLOW_expr_in_com69);
 					v=expr();
 					state._fsp--;
 
 					match(input,RPAR,FOLLOW_RPAR_in_com71); 
 					match(input,EOL,FOLLOW_EOL_in_com73); 
 					 int a =
 							                         (ID1!=null?ID1.getText():null).charAt(0) - 'a'; 
 							                       store[a] = v; 
 					}
 					break;
 
 			}
 		}
 		catch (RecognitionException re) {
 			reportError(re);
 			recover(input,re);
 		}
 		finally {
 			// do for sure before leaving
 		}
 	}
 	// $ANTLR end "com"
 
 
 
 	// $ANTLR start "expr"
 	// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:26:1: expr returns [int value] : (v1= term | ( '(' PLUS v1= expr v2= expr ')' | '(' MINUS v1= expr v2= expr ')' | '(' TIMES v1= expr v2= expr ')' | '(' QUOTIENT v1= expr v2= expr ')' ) );
 	public final int expr() throws RecognitionException {
 		int value = 0;
 
 
 		int v1 =0;
 		int v2 =0;
 
 		try {
 			// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:27:2: (v1= term | ( '(' PLUS v1= expr v2= expr ')' | '(' MINUS v1= expr v2= expr ')' | '(' TIMES v1= expr v2= expr ')' | '(' QUOTIENT v1= expr v2= expr ')' ) )
 			int alt4=2;
 			int LA4_0 = input.LA(1);
 			if ( (LA4_0==ID||LA4_0==NUM) ) {
 				alt4=1;
 			}
 			else if ( (LA4_0==LPAR) ) {
 				int LA4_2 = input.LA(2);
 				if ( (LA4_2==MINUS||LA4_2==PLUS||LA4_2==QUOTIENT||LA4_2==TIMES) ) {
 					alt4=2;
 				}
 				else if ( ((LA4_2 >= ID && LA4_2 <= LPAR)||LA4_2==NUM) ) {
 					alt4=1;
 				}
 
 				else {
 					int nvaeMark = input.mark();
 					try {
 						input.consume();
 						NoViableAltException nvae =
 							new NoViableAltException("", 4, 2, input);
 						throw nvae;
 					} finally {
 						input.rewind(nvaeMark);
 					}
 				}
 
 			}
 
 			else {
 				NoViableAltException nvae =
 					new NoViableAltException("", 4, 0, input);
 				throw nvae;
 			}
 
 			switch (alt4) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:27:4: v1= term
 					{
 					pushFollow(FOLLOW_term_in_expr122);
 					v1=term();
 					state._fsp--;
 
 					 value = v1; 
 					}
 					break;
 				case 2 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:28:4: ( '(' PLUS v1= expr v2= expr ')' | '(' MINUS v1= expr v2= expr ')' | '(' TIMES v1= expr v2= expr ')' | '(' QUOTIENT v1= expr v2= expr ')' )
 					{
 					// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:28:4: ( '(' PLUS v1= expr v2= expr ')' | '(' MINUS v1= expr v2= expr ')' | '(' TIMES v1= expr v2= expr ')' | '(' QUOTIENT v1= expr v2= expr ')' )
 					int alt3=4;
 					int LA3_0 = input.LA(1);
 					if ( (LA3_0==LPAR) ) {
 						switch ( input.LA(2) ) {
 						case PLUS:
 							{
 							alt3=1;
 							}
 							break;
 						case MINUS:
 							{
 							alt3=2;
 							}
 							break;
 						case TIMES:
 							{
 							alt3=3;
 							}
 							break;
 						case QUOTIENT:
 							{
 							alt3=4;
 							}
 							break;
 						default:
 							int nvaeMark = input.mark();
 							try {
 								input.consume();
 								NoViableAltException nvae =
 									new NoViableAltException("", 3, 1, input);
 								throw nvae;
 							} finally {
 								input.rewind(nvaeMark);
 							}
 						}
 					}
 
 					else {
 						NoViableAltException nvae =
 							new NoViableAltException("", 3, 0, input);
 						throw nvae;
 					}
 
 					switch (alt3) {
 						case 1 :
 							// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:28:5: '(' PLUS v1= expr v2= expr ')'
 							{
 							match(input,LPAR,FOLLOW_LPAR_in_expr143); 
 							match(input,PLUS,FOLLOW_PLUS_in_expr161); 
 							pushFollow(FOLLOW_expr_in_expr165);
 							v1=expr();
 							state._fsp--;
 
 							pushFollow(FOLLOW_expr_in_expr169);
 							v2=expr();
 							state._fsp--;
 
 							match(input,RPAR,FOLLOW_RPAR_in_expr171); 
 							value = v1 + v2;
 							}
 							break;
 						case 2 :
 							// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:30:19: '(' MINUS v1= expr v2= expr ')'
 							{
 							match(input,LPAR,FOLLOW_LPAR_in_expr193); 
 							match(input,MINUS,FOLLOW_MINUS_in_expr195); 
 							pushFollow(FOLLOW_expr_in_expr199);
 							v1=expr();
 							state._fsp--;
 
 							pushFollow(FOLLOW_expr_in_expr203);
 							v2=expr();
 							state._fsp--;
 
 							match(input,RPAR,FOLLOW_RPAR_in_expr205); 
 							value = v1 - v2;
 							}
 							break;
 						case 3 :
 							// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:31:19: '(' TIMES v1= expr v2= expr ')'
 							{
 							match(input,LPAR,FOLLOW_LPAR_in_expr228); 
 							match(input,TIMES,FOLLOW_TIMES_in_expr230); 
 							pushFollow(FOLLOW_expr_in_expr234);
 							v1=expr();
 							state._fsp--;
 
 							pushFollow(FOLLOW_expr_in_expr238);
 							v2=expr();
 							state._fsp--;
 
 							match(input,RPAR,FOLLOW_RPAR_in_expr240); 
 							value = v1 * v2;
 							}
 							break;
 						case 4 :
 							// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:32:19: '(' QUOTIENT v1= expr v2= expr ')'
 							{
 							match(input,LPAR,FOLLOW_LPAR_in_expr262); 
 							match(input,QUOTIENT,FOLLOW_QUOTIENT_in_expr264); 
 							pushFollow(FOLLOW_expr_in_expr268);
 							v1=expr();
 							state._fsp--;
 
 							pushFollow(FOLLOW_expr_in_expr272);
 							v2=expr();
 							state._fsp--;
 
 							match(input,RPAR,FOLLOW_RPAR_in_expr274); 
							value = v1 / v2;
 							}
 							break;
 
 					}
 
 					}
 					break;
 
 			}
 		}
 		catch (RecognitionException re) {
 			reportError(re);
 			recover(input,re);
 		}
 		finally {
 			// do for sure before leaving
 		}
 		return value;
 	}
 	// $ANTLR end "expr"
 
 
 
 	// $ANTLR start "term"
 	// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:36:1: term returns [int value] : ( NUM | ID | LPAR v= expr RPAR );
 	public final int term() throws RecognitionException {
 		int value = 0;
 
 
 		Token NUM2=null;
 		Token ID3=null;
 		int v =0;
 
 		try {
 			// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:37:2: ( NUM | ID | LPAR v= expr RPAR )
 			int alt5=3;
 			switch ( input.LA(1) ) {
 			case NUM:
 				{
 				alt5=1;
 				}
 				break;
 			case ID:
 				{
 				alt5=2;
 				}
 				break;
 			case LPAR:
 				{
 				alt5=3;
 				}
 				break;
 			default:
 				NoViableAltException nvae =
 					new NoViableAltException("", 5, 0, input);
 				throw nvae;
 			}
 			switch (alt5) {
 				case 1 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:37:4: NUM
 					{
 					NUM2=(Token)match(input,NUM,FOLLOW_NUM_in_term331); 
 					 value = Integer.parseInt(
 							                         (NUM2!=null?NUM2.getText():null)); 
 					}
 					break;
 				case 2 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:39:4: ID
 					{
 					ID3=(Token)match(input,ID,FOLLOW_ID_in_term355); 
 					 int a =
 							                         (ID3!=null?ID3.getText():null).charAt(0) - 'a'; 
 							                       value = store[a]; 
 					}
 					break;
 				case 3 :
 					// /Users/jorgejaso/NetBeansProjects/LispInt/src/Lisp.g:42:4: LPAR v= expr RPAR
 					{
 					match(input,LPAR,FOLLOW_LPAR_in_term380); 
 					pushFollow(FOLLOW_expr_in_term384);
 					v=expr();
 					state._fsp--;
 
 					match(input,RPAR,FOLLOW_RPAR_in_term386); 
 					 value = v; 
 					}
 					break;
 
 			}
 		}
 		catch (RecognitionException re) {
 			reportError(re);
 			recover(input,re);
 		}
 		finally {
 			// do for sure before leaving
 		}
 		return value;
 	}
 	// $ANTLR end "term"
 
 	// Delegated rules
 
 
 
 	public static final BitSet FOLLOW_com_in_prog19 = new BitSet(new long[]{0x0000000000000080L});
 	public static final BitSet FOLLOW_EOF_in_prog22 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_com38 = new BitSet(new long[]{0x0000000000000800L});
 	public static final BitSet FOLLOW_PUT_in_com40 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_com44 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_com46 = new BitSet(new long[]{0x0000000000000020L});
 	public static final BitSet FOLLOW_EOL_in_com48 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_com61 = new BitSet(new long[]{0x0000000000004000L});
 	public static final BitSet FOLLOW_SETQ_in_com63 = new BitSet(new long[]{0x0000000000000040L});
 	public static final BitSet FOLLOW_ID_in_com65 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_com69 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_com71 = new BitSet(new long[]{0x0000000000000020L});
 	public static final BitSet FOLLOW_EOL_in_com73 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_term_in_expr122 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_expr143 = new BitSet(new long[]{0x0000000000000400L});
 	public static final BitSet FOLLOW_PLUS_in_expr161 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_expr165 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_expr169 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_expr171 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_expr193 = new BitSet(new long[]{0x0000000000000100L});
 	public static final BitSet FOLLOW_MINUS_in_expr195 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_expr199 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_expr203 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_expr205 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_expr228 = new BitSet(new long[]{0x0000000000010000L});
 	public static final BitSet FOLLOW_TIMES_in_expr230 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_expr234 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_expr238 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_expr240 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_expr262 = new BitSet(new long[]{0x0000000000001000L});
 	public static final BitSet FOLLOW_QUOTIENT_in_expr264 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_expr268 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_expr272 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_expr274 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_NUM_in_term331 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_ID_in_term355 = new BitSet(new long[]{0x0000000000000002L});
 	public static final BitSet FOLLOW_LPAR_in_term380 = new BitSet(new long[]{0x00000000000002C0L});
 	public static final BitSet FOLLOW_expr_in_term384 = new BitSet(new long[]{0x0000000000002000L});
 	public static final BitSet FOLLOW_RPAR_in_term386 = new BitSet(new long[]{0x0000000000000002L});
 }

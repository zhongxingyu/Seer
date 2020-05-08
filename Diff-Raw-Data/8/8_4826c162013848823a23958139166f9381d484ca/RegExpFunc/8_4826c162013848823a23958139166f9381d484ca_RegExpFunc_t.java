 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Arrays;
 
 /**
  *
  */
 
 /**
  * @author Rochelle
  *
  */
 public class RegExpFunc {
     private static final String SPACE = " ", BSLASH = "/", MULTI = "*",
     PLUS = "+", OR = "|", LBRAC = "[", RBRAC = "]", LPAREN = "(",
     RPAREN = ")", PERIOD = ".", APOS = "'", QUOT = "\"", UNION = "UNION",
     IN = "IN", NOT = "^", DASH = "-", DOLLAR = "$";
 
     private static final Character ESCAPE = new Character('\\');
 
     private static final Character[] EX_RE_CHAR = {
         ' ', '\\', '*', '+', '?', '|', '[', ']', '(', ')', '.', '\'', '"', '$'
     };
     private static final Character[] EX_CLS_CHAR = {
         '\\', '^', '-', '[', ']', '$'
     };
 
     private static final HashSet<Character> RE_CHAR = new HashSet<Character>(Arrays.asList(EX_RE_CHAR));
     private static final HashSet<Character> CLS_CHAR = new HashSet<Character>(Arrays.asList(EX_CLS_CHAR));
 
     private ArrayList<Terminals> classes = Parser.getClasses();
 
     private String input;
     private InputStream is;
 
     /* Constructor */
     public RegExpFunc(String input) {
         this.input = new String(input);
         this.is = new InputStream(this.input);
         //origRegExp();
     }
 
     private boolean matchToken(String token) {
         //return is.matchToken(token.charAt(0));
         return is.matchToken(token);
     }
 
     private String peekToken() {
         if (is.peekToken() == null) return null;
         String s = String.valueOf(is.peekToken());
         System.out.println("Current regex char: " + s);
         return s;
     }
 
     private boolean peekToken(String token) {
         //return peekToken().equals(token);
         return is.peekToken(token);
     }
 
     private boolean peekEscaped(HashSet<Character> escaped) {
         debug();
         if (is.isConsumed()) return false;
 
         // Escaped character?
         if (is.peekToken(ESCAPE)) {
             return escaped.contains(is.peekToken(1));
         } else {
             return !escaped.contains(is.peekToken());
         }
     }
 
     private Character matchEscaped(HashSet<Character> escaped) {
         if (is.isConsumed()) return null;
 
         // Escaped character?
         Character token;
         if (is.peekToken(ESCAPE)) {
             token = is.peekToken(1);
             is.advancePointer(2);
         } else {
             token = is.peekToken();
             is.advancePointer();
         }
 
         return token;
     }
 
     private boolean peekReToken() {
         return peekEscaped(RE_CHAR);
     }
 
     private boolean peekClsToken() {
         return peekEscaped(CLS_CHAR);
     }
 
     private Character matchReToken() {
         return matchEscaped(CLS_CHAR);
     }
 
     private Character matchClsToken() {
         return matchEscaped(CLS_CHAR);
     }
 
     private void invalid() {
         System.out.println("Invalid Syntax");
         System.exit(0);
     }
 
     private void debug() {
         System.out.println("Pointer at: "+peekToken());
         System.out.println(Thread.currentThread().getStackTrace()[2]);
     }
     
     private void definedClass() {
         debug();
 
         for(int i=0; i<classes.size(); i++){
             String name = classes.get(i).getName();
             if (is.peekToken(name)) {
                 is.matchToken(name);
             }
         }
     }
     
     public void addToHashSet(String className, Character c){
         for(int i=0; i<classes.size(); i++){
             if(classes.get(i).getName().equals(className))
                 classes.get(i).addChar(c);
         }
     }
     
     public void completeHashSet(String className, Character c){
         HashSet<Character> classHash;
         for(int i=0; i<classes.size(); i++){
             if(classes.get(i).getName().equals(className)){
                 classHash = classes.get(i).getChars();
                 Iterator<Character> iter = classHash.iterator();
                while(iter.hasNext()){
	                Character start = iter.next();
	                for(int j=0; j<c; j++){
	                    classHash.add((char)(start+1));
	                }
                 }
                 classHash.add(c);
             }
         }
         
     }
 
     public NFA origRegExp(String className) {
         debug();
         return regExp(className);
     }
 
     public NFA regExp(String className) {
         debug();
         NFA nfa = regExOne(className);
         if(peekToken(OR)){
             nfa = NFA.union(nfa, regExPrime(className));
         }
         return nfa;
     }
 
     public NFA regExPrime(String className) {
         debug();
         if(peekToken(OR)) {
             matchToken(OR);
             NFA nfa = regExOne(className);
             nfa = NFA.concat(regExPrime(className), nfa);
             return nfa;
         }
         else {
             State s = new State();
             NFA nfa = new NFA(s);
             nfa.setAccepts(s, true);
             return nfa;
         }
     }
 
     public NFA regExOne(String className) {
         debug();
         NFA nfa = regExTwo(className);
         nfa = NFA.concat(nfa, regExOnePrime(className));
         return nfa;
     }
 
     public NFA regExOnePrime(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         if(peekToken(LPAREN) || peekToken(PERIOD) || peekToken(LBRAC) ||
                 peekToken(DOLLAR) || peekReToken()) {
             nfa.addEpsilonTransition(s, regExTwo(className).getStart());
             nfa = NFA.concat(nfa, regExOnePrime(className));
             return nfa;
         }
         else {
             nfa.setAccepts(s, true);
             return nfa;
         }
     }
 
     public NFA regExTwo(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         //***********************************Double check the if for minor bug issues***********************************
         if(peekToken(LPAREN)) {
             matchToken(LPAREN);
             if(peekToken(RPAREN)) {
                 matchToken(RPAREN);
                 nfa.addTransition(s, ')', regExTwoTail(className).getStart());
             }
             else {
                 invalid();
                 return null;
             }
             State t = new State();
             NFA nfaNew = new NFA(t);
             NFA rexpNFA = regExp(className);
             NFA concat = NFA.concat(rexpNFA, nfa);
             nfaNew.addTransition(t, '(', concat.getStart());
             return nfaNew;
         }
         else if(peekReToken()) {
             char reChar = matchReToken(); 
             addToHashSet(className, reChar);
             nfa.addTransition(s, reChar, regExTwoTail(className).getStart());
             return nfa;
         }
         else {
             if(peekToken(PERIOD) || peekToken(LBRAC) || peekToken(DOLLAR)){
                 nfa.addEpsilonTransition(s, regExThree(className).getStart());
                 return nfa;
             }
             else {
                 invalid();
                 return null;
             }
         }
     }
 
     public NFA regExTwoTail(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         if(peekToken(MULTI)) {
             matchToken(MULTI);
             State a = new State();
             nfa.addTransition(s, '*', a);
             nfa.setAccepts(a, true);
             return nfa;
         }
         else if(peekToken(PLUS)) {
             matchToken(PLUS);
             State a = new State();
             nfa.addTransition(s, '+', a);
             nfa.setAccepts(a, true);
             return nfa;
         }
         else {
             nfa.setAccepts(s, true);
             return nfa;
         }
     }
 
     public NFA regExThree(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         if(peekToken(PERIOD) || peekToken(LBRAC) || peekToken(DOLLAR)) {
             nfa.addEpsilonTransition(s, charClass(className).getStart());
             return nfa;
         }
         else if(peekToken("null")) {
             nfa.setAccepts(s, true);
             return nfa;
         }
         else {
             invalid();
             return null;
         }
     }
 
     public NFA charClass(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         if(peekToken(PERIOD)) {
             matchToken(PERIOD);
             State a = new State();
             nfa.addTransition(s, '.', a);
             nfa.setAccepts(a, true);
             return nfa;
         }
         else if(peekToken(LBRAC)) {
             matchToken(LBRAC);
             nfa.addTransition(s, '[', charClassOne(className).getStart());
             return nfa;
         }
         //******************************Not sure what to do for transition********************************************
         else if(peekToken(DOLLAR)){
             matchToken(DOLLAR);
             State a = new State();
             nfa.addTransition(s, '$', a);
             System.out.println("I'm in char class!!!");
             definedClass();
             return null;
         }
         else{
             invalid();
             return null;
         }
     }
 
     public NFA charClassOne(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         if(peekClsToken() || peekToken(RBRAC)) {
             nfa.addEpsilonTransition(s,charSetList(className).getStart());
             return nfa;
         }
         else if(peekToken(NOT)) {
             nfa.addEpsilonTransition(s, excludeSet(className).getStart());
             return nfa;
         }
         else {
             invalid();
             return null;
         }
     }
 
     public NFA charSetList(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         if(peekClsToken()) {
             nfa.addEpsilonTransition(s, charSet(className).getStart());
             nfa = NFA.concat(nfa, charSetList(className));
             return nfa;
         }
         else if(peekToken(RBRAC)) {
             matchToken(RBRAC);
             State a = new State();
             nfa.addTransition(s, ']', a);
             nfa.setAccepts(a, true);
             return nfa;
         }
         else{
             invalid();
             return null;
         }
     }
 
     public NFA charSet(String className) {
         debug();
         System.out.println(peekToken());
         System.out.println(is.getPointer());
         State s = new State();
         NFA nfa = new NFA(s);
         if(peekClsToken()) {
             nfa.addTransition(s, matchClsToken(),charSetTail(className).getStart());
             return nfa;
         }
         else {
             invalid();
             return null;
         }
     }
 
     public NFA charSetTail(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         if(peekToken(DASH)) {
             System.out.println("We're there!");
             matchToken(DASH);
             State t = new State();
             nfa.addTransition(s, '-', t);
             if(peekClsToken()) {
                 char c = matchClsToken();
                 nfa.addTransition(t, c, new State());
                 completeHashSet(className, c);
                 return nfa;
             }
             else {
                 invalid();
                 return null;
             }
         }
         else{
             nfa.setAccepts(s, true);
             return nfa;
         }
     }
     /** --------------------------------------------------------------------------------------------------*/
     //Special case with IN exclusion so make sure to pay detailed attention to this part
     public NFA excludeSet(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         //***********************************Double check the if for minor bug issues***********************************
 
         //Assumption made that there is an " " then "IN" then another " "
         if(peekToken(NOT)) {
             matchToken(NOT);
             if(peekToken(RBRAC)) {
                 System.out.println("We're at the ]!");
                 matchToken(RBRAC);
 //                if(peekToken(SPACE)){
 //                    matchToken(SPACE);
                     if(peekToken(IN)) {
                         matchToken(IN);
 //                        if(peekToken(SPACE)){
 //                            matchToken(SPACE);
 //                            nfa.addTransition(s, ' ', excludeSetTail().getStart());
 //                        }
 //                        else{
 //                            invalid();
 //                            return null;
 //                        }
                         if(peekToken(DOLLAR)){
                             matchToken(DOLLAR);
                         }
                         nfa.addEpsilonTransition(s, excludeSetTail(className).getStart());
                     }
                     else{
                         invalid();
                         return null;
                     }
 //                    State t = new State();
 //                    nfa.addTransition(t, ' ', nfa.getStart());
 //                    nfa.setStart(t);
 //                }
 //                else{
 //                    invalid();
 //                    return null;
 //                }
                 State u = new State();
                 nfa.addTransition(u, ']', nfa.getStart());
                 nfa.setStart(u);
             }
             else {
                 invalid();
                 return null;
             }
             State v = new State();
             NFA charSetNFA = charSet(className);
             NFA nfaNew = new NFA(v);
             charSetNFA = NFA.concat(charSetNFA, nfa);
             nfaNew.addTransition(v, '^', charSetNFA.getStart());
             return nfaNew;
         }
         else {
             invalid();
             return null;
         }
     }
 
     public NFA excludeSetTail(String className) {
         debug();
         State s = new State();
         NFA nfa = new NFA(s);
         if(peekToken(LBRAC)) {
             matchToken(LBRAC);
             if(peekToken(RBRAC)) {
                 matchToken(RBRAC);
                 State t = new State();
                 nfa.addTransition(s, ']', t);
                 nfa.setAccepts(t, true);
             }
             else {
                 invalid();
                 return null;
             }
             State u = new State();
             NFA charSetNFA = charSet(className);
             charSetNFA = NFA.concat(charSetNFA, nfa);
             NFA nfaNew = new NFA(u);
             nfaNew.addTransition(u, '[', charSetNFA.getStart());
             return nfaNew;
         }
         //***********************************Not sure what to do for transition***********************************
         else{
             System.out.println("I'm here!!!");
             matchToken(DOLLAR);
             definedClass();
             return null;
         }
     }
 }

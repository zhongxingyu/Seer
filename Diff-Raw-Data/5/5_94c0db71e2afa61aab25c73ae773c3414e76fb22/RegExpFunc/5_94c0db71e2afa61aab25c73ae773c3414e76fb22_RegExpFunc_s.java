 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class RegExpFunc {
     @SuppressWarnings("unused")
     private static final String SPACE = " ", BSLASH = "/", STAR = "*",
     PLUS = "+", OR = "|", LBRAC = "[", RBRAC = "]", LPAREN = "(",
     RPAREN = ")", PERIOD = ".", APOS = "'", QUOT = "\"",
     IN = "IN", NOT = "^", DASH = "-", DOLLAR = "$";
 
     private static final Character ESCAPE = new Character('\\');
 
     private static final Character[] EX_RE_CHAR = { ' ', '\\', '*', '+', '?',
         '|', '[', ']', '(', ')', '.', '\'', '"', '$' };
     private static final Character[] EX_CLS_CHAR = { '\\', '^', '-', '[', ']',
     '$' };
 
     private static final Set<Character> RE_CHAR = new HashSet<Character>(
             Arrays.asList(EX_RE_CHAR));
     private static final Set<Character> CLS_CHAR = new HashSet<Character>(
             Arrays.asList(EX_CLS_CHAR));
     private Parser parser;
     private List<TokenClass> classes;
 
     private String input;
     private InputStream is;
     private char lastCharAdded;
 
     /* Constructor */
     public RegExpFunc(String input, Parser parser) {
         this.input = new String(input);
         this.is = new InputStream(this.input);
         this.parser = parser;
         this.classes = parser.getClasses();
     }
 
     private boolean matchToken(String token) {
         return matchToken(token, true);
     }
 
     private boolean matchToken(String token, boolean skipWhitespace) {
         boolean matched = is.matchToken(token);
         if (matched && skipWhitespace) {
             is.skipWhitespace();
         }
 
         return matched;
     }
 
     //    private String peekToken() {
     //        if (is.peekToken() == null)
     //            return null;
     //        String s = String.valueOf(is.peekToken());
     //        return s;
     //    }
 
     private boolean peekToken(String token) {
         return is.peekToken(token);
     }
 
     private boolean peekEscaped(Set<Character> escaped) {
         debug();
         if (is.isConsumed())
             return false;
 
         // Escaped character?
         if (is.peekToken(ESCAPE)) {
             return escaped.contains(is.peekToken(1));
         } else {
             // Is it in the ASCII printable?
             return !escaped.contains(is.peekToken()) && Helpers.isPrintable(is.peekToken());
         }
     }
 
     private Character matchEscaped(Set<Character> escaped) {
         if (is.isConsumed())
             return null;
 
         // Escaped character?
         Character token;
         if (is.peekToken(ESCAPE)) {
             token = is.peekToken(1);
             is.advancePointer(2);
         } else {
             token = is.peekToken();
             is.advancePointer();
         }
         is.skipWhitespace();
 
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
         if (Parser.debug) {
             System.out.println(is);
             System.out.println(Thread.currentThread().getStackTrace()[2]);
         }
     }
 
     private String definedClass() {
         debug();
 
         for (int i = 0; i < classes.size(); i++) {
             String name = classes.get(i).getName();
             if (is.peekToken(name)) {
                 is.matchToken(name);
                 is.skipWhitespace();
                 return name;
             }
         }
         return null;
     }
 
     public void addToHashSet(String className, Character c) {
         for (int i = 0; i < classes.size(); i++) {
             if (classes.get(i).getName().equals(className)){
                 classes.get(i).addChar(c);
                 lastCharAdded = c;
             }
         }
     }
 
     public Set<Character> completeHashSet(String className, Character c) {
         Set<Character> classHash;
         for (int i = 0; i < classes.size(); i++) {
             if (classes.get(i).getName().equals(className)) {
                 classHash = classes.get(i).getChars();
 
                 char a = lastCharAdded;
                 char b = c;
 
                 // Swap them if they're other way around
                 // Throw error for now.
                 // int diff = b - a;
                 if(b - a <= 0){
                     invalid();
                     char swap = a;
                     a = b;
                     b = swap;
                 }
 
                 for (int j = a; j <= b; j++) {
                     classHash.add((char) j);
                 }
                 return classHash;
             }
         }
         return null;
     }
 
     public Set<Character> exclude(Set<Character> set1, Set<Character> set2){
         Iterator<Character> iter = set1.iterator();
         @SuppressWarnings("unchecked")
         Set<Character> newHashSet = (HashSet<Character>) ((HashSet<Character>)set2).clone();
         while(iter.hasNext()){
             Character c = iter.next();
             if(newHashSet.contains(c))
                 newHashSet.remove(c);
         }
         return newHashSet;
     }
 
     public static NFA createNFA(Set<Character> set){
         State s = new State();
         State t = new State();
         NFA nfa = new NFA(s);
         nfa.setAccepts(t, true);
         Iterator<Character> iter = set.iterator();
         while (iter.hasNext()) {
             nfa.addTransition(s, iter.next(), t);
         }
         return nfa;
     }
 
     private NFA getNFA(String className){
         Set<Character> chars = parser.getClass(className);
         if (chars != null)
             return createNFA(chars);
         else
             return null;
     }
 
     public NFA origRegExp(String className) {
         debug();
         return regExp(className);
     }
 
     public NFA regExp(String className) {
         debug();
         NFA nfa = regExOne(className);
         if (peekToken(OR)) {
             nfa = NFA.union(nfa, regExPrime(className));
         }
         return nfa;
     }
 
     public NFA regExPrime(String className) {
         debug();
         if (peekToken(OR)) {
             matchToken(OR);
             NFA nfa = regExOne(className);
             nfa = NFA.concat(regExPrime(className), nfa);
             return nfa;
         } else {
             State a = new State();
             a.setAccepts(true);
             return new NFA(a);
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
         if (peekToken(LPAREN) || peekToken(PERIOD) || peekToken(LBRAC)
                 || peekToken(DOLLAR) || peekReToken()) {
             NFA nfa = regExTwo(className);
             nfa = NFA.concat(nfa, regExOnePrime(className));
             return nfa;
        } else if(peekToken("")) {
            return null;
        }
        else {
             State a = new State();
             a.setAccepts(true);
             return new NFA(a);
         }
     }
 
     public NFA regExTwo(String className) {
         debug();
         NFA nfa = null;
         if (peekToken(LPAREN)) {
             matchToken(LPAREN);
             nfa = regExp(className);
             if (peekToken(RPAREN)) {
                 matchToken(RPAREN);
                 return regExTwoTail(className, nfa);
             } else {
                 invalid();
                 return null;
             }
         }
         else if (peekReToken()) {
             char reChar = matchReToken();
             addToHashSet(className, reChar);
             nfa = regExTwoTail(className, nfa);
             if(nfa == null){
                 nfa = createNFA(parser.getClass(className));
             }
             return nfa;
         }
         else {
             if (peekToken(PERIOD) || peekToken(LBRAC) || peekToken(DOLLAR)
                     || peekToken(RPAREN) || peekToken(OR)) {
                 nfa = regExThree(className);
                 return nfa;
             } else{
                 invalid();
                 return null;
             }
         }
     }
 
     public NFA regExTwoTail(String className, NFA nfa) {
         debug();
         if (peekToken(STAR)) {
             matchToken(STAR);
             return NFA.star(nfa);
         } else if (peekToken(PLUS)) {
             matchToken(PLUS);
             return NFA.plus(nfa);
         } else {
             if(nfa != null){
                 Set<State> accepts = nfa.getAcceptingStates();
                 State a = new State();
                 a.setAccepts(true);
                 for(State item : accepts) {
                     nfa.addEpsilonTransition(item, a);
                 }
             }
             return nfa;
         }
     }
 
     public NFA regExThree(String className) {
         debug();
         NFA nfa = null;
         if (peekToken(PERIOD) || peekToken(LBRAC) || peekToken(DOLLAR)) {
             nfa = charClass(className);
             return nfa;
         } else {
             // Assume it's epsilon
             State a = new State();
             a.setAccepts(true);
             return new NFA(a);
         }
     }
 
     public NFA charClass(String className) {
         debug();
         NFA nfa = null;
         /** needs major fixing----------------------------------------------------------------------------*/
         if (peekToken(PERIOD)) {
             matchToken(PERIOD);
             State s = new State();
             State a = new State();
             nfa = new NFA(s);
             nfa.setAccepts(a, true);
             for(char c=Helpers.PRINTSTART; c < Helpers.PRINTEND; c++){
                 parser.getClass(className).add(c);
             }
             return createNFA(parser.getClass(className));
             /**-----------------------------------------------------------------------------------------------*/
         } else if (peekToken(LBRAC)) {
             matchToken(LBRAC, false);
             nfa = charClassOne(className);
             return nfa;
         }
         else if (peekToken(DOLLAR)) {
             matchToken(DOLLAR);
             String name = definedClass();
             return getNFA(name);
         } else {
             invalid();
             return null;
         }
     }
 
     public NFA charClassOne(String className) {
         debug();
         if (peekClsToken() || peekToken(RBRAC)) {
             NFA nfa = charSetList(className);
             return nfa;
         } else if (peekToken(NOT)) {
             NFA nfa = excludeSet(className);
             return nfa;
         } else {
             invalid();
             return null;
         }
     }
 
     public NFA charSetList(String className) {
         debug();
         if (peekClsToken()) {
             charSet(className);
             charSetList(className);
             return createNFA(parser.getClass(className));
         } else if (peekToken(RBRAC)) {
             matchToken(RBRAC);
             return null;
         } else {
             invalid();
             return null;
         }
     }
 
     public void charSet(String className) {
         debug();
         if (peekClsToken()) {
             Character hashChar = matchClsToken();
             addToHashSet(className, hashChar);
             charSetTail(className);
         } else {
             invalid();
         }
     }
 
     public void charSetTail(String className) {
         debug();
         if (peekToken(DASH)) {
             matchToken(DASH);
             if (peekClsToken()) {
                 char c = matchClsToken();
                 completeHashSet(className, c);
             } else {
                 invalid();
             }
         } else {
             return;
         }
     }
 
     /**
      * ------------------------------------------------------------------------
      * --------------------------
      */
     // Special case with IN exclusion so make sure to pay detailed attention to
     // this part
     public NFA excludeSet(String className) {
         debug();
         NFA nfa = null;
         // Assumption made that there is an " " then "IN" then another " "
         if (peekToken(NOT)) {
             matchToken(NOT);
             charSet(className);
             if (peekToken(RBRAC)) {
                 matchToken(RBRAC);
                 if (peekToken(IN)) {
                     matchToken(IN);
                     if (peekToken(DOLLAR)) {
                         excludeSetTail(className);
                         nfa = createNFA(parser.getClass(className));
                     }
                     return nfa;
                 } else {
                     invalid();
                     return null;
                 }
             }
             else {
                 invalid();
                 return null;
             }
         }
         else {
             invalid();
             return null;
         }
     }
 
     public void excludeSetTail(String className) {
         debug();
         if (peekToken(LBRAC)) {
             matchToken(LBRAC);
             charSet(className);
             if (peekToken(RBRAC)) {
                 matchToken(RBRAC);
             } else {
                 invalid();
             }
 
         }
         // ***********************************Not sure what to do for
         // transition***********************************
         else {
             matchToken(DOLLAR);
             String name = definedClass();
             Set<Character> hashSet1 = parser.getClass(className);
             Set<Character> hashSet2 = parser.getClass(name);
             parser.setClass(className, exclude(hashSet1, hashSet2));
         }
     }
 }

 package sg.rdp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import sg.Util;
 import sg.fa.DFA;
 import sg.fa.NFA;
 import sg.fa.Transition;
 import sg.tests.NFATest;
 
 public class RecursiveDescentParserNFA
 {
     // Flag used in rexp() and rexpPrime()
     private static boolean epsilon = false;
     // Flag to turn on debug printing
     private static int debug = 0;
     // Flag to print out the results of everything
     private static int returnValueDebug = 0;
     // Counter used for naming NFA states
     private static Integer counter;
     // List used as a queue to store the inputted regex to be validated/creating
     // the NFA for
     private static ArrayList<Character> list;
     // Keeps track of the index for the list so it knows where the head is
     // pointed to
     private static int index;
     // Hashset used for all the characters that need escaped RE_CHARS
     private static HashSet<Character> RESpecials;
     private static Character[] RE_Special =
     { ' ', '\\', '*', '+', '?', '|', '[', ']', '(', ')', '.', '\'', '\"', '$' };
     // 2) escaped characters: \ (backslash space), \\, \*, \+, \?, \|, \[, \],
     // \(, \), \., \' and \"
     // Hashset used for all the characters that need CLS_CHAR
     private static HashSet<Character> CLSSpecials;
     private static Character[] CLS_Special =
     { '\\', '^', '-', '[', ']' };
 
     // private static String[] charClassTestCases = {"[0-9]"};//,
     // "[^0]IN$DIGIT", "[a-zA-Z]", "[^a-z]IN$CHAR", "[^A-Z]IN$CHAR"};
     private static String[] charClassTestCases =
     { "[a-zA-Z]" };
     private static HashMap<String, NFA> definedClasses;
 
     private static int rangeCount;
 
     /**
      * Main function to call that creates an NFA from the regex
      * @param regex Regex string to match
      * @param definedClass Mapping of defined classes and their NFA
      * @return NFA representing the inputted regex string
      */
     public static NFA validateRegex(String regex,
             HashMap<String, NFA> definedClass)
     {
         debugPrint("In validateRegex()");
         init(regex, definedClass);
         return reGex();
     }
     
     
     /**
      * Another function to create an NFA that includes debug flags
      * @param regex Regex string ot match
      * @param definedClass Mapping of deifned classes and their NFA
      * @param debugFlag Turns on what function was called + other debug statements
      * @param returnFlag Turns on the value that each function returns
      * @return NFA representing the inputted regex string
      */
     public static NFA validateRegex(String regex,
             HashMap<String, NFA> definedClass, int debugFlag, int returnFlag)
     {
         debug = debugFlag;
         returnValueDebug = returnFlag;
         debugPrint("In validateRegex()");
         init(regex, definedClass);
         NFA result = reGex();
         debug = 0;
         returnValueDebug = 0;
         return result;
     }
 
     /**
      * Inits some of the structures needed for doing the RDP
      * @param definedClass Mapping of defined classes and their NFA
      * @return NFA representing the inputted regex string
      */
     private static void init(String regex, HashMap<String, NFA> definedClass)
     {
         debugPrint("In init()");
         list = new ArrayList<Character>();
         counter = new Integer(0);
         index = 0;
         initList(regex);
         RESpecials = new HashSet<Character>();
         for (Character character : RE_Special)
         {
             RESpecials.add(character);
         }
         CLSSpecials = new HashSet<Character>();
         for (Character character : CLS_Special)
         {
             CLSSpecials.add(character);
         }
 
         definedClasses = new HashMap<String, NFA>();
         definedClasses.putAll(definedClass);
 
         rangeCount = 0;
 
     }
 
     // <reg-ex> -> <rexp>
     
     /**
      * Beginning of RDP
      * Represents: <reg-ex> -> <rexp>
      * @return NFA representing this line of the grammar
      */
     private static NFA reGex()
     {
         debugPrint("In reGex");
         return rexp();
     }
 
     // <rexp> -> <rexp1> <rexp’>
     // This checks the epsilon flag to see whether or not we need to concatenate
     // or union the nfas
     /**
      * Represents <rexp> -> <rexp1> <rexp’>
      * @return NFA representing this line of the grammar
      */
     private static NFA rexp()
     {
         String name = "rexp()";
         debugPrint("In rexp()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         NFA rexp1 = rexp1();
 
         if (rexp1 == null)
         {
             resultsPrint(name, "Null");
             return null;
         }
 
         NFA rexpPrime = rexpPrime();
         if (rexpPrime == null)
         {
             resultsPrint(name, "Null");
             return null;
         }
         resultsPrint(name, "Union of rexp1 and rexp`");
         if (epsilon == true)
         {
             epsilon = false;
             return NFA.concatenate(rexp1, rexpPrime);
         }
         return NFA.union(rexp1, rexpPrime);
     }
 
     // <rexp’> -> UNION <rexp1> <rexp’> | epsilon
     // There is an epsilon flag set to indicate whether this rexp' returns an
     // epsilon nfa vs an nfa
     // that has other transitions.
     
     /**
      * Represents <rexp’> -> UNION <rexp1> <rexp’> | epsilon
      * @return NFA representing this line of the grammar
      */
     private static NFA rexpPrime()
     {
         epsilon = true;
         String name = "rexp`()";
         debugPrint("In rexp`()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         if (top() == '|')
         {
             consume();
 
             if (top() == null)
             {
                 resultsPrint(name, "Epsilon NFA");
                 return createEpsilonNFA();
             }
             NFA rexp1 = rexp1();
             if (rexp1 == null)
             {
                 resultsPrint(name, "Epsilon NFA");
                 return createEpsilonNFA();
             }
             NFA rexpPrime = rexpPrime();
             if (rexpPrime == null)
             {
                 resultsPrint(name, "Epsilon NFA");
                 return createEpsilonNFA();
             }
             resultsPrint(name, "Concatenate rexp1 and rexp`");
             // Can always return true;
             epsilon = false;
             return NFA.concatenate(rexp1, rexpPrime);
         }
         epsilon = true;
         return createEpsilonNFA();
     }
 
     // <rexp1> -> <rexp2> <rexp1’>
     /**
      * Represents  <rexp1> -> <rexp2> <rexp1’>
      * @return NFA representing this line of the grammar
      */
     private static NFA rexp1()
     {
         String name = "rexp1()";
         debugPrint("In rexp1()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         NFA rexp2 = rexp2();
         if (rexp2 == null)
         {
             resultsPrint(name, "NULL");
             return null;
         }
         NFA rexp1Prime = rexp1Prime();
         if (rexp1Prime == null)
         {
             resultsPrint(name, "NULL");
             return null;
         }
 
         resultsPrint(name, "Concatenate rexp2 and rexp1`");
         return NFA.concatenate(rexp2, rexp1Prime);
     }
 
     // <rexp1’> -> <rexp2> <rexp1’> | epsilon
     /**
      * Represents <rexp1’> -> <rexp2> <rexp1’> | epsilon
      * @return NFA representing this line of the grammar
      */
     private static NFA rexp1Prime()
     {
         String name = "rexp1`()";
         debugPrint("In rexp1Prime()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         NFA rexp2 = rexp2();
         if (rexp2 == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
 
         NFA rexp1Prime = rexp1Prime();
         if (rexp1Prime == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
 
         resultsPrint(name, "Conatenate rexp2 and rexp1`");
         return NFA.concatenate(rexp2, rexp1Prime);
     }
 
     // <rexp2> -> (<rexp>) <rexp2-tail> | RE_CHAR <rexp2-tail> | <rexp3>
     /**
      * Represents <rexp2> -> (<rexp>) <rexp2-tail> | RE_CHAR <rexp2-tail> | <rexp3>
      * @return NFA representing this line of the grammar
      */
     private static NFA rexp2()
     {
         String name = "rexp2()";
         debugPrint("In rexp2()");
         if (top() == null)
         {
             resultsPrint(name, "null");
             // return createEpsilonNFA();
             return null;
         }
 
         // System.out.println("PART1");
         // Part 1
         // (<rexp>) <rexp2-tail>
         if (top() == '(')
         {
             consume();
 
             NFA rexp = rexp();
             if (top() == null)
             {
                 resultsPrint(name, "null");
                 return null;
             }
             if (rexp != null && top() == ')')
             {
                 consume();
                 Character rexp2Tail = rexp2Tail();
                 if (rexp2Tail != null)
                 {
 
                     // NFA result = NFA.concatenate(leftParens, rexp);
                     // result = NFA.concatenate(result, rightParens);
                     // resultsPrint("Concatenate of rexp and rexp2-tail");
                     if (rexp2Tail == '*')
                     {
                         resultsPrint(name, "rexp*");
                         return NFA.star(rexp);
                     }
                     else
                     {
                         resultsPrint(name, "rexp");
                         return rexp;
                     }
                     // return NFA.star(rexp);
                 }
             }
         }
 
         // System.out.println("PART2");
         // Part 2
         // RE_CHAR <rexp2-tail>
         NFA RE_CHAR = RE_CHAR();
         Character rexp2Tail = null;
         if (RE_CHAR != null)
             rexp2Tail = rexp2Tail();
         if (rexp2Tail != null)
         {
             // resultsPrint("RE_CHAR and rexp2Tail: "+rexp2Tail);
 
             // return NFA.concatenate(RE_CHAR, rexp2Tail);
             // System.out.println("rexp2Tail is: "+rexp2Tail);
             if (rexp2Tail == '*')
             {
                 resultsPrint(name, "RE_CHAR*");
                 return NFA.star(RE_CHAR);
             }
             else
             {
                 resultsPrint(name, "RE_CHAR");
                 return RE_CHAR;
             }
         }
 
         // System.out.println("PART3");
 
         // Part3
         // <rexp3>
         return rexp3();
     }
 
     // <rexp2-tail> -> * | + | epsilon
     /**
      * Represents <rexp2-tail> -> * | + | epsilon
      * @return NFA representing this line of the grammar
      */
     private static Character rexp2Tail()
     {
         String name = "rexp2Tail()";
         debugPrint("In rexp2Tail()");
         if (top() == null)
         {
             resultsPrint(name, "EPSILON");
             return Transition.EPSILON;
         }
         Character result = Transition.EPSILON;
         ;
         if (top() == '*')
         {
             consume();
             result = '*';
             resultsPrint(name, "*");
         }
         else if (top() == '+')
         {
             consume();
             result = '+';
             resultsPrint(name, "+");
         }
 
         return result;
     }
 
     // <rexp3> -> <char-class> | epsilon
     /**
      * Represents <rexp3> -> <char-class> | epsilon
      * @return NFA representing this line of the grammar
      */
     private static NFA rexp3()
     {
         String name = "rexp3()";
         debugPrint("In rexp3()");
         if (top() == null)
         {
             resultsPrint(name, "null");
             // return createEpsilonNFA();
             return null;
         }
         NFA charClass = charClass();
         if (charClass != null)
         {
             resultsPrint(name, "charClass");
             return charClass;
         }
         resultsPrint(name, "null");
         // return createEpsilonNFA();
         return null;
     }
 
     // <char-class> -> . | [ <char-class1> | <defined-class>
     /**
      * Return <char-class> -> . | [ <char-class1> | <defined-class>
      * @return NFA representing this line of the grammar
      */
     private static NFA charClass()
     {
         String name = "charClass()";
         debugPrint("In charClass()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         // Part1
         // .
         if (top() == '.')
         {
             consume();
             resultsPrint(name, ".");
             return createLiteralNFA('.');
         }
 
         // Part2
         // [ <char-class1>
         if (top() == '[')
         {
             consume();
             // NFA leftBracket = createLiteralNFA('[');
             NFA charClass1 = charClass1();
             if (charClass1 != null)
             {
                 // return NFA.concatenate(leftBracket, charClass1);
                 resultsPrint(name, "CharClass1");
                 return charClass1;
             }
         }
 
         // Part3
         // <defined-class>
         resultsPrint(name, "DefinedClass");
         return definedClass();
     }
 
     // <char-class1> -> <char-set-list> | <exclude-set>
     /**
      * Represents <char-class1> -> <char-set-list> | <exclude-set>
      * @return NFA representing this line of the grammar
      */
     private static NFA charClass1()
     {
         String name = "charClass1()";
         debugPrint("In charClass1()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         // Part 1
         // <char-set-list>
         NFA charSetList = charSetList();
 
         if (charSetList != null)
         {
             resultsPrint(name, "charSetLIst");
             // HashSet<Character> charSetListChar =
             // NFA.oneLayerTransitions(charSetList);
             /*
              * System.out.println("****Printing charSetList");
              * Util.reallyPrettyPrint(charSetListChar);
              */
             return charSetList;
         }
 
         resultsPrint(name, "ExcludeSet");
         // Part2
         // <exclude-set>
         return excludeSet();
     }
 
     // <char-set-list> -> <char-set> <char-set-list> | ]
     /**
      * Represents <char-set-list> -> <char-set> <char-set-list> | ]
      * @return NFA representing this line of the grammar
      */
     private static NFA charSetList()
     {
         String name = "charSetList()";
         debugPrint("In charSetList()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         NFA charSet = charSet();
         if (charSet != null)
         {
             NFA charSetList = charSetList();
             if (charSetList != null)
             {
                 resultsPrint(name, "Union charSet and charSetList");
                 return NFA.union(charSet, charSetList);
             }
         }
 
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
 
         if (top() == ']')
         {
             consume();
             // return createLiteralNFA(']');
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
 
         resultsPrint(name, "NULL");
         return null;
     }
 
     // <char-set> -> CLS_CHAR <char-set-tail>
     /**
      * Represents <char-set> -> CLS_CHAR <char-set-tail>
      * @return NFA representing this line of the grammar
      */
     private static NFA charSet()
     {
         String name = "charSet()";
         debugPrint("In charSet()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         Character CLS_CHAR = CLS_CHAR();
         if (CLS_CHAR == null)
         {
             resultsPrint(name, "NULL");
             return null;
         }
         Character charSetTail = charSetTail();
         // System.out.println("charsettail is "+charSetTail);
         if (charSetTail != null)
         {
             // return NFA.concatenate(CLS_CHAR, charSetTail);
             if ((int) charSetTail != (int) Transition.EPSILON)
             {
                 resultsPrint(name,
                         "Created RangedNFA from CLS_CHAR and charSetTail");
                 // System.out.println("Value of CLS_CHAR is: "+CLS_CHAR);
                 // System.out.println("Value of charSetTail is: "+charSetTail);
                 
                if((int)CLS_CHAR == (int) charSetTail){
                     return createLiteralNFA(CLS_CHAR);
                 }
                 else{
                     return NFA.makeRangedNFA(CLS_CHAR, charSetTail);
                 }
             }
             else
             {
                 resultsPrint(name, "Made a charNFA from CLS_CHAR");
                 return NFA.makeCharNFA(CLS_CHAR);
             }
         }
         resultsPrint(name, "NULL");
         return null;
     }
 
     // <char-set-tail> -> –CLS_CHAR | epsilon
     /**
      * Represents <char-set-tail> -> –CLS_CHAR | epsilon
      * @return NFA representing this line of the grammar
      */
     private static Character charSetTail()
     {
         String name = "charSetTail()";
         debugPrint("In charSetTail()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon");
             return Transition.EPSILON;
         }
         if (top() == '-')
         {
             consume();
             // NFA dash = createLiteralNFA('-');
 
             Character CLS_CHAR = CLS_CHAR();
             // System.out.println("CLS_CHar is "+CLS_CHAR);
             if (CLS_CHAR != null)
             {
                 // return NFA.concatenate(dash, CLS_CHAR);
                 resultsPrint(name, "CLS_CHAR: " + CLS_CHAR);
                 return CLS_CHAR;
             }
 
         }
         resultsPrint(name, "Epsilon");
         return Transition.EPSILON;
     }
 
     // <exclude-set> -> ^<char-set>] IN <exclude-set-tail>
     /**
      * Representing <exclude-set> -> ^<char-set>] IN <exclude-set-tail>
      * @return NFA representing this line of the grammar
      */
     private static NFA excludeSet()
     {
         String name = "excludeSet()";
         debugPrint("In excludeSet()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         if (top() == '^')
         {
             consume();
             NFA charSet = charSet();
             if (top() == null)
             {
                 resultsPrint(name, "Epsilon NFA");
                 return createEpsilonNFA();
             }
             if (charSet != null && top() == ']')
             {
                 consume();
                 if (top() == 'I')
                 {
                     consume();
                     if (top() == 'N')
                     {
                         consume();
                         NFA excludeSetTail = excludeSetTail();
 
                         if (excludeSetTail != null)
                         {
                             HashSet<Character> charSetChars = NFA
                                     .oneLayerTransitions(charSet);
                             // System.out.println("****Printing charSetChars");
                             // Util.reallyPrettyPrint(charSetChars);
                             HashSet<Character> excludeSetTailChars = NFA
                                     .oneLayerTransitions(excludeSetTail);
                             // System.out.println("\n");
                             // System.out.println("****Printing excludeSetTailChars");
                             // Util.reallyPrettyPrint(excludeSetTailChars);
                             HashSet<Character> disjointSet = NFA.disjointSet(
                                     excludeSetTailChars, charSetChars);
                             // System.out.println("****Printing disjointSet");
                             // Util.reallyPrettyPrint(disjointSet);
                             NFA result = NFA.makeRangedNFA(disjointSet);
                             resultsPrint(name, "Disjoint set stuff");
                             return result;
                         }
                     }
                 }
             }
         }
         resultsPrint(name, "NULL");
         return null;
     }
 
     // <exclude-set-tail> -> [<char-set>] | <defined-class>
     /**
      * <exclude-set-tail> -> [<char-set>] | <defined-class>
      * @return NFA representing this line of the grammar
      */
     private static NFA excludeSetTail()
     {
         String name = "excludeSetTail()";
         debugPrint("In excludeSetTail()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         if (top() == '[')
         {
             consume();
             // NFA leftBracket = createLiteralNFA('[');
             NFA charSet = charSet();
             if (top() == null)
             {
                 resultsPrint(name, "Epsilon NFA");
                 return createEpsilonNFA();
             }
             if (charSet != null && top() == ']')
             {
                 consume();
                 // NFA rightBracket = createLiteralNFA(']');
 
                 // NFA result = NFA.concatenate(leftBracket, charSet);
                 // result = NFA.concatenate(result, rightBracket);
                 resultsPrint(name, "charSet");
                 return charSet;
             }
         }
 
         resultsPrint(name, "DefinedClass");
         return definedClass();
     }
     
     
     /**
      * Determines if a character belongs to CLS_CHAR
      * @return The character that was in CLS_CHAR
      */
     private static Character CLS_CHAR()
     {
         String name = "CLS_CHAR";
         debugPrint("In CLS_CHAR()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon");
             // return createEpsilonNFA();
             return Transition.EPSILON;
         }
 
         // Checks to see if the next character is escaped
         boolean escaped = checkEscaped();
         debugPrint("escaped value is " + escaped);
         if (escaped)
         {
             NFA backslash = createLiteralNFA('\\');
             boolean specialChar = false;
             // If there's a '\\', check to see if the escaped character is one
             // that's
             // accepted by CLS_CHAR
             specialChar = CLSSpecialChars();
             if (specialChar)
             {
                 NFA special = createLiteralNFA(top());
                 Character value = consume();
                 // return NFA.concatenate(backslash, special);
                 resultsPrint(name, "Escaped CLS_CHAR: " + value);
                 return value;
             }
         }
 
         if (top() == null)
         {
             resultsPrint(name, "Epsilon");
             return Transition.EPSILON;
         }
         debugPrint("Value of CLSSpecialChars is " + CLSSpecialChars());
 
         // Check to see if the next character SHOULD be escaped but it's not. If
         // so, it fails
         if (CLSSpecialChars())
         {
             resultsPrint(name, "NULL");
             return null;
         }
 
         // NFA literal = createLiteralNFA(top());
         Character value = consume();
         resultsPrint(name, "CLS_CHAR: " + value);
         return value;
     }
     
     /**
      * Determines if the next character is in RE_CHAR
      * @return NFA of the character
      */
     private static NFA RE_CHAR()
     {
         String name = "RE_CHAR";
         debugPrint("In RE_CHAR()");
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         // Checks to see if the next character is escaped
         boolean escaped = checkEscaped();
         debugPrint("escaped value is " + escaped);
         if (escaped)
         {
             // NFA backslash = createLiteralNFA('\\');
             boolean specialChar = false;
             // If there's a '\\', check to see if the escaped character is one
             // that's
             // accepted by RE_CHAR
             specialChar = RESpecialChars();
             if (specialChar)
             {
                 NFA special = createLiteralNFA(top());
                 consume();
                 // return NFA.concatenate(backslash, special);
                 resultsPrint(name, "Special escaped character: " + special);
                 return special;
             }
         }
         debugPrint("Value of RESpecialChars is " + RESpecialChars());
 
         // Check to see if the next character SHOULD be escaped but it's not. If
         // so, it fails
         if (RESpecialChars())
         {
             resultsPrint(name, "NULL");
             return null;
         }
 
         NFA literal = createLiteralNFA(top());
         consume();
         resultsPrint(name, "Returned literal");
         return literal;
     }
     
     /**
      * Determines if the next few characters are a part of a defined class
      * @return the NFA of the definedClass
      */
     private static NFA definedClass()
     {
         String name = "definedClass()";
         if (top() == null)
         {
             resultsPrint(name, "Epsilon NFA");
             return createEpsilonNFA();
         }
         debugPrint("In definedClass()");
         for (String defined : definedClasses.keySet())
         {
             boolean found = true;
             for (int index = 0; index < defined.length(); index++)
             {
                 if (get(index).equals(defined.charAt(index)) == false)
                 {
                     found = false;
                     break;
                 }
             }
             if (found)
             {
                 resultsPrint(name, "DefinedClass: " + defined);
                 NFA definedNFA = definedClasses.get(defined);
                 resultsPrint(name, "DefinedClass's nfa is not null is: "
                         + (definedNFA != null));
                 for (int index = 0; index < defined.length(); index++)
                 {
                     consume();
                 }
 
                 HashSet<Character> copySet = NFA
                         .oneLayerTransitions(definedNFA);
                 return NFA.makeRangedNFA(copySet);
 
             }
         }
         resultsPrint(name, "null");
         return null;
     }
 
     // Checks to see if the next character is the escape character '\\'
     // If so, it consumes it and returns true
     // If not, it does nothing
     
     /**
      * Determines if the next character is escaped
      * @return Whether the next character is escaped
      */
     private static boolean checkEscaped()
     {
         String name = "checkEscaped()";
         debugPrint("In checkEscaped()");
         if (top() == null)
         {
             resultsPrint(name, "false");
             return false;
         }
         if (top() == '\\')
         {
             consume();
             resultsPrint(name, "true");
             return true;
         }
         resultsPrint(name, "false");
         return false;
     }
 
     // Checks to see if the head of the list is a specialcharacter for RE that
     // should be escaped or not
     /**
      * Determines if the next characters is a special character that needs to be escaped in RE_CHAR
      * @return if the character needs to be secaped
      */
     private static boolean RESpecialChars()
     {
         if (top() == null)
             return false;
         debugPrint("In RESpecialChars()");
         return RESpecials.contains(top());
     }
 
     // Checks to see if the head of the list is a specialcharacter for CLS that
     // should be escaped or not
     /**
      * Determines if the next character is a special character that needs to be escaped in CLS_CHAR
      * @return If the next character needs to be escaped
      */ 
     private static boolean CLSSpecialChars()
     {
         if (top() == null)
             return false;
         debugPrint("In CLSSpecialChars()");
         return CLSSpecials.contains(top());
     }
 
     /*
      * Bookkeeping stuff
      */
 
     // Returns the value of at the head of the list, same as peek()
     /**
      * Returns the next character to parse
      * @return Character to parse
      */
     private static Character top()
     {
         // debugPrint("In top()");
         if (index < list.size())
             return list.get(index);
         // return new Character('☃');]
         return null;
     }
 
     // Increases the index to "head" of the array list and returns the old value
     /**
      * Advances the index to the next character
      * @return The character that has been parsed
      */
     private static Character consume()
     {
         if (index < list.size())
         {
             debugPrint("*****Consumed: " + list.get(index));
             // System.out.println("Consumed: "+list.get(index));
             index++;
             return list.get(index - 1);
         }
         return null;
     }
 
     // Gets the character at a given offset from the begining of the list
     /**
      * Returns the charater to be parsed at the given offset
      * @param offset The index of the character from the front
      * @return The character
      */
     private static Character get(int offset)
     {
         // debugPrint("In get()");
         if (index + offset <= list.size())
         {
             return list.get(index + offset);
         }
         return null;
     }
 
     // Initialize the list into the arraylist
     /**
      * Initializes the queue with the given regex
      * @param regex Regex to be parsed
      */
     private static void initList(String regex)
     {
         for (int x = 0; x < regex.length(); x++)
         {
             list.add(regex.charAt(x));
         }
     }
 
     // Used for debug printing
     /**
      * Method used for debug printing
      * @param statement Statement to print
      */
     private static void debugPrint(String statement)
     {
         if (debug == 1)
             System.out.println(statement);
     }
     
     /**
      * Method used for printing return value
      * @param function Name of the function
      * @param value Value that it's returning
      */
     private static void resultsPrint(String function, String value)
     {
         if (returnValueDebug == 1)
             System.out.println("\tReturning from " + function + " with "
                     + value);
     }
 
     // Creates an NFA with an epsilon
     /**
      * Method used to create an NFA with an epsilon transition
      * @return NFA
      */
     private static NFA createEpsilonNFA()
     {
         return NFA.makeCharNFA(Transition.EPSILON);
     }
 
     // Creates an NFA for a given literal
     /**
      * Creates an NFA with the given string literal
      * @param literal Literal to be changed into an NFA
      * @return NFA
      */
     private static NFA createLiteralNFA(char literal)
     {
         return NFA.makeCharNFA(literal);
     }
 }

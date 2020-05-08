 package de.minestar.maventest.commandsystem;
 
 import java.util.ArrayList;
 
 public class SyntaxHelper {
 
     // init vars
     private static final String KEYS_MUST_ARGS = "<>";
     private static final String KEYS_OPT_ARGS = "[]";
     private static final String KEYS_INF_ARG = "...";
 
     // ///////////////////////////////////////////////////////////////
     //
     // STATIC METHODS
     //
     // ///////////////////////////////////////////////////////////////
 
     /**
      * This method is used to validate a syntaxstring
      * 
      * @param syntax
      * @return a SyntaxValidationResult with all needed infos
      */
     public static SyntaxValidationResult isSyntaxValid(String syntax) {
         // create some vars...
         int neededArgumentCount = 0, optionalArgumentCount = 0;
         boolean neededArgumentOpen = false;
         boolean optionalArgumentClosed = false;
         char key, lastKey = ' ';
         int lastOpenIndex = -1;
 
         // iterate over every char...
         for (int index = 0; index < syntax.length(); index++) {
 
             // get the current char
             key = syntax.charAt(index);
 
             // after closing an optional argument, only other closearguments for
             // optional arguments are allowed
             if (key != ' ' && key != ']' && optionalArgumentClosed) {
                 return new SyntaxValidationResult("No statements after optional argument allowed!", index);
             }
 
             // check for: doublespaces, spaces at the beginning, spaces in
             // needed arguments
             if (key == ' ') {
                 // doublespace or space at the beginning?
                 if (lastKey == ' ') {
                     // space at the beginning
                     if (index == 0) {
                         return new SyntaxValidationResult("Spaces at the beginning are not allowed!", index);
                     }
                     // doublespace
                     else {
                         return new SyntaxValidationResult("Doublespaces are not allowed!", index - 1);
                     }
                 }
                 // spaces in needed argument
                 if (neededArgumentOpen) {
                     return new SyntaxValidationResult("Spaces in needed statements are not allowed!", index);
                 }
             }

             // is the char an opener for a needed arguments?
            if (key == KEYS_MUST_ARGS.charAt(0)) {
                 // we cannot nest needed arguments into needed arguments
                 if (neededArgumentOpen) {
                     return new SyntaxValidationResult("Needed statement is already opened!", index);
                 }
                 ++neededArgumentCount;
                 neededArgumentOpen = true;
                 lastOpenIndex = index;
             }
             // is the char an quitter for a needed arguments?
             else if (key == KEYS_MUST_ARGS.charAt(1)) {
                 --neededArgumentCount;
                 // is the argument at least 1 char long?
                 if (lastOpenIndex + 1 == index) {
                     if (neededArgumentOpen) {
                         return new SyntaxValidationResult("Needed statement is empty!", index);
                     } else {
                         return new SyntaxValidationResult("Needed statement is closed, before it is opened!", index);
                     }
                 }
                 neededArgumentOpen = false;
             }
             // is the char an opener for an optional arguments?
             else if (key == KEYS_OPT_ARGS.charAt(0)) {
                 // we cannot nest optional arguments into needed arguments
                 if (neededArgumentOpen) {
                     return new SyntaxValidationResult("Needed statement is still opened!", index);
                 }
                 ++optionalArgumentCount;
                 lastOpenIndex = index;
             }
             // is the char an quitter for an optional arguments?
             else if (key == KEYS_OPT_ARGS.charAt(1)) {
                 // needed statements must be closed first, otherwise constructs
                 // like [<] would be possible
                 if (neededArgumentOpen) {
                     return new SyntaxValidationResult("Needed statement is still opened!", index);
                 }
                 // is the argument at least 1 char long?
                 if (lastOpenIndex + 1 == index) {
                     return new SyntaxValidationResult("Optional statement is empty!", index);
                 }
                 --optionalArgumentCount;
                 optionalArgumentClosed = true;
             }
 
             // is optionalArgumentCount < 0 => return
             // with errormessage,
             // this means that there is a close-statement without an
             // opening-statement in front of it
             if (optionalArgumentCount < 0) {
                 return new SyntaxValidationResult("Optional statement is closed, before it is opened!", index);
             }
 
            lastKey = key;
             lastOpenIndex = -1;
         }
 
         // space at the end
         if (lastKey == ' ') {
             return new SyntaxValidationResult("Spaces at the end are not allowed!", syntax.length() - 1);
         }
 
         // are all statements closed correct?
         if (optionalArgumentCount != 0 || neededArgumentCount != 0) {
             return new SyntaxValidationResult("Statements are not closed properly!", Integer.MAX_VALUE);
         }
         return new SyntaxValidationResult("OK", -1);
     }
     /**
      * Remove the optional syntaxkeys from a given String
      * 
      * @param syntax
      * @return Return the syntax without the syntaxkeys.<br />
      *         <b>Example:</b><br />
      *         [<Player> [Player...]] => <Player> [Player]
      */
     public static String removeOptionalSyntaxKeys(String syntax) {
         int startIndex = -1, endIndex = -1;
         char key;
 
         // get first "["
         for (int index = 0; index < syntax.length(); index++) {
             key = syntax.charAt(index);
             if (key == KEYS_OPT_ARGS.charAt(0)) {
                 startIndex = index + 1;
                 break;
             }
         }
 
         // get last "]"
         for (int index = syntax.length() - 1; index >= 0; index++) {
             key = syntax.charAt(index);
             if (key == KEYS_OPT_ARGS.charAt(1)) {
                 endIndex = index;
                 break;
             }
         }
 
         // cut off
         if (startIndex < endIndex && startIndex != -1 && endIndex != -1) {
             String substring = syntax.substring(startIndex, endIndex);
             if (substring.startsWith(" ")) {
                 substring = substring.substring(1);
             }
             if (substring.endsWith(" ")) {
                 substring = substring.substring(0, substring.length() - 1);
             }
             return substring;
         }
 
         // return the complete syntax
         return syntax;
     }
 
     /**
      * Get all arguments for a given Syntax
      * 
      * @param syntax
      * @return All arguments for a given Syntax
      */
     public static ArrayList<String> getArguments(String syntax) {
         // create some vars...
         char key;
         String argument = "";
         ArrayList<String> arguments = new ArrayList<String>();
         boolean mustLocked = false, optLocked = false;
         int lockIndex = 0;
 
         // iterate over every key...
         for (int index = 0; index < syntax.length(); index++) {
             key = syntax.charAt(index);
             argument += key;
 
             // do we have an opener for needed arguments?
             if (key == KEYS_MUST_ARGS.charAt(0) && !mustLocked && !optLocked) {
                 mustLocked = true;
                 lockIndex = 1;
                 continue;
             }
 
             // do we have an quitter for needed arguments?
             if (key == KEYS_MUST_ARGS.charAt(1) && mustLocked) {
                 --lockIndex;
                 if (lockIndex == 0) {
                     mustLocked = false;
                     arguments.add(argument);
                     argument = "";
                 }
                 continue;
             }
 
             // do we have an opener for optional arguments?
             if (key == KEYS_OPT_ARGS.charAt(0) && !mustLocked) {
                 if (optLocked) {
                     ++lockIndex;
                 } else {
                     optLocked = true;
                     lockIndex = 1;
                 }
                 continue;
             }
 
             // do we have an quitter for optional arguments?
             if (key == KEYS_OPT_ARGS.charAt(1) && optLocked) {
                 --lockIndex;
                 if (lockIndex == 0) {
                     mustLocked = false;
                     arguments.add(argument);
                     argument = "";
                 }
                 continue;
             }
 
             // do we have a keyword?
             if (key == ' ' && !optLocked && !mustLocked) {
                 argument = argument.replaceAll("  ", "");
                 argument = argument.replaceAll(" ", "");
                 if (!argument.equalsIgnoreCase(" ") && !argument.equalsIgnoreCase("")) {
                     arguments.add(argument);
                 }
                 argument = "";
                 continue;
             }
         }
 
         // add the rest of the argument to complete the list
         if (argument.length() > 0) {
             arguments.add(argument);
         }
         return arguments;
     }
 
     /**
      * Get the ArgumentType for a given argument
      * 
      * @param argument
      * @param currentIndex
      * @return the ArgumentType for the given argument
      */
     public static ArgumentType getArgumentType(String argument, int currentIndex) {
         if (argument.startsWith(String.valueOf(KEYS_MUST_ARGS.charAt(0))) && argument.endsWith(String.valueOf(KEYS_MUST_ARGS.charAt(1)))) {
             return ArgumentType.NEEDED;
         } else if (argument.startsWith(String.valueOf(KEYS_OPT_ARGS.charAt(0))) && argument.endsWith(String.valueOf(KEYS_OPT_ARGS.charAt(1)))) {
             if (argument.contains(KEYS_INF_ARG)) {
                 return ArgumentType.ENDLESS;
             } else {
                 return ArgumentType.OPTIONAL;
             }
         } else if (currentIndex >= 0) {
             if (argument.length() > 0) {
                 return ArgumentType.KEYWORD;
             }
         }
         return ArgumentType.UNKNOWN;
     }
 }

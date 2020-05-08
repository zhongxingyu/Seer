 package de.minestar.maventest.commandsystem;
 
 import java.util.ArrayList;
 
 public class SyntaxTree {
 
     // needed vars for this SyntaxTree
     private ArrayList<Argument> argumentList;
     private int minimumArguments, optionalArguments, maximumArguments;
     private boolean endless = false;
     private final String syntax;
 
     // the childtree
     private SyntaxTree childTree;
 
     /**
      * Constructor
      * 
      * @param syntax
      */
     public SyntaxTree(String syntax) {
        this(syntax, true);
    }

    /**
     * Constructor
     * 
     * @param syntax
     */
    private SyntaxTree(String syntax, boolean doValidation) {
         // set syntax
         this.syntax = syntax;
 
         // is the syntax valid?
        if (!doValidation || this.validateSyntax()) {
             // prepare the syntax for the later use in commands
             this.analyzeSyntax();
         }
     }
 
     /**
      * Validate the given syntax. This method is automatically called by the
      * constructor to validate the given syntaxstring.
      */
     private boolean validateSyntax() {
         SyntaxValidationResult result = SyntaxHelper.isSyntaxValid(this.syntax);
 
         if (result.getErrorIndex() > -1) {
             String message = "\n\nSyntax '" + syntax + "' is not valid!\n";
             if (result.getErrorIndex() == Integer.MAX_VALUE) {
                 message += result.getReason();
             } else {
                 String spaces = "";
                 for (int i = 0; i < result.getErrorIndex() + ("Syntax '").length(); i++) {
                     spaces += " ";
                 }
                 spaces += "^";
                 message += spaces;
                 message += "\nReason:\n" + result.getReason() + "\n";
             }
             throw new RuntimeException(message);
         }
         return true;
     }
 
     /**
      * Method to analyze a given Syntax. This method is automatically called on
      * the creation of a SyntaxTree.
      * 
      * @param syntax
      */
     private void analyzeSyntax() {
         // create some vars...
         this.minimumArguments = 0;
         this.argumentList = new ArrayList<Argument>();
         String singleArg;
 
         // get all Arguments with the help of the SyntaxHelper-Class
         ArrayList<String> arguments = SyntaxHelper.getArguments(this.syntax);
 
         // iterate over all arguments...
         for (int index = 0; index < arguments.size(); index++) {
             // get the current String and create an object of the Type: Argument
             singleArg = arguments.get(index);
             Argument argument = new Argument(singleArg, SyntaxHelper.getArgumentType(singleArg, index));
 
             // if the ArgumentType is unknown, we will ignore this argument
             if (argument.isUnknown()) {
                 continue;
             }
 
             // save the argument to the argumentlist of this SyntaxtTree
             this.argumentList.add(argument);
 
             // increment the minimum argumentcount, only if the ArgumentType is
             // "NEEDED" OR "KEYWORD"
             if (argument.isNeeded() || argument.isKeyword()) {
                 ++this.minimumArguments;
             }
 
             // if the ArgumentType is "OPTIONAL":
             // increment the optional argumentcount && create a child
 
             // if the ArgumentType is "ENDLESS":
             // store the information that it is endless and set the
             // optionalArguments to Integer.MAX_VALUE
             if (argument.isOptional()) {
                 ++optionalArguments;
                this.childTree = new SyntaxTree(SyntaxHelper.removeOptionalSyntaxKeys(singleArg), false);
                 break;
             } else if (argument.isEndless()) {
                 endless = true;
                 optionalArguments = Integer.MAX_VALUE;
                 break;
             }
         }
 
         // calculate the maximum argumentcount
         this.maximumArguments = this.precalculateMaxArgumentCount();
     }
 
     /**
      * Check if the syntax for a given argumentList is valid for this
      * SyntaxTree.
      * 
      * @param argumentList
      * @return <b>true</b> if the syntax is valid, otherwise <b>false</b>
      */
     public boolean checkSyntax(ArgumentList argumentList) {
         // the size of the ArgumentList must be one of the following thins:
         // >= minimumArguments && <= maximumArguments
         // OR
         // >= this.minimuArguments && this SyntaxTree must be endless
         if (argumentList.length() < this.minimumArguments || argumentList.length() > this.maximumArguments) {
             return endless && argumentList.length() >= this.minimumArguments;
         }
 
         // creeate some vars
         Argument argument;
         String currentArgument;
         int newLength = argumentList.length() - 1;
 
         // iterate over every argument of this SyntaxTree, BUT
         // break if we reach the end of the given ArgumenList
 
         // NOTE:
         // needed Arguments are not directly checked, they must only exist!
         for (int index = 0; index < this.maximumArguments && index < argumentList.length(); index++) {
             // fill the vars
             argument = this.argumentList.get(index);
             currentArgument = argumentList.getString(index);
 
             // if the argument is optional:
             // check the new length OR let the child check the syntax
             if (argument.isOptional()) {
                 if (this.childTree != null) {
                     if (newLength < childTree.minimumArguments || newLength > childTree.maximumArguments) {
                         return false;
                     } else {
                         return childTree.checkSyntax(new ArgumentList(argumentList, 1));
                     }
                 }
             } else {
                 // if the argument is a keyword, the currentArgument MUST match
                 if (argument.isKeyword()) {
                     if (!argument.getArgument().contains("|" + currentArgument.toLowerCase() + "|")) {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 
     /**
      * This method is used to precalculate the maximum argumentcount on the
      * creation of this SyntaxTree. It is automatically called.
      * 
      * @return the maximum argumentcount
      */
     private int precalculateMaxArgumentCount() {
         if (childTree != null) {
             return optionalArguments + childTree.precalculateMaxArgumentCount();
         }
         return this.minimumArguments + optionalArguments;
     }
 }

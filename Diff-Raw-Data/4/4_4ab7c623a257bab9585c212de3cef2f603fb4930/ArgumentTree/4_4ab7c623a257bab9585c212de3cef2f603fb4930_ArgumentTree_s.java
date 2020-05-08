 package de.minestar.maventest.commandsystem;
 
 import java.util.ArrayList;
 
 public class ArgumentTree {
     private static String KEYS_MUST_ARGS = "<>";
     private static String KEYS_OPT_ARGS = "[]";
     private static String KEYS_INF_ARG = "...";
 
     private int minArguments, maxArguments, countOptArgs;
     private boolean endless = false;
     private final ArgumentTree parent;
     private ArgumentTree child;
     private final String syntax;
 
     private ArrayList<ArgumentType> argList;
     private ArrayList<String> singleArgs;
 
     public ArgumentTree(String syntax) {
         this(null, syntax);
     }
 
     public ArgumentTree(ArgumentTree parent, String syntax) {
         this.parent = parent;
         this.syntax = syntax;
         this.prepareSyntax();
     }
 
     private void prepareSyntax() {
         if (this.parent == null) {
             System.out.println("------------------------------------------");
             System.out.println("ANALYZING: " + syntax);
             System.out.println("------------------------------------------");
         }
         this.minArguments = 0;
         this.maxArguments = 0;
         this.argList = new ArrayList<ArgumentType>();
         this.singleArgs = new ArrayList<String>();
         ArrayList<String> arguments = getArguments(this.syntax);
         ArgumentType argType;
         int index = 0;
         for (String singleArg : arguments) {
             argType = getArgumentType(singleArg, index);
             index++;
 
             if (argType.equals(ArgumentType.NONE)) {
                 continue;
             }
 
             argList.add(argType);
             singleArgs.add(("|" + singleArg + "|").toLowerCase());
 
             ++this.maxArguments;
             if (!argType.equals(ArgumentType.OPTIONAL) && !argType.equals(ArgumentType.ENDLESS) && !argType.equals(ArgumentType.NONE)) {
                 ++this.minArguments;
             }
             if (argType.equals(ArgumentType.OPTIONAL)) {
                 // String arg = getArgumentWithoutArg(singleArg);
                 ++countOptArgs;
                 this.child = new ArgumentTree(this, getArgumentWithoutArg(singleArg));
                 break;
             } else if (argType.equals(ArgumentType.ENDLESS)) {
                 this.maxArguments = Integer.MAX_VALUE;
                 endless = true;
                 countOptArgs = this.maxArguments;
                 break;
             }
         }
 
         System.out.println("result: " + syntax + " -> " + this.getMinArguments() + " / " + this.getOptArgumentCount());
         System.out.println("Types: " + this.argList.toString().substring(1, this.argList.toString().length() - 1));
         System.out.println("SingleArgs: " + this.singleArgs.toString().substring(1, this.singleArgs.toString().length() - 1));
         System.out.println("-----------------------------------");
     }
 
     public boolean validate(ArgumentList argumentList) {
         //////////////////////////////        
         // DEBUG        
         if (this.parent == null) {
             String txt = "";
             for (int i = 0; i < argumentList.length(); i++) {
                 txt += argumentList.getString(i) + " ";
             }
             if (txt.length() > 1) {
                 txt = txt.substring(0, txt.length() - 1);
             }
             System.out.println("validating input: '" + txt + "' ( COUNT: " + argumentList.length() + " ) ");
         }
         // DEBUG
         //////////////////////////////
 
        if (arguments.length < this.getMinArguments() || argumentList.length() > this.getOptArgumentCount()) {
             return endless && argumentList.length() >= this.getMinArguments();
         }
 
         int argCount = this.getOptArgumentCount();
 
         ArgumentType type;
         String singleArg, currentArg;
         int newLength = argumentList.length() - 1;
         for (int index = 0; index < argCount && index < argumentList.length(); index++) {
             type = this.argList.get(index);
             singleArg = this.singleArgs.get(index);
             currentArg = argumentList.getString(index)
             if (type.equals(ArgumentType.OPTIONAL)) {
                 if (this.child != null) {
                     if (newLength < child.getMinArguments() || newLength > child.getOptArgumentCount()) {
                         return false;
                     } else {
                         return child.validate(new ArgumentList(argumentList, 1));
                     }
                 }
             } else {
                 if (type.equals(ArgumentType.KEYWORD)) {
                     if (!singleArg.contains("|" + currentArg.toLowerCase() + "|")) {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
     public int getMinimalArguments() {
         return minArguments;
     }
 
     public int getMinArguments() {
         if (this.parent != null) {
             return minArguments;
         }
         return minArguments;
     }
 
     public int getOptArgumentCount() {
         if (child != null) {
             return countOptArgs + child.getOptArgumentCount();
         }
         return this.minArguments + countOptArgs;
     }
 
     private static String getArgumentWithoutArg(String syntax) {
         if (syntax.startsWith(" ")) {
             syntax = syntax.substring(1, syntax.length() - 1);
         }
         return syntax.substring(1, syntax.length() - 1);
     }
 
     public static ArrayList<String> getArguments(String syntax) {
         ArrayList<String> arguments = new ArrayList<String>();
         char key;
         String argument = "";
         boolean mustLocked = false, optLocked = false;
         int lockIndex = 0;
         for (int index = 0; index < syntax.length(); index++) {
             key = syntax.charAt(index);
             argument += key;
 
             if (key == KEYS_MUST_ARGS.charAt(0) && !mustLocked && !optLocked) {
                 mustLocked = true;
                 lockIndex = 1;
                 continue;
             }
             if (key == KEYS_MUST_ARGS.charAt(1) && mustLocked) {
                 --lockIndex;
                 if (lockIndex == 0) {
                     mustLocked = false;
                     arguments.add(argument);
                     argument = "";
                 }
                 continue;
             }
             if (key == KEYS_OPT_ARGS.charAt(0) && !mustLocked) {
                 if (optLocked) {
                     ++lockIndex;
                 } else {
                     optLocked = true;
                     lockIndex = 1;
                 }
                 continue;
             }
 
             if (key == KEYS_OPT_ARGS.charAt(1) && optLocked) {
                 --lockIndex;
                 if (lockIndex == 0) {
                     mustLocked = false;
                     arguments.add(argument);
                     argument = "";
                 }
                 continue;
             }
 
             if (key == ' ' && !optLocked && !mustLocked) {
                 argument = argument.replace(" ", "");
                 if (!argument.equalsIgnoreCase(" ") && !argument.equalsIgnoreCase("")) {
                     arguments.add(argument);
                 }
                 argument = "";
                 continue;
             }
         }
 
         if (argument.length() > 0) {
             arguments.add(argument);
         }
         return arguments;
     }
 
     private static ArgumentType getArgumentType(String text, int currentIndex) {
         if (text.startsWith(String.valueOf(KEYS_MUST_ARGS.charAt(0))) && text.endsWith(String.valueOf(KEYS_MUST_ARGS.charAt(1)))) {
             return ArgumentType.NEEDED;
         } else if (text.startsWith(String.valueOf(KEYS_OPT_ARGS.charAt(0))) && text.endsWith(String.valueOf(KEYS_OPT_ARGS.charAt(1)))) {
             if (text.contains(KEYS_INF_ARG)) {
                 return ArgumentType.ENDLESS;
             } else {
                 return ArgumentType.OPTIONAL;
             }
         } else if (currentIndex >= 0) {
             if (text.length() > 0)
                 return ArgumentType.KEYWORD;
         }
 
         return ArgumentType.NONE;
     }
 }

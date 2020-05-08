 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Steven Neisius
  */
 public class Parser {
 
     private static Scanner scn;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         if (args.length < 1) {
             System.out.println("Usage: java TestScanner <file>");
             return;
         }
 
         scn = new Scanner(args[0]);
 
         if (computation()) {
            System.out.println("Parser successful!");
         }
     }
 
     public static void Error(String errorMsg) {
         System.err.println("Parser error: " + errorMsg);
     }
 
     private static boolean computation() {
         //computation = “main” [ varDecl ] “{” statSequence “}” “.” .
         boolean rtn = true;
 
         rtn = rtn & (scn.sym == 200);// "main"
 
         scn.Next();
 
         if (scn.sym == 110) {// "var" [VarDecl]
             rtn = rtn & varDecl();
         }
 
         rtn = rtn & (scn.sym == 150);// "{"
         
         scn.Next();
         rtn = rtn & statSequence();
 
         rtn = rtn & (scn.sym == 80);// "}"
 
         scn.Next();
         rtn = rtn & (scn.sym == 30);// "."
 
         if (!rtn) {
             Error("computation");
         }
         return rtn;
     }
 
     private static boolean varDecl() {
         //varDecl = “var” ident { “,” ident } “;” .
         boolean rtn = true;
 
         rtn = rtn & (scn.sym == 110); // var
 
         scn.Next();
         rtn = rtn & (scn.sym == 61); // ident
         scn.Next();
 
         while (scn.sym == 31) {// ","
             scn.Next();
             rtn = rtn & (scn.sym == 61); // ident
 
             scn.Next();
         }
 
         rtn = rtn & (scn.sym == 70);// ";"
 
         scn.Next();
 
         if (!rtn) {
             Error("varDecl");
         }
         return rtn;
     }
 
     private static boolean statSequence() {
         //statSequence = statement { “;” statement }.
         boolean rtn = true;
         
         rtn = rtn & statement();
 
         while (scn.sym == 70) {// ";"
             scn.Next();
             rtn = rtn & statement();
         }
 
         if (!rtn) {
             Error("statSequence");
         }
         return rtn;
     }
 
     private static boolean statement() {
         //statement = assignment | funcCall | ifStatement .
         boolean rtn = true;
 
         if (scn.sym == 101) {// if
             rtn = rtn & ifStatement();
         } else if (scn.sym == 100) { // call
             rtn = rtn & funcCall();
         } else if (scn.sym == 61) { // ident
             rtn = rtn & assignment();
         } else {// empty statement invalid
             rtn = false;
         }
 
         if (!rtn) {
             Error("statement");
         }
         return rtn;
     }
 
     private static boolean ifStatement() {
         //ifStatement = “if” relation “then” statSequence [ “else” statSequence ] “fi”.
         boolean rtn = true;
 
         rtn = rtn & (scn.sym == 101); // if
 
         scn.Next();
 
         rtn = rtn & relation();
 
 
         rtn = rtn & (scn.sym == 41); // then
 
         scn.Next();
 
         rtn = rtn & statSequence();
 
 
         if (scn.sym == 90) {// else
             scn.Next();
             rtn = rtn & statSequence();
         }
 
         rtn = rtn & (scn.sym == 82);// fi
 
         scn.Next();
 
         if (!rtn) {
             Error("ifStatement");
         }
         return rtn;
     }
 
     private static boolean funcCall() {
         //funcCall = “call” ident [ “(“ [expression { “,” expression } ] “)” ].
         boolean rtn = true;
 
         rtn = rtn & (scn.sym == 100); // call
 
         scn.Next();
 
         rtn = rtn & (scn.sym == 61); // ident
 
         scn.Next();
         if (scn.sym == 50) { // "("
             scn.Next();
             if (!(scn.sym == 35)) { // ")"
                 rtn = rtn & expression();
                 scn.Next();
                 while (scn.sym == 31) {// ","
                     scn.Next();
                     rtn = rtn & expression();
                     scn.Next();
                 }
             }
 
             rtn = rtn & (scn.sym == 35); // ")"
 
             scn.Next();
         }
 
         if (!rtn) {
             Error("funcCall");
         }
         return rtn;
     }
 
     private static boolean assignment() {
         //assignment = ident “<-” expression.
         boolean rtn = true;
 
         rtn = rtn & (scn.sym == 61); // ident
 
         scn.Next();
         
         rtn = rtn & (scn.sym == 40); // "<-"
 
         scn.Next();
 
         rtn = rtn & expression(); // expression
 
 
         if (!rtn) {
             Error("assignment");
         }
         return rtn;
     }
 
     private static boolean expression() {
         //expression = term {(“+” | “-”) term}.
         boolean rtn = true;
 
         rtn = rtn & term();
 
         while (scn.sym == 11 || scn.sym == 12) { // "+" or "-"
             scn.Next();
             rtn = rtn & term();
         }
 
         if (!rtn) {
             Error("expression");
         }
         return rtn;
     }
 
     private static boolean relation() {
         //relation = expression relOp expression .
         boolean rtn = true;
 
         rtn = rtn & expression();
 
         rtn = rtn & relOp();
 
         rtn = rtn & expression();
 
         if (!rtn) {
             Error("relation");
         }
         return rtn;
     }
 
     private static boolean term() {
         //term = factor { (“*” | “/”) factor}.
         boolean rtn = true;
 
         rtn = rtn & factor();
 
         while (scn.sym == 1 || scn.sym == 2) { // "*" or "/"
             scn.Next();
             rtn = rtn & factor();
         }
 
         if (!rtn) {
             Error("term");
         }
         return rtn;
     }
 
     private static boolean relOp() {
         //relOp = “==“ | “!=“ | “<“ | “<=“ | “>“ | “>=“.
         boolean rtn = true;
         if (scn.sym < 20 || scn.sym > 25) {
             rtn = false;
         }
         scn.Next();
 
         if (!rtn) {
             Error("relOp");
         }
         return rtn;
     }
 
     private static boolean factor() {
         //factor = ident | number | “(“ expression “)” | funcCall .
         boolean rtn = true;
 
         if (scn.sym == 60) { // number
             scn.Next();
         } else if (scn.sym == 61) { // ident
             scn.Next();
         } else if (scn.sym == 50) { // "("
            scn.Next();
             rtn = rtn & expression();
             rtn = rtn & (scn.sym == 35); // ")"
             scn.Next();
         } else if (scn.sym == 100) { // call
             rtn = rtn & funcCall();
         } else {
             rtn = false;
         }
 
         if (!rtn) {
             Error("factor");
         }
 
         return rtn;
     }
 }

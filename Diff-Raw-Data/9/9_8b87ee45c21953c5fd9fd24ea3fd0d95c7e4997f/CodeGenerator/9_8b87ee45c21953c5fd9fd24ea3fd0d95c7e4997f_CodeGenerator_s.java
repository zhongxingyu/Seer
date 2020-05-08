 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tarccompiler;
 
 import datamodels.Declaration;
 import datamodels.SymbolTableModel;
 import datamodels.Token;
 import files.WriteFile;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import storage.SymbolTable;
 
 /**
  *
  * @author charles_yu102
  */
 public class CodeGenerator {
     
     // Attributes
     String javaCode;
     SymbolTable symbolTable;
     ArrayList<Token> tokens;
     ArrayList<String> lexemes;
     String output = "";
     ArrayList<Declaration> declarations;
     
     // Constructor
     public CodeGenerator(ArrayList<Token> tokens, SymbolTable symbolTbl){
         this.tokens = tokens;
         this.symbolTable = symbolTbl;
         this.lexemes = new ArrayList<String>();
         this.declarations = new ArrayList<Declaration>();
         //this.displayLexemes();
         this.displaySymbolTable();
     }
     
     // Methods
     //<editor-fold defaultstate="collapsed" desc="Debugging Methods">
     public void displayLexemes(){
         System.out.println("CODE GENERATOR ");
         for(int i=0; i < this.lexemes.size(); i++){
             System.out.println(lexemes.get(i));
         }
     }
     private void displaySymbolTable(){
         System.out.println("\n\nSymbol Table:");
         System.out.println("token \t\t tokenVal \t\t datatype \t\t scope \t\t actual value");
         for(int i=0; i<symbolTable.table.size(); i++){
             SymbolTableModel temp = symbolTable.table.get(i);
             System.out.println(temp.token+" \t\t "+temp.tokenValue+" \t\t\t "+temp.datatype+" \t\t\t "+temp.scope+"\t\t\t"+temp.actualValue);
         }
     }
     //</editor-fold>
     
     public void adjustLexemes(){
         for(int i=0; i<this.tokens.size(); i++){
             Token curToken = this.tokens.get(i);
             String curLexeme = curToken.getToken();
             // if lexeme is an int, charm string or id get token info
             if(curLexeme.equals("int") || curLexeme.equals("char") || curLexeme.equals("string") || curLexeme.equals("id")){           
                 this.lexemes.add(curToken.getTokenInfo());
             } else{ // keyword
                 this.lexemes.add(curLexeme);
             }
         }
     }
     
     @SuppressWarnings("empty-statement")
     private void getMainDeclarations(){
         // Find main
         int i;
         for(i=0; i<this.lexemes.size() && !lexemes.get(i).equals("#main") ;i++);
         i+=4;
         String startDec = this.lexemes.get(i);
         while(startDec.equals("#int") || startDec.equals("#char") || startDec.equals("#boolean")){
             this.declarations.add(new Declaration(startDec.substring(1), lexemes.get(i+1)));
             // Delete global declarations
             for(int ctr=0; ctr<3; ctr++){
                 this.lexemes.remove(i);
             }
             startDec = this.lexemes.get(i);
         }
     }
     
     private void setGlobalVars(){
         for(int i=0; i<this.declarations.size() ;i++){
             Declaration temp = declarations.get(i);
             this.javaCode += "static " + temp.getDatatype() + " " + temp.getVariable() + ";";
         }
         this.javaCode += "\n";
     }
     
     private boolean checkIfFunction(String lexeme){
         boolean functionCheck = false;
         for(int i=0; functionCheck == false && i<symbolTable.table.size(); i++){
             if(lexeme.equals(symbolTable.table.get(i).tokenValue) && symbolTable.table.get(i).datatype.equals("#func")){
                 functionCheck = true;
             }
         }
         return functionCheck;
     }
     
     @SuppressWarnings("empty-statement")
     public void translateToJava(){
         this.getMainDeclarations();
         this.javaCode = "public class TARCCode{\n";
         this.setGlobalVars();
         for(int i=0; i<this.lexemes.size(); i++){
             String curLexeme = lexemes.get(i);
             // Convert to java
             if(curLexeme.equals("|")){
                 javaCode+=" || ";
             } else if(curLexeme.equals("&")){
                 javaCode+=" && ";
             } else if(curLexeme.equals("&")){
                 javaCode+=" && ";
             } else if(curLexeme.equals("end")){
                 javaCode+="\n} \n";
             } else if(curLexeme.equals("#func")){
                 javaCode+="\npublic static void " + lexemes.get(++i) + lexemes.get(++i);
                 // Remove function parameters
                for(i++; !lexemes.get(i+1).equals(")"); i++);
             } else if(curLexeme.equals("#main")){
                 javaCode += "\npublic static void main(String[] args)";
                 i += 2;
             } else if(curLexeme.equals("#int") || curLexeme.equals("#char") || curLexeme.equals("#boolean")){
                 javaCode += curLexeme.substring(1)+" ";
             } else if(curLexeme.equals("#puts")){
                 javaCode += "System.out.println";
             } else if(curLexeme.equals("else")){
                 javaCode += "\n } else{ \n";
             } else if(curLexeme.equals(")")){
                 boolean check = false;
                 for(int j=i; check == false && j>0; j--){
                     if(lexemes.get(j).equals("(")){
                         check = true;
                         if(lexemes.get(j-1).equals("if") || lexemes.get(j-1).equals("while")){
                             javaCode += "){ \n";
                         } else{
                             javaCode += curLexeme;
                         }
                     }
                 } 
             } else{
                 if(curLexeme.equals("=")){
                     javaCode += " " +curLexeme + " ";
                 }else{
                     if(checkIfFunction(curLexeme)){
                         javaCode += curLexeme + "(";
                         // Remove parameters
                        for(i+=1; !lexemes.get(i+1).equals(")"); i++);
                     } else{
                         javaCode += curLexeme;
                     }
                 }
             }
         }
         this.javaCode += "}";
     }
     
     public void writeCompileRun(){
         WriteFile file = new WriteFile(new File("TARCCode.java"), javaCode); 
         file.write();
         try{
             Process pro;
             pro = Runtime.getRuntime().exec("javac TARCCode.java");
             printLines(" stderr:", pro.getErrorStream());
             pro = Runtime.getRuntime().exec("java TARCCode");
             printLines(" Output:", pro.getInputStream());
             printLines(" stderr:", pro.getErrorStream());
             pro.waitFor();
             System.out.println(" exitValue() " + pro.exitValue());
         }catch (Exception e) {
         }
     }
     
     private void printLines(String name, InputStream ins) throws Exception {
         String line;
         BufferedReader in = new BufferedReader(new InputStreamReader(ins));
         while ((line = in.readLine()) != null) {
             System.out.println(name + " " + line);
             this.output += line+"\n";
         }
     }
     
     public String getOutput(){
         return this.output;
     }
     
 }

 package cs444;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 
 import cs444.lexer.Lexer;
 import cs444.lexer.LexerException;
 import cs444.parser.IASTBuilder;
 import cs444.parser.JoosASTBuilder;
 import cs444.parser.Parser;
 import cs444.parser.TextReadingRules;
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ast.AInterfaceOrClassSymbol;
 import cs444.parser.symbols.ast.factories.ASTSymbolFactory;
 import cs444.parser.symbols.exceptions.UnexpectedTokenException;
 import cs444.types.PkgClassInfo;
 import cs444.types.PkgClassResolver;
 
 public class Compiler {
 
     private static final int COMPILER_ERROR_CODE = 42;
 
     /**
      * @param args
      * @throws Exception
      */
     public static void main(String[] args){
         System.exit(compile(args, true));
     }
 
     public static int compile(String[] files, boolean printErrors) {
         Reader reader = null;
         ANonTerminal parseTree = null;
 
         try {
             for(String fileName : files){
                 parseTree = parse(fileName, reader);
 
                 IASTBuilder builder = new JoosASTBuilder();
 
                 for(ASTSymbolFactory astSymbol : builder.getSimplifcations()) parseTree = (ANonTerminal)astSymbol.convertAll(parseTree);
 
                 File file = new File(fileName);
                 if(!builder.isValidFileName(file.getName(), parseTree)){
                     System.err.print("Invalid file name" + fileName);
                     return COMPILER_ERROR_CODE;
                 }
 
                 PkgClassInfo.instance.addClassOrInterface((AInterfaceOrClassSymbol)parseTree);
             }
         }catch(Exception e){
             if (printErrors) e.printStackTrace();
             return COMPILER_ERROR_CODE;
         }finally{
             if(reader != null){
                 try {
                     reader.close();
                 } catch (IOException e) {
                     if (printErrors) e.printStackTrace();
                 }
             }
         }
 
         for(PkgClassResolver resolver : PkgClassInfo.instance.getSymbols()){
             try {
                 resolver.build();
             } catch (Exception e) {
                 if(printErrors) e.printStackTrace();
                 return COMPILER_ERROR_CODE;
             }
         }
 
         return 0;
     }
 
     /**
      * @param fileName
      * @return
      * @throws FileNotFoundException
      * @throws IOException
      * @throws LexerException
      * @throws UnexpectedTokenException
      */
     private static ANonTerminal parse(String fileName, Reader reader)
             throws FileNotFoundException, IOException, LexerException,
             UnexpectedTokenException {
         ANonTerminal parseTree;
 
        reader = new FileReader(fileName);
         Lexer lexer = new Lexer(reader);
         Parser parser = new Parser(new TextReadingRules(new File("JoosRules.txt")));
 
         parseTree = parser.parse(lexer);
         return parseTree;
     }
 }

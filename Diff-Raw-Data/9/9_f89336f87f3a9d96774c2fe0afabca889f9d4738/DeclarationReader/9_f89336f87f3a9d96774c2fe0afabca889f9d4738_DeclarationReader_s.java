 package de.skuzzle.polly.core.parser.ast.declarations;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.io.UnsupportedEncodingException;
 
 import de.skuzzle.polly.core.parser.InputParser;
 import de.skuzzle.polly.core.parser.ParseException;
 import de.skuzzle.polly.core.parser.ast.expressions.Assignment;
 import de.skuzzle.polly.core.parser.ast.expressions.Expression;
 import de.skuzzle.polly.core.parser.ast.visitor.ASTTraversalException;
 import de.skuzzle.polly.core.parser.ast.visitor.ExecutionVisitor;
 import de.skuzzle.polly.core.parser.ast.visitor.resolving.TypeResolver;
 import de.skuzzle.polly.core.parser.problems.SimpleProblemReporter;
 
 
 /**
  * Class to read assignments from a file. The assignments must be valid polly statements
  * and separated by lines. Any empty line or line that starts with a '#' will be skipped.
  * 
  * @author Simon Taddiken
  */
 public class DeclarationReader implements Closeable {
 
     
     
     /**
      * Parser subclass that changes visibility of the parseExpr method. This is used to
      * parse a single assignment from file.
      * 
      * @author Simon Taddiken
      */
     private final class DeclarationParser extends InputParser {
 
         public DeclarationParser(String input, String charset) 
                 throws UnsupportedEncodingException {
             super(input, new SimpleProblemReporter());
         }
         
         
         
         @Override
         public Expression parseAssignment() throws ParseException {
             return super.parseAssignment();
         }
     }
     
     
 
     private final String charset;
     private final LineNumberReader reader;
     private final File file;
     private final ExecutionVisitor executor;
     private final Namespace nspace;
     
     
     
     /**
      * Creates a new DeclarationReader.
      * 
      * @param file Input file to read the declarations from.
      * @param charset Encoding name of that file.
      * @param nspace The namespace where the read declarations are stored.
      * @throws FileNotFoundException If <code>file</code> did not exist.
      */
     public DeclarationReader(File file, String charset, Namespace nspace) 
             throws FileNotFoundException {
         this.charset = charset;
         this.file = file;
         this.reader = new LineNumberReader(new FileReader(file));
         this.nspace = nspace;
         this.executor = new ExecutionVisitor(nspace, nspace, new SimpleProblemReporter());
     }
     
     
     
     /**
      * Reads all declarations from the file and stores them in the namespace.
      * 
      * @throws IOException If an IO error occurs.
      * @see #readDeclaration
      */
     public void readAll() throws IOException {
         while(this.readDeclaration());
     }
     
     
     
     /**
      * <p>Reads the next declaration from the declaration file and stores it to the 
      * namespace that this class was created with. If the read declaration is
      * invalid, it will be skipped. In this case, this method might return 
      * <code>true</code> anyway if there are more declarations to read.</p>
      * 
      * @return <code>true</code> if more declarations are to be read, <code>false</code>
      *          if no more declarations are available.
      * @throws IOException If an IO error occurs.
      */
     public boolean readDeclaration() throws IOException {
         String line = this.reader.readLine();
         if (line == null) {
             return false;
        } else if (line.equals("") || line.startsWith("#")) {
             // line to be skipped
             return true;
         }
         
         final DeclarationParser p = new DeclarationParser(line, this.charset);
         try {
             final Expression exp = p.parseAssignment();
             if (!(exp instanceof Assignment)) {
                 return true;
             }
             
             final Assignment assign = (Assignment) exp;
             TypeResolver.resolveAST(assign, this.nspace, new SimpleProblemReporter());
             assign.visit(this.executor);
             return true;
         } catch (ASTTraversalException e) {
             // skip this delcaration because it was invalid.
             return true;
         } catch (Exception e) {
             throw new IOException("In file: " + 
                 this.file.getName() + ", in line: " + this.reader.getLineNumber() +
                 ": " + e.getMessage(), e);
         }
     }
     
     
     
     @Override
     public void close() throws IOException {
         if (this.reader != null) {
             this.reader.close();
         }
     }
 }

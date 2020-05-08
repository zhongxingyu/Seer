 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.build;
 
 import com.google.common.collect.Lists;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.security.Permission;
 import java.util.regex.Pattern;
 
 
 /**
  * Calls ANTLR parser generator.
  * <p>
  * Previous call from Ant script was starting a JVM with its own {@code user.dir} system property
  * set to the directory containing grammar file. This new JVM induced a small little overhead that
  * is avoided now as this class calls {@code org.antlr.Tool} directly. 
  * <p>
  * Since ANTLR-3.1.1 doesn't generate correct headers (for both parser and lexer), there is 
  * a replacement of the first import declaration, by correct package declaration plus expected
  * import.
  * <p>
  * Because {@code org.antlr.Tool} calls {@code System.exit} at the end, this class wraps the
  * call in a block with a {@code SecurityManager} preventing from VM termination.
  * <p>
  * Because {@code org.antlr.Tool} directly writes errors on {@code System.err}, this one is
  * trapped temporarily.
  * 
  *
  * @author Laurent Caillette
  */
 public class AntlrGenerator extends JavaGenerator {
 
   /**
    * Too bad, on't use Novelang log because of build structure. 
    */
   private static final Logger LOGGER = LoggerFactory.getLogger( AntlrGenerator.class ) ;
   
   private static final String FIRST_IMPORT = "import novelang.parser.antlr.ProblemDelegate ;" ;
 
   public AntlrGenerator(
       File grammarFile,
       String packageName,
       String className,
       File targetDirectory
   ) throws IOException
   {
     super( grammarFile, packageName, className, targetDirectory ) ;
   }
 
   public void generate() throws IOException {
 
     final File targetDirectory = getTargetFile().getParentFile() ;
     createDirectory( targetDirectory ) ;
 
     createDirectory( getGrammarFile() ) ;
     
     final String[] arguments = {
         "-trace",
         "-fo",
         targetDirectory.getCanonicalPath(), 
         getGrammarFile().getAbsolutePath() 
     } ;
     
     LOGGER.info( "Command line: {}", Lists.newArrayList( arguments ) ) ;
 
     final SystemErrorStreamTrapper systemErrorStreamTrapper = new SystemErrorStreamTrapper() ;
     systemErrorStreamTrapper.install() ;
 
     forbidSystemExitCall() ;
 
     try {
       try {
         LOGGER.info( "Running ANTLR..." ) ;
         org.antlr.Tool.main( arguments ) ;
         LOGGER.info( "ANTLR ran successfully." ) ;
       } catch( ExitTrappedException e ) {
         // Do nothing, just prevents org.antlr.Tool from stopping the VM.
       } finally {
         enableSystemExitCall() ;
       }
       checkAntlrToolOutput( systemErrorStreamTrapper.getOutput() ) ;
     } finally {
       systemErrorStreamTrapper.uninstall() ;
     }
 
     fixPackageDeclaration( targetDirectory, getClassName(), "Parser" ) ;
     fixPackageDeclaration( targetDirectory, getClassName(), "Lexer" ) ;
 
   }
 
   private void fixPackageDeclaration( 
       File directory, 
       String radix, 
       String suffix 
   ) throws IOException {
     
     final File javaFile = new File( directory, radix + suffix + JAVA_EXTENSION ) ;
     LOGGER.info( "Fixing package declaration for file: {}", javaFile.getAbsolutePath() ) ;
         
     final String javaWithoutImports = IOUtils.toString( new FileInputStream( javaFile) ) ;
     
     final String javaWithImports = javaWithoutImports.replaceAll(
         Pattern.quote( FIRST_IMPORT ),
         "package " + getPackageName() + " ;\n" + FIRST_IMPORT
     ) ;
 
     final FileOutputStream fileOutputStream = new FileOutputStream( javaFile ) ;
     IOUtils.write( javaWithImports, fileOutputStream ) ;
     fileOutputStream.flush() ;
     fileOutputStream.close() ;
   }
 
   protected String generateCode() throws IOException {
     throw new UnsupportedOperationException( "Don't call this method" ) ; 
   }
 
   private static final String ANTLR_OUTPUT_INTRODUCTION = "ANTLR Parser Generator  Version 3.1.1";
 
   /**
    * Checks ANTLR output on the console. This is an easy way to ensure we've been running 
    * ANTLR as expected.
    */
   private void checkAntlrToolOutput( String output ) {
    final String antlrIntroductoryLine = ANTLR_OUTPUT_INTRODUCTION + "\n\n" ;
     if( output.startsWith( antlrIntroductoryLine ) ) {
       final int introductoryLineLength = antlrIntroductoryLine.length();
      if( output.length() > introductoryLineLength ) {
         throw new RuntimeException( "\n" + output.substring( introductoryLineLength ) ) ;
       }
     } else {
       throw new IllegalStateException( 
           "ANTLR output doesn't start with expected string: \n" + antlrIntroductoryLine + 
           "\nInstead got:\n" + output
       ) ;
     }
   }
 
   
   
 // =======================  
 // Forbid System.exit call
 // =======================  
   
   private static class ExitTrappedException extends SecurityException { }
 
   private static void forbidSystemExitCall() {
     final SecurityManager securityManager = new SecurityManager() {
       public void checkPermission( Permission permission ) {
 	    final String permissionName = permission.getName() ;
         if( permissionName.startsWith( "exitVM" ) ) {
           LOGGER.debug( "Checking permission " + permissionName ) ;
           throw new ExitTrappedException() ;
         }
       }
     } ;
     System.setSecurityManager( securityManager ) ;
   }
 
   private static void enableSystemExitCall() {
     System.setSecurityManager( null ) ;
   }
 
 // ======================  
 // Trap System.err output  
 // ======================
   
   private class SystemErrorStreamTrapper {
     
     private PrintStream initialPrintStream ;
     private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() ;
 
     public void install() {
       synchronized( System.class ) {
         if( initialPrintStream != null ) {
           throw new IllegalStateException( "Already installed" ) ;
         }
         initialPrintStream = System.err ;
         System.setErr( new PrintStream( byteArrayOutputStream ) ) ;
       }
     }
     
     public void uninstall() {
       synchronized( System.class ) {
         if( initialPrintStream == null ) {
           throw new IllegalStateException( "Not installed or already uninstalled" ) ;
         }
         System.setErr( initialPrintStream ) ;
       }      
     }
     
     public String getOutput() {
       return new String( byteArrayOutputStream.toByteArray() ) ;
     }
   }
   
 }

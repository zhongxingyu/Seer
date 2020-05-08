 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.cli;
 
 import de.weltraumschaf.jebnf.ExitCode;
 import de.weltraumschaf.jebnf.ast.nodes.Syntax;
 import de.weltraumschaf.jebnf.ast.visitor.TextGeneratingVisitor;
 import de.weltraumschaf.jebnf.ast.visitor.TextSyntaxTree;
 import de.weltraumschaf.jebnf.ast.visitor.Xml;
 import de.weltraumschaf.jebnf.gfx.CreatorHelper;
 import de.weltraumschaf.jebnf.gfx.RailroadDiagram;
 import de.weltraumschaf.jebnf.gfx.RailroadDiagramImage;
 import de.weltraumschaf.jebnf.parser.Factory;
 import de.weltraumschaf.jebnf.parser.Parser;
 import de.weltraumschaf.jebnf.parser.SyntaxException;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 /**
  * Command line application.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class CliApp extends BaseInvokeable implements Invokeable {
 
     /**
      * Default width in pixel.
      */
     private static final int WIDTH = 800;
 
     /**
      * Default height in pixel.
      */
     private static final int HEIGHT = 600;
 
     /**
      * Helper to create diagrams.
      */
     private final CreatorHelper helper = new CreatorHelper();
 
     /**
      * Initializes app with options and IO streams.
      *
      * @param options Command line options
      * @param ioStreams IO streams.
      * @param invoker Invoked the invokable.
      */
     public CliApp(final CliOptions options, final IOStreams ioStreams, final Invoker invoker) {
         super(options, ioStreams, invoker);
     }
 
     @Override
     public void execute() {
         if (!options.hasSyntaxFile()) {
             ioStreams.printlnErr("No syntax file given!");
             invoker.exit(ExitCode.NO_SYNTAX);
         }
 
         final String syntaxFileName = options.getSyntaxFile();
 
         try {
             final File syntaxFile = new File(syntaxFileName);
             final Parser parser   = Factory.newParserFromSource(syntaxFile);
             final Syntax ast      = parser.parse();
 
             if (options.isTextTree()) {
                 generateTextTree(ast);
             } else if (options.getOutputFormat() == OutputFormat.XML) {
                 generateXmlTree(ast);
             } else {
                 generateRailroadImage(ast);
             }
         } catch (SyntaxException ex) {
             ioStreams.printlnErr("Syntax error: " + ex.getMessage());
 
             if (options.isDebug()) {
                 ioStreams.printStackTraceToStdErr(ex);
             }
 
             invoker.exit(ExitCode.SYNTAX_ERROR);
         } catch (FileNotFoundException ex) {
             ioStreams.printlnErr(String.format("Can not read syntax file '%s'!", syntaxFileName));
 
             if (options.isDebug()) {
                 ioStreams.printStackTraceToStdErr(ex);
             }
 
             invoker.exit(ExitCode.READ_ERROR);
         } catch (IOException ex) {
             ioStreams.printlnErr(String.format("Can not read syntax file '%s'!", syntaxFileName));
 
             if (options.isDebug()) {
                 ioStreams.printStackTraceToStdErr(ex);
             }
 
             invoker.exit(ExitCode.READ_ERROR);
         }
 
         invoker.exit(ExitCode.OK);
     }
 
     /**
      * Play around with image generation.
      *
      * XXX: Does at the moment print always the same thing for development.
      *
      * @param ast Syntax tree to format.
      */
    private void generateRailroadImage(final Syntax ast) {
         final RailroadDiagramImage img = new RailroadDiagramImage(WIDTH, HEIGHT, new File("./test.png"));
         final RailroadDiagram diagram = helper.createDiagram(img.getGraphics());
         diagram.setDebug(options.isDebug());
         img.setDiagram(diagram);
         img.paint();
 
         try {
             img.save();
         } catch (IOException ex) {
             ioStreams.getStderr().println("Can't write file!");
         }
     }
 
     /**
      * Generate ASCII text tree from syntax tree.
      *
      * @param ast Syntax tree to format.
      * @throws IOException On write error to output file.
      */
     private void generateTextTree(final Syntax ast) throws IOException {
         final TextGeneratingVisitor visitor = new TextSyntaxTree();
         ast.accept(visitor);
         handleOutput(visitor.getText());
     }
 
     /**
      * Generate XML from the syntax tree.
      *
      * @param ast Syntax tree to format.
      * @throws IOException On write error to output file.
      */
     private void generateXmlTree(final Syntax ast) throws IOException {
         final TextGeneratingVisitor visitor = new Xml();
         ast.accept(visitor);
         handleOutput(visitor.getText());
     }
 
     /**
      * Determines if the output should be written to STDOUT or file.
      *
      * @param output String to write.
      * @throws IOException On write error to output file.
      */
     private void handleOutput(final String output) throws IOException {
         if (options.hasOutputFile()) {
             final File file = new File(options.getOutputFile());
             final Writer writer = new BufferedWriter(new FileWriter(file));
             writer.write(output);
             writer.close();
         } else {
             ioStreams.println(output);
         }
     }
 
 }

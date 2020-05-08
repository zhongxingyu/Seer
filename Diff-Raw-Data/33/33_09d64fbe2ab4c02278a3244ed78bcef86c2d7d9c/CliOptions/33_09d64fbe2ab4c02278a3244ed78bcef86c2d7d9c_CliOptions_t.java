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
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 /**
  * Available command line arguments.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class CliOptions {
 
     /**
      * Name of the CLI executable.
      */
     static final String EXECUTABLE = "jebnf";
 
     /**
      * Usage header.
      *
      * TODO: Write text.
      */
     private static final String HEADER = String.format("%nWrite sone helpful text.%n%n");
 
     /**
      * Author name and email address.
      */
     private static final String AUTHOR = "Sven Strittmatter <weltraumschaf@googlemail.com>";
 
     /**
      * URI to issue tracker.
      */
     private static final String ISSUE_TRACKER = "https://github.com/Weltraumschaf/jebnf/issues";
     /**
      * Usage footer.
      */
     private static final String FOOTER = String.format("%nWritten 2012 by %s%nWrite bugs to %s",
                                                        AUTHOR, ISSUE_TRACKER);
 
     /**
      * Options configuration.
      */
     private final Options options;
 
     /**
      * Which output format to print (XML or image types etc.).
      *
      * Ignored for ASCII text tree.
      */
     private OutputFormat outputFormat;
 
     /**
      * File name to read from.
      */
     private String syntaxFile = "";
 
     /**
      * File name to print in.
      */
     private String outputFile = "";
 
     /**
      * Print ASCII text tree or not.
      */
     private boolean textTree;
 
     /**
      * Print debug information or not.
      */
     private boolean debug;
 
     /**
      * Print help message or not.
      *
      * Ignores all other options.
      */
     private boolean help;
 
     /**
      * Start the IDE.
      *
      * Ignore all other options.
      */
     private boolean ide;
 
     /**
      * Show version or net.
      */
     private boolean showVersion;
 
     /**
      * Configures the {@link #options}.
      */
     public CliOptions() {
         options = new Options();
         // w/ argument
        options.addOption(OptionBuilder.withDescription(
                "EBNF syntax file to parse. Required option, unless you use the IDE.")
                                        .withArgName("file")
                                        .hasArg()
                                        .create(OptionsParser.OPT_SYNTAX));
        options.addOption(OptionBuilder.withDescription(
                "Output file name. If omitted output will be print to STDOUT.")
                                        .withArgName("file")
                                        .hasArg()
                                        .create(OptionsParser.OPT_OUTPUT));
         options.addOption(OptionBuilder.withDescription("Output format: xml, jpg, gif, or png.")
                                        .withArgName("format")
                                        .hasArg()
                                        .create(OptionsParser.OPT_FORMAT));
         // w/o argument
         options.addOption(OptionsParser.OPT_TEXT_TREE,
                           false,
                           "Prints textual representation of the syntax tree to stdout.");
         options.addOption(OptionsParser.OPT_DEBUG,     false, "Enables debug output.");
         options.addOption(OptionsParser.OPT_HELP,      false, "This help.");
         options.addOption(OptionsParser.OPT_VERSION,   false, "Show version information.");
         options.addOption(OptionsParser.OPT_IDE, OptionsParser.OPT_IDE_LONG, false, "Starts the GUI IDE.");
     }
 
     /**
      * Get the configured options.
      *
      * @return Commons CLI object.
      */
     public Options getOptions() {
         return options;
     }
 
     /**
      * Set print debugging.
      *
      * @param onOrOff True enables, false disables (default).
      */
     public void setDebug(final boolean onOrOff) {
         this.debug = onOrOff;
     }
 
     /**
      * Set show help message.
      *
      * @param onOrOff True enables, false disables (default).
      */
     public void setHelp(final boolean onOrOff) {
         this.help = onOrOff;
     }
 
     /**
      * Set start IDE.
      *
      * @param onOrOff True enables, false disables (default).
      */
     public void setIde(final boolean onOrOff) {
         this.ide = onOrOff;
     }
 
     /**
      * Set filename to save output.
      *
      * @param outputFile File name string.
      */
     public void setOutputFile(final String outputFile) {
         this.outputFile = outputFile;
     }
 
     /**
      * Set output format.
      *
      * @param outputFormat Output format.
      */
     public void setOutputFormat(final OutputFormat outputFormat) {
         this.outputFormat = outputFormat;
     }
 
     /**
      * Set syntax input file.
      *
      * @param syntaxFile File name string.
      */
     public void setSyntaxFile(final String syntaxFile) {
         this.syntaxFile = syntaxFile;
     }
 
     /**
      * Set print ASCII text tree.
      *
      * @param onOrOff True prints text ree, false is default.
      */
     public void setTextTree(final boolean onOrOff) {
         this.textTree = onOrOff;
     }
 
 
     /**
      * Set version output option.
      *
      * @param onOrOff True will shows version, false not.
      */
     public void setVersion(final boolean onOrOff) {
         this.showVersion = onOrOff;
     }
 
     /**
      * Parse given argument strings.
      *
      * @param args Argument strings.
      * @throws ParseException On parse errors.
      */
     public void parse(final String[] args) throws ParseException {
         final OptionsParser parser = new OptionsParser(this);
         parser.parse(args);
     }
 
     /**
      * Whether the output format was set.
      *
      * @return True if {@link #outputFormat} is not null, unless false.
      */
     public boolean hasOutputFormat() {
         return null != outputFormat;
     }
 
     /**
      * Get the output format.
      *
      * @return Output format object.
      */
     public OutputFormat getOutputFormat() {
         return outputFormat;
     }
 
     /**
      * Whether {@link #outputFile} is not null and not empty.
      *
      * @return True if {@link #outputFile} is a not empty string.
      */
     public boolean hasOutputFile() {
         return null != outputFile && !outputFile.isEmpty();
     }
 
     /**
      * Get the output file name.
      *
      * @return Return filename string.
      */
     public String getOutputFile() {
         return outputFile;
     }
 
     /**
      * Whether {@link #syntaxFile} is not null and not empty.
      *
      * @return True if {@link #syntaxFile} is a not empty string.
      */
     public boolean hasSyntaxFile() {
         return null != syntaxFile && !syntaxFile.isEmpty();
     }
 
     /**
      * Get the syntax input file name.
      *
      * @return Return filename string.
      */
     public String getSyntaxFile() {
         return syntaxFile;
     }
 
     /**
      * Whether debug is enabled or not.
      *
      * @return Return true if debug is enabled, unless false.
      */
     public boolean isDebug() {
         return debug;
     }
 
     /**
      * Whether show help message is enabled or not.
      *
      * @return Return true if show help message is enabled, unless false.
      */
     public boolean isHelp() {
         return help;
     }
 
     /**
      * Whether print text tree is enabled or not.
      *
      * @return Return true if print text tree is enabled, unless false.
      */
     public boolean isTextTree() {
         return textTree;
     }
 
     /**
      * Whether show version information is enabled or not.
      *
      * @return Return true if show version is enabled, unless false.
      */
     boolean isShowVersion() {
         return showVersion;
     }
 
     /**
      * Whether execute IDE is enabled or not.
      *
      * @return Return true if execute IDE is enabled, unless false.
      */
     public boolean isIde() {
         return ide;
     }
 
     /**
      * Format the command line options.
      *
      * Useful to show help message.
      *
      * @param formatter Formatter to format with.
      * @param out Stream to print formatted output.
      */
     public void format(final HelpFormatter formatter, final PrintStream out) {
         final PrintWriter writer = new PrintWriter(out);
         formatter.printHelp(
             writer,
             HelpFormatter.DEFAULT_WIDTH,
             EXECUTABLE,
             HEADER,
             options,
             HelpFormatter.DEFAULT_LEFT_PAD,
             HelpFormatter.DEFAULT_DESC_PAD,
             FOOTER,
             true);
         writer.flush();
     }
 
 }

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
 package novelang.configuration.parse;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.List;
 import java.nio.charset.Charset;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import novelang.system.LogFactory;
 import novelang.system.Log;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 /**
  * Base class for command-line parameters parsing.
  *
  * @author Laurent Caillette
  */
 public abstract class GenericParameters {
 
   private static final Log LOG = LogFactory.getLog( GenericParameters.class ) ;
 
   protected final Options options ;
   protected final CommandLine line ;
   protected final HelpPrinter helpPrinter;
 
   private final File baseDirectory ;
   private final Iterable< File > fontDirectories ;
   private final File styleDirectory ;
   private final File hyphenationDirectory ;
   private final Charset defaultSourceCharset ;
   private final Charset defaultRenderingCharset ;
 
   private final File logDirectory ;
 
   public GenericParameters(
       File baseDirectory,
       String[] parameters
   )
       throws ArgumentException
   {    
     LOG.debug( "Base directory: '%s'", baseDirectory.getAbsolutePath() ) ;
     LOG.debug( "Parameters: '%s'", Lists.newArrayList( parameters ) ) ;
 
     this.baseDirectory = Preconditions.checkNotNull( baseDirectory ) ;
     options = new Options() ;
     options.addOption( OPTION_HELP ) ;
     options.addOption( OPTION_FONT_DIRECTORIES ) ;
     options.addOption( OPTION_EMPTY ) ;
     options.addOption( OPTION_STYLE_DIRECTORY ) ;
     options.addOption( OPTION_LOG_DIRECTORY ) ;
     options.addOption( OPTION_HYPHENATION_DIRECTORY ) ;
     options.addOption( OPTION_DEFAULT_SOURCE_CHARSET ) ;
     options.addOption( OPTION_DEFAULT_RENDERING_CHARSET ) ;
     enrich( options ) ;
 
     helpPrinter = new HelpPrinter( options ) ;
 
     if( containsHelpTrigger( parameters ) ) {
       LOG.debug( "Help trigger detected" ) ;
       throw new ArgumentException( helpPrinter ) ;
     }
 
     final CommandLineParser parser = new PosixParser() ;
 
     try {
       line = parser.parse( options, parameters ) ;
 
       logDirectory = extractDirectory( baseDirectory, OPTION_LOG_DIRECTORY, line, false ) ;
 
       if( line.hasOption( OPTION_DEFAULT_SOURCE_CHARSET.getLongOpt() ) ) {
         defaultSourceCharset = Charset.forName(
             line.getOptionValue( OPTION_DEFAULT_SOURCE_CHARSET.getLongOpt() ) ) ;
       } else {
         defaultSourceCharset = null ;
       }
 
       if( line.hasOption( OPTION_DEFAULT_RENDERING_CHARSET.getLongOpt() ) ) {
         defaultRenderingCharset = Charset.forName(
             line.getOptionValue( OPTION_DEFAULT_RENDERING_CHARSET.getLongOpt() ) ) ;
       } else {
         defaultRenderingCharset = null ;
       }
 
       styleDirectory = extractDirectory( baseDirectory, OPTION_STYLE_DIRECTORY, line ) ;
       hyphenationDirectory = extractDirectory( baseDirectory, OPTION_HYPHENATION_DIRECTORY, line ) ;
 
       if( line.hasOption( OPTION_FONT_DIRECTORIES.getLongOpt() ) ) {
         final String[] fontDirectoriesNames =
             line.getOptionValues( OPTION_FONT_DIRECTORIES.getLongOpt() ) ;
         LOG.debug( "Argument for Font directories = '%s'",
             Lists.newArrayList( fontDirectoriesNames ) ) ;
         fontDirectories = extractDirectories( baseDirectory, fontDirectoriesNames ) ;
       } else {
         fontDirectories = ImmutableList.of() ;
       }
 
     } catch( ParseException e ) {
       throw new ArgumentException( e, helpPrinter ) ;
     }
 
   }
 
   protected abstract void enrich( Options options ) ;
 
   private void throwArgumentException( String message ) throws ArgumentException {
     throw new ArgumentException( message, helpPrinter ) ;
   }
 
 
 // =======
 // Getters
 // =======
 
   /**
    * Return the directory used to evaluate all relative directories from.
    * @return a non-null object.
    */
   public File getBaseDirectory() {
     return baseDirectory ;
   }
 
   /**
    * Returns a human-readable description of {@link #OPTION_FONT_DIRECTORIES}.
    */
   public String getFontDirectoriesOptionDescription() {
     return createOptionDescription( OPTION_FONT_DIRECTORIES ) ;
   }
 
   /**
    * Returns the directories containing embeddable font files.
    * @return a non-null object iterating over no nulls.
    */
   public Iterable< File > getFontDirectories() {
     return fontDirectories;
   }
 
   /**
    * Returns a human-readable description of {@link #OPTION_STYLE_DIRECTORY}.
    */
   public String getStyleDirectoryDescription() {
     return createOptionDescription( OPTION_STYLE_DIRECTORY ) ;
   }
 
   /**
    * Returns the directory containing style files.
    * @return a null object if undefined, a reference to an existing directory otherwise.
    */
   public File getStyleDirectory() {
     return styleDirectory;
   }
 
   /**
    * Returns a human-readable of {@link #OPTION_HYPHENATION_DIRECTORY}.
    */
   public String getHyphenationDirectoryOptionDescription() {
     return createOptionDescription( OPTION_HYPHENATION_DIRECTORY ) ;
   }
   /**
    * Returns the directory containing hyphenation files.
    * @return a null object if undefined, a reference to an existing directory otherwise.
    */
   public File getHyphenationDirectory() {
     return hyphenationDirectory;
   }
 
   /**
    * Returns the directory to spit log files into.
    * @return a null object if undefined, a reference to an existing directory otherwise.
    */
   public File getLogDirectory() {
     return logDirectory;
   }
 
   /**
    * Returns a human-readable of {@link #OPTION_DEFAULT_SOURCE_CHARSET}.
    */
   public String getDefaultSourceCharsetOptionDescription() {
     return createOptionDescription( OPTION_DEFAULT_SOURCE_CHARSET ) ;
   }
   /**
    * Returns the default charset for source documents.
    * @return a null object if undefined, a valid {@code Charset} otherwise.
    */
   public Charset getDefaultSourceCharset() {
     return defaultSourceCharset ;
   }
 
   /**
    * Returns a human-readable of {@link #OPTION_DEFAULT_RENDERING_CHARSET}.
    */
   public String getDefaultRenderingCharsetOptionDescription() {
     return createOptionDescription( OPTION_DEFAULT_RENDERING_CHARSET ) ;
   }
   /**
    * Returns the default charset for rendering documents.
    * @return a null object if undefined, a valid {@code Charset} otherwise.
    */
   public Charset getDefaultRenderingCharset() {
     return defaultRenderingCharset;
   }
 
   // ==========
 // Extractors
 // ==========
 
   protected File extractDirectory(
       File baseDirectory,
       Option option,
       CommandLine line
   ) throws ArgumentException {
     return extractDirectory( baseDirectory, option, line, true ) ;
   }
 
   protected File extractDirectory(
       File baseDirectory,
       Option option,
       CommandLine line,
       boolean failOnNonExistingDirectory
   )
       throws ArgumentException
   {
     final File directory ;
     if( line.hasOption( option.getLongOpt() ) ) {
       final String directoryName =
           line.getOptionValue( option.getLongOpt() ) ;
       LOG.debug( "Argument for %s = '%s'", option.getDescription(), directoryName ) ;
       directory = extractDirectory( baseDirectory, directoryName, failOnNonExistingDirectory ) ;
     } else {
       directory = null ;
     }
     return directory ;
   }
 
   protected final Iterable< File > extractDirectories( File parent, String[] directoryNames )
       throws ArgumentException
   {
     final List directories = Lists.newArrayList() ;
     for( String directoryName : directoryNames ) {
       directories.add( extractDirectory( parent, directoryName, true ) ) ;
     }
     return ImmutableList.copyOf( directories ) ;
   }
 
   protected final File extractDirectory(
       File parent,
       String directoryName,
       boolean failOnNonExistingDirectory
   )
       throws ArgumentException
   {
     final File directory ;
    if( directoryName.startsWith( "/" ) ) {
      directory = new File( directoryName ) ;
     } else {
       directory = new File( parent, directoryName ) ;
     }
     if( failOnNonExistingDirectory && ! ( directory.exists() && directory.isDirectory() ) ) {
       throwArgumentException( "Not a directory: '" + directoryName + "'" ) ;
     }
     return directory ;
   }
 
 
 // =================
 // Commons-CLI stuff
 // =================
 
   public static final String OPTIONNAME_FONT_DIRECTORIES = "font-dirs" ;
 
   private static final Option OPTION_FONT_DIRECTORIES = OptionBuilder
       .withLongOpt( OPTIONNAME_FONT_DIRECTORIES )
       .withDescription( "Directories containing embeddable fonts" )
       .withValueSeparator()
       .hasArgs()
       .create()
   ;
 
   private static final Option OPTION_EMPTY = OptionBuilder
       .withLongOpt( "" )
       .withDescription( "Empty option to end directory list" )
       .create()
   ;
 
   private static final Option OPTION_STYLE_DIRECTORY = OptionBuilder
       .withLongOpt( "style-dir" )
       .withDescription( "Directory containing style files" )
       .withValueSeparator()
       .hasArg()
       .create()
   ;
 
   public static final String OPTIONNAME_DEFAULT_SOURCE_CHARSET = "source-charset" ;
 
   private static final Option OPTION_DEFAULT_SOURCE_CHARSET = OptionBuilder
       .withLongOpt( OPTIONNAME_DEFAULT_SOURCE_CHARSET )
       .withDescription( "Default charset for source documents" )
       .withValueSeparator()
       .hasArg()
       .create()
   ;
 
   public static final String OPTIONNAME_DEFAULT_RENDERING_CHARSET = "rendering-charset" ;
 
   private static final Option OPTION_DEFAULT_RENDERING_CHARSET = OptionBuilder
       .withLongOpt( OPTIONNAME_DEFAULT_RENDERING_CHARSET )
       .withDescription( "Default charset for rendered documents" )
       .withValueSeparator()
       .hasArg()
       .create()
   ;
 
 
   public static final String OPTIONPREFIX = "--" ;
   public static final String LOG_DIRECTORY_OPTION_NAME = "log-dir" ;
 
   private static final Option OPTION_LOG_DIRECTORY = OptionBuilder
       .withLongOpt( LOG_DIRECTORY_OPTION_NAME )
       .withDescription( "Directory containing log file(s)" )
       .withValueSeparator()
       .hasArg()
       .create()
   ;
 
   private static final Option OPTION_HYPHENATION_DIRECTORY = OptionBuilder
       .withLongOpt( "hyphenation-dir" )
       .withDescription( "Directory containing hyphenation files" )
       .withValueSeparator()
       .hasArg()
       .create()
   ;
 
   public static final String HELP_OPTION_NAME = "help";
   private static final Option OPTION_HELP = OptionBuilder
       .withLongOpt( HELP_OPTION_NAME )
       .withDescription( "Print help" )
       .create()
   ;
 
 
   protected static String createOptionDescription( Option option ) {
     return OPTIONPREFIX + option.getLongOpt() + ", " + option.getDescription() ;
   }
 
 
 // ====
 // Help
 // ====
 
   public static class HelpPrinter {
     private final Options options ;
 
     public HelpPrinter( Options options ) {
       this.options = options;
     }
 
     public void print( PrintStream printStream, String commandName, int columns ) {
       print( new PrintWriter( printStream ), commandName, columns ) ;
     }
     
     public void print( PrintWriter printWriter, String commandName, int columns ) {
       final HelpFormatter helpFormatter = new HelpFormatter() ;
       helpFormatter.printHelp(
           printWriter,
           columns,
           commandName,
           "",
           options,
           2,
           2,
           ""
       ) ;
       printWriter.flush() ;
     }
 
     public String asString( String commandName, int columns ) {
       final StringWriter stringWriter = new StringWriter() ;
       print( new PrintWriter( stringWriter ), commandName, columns ) ;
       return stringWriter.toString() ;
     }
   }
 
   private static final String HELP_TRIGGER = OPTIONPREFIX + OPTION_HELP.getLongOpt() ;
 
   private boolean containsHelpTrigger( String[] parameters ) {
     for( int i = 0 ; i < parameters.length ; i++ ) {
       final String parameter = parameters[ i ] ;
       if( HELP_TRIGGER.equals( parameter ) ) {
         return true ;
       }
     }
     return false ;
   }
 
 
 
 }

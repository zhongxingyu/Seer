 package com.robusta.pdc.command.line;
 
 import com.robusta.pdc.domain.CommandLineArguments;
 
 public interface CommandLineParser {
     public static final String OPTION_SOURCE_DIRECTORIES = "dir";
     public static final String OPTION_SOURCE_PACKAGES = "source";
     public static final String OPTION_TARGET_PACKAGES = "target";
     public static final String OPTION_HELP = "help";
     public static final String OPTION_VERBOSE = "verbose";
 
     /**
      * Parse the command line arguments supplied and build
      * {@link CommandLineArguments}.
      *
      * @param arguments String[] Command line arguments into the main method.
      * @return CommandLineArguments
      * @throws UserHasAskedForHelp when help option detected,
      * implementation must handle help and throw exception to
      * halt further execution.
      * @throws ParseCommandLineArgumentHasFailed validation of command line
      * arguments has failed.
      */
     CommandLineArguments parseCommandLine(String[] arguments) throws UserHasAskedForHelp, ParseCommandLineArgumentHasFailed;
 }

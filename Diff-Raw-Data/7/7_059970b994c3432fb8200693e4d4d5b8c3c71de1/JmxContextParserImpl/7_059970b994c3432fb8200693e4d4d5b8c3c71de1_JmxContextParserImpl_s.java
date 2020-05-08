 /*   Copyright 2011 Appbox inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ca.appbox.monitoring.jmx.jmxbox.commons.context.parser;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 
 import ca.appbox.monitoring.jmx.jmxbox.JmxClientApp;
 import ca.appbox.monitoring.jmx.jmxbox.commands.JmxCommand;
 import ca.appbox.monitoring.jmx.jmxbox.commands.JmxInvokeOperationCommandImpl;
 import ca.appbox.monitoring.jmx.jmxbox.commands.JmxReadAttributeCommandImpl;
 import ca.appbox.monitoring.jmx.jmxbox.commons.CommandLineOptions;
 import ca.appbox.monitoring.jmx.jmxbox.commons.JmxException;
 import ca.appbox.monitoring.jmx.jmxbox.commons.context.JmxContext;
 
 /**
  * Context parser implementation using the apache commons-cli library.
  * 
  */
 public class JmxContextParserImpl implements JmxContextParser {
 
 	private static final JmxContextParserImpl instance = new JmxContextParserImpl();
 
 	private final CommandLineParser commandLineParser;
 
 	private final Options options;
 
 	private JmxContextParserImpl() {
 		super();
 		commandLineParser = new PosixParser();
 		options = buildOptions();
 	}
 
 	public static JmxContextParserImpl getInstance() {
 		return instance;
 	}
 
 	public JmxContext parse(String[] args) throws JmxException {
 
 		JmxContext jmxContext = null;
 		try {
 			CommandLine commandLine = commandLineParser.parse(buildOptions(),
 					args, false);
 
 			if (commandLine.hasOption(CommandLineOptions.HELP)
 					|| !isValidCommandLine(commandLine)) {
				displayUsage();
 			}

 			jmxContext = buildJmxContext(commandLine);

 		} catch (Exception e) {
 			displayUsage();
 			throw new JmxException("");
 		}
 
 		return jmxContext;
 	}
 
 	private JmxContext buildJmxContext(CommandLine commandLine)
 			throws JmxException {
 
 		final String host = commandLine.getOptionValue(CommandLineOptions.HOST);
 		final Integer port = Integer.valueOf(commandLine
 				.getOptionValue(CommandLineOptions.PORT));
 		final String user = commandLine.getOptionValue(CommandLineOptions.USER);
 		final String password = commandLine
 				.getOptionValue(CommandLineOptions.PASSWORD);
 		final Integer internal = commandLine
 				.getOptionValue(CommandLineOptions.INTERVAL_IN_SECONDS) == null ? null
 				: Integer
 						.valueOf(commandLine
 								.getOptionValue(CommandLineOptions.INTERVAL_IN_SECONDS));
 
 		final File outputFile = commandLine
 				.getOptionValue(CommandLineOptions.OUTPUT_FILE) == null ? null
 				: new File(
 						commandLine
 								.getOptionValue(CommandLineOptions.OUTPUT_FILE));
 		final String delimiter = commandLine
 				.getOptionValue(CommandLineOptions.RECORD_DELIMITER) == null ? null
 				: commandLine
 						.getOptionValue(CommandLineOptions.RECORD_DELIMITER);
 
 		final Integer repetitions = commandLine
 				.getOptionValue(CommandLineOptions.REPETITIONS) == null ? null
 				: Integer.valueOf(commandLine
 						.getOptionValue(CommandLineOptions.REPETITIONS));
 
 		final boolean utcTimestamps = commandLine
 				.hasOption(CommandLineOptions.UTC_TIMESTAMPS);
 
 		JmxContext context = new JmxContext(host, port, user, password,
 				internal, outputFile, delimiter, repetitions, utcTimestamps);
 
 		addCommands(context, commandLine);
 
 		return context;
 
 	}
 
 	private void addCommands(JmxContext context, CommandLine commandLine)
 			throws JmxException {
 
 		addReadAttributeCommands(context, commandLine);
 		addInvokeOperationCommands(context, commandLine);
 	}
 
 	private void addInvokeOperationCommands(JmxContext context,
 			CommandLine commandLine) throws JmxException {
 
 		String[] invokeCommands = commandLine
 				.getOptionValues(CommandLineOptions.INVOKE);
 
 		if (invokeCommands != null) {
 
 			for (String invokeCommandParameter : Arrays.asList(invokeCommands)) {
 
 				String[] splittedReadCommand = invokeCommandParameter
 						.split(";");
 
 				// Has to be at least mBean;operationName
 				if (splittedReadCommand.length < 2) {
 					throw new JmxException(
 							"MBean invoke operation has to specifty at least the name of the mBean and the operation to invoke.");
 				} else {
 
 					JmxCommand invokeCommand = new JmxInvokeOperationCommandImpl(
 							splittedReadCommand[0], splittedReadCommand[1]);
 
 					List<String> mBeanAttributes = Arrays.asList(Arrays
 							.copyOfRange(splittedReadCommand, 2,
 									splittedReadCommand.length));
 
 					for (String commandAttribute : mBeanAttributes) {
 						((JmxInvokeOperationCommandImpl) invokeCommand)
 								.addParameter(commandAttribute);
 					}
 
 					context.addCommand(invokeCommand);
 				}
 			}
 		}
 	}
 
 	private void addReadAttributeCommands(JmxContext context,
 			CommandLine commandLine) throws JmxException {
 
 		String[] readAttributeCommands = commandLine
 				.getOptionValues(CommandLineOptions.READ_ATTRIBUTE);
 
 		if (readAttributeCommands != null) {
 
 			for (String readCommandParameter : Arrays
 					.asList(readAttributeCommands)) {
 
 				String[] splittedReadCommand = readCommandParameter.split(";");
 
 				// Has to be at least mBean;attribute
 				if (splittedReadCommand.length < 2) {
 					throw new JmxException(
 							"MBean read attribute has to specify at least one attribute name.");
 				} else {
 
 					List<String> mBeanAttributes = Arrays.asList(Arrays
 							.copyOfRange(splittedReadCommand, 1,
 									splittedReadCommand.length));
 
 					for (String commandAttribute : mBeanAttributes) {
 						context.addCommand(new JmxReadAttributeCommandImpl(
 								splittedReadCommand[0], commandAttribute));
 					}
 				}
 			}
 		}
 	}
 
 	private boolean isValidCommandLine(CommandLine commandLine) {
 
 		boolean isValid = commandLine.hasOption(CommandLineOptions.HOST)
 				&& commandLine.hasOption(CommandLineOptions.PORT)
 				&& (commandLine.hasOption(CommandLineOptions.INVOKE) || commandLine
 						.hasOption(CommandLineOptions.READ_ATTRIBUTE));
 
 		return isValid;
 	}
 
 	private void displayUsage() {
 
 		// FIXME : Send a wrapped print writer to keep control over the app
 		// output.
 		HelpFormatter helpFormatter = new HelpFormatter();
 		helpFormatter.setOptionComparator(new CommandLineOptionsComparator());
 		helpFormatter.printHelp(JmxClientApp.class.getSimpleName(), options);
 
 	}
 
 	@SuppressWarnings("static-access")
 	private Options buildOptions() {
 
 		Options options = new Options();
 
 		Option help = OptionBuilder.withDescription("prints this message")
 				.create(CommandLineOptions.HELP);
 
 		Option host = OptionBuilder.withDescription("jmx host")
 				.withArgName("host").hasArg(true)
 				.create(CommandLineOptions.HOST);
 
 		Option port = OptionBuilder.withDescription("jmx port")
 				.withArgName("port").hasArg(true)
 				.create(CommandLineOptions.PORT);
 
 		Option user = OptionBuilder.withDescription("user").withArgName("user")
 				.hasArg(true).create(CommandLineOptions.USER);
 
 		Option password = OptionBuilder.withDescription("password")
 				.withArgName("password").hasArg(true)
 				.create(CommandLineOptions.PASSWORD);
 
 		Option interval = OptionBuilder
 				.withDescription("interval  (in miliseconds, default : 1000)")
 				.withArgName("interval").hasArg(true)
 				.create(CommandLineOptions.INTERVAL_IN_SECONDS);
 
 		Option outputFile = OptionBuilder
 				.withDescription("csv file for output")
 				.withArgName("output file").hasArg(true)
 				.create(CommandLineOptions.OUTPUT_FILE);
 
 		Option recordDelimiter = OptionBuilder
 				.withDescription("record delimiter (default : \";\")")
 				.withArgName("record deliniter").hasArg(true)
 				.create(CommandLineOptions.RECORD_DELIMITER);
 
 		Option repetitions = OptionBuilder
 				.withDescription(
 						"number of repetitions (default : 1, 0 : loop for ever)")
 				.withArgName("repetitions").hasArg(true)
 				.create(CommandLineOptions.REPETITIONS);
 
 		Option readAttribute = OptionBuilder
 				.withDescription(
 						"attribute(s) to read (mBeanName;attr1;attr2;attrN)")
 				.withArgName("mBean attribute(s)").hasArg(true)
 				.create(CommandLineOptions.READ_ATTRIBUTE);
 
 		Option operation = OptionBuilder
 				.withDescription(
 						"operation to invoke (mBeanName;operationName;param1;param2;paramN)")
 				.withArgName("mBean operation").hasArg(true)
 				.create(CommandLineOptions.INVOKE);
 
 		Option utcTimestamps = OptionBuilder.withDescription(
 				"use UTC timestamps").create(CommandLineOptions.UTC_TIMESTAMPS);
 
 		options.addOption(help);
 		options.addOption(host);
 		options.addOption(port);
 		options.addOption(repetitions);
 		options.addOption(readAttribute);
 		options.addOption(operation);
 		options.addOption(user);
 		options.addOption(password);
 		options.addOption(interval);
 		options.addOption(outputFile);
 		options.addOption(recordDelimiter);
 		options.addOption(utcTimestamps);
 
 		return options;
 	}
 }

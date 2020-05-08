 /*******************************************************************************
  * Copyright (c) 2004, 2011 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.app.autrun;
 
 import java.net.ConnectException;
 import java.net.InetSocketAddress;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.Parser;
 import org.apache.commons.lang.ArrayUtils;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.jubula.app.autrun.i18n.Messages;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 import org.eclipse.osgi.util.NLS;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Starts an AUT and registers the AUT with an AUT Agent. In order to terminate 
  * at the right time (not too early, not too late) this application assumes 
  * that the following JVM parameters (or equivalent) are used:<ul>
  * <li>osgi.noShutdown=true</li> 
  * <li>eclipse.jobs.daemon=true</li> 
  * <li>eclipse.enableStateSaver=false</li> 
 * <li>osgi.framework.activeThreadType=false</li> 
  * </ul>
  * These parameters are required because the original application was designed 
  * to run outside of an OSGi context, i.e. the application should end only 
  * when no non-daemon threads are active.
  * 
 * The <i>osgi.framework.activeThreadType</i> entry can actually be 
 * anything other than "normal", but it must be present. This requirement exists
 * since changing org.eclipse.equinox.launcher from 1.1.0 to 1.2.0, and was the
 * cause of bug 375931.
 * 
  * @author BREDEX GmbH
  * @created Dec 9, 2009
  */
 public class AutRunApplication implements IApplication {
     
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(AutRunApplication.class);
     
     /**
      * <code>LAUNCHER_NAME</code>
      */
     private static final String LAUNCHER_NAME = "autrun"; //$NON-NLS-1$
 
     /** <code>TOOLKIT_RCP</code> */
     private static final String TK_RCP = "rcp"; //$NON-NLS-1$
 
     /** <code>TK_SWT</code> */
     private static final String TK_SWT = "swt"; //$NON-NLS-1$
 
     /** <code>TK_SWING</code> */
     private static final String TK_SWING = "swing"; //$NON-NLS-1$
 
     /** <code>DEFAULT_NAME_TECHNICAL_COMPONENTS</code> */
     private static final boolean DEFAULT_NAME_TECHNICAL_COMPONENTS = true;
 
     /** <code>DEFAULT_AUT_AGENT_PORT</code> */
     private static final int DEFAULT_AUT_AGENT_PORT = 60000;
 
     /** <code>DEFAULT_AUT_AGENT_HOST</code> */
     private static final String DEFAULT_AUT_AGENT_HOST = "localhost"; //$NON-NLS-1$
 
     // - Command line options - Start //
     /** port number for the Aut Agent with which to register */
     private static final String OPT_AUT_AGENT_PORT = "p"; //$NON-NLS-1$
 
     /** port number for the Aut Agent with which to register */
     private static final String OPT_AUT_AGENT_PORT_LONG = "autagentport"; //$NON-NLS-1$
     
     /** hostname for the Aut Agent with which to register */
     private static final String OPT_AUT_AGENT_HOST = "a"; //$NON-NLS-1$
     
     /** hostname for the Aut Agent with which to register */
     private static final String OPT_AUT_AGENT_HOST_LONG = "autagenthost"; //$NON-NLS-1$
     
     /** help option */
     private static final String OPT_HELP = "h"; //$NON-NLS-1$
     
     /** hostname for the Aut Agent with which to register */
     private static final String OPT_HELP_LONG = "help"; //$NON-NLS-1$
     
     /** name of the AUT to register */
     private static final String OPT_AUT_ID = "i"; //$NON-NLS-1$
     
     /** name of the AUT to register */
     private static final String OPT_AUT_ID_LONG = "autid"; //$NON-NLS-1$
     
     /** flag for name generation for certain technical components */
     private static final String OPT_NAME_TECHNICAL_COMPONENTS = "g"; //$NON-NLS-1$
 
     /** flag for name generation for certain technical components */
     private static final String OPT_NAME_TECHNICAL_COMPONENTS_LONG = "generatenames"; //$NON-NLS-1$
     
     /** keyboard layout */
     private static final String OPT_KEYBOARD_LAYOUT = "k"; //$NON-NLS-1$
 
     /** keyboard layout */
     private static final String OPT_KEYBOARD_LAYOUT_LONG = "kblayout"; //$NON-NLS-1$
 
     /** AUT working directory */
     private static final String OPT_WORKING_DIR = "w"; //$NON-NLS-1$
 
     /** AUT working directory */
     private static final String OPT_WORKING_DIR_LONG = "workingdir"; //$NON-NLS-1$
     
     /** executable file used to start the AUT */
     private static final String OPT_EXECUTABLE = "e"; //$NON-NLS-1$
     
     /** executable file used to start the AUT */
     private static final String OPT_EXECUTABLE_LONG = "exec"; //$NON-NLS-1$
     
     /** AUT agent hostname */
     private static final String HOSTNAME = "hostname"; //$NON-NLS-1$
     
     /** AUT agent port */
     private static final String PORT = "port"; //$NON-NLS-1$
     
     /** AUT id */
     private static final String ID = "id"; //$NON-NLS-1$
     
     /** technical components */
     private static final String TRUE_FALSE = "true / false"; //$NON-NLS-1$
     
     /** AUT keyboard layout */
     private static final String LOCALE = "locale"; //$NON-NLS-1$
     
     /** AUT working directory */
     private static final String DIRECTORY = "directory"; //$NON-NLS-1$
     
     /** AUT options */
     private static final String COMMAND = "command"; //$NON-NLS-1$
     
     /** swing class prefix */
     private static final String SWING_AUT_TOOLKIT_CLASS_PREFIX = "Swing"; //$NON-NLS-1$
     
     /** swt class prefix */
     private static final String SWT_AUT_TOOLKIT_CLASS_PREFIX = "Swt"; //$NON-NLS-1$
     
     /** rcp class prefix */
     private static final String RCP_AUT_TOOLKIT_CLASS_PREFIX = "Rcp"; //$NON-NLS-1$
     // - Command line options - End //
 
     /**
      * prints help options
      * @param pe a parse Execption - may also be null
      */
     private static void printHelp(ParseException pe) {
         HelpFormatter formatter = new HelpFormatter();
         formatter.setOptionComparator(new OptionComparator());
         if (pe != null) {
             formatter.printHelp(
                     LAUNCHER_NAME,
                     StringConstants.EMPTY,
                     createCmdLineOptions(), 
                     "\n" + pe.getLocalizedMessage(),  //$NON-NLS-1$
                     true);
         } else {
             formatter.printHelp(LAUNCHER_NAME,
                     createCmdLineOptions(), true);
         }
     }
 
     /**
      * Creates and returns settings for starting an AUT based on the given
      * command line.
      *  
      * @param cmdLine Provides the settings for the AUT configuration.
      * @return new settings for starting an AUT.
      */
     private static Map<String, Object> createAutConfig(CommandLine cmdLine) {
         Map<String, Object> autConfig = new HashMap<String, Object>();
         if (cmdLine.hasOption(OPT_WORKING_DIR)) {
             autConfig.put(AutConfigConstants.WORKING_DIR, cmdLine
                     .getOptionValue(OPT_WORKING_DIR));
         } else {
             autConfig.put(AutConfigConstants.WORKING_DIR, System
                     .getProperty("user.dir")); //$NON-NLS-1$
         }
         
         if (cmdLine.hasOption(OPT_NAME_TECHNICAL_COMPONENTS)) {
             autConfig.put(AutConfigConstants.NAME_TECHNICAL_COMPONENTS, Boolean
                     .valueOf(cmdLine
                             .getOptionValue(OPT_NAME_TECHNICAL_COMPONENTS)));
         } else {
             autConfig.put(AutConfigConstants.NAME_TECHNICAL_COMPONENTS,
                     DEFAULT_NAME_TECHNICAL_COMPONENTS);
         }
         autConfig.put(AutConfigConstants.EXECUTABLE, cmdLine
                 .getOptionValue(OPT_EXECUTABLE));
         
         if (cmdLine.hasOption(OPT_KEYBOARD_LAYOUT)) {
             autConfig.put(AutConfigConstants.KEYBOARD_LAYOUT, 
                     cmdLine.getOptionValue(OPT_KEYBOARD_LAYOUT));
         }
         
         String[] autArguments = cmdLine.getOptionValues(OPT_EXECUTABLE);
         if (autArguments.length > 1) {
             autConfig.put(AutConfigConstants.AUT_RUN_AUT_ARGUMENTS, 
                     ArrayUtils.subarray(autArguments, 1, autArguments.length));
         }
         
         return autConfig;
     }
 
     /**
      * @return the command line options available when invoking the main method. 
      */
     private static Options createCmdLineOptions() {
         Options options = new Options();
         Option autAgentHostOption = new Option(OPT_AUT_AGENT_HOST, true,
                 NLS.bind(Messages.infoAutAgentHost, DEFAULT_AUT_AGENT_HOST));
         autAgentHostOption.setLongOpt(OPT_AUT_AGENT_HOST_LONG);
         autAgentHostOption.setArgName(HOSTNAME);
         options.addOption(autAgentHostOption);
 
         Option autAgentPortOption = new Option(OPT_AUT_AGENT_PORT, true,
                 NLS.bind(Messages.infoAutAgentPort, DEFAULT_AUT_AGENT_PORT));
         autAgentPortOption.setLongOpt(OPT_AUT_AGENT_PORT_LONG);
         autAgentPortOption.setArgName(PORT);
         options.addOption(autAgentPortOption);
 
         OptionGroup autToolkitOptionGroup = new OptionGroup();
         autToolkitOptionGroup.addOption(new Option(TK_SWING,
                 Messages.infoSwingToolkit));
         autToolkitOptionGroup.addOption(new Option(TK_SWT,
                 Messages.infoSwtToolkit));
         autToolkitOptionGroup.addOption(new Option(TK_RCP,
                 Messages.infoRcpToolkit));
         autToolkitOptionGroup.setRequired(true);
         options.addOptionGroup(autToolkitOptionGroup);
 
         Option autIdOption = new Option(OPT_AUT_ID, true, Messages.infoAutId);
         autIdOption.setLongOpt(OPT_AUT_ID_LONG);
         autIdOption.setArgName(ID);
         autIdOption.setRequired(true);
         options.addOption(autIdOption);
 
         Option nameTechnicalComponentsOption = new Option(
                 OPT_NAME_TECHNICAL_COMPONENTS, true,
                 Messages.infoGenerateTechnicalComponentNames);
         nameTechnicalComponentsOption
                 .setLongOpt(OPT_NAME_TECHNICAL_COMPONENTS_LONG);
         nameTechnicalComponentsOption.setArgName(TRUE_FALSE);
         options.addOption(nameTechnicalComponentsOption);
 
         Option keyboardLayoutOption = new Option(OPT_KEYBOARD_LAYOUT, true,
                 Messages.infoKbLayout);
         keyboardLayoutOption.setLongOpt(OPT_KEYBOARD_LAYOUT_LONG);
         keyboardLayoutOption.setArgName(LOCALE);
         options.addOption(keyboardLayoutOption);
 
         Option workingDirOption = new Option(OPT_WORKING_DIR, true,
                 Messages.infoAutWorkingDirectory);
         workingDirOption.setLongOpt(OPT_WORKING_DIR_LONG);
         workingDirOption.setArgName(DIRECTORY);
         options.addOption(workingDirOption);
 
         Option helpOption = new Option(OPT_HELP, false, Messages.infoHelp);
         helpOption.setLongOpt(OPT_HELP_LONG);
         options.addOption(helpOption);
         
         OptionBuilder.hasArgs();
         Option autExecutableFileOption = OptionBuilder.create(OPT_EXECUTABLE);
         autExecutableFileOption.setDescription(Messages.infoExecutableFile);
         autExecutableFileOption.setLongOpt(OPT_EXECUTABLE_LONG);
         autExecutableFileOption.setRequired(true);
         autExecutableFileOption.setArgName(COMMAND);
         options.addOption(autExecutableFileOption);
         
         return options;
     }
     
     /**
      * This class implements the <code>Comparator</code> interface for comparing
      * Options.
      */
     private static class OptionComparator implements Comparator {
         /** {@inheritDoc} */
         public int compare(Object o1, Object o2) {
             Option opt1 = (Option)o1;
             Option opt2 = (Option)o2;
             // always list -exec as last option
             if (opt1.getOpt().equals(OPT_EXECUTABLE)) {
                 return 1;
             }
             if (opt2.getOpt().equals(OPT_EXECUTABLE)) {
                 return -1;
             }
             return 0;
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public Object start(IApplicationContext context) throws Exception {
         String[] args = (String[])context.getArguments().get(
                 IApplicationContext.APPLICATION_ARGS);
         if (args == null) {
             args = new String[0];
         }
 
         Options options = createCmdLineOptions();
         Parser parser = new BasicParser();
         CommandLine cmdLine = null;
         try {
             cmdLine = parser.parse(options, args, false);
         } catch (ParseException pe) {
             printHelp(pe);
         }
         
         if (cmdLine != null && !cmdLine.hasOption(OPT_HELP)) {
             String toolkit = StringConstants.EMPTY;
             if (cmdLine.hasOption(TK_SWING)) {
                 toolkit = SWING_AUT_TOOLKIT_CLASS_PREFIX;
             } else if (cmdLine.hasOption(TK_SWT)) {
                 toolkit = SWT_AUT_TOOLKIT_CLASS_PREFIX;
             } else if (cmdLine.hasOption(TK_RCP)) {
                 toolkit = RCP_AUT_TOOLKIT_CLASS_PREFIX;
             }
 
             int autAgentPort = DEFAULT_AUT_AGENT_PORT;
             if (cmdLine.hasOption(OPT_AUT_AGENT_PORT)) {
                 try {
                     autAgentPort = Integer.parseInt(cmdLine
                             .getOptionValue(OPT_AUT_AGENT_PORT));
                 } catch (NumberFormatException nfe) {
                     // use default
                 }
             }
             String autAgentHost = DEFAULT_AUT_AGENT_HOST;
             if (cmdLine.hasOption(OPT_AUT_AGENT_HOST)) {
                 autAgentHost = cmdLine.getOptionValue(OPT_AUT_AGENT_HOST);
             }
             
             InetSocketAddress agentAddr = 
                 new InetSocketAddress(autAgentHost, autAgentPort);
             AutIdentifier autId = new AutIdentifier(cmdLine
                     .getOptionValue(OPT_AUT_ID));
             
             Map<String, Object> autConfiguration = createAutConfig(cmdLine);
             
             AutRunner runner = new AutRunner(
                     toolkit, autId, agentAddr, autConfiguration);
             try {
                 runner.run();
             } catch (ConnectException ce) {
                 LOG.info(Messages.infoConnectionToAutAgentFailed, ce);
                 System.err.println(Messages.infoNonAutAgentConnectionInfo);
             }
         } else {
             printHelp(null);
         }
 
         return IApplication.EXIT_OK;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void stop() {
         // no-op
     }
 }

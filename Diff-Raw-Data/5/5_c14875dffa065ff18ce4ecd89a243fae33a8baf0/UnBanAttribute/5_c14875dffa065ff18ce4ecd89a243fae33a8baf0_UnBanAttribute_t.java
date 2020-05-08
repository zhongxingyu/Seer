 package org.glite.authz.pap.ui.cli.policymanagement;
 
 import java.rmi.RemoteException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.glite.authz.pap.services.highlevel_policy_management.axis_skeletons.UnbanResult;
 import org.glite.authz.pap.ui.cli.CLIException;
 
 public class UnBanAttribute extends PolicyManagementCLI {
 
     private static String[] COMMAND_NAME_VALUES = { "un-ban", "ub" };
     private static String DESCRIPTION = "Un-ban a previously banned attribute. <id> is any of the attribute ids that can be specified in the "
             + "simplified policy language. By default the attribute is un-bannen for resource and action both with value \"*\". "
             + "Different values for resource and action can be set using options --"
             + OPT_RESOURCE_LONG
             + " and --"
             + OPT_ACTION_LONG + ".";
     private static String USAGE = "[options] <id> <value>";
     private String alias = null;
 
     public UnBanAttribute() {
         super(COMMAND_NAME_VALUES, USAGE, DESCRIPTION, null);
     }
 
     @SuppressWarnings("static-access")
     @Override
     protected Options defineCommandOptions() {
         Options options = new Options();
         options.addOption(OptionBuilder.hasArg(true)
                                        .withDescription(OPT_ACTION_DESCRIPTION)
                                        .withLongOpt(OPT_ACTION_LONG)
                                        .create(OPT_ACTION));
         options.addOption(OptionBuilder.hasArg(true)
                                        .withDescription(OPT_RESOURCE_DESCRIPTION)
                                        .withLongOpt(OPT_RESOURCE_LONG)
                                        .create(OPT_RESOURCE));
         options.addOption(OptionBuilder.hasArg(true)
                                        .withDescription(OPT_PAPALIAS_DESCRIPTION)
                                        .withLongOpt(OPT_PAPALIAS_LONG)
                                        .create(OPT_PAPALIAS));
         return options;
     }
 
     @Override
     protected int executeCommand(CommandLine commandLine) throws CLIException, ParseException, RemoteException {
 
         String[] args = commandLine.getArgs();
 
         if (args.length != 3) {
             throw new ParseException("Wrong number of arguments");
         }
 
         String id = args[1];
         String value = args[2];
         
         if (commandLine.hasOption(OPT_PAPALIAS)) {
             alias = commandLine.getOptionValue(OPT_PAPALIAS);
         }
 
         String resource = null;
         String action = null;
 
         if (commandLine.hasOption(OPT_RESOURCE)) {
             resource = commandLine.getOptionValue(OPT_RESOURCE);
         } else {
            resource = ".*";
         }
 
         if (commandLine.hasOption(OPT_ACTION)) {
             action = commandLine.getOptionValue(OPT_ACTION);
         } else {
            action = ".*";
         }
 
         if (verboseMode) {
             System.out.print("Removing ban... ");
         }
 
         UnbanResult unbanResult;
 
         unbanResult = highlevelPolicyMgmtClient.unban(alias, id, value, resource, action);
 
         if (unbanResult.getStatusCode() != 0) {
 
             System.out.println("ban policy not found.");
             return ExitStatus.FAILURE.ordinal();
 
         } else {
             if (verboseMode) {
                 System.out.println("ok.");
             }
         }
         return ExitStatus.SUCCESS.ordinal();
     }
 }

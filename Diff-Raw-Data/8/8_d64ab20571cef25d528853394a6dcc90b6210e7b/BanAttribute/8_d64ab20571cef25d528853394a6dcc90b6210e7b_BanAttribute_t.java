 package org.glite.authz.pap.ui.cli.policymanagement;
 
 import java.rmi.RemoteException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 public class BanAttribute extends PolicyManagementCLI {
 
     private static String[] COMMAND_NAME_VALUES = { "ban", "b" };
     private static String DESCRIPTION = "Ban an attribute. <id> is any of the attribute ids that can be specified in the "
            + "simplified policy language. By default the attribute is bannen for resource and action both with value \".*\". "
             + "Different values for resource and action can be set using options --" + OPT_RESOURCE_LONG + " and --"
             + OPT_ACTION_LONG + ".";
     private static String USAGE = "<id> <value> [options]";
     private String alias = null;
 
     public BanAttribute() {
         super(COMMAND_NAME_VALUES, USAGE, DESCRIPTION, null);
     }
 
     @SuppressWarnings("static-access")
     @Override
     protected Options defineCommandOptions() {
         Options options = new Options();
         options.addOption(OptionBuilder.hasArg(false)
                                        .withDescription("Set the policy as public (default)")
                                        .withLongOpt(OPT_PUBLIC_LONG)
                                        .create());
         options.addOption(OptionBuilder.hasArg(false)
                                        .withDescription("Set the policy as private (it won't be distributed)")
                                        .withLongOpt(OPT_PRIVATE_LONG)
                                        .create());
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
     protected int executeCommand(CommandLine commandLine) throws ParseException, RemoteException {
 
         String[] args = commandLine.getArgs();
 
         if (args.length != 3) {
             throw new ParseException("Wrong number of arguments");
         }
 
         String id = args[1];
         String value = args[2];
         
         if (commandLine.hasOption(OPT_PAPALIAS)) {
             alias = commandLine.getOptionValue(OPT_PAPALIAS);
         }
 
         boolean isPublic = true;
         if (commandLine.hasOption(OPT_PRIVATE_LONG)) {
             isPublic = false;
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
             System.out.print("Adding deny rule... ");
         }
 
         String policyId = null;
 
         policyId = highlevelPolicyMgmtClient.ban(alias, id, value, resource, action, isPublic);
 
         if (policyId == null) {
             printOutputMessage(String.format("error (ban rule already exists)."));
         } else {
             if (verboseMode) {
                 System.out.println("ok");
             }
         }
         return ExitStatus.SUCCESS.ordinal();
     }
 }

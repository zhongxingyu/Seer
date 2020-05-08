 package com.danieltwc.aws.glacier;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.AWSCredentialsProvider;
 import com.amazonaws.auth.AWSCredentialsProviderChain;
 import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
 import com.amazonaws.services.glacier.AmazonGlacierClient;
 
 import com.danieltwc.aws.HomeDirectoryAWSCredentialsProvider;
 import com.danieltwc.aws.glacier.commands.GlacierCommand;
 import com.danieltwc.aws.glacier.commands.impl.*;
 
 public class App {
     // Commands that operate on a vault, e.g. "inventory"
     private static final List<String> VAULT_COMMANDS = new ArrayList<String>();
 
     private static final String LIST_COMMAND = "list";
     private static final String INVENTORY_COMMAND = "inventory";
     private static final String UPLOAD_COMMAND = "upload";
     private static final String DOWNLOAD_COMMAND = "download";
     private static final String DELETE_COMMAND = "delete";
     private static final String SAVE_JOB_OUTPUT_COMMAND = "save-job-output";
 
     static AWSCredentials credentials;
     static AmazonGlacierClient client;
 
     public static void main(String[] rawArgs) {
         VAULT_COMMANDS.add(INVENTORY_COMMAND);
         VAULT_COMMANDS.add(UPLOAD_COMMAND);
         VAULT_COMMANDS.add(DOWNLOAD_COMMAND);
         VAULT_COMMANDS.add(DELETE_COMMAND);
         VAULT_COMMANDS.add(SAVE_JOB_OUTPUT_COMMAND);
 
         List<String> args = new ArrayList<String>(Arrays.asList(rawArgs));
 
         loadCredentials();
         loadClient();
         runCommand(args);
     }
 
     public static void loadCredentials() {
         AWSCredentialsProvider provider = new AWSCredentialsProviderChain(
             new HomeDirectoryAWSCredentialsProvider(),
             new DefaultAWSCredentialsProviderChain()
         );
 
         credentials = provider.getCredentials();
     }
 
     public static void loadClient() {
         client = new AmazonGlacierClient(credentials);
     }
 
     public static void runCommand(List<String> args) {
         // The list command does not need a vault
         if (args.size() < 1) {
             System.err.println("App <command> [...]");
             System.exit(1);
         }
 
         String command = args.remove(0).toLowerCase();
         String vaultName = null;
 
         if (VAULT_COMMANDS.contains(command)) {
            if (args.size() < 1) {
                System.err.println("App <command> <vault> [...]");
                System.exit(1);
            }

             vaultName = args.remove(0);
         }
 
         try {
             GlacierCommand cmd = new UnknownCommand();
 
             if (command.equals(LIST_COMMAND)) {
                 cmd = new ListCommand();
             }
             else if (command.equals(INVENTORY_COMMAND)) {
                 cmd = new InventoryCommand();
             }
             else if (command.equals(UPLOAD_COMMAND)) {
                 cmd = new UploadCommand();
             }
             else if (command.equals(DOWNLOAD_COMMAND)) {
                 cmd = new DownloadCommand();
             }
             else if (command.equals(DELETE_COMMAND)) {
                 cmd = new DeleteCommand();
             }
             else if (command.equals(SAVE_JOB_OUTPUT_COMMAND)) {
                 cmd = new SaveJobOutputCommand();
             }
 
             cmd.setOut(System.out);
             cmd.setArgs(args);
             cmd.setClient(client);
             cmd.setCredentials(credentials);
             cmd.setVaultName(vaultName);
 
             cmd.run();
         }
         catch (IllegalArgumentException e) {
             String msg = "App " + command + " ";
 
             if (VAULT_COMMANDS.contains(command)) {
                 msg += "<vault> ";
             }
 
             msg += e.getMessage();
 
             System.err.println(msg);
             System.exit(1);
         }
         catch (Exception e) {
             System.err.println("Command [" + command + "] failed:");
             System.err.println(e);
             System.exit(1);
         }
     }
 }

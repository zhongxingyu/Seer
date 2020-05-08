 /*
  * Copyright 2009 LugIron Software, Inc. All Rights Reserved.
  *
  * $Id$
  */
 
 package com.trustwave.transaction;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.HelpFormatter;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class Runner implements AppConstants {
 
     public static void main(final String... args) {
        final Logger logger = LoggerFactory.getLogger(Runner.class);
 
         final CommandLineParser parser = new PosixParser();
 
         try {
             final CommandLine cmd = parser.parse(createOptions(), args);
 
             final boolean reqOptions = cmd.hasOption("T") && cmd.hasOption("X") && cmd.hasOption("N");
 
             if (cmd.hasOption("help") || !reqOptions) {
                 final HelpFormatter formatter = new HelpFormatter();
                 formatter.printHelp("transaction", createOptions());
                 return;
             }
 
             if (cmd.hasOption("V")) {
                 System.setProperty(VERBOSE, VERBOSE_ON);
             }
 
             final int accountCount = Integer.parseInt(cmd.getOptionValue("N"));
             final int threadCount = Integer.parseInt(cmd.getOptionValue("T"));
             final int transferAmount = Integer.parseInt(cmd.getOptionValue("X"));
 
             if (accountCount < 2 && threadCount < 1) {
                 throw new Exception();
             }
 
             final AccountMonitor accountMgr = new AccountMonitor(createAccounts(accountCount), threadCount);
 
             final TransactionManager transactionMgr =
                     new TransactionManager(accountMgr);
 
             transactionMgr.simulate(threadCount, transferAmount);
         }
         catch (Exception e) {
             final HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("transaction", createOptions());
         }
     }
 
     private static List<Account> createAccounts(final int size) {
         final List<Account> accounts = new ArrayList<Account>();
         for (int x = 0; x < size; ++x) {
             accounts.add(new Account(x, DEFAULT_BALANCE));
         }
 
         return accounts;
     }
 
     private static Options createOptions() {
 
         final Options options = new Options();
 
         options.addOption("help", false, "Print this message");
 
         options.addOption("V", "verbose", false, "Provides more indepth debug information");
 
         options.addOption("T", "threads", true, "Number of simultaneous threads to start e.g. t > 0");
 
         options.addOption("X", "transfer", true, "Number of dollars for each transfer");
 
         options.addOption("N", "accounts", true, "Number of user accounts e.g. x > 1");
 
         return options;
     }
 }

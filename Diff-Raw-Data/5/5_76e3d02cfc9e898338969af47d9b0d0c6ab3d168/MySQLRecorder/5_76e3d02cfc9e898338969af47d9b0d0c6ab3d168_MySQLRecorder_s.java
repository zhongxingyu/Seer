 /**
  * Copyright (c) 2008-2010 Wave2 Limited. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * Neither the name of Wave2 Limited nor the names of its contributors may be
  * used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.binarystor.tools.dbrecorder.mysql;
 
 import java.io.ByteArrayOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.StringWriter;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.sql.*;
 import java.util.*;
 
 import org.binarystor.tools.dbrecorder.dbRecorder;
 import org.binarystor.tools.dbrecorder.SSHTunnel;
 import org.binarystor.mysql.MySQLDump;
 
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNNodeKind;
 import org.tmatesoft.svn.core.SVNProperties;
 import org.tmatesoft.svn.core.io.ISVNEditor;
 import org.tmatesoft.svn.core.io.SVNRepository;
 
 import com.trilead.ssh2.Connection;
 import com.trilead.ssh2.LocalPortForwarder;
 
 /**
  *
  * @author Alan Snelson
  */
 public class MySQLRecorder {
 
     private enum mysqlObjects { EVENTS, ROUTINES, TABLES, TRIGGERS, SCHEMATA, VIEWS };
     private MySQLInstance[] instances;
     private boolean verbose = false;
     private Map<String, SVNRepository[]> svnRepositories;
     private String notifyMessage = "";
 
     public MySQLRecorder(Map<String, SVNRepository[]> svnRepositories, MySQLInstance[] instances) {
         //TODO check this
         this.svnRepositories = svnRepositories;
         this.instances = instances;
         this.verbose = dbRecorder.verbose;
     }
 
     public String record() {
 
         try {
 
             for (MySQLInstance host : instances) {
                 //Host identifier
                 String mysqlName =  host.getName();
                 if (mysqlName == null) {
                     mysqlName = InetAddress.getByName(host.getHostname()).getHostName();
                 }
                 if (verbose) {
                     System.out.println("-- Processing MySQL instance " + mysqlName + " ---");
                 }
                 //Get the repository used to store the MySQL changes
                 SVNRepository writeRepository = svnRepositories.get(host.getRepository())[0];
                 SVNRepository readRepository = svnRepositories.get(host.getRepository())[1];
                 ISVNEditor editor = writeRepository.getCommitEditor("dbRecorder Update", null);
                 editor.openRoot(-1);
                 //For each MySQL host defined in the config file store in VCS
                 SVNNodeKind nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName, -1);
                 if (nodeKind == SVNNodeKind.NONE) {
                     dbRecorder.addDir(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName);
                 }
                 //Check to see if we need an SSH Tunnel
                 SSHTunnel secureShell = host.getSecuretunnel();
                 Connection conn = null;
                 LocalPortForwarder lpf1 = null;
                 if (secureShell != null) {
                     if (verbose) {
                         System.out.println("Creating SSH Tunnel as -L " + secureShell.getLocalport() + ":" + secureShell.getRemotehost() + ":" + secureShell.getRemoteport() + " " + secureShell.getUsername() + "@" + secureShell.getHostname());
                     }
                     conn = new Connection(secureShell.getHostname());
                     conn.connect();
                     try {
                         //Try Public Key Authentication first
                         if (secureShell.getKeyfile() != null) {
                             conn.authenticateWithPublicKey(secureShell.getUsername(), new File(secureShell.getKeyfile()), secureShell.getKeypassword());
                         } else {
                             conn.authenticateWithPassword(secureShell.getUsername(), secureShell.getPassword());
                         }
                     } catch (IOException ioe) {
                         if (ioe.getMessage().equals("Password authentication failed.")) {
                             System.out.println("Error: Password authentication failed.\nMany default SSH server installations are configured to refuse the authentication type 'password'. Often, they only accept 'publickey' and 'keyboard-interactive'.");
                         }
                         System.out.println(ioe.getMessage());
                     }
                     lpf1 = conn.createLocalPortForwarder(secureShell.getLocalport(), secureShell.getRemotehost(), secureShell.getRemoteport());
                 }
 
                 int mysqlPort = host.getPort();
                 if (mysqlPort == 0) {
                     mysqlPort = 3306;
                 }
 
                 //MySQLDump object will be used to obtain the DDL for the Schemata
                 MySQLDump dumper = new MySQLDump();
                 //Connect to Database
                 dumper.connect(host.getHostname(), mysqlPort, host.getUsername(), host.getPassword(), "mysql");
 

                 //Save global variables
                 String currentVariables = "";
                 Map<String, String> globalVariables = dumper.dumpGlobalVariables();
                 //Remove timestamp variable as this changes with every request
                 globalVariables.remove("timestamp");
                 for (Map.Entry<String, String> variable : globalVariables.entrySet()) {
                     currentVariables += variable.getKey() + " : " + variable.getValue() + "\n";
                 }
                 nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/GlobalVariables", -1);
                 if (nodeKind == SVNNodeKind.NONE) {
                     dbRecorder.addFile(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/GlobalVariables", currentVariables.getBytes());
                 } else {
                     //Save Diff for notify
                     SVNProperties fileProperties = new SVNProperties();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     readRepository.getFile("dbRecorder/MySQL/" + mysqlName + "/GlobalVariables", -1, fileProperties, baos);
                     String reposVariables = baos.toString();
                     if (!currentVariables.equals(reposVariables)) {
                         notifyMessage += "--- Variables Modified on " + mysqlName + " ---\n\n" + dbRecorder.printDiffs(currentVariables.split("\n"), reposVariables.split("\n")) + "\n\n";
                         //Update VCS Repository
                         dbRecorder.modifyFile(writeRepository, editor, "dbRecorder/MySQL/" + mysqlName, "dbRecorder/MySQL/" + mysqlName + "/GlobalVariables", reposVariables.getBytes(), currentVariables.getBytes(), "Global Variable Updated");
                     }
                 }
 
                 //Save grant tables
                 dbRecorder.addDir(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/GrantTables");
                 List<String> grantTables = dumper.listGrantTables();
                 for (String grantTable : grantTables) {
                     if (verbose) {
                         System.out.println("Dumping Grant Table " + grantTable);
                     }
                     StringWriter currentTable = new StringWriter();
                     BufferedWriter out = new BufferedWriter(currentTable);
                     dumper.dumpTable(out, grantTable);
                     out.flush();
                     out.close();
                     nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/GrantTables/" + grantTable, -1);
                     if (nodeKind == SVNNodeKind.NONE) {
                         if (!currentTable.toString().equals("")) {
                             notifyMessage += "--- Grant Table " + grantTable + " Created on " + mysqlName + " ---\n" + currentTable.toString() + "\n";
                             dbRecorder.addFile(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/GrantTables/" + grantTable, currentTable.toString().getBytes());
                         }
                     } else {
                         //Save Diff for notify
                         SVNProperties fileProperties = new SVNProperties();
                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         readRepository.getFile("dbRecorder/MySQL/" + mysqlName + "/GrantTables/" + grantTable, -1, fileProperties, baos);
                         String reposTable = baos.toString();
                         if (!currentTable.toString().equals(reposTable)) {
                             notifyMessage += "--- Grant Table " + grantTable + " Modified on " + mysqlName + " ---\n\n" + dbRecorder.printDiffs(currentTable.toString().split("\n"), reposTable.split("\n")) + "\n\n";
                             //Update VCS Repository
                             dbRecorder.modifyFile(writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/GrantTables", "dbRecorder/MySQL/" + mysqlName + "/GrantTables/" + grantTable, reposTable.getBytes(), currentTable.toString().getBytes(), "Grant Table Modified");
                         }
                     }
                 }
 
                 //Check for Schemata if none supplied get all
                 ArrayList<String> schemalist = new ArrayList<String>();
                 MySQLSchemata[] Schemata = host.getSchemata();
                 if (Schemata == null) {
                     schemalist = dumper.listSchemata();
                     Schemata = new MySQLSchemata[schemalist.size()];
                     int i = 0;
                     for (String schema : schemalist) {
                         Schemata[i++] = new MySQLSchemata(schema);
                     }
                 } else {
                     for (MySQLSchemata schema : Schemata) {
                         schemalist.add(schema.getName());
                     }
                 }
 
                //Create Schemata folder if not present
                nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/", -1);
                if (nodeKind == SVNNodeKind.NONE) {
                    dbRecorder.addDir(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/");
                }
 
                 processOjectList(mysqlObjects.SCHEMATA, mysqlName, null, schemalist, editor, readRepository, writeRepository);              
                 for (MySQLSchemata schema : Schemata) {
                     if (verbose) {
                         System.out.println("Dumping Schema " + (String) schema.getName());
                     }
 
                     //Create Schema folder if not present
                     nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName(), -1);
                     if (nodeKind == SVNNodeKind.NONE) {
                         dbRecorder.addDir(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName());
                     }
 
                     dumper.setSchema((String) schema.getName());
                     String currentSchema = dumper.dumpCreateDatabase((String) schema.getName());
                     nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/" + schema.getName(), -1);
                     if (nodeKind == SVNNodeKind.NONE) {
                         notifyMessage += "--- Schema `" + schema.getName() + "` Created on " + mysqlName + " ---\n\n" + currentSchema + "\n\n";
                         dbRecorder.addFile(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/" + schema.getName(), currentSchema.getBytes());
                     } else {
                         //Save Diff for notify
                         SVNProperties fileProperties = new SVNProperties();
                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         readRepository.getFile("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/" + schema.getName(), -1, fileProperties, baos);
                         String reposSchema = baos.toString();
                         if (!currentSchema.equals(reposSchema)) {
                             notifyMessage += "--- Schema `" + schema.getName() + "` Modified on " + mysqlName + " ---\n\n" + dbRecorder.printDiffs(currentSchema.split("\n"), reposSchema.split("\n")) + "\n\n";
                             //Update VCS Repository
                             dbRecorder.modifyFile(writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName(), "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/" + schema.getName(), reposSchema.getBytes(), currentSchema.getBytes(), "Schema Updated");
                         // System.out.print(baos.toString());
                         }
                     }
 
                     
                     //Check for events if none supplied get all
                     ArrayList<String> events = new ArrayList<String>();
                     if (schema.getEvents() == null) {
                         events = dumper.listEvents(schema.getName());
                     } else {
                         for (String event : schema.getEvents()){
                             events.add(event);
                         }
                     }
 
                     //Create Events folder if not present
                     nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Events", -1);
                     if (nodeKind == SVNNodeKind.NONE) {
                         dbRecorder.addDir(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Events");
                     }
 
                     processOjectList(mysqlObjects.EVENTS, mysqlName, schema.getName(), events, editor, readRepository, writeRepository);
                     for (String event : events) {
                         if (verbose) {
                             System.out.println("Dumping Event " + event);
                         }
                         String currentEvent = dumper.dumpCreateEvent(event);
                         if (!currentEvent.equals("")) {
                             nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Events/" + event, -1);
                             if (nodeKind == SVNNodeKind.NONE) {
                                 notifyMessage += "--- Event " + schema.getName() + "." + event + " Created on " + mysqlName + " ---\n\n" + currentEvent;
                                 dbRecorder.addFile(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Events/" + event, currentEvent.getBytes());
                             } else {
                                 //Save Diff for notify
                                 SVNProperties fileProperties = new SVNProperties();
                                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                 readRepository.getFile("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Events/" + event, -1, fileProperties, baos);
                                 String reposEvent = baos.toString();
                                 if (!currentEvent.equals(reposEvent)) {
                                     notifyMessage += "--- Event " + schema.getName() + "." + event + " Modified on " + mysqlName + " ---\n\n" + dbRecorder.printDiffs(currentEvent.split("\n"), reposEvent.split("\n")) + "\n\n";
                                     //Update VCS Repository
                                     dbRecorder.modifyFile(writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Events", "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Events/" + event, reposEvent.getBytes(), currentEvent.getBytes(), "Event Updated");
                                 // System.out.print(baos.toString());
                                 }
                             }
                         }
                     }
 
                     //Check for routines if none supplied get all
                     ArrayList<String> routines = new ArrayList<String>();
                     if (schema.getRoutines() == null) {
                         routines = dumper.listRoutines(schema.getName());
                     } else {
                         for (String routine : schema.getRoutines()){
                             routines.add(routine);
                         }
                     }
 
                     //Create Routines folder if not present
                     nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "Routines", -1);
                     if (nodeKind == SVNNodeKind.NONE) {
                         dbRecorder.addDir(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Routines");
                     }
 
                     processOjectList(mysqlObjects.ROUTINES, mysqlName, schema.getName(), routines, editor, readRepository, writeRepository);
                     for (String routine : routines) {
                         if (verbose) {
                             System.out.println("Dumping Routine " + routine);
                         }
                         String currentRoutine = dumper.dumpCreateRoutine(routine);
                         if (!currentRoutine.equals("")) {
                             nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Routines/" + routine, -1);
                             if (nodeKind == SVNNodeKind.NONE) {
                                 notifyMessage += "--- Routine " + schema.getName() + "." + routine + " Created on " + mysqlName + " ---\n" + currentRoutine + "\n";
                                 dbRecorder.addFile(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Routines/" + routine, currentRoutine.getBytes());
                             } else {
                                 //Save Diff for notify
                                 SVNProperties fileProperties = new SVNProperties();
                                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                 readRepository.getFile("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Routines/" + routine, -1, fileProperties, baos);
                                 String reposRoutine = baos.toString();
                                 if (!currentRoutine.equals(reposRoutine)) {
                                     notifyMessage += "--- Routine " + schema.getName() + "." + routine + " Modified on " + mysqlName + " ---\n" + dbRecorder.printDiffs(currentRoutine.split("\n"), reposRoutine.split("\n")) + "\n\n";
                                     //Update VCS Repository
                                     dbRecorder.modifyFile(writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Routines", "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Routines/" + routine, reposRoutine.getBytes(), currentRoutine.getBytes(), "Routine Updated");
                                 // System.out.print(baos.toString());
                                 }
                             }
                         }
                     }
 
                     //Check for tables if none supplied get all
                     ArrayList<String> tables = new ArrayList<String>();
                     if (schema.getTables() == null) {
                         tables = dumper.listTables(schema.getName());
                     } else {
                         for (String table : schema.getTables()){
                             tables.add(table);
                         }
                     }
 
                     //Create Tables folder if not present
                     nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "Tables", -1);
                     if (nodeKind == SVNNodeKind.NONE) {
                         dbRecorder.addDir(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Tables");
                     }
 
                     processOjectList(mysqlObjects.TABLES, mysqlName, schema.getName(), tables, editor, readRepository, writeRepository);
                     for (String table : tables) {
                         if (verbose) {
                             System.out.println("Dumping Table " + table);
                         }
 
                         //Strip AUTO_INCREMENT by default and only include tables specified using AUTO_INC yaml directive
                         List<String> autoinc = new ArrayList<String>();
                         if (schema.getAuto_increment() != null) {
                             for (String auto_inc: schema.getAuto_increment()){
                                 autoinc.add(auto_inc);
                             }
                         }
                         String currentTable = "";
                         if (autoinc.contains(table)) {
                             currentTable = dumper.dumpCreateTable(table);
                         } else {
                             currentTable = dumper.dumpCreateTable(table).replaceAll("AUTO_INCREMENT=\\d++", "");
                         }
                         if (!currentTable.equals("")) {
                             nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Tables/" + table, -1);
                             if (nodeKind == SVNNodeKind.NONE) {
                                 notifyMessage += "--- Table " + schema.getName() + "." + table + " Created on " + mysqlName + " ---\n" + currentTable + "\n\n";
                                 dbRecorder.addFile(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Tables/" + table, currentTable.getBytes());
                             } else {
                                 //Save Diff for notify
                                 SVNProperties fileProperties = new SVNProperties();
                                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                 readRepository.getFile("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Tables/" + table, -1, fileProperties, baos);
                                 String reposTable = baos.toString();
                                 if (!currentTable.equals(reposTable)) {
                                     notifyMessage += "--- Table " + schema.getName() + "." + table + " Modified on " + mysqlName + " ---\n" + dbRecorder.printDiffs(currentTable.split("\n"), reposTable.split("\n")) + "\n\n";
                                     //Update VCS Repository
                                     dbRecorder.modifyFile(writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Tables", "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Tables/" + table, reposTable.getBytes(), currentTable.getBytes(), "Table Updated");
                                 // System.out.print(baos.toString());
                                 }
                             }
                         }
                     }
                     
                     //Check for Triggers if none supplied get all
                     ArrayList<String> triggers = new ArrayList<String>();
                     if (schema.getTriggers() == null) {
                         triggers = dumper.listTriggers(schema.getName());
                     } else {
                         for (String trigger : schema.getTriggers()){
                             triggers.add(trigger);
                         }
                     }
 
                     //Create Triggers folder if not present
                     nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "Triggers", -1);
                     if (nodeKind == SVNNodeKind.NONE) {
                         dbRecorder.addDir(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Triggers");
                     }
 
                     processOjectList(mysqlObjects.TRIGGERS, mysqlName, schema.getName(), triggers, editor, readRepository, writeRepository);
                     for (String trigger : triggers) {
                         nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Triggers/" + trigger, -1);
                         if (nodeKind == SVNNodeKind.NONE) {
                             dbRecorder.addFile(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Triggers/" + trigger, dumper.dumpCreateTrigger(trigger).getBytes());
                         } else {
                             //Save Diff for notify
                             String currentTrigger = dumper.dumpCreateTrigger(trigger);
                             SVNProperties fileProperties = new SVNProperties();
                             ByteArrayOutputStream baos = new ByteArrayOutputStream();
                             readRepository.getFile("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Triggers/" + trigger, -1, fileProperties, baos);
                             String reposTrigger = baos.toString();
                             if (!currentTrigger.equals(reposTrigger)) {
                                 notifyMessage += "--- Trigger " + schema.getName() + "." + trigger + " ---\n\n" + dbRecorder.printDiffs(currentTrigger.split("\n"), reposTrigger.split("\n")) + "\n\n";
                                 //Update VCS Repository
                                 dbRecorder.modifyFile(writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Triggers", "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Triggers/" + trigger, reposTrigger.getBytes(), currentTrigger.getBytes(), "Trigger Updated");
                             // System.out.print(baos.toString());
                             }
                         }
                     }
 
                     //Check for views if none supplied get all
                     ArrayList<String> views = new ArrayList<String>();
                     if (schema.getViews() == null) {
                         views = dumper.listViews(schema.getName());
                     } else {
                         for (String view : schema.getViews()){
                             views.add(view);
                         }
                     }
 
                     //Create Views folder if not present
                     nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "Views", -1);
                     if (nodeKind == SVNNodeKind.NONE) {
                         dbRecorder.addDir(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Views");
                     }
 
                     processOjectList(mysqlObjects.VIEWS, mysqlName, schema.getName(), views, editor, readRepository, writeRepository);
                     for (String view : views) {
                         if (verbose) {
                             System.out.println("Dumping View " + view);
                         }
 
                         String currentView = "";
                         currentView = dumper.dumpCreateView(view);
                         if (!currentView.equals("")) {
                             nodeKind = readRepository.checkPath("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Views/" + view, -1);
                             if (nodeKind == SVNNodeKind.NONE) {
                                 notifyMessage += "--- View " + schema.getName() + "." + view + " Created on " + mysqlName + " ---\n" + currentView + "\n\n";
                                 dbRecorder.addFile(readRepository, writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Views/" + view, currentView.getBytes());
                             } else {
                                 //Save Diff for notify
                                 SVNProperties fileProperties = new SVNProperties();
                                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                 readRepository.getFile("dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Views/" + view, -1, fileProperties, baos);
                                 String reposView = baos.toString();
                                 if (!currentView.equals(reposView)) {
                                     notifyMessage += "--- View " + schema.getName() + "." + view + " Modified on " + mysqlName + " ---\n" + dbRecorder.printDiffs(currentView.split("\n"), reposView.split("\n")) + "\n\n";
                                     //Update VCS Repository
                                     dbRecorder.modifyFile(writeRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Views", "dbRecorder/MySQL/" + mysqlName + "/Schemata/" + schema.getName() + "/Views/" + view, reposView.getBytes(), currentView.getBytes(), "View Updated");
                                 }
                             }
                         }
                     }
                 }
                 //Close SSH Tunnel if using SSH
                 if (secureShell != null) {
                     if (verbose) {
                         System.out.println("Closing SSH Tunnel to host " + secureShell.getHostname());
                     }
                     lpf1.close();
                     conn.close();
                 }
                 editor.closeEdit();
             }
         } catch (IOException ioe) {
             System.out.println(ioe.getMessage());
         } catch (SVNException svne) {
             System.out.println(svne.getErrorMessage());
         } catch (SQLException sqle) {
             System.out.println(sqle.getMessage());
         }
 
         return notifyMessage;
     }
 
     private void processOjectList(mysqlObjects mysqlObj, String mysqlName, String schema, ArrayList<String> objects, ISVNEditor editor, SVNRepository readRepository, SVNRepository writeRepository){
         //Capitalise first character
         String objectType = Character.toUpperCase(mysqlObj.toString().toLowerCase().charAt(0))+mysqlObj.toString().toLowerCase().substring(1);
         String objectPath = "";
         if (schema == null){
             objectPath = "dbRecorder/MySQL/" + mysqlName + "/" +  objectType + "/" + objectType.toLowerCase();
         } else {
             objectPath = "dbRecorder/MySQL/" + mysqlName + "/Schemata/" +  schema + "/" + objectType + "/" + objectType.toLowerCase();
         }
         try{
             //Check for differences in object list
             String objectList = new String();
             for (String object : objects) {
                 if (schema == null){
                     objectList += "`" + object + "`\n";
                 } else {
                     objectList += "`" + schema + "`.`" + object + "`\n";
                 }
             }
             SVNNodeKind nodeKind = readRepository.checkPath(objectPath, -1);
             if (nodeKind == SVNNodeKind.NONE) {
                 dbRecorder.addFile(readRepository, writeRepository, editor, objectPath, objectList.getBytes());
             } else {
                 SVNProperties fileProperties = new SVNProperties();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 readRepository.getFile(objectPath, -1, fileProperties, baos);
                 String reposObjectList = baos.toString();
                 //Anything changed?
                 if (!objectList.equals(reposObjectList)) {
                     ArrayList<String> dropped = new ArrayList<String>();
                     for (String object: reposObjectList.split("\n")){
                         if (!object.isEmpty() && !objects.contains(object.replace("`", ""))){
                             dropped.add(object.substring(1,object.length() - 1));
                         }
                     }
                     if (!dropped.isEmpty()){
                         //Remove dropped objects
                         for (String object: dropped){
                             if (schema == null){
                                 dbRecorder.removeDir(readRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/" +  objectType + "/" + object);
                             } else {
                                 dbRecorder.removeFile(readRepository, editor, "dbRecorder/MySQL/" + mysqlName + "/Schemata/" +  schema + "/" + objectType + "/" + object);
                             }
                         }
                     }
                     notifyMessage += "--- " + objectType + " list modified on " + mysqlName + " ---\n\n" + dbRecorder.printDiffs(objectList.split("\n"), reposObjectList.split("\n")) + "\n\n";
                     //Update VCS Repository
                     dbRecorder.modifyFile(writeRepository, editor, objectPath, objectPath, reposObjectList.getBytes(), objectList.getBytes(), objectType + " List Updated");
                 }
             }
           } catch (SVNException svne) {
             System.out.println(svne.getErrorMessage());
             }
 
     }
 }

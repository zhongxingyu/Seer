 /*
 * [The "New BSD" license]
 * Copyright (c) 2012 The Board of Trustees of The University of Alabama
 * All rights reserved.
 *
 * See LICENSE for details.
 */
 
 package edu.ua.eng.software.clonelink;
 
 import org.netbeans.lib.cvsclient.CVSRoot;
 import org.netbeans.lib.cvsclient.Client;
 import org.netbeans.lib.cvsclient.connection.PServerConnection;
 import org.netbeans.lib.cvsclient.command.log.LogCommand;
 import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
 import org.netbeans.lib.cvsclient.commandLine.BasicListener;
 import org.netbeans.lib.cvsclient.command.GlobalOptions;
 import java.io.File;
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 /**
 * @author Casey M. Ferris <cmferris1@crimson.ua.edu> 
 */
 
 public class CvsClient {
     
     public CvsClient() throws Exception {
         GlobalOptions globalOptions = new GlobalOptions();
         globalOptions.setCVSRoot(ROOT);
 
         Client client = connect();
 
         LogCommand command = new LogCommand();
 
         client.executeCommand(command, globalOptions);
 
        System.out.println(log.toString().length());
     }
 
     private Client connect() throws Exception {
         CVSRoot rootData = CVSRoot.parse(ROOT);
         PServerConnection c = new PServerConnection(rootData);
         c.open();
 
         Client client = new Client(c, new StandardAdminHandler());
         client.setLocalPath(CVS_PATH);
         client.getEventManager().addCVSListener(new BasicListener(new PrintStream(log), new PrintStream(err)));
 
         return client;
     }
 
     private final String ROOT = ":pserver:anonymous@jhotdraw.cvs.sourceforge.net:/cvsroot/jhotdraw";
     private final String CVS_PATH = "/home/nummer/Desktop/jhotdrawcvs/JHotDraw/";
     private final ByteArrayOutputStream log = new ByteArrayOutputStream();
     private final ByteArrayOutputStream err = new ByteArrayOutputStream();
 }

 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.geotools.cli;
 
 import org.geogit.cli.CLICommandExtension;
 import org.geogit.geotools.porcelain.ShpImport;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameters;
 
 /**
  * {@link CLICommandExtension} that provides a {@link JCommander} for shapefile specific commands.
  * <p>
  * Usage:
  * <ul>
 * <li> {@code geogit shp <command> <args>...]}
  * </ul>
  * 
  * @author jgarrett
  * @see ShpImport
  */
 @Parameters(commandNames = "shp", commandDescription = "GeoGit/Shapefile integration utilities")
 public class ShpCommandProxy implements CLICommandExtension {
 
     /**
      * @return the JCommander parser for this extension
      * @see JCommander
      */
     @Override
     public JCommander getCommandParser() {
         JCommander commander = new JCommander();
         commander.setProgramName("geogit shp");
         commander.addCommand("import", new ShpImport());
         return commander;
     }
 }

 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 
 package org.geogit.geotools.porcelain;
 
 import java.util.Arrays;
 
 import jline.UnsupportedTerminal;
 import jline.console.ConsoleReader;
 
 import org.geogit.api.porcelain.CommitOp;
 import org.geogit.cli.CommandFailedException;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.test.integration.RepositoryTestCase;
 import org.geotools.data.AbstractDataStoreFactory;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 /**
  *
  */
 public class SLExportTest extends RepositoryTestCase {
 
     @Rule
     public ExpectedException exception = ExpectedException.none();
 
     private GeogitCLI cli;
 
     private static AbstractDataStoreFactory factory;
 
     @BeforeClass
     public static void oneTimeSetup() throws Exception {
         factory = TestHelper.createTestFactory();
     }
 
     @Override
     public void setUpInternal() throws Exception {
         ConsoleReader consoleReader = new ConsoleReader(System.in, System.out,
                 new UnsupportedTerminal());
         cli = new GeogitCLI(consoleReader);
 
         cli.setGeogit(geogit);
 
         // Add points
         insertAndAdd(points1);
         insertAndAdd(points2);
         insertAndAdd(points3);
 
         geogit.command(CommitOp.class).call();
 
         // Add lines
         insertAndAdd(lines1);
         insertAndAdd(lines2);
         insertAndAdd(lines3);
 
         geogit.command(CommitOp.class).call();
     }
 
     @Override
     public void tearDownInternal() throws Exception {
         cli.close();
     }
 
     @Test
     public void testExport() throws Exception {
 
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList("Points", "Points");
         exportCommand.dataStoreFactory = factory;
         exportCommand.run(cli);
     }
 
     @Test
     public void testNullDataStore() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList("Points", "Points");
         exportCommand.dataStoreFactory = TestHelper.createNullTestFactory();
         exception.expect(CommandFailedException.class);
         exportCommand.run(cli);
     }
 
     @Test
     public void testNoArgs() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList();
         exportCommand.dataStoreFactory = TestHelper.createNullTestFactory();
         exception.expect(CommandFailedException.class);
         exportCommand.run(cli);
     }
 
     @Test
     public void testExportToTableThatExists() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList("Points", "table1");
         exportCommand.dataStoreFactory = factory;
         exception.expect(CommandFailedException.class);
         exportCommand.run(cli);
     }
 
     @Test
     public void testExportToTableThatExistsWithOverwrite() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList("WORK_HEAD:Points", "testTable");
         exportCommand.dataStoreFactory = factory;
         exportCommand.run(cli);
 
         exportCommand.args = Arrays.asList("Lines", "testTable");
         exportCommand.overwrite = true;
         exportCommand.run(cli);
     }
 
     @Test
     public void testExportWithNonexistentFeatureTypeTree() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList("invalidType", "invalidTable");
         exportCommand.dataStoreFactory = factory;
         exception.expect(IllegalArgumentException.class);
         exportCommand.run(cli);
     }
 
     @Test
     public void testExportWithNullTable() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList("Points", null);
         exportCommand.dataStoreFactory = factory;
         exception.expect(IllegalArgumentException.class);
         exportCommand.run(cli);
     }
 
     @Test
     public void testExportWithNullFeatureType() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList(null, "invalidTable");
         exportCommand.dataStoreFactory = factory;
        exception.expect(CommandFailedException.class);
         exportCommand.run(cli);
     }
 
     @Test
     public void testExportWithEmptyStringForFeatureType() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList("", "invalidTable");
         exportCommand.dataStoreFactory = factory;
         exception.expect(IllegalArgumentException.class);
         exportCommand.run(cli);
     }
 
     @Test
     public void testExportWithEmptyStringForTable() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList("Points", "");
         exportCommand.dataStoreFactory = factory;
         exception.expect(IllegalArgumentException.class);
         exportCommand.run(cli);
     }
 
     @Test
     public void testExportWithFeatureNameInsteadOfType() throws Exception {
         SLExport exportCommand = new SLExport();
         exportCommand.args = Arrays.asList("Points/Points.1", "invalidTable");
         exportCommand.dataStoreFactory = factory;
         exception.expect(IllegalArgumentException.class);
         exportCommand.run(cli);
     }
 }

 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.cli.test.functional;
 
 import static org.geogit.cli.test.functional.GlobalState.currentDirectory;
 import static org.geogit.cli.test.functional.GlobalState.geogit;
 import static org.geogit.cli.test.functional.GlobalState.geogitCLI;
 import static org.geogit.cli.test.functional.GlobalState.homeDirectory;
 import static org.geogit.cli.test.functional.GlobalState.stdIn;
 import static org.geogit.cli.test.functional.GlobalState.stdOut;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.List;
 
 import jline.UnsupportedTerminal;
 import jline.console.ConsoleReader;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.GlobalInjectorBuilder;
 import org.geogit.api.Node;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Platform;
 import org.geogit.api.porcelain.AddOp;
 import org.geogit.cli.GeogitCLI;
 import org.geogit.repository.WorkingTree;
 import org.geotools.data.DataUtilities;
 import org.geotools.feature.NameImpl;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.geometry.jts.WKTReader2;
 import org.geotools.util.logging.Logging;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.GeometryDescriptor;
 import org.opengis.feature.type.Name;
 
 import com.google.common.io.ByteStreams;
 import com.google.common.io.CharStreams;
 import com.google.common.io.InputSupplier;
 import com.google.inject.Injector;
 import com.vividsolutions.jts.io.ParseException;
 
 public abstract class AbstractGeogitFunctionalTest {
 
     protected static final String idL1 = "Lines.1";
 
     protected static final String idL2 = "Lines.2";
 
     protected static final String idL3 = "Lines.3";
 
     protected static final String idP1 = "Points.1";
 
     protected static final String idP2 = "Points.2";
 
     protected static final String idP3 = "Points.3";
 
     protected static final String pointsNs = "http://geogit.points";
 
     protected static final String pointsName = "Points";
 
     protected static final String pointsTypeSpec = "sp:String,ip:Integer,pp:Point:srid=4326";
 
     protected static final Name pointsTypeName = new NameImpl("http://geogit.points", pointsName);
 
     protected SimpleFeatureType pointsType;
 
     protected Feature points1;
 
     protected Feature points1_modified;
 
     protected Feature points2;
 
     protected Feature points3;
 
     protected static final String linesNs = "http://geogit.lines";
 
     protected static final String linesName = "Lines";
 
     protected static final String linesTypeSpec = "sp:String,ip:Integer,pp:LineString:srid=4326";
 
     protected static final Name linesTypeName = new NameImpl("http://geogit.lines", linesName);
 
     protected SimpleFeatureType linesType;
 
     protected Feature lines1;
 
     protected Feature lines2;
 
     protected Feature lines3;
 
     static {
         Logging.ALL.forceMonolineConsoleOutput(java.util.logging.Level.SEVERE);
     }
 
     protected void setupGeogit() throws Exception {
         assertNotNull(currentDirectory);
         assertNotNull(homeDirectory);
 
         stdIn = new ByteArrayInputStream(new byte[0]);
         stdOut = new ByteArrayOutputStream();
 
         ConsoleReader consoleReader = new ConsoleReader(stdIn, stdOut, new UnsupportedTerminal());
 
        GlobalInjectorBuilder.builder = new CLITestInjectorBuilder(currentDirectory, homeDirectory);
        Injector injector = GlobalInjectorBuilder.builder.get();
 
         if (geogit != null) {
             geogit.close();
         }
 
         geogit = new GeoGIT(injector, currentDirectory);
         try {
             geogitCLI = new GeogitCLI(consoleReader);
             geogitCLI.setPlatform(injector.getInstance(Platform.class));
             geogitCLI.setGeogitInjector(injector);
             if (geogit.getRepository() != null) {
                 geogitCLI.setGeogit(geogit);
             }
         } finally {
             geogit.close();
         }
         setupFeatures();
     }
 
     /**
      * Runs the given command with its arguments and returns the command output as a list of
      * strings, one per line.
      */
     protected List<String> runAndParseCommand(String... command) throws Exception {
 
         ByteArrayOutputStream out = runCommand(command);
         InputSupplier<InputStreamReader> readerSupplier = CharStreams.newReaderSupplier(
                 ByteStreams.newInputStreamSupplier(out.toByteArray()), Charset.forName("UTF-8"));
         List<String> lines = CharStreams.readLines(readerSupplier);
         return lines;
     }
 
     protected ByteArrayOutputStream runCommand(String... command) throws Exception {
         // System.err.println("Running command " + Arrays.toString(command));
         assertNotNull(geogitCLI);
 
         stdOut.reset();
 
         geogitCLI.processCommand(command);
 
         return stdOut;
     }
 
     protected void setupFeatures() throws Exception {
         pointsType = DataUtilities.createType(pointsNs, pointsName, pointsTypeSpec);
 
         points1 = feature(pointsType, idP1, "StringProp1_1", new Integer(1000), "POINT(1 1)");
         points1_modified = feature(pointsType, idP1, "StringProp1_1a", new Integer(1001),
                 "POINT(1 2)");
         points2 = feature(pointsType, idP2, "StringProp1_2", new Integer(2000), "POINT(2 2)");
         points3 = feature(pointsType, idP3, "StringProp1_3", new Integer(3000), "POINT(3 3)");
 
         linesType = DataUtilities.createType(linesNs, linesName, linesTypeSpec);
 
         lines1 = feature(linesType, idL1, "StringProp2_1", new Integer(1000),
                 "LINESTRING (1 1, 2 2)");
         lines2 = feature(linesType, idL2, "StringProp2_2", new Integer(2000),
                 "LINESTRING (3 3, 4 4)");
         lines3 = feature(linesType, idL3, "StringProp2_3", new Integer(3000),
                 "LINESTRING (5 5, 6 6)");
     }
 
     protected Feature feature(SimpleFeatureType type, String id, Object... values)
             throws ParseException {
         SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
         for (int i = 0; i < values.length; i++) {
             Object value = values[i];
             if (type.getDescriptor(i) instanceof GeometryDescriptor) {
                 if (value instanceof String) {
                     value = new WKTReader2().read((String) value);
                 }
             }
             builder.set(i, value);
         }
         return builder.buildFeature(id);
     }
 
     protected void insertFeatures() throws Exception {
         insert(points1);
         insert(points2);
         insert(points3);
         insert(lines1);
         insert(lines2);
         insert(lines3);
     }
 
     protected void insertAndAddFeatures() throws Exception {
         insertAndAdd(points1);
         insertAndAdd(points2);
         insertAndAdd(points3);
         insertAndAdd(lines1);
         insertAndAdd(lines2);
         insertAndAdd(lines3);
     }
 
     /**
      * Inserts the Feature to the index and stages it to be committed.
      */
     protected ObjectId insertAndAdd(Feature f) throws Exception {
         ObjectId objectId = insert(f);
 
         geogit.command(AddOp.class).call();
         return objectId;
     }
 
     /**
      * Inserts the feature to the index but does not stages it to be committed
      */
     protected ObjectId insert(Feature f) throws Exception {
         final WorkingTree workTree = geogit.getRepository().getWorkingTree();
         Name name = f.getType().getName();
         String parentPath = name.getLocalPart();
         Node ref = workTree.insert(parentPath, f);
         ObjectId objectId = ref.getObjectId();
         return objectId;
     }
 
     protected void insertAndAdd(Feature... features) throws Exception {
         insert(features);
         geogit.command(AddOp.class).call();
     }
 
     protected void insert(Feature... features) throws Exception {
         for (Feature f : features) {
             insert(f);
         }
     }
 
     /**
      * Deletes a feature from the index
      * 
      * @param f
      * @return
      * @throws Exception
      */
     protected boolean deleteAndAdd(Feature f) throws Exception {
         boolean existed = delete(f);
         if (existed) {
             geogit.command(AddOp.class).call();
         }
 
         return existed;
     }
 
     protected boolean delete(Feature f) throws Exception {
         final WorkingTree workTree = geogit.getRepository().getWorkingTree();
         Name name = f.getType().getName();
         String localPart = name.getLocalPart();
         String id = f.getIdentifier().getID();
         boolean existed = workTree.delete(localPart, id);
         return existed;
     }
 }

 package com.lewisd.ksp.craftstats;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
 
 import com.lewisd.ksp.craftstats.gamedata.Part;
 import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;
 import com.lewisd.ksp.craftstats.util.AbstractVehicleMain;
 import com.lewisd.ksp.craftstats.vehicle.Vehicle;
 import com.lewisd.ksp.craftstats.vehicle.VehiclePart;
 
 public class PartHierarchy extends AbstractVehicleMain {
 
     public static void main(final String[] args) throws IOException {
         new PartHierarchy(args).run();
     }
 
     private final boolean verbose;
 
     public PartHierarchy(final KerbalContext kerbalContext, final boolean verbose) {
         super(kerbalContext);
         this.verbose = verbose;
     }
 
     public PartHierarchy(final String[] args) {
         super(args);
 
         verbose = cmdLine.hasOption("verbose");
     }
 
     @Override
     protected void addMoreOptions(final Options options) {
         options.addOption("v", "verbose", false, "Verbose output");
     }
 
     @Override
     protected void checkMoreRequired(final CommandLine cmdLine) {
     }
 
     @Override
     protected void run(final Vehicle vehicle) throws IOException {
         printPartHierarchy(vehicle, System.out);
     }
 
     public void printPartHierarchy(final Vehicle vehicle, final PrintStream out) {
         printPartHierarchy(out, vehicle.getRootPart(), "", false);
     }
 
     private void printPartHierarchy(final PrintStream out, final VehiclePart parent, final String prefix, final boolean hasNextSibling) {
         final String sym;
         final Part part = parent.getPart();
         if (part.isDecoupler()) {
             sym = "X";
         } else if (part.isEngine()) {
             sym = "V";
         } else if (part.isLaunchClamp()) {
             sym = "C";
         } else {
             sym = "+";
         }
         System.out.print(prefix + sym + "-" + part.getTitle());
         if (verbose) {
             out.print(" (" + parent.getId() + ")");
             final List<String> texts = new ArrayList<>();
             texts.add(String.format("mass %.4f/%.4ft", parent.getMass(), parent.getDryMass()));
             if (parent.getQueuedStage() >= 0) {
                 if (parent.getQueuedStage() >= 0) {
                     texts.add(("sqor=" + parent.getQueuedStage()));
                 }
             }
            out.print(" (" + StringUtils.join(texts, ", ") + ")");
         }
         out.println();
         final List<VehiclePart> children = new ArrayList<>();
         for (final VehiclePart child : parent.getSortedChildren()) {
             if (child.getChildren().isEmpty()) {
                 children.add(child);
             }
         }
         for (final VehiclePart child : parent.getSortedChildren()) {
             if (!child.getChildren().isEmpty()) {
                 children.add(child);
             }
         }
         final Iterator<VehiclePart> i = children.iterator();
         while (i.hasNext()) {
             final VehiclePart child = i.next();
             final String newPrefix;
             if (hasNextSibling) {
                 newPrefix = prefix + "| ";
             } else {
                 newPrefix = prefix + "  ";
             }
             printPartHierarchy(out, child, newPrefix, i.hasNext());
         }
     }
 
 }

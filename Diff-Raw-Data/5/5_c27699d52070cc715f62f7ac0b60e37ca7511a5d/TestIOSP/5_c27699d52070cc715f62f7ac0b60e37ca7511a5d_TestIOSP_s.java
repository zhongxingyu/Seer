 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ooici.eoi.netcdf.test;
 
 import java.io.IOException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import ooici.netcdf.iosp.OOICIiosp;
 import ucar.nc2.dataset.NetcdfDataset;
 
 /**
  *
  * @author cmueller
  * @deprecated This class was for early prototype testing and is no longer functional
  */
 @Deprecated
 public class TestIOSP {
 
     public TestIOSP(String dsName) {
 
         try {
             java.util.HashMap<String, String> connInfo = new java.util.HashMap<String, String>();
             connInfo.put("exchange", "eoitest");
             connInfo.put("service", "eoi_ingest");
             connInfo.put("server", "macpro");
             connInfo.put("topic", "magnet.topic");
 
             /* Initialize and register the OOICI IOSP */
             OOICIiosp.init(connInfo);
             NetcdfDataset.registerIOProvider(OOICIiosp.class);
 
             System.out.println("<<<<<< " + dsName + " >>>>>>");
             testDatasetFromStore("ooici:" + dsName);
 
         } catch (IOException ex) {
             Logger.getLogger(TestIOSP.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(TestIOSP.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             Logger.getLogger(TestIOSP.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void testDatasetFromStore(String dsName) throws IOException {
         NetcdfDataset ncd = null;
         try {
             ncd = NetcdfDataset.openDataset(dsName);
 
             boolean printFTInfo = false,
                     printDetails = true,
                     printDump = true,
                     printDataSample = true,
                     writeLocalDataset = false;
             if (printFTInfo) {
                 /* Find and print featuretype information */
                 System.out.println("********** FeatureType Info **********");
                 ucar.nc2.constants.FeatureType ft = determineFeatureType(ncd);
                 if (ft != null) {
                     System.out.println("FeatureType: " + ft.name());
                 } else {
                     System.out.println("Could not determine the FeatureType of the dataset.");
                 }
                 System.out.println("**************************************");
             }
             if (printDetails) {
                 /* Print detailed information about the dataset */
                 System.out.println("********** Detail Info **********");
                 System.out.println(ncd.getDetailInfo());
                 System.out.println("*********************************");
             }
             if (printDump) {
                 /* Dump the cdl */
                 System.out.println("********** NcDump **********");
                 System.out.println(ncd.toString());
                 System.out.println("****************************");
             }
             if (printDataSample) {
                 /* Test some simple data reads */
                 System.out.println("********** SampleData **********");
                 java.util.List<ucar.nc2.Variable> vars = ncd.getVariables();
                 for (ucar.nc2.Variable v : vars) {
                     System.out.println("~~~~~~~~~~~~~~~");
                     System.out.println("Sample data values for variable: " + v.getNameAndDimensions());
                     int[] shape = v.getShape();
                     if (shape.length == 0) {
                         /* Scalar!  Just print the value*/
                         System.out.print(v.readScalarDouble());
                     } else {
                         int[] origin = shape.clone();
                         java.util.Arrays.fill(origin, 0);
 //                        java.util.Arrays.fill(shape, 1);
                         shape[Math.max(shape.length - 1, 0)] = Math.min(10, shape[shape.length - 1]);
 
                         ucar.ma2.Array a = null;
                         Throwable t = null;
                         try {
                             a = v.read(origin, shape);
                         } catch (ucar.ma2.InvalidRangeException ex) {
                             t = ex;
                         } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                             t = ex;
                         }
                         if (t != null || a == null) {
                             System.out.println("***Error reading variable!!!***");
                             t.printStackTrace(System.out);
                             System.out.println("~~~~~~~~~~~~~~~");
                             continue;
                         }
                         ucar.ma2.IndexIterator ii = a.getIndexIterator();
                         while (ii.hasNext()) {
                             System.out.print(ii.getDoubleNext());
                             if (ii.hasNext()) {
                                 System.out.print(", ");
                             }
                         }
                     }
                     System.out.println();
                     System.out.println("~~~~~~~~~~~~~~~");
                 }
                 System.out.println("********************************");
             }
             if (writeLocalDataset) {
                 /* Write the NetcdfDataset to a local file - for direct comparison to the original */
                 ucar.nc2.FileWriter.writeToFile(ncd, "output/" + dsName.replace("ooici:", "") + "_sermess.nc");
             }
         } finally {
             if (ncd != null) {
                 ncd.close();
             }
         }
 
         System.out.println("---------------------------------------------------");
         System.out.println();
     }
 
     private ucar.nc2.constants.FeatureType determineFeatureType(NetcdfDataset ncd) {
         /* Check for the feature type using "helper" metadata (cdm_data_type, cdm_datatype, or thredds_data_type attribute) */
         ucar.nc2.constants.FeatureType ret = ucar.nc2.ft.FeatureDatasetFactoryManager.findFeatureType(ncd);
         if (ret != null) {
             System.out.print("via FDFM.findFeatureType() --> ");
             return ret;
         }
         /* Try to open the dataset through the FactoryManager (more thorough check than above) */
         java.util.Formatter out = new java.util.Formatter();
         ucar.nc2.ft.FeatureDataset fds = null;
         try {
             fds = ucar.nc2.ft.FeatureDatasetFactoryManager.open(ucar.nc2.constants.FeatureType.ANY, ncd.getLocation(), null, out);
             if (fds != null) {
                 System.out.print("via FDFM.open() --> ");
                 return fds.getFeatureType();
             }
         } catch (IllegalStateException ex) {
         } catch (IOException ex) {
         } finally {
             if (fds != null) {
                 try {
                     fds.close();
                 } catch (IOException ex) {
                 }
             }
         }
         return ret;
     }
 
     public static void main(String[] args) {
         String dataset = null;
         if (args.length == 0) {
             try {
                 java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
                 System.out.println("Enter the dataset name, then hit \"enter\":");
                 dataset = reader.readLine();
             } catch (IOException ex) {
                 System.exit(-1);
             }
         } else {
             dataset = args[0];
         }
         if (dataset != null) {
             new TestIOSP(dataset);
         }
     }
 }

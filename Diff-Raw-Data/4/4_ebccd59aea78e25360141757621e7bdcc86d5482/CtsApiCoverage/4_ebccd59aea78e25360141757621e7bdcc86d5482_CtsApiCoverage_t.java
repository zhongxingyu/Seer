 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.cts.apicoverage;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Tool that generates a report of what Android framework methods are being called from a given
  * set of APKS. See the {@link #printUsage()} method for more details.
  */
 public class CtsApiCoverage {
 
     private static final int FORMAT_TXT = 0;
 
     private static final int FORMAT_XML = 1;
 
     private static void printUsage() {
         System.out.println("Usage: cts-api-coverage [OPTION]... [APK]...");
         System.out.println();
         System.out.println("Generates a report about what Android framework methods are called ");
         System.out.println("from the given APKs.");
         System.out.println();
         System.out.println("Use the Makefiles rules in CtsTestCoverage.mk to generate the report ");
         System.out.println("rather than executing this directly. If you still want to run this ");
         System.out.println("directly, then this must be used from the $ANDROID_BUILD_TOP ");
         System.out.println("directory and dexdeps must be built via \"make dexdeps\".");
         System.out.println();
         System.out.println("Options:");
         System.out.println("  -o FILE         output file or standard out if not given");
         System.out.println("  -f [txt|xml]    format of output either text or xml");
         System.out.println();
         System.exit(1);
     }
 
     public static void main(String[] args) throws Exception {
         List<File> testApks = new ArrayList<File>();
         File outputFile = null;
         int format = FORMAT_TXT;
 
         for (int i = 0; i < args.length; i++) {
             if (args[i].startsWith("-")) {
                 if ("-o".equals(args[i])) {
                     if (i + 1 < args.length) {
                         outputFile = new File(args[++i]);;
                     } else {
                         printUsage();
                     }
                 } else if ("-f".equals(args[i])) {
                     if (i + 1 < args.length) {
                         String formatArg = args[++i];
                         if ("xml".equalsIgnoreCase(formatArg)) {
                             format = FORMAT_XML;
                         } else if ("txt".equalsIgnoreCase(formatArg)) {
                             format = FORMAT_TXT;
                         } else {
                             printUsage();
                         }
                     } else {
                         printUsage();
                     }
                 } else {
                     printUsage();
                 }
             } else {
                 testApks.add(new File(args[i]));
             }
         }
 
         /*
          * 1. Create an ApiCoverage object that is a tree of Java objects representing the API
          *    in current.xml. The object will have no information about the coverage for each
          *    constructor or method yet.
          *
          * 2. For each provided APK, scan it using dexdeps, parse the output of dexdeps, and
          *    call methods on the ApiCoverage object to cumulatively add coverage stats.
          *
          * 3. Output a report based on the coverage stats in the ApiCoverage object.
          */
 
         ApiCoverage apiCoverage = getEmptyApiCoverage();
         for (File testApk : testApks) {
             addApiCoverage(apiCoverage, testApk);
         }
         outputCoverageReport(apiCoverage, testApks, outputFile, format);
     }
 
     /**
      * Creates an object representing the API that will be used later to collect coverage
      * statistics as we iterate over the test APKs.
      *
      * @return an {@link ApiCoverage} object representing the API in current.xml without any
      *     coverage statistics yet
      */
     private static ApiCoverage getEmptyApiCoverage()
             throws SAXException, IOException {
         XMLReader xmlReader = XMLReaderFactory.createXMLReader();
         CurrentXmlHandler currentXmlHandler = new CurrentXmlHandler();
         xmlReader.setContentHandler(currentXmlHandler);
 
         File currentXml = new File("frameworks/base/api/current.xml");
         FileReader fileReader = null;
         try {
             fileReader = new FileReader(currentXml);
             xmlReader.parse(new InputSource(fileReader));
         } finally {
             if (fileReader != null) {
                 fileReader.close();
             }
         }
 
         return currentXmlHandler.getApi();
     }
 
     /**
      * Adds coverage information gleamed from running dexdeps on the APK to the
      * {@link ApiCoverage} object.
      *
      * @param apiCoverage object to which the coverage statistics will be added to
      * @param testApk containing the tests that will be scanned by dexdeps
      */
     private static void addApiCoverage(ApiCoverage apiCoverage, File testApk)
             throws SAXException, IOException {
         XMLReader xmlReader = XMLReaderFactory.createXMLReader();
         DexDepsXmlHandler dexDepsXmlHandler = new DexDepsXmlHandler(apiCoverage);
         xmlReader.setContentHandler(dexDepsXmlHandler);
 
        // TODO: Take an argument to specify the location of dexdeps.
        Process process = new ProcessBuilder("out/host/linux-x86/bin/dexdeps",
                "--format=xml", testApk.getPath()).start();
         xmlReader.parse(new InputSource(process.getInputStream()));
     }
 
     private static void outputCoverageReport(ApiCoverage apiCoverage, List<File> testApks,
             File outputFile, int format) throws IOException {
         OutputStream out = outputFile != null
                 ? new FileOutputStream(outputFile)
                 : System.out;
 
         try {
             switch (format) {
                 case FORMAT_TXT:
                     TextReport.printTextReport(apiCoverage, out);
                     break;
 
                 case FORMAT_XML:
                     XmlReport.printXmlReport(testApks, apiCoverage, out);
                     break;
             }
         } finally {
             out.close();
         }
     }
 }

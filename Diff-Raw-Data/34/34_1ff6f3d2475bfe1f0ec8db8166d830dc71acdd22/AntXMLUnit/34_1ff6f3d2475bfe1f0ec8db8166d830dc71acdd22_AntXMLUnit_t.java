 /*
 Copyright (c) 2011, 2012 Parjanya Mudunuri
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 associated documentation files (the "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to
 the following conditions:
 
 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
 http://opensource.org/licenses/mit-license.php
  */
 
 package uk.co.firstzero.xml;
 
 import org.apache.log4j.Logger;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.FileSet;
 import org.custommonkey.xmlunit.*;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 public class AntXMLUnit extends Task {
     Logger logger = Logger.getLogger(AntXMLUnit.class);
 	private Vector<FileSet> fileSets = new Vector<FileSet>();
 	private String testDirectory;
 	private String resultDirectory;
 	private String separator = ",";
 	
 	private DocumentBuilderFactory factory;
 	private DocumentBuilder builder;
	private static List<Integer> ignoreIds = new ArrayList<Integer>();
 	
 	public AntXMLUnit() { } 
 	
 	/**
 	 * Input file set
 	 * @param fileset
 	 */
 	public void addFileSet(FileSet fileset) {
 		if (!fileSets.contains(fileset)) {
     	  fileSets.add(fileset);
       	}
 	}
 	
 	/**
 	 * The directory from which to read the test xml files
 	 * @param testDirectory
 	 */
 	public void setTestDirectory(String testDirectory) {
 		this.testDirectory = testDirectory;
 	}
 	
 	/**
 	 * The directory to which to publish the csv report
 	 * @param resultDirectory
 	 */
 	public void setResultDirectory(String resultDirectory) {
 		this.resultDirectory = resultDirectory;
 	}
 	
 	public void setSeparator(String separator) {
 		this.separator = separator;
 	}
 
     /**
      * Singleton to return DocumentBuilderFactory
      * @return
      */
     private DocumentBuilderFactory getDocumentBuilderFactory() {
         if (factory == null) {
             factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true);
         }
 
         return factory;
     }
 
     /**
      * Singleton to return a builder
      * @return
      * @throws ParserConfigurationException
      */
     private DocumentBuilder getBuilder() throws ParserConfigurationException {
         if (builder == null)
             builder = getDocumentBuilderFactory().newDocumentBuilder();
 
         return builder;
     }
 
     /**
      * Called by ant to scan and pick up files and then perform a diff
      */
 	public void execute() {
 		//Check if the test directory exists
 		File testDir = new File(testDirectory);
     	if (!testDir.exists()) {
     		logger.error(testDirectory + " does not exist");
     		System.exit(1);
     	}
 
         DirectoryScanner ds;
 		preSetup();
 		
 		for (FileSet fileset : fileSets) {
 			//Read in the control files
 			ds = fileset.getDirectoryScanner(getProject());
         	File controlDir = ds.getBasedir();
         	String[] filesInSet = ds.getIncludedFiles();
         	
             for (String fileName : filesInSet) {
         		 log("Processing " + fileName);
 
                 try {
 	        		File controlFile = new File(controlDir, fileName);
 	        		File testFile = new File(testDir, fileName);
 	        		List<Difference> differences = processFile(controlFile, testFile);
                     writeReport(differences, controlFile);
                 } catch(ParserConfigurationException e) {
                     logger.error("Unable setup dependencies");
                     logger.error(e);
 
                 }
                 catch(IOException e) {
                     logger.error("Unable to process - " + fileName);
                     logger.error(e);
 
                 } catch(SAXException e) {
                     logger.error("Unable to read either control/test file " + fileName);
                     logger.error(e);
 
                 }
         	}
 			 
 		}
 	}
 	
 	/**
 	 * Sets up xmlunit constants
 	 */
 	private void preSetup() {
         XMLUnit.setIgnoreAttributeOrder(true);
         XMLUnit.setIgnoreWhitespace(true);
 
        ignoreIds.add(new Integer(DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID));
 	}
 
     /**
      * Builds up the list of differences
      * @param controlFile The original file
      * @param testFile  The new file to be compared agains
      * @return
      * @throws IOException
      * @throws SAXException
      * @throws ParserConfigurationException
      */
 	public List<Difference> processFile(File controlFile, File testFile) throws IOException, SAXException, ParserConfigurationException {
         builder = getBuilder();
         Document control = builder.parse(controlFile);
         Document test = builder.parse(testFile);
 
 
         Diff diff = new Diff(control, test);
         DetailedDiff dd = new DetailedDiff(diff);
         return dd.getAllDifferences();
 	}
 
     private void writeReport(List<Difference> differences, File controlFile) throws IOException {
         Writer report = new BufferedWriter(new FileWriter(resultDirectory + "/" + controlFile.getName() + ".csv"));
 
         String header = "Diff ID" + this.separator + "Description" + this.separator
                         + "Control Value" + this.separator + "Test Value" + this.separator
                         + "XPath" + "\n";
 
         report.write(header);
 
         for (Object differenceObject : differences) {
             Difference difference = (Difference)differenceObject;
 
            if (ignoreIds.contains(new Integer(difference.getId()))) {
                 logger.trace("SKIP - "
                         + difference.getId() + this.separator
                         + difference.getDescription() + this.separator
                         + difference.getControlNodeDetail().getXpathLocation());
             }
             else {
                 String differenceString = difference.getId() + this.separator
                     + difference.getDescription() + this.separator
                     + difference.getControlNodeDetail().getValue() + this.separator
                     + difference.getTestNodeDetail().getValue() + this.separator
                     + difference.getControlNodeDetail().getXpathLocation();
 
                 report.write(differenceString + "\n");
 
                 logger.debug("DIFF - " + differenceString);
             }
         }
 
         if (differences.size() == 0)
             logger.info(controlFile.getAbsolutePath() + "/" + controlFile.getName() + "No difference found");
 
         report.close();
     }
 }

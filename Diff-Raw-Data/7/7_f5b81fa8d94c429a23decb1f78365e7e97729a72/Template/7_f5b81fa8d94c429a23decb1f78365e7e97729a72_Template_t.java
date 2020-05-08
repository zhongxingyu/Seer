 /*
  * Copyright 1999 - 2014 Herb Bowie
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.powersurgepub.psdatalib.template;
 
   import com.powersurgepub.pstextio.*;
   import com.powersurgepub.psdatalib.tabdelim.*;
   import com.powersurgepub.psdatalib.psdata.*;
   import com.powersurgepub.psutils.*;
   import java.io.File;
   import java.io.IOException;
   import java.util.*;
 
 /**
    A template to be used to create text file 
    output (such as an HTML file) from a tab-delimited text file. <p>
   
    This code is copyright (c) 1999-2000 by Herb Bowie of PowerSurge Publishing. 
    All rights reserved. <p>
    
    Version History: <ul><li>
       2001/02/28 - Corrected logic for reading tab-delimited data to make
                    sure that last good record is processed, and no more.
       2000/05/30 - Modified to be consistent with "The Elements of Java Style".
    </ul>
   
    @author Herb Bowie (<a href="mailto:herb@powersurgepub.com">
            herb@powersurgepub.com</a>)<br>
            of PowerSurge Publishing (<A href="http://www.powersurgepub.com/software/">
            www.powersurgepub.com/software</a>)
   
    @version 2002/03/25 - Added support for group processing.
  */
 public class Template {
   
   /** Used to write log messages. */
   public     Logger         log;
   
   /** Next template line to be processed. */
   private    String         nextLineString;
   
   /** Global variables stored within a data record. */
   private    DataRecord     globals;
   
   /** Definition of global variables record layout. */
   private    RecordDefinition globalDefs;
   
   /** Record number of the next template line. */
   private    int            nextRecordNumber;
   
   /** Next template line to be processed. */
   private    TemplateLine   nextLine;
   
   /** Work area for a template line. */
   private    TemplateLine   recLine;
   
   /** Collection of all the template lines that need to be processed repetitively. */
   private    ArrayList<TemplateLine>          recLines;
 	
 	/** Collection of all the ifendgroup lines. */
 	private		 ArrayList<TemplateLine>          endGroupLines;
   
   private    boolean        outerLoop = false;
   
   /** Collection of all the lines within an outer loop, but before the inner loop. */
   private    ArrayList<TemplateLine>          outerLinesBefore;
   
   /** Collection of all the lines within an outer loop, but after the inner loop. */
   private    ArrayList<TemplateLine>          outerLinesAfter;
 	
 	/** Flag to indicate we are in an end group block. */
 	private		 boolean				endGroupBlock = false;
   
   /** Template line work area for lines at the end of the template file. */
   private    TemplateLine   endLine;
   
   /** All the lines at the end of the template file. */
   private    ArrayList<TemplateLine>         endLines;
   
   private    ArrayList<DataRecord>       dataRecs;
   
   /** File name for the tab-delimited data file. */
   private    String         dataFileName;
   
   /** Data file as a File object. */
   private    File           dataFileSpec;
   
   /** 
      Generic definition of input data file: could be 
      a XTextFile or a DirectoryReader. 
    */
   private    DataSource     dataFile;
   
   /** Was the data file opened successfully? */
   private    boolean        dataFileOK;
   
   /** Number of records so far read from the data file. */
   private    int            dataFileRecordCount = 0;
   
   /** Data dictionary for the input data file. */
   private    DataDictionary dataDict;
   
   /** Work area for data records. */
   private    DataRecord     dataRec;
   
   /** The last data record read. */
   private    DataRecord     lastRec;
   
   /** 
      A data record with no fields (to be passed to TemplateLine
      before there are any real data records available.
    */
   private    DataRecord     nullRec;
   
   /**
      Stuff that needs to be passed to TemplateLine, and shared
      between TemplateLine objects.
    */
   private    TemplateUtil   templateUtil;
 
   /**
      Constructs and initializes a Template object, 
      creating a new Logger occurrence.
    */
   public Template () {
     this (new Logger (new LogOutput()));  
   }
   
   /**
      Constructs and initializes a Template object, 
      using the passed Logger occurrence.
    */
   public Template (Logger log) {
     nullRec = new DataRecord();
     this.log = log;
     templateUtil = new TemplateUtil (log);
     globals = new DataRecord();
     globalDefs = new RecordDefinition();
   }
   
   public void setWebRoot (File webRootFile) {
     templateUtil.setWebRoot(webRootFile);
   }
   
   /**
      Opens the input template file.
     
      @return A boolean value indicating the success of the operation.
     
      @param inTemplateFileSpec  A File object pointing to the input 
                                 template file.
    */
   public boolean openTemplate (File inTemplateFileSpec) {
     return templateUtil.openTemplate (inTemplateFileSpec);
   }
   
   public void setTemplateFilePath (String path) {
     templateUtil.setTemplateFilePath (path);
   }
   
   /**
      Opens the input tab-delimited data file.
      
      @return a boolean value indicating the success of the operation.
     
      @param inDataFileSpec  A File object pointing to the input 
                             tab-delimited data file.
    */
   public boolean openData (File inDataFileSpec) {
     dataFileSpec = inDataFileSpec;
     dataFileName = dataFileSpec.getAbsolutePath();
     FileName workName = new FileName (dataFileName);
     if (dataFileSpec.isFile()) {
       dataFile = new TabDelimFile (dataFileName);
       templateUtil.setDataFileDisplay (workName.getFileName());
       templateUtil.setDataFileBaseName(workName.getBase());
     } else
     if (dataFileSpec.isDirectory()) {
       DirectoryReader dr = new DirectoryReader (dataFileSpec.getAbsolutePath());
       try {
         dataFile = dr.sorted();
       } catch (IOException e) {
       }
       templateUtil.setDataFileDisplay (workName.getFolder());
       templateUtil.setDataFileBaseName(workName.getFolder());
     }
     openDataSource();
     return dataFileOK;
   }
   
   /**
      Opens the input data source.
      
      @return a boolean value indicating the success of the operation.
     
      @param inDataFile  A source of data records.
    */
   public boolean openData (DataSource inDataFile, String inFileName) {
     dataFile = inDataFile;
     templateUtil.setDataFileDisplay (inFileName);
     FileName workName = new FileName (inFileName);
     templateUtil.setDataFileBaseName(workName.getBase());
     templateUtil.setDataParent (dataFile.getDataParent());
     openDataSource();
     return dataFileOK;
   }
   
   /**
      Open the input data, once it has been turned into a data source.
    */
   private void openDataSource () {
     dataFile.setLog (log);
     dataFile.setDataLogging (true);
     dataDict = new DataDictionary();
     try {
       dataFile.openForInput (dataDict);
       dataFileOK = true;
     } catch (IOException e) {
       dataFileOK = false;
     }
   } // end method openDataSource
   
   /**
      Reads the two input files and uses them to generate
      the output file(s) resulting from the merge operation.</p>
     
      @return A boolean value indicating the success of the operation.
    */
   public boolean generateOutput () 
     throws IOException {
   
     if ((! templateUtil.isTemplateFileOK()) 
         // || (! dataFileOK)
         ) {
       return false;
     }
     
     recLines = new ArrayList<TemplateLine>();
     outerLinesBefore = new ArrayList<TemplateLine>();
     outerLinesAfter = new ArrayList<TemplateLine>();
 		endGroupLines = new ArrayList<TemplateLine>();
     endLines = new ArrayList();
     dataRecs = new ArrayList<DataRecord>();
     outerLoop = false;
     
     // Process lines up to the NEXTREC or OUTER command.
     // These lines only need to be processed once. 
     templateUtil.setSkippingData (false);
     templateUtil.setFirstTemplateLine (true);
     do {
       nextLine = nextTemplateLine();
       templateUtil.setFirstTemplateLine (false);
       if (! templateUtil.isTemplateFileAtEnd()) {
         nextLine.generateOutput(nullRec);
       }
     } while ((! templateUtil.isTemplateFileAtEnd()) 
       && (! nextLine.getCommand().equals (TemplateLine.NEXTREC))
       && (! nextLine.getCommand().equals (TemplateLine.OUTER)));
       
     // Let's get out of here if we encountered a problem.
     if ((! templateUtil.isTemplateFileAtEnd())
         && (! dataFileOK)) {
       return false;
     }
     
     // If the template file didn't contain a NEXTREC command  
     // or an OUTER command, then let's get out of here.
     if (templateUtil.isTemplateFileAtEnd()) {
       if (templateUtil.isTextFileOutOpen()) {
         templateUtil.close();
       }
       templateUtil.closeTemplateFile();
       return true;
     }
     
     // If we have an outer loop, then collect those lines and store them
     if ((! templateUtil.isTemplateFileAtEnd())
         && nextLine.getCommand().equals (TemplateLine.OUTER)) {
       outerLoop = true;
       do {
         nextLine = nextTemplateLine ();
         if (! templateUtil.isTemplateFileAtEnd()) {
           if (nextLine.isCommandLine()
               && nextLine.getCommand().equals (TemplateLine.DELIMS)) {
             nextLine.generateOutput(nullRec);
           } else {
             outerLinesBefore.add (nextLine);
             if (nextLine.isCommandLine()
               && nextLine.getCommand().equals (TemplateLine.IFENDGROUP)) {
               endGroupBlock = true;
             }
             if (endGroupBlock) {
               endGroupLines.add (nextLine);
             }
             if (nextLine.isCommandLine()
               && nextLine.getCommand().equals (TemplateLine.IFNEWGROUP)) {
               endGroupBlock = false;
             }
           }
         }
       } while ((! templateUtil.isTemplateFileAtEnd()) 
         && (! nextLine.getCommand().equals (TemplateLine.NEXTREC)));
     }
     
     // table lines between NEXTREC and LOOP
     do {
       nextLine = nextTemplateLine ();
       if (! templateUtil.isTemplateFileAtEnd()) {
         if (nextLine.isCommandLine()
           && nextLine.getCommand().equals (TemplateLine.DELIMS)) {
           nextLine.generateOutput(nullRec);
         } else {
           recLines.add (nextLine);
 					if (nextLine.isCommandLine()
 						&& nextLine.getCommand().equals (TemplateLine.IFENDGROUP)) {
 						endGroupBlock = true;
 					}
 					if (endGroupBlock) {
 						endGroupLines.add (nextLine);
 					}
 					if (nextLine.isCommandLine()
 						&& nextLine.getCommand().equals (TemplateLine.IFNEWGROUP)) {
 						endGroupBlock = false;
 					}
         }
       }
     } while ((! templateUtil.isTemplateFileAtEnd()) 
       && (! nextLine.getCommand().equals (TemplateLine.LOOP)));
     
     // If we have an outer loop, then collect the lines after the inner loop
     // and store them.
     if ((! templateUtil.isTemplateFileAtEnd()) & outerLoop) {
       do {
         nextLine = nextTemplateLine ();
         if (! templateUtil.isTemplateFileAtEnd()) {
           if (nextLine.isCommandLine()
               && nextLine.getCommand().equals (TemplateLine.DELIMS)) {
             nextLine.generateOutput(nullRec);
           } else {
             outerLinesAfter.add (nextLine);
             if (nextLine.isCommandLine()
               && nextLine.getCommand().equals (TemplateLine.IFENDGROUP)) {
               endGroupBlock = true;
             }
             if (endGroupBlock) {
               endGroupLines.add (nextLine);
             }
             if (nextLine.isCommandLine()
               && nextLine.getCommand().equals (TemplateLine.IFNEWGROUP)) {
               endGroupBlock = false;
             }
           }
         }
       } while ((! templateUtil.isTemplateFileAtEnd()) 
         && (! nextLine.getCommand().equals (TemplateLine.OUTERLOOP)));
     }
       
     // table lines after loop
     do {
       nextLine = nextTemplateLine ();
       if (! templateUtil.isTemplateFileAtEnd()) {
         if (nextLine.isCommandLine()
           && nextLine.getCommand().equals (TemplateLine.DELIMS)) {
           nextLine.generateOutput(nullRec);
         } else {
           endLines.add (nextLine);
         }
       }
     } while (! templateUtil.isTemplateFileAtEnd());
     
     // Now let's process the data. 
     templateUtil.setSkippingData (false);
     
     // If we have an outer loop, then read the data records and store them
     // in an array. 
     if (outerLoop) {
       do {
         dataRec = dataFile.nextRecordIn();
         if (dataRec != null) {
           dataRecs.add(dataRec);
         }
       } while (! dataFile.isAtEnd());
       int outerIndex = 0;
       dataRec = null;
       do {
         
         // Write outer loop lines before inner loop
         dataRec = dataRecs.get(outerIndex);
        templateUtil.setSkippingData (false);
         templateUtil.resetGroupBreaks();
         Iterator eOuterLinesBefore = outerLinesBefore.iterator();
         while (eOuterLinesBefore.hasNext()) {
           recLine = (TemplateLine)eOuterLinesBefore.next();
           if (recLine.getCommand().equals (TemplateLine.OUTPUT)) {
             if (! templateUtil.isSkippingData()) {
               if (templateUtil.isTextFileOutOpen()) {
                 writeEndLines();
               } // end if text file out open
             } // end skipping Data check
           } // end if next line is OUTPUT command
           recLine.generateOutput(dataRec);
         }
         
         // Now iterate through inner loop
         int innerIndex = 0;
         do {
           dataRec = dataRecs.get(innerIndex);
          templateUtil.setSkippingData (false);
           templateUtil.resetGroupBreaks();
           Iterator eRecLines = recLines.iterator();
           while (eRecLines.hasNext()) {
             recLine = (TemplateLine)eRecLines.next();
             if (recLine.getCommand().equals (TemplateLine.OUTPUT)) {
               if (! templateUtil.isSkippingData()) {
                 if (templateUtil.isTextFileOutOpen()) {
                   writeEndLines();
                 } // end if text file out open
               } // end skipping Data check
             } // end if next line is OUTPUT command
             recLine.generateOutput(dataRec);
           }
           innerIndex++;
         } while (innerIndex < dataRecs.size());
         
         // Writer outer loop lines after inner loop
         dataRec = dataRecs.get(outerIndex);
        templateUtil.setSkippingData (false);
         templateUtil.resetGroupBreaks();
         Iterator eOuterLinesAfter = outerLinesAfter.iterator();
         while (eOuterLinesAfter.hasNext()) {
           recLine = (TemplateLine)eOuterLinesAfter.next();
           if (recLine.getCommand().equals (TemplateLine.OUTPUT)) {
             if (! templateUtil.isSkippingData()) {
               if (templateUtil.isTextFileOutOpen()) {
                 writeEndLines();
               } // end if text file out open
             } // end skipping Data check
           } // end if next line is OUTPUT command
           recLine.generateOutput(dataRec);
         }
         
         // Bump up the outer loop index
         outerIndex++;
       } while (outerIndex < dataRecs.size());
     } else {
       // process tab delimited data file
       do {
         lastRec = dataRec;
         if (! dataFile.isAtEnd()) {
           dataRec = dataFile.nextRecordIn ();
           if (dataRec != null) {
             templateUtil.resetGroupBreaks();
            templateUtil.setSkippingData (false);
             Iterator eRecLines = recLines.iterator();
             while (eRecLines.hasNext()) {
               recLine = (TemplateLine)eRecLines.next();
               if (recLine.getCommand().equals (TemplateLine.OUTPUT)) {
                 if (! templateUtil.isSkippingData()) {
                   if (templateUtil.isTextFileOutOpen()) {
                     writeEndLines();
                   } // end if text file out open
                 } // end skipping Data check
               } // end if next line is OUTPUT command
               recLine.generateOutput(dataRec);
             } // end while more template record lines in vector 
           } // end dataRec not null
         } // end if more data records
       } while (! dataFile.isAtEnd());
     }
     
 		// end of data file - end all groups
 		templateUtil.setSkippingData (false);
 		templateUtil.resetGroupBreaks();
 		templateUtil.setEndGroupsTrue (0);
 		Iterator eEndGroupLines = endGroupLines.iterator();
 		while (eEndGroupLines.hasNext()) {
 			recLine = (TemplateLine)eEndGroupLines.next();
 			recLine.generateOutput(nullRec);
 		} // end while more end group lines
 		
     dataFileRecordCount = dataFile.getRecordNumber();
     dataFile.close();
     
     if (templateUtil.isTextFileOutOpen()) {
       writeEndLines();
       templateUtil.close();
     }
     templateUtil.closeTemplateFile();
     if (templateUtil.getOutputCommandCount() == 0) {
       templateUtil.recordEvent (LogEvent.MINOR,
         "No OUTPUT Command Found", false);
     } else
     if (templateUtil.getOutputCommandCount() > 1) {
       templateUtil.recordEvent (LogEvent.MINOR,
         "More than one OUTPUT Command Found", false);
     }
     // templateUtil.recordEvent (LogEvent.NORMAL,
     //   "End of TabToTemplateMerge operation", false);
     return true;
       
   } // end GenerateOutput method
   
   /**
      Writes out the last lines in the template 
      (the ones following the LOOP command) each time that 
      an output file is closed.
    */
   public void writeEndLines () {
     templateUtil.setSkippingData (false);
     Iterator eEndLines = endLines.iterator();
     while (eEndLines.hasNext()) {
       endLine = (TemplateLine)eEndLines.next ();
       endLine.generateOutput(lastRec);
     } // end while more template end lines in vector 
   } // end writeEndLines method
   
   /**
      Gets the next line in the template file, and returns
      it as a TemplateLine.
      
      @return The next line of the template.
    */
   private TemplateLine nextTemplateLine() 
       throws IOException {
     return templateUtil.nextTemplateLine();
   } // end method nextTemplateLine
   
   /**
      Returns the name of the last output text file opened
      in the last GenerateOutput execution.</p>
      
      @return Name of the last output text file.
    */
   public FileName getTextFileOutName() {
     return templateUtil.getTextFileOutName();
   }
   
   /**
      Returns the last output text file opened
      in the last GenerateOutput execution.
     
      @return Output text file.
    */
   public TextLineWriter getTextFileOut() {
     return templateUtil.getTextFileOut();
   }
 
   /**
      Returns the number of lines found in the input 
      template file read during the last GenerateOutput operation.</p>
       
      @return templateFileLineCount
    */
   public int getTemplateFileLineCount() {
     return templateUtil.getTemplateFileLineCount();
   }
   
   /**
      Returns the number of lines found in the input 
      tabbed data file read during the last GenerateOutput operation.
     
      @return dataFileRecordCount
    */
   public int getdataFileRecordCount() {
     return dataFileRecordCount;
   }
   
   /**
      Returns the number of lines written to the last output 
      file generated during the last GenerateOutput operation.</p>
      
    @return templateUtil.textFileOutLineCount
    */
   public int getTextFileOutLineCount() {
     return templateUtil.getTextFileOutLineCount();
   }
 
   /**
      Returns this object as some sort of String.
     
      @return Name of the class plus name of the template input file.
    */
   public String toString () {
     return ("Template Text File Name is "
       + templateUtil.getTemplateFileName());
   }
 }

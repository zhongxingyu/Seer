 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xsqconvertergit;
 
 import xsqconvertergit.interfaces.*;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 
     
 
 /**
  *
  * @author root
  */
 public class OutPutWriter {
     
     private ProcessingOptions processingOptions;
     
     private Library currentLibrary;
     private String currentTileName;
     private String currentTagName;
             
     private List<Library> libraries;
     private Map<String, Integer> tagNameLengthMap;    
    
     private Long mpTagWithBarcodeReadCounter = new Long(1);
     private Long mpTagWithoutBarcodeReadCounter = new Long(1);  
     
     
    // private Long f3Counter = new Long(1);
    // private  Long r3Counter = new Long(1);   
     
     private Map<Long, String> readNrBarCodeMap;
     
     private Map<Character, Character> BWAToCSMap;
      
     
     //writer collections
     private HashMap<String, FastQWriter> matePairBarCodeSpecificWriters;
     private HashMap<String, FastQWriter> libraryAndTagSpecificWriters;   
     
     
 
     public OutPutWriter(ProcessingOptions processingOptions, Map<String, Integer> tagNameLengthMap, List<Library> libraries) {
         
          this.processingOptions = processingOptions;
          
          this.tagNameLengthMap = tagNameLengthMap;        
          this.libraries = libraries;  
          
          if(processingOptions.getMatePairBarcodeRun())
          {                          
             createMatePairBarcodeSpecificWriters(processingOptions.getMatePairBarcodeMap());             
          }
          else
          {
             createLibraryAndTagSpecificWriters();   
          }  
          
          
         BWAToCSMap = new HashMap<Character, Character>();
         BWAToCSMap.put('A', '0');
         BWAToCSMap.put('C', '1');
         BWAToCSMap.put('G', '2');
         BWAToCSMap.put('T', '3');  
         BWAToCSMap.put('N', '.');  
          
     }    
     
     
     public CSFastQEntryInterface getNewEntry()
     {
         
         CSFastQEntryInterface cSFastQEntry = null;
         
         FastQDialect fastQDialect = processingOptions.getFastQDialect();
         switch(fastQDialect)
         {
             case sanger:
                 cSFastQEntry= new SangerCSFastQEntry();
                 break;
             case bwa:
                 cSFastQEntry = new BWACSFastQEntry();
                 break;
             case csfasta:
                 cSFastQEntry = new CSFastaQualEntry();
                 break;
         }  
         
         if(processingOptions.getMatePairBarcodeRun())
         {
             if(currentTagName.equalsIgnoreCase(processingOptions.getMatePairBarcodeLocationEnum().toString()))
             {
                cSFastQEntry.setReadStartPosition(1+processingOptions.getMatePairBarCodeLength());    
             }
             else
             {
                  cSFastQEntry.setReadStartPosition(1);
             }
             
         }
         else
         {
             cSFastQEntry.setReadStartPosition(1);
         
         }
         
         cSFastQEntry.setReadLengthCutoff(processingOptions.getReadLenghtOutputCutoff());
         
         return cSFastQEntry;
     }
     
      public void writeFastQEntry(CSFastQEntryInterface fastQEntry) {
         
         
         fastQEntry.setSeqName(currentLibrary.getName()+ "_" + currentTagName+ "_"+currentTileName);        
          
         FastQWriter fastQWriter;
         
         if(processingOptions.getMatePairBarcodeRun())
         {
             
             fastQWriter = getFastQwriterBasedOnBCMPBarcode(fastQEntry);            
             
         }
         else
         {
             fastQWriter = getCurrentFastQWriter();       
         }      
                   
         
         fastQWriter.writeFastQEntry(fastQEntry, processingOptions.getFastQDialect());    
     }
 
     private String getMPBarcodeName(CSFastQEntryInterface fastQEntry) {
         
         String seq = fastQEntry.getSeq();
         
         if(processingOptions.getFastQDialect() == FastQDialect.bwa)
         {
             seq = convertBWAToCS(seq);
         }        
        
         
         List<String> barcodeNamesMatched = new ArrayList<String>();
         String barcodeName = "";
         for(String barcode : processingOptions.getMatePairBarcodeMap().keySet())
         {
             Boolean match = true;
             
             int charLocation = 0;
             int mismatches = 0;
 
             for(char c : barcode.toCharArray())
             {
                if(c != seq.charAt(charLocation++))
                {
                   mismatches++;
                }
                
                if(mismatches > processingOptions.getMPBCMismatchesAllowed())
                {
                    match = false;
                    break;            
                }
             }
             
             if(match)
             {
                 barcodeNamesMatched.add(processingOptions.getMatePairBarcodeMap().get(barcode));
             }
             
             
         }
         
         if(barcodeNamesMatched.size() == 1)
         {
             barcodeName = barcodeNamesMatched.get(0);        
         }
         else
         {
             if(barcodeNamesMatched.isEmpty())
             {
                 barcodeName = "noBarcodeMatch";          
             }
             if(barcodeNamesMatched.size() > 1)
             {
                 barcodeName = "multipleBarcodeMatch";      
             }            
         }
         
         if(barcodeName.isEmpty())
         {
             String blaat = "blaat";
         }
         
         return barcodeName;
     
     }    
     
     
     public void setCurrentLibrary(Library currentLibrary) {
         this.currentLibrary = currentLibrary;
     }
 
     public void setCurrentTileName(String currentTileName) {
         this.currentTileName = currentTileName;
     }
     
     public void resetTileReadCounters()
     {
         mpTagWithBarcodeReadCounter = new Long(1);
         mpTagWithoutBarcodeReadCounter = new Long (1);
         
         readNrBarCodeMap = new HashMap<Long, String>();
     }
 
     public void setCurrentTagName(String currentTagName) {
         this.currentTagName = currentTagName;
     }
 
     public Set<String> getTagNames()
     {
         return tagNameLengthMap.keySet();
     }
     
     public Integer getTagLength(String tagName)
     {
         return tagNameLengthMap.get(tagName);
     } 
     
     
 
     public void closeWriters() {
         
         if(processingOptions.getMatePairBarcodeRun())
         {
             for(FastQWriter fastQWriter : matePairBarCodeSpecificWriters.values())
             {                
                 fastQWriter.closeWriter();
             }       
         }
         else
         {
             for(FastQWriter fastQWriter : libraryAndTagSpecificWriters.values())
             {
                 fastQWriter.closeWriter();
             }         
         }
         
        
     }
 
     private void createLibraryAndTagSpecificWriters() {
         
         libraryAndTagSpecificWriters = new HashMap<String, FastQWriter>();
         
         for(Library library : libraries)
         {
             for(String tagName : tagNameLengthMap.keySet())
             {
                 String writerID = library.getNameAndBarCode()+ "_" +tagName;
                 String writerName;
                 
                 if(processingOptions.getUseBarcodeInOutputName())
                 {
                     writerName = library.getNameAndBarCode() + "_" + tagName;
                 }
                 else
                 {
                      writerName = library.getName() + "_" + tagName;
                 }
                 
                 FastQWriter fastQWriter = new FastQWriter(writerName, processingOptions.getOutputDir(), processingOptions.getChunkSize());
                 libraryAndTagSpecificWriters.put(writerID,fastQWriter );            
             }
         }
         
     }
     
     
 //    private String formatLibraryTagName(String libraryName, String tagName)
 //    {
 //        String[] libraryNameSplit = libraryName.split("_");
 //        libraryName = libraryNameSplit[0];
 //        
 //        String libAndTagName =  libraryName +"_"+ tagName; 
 //        return libAndTagName;
 //        
 //    }
 
     private FastQWriter getCurrentFastQWriter() {
         
         String writerID = currentLibrary.getNameAndBarCode() + "_"+ currentTagName;
         return libraryAndTagSpecificWriters.get(writerID);             
     }
     
     
     private FastQWriter getFastQWriterByLibraryNameAndBarcodeAndTag(String libraryNameAndBarcode, String tag) {
         
         String writerID = libraryNameAndBarcode + "_"+ tag;
         return libraryAndTagSpecificWriters.get(writerID);             
     }
     
     
 //     private FastQWriter getFastQwriterBasedOnBCMPBarcode(CSFastQEntryInterface fastQEntry) {
 //        
 //         FastQWriter fastQWriter;
 //         
 //         //if the tag is R3, look up the writer based on the barcode in the read
 //         if(currentTagName.equalsIgnoreCase("R3"))
 //         {
 //
 //            String barcodeName = getMPBarcodeName(fastQEntry);                
 //
 //            fastQWriter = matePairBarCodeSpecificWriters.get(barcodeName+"_R3");
 //            readNrBarCodeMap.put(r3Counter, barcodeName);
 //            r3Counter++;
 //
 //         }
 //         //if the tag is F3, look up the writer based on where the R3 mate read was written
 //         else
 //         {
 //            String barcodeName = readNrBarCodeMap.get(f3Counter);
 //            fastQWriter = matePairBarCodeSpecificWriters.get(barcodeName+"_F3");
 //            f3Counter++;
 //         }
 //         return fastQWriter;
 //    }
      
      private FastQWriter getFastQwriterBasedOnBCMPBarcode(CSFastQEntryInterface fastQEntry) {
         
          String barcodeName;
          FastQWriter fastQWriter;         
         
          //if this is the tag in which the barcode is located. Extract the barcode from the read and store read number with the barcodename. 
          //Return the fastqwriter for this barcode
          if(currentTagName.equalsIgnoreCase(processingOptions.getMatePairBarcodeLocationEnum().toString()))
          {
              barcodeName = getMPBarcodeName(fastQEntry);                
              readNrBarCodeMap.put(mpTagWithBarcodeReadCounter, barcodeName);
              mpTagWithBarcodeReadCounter++;   
          }
          //if this is the tag in which the barcode is not located. Lookup based on read number what the barcode was in the read in from the other tag     
          else
          {
              barcodeName = readNrBarCodeMap.get(mpTagWithoutBarcodeReadCounter);  
              mpTagWithoutBarcodeReadCounter++;
          }
          
          fastQWriter = matePairBarCodeSpecificWriters.get(barcodeName+"_"+currentTagName); 
          
 //         //if the tag is R3, look up the writer based on where the F3 mate read was written
 //         if(currentTagName.equalsIgnoreCase("R3"))
 //         {
 //            String barcodeName = readNrBarCodeMap.get(r3Counter);
 //            fastQWriter = matePairBarCodeSpecificWriters.get(barcodeName+"_R3"); 
 //            r3Counter++;
 //
 //         }
 //         //if the tag is F3, look up the writer based on the barcode in the read
 //         else
 //         {
 //            String barcodeName = getMPBarcodeName(fastQEntry);                
 //
 //            fastQWriter = matePairBarCodeSpecificWriters.get(barcodeName+"_F3");
 //            readNrBarCodeMap.put(f3Counter, barcodeName);
 //            f3Counter++;
 //         }
          return fastQWriter;
     } 
 
     private void createMatePairBarcodeSpecificWriters(Map<String, String> matePairBarCodeMap) {
         
         matePairBarCodeSpecificWriters = new HashMap<String, FastQWriter>();
         
         for(String barcodeName : matePairBarCodeMap.values())
         {
             if(!matePairBarCodeSpecificWriters.containsKey(barcodeName+"_F3"))
             {
                 FastQWriter f3FastQWriter = new FastQWriter(barcodeName+"_F3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
                 matePairBarCodeSpecificWriters.put(barcodeName+"_F3", f3FastQWriter);
                 
                 FastQWriter r3FastQWriter = new FastQWriter(barcodeName+"_R3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
                 matePairBarCodeSpecificWriters.put(barcodeName+"_R3", r3FastQWriter);
             }
         }
         
         FastQWriter f3NoBarcodeMatchFastQWriter = new FastQWriter("noBarcodeMatch_F3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
         f3NoBarcodeMatchFastQWriter.setResetReadStartPositionsTo0(true);
         matePairBarCodeSpecificWriters.put("noBarcodeMatch_F3", f3NoBarcodeMatchFastQWriter);
 
         FastQWriter r3NoBarcodeMatchFastQWriter = new FastQWriter("noBarcodeMatch_R3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
         r3NoBarcodeMatchFastQWriter.setResetReadStartPositionsTo0(true);
         matePairBarCodeSpecificWriters.put("noBarcodeMatch_R3", r3NoBarcodeMatchFastQWriter);
         
         FastQWriter f3MultipleBarcodeMatchFastQWriter = new FastQWriter("multipleBarcodeMatch_F3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
         f3MultipleBarcodeMatchFastQWriter.setResetReadStartPositionsTo0(true);
         matePairBarCodeSpecificWriters.put("multipleBarcodeMatch_F3", f3MultipleBarcodeMatchFastQWriter);
 
         FastQWriter r3MultipleBarcodeMatchFastQWriter = new FastQWriter("multipleBarcodeMatch_R3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
         r3MultipleBarcodeMatchFastQWriter.setResetReadStartPositionsTo0(true);
         matePairBarCodeSpecificWriters.put("multipleBarcodeMatch_R3", r3MultipleBarcodeMatchFastQWriter);
         
         
         
     }
 
     private String convertBWAToCS(String seq) {
        
         StringBuilder CSSeq  = new StringBuilder(seq.length());
         
         for (int i = 0; i < seq.length(); i++)
         {
             char c = seq.charAt(i);        
             CSSeq.append(BWAToCSMap.get(c));
             
         }
         
         return CSSeq.toString();
         
     }
 
     public Boolean checkExistingOutput(Library library) {
         
         Boolean existingOutput = false;
         
         if(processingOptions.getMatePairBarcodeRun())
         {                          
             if(matePairBarCodeSpecificWriters.values().iterator().next().checkForExistingOutput())
             {
                 existingOutput = true;
             }       
         }
         else
         {
             for(String tagName : tagNameLengthMap.keySet())
             {
                 String writerID = library.getNameAndBarCode() + "_"+ tagName;
                 if(libraryAndTagSpecificWriters.get(writerID).checkForExistingOutput())
                 {
                     existingOutput = true;
                 }
                 
             }
         } 
         
         return existingOutput;
         
     }
 
     public Collection<FastQWriter> getFastQWriters() {
         
         Collection<FastQWriter> fastQWriters = null;
         if(processingOptions.getMatePairBarcodeRun())
         {
             fastQWriters = matePairBarCodeSpecificWriters.values();
         }
         else
         {
             fastQWriters = libraryAndTagSpecificWriters.values();
         }
         
         return fastQWriters;
         
         
         
     }
 
     public void removeExistingOutput() {
         
         for(File exitingOutputFileOrDir : processingOptions.getOutputDir().listFiles())
         {
             exitingOutputFileOrDir.delete();
         }
     }
 
     public SortedMap getMetrics() {
         
         SortedMap metricsMap = new TreeMap<String, String>();
         
         for(FastQWriter fastQWriter: getFastQWriters())        {
                        
            metricsMap.put(fastQWriter.getWriterId(), Long.toString(fastQWriter.getReadCounter()) );             
            
            System.out.println("Processed tag "+ fastQWriter.getWriterId() + " with "+fastQWriter.getReadCounter()+" reads");  
         }
         
         return metricsMap;
         
     }
 
     public void removeFastQWriters(Library library) {
         
         for(String tag : tagNameLengthMap.keySet())
         {
             String writerId = library.getNameAndBarCode()+"_"+tag;
             
             if(processingOptions.getMatePairBarcodeRun())
             {
                 matePairBarCodeSpecificWriters.remove(writerId);
             }
             else
             {
                 libraryAndTagSpecificWriters.remove(writerId);
             }           
         }
         
     }
 
     public void openFastQFilesForWriting() {
         for(FastQWriter fastQWriter :getFastQWriters())
         {
             fastQWriter.openFastQFileForWriting(processingOptions.getFastQDialect());
         }
     }
 
     public void addFastQFilesToLibrary(List<Library> libraries) {
         
         
         if(processingOptions.getMatePairBarcodeRun()){return;}
         
         for(Library library : libraries)
         {        
             for(String tag : tagNameLengthMap.keySet())
             {
                 FastQWriter fastQWriter = getFastQWriterByLibraryNameAndBarcodeAndTag(library.getNameAndBarCode(), tag);
                 List<File> fastqFiles = fastQWriter.getWrittenFiles();
                 library.setWrittenFiles(tag, fastqFiles);
                 
             }
         }  
     }
 
     public ProcessingOptions getProcessingOptions() {
         return processingOptions;
     }
 
     public void setProcessingOptions(ProcessingOptions processingOptions) {
         this.processingOptions = processingOptions;
     }
     
     
     
     
 
     
 
    
 
    
     
     
     
      
      
     
     
     
     
     
     
     
     
 }

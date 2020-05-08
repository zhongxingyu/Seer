 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package xsqconvertergit;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
 import org.apache.commons.cli.*;
 import org.apache.commons.io.FilenameUtils;
 
 
 
 
 /**
  *
  * @author Wim Spee
  */
 public class XSQConverterGit {
 
         
     //private static File outputDir = null;  
     
     private static String version = "v10:03-07-2013";
        
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         
                 
         Options options = new Options();
         options.addOption("i","input", true, "XSQ input file path.");
         options.addOption("o","output", true, "output directory path");
         options.addOption("c","chunk", true, "output fastq chunksize. Default is 1000000");
         options.addOption("l","library", true, "library which should be converted. For multiple libraries use this argument multiple times");
         options.addOption("b","barcode", true, "barcodes which should be converted. For multiple barcodes use this argument multiple times");
         options.addOption("d","display", false, "display all libraries names and quit without processing");
         options.addOption("j","javapath", false, "display Java path");
         options.addOption("f","fastq-dialect", true, "fastQ dialect / format. Either BWA, Sanger or csfasta." );        
         options.addOption("m","matepair-barcode-file", true, "file with matepair bacodes. Each line should contains a barcode color space sequence and a barcode name, separated with a tab. All barcodes are required to have the same length. A output file is created for every barcode name and tag combination.  ");
         options.addOption("n","matepair-barcode-mismatches", true, "number of mismatches to be allowed for matching matepair barcodes. Default is 0.");
         options.addOption("mpbl", true, "The location of the matepair barcode sequence. Either F3 or R3. Default is F3");
         options.addOption("h","help", false, "print this message");
         options.addOption("w","overwrite", false, "overwrite existing output. By default libraries for which existing output is present are skipped. ");
         options.addOption("u","use-barcode-name", false, "use barcode in the output names. Should always be used when processing multiple unassigned libraries by barcode because they have the same name.");
         options.addOption("x","read-lenght-cutoff", true, "Only output reads untill this cutoff. Works on all tags.");
         options.addOption("t", false, "Add the leading base and color call. BWA and Bowtie do not use these but other mappers do. Not yet available for mate pair barcode runs. Can only be used for output in Sanger or csfasta format.");
         
         ProcessingOptions processingOptions = new ProcessingOptions();
         
         CommandLineParser parser = new GnuParser();
         CommandLine cmd = null;
         try {
             cmd = parser.parse( options, args);
         } catch (ParseException ex) {
             Logger.getLogger(XSQConverterGit.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         if(cmd.hasOption("j")){ printJavaPath(); System.exit(0); }
         if(cmd.hasOption("h")){ printHelp(options); }
         
         if(cmd.hasOption("d"))
         { 
             if(!cmd.hasOption("i")){printHelp(options);}
             String xsqFilePath = cmd.getOptionValue("i"); 
             XSQFile xSQFile = new XSQFile(xsqFilePath); 
             try {
                 xSQFile.printAllRootSubGroups();
             } catch (HDF5Exception ex) {
                 ex.printStackTrace();
             }            
             
             System.exit(0);
         }             
         
         
         if(!requiredOptionsAreSet(cmd))
         {
             System.out.println("Not all required options are set");
             printHelp(options);           
         }         
         
         if(cmd.hasOption("m"))
         {
             Map<String, String> matePairBarCodeMap = readMatePairBarCodes(cmd);
             Integer MPBCMismatchesAllowed = new Integer(cmd.getOptionValue("n", "0"));
             
             processingOptions.setMatePairBarcodeMap(matePairBarCodeMap);
             processingOptions.setMatePairBarcodeRun(true);
             processingOptions.setMPBCMismatchesAllowed(MPBCMismatchesAllowed);
             processingOptions.setMatePairBarCodeLength(matePairBarCodeMap.keySet().iterator().next().length());  
             
             MatePairBarcodeLocationEnum matePairBarcodeLocationEnum = MatePairBarcodeLocationEnum.valueOf(cmd.getOptionValue("mpbl", "F3"));
             processingOptions.setMatePairBarcodeLocationEnum(matePairBarcodeLocationEnum);
         }        
         
         if(cmd.hasOption("u"))
         {
             processingOptions.setUseBarcodeInOutputName(true);
         } 
         
         processingOptions.setFastQDialect(FastQDialect.valueOf(cmd.getOptionValue("f").toLowerCase()));        
         
         
         if(cmd.hasOption("t"))
         {
             if(processingOptions.getFastQDialect().equals(FastQDialect.bwa))
             {
                 System.out.println("Can't output leading base and color call for BWA output because of double encoding of color calls. ");
                 System.exit(1);
             }
             else
             {
                 processingOptions.setOutputLeadingBaseAndColorCall1(true);
             }  
         }
         
                      
         processingOptions.setChunkSize( new Long(cmd.getOptionValue("c", "1000000")));     
         
         String xsqFilePath = cmd.getOptionValue("i"); 
         processingOptions.setXSQFile(new File(xsqFilePath));
         
         String outputPath = cmd.getOptionValue("o");
         File outputDir = createOutputDir(outputPath,xsqFilePath);
         processingOptions.setOutputDir(outputDir);  
         
 
         Map<String, String> librariesSubSet = getSpecifiedLibrarySubSet(cmd);
         if(!librariesSubSet.isEmpty())
         {
             processingOptions.setLibraryNameSubset(true);
             processingOptions.setLibraryNamesSubsetList(librariesSubSet);
         }        
         
         Map<Integer, Integer> barcodeSubset = getSpecifiedBarcodeSubSet(cmd);       
         if(!barcodeSubset.isEmpty())
         {
             processingOptions.setBarCodeSubset(true);
             processingOptions.setBarcodesSubsetList(barcodeSubset);
         }
         
         Integer readLenghtCutoff = new Integer(cmd.getOptionValue("x", "1000000000"));
         processingOptions.setReadLenghtOutputCutoff(readLenghtCutoff);
         
         System.out.println("Input file = " + xsqFilePath);
         System.out.println("Output directory = " + outputPath);
          
          
         
         
         
         
         XSQFile xSQFile = new XSQFile(xsqFilePath);
         try {
             xSQFile.processXSQFile(processingOptions);
         } catch (Exception ex) {
            ex.printStackTrace();
         }
                 
         
     }
     
     /**
      * print the command line help options
      * @param options 
      */
     private static void printHelp(Options options)
     {
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp( "XSQ converter version "+version+"\n. By default converts all XSQ libraries into CSFastQ files except the Unassigned_* and the Unclassified library. Options -i and -o are required.", options );
         System.exit(1);
     }
     
     /**
      * check if all the required options where set at the command line
      * @param cmd
      * @return 
      */
     private static Boolean requiredOptionsAreSet(CommandLine cmd)
     {
         Boolean requiredOptionsAreSet = true;
         
         if(!cmd.hasOption("i")){ requiredOptionsAreSet = false; }
         if(!cmd.hasOption("o")){ requiredOptionsAreSet = false; }  
         if(!cmd.hasOption("f")){ requiredOptionsAreSet = false; }
         else
         {
             if(!fastQformatIsSpecified(cmd)){ requiredOptionsAreSet = false;}
         }  
         
         return requiredOptionsAreSet;
         
     }
     
     private static boolean fastQformatIsSpecified(CommandLine cmd) {
         Boolean fastQFormatIsSet = false;
         
         String givenfastQDialect = cmd.getOptionValue("f");
         
         for(FastQDialect fastQDialect : FastQDialect.values())
         {
             if(givenfastQDialect.equalsIgnoreCase(fastQDialect.toString())){fastQFormatIsSet = true;}
         }
        
         return fastQFormatIsSet;
         
     }
     
     /**
      * Validate the output directory path and if it is valid path create the output directory. 
      * @param outputPath The path to create. 
      */
     private static File createOutputDir(String outputPath, String xsqFilePath) {
         
         File xsqFile = new File(xsqFilePath);
         if(!xsqFile.canRead())
         {
             System.out.println("XSQ file "+ xsqFilePath + " could not be read.");
             System.exit(1);  
         }      
         
         
         File baseOutputDir = new File(outputPath);  
         
         if(!baseOutputDir.exists())
         {
             if(!baseOutputDir.mkdirs())
             {
                 System.out.println("Output directory "+ baseOutputDir + " could not be created.");
                 System.exit(1);                
             }
            
         }
         else
         {
             if(!baseOutputDir.isDirectory())
             {
                 System.out.println("Output directory "+ baseOutputDir + " is not a directory.");
                 System.exit(1);   
             } 
         }
         
         String fileNameWithExt = xsqFile.getName();
                 
         String xsqName = FilenameUtils.removeExtension(fileNameWithExt);       
         
         File outputDir = new File(baseOutputDir, xsqName);
         outputDir.mkdir();
         return outputDir;        
         
         
         
     }
     
     /**
      * print the Java Path. The native libraries should be placed in one of the directories listed. 
      */
     private static void printJavaPath()
     {
         String path = System.getProperty("java.library.path");
         
         System.out.println("The Java Path = ");      
         System.out.println(path);
     }
 
     private static Map<String, String> getSpecifiedLibrarySubSet(CommandLine cmd) {        
         
         Map<String, String> librariesSubSet = new HashMap<String, String>();
         if(cmd.hasOption("l"))
         {
             for(Object object :cmd.getOptionValues("l"))
             {
                 librariesSubSet.put(object.toString(), null);                
             } 
         }
         return librariesSubSet;
     }
 
     private static Map<Integer, Integer> getSpecifiedBarcodeSubSet(CommandLine cmd) {
         
         Map<Integer, Integer> barcodes = new HashMap<Integer, Integer>();
         if(cmd.hasOption("b"))
         {
             for(Object object :cmd.getOptionValues("b"))
             {
                 barcodes.put(new Integer(object.toString()), null);                
             } 
         }         
         return barcodes;
     }
 
     private static Map<String, String> readMatePairBarCodes(CommandLine cmd) {
         File matePairBarCodeFile = new File(cmd.getOptionValue("m"));
         
          Map<String, String> matePairBarCodeMap = new HashMap<String, String>();
         
         if(!matePairBarCodeFile.canRead())
         {
             System.out.println("Can't read matePairBarCode file "+ matePairBarCodeFile.getPath());
             System.exit(1);                    
         }
         try {
             BufferedReader in = new BufferedReader(new FileReader(matePairBarCodeFile));
             
             System.out.println ("Reading barcodes");
             System.out.println ("Barcode\tbarcodeName");
             
             String line;
             while ((line = in.readLine()) != null)   
             {
                 String[] splitLine = line.split("\t");
                 String barCodeSequence =  splitLine[0];
                 String barCodeName =  splitLine[1];
                 
                 matePairBarCodeMap.put(barCodeSequence, barCodeName);
                 
                 
                 // Print the content on the console
                 System.out.println (line);
             }
             
         } catch (Exception ex) {
            System.out.println("Can't read matePairBarCode file "+ matePairBarCodeFile.getPath());
             System.exit(1);       
         }
         
         if(matePairBarCodeMap.isEmpty())
         {
             System.out.println("No 'barcode TAB barcodeName' entries found in  "+ matePairBarCodeFile.getPath());
             System.exit(1);     
         }
         
         return matePairBarCodeMap;        
         
     }
 
     
     
   
    
 }

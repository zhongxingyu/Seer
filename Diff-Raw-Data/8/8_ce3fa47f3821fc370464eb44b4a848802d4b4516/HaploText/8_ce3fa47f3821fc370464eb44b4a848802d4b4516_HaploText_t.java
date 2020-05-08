 package edu.mit.wi.haploview;
 
 import edu.mit.wi.pedfile.MarkerResult;
 import edu.mit.wi.pedfile.PedFileException;
 import edu.mit.wi.pedfile.CheckData;
 import edu.mit.wi.haploview.TreeTable.HaplotypeAssociationNode;
 
 import java.io.*;
 import java.util.Vector;
 import java.util.StringTokenizer;
 import java.awt.image.BufferedImage;
 
 import com.sun.jimi.core.Jimi;
 import com.sun.jimi.core.JimiException;
 
 public class HaploText implements Constants{
 
     private boolean nogui = false;
     private String batchFileName;
     private String hapsFileName;
     private String infoFileName;
     private String pedFileName;
     private String hapmapFileName;
     private String blockFileName;
     private String trackFileName;
     private boolean skipCheck = false;
     private Vector excludedMarkers = new Vector();
     private boolean quietMode = false;
     private int blockOutputType;
     private boolean outputCheck;
     private boolean outputDprime;
     private boolean outputPNG;
     private boolean outputCompressedPNG;
 
     public boolean isNogui() {
         return nogui;
     }
 
     public String getBatchMode() {
         return batchFileName;
     }
 
     public String getHapsFileName() {
         return hapsFileName;
     }
 
     public String getPedFileName() {
         return pedFileName;
     }
 
     public String getInfoFileName(){
         return infoFileName;
     }
 
     public String getHapmapFileName(){
         return hapmapFileName;
     }
 
     public int getBlockOutputType() {
         return blockOutputType;
     }
 
     private double getDoubleArg(String[] args, int valueIndex, String argName, double min, double max) {
         double argument = 0;
         if(valueIndex>=args.length || ((args[valueIndex].charAt(0)) == '-')) {
             System.out.println( argName + " requires a value between " + min + " and " + max);
             System.exit(1);
         }
         try {
             argument = Double.parseDouble(args[valueIndex]);
             if(argument<min || argument>max) {
                 System.out.println(argName + " requires a value between " + min + " and " + max);
                 System.exit(1);
             }
         }catch(NumberFormatException nfe) {
             System.out.println(argName + " requires a value between " + min + " and " + max);
             System.exit(1);
         }
         return argument;
     }
 
 
     public HaploText(String[] args) {
         this.argHandler(args);
 
         if(this.batchFileName != null) {
             System.out.println(TITLE_STRING);
             this.doBatch();
         }
 
         if(!(this.pedFileName== null) || !(this.hapsFileName== null) || !(this.hapmapFileName== null)){
             if(nogui){
                 System.out.println(TITLE_STRING);
                 processTextOnly();
             }
         }
 
     }
 
     private void argHandler(String[] args){
 
         int maxDistance = -1;
         //this means that user didn't specify any output type if it doesn't get changed below
         blockOutputType = -1;
         double hapThresh = -1;
         double minimumMAF=-1;
         double spacingThresh = -1;
         double minimumGenoPercent = -1;
         double hwCutoff = -1;
         double missingCutoff = -1;
         int maxMendel = -1;
         boolean assocTDT = false;
         boolean assocCC = false;
 
 
         for(int i =0; i < args.length; i++) {
             if(args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-h")) {
                 System.out.println(HELP_OUTPUT);
                 System.exit(0);
             }
             else if(args[i].equals("-n") || args[i].equals("-nogui")) {
                 nogui = true;
             }
             else if(args[i].equals("-p") || args[i].equals("-pedfile")) {
                 i++;
                 if( i>=args.length || (args[i].charAt(0) == '-')){
                     System.out.println(args[i-1] + " requires a filename");
                     System.exit(1);
                 }
                 else{
                     if(pedFileName != null){
                         System.out.println("multiple "+args[i-1] + " arguments found. only last pedfile listed will be used");
                     }
                     pedFileName = args[i];
                 }
             }
             else if (args[i].equals("-skipcheck") || args[i].equals("--skipcheck")){
                 skipCheck = true;
             }
             else if (args[i].equalsIgnoreCase("-excludeMarkers")){
                 i++;
                 if(i>=args.length || (args[i].charAt(0) == '-')){
                     System.out.println("-excludeMarkers requires a list of markers");
                     System.exit(1);
                 }
                 else {
                     StringTokenizer str = new StringTokenizer(args[i],",");
                     try {
                         while(str.hasMoreTokens()) {
                             String token = str.nextToken();
                             if(token.indexOf("..") != -1) {
                                 int lastIndex = token.indexOf("..");
                                 int rangeStart = Integer.parseInt(token.substring(0,lastIndex));
                                 int rangeEnd = Integer.parseInt(token.substring(lastIndex+2,token.length()));
                                 for(int j=rangeStart;j<=rangeEnd;j++) {
                                     excludedMarkers.add(new Integer(j));
                                 }
                             } else {
                                 excludedMarkers.add(new Integer(token));
                             }
                         }
                     } catch(NumberFormatException nfe) {
                         System.out.println("-excludeMarkers argument should be of the format: 1,3,5..8,12");
                         System.exit(1);
                     }
                 }
             }
             else if(args[i].equals("-ha") || args[i].equals("-l") || args[i].equals("-haps")) {
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     System.out.println(args[i-1] + " requires a filename");
                     System.exit(1);
                 }
                 else{
                     if(hapsFileName != null){
                         System.out.println("multiple "+args[i-1] + " arguments found. only last haps file listed will be used");
                     }
                     hapsFileName = args[i];
                 }
             }
             else if(args[i].equals("-i") || args[i].equals("-info")) {
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     System.out.println(args[i-1] + " requires a filename");
                     System.exit(1);
                 }
                 else{
                     if(infoFileName != null){
                         System.out.println("multiple "+args[i-1] + " arguments found. only last info file listed will be used");
                     }
                     infoFileName = args[i];
                 }
             } else if (args[i].equals("-a") || args[i].equals("-hapmap")){
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     System.out.println(args[i-1] + " requires a filename");
                     System.exit(1);
                 }
                 else{
                     if(hapmapFileName != null){
                         System.out.println("multiple "+args[i-1] + " arguments found. only last hapmap file listed will be used");
                     }
                     hapmapFileName = args[i];
                 }
             }
             else if(args[i].equals("-k") || args[i].equals("-blocks")) {
                 i++;
                 if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                     blockFileName = args[i];
                     blockOutputType = BLOX_CUSTOM;
                 }else{
                     System.out.println(args[i-1] + " requires a filename");
                     System.exit(1);
                 }
             }
             else if (args[i].equalsIgnoreCase("-png")){
                 outputPNG = true;
             }
             else if (args[i].equalsIgnoreCase("-smallpng") || args[i].equalsIgnoreCase("-compressedPNG")){
                 outputCompressedPNG = true;
             }
             else if (args[i].equals("-track")){
                 i++;
                 if (!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                    trackFileName = args[i];
                 }else{
                     System.out.println("-track requires a filename");
                     System.exit(1);
                 }
             }
             else if(args[i].equals("-o") || args[i].equals("-output") || args[i].equalsIgnoreCase("-blockoutput")) {
                 i++;
                 if(!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                     if(blockOutputType != -1){
                         System.out.println("only one output argument is allowed");
                         System.exit(1);
                     }
                     if(args[i].equalsIgnoreCase("SFS") || args[i].equalsIgnoreCase("GAB")){
                         blockOutputType = BLOX_GABRIEL;
                     }
                     else if(args[i].equalsIgnoreCase("GAM")){
                         blockOutputType = BLOX_4GAM;
                     }
                     else if(args[i].equalsIgnoreCase("MJD") || args[i].equalsIgnoreCase("SPI")){
                         blockOutputType = BLOX_SPINE;
                     }
                     else if(args[i].equalsIgnoreCase("ALL")) {
                         blockOutputType = BLOX_ALL;
                     }
                 }
                 else {
                     //defaults to SFS output
                     blockOutputType = BLOX_GABRIEL;
                     i--;
                 }
             }
             else if(args[i].equals("-d") || args[i].equals("--dprime") || args[i].equals("-dprime")) {
                 outputDprime = true;
             }
             else if (args[i].equals("-c") || args[i].equals("-check")){
                 outputCheck = true;
             }
             else if(args[i].equals("-m") || args[i].equals("-maxdistance")) {
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     System.out.println(args[i-1] + " requires an integer argument");
                     System.exit(1);
                 }
                 else {
                     if(maxDistance != -1){
                         System.out.println("only one "+args[i-1] + " argument allowed");
                         System.exit(1);
                     }
                     try {
                         maxDistance = Integer.parseInt(args[i]);
                         if(maxDistance<0){
                             System.out.println(args[i-1] + " argument must be a positive integer");
                             System.exit(1);
                         }
                     } catch(NumberFormatException nfe) {
                         System.out.println(args[i-1] + " argument must be a positive integer");
                         System.exit(1);
                     }
                 }
             }
             else if(args[i].equals("-b") || args[i].equals("-batch")) {
                 //batch mode
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     System.out.println(args[i-1] + " requires a filename");
                     System.exit(1);
                 }
                 else{
                     if(batchFileName != null){
                         System.out.println("multiple " + args[i-1] +  " arguments found. only last batch file listed will be used");
                     }
                     batchFileName = args[i];
                 }
             }
             else if(args[i].equals("-hapthresh")) {
                 i++;
                 hapThresh = getDoubleArg(args,i,"-hapthresh",0,1);
             }
             else if(args[i].equals("-spacing")) {
                 i++;
                 spacingThresh = getDoubleArg(args,i,"-spacing",0,1);
             }
             else if(args[i].equalsIgnoreCase("-minMAF")) {
                 i++;
                 minimumMAF = getDoubleArg(args,i,"-minMAF",0,0.5);
             }
             else if(args[i].equalsIgnoreCase("-minGeno") || args[i].equalsIgnoreCase("-minGenoPercent")) {
                 i++;
                 minimumGenoPercent = getDoubleArg(args,i,"-minGeno",0,1);
             }
             else if(args[i].equalsIgnoreCase("-hwcutoff")) {
                i++;
                 hwCutoff = getDoubleArg(args,i,"-hwcutoff",0,1);
             }
             else if(args[i].equals("-maxMendel") ) {
                 i++;
                 if(i>=args.length || ((args[i].charAt(0)) == '-')){
                     System.out.println("-maxMendel requires an integer argument");
                     System.exit(1);
                 }
                 else {
                     try {
                         maxMendel = Integer.parseInt(args[i]);
                         if(maxMendel<0){
                             System.out.println("-maxMendel argument must be a positive integer");
                             System.exit(1);
                         }
                     } catch(NumberFormatException nfe) {
                         System.out.println("-maxMendel argument must be a positive integer");
                         System.exit(1);
                     }
                 }
             }
             else if(args[i].equalsIgnoreCase("-missingcutoff")) {
                 i++;
                 missingCutoff = getDoubleArg(args,i,"-missingCutoff",0,1);
             }
             else if(args[i].equalsIgnoreCase("-assoctdt")) {
                 assocTDT = true;
             }
             else if(args[i].equalsIgnoreCase("-assoccc")) {
                 assocCC = true;
             }
                      
             else if(args[i].equalsIgnoreCase("-ldcolorscheme")) {
                 i++;
                 if(!(i>=args.length) && !((args[i].charAt(0)) == '-')){
                     if(args[i].equalsIgnoreCase("default")){
                         Options.setLDColorScheme(STD_SCHEME);
                     }
                     else if(args[i].equalsIgnoreCase("RSQ")){
                         Options.setLDColorScheme(RSQ_SCHEME);
                     }
                     else if(args[i].equalsIgnoreCase("DPALT") ){
                         Options.setLDColorScheme(WMF_SCHEME);
                     }
                     else if(args[i].equalsIgnoreCase("GAB")) {
                         Options.setLDColorScheme(GAB_SCHEME);
                     }
                     else if(args[i].equalsIgnoreCase("GAM")) {
                         Options.setLDColorScheme(GAM_SCHEME);
                     }
                 }
                 else {
                     //defaults to STD color scheme
                     Options.setLDColorScheme(STD_SCHEME);
                     i--;
                 }
             }
 
 
             else if(args[i].equals("-q") || args[i].equals("-quiet")) {
                 quietMode = true;
             }
             else {
                 System.out.println("invalid parameter specified: " + args[i]);
                 System.exit(1);
             }
         }
 
         int countOptions = 0;
         if(pedFileName != null) {
             countOptions++;
         }
         if(hapsFileName != null) {
             countOptions++;
         }
         if(hapmapFileName != null) {
             countOptions++;
         }
         if(batchFileName != null) {
             countOptions++;
         }
         if(countOptions > 1) {
             System.out.println("Only one genotype input file may be specified on the command line.");
             System.exit(1);
         }
         else if(countOptions == 0 && nogui) {
             System.out.println("You must specify a genotype input file.");
             System.exit(1);
         }
 
         //mess with vars, set defaults, etc
 
         if( blockOutputType == -1 && ( pedFileName != null ||
                 hapsFileName != null || batchFileName != null || hapmapFileName != null)
                 && !outputDprime && !outputCheck && !outputPNG && !outputCompressedPNG) {
             blockOutputType = BLOX_GABRIEL;
             if(nogui && !quietMode) {
                 System.out.println("No output type specified. Default of Gabriel will be used");
             }
         }
         if(skipCheck && !quietMode) {
             System.out.println("Skipping genotype file check");
         }
         if(maxDistance == -1){
             maxDistance = 500;
         }
 
         Options.setMaxDistance(maxDistance);
 
         if(hapThresh != -1) {
             Options.setHaplotypeDisplayThreshold((int)(hapThresh*100));
         }
         
         if(minimumMAF != -1) {
             CheckData.mafCut = minimumMAF;
         }
 
         if(minimumGenoPercent != -1) {
             CheckData.failedGenoCut = (int)(minimumGenoPercent*100);
         }
 
         if(hwCutoff != -1) {
             CheckData.hwCut = hwCutoff;
         }
 
         if(maxMendel != -1) {
             CheckData.numMendErrCut = maxMendel;
         }
 
         if(spacingThresh != -1) {
             Options.setSpacingThreshold(spacingThresh);
         }
 
         if(missingCutoff != -1) {
             Options.setMissingThreshold(missingCutoff);
         }
 
         if(assocTDT) {
             Options.setAssocTest(ASSOC_TRIO);
         }
         else if(assocCC) {
             Options.setAssocTest(ASSOC_CC);
         }
 
 
     }
 
 
     private void doBatch() {
         Vector files;
         File batchFile;
         File dataFile;
         String line;
         StringTokenizer tok;
         String infoMaybe ="";
 
         files = new Vector();
         if(batchFileName == null) {
             return;
         }
         batchFile = new File(this.batchFileName);
 
         if(!batchFile.exists()) {
             System.out.println("batch file " + batchFileName + " does not exist");
             System.exit(1);
         }
 
         try {
             BufferedReader br = new BufferedReader(new FileReader(batchFile));
             while( (line = br.readLine()) != null ) {
                 files.add(line);
             }
             br.close();
 
             for(int i = 0;i<files.size();i++){
                 line = (String)files.get(i);
                 tok = new StringTokenizer(line);
                 infoMaybe = "";
                 if(tok.hasMoreTokens()){
                     dataFile = new File(tok.nextToken());
                     if(tok.hasMoreTokens()){
                         infoMaybe = tok.nextToken();
                     }
 
                     if(dataFile.exists()) {
                         String name = dataFile.getName();
                         if( name.substring(name.length()-4,name.length()).equals(".ped") ) {
                             processFile(name,PED,infoMaybe);
                         }
                         else if(name.substring(name.length()-5,name.length()).equals(".haps")) {
                             processFile(name,HAPS,infoMaybe);
                         }
                         else if(name.substring(name.length()-4,name.length()).equals(".hmp")){
                             processFile(name,HMP,"");
                         }
                         else{
                             if (!quietMode){
                                 System.out.println("Filenames in batch file must end in .ped, .haps or .hmp\n" +
                                         name + " is not properly formatted.");
                             }
                         }
                     }
                     else {
                         if(!quietMode){
                             System.out.println("file " + dataFile.getName() + " listed in the batch file could not be found");
                         }
                     }
                 }
 
             }
         }
         catch(FileNotFoundException e){
             System.out.println("the following error has occured:\n" + e.toString());
         }
         catch(IOException e){
             System.out.println("the following error has occured:\n" + e.toString());
         }
 
     }
 
     private File validateOutputFile(String fn){
         File f = new File(fn);
         if (f.exists() && !quietMode){
             System.out.println("File " + f.getName() + " already exists and will be overwritten.");
         }
         return f;
     }
 
 
     /**
      * this method finds haplotypes and caclulates dprime without using any graphics
      */
     private void processTextOnly(){
         String fileName;
         int fileType;
         if(hapsFileName != null) {
             fileName = hapsFileName;
             fileType = HAPS;
         }
         else if (pedFileName != null){
             fileName = pedFileName;
             fileType = PED;
         }else{
             fileName = hapmapFileName;
             fileType = HMP;
         }
 
         processFile(fileName,fileType,infoFileName);
     }
     /**
      * this
      * @param fileName name of the file to process
      * @param fileType true means pedfilem false means hapsfile
      * @param infoFileName
      */
     private void processFile(String fileName, int fileType, String infoFileName){
         try {
             HaploData textData;
             File OutputFile;
             File inputFile;
 
             if(!quietMode && fileName != null){
                 System.out.println("Using data file " + fileName);
             }
 
             inputFile = new File(fileName);
             if(!inputFile.exists()){
                 System.out.println("input file: " + fileName + " does not exist");
                 System.exit(1);
             }
 
             textData = new HaploData();
             Vector result = null;
 
             if(fileType == HAPS){
                 //read in haps file
                 textData.prepareHapsInput(inputFile);
             }
             else if (fileType == PED) {
                 //read in ped file
                 result = textData.linkageToChrom(inputFile, PED);
 
                 if(textData.getPedFile().isBogusParents()) {
                     System.out.println("Error: One or more individuals in the file reference non-existent parents.\nThese references have been ignored.");
                 }
             }else{
                 //read in hapmapfile
                 result = textData.linkageToChrom(inputFile,HMP);
             }
 
             //once check has been run, but before anything else, we can filter the markers
             boolean[] markerResults = new boolean[result.size()];
             for (int i = 0; i < result.size(); i++){
                 if (((MarkerResult)result.get(i)).getRating() > 0 || skipCheck){
                     markerResults[i] = true;
                 }else{
                     markerResults[i] = false;
                 }
             }
             for (int i = 0; i < excludedMarkers.size(); i++){
                 int cur = ((Integer)excludedMarkers.elementAt(i)).intValue();
                 if (cur < 1 || cur > markerResults.length){
                     System.out.println("Excluded marker out of bounds has been ignored: " + cur +
                             "\nMarkers must be between 1 and N, where N is the total number of markers.");
                     System.exit(1);
                 }else{
                     markerResults[cur-1] = false;
                 }
             }
 
             Chromosome.doFilter(markerResults);
 
             File infoFile = null;
             if (infoFileName != null){
                 infoFile = new File(infoFileName);
             }
             if (result != null){
                 textData.prepareMarkerInput(infoFile,textData.getPedFile().getHMInfo());
             }else{
                 textData.prepareMarkerInput(infoFile,null);
             }
             if(!quietMode && infoFile != null){
                 System.out.println("Using marker file " + infoFile.getName());
             }
             if(outputCheck && result != null){
                 CheckDataPanel cp = new CheckDataPanel(textData);
                 cp.printTable(validateOutputFile(fileName + ".CHECK"));
             }
             Vector cust = new Vector();
             if(blockOutputType != -1){
                 textData.generateDPrimeTable();
                 Haplotype[][] haplos;
                 Haplotype[][] filtHaplos;
                 switch(blockOutputType){
                     case BLOX_GABRIEL:
                         OutputFile = validateOutputFile(fileName + ".GABRIELblocks");
                         break;
                     case BLOX_4GAM:
                         OutputFile = validateOutputFile(fileName + ".4GAMblocks");
                         break;
                     case BLOX_SPINE:
                         OutputFile = validateOutputFile(fileName + ".SPINEblocks");
                         break;
                     case BLOX_CUSTOM:
                         OutputFile = validateOutputFile(fileName + ".CUSTblocks");
                         //read in the blocks file
                         File blocksFile = new File(blockFileName);
                         cust = textData.readBlocks(blocksFile);
                         break;
                    case BLOX_ALL:
                        //handled below, so we don't do anything here
                        OutputFile = null;
                        break;
                     default:
                         OutputFile = validateOutputFile(fileName + ".GABRIELblocks");
                         break;
 
                 }
 
                 //this handles output type ALL
                 if(blockOutputType == BLOX_ALL) {
                     OutputFile = validateOutputFile(fileName + ".GABRIELblocks");
                     textData.guessBlocks(BLOX_GABRIEL);
                     haplos = textData.generateHaplotypes(textData.blocks, false);
                     filtHaplos = filterHaplos(haplos);
                     textData.pickTags(filtHaplos);
                     textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), OutputFile);
                     OutputFile = validateOutputFile(fileName + ".4GAMblocks");
                     textData.guessBlocks(BLOX_4GAM);
                     haplos = textData.generateHaplotypes(textData.blocks, false);
                     filtHaplos = filterHaplos(haplos);
                     textData.pickTags(filtHaplos);
                     textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), OutputFile);;
                     OutputFile = validateOutputFile(fileName + ".SPINEblocks");
                     textData.guessBlocks(BLOX_SPINE);
                     haplos = textData.generateHaplotypes(textData.blocks, false);
                     filtHaplos = filterHaplos(haplos);
                     textData.pickTags(filtHaplos);
                     textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), OutputFile);
                 }else{
                     //guesses blocks based on output type determined above.
                     textData.guessBlocks(blockOutputType, cust);
                     haplos = textData.generateHaplotypes(textData.blocks, false);
                     filtHaplos = filterHaplos(haplos);
                     textData.pickTags(filtHaplos);
                     textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), OutputFile);
                 }
 
                 if(Options.getAssocTest() == ASSOC_TRIO || Options.getAssocTest() == ASSOC_CC) {
                     if (blockOutputType == BLOX_ALL){
                        System.out.println("Haplotype association results cannot be used with block output \"ALL\"");
                     }else{
                         HaploData.saveHapAssocToText(haplos, fileName + ".HAPASSOC");
                     }
                 }
             }
 
             if(outputDprime) {
                 OutputFile = validateOutputFile(fileName + ".LD");
                 if (textData.dpTable != null){
                     textData.saveDprimeToText(OutputFile, TABLE_TYPE, 0, Chromosome.getSize());
                 }else{
                     textData.saveDprimeToText(OutputFile, LIVE_TYPE, 0, Chromosome.getSize());
                 }
             }
 
             if (outputPNG || outputCompressedPNG){
                 OutputFile = validateOutputFile(fileName + ".LD.PNG");
                 if (textData.dpTable == null){
                     textData.generateDPrimeTable();
                     textData.guessBlocks(BLOX_CUSTOM, new Vector());
                 }
                 if (trackFileName != null){
                     textData.readAnalysisTrack(new File(trackFileName));
                 }
                 DPrimeDisplay dpd = new DPrimeDisplay(textData);
                 BufferedImage i = dpd.export(0,Chromosome.getSize(),outputCompressedPNG);
                 try{
                     Jimi.putImage("image/png", i, OutputFile.getName());
                 }catch(JimiException je){
                     System.out.println(je.getMessage());
                 }
             }
 
 
             if(Options.getAssocTest() == ASSOC_TRIO){
                 Vector tdtResults = TDT.calcTrioTDT(textData.getPedFile());
                 HaploData.saveMarkerAssocToText(tdtResults, fileName + ".ASSOC");
             } else if(Options.getAssocTest() == ASSOC_CC) {
                 Vector ccResults = TDT.calcCCTDT(textData.getPedFile());
                 HaploData.saveMarkerAssocToText(ccResults, fileName + ".ASSOC");
             }
 
         }
         catch(IOException e){
             System.err.println("An error has occured. This probably has to do with file input or output");
         }
         catch(HaploViewException e){
             System.err.println(e.getMessage());
         }
         catch(PedFileException pfe) {
             System.err.println(pfe.getMessage());
         }
     }
 
     public Haplotype[][] filterHaplos(Haplotype[][] haplos) {
         Haplotype[][] filteredHaplos = new Haplotype[haplos.length][];
         for (int i = 0; i < haplos.length; i++){
             Vector tempVector = new Vector();
             for (int j = 0; j < haplos[i].length; j++){
                 if (haplos[i][j].getPercentage()*100 > Options.getHaplotypeDisplayThreshold()){
                     tempVector.add(haplos[i][j]);
                 }
             }
             filteredHaplos[i] = new Haplotype[tempVector.size()];
             tempVector.copyInto(filteredHaplos[i]);
         }
 
         return filteredHaplos;
 
     }
 
 
 }

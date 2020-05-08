 package edu.mit.wi.haploview;
 
 
 import edu.mit.wi.pedfile.PedFile;
 import edu.mit.wi.pedfile.Individual;
 import edu.mit.wi.pedfile.Family;
 import edu.mit.wi.pedfile.PedFileException;
 
 import java.io.*;
 //import java.lang.*;
 import java.util.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.text.NumberFormat;
 
 //import java.text.*;
 //import javax.swing.*;
 //import java.awt.*;
 //import java.awt.geom.*;
 
 public class HaploData{
     Vector chromosomes, blocks;
     boolean[] isInBlock;
     boolean infoKnown = false;
     boolean blocksChanged = false;
     int missingLimit = 5;
     private PairwiseLinkage[][] dPrimeTable;
     PairwiseLinkage[][] filteredDPrimeTable;
     public boolean finished = false;
     private double[] numBadGenotypes;
     private double[] percentBadGenotypes;
     private double[] multidprimeArray;
 
     //stuff for computing d prime
     private int AA = 0;
     private int AB = 1;
     private int BA = 2;
     private int BB = 3;
     private double TOLERANCE = 0.00000001;
     private double LN10 = Math.log(10.0);
     int unknownDH=-1;
     int total_chroms=-1;
     double const_prob=-1.0;
     double[] known = new double[5];
     double[] numHaps = new double[4];
     double[] probHaps = new double[4];
 
     //these are for the progress bars
     //these 3 are for the dprime and dprime display progress bars
     private int totalComps = 0;
     private int compsDone = 0;
     private int realCompsDone =0;
     //these are for the haplotype calcs progress bar
     private int totalBlocks = 0;
     private int blocksDone = 0;
     private int assocTest;
     int numTrios, numSingletons,numPeds;
 
     HaploData(){
         assocTest = 0;
     }
     HaploData(int b){
         assocTest = b;
     }
 
     int getTotalComps(){
         return this.totalComps;
     }
 
     int getCompsDone() {
         return this.compsDone;
     }
 
     int getRealCompsDone() {
         return this.realCompsDone;
     }
 
     int getTotalBlocks() {
         return this.totalBlocks;
     }
 
     int getBlocksDone() {
         return this.blocksDone;
     }
 
     void prepareMarkerInput(File infile, long maxdist, String[][] hapmapGoodies) throws IOException, HaploViewException{
         //this method is called to gather data about the markers used.
         //It is assumed that the input file is two columns, the first being
         //the name and the second the absolute position. the maxdist is
         //used to determine beyond what distance comparisons will not be
         //made. if the infile param is null, loads up "dummy info" for
         //situation where no info file exists
 
         if (infile != null){
             if (infile.length() < 1){
                 throw new HaploViewException("Info file is empty or does not exist: " + infile.getName());
             }
             String currentLine;
             Vector markers = new Vector();
             long negMaxdist = -1 * maxdist;
 
             //read the input file:
             BufferedReader in = new BufferedReader(new FileReader(infile));
             // a vector of SNP's is created and returned.
             int snpcount = 0;
 
 
             int lineCount = 0;
             while ((currentLine = in.readLine()) != null){
                 StringTokenizer st = new StringTokenizer(currentLine);
                 if (st.countTokens() > 1){
                     lineCount++;
                 }else if (st.countTokens() == 1){
                     //complain if only one field found
                     throw new HaploViewException("Info file format error on line "+lineCount+
                             ":\n Info file must be of format: <markername> <markerposition>");
                 }else{
                     //skip blank lines
                     continue;
                 }
 
                 if (lineCount > Chromosome.markers.length){
                     throw(new HaploViewException("Info file error:\nMarker number mismatch: too many\nmarkers in info file."));
                 }
 
                 //to compute maf, browse chrom list and count instances of each allele
                 byte a1 = 0;
                 double numa1 = 0; double numa2 = 0;
                 for (int i = 0; i < chromosomes.size(); i++){
                     //if there is a data point for this marker on this chromosome
                     byte thisAllele = ((Chromosome)chromosomes.elementAt(i)).getGenotype(snpcount);
                     if (!(thisAllele == 0)){
                         if (thisAllele == 5){
                             numa1+=0.5; numa2+=0.5;
                         }else if (a1 == 0){
                             a1 = thisAllele; numa1++;
                         }else if (thisAllele == a1){
                             numa1++;
                         }else{
                             numa2++;
                         }
                     }
                 }
                 double maf = numa1/(numa2+numa1);
                 if (maf > 0.5) maf = 1.0-maf;
 
                 String name = st.nextToken(); String loc = st.nextToken();
                 try{
                     markers.add(new SNP(name, Long.parseLong(loc), infile.getName(), Math.rint(maf*100.0)/100.0));
                 }catch (NumberFormatException nfe){
                     throw new HaploViewException("Info file format error on line "+lineCount+
                             ":\n\"" + loc + "\" should be of type long." +
                             "\n Info file must be of format: <markername> <markerposition>");
                 }
                 snpcount ++;
             }
 
             if (lineCount < Chromosome.markers.length){
                 throw(new HaploViewException("Info file error:\nMarker number mismatch: too few\nmarkers in info file."));
             }
 
             Chromosome.markers = markers.toArray();
             if (dPrimeTable != null){
                 //loop through the dprime table to null-out distant markers
                 for (int pos2 = 1; pos2 < dPrimeTable.length; pos2++){
                     for (int pos1 = 0; pos1 < pos2; pos1++){
                         long sep = Chromosome.getMarker(pos1).getPosition() - Chromosome.getMarker(pos2).getPosition();
                         if (maxdist > 0){
                             if ((sep > maxdist || sep < negMaxdist)){
                                 dPrimeTable[pos1][pos2] = null;
                                 continue;
                             }
                         }
                     }
                 }
                 filteredDPrimeTable = getFilteredTable();
             }
             infoKnown=true;
         }else {
             double numChroms = chromosomes.size();
             Vector markerInfo = new Vector();
             numBadGenotypes = new double[Chromosome.getSize()];
             percentBadGenotypes = new double[Chromosome.getSize()];
             for (int i = 0; i < Chromosome.getSize(); i++){
                 //to compute maf, browse chrom list and count instances of each allele
                 byte a1 = 0;
                 double numa1 = 0; double numa2 = 0;
                 for (int j = 0; j < chromosomes.size(); j++){
                     //if there is a data point for this marker on this chromosome
                     byte thisAllele = ((Chromosome)chromosomes.elementAt(j)).getGenotype(i);
                     if (!(thisAllele == 0)){
                         if (thisAllele == 5){
                             numa1+=0.5; numa2+=0.5;
                         }else if (a1 == 0){
                             a1 = thisAllele; numa1++;
                         }else if (thisAllele == a1){
                             numa1++;
                         }else{
                             numa2++;
                         }
                     }
                     else {
                         numBadGenotypes[i]++;
                     }
                 }
                 double maf = numa1/(numa2+numa1);
                 if (maf > 0.5) maf = 1.0-maf;
                 if (hapmapGoodies != null){
                     //we know some stuff from the hapmap so we'll add it here
                     infoKnown = true;
                     markerInfo.add(new SNP(hapmapGoodies[i][0], Long.parseLong(hapmapGoodies[i][1]),
                             Math.rint(maf*100.0)/100.0));
                 }else{
                     markerInfo.add(new SNP(String.valueOf(i+1), (i*4000), Math.rint(maf*100.0)/100.0));
                 }
                 percentBadGenotypes[i] = numBadGenotypes[i]/numChroms;
             }
             Chromosome.markers = markerInfo.toArray();
         }
     }
 
     void prepareHapsInput(File infile) throws IOException, HaploViewException{
         //this method is called to suck in data from a file (its only argument)
         //of genotypes and sets up the Chromosome objects.
         String currentLine;
         Vector chroms = new Vector();
         byte[] genos = new byte[0];
         String ped, indiv;
 
         if(infile.length() < 1){
             throw new HaploViewException("Genotype file is empty or does not exist: " + infile.getName());
         }
         //read the file:
         BufferedReader in = new BufferedReader(new FileReader(infile));
 
         int lineCount = 0;
         int numTokens = 0;
         boolean even = true;
         while ((currentLine = in.readLine()) != null){
             lineCount++;
             //each line is expected to be of the format:
             //ped   indiv   geno   geno   geno   geno...
             if (currentLine.length() == 0){
                 //skip blank lines
                 continue;
             }
             even = !even;
             StringTokenizer st = new StringTokenizer(currentLine);
             //first two tokens are expected to be ped, indiv
             ped = st.nextToken();
             indiv = st.nextToken();
 
             //all other tokens are loaded into a vector (they should all be genotypes)
             genos = new byte[st.countTokens()];
             int q = 0;
 
             if (numTokens == 0){
                 numTokens = st.countTokens();
             }
             if (numTokens != st.countTokens()){
                 throw new HaploViewException("Genotype file error:\nLine " + lineCount +
                         " appears to have an incorrect number of entries");
             }
             while (st.hasMoreTokens()){
                 String thisGenotype = (String)st.nextElement();
                 if (thisGenotype.equals("h")) {
                     genos[q] = 5;
                 }else{
                     try{
                         genos[q] = Byte.parseByte(thisGenotype);
                     }catch (NumberFormatException nfe){
                             throw new HaploViewException("Genotype file input error:\ngenotype value \""
                                     + thisGenotype + "\" on line " + lineCount + " not allowed.");
                     }
                 }
                 if (genos[q] < 0 || genos[q] > 5){
                     throw new HaploViewException("Genotype file input error:\ngenotype value \"" + genos[q] +
                             "\" on line " + lineCount + " not allowed.");
                 }
                 q++;
             }
             //a Chromosome is created and added to a vector of chromosomes.
             //this is what is evetually returned.
             chroms.add(new Chromosome(ped, indiv, genos, false, infile.getName()));
 
         }
         if (!even){
             //we're missing a line here
             throw new HaploViewException("Genotype file appears to have an odd number of lines.\n"+
                     "Each individual is required to have two chromosomes");
         }
         chromosomes = chroms;
 
         //initialize realIndex
         Chromosome.realIndex = new int[genos.length];
         for (int i = 0; i < genos.length; i++){
             Chromosome.realIndex[i] = i;
         }
         try{
             prepareMarkerInput(null,0,null);
         }catch(HaploViewException e){
         }
     }
 
     public void linkageToChrom(boolean[] markerResults, PedFile pedFile, String[][] hmInfo)
             throws IllegalArgumentException, HaploViewException, PedFileException{
 
         if(markerResults == null){
             throw new IllegalArgumentException();
         }
 
 
         Vector indList = pedFile.getOrder();
         int numMarkers = 0;
         numSingletons = 0;
         numTrios = 0;
         numPeds = pedFile.getNumFamilies();
         Individual currentInd;
         Family currentFamily;
         Vector chrom = new Vector();
 
         for(int x=0; x < indList.size(); x++){
 
             String[] indAndFamID = (String[])indList.elementAt(x);
             currentFamily = pedFile.getFamily(indAndFamID[0]);
             currentInd = currentFamily.getMember(indAndFamID[1]);
 
             if(currentInd.getIsTyped()){
                 //singleton
                 //if only one indiv in fam AND no assoc test OR this is case control (assocTest=2)
                 if((currentFamily.getNumMembers() == 1 && assocTest == 0) || assocTest == 2){
                     numMarkers = currentInd.getNumMarkers();
                     byte[] chrom1 = new byte[numMarkers];
                     byte[] chrom2 = new byte[numMarkers];
                     for (int i = 0; i < numMarkers; i++){
                         byte[] thisMarker = currentInd.getMarker(i);
                         if (thisMarker[0] == thisMarker[1]){
                             chrom1[i] = thisMarker[0];
                             chrom2[i] = thisMarker[1];
                         }else{
                             chrom1[i] = 5;
                             chrom2[i] = 5;
                         }
                     }
                     chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),chrom1,currentInd.getAffectedStatus()==2));
                     chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),chrom2,currentInd.getAffectedStatus()==2));
                     numSingletons++;
                 }else{
                     //trio
                     //if indiv has both parents AND is affected OR no assoc test
                     if (!(currentInd.getMomID().equals("0") || currentInd.getDadID().equals("0"))
                             && (currentInd.getAffectedStatus() == 2 || assocTest == 0)){
                         numMarkers = currentInd.getNumMarkers();
                         byte[] dadTb = new byte[numMarkers];
                         byte[] dadUb = new byte[numMarkers];
                         byte[] momTb = new byte[numMarkers];
                         byte[] momUb = new byte[numMarkers];
 
                         boolean[] kidMissing = new boolean[numMarkers];
 
                         for (int i = 0; i < numMarkers; i++){
                             byte[] thisMarker = currentInd.getMarker(i);
                             byte kid1 = thisMarker[0];
                             byte kid2 = thisMarker[1];
 
                             thisMarker = (currentFamily.getMember(currentInd.getMomID())).getMarker(i);
                             byte mom1 = thisMarker[0];
                             byte mom2 = thisMarker[1];
                             thisMarker = (currentFamily.getMember(currentInd.getDadID())).getMarker(i);
                             byte dad1 = thisMarker[0];
                             byte dad2 = thisMarker[1];
 
                             if (kid1 == 0 || kid2 == 0) {
                                 kidMissing[i] = true;
                                 //kid missing
                                 if (dad1 == dad2) {
                                     dadTb[i] = dad1;
                                     dadUb[i] = dad1;
                                 } else {
                                     dadTb[i] = 5;
                                     dadUb[i] = 5;
                                 }
                                 if (mom1 == mom2) {
                                     momTb[i] = mom1;
                                     momUb[i] = mom1;
                                 } else {
                                     momTb[i] = 5;
                                     momUb[i] = 5;
                                 }
                             } else if (kid1 == kid2) {
                                 //kid homozygous
                                 if (dad1 == 0) {
                                     dadTb[i] = kid1;
                                     dadUb[i] = 0;
                                 } else if (dad1 == kid1) {
                                     dadTb[i] = dad1;
                                     dadUb[i] = dad2;
                                 } else {
                                     dadTb[i] = dad2;
                                     dadUb[i] = dad1;
                                 }
 
                                 if (mom1 == 0) {
                                     momTb[i] = kid1;
                                     momUb[i] = 0;
                                 } else if (mom1 == kid1) {
                                     momTb[i] = mom1;
                                     momUb[i] = mom2;
                                 } else {
                                     momTb[i] = mom2;
                                     momUb[i] = mom1;
                                 }
                             } else {
                                 //kid heterozygous and this if tree's a bitch
                                 if (dad1 == 0 && mom1 == 0) {
                                     //both missing
                                     dadTb[i] = 0;
                                     dadUb[i] = 0;
                                     momTb[i] = 0;
                                     momUb[i] = 0;
                                 } else if (dad1 == 0 && mom1 != mom2) {
                                     //dad missing mom het
                                     dadTb[i] = 0;
                                     dadUb[i] = 0;
                                     momTb[i] = 5;
                                     momUb[i] = 5;
                                 } else if (mom1 == 0 && dad1 != dad2) {
                                     //dad het mom missing
                                     dadTb[i] = 5;
                                     dadUb[i] = 5;
                                     momTb[i] = 0;
                                     momUb[i] = 0;
                                 } else if (dad1 == 0 && mom1 == mom2) {
                                     //dad missing mom hom
                                     momTb[i] = mom1;
                                     momUb[i] = mom1;
                                     dadUb[i] = 0;
                                     if (kid1 == mom1) {
                                         dadTb[i] = kid2;
                                     } else {
                                         dadTb[i] = kid1;
                                     }
                                 } else if (mom1 == 0 && dad1 == dad2) {
                                     //mom missing dad hom
                                     dadTb[i] = dad1;
                                     dadUb[i] = dad1;
                                     momUb[i] = 0;
                                     if (kid1 == dad1) {
                                         momTb[i] = kid2;
                                     } else {
                                         momTb[i] = kid1;
                                     }
                                 } else if (dad1 == dad2 && mom1 != mom2) {
                                     //dad hom mom het
                                     dadTb[i] = dad1;
                                     dadUb[i] = dad2;
                                     if (kid1 == dad1) {
                                         momTb[i] = kid2;
                                         momUb[i] = kid1;
                                     } else {
                                         momTb[i] = kid1;
                                         momUb[i] = kid2;
                                     }
                                 } else if (mom1 == mom2 && dad1 != dad2) {
                                     //dad het mom hom
                                     momTb[i] = mom1;
                                     momUb[i] = mom2;
                                     if (kid1 == mom1) {
                                         dadTb[i] = kid2;
                                         dadUb[i] = kid1;
                                     } else {
                                         dadTb[i] = kid1;
                                         dadUb[i] = kid2;
                                     }
                                 } else if (dad1 == dad2 && mom1 == mom2) {
                                     //mom & dad hom
                                     dadTb[i] = dad1;
                                     dadUb[i] = dad1;
                                     momTb[i] = mom1;
                                     momUb[i] = mom1;
                                 } else {
                                     //everybody het
                                     dadTb[i] = 5;
                                     dadUb[i] = 5;
                                     momTb[i] = 5;
                                     momUb[i] = 5;
                                 }
                             }
                         }
                         chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),
                                 dadTb,currentInd.getAffectedStatus()==2,kidMissing));
                         chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),
                                 dadUb,currentInd.getAffectedStatus()==2,kidMissing));
                         chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),
                                 momTb,currentInd.getAffectedStatus()==2,kidMissing));
                         chrom.add(new Chromosome(currentInd.getFamilyID(),currentInd.getIndividualID(),
                                 momUb,currentInd.getAffectedStatus()==2,kidMissing));
                         numTrios++;
                     }
                 }
             }
         }
 
 
         //set up the indexing to take into account skipped markers. Need
         //to loop through twice because first time we just count number of
         //unskipped markers
         int count = 0;
         for (int i = 0; i < numMarkers; i++){
             if (markerResults[i]){
                 count++;
             }
         }
         Chromosome.realIndex = new int[count];
         int k = 0;
         for (int i =0; i < numMarkers; i++){
             if (markerResults[i]){
                 Chromosome.realIndex[k] = i;
                 k++;
             }
         }
         chromosomes = chrom;
         try{
             prepareMarkerInput(null,0,hmInfo);
         }catch(HaploViewException e){
         }catch(IOException e){
         }
     }
 
     void generateDPrimeTable(long maxdist){
         //calculating D prime requires the number of each possible 2 marker
         //haplotype in the dataset
         dPrimeTable = new PairwiseLinkage[Chromosome.getSize()][Chromosome.getSize()];
         long negMaxdist = -1*maxdist;
 
         totalComps = (Chromosome.getSize()*(Chromosome.getSize()-1))/2;
         compsDone =0;
 
         //loop through all marker pairs
         for (int pos2 = 1; pos2 < dPrimeTable.length; pos2++){
             for (int pos1 = 0; pos1 < pos2; pos1++){
 
                 //clear the array
                 long sep = Chromosome.getMarker(pos2).getPosition() - Chromosome.getMarker(pos1).getPosition();
                 if (maxdist > 0){
                     if ((sep > maxdist || sep < negMaxdist)){
                         dPrimeTable[pos1][pos2] = null;
                         continue;
                     }
                 }
 
                 dPrimeTable[pos1][pos2] = computeDPrime(pos1, pos2);
             }
         }
         filteredDPrimeTable = getFilteredTable();
     }
 
     PairwiseLinkage[][] getFilteredTable(){
         //make a filtered version which doesn't include unchecked markers
         //from ped files. this is the version which needs to be handed off to all
         //display methods etc.
         PairwiseLinkage[][] filt = new PairwiseLinkage[Chromosome.getFilteredSize()][Chromosome.getFilteredSize()];
         for (int j = 1; j < filt.length; j++){
             for (int i = 0; i < j; i++){
                filt[i][j] = dPrimeTable[Chromosome.realIndex[i]][Chromosome.realIndex[j]];
             }
         }
         return filt;
     }
 
     Haplotype[][] generateHaplotypes(Vector blocks, int hapthresh) throws HaploViewException{
         //TODO: output indiv hap estimates
         Haplotype[][] results = new Haplotype[blocks.size()][];
         //String raw = new String();
         //String currentLine;
 
         this.totalBlocks = blocks.size();
         this.blocksDone = 0;
         for (int k = 0; k < blocks.size(); k++){
             this.blocksDone++;
             int[] theBlock = (int[])blocks.elementAt(k);
             int[] hetcount = new int[theBlock.length];
             int[][] loc = new int[theBlock.length][5];
             int[][] convert = new int[theBlock.length][5];
             int[][] unconvert = new int[theBlock.length][5];
             //int totalHaps = 0;
 
             //parse genotypes for unresolved heterozygotes
             for (int i = 0; i < chromosomes.size(); i++){
                 Chromosome thisChrom = (Chromosome)chromosomes.elementAt(i);
                 for (int j = 0; j < theBlock.length; j++){
                     byte theGeno = thisChrom.getFilteredGenotype(theBlock[j]);
                     if (theGeno == 5){
                         hetcount[j]++;
                     } else {
                         loc[j][theGeno]++;
                     }
                 }
                 //totalHaps ++;
             }
 
             for (int j = 0; j < theBlock.length; j++){
                 int a = 1;
                 for (int m = 1; m <= 4; m++){
                     if (loc[j][m] > 0){
                         convert[j][m]=a;
                         unconvert[j][a]=m;
                         loc[j][m]+=(hetcount[j]/2);
                         a++;
                     } else {
                         convert[j][m] = 0;
                         unconvert[j][a] = 8;
                     }
                 }
                 if (unconvert[j][2] == 0) unconvert[j][2] = 8;
             }
 
 
             StringBuffer hapstr = new StringBuffer(theBlock.length);
             Vector inputHaploVector = new Vector();
             for (int i = 0; i < chromosomes.size(); i++){
                 Chromosome thisChrom = (Chromosome)chromosomes.elementAt(i);
                 Chromosome nextChrom = (Chromosome)chromosomes.elementAt(++i);
                 int missing=0;
                 //int dhet=0;
                 for (int j = 0; j < theBlock.length; j++){
                     byte theGeno = thisChrom.getFilteredGenotype(theBlock[j]);
                     byte nextGeno = nextChrom.getFilteredGenotype(theBlock[j]);
                     if(theGeno == 0 || nextGeno == 0) missing++;
                 }
 
                 if (! (missing > theBlock.length/2 || missing > missingLimit)){
                     for (int j = 0; j < theBlock.length; j++){
                         byte theGeno = thisChrom.getFilteredGenotype(theBlock[j]);
                         if (theGeno == 5){
                             hapstr.append("h");
                         } else {
                             hapstr.append(convert[j][theGeno]);
                         }
                     }
                     inputHaploVector.add(hapstr.toString());
                     hapstr = new StringBuffer(theBlock.length);
                     for (int j = 0; j < theBlock.length; j++){
                         byte nextGeno = nextChrom.getFilteredGenotype(theBlock[j]);
                         if (nextGeno == 5){
                             hapstr.append("h");
                         }else{
                             hapstr.append(convert[j][nextGeno]);
                         }
                     }
                     inputHaploVector.add(hapstr.toString());
                     hapstr = new StringBuffer(theBlock.length);
                 }
             }
             String[] input_haplos = (String[])inputHaploVector.toArray(new String[0]);
 
             //break up large blocks if needed
             int[] block_size;
             if (theBlock.length < 9){
                 block_size = new int[1];
                 block_size[0] = theBlock.length;
             } else {
                 //some base-8 arithmetic
                 int ones = theBlock.length%8;
                 int eights = (theBlock.length - ones)/8;
                 if (ones == 0){
                     block_size = new int[eights];
                     for (int i = 0; i < eights; i++){
                         block_size[i]=8;
                     }
                 } else {
                     block_size = new int[eights+1];
                     for (int i = 0; i < eights-1; i++){
                         block_size[i]=8;
                     }
                     block_size[eights-1] = (8+ones)/2;
                     block_size[eights] = 8+ones-block_size[eights-1];
                 }
             }
 
 
             String EMreturn = new String("");
             int[] num_haplos_present = new int[1];
             Vector haplos_present = new Vector();
             Vector haplo_freq = new Vector();
             char[][] input_haplos2 = new char[input_haplos.length][];
             for (int j = 0; j < input_haplos.length; j++){
                 input_haplos2[j] = input_haplos[j].toCharArray();
             }
 
             //kirby patch
             EM theEM = new EM();
             theEM.full_em_breakup(input_haplos2, 4, num_haplos_present, haplos_present, haplo_freq, block_size, 0);
             for (int j = 0; j < haplos_present.size(); j++){
                 EMreturn += (String)haplos_present.elementAt(j)+"\t"+(String)haplo_freq.elementAt(j)+"\t";
             }
 
 
             StringTokenizer st = new StringTokenizer(EMreturn);
             int p = 0;
             Haplotype[] tempArray = new Haplotype[st.countTokens()/2];
             while(st.hasMoreTokens()){
                 String aString = st.nextToken();
                 int[] genos = new int[aString.length()];
                 for (int j = 0; j < aString.length(); j++){
                     genos[j] = unconvert[j][Integer.parseInt(aString.substring(j, j+1))];
                 }
                 double tempPerc = Double.parseDouble(st.nextToken());
                 if (tempPerc*100 > hapthresh){
                     tempArray[p] = new Haplotype(genos, tempPerc, theBlock);
                     p++;
                 }
             }
             //make the results array only large enough to hold haps
             //which pass threshold above
             results[k] = new Haplotype[p];
             for (int z = 0; z < p; z++){
                 results[k][z] = tempArray[z];
             }
         }
         return results;
     }
 
     double[] getMultiDprime(){
         return multidprimeArray;
     }
 
     Haplotype[][] generateCrossovers(Haplotype[][] haplos) throws HaploViewException{
         Vector crossBlock = new Vector();
         double CROSSOVER_THRESHOLD = 0.01;   //to what percentage do we want to consider crossings?
 
         if (haplos.length == 0) return null;
 
         //seed first block with ordering numbers
         for (int u = 0; u < haplos[0].length; u++){
             haplos[0][u].setListOrder(u);
         }
 
         for (int i = 0; i < haplos.length; i++){
             haplos[i][0].clearTags();
         }
 
         multidprimeArray = new double[haplos.length];
         //get "tag" SNPS if there is only one block:
         if (haplos.length==1){
             Vector theBestSubset = getBestSubset(haplos[0]);
             for (int i = 0; i < theBestSubset.size(); i++){
                 haplos[0][0].addTag(((Integer)theBestSubset.elementAt(i)).intValue());
             }
         }
         for (int gap = 0; gap < haplos.length - 1; gap++){         //compute crossovers for each inter-block gap
             Vector preGapSubset = getBestSubset(haplos[gap]);
             Vector postGapSubset = getBestSubset(haplos[gap+1]);
             int[] preMarkerID = haplos[gap][0].getMarkers();       //index haplos to markers in whole dataset
             int[] postMarkerID = haplos[gap+1][0].getMarkers();
 
             crossBlock.clear();                 //make a "block" of the markers which id the pre- and post- gap haps
             for (int i = 0; i < preGapSubset.size(); i++){
                 crossBlock.add(new Integer(preMarkerID[((Integer)preGapSubset.elementAt(i)).intValue()]));
                 //mark tags
                 haplos[gap][0].addTag(((Integer)preGapSubset.elementAt(i)).intValue());
             }
             for (int i = 0; i < postGapSubset.size(); i++){
                 crossBlock.add(new Integer(postMarkerID[((Integer)postGapSubset.elementAt(i)).intValue()]));
                 //mark tags
                 haplos[gap+1][0].addTag(((Integer)postGapSubset.elementAt(i)).intValue());
             }
 
             Vector inputVector = new Vector();
             int[] intArray = new int[crossBlock.size()];
             for (int i = 0; i < crossBlock.size(); i++){      //input format for hap generating routine
                 intArray[i] = ((Integer)crossBlock.elementAt(i)).intValue();
             }
             inputVector.add(intArray);
 
             Haplotype[] crossHaplos = generateHaplotypes(inputVector, 1)[0];  //get haplos of gap
             double[][] multilocusTable = new double[haplos[gap].length][];
             double[] rowSum = new double[haplos[gap].length];
             double[] colSum = new double[haplos[gap+1].length];
             double multilocusTotal = 0;
 
             for (int i = 0; i < haplos[gap].length; i++){
                 double[] crossPercentages = new double[haplos[gap+1].length];
                 StringBuffer firstHapCodeB = new StringBuffer(preGapSubset.size());
                 for (int j = 0; j < preGapSubset.size(); j++){   //make a string out of uniquely identifying genotypes for this hap
                     firstHapCodeB.append(haplos[gap][i].getGeno()[((Integer)preGapSubset.elementAt(j)).intValue()]);
                 }
                 String firstHapCode = firstHapCodeB.toString();
                 for (int gapHaplo = 0; gapHaplo < crossHaplos.length; gapHaplo++){  //look at each crossover hap
                     if (crossHaplos[gapHaplo].getPercentage() > CROSSOVER_THRESHOLD){
                         StringBuffer gapBeginHapCodeB = new StringBuffer(preGapSubset.size());
                         for (int j = 0; j < preGapSubset.size(); j++){     //make a string as above
                             gapBeginHapCodeB.append(crossHaplos[gapHaplo].getGeno()[j]);
                         }
                         String gapBeginHapCode = gapBeginHapCodeB.toString();
                         if (gapBeginHapCode.equals(firstHapCode)){    //if this crossover hap corresponds to this pregap hap
                             StringBuffer gapEndHapCodeB = new StringBuffer(preGapSubset.size());
                             for (int j = preGapSubset.size(); j < crossHaplos[gapHaplo].getGeno().length; j++){
                                 gapEndHapCodeB.append(crossHaplos[gapHaplo].getGeno()[j]);
                             }
                             String gapEndHapCode = gapEndHapCodeB.toString();
                             for (int j = 0; j < haplos[gap+1].length; j++){
                                 StringBuffer endHapCodeB = new StringBuffer();
                                 for (int k = 0; k < postGapSubset.size(); k++){
                                     endHapCodeB.append(haplos[gap+1][j].getGeno()[((Integer)postGapSubset.elementAt(k)).intValue()]);
                                 }
                                 String endHapCode = endHapCodeB.toString();
                                 if (gapEndHapCode.equals(endHapCode)){
                                     crossPercentages[j] = crossHaplos[gapHaplo].getPercentage();
                                 }
                             }
                         }
                     }
                 }
                 //thought i needed to fix these percentages, but the raw values are just as good.
                 /*		double percentageSum = 0;
                 double[] fixedCross = new double[crossPercentages.length];
                 for (int y = 0; y < crossPercentages.length; y++){
                 percentageSum += crossPercentages[y];
                 }
                 for (int y = 0; y < crossPercentages.length; y++){
                 fixedCross[y] = crossPercentages[y]/percentageSum;
                 }*/
                 haplos[gap][i].addCrossovers(crossPercentages);
                 multilocusTable[i] = crossPercentages;
             }
 
             //sort based on "straight line" crossings
             int hilimit;
             int lolimit;
             if (haplos[gap+1].length > haplos[gap].length) {
                 hilimit = haplos[gap+1].length;
                 lolimit = haplos[gap].length;
             }else{
                 hilimit = haplos[gap].length;
                 lolimit = haplos[gap+1].length;
             }
             boolean[] unavailable = new boolean[hilimit];
             int[] prevBlockLocs = new int[haplos[gap].length];
             for (int q = 0; q < prevBlockLocs.length; q++){
                 prevBlockLocs[haplos[gap][q].getListOrder()] = q;
             }
 
             for (int u = 0; u < haplos[gap+1].length; u++){
                 double currentBestVal = 0;
                 int currentBestLoc = -1;
                 for (int v = 0; v < lolimit; v++){
                     if (!(unavailable[v])){
                         if (haplos[gap][prevBlockLocs[v]].getCrossover(u) >= currentBestVal) {
                             currentBestLoc = haplos[gap][prevBlockLocs[v]].getListOrder();
                             currentBestVal = haplos[gap][prevBlockLocs[v]].getCrossover(u);
                         }
                     }
                 }
                 //it didn't get lined up with any of the previous block's markers
                 //put it at the end of the list
                 if (currentBestLoc == -1){
                     for (int v = 0; v < unavailable.length; v++){
                         if (!(unavailable[v])){
                             currentBestLoc = v;
                             break;
                         }
                     }
                 }
 
                 haplos[gap+1][u].setListOrder(currentBestLoc);
                 unavailable[currentBestLoc] = true;
             }
 
             //compute multilocus D'
             for (int i = 0; i < rowSum.length; i++){
                 for (int j = 0; j < colSum.length; j++){
                     rowSum[i] += multilocusTable[i][j];
                     colSum[j] += multilocusTable[i][j];
                     multilocusTotal += multilocusTable[i][j];
                     if (rowSum[i] == 0) rowSum[i] = 0.0001;
                     if (colSum[j] == 0) colSum[j] = 0.0001;
                 }
             }
             double multidprime = 0;
             boolean noDivByZero = false;
             for (int i = 0; i < rowSum.length; i++){
                 for (int j = 0; j < colSum.length; j++){
                     double num = (multilocusTable[i][j]/multilocusTotal) - (rowSum[i]/multilocusTotal)*(colSum[j]/multilocusTotal);
                     double denom;
                     if (num < 0){
                         double denom1 = (rowSum[i]/multilocusTotal)*(colSum[j]/multilocusTotal);
                         double denom2 = (1.0 - (rowSum[i]/multilocusTotal))*(1.0 - (colSum[j]/multilocusTotal));
                         if (denom1 < denom2) {
                             denom = denom1;
                         }else{
                             denom = denom2;
                         }
                     }else{
                         double denom1 = (rowSum[i]/multilocusTotal)*(1.0 -(colSum[j]/multilocusTotal));
                         double denom2 = (1.0 - (rowSum[i]/multilocusTotal))*(colSum[j]/multilocusTotal);
                         if (denom1 < denom2){
                             denom = denom1;
                         }else{
                             denom = denom2;
                         }
                     }
                     if (denom != 0){
                         noDivByZero = true;
                         multidprime += (rowSum[i]/multilocusTotal)*(colSum[j]/multilocusTotal)*Math.abs(num/denom);
                     }
                 }
             }
             if (noDivByZero){
                 multidprimeArray[gap] = multidprime;
             }else{
                 multidprimeArray[gap] = 1.00;
             }
         }
         return haplos;
     }
 
     Vector getBestSubset(Haplotype[] thisBlock){    //from a block of haps, find marker subset which uniquely id's all haps
         Vector bestSubset = new Vector();
         //first make an array with markers ranked by genotyping success rate
         Vector genoSuccessRank = new Vector();
         Vector genoNumberRank = new Vector();
         int[] myMarkers = thisBlock[0].getMarkers();
         genoSuccessRank.add(new Double(percentBadGenotypes[myMarkers[0]]));
         genoNumberRank.add(new Integer(0));
         for (int i = 1; i < myMarkers.length; i++){
             boolean inserted = false;
             for (int j = 0; j < genoSuccessRank.size(); j++){
                 if (percentBadGenotypes[myMarkers[i]] < ((Double)(genoSuccessRank.elementAt(j))).doubleValue()){
                     genoSuccessRank.insertElementAt(new Double(percentBadGenotypes[myMarkers[i]]), j);
                     genoNumberRank.insertElementAt(new Integer(i), j);
                     inserted = true;
                     break;
                 }
             }
             if (!(inserted)) {
                 genoNumberRank.add(new Integer(i));
                 genoSuccessRank.add(new Double(percentBadGenotypes[myMarkers[i]]));
             }
         }
 
         for (int i = 0; i < thisBlock.length-1; i++){
             int[] firstHap = thisBlock[i].getGeno();
             for (int j = i+1; j < thisBlock.length; j++){
                 int[] secondHap = thisBlock[j].getGeno();
                 for (int y = 0; y < firstHap.length; y++){
                     int x = ((Integer)(genoNumberRank.elementAt(y))).intValue();
                     if (firstHap[x] != secondHap[x]){
                         if (!(bestSubset.contains(new Integer(x)))){
                             bestSubset.add(new Integer(x));
                             break;
                         } else {
                             break;
                         }
                     }
                 }
             }
         }
         return bestSubset;
     }
 
     void guessBlocks(int method){
         Vector returnVec = new Vector();
         switch(method){
             case 0: returnVec = FindBlocks.doSFS(filteredDPrimeTable); break;
             case 1: returnVec = FindBlocks.do4Gamete(filteredDPrimeTable); break;
             case 2: returnVec = FindBlocks.doMJD(filteredDPrimeTable); break;
             case 3: returnVec = new Vector();break;
         }
         blocks = returnVec;
         blocksChanged = true;
 
         //keep track of which markers are in a block
         isInBlock = new boolean[Chromosome.getSize()];
         for (int i = 0; i < isInBlock.length; i++){
             isInBlock[i] = false;
         }
         for (int i = 0; i < blocks.size(); i++){
             int[] markers = (int[])blocks.elementAt(i);
             for (int j = 0; j < markers.length; j++){
                 isInBlock[markers[j]] = true;
             }
         }
     }
 
     public void removeFromBlock(int markerNum) {
       if (blocks != null){
           OUTER: for (int i = 0; i < blocks.size(); i ++){
               int thisBlock[] = (int[])blocks.elementAt(i);
               int newBlock[] = new int[thisBlock.length-1];
               int count = 0;
               for (int j = 0; j < thisBlock.length; j++){
                   if(markerNum == thisBlock[j]){
                       blocksChanged = true;
                       if (newBlock.length < 2){
                           blocks.removeElementAt(i);
                           for (int k = 0; k < thisBlock.length; k++){
                               this.isInBlock[thisBlock[k]] = false;
                           }
                           break OUTER;
                       }
                       this.isInBlock[markerNum] = false;
                       for (int k = 0; k < thisBlock.length; k++){
                           if (!(k==j)){
                               newBlock[count] = thisBlock[k];
                               count++;
                           }
                       }
                       blocks.setElementAt(newBlock, i);
                       break OUTER;
                   }
               }
           }
       }
     }
 
     public void addMarkerIntoSurroundingBlock(int markerNum) {
         if (blocks != null){
             OUTER: for (int i = 0; i < blocks.size(); i ++){
                 int thisBlock[] = (int[])blocks.elementAt(i);
                 int newBlock[] = new int[thisBlock.length+1];
                 int count = 0;
                 if(markerNum > thisBlock[0] && markerNum < thisBlock[thisBlock.length-1]){
                     blocksChanged = true;
                     this.isInBlock[markerNum] = true;
                     for (int j = 0; j < thisBlock.length; j++){
                         newBlock[count] = thisBlock[j];
                         count++;
                         if (thisBlock[j] < markerNum && thisBlock[j+1] > markerNum){
                             newBlock[count] = markerNum;
                             count++;
                         }
                     }
                     blocks.setElementAt(newBlock, i);
                     break OUTER;
                 }
             }
         }
     }
 
     public void addBlock(int firstMarker, int lastMarker) {
         if (firstMarker < 0){
             firstMarker = 0;
         }
         if (lastMarker >= Chromosome.realIndex.length){
             lastMarker = Chromosome.realIndex.length-1;
         }
         if (lastMarker - firstMarker < 1){
             return;
         }
 
         int inArray[] = new int[lastMarker-firstMarker+1];
         blocksChanged = true;
         if (blocks.size() != 0){
             boolean placed = false;
             for (int i = 0; i < blocks.size(); i++){
                 int currentBlock[] = (int[])blocks.elementAt(i);
                 //trim out any blocks that are overlapped
                 if ((lastMarker >= currentBlock[0] && firstMarker <= currentBlock[currentBlock.length-1]) ||
                         firstMarker <= currentBlock[currentBlock.length-1] && firstMarker >= currentBlock[0]){
                     for (int j = 0; j < currentBlock.length; j++){
                         isInBlock[currentBlock[j]] = false;
                     }
                     blocks.removeElementAt(i);
                     i--;
                 }
             }
             for (int i = 0; i < blocks.size(); i++){
                 int currentBlock[] = (int[])blocks.elementAt(i);
                 if (firstMarker <= currentBlock[0] && !placed){
                     blocks.insertElementAt(inArray,i);
                     placed = true;
                 }
             }
             if (!placed){
                 blocks.add(inArray);
             }
         }else{
             blocks.add(inArray);
         }
         for (int i = 0; i < inArray.length; i++){
             inArray[i] = firstMarker+i;
             this.isInBlock[firstMarker+i] = true;
         }
     }
 
     public PairwiseLinkage computeDPrime(int pos1, int pos2){
         compsDone++;
         int doublehet = 0;
         int[][] twoMarkerHaplos = new int[3][3];
 
         for (int i = 0; i < twoMarkerHaplos.length; i++){
             for (int j = 0; j < twoMarkerHaplos[i].length; j++){
                 twoMarkerHaplos[i][j] = 0;
             }
         }
         doublehet = 0;
         //get the alleles for the markers
         int m1a1 = 0; int m1a2 = 0; int m2a1 = 0; int m2a2 = 0; int m1H = 0; int m2H = 0;
 
         for (int i = 0; i < chromosomes.size(); i++){
             byte a1 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos1);
             byte a2 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos2);
             if (m1a1 > 0){
                 if (m1a2 == 0 && !(a1 == 5) && !(a1 == 0) && a1 != m1a1) m1a2 = a1;
             } else if (!(a1 == 5) && !(a1 == 0)) m1a1=a1;
 
             if (m2a1 > 0){
                 if (m2a2 == 0 && !(a2 == 5) && !(a2 == 0) && a2 != m2a1) m2a2 = a2;
             } else if (!(a2 == 5) && !(a2 == 0)) m2a1=a2;
 
             if (a1 == 5) m1H++;
             if (a2 == 5) m2H++;
         }
 
         //check for non-polymorphic markers
         if (m1a2==0){
             if (m1H==0){
                return null;
             } else {
                 if (m1a1 == 1){ m1a2=2; }
                 else { m1a2 = 1; }
             }
         }
         if (m2a2==0){
             if (m2H==0){
                 return null;
                 //TODO: look into these nulls (c.f. clean.ped adding in bad m's)
             } else {
                 if (m2a1 == 1){ m2a2=2; }
                 else { m2a2 = 1; }
             }
         }
 
         int[] marker1num = new int[5]; int[] marker2num = new int[5];
 
         marker1num[0]=0;
         marker1num[m1a1]=1;
         marker1num[m1a2]=2;
         marker2num[0]=0;
         marker2num[m2a1]=1;
         marker2num[m2a2]=2;
         //iterate through all chromosomes in dataset
         for (int i = 0; i < chromosomes.size(); i++){
             //System.out.println(i + " " + pos1 + " " + pos2);
             //assign alleles for each of a pair of chromosomes at a marker to four variables
             byte a1 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos1);
             byte a2 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos2);
             byte b1 = ((Chromosome) chromosomes.elementAt(++i)).getGenotype(pos1);
             byte b2 = ((Chromosome) chromosomes.elementAt(i)).getGenotype(pos2);
             if (a1 == 0 || a2 == 0 || b1 == 0 || b2 == 0){
                 //skip missing data
             } else if ((a1 == 5 && a2 == 5) || (a1 == 5 && !(a2 == b2)) || (a2 == 5 && !(a1 == b1))) doublehet++;
             //find doublehets and resolved haplotypes
             else if (a1 == 5){
                 twoMarkerHaplos[1][marker2num[a2]]++;
                 twoMarkerHaplos[2][marker2num[a2]]++;
             } else if (a2 == 5){
                 twoMarkerHaplos[marker1num[a1]][1]++;
                 twoMarkerHaplos[marker1num[a1]][2]++;
             } else {
                 twoMarkerHaplos[marker1num[a1]][marker2num[a2]]++;
                 twoMarkerHaplos[marker1num[b1]][marker2num[b2]]++;
             }
 
         }
         //another monomorphic marker check
         int r1, r2, c1, c2;
         r1 = twoMarkerHaplos[1][1] + twoMarkerHaplos[1][2];
         r2 = twoMarkerHaplos[2][1] + twoMarkerHaplos[2][2];
         c1 = twoMarkerHaplos[1][1] + twoMarkerHaplos[2][1];
         c2 = twoMarkerHaplos[1][2] + twoMarkerHaplos[2][2];
         if ( (r1==0 || r2==0 || c1==0 || c2==0) && doublehet == 0){
             return  new PairwiseLinkage(1,0,0,0,0,new double[0]);
         }
 
         //compute D Prime for this pair of markers.
         //return is a tab delimited string of d', lod, r^2, CI(low), CI(high)
         this.realCompsDone++;
 
 
         int i,count;
         //int j,k,itmp;
         int low_i = 0;
         int high_i = 0;
         double loglike, oldloglike;// meand, mean2d, sd;
         double tmp;//g,h,m,tmp,r;
         double num, denom1, denom2, denom, dprime;//, real_dprime;
         double pA1, pB1, pA2, pB2, loglike1, loglike0, rsq;
         double tmpAA, tmpAB, tmpBA, tmpBB, dpr;// tmp2AA, tmp2AB, tmp2BA, tmp2BB;
         double total_prob, sum_prob;
         double lsurface[] = new double[105];
 
         /* store arguments in externals and compute allele frequencies */
 
         known[AA]=twoMarkerHaplos[1][1];
         known[AB]=twoMarkerHaplos[1][2];
         known[BA]=twoMarkerHaplos[2][1];
         known[BB]=twoMarkerHaplos[2][2];
         unknownDH=doublehet;
         total_chroms= (int)(known[AA]+known[AB]+known[BA]+known[BB]+(2*unknownDH));
         pA1 = (known[AA]+known[AB]+unknownDH) / (double) total_chroms;
         pB1 = 1.0-pA1;
         pA2 = (known[AA]+known[BA]+unknownDH) / (double) total_chroms;
         pB2 = 1.0-pA2;
         const_prob = 0.1;
 
         /* set initial conditions */
 
         if (const_prob < 0.00) {
             probHaps[AA]=pA1*pA2;
             probHaps[AB]=pA1*pB2;
             probHaps[BA]=pB1*pA2;
             probHaps[BB]=pB1*pB2;
         } else {
             probHaps[AA]=const_prob;
             probHaps[AB]=const_prob;
             probHaps[BA]=const_prob;
             probHaps[BB]=const_prob;;
 
             /* so that the first count step will produce an
             initial estimate without inferences (this should
             be closer and therefore speedier than assuming
             they are all at equal frequency) */
 
             count_haps(0);
             estimate_p();
         }
 
         /* now we have an initial reasonable guess at p we can
         start the EM - let the fun begin */
 
         const_prob=0.0;
         count=1; loglike=-999999999.0;
 
         do {
             oldloglike=loglike;
             count_haps(count);
             loglike = known[AA]*log10(probHaps[AA]) + known[AB]*log10(probHaps[AB]) + known[BA]*log10(probHaps[BA]) + known[BB]*log10(probHaps[BB]) + (double)unknownDH*log10(probHaps[AA]*probHaps[BB] + probHaps[AB]*probHaps[BA]);
             if (Math.abs(loglike-oldloglike) < TOLERANCE) break;
             estimate_p();
             count++;
         } while(count < 1000);
         /* in reality I've never seen it need more than 10 or so iterations
         to converge so this is really here just to keep it from running off into eternity */
 
         loglike1 = known[AA]*log10(probHaps[AA]) + known[AB]*log10(probHaps[AB]) + known[BA]*log10(probHaps[BA]) + known[BB]*log10(probHaps[BB]) + (double)unknownDH*log10(probHaps[AA]*probHaps[BB] + probHaps[AB]*probHaps[BA]);
         loglike0 = known[AA]*log10(pA1*pA2) + known[AB]*log10(pA1*pB2) + known[BA]*log10(pB1*pA2) + known[BB]*log10(pB1*pB2) + (double)unknownDH*log10(2*pA1*pA2*pB1*pB2);
 
         num = probHaps[AA]*probHaps[BB] - probHaps[AB]*probHaps[BA];
 
         if (num < 0) {
             /* flip matrix so we get the positive D' */
             /* flip AA with AB and BA with BB */
             tmp=probHaps[AA]; probHaps[AA]=probHaps[AB]; probHaps[AB]=tmp;
             tmp=probHaps[BB]; probHaps[BB]=probHaps[BA]; probHaps[BA]=tmp;
             /* flip frequency of second allele */
             tmp=pA2; pA2=pB2; pB2=tmp;
             /* flip counts in the same fashion as p's */
             tmp=numHaps[AA]; numHaps[AA]=numHaps[AB]; numHaps[AB]=tmp;
             tmp=numHaps[BB]; numHaps[BB]=numHaps[BA]; numHaps[BA]=tmp;
             /* num has now undergone a sign change */
             num = probHaps[AA]*probHaps[BB] - probHaps[AB]*probHaps[BA];
             /* flip known array for likelihood computation */
             tmp=known[AA]; known[AA]=known[AB]; known[AB]=tmp;
             tmp=known[BB]; known[BB]=known[BA]; known[BA]=tmp;
         }
 
         denom1 = (probHaps[AA]+probHaps[BA])*(probHaps[BA]+probHaps[BB]);
         denom2 = (probHaps[AA]+probHaps[AB])*(probHaps[AB]+probHaps[BB]);
         if (denom1 < denom2) { denom = denom1; }
         else { denom = denom2; }
         dprime = num/denom;
 
         /* add computation of r^2 = (D^2)/p(1-p)q(1-q) */
         rsq = num*num/(pA1*pB1*pA2*pB2);
 
 
         //real_dprime=dprime;
 
         for (i=0; i<=100; i++) {
             dpr = (double)i*0.01;
             tmpAA = dpr*denom + pA1*pA2;
             tmpAB = pA1-tmpAA;
             tmpBA = pA2-tmpAA;
             tmpBB = pB1-tmpBA;
             if (i==100) {
                 /* one value will be 0 */
                 if (tmpAA < 1e-10) tmpAA=1e-10;
                 if (tmpAB < 1e-10) tmpAB=1e-10;
                 if (tmpBA < 1e-10) tmpBA=1e-10;
                 if (tmpBB < 1e-10) tmpBB=1e-10;
             }
             lsurface[i] = known[AA]*log10(tmpAA) + known[AB]*log10(tmpAB) + known[BA]*log10(tmpBA) + known[BB]*log10(tmpBB) + (double)unknownDH*log10(tmpAA*tmpBB + tmpAB*tmpBA);
         }
 
         /* Confidence bounds #2 - used in Gabriel et al (2002) - translate into posterior dist of D' -
         assumes a flat prior dist. of D' - someday we may be able to make
         this even more clever by adjusting given the distribution of observed
         D' values for any given distance after some large scale studies are complete */
 
         total_prob=sum_prob=0.0;
 
         for (i=0; i<=100; i++) {
             lsurface[i] -= loglike1;
             lsurface[i] = Math.pow(10.0,lsurface[i]);
             total_prob += lsurface[i];
         }
 
         for (i=0; i<=100; i++) {
             sum_prob += lsurface[i];
             if (sum_prob > 0.05*total_prob &&
                     sum_prob-lsurface[i] < 0.05*total_prob) {
                 low_i = i-1;
                 break;
             }
         }
 
         sum_prob=0.0;
         for (i=100; i>=0; i--) {
             sum_prob += lsurface[i];
             if (sum_prob > 0.05*total_prob &&
                     sum_prob-lsurface[i] < 0.05*total_prob) {
                 high_i = i+1;
                 break;
             }
         }
         if (high_i > 100){ high_i = 100; }
 
         double[] freqarray = {probHaps[AA], probHaps[AB], probHaps[BB], probHaps[BA]};
         return new PairwiseLinkage(roundDouble(dprime), roundDouble((loglike1-loglike0)), roundDouble(rsq), ((double)low_i/100.0), ((double)high_i/100.0), freqarray);
     }
 
     public void count_haps(int em_round){
         /* only the double heterozygote [AB][AB] results in
         ambiguous reconstruction, so we'll count the obligates
         then tack on the [AB][AB] for clarity */
 
         numHaps[AA] = known[AA];
         numHaps[AB] = known[AB];
         numHaps[BA] = known[BA];
         numHaps[BB] = known[BB];
         if (em_round > 0) {
             numHaps[AA] += unknownDH* (probHaps[AA]*probHaps[BB])/((probHaps[AA]*probHaps[BB])+(probHaps[AB]*probHaps[BA]));
             numHaps[BB] += unknownDH* (probHaps[AA]*probHaps[BB])/((probHaps[AA]*probHaps[BB])+(probHaps[AB]*probHaps[BA]));
             numHaps[AB] += unknownDH* (probHaps[AB]*probHaps[BA])/((probHaps[AA]*probHaps[BB])+(probHaps[AB]*probHaps[BA]));
             numHaps[BA] += unknownDH* (probHaps[AB]*probHaps[BA])/((probHaps[AA]*probHaps[BB])+(probHaps[AB]*probHaps[BA]));
         }
     }
 
     public void estimate_p() {
         double total= numHaps[AA]+numHaps[AB]+numHaps[BA]+numHaps[BB]+(4.0*const_prob);
         probHaps[AA]=(numHaps[AA]+const_prob)/total; if (probHaps[AA] < 1e-10) probHaps[AA]=1e-10;
         probHaps[AB]=(numHaps[AB]+const_prob)/total; if (probHaps[AB] < 1e-10) probHaps[AB]=1e-10;
         probHaps[BA]=(numHaps[BA]+const_prob)/total; if (probHaps[BA] < 1e-10) probHaps[BA]=1e-10;
         probHaps[BB]=(numHaps[BB]+const_prob)/total; if (probHaps[BB] < 1e-10) probHaps[BB]=1e-10;
     }
 
     public double roundDouble (double d){
         return Math.rint(d*100.0)/100.0;
     }
 
     public double log10 (double d) {
         return Math.log(d)/LN10;
     }
 
     public void saveHapsToText(Haplotype[][] finishedHaplos, double[] multidprime,
                                File saveHapsFile) throws IOException{
 
         if (finishedHaplos == null) return;
 
         NumberFormat nf = NumberFormat.getInstance(Locale.US);
         nf.setMinimumFractionDigits(3);
         nf.setMaximumFractionDigits(3);
 
         //open file for saving haps text
         FileWriter saveHapsWriter = new FileWriter(saveHapsFile);
 
         //go through each block and print haplos
         for (int i = 0; i < finishedHaplos.length; i++){
             //write block header
             saveHapsWriter.write("BLOCK " + (i+1) + ".  MARKERS:");
             int[] markerNums = finishedHaplos[i][0].getMarkers();
             boolean[] tags = finishedHaplos[i][0].getTags();
             for (int j = 0; j < markerNums.length; j++){
                saveHapsWriter.write(" " + (Chromosome.realIndex[markerNums[j]]+1));
                 if (tags[j]) saveHapsWriter.write("!");
             }
             saveHapsWriter.write("\n");
             //write haps and crossover percentages
             for (int j = 0; j < finishedHaplos[i].length; j++){
                 int[] theGeno = finishedHaplos[i][j].getGeno();
                 StringBuffer theHap = new StringBuffer(theGeno.length);
                 for (int k = 0; k < theGeno.length; k++){
                     theHap.append(theGeno[k]);
                 }
                 saveHapsWriter.write(theHap.toString() + " (" + nf.format(finishedHaplos[i][j].getPercentage()) + ")");
                 if (i < finishedHaplos.length-1){
                     saveHapsWriter.write("\t|");
                     for (int crossCount = 0; crossCount < finishedHaplos[i+1].length; crossCount++){
                         if (crossCount != 0) saveHapsWriter.write("\t");
                         saveHapsWriter.write(nf.format(finishedHaplos[i][j].getCrossover(crossCount)));
                     }
                     saveHapsWriter.write("|");
                 }
                 saveHapsWriter.write("\n");
             }
             if (i < finishedHaplos.length - 1){
                 saveHapsWriter.write("Multiallelic Dprime: " + multidprime[i] + "\n");
             }else{
                 saveHapsWriter.write("\n");
             }
         }
         saveHapsWriter.close();
     }
 
     public void saveDprimeToText(File dumpDprimeFile) throws IOException{
         FileWriter saveDprimeWriter = new FileWriter(dumpDprimeFile);
         if (infoKnown){
             saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\tDist\n");
             long dist;
 
             for (int i = 0; i < filteredDPrimeTable.length; i++){
                 for (int j = 0; j < filteredDPrimeTable[i].length; j++){
                     //many "slots" in table aren't filled in because it is a 1/2 matrix
                     if (i < j){
                         if(filteredDPrimeTable[i][j] != null) {
                             dist = (Chromosome.getFilteredMarker(j)).getPosition() - (Chromosome.getFilteredMarker(i)).getPosition();
                             saveDprimeWriter.write(Chromosome.getFilteredMarker(i).getName() +
                                     "\t" + Chromosome.getFilteredMarker(j).getName() +
                                     "\t" + filteredDPrimeTable[i][j].toString() + "\t" + dist + "\n");
                         }
                     }
                 }
             }
 
         }else{
             saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\n");
 
             for (int i = 0; i < filteredDPrimeTable.length; i++){
                 for (int j = 0; j < filteredDPrimeTable[i].length; j++){
                     //many "slots" in table aren't filled in because it is a 1/2 matrix
                     if (i < j){
                         if(filteredDPrimeTable[i][j] != null) {
                            saveDprimeWriter.write((Chromosome.realIndex[i]+1) + "\t" + (Chromosome.realIndex[j]+1) + "\t" + filteredDPrimeTable[i][j] + "\n");
                         }
                     }
                 }
             }
         }
 
         saveDprimeWriter.close();
     }
 
     public void linkageToHapsFormat(boolean[] markerResults, PedFile pedFile,
                                     String hapFileName)throws IOException, PedFileException{
         FileWriter linkageToHapsWriter = new FileWriter(new File(hapFileName));
 
         Vector indList = pedFile.getOrder();
         int numMarkers = 0;
         Vector usedParents = new Vector();
         Individual currentInd;
         Family currentFamily;
 
 
         for(int x=0; x < indList.size(); x++){
 
             String[] indAndFamID = (String[])indList.elementAt(x);
             currentFamily = pedFile.getFamily(indAndFamID[0]);
             currentInd = currentFamily.getMember(indAndFamID[1]);
 
             boolean begin = false;
 
             if(currentInd.getIsTyped()){
                 //singleton
                 if(currentFamily.getNumMembers() == 1){
                     StringBuffer hap1 = new StringBuffer(numMarkers);
                     StringBuffer hap2 = new StringBuffer(numMarkers);
                     hap1.append(currentInd.getFamilyID()).append("\t").append(currentInd.getIndividualID()).append("\t");
                     hap2.append(currentInd.getFamilyID()).append("\t").append(currentInd.getIndividualID()).append("\t");
                     numMarkers = currentInd.getNumMarkers();
                     for (int i = 0; i < numMarkers; i++){
                         if (markerResults[i]){
                             if (begin){
                                 hap1.append(" "); hap2.append(" ");
                             }
                             byte[] thisMarker = currentInd.getMarker(i);
                             if (thisMarker[0] == thisMarker[1]){
                                 hap1.append(thisMarker[0]);
                                 hap2.append(thisMarker[1]);
                             }else{
                                 hap1.append("h");
                                 hap2.append("h");
                             }
                             begin=true;
                         }
                     }
                     hap1.append("\n"); hap2.append("\n");
                     hap1.append(hap2);
                     linkageToHapsWriter.write(hap1.toString());
                 }
                else{
                     //skip if indiv is parent in trio or unaffected
                     if (!(currentInd.getMomID().equals("0") || currentInd.getDadID().equals("0") || currentInd.getAffectedStatus() != 2)){
                         //trio
                         String dadT = new String("");
                         String dadU = new String("");
                         String momT = new String("");
                         String momU = new String("");
                         if (!(usedParents.contains( currentInd.getFamilyID() + " " + currentInd.getMomID()) ||
                                 usedParents.contains(currentInd.getFamilyID() + " " + currentInd.getDadID()))){
                             //add 4 phased haps provided that we haven't used this trio already
                             numMarkers = currentInd.getNumMarkers();
                             for (int i = 0; i < numMarkers; i++){
                                 if (markerResults[i]){
                                     if (begin){
                                         dadT+=" ";dadU+=" ";momT+=" ";momU+=" ";
                                     }
                                     byte[] thisMarker = currentInd.getMarker(i);
                                     int kid1 = thisMarker[0];
                                     int kid2 = thisMarker[1];
 
                                     thisMarker = (currentFamily.getMember(currentInd.getMomID())).getMarker(i);
                                     int mom1 = thisMarker[0];
                                     int mom2 = thisMarker[1];
                                     thisMarker = (currentFamily.getMember(currentInd.getDadID())).getMarker(i);
                                     int dad1 = thisMarker[0];
                                     int dad2 = thisMarker[1];
 
                                     if (kid1==0 || kid2==0){
                                         //kid missing
                                         if (dad1==dad2){dadT += dad1; dadU +=dad1;}
                                         else{dadT+="h"; dadU+="h";}
                                         if (mom1==mom2){momT+=mom1; momU+=mom1;}
                                         else{momT+="h"; momU+="h";}
                                     }else if (kid1==kid2){
                                         //kid homozygous
                                         if(dad1==0){dadT+=kid1;dadU+="0";}
                                         else if (dad1==kid1){dadT+=dad1;dadU+=dad2;}
                                         else {dadT+=dad2;dadU+=dad1;}
 
                                         if(mom1==0){momT+=kid1;momU+="0";}
                                         else if (mom1==kid1){momT+=mom1;momU+=mom2;}
                                         else {momT+=mom2;momU+=mom1;}
                                     }else{
                                         //kid heterozygous and this if tree's a bitch
                                         if(dad1==0 && mom1==0){
                                             //both missing
                                             dadT+="0";dadU+="0";momT+="0";momU+="0";
                                         }else if (dad1==0 && mom1 != mom2){
                                             //dad missing mom het
                                             dadT+="0";dadU+="0";momT+="h";momU+="h";
                                         }else if (mom1==0 && dad1 != dad2){
                                             //dad het mom missing
                                             dadT+="h"; dadU+="h"; momT+="0"; momU+="0";
                                         }else if (dad1==0 && mom1 == mom2){
                                             //dad missing mom hom
                                             momT += mom1; momU += mom1; dadU+="0";
                                             if(kid1==mom1){dadT+=kid2;}else{dadT+=kid1;}
                                         }else if (mom1==0 && dad1==dad2){
                                             //mom missing dad hom
                                             dadT+=dad1;dadU+=dad1;momU+="0";
                                             if(kid1==dad1){momT+=kid2;}else{momT+=kid1;}
                                         }else if (dad1==dad2 && mom1 != mom2){
                                             //dad hom mom het
                                             dadT+=dad1; dadU+=dad2;
                                             if(kid1==dad1){momT+=kid2;momU+=kid1;
                                             }else{momT+=kid1;momU+=kid2;}
                                         }else if (mom1==mom2 && dad1!=dad2){
                                             //dad het mom hom
                                             momT+=mom1; momU+=mom2;
                                             if(kid1==mom1){dadT+=kid2;dadU+=kid1;
                                             }else{dadT+=kid1;dadU+=kid2;}
                                         }else if (dad1==dad2 && mom1==mom2){
                                             //mom & dad hom
                                             dadT+=dad1; dadU+=dad1; momT+=mom1; momU+=mom1;
                                         }else{
                                             //everybody het
                                             dadT+="h";dadU+="h";momT+="h";momU+="h";
                                         }
                                     }
                                     begin=true;
                                 }
                             }
                             momT+="\n";momU+="\n";dadT+="\n";dadU+="\n";
                             linkageToHapsWriter.write(currentInd.getFamilyID()+"-"+currentInd.getDadID()+"\tT\t" + dadT);
                             linkageToHapsWriter.write(currentInd.getFamilyID()+"-"+currentInd.getDadID()+"\tU\t" + dadU);
                             linkageToHapsWriter.write(currentInd.getFamilyID()+"-"+currentInd.getMomID()+"\tT\t" + momT);
                             linkageToHapsWriter.write(currentInd.getFamilyID()+"-"+currentInd.getMomID()+"\tU\t" + momU);
 
                             usedParents.add(currentInd.getFamilyID()+" "+currentInd.getDadID());
                             usedParents.add(currentInd.getFamilyID()+" "+currentInd.getMomID());
                         }
                     }
                 }
             }
         }
         linkageToHapsWriter.close();
 
     }
 }

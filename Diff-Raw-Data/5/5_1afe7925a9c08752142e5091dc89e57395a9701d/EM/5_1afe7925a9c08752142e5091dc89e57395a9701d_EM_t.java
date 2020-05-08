 package edu.mit.wi.haploview;
 
 import java.util.*;
 
 public class EM implements Constants {
     //results fields
     private int[][] haplotypes;
     private double[] frequencies;
     private Vector obsT, obsU;
     private Vector controlCounts, caseCounts;
 
     final int MISSINGLIMIT = 4;
     final int MAXLOCI = 500;
     final int MAXLN = 1000;
     final double PSEUDOCOUNT = 0.1;
     OBS[] data;
     SUPER_OBS[] superdata;
     int[] two_n = new int[32];
     int[][] ambighet;
     private Vector chromosomes;
     private int numTrios;
     private int numFilteredTrios;
     private boolean[][][] kidConsistentCache;
     private Vector realAffectedStatus;
     private MapWrap fullProbMap;
 
     EM(Vector chromosomes, int numTrios){
 
         this.chromosomes = chromosomes;
         this.numTrios = numTrios;
         //an old-school speedup courtesy of mjdaly
         two_n[0]=1;
         for (int i=1; i<31; i++){
             two_n[i]=2*two_n[i-1];
         }
     }
 
 
     class MapWrap {
         private HashMap theMap;
         private Double defaultVal;
 
         MapWrap(double d) {
             theMap = new HashMap();
             defaultVal = new Double(d);
         }
 
         double get(Object key) {
             if(theMap.containsKey(key)) {
                 return ((Double) theMap.get(key)).doubleValue();
             } else {
                 return defaultVal.doubleValue();
             }
         }
 
         void put(Object key, double val) {
             theMap.put(key,new Double(val));
         }
 
         void setDefaultVal(double val) {
             defaultVal = new Double(val);
         }
 
         void normalize(double normalizer) {
             Iterator itr = theMap.keySet().iterator();
             while(itr.hasNext()) {
                 Object key = itr.next();
                 put(key,(get(key) / normalizer));
             }
             defaultVal = new Double(defaultVal.doubleValue() / normalizer);
         }
 
         Set getKeySet() {
             return theMap.keySet();
         }
     }
 
     class Recovery {
         long h1;
         long h2;
         float p;
         public Recovery() {
             h1=0;
             h2=0;
             p=0.0f;
         }
     }
 
     class OBS {
         int nposs;
         //Recovery[] poss;
         Vector poss;
         public OBS() {
             //intialize poss vector to size passed in, and set its capacity increment to 16
             poss = new Vector(20,8);
             //poss = new Recovery[size];
             //for (int i=0; i<size; ++i) poss[i] = new Recovery();
         }
     }
 
     class SUPER_OBS {
         int nblocks;
         int[] nposs;
         Recovery[][] poss;
         int nsuper;
         Recovery[] superposs;
         public SUPER_OBS(int size) {
             poss = new Recovery[size][];
             nposs = new int[size];
         }
     }
 
     public void doEM(int[] theBlock) throws HaploViewException{
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
 
         byte[] thisHap;
         Vector inputHaploSingletons = new Vector();
         Vector inputHaploTrios = new Vector();
         Vector affSingletons = new Vector();
         Vector affTrios = new Vector();
         //whichVector[i] stores a value which indicates which vector chromosome i's genotype should go in
         //1 indicates inputHaploSingletons (singletons), 2 indicates inputHaploTrios,
         //3 indicates a person from a broken trio who is treated as a singleton
         //0 indicates none (too much missing data)
         int[] whichVector = new int[chromosomes.size()];
 
 
         for(int i=0;i<numTrios*4; i+=4) {
             Chromosome parentAFirst = (Chromosome) chromosomes.elementAt(i);
             Chromosome parentASecond = (Chromosome) chromosomes.elementAt(i+1);
             Chromosome parentBFirst = (Chromosome) chromosomes.elementAt(i+2);
             Chromosome parentBSecond = (Chromosome) chromosomes.elementAt(i+3);
             boolean tooManyMissingInASegmentA = false;
             boolean tooManyMissingInASegmentB = false;
             int totalMissingA = 0;
             int totalMissingB = 0;
             int segmentShift = 0;
             for (int n = 0; n < block_size.length; n++){
                 int missingA = 0;
                 int missingB = 0;
                 for (int j = 0; j < block_size[n]; j++){
                     byte AFirstGeno = parentAFirst.getGenotype(theBlock[segmentShift+j]);
                     byte ASecondGeno = parentASecond.getGenotype(theBlock[segmentShift+j]);
                     byte BFirstGeno = parentBFirst.getGenotype(theBlock[segmentShift+j]);
                     byte BSecondGeno = parentBSecond.getGenotype(theBlock[segmentShift+j]);
 
                     if(AFirstGeno == 0 || ASecondGeno == 0) missingA++;
                     if(BFirstGeno == 0 || BSecondGeno == 0) missingB++;
                 }
                 segmentShift += block_size[n];
                 if (missingA >= MISSINGLIMIT){
                     tooManyMissingInASegmentA = true;
                 }
                 if (missingB >= MISSINGLIMIT){
                     tooManyMissingInASegmentB = true;
                 }
                 totalMissingA += missingA;
                 totalMissingB += missingB;
             }
 
             if(!tooManyMissingInASegmentA && totalMissingA <= 1+theBlock.length/3
                     && !tooManyMissingInASegmentB && totalMissingB <= 1+theBlock.length/3) {
                 //both parents are good so all 4 chroms are added as a trio
                 whichVector[i] = 2;
                 whichVector[i+1] = 2;
                 whichVector[i+2] = 2;
                 whichVector[i+3] = 2;
             }
             else if(!tooManyMissingInASegmentA && totalMissingA <= 1+theBlock.length/3) {
                 //first person good, so he's added as a singleton, other parent is dropped
                 whichVector[i] = 3;
                 whichVector[i+1] =3;
                 whichVector[i+2] =0;
                 whichVector[i+3]=0;
             }
             else if(!tooManyMissingInASegmentB && totalMissingB <= 1+theBlock.length/3) {
                 //second person good, so he's added as a singleton, other parent is dropped
                 whichVector[i] = 0;
                 whichVector[i+1] =0;
                 whichVector[i+2] =3;
                 whichVector[i+3]=3;
             }
             else {
                 //both people have too much missing data so neither is used
                 whichVector[i] = 0;
                 whichVector[i+1] =0;
                 whichVector[i+2] =0;
                 whichVector[i+3]=0;
             }
 
         }
 
 
         for (int i = numTrios*4; i < chromosomes.size(); i++){
             Chromosome thisChrom = (Chromosome)chromosomes.elementAt(i);
             Chromosome nextChrom = (Chromosome)chromosomes.elementAt(++i);
             boolean tooManyMissingInASegment = false;
             int totalMissing = 0;
             int segmentShift = 0;
             for (int n = 0; n < block_size.length; n++){
                 int missing = 0;
                 for (int j = 0; j < block_size[n]; j++){
                     byte theGeno = thisChrom.getGenotype(theBlock[segmentShift+j]);
                     byte nextGeno = nextChrom.getGenotype(theBlock[segmentShift+j]);
                     if(theGeno == 0 || nextGeno == 0) missing++;
                 }
                 segmentShift += block_size[n];
                 if (missing >= MISSINGLIMIT){
                     tooManyMissingInASegment = true;
                 }
                 totalMissing += missing;
             }
             //we want to use chromosomes without too many missing genotypes in a given
             //subsegment (first term) or without too many missing genotypes in the
             //whole block (second term)
             if (!tooManyMissingInASegment && totalMissing <= 1+theBlock.length/3){
                 whichVector[i-1] = 1;
                 whichVector[i] = 1;
             }
         }
 
         //we only want to add an affected status every other chromosome, so we flip this boolean each time
         boolean addAff = true;
         for (int i = 0; i < chromosomes.size(); i++){
             Chromosome thisChrom = (Chromosome)chromosomes.elementAt(i);
 
             if(whichVector[i] > 0) {
                 thisHap = new byte[theBlock.length];
                 for (int j = 0; j < theBlock.length; j++){
                     byte a1 = Chromosome.getMarker(theBlock[j]).getMajor();
                     byte a2 = Chromosome.getMarker(theBlock[j]).getMinor();
                     byte theGeno = thisChrom.getGenotype(theBlock[j]);
                     if (theGeno >= 5){
                         thisHap[j] = 'h';
                     } else {
                         if (theGeno == 0){
                             thisHap[j] = '0';
                         }else if (theGeno == a1){
                             thisHap[j] = '1';
                         }else if (theGeno == a2){
                             thisHap[j] = '2';
                         }else{
                             throw new HaploViewException("Marker with > 2 alleles: " +
                                     Chromosome.getMarker(theBlock[j]).getName());
                         }
                     }
                 }
                 if(whichVector[i] == 1) {
                     inputHaploSingletons.add(thisHap);
                     if(addAff) {
                         affSingletons.add(new Integer(thisChrom.getAffected()));
                     }
                 }
                 else if(whichVector[i] ==2) {
                     inputHaploTrios.add(thisHap);
                     if(addAff) {
                         affTrios.add(new Integer(thisChrom.getAffected()));
                     }
                 }else if (whichVector[i] == 3){
                     inputHaploSingletons.add(thisHap);
                     if(addAff) {
                         affSingletons.add(new Integer(0));
                     }
                 }
                 if(addAff) {
                     addAff = false;
                 }
                 else {
                     addAff =true;
                 }
             }
         }
         numFilteredTrios  = inputHaploTrios.size() / 4;
         inputHaploTrios.addAll(inputHaploSingletons);
         affTrios.addAll(affSingletons);
 
         byte[][] input_haplos = (byte[][])inputHaploTrios.toArray(new byte[0][0]);
 
         full_em_breakup(input_haplos, block_size, affTrios);
 
 
     }
 
     private void full_em_breakup( byte[][] input_haplos, int[] block_size, Vector affStatus) throws HaploViewException{
         int num_poss, iter;
         double total = 0;
         int block, start_locus, end_locus, biggest_block_size;
         int num_indivs = 0;
 
         int num_blocks = block_size.length;
         int num_haplos = input_haplos.length;
         int num_loci = input_haplos[0].length;
 
         Recovery tempRec;
 
         if (num_loci > MAXLOCI){
             throw new HaploViewException("Too many loci in a single block (> "+MAXLOCI+" non-redundant)");
         }
 
         //figure out the size of the biggest block
         biggest_block_size=block_size[0];
         for (int i=1; i<num_blocks; i++) {
             if (block_size[i] > biggest_block_size)
                 biggest_block_size=block_size[i];
         }
 
         num_poss = two_n[biggest_block_size];
         data = new OBS[num_haplos/2];
         for (int i=0; i<num_haplos/2; i++)
             data[i]= new OBS();
 
         superdata = new SUPER_OBS[num_haplos/2];
         for (int i=0; i<num_haplos/2; i++)
             superdata[i]= new SUPER_OBS(num_blocks);
 
         double[][] hprob = new double[num_blocks][num_poss];
         int[][] hlist = new int[num_blocks][num_poss];
         int[] num_hlist = new int[num_blocks];
         int[] hint = new int[num_poss];
 
         MapWrap probMap = new MapWrap(PSEUDOCOUNT);
 
         /* for trio option */
         if (Options.getAssocTest() == ASSOC_TRIO) {
             ambighet = new int[(num_haplos/4)][num_loci];
             store_dhet_status(num_haplos,num_loci,input_haplos);
         }
 
         end_locus=-1;
         //now we loop through the blocks
         for (block=0; block<num_blocks; block++) {
             start_locus=end_locus+1;
             end_locus=start_locus+block_size[block]-1;
             num_poss=two_n[block_size[block]];
 
             //read_observations initializes the values in data[] (array of OBS)
             num_indivs=read_observations(num_haplos,num_loci,input_haplos,start_locus,end_locus);
 
             total=(double)num_poss;
             total *= PSEUDOCOUNT;
 
             /* starting prob is phase known haps + 0.1 (PSEUDOCOUNT) count of every haplotype -
             i.e., flat when nothing is known, close to phase known if a great deal is known */
 
             for (int i=0; i<num_indivs; i++) {
                 if (data[i].nposs==1) {
                     tempRec = (Recovery)data[i].poss.elementAt(0);
                     probMap.put(new Long(tempRec.h1), probMap.get(new Long(tempRec.h1)) + 1.0);
                     probMap.put(new Long(tempRec.h2), probMap.get(new Long(tempRec.h2)) + 1.0);
                     total+=2.0;
                 }
             }
 
             probMap.normalize(total);
 
             // EM LOOP: assign ambiguous data based on p, then re-estimate p
             iter=0;
             while (iter<20) {
                 // compute probabilities of each possible observation
                 for (int i=0; i<num_indivs; i++) {
                     total=0.0;
                     for (int k=0; k<data[i].nposs; k++) {
                         tempRec = (Recovery) data[i].poss.elementAt(k);
                         tempRec.p = (float)(probMap.get(new Long(tempRec.h1))*probMap.get(new Long(tempRec.h2)));
                         total+=tempRec.p;
                     }
                     // normalize
                     for (int k=0; k<data[i].nposs; k++) {
                         tempRec = (Recovery) data[i].poss.elementAt(k);
                         tempRec.p /= total;
                     }
                 }
 
                 // re-estimate prob
                 probMap = new MapWrap(1e-10);
 
                 total=num_poss*1e-10;
 
                 for (int i=0; i<num_indivs; i++) {
                     for (int k=0; k<data[i].nposs; k++) {
                         tempRec = (Recovery) data[i].poss.elementAt(k);
                         probMap.put(new Long(tempRec.h1),probMap.get(new Long(tempRec.h1)) + tempRec.p);
                         probMap.put(new Long(tempRec.h2),probMap.get(new Long(tempRec.h2)) + tempRec.p);
                         total+=(2.0*(tempRec.p));
                     }
                 }
 
                 probMap.normalize(total);
 
                 iter++;
             }
 
             int m=0;
             for(long j=0;j<num_poss; j++){
                 hint[(int)j]=-1;
                 if (probMap.get(new Long(j)) > .001) {
                     // printf("haplo %s   p = %.4lf\n",haplo_str(j,block_size[block]),prob[j]);
                     hlist[block][m]=(int)j;
                     hprob[block][m]=probMap.get(new Long(j));
                     hint[(int)j]=m;
                     m++;
                 }
             }
             num_hlist[block]=m;
 
             // store current block results in super obs structure
             store_block_haplos(hlist, hprob, hint, block, num_indivs);
 
         } /* for each block */
 
         double poss_full=1;
         for (block=0; block<num_blocks; block++) {
             poss_full *= num_hlist[block];
         }
 
         /* LIGATE and finish this mess :) */
 
         fullProbMap = new MapWrap(PSEUDOCOUNT);
 
         create_super_haplos(num_indivs,num_blocks,num_hlist);
 
         /* run standard EM on supercombos */
 
         /* start prob array with probabilities from full observations */
 
         total = poss_full * PSEUDOCOUNT;
 
         /* starting prob is phase known haps + 0.1 (PSEUDOCOUNT) count of every haplotype -
         i.e., flat when nothing is known, close to phase known if a great deal is known */
 
 
         for (int i=0; i<num_indivs; i++) {
             if (superdata[i].nsuper==1) {
                 Long h1 = new Long(superdata[i].superposs[0].h1);
                 Long h2 = new Long(superdata[i].superposs[0].h2);
 
                 fullProbMap.put(h1,fullProbMap.get(h1) +1.0);
                 fullProbMap.put(h2,fullProbMap.get(h2) +1.0);
 
                 total+=2.0;
             }
         }
 
         fullProbMap.normalize(total);
 
         /* EM LOOP: assign ambiguous data based on p, then re-estimate p */
         iter=0;
         while (iter<20) {
             /* compute probabilities of each possible observation */
             for (int i=0; i<num_indivs; i++) {
                 total=0.0;
                 for (int k=0; k<superdata[i].nsuper; k++) {
                     superdata[i].superposs[k].p = (float)
                             (fullProbMap.get(new Long(superdata[i].superposs[k].h1))*
                             fullProbMap.get(new Long(superdata[i].superposs[k].h2)));
                     total+=superdata[i].superposs[k].p;
                 }
                 /* normalize */
                 for (int k=0; k<superdata[i].nsuper; k++) {
                     superdata[i].superposs[k].p /= total;
                 }
             }
 
             /* re-estimate prob */
             fullProbMap = new MapWrap(1e-10);
 
             total=poss_full*1e-10;
 
             for (int i=0; i<num_indivs; i++) {
                 for (int k=0; k<superdata[i].nsuper; k++) {
                     fullProbMap.put(new Long(superdata[i].superposs[k].h1),fullProbMap.get(new Long(superdata[i].superposs[k].h1)) + superdata[i].superposs[k].p);
                     fullProbMap.put(new Long(superdata[i].superposs[k].h2),fullProbMap.get(new Long(superdata[i].superposs[k].h2)) + superdata[i].superposs[k].p);
                     total+=(2.0*superdata[i].superposs[k].p);
                 }
             }
 
             fullProbMap.normalize(total);
 
             iter++;
         }
 
         /* we're done - the indices of superprob now have to be
         decoded to reveal the actual haplotypes they represent */
 
         if(Options.getAssocTest() == ASSOC_TRIO) {
             kidConsistentCache = new boolean[numFilteredTrios][][];
             for(int i=0;i<numFilteredTrios*2;i+=2) {
                 if (((Integer)affStatus.elementAt(i)).intValue() == 2){
                     kidConsistentCache[i/2] = new boolean[superdata[i].nsuper][];
                     for (int n=0; n<superdata[i].nsuper; n++) {
                         kidConsistentCache[i/2][n] = new boolean[superdata[i+1].nsuper];
                         for (int m=0; m<superdata[i+1].nsuper; m++) {
                             kidConsistentCache[i/2][n][m] = kid_consistent(superdata[i].superposs[n].h1,
                                     superdata[i+1].superposs[m].h1,num_blocks,
                                     block_size,hlist,num_hlist,i/2,num_loci);
                         }
                     }
                 }
             }
         }
 
         realAffectedStatus = affStatus;
 
         doAssociationTests(affStatus, null);
 
 
         Vector haplos_present = new Vector();
         Vector haplo_freq= new Vector();
 
        ArrayList keys =  new ArrayList(fullProbMap.theMap.keySet());
        Collections.sort(keys);
        Iterator kitr = keys.iterator();
         while(kitr.hasNext()) {
             Object key = kitr.next();
             long keyLong = ((Long)key).longValue();
             if(fullProbMap.get(key) > .001) {
                 haplos_present.addElement(decode_haplo_str(keyLong,num_blocks,block_size,hlist,num_hlist));
                 haplo_freq.addElement(new Double(fullProbMap.get(key)));
             }
         }
 
         double[] freqs = new double[haplo_freq.size()];
         for(int j=0;j<haplo_freq.size();j++) {
             freqs[j] = ((Double)haplo_freq.elementAt(j)).doubleValue();
         }
 
 
         this.haplotypes = (int[][])haplos_present.toArray(new int[0][0]);
         this.frequencies = freqs;
 
         /*
         if (dump_phased_haplos) {
 
         if ((fpdump=fopen("emphased.haps","w"))!=NULL) {
         for (i=0; i<num_indivs; i++) {
         best=0;
         for (k=0; k<superdata[i].nsuper; k++) {
         if (superdata[i].superposs[k].p > superdata[i].superposs[best].p) {
         best=k;
         }
         }
         h1 = superdata[i].superposs[best].h1;
         h2 = superdata[i].superposs[best].h2;
         fprintf(fpdump,"%s\n",decode_haplo_str(h1,num_blocks,block_size,hlist,num_hlist));
         fprintf(fpdump,"%s\n",decode_haplo_str(h2,num_blocks,block_size,hlist,num_hlist));
         }
         fclose(fpdump);
         }
         }
         */
         //return 0;
     }
 
     public void doAssociationTests(Vector affStatus, Vector permuteInd) {
         if(fullProbMap == null || superdata == null || realAffectedStatus == null) {
             return;
         }
         if(affStatus == null){
             affStatus = realAffectedStatus;
         }
         if(permuteInd == null) {
             permuteInd = new Vector();
             for (int i = 0; i < superdata.length; i++){
                 permuteInd.add(new Boolean(false));
             }
         }
 
         Vector caseCounts = new Vector();
         Vector controlCounts = new Vector();
         if (Options.getAssocTest() == ASSOC_CC){
             MapWrap totalCase = new MapWrap(0);
             MapWrap totalControl = new MapWrap(0);
 
 
             for (int i = numFilteredTrios*2; i < superdata.length; i++){
                 MapWrap tempCase = new MapWrap(0);
                 MapWrap tempControl = new MapWrap(0);
                 double tempnorm=0;
 
                 for (int n=0; n<superdata[i].nsuper; n++) {
                     Long long1 = new Long(superdata[i].superposs[n].h1);
                     Long long2 = new Long(superdata[i].superposs[n].h2);
                     if (((Integer)affStatus.elementAt(i)).intValue() == 1){
                         tempControl.put(long1,tempControl.get(long1) + superdata[i].superposs[n].p);
                         tempControl.put(long2,tempControl.get(long2) + superdata[i].superposs[n].p);
                     }else if (((Integer)affStatus.elementAt(i)).intValue() == 2){
                         tempCase.put(long1,tempCase.get(long1) + superdata[i].superposs[n].p);
                         tempCase.put(long2,tempCase.get(long2) + superdata[i].superposs[n].p);
                     }
                     tempnorm += superdata[i].superposs[n].p;
                 }
                 if (tempnorm > 0.00) {
                     Iterator itr = fullProbMap.getKeySet().iterator();
                     while(itr.hasNext()) {
                         Long curHap = (Long) itr.next();
                         if (tempCase.get(curHap) > 0.0000 || tempControl.get(curHap) > 0.0000) {
                             totalCase.put(curHap,totalCase.get(curHap) + (tempCase.get(curHap)/tempnorm));
                             totalControl.put(curHap,totalControl.get(curHap) + (tempControl.get(curHap)/tempnorm));
                         }
                     }
                 }
             }
             ArrayList sortedKeySet = new ArrayList(fullProbMap.getKeySet());
             Collections.sort(sortedKeySet);
             for (int j = 0; j <sortedKeySet.size(); j++){
                 if (fullProbMap.get(sortedKeySet.get(j)) > .001) {
                     caseCounts.add(new Double(totalCase.get(sortedKeySet.get(j))));
                     controlCounts.add(new Double(totalControl.get(sortedKeySet.get(j))));
                 }
             }
         }
 
         Vector obsT = new Vector();
         Vector obsU = new Vector();
         if(Options.getAssocTest() == ASSOC_TRIO)
         {
             double product;
             MapWrap totalT = new MapWrap(0);
             MapWrap totalU = new MapWrap(0);
             for (int i=0; i<numFilteredTrios*2; i+=2) {
                 MapWrap tempT = new MapWrap(0);
                 MapWrap tempU = new MapWrap(0);
                 double tempnorm = 0;
                 if (((Integer)affStatus.elementAt(i)).intValue() == 2){
                     for (int n=0; n<superdata[i].nsuper; n++) {
                         for (int m=0; m<superdata[i+1].nsuper; m++) {
                             if(kidConsistentCache[i/2][n][m]) {
                                 product=superdata[i].superposs[n].p*superdata[i+1].superposs[m].p;
                                 Long h1 = new Long(superdata[i].superposs[n].h1);
                                 Long h2 = new Long(superdata[i].superposs[n].h2);
                                 Long h3 = new Long(superdata[i+1].superposs[m].h1);
                                 Long h4 = new Long(superdata[i+1].superposs[m].h2);
                                 if(((Boolean)permuteInd.get(i)).booleanValue()) {
                                     if (superdata[i].superposs[n].h1 != superdata[i].superposs[n].h2) {
                                         tempU.put(h1, tempU.get(h1) + product);
                                         tempT.put(h2, tempT.get(h2) + product);
                                     }
                                     if (superdata[i+1].superposs[m].h1 != superdata[i+1].superposs[m].h2) {
                                         tempU.put(h3, tempU.get(h3) + product);
                                         tempT.put(h4, tempT.get(h4) + product);
                                     }
                                 } else {
                                     if (superdata[i].superposs[n].h1 != superdata[i].superposs[n].h2) {
                                         tempT.put(h1, tempT.get(h1) + product);
                                         tempU.put(h2, tempU.get(h2) + product);
                                     }
                                     if (superdata[i+1].superposs[m].h1 != superdata[i+1].superposs[m].h2) {
                                         tempT.put(h3, tempT.get(h3) + product);
                                         tempU.put(h4, tempU.get(h4) + product);
                                     }
                                 }
                                 // normalize by all possibilities, even double hom
                                 tempnorm+=product;
                             }
                         }
                     }
 
                     if (tempnorm > 0.00) {
                         Iterator itr = fullProbMap.getKeySet().iterator();
                         while(itr.hasNext()) {
                             Long curHap = (Long) itr.next();
                             if (tempT.get(curHap) > 0.0000 || tempU.get(curHap) > 0.0000) {
                                 totalT.put(curHap, totalT.get(curHap) + tempT.get(curHap)/tempnorm);
                                 totalU.put(curHap, totalU.get(curHap) + tempU.get(curHap)/tempnorm);
                             }
                         }
                     }
                 }
             }
             ArrayList sortedKeySet = new ArrayList(fullProbMap.getKeySet());
             Collections.sort(sortedKeySet);
             for (int j = 0; j <sortedKeySet.size(); j++){
                 if (fullProbMap.get(sortedKeySet.get(j)) > .001) {
                     obsT.add(new Double(totalT.get(sortedKeySet.get(j))));
                     obsU.add(new Double(totalU.get(sortedKeySet.get(j))));
                 }
             }
         }
 
         if (Options.getAssocTest() == ASSOC_TRIO){
             this.obsT = obsT;
             this.obsU = obsU;
         } else if (Options.getAssocTest() == ASSOC_CC){
             this.caseCounts = caseCounts;
             this.controlCounts = controlCounts;
         }
     }
 
 
     public int read_observations(int num_haplos, int num_loci, byte[][] haplo, int start_locus, int end_locus) throws HaploViewException {
         //TODO: this should return something useful
         int i, j, a1, a2,  two_n, num_poss, loc, ind;
         long h1, h2;
         byte c1, c2;
         int num_indivs = 0;
         int[] dhet = new int[MAXLOCI];
         int[] missing1 = new int[MAXLOCI];
         int[] missing2 = new int[MAXLOCI];
         for (i=0; i<MAXLOCI; ++i) {
             dhet[i]=0;
             missing1[i]=0;
             missing2[i]=0;
         }
 
         for (ind=0; ind<num_haplos; ind+=2) {
 
             two_n=1; h1=h2=0; num_poss=1;
 
             for (loc=start_locus; loc<=end_locus; loc++) {
                 i = loc-start_locus;
 
                 c1=haplo[ind][loc]; c2=haplo[ind+1][loc];
                 if (c1=='h' || c1=='9') {
                     a1=0; a2=1; dhet[i]=1; missing1[i]=0; missing2[i]=0;
                 } else {
                     dhet[i]=0; missing1[i]=0; missing2[i]=0;
                     if (c1 > '0' && c1 < '3') {
                         a1=c1-'1';
                     }
                     else {
                         a1=0; missing1[i]=1;
                     }
 
                     if (c2 > '0' && c2 < '3') {
                         a2=c2-'1';
                     }
                     else {
                         a2=0;
                         missing2[i]=1;
                     }
                 }
 
                 h1 += two_n*a1;
                 h2 += two_n*a2;
                 if (dhet[i]==1) num_poss*=2;
                 if (missing1[i]==1) num_poss*=2;
                 if (missing2[i]==1) num_poss*=2;
 
                 two_n *= 2;
             }
 
             Recovery tempRec;
 
             if(data[num_indivs].poss.size() < num_poss)
             {
                 for(int k=data[num_indivs].poss.size();k<num_poss;k++)
                 {
                     tempRec = new Recovery();
                     data[num_indivs].poss.add(tempRec);
 
                 }
             }
 
             data[num_indivs].nposs = num_poss;
             tempRec = (Recovery) data[num_indivs].poss.elementAt(0);
             tempRec.h1=h1;
             tempRec.h2=h2;
             tempRec.p=0.0f;
 
             /*    printf("h1=%s, ",haplo_str(h1));
             printf("h2=%s, dhet=%d%d%d%d%d, nposs=%d\n",haplo_str(h2),dhet[0],dhet[1],dhet[2],dhet[3],dhet[4],num_poss); */
 
             two_n=1; num_poss=1;
             for (i=0; i<=end_locus-start_locus; i++) {
                 if (dhet[i]!=0) {
                     for (j=0; j<num_poss; j++) {
                         /* flip bits at this position and call this num_poss+j */
                         tempRec = (Recovery) data[num_indivs].poss.elementAt(j);
                         h1 = tempRec.h1;
                         h2 = tempRec.h2;
                         /* printf("FLIP: position %d, two_n=%d, h1=%d, h2=%d, andh1=%d, andh2=%d\n",i,two_n,h1,h2,h1&two_n,h2&two_n);  */
                         if ((h1&two_n)==two_n && (h2&two_n)==0) {
                             h1 -= two_n; h2 += two_n;
                         } else if ((h1&two_n)==0 && (h2&two_n)==two_n) {
                             h1 += two_n; h2 -= two_n;
                         } else {
                             //printf("error - attepmting to flip homozygous position\n");
                         }
                         tempRec = (Recovery) data[num_indivs].poss.elementAt(num_poss+j);
                         tempRec.h1=h1;
                         tempRec.h2=h2;
                         tempRec.p=0.0f;
                     }
                     num_poss *= 2;
                 }
 
                 if (missing1[i]!=0) {
                     for (j=0; j<num_poss; j++) {
                         /* flip bits at this position and call this num_poss+j */
                         tempRec = (Recovery) data[num_indivs].poss.elementAt(j);
                         h1 = tempRec.h1;
                         h2 = tempRec.h2;
                         /* printf("MISS1: position %d, two_n=%d, h1=%d, h2=%d, newh1=%d, newh2=%d\n",i,two_n,h1,h2,h1+two_n,h2); */
                         if ((h1&two_n)==0) {
                             h1 += two_n;
                         } else {
                             //printf("error - attempting to flip missing !=0\n");
                         }
                         tempRec = (Recovery) data[num_indivs].poss.elementAt(num_poss+j);
                         tempRec.h1=h1;
                         tempRec.h2=h2;
                         tempRec.p=0.0f;
                     }
                     num_poss *= 2;
                 }
 
                 if (missing2[i]!=0) {
                     for (j=0; j<num_poss; j++) {
                         /* flip bits at this position and call this num_poss+j */
                         tempRec = (Recovery) data[num_indivs].poss.elementAt(j);
                         h1 = tempRec.h1;
                         h2 = tempRec.h2;
                         /* printf("MISS2: position %d, two_n=%d, h1=%d, h2=%d, newh1=%d, newh2=%d\n",i,two_n,h1,h2,h1,h2+two_n);  */
                         if ((h2&two_n)==0) {
                             h2 += two_n;
                         } else {
                             //printf("error - attempting to flip missing !=0\n");
                         }
                         tempRec = (Recovery) data[num_indivs].poss.elementAt(num_poss+j);
                         tempRec.h1=h1;
                         tempRec.h2=h2;
                         tempRec.p=0.0f;
                     }
                     num_poss *= 2;
                 }
 
                 two_n *= 2;
             }
             /* printf("num_poss = %d  also %d\n",num_poss, data[num_indivs].nposs); */
             num_indivs++;
         }
 
         return(num_indivs);
     }
 
 
     public void store_block_haplos(int[][] hlist, double[][] hprob, int[] hint, int block, int num_indivs)
     {
         int i, j, k, num_poss,h1,h2;
         Recovery tempRec;
 
         for (i=0; i<num_indivs; i++) {
             num_poss=0;
             for (j=0; j<data[i].nposs; j++) {
                 tempRec = (Recovery) data[i].poss.elementAt(j);
                 h1 = (int)tempRec.h1;
                 h2 = (int)tempRec.h2;
                 if (hint[h1] >= 0 && hint[h2] >= 0) {
                     /* valid combination, both haplos passed to 2nd round */
                     num_poss++;
                 }
             }
             /* allocate and store */
             superdata[i].nposs[block]=num_poss;
             if (num_poss > 0) {
                 //superdata[i].poss[block] = (Recovery *) malloc (num_poss * sizeof(Recovery));
                 superdata[i].poss[block] = new Recovery[num_poss];
                 for (int ii=0; ii<num_poss; ++ii) superdata[i].poss[block][ii] = new Recovery();
                 k=0;
                 for (j=0; j<data[i].nposs; j++) {
                     tempRec = (Recovery) data[i].poss.elementAt(j);
                     h1 = (int)tempRec.h1;
                     h2 = (int)tempRec.h2;
                     if (hint[h1] >= 0 && hint[h2] >= 0) {
                         superdata[i].poss[block][k].h1 = hint[h1];
                         superdata[i].poss[block][k].h2 = hint[h2];
                         k++;
                     }
                 }
             }
         }
     }
 
     public int[] decode_haplo_str(long chap, int num_blocks, int[] block_size, int[][] hlist, int[] num_hlist)
     {
         int i, val,size=0,counter=0;
         for(i =0;i<num_blocks;i++) {
             size += block_size[i];
         }
         int[] decoded = new int[size];
 
         for (i=0; i<num_blocks; i++) {
 
             val = (int) (chap % num_hlist[i]);
             for(int j=0; j<block_size[i];j++) {
                 if((hlist[i][val] & two_n[j]) == two_n[j]) {
                     decoded[counter] = 2;
                 }
                 else {
                     decoded[counter]=1;
                 }
                 counter++;
             }
             chap -= val;
             chap /= num_hlist[i];
         }
         return decoded;
     }
 
     public void create_super_haplos(int num_indivs, int num_blocks, int[] num_hlist)
     {
         //System.out.println("Entering create_super_haplos");
         int i, j, num_poss, h1, h2;
 
         for (i=0; i<num_indivs; i++) {
             num_poss=1;
             for (j=0; j<num_blocks; j++) {
                 num_poss *= superdata[i].nposs[j];
             }
 
             superdata[i].nsuper=0;
             superdata[i].superposs = new Recovery[num_poss];
             //System.out.println(num_poss);
             for (int ii=0; ii<num_poss; ++ii) superdata[i].superposs[ii] = new Recovery();
 
             /* block 0 */
             for (j=0; j<superdata[i].nposs[0]; j++) {
                 h1 = (int)superdata[i].poss[0][j].h1;
                 h2 = (int)superdata[i].poss[0][j].h2;
                 recursive_superposs(h1,h2,1,num_blocks,num_hlist,i);
             }
 
             if (superdata[i].nsuper != num_poss) {
                 System.out.println("error in superfill" + " " + i);
             }
         }
     }
 
     public void recursive_superposs(long h1, long h2, int block, int num_blocks, int[] num_hlist, int indiv) {
         int j;
         long newh1, newh2, curr_prod;
 
         if (block == num_blocks) {
             superdata[indiv].superposs[superdata[indiv].nsuper].h1 = h1;
             superdata[indiv].superposs[superdata[indiv].nsuper].h2 = h2;
             superdata[indiv].nsuper++;
         } else {
             curr_prod=1;
             for (j=0; j<block; j++) { curr_prod*=num_hlist[j]; }
 
             for (j=0; j<superdata[indiv].nposs[block]; j++) {
                 newh1 = h1 + (superdata[indiv].poss[block][j].h1 * curr_prod);
                 newh2 = h2 + (superdata[indiv].poss[block][j].h2 * curr_prod);
                 recursive_superposs(newh1,newh2,block+1,num_blocks,num_hlist,indiv);
             }
         }
     }
 
     void store_dhet_status(int num_haplos, int num_loci, byte[][] haplo)
     {
         int ind, loc;
         byte c1, c2;
 
         for (ind=0; ind+2<num_haplos; ind+=4) {
             for (loc=0; loc<num_loci; loc++) {
                 c1=haplo[ind][loc];
                 c2=haplo[ind+2][loc];
                 ambighet[ind/4][loc]=0;
 
                 if (c1=='h' || c1=='9') {
                     if (c2=='h' || c2=='9' || c2=='0') {
                         ambighet[ind/4][loc]=1;
                     }
                 } else if (c1=='0' && (c2=='h' || c2=='9')) {
                     ambighet[ind/4][loc]=1;
                 }
             }
         }
     }
 
     boolean kid_consistent(long chap1, long chap2, int num_blocks, int[] block_size, int[][] hlist, int[] num_hlist, int this_trio, int num_loci)
     {
         int i;
         boolean retval;
 
         int[] temp1 = decode_haplo_str(chap1,num_blocks,block_size,hlist,num_hlist);
         int[] temp2 = decode_haplo_str(chap2,num_blocks,block_size,hlist,num_hlist);
 
         retval=true;
         for (i=0; i<num_loci; i++) {
             if (ambighet[this_trio][i] !=0) {
                 if (temp1[i] == temp2[i])
                 {
                     retval=false;
                     break;
                 }
             }
         }
         return(retval);
     }
 
     public int[][] getHaplotypes() {
         return haplotypes;
     }
 
     public void setHaplotypes(int[][] haplotypes) {
         this.haplotypes = haplotypes;
     }
 
     public double[] getFrequencies() {
         return frequencies;
     }
 
     public void setFrequencies(double[] frequencies) {
         this.frequencies = frequencies;
     }
 
     public double getTransCount(int i) {
         return ((Double)obsT.elementAt(i)).doubleValue();
     }
 
     public void setObsT(Vector obsT) {
         this.obsT = obsT;
     }
 
     public double getUntransCount(int i) {
         return ((Double)obsU.elementAt(i)).doubleValue();
     }
 
     public void setObsU(Vector obsU) {
         this.obsU = obsU;
     }
 
     public double getControlCount(int i) {
         return ((Double)controlCounts.elementAt(i)).doubleValue();
     }
 
     public void setControlCounts(Vector controlCounts) {
         this.controlCounts = controlCounts;
     }
 
     public double getCaseCount(int i) {
         return ((Double)caseCounts.elementAt(i)).doubleValue();
     }
 
     public void setCaseCounts(Vector caseCounts) {
         this.caseCounts = caseCounts;
     }
 
     public int numHaplos(){
         return haplotypes.length;
     }
 }

 package edu.mit.wi.tagger;
 
 import edu.mit.wi.haploview.Util;
 
 import java.util.*;
 import java.io.*;
 
 public class Tagger {
     public static final double DEFAULT_RSQ_CUTOFF = 0.8;
     public static final double DEFAULT_LOD_CUTOFF = 3.0;
     public static final int PAIRWISE_ONLY = 0;
     public static final int AGGRESSIVE_DUPLE = 1;
     public static final int AGGRESSIVE_TRIPLE = 2;
     public static final int NONE = 4;
     private static final long DEFAULT_MAXDIST = 500000;
     public static final int DEFAULT_MAXNUMTAGS = 0;
 
     //vector of SNP objects, which contains every SNP (tags and non-tags)
     private Vector snps;
 
     //vector of SNPs which must be included in the set of Tags
     //no object may be present in forceInclude and forceExclude concurrently.
     private Vector forceInclude;
 
     //vector of SNPs which can never be included in the set of Tags
     //no object may be present in forceInclude and forceExclude concurrently.
     private Vector forceExclude;
 
     private AlleleCorrelator alleleCorrelator;
     private double meanRSq;
     private int percentOver8;
     private double minRSquared;
     private int aggression;
     private int maxNumTags;
     private long maxComparisonDistance;
 
     //Vector of Tag objects determined by the most recent call to findTags()
     private Vector tags;
 
     //vector of sites which arent tagged by anything in the tags vector
     private Vector untagged;
 
     public int taggedSoFar;
 
     public Tagger(Vector s, Vector include, Vector exclude, AlleleCorrelator ac){
         this(s,include,exclude,ac,DEFAULT_RSQ_CUTOFF,AGGRESSIVE_TRIPLE, DEFAULT_MAXDIST, DEFAULT_MAXNUMTAGS);
     }
 
     public Tagger(Vector s, Vector include, Vector exclude, AlleleCorrelator ac, double rsqCut,
                   int aggressionLevel, long maxCompDist, int maxNumTags) {
         //todo: throw illegal argument exception if maxNumTags < include.size()
         minRSquared = rsqCut;
         aggression = aggressionLevel;
         this.maxNumTags = maxNumTags;
 
         if(maxCompDist < 0 ) {
             maxComparisonDistance = DEFAULT_MAXDIST;
         } else {
             maxComparisonDistance = maxCompDist;
         }
 
         if(s != null) {
             snps = s;
         } else {
             snps = new Vector();
         }
 
         if(include != null) {
             forceInclude = (Vector)include.clone();
         } else {
             forceInclude = new Vector();
         }
 
         if(exclude != null) {
             forceExclude = (Vector)exclude.clone();
         } else {
             forceExclude = new Vector();
         }
 
         alleleCorrelator = ac;
 
 
         for(int i=0;i<snps.size();i++) {
             VariantSequence curVarSeq = (VariantSequence) snps.get(i);
             if(curVarSeq.getTagComparator() == null) {
                 TagRSquaredComparator trsc = new TagRSquaredComparator(curVarSeq);
                 curVarSeq.setTagComparator(trsc);
             }
         }
     }
 
     public double getPairwiseCompRsq(VariantSequence a, VariantSequence b){
         return getPairwiseComp(a,b).getRsq();
     }
 
     public LocusCorrelation getPairwiseComp(VariantSequence a, VariantSequence b) {
         return alleleCorrelator.getCorrelation(a,b);
     }
 
     /**
      * This method finds Tags for the SNPs in the snps vector, and returns a vector of Tag objects
      */
     public Vector findTags() {
         tags = new Vector();
         untagged = new Vector();
         taggedSoFar = 0;
 
         //potentialTagsHash stores the PotentialTag objects keyed on the corresponding sequences
         Hashtable potentialTagByVarSeq = new Hashtable();
         PotentialTagComparator ptcomp = new PotentialTagComparator();
         VariantSequence currentVarSeq;
 
         //create SequenceComparison objects for each potential Tag, and
         //add any comparisons which have an r-squared greater than the minimum.
         for(int i=0;i<snps.size();i++) {
             currentVarSeq = (VariantSequence)snps.get(i);
 
             if(!(forceExclude.contains(currentVarSeq) ) ){
                 PotentialTag tempPT = new PotentialTag(currentVarSeq);
                 for(int j=0;j<snps.size();j++) {
                     if( maxComparisonDistance == 0 || Math.abs(((SNP)currentVarSeq).getLocation() - ((SNP)snps.get(j)).getLocation()) <= maxComparisonDistance)  {
                         if( getPairwiseCompRsq(currentVarSeq,(VariantSequence) snps.get(j)) >= minRSquared) {
                             tempPT.addTagged((VariantSequence) snps.get(j));
                         }
                     }
                 }
                 potentialTagByVarSeq.put(currentVarSeq,tempPT);
             }
         }
 
         Vector sitesToCapture = (Vector) snps.clone();
 
 
         debugPrint("snps to tag: " + sitesToCapture.size());
 
         Vector potentialTags = new Vector(potentialTagByVarSeq.values());
 
         int countTagged = 0;
         //add Tags for the ones which are forced in.
         Vector includedPotentialTags = new Vector();
         //construct a list of PotentialTag objects for forced in sequences
         for (int i = 0; i < forceInclude.size(); i++) {
             VariantSequence variantSequence = (VariantSequence) forceInclude.elementAt(i);
             if(variantSequence != null && potentialTagByVarSeq.containsKey(variantSequence)) {
                 includedPotentialTags.add((PotentialTag) potentialTagByVarSeq.get(variantSequence));
             }
         }
 
         //add each forced in sequence to the list of tags
         for(int i=0;i<includedPotentialTags.size();i++) {
             PotentialTag curPT = (PotentialTag) includedPotentialTags.get(i);
             HashSet newlyTagged = addTag(curPT,potentialTagByVarSeq,sitesToCapture);
             countTagged += newlyTagged.size();
             sitesToCapture.removeAll(newlyTagged);
             sitesToCapture.remove(curPT.sequence);
 
         }
 
         //loop until all snps are tagged
         while(sitesToCapture.size() > 0) {
             potentialTags = new Vector(potentialTagByVarSeq.values());
             if(potentialTags.size() == 0) {
                 //we still have sites left to capture, but we have no more available tags.
                 //this should only happen if the sites remaining in sitesToCapture were specifically
                 //excluded from being tags. Since we can't add any more tags, break out of the loop.
                 break;
             }
 
             //sorts the array of potential tags according to the number of untagged sites they can tag.
             //the last element is the one which tags the most untagged sites, so we choose that as our next tag.
             Collections.sort(potentialTags,ptcomp);
             PotentialTag currentBestTag = (PotentialTag) potentialTags.lastElement();
 
             HashSet newlyTagged = addTag(currentBestTag,potentialTagByVarSeq,sitesToCapture);
             countTagged += newlyTagged.size();
 
             sitesToCapture.removeAll(newlyTagged);
             sitesToCapture.remove(currentBestTag.sequence);
         }
        taggedSoFar = countTagged;
 
         if(sitesToCapture.size() > 0) {
             //any sites left in sitesToCapture could not be tagged, so we add them all to the untagged Vector
             untagged.addAll(sitesToCapture);
         }
 
         debugPrint("tagged " + countTagged + " SNPS using " + tags.size() +" tags" );
         debugPrint("# of SNPs that could not be tagged: " + untagged.size());
 
         if (aggression != PAIRWISE_ONLY){
             //peelback starting with the worst tag (i.e. the one that tags the fewest other snps.
             Vector tags2BPeeled = (Vector)tags.clone();
             Collections.reverse(tags2BPeeled);
             peelBack(tags2BPeeled);
         }
 
         //we've done the best we can. now we check to see if there's a limit to the
         //num of tags we're allowed to choose.
         if (maxNumTags > 0){
             //if so we need to chuck out the extras. figure out the utility of each tagSNP
             //i.e. how many SNPs for which they and their combos are the only tags
 
             while (getTagSNPs().size() > maxNumTags){
                 Vector tagSNPs = getTagSNPs();
                 potentialTagByVarSeq = new Hashtable();
                 Hashtable tagSeqByPotentialTag = new Hashtable();
                 //account for stuff tagged by snps themselves
                 for (int i = 0; i < tagSNPs.size(); i++){
                     TagSequence ts = (TagSequence) tagSNPs.get(i);
                     PotentialTag pt = new PotentialTag(ts.getSequence());
                     pt.addTagged(ts.getTagged());
                     potentialTagByVarSeq.put(ts.getSequence(),pt);
                     tagSeqByPotentialTag.put(pt,ts);
                 }
                 //go through all pt's and add their utilities as members of combos
                 Vector tagHaps = getTagHaplotypes();
                 for (int i = 0; i < tagHaps.size(); i++){
                     TagSequence ts = (TagSequence) tagHaps.get(i);
                     Block b = (Block) ts.getSequence();
                     for (int j = 0; j < b.getSnps().size(); j++){
                         ((PotentialTag)potentialTagByVarSeq.get(b.getSNP(j))).addTagged(ts.getTagged());
                     }
                 }
 
                 //now perform the steps of sorting and peeling
                 Vector potTagVec = new Vector(potentialTagByVarSeq.values());
                 Collections.sort(potTagVec,ptcomp);
 
                 PotentialTag dumpedPT = (PotentialTag)potTagVec.firstElement();
                 TagSequence dumpedTS = (TagSequence) tagSeqByPotentialTag.get(dumpedPT);
                 Vector taggedByCurTag = dumpedTS.getTagged();
                 for (int j = 0; j < taggedByCurTag.size(); j++){
                     //note for everything tagged by this guy that they're no longer tagged by him
                     VariantSequence vs =  (VariantSequence)taggedByCurTag.get(j);
                     vs.removeTag(dumpedTS);
                     if (vs.getTags().size() == 0){
                         taggedSoFar--;
                     }
                 }
                 tagHaps = getTagHaplotypes();
                 for (int i = 0; i < tagHaps.size(); i++){
                     TagSequence ts = (TagSequence) tagHaps.get(i);
                     Block b = (Block) ts.getSequence();
                     if (b.getSnps().contains(dumpedTS.getSequence())){
                         //this hap tag is now defunct because it was comprised in part by dumpedTS
                         Vector taggedByHap = ts.getTagged();
                         for (int j = 0; j < taggedByHap.size(); j++){
                             VariantSequence vs =  (VariantSequence)taggedByCurTag.get(j);
                             vs.removeTag(dumpedTS);
                             if (vs.getTags().size() == 0){
                                 taggedSoFar--;
                             }
                         }
                         tags.remove(ts);
                     }
                 }
 
                 tags.remove(dumpedTS);
             }
         }
 
 
         int count = 0;
         double numOver8 = 0;
         meanRSq = 0;
         Iterator itr = snps.iterator();
         while (itr.hasNext()){
             SNP s = (SNP) itr.next();
             TagSequence ts = s.getBestTag();
             if (ts != null){
                 double d = getPairwiseComp(s, ts.getSequence()).getRsq();
                 meanRSq += d;
                 count++;
                 if (d >= 0.8){
                     numOver8++;
                 }
             }
         }
         meanRSq /= count;
         percentOver8 = (int) Math.rint((100*numOver8) / count);
 
         return new Vector(tags);
     }
 
     private void peelBack(Vector tagsToBePeeled){
         Hashtable blockTagsByAllele = new Hashtable();
         HashSet snpsInBlockTags = new HashSet();
 
         debugPrint("starting peelback. untagged.size() = " + untagged.size());
 
         Vector availTagSNPs = new Vector();
         for (int j = 0; j < tags.size(); j++){
             availTagSNPs.add(((TagSequence)tags.get(j)).getSequence());
         }
 
         ListIterator uitr = untagged.listIterator();
 
         //try to tag things that weren't taggable in pairwise with haps
         while(uitr.hasNext()) {
             SNP curSnp = (SNP) uitr.next();
             HashSet comprehensiveBlock = new HashSet();
 
             comprehensiveBlock.add(curSnp);
             HashSet victor = curSnp.getLDList();
             victor.retainAll(availTagSNPs);
             comprehensiveBlock.addAll(victor);
 
             alleleCorrelator.phaseAndCache(comprehensiveBlock);
 
             LocusCorrelation bestPredictor = null;
             Vector potentialTests = generateTests(curSnp, (Vector) tags.clone());
             for (int j = 0; j < potentialTests.size(); j++){
                 LocusCorrelation lc = getPairwiseComp((VariantSequence)potentialTests.get(j),
                         curSnp);
                 if (lc.getRsq() >= minRSquared){
                     if (bestPredictor != null){
                         if (lc.getRsq() >
                                 bestPredictor.getRsq()){
                             bestPredictor = lc;
                         }
                     }else{
                         bestPredictor= lc;
                     }
                 }
             }
 
             if(bestPredictor != null) {
                 Allele bpAllele = bestPredictor.getAllele();
                 snpsInBlockTags.addAll(((Block)bpAllele.getLocus()).getSnps());
                 if (blockTagsByAllele.containsKey(bpAllele)){
                     TagSequence ts = (TagSequence)blockTagsByAllele.get(bpAllele);
                     ts.addTagged(curSnp);
                 }else{
                     TagSequence ts = new TagSequence(bpAllele);
                     ts.addTagged(curSnp);
                     tags.add(ts);
                     blockTagsByAllele.put(bpAllele,ts);
                 }
                uitr.remove();
                //note that we've caught another SNP
                taggedSoFar++;
             }
         }
 
         debugPrint("finished attempt at pairwise untaggables. untagged.size() = " + untagged.size());
 
         for (int i = 0; i < tagsToBePeeled.size(); i++){
             TagSequence curTag = (TagSequence) tagsToBePeeled.get(i);
             if (forceInclude.contains(curTag.getSequence()) ||
                     snpsInBlockTags.contains(curTag.getSequence())){
                 continue;
             }
             Vector taggedByCurTag = curTag.getTagged();
 
             //a hashset that contains all snps tagged by curtag
             //and all tag snps in LD with any of them
             HashSet comprehensiveBlock = new HashSet();
             availTagSNPs = new Vector();
             for (int j = 0; j < tags.size(); j++){
                 availTagSNPs.add(((TagSequence)tags.get(j)).getSequence());
             }
             availTagSNPs.remove(curTag.getSequence());
             for (int j = 0; j < taggedByCurTag.size(); j++) {
                 SNP snp = (SNP) taggedByCurTag.elementAt(j);
                 comprehensiveBlock.add(snp);
                 HashSet victor = snp.getLDList();
                 victor.retainAll(availTagSNPs);
                 comprehensiveBlock.addAll(victor);
             }
             alleleCorrelator.phaseAndCache(comprehensiveBlock);
 
             Hashtable bestPredictor = new Hashtable();
             boolean peelSuccessful = true;
             for (int k = 0; k < taggedByCurTag.size(); k++){
                 //look to see if we can find a predictor for each thing curTag tags
                 SNP thisTaggable = (SNP) taggedByCurTag.get(k);
                 Vector victor = (Vector) tags.clone();
                 victor.remove(curTag);
                 Vector potentialTests = generateTests(thisTaggable, victor);
                 for (int j = 0; j < potentialTests.size(); j++){
                     LocusCorrelation lc = getPairwiseComp((VariantSequence)potentialTests.get(j),
                             thisTaggable);
                     if (lc.getRsq() >= minRSquared){
                         if (bestPredictor.containsKey(thisTaggable)){
                             if (lc.getRsq() >
                                     ((LocusCorrelation)bestPredictor.get(thisTaggable)).getRsq()){
                                 bestPredictor.put(thisTaggable,lc);
                             }
                         }else{
                             bestPredictor.put(thisTaggable,lc);
                         }
                     }
                 }
                 if (thisTaggable.getTags().size() == 1 && !bestPredictor.containsKey(thisTaggable)){
                     peelSuccessful = false;
                     break;
                 }
             }
             if (peelSuccessful){
                 for (int k = 0; k < taggedByCurTag.size(); k++){
                     SNP thisTaggable = (SNP) taggedByCurTag.get(k);
                     //if more than one snp is tagged by the same
                     if (bestPredictor.containsKey(thisTaggable)){
                         Allele bpAllele = ((LocusCorrelation)bestPredictor.get(thisTaggable)).getAllele();
                         snpsInBlockTags.addAll(((Block)bpAllele.getLocus()).getSnps());
                         if (blockTagsByAllele.containsKey(bpAllele)){
                             TagSequence ts = (TagSequence)blockTagsByAllele.get(bpAllele);
                             ts.addTagged(thisTaggable);
                         }else{
                             TagSequence ts = new TagSequence(bpAllele);
                             ts.addTagged(thisTaggable);
                             tags.add(ts);
                             blockTagsByAllele.put(bpAllele,ts);
                         }
                     }
                     thisTaggable.removeTag(curTag);
                 }
                 tags.remove(curTag);
             }
         }
     }
 
     private Vector generateTests(SNP s, Vector availTags){
         //returns all duples and triples from availTags which are in LD
         //with SNP s, and with each other
         HashSet tagsInLD = new HashSet(s.getLDList());
         Vector availTagSNPs = new Vector();
         for (int i = 0; i < availTags.size(); i++){
             availTagSNPs.add(((TagSequence)availTags.get(i)).getSequence());
         }
         tagsInLD.retainAll(availTagSNPs);
         HashSet tests = new HashSet();
         Iterator lditr = tagsInLD.iterator();
         while (lditr.hasNext()){
             SNP curTag = (SNP) lditr.next();
             HashSet hs = new HashSet(curTag.getLDList());
             hs.retainAll(tagsInLD);
             Vector victor = new Vector(hs);
             if (aggression == AGGRESSIVE_DUPLE || aggression == AGGRESSIVE_TRIPLE){
                 //2 marker blocks
                 for (int i = 0; i < victor.size(); i++) {
                     Vector block = new Vector();
                     block.add(curTag);
                     block.add(victor.get(i));
                     tests.add(new Block(block));
                     if (aggression == AGGRESSIVE_TRIPLE){
                         //3 marker blocks
                         for(int j=i+1;j<victor.size();j++) {
                             //make sure these two snps are in LD with each other
                             if (((SNP)victor.get(i)).getLDList().contains(victor.get(j))){
                                 Vector block2 = (Vector) block.clone();
                                 block2.add(victor.get(j));
                                 tests.add(new Block(block2));
                             }
                         }
                     }
                 }
             }
         }
 
         return new Vector(tests);
     }
 
     private HashSet addTag(PotentialTag theTag,Hashtable potentialTagHash, Vector sitesToCapture) {
         Vector potentialTags = new Vector(potentialTagHash.values());
 
         potentialTags.remove(theTag);
         potentialTagHash.remove(theTag.sequence);
         //newlyTagged contains alleles which were not tagged by anything in the set of tags before,
         //and are now tagged by theTag.
         HashSet newlyTagged = ((PotentialTag)theTag).tagged;
 
         TagSequence tagSeq = new TagSequence(theTag.sequence);
         tags.add(tagSeq);
 
         Iterator itr = potentialTagHash.keySet().iterator();
         Vector toRemove = new Vector();
         //iterate through the list of available tags, and remove the newly tagged alleles from
         //the list of alleles that each PotentialTag can tag. (since we want to choose our next tag
         // according to which will tag the most untagged alleles )
         while(itr.hasNext()) {
             PotentialTag pt = (PotentialTag) potentialTagHash.get(itr.next());
             pt.removeTagged(newlyTagged);
             pt.removeTagged(theTag.sequence);
             //if a PotentialTag cannot tag any other uncaptured sites, then we want to remove it from contention,
             //unless its sequence still needs to be captured.
             if(pt.taggedCount() == 0 && !sitesToCapture.contains(pt.sequence)) {
                 toRemove.add(pt.sequence);
             }
         }
 
         for(int i=0;i<toRemove.size();i++) {
             potentialTags.remove(potentialTagHash.remove(toRemove.get(i)));
         }
 
         //loop through the list of alleles the newly added tag can capture, and
         //add them to the TagSequence object.
         //we add all the alleles the tag can capture, _not_ just the newly tagged alleles.
         Iterator ptitr = theTag.allTagged.iterator();
         while(ptitr.hasNext()) {
             tagSeq.addTagged((VariantSequence)ptitr.next());
         }
 
         return newlyTagged;
     }
 
     public void dumpTests(File outFile) throws IOException{
         BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
         for (int i = 0; i < tags.size(); i++){
             bw.write(((TagSequence)tags.get(i)).getTestName());
             bw.newLine();
         }
         bw.close();
     }
 
     class PotentialTag {
         VariantSequence sequence;
         // tagged contains the sequences which this sequence can tag, which are not yet tagged
         //(this is used in the while loop in findTags() )
         HashSet tagged;
         //allTagged contains all sequences that this sequence can tag, regardless of what tags have already been chosen
         HashSet allTagged;
 
         public PotentialTag(VariantSequence s) {
             sequence = s;
             tagged = new HashSet();
             allTagged = new HashSet();
         }
 
         public void addTagged(VariantSequence vs) {
             tagged.add(vs);
             allTagged.add(vs);
         }
 
         public void addTagged(Collection c){
             tagged.addAll(c);
             allTagged.addAll(c);
         }
 
         public void removeTagged(VariantSequence vs) {
             tagged.remove(vs);
         }
 
         public void removeTagged(Collection c) {
             tagged.removeAll(c);
         }
 
         //this returns the number of sequences that havent been tagged this sequence can tag
         public int taggedCount() {
             return tagged.size();
         }
 
     }
 
     public void setExclude(Vector e) {
         if(e != null) {
             forceExclude = (Vector) e.clone();
         }
     }
 
     public void setInclude(Vector e) {
         if(e != null) {
             forceInclude = (Vector) e.clone();
         }
     }
 
     public void clearExclude() {
         forceExclude.removeAllElements();
     }
 
     public void clearInclude() {
         forceInclude.removeAllElements();
     }
 
     public Vector getTags() {
         return tags;
     }
 
     public Vector getTagSNPs(){
         Vector res = new Vector();
         Iterator itr = tags.iterator();
         while (itr.hasNext()){
             TagSequence t = (TagSequence) itr.next();
             if (t.getSequence() instanceof SNP){
                 res.add(t);
             }
         }
 
         return res;
     }
 
     public Vector getTagHaplotypes(){
         Vector res = new Vector();
         Iterator itr = tags.iterator();
         while (itr.hasNext()){
             TagSequence t = (TagSequence) itr.next();
             if (t.getSequence() instanceof Block){
                 res.add(t);
             }
         }
 
         return res;
     }
 
     public Vector getForceInclude() {
         return forceInclude;
     }
 
     public int getUntaggableCount() {
         return untagged.size();
     }
 
     public void saveResultToFile(File outFile) throws IOException {
         BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
 
         bw.write("#tagging with r^2 cutoff: " + minRSquared);
         bw.newLine();
         bw.write("#captured " + taggedSoFar + " of " + snps.size() +" alleles with mean r^2 of " + Util.roundDouble(meanRSq, 3));
         bw.newLine();
         bw.write("#captured " + percentOver8 + " percent of alleles with r^2 > 0.8");
         bw.newLine();
         bw.write("#using " + getTagSNPs().size() + " SNPs in " + tags.size() + " tests.");
         bw.newLine();
 
         bw.write("Marker\tBest Test\tr^2 w/test");
         bw.newLine();
         for (int i = 0; i < snps.size(); i++) {
             StringBuffer line = new StringBuffer();
             SNP snp = (SNP) snps.elementAt(i);
             line.append(snp.getName()).append("\t");
             TagSequence theTag = snp.getBestTag();
             if(theTag != null) {
                 line.append(theTag.getName()).append("\t");
                 line.append(getPairwiseCompRsq(snp,theTag.getSequence())).append("\t");
             }
             bw.write(line.toString());
             bw.newLine();
         }
 
         bw.newLine();
 
         bw.write("Test\tAlleles Captured");
         bw.newLine();
         for(int i=0;i<tags.size();i++) {
             StringBuffer line = new StringBuffer();
             TagSequence theTag = (TagSequence) tags.get(i);
             line.append(theTag.getName()).append("\t");
             Vector tagged = theTag.getBestTagged();
             for (int j = 0; j < tagged.size(); j++) {
                 VariantSequence varSeq = (VariantSequence) tagged.elementAt(j);
                 if(j !=0){
                     line.append(",");
                 }
                 line.append(varSeq.getName());
             }
             bw.write(line.toString());
             bw.newLine();
         }
 
         bw.close();
     }
 
     class PotentialTagComparator implements Comparator {
         public int compare(Object o1, Object o2) {
             return ((PotentialTag)o1).taggedCount() -  ((PotentialTag)o2).taggedCount();
         }
     }
 
     class TagRSquaredComparator implements Comparator {
         VariantSequence seq;
 
         public TagRSquaredComparator(VariantSequence s) {
             seq = s;
         }
 
         public int compare(Object o1, Object o2) {
             if(getPairwiseCompRsq(seq,((TagSequence)o1).getSequence()) ==
                     getPairwiseCompRsq(seq,((TagSequence)o2).getSequence())) {
                 return 0;
             } else if (getPairwiseCompRsq(seq,((TagSequence)o1).getSequence()) >
                     getPairwiseCompRsq(seq,((TagSequence)o2).getSequence())) {
                 return 1;
             } else {
                 return -1;
             }
         }
     }
 
     boolean debug = false;
 
     void debugPrint(String s) {
         if(debug) {
             System.out.println(s);
         }
     }
 
     public double getMeanRSq() {
         return meanRSq;
     }
 
     public int getFracOver8() {
         return percentOver8;
     }
 }

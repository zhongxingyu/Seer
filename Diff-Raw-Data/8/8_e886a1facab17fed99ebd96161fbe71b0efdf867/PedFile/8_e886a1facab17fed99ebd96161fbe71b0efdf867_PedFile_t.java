 /*
* $Id: PedFile.java,v 1.5 2003/10/15 15:37:58 jcbarret Exp $
 * WHITEHEAD INSTITUTE
 * SOFTWARE COPYRIGHT NOTICE AGREEMENT
 * This software and its documentation are copyright 2002 by the
 * Whitehead Institute for Biomedical Research.  All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support
 * whatsoever.  The Whitehead Institute can not be responsible for its
 * use, misuse, or functionality.
 */
 package edu.mit.wi.pedfile;
 
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.StringTokenizer;
 import java.util.Enumeration;
 
 /**
  * Handles input and storage of Pedigree files
  *
  * this class is not thread safe (untested).
  * modified from original Pedfile and checkdata classes by Hui Gong
  * @author Julian Maller
  */
 public class PedFile {
     private Hashtable families;
 
     /*
     * stores the familyIDs and individualIDs of all individuals found by parse()
     * in order. this is usefull for outputting Pedigree information to a file of another type.
     * the information is stored as an array of two strings.
     */
     private Vector order;
 
     public PedFile(){
         this.families = new Hashtable();
     }
 
     /**
      * gets the order Vector
      * @return
      */
     public Vector getOrder() {
         return order;
     }
 
     /**
      *
      * @return enumeration containing a list of familyID's in the families hashtable
      */
     public Enumeration getFamList(){
         return this.families.keys();
     }
     /**
      *
      * @param familyID id of desired family
      * @return Family identified by familyID in families hashtable
      */
     public Family getFamily(String familyID){
         return (Family)this.families.get(familyID);
     }
 
     /**
      *
      * @return the number of Family objects in the families hashtable
      */
     public int getNumFamilies(){
         return this.families.size();
     }
 
     /**
      * this method iterates through each family in Hashtable families and adds up
      * the number of individuals in total across all families
      * @return the total number of individuals in all the family objects in the families hashtable
      */
     public int getNumIndividuals(){
         Enumeration enum = this.families.elements();
         int total =0;
         while (enum.hasMoreElements()) {
             Family fam = (Family) enum.nextElement();
             total += fam.getNumMembers();
         }
         return total;
     }
 
     /**
      * finds the first individual in the first family and returns the number of markers for that individual
      * @return the number of markers
      */
     public int getNumMarkers(){
         Enumeration famList = this.families.elements();
         int numMarkers = 0;
         while (famList.hasMoreElements()) {
             Family fam = (Family) famList.nextElement();
             Enumeration indList = fam.getMemberList();
             Individual ind = new Individual();
             while(indList.hasMoreElements()){
                 try{
                     ind = fam.getMember((String)indList.nextElement());
                 }catch(PedFileException pfe){
                 }
                 numMarkers = ind.getNumMarkers();
                 if(numMarkers > 0){
                     return numMarkers;
                 }
             }
         }
         return 0;
     }
 
 
     /**
      * takes in a pedigree file in the form of a vector of strings and parses it.
      * data is stored in families in the member hashtable families
      * @param pedigrees a Vector of strings containing one pedigree line per string
      */
     public void parse(Vector pedigrees) throws PedFileException {
         int colNum = -1;
         boolean withOptionalColumn = false;
         int numLines = pedigrees.size();
         Individual ind;
 
         this.order = new Vector();
 
         for(int k=0;k<numLines;k++){
             StringTokenizer tokenizer = new StringTokenizer((String)pedigrees.get(k), "\n\t\" \"");
             //reading the first line
             if(colNum < 0){
                 //only check column number count for the first line
                 colNum = tokenizer.countTokens();
                 if(colNum%2==1) {
                     withOptionalColumn = true;
                 }
             }
             if(colNum != tokenizer.countTokens()) {
                 //this line has a different number of columns
                 //should send some sort of error message
                 //TODO: add something which stores number of markers for all lines and checks that they're consistent
                 throw new PedFileException("line number mismatch in pedfile. line " + (k+1));
             }
 
             ind = new Individual();
 
             if(tokenizer.hasMoreTokens()){
 
                 ind.setFamilyID(tokenizer.nextToken().trim());
                 ind.setIndividualID(tokenizer.nextToken().trim());
                 ind.setDadID(tokenizer.nextToken().trim());
                 ind.setMomID(tokenizer.nextToken().trim());
                 try {
                     ind.setGender(Integer.parseInt(tokenizer.nextToken().trim()));
                     ind.setAffectedStatus(Integer.parseInt(tokenizer.nextToken().trim()));
                     if(withOptionalColumn) {
                         ind.setLiability(Integer.parseInt(tokenizer.nextToken().trim()));
                     }
                 }catch(NumberFormatException nfe) {
                     throw new PedFileException("Pedfile error: invalid gender or affected status on line " + (k+1));
                 }
 
                 boolean isTyped = false;
                 while(tokenizer.hasMoreTokens()){
                     try {
                         int allele1 = Integer.parseInt(tokenizer.nextToken().trim());
                         int allele2 = Integer.parseInt(tokenizer.nextToken().trim());
                         if ( !( (allele1==0) && (allele2 == 0) ) ) isTyped = true;
                         if(allele1 <0 || allele1 > 4 || allele2 <0 || allele2 >4) {
                            throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k+1)
                                     + ".\n all genotypes must be 0-4.");
                         }
                         byte[] markers = new byte[2];
                         markers[0] = (byte)allele1;
                         markers[1]= (byte)allele2;
                         ind.addMarker(markers);
                     }catch(NumberFormatException nfe) {
                        throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k+1) );
                     }
                 }
 
                 //note whether this is a real indiv (true) or a "dummy" (false)
                 ind.setIsTyped(isTyped);
 
                 //check if the family exists already in the Hashtable
                 Family fam = (Family)this.families.get(ind.getFamilyID());
                 if(fam == null){
                     //it doesnt exist, so create a new Family object
                     fam = new Family(ind.getFamilyID());
                 }
                 fam.addMember(ind);
                 this.families.put(ind.getFamilyID(),fam);
 
                 String[] indFamID = new String[2];
                 indFamID[0] = ind.getFamilyID();
                 indFamID[1] = ind.getIndividualID();
                 this.order.add(indFamID);
 
             }
         }
     }
 
     public Vector check() throws PedFileException{
         CheckData cd = new CheckData(this);
         Vector results = cd.check();
         /*int size = results.size();
         for (int i = 0; i < size; i++) {
         MarkerResult markerResult = (MarkerResult) results.elementAt(i);
         System.out.println(markerResult.toString());
         }*/
         return results;
     }
 
 }
 
 

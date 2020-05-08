 package gr.auth.ee.lcs.data;
 
 
 /** 
  *  An data object for the *S-LCS update algorithm
  */
 public class GenericSLCSClassifierData {
 
   /** 
    *  niche set size estimation
    */
  public double ns=1;
 
   /** 
    *  Match Set Appearances
    */
  public int msa=0;
 
   /** 
    *  true positives
    */
  public int tp=0;
 
   /** 
    *  false positives
    */
   public int fp=0;
   
   /**
    * Strength
    */
   public double str=0;
   
   public Object clone(){
 	  return this.clone();
   }
 
 }

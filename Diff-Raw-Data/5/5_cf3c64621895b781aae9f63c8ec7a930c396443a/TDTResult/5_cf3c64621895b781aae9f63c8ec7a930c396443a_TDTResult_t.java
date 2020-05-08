 package edu.mit.wi.haploview;
 
 import edu.mit.wi.pedfile.MathUtil;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.FieldPosition;
 import java.text.DecimalFormatSymbols;
 import java.util.Locale;
 
 
 public class TDTResult {
     int[][] counts;
     SNP theSNP;
     private byte allele1=0,allele2=0;
     int numZero;
     boolean tallyHet = false;
     double chiSqVal;
     boolean chiSet = false;
 
 
     public TDTResult(SNP tempSNP) {
         counts = new int[2][2];
         this.theSNP = tempSNP;
     }
 
     public void tallyCCInd(byte a1, byte a2, int cc){
         //case = 0, control = 1 for int cc
        if (a1 >= 5 && a2 >= 5){
             counts[cc][0]++;
             counts[cc][1]++;
         }else{
             //seed the alleles as soon as they're found
             if (allele1 == 0){
                 allele1 = a1;
             }
             if (a1 != 0){
                 if (a1 == allele1){
                     counts[cc][0] ++;
                 }else{
                     counts[cc][1] ++;
                 }
             }
             if (a2 != 0){
                 if (a2 == allele1){
                     counts[cc][0]++;
                 }else{
                     counts[cc][1]++;
                 }
             }
         }
     }
 
     public void tallyTrioInd(byte alleleT, byte alleleU) {
        if(alleleT >= 5 && alleleU >= 5) {
             if(tallyHet){
                 counts[0][0]++;
                 counts[1][1]++;
                 counts[0][1]++;
                 counts[1][0]++;
                 this.tallyHet = false;
             }
             else {
                 this.tallyHet = true;
             }
         }
         else if( (alleleT != alleleU) && (alleleT!=0) && (alleleU!=0) ) {
             if(allele1==0 && allele2==0 ) {
                 allele1 = alleleT;
                 allele2 = alleleU;
             }
 
             if(alleleT == allele1) {
                 counts[0][0]++;
             }
             else if(alleleT==allele2) {
                 counts[0][1]++;
             }
             if(alleleU == allele1){
                 counts[1][0]++;
             }
             else if(alleleU == allele2) {
                 counts[1][1]++;
             }
 
         }
         //System.out.println( counts[0][0] + "\t" + counts[1][1] + "\t" + counts[0][1] + "\t" + counts[1][0]);
 
     }
 
     public String getName() {
         return this.theSNP.getName();
     }
 
     public double getChiSq(int type) {
         if(!this.chiSet){
             if (type == 1){
                 this.chiSqVal = Math.pow( (this.counts[0][0] - this.counts[0][1]),2) / (this.counts[0][0] + this.counts[0][1]);
             }else{
                 int N = counts[0][0] + counts[0][1] + counts[1][0] + counts[1][1];
                 for (int i = 0; i < 2; i++){
                     for (int j = 0; j < 2; j++){
                         double nij = ((double)(counts[i][0] + counts[i][1])*(counts[0][j] + counts[1][j]))/N;
                         this.chiSqVal += Math.pow( (this.counts[i][j] - nij), 2) / nij;
                     }
                 }
             }
             this.chiSqVal = Math.rint(this.chiSqVal*1000.0)/1000.0;
             this.chiSet = true;
         }
         return this.chiSqVal;
     }
 
     public String getTURatio(int type) {
         if (type == 1){
             return this.counts[0][0] + ":" + this.counts[0][1];
         }else{
             return this.counts[0][0] + ":" + this.counts[0][1] +
                     ", " + this.counts[1][0] + ":" + this.counts[1][1];
         }
     }
 
     public String getPValue() {
         double pval = 0;
         pval= MathUtil.gammq(.5,.5*this.chiSqVal);
         DecimalFormat df;
         //java truly sucks for simply restricting the number of sigfigs but still
         //using scientific notation when appropriate
         if (pval < 0.0001){
             df = new DecimalFormat("0.0000E0", new DecimalFormatSymbols(Locale.US));
         }else{
             df = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
         }
         String formattedNumber =  df.format(pval, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
         return formattedNumber;
     }
 
 }

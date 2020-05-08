 package com.runninggee57.sequence_alignment;
 
 
 public class LinearSpaceAlign {
  public static final int GAP_PEN = 5;
   public static final String SEQFILENAME = "pair.txt";
   public static final String SUBFILENAME = "nucleotide_sub.txt";
   
   public static SubMatrix sub;
   
   public static void main(String[] args) {
     String seqs[] = Align.readSeqFile(SEQFILENAME);
     sub = new SubMatrix(SUBFILENAME);
     
     String aligned[] = Hirschberg(seqs[0], seqs[1]);
     
     System.out.println("Aligned Sequences!");
     System.out.println("Sequence 1: " + aligned[0]);
     System.out.println("Sequence 2: " + aligned[1]);
   }
   
   public static String[] NeedlemanWunsch(String X, String Y) {
     int F[][] = new int[X.length() + 1][Y.length() + 1];
     
     for (int i = 0; i <= X.length(); i++) {
       if (i == 0)
         F[i][0] = GAP_PEN * i;
       else
         F[i][0] = GAP_PEN * i;
     }
     
     for (int j = 0; j <= Y.length(); j++) {
       if (j == 0)
         F[0][j] = GAP_PEN * j;
       else
         F[0][j] = GAP_PEN * j;
     }
     
     for (int i = 1; i <= X.length(); i++) {
       for (int j = 1; j <= Y.length(); j++) {
         int match_score = F[i-1][j-1] + sub.get(X.charAt(i - 1), Y.charAt(j - 1));
         int delete_score = F[i-1][j] + GAP_PEN;
         int insert_score = F[i][j-1] + GAP_PEN;
         F[i][j] = Math.max(Math.max(match_score, delete_score), insert_score);
       }
     }
     
     String alignedX = "";
     String alignedY = "";
     int i = X.length();
     int j = Y.length();
     while (i > 0 || j > 0) {
       if (i > 0 && j > 0 && F[i][j] == F[i-1][j-1] + sub.get(X.charAt(i - 1), Y.charAt(j - 1)))
       {
         alignedX = X.charAt(i - 1) + alignedX;
         alignedY = Y.charAt(j - 1) + alignedY;
         i--;
         j--;
       }
       else if (i > 0 && F[i][j] == F[i-1][j] + GAP_PEN)
       {
         alignedX = X.charAt(i - 1) + alignedX;
         alignedY = "-" + alignedY;
         i--;
       }
       else
       {
         alignedX = "-" + alignedX;
         alignedY = Y.charAt(j - 1) + alignedY;
         j--;
       }
     }
     return new String[]{alignedX, alignedY};
   }
   
   public static String[] Hirschberg(String X, String Y) {
     String alignedX = "", alignedY = "";
     
     if (X.length() == 0 || Y.length() == 0) {
       if (X.length() == 0) {
         for (int i = 0; i < Y.length(); i++) {
           alignedX += '-';
           alignedY += Y.charAt(i);
         }
       }
       else if (Y.length() == 0) {
         for (int i = 0; i < X.length(); i++) {
           alignedX += X.charAt(i);
           alignedY += '-';
         }
       }
     }
     else if (X.length() == 1 || Y.length() == 1) {
       String newS[] = NeedlemanWunsch(X, Y);
       alignedX += newS[0];
       alignedY += newS[1];
     }
     else {
       int xmid = X.length() / 2;
       
       int scoreL[] = NWScore(X.substring(0, xmid), Y);
       int scoreR[] = NWScore(new StringBuffer(X.substring(xmid, X.length())).reverse().toString(), new StringBuilder(Y).reverse().toString());
       int ymid = PartitionY(scoreL, scoreR);
       
       String newS[] = Hirschberg(X.substring(0, xmid), Y.substring(0, ymid));
       alignedX += newS[0];
       alignedY += newS[1];
       
       newS = Hirschberg(X.substring(xmid, X.length()), Y.substring(ymid, Y.length()));
       alignedX += newS[0];
       alignedY += newS[1];
     }
     return new String[]{alignedX, alignedY};
   }
   
   public static int[] NWScore(String X, String Y) {
     int rowOne[] = new int[Y.length() + 1];
     int rowTwo[] = new int[Y.length() + 1]; // this only needs 2 rows the whole time, where memory improvement comes frm
     
     rowOne[0] = 0;
     for (int j = 1; j <= Y.length(); j++) {
      rowOne[j] = rowOne[j] + GAP_PEN;
     }
     
     for (int i = 1; i <= X.length(); i++) {
       rowTwo[0] = rowOne[0] + GAP_PEN;
       for (int j = 1; j <= Y.length(); j++) {
         int scoreSub = rowOne[j-1] + sub.get(X.charAt(i - 1), Y.charAt(j - 1));
         int scoreDel = rowOne[j] + GAP_PEN;
         int scoreIns = rowTwo[j-1] + GAP_PEN;
         rowTwo[j] = Math.max(Math.max(scoreSub, scoreDel), scoreIns);
       }
       rowOne = rowTwo;
     }
     
     return rowTwo;
   }
   
   public static int PartitionY(int scoreL[], int scoreR[]) {
     int maxVal = scoreL[0] + scoreR[0];
     int maxIndex = 0;
     
     for (int i = 1; i < scoreL.length; i++) {
       int val = scoreL[i] + scoreR[i];
       if (val > maxVal) {
         maxVal = val;
         maxIndex = i;
       }
     }
     
     return maxIndex;
   }
 }

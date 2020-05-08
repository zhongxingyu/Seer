 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.io.DPMatrixExporter;
 import de.bioinformatikmuenchen.pg4.alignment.io.IAlignmentOutputFormatter;
 import de.bioinformatikmuenchen.pg4.alignment.io.IDPMatrixExporter;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class SmithWaterman extends AlignmentProcessor {
 
     private double[][] matrix;
     //Matrices that save whether any given field got its value from the specified direction
     private boolean[][] leftTopArrows;
     private boolean[][] leftArrows;
     private boolean[][] topArrows;
     private int xSize = -1;
     private int ySize = -1;
     private String querySequence;
     private String targetSequence;
     //IDs / Names of the sequences
     private String querySequenceId;
     private String targetSequenceId;
     private String querySequenceStruct;
     private String targetSequenceStruct;
     private double score;
     boolean[][] leftPath;
     boolean[][] leftTopPath;
     boolean[][] topPath;
     boolean[][] hasPath;
 
     public SmithWaterman(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost) {
         super(mode, algorithm, distanceMatrix, gapCost);
         //AlignmentResult result = new AlignmentResult();
     }
 
     public SmithWaterman(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost, IAlignmentOutputFormatter outputFormatter) {
         super(mode, algorithm, distanceMatrix, gapCost, outputFormatter);
         assert gapCost instanceof ConstantGapCost : "Classic Needleman Wunsch can't use affine gap cost";
         assert algorithm == AlignmentAlgorithm.NEEDLEMAN_WUNSCH;
     }
 
     @Override
     public AlignmentResult align(Sequence seq1, Sequence seq2) {
         assert seq1 != null && seq2 != null;
         assert seq1.getSequence().length() > 0;
         assert seq2.getSequence().length() > 0;
         this.querySequence = seq1.getSequence();
         this.querySequenceId = seq1.getId();
         this.targetSequence = seq2.getSequence();
         this.targetSequenceId = seq2.getId();
         if (secStructAided) {
             if (querySequence.length() != querySequenceStruct.length()) {
                 throw new SSAADataInvalidException("Query sequence length does not match with query SS length, difference (" + querySequence.length() + " vs " + querySequenceStruct.length() + ")");
             } else if (targetSequence.length() != targetSequenceStruct.length()) {
                 throw new SSAADataInvalidException("Target sequence length does not match with target SS length, difference (" + querySequence.length() + " vs " + querySequenceStruct.length() + ")");
             }
         }
         initAndFillMatrix(seq1.getSequence(), seq2.getSequence());
         AlignmentResult result = new AlignmentResult();
         //Calculate the alignment and add it to the result
         result.setAlignments(Collections.singletonList(backtracking()));
         this.score = findMaxScore();
         result.setScore(this.score);
        result.setQuerySequenceId(querySequence);
        result.setTargetSequenceId(targetSequence);
         return result;
     }
 
     private double findMaxScore() {
         double max = 0.0;
         for (int x = 1; x <= xSize; x++) {
             for (int y = 1; y <= ySize; y++) {
                 max = Math.max(matrix[x][y], max);
             }
         }
         return max;
     }
 
     public double distanceScore(int x, int y) {
         double distance = distanceMatrix.distance(querySequence.charAt(x), targetSequence.charAt(y));
         //Set to 0 if not sec struct aided
         try {
             double secStructDistance = (secStructAided ? secStructMatrix[getSecStructIndex(querySequenceStruct.charAt(x))][getSecStructIndex(targetSequenceStruct.charAt(y))] : 0);
             return distance + secStructDistance;
         } catch (Exception ex) {
             return distance;
         }
     }
     
     public void initAndFillMatrix(String s, String t) {
         //////  init matrix:
         this.xSize = s.length();
         this.ySize = t.length();
         int xMatrixSize = this.xSize + 1;
         int ymatrixSize = this.ySize + 1;
         matrix = new double[xMatrixSize][ymatrixSize];
         leftArrows = new boolean[xMatrixSize][ymatrixSize];
         leftTopArrows = new boolean[xMatrixSize][ymatrixSize];
         topArrows = new boolean[xMatrixSize][ymatrixSize];
 
         leftTopPath = new boolean[xMatrixSize][ymatrixSize];
         leftPath = new boolean[xMatrixSize][ymatrixSize];
         topPath = new boolean[xMatrixSize][ymatrixSize];
         hasPath = new boolean[xMatrixSize][ymatrixSize];
         for (int x = 0; x < xSize; x++) {
             for (int y = 0; y < ySize; y++) {
                 leftArrows[x][y] = (y == 0);//first row --> true
                 leftTopArrows[x][y] = false;//true
                 topArrows[x][y] = (x == 0); //first column --> true
                 leftPath[x][y] = false;
                 leftTopPath[x][y] = false;
                 topPath[x][y] = false;
                 hasPath[x][y] = false;
             }
         }
         //Handle the topleft corner
         leftArrows[0][0] = false;
         leftTopArrows[0][0] = false;
         topArrows[0][0] = false;
         //Fill the matrix
         final double compareThreshold = 0.0000001;
         for (int x = 1; x < xMatrixSize; x++) {
             for (int y = 1; y < ymatrixSize; y++) {
                 double leftTopScore = matrix[x - 1][y - 1] + distanceScore(x - 1, y - 1);
                 double leftScore = matrix[x - 1][y] + gapCost.getGapCost(1);
                 double topScore = matrix[x][y - 1] + gapCost.getGapCost(1);
                 //Calculate the max score
                 double intermediateMaxScore = Math.max(leftTopScore, Math.max(leftScore, topScore)); //Basically NW
                 matrix[x][y] = Math.max(0.0, intermediateMaxScore);
                 //Check which 'arrows' are set for the current field
                 leftTopArrows[x][y] = Math.abs(leftTopScore - intermediateMaxScore) < compareThreshold;
                 leftArrows[x][y] = Math.abs(leftScore - intermediateMaxScore) < compareThreshold;
                 topArrows[x][y] = Math.abs(topScore - intermediateMaxScore) < compareThreshold;
                 //Assert this field has at least one arrow
                 //######### assert leftTopArrows[x][y] || leftArrows[x][y] || topArrows[x][y];//assert only the area where the l/a takes place
             }
         }
         //System.out.println("m[" + xMatrixSize + "][" + ymatrixSize + "] = " + matrix[xMatrixSize - 1][ymatrixSize - 1]);
     }
 
     public SequencePairAlignment backtracking() {
         ////    find the cell which contains the maximal entry
         double maxEntry = -1;
         int x = 0;
         int y = 0;
         for (int i = 0; i <= xSize; i++) {
             for (int j = 0; j <= ySize; j++) {
                 if (matrix[i][j] >= maxEntry) {
                     x = i;
                     y = j;
                     maxEntry = matrix[i][j];
                 }
             }
         }
         StringBuilder queryAlignment = new StringBuilder();
         StringBuilder targetAlignment = new StringBuilder();
         int yStart = ySize;
         int xStart = xSize;
         //From the corner, move up until y ==  y of the max coord
         while (yStart > y) {
             queryAlignment.append('-');
             targetAlignment.append(targetSequence.charAt(yStart - 1));
             yStart--;
         }
         //From the corner, move up until x == x of the max coord
         while (xStart > x) {
             targetAlignment.append('-');
             queryAlignment.append(querySequence.charAt(xStart - 1));
             xStart--;
         }
 //        System.out.println("X");
 //        System.out.println(queryAlignment);
 //        System.out.println(targetAlignment);
 //        System.out.println("X");
         while (x >= 0 && y >= 0) {
             if (leftTopArrows[x][y]) { //Match or mismatch
                 leftTopPath[x][y] = true;
                 hasPath[x][y] = true;
                 queryAlignment.append(querySequence.charAt(x - 1));
                 targetAlignment.append(targetSequence.charAt(y - 1));
                 x--;
                 y--;
             } else if (topArrows[x][y]) {//Insertion on query
                 topPath[x][y] = true;
                 hasPath[x][y] = true;
                 queryAlignment.append(querySequence.charAt(x - 1));
                 targetAlignment.append('-');
                 y--;
             } else if (leftArrows[x][y]) { //Deletion on query
                 leftPath[x][y] = true;
                 hasPath[x][y] = true;
                 queryAlignment.append('-');
                 targetAlignment.append(targetSequence.charAt(y - 1));
                 x--;
             }
             //Break if the current value is zero
             if (matrix[x][y] <= 0) {
                 break;
             }
         }
         //Move up, then left until corner has been reached
         while (y > 0) {
             queryAlignment.append('-');
             targetAlignment.append(targetSequence.charAt(y - 1));
             y--;
         }
         while (x > 0) {
             targetAlignment.append('-');
             queryAlignment.append(querySequence.charAt(x - 1));
             x--;
         }
         SequencePairAlignment spa = new SequencePairAlignment();
         spa.setQueryAlignment(queryAlignment.reverse().toString());
         spa.setTargetAlignment(targetAlignment.reverse().toString());
         return spa;
     }
 
     public String printMatrix() {
         StringBuilder builder = new StringBuilder();
         builder.append("\t\t");
         for (int x = 0; x < querySequence.length(); x++) {
             builder.append(querySequence.charAt(x)).append("\t");
         }
         builder.append("\n");
         for (int y = 0; y <= targetSequence.length(); y++) {
             builder.append(y == 0 ? ' ' : targetSequence.charAt(y - 1)).append("\t");
             for (int x = 0; x <= querySequence.length(); x++) {
                 builder.append(matrix[x][y]).append("\t");
             }
             builder.append("\n");
         }
         return builder.toString();
     }
 
     @Override
     public void writeMatrices(IDPMatrixExporter exporter) {
         DPMatrixExporter.DPMatrixInfo info = new DPMatrixExporter.DPMatrixInfo();
         //Set sequences
         info.query = querySequence;
         info.target = targetSequence;
         //Set IDs
         info.queryId = querySequenceId;
         info.targetId = targetSequenceId;
         info.matrix = matrix;
         info.xSize = xSize;
         info.ySize = ySize;
         info.matrixPostfix = "matrix";
         info.leftArrows = leftArrows;
         info.topArrows = topArrows;
         info.topLeftArrows = leftTopArrows;
         info.leftPath = leftPath;
         info.topPath = topPath;
         info.topLeftPath = leftTopPath;
         info.score = score;
         try {
             exporter.write(info);
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     public double[][] getMatrix() {
         return matrix;
     }
 }

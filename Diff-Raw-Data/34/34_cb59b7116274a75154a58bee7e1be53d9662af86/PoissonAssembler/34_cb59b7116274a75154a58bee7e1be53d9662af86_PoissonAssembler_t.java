 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.process.assembler;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class PoissonAssembler extends AbstractLagrangeAssembler {
 
     @Override
     public void assembleVolume() {
         for (int testPos = 0; testPos < nodesAssemblyIndes.size(); testPos++) {
            assembleVolumeVectorElem(testPos);
             for (int trialPos = 0; trialPos < nodesAssemblyIndes.size(); trialPos++) {
                assembleVolumeMatrixElem(testPos, trialPos);
             }
         }
     }
 
    private void assembleVolumeVectorElem(int testPos) {
         int testId = nodesAssemblyIndes.getQuick(testPos);
        if (load == null) {
            return;
        }
        for (int dmRow = 0; dmRow < dimension; dmRow++) {
            int row = testId * dimension + dmRow;
            mainVector.add(row, testShapeFunctionValues[0][testPos] * weight * load[dmRow]);
        }
    }

    private void assembleVolumeMatrixElem(int testPos, int trialPos) {

         int trialId = nodesAssemblyIndes.getQuick(trialPos);
        int testId = nodesAssemblyIndes.getQuick(testPos);
        for (int dmRow = 0; dmRow < dimension; dmRow++) {
            int row = testId * dimension + dmRow;
             double rowShpf = testShapeFunctionValues[dmRow + 1][testPos];
             double td = rowShpf * weight;
            for (int dmCol = 0; dmCol < dimension; dmCol++) {
                int col = trialId * dimension + dmCol;
                 if (upperSymmetric && col < row) {
                     continue;
                 }
                 double colShpf = trialShapeFunctionValues[dmCol + 1][trialPos];
                 mainMatrix.add(row, col, td * colShpf);
             }
         }
     }
 }

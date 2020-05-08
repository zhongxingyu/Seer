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
             for (int trialPos = 0; trialPos < nodesAssemblyIndes.size(); trialPos++) {
                 assembleVolumeElem(testPos, trialPos);
             }
         }
     }
 
     private void assembleVolumeElem(int testPos, int trialPos) {
         int testId = nodesAssemblyIndes.getQuick(testPos);
         int trialId = nodesAssemblyIndes.getQuick(trialPos);
         final int dim = dimension;
         for (int dmRow = 0; dmRow < dim; dmRow++) {
             int row = testId * dim + dmRow;
             double rowShpf = testShapeFunctionValues[dmRow + 1][testPos];
             double td = rowShpf * weight;
            mainVector.add(row, td * load[dmRow]);
             for (int dmCol = 0; dmCol < dim; dmCol++) {
                 int col = trialId * dim + dmCol;
                 if (upperSymmetric && col < row) {
                     continue;
                 }
                 double colShpf = trialShapeFunctionValues[dmCol + 1][trialPos];
                 mainMatrix.add(row, col, td * colShpf);
             }
         }
     }
 }

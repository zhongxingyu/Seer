 /*
  * Software License Agreement (BSD License)
  * 
  * Copyright (c) 2010, IIIA-CSIC, Artificial Intelligence Research Institute
  * All rights reserved.
  * 
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  * 
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  * 
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  * 
  *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute 
  *   nor the names of its contributors may be used to
  *   endorse or promote products derived from this
  *   software without specific prior written permission of
  *   IIIA-CSIC, Artificial Intelligence Research Institute
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package es.csic.iiia.dcop.figdl;
 
 import es.csic.iiia.dcop.CostFunction.Summarize;
 import es.csic.iiia.dcop.bb.UBGraph;
 import es.csic.iiia.dcop.bb.UBResults;
 import es.csic.iiia.dcop.igdl.IGdlMessage;
 import es.csic.iiia.dcop.up.UPEdge;
 import es.csic.iiia.dcop.up.UPGraph;
 import es.csic.iiia.dcop.up.UPResult;
 import es.csic.iiia.dcop.up.UPResults;
 import es.csic.iiia.dcop.util.FunctionCounter;
 import es.csic.iiia.dcop.vp.VPGraph;
 import es.csic.iiia.dcop.vp.VPResults;
 import es.csic.iiia.dcop.vp.strategy.OptimalStrategy;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Marc Pujol <mpujol at iiia.csic.es>
  */
 public class FIGdlGraph extends UPGraph<FIGdlNode,UPEdge<FIGdlNode, IGdlMessage>,UPResults> {
 
     private static Logger log = LoggerFactory.getLogger(UPGraph.class);
 
     private static int minR = 2;
 
     public static void setMinR(int min_r) {
         minR = min_r;
     }
 
     private FIGdlIteration iteration = new FIGdlIteration();
     private int maxR;
 
     @Override
     public void addNode(FIGdlNode clique) {
         super.addNode(clique);
         iteration.addNode(clique);
     }
 
     @Override
     public void addEdge(UPEdge<FIGdlNode, IGdlMessage> edge) {
         super.addEdge(edge);
         iteration.addEdge(edge);
     }
 
     @Override
     protected void initialize() {
         super.initialize();
     }
 
 
     @Override
     public UPResults run(int maxIterations) {
         reportStart();
 
         UPResults globalResults = (UPResults)getResults();
         double bestCost = Double.NaN, bestBound = Double.NaN;
 
         for (int i=minR; i<=maxR; i++) {
 
             // Value propagation
             iteration.setR(i);
             boolean exit = false;
 
            for (int j=0; j<i; j++) {
 
                 UPResults iterResults = iteration.run(maxIterations);
                 if (iterResults == null) {
                     // Early termination, use results from the previous iteration
                     exit = true;
                     break;
                 }
                 globalResults.addCycle(iterResults.getMaximalCcc(), iterResults.getTotalCcc());
                 for (Object result : iterResults.getResults()) {
                     globalResults.add((UPResult)result);
                 }
                 System.out.println("ITERBYTES " + iterResults.getSentBytes());
                 System.out.println("ITERSPARSITY " + FunctionCounter.getRatio());
 
                 Summarize summarize = null;
                 for(FIGdlNode n : getNodes()) {
                     if (n.getRelations().size() > 0) {
                         summarize = n.getRelations().get(0).getFactory().getSummarizeOperation();
                        exit = true;
                         break;
                     }
                 }
 
 
                 // Solution extraction
                 VPGraph st = new VPGraph(this, new OptimalStrategy());
                 VPResults res = st.run(1000);
 
                 // Bound calculation
                 UBGraph ub = new UBGraph(st);
                 UBResults ubres = ub.run(1000);
                 System.out.println("THIS_ITER_LB " + ubres.getBound());
                 System.out.println("THIS_ITER_UB " + ubres.getCost());
 
     /*            if (Double.isInfinite(ubres.getCost()) || Double.isInfinite(ubres.getBound())) {
                     fallbackLastIteration();
                     break;
                 }
      *
      */
 
                 final double newCost = ubres.getCost();
                 if (Double.isNaN(bestCost) || summarize.isBetter(newCost, bestCost)) {
                     bestCost = newCost;
                 }
                 final double newBound = ubres.getBound();
                 if (Double.isNaN(bestBound) || !summarize.isBetter(newBound, bestBound)) {
                     bestBound = newBound;
                 }
                 System.out.println("ITER_LB " + bestBound);
                 System.out.println("ITER_UB " + bestCost);
 
                 if (Math.abs(newCost - bestBound) < 0.0005) {
                     exit = true;
                     break;
                 }
 
                 // Build the new iteration
                 iteration.prepareNextIteration(bestCost);
             }
 
             if (exit) break;
         }
 
         return globalResults;
     }
 
     public void setMaxR(int maxR) {
         this.maxR = maxR;
     }
 
     @Override
     protected UPResults buildResults() {
         return new UPResults();
     }
 
 }

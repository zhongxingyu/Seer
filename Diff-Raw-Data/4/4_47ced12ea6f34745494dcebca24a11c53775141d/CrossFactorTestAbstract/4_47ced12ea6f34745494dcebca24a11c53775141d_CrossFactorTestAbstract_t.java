 /*
  * Software License Agreement (BSD License)
  *
  * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
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
 package es.csic.iiia.maxsum.factors;
 
 import es.csic.iiia.maxsum.CommunicationAdapter;
 import es.csic.iiia.maxsum.Factor;
 import es.csic.iiia.maxsum.MaxOperator;
 import es.csic.iiia.maxsum.Maximize;
 import es.csic.iiia.maxsum.Minimize;
 import java.util.List;
 import java.util.Random;
 import static org.mockito.Mockito.*;
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 
 /**
  * Skeletal implementation of a test class that checks a THOP's computed messages against the
  * messages computed in the standard manner.
  *
  * @see StandardFactor
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public abstract class CrossFactorTestAbstract {
     private final double DELTA = 0.0001d;
     private final int NUMBER_OF_RUNS = 100;
     private Random generator = new Random();
 
     /**
      * Builds the factors to cross-test.
      * <p/>
      * The output must be an array of exactly two factors: (1) the specific HOP to test; and
      * (2) a {@link StandardFactor} whose potential represents the same function.
      * <p/>
      * You <strong>must</strong> initialize the factors with the given maximization operator, and
      * {@link #link(es.csic.iiia.maxsum.Factor, es.csic.iiia.maxsum.Factor[])} them to all the
      * given neighbors.
      *
      * @param op Maximization operator to use
      * @param neighbors List of neighbors of the new factor
      * @return pair of factors to cross-test
      */
     public abstract Factor[] buildFactors(MaxOperator op, Factor[] neighbors);
 
     /**
      * Adds the list of factors as neighbors of the given one.
      *
      * @param f Factor to which others will be added as neighbors
      * @param neighbors List of neighboring factors to add
      */
     protected void link(Factor<Factor> f, Factor[] neighbors) {
         for (Factor n : neighbors) {
             f.addNeighbor(n);
         }
     }
 
     /**
      * Generates and returns a random value in [-1, 1) the range.
      *
      * @return random cost/utility
      */
     protected double getRandomValue() {
         return generator.nextDouble()*2 - 1;
     }
 
     @Test
     public void crossTest() {
         for (int i=0; i<NUMBER_OF_RUNS; i++) {
             int len = generator.nextInt(10) + 1;
             double[] values = new double[len];
             for (int j=0; j<len; j++) {
                 values[j] = generator.nextDouble();
             }

             runAgainstGeneric(new Maximize(), values);
             runAgainstGeneric(new Minimize(), values);
         }
     }
 
     /**
      * Checks the outgoing max-sum's messages of a specific factor type against those computed in
      * the standard manner.
      *
      * @param op Maximization operator to use
      * @param inMessages list of incoming messages (one per neighbor)
      */
     public void runAgainstGeneric(MaxOperator op, double[] inMessages) {
         final int nNeighbors = inMessages.length;
         final CommunicationAdapter<Factor> comSpecific = mock(CommunicationAdapter.class);
         final CommunicationAdapter<Factor> comGeneric = mock(CommunicationAdapter.class);
 
         // Create the neighbors
         Factor[] neighbors = new Factor[nNeighbors];
         for (int i=0; i<nNeighbors; i++) {
             neighbors[i] = mock(Factor.class);
         }
 
         // Create the factors to test (one of the specific type being tested, and a standard one)
         Factor[] factors = buildFactors(op, neighbors);
         Factor<Factor> testedSpecific = factors[0];
         Factor<Factor> testedGeneric = factors[1];
         testedSpecific.setCommunicationAdapter(comSpecific);
         testedSpecific.setIdentity(testedSpecific);
         testedGeneric.setCommunicationAdapter(comGeneric);
         testedGeneric.setIdentity(testedGeneric);
 
         // Input incoming messages
         for (int i=0; i<nNeighbors; i++) {
             testedSpecific.receive(inMessages[i], neighbors[i]);
             testedGeneric.receive(inMessages[i], neighbors[i]);
         }
 
         // Gather outgoing messages
         ArgumentCaptor<Double> specificMessagesCaptor = ArgumentCaptor.forClass(Double.class);
         ArgumentCaptor<Double> genericMessagesCaptor = ArgumentCaptor.forClass(Double.class);
 
         // This makes the factor run and send messages through the mocked com, capturing their
         // outputs.
         testedSpecific.run();
         testedGeneric.run();
         for (int i=0; i<neighbors.length; i++) {
             verify(comSpecific).send(specificMessagesCaptor.capture(), same(testedSpecific), same(neighbors[i]));
             verify(comGeneric).send(genericMessagesCaptor.capture(), same(testedGeneric), same(neighbors[i]));
         }
 
         // Finally verify that the messages match
         List<Double> specificMessages = specificMessagesCaptor.getAllValues();
         List<Double> genericMessages = genericMessagesCaptor.getAllValues();
         for (int i=0; i<neighbors.length; i++) {
            assertEquals(genericMessages.get(i), specificMessages.get(i), DELTA);
         }
 
     }
 
 }

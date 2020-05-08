 /*  Copyright (c) 2012 Tommaso Urli, Markus Wagner
  * 
  *  Tommaso Urli    tommaso.urli@uniud.it   University of Udine
  *  Markus Wagner   wagner@acrocon.com      University of Adelaide
  *
  *  Permission is hereby granted, free of charge, to any person obtaining
  *  a copy of this software and associated documentation files (the
  *  "Software"), to deal in the Software without restriction, including
  *  without limitation the rights to use, copy, modify, merge, publish,
  *  distribute, sublicense, and/or sell copies of the Software, and to
  *  permit persons to whom the Software is furnished to do so, subject to
  *  the following conditions:
  *
  *  The above copyright notice and this permission notice shall be
  *  included in all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 
 package gpframework.algorithms.components;
 
 import cern.jet.random.Poisson;
 import cern.jet.random.engine.DRand;
 import gpframework.common.Utils;
 import gpframework.problems.Problem;
 import java.util.List;
 
 /**
  * Mutation factory which generates a list of 1+Poisson(1) Mutations (with equal
  * probability of selecting a Deletion, an Insertion or a Replacement) to be 
  * applied in a row.
  */
 public class PoissonMutationFactory extends SingleMutationFactory {
 
     /**
      * Poisson distribution.
      */
     protected Poisson poisson;
     
     /**
      * Constructor.
      */
     public PoissonMutationFactory()
     {
         poisson = new Poisson(1, new DRand(Utils.random.nextInt()));
     }
     
     @Override
     public List<Mutation> generate(Problem problem) {
         
         // Generate one mutation according to super class
         List<Mutation> mutations = super.generate(problem);
        int repetitions = poisson.nextInt();
         
         for (int i = 0; i < repetitions; i++)
            mutations.addAll(super.generate(problem));
         
         return mutations;        
     }
 }

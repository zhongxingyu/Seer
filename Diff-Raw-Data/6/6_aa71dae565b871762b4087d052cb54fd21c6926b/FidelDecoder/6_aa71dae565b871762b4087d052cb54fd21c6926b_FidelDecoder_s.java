 /**
  * *******************************************************************************
  * Copyright (c) 2011, Monnet Project All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. * Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. * Neither the name of the Monnet Project nor the names
  * of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *******************************************************************************
  */
 package eu.monnetproject.translation.fidel;
 
 import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
 import it.unimi.dsi.fastutil.objects.Object2IntMap;
 import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Random;
 
 /**
  *
  * @author John McCrae
  */
 public class FidelDecoder {
 
     public static final int UNK = 0;
     public static final int DIST = 1;
     public static final int LM = 2;
     public static final int PT = 3;
     private static final Random r = new Random();
     public static Int2ObjectMap<String> wordMap;
     public static Object2IntMap<String> srcWordMap;
     private static final boolean verbose = Boolean.parseBoolean(System.getProperty("fidel.verbose", "false"));
 //    private static final PrintWriter log;
 
 //    static {
 //        PrintWriter log2 = null;
 //        try {
 //            log2 = new PrintWriter("decode-stats.csv");
 //        } catch (IOException x) {
 //        }
 //        log = log2;
 //        log.println("ITERS,SOLN_ITER,N,SCORE");
 //    }
 
     public static void printPhrase(int[] p) {
         for (int i = 0; i < p.length; i++) {
             if (wordMap != null) {
                 System.err.print(wordMap.get(p[i]) + " ");
             } else {
                 System.err.print(i + " ");
             }
         }
     }
 
     /**
      *
      * @param src The source phrase
      * @param phraseTable The phrase table. Scores should already be logarithmic
      * @param languageModel The language model. Scores should already be
      * logarithmic
      * @param weights Of the form { UNK, DIST, LM, PT1, ... PTN }
      * @param distortionLimit The maximum distortion to consider
      * @param nBest The nBest results to return
      * @param beamSize The size of beam to use (20 for quick, 50 for slow)
      * @param useLazy Evaluate distortion lazily (much quicker, with slightly worse performance)
      * @return
      */
     public static Solution[] decode(int[] src,
             Object2ObjectMap<Phrase, Collection<PhraseTranslation>> phraseTable,
             IntegerLanguageModel languageModel,
             int lmN,
             double[] weights,
             int distortionLimit,
             int nBest,
             int beamSize,
             boolean useLazy) {
         final double[] scorePartial = calcPartialScore(src, phraseTable, weights, languageModel, lmN);
 
         final Beam beam = new Beam(beamSize);
         final Beam solns = new Beam(nBest);
         // Add null solution
         beam.add(new SolutionImpl(0, new int[0], new int[0], sum(scorePartial), sum(scorePartial)));
 
         // Potential code bug here if the maximum translation length is greater
         // than 32 times large than the source
         final int[] buf = new int[src.length * 32];
         final BufferCache bufferCache = new BufferCache(beamSize+5, src.length * 32);
 
         int iterationNo = 0;
         int solnFound = 0;
 
         while (!beam.isEmpty() && beam.bestScore() > solns.leastScore()) {
             // Take the best solution
             final Solution solnTmp = beam.poll();
             final SolutionImpl soln;
             if (solnTmp instanceof SolutionImpl) {
                 soln = (SolutionImpl) solnTmp;
             } else if (solnTmp instanceof LazyDistortedSolution) {
                 soln = ((LazyDistortedSolution) solnTmp).evaluate(weights, languageModel, lmN);
             } else {
                 throw new RuntimeException("Unreachable");
             }
             iterationNo++;
             if (verbose) {
                 System.err.print("Selecting ");
                 soln.printSoln(wordMap);
             }
             // If it is a complete solution we add it to the solution beam
             if (soln.upto == src.length) {
                 solns.add(soln);
                 if (soln == solns.first()) {
                     solnFound = iterationNo;
                 }
                 continue;
             }
             // i is the end of the curent solution (in the source)
             final int i = soln.upto;
             // pos is the end of the current solution (in the target)
             final int pos = soln.soln.length;
 
             // init buf
             System.arraycopy(soln.soln, 0, buf, 0, soln.soln.length);
 
             // j indicates the end of the phrase (in src)
             PHRASE_END:
             for (int j = i + 1; j <= src.length; j++) {
                 final double futureCost = sum(scorePartial, j);
                 if (Double.isNaN(futureCost) || Double.isInfinite(futureCost)) {
                     throw new RuntimeException("Infinite future cost");
                 }
                 final Collection<PhraseTranslation> candidates = phraseTable.get(new Phrase(Arrays.copyOfRange(src, i, j)));
                 // No candidate
                 if (candidates == null || candidates.isEmpty()) {
                     if (j == i + 1) {
                         // d is distance back from beginning we wish to insert the candidate at
                         DISTORTION:
                         for (int d = 0; d < distortionLimit && d <= pos; d++) {
                             // Check this will not push an existing element over distortion limit
                             for (int e = 1; e <= d; e++) {
                                 if (soln.dist[pos - e] + d > distortionLimit) {
                                     break DISTORTION;
                                 }
                             }
                             // Get the score of the solution
                             final double score = weights[UNK]
                                     + soln.score
                                     + futureCost
                                     - soln.futureCost
                                     + deltaDist(soln.dist, 1, d, weights);
 
                             if (Double.isNaN(score)) {
                                 throw new RuntimeException();
                             }
 
                             rightShiftBuffer(buf, d, pos - d);
                             buf[pos - d] = -src[i];
 
 
                             if (!Double.isInfinite(score)
                                     && (score > solns.leastScore() || !solns.isFull())
                                     && (score > beam.leastScore() || !beam.isFull())) {
                                 final Solution newSoln = new SolutionImpl(j, Arrays.copyOfRange(buf, 0, pos + 1), recalcDist(soln.dist, 1, d), score, futureCost);
                                 beam.add(newSoln);
                                 if (verbose) {
                                     System.err.print("Adding ");
                                     newSoln.printSoln(wordMap);
                                 }
                             }
                             leftShiftBuffer(buf, d, pos);
                         }
                     } else {
                         // We don't search for longer phrases
                         break PHRASE_END;
                     }
                 } else {
                     for (PhraseTranslation candidate : candidates) {
                         // d is distance back from beginning we wish to insert the candidate at
                         DISTORTION:
                         for (int d = 0; d < distortionLimit && d <= pos; d++) {
                             // Check this will not push an existing element over distortion limit
                             for (int e = 1; e <= d; e++) {
                                 if (soln.dist[pos - e] + d > distortionLimit) {
                                     break DISTORTION;
                                 }
                             }
                             final double ddScore = deltaDist(soln.dist, candidate.words.length, d, weights);
                             if (useLazy) {
 
                                 double ptScore2 = 0.0;
                                 for (int k = 0; k < candidate.scores.length; k++) {
                                     ptScore2 += weights[PT + k] * candidate.scores[k];
                                 }
                                 double score = ptScore2
                                         + soln.score
                                         + futureCost
                                         - soln.futureCost
                                         + ddScore;
 
                                 if (!Double.isInfinite(score) && (!beam.isFull() || score > beam.leastScore())) {
                                     final LazyDistortedSolution lds = new LazyDistortedSolution(candidate, soln, buf, pos, d, j, futureCost, ddScore, ptScore2, weights, bufferCache);
                                     if(beam.add(lds)) {
                                         beam.addRemovalListener(lds);
                                     }
                                 }
                             } else {
                                 final double tptScore = tryPutTranslation(candidate, weights, buf, pos, languageModel, lmN, d);
                                 // Get the score of the solution
                                 final double score = tptScore
                                         + soln.score
                                         + futureCost
                                         - soln.futureCost
                                         + ddScore;
 
                                 if (Double.isNaN(score)) {
                                     //System.err.println(ddScore);
                                     //System.err.println(soln.score);
                                     //System.err.println(futureCost);
                                     //System.err.println(soln.futureCost);
                                     //System.err.println(tptScore);
                                     //throw new RuntimeException();
                                     continue;
                                 }
 
                                 if (!Double.isInfinite(score)
                                         && (score > solns.leastScore() || !solns.isFull())
                                         && (score > beam.leastScore() || !beam.isFull())) {
                                     final Solution newSoln = new SolutionImpl(j, Arrays.copyOfRange(buf, 0, pos + candidate.words.length), recalcDist(soln.dist, candidate.words.length, d), score, futureCost);
                                     // System.err.println(newSoln.toString());
                                     beam.add(newSoln);
                                     if (verbose) {
                                         System.err.print("Adding ");
                                         newSoln.printSoln(wordMap);
                                     }
                                 } else if (verbose) {
                                     System.err.print("Rejecting ");
                                     new SolutionImpl(j, Arrays.copyOfRange(buf, 0, pos + candidate.words.length), recalcDist(soln.dist, candidate.words.length, d), score, futureCost).printSoln(wordMap);
 
                                 }
                                 // Undo damage by tryPutTranslation
                                 leftShiftBuffer(buf, candidate.words.length, pos - d);
                             }
                         }
                     }
                 }
             }
         }
 //        log.println(iterationNo + "," + solnFound + "," + src.length + "," + solns.bestScore());
 //        log.flush();
         return solns.toArray();
     }
 
     /**
      * Calculate the LM score
      *
      * @param buf The current translation
      * @param p The position (end of n-gram)
      * @param languageModel The model
      * @param lmN The length of the model
      * @param unk The cost of failing to find a translation
      * @return log(p(w_p|w_{p-1}...w_{p-lmN}))
      */
     public static double lmScore(int[] buf, int p,
             IntegerLanguageModel languageModel,
             int lmN, double unk) {
         final Phrase ph = p >= lmN
                 ? new Phrase(buf, p - lmN, lmN)
                 : new Phrase(buf, 0, p);
         /*final double[] lmScore = languageModel.get(ph);
          if (lmScore != null) {
          if(Double.isInfinite(lmScore[0])) {
          return unk;
          } else {
          return lmScore[0];
          }
          } else if (lmN > 1) {
          final Phrase boPh = new Phrase(buf, p - lmN, lmN - 1);
          final double[] boScore = languageModel.get(boPh);
          if (boScore != null && boScore.length > 0) {
          final double scoreWithBO = boScore[1] + lmScore(buf, p, languageModel, lmN - 1, unk);
          return scoreWithBO;
          } else {
          return lmScore(buf, p, languageModel, lmN - 1, unk);
          }
          } else {
          return unk;
          }*/
         final double[] lmScore = languageModel.get(ph);
         if (lmScore != null && !Double.isInfinite(lmScore[0])) {
             return lmScore[0];
         } else {
             return -100;
         }
     }
 
     /**
      * Attempt to append a translation to the end of the array
      *
      * @param pt The phrase table
      * @param weights The weights
      * @param baselineBuffer The baseline buffer
      * @param pos The position of the end of buffer
      * @param languageModel The language model
      * @param lmN The n in the language model
      * @return The cost to add
      */
     public static double tryPutTranslation(PhraseTranslation pt, double[] weights,
             final int[] baselineBuffer, int pos, IntegerLanguageModel languageModel, int lmN) {
         double score = 0.0;
         for (int j = 0; j < pt.scores.length; j++) {
             score += weights[PT + j] * pt.scores[j];
         }
         for (int w : pt.words) {
            baselineBuffer[pos++] = w;
            score += weights[LM] * lmScore(baselineBuffer, pos, languageModel, lmN, weights[UNK]);
         }
         assert (!Double.isInfinite(score) && !Double.isNaN(score));
         return score;
     }
 
     /**
      * Move values from the end of the buffer right
      *
      * @param buf The buffer
      * @param shift The amount to shift
      * @param at The place to start shifting
      */
     public static void rightShiftBuffer(int[] buf, int shift, int at) {
         for (int i = buf.length - 1; i > at && i - shift >= 0; i--) {
             buf[i] = buf[i - shift];
         }
     }
 
     /**
      * Extends the distortion array by adding a new element at
      * {@code dist.length - shift} of {@code length}
      *
      * @param dist The current distortion array
      * @param length The length of the inserted element
      * @param shift The shift at the end of the distortion array to accommodate
      * the new element
      * @return The new distortion array
      */
     public static int[] recalcDist(int[] dist, int length, int shift) {
         final int[] newDist = new int[dist.length + length];
         System.arraycopy(dist, 0, newDist, 0, dist.length - shift);
         for (int i = dist.length - shift; i < dist.length - shift + length; i++) {
             newDist[i] = -shift;
         }
         for (int i = dist.length - shift + length; i < dist.length + length; i++) {
             newDist[i] = dist[i - length] + length;
         }
         return newDist;
     }
 
     /**
      * Calculate the change in distortion by some modification
      *
      * @param dist The current distortion
      * @param length The length to distort
      * @param shift The amount to shift
      * @param weights The weights on the model
      * @return The change in cost
      */
     public static double deltaDist(int[] dist, int length, int shift, double[] weights) {
         return -2.0 * weights[DIST] * length * shift;
     }
 
     /**
      * Shift the end of the buffer back (do not replace now empty values)
      *
      * @param buf The buffer
      * @param shift The amount to shift
      * @param at The place to start shifting
      */
     public static void leftShiftBuffer(int[] buf, int shift, int at) {
         for (int i = at; i < buf.length - shift; i++) {
             buf[i] = buf[i + shift];
         }
     }
 
     /**
      * Attempt to put a translation, potentially offset by some amount
      *
      * @param pt The phrase table
      * @param weights The weights on the model
      * @param buf The buffer
      * @param pos The 'end' of the buffer
      * @param languageModel The language model
      * @param lmN The n in the language model
      * @param dist The amount to shift (positive)
      * @return The cost to do this
      */
     public static double tryPutTranslation(PhraseTranslation pt, double[] weights,
             final int[] buf, int pos, IntegerLanguageModel languageModel, int lmN, int dist) {
         double score = 0.0;
         for (int j = 0; j < pt.scores.length; j++) {
             score += weights[PT + j] * pt.scores[j];
         }
         // remove the "lost n-grams"
         for (int i = 0; i < Math.min(lmN, dist); i++) {
             score -= weights[LM] * lmScore(buf, pos - i, languageModel, lmN, weights[UNK]);
         }
         // shift the n-grams
         rightShiftBuffer(buf, pt.words.length, pos - dist);
 
         //for (int w : pt.p) {
         for (int i = 0; i < pt.words.length; i++) {
             buf[pos - dist + i] = pt.words[i];
             score += weights[LM] * lmScore(buf, pos + i + 1, languageModel, lmN, weights[UNK]);
         }
         for (int i = 0; i < Math.min(lmN, dist); i++) {
             score += weights[LM] * lmScore(buf, pos - i, languageModel, lmN, weights[UNK]);
         }
         // Change should be undone:
         //leftShiftBuffer(buf, pt.w.length, pos - dist);
         //assert (!Double.isInfinite(score) && !Double.isNaN(score));
         return Double.isNaN(score) ? Double.NEGATIVE_INFINITY : score;
     }
 
     private static double sum(double[] ds) {
         double n = 0.0;
         for (double d : ds) {
             n += d;
         }
         return n;
     }
 
     private static double sum(double[] ds, int off) {
         double n = 0.0;
         for (int i = off; i < ds.length; i++) {
             n += ds[i];
         }
         return n;
     }
 
     /**
      * Calculate the naive scores at each point (for future cost estimation)
      *
      * @param src The source phrase
      * @param phraseTable The phrase table
      * @param weights The weights on the model
      * @param languageModel The language model
      * @param lmN The language model's n
      * @return The array of partial scores
      */
     public static double[] calcPartialScore(int[] src, Object2ObjectMap<Phrase, Collection<PhraseTranslation>> phraseTable, double[] weights, IntegerLanguageModel languageModel, int lmN) {
         final double[] scorePartial = new double[src.length];
         final int[] baselineBuffer = new int[src.length * 8];
         int pos = 0;
         for (int i = 0; i < src.length; i++) {
             final Collection<PhraseTranslation> singleTranslations = phraseTable.get(new Phrase(new int[]{src[i]}));
             if (singleTranslations == null || singleTranslations.isEmpty()) {
                 scorePartial[i] = weights[UNK];
                 baselineBuffer[pos++] = src[i];
                 scorePartial[i] += weights[LM] * lmScore(baselineBuffer, pos, languageModel, lmN, weights[UNK]);
             } else {
                 double bestScore = -Double.MAX_VALUE;
                 int ties = 0;
                 for (PhraseTranslation pt : singleTranslations) {
                     double score = tryPutTranslation(pt, weights, baselineBuffer, pos, languageModel, lmN);
                     if ((score > bestScore && (ties = 0) == 0)
                             || (score == bestScore && r.nextInt() % ++ties == 0)) {
                         bestScore = score;
                     }
                 }
                 scorePartial[i] = bestScore;
             }
         }
         return scorePartial;
     }
 }

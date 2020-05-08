 /**
  * ********************************************************************************
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
 package eu.monnetproject.bliss.experiments;
 
 import eu.monnetproject.math.sparse.SparseIntArray;
 import eu.monnetproject.math.sparse.Vector;
 import eu.monnetproject.bliss.CLIOpts;
 import eu.monnetproject.bliss.NGram;
 import eu.monnetproject.bliss.NGramSimilarityMetric;
 import eu.monnetproject.bliss.ParallelBinarizedReader;
 import eu.monnetproject.bliss.SimilarityMetric;
 import eu.monnetproject.bliss.SimilarityMetricFactory;
 import eu.monnetproject.bliss.WordMap;
 import eu.monnetproject.bliss.experiments.DiskBackedStream.Builder;
 import it.unimi.dsi.fastutil.objects.Object2IntMap;
 import java.io.File;
 import java.io.InputStream;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 /**
  *
  * @author John McCrae
  */
 public class MateFindingTrial {
 
     static final HashMap<String, String> metricNames = new HashMap<String, String>() {
         {
             put("CLESA", "eu.monnetproject.bliss.clesa.CLESAFactory");
             put("NG-SAL", "eu.monnetproject.bliss.clesa.NGramSalienceFactory");
             put("LDA", "eu.monnetproject.bliss.lda.LDASimilarityMetricFactory");
             put("KCCA", "eu.monnetproject.bliss.kcca.KCCASimilarityMetricFactory");
             put("BetaLM", "eu.monnetproject.bliss.sim.BetaLMSimilarityFactory");
             put("LSA", "eu.monnetproject.bliss.lsa.LSASimilarityMetricFactory");
             put("SAL", "eu.monnetproject.bliss.betalm.impl.SalienceSimilarityFactory");
             put("W2W", "eu.monnetproject.bliss.experiments.Word2WordTranslation");
         }
     };
     private static final Random random = new Random();
     public static final int STREAM_CHUNK = Integer.parseInt(System.getProperty("streamChunk", "510"));
 
     @SuppressWarnings("unchecked")
     public static double[] compare(File trainFile, Class<SimilarityMetricFactory> factoryClazz, int W, File testFile, int ngram, boolean inverseDirection) throws Exception {
         
         final SimilarityMetricFactory smf = factoryClazz.newInstance();
         SimilarityMetric metric = null;
         NGramSimilarityMetric ngMetric = null;
         boolean metricInverted;
         if (smf.datatype().equals(ParallelBinarizedReader.class)) {
             final ParallelBinarizedReader trainPBR = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(trainFile),inverseDirection);
             metricInverted = inverseDirection;
             if (ngram > 0) {
                 ngMetric = ((SimilarityMetricFactory<ParallelBinarizedReader>) smf).makeNGramMetric(trainPBR, W, ngram);
             } else {
                 metric = ((SimilarityMetricFactory<ParallelBinarizedReader>) smf).makeMetric(trainPBR, W);
             }
         } else if (InputStream.class.isAssignableFrom(smf.datatype())) {
             final InputStream train = CLIOpts.openInputAsMaybeZipped(trainFile);
             if (ngram > 0) {
                 ngMetric = ((SimilarityMetricFactory<InputStream>) smf).makeNGramMetric(train, W, ngram);
             } else {
                 metric = ((SimilarityMetricFactory<InputStream>) smf).makeMetric(train, W);
             }
             metricInverted = false;
         } else if (File.class.isAssignableFrom(smf.datatype())) {
             if (ngram > 0) {
                 ngMetric = ((SimilarityMetricFactory<File>) smf).makeNGramMetric(trainFile, W, ngram);
             } else {
                 metric = ((SimilarityMetricFactory<File>) smf).makeMetric(trainFile, W);
             }
             metricInverted = false;
         } else if (Object.class.equals(smf.datatype())) {
             if (ngram > 0) {
                 ngMetric = ((SimilarityMetricFactory<Object>) smf).makeNGramMetric(null, W, ngram);
             } else {
                 metric = ((SimilarityMetricFactory<Object>) smf).makeMetric(null, W);
             }
             metricInverted = false;
         } else {
             throw new IllegalArgumentException();
         }
 
         if (ngram > 0) {
             return compareNG(ngMetric, W, testFile, ngram, inverseDirection,metricInverted);
         } else {
             return compare(metric, W, testFile, inverseDirection,metricInverted);
         }
     }
 
     public static double[] compare(SimilarityMetric parallelSimilarity, int W, File testFile, boolean inverse, boolean metricInverted) throws Exception {
         final ParallelBinarizedReader testPBR = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(testFile),inverse && !metricInverted);
         System.err.println("Reading test data");
         //final List<SparseIntArray[]> docs = new ArrayList<SparseIntArray[]>();
         int docSize = 0;
         System.err.println("Preparing data ");
         int idx = 0;
         //double[][] predicted = new double[docs.size()][];
         //double[][] foreign = new double[docs.size()][];
         SparseIntArray[] s;
         
         final DiskBackedStream predicted = new DiskBackedStream(STREAM_CHUNK, parallelSimilarity.K(),parallelSimilarity.K() >= parallelSimilarity.W());
         final Builder predictedBuilder = predicted.builder();
         final DiskBackedStream foreign = new DiskBackedStream(STREAM_CHUNK, parallelSimilarity.K(),parallelSimilarity.K() >= parallelSimilarity.W());
         final Builder foreignBuilder = foreign.builder();
             while ((s = testPBR.nextFreqPair(W)) != null) {
                 SparseIntArray[] doc = s;
                 //predicted[idx] = parallelSimilarity.simVecSource(doc[0]).toDoubleArray();
                 if(inverse && !metricInverted) {
                    foreignBuilder.add(parallelSimilarity.simVecSource(doc[metricInverted ?  1 : 0]).toDoubleArray());
                 } else {
                     predictedBuilder.add(parallelSimilarity.simVecSource(doc[metricInverted ?  1 : 0]).toDoubleArray());
                 }
                 //foreign[idx++] = parallelSimilarity.simVecTarget(doc[1]).toDoubleArray();
                 if(inverse && !metricInverted) {
                    predictedBuilder.add(parallelSimilarity.simVecTarget(doc[metricInverted ?  0 : 1]).toDoubleArray());
                 } else {
                     foreignBuilder.add(parallelSimilarity.simVecTarget(doc[metricInverted ?  0 : 1]).toDoubleArray());
                 }
                 idx++;
                 //   System.out.println(Arrays.toString(predicted[idx-1]));
                 //   System.out.println(Arrays.toString(foreign[idx-1]));
                 if (idx % 10 == 0) {
                     System.err.print(".");
                 }
                 docSize++;
             }
         predictedBuilder.finish();
         foreignBuilder.finish();
         System.err.println();
         System.err.println("Starting classification");
         int correct = 0;
         int incorrect = 0;
         int ties = 0;
         int correct5 = 0;
         int correct10 = 0;
         double mrr = 0;
         int i = 0;
         for (double[] pred : predicted) {
             if (allZero(pred)) {
                 incorrect++;
                 mrr += 2.0 / docSize;
                 System.out.print("\u00d8");
                 continue;
             }
             double rightScore;
             double[] foreign_i = foreign.get(i);
             rightScore = cosSim(pred, foreign_i);
             int bestJ = -1;
             int rank = 1;
             double bestMatch = -Double.MAX_VALUE;
             int j = 0;
             for (double[] forin : foreign) {
                 final double cosSim = cosSim(pred, forin);
                 if (Double.isNaN(cosSim) || Double.isInfinite(cosSim)) {
                     System.err.print("N");
                     continue;
                 }
                 if ((cosSim > bestMatch && (ties = 0) == 0)
                         || (cosSim == bestMatch && random.nextInt(++ties) == 0)) {
                     bestMatch = cosSim;
                     bestJ = j;
                 }
                 if (cosSim > rightScore) {
                     rank++;
                 }
                 j++;
                 //System.out.println(i+","+j+","+cosSim);
             }
             if (i == bestJ) {
                 correct++;
                 System.out.print("+");
             } else {
                 incorrect++;
                 if (rank < 10) {
                     System.out.print(rank);
                     if (rightScore == 0.0) {
                         System.out.print("!");
                     }
                 } else {
                     System.out.print("-");
                 }
             }
             if (rank <= 5) {
                 correct5++;
             }
             if (rank <= 10) {
                 correct10++;
             }
             mrr += 1.0 / rank;
             i++;
         }
         final DecimalFormat percentFormat = new DecimalFormat("##.##%");
         System.out.println();
         System.out.println("Precision@1: " + correct + " (" + percentFormat.format((double) correct / (double) docSize) + ")");
         System.out.println("Precision@5: " + correct5 + " (" + percentFormat.format((double) correct5 / (double) docSize) + ")");
         System.out.println("Precision@10: " + correct10 + " (" + percentFormat.format((double) correct10 / (double) docSize) + ")");
         System.out.println("MRR: " + (mrr / docSize));
         return new double[] { (double)correct / docSize,
             (double)correct5 / docSize,
             (double)correct10 / docSize,
             mrr / docSize
         };
     }
 
     public static double[] compareNG(NGramSimilarityMetric parallelSimilarity, int W, File testFile, int N, boolean inverse, boolean metricInverted) throws Exception {
         System.err.println("Reading test data");
         ParallelBinarizedReader testPBR = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(testFile), inverse && !metricInverted);
         //final List<Object2IntMap<NGram>[]> docs = new ArrayList<Object2IntMap<NGram>[]>();
         Object2IntMap<NGram>[] s;
         //while ((s = testFile.nextNGramPair(W)) != null) {
         //    docs.add(s);
         //}
         System.err.println("Preparing data");
         int idx = 0;
         //double[][] predicted = new double[docs.size()][];
         //double[][] foreign = new double[docs.size()][];
 
         final DiskBackedStream predicted = new DiskBackedStream(STREAM_CHUNK, parallelSimilarity.K(),parallelSimilarity.K() >= parallelSimilarity.W());
         final Builder predictedBuilder = predicted.builder();
         final DiskBackedStream foreign = new DiskBackedStream(STREAM_CHUNK, parallelSimilarity.K(),parallelSimilarity.K() >= parallelSimilarity.W());
         final Builder foreignBuilder = foreign.builder();
         int docsSize = 0;
         while ((s = testPBR.nextNGramPair(N)) != null) {
             Object2IntMap<NGram>[] doc = s;
             //predicted[idx] = parallelSimilarity.simVecSource(doc[0]).toDoubleArray();
             predictedBuilder.add(parallelSimilarity.simVecSource(doc[metricInverted ? 1 : 0]).toDoubleArray());
             //foreign[idx++] = parallelSimilarity.simVecTarget(doc[1]).toDoubleArray();
             foreignBuilder.add(parallelSimilarity.simVecTarget(doc[metricInverted ? 0 : 1]).toDoubleArray());
             idx++;
             //   System.out.println(Arrays.toString(predicted[idx-1]));
             //   System.out.println(Arrays.toString(foreign[idx-1]));
             if (idx % 10 == 0) {
                 System.err.print(".");
             }
             docsSize++;
         }
         predictedBuilder.finish();
         foreignBuilder.finish();
         System.err.println();
         System.err.println("Starting classification");
         int correct = 0;
         int incorrect = 0;
         int ties = 0;
         int correct5 = 0;
         int correct10 = 0;
         double mrr = 0;
         int i = 0;
 
         for (double[] pred : predicted) {
             if (allZero(pred)) {
                 incorrect++;
                 mrr += 2.0 / docsSize;
                 System.out.print("\u00d8");
                 continue;
             }
             double rightScore;
             double[] foreign_i = foreign.get(i);
             rightScore = cosSim(pred, foreign_i);
             int bestJ = -1;
             int rank = 1;
             double bestMatch = -Double.MAX_VALUE;
             int j = 0;
             for (double[] forin : foreign) {
                 final double cosSim;
                 cosSim = cosSim(pred, forin);
                 if (Double.isNaN(cosSim) || Double.isInfinite(cosSim)) {
                     System.err.print("N");
                     continue;
                 }
                 if ((cosSim > bestMatch && (ties = 0) == 0)
                         || (cosSim == bestMatch && random.nextInt(++ties) == 0)) {
                     bestMatch = cosSim;
                     bestJ = j;
                 }
                 if (cosSim > rightScore) {
                     rank++;
                 }
                 j++;
                 //System.out.println(i+","+j+","+cosSim);
             }
             if (i == bestJ) {
                 correct++;
                 System.out.print("+");
             } else {
                 incorrect++;
                 if (rank < 10) {
                     System.out.print(rank);
                     if (rightScore == 0.0) {
                         System.out.print("!");
                     }
                 } else {
                     System.out.print("-");
                 }
             }
             if (rank <= 5) {
                 correct5++;
             }
             if (rank <= 10) {
                 correct10++;
             }
             mrr += 1.0 / rank;
             i++;
         }
         final DecimalFormat percentFormat = new DecimalFormat("##.##%");
         System.out.println();
         System.out.println("Precision@1: " + correct + " (" + percentFormat.format((double) correct / (double) docsSize) + ")");
         System.out.println("Precision@5: " + correct5 + " (" + percentFormat.format((double) correct5 / (double) docsSize) + ")");
         System.out.println("Precision@10: " + correct10 + " (" + percentFormat.format((double) correct10 / (double) docsSize) + ")");
         System.out.println("MRR: " + (mrr / docsSize));
         return new double[] { (double)correct / docsSize,
             (double)correct5 / docsSize,
             (double)correct10 / docsSize,
             mrr / docsSize
         };
     }
 
     public static <M extends Number, N extends Number> double cosSim(Vector<M> vec1, Vector<N> vec2) {
         double ab = 0.0;
         double a2 = 0.0;
         for (int i : vec1.keySet()) {
             ab += vec2.value(i).doubleValue() * vec1.value(i).doubleValue();
             a2 += vec1.value(i).doubleValue() * vec1.value(i).doubleValue();
         }
         double b2 = 0.0;
         for (int i : vec2.keySet()) {
             b2 += vec2.value(i).doubleValue() * vec2.value(i).doubleValue();
         }
         return a2 > 0 && b2 > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
     }
 
     public static double cosSim(double[] vec1, double[] vec2) {
         double ab = 0.0;
         double a2 = 0.0;
         assert (vec1.length == vec2.length);
         for (int i = 0; i < vec1.length; i++) {
             ab += vec2[i] * vec1[i];
             a2 += vec1[i] * vec1[i];
         }
         double b2 = 0.0;
         for (int i = 0; i < vec1.length; i++) {
             b2 += vec2[i] * vec2[i];
         }
         return a2 > 0 && b2 > 0 ? ab / Math.sqrt(a2) / Math.sqrt(b2) : 0;
     }
 
     public static void main(String[] args) throws Exception {
         final CLIOpts opts = new CLIOpts(args);
 
         final boolean inverseDirection = opts.flag("inv", "Do mate finding from second language to first");
 
         final int ngram = opts.intValue("ngram", "The number of n-grams to use in n-gram based similarity", 0);
 
         final File trainFile = opts.roFile("trainFile", "The training file");
 
         final Class<SimilarityMetricFactory> factoryClazz = opts.clazz("metricFactory", SimilarityMetricFactory.class, "The factory for the cross-lingual similarity measure", metricNames);
 
         final File wordMapFile = opts.roFile("wordMap", "The final containing the word map");
 
         final File testFile = opts.roFile("testFile", "The test file");
 
         opts.restAsSystemProperties();
 
         if (!opts.verify(MateFindingTrial.class)) {
             return;
         }
 
         final int W = WordMap.calcW(wordMapFile);
 
         compare(trainFile, factoryClazz, W, testFile, ngram,inverseDirection);
     }
 
     private static boolean allZero(double[] pred) {
         for (int i = 0; i < pred.length; i++) {
             if (pred[i] != 0.0 && !Double.isInfinite(pred[i]) && !Double.isInfinite(pred[i])) {
                 return false;
             }
         }
         return true;
     }
 }

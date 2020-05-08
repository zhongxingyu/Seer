 package com.winvector.sample.example;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.Random;
 
 import com.winvector.sample.def.RandomVariable;
 import com.winvector.sample.impl.LogNormal;
 
 public final class DefaultExample {
 	
 	private static final class ScoredExample implements Comparable<ScoredExample> {
 		public final double[] x;
 		public final double score;
 		
 		public ScoredExample(final double[] b, final double score) {
 			this.x = b;
 			this.score = score;
 		}
 
 		@Override
 		public int compareTo(final ScoredExample o) {
 			if(score!=o.score) {
 				if(score>=o.score) {
 					return 1;
 				} else {
 					return -1;
 				}
 			}
 			final int n = Math.max(x.length,o.x.length);
 			for(int i=0;i<n;++i) {
 				final double b1 = x[i];
 				final double b2 = o.x[i];
 				if(b1!=b2) {
 					if(b1>=b2) {
 						return 1;
 					} else {
 						return -1;
 					}
 				}
 			}
 			return 0;
 		}
 		
 		@Override
 		public String toString() {
 			final StringBuilder s = new StringBuilder();
 			s.append("[ ");
 			for(final double bi: x) {
 				s.append("" + bi + " ");
 			}
 			s.append("]\t");
 			s.append(score);
 			return s.toString();
 		}
 	}
 	
 
 	// control
 	final int problemDimension = 5;         // dimension of range space
 	final Random rand = new Random(35153);
 	final int improvementSampleSize = 1000; // sample size to use when building new q() distributions
 	final int estimateSampleSize = 100;         // sample size to use in final estimates
 	final int nExpmt = 1000;                // number of repetitions of experiment to perform for final estimates
 
 	// problem
 	final double[] payments = new double[problemDimension-1];
 	final RandomVariable[] p;
 
 	public DefaultExample() {
 		for(int i=0;i<problemDimension-1;++i) {
 			payments[i] = 500;
 		}
 		p = trueModel();
 	}
 	
 	private int score(final double[] x) {
 		final int n = payments.length;
 		int ndefault = 0;
 		for(int i=0;i<n;++i) {
 			final double totalIncome = x[n] + x[i];
 			if(totalIncome<payments[i]) {
 				++ndefault;
 			}
 		}
 		return ndefault;
 	}
 	
 	private static double[] generate(final RandomVariable[] model, final Random rand) {
 		final int n = model.length;
 		final double[] x = new double[n];
 		for(int i=0;i<n;++i) {
 			x[i] = model[i].generate(rand);
 		}
 		return x;
 	}
 	
 	private static double logwt(final RandomVariable[] prob, final double[] example) {
 		final int n = prob.length;
 		double r = 0.0;
 		for(int i=0;i<n;++i) {
 			r += prob[i].logProb(example[i]);
 		}
 		return r;
 	}
 	
 	public RandomVariable[] trueModel() {
 		final RandomVariable[] p = new RandomVariable[problemDimension];
 		final LogNormal externalActual = new LogNormal(1,1);
 		externalActual.setExpectation(2000,1.0);
 		p[problemDimension-1] = externalActual;
 		for(int i=0;i<problemDimension-1;++i) {
 			final double expeci = 2000.0;
 			final LogNormal actualI = new LogNormal(1,1);
 			actualI.setExpectation(expeci,1.0);
 			p[i] = actualI;
 		}
 		return p;
 	}
 
 	private static final class SampleRes {
 		public static final String sep = "\t";
 		public int nSamples = 0;
 		public double sumDefaultRate = 0.0;
 		public double sumWtDefaultCount = 0.0;
 		public double sumWtXDefault = 0.0;
 		public int nHadDefault = 0;
 		
 		public static String header() {
 			return "Method"
 					+ sep + "NumberOfSamples"
 					+ sep + "DefaultRateEstimate"
 					+ sep + "ProbAtLeastOneDefault"
 					+ sep + "ExepctedNumberDefaultsGivenAtLeastOne"
 					+ sep + "ProportonOfSampleWithADefault";
 		}
 		
 		public String row(final String method) {
 			final double defaultRateEstimate = sumDefaultRate/(double)nSamples;
 			final double probAtLeastOneDefault = sumWtXDefault/(double)nSamples;
 			final double exepctedNumberDefaults = sumWtDefaultCount/(double)nSamples;
 			final double exepctedNumberDefaultsGivenAtLeastOne = exepctedNumberDefaults/probAtLeastOneDefault;
 			final double proportonOfSampleWithADefault = nHadDefault/(double)nSamples;
 			return "" + method
 					+ sep + nSamples
 					+ sep + defaultRateEstimate
 					+ sep + probAtLeastOneDefault
 					+ sep + exepctedNumberDefaultsGivenAtLeastOne
 					+ sep + proportonOfSampleWithADefault;
 		}
 	}
 	
 	private SampleRes workStdSample(final Random rand,
 			final RandomVariable[] actualProbs, final int totalTrials) {
 		final SampleRes s = new SampleRes();
 		for(int rep=0;rep<totalTrials;++rep) {
 			final double[] b = generate(actualProbs,rand);
 			final double score = score(b);
 			final double wt = 1.0;
 			final double defaultRateI = score/(double)payments.length;
 			s.nSamples += 1;
 			s.sumDefaultRate += wt*defaultRateI;
 			if(score>0) {
 				s.nHadDefault += 1;
 				s.sumWtXDefault += wt;
 				s.sumWtDefaultCount += wt*score;
 			}
 		}
 		return s;
 	}
 
 	public void doit() {
 		final RandomVariable[] r = new RandomVariable[p.length];
 		for(int i=0;i<p.length;++i) {
 			r[i] = p[i].copy();
 		}
 		
 		final double[] count = new double[problemDimension];
 		final NumberFormat nf = new DecimalFormat("#.##");
 		for(int step=0;step<10;++step) {
			System.out.println("****************");
 			for(int j=0;j<problemDimension;++j) {
 				System.out.print(" , " + nf.format(r[j].expectation()));
 			}
 			System.out.println();
 			for(int j=0;j<problemDimension;++j) {
 				System.out.print(" , " + r[j]);
 			}
 			System.out.println();
 			final ScoredExample[] samples = new ScoredExample[improvementSampleSize];
 			for(int i=0;i<improvementSampleSize;++i) {
 				final double[] b = generate(r,rand);
 				final double score = score(b);
 				samples[i] = new ScoredExample(b,score);
 			}
 			Arrays.sort(samples);
 			Arrays.fill(count,0.0);
			double observationCount = 0.0;
			double nHadDefault = 0;
 			for(int i=0;i<improvementSampleSize;++i) {
				if(samples[i].score>0) {
					nHadDefault += 1;
					final double si = samples[i].score;
 					final double logwtU = logwt(p,samples[i].x);
 					final double logwtP = logwt(r,samples[i].x);
 					final double wt = si*Math.exp(logwtU-logwtP);
					observationCount += si;
 					for(int j=0;j<problemDimension;++j) {
 						count[j] += wt*samples[i].x[j];
 					}
 				}
 			}
			final double pHadDefault = nHadDefault/(double)improvementSampleSize;
			System.out.println("\t" + pHadDefault + " fraction of samples saw a default");
 			for(int j=0;j<problemDimension;++j) {
				final double oldExpectation = r[j].expectation();
				final double newExpectation = count[j]/observationCount;
				System.out.print(" , " + nf.format(newExpectation));
				r[j].setExpectation(0.9*oldExpectation + 0.1*newExpectation);
			}
			System.out.println();
			if(pHadDefault>=0.25) {
				break;
 			}
 		}
 		
 		// get std estimate of default rate
 		System.out.println(SampleRes.header());
 		System.out.println(workStdSample(rand, p, 1000000).row("Large"));
 		for(int rep=0;rep<nExpmt;++rep) {
 			System.out.println(workStdSample(rand, p,estimateSampleSize).row("Standard"));
 		}
 		for(int rep=0;rep<nExpmt;++rep) {
 			final SampleRes s = new SampleRes();
 			for(int i=0;i<estimateSampleSize;++i) {
 				final double[] b = generate(r,rand);
 				final double score = score(b);
 				final ScoredExample samplei = new ScoredExample(b,score);
 				final double logwtU = logwt(p,samplei.x);
 				final double logwtP = logwt(r,samplei.x);
 				final double wt = Math.exp(logwtU-logwtP);
 				final double defaultRateI = samplei.score/(double)payments.length;
 				s.nSamples += 1;
 				s.sumDefaultRate += wt*defaultRateI;
 				if(samplei.score>0) {
 					s.nHadDefault += 1;
 					s.sumWtXDefault += wt;
 					s.sumWtDefaultCount += wt*samplei.score;
 				}
 			}
 			System.out.println(s.row("ImportanceSample"));
 		}
 	}
 
 
 	/**
 	 * @param args
 	 * 
 	 * Implementation of match bits example from:
 	 * "A Tutorial on the Cross-Entropy Method"
 	 * Pieter-TJerk de Boer, Dirk P Kroese, Shie Mannor, and Reuven Y Rubinstein.
 	 * Annals of Operations Research, 2005 vol. 134 (1) pp. 19-67.
 	 */
 	public static void main(String[] args) {
 		final DefaultExample de = new DefaultExample();
 		de.doit();
 	}
 }

 package model;
 
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.text.DecimalFormat;
 import java.util.Random;
 
 import corpus.Corpus;
 import corpus.Instance;
 import corpus.InstanceList;
 import corpus.Vocabulary;
 
 public class NBayes {
 	Corpus c;
 	double smoothParam = .01; //smoothing parameter for normalization
 	int K = -1;
 	int V = -1;
 	int N = -1;
 			
 	double[] pi;
 	double[][] emission; //emission[w][Y] == P(w | Y), size V * K
 	double[][] posterior; //posterior[Y][Xi] = P(Y | Xi), size K * N
 	
 	double[] piExpectedCounts;
 	double[][] emissionExpectedCounts;
 	
	public NBayes(Corpus c, int K, int V) {
 		this.c = c;
 		this.K = K;
 		this.V = V;
 		this.N = c.instanceList.size();
 	}
 	
 	public void initializeSupervised() {
 		pi = new double[K];
 		emission = new double[V][K];
 		for(int n=0; n<N; n++) {
 			for(int k=0; k<K; k++) {
 				if(c.instanceList.get(n).label == -1) {
 					System.err.println("Error: labeled instance does not have label assigned");
 					System.exit(1);
 				}
 				if(c.instanceList.get(n).label == k) {
 					pi[k]++;
 				}
 			}
 		}
 		double sum = 0;
 		for(int k=0; k<K; k++) {
 			sum += pi[k];
 		}
 		//normalize
 		for(int k=0; k<K; k++) {
 			pi[k] = pi[k] / sum;
 		}
 		
 		for(int n=0; n<N; n++) {
 			for(int k=0; k<K; k++) {
 				for(int v=0; v<V; v++) {
 					Instance instance = c.instanceList.get(n);
 					for(int i=0; i < instance.words.length; i++) {
 						if(instance.words[i] == v && instance.label == k) {
 							emission[v][k]++;
 						}
 					}
 				}
 			}
 		}
 		
 		for(int k=0; k<K; k++) {
 			sum = 0;
 			for(int v=0; v<V; v++) {
 				emission[v][k] += smoothParam;
 				sum += emission[v][k];
 			}
 			//normalize
 			for(int v=0; v<V; v++) {
 				emission[v][k] = emission[v][k] / sum;
 			}
 		}
 	}
 	
 	public void initializeRandom(Random r) {
 		pi = new double[K];
 		emission = new double[V][K];
 		
 		double sum = 0;
 		for(int i=0; i<pi.length; i++) {
 			pi[i] = r.nextDouble() + 0.01;
 			sum += pi[i];
 		}
 		//normalize
 		for(int i=0; i<pi.length; i++) {
 			pi[i] = pi[i]/sum;
 		}
 		
 		for(int i=0; i<K; i++) {
 			sum = 0;
 			for(int j=0; j<V; j++) {
 				emission[j][i] = r.nextDouble() + 0.01;
 				sum += emission[j][i];
 			}
 			//normalize
 			for(int j=0; j<V; j++) {
 				emission[j][i] = emission[j][i] / sum;
 			}
 		}
 	}
 	
 	public void train(int numIter){
 		int iterCount = 0;
 		while(iterCount < numIter) {
 			eStep();
 			mStep();
 			sanityCheck();
 			iterCount++;
 		}
 	}
 	
 	public void eStep() {
 		//find the posteriors P(Y=k | X=n; \theta)
 		initPosterior();
 		for(int n=0; n<N; n++) {
 			for(int k=0; k<K; k++) {
 				posterior[k][n] = computePosterior(n, k);
 			}
 		}
 		//compute expected counts
 		piExpectedCounts = new double[K];
 		emissionExpectedCounts = new double[V][K];
 		for(int k=0; k<K; k++) {
 			for(int n=0; n<N; n++) {
 				piExpectedCounts[k] += posterior[k][n];
 			}
 		}
 		
 		for(int k=0; k<K; k++) {
 			for(int v=0; v<V; v++) {
 				for(int n=0; n<N; n++) {
 					Instance instance = c.instanceList.get(n);
 					for(int i=0; i<instance.words.length; i++) {
 						if(instance.words[i] == v) {
 							emissionExpectedCounts[v][k] += posterior[k][n]; 
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void mStep() {
 		//normalize
 		//smoothing
 		double sum = 0;
 		for(int k=0; k<K; k++) {
 			piExpectedCounts[k] += smoothParam;
 			sum += piExpectedCounts[k]; 
 		}
 		verifyPiSum(sum);//sum should be equal to N + K * smoothParam
 		for(int k=0; k<K; k++) {
 			pi[k] = piExpectedCounts[k] / sum;
 		}
 		
 		//normalize emission
 		sum = 0;
 		for(int k=0; k<K; k++) {
 			sum = 0;
 			for(int v=0; v<V; v++) {
 				emissionExpectedCounts[v][k] += smoothParam;
 				sum += emissionExpectedCounts[v][k]; 
 			}
 			for(int v=0; v<V; v++) {
 				emission[v][k] = emissionExpectedCounts[v][k] / sum;
 			}
 		}
 		clearExpectedCounts();
 	}
 	
 	public void sanityCheck() {
 		double piSum = 0;
 		for(int k=0; k<K; k++) {
 			piSum += pi[k];
 		}
 		if(Math.abs(piSum - 1) > 1E-5) {
 			System.err.println("Error: Sanity check of pi not OK, sum = " + piSum);
 		}
 		
 		
 		for(int k=0; k<K; k++) {
 			double emissionSum = 0;
 			for(int v=0; v<V; v++) {
 				emissionSum += emission[v][k];
 			}
 			if(Math.abs(piSum - 1) > 1E-5) {
 				System.err.println("Error: Sanity check of emission for cluster = " + k + " not OK, sum = " + emissionSum);
 			}
 		}
 	}
 	
 	public void clearExpectedCounts() {
 		piExpectedCounts = null;
 		for(int v=0; v<V; v++) {
 			emissionExpectedCounts[v] = null;
 		}
 		emissionExpectedCounts = null;
 	}
 	
 	private void verifyPiSum(double sum) {
 		double actual = (N + K * smoothParam);
 		if(Math.abs(sum - actual) > 1E-5) {
 			System.err.println("Error: pi sum = " + sum + " should be " + actual);
 		}
 	}
 	
 	/**
 	 * @return Computes posterior probability P(Y = k | X_n)
 	 * @param n = instance number
 	 * @param k = class
 	 */
 	public double computePosterior(int n, int k) {
 		double numerator = computeJoint(n, k);
 		double denominator = 0.0;
 		for(int j=0; j<K; j++) {
 			denominator += computeJoint(n, j);
 		}
 		return numerator / denominator;
 	}
 	
 	/**
 	 * @return joint probability
 	 * @param n = instance number
 	 * @param k = class
 	 */
 	public double computeJoint(int n, int k) {
 		Instance instance = c.instanceList.get(n);
 		double prior = pi[k];
 		double prob = 1.0;
 		for(int i=0; i<instance.words.length; i++) {
 			prob = prob * emission[instance.words[i]][k];
 		}
 		prob = prior * prob;
 		return prob;
 	}
 	
 	public void initPosterior() {
 		posterior = new double[K][N];
 	}
 	
 	public void clearPosterior() {
 		for(int i=0; i<K; i++) {
 			posterior[i] = null;
 		}
 		posterior = null;
 	}
 	
 	public void save() {
 		
 	}
 	
 	public void decode(String outFile) throws FileNotFoundException {
 		PrintWriter pw = new PrintWriter(outFile);
 		int correct = 0;
		if(c.labelMap.size() > 1) {
 			System.out.println("Instance \t Actual \t Predicted \t error \tprobability");
 		}
 		for(int n=0; n<N; n++) {
 			int maxCluster = -1;
 			double maxProb = -Double.MAX_VALUE;
 			double maxActualProb = -Double.MAX_VALUE;
 			for(int k=0; k<K; k++) {
 				double prob = computeJoint(n, k);
 				double actualProb = prob;
 				double denom = 0;
 				for(int i=0; i<K; i++) {
 					denom += computeJoint(n, i);
 				}
 				actualProb = prob / denom;
 				if(prob > maxProb) {
 					maxProb = prob;
 					maxCluster = k;
 					maxActualProb = actualProb;
 				}
 			}
			if(c.labelMap.size() > 1) {
 				String label = c.labelIdToString.get(maxCluster);
 				pw.println(label);
 				int actualCluster = c.instanceList.get(n).label;
 				if(maxCluster == actualCluster) {
 					System.out.println((n+1) +"\t\t" + c.labelIdToString.get(actualCluster) + "\t\t" + label + "\t\t" + " " + "\t\t" + 
 							new DecimalFormat("##.##").format(maxActualProb));
 					correct++;
 				} else {
 					System.out.println((n+1) +"\t\t" + c.labelIdToString.get(actualCluster) + "\t\t" + label + "\t\t" + "*" + "\t\t" + 
 							new DecimalFormat("##.##").format(maxActualProb));
 				}
 				
 			} else {
 				pw.println(maxCluster);
 			}
 			pw.flush();
 		}
		if(c.labelMap.size() > 1) {
 			System.out.println("Correct = " + correct + " Total = " + N + " Accuracy = " + (100.0 * correct/N));
 		}
 		pw.close();
 	}
 }

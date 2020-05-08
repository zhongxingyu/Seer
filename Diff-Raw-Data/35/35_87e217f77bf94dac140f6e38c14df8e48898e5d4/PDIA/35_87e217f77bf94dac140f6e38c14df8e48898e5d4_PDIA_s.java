 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.columbia.stat.wood.pdiastick;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import org.apache.commons.math.special.Gamma;
 
 /**
  *
  * @author davidpfau
  */
 public class PDIA {
 
     private HashMap<Integer, int[]> cMatrix;
     private HashMap<Tuple, Integer> dMatrix = new HashMap<Tuple, Integer>();
     private int nsymb;
     public double gamma = 1;
     public double alpha0 = 1;
     public double beta = 1;
     private int[] states = new int[0];
     private int[][] nTables = new int[0][]; // number of tables serving a dish in a restaurant in HDP sampling scheme
     private int[][] nCustomers = new int[0][]; // number of customers in each restaurant eating a dish
     private double[] sticks = new double[0]; // explicit stick lengths for top-level DP
     private GenSym gensym = new GenSym();
     private DirichletDistribution betaDist = new DirichletDistribution(new double[]{1, gamma});
 
     public PDIA(int nsymb) {
         this.nsymb = nsymb;
     }
 
     public Integer transitionAndAdd(Tuple tuple) {
         Integer state = dMatrix.get(tuple);
         if (state == null) {
             double[] probs = new double[states.length + 1];
             double b_u = 1 - Util.sum(sticks);
             for (int t = 0; t < states.length; t++) {
                 probs[t] = nCustomers[t][tuple.get(1)] + alpha0 * sticks[t];
             }
             probs[states.length] = alpha0 * b_u;
             int idx = CategoricalDistribution.sample(probs);
             if (idx == states.length) {
                 state = gensym.sample();
                 addState(b_u, state, tuple.get(1));
             } else {
                 state = states[idx];
             }
             dMatrix.put(tuple, state);
             nCustomers[idx][tuple.get(1)]++;
         }
         return state;
     }
 
     public void count(int[][] data) {
         cMatrix = new HashMap<Integer, int[]>();
         for (Tuple t : new PDIASequence(this, data)) {
             int[] cts = cMatrix.get(t.get(0));
             if (cts == null) {
                 cts = new int[nsymb];
                 cMatrix.put(t.get(0), cts);
             }
             cts[t.get(1)]++;
         }
     }
 
     public double logLik() {
         double logLik = 0;
         double lgb = Gamma.logGamma(beta);
         double bn = beta / nsymb;
         double lgbn = Gamma.logGamma(bn);
 
         for (int[] arr : cMatrix.values()) {
             for (int count : arr) {
                 if (count != 0) {
                     logLik += Gamma.logGamma(count + bn) - lgbn;
                 }
             }
             logLik -= Gamma.logGamma(Util.sum(arr) + beta) - lgb;
         }
         return logLik;
     }
 
     public void sample_tables(int j, int k) {
         double[] prob = new double[nCustomers[k][j]];
         for (int i = 1; i <= nCustomers[k][j]; i++) {
             prob[i - 1] = Math.exp(Gamma.logGamma(alpha0 * sticks[k])
                     - Gamma.logGamma(alpha0 * sticks[k] + nCustomers[k][j]))
                     * Util.stirling(nCustomers[k][j], i)
                     * Math.pow(alpha0 * sticks[k], i);
         }
         nTables[k][j] = CategoricalDistribution.sample(prob) + 1;
     }
 
     public void sample_sticks() {
         double[] params = new double[nTables.length + 1];
         for (int i = 0; i < nTables.length; i++) {
             params[i] = Util.sum(nTables[i]);
         }
         params[nTables.length] = gamma;
         DirichletDistribution d = new DirichletDistribution(params);
         sticks = Util.delete(d.sample().parameters(), params.length - 1, params.length);
         for (int i = 0; i < sticks.length; i++) {
             if (sticks[i] == 0.0) {
                 sticks = Util.delete(sticks, i, i + 1);
                 states = Util.delete(states, i, i + 1);
                 nTables = Util.delete(nTables, i, i + 1);
                 nCustomers = Util.delete(nCustomers, i, i + 1);
                 i--;
             }
         }
     }
 
     public void sample(int[][] data, int[][] test) {
         Object[] os = Util.randArray(dMatrix.keySet());
         for (Object o : os) {
             // Gibbs sample a single transition
             Tuple t = (Tuple) o;
             if (dMatrix.containsKey(t)) {
                 int state = dMatrix.get(t);
                 int currIdx = -1;
                 double[] logProb = new double[states.length + 1];
                 int[][] n2 = Util.copy(nCustomers);
                 for (int i = 0; i < logProb.length - 1; i++) {
                     int n_ij = n2[i][t.get(1)];
                     if (states[i] == state) {
                         n_ij--;
                         currIdx = i;
                     }
                     logProb[i] = Math.log(n_ij + alpha0 * sticks[i]) + newLogLik(t, states[i], data);
                 }
                 while (logProb.length < states.length + 1) { // Since calling newLogLike can add new states, continue assigning t to different states until we don't add any new ones.
                     double[] newProb = new double[states.length + 1];
                     System.arraycopy(logProb, 0, newProb, 0, logProb.length - 1);
                    for (int i = logProb.length; i < newProb.length - 1; i++) {
                         newProb[i] = Math.log(alpha0 * sticks[i]) + newLogLik(t, states[i], data);
                     }
                     logProb = newProb;
                 }
                 double b_u = 1 - Util.sum(sticks);
                logProb[logProb.length - 1] = Math.log(alpha0 * b_u) + newLogLik(t, gensym.sample(), data);
 
                 // Avoid numerical underflow
                 double max = Double.NEGATIVE_INFINITY;
                 for (int i = 0; i < logProb.length; i++) {
                     if (logProb[i] > max) {
                         max = logProb[i];
                     }
                 }
                 double[] prob = new double[logProb.length];
                 for (int i = 0; i < logProb.length; i++) {
                     prob[i] = Math.exp(logProb[i] - max);
                 }
 
                 // Assign new state to transition
                 int idx = CategoricalDistribution.sample(prob);
                 nCustomers[currIdx][t.get(1)]--;
                 if (idx == states.length) {
                    addState(b_u, dMatrix.get(t), t.get(1));
                 } else {
                     dMatrix.put(t, states[idx]);
                     if (nTables[idx][t.get(1)] == 0) {
                         nTables[idx][t.get(1)] = 1;
                     }
                 }
                 nCustomers[idx][t.get(1)]++;
 
                 // Clear unused states
                if (idx != states.length - 1) {
                    count(data);
                }
                 clearStates();
 
                 // Sample number of tables in lower level restaurants
                 for (int k = 0; k < nTables.length; k++) {
                     for (int j = 0; j < nsymb; j++) {
                         if (nCustomers[k][j] == 0) {
                             nTables[k][j] = 0;
                         } else if (nCustomers[k][j] == 1) {
                             nTables[k][j] = 1;
                         } else {
                             sample_tables(j, k);
                         }
                     }
                 }
                System.out.println(sticks.length + ": " + logLik() + ", " + PDIA.averageScore(score(test)));
                 count(data);
                 clearStates();
             }
         }
 
         // Sample stick lengths for top-level restaurant
         sample_sticks();
     }
 
     private void clearStates() {
         HashSet<Tuple> keysToDiscard = new HashSet();
         for (Tuple key : dMatrix.keySet()) {
             int[] counts = cMatrix.get(key.get(0));
             if (counts == null || counts[key.get(1)] == 0) {
                 keysToDiscard.add(key);
             }
         }
 
         HashMap<Integer, Integer> stateIndex = new HashMap<Integer, Integer>();
         for (int i = 0; i < states.length; i++) {
             stateIndex.put(states[i], i);
         }
         for (Tuple t : keysToDiscard) {
             Integer next = dMatrix.get(t);
             Integer idx = stateIndex.get(next);
             nCustomers[idx][t.get(1)]--;
             dMatrix.remove(t);
         }
 
         for (int idx = 0; idx < states.length; idx++) {
             if (Util.sum(nCustomers[idx]) == 0) {
                 states = Util.delete(states, idx, idx + 1);
                 sticks = Util.delete(sticks, idx, idx + 1);
                 nTables = Util.delete(nTables, idx, idx + 1);
                 nCustomers = Util.delete(nCustomers, idx, idx + 1);
                 idx--;
             }
         }
     }
 
     public double newLogLik(Tuple t, Integer state, int[][] data) {
         dMatrix.put(t, state);
         count(data);
         return logLik();
     }
 
     private void addState(double b_u, int state, int symbol) {
         double b = betaDist.sample().probability(0);
         states = Util.append(states, state);
         sticks = Util.append(sticks, b_u * b);
         nTables = Util.append(nTables, new int[nsymb]);
         nCustomers = Util.append(nCustomers, new int[nsymb]);
         nTables[states.length - 1][symbol]++;
     }
 
     public double[][] score(int[][] data) {
         double[][] scores = new double[data.length][];
         int line = -1;
         int pos = 0;
         for (Tuple t : new PDIASequence(this, data)) {
             if (t.get(0) == 0) {
                 line ++;
                 pos = 0;
                 scores[line] = new double[data[line].length];
             }
             int[] counts = cMatrix.get(t.get(0));
             if (counts == null) {
                 counts = new int[nsymb];
                 cMatrix.put(t.get(0),counts);
             }
 
             int totalCount = Util.sum(counts);
             scores[line][pos++] = ((counts[t.get(1)] + beta / nsymb) / (totalCount + beta));
             counts[t.get(1)]++;
         }
         return scores;
     }
 
     public static double averageScore(double[][] score) {
         double totalScore = 0.0;
         int totalLength = 0;
         for (int i = 0; i < score.length; i++) {
             totalScore  += Util.sum(Util.log(score[i]));
             totalLength += score[i].length;
         }
         return totalScore / totalLength / Math.log(2);
     }
 
     public boolean check() {
         HashMap<Integer,Integer> stateIndex = new HashMap<Integer,Integer>();
         for (int i = 0; i < states.length; i++) {
             stateIndex.put(states[i],i);
         }
         int[][] n = new int[states.length][nsymb];
         for( Tuple t : dMatrix.keySet() ) {
             int state = dMatrix.get(t);
             int idx = stateIndex.get(state);
             int symb = t.get(1);
             n[ idx ][ symb ] ++ ;
         }
         for (int i = 0; i < states.length; i++) {
             if (!Arrays.equals(nCustomers[i], n[i])) {
                 return false;
             }
         }
         return true;
     }
 
     private class PDIASequence implements Iterable<Tuple>, Iterator<Tuple> {
         private int[][] data;
         private PDIA p;
         private int line;
         private int pos;
         private int state;
 
         private PDIASequence(PDIA p, int[][] data) {
             this.p = p;
             this.data = data;
             line = 0;
             pos = 0;
             state = 0;
         }
 
         @Override
         public Iterator<Tuple> iterator() {
             return this;
         }
 
         @Override
         public boolean hasNext() {
             return data.length != 0 && (line < data.length - 1 || (line == data.length - 1 && pos < data[line].length));
         }
 
         @Override
         public Tuple next() {
             Tuple t = new Tuple(state, data[line][pos]);
             if (pos == data[line].length - 1) {
                 pos = 0;
                 line++;
                 state = 0;
             } else {
                 pos++;
                 state = p.transitionAndAdd(t);
             }
             return t;
         }
 
         @Override
         public void remove() {
         }
     }
 
     public static void main(String[] args) {
         HashMap<Integer, Integer> alphabet = new HashMap<Integer, Integer>();
         try {
             int[][] train = Util.loadText("/Users/davidpfau/Documents/Wood Group/pdia_git/data/aiw.train", alphabet);
             int[][] test  = Util.loadText("/Users/davidpfau/Documents/Wood Group/pdia_git/data/aiw.test",  alphabet);
             PDIA p = new PDIA(alphabet.size());
             p.count(train);
            for (int i = 0; i < 10; i++) {
                 p.sample(train,test);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }

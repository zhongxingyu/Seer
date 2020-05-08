 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Yuri
  */
 public class HMM extends HMMAbstract {
     
     public HMM() {
         
     }
     
     public double BaumWelch() {
         int N = initial.length;
         int M = emission[0].length;
         int T = sequence.length;
         
         
         // [state][time]
         double[][] alpha = new double[N][T];
         double[] c = new double[T];
         
         //alpha pass
         
         // compute alpha[i][0]
         c[0] = 0;
         for(int i=0; i<N; i++) {
             alpha[i][0] = initial[i] * emission[i][sequence[0]];
             c[0] = c[0] + alpha[i][0];
         }
         
         // scale the alpha[i][0]
         c[0] = 1.0/c[0];
         for(int i=0; i<N; i++) {
             alpha[i][0] = c[0]*alpha[i][0];
         }
         
         // compute alpha[i][t]
         for(int t=1; t<T; t++) {
             c[t] = 0;
             for(int i=0; i<N; i++) {
                 alpha[i][t] = 0;
                 for(int j=0; j<N; j++) {
                     alpha[i][t] = alpha[i][t] + alpha[j][t-1] * transition[j][i];
                 }
                 alpha[i][t] = alpha[i][t] * emission[i][sequence[t]];
                 c[t] = c[t] + alpha[i][t];
             }
             
             // scale alpha[i][t]
             c[t] = 1.0/c[t];
             for(int i=0; i< N; i++) {
                 alpha[i][t] = c[t] * alpha[i][t];
             }
         }
         
         // beta pass
         // [State][time]
         double[][] beta = new double[N][T];
         
         // Let beta[i][T-1] =1 scaled by c[T-1]
         for(int i=0; i<N; i++) {
             beta[i][T-1] = c[T-1];
         }
         
         // Beta-pass
         for(int t=T-2; t>=0; t--) {
             for(int i=0; i<N; i++) {
                 beta[i][t] = 0;
                 for(int j=0; j<N; j++) {
                     beta[i][t] = beta[i][t] + (transition[i][j]*emission[j][sequence[t+1]]*beta[j][t+1]);
                 }
                 // scale the beta[i][t] with same scale factor as alpha[i][t]
                 beta[i][t] = c[t] * beta[i][t];
             }
         }
         
         // compute gamma and digamma
         double[][] gamma = new double[N][T]; // [State][Time]
         double[][][] digamma = new double[N][N][T]; // [State][State][Time]
         
         for(int t=0; t < T-1; t++) {
             double denom = 0;
             for(int i=0; i< N; i++) {
                 for(int j=0; j< N; j++) {
                     denom = denom + alpha[i][t] * transition[i][j] * emission[j][sequence[t+1]] * beta[j][t+1];
                 }
             }
             for(int i=0; i<N; i++) {
                 gamma[i][t] = 0;
                 for(int j=0; j<N; j++) {
                     digamma[i][j][t] = (alpha[i][t]*transition[i][j]*emission[j][sequence[t+1]]*beta[j][t+1]) / denom;
                     gamma[i][t] = gamma[i][t] + digamma[i][j][t];
                 }
             }
         }
         
         // re-estimate pi
         for(int i=0; i<N; i++) {
             initial[i] = gamma[i][0];
         }
         
         // re-estimate A
         for(int i=0; i<N; i++) {
             for(int j=0; j< N; j++) {
                 double numer = 0;
                 double denom = 0;
                 for(int t=0; t < T-1; t++) {
                     numer = numer + digamma[i][j][t];
                     denom = denom + gamma[i][t];
                 }
                 transition[i][j] = numer / denom;
             }
         }
         
         // re-estimate B
         for(int i=0; i<N; i++) {
             for(int j=0; j<M; j++) {
                 double numer =0;
                 double denom = 0;
                 for(int t=0; t < T-1; t++) {
                     if(sequence[t] == j) {
                         numer = numer + gamma[i][t];
                     }
                     denom = denom + gamma[i][t];
                 }
                 emission[i][j] = numer / denom;
                 
             }
         }
         
         double logprob = 0;
         for(int i=0; i<T; i++) {
             logprob = logprob + Math.log(c[i]);
         }
         
         return -logprob;
                 
     }
     
    public void train() {
         int maxIters = 100;
         int iters = 0;
         double oldLogProb = Double.NEGATIVE_INFINITY;
         double logprob = Double.NEGATIVE_INFINITY;
         
        while(iters < maxIters && logprob <= oldLogProb) {
             oldLogProb = logprob;
             logprob = BaumWelch();
             iters++;
         }
     }
 }

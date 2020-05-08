 import java.lang.Math;
 
 public class SVMkernelAdatron{
 	Double [][] data;
 	Double [][] x;
 	int N, M;
 	Double[] y;
 
 	Double C;
 	Double gamma; //We are using the Gaussian Kernel.
 
 	int numiter = 100;
 
 	Double[] a;
 
 	public void BuildClassifier(Double [][] data){
 		this.data = data;
 		this.N=data.length; //number of rows
 		this.M=data[0].length-1; //number of attributes (the last attribute is attribute y)
 
 		//Fill in x
 		int n;
 		this.x = new Double[N][M];
 		for(n=0; n<N && data[n]!=null; n++)
 			for(int m=0; m<M; m++)
 				x[n][m] = data[n][m];
 		N=n;
 
 		//Fill in y
 		this.y = new Double[N];
 		for(n=0; n<N; n++) y[n] = data[n][M];
 
 		//Compute
 		this.a = this.ComputeAlphasWithSGD();
 	}
 
 	public void ClearClassifier() {
 		this.a = null;
 	}
 
 	public Double Classify(Double[] xn) {
         Double sum = 0.0;
         
         for(int i=0;i<N;i++)
         {
             sum += y[i]*a[i]*K(x[i],xn);
             
         }
         
         if(sum > 0 || sum == 0)
         {
             return 1.0;
         }
         else
         {
             return -1.0;
         }
 	}
 
 	Double K(Double[] x_i, Double[] x_j) {
         Double temp;
         
         Double sum = 0.0;
         
        for(int i = 0;i<M;i++)
         {
             sum += Math.pow((x_i[i] - x_j[i]),2);
         }
         
         temp = -gamma * sum;
         
         return Math.exp(temp);
 	}
 
 	Double Gradient_i(Double[] a, int i) {
         Double sum = 0.0;
         
         for(int j = 0;j<N;j++)
         {
             sum += y[j]*a[j]*K(x[i],x[j]);
         }
         
         return 1-(y[i]*sum);
 	}
 
 	Double Dual(Double[] a) {
         Double sum = 0.0;
         Double sum2 = 0.0;
         
         for(int i = 0;i<N;i++)
         {
             sum2 += a[i];
         }
         
         for(int i = 0;i<N;i++)
         {
             for(int j = 0;j<N;j++)
             {
                 sum += a[i]*a[j]*y[i]*y[j]*K(x[i],x[j]);
             }
         }
         
        return sum2 - ((.5)*sum);
 	}
 
 	Double[] ComputeAlphasWithSGD() { 
         Double eta,C;
         eta = C = 1.0;
         
         a = new Double[N];
         
         for(int i =0;i<a.length;i++)
         {
             a[i] = 0.0;
         }
         
         for(int t = 0;t<30;t++)
         {
             for(int i = 0;i<N;i++)
             {
                 a[i]=a[i]+eta*Gradient_i(a,i);
                 
                 if(a[i]<0.0)
                     a[i] = 0.0;
                 if(a[i]>C)
                     a[i] = C;
             }
         }
         
         return a;
 	}
 
 	public Double getC() {
 		return C;
 	}
 
 	public void setC(Double c) {
 		C = c;
 	}
 
 	public Double getGamma() {
 		return gamma;
 	}
 
 	public void setGamma(Double gamma) {
 		this.gamma = gamma;
 	}
 
 	public int getNumiter() {
 		return numiter;
 	}
 
 	public void setNumiter(int numiter) {
 		this.numiter = numiter;
 	}
 
 	public Double[] getA() {
 		return a;
 	}
 
 	public void setA(Double[] a) {
 		this.a = a;
 	}
 
 	public static void main(String[] args) throws Exception {
 
 		Double[][] data = Utilities.ReadData("svmguide3Normalized.csv");
 
 		SVMkernelAdatron svm = new SVMkernelAdatron();
 		svm.setNumiter(30);
 
 		svm.setC(1.0);
 		svm.setGamma(1.0);
 
 		svm.BuildClassifier(data);
 
 		System.out.print("a = ");
 		Utilities.PrintVector(svm.getA());
 
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 /*
 public Double K(Double[] x_i, Double[] x_j) { //Just dot-product
 	//For the moment just do dot-product
 	Double dotp = 0.0;
 	for(int m=0; m<M; m++)
 		dotp += x_i[m]*x_j[m];
 
 	return dotp;
 }
 */
 
 /*
 public Double[] w(Double[] a) {
 	Double[] w = new Double[M];
 	for(int m=0; m<M; m++) {
 		w[m] = 0.0;
 		for(int i=0; i<N; i++)
 			w[m] += a[i]*y[i]*x[i][m];
 	}
 	return w;
 }
 */
 
 /* Not Ok, can't be computed afterwards
 public Double b(Double[] a, Double[] w) {
 	for(int i=0; i<N; i++) {
 		if (a[i]>0.1 && a[i]<C-0.1) {
 			Double wx = 0.0;
 			for(int m=0; m<M; m++)
 				wx += w[m]*x[i][m];
 			return y[i]-wx;
 		}
 	}
 
 	return 1000000.0;
 }
 */

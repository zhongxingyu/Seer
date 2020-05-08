 package NerualNet;
 
 import java.util.Scanner;
 import java.io.File; 
 import java.io.IOException;
 
 // Main driver class for all network types
 public class NeuralNet implements Runnable{
 	
 /*** Setup requirements for multithreading the training process **************/	
 	Network net;
 	double[][] trainSet;
 	int numSamples;
 	int epochs;
 	char[] classes;
 	
 	public NeuralNet(Network net, double[][] trainSet, int numSamples, int epochs, char[] classes){
 		this.net = net;
 		this.trainSet = trainSet;
 		this.numSamples = numSamples;
 		this.epochs = epochs;
 		this.classes = classes;
 	}
 	public void run(){
 		this.net.train(this.trainSet, this.numSamples, this.epochs, this.classes);
 	}
 /*************** End threading setup *******************************************/
 	
 //// Main Program Entry	
 	public static void main(String[] args) throws IOException, InterruptedException {
 		int type, inputs, samples, epochs, correct, error, fail;
 		
 		int outputs = 1;
 		int layers = 0;
 		int centers = 0;
 		int numLabels = 0;
 		int numClasses = 26;
 		boolean classFirst = true;
 		
 	/** Set number of threads here **/
 		int numThreads = 8 ;
 	/********************************/
 		
 		char out;
 		
 		char[] expected1;
 		char[] expected2;
 		char[] classes;
 		
 		double rate;
 		
 		double[][] set1;
 		double[][] set2;
 		
 		String fileName1;
 		
 		Scanner keyscan;
 		Scanner filescan1;
 		
 		Thread[] threads;
 		
 		Network[] net;
 		
 		File file1;
 		
 		rate = 0.01;
 		
 		keyscan = new Scanner(System.in);
 		
 		
 		System.out.println("Select network type: ");
 		System.out.println("1. MLP");
 		System.out.println("2. RBF");
 		System.out.println("3. ANFIS");
 		
 		type = keyscan.nextInt();
 		
 		if(type == 1){
 			System.out.println("Enter the number of hidden layers: ");
 			layers = keyscan.nextInt();
 		}
 		else if (type == 2){
 			System.out.println("Enter the number of centers: ");
 			centers = keyscan.nextInt();
 		}
 		else if (type == 3){
 			System.out.println("Enter the number of labels: ");
 			numLabels = keyscan.nextInt();
 		}
 		
 		System.out.println("Enter number of training epochs: ");
 		epochs = keyscan.nextInt();
 		
 		System.out.println("Enter number of samples: ");
 		samples = keyscan.nextInt();
 		
 		System.out.println("Enter a value for n: ");
 		inputs = keyscan.nextInt();
 		
 		//System.out.println("Enter input file name: ");
 		//fileName1 = keyscan.next();
 		
 		//////// Hardcode filename for now
 		fileName1 = "data/letter-recognition.data";
 		////////
 		
 		file1 = new File(fileName1);
 		
 		filescan1 = new Scanner(file1);
 		filescan1.useDelimiter(",|\\n");
 		
 		set1 = new double[inputs][samples];
 		set2 = new double[inputs][samples];
 		
 		classes = new char[numClasses];
 		
 		expected1 = new char[samples];
 		expected2 = new char[samples];
 		
 		threads = new Thread[numThreads];
 				
 		// Read in list of possible classes
 		for (int i = 0; i < numClasses; i++){
 			classes[i] = filescan1.next().charAt(0);
 		}
 		
 		// Read in training set vectors
 		for (int i = 0; i < samples; i++){
 			if(classFirst)
 				expected1[i] = filescan1.next().charAt(0);
 			
 			for (int j = 0; j < inputs; j++){
 				set1[j][i] = Integer.parseInt(filescan1.next());
 			}
 			
 			if(!classFirst)
 				expected1[i] = filescan1.next().charAt(0);
 		}
 		
 		// Read in test set vectors
 		for (int i = 0; i < samples; i++){
 			if(classFirst)				
 				expected2[i] = filescan1.next().charAt(0);
 			
 			for (int j = 0; j < inputs; j++){
 				set2[j][i] = Integer.parseInt(filescan1.next());
 			}
 			
 			if(!classFirst)				
 				expected2[i] = filescan1.next().charAt(0);
 		}			
 		
 		// Create one network for each possible class
 		net = new Network[numClasses];	
 		
 		// Fill up the thread array with training networks
 		int t = 0;		
 		for (int i = 0; i < numClasses; i++){
 			if(type == 1)
 				net[i] = new MLPNet(inputs, layers, outputs, rate, classes[i]);
 			else if (type == 2)
 				net[i] = new RBFNet(inputs, centers, outputs, rate, classes[i], samples, set1);
 			else if (type == 3)
 				net[i] = new ANFISNet(inputs, numLabels, outputs, rate, classes[i], set1);
 			
 			threads[t] = new Thread(new NeuralNet(net[i], set1, samples, epochs, expected1));
 			threads[t].start();
 			
 			// If max number of threads is reached, wait for them to finish before starting a new batch
 			if(t == numThreads - 1){
 				for (Thread thread : threads) {
 					  thread.join();
 				}
 				t = 0;				
 			}
 			else
 				t++;
 		}
 		
 		// Make sure last batch of threads finished before moving on
 		for (Thread thread : threads) {
 			  thread.join();
 		}
 		
 		// Begin testing phase ///////////////////////////////////////
 		correct = error = fail = 0;		
 		
 		// Check each test vector against each class network
 		for (int a = 0; a < samples; a++){
 			int responses = numClasses;
 			for (int i = 0; i < numClasses; i++){
 				out = net[i].process(set2, a);
 				if(out != '!'){
 					if(out == expected2[a]){
 						//System.out.print(out + " ");
 						correct++;
 					}
 					else{						
 						//System.out.print("Error(" + out + ") ");
 						error++;
 					}
 				}
 				else
 					responses--;
 			}
 			if(responses == 0){
 				//System.out.print("Failed to classify");
 				fail++;
 			}
 			//System.out.println();
 		}
 		
 		System.out.println("Correct: " + correct + ", Error: " + error + ", Failed to Class: " + fail);
 		System.out.println("Percent incorrect: " + (((double)(error + fail))/((double)samples))*100.00 + "%");
 		
 		filescan1.close();
 		keyscan.close();		
 	}
 
 }

 import Data.DataProvider;
 import Data.ImageData;
 import NeuralNetwork.Network;
 
 
 public class Main {
 	/**
 	 * @param args
 	 * @throws InterruptedException 
 	 */
 	
 	public static void main(String[] args) throws InterruptedException {
 		// TODO Auto-generated method stub
 		
 		// init Data
 		DataProvider dataProvider;
 		
 		if(args.length >= 1)
 		{
 			dataProvider = new DataProvider(args[0]);
 			System.out.println("\n  data path: " + args[0]);
 		}
 		else
 			dataProvider = new DataProvider("./bin/test.h5");
 		ImageData[] testData = dataProvider.getTestData();
 		
 		
 		
 		// init Networks
 		Network learningNets[] = new Network[6];
 		if (args.length >= 3)
 		{
 			// create 6 Networks with different learning data
 			for(int i = 0;i < 6; i++)
 			{
				learningNets[i] = new Network(Float.parseFloat(args[1]),Integer.parseInt(args[2]),dataProvider.getLerntData(i));
 			}
 			System.out.println("  learning rate: " + args[1]);
 			System.out.println("  number hidden nodes: " + args[2]);
 		}
 		else
 		{
 			for(int i = 0;i < 6; i++)
 			{
				learningNets[i] = new Network(0.02f,20,dataProvider.getLerntData(i));
 			}
 		}
 		
 		
 		float error = 1; 
 		//TODO: determine best learningrate
 		//TODO: compare normalisation and no normalisation
 		//TODO: dynamic learnrate
 		int loopCount = 0;
 		while(error > 0.01)
 		{
 			// creating Threads
 			Thread threads[] = new Thread[learningNets.length];
 			for(int i= 0; i < learningNets.length; i++)
 			{
 				threads[i] = new Thread(learningNets[i]);
 				threads[i].start();
 			}
 			for(Thread t :threads)
 				t.join();
 			// middel all nets
 			Network net = middelNets(learningNets);
 			
 			// testing the net
 			int sumTrue = 0;
 			for(ImageData img :testData)
 			{
 				net.setInput(img);
 				net.passforward();
 				if((int) img.getLabel() == net.getOutput())
 					sumTrue++;
 			}
 		    error = (1.0f - ((float)sumTrue/ (float)testData.length));
 			
 		}
 		
 		public static Network middelNets(Network[] nets)
 		{
 			float[] outputWeights = new float[nets.length];
 			for(int i = 0; i < nets.length; i++)
 			{
 				outputWeights[i] = nets[i].getOutputLayer().ge
 			}
 		}
 		
 	}
 	
 	
 
 }

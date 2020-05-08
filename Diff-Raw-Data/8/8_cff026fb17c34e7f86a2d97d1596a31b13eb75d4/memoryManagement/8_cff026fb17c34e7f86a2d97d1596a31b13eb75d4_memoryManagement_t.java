 /*Memory Management Program
  * Used to test the algorithm and print out results.
  */
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 
 public class memoryManagement{
 
 	static final int JOBAMOUNT = 200;
 	static final int MEMORYSIZE = 100;
 	
 	public static void main(String args[])throws Exception{
 				
 		File file = new File("null");
 		PrintWriter out = new PrintWriter(new File("log.txt"));
 		Scanner keyboard = new Scanner(System.in);
 		Scanner fileScan;
 		StringTokenizer token;
 		Random rand = new Random();
 
 		String read = null;
 		int jobLength = 0;
 		long[] timeStart = new long[5], timeEnd = new long[5];
 		
 		//*Job Info*
 		int[] id = new int[JOBAMOUNT];
 		int[] size = new int[JOBAMOUNT];
 		int[] time = new int[JOBAMOUNT];
 		
 		//******Add your algorithm class here******//
 		threadedAllocation threadedFit = new threadedAllocation(MEMORYSIZE);
 		FirstFit firstFit = new FirstFit();
 		NextFit nextFit = new NextFit();
 		BestFitAlgorithm bestFit = new BestFitAlgorithm(MEMORYSIZE);
 		WorstFitAlgorithm worstFit = new WorstFitAlgorithm(MEMORYSIZE);
 		
 		//Gets a file name, else creates five random jobs
 		do{							
 			System.out.println("Type filename to load jobs from a file or just press enter for random jobs");
 			read = keyboard.nextLine();
 			file = new File(read + ".txt");
 			if(!read.equals("") && !file.exists())
 				System.out.println("File not found, try again");
 		}while(!read.equals("") && !file.exists());
 	
 		//Create random jobs or read from the file and create jobs
 		if(read.equals("")){
 			System.out.print("Creating " + JOBAMOUNT + " random jobs...");
 			jobLength = JOBAMOUNT;
 			for(int i = 0; i < jobLength; i++){
 				id[i] = i+1;
 				size[i] = rand.nextInt(MEMORYSIZE / 10)+1;
 				time[i] = rand.nextInt(5000)+ 101;
 			}
 			System.out.println("complete");
 		}
 		else{
 			System.out.print("File found, reading file...");
 			fileScan = new Scanner(file);
 			for(jobLength = 0; fileScan.hasNextLine() ; jobLength++){
 				token = new StringTokenizer(fileScan.nextLine(),",");
 				id[jobLength] = jobLength+1;
 				size[jobLength] = Integer.parseInt(token.nextToken());
 				time[jobLength] = Integer.parseInt(token.nextToken());
 			}
 			fileScan.close();
 			System.out.println("complete");
 			System.out.println(jobLength+" jobs found on file");
 		}
 		
 		//Send jobs to algorithm, time is calculated and printed out after completion
 		//Note that we use `jobLength - 1` to compensate for the id above
 		//Threaded Fit
 		System.out.print("Sending jobs to threaded allocation algorithm...");
 		timeStart[0] = System.currentTimeMillis();
 		for(int i = 0; i < jobLength - 1; i++)
 			threadedFit.allocate(id[i], size[i], time[i]);
 		timeEnd[0] = System.currentTimeMillis() - timeStart[0];
 		System.out.println("complete");
 		System.out.println("Elapsed time for threaded allocation algorithm to complete " + jobLength + " jobs is " + timeEnd[0] + " milliseconds");
 		
 		//Best Fit
 		System.out.print("Sending jobs to best fit allocation algorithm...");
 		timeStart[1] = System.currentTimeMillis();
 		for(int i = 0; i < jobLength - 1; i++)
 			bestFit.allocate(id[i], size[i], time[i]);
 		timeEnd[1] = System.currentTimeMillis() - timeStart[1];
 		System.out.println("complete");
 		System.out.println("Elapsed time for best fit allocation algorithm to complete " + jobLength + " jobs is " + timeEnd[1] + " milliseconds");
 		
 		//Worst Fit
 		System.out.print("Sending jobs to worst fit allocation algorithm...");
 		timeStart[2] = System.currentTimeMillis();
 		for(int i = 0; i < jobLength - 1; i++)
 			worstFit.allocate(id[i], size[i], time[i]);
 		timeEnd[2] = System.currentTimeMillis() - timeStart[2];
 		System.out.println("complete");
 		System.out.println("Elapsed time for worst fit allocation algorithm to complete " + jobLength + " jobs is " + timeEnd[2] + " milliseconds");
 		
 		//First Fit
 		System.out.print("Sending jobs to first fit allocation algorithm...");
 		timeStart[3] = System.currentTimeMillis();
 		for(int i = 0; i < jobLength - 1; i++)
 			firstFit.allocate(id[i], size[i], time[i]);
 		timeEnd[3] = System.currentTimeMillis() - timeStart[3];
 		System.out.println("complete");
 		System.out.println("Elapsed time for first fit allocation algorithm to complete " + jobLength + " jobs is " + timeEnd[3] + " milliseconds");
 		
 		//Next Fit
 		System.out.print("Sending jobs to next fit allocation algorithm...");
 		timeStart[4] = System.currentTimeMillis();
 		for(int i = 0; i < jobLength - 1; i++)
 			nextFit.allocate(id[i], size[i], time[i]);
 		timeEnd[4] = System.currentTimeMillis() - timeStart[4];
 		System.out.println("complete");
 		System.out.println("Elapsed time for next fit allocation algorithm to complete " + jobLength + " jobs is " + timeEnd[4] + " milliseconds");
 		
 		System.out.println("Printing to log...");
 		out.println("Memory Management Log");
 		out.println("---------------------------");
 		out.println("Job Amount: " + jobLength);
 		out.println("Memory Size: " + MEMORYSIZE);
 		out.println("---------------------------");
 		out.println("Final Times (All times in milliseconds)");
 		out.println("Threaded time: " + timeEnd[0]);
		out.println("Best fit time: " + timeEnd[1]);
		out.println("Worst fit time: " + timeEnd[2]);
		out.println("First fit time: " + timeEnd[3]);
		out.println("Next fit time: " + timeEnd[4]);
 		out.close();
 		System.out.println("complete");
 		
 		System.out.println("Completed Successfully");
 		//Forcibly close down all threads
 		System.exit(0);
 	}
 }

 public class dummyAlgorithm{
 	
 	int[] memoryBlock;
 	
	void dummyAlgorithm(int memorySize){
 		/* Constructor needed for each algorithm */
 		memoryBlock = new int[memorySize];
 	}
 	
 	void allocate(int jobID, int jobSize, int jobTime){
 		/* This method to be overloaded by each algorithm */
		return System.out.println("Allocating job " + jobID + " with size: " + jobSize + " for: " + jobTime + " milliseconds.");
 	}
 	void deallocate(int jobSize, int beginningLocation){
		return System.out.println("Removing job with size: " + jobSize + " beginning at: " + beginningLocation);
 	}
 }

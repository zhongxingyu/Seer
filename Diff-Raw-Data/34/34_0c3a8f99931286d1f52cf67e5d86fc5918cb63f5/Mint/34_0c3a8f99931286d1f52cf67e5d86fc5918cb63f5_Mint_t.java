 package hps.mint;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.List;
 
 public class Mint
 {
 	private static final Long startTime = System.currentTimeMillis();
 	
 	private static Solution exactChangeSolution;
 	private static Solution exchangeSolution;
 	
 	public static void main(String[] args)
     {
         if (args.length != 1) {
             usage(args);
             System.exit(-1);
         }
 
         Float weight = 1.0F;
         try {
             weight = Float.valueOf(args[0]);
         } catch (NumberFormatException e)  {
             usage(args);
             System.exit(-1);
         }
 
         System.out.println("Executing. N = " + weight);
         
         new Conductor(weight).conduct();
         
         System.exit(1);
     }
 	
 	private static void usage(String[] args)
     {
         String argStr = "";
         for(String a : args) argStr += a;
         System.out.println("Invalid/missing arguments. " + argStr);
         System.out.println("\tusage: java -jar mint-problem-1.0.0.jar 4.0");
     }
 	
 	// Invoked by different threads to report
 	// a solution for a denomination set.
 	// It would evaluate each passed Solution object looking
 	// for the best solution thus far for both problems.
 	public synchronized static void judge(Solution s){
 
 		if( s.exactScore > 0 ){ // solution contains a valid exact score
 
 			// First Solution is the best solution
 			if(exactChangeSolution == null){
 				exactChangeSolution = s;
 			}
 			else{
 				//new solutions is better than previous solution 
 				if(s.exactScore < exactChangeSolution.exactScore){
 					exactChangeSolution = s;
 				}
 			}
 		}
 
 		if(s.exchangeScore > 0){
 
 			if(exchangeSolution == null){
 				exchangeSolution = s;
 			}else{
 				//new solutions is better than previous solution 
 				if(s.exchangeScore < exchangeSolution.exchangeScore){
 					exchangeSolution = s;
 				}
 			}
 		}
 
 	}
 
 	// Create output file
 	// Would be invoked when all solutions for all denominations
 	// have been calculated and passed to the judge(...) method.
 	// It prints the contents of exactChangeSolution and
 	// exchangeSolution in the specified format to the output file.
 	public static void stop(){
 		// Create output File
 		try{
 			// Create file 
 			FileWriter fstream = new FileWriter("rky-mint-output.txt.txt");
 			BufferedWriter out = new BufferedWriter(fstream);
 			
 			//Print EXACT_CHANGE_NUMBER:
 			out.write("EXACT_CHANGE_NUMBER: \n");
 			out.write("COIN_VALUES: "+formatDenoms(exactChangeSolution.denoms) + "\n");
 			
 			//Iterate through all values 
 			for(int i =1 ; i < 100 ; i++){
 				out.write(i+": "+formatDenoms(ExactChange.getCoins(exactChangeSolution.exactCounts, exactChangeSolution.denoms, i))+"\n");
 			}
 			
 			//Print EXCHANGE_NUMBER
 			out.write("EXCHANGE_NUMBER: \n");
 			out.write("COIN_VALUES: "+formatDenoms(exchangeSolution.denoms) + "\n");
 			
			for( int i=1; i<100; i++ ) {
				out.write(i + ": " + formatCoinsString(exchangeSolution.exchangeCounts, exchangeSolution.exactCounts, exchangeSolution.denoms, i) );
				out.write("\n");
			}
 			
 			out.close();
 		}catch (Exception e){ 
 			e.printStackTrace(System.out);
 		}
 		System.out.println("Done in ~" + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
 	}
 	
 	public static String formatDenoms(List<Integer> denoms) {
				
		String retVal = "";
 		
		for(int i = 0 ; i<denoms.size();i++){
 
			if(denoms.get(i) != null){
				
				if(i != denoms.size() -1) // dont put , for the last value
					retVal += denoms.get(i) + ",";
				else
					retVal += denoms.get(i);
			}
 		}
 		
 		return retVal;
 		
 	}
 	
 	public static String formatCoinsString(List<Integer> counts, List<Integer> exactCounts, List<Integer> denoms, int sum) {
 		
 		StringBuilder sb = new StringBuilder();
 		
 		for( int p=0; p<exactCounts.size(); p++ ) {   // iterate over p(Pay) values
 			int minChange = p - sum + ( p - sum < 0 ? 100 : 0 );
 			int minChangeCount = exactCounts.get(minChange);
 
 			int c2 = minChange + 100;
 			if( c2 < exactCounts.size() && exactCounts.get(c2) < minChangeCount ) {
 				minChangeCount = exactCounts.get(c2);
 				minChange = c2;
 			}
 			
 			if( counts.get(sum) == exactCounts.get(p) + minChangeCount ) {
 				List<Integer> pCoins = ExactChange.getCoins(exactCounts, denoms, p);
 				List<Integer> cCoins = ExactChange.getCoins(exactCounts, denoms, minChange);
 				sb.append(ExactChange.coinsToString(pCoins));
 				sb.append(";");
 				sb.append(ExactChange.coinsToString(cCoins));
 				return sb.toString();
 			}
 		}
 		return "";
 	}
 
 }

 package life;
 
 public class JeuDeLaVie {
 	private static void help(){
 		String[] msg = {
 			"Usage: [-name -h] [-s -c -w] TURN FILE",
 			"-name:      Display authors name",
 			"-h:         Display this message"
 		};
 		for(String line: msg) System.out.println(line);
 	}
 	
 	private static void name(){
 		String[] msg = {
 			"Ibliis",
 			"iTzamma",
 			"ludovic coues",
 			"startrockque"
 		};
 		for(String line: msg) System.out.println(line);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		if(args.length == 0){
			help();
			System.exit(1);
		}
 		switch(args[0]){
 		case "-name": name(); break;
 		default: help();
 		}
 	}
 
 }

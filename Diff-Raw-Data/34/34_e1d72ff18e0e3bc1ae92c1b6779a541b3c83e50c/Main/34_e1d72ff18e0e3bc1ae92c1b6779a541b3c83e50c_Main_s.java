 package tacc;
 
 public class Main {
 
     public static void main(String[] args)
     {
 		boolean debug = false;
 		boolean dump  = false;
 
 		for (int i=1; i<args.length; i++) {
 			switch (args[i].charAt(1)) {
 				case 's':
 					debug = true;
 					break;
 
 				case 'd':
 					dump = true;
 					break;
 				default:
 					Main.wrongUse();
 			}
 		}
 
		Machine tvm = new Machine();
 
 		if (!tvm.loadScript(args[0])) {
 			System.out.println("Non è stato possibile aprire/processare il file " + args[0]);
 			System.exit(1);
 		}
 
		if (debug) tvm.enableDebug();

 		System.out.println("Avvio della macchina TACC..\n");
 		tvm.run();
 		System.out.println("\nElaborazione terminata! La macchina ha restituito " + tvm.getResponse() + ".");
 
 		if (dump) {
 			System.out.println("\nDump delle variabili:");
 			tvm.dumpMemory();
 		}
     }
 
 	public static void wrongUse()
 	{
 		System.out.println("USO: ./tacc script.t [-s] [-d]");
 		System.out.println(" -s: mostra i passi di esecuzione");
 		System.out.println(" -d: esegue il dump delle variabili al termine");
 		System.exit(1);
 	}
 
 }

 package v6.apps.clients.backup.db;
 
 import java.io.Console;
 
 public class DbBackuperClient {
 
 	private static class ArgException extends Exception {
 
 		private static final long serialVersionUID = -5633760898161113093L;
 
 		public ArgException(String m) {
 			super(m);
 		}
 
 	}
 
 	public static void main(String[] args) {
 		if (args.length == 0) {
 			help();
 		} else {
 			// printlns(args);
 			String url = null;
 			String pwd = null;
 			boolean data = true, def = true;
 			try {
 				for (int i = 0; i < args.length; i++) {
 					if (args[i].equals("-url")) {
 						if (url != null) {
 							throw new ArgException("Duplicitni -url na pozici "
 									+ (i + 1));
 						}
 						try {
 							url = args[i + 1];
 							i++;
 						} catch (ArrayIndexOutOfBoundsException e) {
 							throw new ArgException(
 									"Ocekavana url, nalezen konec");
 						}
 					} else if (args[i].equals("-pwd")) {
 						if (pwd != null) {
 							throw new ArgException("Duplicitni -pwd na pozici "
 									+ (i + 1));
 						}
 						try {
 							pwd = args[i + 1];
 							i++;
 						} catch (ArrayIndexOutOfBoundsException e) {
 							throw new ArgException(
 									"Ocekavano heslo, nalezen konec");
 						}
 					} else if (args[i].equals("-nodata")) {
 						if (!data) {
 							throw new ArgException(
 									"Duplicitni -nodata na pozici " + (i + 1));
 						}
 						data = false;
 					} else if (args[i].equals("-nodef")) {
 						if (!def) {
							throw new ArgException(
									"Duplicitni -nodef na pozici " + (i + 1));
 						}
 						def = false;
 					} else {
 						throw new ArgException("Neznamy argument na pozici "
 								+ (i + 1) + ": " + args[i]);
 					}
 				}
 				if (url == null) {
 					throw new ArgException("Argument -url vyzadovan!");
 				}
 				if (pwd == null) {
 					Console c = System.console();
 					if (c == null) {
 						throw new ArgException(
 								"Argument -pwd pri vypnutem konzolovem vstupu vyzadovan!");
 					}
 					System.out.print("Zadejte heslo: ");
 					pwd = new String(c.readPassword());
 					System.out.println();
 				}
 				new Thread(new DbBackuper(url, pwd).setBackupTableData(data)
 						.setBackupTableDefinition(def)).start();
 			} catch (ArgException e) {
 				System.err.println("Chyba v argumentech: " + e.getMessage());
 				System.exit(1);
 			}
 		}
 	}
 
 	static void printlns(String... lns) {
 		for (int i = 0; i < lns.length; i++) {
 			System.out.println(lns[i]);
 		}
 	}
 
 	static void help() {
 		printlns(
 				"Zalohovac databaze - klient",
 				"Zalozni soubory uklada do adresare RRRR-MM-DD--hh-mm-ss",
 				"",
 				"bez parametru vypise napovedu",
 				"... [-nodata] [-nodef] [-pwd <password>] -url <url>",
 				"",
 				"-nodata\t\t Nezalohuje data tabulky.",
 				"-nodef\t\t Nezalohuje definici tabulky.",
 				"-url <url>\t Urcuje zaklad url serveru, ze ktereho se ma zalohovat.",
 				"-pwd <pwd>\t Urcuje heslo k serveru. Je-li vynechano, aplikace se pokusi",
 				"\t\t zeptat uzivatele. Nepodari-li se, skonci to chybou.");
 	}
 
 }

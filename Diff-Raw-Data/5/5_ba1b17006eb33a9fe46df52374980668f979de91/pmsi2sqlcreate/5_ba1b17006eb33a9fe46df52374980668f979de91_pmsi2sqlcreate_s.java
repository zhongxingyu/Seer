 package org.aider.pmsi2sql;
 
 import java.io.StringReader;
 import java.sql.Connection;
 
 import org.aider.pmsi2sql.linetypes.PmsiInsertion;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 /**
  * Classe principale du programme pmsi2sqlcreate qui permet de crer les
  * tables pour utiliser l'application pmsi2sql
  * @author delabre
  *
  */
 public class pmsi2sqlcreate {
 
 	/**
 	 * Fonction principale du programme pmsi2sqlcreate
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		
 		// Dfinition des arguments fournis au programme
 		Pmsi2SqlBaseOptions options = new Pmsi2SqlBaseOptions();
         CmdLineParser parser = new CmdLineParser(options);
 
         // Tentative de lecture de la ligne de commande
         try {
         	parser.parseArgument(args);
         } catch (CmdLineException e) {
         	// Au moins un argument requis est manquant
         	// Si on a appel l'aide ou le numro de version, il est normal qu'un argument manque,
         	// Sinon on affiche le message indiquant les arguments possibles ainsi que l'argument
         	// manquant
         	if(!options.isHelp() && !options.isVersion()) {
         		parser.setUsageWidth(80);
         		parser.printUsage(System.out);
         		System.out.println(e.getMessage());
         		// Sortie de la fonction principale du programme
         		return;
             }
         }
 
         // La ligne de commande a bien t lue, ou bien mal mais parceque l'argument 'help' ou
         // 'version' a t indiqu
     	if(options.isHelp()){
     		// L'affichage de l'aide a t demande
     		parser.setUsageWidth(80);
             parser.printUsage(System.out);
         } else if (options.isVersion()){
         	// L'affichage de la version a t demand
     		parser.setUsageWidth(80);
             System.out.println("Version : test");
         }
 
     	// Connexion  la base de donnes selon les arguments de la ligne de commande
        	Connection myConn = options.getNewSqlConnection();
        	
        	// Cration de la table qui trace les tentatives d'insertion de fichiers pmsi
     	PmsiInsertion myInsertionTable = new PmsiInsertion("");
     	myConn.createStatement().execute(myInsertionTable.getSQLTable());
     	myConn.createStatement().execute(myInsertionTable.getSQLIndex());
     	myConn.createStatement().execute(myInsertionTable.getSQLFK());
     	myConn.commit();
     	
     	// Cration des tables permettant de stocker les RSS
     	PmsiRSSReader r = new PmsiRSSReader(new StringReader(""), options.getNewSqlConnection());
     	r.createTables();
     	r.createIndexes();
     	r.createKF();
     	r.commit();
 
     	// Cration des tables permettant de stocker les RSF
 		PmsiRSFReader f = new PmsiRSFReader(new StringReader(""), options.getNewSqlConnection());
 		f.createTables();
 		f.createIndexes();
 		f.createKF();
 		f.commit();
 
 		//Fermeture de la connexion  la base de donnes
     	myConn.close();
 
     	// Affichage de la russite du programme
         System.out.println("pmsi2sqlcreate successfully finished!");
 	}
 }

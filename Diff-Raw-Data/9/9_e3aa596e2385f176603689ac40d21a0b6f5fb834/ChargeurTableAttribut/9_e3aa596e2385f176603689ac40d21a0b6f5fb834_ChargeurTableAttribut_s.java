 package cinematequexmltobd;
 /**
  *ChargeurTableElement.java
  *
  * Marc Frappier
  * Université de Sherbrooke
  *
  *Permet de mettre à jour une BD relationnelle à partir d'un document XML.
  *La racine du document XML (niveau 0) peut contenir n'importe quel nom.
  *Chaque sous-élément (niveau 1) de la racine correspond à une table.
  *Le nom d'un sous-élément représente le nom de la table.
  *Les sous-elements (niveau 2) d'un sous-élément (niveau 1)
  *représentent les attributs de la table.
  *Les valeurs des sous-éléments de niveau 2 représentent
  *les valeurs des attributs d'un enregistrement de la table.
  *Chaque sous-élément de niveau 1 entraîne une
  *insertion d'un enregistrement dans la table
  *correspondant au sous-élément.
  *Les attributs d'un élément sont ignorés.
  *Paramètres:0- nom du document XML
  *           1- serveur
  *           2- user id
  *           3- password
  *           4- validation du fichier XML (optionnel, valeur = -v)
  */
 
 import java.sql.*;
 import java.io.*;
 import org.xml.sax.*;
 import org.xml.sax.helpers.DefaultHandler;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
import tp3.Connexion;
 
 public class ChargeurTableAttribut extends DefaultHandler {
 
 	private static Connexion conn;
 
 	private static Statement stmt;
 
 	private static int niveau = -1;
 
 	public static void main(String argv[]) throws ParserConfigurationException,
 			SAXException, IOException, SQLException {
 		if (argv.length < 5) {
 			System.err
 					.println("Usage: ChargeurTableAttribut <fichier> <serveur> <bd> <userid> <pw> [-v]");
 			System.exit(1);
 		}
 
 		DefaultHandler handler = new ChargeurTableAttribut();
 		// DefaultHandler handler = new DefaultHandler();
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		if (argv.length == 6 && argv[5].equals("-v"))
 			factory.setValidating(true);
 		SAXParser saxParser = factory.newSAXParser();
 		conn = new Connexion(argv[1], argv[2], argv[3], argv[4]);
 		conn.setAutoCommit(true);
 		stmt = (Statement)conn.getConnection();
 		saxParser.parse(new File(argv[0]), handler);
 		conn.fermer();
 	}
 
 	/**
 	 * Cette m�thode est appel�e par le parser suite � la lecture du marqueur de
 	 * d�but d'un �l�ment.
 	 */
 	public void startElement(String namespaceURI, String lName, String qName,
 			Attributes attrs) throws SAXException {
 		niveau = niveau + 1;
 		if (niveau == 1 && attrs.getLength() > 0) // sous-�l�ment de la racine
 			try {
 				DefinitionTable defTable = new DefinitionTable(conn
 						.getConnection(), qName);
 				String enonceInsert = formatterInsert(qName, defTable, attrs);
 				stmt.executeUpdate(enonceInsert);
 			} catch (Exception e) {
 				System.out.println(e);
 			}
 	}
 
 	/**
 	 * Cette m�thode formatte un �nonc� SQL insert pour ins�rer un
 	 * enregistrement dans la table.
 	 */
 	private static String formatterInsert(String nomTable,
 			DefinitionTable defTable, Attributes attrs) {
 		StringBuffer enonceInsert = new StringBuffer("insert into " + nomTable
 				+ " (");
 		enonceInsert.append(attrs.getQName(0));
 		for (int i = 1; i < attrs.getLength(); i++)
 			enonceInsert.append("," + attrs.getQName(i));
 		enonceInsert.append(") values (");
 		enonceInsert.append(formatterValeur(attrs.getQName(0), attrs
 				.getValue(0), defTable));
 		for (int i = 1; i < attrs.getLength(); i++)
 			enonceInsert.append(","
 					+ formatterValeur(attrs.getQName(i), attrs.getValue(i),
 							defTable));
 		enonceInsert.append(")");
 		return enonceInsert.toString();
 	}
 
 	/**
 	 * Cette m�thode formatte la valeur d'un attribut dans l'�nonc� insert. Elle
 	 * utilise la d�finition de la table pour obtenir le type de l'attribut.
 	 */
 	private static String formatterValeur(String nomAttribut,
 			String valeurAttribut, DefinitionTable defTable) {
 		int typeAttribut = defTable.getTypeAttribut(nomAttribut);
 		if (typeAttribut == Constantes.CHAINE)
 			return "'" + valeurAttribut + "'";
 		else if (typeAttribut == Constantes.NOMBRE)
 			return valeurAttribut;
 		else if (typeAttribut == Constantes.DATE)
 			return "to_date('" + valeurAttribut + "','YYYY-MM-DD')";
 		else
 			return "";
 	}
 
 	/**
 	 * Cette m�thode est appel�e par le parser suite � la lecture du marqueur de
 	 * la fin d'un �l�ment.
 	 */
 	public void endElement(String namespaceURI, String lName, String qName)
 			throws SAXException {
 		niveau = niveau - 1;
 	}
 
 	/**
 	 * Cette m�thode est appel�e par le parser pour les avertissements
 	 */
 	public void warning(SAXParseException e) throws SAXException {
 		System.out.println("Warning : " + e);
 	}
 
 	/**
 	 * Cette m�thode est appel�e par le parser pour les erreur non fatales
 	 */
 	public void error(SAXParseException e) throws SAXException {
 		System.out.println("Error : " + e);
 	}
 
 	/**
 	 * Cette m�thode est appel�e par le parser pour les erreur fatales
 	 */
 	public void fatalError(SAXParseException e) throws SAXException {
 		System.out.println("Fatal error : " + e);
 	}
 }

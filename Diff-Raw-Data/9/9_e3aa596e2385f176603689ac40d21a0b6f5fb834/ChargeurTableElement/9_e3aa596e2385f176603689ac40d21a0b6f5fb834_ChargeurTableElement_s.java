 package cinematequexmltobd;
 /**
  *ChargeurTableElement.java
  *
  * Marc Frappier
  * Universit� de Sherbrooke
  *
  *Permet de mettre � jour une BD relationnelle � partir d'un document XML.
  *La racine du document XML (niveau 0) peut contenir n'importe quel nom.
  *Chaque sous-�l�ment (niveau 1) de la racine correspond � une table.
  *Le nom d'un sous-�l�ment repr�sente le nom de la table.
  *Les sous-elements (niveau 2) d'un sous-�l�ment (niveau 1)
  *repr�sentent les attributs de la table.
  *Les valeurs des sous-�l�ments de niveau 2 repr�sentent
  *les valeurs des attributs d'un enregistrement de la table.
  *Chaque sous-�l�ment de niveau 1 entra�ne une
  *insertion d'un enregistrement dans la table
  *correspondant au sous-�l�ment.
  *Les attributs d'un �l�ment sont ignor�s.
  *Param�tres:0- nom du document XML
  *           1- serveur
  *           2- user id
  *           3- password
  *           4- validation du fichier XML (optionnel, valeur = -v)
  */
 
 import java.io.*;
 import java.sql.*;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.*;
 import org.xml.sax.helpers.DefaultHandler;
import tp3.Connexion;
 
 public class ChargeurTableElement extends DefaultHandler {
 	private Connexion conn;
 
 	private String serveur, bd, user, pass;
 
 	private DefinitionTable defTable;
 
 	private Statement stmt;
 
 	private StringBuffer valeurAttributCourant, clauseAttributs, clauseValeurs;
 
 	private int niveau;
 
 	public static void main(String argv[]) throws Exception,
 			ParserConfigurationException, SAXException, IOException,
 			SQLException {
 		if (argv.length < 5) {
 			System.err
 					.println("Usage: ChargeurTableElement <fichier> <serveur> <userid> <pw> [-v]");
 			System.exit(1);
 		}
 
 		DefaultHandler handler = new ChargeurTableElement(argv[1], argv[2],
 				argv[3], argv[4]);
 		// DefaultHandler handler = new DefaultHandler();
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		if (argv.length == 6 && argv[5].equals("-v"))
 			factory.setValidating(true);
 		SAXParser saxParser = factory.newSAXParser();
 		saxParser.parse(new File(argv[0]), handler);
 	}
 
 	public ChargeurTableElement(String serveur, String bd, String user,
 			String pass) {
 		this.serveur = serveur;
 		this.bd = bd;
 		this.user = user;
 		this.pass = pass;
 	}
 
 	public void startDocument() throws SAXException {
 		try {
 			conn = new Connexion(serveur, bd, user, pass);
 			conn.setAutoCommit(true);
 			stmt = (Statement)conn.getConnection();
 			niveau = -1;
 		} catch (Exception e) {
 			throw new SAXException(e);
 		}
 	}
 
 	public void endDocument() throws SAXException {
 		try {
 			conn.fermer();
 		} catch (Exception e) {
 			throw new SAXException(e);
 		}
 	}
 
 	/**
 	 * Cette m�thode est appel�e par le parser suite � la lecture du marqueur de
 	 * d�but d'un �l�ment.
 	 */
 	public void startElement(String namespaceURI, String lName, String qName,
 			Attributes attrs) throws SAXException {
 		niveau = niveau + 1;
 		if (niveau == 1) // sous-�l�ment de la racine
 		{
 			try {
 				defTable = new DefinitionTable(conn.getConnection(), qName);
 				clauseAttributs = new StringBuffer();
 				clauseValeurs = new StringBuffer();
 			} catch (Exception e) {
 				System.out.println(e);
 			}
 		} else if (niveau == 2) {
 			clauseAttributs.append("," + qName);
 			valeurAttributCourant = new StringBuffer();
 		}
 	}
 
 	/**
 	 * Cette m�thode est appel�e par le parser suite � la lecture du marqueur de
 	 * la fin d'un �l�ment.
 	 */
 	public void endElement(String namespaceURI, String lName, String qName)
 			throws SAXException {
 		if (niveau == 1 && clauseAttributs.length() > 0) // sous-�l�ment de
 															// la
 		// racine
 		{
 			String enonceInsert = "insert into " + qName + " ("
 					+ clauseAttributs.substring(1) + ") values ("
 					+ clauseValeurs.substring(1) + ")";
 
 			try {
 				stmt.executeUpdate(enonceInsert);
 			} catch (Exception e) {
 				System.out.println(e);
 			}
 		} else if (niveau == 2) {
 			clauseValeurs.append(","
 					+ formatterValeur(qName, valeurAttributCourant.toString()));
 		}
 		niveau = niveau - 1;
 	}
 
 	/**
 	 * Cette m�thode est appel�e par le parser suite � la lecture des caract�res
 	 * entre le marqueur de d�but et le marqueur de fin d'un �l�ment
 	 */
 	public void characters(char buf[], int offset, int len) throws SAXException {
 		if (niveau == 2)
 			valeurAttributCourant.append(buf, offset, len);
 	}
 
 	/**
 	 * Cette m�thode formatte la valeur d'un attribut dans l'�nonc� insert. Elle
 	 * utilise la d�finition de la table pour obtenir le type de l'attribut.
 	 */
 	private String formatterValeur(String nomAttribut, String valeurAttribut) {
 		int typeAttribut = defTable.getTypeAttribut(nomAttribut);
 		if (typeAttribut == Constantes.CHAINE)
 			return "'" + valeurAttribut + "'";
 		else if (typeAttribut == Constantes.NOMBRE)
 			return valeurAttribut;
 		else if (typeAttribut == Constantes.DATE)
 			return "to_date('" + valeurAttribut + "','YYYY-MM-DD')";
 		else
 			return null;
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
 		try {
 			conn.fermer();
 		} catch (Exception e1) {
 			throw new SAXException(e1);
 		}
 	}
 }

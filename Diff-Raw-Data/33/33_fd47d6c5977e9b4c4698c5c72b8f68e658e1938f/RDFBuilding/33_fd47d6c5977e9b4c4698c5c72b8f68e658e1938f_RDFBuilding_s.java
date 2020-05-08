 package models;
 
 import java.io.*;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Properties;
 
 import org.h2.command.ddl.CreateRole;
 import org.openjena.riot.RiotException;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.sparql.vocabulary.FOAF;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.vocabulary.DC;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 import controllers.Application;
 
 @SuppressWarnings("deprecation")
 public class RDFBuilding {
 	private static volatile RDFBuilding instance = null;
 	private static final String rdf_file = "public/rdf/upyourmood.rdf";
 	private static Model m=null;
 	public static int cpt = 0;
	private final static String cfgProp = "app/models/compteur.properties";
	private final static Properties configFile = new Properties() {
		private final static long serialVersionUID = 1L; {
			try {
				load(new FileInputStream(cfgProp));
			} catch (Exception e) {}
		}
	};
	
 	private RDFBuilding(){
 		try{
 			m=FileManager.get().loadModel(rdf_file);
 		}catch(RiotException e){
 			m = ModelFactory.createDefaultModel();
 		}
		cpt=Integer.parseInt(configFile.getProperty("compteur"));
 	}
 
 	public final static RDFBuilding getInstance() {
 		if (RDFBuilding.instance == null) {
 			synchronized(RDFBuilding.class) {
 				if (RDFBuilding.instance == null) {
 					RDFBuilding.instance = new RDFBuilding();
 					m.setNsPrefix("uym", OntologyUpYourMood.getUym());
 					m.setNsPrefix("music", OntologyUpYourMood.getUymMusic());
 					m.setNsPrefix("wordconnotation", OntologyUpYourMood.getUymWordConnotation());
 					m.setNsPrefix("user", OntologyUpYourMood.getUymUser());
 					m.setNsPrefix("rdf", RDF.getURI());
 					m.setNsPrefix("rdfs", RDFS.getURI());
 					m.setNsPrefix("foaf", FOAF.getURI());
 					m.setNsPrefix("dc", DC.getURI());
 					m.setNsPrefix("nicetag", NiceTag.getURI());
 					m.add(OntologyUpYourMood.albumTitle, RDFS.subPropertyOf, DC.title);
 					m.add(NiceTag.makesMeFeel,RDFS.subPropertyOf,NiceTag.isRelatedTo);
 					m.add(OntologyUpYourMood.songTitle, RDFS.subPropertyOf, DC.title);
 					m.add(OntologyUpYourMood.hasListen,RDFS.subPropertyOf,FOAF.knows);
 
 				}
 			}
 		}
 		return RDFBuilding.instance;
 	}
 
 	/**
 	 * fonction principale qu'il faudra appeler dans la classe Application
 	 * @param List<String> infoMusic information de la musique courante
 	 */
 	public void rdfUpYourMood(List<String> infoMusic,UserInformation userInfo,WordConnotation word){
 		ajouterMusique(infoMusic);
 		ajouterUtilisateur(userInfo, infoMusic, word);
 		//ajouterMot_Connotation(word);
 
 		//............................................................................................
		
 		//............................................................................................
 		FileOutputStream outStream;
 		try {
 			outStream = new FileOutputStream("public/rdf/upyourmood.rdf");
 			m.write(outStream, "RDF/XML-ABBREV");
 			outStream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Dans cette fonction on ajoute:
 	 * <ul>
 	 * 	<li>l'identifiant de la musique</li>
 	 * 	<li>titre de la musique</li>
 	 * 	<li>titre de l'album</li>
 	 * 	<li>nom de l'artiste</li>
 	 * 	<li>url de la photo de l'album</li>
 	 * </ul>
 	 */
 	private void ajouterMusique(List<String> infoMusic){
 
 		OntologyUpYourMood.Music = m.createResource(OntologyUpYourMood.getUymMusic()+infoMusic.get(0));
 		OntologyUpYourMood.Album = m.createResource(OntologyUpYourMood.getUymMusic()+infoMusic.get(1));
 		OntologyUpYourMood.Artist = m.createResource(OntologyUpYourMood.getUymMusic()+infoMusic.get(2));
 		OntologyUpYourMood.AlbumCover = m.createResource(infoMusic.get(3));
 		OntologyUpYourMood.Title= m.createResource(OntologyUpYourMood.getUymMusic()+infoMusic.get(4));
 
 		m.add(OntologyUpYourMood.Music,OntologyUpYourMood.albumTitle,OntologyUpYourMood.Album);
 		m.add(OntologyUpYourMood.Music,DC.creator,OntologyUpYourMood.Artist);
 		m.add(OntologyUpYourMood.Music, FOAF.depiction,OntologyUpYourMood.AlbumCover);
 		m.add(OntologyUpYourMood.Music,OntologyUpYourMood.songTitle,OntologyUpYourMood.Title);
 
 	}
 
 	/**
 	 * Dans cette fonction on ajoute:
 	 * a minima:
 	 * <ul>
 	 * 	<li>le pseudo</li>
 	 * </ul>
 	 */
 	private void ajouterUtilisateur(UserInformation userInfo, List<String> infoMusic, WordConnotation word ){
 
 		try {
 			userInfo.retrieveInformation(Application.maSession.getPseudo());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
		
 		OntologyUpYourMood.MusicalExperience=m.createResource(OntologyUpYourMood.getUymUser()+userInfo.getInfoUser().pseudo+"/"+"musicalExperience"+cpt);
 		OntologyUpYourMood.User = m.createResource(OntologyUpYourMood.getUymUser()+userInfo.getInfoUser().pseudo);
 		OntologyUpYourMood.MusicalExperience.addProperty(NiceTag.makesMeFeel,
 				m.createResource()
 				.addProperty(OntologyUpYourMood.isAssociatedBy, word.getMot())
 				.addProperty(OntologyUpYourMood.isConnoted, String.valueOf(word.getConnotation())));
 		OntologyUpYourMood.MusicalExperience.addProperty(OntologyUpYourMood.hasListen, infoMusic.get(0));
 		m.add(OntologyUpYourMood.User,OntologyUpYourMood.hasMusicalExperience,OntologyUpYourMood.MusicalExperience);
 		cpt++;
 		try {
 			FileOutputStream fichier=new FileOutputStream(cfgProp);
 			String compteur="compteur "+cpt;
 			fichier.write(compteur.getBytes());
 			fichier.flush();
 			fichier.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/*private void ajouterMot_Connotation(WordConnotation word){
 
 		OntologyUpYourMood.Word = m.createResource(OntologyUpYourMood.getUymWordConnotation()+word.getMot());
 		OntologyUpYourMood.connotation=m.createTypedLiteral(word.getConnotation(),XSDDatatype.XSDfloat);
 
 		OntologyUpYourMood.IsConnoted = m.createProperty(OntologyUpYourMood.getUymWordConnotation()+"IsConnoted");
 		OntologyUpYourMood.IsAssociatedBy = m.createProperty(OntologyUpYourMood.getUymWordConnotation()+"IsAssociatedBy");
 
 		OntologyUpYourMood.Word.addLiteral(OntologyUpYourMood.IsConnoted, OntologyUpYourMood.connotation);
 
 		m.add(OntologyUpYourMood.Word,RDF.type,OntologyUpYourMood.Word);
 
 		m.add(OntologyUpYourMood.Word,RDF.type,OntologyUpYourMood.IsConnoted);	
 		OntologyUpYourMood.User.addProperty(OntologyUpYourMood.IsAssociatedBy, OntologyUpYourMood.Word);
 		//m.add(OntologyUpYourMood.User,OntologyUpYourMood.IsAssociatedBy,OntologyUpYourMood.Word);
 
 	}*/
 }

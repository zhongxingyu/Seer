 package edu.uib.info310.search.builder;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 
 import edu.uib.info310.model.Artist;
 import edu.uib.info310.model.imp.ArtistImp;
 
 import edu.uib.info310.search.DiscogSearch;
 
 import edu.uib.info310.search.LastFMSearch;
 import edu.uib.info310.transformation.XslTransformer;
 import edu.uib.info310.util.GetArtistInfo;
 
 @Component
 public class OntologyBuilder {
 	
 	
 	private LastFMSearch search = new LastFMSearch();
 	
 	
 	private XslTransformer transformer = new XslTransformer();
 
 	private static final String SIMILAR_XSL = "src/main/resources/XSL/SimilarArtistLastFM.xsl";
 	private static final String ARTIST_EVENTS_XSL = "src/main/resources/XSL/Events.xsl";
 	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyBuilder.class);
 	private DiscogSearch disc = new DiscogSearch();
 	public Model createArtistOntology(String search_string) {
 		Model model = ModelFactory.createDefaultModel();
 		Model discs = disc.getDiscography(search_string); 
 		LOGGER.debug("Number of items in discography: " + discs.size());
 		model.add(discs);
 		LOGGER.debug("Model size after getting artist discography: " + model.size());
 		try{
 			transformer.setXml(search.getSimilarArtist(search_string));
 			transformer.setXsl(new File(SIMILAR_XSL));
 
 			InputStream in = new ByteArrayInputStream(transformer.transform().toByteArray());
 			model.read(in, null);
 			LOGGER.debug("Model size after getting similar artists: " + model.size());
 			transformer.setXml(search.getArtistEvents(search_string));
 			transformer.setXsl(new File(ARTIST_EVENTS_XSL));
 
 			in = new ByteArrayInputStream(transformer.transform().toByteArray());
 			model.read(in, null);
 			LOGGER.debug("Model size after getting artist events: " + model.size());
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// get BBC_MUSIC model and add to model
 		model.add(GetArtistInfo.ArtistInfo(search_string));
 
 
 		return model;
 	}
 
 }

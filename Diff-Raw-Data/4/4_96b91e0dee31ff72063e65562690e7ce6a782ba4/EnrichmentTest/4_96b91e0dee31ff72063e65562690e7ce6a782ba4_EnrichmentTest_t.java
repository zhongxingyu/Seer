 package eu.europeana.enrichment;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.solr.common.SolrInputDocument;
 import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
 import org.codehaus.plexus.logging.console.ConsoleLogger;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import de.flapdoodle.embed.mongo.MongodExecutable;
 import de.flapdoodle.embed.mongo.MongodStarter;
 import de.flapdoodle.embed.mongo.config.MongodConfig;
 import de.flapdoodle.embed.mongo.distribution.Version;
 import eu.annocultor.converters.europeana.Entity;
 import eu.europeana.uim.enrichment.utils.EuropeanaEnrichmentTagger;
 
 
 public class EnrichmentTest {
 	public final static String ANNOCULTOR_DB = "src/test/resources/annocultor_db"; 
 	@Test
 	public void testAnnocultorEnrichment(){
 		int port = 10000;
 		try {
 			if(!new File(ANNOCULTOR_DB+"/annocultor_db.3").exists()){
 				final TarGZipUnArchiver ua = new TarGZipUnArchiver();
 				ua.setSourceFile(new File(ANNOCULTOR_DB+"/annocultor_db.tar.gz"));
 				ua.enableLogging(new ConsoleLogger(0,"test"));
 				ua.extract();
 			}
 			
 			MongodConfig conf = new MongodConfig(Version.V2_0_7, 10000,
 					false,ANNOCULTOR_DB);
 			MongodStarter runtime = MongodStarter.getDefaultInstance();
 			MongodExecutable mongoExec = runtime.prepare(conf);
 			mongoExec.start();
 			
 			EuropeanaEnrichmentTagger tagger = new EuropeanaEnrichmentTagger();
 			tagger.init("Europeana","localhost",Integer.toString(port));
 			SolrInputDocument doc = new SolrInputDocument();
 			doc.addField("proxy_dc_subject", "paper");
 			doc.addField("proxy_dcterms_temporal", "1928");
 			doc.addField("proxy_dc_coverage","paris");
 			doc.addField("proxy_dc_creator","rembrandt");
 			List<Entity> entities = tagger.tagDocument(doc);
 			Assert.assertEquals(7, entities.size());
 			mongoExec.stop();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 
 }

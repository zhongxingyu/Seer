 package uk.ac.sanger.artemis.components.variant;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.genedb.crawl.model.MappedVCFRecord;
 import org.genedb.crawl.model.Sequence;
 
 public class BCFReaderAdapter extends VariantReaderAdapter {
 	
 	private Logger logger = Logger.getLogger(BCFReaderAdapter.class);
 	
 	private BCFReader reader;
 	
 	public BCFReaderAdapter(String url) throws IOException {
 		reader = new BCFReader(url);
 		String hdr = reader.headerToString();
 		logger.info(hdr);
 	    if(hdr.indexOf("VCFv4") > -1) {
 	    	reader.setVcf_v4(true);
 	    }
 		abstractReader = reader;
 	}
 	
 	
 	@Override
 	public List<VCFRecord> unFilteredQuery(String region, int start, int end)
 			throws IOException {
 		
 		List<VCFRecord> records = super.unFilteredQuery(region, start,end);
 		
 		if (records.size() == 1) {
 			logger.warn("running second time, suspicious size of 1.");
 			records = super.unFilteredQuery(region, start,end);
 		}
 		
 		return records;
 	}
 	
 	@Override
 	public List<MappedVCFRecord> query(
 			String region, 
 			int start, 
 			int end, 
 			List<GeneFeature> genes, 
 			VariantFilterOptions options, 
 			Sequence regionSequence) throws IOException {
 		
 		List<MappedVCFRecord> records = super.query(region, start, end, genes, options, regionSequence);
 		
		if (records.size() <= 1) {
			logger.warn("running second time, suspicious size of <= 1.");
 			records = super.query(region, start, end, genes, options, regionSequence);
 		}
 		
 		return records;
 	          
 	}
 	
 	
 	
 	
 	
 
 
 	
 	
 }

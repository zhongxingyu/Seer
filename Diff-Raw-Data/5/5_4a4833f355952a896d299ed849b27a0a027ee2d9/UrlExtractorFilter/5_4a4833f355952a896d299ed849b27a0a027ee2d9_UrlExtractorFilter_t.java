 package org.paxle.data.db.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterContext;
 import org.paxle.core.queue.ICommand;
 
 public class UrlExtractorFilter implements IFilter<ICommand> {
 	private CommandDB db;
 	
 	private Log logger = LogFactory.getLog(this.getClass());
 
 	public UrlExtractorFilter(CommandDB db) {
 		this.db = db;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void filter(ICommand command, IFilterContext context) {
 		if (command == null) return;
 
 		// getting the parser-doc
 		IParserDocument parserDoc = command.getParserDocument();
 		if (parserDoc == null) return;
 
 		// getting the link map
 		this.extractLinks(command.getLocation(), parserDoc);
 	}
 	
 	private void extractLinks(final String location, IParserDocument parserDoc) {
 		if (parserDoc == null) return;
 		
 		// getting the link map
 		Map<String, String> linkMap = parserDoc.getLinks();
 		if (linkMap != null) {
 			this.extractLinks(location, linkMap);
 		}
 		
 		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
 		if (subDocs != null) {
 			for (IParserDocument subDoc : subDocs.values()) {
 				this.extractLinks(location, subDoc);
 			}
 		}
 	}
 	
 	private void extractLinks(final String location, Map<String, String> linkMap) {
 		List<String> locations = new ArrayList<String>();
 		
 		for (String ref : (Set<String>) linkMap.keySet()) {
 			// do some url normalization here
 			// TODO: this should be done in another filter
 			int idx = ref.indexOf("#");
 			if (idx != -1) ref = ref.substring(0,idx);
 			
			if (ref.length() > 512) {
				this.logger.debug("Skipping too long URL: " + ref);
				continue;
			}
			
 			// add command into list
 			locations.add(ref);
 		}
 
 		// store commands into DB
 		if (!db.isClosed()) {
 			db.storeUnknownLocations(locations);
 		} else {
 			this.logger.error(String.format(
 					"Unable to write linkmap of location '%s' to db. Database already closed.",
 					location
 			));
 		}
 	}
 
 }

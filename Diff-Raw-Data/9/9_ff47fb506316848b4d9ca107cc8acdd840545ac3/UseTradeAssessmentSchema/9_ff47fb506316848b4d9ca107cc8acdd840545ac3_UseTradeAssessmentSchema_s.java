 package org.iucn.sis.server.schemas.usetrade;
 
import org.iucn.sis.server.api.schema.AssessmentSchema;
 import org.iucn.sis.server.schemas.usetrade.docs.DocumentLoader;
 import org.w3c.dom.Document;
 
public class UseTradeAssessmentSchema implements AssessmentSchema {
 
 	@Override
 	public String getDescription() {
 		return "Assessments for Use and Trade.";
 	}
 
 	@Override
 	public Document getField(String fieldName) {
		return DocumentLoader.getField(fieldName);
 	}
 	
 	@Override
 	public String getTablePrefix() {
 		return "UT";
 	}
 
 	@Override
 	public String getName() {
 		return "Use/Trade";
 	}
 
 	@Override
 	public Document getViews() {
 		return DocumentLoader.getView();
 	}
 
 }

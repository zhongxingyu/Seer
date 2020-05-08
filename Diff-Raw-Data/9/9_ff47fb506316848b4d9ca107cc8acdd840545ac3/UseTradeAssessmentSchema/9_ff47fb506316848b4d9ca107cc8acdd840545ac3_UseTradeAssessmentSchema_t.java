 package org.iucn.sis.server.schemas.usetrade;
 
import org.iucn.sis.server.api.schema.redlist.RedListAssessmentSchema;
 import org.iucn.sis.server.schemas.usetrade.docs.DocumentLoader;
 import org.w3c.dom.Document;
 
public class UseTradeAssessmentSchema extends RedListAssessmentSchema {
 
 	@Override
 	public String getDescription() {
 		return "Assessments for Use and Trade.";
 	}
 
 	@Override
 	public Document getField(String fieldName) {
		Document field = DocumentLoader.getField(fieldName);
		return field != null ? field : super.getField(fieldName);
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

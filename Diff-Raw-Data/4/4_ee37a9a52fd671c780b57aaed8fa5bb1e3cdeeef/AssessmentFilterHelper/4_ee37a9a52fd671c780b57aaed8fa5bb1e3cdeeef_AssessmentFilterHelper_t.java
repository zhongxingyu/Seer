 package org.iucn.sis.server.api.filters;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentFilter;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.Region;
 import org.iucn.sis.shared.api.models.Relationship;
 
 import com.solertium.db.CanonicalColumnName;
 import com.solertium.db.DBException;
 import com.solertium.db.Row;
 import com.solertium.db.RowProcessor;
 import com.solertium.db.query.QComparisonConstraint;
 import com.solertium.db.query.QConstraint;
 import com.solertium.db.query.QConstraintGroup;
 import com.solertium.db.query.QRelationConstraint;
 import com.solertium.db.query.SelectQuery;
 
 public class AssessmentFilterHelper {
 
 	private final AssessmentFilter filter;
 	private final Session session;
 
 	public AssessmentFilterHelper(Session session, AssessmentFilter filter) {
 		this.filter = filter;
 		this.session = session;
 		
 		/*Debug.println("Created an assessment filter with properties: \n" +
 			"Draft: {0}; SI Published: {1}; Recent Published: {2}; " +
 			"Region Relationship: {3}; Regions: {4}", 
 			filter.isDraft(), filter.isAllPublished(), filter.isRecentPublished(), 
 			filter.getRelationshipName(), filter.listRegionIDs()
 		);*/
 	}
 
 	private void reportAssessmentInformation(Assessment assessment) {
 		/*Debug.println("Found assessment {0} with properties: \n" +
 			"Draft: {1}; Is Published: {2}; Is Historical (Not Recent Published): {3}; " +
 			"Regions: {4}", 
 			assessment.getId(), assessment.isDraft(), assessment.isPublished(), 
 			assessment.getIsHistorical(), assessment.getRegionIDs()
 		);*/
 	}
 	
 	public List<Assessment> getAssessments(Integer taxaID) {
 		return getAssessments(taxaID, SIS.get().getDefaultSchema());
 	}
 	
 	public List<Assessment> getAssessments(Integer taxaID, String schema) {
 		final ArrayList<Assessment> ret = new ArrayList<Assessment>();
 
 		final List<Integer> filterRegions = filter.listRegionIDs();
 		
 		final AssessmentIO io = new AssessmentIO(session);
 		
 		if (filter.isDraft()) {
 			List<Assessment> draftAssessments = io.readDraftAssessmentsForTaxon(taxaID);
 			for (Assessment draft : draftAssessments)
 				if (allowAssessment(draft, schema, filterRegions))
 					ret.add(draft);
 		}
 
 		if (filter.isRecentPublished() || filter.isAllPublished()) {
 			List<Assessment> publishedAssessments  = io.readPublishedAssessmentsForTaxon(taxaID);
 			//Probably don't need to sort here...
 			//Collections.sort(publishedAssessments, new PublishedAssessmentsComparator());
 			for (Assessment published : publishedAssessments)
 				if (published != null && allowAssessment(published, schema, filterRegions)) 
 					ret.add(published);
 		}
 
 		return ret;
 	}
 	
 	public boolean allowAssessment(Assessment assessment) {
 		return allowAssessment(assessment, SIS.get().getDefaultSchema(), filter.listRegionIDs());
 	}
 
 	private boolean allowAssessment(Assessment assessment, String schema, List<Integer> filterRegions) {
 		reportAssessmentInformation(assessment);
 		
 		boolean result = false;
 		if (filter.isRecentPublished() && assessment.isPublished() && assessment.getIsHistorical())
 			result = false;
		/*else if (filter.isDraft() && !((filter.isRecentPublished() || filter.isAllPublished()) && !assessment.isDraft()))
			result = false;*/
 		else {
 			if (filter.getRelationshipName().equalsIgnoreCase(Relationship.ALL))
 				result = true;
 			else if (filter.getRelationshipName().equalsIgnoreCase(Relationship.OR)) {
 				List<Integer> regionIds = assessment.getRegionIDs();
 				for (Region region : filter.getRegions())
 					if (regionIds.contains(region.getId()))
 						result |= true;
 				
 				result |= false; //If it hasn't returned yet, no region matched
 			}
 			else {
 				List<Integer> assessmentRegions = assessment.getRegionIDs();
 				result = assessmentRegions.size() == filterRegions.size() && assessmentRegions.containsAll(filterRegions);
 			}
 		}
 		
 		if (schema != null)
 			result &= schema.equals(assessment.getSchema(SIS.get().getDefaultSchema()));
 		
 		//Debug.println("Assessment {0} is allowed: {1}", assessment.getId(), result);
 		
 		return result;
 	}
 	
 	public Collection<Integer> getAssessmentIds(Integer taxaID) {
 		return getAssessmentIds(taxaID, null);
 	}
 	
 	public Collection<Integer> getAssessmentIds(Integer taxaID, String schema) {
 		QConstraintGroup schemaConstraintGroup = new QConstraintGroup();
 		if (schema == null) {
 			schemaConstraintGroup.addConstraint(new QComparisonConstraint(new CanonicalColumnName("assessment", "schema"), QConstraint.CT_EQUALS, null));
 			schemaConstraintGroup.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName("assessment", "schema"), QConstraint.CT_EQUALS, SIS.get().getDefaultSchema()));
 		}
 		else
 			schemaConstraintGroup.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName("assessment", "schema"), QConstraint.CT_EQUALS, schema));	
 		
 		SelectQuery query = new SelectQuery();
 		query.select("assessment", "*");
 		query.constrain(new QComparisonConstraint(new CanonicalColumnName("assessment","taxonid"), QConstraint.CT_EQUALS, taxaID));
 		query.constrain(new QComparisonConstraint(new CanonicalColumnName("assessment","state"), QConstraint.CT_EQUALS, Assessment.ACTIVE));
 		query.constrain(schemaConstraintGroup);
 		
 		if (filter.isAllPublished() || filter.isRecentPublished()) {
 			
 			QConstraintGroup constraint = new QConstraintGroup();
 			constraint.addConstraint(new QComparisonConstraint(new CanonicalColumnName("assessment","assessment_typeid"), QConstraint.CT_EQUALS, AssessmentType.PUBLISHED_ASSESSMENT_STATUS_ID));
 			if (filter.isRecentPublished())
 				constraint.addConstraint(QConstraint.CG_AND, new QComparisonConstraint(new CanonicalColumnName("assessment","historical"), QConstraint.CT_EQUALS, "true") );
 			if (filter.isDraft()) {
 				constraint.addConstraint(QConstraint.CG_OR, new QComparisonConstraint(new CanonicalColumnName("assessment","assessment_typeid"), QConstraint.CT_EQUALS, AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
 			}
 			query.constrain(constraint);
 		} else if (filter.isDraft()){
 			query.constrain(new QComparisonConstraint(new CanonicalColumnName("assessment","assessment_typeid"), QConstraint.CT_EQUALS, AssessmentType.DRAFT_ASSESSMENT_STATUS_ID));
 		}
 		
 		
 		
 		if (!filter.isAllRegions()) {
 			
 			query.select("field", "*");
 			query.select("primitive_field", "*");
 			query.select("fk_list_primitive_values", "*");
 			
 			query.join("field", new QRelationConstraint(new CanonicalColumnName("assessment","id"), new CanonicalColumnName("field","assessmentid")));
 			query.constrain(new QComparisonConstraint(new CanonicalColumnName("field","name"), QConstraint.CT_EQUALS, "RegionInformation"));
 			query.join("primitive_field", new QRelationConstraint(new CanonicalColumnName("field", "id"), new CanonicalColumnName("primitive_field", "fieldid")));
 			query.constrain(new QComparisonConstraint(new CanonicalColumnName("primitive_field", "name"), QConstraint.CT_EQUALS, "regions"));
 			query.join("fk_list_primitive_values", new QRelationConstraint(new CanonicalColumnName("fk_list_primitive_values", "fk_list_primitive_id"), new CanonicalColumnName("primitive_field", "id")));
 
 			
 		
 		}
 			
 		final Set<Integer> ids = new HashSet<Integer>();
 		final Map<Integer, Set<Integer>> idsToRegions = new HashMap<Integer, Set<Integer>>();
 		try {
 			SIS.get().getExecutionContext().doQuery(query, new RowProcessor() {
 			
 				@Override
 				public void process(Row row) {
 					Integer asmID = row.get(0).getInteger();
 					if (filter.isAllRegions())
 						ids.add(asmID);
 					else {
 						if (!idsToRegions.containsKey(asmID))
 							idsToRegions.put(asmID, new HashSet<Integer>());
 						idsToRegions.get(asmID).add(row.get("value").getInteger());
 					}
 				}
 			});
 		} catch (DBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 		
 		
 		if (!filter.isAllRegions()) {
 			if (filter.getRelationshipName().equalsIgnoreCase(Relationship.OR)) {
 				for (Entry<Integer, Set<Integer>> entry : idsToRegions.entrySet()) {
 					if (entry.getValue().containsAll(filter.listRegionIDs()))
 						ids.add(entry.getKey());
 				}
 				
 			} else if (filter.getRelationshipName().equalsIgnoreCase(Relationship.AND)) {
 				for (Entry<Integer, Set<Integer>> entry : idsToRegions.entrySet()) {
 					for (Integer id : filter.listRegionIDs()) {
 						if (entry.getValue().contains(id)) {
 							ids.add(entry.getKey());
 							break;
 						}
 					}
 					
 				}
 			}
 		}
 		
 		return ids;
 		
 	}
 		
 
 }

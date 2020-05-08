 package org.biosemantics.disambiguation.service.impl;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.biosemantics.conceptstore.common.domain.Concept;
 import org.biosemantics.conceptstore.common.domain.ConceptType;
 import org.biosemantics.conceptstore.common.domain.Label;
 import org.biosemantics.conceptstore.common.domain.Notation;
 import org.biosemantics.conceptstore.common.service.ConceptQueryService;
 import org.biosemantics.conceptstore.utils.domain.impl.ErrorMessage;
 import org.biosemantics.disambiguation.domain.impl.ConceptImpl;
 import org.biosemantics.disambiguation.domain.impl.LabelImpl;
 import org.biosemantics.disambiguation.domain.impl.NotationImpl;
 import org.biosemantics.disambiguation.service.IndexService;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.util.CollectionUtils;
 
 public class ConceptQueryServiceImpl implements ConceptQueryService {
 
 	private IndexService indexService;
 	private GraphStorageTemplate graphStorageTemplate;
 	private static final Logger logger = LoggerFactory.getLogger(ConceptQueryServiceImpl.class);
 
 	@Required
 	public void setIndexService(IndexService indexService) {
 		this.indexService = indexService;
 	}
 
	@Required
 	public void setGraphStorageTemplate(GraphStorageTemplate graphStorageTemplate) {
 		this.graphStorageTemplate = graphStorageTemplate;
 	}
 
 	@Override
 	public Collection<Concept> fullTextSearch(String text, int maxResults) {
 		return indexService.fullTextSearch(text, maxResults);
 	}
 
 	@Override
 	public Concept getConceptByUuid(String uuid) {
		checkArgument(!(checkNotNull(uuid).isEmpty()), ErrorMessage.EMPTY_STRING_MSG, "uuid");
 		return indexService.getConceptByUuid(uuid);
 	}
 
 	@Override
 	public Collection<Concept> getConceptsByLabel(Label label) {
 		Collection<Label> labels = indexService.getLabelsByText(label.getText());
 		Label found = null;
 		for (Label existingLabel : labels) {
 			if (existingLabel.getLanguage() == label.getLanguage()) {
 				found = existingLabel;
 				break;
 			}
 		}
 		if (found == null) {
 
 			logger.warn("no label found matching {}", label);
 			// FIXME should i throw an checked exception here instead of returning empty ArrayList
 			return new ArrayList<Concept>();
 		} else {
 			return getConceptsForLabel(found);
 		}
 	}
 
 	@Override
 	public Collection<Concept> getConceptsByLabelText(String text) {
 		checkArgument(!(checkNotNull(text).isEmpty()), ErrorMessage.EMPTY_STRING_MSG, "text");
 		Collection<Label> labels = indexService.getLabelsByText(text);
 		Set<Concept> concepts = new HashSet<Concept>();
 		if (CollectionUtils.isEmpty(labels)) {
 			logger.warn("no labels found for text {} ", text);
 		} else {
 			for (Label label : labels) {
 				concepts.addAll(getConceptsForLabel(label));
 			}
 		}
 		return concepts;
 	}
 
 	@Override
 	public Collection<Concept> getConceptsByNotationCode(String code) {
 		checkArgument(!(checkNotNull(code).isEmpty()), ErrorMessage.EMPTY_STRING_MSG, "code");
 		Collection<Notation> notations = indexService.getNotationByCode(code);
 		Set<Concept> concepts = new HashSet<Concept>();
 		if (CollectionUtils.isEmpty(notations)) {
 			logger.warn("no notations found for code {} ", code);
 		} else {
 			for (Notation notation : notations) {
 				concepts.addAll(getConceptsForNotation(notation));
 			}
 		}
 		return concepts;
 	}
 
 	@Override
 	public Collection<Concept> getConceptsByType(ConceptType conceptType) {
 		// FIXME there is a lot of overlap between concept type and default relationship types. On defining a new
 		// concept type we will have to define a new DefaultRelationshipType and same for deletion, which is not
 		// optimum. See if we can remove this dependency.
 		DefaultRelationshipType defaultRelationshipType = null;
 		switch (conceptType) {
 		case CONCEPT:
 			defaultRelationshipType = DefaultRelationshipType.CONCEPTS;
 			break;
 		case DOMAIN:
 			defaultRelationshipType = DefaultRelationshipType.DOMAINS;
 			break;
 		case CONCEPT_SCHEME:
 			defaultRelationshipType = DefaultRelationshipType.CONCEPT_SCHEMES;
 			break;
 		case PREDICATE:
 			defaultRelationshipType = DefaultRelationshipType.PREDICATES;
 			break;
 		default:
 			break;
 		}
 		Node node = graphStorageTemplate.getParentNode(defaultRelationshipType);
 		Set<Concept> concepts = new HashSet<Concept>();
		Iterable<Relationship> relationships = node.getRelationships(DefaultRelationshipType.CONCEPT,
 				Direction.OUTGOING);
 		for (Relationship relationship : relationships) {
 			concepts.add(new ConceptImpl(relationship.getEndNode()));
 		}
 		return concepts;
 	}
 
 	private Collection<Concept> getConceptsForLabel(Label found) {
 		Set<Concept> concepts = new HashSet<Concept>();
 		LabelImpl labelImpl = (LabelImpl) found;
 		Iterable<Relationship> relationships = labelImpl.getUnderlyingNode().getRelationships(
 				DefaultRelationshipType.HAS_LABEL, Direction.INCOMING);
 		for (Relationship relationship : relationships) {
 			concepts.add(new ConceptImpl(relationship.getStartNode()));
 		}
 		return concepts;
 	}
 
 	private Collection<? extends Concept> getConceptsForNotation(Notation notation) {
 		Set<Concept> concepts = new HashSet<Concept>();
 		NotationImpl notationImpl = (NotationImpl) notation;
 		Iterable<Relationship> relationships = notationImpl.getUnderlyingNode().getRelationships(
 				DefaultRelationshipType.HAS_NOTATION, Direction.INCOMING);
 		for (Relationship relationship : relationships) {
 			concepts.add(new ConceptImpl(relationship.getStartNode()));
 		}
 		return concepts;
 	}
 }

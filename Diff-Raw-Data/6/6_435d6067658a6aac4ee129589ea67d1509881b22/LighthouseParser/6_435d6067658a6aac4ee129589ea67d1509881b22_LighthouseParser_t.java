 package edu.uci.lighthouse.core.parser;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseExternalClass;
 import edu.uci.lighthouse.model.LighthouseField;
 import edu.uci.lighthouse.model.LighthouseInterface;
 import edu.uci.lighthouse.model.LighthouseMethod;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModifier;
 import edu.uci.lighthouse.model.LighthouseRelationship;
 import edu.uci.lighthouse.parser.IParser;
 import edu.uci.lighthouse.parser.ParserEntity;
 import edu.uci.lighthouse.parser.ParserException;
 import edu.uci.lighthouse.parser.ParserFactory;
 import edu.uci.lighthouse.parser.ParserRelationship;
 import edu.uci.lighthouse.parser.ParserEntity.EntityType;
 import edu.uci.lighthouse.parser.ParserRelationship.RelationshipType;
 
 public class LighthouseParser {
 
 	private static Logger logger = Logger.getLogger(LighthouseParser.class);
 	
 	private Collection<LighthouseEntity> listEntities = new LinkedHashSet<LighthouseEntity>();
 	private Collection<LighthouseRelationship> listRelationships = new LinkedHashSet<LighthouseRelationship>();
 	
 	private Map<String, LighthouseEntity> mapEntity = new HashMap<String, LighthouseEntity>();
 	
 	public void execute(Collection<IFile> files) throws ParserException {
 		IParser parser = ParserFactory.getDefaultParser();
 		parser.parse(files);
 		populateAllEntityToModel(parser.getEntities());
 		populateAllRelationshipsToModel(parser.getRelationships());
 	}
 
 	public void executeInAJob(final Collection<IFile> files, final IParserAction action) throws ParserException {
 		execute(files);
 		action.doAction();
 	}
 
 	public Collection<LighthouseEntity> getListEntities() {
 		return listEntities;
 	}
 
 	public Collection<LighthouseRelationship> getListRelationships() {
 		return listRelationships;
 	}
 	
 	private void populateAllEntityToModel(Collection<ParserEntity> listParserEntities) {
 		for (ParserEntity parseEntity : listParserEntities) {
 			String fqn = parseEntity.getFqn();
 			Collection<String> modifiers = parseEntity.getListModifiers();
 			EntityType type = parseEntity.getType();
 			LighthouseEntity entity = null;
 			switch (type) {
 			case CLASS:
 				entity = new LighthouseClass(fqn);
 				break;
 			case INTERFACE:
 				entity = new LighthouseInterface(fqn);
 				break;
 			case FIELD:
 				entity = new LighthouseField(fqn);
 				break;
 			case METHOD:
 				entity = new LighthouseMethod(fqn);
 				break;
 			case EXTERNAL_CLASS:
 				entity = new LighthouseExternalClass(fqn);
 				break;
 			}
 			if (entity != null) {
 				addEntity(entity);
 				if (!modifiers.isEmpty()) {
 					addModifiers(entity, modifiers);
 				}
 			} else {
 				logger.error("Parser generated an unknown entity");
 			}
 		}
 	}
 
 	private void populateAllRelationshipsToModel(Collection<ParserRelationship> listParserRelationships) {		
 		for (ParserRelationship parseRelationship : listParserRelationships) {
 			String fqnFrom = parseRelationship.getFrom();
 			String fqnTo = parseRelationship.getTo();
 			RelationshipType type = parseRelationship.getType();
 			LighthouseEntity entityFrom = LighthouseModel.getInstance().getEntity(fqnFrom);
 			LighthouseEntity entityTo = LighthouseModel.getInstance().getEntity(fqnTo);
 			// If entityFrom is null we need to check the entity inside the LighthouseFile(model)
 			if (entityFrom==null) {
 				entityFrom = getEntity(fqnFrom);
 			}
 			// If entityTo is null we need to check the entity inside the LighthouseFile(model)
 			if (entityTo==null) { 
 				entityTo = getEntity(fqnTo);
 				// If entityTo still null, then it is an external class
 				if (entityTo==null) {
 					entityTo = new LighthouseExternalClass(fqnTo);
 					addEntity(entityTo);
 				}
 			}
 			if (entityFrom != null && entityTo != null) {
 				LighthouseRelationship relationship = new LighthouseRelationship(entityFrom,entityTo,getLHRelationshipType(type));
 				addRelationship(relationship);
 			} else {
				if (entityFrom == null) {
					logger.error("FQN FROM: " + fqnFrom + " does not belong to the model");
				} else if (entityTo == null) {
					logger.error("FQN TO: " + fqnTo + " does not belong to the model");
				}
 			}
 		}
 	}
 	
 	private LighthouseEntity getEntity(String fqn) {
 		return mapEntity.get(fqn);
 	}
 	
 	private void addEntity(LighthouseEntity entity) {
 		listEntities.add(entity);
 		mapEntity.put(entity.getFullyQualifiedName(),entity);
 	}
 	
 	private void addRelationship(LighthouseRelationship rel) {
 		listRelationships.add(rel);
 	}
 
 	private void addModifiers(LighthouseEntity entity, Collection<String> listModifiers) {
 		for (String strModifier : listModifiers) {
 			LighthouseModifier modifier = new LighthouseModifier(strModifier.toString());
 			LighthouseRelationship relationship = new LighthouseRelationship(entity, modifier, LighthouseRelationship.TYPE.MODIFIED_BY);
 			addEntity(modifier);
 			addRelationship(relationship);
 		}
 	}
 	
 	public LighthouseRelationship.TYPE getLHRelationshipType(RelationshipType type) {
 		return LighthouseRelationship.TYPE.valueOf(type.name());
 	}
 	
 }

 package edu.uci.lighthouse.core.controller;
 
 import java.util.Collection;
 import java.util.LinkedHashSet;
 
 import org.apache.log4j.Logger;
 
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseInterface;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.model.LighthouseRelationship;
 import edu.uci.lighthouse.model.LighthouseEvent.TYPE;
 
 public class UpdateLighthouseModel {
 
 	private static Logger logger = Logger.getLogger(UpdateLighthouseModel.class);
 
 	private LighthouseModel model;
 
 	public UpdateLighthouseModel(LighthouseModel model) {
 		this.model = model;
 	}
 
 	/**Does not save in database, only in the lighthouse model*/
 	public void updateByEvents(Collection<LighthouseEvent> listEvents) {
 		LighthouseModelManager LhManager = new LighthouseModelManager(model);
 		Collection<String> listClassesToRemove = new LinkedHashSet<String>(); 
 		// for each entity event
 		for (LighthouseEvent event : listEvents) {
 			Object artifact = event.getArtifact();
 			if (artifact instanceof LighthouseEntity) {
 				logger.debug("updating: " + event.toString());
 				LhManager.addEvent(event);
 				/**
 				 * if I have an event to remove a class/interface AND this class is not on
 				 * my workspace than I have to remove this class/interface from my model and
 				 * hence from my visualization
 				 */
 				if (event.getType()==TYPE.REMOVE) {
 					if (artifact instanceof LighthouseClass || artifact instanceof LighthouseInterface) {
 						LighthouseEntity entity = (LighthouseEntity) artifact;
 						String fqn = entity.getFullyQualifiedName();
 						if ( Controller.getWorkingCopy().get(fqn)==null )  {
 							listClassesToRemove.add(fqn);
 						}
 					}
 				}
 			} else if (artifact instanceof LighthouseRelationship) {
 				if (event.getType()==TYPE.REMOVE) {
 					LighthouseRelationship rel = (LighthouseRelationship) artifact;
 					LhManager.removeRelationship(rel);
 				}
 			}
 		}
 		// for each relationship event
 		for (LighthouseEvent event : listEvents) {
 			Object artifact = event.getArtifact();
 			if (artifact instanceof LighthouseRelationship) {
 				logger.debug("updating: " + event.toString());
				if (event.getType()==TYPE.ADD) {
					LhManager.addEvent(event);	
				}
 			}
 		}
 		LhManager.removeArtifactsAndEvents(listClassesToRemove);
 	}
 
 }

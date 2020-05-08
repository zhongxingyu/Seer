 package org.iucn.sis.client.panels.publication;
 
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.shared.api.models.PublicationData;
 
 import com.extjs.gxt.ui.client.data.BaseModelData;
 
 public class PublicationModelData extends BaseModelData {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private final PublicationData model;
 	
 	public PublicationModelData(PublicationData model) {
 		this.model = model;
 		
 		set("group", model.getGroup());
 		set("taxon", model.getAssessment().getTaxon().getFriendlyName());
 		set("status", model.getAssessment().getType());
		set("date", model.getSubmissionDate());
 		set("goal", model.getTargetGoal() == null ? "Unset" : model.getTargetGoal().getName());
 		set("approved", model.getTargetApproved() == null ? "Unset" : model.getTargetApproved().getName());
 		set("submitter", model.getSubmitter().getDisplayableName());
 		set("notes", model.getNotes());
 		set("priority", model.getPriority() == null ? 0 : model.getPriority());
 	}
 	
 	public PublicationData getModel() {
 		return model;
 	}
 
 }

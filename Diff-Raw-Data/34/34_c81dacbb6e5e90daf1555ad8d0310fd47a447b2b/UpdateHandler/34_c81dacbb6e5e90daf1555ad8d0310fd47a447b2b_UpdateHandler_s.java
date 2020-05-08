 package com.iq4j.faces.taghandler;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.component.UICommand;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ComponentSystemEvent;
 import javax.faces.event.ComponentSystemEventListener;
 import javax.faces.event.PostValidateEvent;
 import javax.faces.view.facelets.ComponentHandler;
 import javax.faces.view.facelets.FaceletContext;
 import javax.faces.view.facelets.TagAttribute;
 import javax.faces.view.facelets.TagConfig;
 import javax.faces.view.facelets.TagHandler;
 
 import org.omnifaces.event.PostInvokeActionEvent;
 import org.primefaces.context.RequestContext;
 import org.primefaces.util.ComponentUtils;
 
 public class UpdateHandler extends TagHandler {
 
 	private final TagAttribute onSuccessAttribute;
 	private final TagAttribute onFailureAttribute;
 	private final TagAttribute updateFormOnSuccessAttribute;
 	
 	public UpdateHandler(TagConfig config) {
 		super(config);
 		onSuccessAttribute = getAttribute("onSuccess");
 		onFailureAttribute = getAttribute("onFailure");
 		updateFormOnSuccessAttribute = getAttribute("updateFormOnSuccess");		 
 	}
 
 	@Override
 	public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
 		
 		if(parent instanceof UICommand  && ComponentHandler.isNew(parent)) {
 		
 			boolean updateFormOnSuccess = this.updateFormOnSuccessAttribute == null ? true : this.updateFormOnSuccessAttribute.getBoolean(ctx);
 			
			List<String> onSuccessList = createUpdateList(onSuccessAttribute);
			List<String> onFailureList = createUpdateList(onFailureAttribute);
 			
 			parent.subscribeToEvent(PostValidateEvent.class, new UpdateEventListener(onSuccessList, onFailureList, updateFormOnSuccess));
 			parent.subscribeToEvent(PostInvokeActionEvent.class, new UpdateEventListener(onSuccessList, onFailureList, updateFormOnSuccess));
 			
 		}
 		
 	}
 
    private List<String> createUpdateList(TagAttribute attribute) {
 		
 		List<String> result = new ArrayList<String>(0);
 		
 		if ( attribute != null) {
			
			String[] renderIds = attribute.getValue()
									.replaceAll("[\\s;]", ",")
									.replaceAll(",+", ",")
								    .replaceAll(",:", ",")
								    .split(",");
			
			for (String string : renderIds) {
				result.add(string.startsWith(":") ? string.replaceFirst(":", "") : string);
 			}
 		}
 		return result;
 	}
 
 	
 }
 
 class UpdateEventListener implements ComponentSystemEventListener, Serializable {
 	
 	private static final long serialVersionUID = -4362334377883544295L;
 
 	private List<String> onSuccess;
 	private List<String> onFailure;
 	private boolean updateFormOnSuccess;
 	private String formClientId;
 	
 	// Necessary for state saving
 	public UpdateEventListener() {}
 	
 	public UpdateEventListener(List<String> onSuccess, List<String> onFailure, boolean updateFormOnSuccess) {
 		this.onSuccess = onSuccess;
 		this.onFailure = onFailure;
 		this.updateFormOnSuccess = updateFormOnSuccess;
 	}
 	
 	@Override
 	public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
 		
 		//final ELContext elContext = FacesContext.getCurrentInstance().getELContext();
 		FacesContext facesContext = FacesContext.getCurrentInstance();
 		RequestContext rc = RequestContext.getCurrentInstance();
 		boolean facesValidation = !facesContext.isValidationFailed();
 
 		
 		if(event instanceof PostValidateEvent ) {
 			if(!facesValidation) {
 				rc.update(onFailure);
 				rc.update(formClientId(facesContext, event.getComponent()));
 			}
 			
 		} 
 		else {
 			if(facesValidation) {				
 				rc.update(onSuccess);
 				if(updateFormOnSuccess) {					
 					rc.update(formClientId(facesContext, event.getComponent()));
 				}
 			} 
 			
 		}
 		
 	}
 	
 	private String formClientId(FacesContext facesContext, UIComponent component) {
 		
 		if(formClientId == null) {
 			UIComponent form = ComponentUtils.findParentForm(facesContext, component);
 			formClientId = (form != null) ? form.getClientId() : "";	
 		}
 		
 		return formClientId;
 	}
 	
 }

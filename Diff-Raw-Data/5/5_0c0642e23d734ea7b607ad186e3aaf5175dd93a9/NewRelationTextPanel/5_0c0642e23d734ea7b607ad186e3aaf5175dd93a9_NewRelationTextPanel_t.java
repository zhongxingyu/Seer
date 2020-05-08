 package pl.psnc.dl.wf4ever.portal.components.annotations;
 
 import java.net.URI;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.event.Broadcast;
 import org.apache.wicket.event.IEvent;
 import org.apache.wicket.markup.head.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 import org.purl.wf4ever.rosrs.client.Annotable;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 
 import pl.psnc.dl.wf4ever.portal.components.feedback.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.components.form.AuthenticatedAjaxEventButton;
 import pl.psnc.dl.wf4ever.portal.events.ErrorEvent;
 import pl.psnc.dl.wf4ever.portal.events.annotations.AnnotationAddedEvent;
 import pl.psnc.dl.wf4ever.portal.events.annotations.AnnotationCancelledEvent;
 import pl.psnc.dl.wf4ever.portal.events.edit.ApplyEvent;
 import pl.psnc.dl.wf4ever.portal.events.edit.CancelEvent;
 import pl.psnc.dl.wf4ever.portal.events.edit.EditEvent;
 import pl.psnc.dl.wf4ever.portal.model.wicket.AnnotationTripleModel;
 
 /**
  * A panel for inline comments, show the annotation author and creation date.
  * 
  * @author piotrekhol
  * 
  */
 public class NewRelationTextPanel extends Panel {
 
     /** Logger. */
     @SuppressWarnings("unused")
     private static final Logger LOG = Logger.getLogger(NewRelationTextPanel.class);
 
     /** id. */
     private static final long serialVersionUID = 1L;
 
     /** A CSS file for this panel. */
     private static final JavaScriptResourceReference JS_REFERENCE = new JavaScriptResourceReference(
             AdvancedAnnotationsPanel.class, "EditableAnnotationTextPanel.js");
 
     /** The read only view of the field. */
     private Fragment viewFragment;
 
     /** The fragment that allows to edit the property and the value. */
     private EditFragment editFragment;
 
     /** The property that the user can edit. */
     private URI newProperty;
 
     /** The value that the user can edit. */
     private String newValue;
 
     private URI selectedSubject;
     private URI selectedObject;
     private URI selectedRelation;
 
     private String newValueFromHand;
 
     public Component checkBoxInnerObjectRelation;
     public IModel<ResearchObject> roModel;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param model
      *            the model for the quad
      * @param editMode
      *            should the field start in an edit mode
      */
     public NewRelationTextPanel(String id, IModel<ResearchObject> model, boolean editMode, List<URI> subjectsList,
             List<URI> relationsList, List<URI> objectsList) {
         super(id, model);
         roModel = model;
         newValueFromHand = "";
         setOutputMarkupPlaceholderTag(true);
 
         selectedSubject = subjectsList.get(0);
         selectedObject = objectsList.get(0);
         selectedRelation = relationsList.get(0);
         DropDownChoice<URI> dropDownSubjects = new DropDownChoice<URI>("subjectsList", new PropertyModel<URI>(this,
                 "selectedSubject"), subjectsList);
         DropDownChoice<URI> dropDownObjects = new DropDownChoice<URI>("objectsList", new PropertyModel<URI>(this,
                 "selectedObject"), objectsList);
         DropDownChoice<URI> dropDownRelations = new DropDownChoice<URI>("relationsList", new PropertyModel<URI>(this,
                 "selectedRelation"), relationsList);
         TextField<String> vlaueFromHand = new TextField<String>("value-from-hand", new PropertyModel<String>(this,
                 "newValueFromHand"));
 
         setOutputMarkupPlaceholderTag(true);
         viewFragment = new ViewFragment("content", "view", this);
         editFragment = new EditFragment("content", "editSingle", this, dropDownSubjects, dropDownObjects,
                 dropDownRelations, vlaueFromHand);
 
         add(editMode ? editFragment : viewFragment).setOutputMarkupPlaceholderTag(true);
 
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
     }
 
 
     @Override
     public void onEvent(IEvent<?> event) {
         super.onEvent(event);
         if (event.getPayload() instanceof EditEvent) {
             onEdit((EditEvent) event.getPayload());
         }
         if (event.getPayload() instanceof ApplyEvent) {
             onApply((ApplyEvent) event.getPayload());
         }
         if (event.getPayload() instanceof CancelEvent) {
             onCancel((CancelEvent) event.getPayload());
         }
     }
 
 
     /**
      * Called when the edit button is clicked. Replace the view fragment with edit.
      * 
      * @param event
      *            click event
      */
     private void onEdit(EditEvent event) {
         viewFragment.replaceWith(editFragment);
         event.getTarget().appendJavaScript("$('.tooltip').remove();");
         event.getTarget().add(this);
     }
 
 
     /**
      * Called when the apply button is clicked. Replace the edit fragment with view and post an event.
      * 
      * @param event
      *            click event
      */
     private void onApply(ApplyEvent event) {
         editFragment.replaceWith(viewFragment);
         event.getTarget().appendJavaScript("$('.tooltip').remove();");
         event.getTarget().add(this);
         //the tripleCopy now holds the updated property and value
         //, new AnnotationTripleModel(new Model<>(annotable), (URI) null, false), true);
         //((AnnotationTripleModel) this.getDefaultModel()).setObject(new AnnotationTriple(n, subject, property, value, merge))
         //getAnnotable
 
         //((AnnotationTripleModel) this.getDefaultModel()).setPropertyAndValue(newProperty, newValue);
         //post event
         // IModel<? extends Annotable> annotable = ((AnnotationTripleModel) this.getDefaultModel()).getAnnotableModel();
 
         Annotable annotable;
         if (selectedSubject.equals(roModel.getObject().getUri())) {
             annotable = roModel.getObject();
         }
         //else try to get resource
         else {
             annotable = roModel.getObject().getResource(selectedSubject);
         }
         // and if it not resource try to get folder
         if (annotable == null) {
             annotable = roModel.getObject().getFolder(selectedSubject);
         }
         AnnotationTripleModel triple = new AnnotationTripleModel(new Model(annotable), selectedRelation, false);
         String value = "";
         if (editFragment.getCheckBoxState()) {
             value = newValueFromHand;
        } else {
            value = selectedObject.toString();
         }
 
         triple.setPropertyAndValue(selectedRelation, value);
         send(getPage(), Broadcast.BREADTH, new AnnotationAddedEvent(event.getTarget(), new Model(annotable)));
     }
 
 
     /**
      * Called when the cancel button is clicked. Replace the edit fragment with view and post an event.
      * 
      * @param event
      *            click event
      */
     private void onCancel(CancelEvent event) {
         editFragment.replaceWith(viewFragment);
         event.getTarget().appendJavaScript("$('.tooltip').remove();");
         event.getTarget().add(this);
         IModel<? extends Annotable> annotable = roModel;
         send(getPage(), Broadcast.BREADTH, new AnnotationCancelledEvent(event.getTarget(), annotable));
     }
 
 
     /**
      * Read-only view fragment.
      * 
      * @author piotrekhol
      * 
      */
     protected class ViewFragment extends Fragment {
 
         /** id. */
         private static final long serialVersionUID = -4169842101720666349L;
 
         /** The value. It may be a simple label of the {@link LinkFragment}. */
         private Component value;
 
 
         /**
          * Constructor.
          * 
          * @param id
          *            wicket id
          * @param markupId
          *            fragment wicket id
          * @param markupProvider
          *            container defining the fragment
          * @param model
          *            value model
          */
         public ViewFragment(String id, String markupId, MarkupContainer markupProvider) {
             super(id, markupId, markupProvider);
             setOutputMarkupPlaceholderTag(true);
         }
     }
 
 
     /**
      * A fragment which the user can use to edit the value.
      * 
      * @author piotrekhol
      * 
      */
     class EditFragment extends Fragment {
 
         /** id. */
         private static final long serialVersionUID = -4169842101720666349L;
 
         /** A div that contains all components. */
         private WebMarkupContainer controlGroup;
         boolean checkBoxState = false;
         private EditFragment fragmet = this;
 
 
         /**
          * Constructor.
          * 
          * @param id
          *            wicket id
          * @param markupId
          *            fragment wicket id
          * @param markupProvider
          *            container defining the fragment
          * @param valueFromHand
          * @param propertyModel
          *            the property to edit
          * @param valueModel
          *            the value to edit
          */
         public EditFragment(String id, String markupId, MarkupContainer markupProvider,
                 DropDownChoice<URI> dropDownSubjects, final DropDownChoice<URI> dropDownObjects,
                 DropDownChoice<URI> dropDownProperties, final TextField<String> valueFromHand) {
             super(id, markupId, markupProvider);
             setOutputMarkupPlaceholderTag(true);
             controlGroup = new WebMarkupContainer("control-group");
             controlGroup.setOutputMarkupPlaceholderTag(true);
 
             dropDownObjects.setOutputMarkupPlaceholderTag(true);
             valueFromHand.setOutputMarkupPlaceholderTag(true);
 
             controlGroup.add(dropDownSubjects);
             controlGroup.add(dropDownObjects);
             controlGroup.add(dropDownProperties);
             controlGroup.add(valueFromHand);
             valueFromHand.setVisible(false);
 
             checkBoxInnerObjectRelation = new AjaxCheckBox("checkbox-inner-object-relation",
                     new PropertyModel<Boolean>(this, "checkBoxState")) {
 
                 @Override
                 protected void onUpdate(AjaxRequestTarget target) {
                     valueFromHand.setVisible(checkBoxState);
                     dropDownObjects.setVisible(!checkBoxState);
                     target.add(valueFromHand);
                     target.add(dropDownObjects);
                 }
             };
             controlGroup.add(checkBoxInnerObjectRelation);
             add(controlGroup);
 
             controlGroup.add(new AuthenticatedAjaxEventButton("apply", null, NewRelationTextPanel.this,
                     ApplyEvent.class));
             controlGroup.add(new AuthenticatedAjaxEventButton("cancel", null, NewRelationTextPanel.this,
                     CancelEvent.class).setDefaultFormProcessing(false));
             controlGroup.add(new MyFeedbackPanel("feedback").setOutputMarkupPlaceholderTag(true));
         }
 
 
         public boolean getCheckBoxState() {
             return checkBoxState;
         }
 
 
         @Override
         public void onEvent(IEvent<?> event) {
             super.onEvent(event);
             if (event.getPayload() instanceof ErrorEvent) {
                 onError((ErrorEvent) event.getPayload());
             }
         }
 
 
         /**
          * Form validation failed.
          * 
          * @param event
          *            the event payload
          */
         private void onError(ErrorEvent event) {
             controlGroup.add(AttributeAppender.append("class", " error"));
             event.getTarget().add(controlGroup);
         }
     }
 
 }

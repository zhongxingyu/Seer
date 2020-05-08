 package pl.psnc.dl.wf4ever.portal.pages.ro;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.apache.log4j.Logger;
 import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Check;
 import org.apache.wicket.markup.html.form.CheckGroup;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.PropertyListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.purl.wf4ever.rosrs.client.Annotation;
 import org.purl.wf4ever.rosrs.client.Creator;
 import org.purl.wf4ever.rosrs.client.Statement;
 import org.purl.wf4ever.rosrs.client.Thing;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import pl.psnc.dl.wf4ever.portal.pages.ro.components.EditLinkFragment;
 import pl.psnc.dl.wf4ever.portal.pages.ro.components.ExternalLinkFragment;
 import pl.psnc.dl.wf4ever.portal.pages.ro.components.InternalLinkFragment;
 import pl.psnc.dl.wf4ever.portal.pages.ro.roexplorer.behaviours.IAjaxLinkListener;
 import pl.psnc.dl.wf4ever.portal.pages.util.CreatorsPanel;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyAjaxButton;
import pl.psnc.dl.wf4ever.portal.ui.components.LoadingCircle;
 
 /**
  * A list of annotations of a resource.
  * 
  * @author piotrekhol
  * 
  */
 @SuppressWarnings("serial")
 class AnnotatingBox extends Panel implements IAjaxLinkListener {
 
     /** The owning page. */
     private final RoPage roPage;
 
     /** List of annotations. */
     final PropertyListView<Annotation> annList;
 
     /** Statements selected by the user. */
     final List<Statement> selectedStatements = new ArrayList<Statement>();
 
     /** Add statement button. */
     private AjaxButton addStatement;
 
     /** Delete statement button. */
     private AjaxButton deleteStatement;
 
     /** Add relation button. */
     private AjaxButton addRelation;
 
     /** Import annotation body button. */
     private AjaxButton importAnnotation;
     /** Buttons Container. */
     private WebMarkupContainer buttonsContainer;
     /** Table container. */
     private WebMarkupContainer tableContainer;
 
     /** Logger. **/
     private static final Logger LOG = Logger.getLogger(RoPage.class);
 
    private LoadingCircle loadingAnnotationCircle;
    private static final String LOADING_ANNOTATIONS_OBJECT = "Loading annotations.<br />Please wait...";

 
     public Thing getModelObject() {
         return (Thing) getDefaultModelObject();
     }
 
 
     /**
      * Constructor.
      * 
      * @param roPage
      *            parent page
      * @param itemModel
      *            {@link AggregatedResource} model
      */
     public AnnotatingBox(final RoPage roPage, final CompoundPropertyModel<Thing> itemModel) {
         super("annotatingBox", itemModel);
         this.roPage = roPage;
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         buttonsContainer = new WebMarkupContainer("buttons-container");
         tableContainer = new WebMarkupContainer("table-container");
         Form<?> annForm = new Form<Void>("annotationsForm");
         add(annForm);
         CheckGroup<Statement> group = new CheckGroup<Statement>("group", selectedStatements);
         tableContainer.add(group);
 
         IModel<List<Annotation>> listModel = new ListModel(new PropertyModel<Collection<Annotation>>(itemModel,
                 "annotations"));
         annList = new PropertyListView<Annotation>("annotationsList", listModel) {
 
             @Override
             protected void populateItem(ListItem<Annotation> item) {
                 final Annotation annotation = item.getModelObject();
                 item.add(new AttributeAppender("title", new PropertyModel<URI>(annotation, "uri")));
                 try {
                     if (!annotation.isLoaded()) {
                         annotation.load();
                     }
                 } catch (ROSRSException | IOException e) {
                     LOG.error(e.getMessage(), e);
                 }
                 PropertyListView<Statement> statementsList = new AnnotationsListView("statementsList",
                         new PropertyModel<List<Statement>>(annotation, "statements"), itemModel);
                 item.add(statementsList);
                 item.add(new CreatorsPanel("creator", new PropertyModel<Set<Creator>>(itemModel, "creators")));
                 item.add(new Label("createdFormatted"));
             }
         };
         group.add(annList);
 
         addStatement = new MyAjaxButton("addAnnotation", annForm) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 try {
                     AnnotatingBox.this.roPage.stmtEditForm.setModelObject(new Statement(itemModel.getObject().getUri(),
                             null));
                     AnnotatingBox.this.roPage.stmtEditForm.setAddMode();
                     target.add(AnnotatingBox.this.roPage.stmtEditForm);
                     target.appendJavaScript("showStmtEdit('');");
                 } catch (Exception e) {
                     error(e.getMessage());
                 }
                 target.add(roPage.getFeedbackPanel());
             }
         };
 
         deleteStatement = new MyAjaxButton("deleteAnnotation", annForm) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 List<Annotation> annotations = new ArrayList<Annotation>();
                 for (Statement statement : selectedStatements) {
                     statement.getAnnotation().getStatements().remove(statement);
                     annotations.add(statement.getAnnotation());
                 }
                 for (Annotation annotation : annotations) {
                     try {
                         if (!annotation.isLoaded()) {
                             annotation.load();
                         }
                         if (annotation.getStatements().isEmpty()) {
                             annotation.delete();
                         } else {
                             annotation.update();
                         }
                     } catch (Exception e) {
                         error(e);
                     }
                 }
                 //                AnnotatingBox.this.getModelObject().setAnnotations(
                 //                    RoFactory.createAnnotations(roPage.getRodlURI(), AnnotatingBox.this.roPage.roURI,
                 //                        AnnotatingBox.this.getModelObject().getUri(), MySession.get().getUsernames()));
                 selectedStatements.clear();
                 target.add(roPage.getFeedbackPanel());
                 target.add(AnnotatingBox.this);
                 //                target.add(AnnotatingBox.this.roPage.roViewerBox.infoPanel);
             }
         };
 
         addRelation = new MyAjaxButton("addRelation", annForm) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 try {
                     AnnotatingBox.this.roPage.relEditForm.setModelObject(new Statement(itemModel.getObject().getUri(),
                             null));
                     AnnotatingBox.this.roPage.relEditForm.setAddMode();
                     target.add(AnnotatingBox.this.roPage.relEditForm);
                     target.appendJavaScript("showRelEdit();");
                 } catch (Exception e) {
                     error(e.getMessage());
                 }
                 target.add(roPage.getFeedbackPanel());
             }
         };
 
         importAnnotation = new MyAjaxButton("importAnnotation", annForm) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 try {
                     target.appendJavaScript("$('#import-annotation-modal').modal('show');");
                 } catch (Exception e) {
                     error(e.getMessage());
                 }
                 target.add(roPage.getFeedbackPanel());
             }
         };
 
         buttonsContainer.add(addStatement);
         buttonsContainer.add(deleteStatement);
         buttonsContainer.add(addRelation);
         buttonsContainer.add(importAnnotation);
 
         annForm.add(buttonsContainer);
         annForm.add(tableContainer);
 
     }
 
 
     @Override
     protected void onConfigure() {
         addRelation.setEnabled(roPage.canEdit && getDefaultModelObject() != null);
         addStatement.setEnabled(roPage.canEdit && getDefaultModelObject() != null);
         deleteStatement.setEnabled(roPage.canEdit && getDefaultModelObject() != null);
         importAnnotation.setEnabled(roPage.canEdit && getDefaultModelObject() != null);
         buttonsContainer.setVisible(getDefaultModelObject() != null);
         tableContainer.setVisible(getDefaultModelObject() != null
                 && !((List<?>) (annList.getDefaultModelObject())).isEmpty());
     }
 
 
     private final class ListModel extends AbstractReadOnlyModel<List<Annotation>> {
 
         private final IModel<Collection<Annotation>> itemModel;
 
 
         private ListModel(IModel<Collection<Annotation>> itemModel) {
             this.itemModel = itemModel;
         }
 
 
         public List<Annotation> getObject() {
             if (itemModel.getObject() == null) {
                 return null;
             }
             return new ArrayList<Annotation>(itemModel.getObject());
         }
     }
 
 
     /**
      * A list of annotations.
      * 
      * @author piotrekhol
      * 
      */
     private final class AnnotationsListView extends PropertyListView<Statement> {
 
         /** Selected resource model. */
         private CompoundPropertyModel<Thing> itemModel;
 
 
         /**
          * Constructor.
          * 
          * @param id
          *            wicket id
          * @param model
          *            list of statements model
          * @param itemModel
          *            selected resource model
          */
         private AnnotationsListView(String id, IModel<? extends List<? extends Statement>> model,
                 CompoundPropertyModel<Thing> itemModel) {
             super(id, model);
             this.itemModel = itemModel;
         }
 
 
         @Override
         protected void populateItem(final ListItem<Statement> item) {
             final Statement statement = item.getModelObject();
             item.add(new Check<Statement>("checkbox", item.getModel()));
             if (statement.isSubjectURIResource()) {
                 if (roPage.researchObject.getResource(statement.getSubjectURI()) != null
                         && roPage.researchObject.getResource(statement.getSubjectURI()).isInternal()) {
                     if (statement.getSubjectURI().equals(itemModel.getObject().getUri())) {
                         item.add(new Label("subject", "[This item]"));
                     } else {
                         item.add(new InternalLinkFragment("subject", "internalLinkFragment", AnnotatingBox.this.roPage,
                                 statement, roPage.researchObject));
                     }
                 } else {
                     item.add(new ExternalLinkFragment("subject", "externalLinkFragment", AnnotatingBox.this.roPage,
                             (CompoundPropertyModel<Statement>) item.getModel(), "subjectURI"));
                 }
             } else {
                 item.add(new Label("subject", ((CompoundPropertyModel<Statement>) item.getModel())
                         .<String> bind("subjectValue")).setEscapeModelStrings(false));
             }
             item.add(new Label("propertyLocalNameNice"));
             if (statement.isObjectURIResource()) {
                 item.add(new ExternalLinkFragment("object", "externalLinkFragment", AnnotatingBox.this.roPage,
                         (CompoundPropertyModel<Statement>) item.getModel(), "objectURI"));
             } else {
                 item.add(new Label("object", ((CompoundPropertyModel<Statement>) item.getModel())
                         .<String> bind("objectValue")).setEscapeModelStrings(false));
             }
             if (AnnotatingBox.this.roPage.canEdit) {
                 item.add(new EditLinkFragment("edit", "editLinkFragment", AnnotatingBox.this.roPage,
                         new AjaxFallbackLink<String>("link") {
 
                             @Override
                             public void onClick(AjaxRequestTarget target) {
                                 AnnotatingBox.this.roPage.stmtEditForm.setModelObject(statement);
                                 AnnotatingBox.this.roPage.stmtEditForm.setEditMode();
                                 target.add(AnnotatingBox.this.roPage.stmtEditForm);
                                 target.appendJavaScript("showStmtEdit('"
                                         + StringEscapeUtils.escapeEcmaScript(statement.getObjectValue()) + "');");
                             }
                         }));
             } else {
                 item.add(new Label("edit", "Edit"));
             }
         }
     }
 
 
     @Override
     public void onAjaxLinkClicked(AjaxRequestTarget target) {
         AbstractDefaultAjaxBehavior x = new AbstractDefaultAjaxBehavior() {
 
             @Override
             protected void respond(AjaxRequestTarget target) {
                 target.add(AnnotatingBox.this);
             }
 
         };
         add(x);
         target.appendJavaScript(x.getCallbackUrl());
     }
 }

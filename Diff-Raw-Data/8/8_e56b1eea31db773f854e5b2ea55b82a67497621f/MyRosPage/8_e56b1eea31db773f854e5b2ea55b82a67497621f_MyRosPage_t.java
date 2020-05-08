 package pl.psnc.dl.wf4ever.portal.pages.my;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.http.HttpStatus;
 import org.apache.log4j.Logger;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Check;
 import org.apache.wicket.markup.html.form.CheckGroup;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.RefreshingView;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.IValidator;
 import org.apache.wicket.validation.ValidationError;
 import org.purl.wf4ever.rosrs.client.ROSRSException;
 import org.purl.wf4ever.rosrs.client.ROSRService;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.pages.MyExpImportPage;
 import pl.psnc.dl.wf4ever.portal.pages.base.Base;
 import pl.psnc.dl.wf4ever.portal.pages.ro.RoPage;
 import pl.psnc.dl.wf4ever.portal.pages.util.ModelIteratorAdapter;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyAjaxButton;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyFeedbackPanel;
 
 /**
  * A page with user's own Research Objects.
  * 
  * @author piotrekhol
  * 
  */
 @AuthorizeInstantiation("USER")
 public class MyRosPage extends Base {
 
     /** id. */
     private static final long serialVersionUID = 1L;
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(MyRosPage.class);
 
     /** ROs selected by the user. */
     final List<ResearchObject> selectedResearchObjects = new ArrayList<ResearchObject>();
 
     /** New RO id. */
     private String roId;
 
     /** Feedback panel for adding ROs. */
     private MyFeedbackPanel addFeedbackPanel;
 
     /** Default feedback panel. */
     private MyFeedbackPanel deleteFeedbackPanel;
 
 
     /**
      * Constructor.
      * 
      * @param parameters
      *            page params
      * @throws URISyntaxException
      *             can't connect to RODL
      * @throws ROSRSException
      *             getting the RO list ends with an unexpected response code
      */
     @SuppressWarnings("serial")
     public MyRosPage(final PageParameters parameters)
             throws URISyntaxException, ROSRSException {
         super(parameters);
 
         final ROSRService rosrs = MySession.get().getRosrs();
         List<URI> uris = rosrs.getROList(false);
         final List<ResearchObject> researchObjects = new ArrayList<ResearchObject>();
         for (URI uri : uris) {
             try {
                 researchObjects.add(new ResearchObject(uri, rosrs));
             } catch (Exception e) {
                 error("Could not get manifest for: " + uri + " (" + e.getMessage() + ")");
             }
         }
 
         final Form<?> form = new Form<Void>("form");
         form.setOutputMarkupId(true);
         add(form);
         form.add(new MyFeedbackPanel("feedbackPanel"));
         CheckGroup<ResearchObject> group = new CheckGroup<ResearchObject>("group", selectedResearchObjects);
         form.add(group);
         RefreshingView<ResearchObject> list = new MyROsRefreshingView("rosListView", researchObjects);
         group.add(list);
 
         final Label deleteCntLabel = new Label("deleteCnt", new PropertyModel<String>(this, "deleteCnt"));
         deleteCntLabel.setOutputMarkupId(true);
         add(deleteCntLabel);
 
         final Form<?> addForm = new Form<Void>("addForm");
         RequiredTextField<String> name = new RequiredTextField<String>("roId", new PropertyModel<String>(this, "roId"));
         name.add(new IValidator<String>() {
 
             @Override
             public void validate(IValidatable<String> validatable) {
                 try {
                     if (!rosrs.isRoIdFree(validatable.getValue())) {
                         validatable.error(new ValidationError().setMessage("This ID is already in use"));
                     }
                 } catch (Exception e) {
                     LOG.error(e);
                     // assume it's ok
                 }
             }
 
         });
         addForm.add(name);
         add(addForm);
 
         addFeedbackPanel = new MyFeedbackPanel("addFeedbackPanel");
         addFeedbackPanel.setOutputMarkupId(true);
         addForm.add(addFeedbackPanel);
 
         form.add(new MyAjaxButton("delete", form) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 form.process(null);
                 if (!selectedResearchObjects.isEmpty()) {
                     target.add(deleteCntLabel);
                     target.appendJavaScript("$('#confirm-delete-modal').modal('show')");
                 }
             }
         });
 
         deleteFeedbackPanel = new MyFeedbackPanel("deleteFeedbackPanel");
         deleteFeedbackPanel.setOutputMarkupId(true);
         add(deleteFeedbackPanel);
 
         add(new MyAjaxButton("confirmDelete", form) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 for (ResearchObject ro : selectedResearchObjects) {
                     try {
                         rosrs.deleteResearchObject(ro.getUri());
                         researchObjects.remove(ro);
                     } catch (Exception e) {
                         error("Could not delete Research Object: " + ro.getUri() + " (" + e.getMessage() + ")");
                     }
                 }
                 target.add(form);
                 target.add(deleteFeedbackPanel);
                 target.appendJavaScript("$('#confirm-delete-modal').modal('hide')");
             }
         });
 
         add(new MyAjaxButton("cancelDelete", form) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 target.appendJavaScript("$('#confirm-delete-modal').modal('hide')");
             }
         });
 
         form.add(new MyAjaxButton("add", form) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 target.appendJavaScript("$('#confirm-add-modal').modal('show')");
             }
         });
 
         addForm.add(new MyAjaxButton("confirmAdd", addForm) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> addForm) {
                 super.onSubmit(target, addForm);
                 try {
                     researchObjects.add(ResearchObject.create(rosrs, roId));
                     target.appendJavaScript("$('#confirm-add-modal').modal('hide')");
                 } catch (ROSRSException e) {
                     if (e.getStatus() == HttpStatus.SC_CONFLICT) {
                         error("This ID is already used.");
                     } else {
                         error("Could not add Research Object: " + roId + " (" + e.getMessage() + ")");
                     }
                 }
                 target.add(form);
                 target.add(addFeedbackPanel);
             }
 
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 super.onError(target, form);
                 target.add(addFeedbackPanel);
             }
         });
 
         addForm.add(new MyAjaxButton("cancelAdd", addForm) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 target.appendJavaScript("$('#confirm-add-modal').modal('hide')");
             }
         }.setDefaultFormProcessing(false));
         form.add(new BookmarkablePageLink<Void>("myExpImport", MyExpImportPage.class));
     }
 
 
     /**
      * The message to display when deleting ROs.
      * 
      * @return the message
      */
     public String getDeleteCnt() {
         if (selectedResearchObjects.size() == 1) {
             return "1 Research Object";
         }
         return selectedResearchObjects.size() + " Research Objects";
     }
 
 
     public String getRoId() {
         return roId;
     }
 
 
     public void setRoId(String roId) {
         this.roId = roId;
     }
 
 
     /**
      * The ROs refreshing view.
      * 
      * @author piotrekhol
      * 
      */
     private final class MyROsRefreshingView extends RefreshingView<ResearchObject> {
 
         /** id. */
         private static final long serialVersionUID = -6310254217773728128L;
 
         /** ROs. */
         private final List<ResearchObject> researchObjects;
 
 
         /**
          * Constructor.
          * 
          * @param id
          *            wicket id
          * @param researchObjects
          *            list of ROs
          */
         private MyROsRefreshingView(String id, List<ResearchObject> researchObjects) {
             super(id);
             this.researchObjects = researchObjects;
         }
 
 
         @Override
         protected void populateItem(Item<ResearchObject> item) {
             ResearchObject researchObject = (ResearchObject) item.getDefaultModelObject();
             item.add(new Check<ResearchObject>("checkbox", item.getModel()));
             BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("link", RoPage.class);
             link.getPageParameters().add("ro", researchObject.getUri().toString());
            link.add(new Label("uri"));
             item.add(link);
             item.add(new Label("createdFormatted"));
         }
 
 
         @Override
         protected Iterator<IModel<ResearchObject>> getItemModels() {
             return new ModelIteratorAdapter<ResearchObject>(researchObjects.iterator()) {
 
                 @Override
                 protected IModel<ResearchObject> model(ResearchObject ro) {
                     return new CompoundPropertyModel<ResearchObject>(ro);
                 }
             };
         }
     }
 }

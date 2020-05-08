 package pl.psnc.dl.wf4ever.portal.pages;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.extensions.markup.html.tree.Tree;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.UrlDecoder;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.util.convert.IConverter;
 import org.apache.wicket.util.lang.Bytes;
 import org.scribe.model.Token;
 
 import pl.psnc.dl.wf4ever.portal.MySession;
 import pl.psnc.dl.wf4ever.portal.model.AggregatedResource;
 import pl.psnc.dl.wf4ever.portal.model.Annotation;
 import pl.psnc.dl.wf4ever.portal.model.ResearchObject;
 import pl.psnc.dl.wf4ever.portal.model.RoFactory;
 import pl.psnc.dl.wf4ever.portal.model.Statement;
 import pl.psnc.dl.wf4ever.portal.pages.util.MyAjaxButton;
 import pl.psnc.dl.wf4ever.portal.pages.util.RoTree;
 import pl.psnc.dl.wf4ever.portal.pages.util.SelectableRefreshableView;
 import pl.psnc.dl.wf4ever.portal.pages.util.URIConverter;
 import pl.psnc.dl.wf4ever.portal.services.OAuthException;
 import pl.psnc.dl.wf4ever.portal.services.ROSRService;
 import pl.psnc.dl.wf4ever.portal.utils.RDFFormat;
 
 public class RoPage
 	extends TemplatePage
 {
 
 	private static final long serialVersionUID = 1L;
 
 	private URI roURI;
 
 	private boolean canEdit = false;
 
 	private final RoViewerBox roViewerBox;
 
 	private final AnnotatingBox annotatingBox;
 
 	private final StatementEditForm stmtEditForm;
 
 	private RoFactory roFactory;
 
 
 	public RoPage(final PageParameters parameters)
 		throws URISyntaxException, MalformedURLException, OAuthException
 	{
 		super(parameters);
 		ResearchObject ro = null;
 		if (!parameters.get("ro").isEmpty()) {
 			roURI = new URI(UrlDecoder.QUERY_INSTANCE.decode(parameters.get("ro").toString(), "UTF-8"));
 			roFactory = new RoFactory(roURI);
 			ro = roFactory.createResearchObject(true);
 		}
 		else {
 			throw new RestartResponseException(ErrorPage.class, new PageParameters().add("message",
 				"The RO URI is missing"));
 		}
 
 		if (MySession.get().isSignedIn()) {
 			List<URI> uris = ROSRService.getROList(MySession.get().getdLibraAccessToken());
 			canEdit = uris.contains(ro.getURI());
 		}
 		add(new Label("title", ro.getURI().toString()));
 
 		final CompoundPropertyModel<AggregatedResource> itemModel = new CompoundPropertyModel<AggregatedResource>(ro);
 		roViewerBox = new RoViewerBox(itemModel, new PropertyModel<TreeModel>(roFactory, "aggregatedResourcesTree"));
 		add(roViewerBox);
 		annotatingBox = new AnnotatingBox(itemModel);
 		add(annotatingBox);
 		annotatingBox.setAnnotationSelection(ro.getAnnotations().isEmpty() ? null : ro.getAnnotations().get(0));
 		stmtEditForm = new StatementEditForm(new CompoundPropertyModel<Statement>(new PropertyModel<Statement>(
 				annotatingBox.entriesList, "selectedObject")));
 		add(stmtEditForm);
 		add(new ChooseRdfFormat());
 		add(new UploadResourceForm());
 	}
 
 	@SuppressWarnings("serial")
 	class RoViewerBox
 		extends WebMarkupContainer
 	{
 
 		final Tree tree;
 
 		final WebMarkupContainer itemInfo;
 
 		final WebMarkupContainer actionButtons;
 
 
 		public RoViewerBox(final CompoundPropertyModel<AggregatedResource> itemModel,
 				IModel< ? extends TreeModel> treeModel)
 		{
 			super("roViewerBox", itemModel);
 			setOutputMarkupId(true);
 			add(new Label("title", roURI.toString()));
 
 			tree = new RoTree("treeTable", treeModel) {
 
 				private static final long serialVersionUID = -7512570425701073804L;
 
 
 				@Override
 				protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node)
 				{
 					AggregatedResource res = (AggregatedResource) ((DefaultMutableTreeNode) node).getUserObject();
 					itemModel.setObject(res);
 					annotatingBox.setAnnotationSelection(res.getAnnotations().isEmpty() ? null : res.getAnnotations()
 							.get(0));
 					target.add(actionButtons);
 					target.add(itemInfo);
 					target.add(annotatingBox);
 				}
 			};
 			tree.getTreeState().expandAll();
 			tree.getTreeState().selectNode(treeModel.getObject().getRoot(), true);
 			add(tree);
 
 			Form< ? > roForm = new Form<Void>("roForm");
 			add(roForm);
 
 			actionButtons = new WebMarkupContainer("actionButtons");
 			actionButtons.setOutputMarkupId(true);
 			roForm.add(actionButtons);
 
 			MyAjaxButton addResource = new MyAjaxButton("addResource", roForm) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					target.appendJavaScript("$('#upload-resource-modal').modal('show')");
 				}
 
 
 				@Override
 				public boolean isEnabled()
 				{
 					return super.isEnabled() && canEdit;
 				}
 			};
 			actionButtons.add(addResource);
 
 			MyAjaxButton deleteResource = new MyAjaxButton("deleteResource", roForm) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					AggregatedResource res = (AggregatedResource) ((DefaultMutableTreeNode) tree.getTreeState()
 							.getSelectedNodes().iterator().next()).getUserObject();
 					try {
 						ROSRService.deleteResource(res.getURI(), MySession.get().getdLibraAccessToken());
 						roFactory.reload();
 						target.add(RoViewerBox.this);
 					}
 					catch (OAuthException e) {
 						error(e);
 					}
 				}
 
 
 				@Override
 				public boolean isEnabled()
 				{
 					return super.isEnabled()
 							&& canEdit
 							&& !tree.getTreeState().getSelectedNodes().isEmpty()
 							&& !((AggregatedResource) ((DefaultMutableTreeNode) tree.getTreeState().getSelectedNodes()
 									.iterator().next()).getUserObject()).getURI().equals(roURI);
 				}
 			};
 			actionButtons.add(deleteResource);
 
 			AjaxButton downloadMetadata = new MyAjaxButton("downloadMetadata", roForm) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					target.appendJavaScript("$('#download-metadata-modal').modal('show')");
 				}
 
 			};
 			roForm.add(downloadMetadata);
 
 			itemInfo = new WebMarkupContainer("itemInfo", itemModel);
 			itemInfo.setOutputMarkupId(true);
 			add(itemInfo);
 			itemInfo.add(new ExternalLink("resourceURI", itemModel.<String> bind("URI.toString"), itemModel
 					.<URI> bind("URI")));
 			itemInfo.add(new ExternalLink("downloadURI", itemModel.<String> bind("downloadURI.toString"), itemModel
 					.<URI> bind("downloadURI")));
 			itemInfo.add(new Label("creator"));
 			itemInfo.add(new Label("createdFormatted"));
 			itemInfo.add(new Label("sizeFormatted"));
 			itemInfo.add(new Label("annotations.size"));
 
 		}
 	}
 
 	@SuppressWarnings("serial")
 	class AnnotatingBox
 		extends WebMarkupContainer
 	{
 
 		final WebMarkupContainer entriesDiv;
 
 		final WebMarkupContainer annotationsDiv;
 
 		final SelectableRefreshableView<Annotation> annList;
 
 		final SelectableRefreshableView<Statement> entriesList;
 
 
 		public AnnotatingBox(final CompoundPropertyModel<AggregatedResource> itemModel)
 
 		{
 			super("annotatingBox", itemModel);
 			setOutputMarkupId(true);
 			add(new Label("annTarget", itemModel.<URI> bind("URI")));
 
 			annotationsDiv = new WebMarkupContainer("annotationsDiv");
 			annotationsDiv.setOutputMarkupId(true);
 			add(annotationsDiv);
 			annList = new SelectableRefreshableView<Annotation>("annsListView", new PropertyModel<List<Annotation>>(
 					itemModel, "annotations")) {
 
 				@Override
 				protected void populateItem(Item<Annotation> item)
 				{
 					super.populateItem(item);
 					item.add(new Label("createdFormatted"));
 					item.add(new Label("creator"));
 					item.add(new AttributeAppender("title", new PropertyModel<URI>(item.getModel(), "URI")));
 					item.setOutputMarkupId(true);
 				}
 
 
 				@Override
 				public void onSelectItem(AjaxRequestTarget target, Item<Annotation> item)
 				{
 					target.add(annotationsDiv);
 					target.add(entriesDiv);
 				}
 
 			};
 			annotationsDiv.add(annList);
 
 			Form< ? > annForm = new Form<Void>("annForm");
 			annotationsDiv.add(annForm);
 
 			AjaxButton addAnnotation = new MyAjaxButton("addAnnotation", annForm) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					try {
 						ROSRService.addAnnotation(roURI, itemModel.getObject().getURI(),
 							getSession().getUsername("Unknown"), getSession().getdLibraAccessToken());
 						RoFactory factory = new RoFactory(roURI);
 						itemModel.getObject().setAnnotations(factory.createAnnotations(itemModel.getObject().getURI()));
 						annList.setSelectedObject(itemModel.getObject().getAnnotations()
 								.get(itemModel.getObject().getAnnotations().size() - 1));
 						target.add(annotationsDiv);
 						target.add(roViewerBox.itemInfo);
 					}
 					catch (OAuthException | URISyntaxException e) {
 						error(e.getMessage());
 					}
 				}
 
 
 				@Override
 				public boolean isEnabled()
 				{
 					return super.isEnabled() && canEdit;
 				}
 			};
 			annForm.add(addAnnotation);
 
 			AjaxButton deleteAnnotation = new MyAjaxButton("deleteAnnotation", annForm) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					try {
 						try {
 							ROSRService.deleteAnnotation(roURI, annList.getSelectedObject().getURI(), getSession()
 									.getdLibraAccessToken());
 						}
 						catch (IllegalArgumentException e) {
 						}
 						RoFactory factory = new RoFactory(roURI);
 						itemModel.getObject().setAnnotations(factory.createAnnotations(itemModel.getObject().getURI()));
 						annList.setSelectedObject(null);
 						target.add(annotationsDiv);
 						target.add(entriesDiv);
 						target.add(roViewerBox.itemInfo);
 					}
 					catch (OAuthException | URISyntaxException e) {
 						error(e.getMessage());
 					}
 				}
 
 
 				@Override
 				public boolean isEnabled()
 				{
 					return super.isEnabled() && canEdit && annList.getSelectedObject() != null;
 				}
 			};
 			annForm.add(deleteAnnotation);
 
 			entriesDiv = new WebMarkupContainer("entriesDiv");
 			entriesDiv.setOutputMarkupId(true);
 			add(entriesDiv);
 			entriesList = new SelectableRefreshableView<Statement>("entriesListView",
 					new PropertyModel<List<Statement>>(annList, "selectedObject.body")) {
 
 				private static final long serialVersionUID = -6310254217773728128L;
 
 
 				@Override
 				protected void populateItem(final Item<Statement> item)
 				{
 					super.populateItem(item);
 					item.add(new Label("propertyLocalName"));
 					if (item.getModelObject().isObjectURIResource()) {
 						item.add(new ExternalLinkFragment("object", "externalLinkFragment", RoPage.this,
 								(CompoundPropertyModel<Statement>) item.getModel()));
 					}
 					else {
 						item.add(new Label("object", ((CompoundPropertyModel<Statement>) item.getModel())
 								.<String> bind("objectValue")).setEscapeModelStrings(false));
 					}
 					if (canEdit) {
 						item.add(new EditLinkFragment("edit", "editLinkFragment", RoPage.this,
 								new AjaxFallbackLink<String>("link") {
 
 									@Override
 									public void onClick(AjaxRequestTarget target)
 									{
 										stmtEditForm.setModelObject(item.getModelObject());
 										stmtEditForm.setTitle("Edit statement");
 										target.add(stmtEditForm);
 										target.appendJavaScript("showStmtEdit('"
 												+ StringEscapeUtils.escapeEcmaScript(item.getModelObject()
 														.getObjectValue()) + "');");
 									}
 								}));
 					}
 					else {
 						item.add(new Label("edit", "Edit"));
 					}
 				}
 
 
 				@Override
 				public void onSelectItem(AjaxRequestTarget target, Item<Statement> item)
 				{
 					target.add(entriesDiv);
 				}
 
 			};
 			entriesDiv.add(entriesList);
 
 			Form< ? > stmtForm = new Form<Void>("stmtForm");
 			entriesDiv.add(stmtForm);
 
 			AjaxButton addStatement = new MyAjaxButton("addStatement", stmtForm) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					try {
 						stmtEditForm.setModelObject(new Statement(itemModel.getObject().getURI(), annList
 								.getSelectedObject()));
 						stmtEditForm.setTitle("Add statement");
 						target.add(stmtEditForm);
 						target.appendJavaScript("showStmtEdit('');");
 					}
 					catch (URISyntaxException e) {
 						error("Error when adding preparing statement: " + e.getMessage());
 					}
 				}
 
 
 				@Override
 				public boolean isEnabled()
 				{
 					return super.isEnabled() && canEdit && annList.getSelectedObject() != null;
 				}
 			};
 			stmtForm.add(addStatement);
 
 			AjaxButton deleteStatement = new MyAjaxButton("deleteStatement", stmtForm) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					Token dLibraToken = getSession().getdLibraAccessToken();
 					Annotation ann = annList.getSelectedObject();
 					ann.getBody().remove(entriesList.getSelectedObject());
 					try {
 						ROSRService.sendResource(ann.getBodyURI(), RoFactory.wrapAnnotationBody(ann.getBody()),
 							"application/rdf+xml", dLibraToken);
 						entriesList.setSelectedObject(null);
 						target.add(annotatingBox.entriesDiv);
 					}
 					catch (OAuthException e) {
 						ann.getBody().add(entriesList.getSelectedObject());
 						error("Could not delete statement (" + e.getMessage() + ")");
 					}
 				}
 
 
 				@Override
 				public boolean isEnabled()
 				{
 					return super.isEnabled() && canEdit && entriesList.getSelectedObject() != null;
 				}
 			};
 			stmtForm.add(deleteStatement);
 		}
 
 
 		public void setAnnotationSelection(Annotation ann)
 		{
 			annList.setSelectedObject(ann);
 			entriesList.setSelectedObject(null);
 		}
 	}
 
 	@SuppressWarnings("serial")
 	class ChooseRdfFormat
 		extends Form<Void>
 	{
 
 		private RDFFormat format = RDFFormat.RDFXML;
 
 
 		public ChooseRdfFormat()
 		{
 			super("downloadMetadataForm");
 			List<RDFFormat> formats = Arrays.asList(RDFFormat.RDFXML, RDFFormat.TURTLE, RDFFormat.TRIG, RDFFormat.TRIX,
 				RDFFormat.N3);
 			DropDownChoice<RDFFormat> formatDropDown = new DropDownChoice<RDFFormat>("rdfFormat",
 					new PropertyModel<RDFFormat>(this, "format"), formats, new IChoiceRenderer<RDFFormat>() {
 
 						@Override
 						public Object getDisplayValue(RDFFormat format)
 						{
 							return format.getName();
 						}
 
 
 						@Override
 						public String getIdValue(RDFFormat format, int index)
 						{
 							return "" + index;
 						}
 					});
 			add(formatDropDown);
 			add(new MyAjaxButton("confirmDownloadMetadata", this) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					throw new RestartResponseException(RoPage.class, RoPage.this
 							.getPageParameters()
 							.add("redirectTo",
 								roURI.resolve(".ro/manifest." + getFormat().getDefaultFileExtension()).toString())
 							.add("redirectDelay", 1));
 				}
 			});
 			add(new MyAjaxButton("cancelDownloadMetadata", this) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					target.appendJavaScript("$('#download-metadata-modal').modal('hide')");
 				}
 			}.setDefaultFormProcessing(false));
 		}
 
 
 		public RDFFormat getFormat()
 		{
 			return format;
 		}
 
 
 		public void setFormat(RDFFormat format)
 		{
 			this.format = format;
 		}
 	}
 
 	@SuppressWarnings("serial")
 	class UploadResourceForm
 		extends Form<Void>
 	{
 
 		public UploadResourceForm()
 		{
 			super("uploadResourceForm");
 
 			// Enable multipart mode (need for uploads file)
 			setMultiPart(true);
 
 			// max upload size, 10k
 			setMaxSize(Bytes.megabytes(10));
 			final FileUploadField fileUpload = new FileUploadField("fileUpload");
 			add(fileUpload);
 			add(new MyAjaxButton("confirmUploadResource", this) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					final FileUpload uploadedFile = fileUpload.getFileUpload();
 					if (uploadedFile != null) {
						URI resourceURI = roURI.resolve(uploadedFile.getClientFileName());
 						try {
 							ROSRService.sendResource(resourceURI, uploadedFile.getInputStream(),
 								uploadedFile.getContentType(), MySession.get().getdLibraAccessToken());
 							roFactory.reload();
 							target.appendJavaScript("$('#upload-resource-modal').modal('hide')");
 							target.add(roViewerBox);
 						}
 						catch (IOException e) {
 							error(e);
 						}
 					}
 				}
 			});
 			add(new MyAjaxButton("cancelUploadResource", this) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					target.appendJavaScript("$('#upload-resource-modal').modal('hide')");
 				}
 			}.setDefaultFormProcessing(false));
 		}
 	}
 
 	@SuppressWarnings("serial")
 	class StatementEditForm
 		extends Form<Statement>
 	{
 
 		private final TextArea<String> value;
 
 		private final TextField<URI> objectURI;
 
 		private final TextField<URI> propertyURI;
 
 		private URI selectedProperty;
 
 		private String title;
 
 
 		public StatementEditForm(CompoundPropertyModel<Statement> model)
 		{
 			super("stmtEditForm", model);
 
 			add(new Label("title", new PropertyModel<String>(this, "title")));
 
 			List<URI> choices = Arrays.asList(RoFactory.defaultProperties);
 			DropDownChoice<URI> properties = new DropDownChoice<URI>("propertyURI", new PropertyModel<URI>(this,
 					"selectedProperty"), choices);
 			properties.setNullValid(true);
 			add(properties);
 
 			final WebMarkupContainer propertyURIDiv = new WebMarkupContainer("customPropertyURIDiv");
 			add(propertyURIDiv);
 
 			propertyURI = new TextField<URI>("customPropertyURI", new PropertyModel<URI>(this, "customProperty"),
 					URI.class) {
 
 				@SuppressWarnings("unchecked")
 				@Override
 				public <C> IConverter<C> getConverter(Class<C> type)
 				{
 					return (IConverter<C>) new URIConverter();
 				}
 			};
 			propertyURIDiv.add(propertyURI);
 
 			final WebMarkupContainer uriDiv = new WebMarkupContainer("objectURIDiv");
 			add(uriDiv);
 
 			objectURI = new TextField<URI>("objectURI", URI.class) {
 
 				@SuppressWarnings("unchecked")
 				@Override
 				public <C> IConverter<C> getConverter(Class<C> type)
 				{
 					return (IConverter<C>) new URIConverter();
 				}
 			};
 			uriDiv.add(objectURI);
 
 			final WebMarkupContainer valueDiv = new WebMarkupContainer("objectValueDiv");
 			add(valueDiv);
 
 			value = new TextArea<String>("objectValue");
 			value.setEscapeModelStrings(false);
 			valueDiv.add(value);
 
 			CheckBox objectType = new CheckBox("objectURIResource");
 			add(objectType);
 
 			add(new MyAjaxButton("save", this) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					Token dLibraToken = getSession().getdLibraAccessToken();
 					Annotation ann = StatementEditForm.this.getModelObject().getAnnotation();
 					boolean isNew = !ann.getBody().contains(StatementEditForm.this.getModelObject());
 					try {
 						if (isNew)
 							ann.getBody().add(StatementEditForm.this.getModelObject());
 						ROSRService.sendResource(ann.getBodyURI(), RoFactory.wrapAnnotationBody(ann.getBody()),
 							"application/rdf+xml", dLibraToken);
 						target.add(form);
 						target.add(annotatingBox.entriesDiv);
 						target.appendJavaScript("$('#edit-ann-modal').modal('hide')");
 					}
 					catch (OAuthException e) {
 						error("Could not update annotation (" + e.getMessage() + ")");
 						if (isNew)
 							ann.getBody().remove(StatementEditForm.this.getModelObject());
 					}
 				}
 			});
 			add(new MyAjaxButton("cancel", this) {
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
 				{
 					super.onSubmit(target, form);
 					target.appendJavaScript("$('#edit-ann-modal').modal('hide')");
 				}
 			}.setDefaultFormProcessing(false));
 		}
 
 
 		/**
 		 * @return the selectedProperty
 		 */
 		public URI getSelectedProperty()
 		{
 			if (selectedProperty == null && getModelObject() != null)
 				return getModelObject().getPropertyURI();
 			return selectedProperty;
 		}
 
 
 		/**
 		 * @param selectedProperty
 		 *            the selectedProperty to set
 		 */
 		public void setSelectedProperty(URI selectedProperty)
 		{
 			this.selectedProperty = selectedProperty;
 			if (selectedProperty != null)
 				getModelObject().setPropertyURI(selectedProperty);
 		}
 
 
 		/**
 		 * @return the selectedProperty
 		 */
 		public URI getCustomProperty()
 		{
 			if (getModelObject() != null)
 				return getModelObject().getPropertyURI();
 			return null;
 		}
 
 
 		/**
 		 * @param selectedProperty
 		 *            the selectedProperty to set
 		 */
 		public void setCustomProperty(URI customProperty)
 		{
 			if (selectedProperty == null && customProperty != null)
 				getModelObject().setPropertyURI(customProperty);
 		}
 
 
 		/**
 		 * @return the title
 		 */
 		public String getTitle()
 		{
 			return title;
 		}
 
 
 		/**
 		 * @param title
 		 *            the title to set
 		 */
 		public void setTitle(String title)
 		{
 			this.title = title;
 		}
 	}
 
 	@SuppressWarnings("serial")
 	class ExternalLinkFragment
 		extends Fragment
 	{
 
 		public ExternalLinkFragment(String id, String markupId, MarkupContainer markupProvider,
 				CompoundPropertyModel<Statement> model)
 		{
 			super(id, markupId, markupProvider, model);
 			add(new ExternalLink("link", model.<String> bind("objectURI"), model.<String> bind("objectURI")));
 		}
 	}
 
 	@SuppressWarnings("serial")
 	class EditLinkFragment
 		extends Fragment
 	{
 
 		public EditLinkFragment(String id, String markupId, MarkupContainer markupProvider,
 				AjaxFallbackLink<String> link)
 		{
 			super(id, markupId, markupProvider);
 			add(link);
 		}
 	}
 
 }

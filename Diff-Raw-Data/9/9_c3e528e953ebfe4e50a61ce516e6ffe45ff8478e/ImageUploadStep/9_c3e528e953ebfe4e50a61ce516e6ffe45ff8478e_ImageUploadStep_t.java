 package com.ecom.web.upload;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.injection.Injector;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.RefreshingView;
 import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.resource.ResourceReference;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.ecom.domain.RealState;
 import com.ecom.domain.RealStateImage;
 import com.ecom.service.interfaces.ImageService;
 import com.ecom.web.components.buttons.IndicatingAjaxSubmitLink;
 import com.ecom.web.components.image.EcomImageResouceReference;
 import com.ecom.web.components.image.StaticImage;
 import com.ecom.web.components.wizard.WizardStep;
 
 public class ImageUploadStep extends WizardStep {
 
 	private static final long serialVersionUID = -7463392511943675380L;
 	public final Collection<FileUpload> uploads = new ArrayList<FileUpload>();
 
 	@SpringBean
 	private ImageService imageService;
 
 	private FileUploadField file1;
 	private FileUploadField file2;
 	private FileUploadField file3;
 	private FileUploadField file4;
 	private FileUploadField file5;
 	private FileUploadField file6;
 	private FileUploadField file7;
 	private FileUploadField file8;
 	private FileUploadField file9;
 	private FileUploadField file10;
 
 	private WebMarkupContainer imageContainer = new WebMarkupContainer("uploadedImagesContainer");
 	private FeedbackPanel imageUploadFeedback = new FeedbackPanel("imageUploadFeedback");
 
 
 	public ImageUploadStep(IModel<String> title, IModel<String> summary, final IModel<RealState> realStateModel) {
 		super(title, summary);
 		Injector.get().inject(this);
 		imageContainer.setOutputMarkupId(true);
 		imageContainer.setOutputMarkupPlaceholderTag(true);
 		setDefaultModel(realStateModel);
 		
 		IModel<List<FileUpload>> model = new PropertyModel<List<FileUpload>>(this, "uploads");
 		add(file1 = new FileUploadField("file1", model));
 		add(file2 = new FileUploadField("file2", model));
 		add(file3 = new FileUploadField("file3", model));
 		add(file4 = new FileUploadField("file4", model));
 		add(file5 = new FileUploadField("file5", model));
 		add(file6 = new FileUploadField("file6", model));
 		add(file7 = new FileUploadField("file7", model));
 		add(file8 = new FileUploadField("file8", model));
 		add(file9 = new FileUploadField("file9", model));
 		add(file10 = new FileUploadField("file10", model));
 
 		// refreshing view
 		final RefreshingView<RealStateImage> imgListView = new RefreshingView<RealStateImage>("imagesListView") {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected Iterator<IModel<RealStateImage>> getItemModels() {
 				RealState realState = realStateModel.getObject();				
 				Iterator<RealStateImage> imageIter = realState.getGalleryImages().iterator();
 
 				return new ModelIteratorAdapter<RealStateImage>(imageIter) {
 
 					@Override
 					protected IModel<RealStateImage> model(RealStateImage object) {
 						return new CompoundPropertyModel<RealStateImage>(object);
 					}
 
 				};
 			}
 
 			@Override
 			protected void populateItem(Item<RealStateImage> listItem) {
 				RealStateImage img = listItem.getModelObject();
 
 				// title thumb nail should be hidden
 				final ResourceReference imagesResourceReference = new EcomImageResouceReference();
 				final PageParameters imageParameters = new PageParameters();
 				String imageId = img.getId();
 				imageParameters.set("id", imageId);
 
 				CharSequence urlForImage = getRequestCycle().urlFor(imagesResourceReference, imageParameters);
 
 				// only title images are allowed to edit on this step
 				listItem.add(new StaticImage("img", new Model<String>(urlForImage.toString())));
 				listItem.add(new Link<String>("deleteLink", new Model<String>(imageId)) {
 
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void onClick() {
 						String imgId = this.getDefaultModelObjectAsString();
 						imageService.deleteImage(realStateModel.getObject(), imgId);
 						this.setVisible(false);
 						imageContainer.addOrReplace(this);
 						ImageUploadStep.this.addOrReplace(imageContainer);
 					}
 				});
 
 				listItem.setVisible(!img.isTitleImage());
 
 			}
 		};
 
 		imgListView.setOutputMarkupId(true);
 		imageContainer.add(imgListView);
 
 		imageUploadFeedback.setOutputMarkupId(true);
 		add(imageUploadFeedback);
 
 		add(new IndicatingAjaxSubmitLink("upload", new Model<String>("Upload")) {
 
 			private static final long serialVersionUID = 2718354305648397798L;
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, final Form<?> form) {
 
 				List<FileUpload> uploadedFilesList = new ArrayList<FileUpload>();
 				FileUpload uploadedFile1 = file1.getFileUpload();
 				FileUpload uploadedFile2 = file2.getFileUpload();
 				FileUpload uploadedFile3 = file3.getFileUpload();
 				FileUpload uploadedFile4 = file4.getFileUpload();
 				FileUpload uploadedFile5 = file5.getFileUpload();
 				FileUpload uploadedFile6 = file6.getFileUpload();
 				FileUpload uploadedFile7 = file7.getFileUpload();
 				FileUpload uploadedFile8 = file8.getFileUpload();
 				FileUpload uploadedFile9 = file9.getFileUpload();
 				FileUpload uploadedFile10 = file10.getFileUpload();
 
 				uploadedFilesList.add(uploadedFile1);
 				uploadedFilesList.add(uploadedFile2);
 				uploadedFilesList.add(uploadedFile3);
 				uploadedFilesList.add(uploadedFile4);
 				uploadedFilesList.add(uploadedFile5);
 				uploadedFilesList.add(uploadedFile6);
 				uploadedFilesList.add(uploadedFile7);
 				uploadedFilesList.add(uploadedFile8);
 				uploadedFilesList.add(uploadedFile9);
 				uploadedFilesList.add(uploadedFile10);
 
 				saveUploadedFiles(uploadedFilesList, realStateModel.getObject());
 				imageContainer.addOrReplace(imgListView);
 				target.add(imageContainer);
 
 			}
 
 		});
 
 		// imgListView.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
 		add(imageContainer);
 	}
 
 	private boolean isMaxSizeReached(List<FileUpload> uploadedFilesList) {
 		long size = 0;
 
 		for (FileUpload f : uploadedFilesList) {
 			if (f == null)
 				continue;
 			size = size + f.getSize();
 		}
 
 		if (size > 10000) {
 			return true;
 		}
 
 		return false;
 
 	}
 
 	private void saveUploadedFiles(List<FileUpload> uploadedFiles, RealState realState) {
 		for (FileUpload uploadedFile : uploadedFiles) {
 
 			if (uploadedFile != null && uploadedFile.getClientFileName() != null) {
 				// saves images in gridFS
 				try {
 					imageService.saveUploadedImageFileInDB(uploadedFile.getClientFileName(), uploadedFile.getInputStream(), realState, false);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }

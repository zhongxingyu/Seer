 package com.ecom.web.upload;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.wicket.injection.Injector;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.FileUploadField;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.request.resource.ResourceReference;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.bson.types.ObjectId;
 
 import com.ecom.domain.RealState;
 import com.ecom.domain.RealStateImage;
 import com.ecom.repository.RealStateRepository;
 import com.ecom.service.interfaces.ImageService;
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
 
     private FileUploadForm<ObjectId> imageUploadForm;
     private WebMarkupContainer imageContainer = new WebMarkupContainer("uploadedImagesContainer");
 
     @SpringBean
     private RealStateRepository realStateRepository;
 
     public ImageUploadStep(IModel<String> title, IModel<String> summary, final IModel<ObjectId> realStateIdModel) {
         super(title, summary);
         Injector.get().inject(this);
 
         imageUploadForm = new FileUploadForm<ObjectId>("uploadForm", realStateIdModel);
         imageUploadForm.setMultiPart(true);
 
         IModel<List<FileUpload>> model = new PropertyModel<List<FileUpload>>(this, "uploads");
         imageUploadForm.add(file1 = new FileUploadField("file1", model));
         imageUploadForm.add(file2 = new FileUploadField("file2", model));
         imageUploadForm.add(file3 = new FileUploadField("file3", model));
         imageUploadForm.add(file4 = new FileUploadField("file4", model));
         imageUploadForm.add(file5 = new FileUploadField("file5", model));
         imageUploadForm.add(file6 = new FileUploadField("file6", model));
         imageUploadForm.add(file7 = new FileUploadField("file7", model));
         imageUploadForm.add(file8 = new FileUploadField("file8", model));
         imageUploadForm.add(file9 = new FileUploadField("file9", model));
         imageUploadForm.add(file10 = new FileUploadField("file10", model));
 
         imageUploadForm.add(new Button("upload") {
 
             private static final long serialVersionUID = 2718354305648397798L;
 
             @SuppressWarnings("serial")
             @Override
             public void onSubmit() {
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
 
                 saveUploadedFiles(uploadedFilesList, imageUploadForm.getModelObject());
 
                 final ObjectId id = imageUploadForm.getModelObject();
 
                 IModel<List<RealStateImage>> realStateImagesListModel = new LoadableDetachableModel<List<RealStateImage>>() {
 
                     @Override
                     protected List<RealStateImage> load() {
                         RealState realState = realStateRepository.findOne(id);
                        return realState.getGalleryImages();
                     }
                 };
 
                 ListView<RealStateImage> imgListView = new ListView<RealStateImage>("imagesListView", realStateImagesListModel) {
 
                     @Override
                     protected void populateItem(ListItem<RealStateImage> listItem) {
 
                         RealStateImage img = listItem.getModelObject();
 
                         // title thumb nail should be hidden
                         final ResourceReference imagesResourceReference = new EcomImageResouceReference();
                         final PageParameters imageParameters = new PageParameters();
                         String imageId = img.getId();
                         imageParameters.set("id", imageId);
 
                         CharSequence urlForImage = getRequestCycle().urlFor(imagesResourceReference, imageParameters);
                         
                         //only title images are allowed to edit on this step
                         listItem.add(new StaticImage("img", new Model<String>(urlForImage.toString())));
                         listItem.add(new Link<String>("deleteLink", new Model<String>(img.getId().toString())) {
                             
                             private static final long serialVersionUID = 1L;
                             
                             @Override
                             public void onClick() {
                                 String imgId = this.getDefaultModelObjectAsString();
                                 imageService.deleteImage(id, new ObjectId(imgId));
                                 this.setVisible(false);
                                 imageContainer.addOrReplace(this);
                             }
                         });
                     
                     }
                 };
 
                 imageContainer.addOrReplace(imgListView);
                 imageContainer.setVisible(true);
             }
 
         });
 
         add(imageUploadForm);
         imageContainer.setOutputMarkupId(true);
         imageContainer.setVisible(false);
         add(imageContainer);
     }
 
     private void saveUploadedFiles(List<FileUpload> uploadedFiles, ObjectId realStateId) {
         for (FileUpload uploadedFile : uploadedFiles) {
             
             if (uploadedFile != null && uploadedFile.getClientFileName() != null) {
                 //saves images in gridFS 
                 try {
                     imageService.saveUploadedImageFileInDB(uploadedFile.getClientFileName(), uploadedFile.getInputStream(), realStateId, false);
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
     }
 
 }

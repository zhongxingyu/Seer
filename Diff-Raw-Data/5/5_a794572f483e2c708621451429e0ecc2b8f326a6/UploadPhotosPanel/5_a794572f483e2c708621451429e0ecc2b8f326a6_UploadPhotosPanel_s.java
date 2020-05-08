 package eu.margiel.components;
 
 import static com.google.common.collect.Lists.*;
 import static eu.margiel.utils.Components.*;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.form.upload.FileUpload;
 import org.apache.wicket.markup.html.form.upload.MultiFileUploadField;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.util.file.Folder;
 import org.apache.wicket.util.lang.Bytes;
 
 import eu.margiel.components.nogeneric.Link;
 
 @SuppressWarnings("serial")
 public class UploadPhotosPanel extends Panel {
 	private ListView<File> imagesList;
 	private transient GalleryPhotoProvider provider;
 	private Class<? extends Page> page;
 
 	public UploadPhotosPanel(String id, Class<? extends Page> page) {
 		super(id);
 		this.page = page;
 		add(new FeedbackPanel("uploadFeedback"));
 		add(new FileUploadForm("photo_upload"));
 		add(new FolderForm());
 		add(createImagesList());
 	}
 
 	private Component createImagesList() {
 		WebMarkupContainer container = new WebMarkupContainer("images_container");
 		imagesList = new PhotosList("images", getProvider().listFiles());
 		container.add(imagesList);
 		return container;
 	}
 
 	private GalleryPhotoProvider getProvider() {
 		if (provider == null)
 			provider = new GalleryPhotoProvider();
 		return provider;
 	}
 
 	private Link createRemoveFileLink(final File file) {
 		return new Link("remove") {
 			@Override
 			public void onClick() {
 				file.delete();
 				setResponsePage(page);
 			}
 
 		};
 	}
 
 	private final class PhotosList extends ListView<File> {
 		private PhotosList(String id, List<? extends File> list) {
 			super(id, list);
 		}
 
 		@Override
 		protected void populateItem(ListItem<File> item) {
 			File file = item.getModelObject();
 			item.add(createRemoveFileLink(file));
			item.add(new StaticImage("image", provider.getPathTo(file)));
 		}
 	}
 
 	private class FileUploadForm extends Form<Void> {
 
 		List<FileUpload> images = newArrayList();
 
 		public FileUploadForm(String name) {
 			super(name);
 			setMultiPart(true);
 			add(new MultiFileUploadField("file_input", new PropertyModel<List<FileUpload>>(this, "images"), 10));
 			setMaxSize(Bytes.kilobytes(10000));
 			add(new UploadProgressBar("upload_progress", this));
 		}
 
 		@Override
 		protected void onSubmit() {
 			for (FileUpload image : images)
				provider.savePhoto(image);
 			setResponsePage(page);
 		}
 
 	}
 
 	private final class FolderForm extends Form<Void> {
 		private TextField<String> folder = textField("folderName", new Model<String>());
 
 		private FolderForm() {
 			super("folderForm");
 			add(folder);
 		}
 
 		@Override
 		protected void onSubmit() {
 			String folderName = folder.getValue();
 			new Folder(provider.getMainFolder(), folderName).mkdir();
 			setResponsePage(page);
 		}
 	}
 
 }

 package org.wicketstuff.pickwick.frontend.panel;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.WebComponent;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.data.IDataProvider;
 import org.apache.wicket.markup.repeater.data.ListDataProvider;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.wicketstuff.pickwick.PickwickApplication;
 import org.wicketstuff.pickwick.auth.PickwickSession;
 import org.wicketstuff.pickwick.backend.ImageUtils;
 import org.wicketstuff.pickwick.backend.Settings;
 import org.wicketstuff.pickwick.bean.Folder;
 import org.wicketstuff.pickwick.bean.Image;
 import org.wicketstuff.pickwick.bean.Sequence;
 
 import com.google.inject.Inject;
 
 /**
  * A panel displaying a table of all thumbnail given by the uri.  The uri is the model object.
  * 
  * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
  * @author Vincent Demay
  */
 public class SequenceGridPanel extends Panel{
 
 	@Inject
 	private Settings settings;
 
 	@Inject
 	ImageUtils imageUtils;
 
 	public SequenceGridPanel(String id, IModel model) {
 		super(id, model);
 		List imageList = imageUtils.getFolderList(
 				new File(settings.getImageDirectoryRoot(), getModelObjectAsString()), 
 				PickwickSession.get().getUser().getRoles());
 		imageList.addAll(imageUtils.getImageList(new File(settings.getImageDirectoryRoot(), getModelObjectAsString())));
 		add(newGridView("item",  new ListDataProvider(imageList)));
 	}
 
 	public SequenceGridPanel(String id) {
 		this(id, null);
 	}
 
 	protected DataView newGridView(String id, IDataProvider imageProvider) {
 		return new SequenceGridView(id, imageProvider);
 	}
 
 	public class SequenceGridView extends DataView {
 		public SequenceGridView(String id, IDataProvider dataProvider) {
 			super(id, dataProvider);
 		}
 
 		@Override
 		protected void populateItem(Item item) {
 			try {
 				if (item.getModelObject() instanceof Image){
 					Image imageProperties = (Image) item.getModelObject();
 					if (!imageProperties.getFile().getCanonicalPath().startsWith(
 							settings.getImageDirectoryRoot().getCanonicalPath()))
 						throw new RuntimeException("Requested image directory not within the root image directory");
 					WebMarkupContainer link;
 					String imagePath = imageUtils.getRelativePath(imageProperties.getFile());
 					PageParameters params = new PageParameters();
 					params.add("uri", imagePath);
 					item.add(link = new WebMarkupContainer("link"));
 					link.add(new AttributeModifier("href", true, new Model(getRequest()
 							.getRelativePathPrefixToContextRoot()
 							+ PickwickApplication.IMAGE_PAGE_PATH + "/" + imagePath)));
 					WebComponent image;
 					link.add(image = new WebComponent("thumbnail"));
 					image.add(new AttributeModifier("src", true, new Model(getRequest()
 							.getRelativePathPrefixToContextRoot()
 							+ PickwickApplication.THUMBNAIL_IMAGE_PATH + "/" + imagePath)));
 					link.add(new Label("thumbnailLabel", imageProperties.getTitle()));
 				} else if (item.getModelObject() instanceof Folder){
 					final Folder folder = (Folder) item.getModelObject();
 					Sequence sequence = imageUtils.readSequence(folder.getFile());
 					WebMarkupContainer link;
 					item.add(link = new WebMarkupContainer("link"){
 						@Override
 						protected void onComponentTag(ComponentTag tag) {
 							super.onComponentTag(tag);
 							tag.put("href", imageUtils.getRelativePath(folder.getFile()));
 						}
 					});
 					WebComponent image;
 					link.add(image = new WebComponent("thumbnail"){
 						@Override
 						protected void onComponentTag(ComponentTag tag) {
 							super.onComponentTag(tag);
 							tag.put("src", urlFor(new ResourceReference(SequenceGridPanel.class, "images/folder.png")));
 						}
 					});
					String title = sequence.getTitle();
 					if (title == null){
 						title = folder.getFile().getName();
 					}
 					link.add(new Label("thumbnailLabel", title));
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 }

 package com.secretnotes.fb.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.secretnotes.fb.client.data.IDataContainer;
 import com.secretnotes.fb.client.ui.AlbumListPanel;
 import com.secretnotes.fb.client.ui.PhotoListPanel;
 
 public class FriendProfilePanel extends DataRequestPanel {
 	
 	private String title;
 	private AlbumListPanel albumListPanel;
 	private FlowPanel photosPanel;
 	private User currentFriend;
 	
 	public FriendProfilePanel(IDataContainer dataContainer) {
 		super(dataContainer);
 		init();
 		resetPanel();
 	}
 	
 	private void init() {
 		title = "Photos";
 		getAlbumListPanel();
 		getPhotosPanel();
 		currentFriend = null;
 	}
 	
 	public void processPhotosRequest(JavaScriptObject response) {
 		JSOModel jso = response.cast();
 		JsArray<JSOModel> photos = jso.getArray(Util.ARRAY_DATA);
		resetPanel();
 		getPhotosPanel().add(new HTML("<h2>"+currentFriend.getName()+"</h2>"));
 		getPhotosPanel().add(new HTML("Photos uploaded: "+photos.length()));
 		Anchor anchor;
 		String picture;
 		for (int i=0; i<photos.length(); i++) {
 			picture = photos.get(i).get(Util.PHOTO_PICTURE);
 			anchor = new Anchor();
 			anchor.setTarget("_blank");
 			anchor.setHref(photos.get(i).get(Util.PHOTO_SOURCE));
 			anchor.getElement().setInnerHTML("<img src='"+picture+"'/>");
 			getPhotosPanel().add(anchor);
 		}
 	}
 	
 	public void addAlbum(final Album album) {
 		ClickHandler clickHandler = new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				getServer().requestAlbumPhotos(album.getId());
 			}
 		};
 		getAlbumListPanel().addAlbum(album, clickHandler);
 	}
 	
 	public void refreshPhotos(Photo photo) {
 		getAlbumListPanel().refreshPhotos(photo);
 	}
 	
 	public void addAlbumPhotos(String albumId, ArrayList<Photo> photos) {
 		Album album = getAlbumListPanel().getAlbum(albumId);
 		PhotoListPanel photoListPanel = new PhotoListPanel();
 		photoListPanel.setStyleName("photoListPanel");
 		photoListPanel.setDisplayTitle(album.getName());
 		photoListPanel.displayPhotos(photos);
 		add(photoListPanel);
 	}
 	
 	public void resetPanel() {
 		clear();
 		getPhotosPanel().clear();
 		add(getPhotosPanel());
 		getAlbumListPanel().resetPanel();
 		add(getAlbumListPanel());
 	}
 	
 	public void setFriend(String userId) {
 		currentFriend = getDataContainer().getFriendFromList(userId);
 	}
 	
 	private FlowPanel getPhotosPanel() {
 		if (photosPanel == null) {
 			photosPanel = new FlowPanel();
 		}
 		return photosPanel;
 	}
 	
 	private AlbumListPanel getAlbumListPanel() {
 		if (albumListPanel == null) {
 			albumListPanel = new AlbumListPanel();
 			albumListPanel.getElement().setId("albumListPanel");
 		}
 		return albumListPanel;
 	}
 	
 	public String getDisplayTitle() {
 		return title;
 	}
 }

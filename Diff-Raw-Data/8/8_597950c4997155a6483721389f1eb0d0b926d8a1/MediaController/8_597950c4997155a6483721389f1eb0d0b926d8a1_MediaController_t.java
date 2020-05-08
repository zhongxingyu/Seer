 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.samples.mediacast;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.ActionEvent;
 
 import org.icefaces.application.PortableRenderer;
 import org.icefaces.application.PushMessage;
 import org.icefaces.application.PushRenderer;
 import org.icefaces.mobi.utils.MobiJSFUtils;
 import org.icemobile.util.ClientDescriptor;
 import org.icemobile.util.Utils;
 
 /**
  * Controller which handles the media file uploads
  * via the new ICEfaces mobi components.
  */
 @ManagedBean(name = MediaController.BEAN_NAME)
 @SessionScoped
 public class MediaController implements Serializable {
 
 	public static final String BEAN_NAME = "mediaController";
 
 	public static final String RENDER_GROUP = "mobi";
 
 	public static final String MEDIA_FILE_KEY = "file";
 
     private static final Logger LOGGER =
             Logger.getLogger(MediaController.class.toString());
 
 	private static transient PortableRenderer portableRenderer;
 	
 	@ManagedProperty(value = "#{uploadModel}")
 	private UploadModel uploadModel;
     
     @ManagedProperty(value="#{mediaStore}")
     private MediaStore mediaStore;
     
     @ManagedProperty(value="#{mediaHelper}")
 	private static MediaHelper mediaHelper;
 
 	@ManagedProperty(value="#{mediaView}")
 	private MediaView mediaView;
 	
 	private boolean showHelpPopup = false;
 	
 	@PostConstruct
 	public void init() {
 		portableRenderer = PushRenderer.getPortableRenderer();
 	}
 
 	/**
 	 * Upload the current media type. This method can be called multiple times on the same 
 	 * MediaMessage to add different media types.
 	 * 
 	 * @param ae JSF ActionEvent
 	 * 
 	 */
 	public void upload(ActionEvent ae) {
 		processUpload(uploadModel, mediaStore);
 	}
 	
 	public static void processUpload(UploadModel model, MediaStore store){
 		if( LOGGER.isLoggable(Level.FINER)){
 			LOGGER.finer("processUpload()");
 		}
 		
 		String selectedMediaInput = model.getSelectedMediaInput();
 		LOGGER.finer(model.toString());
 		LOGGER.finer("selectedMediaInput=" + selectedMediaInput);
 		
 		// check that we have a valid file type before processing.
 		String contentType = null;
 		if (MediaMessage.MEDIA_TYPE_PHOTO.equals(selectedMediaInput)){
 			contentType = (String)model.getPhotoUploadMap().get("contentType");
 			if( contentType != null && contentType.startsWith("image")){
 				mediaHelper.processUploadedImage(model, store);
 			}
 		}
 		else if (MediaMessage.MEDIA_TYPE_VIDEO.equals(selectedMediaInput)){
 			contentType = (String)model.getVideoUploadMap().get("contentType");
 			if( contentType.startsWith("video")) {
 				mediaHelper.processUploadedVideo(model, store);
 			}
 		} 
 		else if (MediaMessage.MEDIA_TYPE_AUDIO.equals(selectedMediaInput)){
 			contentType = (String)model.getAudioUploadMap().get("contentType");
 			if( contentType.startsWith("audio")) {
 				mediaHelper.processUploadedAudio(model, store);
 			}
 		}
 		model.setSelectedMediaInput(null);
 		LOGGER.finer(model.getCurrentMediaMessage().toString());
 
 		if( contentType == null ){
 			String errorMsg = "An error occurred while upload the "
 					+ selectedMediaInput + " file, please try again.";
 			model.setUploadFeedbackMessage(errorMsg);
 			LOGGER.warning(errorMsg);
 		}
 	}
 
 	/**
 	 * An action listener method called from the "Done" button clicked when the user
 	 * has completed all uploads
 	 * 
 	 * @param ae JSF ActionEvent 
 	 */
 
 	/**
 	 * An action listener method called from the "Done" button clicked when the user
 	 * has completed all uploads
 	 * 
 	 * @param ae JSF ActionEvent 
 	 */
 	public void uploadsCompleted(ActionEvent ae) {
         uploadsCompleted(uploadModel, mediaStore);
     }
 
 	public static void uploadsCompleted(UploadModel model, MediaStore store) {
 		MediaMessage msg = model.getCurrentMediaMessage();
		System.out.println("uploadsCompleted() "+msg);
        if (msg.isHasMedia()) {
 			if( model.getTags() != null && model.getTags().length() > 0){
 			    msg.getTags().addAll(Arrays.asList(model.getTags().split(",")));
 			}
 			
 			//set stock icons for small and medium if no photo is included
 			if( model.getCameraFile() == null ){
 				//they have uploaded audio
                 if( model.getAudioFile() != null ){
 					msg.addMediumPhoto(mediaHelper.getSoundIcon());
 					msg.addSmallPhoto(mediaHelper.getSoundIconSmall());
 				}
 			}
 			
			if( msg.getTitle() == null || msg.getTitle().length() == 0 ){
 				msg.setTitle(Utils.getHttpDateFormat().format(new Date()));
 			}
 			
 			//add tags
 			if( msg.getShowVideo() ){
 				msg.getTags().add("video");
 			}
 			if( msg.getShowAudio() ){
 				msg.getTags().add("audio");
 			}
 			if( msg.getShowPhoto() ){
 				msg.getTags().add("photo");
 			}
 			
 			store.addMedia(msg);
 			LOGGER.finer("added new media message to store: " + msg);
 			try {
 				String body = msg.getTitle();
 				portableRenderer.render(RENDER_GROUP, new PushMessage(
 						"New Media Message", body));
 				model.clearCurrentMediaMessage();
 			} catch (Exception e) {
 				LOGGER.log(Level.WARNING,
 						"Media message was not sent to recipients.");
 			}
 			model
 					.setUploadFeedbackMessage("The Media Message was sent successfully.");
 		}
 
 	}
 
 	/**
 	 * Cancel the upload and reset the controls back to input selection.
 	 * 
 	 */
 	public void cancelUpload(ActionEvent ae) {
 		uploadModel.clearCurrentMediaMessage();
 	}
 
 	public String viewMediaDetail() {
 		if (uploadModel.getCurrentMediaMessage() != null) {
 			mediaView.setCurrentTab("viewer");
 		}
 		return null;
 	}
 
 	/**
 	 * Deletes the currently selected media object from the media store.
 	 * 
 	 * @return null no jsf navigation takes place.
 	 */
 	public String deleteCurrentMedia() {
 		if (mediaView.getMedia() != null) {
 			mediaStore.removeMedia(mediaView.getMedia());
 			if( mediaStore.getMedia().size() > 0 ){
 				mediaView.setMedia(mediaStore.getMedia().get(0));
 			}
 			else{
 				mediaView.setMedia(null);
 			}
 			mediaView.setCurrentTab("feed");
 		}
 		return null;
 
 	}
 
 	
 
 	/**
 	 * ActionListener called from the 'Photo' button in home.xhtml
 	 * 
 	 * @param ae ActionEvent
 	 */
 	public void chooseCamera(ActionEvent ae) {
 		LOGGER.finer("chooseCamera()");
 		uploadModel.setSelectedMediaInput(MediaMessage.MEDIA_TYPE_PHOTO);
 		uploadModel.setUploadFeedbackMessage("");
 	}
 
 	/**
 	 * ActionListener called from the 'Video' button in home.xhtml
 	 * 
 	 * @param ae ActionEvent
 	 */
 	public void chooseCamcorder(ActionEvent ae) {
 		LOGGER.finer("chooseCamcorder()");
 		uploadModel.setSelectedMediaInput(MediaMessage.MEDIA_TYPE_VIDEO);
 		uploadModel.setUploadFeedbackMessage("");
 	}
 
 	/**
 	 * ActionListener called from the 'Audio' button in home.xhtml
 	 * 
 	 * @param ae ActionEvent
 	 */
 	public void chooseMicrophone(ActionEvent ae) {
 		LOGGER.finer("chooseMicrophone()");
 		uploadModel.setSelectedMediaInput(MediaMessage.MEDIA_TYPE_AUDIO);
 		uploadModel.setUploadFeedbackMessage("");
 	}
 
     public void setUploadModel(UploadModel uploadModel){
     	this.uploadModel = uploadModel;
     }
     
     public void setMediaStore(MediaStore mediaStore){
     	this.mediaStore = mediaStore;
     }
     
     public void setMediaHelper(MediaHelper mediaHelper) {
 		this.mediaHelper = mediaHelper;
 	}
 
 	public void setMediaView(MediaView mediaView) {
 		this.mediaView = mediaView;
 	}
 	public boolean isShowHelpPopup(){
 		return showHelpPopup;
 	}
 	
 	public void setShowHelpPopup(boolean val){
 		showHelpPopup = val;
 	}
 	
 
 }

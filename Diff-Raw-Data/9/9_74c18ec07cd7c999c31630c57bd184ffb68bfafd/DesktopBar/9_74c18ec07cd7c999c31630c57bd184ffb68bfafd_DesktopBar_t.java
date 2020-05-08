 package net.thesocialos.client.view;
 
 import net.thesocialos.client.CacheLayer;
 import net.thesocialos.client.TheSocialOS;
 import net.thesocialos.client.event.ChannelClose;
 import net.thesocialos.client.event.ChannelEvent;
 import net.thesocialos.client.event.ChannelEventHandler;
 import net.thesocialos.client.event.ChannelOpen;
 import net.thesocialos.client.event.ContactPetitionChangeEventHandler;
 import net.thesocialos.client.event.ContactsPetitionChangeEvent;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Label;
 
 public class DesktopBar extends Composite {
 	
 	interface DesktopBarUiBinder extends UiBinder<HTMLPanel, DesktopBar> {
 	}
 	
 	private static DesktopBarUiBinder uiBinder = GWT.create(DesktopBarUiBinder.class);
 	
 	@UiField Label clock;
 	
 	@UiField Label username;
 	@UiField FocusPanel userPanel;
 	@UiField FocusPanel socialOSButton;
 	@UiField FocusPanel focusContacts;
 	@UiField Label lblContacts;
 	@UiField FocusPanel searchButton;
 	@UiField Label lblPetitions;
 	@UiField Label lblPetitionsNumber;
 	@UiField FocusPanel PetitionsButton;
 	@UiField FocusPanel startButton;
 	@UiField Aplication appManagerButton;
 	@UiField FocusPanel uploadButton;
 	@UiField HTMLPanel channelApi;
 	@UiField FocusPanel sharedButton;
 	
 	public DesktopBar() {
 		initWidget(uiBinder.createAndBindUi(this));
		channelApi.setStyleName("chatMenu_circle_online", true);
		
 		TheSocialOS.getEventBus().addHandler(ContactsPetitionChangeEvent.TYPE, new ContactPetitionChangeEventHandler() {
 			
 			@Override
 			public void onContactsPetitionChange(ContactsPetitionChangeEvent event) {
 				lblPetitionsNumber.setText(Integer.toString(CacheLayer.ContactCalls.getCountPetitionsContanct()));
 			}
 		});
 		TheSocialOS.getEventBus().addHandler(ChannelEvent.TYPE, new ChannelEventHandler() {
 			
 			@Override
 			public void onChannelDisconnect(ChannelClose event) {
 				channelApi.setStyleName("chatMenu_circle_online", false);
 				channelApi.setStyleName("chatMenu_circle_busy", true);
 				
 			}
 			
 			@Override
 			public void onChannelConnect(ChannelOpen event) {
 				channelApi.setStyleName("chatMenu_circle_online", true);
 				channelApi.setStyleName("chatMenu_circle_busy", false);
 				
 			}
 			
 		});
 	}
 	
 	public FocusPanel getFocusContact() {
 		return focusContacts;
 	}
 	
 	public FocusPanel getPetitionsButton() {
 		return PetitionsButton;
 	}
 	
 	public Label getPetitionsNumber() {
 		return lblPetitionsNumber;
 	}
 	
 	/*
 	 * public FocusPanel getProgramsButton() { return ProgramsButton; } public Image getImgProgram() { return
 	 * imgProgram; } public Label getLabelProgram() { return labelProgram; }
 	 */
 	public FocusPanel getSearchBox() {
 		return searchButton;
 	}
 	
 	public FocusPanel getUploadButton() {
 		return uploadButton;
 	}
 	
 	public Aplication getAppManagerButton() {
 		return appManagerButton;
 	}
 	
 	public FocusPanel getSharedButton() {
 		return sharedButton;
 	}
 	
 }

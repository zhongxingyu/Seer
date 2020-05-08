 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.Ternary;
 import ch.cern.atlas.apvs.client.event.AudioSupervisorSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.AudioUsersSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent.ConnectionType;
 import ch.cern.atlas.apvs.client.event.MeetMeRemoteEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.service.AudioServiceAsync;
 import ch.cern.atlas.apvs.client.settings.AudioSettings;
 import ch.cern.atlas.apvs.client.settings.ConferenceRooms;
 import ch.cern.atlas.apvs.client.settings.VoipAccount;
 import ch.cern.atlas.apvs.client.widget.EditableCell;
 import ch.cern.atlas.apvs.client.widget.GenericColumn;
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class AudioView extends GlassPanel implements Module {
 
 	private CellTable<String> table = new CellTable<String>();
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	private AudioSettings voipAccounts = new AudioSettings();
 	private ConferenceRooms conferenceRooms = new ConferenceRooms();
 	private String ptuId = new String("PTUWeb");
 	private VoipAccount supervisorAccount = new VoipAccount();
 
 	private EventBus cmdBus;
 
 	private List<String> fieldName = new ArrayList<String>(Arrays.asList(new String[] { "Status", "Private Call", "Group Call" }));
 	private List<Class<?>> classField = new ArrayList<Class<?>>(Arrays.asList(new Class<?>[] { TextCell.class, ButtonCell.class,ButtonCell.class }));
 
 	public AudioView() {
 	}
 
 	@Override
 	public boolean configure(Element element, final ClientFactory clientFactory,
 			Arguments args) {
 		
 		final RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 		cmdBus = clientFactory.getEventBus(args.getArg(0));
 		table.setWidth("100%");
 
 		add(table, CENTER);
 		
 		// Status/Action Field column
 		EditableCell fieldActionCell = new EditableCell(classField);
 		
 		GenericColumn<String> fieldActionCol = new GenericColumn<String>(fieldActionCell) {
 			@Override
 			public Object getValue(String fieldName) {
 				if (fieldName.equals("Status"))
 						return ((voipAccounts.getStatus(ptuId)?"Online":"Offline"));
 				else if (fieldName.equals("Private Call"))
 					return ((voipAccounts.getOnCall(ptuId) ? "Hangup '": "Call '") + voipAccounts.getUsername(ptuId) + "'");
 				else if (fieldName.equals("Group Call"))
 					return ((!conferenceRooms.conferenceOfActivityExist(voipAccounts.getActivity(ptuId)) ? ("Make "): (supervisorAccount.getOnConference() ? "Leave ": "Join "))+ "Impact conference");
 				else if(fieldName.equals("Conference"))
 					return "Close '" + voipAccounts.getActivity(ptuId)+ "' conference";
 				else if(fieldName.equals("Mute/Unmute"))
 					return (voipAccounts.getMute(ptuId) ? ("Unmute '") + voipAccounts.getUsername(ptuId) + "'" : ("Mute '") + voipAccounts.getUsername(ptuId) + "'");
 				else if(fieldName.equals("Kick/Add"))
					return ( voipAccounts.getOnConference(ptuId)? ( (voipAccounts.getRoom(ptuId) == conferenceRooms.roomOfActivity(voipAccounts.getActivity(ptuId))) ? ("Kick from '" + voipAccounts.getActivity(ptuId) + "'") : ("Hangup from '" + voipAccounts.getActivity(ptuId) + "' conference")) : ("Add to '" + voipAccounts.getActivity(ptuId) + "'"));
 				else
 					return null;
 			}
 			@Override
 			public void render(Context context, String object,
 					SafeHtmlBuilder sb) {
 				String value = (String) getValue(object);
 				
 				sb.append(SafeHtmlUtils.fromSafeConstant("<div class=\""
 						+ value.toLowerCase() + "\">"));
 				getCell().render(context, value, sb);
 				sb.append(SafeHtmlUtils.fromSafeConstant("</div>"));
 			}
 			
 		};
 		
 		fieldActionCol.setHorizontalAlignment(ALIGN_CENTER);
 		fieldActionCol.setEnabled(clientFactory.isSupervisor());
 		table.addColumn(fieldActionCol);
 		
 		fieldActionCol.setFieldUpdater(new FieldUpdater<String, Object>() {
 
 			@Override
 			public void update(int index, String fieldName, Object value) {
 				if (!clientFactory.isSupervisor()) {
 					return;
 				}
 
 				AsyncCallback<Void> callbackConference = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("FAIL CONFERENCE ESTABLISHMENT: "
 								+ caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Conference established...");
 					}
 				};
 
 				AsyncCallback<Void> callbackCall = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err
 								.println("FAIL CALL ESTABLISHMENT: " + caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Call Established...");
 					}
 				};
 
 				AsyncCallback<Void> callbackHangup = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("FAIL HANGUP ESTABLISHMENT: " + caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Call Hangup...");
 					}
 				};
 				
 				AsyncCallback<Void> callbackMute = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("FAIL MUTE: " + caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Mute/Unmute Success...");
 					}	
 				};
 
 				if (fieldName.equals("Private Call")) {
 					if (!voipAccounts.getOnCall(ptuId)) {
 						List<String> channels = new ArrayList<String>();
 						channels.add(supervisorAccount.getChannel());
 						channels.add(voipAccounts.getChannel(ptuId));
 						
 						// Hangup Supervisor and PTU User from active calls
 						AudioServiceAsync.Util.getInstance().hangupMultiple(channels, callbackHangup);
 						AudioServiceAsync.Util.getInstance().call(voipAccounts.getNumber(ptuId),supervisorAccount.getNumber(), callbackCall);
 
 					} else {
 							AudioServiceAsync.Util.getInstance().hangup(supervisorAccount.getChannel(),callbackHangup);
 							if (conferenceRooms.conferenceOfActivityExist(voipAccounts.getActivity(ptuId))) {
 								AudioServiceAsync.Util.getInstance().addToConference(voipAccounts.getNumber(ptuId),conferenceRooms.roomOfActivity(voipAccounts.getActivity(ptuId)),callbackConference);
 						}
 					}
 					
 				} else if (fieldName.equals("Group Call")){
 					if (!supervisorAccount.getOnConference()) {
 						if (!conferenceRooms.conferenceOfActivityExist(voipAccounts.getActivity(ptuId))) {
 							// Hangup Impact Activity Users from active calls
 							AudioServiceAsync.Util.getInstance().hangupMultiple(voipAccounts.getActiveChannelsActivity(voipAccounts.getActivity(ptuId)),callbackHangup);
 							AudioServiceAsync.Util.getInstance().newConference(voipAccounts.getNumbersActivity(voipAccounts.getActivity(ptuId)),callbackConference);
 						} else{
 							// Hangup Supervisor from active calls
 							AudioServiceAsync.Util.getInstance().hangup(supervisorAccount.getChannel(),callbackHangup);
 							AudioServiceAsync.Util.getInstance().addToConference(supervisorAccount.getAccount(),voipAccounts.getRoom(ptuId),callbackConference);
 						}
 					} else {
 						AudioServiceAsync.Util.getInstance().hangup(supervisorAccount.getChannel(),callbackHangup);
 					}
 					
 				} else if (fieldName.equals("Conference")){
 					AudioServiceAsync.Util.getInstance().hangupMultiple(voipAccounts.getActiveChannelsActivity(voipAccounts.getActivity(ptuId)),callbackHangup);
 					if( voipAccounts.getRoom(ptuId).equals(supervisorAccount.getRoom()))
 						AudioServiceAsync.Util.getInstance().hangup(supervisorAccount.getChannel(),callbackHangup);
 					
 				} else if (fieldName.equals("Mute/Unmute")){
 					//System.out.println(voipAccounts.getMute(ptuId));
 					if(voipAccounts.getMute(ptuId)){
 						AudioServiceAsync.Util.getInstance().unMuteUser(voipAccounts.getRoom(ptuId),voipAccounts.getChannel(ptuId), ptuId, callbackMute);
 					}else{
 						AudioServiceAsync.Util.getInstance().muteUser(voipAccounts.getRoom(ptuId),voipAccounts.getChannel(ptuId), ptuId, callbackMute);
 					}
 					
 				} else if (fieldName.equals("Kick/Add")){
 					if(voipAccounts.getOnConference(ptuId))
 						AudioServiceAsync.Util.getInstance().hangup(voipAccounts.getChannel(ptuId),callbackHangup);
 					else
 						AudioServiceAsync.Util.getInstance().addToConference(voipAccounts.getNumber(ptuId),conferenceRooms.roomOfActivity(voipAccounts.getActivity(ptuId)),callbackConference);
 					
 				} else
 					return;
 			}
 		});
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(new ArrayList<String>());
 
 		if (cmdBus != null) {
 			SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 				@Override
 				public void onPtuSelected(SelectPtuEvent event) {
 					ptuId = event.getPtuId();
 					table.redraw();
 				}
 			});
 		}
 
 		ConnectionStatusChangedRemoteEvent.subscribe(eventBus,
 				new ConnectionStatusChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onConnectionStatusChanged(
 							ConnectionStatusChangedRemoteEvent event) {
 						if (event.getConnection() == ConnectionType.audio) {
 							showGlass(!event.getStatus().isTrue());
 						}
 					}
 				});
 
 		AudioUsersSettingsChangedRemoteEvent.subscribe(eventBus,
 				new AudioUsersSettingsChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onAudioUsersSettingsChanged(
 							AudioUsersSettingsChangedRemoteEvent event) {
 						voipAccounts = event.getAudioSettings();
 						dataProvider.getList().clear();
 						dataProvider.getList().addAll(fieldName);
 					}
 				});
 
 		AudioSupervisorSettingsChangedRemoteEvent.subscribe(eventBus, new AudioSupervisorSettingsChangedRemoteEvent.Handler() {
 			
 			@Override
 			public void onAudioSupervisorSettingsChanged(
 					AudioSupervisorSettingsChangedRemoteEvent event) {
 						supervisorAccount = event.getSupervisorSettings();
 			}
 		});
 		
 		MeetMeRemoteEvent.subscribe(eventBus, new MeetMeRemoteEvent.Handler() {
 
 			@Override
 			public void onMeetMeEvent(MeetMeRemoteEvent event) {
				conferenceRooms = event.getConferenceRooms();
 				if (fieldName.size() > 3) {
 					while (fieldName.size() > 3) {
 						fieldName.remove(3);
 						classField.remove(3);
 					}
 				}
 				
 				if (conferenceRooms.conferenceOfActivityExist(voipAccounts.getActivity(ptuId))) {
 					fieldName.add("Conference");
 					classField.add(ButtonCell.class);
 					fieldName.add("Mute/Unmute");
 					classField.add(ButtonCell.class);
 					fieldName.add("Kick/Add");
 					classField.add(ButtonCell.class);
 				}
 				dataProvider.getList().clear();
 				dataProvider.getList().addAll(fieldName);
 			}
 		});
 		
 		return true;
 	}
 
 	@Override
 	public boolean update() {
 		return false;
 	}
 
 }

 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.Conference;
 import ch.cern.atlas.apvs.client.event.AudioSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedEvent.ConnectionType;
 import ch.cern.atlas.apvs.client.event.MeetMeEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.service.AudioServiceAsync;
 import ch.cern.atlas.apvs.client.settings.AudioSettings;
 import ch.cern.atlas.apvs.client.settings.ConferenceRooms;
 import ch.cern.atlas.apvs.client.widget.EditableCell;
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class AudioView extends GlassPanel implements Module {
 
 	private CellTable<String> table = new CellTable<String>();
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	private AudioSettings voipAccounts = new AudioSettings();
 	private ConferenceRooms conferenceRooms = new ConferenceRooms();
 	private Conference conferenceMembers = new Conference();
 	private String ptuId = new String("PTU1234");
 	private static final String SUPERVISOR_ACCOUNT = "SIP/2000";
 	private static final String SUPERVISOR_NUMBER = "2000";
 
 	private EventBus cmdBus;
 
 	private List<String> fieldName = new ArrayList<String>(
 			Arrays.asList(new String[] { "Status", "Private Call", "Group Call" }));
 	private List<Class<?>> classField = new ArrayList<Class<?>>(
 			Arrays.asList(new Class<?>[] { TextCell.class, ButtonCell.class,
 					ButtonCell.class }));
 
 	public AudioView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 		final RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 		cmdBus = clientFactory.getEventBus(args.getArg(0));
 
 		/* 
 		 * TABLE SETUP
 		 * 
 		 	(TextCell)  	 (EditableCell=customizable)	
 		 ___________________________________________________
 		 |  Field Name  | Status/Action Field               |
 		 |______________|___________________________________|
 		 | Status		|  		Online / Offline		    | (textCell field)
 		 | Private Call |    Call/Hangup 'username'         | (buttonCell field) 
 		 | Group Call	| Join/Leave/Create 'IMPACT ACTION' | (buttonCell field)
 		 ****************************************************
 		 * FOLLOWING ROWS ARE DYNAMICALLY ADDED				*
 		 * Mode = On conference								*
 		 ****************************************************
  		 | Conference   | Mute all users(btn) | Close(btn)  |
  		 | 'User 1'     | Mute 'User 1'(btn)  | Kick(btn)   |
  		 | 'User 2'     | Mute 'User 2'(btn)  | Kick(btn)   |
  		 | 'User n'     | Mute 'User n'(btn)  | Kick(btn)   |
  		 |______________|___________________________________|
  
 		 */
 
 		add(table, CENTER);
 
 		// Field Name column
 		Column<String, String> name = new Column<String, String>(new TextCell()) {
 			@Override
 			public String getValue(String field) {
 				return field;
 			}
 		};
 		name.setHorizontalAlignment(ALIGN_CENTER);
 		table.addColumn(name);
 
 		// Status/Action Field column
 		EditableCell fieldCell = new EditableCell(classField);
 		Column<String, Object> field = new Column<String, Object>(fieldCell) {
 			@Override
 			public Object getValue(String fieldName) {
 				if (fieldName.equals("Status"))
 					return voipAccounts.getStatus(ptuId);
 				else if (fieldName.equals("Private Call"))
 					return ((voipAccounts.getOnCall(ptuId) ? "Hangup '"
 							: "Call '") + voipAccounts.getUsername(ptuId) + "'");
 				else if (fieldName.equals("Group Call"))
 					return ((!conferenceRooms
 							.conferenceOfActivityExist(voipAccounts
 									.getActivity(ptuId)) ? ("Create '")
 							: (voipAccounts.getOnConference(voipAccounts
 									.getPtuId(SUPERVISOR_ACCOUNT)) ? "Leave '"
 									: "Join '"))
 							+ voipAccounts.getActivity(ptuId) + "' conference");
 				else if (fieldName.equals("Conference"))
 					return "Close '" + voipAccounts.getActivity(ptuId)
 							+ "' conference";
 				else if (conferenceMembers.containsUsername(fieldName)) {
 					return "BUTTONS OPTION goes here";
 				} else
 					return null;
 			}
 		};
 		field.setHorizontalAlignment(ALIGN_CENTER);
 		table.addColumn(field);
 
 		field.setFieldUpdater(new FieldUpdater<String, Object>() {
 
 			@Override
 			public void update(int index, String fieldName, Object value) {
 
 				AsyncCallback<Void> callbackConference = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("FAIL CONFERENCE ESTABLISHMENT: "
 								+ caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Conference success...");
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
 						System.out.println("Conference success...");
 					}
 				};
 
 				AsyncCallback<Void> callbackHangup = new AsyncCallback<Void>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("FAIL HANGUP: " + caught);
 					}
 
 					@Override
 					public void onSuccess(Void result) {
 						System.out.println("Hangup Success...");
 					}
 				};
 
 				if (fieldName.equals("Private Call")) {
 					if (!voipAccounts.getOnCall(ptuId)) {
 						List<String> channels = new ArrayList<String>();
 						channels.add(voipAccounts.getChannel(voipAccounts
 								.getPtuId(SUPERVISOR_ACCOUNT)));
 						channels.add(voipAccounts.getChannel(ptuId));
						//Hangup Supervisor and PTU User from active calls
 						AudioServiceAsync.Util.getInstance().hangupMultiple(
 								channels, callbackHangup);
 						AudioServiceAsync.Util.getInstance().call(
 								voipAccounts.getNumber(ptuId),
 								SUPERVISOR_NUMBER, callbackCall);
 					} else {
 						AudioServiceAsync.Util.getInstance().hangup(
 								voipAccounts.getChannel(voipAccounts
 										.getPtuId(SUPERVISOR_ACCOUNT)),
 								callbackHangup);
 						if (conferenceRooms
 								.conferenceOfActivityExist(voipAccounts
 										.getActivity(ptuId))) {
 							AudioServiceAsync.Util
 									.getInstance()
 									.addToConference(
 											voipAccounts.getNumber(ptuId),
 											conferenceRooms
 													.roomOfActivity(voipAccounts
 															.getActivity(ptuId)),
 											callbackConference);
 						}
 					}
 				} else if (fieldName.equals("Group Call")) {
 					if (!voipAccounts.getOnConference(voipAccounts
 							.getPtuId(SUPERVISOR_ACCOUNT))) {
 						if (!conferenceRooms
 								.conferenceOfActivityExist(voipAccounts
 										.getActivity(ptuId))) {
							//Hangup Impact Activity Users from active calls
 							AudioServiceAsync.Util
 									.getInstance()
 									.hangupMultiple(
 											voipAccounts
 													.getActiveChannelsActivity(voipAccounts
 															.getActivity(ptuId)),
 											callbackHangup);
 							AudioServiceAsync.Util.getInstance().newConference(
 									voipAccounts
 											.getNumbersActivity(voipAccounts
 													.getActivity(ptuId)),
 									callbackConference);
 						} else {
							//Hangup Supervisor from active calls
 							AudioServiceAsync.Util.getInstance().hangup(
 									voipAccounts.getChannel(voipAccounts
 											.getPtuId(SUPERVISOR_ACCOUNT)),
 									callbackHangup);
 							AudioServiceAsync.Util.getInstance()
 									.addToConference(SUPERVISOR_ACCOUNT,
 											voipAccounts.getRoom(ptuId),
 											callbackConference);
 						}
 					} else {
 						AudioServiceAsync.Util.getInstance().hangup(
 								voipAccounts.getChannel(voipAccounts
 										.getPtuId(SUPERVISOR_ACCOUNT)),
 								callbackHangup);
 					}
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
 					System.err.println("NEW PTU " + ptuId);
 					table.redraw();
 				}
 			});
 		}
 
 		ConnectionStatusChangedEvent.subscribe(eventBus,
 				new ConnectionStatusChangedEvent.Handler() {
 
 					@Override
 					public void onConnectionStatusChanged(
 							ConnectionStatusChangedEvent event) {
 						if (event.getConnection() == ConnectionType.audio) {
 							showGlass(!event.isOk());
 						}
 					}
 				});
 
 		AudioSettingsChangedEvent.subscribe(eventBus,
 				new AudioSettingsChangedEvent.Handler() {
 
 					@Override
 					public void onAudioSettingsChanged(
 							AudioSettingsChangedEvent event) {
 						voipAccounts = event.getAudioSettings();
 
 						dataProvider.getList().clear();
 						dataProvider.getList().addAll(fieldName);
 					}
 				});
 
 		MeetMeEvent.subscribe(eventBus, new MeetMeEvent.Handler() {
 
 			@Override
 			public void onMeetMeEvent(MeetMeEvent event) {
 				if (fieldName.size() > 3) {
 					while (fieldName.size() > 3) {
 						fieldName.remove(3);
 						classField.remove(3);
 					}
 				}
 
 				conferenceRooms = event.getConferenceRooms();
 				System.out.println(conferenceRooms
 						.conferenceOfActivityExist(voipAccounts
 								.getActivity(ptuId)));
 				if (conferenceRooms.conferenceOfActivityExist(voipAccounts
 						.getActivity(ptuId))) {
 					fieldName.add("Conference");
 					classField.add(ButtonCell.class);
 
 					conferenceMembers = conferenceRooms.get(conferenceRooms
 							.roomOfActivity(voipAccounts.getActivity(ptuId)));
 					for (int i = 0; i < conferenceMembers.getUserNum(); i++) {
 						fieldName.add(conferenceMembers.getUsernames().get(i));
 						classField.add(ButtonCell.class);
 					}
 					dataProvider.getList().clear();
 					dataProvider.getList().addAll(fieldName);
 				}
 			}
 		});
 
 		return true;
 
 	}
 
 	@Override
 	public boolean update() {
 		return false;
 	}
 
 }

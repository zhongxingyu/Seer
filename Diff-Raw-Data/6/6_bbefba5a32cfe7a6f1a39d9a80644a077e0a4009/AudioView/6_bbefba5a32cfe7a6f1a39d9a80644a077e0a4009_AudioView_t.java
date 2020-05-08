 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.AudioSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.service.AudioServiceAsync;
 import ch.cern.atlas.apvs.client.settings.AudioSettings;
 import ch.cern.atlas.apvs.client.widget.EditableCell;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.CompositeCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class AudioView extends VerticalPanel implements Module {
 
 	private CellTable<String> table = new CellTable<String>();
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	private AudioSettings voipAccounts = new AudioSettings();
 	private String ptuId = new String("PTU1234");
 	private static final String SUPERVISOR_ACCOUNT = "SIP/2000";
 	private static final String SUPERVISOR_NUMBER = "2000";
 
 	private EventBus cmdBus;
 
 	private List<String> fieldName = new ArrayList<String>(Arrays.asList(new String[] { "Status",
 			"Private Call", "Group Call", "Establish Conference Call" }));
 	//private List<String> fieldName2 = new ArrayList<String>(fieldName); 
 	//private List<String> fieldName = Arrays.asList(new String[] { "Status", "Private Call", "Group Call", "New Group Call" });
 	private List<Class<?>> classField = Arrays.asList(new Class<?>[] {
 			TextCell.class, ButtonCell.class, ButtonCell.class,
 			ButtonCell.class, CompositeCell.class, CompositeCell.class});
 
 	public AudioView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,Arguments args) {
 		final RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 
 		cmdBus = clientFactory.getEventBus(args.getArg(0));
 
 		add(table);		
 		
 		// Field Name column
 		Column<String, String> name = new Column<String, String>(new TextCell()) {
 
 			@Override
 			public String getValue(String field) {
 				return field;
 			}
 		};
 		name.setHorizontalAlignment(ALIGN_CENTER);
 		table.addColumn(name);
 
 		// Field
 		EditableCell fieldCell = new EditableCell(classField);
 
 		Column<String, Object> field = new Column<String, Object>(fieldCell) {
 
 			@Override
 			public Object getValue(String fieldName) {
 				if (fieldName.equals("Status"))
 					return voipAccounts.getStatus(ptuId);
 				else if (fieldName.equals("Private Call"))
 					return ((voipAccounts.getOnCall(ptuId)?"Hangup '":"Call '") + voipAccounts.getUsername(ptuId) + "'");
 				else if (fieldName.equals("Group Call"))
 					return ((voipAccounts.getOnConference(voipAccounts.getPtuId(voipAccounts,SUPERVISOR_ACCOUNT))?"Leave '":"Join '") + voipAccounts.getActivity(ptuId) + "'");
 				else if (fieldName.equals("Establish Conference Call"))
 					return "Create '" + voipAccounts.getActivity(ptuId) + "' conference";
 				else if (voipAccounts.getOnCall(ptuId))
 					return "Info goes here";
 				else
 					return null;
 			}
 		};
 
 	
 		field.setFieldUpdater(new FieldUpdater<String, Object>() {
 
 			@Override
 			public void update(int index, String fieldName, Object value) {
 				if (fieldName.equals("Private Call")) {
 					if (voipAccounts.getOnCall(ptuId)) {
 						AudioServiceAsync.Util.getInstance().hangup(voipAccounts.getChannel(voipAccounts.getPtuId(voipAccounts, SUPERVISOR_ACCOUNT)),
 								new AsyncCallback<Void>() {
 									@Override
 									public void onSuccess(Void result) {
 										System.err.println("Hangup Success...");
 									}
 
 									@Override
 									public void onFailure(Throwable caught) {
 										System.err.println("Fail to established the hangup call "+ caught);
 									}
 								});
 
 					}else {
 						AudioServiceAsync.Util.getInstance().call(
 								voipAccounts.getNumber(ptuId), SUPERVISOR_NUMBER,
 								new AsyncCallback<Void>() {
 
 									@Override
 									public void onSuccess(Void result) {
 										System.err.println("Call Established...");
 									}
 
 									@Override
 									public void onFailure(Throwable caught) {
 										System.err.println("Fail to established the call "+ caught);
 									}
 								});
 					}
 				} else if (fieldName.equals("Group Call")) {
 					//if(voipAccounts.getOnConference(voipAccounts.getPtuId(voipAccounts, SUPERVISOR_ACCOUNT))){
 							AudioServiceAsync.Util.getInstance().addToConference(SUPERVISOR_ACCOUNT, voipAccounts.getRoom(ptuId),new AsyncCallback<Void>() {
 
 								@Override
 								public void onSuccess(Void result) {
 									System.err.println("Added to conference Call...");
 								}
 
 								@Override
 								public void onFailure(Throwable caught) {
 									System.err.println("Fail to join the conference "+ caught);
 								}
 							});
 					//}
 				} else if (fieldName.equals("Establish Conference Call")) {
 					AudioServiceAsync.Util.getInstance().newConference(voipAccounts.getNumbersActivity(voipAccounts, voipAccounts.getActivity(ptuId)),
 							new AsyncCallback<Void>() {
 
 								@Override
 								public void onSuccess(Void result) {
 									System.err.println("Conference Call Establish...");
 								}
 
 								@Override
 								public void onFailure(Throwable caught) {
 									System.err
 											.println("Fail to established the conference call " + caught);
 								}
 							});
 
 				} else
 					return;
 			}
 		});
 
 		field.setHorizontalAlignment(ALIGN_CENTER);
 		table.addColumn(field);
 
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
 
 		AudioSettingsChangedEvent.subscribe(eventBus, new AudioSettingsChangedEvent.Handler() {
 
 					@Override
 					public void onAudioSettingsChanged(AudioSettingsChangedEvent event) {
 						voipAccounts = event.getAudioSettings();
 						//System.out.println("lasdasdadsasdasdad "+fieldName.get(0)+" asdad "+ fieldName);
 						if(fieldName.size()>4){
 							while(fieldName.size()>4){
 								//System.out.println(fieldName.get(4));
 								fieldName.remove(4);// = fieldName.subList(0, 4);
 							}
 						}
 						//System.out.println("lasdasdadsasdasdad "+fieldName.get(0)+" asdad "+ fieldName);
 						//System.out.println("AI "+!voipAccounts.getDestPtu(ptuId).isEmpty());
 						if(!voipAccounts.getDestPtu(ptuId).isEmpty()){
 							//System.out.println("fieldName2 "+voipAccounts.getDestPtu(ptuId));
 							String[] ptuList = voipAccounts.getDestPtu(ptuId).split(",");
 							//System.out.println("voipAccount "+voipAccounts.getPtuIds().size());
 							//System.out.println("ptuList "+ptuList.length);
 							fieldName.add((voipAccounts.getOnCall(ptuId)?"Call with":"Conference with"));
 							for(int i = 0; i < ptuList.length; i++){
 							//	System.out.println("PTU ID = "+(ptuList[i]));
 								fieldName.add(voipAccounts.getUsername(ptuList[i]));
 								//System.out.println(voipAccounts.getUsername(ptuList[i]));
 							}
 							//System.out.println("fieldName "+fieldName);
 						}
 						dataProvider.getList().clear();
 						dataProvider.getList().addAll(fieldName);
 						//table.redraw();
 					}
 				});
 		
 		return true;
 
 	}
 
	@Override
	public boolean update() {
		// TODO Auto-generated method stub
		return false;
	}

 }

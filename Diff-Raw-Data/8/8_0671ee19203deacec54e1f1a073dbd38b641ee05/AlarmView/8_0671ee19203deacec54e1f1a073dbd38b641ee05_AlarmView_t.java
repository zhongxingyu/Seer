 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Arrays;
 import java.util.List;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.AlarmMap;
 import ch.cern.atlas.apvs.client.domain.Ternary;
 import ch.cern.atlas.apvs.client.event.AlarmMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.EditableCell;
 import ch.cern.atlas.apvs.client.widget.GenericColumn;
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class AlarmView extends GlassPanel implements Module {
 
 	private CellTable<String> table = new CellTable<String>();
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 
 	private EventBus cmdBus;
 
 	private String ptuId;
 	private Ternary daqOk = Ternary.Unknown;
 
 	private boolean showHeader = true;
 
 	private String options;
 	private List<String> names = Arrays.asList(new String[] { "Panic Button",
 			"Dose Rate", "Fall Detect" });
 	private List<Class<?>> classes = Arrays.asList(new Class<?>[] {
 			ButtonCell.class, ButtonCell.class, ButtonCell.class });
 
 	private AlarmMap alarms;
 
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	private final static String ALARM = "alarm";
 	private final static String CLEARED = "cleared";
 	private final static String UNKNOWN = "unknown";
 	
 	public AlarmView() {
 	}
 
 	@Override
 	public boolean configure(Element element,
 			final ClientFactory clientFactory, Arguments args) {
 
 		RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 
 		cmdBus = clientFactory.getEventBus(args.getArg(0));
 		options = args.getArg(1);
 
 		showHeader = !options.contains("NoHeader");
 
 		add(table);
 
 		// name column
 		ClickableHtmlColumn<String> name = new ClickableHtmlColumn<String>() {
 			@Override
 			public String getValue(String name) {
 				return name;
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		table.addColumn(name, showHeader ? new TextHeader("Name") : null);
 
 		EditableCell cell = new EditableCell(classes, 50);
 
 		GenericColumn<String> column = new GenericColumn<String>(cell) {
 			@Override
 			public Object getValue(String name) {
 				if (name.equals(names.get(0))) {
 					return alarms != null && alarms.isPanic(ptuId) ? "ALARM"
 							: "Cleared";
 				} else if (name.equals(names.get(1))) {
 					return alarms != null && alarms.isDose(ptuId) ? "ALARM"
 							: "Cleared";
 				} else if (name.equals(names.get(2))) {
 					return alarms != null && alarms.isFall(ptuId) ? "ALARM"
 							: "Cleared";
 				}
 				System.out.println("AlarmView name unknown '" + name + "'");
 				return "unknown";
 			}
 
 			@Override
 			public void render(Context context, String name, SafeHtmlBuilder sb) {
				String status;
 				if (name.equals(names.get(0))) {
 					status = alarms != null && alarms.isPanic(ptuId) ? ALARM
 							: CLEARED;
 				} else if (name.equals(names.get(1))) {
 					status = alarms != null && alarms.isDose(ptuId) ? ALARM
 							: CLEARED;
 				} else if (name.equals(names.get(2))) {
 					status = alarms != null && alarms.isFall(ptuId) ? ALARM
 							: CLEARED;
 				} else {
 					status = UNKNOWN;
 				}
 			
 				sb.append(SafeHtmlUtils.fromSafeConstant("<div class=\""
 						+ status + "\">"));
 
 				getCell().render(context, getValue(name), sb);
 
 				sb.append(SafeHtmlUtils.fromSafeConstant("</div>"));				
 			}
 		};
 		column.setEnabled(clientFactory.isSupervisor());
 		column.setFieldUpdater(new FieldUpdater<String, Object>() {
 
 			@Override
 			public void update(int index, String name, Object value) {
 				if (!clientFactory.isSupervisor()) {
 					return;
 				}
 				
 				if (name.equals(names.get(0))) {
 					clientFactory.getPtuService().clearPanicAlarm(ptuId,
 							new AsyncCallback<Void>() {
 
 								@Override
 								public void onFailure(Throwable caught) {
 									// FIXME
 								}
 
 								@Override
 								public void onSuccess(Void result) {
 								}
 							});
 				} else if (name.equals(names.get(1))) {
 					clientFactory.getPtuService().clearDoseAlarm(ptuId,
 							new AsyncCallback<Void>() {
 
 								@Override
 								public void onFailure(Throwable caught) {
 									// FIXME
 								}
 
 								@Override
 								public void onSuccess(Void result) {
 								}
 							});
 				} else if (name.equals(names.get(2))) {
 					clientFactory.getPtuService().clearFallAlarm(ptuId,
 							new AsyncCallback<Void>() {
 
 								@Override
 								public void onFailure(Throwable caught) {
 									// FIXME
 								}
 
 								@Override
 								public void onSuccess(Void result) {
 								}
 							});
 				}
 			}
 		});
 		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		table.addColumn(column, showHeader ? new TextHeader("Value") : null);
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(names);
 
 		ConnectionStatusChangedRemoteEvent.subscribe(eventBus,
 				new ConnectionStatusChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onConnectionStatusChanged(
 							ConnectionStatusChangedRemoteEvent event) {
 						switch (event.getConnection()) {
 						case daq:
 							daqOk = event.getStatus();
 							break;
 						default:
 							return;
 						}
 
 						showGlass(daqOk.not().isTrue());
 					}
 				});
 
 		AlarmMapChangedRemoteEvent.subscribe(eventBus,
 				new AlarmMapChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onAlarmMapChanged(
 							AlarmMapChangedRemoteEvent event) {
 						alarms = event.getAlarmMap();
 						System.err.println("Alarms changed " + alarms);
 						scheduler.update();
 					}
 				});
 
 		SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 			@Override
 			public void onPtuSelected(SelectPtuEvent event) {
 				ptuId = event.getPtuId();
 				scheduler.update();
 			}
 		});
 
 		return true;
 	}
 
 	@Override
 	public boolean update() {
 		table.redraw();
 
 		return false;
 	}
 
 }

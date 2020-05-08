 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.Intervention;
 import ch.cern.atlas.apvs.client.domain.InterventionMap;
 import ch.cern.atlas.apvs.client.domain.Ternary;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent.ConnectionType;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.DurationCell;
 import ch.cern.atlas.apvs.client.widget.EditableCell;
import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.DateCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.web.bindery.event.shared.EventBus;
 
public class GeneralInfoView extends GlassPanel implements Module {
 
 	@SuppressWarnings("unused")
 	private Logger log = LoggerFactory.getLogger(getClass());
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	private CellTable<String> table = new CellTable<String>();
 
 	private EventBus cmdBus;
 
 	private String ptuId;
 	private Ternary audioOk = Ternary.Unknown;
 	private Ternary videoOk = Ternary.Unknown;
 	private Ternary daqOk = Ternary.Unknown;
 	private Ternary dosimeterOk = Ternary.Unknown;
 	private Ternary databaseOk = Ternary.Unknown;
 	private InterventionMap interventions;
 
 	private boolean showHeader = true;
 
 	private String options;
 
 	private List<String> names = Arrays.asList(new String[] {
 			ConnectionType.audio.getString(), // ConnectionType.video.getString(),
 			ConnectionType.daq.getString(),
 			ConnectionType.dosimeter.getString(),
 			ConnectionType.database.getString(), "Start Time", "Duration" });
 	private List<Class<?>> classes = Arrays.asList(new Class<?>[] {
 			TextCell.class, /* TextCell.class, */ TextCell.class,
 			TextCell.class, TextCell.class, DateCell.class,
 			DurationCell.class });
 
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	public GeneralInfoView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 
 		cmdBus = clientFactory.getEventBus(args.getArg(0));
 		options = args.getArg(1);
 
 		showHeader = !options.contains("NoHeader");
 
 		table.setWidth("100%");
 		
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
 		cell.setDateFormat(PtuClientConstants.dateFormatNoSeconds);
 		cell.setEnabled(false);
 		Column<String, Object> column = new Column<String, Object>(cell) {
 			@Override
 			public Object getValue(String name) {
 				if (name.equals(ConnectionType.audio.getString())) {
 					return audioOk;
 				} else if (name.equals(ConnectionType.video.getString())) {
 					return videoOk;
 				} else if (name.equals(ConnectionType.daq.getString())) {
 					return daqOk;
 				} else if (name.equals(ConnectionType.dosimeter.getString())) {
 					return dosimeterOk;
 				} else if (name.equals(ConnectionType.database.getString())) {
 					return databaseOk;
 				} else if (name.equals("Start Time")) {
 					return getStartTime();
 				} else if (name.equals("Duration")) {
 					Date startTime = getStartTime();
 					return startTime != null ? new Date().getTime()
 							- startTime.getTime() : null;
 				}
 				System.out.println("GeneralInfoView name unknown '" + name
 						+ "'");
 				return null;
 			}
 
 			@Override
 			public void render(Context context, String name, SafeHtmlBuilder sb) {
 				Object s = getValue(name);
 				if (s instanceof Ternary) {
 					Ternary t = (Ternary)s;
 					s =  t.isTrue() ? "Ok" : t.isFalse() ? "Fail" : "Unknown";
 				}
 				getCell().render(context, s, sb);
 			}
 		};
 		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		table.addColumn(column, showHeader ? new TextHeader("Value") : null);
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(names);
 
 		ConnectionStatusChangedRemoteEvent.subscribe(
 				clientFactory.getRemoteEventBus(),
 				new ConnectionStatusChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onConnectionStatusChanged(
 							ConnectionStatusChangedRemoteEvent event) {
 						switch (event.getConnection()) {
 						case audio:
 							audioOk = event.getStatus();
 							break;
 						case video:
 							// FIXME #192, not sent yet
 							videoOk = event.getStatus();
 							break;
 						case daq:
 							daqOk = event.getStatus();
 							break;
 						case dosimeter:
 							dosimeterOk = event.getStatus();
 							break;
 						case database:
 							databaseOk = event.getStatus();
 							break;
 						default:
 							return;
 						}
 						scheduler.update();
 					}
 				});
 
 		InterventionMapChangedRemoteEvent.subscribe(
 				clientFactory.getRemoteEventBus(),
 				new InterventionMapChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onInterventionMapChanged(
 							InterventionMapChangedRemoteEvent event) {
 						interventions = event.getInterventionMap();
 						scheduler.update();
 					}
 				});
 
 		if (cmdBus != null) {
 			SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 				@Override
 				public void onPtuSelected(SelectPtuEvent event) {
 					ptuId = event.getPtuId();
 					scheduler.update();
 				}
 			});
 		}
 
 		return true;
 	}
 
 	private Date getStartTime() {
 		if (ptuId == null) {
 			return null;
 		}
 
 		if (interventions == null) {
 			return null;
 		}
 
 		Intervention intervention = interventions.get(ptuId);
 		if (intervention == null) {
 			return null;
 		}
 
 		return intervention.getStartTime();
 	}
 
 	@Override
 	public boolean update() {
 		table.redraw();
 		return false;
 	}
 
 }

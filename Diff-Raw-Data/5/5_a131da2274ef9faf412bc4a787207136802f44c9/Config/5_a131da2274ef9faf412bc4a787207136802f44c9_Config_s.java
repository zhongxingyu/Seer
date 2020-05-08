 package ch.bergturbenthal.hs485.frontend.gwtfrontend.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.shared.OutputDevice;
 import ch.bergturbenthal.hs485.frontend.gwtfrontend.shared.OutputDevice.Type;
 
 import com.google.gwt.cell.client.EditTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.SelectionCell;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.MenuItem;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Config implements EntryPoint {
 
 	private final ConfigServiceAsync	configService	= ConfigServiceAsync.Util.getInstance();
 	private final Messages						messages			= GWT.create(Messages.class);
 	private CellTable<OutputDevice>		outputDeviceCellTable;
 	private ArrayList<OutputDevice>		outputDeviceList;
 
 	private void addNameColumn(final CellTable<OutputDevice> cellTable) {
 		final Column<OutputDevice, String> nameColumn = new Column<OutputDevice, String>(new EditTextCell()) {
 			@Override
 			public String getValue(final OutputDevice object) {
 				return object.getName();
 			}
 		};
 		nameColumn.setFieldUpdater(new FieldUpdater<OutputDevice, String>() {
 
 			public void update(final int index, final OutputDevice device, final String value) {
 				device.setName(value);
 				updateDevice(device);
 			}
 		});
 		cellTable.addColumn(nameColumn, "Name");
 	}
 
 	private void addTypeColumn(final CellTable<OutputDevice> cellTable) {
 		final ArrayList<String> options = new ArrayList<String>();
 		for (final Type type : Type.values())
 			options.add(type.name());
 
 		final Column<OutputDevice, String> typeColumn = new Column<OutputDevice, String>(new SelectionCell(options)) {
 			@Override
 			public String getValue(final OutputDevice object) {
 				return object.getType().name();
 			}
 		};
 		typeColumn.setFieldUpdater(new FieldUpdater<OutputDevice, String>() {
 
 			public void update(final int index, final OutputDevice device, final String value) {
 				device.setType(Type.valueOf(value));
 				updateDevice(device);
 
 			}
 		});
 		cellTable.addColumn(typeColumn, "Type");
 	}
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 		// Add the nameField and sendButton to the RootPanel
 		// Use RootPanel.get() to get the entire body element
 		final RootPanel rootPanel = RootPanel.get("main");
 
 		final DockPanel dockPanel = new DockPanel();
 		dockPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		dockPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		rootPanel.add(dockPanel);
 		dockPanel.setSize("100%", "100%");
 
 		final MenuBar menuBar = new MenuBar(false);
 		dockPanel.add(menuBar, DockPanel.NORTH);
 		final MenuBar menuBar_1 = new MenuBar(true);
 
 		final MenuItem mntmFile = new MenuItem(messages.mntmFile_text(), false, menuBar_1);
 		menuBar.addItem(mntmFile);
 
 		final Grid grid = new Grid(3, 3);
 		dockPanel.add(grid, DockPanel.CENTER);
 
 		outputDeviceCellTable = new CellTable<OutputDevice>();
 		grid.setWidget(1, 1, outputDeviceCellTable);
 		addNameColumn(outputDeviceCellTable);
 		addTypeColumn(outputDeviceCellTable);
 
 		outputDeviceList = new ArrayList<OutputDevice>();
 		reloadOutputDeviceList();
 		// cellTable.setRowData(values);
 
 		final Button newButton = new Button(messages.addOutputDeviceEnry());
 		grid.setWidget(1, 2, newButton);
 		newButton.addClickHandler(new ClickHandler() {
 
 			public void onClick(final ClickEvent event) {
 				final OutputDevice device = new OutputDevice();
 				device.setName("Hello");
 				device.setType(Type.DIMMER);
 				configService.addOutputDevice(device, new AsyncCallback<Void>() {
 
 					public void onFailure(final Throwable caught) {
 						GWT.log("Error", caught);
 					}
 
 					public void onSuccess(final Void result) {
 						reloadOutputDeviceList();
 					}
 				});
 				// outputDeviceList.add(device);
 				// outputDeviceCellTable.setRowData(outputDeviceList);
 			}
 		});
 
 		// Create the popup dialog box
 		final DialogBox dialogBox = new DialogBox();
 		dialogBox.setText("Remote Procedure Call");
 		dialogBox.setAnimationEnabled(true);
 		final Button closeButton = new Button("Close");
 		// We can set the id of a widget by accessing its Element
 		closeButton.getElement().setId("closeButton");
 		final Label textToServerLabel = new Label();
 		final HTML serverResponseLabel = new HTML();
 		final VerticalPanel dialogVPanel = new VerticalPanel();
 		dialogVPanel.addStyleName("dialogVPanel");
 		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
 		dialogVPanel.add(textToServerLabel);
 		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
 		dialogVPanel.add(serverResponseLabel);
 		dialogVPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		dialogVPanel.add(closeButton);
 		dialogBox.setWidget(dialogVPanel);
 
 	}
 
 	/**
 	 * 
 	 */
 	private void reloadOutputDeviceList() {
		configService.getOutputDevices(new AsyncCallback<List>() {
 
 			public void onFailure(final Throwable caught) {
 				GWT.log("Error", caught);
 			}
 
			public void onSuccess(final List result) {
 				outputDeviceList.clear();
 				outputDeviceList.addAll(result);
 				outputDeviceCellTable.setRowData(outputDeviceList);
 			}
 		});
 	}
 
 	/**
 	 * @param device
 	 */
 	private void updateDevice(final OutputDevice device) {
 		configService.updateOutputDevice(device, new AsyncCallback<Void>() {
 
 			public void onFailure(final Throwable caught) {
 				// TODO Auto-generated method stub
 			}
 
 			public void onSuccess(final Void result) {
 				reloadOutputDeviceList();
 			}
 		});
 	}
 }

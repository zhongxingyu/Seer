 package ch.cern.atlas.apvs.client;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import ch.cern.atlas.apvs.domain.Dosimeter;
 import ch.cern.atlas.apvs.domain.Measurement;
 
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.NumberCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.view.client.ListDataProvider;
 
 public class MeasurementView extends SimplePanel {
 
 	private static final int NUMBER_OF_ITEMS = 2;
 	private ListDataProvider<Measurement<?>> dataProvider = new ListDataProvider<Measurement<?>>();
 	private CellTable<Measurement<?>> table = new CellTable<Measurement<?>>();
 	private ListHandler<Measurement<?>> columnSortHandler;
 
 	private DosimeterServiceAsync dosimeterService = GWT
 			.create(DosimeterService.class);
 	private Dosimeter dosimeter = new Dosimeter();
 	private int serialNo = 0;
 
 	public MeasurementView(DosimeterSelector dosimeterSelector) {
 		dosimeterSelector.addChangeHandler(new ChangeHandler() {
 
 			@Override
 			public void onChange(ChangeEvent event) {
 				// Maybe use something in DosimeterSelector
 				ListBox list = (ListBox) event.getSource();
 				try {
 					serialNo = Integer.parseInt(list.getItemText(list
 							.getSelectedIndex()));
 				} catch (NumberFormatException e) {
 					serialNo = 0;
 				}
 				getDosimeter();
 			}
 		});
 
 		add(table);
 
 		TextColumn<Measurement<?>> name = new TextColumn<Measurement<?>>() {
 			@Override
 			public String getValue(Measurement<?> object) {
 				return object.getName();
 			}
 
 			@Override
 			public void render(Context context, Measurement<?> object,
 					SafeHtmlBuilder sb) {
 				((TextCell) getCell()).render(context,
 						SafeHtmlUtils.fromSafeConstant(getValue(object)), sb);
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		name.setSortable(true);
 		table.addColumn(name, "Name");
 
 		Column<Measurement<?>, Number> value = new Column<Measurement<?>, Number>(
 				new NumberCell(NumberFormat.getFormat("0.0"))) {
 			@Override
 			public Number getValue(Measurement<?> object) {
 				// FIXME what about arrays
 				return (Number)object.getValue();
 			}
 		};
 		value.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		value.setSortable(true);
 		table.addColumn(value, "Value");
 
 		TextColumn<Measurement<?>> unit = new TextColumn<Measurement<?>>() {
 			@Override
 			public String getValue(Measurement<?> object) {
 				return object.getUnit();
 			}
 
 			@Override
 			public void render(Context context, Measurement<?> object,
 					SafeHtmlBuilder sb) {
 				((TextCell) getCell()).render(context,
 						SafeHtmlUtils.fromSafeConstant(getValue(object)), sb);
 			}
 		};
 		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		unit.setSortable(true);
 		table.addColumn(unit, "Unit");
 
 		List<Measurement<?>> list = new ArrayList<Measurement<?>>();
 		for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
 			list.add(new Measurement<Object>());
 		}
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(list);
 
 		columnSortHandler = new ListHandler<Measurement<?>>(dataProvider.getList());
 		columnSortHandler.setComparator(name, new Comparator<Measurement<? extends Object>>() {
 			public int compare(Measurement<? extends Object> o1, Measurement<? extends Object> o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getName().compareTo(o2.getName()) : 1;
 				}
 				return -1;
 			}
 		});
		columnSortHandler.setComparator(value, new Comparator<Measurement<? extends Object>>() {
			public int compare(Measurement<? extends Object> o1, Measurement<? extends Object> o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if ((o1 != null) && (o1.getValue() instanceof Comparable<?>)) {
					Comparable<?> value = (Comparable<?>)o1.getValue();
 					return (o2 != null) ? value.compareTo(o2.getValue()) : 1;
 				}
 				return -1;
 			}
 		});
 		columnSortHandler.setComparator(unit, new Comparator<Measurement<? extends Object>>() {
 			public int compare(Measurement<? extends Object> o1, Measurement<? extends Object> o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getUnit().compareTo(o2.getUnit())
 							: 1;
 				}
 				return -1;
 			}
 		});
 		table.addColumnSortHandler(columnSortHandler);
 		table.getColumnSortList().push(name);
 
 		getDosimeter();
 	}
 
 	private void getDosimeter() {
 		dosimeterService.getDosimeter(serialNo, dosimeter.hashCode(),
 				new AsyncCallback<Dosimeter>() {
 
 					@Override
 					public void onSuccess(Dosimeter result) {
 						if (result == null) {
 							System.err
 									.println("FIXME onSuccess null in measurementView");
 							return;
 						}
 
 						if (serialNo != result.getSerialNo()) {
 							System.err.println("SerialNo "
 									+ result.getSerialNo() + " != " + serialNo
 									+ ", abandoned");
 							return;
 						}
 
 						dosimeter = result;
 
 						List<Measurement<?>> list = dataProvider.getList();
 						list.set(0, new Measurement<Double>("Dose",
 								dosimeter.getDose(), "&micro;Sv"));
 						list.set(1, new Measurement<Double>("Rate",
 								dosimeter.getRate(), "&micro;Sv/h"));
 
 						// Resort the table
 						ColumnSortEvent.fire(table, table.getColumnSortList());
 						table.redraw();
 
 						getDosimeter();
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						GWT.log("Could not retrieve dosimeter");
 
 						getDosimeter();
 					}
 				});
 
 	}
 
 }

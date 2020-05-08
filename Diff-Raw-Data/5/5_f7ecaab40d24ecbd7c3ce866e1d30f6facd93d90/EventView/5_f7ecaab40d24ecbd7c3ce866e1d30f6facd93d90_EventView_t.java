 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.event.SelectTabEvent;
 import ch.cern.atlas.apvs.client.service.EventServiceAsync;
 import ch.cern.atlas.apvs.client.service.SortOrder;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.ptu.shared.EventChangedEvent;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.ColumnSortList;
 import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
 import com.google.gwt.user.cellview.client.DataGrid;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.Range;
 import com.google.gwt.view.client.RangeChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class EventView extends SimplePanel implements Module {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private EventBus cmdBus;
 	private String ptuId;
 	private String measurementName;
 
 	private DataGrid<Event> table = new DataGrid<Event>();
 
 	private String ptuHeader;
 	private ClickableTextColumn<Event> ptu;
 	private String nameHeader;
 	private ClickableHtmlColumn<Event> name;
 
 	private Map<String, String> units = new HashMap<String, String>();
 
 	private boolean selectable = false;
 	private boolean sortable = true;
 
 	public EventView() {
 	}
 	
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory, Arguments args) {
 
 
 		String height = args.getArg(0);
 
 		if (args.size() > 1) {
 			cmdBus = clientFactory.getEventBus(args.getArg(1));
 		}
 
 		table.setSize("100%", height);
 		table.setEmptyTableWidget(new Label("No Events"));
 
 		add(table);
 
 		AsyncDataProvider<Event> dataProvider = new AsyncDataProvider<Event>() {
 
 			@SuppressWarnings("unchecked")
 			@Override
 			protected void onRangeChanged(HasData<Event> display) {
 				EventServiceAsync.Util.getInstance().getRowCount(new AsyncCallback<Integer>() {
 					
 					@Override
 					public void onSuccess(Integer result) {
 						table.setRowCount(result);
 					}
 					
 					@Override
 					public void onFailure(Throwable caught) {
 						table.setRowCount(0);
 					}
 				});
 				
 				final Range range = display.getVisibleRange();
 				System.err.println(range);
 
 				final ColumnSortList sortList = table.getColumnSortList();
 				SortOrder[] order = new SortOrder[sortList.size()];
 				for (int i=0; i<sortList.size(); i++) {
 					ColumnSortInfo info = sortList.get(i);
 					// FIXME #88 remove cast
 					order[i] = new SortOrder(((ClickableTextColumn<Event>)info.getColumn()).getDataStoreName(), info.isAscending());
 				}
 				
 				if (order.length == 0) {
 					order = new SortOrder[1];
					order[0] = new SortOrder("tbl_events.datetime", false);
 				} 	
 				
 				EventServiceAsync.Util.getInstance().getTableData(range, order, new AsyncCallback<List<Event>>() {
 					
 					@Override
 					public void onSuccess(List<Event> result) {
 						System.err.println("RPC DB SUCCESS");
 						table.setRowData(range.getStart(), result);
 					}
 					
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("RPC DB FAILED");
 						table.setRowCount(0);
 					}
 				});
 			}
 		};
 
 		// Table
 		dataProvider.addDataDisplay(table);
 
 		AsyncHandler columnSortHandler = new AsyncHandler(table);
 		table.addColumnSortHandler(columnSortHandler);
 		
 		// Subscriptions
 		EventChangedEvent.subscribe(clientFactory.getRemoteEventBus(),
 				new EventChangedEvent.Handler() {
 
 					@Override
 					public void onEventChanged(EventChangedEvent e) {
 						Event event = e.getEvent();
 						if (event == null)
 							return;
 
 						if (((ptuId == null) || event.getPtuId().equals(ptuId))
 								&& ((measurementName == null) || event
 										.getName().equals(measurementName))) {
 							update();
 						}
 					}
 				});
 
 		MeasurementChangedEvent.subscribe(clientFactory.getRemoteEventBus(),
 				new MeasurementChangedEvent.Handler() {
 
 					@Override
 					public void onMeasurementChanged(
 							MeasurementChangedEvent event) {
 						Measurement m = event.getMeasurement();
 						units.put(unitKey(m.getPtuId(), m.getName()),
 								m.getUnit());
 
 						update();
 					}
 				});
 
 		if (cmdBus != null) {
 			SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 				@Override
 				public void onPtuSelected(SelectPtuEvent event) {
 					ptuId = event.getPtuId();
 
 //					dataProvider.getList().clear();
 
 					update();
 				}
 			});
 
 			SelectMeasurementEvent.subscribe(cmdBus,
 					new SelectMeasurementEvent.Handler() {
 
 						@Override
 						public void onSelection(SelectMeasurementEvent event) {
 							measurementName = event.getName();
 
 //							dataProvider.getList().clear();
 
 							update();
 						}
 					});
 			
 			// FIXME #189
 			SelectTabEvent.subscribe(cmdBus, new SelectTabEvent.Handler() {
 				
 				@Override
 				public void onTabSelected(SelectTabEvent event) {
 					if (event.getTab().equals("Summary")) {
 						update();
 					}
 				}
 			});
 		}
 
 		// DATE and TIME (1)
 		ClickableTextColumn<Event> date = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return PtuClientConstants.dateFormat.format(object.getDate());
 			}
 			
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.datetime";
 			}
 		};
 		date.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		date.setSortable(sortable);
 		if (selectable) {
 			date.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(date, "Date / Time");
		// desc sort, push twice
		table.getColumnSortList().push(date);
 		table.getColumnSortList().push(date);
 
 		// PtuID (2)
 		ptu = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getPtuId();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_devices.name";
 			}
 		};
 		ptu.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		ptu.setSortable(sortable);
 		if (selectable) {
 			ptu.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		ptuHeader = "PTU ID";
 		table.addColumn(ptu, ptuHeader);
 
 		// Name (3)
 		name = new ClickableHtmlColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getName();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.sensor";
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		name.setSortable(sortable);
 		if (selectable) {
 			name.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		nameHeader = "Name";
 		table.addColumn(name, nameHeader);
 
 		// EventType
 		ClickableTextColumn<Event> eventType = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getEventType();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.event_type";
 			}
 		};
 		eventType.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		eventType.setSortable(sortable);
 		if (selectable) {
 			eventType.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(eventType, "EventType");
 
 		// Value
 		ClickableTextColumn<Event> value = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getValue().toString();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.value";
 			}
 		};
 		value.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		if (selectable) {
 			value.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(value, "Value");
 
 		// Threshold
 		ClickableTextColumn<Event> threshold = new ClickableTextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.getTheshold().toString();
 			}
 
 			@Override
 			public String getDataStoreName() {
 				return "tbl_events.threshold";
 			}
 		};
 		threshold.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		if (selectable) {
 			threshold.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(threshold, "Threshold");
 
 		// Unit
 		ClickableHtmlColumn<Event> unit = new ClickableHtmlColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return units.get(unitKey(object.getPtuId(), object.getName()));
 			}
 		};
 		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		if (selectable) {
 			unit.setFieldUpdater(new FieldUpdater<Event, String>() {
 
 				@Override
 				public void update(int index, Event object, String value) {
 					selectEvent(object);
 				}
 			});
 		}
 		table.addColumn(unit, "Unit");
 
 		// Selection
 		if (selectable) {
 			final SingleSelectionModel<Event> selectionModel = new SingleSelectionModel<Event>();
 			table.setSelectionModel(selectionModel);
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 						@Override
 						public void onSelectionChange(SelectionChangeEvent event) {
 							Event m = selectionModel.getSelectedObject();
 							log.info(m + " " + event.getSource());
 						}
 					});
 		}
 		
 		return true;
 	}
 
 	private void selectEvent(Event event) {
 	}
 
 	private void update() {
 		// enable / disable columns
 		if (table.getColumnIndex(ptu) >= 0) {
 			table.removeColumn(ptu);
 		}
 		if (ptuId == null) {
 			// add Ptu Column
 			table.insertColumn(1, ptu, ptuHeader);
 		}
 
 		if (table.getColumnIndex(name) >= 0) {
 			table.removeColumn(name);
 		}
 		if (measurementName == null) {
 			// add Name column
 			table.insertColumn(2, name, nameHeader);
 		}
 
 		// Re-sort the table
 //		if (sortable) {
 //			ColumnSortEvent.fire(table, table.getColumnSortList());
 //		}
 		RangeChangeEvent.fire(table, table.getVisibleRange());
 		table.redraw();
 
 //		if (selectable) {
 //			Event selection = table.getSelectionModel().getSelectedObject();
 //
 //			if ((selection == null) && (dataProvider.getList().size() > 0)) {
 //				selection = dataProvider.getList().get(0);
 //
 //				selectEvent(selection);
 //			}
 //
 //			// re-set the selection as the async update may have changed the
 //			// rendering
 //			if (selection != null) {
 //				selectionModel.setSelected(selection, true);
 //			}
 //		}
 	}
 
 	private String unitKey(String ptuId, String name) {
 		return ptuId + ":" + name;
 	}
 }

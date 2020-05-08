 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.widget.ClickableHtmlColumn;
 import ch.cern.atlas.apvs.client.widget.ClickableTextCell;
 import ch.cern.atlas.apvs.client.widget.ClickableTextColumn;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.Header;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.view.client.SelectionChangeEvent;
 
 public class MeasurementView extends AbstractMeasurementView {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private CellTable<String> table = new CellTable<String>();
 	private ListHandler<String> columnSortHandler;
 	private ClickableHtmlColumn<String> name;
 
 	private boolean sortable = true;
 
 	public MeasurementView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 
 		super.configure(element, clientFactory, args);
 
 		sortable = !options.contains("NoSort");
 
 		table.setWidth("100%");
 
 		add(table, CENTER);
 
 		name = new ClickableHtmlColumn<String>() {
 			@Override
 			public String getValue(String name) {
 				return historyMap.getDisplayName(name);
 			}
 
 			@Override
 			public void render(Context context, String name, SafeHtmlBuilder sb) {
 				String s = getValue(name);
 				Measurement m = historyMap.getMeasurement(ptuId, name);
 				if (m == null) {
 					return;
 				}
 				((ClickableTextCell) getCell()).render(context, decorate(s, m),
 						sb);
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		name.setSortable(sortable);
 		if (selectable) {
 			name.setFieldUpdater(new FieldUpdater<String, String>() {
 
 				@Override
 				public void update(int index, String object, String value) {
 					selectMeasurement(object);
 				}
 			});
 		}
 		table.addColumn(name, showHeader ? new TextHeader("") {
 			@Override
 			public String getValue() {
 				if (!showName)
 					return null;
 
 				return ptuId;
 			}
 
 			public void render(Context context, SafeHtmlBuilder sb) {
 				String s = getValue();
 				if (s != null) {
 					s = "PTU Id: " + ptuId;
 
 					if (interventions != null) {
 						String realName = interventions.get(ptuId) != null ? interventions
 								.get(ptuId).getName() : null;
 
 						if (realName != null) {
 							s = "<div title=\"" + s + "\">" + realName
 									+ "</div>";
 						}
 					}
 
 					sb.append(SafeHtmlUtils.fromSafeConstant(s));
 				}
 			};
 		}
 				: null);
 
 		// ClickableTextColumn<String> gauge = new ClickableTextColumn<String>()
 		// {
 		// @Override
 		// public String getValue(String name) {
 		// if ((name == null) || (historyMap == null) || (ptuId == null)) {
 		// return "";
 		// }
 		// Measurement m = historyMap.getMeasurement(ptuId, name);
 		// return m != null ? m.getLowLimit()+" "+m.getHighLimit() : "";
 		// }
 		//
 		// @Override
 		// public void render(Context context, String name, SafeHtmlBuilder sb)
 		// {
 		// Measurement m = historyMap != null ? historyMap.getMeasurement(ptuId,
 		// name) : null;
 		// if (m == null) {
 		// return;
 		// }
 		// gaugeWidget.setValue(m.getValue(), m.getLowLimit(),
 		// m.getHighLimit());
 		// sb.appendEscaped(gaugeWidget.getElement().getInnerHTML());
 		// Window.alert(gaugeWidget.getElement().getInnerHTML());
 		// }
 		//
 		// };
 		// gauge.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		// if (selectable) {
 		// gauge.setFieldUpdater(new FieldUpdater<String, String>() {
 		//
 		// @Override
 		// public void update(int index, String object, String value) {
 		// selectMeasurement(object);
 		// }
 		// });
 		// }
 		// table.addColumn(gauge, showHeader ? new TextHeader("Limits")
 		// : (Header<?>) null);
 
 		ClickableTextColumn<String> value = new ClickableTextColumn<String>() {
 			@Override
 			public String getValue(String name) {
 				if ((name == null) || (historyMap == null) || (ptuId == null)) {
 					return "";
 				}
 				Measurement m = historyMap.getMeasurement(ptuId, name);
 				return m != null ? format.format(m.getValue()) : "";
 			}
 
 			@Override
 			public void render(Context context, String name, SafeHtmlBuilder sb) {
 				String s = getValue(name);
 				Measurement m = historyMap != null ? historyMap.getMeasurement(
 						ptuId, name) : null;
 				if (m == null) {
 					return;
 				}
 
 				double c = m.getValue().doubleValue();
 				double lo = m.getLowLimit().doubleValue();
 				double hi = m.getHighLimit().doubleValue();
 				String status = lo >= hi ? "in_range" : c < lo ? "lo-limit"
 						: c > hi ? "hi-limit" : "in-range";
 
 				sb.append(SafeHtmlUtils.fromSafeConstant("<div class=\""
 						+ status + "\">"));
 
 				((ClickableTextCell) getCell()).render(context,
 						decorate(s, m, last), sb);
 
 				sb.append(SafeHtmlUtils.fromSafeConstant("</div>"));
 			}
 
 		};
 		value.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		if (selectable) {
 			value.setFieldUpdater(new FieldUpdater<String, String>() {
 
 				@Override
 				public void update(int index, String object, String value) {
 					selectMeasurement(object);
 				}
 			});
 		}
 		table.addColumn(value, showHeader ? new TextHeader("Value")
 				: (Header<?>) null);
 
 		ClickableHtmlColumn<String> unit = new ClickableHtmlColumn<String>() {
 			@Override
 			public String getValue(String name) {
 				Measurement m = historyMap != null ? historyMap.getMeasurement(
 						ptuId, name) : null;
 				return m != null ? m.getUnit() : "";
 			}
 
 			@Override
 			public void render(Context context, String name, SafeHtmlBuilder sb) {
 				String s = getValue(name);
 				Measurement m = historyMap != null ? historyMap.getMeasurement(
 						ptuId, name) : null;
 				if (m == null) {
 					return;
 				}
 				((ClickableTextCell) getCell()).render(context, decorate(s, m),
 						sb);
 			}
 		};
 		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		unit.setSortable(sortable);
 		if (selectable) {
 			unit.setFieldUpdater(new FieldUpdater<String, String>() {
 
 				@Override
 				public void update(int index, String object, String value) {
 					selectMeasurement(object);
 				}
 			});
 		}
 		table.addColumn(unit, showHeader ? new TextHeader("Unit")
 				: (Header<?>) null);
 
 		ClickableHtmlColumn<String> date = new ClickableHtmlColumn<String>() {
 			@Override
 			public String getValue(String name) {
 				Measurement measurement = historyMap.getMeasurement(ptuId, name);
 				return measurement != null ? PtuClientConstants.dateFormat.format(measurement
 						.getDate()) : "";
 			}
 
 			@Override
 			public void render(Context context, String name, SafeHtmlBuilder sb) {
 				String s = getValue(name);
 				Measurement m = historyMap.getMeasurement(ptuId, name);
 				if (m == null) {
 					return;
 				}
 				((ClickableTextCell) getCell()).render(context, decorate(s, m),
 						sb);
 			}
 		};
 		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		unit.setSortable(sortable);
 		if (selectable) {
 			unit.setFieldUpdater(new FieldUpdater<String, String>() {
 
 				@Override
 				public void update(int index, String object, String value) {
 					selectMeasurement(object);
 				}
 			});
 		}
 		if (showDate) {
 			table.addColumn(date, showHeader ? new TextHeader("Date")
 					: (Header<?>) null);
 		}
 
 		List<String> list = new ArrayList<String>();
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(list);
 
 		columnSortHandler = new ListHandler<String>(dataProvider.getList());
 		columnSortHandler.setComparator(name, new Comparator<String>() {
 			public int compare(String s1, String s2) {
 				if (s1 == s2) {
 					return 0;
 				}
 
 				if (s1 != null) {
 					return s1.compareTo(s2);
 				}
 				return -1;
 			}
 		});
 		columnSortHandler.setComparator(value, new Comparator<String>() {
 			public int compare(String s1, String s2) {
 				Measurement o1 = historyMap.getMeasurement(ptuId, s1);
 				Measurement o2 = historyMap.getMeasurement(ptuId, s2);
 
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if ((o1 != null) && (o1.getValue() != null)) {
 					if ((o2 != null) && (o2.getValue() != null)) {
 						double d1 = o1.getValue().doubleValue();
 						double d2 = o2.getValue().doubleValue();
 						return d1 < d2 ? -1 : d1 == d2 ? 0 : 1;
 					}
 					return 1;
 				}
 				return -1;
 			}
 		});
 		columnSortHandler.setComparator(unit, new Comparator<String>() {
 			public int compare(String s1, String s2) {
 				Measurement o1 = historyMap.getMeasurement(ptuId, s1);
 				Measurement o2 = historyMap.getMeasurement(ptuId, s2);
 
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
 		columnSortHandler.setComparator(date, new Comparator<String>() {
 			public int compare(String s1, String s2) {
 				Measurement o1 = historyMap.getMeasurement(ptuId, s1);
 				Measurement o2 = historyMap.getMeasurement(ptuId, s2);
 
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getDate().compareTo(o2.getDate())
 							: 1;
 				}
 				return -1;
 			}
 		});
 		table.addColumnSortHandler(columnSortHandler);
 		table.getColumnSortList().push(name);
 
 		if (selectable) {
 			table.setSelectionModel(selectionModel);
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 						@Override
 						public void onSelectionChange(SelectionChangeEvent event) {
 							String s = selectionModel.getSelectedObject();
 							log.info(s + " " + event.getSource());
 						}
 					});
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean update() {
 		boolean result = super.update();
 
		// resort the table
		ColumnSortEvent.fire(table, table.getColumnSortList());
 		table.redraw();
 
 		return result;
 	}
 }

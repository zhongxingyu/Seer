 /*******************************************************************************
  * Copyright (c) Oct 13, 2012 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.screens.f3;
 
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.common.id.CDOID;
 import org.eclipse.emf.cdo.util.ObjectNotFoundException;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.StyledCellLabelProvider;
 import org.eclipse.jface.viewers.StyledString;
 import org.eclipse.jface.viewers.StyledString.Styler;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.wb.swt.ResourceManager;
 import org.eclipse.wb.swt.TableViewerColumnSorter;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IQueryService;
 import com.netxforge.netxstudio.data.cdo.CDOQueryService;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.BaseResource;
 import com.netxforge.netxstudio.library.LevelKind;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.metrics.MetricValueRange;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.operators.Marker;
 import com.netxforge.netxstudio.operators.ToleranceMarker;
 import com.netxforge.netxstudio.operators.ToleranceMarkerDirectionKind;
 import com.netxforge.netxstudio.screens.AbstractSmartTableViewerComponent;
 import com.netxforge.netxstudio.screens.IItemsFilter;
 import com.netxforge.netxstudio.screens.ISearchPattern;
 import com.netxforge.netxstudio.screens.PeriodItemsFilter;
 import com.netxforge.netxstudio.screens.PeriodItemsFilter.PeriodPattern;
 import com.netxforge.netxstudio.screens.editing.IScreenFormService;
 import com.netxforge.netxstudio.screens.editing.ScreenUtil;
 import com.netxforge.netxstudio.screens.editing.tables.TableHelper;
 import com.netxforge.netxstudio.screens.editing.tables.TableHelper.TBVCSorterValueProvider;
 import com.netxforge.netxstudio.screens.internal.ScreensActivator;
 
 /**
  * Holds all needed to build a component showing NetXResource values. The data
  * is pre-processed using a background job, which populates a source list.
  * 
  * @author Christophe Bouhier
  * 
  */
 public class SmartValueComponent {
 
 	private ModelUtils modelUtils;
 	private TableHelper tableHelper;
 
 	@Inject
 	private CDOQueryService queryService;
 
 	@Inject
 	public SmartValueComponent(ModelUtils modelUtils, TableHelper tableHelper) {
 		this.modelUtils = modelUtils;
 		this.tableHelper = tableHelper;
 	}
 
 	private BaseResource baseResource;
 
 	private IScreenFormService screenService;
 
 	private AbstractSmartTableViewerComponent viewerComponent;
 
 	/**
 	 * Our markers, access synchronized.
 	 */
 	private List<Marker> markers;
 
 	private Date from;
 	private Date to;
 
 	public void configure(IScreenFormService screenService) {
 		this.screenService = screenService;
 	}
 
 	public TableViewer getValuesTableViewer() {
 		return viewerComponent.getTableViewer();
 	}
 
 	public void buildUI(Composite parent, Object layoutData) {
 
 		viewerComponent = new AbstractSmartTableViewerComponent(
 				parent.getShell(), true) {
 
 			@Override
 			protected Control createExtendedContentArea(Composite parent) {
 				return null;
 			}
 
 			@Override
 			protected void buildColumns(TableViewer viewer) {
 				SmartValueComponent.this.delegateBuildColumns();
 			}
 
 			@Override
 			public void handleDoubleClick() {
 
 			}
 
 			@Override
 			protected IStatus validateItem(Object item) {
 				return Status.OK_STATUS;
 			}
 
 			@Override
 			protected IItemsFilter createFilter() {
 
 				PeriodItemsFilter periodItemsFilter = new PeriodItemsFilter(
 						modelUtils) {
 
 					/**
 					 * Match on the first array of the item, which is a Value
 					 * with a timestamp.
 					 */
 					@Override
 					public boolean matchItem(Object item) {
 
 						if (item instanceof Object[]) {
 							Date d = (Date) ((Object[]) item)[0];
 							ISearchPattern pattern = this.getPattern();
 							if (pattern instanceof PeriodPattern) {
 								return ((PeriodPattern) pattern).matches(d);
 							}
 						}
 						//
 						return false;
 					}
 
 				};
 				ISearchPattern pattern = periodItemsFilter.getPattern();
 				// set the current period.
 				if (pattern instanceof PeriodPattern) {
 					// From & To should not be null!
 					((PeriodPattern) pattern).updateDates(from, to);
 				}
 				return periodItemsFilter;
 
 			}
 
 			@Override
 			protected <T> Comparator<T> getItemsComparator() {
 
 				/**
 				 * Compare on the first array in the matrix, which are dates.
 				 */
 				return new Comparator<T>() {
 
 					public int compare(T o1, T o2) {
 
 						if (o1 instanceof Object[] && o1 instanceof Object[]) {
 							Date d1 = (Date) ((Object[]) o1)[0];
 							Date d2 = (Date) ((Object[]) o2)[0];
 							// Compare Ascending.
 							return modelUtils.dateComparator().compare(d2, d1);
 						}
 						return 0;
 					}
 				};
 			}
 
 			@Override
 			protected void fillContentProvider(
 					AbstractContentProvider contentProvider,
 					IItemsFilter itemsFilter, IProgressMonitor progressMonitor)
 					throws CoreException {
 
 				if (ScreensActivator.DEBUG) {
 					ScreensActivator.TRACE.trace(
 							ScreensActivator.TRACE_SCREENS_OPTION,
 							"filling content provider");
 					ScreensActivator.TRACE
 							.trace(ScreensActivator.TRACE_SCREENS_OPTION,
 									"items filter pattern: "
 											+ itemsFilter.getPattern());
 
 				}
 
 				List<?> delegateGetItems = delegateGetItems(progressMonitor);
 
 				// Will be collection of Object[], each array corresponding to
 				// the column index.
 				for (int i = 0; i < delegateGetItems.size(); i++) {
 					contentProvider.addItem(delegateGetItems.get(i),
 							itemsFilter);
 				}
 				progressMonitor.done();
 
 			}
 
 			@Override
 			public String getElementName(Object item) {
 				return null;
 			}
 
 		};
 		viewerComponent.buildUI(parent);
 		viewerComponent
 				.setListLabelProvider(new NetXResourceValueLabelProvider());
 	}
 
 	protected List<?> delegateGetItems(IProgressMonitor monitor) {
 		return Arrays.asList(itemsForNetXResource(monitor));
 	}
 
 	protected void delegateBuildColumns() {
 		this.buildColumns();
 	}
 
 	private void buildColumns() {
 
 		Table table = this.getValuesTableViewer().getTable();
 		table.setRedraw(false);
 
 		for (TableColumn tc : table.getColumns()) {
 			tc.dispose();
 		}
 
 		if (baseResource instanceof NetXResource) {
 
 			// The marker column.
 			tableHelper.new TBVC<Date>(new NetXResourceValueLabelProvider())
 					.tbvcFor(this.getValuesTableViewer(), "M", 20);
 
 			// The time stamp column
 			tableHelper.new TBVC<Date>(new NetXResourceValueLabelProvider())
 					.tbvcFor(this.getValuesTableViewer(), "Time Stamp", 185);
 
 			{
 				ColumnRangeFeedback columnRangeFeedback = new ColumnRangeFeedback(
 						MetricsPackage.Literals.METRIC_VALUE_RANGE__METRIC_VALUES);
 
 				// ObjectArraySorter objectArraySorter = new ObjectArraySorter(
 				// tbvcFor);
 				// objectArraySorter.setSorter(TableViewerColumnSorter.DESC);
 
 				// metric value ranges....
 				NetXResource resource = (NetXResource) baseResource;
 
 				List<MetricValueRange> mvrList = Lists.newArrayList(resource
 						.getMetricValueRanges());
 				Collections.sort(mvrList, modelUtils.mvrCompare());
 				for (MetricValueRange mvr : mvrList) {
 
 					int intervalHint = mvr.getIntervalHint();
 
 					// CB 14-07-2012, interpret the interval.
 					String columnName = modelUtils.fromMinutes(intervalHint)
 							+ " [" + mvr.getKindHint().getName() + "]";
 					// String columnName = new Integer(intervalHint).toString()
 					// + " (min), " + mvr.getKindHint().getName();
 
 					TableViewerColumn tbvc = tableHelper.new TBVC<Double>(
 							new NetXResourceValueLabelProvider()).tbvcFor(
 							this.getValuesTableViewer(), columnName,
 							"Metric value range with " + columnName
 									+ " values.", 100);
 
 					setColumnData(mvr, tbvc);
 					setRangeFeedback(columnRangeFeedback, tbvc);
 
 				}
 			}
 			{
 				ColumnRangeFeedback columnRangeFeedback = new ColumnRangeFeedback(
 						LibraryPackage.Literals.NET_XRESOURCE__CAPACITY_VALUES);
 
 				TableViewerColumn tbvc = tableHelper.new TBVC<Double>(
 						new NetXResourceValueLabelProvider()).tbvcFor(
 						this.getValuesTableViewer(), "Capacity",
 						"Capacity value range", 100);
 				setColumnData(baseResource, tbvc);
 				setRangeFeedback(columnRangeFeedback, tbvc);
 			}
 			{
 				ColumnRangeFeedback columnRangeFeedback = new ColumnRangeFeedback(
 						LibraryPackage.Literals.NET_XRESOURCE__UTILIZATION_VALUES);
 				TableViewerColumn tbvc = tableHelper.new TBVC<Double>(
 						new NetXResourceValueLabelProvider()).tbvcFor(
 						this.getValuesTableViewer(), "Utilization",
 						"Utilization value range", 100);
 				setColumnData(baseResource, tbvc);
 				setRangeFeedback(columnRangeFeedback, tbvc);
 			}
 
 		}
 		this.getValuesTableViewer().getTable().setRedraw(true);
 	}
 
 	private void setRangeFeedback(ColumnRangeFeedback columnRangeFeedback,
 			TableViewerColumn tbvc) {
 		tbvc.getColumn().addListener(SWT.Selection, columnRangeFeedback);
 	}
 
 	private void setColumnData(CDOObject mvr, TableViewerColumn tbvc) {
 		CDOID cdoIDOfMVR = mvr.cdoID();
 		tbvc.getColumn().setData(cdoIDOfMVR);
 	}
 
 	public void injectData(BaseResource object) {
 
 		baseResource = object;
 		applyMarkers(null);
 
 		buildColumns();
 
 		// Clear the last completed, and re-apply the filter.
 		viewerComponent.setRefreshWithLastSelection(false);
 		viewerComponent.clearFilter();
 		viewerComponent.applyFilter();
 
 	}
 
 	/**
 	 * Call to clear the viewer.
 	 */
 	public void clearData() {
 		baseResource = null; // No Resource.
 
 		applyMarkers(null);
 
 		buildColumns();
 
 		viewerComponent.clearFilter(); // Will not fall back to the remembered
 										// items.
 
 		if (from != null && to != null) {
 			viewerComponent.applyFilter(); // Reapply.
 		}
 	}
 
 	/**
 	 * Refresh for smart viewers.
 	 */
 	public void smartRefresh() {
		viewerComponent.clearFilter();
		viewerComponent.applyFilter();
 	}
 
 	/*
 	 * Return a feedback for a table column data, which needs to be a CDOID
 	 * object. the provided EReference is used to produce the correct feedback.
 	 */
 	private final class ColumnRangeFeedback implements Listener {
 
 		// The feature which we want feedback on.
 		private EReference reference;
 
 		public ColumnRangeFeedback(EReference reference) {
 			super();
 			this.reference = reference;
 		}
 
 		public void handleEvent(Event event) {
 
 			// absolute positions
 			@SuppressWarnings("unused")
 			int x = event.x;
 			@SuppressWarnings("unused")
 			int y = event.y;
 
 			Widget widget = event.widget;
 			if (widget instanceof TableColumn) {
 				TableColumn tc = (TableColumn) widget;
 				Object data = tc.getData();
 				if (data instanceof CDOID) {
 					String valuesQuery = screenService.getEditingService()
 							.getDataService().getQueryService()
 							.getValuesQuery((CDOID) data, reference);
 					System.out.println(valuesQuery);
 					MessageDialog.openInformation(Display.getCurrent()
 							.getActiveShell(), "SQL Query", valuesQuery);
 				}
 			}
 		}
 	}
 
 	/*
 	 * The object array position correspond to the columns.
 	 * 
 	 * @author Christophe Bouhier
 	 */
 	class ObjectArraySorter extends TableViewerColumnSorter implements
 			TBVCSorterValueProvider {
 
 		private TableViewerColumn column;
 
 		public ObjectArraySorter(TableViewerColumn column) {
 			super(column);
 			this.column = column;
 		}
 
 		@Override
 		protected int doCompare(Viewer viewer, Object e1, Object e2) {
 			int cIndex = -1;
 			if (viewer instanceof TableViewer) {
 				cIndex = ((TableViewer) viewer).getTable().indexOf(
 						column.getColumn());
 			} else {
 				return 0;
 			}
 
 			Object value1 = valueOf((Object[]) e1, cIndex);
 			Object value2 = valueOf((Object[]) e2, cIndex);
 			if (value1 != null && value2 != null) {
 				return modelUtils.dateComparator().compare((Date) value1,
 						(Date) value2);
 			} else {
 				return super.doCompare(viewer, e1, e2);
 			}
 		}
 
 		public Object valueOf(Object rowObject, int columnIndex) {
 			assert rowObject instanceof Object[];
 			Object[] array = (Object[]) rowObject;
 			assert columnIndex >= 0 && columnIndex < array.length;
 			return array[columnIndex];
 		}
 	}
 
 	/*
 	 * A table filter which can be update with a from/to date.
 	 */
 	public class PeriodFilter extends ViewerFilter {
 
 		private long from = -1;
 		private long to = -1;
 
 		public void updateDates(Date from, Date to) {
 			assert from != null && to != null;
 			this.from = from.getTime();
 			this.to = to.getTime();
 		}
 
 		@Override
 		public boolean select(Viewer viewer, Object parentElement,
 				Object element) {
 
 			if (from <= 0 && to <= 0) {
 				return true;
 			}
 
 			// System.out.println("Updating filter with from=" + from + ", to="
 			// + to);
 
 			if (element instanceof Object[]) {
 				Object[] array = (Object[]) element;
 				Date ts = (Date) array[0];
 				// date is the first column.
 				long target = ts != null ? ts.getTime() : 0;
 				return from <= target && to >= target;
 			}
 			return false;
 		}
 	}
 
 	public void applyDateFilter(DateTimeRange dtr, boolean applyIt) {
 		// Do not update on empty date selectors.
 		if (dtr == null) {
 			return;
 		}
 		;
 		this.applyDateFilter(modelUtils.begin(dtr), modelUtils.end(dtr),
 				applyIt);
 	}
 
 	/**
 	 * get the content for this resource.
 	 * <ul>
 	 * <li>Array[0] => Date</li>
 	 * <li>Array[1] => MVR Value 0</li>
 	 * <li>Array[2] => MVR Value n</li>
 	 * <li>Array[3] => Capacity Value</li>
 	 * <li>Array[4] => Utilization Value</li>
 	 * </ul>
 	 * 
 	 */
 	private Object[] itemsForNetXResource(IProgressMonitor monitor) {
 		if (this.baseResource instanceof NetXResource) {
 
 			NetXResource res = (NetXResource) this.baseResource;
 
 			// populate the array, in a set of indexed lists.
 			// later create an object matrix from the list.
 
 			List<Date> existingDates = Lists.newArrayList();
 
 			int rangeIndex = 0;
 
 			List<MetricValueRange> sortedCopy = Ordering.from(
 					modelUtils.mvrCompare()).sortedCopy(
 					res.getMetricValueRanges());
 
 			Map<MetricValueRange, List<Value>> valueMap = Maps.newHashMap();
 
 			DateTimeRange period = modelUtils.period(from, to);
 
 			int totalWork = 0;
 			int metricWork = 0;
 
 			for (MetricValueRange mvr : sortedCopy) {
 				List<Value> values = queryService.mvrValues(
 						baseResource.cdoView(), mvr, IQueryService.QUERY_MYSQL,
 						period);
 				if (!values.isEmpty()) {
 					totalWork += values.size();
 					valueMap.put(mvr, values);
 				}
 			}
 
 			// determine the size of the array.
 			int arraySize = 0;
 			Object[] rangeArray;
 			arraySize = sortedCopy.size();
 			arraySize += 2; // Cap, Utilization.
 			rangeArray = new Object[arraySize];
 
 			metricWork = totalWork;
 
 			totalWork += res.getCapacityValues().size();
 			totalWork += res.getUtilizationValues().size();
 
 			SubMonitor convert = SubMonitor.convert(monitor, totalWork);
 			for (MetricValueRange mvr : sortedCopy) {
 				if (valueMap.containsKey(mvr)) {
 
 					List<Double> doubles = modelUtils.merge(" Metrics: "
 							+ modelUtils.fromMinutes(mvr.getIntervalHint()),
 							existingDates, valueMap.get(mvr),
 							convert.newChild(metricWork));
 
 					// Check for interruption, and bail if so.
 					if (doubles == null && monitor.isCanceled()) {
 						return new Object[0];
 					}
 					// check if the date exist, add if not and add the value
 					rangeArray[rangeIndex++] = doubles;
 				} else {
 					rangeArray[rangeIndex++] = Lists.newArrayList();
 				}
 			}
 
 			{
 
 				List<Double> doubles = modelUtils.merge(" Capacity",
 						existingDates, res.getCapacityValues(),
 						convert.newChild(res.getCapacityValues().size()));
 				// Check for interruption, and bail if so.
 				if (doubles == null && monitor.isCanceled()) {
 					return new Object[0];
 				}
 
 				rangeArray[rangeIndex++] = doubles;
 			}
 			{
 				List<Double> doubles = modelUtils.merge(" Utilization",
 						existingDates, res.getUtilizationValues(),
 						convert.newChild(res.getUtilizationValues().size()));
 				// Check for interruption, and bail if so.
 				if (doubles == null && monitor.isCanceled()) {
 					return new Object[0];
 				}
 
 				rangeArray[rangeIndex++] = doubles;
 			}
 
 			// 2nd Phase. Populate the end result.
 			List<Object[]> result = Lists.newArrayList();
 
 			for (Date date : existingDates) {
 				if (date == null)
 					continue;
 				int indexOfDate = existingDates.indexOf(date);
 				if (indexOfDate != -1) {
 					Object[] row = new Object[arraySize + 1];
 					row[0] = date;
 					for (int i = 0; i < rangeArray.length; i++) {
 						// ranges can be safely casted.
 						@SuppressWarnings("unchecked")
 						List<Double> range = (List<Double>) rangeArray[i];
 						// Should not be index out of bound, as we have
 						// grown the lists to the size of the dates.
 						if (indexOfDate < range.size()) {
 							row[i + 1] = range.get(indexOfDate);
 						} else {
 							row[i + 1] = new Object();
 						}
 
 					}
 					result.add(row);
 				}
 			}
 
 			return result.toArray();
 		}
 		return new Object[0];
 	}
 
 	/**
 	 * call to apply the period filter on the table viewer.
 	 * 
 	 * @param from
 	 * @param to
 	 * @param applyIt
 	 *            Applies this filter to the viewer or just set the period here.
 	 */
 	public void applyDateFilter(Date from, Date to, boolean applyIt) {
 		// Do not update on empty date selectors.
 		if (from == null || to == null) {
 			return;
 		}
 
 		this.from = from;
 		this.to = to;
 		if (applyIt) {
 			viewerComponent.applyFilter();
 		}
 
 	}
 
 	/**
 	 * 
 	 * Refer to the content provider, we assume an array of Objects with defined
 	 * object types for each index.
 	 * 
 	 * <ul>
 	 * <li>Column 0 => Derive from Column 1.</li>
 	 * <li>Column 1 => should be a a Date object</li>
 	 * <li>Column > 1 => should be a a Double object</li>
 	 * </ul>
 	 * 
 	 * If we have a marker for the date, we will style the cell according to the
 	 * marker type.
 	 * 
 	 * @author Christophe Bouhier
 	 */
 	public class NetXResourceValueLabelProvider extends StyledCellLabelProvider {
 
 		Map<LevelKind, Styler> styleMap = Maps.newHashMap();
 
 		public NetXResourceValueLabelProvider() {
 			super();
 		}
 
 		@Override
 		public void update(ViewerCell cell) {
 			Object element = cell.getElement();
 
 			if (element instanceof Object[]) {
 				Object[] array = (Object[]) element;
 				int columnIndex = cell.getColumnIndex();
 				if (columnIndex < array.length + 1) {
 					switch (columnIndex) {
 					case 0: {
 
 						Date d = (Date) array[0];
 						final Marker marker = markerForDate(d);
 						if (marker != null && marker instanceof ToleranceMarker) {
 							final ToleranceMarker tm = (ToleranceMarker) marker;
 							Image markerImage = imageForMarker(tm);
 							if (markerImage != null) {
 								cell.setImage(markerImage);
 							}
 
 							// Set up or down, as unicode character.
 							cell.setText(tm.getDirection() == ToleranceMarkerDirectionKind.UP ? new String(
 									"\u2B06") : new String("\u2B07"));
 						}
 					}
 						break;
 					case 1: {
 						Object object = array[columnIndex - 1];
 						if (object instanceof Date) {
 							Date d = (Date) object;
 							String ts = new String(modelUtils.date(d) + " @ "
 									+ modelUtils.time(d));
 							// find a marker for this date.
 							Styler markerStyle = null;
 							final Marker marker = markerForDate(d);
 							StyledString styledString = new StyledString();
 							if (marker != null
 									&& marker instanceof ToleranceMarker) {
 								markerStyle = styleForMarker((ToleranceMarker) marker);
 								styledString.append(ts, markerStyle);
 							} else {
 								styledString.append(ts);
 							}
 							cell.setText(styledString.getString());
 							cell.setStyleRanges(styledString.getStyleRanges());
 						}
 					}
 						break;
 					// All other indexes are values.
 					default: {
 						Object object = array[columnIndex - 1];
 						if (object instanceof Double) {
 							double value = (Double) object;
 							if (value != -1) {
 								DecimalFormat numberFormatter = new DecimalFormat(
 										ModelUtils.DEFAULT_VALUE_FORMAT_PATTERN);
 								numberFormatter
 										.setDecimalSeparatorAlwaysShown(true);
 								cell.setText(numberFormatter.format(value));
 							}
 						}
 					}
 					}
 
 				}
 			}
 
 			super.update(cell);
 		}
 
 		/**
 		 * @param d
 		 * @param marker
 		 * @return
 		 */
 		private Marker markerForDate(Date d) {
 
 			if (getMarkers() == null) {
 				return null;
 			}
 
 			ImmutableList<Marker> copyOfMarkers = ImmutableList
 					.copyOf(getMarkers());
 
 			Marker marker = null;
 
 			if (d != null && copyOfMarkers != null && copyOfMarkers.size() > 0) {
 				try {
 
 					marker = Iterables.find(copyOfMarkers,
 							modelUtils.markerForDate(d));
 				} catch (NoSuchElementException nsee) {
 					// ignore.
 				} catch (ObjectNotFoundException onfe) {
 					// ignore, simply bad markers....
 					// markers = null;
 					System.out.println(onfe.getMessage());
 				}
 			}
 			return marker;
 		}
 
 		/*
 		 * Get a styler from a map.
 		 */
 		private Styler styleForMarker(ToleranceMarker marker) {
 			LevelKind level = marker.getLevel();
 
 			// In time create the Stylers.
 			Styler markerStyle = styleMap.get(level);
 			if (markerStyle == null) {
 
 				switch (level.getValue()) {
 				case LevelKind.RED_VALUE: {
 					markerStyle = StyledString.createColorRegistryStyler(
 							ScreenUtil.RED_MARKER, null);
 				}
 					break;
 				case LevelKind.AMBER_VALUE: {
 					markerStyle = StyledString.createColorRegistryStyler(
 							ScreenUtil.AMBER_MARKER, null);
 				}
 					break;
 				case LevelKind.YELLOW_VALUE: {
 					markerStyle = StyledString.createColorRegistryStyler(
 							ScreenUtil.YELLOW_MARKER, null);
 				}
 				}
 				styleMap.put(level, markerStyle);
 			}
 			return markerStyle;
 		}
 
 		private Image imageForMarker(ToleranceMarker marker) {
 
 			ToleranceMarkerDirectionKind direction = marker.getDirection();
 			Image result = null;
 
 			switch (direction.getValue()) {
 			case ToleranceMarkerDirectionKind.START_VALUE: {
 				result = ResourceManager.getPluginImage(
 						"com.netxforge.netxstudio.models.edit",
 						"icons/full/obj16/Marker_s_H.png");
 			}
 				break;
 			case ToleranceMarkerDirectionKind.UP_VALUE: {
 				// No image for up :-(
 				// result = ResourceManager.getPluginImage(
 				// "com.netxforge.netxstudio.models.edit",
 				// "icons/full/obj16/Marker_s_H.png");
 
 			}
 				break;
 			case ToleranceMarkerDirectionKind.DOWN_VALUE: {
 				// No image for up :-(
 				// result = ResourceManager.getPluginImage(
 				// "com.netxforge.netxstudio.models.edit",
 				// "icons/full/obj16/Marker_s_H.png");
 
 			}
 				break;
 
 			}
 
 			return result;
 		}
 	}
 
 	/**
 	 * The Value component as the option to show the markers. We will apply only
 	 * markers, as per
 	 */
 	public synchronized void applyMarkers(List<Marker> markers) {
 		this.markers = markers;
 	}
 
 	public synchronized List<Marker> getMarkers() {
 		return markers;
 	}
 
 }

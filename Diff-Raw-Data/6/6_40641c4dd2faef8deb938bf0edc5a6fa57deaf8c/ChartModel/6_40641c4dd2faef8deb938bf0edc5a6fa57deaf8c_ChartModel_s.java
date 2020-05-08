 /*******************************************************************************
  * Copyright (c) 18 dec. 2012 NetXForge.
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
 package com.netxforge.netxstudio.screens.f3.charts;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.common.model.ModelUtils.TimeStampPredicate;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.metrics.KindHintType;
 import com.netxforge.netxstudio.metrics.MetricValueRange;
 import com.netxforge.netxstudio.operators.ResourceMonitor;
 import com.netxforge.netxstudio.screens.internal.ScreensActivator;
 
 /**
  * Wraps model references to keep them together.
  * 
  * @author Christophe Bouhier
  * 
  */
 public class ChartModel {
 
 	/** The period covering the chart */
 	protected DateTimeRange dtr;
 
 	/** The interval */
 	protected int interval = -1;
 
 	/** The kind of range */
 	protected KindHintType kind = null;
 
 	/** The NetXResource */
 	protected NetXResource netXRes = null;
 
 	/** The ResourceMonitor */
 	protected ResourceMonitor resourceMonitor = null;
 
 	@Inject
 	private ModelUtils modelUtils;
 
 	/**
 	 * Metric values. It is equal to the values in the {@link MetricValueRange}
 	 * of the {@link NetXResource} with the
 	 * {@link MetricValueRange#getIntervalHint()}
 	 */
 	protected List<Value> metricValues = null;
 
 	/**
 	 * Capacity values. Only available if a {@link NetXResource} is provided at
 	 * construction.
 	 */
 	protected List<Value> capValues = null;
 
 	/**
 	 * Utilization values. Only available if a {@link NetXResource} is provided
 	 * at construction.
 	 */
 	protected List<Value> utilValues = null;
 
 	/**
 	 * All Data in array format, split by date / value, sorted and filled up.
 	 */
 	protected Date[] timeStampArray;
 
 	protected double[] metriDoubleArray;
 
 	protected double[] capDoubleArray;
 
 	protected double[] utilDoubleArray;
 
 	/**
 	 * The state of the model, do not load when not OK.
 	 */
 	protected boolean chartModelOk = false;

 	public boolean isChartModelOk() {
 		return chartModelOk;
 	}
 
 	/**
 	 * The state of the model, do not load when not OK.
 	 */
 	private void setChartModelOk(boolean chartModelOk) {
 		this.chartModelOk = chartModelOk;
 	}
 
 	public ChartModel(ModelUtils modelUtils, DateTimeRange dtr, int interval,
 			KindHintType kind, NetXResource netXRes,
 			ResourceMonitor resMonitor, List<Value> values) {
 		super();
 		this.modelUtils = modelUtils;
 		deriveValues(dtr, interval, kind, netXRes, resMonitor, values);
 	}
 
 	private void deriveValues(DateTimeRange dtr, int interval,
 			KindHintType kind, NetXResource netXRes,
 			ResourceMonitor resMonitor, List<Value> values) {
 		// 1. ResourceMonitor
 		// 2. NetXResource
 		// 3. Values
 		if (resMonitor != null && resMonitor.getResourceRef() != null) {
 			this.resourceMonitor = resMonitor;
 			this.netXRes = resMonitor.getResourceRef();
 		} else if (netXRes != null) {
 			this.netXRes = netXRes;
 		} else if (values != null) {
 			this.metricValues = values;
 		}
 
 		this.interval = interval;
 		this.kind = kind;
 
 		// Populate the values, if we need to, (The model can also be used
 		// without
 		// the NetXResource object).
 		if (this.metricValues == null && this.netXRes != null
 				&& this.interval != -1) {
 			MetricValueRange mvr = modelUtils.valueRangeForIntervalAndKind(
 					this.netXRes, this.kind, interval);
 			if (mvr != null) {
 				this.metricValues = Lists.newArrayList(mvr.getMetricValues());
 			} else {
 				return;
 			}
 
 			capValues = Lists.newArrayList(netXRes.getCapacityValues());
 			utilValues = Lists.newArrayList(netXRes.getUtilizationValues());
 
 		}
 
 		// Apply the period to the ranges.
 		if (dtr != null) {
 			metricValues = sortAndApplyPeriod(metricValues, dtr, false);
 			// Derive a new DTR, for begin and end of the metric values.
 			DateTimeRange metricDTR = modelUtils.period(metricValues);
 			// Split in Date and double arrays.
 			timeStampArray = modelUtils.transformValueToDateArray(metricValues);
 
 			capValues = sortAndApplyPeriod(capValues, dtr, false);
 
 			// Apply a period filter.
 			// Capacity values typically occur at day start, with a single
 			// entry.
 			// fill up the list, so that a
 			capValues = rangeFillUpWithLastValue(timeStampArray, metricDTR,
 					capValues);
 
 			utilValues = sortAndApplyPeriod(utilValues, dtr, false);
 
 			// Why not get from the period?
 			utilValues = modelUtils.valuesForValues(utilValues, metricValues);
 
 			// Make sure we are equal size.
 			if (timeStampArray.length != utilValues.size()) {
 				if (ScreensActivator.DEBUG) {
 					ScreensActivator.TRACE
 							.trace(ScreensActivator.TRACE_SCREENS_OPTION,
 									"Skip plotting utilization, date array and util array length mismatch sizes => dates: "
 											+ timeStampArray.length
 											+ " util values: "
 											+ utilValues.size());
 				}
 				utilValues = rangeFillUpGaps(timeStampArray, dtr, utilValues);
 			}
 
 		}
 
 		metriDoubleArray = modelUtils.transformValueToDoubleArray(metricValues);
 
 		if (capValues != null) {
 			capDoubleArray = modelUtils.transformValueToDoubleArray(capValues);
 		}
 		if (utilValues != null) {
 			List<Double> utilDoubleValues = modelUtils
 					.transformValueToDouble(utilValues);
 			// Multiply by 100
 			utilDoubleArray = modelUtils
 					.multiplyByHundredAndToArray(utilDoubleValues);
 		}
 		// Assert all arrays have equal size. SWTChart behaves badly when it's
 		// not the case.
 		setChartModelOk(true);
 
 	}
 
 	public DateTimeRange getDtr() {
 		return dtr;
 	}
 
 	public int getInterval() {
 		return interval;
 	}
 
 	public NetXResource getNetXRes() {
 		return netXRes;
 	}
 
 	public ResourceMonitor getResMonitor() {
 		return resourceMonitor;
 	}
 
 	public List<Value> getValues() {
 		return metricValues;
 	}
 
 	public boolean hasMonitor() {
 		return this.getResMonitor() != null;
 	}
 
 	public boolean hasCapacity() {
 		return capDoubleArray != null
 				&& capDoubleArray.length == timeStampArray.length;
 	}
 
 	public boolean hasUtilization() {
 		return utilDoubleArray != null
 				&& utilDoubleArray.length == timeStampArray.length;
 	}
 
 	public boolean hasNetXResource() {
 		return netXRes != null;
 	}
 
 	public List<Value> sortAndApplyPeriod(List<Value> values,
 			DateTimeRange dtr, boolean reverse) {
 		List<Value> sortedCopy;
 		if (reverse) {
 			sortedCopy = modelUtils.sortValuesByTimeStampAndReverse(values);
 
 		} else {
 			sortedCopy = modelUtils.sortValuesByTimeStamp(values);
 
 		}
 		return modelUtils.valuesInsideRange(sortedCopy, dtr);
 	}
 
 	/**
 	 * Fill up a range with missing values from the initial collection for the
 	 * provided period.
 	 * 
 	 * @param timeStamps
 	 * @param dtr
 	 * @param initialCollection
 	 * @return
 	 */
 	private List<Value> rangeFillUpWithLastValue(Date[] timeStamps,
 			DateTimeRange dtr, List<Value> initialCollection) {
 		List<Value> filledCollection = Lists.newArrayList(initialCollection);
 
 		int initialSize = filledCollection.size();
 		if (initialSize > 0 && initialSize > timeStamps.length) {
 			// we have more in the initial collection then the number of
 			// timeStamps timestamps , strip according
 			// to the metric value date time range, filling up the blanks.
 			filledCollection = modelUtils.valuesInsideRange(filledCollection,
 					dtr);
 			
 		}
 
 		// We could still be bigger, so we should remove non-matched
		// timestamps
 		if (filledCollection.size() > timeStamps.length) {
 			filledCollection = modelUtils.valuesForTimestamps(filledCollection,
 					Lists.newArrayList(timeStamps));
 		}else{
 			return filledCollection;
 		}
 		
 		// Get the last value from the collection, and fill up for the quantity
 		// of dates.
 
 		initialSize = filledCollection.size();
 		if (initialSize > 0 && initialSize < timeStamps.length) {
 			Value lastVal = filledCollection.get(initialSize - 1);
 			for (int i = initialSize; i < timeStamps.length; i++) {
 				filledCollection.add(i, lastVal);
 			}
 		}
 		return filledCollection;
 	}
 
 	private List<Value> rangeFillUpGaps(Date[] timeStamps, DateTimeRange dtr,
 			List<Value> initialCollection) {
 		List<Value> filledCollection = Lists.newArrayList(initialCollection);
 
 		int initialSize = filledCollection.size();
 		if (initialSize > 0 && initialSize > timeStamps.length) {
 			// we have more in the initial collection then the number of
 			// timeStamps timestamps , strip according
 			// to the metric value date time range, filling up the blanks.
 			filledCollection = modelUtils.valuesInsideRange(filledCollection,
 					dtr);
 			return filledCollection;
 		}
 
 		// Get the last value from the collection, and fill up for the quantity
 		// of dates.
 		// Find the collection value for each date.
 		ArrayList<Date> timeStampsCollection = Lists.newArrayList(timeStamps);
 		filledCollection = Lists.newArrayListWithCapacity(timeStampsCollection
 				.size());
 
 		for (int i = 0; i < timeStampsCollection.size(); i++) {
 			Date date = timeStampsCollection.get(i);
 			TimeStampPredicate timeStampPredicate = modelUtils.new TimeStampPredicate(
 					date);
 			try {
 				Value find = Iterables.find(initialCollection,
 						timeStampPredicate);
 				filledCollection.add(i, find);
 			} catch (NoSuchElementException nsee) {
 				Value createValue = GenericsFactory.eINSTANCE.createValue();
 				createValue.setTimeStamp(modelUtils.toXMLDate(date));
 				createValue.setValue(0);
 				// Nope
 				filledCollection.add(i, createValue);
 			}
 
 		}
 
 		return filledCollection;
 	}
 
 	public void loadDummyData() {
 
 		// Demo values. Use Databinding !
 		long ts = System.currentTimeMillis();
 		double v = 10;
 		int ticks = 30;
 		List<Date> dates = new ArrayList<Date>();
 		double[] ySeries = new double[ticks];
 
 		// Dummy Series settings.
 		for (int i = 0; i < ticks; i++) {
 			// x
 			ts += (1000 * 60) * 15;
 			dates.add(new Date(ts));
 			// y
 			v += 1;
 			ySeries[i] = v;
 		}
 		Date[] xSeries = new Date[ticks];
 		dates.toArray(xSeries);
 
 		v = 20;
 		double[] yCapSeries = new double[ticks];
 
 		for (int i = 0; i < ticks; i++) {
 			if (i % 5 == 0)
 				v += 5;
 			yCapSeries[i] = v;
 		}
 
 		double[] yUtilSeries = new double[ticks];
 		for (int i = 0; i < ticks; i++) {
 			yUtilSeries[i] = (ySeries[i] / yCapSeries[i]);
 		}
 
 		double[] yToleranceSeries = new double[ticks];
 		for (int i = 0; i < ticks; i++) {
 			yToleranceSeries[i] = 0.9;
 		}
 	}
 
 }

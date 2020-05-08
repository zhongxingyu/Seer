 /*******************************************************************************
  * Copyright (c) 20 jan. 2014 NetXForge.
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
 package com.netxforge.netxstudio.common.model;
 
 import java.text.Normalizer.Form;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.netxforge.netxstudio.common.GenericsTuple;
 import com.netxforge.netxstudio.common.internal.CommonActivator;
 import com.netxforge.netxstudio.common.math.INativeFunctions;
 import com.netxforge.netxstudio.common.model.ModelUtils.TimeStampPredicate;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.Equipment;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.metrics.KindHintType;
 import com.netxforge.netxstudio.metrics.MetricValueRange;
 import com.netxforge.netxstudio.operators.Marker;
 
 /**
  * 
  * A model suitable for a {@link Chart}
  * 
  * @author Christophe Bouhier
  */
 public class ChartModel implements IChartModel {
 
 	/** The period covering the chart */
 	protected DateTimeRange chartPeriod;
 
 	/** The interval, defauts to {@link ModelUtils#SECONDS_IN_AN_HOUR} */
 	protected int interval = ModelUtils.MINUTES_IN_AN_HOUR;
 
 	/** The kind of range, defaults to {@link KindHintType#BH} */
 	protected KindHintType kind = KindHintType.BH;
 
 	/**
 	 * Sum state.
 	 */
 	protected boolean sum;
 
 	private ModelUtils modelUtils;
 
 	private INativeFunctions nativeFunctions;
 
 	/**
 	 * A cache holding unique ChartID's. The algorithm for the chartID, could
 	 * generated duplicates.
 	 */
 	private List<String> chartIDCache = Lists.newArrayList();
 
 	public ChartModel() {
 	}
 
 	public void setModelUtils(ModelUtils modelUtils) {
 		this.modelUtils = modelUtils;
 	}
 
 	public void setNativeFunctions(INativeFunctions nativeFunctions) {
 		this.nativeFunctions = nativeFunctions;
 	}
 
 	/**
 	 * A Collection of {@link IChartResource}
 	 */
 	private List<IChartResource> chartResources = Lists.newArrayList();
 
 	/**
 	 * If the trending is enabled on the {@link IChartModel model}
 	 */
 	private boolean trend;
 
 	public class ChartResource implements IChartResource {
 
 		/**
 		 * Metric values. It is equal to the values in the
 		 * {@link MetricValueRange} of the {@link NetXResource} with the
 		 * {@link MetricValueRange#getIntervalHint()}
 		 */
 		protected List<Value> metricValues = null;
 
 		/**
 		 * All Data in array format, split by date / value, sorted and filled
 		 * up.
 		 */
 		protected Date[] timeStampArray = null;
 
 		protected double[] metriDoubleArray = null;
 
 		protected double[] capDoubleArray = null;
 
 		protected double[] utilDoubleArray = null;
 
 		/**
 		 * The state of the model, do not load when not OK.
 		 */
 		protected boolean chartModelOk = false;
 
 		/**
 		 * cache entry of the chart if.
 		 */
 		private String chartID = null;
 
 		/**
 		 * The filtered state will either show or hide us in the chart viewer.
 		 */
 		private boolean filtered;
 		/**
 		 * The summary.
 		 */
 		private NetxresourceSummary netxSummary;
 
 		/**
 		 * The {@link DateTimeRange period} defining the start and end for the
 		 * metric values.
 		 */
 		private DateTimeRange metricDTR;
 
 		public ChartResource(NetxresourceSummary netxSummary) {
 			this.netxSummary = netxSummary;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * com.netxforge.netxstudio.common.model.IChartModel#isChartModelOk()
 		 */
 		public boolean isChartModelOk() {
 			return chartModelOk;
 		}
 
 		/**
 		 * The state of the model, do not load when not OK.
 		 */
 		private void setChartModelOk(boolean chartModelOk) {
 			this.chartModelOk = chartModelOk;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * com.netxforge.netxstudio.common.model.IChartModel#getNetXResource()
 		 */
 		public NetXResource getNetXResource() {
 			return netxSummary.getTarget();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see com.netxforge.netxstudio.common.model.IChartModel#getValues()
 		 */
 		@SuppressWarnings("unchecked")
 		private List<Value> getValues() {
 
 			if (metricValues == null) {
 
 				NetXResource target = netxSummary.getTarget();
 
 				// It could be, there is no aggregation data for higher order
 				// intervals.
 				// this will yield and empty list.
 				MetricValueRange valueRangeForIntervalAndKind = modelUtils
 						.valueRangeForIntervalAndKind(target, kind, interval);
 				if (valueRangeForIntervalAndKind != null) {
 
 					// When the defined period is larger than the period for the
 					// values,
 					// we can reduce it with:
 					List<Value> sortAndApplyPeriod = modelUtils
 							.sortAndApplyPeriod(valueRangeForIntervalAndKind
 									.getMetricValues(), chartPeriod, false);
 					// This is a valid chart model :-)
 					if (!sortAndApplyPeriod.isEmpty()) {
 						setChartModelOk(true);
 					}
 					metricDTR = modelUtils.period(sortAndApplyPeriod);
 					metricValues = sortAndApplyPeriod;
 				} else {
 					metricValues = Collections.EMPTY_LIST;
 					// throw new
 					// UnsupportedOperationException("no value range");
 				}
 			}
 			return metricValues;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see com.netxforge.netxstudio.common.model.IChartModel#hasCapacity()
 		 */
 		public boolean hasCapacity() {
 
 			return getCapDoubleArray() != null
 					&& getCapDoubleArray().length == timeStampArray.length;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * com.netxforge.netxstudio.common.model.IChartModel#hasUtilization()
 		 */
 		public boolean hasUtilization() {
 			return utilDoubleArray != null
 					&& utilDoubleArray.length == timeStampArray.length;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * com.netxforge.netxstudio.common.model.IChartModel#hasNetXResource()
 		 */
 		public boolean hasNetXResource() {
 			return netxSummary != null;
 		}
 
 		/**
 		 * Fill up a range with missing values from the initial collection for
 		 * the provided period.
 		 * 
 		 * @param timeStamps
 		 * @param dtr
 		 * @param initialCollection
 		 * @return
 		 */
 		private List<Value> rangeFillUpWithLastValue(Date[] timeStamps,
 				DateTimeRange dtr, List<Value> initialCollection) {
 			List<Value> filledCollection = Lists
 					.newArrayList(initialCollection);
 
 			int initialSize = filledCollection.size();
 			if (initialSize > 0 && initialSize > timeStamps.length) {
 				// we have more in the initial collection then the number of
 				// timeStamps timestamps , strip according
 				// to the metric value date time range, filling up the blanks.
 				filledCollection = modelUtils.valuesInsideRange(
 						filledCollection, dtr);
 
 			}
 
 			// We could still be bigger, so we should remove non-matched
 			// timestamps. TODO: Danger that we remove cap. where the value
 			// changes, and we would fill-up with the wrong cap. value.
 			if (filledCollection.size() > timeStamps.length) {
 				filledCollection = modelUtils.valuesForTimestamps(
 						filledCollection, Lists.newArrayList(timeStamps));
 			}
 
 			// Get the last value from the collection, and fill up for the
 			// quantity
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
 
 		private List<Value> rangeFillUpGaps(Date[] timeStamps,
 				DateTimeRange dtr, List<Value> initialCollection) {
 			List<Value> filledCollection = Lists
 					.newArrayList(initialCollection);
 
 			int initialSize = filledCollection.size();
 			if (initialSize > 0 && initialSize > timeStamps.length) {
 				// we have more in the initial collection then the number of
 				// timeStamps timestamps , strip according
 				// to the metric value date time range, filling up the blanks.
 				filledCollection = modelUtils.valuesInsideRange(
 						filledCollection, dtr);
 				return filledCollection;
 			}
 
 			// Get the last value from the collection, and fill up for the
 			// quantity
 			// of dates.
 			// Find the collection value for each date.
 			ArrayList<Date> timeStampsCollection = Lists
 					.newArrayList(timeStamps);
 			filledCollection = Lists
 					.newArrayListWithCapacity(timeStampsCollection.size());
 
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
 					filledCollection.add(i, createValue);
 				}
 
 			}
 
 			return filledCollection;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * com.netxforge.netxstudio.common.model.IChartModel#getTimeStampArray()
 		 */
 		public Date[] getTimeStampArray() {
 			if (timeStampArray == null) {
 				timeStampArray = modelUtils.transformValueToDateArray(this
 						.getValues());
 			}
 			return timeStampArray;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * com.netxforge.netxstudio.common.model.IChartModel#getCapDoubleArray()
 		 */
 		public double[] getCapDoubleArray() {
 
			if (capDoubleArray == null && metricDTR != null) {
 				List<Value> capValues = modelUtils.sortAndApplyPeriod(
 						netxSummary.getTarget().getCapacityValues(),
 						chartPeriod, false);
 
 				capValues = rangeFillUpWithLastValue(getTimeStampArray(),
 						metricDTR, capValues);
 				if (capValues != null) {
 					capDoubleArray = modelUtils
 							.transformValueToDoubleArray(capValues);
 				}
 			}
 			return capDoubleArray;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * com.netxforge.netxstudio.common.model.IChartModel#getMetriDoubleArray
 		 * ()
 		 */
 		public double[] getMetriDoubleArray() {
 			return modelUtils.transformValueToDoubleArray(getValues());
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * com.netxforge.netxstudio.common.model.IChartModel#getUtilDoubleArray
 		 * ()
 		 */
 		public double[] getUtilDoubleArray() {
 
 			if (utilDoubleArray == null) {
 				List<Value> utilValues = modelUtils.sortAndApplyPeriod(this
 						.getNetXResource().getUtilizationValues(), chartPeriod,
 						false);
 				// Why not get from the period?
 				utilValues = modelUtils
 						.valuesForValues(utilValues, getValues());
 
 				// Make sure we are equal size.
 				if (timeStampArray.length != utilValues.size()) {
 					if (CommonActivator.DEBUG) {
 						CommonActivator.TRACE
 								.trace(CommonActivator.TRACE_COMMON_CHART_OPTION,
 										"Skip plotting utilization, date array and util array length mismatch sizes => dates: "
 												+ timeStampArray.length
 												+ " util values: "
 												+ utilValues.size());
 					}
 					utilValues = rangeFillUpGaps(this.getTimeStampArray(),
 							chartPeriod, utilValues);
 				}
 
 			}
 
 			return utilDoubleArray;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see com.netxforge.netxstudio.common.model.IChartModel#getMarkers()
 		 */
 		public List<Marker> getMarkers() {
 			return netxSummary.markers();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see com.netxforge.netxstudio.common.model.IChartModel#hasMarkers()
 		 */
 		public boolean hasMarkers() {
 			return !netxSummary.markers().isEmpty();
 		}
 
 		/**
 		 * Trend use, linear regression.
 		 */
 		public double[] getTrendDoubleArray() {
 
 			double[][] data = modelUtils.transformValueToDoubleMatrix(this
 					.getValues());
 
 			// Get the slope and Intercept.
 			GenericsTuple<Double, Double> regress = nativeFunctions.trend(data);
 			double slope = regress.getKey();
 			double intercept = regress.getValue();
 
 			// System.out.println("slope: " + slope + " intercept: " +
 			// intercept);
 
 			// y = intercept + slope*x
 			double[] trend = new double[this.getTimeStampArray().length];
 			for (int i = 0; i < data.length; i++) {
 				// create the regression line in seconds.
 				double y = intercept + (slope * data[i][0]);
 				trend[i] = y;
 
 			}
 			return trend;
 		}
 
 		public boolean isFiltered() {
 			return filtered;
 		}
 
 		public void setFiltered(boolean filtered) {
 			this.filtered = filtered;
 		}
 
 		public void resetCaches() {
 			metricValues = null;
			metricDTR = null;
 			capDoubleArray = null;
 			utilDoubleArray = null;
 			timeStampArray = null;
 			chartID = null;
 		}
 
 		public String getChartID() {
 
 			if (chartID == null) {
 
 				final StringBuilder sb = new StringBuilder();
 
 				final Component c = this.getNetXResource().getComponentRef();
 				if (c instanceof com.netxforge.netxstudio.library.Function) {
 					sb.append(c.getName());
 				}
 				if (c instanceof Equipment) {
 					Equipment eq = (Equipment) c;
 					sb.append(eq.getEquipmentCode() != null ? eq
 							.getEquipmentCode() : "?");
 					if (eq.eIsSet(LibraryPackage.Literals.COMPONENT__NAME)) {
 						sb.append(" : " + eq.getName());
 					}
 				}
 				sb.append(" - ");
 				sb.append(getNetXResource().getShortName());
 
 				String proposedChartID = sb.toString();
 
 				while (chartIDCache.contains(proposedChartID)) {
 					// append the index of the existing one to the new one so it
 					// becomes unique.
 					int indexOf = chartIDCache.indexOf(proposedChartID);
 					proposedChartID = proposedChartID.concat("_" + indexOf);
 				}
 				chartIDCache.add(proposedChartID);
 				this.chartID = proposedChartID;
 			}
 			// Add the index, so we don't have duplicates.
 			return chartID;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.common.model.IChartModel#getInterval()
 	 */
 	public int getInterval() {
 		return interval;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.common.model.IChartModel#setInterval(int)
 	 */
 	public void setInterval(int interval) {
 		this.interval = interval;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.common.model.IChartModel#getKindHint()
 	 */
 	public KindHintType getKindHint() {
 		return kind;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.common.model.IChartModel#setKindHint(com.netxforge
 	 * .netxstudio.metrics.KindHintType)
 	 */
 	public void setKindHint(KindHintType kind) {
 		this.kind = kind;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.common.model.IChartModel#getPeriod()
 	 */
 	public DateTimeRange getPeriod() {
 		return chartPeriod;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.common.model.IChartModel#setPeriod(com.netxforge
 	 * .netxstudio.generics.DateTimeRange)
 	 */
 	public void setPeriod(DateTimeRange dtr) {
 		this.chartPeriod = dtr;
 	}
 
 	public void removeChartResource(IChartResource chartResource) {
 		if (chartResources.contains(chartResource)) {
 			if (chartResources.remove(chartResource)) {
 				// removed from collection.
 			}
 		}
 	}
 
 	public void addChartResource(NetxresourceSummary summary) {
 
 		if (summary instanceof NetxresourceSummary) {
 			NetxresourceSummary netxSummary = (NetxresourceSummary) summary;
 
 			// Make sure we set the model period, if this is the first
 			// IChartResource.
 			if (chartResources.isEmpty()) {
 				this.chartPeriod = netxSummary.getPeriod();
 				intervalForPeriod(chartPeriod);
 			}
 
 			final ChartResource chartResource = new ChartResource(netxSummary);
 			chartResources.add(chartResource);
 			// chartModel.deriveValues(summary.getPeriod(), interval, kind,
 			// target, null, null, netxSummary.markers());
 		} else {
 			throw new UnsupportedOperationException(
 					"Chart Model for summary type:  "
 							+ summary.getClass().getName()
 							+ " is not supported");
 		}
 
 	}
 
 	/**
 	 * Set the default interval based on the period. If the period < 3 days =>
 	 * Show in hours. If the period < 1 month => Show in days. If the period < 6
 	 * months => Show in weeks.
 	 * 
 	 * @param chartPeriod
 	 */
 	private void intervalForPeriod(DateTimeRange chartPeriod) {
 		int days = modelUtils.daysInPeriod(chartPeriod);
 		if (days < 3) {
 			// default do nothing.
 			return;
 		} else if (days <= 31) {
 			interval = ModelUtils.MINUTES_IN_A_DAY;
 		} else if (days <= 183) {
 			interval = ModelUtils.MINUTES_IN_A_WEEK;
 		} else {
 			interval = ModelUtils.MINUTES_IN_A_MONTH;
 		}
 
 	}
 
 	/**
 	 * A textual representation of the {@link IChartModel} to be used in the
 	 * title of a {@link Form}
 	 */
 	public String getChartText() {
 
 		StringBuilder sb = new StringBuilder();
 
 		List<IChartResource> visibleChartResources = this
 				.getChartNonFilteredResources();
 
 		if (visibleChartResources.size() == 0) {
 			sb.append("none");
 		} else if (visibleChartResources.size() == 1) {
 			sb.append(chartResources.get(0).getNetXResource().getLongName());
 		} else if (visibleChartResources.size() > 1) {
 			sb.append("Multiple resources");
 		}
 
 		// boolean first = true;
 		// for (IChartResource chartResource : chartResources) {
 		// if (!chartResource.isFiltered()) {
 		// if (first) {
 		// first = false;
 		// sb.append("Resource: ");
 		// } else {
 		// sb.append(", ");
 		// }
 		// sb.append(chartResource.getNetXResource().getLongName());
 		// }
 		// }
 		return sb.toString();
 	}
 
 	/**
 	 * Clear the {@link ChartModel}.
 	 */
 	public void clear() {
 		this.getChartResources().clear();
 	}
 
 	public synchronized Collection<IChartResource> getChartResources() {
 		return this.chartResources;
 	}
 
 	public List<IChartResource> getChartNonFilteredResources() {
 		Iterable<IChartResource> filter = Iterables.filter(chartResources,
 				new Predicate<IChartResource>() {
 					public boolean apply(IChartResource input) {
 						return input.isFiltered() ? false : true;
 					}
 				});
 		return Lists.newArrayList(filter);
 	}
 
 	/**
 	 * Proxied objects and equality break. As we inject this class it's a proxy
 	 * object and we present it an StructuredViewer nothing shows. The element
 	 * comparer relies on equality
 	 * 
 	 * See this:
 	 * http://cwmaier.blogspot.nl/2007/07/liskov-substitution-principle
 	 * -equals.html http://code.google.com/p/peaberry/issues/detail?id=64
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		return obj instanceof IChartModel;
 	}
 
 	public double[] sum() {
 		List<IChartResource> chartNonFilteredResources = getChartNonFilteredResources();
 
 		List<Double[]> input = Lists.transform(chartNonFilteredResources,
 				new Function<IChartResource, Double[]>() {
 
 					public Double[] apply(IChartResource input) {
 						return modelUtils.transformToDoubleArray(input
 								.getMetriDoubleArray());
 					}
 
 				});
 		try {
 			return nativeFunctions.sumCollections(input);
 		} catch (Exception e) {
 
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.common.model.IChartModel#setShouldSum(boolean)
 	 */
 	public void setShouldSum(boolean shouldSum) {
 		this.sum = shouldSum;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.common.model.IChartModel#shouldSum()
 	 */
 	public boolean shouldSum() {
 		return sum;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.common.model.IChartModel#getFirstChartResource()
 	 */
 	public IChartResource getFirstChartResource() {
 		if (!getChartNonFilteredResources().isEmpty()) {
 			return getChartNonFilteredResources().iterator().next();
 		}
 		return null;
 	}
 
 	public void reset() {
 		
 		chartIDCache.clear();
 		
 		for (IChartResource cr : getChartResources()) {
 			cr.resetCaches();
 		}
 		
 
 	}
 
 	public void setShouldTrend(boolean shouldTrend) {
 		trend = shouldTrend;
 
 	}
 
 	public boolean shouldTrend() {
 		return trend;
 	}
 
 }

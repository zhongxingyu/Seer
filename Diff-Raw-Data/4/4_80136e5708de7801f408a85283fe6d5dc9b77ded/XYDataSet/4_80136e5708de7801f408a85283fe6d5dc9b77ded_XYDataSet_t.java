 package org.corewall.geology.models;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.corewall.data.models.Unit;
 
 /**
  * An x-y dataset that hashes x-space into fixed-size segments to provide
  * efficient random access and range queries. This dataset does not track units
  * but expects all x values to be in the same unit.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class XYDataSet {
 
 	/**
 	 * Defines the interface for a hash function.
 	 */
 	public interface Hash {
 		/**
 		 * Hashes the specified x value to an index.
 		 * 
 		 * @param x
 		 *            the x value.
 		 * @return the index.
 		 */
 		int hash(double x);
 	}
 
 	/**
 	 * Defines the interface for an interpolation strategy.
 	 */
 	public interface Interpolation {
 		/**
 		 * Interpolate a datum for the specified x value.
 		 * 
 		 * @param x
 		 *            the x value.
 		 * @param dataset
 		 *            the data set.
 		 * @return the interpolated data point or null for none.
 		 */
 		public XYDatum interpolate(double x, XYDataSet dataset);
 	}
 
 	protected static final DecimalFormat NUM = new DecimalFormat("0.0####");
 
 	/**
 	 * Creates an averaging {@link Interpolation} over a fixed window of data
 	 * points.
 	 * 
 	 * @param window
 	 *            the window size.
 	 * @param center
 	 *            true if the window should be centered on the interpolation
 	 *            point, false if it should be preceding the interpolation
 	 *            point.
 	 * @return the {@link Interpolation} instance.
 	 */
 	public static Interpolation average(final int window, final boolean center) {
 		return new Interpolation() {
 			public XYDatum interpolate(final double x, final XYDataSet dataset) {
 				// check if we need interpolation
 				XYDatum exact = dataset.get(x);
 				if (exact != null) {
 					return exact;
 				}
 
 				// average the data points in the window
 				double sum = 0.0;
 				int count = 0;
 				for (XYDatum d : dataset.get(x, center ? window / 2 : window, center ? window / 2 : 0)) {
 					sum += d.y;
 					count++;
 				}
 				return new XYDatum(x, sum / count);
 			}
 		};
 	}
 
 	/**
 	 * Creates a scaling floor {@link Hash}.
 	 * 
 	 * @param scale
 	 *            the scaling factor.
 	 * @return the {@link Hash} instance.
 	 */
 	public static Hash floorHash(final double scale) {
 		return new Hash() {
 			public int hash(final double x) {
 				return (int) Math.floor((x + 0.000000001) * (scale + 0.000000001));
 			}
 		};
 	}
 
 	/**
 	 * Creates a linear {@link Interpolation}.
 	 * 
 	 * @return the {@link Interpolation} instance.
 	 */
 	public static Interpolation linear() {
 		return average(2, true);
 	}
 
 	/**
 	 * Creates a nearest {@link Interpolation}. This interpolation returns the
 	 * datum closest to the desired x-value.
 	 * 
 	 * @return the {@link Interpolation} instance.
 	 */
 	public static Interpolation nearest() {
 		return new Interpolation() {
 			public XYDatum interpolate(final double x, final XYDataSet dataset) {
 				List<XYDatum> around = dataset.get(x, 1, 1);
 				switch (around.size()) {
 					case 0:
 						return null;
 					case 1:
 						return new XYDatum(x, around.get(0).y);
 					case 3:
 						return around.get(1); // no interpolation needed
 					default:
 						if (x - around.get(0).x <= around.get(1).x - x) {
 							return new XYDatum(x, around.get(0).y);
 						} else {
 							return new XYDatum(x, around.get(1).y);
 						}
 				}
 			}
 		};
 	}
 
 	/**
 	 * Creates an {@link Interpolation} instance that returns the exact datum if
 	 * it exists, null otherwise.
 	 * 
 	 * @return the {@link Interpolation} instance.
 	 */
 	public static Interpolation none() {
 		return new Interpolation() {
 			public XYDatum interpolate(final double x, final XYDataSet dataset) {
 				return dataset.get(x);
 			}
 		};
 	}
 
 	protected int count = 0;
 	protected final Hash function;
 	protected double max = Double.MIN_VALUE;
 	protected double min = Double.MAX_VALUE;
 	protected final String name;
 	protected int segMax = 0;
 	protected final SortedMap<Integer, List<XYDatum>> segments = new TreeMap<Integer, List<XYDatum>>();
 
 	/**
 	 * Create a new XYDataSet.
 	 * 
 	 * @param name
 	 *            the name.
 	 */
 	public XYDataSet(final String name) {
 		this.name = name;
 		function = floorHash(1);
 	}
 
 	/**
 	 * Create a new XYDataSet.
 	 * 
 	 * @param name
 	 *            the name.
 	 * @param hash
 	 *            the hash function.
 	 */
 	public XYDataSet(final String name, final Hash hash) {
 		this.name = name;
 		function = hash;
 	}
 
 	/**
 	 * Creates a new XYDataSet.
 	 * 
 	 * @param name
 	 *            the name.
 	 * @param hash
 	 *            the hash function.
 	 * @param data
 	 *            the initial data.
 	 */
 	public XYDataSet(final String name, final Hash hash, final List<XYDatum> data) {
 		this.name = name;
 		function = hash;
 		for (XYDatum d : data) {
 			add(d);
 		}
 	}
 
 	/**
 	 * Adds the specified data point to this dataset.
 	 * 
 	 * @param x
 	 *            the x-value.
 	 * @param y
 	 *            the y-value.
 	 */
 	public void add(final double x, final double y) {
 		add(new XYDatum(x, y));
 	}
 
 	/**
 	 * Adds the specified datum to this dataset.
 	 * 
 	 * @param XYDatum
 	 *            the datum.
 	 */
 	public void add(final XYDatum XYDatum) {
 		// get the segment
 		int segmentIndex = function.hash(XYDatum.x);
 		List<XYDatum> segment = segments.get(segmentIndex);
 		if (segment == null) {
 			segment = new ArrayList<XYDatum>();
 			segments.put(segmentIndex, segment);
 		}
 
 		// insert the XYDatum
 		int i = Collections.binarySearch(segment, XYDatum);
 		if (i < 0) {
 			i = -(i + 1);
 		}
 		segment.add(i, XYDatum);
 
 		// update stats
 		count++;
 		max = Math.max(max, XYDatum.y);
 		min = Math.min(min, XYDatum.y);
 		segMax = Math.max(segMax, segment.size());
 	}
 
 	/**
 	 * Converts all x values in this dataset from one unit to another.
 	 * 
 	 * @param from
 	 *            the current unit.
 	 * @param to
 	 *            the desired unit.
 	 */
 	public void convertX(final Unit from, final Unit to) {
 		for (XYDatum d : getAll()) {
 			d.convertX(from, to);
 		}
 	}
 
 	/**
 	 * Converts all y values in this dataset from one unit to another.
 	 * 
 	 * @param from
 	 *            the current unit.
 	 * @param to
 	 *            the desired unit.
 	 */
 	public void convertY(final Unit from, final Unit to) {
 		for (XYDatum d : getAll()) {
 			d.convertY(from, to);
 		}
 	}
 
 	/**
 	 * Gets the datum for the specified x value.
 	 * 
 	 * @param x
 	 *            the x value.
 	 * @return the datum or null if not found.
 	 */
 	public XYDatum get(final double x) {
 		List<XYDatum> segment = segments.get(function.hash(x));
 		if (segment == null) {
 			return null;
 		} else {
 			int index = Collections.binarySearch(segment, new XYDatum(x, 0.0));
 			return (index < 0) ? null : segment.get(index);
 		}
 	}
 
 	/**
 	 * Gets the datums between two x values.
 	 * 
 	 * @param x1
 	 *            the starting x value.
 	 * @param x2
 	 *            the ending x value.
 	 * @return the list of datums in range.
 	 */
 	public List<XYDatum> get(final double x1, final double x2) {
 		List<XYDatum> data = new ArrayList<XYDatum>();
 		for (int i = function.hash(x1); i <= function.hash(x2); i++) {
 			List<XYDatum> segment = segments.get(function.hash(i));
 			if (segment != null) {
 				for (XYDatum pt : segment) {
 					if ((pt.x >= x1) && (pt.x <= x2)) {
 						data.add(pt);
 					}
 				}
 			}
 		}
 		return data;
 	}
 
 	/**
 	 * Gets the list of datums around the specified x value.
 	 * 
 	 * @param x
 	 *            the x value.
 	 * @param before
 	 *            the number of datums before x to include.
 	 * @param after
 	 *            the number of datums after x to include.
 	 * @return the list of datums.
 	 */
 	public List<XYDatum> get(final double x, final int before, final int after) {
 		return getSegmented(x, before, after);
 	}
 
 	/**
 	 * Gets all datums in this dataset.
 	 * 
 	 * @return the list of all datums.
 	 */
 	public List<XYDatum> getAll() {
 		List<XYDatum> data = new ArrayList<XYDatum>();
 		for (int i : segments.keySet()) {
 			data.addAll(segments.get(i));
 		}
 		return data;
 	}
 
 	protected List<XYDatum> getAll(final double x, final int before, final int after) {
 		List<XYDatum> all = getAll();
 		int i = Collections.binarySearch(all, new XYDatum(x, 0));
 		if (i < 0) {
 			i = -(i + 1);
 		}
 		return all.subList(Math.max(0, i - before), Math.min(all.size(), i + after));
 	}
 
 	/**
 	 * Gets the maximum y value in this dataset.
 	 * 
 	 * @return the maximum y value.
 	 */
 	public double getMax() {
 		return max;
 	}
 
 	/**
 	 * Gets the minimum y value in this dataset.
 	 * 
 	 * @return the minimum y value.
 	 */
 	public double getMin() {
 		return min;
 	}
 
 	/**
 	 * Gets the name of this dataset.
 	 * 
 	 * @return the name.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Gets the segment for the specified x-value.
 	 * 
 	 * @param x
 	 *            the x value.
 	 * @return the segment or null if not found.
 	 */
 	public List<XYDatum> getSegment(final double x) {
 		return segments.get(function.hash(x));
 	}
 
 	protected List<XYDatum> getSegmented(final double x, final int before, final int after) {
 		List<XYDatum> data = new ArrayList<XYDatum>();
 
 		// our remaining count
 		int remainingBefore = before;
 		int remainingAfter = after;
 
 		// get our starting segment
 		int key = function.hash(x);
 		List<XYDatum> segment = segments.get(key);
 		if (segment != null) {
 			int i = Collections.binarySearch(segment, new XYDatum(x, 0));
 			// matched so add
 			if (i >= 0) {
 				data.add(segment.get(i));
 			}
 
 			// get points before in this same segment
 			if (i < 0) {
 				i = -(i + 1);
 			}
 			List<XYDatum> list = tail(segment.subList(0, i), remainingBefore);
 			data.addAll(list);
 			remainingBefore -= list.size();
 
 			// gets the points after in this same segment
 			list = head(segment.subList(i, segment.size()), remainingAfter);
 			data.addAll(list);
 			remainingAfter -= list.size();
 		}
 
 		// spans segments so check before and after
 		if (remainingBefore > 0) {
 			SortedMap<Integer, List<XYDatum>> headMap = segments.headMap(key);
 			while (!headMap.isEmpty() && (remainingBefore > 0)) {
 				List<XYDatum> foo = tail(headMap.get(headMap.lastKey()), remainingBefore);
 				data.addAll(0, foo);
 				remainingBefore -= foo.size();
 				headMap = headMap.headMap(headMap.lastKey());
 			}
 		}
 		if (remainingAfter > 0) {
 			SortedMap<Integer, List<XYDatum>> tailMap = segments.tailMap(key);
 			while (!tailMap.isEmpty() && (remainingAfter > 0)) {
 				List<XYDatum> foo = head(tailMap.get(tailMap.firstKey()), remainingAfter);
 				data.addAll(foo);
 				remainingAfter -= foo.size();
 				tailMap = tailMap.tailMap(tailMap.firstKey());
 			}
 		}
 		return data;
 	}
 
 	/**
 	 * Gets the number of datums in this dataset.
 	 * 
 	 * @return the size.
 	 */
 	public int getSize() {
 		return count;
 	}
 
 	protected List<XYDatum> head(final List<XYDatum> list, final int count) {
 		return list.subList(0, Math.min(count, list.size()));
 	}
 
 	/**
 	 * Interpolate XYDatums over a range.
 	 * 
 	 * @param start
 	 *            the starting x-value.
 	 * @param end
 	 *            the ending x-value.
 	 * @param step
 	 *            the step size.
 	 * @param interpolation
 	 *            the interpolation strategy.
 	 * @return the list of interpolated data.
 	 */
 	public List<XYDatum> interpolate(final double start, final double end, final double step,
 			final Interpolation interpolation) {
 		List<XYDatum> data = new ArrayList<XYDatum>();
 		for (double x = start; x <= end; x += step) {
 			XYDatum d = interpolate(x, interpolation);
 			if (d != null) {
 				data.add(d);
 			}
 		}
 		return data;
 	}
 
 	/**
 	 * Interpolate a XYDatum for the specified x value.
 	 * 
 	 * @param x
 	 *            the x value.
 	 * @param interpolation
 	 *            the interpolation strategy.
 	 * @return the interpolated data point or null for none.
 	 */
 	public XYDatum interpolate(final double x, final Interpolation interpolation) {
 		return interpolation.interpolate(x, this);
 	}
 
 	/**
 	 * Removes all datums with the specified x-value from this dataset.
 	 * 
 	 * @param x
 	 *            the x-value.
 	 */
 	public void remove(final double x) {
 		List<XYDatum> segment = getSegment(x);
 		if (segment != null) {
 			int index = Collections.binarySearch(segment, new XYDatum(x, 0.0));
 			while (index >= 0) {
 				if (segment.remove(index) != null) {
 					count--;
 				}
 				index = Collections.binarySearch(segment, new XYDatum(x, 0.0));
 			}
 		}
 	}
 
 	/**
 	 * Removes a specific datum from this dataset.
 	 * 
 	 * @param XYDatum
 	 *            the datum to remove.
 	 */
 	public void remove(final XYDatum XYDatum) {
 		if (getSegment(XYDatum.x).remove(XYDatum)) {
 			count--;
 		}
 	}
 
 	protected List<XYDatum> tail(final List<XYDatum> list, final int count) {
 		return list.subList(Math.max(0, list.size() - count), list.size());
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder s = new StringBuilder();
		s.append("XYDataSet[name: " + name);
 		s.append(", points: " + count);
 		s.append(", segments: " + segments.size());
 		s.append(", max: " + max);
 		s.append(", min: " + min);
 		s.append(", max seg: " + segMax);
 		s.append(']');
 		return s.toString();
 	}
 }

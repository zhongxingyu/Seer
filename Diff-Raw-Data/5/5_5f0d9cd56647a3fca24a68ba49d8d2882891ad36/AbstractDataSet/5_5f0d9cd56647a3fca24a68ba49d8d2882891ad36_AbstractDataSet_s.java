 /**
  * 
  */
 package org.esa.beam.dataViewer3D.data.dataset;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.esa.beam.dataViewer3D.data.point.DataPoint;
 import org.esa.beam.dataViewer3D.data.point.DataPoint3D;
 import org.esa.beam.dataViewer3D.data.point.DataPoint4D;
 import org.esa.beam.dataViewer3D.data.point.SimpleDataPoint3D;
 import org.esa.beam.dataViewer3D.data.point.SimpleDataPoint4D;
 import org.esa.beam.dataViewer3D.data.source.DataSource;
 import org.esa.beam.dataViewer3D.data.type.NumericType;
 
 /**
  * Abstract data set implementation with the methods common for all subclasses.
  * 
  * @author Martin Pecka
  */
 public abstract class AbstractDataSet implements DataSet
 {
 
     /**
      * Read data points from the given sources into the given builder.
      * 
      * @param builder The builder to be filled with data.
      * @param x Data source for x coordinate.
      * @param y Data source for y coordinate.
      * @param z Data source for z coordinate.
      * @param w Data source for w coordinate.
      */
     protected static <X extends Number, Y extends Number, Z extends Number, W extends Number> void setupBuilderFromDataSources(
             Builder4D<X, Y, Z, W> builder, DataSource<X> x, DataSource<Y> y, DataSource<Z> z, DataSource<W> w)
     {
         if (x.size() != y.size() || y.size() != z.size() || z.size() != w.size())
             throw new IllegalArgumentException(AbstractDataSet.class
                     + ": cannot create a data set from sources of different sizes.");
 
         Iterator<NumericType<X>> iteratorX = x.numericTypeIterator();
         Iterator<NumericType<Y>> iteratorY = y.numericTypeIterator();
         Iterator<NumericType<Z>> iteratorZ = z.numericTypeIterator();
         Iterator<NumericType<W>> iteratorW = w.numericTypeIterator();
 
         while (iteratorX.hasNext()) {
             builder.addDataPoint(iteratorX.next(), iteratorY.next(), iteratorZ.next(), iteratorW.next());
         }
 
         iteratorX = null;
         iteratorY = null;
         iteratorZ = null;
         iteratorW = null;
     }
 
     /**
      * Read data points from the given sources into the given builder.
      * 
      * @param builder The builder to be filled with data.
      * @param x Data source for x coordinate.
      * @param y Data source for y coordinate.
      * @param z Data source for z coordinate.
      */
     protected static <X extends Number, Y extends Number, Z extends Number> void setupBuilderFromDataSources(
             Builder3D<X, Y, Z> builder, DataSource<X> x, DataSource<Y> y, DataSource<Z> z)
     {
         if (x.size() != y.size() || y.size() != z.size())
             throw new IllegalArgumentException(AbstractDataSet.class
                     + ": cannot create a data set from sources of different sizes.");
 
         Iterator<NumericType<X>> iteratorX = x.numericTypeIterator();
         Iterator<NumericType<Y>> iteratorY = y.numericTypeIterator();
         Iterator<NumericType<Z>> iteratorZ = z.numericTypeIterator();
 
         while (iteratorX.hasNext()) {
             builder.addDataPoint(iteratorX.next(), iteratorY.next(), iteratorZ.next());
         }
 
         iteratorX = null;
         iteratorY = null;
         iteratorZ = null;
     }
 
     /**
      * Create a new 3D data set from the given data sources.
      * 
      * @param maxPoints The maximum number of data points in the resulting set (<code>null</code> means the count is
      *            unbounded).
      * @param x The data source for the x coordinate.
      * @param y The data source for the y coordinate.
      * @param z The data source for the z coordinate.
      * 
      * @return A new 3D data set from the given data sources.
      */
     public static <X extends Number, Y extends Number, Z extends Number> DataSet3D<X, Y, Z> createFromDataSources(
             Integer maxPoints, DataSource<X> x, DataSource<Y> y, DataSource<Z> z)
     {
        if (maxPoints < StreamDataSet.MAX_SIZE && x.size() < StreamDataSet.MAX_SIZE) {
             return StreamDataSet.createFromDataSources(maxPoints, x, y, z);
         } else {
             throw new UnsupportedOperationException(AbstractDataSet.class
                     + ": cannot create data sets with size bigger than Integer.MAX_VALUE yet.");
         }
     }
 
     /**
      * Create a new 4D data set from the given data sources.
      * 
      * @param maxPoints The maximum number of data points in the resulting set (<code>null</code> means the count is
      *            unbounded).
      * @param x The data source for the x coordinate.
      * @param y The data source for the y coordinate.
      * @param z The data source for the z coordinate.
      * @param w The data source for the w coordinate.
      * 
      * @return A new 4D data set from the given data sources.
      */
     public static <X extends Number, Y extends Number, Z extends Number, W extends Number> DataSet4D<X, Y, Z, W> createFromDataSources(
             Integer maxPoints, DataSource<X> x, DataSource<Y> y, DataSource<Z> z, DataSource<W> w)
     {
        if (maxPoints < StreamDataSet.MAX_SIZE && x.size() < StreamDataSet.MAX_SIZE) {
             return StreamDataSet.createFromDataSources(maxPoints, x, y, z, w);
         } else {
             throw new UnsupportedOperationException(AbstractDataSet.class
                     + ": cannot create data sets with size bigger than Integer.MAX_VALUE yet.");
         }
     }
 
     /**
      * A class for easy building of the data set.
      * 
      * @author Martin Pecka
      * 
      * @param <P> The type of the data points the data set will contain.
      */
     protected static abstract class Builder<P extends DataPoint>
     {
         /** The maximum number of data points in the resulting set. */
         protected final Integer         maxPoints;
         /**
          * The number of data points this builder will get as input (<code>null</code> if the count cannot be
          * determined).
          */
         protected final Integer         inputSize;
         /** The number of already processed points. */
         protected long                  alreadyProcessedPoints = 0L;
         /** The data points that will go in the result (as keys) and their counts (as values). */
         protected final Map<P, Integer> data;
 
         /**
          * @param maxPoints The maximum number of data points in the resulting set.
          * @param inputSize The number of data points this builder will get as input (<code>null</code> if the count
          *            cannot be determined).
          * 
          * @throws UnsupportedOperationException If <code>expectedCount &gt; Integer.MAX_VALUE</code> -
          *             <code>long</code>-sized data sets hasn't been supported yet, but an implementation can be
          *             written.
          */
         protected Builder(Integer maxPoints, Integer inputSize)
         {
             if (maxPoints != null && maxPoints <= 0)
                 throw new IllegalArgumentException(
                         "The maximum number of points must be either null or a positive number, but " + maxPoints
                                 + " given.");
 
             if (inputSize != null && inputSize <= 0)
                 throw new IllegalArgumentException("The inputSize must be either null or a positive number, but "
                         + inputSize + " given.");
 
             this.maxPoints = maxPoints;
             this.inputSize = inputSize;
 
             if (inputSize == null) {
                 data = new HashMap<P, Integer>();
             } else if (maxPoints == null) {
                 if (inputSize * 4 / 3 < Integer.MAX_VALUE) {
                     data = new HashMap<P, Integer>(inputSize * 4 / 3);
                 } else {
                     throw new UnsupportedOperationException(getClass()
                             + ": Building data sets of size larger than Integer.MAX_VALUE hasn't been supported yet.");
                 }
             } else {
                 if (maxPoints * 4 / 3 < Integer.MAX_VALUE) {
                     data = new HashMap<P, Integer>(maxPoints * 4 / 3);
                 } else {
                     throw new UnsupportedOperationException(getClass()
                             + ": Building data sets of size larger than Integer.MAX_VALUE hasn't been supported yet.");
                 }
             }
         }
 
         /**
          * Add the given data point.
          * 
          * @param point The point to add.
          * 
          * @return <code>this</code> - provides fluent interface.
          */
         public Builder<P> addDataPoint(P point)
         {
             alreadyProcessedPoints++;
 
             if (data.containsKey(point)) {
                 data.put(point, data.get(point) + 1);
             } else {
                 // if we know the size of the input set and we want to constrain the result set to contain only a
                 // maximum number of entries, this probability test is a quick way to do it... if all input data points
                 // were different, it'd work fine, but with some equal points the resulting set might be slightly
                 // smaller than maxPoints.
                 Double addProbabilty = (inputSize == null || maxPoints == null) ? null : ((double) (maxPoints - data
                         .size()) / (inputSize - alreadyProcessedPoints + 1));
                 if (addProbabilty == null || addProbabilty >= 1 || Math.random() <= addProbabilty)
                     data.put(point, 1);
             }
 
             return this;
         }
 
     }
 
     /**
      * A builder for 3D data sets.
      * 
      * @author Martin Pecka
      * 
      * @param <X> Type of the values of the x coordinate.
      * @param <Y> Type of the values of the y coordinate.
      * @param <Z> Type of the values of the z coordinate.
      */
     protected static abstract class Builder3D<X extends Number, Y extends Number, Z extends Number> extends
             Builder<DataPoint3D<NumericType<X>, NumericType<Y>, NumericType<Z>>>
     {
 
         /**
          * @param maxPoints The maximum number of data points in the resulting set.
          * @param inputSize The number of data points this builder will get as input (<code>null</code> if the count
          *            cannot be determined).
          * 
          * @throws UnsupportedOperationException If <code>expectedCount &gt; Integer.MAX_VALUE</code> -
          *             <code>long</code>-sized data sets hasn't been supported yet, but an implementation can be
          *             written.
          */
         protected Builder3D(Integer maxPoints, Integer inputSize)
         {
             super(maxPoints, inputSize);
         }
 
         /**
          * Add a data point defined by the given coordinates.
          * 
          * @param x The x coordinate.
          * @param y The y coordinate.
          * @param z The z coordinate.
          * 
          * @return <code>this</code> - provides fluent interface.
          */
         public Builder<DataPoint3D<NumericType<X>, NumericType<Y>, NumericType<Z>>> addDataPoint(NumericType<X> x,
                 NumericType<Y> y, NumericType<Z> z)
         {
             DataPoint3D<NumericType<X>, NumericType<Y>, NumericType<Z>> point = new SimpleDataPoint3D<NumericType<X>, NumericType<Y>, NumericType<Z>>(
                     x, y, z);
 
             return addDataPoint(point);
         }
     }
 
     /**
      * A builder for 4D data sets.
      * 
      * @author Martin Pecka
      * 
      * @param <X> Type of the values of the x coordinate.
      * @param <Y> Type of the values of the y coordinate.
      * @param <Z> Type of the values of the z coordinate.
      * @param <W> Type of the values of the w coordinate.
      */
     protected static abstract class Builder4D<X extends Number, Y extends Number, Z extends Number, W extends Number>
             extends Builder<DataPoint4D<NumericType<X>, NumericType<Y>, NumericType<Z>, NumericType<W>>>
     {
 
         /**
          * @param maxPoints The maximum number of data points in the resulting set.
          * @param inputSize The number of data points this builder will get as input (<code>null</code> if the count
          *            cannot be determined).
          * 
          * @throws UnsupportedOperationException If <code>expectedCount &gt; Integer.MAX_VALUE</code> -
          *             <code>long</code>-sized data sets hasn't been supported yet, but an implementation can be
          *             written.
          */
         protected Builder4D(Integer maxPoints, Integer inputSize)
         {
             super(maxPoints, inputSize);
         }
 
         /**
          * Add a data point defined by the given coordinates.
          * 
          * @param x The x coordinate.
          * @param y The y coordinate.
          * @param z The z coordinate.
          * @param w The w coordinate.
          * 
          * @return <code>this</code> - provides fluent interface.
          */
         public Builder<DataPoint4D<NumericType<X>, NumericType<Y>, NumericType<Z>, NumericType<W>>> addDataPoint(
                 NumericType<X> x, NumericType<Y> y, NumericType<Z> z, NumericType<W> w)
         {
             DataPoint4D<NumericType<X>, NumericType<Y>, NumericType<Z>, NumericType<W>> point = new SimpleDataPoint4D<NumericType<X>, NumericType<Y>, NumericType<Z>, NumericType<W>>(
                     x, y, z, w);
 
             return addDataPoint(point);
         }
 
     }
 
 }

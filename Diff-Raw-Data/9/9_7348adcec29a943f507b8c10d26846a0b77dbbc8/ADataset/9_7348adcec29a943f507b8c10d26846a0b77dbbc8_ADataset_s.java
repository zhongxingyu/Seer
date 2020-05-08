 /*-
  * Copyright 2013 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 import java.io.Serializable;
 
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * Interface for our implementation of dataset that adds a lot of extra functionality
  */
 public interface ADataset extends IErrorDataset {
 
 	/**
 	 * Boolean
 	 */
 	public static final int BOOL = 0;
 
 	/**
 	 * Signed 8-bit integer
 	 */
 	public static final int INT8 = 1;
 
 	/**
 	 * Signed 16-bit integer
 	 */
 	public static final int INT16 = 2;
 
 	/**
 	 * Signed 32-bit integer
 	 */
 	public static final int INT32 = 3;
 	/**
 	 * Integer (same as signed 32-bit integer)
 	 */
 	public static final int INT = INT32;
 
 	/**
 	 * Signed 64-bit integer
 	 */
 	public static final int INT64 = 4;
 
 	/**
 	 * 32-bit floating point
 	 */
 	public static final int FLOAT32 = 5;
 
 	/**
 	 * 64-bit floating point
 	 */
 	public static final int FLOAT64 = 6;
 
 	/**
 	 * Floating point (same as 64-bit floating point)
 	 */
 	public static final int FLOAT = FLOAT64;
 
 	/**
 	 * 64-bit complex floating point (real and imaginary parts are 32-bit floats)
 	 */
 	public static final int COMPLEX64 = 7;
 
 	/**
 	 * 128-bit complex floating point (real and imaginary parts are 64-bit floats)
 	 */
 	public static final int COMPLEX128 = 8;
 
 	/**
 	 * Complex floating point (same as 64-bit floating point)
 	 */
 	public static final int COMPLEX = COMPLEX128;
 
 	/**
 	 * String
 	 */
 	public static final int STRING = 9;
 
 	/**
 	 * Object
 	 */
 	public static final int OBJECT = 10;
 
 	static final int ARRAYMUL = 100;
 
 	/**
 	 * Array of signed 8-bit integers
 	 */
 	public static final int ARRAYINT8 = ARRAYMUL * INT8;
 
 	/**
 	 * Array of signed 16-bit integers
 	 */
 	public static final int ARRAYINT16 = ARRAYMUL * INT16;
 
 	/**
 	 * Array of three signed 16-bit integers for RGB values
 	 */
 	public static final int RGB = ARRAYINT16 + 3;
 
 	/**
 	 * Array of signed 32-bit integers
 	 */
 	public static final int ARRAYINT32 = ARRAYMUL * INT32;
 
 	/**
 	 * Array of signed 64-bit integers
 	 */
 	public static final int ARRAYINT64 = ARRAYMUL * INT64;
 
 	/**
 	 * Array of 32-bit floating points
 	 */
 	public static final int ARRAYFLOAT32 = ARRAYMUL * FLOAT32;
 
 	/**
 	 * Array of 64-bit floating points
 	 */
 	public static final int ARRAYFLOAT64 = ARRAYMUL * FLOAT64;
 
 	/**
 	 * The shape (or array of lengths for each dimension) of the dataset can be empty for zero-rank
 	 * datasets
 	 * 
 	 * @return reference of shape of dataset
 	 */
 	public int[] getShapeRef();
 
 	/**
 	 * @return type of data item
 	 */
 	public int getDtype();
 
 	/**
 	 * @return true if dataset has elements which are floating point values
 	 */
 	public boolean hasFloatingPointElements();
 
 	/**
 	 * @return number of bytes used (does not include reserved space)
 	 */
 	public int getNbytes();
 
 	/**
 	 * @return the buffer that backs the dataset
 	 */
 	public Serializable getBuffer();
 
 	/**
 	 * This is a <b>synchronized</b> version of the clone method
 	 * 
 	 * @return a copy of dataset
 	 */
 	public ADataset synchronizedCopy();
 
 	/**
 	 * @return whole view of dataset (i.e. data buffer is shared)
 	 */
 	public ADataset getView();
 
 	/**
 	 * @param showData
 	 * @return string representation
 	 */
 	public String toString(boolean showData);
 
 	@Override
 	public ADataset clone();
 
 	/**
 	 * This function allows anything that dirties the dataset to set stored values to null so that the other functions
 	 * can work correctly.
 	 */
 	public void setDirty();
 
 	/**
 	 * The n-D position in the dataset of the given index in the data array
 	 * 
 	 * @param n
 	 *            The index in the array
 	 * @return the corresponding [a,b,...,n] position in the dataset
 	 */
 	public int[] getNDPosition(int n);
 
 	/**
 	 * Check that axis is in range [-rank,rank)
 	 * 
 	 * @param axis
 	 * @return sanitized axis in range [0, rank)
 	 */
 	public int checkAxis(int axis);
 
 	/**
 	 * This function takes a dataset and checks its shape against the current dataset. If they are both of the same
 	 * size, then this returns true otherwise it returns false.
 	 * 
 	 * @param g
 	 *            The dataset to be compared
 	 * @return true if shapes are compatible
 	 */
 	public boolean isCompatibleWith(ILazyDataset g);
 
 	/**
 	 * This function takes a dataset and checks its shape against the current dataset. If they are both of the same
 	 * size, then this returns with no error, if there is a problem, then an error is thrown.
 	 * 
 	 * @param g
 	 *            The dataset to be compared
 	 * @throws IllegalArgumentException
 	 *             This will be thrown if there is a problem with the compatibility
 	 */
 	public void checkCompatibility(ILazyDataset g) throws IllegalArgumentException;
 
 	/**
 	 * Returns dataset with new shape but old data <b>Warning</b> only works for un-expanded datasets! Copy the dataset
 	 * first
 	 * 
 	 * @param shape
 	 *            new shape
 	 */
 	public ADataset reshape(int... shape);
 
 	/**
 	 * @return true if dataset is complex
 	 */
 	public boolean isComplex();
 
 	/**
 	 * @return real part of dataset as new dataset
 	 */
 	public ADataset real();
 
 
 	/**
 	 * Get the error array from the dataset, or creates an error array if all 
 	 * values are the same
 	 * @return the dataset which contains the error information (can be null)
 	 */
 	@Override
 	public ADataset getError();
 
 	/**
 	 * Get the buffer that backs the error data
 	 * @return the buffer which contains the error information (can be null)
 	 */
 	public Serializable getErrorBuffer();
 
 	/**
 	 * Set the buffer that backs the error data
 	 * @buffer the buffer which contains the error information (can be null)
 	 */
 	public void setErrorBuffer(Serializable buffer);
 
 	/**
 	 * Cast a dataset
 	 * 
 	 * @param dtype
 	 *            dataset type
 	 * @return a converted dataset
 	 */
 	public ADataset cast(int dtype);
 
 	/**
 	 * Cast a dataset
 	 * 
 	 * @param repeat
 	 * @param dtype
 	 *            dataset type
 	 * @param isize
 	 *            item size
 	 * @return a converted dataset
 	 */
 	public ADataset cast(boolean repeat, int dtype, int isize);
 
 	/**
 	 * Generate an index dataset for current dataset
 	 * 
 	 * @return an index dataset
 	 */
 	public IntegerDataset getIndices();
 
 	/**
 	 * Permute copy of dataset's axes so that given order is old order:
 	 * 
 	 * <pre>
 	 *  axisPerm = (p(0), p(1),...) => newdata(n(0), n(1),...) = olddata(o(0), o(1), ...)
 	 *  such that n(i) = o(p(i)) for all i
 	 * </pre>
 	 * 
 	 * I.e. for a 3D dataset (1,0,2) implies the new dataset has its 1st dimension running along the old dataset's 2nd
 	 * dimension and the new 2nd is the old 1st. The 3rd dimension is left unchanged.
 	 * 
 	 * @param axes
 	 *            if zero length then axes order reversed
 	 * @return remapped copy of data
 	 */
 	public ADataset transpose(int... axes);
 
 	/**
 	 * Swap two axes in dataset
 	 * 
 	 * @param axis1
 	 * @param axis2
 	 * @return swapped dataset
 	 */
	public ADataset swapaxes(int axis1, int axis2);
 
 	/**
 	 * Flatten shape
 	 * 
 	 * @return a flattened dataset which is a view if dataset is contiguous otherwise is a copy
 	 */
 	public ADataset flatten();
 
 	/**
 	 * Fill dataset from object at depth dimension
 	 * @param obj
 	 * @param depth
 	 * @param pos position
 	 */
 	public void fillData(Object obj, int depth, int[] pos);
 
 	/**
 	 * @param withPosition
 	 *            set true if position is needed
 	 * @return an IndexIterator tailored for this dataset
 	 */
 	public IndexIterator getIterator(boolean withPosition);
 
 	/**
 	 * @return an IndexIterator tailored for this dataset
 	 */
 	public IndexIterator getIterator();
 
 	/**
 	 * @param axes
 	 * @return a PositionIterator that misses out axes
 	 */
 	public IndexIterator getPositionIterator(int... axes);
 
 	/**
 	 * @param start
 	 *            specifies the starting indexes
 	 * @param stop
 	 *            specifies the stopping indexes (nb, these are <b>not</b> included in the slice)
 	 * @param step
 	 *            specifies the steps in the slice
 	 * @return an slice iterator that operates like an IndexIterator
 	 */
 	public IndexIterator getSliceIterator(int[] start, int[] stop, int[] step);
 
 	/**
 	 * Get a slice iterator that is defined by a starting position and a set of axes to include
 	 * 
 	 * @param pos
 	 * @param axes
 	 *            to include
 	 * @return slice iterator
 	 */
 	public IndexIterator getSliceIteratorFromAxes(int[] pos, boolean[] axes);
 
 	/**
 	 * Copy content from axes in given position to array
 	 * 
 	 * @param pos
 	 *            - null means position at origin
 	 * @param axes
 	 *            - true means copy
 	 * @param dest
 	 */
 	public void copyItemsFromAxes(int[] pos, boolean[] axes, ADataset dest);
 
 	/**
 	 * Set content on axes in given position to values in array
 	 * 
 	 * @param pos
 	 * @param axes
 	 *            - true means copy
 	 * @param src
 	 */
 	public void setItemsOnAxes(int[] pos, boolean[] axes, Object src);
 
 	/**
 	 * This is modelled after the NumPy array slice
 	 * @param obj
 	 *            specifies the object used to set the specified slice
 	 * @param start
 	 *            specifies the starting indexes
 	 * @param stop
 	 *            specifies the stopping indexes (nb, these are <b>not</b> included in the slice)
 	 * @param step
 	 *            specifies the steps in the slice
 	 * 
 	 * @return The dataset with the sliced set to object
 	 */
 	public ADataset setSlice(Object obj, int[] start, int[] stop, int[] step);
 
 	/**
 	 * @param obj
 	 *            specifies the object used to set the specified slice
 	 * @param iterator
 	 *            specifies the slice iterator
 	 * 
 	 * @return The dataset with the sliced set to object
 	 */
 	public ADataset setSlice(Object obj, IndexIterator iterator);
 
 	/**
 	 * Get an iterator that visits every item in this dataset where the corresponding item in choice dataset is true
 	 * 
 	 * @param choice
 	 * @return an iterator of dataset that visits items chosen by given choice dataset
 	 */
 	public IndexIterator getBooleanIterator(ADataset choice);
 
 	/**
 	 * Get an iterator that visits every item in this dataset where the corresponding item in choice dataset
 	 * is given by value
 	 * 
 	 * @param choice
 	 * @param value
 	 * @return an iterator of dataset that visits items chosen by given choice dataset
 	 */
 	public IndexIterator getBooleanIterator(ADataset choice, boolean value);
 
 	/**
 	 * This is modelled after the NumPy get item with a condition specified by a boolean dataset
 	 * 
 	 * @param selection
 	 *            a boolean dataset of same shape to use for selecting items
 	 * @return The new selected dataset
 	 */
 	public ADataset getByBoolean(ADataset selection);
 
 	/**
 	 * This is modelled after the NumPy set item with a condition specified by a boolean dataset
 	 * @param obj
 	 *            specifies the object used to set the selected items
 	 * @param selection
 	 *            a boolean dataset of same shape to use for selecting items
 	 * 
 	 * @return The dataset with modified content
 	 */
 	public ADataset setByBoolean(Object obj, ADataset selection);
 
 	/**
 	 * This is modelled after the NumPy get item with an index dataset
 	 * 
 	 * @param index
 	 *            an integer dataset
 	 * @return The new selected dataset by indices
 	 */
 	public ADataset getByIndex(IntegerDataset index);
 
 	/**
 	 * This is modelled after the NumPy get item with an array of indexing objects
 	 * @param index
 	 *            an array of integer dataset, boolean dataset, slices or null entries (same as full slices)
 	 * @return The new selected dataset by index
 	 */
 	public ADataset getByIndexes(Object... index);
 
 	/**
 	 * This is modelled after the NumPy set item with an index dataset
 	 * @param obj
 	 *            specifies the object used to set the selected items
 	 * @param index
 	 *            an integer dataset
 	 * 
 	 * @return The dataset with modified content
 	 */
 	public ADataset setByIndex(Object obj, ADataset index);
 
 	/**
 	 * This is modelled after the NumPy set item with an array of indexing objects
 	 * @param obj
 	 *            specifies the object used to set the selected items
 	 * @param index
 	 *            an array of integer dataset, boolean dataset, slices or null entries (same as full slices)
 	 * 
 	 * @return The dataset with modified content
 	 */
 	public ADataset setByIndexes(Object obj, Object... index);
 
 	/**
 	 * Fill dataset with number represented by given object
 	 * 
 	 * @param obj
 	 * @return filled dataset
 	 */
 	public ADataset fill(Object obj);
 
 	/**
 	 * Get an element from given absolute index as a boolean - note this index does not take in account the item size so
 	 * be careful when using with multi-element items
 	 * 
 	 * @param index
 	 * @return element as boolean
 	 */
 	public boolean getElementBooleanAbs(int index);
 
 	/**
 	 * Get an element from given absolute index as a double - note this index does not take in account the item size so
 	 * be careful when using with multi-element items
 	 * 
 	 * @param index
 	 * @return element as double
 	 */
 	public double getElementDoubleAbs(int index);
 
 	/**
 	 * Get an element from given absolute index as a long - note this index does not take in account the item size so be
 	 * careful when using with multi-element items
 	 * 
 	 * @param index
 	 * @return element as long
 	 */
 	public long getElementLongAbs(int index);
 
 	/**
 	 * Get an item from given absolute index as an object - note this index does not take in account the item size so be
 	 * careful when using with multi-element items
 	 * 
 	 * @param index
 	 * @return item
 	 */
 	public Object getObjectAbs(int index);
 
 	/**
 	 * Get an item from given absolute index as a string - note this index does not take in account the item size so be
 	 * careful when using with multi-element items
 	 * 
 	 * @param index
 	 * @return item
 	 */
 	public String getStringAbs(int index);
 
 	/**
 	 * Set an item at absolute index from an object - note this index does not take into account the item size so be
 	 * careful when using with multi-element items
 	 * @param index
 	 * @param obj
 	 */
 	public void setObjectAbs(int index, Object obj);
 
 	/**
 	 * In-place sort of dataset
 	 * 
 	 * @param axis
 	 *            to sort along
 	 * @return sorted dataset
 	 */
 	public ADataset sort(Integer axis);
 
 	@Override
 	public ADataset getSlice(int[] start, int[] stop, int[] step);
 
 	@Override
 	public ADataset getSlice(IMonitor mon, int[] start, int[] stop, int[] step);
 
 	@Override
 	public ADataset getSlice(Slice... slice);
 
 	@Override
 	public ADataset getSlice(IMonitor mon, Slice... slice);
 
 	/**
 	 * Get a slice of the dataset. The returned dataset is a copied selection of items
 	 * 
 	 * @param iterator Slice iterator
 	 * @return The dataset of the sliced data
 	 */
 	public ADataset getSlice(SliceIterator iterator);
 
 	@Override
 	public ADataset getSliceView(int[] start, int[] stop, int[] step);
 
 	@Override
 	public ADataset getSliceView(Slice... slice);
 
 	/**
 	 * 
 	 * @param object
 	 * @param slice
 	 */
 	public void setSlice(Object object, Slice... slice);
 
 	/**
 	 * Populate a dataset with part of current dataset
 	 * 
 	 * @param result
 	 * @param iter
 	 *            over current dataset
 	 */
 	public void fillDataset(ADataset result, IndexIterator iter);
 
 	/**
 	 * Test if all items are true
 	 */
 	public boolean all();
 
 	/**
 	 * @param axis
 	 * @return dataset where items are true if all items along axis are true
 	 */
 	public ADataset all(int axis);
 
 	/**
 	 * Test if any items are true
 	 */
 	public boolean any();
 
 	/**
 	 * @param axis
 	 * @return dataset where items are true if any items along axis are true
 	 */
 	public ADataset any(int axis);
 
 	/**
 	 * In-place addition with object o
 	 * 
 	 * @param o
 	 * @return sum dataset
 	 */
 	public ADataset iadd(Object o);
 
 	/**
 	 * In-place subtraction with object o
 	 * 
 	 * @param o
 	 * @return difference dataset
 	 */
 	public ADataset isubtract(Object o);
 
 	/**
 	 * In-place multiplication with object o
 	 * 
 	 * @param o
 	 * @return product dataset
 	 */
 	public ADataset imultiply(Object o);
 
 	/**
 	 * In-place division with object o
 	 * 
 	 * @param o
 	 * @return dividend dataset
 	 */
 	public ADataset idivide(Object o);
 
 	/**
 	 * In-place floor division with object o
 	 * 
 	 * @param o
 	 * @return dividend dataset
 	 */
 	public ADataset ifloorDivide(Object o);
 
 	/**
 	 * In-place remainder
 	 * 
 	 * @return remaindered dataset
 	 */
 	public ADataset iremainder(Object o);
 
 	/**
 	 * In-place floor
 	 * 
 	 * @return floored dataset
 	 */
 	public ADataset ifloor();
 
 	/**
 	 * In-place raise to power of object o
 	 * 
 	 * @param o
 	 * @return raised dataset
 	 */
 	public ADataset ipower(Object o);
 
 	/**
 	 * Calculate residual of dataset with object o
 	 * See {@link #residual(Object o, boolean ignoreNaNs)} with ignoreNaNs = false
 	 * 
 	 * @param o
 	 * @return sum of the squares of the differences
 	 */
 	public double residual(Object o);
 
 	/**
 	 * Calculate residual of dataset with object o
 	 * 
 	 * @param o
 	 * @param ignoreNaNs if true, skip NaNs
 	 * @return sum of the squares of the differences
 	 */
 	public double residual(Object o, boolean ignoreNaNs);
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and Infs
 	 * @return position of maximum value
 	 */
 	public int[] maxPos(boolean ignoreInvalids);
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and Infs
 	 * @return position of minimum value
 	 */
 	public int[] minPos(boolean ignoreInvalids);
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return maximum
 	 */
 	public Number max(boolean ignoreInvalids);
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @return minimum
 	 */
 	public Number max(boolean ignoreNaNs, boolean ignoreInfs);
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return minimum positive value (or infinity if there are no positive values)
 	 */
 	public Number positiveMax(boolean ignoreInvalids);
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @return maximum positive value (or Double.MIN_VALUE=2^-1024 if there are no positive values)
 	 */
 	public Number positiveMax(boolean ignoreNaNs, boolean ignoreInfs);
 
 	/**
 	 * See {@link #max(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return maxima along axis in dataset
 	 */
 	public ADataset max(int axis);
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param axis
 	 * @return maxima along axis in dataset
 	 */
 	public ADataset max(boolean ignoreNaNs, int axis);
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return minimum
 	 */
 	public Number min(boolean ignoreInvalids);
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @return minimum
 	 */
 	public Number min(boolean ignoreNaNs, boolean ignoreInfs);
 
 	/**
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return minimum positive value (or infinity if there are no positive values)
 	 */
 	public Number positiveMin(boolean ignoreInvalids);
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param ignoreInfs if true, ignore infinities
 	 * @return minimum positive value (or infinity if there are no positive values)
 	 */
 	public Number positiveMin(boolean ignoreNaNs, boolean ignoreInfs);
 
 	/**
 	 * See {@link #min(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return minima along axis in dataset
 	 */
 	public ADataset min(int axis);
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param axis
 	 * @return minima along axis in dataset
 	 */
 	public ADataset min(boolean ignoreNaNs, int axis);
 
 	/**
 	 * Find absolute index of maximum value.
 	 * See {@link #argMax(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * 
 	 * @return absolute index
 	 */
 	public int argMax();
 
 	/**
 	 * Find absolute index of maximum value
 	 * 
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return absolute index
 	 */
 	public int argMax(boolean ignoreInvalids);
 
 	/**
 	 * Find indices of maximum values along given axis.
 	 * See {@link #argMax(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * 
 	 * @param axis
 	 * @return index dataset
 	 */
 	public ADataset argMax(int axis);
 
 	/**
 	 * Find indices of maximum values along given axis
 	 * 
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param axis
 	 * @return index dataset
 	 */
 	public ADataset argMax(boolean ignoreNaNs, int axis);
 
 	/**
 	 * Find absolute index of minimum value.
 	 * See {@link #argMin(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * 
 	 * @return absolute index
 	 */
 	public int argMin();
 
 	/**
 	 * Find absolute index of minimum value
 	 * 
 	 * @param ignoreInvalids if true, ignore NaNs and infinities
 	 * @return absolute index
 	 */
 	public int argMin(boolean ignoreInvalids);
 
 	/**
 	 * Find indices of minimum values along given axis.
 	 * See {@link #argMin(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * 
 	 * @param axis
 	 * @return index dataset
 	 */
 	public ADataset argMin(int axis);
 
 	/**
 	 * Find indices of minimum values along given axis
 	 * 
 	 * @param ignoreNaNs if true, ignore NaNs
 	 * @param axis
 	 * @return index dataset
 	 */
 	public ADataset argMin(boolean ignoreNaNs, int axis);
 
 	/**
 	 * @return true if dataset contains any infinities
 	 */
 	public boolean containsInfs();
 
 	/**
 	 * @return true if dataset contains any NaNs
 	 */
 	public boolean containsNans();
 
 	/**
 	 * @return true if dataset contains any NaNs or infinities
 	 */
 	public boolean containsInvalidNumbers();
 
 	/**
 	 * @return peak-to-peak value, the difference of maximum and minimum of dataset
 	 */
 	public Number peakToPeak();
 
 	/**
 	 * @param axis
 	 * @return peak-to-peak dataset, the difference of maxima and minima of dataset along axis
 	 */
 	public ADataset peakToPeak(int axis);
 
 	/**
 	 * See {@link #count(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * @return number of items in dataset
 	 */
 	public long count();
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs (treat as zeros)
 	 * @return number of items in dataset
 	 */
 	public long count(boolean ignoreNaNs);
 
 	/**
 	 * See {@link #count(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return number of items along axis in dataset
 	 */
 	public ADataset count(int axis);
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs (treat as zeros)
 	 * @param axis
 	 * @return number of items along axis in dataset
 	 */
 	public ADataset count(boolean ignoreNaNs, int axis);
 
 	/**
 	 * See {@link #sum(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * @return sum over all items in dataset as a Double, array of doubles or a complex number
 	 */
 	public Object sum();
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs (treat as zeros)
 	 * @return sum over all items in dataset as a Double, array of doubles or a complex number
 	 */
 	public Object sum(boolean ignoreNaNs);
 
 	/**
 	 * See {@link #sum(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return sum along axis in dataset
 	 */
 	public ADataset sum(int axis);
 
 	/**
 	 * @param ignoreNaNs if true, ignore NaNs (treat as zeros)
 	 * @param axis
 	 * @return sum along axis in dataset
 	 */
 	public ADataset sum(boolean ignoreNaNs, int axis);
 
 	/**
 	 * @return sum over all items in dataset as appropriate to dataset type
 	 * (integers for boolean, byte, short and integer; longs for long; floats for float; doubles for double)
 	 */
 	public Object typedSum();
 
 	/**
 	 * @param dtype
 	 * @return sum over all items in dataset as appropriate to given dataset type
 	 */
 	public Object typedSum(int dtype);
 
 	/**
 	 * @param dtype
 	 * @param axis
 	 * @return sum along axis in dataset
 	 */
 	public ADataset typedSum(int dtype, int axis);
 
 	/**
 	 * @return product over all items in dataset
 	 */
 	public Object product();
 
 	/**
 	 * @param axis
 	 * @return product along axis in dataset
 	 */
 	public ADataset product(int axis);
 
 	/**
 	 * @param dtype
 	 * @return product over all items in dataset
 	 */
 	public Object typedProduct(int dtype);
 
 	/**
 	 * @param dtype
 	 * @param axis
 	 * @return product along axis in dataset
 	 */
 	public ADataset typedProduct(int dtype, int axis);
 
 	/**
 	 * See {@link #mean(boolean ignoreNaNs)} with ignoreNaNs = false
 	 * @return mean of all items in dataset as a double, array of doubles or a complex number
 	 */
 	@Override
 	public Object mean();
 
 	/**
 	 * @param ignoreNaNs if true, skip NaNs
 	 * @return mean of all items in dataset as a double, array of doubles or a complex number
 	 */
 	public Object mean(boolean ignoreNaNs);
 
 	/**
 	 * See {@link #mean(boolean ignoreNaNs, int axis)} with ignoreNaNs = false
 	 * @param axis
 	 * @return mean along axis in dataset
 	 */
 	public ADataset mean(int axis);
 
 	/**
 	 * @param ignoreNaNs if true, skip NaNs
 	 * @param axis
 	 * @return mean along axis in dataset
 	 */
 	public ADataset mean(boolean ignoreNaNs, int axis);
 
 	/**
 	 * @return sample variance of whole dataset
 	 * @see #variance(boolean)
 	 */
 	public Number variance();
 
 	/**
 	 * The sample variance can be calculated in two ways: if the dataset is considered as the entire population then the
 	 * sample variance is simply the second central moment:
 	 * 
 	 * <pre>
 	 *    sum((x_i - m)^2)/N
 	 * where {x_i} are set of N population values and m is the mean
 	 *    m = sum(x_i)/N
 	 * </pre>
 	 * 
 	 * Otherwise, if the dataset is a set of samples (with replacement) from the population then
 	 * 
 	 * <pre>
 	 *    sum((x_i - m)^2)/(N-1)
 	 * where {x_i} are set of N sample values and m is the unbiased estimate of the mean
 	 *    m = sum(x_i)/N
 	 * </pre>
 	 * 
 	 * Note that the second definition is also the unbiased estimator of population variance.
 	 * 
 	 * @param isDatasetWholePopulation
 	 * @return sample variance
 	 */
 	public Number variance(boolean isDatasetWholePopulation);
 
 	/**
 	 * @param axis
 	 * @return sample variance along axis in dataset
 	 * @see #variance(boolean)
 	 */
 	public ADataset variance(int axis);
 
 	/**
 	 * Standard deviation is square root of the variance
 	 * 
 	 * @return sample standard deviation of all items in dataset
 	 * @see #variance()
 	 */
 	public Number stdDeviation();
 
 	/**
 	 * Standard deviation is square root of the variance
 	 * 
 	 * @param isDatasetWholePopulation
 	 * @return sample standard deviation of all items in dataset
 	 * @see #variance(boolean)
 	 */
 	public Number stdDeviation(boolean isDatasetWholePopulation);
 
 	/**
 	 * @param axis
 	 * @return standard deviation along axis in dataset
 	 */
 	public ADataset stdDeviation(int axis);
 
 	/**
 	 * @return root mean square
 	 */
 	public Number rootMeanSquare();
 
 	/**
 	 * @param axis
 	 * @return root mean square along axis in dataset
 	 */
 	public ADataset rootMeanSquare(int axis);
 
 	/**
 	 * @see DatasetUtils#put(AbstractDataset, int[], Object[])
 	 */
 	public ADataset put(int[] indices, Object[] values);
 
 	/**
 	 * @see DatasetUtils#take(AbstractDataset, int[], Integer)
 	 */
 	public ADataset take(int[] indices, Integer axis);
 }

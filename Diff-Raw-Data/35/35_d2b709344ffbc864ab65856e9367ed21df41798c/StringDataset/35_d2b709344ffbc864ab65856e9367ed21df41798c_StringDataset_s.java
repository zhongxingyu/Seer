 /*-
  * Copyright 2011 Diamond Light Source Ltd.
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
 
 /**
  * Extend dataset for objects
  */
 public class StringDataset extends StringDatasetBase {
 	// pin UID to base class
 	private static final long serialVersionUID = AbstractDataset.serialVersionUID;
 
 	/**
 	 * Setup the logging facilities
 	 */
 //	transient private static final Logger logger = LoggerFactory.getLogger(ObjectDataset.class);
 
 	public StringDataset() {
 		super();
 	}
 
 	/**
 	 * Create a null-filled dataset of given shape
 	 * @param shape
 	 */
 	public StringDataset(final int... shape) {
 		super(shape);
 	}
 
 	/**
 	 * Create a dataset using given data
 	 * @param data
 	 * @param shape
 	 *            (can be null to create 1D dataset)
 	 */
 	public StringDataset(final String[] data, int... shape) {
 		super(data, shape);
 	}
 
 	/**
 	 * Copy a dataset
 	 * @param dataset
 	 */
 	public StringDataset(final StringDataset dataset) {
 		super(dataset);
 	}
 
 	/**
 	 * Cast a dataset to this class type
 	 * @param dataset
 	 */
 	public StringDataset(final AbstractDataset dataset) {
 		super(dataset);
 	}
 
 	@Override
 	public StringDataset getView() {
 		StringDataset view = new StringDataset();
 		copyToView(this, view, true, true);
 		view.data = data;
 		return view;
 	}
 
 	/**
 	 * Create a dataset from an object which could be a PySequence, a Java array (of arrays...)
 	 * or Number. Ragged sequences or arrays are padded with zeros.
 	 * 
 	 * @param obj
 	 * @return dataset with contents given by input
 	 */
 	public static StringDataset createFromObject(final Object obj) {
 		StringDatasetBase result = StringDatasetBase.createFromObject(obj);
 		return new StringDataset(result.data, result.shape);
 	}
 
 	/**
 	 * @param shape
 	 * @return a dataset filled with ones
 	 */
 	public static StringDataset ones(final int... shape) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public boolean getElementBooleanAbs(int index) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public double getElementDoubleAbs(int index) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public long getElementLongAbs(int index) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public double getDouble(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public long getLong(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public float getFloat(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public int getInt(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public short getShort(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public byte getByte(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public boolean getBoolean(int... pos) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset getSlice(final int[] start, final int[] stop, final int[] step) {
 		StringDatasetBase result = (StringDatasetBase) super.getSlice(start, stop, step);
 
 		return new StringDataset(result.data, result.shape);
 	}
 
 	@Override
 	public int[] minPos() {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public int[] maxPos() {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public boolean containsInfs() {
 		return false;
 	}
 
 	@Override
 	public boolean containsNans() {
 		return false;
 	}
 
 	@Override
 	public StringDataset iadd(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset isubtract(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset imultiply(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset idivide(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset iremainder(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset ifloor() {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public StringDataset ipower(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 
 	@Override
 	public double residual(Object o) {
 		throw new UnsupportedOperationException("Unsupported method for class");
 	}
 }
